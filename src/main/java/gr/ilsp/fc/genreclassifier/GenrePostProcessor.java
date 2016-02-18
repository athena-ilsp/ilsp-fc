package gr.ilsp.fc.genreclassifier;

import gr.ilsp.fc.main.ReadResources;
import gr.ilsp.fc.main.WriteResources;
import gr.ilsp.fc.utils.LicensePostProcessor;
import gr.ilsp.fc.utils.TopicTools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
//import java.util.List;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class GenrePostProcessor {

	private File directory;
	private String keywords;
	private static int max_nGramOrder=5;
	private static int type=1;
	private static String l1="";
	private static String l2="";
	private String pairlist="";
	private static final String TagName = "p";
	private static final String TagGenre1 = "<genre>";
	private static final String TagGenre2 = "</genre>";
	private static final String TagGenre3 = "<genre />";
	private static final String ProcFileExt="xml";
	private static final String appXMLext = ".xml";
	private static final String appHTMLext = ".html";
	private static final String TempFileExt=".tmp";
	//private static final String NgramFileExt=".ngr";
	private static final String fs = System.getProperty("file.separator");
	//private static final String rarechar="~";
	//private static final String space_char=" ";	
	//private static final String ngramExec="ngran-count";
	private static final String underscore="_";
	private static final String separator=",";
	private static final String page_url ="eAddress";
	private static final String page_title = "title";
	private static final String page_genre = "genre";
	private static final String paragraph = "p";
	private static final String ooi_text = "crawlinfo";
	
	
	static Logger logger = LoggerFactory.getLogger(LicensePostProcessor.class.getCanonicalName());



	public static void main(String... args) throws Exception {
		type = Integer.parseInt(args[0]);
		File directory = new File(args[1]);
		GenrePostProcessor gpc = new GenrePostProcessor();
		gpc.setDirectory(directory);
		gpc.setMethod(type);
		if (args.length>3){
			gpc.setLang1(args[3]);
			gpc.setLang2(args[4]);
			gpc.setPairlist(args[5]);
		}
				
		logger.info("Input Directory: " + directory.toString());

		switch (type) {
			case 0:{//just counts tokens and docs of a collection that is already annotated on genre level. 
				logger.info("Statistics in a collection with documents that are already classified into genre classes");
				gpc.setKeywords(args[2]);
				gpc.run0();
			}
			case 1: {
				logger.info("Genre classification based on ngrams");
				max_nGramOrder =  Integer.parseInt(args[2]);
				logger.info("Selected Ngram Orders are up to: " + max_nGramOrder);
				gpc.run1();
			}
			case 2:{
				logger.info("Genre classification based on keywrods");
				gpc.setKeywords(args[2]);
				gpc.run2();
			}
			case 3:{
				logger.info("Genre classification based on keywrods");
				gpc.setKeywords(args[2]);
				gpc.run3();
			}
		}
		
	}

	
	private void run3() throws IOException {
		List<String> genres_features=FileUtils.readLines(new File(this.keywords));
		String[] genres = new String[genres_features.size()];
		String[] features = new String[genres_features.size()];
		for (int ii=0;ii<genres_features.size();ii++){
			String[] temp= genres_features.get(ii).split("\t");
			genres[ii]=temp[0]; 	
			if (temp.length>1)
				features[ii]=temp[1].toLowerCase();
			else
				features[ii]=" ";
		}
		List<String> files = FileUtils.readLines(new File(this.pairlist));
		logger.info("Pairs are: "+ files.size());
			
		String doc_title1, doc_title2, doc_url1, doc_url2, text1, text2, doc_genre;
		int[] tokens_in_genres1 = new int[genres_features.size()];
		int[] tokens_in_genres2 = new int[genres_features.size()];
		int[] files_in_genres1 = new int[genres_features.size()];
		int[] files_in_genres2 = new int[genres_features.size()];
		int found1, found2, found, alr_genre_counter=0;
		File tempfile ;
		for (int ii=0;ii<files.size();ii++){
			tempfile = new File(FilenameUtils.concat("C:\\QTLaunchPad\\MEDICAL\\PT-EL\\delivered",files.get(ii)));
			if (!tempfile.getName().contains(underscore)){
				logger.error("Not a proper pair");
				System.exit(0);
			}
			String[] tmp;
			String f1,f2, f11, f22;
			if (tempfile.getAbsolutePath().contains("pdfs")){
				//System.out.println(tempfile.getName());
				tmp=tempfile.getName().split(underscore+this.l1+underscore);
				f1 = FilenameUtils.concat(tempfile.getParent(),tmp[0]+underscore+this.l1+appXMLext);
				f2 = FilenameUtils.concat(tempfile.getParent(),tmp[1]);
			}else{
				//continue;
				tmp=tempfile.getName().split(underscore);
				f1 = FilenameUtils.concat(tempfile.getParent(),tmp[0]+appXMLext);
				f2 = FilenameUtils.concat(tempfile.getParent(),tmp[1]+appXMLext);	
				f11 = FilenameUtils.concat(tempfile.getParent(),tmp[0]+appHTMLext);
				f22 = FilenameUtils.concat(tempfile.getParent(),tmp[1]+appHTMLext);
				text1 = FileUtils.readFileToString(new File(f11));
				text2 = FileUtils.readFileToString(new File(f22));
			}
			text1 = FileUtils.readFileToString(new File(f1));
			if (!tempfile.getAbsolutePath().contains("pdfs")){
				if (text1.contains("iso639=\""+this.l1+"\"")){
					f1 = FilenameUtils.concat(tempfile.getParent(),tmp[0]+appXMLext);
					f2 = FilenameUtils.concat(tempfile.getParent(),tmp[1]+appXMLext);
				}else{
					f1 = FilenameUtils.concat(tempfile.getParent(),tmp[1]+appXMLext);
					f2 = FilenameUtils.concat(tempfile.getParent(),tmp[0]+appXMLext);
				}
			}
			text2 = FileUtils.readFileToString(new File(f2));
			if 	((!text1.contains(TagGenre3) & !text1.contains(TagGenre1+TagGenre2) & 
					!text1.contains(TagGenre1+"Other"+TagGenre2)) | 
				(!text2.contains(TagGenre3) & !text2.contains(TagGenre1+TagGenre2) &
						!text2.contains(TagGenre1+"Other"+TagGenre2))){
				logger.info(f1);
				logger.info(f2);
				logger.info("genre is already assigned to these files");
				text1 = ReadResources.extractTextfromXML_clean(f1,paragraph,ooi_text, false);
				StringTokenizer st1 = new StringTokenizer(text1);
				text2 = ReadResources.extractTextfromXML_clean(f2,paragraph,ooi_text, false);
				StringTokenizer st2 = new StringTokenizer(text2);
				
				doc_genre=extractfromXML(f1, page_genre);
				if (doc_genre.equals(extractfromXML(f2, page_genre))){
					logger.info("The common genre is : "+ doc_genre);
					for (int jj=0; jj<genres.length;jj++){
						if (doc_genre.equals(genres[jj])){
							tokens_in_genres1[jj]=tokens_in_genres1[jj]+st1.countTokens();
							files_in_genres1[jj]++;
							tokens_in_genres2[jj]=tokens_in_genres2[jj]+st2.countTokens();
							files_in_genres2[jj]++;
							break;
						}
					}
				}else{
					logger.error("different genres have been assinged to the docs of this pair");
					System.exit(0);
				}
				alr_genre_counter++;
				continue;
			}
			found1=genres_features.size()-1;
						
			text1 = ReadResources.extractTextfromXML_clean(f1,paragraph,ooi_text, false);
			StringTokenizer st1 = new StringTokenizer(text1);	
			doc_url1=extractfromXML(f1, page_url).toLowerCase();
			for (int jj=0; jj<genres.length;jj++){
				if (matches(doc_url1,features[jj],separator)){
					found1=jj;
					break;
				}
			}
			doc_title1 = extractfromXML(f1, page_title).toLowerCase();
			if (found1==genres_features.size()-1){	
				for (int jj=0; jj<genres.length;jj++){
					if (matches(doc_title1,features[jj],separator)){
						found1=jj;
						break;
					}
				}
			}
			
			found2=genres_features.size()-1; //doc_genre2="Other"; 
			
			text2 = ReadResources.extractTextfromXML_clean(f2,paragraph,ooi_text, false);
			StringTokenizer st2 = new StringTokenizer(text2);	
			doc_url2=extractfromXML(f2, page_url).toLowerCase();
			for (int jj=0; jj<genres.length;jj++){
				if (matches(doc_url2,features[jj],separator)){
					found2=jj;
					break;
				}
			}
			doc_title2 = extractfromXML(f2, page_title).toLowerCase();
			if (found2==genres_features.size()-1){
				for (int jj=0; jj<genres.length;jj++){
					if (matches(doc_title2,features[jj],separator)){
						found2=jj;
						break;
					}
				}
			}
			
			if (found1>found2)
				found = found2;
			else
				found = found1;
							
			doc_genre = genres[found];
			
			tokens_in_genres1[found]=tokens_in_genres1[found]+st1.countTokens();
			files_in_genres1[found]++;
			tokens_in_genres2[found]=tokens_in_genres2[found]+st2.countTokens();
			files_in_genres2[found]++;
						
			logger.info(f1);
			logger.info(doc_url1);
			logger.info(doc_title1);
			logger.info(doc_genre);
						
			logger.info(f2);
			logger.info(doc_url2);
			logger.info(doc_title2);
			logger.info(doc_genre);
			if (doc_genre.equals("Other"))
				System.out.println("EP");
			
			if (doc_genre.contains("Commercial"))
				System.out.println("EP");
			
			if (doc_genre.contains("Discussion"))
				System.out.println("EP");
			
			/*tmp1 = ReadResources.readFileAsString(f1);
			tmp1=tmp1.replace(TagGenre3, TagGenre1+ doc_genre+TagGenre2);
			tmp1=tmp1.replace(TagGenre1+TagGenre2, TagGenre1+ doc_genre+TagGenre2);
			ReadResources.writetextfile(f1,tmp1);
			
			tmp1 = ReadResources.readFileAsString(f2);
			tmp1=tmp1.replace(TagGenre3, TagGenre1+ doc_genre+TagGenre2);
			tmp1=tmp1.replace(TagGenre1+TagGenre2, TagGenre1+ doc_genre+TagGenre2);
			ReadResources.writetextfile(f2,tmp1);*/
			
			System.out.println();
			System.out.println(ii);
		}
		for (int jj=0; jj<genres.length;jj++){
			logger.info("LANG:\t"+this.l1+"\t"+genres[jj] + ":\tfiles\t"+Integer.toString(files_in_genres1[jj])
					+"\twith\t"+ Integer.toString(tokens_in_genres1[jj]) + "\ttokens.");
			logger.info("LANG:\t"+this.l2+"\t"+genres[jj] + ":\tfiles\t"+Integer.toString(files_in_genres2[jj])
					+"\twith\t"+ Integer.toString(tokens_in_genres2[jj]) + "\ttokens.");
		}
		logger.info("files with already assigned genre is: "+ alr_genre_counter);
	}

	
	private void run0()  throws IOException {
		String tmp1 = FileUtils.readFileToString(new File(this.keywords));
		String[] genres = tmp1.split("\n");;
		for (int ii=0;ii<genres.length;ii++){
			String[] temp= genres[ii].split("\t");
			genres[ii]=temp[0]; 	
		}

		FilenameFilter filter = new FilenameFilter() {			
			public boolean accept(File arg0, String arg1) {
				return (arg1.substring(arg1.length()-(ProcFileExt.length()+1)).equals("."+ProcFileExt));
			}
		};
		File[] files=this.directory.listFiles(filter);
		String text;
		int[] tokens_in_genres = new int[genres.length];
		int[] files_in_genres = new int[genres.length];
		for (int ii=0;ii<files.length;ii++){
			if (files[ii].getName().contains(underscore))
				continue;
			tmp1 = FileUtils.readFileToString(files[ii]);
			text = ReadResources.extractTextfromXML_clean
					(files[ii].getAbsolutePath(),paragraph,ooi_text, false);
			StringTokenizer st = new StringTokenizer(text);
			for (int jj=0;jj<genres.length;jj++){
				if (tmp1.contains("<genre>"+genres[jj]+"</genre>")){
					files_in_genres[jj]++;
					tokens_in_genres[jj]=tokens_in_genres[jj]+st.countTokens();
					break;
				}
			}
		}
		for (int jj=0; jj<genres.length;jj++){
			logger.info("\t"+genres[jj] + ":\tfiles\t"+Integer.toString(files_in_genres[jj])
					+"\twith\t"+ Integer.toString(tokens_in_genres[jj]) + "\ttokens.");
		}
	}

	private void run2() throws IOException {
		String tmp1 = FileUtils.readFileToString(new File(this.keywords));
		String[] genres_features = tmp1.split("\n");
		String[] genres = new String[genres_features.length];
		String[] features = new String[genres_features.length];
		for (int ii=0;ii<genres_features.length;ii++){
			String[] temp= genres_features[ii].split("\t");
			genres[ii]=temp[0]; 	
			if (temp.length>1)
				features[ii]=temp[1].toLowerCase();
			else
				features[ii]=" ";
		}

		FilenameFilter filter = new FilenameFilter() {			
			public boolean accept(File arg0, String arg1) {
				return (arg1.substring(arg1.length()-(ProcFileExt.length()+1)).equals("."+ProcFileExt));
			}
		};
		File[] files=this.directory.listFiles(filter);
		String doc_url, doc_title, doc_genre, text;
		int[] tokens_in_genres = new int[genres_features.length];
		int[] files_in_genres = new int[genres_features.length];
		int found;
		for (int ii=0;ii<files.length;ii++){
			if (files[ii].getName().contains(underscore))
				continue;
			
			doc_genre="Other";
			found=genres_features.length-1;
			
			text = ReadResources.extractTextfromXML_clean
					(files[ii].getAbsolutePath(),paragraph,ooi_text, false);
			StringTokenizer st = new StringTokenizer(text);	
			
			doc_url=extractfromXML(files[ii].getAbsolutePath(), page_url).toLowerCase();
			for (int jj=0; jj<genres.length;jj++){
				if (matches(doc_url,features[jj],separator)){
					doc_genre=genres[jj];
					found=jj;
					break;
				}
			}
			
			doc_title = extractfromXML(files[ii].getAbsolutePath(), page_title).toLowerCase();
			if (doc_genre.equals("Other")){
				//doc_title = extractfromXML(files[ii].getAbsolutePath(), page_title).toLowerCase();
				for (int jj=0; jj<genres.length;jj++){
					if (matches(doc_title,features[jj],separator)){
						doc_genre=genres[jj];
						found=jj;
						break;
					}
				}
			}
			tokens_in_genres[found]=tokens_in_genres[found]+st.countTokens();
			files_in_genres[found]++;
			tmp1 = FileUtils.readFileToString(files[ii]);
			String tmp2=tmp1.replace(TagGenre3, TagGenre1+ doc_genre+TagGenre2);
			tmp2=tmp2.replace(TagGenre1+TagGenre2, TagGenre1+ doc_genre+TagGenre2);
			WriteResources.writetextfile(files[ii].getAbsolutePath(),tmp2);
			//if (doc_genre.equals("Other"))
			//		System.out.println(doc_url);
					
			logger.info(files[ii].getName()+"\t"+doc_url+"\t"+doc_title+"\t"+doc_genre);
			//tmp1 = ReadResources.readFileAsString(files[ii].getAbsolutePath()+appHTMLext);
			//tmp1 = ReadResources.readFileAsString(files[ii].getAbsolutePath());
			//String tmp2=tmp1.replace("<distributor>ILSP project</distributor>", "<distributor>QTLP</distributor>");
			//String tmp3=tmp2.replace(">project_website<", ">http://www.qt21.eu/launchpad/<");
			//String tmp4=tmp1.replace("        <availability>Under review</availability>\r\n", "");
			//if (!tmp4.equals(tmp1))
			//	ReadResources.writetextfile(files[ii].getAbsolutePath(),tmp4);
			//System.out.println(ii);
		}
		for (int jj=0; jj<genres.length;jj++){
			logger.info("\t"+genres[jj] + ":\tfiles\t"+Integer.toString(files_in_genres[jj])
					+"\twith\t"+ Integer.toString(tokens_in_genres[jj]) + "\ttokens.");
		}
	}


	private void run1() {
		
		FilenameFilter filter = new FilenameFilter() {			
			public boolean accept(File arg0, String arg1) {
				return (arg1.substring(arg1.length()-(ProcFileExt.length()+1)).equals("."+ProcFileExt));
			}
		};
		File[] filesinXML=this.directory.listFiles(filter);
		String text="", test_space_separated="";
		File inFile; //, outFile
		String tempstr;
		for (int ii=0;ii<filesinXML.length;ii++){
			if (filesinXML[ii].getAbsolutePath().endsWith(ProcFileExt)){
				text = ReadResources.extractTextfromXML_clean
						(this.getDirectory()+fs+filesinXML[ii].getName(),
						paragraph, ooi_text, true);
				
				//test_space_separated = separateText(text);

				//String langIdentified = ReadResources.extractLangfromXML(filesinXML[ii].getAbsolutePath(), "language", "iso639");
				String langIdentified = ReadResources.extractAttrfromXML(filesinXML[ii].getAbsolutePath(), "language", "iso639", true, false);
				ArrayList<String> stems =new ArrayList<String>();
				try {
					stems = TopicTools.getStems(text, langIdentified);
				}catch (IOException e) {
					logger.warn(e.getMessage());
				}
				//concatenate stems 
				tempstr="";
				for (String s:stems){
					tempstr+=" "+s;
				}
				HashMap<String, Token> tokens = new HashMap<String, Token>();
				StringBuffer curToken = new StringBuffer();
				int maxFreq = 0;
				for (int i = 0; i < tempstr.length(); i++) {
					char c = text.charAt(i);
					if (Character.isLetterOrDigit(c)) {
						curToken.append(Character.toLowerCase(c));
					} else {
						if (curToken.length() > 0) {
							//if (curToken.length() > MIN_TOKEN_LEN) {
								// add it
								String s = curToken.toString();
								Token tok = tokens.get(s);
								if (tok == null) {
									tok = new Token(0, s);
									tokens.put(s, tok);
								}
								tok.cnt++;
								if (tok.cnt > maxFreq) maxFreq = tok.cnt;
							//}
							curToken.setLength(0);
						}
					}
				}
				
				inFile=new File(this.getDirectory()+fs+filesinXML[ii].getName()+TempFileExt);
				writeTextFile(inFile.getAbsolutePath(),test_space_separated);
				//FIXME
				//outFile = new File(inFile.getAbsolutePath()+NgramFileExt);
				//calc_ngrams(inFile,this.max_nGramOrder,outFile);
				//inFile.delete();
				//FIXMEcalcNGramWeight(outFile);
			}
		}
	}


	/*private void calc_ngrams(File inFile,int nGramOrder, File outFile) {

		Process p = XXX.exec(ngramExec + " -text "
				+ inFile.getAbsolutePath() + " -order " + nGramOrder
				+ " -write" + nGramOrder + space_char + outFile.getAbsolutePath());
		int exitStatus = p.waitFor();
	}
	 */


	private static boolean matches(String text, String list,
			String separator) {
		String[] temp=list.split(separator);
		for (int ii=0;ii<temp.length;ii++){
			if (text.contains(temp[ii].trim())){
				logger.info("found term:  "+temp[ii]);
				return true;
			}
		}
		return false;
	}


	static void writeTextFile(String filename,String text) {
		Writer out;
		try {
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename),"UTF-8"));
			out.write(text.trim());
			out.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			logger.error("Error in writing the output text file. The encoding is not supported.");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			logger.error("Error in writing the output text file. The file does not exist.");
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Error in writing the output text file.");
		}
	}

	public static String extractTextfromXML(String inputString) {
		String result="";
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
			Document doc = db.parse(inputString);
			doc.getDocumentElement().normalize();
			NodeList nodeLstP = doc.getElementsByTagName(TagName);
			for(int s=0; s<nodeLstP.getLength() ; s++){
				Element NameElement = (Element)nodeLstP.item(s);
				//if (!NameElement.hasAttribute("crawlinfo")){
				result+=NameElement.getTextContent();
				result+="\n";
				//}
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


	public static String extractfromXML(String inputString, String xmltag) {
		String result="";
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
			Document doc = db.parse(inputString);
			doc.getDocumentElement().normalize();
			NodeList nodeLstP = doc.getElementsByTagName(xmltag);
			for(int s=0; s<nodeLstP.getLength() ; s++){
				Element NameElement = (Element)nodeLstP.item(s);
				if (!NameElement.hasAttributes()){
					result+=NameElement.getTextContent();
					break;
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




	/**
	 * @return the directory
	 */
	public File getDirectory() {
		return directory;
	}


	/**
	 * @param directory the directory to set
	 */
	public void setDirectory(File directory) {
		this.directory = directory;
	}

	/**
	 * @return the keywrodsfile
	 */
	public String getKeywords() {
		return keywords;
	}


	/**
	 * @param keywords the keywordsfile to set
	 */
	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}



	/**
	 * @return the NgramOrders
	 */
	public int getNgramOrders() {
		return max_nGramOrder;
	}


	/**
	 * @param directory the directory to set
	 */
	public void setgetNgramOrders(int ngramOrder) {
		this.max_nGramOrder = ngramOrder;
	}

	/**
	 * @param method the directory to get
	 */
	private int getMethod() {
		return type;
	}
		
	/**
	 * @param method the directory to set
	 */
	private void setMethod(int m) {
		this.type = m;
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
	
	private void setLang1(String l1) {
		this.l1 = l1;
	}
	private String getLang1() {
		return l1;
	}
	
	private void setLang2(String l2) {
		this.l2 = l2;
	}
	private String getLang2() {
		return l2;
	}
	
	private void setPairlist(String pairlist) {
		this.pairlist = pairlist;
	}
	private String getPairlist() {
		return pairlist;
	}
}
