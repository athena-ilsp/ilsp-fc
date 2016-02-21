package gr.ilsp.fc.dedup;

import gr.ilsp.fc.dedup.DedupUtils.TextParsAttr;
import gr.ilsp.fc.langdetect.LangDetectUtils;
import gr.ilsp.fc.main.WriteResources;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

public class DedupParsMD5 {

	private static final Logger LOGGER = Logger.getLogger(DedupParsMD5.class);
	private static File input;
	private static File out_textfile;
	private static final String xmlext="xml";
	private static final String txtext="txt";
	private static final String appHTMLext = ".html";
	private static final String appXMLHTMLext = ".xml.html";
	private static final String UNDERSCORE_STR = "_";
	//private static double inter_thr=0.7;

	/**
	 * Gets files ending with input_type (cesDoc files are targeted) in the indirname directory,
	 * files in filesinPairs are excluded,
	 * finds (near) duplicates based on lists of quantized frequencies,
	 * discards the shortest document of each detected pair of (near) duplicates,
	 * generates lists of remaining cesDocs and their HTML transformations
	 * @param indirname : 
	 * @param filesinPairs 
	 * @param outputfilename
	 * @param outputHTMLfilename
	 * @param applyOfflineXSLT
	 * @param inputtype 
	 */

	public static void dedup(File indirname, Set<String> filesinPairs, File outputfilename,
			File outputHTMLfilename, 	boolean applyOfflineXSLT, int MIN_PAR_LEN, double inter_thr, final String input_type) {

		input= indirname;
		if (!input.exists() || !input.isDirectory()){
			System.err.println( "the directory with the cesdoc files does not exist!!!!!!!!" );			
			System.exit(64);
		}
		out_textfile =outputfilename;
		FilenameFilter filter = new FilenameFilter() {			
			public boolean accept(File arg0, String arg1) {
				return (arg1.substring(arg1.length()-(input_type.length()+1)).equals("."+input_type) && !arg1.contains(UNDERSCORE_STR));
			}
		};

		File[] files=input.listFiles(filter);
		if (files.length<2){
			LOGGER.info("The input list contains less than 2 files.");
			return;
		}
		long start = System.currentTimeMillis();

		int cents=0;
		HashMap<String, TextParsAttr> filesTextParsAttrs = new HashMap<String, TextParsAttr>();
		if (input_type.equals(txtext)){
			LangDetectUtils.loadCybozuLangIdentifier();
		}
		for (int ii=0;ii<files.length;ii++){
			TextParsAttr t =null;
			if (input_type.equals(xmlext))
				try {
					t= DedupUtils.getTextParsAttr(files[ii], MIN_PAR_LEN,input_type);
				} catch (IOException e) {
					LOGGER.error("Problem in reading file "+files[ii].getAbsolutePath());
					e.printStackTrace();
				}
			if (input_type.equals(txtext)){
				try {
					t= DedupUtils.getTextParsAttr(files[ii], MIN_PAR_LEN,input_type);
				} catch (IOException e) {
					LOGGER.error("Problem in reading file "+files[ii].getAbsolutePath());
					e.printStackTrace();
				}
			}
			if (t==null)
				continue;
			filesTextParsAttrs.put(t.filename, t);
			if (ii/1000>cents){
				cents++;
				LOGGER.info("properties for more than "+ cents*1000+" files have been extracted.");
			}
		}
		Set<String> keys=filesTextParsAttrs.keySet();
		Iterator<String> it = keys.iterator();
		String file1_key , file_key;
		HashSet<String> checked1 = new HashSet<String>();
		int counter=0;
		cents=0;
		double common_pars_length;
		while (it.hasNext()){
			file_key = it.next();
			if (checked1.contains(file_key) | filesTextParsAttrs.get(file_key)==null){
				continue;
			}
			Iterator<String> it1 = keys.iterator();
			List<String> tempkeylist = filesTextParsAttrs.get(file_key).parskeys;

			counter++;
			while (it1.hasNext()){
				file1_key = it1.next();
				if (file1_key.equals(file_key))
					continue;
				if (checked1.contains(file_key) || checked1.contains(file1_key) || filesTextParsAttrs.get(file1_key)==null )
					continue;
				if (filesinPairs!=null &&	filesinPairs.contains(file_key) && filesinPairs.contains(file1_key)  ){
					continue;
				}
				//intersection of 2 lists and count tokens of common paragraphs
				common_pars_length=0;
				List<String> tempkeylist1 = filesTextParsAttrs.get(file1_key).parskeys; 
				List<Integer> tempparlist1 = filesTextParsAttrs.get(file1_key).parslengths; 
				ArrayList<Integer> examined = new ArrayList<Integer>();
				for(int i = tempkeylist1.size() - 1; i > -1; --i){
					String str = tempkeylist1.get(i);
					for (int j=0;j<tempkeylist.size();j++){
						if (tempkeylist.get(j).equals(str) & !examined.contains(j)){
							common_pars_length+=tempparlist1.get(i);
							examined.add(j);
							break;
						}
					}
				}
				if ( common_pars_length/filesTextParsAttrs.get(file1_key).length > inter_thr
						|| common_pars_length/filesTextParsAttrs.get(file_key).length >inter_thr){	

					String file_to_delete=""; 
					//if there are pairs and one of the examining files are in a pair
					if (filesinPairs!=null &&	(filesinPairs.contains(file_key)|| filesinPairs.contains(file1_key) ) ){
						if (filesinPairs.contains(file_key) && !filesinPairs.contains(file1_key)){
							file_to_delete = FilenameUtils.concat(input.getPath(),file1_key);
							checked1.add(file1_key);
						}else{
							if (!filesinPairs.contains(file_key) && filesinPairs.contains(file1_key)){
								file_to_delete = FilenameUtils.concat(input.getPath(), file_key);
								checked1.add(file_key);
							}
						}
					}else{	
						if (filesTextParsAttrs.get(file1_key).length>filesTextParsAttrs.get(file_key).length){
							file_to_delete = FilenameUtils.concat(input.getPath(),file_key);
							checked1.add(file_key);
						}else{
							file_to_delete = FilenameUtils.concat(input.getPath(),file1_key);
							checked1.add(file1_key);
						}
					}
					if (!file_to_delete.isEmpty()){
						LOGGER.debug(file_key+"\t\t"+ file1_key + "\tDELETED: "+ file_to_delete);
						(new File(file_to_delete)).delete();
						(new File(file_to_delete.replace("."+input_type,appHTMLext))).delete();
						(new File(file_to_delete.replace("."+input_type,appXMLHTMLext))).delete();
					}
				}
			}
			checked1.add(file_key);
			if (counter/1000>cents){
				cents++;
				LOGGER.info("more than "+ cents*1000+" files have been checked.");
			}
		}

		files=input.listFiles(filter);
		List<File> remFiles =new ArrayList<File>(); 
		for (File file:files){
			remFiles.add(file); 
		}
		WriteResources.WriteTextList(remFiles, out_textfile);
		if (applyOfflineXSLT){
				WriteResources.WriteHTMLList(remFiles, outputHTMLfilename);
		}
	
		long elapsedTime = System.currentTimeMillis()-start;
		LOGGER.info("Deduplication completed in " + elapsedTime + " milliseconds. "+ remFiles.size() +  " files remained.");
	}

}
