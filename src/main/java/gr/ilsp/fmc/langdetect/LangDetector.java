/**
 * 
 */
package gr.ilsp.fmc.langdetect;

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
	protected abstract void initialize() throws Exception;

	/**
	 * @param text
	 * @return ISO-639 code representing the language or null
	 */
	protected abstract String detect(String text);

	protected abstract HashMap<String, Double> detectLangs(String text) throws Exception ;

	protected abstract void createNewLanguageProfile(String lang, File trainFile, File profileFile) throws Exception ;


}
