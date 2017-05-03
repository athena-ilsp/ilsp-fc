/**
 * 
 */
package gr.ilsp.fc.langdetect;

import gr.ilsp.fc.utils.DirUtils;
import gr.ilsp.fc.utils.ISOLangCodes;
import gr.ilsp.fc.utils.JarUtils;
import gr.ilsp.nlp.commons.Constants;

import java.io.File;
import java.net.URL;
import java.util.HashMap;

import net.arnx.jsonic.JSON;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.GenProfile;
import com.cybozu.labs.langdetect.LangDetectException;
import com.cybozu.labs.langdetect.util.LangProfile;

public class CybozuLangDetector extends LangDetector {

	/**
	 * 
	 */
	private static final long serialVersionUID = -466144670548859084L;
	private static final Logger logger = LoggerFactory.getLogger(CybozuLangDetector.class);

	/* (non-Javadoc)
	 * @see gr.ilsp.fc.langdetect.LangDetector#initialize()
	 */
	@Override
	public void initialize() throws Exception {
		try {
			URL urldir = CybozuLangDetector.class.getResource("/profiles");
			File resdir = null;
			if (urldir.getProtocol()=="jar"){
				File tempDir = DirUtils.createTempDir();
				tempDir.deleteOnExit();
				JarUtils.copyResourcesRecursively(urldir, tempDir);
				resdir=tempDir;
			} else {
				resdir=new File(urldir.toURI());
			}	
			logger.debug("tempdir is " + resdir);
			DetectorFactory.loadProfile(resdir);
			
		} catch (LangDetectException e) {
			logger.error("Cannot initialize language identifier." );
			throw new Exception(e.getMessage());			
		}
	}

	/* (non-Javadoc)
	 * @see gr.ilsp.fc.langdetect.LangDetector#detect(java.lang.String)
	 */
	@Override
	protected String detectLang(String text) {
		try {
			Detector detector = DetectorFactory.create();
			detector.append(text);
			return ISOLangCodes.get3LetterCode(detector.detect());
		} catch (LangDetectException e) {
			logger.debug("Cannot detect language in " + text);
		}
		return null;
	}

	@Override
	public HashMap<String, Double> detectLangs(String text) throws Exception {
		Detector detector = DetectorFactory.create();
		detector.append(text);
		logger.warn(detector.getProbabilities()+"");
		return null;
	}

	@Override
	public void createNewLanguageProfile(String lang, File trainFile, File profileFile) throws Exception {
		LangProfile langProfile = GenProfile.loadFromText(lang, trainFile);
		String jsonProfile = JSON.encode(langProfile);
		FileUtils.writeStringToFile(profileFile, jsonProfile);
	}
	
	
	public static void main(String[] args) throws Exception {
		String lang = args[0];
		File trainFile = new File(args[1]);
		File profileFile = new File(args[2]);
		LangProfile langProfile = GenProfile.loadFromText(lang, trainFile);
		String jsonProfile = JSON.encode(langProfile);
		FileUtils.writeStringToFile(profileFile, jsonProfile);
	}

	
}
