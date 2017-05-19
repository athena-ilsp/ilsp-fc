package gr.ilsp.fc.utils;


//import gr.ilsp.fc.main.Crawl;


/*import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
//import java.io.Reader;
import java.io.StringReader;
//import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
//import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;*/

/*//import org.apache.hadoop.fs.FileSystem;
//import org.apache.hadoop.fs.Path;
//import org.apache.hadoop.mapred.JobConf;
import org.apache.commons.lang.ArrayUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
*/


//@SuppressWarnings("deprecation")
public class TestComparable {
	//private static Analyzer analyzer = null;
	//private static AnalyzerFactory analyzerFactory = new AnalyzerFactory();
	//private static int len=100;
	//private static double thr=0.75;
	//private static boolean use_pdf=false;
	//private static final String UNDERSCORE_STR = "_";
	
	public static void main(String[] args) {
/*		//File f_topic1 = new File(args[0]);
		//File files1 = new File(args[1]);
		//String lang1 = args[2];
		//File f_topic2 = new File(args[3]);
		//File files2 = new File(args[4]);
		//String lang2 = args[5];
		//double thr = Double.parseDouble(args[6]);
		File f_topic1 = new File("C:\\PANACEA\\ACCURAT\\wind_energy_termsfile_EL.txt");
		File files1 = new File("C:\\PANACEA\\ACCURAT\\EL24p");
		String lang1 = "el";
		File f_topic2 = new File("C:\\PANACEA\\ACCURAT\\wind_energy_termsfile_EN_v2.txt");
		File files2 = new File("C:\\PANACEA\\ACCURAT\\EN24p");
		String lang2 = "en";
		
		//read the file (f_topic1) of the topic definition in language (lang1) and returns the stemmed terms in a HashMap (key value is auto-numbered).
		HashMap<Integer, String> topic1 = readTopicfile(f_topic1, lang1);
		HashMap<Integer, String> topic2 = readTopicfile(f_topic2, lang2);
		if (topic1.size()!=topic2.size()){
			System.out.println("The topics do not contain the same amount of terms. So, estimated pdfs will be used.");
			System.out.println("The defualt threshold for histogram comparison is 0.75");
			System.out.println("The defualt threshold for pdf comparison is 0.6");
			use_pdf=true;
		}

		//Compares the stemmed terms list with the stemmed content of each crawled txt file and counts the matches.
		//For each txt file calculates the histogram of terms's occurrences normalized by the length of txt (in terms of tokens).
		//All histograms are stored in a HashMap with key the filename.
		HashMap<String, double[]> files_hist_lang1 = findTermsinFile(files1,topic1,lang1); //writeFilehistog( files_hist1,lang1);
		HashMap<String, double[]> files_hist_lang2 = findTermsinFile(files2,topic2,lang2); //writeFilehistog( files_hist2,lang2);
		topic1=null;
		topic2=null;
		//Estimates the normalized pdf (with the Gaussian kernel) for each histogram
		HashMap<String, double[]> files_pdf_lang1 =  files_estimate_pdf(files_hist_lang1,len);
		HashMap<String, double[]> files_pdf_lang2 =  files_estimate_pdf(files_hist_lang2,len);
		
		HashMap<String, double[]> vec_lang1=new HashMap<String, double[]>();
		HashMap<String, double[]> vec_lang2=new HashMap<String, double[]>();
		if (use_pdf){
			vec_lang1=files_pdf_lang1;
			vec_lang2=files_pdf_lang2;
			thr=0.6;
		}
		else{
			vec_lang1=files_hist_lang1;
			vec_lang2=files_hist_lang2;
			thr=0.75;
		}
		HashMap<String, Double> pairs = new HashMap<String, Double>();
		Set<String> keys_lang1=vec_lang1.keySet();
		Set<String> keys_lang2=vec_lang2.keySet();
		Iterator<String> it1 = keys_lang1.iterator();
		while (it1.hasNext()){
			String filename1 = it1.next();
			System.out.println("Checking file "+ filename1);
			double[] vector1 = vec_lang1.get(filename1);
			Iterator<String> it2 = keys_lang2.iterator();
			double dist=0;
			while (it2.hasNext()){
				String filename2 = it2.next();
				double[] vector2 = vec_lang2.get(filename2);
				dist=1/(1+eucl_dist(vector1,vector2));
				if (dist>thr){
					pairs.put(filename1+UNDERSCORE_STR+filename2, dist);
				}
			}
		}
		files_hist_lang1=null;
		files_hist_lang2=null;
		
		Double[] result=new Double[pairs.size()];

		Set<String> pairkeys=pairs.keySet();
		Iterator<String> pairit1 = pairkeys.iterator();
		int counter=0;
		String pair_key1="";
		while (pairit1.hasNext()){
			pair_key1 = pairit1.next();
			result[counter] = pairs.get(pair_key1);
			counter++;
		}
		Arrays.sort(result, new Comparator<Double>(){
			public int compare(Double o1, Double o2){
				if((o1) < (o2)) {
					return 1;
				} else if((o1) > (o2)) {
					return -1;
				} else {
					return 0;
				}
			}
		}
				);
		Writer out;
		try {
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("output.txt"),"UTF-8"));
			for (int ii=0; ii<result.length;ii++){
				Iterator<String> pairit2 = pairkeys.iterator();
				while (pairit2.hasNext()){
					pair_key1 = pairit2.next();
					if (pairs.get(pair_key1)-result[ii]==0)
						out.write(pair_key1+ " with "+ result[ii]);
						//System.out.println(pair_key1+ " with "+ result[ii]);
				}
			}
			out.close();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}

	/*private static double eucl_dist(double[] vector1, double[] vector2) {
		double dist=0;
		double sum=0;
		for (int ii=0;ii<vector1.length;ii++){
			sum=sum+Math.pow(vector1[ii]-vector2[ii],2);
		}
		dist=Math.sqrt(sum);
		return dist;
	}

	private static HashMap<String, double[]> files_estimate_pdf(
			HashMap<String, double[]> files_hist_lang1, int len2) {
		HashMap<String, double[]> result=new HashMap<String, double[]>();
		Set<String> keys_lang1=files_hist_lang1.keySet();
		Iterator<String> it1 = keys_lang1.iterator();
		while (it1.hasNext()){
			String filename1 = it1.next();
			double[] vector1 = files_hist_lang1.get(filename1);
			double[] pdf=estimate_pdf(vector1, len);
			result.put(filename1, pdf);
		}
		return result;
	}*/

/*	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static double[] estimate_pdf(double[] new_vector1, int len) {
		double[] result=new double[len];
		List b = Arrays.asList(ArrayUtils.toObject(new_vector1));
		double minval=Collections.min(b);
		double maxval=Collections.max(b);
		b=null;
		double[] weights=new double[new_vector1.length];
		double temp=1.0/new_vector1.length;
		for (int ii=0; ii<new_vector1.length-1;ii++)
			weights[ii]=temp;
		double med=median(new_vector1);
		double[] abs_new_vector1 = new double[new_vector1.length];
		for (int ii=0; ii<abs_new_vector1.length;ii++)
			abs_new_vector1[ii]=Math.abs(new_vector1[ii]-med);
		double sig=median(abs_new_vector1)/0.6745;
		if (sig<=0)
			sig = maxval-minval;
		double h=0;
		if (sig>0)
			h=sig*Math.pow(4.0/(3.0*new_vector1.length),0.2);
		else
			h=1;
		abs_new_vector1=null;
		double[] x=new double[len]; 
		x[0]=minval-3.0*h;
		x[len-1]=maxval+3.0*h;
		double step=(x[len-1]-x[0])/(len-1);
		for (int ii=1; ii<len-1;ii++)
			x[ii]=x[ii-1]+step;
		//repmat
		double[][] x_val=new double[new_vector1.length][len];
		for (int ii=0; ii<new_vector1.length;ii++){
			for (int jj=0; jj<len;jj++){
				x_val[ii][jj]=x[jj];
			}
		}
		//repmat
		double[][] xi_val=new double[new_vector1.length][len];
		for (int jj=0; jj<len;jj++){
			for (int ii=0; ii<new_vector1.length;ii++){
				xi_val[ii][jj]=new_vector1[ii];
			}
		}
		double[][] x_xi_val=new double[new_vector1.length][len];
		for (int jj=0; jj<len;jj++){
			for (int ii=0; ii<new_vector1.length;ii++){
				x_xi_val[ii][jj]=(x_val[ii][jj]-xi_val[ii][jj])/h;
			}
		}
		x_val=null;
		xi_val=null;
		double[][] normal_val=new double[new_vector1.length][len];
		temp=Math.sqrt(2*Math.PI);
		for (int jj=0; jj<len;jj++){
			for (int ii=0; ii<new_vector1.length;ii++){
				normal_val[ii][jj]=Math.exp(-0.5*Math.pow(x_xi_val[ii][jj], 2.0))/temp;
				//f = exp(-0.5 * z .^2) ./ sqrt(2*pi);
			}
		}
		x_xi_val=null;
		for (int jj=0; jj<len;jj++){
			result[jj]=0;
			for (int ii=0; ii<new_vector1.length;ii++)
				result[jj]=result[jj]+weights[ii]*normal_val[ii][jj];
		}
		return result;
	}*/
/*
	public static double median(double[] m) {
		int middle = m.length/2;
		if (m.length%2 == 1) {
			return m[middle];
		} else {
			return (m[middle-1] + m[middle]) / 2.0;
		}
	}

	private static HashMap<String, double[]> findTermsinFile(File files1,
			HashMap<Integer, String> topic1, String lang1) {

		HashMap<String, double[]> result=new HashMap<String, double[]>();
		FilenameFilter filter = new FilenameFilter() {			
			public boolean accept(File arg0, String arg1) {
				return (arg1.substring(arg1.length()-4).equals(".txt"));
			}
		};
		String[] files= files1.list(filter);

		for (int ii=0; ii<files.length ; ii++){
			String filename=files[ii];
			System.out.println(files[ii]);
			try {
				String filetext = readFileAsString(FilenameUtils.concat(files1,filename));
				ArrayList<String> stems =new ArrayList<String>();
				stems = analyze(filetext, lang1);
				String str="";
				for (String s:stems)
					str+=Constants.SPACE+s;
				str = str.trim();
				//Set<Integer> keys1=topic1.keySet();
				//Iterator<Integer> it = keys1.iterator();
				//int counter=0;
				double total_matches=0;
				double[] terms_freqs_in_file=new double[topic1.size()];
				//while (it.hasNext()){
				for (Integer jj=1; jj<topic1.size()+1;jj++){
					double matches=0;
					//String term=topic1.get(it.next());
					String term=topic1.get(jj);
					Pattern pattern = Pattern.compile(Constants.SPACE+term+Constants.SPACE);	
					Matcher matcher = pattern.matcher(Constants.SPACE+str+Constants.SPACE);
					//list with positions of a found term
					//ArrayList<String> termpos=new ArrayList<String>(); 
					while (matcher.find()) {
						//get the position of a found term
						//termpos.add(Integer.toString(matcher.start()));
						matches++;
					}
					//if (matches>0)
					//	System.out.println(term+"\t"+matches);
					terms_freqs_in_file[jj-1]=matches;//stems.size();
					//counter++;
					total_matches=total_matches+matches;
				}
				//StringTokenizer tkzr = new StringTokenizer(filetext);
				//int length_in_tok=tkzr.countTokens();
				//terms_freqs_in_file[counter]=(double) stems.size();
				//terms_freqs_in_file[counter]=(double) length_in_tok;
				//terms_freqs_in_file[counter]=total_matches;
				for (Integer jj=0; jj<topic1.size();jj++)
					terms_freqs_in_file[jj]=terms_freqs_in_file[jj] / total_matches;
					//terms_freqs_in_file[jj]=terms_freqs_in_file[jj]/(double) length_in_tok;
				result.put(filename, terms_freqs_in_file);
			} catch (IOException e) {
				System.err.println("Problem in reading file: " + files[ii]);
				e.printStackTrace();
			}
		}
		return result;
	}

	public static String readFileAsString(String filePath) throws java.io.IOException{
		byte[] buffer = new byte[(int) new File(filePath).length()];
		BufferedInputStream f = new BufferedInputStream(new FileInputStream(filePath));
		f.read(buffer);
		f.close();
		return new String(buffer);
	}
	

	public static HashMap<Integer, String> readTopicfile(File txtfile, String lang) {
		String str=null;
		HashMap<Integer, String> result = new HashMap<Integer, String>();
		Integer kk=1;
		try {
			BufferedReader in = new BufferedReader(new FileReader(txtfile));
			while ((str = in.readLine()) != null) {
				String[] temp=str.split(":");
				String[] temp1=temp[1].split("=");
				String term = temp1[0].trim();
				ArrayList<String> stemmed_terms = analyze(term, lang);
				String stemmed_term="";
				for (int ii=0;ii<stemmed_terms.size();ii++)
					stemmed_term=stemmed_term +Constants.SPACE+stemmed_terms.get(ii);
				result.put(kk,stemmed_term.trim());
				//System.out.println(stemmed_term.trim());
				kk++;
			}
			in.close();
		} catch (IOException e) {
			System.err.println("Problem in reading file: " + txtfile.getName());
		}
		return result;
	}
	

	public static ArrayList<String> analyze(String text, String lang) throws IOException  {
		ArrayList<String> stems = new ArrayList<String>();
		if (lang.equals("lt"))
			//TODO: Convert Analyzer to Lucene compatible analyzer
			stems = LithuanianAnalyzer.analyze(text);
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

	*/
	/*private static void writeFilehistog(HashMap<String, Double[]> files_hist1, String lang1) {
	Writer out_lang1;
	Set<String> keys_el=files_hist1.keySet();
	Iterator<String> it_el = keys_el.iterator();
	try {
		out_lang1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(lang1+"_files_terms.txt"),"UTF-8"));
		String string_key1="";
		while (it_el.hasNext()){
			string_key1 = it_el.next();
			Double temp[] = files_hist1.get(string_key1);
			out_lang1.write(string_key1);
			for (int ii=1;ii<temp.length;ii++)
				out_lang1.write("\t"+temp[ii].toString());
			out_lang1.write("\n");
		}
		out_lang1.close();
	} catch (UnsupportedEncodingException e) {
		e.printStackTrace();
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}

}*/
	//---------------------
	
/*	private static double[] calc_product(double[] vector1, double[] vector2) {
		double[] result=new double[2];
		result[0]=0.0; result[1]=0.0;
		for (int ii=0;ii<vector1.length;ii++){
			double temp=vector1[ii]*vector2[ii];
			if (temp>0)
				result[1]=result[1]+1;
			result[0]=result[0]+temp;
		}
		return result;
	}*/
	
	/*public static String convertStreamToString(InputStream is)
	throws IOException {

	 * To convert the InputStream to String we use the
	 * Reader.read(char[] buffer) method. We iterate until the
	 * Reader return -1 which means there's no more data to
	 * read. We use the StringWriter class to produce the string.

if (is != null) {
	Writer writer = new StringWriter();

	char[] buffer = new char[1024];
	try {
		Reader reader = new BufferedReader(
				new InputStreamReader(is, "UTF-8"));
		int n;
		while ((n = reader.read(buffer)) != -1) 
			writer.write(buffer, 0, n);
	} finally {
		is.close();
	}
	return writer.toString();
} else        
	return "";
}*/
}
