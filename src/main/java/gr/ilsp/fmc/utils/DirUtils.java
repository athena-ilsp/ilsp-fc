package gr.ilsp.fmc.utils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

public class DirUtils {
	private static final Logger LOGGER = Logger.getLogger(DirUtils.class);
	
	@SuppressWarnings("deprecation")
	public static void clearPreviousLoopDir(FileSystem fs, Path outputPath, int curLoop) {
		Path dir;
		Path crawldb;
		String dirName = "";
		int index = 0;
		try {
			FileStatus[] files = fs.listStatus(outputPath);
			for (FileStatus status: files){
				if (!status.isDir()) continue;
				dir = status.getPath();
				dirName = dir.getName();
				try {
					index = Integer.parseInt(dirName.split("-")[0]);
				} catch (NumberFormatException e){
					continue;
				}
				crawldb = new Path(dir,CrawlConfig.CRAWLDB_SUBDIR_NAME);
				if (index == curLoop-2 && fs.exists(crawldb)){
					fs.delete(crawldb);
					return;
				}				
			}
		} catch (IOException e) {
			LOGGER.info(e.getMessage());
		}
	}
	
	public static File createTempDir() {
	    final String baseTempPath = System.getProperty("java.io.tmpdir");

	    File tempDir = new File(baseTempPath + File.separator + "ilsp-fc-tempDir" + getRandomInt());
	    if (tempDir.exists() == false) {
	        tempDir.mkdir();
	    }
	    tempDir.deleteOnExit();
	    return tempDir;
	}

	public static int getRandomInt () {
	    return 1 + new Random().nextInt();
	}
	
	/**
	 * Removes all sub-directories of a parent directory provided as a string.
	 * Sub-directories whose name equals one of the elements in the
	 * notToBeDeleted list are not deleted. If notToBeDeleted is null, all
	 * subdirectories are deleted.
	 * 
	 * Usage:
	 * 
	 * String testDir = "/tmp/testDir"; //removeSubDirs(testDir, null);
	 * //Removes all subdirs. removeSubDirs(testDir, Arrays.asList(new
	 * String[]{"xml", "hey", "yo"})); //Removes all subdirs but xml, hey and yo
	 * 
	 * 
	 * @param parentDir
	 * @param notToBeDeleted
	 * @throws IOException
	 */
	public static void removeSubDirs(String dirName, List<String> notToBeDeleted)
			throws IOException {
		File parentDir = new File(dirName);
		removeSubDirs(parentDir, notToBeDeleted);
	}

	/**
	 * Removes all sub-directories of a parent directory. Sub-directories whose
	 * name equals one of the elements in the notToBeDeleted list are not
	 * deleted. If notToBeDeleted is null, all subdirectories are deleted.
	 * 
	 * Usage:
	 * 
	 * File testDir = newFile("/tmp/testDir"); //removeSubDirs(testDir, null);
	 * //Removes all subdirs. removeSubDirs(testDir, Arrays.asList(new
	 * String[]{"xml", "hey", "yo"})); //Removes all subdirs but xml, hey and yo
	 * 
	 * @param parentDir
	 * @param notToBeDeleted
	 * @throws IOException
	 */
	public static void removeSubDirs(File parentDir, List<String> notToBeDeleted) throws IOException {
		if (parentDir.isDirectory()) {
			List<File> files = Arrays.asList(parentDir.listFiles());
			for (File file : files) {
				LOGGER.debug ("Checking "+file);
				if (notToBeDeleted!=null && notToBeDeleted.contains(file.getName())) {
					LOGGER.debug ("Not deleting "+file);
					continue;
				}
				if (file.isDirectory()) {
					FileUtils.deleteDirectory(file);
				}
			}
		} else {
			LOGGER.warn(parentDir + " is not a directory.");
		}
	}

	
}
