package gr.ilsp.fc.tmxhandler;

import gr.ilsp.fc.aligner.factory.ILSPAlignment;
import gr.ilsp.fc.corpora.BilingualCorpusInformation;
import gr.ilsp.fc.langdetect.LangDetectUtils;
import gr.ilsp.fc.langdetect.LangDetector;
import gr.ilsp.fc.readwrite.ReadResources;
import gr.ilsp.fc.utils.ContentNormalizer;
import gr.ilsp.fc.utils.FCStringUtils;
import gr.ilsp.fc.utils.ISOLangCodes;
import gr.ilsp.fc.utils.Statistics;
import gr.ilsp.fc.utils.ValidateUtils;
import gr.ilsp.fc.utils.sentencesplitters.MorphAdornerSentenceSplitter;
import gr.ilsp.nlp.commons.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
//import java.net.MalformedURLException;
//import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.loomchild.maligna.parser.TmxParseException;
import net.loomchild.maligna.util.bind.TmxMarshallerUnmarshaller;
import net.loomchild.maligna.util.bind.tmx.Prop;
import net.loomchild.maligna.util.bind.tmx.Seg;
import net.loomchild.maligna.util.bind.tmx.Tmx;
import net.loomchild.maligna.util.bind.tmx.Tu;
import net.loomchild.maligna.util.bind.tmx.Tuv;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class TMXHandlerUtils {
	private static final Logger LOGGER = Logger.getLogger(TMXHandlerUtils.class);

	private final static String YES = "yes";
	private final static String PSI = "psi";
	private final static String UNCLEAR = "unclear";
	private final static String NONPSI = "nonpsi";
	private static final String XML_EXTENSION = ".xml";
	private final static String eAddressNode ="eAddress"; 
	private final static String licenseNode = "license";
	private final static String TARGET = "target";
	private final static String SCORE = "score";
	private final static String INFO = "info";
	private final static String RATIO = "lengthRatio";
	private final static String L1URL = "l1-url";
	private final static String L2URL = "l2-url";
	private final static String SEGMENTTYPE = "segmentType";
	private static boolean doNotCountZeroToOneAlignments = true;
	static Matcher twitterHandleMatcher = Pattern.compile("(^|[^@\\w])@(\\w{1,15})\\b").matcher("");
	//private static final int length_THR = 6;

	private static LangDetector _langDetector;
	private static String defaultlangDetectorId="langdetect"; //"tika";//
	public static Set<String> langsTBFI = new HashSet<String>(Arrays.asList("bos", "hrv", "srp")); 

	public static void main(String[] args) {
		
		TMXHandlerUtils.checkurl("http://greece.greekreporter.com/2017/01/19/it-will-take-21-vears-until-unemplovment-in-greece-returns-to-normal-");		
		
		String[] _targetedLangs= new String[2];
		_targetedLangs[0]= "en"; _targetedLangs[1]= "es";
		_langDetector = LangDetectUtils.loadLangDetectors(_targetedLangs,defaultlangDetectorId);
		Set<String> segs = new HashSet<String>() ;
		/*String testtext = "Special Data Dissemination System (SDDS)";
		try {
			_langDetector.detectLangs1(testtext);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

		//List<SegPair> testsegpairs = TMXHandlerUtils.getTUsFromTMX(new File("C:/Users/vpapa/Downloads/archive (1)/EN-ES_Website_corpus/output_data_en-es.tmx"), false,
		//		_targetedLangs[0], _targetedLangs[1]);
		
		//File infile = new File("C:/Users/vpapa/ELRC/created_datasets/datasets_v2/pub_admin_v2_elrc_ell-eng.tmx");
		File infile = new File("C:/Users/vpapa/Downloads/archive (1)/EN-ES_Website_corpus/output_data_en-es.tmx");
		List<SegPair> testsegpairs = TMXHandlerUtils.getTUsFromTMX(infile, false,	_targetedLangs[0], _targetedLangs[1]);

		String idenlang1a="", idenlang2a="";
		int counter=0;
		System.out.println(infile.getName() +":\t"+ testsegpairs.size()+ " TUs");
		for (SegPair segpair:testsegpairs){
			//System.out.println();
			if (segpair.l1url.contains("twitter.") || segpair.l2url.contains("twitter.")){ //temp addition. it should be removed 
				System.out.println("TWITTER\t"+segpair.seg1+"\t\t"+segpair.seg2);
				counter++;
				continue;
			}
			//FIXME add constrains for length, or other "filters"
			String normS = ContentNormalizer.normtext(segpair.seg1);
			String normT = ContentNormalizer.normtext(segpair.seg2);
			if ( normS.isEmpty() || normT.isEmpty()){
				System.out.println("EMPTY\t"+segpair.seg1+"\t\t"+segpair.seg2);
				counter++;
				continue;
			}
			if (normS.equals(normT)){
				System.out.println("EQUAL\t"+segpair.seg1+"\t\t"+segpair.seg2);
				counter++;
				continue;
			}
			if (ValidateUtils.isValidEmailAddress(segpair.seg1) || ValidateUtils.isValidEmailAddress(segpair.seg2)){
				System.out.println("EMAIL\t"+segpair.seg1+"\t\t"+segpair.seg2);
				counter++;
				continue;
			}	
			if (ValidateUtils.isValidUrl(segpair.seg1) || ValidateUtils.isValidUrl(segpair.seg2)){
				System.out.println("URL\t"+segpair.seg1+"\t\t"+segpair.seg2);
				counter++;
				continue;
			}

			/*List<String> stokens = FCStringUtils.getTokens(normS);
			List<String> ttokens = FCStringUtils.getTokens(normT);
			Double[] stokenslen = FCStringUtils.getTokensLength(stokens);
			Double[] ttokenslen = FCStringUtils.getTokensLength(ttokens);*/

			if (FCStringUtils.countTokens(normS)<3 || FCStringUtils.countTokens(normT)<3){
				System.out.println("SHORT\t"+segpair.seg1+"\t\t"+segpair.seg2);
				counter++;
				continue;
			}
			List<String> stokens = FCStringUtils.getTokens(segpair.seg1);
			List<String> stokensn = FCStringUtils.getTokens(normS);
			
			if (((double)stokensn.size() / (double) stokens.size()) >3){
				System.out.println("TEXT_EXTRACTION\t"+segpair.seg1+"\t\t"+segpair.seg2);
				counter++;
				continue;
			}
			List<String> ttokens = FCStringUtils.getTokens(segpair.seg2);
			List<String> ttokensn = FCStringUtils.getTokens(normT);
			if (((double)ttokensn.size() / (double) ttokens.size()) >2){
				System.out.println("TEXT_EXTRACTION\t"+segpair.seg1+"\t\t"+segpair.seg2);
				counter++;
				continue;
			}

			Double[] stokenslen = FCStringUtils.getTokensLength(stokensn);
			Double[] ttokenslen = FCStringUtils.getTokensLength(ttokensn);
			if (Statistics.getMax(stokenslen)>30 || Statistics.getMax(ttokenslen)>30){
				System.out.println("LONG_WORD\t"+segpair.seg1+"\t\t"+segpair.seg2);
				counter++;
				continue;
			}else{
				if (Statistics.getMedian(stokenslen)>=25 || Statistics.getMedian(ttokenslen)>=25){
					System.out.println("LONG_WORDS\t"+segpair.seg1+"\t\t"+segpair.seg2);
					counter++;
					continue;
				}
			}
			

			float ratio = (float)segpair.seg1.length()/(float)segpair.seg2.length();
			if (ratio>1.6 || ratio < 0.6){
				System.out.println("RATIO\t"+segpair.seg1+"\t\t"+segpair.seg2);
				counter++;
				continue;
			}

			String num1=segpair.seg1.replaceAll("\\D+","");
			String num2=segpair.seg2.replaceAll("\\D+","");
			if (!num1.equals(num2)){
				System.out.println("DIF_NUM\t"+segpair.seg1+"\t\t"+segpair.seg2);
				counter++;
				continue;
			}

			idenlang1a=_targetedLangs[0];
			idenlang2a=_targetedLangs[1];
			if (segpair.seg1.length()>=120)
				idenlang1a = LangDetectUtils.updateLanguages(_langDetector.detect(segpair.seg1),false);
			if (segpair.seg2.length()>=120)
				idenlang2a = LangDetectUtils.updateLanguages(_langDetector.detect(segpair.seg2), false);
			if (!_targetedLangs[0].equals(idenlang1a) || !_targetedLangs[1].equals(idenlang2a)){
				System.out.println("--------");
				System.out.println("LANG\t"+segpair.seg1+"\t"+FCStringUtils.getTokens(segpair.seg1).size()+"\t"+segpair.seg1.length()+"\t"+_targetedLangs[0]+"\t"+idenlang1a);
				System.out.println("LANG\t"+segpair.seg2+"\t"+FCStringUtils.getTokens(segpair.seg2).size()+"\t"+segpair.seg2.length()+"\t"+_targetedLangs[1]+"\t"+idenlang2a);
				System.out.println("--------");
				counter++;
				continue;
			}
			String temp = normS+"\t"+normT;
			if (segs.contains(temp)){
				System.out.println("DUP\t"+segpair.seg1+"\t\t"+segpair.seg2);
				counter++;
				continue;
			}else{
				segs.add(temp);
			}


			/*try {
				idenlang1a = _langDetector.detectLangs1(t1);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/

			/*if (!_targetedLangs[0].equals(LangDetectUtils.updateLanguages(idenlang1a, false))){
				for (int ii=0;ii<4;ii++)
					t1 = " "+t1 + " "+t1;
				idenlang11 = _langDetector.detect(t1);
				if (!_targetedLangs[0].equals(LangDetectUtils.updateLanguages(idenlang11, false))){
					check1 = true;//System.out.println(idenlang1+"\t"+segpair.seg1);
					if (_targetedLangs[0].equals("en"))
						check =true;
				}
			}*/
			//String t2 = ContentNormalizer.normtext(segpair.seg2);


			//if (!_targetedLangs[1].equals(LangDetectUtils.updateLanguages(idenlang2a, false)))
			//	System.out.println(segpair.seg2+"\t"+FCStringUtils.getTokens(segpair.seg2).size()+"\t"+segpair.seg2.length()+"\t"+_targetedLangs[1]+"\t"+idenlang2a);


			//if (!_targetedLangs[0].equals(LangDetectUtils.updateLanguages(idenlang1a, false)) ||  !_targetedLangs[1].equals(LangDetectUtils.updateLanguages(idenlang2a, false))  ){


			/*try {
				idenlang2a = _langDetector.detectLangs1(t1);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			/*if (!_targetedLangs[1].equals(LangDetectUtils.updateLanguages(idenlang2a, false))){
				for (int ii=0;ii<4;ii++)
					t1 = " "+t1 + " "+t1;
				idenlang22 = _langDetector.detect(t1);
				if (!_targetedLangs[1].equals(LangDetectUtils.updateLanguages(idenlang22, false))){
					check2 = true;//System.out.println(idenlang1+"\t"+segpair.seg1);
					if (_targetedLangs[1].equals("en"))
						check =true;
				}
			}*/
			/*idenlang1 = _langDetector.detect(segpair.seg1);
			if (langsTBFI.contains(idenlang1)) 
				idenlang1 = _langDetector.detect(segpair.seg1, idenlang1);
			if (!_targetedLangs[0].equals(LangDetectUtils.updateLanguages(idenlang1, false)))
				check1=true;
			idenlang2 = _langDetector.detect(segpair.seg2);
			if (langsTBFI.contains(idenlang2)) 
				idenlang2 = _langDetector.detect(segpair.seg2, idenlang2);
			if (!_targetedLangs[1].equals(LangDetectUtils.updateLanguages(idenlang2, false)))
				check2=true;*/
			//if ((check1 && check2) || check ){
			//	System.out.println(idenlang1a+"\t"+idenlang11+"\t"+segpair.seg1+"\t"+idenlang2a+"\t"+idenlang22+"\t"+segpair.seg2);
			//}
		}
		System.out.println(counter);
		System.exit(0);
		File inTMX = new File(args[0]);
		File psiInfo = new File(args[1]);
		String[] languages = new String[2];
		languages[0] = args[2];
		languages[1] = args[3];

		BilingualCorpusInformation bilingualCorpusInfo = new BilingualCorpusInformation();

		List<SegPair> segpairs = udpateTMX_psi(inTMX, psiInfo, languages[0], languages[1], false);

		File outTMX = new File(FilenameUtils.concat(inTMX.getParent(), FilenameUtils.removeExtension(inTMX.getName())+"_psi.TMX"));

		TMXHandler.generateMergedTMX(outTMX, languages, bilingualCorpusInfo, null);
	}

	/**
	 * gets the tus of a tmx if the absolute and relative numbers of 0:1 alignments are less than thr and minPerce01Align respectively
	 * @param tmxFile
	 * @param thr
	 * @param minPerce01Align 
	 * @param sites 
	 */
	public static List<SegPair>  getTUsFromTMX(File tmxFile, int thr, double minPerce01Align, String lang1, String lang2, boolean lic, List<String> sites) {
		List<SegPair> segpairs = new ArrayList<SegPair>();
		Tmx tmx;
		try {
			tmx = TmxMarshallerUnmarshaller.getInstance().unmarshal(new FileReader(tmxFile));
			int alignments = 0;
			int sourceSegments = 0;
			int targetSegments = 0;
			int zeroToOneAlignments = 0;
			//double meanAlignmentScore = 0.0;
			TmxInfo tmxinfo = TMXHandlerUtils.getInfo(tmxFile,sites);
			if (tmxinfo==null)
				return segpairs;
			if (lic && tmxinfo.license.isEmpty())
				return segpairs;
			List<Tu> tus = tmx.getBody().getTu();
			for (Tu tu: tus) {
				alignments = alignments+1;
				List<Object> tuProps = tu.getNoteOrProp();
				String type="", annot = "";
				double score = 0, ratio=0;
				for (Object obProp : tuProps) {
					Prop prop = (Prop) obProp;
					if (prop.getType().equals(SCORE)) 
						score = Double.parseDouble(prop.getContent().get(0));
					else{
						if (prop.getType().equals(RATIO))
							ratio = Double.parseDouble(prop.getContent().get(0));
						else{
							if (prop.getType().equals(INFO))
								annot = prop.getContent().get(0);
							else{
								if (prop.getType().equals(SEGMENTTYPE)) {
									type = prop.getContent().get(0);
									String[] segs = StringUtils.split(prop.getContent().get(0), ":");
									sourceSegments = sourceSegments + Integer.parseInt(segs[0]);
									targetSegments = targetSegments + Integer.parseInt(segs[1]);
									if (doNotCountZeroToOneAlignments && (Integer.parseInt(segs[0])==0 ||Integer.parseInt(segs[1])==0 )) {
										alignments--;
										zeroToOneAlignments++;
									}
								}
							}
						}
					}
				}
				segpairs.add(new SegPair(StringUtils.join(createSegmentList(tu, lang1), Constants.SPACE), 
						StringUtils.join(createSegmentList(tu, lang2), Constants.SPACE),
						score, type, tmxinfo.method, tmxinfo.l1url, tmxinfo.l2url, tmxinfo.license, tmxinfo.other, ratio, annot));
			}
			LOGGER.debug("Examining " + tmxFile.getAbsolutePath() + Constants.SPACE + tus.size());
			double percent = (double)zeroToOneAlignments / (double)tus.size();
			if (percent>minPerce01Align){
				return null;
			}
			/*if (zeroToOneAlignments>thr || percent>percent_thr){
				return null;
			}*/
		} catch (FileNotFoundException e) {
			LOGGER.warn("Problem in reading "+ tmxFile.getAbsolutePath());
			e.printStackTrace();
		}
		return segpairs;
	}

	/**
	 * selects (all or not annotated) TUs of a tmxfile
	 * @param tmxFile
	 * @param hasannot
	 * @param lang1
	 * @param lang2
	 * @return
	 */
	public static List<SegPair>  getTUsFromTMX(File tmxFile, boolean hasannot, String lang1, String lang2) {
		List<SegPair> segpairs = new ArrayList<SegPair>();
		Tmx tmx;
		try {
			tmx = TmxMarshallerUnmarshaller.getInstance().unmarshal(new FileReader(tmxFile));
			List<Tu> tus = tmx.getBody().getTu();
			for (Tu tu: tus) {
				List<Object> tuProps = tu.getNoteOrProp();
				boolean found=false;
				for (Object obProp : tuProps) {
					Prop prop = (Prop) obProp;
					if (prop.getType().equals(INFO)) {
						if (prop.getContent().isEmpty())
							found=true;
						else
							System.out.println(prop.getContent().get(0));
					}
				}
				if (found){
					segpairs.add(new SegPair(StringUtils.join(createSegmentList(tu, lang1), Constants.SPACE), 
							StringUtils.join(createSegmentList(tu, lang2), Constants.SPACE),
							0, "", "", "", "", "", "",0,""));
				}
			}
			LOGGER.debug("Examining " + tmxFile.getAbsolutePath() + Constants.SPACE + tus.size());
		} catch (FileNotFoundException e) {
			LOGGER.warn("Problem in reading "+ tmxFile.getAbsolutePath());
			e.printStackTrace();
		}
		return segpairs;
	}


	/**
	 * selects TUs of a tmxfile depending on their annotation
	 * @param tmxFile
	 * @param hasannot
	 * @param lang1
	 * @param lang2
	 * @return
	 */
	public static List<SegPair> getTUsFromTMX(File tmxfile, List<String> info, String lang1, String lang2) {
		List<SegPair> segpairs = new ArrayList<SegPair>();
		Tmx tmx;
		try {
			tmx = TmxMarshallerUnmarshaller.getInstance().unmarshal(new FileReader(tmxfile));
			List<Tu> tus = tmx.getBody().getTu();
			boolean found = false;
			for (Tu tu: tus) {
				List<String> segmentList1 = new ArrayList<String>();
				List<String> segmentList2 = new ArrayList<String>();
				List<Object> tuProps = tu.getNoteOrProp();
				String type="", l1url="", l2url="";
				double score = 0;
				found=true;
				for (Object obProp : tuProps) {
					Prop prop = (Prop) obProp;
					if (prop.getType().equals(SCORE)) 
						score = Double.parseDouble(prop.getContent().get(0));
					if (prop.getType().equals(L1URL)) 
						l1url = prop.getContent().get(0);
					if (prop.getType().equals(L2URL)) 
						l2url = prop.getContent().get(0);
					if (prop.getType().equals(INFO)){
						if (prop.getContent().isEmpty())
							found = true;
						else{
							if (info.isEmpty())
								found=true;
							else{
								String annot = prop.getContent().get(0);
								for (String infoitem:info){
									if (annot.contains(infoitem)){
										found = false;
										break;
									}
								}
							}
						}
					}
				}
				if (!found)
					continue;
				for (Tuv tuv : tu.getTuv()) {
					if (lang1.equals(tuv.getLang())) {
						String segment = getSegment(tuv.getSeg());
						segmentList1.add(segment);
					}
					if (lang2.equals(tuv.getLang())) {
						String segment = getSegment(tuv.getSeg());
						segmentList2.add(segment);
					}
				}

				segpairs.add(new SegPair(StringUtils.join(segmentList1, Constants.SPACE), 
						StringUtils.join(segmentList2, Constants.SPACE),
						score, type, "", l1url, l2url,  "", "",0,""));
			}
			LOGGER.debug("Examining " + tmxfile.getAbsolutePath() + Constants.SPACE + tus.size());

		} catch (FileNotFoundException e) {
			LOGGER.warn("Problem in reading "+ tmxfile.getAbsolutePath());
			e.printStackTrace();
		}
		return segpairs;
	}


	public static TmxInfo getInfo(File tmxFile, List<String> sites) {
		String method = tmxFile.getName().substring(tmxFile.getName().lastIndexOf(Constants.UNDERSCORE)+1, tmxFile.getName().lastIndexOf(Constants.DOT));
		File f1 = new File(FilenameUtils.concat(tmxFile.getParent(), StringUtils.split(tmxFile.getName(), Constants.UNDERSCORE)[0])+XML_EXTENSION);
		File f2 = new File(FilenameUtils.concat(tmxFile.getParent(), StringUtils.split(tmxFile.getName(), Constants.UNDERSCORE)[1])+XML_EXTENSION);
		String  license=""; //site="",
		String webpage1=ReadResources.extractNodefromXML(f1.getAbsolutePath(), eAddressNode, false);
		if (!inSites(webpage1,sites))
			return null;
		String webpage2=ReadResources.extractNodefromXML(f2.getAbsolutePath(), eAddressNode, false);
		if (!inSites(webpage2,sites))
			return null;
		String license1 = ReadResources.extractAttrfromXML(f1.getAbsolutePath(), licenseNode, TARGET,true, true);
		String license2 = ReadResources.extractAttrfromXML(f2.getAbsolutePath(), licenseNode, TARGET,true, true);
		if (!license1.isEmpty())
			license = license1;
		else{
			if (!license2.isEmpty())
				license = license2;
			else
				license="";
		}
		return new TmxInfo(method, webpage1, webpage2, license, "");
	}

	public static List<String> getSites(List<ILSPAlignment> alignmentList,	boolean noannot) {
		List<String> sites = new ArrayList<String>();
		for (ILSPAlignment align:alignmentList){
			String temphost1="", temphost2="";
			try {
				temphost1 = new URL(align.getL1url()).getHost();
				temphost2 = new URL(align.getL2url()).getHost();
			} catch (MalformedURLException e) {
				continue;
				//LOGGER.debug("no protocol of acquired files " +temphost1 + " or " + temphost2);			//e.printStackTrace();
			}
			if (noannot && !align.getInfo().isEmpty())
				continue;
			if (!sites.contains(temphost1))
				sites.add(temphost1);
			if (!sites.contains(temphost2))
				sites.add(temphost2);
		}
		return sites;
	}

	public static List<String> getSitesFromSegPairs(List<SegPair> segpairs, boolean noannot) {
		List<String> sites = new ArrayList<String>();
		for (SegPair sg:segpairs){
			String temphost1="", temphost2="";
			try {
				temphost1= new URL(sg.l1url).getHost();
				temphost2= new URL(sg.l2url).getHost();
			} catch (MalformedURLException e) {
				LOGGER.warn("no protocol of acquired files " +temphost1 + " or " + temphost2);			//e.printStackTrace();
			}
			if (noannot){
				if (sg.other.isEmpty()){
					if (!sites.contains(temphost1))			
						sites.add(temphost1);
					if (!sites.contains(temphost2))			
						sites.add(temphost2);
				}
			}else{
				if (!sites.contains(temphost1))			
					sites.add(temphost1);
				if (!sites.contains(temphost2))			
					sites.add(temphost2);
			}
		}
		return sites;
	}

	public static List<String> getSampleSegs(List<SegPair> segs, int samplesize) {
		List<String> res = new ArrayList<String>();
		samplesize = Math.min(samplesize, segs.size());
		Set<SegPair> selpairs = Statistics.distinctRandomSegPairs(segs, samplesize);
		for (SegPair sg:selpairs){
			res.add(sg.seg1+Constants.TAB+ sg.seg2);
		}
		return res;
	}

	public static List<String> getSegsAndProps(List<SegPair> segs, String lang1,	String lang2) {
		List<String> res = new ArrayList<String>();
		int i = 1;
		for (SegPair seg : segs){
			res.add(StringUtils.join(new String[] {String.valueOf(i), seg.seg1, seg.seg2, String.valueOf(seg.score), String.valueOf(seg.ratio), seg.annot }, Constants.TAB));
			i++;
		}
		return res;
	}

	public static List<List<String>>  getTokedSegs(List<String> l1segs, List<String> l2segs, String lang1, String lang2) throws IOException {
		List<List<String>> res = new ArrayList<List<String>>();
		MorphAdornerSentenceSplitter splitter  = new MorphAdornerSentenceSplitter(lang1);
		List<String> l1toksegs = getTokenizedSentences(l1segs, splitter);
		res.add(l1toksegs);
		splitter  = new MorphAdornerSentenceSplitter(lang2);
		List<String> l2toksegs = getTokenizedSentences(l2segs, splitter);
		res.add(l2toksegs);
		return res;
	}

	private static List<String> getTokenizedSentences(List<String> segs, MorphAdornerSentenceSplitter splitter) {
		List<String> toksegs = new ArrayList<String>();
		for (String seg : segs) {
			try {
				List<List<String>> splitterTokSents = splitter.getTokens(seg);
				for (List<String> tokseg : splitterTokSents) {
					String temp="";
					for (String tok : tokseg) {
						temp = temp+ Constants.SPACE+tok;
					}
					toksegs.add(temp.trim());
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return toksegs;
	}

	private static boolean inSites(String webpage1, List<String> sites) {
		if (!sites.isEmpty()){
			try {
				if (!sites.contains(new URL(webpage1).getHost()))
					return false;
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return true;
	}

	public static List<String> createSegmentList(Tu tu, String languageCode) {
		List<String> segmentList = new ArrayList<String>();
		String languageCode1 = "";
		if (languageCode.length()==2)
			languageCode1 = ISOLangCodes.get3LetterCode(languageCode);
		else
			languageCode1 = ISOLangCodes.get2LetterCode(languageCode);
		for (Tuv tuv : tu.getTuv()) {
			if (languageCode.equals(tuv.getLang()) || languageCode1.equals(tuv.getLang())) {
				String segment = getSegment(tuv.getSeg());
				segmentList.add(segment);
			}
		}
		if (segmentList.size() > 1) {
			throw new TmxParseException(languageCode + " variant count greater than 1");
		}
		return segmentList;
	}

	/**
	 * selects samplesize segment pairs randomly, merges the segments of each pair (tab separated) and put them in a list 
	 * @param inlist
	 * @param samplesize
	 * @return
	 */
	public static List<String> getSample(List<SegPair> inlist, int samplesize){
		List<String> samplelist = new ArrayList<String>();
		samplesize = Math.min(samplesize, inlist.size());
		Set<SegPair> segpairs = Statistics.distinctRandomSegPairs(inlist, samplesize);
		for (SegPair sg:segpairs){
			samplelist.add(sg.seg1+Constants.TAB+ sg.seg2);
		}
		return samplelist;
	}


	private static String getSegment(Seg seg) {
		StringBuilder builder = new StringBuilder();
		for (Object object : seg.getContent()) {
			builder.append(object.toString());
		}
		return builder.toString();
	}


	/**
	 * Enriches a TMX file (the license property) based on psi properties
	 * @param tmxFile : the TMX file to be enriched
	 * @param psiInfo : a text file containing a list of webdomains and their psi properties ("yes", "no", "unclear")
	 * a webdomain per line 
	 * @param lang1
	 * @param lang2
	 * @param lic
	 * @return
	 */
	public static List<SegPair>  udpateTMX_psi(File tmxFile, File psiInfo, String lang1, String lang2, boolean lic){
		Map<String, String> psi = new HashMap<String, String>();
		try {
			List<String> temp = FileUtils.readLines(psiInfo);
			for (String line:temp){
				String[] lineparts = line.trim().split(Constants.SPACE);
				psi.put(lineparts[0], lineparts[1].toLowerCase());
			}
		} catch (IOException e1) {
			LOGGER.error("Problem in reading "+ psiInfo.getAbsolutePath());
			e1.printStackTrace();
		}
		List<SegPair> segpairs = getTUsFromTMX(tmxFile, 1000000000, 1000, lang1, lang2, false, null);
		String  res1 = "", res2 = "", res = "";
		for (SegPair segpair:segpairs){
			if (segpair.license.isEmpty()){
				try {
					res1 = new URL(segpair.l1url).getHost();
					res2 = new URL(segpair.l2url).getHost();
				} catch (MalformedURLException e) {
					LOGGER.warn("not valid URL");
					e.printStackTrace();
				}
				if (psi.containsKey(res1) && psi.containsKey(res2)){
					res =  psi.get(res1);
					if (res.equals(psi.get(res2))){
						if (res.equals(YES))
							segpair.license=PSI;
						else
							segpair.license=NONPSI;
					}else
						segpair.license=UNCLEAR;
				}else
					segpair.license=UNCLEAR;
			}
		}
		return segpairs;
	}



	/**
	 * holds segs in lang1 and in lang2, and properties of a pair of segments 
	 * @author 
	 *
	 */
	public static class SegPair {
		public String seg1;
		public double score;
		public double ratio;
		public String annot;
		public String seg2;
		public String type;
		public String method;
		public String site;
		public String l1url;
		public String l2url;
		public String license;
		public String other;

		public SegPair(String seg1, String seg2, double score, String type, String method, String l1url, String l2url, String license, String other, double ratio, String annot) {
			this.seg1 = seg1;
			this.seg2 = seg2;
			this.type = type;
			this.score = score;
			this.ratio = ratio;
			this.method = method;
			this.l1url = l1url;
			this.l2url = l2url;
			this.license = license;
			this.annot = annot;
			this.other = other;

		}
	}

	/**
	 * holds site, license, and method of pair detection 
	 * @author 
	 *
	 */
	public static class TmxInfo {
		public String method;
		public String l1url;
		public String l2url;
		public String license;
		public String other;

		public TmxInfo(String method, String l1url, String l2url, String license, String other) {
			this.method = method;
			this.l1url = l1url;
			this.l2url = l2url;
			this.license = license;
			this.other = other;
		}
	}

	/**
	 * counts tokens and unique words of i-th TUV of each TU in the alignmentList.
	 * If clean is true, it also counts tokens and words of TUs with no annotation  
	 * @param alignmentList
	 * @param i
	 * @param noannot
	 * @return
	 */
	public static TUsNumStats numstats(List<ILSPAlignment> alignmentList, int i, boolean noannot) {
		TUsNumStats stats = new TUsNumStats(0,0,0,0,0,0);
		int totallen=0, cleanlen=0, cleanpairslen=0;
		Set<String> allwords = new HashSet<String>();
		Set<String> cleanwords = new HashSet<String>();
		for (ILSPAlignment align:alignmentList){
			List<String> temp=new ArrayList<String>();
			if (i==1)
				temp =align.getSourceSegmentList();
			if (i==2)
				temp =align.getTargetSegmentList();
			for (String str:temp){
				List<String> toks = FCStringUtils.getWords(str); //FIXME for CHINESE
				totallen =totallen +toks.size();
				for (String tok:toks){
					if (!allwords.contains(tok))
						allwords.add(tok);
				}
				if (noannot){
					if (!align.getInfo().isEmpty())
						continue;
					cleanpairslen++;
					cleanlen =cleanlen +toks.size();
					for (String tok:toks){
						if (!cleanwords.contains(tok))
							cleanwords.add(tok);
					}
				}
			}
		}
		stats.tokens_all = totallen ; stats.words_all = allwords.size(); stats.tus_all = alignmentList.size();
		stats.tokens_noan = cleanlen ; stats.words_noan = cleanwords.size(); stats.tus_noan = cleanpairslen;
		return stats;
	}

	/**
	 * @param tmxFile
	 * @return
	 * @throws FileNotFoundException
	 */
	public static List<Tu> getTUs(File tmxFile) throws FileNotFoundException {
		return TmxMarshallerUnmarshaller.getInstance().unmarshal(new InputStreamReader(new FileInputStream(tmxFile))).getBody().getTu();
	}


	/**
	 * calculates mean and std values of aligner's score for all and notannotated TUs
	 * @author 
	 *
	 */
	public static TUsScoreStats scorestats(List<ILSPAlignment> alignmentList, boolean noannot) {
		TUsScoreStats stats = new TUsScoreStats(0,0,0,0);
		List<Double> scores = new ArrayList<Double>();
		List<Double> cleanscores = new ArrayList<Double>(); 
		for (ILSPAlignment align:alignmentList){
			scores.add((double)align.getScore());
			if (noannot){
				if (align.getInfo().isEmpty())
					cleanscores.add((double)align.getScore());
			}
		}
		Double[] scoresA = new  Double[scores.size()];
		for (int ii=0;ii<scoresA.length;ii++)
			scoresA[ii] = scores.get(ii);
		Double[] cleanscoresA = new  Double[cleanscores.size()];
		for (int ii=0;ii<cleanscoresA.length;ii++)
			cleanscoresA[ii] = cleanscores.get(ii);
		stats.meanscore_all = Statistics.getMean(scoresA);
		stats.stdscore_all = Statistics.getStdDev(scoresA);
		stats.meanscore_noan = Statistics.getMean(cleanscoresA);
		stats.stdscore_noan = Statistics.getStdDev(cleanscoresA);
		return stats;
	}

	/**
	 * gets distinct-Random- ILSPAlignments from the alignmentList and "writes them" in a textfile (properties are tab-separated)
	 * @param alignmentList
	 * @param samplesize
	 */
	public static void generateSample(List<ILSPAlignment> alignmentList, int samplesize, File samplefile) {
		
		List<ILSPAlignment> tempList = new ArrayList<ILSPAlignment>();
		for (ILSPAlignment t:alignmentList)
			tempList.add(t);
		
		samplesize = Math.min(samplesize, tempList.size());
		Set<ILSPAlignment> selpairs = Statistics.distinctRandomILSPAlignments(tempList, samplesize);
		List<String> sample= new ArrayList<String>();
		//sample.add("SEG_L1\tSEG_L2\tLenRatio\tAlignScore\tInfo\tMethod\tType\tURL_L1\tURL_L2\tLicense");

		for (ILSPAlignment a:selpairs){
			sample.add(a.getSourceSegmentList().get(0)+Constants.TAB+a.getTargetSegmentList().get(0)+Constants.TAB
					+a.getLengthRatio()+Constants.TAB+a.getScore()+Constants.TAB+a.getInfo()+Constants.TAB
					+a.getMethod()+Constants.TAB+a.getType()+Constants.TAB+a.getL1url()+Constants.TAB+a.getL2url()+Constants.TAB+a.getLicense());
		}
		try {
			FileUtils.writeLines(samplefile, sample);
		} catch (IOException e) {
			LOGGER.error("problem in writing the sample file "+ samplefile.getAbsolutePath());
			e.printStackTrace();
		}
	}
	
	public static boolean checkurl(String seg) {
		List<String> tokens = FCStringUtils.getTokens(seg);
		double len= (double)seg.length();
		double len1 = 0;
		for (String token:tokens){
			if (ValidateUtils.isValidUrl(token))
				len1 = len1+(double)token.length();
		}
		if ((len1/len)> 0.5)
			return true;
		return false;
	}

	public static boolean checkemail(String seg) {
		List<String> tokens = FCStringUtils.getTokens(seg);
		double len= (double)seg.length();
		double len1 = 0;
		for (String token:tokens){
			if (ValidateUtils.isValidEmailAddress(token))
				len1 = len1+(double)token.length();
		}
		if ((len1/len)> 0.5)
			return true;
		return false;
	}
	
	
	/**
	 * calculates mean and std values of length ratio for all and notannotated TUs
	 * @author 
	 *
	 */
	public static TUsRatioStats ratiostats(List<ILSPAlignment> alignmentList, boolean noannot) {
		TUsRatioStats stats = new TUsRatioStats(0,0,0,0);
		List<Double> ratios = new ArrayList<Double>(); 
		List<Double> cleanratios = new ArrayList<Double>();
		for (ILSPAlignment align:alignmentList){
			ratios.add(Double.parseDouble(align.getLengthRatio()));
			if (noannot){
				if (align.getInfo().isEmpty())
					cleanratios.add(Double.parseDouble(align.getLengthRatio()));
			}
		}
		Double[] ratiosA = new  Double[ratios.size()];
		for (int ii=0;ii<ratiosA.length;ii++)
			ratiosA[ii] = ratios.get(ii);
		Double[] cleanratiosA = new  Double[cleanratios.size()];
		for (int ii=0;ii<cleanratiosA.length;ii++)
			cleanratiosA[ii] = cleanratios.get(ii);
		stats.meanratio_all = Statistics.getMean(ratiosA);
		stats.stdratio_all = Statistics.getStdDev(ratiosA);
		stats.meanratio_noan = Statistics.getMean(cleanratiosA);
		stats.stdratio_noan = Statistics.getStdDev(cleanratiosA);
		return stats; 
	}


	/**
	 * holds mean and std values of length ratio for all and notannotated TUs
	 * @author 
	 *
	 */
	public static class TUsRatioStats {
		public double meanratio_all, stdratio_all;
		public double meanratio_noan, stdratio_noan;

		public TUsRatioStats(double meanratio_all, double stdratio_all, double meanratio_noan, double stdratio_noan) {
			this.meanratio_all = meanratio_all;
			this.stdratio_all = stdratio_all;
			this.meanratio_noan = meanratio_noan;
			this.stdratio_noan = stdratio_noan;
		}
	}

	/**
	 * holds mean and std values of aligner's score for all and notannotated TUs
	 * @author 
	 *
	 */
	public static class TUsScoreStats {
		public double meanscore_all, stdscore_all;
		public double meanscore_noan, stdscore_noan;

		public TUsScoreStats(double meanscore_all, double stdscore_all,	double meanscore_noan, double stdscore_noan) {
			this.meanscore_all = meanscore_all;
			this.stdscore_all = stdscore_all;
			this.meanscore_noan = meanscore_noan;
			this.stdscore_noan = stdscore_noan;
		}
	}


	/**
	 * holds number of TUs/nonannot TUs, number of words and lexical types for TUs/notannot TUs
	 * @author 
	 *
	 */
	public static class TUsNumStats {
		public int tokens_all, words_all, tus_all;
		public int tokens_noan, words_noan, tus_noan;

		public TUsNumStats(int tokens_all, int words_all, int tus_all, int tokens_noan, int words_noan, int tus_noan) {
			this.tokens_all = tokens_all;
			this.words_all = words_all;
			this.tus_all = tus_all;
			this.tokens_noan = tokens_noan;
			this.words_noan = words_noan;
			this.tus_noan = tus_noan;
		}
	}

	/*
	 * returns a table that holds the segments of "source" and "target" languages
	 */
	public static List<String> getLangPart(List<ILSPAlignment> alignmentList, boolean source) {
		List<String> res =new ArrayList<String>();
		if (source){
			for (ILSPAlignment al:alignmentList)
				res.add(al.getSourceSegmentList().get(0));
		}else{
			for (ILSPAlignment al:alignmentList)
				res.add(al.getTargetSegmentList().get(0));
		}
		return res;
	}

	public static void splitIntolangFiles(List<ILSPAlignment> alignmentList,String[] languages, File baseName) {
		List<String> res = getLangPart(alignmentList, true);
		File filelang1 = new File(baseName.getAbsolutePath() + Constants.DOT+ ISOLangCodes.get2LetterCode(languages[0]));
		try {
			FileUtils.writeLines(filelang1, res);
		} catch (IOException e) {
			LOGGER.warn("problem in writing "+ filelang1);
		}
		 res = getLangPart(alignmentList, false);
		File filelang2 = new File(baseName.getAbsolutePath() + Constants.DOT+ ISOLangCodes.get2LetterCode(languages[1]));
		try {
			FileUtils.writeLines(filelang2, res);
		} catch (IOException e) {
			LOGGER.warn("problem in writing "+ filelang2);
		}
	}



}
