/**
 * 
 */
package gr.ilsp.fc.parser;

import gr.ilsp.fc.datums.ExtendedParsedDatum;


//import java.io.BufferedReader;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
//import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;




import org.apache.commons.io.FilenameUtils;
//import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

//import org.apache.tika.language.ProfilingHandler;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.Parser;
//import org.apache.tika.sax.TeeContentHandler;

//import com.google.common.io.Files;
//import org.apache.tika.detect.DefaultDetector;
//import org.apache.tika.detect.Detector;
//import org.apache.tika.mime.MimeTypes;




//import de.l3s.boilerpipe.extractors.NumWordsRulesExtractor;
//import de.l3s.boilerpipe.extractors.ArticleExtractor;
import bixo.parser.BaseContentExtractor;


public class MsWordCallableParser implements Callable<ExtendedParsedDatum> {

	private InputStream _input;
	private Metadata _metadata;
	private String _storedir_path;
	private static String Wordcontent = "wordcontent";
	private static String DOCext = ".doc";
	private static String DOC = "doc";
	private static final String EUROPE_ORG_STR = "europa.eu";
	private static final String default_Europecomment_in_url = "©European Union, 1995-2014. Reuse is authorised, provided the source is acknowledged.";

	private static final Logger LOGGER = Logger.getLogger(MsWordCallableParser.class);
	

	public MsWordCallableParser(Parser parser, BaseContentExtractor contentExtractor, InputStream input, 
			Metadata metadata, String storedir_path) {
		this(parser, contentExtractor, input, metadata, true, false, storedir_path);
	}

	public MsWordCallableParser(Parser parser, BaseContentExtractor contentExtractor, InputStream input,
			Metadata metadata, boolean extractLanguage, boolean keepBoiler, String storedir_path) {
		_input = input;
		_metadata = metadata;
		_storedir_path = storedir_path;    
	}

	@Override
	public ExtendedParsedDatum call() throws Exception {	
		//try {        
			String content=Wordcontent;
			if (_storedir_path.startsWith("file:/"))
				_storedir_path = _storedir_path.substring(5); 
			File temp_dir = new File(FilenameUtils.concat(_storedir_path,DOC));
			File[] stored_files = temp_dir.listFiles();
			String filename = FilenameUtils.concat(temp_dir.getAbsolutePath(),stored_files.length+DOCext);
			OutputStream out = new BufferedOutputStream(new FileOutputStream(filename));

			URL baseUrl = new URL(_metadata.get(Metadata.CONTENT_LOCATION));
			URLConnection urlc=baseUrl.openConnection(); 
			InputStream inputstream=urlc.getInputStream();
			byte[] buffer = new byte[1024];
			int numRead;
			//long numWritten = 0;
			while ((numRead = inputstream.read(buffer)) != -1) {
				out.write(buffer, 0, numRead);
				//numWritten += numRead;
			}
			inputstream.close();
			out.close();
			LOGGER.info(filename + " saved.");
		
			String lang = "";
			_metadata.set(Metadata.CONTENT_ENCODING, "UTF-8");
			_metadata.set(Metadata.COMMENT,filename);
			ExtendedOutlink[] outlinks=new ExtendedOutlink[0];

			// Check if the  sourcelink is from europa.eu
			String sourceUrl = _metadata.get(Metadata.CONTENT_LOCATION);
			if (sourceUrl.contains(EUROPE_ORG_STR)){
				_metadata.set(Metadata.LICENSE_URL, default_Europecomment_in_url);
			}
			LOGGER.debug(sourceUrl + _metadata.get(Metadata.LICENSE_URL));    

			_metadata.set(Metadata.AUTHOR, "");
			_metadata.set(Metadata.TITLE, "");
			_metadata.set(Metadata.KEYWORDS,"");
			_metadata.set(Metadata.PUBLISHER,"");
			return new ExtendedParsedDatum(_metadata.get(Metadata.RESOURCE_NAME_KEY), null,	content, lang,
					_metadata.get(Metadata.TITLE), outlinks,makeMap(_metadata));
	}

	private static Map<String, String> makeMap(Metadata metadata) {
		Map<String, String> result = new HashMap<String, String>();

		for (String key : metadata.names()) {
			result.put(key, metadata.get(key));
		}

		return result;
	}

}