package gr.ilsp.fmc.main;

import gr.ilsp.fmc.exporter.SampleExporter;
import gr.ilsp.fmc.utils.TopicTools;

//import java.io.BufferedReader;
//import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
//import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
//import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.parsers.ParserConfigurationException;

//import org.apache.commons.io.FileUtils;
import org.apache.hadoop.io.MD5Hash;
import org.apache.log4j.Logger;
//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
//import org.w3c.dom.NodeList;
//import org.xml.sax.SAXException;


//import org.apache.commons.cli.Options;

public class DedupMD5 {
	private static final Logger LOGGER = Logger.getLogger(SampleExporter.class);
	private static File input;
	private static File out_textfile;
	private static boolean html=false;
	private static String input_type="xml";
	private static int MIN_TOKEN_LEN=3;	//tokens with less than MIN_TOKEN_LEN are excluded
	private static float QUANT_RATE= (float) 0.01;
	private static int QAUNT_DEFAULT=5; // quantization interval 
	private static double inter_thr=0.7;
	private static int min_tok_len=3;
	private static String fs = System.getProperty("file.separator");
	private static String methodtype="0";
	//private static String tobe_replaced ="( between | among | other | another | due | because | either | my | your | his | her | its | our | their | off)";
	
	//private static final String VAR_RES_CACHE = "/var/lib/tomcat6/webapps/soaplab2-results/";
	//private static final String HTTP_PATH = "http://sifnos.ilsp.gr/soaplab2-axis";
	//private static final String VAR_RES_CACHE = "/var/lib/tomcat6/webapps/soaplab2-results/";
	//private static final String HTTP_PATH = "http://nlp.ilsp.gr/soaplab2-results/";	
	//private static boolean applyOfflineXSLT = false;

	public static void main(String[] args) {
		String outputDirName =args[0];
		String file1 = outputDirName+fs+args[1];
		String file2 = outputDirName+fs+args[2];
		if (args.length>3)
			methodtype = args[3];
		if (args.length>4)
			inter_thr = Double.parseDouble(args[4]);
		if (args.length>5)
			min_tok_len = Integer.parseInt(args[5]);
		
		if (methodtype.equals("1")){
			LOGGER.info("Deduplication by using lists and MD5 method.");
			DedupMD5.dedup(outputDirName, file1,file2,true);
		}
		if (methodtype.equals("2")){
			LOGGER.info("Deduplication based on common paragraphs.");
			DedupMD5.dedupnew(outputDirName, file1,file2,true);
		}
		if (methodtype.equals("0")){
			LOGGER.info("Deduplication by using lists and MD5 method.");
			DedupMD5.dedup(outputDirName, file1,file2,true);
			LOGGER.info("Deduplication based on common paragraphs.");
			DedupMD5.dedupnew(outputDirName, file1,file2,true);
		}
		Bitexts.counttokens(outputDirName+fs+"xml","xml","crawlinfo=");
		System.out.println("tokens calculated.");
	}

	public static void dedup(String indirname, String outputfilename, String outputHTMLfilename,
			boolean applyOfflineXSLT){
		//modify indirname to be valid for windows
		String temp = indirname+fs+"xml";
		int tempid=temp.indexOf(":");
		if (tempid<0 || tempid<2)
			input= new File(temp);
		else
			input= new File(temp.substring(tempid+2, temp.length()));

		if (!input.exists() || !input.isDirectory()){
			System.err.println( "the directory with the cesdoc files does not exist!!!!!!!!" );			
			System.exit(64);
		}
		out_textfile =new File(outputfilename);
		if (!out_textfile.exists()){
			System.err.println( "List of cesdoc files does not exist!!!!!!!!" );			
			System.exit(64);
		}
		if (!(outputHTMLfilename==null)){
			html=true;
		}
		File outputdir= new File(input.getParent()+fs+"xml");
		FilenameFilter filter = new FilenameFilter() {			
			public boolean accept(File arg0, String arg1) {
				return (arg1.substring(arg1.length()-(input_type.length()+1)).equals("."+input_type));
			}
		};
		File[] files=input.listFiles(filter);
		if (files.length<2){
			LOGGER.info("The input list contains less than 2 files.");
			return;
		}
		else
			LOGGER.info(files.length+" files will be processed.");

		long start = System.currentTimeMillis();
		String text="";
		HashMap<String, TextAttr> freqs = new HashMap<String, TextAttr>();
		byte[] texthashkey =null;
		String string_key="";
		String pairs="";

		for (int ii=0;ii<files.length;ii++){
			text = ReadResources.extractTextfromXML_clean(files[ii].getAbsolutePath(),"p","crawlinfo");
			LOGGER.debug(files[ii].getAbsolutePath());
			if (text.isEmpty())
				continue;
			texthashkey =calculate(text); //TODO should text be stemmed?
			string_key="";
			for (int jj=0;jj<texthashkey.length;jj++) {
				string_key += texthashkey[jj];
			}
			TextAttr t= new TextAttr(text.length(),files[ii].getName());
			if (freqs.containsKey(string_key)) {
				pairs = pairs + t.filename+ "\t\t" + freqs.get(string_key).filename + "\n";
				if (t.length>freqs.get(string_key).length){
					LOGGER.debug("OUT"+"\t"+freqs.get(string_key).filename);
					freqs.put(string_key, t);
					String temp2 = input.getPath()+fs+freqs.get(string_key).filename;
					gr.ilsp.fmc.utils.FileUtils.delete(temp2);
					temp2 = temp2.replace("."+input_type,".html");
					gr.ilsp.fmc.utils.FileUtils.delete(temp2);
					temp2 = temp2.replace(".html",".xml.html");
					gr.ilsp.fmc.utils.FileUtils.delete(temp2);
				}else{
					LOGGER.debug("OUT"+"\t"+t.filename);
					String temp2 = input.getPath()+fs+t.filename;
					gr.ilsp.fmc.utils.FileUtils.delete(temp2);
					temp2 = temp2.replace("."+input_type,".html");
					gr.ilsp.fmc.utils.FileUtils.delete(temp2);
					temp2 = temp2.replace(".html",".xml.html");
					gr.ilsp.fmc.utils.FileUtils.delete(temp2);
				}
			}
			else{
				freqs.put(string_key, t);
			}
		}
		LOGGER.debug(pairs);
		Set<String> keys=freqs.keySet();
		Iterator<String> it = keys.iterator();
		String urlList = "";
		String sourcefile="";
		int counter=0;
		while (it.hasNext()){
			string_key = it.next();
			//try {
			sourcefile = freqs.get(string_key).filename;
			String temp1 = outputdir.getAbsolutePath().replace("\\","/");
			//temp1 = temp1.replace(VAR_RES_CACHE,HTTP_PATH);
			//temp1=temp1.substring(temp1.indexOf("http:"));
			//urlList=urlList + temp1.replace(VAR_RES_CACHE, HTTP_PATH)+fs+sourcefile+"\n";
			urlList=urlList + temp1+fs+sourcefile+"\n";
			counter++;
		}

		ReadResources.writetextfile(out_textfile.getAbsolutePath(),urlList);
		if (html){
			File out_HTMLfile =new File(outputHTMLfilename);
			writeHTMLfile(out_HTMLfile.getAbsolutePath(),urlList,applyOfflineXSLT);
		}

		long elapsedTime = System.currentTimeMillis()-start;
		LOGGER.info("Deduplication completed in " + elapsedTime + " milliseconds. "+ counter +  " files remained.");
	}

	private static class TextAttr {
		public int length;
		public String filename;

		public TextAttr(int length, String filename) {
			this.length = length;
			this.filename = filename;
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

	private static byte[] calculate(String text) {
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
		//System.out.println(newText.toString());
		return MD5Hash.digest(newText.toString()).getDigest();
	}
			
	static void writeHTMLfile(String filename, String urlList,
			boolean applyOfflineXSLT2) {
		String outputfile1 =filename;
		String[] urls=urlList.split("\n");
		OutputStreamWriter xmlFileListWrt1;
		try {
			xmlFileListWrt1 = new OutputStreamWriter(new FileOutputStream(outputfile1),"UTF-8");
			xmlFileListWrt1.write("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
			for (int ii=0; ii<urls.length;ii++) {
				String ttt;// = urls[ii];//.toString();
				File xmlFile=new File(urls[ii]);
				//String ttt=qq.toURI().toString();
				URL fileURL = xmlFile.toURI().toURL();
				if (applyOfflineXSLT2)
					ttt = "<a href=\""+fileURL+".html\">\n"+fileURL+".html</a>";
				else
					ttt = "<a href=\""+fileURL+"\">\n"+fileURL+"</a>";

				//<a href="https://issues.apache.org/jira/browse/NUTCH-721" target="_blank">NUTCH-721</a>
				xmlFileListWrt1.write("<br />"+ttt+"\n");
				//xmlFileListWrt.write(xmlFile.replace(VAR_RES_CACHE, HTTP_PATH).replace("file:", "")   +"\n");
			}
			xmlFileListWrt1.write("</html>");
			xmlFileListWrt1.close();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void dedupnew(String indirname, String outputfilename, String outputHTMLfilename, 
			boolean applyOfflineXSLT) {
		//modify indirname to be valid for windows
		String temp = indirname+fs+"xml";
		int tempid=temp.indexOf(":");
		if (tempid<0 || tempid<2)
			input= new File(temp);
		else
			input= new File(temp.substring(tempid+2, temp.length()));

		if (!input.exists() || !input.isDirectory()){
			System.err.println( "the directory with the cesdoc files does not exist!!!!!!!!" );			
			System.exit(64);
		}
		out_textfile =new File(outputfilename);
		if (!out_textfile.exists()){
			System.err.println( "List of cesdoc files does not exist!!!!!!!!" );			
			System.exit(64);
		}
		if (!(outputHTMLfilename==null)){
			html=true;
		}
		File outputdir= new File(input.getParent()+fs+"xml");
		FilenameFilter filter = new FilenameFilter() {			
			public boolean accept(File arg0, String arg1) {
				return (arg1.substring(arg1.length()-(input_type.length()+1)).equals("."+input_type));
			}
		};
		File[] files=input.listFiles(filter);
		if (files.length<2){
			LOGGER.info("The input list contains less than 2 files.");
			return;
		}

		long start = System.currentTimeMillis();
		String text="";
		String string_key="";
		byte[] parhashkey =null;
		HashMap<String, HashSet<String>> fileshash= new HashMap<String, HashSet<String>>();
		//HashMap<String, HashSet<Integer>> fileslengthhash= new HashMap<String, HashSet<Integer>>();
		HashMap<String, Integer> fileTextlength= new HashMap<String, Integer>();
		int cents=0;
		HashMap<String, List<Integer>> filename_parlengths = new HashMap<String, List<Integer>>();
		HashMap<String, List<String>> filename_parkeys = new HashMap<String, List<String>>();
		for (int ii=0;ii<files.length;ii++){
			text = ReadResources.extractTextfromXML_clean(files[ii].getAbsolutePath(),"p","crawlinfo");
			//System.out.println(text);
			String langIdentified = ReadResources.extractLangfromXML(files[ii].getAbsolutePath(), "language", "iso639");
			String[] pars=text.split("\n");
			List<String> filelist=new ArrayList<String>();
			List<Integer> filelengthlist=new ArrayList<Integer>();
			for (int jj=0;jj<pars.length;jj++){
				//stem the paragraph
				String tempstr=pars[jj].replaceAll("[0-9]", "");
				//tempstr=tempstr.replaceAll("\\[.*?\\]", "");
				//tempstr = tempstr.replaceAll(tobe_replaced, " ");
				ArrayList<String> stems =new ArrayList<String>();
				try {
					stems = TopicTools.analyze(tempstr, langIdentified);
				} catch (IOException e) {
					LOGGER.warn(e.getMessage());
				}
				//concatenate stems 
				tempstr="";
				for (String s:stems){
					if (s.length()>min_tok_len)
						tempstr+=" "+s;
				}
				tempstr = tempstr.replaceAll("(\\s){2,}", " ");
				tempstr = tempstr.trim();
				if (tempstr.isEmpty()){
					LOGGER.debug/*System.out.println*/(pars[jj]);
					continue;
				}
				LOGGER.debug/*System.out.println*/(pars[jj]);
				LOGGER.debug/*System.out.println*/(tempstr);
				//System.out.println(pars[jj]);
				//System.out.println(tempstr);
				
				parhashkey=MD5Hash.digest(tempstr).getDigest();
				string_key="";
				for (int kk=0;kk<parhashkey.length;kk++) {
					string_key += parhashkey[kk];
				}
				filelist.add(string_key);
				//filelengthlist.add(pars[jj].length());
				StringTokenizer st = new StringTokenizer(pars[jj]);	
				filelengthlist.add(st.countTokens());
			}
			HashSet<String> fileset= new HashSet<String>(filelist);
			fileshash.put(files[ii].getName().toString(), fileset);
			//HashSet<Integer> filelengthset= new HashSet<Integer>(filelengthlist);
			//fileslengthhash.put(files[ii].getName().toString(), filelengthset);
			
			//fileTextlength.put(files[ii].getName().toString(), text.length());
			int len_in_toks = 0;
			for (int jj=0;jj<filelengthlist.size();jj++){
				len_in_toks+=filelengthlist.get(jj);
			}
			fileTextlength.put(files[ii].getName().toString(),len_in_toks);
			filename_parlengths.put(files[ii].getName().toString(), filelengthlist);
			filename_parkeys.put(files[ii].getName().toString(), filelist);
			if (filelist.isEmpty())
				System.out.println(files[ii].getName().toString());
			if (filelengthlist.isEmpty())
				System.out.println(files[ii].getName().toString());
			if (ii/100>cents){
				cents++;
				LOGGER.info("Lists for more than "+ cents*100+" files have been created.");
			}
		}
		Set<String> keys=fileshash.keySet();
		Iterator<String> it = keys.iterator();
		String urlList="", string_key1 = "";
		String sourcefile="";
		//ArrayList<String> checked = new ArrayList<String>();
		HashSet<String> checked1 = new HashSet<String>();
		int counter=0;
		cents=0;
		while (it.hasNext()){
			string_key = it.next();
			if (checked1.contains(string_key))
				continue;
			//HashSet<String> fileset=fileshash.get(string_key);
			Iterator<String> it1 = keys.iterator();
			counter++;
			//int counter1=0;
			while (it1.hasNext()){
				string_key1 = it1.next();
				if (string_key1.equals(string_key))
					continue;
				if (checked1.contains(string_key) | checked1.contains(string_key1))
					continue;
				
				//intersection of 2 lists and count tokens of common paragraphs
				double common_pars_length=0;
				List<String> tempkeylist = filename_parkeys.get(string_key);
				double t=Double.parseDouble(Integer.toString(tempkeylist.size()));
				
				List<String> tempkeylist1 = filename_parkeys.get(string_key1); 
				List<Integer> tempparlist1 = filename_parlengths.get(string_key1); 
				double t1=Double.parseDouble(Integer.toString(tempkeylist1.size()));
				
				if (t==0){
					System.out.println(string_key);
				}
				if (t1==0){
					System.out.println(string_key1);
				}	
				//double ti=0;
				for(int i = tempkeylist1.size() - 1; i > -1; --i){
				    String str = tempkeylist1.get(i);
				    /*if(!tempkeylist.remove(str)){tempkeylist1.remove(str);}
				     *else{common_pars_length+=tempparlist1.get(i);}*/
				    if(tempkeylist.contains(str)){
				    	common_pars_length+=tempparlist1.get(i);
				    	//ti++;
			    	}
				}
				//double ti=Double.parseDouble(Integer.toString(tempkeylist1.size()));
				
				LOGGER.debug("CHECK: "+ string_key + " with " +t +"\tpars TO\t" +	string_key1 + " with "+ t1+ "pars");
				/*HashSet<String> fileset1=fileshash.get(string_key1);
				HashSet intersection = new HashSet(fileset);
				intersection.retainAll(fileset1);
				double t=Double.parseDouble(Integer.toString(fileset.size()));
				double t1=Double.parseDouble(Integer.toString(fileset1.size()));
				double ti=Double.parseDouble(Integer.toString(intersection.size()));*/
				//double tu=t+t1-ti;
				//if (ti/t >0.9 || ti/t1 >0.9 || ti/tu >0.9){
				//double tu=t+t1-2*ti;
				if (/*((ti/t >inter_thr || ti/t1 >inter_thr) 
						&& (common_pars_length/fileTextlength.get(string_key1) > inter_thr
						|| common_pars_length/fileTextlength.get(string_key) >inter_thr))
					||*/ common_pars_length/fileTextlength.get(string_key1) > inter_thr
					|| common_pars_length/fileTextlength.get(string_key) >inter_thr){	
					LOGGER.debug(string_key+" pair with "+ string_key1);
					System.out.println(string_key+" pair with "+ string_key1);
					if (fileTextlength.get(string_key1)>fileTextlength.get(string_key)){
						//System.out.println("OUT"+"\t"+freqs.get(string_key).filename);
						//freqs.put(string_key, t);
						String temp2 = input.getPath()+fs+string_key;
						gr.ilsp.fmc.utils.FileUtils.delete(temp2);
						/*try {
							FileUtils.forceDelete(new File(temp2));
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}*/
						temp2 = temp2.replace("."+input_type,".html");
						gr.ilsp.fmc.utils.FileUtils.delete(temp2);
						temp2 = temp2.replace(".html",".xml.html");
						gr.ilsp.fmc.utils.FileUtils.delete(temp2);
						checked1.add(string_key);
					}else{
						//System.out.println("OUT"+"\t"+t.filename);
						String temp2 = input.getPath()+fs+string_key1;
						gr.ilsp.fmc.utils.FileUtils.delete(temp2);
						temp2 = temp2.replace("."+input_type,".html");
						gr.ilsp.fmc.utils.FileUtils.delete(temp2);
						temp2 = temp2.replace(".html",".xml.html");
						gr.ilsp.fmc.utils.FileUtils.delete(temp2);
						checked1.add(string_key1);
					}
				}
				//counter1++;
			}
			checked1.add(string_key);
			//System.out.println(counter1 +" files");
			if (counter/1000>cents){
				cents++;
				LOGGER.info("more than "+ cents*1000+" files have been checked.");
			}
		}
		File[] files1=input.listFiles(filter);
		counter=0;
		for (int ii=0;ii<files1.length;ii++){
			sourcefile = files1[ii].getName(); //freqs.get(string_key).filename;
			String temp1 = outputdir.getAbsolutePath().replace("\\","/");
			urlList=urlList + temp1+fs+sourcefile+"\n";
			counter++;
		}
		ReadResources.writetextfile(out_textfile.getAbsolutePath(),urlList);
		if (html){
			File out_HTMLfile =new File(outputHTMLfilename);
			writeHTMLfile(out_HTMLfile.getAbsolutePath(),urlList,applyOfflineXSLT);
		}

		long elapsedTime = System.currentTimeMillis()-start;
		LOGGER.info("New Deduplication completed in " + elapsedTime + " milliseconds. "+ counter +  " files remained.");
	}

}
