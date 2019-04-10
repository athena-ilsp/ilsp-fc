package gr.ilsp.fc.DGThandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import gr.ilsp.nlp.commons.Constants;

public class DGTHandlerUtils {
	private static final String HYPHEN = "-";
	private final static String GT = ">";
	private static final String UNDERSCORE = "_";

	static Matcher twitterHandleMatcher = Pattern.compile("(^|[^@\\w])@(\\w{1,15})\\b").matcher("");

	/*public static void main(String[] args) throws IOException {
		String langs="el;es;cs;fr;nl;ru;mk;de;bg;it;pt;sq;da;ro;tr;pl;hu;sv;lv;lt;hr;ga;fi;no;sl;sk;is";
		String[] lang = langs.split(";");
		String part1= "java -jar C:/Users/vpapa/ELRC/DGT-TM/TMXtract.jar \"EN\Constants.SPACE;
		String part2 = "C:/Users/vpapa/ELRC/DGT-TM/tmx_2015/output";
		String part3 = "2015";
		String part4 = ".tmx";
		String part5 = "EN";
		//String part6 = "C:/Users/vpapa/ELRC/DGT-TM/Vol";
		String part6 = "Vol";
		String part7 = ".zip";
		String part8a = " C:/Users/vpapa/ELRC/DGT-TM/tmx_2015/log_extract";
		String part8b = " C:/Users/vpapa/ELRC/DGT-TM/tmx_2015/log_process";
		String part9 = "java -cp C:/Users/vpapa/workspace/gr.ilsp.ilsp-fc-2.2.4-SNAPSHOT/target/ilsp-fc-2.2.4-SNAPSHOT-jar-with-dependencies.jar "
				+ "gr.ilsp.fc.DGThandler.DGTHandler -i ";
		String part10 = " -l1 "+ part5;
		String part11 = " -l2 ";
		String part12 = " -medwl 18 -maxwl 35 mtuvl \"3\" -minlr \"0.6\" -maxlr \"1.6\Constants.SPACE;
		
		List<String> commands = new ArrayList<String>();
		for (String l:lang){
			l=l.toUpperCase();
			String p1 = part1+" \""+l+"\Constants.SPACE ;
			String p2 = " \""+part2+UNDERSCORE+part5+HYPHEN+l+UNDERSCORE+part3+part4+"\"";
			String p3 = part6+UNDERSCORE+part3+UNDERSCORE+"*"+part7;
			String p4 = part8a+UNDERSCORE+part5+HYPHEN+l+UNDERSCORE+part3;
			commands.add(p1+SPACE+p2+ SPACE +p3 + SPACE +GT + p4);
		}
		for (String l:lang){
			l=l.toUpperCase();
			String p1 = part9 + " \""+part2+UNDERSCORE+part5+HYPHEN+l+UNDERSCORE+part3+part4+"\"";
			String p2 = part10 + part11+ l;
			String p3 = part12;
			String p4 = part8b+UNDERSCORE+part5+HYPHEN+l+UNDERSCORE+part3;
			commands.add(p1+SPACE+p2+ SPACE +p3 + SPACE +GT + p4);
		}
		FileUtils.writeLines(new File("C:/Users/vpapa/ELRC/DGT-TM/dgt_commands"), commands,"\n");
	}*/
}
