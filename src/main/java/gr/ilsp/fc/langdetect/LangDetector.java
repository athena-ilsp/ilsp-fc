/**
 * 
 */
package gr.ilsp.fc.langdetect;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


/**
 * @author prokopis
 *
 */
public abstract class LangDetector implements Serializable {
	
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
	 * @return ISO-639-3 code representing the language or null
	 */
	public abstract String detect(String text);

	public abstract HashMap<String, Double> detectLangs(String text) throws Exception ;

	public abstract void createNewLanguageProfile(String lang, File trainFile, File profileFile) throws Exception ;
	
	/**
	 * @param text
	 * @param lang
	 * @return a new lang for this string, if a better language detector has been initialized and declared for this language
	 */
	public String detect(String text, String lang) {
		if (langDetectorsMap.containsKey(lang)) {
			return langDetectorsMap.get(lang).detect(text);
		}
		return lang;
	}

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
