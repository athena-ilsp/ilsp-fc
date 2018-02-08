package gr.ilsp.fc.tmxhandler;

import gr.ilsp.fc.langdetect.LangDetectUtils;
import gr.ilsp.nlp.commons.Constants;

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

public class GetTMXsubsetOptions {
	private Options options;
	private String APPNAME = "Get TMX subsets";
	
	private static final Logger LOGGER = Logger.getLogger(GetTMXsubsetOptions.class);
	//private static final String QUEST_SEPAR = ";";
	//private static final String TAB_STR = "\t";
	private List<String> _sites ;
	private Map<String,String> _psi = null;
	private File _targetTMX = null;
	private File _baseName = null;
	private String _descr;
	private File _o1 = null;
	private File _o2 = null;
	private String _language;
	private boolean _oxslt = false;
	private boolean _keepsn = false;
	private boolean _clean = false;
	private boolean _iso6393 = false;
	private boolean _cc = false;
	private boolean _keepem = false;
	private boolean _keepiden = false;
	private boolean _keepdup = false;
	private int _samplesize = 0;
	private  int _minTuvLen = 3;
	private  double _minScore = 0;
	//private  double _minPerce01Align = 0.16;
	private  double _minTULenRatio = 0.6;
	private  double _maxTULenRatio = 1.6;
	
	public GetTMXsubsetOptions() {
		createOptions();
	}

	@SuppressWarnings("static-access")
	private Options createOptions() {
		options = new Options();
		options.addOption( OptionBuilder.withLongOpt( "input" )
				.withDescription( "TMX file to get a subset from it" )
				.isRequired()
				.hasArg()
				.create("i") );
		options.addOption( OptionBuilder.withLongOpt( "domain" )
				.withDescription( "A descriptive title for the targeted domain" )
				.hasArg()
				.create("dom") );
		options.addOption( OptionBuilder.withLongOpt( "baseName" )
				.withDescription( "baseName to be used for outfiles" )
				.isRequired()
				.hasArg()
				.create("bs") );
		options.addOption( OptionBuilder.withLongOpt( "transform_TMX2HTML" )
				.withDescription( "render the generated merged TMX file as HTML" )
				.create("oxslt") );
		options.addOption( OptionBuilder.withLongOpt( "language(s)" )
				.withDescription( "Target languages separated by ';' i.e. en;el" )
				.hasArg()
				.create("lang") );
		options.addOption( OptionBuilder.withLongOpt( "lang_code" )
				.withDescription( "if exists iso6393 language codes are used.")
				.create("iso6393") );
		options.addOption( OptionBuilder.withLongOpt( "licence_detected_in_document_pairs" )
				.withDescription( "If exist, only document pairs for which"
						+ " a license has been detected will be selected in merged TMX.")
				.create("cc") );
		options.addOption( OptionBuilder.withLongOpt( "Min_TUV_Length" )
				.withDescription( "minimum length of an acceptable TUV")
				.hasArg()
				.create("mtuvl") );
		options.addOption( OptionBuilder.withLongOpt( "SampleSize_TUs" )
				.withDescription( "sample size in TUs")
				.hasArg()
				.create("size") );
		options.addOption( OptionBuilder.withLongOpt( "MinAlignerScore" )
				.withDescription( "minimum score of a TU given by the aligner")
				.hasArg()
				.create("minscore") );
		options.addOption( OptionBuilder.withLongOpt( "MinTuLenRatio" )
				.withDescription( "minimum ratio of length (in chars) in a TU")
				.hasArg()
				.create("minlr") );
		options.addOption( OptionBuilder.withLongOpt( "MaxTuLenRatio" )
				.withDescription( "maximum ratio of length (in chars) in a TU")
				.hasArg()
				.create("maxlr") );
		options.addOption( OptionBuilder.withLongOpt( "KeepTuSameNum" )
				.withDescription( "keeps only TUs with same digits")
				.create("ksn") );
		options.addOption( OptionBuilder.withLongOpt( "KeepNonAnnotatedTu" )
				.withDescription( "keeps only non-annotated TUs")
				.create("clean") );
		options.addOption( OptionBuilder.withLongOpt( "KeepEmpty" )
				.withDescription( "keeps TUs, even if one of its TUV does not contain any letter")
				.create("keepem") );
		options.addOption( OptionBuilder.withLongOpt( "KeepIdentical" )
				.withDescription( "keeps TUs, even if its TUVs are identical after removing non-letters")
				.create("keepiden") );
		options.addOption( OptionBuilder.withLongOpt( "KeepDuplicates" )
				.withDescription( "keeps duplicate TUs")
				.create("keepdup") );
		options.addOption( OptionBuilder.withLongOpt( "sites" )
				.withDescription( "A list of accepted websites" )
				.hasArg()
				.create("sites") );
		options.addOption( OptionBuilder.withLongOpt( "psi" )
				.withDescription( "text file containing PSinfo. A website and its PSinfo (yes,no,unclear), separated by \" \", per line" )
				.hasArg()
				.create("psi") );
		options.addOption( OptionBuilder.withLongOpt( "specific_output1" )
				.withDescription( "Not used (Specific outout1)")
				.hasArg()
				.create("o1") );
		options.addOption( OptionBuilder.withLongOpt( "specific_output2" )
				.withDescription( "Not used(Specific outout2)")
				.hasArg()
				.create("o2") );
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
			if(cmdline.hasOption( "i")) {
				_targetTMX = new File(cmdline.getOptionValue("i")).getAbsoluteFile();
				if (!_targetTMX.exists()){
					LOGGER.error("input file does not exist "+ _targetTMX.getAbsolutePath());
					System.exit(0);
				}
			}
			if(cmdline.hasOption( "lang")) {
				_language = LangDetectUtils.updateLanguages(cmdline.getOptionValue("lang"),_iso6393);
				if (_language.split(Constants.SEMICOLON).length!=2){
					LOGGER.error("You should provide 2 languages.");
					help();
				}
			}else{
				LOGGER.error("No languages have been defined.");
				System.exit(0);
			}
			if(cmdline.hasOption( "bs"))
				_baseName = new File(cmdline.getOptionValue("bs")).getAbsoluteFile();
			else{
				LOGGER.error("You should provide a baseName to be used for outfiles.");
				System.exit(0);
			}
			if (cmdline.hasOption("dom"))
				_descr=cmdline.getOptionValue("dom");
			if (cmdline.hasOption("oxslt"))
				_oxslt=true;
			if (cmdline.hasOption("cc")) //keep CC TUs omly
				_cc=true;
			if (cmdline.hasOption("keepem")) //keep "empty" TUs
				_keepem=true;
			if (cmdline.hasOption("keepiden")) // keep TUs with "identical" TUVs
				_keepiden=true;
			if (cmdline.hasOption("keepdup")) //keep duplicate TUs
				_keepdup=true;
			if(cmdline.hasOption( "ksn")) //keep only TUs with same numbers in the TUVs
				_keepsn = true;
			if (cmdline.hasOption("clean")) //keep not-annotated TUs only
				_clean=true;
			if (cmdline.hasOption("iso6393"))		
				_iso6393=true;
			
			if(cmdline.hasOption( "mtuvl"))
				_minTuvLen = Integer.parseInt(cmdline.getOptionValue("mtl"));
			if(cmdline.hasOption( "minlr"))
				_minTULenRatio = Double.parseDouble(cmdline.getOptionValue("minlr"));
			if(cmdline.hasOption( "maxlr"))
				_maxTULenRatio = Double.parseDouble(cmdline.getOptionValue("maxlr"));
			if(cmdline.hasOption( "minscore"))
				_minScore = Double.parseDouble(cmdline.getOptionValue("minscore"));
			
			if(cmdline.hasOption( "size"))
				_samplesize = Integer.parseInt(cmdline.getOptionValue("size"));
			
			if(cmdline.hasOption( "sites")){
				try {
					_sites = FileUtils.readLines(new File(cmdline.getOptionValue("sites")));
				} catch (IOException e) {
					LOGGER.error("Text file containing a list of accepted websites does not exist.");	
					e.printStackTrace();
				}	
			}
			if(cmdline.hasOption( "psi")){
				try {
					 List<String> lines= FileUtils.readLines(new File(cmdline.getOptionValue("psi")));
					 _psi = new HashMap<String, String>();
					for (String line:lines){
						String[] info = line.toLowerCase().split(Constants.TAB);
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
	public String getTargetedDomain() {
		return _descr;
	}
	public File getO1() {
		return _o1;
	}
	public File getO2() {
		return _o2;
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
	public boolean keepTuSameNum() {
		return _keepsn;
	}
	public boolean getKeepEmpty() {
		return _keepem;
	}
	public boolean getClean() {
		return _clean;
	}
	public boolean getKeepIdentical() {
		return _keepiden;
	}
	public boolean getKeepDuplicates() {
		return _keepdup;
	}
	public File getBaseName(){
		return _baseName;
	}
	public String getLanguage() { 
		return _language;
	}
	public int getMinTuvLen() {
		return _minTuvLen;
	}
	public int getSampleSize() {
		return _samplesize;
	}
	//public double getMinPerce01Align() {
	//	return _minPerce01Align;
	//}
	public double getMaxTuLenRatio() {
		return _maxTULenRatio;
	}
	public double getMinTuLenRatio() {
		return _minTULenRatio;
	}
	public double getMinScore() {
		return _minScore;
	}
	public List<String> getSites() {
		return _sites;
	}
	public Map<String, String> getPSinfo() {
		return _psi;
	}
}
