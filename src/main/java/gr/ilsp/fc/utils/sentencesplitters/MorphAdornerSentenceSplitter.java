package gr.ilsp.fc.utils.sentencesplitters;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.northwestern.at.morphadorner.corpuslinguistics.abbreviations.Abbreviations;
import edu.northwestern.at.morphadorner.corpuslinguistics.sentencesplitter.DefaultSentenceSplitter;
import edu.northwestern.at.morphadorner.corpuslinguistics.sentencesplitter.ICU4JBreakIteratorSentenceSplitter;
import edu.northwestern.at.morphadorner.corpuslinguistics.sentencesplitter.SentenceSplitterIterator;
import edu.northwestern.at.morphadorner.corpuslinguistics.tokenizer.DefaultWordTokenizer;
import edu.northwestern.at.morphadorner.corpuslinguistics.tokenizer.ICU4JBreakIteratorWordTokenizer;
import edu.northwestern.at.morphadorner.corpuslinguistics.tokenizer.PreTokenizer;
import edu.northwestern.at.morphadorner.corpuslinguistics.tokenizer.WordTokenizer;

public class MorphAdornerSentenceSplitter extends SentenceSplitter {

	private static final String TXT_SUFFIX = ".txt";
	private static final String DEFAULT_ICU_SEGMENTATION_RULES = "/sentence-splitters/icu_segmentation_rules";
	private static final Logger logger = LoggerFactory.getLogger(MorphAdornerSentenceSplitter.class);
	private String lang;
	private Set<String> noLatinPunctuationLangs = new HashSet<String>();
	Matcher startsWithClosePunctMatcher = Pattern.compile("[”›』›»\\p{Pe}].*").matcher(""); //\p{Pe} or \p{Close_Punctuation}: any kind of closing bracket. 
	Matcher isPossibleEnumMatcher = Pattern.compile("\\s*([0-9]+)[\\.\\)]\\s*").matcher(""); 
	
	edu.northwestern.at.morphadorner.corpuslinguistics.sentencesplitter.SentenceSplitter splitter;
	PreTokenizer preTokenizer;
	WordTokenizer tokenizer;
	SentenceSplitterIterator sentenceSplitterIterator;

	public MorphAdornerSentenceSplitter(String lang)  {
		
		noLatinPunctuationLangs.addAll(Arrays.asList(new String[] { "rus",
				"ben", "jpn", "ori", "ara", "fas", "urd", "heb", "amh", "kor",
				"hin", "mya", "khm", "pus" }));		
				
		this.lang = lang;
		logger.debug(this.lang);
	
		if (noLatinPunctuationLangs.contains(lang)  ) {
			tokenizer = new ICU4JBreakIteratorWordTokenizer(Locale.forLanguageTag(lang));
			splitter = new ICU4JBreakIteratorSentenceSplitter(Locale.forLanguageTag(lang)  );
		} else  {
			logger.debug("Loading default segmenters");
			splitter = new  DefaultSentenceSplitter();
			String segmentationRules = getSegmentationRulesURL(lang);
			try {
				sentenceSplitterIterator = new ICU4JRBBISentenceSplitterIterator(this.getClass().getResource(segmentationRules));
				splitter.setSentenceSplitterIterator(sentenceSplitterIterator);
			} catch (Exception e) {
				logger.error("Cannot load segmentation rules. Skipping.");
			}
			tokenizer = new DefaultWordTokenizer();
		}
	}

	/**
	 * @param lang
	 * @return
	 */
	private String getSegmentationRulesURL(String lang) {
		String segmentationRules = DEFAULT_ICU_SEGMENTATION_RULES + TXT_SUFFIX;
		logger.debug("Checking for language specific segmentation rules for " + lang);
		if (this.getClass().getResource(DEFAULT_ICU_SEGMENTATION_RULES + "-" + lang + TXT_SUFFIX) != null) {
			logger.debug("Loading language specific segmentation rules for " + lang);
			segmentationRules = DEFAULT_ICU_SEGMENTATION_RULES + "-" + lang + TXT_SUFFIX;
		}
		return segmentationRules;
	}

	@Override
	public List<String> getSentences(String text, int paragraphMode)
			throws IOException {
		
		List<List<String>> splitterSents = splitter.extractSentences(text, tokenizer);
		List<String> paraSents = new ArrayList<String>();

		// No need to bother looking for offsets if sentsize = 1
		if (splitterSents.size()==1) {
			//logger.info("Just one: " + text);
			paraSents.add(text);	
			return paraSents;
		}
		
		// Get sentence start and end
		// offsets in input text.
		//logger.info("In text " + text );
		int[] sentenceOffsets  = splitter.findSentenceOffsets(text, splitterSents);
		// logger.debug("After finding " + splitterSents.size() + " offsets.");
		// Loop over sentences.
		for (int i = 0; i < splitterSents.size(); i++) {

			// Get start and end offset of
			// sentence text. Note: the
			// end is the end + 1 since that
			// is what substring wants.

			int start = sentenceOffsets[i];
			int end = sentenceOffsets[i + 1];

			// Get sentence text.
			//logger.debug(text.substring(start, end));

			// FIXME: Ugly hack to fixup certain obvious errors by sentence splitter. Remove once solved in the ICU rules.
			if (paraSents.isEmpty()) {
				paraSents.add(text.substring(start, end));				
			} else {
				boolean prevSentIsFirst=false;
				if (paraSents.size()-1==0) {
					prevSentIsFirst=true;
				}
				
				String prevSent = paraSents.get(paraSents.size()-1);
				String sent = text.substring(start, end);
//				logger.info(prevSent);
//				logger.info(sent);
				if (startsWithClosePunctMatcher.reset(sent).matches()) {
					paraSents.set(paraSents.size()-1, prevSent+sent);
				} else if (isPossibleEnumMatcher.reset(prevSent).matches()) {
					paraSents.set(paraSents.size()-1, prevSent+sent);
				} else {
					paraSents.add(text.substring(start, end));
				}
			}
			
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
