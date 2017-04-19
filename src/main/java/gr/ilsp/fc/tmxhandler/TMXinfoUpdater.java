package gr.ilsp.fc.tmxhandler;

import gr.ilsp.fc.aligner.factory.ILSPAlignment;
import gr.ilsp.fc.corpora.BilingualCorpusInformation;
import gr.ilsp.fc.tmxhandler.TMXHandlerUtils.SegPair;
import gr.ilsp.fc.tmxhandler.TMXHandlerUtils.TUsNumStats;
import gr.ilsp.fc.tmxhandler.TMXHandlerUtils.TUsRatioStats;
import gr.ilsp.fc.tmxhandler.TMXHandlerUtils.TUsScoreStats;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.loomchild.maligna.util.bind.TmxMarshallerUnmarshaller;
import net.loomchild.maligna.util.bind.tmx.Prop;
import net.loomchild.maligna.util.bind.tmx.Tmx;
import net.loomchild.maligna.util.bind.tmx.Tu;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TMXinfoUpdater {

	private static final Logger logger = LoggerFactory.getLogger(TMXinfoUpdater.class);
	private static TMXinfoUpdaterOptions options = null;
	private static File targetTMX=null;
	private static String[] languages;
	//private static boolean iso6393=false;
	private static Map<String, String> psinfo;

	private static final String SEMICOLON_STR=";";	
	private final static String licenseNode = "license";
	private static final String SPACE_SEPARATOR = " ";
	private final static String SCORE = "score";
	private final static String SEGMENTTYPE = "type"; 
	private final static String L1URL = "l1-url";
	private final static String L2URL = "l2-url";
	private final static String RATIO = "lengthRatio";
	private final static String INFO = "info";
	//private static int minTuvLen=3;
	//private static double minTuLenRatio = 0.6;
	//private static double maxTuLenRatio = 1.6;
	//static Matcher twitterHandleMatcher = Pattern.compile("(^|[^@\\w])@(\\w{1,15})\\b").matcher("");
	//private static final String HTML =".html";
	//private static final String TMXEXT = ".tmx";
	//private final static String MetadataExt = ".md.xml";

	private final static String UNKNOWN_STR ="unknown";



	public static void main(String[] args){
		TMXinfoUpdater tmu = new TMXinfoUpdater();
		options = new TMXinfoUpdaterOptions();
		options.parseOptions(args);
		tmu.setTargetTMX(options.getTargetTMX());
		//tmu.setMinTuLenRatio(options.getMinTuLenRatio());
		//tmu.setMaxTuLenRatio(options.getMaxTuLenRatio());
		//tmu.setMinTuvLen(options.getMinTuvLen());
		tmu.setLanguage(options.getLanguage());
		//tmu.useISO6393(options.useISO6393());

		tmu.setPSinfo(options.getPSinfo());

		//Tmx tmx = TmxMarshallerUnmarshaller.getInstance().unmarshal(new FileReader(targetTMX));
		//List<Object> headerProps = tmx.getHeader().getNoteOrPropOrUde();

		List<SegPair> segpairs =  addPSinfo();

		List<ILSPAlignment> alignmentList = new ArrayList<ILSPAlignment>();
		alignmentList = getSegs(segpairs,alignmentList);
		if (!alignmentList.isEmpty()){
			TUsNumStats stats1 =TMXHandlerUtils.numstats(alignmentList, 1, true);
			TUsNumStats stats2 =TMXHandlerUtils.numstats(alignmentList, 2, true);
			TUsScoreStats scores = TMXHandlerUtils.scorestats(alignmentList, true);
			TUsRatioStats ratios = TMXHandlerUtils.ratiostats(alignmentList, true);

			BilingualCorpusInformation bilingualCorpusInfo;
			/*bilingualCorpusInfo = new BilingualCorpusInformation(FilenameUtils.getBaseName(outTMX.getAbsolutePath()), languages[0], languages[1], 
					alignmentList, alignmentList.size(), stats1.tus_noan,
					stats1.tokens_all, stats2.tokens_all, stats1.words_all, stats2.words_all, 
					stats1.tokens_noan, stats2.tokens_noan, stats1.words_noan, stats2.words_noan, 
					scores.meanscore_all, scores.stdscore_all, ratios.meanratio_all, ratios.stdratio_all,
					scores.meanscore_noan, scores.stdscore_noan, ratios.meanratio_noan, ratios.stdratio_noan,
					domain, domainEurovocId, UNKNOWN_STR, creationDescription,
					projectId, projectURL, organization, organizationURL);*/

			bilingualCorpusInfo = new BilingualCorpusInformation(FilenameUtils.getBaseName(targetTMX.getAbsolutePath()), languages[0], languages[1], 
					alignmentList, alignmentList.size(), stats1.tus_noan,
					stats1.tokens_all, stats2.tokens_all, stats1.words_all, stats2.words_all, 
					stats1.tokens_noan, stats2.tokens_noan, stats1.words_noan, stats2.words_noan, 
					scores.meanscore_all, scores.stdscore_all, ratios.meanratio_all, ratios.stdratio_all,
					scores.meanscore_noan, scores.stdscore_noan, ratios.meanratio_noan, ratios.stdratio_noan,
					"", "", UNKNOWN_STR, "",
					"", "", "", "");

			TMXHandler.generateMergedTMX(targetTMX, languages, bilingualCorpusInfo, null);
		}else{
			logger.info("No proper TUs found.");
		}
	}


	public static List<SegPair> addPSinfo() {
		List<SegPair> segpairs = new ArrayList<SegPair>();
		Tmx tmx;
		try {
			tmx = TmxMarshallerUnmarshaller.getInstance().unmarshal(new FileReader(targetTMX));
			int alignments = 0;
			int sourceSegments = 0;
			int targetSegments = 0;
			List<Tu> tus = tmx.getBody().getTu();
			for (Tu tu: tus) {
				alignments = alignments+1;
				List<Object> tuProps = tu.getNoteOrProp();
				String type="", license="", other = "", l1url="", l2url="", annot="";
				double score = 0, ratio=0;
				for (Object obProp : tuProps) {
					Prop prop = (Prop) obProp;
					if (prop.getType().equals(SCORE)) {
						score = Double.parseDouble(prop.getContent().get(0));
					} else if (prop.getType().equals(RATIO)) {
						ratio = Double.parseDouble(prop.getContent().get(0));
					} else if (prop.getType().equals(SEGMENTTYPE)) {
						type = prop.getContent().get(0);
						String[] segs = StringUtils.split(prop.getContent().get(0), ":");
						sourceSegments = sourceSegments + Integer.parseInt(segs[0]);
						targetSegments = targetSegments + Integer.parseInt(segs[1]);
					}else if (prop.getType().equals(licenseNode)){
						if (prop.getContent().isEmpty())
							license="";
						else
							license  = prop.getContent().get(0);
					}else if(prop.getType().equals(L1URL)){
						l1url = prop.getContent().get(0);
					}else if(prop.getType().equals(L2URL)){
						l2url = prop.getContent().get(0);
					}else if(prop.getType().equals(INFO)){
						if (prop.getContent().isEmpty())
							annot="";
						else
							annot = prop.getContent().get(0);
					}
				}
				if (license.isEmpty()){
					String w1 = checkPSI(l1url, psinfo);
					String w2 = checkPSI(l2url, psinfo);
					if (w1.equals(w2))
						license= w1;
					else
						license = "";
					if (license.equals("yes"))
						license = "psi";
					if (license.equals("no"))
						license = "no-psi";	
				}
				segpairs.add(new SegPair(StringUtils.join(TMXHandlerUtils.createSegmentList(tu, languages[0]), SPACE_SEPARATOR), 
						StringUtils.join(TMXHandlerUtils.createSegmentList(tu, languages[1]), SPACE_SEPARATOR),
						score, type, "",l1url, l2url, license, other,ratio,annot));
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return segpairs;
	}


	private static String checkPSI(String l1url, Map<String, String> psi) {
		String res="";
		if (StringUtils.isBlank(l1url))
			return res;
		try {
			URL url1 = new URL(l1url);
			String a = url1.getHost();
			if (psi.containsKey(a))
				res = psi.get(a);
		} catch (MalformedURLException e) {
			return res;
		}
		return res;
	}


	private static List<ILSPAlignment> getSegs(List<SegPair> segpairs,	List<ILSPAlignment> alignmentList) {
		for (SegPair segpair:segpairs){
			ILSPAlignment alignment = new ILSPAlignment();
			alignment.addSourceSegment(segpair.seg1);
			alignment.addTargetSegment(segpair.seg2);
			alignment.setScore((float)segpair.score);
			alignment.setL1url(segpair.l1url);
			alignment.setL2url(segpair.l2url);
			alignment.setMethod(segpair.method);
			alignment.setLicense(segpair.license);
			alignment.setType(segpair.type);
			alignment.setLengthRatio(Double.toString(segpair.ratio));
			alignment.setInfo(segpair.annot);
			alignmentList.add(alignment);
		}
		return alignmentList;
	}


	/**
	 * 
	 * @param pSinfo
	 */
	private void setPSinfo(Map<String, String> pSinfo) {
		TMXinfoUpdater.psinfo = pSinfo;

	}
	/**
	 * targeted TMX file to be updated with more info
	 * @param targetTMX
	 */
	private void setTargetTMX(File targetTMX) {
		TMXinfoUpdater.targetTMX = 	targetTMX;	
	}
	/**
	 * language iso codes separated by ";"
	 * @param languages
	 */
	public void setLanguage(String languages) {
		TMXinfoUpdater.languages = languages.split(SEMICOLON_STR);
	}
	//public void useISO6393(boolean iso6393) {
	//	TMXinfoUpdater.iso6393 = iso6393;
	//}
}
