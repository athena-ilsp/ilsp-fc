package gr.ilsp.fmc.utils;

import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

//import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.el.GreekAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.analysis.hu.HungarianAnalyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.lv.LatvianAnalyzer;
import org.apache.lucene.analysis.pt.PortugueseAnalyzer;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.analysis.ga.IrishAnalyzer;

//import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
//import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.util.Version;

public class AnalyzerFactory {
	//private static final Logger LOGGER = Logger.getLogger(AnalyzerFactory.class);

	String[] langs = {"de", "el", "en", "es", "fr", "it", "pt", "lv", "hr", "hu", "ja","ga"};
	List<String> langsList = Arrays.asList(langs);

	public Analyzer getAnalyzer (String lang) throws Exception {

		if (lang.equals("de")) {
			return new GermanAnalyzer(Version.LUCENE_40);
		} else	if (lang.equals("el")) {
			return new GreekAnalyzer(Version.LUCENE_40);
		} else	if (lang.equals("en")) {
			return new EnglishAnalyzer(Version.LUCENE_40);
		} else if (lang.equals("es")) {
			return new SpanishAnalyzer(Version.LUCENE_40);
		} else	if (lang.equals("fr")) {
			return new FrenchAnalyzer(Version.LUCENE_40);
		} else	if (lang.equals("it")) {
			return new ItalianAnalyzer(Version.LUCENE_40);
		} else	if (lang.equals("pt")) {
			return new PortugueseAnalyzer(Version.LUCENE_40);
		} else	if (lang.equals("lv")) {
			return new LatvianAnalyzer(Version.LUCENE_40);
		} else	if (lang.equals("hr") ) {
			return new CroatianAnalyzer(Version.LUCENE_40);
		} else	if (lang.equals("hu") ) {
			return new HungarianAnalyzer(Version.LUCENE_40);
		} else	if (lang.equals("ja") ) {
			return new CJKAnalyzer(Version.LUCENE_40);		
		}else	if (lang.equals("ga") ) {
			return new IrishAnalyzer(Version.LUCENE_40);
		}
		else {
			throw new Exception("No analyzer available for language " + lang + ".\n"
					+ "Available languages are " + langsList.toString() + ".\n");
		}
	}
	
	public static void main(String[] args) throws Exception {
		AnalyzerFactory analyzerFactory = new AnalyzerFactory();
		Analyzer analyzer = analyzerFactory.getAnalyzer("hr");
		TokenStream tokenStream = analyzer.tokenStream("contents", new InputStreamReader(AnalyzerFactory.class.getClass().getResourceAsStream("/hr_stemmer_test_small.txt")));

		//OffsetAttribute offsetAttribute = tokenStream.addAttribute(OffsetAttribute.class);
		//CharTermAttribute charTermAttribute = (CharTermAttribute) tokenStream.addAttribute(CharTermAttribute.class);

		try {
			tokenStream.reset(); // Resets this stream to the beginning. (Required)
			while (tokenStream.incrementToken()) {
				// Right way to get tokens
				//String stemmedToken = charTermAttribute.toString();

				//System.out.println("In: " + tokenStream.reflectAsString(true));
				//System.out.println("Out: " + stemmedToken);
				
//				System.out.println("token start offset: " + offsetAttribute.startOffset());
//				System.out.println("token end offset: " + offsetAttribute.endOffset());
			}
			tokenStream.end(); // Perform end-of-stream operations, e.g. set the final offset.
		} finally {
			tokenStream.close(); // Release resources associated with this stream.
			analyzer.close();
		} 		
	}
	
}
