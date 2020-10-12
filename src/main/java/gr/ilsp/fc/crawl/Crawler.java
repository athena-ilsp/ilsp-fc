package gr.ilsp.fc.crawl;

import gr.ilsp.fc.crawl.CrawlerDirConfig;
import gr.ilsp.fc.bitext.BitextsTranslationLinks;
import gr.ilsp.fc.crawl.CrawlerUtils;
import gr.ilsp.fc.crawl.CrawlerOptions;
import gr.ilsp.fc.parser.LevelUrlFilter;
import gr.ilsp.fc.parser.DomainUrlFilter;
import gr.ilsp.fc.utils.DirUtils;
import gr.ilsp.fc.utils.ISOLangCodes;
import gr.ilsp.fc.utils.TopicTools;
import gr.ilsp.fc.workflows.CrawlerWorkflow;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.JobConf;
import org.apache.log4j.Logger;

import bixo.config.FetcherPolicy;
import bixo.config.FetcherPolicy.FetcherMode;
import bixo.config.FetcherPolicy.RedirectMode;
import bixo.config.UserAgent;
import bixo.urls.BaseUrlFilter;
import bixo.utils.CrawlDirUtils;
import cascading.flow.Flow;

/**
 * JDBCCrawlTool is an example of using Bixo to write a simple crawl tool.
 * 
 * This tool uses an in-memory hsqldb to demonstrate how one could use a 
 * database to maintain the crawl db. 
 *  
 * 
 */
@SuppressWarnings("deprecation")
public class Crawler {
	private static final Logger LOGGER = Logger.getLogger(Crawler.class);
	private static final String resultPDFDir = "pdf";
	private static final String resultDOCDir = "doc";
	private static final String p_type = "p";

	private static final String DEFAULT_MONO_CONFIG_FILE = "FMC_config.xml";
	private static final String DEFAULT_BI_CONFIG_FILE = "FBC_config.xml";

	private static int PAGES_STORED = 0, max_crawldepth = 10000;  
	private static int encoding_issues=0;
	private static int PAGES_FAILED_CLASSIFICATION=0;
	private static int PAGES_VISITED = 0;
	private static int TOKENS_STORED = 0;
	private static int TOKENS_TARGET = 2000000000;
	protected static Matcher skipLineM = Pattern.compile("^(\\s*)||(#.*)$").matcher("");

	//private static CrawlerOptions options;

	//parameters for domain classification
	private static ArrayList<String[]> topic;
	private static String[] classes;
	private static double abs_thres = 0;
	private static double rel_thres=0;
	private static int min_uniq_terms=0;

	private static int max_requests_per_run = 10000;

	public static CompositeConfiguration configuration;
	public static CrawlerO cr;

	public static void main(String[] args) {
		LOGGER.info(Arrays.toString(args));
		CrawlerOptions options = new CrawlerOptions();
		options.parseOptions(args);		
		crawl(options);
	}

	/**
	 * Parses CrawlerOprions, creates a Crawler Object, and runs the Crawl Job.
	 * Creates a specific file structure "user-defined directory/crawl directory/job directory" in which the data acquired in each crawl cycle is stored in directories.
	 * @param options
	 * @return
	 */
	public static CrawlerO crawl(CrawlerOptions options) {
		cr = new CrawlerO();
		if (options.getLoggingAppender() != null)	//if (cr.getLoggingAppender()!=null)
			System.setProperty("fc.appender",options.getLoggingAppender()); // Set console vs. DRFA vs. something else

		cr.setIsdegub(options.isDebug());

		//about job configuration
		cr.setJobconf(defineJobConf());

		//crawl type (mono or bilingual)
		cr.setType(options.getType());
		cr.setConfigFile(options.getConfig());
		//load configuration file for this crawl type
		configuration = getConfig(cr.type, cr.getConfigFile());
		cr.setConfiguration(configuration);

		cr.setForce(options.getForce());
		cr.setAgentName(options.getAgentName());
		cr.setUserAgent(new UserAgent(cr.agentName, cr.configuration.getString("agent.email"), cr.configuration.getString("agent.web_address")));
		cr.setForce(options.getForce());
		cr.setDepth(options.upToDepth());
		cr.setLevel(options.getCrawlLevel());
		cr.setUrlLevelFilter(new LevelUrlFilter(cr.level));
		cr.setThreads(options.getThreads());
		cr.setLinkAttrs(CrawlerOptions.findTransLinks()); 
		cr.setFilter(options.getFilter());
		cr.setStorefilter(options.getStoreFilter());
		cr.setWebDomain(options.getWebDomain());
		cr.setWebMainDomain(options.getWebMainDomain());
		cr.setDomainFile(isDomainFile(cr.webDomain,cr.jobconf));

		//outputdir
		cr.setOutputDir(options.getOutputDir());
		cr.setOutputDirPath(new Path(cr.outputDir.getAbsolutePath()));
		//filesystem
		cr.setFs(defineFileFystem(cr.outputDirPath, cr.jobconf));
		cr.setOutputDirPath(cr.outputDirPath.makeQualified(cr.fs));

		setFileStruct(cr);

		//about languages
		cr.setTargetlangs(options.getTargetedLangs());
		cr.setMapLangs(options.getMapLangs());
		cr.setLangKeys(options.getLangKeys());
		cr.setLangDetector(options.getLangDetector());
		//LangDetectUtils.loadCybozuLangIdentifier(); //load languages' profiles

		//about properties of the targeted content
		cr.setKeepboiler(options.keepBoiler());
		//cr.setMinParLen(options.getMinParLen());
		cr.setMinDocLen(options.getMinDocLen());
		cr.setThreads(options.getThreads());

		//about topic
		cr.setMaxTunnelingDepth(cr.configuration.getInt("classifier.max_depth.value"));
		cr.setTopicFile(options.getTopicFile());
		parseTopicDefinition(cr.topicFile, cr.configuration,  cr.targetlangs); 
		cr.setAbs_thres(abs_thres);
		cr.setRel_thres(rel_thres);
		cr.setTopic(topic);
		cr.setClasses(classes);
		cr.setMin_uniq_terms(min_uniq_terms);
		cr.setUserTopic(options.getUserTopic());
		//cr.setNegWordsFile(options.getNegwordsFile());

		//if crawl is running on specified duration
		cr.setDurationMinutes(options.getCrawlDuration());
		cr.setHasEndTime(cr.durationMinutes != CrawlerOptions.NO_CRAWL_DURATION);
		cr.setTargetEndTime(cr.hasEndTime ? System.currentTimeMillis() + 	(cr.durationMinutes * 60000L) : FetcherPolicy.NO_CRAWL_END_TIME);	

		//Loading of acceptable MIME types from the configuration file
		cr.setValidMimes(CrawlerUtils.array2Set(cr.configuration.getStringArray("fetcher.valid_mime_types.mime_type[@value]")));

		//seedURLs
		cr.setSeedsFile(options.getSeedFile());	

		//Setting up the URL filter. If domain is supplied, the filter will disregard all
		//URLs that do not belong in the specified web domain.
		cr.setUrlDomainFilter(defineDomainFilter(cr.webDomain, cr.isDomainFile));
		//fetcherPolicy
		cr.setPolicy(definePolicy(cr));

		importSeedURLs(cr);

		cr.setInputPath(defineInputPath(cr.fs, cr.outputDirPath, cr.outputDir)); 	

		//if crawl is running on specified number of loops
		cr.setDurationNumLoops(options.getNumLoops());
		cr.setStartLoop(CrawlDirUtils.extractLoopNumber(cr.inputPath));
		cr.setEndLoop(cr.startLoop + cr.durationNumLoops);
		cr.setHasNumLoops(checkhasNumLoops(cr.endLoop));

		runCrawl(cr);
		return cr;
	}


	/**
	 * Seed URL list (or domain) is imported into the hfs.
	 * @param cr
	 */
	private static void importSeedURLs(CrawlerO cr) {
		try {
			Path curLoopDir = CrawlDirUtils.makeLoopDir(cr.fs, cr.outputDirPath, 0);
			String curLoopDirName = CrawlerUtils.path2str(curLoopDir);
			CrawlerUtils.setLoopLoggerFile(curLoopDirName, 0);
			cr.setCrawlDbPath(new Path(curLoopDir, CrawlerDirConfig.CRAWLDB_SUBDIR_NAME));
			if (cr.webDomain!=null && !cr.isDomainFile){
				if (cr.seedsFile!=null)
					CrawlerUtils.importURLOneDomain(cr.seedsFile.getAbsolutePath(),cr.crawlDbPath , cr.jobconf);
				else
					CrawlerUtils.importOneDomain(cr.webDomain,cr.crawlDbPath , cr.jobconf);
			}else
				CrawlerUtils.importUrlList(cr.seedsFile.getAbsolutePath(),cr.crawlDbPath, cr.jobconf);
		} catch (IOException e) {
			LOGGER.error("Problem in importing the URLs in HFS");
			e.printStackTrace();
		}
	}


	/**
	 * makes the required directories to store the crawl files (run directories, doc files, pdf files, etc.)
	 * @param cr
	 */
	private static void setFileStruct(CrawlerO cr) {
		//If force is used, the outputPath will be deleted if it already exists
		try {
			if (cr.force && cr.fs.exists(cr.outputDirPath)){
				LOGGER.warn("Removing previous crawl data in " + cr.outputDirPath);
				cr.fs.delete(cr.outputDirPath);
			}
		} catch (IOException e) {
			LOGGER.error("Problem in removing previous crawl data in " + cr.outputDirPath);
			e.printStackTrace();
		}
		try {
			if (!cr.fs.exists(cr.outputDirPath)) {
				//FIXME convert Path to String (file protocol should be removed)
				//System.out.println(outputPath.toUri().getPath());
				LOGGER.info("The results will be stored in: " +CrawlerUtils.path2str(cr.outputDirPath));
				System.out.println();
				cr.fs.mkdirs(cr.outputDirPath);
				Path m_dir =new Path(FilenameUtils.concat(cr.outputDirPath.toString(),resultPDFDir));  
				if (!cr.fs.exists(m_dir))
					cr.fs.mkdirs(m_dir);
				m_dir =new Path(FilenameUtils.concat(cr.outputDirPath.toString(),resultDOCDir));  
				if (!cr.fs.exists(m_dir))
					cr.fs.mkdirs(m_dir);
			}
		} catch (IOException e) {
			LOGGER.error("Problem in creating directories needed for the new crawl");
			e.printStackTrace();
		}
	}

	private static void runCrawl(CrawlerO cr) {
		List<String> loopLogFiles = new ArrayList<String>();

		if (cr.isdegub) 
			System.setProperty("fc.root.level", "DEBUG");            
		else 
			System.setProperty("fc.root.level", "ERROR");		//System.setProperty("fmc.root.level", "INFO");

		if (cr.loggingAppender != null) 
			System.setProperty("fc.appender", cr.loggingAppender); // Set console vs. DRFA vs. something else


		//try {
		// Main loop. This will run as many times as specified by the numloop option
		//or until the specified duration is reached
		long startTime = System.currentTimeMillis();
		ArrayList<int[]> stor_vis = new ArrayList<int[]>();

		for (int curLoop = cr.startLoop + 1; curLoop <= cr.endLoop; curLoop++) {
			// Checking if duration is expired. If so, crawling is terminated.
			if (cr.hasEndTime) {
				long now = System.currentTimeMillis();
				if (cr.targetEndTime-now<=0 || TOKENS_STORED>TOKENS_TARGET || curLoop>cr.endLoop){
					LOGGER.info("Number of cycles reached, or Time expired, or Targeted tokens amount reached, ENDING CRAWL.");
					crawlCycleInfo(curLoop, cr.startLoop, startTime);
					break;
				}
				if (!cr.hasNumLoops)
					cr.setEndLoop(curLoop + 1); //If duration is not reached, endLoop is increased to run the next loop
			}
			//checking if nums of stored/visited have changed. If not, crawling is terminated.
			if (CrawlerUtils.check_evol(stor_vis)){
				if (stor_vis.size()==1){
					LOGGER.info("No seed page was visited. So no new links to follow");
					System.exit(64);
				}else{
					LOGGER.info("No new links to follow, ENDING CRAWL.");
					crawlCycleInfo(curLoop, cr.startLoop, startTime);
					break;
				}
			}
			//The workflow is created and launched. Each flow is created using the directory of the
			//current loop, the crawl db directory, the policy for the Fetcher, the URL filter, the
			//topic and classes arrays, the term threshold and the crawl options
			Path curLoopDir = null;
			String curLoopDirName="";
			try {
				curLoopDir = CrawlDirUtils.makeLoopDir(cr.fs, cr.outputDirPath, curLoop);
				curLoopDirName = CrawlerUtils.path2str(curLoopDir);
			} catch (IOException e) {
				LOGGER.error("problem in creating LoopDirs");
				e.printStackTrace();
			}

			String loopLogFile = CrawlerUtils.setLoopLoggerFile(curLoopDirName, curLoop);	
			//	for(int il =0; il<cr.jobconf.getLocalDirs().length;il++) 
			//		LOGGER.debug(cr.jobconf.getLocalDirs()[il]);

			LOGGER.info("The crawler runs in cycles:");
			LOGGER.info("Starting cycle "+curLoop);
			if (curLoop==1){
				LOGGER.info("  1. The seed url(s) are being fetched.");
				LOGGER.info("  2. The content of each fetched page/document is normalised (UTF8 conversion, metadata extraction).");
				LOGGER.info("  3. The main content of each document is extracted, i.e. boilerplate (e.g. advertisements) is detected.");
				LOGGER.info("  4. The language of the main content of each document is identified.");
				LOGGER.info("  5. In case a topic definition is provided by the user, each document is classified as relevant to the targeted topic or not.");
				LOGGER.info("  6. The links of the fetched pages are extracted and prioritized in order to feed the crawler's next cycle.");
			}
			boolean extractlinks = true;
			if (cr.depth-curLoop<0)
				extractlinks = false;
			Flow flow = null;
			try {
				flow = CrawlerWorkflow.createFlowN(curLoopDir, cr, extractlinks);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			flow.complete();
			//defaultPolicy.setRedirectMode(RedirectMode.FOLLOW_TEMP);

			//Reseting counters of parent class. We do it here so that SplitFetchedUnfetchedCrawlDatums
			//when run again will not return the 256(or whatever) that were selected in the first run
			CrawlerWorkflow.resetCounters();
			if (curLoop>3) 
				DirUtils.clearPreviousLoopDir(cr.fs,cr.outputDirPath,curLoop);
			System.out.println();
			LOGGER.info("Total pages stored/visited: " + PAGES_STORED + "/" + PAGES_VISITED);
			LOGGER.info("Encoding issues in "+encoding_issues + " pages");
			LOGGER.info("Total pages failed classification (due to language/topic), or are too short : " + PAGES_FAILED_CLASSIFICATION );
			LOGGER.info("Total tokens stored: " + TOKENS_STORED );
			stor_vis.add(new int[] {PAGES_STORED,PAGES_VISITED});
			loopLogFiles.add(loopLogFile);
			// flow.writeDOT("build/valid-flow.dot");
			// Input for the next round is our current output
			cr.setCrawlDbPath(new Path(curLoopDir, CrawlerDirConfig.CRAWLDB_SUBDIR_NAME));
		}
		for (String loopLogFile:loopLogFiles){
			new File(loopLogFile).deleteOnExit();
		}
		try {
			FileUtils.deleteDirectory(new File(cr.jobconf.get("hadoop.tmp.dir")));
		} catch (IOException e) {
			LOGGER.warn("problem in deleting hadoop.tmp.dir ");
			e.printStackTrace();
		}

		////////////////////////////////////////////////////////////////////////////////////////////////////
		Map<String, String> urlPairsFromTranslationLinks = new HashMap<String, String>();
		if (cr.type.equals(p_type)){
			try {
				urlPairsFromTranslationLinks = BitextsTranslationLinks.getURLPairsFromTranslationLinks(cr.jobconf, 
						ISOLangCodes.get3LetterCodes(cr.targetlangs), cr.outputDir.getAbsolutePath());
			} catch (IOException e) {
				LOGGER.warn("problem in defining pairs based on links");
				e.printStackTrace();
			}
		}
		cr.setURLPairsFromTranslationLinks(urlPairsFromTranslationLinks);
		
		List<String> urlPairsFromTranslationLinksList = new ArrayList<String>();
		if (cr.type.equals(p_type)){
			try {
				urlPairsFromTranslationLinksList = BitextsTranslationLinks.getURLPairsFromTranslationLinksList(cr.jobconf, 
						ISOLangCodes.get3LetterCodes(cr.targetlangs), cr.outputDir.getAbsolutePath());
			} catch (IOException e) {
				LOGGER.warn("problem in defining pairs based on links");
				e.printStackTrace();
			}
		}
		cr.setURLPairsFromTranslationLinksList(urlPairsFromTranslationLinksList);
		////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		System.out.println();
		LOGGER.info("Crawler ended");
		/*} catch (PlannerException e) {
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
		}*/
	}

	/**
	 * Analyzes the topicFile and creates topicness-related variables: classes, absolute_threshold, relative_threshold, minimum_unique_terms and topicList. Term threshold
	 * is calculate based on the median weight of the terms and the minimum number
	 * of terms each text must have as defined on the config file.
	 * @param compositeConfiguration 
	 * @param topicDef
	 * @param langs
	 */
	private static void parseTopicDefinition(File topicFile, CompositeConfiguration compositeConfiguration, String[] langs) {
		if (topicFile!=null){
			//topic=TopicTools.analyzeTopic(topicFile,langs, conf);
			topic=TopicTools.analyzeTopic(topicFile,langs);
			if (Crawler.topic.isEmpty()){
				LOGGER.error("Even though a file for topic definition is defined, no terms have been found (check the file for existance and/or format).");
				System.exit(0);
			}
			LOGGER.info("Topic analyzed, " + topic.size() + " terms found.");
			classes=TopicTools.findSubclasses(topic);
			LOGGER.info(classes.length + " classes found.");
			abs_thres = TopicTools.calculateTopicThreshold(topic, compositeConfiguration.getInt("classifier.min_content_terms.value"));
			LOGGER.info("Classifier threshold calculated: "+ abs_thres);
			min_uniq_terms = compositeConfiguration.getInt("classifier.min_unique_content_terms.value");
			rel_thres = compositeConfiguration.getDouble("classifier.relative_relevance_threshold.value");
		} else 
			LOGGER.info("Running with no topic definition.\n"+"\t\t In general, the topic defintion is a list of terms that describe the targeted topic in the targeted languages.");
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
		LOGGER.info("Visited pages with encoding issues: " + encoding_issues );
		LOGGER.info("Total tokens stored: " + TOKENS_STORED);
		LOGGER.info("Average run time: " + avg + " milliseconds.");	
	}


	private static JobConf defineJobConf() {
		JobConf jobconf = new JobConf();
		jobconf.setJarByClass(Crawler.class);
		//Added this for concurrency issues
		jobconf.set("hadoop.tmp.dir", jobconf.get("hadoop.tmp.dir")+UUID.randomUUID().toString());
		//conf.set("mapred.dir",conf.get("hadoop.tmp.dir") + fs1+"mapred");
		//conf.set("mapred.local.dir",conf.get("mapred.dir") + fs1+"local");
		//conf.set("mapred.system.dir",conf.get("mapred.dir") + fs1+"system");	
		jobconf.set("mapred.dir",FilenameUtils.concat(jobconf.get("hadoop.tmp.dir"),"mapred"));
		jobconf.set("mapred.local.dir",FilenameUtils.concat(jobconf.get("mapred.dir"),"local"));
		jobconf.set("mapred.system.dir",FilenameUtils.concat(jobconf.get("mapred.dir"),"system"));
		return jobconf;
	}

	/**
	 * Loads the default configuration file and checks if user supplied a custom one.
	 * @param type
	 * @param confFile
	 * @return
	 */
	public static CompositeConfiguration getConfig(String type, String confFile) {
		CompositeConfiguration configuration = new CompositeConfiguration();
		URL default_config = Crawler.class.getClassLoader().getResource(DEFAULT_MONO_CONFIG_FILE);
		if (type.equals(p_type))
			default_config = Crawler.class.getClassLoader().getResource(DEFAULT_BI_CONFIG_FILE);

		if (confFile!=null){
			String custom_config = confFile;
			try {
				XMLConfiguration xml_custom_config = new XMLConfiguration(custom_config);
				xml_custom_config.setValidating(true);
				configuration.addConfiguration(xml_custom_config);
			} catch (ConfigurationException e) {
				LOGGER.error("Invalid configuration file: " + custom_config);
			}
		}
		try {			
			configuration.addConfiguration(new XMLConfiguration(default_config));				
		} catch (ConfigurationException e1) {
			// Shouldn't happen
			LOGGER.error("Problem with default configuration file.");
		}
		return configuration;
	}


	public static void incrementPagesStored() {
		PAGES_STORED++;
	}

	public static void incrementPagesVisited() {
		PAGES_VISITED++;
	}

	public static void incrementEncodingIssues() {
		encoding_issues++;
	}


	public static int incrementTokensStored(Double len) {
		TOKENS_STORED=(int) (TOKENS_STORED+len);
		return TOKENS_STORED;
	}

	public static void incrementPagesCutByClassifier() {
		PAGES_FAILED_CLASSIFICATION++;
	}

	private static BaseUrlFilter defineDomainFilter(String webDomain,	boolean isDomainFile) {
		BaseUrlFilter urlDomainFilter = null;

		Path dompath = null;
		if (webDomain!=null) 
			dompath = new Path(webDomain);	
		if (isDomainFile)
			urlDomainFilter = new DomainUrlFilter(dompath);
		else
			urlDomainFilter = new DomainUrlFilter(webDomain);
		return urlDomainFilter;
	}

	private static boolean checkhasNumLoops(int endLoop) {
		boolean hasNumLoops = false;
		if (endLoop>0)
			hasNumLoops =true;
		return hasNumLoops;
	}

	private static FileSystem defineFileFystem(Path outputDirPath, JobConf jobconf) {
		FileSystem fs = null;
		try {
			fs = outputDirPath.getFileSystem(jobconf);
		} catch (IOException e) {
			LOGGER.error("Problem in setting dedicated fs for this job.");
			e.printStackTrace();
		}
		return fs;
	}

	private static boolean isDomainFile(String webdomain, JobConf conf) {
		FileSystem fs;
		boolean isDomainFile = true;
		Path dompath = null;
		if (webdomain==null) 
			isDomainFile = false;
		else {
			dompath = new Path(webdomain);
			try {
				fs = dompath.getFileSystem(conf);
				if (!fs.exists(dompath)) {
					isDomainFile = false;
					if (!webdomain.equals("localhost") && (webdomain.split("\\.").length < 2)) {
						LOGGER.error("The target domain should be a valid paid-level domain or subdomain of the same: " + webdomain);
						//printUsageAndExit(parser);
						//options.help();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}	
		return isDomainFile;
	}

	private static FetcherPolicy definePolicy(CrawlerO cr) {
		//Fetch policy configuration
		FetcherPolicy defaultPolicy = new FetcherPolicy();
		defaultPolicy.setCrawlDelay(cr.configuration.getLong("fetcher.crawl_delay.value"));
		defaultPolicy.setFetcherMode(FetcherMode.EFFICIENT);
		defaultPolicy.setRequestTimeout(cr.configuration.getLong("fetcher.request_timeout.value"));
		if (cr.depth<max_crawldepth)
			defaultPolicy.setMaxRequestsPerConnection(max_requests_per_run);
		else
			defaultPolicy.setMaxRequestsPerConnection(cr.configuration.getInt("fetcher.max_requests_per_run.value"));

		defaultPolicy.setMaxConnectionsPerHost(cr.configuration.getInt("fetcher.max_connections_per_host.value"));
		defaultPolicy.setMinResponseRate(cr.configuration.getInt("fetcher.min_response_rate.value"));
		defaultPolicy.setMaxRedirects(cr.configuration.getInt("fetcher.max_redirects.value"));
		defaultPolicy.setMaxContentSize(cr.configuration.getInt("fetcher.max_content_size.value"));
		//defaultPolicy.setRedirectMode(RedirectMode.FOLLOW_TEMP);
		defaultPolicy.setRedirectMode(RedirectMode.FOLLOW_ALL);
		defaultPolicy.setValidMimeTypes(cr.validMimes);
		return defaultPolicy;
	}

	private static Path defineInputPath(FileSystem fs, Path outputDirPath,	File outputDir) {
		Path inputPath = null;
		try {
			inputPath = CrawlDirUtils.findLatestLoopDir(fs, outputDirPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (inputPath == null) {
			System.err.println("No previous cycle output dirs exist in " + outputDir.getAbsolutePath()); 
			//options.help();
		}
		return inputPath; 
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
