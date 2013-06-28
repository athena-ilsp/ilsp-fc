package gr.ilsp.fmc.main;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
//import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
//import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
//import org.apache.hadoop.conf.Configuration;
//import org.apache.hadoop.fs.FileSystem;
import org.apache.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.net.InternetDomainName;
//import com.google.common.net.TopPrivateDomain;

public class SimpleCrawlHFSOptions {
	public static int NO_CRAWL_DURATION = 0;
	private  final String APPNAME = "SimpleCrawlHFS crawlandexport";
	private  Options options;
	private  String _domain=null;
	private  String _maindomain=null;
	private  String _descr=null;
	private  String _filter=null;
	private  boolean _debug = false;
	private  String _loggingAppender = null;
	private  String _type="m";
	private  String _outputDir;
	private  String _outputFile;
	private  String _outputFileHTML;
	private  String _agentName;
	private  int _threads = 10;
	private  int _numLoops = 1;
	private  int _crawlDuration = 10;	
	private int _minTokensNumber=200;
	private String _topic=null;
	private String _language;
	private String[] _langKeys;
	private String _urls;
	//changed to true 
	private boolean _keepBoiler = true;
	private boolean _keepimagefp=false;
	private boolean _cesAlign = false;
	private boolean _force = false;
	private boolean offlineXSLT = false;
	private String _config;
	private int _length = 10;
	private static final Logger LOGGER = Logger.getLogger(SimpleCrawlHFSOptions.class);
	//private String ws_dir="/var/lib/tomcat6/webapps/soaplab2-results/";
	private String ws_dir;
	private static String fs = System.getProperty("file.separator");
	protected static Matcher skipLineM = Pattern.compile("^(\\s*)||(#.*)$").matcher("");

	public SimpleCrawlHFSOptions() {
		createOptions();
	}
	@SuppressWarnings("static-access")
	private  Options createOptions() {
		options = new Options();

		options.addOption( OptionBuilder.withLongOpt( "TargetedDomainTitle" )
				.withDescription( "A descriptive title for the targeted domain" )
				.hasArg()
				.create("dom") );
		//vpapa changed use of option -d
		/*options.addOption( OptionBuilder.withLongOpt("hostdomain")
				.withDescription( "domain to crawl (e.g. cnn.com) or path to file " +
						"with domains to crawl. Use for crawling ONLY inside specific domain(s)" )
						.hasArg()
						.create("d") );*/
		options.addOption( OptionBuilder.withLongOpt("stay_in_webdomain")
				.withDescription( "Force monolingual crawler to stay in a specific webdomain" )				
				.create("d") );
		options.addOption( OptionBuilder.withLongOpt( "urls" )
				.withDescription( "file with list of urls to crawl" )
				.hasArg()
				.create("u") );
		options.addOption( OptionBuilder.withLongOpt( "debug" )
				.withDescription( "debug logging" )				
				.create("dbg") );
		options.addOption( OptionBuilder.withLongOpt( "loggingAppender" )
				.withDescription( "set logging appender (console, DRFA)")
				.hasArg()
				.create("l") );
		options.addOption( OptionBuilder.withLongOpt( "outputdir" )
				.withDescription( "output directory" )
				.hasArg()
				.create("o") );
		options.addOption( OptionBuilder.withLongOpt( "outputfile" )
				.withDescription( "output list file" )
				.hasArg()
				.create("of") );
		options.addOption( OptionBuilder.withLongOpt( "outputhtml" )
				.withDescription( "output HTML list file" )
				.hasArg()
				.create("ofh") );
		options.addOption( OptionBuilder.withLongOpt( "agentname" )
				.withDescription( "user agent name" )
				.hasArg()
				.create("a") );
		options.addOption( OptionBuilder.withLongOpt( "threads" )
				.withDescription( "maximum number of fetcher threads to use" )
				.hasArg()
				.create("t") );		
		options.addOption( OptionBuilder.withLongOpt( "numloops" )
				.withDescription( "number of fetch/update loops" )
				.hasArg()
				.create("n") );		
		options.addOption( OptionBuilder.withLongOpt( "crawlduration" )
				.withDescription( "target crawl duration in minutes" )
				.hasArg()
				.create("c") );
		options.addOption( OptionBuilder.withLongOpt( "topic" )
				.withDescription( "Topic definition" )
				.hasArg()
				.create("tc") );
		options.addOption( OptionBuilder.withLongOpt( "language" )
				.withDescription( "Target language. If more than one, separate with ';', " +
						"i.e. en;el" )
						.hasArg()
						.create("lang") );
		options.addOption( OptionBuilder.withLongOpt( "config" )
				.withDescription( "XML file with configuration for the crawler." )
				.hasArg()
				.create("cfg") );
		options.addOption( OptionBuilder.withLongOpt( "keepboiler" )
				.withDescription( "Annotate boilerplate content in parsed text" )				
				.create("k") );
		options.addOption( OptionBuilder.withLongOpt( "image_fullpath" )
				.withDescription( "Keep image fullpath for pair detection" )				
				.create("ifp") );
		options.addOption( OptionBuilder.withLongOpt( "force" )
				.withDescription( "Force to start new crawl. " +
						"Caution: This will remove any previous crawl data (if they exist)." )				
						.create("f") );		
		options.addOption( OptionBuilder.withLongOpt( "help" )
				.withDescription( "Help" )
				.create("h") );
		options.addOption( OptionBuilder.withLongOpt( "length" )
				.withDescription( "Minimum number of tokens per text block" )	
				.hasArg()
				.create("len") );
		options.addOption( OptionBuilder.withLongOpt( "minlength" )
				.withDescription( "Minimum number of tokens in cleaned document" )	
				.hasArg()
				.create("mtlen") );
		options.addOption( OptionBuilder.withLongOpt( "type" )
				.withDescription( "Crawling for monolingual (m), parallel (p), comparable (q)" )	
				.hasArg()
				.create("type") );
		options.addOption( OptionBuilder.withLongOpt( "specialFilter" )
				.withDescription( "Use this special filter for filter urls in order to stay in sub webdomains." )	
				.hasArg()
				.create("filter") );
		options.addOption( OptionBuilder.withLongOpt( "language1" )
				.withDescription( "Target language1.")
				.hasArg()
				.create("l1") );
		options.addOption( OptionBuilder.withLongOpt( "language2" )
				.withDescription( "Target language2.")
				.hasArg()
				.create("l2") );
		options.addOption( OptionBuilder.withLongOpt( "xslt" )
				.withDescription( "Insert a stylesheet for rendering xml results as html.")
				.create("xslt") );
		options.addOption( OptionBuilder.withLongOpt( "offlineXslt" )
				.withDescription( "Apply an xsl transformation to generate html files during exporting.")
				.create("oxslt") );
		options.addOption( OptionBuilder.withLongOpt( "destination" )
				.withDescription( "Destination.")
				.hasArg()
				.create("dest") );

		return options;
	}

	public  void parseOptions( String[] args) {
		// create the command line parser
		CommandLineParser clParser = new GnuParser();
		try {
			CommandLine line = clParser.parse( options, args );

			if(line.hasOption( "h")) {
				help();
			}
			if(line.hasOption( "a")) {
				_agentName = line.getOptionValue("a");
			}			
			else help();

			if (line.hasOption( "dest")) {
				ws_dir = line.getOptionValue("dest")+fs;
			}else{
				String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
				//System.out.println(timeStamp );
				ws_dir=_agentName+"_"+timeStamp+fs;
			}
			//vpapa changed the use of the option -d
			/*if(line.hasOption( "d")) {
				_domain = line.getOptionValue("d");
				if (_domain.startsWith("http")) {
					LOGGER.error("The target domain should be specified as just the host, without the http protocol: " + _domain);
					//printUsageAndExit(parser);
					help();
				}
			}*/
			if (line.hasOption("u")) {
				_urls = line.getOptionValue("u");
				File f = new File(_urls);
				if (f.exists()==false){
					LOGGER.error("The seed file does not exist.");
					help();
				}
			}

			if(line.hasOption( "dbg")) {
				_debug = true;
			}
			if(line.hasOption( "l")) {
				_loggingAppender = line.getOptionValue("l");
			}
			if(line.hasOption( "o")) {
				File of = new File(line.getOptionValue("o"));
				_outputDir = of.getAbsolutePath();
				_outputFile = _outputDir + System.getProperty("file.separator") + "outputlist.txt";	
				_outputFileHTML = _outputDir + System.getProperty("file.separator") + "outputlist.txt.html";
			} else if (line.hasOption( "of")) {
				File of = new File(line.getOptionValue("of"));
				//System.out.println("of: "+of.getAbsolutePath());
				//_outputFile = of.getAbsolutePath();
				//File outputDir = new File("/var/lib/tomcat6/webapps/soaplab2-results/" + UUID.randomUUID().toString());
				File outputDir = new File(ws_dir + UUID.randomUUID().toString());

				_outputDir = outputDir.getAbsolutePath();
				//System.out.println("_outputDir: "+_outputDir);
				//_outputFile = outputDir.getAbsolutePath()+System.getProperty("file.separator")+of.getName();
				//_outputFile = "/var/lib/tomcat6/webapps/soaplab2-results/"+of.getName();
				//_outputFile = ws_dir+of.getName();
				_outputFile = of.getAbsolutePath();
				//help();
				if (line.hasOption( "ofh")){
					//_outputFileHTML = of.getAbsolutePath()+".html";
					File ofh = new File(line.getOptionValue("ofh"));
					_outputFileHTML = ofh.getAbsolutePath();
				}
				else
					_outputFileHTML=null;
			}
			else help();

			if(line.hasOption( "t")) {
				_threads = Integer.parseInt(line.getOptionValue("t"));
			}			
			if(line.hasOption( "n")) {
				_numLoops = Integer.parseInt(line.getOptionValue("n"));
				_crawlDuration=0;
			}						
			if(line.hasOption("c")) {
				_crawlDuration = Integer.parseInt(line.getOptionValue("c"));
			}			
			if(line.hasOption( "tc")) {
				_topic = line.getOptionValue("tc");
			}	
			if(line.hasOption( "dom")) {
				if (_topic==null){
					LOGGER.error("The targeted domain is defined but " +
							"a topic definition is not applied. " +
							"Regarding Topic definition and targeted domain," +
							" you should either define both or none of them.");
					help();
				}else
					_descr = line.getOptionValue("dom");
			}else{
				if (_topic!=null){
					LOGGER.error("Even though a topic definition is applied " +
							"the targeted domain is not defined. "+
							"Regarding Topic definition and targeted domain," +
							"you should either define both or none of them.");
					help();
				}
			}

			if(line.hasOption( "lang")) {
				_language = line.getOptionValue("lang");
				String[] langs=_language.split(";");
				if (langs.length>2){
					LOGGER.error("The targeted languages are more than 2.");
					help();
				}
				if (langs.length==2){
					if (langs[0].equals(langs[1])){
						LOGGER.error("The targeted languages are the same:"+langs[0]);
						help();
					}
				}
				//vpapa
				_langKeys = findKeys4lang(_language);
			}else{
				//vpapa for bilingual web service
				if(line.hasOption("l1") & line.hasOption("l2")) {
					_language = line.getOptionValue("l1")+";"+line.getOptionValue("l2");
					_langKeys = findKeys4lang(_language);
				}else{
					LOGGER.error("Only 1 language has been defined.");
				}
			}

			if(line.hasOption( "cfg")) {
				_config = line.getOptionValue("cfg");
			}		
			if(line.hasOption( "k")) {
				_keepBoiler  = true;
			}
			if(line.hasOption( "ifp")) {
				_keepimagefp  = true;
			}
			if(line.hasOption( "f")) {
				_force   = true;
			}
			if(line.hasOption( "len")) {
				_length = Integer.parseInt(line.getOptionValue("len"));
			} 
			if(line.hasOption( "mtlen")) {
				_minTokensNumber = Integer.parseInt(line.getOptionValue("mtlen"));
			} 
			
			if(line.hasOption( "xslt")) 
				_cesAlign  = true;
			else	
				_cesAlign  = false;

			if(line.hasOption( "oxslt")) {
				_cesAlign  = false;
				offlineXSLT = true;
			}

			if(line.hasOption( "type")) {
				_type = line.getOptionValue("type");
				if ((_type.equals("p") | _type.equals("q"))& !_language.contains(";")){
					LOGGER.error("You crawl for parallel or comparable but only 1 language has been defined.");
					//printUsageAndExit(parser);
					help();
				}
				if (_type.equals("p")){
					URL url;
					try {
						//String temp=Bitexts.readFileAsString(_urls);//temp.split("\n");
						ArrayList<String> seed_list =new ArrayList<String>();
						BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream(_urls),"utf8"));
						String cur_line="";
						while ((cur_line=rdr.readLine())!=null){
							if (skipLineM.reset(cur_line).matches()) 
								continue;
							seed_list.add(cur_line);
						}
						if (!seed_list.isEmpty()){
							String[] seeds=new String[seed_list.size()];
							for (int ii=0;ii<seeds.length;ii++)
								seeds[ii]=seed_list.get(ii);
							if (seeds.length==1){
								try {
									url = new URL(seeds[0]);
									String host = url.getHost();
									if (host.substring(0, 3).equals("www"))
										host=host.substring(4);
									String mainhost=processhost(host);
									if (mainhost.substring(0, 3).equals("www"))
										mainhost=host.substring(4);
									_domain=host;
									_maindomain=mainhost;
									LOGGER.info(_domain);
									LOGGER.info(_maindomain);
								} catch (MalformedURLException e) {
									LOGGER.error("Seed URL is not valid: "+seeds[0]);
									help();
								}
							}else{
								String firsthost="";
								for (int ii=0;ii<seeds.length;ii++){
									try {
										if (ii==0)
											firsthost = new URL(seeds[ii]).getHost();
										url = new URL(seeds[ii]);
										String host = url.getHost();
										if (!host.equals(firsthost)){
											System.out.println("Since the provided seed list for bilingual crawling includes more than on webdomains, " +
													" USE the filter argument to confine FC within these webdomains.");
											if (!line.hasOption( "filter"))
												System.exit(0);				
											else{
												_domain=null;
												_maindomain=null;
											}
										}else{
											if (host.substring(0, 3).equals("www"))
												host=host.substring(4);
											String mainhost=processhost(host);
											if (mainhost.substring(0, 3).equals("www"))
												mainhost=host.substring(4);
											_domain=host;
											_maindomain=mainhost;
											LOGGER.info(_domain);
											LOGGER.info(_maindomain);
										}
									}catch (MalformedURLException e) {
										LOGGER.error("Seed URL is not valid:"+seeds[ii]);
										//help();
										//e.printStackTrace();
									}		
								} 
							}
						}else{
							LOGGER.error("There is no valid seed URL.");
							help();
						}
					} catch (IOException e) {
						LOGGER.error("The seed URL file does not exist.");
						help();
						//e.printStackTrace();
					}
				}
			}else{
				if (_language.contains(";")){
					LOGGER.error("You crawl for 2 languages but the type of outcome (parallel or comparable) has not been defined.");
					//printUsageAndExit(parser);
					help();
				}
			}
			if (line.hasOption( "filter")) {
				_filter = line.getOptionValue("filter");
			} 

			if(line.hasOption( "d")) {
				if (_type.equals("m")){
					if (_language.contains(";")){
						LOGGER.error("Choose only one target language for monolingual crawl.");
						//printUsageAndExit(parser);
						help();
					}else{
						URL url;
						try {
							String temp=Bitexts.readFileAsString(_urls);
							url = new URL(temp);
							String host = url.getHost();
							if (host.substring(0, 3).equals("www")){
								host=host.substring(4);
							}
							String mainhost=processhost(host);
							if (mainhost.substring(0, 3).equals("www")){
								mainhost=host.substring(4);
							}
							_domain=host;
							_maindomain=mainhost;
							//_domain="."+host;
							//_maindomain="."+mainhost;
							System.out.println(_domain);
							System.out.println(_maindomain);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}else{
					LOGGER.error("option d is used only for monolingual crawling");
					help();
				}
			}
		} catch( ParseException exp ) {
			// oops, something went wrong
			System.err.println( "Parsing options failed.  Reason: " + exp.getMessage() );			
			System.exit(64);
		}
	}
	private String processhost(String host) {
		String finalhost="";
		InternetDomainName domainname = InternetDomainName.from(host);
		InternetDomainName ps = domainname.publicSuffix();
		int ind1=0, ind2=0;
		String temp1 = "";
		while (ind1>-1){
			temp1 = domainname.name().substring(ind2);
			ind1=temp1.indexOf("."+ps.name());
			ind2=ind2+ind1+1;
		}
		InternetDomainName domainname1 = InternetDomainName.from(domainname.name().substring(0, ind2));
		ImmutableList<String> domainparts=domainname1.parts();
		for (int kk=0;kk<domainparts.size();kk++){
			InternetDomainName temp = InternetDomainName.from(domainparts.get(kk));
			if (!temp.isPublicSuffix()){
				finalhost=finalhost+domainparts.get(kk)+".";
			}
		}
		//return finalhost.substring(0, finalhost.length()-1);
		return finalhost;
	}
	//vpapa
	private String[] findKeys4lang(String language) {
		ArrayList<String> langKeys=new ArrayList<String>();
		String[] langs = _language.split(";");
		//File langfilepath=new File("conf/langKeys.txt");
		//URL svURL = ReadResources.class.getClassLoader().getResource("langKeys.txt");
		//BufferedReader in = new BufferedReader(new InputStreamReader(svURL.openStream()));
		//String inputLine;
		//File langfilepath=new File("langKeys.txt");
		//if (!langfilepath.exists()){
		//	System.out.println("The file for langKeys does not exist.");
		//}
		//else {
		try {
			//BufferedReader in = new BufferedReader(new FileReader(langfilepath));
			URL svURL = ReadResources.class.getClassLoader().getResource("langKeys.txt");
			BufferedReader in = new BufferedReader(new InputStreamReader(svURL.openStream()));
			String str, b;
			while ((str = in.readLine()) != null) {
				b=str.subSequence(0, str.indexOf(">")).toString();
				for (String lang:langs){
					if (b.equals(lang)){
						langKeys.add(str.subSequence(str.indexOf(">")+1, str.length()).toString());
					}
				}
				if (langKeys.size()==langs.length)
					break;
			}
			in.close();
		} catch (IOException e) {
			System.out.println("Problem in reading the file for langKeys.");
		}
		//}
		String[] result=new String[langKeys.size()];
		for (int ii=0;ii<langKeys.size();ii++)
			result[ii]=langKeys.get(ii);

		return result;
	}



	public  void help(){
		printHelp( APPNAME , options );
		System.exit(0);
	}
	public  void printHelp(String program, Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( program, options );
	}
	public String getLanguage() { 
		return _language;
	}

	public String[] getLangKeys() {
		return _langKeys;
	}
	public String getDest() {
		return ws_dir;
	}

	public String getTopic() {
		return _topic;
	}
	public  String getDomain() {
		return _domain;
	}
	public  String getMainDomain() {
		return _maindomain;
	}
	public  boolean isDebug() {
		return _debug;
	}
	public  String getLoggingAppender() {
		return _loggingAppender;
	}
	public  String getOutputDir() {
		return _outputDir;
	}
	public  String getOutputFile() {
		return _outputFile;
	}
	public  String getOutputFileHTML() {
		return _outputFileHTML;
	}
	public  String getAgentName() {
		return _agentName;
	}
	public  int getThreads() {
		return _threads;
	}
	public  int getNumLoops() {
		return _numLoops;
	}
	public  int getCrawlDuration() {
		return _crawlDuration;
	}
	public String getUrls() {
		return _urls;
	}
	public boolean keepBoiler() {
		return _keepBoiler;
	}
	public boolean Force() {
		return _force;
	}
	public String getConfig(){
		return _config;
	}

	public int getlength() {
		return _length;
	}

	public int getTokensNumber() {
		return _minTokensNumber;
	}

	public String getType() {
		return _type;
	}
	public String getTargetedDomain() {
		return _descr;
	}
	public String getFilter() {
		return _filter;
	}
	public boolean getAlign() {
		return _cesAlign;
	}
	public String getDesc() {
		return _descr;
	}
	public boolean getImpath() {
		return _keepimagefp;
	}
	public boolean isOfflineXSLT() {
		return offlineXSLT;
	}
	public void setOfflineXSLT(boolean offlineXSLT) {
		this.offlineXSLT = offlineXSLT;
	}
	public int getminTokenslength() {
		return _minTokensNumber;
	}

}
