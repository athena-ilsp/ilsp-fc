package gr.ilsp.fc.utils.sentencesplitters;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.process.DocumentPreprocessor;

public class StanfordCoreNlpSentenceSplitter extends SentenceSplitter {

	@Override
	public List<String> getSentences(String text, int paragraphMode)
			throws IOException {
		Reader reader = new StringReader(text);
		DocumentPreprocessor dp = new DocumentPreprocessor(reader);
		
		List<String> sentenceList = new ArrayList<String>();

		for (List<HasWord> sentence : dp) {
		   String sentenceString = Sentence.listToString(sentence);
		   sentenceList.add(sentenceString.toString());
		}
		return sentenceList;
	}

	@Override
	public void setAbbreviationsURL(URL abbreviationsURL) {
		// TODO Auto-generated method stub
		
	}



}
