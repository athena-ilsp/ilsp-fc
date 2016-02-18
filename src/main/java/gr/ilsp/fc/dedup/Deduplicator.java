package gr.ilsp.fc.dedup;

import java.io.File;
import java.util.Set;

import org.apache.log4j.Logger;

public class Deduplicator {
	private static final Logger LOGGER = Logger.getLogger(Deduplicator.class);
	private static String method;
	private static File targetDir;
	private static File outTextList;
	private static String inputType;
	private static File outHTMLList;
	private static Set<String> excludeSetFiles;
	private static DeduplicatorOptions options = null;
	private static double IntersectionThr;
	private static int MIN_PAR_LEN;
	private static int MIN_TOK_LEN;
	private static boolean applyXSLT;

	public static void main(String[] args) {
		Deduplicator ded = new Deduplicator();
		options = new DeduplicatorOptions();
		options.parseOptions(args);
		ded.setTargetDir(options.getTargetDir());
		ded.setOutTextList(options.getOutTextList());
		ded.setOutHTMLList(options.getOutHTMLList());
		ded.setExcludeSetFiles(options.getListExcludeFiles());
		ded.setMethod(options.getMethod());
		ded.setIntersectionThr(options.getInterThr());
		ded.setMIN_PAR_LEN(options.getMinParLen());
		ded.setMIN_TOK_LEN(options.getMinTokLen());
		ded.setApplyXSLT(options.getApplyXSLT());
		ded.setInputType(options.getInputType());
		ded.nearDedup();
	}
	/**
	 * apply near Deduplication
	 */
	public void nearDedup(){
		if (method.equals("1")){
			LOGGER.info("Deduplication by using lists and MD5 method.");
			DedupMD5.dedup(targetDir, excludeSetFiles, outTextList,outHTMLList,applyXSLT, MIN_TOK_LEN,inputType);
		}
		if (method.equals("2")){
			LOGGER.info("Deduplication based on common paragraphs.");
			DedupParsMD5.dedup(targetDir, excludeSetFiles, outTextList,outHTMLList,applyXSLT, MIN_PAR_LEN, IntersectionThr,inputType);
		}
		if (method.equals("0")){
			LOGGER.info("Deduplication by using lists and MD5 method.");
			DedupMD5.dedup(targetDir, excludeSetFiles,  outTextList,outHTMLList,applyXSLT, MIN_TOK_LEN,inputType);
			LOGGER.info("Deduplication based on common paragraphs.");
			DedupParsMD5.dedup(targetDir, excludeSetFiles,  outTextList,outHTMLList,applyXSLT, MIN_PAR_LEN, IntersectionThr,inputType);
		}
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
	 * Tokens with less than MIN_TOK_LEN (default is 3) are excluded from content
	 * @param min_tok_len
	 */
	public void setMIN_TOK_LEN(int min_tok_len) {
		Deduplicator.MIN_TOK_LEN = min_tok_len;
	}
	/**
	 * cesDocFiles in this directory will be examined
	 * @param targetDir
	 */
	public void setTargetDir(File targetDir ){
		Deduplicator.targetDir = targetDir;
	}
	/**
	 * textfile with list of paths of the remained cesDocFiles
	 * @param outTextList
	 */
	public void setOutTextList(File outTextList) {
		Deduplicator.outTextList= outTextList;
	}
	/**
	 * HTML file with list of links pointing to the remained cesDocFiles
	 * @param outHTMLList
	 */
	public void setOutHTMLList(File outHTMLList) {
		Deduplicator.outHTMLList= outHTMLList;
	}
	/**
	 * cesDocFiles to be excluded for deduplication
	 * @param excludeFiles
	 */
	public void setExcludeSetFiles(Set<String> excludeFiles){
		Deduplicator.excludeSetFiles = excludeFiles;
	}
	/**
	 * Documents for which the ratio between the number of common paragraphs and the shortest
	 *  of these documents is more than this threshold, are considered duplicates"
	 * @param interThr
	 */
	public void setIntersectionThr(double interThr){
		Deduplicator.IntersectionThr = interThr;
	}
	/**
	 * cesDocFiles have been XSLT transformed
	 * @param applyXSLT
	 */
	public void setApplyXSLT(boolean applyXSLT){
		Deduplicator.applyXSLT = applyXSLT;
	}
	
	public void setInputType(String inputType ){
		Deduplicator.inputType = inputType;
	}
}
