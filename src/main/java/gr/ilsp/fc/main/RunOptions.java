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
import java.util.Arrays;
//import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
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
//import org.apache.commons.configuration.ConfigurationException;
//import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.net.InternetDomainName;


//import gr.ilsp.fc.crawl.Crawler;
import gr.ilsp.fc.langdetect.LangDetectUtils;
import gr.ilsp.fc.langdetect.LangDetector;
import gr.ilsp.fc.operations.ILSPFCUrlNormalizer;
import gr.ilsp.fc.readwrite.ReadResources;
import gr.ilsp.fc.utils.AnalyzerFactory;
import gr.ilsp.fc.utils.FCStringUtils;
import gr.ilsp.fc.utils.ISOLangCodes;
import gr.ilsp.nlp.commons.Constants;

public class RunOptions {
	public static int NO_CRAWL_DURATION = 0;
	private  final String APPNAME = "ILSP Focused Crawler";
	private static final Logger LOGGER = Logger.getLogger(RunOptions.class);
	private boolean _debug = false;
	private String _loggingAppender = null;

	private Options options;
	//private String ws_dir="/var/lib/tomcat6/webapps/soaplab2-results/";

	private static final String XMLlist = ".xmllist.txt", XMLHTMLlist = ".xmllist.html", TMXlist = ".tmxlist.txt", xml_type ="xml";
	private static final String TMXHTMLlist = ".tmxlist.html", TMXEXT = ".tmx", HTMLEXT = ".html";
	private static final String DIESIS="#", DOUBLESEMICOLON_SEPAR = ";;";
	
	//operations
	private static final String CRAWL_operation = "crawl", EXPORT_operation = "export", DEDUP_operation = "dedup", PAIRDETECT_operation = "pairdetect";
	private static final String ALIGN_operation = "align", TMX_MERGE_operation = "tmxmerge", MONO_MERGE_operation = "monomerge";
	private static final String type_p = "p", type_q = "q", type_m = "m";
	private String _operation="";

	//resources (integrated or user provided)
	private static final String LANG_KEYS_RESOURCE = "langKeys.txt" ;
	private static final String TRANS_LINKS_ATTRS = "crossLinksAttrs.txt";
	//private static final String DEFAULT_MONO_CONFIG_FILE = "FMC_config.xml";
	//private static final String DEFAULT_BI_CONFIG_FILE = "FBC_config.xml";
	//private String default_genrefile="genres_keys.txt";
	private URL _genre;
	private File _topicFile=null, _negwordsFile;
	private String _usertopic="",  _config;

	// linguality params
	private static String defaultlangDetectorId="langdetect";
	private String[] _langKeys, _targetedLangs;
	private HashMap<String, String> _mapLangs;
	private List<String> _langPairs;
	private String _language;
	private boolean _iso6393 = false;
	private LangDetector _langDetector;

	//crawl params
	private String _webdomain=null, _webmaindomain=null,  _filter=null, _storefilter=null;
	private String _type="m", _agentName="ILSP-FC", ws_dir;
	private List<String[]> _linkAttrs;
	private  int _threads = 10, _numLoops = 0, _crawlDuration = 10, _MinDocLen = 100, _minTuvLen = 0, _level = 100, _depth = 10000, _MinParLen = 3;
	private boolean _keepBoiler = true, _force = false;
	private File _seedFile;

	//export params
	private File _outputDir, _inputDir, _outBaseName, _outputFile, _outputFileHTML;
	private File _o1 , _o2;
	private String[][] _urls_repls=null;
	private boolean  _runoffline=false, _textexport=false;

	//pairdetect params
	private String _methods = "aupdih";
	private boolean _del=false,  _keepimagefp=false;
	private  int _maxSize=1000000000;

	//tmxmerge params
	private  double _minPerce01Align = 1, _minPerceM1Align = 1, _minTULenRatio = 0, _maxTULenRatio = 100;
	private boolean _cc = false, _keepem = false, _keepiden = false, _keepdup = false, _keepneardup = false, _clean =false, _keepsn = false, _samesymb=false;
	private String defaultSegType="1:1";
	private File _outputFile_mergedTMX, _outputFile_mergedTMXHTML;
	private static String _selectDocs = "aupdih";
	private static List<String> _selectSegs = new ArrayList<String>();
	private int _maxSampleSize = 1500;		//no more than this value
	
	//monomerge params
	private String _corpuslevel="par";

	//alignment params
	private List<String> alignerIds = Arrays.asList(new String[] {"hunalign","maligna"});
	private static String default_aligner="maligna";
	private File _outputFileTMX, _outputFileHTMLTMX;
	private String _aligner=null, _dict=null, _dictpath=null;

	protected static Matcher skipLineM = Pattern.compile("^(\\s*)||(#.*)$").matcher("");

	private static CompositeConfiguration _configuration;
	private boolean  _offlineXSLT = false;

	//deduplication params
	private static double _dedupinter_thr=0.7; //intersection of common paragraphs
	private static int _dedupMIN_TOK_LEN = 3; //tokens with less that MIN_TOK_LEN letters are excluded
	private static int _dedupMIN_PAR_LEN = 3; //paragraphs with less than MIN_PAR_LEN tokens are excluded
	private static String _dedupmethod="0";	  //"1" on document level, "2" on paragraph level, "0" both  
	private static String _dedupInputType = "xml";
	private static Set<String> _dedupExcludefiles=null;

	public RunOptions() {
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

		//operations
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
		options.addOption( OptionBuilder.withLongOpt("monomerge")
				.withDescription( "Construct a monolingual collection by merging the already exported cesDoc files" )				
				.create("monomerge") );

		//linguality
		options.addOption( OptionBuilder.withLongOpt( "languages" )
				.withDescription( "Two or three letter ISO code(s) of target language(s), e.g. el (for a monolingual crawl for Greek content) or en;el (for a bilingual crawl)" )
				.hasArg()
				.create("lang") );
		options.addOption( OptionBuilder.withLongOpt( "lang_code" )
				.withDescription( "if exists iso6393 language codes are used.")
				.create("iso6393") );

		//resources (integrated or user provided)
		options.addOption( OptionBuilder.withLongOpt( "config" )
				.withDescription( "Path to the XML configuration file" )
				.hasArg()
				.create("cfg") );
		options.addOption( OptionBuilder.withLongOpt( "topicFile" )
				.withDescription( "Path to a file with the topic definition" )
				.hasArg()
				.create("tc") );
		options.addOption( OptionBuilder.withLongOpt( "UserTopic" )
				.withDescription( "A descriptive title for the targeted domain" )
				.hasArg()
				.create("dom") );
		options.addOption( OptionBuilder.withLongOpt( "negwords" )
				.withDescription( "Path to file containing negative words")
				.hasArg()
				.create("neg") );
		//options.addOption( OptionBuilder.withLongOpt( "genre" )
		//		.withDescription( "text file with genre types and keywords for each type." )
		//		.hasArg()
		//		.create("gnr") );
		/*	options.addOption( OptionBuilder.withLongOpt( "dictionary for aligning sentences" )
		.withDescription( "This dictionary will be used for the sentence alignment" +
				"If has no argument the default dictionary of the aligner will be used if exists" )
				.hasOptionalArg()
				.create("dict") );*/

		//crawl params
		options.addOption( OptionBuilder.withLongOpt( "urls" )
				.withDescription( "File with seed urls used to initialize the crawl" )
				.hasArg()
				.create("u") );
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
		options.addOption( OptionBuilder.withLongOpt( "CrawlDurationMinutes" )
				.withDescription( "Crawl Duration in Minutes" )
				.hasArg()
				.create("cdm") );
		options.addOption( OptionBuilder.withLongOpt( "keepboiler" )
				.withDescription( "Keep and annotate boilerplate content in parsed text" )				
				.create("k") );
		options.addOption( OptionBuilder.withLongOpt( "crawlUpToDepth" )
				.withDescription( "Links will be extracted only from webpages which have been visited up to this number of cycles" )	
				.hasArg()
				.create("depth") );
		options.addOption( OptionBuilder.withLongOpt( "levelfilter" )
				.withDescription( "Use this option to force the crawler visit only urls that are up to a specific level of a webdomain webdomains." )	
				.hasArg()
				.create("level") );
		options.addOption( OptionBuilder.withLongOpt( "force" )
				.withDescription( "Force a new crawl. Caution: This will remove any previously crawled data" )				
				.create("f") );
		options.addOption( OptionBuilder.withLongOpt( "minlength" )
				.withDescription( "Minimum number of tokens in crawled documents (after boilerplate detection). Shorter documents will be discarded.")
				.hasArg()
				.create("mtlen") );
		options.addOption( OptionBuilder.withLongOpt( "type" )
				.withDescription("Crawl type: m (monolingual) or  p (parallel)" )
				.hasArg()
				.create("type") );
		options.addOption( OptionBuilder.withLongOpt( "fetchfilter" )
				.withDescription( "Use this regex to force the crawler to crawl only in specific sub webdomains. Webpages with urls that do not match this regex will not be fetched." )	
				.hasArg()
				.create("filter") );
		options.addOption( OptionBuilder.withLongOpt( "storefilter" )
				.withDescription( "Use this regex to force the crawler to store only webpages with urls that match this regex." )	
				.hasArg()
				.create("storefilter") );
		//options.addOption( OptionBuilder.withLongOpt("stay_in_webdomain")
		//		.withDescription( "Force the monolingual crawler to stay in a specific web domain" )				
		//		.create("d") );


		//input/output params 
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
		options.addOption( OptionBuilder.withLongOpt( "destination" )
				.withDescription( "Path to a directory where the acquired/generated resources will be stored")
				.hasArg()
				.create("dest") );
		options.addOption( OptionBuilder.withLongOpt( "offline_xslt" )
				.withDescription( "Apply an xsl transformation to generate html files during exporting.")
				.create("oxslt") );

		//tmxmerge params
		options.addOption( OptionBuilder.withLongOpt( "Min_TUV_Length" )
				.withDescription( "minimum length in tokens of an acceptable TUV")
				.hasArg()
				.create("mtuvl") );
		options.addOption( OptionBuilder.withLongOpt( "MinPerce01Align" )
				.withDescription( "minimum percentage of 0:1 alignments in a TMX, to be accepted")
				.hasArg()
				.create("mpa") );
		options.addOption( OptionBuilder.withLongOpt( "MinPerceM1Align" )
				.withDescription( "minimum percentage of many:1 alignments in a TMX, to be accepted")
				.hasArg()
				.create("mpma") );
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
		options.addOption( OptionBuilder.withLongOpt( "KeepTUsWithSameSymbolsInTUVs" )
				.withDescription( "keeps only Tus in which TUVs have the same symbols")
				.create("samesymb") );
		options.addOption( OptionBuilder.withLongOpt( "KeepEmptyTUs" )
				.withDescription( "keeps TUs, even if one of its TUV does not contain any letter")
				.create("keepem") );
		options.addOption( OptionBuilder.withLongOpt( "KeepIdenticalTUs" )
				.withDescription( "keeps TUs, even if its TUVs are identical after removing non-letters")
				.create("keepiden") );
		options.addOption( OptionBuilder.withLongOpt( "KeepDuplicatesTUs" )
				.withDescription( "keeps duplicate TUs")
				.create("keepdup") );
		options.addOption( OptionBuilder.withLongOpt( "KeepNearDuplicatesTUs" )
				.withDescription( "keeps (near)duplicate TUs")
				.create("keepneardup") );
		options.addOption( OptionBuilder.withLongOpt("segtypes")
				.withDescription( "When creating a merged TMX file, only use sentence alignments of specific types, ie. 1:1" )	
				.hasArg()
				.create("segtypes") );
		options.addOption( OptionBuilder.withLongOpt( "creative_commons" )
				.withDescription( "Force the tmxmerging process to generate a merged TMX with sentence alignments only from document pairs for which an open content license has been detected.")
				.create("cc") );
		options.addOption( OptionBuilder.withLongOpt( "Max_TUs" )
				.withDescription( "maximum number of TUs")
				.hasArg()
				.create("size") );
		options.addOption( OptionBuilder.withLongOpt( "MaxSampleSize_TUs" )
				.withDescription( "max sample size in TUs")
				.hasArg()
				.create("samplesize") );
		//pair detect params
		options.addOption( OptionBuilder.withLongOpt( "url_replacements" )
				.withDescription( "A string to be replaced, separated by ';'.")
				.hasArg()
				.create("u_r") );
		options.addOption( OptionBuilder.withLongOpt( ""
				+ "pair_detection_methods" )
				.withDescription( "Α string forcing the tool to detect pairs using one or more specific methods: "
						+ "a (links between documents), "
						+ "u (patterns in urls), "
						+ "p (common images and similar digit sequences),"
						+ "i (common images), "
						+ "d (similar digit sequences), "
						+ "u (high similarity of html structure)"
						+ "m (medium similarity of html structure)"
						+ "l (low similarity of html structure)"
						+ " When creating a merged TMX file, only sentence alignments from document pairs that have been identified by specific methods, will be used.")
						.hasArg()
						.create("pdm") );
		options.addOption( OptionBuilder.withLongOpt( "delete_redundant_files" )
				.withDescription( "Delete redundant crawled documents that have not been detected as members of a document pair" )				
				.create("del") );
		options.addOption( OptionBuilder.withLongOpt( "image_urls" )
				.withDescription( "Full image URLs (and not only their basenames) will be used in pair detection with common images")				
				.create("ifp") );

		//export params
		options.addOption( OptionBuilder.withLongOpt( "specific_output1" )
				.withDescription( "Not used (Specific outout1)")
				.hasArg()
				.create("o1") );
		options.addOption( OptionBuilder.withLongOpt( "specific_output2" )
				.withDescription( "Not used (Specific outout2)")
				.hasArg()
				.create("o2") );
		options.addOption( OptionBuilder.withLongOpt( "textexport" )
				.withDescription( "Export raw txt files" )				
				.create("te") );
		options.addOption( OptionBuilder.withLongOpt( "offlineExport" )
				.withDescription( "Export files not coming from crawls" )				
				.create("offline") );
		options.addOption( OptionBuilder.withLongOpt( "path_replacements" )
				.withDescription( "Put the strings to be replaced, separated by ';'.")
				.hasArg()
				.create("p_r") );
		options.addOption( OptionBuilder.withLongOpt( "length" )
				.withDescription( "Μinimum number of tokens per text block. Shorter text blocks will be annoteted as \"ooi-length\"" )	
				.hasArg()
				.create("len") );

		//deduplication parameters
		options.addOption( OptionBuilder.withLongOpt( "DedupMethod" )
				.withDescription( "Method type for deduplication: "
						+ "1 for Deduplication by using lists and MD5 method."
						+ "2 for Deduplication based on common paragraphs."
						+ "0 for applying both methods." )
						.hasArg()
						.create("dedup_meth") );
		options.addOption( OptionBuilder.withLongOpt( "dedup_minTokLen" )
				.withDescription( "Tokens with less than MIN_TOK_LEN (default is 3) are excluded from content" )
				.hasArg()
				.create("dedup_mtl") );
		options.addOption( OptionBuilder.withLongOpt( "dedup_minParLen_inToks" )
				.withDescription( "Paragraphs with less than MIN_PAR_LEN (default is 3) tokens are excluded from content" )	
				.hasArg()
				.create("dedup_mpl") );
		options.addOption( OptionBuilder.withLongOpt( "dedup_intersectThr_pars" )
				.withDescription( "Documents for which the ratio the common paragraphs"
						+ " with the shortest of them is more than this threshold are considered duplicates")	
						.hasArg()
						.create("dedup_ithr") );
		options.addOption( OptionBuilder.withLongOpt( "dedup_inputType" )
				.withDescription( "type of input files, default is xml, also supports txt" )
				.hasArg()
				.create("dedup_intype") );
		options.addOption( OptionBuilder.withLongOpt( "dedupexclude_files" )
				.withDescription( "cesDocFiles to be excluded for deduplication separated by \";\"" )	
				.hasArg()
				.create("dedup_ex") );

		//monomerging options
		options.addOption( OptionBuilder.withLongOpt( "level of corpus' item" )
				.withDescription( "corpus consists of txt documents (default), or paragraphs, or sentences")
				.hasArg()
				.create("corpuslevel") );

		return options;
	}

	public  void parseOptions ( String[] args) {
		if ((args.length==0) || (helpAsked(args) )) {
			help();
		}
		//String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
		
		// create the command line parser
		CommandLineParser clParser = new GnuParser();
		try {
			CommandLine line = clParser.parse( options, args );
			if(line.hasOption( "h")) 	{	help();			}
			if(line.hasOption( "dbg"))	{	_debug = true;	}
			if(line.hasOption( "l"))	{	_loggingAppender = line.getOptionValue("l");	}

			//get operations to be applied
			if(line.hasOption( CRAWL_operation)) 
				_operation=_operation+CRAWL_operation;	
			if(line.hasOption( EXPORT_operation)) 
				_operation=_operation+EXPORT_operation;	
			if(line.hasOption( DEDUP_operation)) 
				_operation=_operation+DEDUP_operation;	
			if(line.hasOption( PAIRDETECT_operation)) 
				_operation=_operation+PAIRDETECT_operation;	
			if(line.hasOption( ALIGN_operation)) 	
				_operation=_operation+ALIGN_operation;	
			if(line.hasOption( TMX_MERGE_operation)) 
				_operation=_operation+TMX_MERGE_operation;	
			if(line.hasOption( MONO_MERGE_operation)) 
				_operation=_operation+MONO_MERGE_operation;
			if (!isValidOperationsSequence(_operation)){
				LOGGER.info("The sequence of asked operations is not valid!");
				System.exit(0);
			}
			if(line.hasOption( "offline"))
				_runoffline = true;
			//get parameters concerning languages
			if(line.hasOption( "lang")) {
				String ll = line.getOptionValue("lang").toLowerCase();	
				if (line.getOptionValue("lang").toLowerCase().equals("g"))
					ll = ReadResources.getSupportedLanguages();
				_language = LangDetectUtils.updateLanguages(ll,true);
				_targetedLangs =_language.split(Constants.SEMICOLON);
				_langKeys = findKeys4lang(_language);
				_mapLangs = mapLangs(_language);
				_langPairs = findLangPairs(_language);
				_linkAttrs = findTransLinks();
				checkAnalyzers(_language);
				//_langDetector = loadLangDetectors(_targetedLangs);
				_langDetector = LangDetectUtils.loadLangDetectors(_targetedLangs,defaultlangDetectorId);
			}else{
				if (!_operation.contains(DEDUP_operation)){ //for all tasks but deduplication, language(s) is required
					LOGGER.error("No targeted languages have been defined.");
					System.exit(0);
				}
			}
			if(line.hasOption( "a"))
				_agentName = line.getOptionValue("a").replace(Constants.SPACE, Constants.UNDERSCORE);

			if (_operation.contains(CRAWL_operation))
				getParams4Crawl(line);
			if (_operation.contains(CRAWL_operation) || _operation.contains(EXPORT_operation)){
				getParams4Topicness(line);
				getParams4ContentProps(line);
				if(line.hasOption( "neg")) 
					_negwordsFile = new File(line.getOptionValue("neg")).getAbsoluteFile();

			}
			if (_operation.contains(DEDUP_operation))
				getParams4Dedup(line);

			if (_operation.contains(EXPORT_operation) || _operation.contains(DEDUP_operation) 
					|| _operation.contains(PAIRDETECT_operation) || _operation.contains(ALIGN_operation)
					|| _operation.contains(TMX_MERGE_operation) ||  _operation.contains(MONO_MERGE_operation)){
				if(line.hasOption( "i"))	
					_inputDir = new File(line.getOptionValue("i")).getAbsoluteFile();		
				if(line.hasOption( "o"))
					_outputDir = new File(line.getOptionValue("o")).getAbsoluteFile();		
				else{
					if (_operation.equals(EXPORT_operation)){
						_outputDir =  new File(FilenameUtils.concat(_inputDir.getAbsolutePath(), xml_type)).getAbsoluteFile();
						if (_runoffline)
							_outputDir = new File(line.getOptionValue("o")).getAbsoluteFile();
					}
				}
				if (_operation.equals(EXPORT_operation)){
					if (line.hasOption("te"))
						_textexport = true;		
				}
				/*if (line.hasOption( "bs")) {
					_outBaseName = new File(line.getOptionValue("bs")+Constants.UNDERSCORE+_agentName+ Constants.UNDERSCORE+timeStamp).getAbsoluteFile();
					_outputFile = new File(line.getOptionValue("bs")+Constants.UNDERSCORE+_agentName+ Constants.UNDERSCORE+timeStamp+XMLlist).getAbsoluteFile();
					if(line.hasOption( "oxslt")){
						_offlineXSLT  = true;
						_outputFileHTML = new File(line.getOptionValue("bs")+Constants.UNDERSCORE+_agentName+ Constants.UNDERSCORE+timeStamp+ XMLHTMLlist).getAbsoluteFile();
					}
				}else{
					if (!_runoffline){
						LOGGER.error("Outputfile baseName required ");
						System.exit(0);
					}else
						_outBaseName = new File(FilenameUtils.concat(_inputDir.getAbsolutePath(),_agentName)).getAbsoluteFile();
				} */
				if (line.hasOption( "bs")) {
					_outBaseName = new File(line.getOptionValue("bs")+Constants.UNDERSCORE+_agentName).getAbsoluteFile();
					_outputFile = new File(line.getOptionValue("bs")+Constants.UNDERSCORE+_agentName+XMLlist).getAbsoluteFile();
					if(line.hasOption( "oxslt")){
						_offlineXSLT  = true;
						_outputFileHTML = new File(line.getOptionValue("bs")+Constants.UNDERSCORE+_agentName+XMLHTMLlist).getAbsoluteFile();
					}
				}else{
					if (!_runoffline){
						LOGGER.error("Outputfile baseName required ");
						System.exit(0);
					}else
						_outBaseName = new File(FilenameUtils.concat(_inputDir.getAbsolutePath(),_agentName)).getAbsoluteFile();
				}

			}
			if (_operation.contains(ALIGN_operation) || _operation.contains(TMX_MERGE_operation)){
				if(line.hasOption("iso6393"))
					_iso6393=true;
			}
			if (_operation.contains(TMX_MERGE_operation) )
				getParams4MergingAlignments(line); //getParams4MergingAlignments(line, timeStamp);
			if (_operation.contains(ALIGN_operation))
				getParams4Align(line); //getParams4Align(line, timeStamp);
			//if (_operation.contains(CRAWL_operation) || _operation.contains(EXPORT_operation) || _operation.contains(TMX_MERGE_operation))
			//	getParams4ContentProps(line);
			if(line.hasOption( "cfg")) 
				_config = line.getOptionValue("cfg");

			if (_operation.contains(PAIRDETECT_operation))
				getPairDetectionParams(line);

			if (_operation.contains(MONO_MERGE_operation))
				getMonoMergeParams(line);

			/*if(line.hasOption( "p_r")) {
				_paths_repl= line.getOptionValue("p_r").trim();
				if (_paths_repl.endsWith("/")){
					_paths_repl=_paths_repl.substring(0, _paths_repl.length()-1);
				}
			}*/

		} catch( ParseException exp ) {
			// oops, something went wrong
			System.err.println( "Parsing options failed.  Reason: " + exp.getMessage() );			
			System.exit(64);
		}
	}

	private void getMonoMergeParams(CommandLine line) {
		if (line.hasOption("corpuslevel"))
			_corpuslevel =  line.getOptionValue("corpuslevel");
	}

	private boolean isValidOperationsSequence(String operation) {
		if (StringUtils.isBlank(operation)){
			LOGGER.info("No operation asked");
			return false;
		}
		if (!operation.equals(CRAWL_operation)){
			if (operation.contains(CRAWL_operation) && !operation.contains(EXPORT_operation)){
				LOGGER.info("Exporting is required after crawling");
				return false;
			}
		}
		if (operation.contains(EXPORT_operation+ALIGN_operation) || operation.contains(EXPORT_operation+TMX_MERGE_operation) || 
				operation.contains(DEDUP_operation+ALIGN_operation) || operation.contains(DEDUP_operation+TMX_MERGE_operation)){
			LOGGER.info("Pair detection is required before alignment of merging TMXs");
			return false;
		}
		if (operation.contains(PAIRDETECT_operation+TMX_MERGE_operation)){
			LOGGER.info("Alignemnt is required before merging TMXs");
			return false;
		}
		return true;
	}

	private void getPairDetectionParams(CommandLine line) {
		if(line.hasOption( "ifp")) 
			_keepimagefp  = true;
		if(line.hasOption( "pdm"))
			_methods = line.getOptionValue("pdm");
		if(line.hasOption( "del")) 		
			_del  = true;
		String temp= line.getOptionValue("u_r");
		if (temp!=null){
			String[] aa=temp.split(DOUBLESEMICOLON_SEPAR);
			String[][] urls_repls =new String[aa.length][2];  
			for (int ii=0;ii<aa.length;ii++){
				String[] bb = aa[ii].split(Constants.SEMICOLON);
				if (bb.length<1){
					LOGGER.error("the argument for URL replacements is not correct." +
							" Use ;; to seperate pairs and ; to separate the parts of each pair." );
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

	private void getParams4Dedup(CommandLine line) {
		if(line.hasOption( "dedup_meth")) 
			_dedupmethod = line.getOptionValue("dedup_meth");
		if(line.hasOption( "dedup_mtl")) 
			_dedupMIN_TOK_LEN = Integer.parseInt(line.getOptionValue("dedup_mtl"));
		if(line.hasOption( "dedup_mpl")) 
			_dedupMIN_PAR_LEN = Integer.parseInt(line.getOptionValue("dedup_mpl"));
		if(line.hasOption( "dedup_ithr")) 
			_dedupinter_thr = Double.parseDouble(line.getOptionValue("dedup_ithr"));	
		if(line.hasOption( "dedup_intype"))
			_dedupInputType = line.getOptionValue("dedup_intype");
		if(line.hasOption( "dedup_exf")){ 
			String[] temp= line.getOptionValue("exf").split(Constants.SEMICOLON);
			for (int ii=0;ii<temp.length;ii++){
				_dedupExcludefiles.add(FilenameUtils.concat(_outputDir.getAbsolutePath(), temp[ii]));
			}
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
				String[] temp = str.split(Constants.COLON); 
				res.add(temp);
			}
		} catch (IOException e) {
			LOGGER.error("Problem in reading the file for crossLinksAttrs.");
		}
		return res;
	}

	private List<String> findLangPairs(String language) {
		String[] langs = language.split(Constants.SEMICOLON); 
		List<String> lang_pairs = new ArrayList<String>();
		if (langs.length>1){
			for (int ii=0;ii<langs.length-1;ii++){
				for (int jj=ii+1;jj<langs.length;jj++){
					lang_pairs.add(langs[ii]+Constants.SEMICOLON+langs[jj]);
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
		if(line.hasOption( "del")) 		{	_del  = true;				}
		if(line.hasOption( "len")) 		{	_MinParLen = Integer.parseInt(line.getOptionValue("len"));		} 
		if(line.hasOption( "mtlen")) 	{	_MinDocLen = Integer.parseInt(line.getOptionValue("mtlen"));	} 
	}
	/**
	 * parses the command Line and gets _topic (file with terms, etc) and _descr (topicName)
	 * @param line
	 */
	private void getParams4Topicness(CommandLine line) {
		if(line.hasOption( "tc")) 
			_topicFile = new File(line.getOptionValue("tc"));
		if(line.hasOption( "dom")) {
			if (_topicFile==null){
				LOGGER.info("The targeted domain is defined but a topic definition is not applied. " +
						"So, the domain will be used as provided, i.e. it will not be identified.");
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
		if (line.hasOption("type"))
			_type = line.getOptionValue("type");
		else{
			LOGGER.error("option -type is required");
			help();
		}
		if (line.hasOption("u")) {
			_seedFile = new File(line.getOptionValue("u")).getAbsoluteFile();
			if (_seedFile.exists()==false){
				LOGGER.error("The seed file does not exist.");
				help();
			}
		}
		if(line.hasOption("t")) 
			_threads = Integer.parseInt(line.getOptionValue("t"));	
		if(line.hasOption("cdl"))	{
			_numLoops = Integer.parseInt(line.getOptionValue("cdl"));
			_crawlDuration=0;
		}
		if(line.hasOption("cdm")) 
			_crawlDuration = Integer.parseInt(line.getOptionValue("cdm"));
		if(line.hasOption("f"))	
			_force   = true;
		if(line.hasOption( "k"))
			_keepBoiler  = true;
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
		if(line.hasOption("level")) 
			_level = Integer.parseInt(line.getOptionValue("level"));	
		if(line.hasOption("depth")) 
			_depth = Integer.parseInt(line.getOptionValue("depth"));

		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
		if (line.hasOption( "dest")) 
			ws_dir = FilenameUtils.concat(line.getOptionValue("dest"), _agentName+Constants.UNDERSCORE+timeStamp) ;
		else
			ws_dir=_agentName+Constants.UNDERSCORE+timeStamp;

		if(!line.hasOption( "o")) 
			_outputDir = new File(FilenameUtils.concat(ws_dir, UUID.randomUUID().toString())); 				 	

		if (_type.equals(type_m)){
			if(line.hasOption( "filter")) 
				_filter = line.getOptionValue("filter");
			else
				_filter = ".*.*";
		}
		if (_type.equals(type_p) | _type.equals(type_q)){
			if (!_language.contains(Constants.SEMICOLON)){
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
	 * @return
	 */
	private ArrayList<String> readSeedList() {
		ArrayList<String> seed_list = new ArrayList<String>();
		BufferedReader rdr;
		try {
			rdr = new BufferedReader(new InputStreamReader(new FileInputStream(_seedFile),"utf8"));
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
	//private void getParams4Align(CommandLine line, String timeStamp) {
	private void getParams4Align(CommandLine line) {	
		String tt =  line.getOptionValue("align");
		if (tt!=null)
			_aligner = line.getOptionValue("align").toLowerCase();
		else
			_aligner = null;
		if ((_aligner==null) || !alignerIds.contains(_aligner)) 
			_aligner = default_aligner;
		if (line.hasOption( "dict")) {
			_dict = line.getOptionValue("dict");
			if (_dict==null) 
				_dict = "default";
		}else
			_dict=null;
		_outputFileTMX = new File(line.getOptionValue("bs")+Constants.UNDERSCORE+_agentName+TMXlist);
		_outputFileTMX = _outputFileTMX.getAbsoluteFile();	
		if (line.hasOption( "oxslt")){
			_outputFileHTMLTMX = new File(line.getOptionValue("bs")+Constants.UNDERSCORE+_agentName+TMXHTMLlist);
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
	//private void getParams4MergingAlignments(CommandLine line, String timeStamp) {
	private void getParams4MergingAlignments(CommandLine line) {	
		_outputFile_mergedTMX = new File(line.getOptionValue("bs")+Constants.UNDERSCORE+_agentName+TMXEXT);
		_outputFile_mergedTMX = _outputFile_mergedTMX.getAbsoluteFile();
		if (line.hasOption( "oxslt")){
			_outputFile_mergedTMXHTML = new File(line.getOptionValue("bs")+Constants.UNDERSCORE+_agentName+TMXEXT+HTMLEXT);
			_outputFile_mergedTMXHTML =_outputFile_mergedTMXHTML.getAbsoluteFile();
		}
		if (line.hasOption("pdm"))
			_selectDocs = line.getOptionValue("pdm");
		if(line.hasOption( "mtuvl"))
			_minTuvLen = Integer.parseInt(line.getOptionValue("mtuvl"));
		if(line.hasOption( "mpa"))
			_minPerce01Align = Double.parseDouble(line.getOptionValue("mpa"));
		if(line.hasOption( "mpma"))
			_minPerceM1Align = Double.parseDouble(line.getOptionValue("mpma"));
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
		if (line.hasOption("samesymb"))
			_samesymb=true;
		if(line.hasOption( "keepiden"))
			_keepiden = true;
		if(line.hasOption( "keepdup"))
			_keepdup = true;
		if(line.hasOption( "keepneardup"))
			_keepneardup = true;
		if (line.hasOption("segtypes")){
			String[] temp= line.getOptionValue("segtypes").split(Constants.SEMICOLON); 
			for (String str:temp){
				_selectSegs.add(str);
			}
		}else
			_selectSegs.add(defaultSegType);

		if (line.hasOption("cc"))
			_cc=true;
		if (line.hasOption("o1"))
			_o1=new File(line.getOptionValue("o1"));
		if (line.hasOption("o2"))
			_o2=new File(line.getOptionValue("o2"));
		if (line.hasOption("dom"))
			_usertopic = line.getOptionValue("dom");
		if(line.hasOption( "size"))
			_maxSize = Integer.parseInt(line.getOptionValue("size"));
		if(line.hasOption( "samplesize"))
			_maxSampleSize = Integer.parseInt(line.getOptionValue("samplesize"));
	}

	/**
	 * checks if analyzers of the targeted languages are available
	 * @param _languages
	 */
	private void checkAnalyzers(String languages) {
		String[] langs=languages.split(Constants.SEMICOLON);
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
		if (ps==null)
			return host;
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
			if (!temp.isPublicSuffix())
				finalhost=finalhost+domainparts.get(kk)+".";
		}
		return finalhost;
	}

	/**
	 * parses the predefined project resource LANG_KEYS_RESOURCE and for each targeted language
	 * returns the array with alternative patterns of links for each targeted language
	 * Targeted languages must be included in LANG_KEYS_RESOURCE 
	 * @param language
	 * @return
	 *//*
	private String[] findKeys4langOLD(String language) {
		ArrayList<String> langKeys=new ArrayList<String>();
		String[] langs = _language.split(Constants.SEMICOLON);
		try {
			URL svURL = ReadResources.class.getClassLoader().getResource(LANG_KEYS_RESOURCE);
			BufferedReader in = new BufferedReader(new InputStreamReader(svURL.openStream()));
			String str, b;
			while ((str = in.readLine()) != null) {
				b=str.subSequence(0, str.indexOf(">")).toString();
				for (String lang:langs){
					if (b.equals(lang))
						langKeys.add(str.subSequence(str.indexOf(">")+1, str.length()).toString());
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
	*/
	
	/**
	 * parses the predefined project resource LANG_KEYS_RESOURCE and for each targeted language
	 * returns the array with alternative patterns of links for each targeted language
	 * Targeted languages must be included in LANG_KEYS_RESOURCE 
	 * @param language
	 * @return
	 */
	private String[] findKeys4lang(String language) {
		String langKeys="";
		String[] langs = _language.split(Constants.SEMICOLON);
		HashMap<String,String> resultmap= new HashMap<String,String>();
		String[] result=new String[langs.length];
		try {
			URL svURL = ReadResources.class.getClassLoader().getResource(LANG_KEYS_RESOURCE);
			BufferedReader in = new BufferedReader(new InputStreamReader(svURL.openStream()));
			String str, b;
			while ((str = in.readLine()) != null) {
				b=str.subSequence(0, str.indexOf(">")).toString();
				langKeys = str.subSequence(str.indexOf(">")+1, str.length()).toString();
				resultmap.put(b, langKeys);
			}
			in.close();
		} catch (IOException e) {
			LOGGER.error("Problem in reading the file for langKeys.");
		}	
		
		for (int ii=0;ii<langs.length;ii++){
			if (!resultmap.containsKey(langs[ii])){
				String lang = ISOLangCodes.get2LetterCode(langs[ii]);
				//String langName = ISOLangCodes.getLangName(lang);
				LOGGER.error("The targeted language \"" + lang +"\" is not supported. Check the file for langKeys and/or langcode-langs.");
				if (lang.equals("nor"))
					LOGGER.error("In case a targeted language is Norwegian, you have to set \"nb\" for Norwegian Bokmål or \"nn\" for Norwegian Nynorsk.");
				System.exit(0);
			}
			result[ii] =  resultmap.get(langs[ii]);
		}		
		return result;
	}
	
	

	/**
	 * parses the predefined project resource LANG_KEYS_RESOURCE and for each targeted language
	 * returns the array with alternative patterns of links for each targeted language  
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

	/*private String getSupportedLanguages() {
		String supportedlangs = "";
		try {
			URL svURL = ReadResources.class.getClassLoader().getResource(LANG_KEYS_RESOURCE);
			BufferedReader in = new BufferedReader(new InputStreamReader(svURL.openStream()));
			String str;
			while ((str = in.readLine()) != null) {
				supportedlangs=supportedlangs+Constants.SEMICOLON+str.subSequence(0, str.indexOf(">")).toString();
			}
			in.close();
		} catch (IOException e) {
			LOGGER.error("Problem in reading the file for langKeys.");
		}
		return supportedlangs.substring(1);
	}
*/
	public  void help(){
		printHelp( APPNAME , options );
		System.exit(0);
	}

	public  void printHelp(String program, Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.setWidth(200);
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
	/**
	 * returns the language pairs as combinations of the targeted languages
	 * @return
	 */
	public List<String> getLangPairs() {
		return _langPairs;
	}
	public LangDetector  getLangDetector() {
		return _langDetector;
	}
	public List<String[]> getTransLinksAttrs() {
		return _linkAttrs;
	}
	public File getTopicFile() {
		return _topicFile;
	}
	public  String getWebDomain() {
		return _webdomain;
	}
	public  String getWebMainDomain() {
		return _webmaindomain;
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
	/**
	 * returns the basename of all output files (lists) 
	 * @return
	 */
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
	public File getSeedFile() {
		return _seedFile;
	}
	public boolean keepBoiler() {
		return _keepBoiler;
	}
	/**
	 * returns methods for pair detection
	 * @return
	 */
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
	/**
	 * returns the minimum accepted length (in terms of tokens) of a paragraph, default is 3
	 * @return
	 */
	public int getMinParLen() {
		return _MinParLen;
	}
	/**
	 * returns the minimum accepted length (in terms of tokens) of the clean content of a document, default is 80
	 * @return
	 */
	public int getMinDocLen() {
		return _MinDocLen;
	}
	public int getMinTuvLen() {
		return _minTuvLen;
	}
	public double getMinPerce01Align() {
		return _minPerce01Align;
	}
	public double getMinPerceM1Align() {
		return _minPerceM1Align;
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
	public String getType() {
		return _type;
	}
	/**
	 * returns the user defined name of the targeted topic
	 * @return
	 */
	public String getUserTopic() {
		return _usertopic;
	}
	public String getFilter() {
		return _filter;
	}
	public String getStoreFilter() {
		return _storefilter;
	}
	/**
	 * Special pairs of patterns to match URLs of candidate translations (webpages).
	 * @return
	 */
	public String[][] getUrlReplaces() {
		return _urls_repls;
	}
	/*public String getPathReplace() {
		return _paths_repl;
	}*/
	public List<String> getSegTypes() {
		return _selectSegs;
	}
	public String getDocTypes() {
		return _selectDocs;
	}
	/**
	 * if true, full paths of images in HTML are extracted
	 * if false, filenames of images in HTML are extracted 
	 * @return
	 */
	public boolean getImpath() {
		return _keepimagefp;
	}
	/**
	 * Apply an xsl transformation to generate html files during exporting.
	 * @return
	 */
	public boolean isOfflineXSLT() {
		return _offlineXSLT;
	}
	public boolean useISO6393() {
		return _iso6393;
	}
	public int getCrawlLevel() {
		return _level;
	}
	/**
	 * sets the NumId of the last crawlDir (i.e. number of runs) from which the acquired data will be exported,
	 * default is 10000 (i.e. a very large numbers of cycles which implies the whole crawled content) 
	 */
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
	/**
	 * Use it very carefully. If true, the documents that have not been involved into document pairs are deleted!  
	 * @return
	 */
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
	public boolean getKeepNearDuplicates() {
		return _keepneardup;
	}
	public boolean getClean() {
		return _clean;
	}
	public boolean getSameSymb() {
		return _samesymb;
	}
	public File getO1() {
		return _o1;
	}
	public File getO2() {
		return _o2;
	}

	public CompositeConfiguration getConfiguration() {
		return _configuration;
	}

	public boolean getRunOffLine(){
		return _runoffline;
	}
	public File getNegwordsFile() {
		return _negwordsFile;
	}
	public boolean getTextExport(){
		return _textexport;
	}
	/**
	 * Method type for deduplication: 1 for Deduplication by using lists and MD5 method. 
	 * 2 for Deduplication based on common paragraphs. 0 for applying both methods (default).
	 * @return
	 */
	public String getDedupMethod() {
		return _dedupmethod;
	}
	/**
	 * Tokens with less than MIN_TOK_LEN (default is 3) letters are excluded from content
	 * @return
	 */
	public int getDedupMinTokLen() {
		return _dedupMIN_TOK_LEN;
	}
	/**
	 * Paragraphs with less than MIN_PAR_LEN (default is 3) tokens are excluded from content
	 * @return
	 */
	public int getDedupMinParLen() {
		return _dedupMIN_PAR_LEN;
	}
	public double getDedupInterThr() {
		return _dedupinter_thr;
	}
	public Set<String> getListExcludeFiles(){
		return _dedupExcludefiles;
	}
	public String getInputType() {
		return _dedupInputType;
	}

	public String getCorpusLevel() {
		return _corpuslevel;
	}
	public int getMaxSize() {
		return _maxSize;
	}
	public int getMaxSampleSize() {
		return _maxSampleSize;
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
	}*/

	/**
	 * @return the _paths_repl
	 *//*
	public String get_paths_repl() {
		return _paths_repl;
	}

	  *//**
	  * @param _paths_repl the _paths_repl to set
	  *//*
	public void set_paths_repl(String _paths_repl) {
		this._paths_repl = _paths_repl;
	}*/

}
