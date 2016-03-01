package gr.ilsp.fc.utils;

import gr.ilsp.fc.main.ReadResources;
import gr.ilsp.fc.main.WriteResources;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

public class TempUtils {
	private static final Logger LOGGER = Logger.getLogger(TempUtils.class);
	private static final String appXMLext = ".xml";
	private static final String appHTMLext = ".html";
	private static final String appXMLHTMLext = ".xml.html";
	private static final String type_p ="p";
	private static final String HTTRACK1 = "<!-- Mirrored from"; 
	private static final String HTTRACK2 = "by HTTrack Website Copier";
	
	public static void main(String[] args) {
		FilenameFilter filter = new FilenameFilter() {			
			public boolean accept(File arg0, String arg1) {
				return (arg1.endsWith("_u.xml") );
			}
		};	
		
	File t = new File("C:\\Users\\vpapa\\ELRC");
	
	List<File> files=FcFileUtils.listFiles(t, filter, true);
	System.out.println(files.size());
	for (File file:files){
		file.delete();
	}
	System.exit(0);

		List<String> res=new ArrayList<String>();	
	int group=4;
	try {
		List<String> tusinfo = FileUtils.readLines(new File("C:/Users/vpapa/Dropbox/ilsp-fc/201602_culture_eng_fra_eng_spa_datasets/eng-fra_culture_aupdih.csv"));
		String[][] tus=new String[tusinfo.size()][2];
		//String[] itus=new String[tusinfo.size()];
		int counter=0;
		for (String tu:tusinfo){
			if (counter==0){
				counter++;
				continue;
			}
			String[] temp = tu.split("\t");
			tus[counter][0] = temp[3];
			tus[counter][1] = temp[4];
			//itus[counter] = temp[6];
			counter++;
		}
		//FileUtils.writeLines(new File("C:/Users/vpapa/Dropbox/ilsp-fc/201602_culture_eng_fra_eng_spa_datasets/sampling_eng-fra_culture_charRatios.csv"), Arrays.asList(itus));
		
		List<String> lines = FileUtils.readLines(new File("C:/Users/vpapa/Dropbox/ilsp-fc/201602_culture_eng_fra_eng_spa_datasets/sampling_eng-fra_culture.txt"));
		counter=0;
		for (String line:lines){
			if (counter==0){
				counter++;
				continue;
			}
			String[] temp = line.split("\t");
			if (!temp[group].trim().isEmpty()){
				System.out.println(temp[group]);
				int ind = Integer.parseInt(temp[group].trim()) ;
				res.add(temp[group].trim()+"\t"+tus[ind][0]+"\t"+tus[ind][1]);
			}	
		}
		FileUtils.writeLines(new File("C:/Users/vpapa/Dropbox/ilsp-fc/201602_culture_eng_fra_eng_spa_datasets/sampling_eng-fra_culture_g"+Integer.toString(group+1)+".csv"), res);
	} catch (IOException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
	
	System.exit(0);
		
		String filename="C:\\Users\\vpapa\\ABU\\spidextor_output.uniq.rand.filt.txt";
		String inputLine;
		BufferedReader in;
		int count=0, count1=0;
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
			while ((inputLine = in.readLine()) != null) {
				//String[] temp = inputLine.split("\t");
				//double d= Double.parseDouble(temp[temp.length-1]);
				//if (d>=0.35){
				//	count1++;
				//}
				count++;
			}
			in.close();
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		//BufferedReader in = new BufferedReader(new InputStreamReader(genreFile.openStream()));
		catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(0);





		String test="Tekki";
		for (int ii=0;ii<test.length();ii++){
			int int_value = (int) test.charAt(ii);
			String unicode_value = Integer.toHexString(int_value);
			System.out.println(unicode_value);
			System.out.println();
		}
		try {
			several_specific_helpful_tasks();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private static void several_specific_helpful_tasks() throws IOException {

		sample_selection("C:\\QTLaunchPad\\AUTOMOTIVE\\EN-EL\\delivered\\alignOutput2014-05-26_024952.log");

		count_tmx_scores("C:\\QTLaunchPad\\MEDICAL\\EN-PT\\delivered_uih", "alignOutput2014-05-23_105241.txt", "scores.txt");

		correct_select_count_tmx("C:\\QTLaunchPad\\MEDICAL\\EN-PT\\delivered_uih", "alignOutput2014-05-23_105241.txt", 0.4);

		select_copy_docpairs("C:\\QTLaunchPad\\MEDICAL\\EN-PT", "C:\\QTLaunchPad\\MEDICAL\\EN-PT\\delivered_uih",
				"C:\\QTLaunchPad\\MEDICAL\\EN-PT\\outputs\\output.txt" ,"u i h");

		check_bilingual_collection("C:\\QTLaunchPad\\AUTOMOTIVE\\EN-PT\\", "en;pt","output_pdfs.txt" );

		merge_outlist_files("C:\\QTLaunchPad\\MEDICAL\\EN-PT\\delivered_uih\\outputs","output.txt");

		//task1//
		String parent_dir  ="C:\\QTLaunchPad\\AUTOMOTIVE\\EN-EL\\"; 
		String pair_list ="C:\\QTLaunchPad\\AUTOMOTIVE\\EN-EL\\total_pairs.txt";
		//task2//
		String list_with_attricutes_of_files =
				"C:\\QTLaunchPad\\AUTOMOTIVE\\EN\\EN_AUTOMOTIVE_results_nonCC.txt";
		//task3/
		String xml_dir="C:\\QTLaunchPad\\AUTOMOTIVE\\EN\\qtlp_20131227_132408\\e401c679-d4d1-4099-8711-a3b97d634614\\xml";
		String xmlCC_dir="C:\\QTLaunchPad\\AUTOMOTIVE\\EN\\qtlp_20131227_132408\\e401c679-d4d1-4099-8711-a3b97d634614\\xml_CC";
		String nonCC_list="C:\\QTLaunchPad\\AUTOMOTIVE\\EN\\output_AUTOMOTIVE_EN_nonCC.txt";
		String CC_list="C:\\QTLaunchPad\\AUTOMOTIVE\\EN\\output_AUTOMOTIVE_EN_CC.txt";
		String licensed_list="C:\\QTLaunchPad\\AUTOMOTIVE\\EN\\AUTOMOTIVE_EN_licensed.txt";
		String pathstring="qtlp_20131227_132408/e401c679-d4d1-4099-8711-a3b97d634614/xml/";
		//task4/
		String target_dir =
				"C:\\QTLaunchPad\\AUTOMOTIVE\\EN\\qtlp_20131227_132408\\e401c679-d4d1-4099-8711-a3b97d634614\\xml";
		String exten="xml";
		String exclude_str="crawlinfo=";
		//task5//
		String removefilelist="C:\\QTLaunchPad\\AUTOMOTIVE\\EN\\to_remove.txt";
		String path_file_t="C:\\QTLaunchPad\\AUTOMOTIVE\\EN\\qtlp_20131227_132408\\e401c679-d4d1-4099-8711-a3b97d634614\\xml";
		int task=4;

		if (task==1){
			pdf_pairs(pair_list, parent_dir);
			System.out.println("existance of pdf files in list of pairs is checked");
		}
		if (task==2){
			//list of attributes are extracted by QueryXMLwithXpath class
			print_unique_sites_in_collection(list_with_attricutes_of_files);
			System.out.println("unique sites in collection is printed");
		}
		if (task==3){
			discriminate_CC_from_nonCC(xml_dir, xmlCC_dir, licensed_list, nonCC_list, CC_list, pathstring);
			System.out.println("files with CC licensed are moved from XML to XML_CC");
		}
		if (task==4){
			counttokens(target_dir,exten,exclude_str);
			System.out.println("tokens calculated.");
		}
		if (task==5){
			String tmp1 = FileUtils.readFileToString(new File(removefilelist));
			String[] filestoremove = tmp1.split("\n");	
			ArrayList<String> filestoremoveList=new ArrayList<String>();
			for (int ii=0;ii<filestoremove.length;ii++){
				filestoremoveList.add(filestoremove[ii]);
			}
			File xmldir = new File(path_file_t);
			String[] filesinXML= xmldir.list();
			for (int ii=0;ii<filesinXML.length;ii++){
				if (filesinXML[ii].endsWith("xml")){
					if (filestoremoveList.contains(filesinXML[ii])){
						File tempfile=new File(path_file_t+"\\"+filesinXML[ii]);
						System.out.println(tempfile.getName());
						tempfile.delete();
						tempfile=new File(path_file_t+"\\"+filesinXML[ii]+appHTMLext);
						System.out.println(tempfile.getName());
						tempfile.delete();
						String tt = filesinXML[ii].substring(0, filesinXML[ii].indexOf("."));
						tempfile=new File(path_file_t+"\\"+tt+appHTMLext);
						System.out.println(tempfile.getName());
						tempfile.delete();
					}
				}
			}
		}
	}

	private static void sample_selection(String tmx_listfile) {
		String[] types =new String[ ] {"u","i","h", "l", "m", "pdf"};
		int[] tmxfile_counts = new int[types.length], selected_tmxfile_counts = new int[types.length];
		int[] tmx_counts = new int[types.length], selected_tmx_counts = new int[types.length];
		boolean found=false;
		int total_tmx=0, totaltmxfile=0; 
		double sample_factor = 0.05, sample_factor1 = 0.05; 
		try {
			List<String> tmx_files=FileUtils.readLines(new File(tmx_listfile));
			for (int ii=0;ii<tmx_files.size();ii++){
				if (!tmx_files.get(ii).startsWith("align"))
					continue;

				String[] temp = tmx_files.get(ii).split(" ");
				found = false;
				for (int jj=0;jj<types.length;jj++){
					if (temp[0].endsWith(types[jj]+".tmx")){
						found=true;
						tmx_counts[jj]=tmx_counts[jj]+Integer.parseInt(temp[2]);
						tmxfile_counts[jj]=tmxfile_counts[jj]+1;
						break;
					}
				}
				if (!found){
					System.out.println("EP. Somethng goes wrong, since the type is not recognised.");
					System.exit(0);
				}
			}
			for (int jj=0;jj<tmx_counts.length;jj++){
				total_tmx = total_tmx + tmx_counts[jj];
				totaltmxfile = totaltmxfile + tmxfile_counts[jj];
			}
			System.out.println("total tmxfiles: "+ totaltmxfile);
			System.out.println("total tmx sentence pairs: "+ total_tmx);

			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("Enter sample size of files (Integer):");
			int sample_files=0;
			try{
				sample_files = Integer.parseInt(br.readLine());
			}catch(NumberFormatException nfe){
				System.err.println("Invalid Format!");
			}
			System.out.print("Enter sample size of sentence pairs (Integer):");
			int sample_sentencepairs=0;
			try{
				sample_sentencepairs = Integer.parseInt(br.readLine());
			}catch(NumberFormatException nfe){
				System.err.println("Invalid Format!");
			}
			System.out.println("files to be selected: " + sample_files);
			System.out.println("files to be selected: " + sample_sentencepairs);
			sample_factor = (double) sample_files / (double) totaltmxfile;
			sample_factor1 = (double) sample_sentencepairs / (double) total_tmx;
			for (int jj=0;jj<tmx_counts.length;jj++){
				selected_tmxfile_counts[jj] = (int) Math.round(sample_factor*tmxfile_counts[jj]);
				selected_tmx_counts[jj] = (int) Math.round(sample_factor1*tmx_counts[jj]);
			}
			//			System.out.println("aaa");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void count_tmx_scores(String location, String tmx_list_file, String score_text) throws IOException {
		String score="";
		List<String> pairlist =  FileUtils.readLines(new File(FilenameUtils.concat(location,tmx_list_file)));
		for (int ii=0;ii<pairlist.size();ii++){
			LOGGER.debug("ID:\t"+ii);
			LOGGER.debug(FilenameUtils.concat(location,pairlist.get(ii)));
			score = score+ 
					ReadResources.extractAttrfromXML(FilenameUtils.concat(location,pairlist.get(ii)),"tu", "score", true,false);
			if (score.isEmpty())
				continue;

		}
		WriteResources.writetextfile(FilenameUtils.concat(location,score_text), score);
	}

	private static void merge_outlist_files(String sourcedir, String newfile) throws IOException {
		File curdir = new File(sourcedir);
		File[] files= curdir.listFiles();

		String whole_text = "";
		String filename = FilenameUtils.concat(curdir.getAbsolutePath(),newfile);
		for (File file:files){
			if (file.getName().endsWith(".txt") & file.getName().startsWith("out")){
				String text =FileUtils.readFileToString(file);
				whole_text=whole_text+"\n"+text;
			}
		}
		WriteResources.writetextfile(filename, whole_text);
	}

	private static void check_bilingual_collection(String source_path,	String langs1, String output_list_file) throws IOException {
		int p_xml=0, p_xmlhtml=0, xml1=0, xml2=0, html1=0, html2=0, xmlhtml1=0, xmlhtml2=0;
		String[] langs = langs1.split(";");
		int[] tokens = new int[2];
		ArrayList<String> fff = new ArrayList<String>();
		List<String> pairs =FileUtils.readLines(new File(FilenameUtils.concat(source_path,output_list_file)));

		for (int ii=0; ii<pairs.size();ii++){
			pairs.set(ii, pairs.get(ii).replace("/", "\\"));
			if (pairs.get(ii).startsWith("pdfs") )
				continue;
			File pairfile = new File(source_path+"\\"+pairs.get(ii));
			String pparent = pairfile.getParent();
			String pname =  pairfile.getName();
			String[] items = pname.split("_");

			//xml pair
			if (pairfile.exists())
				p_xml++;
			else
				LOGGER.info(pairfile.getAbsolutePath() + " does not exist.");
			//xmlhtml pair
			pairfile = new File(FilenameUtils.concat(source_path,pairs.get(ii)+appHTMLext));
			if (pairfile.exists())
				p_xmlhtml++;
			else
				LOGGER.info(pairfile.getAbsolutePath() + " does not exist.");

			File file1 = new File(FilenameUtils.concat(pparent,items[0]+appXMLext));
			File file2 = new File(FilenameUtils.concat(pparent,items[1]+appXMLext));
			//xml 1
			if (file1.exists()){
				//String langIdentified = ReadResources.extractLangfromXML(file1.getAbsolutePath(), "language", "iso639");

				String langIdentified = ReadResources.extractAttrfromXML(file1.getAbsolutePath(), "language", "iso639", true,false);
				String text_temp = ReadResources.extractTextfromXML_clean(file1.getAbsolutePath(),type_p,"crawlinfo", false);

				if (langIdentified.equals(langs[0]))
					tokens[0]=tokens[0]+FCStringUtils.countTokens(text_temp);
				else
					tokens[1]=tokens[1]+FCStringUtils.countTokens(text_temp);
				xml1++;

				//ReadResources.writetextfile(source_path+"\\"+langs[0]+"\\"+xml1+".txt", text_temp);
				//FileUtils.copyFile(file1, new File(source_path+"\\"+langs[0]+"\\"+xml1+appXMLext));
			}else
				LOGGER.info(file1.getAbsolutePath() + " does not exist.");
			//xml2
			if (file2.exists()){
				//String langIdentified = ReadResources.extractLangfromXML(file2.getAbsolutePath(), "language", "iso639");
				String langIdentified = ReadResources.extractAttrfromXML(file1.getAbsolutePath(), "language", "iso639", true, false);
				String text_temp = ReadResources.extractTextfromXML_clean(file2.getAbsolutePath(),type_p,"crawlinfo", false);
				if (langIdentified.equals(langs[0]))
					tokens[0]=tokens[0]+FCStringUtils.countTokens(text_temp);
				else
					tokens[1]=tokens[1]+FCStringUtils.countTokens(text_temp);
				xml2++;
				//ReadResources.writetextfile(source_path+"\\"+langs[1]+"\\"+xml1+".txt", text_temp);
				//FileUtils.copyFile(file2, new File(source_path+"\\"+langs[1]+"\\"+xml2+appXMLext));
			}else
				LOGGER.info(file2.getAbsolutePath() + " does not exist.");

			if (fff.contains(file1.getAbsolutePath()))
				LOGGER.info("EEEP	" +  file1.getAbsolutePath());
			else
				fff.add(file1.getAbsolutePath());
			if (fff.contains(file2.getAbsolutePath()))
				LOGGER.info("EEEP	" +  file2.getAbsolutePath());
			else
				fff.add(file2.getAbsolutePath());

			file1 = new File(pparent+"\\"+items[0]+appHTMLext);
			file2 = new File(pparent+"\\"+items[1]+appHTMLext);
			//html 1
			if (file1.exists())
				html1++;
			else
				LOGGER.info(file1.getAbsolutePath() + " does not exist.");
			//html2
			if (file2.exists())
				html2++;
			else
				LOGGER.info(file2.getAbsolutePath() + " does not exist.");

			file1 = new File(pparent+"\\"+items[0]+appXMLHTMLext);
			file2 = new File(pparent+"\\"+items[1]+appXMLHTMLext);
			//xmlhtml 1
			if (file1.exists())
				xmlhtml1++;
			else
				LOGGER.info(file1.getAbsolutePath() + " does not exist.");
			//xmlhtml2
			if (file2.exists())
				xmlhtml2++;
			else
				LOGGER.info(file2.getAbsolutePath() + " does not exist.");
		}
		LOGGER.info(p_xml +"\t"+ p_xmlhtml+"\t"+ xml1+"\t"+xml2+"\t"+html1+"\t"+html2+"\t"+xmlhtml1+"\t"+xmlhtml2);
		System.out.println("DONE");
	}

	private static void select_copy_docpairs(String sourcedir1, String targetdir1, String cesAling_listfile, String type) {
		try {
			File sourcedir = new File(sourcedir1);
			File targetdir = new File(targetdir1);
			String[] pairlist =FileUtils.readFileToString(new File(cesAling_listfile)).replace("/", "\\").split("\n");
			String[] types = type.split(" ");
			for (int ii=0;ii<pairlist.length;ii++){	
				if (pairlist[ii].startsWith("pdfs"))
					continue;
				if (!pairlist[ii].endsWith("_"+types[0]+appXMLext) 
						& !pairlist[ii].endsWith("_"+types[1]+appXMLext) 
						& !pairlist[ii].endsWith("_"+types[2]+appXMLext))
					continue;
				LOGGER.info(ii);
				//File temppair = new File(sourcedir.getAbsolutePath()+fs+pairlist[ii]);
				//File temppairNEW = new File(targetdir.getAbsolutePath()+fs+pairlist[ii]);
				//FcFileUtils.copyFile(temppair, temppairNEW);
				FcFileUtils.copy(FilenameUtils.concat(sourcedir.getAbsolutePath(),pairlist[ii]), FilenameUtils.concat(targetdir.getAbsolutePath(),pairlist[ii]));

				//temppair = new File(sourcedir.getAbsolutePath()+"\\"+pairlist[ii]+appHTMLext);
				//temppairNEW = new File(targetdir.getAbsolutePath()+"\\"+pairlist[ii]+appHTMLext);
				//FcFileUtils.copyFile(temppair, temppairNEW);
				FcFileUtils.copy(FilenameUtils.concat(sourcedir.getAbsolutePath(),pairlist[ii]+appHTMLext),
						FilenameUtils.concat(targetdir.getAbsolutePath(),pairlist[ii]+appHTMLext));

				File temppair = new File(FilenameUtils.concat(sourcedir.getAbsolutePath(),pairlist[ii]));
				File temppairNEW = new File(FilenameUtils.concat(targetdir.getAbsolutePath(),pairlist[ii]+appHTMLext));
				String[] tempitems = temppair.getName().split("_");

				//File tempitem = new File(temppair.getParent()+"\\"+tempitems[0]+appXMLext);
				//File tempitemNEW = new File(temppairNEW.getParent()+"\\"+tempitems[0]+appXMLext);
				//FcFileUtils.copyFile(tempitem, tempitemNEW);
				FcFileUtils.copy(FilenameUtils.concat(temppair.getParent(),tempitems[0]+appXMLext),
						FilenameUtils.concat(temppairNEW.getParent(),tempitems[0]+appXMLext));

				//tempitem = new File(temppair.getParent()+"\\"+tempitems[0]+appXMLHTMLext);
				//tempitemNEW = new File(temppairNEW.getParent()+"\\"+tempitems[0]+appXMLHTMLext);
				//FcFileUtils.copyFile(tempitem, tempitemNEW);
				FcFileUtils.copy(FilenameUtils.concat(temppair.getParent(),tempitems[0]+appXMLHTMLext),
						FilenameUtils.concat(temppairNEW.getParent(),tempitems[0]+appXMLHTMLext));

				//tempitem = new File(temppair.getParent()+"\\"+tempitems[0]+appHTMLext);
				//tempitemNEW = new File(tempitemNEW.getParent()+"\\"+tempitems[0]+appHTMLext);
				//FcFileUtils.copyFile(tempitem, tempitemNEW);
				FcFileUtils.copy(FilenameUtils.concat(temppair.getParent(),tempitems[0]+appHTMLext),
						FilenameUtils.concat(temppairNEW.getParent(),tempitems[0]+appHTMLext));

				//tempitem = new File(temppair.getParent()+"\\"+tempitems[1]+appXMLext);
				//tempitemNEW = new File(temppairNEW.getParent()+"\\"+tempitems[1]+appXMLext);
				//FcFileUtils.copyFile(tempitem, tempitemNEW);
				FcFileUtils.copy(FilenameUtils.concat(temppair.getParent(),tempitems[1]+appXMLext),
						FilenameUtils.concat(temppairNEW.getParent(),tempitems[1]+appXMLext));

				//tempitem = new File(temppair.getParent()+"\\"+tempitems[1]+appXMLHTMLext);
				//tempitemNEW = new File(temppairNEW.getParent()+"\\"+tempitems[1]+appXMLHTMLext);
				//FcFileUtils.copyFile(tempitem, tempitemNEW);
				FcFileUtils.copy(FilenameUtils.concat(temppair.getParent(),tempitems[1]+appXMLHTMLext),
						FilenameUtils.concat(temppairNEW.getParent(),tempitems[1]+appXMLHTMLext));

				//tempitem = new File(temppair.getParent()+"\\"+tempitems[1]+appHTMLext);
				//tempitemNEW = new File(tempitemNEW.getParent()+"\\"+tempitems[1]+appHTMLext);
				//FcFileUtils.copyFile(tempitem, tempitemNEW);
				FcFileUtils.copy(FilenameUtils.concat(temppair.getParent(),tempitems[1]+appHTMLext),
						FilenameUtils.concat(temppairNEW.getParent(),tempitems[1]+appHTMLext));
			}
		} catch (IOException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
	}

	private static void correct_select_count_tmx(String location, String tmx_list_file,	double align_thr) {

		int counter_tmx=0;
		try {
			String tmx_list = FileUtils.readFileToString(new File(FilenameUtils.concat(location,tmx_list_file)));
			String[] pairlist = tmx_list.split("\r\n");
			for (int ii=0;ii<pairlist.length;ii++){
				XMLTextCharsCleaner.clean(FilenameUtils.concat(location,pairlist[ii]), FilenameUtils.concat(location,pairlist[ii]+"1"));
				FcFileUtils.copy(FilenameUtils.concat(location,pairlist[ii]+"1"),FilenameUtils.concat(location,pairlist[ii]));
				File temp = new File(FilenameUtils.concat(location,pairlist[ii]+"1"));
				temp.delete();
			}
			for (int ii=0;ii<pairlist.length;ii++){
				LOGGER.info("ID:\t"+ii);
				LOGGER.info(FilenameUtils.concat(location,pairlist[ii]));
				String score = 
						ReadResources.extractAttrfromXML(FilenameUtils.concat(location,pairlist[ii]),"tu", "score", true, false);
				if (score.isEmpty())
					continue;
				String scores[] =score.split("\n");
				LOGGER.info(scores.length);
				for (int jj=0; jj<scores.length;jj++){
					if (Double.parseDouble(scores[jj])>=align_thr){
						counter_tmx++;
					}
				}
			}
		} catch (IOException e3) {
			e3.printStackTrace();
		}
		System.out.println(counter_tmx);

	}

	private static void pdf_pairs(String pair_list, String parent_dir) {

		File tempfile = new File(pair_list);
		try {
			BufferedReader in = new BufferedReader(new FileReader(tempfile));
			String str; //, file1, file2;
			int counter=1;
			//HashMap<String, Integer> sites = new  HashMap<String, Integer>();
			while ((str = in.readLine()) != null) {
				String[] linedata = str.split("\t");			
				if (linedata.length!=2){
					System.out.println("problem in line "+counter+ " of " + pair_list);
					System.exit(0);
				}
				//file1=linedata[0].replaceAll("/", fs);		file2=linedata[1].replaceAll("/", fs);
				File pdf1=new File(FilenameUtils.concat(parent_dir,linedata[0]));
				File pdf2=new File(FilenameUtils.concat(parent_dir,linedata[1]));
				if (!pdf1.exists()){
					System.out.println("DOES NOT EXIST file "+ pdf1.getAbsolutePath() + " mentioned in line "+counter+ " of " + pair_list);
					//System.exit(0);
				}
				if (!pdf2.exists()){
					System.out.println("DOES NOT EXIST file "+ pdf2.getAbsolutePath() + " mentioned in line "+counter+ " of " + pair_list);
					//System.exit(0);
				}
				counter++;
			}
			in.close();
		} catch (IOException e) {
		}
	}


	private static void print_unique_sites_in_collection(
			String list_with_attricutes_of_files) {

		File tempfile = new File(list_with_attricutes_of_files);
		try {
			BufferedReader in = new BufferedReader(new FileReader(tempfile));
			String str, site, tempstr; //, tempsite;
			int ind1, ind2, counter; //ind3, ind4 
			HashMap<String, Integer> sites = new  HashMap<String, Integer>();
			while ((str = in.readLine()) != null) {
				String[] linedata = str.split("\t");				
				site=linedata[2];				ind1=site.indexOf("//");
				tempstr=site.substring(ind1+2);	ind2=tempstr.indexOf("/")+ind1+2;
				if (ind2>=site.length()| ind2<0)
					ind2=site.length()-1;
				site = site.substring(ind1+2, ind2);	//	ind3 = site.lastIndexOf(".");
				//tempsite = site.substring(0, ind3);			ind4 = tempsite.lastIndexOf(".");
				//site = site.substring(ind4+1);
				if (sites.containsKey(site)){
					counter = sites.get(site)+1;
					sites.put(site, counter);
				}
				else
					sites.put(site, 1);
			}
			in.close();
			Set<String> site_keys=sites.keySet();
			Iterator<String> key = site_keys.iterator();
			String key_im = "";
			while (key.hasNext()){
				key_im = key.next();
				//LOGGER.info(key_im+"\t"+sites.get(key_im));
				System.out.println(key_im+"\t"+sites.get(key_im));
			}
		} catch (IOException e) {
		}
	}


	private static void discriminate_CC_from_nonCC(String xml_dir,
			String xmlCC_dir, String licensed_list, String nonCC_list, String CC_list, String pathstring) {

		String tmp1;
		try {
			tmp1 = FileUtils.readFileToString(new File(licensed_list));
			String[] filestoremove = tmp1.split("\n");	
			ArrayList<String> filestoremoveList=new ArrayList<String>();
			for (int ii=0;ii<filestoremove.length;ii++)
				filestoremoveList.add(filestoremove[ii]);
			File xmldir = new File(xml_dir);
			String[] filesinXML= xmldir.list();
			int counter=0;
			for (int ii=0;ii<filesinXML.length;ii++){
				if (filesinXML[ii].endsWith("xml")){
					if (filestoremoveList.contains(filesinXML[ii])){
						String tt = filesinXML[ii].substring(0,filesinXML[ii].indexOf("."));
						//appHTMLext
						File tempfile=new File(xml_dir+"\\"+tt+appHTMLext);
						File destfile=new File(xmlCC_dir+"\\"+tt+appHTMLext);
						FileUtils.moveFile(tempfile, destfile);
						//appXMLext
						tempfile=new File(xml_dir+"\\"+filesinXML[ii]);
						destfile=new File(xmlCC_dir+"\\"+tt+appXMLext);
						FileUtils.moveFile(tempfile, destfile);
						//appXMLHTMLext
						tt=filesinXML[ii].substring(0, filesinXML[ii].lastIndexOf("."))+appXMLHTMLext;
						tempfile=new File(xml_dir+"\\"+tt);
						destfile=new File(xmlCC_dir+"\\"+tt);
						FileUtils.moveFile(tempfile, destfile);

						counter++;
						System.out.println(counter);
					}
				}
			}

		}catch (IOException e1) {
			e1.printStackTrace();
		}

		File xmldir = new File(xml_dir);
		String[] filesinXML= xmldir.list();
		for (int ii=0;ii<filesinXML.length;ii++){
			if (filesinXML[ii].endsWith("html") & !filesinXML[ii].contains("xml.html")){
				File tempfile=new File(xml_dir+"\\"+filesinXML[ii]);
				System.out.println(tempfile.getName());
				tempfile.delete();
			}
		}

		String temp_dir = xmlCC_dir;
		xmldir = new File(temp_dir);
		filesinXML= xmldir.list();
		String urlList="";
		for (int ii=0;ii<filesinXML.length;ii++){
			if (filesinXML[ii].endsWith("xml"))
				urlList=urlList + pathstring+filesinXML[ii]+"\n";
		}
		WriteResources.writetextfile(CC_list,urlList);
		writeHTMLfile(CC_list+appHTMLext,urlList,true);

		temp_dir = xml_dir;
		xmldir = new File(temp_dir);
		filesinXML= xmldir.list();
		urlList="";
		for (int ii=0;ii<filesinXML.length;ii++){
			if (filesinXML[ii].endsWith("xml"))
				urlList=urlList + pathstring+filesinXML[ii]+"\n";
		}
		WriteResources.writetextfile(nonCC_list,urlList);
		writeHTMLfile(nonCC_list+appHTMLext,urlList,true);
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
				String fileURL = new File(xmlFile.getAbsolutePath()+appHTMLext)
				.getAbsolutePath().replace("\\", "/");
				//String ttt=qq.toURI().toString();
				//URL fileURL = xmlFile.toURI().toURL();
				if (applyOfflineXSLT2)
					//ttt= "<a href=\""+fileURL+".html\">\n"+xmlFile.getAbsolutePath()+".html</a>";
					ttt= "<a href=\""+fileURL+"\">\n"+fileURL+"</a>";
				else
					//ttt= "<a href=\""+fileURL+"\">\n"+xmlFile.getAbsolutePath()+"</a>";
					ttt= "<a href=\""+xmlFile.getAbsolutePath().replace("\\", "/")+"\">\n"+
							xmlFile.getAbsolutePath().replace("\\", "/")+"</a>";

				//<a href="https://issues.apache.org/jira/browse/NUTCH-721" target="_blank">NUTCH-721</a>
				xmlFileListWrt1.write("<br />"+ttt.replace("\\","/")+"\n");
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


	public static void counttokens(String target_dir, String exten, String exclude_str) throws IOException {
		File xmldir = new File(target_dir);
		String[] filesinXML= xmldir.list();
		String tmp1;
		int tokens_num=0;
		for (int ii=0;ii<filesinXML.length;ii++){
			if (filesinXML[ii].endsWith(exten)){
				tmp1 = FileUtils.readFileToString(new File(FilenameUtils.concat(target_dir,filesinXML[ii])));
				String[] lines = tmp1.split("\n");
				for (int jj=0;jj<lines.length;jj++){
					if (lines[jj].contains("<p id=") & !lines[jj].contains(exclude_str)){
						int s=lines[jj].indexOf(">")+1;
						int e=lines[jj].length()-4;
						String temp=lines[jj].substring(s, e);
						tokens_num=tokens_num+FCStringUtils.countTokens(temp);
					}
				}
			}
		}
	}


	public static String handleCopiedSite(InputStream input) {
		String url="";
		int len = +HTTRACK1.length();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(input,"UTF-8"));
			String nextLine="";
			while((nextLine = reader.readLine())!=null){
				int i1 = nextLine.indexOf(HTTRACK1);
				int i2 = nextLine.indexOf(HTTRACK2);
				if (i1<i2){
					url=nextLine.substring(i1+len, i2-1).trim();
					break;
				}
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return url;
	}

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
	}*/

}
