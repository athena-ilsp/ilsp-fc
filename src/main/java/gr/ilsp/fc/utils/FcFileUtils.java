/**
 * 
 */
package gr.ilsp.fc.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
//import java.net.MalformedURLException;
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
//import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
//import java.util.Random;
import java.util.Set;
import java.util.Vector;

//import org.apache.commons.cli.HelpFormatter;
//import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.northwestern.at.utils.ZipUtils;
import gr.ilsp.fc.readwrite.ReadResources;
import gr.ilsp.nlp.commons.Constants;

/**
 * @author prokopis
 * 
 */
public class FcFileUtils {

	private static final Logger logger = LoggerFactory.getLogger(FcFileUtils.class
			.getName());
	private static final String TMX_EXTENSION = ".tmx";
	private static final String ZIP=".zip";
	private static final String LANGUAGE_ELE = "language";
	private static final String LANGUAGE_ATT = "iso639";

	/*public static Vector<URL> listFilesAsURLs(File directory, FilenameFilter filter,
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
	}*/

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

	/*public static Collection<File> listDirs(File directory, boolean recurse) {
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
	}*/

	
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
				if (file.getName().endsWith(Constants.UNDERSCORE+types[ii]+TMX_EXTENSION)){
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
			if (file.isDirectory() || file.getName().contains(Constants.UNDERSCORE))  // Skip directories and cesAlignFiles
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
	
	
	/**
	 * Selects files that end with one of elements of types 
	 * @param tfs
	 * @param types
	 * @return
	 */
	public static  List<File> selectTypes(List<File> tfs, String[] types) {
		List<File> tmxfiles = new ArrayList<File>();
		for (File tf:tfs){
			for (int ii=0;ii<types.length;ii++){
				if (tf.getName().endsWith(types[ii])){
					tmxfiles.add(tf);
					break;
				}
			}
		}
		return tmxfiles;
	}
	

	public static String MimeDetect(String filename){
		String mimetype=null;
		InputStream is;
		try {
			is = new FileInputStream(filename);
			BufferedInputStream bis = new BufferedInputStream(is);
			AutoDetectParser parser = new AutoDetectParser();
			Detector detector = parser.getDetector();
			Metadata md = new Metadata();
			md.add(Metadata.RESOURCE_NAME_KEY, filename);
			MediaType mediaType = detector.detect(bis, md);
			mimetype=  mediaType.toString();
			bis.close();
		} catch (IOException e) {
			logger.error("Problem in examing mime type of file "+ filename);
			e.printStackTrace();
		}
		logger.info(filename + " detected mimetype is " + mimetype);
		return mimetype;
	}

	public static List<File> getCesDocs(File inputDir, String language) {
		List<File> xmlfiles = new ArrayList<File>();
		String[] ext = {"xml"};
		List<File> xmlfiles1 = (List<File>) FileUtils.listFiles(inputDir, ext, true);
		for (File xmlfile1:xmlfiles1){
			if (ReadResources.extractAttrfromXML(xmlfile1.getAbsolutePath(), LANGUAGE_ELE, LANGUAGE_ATT, true, false).equals(language))
				xmlfiles.add(xmlfile1);
		}
		return xmlfiles;
	}


}
