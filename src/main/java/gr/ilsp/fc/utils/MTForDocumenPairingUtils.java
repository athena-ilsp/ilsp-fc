package gr.ilsp.fc.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utils class to handle MTForDocumenPairingUtils
 * 
 * @author prokopis
 *
 */
public class MTForDocumenPairingUtils {

	private static final Logger logger = LoggerFactory.getLogger(MTForDocumenPairingUtils.class);

	public static void main(String[] args) throws IOException {
		
		File topHtmlDir = new File(args[0]).getAbsoluteFile();
		List<File> htmlDirs = Arrays.asList(topHtmlDir.listFiles((FileFilter) FileFilterUtils.directoryFileFilter()));
		for (File htmlDir : htmlDirs) {
			// Test dir. Remove to process all dirs.
			if (!htmlDir.getName().contains("ledindon.com") ) {
				continue;
			}

			//String basename = StringUtils.replacePattern(htmlDir.getName(), "\\.lett$", "");
			File outF = new File(htmlDir.getParent(), "out_" + htmlDir.getName());
			File listF = new File(htmlDir.getParent(), "list_" + htmlDir.getName());
			File URLListF = new File(htmlDir.getParent(), "URLList_" + htmlDir.getName());
			File mtTextDir = new File(htmlDir.getParentFile().getParentFile(), FilenameUtils.concat("lett.test.mt.txt" , "www.ledindon.com.lett")); 

			logger.info("Examining web domain dir: " + htmlDir);
			logger.info("Out list: " + outF.getName());
			logger.info("List: " + listF.getName());
			logger.info("URLList: " + URLListF.getName());
			logger.info("MT dir: " + mtTextDir.getAbsolutePath());
			
			String l1 = "en"; //String l2 = "fr";
			String mtl2l1 = "mt-en-fr";
			String mtPrefix = "mt-en-";

			// Get an initial list of files to compare
			Collection<File> txtFiles = FileUtils.listFiles(mtTextDir, new WildcardFileFilter(l1+"*txt"), null);
			txtFiles.addAll(FileUtils.listFiles(mtTextDir, new WildcardFileFilter(mtl2l1+"*txt"), null));
			
			Map<String, String> pairedFilesMethodsMap = getAlreadyPairedFiles(outF);
			//List<String> trustMethods = new ArrayList<String>(Arrays.asList("aupdi".split("")));
			List<String> trustMethods = new ArrayList<String>(Arrays.asList("pdi".split("")));
			
			// Filter out all files of pairs generating from specific methods. A horror story with wolves. 
			txtFiles = filterOutTxtFiles(txtFiles, trustMethods, pairedFilesMethodsMap, mtPrefix);
			
			for (File txtFile : txtFiles) {
				logger.info("Examining " + txtFile);
//				if (xmlFile.getName().startsWith("en")) {
//					File txtFile = new File(xmlFile.getParentFile(), FilenameUtils.getBaseName(xmlFile.getAbsolutePath())+".txt"); 
//					cesDoc2Text(xmlFile, txtFile);
//				}
			}
			
			System.out.println();
		}
	}

	private static Collection<File> filterOutTxtFiles(Collection<File> txtFiles, List<String> trustMethods, Map<String, String> pairedFilesMethodsMap, String mtprefix) {
		Collection<File> filteredTxtFiles = new HashSet<File>();
		for (File file: txtFiles) {
			String basename = StringUtils.replacePattern(FilenameUtils.getBaseName(file.getName()), mtprefix, "");
			String method = pairedFilesMethodsMap.get(basename);
			if (!trustMethods.contains(method)) {
				filteredTxtFiles.add(file);
			} else {
				logger.info(file.getName() + " found with " + method);
			}
		}
		return filteredTxtFiles;
	}

	private static Map<String, String> getAlreadyPairedFiles(File outF) throws IOException {
		List<String> lines = FileUtils.readLines(outF);
		Map<String, String> pairedFilesMethodsMap = new HashMap<String, String>();
		for (String line: lines ) {
			String pair[] = FilenameUtils.getBaseName(new File(line.trim()).getName()).split("_");
			pairedFilesMethodsMap.put(pair[0], pair[2]);
			pairedFilesMethodsMap.put(pair[1], pair[2]);
		}
		return pairedFilesMethodsMap;
	}

	public static void cesDoc2Text(File xmlFile, File txtFile) throws IOException {
		Document doc = Jsoup.parse(xmlFile, "UTF-8");
		Elements ps = doc.select("p");
		FileUtils.write(txtFile, "", "UTF-8", false);
		for (Element p: ps) {		
			FileUtils.write(txtFile, p.text()+"\n", "UTF-8", true);
		}
		FileUtils.write(txtFile, "\n", "UTF-8", true);
	}

}
