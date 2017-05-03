/**
 * 
 */
package gr.ilsp.fc.langdetect;

import java.io.File;
import java.util.HashMap;

import com.carrotsearch.labs.langid.DetectedLanguage;
import com.carrotsearch.labs.langid.LangIdV3;

import gr.ilsp.fc.utils.ISOLangCodes;


public class LangIdDetector extends LangDetector {

	/**
	 * 
	 */
	private static final long serialVersionUID = -973948477521477985L;
	LangIdV3 langIdV3;
	
	/* (non-Javadoc)
	 * @see gr.ilsp.fc.langdetect.LangDetector#initialize()
	 */
	@Override
	public void initialize() {
		 langIdV3 = new LangIdV3();
	}

	/* (non-Javadoc)
	 * @see gr.ilsp.fc.langdetect.LangDetector#detect(java.lang.String)
	 */
	@Override
	public String detectLang(String text) {
		DetectedLanguage result = langIdV3.classify(text, true);
		return ISOLangCodes.get3LetterCode(result.langCode);
	}

	@Override
	public HashMap<String, Double> detectLangs(String text) {
		return null;
	}

	@Override
	public void createNewLanguageProfile(String lang, File trainFile,
			File profileFile) throws Exception {
		// TODO Auto-generated method stub
		
	}
}
