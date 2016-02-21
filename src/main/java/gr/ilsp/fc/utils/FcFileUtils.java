/**
 * 
 */
package gr.ilsp.fc.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.northwestern.at.utils.ZipUtils;

/**
 * @author prokopis
 * 
 */
public class FcFileUtils {

	private static final Logger logger = LoggerFactory.getLogger(FcFileUtils.class
			.getName());
	private static final String UNDERSCORE_STR = "_";
	private static final String XML_EXTENSION = ".xml";
	private static final String TMX_EXTENSION = ".tmx";
	private static final String ZIP=".zip";
	//private final static String PUNCT = ".";

	public static File[] listFilesAsArray(File directory,
			FilenameFilter filter, boolean recurse) {
		Collection<File> files = listFiles(directory, filter, recurse);
		// Java4: Collection files = listFiles(directory, filter, recurse);
		File[] arr = new File[files.size()];
		return files.toArray(arr);
	}

	public static Vector<URL> listFilesAsURLs(File directory, FilenameFilter filter,
			boolean recurse) {
		List<File> files = listFiles(directory, filter, recurse);
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

	/**
	 * Searches the directory recursively (if recurse is true) and matches filter
	 * @param directory
	 * @param filter
	 * @param recurse
	 * @return
	 */
	public static List<File> listFiles(File directory, FilenameFilter filter,	boolean recurse) {
		// List of files / directories
		List<File> files = new ArrayList<File>();
		if (!directory.exists()){
			return files;
		}
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

	/**
	 * Select files which end with x+"."+ext, where x is in selectedtypes 
	 * @param targetDir
	 * @param types
	 * @param ext
	 * @return
	 *//*
	public static List<File> selectfiles(Vector<File> files, String selectedtypes,String ext) {
		List<File> selectedfiles = new ArrayList<File>();
		for (File file:files){
			if (file.getName().endsWith(ext)){
				int t= file.getName().lastIndexOf(PUNCT);
				String tt = file.getName().substring(t-1, t);
				if (selectedtypes.contains(tt)){
					selectedfiles.add(file);
				}
			}
		}
		return selectedfiles;
	}*/


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

	public static void copy(String fromFileName, String toFileName)
			throws IOException {
		File fromFile = new File(fromFileName);
		File toFile = new File(toFileName);

		if (!fromFile.exists())
			throw new IOException("FileCopy: " + "no such source file: "
					+ fromFileName);
		if (!fromFile.isFile())
			throw new IOException("FileCopy: " + "can't copy directory: "
					+ fromFileName);
		if (!fromFile.canRead())
			throw new IOException("FileCopy: " + "source file is unreadable: "
					+ fromFileName);

		if (toFile.isDirectory())
			toFile = new File(toFile, fromFile.getName());

		if (toFile.exists()) {
			if (!toFile.canWrite())
				throw new IOException("FileCopy: "
						+ "destination file is unwriteable: " + toFileName);
			//System.out.print("Overwrite existing file " + toFile.getName()
			//		+ "? (Y/N): ");
			//System.out.flush();
			//BufferedReader in = new BufferedReader(new InputStreamReader(
			//		System.in));
			//String response = in.readLine();
			String response ="Y";
			if (!response.equals("Y") && !response.equals("y"))
				throw new IOException("FileCopy: "
						+ "existing file was not overwritten.");
		} else {
			String parent = toFile.getParent();
			if (parent == null)
				parent = System.getProperty("user.dir");
			File dir = new File(parent);
			if (!dir.exists())
				throw new IOException("FileCopy: "
						+ "destination directory doesn't exist: " + parent);
			if (dir.isFile())
				throw new IOException("FileCopy: "
						+ "destination is not a directory: " + parent);
			if (!dir.canWrite())
				throw new IOException("FileCopy: "
						+ "destination directory is unwriteable: " + parent);
		}
		FileUtils.copyFile(fromFile,toFile);
	}


	public static ArrayList<File> getFilesList(File datadir, String start_name,	String end_name) {
		File[] allfiles=datadir.listFiles(); 
		ArrayList<File> temp = new ArrayList<File>();
		for (int ii=0;ii<allfiles.length;ii++){
			if (start_name.isEmpty()){
				if (end_name.isEmpty()){
					temp.add(allfiles[ii]);
				}else{
					if (allfiles[ii].getName().endsWith(end_name)){
						temp.add(allfiles[ii]);
					}
				}
			}else{
				if (end_name.isEmpty()){
					if (allfiles[ii].getName().startsWith(start_name)){
						temp.add(allfiles[ii]);
					}
				}else{
					if (allfiles[ii].getName().startsWith(start_name) & allfiles[ii].getName().endsWith(end_name)){
						temp.add(allfiles[ii]);
					}
				}
			}
		}

		return temp;
	}

	/**
	 * gets the cesDoc files (i.e. end with .xml and do not contain "_" from the datadir)
	 * @param datadir
	 * @return
	 */
	public static ArrayList<File> getCesDocFiles(File datadir) {
		File[] allfiles=datadir.listFiles(); 
		ArrayList<File> result = new ArrayList<File>();
		String filename ;
		for (int ii=0;ii<allfiles.length;ii++){
			filename =allfiles[ii].getName(); 
			if (filename.endsWith(XML_EXTENSION)&& !filename.contains(UNDERSCORE_STR)){
				result.add(allfiles[ii]);		
			}
		}		
		return result;
	}

	/**
	 * Deletes the files (created for the output lists) if these lists are empty
	 * i.e. no pairs detected
	 */

	public static void removeOutputList(File outputFile, File outputFileHTML) {

		if (outputFile.exists())
			outputFile.delete(); 

		if (outputFileHTML.exists())
			outputFileHTML.delete(); 
	}

	/**
	 * Cluster files in groups based on the last past of their names
	 * @param tmxfiles
	 * @param doctypes
	 * @return
	 */
	public static HashMap<String, List<File>> clusterfiles(List<File> tmxfiles,	String doctypes) {
		HashMap<String, List<File>> filegroups = new HashMap<String, List<File>>();
		String[] types = new String[doctypes.length()];
		for (int ii=0;ii<doctypes.length();ii++){
			types[ii] = Character.toString(doctypes.charAt(ii));
		}
		for (File file:tmxfiles){
			for (int ii=0;ii<types.length;ii++){
				if (file.getName().endsWith(UNDERSCORE_STR+types[ii]+TMX_EXTENSION)){
					if (filegroups.containsKey(types[ii])){
						List<File> filesgroup = filegroups.get(types[ii]);
						filesgroup.add(file);
						filegroups.put(types[ii], filesgroup);
					}else{
						List<File> filesgroup = new ArrayList<File>();
						filesgroup.add(file);
						filegroups.put(types[ii], filesgroup);
					}
					break;						
				}
			}
		}
		Set<String> methods=filegroups.keySet();
		Iterator<String> method = methods.iterator();
		String key;
		while (method.hasNext()){
			key = method.next();
			List<File> filesgroup = filegroups.get(key);
			logger.info(filesgroup.size() + " pairs of method "+ key);			
		}
		return filegroups;
	}


	/**
	 * 
	 * <B>Use carefully.</B>
	 * 
	 * All files ending with (ext) are deleted.
	 * 
	 * @param parentDir
	 * @param tempfileext
	 */
	public static void removeFiles(List<File> filelist, final String ext) {
		for (File file: filelist) {
			if (file.isDirectory() || file.getName().contains(UNDERSCORE_STR))  // Skip directories and cesAlignFiles
				continue;
			if (file.getName().endsWith(ext))
				file.delete();
		}
	}

	/**
	 * Creates a dir named newdir in parentDir. Moves files ending with one of exts from parentDir to parentDir/newdir.
	 * Compresses  parentDir/newdir and then deletes the  parentDir/newdir.
	 */
	public static void moveZipDeleteFiles(File parentDir, String newdir, List<String> exts, String inname, boolean ispairfile) {
		if (parentDir.isDirectory()) {
			File newDirName = new File(FilenameUtils.concat(parentDir.getAbsolutePath(), newdir));
			newDirName.mkdir();
			List<File> files = Arrays.asList(parentDir.listFiles());
			int counter = 0, thous=0;
			for (File file: files) {
				if (file.isDirectory()) { 
					continue;
				}
				if (ispairfile){ //cesAlignFiles
					if (!file.getName().contains(inname)){
						continue;
					}
				}else{//cesDocFiles
					if (file.getName().contains(inname)){
						continue;
					}
				}
				boolean tobeselected = false;
				for (String ext:exts){
					if (file.getName().endsWith(ext)){
						tobeselected=true;
						break;
					}
				}
				if (tobeselected){
					try {
						FileUtils.moveFileToDirectory(file, newDirName, false);
					} catch (IOException e) {
						e.printStackTrace();
					}
					counter++;
					if (counter/1000>thous){
						thous++;
						logger.info((thous*1000)+ " files have been added to zip dir");
					}
				}
			}
			String outputZipFileName = FilenameUtils.concat(newDirName.getParent(), newDirName.getName()+ZIP);
			try {
				ZipUtils.zipDirectoryTree(newDirName.getAbsolutePath(), outputZipFileName);
				FileUtils.deleteDirectory(newDirName);
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else 
			logger.warn(parentDir + " is not a directory.");
	}
	/**
	 * get from targetDir the files that have names ending in one of types
	 * @param targetDir
	 * @param types
	 * @return
	 */
	public static Vector<File> selectfiles(File targetDir, String[] types) {
		Vector<File> tmxfiles = new Vector<File>();
		if (targetDir.isDirectory()){
			File[] files = targetDir.listFiles();
			for (File file:files){
				for (int ii=0;ii<types.length;ii++){
					if (file.getName().endsWith(types[ii])){
						tmxfiles.add(file);
						break;
					}
				}
			}
		}
		return tmxfiles;
	}
	
}
