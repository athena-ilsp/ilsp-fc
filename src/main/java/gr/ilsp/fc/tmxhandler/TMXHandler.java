package gr.ilsp.fc.tmxhandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
//import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import gr.ilsp.fc.tmxhandler.TMXHandlerUtils;
//import gr.ilsp.fc.aligner.factory.BilingualScoredTmxFormatter;
import gr.ilsp.fc.aligner.factory.BilingualScoredTmxFormatterILSP;
import gr.ilsp.fc.aligner.factory.ILSPAlignment;
import gr.ilsp.fc.corpora.BilingualCorpusInformation;
import gr.ilsp.fc.corpora.BilingualTmxMetashareDescriptor;
import gr.ilsp.fc.exporter.XSLTransformer;
import gr.ilsp.fc.main.Crawl;
import gr.ilsp.fc.main.ReadResources;
import gr.ilsp.fc.tmxhandler.TMXHandlerUtils.SegPair;
import gr.ilsp.fc.utils.ContentNormalizer;
import gr.ilsp.fc.utils.Eurovoc;
import gr.ilsp.fc.utils.FCStringUtils;
import gr.ilsp.fc.utils.FcFileUtils;
import gr.ilsp.fc.utils.ISOLangCodes;
import gr.ilsp.fc.utils.Statistics;
import gr.ilsp.fc.utils.ValidateUtils;

public class TMXHandler {
	private static final Logger LOGGER = Logger.getLogger(TMXHandler.class);
	private static TMXHandlerOptions options = null;
	private static File inputFile = null;
	private static File baseName = null;
	private static File outTMX = null;
	private static File outHTML = null;
	private static File o1 = null;
	private static File o2 = null;
	private static final String DEFAULT_BI_CONFIG_FILE = "FBC_config.xml";
	private static CompositeConfiguration config;
	private static String[] languages;
	private static int[] thres = { 10,10,10,10,10,10,10};
	private static boolean oxslt=false;
	private static boolean iso6393=false;
	private static boolean cc=false;
	private static boolean keepem = false;
	private static boolean keepiden = false;
	private static boolean keepdup = false;
	private static boolean clean = false;
	//private static boolean metadata=true;
	private static boolean keepsn=false;
	private static int minTuvLen=0;
	private static double minPerce01Align=1;
	private static double minTuLenRatio = 0;
	private static double maxTuLenRatio = 100;
	private static final double median_word_length=18;
	private static final double max_word_length=25;
	private static String doctypes;// = "aupidhml";
	private static List<String> segtypes;
	private static Set<String> segs = new HashSet<String>() ;
	static Matcher twitterHandleMatcher = Pattern.compile("(^|[^@\\w])@(\\w{1,15})\\b").matcher("");
	private static final String UNDERSCORE_STR = "_";
	private static final String SEMICOLON_STR=";";
	private static final String HYPHEN_STR="-";
	//private final static String PUNCT = ".";
	//private final static String SEP = "-";
	//private final static String UNDERSCORE = "_";
	private final static String UNKNOWN_STR ="unknown";
	private static final String HTML =".html";
	private static final String TMXEXT = ".tmx";
	private final static String MetadataExt = ".md.xml";
	private final static String domainNode = "domain";
	private final static String FREE_STR="free";
	private final static String TAB = "\t";
	private final static String UTF_8 = "UTF-8";
	//private final static String XSL_TMX2HTML = "http://nlp.ilsp.gr/xslt/ilsp-fc/tmx2html.xsl";
	private final static String XSL_TMX2HTML ="http://nlp.ilsp.gr/xslt/ilsp-fc/tmx2html-no-segtype.xsl";

	private final static String mes1 = "non-letters";
	private final static String mes2 = "equal TUVs";
	//private final static String mes3 = "very long token, longer than ";
	//private final static String mes3a = "very long tokens, median length is longer than ";
	private final static String mes4 = "very short segments, shorter than ";
	private final static String mes5 = "charlength ratio of TUVs is lower than ";
	private final static String mes5a = " or higher than ";
	private final static String mes6 = "different numbers in TUVs";
	private final static String mes7 = "duplicate";
	private final static String mes8 = "e-mail address";

	private static int totalcounter=0;
	//private static int distthr=5; //FIXME add as parameter

	public static CompositeConfiguration getConfig() {
		return config;
	}

	public void setConfig(CompositeConfiguration config) {
		TMXHandler.config = config;
	}

	private String alignerStr = "Maligna";

	private String creationDescription = "";
	private String filter6 = " Alignments in which different digits appear in each TUV were kept and annotated.";
	private String filter7 = " Alignments with identical TUVs (after normalization) were removed.";
	private String filter8 = " Alignments with only non-letters in at least one of their TUVs were removed";
	private String filter9 = " Duplicate alignments were discarded.";


	public static void main(String[] args) {
		TMXHandler ha = new TMXHandler();
		options = new TMXHandlerOptions();
		options.parseOptions(args);
		ha.setTargetDir(options.getTargetDir());
		ha.setConfig(getConfig( options.getConfig()));
		ha.setDocTypes(options.getDocTypes());
		ha.setThres(options.getThres());
		ha.setSegTypes(options.getSegTypes());
		ha.setApplyOfflineXSLT(options.getXSLTransform());
		ha.setLanguage(options.getLanguage());
		ha.useISO6393(options.useISO6393());
		ha.setMinTuvLen(options.getMinTuvLen());
		ha.setMinPerce01Align(options.getMinPerce01Align());
		ha.setMinTuLenRatio(options.getMinTuLenRatio());
		ha.setMaxTuLenRatio(options.getMaxTuLenRatio());
		ha.KeepTuSameNum(options.keepTuSameNum());
		ha.setCC(options.getCC());
		ha.setKeepEmpty(options.getKeepEmpty());
		ha.setKeepIdentical(options.getKeepIdentical());
		ha.setKeepDuplicates(options.getKeepDuplicates());
		ha.setClean(options.getClean());
		ha.setO1(options.getO1());
		ha.setO2(options.getO2());

		String[] languages = options.getLanguage().split(SEMICOLON_STR);
		List<String> lang_pairs = new ArrayList<String>();
		if (languages.length>1){
			for (int ii=0;ii<languages.length-1;ii++){
				for (int jj=ii+1;jj<languages.length;jj++){
					lang_pairs.add(languages[ii]+SEMICOLON_STR+languages[jj]);
				}
			}
		}
		for (String lang_pair:lang_pairs){
			ha.setLanguage(lang_pair);
			String[] temp_langs = lang_pair.split(SEMICOLON_STR);
			String lang = UNDERSCORE_STR+temp_langs[0]+HYPHEN_STR+temp_langs[1];
			ha.setBaseName(new File(options.getBaseName()+lang));
			ha.mergeTMXs();
		}
	}

	/**
	 * Gets selected TMXs from a directory and adds selected segments of these TMXs in a new TMX file.
	 * The selected TMXs should be extracted from document pairs which have been identified by the methods defined in DocTypes.
	 * The selected TMXs should include less than X% segment pairs of type "0:1", where X is the threshold provided by the user (default is 15) 
	 * The selected segments (to be added) should be of type identified in SegTypes. 
	 * 
	 */
	public void mergeTMXs() {
		LOGGER.info("------------Merging of generated TMXs for "+languages[0]+"-"+languages[1] +" language pair.------------");
		creationDescription = "The ILSP Focused Crawler was used for the acquisition "
				+ "of bilingual data from multilingual websites, and for the normalization, cleaning, (near) de-duplication and identification of parallel documents. "
				+ "The " + alignerStr + " sentence aligner was used for extracting segment alignments from crawled parallel documents. "
				+ "As a post-processing step, alignments were merged into one TMX file. "
				+ "The following filters were applied: ";
		outTMX = new File(baseName.getAbsolutePath()+TMXEXT);
		if (!outTMX.getParentFile().exists())
			outTMX.getParentFile().mkdirs();

		String filter1=" TMX files generated from document pairs which have been identified by non-"+ doctypes + " methods were discarded";
		String filter2=" TMX files with a zeroToOne_alignments/total_alignments ratio larger than "+ minPerce01Align + ", were discarded";
		String filter3=" Alignments of non-" + segtypes+ " type(s) were discarded.";
		String filter4=" Alignments with a TUV (after normalization) that has less than "+ minTuvLen + " tokens, were discarded/annotated";
		String filter5=" Alignments with a l1/l2 TUV length ratio smaller than " + minTuLenRatio+ " or larger than "+ maxTuLenRatio + ", were discarded/annotated";
		if (keepsn)
			filter6=" Alignments in which different digits appear in each TUV were discarded/annotated";
		if (keepiden)
			filter7=" Alignments with identical TUVs (after normalization) were discarded/annotated";
		if (keepem)
			filter8=" Alignments with only non-letters in at least one of their TUVs were discarded/annotated";
		if (keepdup)
			filter9=" Duplicate alignments were kept and were discarded/annotated";
		//LOGGER.info(filter1+"\n"+filter2+"\n"+filter3+"\n"+filter4+"\n"+filter5+"\n"+filter6+"\n"+filter7+"\n"+filter8+"\n"+filter9);

		List<ILSPAlignment> alignmentList = new ArrayList<ILSPAlignment>();
		FilenameFilter filter = new FilenameFilter() {			
			public boolean accept(File arg0, String arg1) {
				return (arg1.endsWith(TMXEXT) & arg1.contains(ISOLangCodes.get3LetterCode(languages[0])) & arg1.contains(ISOLangCodes.get3LetterCode(languages[1])));
			}
		};
		if (!iso6393){
			languages[0]=ISOLangCodes.get2LetterCode(languages[0]);
			languages[1]=ISOLangCodes.get2LetterCode(languages[1]);
		}else{
			languages[0]=ISOLangCodes.get3LetterCode(languages[0]);
			languages[1]=ISOLangCodes.get3LetterCode(languages[1]);			
		}		
		String[] types = new String[doctypes.length()];
		for (int ii=0;ii<doctypes.length();ii++){
			types[ii] = UNDERSCORE_STR+Character.toString(doctypes.charAt(ii))+TMXEXT;
		}
		List<File> tmxfiles = new ArrayList<File>();
		if (inputFile.isDirectory()){
			List<File> tfs = FcFileUtils.listFiles(inputFile, filter,true);
			tmxfiles = FcFileUtils.selectTypes(tfs, types);
		}else{ //it is considered a text file containing a list with full paths of targeted directories (a full path per line)
			List<String> targetdirs;
			try {
				targetdirs = FileUtils.readLines(inputFile);
				for (String targetdir:targetdirs){
					LOGGER.info("finding files from "+ targetdir);
					List<File> tfs = FcFileUtils.listFiles(new File(targetdir), filter,true);
					tmxfiles.addAll(FcFileUtils.selectTypes(tfs, types));
				}
			} catch (IOException e) {
				LOGGER.error("Problem in reading file "+ inputFile.getAbsolutePath() );
				e.printStackTrace();
			} 
		}
		if (tmxfiles.isEmpty()){
			LOGGER.info("No tmx files found.");
			return;
		}else
			LOGGER.info(filter1+"\n"+filter2+"\n"+filter3+"\n"+filter4+"\n"+filter5+"\n"+filter6+"\n"+filter7+"\n"+filter8+"\n"+filter9);

		creationDescription = creationDescription+filter1+" ; "+filter2+" ; "+filter3+" ; "+filter4+" ; "+filter5+" ; "+filter6+" ; "+filter7+" ; "+filter8+" ; "+filter9;
		List<String> domains = ReadResources.extactValueFromDocPair(tmxfiles, domainNode);
		List<String> domainEurovocIds=getEurovocId(domains);
		//FIXME
		String domain = StringUtils.join(domains, ',');
		String domainEurovocId = StringUtils.join(domainEurovocIds, ',');

		HashMap<String, List<File>> tmxTypeFiles =FcFileUtils.clusterfiles(tmxfiles,doctypes);
		for (int ii=0;ii<doctypes.length();ii++){
			String m= Character.toString(doctypes.charAt(ii));
			alignmentList = addTMXs(tmxTypeFiles.get(m),alignmentList,m, keepem, keepiden, keepdup, keepsn, clean, cc);
		}
		if (!alignmentList.isEmpty()){
			int[] stats1 = TMXHandlerUtils.countWordsInTMX(alignmentList, 1, true);
			int[] stats2 = TMXHandlerUtils.countWordsInTMX(alignmentList, 2, true);
			double[] scores = TMXHandlerUtils.scorestats(alignmentList);
			
			String organization = config.getString("resourceCreator.organization");
			String organizationURL = config.getString("resourceCreator.organizationURL"); 
			String projectId = config.getString("fundingProject.projectId"); 
			String projectURL = config.getString("fundingProject.projectURL"); 

			creationDescription = creationDescription + " There are "+ stats1[5] +" TUs with no annotation,"+
					" containing "+ stats1[2] +" words and "+ stats1[3] +" lexical types in "+ TMXHandler.languages[0] + 
					" and "+ stats2[2] +" words and "+ stats2[3]+" lexical types in "+ TMXHandler.languages[1] +
					". The mean value of aligner's scores is "+ scores[0]+ ", the std value is "+ scores[1];

			BilingualCorpusInformation bilingualCorpusInfo;
			if (cc) {
				bilingualCorpusInfo = new BilingualCorpusInformation(FilenameUtils.getBaseName(outTMX.getAbsolutePath()), TMXHandler.languages[0], TMXHandler.languages[1], 
						alignmentList, alignmentList.size(), stats1[5], stats1[0], stats2[0], stats1[1], stats2[1], stats1[2], stats2[2], stats1[3], stats2[3], scores[0], scores[1],
						domain, domainEurovocId, FREE_STR, creationDescription,
						projectId, projectURL, organization, organizationURL);
			} else {
				bilingualCorpusInfo = new BilingualCorpusInformation(FilenameUtils.getBaseName(outTMX.getAbsolutePath()), TMXHandler.languages[0], TMXHandler.languages[1], 
						alignmentList, alignmentList.size(), stats1[5], stats1[0], stats2[0], stats1[1], stats2[1], stats1[2], stats2[2], stats1[3], stats2[3], scores[0], scores[1], 
						domain, domainEurovocId, UNKNOWN_STR, creationDescription,
						projectId, projectURL, organization, organizationURL);
			}
			if (oxslt) 
				outHTML =  new File(baseName.getAbsolutePath() + HTML);

			generateMergedTMX(outTMX, languages, bilingualCorpusInfo, outHTML);

			BilingualTmxMetashareDescriptor bilingualTmxMetashareDescriptor = new BilingualTmxMetashareDescriptor(bilingualCorpusInfo);
			File metadataFile = new File(baseName.getAbsolutePath()+ MetadataExt);
			LOGGER.info("Generating metadata descriptor " + metadataFile);
			bilingualTmxMetashareDescriptor.setOutFile(metadataFile);
			if (iso6393) {
				bilingualTmxMetashareDescriptor.setMetadataLang("eng");
			} else {
				bilingualTmxMetashareDescriptor.setMetadataLang("en");
			}
			bilingualTmxMetashareDescriptor.run();
			if (o1!=null){
				try {
					FileUtils.copyFile(outHTML, o1);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (o2!=null){
				try {
					FileUtils.copyFile(outTMX, o2);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}else{
			LOGGER.info("No proper TUs found.");
		}
	}

	/**
	 * Gets Eurovoc id for each element of domain 
	 * @param domain
	 * @return
	 */
	public static List<String> getEurovocId(List<String> domains) {
		List<String> domainEurovocIds=new ArrayList<String>();
		Eurovoc eurovoc = Eurovoc.INSTANCE;
		String did="";
		for (String d:domains){
			did = eurovoc.getIdentifier(d);
			if (did!=null)
				domainEurovocIds.add(did);

		}
		return domainEurovocIds;
	}


	/**
	 * Generates the MergedTMX (outTMX), and its XSLT transformation (outHTML, if asked).
	 * Alignments are in (alignmentList). 
	 * @param outTMX
	 * @param lang1
	 * @param lang2
	 * @param bilingualCorpusInformation
	 * @param outHTML
	 */
	public static void generateMergedTMX(File outTMX, String[] languages,BilingualCorpusInformation bilingualCorpusInformation, File outHTML) {
		Writer writer;
		try {
			writer = new OutputStreamWriter(new FileOutputStream(outTMX.getAbsolutePath()), UTF_8);
			BilingualScoredTmxFormatterILSP bilingualScoredTmxFormatter = new BilingualScoredTmxFormatterILSP(writer, languages[0], languages[1],null, null, bilingualCorpusInformation);
			bilingualScoredTmxFormatter.setSkipZeroToOneAlignments(true);
			bilingualScoredTmxFormatter.setPrintAlignmentCategory(false);
			bilingualScoredTmxFormatter.formatILSPAlignments(bilingualCorpusInformation.getAlignmentList());
			writer.close();
			LOGGER.info("Merged TMX at " + outTMX.getAbsolutePath());
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("problem in writing (unsupported encoding) of the merged TMX file "+ outTMX.getAbsolutePath());
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			LOGGER.error("problem in writing (file not found) of the merged TMX file "+ outTMX.getAbsolutePath());
			e.printStackTrace();
		} catch (IOException e) {
			LOGGER.error("problem in writing (writer error) of the merged TMX file "+ outTMX.getAbsolutePath());
			e.printStackTrace();
		}
		if (outHTML!=null ){
			try {
				XSLTransformer xslTransformer = new XSLTransformer(XSL_TMX2HTML);
				xslTransformer.setBaseDir(outTMX.getParent());
				xslTransformer.transform(outTMX, outHTML);
				LOGGER.info("Rendering merged TMX as " + outHTML.getAbsolutePath());
			} catch (TransformerConfigurationException | IOException e) {
				LOGGER.warn("problem in writing the transformed merged TMX file: "+ outHTML.getAbsolutePath());
				e.printStackTrace();
			} catch (TransformerException e) {
				LOGGER.warn("problem in writing the transformed merged TMX file: "+ outHTML.getAbsolutePath());
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * TUs are excluded if: are identical TUs (), have identical TUVs, have an empty TUV, have alignmentType (i.e. 1:2, 2:1, etc) not in "segtypes"   
	 * @param tmxFiles
	 * @param alignmentList
	 * @param type
	 * @return
	 */

	/**
	 * Enriches list of Alignments (alignmentList) by examining tmxFiles of specific type (i.e. TMXs based on document pairs detected by specific method)
	 * @param tmxFiles
	 * @param alignmentList
	 * @param type
	 * @param keepem
	 * @param keepiden
	 * @param keepdup
	 * @param cc
	 * @param cc2 
	 * @return
	 */

	private List<ILSPAlignment> addTMXs(List<File> tmxFiles, List<ILSPAlignment> alignmentList, String type, boolean keepem, boolean keepiden, boolean keepdup, boolean ksn, boolean clean, boolean cc) {
		LOGGER.info("Examining docpairs of type "+ type);
		int thr = thres[doctypes.indexOf(type)];
		if (tmxFiles==null)
			return alignmentList;
		if (tmxFiles.isEmpty())
			return alignmentList;
		for (File tmxFile : tmxFiles) {
			List<SegPair> segpairs = TMXHandlerUtils.getTUsFromTMX(tmxFile,thr, minPerce01Align, languages[0], languages[1], cc);
			if (segpairs==null){
				LOGGER.info("Cut due to many 0:1 alignments: " +tmxFile.getAbsolutePath());
				continue;
			}
			totalcounter=totalcounter+segpairs.size();
			float ratio;
			for (SegPair segpair:segpairs){
				if (!segtypes.isEmpty()){
					if (!segtypes.contains(segpair.type))
						continue;
				}
				String info="";//, info1="";
				//FIXME add constrains for length, or other "filters"

				String normS = ContentNormalizer.normtext(segpair.seg1);
				String normT = ContentNormalizer.normtext(segpair.seg2);
				if ( normS.isEmpty() || normT.isEmpty()){
					if (clean)
						continue;
					if (!keepem)
						continue;
					info =  mes1;
				}
				if (normS.equals(normT)){
					if (clean)
						continue;
					if (!keepiden)
						continue;
					if (info.isEmpty()){	info =  mes2;}		else{	info =  info + " | "+mes2;}	
				}
				if (ValidateUtils.isValidEmailAddress(segpair.seg1) || ValidateUtils.isValidEmailAddress(segpair.seg2)){
					if (clean)
						continue;
					if (!keepem)
						continue;
					if (info.isEmpty()){	info =  mes8;}		else{	info =  info + " | "+mes8;}	
				}

				List<String> stokens = FCStringUtils.getTokens(normS);
				List<String> ttokens = FCStringUtils.getTokens(normT);
				Double[] stokenslen = FCStringUtils.getTokensLength(stokens);
				Double[] ttokenslen = FCStringUtils.getTokensLength(ttokens);
				if (Statistics.getMax(stokenslen)>max_word_length || Statistics.getMax(ttokenslen)>max_word_length){
					LOGGER.info("discarded TU, very large word (due to bad text extraction from pdf):"+ segpair.seg1 +"\t"+ segpair.seg2);
					//String info1 = "a: "+ max_word_length;
					continue;
				}else{
					if (Statistics.getMedian(stokenslen)>=median_word_length || Statistics.getMedian(ttokenslen)>=median_word_length){
						LOGGER.info("discarded TU, very large words (due to bad text extraction from pdf):"+ segpair.seg1 +"\t"+ segpair.seg2);
						//	String info1 = "a: "+ median_word_length;
						continue;
					}
				}
				//if (!info1.isEmpty()){
				//	if (info.isEmpty()){	info = info1;}	else{	info = info + " | "+info1;}
				//}	
				if (FCStringUtils.countTokens(normS)<minTuvLen || FCStringUtils.countTokens(normT)<minTuvLen){
					if (clean)
						continue;
					if (info.isEmpty()){	info = mes4+minTuvLen ;}		else{	info = info + " | "+mes4+minTuvLen ;}
				}
				ratio = (float)segpair.seg1.length()/(float)segpair.seg2.length();
				if (ratio>maxTuLenRatio || ratio < minTuLenRatio){
					if (clean)
						continue;
					if (info.isEmpty()){	info = mes5+  minTuLenRatio +mes5a+ maxTuLenRatio;}	
					else{	info = info + " | "+mes5+  minTuLenRatio +mes5a+ maxTuLenRatio ;}
				}
				String num1=segpair.seg1.replaceAll("\\D+","");
				String num2=segpair.seg2.replaceAll("\\D+","");
				if (!num1.equals(num2)){
					if (clean)
						continue;
					if (ksn)
						continue;
					if (info.isEmpty()){	info =  mes6;}		else{	info =  info + " | "+mes6;}	
				}
				String temp = normS+TAB+normT;
				if (segs.contains(temp)){
					if (clean)
						continue;
					if (info.isEmpty()){	info =  mes7;}		else{	info =  info + " | "+mes7;}
					if (!keepdup)
						continue;
				}else
					segs.add(temp);
				ILSPAlignment alignment = new ILSPAlignment();
				alignment.addSourceSegment(segpair.seg1);
				alignment.addTargetSegment(segpair.seg2);
				alignment.setScore((float)segpair.score);
				alignment.setL1url(segpair.l1url);
				alignment.setL2url(segpair.l2url);

				//alignment.setSite(segpair.site);
				alignment.setMethod(segpair.method);
				alignment.setLicense(segpair.license);
				alignment.setType(segpair.type);
				alignment.setLengthRatio(Float.toString(ratio));
				alignment.setInfo(info);
				alignmentList.add(alignment);
			}
		}
		LOGGER.info("Number of Kept/Annotated Alignments: "+alignmentList.size()+"\t"+"totalNumofSegmentPairs: "+totalcounter);
		return alignmentList;
	}


	/**
	 * absolute path of the baseName for the outfiles  
	 * @param baseName
	 */
	public void setBaseName(File baseName) {
		TMXHandler.baseName  = baseName;
	}

	/**
	 * absolute path of the directory containing the TMXs to be merged
	 * @param targetDir
	 */
	public void setTargetDir(File inputFile) {
		TMXHandler.inputFile  = inputFile;
	}

	/**
	 * language iso codes separated by ";"
	 * @param languages
	 */
	public void setLanguage(String languages) {
		TMXHandler.languages = languages.split(SEMICOLON_STR);
	}
	public void useISO6393(boolean iso6393) {
		TMXHandler.iso6393 = iso6393;
	}

	/**
	 * apply transformation of the generated TMX to HTML. if exists, an HTML file will be created next to the generated TMX
	 * @param offlineXSLT
	 */
	public void setApplyOfflineXSLT(boolean offlineXSLT) {
		TMXHandler.oxslt  = offlineXSLT;
	}

	public void setCC(boolean cc) {
		TMXHandler.cc  = cc;
	}
	//public void setMetadata(boolean metadata) {
	//	TMXHandler.metadata  = metadata;
	//}

	public void KeepTuSameNum(boolean keep){
		TMXHandler.keepsn=keep;
	}

	public void setKeepEmpty(boolean keepem){
		TMXHandler.keepem=keepem;
	}

	public void setKeepIdentical(boolean keepiden){
		TMXHandler.keepiden=keepiden;
	}

	public void setKeepDuplicates(boolean keepdup){
		TMXHandler.keepdup=keepdup;
	}

	public void setClean(boolean clean){
		TMXHandler.clean=clean;
	}
	/**
	 * types of TMXs to be merged, i.e. method by which the documents have been paired (and then aligned), default "auidhml" 
	 * @param docTypes
	 */
	public void setDocTypes(String docTypes) {
		TMXHandler.doctypes=docTypes;
	}
	/**
	 * maximum number of 0:1 segment pairs that could exist in a TMX in order to be selected.
	 * a threshold for each pair detection method, default is "100;100;100;100;100;100;100"
	 * @param thres
	 */
	public void setThres(int[] thres) {
		TMXHandler.thres=thres;
	}
	/**
	 * types of segments to be included, i.e. 1:1, 1:2, etc, default is all (empty list). 1:0, and 0:1 are of course excluded
	 * @param docTypes
	 */
	public void setSegTypes(List<String> list) {
		TMXHandler.segtypes=list;
	}

	public void setMinTuvLen(int minTuvLen) {
		TMXHandler.minTuvLen = minTuvLen;
	}
	public void setMinPerce01Align(double minPerce01align) {
		TMXHandler.minPerce01Align = minPerce01align;
	}
	public void setMinTuLenRatio(double minTuLenRatio) {
		TMXHandler.minTuLenRatio = minTuLenRatio;
	}
	public void setMaxTuLenRatio(double maxTuLenRatio) {
		TMXHandler.maxTuLenRatio = maxTuLenRatio;
	}
	/**
	 * Loads the default configuration file and checks if user supplied a custom one.
	 * @param type
	 * @param confFile
	 * @return
	 */
	public static CompositeConfiguration getConfig( String confFile) {
		config = new CompositeConfiguration();
		URL default_config = Crawl.class.getClassLoader().getResource(DEFAULT_BI_CONFIG_FILE);
		if (confFile!=null){
			String custom_config = confFile;
			try {
				XMLConfiguration xml_custom_config = new XMLConfiguration(custom_config);
				xml_custom_config.setValidating(true);
				config.addConfiguration(xml_custom_config);
			} catch (ConfigurationException e) {
				LOGGER.error("Invalid configuration file: " + custom_config);
			}
		}
		try {			
			config.addConfiguration(new XMLConfiguration(default_config));				
		} catch (ConfigurationException e1) {
			// Shouldn't happen
			LOGGER.error("Problem with default configuration file.");
		}
		return config;
	}

	public void setO1(File o1) {
		TMXHandler.o1 = o1;
	}
	public void setO2(File o2) {
		TMXHandler.o2 = o2;
	}
}
