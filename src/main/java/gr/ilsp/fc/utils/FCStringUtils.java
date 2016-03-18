package gr.ilsp.fc.utils;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;

public  class FCStringUtils {
	private static final String delimiters = "-|;|!| |\\?|,|\\.|\\)|\\(|\\[|\\]";
    private static final String NOT_A_LETTER_PATTERN = "\\P{L}+";

	public static int getGraphemeLength(String str) {
	    BreakIterator it = BreakIterator.getCharacterInstance();
	    it.setText(str);
	    int count = 0;
	    while (it.next() != BreakIterator.DONE) {
	        count++;
	    }
	    return count;
	}
	
	public static int getWordLength(String string) {
		if (StringUtils.isBlank(string)) {
			return 0;
		} else { 
			return getWords(string).size();
		}
	}

	public static List<String> getWords(String string) {
		if (StringUtils.isBlank(string)) {
			return null;
		} else { 
			return Arrays.asList(string.split(NOT_A_LETTER_PATTERN));
		}
	}
	
	
		
	/**
	 * count parts of a string based on specific delimiters
	 * @param str
	 * @param delimiter
	 * @return
	 */
	public static int getPartLength(String str, String delimiter) {
		if (StringUtils.isBlank(str)) 
			return 0;
		if (delimiter.isEmpty())
			return getParts(str, delimiters).size();
		else
			return getParts(str, delimiter).size();
	}
	
	/**
	 * gets parts of a string, using specific delimiters
	 * @param str
	 * @param delimiter: if empty, default delimiters are used
	 * @return
	 */
	public static List<String> getParts(String str, String delimiter) {
		if (StringUtils.isBlank(str)) 
			return null;
		List<String> parts = new ArrayList<String>();
		List<String> res = new ArrayList<String>();
		if (delimiter.isEmpty())
			parts= Arrays.asList(str.split(delimiters));
		else
			parts= Arrays.asList(str.split(delimiter));
		for (String part:parts){
			if (!StringUtils.isBlank(part))
			 res.add(part);
		}
		return res;
	}


	/**
	 * counts tokens (space separated) of a text and compares the number with the thresh 
	 * @param text
	 * @param thresh
	 * @return
	 */
	public static Boolean isLong(String text, int thresh){
		if (thresh<=0)
			return true;
		StringTokenizer st = new StringTokenizer(text);
		int count=st.countTokens();
		if (count<thresh){	
			return false;
		}else
			return true;				
	}

	/**
	 * counts tokens (space separated)
	 * @param text
	 * @return
	 */
	public static int countTokens(String text) {
		StringTokenizer st = new StringTokenizer(text);
		return st.countTokens();
	}
	
	/**
	 * counts tokens (space separated)
	 * @param text
	 * @return
	 */
	public static List<String> getTokens(String text) {
		List<String> tokens = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(text);
		while (st.hasMoreTokens()){
			tokens.add(st.nextToken());
		}
		return tokens;
	}
	
	
	public static Double[] getTokensLength(List<String> tokens) {
		Double[] lens = new Double[tokens.size()];
		for (int ii=0;ii<tokens.size();ii++){
			lens[ii] = (double) tokens.get(ii).length();
		}
		return lens;
	}
	
}
