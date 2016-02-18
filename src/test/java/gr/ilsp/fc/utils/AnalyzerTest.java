package gr.ilsp.fc.utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalyzerTest {


	private final static String APPNAME = AnalyzerTest.class.getSimpleName();
	static Logger logger = LoggerFactory.getLogger(APPNAME);

	Analyzer analyzer ;
	AnalyzerFactory analyzerFactory = new AnalyzerFactory();


	/**
	 * Test method for {@link gr.ilsp.fc.utils.Analyzer()}.
	 * @throws Exception 
	 */
	@Test
	public void testAnalyzer() throws Exception {
		String language = "eng";
		String text = "Questions concerning the philosophy of language, such as whether words can represent experience, "
				+ "have been debated since Gorgias and Plato in Ancient Greece. Thinkers such as Rousseau have argued "
				+ "that language originated from emotions while others like Kant have held that it originated from rational "
				+ "and logical thought. 20th-century philosophers such as Wittgenstein argued that philosophy is really the "
				+ "study of language. Major figures in linguistics include Ferdinand de Saussure, Noam Chomsky and William C. Stokoe.";
		List<String> result = new ArrayList<String>();
		try {	
			analyzer = analyzerFactory.getAnalyzer(language);
			TokenStream tokenStream  = analyzer.tokenStream("contents", new StringReader(text));
			tokenStream.reset();
			while (tokenStream.incrementToken()) {
				logger.debug(tokenStream.getAttribute(CharTermAttribute.class).toString());
				result.add(tokenStream.getAttribute(CharTermAttribute.class).toString());
			}
		} catch (IOException e) {
			// not thrown b/c we're using a string reader...
			throw new RuntimeException(e);
		}
		logger.info(result.toString());
	}

}


