/**
 * 
 */
package gr.ilsp.fc.utils;

import gr.ilsp.fc.utils.AnalyzerFactory;
import gr.ilsp.fc.utils.CroatianStemmer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

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
public class CroatianStemmerTest {

	private final static String APPNAME = CroatianStemmerTest.class.getSimpleName();
	static Logger logger = LoggerFactory.getLogger(APPNAME);
	
	CroatianStemmer croatianStemmer;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		croatianStemmer = new CroatianStemmer();
	}

	/**
	 * Test method for {@link gr.ilsp.fc.utils.CroatianStemmer#stem()}.
	 * @throws Exception 
	 */
	@Test
	public void testStem() throws Exception {
		String inCharsetName = "UTF-8";
		String outCharsetName = "UTF-8";
		String inFilename = "src/test/resources/vecernji.hr.stem";
		String outFilename = "src/test/resources/vecernji.hr.test";
		logger.info("Reading: " + inFilename);
		logger.info("Writing: " + outFilename);

		BufferedReader br = null;
		BufferedWriter bw = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(inFilename), inCharsetName));
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFilename), outCharsetName));
			AnalyzerFactory analyzerFactory = new AnalyzerFactory();
			Analyzer analyzer = analyzerFactory.getAnalyzer("hr");

			int lines=0;
			int hits=0;
			
			String line;
			while ((line = br.readLine()) != null) {
				lines++;
				List<String> fields = Arrays.asList(line.split("\\s+"));
				String token = fields.get(0).toLowerCase();
				String stem0 = fields.get(1);

				String stemmedToken = token;
				TokenStream tokenStream = analyzer.tokenStream("contents",
						new StringReader(token));
				CharTermAttribute charTermAttribute = (CharTermAttribute) tokenStream
						.addAttribute(CharTermAttribute.class);
				try {
					tokenStream.reset(); // Resets this stream to the beginning.
											// (Required)
					while (tokenStream.incrementToken()) {
						// Right way to get tokens
						stemmedToken = charTermAttribute.toString();
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
				if (stemmedToken.equals(stem0)) {
					hits++;
				} else {
					//bw.write( fields.get(0)+ "\t"+ fields.get(1)+ "\t"+ stemmedToken +"\t********************\n");
				}
				bw.write( fields.get(0)+ "\t"+ stemmedToken +"\n");
			}
			analyzer.close();

			if (br != null)
				br.close();
			if (bw != null)
				bw.close();
			logger.info("Done");
			logger.info("Acc: "+ ((double) hits / lines));

		} catch (FileNotFoundException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

}
