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
import org.apache.commons.lang3.StringUtils;

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
import gr.ilsp.nlp.commons.Constants;

public class MorphAdornerSentenceSplitter extends SentenceSplitter {

	private static final String TXT_SUFFIX = ".txt";
	private static final String DEFAULT_ICU_SEGMENTATION_RULES = "/sentence-splitters/icu_segmentation_rules";
	private static final Logger logger = LoggerFactory.getLogger(MorphAdornerSentenceSplitter.class);
	private String lang;
	private Set<String> noLatinPunctuationLangs = new HashSet<String>();
	Matcher startsWithClosePunctMatcher = Pattern.compile("[”›』›»\\p{Pe}].*").matcher(""); //\p{Pe} or \p{Close_Punctuation}: any kind of closing bracket. 
	Matcher endsWithPossibleEnumΑ = Pattern.compile("\\s*([0-9]{1,2}||[\\p{L}]{1,2})[\\.\\)]\\s*").matcher(""); 
	Matcher endsWithPossibleEnumB = Pattern.compile(".*\\s*[‹«]([0-9]{1,2}||[\\p{L}]{1,2})[\\.\\)]\\s*").matcher("");
	Matcher endsWithDigPunctMatcher = Pattern.compile(".*[0-9][\\.\\,]\\s*").matcher(""); 
	Matcher startsWithDigPercMatcher = Pattern.compile("[0-9][\\.\\,]?[0-9]*%.*").matcher(""); 

	Matcher endsWithCapsDotMatcher = Pattern.compile(".*\\s*[‹«]*([\\p{Lu}]{2,}\\.)").matcher("");
	Matcher startsWithCapsDotMatcher = Pattern.compile("([\\p{Lu}]{2,}\\.).*").matcher(""); 

	Matcher endsWithPossibleInitDotMatcher = Pattern.compile(".*\\s[‹«]*[\\p{Lu}]\\.").matcher("");
	Matcher startsWithPossibleNameMatcher = Pattern.compile("\\s*[\\p{Lu}][\\p{Ll}]{2,}.*").matcher(""); 

	Matcher startsWithNumberMatcher = Pattern.compile("[0-9].*").matcher("");
	Matcher endsWithNumberMatcher = Pattern.compile(".*[0-9][%‰‱°\\,\\.]*").matcher("");
	private static final String COLON = ":"; 	
	private static final String REPL = "\u2AF6"; // One day, this will hurt you thrice :-)
	// Compiled regular expression to match
	// an abbreviation.
	Matcher abbreviationMatcher = Pattern.compile(
			// "([A-Z,a-z]\\.([A-Z,a-z,0-9]\\.)*)|([A-Z][bcdfghj-np-tvxz]+\\.)"
			"^" + "([\\p{Lu}]{1,3}\\.)+" + "|" + "([\\p{Lu}]\\.[\\p{Lu}]+\\.)" + "$").matcher("");

	Matcher numberMatcher1 = Pattern.compile("^['\\+\\-\\.,\\p{Sc}]*([0-9\\p{N}]+[0-9\\p{N},\\.]*)[%‰‱°\\p{Sc}′'’]*(\\-[\\p{N}0-9]+[\\p{N}0-9,\\.]*)*[%‰‱°\\p{Sc}′'’]*$").matcher("");
	//Matcher numberMatcher2 = Pattern.compile("^[0-9]+.[0-9]+[%‰‱°]$").matcher("");

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

	//@Override
	public List<List<String>> getTokens(String text)	throws IOException {
		
		List<List<String>> splitterSents = splitter.extractSentences(text, tokenizer);
		return splitterSents;
	}
	
	
	@Override
	public List<String> getSentences(String text, int paragraphMode)
			throws IOException {
		
		//List<List<String>> splitterSents = splitter.extractSentences(text, tokenizer);
		
		List<List<String>> splitterSents = new ArrayList<List<String>>();
		if (splitOnColon) {
			text = splitOnColon(text, splitterSents);
		} else {
			splitterSents = splitter.extractSentences(text, tokenizer);
		}
				
		//logger.debug(splitterSents.toString());
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
		// logger.debug("Looping over " + splitterSents.size() + " sentences.");
		int prevSentEnd = 0;

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
				boolean firstSent=false;
				//if (paraSents.size()-1==0) {
				if (prevSentEnd==0) {
					firstSent=true;
				}
				String prevSent = paraSents.get(paraSents.size()-1);
				String sent = text.substring(start, end);
				//logger.info(prevSent);
				//logger.info(sent);
//				logger.info(firstSent+"");
				if (startsWithClosePunctMatcher.reset(sent).matches()) {
					paraSents.set(paraSents.size()-1, prevSent+sent);
				} else if (endsWithDigPunctMatcher.reset(prevSent).matches() && startsWithDigPercMatcher.reset(sent).matches()) {
					paraSents.set(paraSents.size()-1, prevSent+sent);
				} else if (endsWithCapsDotMatcher.reset(prevSent).matches()	&& startsWithCapsDotMatcher.reset(sent).matches()) {
					paraSents.set(paraSents.size()-1, prevSent+sent);
				} else if (endsWithPossibleInitDotMatcher.reset(prevSent).matches() && startsWithPossibleNameMatcher.reset(sent).matches()) {
					paraSents.set(paraSents.size()-1, prevSent+sent);
				} else if (endsWithPossibleEnumΑ.reset(prevSent).matches()) {
					paraSents.set(paraSents.size()-1, prevSent+sent);
				} else if (endsWithPossibleEnumB.reset(prevSent).matches()) {
					paraSents.set(paraSents.size()-1, prevSent+sent);
				} else if (firstSent==false && prevSentEnd==start) {
					String[] prevTokens = StringUtils.split(prevSent);
					String[] tokens = StringUtils.split(sent);
					if (prevTokens.length > 0 && tokens.length > 0 && isTokenNotToBeSplit(prevTokens[prevTokens.length-1] + tokens[0])) { 	
//						logger.info("PrevSentEnd : " + prevSentEnd);
//						logger.info("SentStart : " + start);
//						logger.info(prevTokens[prevTokens.length-1] + tokens[0]);
						paraSents.set(paraSents.size()-1, prevSent+sent);
					} else if (prevTokens.length > 0 && tokens.length > 0 && isTokenNotToBeSplit(prevTokens[prevTokens.length-1], tokens[0])) { 	
//						logger.info("PrevSentEnd : " + prevSentEnd);
//						logger.info("SentStart : " + start);
//						logger.info(prevTokens[prevTokens.length-1] + tokens[0]);
						paraSents.set(paraSents.size()-1, prevSent+sent);
					} else {
						paraSents.add(text.substring(start, end));
					}
				} else {
					paraSents.add(text.substring(start, end));
				}
			}
			prevSentEnd = end;
		}
		return paraSents;
	}

	/**
	 * @param text
	 * @param splitterSents
	 * @return
	 */
	private String splitOnColon(String text, List<List<String>> splitterSents) {
		text = StringUtils.replace(text, (COLON + Constants.SPACE), (REPL + Constants.SPACE));
		for (List<String> sentence : splitter.extractSentences(text, tokenizer)) {
			List<String> tempSent = new ArrayList<String>();
			for (int i = 0; i < sentence.size(); i++) {
				if (sentence.get(i).contains(REPL)) {
					tempSent.add(sentence.get(i).replace(REPL, COLON));
				} else {
					tempSent.add(sentence.get(i));						
				}
			}
			splitterSents.add(tempSent);
		}
		text = StringUtils.replace(text, (REPL + Constants.SPACE), (COLON + Constants.SPACE));
		return text;
	}

	
	
	
	
	
	
	private boolean isTokenNotToBeSplit(String string1, String string2) {		
		if (endsWithNumberMatcher.reset(string1).matches() &&  startsWithNumberMatcher.reset(string2).matches() ) {
			return true;
		}
		return false; 
	}

	private boolean isTokenNotToBeSplit(String string) {
		//logger.info(string +  " " + abbreviationMatcher.reset(string).matches() );
		return 
				abbreviationMatcher.reset(string).matches() 	
				|| 
				numberMatcher1.reset(string).matches() 
				;
	}



	@Override
	public void setAbbreviationsURL(URL abbreviationsURL) {
		Abbreviations abbreviations = new Abbreviations();
		abbreviations.loadAbbreviations(abbreviationsURL.toString());
		splitter.setAbbreviations(abbreviations);
		tokenizer.setAbbreviations(abbreviations);
	}

}
