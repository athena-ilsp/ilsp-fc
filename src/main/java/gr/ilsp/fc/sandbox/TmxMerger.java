package gr.ilsp.fc.sandbox;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import gr.ilsp.nlp.commons.Constants;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TmxMerger {

	private static final Logger logger = LoggerFactory.getLogger(TmxMerger.class);

	public void mergeTmxFiles(List<File> tmxFiles, File mergedFile) throws FileNotFoundException, UnsupportedEncodingException {
		int tmxI = 0;
		for (File tmxFile : tmxFiles) {
			logger.info("Parsing " + tmxFile.getAbsolutePath()); 
			tmxI++;
		}
		logger.info("NOT HERE YET Merged " + tmxI + " tmx files into " + mergedFile.getAbsolutePath() ); 
	}

	public void mergeTmxFiles(File tmxFileList, File mergedFile) throws IOException {
		List<File> tmxFiles = new ArrayList<File>();
		logger.info("Parsing " + tmxFileList.getAbsolutePath()); 
		for (String tmxLine : FileUtils.readLines(tmxFileList, Constants.UTF8)) {
			tmxFiles.add(new File(tmxLine.trim()));
		}
		mergeTmxFiles(tmxFiles, mergedFile);
	}

}
