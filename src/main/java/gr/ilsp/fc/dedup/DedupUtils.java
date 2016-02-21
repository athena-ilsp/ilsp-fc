package gr.ilsp.fc.dedup;

import gr.ilsp.fc.langdetect.LangDetectUtils;
import gr.ilsp.fc.main.ReadResources;
import gr.ilsp.fc.utils.ContentNormalizer;
import gr.ilsp.fc.utils.TopicTools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.io.MD5Hash;
import org.apache.log4j.Logger;

public class DedupUtils {
	private static final Logger LOGGER = Logger.getLogger(DedupUtils.class);
	//private static int MIN_TOKEN_LEN=3;	//tokens with less than MIN_TOKEN_LEN are excluded
	private static float QUANT_RATE= (float) 0.01;
	private static int QAUNT_DEFAULT=1; // quantization interval 
	//private static final String LANGUAGE_ELE = "language";
	//private static final String LANGUAGE_ATT ="iso639";
	private static final String ooi_crawlinfo = "crawlinfo";
	private static final String P_ELE = "p";
	//private static int min_tok_num=3;  
	private static final String HYPHEN="-";
	private static final String xmlext="xml";
	private static final String txtext="txt";

	/**
	 * calculates an MD5 hashkey for a  text	
	 * @param text
	 * @return
	 */
	private static byte[] calculateMD5(String text, int MIN_TOKEN_LEN) {
		HashMap<String, Token> tokens = new HashMap<String, Token>();
		StringBuffer curToken = new StringBuffer();
		int maxFreq = 0;
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (Character.isLetterOrDigit(c)) {
				curToken.append(Character.toLowerCase(c));
			} else {
				if (curToken.length() > 0) {
					if (curToken.length() > MIN_TOKEN_LEN) {
						// add it
						String s = curToken.toString();
						Token tok = tokens.get(s);
						if (tok == null) {
							tok = new Token(0, s);
							tokens.put(s, tok);
						}
						tok.cnt++;
						if (tok.cnt > maxFreq) maxFreq = tok.cnt;
					}
					curToken.setLength(0);
				}
			}
		}
		// check the last token
		if (curToken.length() > MIN_TOKEN_LEN) {
			// add it
			String s = curToken.toString();
			Token tok = tokens.get(s);
			if (tok == null) {
				tok = new Token(0, s);
				tokens.put(s, tok);
			}
			tok.cnt++;
			if (tok.cnt > maxFreq) maxFreq = tok.cnt;
		}
		Iterator<Token> it = tokens.values().iterator();
		ArrayList<Token> profile = new ArrayList<Token>();
		// calculate the QUANT value
		int QUANT = Math.round(maxFreq * QUANT_RATE);
		if (QUANT < QAUNT_DEFAULT) {
			if (maxFreq > QAUNT_DEFAULT-1)
				QUANT = QAUNT_DEFAULT;
			else 
				QUANT = 1;
			//if (maxFreq < QAUNT_DEFAULT) QUANT = 1;
		}

		while(it.hasNext()) {
			Token t = it.next();
			// round down to the nearest QUANT
			t.cnt = (t.cnt / QUANT) * QUANT;
			// discard the frequencies below the QUANT
			if (t.cnt < QUANT) {
				continue;
			}
			profile.add(t);
		}
		if (profile.size()==0)
			System.out.println("Empty profile");
		Collections.sort(profile, new TokenComparator());
		StringBuffer newText = new StringBuffer();
		it = profile.iterator();
		while (it.hasNext()) {
			Token t = it.next();
			if (newText.length() > 0) newText.append("\n");
			newText.append(t.toString());
		}
		LOGGER.debug(newText.toString());
		return MD5Hash.digest(newText.toString()).getDigest();
	}

	/**
	 * holds filename, length, list of lengths of paragraphs, list of MD5hashes of paragraphs
	 * @author 
	 *
	 */
	public static class TextParsAttr {
		public List<String> parskeys;
		public List<Integer> parslengths;
		public int length;
		public String filename;

		public TextParsAttr(String filename, int length, List<String> parskeys, List<Integer> parslengths) {
			this.length = length;
			this.filename = filename;
			this.parskeys = parskeys;
			this.parslengths = parslengths;
		}
	}

	/**
	 * holds filename, length of text, MD5hash of text
	 * @author 
	 *
	 */
	public static class TextAttr {
		public String hashkeyText;
		public int length;
		public String filename;

		public TextAttr(int length, String filename, String hashkeyText) {
			this.length = length;
			this.filename = filename;
			this.hashkeyText = hashkeyText;
		}
	}

	private static class TokenComparator implements Comparator<Token> {
		public int compare(Token t1, Token t2) {
			return t2.cnt - t1.cnt;
		}
	}

	private static class Token {
		public int cnt;
		public String val;

		public Token(int cnt, String val) {
			this.cnt = cnt;
			this.val = val;
		}

		@Override
		public String toString() {
			return val + " " + cnt;
		}
	}

	/**
	 * returns the filename, length of text, MD5hash of text (in paragraphs with no crawlinfo attribute) of the input file
	 * @param file
	 * @param input_type 
	 * @return
	 */
	public static TextAttr getTextAttrs(File file, int MIN_TOKEN_LEN, String input_type) {
		String text="";
		if (input_type.endsWith(xmlext))
			text = ReadResources.extractTextfromXML_clean(file.getAbsolutePath(),P_ELE,ooi_crawlinfo, false);
		if (input_type.endsWith(txtext)){
			try {
				text = FileUtils.readFileToString(file);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		LOGGER.debug(text);
		String string_key="";
		if (StringUtils.isBlank(text))
			return null;
		byte[] texthashkey = calculateMD5(text, MIN_TOKEN_LEN); //TODO should text be stemmed?

		for (int jj=0;jj<texthashkey.length;jj++) {
			string_key += texthashkey[jj];
		}
		TextAttr t= new TextAttr(text.length(),file.getName(), string_key);
		return t;
	}

	/**
	 * extracts clean text from the cesDoc and returns an object which holds the filename, the length (in terms of tokens),
	 * a list with lengths of paragraphs, a list with MD5hashes of paragraphs
	 * @param file
	 * @return
	 * @throws IOException 
	 */
	public static TextParsAttr getTextParsAttr(File file, int min_tok_num, String input_type) throws IOException {
		String text="", langIdentified="";
		if (input_type.endsWith(xmlext)){
			text = ReadResources.extractTextfromXML_clean(file.getAbsolutePath(),P_ELE,ooi_crawlinfo, false);
			//String langIdentified = ReadResources.extractLangfromXML(file.getAbsolutePath(), LANGUAGE_ELE, LANGUAGE_ATT);
			langIdentified = file.getName().split(HYPHEN)[0];
		}		
		if (input_type.endsWith(txtext)){
			text = FileUtils.readFileToString(file);
			langIdentified = LangDetectUtils.detectLanguage(text);
		}

		String[] pars=text.split("\n");
		List<String> parskeys=new ArrayList<String>();
		List<Integer> parslengths=new ArrayList<Integer>();
		String textkey;
		int len_in_toks=0;
		for (int jj=0;jj<pars.length;jj++){
			//stem the paragraph
			String tempstr=pars[jj].replaceAll("[0-9]", "");
			ArrayList<String>  stems = TopicTools.getStems(tempstr, langIdentified);
			//concatenate stems 
			tempstr = ContentNormalizer.concatenateStems(stems,min_tok_num);
			if (StringUtils.isBlank(tempstr)){
				LOGGER.debug/*System.out.println*/(pars[jj]);
				continue;
			}

			byte[] parhashkey=MD5Hash.digest(tempstr).getDigest();
			textkey="";
			for (int kk=0;kk<parhashkey.length;kk++) {
				textkey += parhashkey[kk];
			} 
			parskeys.add(textkey);
			StringTokenizer st = new StringTokenizer(pars[jj]);	
			parslengths.add(st.countTokens());
			len_in_toks = len_in_toks+st.countTokens();
		}

		if (parskeys.size()==0)
			return null;

		TextParsAttr t= new TextParsAttr(file.getName(), len_in_toks, parskeys, parslengths);
		return t;
	}

}
