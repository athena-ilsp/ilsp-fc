package gr.ilsp.fc.utils.sentencesplitters;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.northwestern.at.morphadorner.corpuslinguistics.abbreviations.Abbreviations;
import edu.northwestern.at.morphadorner.corpuslinguistics.sentencesplitter.DefaultSentenceSplitter;
import edu.northwestern.at.morphadorner.corpuslinguistics.sentencesplitter.ICU4JBreakIteratorSentenceSplitter;
import edu.northwestern.at.morphadorner.corpuslinguistics.tokenizer.DefaultPreTokenizer;
import edu.northwestern.at.morphadorner.corpuslinguistics.tokenizer.DefaultWordTokenizer;
import edu.northwestern.at.morphadorner.corpuslinguistics.tokenizer.ICU4JBreakIteratorWordTokenizer;
import edu.northwestern.at.morphadorner.corpuslinguistics.tokenizer.PreTokenizer;
import edu.northwestern.at.morphadorner.corpuslinguistics.tokenizer.WordTokenizer;

public class MorphAdornerSentenceSplitter extends SentenceSplitter {

	private String lang;
	
	private Set<String> noLatinPunctuationLangs = new HashSet<String>();


	
	edu.northwestern.at.morphadorner.corpuslinguistics.sentencesplitter.SentenceSplitter splitter;
	PreTokenizer preTokenizer;
	WordTokenizer tokenizer;
	private static final Logger logger = LoggerFactory
			.getLogger(MorphAdornerSentenceSplitter.class);

	public MorphAdornerSentenceSplitter(String lang) {
		
		noLatinPunctuationLangs.addAll(Arrays.asList(new String[] { "rus",
				"ben", "jpn", "ori", "ara", "fas", "urd", "heb", "amh", "kor",
				"hin", "mya", "khm", "pus" }));		
				
		this.lang = lang;
		logger.debug(this.lang);
		preTokenizer = new DefaultPreTokenizer();
		
		if (noLatinPunctuationLangs.contains(lang)) {
			tokenizer = new ICU4JBreakIteratorWordTokenizer(Locale.forLanguageTag(lang));
			splitter = new ICU4JBreakIteratorSentenceSplitter(Locale.forLanguageTag(lang));
		} else  {
			logger.debug("Loading default segmenters");
			splitter = new DefaultSentenceSplitter();
			tokenizer = new DefaultWordTokenizer();
		}
	}

	@Override
	public List<String> getSentences(String text, int paragraphMode)
			throws IOException {
		List<List<String>> splitterSents = splitter.extractSentences(text, tokenizer);
		List<String> paraSents = new ArrayList<String>();

		// No need to bother looking for offsets if sentsize = 1
		if (splitterSents.size()==1) {
			paraSents.add(text);	
			return paraSents;
		}
		
		// Get sentence start and end
		// offsets in input text.
		//logger.info("In text " + text );
		int[] sentenceOffsets  = splitter.findSentenceOffsets(text, splitterSents);
		//logger.info("After finding " + splitterSents.size() + " offsets.");
		// Loop over sentences.
		for (int i = 0; i < splitterSents.size(); i++) {

			// Get start and end offset of
			// sentence text. Note: the
			// end is the end + 1 since that
			// is what substring wants.

			int start = sentenceOffsets[i];
			int end = sentenceOffsets[i + 1];

			// Get sentence text.
			//logger.info(text.substring(start, end));
			paraSents.add(text.substring(start, end));
			
		}
		return paraSents;
	}

	@Override
	public void setAbbreviationsURL(URL abbreviationsURL) {
		Abbreviations abbreviations = new Abbreviations();
		abbreviations.loadAbbreviations(abbreviationsURL.toString());
		splitter.setAbbreviations(abbreviations);
		tokenizer.setAbbreviations(abbreviations);
	}

}
