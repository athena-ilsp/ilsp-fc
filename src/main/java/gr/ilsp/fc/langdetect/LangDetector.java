/**
 * 
 */
package gr.ilsp.fc.langdetect;

import java.io.File;
import java.util.HashMap;


/**
 * @author prokopis
 *
 */

public abstract class LangDetector {

	/**
	 * @param token
	 * @return
	 * @throws Exception 
	 */
	public abstract void initialize() throws Exception;

	/**
	 * @param text
	 * @return ISO-639-3 code representing the language or null
	 */
	public abstract String detect(String text);

	public abstract HashMap<String, Double> detectLangs(String text) throws Exception ;

	public abstract void createNewLanguageProfile(String lang, File trainFile, File profileFile) throws Exception ;


}
