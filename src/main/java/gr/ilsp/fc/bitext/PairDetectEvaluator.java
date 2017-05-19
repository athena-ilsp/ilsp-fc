package gr.ilsp.fc.bitext;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import gr.ilsp.nlp.commons.Constants;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

public class PairDetectEvaluator {
	private static final Logger LOGGER = Logger.getLogger(PairDetectEvaluator.class);
	private static String UNDERSCORE = "_";
	private static final String XMLEXT = ".xml";

	public static void main(String[] args) {

		File indir = new File(args[0]);
		HashMap<String,String> truepairs = BitextUtils.getTruePairs( args[1]);
		File[] pairs = indir.listFiles();
		HashMap<String,int[]> results = new HashMap<String,int[]>();
		ArrayList<String> checked = new ArrayList<String>(); 
		for (File file:pairs){
			String name = file.getName();
			if (!name.contains(UNDERSCORE))
				continue;
			if (!name.endsWith(XMLEXT))
				continue;
			String[] parts = FilenameUtils.getBaseName(name).split(UNDERSCORE);
			String key1 = parts[0]/*.replaceAll("-el-", "-ell-")*/;
			String key2 = parts[1]/*.replaceAll("-en-", "-eng-")*/;
			if (checked.contains(key1)){
				System.out.println("OOPS!");
			}else{
				checked.add(key1);
			}
			if (checked.contains(key2)){
				System.out.println("OOPS!");
			}else{
				checked.add(key2);
			}

			String method = parts[2];
			boolean correct=false;
			if ((truepairs.containsKey(key1) && truepairs.get(key1).equals(key2)) || (truepairs.containsKey(key2) && truepairs.get(key2).equals(key1))){
				System.out.println("\t\t"+key1+Constants.SPACE+key2+Constants.SPACE+"1");
				correct=true;
			}else{
				System.out.println("\t\t"+key1+Constants.SPACE+key2+Constants.SPACE+"0");
			}
			int[] res=new int[2];
			if (results.containsKey(method)){
				res= results.get(method);
				if (correct)
					res[0] = res[0] + 1;
				else
					res[1] = res[1] + 1;
			}else{
				if (correct){
					res[0]=1; res[1]=0;
				}else{
					res[0]=0; res[1]=1;
				}
			}
			results.put(method, res);
		}
		Set<String> methods = results.keySet();
		Iterator<String> method = methods.iterator();
		String method_it;
		int pos=0, neg=0;
		while (method.hasNext()){
			method_it = method.next();
			int[] res=results.get(method_it);
			LOGGER.info("Method\t"+ method_it +"\tCorrect =\t"+ res[0]+"\tWrong =\t\t"+ res[1]);
			pos = pos + res[0];
			neg = neg + res[1];
		}
		LOGGER.info("Total results\t\tCorrect =\t"+ pos+"\tWrong =\t"+ neg);
	}
}
