package gr.ilsp.fc.bitext;

import gr.ilsp.fc.utils.FcFileUtils;
import gr.ilsp.fc.utils.ISOLangCodes;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

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
	//private static final String transCesExt = ".xml.html";
	private static final String XMLlist = ".xmllist.txt";
	private static final String XMLHTMLlist = ".xmllist.html";
	//private static final String pdfExt=".pdf";
	//private static final String htmlExt = ".html";
	//private static final String html = "html";
	//private static final String transCes = "xml.html";
	private static boolean delFiles = false;	

	public static void main(String[] args) {
		PairDetector pd = new PairDetector();
		options = new PairDetectorOptions();
		options.parseOptions(args);
		pd.setLanguage(options.getLanguage());
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
		pd.pairDetect();
	}

	public String pairDetect() {
		for (int ii=0;ii<languages.length;ii++){
			languages[ii] = ISOLangCodes.get3LetterCode(languages[ii]);
		}
		if (languages.length>2){
			LOGGER.info("exact two languages required");
			System.exit(0);
		}
		String lang = languages[0]+HYPHEN_STR+languages[1];
		LOGGER.info("------------Detection of pairs of parallel documents for "+ lang+"------------");
		lang = UNDERSCORE_STR+lang;
		ArrayList<String[]> bitextsALL = Bitexts.findPairsUIDS(indir, methods, languages, excludeSetFiles 
				, outdir.getAbsolutePath(), urlReplaces, offlineXSLT, useImagePath, groundTruth);
		outTextList = new File(outBaseName.getAbsolutePath()+lang+XMLlist);
		if (offlineXSLT)
			outHTMLList = new File(outBaseName.getAbsolutePath()+lang+XMLHTMLlist);
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
			return "document pairs in "+lang.substring(1)+"\t"+bitextsALL.size();
		else
			return "";
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
	public void setBaseName(File outBaseName) {
		PairDetector.outBaseName = outBaseName;
	}
	public void setGroundTruth(File gt) {
		PairDetector.groundTruth = gt;
	}
	public void  setExcludeSetFiles(Map<String, String> excludeSetFiles){
		PairDetector.excludeSetFiles = excludeSetFiles;
	}
	public void  setUseImagepath(boolean useImagePath){
		PairDetector.useImagePath = useImagePath;
	}
	public void  setApplyXSLT(boolean offlineXSLT){
		PairDetector.offlineXSLT = offlineXSLT;
	}
	public void setURL_REPL(String[][] urlReplaces) {
		PairDetector.urlReplaces = urlReplaces;
	}
	public void setMethods(String methods) {
		PairDetector.methods = methods;
	}
	public void setDelFiles(boolean delFiles) {
		PairDetector.delFiles = delFiles;
	}
}
