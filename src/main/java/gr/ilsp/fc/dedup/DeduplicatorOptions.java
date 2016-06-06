package gr.ilsp.fc.dedup;

import java.io.File;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;

public class DeduplicatorOptions {

	private Options options;
	private String APPNAME = "(Near) Deduplication";
	private static String QUEST_SEPAR = ";";
	
	private String _method="0";
	private static double inter_thr=0.7; //intersection of common paragraphs
	private static int MIN_TOK_LEN = 3; //tokens with less that MIN_TOK_LEN letters are excluded
	private static int MIN_PAR_LEN = 3; //paragraphs with less than MIN_PAR_LEN tokens are excluded
	private static boolean _applyOfflineXSLT= false;
	private File _targetDir = null;
	private File _outBaseName = null;
	private String _inputType = "xml";
	private Set<String> _exludefiles=null;
		
	public DeduplicatorOptions() {
		createOptions();
	}
	
	@SuppressWarnings("static-access")
	private  Options createOptions() {
		options = new Options();

		options.addOption( OptionBuilder.withLongOpt( "methodType" )
				.withDescription( "Method type for deduplication: "
						+ "1 for Deduplication by using lists and MD5 method."
						+ "2 for Deduplication based on common paragraphs."
						+ "0 for applying both methods." )
				.hasArg()
				.create("m") );
		options.addOption( OptionBuilder.withLongOpt( "min_tok_len" )
				.withDescription( "Tokens with less than MIN_TOK_LEN (default is 3) are excluded from content" )
				.hasArg()
				.create("mtl") );
		options.addOption( OptionBuilder.withLongOpt( "min_par_len_in_toks" )
				.withDescription( "Paragraphs with less than MIN_PAR_LEN (default is 3) tokens are excluded from content" )	
				.hasArg()
				.create("mpl") );
		options.addOption( OptionBuilder.withLongOpt( "intersectionThr_paragraphs" )
				.withDescription( "Documents for which the ratio the common paragraphs"
						+ " with the shortest of them is more than this threshold are considered duplicates")	
				.hasArg()
				.create("ithr") );
		options.addOption( OptionBuilder.withLongOpt( "targetdir" )
				.withDescription( "cesDocFiles in this directory will be examined" )
				.hasArg()
				.create("o") );	
		options.addOption( OptionBuilder.withLongOpt( "inputType" )
				.withDescription( "type of input files, default is xml, also supports txt" )
				.hasArg()
				.create("int") );
		options.addOption( OptionBuilder.withLongOpt( "baseName" )
				.withDescription( "baseName to be used for outfiles (textfile or HTML file with list of paths of the remained cesDocFiles)" )
				.hasArg()
				.create("bs") );
		options.addOption( OptionBuilder.withLongOpt( "exclude_files" )
				.withDescription( "cesDocFiles to be excluded for deduplication separated by \";\"" )	
				.hasArg()
				.create("ex") );
		options.addOption( OptionBuilder.withLongOpt( "offlineXslt" )
				.withDescription( "cesDocFiles have been XSLT transformed" )				
				.create("oxslt") );
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
			if(line.hasOption( "m")) 
				_method = line.getOptionValue("mt");
			if(line.hasOption( "mtl")) 
				MIN_TOK_LEN = Integer.parseInt(line.getOptionValue("mtl"));
			if(line.hasOption( "mpl")) 
				MIN_PAR_LEN = Integer.parseInt(line.getOptionValue("mpl"));
			if(line.hasOption( "ithr")) 
				inter_thr = Integer.parseInt(line.getOptionValue("ithr"));
			if(line.hasOption( "o")) {
				_targetDir = new File(line.getOptionValue("o"));
				_targetDir = new File(_targetDir.getAbsolutePath());
			}
			if(line.hasOption( "bs"))
				_outBaseName = new File(line.getOptionValue("bs"));
			if(line.hasOption( "int"))
				_inputType = line.getOptionValue("int");
			if(line.hasOption( "exf")){ 
				String[] temp= line.getOptionValue("exf").split(QUEST_SEPAR);
				for (int ii=0;ii<temp.length;ii++){
					_exludefiles.add(FilenameUtils.concat(_targetDir.getAbsolutePath(), temp[ii]));
				}
			}
			if(line.hasOption( "oxslt"))
				_applyOfflineXSLT = true;
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
	public String getMethod() {
		return _method;
	}
	public int getMinTokLen() {
		return MIN_TOK_LEN;
	}
	public int getMinParLen() {
		return MIN_PAR_LEN;
	}
	public double getInterThr() {
		return inter_thr;
	}
	public File getTargetDir() {
		return _targetDir;
	}
	public File getBaseName() {
		return _outBaseName;
	}
	public Set<String> getListExcludeFiles(){
		return _exludefiles;
	}
	public boolean applyOfflineXSLT(){
		return _applyOfflineXSLT;
	}
	public String getInputType() {
		return _inputType;
	}
}
