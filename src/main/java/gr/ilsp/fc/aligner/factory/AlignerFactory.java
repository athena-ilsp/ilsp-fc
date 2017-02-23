package gr.ilsp.fc.aligner.factory;

import gr.ilsp.fc.crawl.Crawler;
import gr.ilsp.fc.utils.ISOLangCodes;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlignerFactory {

	private String[] alignerIds = {"hunalign","maligna"};
	private static final String DEFAULT_BI_CONFIG_FILE = "FBC_config.xml";
	private static final Logger LOGGER = LoggerFactory.getLogger(AlignerFactory.class);
	
	/**
	 * Loads the default configuration file and checks if user supplied a custom one.
	 * @param type
	 * @param confFile
	 * @return
	 */
	public static CompositeConfiguration getConfig( String confFile) {
		CompositeConfiguration configuration = new CompositeConfiguration();
		URL default_config = Crawler.class.getClassLoader().getResource(DEFAULT_BI_CONFIG_FILE);

		if (confFile!=null){
			String custom_config = confFile;
			try {
				XMLConfiguration xml_custom_config = new XMLConfiguration(custom_config);
				xml_custom_config.setValidating(true);
				configuration.addConfiguration(xml_custom_config);
			} catch (ConfigurationException e) {
				LOGGER.error("Invalid configuration file: " + custom_config);
			}
		}
		try {			
			configuration.addConfiguration(new XMLConfiguration(default_config));				
		} catch (ConfigurationException e1) {
			// Shouldn't happen
			LOGGER.error("Problem with default configuration file.");
		}
		return configuration;
	}
	
	
	public Aligner getAligner(String aligner) {
		if (aligner.equalsIgnoreCase("hunalign")) {
			return new HunalignAligner();
		} else if (aligner.equalsIgnoreCase("maligna")) {
			return new MalignaAligner();
		} else if (aligner.equalsIgnoreCase("default")) {
			return new MalignaAligner();
		} else {
			LOGGER.warn("Aligner " + aligner + " not among known aligners: " + Arrays.toString(alignerIds));
			LOGGER.warn("Using default aligner: " + MalignaAligner.class.getName());
			return new MalignaAligner();
		}
	}

	/*public static void main(String[] args) throws IOException, ParserConfigurationException {
		String alignerStr = args[0];
		String sourceLang = args[1];		
		String targetLang = args[2];		
		File cesAlignList = new File(args[3]);		
		File tmxList = new File(args[4]);		
		File tmxHtmlList = new File(args[5]);
		try {
			AlignerFactory alignerFactory = new AlignerFactory();
			Aligner aligner = alignerFactory.getAligner(alignerStr);
			Properties properties = new Properties();
			properties.setProperty("sourceLang", sourceLang);			
			properties.setProperty("targetLang", targetLang);			
			if (alignerStr.equalsIgnoreCase("hunalign")){
				properties.setProperty("aligner_runnable_path", args[6]);
				properties.setProperty("dictalign_path", args[7]);
			}
			aligner.initialize(properties);
			//aligner.processCesAlignList(cesAlignList, tmxList, tmxHtmlList,xslt);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		}
	}*/
	public static void main(String[] args) throws IOException, ParserConfigurationException {
		String alignerStr = args[0];
		String sourceLang = args[1];		
		String targetLang = args[2];		

		//File tmxList = new File(args[4]);		
		//File tmxHtmlList = new File(args[5]);
		try {
			AlignerFactory alignerFactory = new AlignerFactory();
			Aligner aligner = alignerFactory.getAligner(alignerStr);
			Properties properties = new Properties();
			properties.setProperty("sourceLang", sourceLang);			
			properties.setProperty("targetLang", targetLang);			
			if (alignerStr.equalsIgnoreCase("hunalign")){
				properties.setProperty("aligner_runnable_path", args[6]);
				properties.setProperty("dictalign_path", args[7]);
			}
			aligner.initialize(properties);
			List<Pair<File, File>> l1L2Files=new ArrayList<Pair<File, File>>();
			ArrayList<String> filePairs=IOtools.readFileToArray(args[3]);
			for(String pair:filePairs){
				String[] files=pair.split(" ");
				Pair<File, File> newPair= Pair.of(new File((new File(args[3]).getParent())+File.separator+files[0]), new File((new File(args[3]).getParent())+File.separator+files[1]));
				l1L2Files.add(newPair);
				
			}
			aligner.processL1L2Files(l1L2Files, sourceLang, targetLang);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		}
	}
	
	
	
	/**
	 * Loads Aligner, and checks segmenters for targeted languages, if there is no available aligner, returns null.
	 * Supported aligners are "maligna" (default) and "hunalign"
	 * @param configuration 
	 * @param toAlign 	aligner Name
	 * @param useDict	dictionary name
	 * @param pathDict	dictionary path
	 * @param langs		targeted languages
	 * @return
	 */
	public static Aligner prepareAligner(CompositeConfiguration configuration, String toAlign, String useDict, String pathDict, String[] langs) {
		AlignerFactory alignerFactory = new AlignerFactory();
		Aligner aligner = alignerFactory.getAligner(toAlign);
		if (aligner!= null) {
			Properties properties = new Properties();
			if (toAlign.matches("hunalign")){
				String hunpath=Aligner.getRunningJarPath();
				String prop=System.getProperty("os.name").toLowerCase();
				String aligner_runnable_path = null;
				if (prop.equals("linux")) 
					aligner_runnable_path = FilenameUtils.concat(hunpath, configuration.getString("aligner.lin_align_path.value"));
				else if(prop.startsWith("windows")) 
					aligner_runnable_path = FilenameUtils.concat(hunpath, configuration.getString("aligner.win_align_path.value"));
				
				String usedict = useDict;
				String dictalign_path=null;
				if (toAlign.equals("hunalign")){
					if (usedict!=null && !usedict.equals("default")) 
						dictalign_path=pathDict;
					else 
						dictalign_path = FilenameUtils.concat(hunpath, configuration.getString("aligner.align_dict.value"));
				} else 
					dictalign_path = pathDict;
				properties.setProperty("aligner_runnable_path", aligner_runnable_path);
				if (dictalign_path!=null) 
					properties.setProperty("dictalign_path", dictalign_path);
			}
			properties.setProperty("sourceLang", ISOLangCodes.get3LetterCode(langs[0]));
			properties.setProperty("targetLang", ISOLangCodes.get3LetterCode(langs[1]));
			try {
				aligner.initialize(properties);
			} catch (Exception ex) {
				//ex.printStackTrace();
				return null; 
			}
		}
		return aligner;
	}

	
}
