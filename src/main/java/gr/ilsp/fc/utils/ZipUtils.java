package gr.ilsp.fc.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ZipUtils {

	private static final Logger logger = LoggerFactory.getLogger(ZipUtils.class);

	public static void zipFile(File file, File zipFile) throws IOException  {
		ZipArchiveOutputStream zos = new ZipArchiveOutputStream(zipFile);
        ZipArchiveEntry in = new ZipArchiveEntry(file, file.getName());
        zos.putArchiveEntry(in);
        byte[] b = new byte[(int) file.length()];
        FileInputStream fis = new FileInputStream(file);
        while (fis.read(b) > 0) {
            zos.write(b);
        }
        fis.close();
        zos.closeArchiveEntry();
        zos.close();
	}

    /**
     * @param arcFile The zip archive to process
     * @param outDir The dir where the archive is deflated
     * @throws IOException
     */
	public void unzipArchive(File arcFile, File outDir) throws IOException {
		ZipFile zipfile = new ZipFile(arcFile);
		for (Enumeration<? extends ZipEntry> e = zipfile.entries(); e.hasMoreElements(); ) {
			ZipEntry entry = (ZipEntry) e.nextElement();
			if (!entry.isDirectory()) {
				unzipEntry(zipfile, entry, outDir);
			}
		}

	}

	   /**
     * @param arcFile The zip archive to process
     * @param outDir The dir where the archive is deflated
     * @param pathname The pathname of the entry that should be extracted from the zip archive
     * @throws IOException
     */
	public void unzipFileFromArchive(File arcFile, File outDir, String pathname) throws IOException {
		ZipFile zipfile = new ZipFile(arcFile);
		for (Enumeration<? extends ZipEntry> e = zipfile.entries(); e.hasMoreElements(); ) {
			ZipEntry entry = (ZipEntry) e.nextElement();
			if (!entry.isDirectory() && entry.getName().equals(pathname)) {
				unzipEntry(zipfile, entry, outDir);
			}
		}

	}

	
    private void unzipEntry(ZipFile zipfile, ZipEntry entry, File outDir) throws IOException {

        if (entry.isDirectory()) {
            DirUtils.createDir(new File(outDir, entry.getName()));
            return;
        }

        File outputFile = new File(outDir, entry.getName());
        if (!outputFile.getParentFile().exists()){
            DirUtils.createDir(outputFile.getParentFile());
        }

        logger.debug("Extracting: " + entry);
        BufferedInputStream inputStream = new BufferedInputStream(zipfile.getInputStream(entry));
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

        try {
            IOUtils.copy(inputStream, outputStream);
        } finally {
            outputStream.close();
            inputStream.close();
        }
    }


	/**
	 * @param zipfile
	 * @param directoryToZip
	 * @param fileList
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void zipDir(File zipfile,File directoryToZip, Collection<File> fileList, String topDir) throws FileNotFoundException, IOException {

		FileOutputStream fos = new FileOutputStream(zipfile);
		ZipOutputStream zos = new ZipOutputStream(fos);
		
		for (File file : fileList) {
			if (!file.isDirectory()) { // we only zip files, not directories
				addToZip(directoryToZip, file, zos, topDir);
			}
		}

		zos.close();
		fos.close();
	}
	
	/**
	 * @param zipfile
	 * @param directoryToZip
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void zipDir(File zipfile,File directoryToZip) throws FileNotFoundException, IOException {
		Collection<File> fileList = FileUtils.listFiles(directoryToZip, null, false);
		zipDir(zipfile,directoryToZip, fileList, null);
	}

	private void addToZip(File directoryToZip, File file, ZipOutputStream zos, String topDir) throws FileNotFoundException,
			IOException {

		FileInputStream fis = new FileInputStream(file);

		// we want the zipEntry's path to be a relative path that is relative
		// to the directory being zipped, so chop off the rest of the path
		String zipFilePath = file.getCanonicalPath().substring(directoryToZip.getCanonicalPath().length() + 1,
				file.getCanonicalPath().length());
		if (topDir!= null) {
			zipFilePath = topDir + System.getProperty("file.separator") + zipFilePath;
		}
		
		//logger.debug("Writing '" + zipFilePath + "' to zip file");
		ZipEntry zipEntry = new ZipEntry(zipFilePath);
		zos.putNextEntry(zipEntry);

		byte[] bytes = new byte[1024];
		int length;
		while ((length = fis.read(bytes)) >= 0) {
			zos.write(bytes, 0, length);
		}

		zos.closeEntry();
		fis.close();
	}

	public static List<String> getLines(File gzFile) throws FileNotFoundException, IOException {
		GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(gzFile));
		return IOUtils.readLines(new BufferedReader(new InputStreamReader(gzip)));
	}

	
	
}
