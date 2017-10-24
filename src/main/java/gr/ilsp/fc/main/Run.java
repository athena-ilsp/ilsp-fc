package gr.ilsp.fc.main;

import gr.ilsp.fc.aligner.factory.Aligner;
import gr.ilsp.fc.aligner.factory.AlignerFactory;
import gr.ilsp.fc.bitext.BitextUtils;
import gr.ilsp.fc.bitext.BitextsTranslationLinks;
import gr.ilsp.fc.bitext.PairDetector;
import gr.ilsp.fc.crawl.Crawler;
import gr.ilsp.fc.crawl.CrawlerO;
import gr.ilsp.fc.crawl.CrawlerOptions;
import gr.ilsp.fc.dedup.Deduplicator;
import gr.ilsp.fc.exporter.Exporter;
//import gr.ilsp.fc.langdetect.LangDetectUtils;
import gr.ilsp.fc.monomerge.MonoMerger;
import gr.ilsp.fc.tmxhandler.TMXHandler;
import gr.ilsp.fc.utils.sentencesplitters.SentenceSplitterFactory;
import gr.ilsp.nlp.commons.Constants;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

public class Run {
	private static final Logger LOGGER = Logger.getLogger(Run.class);
	private static final String xml_type="xml";
	private static final String CRAWL_operation = "crawl", EXPORT_operation = "export", DEDUP_operation = "dedup", PAIRDETECT_operation = "pairdetect";
	private static final String ALIGN_operation = "align", TMX_MERGE_operation = "tmxmerge", MONO_MERGE_operation = "monomerge";
	private static final String XMLlist = ".xmllist.txt";

	private static int[] thres={ 100, 100, 100, 100, 100, 100, 100, 100};

	public static void main(String[] args) {
		LOGGER.info(Arrays.toString(args));
		RunOptions run_options = new RunOptions();
		run_options.parseOptions(args);	

		String operations=run_options.getOperation(); 
		boolean crawl= false, export=false, dedup= false, pairdetect=false, align=false, tmxmerge=false, monomerge=false;

		CrawlerO cro=new CrawlerO();
		Map<String, String> uRLPairsFromTranslationLinks = new HashMap<String, String>();	//URL pairs identified by translation links
		Map<String, String> idPairsFromTranslationLinks = new HashMap<String, String>();	//ids of files participated into document pairs due to translation links  

		Exporter exp = new Exporter();
		Map<String,Integer> numInLangMap = new HashMap<String,Integer>();		//number of documents in each language
		Map<String,Integer> numInLangPairMap = new HashMap<String,Integer>();	//number of document pairs in each language pair
		Map<String, String> urlsToIds = new HashMap<String,String>();			//map of URLs to ids
		Set<String> filesinPairs = null ;  										//cesDoc files in document pairs identified by translation links

		Deduplicator ded = new Deduplicator();

		PairDetector pd = new PairDetector();

		TMXHandler tm = new TMXHandler();

		MonoMerger mm = new MonoMerger();

		//Parses CrawlerOprions, creates a Crawler Object, and runs the Crawl Job.
		//Creates a specific file structure "user-defined directory/crawl directory/job directory" in which the data acquired in each crawl cycle is stored in directories.
		if (operations.contains(CRAWL_operation)){
			LOGGER.info("---------------------------------------------------");
			LOGGER.info("------------------Running Crawler------------------");
			LOGGER.info("---------------------------------------------------");
			CrawlerOptions cr_options = getCrawlerOptions(run_options);
			cro=Crawler.crawl(cr_options);
			uRLPairsFromTranslationLinks = cro.getURLPairsFromTranslationLinks();
			crawl=true;
		}

		//Parses the "crawl directory" and generates CesDoc Files in "crawl directory/xml" for each stored web document
		//In case the input does not come from crawls (i.e. offline), extract text and metadata from the files in the input directory
		//Generates a text file (and its transformation to HTML if asked) with a list  of paths of the generated cesDoc files.
		if (operations.contains(EXPORT_operation)){
			LOGGER.info("---------------------------------------------------");
			LOGGER.info("------------------Running Exporter-----------------");
			LOGGER.info("---------------------------------------------------");
			exp.setMinParLen(run_options.getMinParLen());
			exp.setMinDocLen(run_options.getMinDocLen());
			exp.setDepth(run_options.upToDepth());
			exp.setTargetLanguages(run_options.getTargetedLangs());
			exp.setTopicFile(run_options.getTopicFile());
			exp.setNegWordsFile(run_options.getNegwordsFile());
			exp.setBaseName(run_options.getBaseName());
			exp.setUserTopic(run_options.getUserTopic());
			//exp.setGenre(run_options.getGenre());
			exp.setRunOffLine(run_options.getRunOffLine());
			exp.setTextExport(run_options.getTextExport());
			exp.setApplyOfflineXSLT(run_options.isOfflineXSLT());
			//exp.set_paths_repl(run_options.get_paths_repl());
			if (crawl){
				LOGGER.info(cro.getOutputDir());
				exp.setInputDir(cro.getOutputDir());
				exp.setOutputDir(new File(FilenameUtils.concat(cro.getOutputDir().getAbsolutePath(), xml_type)).getAbsoluteFile());
				exp.setAcceptedMimeTypes(cro.getValidMimes().toArray(new String[cro.getValidMimes().size()]));
				exp.setConfiguration(cro.getConfiguration());
				exp.setLangDetector(cro.getLangDetector());
			}else{
				exp.setConfiguration(Exporter.getConfig(run_options.getConfig()));
				exp.setInputDir(run_options.getInputDir());
				exp.setOutputDir(run_options.getOutputDir());
				exp.setAcceptedMimeTypes(exp.getConfiguration().getStringArray("fetcher.valid_mime_types.mime_type[@value]"));
				//LangDetectUtils.loadCybozuLangIdentifier();
				exp.setLangDetector(run_options.getLangDetector());
			}
			numInLangMap = exp.export(false);
			urlsToIds = exp.getUrlsToIds();
			idPairsFromTranslationLinks = BitextsTranslationLinks.getIdPairsFromTranslationLinks(uRLPairsFromTranslationLinks, urlsToIds);
			filesinPairs = BitextUtils.getDocsinPairs(idPairsFromTranslationLinks);
			export=true;
		}

		//Goes through the targeted directory ("crawl directory/xml"), processes the cesDoc files, detects (near) duplicates and keeps only the longest of each group of duplicates.
		//Generates a text file (and its transformation to HTML if asked) with a list of paths of the remaining cesDoc files
		if (operations.contains(DEDUP_operation)){
			LOGGER.info("---------------------------------------------------");
			LOGGER.info("-------------Running (near)Deduplicator------------");
			LOGGER.info("---------------------------------------------------");
			if (export)
				ded.setTargetDir(exp.getOutputDir());
			else
				ded.setTargetDir(run_options.getInputDir());
			ded.setInputType(xml_type);
			ded.setBaseName(run_options.getBaseName());
			ded.setExcludeSetFiles(filesinPairs); //DOCS THAT ARE INCLUDED IN hreflangURLPairs ARE NOT REMOVED
			ded.setApplyOfflineXSLT(run_options.isOfflineXSLT());
			ded.setIntersectionThr(run_options.getDedupInterThr());
			ded.setMIN_TOK_LEN(run_options.getDedupMinTokLen());
			ded.setMIN_PAR_LEN(run_options.getDedupMinParLen());
			ded.setMethod(run_options.getDedupMethod());
			ded.nearDedup();
			dedup=true;	
		}

		//Identifies pairs of cesDoc files for a language pair.
		//In each document pair, the two documents are candidate translations of each other. 
		//Generates a cesAlign file (and its transformation to HTML if asked) for each identified pair.
		//Writes a text file (and its transformation to HTML if asked) containing a list of path of the generated cesAlign files.
		if (operations.contains(PAIRDETECT_operation)){
			LOGGER.info("---------------------------------------------------");
			LOGGER.info("---------------Running PairDetector----------------");
			LOGGER.info("---------------------------------------------------");
			List<String> langpairs = run_options.getLangPairs();
			for (String langpair:langpairs){
				if (export){ //then there is information about number of available documents in each targeted language //FIXME it is probable that numbers have been changed due to (near)deduplication
					boolean stop = noDocsForLangPair(numInLangMap, langpair.split(Constants.SEMICOLON));
					if (stop){
						numInLangPairMap.put(langpair,0);
						LOGGER.info("No pairs for "+ langpair.replaceAll(Constants.SEMICOLON, Constants.HYPHEN));
						continue;
					}
				}
				pd.setLanguage(langpair);
				//source and target directories are the same (i.e. cesAlign files are stored next to the cesDoc files)
				if (export){
					pd.setSourceDir(exp.getOutputDir());
					pd.setTargetDir(exp.getOutputDir());
				}else{
					if (dedup){
						pd.setSourceDir(ded.getTargetDir());
						pd.setTargetDir(ded.getTargetDir());
					}else{
						pd.setSourceDir(run_options.getInputDir());
						pd.setTargetDir(run_options.getInputDir());
					}
				}
				pd.setBaseName(run_options.getBaseName());
				if (crawl && export)
					pd.setExcludeSetFiles(idPairsFromTranslationLinks);
				else	
					pd.setExcludeSetFiles(null);
				pd.setUseImagepath(run_options.getImpath());
				pd.setApplyXSLT(run_options.isOfflineXSLT());
				pd.setURL_REPL(run_options.getUrlReplaces());
				pd.setMethods(run_options.getPairMethods());
				pd.setDelFiles(run_options.getDel());
				int numOfPairs=pd.pairDetect();
				numInLangPairMap.put(langpair, numOfPairs);
			}
			pairdetect=true;
		}
		// 1)	prepares Aligner for the targeted language pair,
		// 2)	reads a text file (if available) containing the list of paths of cesAlign files,
		//		or "goes through" the input directory and generates a list  of paths of cesAlign files concerning the targeted language pair
		// 3)	generates a TMX file for each cesAlign file
		// 4)	generates a text file (and its transformation to HTML if asked) with a list  of paths of the TMXs
		if (operations.contains(ALIGN_operation)){
			LOGGER.info("---------------------------------------------------");
			LOGGER.info("-----------------Running Aligner-------------------");
			LOGGER.info("---------------------------------------------------");
			CompositeConfiguration configuration=null;
			if (export)
				configuration = exp.getConfiguration();
			if (configuration==null)
				configuration = AlignerFactory.getConfig(run_options.getConfig());
			Aligner aligner = null; 
			List<String> langpairs = run_options.getLangPairs();
			for (String langpair:langpairs){
				String[] langs = langpair.split(Constants.SEMICOLON);
				LOGGER.info("------------Alignment of segments in "+ langs[0]+Constants.HYPHEN+langs[1]+".------------");
				if (pairdetect){ //then there is information about the number of document pairs (cesAlign files) per language pair
					boolean stop = noDocPairsForLangPair(numInLangPairMap, langpair);
					if (stop)
						continue;
				}
				aligner = AlignerFactory.prepareAligner(configuration, run_options.toAlign(), run_options.useDict(), run_options.pathDict(), langs);
				if (aligner!=null){
					String langext = Constants.UNDERSCORE+langs[0]+Constants.HYPHEN+langs[1];
					File outTextList = null;
					if (pairdetect)
						outTextList = new File(run_options.getBaseName()+langext+XMLlist);
					else{
						outTextList = run_options.getInputDir(); //this should not be a directory but a textfile with paths to cesAlign files.
						if (outTextList.isDirectory()){
							try {
								List<String> cesAlignPaths = aligner.getCesAlignPaths(outTextList, langs);
								if (!cesAlignPaths.isEmpty()){
									FileUtils.writeLines(new File(FilenameUtils.concat(outTextList.getAbsolutePath(),run_options.getAgentName()+Constants.UNDERSCORE+langext+XMLlist)), cesAlignPaths);
									//outTextList = new File(outTextList+langext+XMLlist);
									outTextList = new File(FilenameUtils.concat(outTextList.getAbsolutePath(),run_options.getAgentName()+Constants.UNDERSCORE+langext+XMLlist));
								}else{
									LOGGER.info("no cesAlign files for "+ langs[0]+Constants.HYPHEN+langs[1]);
									continue;
								}
							} catch (IOException e) {
								LOGGER.error("problem in writing the list of cseAlign files");
								e.printStackTrace();
							}
						}
					}
					aligner.processCesAlignList(outTextList, run_options.getBaseName().getAbsolutePath(), run_options.isOfflineXSLT(), run_options.useISO6393());
				}
			}
			align = true;
		}

		//constructs parallel corpora 
		if (operations.contains(TMX_MERGE_operation)){
			LOGGER.info("---------------------------------------------------");
			LOGGER.info("---------------Running TMXMerger-------------------");
			LOGGER.info("---------------------------------------------------");
			//FIXME: get config from previous steps
			tm.setConfig(TMXHandler.getConfig(run_options.getConfig()));
			tm.setApplyOfflineXSLT(run_options.isOfflineXSLT());
			tm.setDocTypes(run_options.getDocTypes());
			tm.setSegTypes(run_options.getSegTypes());
			tm.setMinTuvLen(run_options.getMinTuvLen());
			tm.setMinPerce01Align(run_options.getMinPerce01Align());
			tm.setMinPerceM1Align(run_options.getMinPerceM1Align());
			tm.setMinTuLenRatio(run_options.getMinTuLenRatio());
			tm.setMaxTuLenRatio(run_options.getMaxTuLenRatio());
			tm.KeepTuSameNum(run_options.keepTuSameNum());
			tm.setKeepEmpty(run_options.getKeepEmpty());
			tm.setKeepIdentical(run_options.getKeepIdentical());
			tm.setKeepDuplicates(run_options.getKeepDuplicates());
			tm.setClean(run_options.getClean());
			tm.setSameSymb(run_options.getSameSymb());
			tm.setMaxSize(run_options.getMaxSize());
			tm.setThres(thres);
			tm.setCC(run_options.getCC());
			tm.useISO6393(run_options.useISO6393());
			tm.setUserTopic(run_options.getUserTopic());
			tm.setMaxSampleSize(run_options.getMaxSampleSize());
			if (pairdetect)
				tm.setTargetDir(pd.getTargetDir());
			else
				tm.setTargetDir(run_options.getInputDir());
			if (export)
				tm.setLangDetector(exp.getLangDetector());
			else
				tm.setLangDetector(run_options.getLangDetector());
			
			if (tm.getTargetDir()!=null){
				List<String> langpairs = run_options.getLangPairs();
				for (String langpair:langpairs){
					tm.setLanguage(langpair);
					String[] temp_langs = langpair.split(Constants.SEMICOLON);
					String lang = Constants.UNDERSCORE+temp_langs[0]+Constants.HYPHEN+temp_langs[1];
					tm.setBaseName(new File(run_options.getBaseName()+lang));
					tm.mergeTMXs();
				}
			}else
				LOGGER.warn("No targeted directory");
			tmxmerge=true;
		}
		//constructs monolingual corpora 
		if (operations.contains(MONO_MERGE_operation)){
			LOGGER.info("---------------------------------------------------");
			LOGGER.info("----------------Running MonoMerger-----------------");
			LOGGER.info("---------------------------------------------------");
			mm.setConfiguration(MonoMerger.getConfig(run_options.getConfig()));
			if (export)
				mm.setTargetDir(exp.getOutputDir());
			else
				mm.setTargetDir(run_options.getInputDir());
			mm.setCC(run_options.getCC());
			mm.setUserTopic(run_options.getUserTopic());
			SentenceSplitterFactory sentenceSplitterFactory = new SentenceSplitterFactory();
			mm.setCorpusLevel(run_options.getCorpusLevel());
			//mm.setSites(run_options.getSites());
			mm.setLanguage(run_options.getLanguage());
			String[] languages = mm.getLanguage().split(Constants.SEMICOLON);
			for (String lang:languages){
				mm.setLanguage(lang);
				mm.setBaseName(new File(run_options.getBaseName()+Constants.UNDERSCORE+lang));
				mm.setSentenceSplitter(sentenceSplitterFactory.getSentenceSplitter(lang));
				mm.merge();
			}
			monomerge=true;
		}
	}

	/**
	 * checks if there are cesAlign files for a language pair
	 * @param numInLangPairMap
	 * @param langpair
	 * @return
	 */
	private static boolean noDocPairsForLangPair(Map<String, Integer> numInLangPairMap, String langpair) {
		if (numInLangPairMap.containsKey(langpair)){
			if (numInLangPairMap.get(langpair)==0){
				LOGGER.info("No pairs in "+ langpair.replaceAll(Constants.SEMICOLON, Constants.HYPHEN));
				return true;
			}
		}
		return false;
	}
	/**
	 * checks if there are cesDoc files in both of the targeted languages
	 * @param numInLangMap
	 * @param langs
	 * @return
	 */
	private static boolean noDocsForLangPair(Map<String, Integer> numInLangMap,	String[] langs) {
		for (String lang:langs){
			if (numInLangMap.get(lang)==0){
				LOGGER.info("No documents in "+ lang);
				return true;
			}
		}
		return false;
	}

	/**
	 * keeps the subset of RunOptions that concern CrawlerOptions 
	 * @param run_options
	 * @return
	 */

	private static CrawlerOptions getCrawlerOptions(RunOptions run_options) {
		CrawlerOptions cr_options = new CrawlerOptions();

		cr_options.set_debug(run_options.isDebug());
		cr_options.set_loggingAppender(run_options.getLoggingAppender());
		cr_options.set_config(run_options.getConfig());
		//cr_options.set_configuration(run_options.getConfiguration());

		cr_options.set_force(run_options.Force());
		cr_options.set_keepBoiler(run_options.keepBoiler());
		cr_options.set_threads(run_options.getThreads());
		cr_options.set_agentName(run_options.getAgentName());
		cr_options.set_crawlDuration(run_options.getCrawlDuration());
		cr_options.set_numLoops(run_options.getNumLoops());
		cr_options.set_type(run_options.getType());
		cr_options.set_webdomain(run_options.getWebDomain());
		cr_options.set_webmaindomain(run_options.getWebMainDomain());
		cr_options.set_filter(run_options.getFilter());
		cr_options.set_storefilter(run_options.getStoreFilter());
		cr_options.set_level(run_options.getCrawlLevel());
		cr_options.set_depth(run_options.upToDepth());

		cr_options.set_seedFile(run_options.getSeedFile());

		cr_options.set_minDocLen(run_options.getMinDocLen());
		//cr_options.set_minParLen(run_options.getMinParLen());

		cr_options.set_targetedLangs(run_options.getTargetedLangs());
		cr_options.set_LangDetector(run_options.getLangDetector());
		cr_options.set_langKeys(run_options.getLangKeys());
		cr_options.set_mapLangs(run_options.getMapLangs());
		cr_options.set_linkAttrs(run_options.getTransLinksAttrs());
		//cr_options.set_iso6393(run_options.useISO6393());

		cr_options.set_outputDir(run_options.getOutputDir());

		cr_options.set_topicFile(run_options.getTopicFile());
		cr_options.set_usertopic(run_options.getUserTopic());

		cr_options.set_genre(run_options.getGenre());

		return cr_options;
	}


	/**
	 * Loads Aligner, and checks segmenters for targeted languages, if there is no available aligner, returns null.
	 * Supported aligners are "maligna" (default) and "hunalign"
	 * @param configuration 
	 * @param toAlign 	aligner Name
	 * @param useDict	dictionary name
	 * @param pathDict	dictionary path
	 * @param langs		targeted languages
	 * @return
	 *//*
	public static Aligner prepareAligner(CompositeConfiguration configuration, String toAlign, String useDict, String pathDict, String[] langs) {
		AlignerFactory alignerFactory = new AlignerFactory();
		Aligner aligner = alignerFactory.getAligner(toAlign);
		if (aligner!= null) {
			Properties properties = new Properties();
			if (toAlign.matches("hunalign")){
				String hunpath=Aligner.getRunningJarPath();
				String prop=System.getProperty("os.name").toLowerCase();
				String aligner_runnable_path = null;
				if (prop.equals("linux")) 
					aligner_runnable_path = FilenameUtils.concat(hunpath, configuration.getString("aligner.lin_align_path.value"));
				else if(prop.startsWith("windows")) 
					aligner_runnable_path = FilenameUtils.concat(hunpath, configuration.getString("aligner.win_align_path.value"));

				String usedict = useDict;
				String dictalign_path=null;
				if (toAlign.equals("hunalign")){
					if (usedict!=null && !usedict.equals("default")) 
						dictalign_path=pathDict;
					else 
						dictalign_path = FilenameUtils.concat(hunpath, configuration.getString("aligner.align_dict.value"));
				} else 
					dictalign_path = pathDict;
				properties.setProperty("aligner_runnable_path", aligner_runnable_path);
				if (dictalign_path!=null) 
					properties.setProperty("dictalign_path", dictalign_path);
			}
			properties.setProperty("sourceLang", ISOLangCodes.get3LetterCode(langs[0]));
			properties.setProperty("targetLang", ISOLangCodes.get3LetterCode(langs[1]));
			try {
				aligner.initialize(properties);
			} catch (Exception ex) {
				//ex.printStackTrace();
				return null; 
			}
		}
		return aligner;
	}
	  */


	/*	private static ExporterOptions getExporterOptions(RunOptions run_options) {
	ExporterOptions ex_options = new ExporterOptions();

	//ex_options.set_debug(run_options.isDebug());
	//ex_options.set_loggingAppender(run_options.getLoggingAppender());
	ex_options.set_config(run_options.getConfig());
	ex_options.set_configuration(run_options.getConfiguration());

	ex_options.set_depth(run_options.upToDepth());

	ex_options.set_minDocLen(run_options.getMinDocLen());
	ex_options.set_minParLen(run_options.getMinParLen());

	ex_options.set_targetedLangs(run_options.getTargetedLangs());
	//ex_options.set_iso6393(run_options.useISO6393());

	ex_options.set_outputDir(run_options.getOutputDir());
	ex_options.set_outBaseName(run_options.getBaseName());

	ex_options.set_topicFile(run_options.getTopicFile());
	ex_options.set_usertopic(run_options.getUserTopic());
	ex_options.set_negwordsFile(run_options.getNegwordsFile());

	ex_options.set_genre(run_options.getGenre());
	ex_options.set_offlineXSLT(run_options.isOfflineXSLT());
	ex_options.set_runoffline(run_options.getRunOffLine());
	ex_options.set_paths_repl(run_options.get_paths_repl());

	public File get_inputdir() {
		return _inputdir;
	}

	return ex_options;
}*/

	/*	*//**
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
	 *//*
	public static void tmxMerging(RunOptions options) {
		LOGGER.info("Running TMXMerger");
		TMXHandler ha = new TMXHandler();
		ha.setConfig(TMXHandler.getConfig( options.getConfig()));
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
		ha.setTargetedDomain(options.getUserTopic());
		for (String lang_pair:options.getLangPairs()){
			ha.setLanguage(lang_pair);
			String[] temp_langs = lang_pair.split(SEMICOLON_STR);
			String lang = UNDERSCORE_STR+temp_langs[0]+HYPHEN_STR+temp_langs[1];
			ha.setBaseName(new File(options.getBaseName()+lang));
			ha.mergeTMXs();
		}
	}
	  */



	/**
	 * extracts default configuration file and saves it in the user-defined location.  
	 * @param args
	 *//*
	public static void extractDefaultConfig(String outputFile, String defaultconfig) {
		if ((new File(outputFile)).isDirectory()){
			LOGGER.error(outputFile + " is a directory.");
		}else if ((new File(outputFile)).isFile()){
			URL default_config = Run.class.getClassLoader().getResource(defaultconfig);
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
	}*/




	/*if (operations.contains(TMX_MERGE_operation))
		tmxMerging(options);*/


	/*public String getPairMethods() {
	return _methods;
}
public String toAlign() {
	return _aligner;
}
public String useDict() {
	return _dict;
}
public String pathDict() {
	return _dictpath;
}*/



	/*public int getMinTuvLen() {
	return _minTuvLen;
}
public double getMinPerce01Align() {
	return _minPerce01Align;
}
public double getMaxTuLenRatio() {
	return _maxTULenRatio;
}
public double getMinTuLenRatio() {
	return _minTULenRatio;
}
public boolean keepTuSameNum() {
	return _keepsn;
}
	 */

	/*public String[][] getUrlReplaces() {
	return _urls_repls;
}
public String getPathReplace() {
	return _paths_repl;
}*/
	//public String getDesc() {
	//	return _descr;
	//}
	/*public List<String> getSegTypes() {
	return _selectSegs;
}
public String getDocTypes() {
	return _selectDocs;
}
public boolean getImpath() {
	return _keepimagefp;
}
public boolean isOfflineXSLT() {
	return _offlineXSLT;
}*/



	/*public boolean getCC() {
	return _cc;
}
public boolean getKeepEmpty() {
	return _keepem;
}
public boolean getKeepIdentical() {
	return _keepiden;
}
public boolean getKeepDuplicates() {
	return _keepdup;
}
public boolean getClean() {
	return _clean;
}*/
	/*public File getO1() {
	return _o1;
}
public File getO2() {
	return _o2;
}*/

}
