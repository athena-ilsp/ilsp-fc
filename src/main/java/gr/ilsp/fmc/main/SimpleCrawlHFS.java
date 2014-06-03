package gr.ilsp.fmc.main;

import gr.ilsp.fmc.datums.CrawlDbDatum;
import gr.ilsp.fmc.exporter.SampleExporter;
import gr.ilsp.fmc.parser.DomainUrlFilter;
import gr.ilsp.fmc.utils.BitextUtils;
import gr.ilsp.fmc.utils.CrawlConfig;
import gr.ilsp.fmc.utils.DirUtils;
import gr.ilsp.fmc.utils.JarUtils;
import gr.ilsp.fmc.utils.TopicTools;
import gr.ilsp.fmc.workflows.SimpleCrawlHFSWorkflow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.io.FileUtils;
//import org.apache.commons.io.FileUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.JobConf;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import bixo.config.FetcherPolicy;
import bixo.config.FetcherPolicy.FetcherMode;
import bixo.config.UserAgent;
import bixo.datum.UrlStatus;
import bixo.urls.BaseUrlFilter;
import bixo.urls.SimpleUrlNormalizer;
import bixo.utils.CrawlDirUtils;
import cascading.flow.Flow;
import cascading.flow.PlannerException;
import cascading.scheme.SequenceFile;
import cascading.tap.Hfs;
import cascading.tap.Tap;
import cascading.tuple.TupleEntryCollector;

import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

/**
 * JDBCCrawlTool is an example of using Bixo to write a simple crawl tool.
 * 
 * This tool uses an in-memory hsqldb to demonstrate how one could use a 
 * database to maintain the crawl db. 
 *  
 * 
 */
@SuppressWarnings("deprecation")
public class SimpleCrawlHFS {
	private static ArrayList<String[]> topic;
	private static String[] classes;
	private static final Logger LOGGER = Logger.getLogger(SimpleCrawlHFS.class);
	public static CompositeConfiguration config;
	private static int PAGES_STORED = 0;
	private static int PAGES_FAILED_CLASSIFICATION=0;
	private static int PAGES_VISITED = 0;
	private static int TOKENS_STORED = 0;
	private static int TOKENS_TARGET = 100000000;
	private static String fs1 = System.getProperty("file.separator");
	private static final String resultDir = "xml";
	private static final String tempFileExt=".xml.txt";
	private static String lang_separator=";";
	public static JobConf conf = null;

	private static String operation = "crawlandexport";

	protected static Matcher skipLineM = Pattern.compile("^(\\s*)||(#.*)$").matcher("");

	/**
	 * @param outputDirName to store the downloaded HTML, the created cesDoc or/and cesAling XML files 
	 * @return Create log output file in loop directory.. 
	 */
	private static void setLoopLoggerFile(String outputDirName, int loopNumber) {
		Logger rootLogger = Logger.getRootLogger();

		String filename = String.format("%s/%d-JDBCCrawlTool.log", outputDirName, loopNumber);
		FileAppender appender = (FileAppender) rootLogger.getAppender("loop-logger");
		if (appender == null) {
			appender = new FileAppender();
			appender.setName("loop-logger");
			appender.setLayout(new PatternLayout("%d{yy/MM/dd HH:mm:ss} %p %c{2}:%L - %m%n"));
			// We have to do this before calling addAppender, as otherwise Log4J warns us.
			appender.setFile(filename);
			appender.activateOptions();
			rootLogger.addAppender(appender);
		} else {
			appender.setFile(filename);
			appender.activateOptions();
		}
	}

	/**
	 * @param targetDomain:crawler will stay in this web domain, crawlDbPath: path for loops' results, conf:crawler's configuration
	 * @return initialize the frontier with the seed URL list which contains only one URL (crawler will stay in this web domain). 
	 */
	private static void importOneDomain(String targetDomain, Path crawlDbPath, JobConf conf) throws IOException {
		try {
			Tap urlSink = new Hfs(new SequenceFile(CrawlDbDatum.FIELDS), crawlDbPath.toUri().toString(), true);
			TupleEntryCollector writer = urlSink.openForWrite(conf);			
			SimpleUrlNormalizer normalizer = new SimpleUrlNormalizer();
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
			SimpleUrlNormalizer normalizer = new SimpleUrlNormalizer();            
			BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream(urls),"utf8"));
			String line = "";
			//UrlValidator urlValidator = new UrlValidator(UrlValidator.NO_FRAGMENTS);
			ArrayList<String> seedUrls = new ArrayList<String>();
			while ((line=rdr.readLine())!=null){
				if (skipLineM.reset(line).matches()) 
					continue;
				byte[] bts = line.trim().getBytes("UTF-8");
				if (bts[0] == (byte) 0xEF && bts[1] == (byte) 0xBB && bts[2]==(byte) 0xBF) {
					byte[] bts2 = new byte[bts.length-3];
					for (int i = 3; i<bts.length;i++)
						bts2[i-3]=bts[i];
					line = new String(bts2);
				}
				if (seedUrls.contains(line))
					continue;
				else
					seedUrls.add(line);
				if (line.equals("") || line.startsWith("ftp") || line.equals("http://")) continue;
				//if (!urlValidator.isValid(line) && !line.contains("#")) continue;

				CrawlDbDatum datum = new CrawlDbDatum(normalizer.normalize(line), 0, 0, 
						UrlStatus.UNFETCHED, 0,0.0);
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
			SimpleUrlNormalizer normalizer = new SimpleUrlNormalizer();
			//CrawlDbDatum datum = new CrawlDbDatum(normalizer.normalize("http://" + targetDomain), 0, 0, UrlStatus.UNFETCHED, 0,0.0);
			BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream(urls),"utf8"));
			String line = "";
			//UrlValidator urlValidator = new UrlValidator(UrlValidator.NO_FRAGMENTS);
			ArrayList<String> seedUrls = new ArrayList<String>();
			while ((line=rdr.readLine())!=null){
				if (skipLineM.reset(line).matches()) 
					continue;
				byte[] bts = line.getBytes("UTF-8");
				if (bts[0] == (byte) 0xEF && bts[1] == (byte) 0xBB && bts[2]==(byte) 0xBF) {
					byte[] bts2 = new byte[bts.length-3];
					for (int i = 3; i<bts.length;i++)
						bts2[i-3]=bts[i];
					line = new String(bts2);
				}
				if (seedUrls.contains(line))
					continue;
				else
					seedUrls.add(line);
				if (line.equals("") || line.startsWith("ftp") || line.equals("http://")) continue;
				//if (!urlValidator.isValid(line)&& !line.contains("#")) continue;

				CrawlDbDatum datum = new CrawlDbDatum(normalizer.normalize(line), 0, 0, 
						UrlStatus.UNFETCHED, 0,0.0);
				writer.add(datum.getTuple());
			}
			LOGGER.info("Starting from "+ seedUrls.size()+ " URLs");
			rdr.close();
			writer.close();
		} catch (IOException e) {
			throw e;
		}
	}	

	public static void main(String[] args) {

		if (args.length==0){
			LOGGER.info("Usage: SimpleCrawlHFS [crawl|export|config]");
			System.exit(-1);
		}

		if (helpAsked(args)){
			try {
				crawl(args);
			} catch (IOException e) {
				LOGGER.error(e.getMessage());
			}
		}

		operation = args[0].toLowerCase();
		if (operation.equals("export")){
			SampleExporter.main(args);
		} else if (operation.equals("config")){
			if (args.length>1){
				String out = args[1];			
				URL default_config = SimpleCrawlHFS.class.getClassLoader().getResource("crawler_config.xml");
				XMLConfiguration xml;
				try {
					xml = new XMLConfiguration(default_config);
					xml.save(out);
					LOGGER.info("Saved default config file at " + out);
				} catch (ConfigurationException e) {
					// Shouldn't happen
					LOGGER.error("Couldn't save file " + out);
				}			
			} else LOGGER.error("Usage: SimpleCrawlHFS config <file to save config xml>");
		} else if (operation.equals("crawl") || operation.equals("crawlandexport")) {
			try {
				crawl(args);
			} catch (IOException e) {
				LOGGER.error(e.getMessage());
			}
		} else {
			LOGGER.error("Invalid operation.");
			System.exit(-1);
		}
	}

	private static boolean helpAsked(String[] args) {

		for (int ii=0; ii<args.length;ii++){
			if (args[ii].equals("-h") | args[ii].equals("-help") | args[ii].equals("--help") | args[ii].equals("--h")){
				return true;
			}		
		}
		return false;
	}

	private static void crawl(String[] args) throws IOException {
		SimpleCrawlHFSOptions options = new SimpleCrawlHFSOptions();
		options.parseOptions(args);		
			
		//Loading the default configuration file and checking if user supplied a custom one.
		URL default_config;
		if (options.getType().equals("p"))
			default_config = SimpleCrawlHFS.class.getClassLoader().getResource("FBC_config.xml");			
		else
			default_config = SimpleCrawlHFS.class.getClassLoader().getResource("FMC_config.xml");
		
		config = new CompositeConfiguration();			
		if (options.getConfig()!=null){
			String custom_config = options.getConfig();
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
		conf = new JobConf();
		conf.setJarByClass(SimpleCrawlHFS.class);
		//Added this for concurrency issues
		conf.set("hadoop.tmp.dir", conf.get("hadoop.tmp.dir")+UUID.randomUUID().toString());
		conf.set("mapred.dir",conf.get("hadoop.tmp.dir") + fs1+"mapred");
		conf.set("mapred.local.dir",conf.get("mapred.dir") + fs1+"local");
		conf.set("mapred.system.dir",conf.get("mapred.dir") + fs1+"system");		

		FileSystem fs;
		//if domain is supplied, it is checked for errors
		String domain = options.getDomain();
		String urls = null;
		boolean isDomainFile = true;
		Path dompath = null;
		if (domain==null) {
			urls = options.getUrls();
			isDomainFile = false;
		}
		else {
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

		if (options.getType().equals("p")) 
			urls = options.getUrls();
		if (options.getDomain()!=null) 
			urls = options.getUrls();
		//String[][] tttt =options.getUrlReplaces();
		URL urldir = SimpleCrawlHFS.class.getResource("/profiles");
		LOGGER.debug(urldir );
		if (urldir.getProtocol()=="jar"){
			File tempDir = DirUtils.createTempDir();
			LOGGER.debug(tempDir );
			JarUtils.copyResourcesRecursively(urldir, tempDir);
			try {
				DetectorFactory.loadProfile(tempDir);				
			} catch (LangDetectException e1) {
				LOGGER.error(e1.getMessage());
			} 
		} else {
			try {
				DetectorFactory.loadProfile(new File(urldir.toURI()));
			} catch (LangDetectException e) {
				LOGGER.error(e.getMessage());
			} catch (URISyntaxException e) {
				LOGGER.error(e.getMessage());
			}
		}

		int min_uniq_terms = SimpleCrawlHFS.config.getInt("classifier.min_unique_content_terms.value");
		double rel_thres = SimpleCrawlHFS.config.getDouble("classifier.relative_relevance_threshold.value");
		int max_depth = SimpleCrawlHFS.config.getInt("classifier.max_depth.value");//value for tunneling
		//Analyze the topic and create the classes and topic variables. Term threshold
		//is calculate based on the median weight of the terms and the minimum number
		//of terms each text must have as defined on the config file.
		double abs_thres = 0;
		if (options.getTopic()!=null){
			topic=TopicTools.analyzeTopic(options.getTopic(),options.getLanguage(), conf);
			if (topic.isEmpty()){
				LOGGER.error("Even though a file for topic definition is defined, no terms have been found (check the file for existance and/or format).");
				System.exit(0);
			}
			LOGGER.info("Topic analyzed, " + topic.size() + " terms found.");
			//find the subclasses of the topic definition
			classes=TopicTools.findSubclasses(topic);
			LOGGER.info(classes.length + " classes found.");
			abs_thres = TopicTools.calculateThreshold(topic,
					SimpleCrawlHFS.config.getInt("classifier.min_content_terms.value"));
			LOGGER.info("Classifier threshold calculated: "+ abs_thres);
		} else{ 
			LOGGER.info("Running with no topic.");
			min_uniq_terms=0;
			rel_thres=0;
		}

		String outputDirName = options.getOutputDir();
		if (options.isDebug()) 
			System.setProperty("fmc.root.level", "DEBUG");            
		else 
			System.setProperty("fmc.root.level", "ERROR");
		//System.setProperty("fmc.root.level", "INFO");

		if (options.getLoggingAppender() != null) 
			System.setProperty("fmc.appender", options.getLoggingAppender()); // Set console vs. DRFA vs. something else

		try {
			//JobConf conf = new JobConf();
			//conf.setJarByClass(SimpleCrawlHFS.class);
			Path outputPath = new Path(outputDirName);				
			fs = outputPath.getFileSystem(conf);
			outputPath = outputPath.makeQualified(fs);
			//If force is used, the outputPath will be deleted if it already exists
			if (options.Force() && fs.exists(outputPath)){
				LOGGER.warn("Removing previous crawl data in " + outputPath);
				fs.delete(outputPath);
			}
			//If the outputPath does not exist, it is created. Seed URL list (or domain) is imported into the hfs.
			if (!fs.exists(outputPath)) {
				LOGGER.info("Creating path: " +outputPath.toString());
				fs.mkdirs(outputPath);

				Path curLoopDir = CrawlDirUtils.makeLoopDir(fs, outputPath, 0);
				Path pdf_dir =new Path(outputPath.toString()+fs1+"pdf"); 
				if (!fs.exists(pdf_dir)){
					fs.mkdirs(pdf_dir);
				}

				//fs.deleteOnExit(curLoopDir);

				String curLoopDirName = curLoopDir.toUri().toString();
				if (curLoopDirName.startsWith("file:/"))
					curLoopDirName = curLoopDirName.substring(5); 

				setLoopLoggerFile(curLoopDirName, 0);
				Path crawlDbPath = new Path(curLoopDir, CrawlConfig.CRAWLDB_SUBDIR_NAME);
				if (domain!=null && !isDomainFile){
					if (urls!=null)
						importURLOneDomain(urls,crawlDbPath , conf);
					else
						importOneDomain(domain,crawlDbPath , conf);
				}
				else
					importUrlList(urls,crawlDbPath, conf);
			} 
			//The last run folder is detected (in case we are resuming a previous crawl)
			Path inputPath = CrawlDirUtils.findLatestLoopDir(fs, outputPath);
			if (inputPath == null) {
				System.err.println("No previous cycle output dirs exist in " + outputDirName);
				//printUsageAndExit(parser);
				options.help();
			}
			//CrawlDbPath is the path where the crawl database will be stored for the current run
			Path crawlDbPath = new Path(inputPath, CrawlConfig.CRAWLDB_SUBDIR_NAME);

			//Start and end loop numbers are calculated (if the crawl is running on a fixed number of loops)
			int startLoop = CrawlDirUtils.extractLoopNumber(inputPath);
			int endLoop = startLoop + options.getNumLoops();

			UserAgent userAgent = new UserAgent(options.getAgentName(), config.getString("agent.email"), config.getString("agent.web_address"));

			//Fetch policy configuration
			FetcherPolicy defaultPolicy = new FetcherPolicy();
			defaultPolicy.setCrawlDelay(config.getLong("fetcher.crawl_delay.value"));
			defaultPolicy.setFetcherMode(FetcherMode.EFFICIENT);
			defaultPolicy.setRequestTimeout(config.getLong("fetcher.request_timeout.value"));
			defaultPolicy.setMaxRequestsPerConnection(config.getInt("fetcher.max_requests_per_run.value"));
			defaultPolicy.setMaxConnectionsPerHost(config.getInt("fetcher.max_connections_per_host.value"));
			defaultPolicy.setMinResponseRate(config.getInt("fetcher.min_response_rate.value"));
			defaultPolicy.setMaxRedirects(config.getInt("fetcher.max_redirects.value"));
			defaultPolicy.setMaxContentSize(config.getInt("fetcher.max_content_size.value"));
			//Loading of acceptable MIME types from the config file
			String[] mimes = config.getStringArray("fetcher.valid_mime_types.mime_type[@value]");			
			Set<String> validMimeTypes = new HashSet<String>();
			for (String s: mimes) validMimeTypes.add(s);
			defaultPolicy.setValidMimeTypes(validMimeTypes);
			int crawlDurationInMinutes = options.getCrawlDuration();
			//hasEndTime is the time the crawl must end (if crawl is running on specified duration)
			boolean hasEndTime = crawlDurationInMinutes != SimpleCrawlHFSOptions.NO_CRAWL_DURATION;
			long targetEndTime = hasEndTime ? System.currentTimeMillis() + 	(crawlDurationInMinutes * 60000L) : FetcherPolicy.NO_CRAWL_END_TIME;
			//Setting up the URL filter. If domain is supplied, the filter will disregard all
			//URLs that do not belong in the specified web domain.
			BaseUrlFilter urlFilter = null;
			if (isDomainFile)
				urlFilter = new DomainUrlFilter(dompath);
			else
				urlFilter = new DomainUrlFilter(domain);

			// Main loop. This will run as many times as specified by the numloop option
			//or until the specified duration is reached
			long startTime = System.currentTimeMillis();

			ArrayList<int[]> stor_vis = new ArrayList<int[]>();
			for (int curLoop = startLoop + 1; curLoop <= endLoop; curLoop++) {
				// Checking if duration is expired. If so, crawling is terminated.
				if (hasEndTime) {
					long now = System.currentTimeMillis();
					if (targetEndTime-now<=0 || TOKENS_STORED>TOKENS_TARGET){
						LOGGER.info("Time expired or target tokens amount reached, ending crawl.");
						long duration = System.currentTimeMillis()-startTime;
						LOGGER.info("Made " + (curLoop-startLoop-1) + " runs in " + 
								(System.currentTimeMillis()-startTime) + " milliseconds.");
						float avg = (float)duration/(curLoop-startLoop-1);
						LOGGER.info("Total pages stored/visited: " + PAGES_STORED + "/" + PAGES_VISITED);
						LOGGER.info("Total pages failed classification or are too short: " + PAGES_FAILED_CLASSIFICATION );
						LOGGER.info("Total tokens stored: " + TOKENS_STORED);
						LOGGER.info("Average run time: " + avg + " milliseconds.");						
						break;
					}
					//If duration is not reached, endLoop is increased to run the next loop
					endLoop = curLoop + 1;
				}
				//checking if nums of stored/visited have changed. If not, crawling is terminated.
				if (check_evol1(stor_vis)){
					LOGGER.info("Seed page was not visited. So no new links to follow");
					System.exit(64);
				}
				if (check_evol(stor_vis)){
					LOGGER.info("No new links to follow, ending crawl.");
					long duration = System.currentTimeMillis()-startTime;
					LOGGER.info("Made " + (curLoop-startLoop-1) + " runs in " + 
							(System.currentTimeMillis()-startTime) + " milliseconds.");
					float avg = (float)duration/(curLoop-startLoop-1);
					LOGGER.info("Total pages stored/visited: " + PAGES_STORED + "/" + PAGES_VISITED);
					LOGGER.info("Total pages failed classification or are too short: " + PAGES_FAILED_CLASSIFICATION );
					LOGGER.info("Total tokens stored: " + TOKENS_STORED);
					LOGGER.info("Average run time: " + avg + " milliseconds.");						
					break;
				}
				//The workflow is created and launched. Each flow is created using the directory of the
				//current loop, the crawl db directory, the policy for the Fetcher, the URL filter, the
				//topic and classes arrays, the term threshold and the crawl options
				Path curLoopDir = CrawlDirUtils.makeLoopDir(fs, outputPath, curLoop);

				//fs.deleteOnExit(curLoopDir);

				String curLoopDirName = curLoopDir.toUri().toString();
				if (curLoopDirName.startsWith("file:/"))
					curLoopDirName = curLoopDirName.substring(5); 
				setLoopLoggerFile(curLoopDirName, curLoop);	
				LOGGER.debug( conf.toString());
				for(int il =0; il<conf.getLocalDirs().length;il++) 
					LOGGER.debug(conf.getLocalDirs()[il]);

				//System.out.println("LOOP "+ Integer.toString(curLoop));
				Flow flow = SimpleCrawlHFSWorkflow.createFlow(curLoopDir, crawlDbPath, userAgent, defaultPolicy, urlFilter, 
						classes, topic, abs_thres,rel_thres, min_uniq_terms,max_depth,options);							
				flow.complete();
				//LOGGER.info("Hadoop tmp dir = " + conf.get("hadoop.tmp.dir"));

				//Reseting counters of parent class. We do it here so that SplitFetchedUnfetchedCrawlDatums
				//when run again will not return the 256(or whatever) that were selected in the first run
				SimpleCrawlHFSWorkflow.resetCounters();
				if (curLoop>3) 
					DirUtils.clearPreviousLoopDir(fs,outputPath,curLoop);

				LOGGER.info("Total pages stored/visited: " + PAGES_STORED + "/" + PAGES_VISITED);
				LOGGER.info("Total pages failed classification or are too short : " + PAGES_FAILED_CLASSIFICATION );
				LOGGER.info("Total tokens stored: " + TOKENS_STORED);
				stor_vis.add(new int[] {PAGES_STORED,PAGES_VISITED});
				// flow.writeDOT("build/valid-flow.dot");

				//fs.deleteOnExit(curLoopDir);
				// Input for the next round is our current output
				crawlDbPath = new Path(curLoopDir, CrawlConfig.CRAWLDB_SUBDIR_NAME);
				//inputPath = curLoopDir;
			}
			// Finished crawling. Now export if needed.

			if (operation.equals("crawlandexport")) {
				SampleExporter se = new SampleExporter();
				se.setMIN_TOKENS_PER_PARAGRAPH(options.getlength());
				se.setMIN_TOKENS_NUMBER(options.getminTokenslength());
				se.setLanguage(options.getLanguage());
				se.setCrawlDirName(outputDirName);
				se.setOutputFile(options.getOutputFile());	
				se.setTopic(options.getTopic());
				se.setStyleExport(options.getAlign()); 
				se.setOutputFileHTML(options.getOutputFileHTML());
				se.setHTMLOutput(options.getOutputFileHTML()!=null);
				se.setApplyOfflineXSLT(options.isOfflineXSLT());
				se.setAcceptedMimeTypes(mimes);
				se.setTargetedDomain(options.getTargetedDomain());
				se.setGenres(options.getGenre());
				se.export(false);
			}

			// Finished exporting. Now remove (near) duplicates
			LOGGER.info("Deduplication by using lists and MD5 method.");
			DedupMD5.dedup(outputDirName, options.getOutputFile(),options.getOutputFileHTML(),
					options.isOfflineXSLT());
			LOGGER.info("Deduplication based on common paragraphs.");
			DedupMD5.dedupnew(outputDirName, options.getOutputFile(), options.getOutputFileHTML(),
					options.isOfflineXSLT());

			//Detect candidate parallel documents if crawled for parallel.
			File parentDir =new File(outputDirName + fs1 + resultDir); 
			if (options.getType().equals("p")){	
				try {				
					File xmldir = new File(options.getOutputDir()+fs1+resultDir);

					HashMap<String,String[]> props=Bitexts.representXML_NEW(xmldir);
					HashMap<String,String[]> props_short = new HashMap<String,String[]>();

					if (props.size()<2){
						LOGGER.info("Less than 2 files found. Detection of pairs is stopped.");
						File out_temp=new File(options.getOutputFile());
						if (out_temp.exists())
							out_temp.delete();
						System.exit(0);
					}
					int l1=0, l2=0;
					String[] lang=options.getLanguage().split(lang_separator);

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
						File out_temp=new File(options.getOutputFile());
						if (out_temp.exists())
							out_temp.delete();
						System.exit(0);
					}
					if (l2==0){
						LOGGER.info("No file found in " + lang[1]+"; Detection of pairs is stopped.");
						File out_temp=new File(options.getOutputFile());
						if (out_temp.exists())
							out_temp.delete();
						System.exit(0);
					}
					ArrayList<String[]> bitextsALL=new ArrayList<String[]>();
					String[] stats1=new String[4];
					//Find pairs based on URLs
					ArrayList<String[]> bitextsURLs=new ArrayList<String[]>();
					HashMap<String, String> filesURLS = Bitexts.findURLs(xmldir);

					//String[][] _url_replaces = Bitexts.checkUrls(options.getUrlReplaces());
					bitextsURLs=Bitexts.findpairsURLs(filesURLS,props,options.getUrlReplaces());
					//bitextsURLs=Bitexts.findpairsURLs_dist(filesURLS,props);
					if (bitextsURLs.size()>0){
						LOGGER.info(bitextsURLs.size()+ " pairs found (based on URLs).");
						Bitexts.writeXMLs(outputDirName,bitextsURLs,options.getAlign(),options.isOfflineXSLT());
						props_short = Bitexts.excludepairsIM(bitextsURLs,props);
						for (int ii=0;ii<bitextsURLs.size();ii++)
							bitextsALL.add(bitextsURLs.get(ii));
						stats1=Bitexts.calcStats2(props,bitextsURLs,"u");
						LOGGER.info("Tokens in "+stats1[0] +" : "+ stats1[1]);
						LOGGER.info("Tokens in "+stats1[2] +" : "+ stats1[3]);
						LOGGER.info(props_short.size()+ " files still remained for pair detection.");
					}else{
						LOGGER.info("No pairs found (based on URLs)");
						props_short=props;
					}

					//Find pairs based on common images
					ArrayList<String[]> bitextsIM=new ArrayList<String[]>();
					HashMap<String, String[]> imagesInHTML=ImageExtractor.findImages(xmldir,options.getImpath());
					if (imagesInHTML.size()>1){
						bitextsIM=Bitexts.findpairsIM(imagesInHTML,props_short);
						if (bitextsIM.size()>0){
							LOGGER.info(bitextsIM.size()+ " pairs found (based on images).");
							Bitexts.writeXMLs(outputDirName,bitextsIM,options.getAlign(),options.isOfflineXSLT());
							props_short = Bitexts.excludepairsIM(bitextsIM,props_short);
							LOGGER.info(props_short.size()+ " files still remained for pair detection.");
							for (int ii=0;ii<bitextsIM.size();ii++)
								bitextsALL.add(bitextsIM.get(ii));
							stats1=Bitexts.calcStats2(props,bitextsIM,"im");
							LOGGER.info("Tokens in "+stats1[0] +" : "+ stats1[1]);
							LOGGER.info("Tokens in "+stats1[2] +" : "+ stats1[3]);
						}else
							LOGGER.info("No pairs found (based on images)");
					}

					//Find pairs based on similar structures
					double[][] sv=Bitexts.readRes("SVs19_last.txt");
					double[][] b=Bitexts.readRes("B19_last.txt");
					double[][] w=Bitexts.readRes("Ws19_last.txt");

					ArrayList<String[]> pairs_new  = Bitexts.findpairsXML_SVM_NEW(xmldir,props_short,sv,w,b);
					ArrayList<String[]> bitextsSTRUCT = Bitexts.findBestPairs_SVM_NEW(pairs_new);
					if (bitextsSTRUCT.size()>0){
						int counter_h=0, counter_m=0, counter_l=0;
						for (int kk=0;kk<bitextsSTRUCT.size();kk++){
							if (bitextsSTRUCT.get(kk)[4].equals("high")) {
								counter_h++;
								continue;
							}
							if (bitextsSTRUCT.get(kk)[4].equals("medium")){
								counter_m++;
								continue;
							}
							if (bitextsSTRUCT.get(kk)[4].equals("low"))
								counter_l++;
						}
						Bitexts.writeXMLs(outputDirName,bitextsSTRUCT,options.getAlign(),options.isOfflineXSLT());
						LOGGER.info("Pairs found (based on structure) : "+bitextsSTRUCT.size());

						LOGGER.info("(with high conf) : " + counter_h);
						if (counter_h>0){
							stats1=Bitexts.calcStats2(props,bitextsSTRUCT,"high");
							LOGGER.info("Tokens in "+stats1[0] +" : "+ stats1[1]);
							LOGGER.info("Tokens in "+stats1[2] +" : "+ stats1[3]);
						}
						LOGGER.info("(with medium conf) : " + counter_m);
						if (counter_m>0){
							stats1=Bitexts.calcStats2(props,bitextsSTRUCT,"medium");
							LOGGER.info("Tokens in "+stats1[0] +" : "+ stats1[1]);
							LOGGER.info("Tokens in "+stats1[2] +" : "+ stats1[3]);
						}
						LOGGER.info("(with low conf) :  "+ counter_l);
						if (counter_l>0){
							stats1=Bitexts.calcStats2(props,bitextsSTRUCT,"low");
							LOGGER.info("Tokens in "+stats1[0] +" : "+ stats1[1]);
							LOGGER.info("Tokens in "+stats1[2] +" : "+ stats1[3]);
						}
						for (int ii=0;ii<bitextsSTRUCT.size();ii++)
							bitextsALL.add(bitextsSTRUCT.get(ii));
					}else
						LOGGER.info("No pairs found (based on structure)");

					//Total results
					bitextsALL = Bitexts.sortbyLength(bitextsALL);
					Bitexts.writeOutList(outputDirName,options.getOutputFile(),options.getOutputFileHTML(),bitextsALL,options.getDest());
					LOGGER.info("Total pairs found: "+ bitextsALL.size());
					String[] stats=Bitexts.calcStats1(props,bitextsALL);
					if (stats!=null){
						LOGGER.info("Tokens in "+stats[0] +" : "+ stats[1]);
						LOGGER.info("Tokens in "+stats[2] +" : "+ stats[3]);
					}
					if (options.toAlign()){
						//FIXME aligner to be added.
					}
					BitextUtils.removeTempFiles(parentDir,tempFileExt);
					BitextUtils.removeRedundantFiles(parentDir, bitextsALL);

				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				} catch (XMLStreamException e1) {
					e1.printStackTrace();
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
			//crawl for comparable
			if (options.getType().equals("q")){
				//put Nikos module based on aligned topics
			}
			//crawled for monolingual
			if (options.getType().equals("m")){
				File input1=null;
				String temp = outputDirName+fs1+resultDir;
				int tempid=temp.indexOf(":");
				if (tempid<0 || tempid<2)
					input1= new File(temp);
				else
					input1= new File(temp.substring(tempid+2, temp.length()));

				FilenameFilter filter = new FilenameFilter() {			
					public boolean accept(File arg0, String arg1) {
						return (arg1.substring(arg1.length()-4).equals(".xml"));
					}
				};
				File[] files=input1.listFiles(filter);
				String text="";
				int total_tokens=0;
				for (int ii=0;ii<files.length;ii++){
					text = ReadResources.extractTextfromXML_clean
							(files[ii].getAbsolutePath(),"p","crawlinfo", false);
					StringTokenizer tkzr = new StringTokenizer(text);
					int length_in_tok=tkzr.countTokens();
					total_tokens=total_tokens+length_in_tok;
				}
				LOGGER.info("Total tokens: "+ total_tokens);
			}

			File hadoopTempFile=new File(conf.get("hadoop.tmp.dir"));
			FileUtils.deleteDirectory(hadoopTempFile);

			//Delete every dir created for each run
			List<String> notToBeDeleted= new ArrayList<String>();
			notToBeDeleted.add(resultDir);
			DirUtils.deleteLoopDirs(fs, outputPath, notToBeDeleted);
			fs.close();
			
			renamePaths(options.getAgentName(), outputDirName,	options.getOutputFile(),
					options.getOutputFileHTML(), options.getPathReplace());
			
			System.exit(0);
		} catch (PlannerException e) {
			LOGGER.debug(conf.get("hadoop.tmp.dir"));			
			e.writeDOT("failed-flow.dot");
			System.err.println("PlannerException: " + e.getMessage());
			e.printStackTrace(System.err);
			System.exit(-1);
		} catch (Throwable t) {
			System.err.println(conf.get("hadoop.tmp.dir"));
			System.err.println("Exception running tool: " + t.getMessage());
			t.printStackTrace(System.err);
			System.exit(-1);
		}
	} 

	private static void renamePaths(String agentName, String outputDirName, String outputFile,
			String outputHtmlFile, String repl_paths) {
		File tempfile = new File(outputFile);
		String tobematched = tempfile.getParent().replace("\\","/");
		String temp;
		if (outputDirName.replace("\\","/").contains(tobematched)){
			try {
				temp = ReadResources.readFileAsString(outputFile);
				if (temp.startsWith(agentName+"_")){
					repl_paths=(repl_paths.trim()+"\\"+agentName+"_").replace("\\","/");
					tobematched = agentName+"_";
				}
				if (repl_paths!=null){
					temp = temp.replace(tobematched, repl_paths.trim());
				}else{
					temp = temp.replace((tobematched+fs1).replace("\\","/"), "");
				}
				ReadResources.writetextfile(outputFile,temp.replace("\\", "/"));
				temp = ReadResources.readFileAsString(outputHtmlFile);
				if (repl_paths!=null){
					temp = temp.replace(tobematched, repl_paths.trim());
				}else{
					temp = temp.replace((tobematched+fs1).replace("\\","/"), "");
				}
				ReadResources.writetextfile(outputHtmlFile,temp.replace("\\","/"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param stor_vis Array of stored and visited pages per run
	 * @return true if first run failed to visit the seed URLs. 
	 */
	private static boolean check_evol1(ArrayList<int[]> stor_vis) {
		boolean stop_crawl = false;
		int runs = stor_vis.size();
		if (runs==1) {
			if (stor_vis.get(0)[1] == 0 ) {
				stop_crawl=true;				
			}
		}
		return stop_crawl;
	}

	/**
	 * @param stor_vis Array of stored and visited pages per run
	 * @return true if no new links to follow exist and crawl has to stop. 
	 */
	private static boolean check_evol(ArrayList<int[]> stor_vis) {
		boolean stop_crawl = false;
		int runs = stor_vis.size();
		if (runs>1){ //two runs at least 
			if (stor_vis.get(runs-1)[1]==stor_vis.get(runs-2)[1]){
				stop_crawl=true;
			}
		}
		return stop_crawl;
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


}