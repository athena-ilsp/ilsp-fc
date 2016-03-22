package gr.ilsp.fc.utils.sentencesplitters;


import com.ibm.icu.text.RuleBasedBreakIterator;

import edu.northwestern.at.morphadorner.corpuslinguistics.sentencesplitter.SentenceSplitterIterator;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;

import org.apache.commons.io.IOUtils;


/**	BreakIterator-based sentence splitter iterator. */

public class ICU4JRBBISentenceSplitterIterator implements
		SentenceSplitterIterator {
	/**
	 * BreakIterator used to iterate over sentences.
	 */

	protected RuleBasedBreakIterator sentenceExtractor;

	/** Start of current sentence. */

	protected int start = RuleBasedBreakIterator.DONE;

	/** End of current sentence. */

	protected int end = RuleBasedBreakIterator.DONE;

	/** Text to break up. */

	protected String text;

	/**
	 * Create sentence iterator.
	 */

	public ICU4JRBBISentenceSplitterIterator() {
		sentenceExtractor = (RuleBasedBreakIterator) RuleBasedBreakIterator.getSentenceInstance(new Locale("el",
				"GR"));
	}

	/**
	 * Create sentence iterator with specified locale.
	 * 
	 * @param locale
	 *            The locale.
	 */

	public ICU4JRBBISentenceSplitterIterator(Locale locale) {
		sentenceExtractor = (RuleBasedBreakIterator) RuleBasedBreakIterator.getSentenceInstance(locale);
	}

	/**
	 * Create sentence iterator over text.
	 * 
	 * @param text
	 *            The text from which to extract sentences.
	 */

	public ICU4JRBBISentenceSplitterIterator(String text) {
		sentenceExtractor = (RuleBasedBreakIterator) RuleBasedBreakIterator.getSentenceInstance(new Locale("el",
				"GR"));

		setText(text);
	}

	/**
	 * Create sentence iterator over text with specified locale.
	 * 
	 * @param text
	 *            The text from which to extract sentences.
	 * @param locale
	 *            The locale.
	 */

	public ICU4JRBBISentenceSplitterIterator(String text, Locale locale) {
		sentenceExtractor = (RuleBasedBreakIterator) RuleBasedBreakIterator.getSentenceInstance(locale);

		setText(text);
	}


	/**
	 * Create sentence iterator with specified locale and segmentation rules.
	 * 
	 * @throws IOException 
	 * @param locale
	 * @param segmentationRulesURL
	 */
	public ICU4JRBBISentenceSplitterIterator(Locale locale, URL segmentationRulesURL) throws IOException  {
		InputStream in = segmentationRulesURL.openStream();
		String segmentationRulesString = IOUtils.toString(in);
		sentenceExtractor = new RuleBasedBreakIterator(segmentationRulesString);
		in.close();
	}

	/**
	 * Create sentence iterator with specified locale and segmentation rules.
	 * 
	 * @throws IOException 
	 * @param locale
	 * @param segmentationRulesURL
	 */
	public ICU4JRBBISentenceSplitterIterator(URL segmentationRulesURL) throws IOException  {
		InputStream in = segmentationRulesURL.openStream();
		String segmentationRulesString = IOUtils.toString(in);
		sentenceExtractor = new RuleBasedBreakIterator(segmentationRulesString);
		in.close();
	}

	
	/**
	 * Set the text to split.
	 * 
	 * @param text
	 *            Text to split.
	 */

	public void setText(String text) {
		this.text = text;

		sentenceExtractor.setText(this.text);

		start = sentenceExtractor.first();
		end = sentenceExtractor.next();
	}

	/**
	 * Check if there is another sentence available.
	 * 
	 * @return true if another sentence is available.
	 */

	public boolean hasNext() {
		return (end != RuleBasedBreakIterator.DONE);
	}

	/**
	 * Return next sentence.
	 * 
	 * @return next sentence, or null if none.
	 */

	public String next() {
		String result = null;

		if (end != RuleBasedBreakIterator.DONE) {
			result = text.substring(start, end);
			start = end;
			end = sentenceExtractor.next();
		}

		return result;
	}

	/**
	 * Return next sentence without advancing sentence pointer.
	 * 
	 * @return next sentence, or null if none.
	 */

	public String peek() {
		String result = null;

		if (end != RuleBasedBreakIterator.DONE) {
			result = text.substring(start, end);
		}

		return result;
	}
}
