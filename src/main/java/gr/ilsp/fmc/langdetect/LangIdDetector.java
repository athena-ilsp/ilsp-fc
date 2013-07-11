/**
 * 
 */
package gr.ilsp.fmc.langdetect;

import java.io.File;
import java.util.HashMap;

import com.carrotsearch.labs.langid.DetectedLanguage;
import com.carrotsearch.labs.langid.LangIdV3;


public class LangIdDetector extends LangDetector {

	LangIdV3 langIdV3;
	
	/* (non-Javadoc)
	 * @see gr.ilsp.fmc.langdetect.LangDetector#initialize()
	 */
	@Override
	protected void initialize() {
		 langIdV3 = new LangIdV3();
	}

	/* (non-Javadoc)
	 * @see gr.ilsp.fmc.langdetect.LangDetector#detect(java.lang.String)
	 */
	@Override
	protected String detect(String text) {
		DetectedLanguage result = langIdV3.classify(text, true);
		return result.langCode;
	}

	@Override
	protected HashMap<String, Double> detectLangs(String text) {
		return null;
	}

	@Override
	protected void createNewLanguageProfile(String lang, File trainFile,
			File profileFile) throws Exception {
		// TODO Auto-generated method stub
		
	}
}
