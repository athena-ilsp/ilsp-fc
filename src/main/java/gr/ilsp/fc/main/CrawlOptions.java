package gr.ilsp.fc.main;

import gr.ilsp.fc.langdetect.LangDetectUtils;
import gr.ilsp.fc.utils.AnalyzerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;

import com.google.common.collect.ImmutableList;
import com.google.common.net.InternetDomainName;

public class CrawlOptions {
	public static int NO_CRAWL_DURATION = 0;
	private  final String APPNAME = "Crawl crawlandexport";
	private Options options;

	private String _domain=null;
	private String _maindomain=null;
	private String _descr=null;
	private String _filter=null;
	private String _storefilter=null;
	private String _paths_repl=null;
	private String _loggingAppender = null;
	private String _type="m";
	private String _operation="";
	private String _agentName;
	private String _language;
	private String[] _langKeys;
	private String _urls;
	private String _aligner=null;
	private String _dict=null;
	private String _dictpath=null;
	private String _config;
	private String ws_dir;
	private String _methods = "auidsp";

	//private String default_genrefile="genres_keys.txt";
	private URL _genre;

	private String[][] _urls_repls=null;

	private File _outputDir;
	private File _inputDir;
	private File _outputFile;
	private File _outputFileHTML;
	private File _outputFileTMX;
	private File _outputFileHTMLTMX;
	private File _outputFile_mergedTMX;
	private File _outputFile_mergedTMXHTML;
	private File _topic=null;

	private  int _threads = 10;
	private  int _numLoops = 1;
	private  int _crawlDuration = 10;	
	private  int _minTokensNumber = 100;
	private  int _length = 3;

	private boolean _debug = false;
	private boolean _del=false;
	private boolean _keepBoiler = true;
	private boolean _keepimagefp=false;
	//private boolean _xslt = false;
	private boolean _force = false;
	private boolean _offlineXSLT = false;
	private boolean _cc = false;
	private boolean _metadata = false;

	private static final String XMLlist = ".XMLlist.txt";
	private static final String XMLHTMLlist = ".XMLlist.html";
	private static final String TMXlist = ".TMXlist.txt";
	private static final String TMXHTMLlist = ".TMXlist.html";
	private static final String TMXEXT = ".tmx";
	private static final String HTMLEXT = ".html";
	private static final String UNDERSCORE_STR = "_";
	private static final Logger LOGGER = Logger.getLogger(CrawlOptions.class);
	private static final String type_p = "p";
	private static final String type_q = "q";
	private static final String type_m = "m";
	//private String ws_dir="/var/lib/tomcat6/webapps/soaplab2-results/";
	private static final String SEMI_SEPAR = ";";
	private static final String DOUBLEQUEST_SEPAR = ";;";

	private static String _selectDocs = "auidhml";
	private static List<String> _selectSegs = new ArrayList<String>();
	protected static Matcher skipLineM = Pattern.compile("^(\\s*)||(#.*)$").matcher("");
	//private String default_aligner; //hunalign v1.1
	private static String default_aligner="default"; //hunalign v1.1
	private static final String CRAWL_operation = "crawl";
	private static final String EXPORT_operation = "export";
	private static final String DEDUP_operation = "dedup";
	private static final String PAIRDETECT_operation = "pairdetect";
	private static final String ALIGN_operation = "align";
	private static final String TMX_MERGE_operation = "tmxmerge";
	private static final String LANG_KEYS_RESOURCE = "langKeys.txt" ;

	public CrawlOptions() {
		createOptions();
	}
	@SuppressWarnings("static-access")
	private  Options createOptions() {
		options = new Options();

		options.addOption( OptionBuilder.withLongOpt( "help" )
				.withDescription( "Help" )
				.create("h") );
		options.addOption( OptionBuilder.withLongOpt( "debug" )
				.withDescription( "debug logging" )				
				.create("dbg") );

		options.addOption( OptionBuilder.withLongOpt( "loggingAppender" )
				.withDescription( "set logging appender (console, DRFA)")
				.hasArg()
				.create("l") );

		options.addOption( OptionBuilder.withLongOpt( "config" )
				.withDescription( "XML file with configuration for the tool." )
				.hasArg()
				.create("cfg") );

		options.addOption( OptionBuilder.withLongOpt("crawl")
				.withDescription( "if exists, operation includes crawl" )				
				.create("crawl") );
		options.addOption( OptionBuilder.withLongOpt("export")
				.withDescription( "if exists, operation includes export to cesDoc files of downloaded documents" )				
				.create("export") );
		options.addOption( OptionBuilder.withLongOpt( "apply_near_deduplication" )
				.withDescription( "if exists, operation includes deduplication, i.e.compares clean text content and discards near duplicates" )				
				.create("dedup") );
		options.addOption( OptionBuilder.withLongOpt( "pair_detection" )
				.withDescription( "if exists, detection of document pairs will be applied" )				
				.create("pairdetect") );
		options.addOption( OptionBuilder.withLongOpt( "align_sentences" )
				.withDescription( "if exists, operation includes extraction of sentences from the detected document pairs and " +
						"alignment of the extracted sentences by using an aligner (default is maligna)" )
						.hasOptionalArg()
						.create("align") );
		options.addOption( OptionBuilder.withLongOpt("tmxmerge")
				.withDescription( "if exists, operation includes tmx merging" )				
				.create("tmxmerge") );

		options.addOption( OptionBuilder.withLongOpt("SelectDocsForTMXmerge")
				.withDescription( "selects document pairs (to get their segment pairs) that have been identified by specific methods, ie. auidh" )	
				.hasArg()
				.create("doctypes") );
		options.addOption( OptionBuilder.withLongOpt("SelectSegsForTMXmerge")
				.withDescription( "selects segment pairs from document pairs that have been identified by specific methods, see doctypes parameters" )	
				.hasArg()
				.create("segtypes") );

		/*	options.addOption( OptionBuilder.withLongOpt( "dictionary for aligning sentences" )
				.withDescription( "This dictionary will be used for the sentence alignment" +
						"If has no argument the default dictionary of the aligner will be used if exists" )
						.hasOptionalArg()
						.create("dict") );*/
		options.addOption( OptionBuilder.withLongOpt( "topic" )
				.withDescription( "fullpath of the text file containing the topic definition" )
				.hasArg()
				.create("tc") );
		options.addOption( OptionBuilder.withLongOpt( "TargetedDomainTitle" )
				.withDescription( "A descriptive title for the targeted domain" )
				.hasArg()
				.create("dom") );
		options.addOption( OptionBuilder.withLongOpt("stay_in_webdomain")
				.withDescription( "Force monolingual crawler to stay in a specific webdomain" )				
				.create("d") );
		options.addOption( OptionBuilder.withLongOpt( "urls" )
				.withDescription( "file with list of urls to crawl" )
				.hasArg()
				.create("u") );
		options.addOption( OptionBuilder.withLongOpt( "inputdir" )
				.withDescription( "input directory for deduplication, or pairdetection, or alignment" )
				.hasArg()
				.create("i") );
		options.addOption( OptionBuilder.withLongOpt( "outputdir" )
				.withDescription( "output directory" )
				.hasArg()
				.create("o") );
		options.addOption( OptionBuilder.withLongOpt( "outputXML" )
				.withDescription( "fullpath of text file with list of paths of XML files" )
				.hasArg()
				.create("of") );
		options.addOption( OptionBuilder.withLongOpt( "agentname" )
				.withDescription( "user agent name" )
				.hasArg()
				.create("a") );

		options.addOption( OptionBuilder.withLongOpt( "threads" )
				.withDescription( "maximum number of fetcher threads to use" )
				.hasArg()
				.create("t") );		
		options.addOption( OptionBuilder.withLongOpt( "numloops" )
				.withDescription( "number of fetch/update loops" )
				.hasArg()
				.create("n") );		
		options.addOption( OptionBuilder.withLongOpt( "crawlduration" )
				.withDescription( "crawl duration in minutes" )
				.hasArg()
				.create("c") );
		//options.addOption( OptionBuilder.withLongOpt( "genre" )
		//		.withDescription( "text file with genre types and keywords for each type." )
		//		.hasArg()
		//		.create("gnr") );
		options.addOption( OptionBuilder.withLongOpt( "keepboiler" )
				.withDescription( "if exists, boilerplate content in parsed text will be kept and annotated" )				
				.create("k") );
		options.addOption( OptionBuilder.withLongOpt( "delete_redundant_files" )
				.withDescription( "if exists, the files tha have not been involved into pairs, will be deleted" )				
				.create("del") );

		options.addOption( OptionBuilder.withLongOpt( "image_fullpath" )
				.withDescription( "if exists, fullpath of images will be used in pair detection" )				
				.create("ifp") );
		options.addOption( OptionBuilder.withLongOpt( "force" )
				.withDescription( "if exists, it forces to start new crawl. " +
						"Caution: This will remove any previous crawl data (if they exist)." )				
						.create("f") );		
		options.addOption( OptionBuilder.withLongOpt( "length" )
				.withDescription( "Minimum number of tokens per text block. Shorter text block will be annoteted as \"ooi-length\"" )	
				.hasArg()
				.create("len") );
		options.addOption( OptionBuilder.withLongOpt( "minlength" )
				.withDescription( "Minimum number of tokens in cleaned document. Shorter documents will not be stored" )	
				.hasArg()
				.create("mtlen") );
		options.addOption( OptionBuilder.withLongOpt( "type" )
				.withDescription( "Crawling for monolingual (m), parallel (p), comparable (q)" )	
				.hasArg()
				.create("type") );
		options.addOption( OptionBuilder.withLongOpt( "Fetching_Filter" )
				.withDescription( "Use this regex to force crawler staying in sub webdomains. Webpages with urls that do not match this regex will not be fetched." )	
				.hasArg()
				.create("filter") );
		options.addOption( OptionBuilder.withLongOpt( "Storing_Filter" )
				.withDescription( "Use this regex to force crawler storing only webpages with urls that match this regex." )	
				.hasArg()
				.create("storefilter") );
		options.addOption( OptionBuilder.withLongOpt( "language(s)" )
				.withDescription( "Iso code(s) of target language(s). If more than one, separate them with ';', " +
						"i.e. en;el" )
						.hasArg()
						.create("lang") );
		options.addOption( OptionBuilder.withLongOpt( "offlineXslt" )
				.withDescription( "Apply an xsl transformation to generate html files during exporting.")
				.create("oxslt") );
		options.addOption( OptionBuilder.withLongOpt( "destination" )
				.withDescription( "fullpath of the directory in which the acquired/generated resources will be stored.")
				.hasArg()
				.create("dest") );
		options.addOption( OptionBuilder.withLongOpt( "url_replacements" )
				.withDescription( "Put the strings to be replaced, separated by ';'.")
				.hasArg()
				.create("u_r") );
		options.addOption( OptionBuilder.withLongOpt( "methods_to_be_used_for_pair_detection" )
				.withDescription( "Put a string which contains \"a\" for checking links,"
						+ "\"u\" for checking urls for patterns, \"p\" for using common images and digits" 
						+ "\"i\" for using common images"
						+ "\"d\" for examining digit sequences, \"s\" for examining structures")
						.hasArg()
						.create("meth") );
		options.addOption( OptionBuilder.withLongOpt( "paths_replacements" )
				.withDescription( "Put the strings to be replaced, separated by ';'." +
						" This might be useful for crawling via the web service")
						.hasArg()
						.create("p_r") );
		options.addOption( OptionBuilder.withLongOpt( "licence_detected_for_document_pairs" )
				.withDescription( "If exists, only document pairs for which"
						+ " a license has been detected will be selected in generating the merged TMX.")
						.create("cc") );
		options.addOption( OptionBuilder.withLongOpt( "export_metadata_of_the_collection" )
				.withDescription( "If exists, metadata of the collection (i.e. the merged TMX file), will be exported")
				.create("metadata") );

		return options;
	}

	public  void parseOptions ( String[] args) {
		if (helpAsked(args)){
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
				_language = LangDetectUtils.updateLanguages(line.getOptionValue("lang").toLowerCase());
				_langKeys = findKeys4lang(_language);
				checkAnalyzers(_language);
			}else{
				LOGGER.error("No languages have been defined.");
				System.exit(0);
			}

			if(line.hasOption( "i"))			{	_inputDir = new File(line.getOptionValue("i"));			}
			if(line.hasOption( "o"))			{	_outputDir = new File(line.getOptionValue("o"));		}

			if (_operation.contains(CRAWL_operation)){
				getParams4Crawl(line);
			}
			if (_operation.contains(TMX_MERGE_operation) /*|| _operation.contains(CREATEPCORPUS_operation)*/){
				getParams4MergingAlignments(line);
			}
			if (_operation.contains(ALIGN_operation)){
				getParams4Align(line);
			}
			if (_operation.contains(CRAWL_operation) || _operation.contains(EXPORT_operation)){
				getParams4Topicness(line);
				getParams4ContentProps(line);
			}
			if (_operation.contains(CRAWL_operation) || _operation.contains(EXPORT_operation) ||  _operation.contains(ALIGN_operation) ||  _operation.contains(TMX_MERGE_operation)){
				if(line.hasOption( "cfg")) {
					_config = line.getOptionValue("cfg");
				}	
			}
			if (_operation.contains(PAIRDETECT_operation)){
				if(line.hasOption( "ifp")) 
					_keepimagefp  = true;
				if(line.hasOption( "meth"))
					_methods = line.getOptionValue("meth");
				if(line.hasOption( "del")) 		
					_del  = true;
			}
			if (line.hasOption( "of")) {
				_outputFile = new File(line.getOptionValue("of")+XMLlist);
				if (line.hasOption( "oxslt"))
					_outputFileHTML = new File(line.getOptionValue("of")+XMLHTMLlist);
			}else{
				if (_operation.contains(EXPORT_operation)){
					LOGGER.error("Outputfile required since exporting is asked");
					System.exit(0);
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
			_agentName = line.getOptionValue("a");
		}else{
			help();
		}
		if (line.hasOption("type"))
			_type = line.getOptionValue("type");
		else{
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
		if (line.hasOption("filter"))		{	_filter = line.getOptionValue("filter");	}
		if (line.hasOption("storefilter"))	{	_storefilter = line.getOptionValue("storefilter");	}

		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
		if (line.hasOption( "dest")) {
			ws_dir = FilenameUtils.concat(line.getOptionValue("dest"), _agentName+UNDERSCORE_STR+timeStamp) ;
		}else{
			ws_dir=_agentName+UNDERSCORE_STR+timeStamp;
		}
		if(!line.hasOption( "o")) {
			_outputDir = new File(FilenameUtils.concat(ws_dir, UUID.randomUUID().toString())); 				 	
		}

		if (_type.equals(type_m)){
			if(line.hasOption( "d")) {
				URL url;
				try {
					//FIXME it is supposed that there is only one line in _urls
					String temp=FileUtils.readFileToString(new File(_urls));
					url = new URL(temp);
					String host = url.getHost();
					if (host.substring(0, 3).equals("www"))			{		host=host.substring(4);		}
					String mainhost=processhost(host);
					if (mainhost.substring(0, 3).equals("www"))		{		mainhost=host.substring(4);	}
					_domain=host;
					_maindomain=mainhost;
					LOGGER.info(_domain);
					LOGGER.info(_maindomain);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		if (_type.equals(type_p) | _type.equals(type_q)){
			if (!_language.contains(SEMI_SEPAR)){
				LOGGER.error("You crawl for parallel or comparable but only 1 language has been defined.");
				help();
			}
		}
		if (_type.equals(type_p)){
			String temp= line.getOptionValue("u_r");
			if (temp!=null){
				String[] aa=temp.split(DOUBLEQUEST_SEPAR);
				String[][] urls_repls =new String[aa.length][2];  
				for (int ii=0;ii<aa.length;ii++){
					String[] bb = aa[ii].split(SEMI_SEPAR);
					if (bb.length<=1){
						LOGGER.error("the argument for URL replacements is not correct." +
								" Use ;; to seperate pairs and ; to separate the parts of each pair." +
								"Check that none of the parts is empty.");
						help();
					}else{
						urls_repls[ii][0] = bb[0]; urls_repls[ii][1] = bb[1];
					}
				}
				_urls_repls=urls_repls;
			}
			URL url;
			if (!line.hasOption( "filter")){
				try {
					ArrayList<String> seed_list =new ArrayList<String>();
					BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream(_urls),"utf8"));
					String cur_line="";
					while ((cur_line=rdr.readLine())!=null){
						if (skipLineM.reset(cur_line).matches()) 
							continue;
						seed_list.add(cur_line);
					}
					rdr.close();
					if (!seed_list.isEmpty()){
						String[] seeds=new String[seed_list.size()];
						for (int ii=0;ii<seeds.length;ii++)
							seeds[ii]=seed_list.get(ii);
						if (seeds.length==1){
							try {
								url = new URL(seeds[0]);
								String host = url.getHost();

								if (host.startsWith("www5") | host.startsWith("www2"))
									host=host.substring(5);
								if (host.startsWith("www"))
									host=host.substring(4);
								String mainhost=processhost(host);
								if (mainhost.startsWith("www5") | mainhost.startsWith("www2"))
									mainhost=host.substring(5);
								if (mainhost.startsWith("www"))
									mainhost=host.substring(4);

								_domain=host;
								_maindomain=mainhost;
								LOGGER.debug("Examining web domain : " + _domain);
								LOGGER.debug("Examining second domain : " + _maindomain);
							} catch (MalformedURLException e) {
								LOGGER.error("Seed URL is not valid: "+seeds[0]);
								help();
							}
						}else{
							String firsthost="";
							for (int ii=0;ii<seeds.length;ii++){
								try {
									if (ii==0)
										firsthost = new URL(seeds[ii]).getHost();
									url = new URL(seeds[ii]);
									String host = url.getHost();
									if (!host.equals(firsthost)){
										if (!line.hasOption( "filter")){
											LOGGER.error("Since the provided seed list for bilingual crawling includes more than on webdomains, " +
													" USE the filter argument to confine FC within these webdomains.");
											System.exit(0);				
										}
										else{
											_domain=null;
											_maindomain=null;
										}
									}else{
										if (host.startsWith("www5") |host.startsWith("www2"))
											host=host.substring(5);
										else{
											if (host.startsWith("www"))//(host.substring(0, 3).equals("www"))
												host=host.substring(4);
										}
										String mainhost="";
										char c = host.charAt(0);
										if (Character.isDigit(c)){
											mainhost=host;
										}else{
											mainhost=processhost(host);
										}
										if (mainhost.startsWith("www5") | mainhost.startsWith("www2"))
											//if (mainhost.substring(0, 4).equals("www5") | mainhost.substring(0, 4).equals("www2"))
											mainhost=host.substring(5);
										else{
											if (mainhost.startsWith("www"))//(mainhost.substring(0, 3).equals("www"))
												mainhost=host.substring(4);
										}
										_domain=host;
										_maindomain=mainhost;
									}
								}catch (MalformedURLException e) {
									LOGGER.error("Seed URL is not valid:"+seeds[ii]);
								}		
							} 
						}
					}else{
						LOGGER.error("There is no valid seed URL.");
						help();
					}
				} catch (IOException e) {
					LOGGER.error("The seed URL file does not exist.");
					help();
				}
			}else{
				_domain=null;
				_maindomain=null;
			}
		}
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
		if (_aligner==null){
			_aligner = default_aligner;
		}
		if (line.hasOption( "dict")) {
			_dict = line.getOptionValue("dict");
			if (_dict==null) {
				_dict = "default";
			}
		}else{
			_dict=null;
		}
		_outputFileTMX = new File(line.getOptionValue("of")+TMXlist);	
		if (line.hasOption( "oxslt"))//{
			_outputFileHTMLTMX = new File(line.getOptionValue("of")+TMXHTMLlist);	
		
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
		_outputFile_mergedTMX =  new File(line.getOptionValue("of")+TMXEXT);
		if (line.hasOption( "oxslt"))	
			_outputFile_mergedTMXHTML = new File(line.getOptionValue("of")+HTMLEXT);
		if (line.hasOption("doctypes")){
			_selectDocs = line.getOptionValue("doctypes");
		}
		if (line.hasOption("segtypes")){
			String[] temp= line.getOptionValue("segtypes").split(SEMI_SEPAR); 
			for (String str:temp){
				_selectSegs.add(str);
			}
		}else{
			_selectSegs.add("1:1");
		}
		if (line.hasOption("cc"))
			_cc=true;
		if (line.hasOption("metadata"))
			_metadata=true;
	}

	/**
	 * checks if analyzers of the targeted languages are available
	 * @param _languages
	 */
	private void checkAnalyzers(String languages) {
		Analyzer analyzer =null;
		String[] langs=languages.split(SEMI_SEPAR);
		for (int ii=0;ii<langs.length;ii++){
			try {
				AnalyzerFactory analyzerFactory = new AnalyzerFactory();
				analyzer = analyzerFactory.getAnalyzer(langs[ii]);
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
	 * @param language
	 * @return
	 */
	private String[] findKeys4lang(String language) {
		ArrayList<String> langKeys=new ArrayList<String>();
		String[] langs = _language.split(SEMI_SEPAR);
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
				LOGGER.error("The targetted language(s) is (are) not supported. Check the file for langKeys.");
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


	public  void help(){
		printHelp( APPNAME , options );
		System.exit(0);
	}

	public  void printHelp(String program, Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( program, options );
	}
	public String getLanguage() { 
		return _language;
	}
	public String[] getLangKeys() {
		return _langKeys;
	}
	//	public String getDest3() {
	//		return ws_dir;
	//	}
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
	//public boolean useXSLT() {
	//	return _xslt;
	//}
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
	public int getminTokenslength() {
		return _minTokensNumber;
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
	public boolean getMetadata() {
		return _metadata;
	}
}
