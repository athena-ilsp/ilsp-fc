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
			if (numOfSents!=sentences.size()) {
				logger.info( sentences.size() + " != (expected) " + numOfSents);
				logger.info(input);
				logger.info(sentences.toString());
				for (String sentence: sentences) {
					System.out.println(sentence);
				}
			}
			Assert.assertEquals(testName + ": ", numOfSents, sentences.size());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testDeuSentenceSplitter() {
		logger.info("Testing deu sentence splitter");

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
		logger.info("Testing eng sentence splitter");

		SentenceSplitter sentenceSplitter = sentenceSplitterFactory.getSentenceSplitter("eng");
		
		testSentenceSplitter(sentenceSplitter, 
				//"Prices in June dropped 0.1%, while average monthly change is -0.6%.",
				"Prices in June dropped 0.1 10.1 -10.1 0.1 per cent",				
				1,
				"eng sentence splitter");

		testSentenceSplitter(sentenceSplitter, 
				"I visited https://www.google.gr/search?q=ICU+segmentation+rules&ie=utf-8&oe=utf-8&gws_rd=cr&ei=97e-V5H6IsOsUZyFuqAN#q=language+independent+sentence+splitter",
				1,
				"eng sentence splitter");
		
		
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
		
		testSentenceSplitter(sentenceSplitter, 
				"I still get goose bumps thinking about it,\" 'Der Bomber' said recently.",
				1,
				"eng sentence splitter");
		
		testSentenceSplitter(sentenceSplitter, 				
				"It was a fairy tale,\" Rossi said.",
				1,
				"eng sentence splitter");

		testSentenceSplitter(sentenceSplitter, 
				"They left around 12:30:20.", 
						1,
				"eng sentence splitter");

		testSentenceSplitter(sentenceSplitter, "The wreck, fully loaded with trade amphorae that are dated in the end of 2nd or beginning of 1st cent. B.C., was assessed as partcularly interesting.",
				1,
				"eng sentence splitter");

		// Notice that arts. is not an abbr in eng list, but art. is 
		testSentenceSplitter(sentenceSplitter, 
				"The ICRE together with the International Contest of Guitar \"Miquel Llobet\" and the Museu de la Música launched this year's TRIDIMENSIONAL MUSIC INTERNATIONAL CONTEST '' "
				+ "Homage to the Guitar, with the intention of promoting creativity between the artists and sculptors and taking part in this proposal "
				+ "that puts music in relation with the plastic arts. It is, therefore, an event that aims to promote the various representations of the guitar, its music, composers or interpreters.",
				2,
				"eng sentence splitter");


		testSentenceSplitter(sentenceSplitter,
				"The wreck, fully loaded with trade amphorae that are dated in the end of 2nd or beginning of 1st b.C., was assessed as partcularly interesting.",
				1,
				"eng sentence splitter");
		
	}

	@Test
	public void testEllSentenceSplitter() {
		logger.info("Testing ell sentence splitter");
		
		SentenceSplitter sentenceSplitter = sentenceSplitterFactory.getSentenceSplitter("ell");
		
		testSentenceSplitter(sentenceSplitter, 
				"Η περίπτωση β' της παρ. 3 του άρθρου 9 του ν. 3213/2003 αντικαθίσταται ως ακολούθως: «β. Τα περιουσιακά στοιχεία που δεν δηλώθηκαν στην περίπτωση κάποιου από τα αδικήματα των παραγράφων 1 και 2 του άρθρου 6, και εφόσον δεν έχει προηγηθεί εφαρμογή της παρ. 5 του άρθρου 2, δημεύονται με την καταδικαστική απόφαση, εκτός αν ο υπαίτιος αποδεικνύει τη νόμιμη προέλευσή τους και ότι η μη υποβολή ή η υποβολή ανακριβούς ή ελλιπούς δήλωσης δεν οφείλεται σε δόλο.»", 
				1,
				"ell sentence splitter");
		
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
				"3. Με τις ποινές των παραγράφων 1 και 2 τιμωρούνται και οι τρίτοι που πορίζονται το αθέμιτο όφελος που προκύπτει από τα αδικήματα των παραγράφων 1 και 2 εν γνώσει της τέλεσης αυτών από τον υπόχρεο σε δήλωση. 4. Οι παραπάνω διατάξεις εφαρμόζονται, εφόσον η πράξη του υπόχρεου σε δήλωση ή του τρίτου δεν τιμωρείται βαρύτερα με άλλη διάταξη.»",
				2,
				"ell sentence splitter");

		testSentenceSplitter(sentenceSplitter, 
				"4. Οι παραπάνω διατάξεις εφαρμόζονται, εφόσον η πράξη του υπόχρεου σε δήλωση ή του τρίτου δεν τιμωρείται βαρύτερα με άλλη διάταξη.»",
				1,
				"ell sentence splitter");
		
		testSentenceSplitter(sentenceSplitter, 
				"Η εφαρμογή αναπτύχθηκε από την Υ.Σ.Μ.Α. και το Εθνικό Κέντρο Τεκμηρίωσης (ΕΚΤ)."
				+ "Για τον δανεισμό είναι απαραίτητη η τηλεφωνική κράτηση στο 210 9239186, Δευτέρα έως Παρασκευή 12.00 μ.μ. έως 2.00 μ.μ.", 
						2,
				"ell sentence splitter");
		
		testSentenceSplitter(sentenceSplitter, 
				"Από τις αρχές του 1ου αιώνα π.Χ., είχε αξιολογηθεί ως ιδιαίτερα ενδιαφέρον. Από τις αρχές του 1ου αιώνα π.Χ.) είχε αξιολογηθεί ως ιδιαίτερα ενδιαφέρον. ", 
						2,
				"ell sentence splitter");

		testSentenceSplitter(sentenceSplitter, 
				"Από τις αρχές του 1ου αιώνα π.Χ.), είχε αξιολογηθεί ως ιδιαίτερα ενδιαφέρον.", 
						1,
				"ell sentence splitter");
		testSentenceSplitter(sentenceSplitter, 
				"Έφυγαν π.χ. κατά τις 12:30:20 το μεσημέρι.", 
						1,
				"ell sentence splitter");

	}

	@Test
	public void testPorSentenceSplitter() {
		logger.info("Testing por sentence splitter");

		SentenceSplitter sentenceSplitter = sentenceSplitterFactory.getSentenceSplitter("por");
		testSentenceSplitter(sentenceSplitter, 
				"Isto é um parágrafo. Contém várias frases. «Mas porquê,» perguntas tu?", 
						3,
				"por sentence splitter");
	}

	@Test
	public void testSpaSentenceSplitter() {
		logger.info("Testing spa sentence splitter");

		SentenceSplitter sentenceSplitter = sentenceSplitterFactory.getSentenceSplitter("spa");
		testSentenceSplitter(sentenceSplitter, 
				"La UE ofrece una gran variedad de empleos en un entorno multinacional y multilingüe."
				+ " La Oficina Europea de Selección de Personal (EPSO) se ocupa de la contratación, sobre todo mediante oposiciones generales.", 
						2,
				"por sentence splitter");
	}

	//	


}
