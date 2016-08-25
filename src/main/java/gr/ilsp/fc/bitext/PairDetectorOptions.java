package gr.ilsp.fc.bitext;



import gr.ilsp.fc.langdetect.LangDetectUtils;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
//import org.apache.hadoop.conf.Configuration;
//import org.apache.hadoop.fs.FileSystem;
import org.apache.log4j.Logger;


public class PairDetectorOptions {
	public static int NO_CRAWL_DURATION = 0;
	private  final String APPNAME = "Pair Detector";
	private  Options options;
	private String[][] _urls_repls=null;
	private String _paths_repl=null;
	private  boolean _debug = false;
	private  String _loggingAppender = null;
	private  File _outputDir;
	private  File _inDir;
	private  File _outBaseName;
	private  File _groundTruth;
	private String _methods="aupdih";
	private String _language;
	private boolean _keepimagefp=false;
	private boolean offlineXSLT = false;
	private static final Logger LOGGER = Logger.getLogger(PairDetectorOptions.class);
	private static final String QUEST_SEPAR = ";";
	private static String DOUBLEQUEST_SEPAR = ";;";
	private boolean _delFiles = false;
	
	public PairDetectorOptions() {
		createOptions();
	}
	@SuppressWarnings("static-access")
	private  Options createOptions() {
		options = new Options();

		options.addOption( OptionBuilder.withLongOpt( "debug" )
				.withDescription( "debug logging" )				
				.create("dbg") );
		options.addOption( OptionBuilder.withLongOpt( "loggingAppender" )
				.withDescription( "set logging appender (console, DRFA)")
				.hasArg()
				.create("l") );
		options.addOption( OptionBuilder.withLongOpt( "inputdir" )
				.withDescription( "input directory" )
				.hasArg()
				.create("i") );
		options.addOption( OptionBuilder.withLongOpt( "outputdir" )
				.withDescription( "output directory" )
				.hasArg()
				.create("o") );
		options.addOption( OptionBuilder.withLongOpt( "baseName" )
				.withDescription( "Basename to be used in generating all output files for easier content navigation" )
				.hasArg()
				.create("bs") );
		options.addOption( OptionBuilder.withLongOpt( "imageFullPath" )
				.withDescription( "Keep image fullpath for pair detection" )				
				.create("ifp") );
		options.addOption( OptionBuilder.withLongOpt( "help" )
				.withDescription( "Help" )
				.create("h") );
		options.addOption( OptionBuilder.withLongOpt( "language(s)" )
				.withDescription( "Target language. If more than one, separate with ';', i.e. en;el" )
				.hasArg()
				.create("lang") );
		options.addOption( OptionBuilder.withLongOpt( "offlineXslt" )
				.withDescription( "Apply an xsl transformation to generate html files during exporting.")
				.create("oxslt") );
		options.addOption( OptionBuilder.withLongOpt( "url_replacements" )
				.withDescription( "Put the strings to be replaced, separated by ';'.")
				.hasArg()
				.create("u_r") );
		options.addOption( OptionBuilder.withLongOpt( "paths_replacements" )
				.withDescription( "Put the strings to be replaced, separated by ';'." +
						" This might be useful for crawling via the web service")
						.hasArg()
						.create("p_r") );
		options.addOption( OptionBuilder.withLongOpt( "PairDetectMeth" )
				.withDescription( "Put a string which contains \"a\" for checking links,"
						+ "\"u\" for checking urls for patterns, \"p\" for combining common images and digits, \"i\" for using common images"
						+ "\"d\" for examining digit sequences, \"s\" for examining structures")
						.hasArg()
						.create("pdm") );
		options.addOption( OptionBuilder.withLongOpt( "DelRedundantFiles" )
				.withDescription( "deletes files that have not been pairs" )				
				.create("del") );
		options.addOption( OptionBuilder.withLongOpt( "GT_TextFile" )
				.withDescription( "in case of evaluation we need this file with 2 coloumns, i.e. <file in lang1> <file in lang2>" )	
				.hasArg()
				.create("gt") );
		options.addOption( OptionBuilder.withLongOpt( "help" )
				.withDescription( "Help" )
				.create("h") );
		return options;
	}

	public  void parseOptions ( String[] args) {
		// create the command line parser
		CommandLineParser clParser = new GnuParser();
		try {
			CommandLine line = clParser.parse( options, args );

			if(line.hasOption( "h"))
				help();
			if(line.hasOption( "dbg"))
				_debug = true;
			if(line.hasOption( "l"))
				_loggingAppender = line.getOptionValue("l");
			if(line.hasOption( "pdm")) 
				_methods = line.getOptionValue("pdm");
			if(line.hasOption( "i")) {
				_inDir = new File(line.getOptionValue("i"));
				_inDir = new File(_inDir.getAbsolutePath());
			}
			if(line.hasOption( "o")) {
				_outputDir = new File(line.getOptionValue("o"));
				_outputDir = new File(_outputDir.getAbsolutePath());
			}
			if(line.hasOption( "bs")) {
				_outBaseName = new File(line.getOptionValue("bs"));
				if(line.hasOption( "oxslt"))
					offlineXSLT  = true;
			}
			if(line.hasOption( "lang")) {
				_language = LangDetectUtils.updateLanguages(line.getOptionValue("lang").toLowerCase(), true);
				if (_language.split(QUEST_SEPAR).length!=2){
					LOGGER.error("You should provide 2 languages.");
					help();
				}
			}else{
				LOGGER.error("No languages have been defined.");
				System.exit(0);
			}
			if(line.hasOption( "p_r")) {
				_paths_repl= line.getOptionValue("p_r").trim();
				if (_paths_repl.endsWith("/"))
					_paths_repl=_paths_repl.substring(0, _paths_repl.length()-1);
			}
			if(line.hasOption( "ifp")) 
				_keepimagefp  = true;
			if(line.hasOption( "del")) 
				_delFiles  = true;
			if(line.hasOption( "gt"))
				_groundTruth  = new File(line.getOptionValue("gt"));
			String temp= line.getOptionValue("u_r");
			if (temp!=null){
				String[] aa=temp.split(DOUBLEQUEST_SEPAR);
				String[][] urls_repls =new String[aa.length][2];  
				for (int ii=0;ii<aa.length;ii++){
					String[] bb = aa[ii].split(QUEST_SEPAR);
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

		} catch( ParseException exp ) {
			// oops, something went wrong
			System.err.println( "Parsing options failed.  Reason: " + exp.getMessage() );			
			System.exit(64);
		}
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
	public  boolean isDebug() {
		return _debug;
	}
	public  String getLoggingAppender() {
		return _loggingAppender;
	}
	public  File getInDir() {
		return _inDir;
	}
	public  File getOutDir() {
		return _outputDir;
	}
	public  File getBaseName() {
		return _outBaseName;
	}
	public  File getGroundTruth() {
		return _groundTruth;
	}
	public  String getMethods() {
		return _methods;
	}
	public  boolean getDelFiles() {
		return _delFiles;
	}
	public String[][] getUrlReplaces() {
		return _urls_repls;
	}
	public String getPathReplace() {
		return _paths_repl;
	}
	public boolean getImpath() {
		return _keepimagefp;
	}
	public boolean isOfflineXSLT() {
		return offlineXSLT;
	}
}
