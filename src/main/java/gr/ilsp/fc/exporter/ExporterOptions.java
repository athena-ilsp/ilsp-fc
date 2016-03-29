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
//import org.apache.hadoop.mapred.JobConf;
import org.apache.log4j.Logger;


public class ExporterOptions {
	private static final Logger LOGGER = Logger.getLogger(ExporterOptions.class);
	private static final String SEPARATOR = ";";
	
	private String APPNAME = "Export";
	private Options options;
	
	private File _inputdir;
	private File _outputdir;
	private File _topic;
	private File _negwords;
	private File _outBaseName;
		
	private String[] _targetlanguages;
		
	private int _length=3;
	private int _minTokensNumber=100;
	
	private boolean _textexport=false;
	private boolean _offline=false;
	private boolean _applyOfflineXSLT=false;
	private boolean _httrack=false;
	private String _paths_repl="";
	private String _targetDomain="";
	
	private URL _genre;
	private String _config;
	
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
				.withDescription( "cleaning and normalization will be applied, i.e. HTML will be processed, other info will be extracted from RUN dirs")
				.create("off") );
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
				.create("t") );
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
		options.addOption( OptionBuilder.withLongOpt( "webpages_by_httrack" )
				.withDescription( "Httrack website copier has been used to download html pages" )
				.create("httrack") );
		options.addOption( OptionBuilder.withLongOpt( "offlineXslt" )
				.withDescription( "Apply an xsl transformation to generate list of links pointing to HTML (rendered XML) files.")
				.create("oxslt") );
		options.addOption( OptionBuilder.withLongOpt( "paths_replacements" )
				.withDescription( "Put the strings to be replaced, separated by ';'." +
						" This might be useful for crawling via the web service")
						.hasArg()
						.create("p_r") );
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
			if(line.hasOption( "i")) {
				_inputdir = new File(line.getOptionValue("i"));
				_inputdir = new File(_inputdir.getAbsolutePath());
			}
			else help();
			if(line.hasOption( "off")) 
				_offline=true;
			if(line.hasOption( "httrack")) 
				_httrack=true;
			if(line.hasOption( "lang")) 
				_targetlanguages = LangDetectUtils.updateLanguages(line.getOptionValue("lang").toLowerCase(),true).split(SEPARATOR);
				//	_targetlanguages = line.getOptionValue("lang").split(SEPARATOR);
			if(line.hasOption( "t")) {
				_topic = new File(line.getOptionValue("t"));
				_topic = new File(_topic.getAbsolutePath());
				//_topicterms = TopicTools.analyzeTopic(_topic, _targetlanguages);
			}
			if(line.hasOption( "dom")) {
				if (_topic==null){
					LOGGER.warn("The targeted domain is defined but a topic definition is not applied. " +
							"So, the domain will be used as provided, i.e. it will not be identified.");
					help();
				}else
					_targetDomain = line.getOptionValue("dom");
			}else{
				if (_topic!=null){
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
				_length = Integer.parseInt(line.getOptionValue("len"));
			if(line.hasOption( "mtlen"))
				_minTokensNumber = Integer.parseInt(line.getOptionValue("mtlen"));
			if(line.hasOption( "neg")) {
				_negwords = new File(line.getOptionValue("n"));
				_negwords = new File(_negwords.getAbsolutePath());
			} 
			if(line.hasOption( "te"))
				_textexport = true;
			if(line.hasOption( "o")) {
				_outputdir = new File(line.getOptionValue("o"));
				_outputdir = new File(_outputdir.getAbsolutePath());
			} else help();						
			if(line.hasOption( "bs")) {
				_outBaseName = new File(line.getOptionValue("bs"));
			} else help();				
			if(line.hasOption( "p_r")) {
				_paths_repl= line.getOptionValue("p_r").trim();
				if (_paths_repl.endsWith("/"))
					_paths_repl=_paths_repl.substring(0, _paths_repl.length()-1);
			}
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

	public File get_inputdir() {
		return _inputdir;
	}

	public String[] get_language() {
		return _targetlanguages;
	}

	public File get_topic() {
		return _topic;
	}

	public File get_negwords() {
		return _negwords;
	}

	public File get_outputdir() {
		return _outputdir;
	}

	public int get_length() {
		return _length;
	}
	public int get_minTokenslength() {
		return _minTokensNumber;
	}
	public boolean get_textexport(){
		return _textexport;
	}
	public boolean getRunOffLine(){
		return _offline;
	}
	public boolean applyOfflineXSLT(){
		return _applyOfflineXSLT;
	}
	public boolean usedHttrack(){
		return _httrack;
	}
	public  File getBaseName() {
		return _outBaseName;
	}
	public  String getTargetDomain() {
		return _targetDomain;
	}
	public URL getGenre(){
		return _genre;
	}
	public String getConfig(){
		return _config;
	}
	public boolean runOffLine(){
		return _offline;
	}
}
