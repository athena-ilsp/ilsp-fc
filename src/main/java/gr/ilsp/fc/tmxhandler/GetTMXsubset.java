package gr.ilsp.fc.tmxhandler;

import gr.ilsp.fc.aligner.factory.ILSPAlignment;
import gr.ilsp.fc.corpora.BilingualCorpusInformation;
import gr.ilsp.fc.corpora.BilingualTmxMetashareDescriptor;
import gr.ilsp.fc.tmxhandler.TMXHandlerUtils.SegPair;
import gr.ilsp.fc.tmxhandler.TMXHandlerUtils.TUsNumStats;
import gr.ilsp.fc.tmxhandler.TMXHandlerUtils.TUsRatioStats;
import gr.ilsp.fc.tmxhandler.TMXHandlerUtils.TUsScoreStats;
import gr.ilsp.fc.utils.ContentNormalizer;
import gr.ilsp.fc.utils.FCStringUtils;
import gr.ilsp.fc.utils.Statistics;
import gr.ilsp.nlp.commons.Constants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.loomchild.maligna.util.bind.TmxMarshallerUnmarshaller;
import net.loomchild.maligna.util.bind.tmx.Prop;
import net.loomchild.maligna.util.bind.tmx.Tmx;
import net.loomchild.maligna.util.bind.tmx.Tu;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetTMXsubset {

	private static final Logger logger = LoggerFactory.getLogger(GetTMXsubset.class);
	private final static String mes1 = "non-letters";
	private final static String mes2 = "equal TUVs";
	//private final static String mes3 = "very long token, longer than ";
	//private final static String mes3a = "very long tokens, median length is longer than ";
	private final static String mes4 = "very short segments, shorter than ";
	private final static String mes5 = "charlength ratio of TUVs is lower than ";
	private final static String mes5a = " or higher than ";
	private final static String mes6 = "different numbers in TUVs";
	private final static String mes7 = "duplicate";
	private final static String mes10 = "aligner's score is lower than ";
	private final static String licenseNode = "license";
	private final static String CREATIVE_COMMONS = "Creative";
	private final static String SCORE = "score";
	private final static String SEGMENTTYPE = "type"; 
	private final static String L1URL = "l1-url";
	private final static String L2URL = "l2-url";
	private final static String RATIO = "lengthRatio";
	private final static String INFO = "info";
	private final static String PSI_STR = "psi";
	private final static String NOPSI_STR = "no-psi";
	private final static String YES_STR = "yes";
	private final static String NO_STR = "no";
	private final static String UNCLEAR_STR = "unclear";

	private static boolean iso6393=false;
	private static boolean oxslt=false;
	private static boolean cc=true;
	private static boolean keepem = true;
	private static boolean keepiden = true;
	private static boolean keepdup = true;
	private static boolean keepsn=false;
	private static boolean clean=false;
	private static double minPerce01Align = 0.16;
	private static int minTuvLen=3;
	private static double minScore= 0;
	private static double minTuLenRatio = 0.6;
	private static double maxTuLenRatio = 1.6;
	private static double median_word_length=20;
	private static double max_word_length=30;
	private static int samplesize= 0;
	private static Map<String,String> psi = new HashMap<String,String>() ;
	private static Set<String> segs = new HashSet<String>() ;
	static Matcher twitterHandleMatcher = Pattern.compile("(^|[^@\\w])@(\\w{1,15})\\b").matcher("");
	private static final String HTMLEXT =".html";
	private static final String TMXEXT = ".tmx";
	private static final String TXTEXT = ".txt";
	private static final String SAMPLE = ".sample";
	private final static String MetadataExt = ".md.xml";
	private final static String TAB = "\t";
	private static File intmx = null;
	private static File baseName=null;
	private static File outTMX=null;
	private static File outHTML=null;
	private static File sampleTXT=null;
	private static String targetdomain="";

	private final static String UNKNOWN_STR ="unknown";
	private final static String FREE_STR="free";

	private static String alignerStr = "Maligna";

	private static String creationModeDescription = "The ILSP Focused Crawler was used for the acquisition "
			+ "of bilingual data from multilingual websites, and for the normalization, cleaning, deduplication and identification of parallel documents. "
			+ "The " + alignerStr + " sentence aligner was used for extracting segment alignments from crawled parallel documents. "
			+ "As a post-processing step, alignments were merged into one TMX file. "
			+ "The following filters were applied: ";
	private static String filter6 = " Alignments in which different digits appear in each TUV were kept and annotated.";
	private static String filter7 = " Alignments with identical TUVs (after normalization) were removed.";
	private static String filter8 = " Alignments with only non-letters in at least one of their TUVs were removed";
	private static String filter9 = " Duplicate alignments were annotated/discarded.";


	public static void main(String[] args){
		logger.info("-----------------------------------\n" 
				+ "1. A subset will be generated based on the user provided rules.\n"
				+ "2. A sample will be generated");

		GetTMXsubsetOptions options = new GetTMXsubsetOptions();
		options.parseOptions(args);
		intmx = options.getTargetTMX();
		String[] langs = options.getLanguage().split(Constants.SEMICOLON);
		psi = options.getPSinfo();
		baseName = options.getBaseName();
		outTMX = new File(baseName.getAbsolutePath()+TMXEXT);
		outHTML =  new File(baseName.getAbsolutePath() + HTMLEXT);
		targetdomain = options.getTargetedDomain();
		keepsn = options.keepTuSameNum();
		keepiden = options.getKeepIdentical();
		keepem = options.getKeepEmpty();
		keepdup = options.getKeepDuplicates();
		cc = options.getCC();
		maxTuLenRatio = options.getMaxTuLenRatio();
		minTuLenRatio = options.getMinTuLenRatio();
		minTuvLen=options.getMinTuvLen();
		clean = options.getClean();
		minScore = options.getMinScore();

		samplesize = options.getSampleSize();
		sampleTXT = new File(baseName.getAbsolutePath()+SAMPLE+TXTEXT);
		logger.info("Reading " + intmx.getAbsolutePath());
		logger.info("Languages: " + langs[0] + Constants.SPACE + langs[1] + Constants.SPACE);
		logger.info("Constructing " + outTMX.getAbsolutePath());

		List<ILSPAlignment> alignmentList = getTMXsubset(langs);
		
		int aa =   (int) (0.05 * alignmentList.size());
		samplesize = Math.max(samplesize, aa);
		TMXHandlerUtils.generateSample(alignmentList, samplesize, sampleTXT);

	}
	

	/**
	 * Generates a new mergedTMX which is a subset of the input TMX based on user's restrictions
	 * It also generates the MD file 
	 * @param langs
	 * @return
	 */
	public static List<ILSPAlignment> getTMXsubset(String[] langs) {
		String filter1=" TMX files generated from document pairs which have been identified by non-"+ "aupdih" + " methods were discarded.";
		String filter2=" TMX files with a zeroToOne_alignments/total_alignments ratio is larger than " + minPerce01Align +",  were nor participated into TMX merging.";
		String filter3=" Alignments of non-" + "1:1"+ " were discarded.";
		String filter4=" Alignments with a TUV (after normalization) that has less than "+ minTuvLen + " tokens, were discarded/annotated.";
		String filter5=" Alignments with a TUVs' length ratio less than " + minTuLenRatio+ " or more than "+ maxTuLenRatio + " , were discarded/annotated.";
		String filter10=" Alignments with aligner's score less than " + minScore + ", were discarded/annotated.";
		if (keepsn)
			filter6=" Alignments in which different digits appear in each TUV were discarded.";
		if (keepiden)
			filter7=" Alignments with identical TUVs (after normalization) were kept and annotated.";
		if (keepem)
			filter8=" Alignments with only non-letters in at least one of their TUVs were kept and annotated.";
		if (keepdup)
			filter9=" Duplicate alignments were kept and annotated.";
		logger.info(filter1+"\n"+filter2+"\n"+filter3+"\n"+filter4+"\n"+filter5+"\n"+filter10+"\n"+filter6+"\n"+filter7+"\n"+filter8+"\n"+filter9);

		List<String> domains = new ArrayList<String>();
		domains.add(targetdomain);
		List<String> domainEurovocIds=TMXHandler.getEurovocId(domains);
		String domain = StringUtils.join(domains, ',');
		String domainEurovocId = StringUtils.join(domainEurovocIds, ',');

		List<SegPair> segpairs = getTUsFromTMX(intmx, langs[0], langs[1]);

		List<ILSPAlignment> alignmentList = new ArrayList<ILSPAlignment>();
		alignmentList = getSelectedSegs(segpairs,alignmentList);

		if (!alignmentList.isEmpty()){
			TUsNumStats stats1 =TMXHandlerUtils.numstats(alignmentList, 1, true);
			TUsNumStats stats2 =TMXHandlerUtils.numstats(alignmentList, 2, true);
			TUsScoreStats scores = TMXHandlerUtils.scorestats(alignmentList, true);
			TUsRatioStats ratios = TMXHandlerUtils.ratiostats(alignmentList, true);
			String organization = "ILSP";
			String organizationURL = "http://www.ilsp.gr"; 
			String projectId= "ELRC"; 
			String projectURL = "http://lr-coordination.eu/"; 

			creationModeDescription = creationModeDescription+filter1+" ; "+filter2+" ; "+filter3+" ; "+filter4+" ; "+filter5+" ; "+filter6+" ; "+filter7+" ; "+filter8+" ; "+filter9;
			creationModeDescription = creationModeDescription + 
					".\nThe mean value of aligner's scores is "+ scores.meanscore_all+ ", the std value is "+ scores.stdscore_all +
					". The mean value of length (in terms of characters) ratios is "+ ratios.meanratio_all+ " and the std value is "+ ratios.stdratio_all + "." +
					"\nThere are "+ stats1.tus_noan +" TUs with no annotation,"+
					" containing "+ stats1.tokens_noan +" words and "+ stats1.words_noan +" lexical types in "+ langs[0] + 
					" and "+ stats2.tokens_noan +" words and "+ stats2.words_noan+" lexical types in "+ langs[1] +
					". The mean value of aligner's scores is "+ scores.meanscore_noan+ ", the std value is "+ scores.stdscore_noan +
					". The mean value of length (in terms of characters) ratios is "+ ratios.meanratio_noan + " and the std value is "+ ratios.stdratio_noan + "." ;
			
			
			BilingualCorpusInformation bilingualCorpusInfo;
			if (cc) {
				bilingualCorpusInfo = new BilingualCorpusInformation(FilenameUtils.getBaseName(outTMX.getAbsolutePath()), langs[0], langs[1], 
						alignmentList, alignmentList.size(), stats1.tus_noan,
						stats1.tokens_all, stats2.tokens_all, stats1.words_all, stats2.words_all, 
						stats1.tokens_noan, stats2.tokens_noan, stats1.words_noan, stats2.words_noan, 
						scores.meanscore_all, scores.stdscore_all, ratios.meanratio_all, ratios.stdratio_all,
						scores.meanscore_noan, scores.stdscore_noan, ratios.meanratio_noan, ratios.stdratio_noan,
						domain, domainEurovocId, FREE_STR, creationModeDescription,
						projectId, projectURL, organization, organizationURL);
			} else {
				bilingualCorpusInfo = new BilingualCorpusInformation(FilenameUtils.getBaseName(outTMX.getAbsolutePath()), langs[0], langs[1], 
						alignmentList, alignmentList.size(), stats1.tus_noan,
						stats1.tokens_all, stats2.tokens_all, stats1.words_all, stats2.words_all, 
						stats1.tokens_noan, stats2.tokens_noan, stats1.words_noan, stats2.words_noan, 
						scores.meanscore_all, scores.stdscore_all, ratios.meanratio_all, ratios.stdratio_all,
						scores.meanscore_noan, scores.stdscore_noan, ratios.meanratio_noan, ratios.stdratio_noan,
						domain, domainEurovocId, UNKNOWN_STR, creationModeDescription,
						projectId, projectURL, organization, organizationURL);
			}
			if (oxslt) 
				outHTML =  new File(baseName.getAbsolutePath() + HTMLEXT);
			else
				outHTML =null;
			String[] languages = new String[2]; languages[0]=langs[0]; languages[1]=langs[1];
			TMXHandler.generateMergedTMX(outTMX, languages, bilingualCorpusInfo, outHTML);

			BilingualTmxMetashareDescriptor bilingualTmxMetashareDescriptor = new BilingualTmxMetashareDescriptor(bilingualCorpusInfo);
			File metadataFile = new File(baseName.getAbsolutePath()+ MetadataExt);
			logger.info("Generating metadata descriptor " + metadataFile);
			bilingualTmxMetashareDescriptor.setOutFile(metadataFile);
			if (iso6393) {
				bilingualTmxMetashareDescriptor.setMetadataLang("eng");
			} else {
				bilingualTmxMetashareDescriptor.setMetadataLang("en");
			}
			bilingualTmxMetashareDescriptor.run();
		}else{
			logger.info("No proper TUs found.");
		}
		return alignmentList;
	}


	/**
	 * gets the tus of a tmx 
	 * @param tmxFile
	 * @param thr
	 * @param minPerce01Align 
	 */
	public static List<SegPair>  getTUsFromTMX(File tmxFile, String lang1, String lang2) {
		List<SegPair> segpairs = new ArrayList<SegPair>();
		Tmx tmx;
		try {
			tmx = TmxMarshallerUnmarshaller.getInstance().unmarshal(new FileReader(tmxFile));
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
							annot  = prop.getContent().get(0);
					}
				}
				segpairs.add(new SegPair(StringUtils.join(TMXHandlerUtils.createSegmentList(tu, lang1), Constants.SPACE), 
						StringUtils.join(TMXHandlerUtils.createSegmentList(tu, lang2), Constants.SPACE),
						score, type, "",l1url, l2url, license, other,ratio,annot));
			}
			logger.debug("Examining " + tmxFile.getAbsolutePath() + Constants.SPACE + tus.size());
		} catch (FileNotFoundException e) {
			logger.warn("Problem in reading "+ tmxFile.getAbsolutePath());
			e.printStackTrace();
		}
		return segpairs;
	}


	private static List<ILSPAlignment> getSelectedSegs(List<SegPair> segpairs,	List<ILSPAlignment> alignmentList) {

		double ratio, score;
		for (SegPair segpair:segpairs){
			String info="";

			String license = segpair.license;
			if (cc && !license.contains(CREATIVE_COMMONS))
				continue;
			if (psi!=null){ //psi or CC TUs must be selected
				if (!license.contains(CREATIVE_COMMONS)){
					if (!license.equals(PSI_STR)){
						String w1 = checkPSI( segpair.l1url, psi);
						String w2 = checkPSI( segpair.l2url, psi);
						if (w1.equals(w2))
							license= w1;
						if (license.isEmpty())
							license = UNCLEAR_STR;
						if (license.equals(YES_STR))
							license = PSI_STR;
						if (license.equals(NO_STR))
							license = NOPSI_STR;	
					}
					if (!license.equals(PSI_STR) )
						continue;
				}
			}

			ratio = (double)segpair.ratio;
			if (ratio>maxTuLenRatio || ratio < minTuLenRatio){
				if (clean)
					continue;
				if (info.isEmpty())
					info = mes5+  minTuLenRatio +mes5a+ maxTuLenRatio;
				else
					info = info + " | "+mes5+  minTuLenRatio +mes5a+ maxTuLenRatio ;
			}

			score = (double)segpair.score;
			if (score< minScore){
				if (clean)
					continue;
				if (info.isEmpty())
					info = mes10+  minScore;
				else
					info = info + " | "+ mes10+  minScore; ;
			}

			String normS = ContentNormalizer.normtext(segpair.seg1);
			String normT = ContentNormalizer.normtext(segpair.seg2);
			if ( normS.isEmpty() || normT.isEmpty()){
				if (!keepem)
					continue;
				if (clean)
					continue;
				info =  mes1;
			}

			if (normS.equals(normT)){
				if (!keepiden)
					continue;
				if (clean)
					continue;
				if (info.isEmpty())
					info =  mes2;
				else
					info =  info + " | "+mes2;	
			}

			List<String> stokens = FCStringUtils.getTokens(normS);
			List<String> ttokens = FCStringUtils.getTokens(normT);
			Double[] stokenslen= FCStringUtils.getTokensLength(stokens);
			Double[] ttokenslen= FCStringUtils.getTokensLength(ttokens);
			if (Statistics.getMax(stokenslen)>max_word_length || Statistics.getMax(ttokenslen)>max_word_length){
				continue;
			}else{
				if (Statistics.getMedian(stokenslen)>median_word_length || Statistics.getMedian(ttokenslen)>median_word_length)
					continue;
			}

			if (FCStringUtils.countTokens(normS)<minTuvLen || FCStringUtils.countTokens(normT)<minTuvLen){
				if (clean)
					continue;
				if (info.isEmpty())	
					info = mes4+minTuvLen ;
				else
					info = info + " | "+mes4+minTuvLen ;
			}

			String num1=segpair.seg1.replaceAll("\\D+","");
			String num2=segpair.seg2.replaceAll("\\D+","");
			if (!num1.equals(num2)){
				if (keepsn)
					continue;
				if (clean)
					continue;
				if (info.isEmpty())
					info =  mes6;
				else
					info =  info + " | "+mes6;

			}
			String temp = normS+TAB+normT;
			if (segs.contains(temp)){
				if (!keepdup)
					continue;
				if (clean)
					continue;
				if (info.isEmpty())
					info =  mes7;
				else
					info =  info + " | "+mes7;
			}else
				segs.add(temp);

			ILSPAlignment alignment = new ILSPAlignment();
			alignment.addSourceSegment(segpair.seg1);
			alignment.addTargetSegment(segpair.seg2);
			alignment.setScore((float)segpair.score);
			alignment.setL1url(segpair.l1url);
			alignment.setL2url(segpair.l2url);
			alignment.setMethod(segpair.method);
			alignment.setLicense(segpair.license);
			alignment.setType(segpair.type);
			alignment.setLengthRatio(Double.toString(ratio));
			alignment.setScore((float)score);
			alignment.setInfo(info);
			alignmentList.add(alignment);
		}
		return alignmentList;
	}

	private static String checkPSI(String l1url, Map<String, String> psi) {
		String res="";
		if (StringUtils.isBlank(l1url))
			return res;
		try {
			URL url1 = new URL(l1url);
			String a = url1.getAuthority();
			if (psi.containsKey(a))
				res = psi.get(a);
		} catch (MalformedURLException e) {
			return res;
		}
		return res;
	}

}
