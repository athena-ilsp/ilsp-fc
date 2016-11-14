package gr.ilsp.fc.depth;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

public class DepthPostCounterOptions {

	private static final Logger LOGGER = Logger.getLogger(DepthPostCounterOptions.class);
	private Options options;
	private String APPNAME = "DepthPostCounter";
	
	private File _inFile1 = null, _inFile2 = null;
	private boolean _merge = false;
	
	public DepthPostCounterOptions() {
		createOptions();
	}
	
	@SuppressWarnings("static-access")
	private  Options createOptions() {
		options = new Options();

		options.addOption( OptionBuilder.withLongOpt( "input1" )
				.withDescription( "logfile or dir with logfiles" )
				.hasArg()
				.create("i1") );
		options.addOption( OptionBuilder.withLongOpt( "input2" )
				.withDescription( "dir with outfiles" )
				.hasArg()
				.create("i2") );
		options.addOption( OptionBuilder.withLongOpt( "merge" )
				.withDescription( "merge files in case of many logs" )
				.create("m") );		
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
			if(line.hasOption( "h")) 
				help();
			if(line.hasOption( "i1")) 
				_inFile1 = new File(line.getOptionValue("i1"));
			else
				help();
			if (!_inFile1.exists()){
				LOGGER.error("the input does not exist!");
				System.exit(0);
			}
			if(line.hasOption( "i2")) 
				_inFile2 = new File(line.getOptionValue("i2"));
			else
				help();
			if (!_inFile2.exists()){
				LOGGER.error("the input does not exist!");
				System.exit(0);
			}
			
			
			if(line.hasOption( "m"))
				_merge=true;
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
	
	public File getInput1() {
		return _inFile1;
	}
	public File getInput2() {
		return _inFile2;
	}
	
	public boolean toMerge() {
		return _merge;
	}
	
}





	
	

