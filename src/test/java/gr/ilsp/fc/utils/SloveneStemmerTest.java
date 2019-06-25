/**
 * 
 */
package gr.ilsp.fc.utils;

import java.io.File;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import gr.ilsp.nlp.commons.Constants;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author prokopis
 *
 */
public class SloveneStemmerTest {

	private final static String APPNAME = SloveneStemmerTest.class.getSimpleName();
	static Logger logger = LoggerFactory.getLogger(APPNAME);

	SloveneStemmer sloveneStemmer;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		sloveneStemmer = new SloveneStemmer();
	}

	/**
	 * Test method for {@link gr.ilsp.fc.utils.SloveneStemmer#stem()}.
	 * @throws Exception 
	 */
	@Test
	public void testStem() throws Exception {
		File sloveneTestsFile = new File("src/test/resources/slovese-stemmer-tests.txt");
		logger.info("Reading test case file: " + sloveneTestsFile);
		List<String> lines = FileUtils.readLines(sloveneTestsFile, Constants.UTF8);
		
		AnalyzerFactory analyzerFactory = new AnalyzerFactory();
		Analyzer analyzer = analyzerFactory.getAnalyzer("slv");
		
		int i = 0;
		int hits=0;

		for (String line: lines) {
			i++;
			List<String> fields = Arrays.asList(StringUtils.split(line));
			String token = fields.get(0);
			String gold = fields.get(1);

			String system = token;
			TokenStream tokenStream = analyzer.tokenStream("contents",	new StringReader(token));
			CharTermAttribute charTermAttribute = (CharTermAttribute) tokenStream.addAttribute(CharTermAttribute.class);
			try {
				tokenStream.reset(); // Resets this stream to the beginning.
				// (Required)
				while (tokenStream.incrementToken()) {
					// Right way to get tokens
					system = charTermAttribute.toString();
					// System.out.println("In: " +
					// tokenStream.reflectAsString(true));
					// System.out.println("Out: " + stemmedToken);
				}
				tokenStream.end(); // Perform end-of-stream operations, e.g.
				// set the final offset.
			} finally {
				tokenStream.close(); // Release resources associated with
				// this stream.
			}
			if (system.equals(gold)) {
				//logger.warn("correct : " + "source: " + token  + " system: " + system + " gold:" + gold);

				hits++;
			} else {
				//logger.warn("Error : " + "source: " + token  + " system: " + system + " gold:" + gold);
			}
		}
		analyzer.close();
		logger.info("Done");
		logger.info("Acc: "+ ((double) hits / i));

	}

}