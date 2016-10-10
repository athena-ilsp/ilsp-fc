package gr.ilsp.fc.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalyzerTest {

	private final static String APPNAME = AnalyzerTest.class.getSimpleName();
	static Logger logger = LoggerFactory.getLogger(APPNAME);

	AnalyzerFactory analyzerFactory = new AnalyzerFactory();
	
	private void testTokenAnalyzer(Analyzer analyzer, String token, String expectedStem, String langcode, String testName) {
		try {
			List<String> result = new ArrayList<String>();
			try {	
				TokenStream tokenStream  = analyzer.tokenStream("contents", new StringReader(token));
				tokenStream.reset();
				while (tokenStream.incrementToken()) {
					logger.debug(tokenStream.getAttribute(CharTermAttribute.class).toString());
					result.add(tokenStream.getAttribute(CharTermAttribute.class).toString());
				}
				tokenStream.close();
			} catch (IOException e) {
				// not thrown b/c we're using a string reader...
				throw new RuntimeException(e);
			}
			String actualStem = result.get(0);
			if (expectedStem.equals(actualStem)) {
				logger.debug("SUC Token/ExpectedStem/SystemStem: " + token + "/" + expectedStem + "/" + actualStem);
			} else {
				logger.debug("ERR Token/ExpectedStem/SystemStem: " + token + "/" + expectedStem + "/" + actualStem);
			}
			Assert.assertEquals(expectedStem, actualStem);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testNaiveAnalyzer() throws Exception {
		String[] languages = new String[] {"sk", };
		//String[] languages = new String[] {"mt", }; // FIXME. More tests for mlt using the gabra resource.
		String testDir = "/analyzer-tests/";

		for (String lang: languages) {
			logger.info("Stemming test for " + lang );
			Analyzer analyzer = analyzerFactory.getAnalyzer(lang);
			InputStream in  =  AnalyzerTest.class.getResource(testDir+lang).openStream();
			List<Pair<String, String>> tokenStems = getTokenStemsForLang(in);
			in.close();
			for (Pair<String, String> tokenStem: tokenStems) {
				testTokenAnalyzer(analyzer, tokenStem.getLeft(),  tokenStem.getRight(), lang,	lang + " test");
			}
		}			
		logger.info("Finished stemming tests" );
	}

	
	
	private List<Pair<String, String>> getTokenStemsForLang(InputStream in) throws IOException {
		List<Pair<String, String>> tokenStems = new ArrayList<Pair<String, String>>();
		for (String line:  IOUtils.readLines(in, "UTF-8")) {
			if (StringUtils.isAnyBlank(line) || line.startsWith("#")) {
				continue;
			} else {
				String[] fields = StringUtils.split(line);
				tokenStems.add(Pair.of(fields[0], fields[1]));
			}
		}
		return tokenStems;
	}

	
}



///**
// * Test method for {@link gr.ilsp.fc.utils.Analyzer()}.
// * @throws Exception 
// */
//@Test
//public void testAnalyzer() throws Exception {
//	String language = "eng";
//	String text = "Questions concerning the philosophy of language, such as whether words can represent experience, "
//			+ "have been debated since Gorgias and Plato in Ancient Greece. Thinkers such as Rousseau have argued "
//			+ "that language originated from emotions while others like Kant have held that it originated from rational "
//			+ "and logical thought. 20th-century philosophers such as Wittgenstein argued that philosophy is really the "
//			+ "study of language. Major figures in linguistics include Ferdinand de Saussure, Noam Chomsky and William C. Stokoe.";
//	List<String> result = new ArrayList<String>();
//	try {	
//		analyzer = analyzerFactory.getAnalyzer(language);
//		TokenStream tokenStream  = analyzer.tokenStream("contents", new StringReader(text));
//		tokenStream.reset();
//		while (tokenStream.incrementToken()) {
//			logger.debug(tokenStream.getAttribute(CharTermAttribute.class).toString());
//			result.add(tokenStream.getAttribute(CharTermAttribute.class).toString());
//		}
//	} catch (IOException e) {
//		// not thrown b/c we're using a string reader...
//		throw new RuntimeException(e);
//	}
//	logger.info(result.toString());
//}
