package gr.ilsp.fc.dedup;

import gr.ilsp.fc.dedup.DedupUtils.TextAttr;
import gr.ilsp.fc.dedup.DedupUtils;
import gr.ilsp.fc.dedup.DedupUtils.TextFullAttr;
import gr.ilsp.fc.readwrite.WriteResources;
import gr.ilsp.nlp.commons.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

public class DedupMD5 {
	private static final Logger LOGGER = Logger.getLogger(DedupMD5.class);
	private static File input;
	private static File out_textfile;
	private static final String appHTMLext = ".html";
	private static final String appXMLHTMLext = ".xml.html";

	/**
	 * Gets files ending with input_type (cesDoc files are targeted) in the indirname directory,
	 * files in filesinPairs are excluded,
	 * finds (near) duplicates based on lists of quantized frequencies,
	 * discards the shortest document of each detected pair of (near) duplicates,
	 * generates lists of remaining cesDocs and their HTML transformations
	 * @param indirname
	 * @param filesinPairs
	 * @param outputfilename
	 * @param outputHTMLfilename
	 * @param applyOfflineXSLT
	 * @param inputtype 
	 */
	public static void dedup(File indirname, Set<String> filesinPairs, File outputfilename, File outputHTMLfilename,
			boolean applyOfflineXSLT, int MIN_TOKEN_LEN, final String input_type){

		input= indirname;
		if (!input.exists() || !input.isDirectory()){
			System.err.println( "the directory with the cesdoc files does not exist!!!!!!!!" );			
			System.exit(64);
		}
		out_textfile =outputfilename;
		
		List<File> files = DedupUtils.getTargetFiles(input,Constants.UNDERSCORE, input_type);
		if (files.size()<2){
			LOGGER.warn("The input list contains less than 2 files.");
			return;
		}else
			LOGGER.info(files.size()+" files will be processed.");
		
		long start = System.currentTimeMillis();
		HashMap<String, TextAttr> freqs = new HashMap<String, TextAttr>();
		String file_hashkey=Constants.EMPTY_STRING;
		int cents=0;
		String file_to_delete;
		for (int ii=0;ii<files.size();ii++){
			TextAttr t= DedupUtils.getTextAttrs(files.get(ii), MIN_TOKEN_LEN, input_type); //file under examination
			if (t==null)
				continue;
			LOGGER.debug(t.filename);
			LOGGER.debug(t.hashkeyText);
			file_hashkey = t.hashkeyText;
			if (filesinPairs!=null){ //files in pairs are excluded from comparison for deduplication
				if (filesinPairs.contains(files.get(ii).getName())){
					freqs.put(file_hashkey, t);
					continue;
				}
			}
			if (freqs.containsKey(file_hashkey)) { //file under examination is duplicate with an already checked file 
				file_to_delete=Constants.EMPTY_STRING;
				if (filesinPairs!=null){
					if (filesinPairs.contains(freqs.get(file_hashkey).filename)){ //file under examination will be deleted since its duplicate participates in a pair
						file_to_delete = FilenameUtils.concat(input.getPath(),t.filename);		
					}else{ //file under examination and its duplicate are not in pairs, the shorter will be deleted
						if (t.length>freqs.get(file_hashkey).length){ //file under examination is the longer
							freqs.put(file_hashkey, t);
							file_to_delete = FilenameUtils.concat(input.getPath(),freqs.get(file_hashkey).filename);
						}else{
							file_to_delete = FilenameUtils.concat(input.getPath(),t.filename);
						}
					}
				}else{ //no pairs exist
					if (t.length>freqs.get(file_hashkey).length){
						freqs.put(file_hashkey, t);
						file_to_delete = FilenameUtils.concat(input.getPath(),freqs.get(file_hashkey).filename);
					}else{
						file_to_delete = FilenameUtils.concat(input.getPath(),t.filename);
					}
				}
				if (!file_to_delete.isEmpty()){
					LOGGER.info(t.filename+ "\t\t" + freqs.get(file_hashkey).filename+ "\tDELETED "+ file_to_delete);
					(new File(file_to_delete)).delete();
					(new File(file_to_delete.replace(Constants.DOT+input_type,appHTMLext))).delete();
					(new File(file_to_delete.replace(Constants.DOT+input_type,appXMLHTMLext))).delete();
				}
			}else{
				freqs.put(file_hashkey, t);
			}
			if (ii/1000>cents){
				cents++;
				LOGGER.info("Lists for more than "+ cents*1000+" files have been checked.");
			}
		}

		String[] extensions=  {input_type};
		List<File> remFiles = (List<File>) FileUtils.listFiles(input, extensions, false);
		
		WriteResources.WriteTextList(remFiles, out_textfile);
		LOGGER.info("Created list of remaining cesDoc in "+ out_textfile.getAbsolutePath());
		if (applyOfflineXSLT){
			WriteResources.WriteHTMLList(remFiles, outputHTMLfilename);
			LOGGER.info("Created list of rendered remaining cesDoc in "+ outputHTMLfilename.getAbsolutePath());
		}
		long elapsedTime = System.currentTimeMillis()-start;
		LOGGER.info("Deduplication completed in " + elapsedTime + " milliseconds. "+ remFiles.size() +  " files remained.");
	}

	/**
	 * Gets files ending with input_type (cesDoc files are targeted) in the indirname directory,
	 * files in filesinPairs are excluded,
	 * finds (near) duplicates based on lists of quantized frequencies,
	 * discards the shortest document of each detected pair of (near) duplicates,
	 * generates lists of remaining cesDocs and their HTML transformations
	 * @param indirname
	 * @param filesinPairs
	 * @param outputfilename
	 * @param outputHTMLfilename
	 * @param applyOfflineXSLT
	 * @param inputtype 
	 */
	public static void dedupfull(File indirname, Set<String> filesinPairs, File outputfilename, File outputHTMLfilename,
			boolean applyOfflineXSLT, int MIN_TOKEN_LEN, final String input_type)  {
		
		input= indirname;
		if (!input.exists() || !input.isDirectory()){
			System.err.println( "the directory with the cesdoc files does not exist!!!!!!!!" );			
			System.exit(64);
		}
		out_textfile =outputfilename;

		String[] extensions=  {input_type};
		List<File> files = (List<File>) FileUtils.listFiles(input, extensions, false); //DedupUtils.getTargetFiles(input,Constants.UNDERSCORE, input_type);
		if (files.size()<2){
			LOGGER.warn("The input list contains less than 2 files.");
			return;
		}else
			LOGGER.info(files.size()+" files will be processed/examined.");

		long start = System.currentTimeMillis();
		List<TextFullAttr> fileattrs = new ArrayList<TextFullAttr>();
		
		int cents=0;
		String file_to_delete;
		List<String> found = new ArrayList<String>();
		for (int ii=0;ii<files.size();ii++){
			TextFullAttr t= DedupUtils.getTextFullAttrs(files.get(ii), MIN_TOKEN_LEN, input_type); //file under examination
			//if (t==null)
			//	continue;
			//LOGGER.debug(t.filename);
			fileattrs.add(t);
			if (t.length==0)
				found.add(files.get(ii).getName());
		}

		boolean found1 = false;
		for (int ii=0;ii<files.size()-1;ii++){
			TextFullAttr t1 = fileattrs.get(ii);
			if (found.contains(t1.filename))
				continue;
			String lang1 = t1.lang;
			Set<String> list1 = t1.textlist;
			found1 = false;
			for (int jj=ii+1;jj<files.size();jj++){
				TextFullAttr t2 = fileattrs.get(jj);
				if (found.contains(t2.filename))
					continue;
				String lang2 = t2.lang;
				Set<String> list2 = t2.textlist;
				if (lang1.equals(lang2)){
					Set<String> intersection = new HashSet<String>(list1);
					intersection.retainAll(list2);
					double ratio = (double)intersection.size()  / (double)Math.min(list2.size(),list1.size()) ;
					if (ratio>0.8){
						if (t1.length>t2.length){
							found.add(t2.filename);
							file_to_delete = FilenameUtils.concat(input.getPath(),t2.filename);
						}else{
							found.add(t1.filename);
							file_to_delete = FilenameUtils.concat(input.getPath(),t1.filename);
							found1 = true;
						}
						LOGGER.info("(near) duplicates identified: "+ t1.filename+ "\t\t" + t2.filename+ "\tDELETED "+ (new File(file_to_delete)).getName());
						/*if (!file_to_delete.isEmpty()){
							(new File(file_to_delete)).delete();
							(new File(file_to_delete.replace(Constants.DOT+input_type,appHTMLext))).delete();
							(new File(file_to_delete.replace(Constants.DOT+input_type,appXMLHTMLext))).delete();
						}*/
						file_to_delete=Constants.EMPTY_STRING;
						if (found1){
							found1=false;
							break;
						}
					}
				}
			}
			if (ii/1000>cents){
				cents++;
				LOGGER.info("Lists for more than "+ cents*1000+" files have been checked.");
			}
		}
		for (int ii=0;ii<found.size();ii++){
			file_to_delete = FilenameUtils.concat(input.getPath(),found.get(ii));
			(new File(file_to_delete)).delete();
			(new File(file_to_delete.replace(Constants.DOT+input_type,appHTMLext))).delete();
			(new File(file_to_delete.replace(Constants.DOT+input_type,appXMLHTMLext))).delete();
		}
				
		List<File> remFiles = (List<File>) FileUtils.listFiles(input, extensions, false);
		if (out_textfile!=null){
			WriteResources.WriteTextList(remFiles, out_textfile);
			LOGGER.info("Created list of remaining cesDoc in "+ out_textfile.getAbsolutePath());
			if (applyOfflineXSLT){
				WriteResources.WriteHTMLList(remFiles, outputHTMLfilename);
				LOGGER.info("Created list of rendered remaining cesDoc in "+ outputHTMLfilename.getAbsolutePath());
			}
		}
		long elapsedTime = System.currentTimeMillis()-start;
		LOGGER.info("Deduplication completed in " + elapsedTime + " milliseconds. "+ remFiles.size() +  " files remained.");
	}
	
}
