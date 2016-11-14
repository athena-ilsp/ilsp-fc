package gr.ilsp.fc.main;

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
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.net.InternetDomainName;

import gr.ilsp.fc.langdetect.LangDetectUtils;
import gr.ilsp.fc.operations.ILSPFCUrlNormalizer;
import gr.ilsp.fc.utils.AnalyzerFactory;
import gr.ilsp.fc.utils.FCStringUtils;

public class CrawlOptions {
	public static int NO_CRAWL_DURATION = 0;
	private  final String APPNAME = "ILSP Focused Crawler";
	private Options options;

	private String _domain=null, _maindomain=null, _descr=null, _filter=null, _storefilter=null, _paths_repl=null, _loggingAppender = null;
	private String _type="m", _operation="", _agentName, _language, _urls, _aligner=null, _dict=null, _dictpath=null, _config, ws_dir, _methods = "aupdihml", defaultSegType="1:1";
	private String[] _langKeys, _targetedLangs;
	private List<String[]> _linkAttrs;
	private HashMap<String, String> _mapLangs;
	private List<String> _langPairs;
	//private String default_genrefile="genres_keys.txt";
	private URL _genre;
	private String[][] _urls_repls=null;
	private File _outputDir, _inputDir, _outBaseName, _outputFile, _outputFileHTML, _outputFileTMX, _outputFileHTMLTMX;
	private File _outputFile_mergedTMX, _outputFile_mergedTMXHTML, _o1 , _o2, _topic=null;
	private  int _threads = 10, _numLoops = 0, _crawlDuration = 10, _minTokensNumber = 100, _minTuvLen = 0, _level = 100, _depth = 10000, _length = 3;
	private  double _minPerce01Align = 1, _minTULenRatio = 0, _maxTULenRatio = 100;
	private boolean _debug = false,	 _del=false, _keepBoiler = true, _keepsn = false, _keepimagefp=false, _iso6393 = false; 
	private boolean _cc = false, _keepem = false, _keepiden = false, _keepdup = false, _clean =false, _force = false,  _offlineXSLT = false;
	//private boolean _metadata = true;
	private static final String XMLlist = ".xmllist.txt", XMLHTMLlist = ".xmllist.html", TMXlist = ".tmxlist.txt";
	private static final String TMXHTMLlist = ".tmxlist.html", TMXEXT = ".tmx", HTMLEXT = ".html", UNDERSCORE_STR = "_";
	private static final Logger LOGGER = Logger.getLogger(CrawlOptions.class);
	private static final String type_p = "p", type_q = "q", type_m = "m";
	//private String ws_dir="/var/lib/tomcat6/webapps/soaplab2-results/";
	private static final String DIESIS="#", QUEST_SEPAR = ";", COLON_SEPAR = ":", DOUBLEQUEST_SEPAR = ";;";
	//private static final String PUNCT = ".";
	private static String _selectDocs = "aupdihml", default_aligner="maligna";
	private static List<String> _selectSegs = new ArrayList<String>();
	protected static Matcher skipLineM = Pattern.compile("^(\\s*)||(#.*)$").matcher("");
	private static final String CRAWL_operation = "crawl", EXPORT_operation = "export", DEDUP_operation = "dedup", PAIRDETECT_operation = "pairdetect";
	private static final String ALIGN_operation = "align", TMX_MERGE_operation = "tmxmerge", LANG_KEYS_RESOURCE = "langKeys.txt" ;
	private static final String TRANS_LINKS_ATTRS = "crossLinksAttrs.txt";

	public CrawlOptions() {
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
		options.addOption( OptionBuilder.withLongOpt("crawl")
				.withDescription( "Start or continue a crawl" )				
				.create("crawl") );
		options.addOption( OptionBuilder.withLongOpt("export")
				.withDescription( "Export crawled documents to cesDoc XML files" )				
				.create("export") );
		options.addOption( OptionBuilder.withLongOpt( "deduplicate" )
				.withDescription( "Deduplicate and discard (near) duplicate documents" )				
				.create("dedup") );
		options.addOption( OptionBuilder.withLongOpt( "pair_detection" )
				.withDescription( "Detect document pairs in crawled documents" )				
				.create("pairdetect") );
		options.addOption( OptionBuilder.withLongOpt( "align_sentences" )
				.withDescription( "Sentence align document pairs using this aligner (default is " + default_aligner + ")" )
				.hasOptionalArg()
				.create("align") );
		options.addOption( OptionBuilder.withLongOpt("tmxmerge")
				.withDescription( "Merge aligned segments from each document pair into one tmx file" )				
				.create("tmxmerge") );
		options.addOption( OptionBuilder.withLongOpt("pdm")
				.withDescription( "When creating a merged TMX file, only use sentence alignments from document pairs that have been identified by specific methods, e.g. auidh. See the pdm option." )	
				.hasArg()
				.create("pdm") );
		options.addOption( OptionBuilder.withLongOpt("segtypes")
				.withDescription( "When creating a merged TMX file, only use sentence alignments of specific types, ie. 1:1" )	
				.hasArg()
				.create("segtypes") );

		/*	options.addOption( OptionBuilder.withLongOpt( "dictionary for aligning sentences" )
				.withDescription( "This dictionary will be used for the sentence alignment" +
						"If has no argument the default dictionary of the aligner will be used if exists" )
						.hasOptionalArg()
						.create("dict") );*/
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
		options.addOption( OptionBuilder.withLongOpt( "inputdir" )
				.withDescription( "Input directory for deduplication, pairdetection, or alignment" )
				.hasArg()
				.create("i") );
		options.addOption( OptionBuilder.withLongOpt( "outputdir" )
				.withDescription( "Output directory" )
				.hasArg()
				.create("o") );
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
		options.addOption( OptionBuilder.withLongOpt( "numloops" )
				.withDescription( "Maximum number of fetch/update loops" )
				.hasArg()
				.create("n") );		
		options.addOption( OptionBuilder.withLongOpt( "crawlduration" )
				.withDescription( "Maximum crawl duration in minutes" )
				.hasArg()
				.create("c") );
		//options.addOption( OptionBuilder.withLongOpt( "genre" )
		//		.withDescription( "text file with genre types and keywords for each type." )
		//		.hasArg()
		//		.create("gnr") );
		options.addOption( OptionBuilder.withLongOpt( "keepboiler" )
				.withDescription( "Keep and annotate boilerplate content in parsed text" )				
				.create("k") );
		options.addOption( OptionBuilder.withLongOpt( "crawlUpToDepth" )
				.withDescription( "Links will not be extracted only from webpages which have been visited up to this number of cycles" )	
				.hasArg()
				.create("depth") );
		options.addOption( OptionBuilder.withLongOpt( "delete_redundant_files" )
				.withDescription( "Delete redundant crawled documents that have not been detected as members of a document pair" )				
				.create("del") );
		options.addOption( OptionBuilder.withLongOpt( "image_urls" )
				.withDescription( "Full image URLs (and not only their basenames) will be used in pair detection with common images")				
				.create("ifp") );
		options.addOption( OptionBuilder.withLongOpt( "force" )
				.withDescription( "Force a new crawl. Caution: This will remove any previously crawled data" )				
				.create("f") );		
		options.addOption( OptionBuilder.withLongOpt( "length" )
				.withDescription( "Μinimum number of tokens per text block. Shorter text blocks will be annoteted as \"ooi-length\"" )	
				.hasArg()
				.create("len") );
		options.addOption( OptionBuilder.withLongOpt( "Min_TUV_Length" )
				.withDescription( "minimum length in tokens of an acceptable TUV")
				.hasArg()
				.create("mtuvl") );
		options.addOption( OptionBuilder.withLongOpt( "MinPerce01Align" )
				.withDescription( "minimum percentage of 0:1 alignments in a TMX, to be accepted")
				.hasArg()
				.create("mpa") );
		options.addOption( OptionBuilder.withLongOpt( "MinTuLenRatio" )
				.withDescription( "minimum ratio of length (in chars) in a TU")
				.hasArg()
				.create("minlr") );
		options.addOption( OptionBuilder.withLongOpt( "MaxTuLenRatio" )
				.withDescription( "maximum ratio of length (in chars) in a TU")
				.hasArg()
				.create("maxlr") );
		options.addOption( OptionBuilder.withLongOpt( "KeepTuSameNum" )
				.withDescription( "keeps only TUs with same digits")
				.create("ksn") );
		options.addOption( OptionBuilder.withLongOpt( "KeepNonAnnotatedTu" )
				.withDescription( "keeps only non-annotated TUs")
				.create("clean") );
		options.addOption( OptionBuilder.withLongOpt( "KeepEmpty" )
				.withDescription( "keeps TUs, even if one of its TUV does not contain any letter")
				.create("keepem") );
		options.addOption( OptionBuilder.withLongOpt( "KeepIdentical" )
				.withDescription( "keeps TUs, even if its TUVs are identical after removing non-letters")
				.create("keepiden") );
		options.addOption( OptionBuilder.withLongOpt( "KeepDuplicates" )
				.withDescription( "keeps duplicate TUs")
				.create("keepdup") );
		options.addOption( OptionBuilder.withLongOpt( "minlength" )
				.withDescription( "Minimum number of tokens in crawled documents (after boilerplate detection). Shorter documents will be discarded.")
				.hasArg()
				.create("mtlen") );
		options.addOption( OptionBuilder.withLongOpt( "type" )
				.withDescription("Crawl type: m (monolingual) or  p (parallel)" )
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
		options.addOption( OptionBuilder.withLongOpt( "lang_code" )
				.withDescription( "if exists iso6393 language codes are used.")
				.create("iso6393") );
		options.addOption( OptionBuilder.withLongOpt( "offline_xslt" )
				.withDescription( "Apply an xsl transformation to generate html files during exporting.")
				.create("oxslt") );
		options.addOption( OptionBuilder.withLongOpt( "destination" )
				.withDescription( "Path to a directory where the acquired/generated resources will be stored")
				.hasArg()
				.create("dest") );
		options.addOption( OptionBuilder.withLongOpt( "url_replacements" )
				.withDescription( "A string to be replaced, separated by ';'.")
				.hasArg()
				.create("u_r") );
		options.addOption( OptionBuilder.withLongOpt( ""
				+ "pair_detection_methods" )
				.withDescription( "Α string forcing the crawler to detect pairs using one or more specific methods: "
						+ "a (links between documents), "
						+ "u (patterns in urls), "
						+ "p (common images and similar digit sequences),"
						+ "i (common images), "
						+ "d (similar digit sequences), "
						+ "s (similar html structure)")
						.hasArg()
						.create("pdm") );
		options.addOption( OptionBuilder.withLongOpt( "path_replacements" )
				.withDescription( "Put the strings to be replaced, separated by ';'. This might be useful for crawling via the web service")
				.hasArg()
				.create("p_r") );
		options.addOption( OptionBuilder.withLongOpt( "creative_commons" )
				.withDescription( "Force the alignment process to generate a merged TMX with sentence alignments only from document pairs for which an open content license has been detected.")
				.create("cc") );
		options.addOption( OptionBuilder.withLongOpt( "specific_output1" )
				.withDescription( "Not used (Specific outout1)")
				.hasArg()
				.create("o1") );
		options.addOption( OptionBuilder.withLongOpt( "specific_output2" )
				.withDescription( "Not used (Specific outout2)")
				.hasArg()
				.create("o2") );
		//options.addOption( OptionBuilder.withLongOpt( "metadata" )
		//		.withDescription( "Generate a metadata description with information for a resource created with the crawler")
		//		.create("metadata") );
		return options;
	}

	public  void parseOptions ( String[] args) {
		if ((args.length==0) || (helpAsked(args) )) {
			help();
		}	
		// create the command line parser
		CommandLineParser clParser = new GnuParser();
		try {
			CommandLine line = clParser.parse( options, args );
			if(line.hasOption( "h")) 	{	help();			}
			if(line.hasOption( "dbg"))	{	_debug = true;	}
			if(line.hasOption( "l"))	{	_loggingAppender = line.getOptionValue("l");	}

			//get operations to be applied
			if(line.hasOption( CRAWL_operation)) 		{	_operation=_operation+CRAWL_operation;			}
			if(line.hasOption( EXPORT_operation)) 		{	_operation=_operation+EXPORT_operation;			}
			if(line.hasOption( DEDUP_operation)) 		{	_operation=_operation+DEDUP_operation;			}
			if(line.hasOption( PAIRDETECT_operation)) 	{	_operation=_operation+PAIRDETECT_operation;		}
			if(line.hasOption( ALIGN_operation)) 		{	_operation=_operation+ALIGN_operation;			}
			if(line.hasOption( TMX_MERGE_operation)) 	{	_operation=_operation+TMX_MERGE_operation;		}

			//get parameters concerning languages
			if(line.hasOption( "lang")) {
				String ll = line.getOptionValue("lang").toLowerCase();
				if (line.getOptionValue("lang").toLowerCase().equals("g"))
					ll = getSupportedLanguages();
				_language = LangDetectUtils.updateLanguages(ll,true);
				_targetedLangs =_language.split(QUEST_SEPAR);
				_langKeys = findKeys4lang(_language);
				_mapLangs = mapLangs(_language);
				_langPairs = findLangPairs(_language);
				_linkAttrs = findTransLinks();
				checkAnalyzers(_language);
			}else{
				if (_operation.contains(CRAWL_operation) || _operation.contains(EXPORT_operation) || _operation.contains(PAIRDETECT_operation) 
						|| _operation.contains(ALIGN_operation) ||  _operation.contains(TMX_MERGE_operation)){
					LOGGER.error("No languages have been defined.");
					System.exit(0);
				}
			}

			if(line.hasOption( "i"))			{	_inputDir = new File(line.getOptionValue("i"));			}
			if(line.hasOption( "o"))			{	_outputDir = new File(line.getOptionValue("o"));		}

			if (_operation.contains(CRAWL_operation)){
				getParams4Crawl(line);
			}
			if(line.hasOption( "a"))
				_agentName = line.getOptionValue("a").replace(" ", "_");
			else
				_agentName="A";
			
			if (line.hasOption( "bs")) {
				_outBaseName = new File(line.getOptionValue("bs")+UNDERSCORE_STR+_agentName);			_outBaseName = _outBaseName.getAbsoluteFile();
				_outputFile = new File(line.getOptionValue("bs")+UNDERSCORE_STR+_agentName+XMLlist);	_outputFile = _outputFile.getAbsoluteFile();
				if(line.hasOption( "oxslt")) {
					_outputFileHTML = new File(line.getOptionValue("bs")+UNDERSCORE_STR+_agentName+XMLHTMLlist);
					_outputFileHTML = _outputFileHTML.getAbsoluteFile();
				} 
			}else{
				if (_operation.contains(EXPORT_operation) || _operation.contains(DEDUP_operation) || _operation.contains(PAIRDETECT_operation) || _operation.contains(ALIGN_operation) ||  _operation.contains(TMX_MERGE_operation)){
					LOGGER.error("Outputfile required ");
					System.exit(0);
				}
			}
			if (_operation.contains(ALIGN_operation) || _operation.contains(TMX_MERGE_operation)){
				if(line.hasOption("iso6393"))
					_iso6393=true;
			}
			if (_operation.contains(TMX_MERGE_operation) ){
				getParams4MergingAlignments(line);
			}
			if (_operation.contains(ALIGN_operation)){
				getParams4Align(line);
			}
			if (_operation.contains(CRAWL_operation) || _operation.contains(EXPORT_operation)){
				getParams4Topicness(line);
				getParams4ContentProps(line);
			}
			if (_operation.contains(CRAWL_operation) || _operation.contains(EXPORT_operation) ||  _operation.contains(ALIGN_operation) ||  _operation.contains(TMX_MERGE_operation) ){
				if(line.hasOption( "cfg")) {
					_config = line.getOptionValue("cfg");
				}	
			}
			if (_operation.contains(PAIRDETECT_operation)){
				if(line.hasOption( "ifp")) 
					_keepimagefp  = true;
				if(line.hasOption( "pdm"))
					_methods = line.getOptionValue("pdm");
				if(line.hasOption( "del")) 		
					_del  = true;
				String temp= line.getOptionValue("u_r");
				if (temp!=null){
					String[] aa=temp.split(DOUBLEQUEST_SEPAR);
					String[][] urls_repls =new String[aa.length][2];  
					for (int ii=0;ii<aa.length;ii++){
						String[] bb = aa[ii].split(QUEST_SEPAR);
						if (bb.length<1){
							LOGGER.error("the argument for URL replacements is not correct." +
									" Use ;; to seperate pairs and ; to separate the parts of each pair." +
									"Check that none of the parts is empty.");
							help();
						}else{
							if (bb.length==1){
								urls_repls[ii][0] = bb[0]; urls_repls[ii][1] = "";
							}else{
								urls_repls[ii][0] = bb[0]; urls_repls[ii][1] = bb[1];
							}
						}
					}
					_urls_repls=urls_repls;
				}
			}
			if(line.hasOption( "p_r")) {
				_paths_repl= line.getOptionValue("p_r").trim();
				if (_paths_repl.endsWith("/")){
					_paths_repl=_paths_repl.substring(0, _paths_repl.length()-1);
				}
			}
			if(line.hasOption( "oxslt")) 
				_offlineXSLT  = true;
		} catch( ParseException exp ) {
			// oops, something went wrong
			System.err.println( "Parsing options failed.  Reason: " + exp.getMessage() );			
			System.exit(64);
		}
	}
	private List<String[]> findTransLinks() {
		List<String[]> res = new ArrayList<String[]>();
		try {
			URL svURL = ReadResources.class.getClassLoader().getResource(TRANS_LINKS_ATTRS);
			BufferedReader in = new BufferedReader(new InputStreamReader(svURL.openStream()));
			String str;
			while ((str = in.readLine()) != null) {
				if (str.startsWith(DIESIS))
					continue;
				String[] temp = str.split(COLON_SEPAR);
				res.add(temp);
			}
		} catch (IOException e) {
			LOGGER.error("Problem in reading the file for crossLinksAttrs.");
		}
		return res;
	}

	private List<String> findLangPairs(String language) {
		String[] langs = language.split(QUEST_SEPAR); 
		List<String> lang_pairs = new ArrayList<String>();
		if (langs.length>1){
			for (int ii=0;ii<langs.length-1;ii++){
				for (int jj=ii+1;jj<langs.length;jj++){
					lang_pairs.add(langs[ii]+QUEST_SEPAR+langs[jj]);
				}
			}
		}
		return lang_pairs;
	}
	/**
	 * parses commandLine and gets _keepBoiler, _del, _length, _minTokensNumber
	 * @param line
	 */
	private void getParams4ContentProps(CommandLine line) {
		if(line.hasOption( "k")) 		{	_keepBoiler  = true;				}
		if(line.hasOption( "del")) 		{	_del  = true;				}
		if(line.hasOption( "len")) 		{	_length = Integer.parseInt(line.getOptionValue("len"));				} 
		if(line.hasOption( "mtlen")) 	{	_minTokensNumber = Integer.parseInt(line.getOptionValue("mtlen"));	} 
	}
	/**
	 * parses the command Line and gets _topic (file with terms, etc) and _descr (topicName)
	 * @param line
	 */
	private void getParams4Topicness(CommandLine line) {
		if(line.hasOption( "tc")) {
			_topic = new File(line.getOptionValue("tc"));
		}
		if(line.hasOption( "dom")) {
			if (_topic==null){
				LOGGER.info("The targeted domain is defined but a topic definition is not applied. " +
						"So, the domain will be used as provided, i.e. it will not be identified.");
				//help();
			}
			_descr = line.getOptionValue("dom");
		}else{
			if (_topic!=null){
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
		if(line.hasOption( "a")){
			_agentName = line.getOptionValue("a").replace(" ", "_");
		}else{
			LOGGER.error("option -a is required");
			help();
		}
		if (line.hasOption("type"))
			_type = line.getOptionValue("type");
		else{
			LOGGER.error("option -type is required");
			help();
		}
		if (line.hasOption("u")) {
			_urls = line.getOptionValue("u");
			File f = new File(_urls);
			if (f.exists()==false){
				LOGGER.error("The seed file does not exist.");
				help();
			}
		}
		if(line.hasOption("t")) 			{	_threads = Integer.parseInt(line.getOptionValue("t"));	}							
		if(line.hasOption("n")) 			{	_numLoops = Integer.parseInt(line.getOptionValue("n"));	_crawlDuration=0;	}
		if(line.hasOption("c")) 			{	_crawlDuration = Integer.parseInt(line.getOptionValue("c"));	}
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
			ws_dir = FilenameUtils.concat(line.getOptionValue("dest"), _agentName+UNDERSCORE_STR+timeStamp) ;
		else
			ws_dir=_agentName+UNDERSCORE_STR+timeStamp;

		if(!line.hasOption( "o")) 
			_outputDir = new File(FilenameUtils.concat(ws_dir, UUID.randomUUID().toString())); 				 	

		if (_type.equals(type_m)){
			if(line.hasOption( "filter")) 
				_filter = line.getOptionValue("filter");
			else
				_filter = ".*";
		}
		if (_type.equals(type_p) | _type.equals(type_q)){
			if (!_language.contains(QUEST_SEPAR)){
				LOGGER.error("You crawl for parallel or comparable but only 1 language has been defined.");
				help();
			}
		}
		if (_type.equals(type_p)){
			URL url;
			if (!line.hasOption( "filter")){
				ArrayList<String> seed_list = readSeedList();
				ILSPFCUrlNormalizer normalizer = new ILSPFCUrlNormalizer();
				if (!seed_list.isEmpty()){
					if (seed_list.size()==1){
						try {
							url = new URL(normalizer.normalize(seed_list.get(0)));
							_domain = FCStringUtils.removeWWW(url.getHost());				//LOGGER.debug("Examining web domain : " + _domain);
							_maindomain = FCStringUtils.removeWWW(processhost(_domain));	//LOGGER.debug("Examining second domain : " + _maindomain);
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
										_domain=null;
										_maindomain=null;
									}
								}else{
									_domain = FCStringUtils.removeWWW(host);
									String mainhost="";
									char c = host.charAt(0);
									if (Character.isDigit(c)){
										mainhost=host;
									}else{
										mainhost=processhost(host);
									}
									_maindomain = FCStringUtils.removeWWW(mainhost);
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
				String t = _domain.replace(".", "\\.");
				//_filter = ".*"+t+".*";
				_filter = "^[^/]*\\."+t+".*";
			}else{
				_domain=null;
				_maindomain=null;
				_filter = line.getOptionValue("filter");
			}
		}
	}

	/**
	 * read the seed list, skip commented or empty lines
	 * @return
	 */
	private ArrayList<String> readSeedList() {
		ArrayList<String> seed_list = new ArrayList<String>();
		BufferedReader rdr;
		try {
			rdr = new BufferedReader(new InputStreamReader(new FileInputStream(_urls),"utf8"));
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
	 * parses the command line and returns:
	 * The aligner @param _aligner,
	 * The path of the directory with available dictionaries to be used @param _dict,
	 * The file in which the list of paths of generated TMXs will be stored @param _outputFileTMX,
	 * The file in which the list of links pointing to generated TMXs will be stored @param _outputFileHTMLTMX.
	 * @param line
	 */
	private void getParams4Align(CommandLine line) {
		_aligner = line.getOptionValue("align");
		if (_aligner==null)
			_aligner = default_aligner;
		if (line.hasOption( "dict")) {
			_dict = line.getOptionValue("dict");
			if (_dict==null) 
				_dict = "default";
		}else
			_dict=null;
		_outputFileTMX = new File(line.getOptionValue("bs")+UNDERSCORE_STR+_agentName+TMXlist);
		_outputFileTMX = _outputFileTMX.getAbsoluteFile();	
		if (line.hasOption( "oxslt")){
			_outputFileHTMLTMX = new File(line.getOptionValue("bs")+UNDERSCORE_STR+_agentName+TMXHTMLlist);
			_outputFileHTMLTMX =_outputFileHTMLTMX.getAbsoluteFile();	
		}
	}

	/**
	 * parses the command line and returns:
	 * The types of documents pairs from which the alignments will be selected @param _selectDocs,
	 * The types of alignments which will be selected @param _selectSegs,
	 * The file in which the selected alignments will be stored @param _outputFile_mergedTMX,
	 * The xslt transformed file of the selected alignments @param _outputFile_mergedTMXHTML.
	 * @param line
	 */
	private void getParams4MergingAlignments(CommandLine line) {
		_outputFile_mergedTMX = new File(line.getOptionValue("bs")+UNDERSCORE_STR+_agentName+TMXEXT);
		_outputFile_mergedTMX = _outputFile_mergedTMX.getAbsoluteFile();
		if (line.hasOption( "oxslt")){
			_outputFile_mergedTMXHTML = new File(line.getOptionValue("bs")+UNDERSCORE_STR+_agentName+TMXEXT+HTMLEXT);
			_outputFile_mergedTMXHTML =_outputFile_mergedTMXHTML.getAbsoluteFile();
		}
		if (line.hasOption("pdm"))
			_selectDocs = line.getOptionValue("pdm");
		if(line.hasOption( "mtuvl"))
			_minTuvLen = Integer.parseInt(line.getOptionValue("mtuvl"));
		if(line.hasOption( "mpa"))
			_minPerce01Align = Double.parseDouble(line.getOptionValue("mpa"));
		if(line.hasOption( "minlr"))
			_minTULenRatio = Double.parseDouble(line.getOptionValue("minlr"));
		if(line.hasOption( "maxlr"))
			_maxTULenRatio = Double.parseDouble(line.getOptionValue("maxlr"));
		if(line.hasOption( "ksn"))
			_keepsn = true;
		if(line.hasOption( "keepem"))
			_keepem = true;
		if (line.hasOption("clean"))
			_clean=true;
		if(line.hasOption( "keepiden"))
			_keepiden = true;
		if(line.hasOption( "keepdup"))
			_keepdup = true;
		if (line.hasOption("segtypes")){
			String[] temp= line.getOptionValue("segtypes").split(QUEST_SEPAR); 
			for (String str:temp){
				_selectSegs.add(str);
			}
		}else{
			_selectSegs.add(defaultSegType);
		}
		if (line.hasOption("cc"))
			_cc=true;
		if (line.hasOption("o1"))
			_o1=new File(line.getOptionValue("o1"));
		if (line.hasOption("o2"))
			_o2=new File(line.getOptionValue("o2"));
	}

	/**
	 * checks if analyzers of the targeted languages are available
	 * @param _languages
	 */
	private void checkAnalyzers(String languages) {
		String[] langs=languages.split(QUEST_SEPAR);
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
		String[] langs = _language.split(QUEST_SEPAR);
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
		String[] langs = _language.split(QUEST_SEPAR);
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

	private String getSupportedLanguages() {
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

	public  void help(){
		printHelp( APPNAME , options );
		System.exit(0);
	}

	public  void printHelp(String program, Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.setWidth(100);
		formatter.printHelp( program, options );
	}
	public String getLanguage() { 
		return _language;
	}
	public String[] getTargetedLangs() {
		return _targetedLangs;
	}
	public String[] getLangKeys() {
		return _langKeys;
	}
	public HashMap<String,String> getMapLangs() {
		return _mapLangs;
	}
	public List<String> getLangPairs() {
		return _langPairs;
	}
	public List<String[]> getTransLinksAttrs() {
		return _linkAttrs;
	}
	public File getTopic() {
		return _topic;
	}
	public  String getDomain() {
		return _domain;
	}
	public  String getMainDomain() {
		return _maindomain;
	}
	public  boolean isDebug() {
		return _debug;
	}
	public  String getLoggingAppender() {
		return _loggingAppender;
	}
	public  File getInputDir() {
		return _inputDir;
	}
	public  File getOutputDir() {
		return _outputDir;
	}
	public  File getOutputFile() {
		return _outputFile;
	}
	public  File getOutputFileHTML() {
		return _outputFileHTML;
	}
	public  File getOutputFileTMX() {
		return _outputFileTMX;
	}
	public  File getOutputFileHTMLTMX() {
		return _outputFileHTMLTMX;
	}
	public  File getMergedTMX() {
		return _outputFile_mergedTMX;
	}
	public  File getMergedTMXHTML() {
		return _outputFile_mergedTMXHTML;
	}
	public  File getBaseName() {
		return _outBaseName;
	}
	public  String getAgentName() {
		return _agentName;
	}
	public  int getThreads() {
		return _threads;
	}
	public  int getNumLoops() {
		return _numLoops;
	}
	public  int getCrawlDuration() {
		return _crawlDuration;
	}
	public String getUrls() {
		return _urls;
	}
	public boolean keepBoiler() {
		return _keepBoiler;
	}
	public String getPairMethods() {
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
	}
	public boolean Force() {
		return _force;
	}
	public String getConfig(){
		return _config;
	}
	public URL getGenre(){
		return _genre;
	}
	public int getlength() {
		return _length;
	}
	public int getMinTuvLen() {
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
	public int getTokensNumber() {
		return _minTokensNumber;
	}
	public String getType() {
		return _type;
	}
	public String getTargetedDomain() {
		return _descr;
	}
	public String getFilter() {
		return _filter;
	}
	public String getStoreFilter() {
		return _storefilter;
	}
	public String[][] getUrlReplaces() {
		return _urls_repls;
	}
	public String getPathReplace() {
		return _paths_repl;
	}
	public String getDesc() {
		return _descr;
	}
	public List<String> getSegTypes() {
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
	}
	public boolean useISO6393() {
		return _iso6393;
	}
	public int getminTokenslength() {
		return _minTokensNumber;
	}
	public int getCrawlLevel() {
		return _level;
	}
	public int upToDepth() {
		return _depth;
	}
	public String getOperation() {
		return _operation;
	}
	private static boolean helpAsked(String[] args) {
		for (int ii=0; ii<args.length;ii++){
			if (args[ii].equals("-h") | args[ii].equals("-help") | args[ii].equals("--help") | args[ii].equals("--h")){
				return true;
			}		
		}
		return false;
	}
	public boolean getDel() {
		return _del;
	}
	public boolean getCC() {
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
	}
	public File getO1() {
		return _o1;
	}
	public File getO2() {
		return _o2;
	}
}
