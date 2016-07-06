package gr.ilsp.fc.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ISOLangCodes {

	private static final Logger logger = LoggerFactory.getLogger(ISOLangCodes.class);
	private static final String CHINESE="zh-cn";
	
    private static final Map<String, String> iso2Toiso3;
    static {
        Map<String, String> tempMap = new HashMap<String, String>();
        for (String language: Locale.getISOLanguages()) {
    		Locale locale = new Locale(language);
	        tempMap.put(language, locale.getISO3Language());
        }
        iso2Toiso3 = Collections.unmodifiableMap(tempMap);
    }
    
    private static final Map<String, String> iso3Toiso2;
    static {
        Map<String, String> tempMap = new HashMap<String, String>();
        for (String language: Locale.getISOLanguages()) {
    		Locale locale = new Locale(language);
	        tempMap.put( locale.getISO3Language(), language);
        }
        iso3Toiso2 = Collections.unmodifiableMap(tempMap);
    }
    

	public static String get3LetterCode(String language) {
		if (language.equals(CHINESE))
			language="zh";
		if (iso2Toiso3.containsKey(language)) {
			return iso2Toiso3.get(language);
		} else {
			return language;
		}
	}
	
	public static String get2LetterCode(String language) {
		if (language.equals(CHINESE))
			language="zh";
		if (iso3Toiso2.containsKey(language)) {
			return iso3Toiso2.get(language);
		} else {
			return language;
		}
	}
	
	public static void main(String[] args) {
		List<String> languages = Arrays.asList(Locale.getISOLanguages());
		Map<String, Locale> localeMap = new HashMap<String, Locale>(languages.size());
		for (String language : languages) {
		    Locale locale = new Locale(language);
		    localeMap.put(locale.getISO3Language(), locale);
		    logger.info(language + "->" + locale.getISO3Language());
		}
	}

	public static List<String> get23LetterCodes(String[] languages) {
		List<String> langslist = new ArrayList<String>();
		for (int ii=0;ii<languages.length;ii++){
			langslist.add(languages[ii]);
			if (iso3Toiso2.containsKey(languages[ii])) {
				langslist.add(iso3Toiso2.get(languages[ii]));
			}
			if (iso2Toiso3.containsKey(languages[ii])) {
				langslist.add(iso2Toiso3.get(languages[ii]));
			}
		}
		return langslist;
	}
	
	public static List<String> get2LetterCodes(String[] languages) {
		List<String> langslist = new ArrayList<String>();
		for (int ii=0;ii<languages.length;ii++){
			if (iso3Toiso2.containsKey(languages[ii])) 
				langslist.add(iso3Toiso2.get(languages[ii]));
			else
				langslist.add(languages[ii]);
		}
		return langslist;
	}
	
	public static List<String> get3LetterCodes(String[] languages) {
		List<String> langslist = new ArrayList<String>();
		for (int ii=0;ii<languages.length;ii++){
			if (iso2Toiso3.containsKey(languages[ii])) 
				langslist.add(iso2Toiso3.get(languages[ii]));
			else
				langslist.add(languages[ii]);
		}
		return langslist;
	}
	
	
}
