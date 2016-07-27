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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
		for (String l:targeted_langs){
			if (lang.equals(l)){
				return true;
			}
		}
		return false;
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
	 * returns unique targeted languages in 3digit (if iso6393 is true)
	 * or 2digit (if iso6393 is false) LangCodeFormat 
	 * @param languages
	 * @return
	 */
	public static String updateLanguages(String languages, boolean iso6393) {
		String targetlanguages="";
		String[] initlangs= languages.split(QUEST_SEPAR);
		List<String> langs = new ArrayList<String>();
		String temp="";
		for (int ii=0;ii<initlangs.length;ii++) {
			if (iso6393)
				temp = ISOLangCodes.get3LetterCode(initlangs[ii].toLowerCase());
			else
				temp = ISOLangCodes.get2LetterCode(initlangs[ii].toLowerCase());
			if (!langs.contains(temp))
				langs.add(temp);
		}
		if (langs.isEmpty()){
			LOGGER.error("No languages have been defined.");
			System.exit(0);
		}
		Collections.sort(langs);
		for (int ii=0;ii<langs.size();ii++) {
			targetlanguages = targetlanguages+QUEST_SEPAR+langs.get(ii);
		}
		targetlanguages=targetlanguages.substring(1);
		return targetlanguages;
	}

	public static String getlangCodeFromLangkeys(	HashMap<String, String> _maplangs, String langkey) {
		String langcode = "", key;
		Set<String> langcodes = _maplangs.keySet();
		Iterator<String> langcodes_it = langcodes.iterator();
		while (langcodes_it.hasNext()){									
			key = langcodes_it.next();
			if (_maplangs.get(key).contains(langkey)){
				return key;
			}
		}
		return langcode;
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


}
