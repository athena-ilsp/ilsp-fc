/**
 * 
 */
package gr.ilsp.fmc.langdetect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LangDetectorFactory {

	private static final Logger logger = LoggerFactory.getLogger(LangDetectorFactory.class);

	private String[] langDetectorIds = {
		"langdetect", 
		"tika",
		"langid"};

	public LangDetector getLangDetector(String langDetector) {

		if (langDetector.equalsIgnoreCase("langdetect")) {
			return new CybozuLangDetector();
		} else if (langDetector.equalsIgnoreCase("langid")) {
			return new LangIdDetector();
		} else if (langDetector.equalsIgnoreCase("tika")) {
			return new TikaLangDetector();
		} else {
			logger.warn("LangDetector " + langDetector + " not among known langDetectors: " + langDetectorIds);
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
		LangDetector langDetector = langDetectorFactory.getLangDetector("langdetect"); // or tika or langid
		langDetector.initialize();
		logger.info(langDetector.detect("Μια πρόταση στα ελληνικά."));
	}

}
