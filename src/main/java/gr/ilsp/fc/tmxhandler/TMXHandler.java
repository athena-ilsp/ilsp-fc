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
import gr.ilsp.fc.utils.FcFileUtils;

public class TMXHandler {
	private static final Logger LOGGER = Logger.getLogger(TMXHandler.class);
	private static TMXHandlerOptions options = null;
	private static File inputFile = null;
	private static File outTMX = null;
	private static File outHTML = null;
	private static final String DEFAULT_BI_CONFIG_FILE = "FBC_config.xml";
	private static CompositeConfiguration config;
	private static String[] languages;
	private static int[] thres;// = { 100,100,100,100,100,100,100};
	private static boolean xslt=false;
	private static boolean cc=false;
	private static boolean metadata=false;
	private static String doctypes;// = "auidhml";
	
	private static List<String> segtypes;// = new ArrayList<String>();
	private static Set<String> segs = new HashSet<String>() ;
	static Matcher twitterHandleMatcher = Pattern.compile("(^|[^@\\w])@(\\w{1,15})\\b").matcher("");
	//private final static String TMX = "tmx";
	//private final static String MERGED = ".merged";
	private static final String HTML =".html";
	private static final String TMX_EXTENSION = ".tmx";
	private static final String UNDERSCORE = "_";
	//private final static String PUNCT = ".";
	//private final static String UNDERSCORE = "_";
	private final static String UNKNOWN_STR ="unknown";
	private final static String MetadataExt = ".md.xml";
	private final static String domainNode = "domain";
	private final static String FREE_STR="free";
	//private final static String SEP = "-";
	private final static String TAB = "\t";
	private final static String UTF_8 = "UTF-8";
	//private final static String XSL_TMX2HTML = "http://nlp.ilsp.gr/xslt/ilsp-fc/tmx2html.xsl";
	private final static String XSL_TMX2HTML ="http://nlp.ilsp.gr/xslt/ilsp-fc/tmx2html-no-segtype.xsl";
	/*private final static String a_type = "a";
	private final static String p_type = "p";
	private final static String u_type = "u";
	private final static String i_type = "i";
	private final static String d_type = "d";
	private final static String h_type = "h";
	private final static String m_type = "m";
	private final static String l_type = "l";*/
	private static final String SEMI_SEPAR = ";";
	private final static int length_THR = 5;
	private static int totalcounter=0;

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
			+ "As a post-processing step, alignments were merged into one TMX file and non-1:1 alignments were filtered out.";


	public static void main(String[] args) {
		TMXHandler ha = new TMXHandler();
		options = new TMXHandlerOptions();
		options.parseOptions(args);
		ha.setTargetDir(options.getTargetDir());
		ha.setConfig(getConfig( options.getConfig()));
		ha.setDocTypes(options.getDocTypes());
		ha.setThres(options.getThres());
		ha.setSegTypes(options.getSegTypes());
		ha.setOutputTMX(options.getOutTMX());
		ha.setApplyOfflineXSLT(options.getXSLTransform());
		ha.setLanguage(options.getLanguage());
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

		List<ILSPAlignment> alignmentList = new ArrayList<ILSPAlignment>();
		FilenameFilter filter = new FilenameFilter() {			
			public boolean accept(File arg0, String arg1) {
				return (arg1.endsWith(TMX_EXTENSION));
			}
		};
		String[] types = new String[doctypes.length()];
		for (int ii=0;ii<doctypes.length();ii++){
			types[ii] = UNDERSCORE+Character.toString(doctypes.charAt(ii))+TMX_EXTENSION;
		}
		List<File> tmxfiles = new ArrayList<File>();
		if (inputFile.isDirectory()){
			List<File> tfs = FcFileUtils.listFiles(inputFile, filter,true);
			tmxfiles = TMXHandlerUtils.selectTypes(tfs, types);
		}else{ //it is considered a text file containing a list with full paths of targeted directories (a full path per line)
			List<String> targetdirs;
			try {
				targetdirs = FileUtils.readLines(inputFile);
				for (String targetdir:targetdirs){
					LOGGER.info("finding files from "+ targetdir);
					List<File> tfs = FcFileUtils.listFiles(new File(targetdir), filter,true);
					tmxfiles.addAll(TMXHandlerUtils.selectTypes(tfs, types));
				}
			} catch (IOException e) {
				LOGGER.error("Problem in reading file "+ inputFile.getAbsolutePath() );
				e.printStackTrace();
			} 
		}

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
		/*if (doctypes.contains(a_type))
			alignmentList = addTMXs(tmxTypeFiles.get(a_type),alignmentList,a_type, cc);
		if (doctypes.contains(p_type))
			alignmentList = addTMXs(tmxTypeFiles.get(p_type),alignmentList,p_type, cc);
		if (doctypes.contains(i_type))
			alignmentList = addTMXs(tmxTypeFiles.get(i_type),alignmentList,i_type, cc);
		if (doctypes.contains(d_type))
			alignmentList = addTMXs(tmxTypeFiles.get(d_type),alignmentList,d_type, cc);
		if (doctypes.contains(u_type))
			alignmentList = addTMXs(tmxTypeFiles.get(u_type),alignmentList,u_type, cc);
		if (doctypes.contains(h_type))
			alignmentList = addTMXs(tmxTypeFiles.get(h_type),alignmentList,h_type, cc);
		if (doctypes.contains(m_type))
			alignmentList = addTMXs(tmxTypeFiles.get(m_type),alignmentList,m_type, cc);
		if (doctypes.contains(l_type))
			alignmentList = addTMXs(tmxTypeFiles.get(l_type),alignmentList,l_type, cc);*/

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
			if (xslt) { 
				outHTML =  new File(FilenameUtils.concat(outTMX.getParent(),  FilenameUtils.getBaseName(outTMX.getAbsolutePath()) + HTML));
				LOGGER.info("Rendering merged TMX as " + outHTML);
			}
			generateMergedTMX(outTMX, languages, bilingualCorpusInfo, outHTML);

			if (metadata){
				BilingualTmxMetashareDescriptor bilingualTmxMetashareDescriptor = new BilingualTmxMetashareDescriptor(bilingualCorpusInfo);
				File metadataFile = new File(FilenameUtils.concat(outTMX.getParent(),  FilenameUtils.getBaseName(outTMX.getAbsolutePath()) + MetadataExt));
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
			List<SegPair> segpairs = TMXHandlerUtils.getTUsFromTMX(tmxFile,thr, languages[0], languages[1], cc);
			if (segpairs==null){
				LOGGER.info("Cut due to many 0:1 alignments: " +tmxFile.getAbsolutePath());
				continue;
			}
			totalcounter=totalcounter+segpairs.size();
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
				if (ReadResources.countTokens(normS)<length_THR)
					continue;
				if (ReadResources.countTokens(normT)<length_THR)
					continue;
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
					float ratio = (float)segpair.seg1.length()/(float)segpair.seg2.length();
					alignment.setLengthRatio(Float.toString(ratio));
					alignmentList.add(alignment);
				}
			}
		}
		LOGGER.info("NumofValid/UniqueAlignments: "+alignmentList.size()+"\t"+"totalNumofSegmentPairs: "+totalcounter);
		return alignmentList;
	}


	/**
	 * absolute path of the TMX file that will contain all TMXs 
	 * @param outTMX
	 */
	public void setOutputTMX(File outTMX) {
		TMXHandler.outTMX  = outTMX;
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


	/**
	 * apply transformation of the generated TMX to HTML. if exists, an HTML file will be created next to the generated TMX
	 * @param offlineXSLT
	 */
	public void setApplyOfflineXSLT(boolean offlineXSLT) {
		TMXHandler.xslt  = offlineXSLT;
	}

	public void setCC(boolean cc) {
		TMXHandler.cc  = cc;
	}
	public void setMetadata(boolean metadata) {
		TMXHandler.metadata  = metadata;
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
