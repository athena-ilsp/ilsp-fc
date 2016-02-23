package gr.ilsp.fc.utils;

import java.text.BreakIterator;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public  class FCStringUtils {

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
}
