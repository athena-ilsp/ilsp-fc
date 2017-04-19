package gr.ilsp.fc.tmxhandler;

import gr.ilsp.fc.langdetect.LangDetectUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

public class TMXinfoUpdaterOptions {
	private Options options;
	private String APPNAME = "TMXinfo Updater";
	
	private static final Logger LOGGER = Logger.getLogger(TMXinfoUpdaterOptions.class);
	private static final String QUEST_STR = ";";
	private static final String SPACE_STR = " ";
	private static final String TAB_STR = "\t";
	
	private Map<String,String> _psi =new HashMap<String, String>();
	private File _targetTMX = null;
	private String _language;
	private boolean _iso6393=false;
	//private  int _minTuvLen = 0;
	//private  double _minTULenRatio = 0;
	//private  double _maxTULenRatio = 100;
	
	public TMXinfoUpdaterOptions() {
		createOptions();
	}

	@SuppressWarnings("static-access")
	private Options createOptions() {
		options = new Options();
		options.addOption( OptionBuilder.withLongOpt( "inputTMX" )
				.withDescription( "TMX file to be updated with properties" )
				.isRequired()
				.hasArg()
				.create("i") );
		options.addOption( OptionBuilder.withLongOpt( "language(s)" )
				.withDescription( "Target languages separated by ';' i.e. en;el" )
				.hasArg()
				.create("lang") );
		options.addOption( OptionBuilder.withLongOpt( "lang_code" )
				.withDescription( "if exists iso6393 language codes are used.")
				.create("iso6393") );
		options.addOption( OptionBuilder.withLongOpt( "psi" )
				.withDescription( "text file containing PSinfo. A website and its PSinfo (yes,no,unclear), separated by \" \", per line" )
				.hasArg()
				.create("psi") );
		options.addOption( OptionBuilder.withLongOpt( "help" )
				.withDescription( "Help" )
				.create("h") );
		return options;
	}


	public  void parseOptions( String[] args) {
		// create the command line parser
		CommandLineParser clParser = new GnuParser();
		try {
			CommandLine cmdline = clParser.parse( options, args );
			if (cmdline.hasOption("i"))
			_targetTMX = new File(cmdline.getOptionValue("i"));
			if (cmdline.hasOption("iso6393"))		
				_iso6393=true;
			if(cmdline.hasOption( "lang")) {
				_language = LangDetectUtils.updateLanguages(cmdline.getOptionValue("lang").toLowerCase(),_iso6393);
				if (_language.split(QUEST_STR).length!=2){
					LOGGER.error("You should provide 2 languages.");
					help();
				}
			}else{
				LOGGER.error("No languages have been defined.");
				System.exit(0);
			}
			//if(cmdline.hasOption( "mtuvl"))
			//	_minTuvLen = Integer.parseInt(cmdline.getOptionValue("mtl"));
			//if(cmdline.hasOption( "minlr"))
			//	_minTULenRatio = Double.parseDouble(cmdline.getOptionValue("minlr"));
			//if(cmdline.hasOption( "maxlr"))
			//	_maxTULenRatio = Double.parseDouble(cmdline.getOptionValue("maxlr"));
			if(cmdline.hasOption( "psi")){
				try {
					 List<String> lines= FileUtils.readLines(new File(cmdline.getOptionValue("psi")));
					for (String line:lines){
						String[] info = line.toLowerCase().split(TAB_STR);
						//System.out.println(line);
						if (info.length!=2)
							_psi.put(info[0], "");
						else
							_psi.put(info[0], info[1]);
					}
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
	public File getTargetTMX() {
		return _targetTMX;
	}
	public boolean useISO6393() {
		return _iso6393;
	}
	public String getLanguage() { 
		return _language;
	}
	//public int getMinTuvLen() {
	//	return _minTuvLen;
	//}
	//public double getMaxTuLenRatio() {
	//	return _maxTULenRatio;
	//}
	//public double getMinTuLenRatio() {
	//	return _minTULenRatio;
	//}
	public Map<String, String> getPSinfo() {
		return _psi;
	}
}
