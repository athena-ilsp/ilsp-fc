package gr.ilsp.fc.experiments;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class DataSetsCleanerOptions {
	private Options options;
	private String APPNAME = "DataSets Creation";
	private String _process="", _cols1="", _cols2="";
	private File _infile1 = null;
	private File _infile2 = null;
	private File _outfile1 = null;
	
	public DataSetsCleanerOptions() {
		createOptions();
	}
	
	@SuppressWarnings("static-access")
	private  Options createOptions() {
		options = new Options();

		options.addOption( OptionBuilder.withLongOpt( "processType" )
				.withDescription( "Process to apply : "
						+ "1 Deduplication of parts in a file)."
						+ "2 ." )
				.hasArg()
				.create("p") );
		options.addOption( OptionBuilder.withLongOpt( "processCols1" )
				.withDescription( " ids of columns of textfile1 to be process, starting from 0. Ids should be separated by ;")
				.hasArg()
				.create("c1") );
		options.addOption( OptionBuilder.withLongOpt( "processCols2" )
				.withDescription( " ids of columns of textfile2 to be process, starting from 0. Ids should be separated by ;")
				.hasArg()
				.create("c2") );
		options.addOption( OptionBuilder.withLongOpt( "targetFile1" )
				.withDescription( "fullpath of file to be processed" )
				.hasArg()
				.create("if1") );	
		options.addOption( OptionBuilder.withLongOpt( "targetFile2" )
				.withDescription( "fullpath of file to be processed" )
				.hasArg()
				.create("if2") );	
		options.addOption( OptionBuilder.withLongOpt( "outputFile1" )
				.withDescription( "fullpath of the output file, it will be the updated targetFile1" )
				.hasArg()
				.create("of1") );
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
			if(line.hasOption( "p")) 
				_process = line.getOptionValue("p");
			if(line.hasOption( "c1")) 
				_cols1 = line.getOptionValue("c1");
			if(line.hasOption( "c2")) 
				_cols2 = line.getOptionValue("c2");
			if(line.hasOption( "if1"))
				_infile1 = new File(line.getOptionValue("if1"));
			if(line.hasOption( "if2"))
				_infile2 = new File(line.getOptionValue("if2"));
			if(line.hasOption( "of1"))
				_outfile1 = new File(line.getOptionValue("of1"));
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
	public String getProcess() {
		return _process;
	}
	public String getCols1() {
		return _cols1;
	}
	public String getCols2() {
		return _cols2;
	}
	public File getInFile1() {
		return _infile1;
	}
	public File getInFile2() {
		return _infile2;
	}
	public File getOutFile1() {
		return _outfile1;
	}
}
