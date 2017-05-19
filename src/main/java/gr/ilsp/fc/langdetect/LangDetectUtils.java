package gr.ilsp.fc.langdetect;

//import org.apache.tika.language.LanguageIdentifier;

//import gr.ilsp.fc.main.Crawl;
import gr.ilsp.fc.main.Run;
import gr.ilsp.fc.utils.DirUtils;
import gr.ilsp.fc.utils.ISOLangCodes;
import gr.ilsp.fc.utils.JarUtils;
import gr.ilsp.nlp.commons.Constants;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
//import com.cybozu.labs.langdetect.Language;

public class LangDetectUtils {
	private static final Logger LOGGER = Logger.getLogger(LangDetectUtils.class);
	
	//private static final String LANG_CODES_RESOURCE = "langcode-langs.txt";
	//private static final String SPACE_SEPERATOR = Constants.SPACE;
	private static Set<String> langsTBFI = new HashSet<String>(Arrays.asList("bos", "hrv", "srp"));
	private static final int min_strlen=5;
	//private static final double min_prob=0.6;

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
			//for (int ii=0;ii<2;ii++){
			detector = DetectorFactory.create();
			detector.append(content);
				//ArrayList<Language> aaa = detector.getProbabilities();
				//System.out.println(aaa.get(0).prob +"\t"+aaa.get(0).lang);
				//if (aaa.get(0).prob>min_prob){
				//	lang = aaa.get(0).lang;
				//	break;
				//}
			//}
			//if (lang.isEmpty())
			lang = ISOLangCodes.get3LetterCode(detector.detect());
			//else
			//	lang = ISOLangCodes.get3LetterCode(lang);
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
			if (lang.equals(l))
				return true;
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
			if (lang.equals(targeted_langs[ii]))
				ind =ii;
		}
		return ind;
	}

	/**
	 * Loads Cybozu Language Detector
	 */
	public static void loadCybozuLangIdentifier() {
		URL urldir = Run.class.getResource("/profiles");
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
		String[] initlangs= languages.split(Constants.SEMICOLON);
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
			targetlanguages = targetlanguages+Constants.SEMICOLON+langs.get(ii);
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
		URL urldir = Crawler.class.getResource("/profiles");
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

	/**
	 * use one (default) or more language detectors and favoring one for particular languages
	 * @param targetedLangs
	 * @param id of the default language Detector
	 */
	public static LangDetector loadLangDetectors(String[] targetedLangs, String langdetectorid) {
		LangDetectorFactory langDetectorFactory = new LangDetectorFactory();
		LangDetector langDetector = langDetectorFactory.getLangDetector(langdetectorid);
		try {
			langDetector.initialize();
		} catch (Exception e1) {
			LOGGER.error("problem in loading LangDetector: "+ langdetectorid );
			e1.printStackTrace();
		}
		Set<String> crawlLangs = new HashSet<String>(Arrays.asList(targetedLangs));
		for (String crawlLang: crawlLangs) {
			if (langsTBFI.contains(crawlLang)) {
				Map<String, LangDetector> otherLangDetectorMap = new HashMap<String, LangDetector>(); 
				LangDetector otherLangDetector =  langDetectorFactory.getLangDetector("bs-hr-sr-nb");
				try {
					otherLangDetector.initialize();
				} catch (Exception e) {
					LOGGER.error("problem in loading LangDetector: "+ "bs-hr-sr-nb" );
					e.printStackTrace();
				}
				for (String lang: langsTBFI) {
					otherLangDetectorMap.put(lang, otherLangDetector);
				}
				langDetector.setLangDetectorsMap(otherLangDetectorMap);
				break;
			}
		} 
		return langDetector;
	}

}
