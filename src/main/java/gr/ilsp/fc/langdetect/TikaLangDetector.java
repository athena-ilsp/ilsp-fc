/**
 * 
 */
package gr.ilsp.fc.langdetect;

import java.io.File;
import java.util.HashMap;

import org.apache.tika.language.LanguageIdentifier;
import org.apache.tika.language.LanguageProfile;

import gr.ilsp.fc.utils.ISOLangCodes;

public class TikaLangDetector extends LangDetector {
	
	/* (non-Javadoc)
	 * @see gr.ilsp.fc.langdetect.LangDetector#initialize()
	 */
	@Override
	public void initialize() {     
		LanguageIdentifier.initProfiles();
	}

	/* (non-Javadoc)
	 * @see gr.ilsp.fc.langdetect.LangDetector#detect(java.lang.String)
	 */
	@Override
	public String detect(String text) {
		return ISOLangCodes.get3LetterCode(new LanguageIdentifier(new LanguageProfile(text)).getLanguage());
	}

	@Override
	public HashMap<String, Double> detectLangs(String text) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void createNewLanguageProfile(String lang, File trainFile,
			File profileFile) throws Exception {
		// TODO Auto-generated method stub
		
	}



}
