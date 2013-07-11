/**
 * 
 */
package gr.ilsp.fmc.langdetect;

import java.io.File;
import java.util.HashMap;

import org.apache.tika.language.LanguageIdentifier;
import org.apache.tika.language.LanguageProfile;

public class TikaLangDetector extends LangDetector {
	
	/* (non-Javadoc)
	 * @see gr.ilsp.fmc.langdetect.LangDetector#initialize()
	 */
	@Override
	protected void initialize() {     
		LanguageIdentifier.initProfiles();
	}

	/* (non-Javadoc)
	 * @see gr.ilsp.fmc.langdetect.LangDetector#detect(java.lang.String)
	 */
	@Override
	protected String detect(String text) {
		return new LanguageIdentifier(new LanguageProfile(text)).getLanguage();
	}

	@Override
	protected HashMap<String, Double> detectLangs(String text) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void createNewLanguageProfile(String lang, File trainFile,
			File profileFile) throws Exception {
		// TODO Auto-generated method stub
		
	}



}
