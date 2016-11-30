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
	private String _config;
	private String _language, _domain="", _agent;
	//private boolean _oxslt=false;
	private boolean _cc=false;
	private String _corpuslevel="doc";
	private static final String CORPUS = "-corpus";
	private static final String UNDERSCORE_STR="_";
	private static final String SEMICOLON_STR=";";
	
	public MonoMergerOptions() {
		createOptions();
	}

	@SuppressWarnings("static-access")
	private Options createOptions() {
		options = new Options();
		options.addOption( OptionBuilder.withLongOpt( "inputDir" )
				.withDescription( "A directory or a text file with fullpaths of directories per textline. XML files in the directories will be examined" )
				.isRequired()
				.hasArg()
				.create("dest") );
		options.addOption( OptionBuilder.withLongOpt( "baseName" )
				.withDescription( "baseName to be used for outfiles" )
				.isRequired()
				.hasArg()
				.create("bs") );
		/*options.addOption( OptionBuilder.withLongOpt( "transform_TMX2HTML" )
				.withDescription( "render the generated merged TMX file as HTML" )
				.create("oxslt") );*/
		options.addOption( OptionBuilder.withLongOpt( "language" )
				.withDescription( "Target language" )
				.hasArg()
				.create("lang") );
		options.addOption( OptionBuilder.withLongOpt( "agent" )
				.withDescription( "agent/creator name" )
				.hasArg()
				.create("a") );
		options.addOption( OptionBuilder.withLongOpt( "licence_detected_in_documents" )
				.withDescription( "If exist, only documents for which"
						+ " a license has been detected will be selected in collection.")
						.create("cc") );
		options.addOption( OptionBuilder.withLongOpt( "level of corpus' item" )
				.withDescription( "copus consists of txt documents (default), or paragraphs, or sentences")
				.hasArg()
				.create("level") );
		options.addOption( OptionBuilder.withLongOpt( "domain" )
				.withDescription( "A descriptive title for the targeted domain" )
				.hasArg()
				.create("dom") );
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
			if(line.hasOption( "dest")) {
				_targetDir = new File(line.getOptionValue("dest"));
				_targetDir = new File(_targetDir.getAbsolutePath());
				if (!_targetDir.exists()){
					LOGGER.error("input directory does not exist "+ _targetDir.getAbsolutePath());
					System.exit(0);
				}
			}
			//if (line.hasOption("oxslt"))
			//	_oxslt=true;
			if (line.hasOption("cc"))
				_cc=true;
			if (line.hasOption("level")){
				_corpuslevel=line.getOptionValue("level");
				if (!(_corpuslevel.equals("doc") || _corpuslevel.equals("par") || _corpuslevel.equals("sen"))){
					LOGGER.error("Value should be \"doc\" (default) or \"par\" or \"sen\".");
					System.exit(0);
				}
			}
			if (line.hasOption("dom"))
				_domain = line.getOptionValue("dom");
			if (line.hasOption("a"))
				_agent = line.getOptionValue("a");
			if(line.hasOption( "lang")) 
				_language = LangDetectUtils.updateLanguages(line.getOptionValue("lang").toLowerCase(),true).split(SEMICOLON_STR)[0];
			else{
				LOGGER.error("No language has been defined.");
				System.exit(0);
			}
			if(line.hasOption( "bs"))
				_baseName = new File(line.getOptionValue("bs")+UNDERSCORE_STR+_agent+CORPUS+UNDERSCORE_STR+_corpuslevel+UNDERSCORE_STR+_language);
			else
				LOGGER.error("You should provide a baseName to be used for outfiles.");
			
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
	//	public boolean getXSLTransform() {
	//		return _oxslt;
	//	}
	public boolean getCC() {
		return _cc;
	}
	public File getBaseName(){
		return _baseName;
	}
	public String getLanguage() { 
		return _language;
	}
	public String getDomain() {
		return _domain;
	}
	public String getCorpusLevel() {
		return _corpuslevel;
	}
	public String getConfig(){
		return _config;
	}
	public String getAgent(){
		return _agent;
	}
}
