package gr.ilsp.fc.utils;

import gr.ilsp.fc.main.ReadResources;
import gr.ilsp.fc.main.Crawl;
import gr.ilsp.fc.main.WriteResources;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.JobConf;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

@SuppressWarnings("deprecation")
public class TopicTools {
	private static final Logger LOGGER = Logger.getLogger(TopicTools.class);
	private static Analyzer analyzer = null;
	private static AnalyzerFactory analyzerFactory = new AnalyzerFactory();
	//private static int MAX_CONTENT_TERMS = Crawl.config.getInt("classifier.min_content_terms.value");
	protected static Matcher skipLineM = Pattern.compile("^(\\s*)||(#.*)$").matcher("");
	private static final String XML_EXTENSION = ".xml";
	private static final String UNDERSCORE_STR = "_";
	private static final String QUESTION_SEP=";";
	private static final String  COLON =" : ";
	/**
	 * Parses the topic definition file and  returns a list of terms with their properties i.e. weight, stemmed term, subclasses or class, language, original term  
	 * @param topicFile
	 * @param targetlanguages
	 * @param conf
	 * @return
	 */
	//FIXME store topic definition in an Object with terms and their properties 
	public static ArrayList<String[]> analyzeTopic(File topicFile, String[] targetlanguages, JobConf conf) {		
		//topicdef:filename of file with the topic definition
		//returns an array of strings with three columns (the triplets)
		ArrayList<String[]> topic = new ArrayList<String[]>();
		Path p = new Path(topicFile.getAbsolutePath());
		//JobConf conf = new JobConf();
		conf.setJarByClass(Crawl.class);
		//System.err.println(conf.get("hadoop.tmp.dir"));
		//conf.set("hadoop.tmp.dir", "hadoop-temp");

		try {
			FileSystem fs = FileSystem.get(conf);
			if (!fs.exists(p)) 
				LOGGER.info("The file for topic definition does not exist.");
			else {
				BufferedReader in = new BufferedReader(new InputStreamReader(fs.open(p),"UTF-8"));
				String str, a, b, c, d, b_or="";
				while ((str = in.readLine()) != null) {
					// Do not bother with commented out or empty lines
					if (skipLineM.reset(str).matches()) 
						continue;
					a=str.subSequence(0, str.indexOf(":")).toString().trim();
					b=str.subSequence(str.indexOf(":")+1, str.indexOf("=")).toString().toLowerCase().trim();
					b_or=b;

					int ind=str.indexOf(">");
					if (targetlanguages.length>1){
						if (ind<0){
							LOGGER.info("Even though the target languages are more than 1," +
									" the language of term '"+str+"' is not defined. Modify the topic definition properly.");
							System.exit(0);	
						}
						d=str.subSequence(ind+1, str.length()).toString().toLowerCase().trim();
					}else{
						d=targetlanguages[0].toString().trim();
					}
					boolean match = false;
					for (String tlang:targetlanguages){
						if (tlang.equals(d)) {
							match = true;
							break;
						}
					}
					ArrayList<String> stems = new ArrayList<String>();
					if (match);
					stems = getStems(b, d); //FIXME Call Analyzer for each line!!
					//FIXME when the following happens?
					//if (d.isEmpty())
					//	stems = analyze(b, lang);

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
						//LOGGER.info(b+"\t"+b_or);
					}
					//topic.add(new String[] {a,b,c,d});
				}
				in.close();

			}
		} catch (IOException e) {e.printStackTrace();}
		return topic;
	}

	/**
	 * Returns the stems of the text (in language) based on a naive analyzer of this language
	 * @param text
	 * @param lang
	 * @return
	 * @throws IOException
	 */
	public static ArrayList<String> getStems(String text, String language) throws IOException  {
		ArrayList<String> stems = new ArrayList<String>();
		try {
			analyzer = analyzerFactory.getAnalyzer(language);
			TokenStream ts = analyzer.tokenStream("contents", text);
			ts.reset(); // Resets this stream to the beginning. (Required)
			while (ts.incrementToken()) {
				stems.add(ts.getAttribute(CharTermAttribute.class).toString());
			}
			ts.end();   // Perform end-of-stream operations, e.g. set the final offset.
		} catch (Exception e) {
			LOGGER.warn("Cannot initialize analyzer for lang " + language);
			e.printStackTrace();
		}
		analyzer.close();
		return stems;
	}

	/**
	 * Returns the subclasses of the topic definition 
	 * @param topic
	 * @return
	 */
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
			subclasses = temp_line.split(QUESTION_SEP);
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

	/**
	 * Calculates the median value of terms' weights of the topic definition and multiplies it with MAX_CONTENT_TERMS
	 * @param topic
	 * @param MAX_CONTENT_TERMS 
	 * @return
	 */
	public static double calculateTopicThreshold(ArrayList<String[]> topic, int MAX_CONTENT_TERMS) {
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

	/**
	 * checks if a word of words is included in the text  
	 * @param text
	 * @param thresh
	 * @return
	 */
	public static Boolean findWords(String text, List<String> neg_words){
		if (neg_words==null) 
			return false;
		Boolean isIncluded=false;
		int ind=0;
		for (int ii=0;ii<neg_words.size();ii++){
			ind = text.indexOf(neg_words.get(ii)); 
			if (ind>0){
				isIncluded=true;
				break;
			}
		}
		return isIncluded;		
	}

	/**
	 * returns a string containing terms (separated by ";") that are in the topic_terms and in the text
	 * @param part_of_text
	 * @param topic_terms
	 * @param lang
	 * @param topic_termsALL
	 * @return
	 */
	public static String findTopicTerms(String text,
			ArrayList<String[]> topic_terms, String lang){
		String found="";
		if (topic_terms==null || lang.isEmpty())
			return found;

		String[] tempstr = new String[1];		String term;
		ArrayList<String> stems =new ArrayList<String>();
		try {
			stems = getStems(text, lang);
		} catch (IOException e) {
			e.printStackTrace();
		} 
		String par_text="", term_lang ;
		for (String st:stems){
			par_text+=" "+st;
		}
		par_text = par_text.trim();
		double weight=0.0;

		for (int ii=0;ii<topic_terms.size();ii++){ //for each row of the topic
			tempstr=topic_terms.get(ii);
			weight=Double.parseDouble(tempstr[0]);
			term = tempstr[1];
			term_lang=tempstr[3];
			if (!term_lang.equals(lang))
				continue;
			Pattern pattern = Pattern.compile(" "+term+" ");	
			Matcher matcher = pattern.matcher(" "+par_text+" ");
			if (matcher.find() & weight>0){
				found=found+QUESTION_SEP+tempstr[4];
			}
		}
		if (!found.isEmpty())
			found=found.substring(1);
		return found;
	}

	
	/**
	 * selects paragraphs (Ps) with attribute "topic" from the cesDoc files that are in the targetDir.
	 * Stores in the corpusFile: the fileName and eAddress of each cesDoc, the detected terms and the content of the Ps with these terms
	 * @param targetDir
	 * @param corpusfFile
	 */
	public static void mergeTopicPars(File targetDir, File corpusFile){
		File[] allfiles = targetDir.listFiles();
		String corpus="";
		String[] attrs, texts;
		Set<String> pars = new HashSet<String>();
		for (File file:allfiles){
			if (file.getName().endsWith(XML_EXTENSION) && !file.getName().contains(UNDERSCORE_STR)){
				corpus = corpus+file.getAbsolutePath()+"\n";
				corpus = corpus+ReadResources.extractAttrfromXML(file.getAbsolutePath(), "eAddress", "type", true,false)+"\n";
				//corpus = corpus+ReadResources.extractURLfromXML(file)+"\n";
				attrs = ReadResources.extractAttrfromXML(file.getAbsolutePath(), "p", "topic", true,false).split("\n");
				texts = ReadResources.extractTextfromXML_clean(file.getAbsolutePath(), "p", "topic", true).split("\n");
				if (attrs.length==texts.length){
					for (int jj=0;jj<attrs.length;jj++){
						if (pars!=null){
							if (pars.contains(texts[jj])){
								continue;
							}
						}
						corpus = corpus+attrs[jj]+COLON+texts[jj]+"\n";
						pars.add(texts[jj]);
					}
				}else{
					LOGGER.error("Somerthing strange happened");
				}
			}
		}
		WriteResources.writetextfile(corpusFile.getAbsolutePath(), corpus);
	}

	
}
