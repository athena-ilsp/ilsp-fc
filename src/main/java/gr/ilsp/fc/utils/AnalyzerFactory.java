package gr.ilsp.fc.utils;

import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

//import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;

import org.apache.lucene.analysis.ar.ArabicAnalyzer;
import org.apache.lucene.analysis.bg.BulgarianAnalyzer;
import org.apache.lucene.analysis.ca.CatalanAnalyzer;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.analysis.cz.CzechAnalyzer;
import org.apache.lucene.analysis.da.DanishAnalyzer;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.el.GreekAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.eu.BasqueAnalyzer;
import org.apache.lucene.analysis.fa.PersianAnalyzer;
import org.apache.lucene.analysis.fi.FinnishAnalyzer;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.analysis.ga.IrishAnalyzer;
import org.apache.lucene.analysis.gl.GalicianAnalyzer;
import org.apache.lucene.analysis.hi.HindiAnalyzer;
import org.apache.lucene.analysis.hu.HungarianAnalyzer;
import org.apache.lucene.analysis.id.IndonesianAnalyzer;
import org.apache.lucene.analysis.it.ItalianAnalyzer;
import org.apache.lucene.analysis.lt.LithuanianAnalyzer;
import org.apache.lucene.analysis.lv.LatvianAnalyzer;
import org.apache.lucene.analysis.nl.DutchAnalyzer;
import org.apache.lucene.analysis.no.NorwegianAnalyzer;
import org.apache.lucene.analysis.pl.PolishAnalyzer;
import org.apache.lucene.analysis.pt.PortugueseAnalyzer;
import org.apache.lucene.analysis.ro.RomanianAnalyzer;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.analysis.sv.SwedishAnalyzer;
import org.apache.lucene.analysis.tr.TurkishAnalyzer;
import org.apache.lucene.analysis.th.ThaiAnalyzer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnalyzerFactory {

	private static final Logger logger = LoggerFactory.getLogger(AnalyzerFactory.class);
	
	static String[] langs = { "deu", "ell", "eng", "spa", "fra", "ita", "por", "lav", "lit", "hrv", "srp", "bos", "hun", "jpn", "zho", "vie", "tur", "ara", "hin","fas","isl",
			"gle", "fin", "grc", "pol", "ron", 
			"nob", "nno", "nor", // Should we remove nor?  FIXME
			"nld", "glg", "ces", "dan", "euq", "bul", "rus", "swe", "slv", "cat", "est", "mlt", "slk", "tha", "ind" };
	List<String> langsList = Arrays.asList(langs);
	
	
	public Analyzer getAnalyzer(String lang) throws Exception {

		String lang3 = ISOLangCodes.get3LetterCode(lang);
		logger.debug(lang + "->" + lang3);
		
		if (lang3.equals("deu")) {
			return new GermanAnalyzer();
		} else if (lang3.equals("ell")) {
			return new GreekAnalyzer();
		} else if (lang3.equals("grc")) {
			return new GreekAnalyzer();
		} else if (lang3.equals("eng")) {
			return new EnglishAnalyzer();
		} else if (lang3.equals("spa")) {
			return new SpanishAnalyzer();
		} else if (lang3.equals("fra")) {
			return new FrenchAnalyzer();
		} else if (lang3.equals("ita")) {
			return new ItalianAnalyzer();
		} else if (lang3.equals("por")) {
			return new PortugueseAnalyzer();
		} else if (lang3.equals("lav")) {
			return new LatvianAnalyzer();
		} else if (lang3.equals("lit")) {
			return new LithuanianAnalyzer();
		} else if (lang3.equals("hrv") || lang3.equals("srp") || lang3.equals("bos")) { //FIXME Same analyzer for Serbian, Bosnian and Croatian !!!
			return new CroatianAnalyzer();
		} else if (lang3.equals("hun")) {
			return new HungarianAnalyzer();
		} else if (lang3.equals("jpn") || lang3.equals("zho") || lang3.equals("vie")) {
			return new CJKAnalyzer();
		} else if (lang3.equals("gle")) {
			return new IrishAnalyzer();
		} else if (lang3.equals("fin") || lang3.equals("est")) { //FIXME FinnishAnalyzer for Finnish and  Estonian
			return new FinnishAnalyzer();
		} else if (lang3.equals("pol")) {
			return new PolishAnalyzer();
		} else if (lang3.equals("ron")) {
			return new RomanianAnalyzer();
		} else if (lang3.equals("nob") || lang3.equals("nno") || lang3.equals("isl") || lang3.equals("nor")) {//FIXME Same analyzer for Norwegian and Icelandic !!! // FIXME: Should we remove nor?
			return new NorwegianAnalyzer();
		} else if (lang3.equals("nld")) {
			return new DutchAnalyzer();
		} else if (lang3.equals("glg")) {
			return new GalicianAnalyzer();
		} else if (lang3.equals("ces") || lang3.equals("slk")) { //FIXME Same analyzer for Czech and Slovak !!!
			return new CzechAnalyzer();
		} else if (lang3.equals("dan")) {
			return new DanishAnalyzer();
		} else if (lang3.equals("euq")) {
			return new BasqueAnalyzer();
		} else if (lang3.equals("bul")) {
			return new BulgarianAnalyzer();
		} else if (lang3.equals("rus")) {
			return new RussianAnalyzer();
		} else if (lang3.equals("swe")) {
			return new SwedishAnalyzer();
		} else if (lang3.equals("slv")) {
			return new SloveneAnalyzer();
		} else if (lang3.equals("cat")) {
			return new CatalanAnalyzer();
		} else if (lang3.equals("mlt")) {
			return new MalteseAnalyzer(); 
		}else if (lang3.equals("tur")) {
			return new TurkishAnalyzer(); 
		}else if (lang3.equals("ara")) {
			return new ArabicAnalyzer(); 
		}else if (lang3.equals("hin")) {
			return new HindiAnalyzer(); 
		}else if (lang3.equals("fas")) {
			return new PersianAnalyzer(); 
		}else if (lang3.equals("tha")) {
			return new ThaiAnalyzer(); 
		}else if (lang3.equals("ind")) {
			return new IndonesianAnalyzer(); 
		}
		
		else if (lang3.equals("slk-hunspell")) {
			// FIXME Obviously this if and the hunspell-analyzer will never be reached due to the CzechAnalyzer if above. 
			// Our current hunspell-based stemmer seems to perform worse on a very small test case.
			return new SlovakAnalyzer(); 
		} 
		else {
			//throw new Exception("No analyzer available for language " + lang3 + ".\n" + 
			//		"Available languages are " + langsList.toString() + ".\n");
			logger.error("No analyzer available for language " + lang3 + ".\n" + 	"Available languages are " + langsList.toString() + ".\n"+ " English Analyzer is used!");
			return new EnglishAnalyzer();
		}
	}

	public static void main(String[] args) throws Exception {
		AnalyzerFactory analyzerFactory = new AnalyzerFactory();
		Analyzer analyzer = analyzerFactory.getAnalyzer("hr");
		TokenStream tokenStream = analyzer.tokenStream("contents", 
				new InputStreamReader(AnalyzerFactory.class.getClass().getResourceAsStream("/hr_stemmer_test_small.txt")));

		// OffsetAttribute offsetAttribute =
		// tokenStream.addAttribute(OffsetAttribute.class);
		// CharTermAttribute charTermAttribute = (CharTermAttribute)
		// tokenStream.addAttribute(CharTermAttribute.class);

		try {
			tokenStream.reset(); // Resets this stream to the beginning.
									// (Required)
			while (tokenStream.incrementToken()) {
				// Right way to get tokens
				// String stemmedToken = charTermAttribute.toString();

				// System.out.println("In: " +
				// tokenStream.reflectAsString(true));
				// System.out.println("Out: " + stemmedToken);

				// System.out.println("token start offset: " +
				// offsetAttribute.startOffset());
				// System.out.println("token end offset: " +
				// offsetAttribute.endOffset());
			}
			tokenStream.end(); // Perform end-of-stream operations, e.g. set the
								// final offset.
		} finally {
			tokenStream.close(); // Release resources associated with this
									// stream.
			analyzer.close();
		}
	}

}
