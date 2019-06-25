package gr.ilsp.fc.utils.sentencesplitters;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.trees.international.pennchinese.ChineseUtils;
import edu.stanford.nlp.util.Generics;

/**
 * This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

 * Altered code from GPL'ed edu.stanford.nlp.process.ChineseDocumentToSentenceProcessor, 
 * mainly skipping normalization and removing whitespace
 * 
 * 
 */
public class ChineseSentenceSplitter extends SentenceSplitter {

	private static final Logger logger = LoggerFactory.getLogger(ChineseSentenceSplitter.class);
	
	private static final Set<Character> fullStopsSet = Generics.newHashSet(Arrays.asList(new Character[]{'\u3002', '\uff01', '\uff1f', '!', '?'}));
	// not \uff0e . (too often separates English first/last name, etc.)

	private static final Set<Character> rightMarkSet = Generics.newHashSet(Arrays.asList(new Character[]{'\u201d', '\u2019', '\u300b', '\u300f', '\u3009', '\u300d', '\uff1e', '\uff07', '\uff09', '\'', '"', ')', ']', '>'}));

	public ChineseSentenceSplitter() {
		super();
		logger.debug("Initializing Chinese Sentence Segmenter");

	}

	@Override
	public List<String> getSentences(String text, int paragraphMode) throws IOException {
		List<String> sentences =  fromPlainText(text, false);
		return sentences;
	}	

	public static List<String> fromPlainText(String contentString, boolean segmented) throws IOException {
		contentString = ChineseUtils.normalize(contentString,
				ChineseUtils.LEAVE,
				ChineseUtils.ASCII);

		String sentenceString = "";

		char[] content = contentString.toCharArray();
		boolean sentenceEnd = false;
		List<String> sentenceList = new ArrayList<String>();

		int lastCh = -1;
		for (Character c : content) {
			// EncodingPrintWriter.out.println("Char is |" + c + "|", Constants.UTF8);
			String newChar = c.toString();

			if (sentenceEnd == false) {
				if (segmented && fullStopsSet.contains(c) &&
						(lastCh == -1 || Character.isSpaceChar(lastCh))) {
					// require it to be a standalone punctuation mark -- cf. URLs
					sentenceString += newChar;
					sentenceEnd = true;
				} else if ( ! segmented && fullStopsSet.contains(c)) {
					// EncodingPrintWriter.out.println("  End of sent char", Constants.UTF8);
					sentenceString += newChar;
					sentenceEnd = true;
				} else {
					sentenceString += newChar;
				}
			} else { // sentenceEnd == true
				if (rightMarkSet.contains(c)) {
					sentenceString += newChar;
					// EncodingPrintWriter.out.println("  Right mark char", Constants.UTF8);
				} else if (newChar.matches("\\s")) {
					sentenceString += newChar;
				} else if (fullStopsSet.contains(c)) {
					// EncodingPrintWriter.out.println("  End of sent char (2+)", Constants.UTF8);
					sentenceString += newChar;
				} else { // otherwise
					if (sentenceString.length() > 0) {
						sentenceEnd = false;
					}
					//sentenceString = removeWhitespace(sentenceString, segmented);
					if (sentenceString.length() > 0) {
						//System.err.println("<<< "+sentenceString+" >>>");
						sentenceList.add(sentenceString);
					}
					sentenceString = "";
					sentenceString += newChar;
				}
			}
			lastCh = c.charValue();
		} // end for (Character c : content)

		//sentenceString = removeWhitespace(sentenceString, segmented);
		if (sentenceString.length() > 0) {
			//System.err.println("<<< "+sentenceString+" >>>");
			sentenceList.add(sentenceString);
		}
		return sentenceList;
	}



	@Override
	public void setAbbreviationsURL(URL abbreviationsURL) {
		// TODO Auto-generated method stub
		
	}


}
