/**
 * 
 */
package gr.ilsp.fmc.langdetect;

import gr.ilsp.fmc.utils.DirUtils;
import gr.ilsp.fmc.utils.JarUtils;

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

	private static final Logger logger = LoggerFactory.getLogger(CybozuLangDetector.class);

	/* (non-Javadoc)
	 * @see gr.ilsp.fmc.langdetect.LangDetector#initialize()
	 */
	@Override
	protected void initialize() throws Exception {
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
	 * @see gr.ilsp.fmc.langdetect.LangDetector#detect(java.lang.String)
	 */
	@Override
	protected String detect(String text) {	
		try {
			Detector detector = DetectorFactory.create();
			detector.append(text);
			return detector.detect();
		} catch (LangDetectException e) {
			logger.info("Cannot detect language in " + text);
		}
		return null;
	}

	@Override
	protected HashMap<String, Double> detectLangs(String text) throws Exception {
		Detector detector = DetectorFactory.create();
		detector.append(text);
		logger.warn(detector.getProbabilities()+"");
		return null;
	}

	@Override
	protected void createNewLanguageProfile(String lang, File trainFile, File profileFile) throws Exception {
		LangProfile langProfile = GenProfile.loadFromText(lang, trainFile);
		String jsonProfile = JSON.encode(langProfile);
		FileUtils.writeStringToFile(profileFile, jsonProfile);
	}
	
}
