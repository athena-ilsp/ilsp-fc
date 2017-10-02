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
//import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
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
//import org.apache.commons.configuration.ConfigurationException;
//import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
//import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

//import gr.ilsp.fc.langdetect.LangDetectUtils;
import gr.ilsp.fc.langdetect.LangDetector;
import gr.ilsp.fc.readwrite.ReadResources;
import gr.ilsp.fc.tmxhandler.TMXHandlerUtils;
import gr.ilsp.fc.tmxhandler.TMXHandlerUtils.TUsNumStats;
import gr.ilsp.fc.tmxhandler.TMXHandlerUtils.TUsRatioStats;
import gr.ilsp.fc.tmxhandler.TMXHandlerUtils.TUsScoreStats;
//import gr.ilsp.fc.aligner.factory.BilingualScoredTmxFormatter;
import gr.ilsp.fc.aligner.factory.BilingualScoredTmxFormatterILSP;
import gr.ilsp.fc.aligner.factory.ILSPAlignment;
import gr.ilsp.fc.corpora.BilingualCorpusInformation;
import gr.ilsp.fc.corpora.BilingualTmxMetashareDescriptor;
import gr.ilsp.fc.tmxhandler.TMXHandlerUtils.SegPair;
import gr.ilsp.fc.utils.ContentNormalizer;
import gr.ilsp.fc.utils.Eurovoc;
import gr.ilsp.fc.utils.FCStringUtils;
import gr.ilsp.fc.utils.FcFileUtils;
import gr.ilsp.fc.utils.ISOLangCodes;
import gr.ilsp.fc.utils.Statistics;
import gr.ilsp.fc.utils.XSLTransformer;
import gr.ilsp.nlp.commons.Constants;

public class TMXHandler {
	private static final Logger LOGGER = Logger.getLogger(TMXHandler.class);
	private static TMXHandlerOptions options = null;
	private static File inputFile = null;
	private static File baseName = null;
	private static File outTMX = null;
	private static File outHTML = null;
	private static File o1 = null;
	private static File o2 = null;
	private static List<String> targ_sites=new ArrayList<String>();
	private static final String DEFAULT_BI_CONFIG_FILE = "FBC_config.xml";
	private static CompositeConfiguration config;
	private static String[] languages;
	private static String usertopic;
	private static int[] thres = { 100,100,100,100,100,100,100};
	private static boolean oxslt=false;
	private static boolean iso6393=true;
	private static boolean cc=false;
	private static boolean keepem = false;
	private static boolean keepiden = false;
	private static boolean keepdup = false;
	private static boolean clean = false;
	private static boolean keepsn=false;
	private static int minTuvLen=0;
	private static int maxSize=1000000000;
	private final static double sampleSizePerCe = 0.1; //fixed to 10% of the total collection 
	private static double minPerce01Align=1;
	private static double minTuLenRatio = 0;
	private static double maxTuLenRatio = 100;
	private final static double median_word_length=25;
	private final static double max_word_length=30;
	private static String doctypes="aupidh";// = "aupidhml";
	private static List<String> segtypes;
	private static Set<String> segs = new HashSet<String>() ;
	static Matcher twitterHandleMatcher = Pattern.compile("(^|[^@\\w])@(\\w{1,15})\\b").matcher("");
	//private final static String PUNCT = ".";
	private final static String UNKNOWN_STR ="unknown";
	private final static  String SAMPLE ="_sample.csv";
	private final static String HTML =".html";
	private final static String SITES =".sites.txt";
	private final static String SITESN =".sitesn.txt";
	private final static String TMXEXT = ".tmx";
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
	private final static String mes9 = "url";

	private static LangDetector langDetector;
	public static Set<String> langsTBFI = new HashSet<String>(Arrays.asList("bos", "hrv", "srp")); 
	private static int maxSampleSize;		//no more than this value
	
	private static int totalcounter=0;
	//private static int distthr=5; //FIXME add as parameter

	public static CompositeConfiguration getConfig() {
		return config;
	}

	public void setConfig(CompositeConfiguration config) {
		TMXHandler.config = config;
	}

	private String alignerStr = "Maligna";

	private String creationModeDescription = "";
	private String creationDescription = "";
	private String filter6 = " Alignments in which different digits appear in each TUV were kept and annotated.";
	private String filter7 = " Alignments with identical TUVs (after normalization) were removed.";
	private String filter8 = " Alignments with only non-letters in at least one of their TUVs were removed";
	private String filter9 = " Duplicate alignments were discarded";

	public static void main(String[] args) {
		TMXHandler tm = new TMXHandler();
		options = new TMXHandlerOptions();
		options.parseOptions(args);
		tm.setTargetDir(options.getTargetDir());
		tm.setConfig(getConfig( options.getConfig()));
		tm.setDocTypes(options.getDocTypes());
		tm.setThres(options.getThres());
		tm.setSegTypes(options.getSegTypes());
		tm.setMaxSize(options.getMaxSize());
		tm.setApplyOfflineXSLT(options.getXSLTransform());
		tm.setLanguage(options.getLanguage());
		tm.useISO6393(options.useISO6393());
		tm.setMinTuvLen(options.getMinTuvLen());
		tm.setMinPerce01Align(options.getMinPerce01Align());
		tm.setMinTuLenRatio(options.getMinTuLenRatio());
		tm.setMaxTuLenRatio(options.getMaxTuLenRatio());
		tm.KeepTuSameNum(options.keepTuSameNum());
		tm.setCC(options.getCC());
		tm.setKeepEmpty(options.getKeepEmpty());
		tm.setKeepIdentical(options.getKeepIdentical());
		tm.setKeepDuplicates(options.getKeepDuplicates());
		tm.setClean(options.getClean());
		//tm.setSites(options.getSites());
		tm.setO1(options.getO1());
		tm.setO2(options.getO2());
		tm.setUserTopic(options.getTargetedDomain());
		tm.setLangDetector(options.getLangDetector()); 
		tm.setMaxSampleSize(options.getMaxSampleSize());
		String[] languages = options.getLanguage().split(Constants.SEMICOLON);
		List<String> lang_pairs = new ArrayList<String>();
		if (languages.length>1){
			for (int ii=0;ii<languages.length-1;ii++){
				for (int jj=ii+1;jj<languages.length;jj++){
					lang_pairs.add(languages[ii]+Constants.SEMICOLON+languages[jj]);
				}
			}
		}
		for (String lang_pair:lang_pairs){
			tm.setLanguage(lang_pair);
			String[] temp_langs = lang_pair.split(Constants.SEMICOLON);
			String lang = Constants.UNDERSCORE+temp_langs[0]+Constants.HYPHEN+temp_langs[1];
			tm.setBaseName(new File(options.getBaseName()+lang));
			tm.mergeTMXs();
		}
	}

	/**
	 * Gets selected TMXs from the targeted directory and its sub directories a directory and adds selected segments of these TMXs in a new TMX file.
	 * The selected TMXs should be extracted from document pairs which have been identified by the methods defined in DocTypes.
	 * The selected TMXs should include less than X% segment pairs of type "0:1", where X is the threshold provided by the user (default is 15) 
	 * The selected segments (to be added) should be of type identified in SegTypes. 
	 * 
	 */
	public void mergeTMXs() {
		LOGGER.info("------------Merging of generated TMXs for "+languages[0]+"-"+languages[1] +" language pair.------------");
		String[] _targetedLangs = new String[2]; _targetedLangs[0] = languages[0]; _targetedLangs[1] = languages[1];
		//_langDetector = LangDetectUtils.loadLangDetectors(_targetedLangs,defaultlangDetectorId);

		creationModeDescription = "The ILSP Focused Crawler was used for the acquisition "
				+ "of bilingual data from multilingual websites, and for the normalization, cleaning, (near) de-duplication and identification of parallel documents. "
				+ "The " + alignerStr + " sentence aligner was used for extracting segment alignments from crawled parallel documents. "
				+ "As a post-processing step, alignments were merged into one TMX file. "
				+ "The following filters were applied: ";
		
		//creationModeDescription = "It was created/offered by the Ľudovít Štúr Institute of Linguistics, Slovak Academy of Sciences."
		//		+ "As a post-processing process several filters were applied to discard/annotate alignmentsthat that might be incorrect";
		
		outTMX = new File(baseName.getAbsolutePath()+TMXEXT);
		if (!outTMX.getParentFile().exists())
			outTMX.getParentFile().mkdirs();

		String filter1=" TMX files generated from document pairs which have been identified by non-"+ doctypes + " methods were discarded";
		String filter2=" TMX files with a zeroToOne_alignments/total_alignments ratio larger than "+ minPerce01Align + ", were discarded";
		String filter3=" Alignments of non-" + segtypes+ " type(s) were discarded.";
		String filter4=" Alignments with a TUV (after normalization) that has less than "+ minTuvLen + " tokens, were discarded/annotated";
		String filter5=" Alignments with a l1/l2 TUV length ratio smaller than " + minTuLenRatio+ " or larger than "+ maxTuLenRatio + ", were discarded/annotated";
		if (keepsn)
			filter6=" Alignments in which different digits appear in each TUV were discarded";
		if (keepiden)
			filter7=" Alignments with identical TUVs (after normalization) were annotated";
		if (keepem)
			filter8=" Alignments with only non-letters in at least one of their TUVs were annotated";
		if (keepdup)
			filter9=" Duplicate alignments were kept and were annotated";

		List<ILSPAlignment> alignmentList = new ArrayList<ILSPAlignment>();
		FilenameFilter filter = new FilenameFilter() {			
			public boolean accept(File arg0, String arg1) {
				return (arg1.endsWith(TMXEXT) & arg1.contains(ISOLangCodes.get3LetterCode(languages[0])) & arg1.contains(ISOLangCodes.get3LetterCode(languages[1])));
			}
		};
		/*filter = new FilenameFilter() {			
			public boolean accept(File arg0, String arg1) {
				return (arg1.endsWith(TMXEXT) );
			}
		};*/
		
		if (!iso6393){
			languages[0]=ISOLangCodes.get2LetterCode(languages[0]);
			languages[1]=ISOLangCodes.get2LetterCode(languages[1]);
		}else{
			languages[0]=ISOLangCodes.get3LetterCode(languages[0]);
			languages[1]=ISOLangCodes.get3LetterCode(languages[1]);			
		}		
		String[] types = new String[doctypes.length()];
		for (int ii=0;ii<doctypes.length();ii++){
			types[ii] = Constants.UNDERSCORE+Character.toString(doctypes.charAt(ii))+TMXEXT;
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
		creationModeDescription = creationModeDescription+filter1+" ; "+filter2+" ; "+filter3+" ; "+filter4+" ; "+filter5+" ; "+filter6+" ; "+filter7+" ; "+filter8+" ; "+filter9;

		List<String> domains = ReadResources.extactValueFromDocPair(tmxfiles, domainNode);
		if (domains.isEmpty()){
			if (!StringUtils.isBlank(usertopic))
				domains.add(usertopic);
		}else{
			if (!StringUtils.isBlank(usertopic)){
				if (!domains.contains(usertopic)){
					LOGGER.warn("User-defined topic is different from the identified topic(s). All will be added");
					domains.add(usertopic);
				}
			}
		}
		List<String> domainEurovocIds=getEurovocId(domains);
		//FIXME shall we integrate JRX Eurovoc Indexer?
		String domain = StringUtils.join(domains, ',');
		String domainEurovocId = StringUtils.join(domainEurovocIds, ',');

		HashMap<String, List<File>> tmxTypeFiles =FcFileUtils.clusterfiles(tmxfiles,doctypes);
		for (int ii=0;ii<doctypes.length();ii++){
			String m= Character.toString(doctypes.charAt(ii));
			alignmentList = addTMXs(tmxTypeFiles.get(m),alignmentList,m, keepem, keepiden, keepdup, keepsn, clean, cc,targ_sites, maxSize);
		}
		if (!alignmentList.isEmpty()){
			TUsScoreStats scores = TMXHandlerUtils.scorestats(alignmentList, true);
			TUsRatioStats ratios = TMXHandlerUtils.ratiostats(alignmentList, true);
			TUsNumStats stats1 = TMXHandlerUtils.numstats(alignmentList, 1, true);
			TUsNumStats stats2 = TMXHandlerUtils.numstats(alignmentList, 2, true);
			List<String> sites_all = TMXHandlerUtils.getSites(alignmentList, false);
			List<String> sites_noannot = TMXHandlerUtils.getSites(alignmentList, true);
			creationModeDescription = creationModeDescription + 
					".\nThe mean value of aligner's scores is "+ scores.meanscore_all+ ", the std value is "+ scores.stdscore_all +
					". The mean value of length (in terms of characters) ratios is "+ ratios.meanratio_all+ " and the std value is "+ ratios.stdratio_all + "." +
					"\nThere are "+ stats1.tus_noan +" TUs with no annotation,"+
					" containing "+ stats1.tokens_noan +" words and "+ stats1.words_noan +" lexical types in "+ TMXHandler.languages[0] + 
					" and "+ stats2.tokens_noan +" words and "+ stats2.words_noan+" lexical types in "+ TMXHandler.languages[1] +
					". The mean value of aligner's scores is "+ scores.meanscore_noan+ ", the std value is "+ scores.stdscore_noan +
					". The mean value of length (in terms of characters) ratios is "+ ratios.meanratio_noan + " and the std value is "+ ratios.stdratio_noan + "." ;

			creationDescription = "Parallel ("+ languages[0] + Constants.HYPHEN + languages[1] +") corpus of "+
					alignmentList.size()+ " (" +stats1.tus_noan +" not-annotated) translation units";
			if (!StringUtils.isEmpty(domain))
				creationDescription = creationDescription + " in the "+	domain+" domain";

			BilingualCorpusInformation bilingualCorpusInfo = new BilingualCorpusInformation();
			bilingualCorpusInfo.setName(FilenameUtils.getBaseName(outTMX.getAbsolutePath()));
			bilingualCorpusInfo.setL1(TMXHandler.languages[0]);
			bilingualCorpusInfo.setL2(TMXHandler.languages[1]);
			bilingualCorpusInfo.setAlignmentList(alignmentList);

			bilingualCorpusInfo.setAlignmentsSize(alignmentList.size());
			bilingualCorpusInfo.setLenInWordsL1(stats1.tokens_all);
			bilingualCorpusInfo.setLenInWordsL2(stats2.tokens_all);
			bilingualCorpusInfo.setVocSizeInL1(stats1.words_all);
			bilingualCorpusInfo.setVocSizeInL2(stats2.words_all);

			bilingualCorpusInfo.setCleanLenInTUs(stats1.tus_noan);
			bilingualCorpusInfo.setCleanVocSizeInL1(stats1.words_noan);
			bilingualCorpusInfo.setCleanVocSizeInL2(stats2.words_noan);
			bilingualCorpusInfo.setCleanLenInWordsL1(stats1.words_noan);
			bilingualCorpusInfo.setCleanLenInWordsL2(stats2.words_noan);

			bilingualCorpusInfo.setMeanAlignScore(scores.meanscore_all);
			bilingualCorpusInfo.setMeanAlignScoreNo(scores.meanscore_noan);
			bilingualCorpusInfo.setStdAlignScore(scores.stdscore_all);
			bilingualCorpusInfo.setStdAlignScoreNo(scores.stdscore_noan);

			bilingualCorpusInfo.setMeanRatioScore(ratios.meanratio_all);
			bilingualCorpusInfo.setMeanRatioScoreNo(ratios.meanratio_noan);
			bilingualCorpusInfo.setStdRatioScore(ratios.stdratio_all);
			bilingualCorpusInfo.setStdRatioScoreNo(ratios.stdratio_noan);

			bilingualCorpusInfo.setDomain(domain);
			bilingualCorpusInfo.setDomainId(domainEurovocId);
			bilingualCorpusInfo.setCreationDescription(creationModeDescription);
			bilingualCorpusInfo.setDescription(creationDescription);
			bilingualCorpusInfo.setProjectId(config.getString("fundingProject.projectId"));
			bilingualCorpusInfo.setProjectURL(config.getString("fundingProject.projectURL"));
			bilingualCorpusInfo.setOrganization(config.getString("resourceCreator.organization"));
			bilingualCorpusInfo.setOrganizationURL(config.getString("resourceCreator.organizationURL"));
			if (cc) 
				bilingualCorpusInfo.setAvailability(FREE_STR);
			else
				bilingualCorpusInfo.setAvailability(UNKNOWN_STR);
			if (oxslt) 
				outHTML =  new File(baseName.getAbsolutePath() + HTML);

			generateMergedTMX(outTMX, languages, bilingualCorpusInfo, outHTML);
			
			LOGGER.info("Generating language files");
			TMXHandlerUtils.splitIntolangFiles(alignmentList, languages, baseName);
			
			int sampleSizeCe = (int)((double)alignmentList.size()*sampleSizePerCe);
			int sampleSize = Math.min(maxSampleSize, sampleSizeCe);
			File samplefile = new File(baseName.getAbsolutePath() + SAMPLE);
			LOGGER.info("Generating sample file " + samplefile.getAbsolutePath());
			TMXHandlerUtils.generateSample(alignmentList, sampleSize, samplefile);
			
			try {
				FileUtils.writeLines(new File(baseName.getAbsolutePath() + SITES), sites_all);
				FileUtils.writeLines(new File(baseName.getAbsolutePath() + SITESN), sites_noannot);
			} catch (IOException e1) {
				LOGGER.error("problem in writing lists of sites");
				e1.printStackTrace();
			}

			BilingualTmxMetashareDescriptor bilingualTmxMetashareDescriptor = new BilingualTmxMetashareDescriptor(bilingualCorpusInfo);
			File metadataFile = new File(baseName.getAbsolutePath()+ MetadataExt);
			LOGGER.info("Generating metadata descriptor " + metadataFile);
			bilingualTmxMetashareDescriptor.setOutFile(metadataFile);
			if (iso6393)
				bilingualTmxMetashareDescriptor.setMetadataLang("eng");
			else 
				bilingualTmxMetashareDescriptor.setMetadataLang("en");

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
	 * @param sites 
	 * @param maxSize 
	 * @param cc2 
	 * @return
	 */

	private List<ILSPAlignment> addTMXs(List<File> tmxFiles, List<ILSPAlignment> alignmentList, String type,
			boolean keepem, boolean keepiden, boolean keepdup, boolean ksn, boolean clean, boolean cc, List<String> sites, int maxSize) {
		LOGGER.info("Examining docpairs of type "+ type);
		int thr = thres[doctypes.indexOf(type)];
		if (tmxFiles==null)
			return alignmentList;
		if (tmxFiles.isEmpty())
			return alignmentList;
		for (File tmxFile : tmxFiles) {
			if (alignmentList.size()>maxSize)
				return alignmentList;
			List<SegPair> segpairs = TMXHandlerUtils.getTUsFromTMX(tmxFile,thr, minPerce01Align, languages[0], languages[1], cc,sites);
			if (segpairs==null){
				LOGGER.info("Cut due to many 0:1 alignments: " +tmxFile.getAbsolutePath());
				continue;
			}
			totalcounter=totalcounter+segpairs.size();
			float ratio;
			for (SegPair segpair:segpairs){
				if (segpair.l1url.contains("twitter.") || segpair.l2url.contains("twitter.")) //temp addition. it should be removed 
					continue;
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
				if (TMXHandlerUtils.checkemail(segpair.seg1) || TMXHandlerUtils.checkemail(segpair.seg2)){
				//if (ValidateUtils.isValidEmailAddress(segpair.seg1) || ValidateUtils.isValidEmailAddress(segpair.seg2)){
					if (clean)
						continue;
					if (!keepem)
						continue;
					if (info.isEmpty()){	info =  mes8;}		else{	info =  info + " | "+mes8;}	
				}
				if (TMXHandlerUtils.checkurl(segpair.seg1) || TMXHandlerUtils.checkurl(segpair.seg2)){
				//if (ValidateUtils.isValidUrl(segpair.seg1) || ValidateUtils.isValidUrl(segpair.seg2)){
					if (clean)
						continue;
					if (!keepem)
						continue;
					if (info.isEmpty()){	info =  mes9;}		else{	info =  info + " | "+mes9;}	
				}

				/*List<String> stokens = FCStringUtils.getTokens(normS);
				List<String> ttokens = FCStringUtils.getTokens(normT);
				Double[] stokenslen = FCStringUtils.getTokensLength(stokens);
				Double[] ttokenslen = FCStringUtils.getTokensLength(ttokens);*/
				
				List<String> stokens = FCStringUtils.getTokens(segpair.seg1);
				List<String> ttokens = FCStringUtils.getTokens(segpair.seg2);
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
					if (!keepdup)
						continue;
					if (info.isEmpty()){	info =  mes7;}		else{	info =  info + " | "+mes7;}

				}else{
					/*boolean check =false;
					String identifiedlanguage1 = _langDetector.detect(segpair.seg1);
					if (langsTBFI.contains(identifiedlanguage1)) 
						identifiedlanguage1 = _langDetector.detect(segpair.seg1, identifiedlanguage1);
					if (!languages[0].equals(LangDetectUtils.updateLanguages(identifiedlanguage1, false)))
						check=true;
					String identifiedlanguage2 = _langDetector.detect(segpair.seg2);
					if (langsTBFI.contains(identifiedlanguage2)) 
						identifiedlanguage2 = _langDetector.detect(segpair.seg2, identifiedlanguage2);
					if (!languages[1].equals(LangDetectUtils.updateLanguages(identifiedlanguage2, false)))
						check=true;
					if (check && info.isEmpty()){
						System.out.println(identifiedlanguage1+"\t"+segpair.seg1+"\t"+identifiedlanguage2+"\t"+segpair.seg2);
						continue;
					}else*/
					segs.add(temp);
				}
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
	public File getTargetDir() {
		return inputFile ;
	}

	/**
	 * language iso codes separated by ";"
	 * @param languages
	 */
	public void setLanguage(String languages) {
		TMXHandler.languages = languages.split(Constants.SEMICOLON);
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
	/**
	 * only TUs coming from CC licensed documents are kept
	 * @param cc
	 */
	public void setCC(boolean cc) {
		TMXHandler.cc  = cc;
	}

	/**
	 * TUs with different number in TUVs are annotated
	 * @param keep
	 */
	public void KeepTuSameNum(boolean keep){
		TMXHandler.keepsn=keep;
	}
	/**
	 * TUs with a TUV with no letters are annotated
	 * @param keepem
	 */
	public void setKeepEmpty(boolean keepem){
		TMXHandler.keepem=keepem;
	}
	/**
	 * TUs with identical TUVs are annotated
	 * @param keepiden
	 */
	public void setKeepIdentical(boolean keepiden){
		TMXHandler.keepiden=keepiden;
	}
	/**
	 * duplicate TUs are annotated
	 * @param keepdup
	 */
	public void setKeepDuplicates(boolean keepdup){
		TMXHandler.keepdup=keepdup;
	}
	/**
	 * TUs with annotation are excluded
	 * @param clean
	 */
	public void setClean(boolean clean){
		TMXHandler.clean=clean;
	}
	/**
	 * types of TMXs to be merged, i.e. method by which the documents have been paired (and then aligned), default "aupidh" 
	 * @param docTypes
	 */
	public void setDocTypes(String docTypes) {
		TMXHandler.doctypes=docTypes;
	}
	/**
	 * maximum absolute number of 0:1 segment pairs that could exist in a TMX in order to be selected.
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
	/**
	 * TUs with a TUV with length (in tokens) less than minTuvLen (default is 0) are annotated
	 * @param minTuvLen
	 */
	public void setMinTuvLen(int minTuvLen) {
		TMXHandler.minTuvLen = minTuvLen;
	}
	/**
	 * tmx files with ratio (number of 0:1 TUs /total number of TUs) more than minPerce01align (default is 1, i.e. all in) are annotated
	 * @param minPerce01align
	 */
	public void setMinPerce01Align(double minPerce01align) {
		TMXHandler.minPerce01Align = minPerce01align;
	}
	/**
	 * TUs with length (in characters) ratio of TUVs less than minTuLenRatio (default is 0, i.e all in) are annotated 
	 * @param minTuLenRatio
	 */
	public void setMinTuLenRatio(double minTuLenRatio) {
		TMXHandler.minTuLenRatio = minTuLenRatio;
	}
	/**
	 * TUs with length (in characters) ratio of TUVs less than maxTuLenRatio (default is 100, i.e all in) are annotated
	 * @param maxTuLenRatio
	 */
	public void setMaxTuLenRatio(double maxTuLenRatio) {
		TMXHandler.maxTuLenRatio = maxTuLenRatio;
	}
	/**
	 * sets the user defined name of the targeted topic
	 * @param targeteddomain
	 */
	public void setUserTopic(String targeteddomain){
		TMXHandler.usertopic = targeteddomain;
	}
	/**
	 * Loads the default configuration file and checks if user supplied a custom one.
	 * @param type
	 * @param confFile
	 * @return
	 */
	public static CompositeConfiguration getConfig( String confFile) {
		config = new CompositeConfiguration();
		URL default_config = TMXHandler.class.getClassLoader().getResource(DEFAULT_BI_CONFIG_FILE);
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
	public void setSites(List<String> sites) {
		TMXHandler.targ_sites  = sites;
	}
	public void setO1(File o1) {
		TMXHandler.o1 = o1;
	}
	public void setO2(File o2) {
		TMXHandler.o2 = o2;
	}
	public void setMaxSize(int maxSize) {
		TMXHandler.maxSize = maxSize;
	}
	public void setMaxSampleSize(int maxSampleSize) {
		TMXHandler.maxSampleSize = maxSampleSize;
	}
	public void setLangDetector(LangDetector langDetector) {
		TMXHandler.langDetector = langDetector;
	}
	public LangDetector getLangDetector() {
		return langDetector ;
	}
}
