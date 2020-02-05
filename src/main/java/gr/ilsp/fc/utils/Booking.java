package gr.ilsp.fc.utils;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;


public class Booking {
	private static final Logger LOGGER = Logger.getLogger(Booking.class);
	//private static final String header = "en\tbg\tcs\tda\tde\tel\tet\tes\tfi\tfr\tga\thr\thu\tit\tis\tlt\tlv\tmt\tnl\tno\tpl\tpt\tro\tsk\tsl\tsv";
	private static final String header = "bg\tcs\tda\tde\tel\tes\tet\tfi\tfr\tga\thr\thu\tis\tit\tlt\tlv\tmt\tnl\tno\tpl\tpt\tro\tsk\tsl\tsv";



	public static void main(String[] args) throws IOException {

		System.out.println("---------------------MERGE--------------------------------");
		System.out.println("1st argument could be : \"merge\" for merging laser files of the same language pair");
		System.out.println("2nd argument is a directory containing the files ");
		System.out.println("returns a directory (*_merge) with a file per language pair");

		System.out.println("---------------------FILTER--------------------------------");
		System.out.println("1st argument could be : \"filt\" for filtering out pairs with score lower than a specific threshold");
		System.out.println("2nd argument is a specific threshold, it is recommended to be higher than 0.9");
		System.out.println("3rd argument is is the fullpath of infile");
		System.out.println("returns *.filt");

		System.out.println("---------------------ADD--------------------------------");
		System.out.println("1st argument could be : \"add\" for adding column with lang");
		System.out.println("2nd argument is the fullpath of infile (the result of merging or filtering)");
		System.out.println("3rd argument is the language to be added");
		System.out.println("returns *.lang , score and non-en part are removed. It containt the en part and the id of the line" );

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

		System.out.println("---------------------GETMULTI--------------------------------");
		System.out.println("1st argument could be : \"getmulti\" for generating multilingual corpus based on idmulti file");
		System.out.println("2nd argument is the fullpath of infile (the idmulti file)");
		System.out.println("returns *.corpus");


		String mode = args[0];
		File f1, f2=null;

		if (mode.equals("merge")){
			f1 = new File(args[1]);
			f2 = merge(f1);
		}

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

		if (mode.equals("getmulti")){
			f1 = new File(args[1]);
			f2 = getMultilinguality(f1);
		}

		LOGGER.info("RESULT IN "+ f2.getAbsolutePath());




	}


	private static File getMultilinguality(File f11) throws IOException {
		File f1 = f11.getAbsoluteFile();
		LOGGER.info(f1.getAbsolutePath());
		String basePath = f1.getParent();
		LOGGER.info(basePath);

		List<String> ids = FileUtils.readLines(f1, Constants.UTF8);
		LOGGER.info(ids.get(0));
		String[] heads = ids.get(0).split(Constants.TAB);
		for (int ii=0;ii<heads.length;ii++)
			LOGGER.info(heads[ii]);
		String temp = "";

		Map<String, List<String>> tuples = new HashMap<String, List<String>>();
		for (int ii=0;ii<heads.length;ii++){
			LOGGER.info("Getting en-" +heads[ii] + " ...");
			temp = FilenameUtils.concat(basePath, "en"+Constants.HYPHEN+heads[ii]);

			LOGGER.info("Reading "+ temp);
			List<String> templist = FileUtils.readLines(new File(temp));
			List<String> newtemplist = new ArrayList<String>();
			for (int jj=1;jj<ids.size();jj++){
				int id = Integer.parseInt(ids.get(jj).split(Constants.TAB)[ii]);
				//System.out.println(id +"\t:\t"+templist.get(id-1));
				newtemplist.add(templist.get(id-1));
			}
			templist = null;
			List<String> line= new ArrayList<String>();
			List<String> en= new ArrayList<String>();
			List<String> cur= new ArrayList<String>();
			for (int jj=0;jj<newtemplist.size();jj++){
				line = Arrays.asList(newtemplist.get(jj).split(Constants.TAB));
				en.add(line.get(1));
				cur.add(line.get(2));
			}
			tuples.put("en-"+heads[ii], en);
			LOGGER.info("en-"+heads[ii]+":\t"+en.size());
			tuples.put(heads[ii], cur);
			LOGGER.info(heads[ii]+":\t"+cur.size());
			newtemplist = null;
		}
		File f2 = new File(f1.getAbsolutePath()+".corpus");
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f2.getAbsolutePath()),Constants.UTF8));
		String headline = "";
		for (String head:heads)
			headline = headline+Constants.TAB+head;
		out.write("en"+ Constants.TAB+headline.trim()+"\n");

		//Set<String> keys = tuples.keySet();
		int tuplen = tuples.get(heads[0]).size();
		for (int ii=0;ii<heads.length;ii++){
			if (tuples.get(heads[ii]).size()!=tuplen)
				LOGGER.error("Some sentences missing");
			if (tuples.get("en-"+heads[ii]).size()!=tuplen)
				LOGGER.error("Some sentences missing");
		}
		String currentEN="", currentline="";
		int counter=0;
		for (int jj=0;jj<tuplen;jj++){
			for (int ii=0;ii<heads.length;ii++){
				temp = tuples.get("en-"+heads[ii]).get(jj);
				if (currentEN.isEmpty())
					currentEN=temp;
				else{
					if (!currentEN.equals(temp)){
						LOGGER.info("en-"+heads[0]+":\t"+currentEN);
						LOGGER.info("en-"+heads[ii]+":\t"+temp);
					}
				}
				currentline =  currentline+Constants.TAB + tuples.get(heads[ii]).get(jj);
			}
			counter++;
			currentline = currentEN+currentline;
			out.write(currentline.trim()+"\n");
			currentline="";
			currentEN="";
		}
		out.close();
		LOGGER.info("Total multiples IDs = "+ (ids.size()-1));
		LOGGER.info("Total multiples segments = "+ counter);
		return f2;
	}


	private static File merge(File f1) {
		String temp = f1.getAbsolutePath();
		File f2 = new File(temp+"_merge");
		List<File> files = Arrays.asList(f1.listFiles());
		List<String> pairs = new ArrayList<String>();
		for (File file:files){
			temp = file.getName();
			if (!temp.endsWith(".laser")){
				System.out.println("Skip file "+ temp);
				continue;
			}
			temp = temp.substring(0, temp.lastIndexOf("."));
			temp = temp.substring(temp.lastIndexOf(".")+1);
			if (pairs.contains(temp))
				continue;
			pairs.add(temp);
		}
		if (!pairs.isEmpty())
			f2.mkdirs();
		int counter = 0, counter1=0;
		for (String pair:pairs){
			File pairfile = new File(FilenameUtils.concat(f2.getAbsolutePath(), pair));
			List<String> sents = new ArrayList<String>();
			List<String> tempsents = new ArrayList<String>();
			counter = 0;
			counter1=0;
			for (File file:files){
				temp = file.getName();
				if (!temp.endsWith(pair+".laser")){
					counter1++;
					continue;
				}try {
					tempsents = FileUtils.readLines(file, Constants.UTF8);
					sents.addAll(tempsents);
					counter++;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			System.out.println(pair);
			System.out.println(counter);
			System.out.println(counter1);
			System.out.println(sents.size());

			try {
				FileUtils.writeLines(pairfile, Constants.UTF8, sents, "\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("-----------");
		}

		return f2;
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
			if (parts.length!=3){
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

			line = parts[1] + Constants.TAB + lang + Constants.TAB + counter;
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
				String es, String et, String fi, String fr, String ga, String hr,
				String hu, String is, String it, String lt, String lv, String mt,
				String nl, String no, String pl, String pt, String ro, String sk,
				String sl, String sv) {
			super();
			this.bg = bg;
			this.cs = cs;
			this.da = da;
			this.de = de;
			this.el = el;
			this.es = es;
			this.et = et;
			this.fi = fi;
			this.fr = fr;
			this.ga = ga;
			this.hr = hr;
			this.hu = hu;
			this.is = is;
			this.it = it;
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
