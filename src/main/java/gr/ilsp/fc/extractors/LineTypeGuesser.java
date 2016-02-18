package gr.ilsp.fc.extractors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import org.apache.commons.lang.StringUtils;

public class LineTypeGuesser {

	//	DIG pattern.
	//protected static final Pattern DIG_P	=	Pattern.compile("^[\\+\\-$€\\.,]*([0-9]+[0-9,\\.]*)(\\-[0-9]+[0-9,\\.]*)*(%‰‱)*$");
	// TODO: Check whether replacing 0-9 with \\p{N} has any side effects. \\p{N} allows recognition of unicode fractions
	//\\pSc=any currency symbol
	protected static final String digitPattern	=	"['’\\+\\-\\.,/=:\\p{Sc}]*([0-9\\p{N}]+[0-9\\p{N},/=:'’\\.]*)(\\-[\\p{N}0-9]+[\\p{N}0-9,/=:'’\\.]*)*[%‰‱°\\p{Sc}:’′'’]*";
	
	protected static Matcher digitTokenM	=	Pattern.compile("^"
			+ digitPattern
			+ "$").matcher("");

	
	public static boolean isDig(String token) {
		return digitTokenM.reset(token).matches()   ;
	}
	
	public  static boolean isDigitsOnlyLine(String line) {
		//String tokens[] = StringUtils.split(line);
		String tokens[] = line.split(" ");
		for (int i = 0; i < tokens.length; i++) {
			if (!digitTokenM.reset(tokens[i]).matches()) {
				//System.out.println(tokens[i]);
				return false;
			}
		}
		return true;
	}
	
	
	
}
