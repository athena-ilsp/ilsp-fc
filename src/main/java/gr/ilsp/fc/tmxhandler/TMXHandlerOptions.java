package gr.ilsp.fc.tmxhandler;

import gr.ilsp.fc.langdetect.LangDetectUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

public class TMXHandlerOptions {
	private Options options;
	private String APPNAME = "TMX Handler";
	private static final String HTML =".html";
	private static final Logger LOGGER = Logger.getLogger(TMXHandlerOptions.class);
	private static final String QUESTION_SEP = ";";
	private File _targetDir = null;
	private File _outTMX = null;
	private File _outHTML = null;
	private String _config;
	private String _language;
	private String _doctypes="aupdih";
	private boolean xslt=false;
	private boolean _cc=false;
	private boolean _metadata=false;
	private List<String> _segtypes=new ArrayList<String>();
	private int[] _thres={ 10, 10, 10, 10, 10, 10, 10, 10};
	private static final String QUEST_SEPAR = ";";
	
	public TMXHandlerOptions() {
		createOptions();
	}

	@SuppressWarnings("static-access")
	private Options createOptions() {
		options = new Options();
		options.addOption( OptionBuilder.withLongOpt( "config" )
				.withDescription( "XML file with configuration for the tool." )
				.hasArg()
				.create("cfg") );
		options.addOption( OptionBuilder.withLongOpt( "inputDir" )
				.withDescription( "TMX files in this directory will be examined" )
				.isRequired()
				.hasArg()
				.create("i") );
		options.addOption( OptionBuilder.withLongOpt( "outputTMXFile" )
				.withDescription( "content of the merged TMX files will be stored in this new TMX file" )
				.isRequired()
				.hasArg()
				.create("tmx") );
		options.addOption( OptionBuilder.withLongOpt( "transform_TMX2HTML" )
				.withDescription( "render the generated merged TMX file as HTML" )
				.create("xslt") );
		options.addOption( OptionBuilder.withLongOpt( "language(s)" )
				.withDescription( "Target languages separated by ';' i.e. en;el" )
						.hasArg()
						.create("lang") );
		options.addOption( OptionBuilder.withLongOpt( "TMX_types_to_be_processed" )
				.withDescription( "types of TMXs, i.e. methods the document pairs have been detected (auipdhml), to be processed, default is \"auipdh\"")
				.hasArg()
				.create("doctypes") );
		options.addOption( OptionBuilder.withLongOpt( "thresholds_for_0:1_alignments_per_type" )
				.withDescription( "it should be of the same length with the types parameter. "
						+ "If a TMX of type X contains more 0:1 segment pairs than the corresponding threshold, it will not be selected ")
				.hasArg()
				.create("thres") );
		options.addOption( OptionBuilder.withLongOpt( "SegmentTypes_in_TMXs_to_be_processed" )
				.withDescription( "types of segment pairs to be procesed. If omitted, segments of all types will be processed. "
						+ "Otherwise put segment types seperated by \";\" i.e. \"1:1;1:2;2:1\"")
				.hasArg()
				.create("segtypes") );
		options.addOption( OptionBuilder.withLongOpt( "licence_detected_in_document_pairs" )
				.withDescription( "If exist, only document pairs for which"
						+ " a license has been detected will be selected in merged TMX.")
				.create("cc") );
		options.addOption( OptionBuilder.withLongOpt( "export_collection_metadata" )
				.withDescription( "If exist, metadata of the collection (i.e. the merged TMX file), will be exported")
				.create("metadata") );
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
			if(line.hasOption( "cfg")) {
				_config = line.getOptionValue("cfg");
			}
			if(line.hasOption( "i")) {
				_targetDir = new File(line.getOptionValue("i"));
				_targetDir = new File(_targetDir.getAbsolutePath());
				if (!_targetDir.exists()){
					LOGGER.error("input directory does not exist "+ _targetDir.getAbsolutePath());
					System.exit(0);
				}
			}
			if(line.hasOption( "tmx")){
				_outTMX = new File(line.getOptionValue("tmx"));
				_outTMX = new File(_outTMX.getAbsolutePath());
				if (!_outTMX.getParentFile().exists()){
					_outTMX.getParentFile().mkdirs();
				}
				if (line.hasOption("xslt")){
					_outHTML=new File(_outTMX.getAbsolutePath()+HTML);
				}
				if (line.hasOption("cc")){
					_cc=true;
				}
				if (line.hasOption("metadata"))
					_metadata=true;
			}else{
				LOGGER.error("You should provide a fullpath of the TMX to be constructed.");
			}
			if(line.hasOption( "lang")) {
				_language = LangDetectUtils.updateLanguages(line.getOptionValue("lang").toLowerCase());
				if (_language.split(QUEST_SEPAR).length!=2){
					LOGGER.error("You should provide 2 languages.");
					help();
				}
			}else{
				LOGGER.error("No languages have been defined.");
				System.exit(0);
			}
			if(line.hasOption( "doctypes")){
				_doctypes = line.getOptionValue("doctypes");
			}
			if(line.hasOption( "thres")){
				String[] temp = line.getOptionValue("thres").split(QUESTION_SEP); 
				if (_doctypes.length()!=temp.length){
					LOGGER.error("for each method a threshold should be defined");
					help();
					System.exit(0);
				}
				_thres = new int[temp.length];
				for (int ii=0;ii<temp.length;ii++){
					_thres[ii]= Integer.parseInt(temp[ii]);
				}
			}
			if(line.hasOption( "segtypes")){
				String[] temp= line.getOptionValue("segtypes").split(QUESTION_SEP); 
				for (String str:temp){
					_segtypes.add(str);
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
	public File getOutTMX() {
		return _outTMX;
	}

	public boolean getXSLTransform() {
		return xslt;
	}
	public boolean getCC() {
		return _cc;
	}
	public boolean getMetadata() {
		return _metadata;
	}
	public File getOutHTML(){
		return _outHTML;
	}
	public String getLanguage() { 
		return _language;
	}
	public String getDocTypes() {
		return _doctypes;
	}
	public List<String> getSegTypes() {
		return _segtypes;
	}
	public int[] getThres() {
		return _thres;
	}
	public String getConfig(){
		return _config;
	}
}
