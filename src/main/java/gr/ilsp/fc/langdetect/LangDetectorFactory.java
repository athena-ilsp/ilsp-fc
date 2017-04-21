/**
 * 
 */
package gr.ilsp.fc.langdetect;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LangDetectorFactory {


	private static final Logger logger = LoggerFactory.getLogger(LangDetectorFactory.class);

	private String[] langDetectorIds = {
		"langdetect",  
		"bs-hr-sr-nb", // naivebayes classifier only for bs-hr-sr
		"tika",
		"langid"};

	public LangDetector getLangDetector(String langDetector) {

		if (langDetector.equalsIgnoreCase("langdetect")) {
			return new CybozuLangDetector();
		} else if (langDetector.equalsIgnoreCase("langid")) {
			return new LangIdDetector();
		} else if (langDetector.equalsIgnoreCase("tika")) {
			return new TikaLangDetector();
		} else if (langDetector.equalsIgnoreCase("bs-hr-sr-nb")) {	
			return new NaiveBayesClassifier();
		} else {
			logger.warn("LangDetector " + langDetector + " not among known langDetectors: " + Arrays.toString(langDetectorIds));
			logger.warn("Using default langDetector langdetect");
			return new CybozuLangDetector();
		}
	}

	/**
	 * @return the langDetectorIds
	 */
	public String[] getLangDetectorIds() {
		return langDetectorIds;
	}

	/**
	 * @param langDetectorIds the langDetectorIds to set
	 */
	public void setLangDetectorIds(String[] langDetectorIds) {
		this.langDetectorIds = langDetectorIds;
	}
	
	public static void main(String[] args) throws Exception {
		LangDetectorFactory langDetectorFactory = new LangDetectorFactory();
		LangDetector langDetector = langDetectorFactory.getLangDetector("bs-hr-sr-nb"); // or tika or langid or bs-hr-sr-nb or langdetect
		langDetector.initialize();
		// logger.info(langDetector.detect("Μια πρόταση στα ελληνικά."));		
		logger.info(langDetector.detect(croatianSent));
		logger.info(langDetector.detect(bosnianSent));
	}

	private static String croatianSent = 
			"Dok se turski premijer Recep Tayyip Erdogan suočavao s masovnim prosvjedima u gradovima diljem zemlje, " +
			"ljudi u nekoliko balkanskih država iskazali su svoju potporu premijeru." +
			"";
	private static String bosnianSent = 
			"Dok se premijer Recep Tayyip Erdogan suočavao s masovnim protestima u gradovima širom zemlje, " +
			"ljudi u nekoliko balkanskih zemalja izašli su da pokažu svoju podršku premijeru." +			
			"";


}
