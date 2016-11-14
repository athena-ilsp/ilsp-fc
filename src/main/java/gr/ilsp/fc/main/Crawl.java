package gr.ilsp.fc.main;

import gr.ilsp.fc.aligner.factory.Aligner;
import gr.ilsp.fc.aligner.factory.AlignerFactory;
import gr.ilsp.fc.bitext.BitextUtils;
import gr.ilsp.fc.bitext.BitextsTranslationLinks;
import gr.ilsp.fc.bitext.PairDetector;
import gr.ilsp.fc.datums.CrawlDbDatum;
import gr.ilsp.fc.dedup.Deduplicator;
import gr.ilsp.fc.exporter.Exporter;
import gr.ilsp.fc.langdetect.LangDetectUtils;
import gr.ilsp.fc.monomerge.MonoMerger;
import gr.ilsp.fc.operations.ILSPFCUrlNormalizer;
import gr.ilsp.fc.parser.LevelUrlFilter;
import gr.ilsp.fc.parser.DomainUrlFilter;
import gr.ilsp.fc.tmxhandler.TMXHandler;
//import gr.ilsp.fc.tmxhandler.TMXHandlerOptions;
import gr.ilsp.fc.utils.CrawlConfig;
import gr.ilsp.fc.utils.DirUtils;
import gr.ilsp.fc.utils.FCStringUtils;
import gr.ilsp.fc.utils.ISOLangCodes;
import gr.ilsp.fc.utils.Statistics;
import gr.ilsp.fc.utils.TopicTools;
import gr.ilsp.fc.workflows.CrawlWorkflow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
//import org.apache.commons.io.FileUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.JobConf;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import bixo.config.FetcherPolicy;
import bixo.config.FetcherPolicy.FetcherMode;
import bixo.config.FetcherPolicy.RedirectMode;
import bixo.config.UserAgent;
import bixo.datum.UrlStatus;
import bixo.urls.BaseUrlFilter;
import bixo.utils.CrawlDirUtils;
import cascading.flow.Flow;
import cascading.flow.PlannerException;
import cascading.scheme.SequenceFile;
import cascading.tap.Hfs;
import cascading.tap.Tap;
import cascading.tuple.TupleEntryCollector;

/**
 * JDBCCrawlTool is an example of using Bixo to write a simple crawl tool.
 * 
 * This tool uses an in-memory hsqldb to demonstrate how one could use a 
 * database to maintain the crawl db. 
 *  
 * 
 */
@SuppressWarnings("deprecation")
public class Crawl {
	private static final Logger LOGGER = Logger.getLogger(Crawl.class);
	protected static String filesepar = System.getProperty("file.separator");
	private static final String UNDERSCORE_STR = "_";
	private static final String SEMICOLON_STR=";";
	private static final String HYPHEN_STR="-";
	//parameters for file handling
	private static final String resultXMLDir = "xml";
	private static final String resultPDFDir = "pdf";
	private static final String resultDOCDir = "doc";
	private static final String XML_EXTENSION = ".xml";
	private static final String XMLlist = ".xmllist.txt";

	private static final String p_type = "p";
	private static final String m_type = "m";
	private static final String q_type = "q";
	private static final String CSV = ".csv";
	//parameters for operations
	private static final String CRAWL_operation = "crawl";
	private static final String EXPORT_operation = "export";
	private static final String DEDUP_operation = "dedup";
	private static final String PAIRDETECT_operation = "pairdetect";
	private static final String ALIGN_operation = "align";
	private static final String CONFIG_operation = "config";
	private static final String TMXMERGE_operation = "tmxmerge";
	private static final String MERGE_operation = "merge";
	//parameters for configuration files
	private static final String DEFAULT_CONFIG_FILE= "crawler_config.xml";
	private static final String DEFAULT_MONO_CONFIG_FILE = "FMC_config.xml";
	private static final String DEFAULT_BI_CONFIG_FILE = "FBC_config.xml";
	private static final int default_dirdepth=4;
	//parameters for crawling
	public static JobConf conf = null;
	public static CompositeConfiguration config;
	private static int PAGES_STORED = 0, max_depth = 10000;
	private static int PAGES_FAILED_CLASSIFICATION=0;
	private static int PAGES_VISITED = 0;
	private static int TOKENS_STORED = 0;
	private static int TOKENS_TARGET = 2000000000;
	protected static Matcher skipLineM = Pattern.compile("^(\\s*)||(#.*)$").matcher("");
	//parameters for domain classification
	private static ArrayList<String[]> topic;
	private static String[] classes;
	private static double abs_thres = 0;
	private static double rel_thres=0;
	private static int min_uniq_terms=0;
	//parameters for (near)deduplication
	private static double intersection_thres = 0.7;

	private static int max_requests_per_run = 100000;
	private static int MIN_TOK_LEN = 3;
	private static int MIN_PAR_LEN = 3;
	private static String dedup_method ="0";
	//parameters for valid TMXs
	//private static String detectpair_methods ="aupdih"; //i.e. href,url,images,digits, structure
	private static int[] thres = new int[] { 10, 10, 10, 10, 10, 10, 10, 10}; //maximum numbers of 0:1 segments in a TMX per pair detection method

	/**
	 * @param outputDirName to store the downloaded HTML, the created cesDoc or/and cesAling XML files 
	 * @return Create log output file in loop directory.. 
	 */
	private static String setLoopLoggerFile(String outputDirName, int loopNumber) {
		Logger rootLogger = Logger.getRootLogger();
		//LOGGER.info(outputDirName);
		String loopLogFile = String.format("%s/%d-JDBCCrawlTool.log", outputDirName, loopNumber);
		//LOGGER.info(filename);
		FileAppender appender = (FileAppender) rootLogger.getAppender("loop-logger");
		if (appender == null) {
			appender = new FileAppender();
			appender.setName("loop-logger");
			appender.setLayout(new PatternLayout("%d{yy/MM/dd HH:mm:ss} %p %c{2}:%L - %m%n"));
			// We have to do this before calling addAppender, as otherwise Log4J warns us.
			appender.setFile(loopLogFile);
			appender.activateOptions();
			rootLogger.addAppender(appender);
		} else {
			appender.setFile(loopLogFile);
			appender.activateOptions();
		}
		return loopLogFile;
	}

	/**
	 * @param targetDomain:crawler will stay in this web domain, crawlDbPath: path for loops' results, conf:crawler's configuration
	 * @return initialize the frontier with the seed URL list which contains only one URL (crawler will stay in this web domain). 
	 */
	private static void importOneDomain(String targetDomain, Path crawlDbPath, JobConf conf) throws IOException {
		try {
			Tap urlSink = new Hfs(new SequenceFile(CrawlDbDatum.FIELDS), crawlDbPath.toUri().toString(), true);
			TupleEntryCollector writer = urlSink.openForWrite(conf);			
			//SimpleUrlNormalizer normalizer = new SimpleUrlNormalizer();
			ILSPFCUrlNormalizer normalizer = new ILSPFCUrlNormalizer();
			CrawlDbDatum datum = new CrawlDbDatum(normalizer.normalize("http://" + targetDomain), 0, 0, UrlStatus.UNFETCHED, 0,0.0);

			writer.add(datum.getTuple());
			writer.close();
			LOGGER.info("Added domain: " + datum.getUrl());
		} catch (IOException e) {
			throw e;
		}
	}

	/**
	 * @param urls:crawler will start from these URLS, crawlDbPath: path for loops' results, conf:crawler's configuration
	 * @return initialize the frontier with the seed URL list. 
	 */
	private static void importUrlList(String urls, Path crawlDbPath, JobConf conf) throws IOException {		        
		try {
			Tap urlSink = new Hfs(new SequenceFile(CrawlDbDatum.FIELDS), crawlDbPath.toUri().toString(), true);
			TupleEntryCollector writer = urlSink.openForWrite(conf);
			//SimpleUrlNormalizer normalizer = new SimpleUrlNormalizer();
			ILSPFCUrlNormalizer normalizer = new ILSPFCUrlNormalizer();
			BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream(urls),"utf8"));
			String line = "";
			List<String> seedUrls = new ArrayList<String>();
			int linecounter=0;
			while ((line=rdr.readLine())!=null){
				linecounter++;
				if (skipLineM.reset(line).matches()) 
					continue;
				if (linecounter==1)
					line = ReadResources.checkBOM(line);
				//FIXME put these checks in a checker for valid/useful URLs
				if (line.startsWith("ftp") || line.equals("http://"))
					continue;
				line = normalizer.normalize(line);
				if (seedUrls.contains(line))
					continue;
				seedUrls.add(line);
				CrawlDbDatum datum = new CrawlDbDatum(line, 0, 0, UrlStatus.UNFETCHED, 0,0.0);
				writer.add(datum.getTuple());
			}
			LOGGER.info("Starting from "+ seedUrls.size()+ " URLs");
			rdr.close();
			writer.close();
		} catch (IOException e) {
			throw e;
		}
	}

	/**
	 * @param urls:crawler will stay in this web domain, crawlDbPath: path for loops' results, conf:crawler's configuration
	 * @return initialize the frontier with the seed URL. Crawler will stay in this web domain. 
	 */
	private static void importURLOneDomain(String urls, Path crawlDbPath, JobConf conf) throws IOException {
		try {
			Tap urlSink = new Hfs(new SequenceFile(CrawlDbDatum.FIELDS), crawlDbPath.toUri().toString(), true);
			TupleEntryCollector writer = urlSink.openForWrite(conf);			
			//SimpleUrlNormalizer normalizer = new SimpleUrlNormalizer();
			ILSPFCUrlNormalizer normalizer = new ILSPFCUrlNormalizer();
			//CrawlDbDatum datum = new CrawlDbDatum(normalizer.normalize("http://" + targetDomain), 0, 0, UrlStatus.UNFETCHED, 0,0.0);
			BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream(urls),"utf8"));
			String line = "";
			//UrlValidator urlValidator = new UrlValidator(UrlValidator.NO_FRAGMENTS);
			List<String> seedUrls = new ArrayList<String>();
			int linecounter=0;
			while ((line=rdr.readLine())!=null){
				linecounter++;
				if (skipLineM.reset(line).matches()) 
					continue;
				if (linecounter==1) 
					line = ReadResources.checkBOM(line);
				if ( line.startsWith("ftp") || line.equals("http://"))
					continue;	
				line = normalizer.normalize(line);
				if (seedUrls.contains(line)) 
					continue;
				seedUrls.add(line);
				//if (!urlValidator.isValid(line)&& !line.contains("#")) continue;
				CrawlDbDatum datum = new CrawlDbDatum(line, 0, 0, UrlStatus.UNFETCHED, 0,0.0);
				writer.add(datum.getTuple());
			}
			LOGGER.info("Starting from "+ seedUrls.size()+ " URL(s)");
			rdr.close();
			writer.close();
		} catch (IOException e) {
			throw e;
		}
	}	

	/**
	 * Checks for operation help|crawl|export|dedup|config|align|pairdetect|tmxmerge
	 * and applies it
	 * @param args
	 */
	public static void main(String[] args) {
		LOGGER.info(Arrays.toString(args));
		CrawlOptions options = new CrawlOptions();
		options.parseOptions(args);		

		config = getConfig(options.getType(), options.getConfig());
		conf = setConf();

		String operation=options.getOperation();
		if (operation.equals(EXPORT_operation)){
			//FIXME
			Operations.exporting(options,operation, config);
		} else if (operation.equals(CONFIG_operation)){
			Operations.extractDefaultConfig(options.getOutputFile().getAbsolutePath(), DEFAULT_CONFIG_FILE);
		} else	if (operation.contains(CRAWL_operation) ) {
			try { 
				crawl(options);
			} catch (IOException e) {
				LOGGER.error(e.getMessage());
			}
		} else if (operation.equals(ALIGN_operation)){
			Operations.alignment(options, config);
		} else if (operation.contains(PAIRDETECT_operation)){
			Operations.pairdetection(options, operation);
		} else if (operation.equals(DEDUP_operation)){
			Operations.deduplication(options);
		} else if (operation.equals(TMXMERGE_operation)){
			Operations.tmxMerging(options);
		} else {
			LOGGER.error("Invalid operation.");
			System.exit(-1);
		}
	}

	/**
	 * It does the crawl  
	 * @param options
	 * @throws IOException
	 */
	private static void crawl(CrawlOptions options) throws IOException {
		String operation = options.getOperation();
		FileSystem fs;
		String domain = options.getDomain();
		String urls = null;
		boolean isDomainFile = true;
		Path dompath = null;
		if (domain==null) {
			urls = options.getUrls();
			isDomainFile = false;
		}else {
			dompath = new Path(domain);
			try {
				fs = dompath.getFileSystem(conf);
				if (!fs.exists(dompath)) {
					isDomainFile = false;
					if (!domain.equals("localhost") && (domain.split("\\.").length < 2)) {
						LOGGER.error("The target domain should be a valid paid-level domain or subdomain of the same: " + domain);
						//printUsageAndExit(parser);
						options.help();
					}
				} else urls = options.getUrls();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}	

		LangDetectUtils.loadCybozuLangIdentifier();
		//check if there is an available aligner (if needed) for the targeted language pairs  
		LOGGER.info("language profiles loaded");
		Aligner aligner = null; 
		if (operation.contains(ALIGN_operation)){
			for (String lang_pair:options.getLangPairs()){
				aligner = prepareAligner(options.toAlign(), options.useDict(), options.pathDict(), lang_pair.split(SEMICOLON_STR));
				if (aligner==null){
					LOGGER.error("Aligner cannot be initialized:");
					System.exit(0);
				}
			}
		}
		
		//outputDir
		File outputDirName = options.getOutputDir();
		//value for tunneling
		int max_depth = Crawl.config.getInt("classifier.max_depth.value");
		//boolean for deleting files that have not been paired
		boolean del = options.getDel();

		if (options.getType().equals(p_type)) 
			urls = options.getUrls();
		if (domain!=null) 
			urls = options.getUrls();

		parseTopicDefinition(options.getTopic(), options.getTargetedLangs());

		if (options.isDebug()) 
			System.setProperty("fc.root.level", "DEBUG");            
		else 
			System.setProperty("fc.root.level", "ERROR");		//System.setProperty("fmc.root.level", "INFO");

		if (options.getLoggingAppender() != null) 
			System.setProperty("fc.appender", options.getLoggingAppender()); // Set console vs. DRFA vs. something else
		List<String> loopLogFiles = new ArrayList<String>();

		Map<String,Integer> langnumMap = new HashMap<String,Integer>();
		String logpair="";

		try {
			//JobConf conf = new JobConf();
			//conf.setJarByClass(Crawl.class);
			Path outputPath = new Path(outputDirName.getAbsolutePath());				
			fs = outputPath.getFileSystem(conf);
			outputPath = outputPath.makeQualified(fs);
			//If force is used, the outputPath will be deleted if it already exists
			if (options.Force() && fs.exists(outputPath)){
				LOGGER.warn("Removing previous crawl data in " + outputPath);
				fs.delete(outputPath);
			}
			//If the outputPath does not exist, it is created. Seed URL list (or domain) is imported into the hfs.
			importURLs(fs, outputPath, domain, isDomainFile, urls);

			//The last run folder is detected (in case we are resuming a previous crawl)
			Path inputPath = CrawlDirUtils.findLatestLoopDir(fs, outputPath);
			if (inputPath == null) {
				System.err.println("No previous cycle output dirs exist in " + outputDirName); //printUsageAndExit(parser);
				options.help();
			}
			//CrawlDbPath is the path where the crawl database will be stored for the current run
			Path crawlDbPath = new Path(inputPath, CrawlConfig.CRAWLDB_SUBDIR_NAME);

			//Start and end loop numbers are calculated (if the crawl is running on a fixed number of loops)
			int startLoop = CrawlDirUtils.extractLoopNumber(inputPath);
			int endLoop = startLoop + options.getNumLoops();
			boolean hasNumLoops = false;
			if (endLoop>0)
				hasNumLoops =true;

			UserAgent userAgent = new UserAgent(options.getAgentName(), config.getString("agent.email"), config.getString("agent.web_address"));
			//Fetch policy configuration
			FetcherPolicy defaultPolicy = new FetcherPolicy();
			defaultPolicy.setCrawlDelay(config.getLong("fetcher.crawl_delay.value"));
			defaultPolicy.setFetcherMode(FetcherMode.EFFICIENT);
			defaultPolicy.setRequestTimeout(config.getLong("fetcher.request_timeout.value"));
			if (options.upToDepth()<max_depth)
				defaultPolicy.setMaxRequestsPerConnection(max_requests_per_run);
			else
				defaultPolicy.setMaxRequestsPerConnection(config.getInt("fetcher.max_requests_per_run.value"));
			
			defaultPolicy.setMaxConnectionsPerHost(config.getInt("fetcher.max_connections_per_host.value"));
			defaultPolicy.setMinResponseRate(config.getInt("fetcher.min_response_rate.value"));
			defaultPolicy.setMaxRedirects(config.getInt("fetcher.max_redirects.value"));
			defaultPolicy.setMaxContentSize(config.getInt("fetcher.max_content_size.value"));
			//defaultPolicy.setRedirectMode(RedirectMode.FOLLOW_TEMP);
			defaultPolicy.setRedirectMode(RedirectMode.FOLLOW_ALL);

			//Loading of acceptable MIME types from the config file
			String[] mimes = config.getStringArray("fetcher.valid_mime_types.mime_type[@value]");			
			Set<String> validMimeTypes = new HashSet<String>();
			for (String s: mimes) validMimeTypes.add(s);
			defaultPolicy.setValidMimeTypes(validMimeTypes);
			int crawlDurationInMinutes = options.getCrawlDuration();
			//hasEndTime is the time the crawl must end (if crawl is running on specified duration)
			boolean hasEndTime = crawlDurationInMinutes != CrawlOptions.NO_CRAWL_DURATION;
			long targetEndTime = hasEndTime ? System.currentTimeMillis() + 	(crawlDurationInMinutes * 60000L) : FetcherPolicy.NO_CRAWL_END_TIME;
			//Setting up the URL filter. If domain is supplied, the filter will disregard all
			//URLs that do not belong in the specified web domain.
			BaseUrlFilter urlDomainFilter = null;
			BaseUrlFilter urlLevelFilter = null;
			if (isDomainFile)
				urlDomainFilter = new DomainUrlFilter(dompath);
			else
				urlDomainFilter = new DomainUrlFilter(domain);

			urlLevelFilter = new LevelUrlFilter(options.getCrawlLevel());

			// Main loop. This will run as many times as specified by the numloop option
			//or until the specified duration is reached
			long startTime = System.currentTimeMillis();
			ArrayList<int[]> stor_vis = new ArrayList<int[]>();

			for (int curLoop = startLoop + 1; curLoop <= endLoop; curLoop++) {
				// Checking if duration is expired. If so, crawling is terminated.
				if (hasEndTime) {
					long now = System.currentTimeMillis();
					if (targetEndTime-now<=0 || TOKENS_STORED>TOKENS_TARGET || curLoop>endLoop){
						LOGGER.info("Number of cycles reached, or Time expired, or Targeted tokens amount reached, ENDING CRAWL.");
						crawlCycleInfo(curLoop, startLoop, startTime);
						break;
					}
					if (!hasNumLoops)
						endLoop = curLoop + 1; //If duration is not reached, endLoop is increased to run the next loop
				}
				//checking if nums of stored/visited have changed. If not, crawling is terminated.
				if (check_evol(stor_vis)){
					if (stor_vis.size()==1){
						LOGGER.info("No seed page was visited. So no new links to follow");
						System.exit(64);
					}else{
						LOGGER.info("No new links to follow, ENDING CRAWL.");
						crawlCycleInfo(curLoop, startLoop, startTime);
						break;
					}
				}
				//The workflow is created and launched. Each flow is created using the directory of the
				//current loop, the crawl db directory, the policy for the Fetcher, the URL filter, the
				//topic and classes arrays, the term threshold and the crawl options
				Path curLoopDir = CrawlDirUtils.makeLoopDir(fs, outputPath, curLoop);
				String curLoopDirName = path2str(curLoopDir);
				String loopLogFile = setLoopLoggerFile(curLoopDirName, curLoop);	
				for(int il =0; il<conf.getLocalDirs().length;il++) 
					LOGGER.debug(conf.getLocalDirs()[il]);

				LOGGER.info("Starting cycle "+curLoop);
				boolean extractliks = true;
				if (options.upToDepth()-curLoop<0)
					extractliks = false;

				Flow flow = CrawlWorkflow.createFlow(curLoopDir, crawlDbPath, userAgent, defaultPolicy, urlDomainFilter, urlLevelFilter, options.getMapLangs(),
						options.getTargetedLangs(), options.getTransLinksAttrs(), classes, topic, abs_thres,rel_thres, min_uniq_terms,max_depth,options, extractliks);							
				flow.complete();
				//defaultPolicy.setRedirectMode(RedirectMode.FOLLOW_TEMP);

				//Reseting counters of parent class. We do it here so that SplitFetchedUnfetchedCrawlDatums
				//when run again will not return the 256(or whatever) that were selected in the first run
				CrawlWorkflow.resetCounters();
				if (curLoop>3) 
					DirUtils.clearPreviousLoopDir(fs,outputPath,curLoop);

				LOGGER.info("Total pages stored/visited: " + PAGES_STORED + "/" + PAGES_VISITED);
				LOGGER.info("Total pages failed classification or are too short : " + PAGES_FAILED_CLASSIFICATION );
				LOGGER.info("Total tokens stored: " + TOKENS_STORED );
				stor_vis.add(new int[] {PAGES_STORED,PAGES_VISITED});
				loopLogFiles.add(loopLogFile);
				// flow.writeDOT("build/valid-flow.dot");
				// Input for the next round is our current output
				crawlDbPath = new Path(curLoopDir, CrawlConfig.CRAWLDB_SUBDIR_NAME);
			}
			for (String loopLogFile:loopLogFiles){
				new File(loopLogFile).deleteOnExit();
			}
			FileUtils.deleteDirectory(new File(conf.get("hadoop.tmp.dir")));

			////////////////////////////////////////////////////////////////////////////////////////////////////
			Map<String, String> urlPairsFromTranslationLinks = null;
			if (options.getType().equals(p_type))
				urlPairsFromTranslationLinks = BitextsTranslationLinks.getURLPairsFromTranslationLinks(conf, ISOLangCodes.get3LetterCodes(options.getTargetedLangs()), outputDirName.getAbsolutePath());
			////////////////////////////////////////////////////////////////////////////////////////////////////////

			Map<String, String> urlsToIds = new HashMap<String, String>();
			// Finished crawling. Now export if needed.

			if (operation.contains(EXPORT_operation)) {
				Exporter se = new Exporter();
				se.setMIN_TOKENS_PER_PARAGRAPH(options.getlength());
				se.setMIN_TOKENS_NUMBER(options.getminTokenslength());
				se.setDepth(options.upToDepth());
				se.setTargetLanguages(options.getTargetedLangs());
				se.setCrawlDirName(outputDirName);
				se.setTopic(options.getTopic());
				se.setRunOffLine(false);
				se.setApplyOfflineXSLT(options.isOfflineXSLT());
				se.setBaseName(options.getBaseName());
				se.setAcceptedMimeTypes(mimes);
				se.setTargetedDomain(options.getTargetedDomain());
				se.setGenres(options.getGenre());
				se.setUrlsToIds(urlsToIds);
				langnumMap = se.export(false);
			}else{
				LOGGER.info("Crawl ended");
				System.exit(0);
			}
			/////////////////////////////////////////////////////////////////////////////////////////////////////////
			//File parentDir =new File(FilenameUtils.concat(outputDirName.getAbsolutePath(),resultXMLDir)); 
			//Detect candidate parallel documents if crawled for parallel.
			File xmldir = new File(FilenameUtils.concat(options.getOutputDir().getAbsolutePath(),resultXMLDir));
			if (options.getType().equals(p_type)){	
				//This info is known from crawl session. If a pair exists in hreflangIDPairs, it does not mean that we have fetched the webpages of this pair (i.e. the cycles were not enough, the content is too short.
				Map<String, String> idPairsFromTranslationLinks = BitextsTranslationLinks.getIdPairsFromTranslationLinks(urlPairsFromTranslationLinks, urlsToIds);
				// Remove (near) duplicates.  DOCS THAT ARE INCLUDED IN hreflangURLPairs ARE NOT REMOVED
				if (operation.contains(DEDUP_operation)){
					Set<String> filesinPairs = BitextUtils.getDocsinPairs(idPairsFromTranslationLinks);  
					Deduplicator ded = new Deduplicator();
					ded.setTargetDir(new File(FilenameUtils.concat(outputDirName.getAbsolutePath(),resultXMLDir)));
					ded.setBaseName(options.getBaseName());
					ded.setExcludeSetFiles(filesinPairs);
					ded.setApplyOfflineXSLT(options.isOfflineXSLT());
					ded.setMIN_TOK_LEN(MIN_TOK_LEN);
					ded.setMIN_PAR_LEN(MIN_PAR_LEN);
					ded.setIntersectionThr(intersection_thres);
					ded.setMethod(dedup_method);
					ded.setInputType(XML_EXTENSION.substring(1));
					ded.nearDedup();
				}
				//////////////////////////////////////////////////////////////////////////////////////////////
				if (operation.contains(PAIRDETECT_operation)){
					if (options.getTargetedLangs().length<1){
						LOGGER.warn("At least 2 languages are required.");
						System.exit(0);
					}
					PairDetector pd = new PairDetector();
					for (String lang_pair:options.getLangPairs()){
						pd.setLanguage(lang_pair);
						pd.setSourceDir(new File(FilenameUtils.concat(outputDirName.getAbsolutePath(),resultXMLDir)));
						pd.setTargetDir(new File(FilenameUtils.concat(outputDirName.getAbsolutePath(),resultXMLDir)));
						pd.setBaseName(options.getBaseName());
						pd.setExcludeSetFiles(idPairsFromTranslationLinks);
						pd.setUseImagepath(options.getImpath());
						pd.setApplyXSLT(options.isOfflineXSLT());
						pd.setURL_REPL(options.getUrlReplaces());
						pd.setMethods(options.getPairMethods());
						pd.setDelFiles(options.getDel());
						logpair = logpair+pd.pairDetect()+"\n";
					}
					logpair = logpair.replaceAll("(\n){2,}","\n");
				}
				//////////////////////////////////////////////////////////////////////////////////////////////
				if (operation.contains(ALIGN_operation)){
					for (String lang_pair:options.getLangPairs()){
						String[] temp_langs = lang_pair.split(SEMICOLON_STR);
						aligner = prepareAligner(options.toAlign(), options.useDict(), options.pathDict(), temp_langs);
						if (aligner!=null){
							String lang = UNDERSCORE_STR+temp_langs[0]+HYPHEN_STR+temp_langs[1];
							File outTextList = new File(options.getBaseName()+lang+XMLlist);
							aligner.processCesAlignList(outTextList, options.isOfflineXSLT(), options.useISO6393());
						}
					}
				}
				//////////////////////////////////////////////////////////////////////////////////////////////
				if (operation.contains(TMXMERGE_operation)){
					TMXHandler ha = new TMXHandler();
					ha.setTargetDir(xmldir);
					ha.setConfig(config);
					ha.setApplyOfflineXSLT(options.isOfflineXSLT());
					ha.setDocTypes(options.getDocTypes());
					ha.setThres( thres);
					ha.setSegTypes(options.getSegTypes());
					ha.useISO6393(options.useISO6393());
					ha.setMinTuvLen(options.getMinTuvLen());
					ha.setMinPerce01Align(options.getMinPerce01Align());
					ha.setMinTuLenRatio(options.getMinTuLenRatio());
					ha.setMaxTuLenRatio(options.getMaxTuLenRatio());
					ha.KeepTuSameNum(options.keepTuSameNum());
					ha.setCC(options.getCC());
					ha.setKeepEmpty(options.getKeepEmpty());
					ha.setKeepIdentical(options.getKeepIdentical());
					ha.setClean(options.getClean());
					ha.setKeepDuplicates(options.getKeepDuplicates());
					ha.setO1(options.getO1());
					ha.setO2(options.getO2());
					for (String lang_pair:options.getLangPairs()){
						ha.setLanguage(lang_pair);
						String[] temp_langs = lang_pair.split(SEMICOLON_STR);
						String lang = UNDERSCORE_STR+temp_langs[0]+HYPHEN_STR+temp_langs[1];
						ha.setBaseName(new File(options.getBaseName()+lang));
						ha.mergeTMXs();
					}
				}
			}
			//crawl for comparable
			if (options.getType().equals(q_type)){		/*put Nikos module based on aligned topics*/	}
			//crawled for monolingual
			if (options.getType().equals(m_type)){
				if (operation.contains(DEDUP_operation)){
					Deduplicator ded = new Deduplicator();
					ded.setTargetDir(new File(FilenameUtils.concat(outputDirName.getAbsolutePath(),resultXMLDir)));
					ded.setBaseName(options.getBaseName());
					ded.setApplyOfflineXSLT(options.isOfflineXSLT());
					ded.setMIN_TOK_LEN(MIN_TOK_LEN);
					ded.setMIN_PAR_LEN(MIN_PAR_LEN);
					ded.setIntersectionThr(intersection_thres);
					ded.setMethod(dedup_method);
					ded.setInputType(XML_EXTENSION.substring(1));
					ded.nearDedup();
					int total_tokens = ReadResources.countToksinDir(new File(FilenameUtils.concat(outputDirName.getAbsolutePath(),resultXMLDir)));
					LOGGER.info("Total tokens: "+ total_tokens);
				}
				if (operation.contains(MERGE_operation)){
					MonoMerger  mm = new MonoMerger();
					mm.setTargetDir(xmldir);
					mm.setCC(options.getCC());
					//mm.setApplyOfflineXSLT(options.isOfflineXSLT());
					mm.setdomain(options.getDomain());
					mm.setCorpusLevel("doc");
					for (String lang:options.getTargetedLangs()){
						mm.setLanguage(lang);
						mm.setBaseName(new File(options.getBaseName()+lang));
						mm.merge();
					}
				}
			}
			/////////////////////////////////////////////////////////////////////////////////////////////////////			
			if (operation.contains(EXPORT_operation)){
				if (del)
					deleteRedunFiles(fs,outputPath );
				// Rename file paths in output, if needed.
				if (options.getPathReplace() != null){
					List<File> outputFiles =  new ArrayList<File>();
					outputFiles.add(options.getOutputFile());
					outputFiles.add(options.getOutputFileHTML());
					String topDirName = getTopNDir(options.getOutputDir(), default_dirdepth).replace("\\","/");
					if (options.getType().equals(p_type)){
						outputFiles.add(options.getOutputFileHTMLTMX());
						outputFiles.add(options.getOutputFileTMX());
						replaceFilePathsInOutputFiles(outputDirName.getAbsolutePath().replace("\\","/"), outputFiles, topDirName, options.getPathReplace());
					} else if (options.getType().equals(m_type)){
						replaceFilePathsInOutputFiles(outputDirName.getAbsolutePath().replace("\\","/"), outputFiles, topDirName, options.getPathReplace());
					}
				}
			}
			if (options.upToDepth()<10000)
				createCSV(options, langnumMap, logpair);
			System.exit(0);
		} catch (PlannerException e) {
			//LOGGER.debug(conf.get("hadoop.tmp.dir"));			
			e.writeDOT("failed-flow.dot");
			LOGGER.error("PlannerException: " + e.getMessage());
			e.printStackTrace(System.err);
			System.exit(-1);
		} catch ( NullPointerException  e) {
			LOGGER.error("null pointer: " + e.getMessage());			
			e.printStackTrace(System.err);
			System.exit(-1);
		}catch (Throwable t) {
			//System.err.println(conf.get("hadoop.tmp.dir"));
			LOGGER.error("Exception running tool: " + t.getMessage());
			LOGGER.error("Things you could check:");
			LOGGER.error("Is the disk full?");
			t.printStackTrace(System.err);
			System.exit(-1);
		}
	}

	private static void createCSV(CrawlOptions options, Map<String, Integer> langnumMap, String logpair) {
		File csvfile = new File(options.getBaseName()+UNDERSCORE_STR+"depthIs"+options.upToDepth()+UNDERSCORE_STR+CSV);
		String csvtext="";
		String webdomains = options.getFilter();
		if (webdomains.isEmpty())
			webdomains = options.getDomain()+"\t"+options.getMainDomain();
		csvtext = "targeted domain:\t"+webdomains+"\n";
		csvtext = csvtext+"crawled up to depth:\t"+options.upToDepth()+"\n";
		csvtext = csvtext+"minimum length of text of accepted webpages:\t"+options.getminTokenslength()+"\n";
		//csvtext = csvtext+"staring from";
		//csvtext = addSeeds(csvtext, new File(options.getUrls()));
		String[][] sortlangs = Statistics.sort2darray(FCStringUtils.map2array(langnumMap),2,"d");
		for (int kk=0;kk<sortlangs.length;kk++){
			if (!sortlangs[kk][1].equals("0"))
				csvtext = csvtext+"number of pages in "+sortlangs[kk][0]+"\t"+sortlangs[kk][1]+"\n";
		}
		csvtext = csvtext+logpair;
		try {
			FileUtils.writeStringToFile(csvfile, csvtext);
		} catch (IOException e) {
			LOGGER.error("problem in writing the file "+csvfile.getAbsolutePath());
			e.printStackTrace();
		}
	}

	private static String addSeeds(String csvtext, File seedFile) {
		List<String> urlLines;
		try {
			urlLines = FileUtils.readLines(seedFile);
			for (String urlLine:urlLines){
				if (skipLineM.reset(urlLine).matches()) 
					continue;
				csvtext = csvtext+"\t"+urlLine+"\n";
			}
		} catch (IOException e) {
			LOGGER.error("Problem in reading "+seedFile.getAbsolutePath());
			e.printStackTrace();
		}
		return csvtext;
	}

	private static String path2str(Path curLoopDir) {
		String curLoopDirName = curLoopDir.toUri().toString();
		if (curLoopDirName.startsWith("file:/")){
			if (filesepar.equals("\\"))
				curLoopDirName = curLoopDirName.substring(6); 
			else
				curLoopDirName = curLoopDirName.substring(5);
		}
		return curLoopDirName;
	}

	private static String getTopNDir(File file, int n) {
		List<String> dirs = new ArrayList<String>();
		String temp="";
		while (temp!=null){
			temp = FilenameUtils.getBaseName(file.getAbsolutePath());
			if (temp.isEmpty())
				break;
			dirs.add(temp);
			file = file.getParentFile();
		}

		String res=file.getAbsolutePath();
		for (int ii=dirs.size()-1;ii>dirs.size()-n;ii--){
			res = FilenameUtils.concat(res, dirs.get(ii));
		}
		return res;
	}

	/**
	 * Loads Aligner, (if alignment is asked) and checks segmenters for targeted languages, if there is no available aligner, returns null.
	 * NOTE: It is initialized by the first two languages 
	 * @param toAlign 	aligner Name
	 * @param useDict	dictionary name
	 * @param pathDict	dictionary path
	 * @param langs		targeted languages
	 * @return
	 */
	public static Aligner prepareAligner(String toAlign, String useDict, String pathDict, String[] langs) {
		AlignerFactory alignerFactory = new AlignerFactory();
		Aligner aligner = alignerFactory.getAligner(toAlign);
		if (aligner!= null) {
			Properties properties = new Properties();
			if (toAlign.matches("hunalign")){
				String hunpath=getRunningJarPath();
				String prop=System.getProperty("os.name").toLowerCase();
				String aligner_runnable_path = null;
				if (prop.equals("linux")) {
					aligner_runnable_path = FilenameUtils.concat(hunpath, Crawl.config.getString("aligner.lin_align_path.value"));
				} else if(prop.startsWith("windows")) {
					aligner_runnable_path = FilenameUtils.concat(hunpath, Crawl.config.getString("aligner.win_align_path.value"));
				}
				String usedict = useDict;
				String dictalign_path=null;
				if (toAlign.equals("hunalign")){
					if (usedict!=null && !usedict.equals("default")) {
						dictalign_path=pathDict;
					} else {	
						dictalign_path = FilenameUtils.concat(hunpath, Crawl.config.getString("aligner.align_dict.value"));
					}
				} else {
					dictalign_path = pathDict;
				}
				properties.setProperty("aligner_runnable_path", aligner_runnable_path);
				if (dictalign_path!=null) {
					properties.setProperty("dictalign_path", dictalign_path);
				}
			}
			properties.setProperty("sourceLang", ISOLangCodes.get3LetterCode(langs[0]));
			for (int jj=langs.length-1;jj>0;jj--){
				properties.setProperty("targetLang", ISOLangCodes.get3LetterCode(langs[jj]));			
				try {
					aligner.initialize(properties);
				} catch (Exception ex) {
					//ex.printStackTrace();
					return null; 
				}
			}
		}
		return aligner;
	}

	/**
	 * Analyzes the topic and creates the classes and topic variables. Term threshold
	 * is calculate based on the median weight of the terms and the minimum number
	 * of terms each text must have as defined on the config file.
	 * @param topicDef
	 * @param langs
	 */
	private static void parseTopicDefinition(File topicFile, String[] langs) {
		if (topicFile!=null){
			//topic=TopicTools.analyzeTopic(topicFile,langs, conf);
			topic=TopicTools.analyzeTopic(topicFile,langs);
			if (topic.isEmpty()){
				LOGGER.error("Even though a file for topic definition is defined, no terms have been found (check the file for existance and/or format).");
				System.exit(0);
			}
			LOGGER.info("Topic analyzed, " + topic.size() + " terms found.");
			classes=TopicTools.findSubclasses(topic);
			LOGGER.info(classes.length + " classes found.");
			abs_thres = TopicTools.calculateTopicThreshold(topic, Crawl.config.getInt("classifier.min_content_terms.value"));
			LOGGER.info("Classifier threshold calculated: "+ abs_thres);
			min_uniq_terms = Crawl.config.getInt("classifier.min_unique_content_terms.value");
			rel_thres = Crawl.config.getDouble("classifier.relative_relevance_threshold.value");
		} else 
			LOGGER.info("Running with no topic.");

	}

	/**
	 * Deletes hadoopTempFiles and RunDirs
	 * @param fs
	 * @param outputPath
	 */
	private static void deleteRedunFiles(FileSystem fs, Path outputPath) {
		File hadoopTempFile=new File(conf.get("hadoop.tmp.dir"));
		try {
			FileUtils.deleteDirectory(hadoopTempFile);
			//Delete every dir created for each run
			List<String> notToBeDeleted= new ArrayList<String>();
			notToBeDeleted.add(resultXMLDir);
			//notToBeDeleted.add(resultTMXDir);
			DirUtils.deleteLoopDirs(fs, outputPath, notToBeDeleted);
		} catch (IOException e) {
			LOGGER.error("Problem in deleting redundant files");
			e.printStackTrace();
		}
	}

	/**
	 * Handles list of seed URLs per run/crawl cycle
	 * @param fs
	 * @param outputPath
	 * @param domain
	 * @param isDomainFile
	 * @param urls
	 */
	private static void importURLs(FileSystem fs, Path outputPath, String domain, boolean isDomainFile, String urls) {
		try {
			if (!fs.exists(outputPath)) {
				//FIXME convert Path to String (file protocol should be removed)
				//System.out.println(outputPath.toUri().getPath());
				LOGGER.info("Creating directory: " +path2str(outputPath));

				fs.mkdirs(outputPath);
				Path curLoopDir = CrawlDirUtils.makeLoopDir(fs, outputPath, 0);
				Path pdf_dir =new Path(FilenameUtils.concat(outputPath.toString(),resultPDFDir));  
				if (!fs.exists(pdf_dir))
					fs.mkdirs(pdf_dir);
				pdf_dir =new Path(FilenameUtils.concat(outputPath.toString(),resultDOCDir));  
				if (!fs.exists(pdf_dir))
					fs.mkdirs(pdf_dir);
				String curLoopDirName = path2str(curLoopDir);
				setLoopLoggerFile(curLoopDirName, 0);
				Path crawlDbPath = new Path(curLoopDir, CrawlConfig.CRAWLDB_SUBDIR_NAME);
				if (domain!=null && !isDomainFile){
					if (urls!=null)
						importURLOneDomain(urls,crawlDbPath , conf);
					else
						importOneDomain(domain,crawlDbPath , conf);
				}else
					importUrlList(urls,crawlDbPath, conf);
			}
		} catch (IOException e) {
			LOGGER.error("Problem in importing the URLs in HFS");
			e.printStackTrace();
		}
	}

	private static JobConf setConf() {
		conf = new JobConf();
		conf.setJarByClass(Crawl.class);
		//Added this for concurrency issues
		conf.set("hadoop.tmp.dir", conf.get("hadoop.tmp.dir")+UUID.randomUUID().toString());
		//conf.set("mapred.dir",conf.get("hadoop.tmp.dir") + fs1+"mapred");
		//conf.set("mapred.local.dir",conf.get("mapred.dir") + fs1+"local");
		//conf.set("mapred.system.dir",conf.get("mapred.dir") + fs1+"system");	

		conf.set("mapred.dir",FilenameUtils.concat(conf.get("hadoop.tmp.dir"),"mapred"));
		conf.set("mapred.local.dir",FilenameUtils.concat(conf.get("mapred.dir"),"local"));
		conf.set("mapred.system.dir",FilenameUtils.concat(conf.get("mapred.dir"),"system"));
		return conf;
	}

	/**
	 * Loads the default configuration file and checks if user supplied a custom one.
	 * @param type
	 * @param confFile
	 * @return
	 */
	private static CompositeConfiguration getConfig(String type, String confFile) {
		config = new CompositeConfiguration();
		URL default_config = Crawl.class.getClassLoader().getResource(DEFAULT_MONO_CONFIG_FILE);
		if (type.equals(p_type))
			default_config = Crawl.class.getClassLoader().getResource(DEFAULT_BI_CONFIG_FILE);

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

	/**
	 * Replaces dirPaths with URLs: the part "match" (if exists in the "outputDirName"), will be replaced by "replacement" in content of all files in "outputFiles" 
	 * @param outputDirName
	 * @param outputFiles
	 * @param match
	 * @param replacement
	 */
	public static void replaceFilePathsInOutputFiles(String outputDirName, List<File> outputFiles, 
			String match, String replacement) {
		File outputDir = new File(FilenameUtils.concat(outputDirName,resultXMLDir));
		replacement = replacement.trim();

		LOGGER.debug("Replacing in " + outputDirName + " the " +	match);
		if (outputDirName.contains(match)) {
			try {
				for (File  outputListFile : outputFiles) {
					if (outputListFile==null)
						continue;
					if (outputListFile.exists() && outputListFile.canWrite()) {
						List<String> lines = FileUtils.readLines(outputListFile);
						List<String> newLines = new ArrayList<>();
						for (String line: lines) {
							line = line.replace(match, replacement);
							newLines.add(line);
						}
						FileUtils.writeLines(outputListFile, newLines);
					}
				}
				File[] allfiles= outputDir.listFiles();
				for (int ii=0;ii<allfiles.length;ii++){
					String curfilename=allfiles[ii].getName();
					if (curfilename.endsWith(XML_EXTENSION) && curfilename.contains(UNDERSCORE_STR)) {
						File curFile =  new File(allfiles[ii].getAbsolutePath());
						List<String> lines = FileUtils.readLines(curFile);
						List<String> newLines = new ArrayList<>();
						for (String line: lines) {
							//line = line.replace(match, replacement);
							line = line.replace(match, "");
							newLines.add(line);
						}
						FileUtils.writeLines(curFile, newLines);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Returns the dirPath of the running jar
	 * @return
	 */
	public static String getRunningJarPath() {
		String runpath="";
		String path = Crawl.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		try {
			File decodedPath = new File(URLDecoder.decode(path, "UTF-8"));
			runpath= decodedPath.getParent();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return runpath;
	}

	/**
	 * Checks if stored/visited pages of the current crawl cycle is different from the previous crawl cycle,
	 * @param stor_vis Array of stored and visited pages per run
	 * @return true if no new links to follow exist and crawl has to stop. 
	 */
	private static boolean check_evol(ArrayList<int[]> stor_vis) {
		boolean stop_crawl = false;
		int runs = stor_vis.size();
		if (runs>1){ //two runs at least 
			if (stor_vis.get(runs-1)[1]==stor_vis.get(runs-2)[1])
				stop_crawl=true;
		}
		return stop_crawl;
	}

	/**
	 * Provides info about stored/visited pages up to this crawl
	 * @param curLoop
	 * @param startLoop
	 * @param startTime
	 */
	private static void crawlCycleInfo(int curLoop, int startLoop, long startTime){
		long duration = System.currentTimeMillis()-startTime;
		LOGGER.info("Made " + (curLoop-startLoop-1) + " runs in " + 
				(System.currentTimeMillis()-startTime) + " milliseconds.");
		float avg = (float)duration/(curLoop-startLoop-1);
		LOGGER.info("Total pages stored/visited: " + PAGES_STORED + "/" + PAGES_VISITED);
		LOGGER.info("Total pages failed classification or are too short: " + PAGES_FAILED_CLASSIFICATION );
		LOGGER.info("Total tokens stored: " + TOKENS_STORED);
		LOGGER.info("Average run time: " + avg + " milliseconds.");	
	}

	public static void incrementPagesStored() {
		PAGES_STORED++;
	}

	public static void incrementPagesVisited() {
		PAGES_VISITED++;
	}

	public static int incrementTokensStored(Double len) {
		TOKENS_STORED=(int) (TOKENS_STORED+len);
		return TOKENS_STORED;
	}

	public static void incrementPagesCutByClassifier() {
		PAGES_FAILED_CLASSIFICATION++;
	}

	/**
	 * In case of bilingual crawl, checks if there are enough files for pair detection and calculates tokens per language (before pairing)
	 * @param lang
	 * @param props
	 * @param outputFile
	 * @return
	 */
	public static boolean check_crawl_stats(String[] lang,	HashMap<String, String[]> props, String outputFile) {
		if (props.size()<2){
			LOGGER.info("Less than 2 files found. Detection of pairs is stopped.");
			File out_temp=new File(outputFile);
			if (out_temp.exists())
				out_temp.delete();
			return false;
		}
		int l1=0, l2=0;
		Set<String> files_keys=props.keySet();
		Iterator<String> files_it = files_keys.iterator();
		String key;
		while (files_it.hasNext()){
			key = files_it.next();
			String[] file_props=props.get(key);
			if (file_props[1].equals(lang[0]))
				l1++;
			else{
				if (file_props[1].equals(lang[1]))
					l2++;
			}
		}
		LOGGER.info(l1 + " documents in "+lang[0]+ " . "+ l2 +" documents in "+lang[1]+ " .");
		if (l1==0){
			LOGGER.info("No file found in " + lang[0]+"; Detection of pairs is stopped.");
			File out_temp=new File(outputFile);
			if (out_temp.exists())
				out_temp.delete();
			return false;
		}
		if (l2==0){
			LOGGER.info("No file found in " + lang[1]+"; Detection of pairs is stopped.");
			File out_temp=new File(outputFile);
			if (out_temp.exists())
				out_temp.delete();
			return false;
		}
		return true;
	}

	//System.out.println("Mirroring site ...");
	//File newdir=Bitexts.mirrorsite(options.getOutputDir()+"/xml");
	//File outFile = new File(options.getOutputFile());
	//File tempdir = new File(options.getOutputDir()+"/xml/downloadedsite");
	//System.out.println(tempdir.getAbsolutePath());
	//if (newdir!=null){
	//	System.out.println("Finding bitexts ...");
	//Bitexts.runbitextor(newdir);
	//System.out.println("Bitextor finished.");
	//Bitexts.findbitexts(newdir,outFile);
	//Bitexts.deleteDir(tempdir);
	//Bitexts.createprofiles(newdir);
	//}
}
