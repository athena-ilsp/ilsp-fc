package gr.ilsp.fc.exporter;

import java.io.File;
import java.net.URL;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;


public class ExporterOptions {
	private static final Logger LOGGER = Logger.getLogger(ExporterOptions.class);
	private Options options;
	private File _inputdir;
	private File _outputdir = null;
	private File _topic;
	private File _negwords;
	private File _outputFile;
	private File _outputFileHTML;
	
	private String[] _targetlanguages;
	
	private static final String SEPARATOR = ";";
	private String APPNAME = "Crawl export";
	private int _length;
	private int _minTokensNumber;
	private boolean _textexport=false;
	private boolean _cesdoc=false;
	private boolean _html=false;
	private String _paths_repl=null;
	
	private String _targetDomain;
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
		options.addOption( OptionBuilder.withLongOpt( "inputdir" )
				.withDescription( "Directory containing crawled data" )
				.hasArg()
				.create("i") );
		options.addOption( OptionBuilder.withLongOpt( "language(s)" )
				.withDescription( "Target language of crawled data" )
				.hasArg()
				.create("l") );
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
		options.addOption( OptionBuilder.withLongOpt( "length" )
				.withDescription( "Minimum number of tokens per text block" )	
				.hasArg()
				.create("len") );
		options.addOption( OptionBuilder.withLongOpt( "Minlength" )
				.withDescription( "Minimum number of tokens in clean content" )	
				.hasArg()
				.create("mtlen") );
		options.addOption( OptionBuilder.withLongOpt( "negwords" )
				.withDescription( "Path to file containing negative words")
				.hasArg()
				.create("n") );
		options.addOption( OptionBuilder.withLongOpt( "outputdir" )
				.withDescription( "output directory" )
				.hasArg()
				.create("o") );	
		options.addOption( OptionBuilder.withLongOpt( "html_outlist" )
				.withDescription( "outputlist in html format" )				
				.create("ofh") );
		options.addOption( OptionBuilder.withLongOpt( "text_outlist" )
				.withDescription( "outputlist in text format" )				
				.create("of") );
		options.addOption( OptionBuilder.withLongOpt( "textexport" )
				.withDescription( "Export raw txt files" )				
				.create("te") );
	/*	options.addOption( OptionBuilder.withLongOpt( "export" )
				.withDescription( "export" )
				.create("export") );*/
		options.addOption( OptionBuilder.withLongOpt( "help" )
				.withDescription( "Help" )
				.hasArg()
				.create("h") );
		options.addOption( OptionBuilder.withLongOpt( "style" )
				.withDescription( "add stylesheet" )				
				.create("ces") );

		options.addOption( OptionBuilder.withLongOpt( "paths_replacements" )
				.withDescription( "Put the strings to be replaced, separated by ';'." +
						" This might be useful for crawling via the web service")
						.hasArg()
						.create("p_r") );
		//options.addOption( OptionBuilder.withLongOpt( "mimes" )
		//		.withDescription( "MimeTypes that the exporter can handle" )				
		//		.create("mime") );
		return options;
	}

	public  void parseOptions( String[] args) {
		// create the command line parser
		CommandLineParser clParser = new GnuParser();
		try {
			CommandLine line = clParser.parse( options, args );

			if(line.hasOption( "h")) {
				help();
			}
			if(line.hasOption( "cfg")) {
				_config = line.getOptionValue("cfg");
			}	
			if(line.hasOption( "i")) {
				_inputdir = new File(line.getOptionValue("i"));
				_inputdir = new File(_inputdir.getAbsolutePath());
			}
			else help();

			if(line.hasOption( "l")) {
				_targetlanguages = line.getOptionValue("l").split(SEPARATOR);
			}
			if(line.hasOption( "t")) {
				_topic = new File(line.getOptionValue("t"));
				_topic = new File(_topic.getAbsolutePath());
			}
			if(line.hasOption( "dom")) {
				if (_topic==null){
					LOGGER.error("The targeted domain is defined but " +
							"a topic definition is not applied. " +
							"Regarding Topic definition and targeted domain," +
							" you should either define both or none of them.");
					help();
				}else
					_targetDomain = line.getOptionValue("dom");
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
			if(line.hasOption( "len")) {
				_length = Integer.parseInt(line.getOptionValue("len"));
			} 
			if(line.hasOption( "mtlen")) {
				_minTokensNumber = Integer.parseInt(line.getOptionValue("mtlen"));
			} 
			if(line.hasOption( "n")) {
				_negwords = new File(line.getOptionValue("n"));
				_negwords = new File(_negwords.getAbsolutePath());
			} 
			if(line.hasOption( "te")) {
				_textexport = true;
			} 
			if(line.hasOption( "o")) {
				_outputdir = new File(line.getOptionValue("o"));
				_outputdir = new File(_outputdir.getAbsolutePath());
			} else help();						
			if(line.hasOption( "of")) {
				_outputFile = new File(line.getOptionValue("of"));
				_outputFile = new File(_outputFile.getAbsolutePath());
			} else help();				
			if(line.hasOption( "ofh")) {
				_outputFileHTML = new File(line.getOptionValue("ofh"));
				_outputFileHTML = new File(_outputFileHTML.getAbsolutePath());
				_html = true;
			}else{
				_html = false;
			}
			if(line.hasOption( "ces")) {
				_cesdoc = true;
			} 
			if(line.hasOption( "p_r")) {
				_paths_repl= line.getOptionValue("p_r").trim();
				if (_paths_repl.endsWith("/")){
					_paths_repl=_paths_repl.substring(0, _paths_repl.length()-1);
				}
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
	public boolean get_style(){
		return _cesdoc;
	}
	public boolean get_htmloutput(){
		return _html;
	}
	public  File getOutputFile() {
		return _outputFile;
	}
	public  File getOutputFileHTML() {
		return _outputFileHTML;
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
}
