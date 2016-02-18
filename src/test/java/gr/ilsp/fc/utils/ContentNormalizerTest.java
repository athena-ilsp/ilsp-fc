/**
 * 
 */
package gr.ilsp.fc.utils;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author prokopis
 *
 */
public class ContentNormalizerTest {

	
	private static final Logger logger = LoggerFactory.getLogger(ContentNormalizerTest.class);
	
	
	static Random rand = new Random(1234567890);
	
	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * Test method for {@link gr.ilsp.fc.utils.ContentNormalizer#normalizeText(java.lang.String)}.
	 * @throws IOException 
	 */
	@Test
	public void testNormalizeText() throws IOException {
		List<String> origStrings = IOUtils.readLines(ClassLoader.getSystemResourceAsStream("content-normalizer-orig-strings.txt"));
		List<String> cleanStrings = IOUtils.readLines(ClassLoader.getSystemResourceAsStream("content-normalizer-clean-strings.txt"));
		
		for (int i=0; i<origStrings.size(); i++) {
			String origString = origStrings.get(i);
			String cleanString = cleanStrings.get(i);
			logger.info(origString);
			logger.info(ContentNormalizer.normalizeText(origString));
			assertTrue(cleanString.equals(ContentNormalizer.normalizeText(origString)));
		}
		
		
		
		String repeated = StringUtils.repeat("abcdefghijklmnopqrstuvwxyz", 1000);
		StringBuilder sb = new StringBuilder(repeated);
		
		String testStringOrig = sb.toString();

		// U+D800..U+DB7F 	High Surrogates 	896 Code points, 0 	Assigned characters
		// U+DB80..U+DBFF 	High Private Use Surrogates 	128 Code points, 0 	Assigned characters
		// U+DC00..U+DFFF 	Low Surrogates 	1,024 	0	Assigned characters
		for (int hex = 0xD800; hex <= 0xDBFF ; hex++){
			sb.insert(randInt(0, sb.length()), new String(Character.toChars(hex)));
		}
		for (int hex = 0xDC00; hex <= 0xDFFF ; hex++){
			sb.insert(randInt(0, sb.length()), new String(Character.toChars(hex)));
		}

		
	//	assertTrue(testStringOrig.equals(ContentNormalizer.normalizeText(sb.toString())));
	}

	
	
	
	

	/**
	 * Returns a pseudo-random number between min and max, inclusive.
	 * The difference between min and max can be at most
	 * <code>Integer.MAX_VALUE - 1</code>.
	 *
	 * @param min Minimum value
	 * @param max Maximum value.  Must be greater than min.
	 * @return Integer between min and max, inclusive.
	 * @see java.util.Random#nextInt(int)
	 */
	public static int randInt(int min, int max) {

		// nextInt is normally exclusive of the top value,
		// so add 1 to make it inclusive
		int randomNum = rand.nextInt((max - min) + 1) + min;

		return randomNum;
	}




}
