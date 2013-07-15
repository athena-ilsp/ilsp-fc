package gr.ilsp.fmc.utils;


import gr.ilsp.fmc.main.SimpleCrawlHFS;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.JobConf;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;



@SuppressWarnings("deprecation")
public class TopicTools {
	private static Analyzer analyzer = null;
	private static AnalyzerFactory analyzerFactory = new AnalyzerFactory();
	//private static int MAX_CONTENT_TERMS = SimpleCrawlHFS.config.getInt("classifier.min_content_terms.value");
	protected static Matcher skipLineM = Pattern.compile("^(\\s*)||(#.*)$").matcher("");


	public static ArrayList<String[]> analyzeTopic(String topicdef, String lang, JobConf conf) {		
		//topicdef:filename of file with the topic definition
		//returns an array of strings with three columns (the triplets)
		ArrayList<String[]> topic = new ArrayList<String[]>();
		Path p = new Path(topicdef);
		//JobConf conf = new JobConf();
		conf.setJarByClass(SimpleCrawlHFS.class);
		//System.err.println(conf.get("hadoop.tmp.dir"));
		//conf.set("hadoop.tmp.dir", "hadoop-temp");
				
		try {
			FileSystem fs = FileSystem.get(conf);
			//Path p = new Path(fs.getWorkingDirectory()+"/"+topicdef);
			if (!fs.exists(p)) 
				System.out.println("The file for topic definition does not exist.");
			else {
				BufferedReader in = new BufferedReader(new InputStreamReader(fs.open(p),"UTF-8"));
				//BufferedReader in = new BufferedReader(new FileReader(temp));
				
				String str, a, b, c, d, b_or="";
				String[] langs = lang.split(";");
				while ((str = in.readLine()) != null) {
					// Do not bother with commented out or empty lines
					if (skipLineM.reset(str).matches()) 
						continue;
					a=str.subSequence(0, str.indexOf(":")).toString().trim();
					b=str.subSequence(str.indexOf(":")+1, str.indexOf("=")).toString().toLowerCase().trim();
					b_or=b;
					
					int ind=str.indexOf(">");
					if (langs.length>1){
						if (ind<0){
							System.out.println("Even though the target languages are more than 1," +
									" the language of term '"+str+"' is not defined. Modify the topic definition properly.");
							System.exit(0);	
						}
						d=str.subSequence(ind+1, str.length()).toString().toLowerCase().trim();
					}else{
						d=langs[0].toString().trim();
					}
					boolean match = false;
					for (String tlang:langs){
						if (tlang.equals(d)) {
							match = true;
							break;
						}
					}
					ArrayList<String> stems = new ArrayList<String>();
					if (match);
						stems = analyze_vp(b, d);
					//ArrayList<String> stems = analyze(b, lang);
					if (d.isEmpty())
						stems = analyze(b, lang);
					
					b="";
					//concatenate stems
					for (String s:stems){ b=b.concat(" "+s);}
					b = b.trim();
					if (ind>=0)
						c=str.subSequence(str.indexOf("=")+1, str.indexOf(">")).toString().trim();
					else
						c=str.subSequence(str.indexOf("=")+1, str.length()).toString().trim();
					Boolean flag=true;
					String[] tempstr = new String[1];
					for (int jj=0;jj<topic.size();jj++){
						tempstr=topic.get(jj);
						if (tempstr[1].equals(b) & tempstr[3].equals(d)){
							double a1=Math.round((Double.parseDouble(a)+Double.parseDouble(tempstr[0]))/2);
							a=Integer.toString((int)a1);
							b_or=tempstr[4].trim();
							flag=false;
							topic.remove(jj);
							topic.add(new String[] {a,b,c,d,b_or});
						}
					}
					if (flag){
						topic.add(new String[] {a,b,c,d,b_or});
					}
					//topic.add(new String[] {a,b,c,d});
				}
				in.close();
				
			}
		} catch (IOException e) {e.printStackTrace();}

		return topic;
	}
	public static ArrayList<String> analyzeTopicALL(String topicdef) {
		//topicdef:filename of file with the topic definition
		//returns an array of strings with three columns (the triplets)
		File temp=new File(topicdef);
		ArrayList<String> topic = new ArrayList<String>();
		if (!temp.exists()){
			System.out.println("The file for topic definition does not exist.");
		}
		else {
			try {
				BufferedReader in = new BufferedReader(new FileReader(temp));
				String str, b;
				while ((str = in.readLine()) != null) {
					// Do not bother with commented out or empty lines
					if (skipLineM.reset(str).matches()) 
						continue;
					
					b=str.subSequence(str.indexOf(":")+1, str.indexOf("=")).toString().trim();
					topic.add(b);
				}
				in.close();
			} catch (IOException e) {				
			}
		}
		return topic;
	}
	
	public static ArrayList<String> analyzeTopicALL(ArrayList<String[]> topic) {
		ArrayList<String> topic_all = new ArrayList<String>();
		String[] tempstr = new String[1];
		for (int jj=0;jj<topic.size();jj++){
			tempstr = topic.get(jj);
			topic_all.add(tempstr[4].trim());
		}
		return topic_all;
	}
	
	
	public static ArrayList<String> analyze(String text, String lang) throws IOException  {
		ArrayList<String> stems = new ArrayList<String>();
		if (lang.equals("lt")){
			stems = LithuanianAnalyzer.analyze(text);
		}
		else {
			try {
				analyzer = analyzerFactory.getAnalyzer(lang);
			} catch (Exception e) {
				//logger.fatal("Cannot initialize analyzer for lang " + lang);
				e.printStackTrace();
				return null;
			}
			TokenStream tokenStream = analyzer.tokenStream("contents", new StringReader(text));
			CharTermAttribute termAtt = (CharTermAttribute) tokenStream.addAttribute(CharTermAttribute.class);
			while (tokenStream.incrementToken()) {
				//logger.debug(termAtt.toString());
				stems.add(termAtt.toString());
			}
			tokenStream.close();
			analyzer.close();
		}
		return stems;
	}
	
	public static ArrayList<String> analyze_vp(String text, String lang) throws IOException  {
		ArrayList<String> stems = new ArrayList<String>();
		if (lang.equals("lt")){
			stems = LithuanianAnalyzer.analyze(text);
		}
		else {
			try {
				analyzer = analyzerFactory.getAnalyzer(lang);
			} catch (Exception e) {
				//logger.fatal("Cannot initialize analyzer for lang " + lang);
				e.printStackTrace();
				return null;
			}
			TokenStream tokenStream = analyzer.tokenStream("contents", new StringReader(text));
			CharTermAttribute termAtt = (CharTermAttribute) tokenStream.addAttribute(CharTermAttribute.class);
			while (tokenStream.incrementToken()) {
				//logger.debug(termAtt.toString());
				stems.add(termAtt.toString());
			}
			tokenStream.close();
			analyzer.close();
		}
		return stems;
	}
	
	public static String[] findSubclasses(ArrayList<String[]> topic) {
		// gets the array with triplets and returns an array of strings with the subclasses
		// i.e. unique of third column of topic
		ArrayList<String> temp = new ArrayList<String>();
		String[] tempstr = new String[1];
		String temp_line="";
		String[] subclasses;

		for (int ii=0;ii<topic.size();ii++){
			tempstr =topic.get(ii);	
			temp_line=tempstr[2];
			subclasses = temp_line.split(";");
			for (int kk=0;kk<subclasses.length;kk++){
				temp.add(subclasses[kk].trim());
			}
		}
		String[] new_temp = new String[temp.size()];
		for (int i = 0; i < new_temp.length; i++)
			new_temp[i] = temp.get(i);

		Arrays.sort(new_temp);
		int k = 0;
		for (int i = 0; i < new_temp.length; i++){
			if (i > 0 && new_temp[i].equals(new_temp[i -1]))
				continue;
			new_temp[k++] = new_temp[i];
		}
		String[] classes = new String[k];
		System.arraycopy(new_temp, 0, classes, 0, k);
		return classes;
	}
	public static double calculateThreshold(ArrayList<String[]> topic, int MAX_CONTENT_TERMS) {
		ArrayList<Double> temp1 = new ArrayList<Double>();
		double result=0.0;
		int kk=0;
		for (int ii=0;ii<topic.size();ii++){
			double s = Double.parseDouble(topic.get(ii)[0]);
			if (s>0){
				temp1.add(s);
				kk++;
			}	
		}
		if (temp1.size()==0)
			return 0;

		Double[] temp=new Double[kk];
		System.arraycopy(temp1.toArray(), 0, temp, 0, kk);
		Arrays.sort(temp);
		if (temp.length % 2 == 1) {
			result = temp[((temp.length+1)/2)-1];
		}else{
			result = (temp[((temp.length)/2)-1]+temp[(temp.length)/2])/2;
		}
		result=MAX_CONTENT_TERMS *result;
		return result;
	}

	public static String convertStreamToString(InputStream is)
	throws IOException {
		/*
		 * To convert the InputStream to String we use the
		 * Reader.read(char[] buffer) method. We iterate until the
		 * Reader return -1 which means there's no more data to
		 * read. We use the StringWriter class to produce the string.
		 */
		if (is != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(
						new InputStreamReader(is, "UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		} else {       
			return "";
		}
	}

}
