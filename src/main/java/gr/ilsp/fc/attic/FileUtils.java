/**
 * 
 */
package gr.ilsp.fc.attic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Random;
import java.util.Vector;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author prokopis
 * 
 */
public class FileUtils {

	private static final Logger logger = LoggerFactory.getLogger(FileUtils.class
			.getName());
	
	public static File[] listFilesAsArray(File directory,
			FilenameFilter filter, boolean recurse) {
		Collection<File> files = listFiles(directory, filter, recurse);
		// Java4: Collection files = listFiles(directory, filter, recurse);
		File[] arr = new File[files.size()];
		return files.toArray(arr);
	}
	
	public static Vector<URL> listFilesAsURLs(File directory, FilenameFilter filter,
			boolean recurse) {
		Vector<File> files = listFiles(directory, filter, recurse);
		Vector<URL> URLs = new Vector<URL>();
		for (File file: files) {
			try {
				URLs.add(file.toURI().toURL());
			} catch (MalformedURLException e) {
				logger.warn("Caught MalformedURLException for " + file.getAbsolutePath() + " . Skipping");
			}
		}
		return URLs;
	}
	
	public static Vector<File> listFiles(File directory, FilenameFilter filter,
			boolean recurse) {
		// List of files / directories
		Vector<File> files = new Vector<File>();
		// Get files / directories in the directory
		File[] entries = directory.listFiles();
		// Go over entries
		for (File entry : entries) {
			// If there is no filter or the filter accepts the
			// file / directory, add it to the list
			if (filter == null || filter.accept(directory, entry.getName())) {
				files.add(entry);
			}

			// If the file is a directory and the recurse flag
			// is set, recurse into the directory
			if (recurse && entry.isDirectory()) {
				files.addAll(listFiles(entry, filter, recurse));
			}
		}
		// Return collection of files
		return files;
	}

	public static Collection<File> listDirs(File directory, boolean recurse) {
		// List of files / directories
		Vector<File> files = new Vector<File>();
		// Java4: Vector files = new Vector();

		// Get files / directories in the directory
		File[] entries = directory.listFiles();

		// Go over entries
		for (File entry : entries) {
			// Java4: for (int f = 0; f < files.length; f++) {
			// Java4: File entry = (File) files[f];

			// If the file is a directory
			// add it to the list
			if (entry.isDirectory()) {
				files.add(entry);

				// If the file is a directory and the recurse flag
				// is set, recurse into the directory
				if (recurse) {
					files.addAll(listDirs(entry, recurse));
				}
			}
		}

		// Return collection of files
		return files;
	}

	public static String replaceExtension(String inFilename, String stripExt,
			String appendExt) {
		String outFilename;
		if (stripExt.equals("")) {
			outFilename = inFilename + appendExt;
		} else if (inFilename.endsWith(stripExt)) {
			outFilename = inFilename.substring(0,
					inFilename.lastIndexOf(stripExt))
					+ appendExt;
		} else {
			outFilename = inFilename + appendExt;
		}
		return outFilename;
	}

	public static void printHelp(String program, Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(program, options);
	}

	public static int getRandomInt() {
		return Math.abs(1 + new Random().nextInt(10000));
	}

	public static File createTempDir(String prefix) {
		if (prefix == null) {
			prefix = "";
		}
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		prefix = prefix + "-" + dateFormat.format(new Date()) + "-";

		final String baseTempPath = System.getProperty("java.io.tmpdir");
		File tempDir = new File(baseTempPath + File.separator + prefix 
				+ getRandomInt());
		if (tempDir.exists() == false) {
			tempDir.mkdir();
		}

		// tempDir.deleteOnExit();

		return tempDir;
	}

	public static File createTempDir() {
		return createTempDir(null);
	}
	
	public static boolean deleteTempDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteTempDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		return dir.delete();
	}

	/**
	 * Reads the contents of a URL into a string
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static String URL2String(URL url) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(
				url.openStream()));

		StringBuffer sb = new StringBuffer();
		String inputLine;

		while ((inputLine = in.readLine()) != null) {
			sb.append(inputLine);
			sb.append("\n");
		}

		in.close();
		return sb.toString();
	}

	public static String stripExtension(String filename, String mStripExt) {
		if (filename.endsWith(mStripExt)) {
			int lastMatch = filename.lastIndexOf(mStripExt);
			return filename.substring(0, lastMatch);
		} else {
			return filename;
		}
	}

	public static String stripExtension(String filename, String mStripExt,
			boolean removeLastDot) {
		filename = stripExtension(filename, mStripExt);
		if (removeLastDot) {
			filename = filename.replaceAll("^(.*)\\.$", "$1");
		}
		return filename;
	}

	public static void delete(String fileName) {
		try {
			// Construct a File object for the file to be deleted.
			File target = new File(fileName);

			if (!target.exists()) {
				//System.err.println("File " + fileName
				//		+ " not present to begin with!");
				return;
			}

			// Quick, now, delete it immediately:
			target.delete();
			//if (!target.delete())
			//System.err.println("** Deleted " + fileName + " **");
			//else
			//System.err.println("Failed to delete " + fileName);
		} catch (SecurityException e) {
			System.err.println("Unable to delete " + fileName + "("
					+ e.getMessage() + ")");
		}
	}
	
	
}
