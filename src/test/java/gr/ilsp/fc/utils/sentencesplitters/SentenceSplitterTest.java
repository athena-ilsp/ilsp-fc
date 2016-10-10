package gr.ilsp.fc.utils.sentencesplitters;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gr.ilsp.fc.utils.AnalyzerTest;

public class SentenceSplitterTest {
	
	String testDir = "/sentence-splitters/";

	private static final Logger logger = LoggerFactory.getLogger(SentenceSplitterTest.class);
	
	SentenceSplitterFactory sentenceSplitterFactory ;

	@Before
	public void setUp() throws Exception {
		sentenceSplitterFactory = new SentenceSplitterFactory();
	}

	private void testSentenceSplitter(SentenceSplitter sentenceSplitter, String input, int numOfSents, String testName) {
		try {
			List<String> sentences = sentenceSplitter.getSentences(input, 1);
			if (numOfSents!=sentences.size()) {
				logger.info( sentences.size() + " != (expected) " + numOfSents);
				logger.info(input);
				int i = 1;
				for (String sentence: sentences) {
					System.out.println(i + " " +sentence.trim());
					i++;
				}
			}
			Assert.assertEquals(testName + ": ", numOfSents, sentences.size());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}


	@Test
	public void testLangSentenceSplitters() throws IOException {
		List<String> langs = Arrays.asList(new String[]{"ell", "fra", "eng", "deu", "spa", "por"});
		
		for (String lang: langs ) {
			logger.info("Testing " + lang	+ " sentence splitter");
			SentenceSplitter sentenceSplitter = sentenceSplitterFactory.getSentenceSplitter(lang);
			InputStream in  =  AnalyzerTest.class.getResource(testDir+lang).openStream();
			List<Pair<String, Integer>> stringSentsList = getStringSents(in);
			in.close();
			for (Pair<String, Integer> stringSents: stringSentsList) {
				testSentenceSplitter(sentenceSplitter, stringSents.getLeft(),stringSents.getRight(), lang + " sentence splitter");
			}
		}
	}

	private List<Pair<String, Integer>> getStringSents(InputStream in) throws IOException {
		List<Pair<String, Integer>> stringSents = new ArrayList<Pair<String, Integer>>();
		for (String line:  IOUtils.readLines(in, "UTF-8")) {
			if (StringUtils.isAnyBlank(line) || line.startsWith("#")) {
				continue;
			} else {
				String[] fields = StringUtils.split(line, '\t');
				stringSents.add(Pair.of(fields[0], Integer.valueOf(fields[1])));
			}
		}
		return stringSents;
	}

}
