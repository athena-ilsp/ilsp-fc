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
	private static final String SEMICOLON_STR = ";";
	private static final String UNDERSCORE_STR = "_";
	
	private String _method="0";
	private static double inter_thr=0.7; //intersection of common paragraphs
	private static int MIN_TOK_LEN = 3; //tokens with less that MIN_TOK_LEN letters are excluded
	private static int MIN_PAR_LEN = 3; //paragraphs with less than MIN_PAR_LEN tokens are excluded
	private static boolean _applyOfflineXSLT= false;
	private File _targetDir = null;
	private File _outBaseName = null;
	private String _agentName = "ILSP-FC";
	private String _inputType = "xml";
	private Set<String> _excludefiles=null;
		
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
				.create("dedup_meth") );
		options.addOption( OptionBuilder.withLongOpt( "min_tok_len" )
				.withDescription( "Tokens with less than MIN_TOK_LEN (default is 3) are excluded from content" )
				.hasArg()
				.create("dedup_mtl") );
		options.addOption( OptionBuilder.withLongOpt( "min_par_len_in_toks" )
				.withDescription( "Paragraphs with less than MIN_PAR_LEN (default is 3) tokens are excluded from content" )	
				.hasArg()
				.create("dedup_mpl") );
		options.addOption( OptionBuilder.withLongOpt( "intersectionThr_paragraphs" )
				.withDescription( "Documents for which the ratio the common paragraphs"
						+ " with the shortest of them is more than this threshold are considered duplicates")	
				.hasArg()
				.create("dedup_ithr") );
		options.addOption( OptionBuilder.withLongOpt( "targetdir" )
				.withDescription( "cesDocFiles in this directory will be examined" )
				.hasArg()
				.create("i") );	
		options.addOption( OptionBuilder.withLongOpt( "inputType" )
				.withDescription( "type of input files, default is xml, also supports txt" )
				.hasArg()
				.create("dedup_intype") );
		options.addOption( OptionBuilder.withLongOpt( "baseName" )
				.withDescription( "baseName to be used for outfiles (textfile or HTML file with list of paths of the remained cesDocFiles)" )
				.hasArg()
				.create("bs") );
		options.addOption( OptionBuilder.withLongOpt( "exclude_files" )
				.withDescription( "cesDocFiles to be excluded for deduplication separated by \";\"" )	
				.hasArg()
				.create("dedup_ex") );
		options.addOption( OptionBuilder.withLongOpt( "offlineXslt" )
				.withDescription( "cesDocFiles have been XSLT transformed" )				
				.create("oxslt") );
		options.addOption( OptionBuilder.withLongOpt( "agentname" )
				.withDescription( "Agent name to identify the person or the organization responsible for the task" )
				.hasArg()
				.create("a") );
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
			if(line.hasOption( "dedupmeth")) 
				_method = line.getOptionValue("mt");
			if(line.hasOption( "dedup_mtl")) 
				MIN_TOK_LEN = Integer.parseInt(line.getOptionValue("dedup_mtl"));
			if(line.hasOption( "dedup_mpl")) 
				MIN_PAR_LEN = Integer.parseInt(line.getOptionValue("dedup_mpl"));
			if(line.hasOption( "dedup_ithr")) 
				inter_thr = Double.parseDouble(line.getOptionValue("dedup_ithr"));
			if(line.hasOption( "i")) 
				_targetDir = new File(line.getOptionValue("i")).getAbsoluteFile();
			if(line.hasOption( "bs"))
				_outBaseName = new File(line.getOptionValue("bs")+UNDERSCORE_STR+_agentName).getAbsoluteFile();
			if(line.hasOption("a"))
				_agentName= line.getOptionValue("a").replace(" ", "_");
			if(line.hasOption( "dedup_intype"))
				_inputType = line.getOptionValue("dedup_intype");
			if(line.hasOption( "dedup_exf")){ 
				String[] temp= line.getOptionValue("dedup_exf").split(SEMICOLON_STR);
				for (int ii=0;ii<temp.length;ii++){
					_excludefiles.add(FilenameUtils.concat(_targetDir.getAbsolutePath(), temp[ii]));
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
		return _excludefiles;
	}
	public boolean applyOfflineXSLT(){
		return _applyOfflineXSLT;
	}
	public String getInputType() {
		return _inputType;
	}
}
