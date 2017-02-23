package gr.ilsp.fc.crawl;

import gr.ilsp.fc.datums.CrawlDbDatum;
import gr.ilsp.fc.operations.ILSPFCUrlNormalizer;
import gr.ilsp.fc.readwrite.ReadResources;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.JobConf;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import bixo.datum.UrlStatus;
import cascading.scheme.SequenceFile;
import cascading.tap.Hfs;
import cascading.tap.Tap;
import cascading.tuple.TupleEntryCollector;

public class CrawlerUtils {
	private static final Logger LOGGER = Logger.getLogger(CrawlerUtils.class);
	protected static Matcher skipLineM = Pattern.compile("^(\\s*)||(#.*)$").matcher("");
	private static final String QUEST_SEPAR = ";";

	private static final String LANG_KEYS_RESOURCE = "langKeys.txt" ;
	protected static String filesepar = System.getProperty("file.separator");



	/**
	 * @param 
	 * @return Create log output file in loop directory.. 
	 */
	public static String setLoopLoggerFile(String outputDirName, int loopNumber) {
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
	 * @param urls:crawler will start from these URLS, crawlDbPath: path for loops' results, conf:crawler's configuration
	 * @return initialize the frontier with the seed URL list. 
	 */
	public static void importUrlList(String urls, Path crawlDbPath, JobConf conf) throws IOException {		        
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
	 * @param targetDomain:crawler will stay in this web domain, crawlDbPath: path for loops' results, conf:crawler's configuration
	 * @return initialize the frontier with the seed URL list which contains only one URL (crawler will stay in this web domain). 
	 */
	public static void importOneDomain(String targetDomain, Path crawlDbPath, JobConf conf) throws IOException {
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
	 * @param urls:crawler will stay in this web domain, crawlDbPath: path for loops' results, conf:crawler's configuration
	 * @return initialize the frontier with the seed URL. Crawler will stay in this web domain. 
	 */
	public static void importURLOneDomain(String urls, Path crawlDbPath, JobConf conf) throws IOException {
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
	 * Checks if stored/visited pages of the current crawl cycle is different from the previous crawl cycle,
	 * @param stor_vis Array of stored and visited pages per run
	 * @return true if no new links to follow exist and crawl has to stop. 
	 */
	public static boolean check_evol(ArrayList<int[]> stor_vis) {
		boolean stop_crawl = false;
		int runs = stor_vis.size();
		if (runs>1){ //two runs at least 
			if (stor_vis.get(runs-1)[1]==stor_vis.get(runs-2)[1])
				stop_crawl=true;
		}
		return stop_crawl;
	}


	/*public static void createCSV(CrawlerOptions options, Map<String, Integer> langnumMap, String logpair) {
		File csvfile = new File(options.getBaseName()+UNDERSCORE_STR+"depthIs"+options.upToDepth()+UNDERSCORE_STR+CSV);
		String csvtext="";
		String webdomains = options.getFilter();
		if (webdomains.isEmpty())
			webdomains = options.getDomain()+"\t"+options.getMainDomain();
		csvtext = "targeted domain:\t"+webdomains+"\n";
		csvtext = csvtext+"crawled up to depth:\t"+options.upToDepth()+"\n";
		csvtext = csvtext+"minimum length of text of accepted webpages:\t"+options.getMinDocLen()+"\n";
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
	}*/

	/*private static String addSeeds(String csvtext, File seedFile) {
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
	}*/

	public static String path2str(Path curLoopDir) {
		String curLoopDirName = curLoopDir.toUri().toString();
		if (curLoopDirName.startsWith("file:/")){
			if (filesepar.equals("\\"))
				curLoopDirName = curLoopDirName.substring(6); 
			else
				curLoopDirName = curLoopDirName.substring(5);
		}
		return curLoopDirName;
	}

	public static String getTopNDir(File file, int n) {
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
	 * Returns the dirPath of the running jar
	 * @return
	 */
	public static String getRunningJarPath() {
		String runpath="";
		String path = CrawlerUtils.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		try {
			File decodedPath = new File(URLDecoder.decode(path, "UTF-8"));
			runpath= decodedPath.getParent();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return runpath;
	}


	public static String getSupportedLanguages() {
		String supportedlangs = "";
		try {
			URL svURL = ReadResources.class.getClassLoader().getResource(LANG_KEYS_RESOURCE);
			BufferedReader in = new BufferedReader(new InputStreamReader(svURL.openStream()));
			String str;
			while ((str = in.readLine()) != null) {
				supportedlangs=supportedlangs+QUEST_SEPAR+str.subSequence(0, str.indexOf(">")).toString();
			}
			in.close();
		} catch (IOException e) {
			LOGGER.error("Problem in reading the file for langKeys.");
		}
		return supportedlangs.substring(1);
	}

	public static Set<String> array2Set(String[] mimes) {
		Set<String> validMimeTypes = new HashSet<String>();
		for (String s: mimes) validMimeTypes.add(s);
		return validMimeTypes;
	}


}
