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
import static org.apache.lucene.analysis.util.StemmerUtil.deleteN;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stemmer for Maltese.
 * <p>
 * Stemming is done in-place for efficiency, operating on a termbuffer.
 * <p>
 * Stemming is defined as:
 * <ul>
 * <li>Removal of attached definite article, conjunction, and prepositions.
 * <li>Stemming of common suffixes.
 * </ul>
 *
 */
public class MalteseStemmer {

	
	private static final Logger logger = LoggerFactory.getLogger(MalteseStemmer.class);

	public static List<String> prefixes;
	public static List<String> suffixes;
	public static Map<String, String> tokenLemmaMap;
	
	public MalteseStemmer() {
		loadResources();
	}

	private void loadResources() {
		prefixes = new ArrayList<String>();
		suffixes = new ArrayList<String>();
		tokenLemmaMap = new HashMap<String, String>();
		try {
			for (String prefixLine : IOUtils.readLines(this.getClass().getResourceAsStream("/mt_analyzer_prefixes.txt"))) {
				prefixes.add(prefixLine.trim());
			}
			for (String suffixLine : IOUtils.readLines(this.getClass().getResourceAsStream("/mt_analyzer_suffixes.txt"))) {
				suffixes.add(suffixLine.trim());
			}
			for (String dictLine : IOUtils.readLines(this.getClass().getResourceAsStream("/mt_analyzer_dict.txt"))) {
				if (dictLine.matches("^\\s+$")) {
					continue;
				}
				tokenLemmaMap.put(StringUtils.split(dictLine)[0].trim(), StringUtils.split(dictLine)[1].trim());
			}
		} catch (IOException e) {		
			logger.error("Cannot load  stemmer resources...");
			e.printStackTrace();
		}

	    Comparator<String> comparator = new Comparator<String>()  {
	        @Override
	        public int compare(String o1, String o2)  {
	            if (o1.length() > o2.length()) {
	                return -1;
	            }
	            if(o2.length() > o1.length()) {
	                return 1;
	            }
	            return 0;
	        }
	    };
		Collections.sort(prefixes,  comparator);
		Collections.sort(suffixes,  comparator);
	}

	/**
	 * Stem an input buffer of Maltese text.
	 * 
	 * @param s   input buffer
	 * @param len length of input buffer
	 * @return length of input buffer after normalization
	 */
	public int stem(char s[], int len) {
		String str = new String(s);
		if (tokenLemmaMap.containsKey(str)) {
			s = tokenLemmaMap.get(str).toCharArray();
			return s.length;
		}
		len = stemPrefix(s, len);
		len = stemSuffix(s, len);

		return len;
	}

	/**
	 * Stem a prefix off an Maltese word.
	 * 
	 * @param s   input buffer
	 * @param len length of input buffer
	 * @return new length of input buffer after stemming.
	 */
	public int stemPrefix(char s[], int len) {
		for (String prefix : prefixes) {
			if (startsWithCheckLength(s, len, prefix.toCharArray())) {
				return deleteN(s, 0, len, prefix.toCharArray().length);
			}
		}
		return len;
	}

	/**
	 * Stem suffix(es) off an Maltese word.
	 * 
	 * @param s   input buffer
	 * @param len length of input buffer
	 * @return new length of input buffer after stemming
	 */
	public int stemSuffix(char s[], int len) {
		for (String suffix: suffixes) {
			if (endsWithCheckLength(s, len, suffix.toCharArray())) {
				len = deleteN(s, len - suffix.toCharArray().length, len, suffix.toCharArray().length);
			}
		}
		return len;
	}

	/**
	 * Returns true if the prefix matches and can be stemmed
	 * 
	 * @param s   input buffer
	 * @param len length of input buffer
	 * @param prefix prefix to check
	 * @return true if the prefix matches and can be stemmed
	 */
	boolean startsWithCheckLength(char s[], int len, char prefix[]) {
		if (prefix.length == 1 && len < 4) { // wa- prefix requires at least 3
												// characters
			return false;
		} else if (len < prefix.length + 2) { // other prefixes require only 2.
			return false;
		} else {
			for (int i = 0; i < prefix.length; i++)
				if (s[i] != prefix[i])
					return false;

			return true;
		}
	}

	/**
	 * Returns true if the suffix matches and can be stemmed
	 * 
	 * @param s   input buffer
	 * @param len length of input buffer
	 * @param suffix suffix to check
	 * @return true if the suffix matches and can be stemmed
	 */
	boolean endsWithCheckLength(char s[], int len, char suffix[]) {
		if (len < suffix.length + 2) { // all suffixes require at least 2
									   // characters after stemming
			return false;
		} else {
			for (int i = 0; i < suffix.length; i++)
				if (s[len - suffix.length + i] != suffix[i])
					return false;

			return true;
		}
	}
}
