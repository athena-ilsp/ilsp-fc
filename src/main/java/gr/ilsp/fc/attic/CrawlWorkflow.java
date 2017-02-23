package gr.ilsp.fc.attic;

//import gr.ilsp.fc.attic.SimpleCrawlWorkflow;
import gr.ilsp.fc.classifier.Classifier;
import gr.ilsp.fc.crawl.Crawler;
import gr.ilsp.fc.datums.ClassifierDatum;
import gr.ilsp.fc.datums.CrawlDbDatum;
import gr.ilsp.fc.datums.ExtendedParsedDatum;
import gr.ilsp.fc.datums.ExtendedUrlDatum;
//import gr.ilsp.fc.crawl.Crawl;
import gr.ilsp.fc.crawl.CrawlerOptions;
import gr.ilsp.fc.operations.CreateCrawlDbDatumFromUrlFunction;
import gr.ilsp.fc.operations.CreateUrlDatumFromCrawlDbFunction;
import gr.ilsp.fc.operations.CreateUrlDatumFromStatusFunction;
import gr.ilsp.fc.operations.ExtendedNormalizeUrlFunction;
import gr.ilsp.fc.operations.ILSPFCUrlNormalizer;
import gr.ilsp.fc.operations.MakeDistinctCrawlDbFunction;
import gr.ilsp.fc.operations.SelectUrlOnlyFunction;
import gr.ilsp.fc.parser.ExtendedUrlFilter;
import gr.ilsp.fc.parser.RobotRulesParser;
import gr.ilsp.fc.parser.SimpleNoLinksParser;
import gr.ilsp.fc.pipes.ClassifierPipe;
import gr.ilsp.fc.pipes.ExtendedParsePipe;




//import java.io.File;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
//import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.JobConf;

import bixo.config.DefaultFetchJobPolicy;
import bixo.config.FetcherPolicy;
import bixo.config.UserAgent;
import bixo.datum.FetchedDatum;
import bixo.datum.StatusDatum;
import bixo.datum.UrlStatus;
import bixo.fetcher.BaseFetcher;
import bixo.fetcher.SimpleHttpFetcher;
import bixo.operations.BaseScoreGenerator;
import bixo.operations.FixedScoreGenerator;
import bixo.pipes.FetchPipe;
import bixo.robots.RobotUtils;
import bixo.urls.BaseUrlFilter;
import cascading.flow.Flow;
import cascading.flow.FlowConnector;
import cascading.pipe.CoGroup;
import cascading.pipe.Each;
import cascading.pipe.Every;
import cascading.pipe.GroupBy;
import cascading.pipe.Pipe;
import cascading.pipe.cogroup.LeftJoin;
import cascading.pipe.cogroup.RightJoin;
import cascading.scheme.SequenceFile;
import cascading.tap.Hfs;
import cascading.tap.Tap;
import cascading.tuple.Fields;
import cascading.tuple.TupleEntry;

import com.bixolabs.cascading.BaseSplitter;
import com.bixolabs.cascading.HadoopUtils;
import com.bixolabs.cascading.SplitterAssembly;


@SuppressWarnings("deprecation")
public class CrawlWorkflow {
	//private static final Logger LOGGER = Logger.getLogger(CrawlWorkflow.class);
	//BUFFER_SIZE represents maximum number of URLs per run
	//private static final int BUFFER_SIZE = Crawl.config.getInt("fetcher.fetch_buffer_size.value");
	private static int BUFFER_SIZE = Crawler.configuration.getInt("fetcher.fetch_buffer_size.value");
	
	private static final String type_p="p";
	//_numSelected is used as a counter for URLs selected for this run
	private static long _numSelected = 0;
	private static int _level;
	private static int _max_depth;
	private static int _upToDepth;
	private static String _urlfilterstr;
	private static String _storeFilter;
	private static String _inithost;
	private static String _mainhost;
	private static String _targlang;
	private static String _type;
	
	//private static int _minTokensNumber;
	//hostsMap and newHostsMap represent the pairs of hosts-occurences globally and per-run respectively
	private static HashMap<Integer,Integer> hostsMap = new HashMap<Integer,Integer>();
	private static HashMap<Integer,Integer> hostsIpMap = new HashMap<Integer,Integer>();
	//statusSet contains all different status levels that are considered equal when selecting URLs to fetch
	private static final HashSet<String> statusSet = new HashSet<String>(Arrays.asList(new String[] {
			"SKIPPED_DEFERRED",
			"SKIPPED_BY_SCORER",
			"SKIPPED_BY_SCORE",
			"SKIPPED_TIME_LIMIT",
			"SKIPPED_INTERRUPTED",
			"SKIPPED_INEFFICIENT",
			"SKIPPED_PER_SERVER_LIMIT",
			"UNFETCHED"
	}));

	//This method basically selects the BUFFER_SIZE number of URLs to be visited in this run
	private static class SplitFetchedUnfetchedCrawlDatums extends BaseSplitter {		
		private static final long serialVersionUID = -5255131937144107833L;
		private static final int urlsPerServer = Crawler.configuration.getInt("fetcher.max_fetched_per_host.value"); 
		//private static final int urlsPerServerPerRun = Crawl.config.getInt("fetcher.max_requests_per_host_per_run.value"); 
		private static int urlsPerServerPerRun = Crawler.configuration.getInt("fetcher.max_requests_per_host_per_run.value");

		@Override
		public String getLHSName() {
			return "fetched unfetched UrlDatums";
		}                

		@Override
		public boolean isLHS(TupleEntry tupleEntry) {
			if (_upToDepth<100){
				BUFFER_SIZE = 100000;
				urlsPerServerPerRun = 100000;
			}
			if (_numSelected>=BUFFER_SIZE) 
				return false;
			CrawlDbDatum datum = new CrawlDbDatum(tupleEntry);
			UrlStatus status = datum.getLastStatus(); 
			URL url = null;
			Integer count = null, ipCount = null;
			String host = null; 
			int hostIpHash = 0,hostHash = 0;
			InetAddress ipaddress = null;

			try {
				url = new URL(datum.getUrl());
				host = url.getHost();
				if (host.length()<5)
					return false;
				//force crawler stay in web site 
				if (_type.equals(type_p)){
					if (_inithost!=null && _mainhost!=null){
						String temp1 = url.getAuthority()+url.getFile();
						temp1 = temp1.substring(0,temp1.indexOf("/"));
						if (temp1.startsWith("www2") | temp1.startsWith("www5")){
							temp1=temp1.substring(5);
						}else{
							if (temp1.startsWith("www"))
								temp1=temp1.substring(4);
						}
					
						int ind2=temp1.toString().indexOf(_inithost);
						int ind3=temp1.toString().indexOf(_mainhost);
						//if (ind2>3 || ind3<0 || ind3>23)  //FIXME It was (3 instead of 23) in order to allow olny URLS like langcode.mainhost to be visited
						//	return false;
						if (ind2>0 && ind2==ind3){
							String temp2=temp1.substring(0, ind2);
							String[] langs=_targlang.split(";");
							boolean match=false;
							for (int mm=0;mm<langs.length;mm++){
								if (temp2.equals(langs[mm])){
									match=true;
									break;
								}
							}
							if (!match)
								return false;
						}
					}
				}
				if (_urlfilterstr!=null){
					String temp = url.getAuthority()+url.getFile();
					if (!temp.matches(_urlfilterstr))	
						return false;
				}
				System.out.println(url);
				hostHash = url.getHost().hashCode();				
				count = hostsMap.get(hostHash);			
			} catch (MalformedURLException e1) {
				//System.out.println("selected: "+url);
				//System.out.println("selected: "+ datum.getUrl());
				//LOGGER.info("Not valid url "+ datum.getUrl());
				return false;
				//LOGGER.error(e1.getMessage());
			} 

			if (statusSet.contains(status.name())) {
				if (count==null)
					hostsMap.put(hostHash, 1);
				else {
					count++;
					hostsMap.put(hostHash, count);
					if (count>urlsPerServer)
						return false;
				}
				try {
					ipaddress = InetAddress.getByName(host);
					hostIpHash = ipaddress.getHostAddress().hashCode();				
				} catch (UnknownHostException e){
					hostIpHash = hostHash;
				}
				ipCount = hostsIpMap.get(hostIpHash);
				if (ipCount==null)
					hostsIpMap.put(hostIpHash,1);
				else if (ipCount>=urlsPerServerPerRun){
					return false;
				}else {
					ipCount++;
					hostsIpMap.put(hostIpHash, ipCount);
				}		
				//LOGGER.info(hostIpHash + " " + url + " " + datum.getScore() + " " + datum.getLastStatus());
				_numSelected++;
				//System.out.println("SELECTED URL: "+url);
				return true;
			} else if (status == UrlStatus.FETCHED){
				if (count==null)
					hostsMap.put(hostHash, 1);
				else {
					count++;
					hostsMap.put(hostHash, count);					
				}
			}
			return false;
		}
		//System.out.println("END OF SELECTED URLS");
	}

	public static void resetCounters() {
		hostsMap = new HashMap<Integer,Integer>();
		hostsIpMap = new HashMap<Integer,Integer>();
		_numSelected = 0;
	}

	//Custom comparators. These enable grouping the URL list by Status (so that Fetched come first in order
	//for host-occurency hash map to be filled correctly. Everything else that has a status that belongs
	//in statusSet, is ordered by Score
	private static class StatusComparator implements Comparator<String>, Serializable {

		private static final String FETCHED_STR = "FETCHED";
		private static final long serialVersionUID = -5250304580989068398L;

		public int compare(String o1, String o2) {
			if (o1.equals(FETCHED_STR) && o2.equals(FETCHED_STR)) return 0;
			else if (o1.equals(FETCHED_STR)) return -1;
			else if (o2.equals(FETCHED_STR)) return 1;

			if (statusSet.contains(o1) && statusSet.contains(o2)) return 0;
			else return o1.compareTo(o2);			
		}		
	}

	private static class ScoreComparator implements Comparator<Double>, Serializable {

		private static final long serialVersionUID = -1057396789369463851L;

		public int compare(Double o1, Double o2) {			
			return o2.compareTo(o1);
		}		
	}

	/*private static class UrlComparator implements Comparator<String>, Serializable {

		private static final long serialVersionUID = -1057396789369463851L;

		public int compare(String o1, String o2) {			
			if (o1.equals(o2)) return -1;
			else return 0;
		}		
	}*/
	
	
	public static Flow createFlow(Path curWorkingDirPath, Path crawlDbPath, UserAgent userAgent, FetcherPolicy fetcherPolicy,
			BaseUrlFilter urlDomainFilter, BaseUrlFilter urlDepthFilter, HashMap<String, String> maplangs, 
			String[] targeted_langs, List<String[]> tranlistAttrs, String[] classes, ArrayList<String[]> topic, 
			double abs_thres, double rel_thres, int min_uniq_terms,int max_depth,CrawlOptions options, boolean extractLinks) throws Throwable {

		return createFlow071(curWorkingDirPath, crawlDbPath, userAgent, fetcherPolicy,
				urlDomainFilter, urlDepthFilter, maplangs, targeted_langs, tranlistAttrs,
				classes, topic, abs_thres, rel_thres, min_uniq_terms, max_depth, options, extractLinks);				
	}


	public static Flow createFlow071(Path curWorkingDirPath, Path crawlDbPath, UserAgent userAgent, FetcherPolicy fetcherPolicy,
			BaseUrlFilter urlDomainFilter, BaseUrlFilter urlDepthFilter, HashMap<String, String> maplangs, 
			String[] targeted_langs, List<String[]> tranlistAttrs, String[] classes, ArrayList<String[]> topic, double abs_thres,
			double rel_thres, int min_uniq_terms,int max_depth, CrawlOptions options, boolean extractLinks) throws Throwable {
		int maxThreads = options.getThreads();
		boolean debug = options.isDebug();
		boolean keepBoiler = options.keepBoiler();
		
		String subfilter = options.getFilter();
		String initial_host = options.getDomain();
		int minTokensNumber = options.getMinDocLen();
		_urlfilterstr=subfilter;
		_inithost = initial_host;
		_mainhost=options.getMainDomain();
		_level = options.getCrawlLevel();
		//String language = options.getLanguage();
		_targlang = options.getLanguage() ;//targeted_langs;
		_type =options.getType();
		String[] langKeys = options.getLangKeys();
		_storeFilter = options.getStoreFilter();
		_max_depth = max_depth;
		_upToDepth = options.upToDepth();
		//JobConf conf = Crawl.conf;
		JobConf conf= null;
		//System.err.println(conf.get("hadoop.tmp.dir"));

		//conf.setJarByClass(Crawl.class);
		//conf.setQuietMode(true);
		//conf.set("hadoop.tmp.dir", "hadoop-temp");
		int numReducers = conf.getNumReduceTasks() * HadoopUtils.getTaskTrackers(conf);
		Properties props = HadoopUtils.getDefaultProperties(CrawlWorkflow.class, debug, conf);

		FileSystem fs = curWorkingDirPath.getFileSystem(conf);
		//System.err.println(conf.get("hadoop.tmp.dir"));

		if (!fs.exists(crawlDbPath)) {
			throw new IllegalStateException(String.format("Input directory %s doesn't exist", crawlDbPath));
		}
		//Setting up the input source and the input pipe
		Tap inputSource = new Hfs(new SequenceFile(CrawlDbDatum.FIELDS), crawlDbPath.toString());
		//The import pipe contains ALL the urls that the crawler has processed since it started
		//(fetched, unfetched, links etc). Using the custom comparators, these are grouped and sorted
		//first by their STATUS (all fetched URLs will be first in order for the occurencies hashmaps
		//to be filled correctly; these hashmaps will count how many URLs have been stored from each
		//web domain in order to enforce URLs-per-webdomain restrictions). After all fetched URLs have
		//been counted, all the rest that have STATUS that is acceptable for crawling are sorted by
		//their score. 		
		Pipe importPipe = new Pipe("url importer");
		Fields f = new Fields(CrawlDbDatum.LAST_STATUS_FIELD).append(new Fields(CrawlDbDatum.SCORE));
		StatusComparator statComp = new StatusComparator();
		ScoreComparator scoreComp = new ScoreComparator();
		f.setComparator(CrawlDbDatum.LAST_STATUS_FIELD, statComp);
		f.setComparator(CrawlDbDatum.SCORE, scoreComp);		
		importPipe = new GroupBy(importPipe,f);
		//The import pipe is splitted in 2 pipes, one for the next batch of urls to fetch
		//and one for all the rest. The SplitFetchedUnfetchedCrawlDatums will select BUFFER_SIZE
		//URLs from the sorted input source by checking that: a) their web domain does not have
		//an occurrence higher than the thresholds defined in the config and b) their STATUS is one
		//of those defined in statusSet. 
		SplitterAssembly splitter = new SplitterAssembly(importPipe, new SplitFetchedUnfetchedCrawlDatums());
		//finishedDatumsFromDb will represent ALL the URLs from the input source and urlsToFetchPipe
		//will represent the selected BUFFER_SIZE URLs that are to be fetched.
		Pipe finishedDatumsFromDbPipe = new Pipe("finished urls", importPipe);
		Pipe urlsToFetchPipe = new Pipe("urls to Fetch", splitter.getLHSPipe());
		urlsToFetchPipe = new Each(urlsToFetchPipe, new CreateUrlDatumFromCrawlDbFunction()); 
		//Setting up all the sinks (each pipe that will write data is connected to a sink, a "path"
		//for writing data in hadoop terms.
		Path outCrawlDbPath = new Path(curWorkingDirPath, CrawlConfig.CRAWLDB_SUBDIR_NAME);
		Tap loopCrawldbSink = new Hfs(new SequenceFile(CrawlDbDatum.FIELDS), outCrawlDbPath.toString());
		Path contentDirPath = new Path(curWorkingDirPath, CrawlConfig.CONTENT_SUBDIR_NAME);
		Tap contentSink = new Hfs(new SequenceFile(FetchedDatum.FIELDS), contentDirPath.toString());
		Path parseDirPath = new Path(curWorkingDirPath, CrawlConfig.PARSE_SUBDIR_NAME);
		Tap parseSink = new Hfs(new SequenceFile(ExtendedParsedDatum.FIELDS), parseDirPath.toString());
		Path classifierDirPath = new Path(curWorkingDirPath, CrawlConfig.CLASSIFIER_SUBDIR_NAME);
		Tap classifierSink = new Hfs(new SequenceFile(ClassifierDatum.FIELDS), classifierDirPath.toString());

		// Create the sub-assembly that runs the fetch job                
		BaseFetcher fetcher = new SimpleHttpFetcher(maxThreads, fetcherPolicy, userAgent);

		((SimpleHttpFetcher) fetcher).setConnectionTimeout(Crawler.configuration.getInt("fetcher.connection_timeout.value"));
		((SimpleHttpFetcher) fetcher).setSocketTimeout(Crawler.configuration.getInt("fetcher.socket_timeout.value"));
		((SimpleHttpFetcher) fetcher).setMaxRetryCount(Crawler.configuration.getInt("fetcher.max_retry_count.value"));
		BaseScoreGenerator scorer = new FixedScoreGenerator();
		FetchPipe fetchPipe = new FetchPipe(urlsToFetchPipe, scorer, fetcher, RobotUtils.createFetcher(fetcher),
				new RobotRulesParser(), new DefaultFetchJobPolicy(fetcherPolicy.getMaxRequestsPerConnection(), 
						Crawler.configuration.getInt("fetcher.max_requests_per_host_per_run.value"), 
						fetcherPolicy.getCrawlDelay()), numReducers);        
		//The fetch pipe returns data in 2 different pipes, one contains the content
		//of downloaded pages and one that contains the status of all the URLs that
		//were fed to the fetcher. contentPipe will handle the content of the fetched pages.
		Pipe contentPipe = new Pipe("content pipe", fetchPipe.getContentTailPipe());  

		//contentPipe is parsed. Metadata, content and links are extracted. Content is
		//cleaned using Boilerpipe.
		ExtendedParsePipe parsePipe = new ExtendedParsePipe(contentPipe, new SimpleNoLinksParser(keepBoiler, 
				curWorkingDirPath.getParent().toString(),	maplangs, tranlistAttrs, targeted_langs, _urlfilterstr, extractLinks));
		//The results from the parser are forwarded to the classifier. The classifier
		//will score the content of each fetched page. It will also score all links
		//based on the score of the page they came from, the anchor text and the surrounding
		//text.
		//ClassifierPipe classifyPipe = new ClassifierPipe(parsePipe.getTailPipe(),new Classifier(language,classes, topic, thres,keepBoiler,min_uniq_terms, max_depth ));
		
		ClassifierPipe classifyPipe = new ClassifierPipe(parsePipe.getTailPipe(), 
				new Classifier(langKeys,targeted_langs,classes, topic, abs_thres,rel_thres, keepBoiler,
						min_uniq_terms, max_depth,minTokensNumber, _storeFilter));
		Pipe urlsFromClassifier = new Pipe("urls from classifier", classifyPipe.getClassifierTailPipe());
		urlsFromClassifier = new Each(urlsFromClassifier, new SelectUrlOnlyFunction());

		//The classifier's and parser's results are forwarded to their final pipes for writing	
		Fields fetchedDatumFields = new Fields(FetchedDatum.URL_FN);		
		Fields statusDatumURLFields = new Fields(StatusDatum.URL_FN);		
		Fields extendedUrlDatumFields = new Fields(ExtendedUrlDatum.URL_FN);
		Fields fetchedDatumFieldsWithUrl = FetchedDatum.FIELDS.append(new Fields("url"));
		Fields extendedParsedDatumFieldsWithUrl = ExtendedParsedDatum.FIELDS.append(new Fields("url"));

		Pipe finalContentPipe = new CoGroup(
				contentPipe, fetchedDatumFields,
				urlsFromClassifier, extendedUrlDatumFields,
				fetchedDatumFieldsWithUrl,
				new RightJoin());

		Pipe finalParsePipe = new CoGroup(
				parsePipe, extendedUrlDatumFields,
				urlsFromClassifier, extendedUrlDatumFields,
				extendedParsedDatumFieldsWithUrl,
				new RightJoin());

		//The links scored by the classifier are handled be the urlFromOutlinksPipe
		Pipe urlFromOutlinksPipe = new Pipe("url from outlinks", classifyPipe.getScoredLinksTailPipe());
		//Outlinks are filtered and normalized
		urlFromOutlinksPipe = new Each(urlFromOutlinksPipe, new ExtendedUrlFilter(urlDomainFilter, urlDepthFilter, _mainhost,_level));
		//urlFromOutlinksPipe = new Each(urlFromOutlinksPipe, new ExtendedNormalizeUrlFunction(new ILSPFCUrlNormalizer()));
		urlFromOutlinksPipe = new Each(urlFromOutlinksPipe, new ExtendedNormalizeUrlFunction(new ILSPFCUrlNormalizer()));

		//The second pipe returned from the fetcher, is assigned to urlFromFetchPipe.
		Pipe urlFromFetchPipe = new Pipe("fetched pipe", fetchPipe.getStatusTailPipe());
		//The URLs the Fetcher attempted to fetch are joined with the
		//URLs the classifier managed to score in order to get a pipe
		//with the BUFFER_SIZE URLs and their updated status and scores
		Fields classifierFields = ClassifierDatum.FIELDS;
		classifierFields = classifierFields.rename(new Fields(ClassifierDatum.URL_FN), new Fields("classifier_url"));
		classifierFields = classifierFields.rename(new Fields(ClassifierDatum.PAYLOAD_FN), new Fields("classifier_payload"));        

		Fields statusAndClassifierFields = StatusDatum.FIELDS.append(classifierFields);
		//		LOGGER.info(statusAndClassifierFields);		
		//		LOGGER.info(fetchedDatumFields);		
		//		LOGGER.info(fetchedDatumFieldsWithUrl);		

		urlFromFetchPipe = new CoGroup(
				urlFromFetchPipe, statusDatumURLFields,
				classifyPipe.getClassifierTailPipe(), extendedUrlDatumFields,
				statusAndClassifierFields, new LeftJoin());

		urlFromFetchPipe = new Each(urlFromFetchPipe, new CreateUrlDatumFromStatusFunction());
		//Finally, these URLs as well as the extracted links are converted to CrawlDbDatums. 
		//Then, we add/update these 2 pipes to the original pipe containing the whole URL
		//collection when we started this run and we make it unique. The result is the final URL collection that
		//must be stored as a result of this run.
		urlFromFetchPipe = new Each(urlFromFetchPipe, new CreateCrawlDbDatumFromUrlFunction());
		urlFromOutlinksPipe = new Each(urlFromOutlinksPipe,new CreateCrawlDbDatumFromUrlFunction());
		Pipe finishedDatumsPipe = new GroupBy(
				Pipe.pipes(
						finishedDatumsFromDbPipe, 
						urlFromFetchPipe,
						urlFromOutlinksPipe),
						new Fields(CrawlDbDatum.URL_FIELD));
		//new Fields(StatusDatum.URL_FIELD));

		finishedDatumsPipe = new Every(finishedDatumsPipe, new MakeDistinctCrawlDbFunction(),Fields.RESULTS);

		// Create the output map that connects each tail pipe to the appropriate sink.
		Map<String, Tap> sinkMap = new HashMap<String, Tap>();
		sinkMap.put(finalContentPipe.getName(), contentSink);
		sinkMap.put(finalParsePipe.getName(), parseSink);
		sinkMap.put(ClassifierPipe.CLASSIFIER_PIPE_NAME, classifierSink);
		sinkMap.put(finishedDatumsPipe.getName(), loopCrawldbSink);

		// LOGGER.info(classifierFields);
		// LOGGER.info(statusAndClassifierFields);		

		// Finally we can run it.
		FlowConnector flowConnector = new FlowConnector(props);

		Flow flow = flowConnector.connect(inputSource, sinkMap/*, statusOutputPipe*/, finalContentPipe, finalParsePipe,
				classifyPipe.getClassifierTailPipe()//, 
				,finishedDatumsPipe
				);

		return flow;
	}


}
