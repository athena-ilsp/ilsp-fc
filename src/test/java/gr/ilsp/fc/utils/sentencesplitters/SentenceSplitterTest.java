package gr.ilsp.fc.utils.sentencesplitters;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SentenceSplitterTest {
	
	
	private static final Logger logger = LoggerFactory.getLogger(SentenceSplitterTest.class);
	
	
	SentenceSplitterFactory sentenceSplitterFactory ;

	@Before
	public void setUp() throws Exception {
		sentenceSplitterFactory = new SentenceSplitterFactory();
	}

	private void testSentenceSplitter(SentenceSplitter sentenceSplitter, String input, int numOfSents, String testName) {
		try {
			List<String> sentences = sentenceSplitter.getSentences(input, 1);
			logger.debug(sentences.toString());
			Assert.assertEquals(testName + ": ", numOfSents, sentences.size());
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testDeuSentenceSplitter() {
		SentenceSplitter sentenceSplitter = sentenceSplitterFactory.getSentenceSplitter("deu");
		testSentenceSplitter(sentenceSplitter, 
				"In dieser Finanzierungsrunde fließen Subitec Mittel in Höhe von 4,5 Mio. Euro zu. ", 
				1,
				"deu sentence splitter");
		testSentenceSplitter(sentenceSplitter, 
				"Nie hätte das passieren sollen. Dr. Soltan sagte: \"Der Fluxcompensator war doch kalibriert!\".", 
				2,
				"deu sentence splitter");
	}

	@Test
	public void testEngSentenceSplitter() {
		SentenceSplitter sentenceSplitter = sentenceSplitterFactory.getSentenceSplitter("eng");
		testSentenceSplitter(sentenceSplitter, 
				"As stated in art. 6, par. 2, of legislative decree no. 300/1999, "
						+ "the Secretary General assists the Minister directly in drafting Ministry policies and programmes, "
						+ "oversees the Ministry to ensure the continuity of its functions, and coordinates the activities of its various offices.", 
						1,
				"eng sentence splitter");
		testSentenceSplitter(sentenceSplitter, 
				"This is a paragraph. It contains several sentences. \"But why,\" you ask?'",
				3,
				"eng sentence splitter");
		testSentenceSplitter(sentenceSplitter, 
				"Hey! Now.",
				2,
				"eng sentence splitter");

		// Notice that arts. is not an abbr in eng list, but art. is 
		testSentenceSplitter(sentenceSplitter, 
				"The ICRE together with the International Contest of Guitar \"Miquel Llobet\" and the Museu de la Música launched this year's TRIDIMENSIONAL MUSIC INTERNATIONAL CONTEST '' "
				+ "Homage to the Guitar, with the intention of promoting creativity between the artists and sculptors and taking part in this proposal "
				+ "that puts music in relation with the plastic arts. It is, therefore, an event that aims to promote the various representations of the guitar, its music, composers or interpreters.",
				2,
				"eng sentence splitter");
	
	}

	@Test
	public void testEllSentenceSplitter() {
		SentenceSplitter sentenceSplitter = sentenceSplitterFactory.getSentenceSplitter("ell");
		testSentenceSplitter(sentenceSplitter, 
				"Μπορούν να επιλύουν τις απορίες τους καθημερινά από τις 9 π.μ. έως τις 5 μ.μ.", 
						1,
				"ell sentence splitter");
		testSentenceSplitter(sentenceSplitter, 
				"Όλα τα συστήματα ανώτατης εκπαίδευσης σχεδιάζονται σε εθνικό επίπεδο. "
				+ "Η Ε.Ε. αναλαμβάνει κυρίως να συμβάλει στη βελτίωση της συγκρισιμότητας μεταξύ των διάφορων συστημάτων και να βοηθά φοιτητές "
				+ "και καθηγητές να μετακινούνται με ευκολία μεταξύ των συστημάτων των κρατών μελών.", 
						2,
				"ell sentence splitter");
		
		
		testSentenceSplitter(sentenceSplitter, 
				"Η εφαρμογή αναπτύχθηκε από την Υ.Σ.Μ.Α. και το Εθνικό Κέντρο Τεκμηρίωσης (ΕΚΤ)."
				+ "Για τον δανεισμό είναι απαραίτητη η τηλεφωνική κράτηση στο 210 9239186, Δευτέρα έως Παρασκευή 12.00 μ.μ. έως 2.00 μ.μ.", 
						2,
				"ell sentence splitter");
		
		 
	}

	@Test
	public void testPorSentenceSplitter() {
		SentenceSplitter sentenceSplitter = sentenceSplitterFactory.getSentenceSplitter("por");
		testSentenceSplitter(sentenceSplitter, 
				"Isto é um parágrafo. Contém várias frases. «Mas porquê,» perguntas tu?", 
						3,
				"por sentence splitter");
	}

	@Test
	public void testSpaSentenceSplitter() {
		SentenceSplitter sentenceSplitter = sentenceSplitterFactory.getSentenceSplitter("spa");
		testSentenceSplitter(sentenceSplitter, 
				"La UE ofrece una gran variedad de empleos en un entorno multinacional y multilingüe."
				+ " La Oficina Europea de Selección de Personal (EPSO) se ocupa de la contratación, sobre todo mediante oposiciones generales.", 
						2,
				"por sentence splitter");
	}

	//	


}
