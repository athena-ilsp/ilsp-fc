package gr.ilsp.fmc.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

public class BitextUtils {
	
	private static final String UNDERSCORE = "_";
	private static final Logger LOGGER = Logger.getLogger(BitextUtils.class);

	/**
	 * 
	 * <B>Use carefully.</B>
	 * 
	 * Deletes all redundant files of a bilingual crawl. All files whose id is not
	 * contained in one of the first two entries in a bitextStructure entry are
	 * deleted.
	 * 
	 * Usage:
	 * 
	 * <pre>
	 * {@code
	 * ArrayList<String[]> bitextStruct= new ArrayList<String[]>();
	 * bitextStruct.add(new String[] {"127", "75", "", "", ""});
	 * bitextStruct.add(new String[] {"128", "76", "", "", ""});
	 * ...
	 * removeRedundantFiles(new File("/data/dirWithCrawlFiles"), bitextStruct);
	 * </pre>
	 *
	 * @param parentDir
	 * @param bitextStructure
	 * @throws IOException
	 */
	public static void removeRedundantFiles(File parentDir, ArrayList<String[]> bitextStructure) throws IOException {
		if (parentDir.isDirectory()) {			
			// First collect all ids we want to keep.
			List<String> ids=  new ArrayList<String>();
			for (String[] bitext : bitextStructure) {
				ids.add(bitext[0]); ids.add(bitext[1]);
			}			
			List<File> files = Arrays.asList(parentDir.listFiles());
			for (File file: files) {
				if (file.isDirectory() || file.getName().contains(UNDERSCORE)) { // Skip directories and cesAlignFiles
					continue;
				}
				String basename = FilenameUtils.getBaseName(file.getName()); // Get the basename
				while (!FilenameUtils.getBaseName(basename).equals(basename)) { // Get the real basename
					basename = FilenameUtils.getBaseName(basename);
				}
				
				if (!ids.contains(basename)) { // Finally delete all redundant files
					LOGGER.info("Deleting redundant " +file);
					file.delete();					
				}
			}
		} else {
			LOGGER.warn(parentDir + " is not a directory.");
		}
	}
	
	public static void main(String[] args) throws IOException {
		ArrayList<String[]> bitextStruct= new ArrayList<String[]>();
		bitextStruct.add(new String[] {"127", "75", "", "", ""});
		bitextStruct.add(new String[] {"128", "76", "", "", ""});
		bitextStruct.add(new String[] {"142", "88", "", "", ""});
		bitextStruct.add(new String[] {"143", "90", "", "", ""});
		bitextStruct.add(new String[] {"144", "91", "", "", ""});
		bitextStruct.add(new String[] {"145", "92", "", "", ""});
		bitextStruct.add(new String[] {"146", "93", "", "", ""});
		bitextStruct.add(new String[] {"147", "94", "", "", ""});
		bitextStruct.add(new String[] {"148", "95", "", "", ""});
		bitextStruct.add(new String[] {"150", "97", "", "", ""});
		bitextStruct.add(new String[] {"151", "98", "", "", ""});
		bitextStruct.add(new String[] {"152", "99", "", "", ""});
		bitextStruct.add(new String[] {"153", "100", "", "", ""});
		bitextStruct.add(new String[] {"154", "101", "", "", ""});
		bitextStruct.add(new String[] {"155", "102", "", "", ""});
		bitextStruct.add(new String[] {"156", "103", "", "", ""});
		bitextStruct.add(new String[] {"157", "104", "", "", ""});
		bitextStruct.add(new String[] {"158", "105", "", "", ""});
		bitextStruct.add(new String[] {"159", "107", "", "", ""});
		bitextStruct.add(new String[] {"160", "108", "", "", ""});
		bitextStruct.add(new String[] {"161", "109", "", "", ""});
		bitextStruct.add(new String[] {"163", "111", "", "", ""});
		bitextStruct.add(new String[] {"164", "112", "", "", ""});
		bitextStruct.add(new String[] {"16", "44", "", "", ""});
		bitextStruct.add(new String[] {"166", "114", "", "", ""});
		bitextStruct.add(new String[] {"169", "115", "", "", ""});
		bitextStruct.add(new String[] {"170", "78", "", "", ""});
		bitextStruct.add(new String[] {"171", "117", "", "", ""});
		bitextStruct.add(new String[] {"172", "118", "", "", ""});
		bitextStruct.add(new String[] {"173", "119", "", "", ""});
		bitextStruct.add(new String[] {"17", "45", "", "", ""});
		bitextStruct.add(new String[] {"176", "120", "", "", ""});
		bitextStruct.add(new String[] {"177", "121", "", "", ""});
		bitextStruct.add(new String[] {"18", "46", "", "", ""});
		bitextStruct.add(new String[] {"185", "191", "", "", ""});
		bitextStruct.add(new String[] {"194", "193", "", "", ""});
		bitextStruct.add(new String[] {"195", "192", "", "", ""});
		bitextStruct.add(new String[] {"197", "199", "", "", ""});
		bitextStruct.add(new String[] {"201", "198", "", "", ""});
		bitextStruct.add(new String[] {"204", "210", "", "", ""});
		bitextStruct.add(new String[] {"20", "48", "", "", ""});
		bitextStruct.add(new String[] {"205", "211", "", "", ""});
		bitextStruct.add(new String[] {"206", "209", "", "", ""});
		bitextStruct.add(new String[] {"208", "196", "", "", ""});
		bitextStruct.add(new String[] {"21", "49", "", "", ""});
		bitextStruct.add(new String[] {"215", "213", "", "", ""});
		bitextStruct.add(new String[] {"219", "218", "", "", ""});
		bitextStruct.add(new String[] {"22", "50", "", "", ""});
		bitextStruct.add(new String[] {"226", "228", "", "", ""});
		bitextStruct.add(new String[] {"227", "229", "", "", ""});
		bitextStruct.add(new String[] {"230", "232", "", "", ""});
		bitextStruct.add(new String[] {"23", "51", "", "", ""});
		bitextStruct.add(new String[] {"243", "250", "", "", ""});
		bitextStruct.add(new String[] {"244", "251", "", "", ""});
		bitextStruct.add(new String[] {"245", "253", "", "", ""});
		bitextStruct.add(new String[] {"24", "52", "", "", ""});
		bitextStruct.add(new String[] {"248", "247", "", "", ""});
		bitextStruct.add(new String[] {"254", "256", "", "", ""});
		bitextStruct.add(new String[] {"25", "55", "", "", ""});
		bitextStruct.add(new String[] {"262", "264", "", "", ""});
		bitextStruct.add(new String[] {"263", "265", "", "", ""});
		bitextStruct.add(new String[] {"27", "57", "", "", ""});
		bitextStruct.add(new String[] {"29", "56", "", "", ""});
		bitextStruct.add(new String[] {"30", "60", "", "", ""});
		bitextStruct.add(new String[] {"32", "89", "", "", ""});
		bitextStruct.add(new String[] {"3", "31", "", "", ""});
		bitextStruct.add(new String[] {"33", "5", "", "", ""});
		bitextStruct.add(new String[] {"35", "7", "", "", ""});
		bitextStruct.add(new String[] {"37", "9", "", "", ""});
		bitextStruct.add(new String[] {"38", "10", "", "", ""});
		bitextStruct.add(new String[] {"39", "11", "", "", ""});
		bitextStruct.add(new String[] {"40", "12", "", "", ""});
		bitextStruct.add(new String[] {"41", "13", "", "", ""});
		bitextStruct.add(new String[] {"42", "14", "", "", ""});
		bitextStruct.add(new String[] {"62", "61", "", "", ""});
		bitextStruct.add(new String[] {"64", "65", "", "", ""});
		bitextStruct.add(new String[] {"79", "130", "", "", ""});
		bitextStruct.add(new String[] {"80", "136", "", "", ""});
		bitextStruct.add(new String[] {"81", "132", "", "", ""});
		bitextStruct.add(new String[] {"82", "133", "", "", ""});
		bitextStruct.add(new String[] {"84", "137", "", "", ""});
		bitextStruct.add(new String[] {"85", "138", "", "", ""});
		bitextStruct.add(new String[] {"86", "139", "", "", ""});
		bitextStruct.add(new String[] {"87", "140", "", "", ""});
		removeRedundantFiles(new File("/tmp/new3"), bitextStruct);
	}

	
}
