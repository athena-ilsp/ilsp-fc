package gr.ilsp.fc.monomerge;

import gr.ilsp.fc.langdetect.LangDetectUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

public class MonoMergerOptions {
	private Options options;
	private String APPNAME = "TMX Handler";

	private static final Logger LOGGER = Logger.getLogger(MonoMergerOptions.class);
	private File _targetDir = null;
	private File _baseName=null;
	private String _language, _userTopic="", _config; //_agentName="ILSP-FC",
	private List<String> _sites= new ArrayList<String>();
	private boolean _cc=false;
	private String _corpuslevel="doc";
	private static final String CORPUS = "-corpus";
	private static final String UNDERSCORE_STR="_";
	
	public MonoMergerOptions() {
		createOptions();
	}

	@SuppressWarnings("static-access")
	private Options createOptions() {
		options = new Options();
		options.addOption( OptionBuilder.withLongOpt( "config" )
				.withDescription( "Path to the XML configuration file" )
				.hasArg()
				.create("cfg") );
		options.addOption( OptionBuilder.withLongOpt( "inputDir" )
				.withDescription( "A directory or a text file with fullpaths of directories per textline. XML files in the directories will be examined" )
				.isRequired()
				.hasArg()
				.create("i") );
		options.addOption( OptionBuilder.withLongOpt( "baseName" )
				.withDescription( "baseName to be used for outfiles" )
				.isRequired()
				.hasArg()
				.create("bs") );
		options.addOption( OptionBuilder.withLongOpt( "language" )
				.withDescription( "Target language" )
				.hasArg()
				.create("lang") );
		/*options.addOption( OptionBuilder.withLongOpt( "agentName" )
				.withDescription( "agent/creator name" )
				.hasArg()
				.create("a") );*/
		options.addOption( OptionBuilder.withLongOpt( "licence_detected_in_documents" )
				.withDescription( "If exist, only documents for which"
						+ " a license has been detected will be selected in collection.")
						.create("cc") );
		options.addOption( OptionBuilder.withLongOpt( "level of corpus' item" )
				.withDescription( "corpus consists of txt documents (default), or paragraphs, or sentences")
				.hasArg()
				.create("corpusLevel") );
		options.addOption( OptionBuilder.withLongOpt( "uderTopic" )
				.withDescription( "A descriptive title for the targeted domain" )
				.hasArg()
				.create("dom") );
		options.addOption( OptionBuilder.withLongOpt( "sites" )
				.withDescription( "A list of accepted websites" )
				.hasArg()
				.create("sites") );
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
			if(line.hasOption( "cfg")) 
				_config = line.getOptionValue("cfg");
					
			if(line.hasOption( "i")) {
				_targetDir = new File(line.getOptionValue("i")).getAbsoluteFile();
				if (!_targetDir.exists()){
					LOGGER.error("input directory does not exist "+ _targetDir.getAbsolutePath());
					System.exit(0);
				}
			}
			if (line.hasOption("cc"))
				_cc=true;
			if (line.hasOption("corpusLevel")){
				_corpuslevel=line.getOptionValue("corpusLevel");
				if (!(_corpuslevel.equals("doc") || _corpuslevel.equals("par") || _corpuslevel.equals("sen"))){
					LOGGER.error("Value should be \"doc\" (default) or \"par\" or \"sen\".");
					System.exit(0);
				}
			}
			if (line.hasOption("dom"))
				_userTopic = line.getOptionValue("dom");
			//if (line.hasOption("a"))
			//	_agentName = line.getOptionValue("a").replace(" ", "_");
			if(line.hasOption( "lang")) 
				_language = LangDetectUtils.updateLanguages(line.getOptionValue("lang").toLowerCase(),true);
			else{
				LOGGER.error("No language has been defined.");
				System.exit(0);
			}
			if(line.hasOption( "bs")) //_baseName = new File(line.getOptionValue("bs")+UNDERSCORE_STR+_agentName+CORPUS+UNDERSCORE_STR+_corpuslevel);
				_baseName = new File(line.getOptionValue("bs")+UNDERSCORE_STR+CORPUS+UNDERSCORE_STR+_corpuslevel);
			else
				LOGGER.error("You should provide a baseName to be used for outfiles.");
			
			if(line.hasOption( "sites")){
				try {
					_sites = FileUtils.readLines(new File(line.getOptionValue("sites")));
				} catch (IOException e) {
					LOGGER.error("Text file containing a list of accepted websites does not exist.");	
					e.printStackTrace();
				}	
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
	public boolean getCC() {
		return _cc;
	}
	public File getBaseName(){
		return _baseName;
	}
	public String getLanguage() { 
		return _language;
	}
	public String getUserTopic() {
		return _userTopic;
	}
	public List<String> getSites() {
		return _sites;
	}
	public String getCorpusLevel() {
		return _corpuslevel;
	}
	//public String getAgentName(){
	//	return _agentName;
	//}
	public String getConfig(){
		return _config;
	}
}
