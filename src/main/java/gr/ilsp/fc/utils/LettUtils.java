package gr.ilsp.fc.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utils class for reading wmt16 lett files
 * (http://www.statmt.org/wmt16/bilingual-task.html)
 * 
 * @author prokopis
 *
 */
public class LettUtils {

	private static final String HYPHEN = "-";
	private static final String UTF_8 = "UTF-8";
	private static final String HTML_EXTENSION = ".html";
	private static final String TAB_SEPARATOR = "\t";
	private static final Logger logger = LoggerFactory.getLogger(LettUtils.class);
	private static Map<String, String> file2Url = new HashMap<String, String>();
	/**
	 * Reads an (optionally gzipped) wmt-16 lett file, base64-decodes the html
	 * content in each line and writes this content in a file in corpusOutDir
	 * 
	 * @param lettFile
	 * @param corpusOutDir
	 * @param gzipped
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void createCorpusFromLettFile(File lettFile, File corpusOutDir, boolean gzipped) throws FileNotFoundException, IOException {
		DirUtils.createDir(corpusOutDir);
		Charset cs = Charset.forName(UTF_8);
		CharsetDecoder decoder = cs.newDecoder()
				.onMalformedInput(CodingErrorAction.REPLACE)
				.onUnmappableCharacter(CodingErrorAction.REPLACE);
		BufferedReader br;
		if (gzipped) {
			br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(lettFile)), decoder));
		} else {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(lettFile), decoder));
		}
		int id = 1;
		Base64 base64 = new Base64();
		for (String line = null; (line = br.readLine()) != null;) {
			String[] fields = StringUtils.split(line, TAB_SEPARATOR);
//			if (id==733) {		
//				logger.info(id + "\t" + fields[0] + "\t" + fields[3] );
//				logger.info(new String(base64.decode(fields[4].getBytes())));
//
//			}
			File outFileHtml = new File(FilenameUtils.concat(corpusOutDir.getAbsolutePath(), fields[0] + HYPHEN + id++ + HTML_EXTENSION));
			String decoded = new String(base64.decode(fields[4].getBytes()));
			FileUtils.write(outFileHtml, decoded);
			file2Url.put(outFileHtml.getAbsolutePath(), (fields[3] + "\t" +fields[0]));
		}
		br.close();
	}

	public static void main(String[] args) throws IOException  {
		File lettDir = new File(args[0]);
		File lettDirHtml = new File(args[1]);
		logger.info("Creating corpus from lett files in "+ lettDir.getAbsolutePath());
		logger.info("Using out dir " + lettDirHtml.getAbsolutePath());
		DirUtils.createDir(lettDirHtml);
		Collection<File> gzLettFiles = FileUtils.listFiles(lettDir, new WildcardFileFilter("*gz"), null);
		for (File gzLettFile : gzLettFiles) {		
			File corpusOutDir = new File(FilenameUtils.concat(lettDirHtml.getAbsolutePath(),
					FilenameUtils.getBaseName(gzLettFile.getName())));
//			if (!gzLettFile.getAbsolutePath().contains("italiasullarete") ) {
//				continue;
//			}
			logger.info("Reading " + gzLettFile);
			logger.info("Exporting html files to " + corpusOutDir);
			try {
				createCorpusFromLettFile(gzLettFile, corpusOutDir, true);
			} catch (IOException e) {
				logger.error("Could not process file " + gzLettFile);
				e.printStackTrace();
			}
		}
		File f = new File("/tmp/file_url_lang.txt"); 
		TreeSet<String> treeSet = new TreeSet<String>(file2Url.keySet());
		for (String key : treeSet) {
		    String value = file2Url.get(key);
		    FileUtils.write(f,  (key + "\t" + value+"\n"),  "UTF-8", true);
		}
		
		logger.info("Done.");
	}

}
