package gr.ilsp.fc.exporter;

import gr.ilsp.fc.langdetect.LangDetectUtils;

import java.io.File;
import java.net.URL;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.io.FilenameUtils;
//import org.apache.hadoop.mapred.JobConf;
import org.apache.log4j.Logger;


public class ExporterOptions {
	private static final Logger LOGGER = Logger.getLogger(ExporterOptions.class);
	private static final String QUEST_SEPAR = ";";
	private static final String UNDERSCORE_STR = "_";
	private static final String xml_type="xml";
	private String APPNAME = "Export";
	private Options options;

	private File _inputDir;
	private File _outputDir;
	private File _topicFile;
	private File _negwordsFile;
	private File _outBaseName;

	private String[] _targetlanguages;

	private int _MinParLen=3;
	private int _depth=10000;
	private int _MinDocLen=80;

	private boolean _textexport=false;
	private boolean _runoffline=false;
	private boolean _offlineXSLT=false;
	//private boolean _httrack=false;
	//private String _paths_repl="";
	private String _usertopic="";

	private URL _genre;
	private String _config;
	private String _agentName="ILSP-FC";
	private CompositeConfiguration _configuration ;  

	public ExporterOptions() {
		createOptions();
	}

	@SuppressWarnings("static-access")
	private  Options createOptions() {
		options = new Options();

		options.addOption( OptionBuilder.withLongOpt( "config" )
				.withDescription( "XML file with configuration for the crawler." )
				.hasArg()
				.create("cfg") );
		options.addOption( OptionBuilder.withLongOpt( "offline" )
				.withDescription( "input does not come from crawl")
				.create("offline") );
		options.addOption( OptionBuilder.withLongOpt( "inputdir" )
				.withDescription( "Directory containing crawled data" )
				.hasArg()
				.isRequired()
				.create("i") );
		options.addOption( OptionBuilder.withLongOpt( "language(s)" )
				.withDescription( "Target language of crawled data" )
				.hasArg()
				.isRequired()
				.create("lang") );
		options.addOption( OptionBuilder.withLongOpt( "topic" )
				.withDescription( "Path to topic file" )	
				.hasArg()
				.create("tc") );
		options.addOption( OptionBuilder.withLongOpt( "targeted_domain" )
				.withDescription( "user targeted domain " )	
				.hasArg()
				.create("dom") );
		//options.addOption( OptionBuilder.withLongOpt( "genre" )
		//		.withDescription( "text file with genre types and keywords for each type." )
		//		.hasArg()
		//		.create("gnr") );
		options.addOption( OptionBuilder.withLongOpt( "MinParLen" )
				.withDescription( "Minimum number of tokens per text block" )	
				.hasArg()
				.create("len") );
		options.addOption( OptionBuilder.withLongOpt( "MinDocLen" )
				.withDescription( "Minimum number of tokens in clean content" )	
				.hasArg()
				.create("mtlen") );
		options.addOption( OptionBuilder.withLongOpt( "negwords" )
				.withDescription( "Path to file containing negative words")
				.hasArg()
				.create("neg") );
		options.addOption( OptionBuilder.withLongOpt( "outputdir" )
				.withDescription( "output directory" )
				.hasArg()
				.create("o") );	
		options.addOption( OptionBuilder.withLongOpt( "agentname" )
				.withDescription( "Agent name to identify the person or the organization responsible for the task" )
				.hasArg()
				.create("a") );
		options.addOption( OptionBuilder.withLongOpt( "basename" )
				.withDescription( "Basename to be used in generating all output files for easier content navigation" )
				.isRequired()
				.hasArg()
				.create("bs") );
		options.addOption( OptionBuilder.withLongOpt( "textexport" )
				.withDescription( "Export raw txt files" )				
				.create("te") );
		options.addOption( OptionBuilder.withLongOpt( "help" )
				.withDescription( "Help" )
				.create("help") );
		//options.addOption( OptionBuilder.withLongOpt( "webpages_by_httrack" )
		//		.withDescription( "Httrack website copier has been used to download html pages" )
		//		.create("httrack") );
		options.addOption( OptionBuilder.withLongOpt( "offline_xslt" )
				.withDescription( "Apply an xsl transformation to generate html files during exporting.")
				.create("oxslt") );
		options.addOption( OptionBuilder.withLongOpt( "paths_replacements" )
				.withDescription( "Put the strings to be replaced, separated by ';'." +
						" This might be useful for crawling via the web service")
						.hasArg()
						.create("p_r") );
		options.addOption( OptionBuilder.withLongOpt( "exportUpToDepth" )
				.withDescription( "Export webpages which have been stored up to this number of cycles" )	
				.hasArg()
				.create("depth") );
		return options;
	}

	public  void parseOptions( String[] args) {
		// create the command line parser
		CommandLineParser clParser = new GnuParser();
		try {
			CommandLine line = clParser.parse( options, args );
			if(line.hasOption( "help")) 
				help();
			if(line.hasOption( "cfg"))
				_config = line.getOptionValue("cfg");
			if(line.hasOption( "i")) 
				_inputDir = new File(line.getOptionValue("i")).getAbsoluteFile();				//_inputDir = new File(_inputDir.getAbsolutePath());
			else
				help();
			if(line.hasOption( "offline")) 
				_runoffline=true;
			//if(line.hasOption( "httrack")) 
			//	_httrack=true;
			if(line.hasOption( "oxslt")) 
				_offlineXSLT  = true;
			if(line.hasOption( "lang")) 
				_targetlanguages = LangDetectUtils.updateLanguages(line.getOptionValue("lang").toLowerCase(),true).split(QUEST_SEPAR);
			if(line.hasOption( "tc")) 
				_topicFile = new File(line.getOptionValue("tc")).getAbsoluteFile();				//_topicFile = new File(_topicFile.getAbsolutePath());
			if(line.hasOption( "dom")) {
				if (_topicFile==null){
					LOGGER.warn("The targeted domain is defined but a topic definition is not applied. " +
							"So, the domain will be used as provided, i.e. it will not be identified.");
					//help();
				}else
					_usertopic = line.getOptionValue("dom");
			}else{
				if (_topicFile!=null){
					LOGGER.error("Even though a topic definition is applied the targeted domain is not defined. Define a domain name ");
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
			if(line.hasOption( "len")) 
				_MinParLen = Integer.parseInt(line.getOptionValue("len"));
			if(line.hasOption( "mtlen"))
				_MinDocLen = Integer.parseInt(line.getOptionValue("mtlen"));
			if(line.hasOption( "depth"))
				_depth = Integer.parseInt(line.getOptionValue("depth"));
			if(line.hasOption( "neg")) 
				_negwordsFile = new File(line.getOptionValue("neg")).getAbsoluteFile();
			//_negwordsFile = new File(_negwordsFile.getAbsolutePath());
			if(line.hasOption( "te"))
				_textexport = true;
			if(line.hasOption( "o")) 
				_outputDir = new File(line.getOptionValue("o")).getAbsoluteFile();
			else
				_outputDir = new File(FilenameUtils.concat(_inputDir.getAbsolutePath(), xml_type));
			//if(line.hasOption( "bs")) 
			//	_outBaseName = new File(line.getOptionValue("bs")).getAbsoluteFile();
			if(line.hasOption( "a"))
				_agentName = line.getOptionValue("a").replace(" ", "_");
			if (line.hasOption( "bs")) 
				_outBaseName = new File(line.getOptionValue("bs")+UNDERSCORE_STR+_agentName).getAbsoluteFile();
			else{
				LOGGER.error("BaseName for outfiles is required");
				help();				
			}
			/*if(line.hasOption( "p_r")) {
				_paths_repl= line.getOptionValue("p_r").trim();
				if (_paths_repl.endsWith("/"))
					_paths_repl=_paths_repl.substring(0, _paths_repl.length()-1);
			}*/
		} catch( ParseException exp ) {
			// oops, something went wrong
			System.err.println( "Parsing options failed.  Reason: " + exp.getMessage() );			
			System.exit(64);
		}
	}

	public  void help(){
		printHelp( APPNAME  , options );
		System.exit(0);
	}
	public  void printHelp(String program, Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( program, options );
	}

	public String[] get_language() {
		return _targetlanguages;
	}
	public File getTopicFile() {
		return _topicFile;
	}
	public File getNegwordsFile() {
		return _negwordsFile;
	}
	public File getOutputDir() {
		return _outputDir;
	}
	public File getInputDir() {
		return _inputDir;
	}
	public int getMinParLen() {
		return _MinParLen;
	}
	public int getMinDocLen() {
		return _MinDocLen;
	}
	public int get_depth() {
		return _depth;
	}
	public boolean get_textexport(){
		return _textexport;
	}
	public boolean getRunOffLine(){
		return _runoffline;
	}
	public boolean applyOfflineXSLT(){
		return _offlineXSLT;
	}
	/*public boolean usedHttrack(){
		return _httrack;
	}*/
	public  File getBaseName() {
		return _outBaseName;
	}
	public  String getUserTopic() {
		return _usertopic;
	}
	/*public String get_paths_repl() {
		return _paths_repl;
	}*/

	public URL getGenre(){
		return _genre;
	}

	public String getConfig(){
		return _config;
	}
	//	public boolean runOffLine(){
	//		return _runoffline;
	//	}

	public CompositeConfiguration getConfiguration(){
		return _configuration;
	}

	public String getAgentName() {
		return _agentName;
	}

}
