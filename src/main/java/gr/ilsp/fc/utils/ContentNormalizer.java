package gr.ilsp.fc.utils;


//import java.util.ArrayList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;


public class ContentNormalizer {
	

    // U+D800..U+DB7F     High Surrogates     896 Code points, 0     Assigned characters
    // U+DB80..U+DBFF     High Private Use Surrogates     128 Code points, 0     Assigned characters
    // U+DC00..U+DFFF     Low Surrogates     1,024     0    Assigned characters
    //static Matcher surrogatesMatcher =  Pattern.compile("[\\uD800-\\uDBFF\\uDC00-\\uDFFF]").matcher("");
    //static Matcher surrogatesMatcher =  Pattern.compile("[\\uD800-\\uDBFF][\\uDC00-\\uDFFF]").matcher("");
	static Matcher surrogatesMatcher =  Pattern.compile("[\\uD800-\\uDFFF]").matcher("");
	
	private static final String boiler_st = "<boiler";
	/*public static void main(String[] args){
		String s = args[0];//"s offensichtlich unbegründet a";
		char s1;
		for (int ii=0;ii<s.length()-1;ii++){
			s1 = s.charAt(ii);
			System.out.println(s1+"="+(int)s1 );
			System.out.println();
		}
		String s2 = normalizeText(s);
		System.out.println(s2);
	}*/
	private static final HashMap<String,String> invalidChars = new HashMap<String,String>(){
		/**
		 * 
		 */
		private static final long serialVersionUID = -7208860988679686271L;

		{ 
			put("\\uFEFF", " "); //ZERO WIDTH NO-BREAK SPACE
			put("\\uFFFF","");
			put("\\u00a0", " "); //NO BREAK SPACE
			put("\\u200E"," "); //LEFT-TO-RIGHT MARK
			put("\\u0097", "-"); //END OF GUARDED AREA
			put("\\u0092", "’"); //APOSTROPHE
			put("\\u0313","’"); //COMBINING COMMA ABOVE
			put("\\u0094", "”"); //CLOSE DOUBLE QUOTE
			put("\\u0093", "“"); //OPEN DOUBLE QUOTE
			put("\\u00AB","«"); //LEFT-POINTING DOUBLE ANGLE QUOTATION MARK
			put("\\u00BB","»");//RIGHT-POINTING DOUBLE ANGLE QUOTATION MARK
			put("\\u201C","“");	//LEFT DOUBLE QUOTATION MARK
			put("\\u201D","”");//RIGHT DOUBLE QUOTATION MARK
			put("\\u0095", "•"); 
			put("\\u0096", "-");
			put("\\u202f", " "); //NARROW NO-BREAK SPACE
			put("\\u2206", "Δ"); //INCREMENT
			put("\\u02bc", "’"); //MODIFIER LETTER APOSTROPHE
			put("\\u003e", ">"); //GREATER-THAN SIGN
			put("\\u0000", "");
			put("\\u0001", "");
			put("\\u0002", "");
			put("\\u0003", "");
			put("\\u0004", "");
			put("\\u0005", "");
			put("\\u0006", "");
			put("\\u0007", "");
			put("\\u0008", "");

			put("\\u000B", "");
			put("\\u000C", "");
			put("\\u000E", "");
			put("\\u000F", "");
			put("\\u0010", "");
			put("\\u0011", "");
			put("\\u0012", "");
			put("\\u0013", "");
			put("\\u0014", "");
			put("\\u0015", "");
			put("\\u0016", "");
			put("\\u0017", "");
			put("\\u0018", "");
			put("\\u0019", "");
			put("\\u001A", "");
			put("\\u001B", "");
			put("\\u001C", "");
			put("\\u001D", "");
			put("\\u001E", "");
			put("\\u001F", "");
			put("\\u007F", "");
			put("\\u0080",""); //RIGHT-TO-LEFT MARK
			put("\\u0081", "");
			put("\\u0082", "");
			put("\\u0083", "");
			put("\\u0084", "");
			//put("\\u0085", "");
			put("\\u0086", "");
			put("\\u0087", "");
			put("\\u0088", "");
			put("\\u0089", "");
			put("\\u008A", "");
			put("\\u008B", "");
			put("\\u008C", "");
			put("\\u008D", "");
			put("\\u008E", "");
			put("\\u008F", "");
			put("\\u0090", "");
			put("\\u0091", "");
			put("\\u0092", "");
			put("\\u0093", "");
			put("\\u0094", "");
			put("\\u0095", "");
			put("\\u0096", "");
			put("\\u0097", "");
			put("\\u0098", "");
			put("\\u0099", "");
			put("\\u009A", "");
			put("\\u009B", "");
			put("\\u009C", "");
			put("\\u009D", "");
			put("\\u009E", "");
			put("\\u009F", "");

			put("\\uFFFD",""); //REPLACEMENT CHARACTER Specials
			put("\\uF0D8",""); //INVALID UNICODE CHARACTER
			put("\\uF02D",""); //INVALID UNICODE CHARACTER
			put("\\uF0FC",""); //INVALID UNICODE CHARACTER
			put("\\uF034",""); //INVALID UNICODE CHARACTER
			put("\\uF076",""); //INVALID UNICODE CHARACTER
			put("\\uF0BC",""); //INVALID UNICODE CHARACTER
			put("\\uF06C",""); //INVALID UNICODE CHARACTER
			put("\\uF0E8",""); //INVALID UNICODE CHARACTER
			put("\\uF0B7",""); //INVALID UNICODE CHARACTER
			put("\\uF0A7",""); //INVALID UNICODE CHARACTER
			put("\\uF0FB",""); //INVALID UNICODE CHARACTER
			put("\\uF06E",""); //INVALID UNICODE CHARACTER
			put("\\uF0F1",""); //INVALID UNICODE CHARACTER
			put("\\uF075",""); //INVALID UNICODE CHARACTER
			//FIXME check replacement of 2028
			//put("\\u2028","\n"); //Unicode Character 'LINE SEPARATOR'  
			put("\\u2028"," "); //Unicode Character 'LINE SEPARATOR'
			put("\\u0084","");
			put("\\uC284","");
			put("\\uC293","");
			put("\\u2126","Ω"); //OHM SIGN
			put("\\u25B6","►"); //BLACK RIGHT-POINTING TRIANGLE
			put("\\u200F",""); //RIGHT-TO-LEFT MARK
			put("\\u2026","…"); //HORIZONTAL ELLIPSIS
			put("\\u2019","'"); //RIGHT SINGLE QUOTATION MARK
			//got from prokopis
			put("\\u2002"," "); // EN SPACE
			put("\\u2003"," "); // EM SPACE
			put("\\u0085","…");
			put("\\u2004"," "); // THREE-PER-EM SPACE
			put("\\u2005"," "); // FOUR-PER-EM SPACE
			put("\\u2006"," "); // SIX-PER-EM SPACE
			put("\\u2007"," "); // FIGURE SPACE
			put("\\u2008"," "); // PUNCTUATION SPACE
			put("\\u2009"," "); // THIN SPACE
			put("\\u200A"," "); // HAIR SPACE
			put("\\uF0B7","•"); // INVALID UNICODE CHARACTER->BULLET
			put("\\u2043","•"); // HYPHEN BULLET
			put("\\uC2B3","•");
			put("\\u225C","Δ");
			put("\\u2206","Δ");
			put("\\u002D","-");
			put("\\u2012","-");
			put("\\u2013","-");
			put("\\u2014","-");
			put("\\u2015","-");
			put("\\u2E17","-");
			put("\\u2E3A","-");
			put("\\u2E3B","-");
			put("\\u301C","-");
			put("\\u3030","-");
			put("\\u30A0","-");
			put("\\uFE31","-");
			put("\\uFE32","-");
			put("\\uFE58","-");
			put("\\uFE63","-");
			put("\\uFF0D","-");
			//put("\\u009a","š");
			//put("\\u008a","Š");
			//put("\\u008c","Œ");
			//put("\\u009c","œ");
			//put("\\u008e","Ž");
			//put("\\u009e","ž");
			//put("\\u009f","Ÿ");
			//put("\\u0014"," ");
			

		}
	};


	public static String normalizeText(String text){
		
		if (StringUtils.isBlank(text))
			return "";
		
        text = surrogatesMatcher.reset(text).replaceAll("");
		
		for (String s:invalidChars.keySet()){
			text = text.replaceAll(s, invalidChars.get(s));
		}
		text = text.replaceAll("\t", " ");
		text = text.replaceAll("(\\s){2,}", " ");
		text = text.replaceAll("\r\n", "\n");
		text = text.replaceAll("\r", "\n");
		text = text.replaceAll("</text><text>", "</text>\n<text>");
		text = text.replaceAll("\n</text>", "</text>");
		text = text.replaceAll("<text> ", "<text>");
		text = text.replaceAll("<boiler> ", "<boiler>");
		text = text.replaceAll(" </text>", "</text>");
		text = text.replaceAll(" </boiler> ", "</boiler>");
		text = text.replaceAll("<text></text>", "");
		text = text.replaceAll("<boiler></boiler>", "");
		text = text.replaceAll("( \n)", "\n");
		text = text.replaceAll("(\n){2,}","\n");
		return text;
	}


public static String normalizeText1(String text){
		
		 text = surrogatesMatcher.reset(text).replaceAll("");
		
		for (String s:invalidChars.keySet()){
			text = text.replaceAll(s, invalidChars.get(s));
		}
		text = text.replaceAll("\t", " ");
		text = text.replaceAll("(\\s){2,}", " ");
		text = text.replaceAll("<text> ", "<text>");
		text = text.replaceAll("<boiler> ", "<boiler>");
		text = text.replaceAll(" </text>", "</text>");
		text = text.replaceAll(" </boiler> ", "</boiler>");
		text = text.replaceAll("<text></text>", "");
		text = text.replaceAll("<boiler></boiler>", "");
		text = text.replaceAll("( \n)", "\n");
		text = text.replaceAll("(\n){2,}","\n");
		return text;
	}

	
	
	private static final String XML10PATTERN = "[^"
			+ "\u0009\r\n"
			+ "\u0020-\uD7FF"
			+ "\uE000-\uFFFD"
			+ "\ud800\udc00-\udbff\udfff"
			+ "]";


	public static String normalizePdfText(String content) {
		for (String s:invalidChars.keySet()){
			content = content.replaceAll(s, invalidChars.get(s));
		}
		content=content.replaceAll(XML10PATTERN, "");
		//content = content.replaceAll("(\\s){2,}", " ");
		content = content.replaceAll("( \n)", "\n");
		content = content.replaceAll("(\n){3,}","\n");
		content=splitParagraphs("\n"+content);

		return content;
	}

	//FIXME
	private static String splitParagraphs(String content) {
		//String text="";

		//int par_id=0;  
		//ArrayList<String> paragraphs =new ArrayList<String>();
		String[] lines=content.split("\n");

		int[] lengths=new int[lines.length];
		for (int ii=0;ii<lines.length-1;ii++){
			lengths[ii]=lines[ii].length();
		}


		for (int ii=0;ii<lines.length-1;ii++){
			lengths[ii]=lines[ii].length();
		}
		return content;
	}


	/**
	 * Removes special patterns (structural info) added during cleaning 
	 * @param content
	 * @return
	 */
	public static String cleanContent(String content){
		String result = "";
		String REGEX = "<text.*>.*</text>";
		String REPLACE = " ";
		Pattern p = Pattern.compile(REGEX);
		Matcher m = p.matcher(content);
		String text = "";
		while (m.find()){
			text = m.group();
			text = text.replaceAll(" type='listelem'","");
			text = text.replaceAll(" type='title'","");
			text = text.replaceAll(" type='heading'","");
			text = text.replaceAll("</?text>", REPLACE);
			result = result.concat(text);
		}
		return result;
	}


	/**
	 * Returns a string with the concatenation of the strings in the ArrayList 
	 * @param stems
	 * @param min_tok_num
	 * @return
	 */
	public static String concatenateStems(ArrayList<String> stems,	int min_tok_num) {
		String tempstr="";
		for (String s:stems){
			if (s.length()>min_tok_num)
				tempstr+=" "+s;
		}
		tempstr = tempstr.replaceAll("(\\s){2,}", " ");
		tempstr = tempstr.trim();
		return tempstr;
	}

	/**
	 * removes non-letters and a few "special" chars 
	 * @param text
	 * @return
	 */
	public static String normtext(String text) {
		
		text=text.replaceAll("&apos;"," ");		text=text.replaceAll("&quot;"," ");
		text=text.replaceAll("&amp;"," ");		text=text.replaceAll("&lt"," ");
		text=text.replaceAll("&gt"," ");			text=text.replaceAll("&#"," ");
		//if (text.contains("&")){
		//	System.out.println(text);
		//}
		text=text.replaceAll("[^\\p{L} ]", " ").trim();
		text=text.replaceAll("(\\s){2,}", " ").trim();
		text=text.toLowerCase();
		return text;
	}

public static String normtext1(String text) {
		text=text.replaceAll("&apos;"," ");		text=text.replaceAll("&quot;"," ");
		text=text.replaceAll("&amp;"," ");		text=text.replaceAll("&lt"," ");
		text=text.replaceAll("&gt"," ");			text=text.replaceAll("&#"," ");
		//if (text.contains("&")){
		//	System.out.println(text);
		//}
		text=text.replaceAll("[\\p{N} ]", " ").trim();
		text=text.replaceAll("(\\s){2,}", " ").trim();
		text=text.toLowerCase();
		return text;
	}
	
	public static String removeBoilerPars(String content) {
		content = content.replaceAll(boiler_st+"\\.*", "");
		content=content.replaceAll("(\\n){2,}", "");
		return content;
	}

}
