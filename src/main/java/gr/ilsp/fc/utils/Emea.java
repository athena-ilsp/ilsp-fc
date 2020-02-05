package gr.ilsp.fc.utils;

import gr.ilsp.fc.extractors.MSO2text;
import gr.ilsp.fc.extractors.Pdf2text;
import gr.ilsp.nlp.commons.Constants;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;


public class Emea {
	private static final Logger LOGGER = Logger.getLogger(Emea.class);
	//private static final String header = "en\tbg\tcs\tda\tde\tel\tet\tes\tfi\tfr\tga\thr\thu\tit\tis\tlt\tlv\tmt\tnl\tno\tpl\tpt\tro\tsk\tsl\tsv";
	private static final String header = "bg\tcs\tda\tde\tel\tes\tet\tfi\tfr\tga\thr\thu\tit\tis\tlt\tlv\tmt\tnl\tno\tpl\tpt\tro\tsk\tsl\tsv";
	private static final String text_tag = "<text>";
	private static final String text_tag_en = "</text>";
	private static final String PDF_EXT = ".pdf";
	private static final String DOC_EXT = ".doc";
	private static final String DOCX_EXT = ".docx";
	private static final String TXT_EXT = ".txt";
	private static final String CONTENT = "content";
	
	public static void main(String[] args) throws IOException {

	
		System.out.println("---------------------PARSE SITEMAP--------------------------------");
		System.out.println("1st argument could be : \"parsesitemap\" for parsing the sitemap xml files ");
		System.out.println("2nd argument is the fullpath of the directory containing the sitemap xml files");
		System.out.println("returns *_seeds, a text file (.sh) including the urls of the sitemap files");

		//WGET -W 1 seeds, to download the webpages

		System.out.println("---------------------PARSE PAGES--------------------------------");
		System.out.println("1st argument could be : \"parsepages\" for parsing downloaded webpages, to find translation links");
		System.out.println("2nd argument is the fullpath of the directory containing the webpages");
		System.out.println("returns *_pages, a text file (.sh) including the links to files that are available in many languages");

		System.out.println("---------------------TEXT EXTRACTION--------------------------------");
		System.out.println("1st argument could be : \"textextract\" for text extraction from the downloaded files (pdf, doc, docx");
		System.out.println("2nd argument is the fullpath of the directory containing the files");
		System.out.println("returns *_text, a direcroty containign the text files");

		System.out.println("---------------------PAIR LIST--------------------------------");
		System.out.println("1st argument could be : \"pairlist\" for text extraction from the downloaded files (pdf, doc, docx");
		System.out.println("2nd argument is the fullpath of the directory containing the files");
		System.out.println("returns *_text, a direcroty containign the text files");
		String mode = args[0];
		File f1, f2=null;

		if (mode.equals("parsesitemap")){
			f1 = new File(args[1]);
			f2 = parseSiteMap(f1);
		}
		if (mode.equals("parsepages")){
			f1 = new File(args[1]);
			f2 = parsePages(f1);
		}

		//WGET -W 1 pages, to download the pdf/doc/docx files

		//TEXT EXTRACTION
		if (mode.equals("textextract")){
			f1 = new File(args[1]);
			f2 = textextract(f1);
		}
		//GENERATE LIST OF DOCUMENT PAIRS PER EN_X LANGUAGE PAIR
		if (mode.equals("pairlist")){
			f1 = new File(args[1]);
			f2 = pairlist(f1);
		}

		System.exit(0);



		System.out.println("---------------------FILT--------------------------------");
		System.out.println("1st argument could be : \"filt\" for filtering out pairs with score lower than a specific threshold");
		System.out.println("2nd argument is a specific threshold, it is recommended to be higher than 0.9");
		System.out.println("3rd argument is is the fullpath of infile");
		System.out.println("returns *.filt");

		System.out.println("---------------------ADD--------------------------------");
		System.out.println("1st argument could be : \"add\" for adding column with lang");
		System.out.println("2nd argument is the fullpath of infile (downloaded from paracrawl)");
		System.out.println("3rd argument is the language to be added");
		System.out.println("returns *.lang");

		System.out.println("---------------------NORM--------------------------------");
		System.out.println("1st argument could be : \"norm\" for normalizing the EN part");
		System.out.println("2nd argument is is the fullpath of infile");
		System.out.println("returns *.norm");

		System.out.println("---------------------KEEPMULTI--------------------------------");
		System.out.println("1st argument could be : \"keepmulti\" for keeping only multilingual pairs");
		System.out.println("2nd argument is the fullpath of infile, (file after CAT all datasets and SORT");
		System.out.println("returns *.multi");

		System.out.println("---------------------PROCESSMULTI--------------------------------");
		System.out.println("1st argument could be : \"processmulti\" for generating the sparse table");
		System.out.println("2nd argument is the fullpath of infile");
		System.out.println("returns *.idmulti");

		System.out.println("---------------------RESULTMULTI--------------------------------");
		System.out.println("1st argument could be : \"resultmulti\" for multilingual pairs in specific languages");
		System.out.println("2nd argument is the fullpath of infile");
		System.out.println("3rd argument could be : targeted languages separated by ;");
		System.out.println("returns *.(targeted languages)");


		if (mode.equals("filt")){
			f1 = new File(args[1]);
			double thr = Double.parseDouble(args[2]); 
			f2 = filt(f1, thr);
		}
		if (mode.equals("add")){
			f1 = new File(args[1]);
			String lang = (args[2]); 
			f2 = addlang(f1, lang);
		}
		//Step 3. NORMALIZE MERGED FILE
		if (mode.equals("norm")){
			f1 = new File(args[1]);
			f2 = normalizeContent(f1);
		}

		if (mode.equals("keepmulti")){
			f1 = new File(args[1]);
			//String lang = args[2];
			//f2 = addlang(f1, lang);
			f2 = keepMultilinguality(f1);
		}

		if (mode.equals("processmulti")){
			f1 = new File(args[1]);
			//String lang = args[2];
			//f2 = addlang(f1, lang);
			f2 = processMultilinguality(f1);
		}

		if (mode.equals("resultmulti")){
			f1 = new File(args[1]);
			String[] langs = args[2].split(Constants.SEMICOLON);
			f2 = resultMultilinguality(f1, langs);
		}
		LOGGER.info("RESULT IN "+ f2.getAbsolutePath());
	}


	private static File pairlist(File f1) throws IOException {
		String[] langs = header.split("\t");
		File res = new File(f1.getAbsolutePath()+"_pairs");
		List<File> files =  Arrays.asList(f1.listFiles());
		List<String> names =  new ArrayList<String>();
		List<String> ennames =  new ArrayList<String>();
		Set<String> checklist = new HashSet<String>();
		String name ="", temp="";
		int ind=0, ind1=0;
		for (File file:files){
			name = file.getName();
			ind = name.lastIndexOf("_");
			temp = name.substring(ind);
			if (temp.contains("en"))
				ennames.add(file.getName());
			names.add(name);
		}
		
		names = FileUtils.readLines(new File("C:/Users/vpapa/ELRC/emea/pages_urls"), Constants.UTF8);
		ennames = new ArrayList<String>();
		for (String name1:names){
			ind = name1.lastIndexOf("_");
			temp = name1.substring(ind);
			if (temp.contains("en"))
				ennames.add(name1);
		}
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		for (String lang:langs)
			map.put("en-"+lang, new ArrayList<String>());
		
		String temp1 = "", temp2="", temp3="", temp11;
		boolean found = false;
		int counter =0;
		for (String enname:ennames){
			counter=0;
			for (String lang:langs){
				found = false;
				ind = enname.lastIndexOf("_");
				temp = enname.substring(0, ind);
				temp1 = enname.substring(ind+1); 
				temp3 = temp1.replaceAll("en", lang);
				temp2 = temp+"_"+temp3;
				if (names.contains(temp2)){
					List<String> templist1 = map.get("en-"+lang);
					templist1.add(enname+"\t"+temp2);
					map.put("en-"+lang, templist1);
					checklist.add(temp2);
					checklist.add(enname);
					found = true;
					counter++;
				}
				if (!found){
					ind = enname.lastIndexOf("_");
					temp = enname.substring(0, ind);
					temp1 = enname.substring(ind+1);
					ind1 = temp1.indexOf(".");
					temp11 = "-0"+temp1.substring(ind1);
					temp1 = temp1.substring(0, 2)+temp11;
					temp3 = temp1.replaceAll("en", lang);
					temp2 = temp+"_"+temp3;
					if (names.contains(temp2)){
						List<String> templist1 = map.get("en-"+lang);
						templist1.add(enname+"\t"+temp2);
						map.put("en-"+lang, templist1);
						checklist.add(temp2);
						checklist.add(enname);
						found = true;
						counter++;
					}
				}
				if (!found){
					ind = enname.lastIndexOf("_");
					temp = enname.substring(0, ind);
					temp1 = enname.substring(ind+1);
					ind1 = temp1.indexOf(".");
					temp11 = "-1"+temp1.substring(ind1);
					temp1 = temp1.substring(0, 2)+temp11;
					temp3 = temp1.replaceAll("en", lang);
					temp2 = temp+"_"+temp3;
					if (names.contains(temp2)){
						List<String> templist1 = map.get("en-"+lang);
						templist1.add(enname+"\t"+temp2);
						map.put("en-"+lang, templist1);
						checklist.add(temp2);
						checklist.add(enname);
						counter++;
					}
				}
			}
			if (!checklist.contains(enname))
				System.out.println("OOOOOOOOOPPPPP\t" +enname);
			if (counter==0)
				System.out.println("EEEEEEEEEPPPPP\t" +enname);
		}
		for (String name1:names){
			if (!checklist.contains(name1)){
				ind = name1.lastIndexOf("_");
				temp = name1.substring(0, ind);
				temp1 = name1.substring(ind+1);
				temp1 = temp1.replaceAll("-0", "");
				temp1 = temp1.replaceAll("-1", "");
				temp = temp+"_"+temp1;
				if (!checklist.contains(temp)){
					System.out.println(name1);
				}
			}
		}
		
		Set<String> keys = map.keySet();
		Iterator<String> keys_it = keys.iterator();
		String key="";
		counter = ennames.size();
		while (keys_it.hasNext()){									
			key = keys_it.next();
			List<String> templist1 = map.get(key);
			counter = counter + templist1.size();
			try {
				FileUtils.writeLines(new File(FilenameUtils.concat(res.getAbsolutePath(), key)), Constants.UTF8, templist1, "\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println(counter);
		return res;
	}


	private static File textextract(File f1) {
		File res = new File(f1.getAbsolutePath()+"_text");
		List<File> files =  Arrays.asList(f1.listFiles());
		String name ="", text="", cleanText="", temp="";
		Map<String, String> data = new HashMap<String, String>();
		int ind = 0;
		for (File file:files){
			name = file.getName();
			if (name.toLowerCase().endsWith(PDF_EXT))	
				data = Pdf2text.run1(file, false);
			if (name.toLowerCase().endsWith(DOC_EXT) || name.toLowerCase().endsWith(DOCX_EXT))	
				data = MSO2text.run1(file);
			text = data.get(CONTENT);
			cleanText = ContentNormalizer.normalizeText(text);
			cleanText = cleanText.replaceAll(text_tag, "");
			cleanText = cleanText.replaceAll(text_tag_en, "");
			text = "";
			String[] lines = cleanText.split("\n");
			for (String line:lines){
				line=line.trim();
				if (Character.isLowerCase(line.charAt(0)) && Character.isLowerCase(text.charAt(text.length()-1)))
					text = text+" "+line;
				else
					text = text+"\n"+line;
			}
			text = text.substring(1);
			ind = name.lastIndexOf(Constants.UNDERSCORE);
			temp = name.substring(ind);
			if (temp.contains("_cz"))
				temp = temp.replaceAll("_cz", "_cs");
			if (temp.contains("_se"))
				temp = temp.replaceAll("_se", "_sv");
			if (temp.contains("_dk"))
				temp = temp.replaceAll("_dh", "_da");
			if (temp.contains("_si"))
				temp = temp.replaceAll("_si", "_sl");
			if (temp.contains("_ee"))
				temp = temp.replaceAll("_ee", "_et");
			name  = name.substring(0, ind)+temp+ TXT_EXT;
			file = new File(FilenameUtils.concat(res.getAbsolutePath(), name));
			try {
				FileUtils.write(file, text, Constants.UTF8);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return res;
	}


	private static File parsePages(File f1) {
		File res = new File(f1.getAbsolutePath()+"_urls");
		File[] maps = f1.listFiles();
		List<String> urls = new ArrayList<String>();
		int ind = 0;
		List<String> lines = new ArrayList<String>();
		String line="", url="";
		for (File map:maps){
			try {
				lines = FileUtils.readLines(map, Constants.UTF8);
				for (int ii=0;ii<lines.size();ii++){
					line = lines.get(ii);
					if (line.contains("ecl-file__translations-metadata")){
						ii++;
						line = lines.get(ii).trim();
						if (line.startsWith("<a href=")){
							line = line.substring(9);
							ind = line.indexOf(" ");
							url = line.substring(0, ind-1);
							if (!url.endsWith("pdf") && !url.endsWith("doc") && !url.endsWith("docx") ){
								System.out.println(line);
								continue;
							}
							url = "wget -w 1 " +url;
							if (urls.contains(url))
								continue;
							urls.add(url);
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println(urls.size());
		List<String> lines1 = new ArrayList<String>();
		String t1 = "";
		for (String t:urls){
			ind = t.lastIndexOf("_");
			t1 = t.substring(0, ind);
			line = t.substring(t.lastIndexOf("."));
			t = t1+"_en" + line; //en.pdf";
			if (lines1.contains(t))
				continue;
			lines1.add(t);
		}
		urls.addAll(lines1);
		Collections.sort(urls);
		
		for (String u:urls){
			ind = u.lastIndexOf("_");
			url = u.substring(0, ind);
			t1 =u.substring(ind);
			if (t1.contains("-0")){
				t1 = t1.substring(2);
				url = url+"_"+t1;
			}
		}
		
		
		try {
			FileUtils.writeLines(res, Constants.UTF8, urls, "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}


	private static File parseSiteMap(File f1) {
		File res = new File(f1.getAbsolutePath()+"_seeds");
		File[] maps = f1.listFiles();
		List<String> urls = new ArrayList<String>();
		int ind = 0;
		List<String> lines = new ArrayList<String>();
		for (File map:maps){
			if (map.getName().contains("sitemap")){
				try {
					lines = FileUtils.readLines(map, Constants.UTF8);
					for (String line:lines){
						if (line.startsWith("<url><loc>")){
							line = line.replaceAll("<url><loc>", "");
							ind = line.indexOf("</loc>");
							urls.add("wget -w 1 " + line.substring(0, ind));
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		try {
			FileUtils.writeLines(res, Constants.UTF8, urls, "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}


	private static File keepMultilinguality(File f1)  throws IOException {
		File f2 = new File(f1.getAbsolutePath()+".multi");
		BufferedReader in = new BufferedReader(new FileReader(f1));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f2.getAbsolutePath()),Constants.UTF8));
		String inputLine, key="", previouskey="";
		List<String> list = new ArrayList<String>();
		while ((inputLine = in.readLine()) != null) {
			String[] parts = inputLine.split(Constants.TAB);
			if (parts.length!=3){
				LOGGER.warn("CUT\t" +inputLine);
				continue;
			}
			key = parts[0];
			if (!key.equals(previouskey)){
				if (list.size()>1){
					for (int ii=0;ii<list.size();ii++){
						out.write(list.get(ii)+"\n");
					}
				}
				list.clear();
				list.add(key+"\t"+parts[1]+"\t"+parts[2]);
				previouskey=key;
			}else{
				list.add(key+"\t"+parts[1]+"\t"+parts[2]);
			}
		}
		in.close();
		out.close();
		return f2;
	}


	private static File resultMultilinguality(File f1, String[] langs) throws IOException{
		Arrays.sort(langs);
		String foundpair = "";
		for (String lang:langs){
			foundpair = foundpair +lang+Constants.HYPHEN;
		}

		File f2 = new File(f1.getAbsolutePath()+"." + foundpair.substring(0, foundpair.length()-1));
		BufferedReader in = new BufferedReader(new FileReader(f1));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f2.getAbsolutePath()),Constants.UTF8));
		String inputLine;
		int counter = 0;
		List<String> headlangs = new ArrayList<String>();

		List<String> targetlangs = Arrays.asList(langs);
		while ((inputLine = in.readLine()) != null) {
			if (counter==0){ 
				headlangs = Arrays.asList(inputLine.split(Constants.TAB));
				break;
			}
		}
		in.close();
		List<Integer> cols = new ArrayList<Integer>();
		for (int ii=0;ii<targetlangs.size();ii++){
			for (int jj=0;jj<headlangs.size();jj++){
				if (targetlangs.get(ii).equals(headlangs.get(jj))){
					cols.add(jj);
					break;
				}
			}
		}
		in = new BufferedReader(new FileReader(f1));
		foundpair="";
		boolean found = true;
		int thr = cols.get(cols.size()-1)+1;
		counter = 0;
		while ((inputLine = in.readLine()) != null) {
			String[] parts = inputLine.split(Constants.TAB);
			if (parts.length<thr)
				continue;
			foundpair = "";
			found = true;
			for (int ii=0;ii<cols.size();ii++){
				if (parts[cols.get(ii)].isEmpty()){
					found = false;
					break;
				}else{
					foundpair = foundpair +parts[cols.get(ii)]+Constants.TAB;
				}
			}
			if (found){
				out.write(foundpair.trim()+"\n");
				counter++;
			}
		}
		in.close();
		out.close();
		LOGGER.info("# TUs found:\t"+ (counter-1));
		return f2;
	}

	private static File filt(File f1, double thr)  throws IOException {
		File f2 = new File(f1.getAbsolutePath()+"-"+thr+".filt");
		BufferedReader in = new BufferedReader(new FileReader(f1));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f2.getAbsolutePath()),Constants.UTF8));
		String inputLine;
		double score;
		while ((inputLine = in.readLine()) != null) {
			String[] parts = inputLine.split(Constants.TAB);
			if (parts.length!=5){
				LOGGER.warn("CUT\t" +inputLine);
				continue;
			}
			score = Double.parseDouble(parts[0]);
			if (score>=thr)
				out.write(inputLine+"\n");
			else
				LOGGER.warn("CUT\t" +inputLine);
		}
		in.close();
		out.close();
		return f2;
	}

	private static File processMultilinguality(File f1) throws IOException {
		File f2 = new File(f1.getAbsolutePath()+".idmulti");
		BufferedReader in = new BufferedReader(new FileReader(f1));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f2.getAbsolutePath()),Constants.UTF8));
		String inputLine, line="";
		LOGGER.info("reading file");
		Map<Integer,LangObject> obj= new HashMap<Integer,LangObject>();
		String key="", curlang="", previouskey="";
		LangObject templangobj = initLangObject();
		int idkey=0;
		int counter = 0,  thous = 1, level=100000, thr = level;
		while ((inputLine = in.readLine()) != null) {
			counter++;
			if (counter==thr){
				thous++;
				thr = level*thous;
				LOGGER.info(counter);
			}		
			String[] parts = inputLine.split(Constants.TAB);
			if (parts.length!=3){
				LOGGER.warn("CUT\t" +inputLine);
				continue;
			}
			key = parts[0];
			if (!key.equals(previouskey)){
				idkey++;
				previouskey=key;
			}
			if (obj.containsKey(idkey))
				templangobj = obj.get(idkey);
			else
				templangobj = initLangObject();
			curlang = parts[1];
			line = parts[2];
			Field languageField =  null;
			try {
				languageField = LangObject.class.getDeclaredField(curlang);
				//boolean accessible = languageField.isAccessible();
				//templangobj.el = parts[3];
				languageField.set(templangobj, line);
			} catch (NoSuchFieldException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//templangobj = update(templangobj, curlang, parts[3]);
			obj.put(idkey, templangobj);
		}
		in.close();
		Set<Integer> keys = obj.keySet();
		LOGGER.info("Number of multilingual TUs:\t"+keys.size());
		Iterator<Integer> keys_it = keys.iterator();
		thous=0;
		int key_temp;
		LOGGER.info("writing file");
		out.write(header+"\n");
		boolean multi=false;
		while (keys_it.hasNext()){									
			counter++;
			if (counter/100000>thous){
				thous++;
				LOGGER.info((thous*100000)+ " files have been examined");
			}
			key_temp = keys_it.next();
			templangobj = obj.get(key_temp);
			multi = checkmutli(templangobj);
			if (multi)
				writeline(out, key_temp, templangobj);
			//writeline(out, key_temp, templangobj);
		}
		out.close();
		return f2;
	}






	/*private static File denormalize(File f1, File f2, File f3) throws IOException {
		File f4 = new File(f1.getAbsolutePath()+"_1");
		List<String> comm = FileUtils.readLines(f1, Constants.UTF8);
		BufferedReader in = new BufferedReader(new FileReader(f2));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f2.getAbsolutePath()),Constants.UTF8));
		String inputLine, line="";
		int counter = 0;
		boolean found = false;
		while ((inputLine = in.readLine()) != null) {
			found = false;
			String[] parts = inputLine.split(Constants.TAB);
			String temp = ContentNormalizer.normtext(parts[0]);
			for (int jj=0;jj<comm.size();jj++){
				String[] t =comm.get(jj).split(Constants.TAB); 
				if (t[0].equals(temp)){
					line = parts[0] + Constants.TAB+t[1] + Constants.TAB+t[2];
					found = true;
					break;
				}	
			}
			if (found)
				out.write(line.trim()+"\n");

		}
		System.out.println(counter);
		in.close();
		out.close();

		return f4;
	}*/



	private static File addlang(File f1, String lang) throws IOException {
		File f2 = new File(f1.getAbsolutePath()+".lang");
		BufferedReader in = new BufferedReader(new FileReader(f1));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f2.getAbsolutePath()),Constants.UTF8));
		String inputLine, line;
		int counter = 0;
		while ((inputLine = in.readLine()) != null) {
			counter++;
			//LOGGER.info(inputLine);
			String[] parts = inputLine.split(Constants.TAB);

			line = parts[2] + Constants.TAB + lang + Constants.TAB + counter;
			out.write(line.trim()+"\n");
		}
		System.out.println(counter);
		in.close();
		out.close();
		return f2;
	}



	private static File normalizeContent(File f1) throws IOException {
		File f2 = new File(f1.getAbsolutePath()+".norm");
		BufferedReader in = new BufferedReader(new FileReader(f1));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f2.getAbsolutePath()),Constants.UTF8));
		String inputLine, line;
		while ((inputLine = in.readLine()) != null) {
			String[] parts = inputLine.split(Constants.TAB);
			parts[0] = ContentNormalizer.normtext(parts[0]);
			line="";
			for (int ii=0;ii<parts.length;ii++){
				line = line + Constants.TAB + parts[ii];
			}
			out.write(line.trim()+"\n");
		}
		in.close();
		out.close();
		return f2;
	}



	/*	private static File findCommInSorted(File f1, int range1) throws IOException {
		File f2 = new File(f1.getAbsolutePath()+".comm");
		BufferedReader in = new BufferedReader(new FileReader(f1));
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f2.getAbsolutePath()),Constants.UTF8));
		String inputLine;
		boolean found = false;
		String preen = "", prexx = "", prela="", text = "";
		int  counter = 0, tus = 0;
		String en, xx, la;
		while ((inputLine = in.readLine()) != null) {
			counter++;
			found = false;
			String[] parts = inputLine.split(Constants.TAB);
			en = parts[0];
			xx = parts[1];
			la = parts[2];
			if (!en.equals(preen)){
				preen = en;
				prexx = xx;
				prela = la;
			}else{
				if (prela.equals(la)){
					preen = en;
					prexx = xx;
					prela = la;
				}else{
					found = true;
				}
			}
			if (found){
				text = en+Constants.TAB;
				if (prela.compareTo(la) < 0)
					text = text + prexx +Constants.TAB+ xx;
				else
					text = text + xx +Constants.TAB+ prexx;
				tus++;
				out.write(text.trim()+"\n");
			}
		}
		LOGGER.info(tus + "common TUs");
		in.close();
		out.close();
		return f2;
	}*/


	public static class LangObject {
		public String bg, cs, da, de, el, et, es, fi, fr, ga;
		public String hr, hu, it, is, lt, lv, mt, nl, no, pl, pt;
		public String ro, sk, sl, sv;

		public LangObject(String bg, String cs, String da, String de, String el,
				String et, String es, String fi, String fr, String ga, String hr,
				String hu, String it, String is, String lt, String lv, String mt,
				String nl, String no, String pl, String pt, String ro, String sk,
				String sl, String sv) {
			super();
			this.bg = bg;
			this.cs = cs;
			this.da = da;
			this.de = de;
			this.el = el;
			this.et = et;
			this.es = es;
			this.fi = fi;
			this.fr = fr;
			this.ga = ga;
			this.hr = hr;
			this.hu = hu;
			this.it = it;
			this.is = is;
			this.lt = lt;
			this.lv = lv;
			this.mt = mt;
			this.nl = nl;
			this.no = no;
			this.pl = pl;
			this.pt = pt;
			this.ro = ro;
			this.sk = sk;
			this.sl = sl;
			this.sv = sv;
		}
	}
	public static LangObject initLangObject(){
		LangObject docdata = new LangObject("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "" );
		return docdata;
	}

	private static void writeline(BufferedWriter out, int key_temp, 	LangObject templangobj) throws IOException {

		out.write( templangobj.bg+"\t"+templangobj.cs+"\t"+templangobj.da+"\t"+templangobj.de+"\t"+templangobj.el+"\t"+templangobj.es+"\t"
				+templangobj.et+"\t"+templangobj.fi+"\t"+templangobj.fr+"\t"+templangobj.ga+"\t"+templangobj.hr+"\t"+templangobj.hu+"\t"+templangobj.is+"\t"
				+templangobj.it+"\t"+templangobj.lt+"\t"+templangobj.lv+"\t"+templangobj.mt+"\t"+templangobj.nl+"\t"+templangobj.no+"\t"+templangobj.pl+"\t"
				+templangobj.pt+"\t"+templangobj.ro+"\t"+templangobj.sk+"\t"+templangobj.sl+"\t"+templangobj.sv+"\n");

		//	out.write(key_temp+"\t"+ templangobj.bg+"\t"+templangobj.cs+"\t"+templangobj.da+"\t"+templangobj.de+"\t"+templangobj.el+"\t"+templangobj.es+"\t"
		//			+templangobj.et+"\t"+templangobj.fi+"\t"+templangobj.fr+"\t"+templangobj.ga+"\t"+templangobj.hr+"\t"+templangobj.hu+"\t"+templangobj.is+"\t"
		//			+templangobj.it+"\t"+templangobj.lt+"\t"+templangobj.lv+"\t"+templangobj.mt+"\t"+templangobj.nl+"\t"+templangobj.no+"\t"+templangobj.pl+"\t"
		//			+templangobj.pt+"\t"+templangobj.ro+"\t"+templangobj.sk+"\t"+templangobj.sl+"\t"+templangobj.sv+"\n");
	}

	private static boolean checkmutli(LangObject templangobj) {

		int counter = 0;
		if (!templangobj.bg.isEmpty())
			counter++;
		if (!templangobj.cs.isEmpty())
			counter++;
		if (!templangobj.da.isEmpty())
			counter++;
		if (!templangobj.de.isEmpty())
			counter++;
		if (!templangobj.el.isEmpty())
			counter++;
		if (!templangobj.es.isEmpty())
			counter++;
		if (!templangobj.et.isEmpty())
			counter++;
		if (!templangobj.fi.isEmpty())
			counter++;
		if (!templangobj.fr.isEmpty())
			counter++;
		if (!templangobj.ga.isEmpty())
			counter++;
		if (!templangobj.hr.isEmpty())
			counter++;
		if (!templangobj.hu.isEmpty())
			counter++;
		if (!templangobj.is.isEmpty())
			counter++;
		if (!templangobj.it.isEmpty())
			counter++;
		if (!templangobj.lt.isEmpty())
			counter++;
		if (!templangobj.lv.isEmpty())
			counter++;
		if (!templangobj.mt.isEmpty())
			counter++;
		if (!templangobj.nl.isEmpty())
			counter++;
		if (!templangobj.no.isEmpty())
			counter++;
		if (!templangobj.pl.isEmpty())
			counter++;
		if (!templangobj.pt.isEmpty())
			counter++;
		if (!templangobj.ro.isEmpty())
			counter++;
		if (!templangobj.sk.isEmpty())
			counter++;
		if (!templangobj.sl.isEmpty())
			counter++;
		if (!templangobj.sv.isEmpty())
			counter++;
		if (counter==0)
			System.out.println("problem");
		if (counter>1)
			return true;

		return false;
	}



}
