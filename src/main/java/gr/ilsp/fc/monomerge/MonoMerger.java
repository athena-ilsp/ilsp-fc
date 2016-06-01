package gr.ilsp.fc.monomerge;

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

import gr.ilsp.fc.corpora.MonolingualCorpusInformation;
import gr.ilsp.fc.exporter.XSLTransformer;
import gr.ilsp.fc.main.Crawl;
import gr.ilsp.fc.main.ReadResources;
import gr.ilsp.fc.utils.ContentNormalizer;
import gr.ilsp.fc.utils.Eurovoc;
import gr.ilsp.fc.utils.FCStringUtils;
import gr.ilsp.fc.utils.FcFileUtils;
import gr.ilsp.fc.utils.ISOLangCodes;
import gr.ilsp.fc.utils.Statistics;

public class MonoMerger {
	private static final Logger LOGGER = Logger.getLogger(MonoMerger.class);
	private static MonoMergerOptions options = null;
	private static File inputFile = null;
	private static File baseName = null;
	private static File outFile = null;
	private static File outHTML = null;
	private static final String DEFAULT_M_CONFIG_FILE = "FMC_config.xml";
	private static CompositeConfiguration config;
	private static String[] languages;
	private static boolean oxslt=false;
	private static boolean iso6393=false;
	private static boolean cc=false;
	private static boolean metadata=true;
	static Matcher twitterHandleMatcher = Pattern.compile("(^|[^@\\w])@(\\w{1,15})\\b").matcher("");
	private static final String SEMICOLON_STR=";";
	//private final static String PUNCT = ".";
	private final static String UNKNOWN_STR ="unknown";
	private static final String HTML =".html";
	private static final String XMLEXT = ".xml";
	private static final String TXTEXT = ".txt";
	private final static String MetadataExt = ".md.xml";
	private final static String domainNode = "domain";
	private final static String FREE_STR="free";
	private final static String UTF_8 = "UTF-8";
	//private final static String XSL_TMX2HTML = "http://nlp.ilsp.gr/xslt/ilsp-fc/tmx2html.xsl";
	private final static String XSL_TMX2HTML ="http://nlp.ilsp.gr/xslt/ilsp-fc/tmx2html-no-segtype.xsl";

	private String creationDescription = "The ILSP Focused Crawler was used for the acquisition "
			+ "of monolingual data from websites, and for the normalization, cleaning, and deduplication. ";
			
	public static void main(String[] args) {
		MonoMerger mm = new MonoMerger();
		options = new MonoMergerOptions();
		options.parseOptions(args);
		mm.setTargetDir(options.getTargetDir());
		mm.setApplyOfflineXSLT(options.getXSLTransform());
		mm.setLanguage(options.getLanguage());
		mm.useISO6393(options.useISO6393());
		mm.setCC(options.getCC());
		String lang = options.getLanguage().split(SEMICOLON_STR)[0];
		mm.setLanguage(lang);
		mm.setBaseName(new File(options.getBaseName()+lang));
		mm.merge();
	}

	/**
	 * Gets selected TMXs from a directory and adds selected segments of these TMXs in a new TMX file.
	 * The selected TMXs should be extracted from document pairs which have been identified by the methods defined in DocTypes.
	 * The selected TMXs should include less than X% segment pairs of type "0:1", where X is the threshold provided by the user (default is 15) 
	 * The selected segments (to be added) should be of type identified in SegTypes. 
	 * 
	 */
	public void merge() {
		LOGGER.info("------------Constructing a monolingual corpus in "+languages[0]+".------------");
		int totalTokens = 0;
		outFile = new File(baseName.getAbsolutePath()+TXTEXT);
		if (!outFile.getParentFile().exists())
			outFile.getParentFile().mkdirs();

		FilenameFilter filter = new FilenameFilter() {			
			public boolean accept(File arg0, String arg1) {
				return (arg1.endsWith(XMLEXT) & arg1.contains(ISOLangCodes.get3LetterCode(languages[0])));
			}
		};
		if (!iso6393)
			languages[0]=ISOLangCodes.get2LetterCode(languages[0]);
		else
			languages[0]=ISOLangCodes.get3LetterCode(languages[0]);
		List<File> xmlfiles = new ArrayList<File>();
		if (inputFile.isDirectory()){
			xmlfiles = FcFileUtils.listFiles(inputFile, filter,true);
			totalTokens = totalTokens+ReadResources.countToksinDir(inputFile);
		}else{ //it is considered a text file containing a list with full paths of targeted directories (a full path per line)
			List<String> targetdirs;
			try {
				targetdirs = FileUtils.readLines(inputFile);
				for (String targetdir:targetdirs){
					LOGGER.info("finding files from "+ targetdir);
					List<File> tfs = FcFileUtils.listFiles(new File(targetdir), filter,true);
					totalTokens = totalTokens+ReadResources.countToksinDir(new File(targetdir));
					xmlfiles.addAll(tfs);
				}
			} catch (IOException e) {
				LOGGER.error("Problem in reading file "+ inputFile.getAbsolutePath() );
				e.printStackTrace();
			} 
		}
		//creationDescription = creationDescription+filter1+" ; "+filter2+" ; "+filter3+" ; "+filter4+" ; "+filter5+" ; "+filter6+" ; "+filter7+" ; "+filter8+" ; "+filter9;
		List<String> domains = ReadResources.extactValueFromCesDoc(xmlfiles, domainNode);
		List<String> domainEurovocIds=getEurovocId(domains);
		//FIXME
		String domain = StringUtils.join(domains, ',');
		String domainEurovocId = StringUtils.join(domainEurovocIds, ',');

		if (!xmlfiles.isEmpty()){
			String organization = config.getString("resourceCreator.organization");
			String organizationURL = config.getString("resourceCreator.organizationURL"); 
			String projectId= config.getString("fundingProject.projectId"); 
			String projectURL = config.getString("fundingProject.projectURL"); 
			MonolingualCorpusInformation monlingualCorpusInfo; 
			if (cc) {
				monlingualCorpusInfo = new MonolingualCorpusInformation(FilenameUtils.getBaseName(outFile.getAbsolutePath()),MonoMerger.languages[0], totalTokens,
						domain, domainEurovocId, FREE_STR, creationDescription, projectId, projectURL, organization, organizationURL);
			} else {
				monlingualCorpusInfo = new MonolingualCorpusInformation(FilenameUtils.getBaseName(outFile.getAbsolutePath()),MonoMerger.languages[0], totalTokens,
						domain, domainEurovocId, UNKNOWN_STR, creationDescription, projectId, projectURL, organization, organizationURL);
			}
			if (oxslt) 
				outHTML =  new File(baseName.getAbsolutePath() + HTML);
			//generateMergedTMX(outFile, languages, bilingualCorpusInfo, outHTML);

			//if (metadata){
			/*BilingualTmxMetashareDescriptor bilingualTmxMetashareDescriptor = new BilingualTmxMetashareDescriptor(bilingualCorpusInfo);
			File metadataFile = new File(baseName.getAbsolutePath()+ MetadataExt);
			LOGGER.info("Generating metadata descriptor " + metadataFile);
			bilingualTmxMetashareDescriptor.setOutFile(metadataFile);
			if (iso6393) {
				bilingualTmxMetashareDescriptor.setMetadataLang("eng");
			} else {
				bilingualTmxMetashareDescriptor.setMetadataLang("en");
			}
			bilingualTmxMetashareDescriptor.run();*/
			//}
		}else{
			LOGGER.info("No CesDoc found.");
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


/*	*//**
	 * Generates the MergedTMX (outTMX), and its XSLT transformation (outHTML, if asked).
	 * Alignments are in (alignmentList). 
	 * @param outTMX
	 * @param lang1
	 * @param lang2
	 * @param bilingualCorpusInformation
	 * @param outHTML
	 *//*
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
	}*/

	/**
	 * absolute path of the baseName for the outfiles  
	 * @param baseName
	 */
	public void setBaseName(File baseName) {
		MonoMerger.baseName  = baseName;
	}

	/**
	 * absolute path of the directory containing the TMXs to be merged
	 * @param targetDir
	 */
	public void setTargetDir(File inputFile) {
		MonoMerger.inputFile  = inputFile;
	}

	/**
	 * language iso codes separated by ";"
	 * @param languages
	 */
	public void setLanguage(String languages) {
		MonoMerger.languages = languages.split(SEMICOLON_STR);
	}
	public void useISO6393(boolean iso6393) {
		MonoMerger.iso6393 = iso6393;
	}

	/**
	 * apply transformation of the generated TMX to HTML. if exists, an HTML file will be created next to the generated TMX
	 * @param offlineXSLT
	 */
	public void setApplyOfflineXSLT(boolean offlineXSLT) {
		MonoMerger.oxslt  = offlineXSLT;
	}

	public void setCC(boolean cc) {
		MonoMerger.cc  = cc;
	}
	public void setMetadata(boolean metadata) {
		MonoMerger.metadata  = metadata;
	}

	/**
	 * Loads the default configuration file and checks if user supplied a custom one.
	 * @param type
	 * @param confFile
	 * @return
	 */
	public static CompositeConfiguration getConfig( String confFile) {
		config = new CompositeConfiguration();
		URL default_config = Crawl.class.getClassLoader().getResource(DEFAULT_M_CONFIG_FILE);
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
