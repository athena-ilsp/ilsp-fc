package gr.ilsp.fc.utils;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.text.ParseException;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
//.miscellaneous.KeywordMarkerFilter;
import org.apache.lucene.analysis.miscellaneous.KeywordMarkerFilter;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.analysis.util.WordlistLoader;
import org.apache.lucene.util.IOUtils;
import org.apache.lucene.util.Version;

import org.apache.lucene.analysis.hunspell.HunspellStemFilter;
import org.apache.lucene.analysis.hunspell.Dictionary; 
import com.google.common.base.Charsets;

/**
 * {@link Analyzer} for Slovak.
 */
public final class SlovakAnalyzer extends StopwordAnalyzerBase {

	private static final Logger LOGGER = Logger.getLogger(SlovakAnalyzer.class);

	private final CharArraySet stemExclusionSet;
	private Dictionary dictionary = null;

	/** File containing default Slovak stopwords. */
	public final static String DEFAULT_STOPWORD_FILE = "/stopword-lists/slk.txt";

	/**
	 * Returns an unmodifiable instance of the default stop words set.
	 * 
	 * @return default stop words set.
	 */
	public static CharArraySet getDefaultStopSet() {
		return DefaultSetHolder.DEFAULT_STOP_SET;
	}

	/**
	 * Atomically loads the DEFAULT_STOP_SET in a lazy fashion once the outer
	 * class accesses the static final set the first time.;
	 */
	private static class DefaultSetHolder {
		static final CharArraySet DEFAULT_STOP_SET;

		static {
			try {
				DEFAULT_STOP_SET = WordlistLoader.getSnowballWordSet(IOUtils
						.getDecodingReader(SnowballFilter.class,
								DEFAULT_STOPWORD_FILE, Charsets.UTF_8));
			} catch (IOException ex) {
				// default set should always be present as it is part of the
				// distribution (JAR)
				throw new RuntimeException( "Unable to load default stopword set");
			}
		}
	}

	
	/**
	 * Builds an analyzer with the default stop words:
	 * {@link #DEFAULT_STOPWORD_FILE}.
	 * @throws ParseException 
	 * @throws IOException 
	 */
	public SlovakAnalyzer(CharArraySet stopwords) throws IOException, ParseException {
	    this(stopwords, CharArraySet.EMPTY_SET);

	}

	/**
	 * Builds an analyzer with the given stop words.
	 * 
	 * @param matchVersion
	 *            lucene compatibility version
	 * @param stopwords
	 *            a stopword set
	 * @throws ParseException 
	 * @throws IOException 
	 */
	public SlovakAnalyzer(Version matchVersion, CharArraySet stopwords) throws IOException, ParseException {
		this(stopwords, CharArraySet.EMPTY_SET);
	}

	/**
	 * Builds an analyzer with the given stop words. If a non-empty stem
	 * exclusion set is provided this analyzer will add a
	 * {@link KeywordMarkerFilter} before stemming.
	 * 
	 * @param matchVersion
	 *            lucene compatibility version
	 * @param stopwords
	 *            a stopword set
	 * @param stemExclusionSet
	 *            a set of terms not to be stemmed
	 * @throws ParseException 
	 * @throws IOException 
	 */
	public SlovakAnalyzer(CharArraySet stopwords,	CharArraySet stemExclusionSet)  {
		super( stopwords);
		this.stemExclusionSet = CharArraySet.unmodifiableSet(CharArraySet.copy(stemExclusionSet));
		InputStream affixStream = SlovakAnalyzer.class.getResourceAsStream("/hunspell-dictionaries/sk/sk_SK.aff"); 
		InputStream dictStream = SlovakAnalyzer.class.getResourceAsStream("/hunspell-dictionaries/sk/sk_SK.dic"); 
		try {
			dictionary = new Dictionary(affixStream, dictStream);
		} catch (Exception e) {
			LOGGER.error("Could not initialize analyzer correctly: " );
			LOGGER.error(e.getMessage());
		} 
	}

	public SlovakAnalyzer() throws IOException, ParseException {
		this(DefaultSetHolder.DEFAULT_STOP_SET);
	}

	/**
	 * Creates a
	 * {@link org.apache.lucene.analysis.Analyzer.TokenStreamComponents} which
	 * tokenizes all the text in the provided {@link Reader}.
	 * 
	 * @return A
	 *         {@link org.apache.lucene.analysis.Analyzer.TokenStreamComponents}
	 *         built from an {@link StandardTokenizer} filtered with
	 *         {@link StandardFilter}, {@link LowerCaseFilter},
	 *         {@link StopFilter} , {@link KeywordMarkerFilter} if a stem
	 *         exclusion set is provided and {@link SnowballFilter}.
	 */
	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		LOGGER.debug("Creating components  ");
	    Tokenizer tokenizer = new StandardTokenizer();
	    return new TokenStreamComponents(tokenizer, new HunspellStemFilter(tokenizer, dictionary));
	}


}
