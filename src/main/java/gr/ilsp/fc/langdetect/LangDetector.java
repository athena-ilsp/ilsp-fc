/**
 * 
 */
package gr.ilsp.fc.langdetect;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import gr.ilsp.nlp.commons.Constants;


/**
 * @author prokopis
 *
 */
public abstract class LangDetector implements Serializable {

	private static final int min_strlen=5;

	/**
	 * 
	 */
	private static final long serialVersionUID = -5579002462789690990L;

	/**
	 * @param token
	 * @return
	 * @throws Exception 
	 */
	public abstract void initialize() throws Exception;
	Map<String, LangDetector> langDetectorsMap = new HashMap<String, LangDetector>();

	
	/**
	 * @param text
	 * @return ISO-639-3 code representing the language or the empty string
	 */
	public String detect(String text) {
		String lang = Constants.EMPTY_STRING;
		if (text.length() < min_strlen)
			return lang;
		try {
			lang = detectLang(text); 
		} catch (Exception ex){
		}
		if (lang==null) 
			return Constants.EMPTY_STRING;
		return lang;
	}

	/**
	 * @param text
	 * @param lang
	 * @return a new lang for this string, if a better language detector has been initialized and declared for this language
	 */
	public String detect(String text, String lang) {
		if (text.length() < min_strlen) {
			return Constants.EMPTY_STRING;
		} else if (langDetectorsMap.containsKey(lang)) {
			lang = langDetectorsMap.get(lang).detect(text);
		}
		if (lang==null) 
			return Constants.EMPTY_STRING;
		return lang;
	}


	/*public String detectLangs1(String text) throws Exception {
		HashMap<String, Double> aa = new  HashMap<String, Double>();
		if (text.length() < min_strlen) 
			return "";
		aa = detectLangs(text);		
		Set<String> bb = aa.keySet();
		Iterator<String> cc = bb.iterator();
		
		return cc.next();
	}
	*/
	
	/**
	 * @param text
	 * @return ISO-639-3 code representing the language or null
	 */
	protected abstract String detectLang(String text);

	protected abstract HashMap<String, Double> detectLangs(String text) throws Exception ;
	

	protected abstract void createNewLanguageProfile(String lang, File trainFile, File profileFile) throws Exception ;


	/**
	 * @return the langDetectorsMap
	 */
	public Map<String, LangDetector> getLangDetectorsMap() {
		return langDetectorsMap;
	}

	/**
	 * @param langDetectorsMap the langDetectorsMap to set
	 */
	public void setLangDetectorsMap(Map<String, LangDetector> langDetectorsMap) {
		this.langDetectorsMap = langDetectorsMap;
	}

}
