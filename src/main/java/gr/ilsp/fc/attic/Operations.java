package gr.ilsp.fc.attic;

import gr.ilsp.fc.aligner.factory.Aligner;
import gr.ilsp.fc.bitext.PairDetector;
import gr.ilsp.fc.dedup.Deduplicator;
import gr.ilsp.fc.exporter.Exporter;
import gr.ilsp.fc.langdetect.LangDetectUtils;
import gr.ilsp.fc.tmxhandler.TMXHandler;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

public class Operations {
	private static final Logger LOGGER = Logger.getLogger(Operations.class);
	private static final String XML_EXTENSION = ".xml";
	private static final String UNDERSCORE_STR = "_";
	private static final String SEMICOLON_STR=";";
	private static final String HYPHEN_STR="-";
	
	private static String default_dedup_method="0";
	private static int default_dedup_minTokLen=3;
	private static int default_dedup_minParLen=3;
	private static double default_dedup_intersection=0.7;

	private static int[] thres={ 10, 10, 10, 10, 10, 10, 10, 10};

	/**
	 * detects pairs of candidate parallel documents 
	 * @param options
	 * @param operation
	 */

	public static void pairdetection(CrawlOptions options, String operation) {
		LOGGER.info("Running PairDetector");

		List<String> lang_pairs = options.getLangPairs();
		PairDetector pd = new PairDetector();
		for (String lang_pair:lang_pairs){
			pd.setLanguage(lang_pair);
			pd.setSourceDir(options.getInputDir());
			pd.setTargetDir(options.getInputDir());
			pd.setBaseName(options.getBaseName());
			pd.setExcludeSetFiles(null);
			pd.setUseImagepath(options.getImpath());
			pd.setApplyXSLT(options.isOfflineXSLT());
			pd.setURL_REPL(options.getUrlReplaces());
			pd.setMethods(options.getPairMethods());
			pd.setDelFiles(options.getDel());
			pd.pairDetect();
		}
		if (options.getPathReplace() != null){
			List<File> outputFiles =  new ArrayList<File>();
			outputFiles.add(options.getOutputFile());
			outputFiles.add(options.getOutputFileHTML());
			String topDirName = options.getOutputDir().getAbsoluteFile().getParentFile().getParentFile().getAbsolutePath().replace("\\","/");
			Crawl.replaceFilePathsInOutputFiles(options.getOutputDir().getAbsolutePath().replace("\\","/"), outputFiles, topDirName, options.getPathReplace());
		}
	}

	/**
	 * Initialize deduplicator and runs deduplication
	 * @param options
	 * @param operation
	 */
	public static void deduplication(CrawlOptions options) {
		LOGGER.info("Running (near)Deduplicator");
		Deduplicator ded = new Deduplicator();
		ded.setTargetDir(options.getInputDir());
		ded.setBaseName(options.getBaseName());
		ded.setExcludeSetFiles(null);
		ded.setInputType(XML_EXTENSION.substring(1));
		ded.setApplyOfflineXSLT(options.isOfflineXSLT());
		ded.setIntersectionThr(default_dedup_intersection);
		ded.setMIN_TOK_LEN(default_dedup_minTokLen);
		ded.setMIN_PAR_LEN(default_dedup_minParLen);
		ded.setMethod(default_dedup_method);
		ded.nearDedup();
	}

	/**
	 * Initialize exporter and run exporting
	 * @param options
	 * @param operation
	 * @param config
	 */
	public static void exporting(CrawlOptions options, String operation, CompositeConfiguration config) {
		LOGGER.info("Running Exporter");
		LangDetectUtils.loadCybozuLangIdentifier();
		Exporter se = new Exporter();
		se.setMinParLen(options.getMinParLen());
		se.setMinDocLen(options.getMinDocLen());
		se.setTargetLanguages(options.getTargetedLangs());
		se.setTopicFile(options.getTopicFile());
		se.setUserTopic(options.getTargetedDomain());
		se.setInputDir(options.getInputDir());
		se.setBaseName(options.getBaseName());	
		//FIXME
		se.setRunOffLine(false); 
		se.setApplyOfflineXSLT(options.isOfflineXSLT());
		se.setAcceptedMimeTypes( config.getStringArray("fetcher.valid_mime_types.mime_type[@value]"));
		//se.setGenres(options.getGenre());
		//Map<String, String> urlsToIds_empty = new HashMap<String, String>();
		//se.setUrlsToIds(urlsToIds_empty);
		se.export(false);
	}

	/**
	 * Applies alignment on detected document pairs 
	 * @param args
	 */
	public static void alignment(CrawlOptions options,	CompositeConfiguration config) {
		//String[] langs = options.getLanguage().split(SEMICOLON_STR);
		Aligner aligner = null; 
		if (options.toAlign()!=null) {
			aligner = Crawl.prepareAligner(options.toAlign(), options.useDict(), options.pathDict(), options.getTargetedLangs());
			if (aligner==null){
				LOGGER.error("Aligner cannot be initialized:");
				System.exit(0);
			}
		}
		File docpairsFile = options.getOutputFileTMX();
		if (aligner!=null) {
			try {
				File[] docpairs= options.getInputDir().listFiles();
				List<String> cesAlignList = new ArrayList<String>();
				for (File file:docpairs){
					if (file.getName().contains(UNDERSCORE_STR) && file.getName().endsWith(XML_EXTENSION)){
						cesAlignList.add(file.getAbsolutePath());
					}
				}
				FileUtils.writeLines(docpairsFile, cesAlignList);
				//aligner.processCesAlignList(docpairsFile, docpairsFile, options.getOutputFileHTMLTMX(),options.isOfflineXSLT(), options.useISO6393());
				aligner.processCesAlignList(docpairsFile, options.getBaseName().getAbsolutePath(), options.isOfflineXSLT(), options.useISO6393());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * extracts default configuration file and saves it in the user-defined location.  
	 * @param args
	 */
	public static void extractDefaultConfig(String outputFile, String defaultconfig) {
		if ((new File(outputFile)).isDirectory()){
			LOGGER.error(outputFile + " is a directory.");
		}else if ((new File(outputFile)).isFile()){
			URL default_config = Crawl.class.getClassLoader().getResource(defaultconfig);
			LOGGER.error(outputFile + " file already exists. It will be replaced.");
			XMLConfiguration xml;
			try {
				xml = new XMLConfiguration(default_config);
				xml.save(outputFile);
				LOGGER.info("Saved default config file at " + outputFile);
			} catch (ConfigurationException e) {
				// Shouldn't happen
				LOGGER.error("Couldn't save file " + outputFile);
			}			
		}
	}

	/**
	 * selects TMXs that:
	 * 1) are located in setTargetDir,
	 * 2) are generated from document pairs of types (DocTypes) and
	 * 3)  if 0:1 alignments are less than setThres per type. 
	 * Keep only segments that:
	 * 1) are of a type which is included in setSegTypes and
	 * 2) are in the languages
	 * merge them in a new TMX (setOutputTMX) and transformed TMX (xslt)  
	 * @param options
	 * @param operation
	 */
	public static void tmxMerging(CrawlOptions options) {
		LOGGER.info("Running TMXMerger");
		TMXHandler ha = new TMXHandler();
		//ha.setConfig(TMXHandler.getConfig( options.getConfig()));
		ha.setTargetDir(options.getInputDir());
		ha.setApplyOfflineXSLT(options.isOfflineXSLT());
		ha.setDocTypes(options.getDocTypes());
		ha.setSegTypes(options.getSegTypes());
		ha.setThres(thres);
		ha.setMinTuvLen(options.getMinTuvLen());
		ha.setMinPerce01Align(options.getMinPerce01Align());
		ha.setMinTuLenRatio(options.getMinTuLenRatio());
		ha.setMaxTuLenRatio(options.getMaxTuLenRatio());
		ha.KeepTuSameNum(options.keepTuSameNum());
		ha.useISO6393(options.useISO6393());
		ha.setCC(options.getCC());
		ha.setKeepEmpty(options.getKeepEmpty());
		ha.setKeepIdentical(options.getKeepIdentical());
		ha.setKeepDuplicates(options.getKeepDuplicates());
		ha.setClean(options.getClean());
		ha.setUserTopic(options.getTargetedDomain());
		for (String lang_pair:options.getLangPairs()){
			ha.setLanguage(lang_pair);
			String[] temp_langs = lang_pair.split(SEMICOLON_STR);
			String lang = UNDERSCORE_STR+temp_langs[0]+HYPHEN_STR+temp_langs[1];
			ha.setBaseName(new File(options.getBaseName()+lang));
			ha.mergeTMXs();
		}
	}
	
}
