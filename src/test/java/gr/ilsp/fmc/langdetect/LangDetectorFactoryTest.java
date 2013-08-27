package gr.ilsp.fmc.langdetect;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LangDetectorFactoryTest {
	
	private static final Logger logger = LoggerFactory.getLogger(LangDetectorFactoryTest.class);
	private HashSet<String> langDetectorIds;

	@Before
	public void setUp() throws Exception {
		langDetectorIds = new HashSet<String>();
		langDetectorIds.add("langdetect");
		langDetectorIds.add("bs-hr-sr-nb");

//		langDetectorIds.add("tika");
//		langDetectorIds.add("langid");
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
		fileNamesHM.put("politika.rs.txt", "sr");
		fileNamesHM.put("vecernji.hr.txt", "hr");
		fileNamesHM.put("dnevniavaz.ba.txt", "bs");
		Set<String> fileNames = fileNamesHM.keySet();

		LangDetectorFactory langDetectorFactory = new LangDetectorFactory();
		for (String langDetectorId : langDetectorIds) {
			logger.info("Loading LangDetector " + langDetectorId );
			LangDetector langDetector = langDetectorFactory.getLangDetector(langDetectorId);
			langDetector.initialize();
			for (String fileName : fileNames) {
				String goldLang = fileNamesHM.get(fileName);
				int hits = 0;
				InputStream in  =  LangDetectorFactory.class.getResource("/rs_ba_hr_corpus/"+fileName).openStream();
				try {
					List<String> lines =  IOUtils.readLines(in, "UTF-8");
					HashMap<String, Integer> misses = new HashMap<String, Integer>();
					for (Iterator<String> iterator = lines.iterator(); iterator.hasNext();) {
						String line =  iterator.next();
						String autoLang = langDetector.detect(line);
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

}
