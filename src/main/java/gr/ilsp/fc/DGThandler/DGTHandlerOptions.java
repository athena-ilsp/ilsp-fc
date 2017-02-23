package gr.ilsp.fc.DGThandler;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

public class DGTHandlerOptions {
	private Options options;
	private String APPNAME = "DGT Handler";
	
	private static final Logger LOGGER = Logger.getLogger(DGTHandlerOptions.class);
	private File _infile = null;
	private String _l1="";
	private String _l2="";
	private  int _minTuvLen = 0;
	private  double _minTULenRatio = 0;
	private  double _maxTULenRatio = 100;
	private static double _median_word_length=100; //18
	private static double _max_word_length=100;	//25	
	
	public DGTHandlerOptions() {
		createOptions();
	}

	@SuppressWarnings("static-access")
	private Options createOptions() {
		options = new Options();
		options.addOption( OptionBuilder.withLongOpt( "inFile" )
				.withDescription( "TMX files in this directory will be examined" )
				.isRequired()
				.hasArg()
				.create("i") );
		options.addOption( OptionBuilder.withLongOpt( "l1" )
				.withDescription( "Source language" )
				.hasArg()
				.create("l1") );
		options.addOption( OptionBuilder.withLongOpt( "l2" )
				.withDescription( "Target language" )
				.hasArg()
				.create("l2") );
		options.addOption( OptionBuilder.withLongOpt( "Min_TUV_Length" )
				.withDescription( "minimum length of an acceptable TUV")
				.hasArg()
				.create("mtuvl") );
		options.addOption( OptionBuilder.withLongOpt( "MinTuLenRatio" )
				.withDescription( "minimum ratio of length (in chars) in a TU")
				.hasArg()
				.create("minlr") );
		options.addOption( OptionBuilder.withLongOpt( "MaxTuLenRatio" )
				.withDescription( "maximum ratio of length (in chars) in a TU")
				.hasArg()
				.create("maxlr") );
		options.addOption( OptionBuilder.withLongOpt( "MaxWordLength" )
				.withDescription( "maximum word length (in chars) in a TU")
				.hasArg()
				.create("maxwl") );
		options.addOption( OptionBuilder.withLongOpt( "MedianWordLength" )
				.withDescription( "median word length (in chars) in a TU")
				.hasArg()
				.create("medwl") );
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
			if(line.hasOption( "i")) {
				_infile = new File(line.getOptionValue("i"));
				if (!_infile.exists()){
					LOGGER.error("input file does not exist "+ _infile.getAbsolutePath());
					System.exit(0);
				}
			}
			
			if(line.hasOption( "l1"))
				_l1 = line.getOptionValue("l1");
			else
				LOGGER.error("You should provide the source language.");
			if(line.hasOption( "l2"))
				_l2 = line.getOptionValue("l2");
			else
				LOGGER.error("You should provide the target language.");
			
			if(line.hasOption( "mtuvl"))
				_minTuvLen = Integer.parseInt(line.getOptionValue("mtuvl"));
			
			if(line.hasOption( "minlr"))
				_minTULenRatio = Double.parseDouble(line.getOptionValue("minlr"));
			if(line.hasOption( "maxlr"))
				_maxTULenRatio = Double.parseDouble(line.getOptionValue("maxlr"));
			
			if(line.hasOption( "maxwl"))
				_max_word_length = Double.parseDouble(line.getOptionValue("maxwl"));
			if(line.hasOption( "medwl"))
				_median_word_length = Double.parseDouble(line.getOptionValue("medwl"));
			
		}catch( ParseException exp ) {
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
	public File getInFile() {
		return _infile;
	}
	public String getL1() { 
		return _l1;
	}
	public String getL2() { 
		return _l2;
	}
	public int getMinTuvLen() {
		return _minTuvLen;
	}
	public double getMaxTuLenRatio() {
		return _maxTULenRatio;
	}
	public double getMinTuLenRatio() {
		return _minTULenRatio;
	}
	public double getMaxWordLen() {
		return _max_word_length;
	}
	public double getMedWordLen() {
		return _median_word_length;
	}
}
