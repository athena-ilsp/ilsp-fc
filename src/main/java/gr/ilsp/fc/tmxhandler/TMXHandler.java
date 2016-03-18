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

public class TMXHandler {
	private static final Logger LOGGER = Logger.getLogger(TMXHandler.class);
	private static TMXHandlerOptions options = null;
	private static File inputFile = null;
	private static File baseName = null;
	private static File outTMX = null;
	private static File outHTML = null;
	private static final String DEFAULT_BI_CONFIG_FILE = "FBC_config.xml";
	private static CompositeConfiguration config;
	private static String[] languages;
	private static int[] thres;// = { 100,100,100,100,100,100,100};
	private static boolean oxslt=false;
	private static boolean iso6393=false;
	private static boolean cc=false;
	private static boolean metadata=false;
	private static boolean keepsn=false;
	private static int minTuvLen=0;
	private static double minPerce01Align=1;
	private static double minTuLenRatio = 0;
	private static double maxTuLenRatio = 100;
	private static String doctypes;// = "aupidhml";
	private static List<String> segtypes;
	private static Set<String> segs = new HashSet<String>() ;
	static Matcher twitterHandleMatcher = Pattern.compile("(^|[^@\\w])@(\\w{1,15})\\b").matcher("");
	private static final String UNDERSCORE = "_";
	//private final static String PUNCT = ".";
	//private final static String SEP = "-";
	//private final static String UNDERSCORE = "_";
	private static final String SEMI_SEPAR = ";";
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

	private static int totalcounter=0;
	private static int distthr=5; //FIXME add as parameter
	
	public static CompositeConfiguration getConfig() {
		return config;
	}

	public void setConfig(CompositeConfiguration config) {
		TMXHandler.config = config;
	}

	private String alignerStr = "Maligna";

	private String creationDescription = "The ILSP Focused Crawler was used for the acquisition "
			+ "of bilingual data from multilingual websites, and for the normalization, cleaning, deduplication and identification of parallel documents. "
			+ "The " + alignerStr + " sentence aligner was used for extracting segment alignments from crawled parallel documents. "
			+ "As a post-processing step, alignments were merged into one TMX file. The following filters are applied: ";

	private String filter7 = " Alignments with identical TUVs are filtered out.";
	private String filter8 = " Duplicate alignments are filtered out.";


	public static void main(String[] args) {
		TMXHandler ha = new TMXHandler();
		options = new TMXHandlerOptions();
		options.parseOptions(args);
		ha.setTargetDir(options.getTargetDir());
		ha.setConfig(getConfig( options.getConfig()));
		ha.setDocTypes(options.getDocTypes());
		ha.setThres(options.getThres());
		ha.setSegTypes(options.getSegTypes());
		ha.setBaseName(options.getBaseName());
		ha.setApplyOfflineXSLT(options.getXSLTransform());
		ha.setLanguage(options.getLanguage());
		ha.useISO6393(options.useISO6393());
		ha.setMinTuvLen(options.getMinTuvLen());
		ha.setMinPerce01Align(options.getMinPerce01Align());
		ha.setMinTuLenRatio(options.getMinTuLenRatio());
		ha.setMaxTuLenRatio(options.getMaxTuLenRatio());
		ha.KeepTuSameNum(options.keepTuSameNum());
		ha.setCC(options.getCC());
		ha.setMetadata(options.getMetadata());
		ha.mergeTMXs();	
	}

	/**
	 * Gets selected TMXs from a directory and adds selected segments of these TMXs in a new TMX file.
	 * The selected TMXs should be extracted from document pairs which have been identified by the methods defined in DocTypes.
	 * The selected TMXs should include less than X segment pairs of type "0:1", where X is the threshold for the specific DocType 
	 * The selected segments (to be added) should be of type identified in SegTypes. 
	 * 
	 */
	public void mergeTMXs() {
		LOGGER.info("------------Merging of generated TMXs.------------");
		outTMX = new File(baseName.getAbsolutePath()+TMXEXT);
		if (!outTMX.getParentFile().exists())
			outTMX.getParentFile().mkdirs();

		String filter1=" Only TMX files generated from document pairs which have been identified by methods "+ doctypes + " were selected.";
		String filter2=" TMX files in which zeroToOne alignments/total alignments is more than "+ minPerce01Align + " were discarded.";
		String filter3=" Alignments of non-" + segtypes+ " were filtered out";
		String filter4=" Alignments in which a TUV (after normalization) has less than "+ minTuvLen + " tokens are filtered out.";
		String filter5=" Alignments for which the ratio of TUVs' length is less than " + minTuLenRatio+ " or more "+ maxTuLenRatio + " are filtered out.";
		String filter6="";
		if (keepsn)
			filter6=" Alignments for which the TUVs include different digits are filtered out.";
		LOGGER.info(filter1+"\n"+filter2+"\n"+filter3+"\n"+filter4+"\n"+filter5+"\n"+filter6+"\n"+filter7+"\n"+filter8);

		if (!iso6393){
			languages[0]=ISOLangCodes.get2LetterCode(languages[0]);
			languages[1]=ISOLangCodes.get2LetterCode(languages[1]);
		}else{
			languages[0]=ISOLangCodes.get3LetterCode(languages[0]);
			languages[1]=ISOLangCodes.get3LetterCode(languages[1]);			
		}
		List<ILSPAlignment> alignmentList = new ArrayList<ILSPAlignment>();
		FilenameFilter filter = new FilenameFilter() {			
			public boolean accept(File arg0, String arg1) {
				return (arg1.endsWith(TMXEXT));
			}
		};
		String[] types = new String[doctypes.length()];
		for (int ii=0;ii<doctypes.length();ii++){
			types[ii] = UNDERSCORE+Character.toString(doctypes.charAt(ii))+TMXEXT;
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
		creationDescription = creationDescription+filter1+" ; "+filter2+" ; "+filter3+" ; "+filter4+" ; "+filter5+" ; "+filter6+" ; "+filter7+" ; "+filter8;
		List<String> domains = ReadResources.extactValueFromDocPair(tmxfiles, domainNode);
		List<String> domainEurovocIds=getEurovocId(domains);
		//FIXME
		String domain = StringUtils.join(domains, ',');
		String domainEurovocId = StringUtils.join(domainEurovocIds, ',');

		HashMap<String, List<File>> tmxTypeFiles =FcFileUtils.clusterfiles(tmxfiles,doctypes);
		for (int ii=0;ii<doctypes.length();ii++){
			String m= Character.toString(doctypes.charAt(ii));
			alignmentList = addTMXs(tmxTypeFiles.get(m),alignmentList,m, cc);
		}
		if (!alignmentList.isEmpty()){
			int[] stats1 =TMXHandlerUtils.countWordsInTMX(alignmentList,1);
			int[] stats2 =TMXHandlerUtils.countWordsInTMX(alignmentList,2);
			String organization = config.getString("resourceCreator.organization");
			String organizationURL = config.getString("resourceCreator.organizationURL"); 
			String projectId= config.getString("fundingProject.projectId"); 
			String projectURL = config.getString("fundingProject.projectURL"); 
			BilingualCorpusInformation bilingualCorpusInfo;
			if (cc) {
				bilingualCorpusInfo = new BilingualCorpusInformation(FilenameUtils.getBaseName(outTMX.getAbsolutePath()), TMXHandler.languages[0], TMXHandler.languages[1], 
						alignmentList, alignmentList.size(), stats1[0], stats2[0],stats1[1], stats2[1], domain, domainEurovocId, FREE_STR, creationDescription,
						projectId, projectURL, organization, organizationURL);
			} else {
				bilingualCorpusInfo = new BilingualCorpusInformation(FilenameUtils.getBaseName(outTMX.getAbsolutePath()), TMXHandler.languages[0], TMXHandler.languages[1], 
						alignmentList, alignmentList.size(), stats1[0], stats2[0],stats1[1], stats2[1], domain, domainEurovocId, UNKNOWN_STR, creationDescription,
						projectId, projectURL, organization, organizationURL);
			}
			if (oxslt) 
				outHTML =  new File(baseName.getAbsolutePath() + HTML);
			generateMergedTMX(outTMX, languages, bilingualCorpusInfo, outHTML);
			LOGGER.info("Merged TMX at " + outTMX.getAbsolutePath());
			LOGGER.info("Rendering merged TMX as " + outHTML.getAbsolutePath());
			if (metadata){
				BilingualTmxMetashareDescriptor bilingualTmxMetashareDescriptor = new BilingualTmxMetashareDescriptor(bilingualCorpusInfo);
				File metadataFile = new File(baseName.getAbsolutePath()+ MetadataExt);
				LOGGER.info("Generating metadata descriptor " + metadataFile);
				bilingualTmxMetashareDescriptor.setOutFile(metadataFile);
				bilingualTmxMetashareDescriptor.run();
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
	private List<String> getEurovocId(List<String> domains) {
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
	 * Enriches list of Alignments (alignmentList) by examining tmxFiles of specific type (i.e. TMXs based on document pairs detected by specific method)
	 * TUs are excluded if: are identical TUs, have identical TUVs, have an empty TUV, have alignmentType (i.e. 1:2, 2:1, etc) not in "segtypes"   
	 * @param tmxFiles
	 * @param alignmentList
	 * @param type
	 * @return
	 */
	private List<ILSPAlignment> addTMXs(List<File> tmxFiles, List<ILSPAlignment> alignmentList, String type, boolean cc) {
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
					if (!segtypes.contains(segpair.type)){
						continue;
					}
				}
				//FIXME add constrains for length, or other "filters"
				String normS = ContentNormalizer.normtext(segpair.seg1);
				String normT = ContentNormalizer.normtext(segpair.seg2);
				if ( normS.isEmpty() || normT.isEmpty())
					continue;
				if (normS.equals(normT))
					continue;
				if (FCStringUtils.countTokens(normS)<minTuvLen){
					LOGGER.warn("Discard due to toklength of a TUV ");
					LOGGER.warn("\t"+segpair.seg1);
					LOGGER.warn("\t"+ segpair.seg2);
					continue;
				}
				if (FCStringUtils.countTokens(normT)<minTuvLen){
					LOGGER.warn("Discard due to toklength of a TUV ");
					LOGGER.warn("\t"+segpair.seg1);
					LOGGER.warn("\t"+ segpair.seg2);
					continue;
				}
				ratio = (float)segpair.seg1.length()/(float)segpair.seg2.length();
				if (ratio>maxTuLenRatio || ratio < minTuLenRatio){
					LOGGER.warn("Discard due to charlength ratio of TUVs ");
					LOGGER.warn("\t"+segpair.seg1);
					LOGGER.warn("\t"+ segpair.seg2);
					continue;
				}
				if (Statistics.editDist(normS,normT)<distthr){ //FIXME add as parameter, check its influence
					LOGGER.warn("Discard due to high similarity of TUVs ");
					LOGGER.warn("\t"+segpair.seg1);
					LOGGER.warn("\t"+ segpair.seg2);
					continue;
				}
				if (keepsn){
					String num1=segpair.seg1.replaceAll("\\D+","");
					String num2=segpair.seg2.replaceAll("\\D+","");
					if (!num1.equals(num2)){
						//double temp=Statistics.editDist(num1, num2) / (double) Math.min(num1.length(),num2.length());
						//if (temp>0.35){
						LOGGER.warn("Discard due to different numbers in TUVs ");
						LOGGER.warn("\t"+segpair.seg1);
						LOGGER.warn("\t"+ segpair.seg2);
						continue;
						//}
					}
				}
				//FIXME should we check language?	//FIXME keep MD5 instead of string
				String temp = normS+TAB+normT;
				if (!segs.contains(temp)){
					segs.add(temp);
					ILSPAlignment alignment = new ILSPAlignment();
					alignment.addSourceSegment(segpair.seg1);
					alignment.addTargetSegment(segpair.seg2);
					alignment.setScore((float)segpair.score);
					alignment.setSite(segpair.site);
					alignment.setMethod(segpair.method);
					alignment.setLicense(segpair.license);
					alignment.setType(segpair.type);
					//float ratio = (float)segpair.seg1.length()/(float)segpair.seg2.length();
					alignment.setLengthRatio(Float.toString(ratio));
					alignmentList.add(alignment);
				}
			}
		}
		LOGGER.info("NumofValid/UniqueAlignments: "+alignmentList.size()+"\t"+"totalNumofSegmentPairs: "+totalcounter);
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
		TMXHandler.languages = languages.split(SEMI_SEPAR);
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
	public void setMetadata(boolean metadata) {
		TMXHandler.metadata  = metadata;
	}

	public void KeepTuSameNum(boolean keep){
		TMXHandler.keepsn=keep;
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
}
