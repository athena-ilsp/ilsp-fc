package gr.ilsp.fc.utils;

import java.io.IOException;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public  class FCStringUtils {
	private static final String delimiters = "-|;|!| |\\?|,|\\.|\\)|\\(|\\[|\\]";
	private static final String NOT_A_LETTER_PATTERN = "\\P{L}+";
	private static String CHINESE="zho";
	private static final Logger LOGGER = Logger.getLogger(FCStringUtils.class);

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
		if (StringUtils.isBlank(string))
			return 0;
		else 
			return getWords(string).size();
	}

	public static List<String> getWords(String string) {
		if (StringUtils.isBlank(string))
			return null;
		else 
			return Arrays.asList(string.split(NOT_A_LETTER_PATTERN));
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
		if (count<thresh)	
			return false;
		else
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

	public static int countTokens(String text, String identifiedlanguage){
		int length_in_tok=0;
		if (identifiedlanguage.equals(CHINESE)){
			try {
				length_in_tok =  TopicTools.getStems(text, identifiedlanguage).size();
			} catch (IOException e) {
				LOGGER.warn("Problem in counting tokens in "+CHINESE);
				e.printStackTrace();
			}
		}else
			length_in_tok = FCStringUtils.countTokens(text);
		return length_in_tok;
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

	public static String[][] map2array(Map<String, Integer> map) {
		String[][] arr = new String[map.size()][2];
		Set entries = map.entrySet();
		Iterator entriesIterator = entries.iterator();
		int i = 0;
		while(entriesIterator.hasNext()){
			Map.Entry mapping = (Map.Entry) entriesIterator.next();
			arr[i][0] = mapping.getKey().toString();
			arr[i][1] = mapping.getValue().toString();
			i++;
		}
		return arr;
	}

}
