package gr.ilsp.fc.monomerge;

import gr.ilsp.fc.langdetect.LangDetectUtils;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

public class MonoMergerOptions {
	private Options options;
	private String APPNAME = "TMX Handler";
	
	private static final Logger LOGGER = Logger.getLogger(MonoMergerOptions.class);
	private File _targetDir = null;
	private File _baseName=null;
	private String _language;
	private boolean _oxslt=false;
	private boolean _iso6393=false;
	private boolean _cc=false;
	
	public MonoMergerOptions() {
		createOptions();
	}

	@SuppressWarnings("static-access")
	private Options createOptions() {
		options = new Options();
		options.addOption( OptionBuilder.withLongOpt( "inputDir" )
				.withDescription( "TMX files in this directory will be examined" )
				.isRequired()
				.hasArg()
				.create("i") );
		options.addOption( OptionBuilder.withLongOpt( "baseName" )
				.withDescription( "baseName to be used for outfiles" )
				.isRequired()
				.hasArg()
				.create("bs") );
		options.addOption( OptionBuilder.withLongOpt( "transform_TMX2HTML" )
				.withDescription( "render the generated merged TMX file as HTML" )
				.create("oxslt") );
		options.addOption( OptionBuilder.withLongOpt( "language" )
				.withDescription( "Target language" )
				.hasArg()
				.create("lang") );
		options.addOption( OptionBuilder.withLongOpt( "lang_code" )
				.withDescription( "if exists iso6393 language code is used.")
				.create("iso6393") );
		options.addOption( OptionBuilder.withLongOpt( "licence_detected_in_documents" )
				.withDescription( "If exist, only documents for which"
						+ " a license has been detected will be selected in collection.")
				.create("cc") );
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
				_targetDir = new File(line.getOptionValue("i"));
				_targetDir = new File(_targetDir.getAbsolutePath());
				if (!_targetDir.exists()){
					LOGGER.error("input directory does not exist "+ _targetDir.getAbsolutePath());
					System.exit(0);
				}
			}
			if(line.hasOption( "bs")){
				_baseName = new File(line.getOptionValue("bs"));
				_baseName = _baseName.getAbsoluteFile();
			}else{
				LOGGER.error("You should provide a baseName to be used for outfiles.");
			}
			if (line.hasOption("oxslt"))
				_oxslt=true;
			if (line.hasOption("cc"))
				_cc=true;
			if (line.hasOption("iso6393"))		
				_iso6393=true;
			if(line.hasOption( "lang")) {
				_language = LangDetectUtils.updateLanguages(line.getOptionValue("lang").toLowerCase(),_iso6393);
			}else{
				LOGGER.error("No language has been defined.");
				System.exit(0);
			}
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
	public File getTargetDir() {
		return _targetDir;
	}
	public boolean getXSLTransform() {
		return _oxslt;
	}
	public boolean useISO6393() {
		return _iso6393;
	}
	public boolean getCC() {
		return _cc;
	}
	public File getBaseName(){
		return _baseName;
	}
	public String getLanguage() { 
		return _language;
	}
}
