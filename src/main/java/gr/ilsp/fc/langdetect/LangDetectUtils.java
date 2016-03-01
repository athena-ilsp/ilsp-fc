package gr.ilsp.fc.langdetect;

//import org.apache.tika.language.LanguageIdentifier;

import gr.ilsp.fc.main.Crawl;
import gr.ilsp.fc.utils.DirUtils;
import gr.ilsp.fc.utils.ISOLangCodes;
import gr.ilsp.fc.utils.JarUtils;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

public class LangDetectUtils {
	private static final Logger LOGGER = Logger.getLogger(LangDetectUtils.class);
	private static final String QUEST_SEPAR = ";";
	//private static final String LANG_CODES_RESOURCE = "langcode-langs.txt";
	//private static final String SPACE_SEPERATOR = " ";
	private static final int min_strlen=5;
	
	/**
	 * Detects language of the provided content with loaded Cybozu. 
	 * @param content
	 * @return
	 */
	public static String detectLanguage(String content) {
		String lang="";
		if (content.length()<min_strlen)
			return lang;
		//LanguageIdentifier LangI=new LanguageIdentifier(content); 
		//String langIdentified = LangI.getLanguage();
		Detector detector = null;			
		try {
			detector = DetectorFactory.create();
			detector.append(content);
			lang = ISOLangCodes.get3LetterCode(detector.detect());
		} catch (LangDetectException e) {
			//LOGGER.error(e.getMessage());
		}
		return lang;
	}
	
	
	/**
	 * compares language with the targeted languages
	 * @param lang
	 * @return
	 */
	public static boolean istargetedlang(String lang, String[] targeted_langs) {
		boolean match = false;
		for (String l:targeted_langs){
			if (lang.equals(l)){
				match = true;
				break;
			}
		}
		return match;
	}
	
	/**
	 * compares language with the targeted languages
	 * @param lang
	 * @return
	 */
	public static int idOfTargetedLang(String lang, String[] targeted_langs) {
		int ind=-1;
		for (int ii=0;ii<targeted_langs.length;ii++){
			if (lang.equals(targeted_langs[ii])){
				ind =ii;
			}
		}
		return ind;
	}
	
	
	
	/**
	 * Loads Cybozu Language Detector
	 */
	public static void loadCybozuLangIdentifier() {
		URL urldir = Crawl.class.getResource("/profiles");
		LOGGER.info("Loading language profiles for Cybozu language identifier");
		if (urldir.getProtocol()=="jar"){
			File tempDir = DirUtils.createTempDir();
			LOGGER.debug(tempDir );
			JarUtils.copyResourcesRecursively(urldir, tempDir);
			try {
				DetectorFactory.loadProfile(tempDir);				
			} catch (LangDetectException e1) {
				LOGGER.error(e1.getMessage());
			} 
		} else {
			try {
				DetectorFactory.loadProfile(new File(urldir.toURI()));
			} catch (LangDetectException e) {
				LOGGER.error(e.getMessage());
			} catch (URISyntaxException e) {
				LOGGER.error(e.getMessage());
			}
		}
	}
	
	
	/**
	 * checks languages provided by the user (i.e. lang codes separated by ;)
	 * returns unique targeted languages in 3digit LangCodeFormat
	 * @param languages
	 * @return
	 */
	public static String updateLanguages(String languages) {
		String targetlanguages="";
		String[] initlangs= languages.split(QUEST_SEPAR);
		List<String> langs = new ArrayList<String>();
		for (int ii=0;ii<initlangs.length;ii++) {
			String temp = ISOLangCodes.get3LetterCode(initlangs[ii].toLowerCase()); 
			if (!langs.contains(temp))
				langs.add(temp);
		}
		if (langs.isEmpty()){
			LOGGER.error("No languages have been defined.");
			System.exit(0);
		}
		for (int ii=0;ii<langs.size();ii++) {
			targetlanguages = targetlanguages+QUEST_SEPAR+langs.get(ii);
		}
		targetlanguages=targetlanguages.substring(1);
		return targetlanguages;
	}
	
	
	/*//LanguageIdentifier initialization	
	if (loadProfile){
		URL urldir = Crawl.class.getResource("/profiles");
		LOGGER.debug(urldir );
		if (urldir.getProtocol()=="jar"){
			File tempDir = DirUtils.createTempDir();
			LOGGER.debug(tempDir );
			JarUtils.copyResourcesRecursively(urldir, tempDir);
			try {
				DetectorFactory.loadProfile(tempDir);				
			} catch (LangDetectException e1) {
				LOGGER.error(e1.getMessage());
			} 
		} else {
			try {
				DetectorFactory.loadProfile(new File(urldir.toURI()));
			} catch (LangDetectException e) {
				LOGGER.error(e.getMessage());
			} catch (URISyntaxException e) {
				LOGGER.error(e.getMessage());
			}

		}
	}*/
	
/*	
	*//**
	 * given the HashMap langcodes (key is the 2-iso langcode, value is the 3-iso langcode),
	 * returns the 2-letter or 3-letter language code (if a 3-letter or 2-letter language code is provided)   
	 * @param langcode1
	 * @return
	 *//*
	public static String convertlangCodes(String langcode, HashMap<String,String> langcodes) {
		if (langcode.length()==2){
			if (langcodes.containsKey(langcode)){
				return langcodes.get(langcode);
			}
		}
		Set<String> lang2codes = langcodes.keySet();
		Iterator<String> langit = lang2codes.iterator();
		String lang = null;
		while (langit.hasNext()){
			lang = langit.next();
			if (langcodes.get(lang).equals(langcode)){
				break;
			}
		}
		return lang;
	}
	

	
	*//**
	 * parses the predefined project resource LANG_CODES_RESOURCE and 
	 * returns an array with the 2-letter or 3-letter language code    
	 * @param langcode1
	 * @return
	 *//*
	public static HashMap<String,String> getlangCodes() {
		HashMap<String, String> langcodes = new HashMap<String,String>();
		try {
			URL svURL = ReadResources.class.getClassLoader().getResource(LANG_CODES_RESOURCE);
			BufferedReader in = new BufferedReader(new InputStreamReader(svURL.openStream()));
			String str;
			String[] b = new String[3];
			while ((str = in.readLine()) != null) {
				b=str.split(SPACE_SEPERATOR);
				if (b[0].length()==2 && b[1].length()==3){
					if (!langcodes.containsKey(b[0])){
						langcodes.put(b[0], b[1]);
					}
				}else if (b[0].length()==3 && b[1].length()==2){
					if (!langcodes.containsKey(b[1])){
						langcodes.put(b[1], b[0]);
					}
				}else{
					LOGGER.warn("Problem in reading the file for "+LANG_CODES_RESOURCE + " . the first 2 columns should be for 2-let and 3-let language codes.");
				}
			}
			in.close();
		} catch (IOException e) {
			LOGGER.error("Problem in reading the file for "+LANG_CODES_RESOURCE);
		}		
		return langcodes;
	}*/
	
	
}
