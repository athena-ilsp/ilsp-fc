package gr.ilsp.fc.langdetect;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.ilsp.fc.utils.AnalyzerTest;

public class LangDetectorFactoryTest {
	
	private static final Logger logger = LoggerFactory.getLogger(LangDetectorFactoryTest.class);
	private static HashSet<String> langDetectorIds;

	static String langDetectorStr = "langdetect";
	static LangDetectorFactory langDetectorFactory ;
	static LangDetector langDetector;
	
	@BeforeClass
	public static void setUp() throws Exception {
		
		// for testGetLangDetector
		langDetectorIds = new HashSet<String>();
		langDetectorIds.add("langdetect");
		langDetectorIds.add("bs-hr-sr-nb");
//		langDetectorIds.add("tika");
//		langDetectorIds.add("langid");
		
		
		// for testLangDetector
		langDetectorFactory = new LangDetectorFactory();
		langDetector = langDetectorFactory.getLangDetector(langDetectorStr);
		langDetector.initialize();
	}

	@Test
	/**
	 * Actually not a test, but a method to illustrate use.
	 * 
	 * @param args
	 * @throws Exception 
	 */
	public void testGetLangDetector() throws Exception {
		
		HashMap<String, String> fileNamesHM = new HashMap<String, String>();
		fileNamesHM.put("politika.srp.txt", "srp");
		fileNamesHM.put("vecernji.hrv.txt", "hrv");
		fileNamesHM.put("dnevniavaz.bos.txt", "bos");
		Set<String> fileNames = fileNamesHM.keySet();

		LangDetectorFactory langDetectorFactory = new LangDetectorFactory();
		for (String langDetectorId : langDetectorIds) {
			logger.info("Loading LangDetector " + langDetectorId );
			LangDetector loopLangDetector;
			if (langDetectorId.contains("langdetect")) {
				loopLangDetector = langDetector; 
			} else {
				loopLangDetector = langDetectorFactory.getLangDetector(langDetectorId);
				loopLangDetector.initialize();
			}
			for (String fileName : fileNames) {
				String goldLang = fileNamesHM.get(fileName);
				int hits = 0;
				InputStream in  =  LangDetectorFactory.class.getResource("/rs_ba_hr_corpus/"+fileName).openStream();
				try {
					List<String> lines =  IOUtils.readLines(in, "UTF-8");
					HashMap<String, Integer> misses = new HashMap<String, Integer>();
					for (Iterator<String> iterator = lines.iterator(); iterator.hasNext();) {
						String line =  iterator.next();
						String autoLang = loopLangDetector.detect(line);
						if ( autoLang.equalsIgnoreCase(goldLang) ) {
							hits++;
						} else if (misses.containsKey(autoLang)) {
							misses.put(autoLang, misses.get(autoLang) + 1);
						} else {
							misses.put(autoLang, 1);
						}
						//langDetector.detectLangs(line);
					}
					logger.info(langDetectorId + ":" + fileName + ":" + goldLang + ":" + ((float) hits*100/lines.size()));
					logger.info("Misses are: "+misses);
				} finally {
					IOUtils.closeQuietly(in);
				}
			}
			System.out.println();
		}
	}

	

	private void testLangDetector(String input, String langcode, String testName) {
		try {
			logger.info("Expecting " + langcode  + " for " + input);
			Assert.assertEquals(testName + ": ", langcode, langDetector.detect(input));
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void testLangDetector() throws Exception {
		String testFile = "/lang-detection/tests.txt";

		logger.info("Started testing language detector" );
		InputStream in  =  AnalyzerTest.class.getResource(testFile).openStream();
		for (String line:  IOUtils.readLines(in, "UTF-8")) {
			if (StringUtils.isAnyBlank(line) || line.startsWith("#")) {
				continue;
			} else {
				String[] fields = StringUtils.split(line, "\t");
				//logger.info(line);
				testLangDetector(fields[1], fields[0], fields[0]+ " test");
			}
		}
	}
	
}
