package gr.ilsp.fc.corpusfilter;

import gr.ilsp.fc.langdetect.LangDetectUtils;
import gr.ilsp.fc.langdetect.LangDetector;
import gr.ilsp.nlp.commons.Constants;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

public class MainRunFilterOptions {
	private Options options;
	private String APPNAME = "CorpusFilter";
	
	private static final Logger LOGGER = Logger.getLogger(MainRunFilterOptions.class);
	private static File _infile ;
	private static int _minTuvLen = 3;
	private static int _maxTuvLen = 80;
	private static double _minlr = 0.25;
	private static double _maxlr = 2;
	private static String[] _langs;
	private static LangDetector _langDetector;
	private String defaultlangDetectorId="langdetect" ;
	
	public MainRunFilterOptions() {
		createOptions();
	}

	@SuppressWarnings("static-access")
	private Options createOptions() {
		options = new Options();
		options.addOption( OptionBuilder.withLongOpt( "inputFile" )
				.withDescription( "The corpus txt file should contain a sentence pair per textline and optionally a score (the higher the better), tab separated" )
				.isRequired()
				.hasArg()
				.create("i") );
		options.addOption( OptionBuilder.withLongOpt( "language(s)" )
				.withDescription( "Target languages separated by ';' i.e. eng;ell" )
				.hasArg()
				.create("lang") );
		options.addOption( OptionBuilder.withLongOpt( "SegmentTypes_in_TMXs_to_be_processed" )
				.withDescription( "types of segment pairs to be procesed. If omitted, segments of all types will be processed. "
						+ "Otherwise put segment types seperated by \";\" i.e. \"1:1;1:2;2:1\"")
				.hasArg()
				.create("segtypes") );
		options.addOption( OptionBuilder.withLongOpt( "Min_TUV_Length" )
				.withDescription( "minimum length of an acceptable TUV")
				.hasArg()
				.create("mintuvl") );
		options.addOption( OptionBuilder.withLongOpt( "MinTuLenRatio" )
				.withDescription( "minimum ratio of length (in chars) in a TU")
				.hasArg()
				.create("minlr") );
		options.addOption( OptionBuilder.withLongOpt( "MaxTuLenRatio" )
				.withDescription( "maximum ratio of length (in chars) in a TU")
				.hasArg()
				.create("maxlr") );
		options.addOption( OptionBuilder.withLongOpt( "help" )
				.withDescription( "Help" )
				.create("h") );
		return options;
	}


	public  void parseOptions( String[] args) {
		// create the command line parser
		CommandLineParser clParser = new GnuParser();
		try {
			CommandLine line = clParser.parse( options, args );
			/*if(line.hasOption( "cfg"))
				_config = line.getOptionValue("cfg");*/
			if(line.hasOption( "i")) {
				_infile = new File(line.getOptionValue("i"));
				_infile = new File(_infile.getAbsolutePath());
				if (!_infile.exists()){
					LOGGER.error("input directory does not exist "+ _infile.getAbsolutePath());
					System.exit(0);
				}
			}
			
			if(line.hasOption( "lang")) {
				_langs = LangDetectUtils.updateLanguages(line.getOptionValue("lang"),true).split(Constants.SEMICOLON);
				if (_langs.length!=2){
					LOGGER.error("You should provide 2 languages.");
					help();
				}
				_langDetector = LangDetectUtils.loadLangDetectors(_langs,defaultlangDetectorId);
			}
			if(line.hasOption( "mintuvl"))
				_minTuvLen = Integer.parseInt(line.getOptionValue("mtuvl"));
			if(line.hasOption( "maxtuvl"))
				_maxTuvLen = Integer.parseInt(line.getOptionValue("mtuvl"));
			if(line.hasOption( "minlr"))
				_minlr = Double.parseDouble(line.getOptionValue("minlr"));
			if(line.hasOption( "maxlr"))
				_maxlr = Double.parseDouble(line.getOptionValue("maxlr"));
		}catch( ParseException exp ) {
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
	public File getInfile() {
		return _infile;
	}
		
	public String[] getLanguage() { 
		return _langs;
	}
	
	public int getMinTuvLen() {
		return _minTuvLen;
	}
	public int getMaxTuvLen() {
		return _maxTuvLen;
	}
	
	public double getMaxTuLenRatio() {
		return _maxlr;
	}
	public double getMinTuLenRatio() {
		return _minlr;
	}
	
	public LangDetector getLangDetector() {
		return _langDetector;	
	}
	
}
