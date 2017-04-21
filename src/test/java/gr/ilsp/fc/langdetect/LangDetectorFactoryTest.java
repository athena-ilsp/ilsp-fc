package gr.ilsp.fc.langdetect;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.icu.text.DecimalFormat;

import gr.ilsp.fc.utils.AnalyzerTest;
import gr.ilsp.nlp.commons.Constants;

public class LangDetectorFactoryTest {
	
	private static final Logger logger = LoggerFactory.getLogger(LangDetectorFactoryTest.class);
	private static Set<String> langDetectorIds;

	static String langDetectorStr = "langdetect";
	static LangDetectorFactory langDetectorFactory ;
	static LangDetector langDetector;
	private static DecimalFormat df2 = new DecimalFormat(".##");

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

	@Test
	/**
	 * Actually not a test, but a method to illustrate use of using two (or more) language detectors and favoring one for particular languages.
	 * 
	 * @param args
	 * @throws Exception 
	 */
	public void testGetLangDetectors() throws Exception {
		
		HashMap<String, String> fileNamesHM = new HashMap<String, String>();
		fileNamesHM.put("politika.srp.txt", "srp");
		fileNamesHM.put("vecernji.hrv.txt", "hrv");
		fileNamesHM.put("dnevniavaz.bos.txt", "bos");
		Set<String> fileNames = fileNamesHM.keySet();

		LangDetectorFactory langDetectorFactory = new LangDetectorFactory();
		String langDetectorId = "langdetect";
		LangDetector defaultLangDetector =  langDetectorFactory.getLangDetector(langDetectorId);
		
		// Initialize
		Set<String> crawlLangs = new HashSet<String>(Arrays.asList("hrv", "eng")); 
		Set<String> langsTBFI = new HashSet<String>(Arrays.asList("bos", "hrv", "srp")); 
		for (String crawlLang: crawlLangs) {
			if (langsTBFI.contains(crawlLang)) {
				Map<String, LangDetector> otherLangDetectorMap = new HashMap<String, LangDetector>(); 
				LangDetector otherLangDetector =  langDetectorFactory.getLangDetector("bs-hr-sr-nb");
				otherLangDetector.initialize();
				for (String lang: crawlLangs) {
					otherLangDetectorMap.put(lang, otherLangDetector);
				}
				defaultLangDetector.setLangDetectorsMap(otherLangDetectorMap);
				break;
			}
		} 
		
		// Now check	
		for (String fileName : fileNames) {
			String goldLang = fileNamesHM.get(fileName);
			int hits = 0;
			InputStream in  =  LangDetectorFactory.class.getResource("/rs_ba_hr_corpus/"+fileName).openStream();
			try {
				List<String> lines =  IOUtils.readLines(in, "UTF-8");
				HashMap<String, Integer> misses = new HashMap<String, Integer>();
				for (Iterator<String> iterator = lines.iterator(); iterator.hasNext();) {
					String line =  iterator.next();
					String autoLang = defaultLangDetector.detect(line);
					if (langsTBFI.contains(autoLang)) {
						// logger.debug("Rechecking " + autoLang);
						autoLang = defaultLangDetector.detect(line, autoLang);
						// logger.debug("Result is  " + autoLang);

					}
					if ( autoLang.equalsIgnoreCase(goldLang) ) {
						hits++;
					} else if (misses.containsKey(autoLang)) {
						misses.put(autoLang, misses.get(autoLang) + 1);
					} else {
						misses.put(autoLang, 1);
					}
				}
				logger.info(langDetectorId + ":" + fileName + ":" + goldLang + ":" + ((float) hits*100/lines.size()));
				logger.info("Misses are: "+misses);
			} finally {
				IOUtils.closeQuietly(in);
			}
		}
		System.out.println();
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
	
	@Test
	public void testLangDetectorEval()  {
		System.out.println();
		logger.info("Started evaluating language detector" );

		for (String testFile:  new String[]{"/lang-detection/nno-nob-100-chars.txt", "/lang-detection/nno-nob-500-chars.txt"}) {

			InputStream in;
			try {
				in = AnalyzerTest.class.getResource(testFile).openStream();
				int instances = 0;
				int hits = 0;
				logger.debug("Started testing language detector on {} ", testFile );
				for (String line:  IOUtils.readLines(in, "UTF-8")) {
					if (StringUtils.isAnyBlank(line) || line.startsWith(Constants.BAR)) {
						continue;
					} else {
						instances++;
						String[] fields = StringUtils.split(line, Constants.TAB);
						String expected = fields[0]; 
						String detected = langDetector.detect(fields[1]);
						if (detected.equals(expected)) {
							hits++;
						} else {
							//logger.info(detected);
						}
					}
				}
				logger.info("Accuracy (non-deterministic) is {} on {} ", ( df2.format(((double) hits) /instances)), testFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
