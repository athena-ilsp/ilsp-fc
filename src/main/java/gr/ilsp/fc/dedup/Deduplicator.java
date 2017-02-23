package gr.ilsp.fc.dedup;

import java.io.File;
import java.util.Set;

import org.apache.log4j.Logger;

public class Deduplicator {
	private static final Logger LOGGER = Logger.getLogger(Deduplicator.class);
	private static String method;
	private static File targetDir;
	private static File outTextList;
	private static File outBaseName;
	private static String inputType;
	private static File outHTMLList;
	private static Set<String> excludeSetFiles;
	private static DeduplicatorOptions options = null;
	private static boolean applyOfflineXSLT=false;
	private static double IntersectionThr;
	private static int MIN_PAR_LEN;
	private static int MIN_TOK_LEN;
	private static final String XMLlist = ".xmllist.txt";
	private static final String XMLHTMLlist = ".xmllist.html";
	
	public static void main(String[] args) {
		Deduplicator ded = new Deduplicator();
		options = new DeduplicatorOptions();
		options.parseOptions(args);
		ded.setTargetDir(options.getTargetDir());
		ded.setBaseName(options.getBaseName());
		ded.setExcludeSetFiles(options.getListExcludeFiles());
		ded.setMethod(options.getMethod());
		ded.setIntersectionThr(options.getInterThr());
		ded.setMIN_PAR_LEN(options.getMinParLen());
		ded.setMIN_TOK_LEN(options.getMinTokLen());
		ded.setApplyOfflineXSLT(options.applyOfflineXSLT());
		ded.setInputType(options.getInputType());
		ded.nearDedup();
	}
	/**
	 * Goes through the targeted directory, processes the files of type inputType (e.g. xml, or txt), detects (near) duplicates and keeps only the longest of each group of duplicates 
	 * Generates a text file (and its transformation to HTML if asked) with a list of paths of the remaining cesDoc files
	 */
	public void nearDedup(){
		long startTime = System.currentTimeMillis();
		//outTextList = new File(outBaseName.getAbsolutePath()+DEDUP+XMLlist);
		outTextList = new File(outBaseName.getAbsolutePath()+XMLlist);
		if (applyOfflineXSLT)
			outHTMLList = new File(outBaseName.getAbsolutePath()+XMLHTMLlist);
			//outHTMLList = new File(outBaseName.getAbsolutePath()+DEDUP+XMLHTMLlist);
		if (method.equals("1")){
			LOGGER.info("------------Deduplication by using lists and MD5 method.------------");
			DedupMD5.dedup(targetDir, excludeSetFiles, outTextList,outHTMLList,applyOfflineXSLT, MIN_TOK_LEN,inputType);
		}
		if (method.equals("2")){
			LOGGER.info("------------Deduplication based on common paragraphs.------------");
			DedupParsMD5.dedup(targetDir, excludeSetFiles, outTextList,outHTMLList,applyOfflineXSLT, MIN_PAR_LEN, IntersectionThr,inputType);
		}
		if (method.equals("0")){
			LOGGER.info("------------Deduplication by using lists and MD5 method.------------");
			DedupMD5.dedup(targetDir, excludeSetFiles,  outTextList,outHTMLList,applyOfflineXSLT, MIN_TOK_LEN,inputType);
			LOGGER.info("------------Deduplication based on common paragraphs.------------");
			DedupParsMD5.dedup(targetDir, excludeSetFiles,  outTextList,outHTMLList,applyOfflineXSLT, MIN_PAR_LEN, IntersectionThr,inputType);
		}
		LOGGER.info("Completed in " + (System.currentTimeMillis()-startTime) + " milliseconds.");
	}
	/**
	 * Method type for deduplication: 
	 * 1 for Deduplication by using lists and MD5 method.
	 * 2 for Deduplication based on common paragraphs.
	 * 0 for applying both methods.
	 * @param method
	 */
	public void setMethod(String method){
		Deduplicator.method = method;
	}
	/**
	 * Paragraphs with less than MIN_PAR_LEN (default is 3) tokens are excluded from content
	 * @param minParLen
	 */
	public void setMIN_PAR_LEN(int minParLen) {
		Deduplicator.MIN_PAR_LEN = minParLen;
	}
	/**
	 * Tokens with less than MIN_TOK_LEN (default is 3) letters are excluded from content
	 * @param min_tok_len
	 */
	public void setMIN_TOK_LEN(int min_tok_len) {
		Deduplicator.MIN_TOK_LEN = min_tok_len;
	}
	/**
	 * files in this directory will be examined
	 * @param targetDir
	 */
	public void setTargetDir(File targetDir ){
		Deduplicator.targetDir = targetDir;
	}
	public File getTargetDir( ){
		return targetDir;
	}
	/**
	 * outBaseName of output files (list of paths, or/and links)
	 * @param outBaseName
	 */
	public void setBaseName(File outBaseName) {
		Deduplicator.outBaseName= outBaseName;
	}
	/**
	 * files to be excluded for deduplication
	 * @param excludeFiles
	 */
	public void setExcludeSetFiles(Set<String> excludeFiles){
		Deduplicator.excludeSetFiles = excludeFiles;
	}
	/**
	 * Documents for which the ratio between the number of common paragraphs and the shortest
	 *  of these documents is more than this threshold, are considered "duplicates"
	 * @param interThr
	 */
	public void setIntersectionThr(double interThr){
		Deduplicator.IntersectionThr = interThr;
	}
	/**
	 * cesDocFiles have been XSLT transformed
	 * @param applyXSLT
	 */
	public void setApplyOfflineXSLT(boolean applyXSLT){
		Deduplicator.applyOfflineXSLT = applyXSLT;
	}
	/**
	 * files with filename ending to inputType will be processed
	 * @param inputType
	 */
	public void setInputType(String inputType ){
		Deduplicator.inputType = inputType;
	}
}
