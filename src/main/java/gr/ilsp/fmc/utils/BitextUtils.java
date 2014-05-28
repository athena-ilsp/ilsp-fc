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
					LOGGER.debug("Deleting redundant " +file);
					file.delete();					
				}
			}
		} else {
			LOGGER.warn(parentDir + " is not a directory.");
		}
	}
	
	/**
	 * 
	 * <B>Use carefully.</B>
	 * 
	 * Deletes all tempfiles (txt files containing the fingerprints of cesDoc files) of a bilingual crawl.
	 * All files end with (tempfileext) are deleted.
	 * 
	 * @param parentDir
	 * @param tempfileext
	 */
	
	public static void removeTempFiles(File parentDir, final String tempfileext) {
		if (parentDir.isDirectory()) {			
			List<File> files = Arrays.asList(parentDir.listFiles());
			for (File file: files) {
				if (file.isDirectory() || file.getName().contains(UNDERSCORE)) { // Skip directories and cesAlignFiles
					continue;
				}
				if (file.getName().endsWith(tempfileext))
					file.delete();
			}
		} else 
			LOGGER.warn(parentDir + " is not a directory.");
	}
	
	public static void main(String[] args) throws IOException {
		ArrayList<String[]> bitextStruct= new ArrayList<String[]>();
		bitextStruct.add(new String[] {"127", "75", "", "", ""});
		bitextStruct.add(new String[] {"87", "140", "", "", ""});
		removeRedundantFiles(new File("/tmp/new3"), bitextStruct);
	}

	
}
