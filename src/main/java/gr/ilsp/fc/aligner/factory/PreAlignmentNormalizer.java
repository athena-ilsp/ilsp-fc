package gr.ilsp.fc.aligner.factory;

//import gr.ilsp.fc.utils.ContentNormalizer;
import gr.ilsp.nlp.commons.Constants;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class PreAlignmentNormalizer {

	//private static final String ELLIPSIS_IN_BRACKETS = "[…]";
	//private static final Logger logger = LoggerFactory.getLogger(ContentNormalizer.class);
	private static final String EMPTYSTR = "";
	//private static final Pattern lowercaseLetterP = Pattern.compile(".*\\p{Ll}.*");

	private static final String ABBRMASK = "ILSPABBRMASK";
	private static final String CCBYVERSIONMASK = "ILSPCCBYVERSIONMASK";

	private static Matcher ccBYMatcher = Pattern.compile("(^|.*)(CC[- ]+BY[- ](SA|NC|ND)+([- ](SA|NC|ND))*([- ]([234][\\. ][05])))(.*|$)", Pattern.CASE_INSENSITIVE).matcher("");	
	private static Matcher ccBYMatcherVersion = Pattern.compile("(.*)([ -])(\\d)(\\.)([05])").matcher("");
	private static Matcher punctOnlyMatcher = Pattern.compile("[\\p{P}\\p{S}\\p{Z}\\p{C}]+").matcher("");
	private static Matcher punctDigitOnlyMatcher = Pattern.compile("[\\p{P}\\p{S}\\p{N}\\p{Z}\\p{C}]+").matcher("");

	/**
	 * Pattern for abbreviations
	 */
	protected static final Pattern abbreviationPattern = Pattern.compile
 	(
 			// "([A-Z,a-z]\\.([A-Z,a-z,0-9]\\.)*)|([A-Z][bcdfghj-np-tvxz]+\\.)"
 			"^" +
 			"([\\p{Lu}]{1,3}\\.)+" +
 			"|" +
 			"([\\p{Lu}]{1,2}τ[\\p{Lu}]{1,2})+" + // ΚτΠ, ΤτΕ
 			"|" +
 			"([\\p{Lu}]\\.[\\p{Lu}]+\\.)" +
 			"$"
 	);

	/**
	 * Abbreviation matcher.
	 */
 	protected Matcher abbreviationMatcher	= abbreviationPattern.matcher( EMPTYSTR );
 	// FIXME: The above pattern/matcher is duplicate code from DefaultTokenTypeGuesser 

 	
	/**
	 * Pattern for token with one or two token initials.
	 */
	protected static final Pattern initialPattern	=
		Pattern.compile( "(\\p{Lu}\\.){1,2}");

	/**
	 * Matcher for initials.
	 */
	protected static Matcher initialMatcher	=
		initialPattern.matcher( EMPTYSTR );

	/**
	 * Pattern to match URLs and emails.
	 */
	protected static final Pattern URLEMAILPATTERN	= Pattern.compile(
			// Email
			"([a-zA-Z][\\w\\.-]*[a-zA-Z0-9]"+
			"@[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z])"+
			"|"+
			// URL1
			"((www|ftp)\\.[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])" +
			"|"+
			// URL2
			"((https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])" ,
			Pattern.CASE_INSENSITIVE+Pattern.UNICODE_CASE );

	/**
	 * Matcher to match URLs.
	 */
	protected Matcher urlEmailMatcher = URLEMAILPATTERN.matcher(EMPTYSTR);
	
	public static String maskTokensWithInitials (String sentence) {
		List<String> tokens = Arrays.asList(StringUtils.split(sentence));
		for (int i=0; i<tokens.size(); i++) {
			String token = tokens.get(i);
			if (initialMatcher.reset(token).matches()) {
				tokens.set(i, token+ABBRMASK);
			}
		}
		return StringUtils.join(tokens, Constants.SPACE);
	}

	public static String unMaskSentence(String sentence) {
		sentence = StringUtils.replace(sentence, ABBRMASK, EMPTYSTR);
		sentence = StringUtils.replace(sentence, CCBYVERSIONMASK, Constants.DOT);
		return sentence;
	}

	public static void unMaskSentences(List<String> sentences) {
		for (int i=0; i<sentences.size(); i++) {
			sentences.set(i, unMaskSentence(sentences.get(i)));
		}
	}

	public static String normalizeSentenceForAlignment (String sentence) {
		
		maskTokensWithInitials(sentence);
		//logger.info(sentence);

		// CCBY normalizer
		if (!ccBYMatcher.reset(sentence).matches()) {
			return sentence;
		}
		//logger.info("Matched!");
		//logger.info(sentence);

		String match;
		StringBuilder sentenceSB = new StringBuilder();
		while (ccBYMatcher.reset(sentence).matches()) {
			sentenceSB.append(ccBYMatcher.group(1));
			match = ccBYMatcher.group(2);
			match = StringUtils.capitalize(match);
			match = StringUtils.replace(match, EMPTYSTR , Constants.HYPHEN);
			//logger.info(match);
			if (ccBYMatcherVersion.reset(match).matches()) {	
				match = ccBYMatcherVersion.group(1) + Constants.SPACE  + ccBYMatcherVersion.group(3) +CCBYVERSIONMASK + ccBYMatcherVersion.group(5); 
			}			
			//logger.info(match);

			sentenceSB.append(match);
			sentence = ccBYMatcher.group(8);
			//logger.info(sentence);
		}
		sentenceSB.append(sentence);
		//logger.info(sentenceSB.toString());		
		return sentenceSB.toString();
	}

	public static void mergeUpPunctutionOnlySentences(List<String> sentences) {
		for (int i =0; i<sentences.size(); i++) {
			String sentence = sentences.get(i);
			if (punctOnlyMatcher.reset(sentence).matches() &&  i>0) {
			//	logger.info(sentence);
				sentences.set(i-1, sentences.get(i-1) + Constants.SPACE + sentence);
				sentences.set(i, EMPTYSTR);
			} else if (punctDigitOnlyMatcher.reset(sentence).matches() &&  i< (sentences.size()-1)) {
				//logger.info(sentence);
				//logger.info(sentences.toString());
				sentences.set(i+1, sentence + Constants.SPACE +sentences.get(i+1)  );
				sentences.set(i, EMPTYSTR);
			} else if (punctDigitOnlyMatcher.reset(sentence).matches() ) {
				//logger.info(sentence);
				//logger.info(sentences.toString());
				sentences.set(i-1, sentences.get(i-1) + Constants.SPACE + sentence);
				sentences.set(i, EMPTYSTR);
			}
			
		}
		for (Iterator<String> iter = sentences.iterator(); iter.hasNext(); ) {
		    if (iter.next().equals(EMPTYSTR)) {
		    	iter.remove();
		    }
		}
	}
	
}
