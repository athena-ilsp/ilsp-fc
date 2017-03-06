package gr.ilsp.fc.bitext;

import gr.ilsp.fc.utils.FcFileUtils;
import gr.ilsp.fc.utils.ISOLangCodes;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * PairDetector detects pairs of parallel documents based on website graph, specific patterns in URLs,
 * occurrences of common images, similarity of sequences of digits and similarity of structure.
 * For each detected pair, a cesAlign and its xls transformation (if asked) are generated.
 * The basename of a cesAling file consists of the basenames of the paired documents and the identifier of the method
 * which provided this pair (e.g. eng-12_ell-18_x.xml, where x stands for a, u, p, i, d, and h denoting the method used to identify of the pair).
 * This file holds references to the paired documents. It also creates a text file with a list of the fullpaths of the generated cesAlign files
 * and (if asked) an HTML file with links pointing to the xls transformations is generated too.
 * @author vpapa
 *
 */
public class PairDetector {
	private static final Logger LOGGER = Logger.getLogger(PairDetector.class);
	private static PairDetectorOptions options = null;
	private static String[] languages;
	private static File indir;
	private static File outdir;
	private static File outTextList;
	private static File outHTMLList;
	private static File outBaseName;
	private static File groundTruth;
	private static String methods;
	private static boolean useImagePath=false;
	private static boolean offlineXSLT=false;
	private static String[][] urlReplaces;
	private static Map<String, String> excludeSetFiles =null;
	private static String SEMI_SEPAR = ";";
	private static final String UNDERSCORE_STR = "_";
	private static final String HYPHEN_STR = "-";
	private static final String tempFileExt = ".xml.txt";
	private static final String SEMICOLON_STR=";";
	private static final String XMLlist = ".xmllist.txt";
	private static final String XMLHTMLlist = ".xmllist.html";

	private static boolean delFiles = false;	

	public static void main(String[] args) {
		PairDetector pd = new PairDetector();
		options = new PairDetectorOptions();
		options.parseOptions(args);
		Map<String,Integer> numInLangPairMap = new HashMap<String,Integer>();	//number of document pairs in each language pair
		List<String> langpairs = options.getLangPairs();
		for (String langpair:langpairs){
			pd.setLanguage(langpair);
			pd.setSourceDir(options.getInDir());
			pd.setTargetDir(options.getOutDir());
			pd.setBaseName(options.getBaseName());
			pd.setExcludeSetFiles(null);
			pd.setUseImagepath(options.getImpath());
			pd.setApplyXSLT(options.isOfflineXSLT());
			pd.setURL_REPL(options.getUrlReplaces());
			pd.setMethods(options.getMethods());
			pd.setDelFiles(options.getDelFiles());
			pd.setGroundTruth(options.getGroundTruth());
			int numOfPairs=pd.pairDetect();
			numInLangPairMap.put(langpair, numOfPairs);
			String[] langs = langpair.split(SEMICOLON_STR); 
			LOGGER.info("Language Pair: "+ langs[0]+ HYPHEN_STR+langs[1]+ " Number of document pairs is: "+numOfPairs);
		}
	}
	/**
	 * Identifies pairs of cesDoc files for a language pair. In each document pair, the two documents are candidate translations of each other.
	 * Generates a cesAlign file (and its transformation to HTML if asked) for each identified pair.
	 * Writes a text file (and its transformation to HTML if asked) containing a list of path of the generated cesAlign files.
	 * @return
	 */
	public int pairDetect() {
		for (int ii=0;ii<languages.length;ii++){
			languages[ii] = ISOLangCodes.get3LetterCode(languages[ii]);
		}
		if (languages.length!=2){
			LOGGER.info("exact two languages required");
			System.exit(0);
		}
		String langpair = languages[0]+HYPHEN_STR+languages[1];
		LOGGER.info("------------Detection of pairs of parallel documents for "+ langpair+"------------");
		langpair = UNDERSCORE_STR+langpair;
		ArrayList<String[]> bitextsALL = Bitexts.findPairsUIDS(indir, methods, languages, excludeSetFiles 
				, outdir.getAbsolutePath(), urlReplaces, offlineXSLT, useImagePath, groundTruth);
		outTextList = new File(outBaseName.getAbsolutePath()+langpair+XMLlist);
		if (offlineXSLT)
			outHTMLList = new File(outBaseName.getAbsolutePath()+langpair+XMLHTMLlist);
		if (bitextsALL!=null && !bitextsALL.isEmpty() ){
			LOGGER.info("Total pairs found: "+ bitextsALL.size());
			WriteBitexts.writeOutList(outdir,outTextList,outHTMLList,bitextsALL);
			LOGGER.info("Created list of cesAlign files in "+ outTextList.getAbsolutePath());
			if (outHTMLList!=null)
				LOGGER.info("Created list of rendered cesAling files in "+ outHTMLList.getAbsolutePath());
		}else{
			LOGGER.info("No pairs found");
		}
		File[] f=indir.listFiles();
		if (f!=null){
			List<File> filelist = Arrays.asList(indir.listFiles());
			FcFileUtils.removeFiles(filelist,tempFileExt);
		}
		if (delFiles)
			BitextUtils.removeRedundantFiles(indir,bitextsALL);

		//FcFileUtils.moveZipDeleteFiles(indir,html, Arrays.asList(htmlExt, pdfExt), UNDERSCORE_STR, false);
		//if (!offlineXSLT)
		//	FcFileUtils.moveZipDeleteFiles(indir,transCes, Arrays.asList(transCesExt), UNDERSCORE_STR, true);
		if (bitextsALL!=null && !bitextsALL.isEmpty() )
			return bitextsALL.size(); //LOGGER.info("document pairs in "+lang.substring(1)+"\t"+bitextsALL.size());
		else
			return 0; //LOGGER.info("");
	}

	public void setLanguage(String languages) {
		PairDetector.languages = languages.split(SEMI_SEPAR);
	}
	public void setSourceDir(File inDir) {
		PairDetector.indir = inDir;
	}
	public void setTargetDir(File outDir) {
		PairDetector.outdir = outDir;
	}
	public File getTargetDir() {
		return outdir;
	}
	/**
	 * sets the basename of all output files (lists) 
	 * @param outBaseName
	 */
	public void setBaseName(File outBaseName) {
		PairDetector.outBaseName = outBaseName;
	}
	public void setGroundTruth(File gt) {
		PairDetector.groundTruth = gt;
	}
	public void  setExcludeSetFiles(Map<String, String> excludeSetFiles){
		PairDetector.excludeSetFiles = excludeSetFiles;
	}
	/**
	 * if true, full paths of images in HTML are extracted
	 * if false (default), filenames of images in HTML are extracted 
	 * @param useImagePath
	 */
	public void  setUseImagepath(boolean useImagePath){
		PairDetector.useImagePath = useImagePath;
	}
	/**
	 * Apply an xsl transformation to generate html files during exporting
	 * @param offlineXSLT
	 */
	public void  setApplyXSLT(boolean offlineXSLT){
		PairDetector.offlineXSLT = offlineXSLT;
	}
	/**
	 * Special pairs of patterns to match URLs of candidate translations (webpages).
	 * Use ;; to separate pairs and ; to separate the parts of each pair.
	 * @param urlReplaces
	 */
	public void setURL_REPL(String[][] urlReplaces) {
		PairDetector.urlReplaces = urlReplaces;
	}
	/**
	 * sets methods for pair detection: "a" (links between documents), "u" (patterns in urls), "p" (common images and similar digit sequences),"
	 * "i" (common images), "d" (similar digit sequences), "u" (high similarity of html structure), "m" (medium similarity of html structure)", 
	 * "l" (low similarity of html structure)"
	 * Default value is "aupdih". 
	 * @param methods
	 */
	public void setMethods(String methods) {
		PairDetector.methods = methods;
	}
	/**
	 * Use it very carefully. If true, the documents that have not been involved into document pairs are deleted! 
	 * @param delFiles
	 */
	public void setDelFiles(boolean delFiles) {
		PairDetector.delFiles = delFiles;
	}
	
}
