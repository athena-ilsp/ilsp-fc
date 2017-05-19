package gr.ilsp.fc.crawl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
//import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.net.InternetDomainName;

import gr.ilsp.fc.langdetect.LangDetectUtils;
import gr.ilsp.fc.langdetect.LangDetector;
import gr.ilsp.fc.operations.ILSPFCUrlNormalizer;
import gr.ilsp.fc.readwrite.ReadResources;
import gr.ilsp.fc.utils.AnalyzerFactory;
import gr.ilsp.fc.utils.FCStringUtils;
import gr.ilsp.nlp.commons.Constants;

public class CrawlerOptions {
	private static final Logger LOGGER = Logger.getLogger(CrawlerOptions.class);
	private  final String APPNAME = "ILSP Focused Crawler";
	//private static final String DEFAULT_MONO_CONFIG_FILE = "FMC_config.xml";
	//private static final String DEFAULT_BI_CONFIG_FILE = "FBC_config.xml";
	private static final String type_p = "p", type_q = "q", type_m = "m";
	private static final String DIESIS="#";

	protected static Matcher skipLineM = Pattern.compile("^(\\s*)||(#.*)$").matcher("");
	private static final String LANG_KEYS_RESOURCE = "langKeys.txt" ;
	private static final String TRANS_LINKS_ATTRS = "crossLinksAttrs.txt";
	/*private static final String default_agent = "ILSP-FC";
	private static final int default_threads=10;
	private static final int default_duration_minutes = 10;
	private static final boolean default_force= false;
	private static final int default_level = 100;
	private static final int default_depth = 10000;
	private static final boolean default_keepboiler = false;
	private  static final int default_minParLen=3;
	private  static final int default_minDocLen = 80;*/
	
	private Options options;
	public static int NO_CRAWL_DURATION = 0;

	//private  int _minParLen1=3;
	private  int _minDocLen = 80;
	private boolean _force = false, _keepBoiler = true;
	private String _type="p";

	private String _webdomain=null, _webmaindomain=null, _usertopic=null, _filter=null, _storefilter=null, _paths_repl=null, _loggingAppender = null;

	private String _agentName="ILSP-FC", _language, _config, ws_dir, defaultlangDetectorId="langdetect" ;
	private String[] _langKeys, _targetedLangs;
	private LangDetector _langDetector;
	private List<String[]> _linkAttrs;
	private HashMap<String, String> _mapLangs;
	//private String default_genrefile="genres_keys.txt";
	private URL _genre;
	private File _seedFile, _topicFile=null;
	private File _outputDir, _outBaseName, _outputFile;
	private File _o1 , _o2;

	private  int _threads = 10, _numLoops = 0, _crawlDuration = 10,  _level = 100, _depth = 10000;
	private boolean _debug = false, _iso6393 = true; 

	private CompositeConfiguration _configuration ;  
	//private CrawlerO cro = new CrawlerO();


	public CrawlerOptions() {
		createOptions();
	}
	@SuppressWarnings("static-access")
	private  Options createOptions() {
		options = new Options();

		options.addOption( OptionBuilder.withLongOpt( "help" )
				.withDescription( "This message" )
				.create("h") );
		options.addOption( OptionBuilder.withLongOpt( "debug" )
				.withDescription( "Use debug level for logging" )				
				.create("dbg") );
		options.addOption( OptionBuilder.withLongOpt( "loggingAppender" )
				.withDescription( "Logging appender (console, DRFA) to use")
				.hasArg()
				.create("l") );
		options.addOption( OptionBuilder.withLongOpt( "config" )
				.withDescription( "Path to the XML configuration file" )
				.hasArg()
				.create("cfg") );
		options.addOption( OptionBuilder.withLongOpt( "topic" )
				.withDescription( "Path to a file with the topic definition" )
				.hasArg()
				.create("tc") );
		options.addOption( OptionBuilder.withLongOpt( "domain" )
				.withDescription( "A descriptive title for the targeted domain" )
				.hasArg()
				.create("dom") );
		//options.addOption( OptionBuilder.withLongOpt("stay_in_webdomain")
		//		.withDescription( "Force the monolingual crawler to stay in a specific web domain" )				
		//		.create("d") );
		options.addOption( OptionBuilder.withLongOpt( "urls" )
				.withDescription( "File with seed urls used to initialize the crawl" )
				.hasArg()
				.create("u") );
	/*	options.addOption( OptionBuilder.withLongOpt( "outputdir" )
				.withDescription( "Output directory" )
				.hasArg()
				.create("o") );*/
		options.addOption( OptionBuilder.withLongOpt( "basename" )
				.withDescription( "Basename to be used in generating all output files for easier content navigation" )
				.hasArg()
				.create("bs") );
		options.addOption( OptionBuilder.withLongOpt( "agentname" )
				.withDescription( "Agent name to identify the person or the organization responsible for the crawl" )
				.hasArg()
				.create("a") );
		options.addOption( OptionBuilder.withLongOpt( "threads" )
				.withDescription( "Maximum number of fetcher threads to use" )
				.hasArg()
				.create("t") );		
		options.addOption( OptionBuilder.withLongOpt( "CrawlDurationLoops" )
				.withDescription( "Crawl Duration in Loops" )
				.hasArg()
				.create("cdl") );		
		options.addOption( OptionBuilder.withLongOpt( "crawldurationMinutes" )
				.withDescription( "Crawl Duration in Minutes" )
				.hasArg()
				.create("cdm") );
		//options.addOption( OptionBuilder.withLongOpt( "genre" )
		//		.withDescription( "text file with genre types and keywords for each type." )
		//		.hasArg()
		//		.create("gnr") );
		options.addOption( OptionBuilder.withLongOpt( "keepboiler" )
				.withDescription( "Keep and annotate boilerplate content in parsed text" )				
				.create("k") );
		options.addOption( OptionBuilder.withLongOpt( "crawlUpToDepth" )
				.withDescription( "Links will be extracted only from webpages which have been visited up to this number of cycles" )	
				.hasArg()
				.create("depth") );
		options.addOption( OptionBuilder.withLongOpt( "force" )
				.withDescription( "Force a new crawl. Caution: This will remove any previously crawled data" )				
				.create("f") );		
		options.addOption( OptionBuilder.withLongOpt( "length" )
				.withDescription( "Μinimum number of tokens per text block. Shorter text blocks will be annoteted as \"ooi-length\"" )	
				.hasArg()
				.create("len") );
		options.addOption( OptionBuilder.withLongOpt( "minlength" )
				.withDescription( "Minimum number of tokens in crawled documents (after boilerplate detection). Shorter documents will be discarded.")
				.hasArg()
				.create("mtlen") );
		options.addOption( OptionBuilder.withLongOpt( "type" )
				.withDescription("Crawler type: m (monolingual) or  p (parallel)" )
				.hasArg()
				.create("type") );
		options.addOption( OptionBuilder.withLongOpt( "levelfilter" )
				.withDescription( "Use this option to force the crawler visit only urls that are up to a specific level of a webdomain webdomains." )	
				.hasArg()
				.create("level") );
		options.addOption( OptionBuilder.withLongOpt( "fetchfilter" )
				.withDescription( "Use this regex to force the crawler to crawl only in specific sub webdomains. Webpages with urls that do not match this regex will not be fetched." )	
				.hasArg()
				.create("filter") );
		options.addOption( OptionBuilder.withLongOpt( "storefilter" )
				.withDescription( "Use this regex to force the crawler to store only webpages with urls that match this regex." )	
				.hasArg()
				.create("storefilter") );
		options.addOption( OptionBuilder.withLongOpt( "languages" )
				.withDescription( "Two or three letter ISO code(s) of target language(s), e.g. el (for a monolingual crawl for Greek content) or en;el (for a bilingual crawl)" )
				.hasArg()
				.create("lang") );
		/*options.addOption( OptionBuilder.withLongOpt( "lang_Isocode_" )
				.withDescription( "if exists iso6393 language codes are used.")
				.create("iso6393") );*/
		options.addOption( OptionBuilder.withLongOpt( "offline_xslt" )
				.withDescription( "Apply an xsl transformation to generate html files during exporting.")
				.create("oxslt") );
		options.addOption( OptionBuilder.withLongOpt( "destination" )
				.withDescription( "Path to a directory where the acquired/generated resources will be stored")
				.hasArg()
				.create("dest") );
		options.addOption( OptionBuilder.withLongOpt( "path_replacements" )
				.withDescription( "Put the strings to be replaced, separated by ';'. This might be useful for crawling via the web service")
				.hasArg()
				.create("p_r") );
		options.addOption( OptionBuilder.withLongOpt( "specific_output1" )
				.withDescription( "Not used (Specific outout1)")
				.hasArg()
				.create("o1") );
		options.addOption( OptionBuilder.withLongOpt( "specific_output2" )
				.withDescription( "Not used (Specific outout2)")
				.hasArg()
				.create("o2") );
		return options;
	}

	public  void parseOptions ( String[] args) {
		//CrawlerO cro = new CrawlerO();
		if ((args.length==0) || (helpAsked(args) )) {
			help();
		}	
		// create the command line parser
		CommandLineParser clParser = new GnuParser();
		try {
			CommandLine line = clParser.parse( options, args );
			if(line.hasOption( "h"))
				help();	
			if(line.hasOption( "dbg"))	{
				_debug = true;
				System.setProperty("fc.root.level", "DEBUG");            
			}else 
				System.setProperty("fc.root.level", "ERROR");		//System.setProperty("fmc.root.level", "INFO");
			
			if(line.hasOption( "l"))	
				_loggingAppender = line.getOptionValue("l");

			//get parameters concerning languages
			if(line.hasOption( "lang")) {
				String ll = line.getOptionValue("lang").toLowerCase();
				if (line.getOptionValue("lang").toLowerCase().equals("g"))
					ll = CrawlerUtils.getSupportedLanguages();
				_language = LangDetectUtils.updateLanguages(ll,_iso6393);
				_targetedLangs =_language.split(Constants.SEMICOLON);
				_langKeys = findKeys4lang(_language);
				_mapLangs = mapLangs(_language);
				_linkAttrs = findTransLinks();
				checkAnalyzers(_targetedLangs);
				_langDetector = LangDetectUtils.loadLangDetectors(_targetedLangs,defaultlangDetectorId);
			}else{
				LOGGER.error("No languages have been defined.");
				System.exit(0);
			}

			getParams4Crawl(line);
			getParams4Topicness(line);
			getParams4ContentProps(line);

			if(line.hasOption( "cfg")) 
				_config = line.getOptionValue("cfg");
		} catch( ParseException exp ) {
			// oops, something went wrong
			System.err.println( "Parsing options failed.  Reason: " + exp.getMessage() );			
			System.exit(64);
		}
	}



	public static List<String[]> findTransLinks() {
		List<String[]> res = new ArrayList<String[]>();
		try {
			URL svURL = ReadResources.class.getClassLoader().getResource(TRANS_LINKS_ATTRS);
			BufferedReader in = new BufferedReader(new InputStreamReader(svURL.openStream()));
			String str;
			while ((str = in.readLine()) != null) {
				if (str.startsWith(DIESIS)) 
					continue;
				String[] temp = str.split(Constants.COLON);
				res.add(temp);
			}
		} catch (IOException e) {
			LOGGER.error("Problem in reading the file for crossLinksAttrs.");
		}
		return res;
	}

	/*private List<String> findLangPairs(String language) {
		String[] langs = language.split(SEMICOLON_STR); 
		List<String> lang_pairs = new ArrayList<String>();
		if (langs.length>1){
			for (int ii=0;ii<langs.length-1;ii++){
				for (int jj=ii+1;jj<langs.length;jj++){
					lang_pairs.add(langs[ii]+SEMICOLON_STR+langs[jj]);
				}
			}
		}
		return lang_pairs;
	}*/
	/**
	 * parses commandLine and gets _keepBoiler, _del, _length, _minTokensNumber
	 * @param line
	 */
	private void getParams4ContentProps(CommandLine line) {
		if(line.hasOption( "k")) 		{	_keepBoiler  = true;				}
		//if(line.hasOption( "len")) 		{	_minParLen = Integer.parseInt(line.getOptionValue("len"));				} 
		if(line.hasOption( "mtlen")) 	{	_minDocLen = Integer.parseInt(line.getOptionValue("mtlen"));	} 
	}
	/**
	 * parses the command Line and gets _topic (file with terms, etc) and _descr (topicName)
	 * @param line
	 */
	private void getParams4Topicness(CommandLine line) {
		if(line.hasOption( "tc")) {
			_topicFile = new File(line.getOptionValue("tc")).getAbsoluteFile();
		}
		if(line.hasOption( "dom")) {
			if (_topicFile==null){
				LOGGER.info("The targeted domain is defined but a topic definition is not applied. " +
						"So, the domain will be used as provided, i.e. it will not be identified.");
				//help();
			}
			_usertopic = line.getOptionValue("dom");
		}else{
			if (_topicFile!=null){
				LOGGER.error("Even though a topic definition is applied " +
						"the targeted domain is not defined. "+
						"Regarding Topic definition and targeted domain," +
						"you should either define both or none of them.");
				help();
			}
		}
		/*if(line.hasOption( "gnr")) {
			try {
				_genre = new URL(line.getOptionValue("gnr"));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			_genre =SimpleCrawl.class.getClassLoader().getResource(default_genrefile);
			LOGGER.info("Genre types and keywrods are included in file: " + _genre);
		}*/
	}
	/**
	 * parses the commandLine and gets all parameters required for crawls:
	 * agentName, file containing the seed URLs, number of threads, duration (in number of loops or in seconds),
	 * regex for filtering URLs to be visited, regex for filtering URLs to be stored,
	 * check if the crawler has to stay in a specific website (in case of monolingual crawls),
	 *   
	 * @param line
	 */
	private void getParams4Crawl(CommandLine line) {
		if(line.hasOption( "a"))
			_agentName = line.getOptionValue("a").replace(Constants.SPACE, Constants.UNDERSCORE);
		if (line.hasOption("type"))
			_type = line.getOptionValue("type");
		else{
			LOGGER.error("option -type is required");
			help();
		}
		if (line.hasOption("u")) {
			_seedFile = new File(line.getOptionValue("u"));
			if (_seedFile.exists()==false){
				LOGGER.error("The seed file does not exist.");
				help();
			}
		}
		if(line.hasOption("t")) 			{	_threads = Integer.parseInt(line.getOptionValue("t"));	}							
		if(line.hasOption("cdl")) 			{	_numLoops = Integer.parseInt(line.getOptionValue("cdl"));	_crawlDuration=0;	}
		if(line.hasOption("cdm")) 			{	_crawlDuration = Integer.parseInt(line.getOptionValue("cdm"));	}
		if(line.hasOption("f"))				{	_force   = true;	}
		if (line.hasOption("filter"))		{	
			_filter = line.getOptionValue("filter");	
			try{
				Pattern.compile(_filter);
			}catch (PatternSyntaxException exception) {
				System.err.println(_filter +" is not a valid regex. See parameter -filter");
				System.exit(0);
			}
		}
		if (_type.equals(type_m)){
			if(line.hasOption( "filter")) 
				_filter = line.getOptionValue("filter");
			else
				_filter = ".*";
		}
		
		if (line.hasOption("storefilter"))	{
			_storefilter = line.getOptionValue("storefilter");
			try{
				Pattern.compile(_storefilter);
			}catch (PatternSyntaxException exception) {
				System.err.println(_storefilter +" is not a valid regex. See parameter -storefilter");
				System.exit(0);
			}
		}
		if(line.hasOption("level")) 		{	_level = Integer.parseInt(line.getOptionValue("level"));	}
		if(line.hasOption("depth")) 		{	_depth = Integer.parseInt(line.getOptionValue("depth"));	}

		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
		if (line.hasOption( "dest")) 
			ws_dir = FilenameUtils.concat(line.getOptionValue("dest"), _agentName+Constants.UNDERSCORE+timeStamp) ;
		else
			ws_dir=_agentName+Constants.UNDERSCORE+timeStamp;

		_outputDir = new File(FilenameUtils.concat(ws_dir, UUID.randomUUID().toString()));
		//if(!line.hasOption( "o")) 
		//	_outputDir = new File(FilenameUtils.concat(ws_dir, UUID.randomUUID().toString())); 				 	

		
		if (_type.equals(type_p) | _type.equals(type_q)){
			if (!_language.contains(Constants.SEMICOLON)){
				LOGGER.error("You crawl for parallel or comparable but only 1 language has been defined.");
				help();
			}
		}
		if (_type.equals(type_p)){
			URL url;
			if (!line.hasOption( "filter")){
				ArrayList<String> seed_list = readSeedList(_seedFile);
				ILSPFCUrlNormalizer normalizer = new ILSPFCUrlNormalizer();
				if (!seed_list.isEmpty()){
					if (seed_list.size()==1){
						try {
							url = new URL(normalizer.normalize(seed_list.get(0)));
							_webdomain = FCStringUtils.removeWWW(url.getHost());				//LOGGER.debug("Examining web domain : " + _domain);
							_webmaindomain = FCStringUtils.removeWWW(processhost(_webdomain));	//LOGGER.debug("Examining second domain : " + _maindomain);
							//_domain = PUNCT+_domain;
						} catch (MalformedURLException e) {
							LOGGER.error("Seed URL is not valid: "+seed_list.get(0));
							help();
						}
					}else{
						String firsthost="";
						for (int ii=0;ii<seed_list.size();ii++){
							try {
								if (ii==0)
									firsthost = new URL(normalizer.normalize(seed_list.get(ii))).getHost();
								url = new URL(normalizer.normalize(seed_list.get(ii)));
								String host = url.getHost();
								if (!host.equals(firsthost)){
									if (!line.hasOption( "filter")){
										LOGGER.error("Since the provided seed list for bilingual crawling includes more than one webdomains, " +
												" USE the filter argument to confine FC within these webdomains.");
										System.exit(0);				
									}
									else{
										_webdomain=null;
										_webmaindomain=null;
									}
								}else{
									_webdomain = FCStringUtils.removeWWW(host);
									String mainhost="";
									char c = host.charAt(0);
									if (Character.isDigit(c)){
										mainhost=host;
									}else{
										mainhost=processhost(host);
									}
									_webmaindomain = FCStringUtils.removeWWW(mainhost);
								}
							}catch (MalformedURLException e) {
								LOGGER.error("Seed URL is not valid:"+seed_list.get(ii));
							}		
						} 
					}
				}else{
					LOGGER.error("There is no valid seed URL.");
					help();
				}
				String t = _webdomain.replace(".", "\\.");
				//_filter = ".*"+t+".*";
				_filter = "^[^/]*\\."+t+".*";
			}else{
				_webdomain=null;
				_webmaindomain=null;
				_filter = line.getOptionValue("filter");
			}
		}
	}

	/**
	 * read the seed list, skip commented or empty lines
	 * @param seedsFile 
	 * @return
	 */
	private ArrayList<String> readSeedList(File seedsFile) {
		ArrayList<String> seed_list = new ArrayList<String>();
		BufferedReader rdr;
		try {
			rdr = new BufferedReader(new InputStreamReader(new FileInputStream(seedsFile),"utf8"));
			String cur_line="";
			while ((cur_line=rdr.readLine())!=null){
				if (skipLineM.reset(cur_line).matches()) 
					continue;
				seed_list.add(cur_line);
			}
			rdr.close();
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("Problem in reading the seed URL file.");
			help();
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			LOGGER.error("The seed URL file does not exist.");
			help();
			e.printStackTrace();
		} catch (IOException e) {
			LOGGER.error("Problem in reading the seed URL file.");
			help();
			e.printStackTrace();
		}
		return seed_list;
	}

	/**
	 * checks if analyzers of the targeted languages are available
	 * @param _languages
	 */
	private void checkAnalyzers(String[] langs) {
		//String[] langs=languages.split(SEMICOLON_STR);
		for (int ii=0;ii<langs.length;ii++){
			try {
				AnalyzerFactory analyzerFactory = new AnalyzerFactory();
				analyzerFactory.getAnalyzer(langs[ii]);
			} catch (Exception e) {
				LOGGER.error("No analyser available for language "+ langs[ii]);
				System.exit(0);
			}
		}
	}

	private String processhost(String host) {
		String finalhost="";
		InternetDomainName domainname = InternetDomainName.from(host);
		InternetDomainName ps = domainname.publicSuffix();
		if (ps==null){
			return host;
		}
		int ind1=0, ind2=0;
		String temp1 = "";
		while (ind1>-1){
			temp1 = domainname.name().substring(ind2);
			ind1=temp1.indexOf("."+ps.name());
			ind2=ind2+ind1+1;
		}
		InternetDomainName domainname1 = InternetDomainName.from(domainname.name().substring(0, ind2));
		ImmutableList<String> domainparts=domainname1.parts();
		for (int kk=0;kk<domainparts.size();kk++){
			InternetDomainName temp = InternetDomainName.from(domainparts.get(kk));
			if (!temp.isPublicSuffix()){
				finalhost=finalhost+domainparts.get(kk)+".";
			}
		}
		return finalhost;
	}

	/**
	 * parses the predefined project resource LANG_KEYS_RESOURCE and for each targeted language
	 * returns the array with alternative patterns each targeted languages
	 * Targeted languages must be included in LANG_KEYS_RESOURCE 
	 * @param language
	 * @return
	 */
	private String[] findKeys4lang(String language) {
		ArrayList<String> langKeys=new ArrayList<String>();
		String[] langs = _language.split(Constants.SEMICOLON);
		try {
			URL svURL = ReadResources.class.getClassLoader().getResource(LANG_KEYS_RESOURCE);
			BufferedReader in = new BufferedReader(new InputStreamReader(svURL.openStream()));
			String str, b;
			while ((str = in.readLine()) != null) {
				b=str.subSequence(0, str.indexOf(">")).toString();
				for (String lang:langs){
					if (b.equals(lang)){
						langKeys.add(str.subSequence(str.indexOf(">")+1, str.length()).toString());
					}
				}
				if (langKeys.size()==langs.length)
					break;
			}
			in.close();
			if (langKeys.size()!=langs.length){
				LOGGER.error("The targeted language(s) is (are) not supported. Check the file for langKeys and/or langcode-langs.");
				System.exit(0);
			}
		} catch (IOException e) {
			LOGGER.error("Problem in reading the file for langKeys.");
		}
		String[] result=new String[langKeys.size()];
		for (int ii=0;ii<langKeys.size();ii++)
			result[ii]=langKeys.get(ii);

		return result;
	}

	/**
	 * parses the predefined project resource LANG_KEYS_RESOURCE and for each targeted language
	 * returns the array with alternative patterns each targeted languages  
	 * @param language
	 * @return
	 */
	private HashMap<String,String> mapLangs(String language) {
		ArrayList<String> langKeys=new ArrayList<String>();
		String[] langs = _language.split(Constants.SEMICOLON);
		HashMap<String,String> result= new HashMap<String,String>();
		try {
			URL svURL = ReadResources.class.getClassLoader().getResource(LANG_KEYS_RESOURCE);
			BufferedReader in = new BufferedReader(new InputStreamReader(svURL.openStream()));
			String str, b;
			while ((str = in.readLine()) != null) {
				b=str.subSequence(0, str.indexOf(">")).toString();
				for (String lang:langs){
					if (b.equals(lang))
						result.put(b, str.subSequence(str.indexOf(">")+1, str.length()).toString());
				}
				if (langKeys.size()==langs.length)
					break;
			}
			in.close();
		} catch (IOException e) {
			LOGGER.error("Problem in reading the file for langKeys.");
		}
		return result;
	}


	/**
	 * Loads the default configuration file and checks if user supplied a custom one.
	 * @param type
	 * @param confFile
	 * @return
	 *//*
	private static CompositeConfiguration getConfig(String type, String confFile) {
		CompositeConfiguration configuration = new CompositeConfiguration();
		URL default_config = Crawler.class.getClassLoader().getResource(DEFAULT_MONO_CONFIG_FILE);
		if (type.equals(type_p))
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
	}*/

	public  void help(){
		printHelp( APPNAME , options );
		System.exit(0);
	}

	public  void printHelp(String program, Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.setWidth(100);
		formatter.printHelp( program, options );
	}


	public List<String[]> getTransLinksAttrs() {
		return _linkAttrs;
	}

	/**
	 * @param _linkAttrs the _linkAttrs to set
	 */
	public void set_linkAttrs(List<String[]> _linkAttrs) {
		this._linkAttrs = _linkAttrs;
	}
	public  String getWebDomain() {
		return _webdomain;
	}
	/**
	 * @param _webdomain the _webdomain to set
	 */
	public void set_webdomain(String _webdomain) {
		this._webdomain = _webdomain;
	}


	public  String getWebMainDomain() {
		return _webmaindomain;
	}
	/**
	 * @param _webmaindomain the _webmaindomain to set
	 */
	public void set_webmaindomain(String _webmaindomain) {
		this._webmaindomain = _webmaindomain;
	}
	public  boolean isDebug() {
		return _debug;
	}
	/**
	 * @param _debug the _debug to set
	 */
	public void set_debug(boolean _debug) {
		this._debug = _debug;
	}
	public  String getLoggingAppender() {
		return _loggingAppender;
	}

	/**
	 * @param _loggingAppender the _loggingAppender to set
	 */
	public void set_loggingAppender(String _loggingAppender) {
		this._loggingAppender = _loggingAppender;
	}
	public  File getOutputDir() {
		return _outputDir;
	}
	/**
	 * @param _outputDir the _outputDir to set
	 */
	public void set_outputDir(File _outputDir) {
		this._outputDir = _outputDir;
	}
	public  File getOutputFile() {
		return _outputFile;
	}
	public  File getBaseName() {
		return _outBaseName;
	}

	/**
	 * @param _outBaseName the _outBaseName to set
	 */
	public void set_outBaseName(File _outBaseName) {
		this._outBaseName = _outBaseName;
	}
	public  int getThreads() {
		return _threads;
	}

	/**
	 * @param _threads the _threads to set
	 */
	public void set_threads(int _threads) {
		this._threads = _threads;
	}
	public String getConfig(){
		return _config;
	}
	/**
	 * @param _config the _config to set
	 */
	public void set_config(String _config) {
		this._config = _config;
	}

	public CompositeConfiguration getConfiguration(){
		return _configuration;
	}
	/**
	 * @param _config the _config to set
	 */
	public void set_configuration(CompositeConfiguration _configuration) {
		this._configuration = _configuration;
	}

	public URL getGenre(){
		return _genre;
	}
	/**
	 * @param _genre the _genre to set
	 */
	public void set_genre(URL _genre) {
		this._genre = _genre;
	}

	public String getUserTopic() {
		return _usertopic;
	}
	/**
	 * @param _usertopic the _usertopic to set
	 */
	public void set_usertopic(String _usertopic) {
		this._usertopic = _usertopic;
	}

	public String getFilter() {
		return _filter;
	}
	/**
	 * @param _filter the _filter to set
	 */
	public void set_filter(String _filter) {
		this._filter = _filter;
	}

	public String getStoreFilter() {
		return _storefilter;
	}
	/**
	 * @param _storefilter the _storefilter to set
	 */
	public void set_storefilter(String _storefilter) {
		this._storefilter = _storefilter;
	}

	public String getPathReplace() {
		return _paths_repl;
	}

/*	public boolean useISO6393() {
		return _iso6393;
	}
	*//**
	 * @param _iso6393 the _iso6393 to set
	 *//*
	public void set_iso6393(boolean _iso6393) {
		this._iso6393 = _iso6393;
	}
*/
	public int getCrawlLevel() {
		return _level;
	}
	/**
	 * @param _level the _level to set
	 */
	public void set_level(int _level) {
		this._level = _level;
	}
	public int upToDepth() {
		return _depth;
	}
	/**
	 * @param _depth the _depth to set
	 */
	public void set_depth(int _depth) {
		this._depth = _depth;
	}

	public File getO1() {
		return _o1;
	}
	public File getO2() {
		return _o2;
	}

	private static boolean helpAsked(String[] args) {
		for (int ii=0; ii<args.length;ii++){
			if (args[ii].equals("-h") | args[ii].equals("-help") | args[ii].equals("--help") | args[ii].equals("--h")){
				return true;
			}		
		}
		return false;
	}

	public boolean keepBoiler() {
		return _keepBoiler;
	}
	/**
	 * @param _keepBoiler the _keepBoiler to set
	 */
	public void set_keepBoiler(boolean _keepBoiler) {
		this._keepBoiler = _keepBoiler;
	}
	public boolean getForce() {
		return _force;
	}
	/**
	 * @param _force the _force to set
	 */
	public void set_force(boolean _force) {
		this._force = _force;
	}
	//public int getMinParLen() {
	//	return _minParLen;
	//}
	/**
	 * @param _minParLen the _minParLen to set
	 */
	//public void set_minParLen(int _minParLen) {
	//	this._minParLen = _minParLen;
	//}
	public int getMinDocLen() {
		return _minDocLen;
	}
	/**
	 * @param _minDocLen the _minDocLen to set
	 */
	public void set_minDocLen(int _minDocLen) {
		this._minDocLen = _minDocLen;
	}
	public String getType() {
		return _type;
	}
	/**
	 * @param _type the _type to set
	 */
	public void set_type(String _type) {
		this._type = _type;
	}
	public String[] getTargetedLangs() {
		return _targetedLangs;
	}
	/**
	 * @param _targetedLangs the _targetedLangs to set
	 */
	public void set_targetedLangs(String[] _targetedLangs) {
		this._targetedLangs = _targetedLangs;
	}
	public  int getNumLoops() {
		return _numLoops;
	}
	/**
	 * @param _numLoops the _numLoops to set
	 */
	public void set_numLoops(int _numLoops) {
		this._numLoops = _numLoops;
	}
	public  int getCrawlDuration() {
		return _crawlDuration;
	}
	/**
	 * @param _crawlDuration the _crawlDuration to set
	 */
	public void set_crawlDuration(int _crawlDuration) {
		this._crawlDuration = _crawlDuration;
	}
	public  String getAgentName() {
		return _agentName;
	}
	/**
	 * @param _agentName the _agentName to set
	 */
	public void set_agentName(String _agentName) {
		this._agentName = _agentName;
	}
	public File getTopicFile() {
		return _topicFile;
	}
	/**
	 * @param _topic the _topic to set
	 */
	public void set_topicFile(File _topic) {
		this._topicFile = _topic;
	}
	public File getSeedFile() {
		return _seedFile;
	}

	/**
	 * @param _seedFile the _seedFile to set
	 */
	public void set_seedFile(File _seedFile) {
		this._seedFile = _seedFile;
	}
	public String[] getLangKeys() {
		return _langKeys;
	}
	/**
	 * @param _langKeys the _langKeys to set
	 */
	public void set_langKeys(String[] _langKeys) {
		this._langKeys = _langKeys;
	}
	public HashMap<String,String> getMapLangs() {
		return _mapLangs;
	}
	/**
	 * @param _mapLangs the _mapLangs to set
	 */
	public void set_mapLangs(HashMap<String, String> _mapLangs) {
		this._mapLangs = _mapLangs;
	}
	
	public void set_LangDetector(LangDetector langDetector) {
		this._langDetector = langDetector;// TODO Auto-generated method stub	
	}
	public LangDetector getLangDetector() {
		return _langDetector;	
	}
	
	
	
	/*private int procMPL(CommandLine line) {
		if(line.hasOption( "mtlen")) 
			return Integer.parseInt(line.getOptionValue("mtlen"));
		else
			return default_minParLen;
	}
	private int procMDL(CommandLine line) {
		if(line.hasOption( "len")) 
			return Integer.parseInt(line.getOptionValue("len"));
		else
			return default_minDocLen;
	}
	private boolean prockeep(CommandLine line) {
		
		if(line.hasOption( "k"))
			return  true;
		else
			return default_keepboiler;
	}
	
	
	private String[] procWebDomainess(String type, String filter, File seedsFile) {
		String webdomain=null;
		String webmaindomain=null;
		String[] restricts = new String[3];
		if (type.equals(type_p)){
			URL url;
			//if (!line.hasOption( "filter")){
			if (filter==null){
				ArrayList<String> seed_list = readSeedList(seedsFile);
				ILSPFCUrlNormalizer normalizer = new ILSPFCUrlNormalizer();
				if (!seed_list.isEmpty()){
					if (seed_list.size()==1){
						try {
							url = new URL(normalizer.normalize(seed_list.get(0)));
							webdomain = FCStringUtils.removeWWW(url.getHost());				//LOGGER.debug("Examining web domain : " + _domain);
							webmaindomain = FCStringUtils.removeWWW(processhost(webdomain));	//LOGGER.debug("Examining second domain : " + _maindomain);
							//_domain = PUNCT+_domain;
						} catch (MalformedURLException e) {
							LOGGER.error("Seed URL is not valid: "+seed_list.get(0));
							help();
						}
					}else{
						String firsthost="";
						for (int ii=0;ii<seed_list.size();ii++){
							try {
								if (ii==0)
									firsthost = new URL(normalizer.normalize(seed_list.get(ii))).getHost();
								url = new URL(normalizer.normalize(seed_list.get(ii)));
								String host = url.getHost();
								if (!host.equals(firsthost)){
									if (filter==null){
										LOGGER.error("Since the provided seed list for bilingual crawling includes more than one webdomains, " +
												" USE the filter argument to confine FC within these webdomains.");
										System.exit(0);				
									}
									else{
										webdomain=null;
										webmaindomain=null;
									}
								}else{
									webdomain = FCStringUtils.removeWWW(host);
									String mainhost="";
									char c = host.charAt(0);
									if (Character.isDigit(c))
										mainhost=host;
									else
										mainhost=processhost(host);
									
									webmaindomain = FCStringUtils.removeWWW(mainhost);
								}
							}catch (MalformedURLException e) {
								LOGGER.error("Seed URL is not valid:"+seed_list.get(ii));
							}		
						} 
					}
				}else{
					LOGGER.error("There is no valid seed URL.");
					help();
				}
				String t = webdomain.replace(".", "\\.");
				//_filter = ".*"+t+".*";
				filter = "^[^/]*\\."+t+".*";
			}else{
				webdomain=null;
				webmaindomain=null;
				//filter = c.getOptionValue("filter");
			}
		}
		restricts[0] =webdomain; restricts[1] =webmaindomain; restricts[2] =filter;
		return restricts;
	}
	private File procOutDir(CommandLine line) {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
		if (line.hasOption( "dest")) 
			return new File(FilenameUtils.concat(line.getOptionValue("dest"), _agentName+UNDERSCORE_STR+timeStamp)) ;
		else
			return null;
	}
	private int procDepth(CommandLine line) {
		if(line.hasOption("depth")) 
			return  Integer.parseInt(line.getOptionValue("depth"));
		else
		return default_depth;
	}
	private int procLevel(CommandLine line) {
		if(line.hasOption("level")) 
			return  Integer.parseInt(line.getOptionValue("level"));
		else
		return default_level;
	}
	private String procStoreFilter(CommandLine line) {
		String filter = null;
		if (line.hasOption("storefilter"))	{
			_storefilter = line.getOptionValue("storefilter");
			try{
				Pattern.compile(_storefilter);
			}catch (PatternSyntaxException exception) {
				System.err.println(_storefilter +" is not a valid regex. See parameter -storefilter");
				System.exit(0);
			}
		}
		return filter;
	}
	private String procFilter(CommandLine line) {
		String filter = null;
		if (line.hasOption("filter"))		{	
			filter = line.getOptionValue("filter");	
			try{
				Pattern.compile(_filter);
			}catch (PatternSyntaxException exception) {
				System.err.println(_filter +" is not a valid regex. See parameter -filter");
				System.exit(0);
			}
		}
		return filter;
	}
	private boolean procForce(CommandLine line) {
		if(line.hasOption("f"))	
			return true;
		else
			return default_force;
	}
	private int procDurationMinutes(CommandLine line) {
		if(line.hasOption("c")) 			
			return Integer.parseInt(line.getOptionValue("c")); //_crawlDuration = Integer.parseInt(line.getOptionValue("c"));
		else
			return default_duration_minutes;
	}
	private int procThreads(CommandLine line) {
		if(line.hasOption("t")) 			
			return Integer.parseInt(line.getOptionValue("t"));
		else
			return default_threads;
	}
	private File procSeedFile(CommandLine line) {
		File seedFile = null;
		if (line.hasOption("u")) {
			_seedFile = new File(line.getOptionValue("u"));
			if (_seedFile.exists()==false){
				LOGGER.error("The seed file does not exist.");
				help();
			}
		}
		return seedFile;
	}
	private String procAgent(CommandLine line) {
		if(line.hasOption( "a"))
			return line.getOptionValue("a").replace(Constants.SPACE, "_");
		else
			return default_agent;
	}
	private String procType(CommandLine line) {
		String type = null;
		if (line.hasOption("type"))
			return line.getOptionValue("type");
		else{
			LOGGER.error("option -type is required");
			help();
		}
		return type;
	}

	private String procUserTopic(CommandLine line) {
		String userTopic = null ;
		if(line.hasOption( "dom")) {
			if (cro.getTopicFile()==null) {//(_topicFile==null){
				LOGGER.info("The targeted domain is defined but a topic definition is not applied. " +
						"So, the domain will be used as provided, i.e. it will not be identified.");
				//help();
			}
			userTopic = line.getOptionValue("dom"); //_usertopic = line.getOptionValue("dom");
		}else{
			if (cro.getTopicFile()!=null){ // (_topicFile!=null){
				LOGGER.error("Even though a topic definition is applied " +
						"the targeted domain is not defined. "+
						"Regarding Topic definition and targeted domain," +
						"you should either define both or none of them.");
				help();
			}
		}
		return userTopic;
	}
	*/
	
	
}
