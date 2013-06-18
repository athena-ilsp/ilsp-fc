package gr.ilsp.fmc.main;

import gr.ilsp.fmc.exporter.SampleExporter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.io.MD5Hash;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


//import org.apache.commons.cli.Options;

public class DedupMD5 {
	private static final Logger LOGGER = Logger.getLogger(SampleExporter.class);
	private static File input;
	private static File out_textfile;
	private static boolean html=false;
	private static String input_type="xml";
	private static int MIN_TOKEN_LEN=3;
	private static float QUANT_RATE= (float) 0.01;
	private static int QAUNT_DEFAULT=5;
	private static String fs = System.getProperty("file.separator");
	//private static final String VAR_RES_CACHE = "/var/lib/tomcat6/webapps/soaplab2-results/";
	//private static final String HTTP_PATH = "http://sifnos.ilsp.gr/soaplab2-axis";
	//private static final String VAR_RES_CACHE = "/var/lib/tomcat6/webapps/soaplab2-results/";
	//private static final String HTTP_PATH = "http://nlp.ilsp.gr/soaplab2-results/";	
	//private static boolean applyOfflineXSLT = false;

	public static void dedup(String indirname, String outputfilename, String outputHTMLfilename,
									boolean applyOfflineXSLT){
		//modify indirname to be valid for windows
		String temp = indirname+fs+"xml";
		int tempid=temp.indexOf(":");
		if (tempid<0 || tempid<2)
			input= new File(temp);
		else
			input= new File(temp.substring(tempid+2, temp.length()));

		//System.out.println(input.getAbsolutePath());
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
		//outputdir.mkdir();
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
		//long start = System.nanoTime(); 
		long start = System.currentTimeMillis();
		String text="";
		HashMap<String, TextAttr> freqs = new HashMap<String, TextAttr>();
		byte[] texthashkey =null;
		String string_key="";
		String pairs="";

		for (int ii=0;ii<files.length;ii++){
			text = extractTextfromXML_clean(files[ii].getAbsolutePath());
			
			if (text.isEmpty())
				continue;
			texthashkey =calculate(text);
			string_key="";
			for (int jj=0;jj<texthashkey.length;jj++) {
				string_key += texthashkey[jj];
			}
			TextAttr t= new TextAttr(text.length(),files[ii].getName());
			if (freqs.containsKey(string_key)) {

				pairs = pairs + t.filename+ "\t\t" + freqs.get(string_key).filename + "\n";
				if (t.length>freqs.get(string_key).length){
					//System.out.println("OUT"+"\t"+freqs.get(string_key).filename);
					freqs.put(string_key, t);
					String temp2 = input.getPath()+fs+freqs.get(string_key).filename;
					delete(temp2);
					temp2 = temp2.replace("."+input_type,".html");
					delete(temp2);
					temp2 = temp2.replace("."+input_type,".xml.html");
					delete(temp2);
				}else{
					//System.out.println("OUT"+"\t"+t.filename);
					String temp2 = input.getPath()+fs+t.filename;
					delete(temp2);
					temp2 = temp2.replace("."+input_type,".html");
					delete(temp2);
					temp2 = temp2.replace("."+input_type,".xml.html");
					delete(temp2);
				}
			}
			else{
				//System.out.println("IN"+"\t"+t.filename);
				freqs.put(string_key, t);
			}
		}
		//System.out.println(pairs);
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

		writetextfile(out_textfile.getAbsolutePath(),urlList);
		if (html){
			File out_HTMLfile =new File(outputHTMLfilename);
			writeHTMLfile(out_HTMLfile.getAbsolutePath(),urlList,applyOfflineXSLT);
		}

		long elapsedTime = System.currentTimeMillis()-start;
		//System.out.println(counter + " files remained."); 
		//System.out.println("Duration: "+elapsedTime);
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

	//private static String extractTextfromXML_clean(String inputString) {
	public static String extractTextfromXML_clean(String inputString) {
		String result="";
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
			Document doc = db.parse(inputString);
			doc.getDocumentElement().normalize();
			NodeList nodeLstP = doc.getElementsByTagName("p");
			for(int s=0; s<nodeLstP.getLength() ; s++){
				Element NameElement = (Element)nodeLstP.item(s);
				if (!NameElement.hasAttribute("crawlinfo")){
					result+=NameElement.getTextContent();
					result+="\n";
				}
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	private static void writetextfile(String filename,String text) {
		Writer out;
		try {
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename),"UTF-8"));
			out.write(text.trim());
			out.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			System.err.println("Error in writing the output text file. The encoding is not supported.");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.println("Error in writing the output text file. The file does not exist.");
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error in writing the output text file.");
		}
	}

	private static void writeHTMLfile(String filename, String urlList,
			boolean applyOfflineXSLT2) {
		String outputfile1 =filename;
		String[] urls=urlList.split("\n");
		OutputStreamWriter xmlFileListWrt1;
		try {
			xmlFileListWrt1 = new OutputStreamWriter(new FileOutputStream(outputfile1),"UTF-8");
			xmlFileListWrt1.write("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
			for (int ii=0; ii<urls.length;ii++) {
				String ttt = urls[ii].toString();
				if (applyOfflineXSLT2)
					ttt = "<a href=\""+ttt+".html\">\n"+ttt+".html</a>";
				else
					ttt = "<a href=\""+ttt+"\">\n"+ttt+"</a>";
					
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

	public static void delete(String fileName) {
		try {
			// Construct a File object for the file to be deleted.
			File target = new File(fileName);

			if (!target.exists()) {
				//System.err.println("File " + fileName
				//		+ " not present to begin with!");
				return;
			}

			// Quick, now, delete it immediately:
			target.delete();
			//if (!target.delete())
				//System.err.println("** Deleted " + fileName + " **");
				//else
				//System.err.println("Failed to delete " + fileName);
		} catch (SecurityException e) {
			System.err.println("Unable to delete " + fileName + "("
					+ e.getMessage() + ")");
		}
	}

	public static void copy(String fromFileName, String toFileName)
			throws IOException {
		File fromFile = new File(fromFileName);
		File toFile = new File(toFileName);

		if (!fromFile.exists())
			throw new IOException("FileCopy: " + "no such source file: "
					+ fromFileName);
		if (!fromFile.isFile())
			throw new IOException("FileCopy: " + "can't copy directory: "
					+ fromFileName);
		if (!fromFile.canRead())
			throw new IOException("FileCopy: " + "source file is unreadable: "
					+ fromFileName);

		if (toFile.isDirectory())
			toFile = new File(toFile, fromFile.getName());

		if (toFile.exists()) {
			if (!toFile.canWrite())
				throw new IOException("FileCopy: "
						+ "destination file is unwriteable: " + toFileName);
			System.out.print("Overwrite existing file " + toFile.getName()
					+ "? (Y/N): ");
			System.out.flush();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					System.in));
			String response = in.readLine();
			if (!response.equals("Y") && !response.equals("y"))
				throw new IOException("FileCopy: "
						+ "existing file was not overwritten.");
		} else {
			String parent = toFile.getParent();
			if (parent == null)
				parent = System.getProperty("user.dir");
			File dir = new File(parent);
			if (!dir.exists())
				throw new IOException("FileCopy: "
						+ "destination directory doesn't exist: " + parent);
			if (dir.isFile())
				throw new IOException("FileCopy: "
						+ "destination is not a directory: " + parent);
			if (!dir.canWrite())
				throw new IOException("FileCopy: "
						+ "destination directory is unwriteable: " + parent);
		}
		FileUtils.copyFile(fromFile,toFile);
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

		//System.out.println(input.getAbsolutePath());
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
		//outputdir.mkdir();
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
		
		//long start = System.nanoTime(); 
		long start = System.currentTimeMillis();
		String text="";
		String string_key="";
		byte[] parhashkey =null;
		HashMap<String, HashSet<String>> fileshash= new HashMap<String, HashSet<String>>();
		HashMap<String, HashSet<Integer>> fileslengthhash= new HashMap<String, HashSet<Integer>>();
		HashMap<String, Integer> fileTextlength= new HashMap<String, Integer>();
		for (int ii=0;ii<files.length;ii++){
			text = extractTextfromXML_clean(files[ii].getAbsolutePath());
			String[] pars=text.split("\n");
			List<String> filelist=new ArrayList<String>();
			List<Integer> filelengthlist=new ArrayList<Integer>();
			for (int jj=0;jj<pars.length;jj++){
				parhashkey=MD5Hash.digest(pars[jj]).getDigest();
				string_key="";
				for (int kk=0;kk<parhashkey.length;kk++) {
					string_key += parhashkey[kk];
				}
				filelist.add(string_key);
				filelengthlist.add(pars[jj].length());
			}
			HashSet<String> fileset= new HashSet<String>(filelist);
			fileshash.put(files[ii].getName().toString(), fileset);
			HashSet<Integer> filelengthset= new HashSet<Integer>(filelengthlist);
			fileslengthhash.put(files[ii].getName().toString(), filelengthset);
			fileTextlength.put(files[ii].getName().toString(), text.length());
		}
		Set<String> keys=fileshash.keySet();
		Iterator<String> it = keys.iterator();
		String urlList="", string_key1 = "";
		String sourcefile="";
		int counter=0;
		while (it.hasNext()){
			string_key = it.next();
			HashSet<String> fileset=fileshash.get(string_key);
			Iterator<String> it1 = keys.iterator();
			counter++;
			while (it1.hasNext()){
				string_key1 = it1.next();
				if (string_key1.equals(string_key)) continue;
				HashSet<String> fileset1=fileshash.get(string_key1);
				HashSet intersection = new HashSet(fileset);
				intersection.retainAll(fileset1);
				double t=Double.parseDouble(Integer.toString(fileset.size()));
				double t1=Double.parseDouble(Integer.toString(fileset1.size()));
				double ti=Double.parseDouble(Integer.toString(intersection.size()));

				double tu=t+t1-ti;
				if (ti/t >0.9 || ti/t1 >0.9 || ti/tu >0.9){
					//System.out.println(string_key+" pair with "+ string_key1);
					if (fileTextlength.get(string_key1)>fileTextlength.get(string_key)){
						//System.out.println("OUT"+"\t"+freqs.get(string_key).filename);
						//freqs.put(string_key, t);
						String temp2 = input.getPath()+fs+string_key;
						delete(temp2);
						temp2 = temp2.replace("."+input_type,".html");
						delete(temp2);
						temp2 = temp2.replace("."+input_type,".xml.html");
						delete(temp2);
					}else{
						//System.out.println("OUT"+"\t"+t.filename);
						String temp2 = input.getPath()+fs+string_key1;
						delete(temp2);
						temp2 = temp2.replace("."+input_type,".html");
						delete(temp2);
						temp2 = temp2.replace("."+input_type,".xml.html");
						delete(temp2);
					}
				}

			}
		}
		File[] files1=input.listFiles(filter);
		counter=0;
		//while (it.hasNext()){
		for (int ii=0;ii<files1.length;ii++){
			//string_key = files1[ii].getName();
			//try {
			sourcefile = files1[ii].getName(); //freqs.get(string_key).filename;
			String temp1 = outputdir.getAbsolutePath().replace("\\","/");
			urlList=urlList + temp1+fs+sourcefile+"\n";
			counter++;
		}
		writetextfile(out_textfile.getAbsolutePath(),urlList);
		if (html){
			File out_HTMLfile =new File(outputHTMLfilename);
			writeHTMLfile(out_HTMLfile.getAbsolutePath(),urlList,applyOfflineXSLT);
		}

		long elapsedTime = System.currentTimeMillis()-start;
		//System.out.println(counter + " files remained."); 
		//System.out.println("Duration: "+elapsedTime);
		LOGGER.info("New Deduplication completed in " + elapsedTime + " milliseconds. "+ counter +  " files remained.");
	}

	/*public static void deduppars(String indirname, String outputfilename, String outputHTMLfilename) {
		//modify indirname to be valid for windows
		String temp = indirname+fs+"data";
		String dir_txts = temp+fs+"pars_dedup_txt";
		int tempid=temp.indexOf(":");
		if (tempid<0 || tempid<2)
			input= new File(temp);
		else
			input= new File(temp.substring(tempid+2, temp.length()));

		System.out.println(input.getAbsolutePath());
		if (!input.exists() || !input.isDirectory()){
			System.err.println( "the directory with the cesdoc files does not exist!!!!!!!!" );			
			System.exit(64);
		}

		FilenameFilter filter = new FilenameFilter() {			
			public boolean accept(File arg0, String arg1) {
				return (arg1.substring(arg1.length()-(input_type.length()+1)).equals("."+input_type));
			}
		};
		File[] files=input.listFiles(filter);
		if (files.length<2){
			//System.err.println("The input list contains less than 2 files.");
			LOGGER.info("The input list contains less than 2 files.");
			//return;
			System.exit(64);
		}
		//else
		//	System.out.println(files.length+" files will be processed.");
		//long start = System.nanoTime(); 
		String text="";
		String string_key="";
		String new_string="";
		byte[] parhashkey =null;
		String txtfilename="";
		HashMap<String, String> parshash= new HashMap<String, String>();
		for (int ii=0;ii<files.length;ii++){
			txtfilename = dir_txts+fs+files[ii].getName()+".txt";
			Writer out;
			try {
				out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(txtfilename),"UTF-8"));	
				text = extractTextfromXML_clean(files[ii].getAbsolutePath());
				String[] pars=text.split("\n");
				for (int jj=0;jj<pars.length;jj++){
					parhashkey=MD5Hash.digest(pars[jj]).getDigest();
					string_key="";
					for (int kk=0;kk<parhashkey.length;kk++) 
						string_key += parhashkey[kk];
					if (parshash.containsKey(string_key)){
						new_string=parshash.get(string_key)+","+files[ii].getName();
						parshash.put(string_key, new_string);
						//System.out.println(new_string);
						//System.out.println(pars[jj]);
					}else{
						out.write(pars[jj]+"\n");
						parshash.put(string_key, files[ii].getName());
					}
				}
				out.close();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				System.err.println("Error in writing the output text file. The encoding is not supported.");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.err.println("Error in writing the output text file. The file does not exist.");
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Error in writing the output text file.");
			}
		}
	}
*/

	/*public static void deduppars(String indirname) {
		//modify indirname to be valid for windows
		String temp = indirname;
		String dir_txts = temp+fs+"pars_dedup_txt";
		int tempid=temp.indexOf(":");
		if (tempid<0 || tempid<2)
			input= new File(temp);
		else
			input= new File(temp.substring(tempid+2, temp.length()));

		//System.out.println(input.getAbsolutePath());
		if (!input.exists() || !input.isDirectory()){
			System.err.println( "the directory with the cesdoc files does not exist!!!!!!!!" );			
			System.exit(64);
		}

		FilenameFilter filter = new FilenameFilter() {			
			public boolean accept(File arg0, String arg1) {
				return (arg1.substring(arg1.length()-(input_type.length()+1)).equals("."+input_type));
			}
		};
		File[] files=input.listFiles(filter);
		if (files.length<2){
			//System.err.println("The input list contains less than 2 files.");
			LOGGER.info("The input list contains less than 2 files.");
			//return;
			System.exit(64);
		}
		else
			LOGGER.info(files.length+" files will be processed.");
		//long start = System.nanoTime(); 
		String text="";
		String string_key="";
		String new_string="";
		byte[] parhashkey =null;
		String txtfilename="";
		HashMap<String, String> parshash= new HashMap<String, String>();
		for (int ii=0;ii<files.length;ii++){
			txtfilename = dir_txts+fs+files[ii].getName()+".txt";
			Writer out;
			try {
				out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(txtfilename),"UTF-8"));	
				text = extractTextfromXML_clean(files[ii].getAbsolutePath());
				String[] pars=text.split("\n");
				for (int jj=0;jj<pars.length;jj++){
					parhashkey=MD5Hash.digest(pars[jj]).getDigest();
					string_key="";
					for (int kk=0;kk<parhashkey.length;kk++) {
						string_key += parhashkey[kk];
					}
					if (parshash.containsKey(string_key)){
						new_string=parshash.get(string_key)+","+files[ii].getName();
						parshash.put(string_key, new_string);
						//System.out.println(new_string);
						//System.out.println(pars[jj]);
					}else{
						out.write(pars[jj]+"\n");
						parshash.put(string_key, files[ii].getName());
					}
				}
				out.close();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				System.err.println("Error in writing the output text file. The encoding is not supported.");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.err.println("Error in writing the output text file. The file does not exist.");
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Error in writing the output text file.");
			}
		}
	}
*/
	
	/*public static void dedupNew(String indirname, String outputfilename, String outputHTMLfilename,
			 boolean applyOfflineXSLT){
		//modify indirname to be valid for windows
		String temp = indirname+"/xml";
		int tempid=temp.indexOf(":");
		if (tempid<0)
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
		File outputdir= new File(input.getParent()+fs+"xmlDedup");
		outputdir.mkdir();

		FilenameFilter filter = new FilenameFilter() {			
			public boolean accept(File arg0, String arg1) {
				return (arg1.substring(arg1.length()-(input_type.length()+1)).equals("."+input_type));
			}
		};
		File[] files=input.listFiles(filter);
		if (files.length<2){
			System.err.println("The input list contains less than 2 files.");
			//return;
			System.exit(64);
		}
		//else
			//System.out.println(files.length+" files will be processed.");
		//long start = System.nanoTime(); 
		String text="";
		HashMap<String, TextAttr> freqs = new HashMap<String, TextAttr>();
		byte[] texthashkey =null;
		String string_key="";
		String pairs="";
		//String tempfile="";
		for (int ii=0;ii<files.length;ii++){
			text = extractTextfromXML_clean(files[ii].getAbsolutePath());
			if (text.isEmpty())
				continue;
			texthashkey =calculate(text);
			string_key="";
			for (int jj=0;jj<texthashkey.length;jj++) {
				string_key += texthashkey[jj];
			}
			TextAttr t= new TextAttr(text.length(),files[ii].getName());
			if (freqs.containsKey(string_key)) {
				pairs = pairs + t.filename+ "\t\t" + freqs.get(string_key).filename + "\n";
				if (t.length>freqs.get(string_key).length){
					freqs.put(string_key, t);
					String temp2 = input.getPath()+fs+freqs.get(string_key).filename;
					delete(temp2);
					temp2 = temp2.replace("."+input_type,".html");
					delete(temp2);
					temp2 = temp2.replace("."+input_type,".xml.html");
					delete(temp2);
				}else{
					String temp2 = input.getPath()+fs+t.filename;
					delete(temp2);
					temp2 = temp2.replace("."+input_type,".html");
					delete(temp2);
					temp2 = temp2.replace("."+input_type,".xml.html");
					delete(temp2);
				}
			}
			else
				freqs.put(string_key, t);
		}
		Set<String> keys=freqs.keySet();
		Iterator<String> it = keys.iterator();
		String urlList = "";
		String sourcefile="";
		//int counter=0;
		while (it.hasNext()){
			string_key = it.next();
			try {
				sourcefile = freqs.get(string_key).filename;
				copy(input.getAbsolutePath()+fs+sourcefile,outputdir.getAbsolutePath()+fs+sourcefile);
				String temp1 = outputdir.getAbsolutePath().replace("\\","/");
				//temp1 = temp1.replace(VAR_RES_CACHE,HTTP_PATH);
				//temp1=temp1.substring(temp1.indexOf("http:"));
				//urlList=urlList + temp1.replace(VAR_RES_CACHE, HTTP_PATH)+fs+sourcefile+"\n";
				urlList=urlList + temp1+fs+sourcefile+"\n";
				
				//counter++;
				sourcefile = freqs.get(string_key).filename.replace(".xml", ".html");
				copy(input.getPath()+fs+sourcefile,outputdir.getPath()+fs+sourcefile);
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}
		writetextfile(out_textfile.getAbsolutePath(),urlList);
		if (html){
			File out_HTMLfile =new File(outputHTMLfilename);
			writeHTMLfile(out_HTMLfile.getAbsolutePath(),urlList,applyOfflineXSLT);
		}

		//long elapsedTime = System.nanoTime() - start;
		//System.out.println(counter + " files remained."); 
		//System.out.println("Duration: "+elapsedTime);
	}*/
	
}
