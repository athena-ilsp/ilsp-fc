package gr.ilsp.fc.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LettUtils {

	private static final String EXTENSION = ".html";
	private static final String SEPARATOR_CHAR = "\t";
	private static final Logger logger = LoggerFactory.getLogger(LettUtils.class);

	public static void main(String[] args) throws IOException {
		///opt/wmt16-bda/lett.train /var/www/lett.train.html
		File lettDir = new File(args[0]);
		File lettDirHtml = new File(args[1]);
		logger.info(lettDir.getAbsolutePath());
		logger.info(lettDirHtml.getAbsolutePath());
		DirUtils.createDir(lettDirHtml);
		Collection<File> lettFiles = FileUtils.listFiles(lettDir, new WildcardFileFilter("*lett"), null);
		for (File lettFile : lettFiles) {
			// TBFI: Why does this crash the decoding process?
			if (lettFile.getName().contains("cgfmanet")) {
				continue;
			}
			createCorpusFromLettFile(lettFile, new File(FilenameUtils.concat(lettDirHtml.getAbsolutePath(), FilenameUtils.getBaseName(lettFile.getName()))));
		}
// 		Memory issues even with -Xmx4g for gz files 
//		Collection<File> gzLettFiles = FileUtils.listFiles(lettDir, new WildcardFileFilter("*gz"), null);
//		for (File gzLettFile : gzLettFiles) {
//			createCorpusFromGzLettFile(gzLettFile, new File(FilenameUtils.concat(lettDirHtml.getAbsolutePath(), FilenameUtils.getBaseName(gzLettFile.getName()))));
//		}
	}

	public static void createCorpusFromLettFile(File lettFile, File corpusDir) throws IOException  {
		logger.info("Reading " + lettFile);
		logger.info("Creating " + corpusDir);
		DirUtils.createDir(corpusDir);
		try (BufferedReader br = Files.newBufferedReader(Paths.get(lettFile.getAbsolutePath()), StandardCharsets.UTF_8)) {
			int id = 1;
		    Base64 base64 = new Base64();
		    for (String line = null; (line = br.readLine()) != null;) {
				String[] fields =  StringUtils.split(line, SEPARATOR_CHAR);
				File outFileHtml = new File(FilenameUtils.concat(corpusDir.getAbsolutePath(), fields[0] + "-" + id++ + EXTENSION));
				String decoded = new String(base64.decode(fields[4].getBytes()));
				logger.debug("Writing to " + outFileHtml);
				FileUtils.write(outFileHtml, decoded);
			}
		}
	}

	public static void createCorpusFromGzLettFile(File gzLettFile, File corpusDir) throws IOException  {
		logger.info("Gunzipping " + gzLettFile);
		logger.info("Creating " + corpusDir);
		DirUtils.createDir(corpusDir);
		List<String> lines =  ZipUtils.getLines(gzLettFile);
		int id = 1;
	    Base64 base64 = new Base64();
		for (String line: lines) {
			String[] fields =  StringUtils.split(line, SEPARATOR_CHAR);
			File outFileHtml = new File(FilenameUtils.concat(corpusDir.getAbsolutePath(), fields[0] + "-" + id++ + EXTENSION));
			String decoded = new String(base64.decode(fields[4].getBytes()));
			logger.debug("Writing to " + outFileHtml);
			FileUtils.write(outFileHtml, decoded);
		}
	}
	
}
