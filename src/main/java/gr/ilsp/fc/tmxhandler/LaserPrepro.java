package gr.ilsp.fc.tmxhandler;

import gr.ilsp.fc.readwrite.ReadResources;
import gr.ilsp.nlp.commons.Constants;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;


public class LaserPrepro {
	private static final Logger LOGGER = Logger.getLogger(LaserPrepro.class);
	private static final String ooi_crawlinfo = "crawlinfo";
	private static final String P_ELE = "p";
	//private static final String EXT_TMX = ".tmx";
	private static final String EXT_XML = ".xml";
	private static final String XML = "xml";

	public static void main(String[] args) {
		LOGGER.info("1st arg. could be: 0 for all jobs, 1 for laserprepro, 2 for merging lists");
		LOGGER.info("\n");
		LOGGER.info("fullpath of input directory");
		LOGGER.info("\n");
		LOGGER.info("could be 1 (meaning true, default and proposed), or 0 (meaning false). If true, only identified pairs (by pairdetection module) will be examined."
				+ " If false, (besides identified pairs) each candidate pair will be examined, and this will take long ...");
		LOGGER.info("\n");
		String mode = args[0];
		File input = new File(args[1]);
		String langpair = args[2];
		boolean paired = true;
		String temp = "1";
		if (args.length>2)
			temp = args[2];
		if (temp.equals("0"))
			paired = false;
		else
			paired = true;
		if (mode.equals("0"))
			alljobs(input, langpair, paired);	
		if (mode.equals("1"))
			laserprepro(input, langpair,false);		
		if (mode.equals("2"))
			mergelists(input);
		if (mode.equals("3")){
			//filterLaserOut(input, args[2], "en", 1.2,  false, false);
			//filterLaserOut(input, args[2], "en", 1.1,  false, false);
			//filterLaserOut(input, args[2], "en", 0.9,  false, false);
			//filterLaserOut(input, args[2], "en", 1.2,  false, true);
			//filterLaserOut(input, args[2], "en", 1.2,  true, false);
			LOGGER.info("!!!!!!NOT READY!!!!!!!!!!");
		}
	}

	private static void alljobs(File input, String langpair, boolean paired) {
		if (!input.isDirectory()){
			LOGGER.error("When mode=all, input should be a directory");
			return;
		}
		List<String> listdirs = new ArrayList<String>();
		List<String> xmldirs = recursiveList(input, XML, listdirs);
		File xmldirslist = new File (FilenameUtils.concat(input.getParent(), input.getName()+"_xmldirs"));
		try {
			FileUtils.writeLines(xmldirslist, Constants.UTF8, xmldirs, "\n");
		} catch (IOException e) {
			LOGGER.warn("text file with path of xml dirs cannot be written");
			e.printStackTrace();
		}
		LOGGER.info("NUMBER of XML directories is : " + xmldirs.size());

		File pairlist = new File(FilenameUtils.concat(input.getParent(), input.getName()+"_pairlist.tsv"));
		List<String> pairs = new ArrayList<String>();
		FileOutputStream fos;
		int counter = 0;
		try {
			fos = new FileOutputStream(pairlist);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos, Constants.UTF8));
			for (String xmldir:xmldirs){
				File temp = new File(xmldir);
				File txtlist = laserprepro(temp, langpair, paired);
				try {
					pairs = FileUtils.readLines(txtlist, Constants.UTF8);
				} catch (IOException e) {
					LOGGER.error("problem in reading "+ txtlist.getAbsolutePath());
				}
				LOGGER.info(temp.getAbsolutePath()+Constants.TAB+pairs.size());
				for (String pair:pairs){
					counter++;
					bw.write(pair+"\n");
				}
			}
			bw.close();
			fos.close();
		} catch (FileNotFoundException e) {
			LOGGER.error("problem in writing "+ pairlist.getAbsolutePath());
		} catch (IOException e) {
			LOGGER.error("problem in writing files.");
			e.printStackTrace();
		}
		LOGGER.info("Total number of document pairs:" +Constants.TAB + counter);
		LOGGER.info("Results in "+ pairlist.getAbsolutePath());
	}


	public static List<String> recursiveList(File infile, String filenamefilter, List<String> targetdirs) {
		File[] f = infile.listFiles();
		for (int i = 0; i < f.length; i++) {
			if (f[i].isDirectory() && !f[i].isHidden() ) {
				//System.out.println(f[i].getAbsolutePath());
				if (f[i].getName().contains(filenamefilter)){
					targetdirs.add(f[i].getAbsolutePath());
				}
				recursiveList(f[i], filenamefilter, targetdirs);
			} //else {
			//	System.out.println(f[i].getName());
			//}
		}
		return targetdirs;
	}



	/**
	 * Merges the lists of fullpaths that are included in each txt file. 
	 * @param input : a text file including the fullpaths of text files (a fullpath per textline) that contain the full paths of the txt files which participate into TMX files
	 */
	private static void mergelists(File input) {
		if (!input.isFile()){
			LOGGER.error("When mode=1, input should be a file");
			return;
		}
		List<String> pairs = new ArrayList<String>();
		int counter = 0;
		try {
			List<String> lists = FileUtils.readLines(input, Constants.UTF8);
			List<String> temp = new ArrayList<String>();
			for (String list:lists){
				temp = FileUtils.readLines(new File(list), Constants.UTF8);
				LOGGER.info(list+ "\t\t" + temp.size());
				counter = counter + temp.size();
				pairs.addAll(temp);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		LOGGER.info(counter+ "\t\t" + pairs.size());
		File outfile = new File(input.getAbsolutePath()+".merge");
		try {
			FileUtils.writeLines(outfile, Constants.UTF8, pairs, "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Generates:
	 * a) "txts" directory which contains the txt version of the CesDoc files which "participate" into paired files
	 * b) txt file including the full paths of the txt files (a pair per line, tab separated)
	 * @param indir : the xml directory of ILSP-FC
	 * @param paired : if true, only identified pairs will be processed. If yes, the remaining "pairs" will be generated and examined
	 * @param langpair : the pair of targeted languages, separated by "-"  
	 */
	public static File  laserprepro(File indir, String langpair, boolean paired) {
		if (!indir.isDirectory()){
			LOGGER.error("When mode=0, input should be a directory");
			return null;
		}
		File txtdir = new File(FilenameUtils.concat(indir.getParent(), "txts1_"+langpair));
		txtdir.mkdir();
		File txtlist1 = new File(FilenameUtils.concat(indir.getParent(), "txts_true_list_"+langpair));
		File txtlist2 = new File(FilenameUtils.concat(indir.getParent(), "txts_false_list_"+langpair));
		File[] files = indir.listFiles();
		List<String> txtpairs = new ArrayList<String>();
		List<String> donexmls = new ArrayList<String>();
		String[] targetedlangs = langpair.split(Constants.HYPHEN);
		int counter = 0;
		for (File file:files){
			String tempname = file.getName();
			if (!tempname.contains(Constants.UNDERSCORE))
				continue;
			//if (tempname.endsWith(".tmx")){
			if (!tempname.endsWith(EXT_XML))
				continue;
			if (!(tempname.contains(targetedlangs[0]) && tempname.contains(targetedlangs[1])))
				continue;
			LOGGER.debug("Processing file " + file.getAbsolutePath());
			String[] parts = tempname.split(Constants.UNDERSCORE);
			File f1 = new File(FilenameUtils.concat(file.getParent(), parts[0]));
			f1 = new File(FilenameUtils.concat(file.getParent(), parts[0]+EXT_XML));
			File f2 = new File(FilenameUtils.concat(file.getParent(), parts[1]));
			f2 = new File(FilenameUtils.concat(file.getParent(), parts[1]+EXT_XML));
			if (donexmls.contains(f1.getName()) || donexmls.contains(f2.getName()))
				continue;
			counter++;
			String text1 =  ReadResources.extractTextfromXML_clean(f1.getAbsolutePath(), P_ELE, ooi_crawlinfo, false);
			String text2 =  ReadResources.extractTextfromXML_clean(f2.getAbsolutePath(), P_ELE, ooi_crawlinfo, false);
			//LOGGER.info("CES file " + f1.getAbsolutePath()); LOGGER.info("CES file " + f2.getAbsolutePath());
			File tf1 = new File(FilenameUtils.concat(txtdir.getAbsolutePath(), f1.getName()+Constants.EXTENSION_TXT));
			File tf2 = new File(FilenameUtils.concat(txtdir.getAbsolutePath(), f2.getName()+Constants.EXTENSION_TXT));
			try {
				FileUtils.writeStringToFile(tf1, text1, Constants.UTF8);
				FileUtils.writeStringToFile(tf2, text2, Constants.UTF8);
			} catch (IOException e) {
				e.printStackTrace();
			}
			//LOGGER.info("TXT file" + tf1.getAbsolutePath()); 	LOGGER.info("TXT file " + tf2.getAbsolutePath());
			txtpairs.add(tf1.getAbsolutePath()+Constants.TAB+tf2.getAbsolutePath());
			donexmls.add(f1.getName());
			donexmls.add(f2.getName());
		}
		try {
			FileUtils.writeLines(txtlist1, Constants.UTF8, txtpairs, "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (paired){
			LOGGER.info("LIST TXT files " + txtlist1.getAbsolutePath());
			LOGGER.info("CES pairs = "+ counter);
			LOGGER.info("TXT pairs = "+ txtpairs.size());
			return txtlist1;
		}
		List<File> templist = new ArrayList<File>();	
		for (File file:files){
			String tempname = file.getName();
			if (tempname.endsWith(EXT_XML) && !tempname.contains(Constants.UNDERSCORE)){
				if (!tempname.contains(targetedlangs[0]+Constants.HYPHEN) && !tempname.contains(targetedlangs[1]+Constants.HYPHEN))
					continue;
				if (!donexmls.contains(tempname)){
					String text1 =  ReadResources.extractTextfromXML_clean(file.getAbsolutePath(), P_ELE, ooi_crawlinfo, false);
					File tf1 = new File(FilenameUtils.concat(txtdir.getAbsolutePath(), file.getName()+Constants.EXTENSION_TXT));
					try {
						FileUtils.writeStringToFile(tf1, text1, Constants.UTF8);
					} catch (IOException e) {
						e.printStackTrace();
					}
					donexmls.add(tempname);
					templist.add(file);
				}
			}
		}
		for (int ii=0;ii<templist.size()-1;ii++){
			File tempfile1 = templist.get(ii);
			String a1 = tempfile1.getName().split(Constants.HYPHEN)[0];
			for (int jj=ii+1;jj<templist.size();jj++){
				File tempfile2 = templist.get(jj);
				String a2 = tempfile2.getName().split(Constants.HYPHEN)[0];
				if (!a1.equals(a2)){
					txtpairs.add(tempfile1.getAbsolutePath()+Constants.EXTENSION_TXT+Constants.TAB+tempfile2.getAbsolutePath()+Constants.EXTENSION_TXT);
				}
			}
		}
		try {
			FileUtils.writeLines(txtlist2, Constants.UTF8, txtpairs, "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		LOGGER.info("TXT pairs = "+ txtpairs.size());
		return txtlist2;
	}
}
