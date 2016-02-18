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


public class PdfboxCallableParser implements Callable<ExtendedParsedDatum> {
	//private static final Logger LOGGER = Logger.getLogger(PdfboxCallableParser.class);

	//private Parser _parser;
	// private BaseContentExtractor _contentExtractor;
	private InputStream _input;
	private Metadata _metadata;
	//private boolean _extractLanguage;
	private String _storedir_path;
	private boolean _sort_type = false;
	//private boolean _keepBoiler = false;
	private static String fs1 = System.getProperty("file.separator");
	private static String PDFcontent = "PDFcontent";
	private static String PDFext = ".pdf";
	private static final String EUROPE_ORG_STR = "europa.eu";
	private static final String default_Europecomment_in_url = "©European Union, 1995-2014. Reuse is authorised, provided the source is acknowledged.";

	private static final Logger LOGGER = Logger.getLogger(PdfboxCallableParser.class);
	//private static final Detector DETECTOR = new DefaultDetector(
	//        MimeTypes.getDefaultMimeTypes());

	public PdfboxCallableParser(Parser parser, BaseContentExtractor contentExtractor, InputStream input, 
			Metadata metadata, String storedir_path) {
		this(parser, contentExtractor, input, metadata, true, false, storedir_path);
	}

	public PdfboxCallableParser(Parser parser, BaseContentExtractor contentExtractor, InputStream input,
			Metadata metadata, boolean extractLanguage, boolean keepBoiler, String storedir_path) {
		//_parser = parser;
		//_contentExtractor = contentExtractor;
		_input = input;
		_metadata = metadata;
		// _extractLanguage = extractLanguage;
		_storedir_path = storedir_path;
		//_keepBoiler = keepBoiler;     
	}

	@Override
	public ExtendedParsedDatum call() throws Exception {	
		//try {        
			String content=PDFcontent;
			if (_storedir_path.startsWith("file:/"))
				_storedir_path = _storedir_path.substring(5); 
			File temp_dir = new File(_storedir_path+fs1+"pdf");
			File[] stored_files = temp_dir.listFiles();
			String filename = temp_dir.getAbsolutePath()+fs1+stored_files.length+PDFext;
			OutputStream out = new BufferedOutputStream(new FileOutputStream(filename));

			URL baseUrl = new URL(_metadata.get(Metadata.CONTENT_LOCATION));
			URLConnection urlc=baseUrl.openConnection(); 
			InputStream inputstream=urlc.getInputStream();
			byte[] buffer = new byte[1024];
			int numRead;
			long numWritten = 0;
			while ((numRead = inputstream.read(buffer)) != -1) {
				out.write(buffer, 0, numRead);
				numWritten += numRead;
			}
			inputstream.close();
			out.close();
			LOGGER.info(filename + " saved.");
			//System.out.println(filename);
			//content = Pdf2text.run1(new File(filename), _sort_type);
			//content = "";
			//System.out.println(content);
			//if (content==null){
			//	LOGGER.info("PDF to Text Conversion failed.");
			//}else{
			//	content = ContentNormalizer.normalizeText(content);
			//}

			String lang = "";

			_metadata.set(Metadata.CONTENT_ENCODING, "UTF-8");
			_metadata.set(Metadata.COMMENT,filename);
			//ExtendedOutlink[] outlinks = ExtendedLinksExtractor.getLinks(_input,_metadata);
			ExtendedOutlink[] outlinks=new ExtendedOutlink[0];

			// Check if the  sourcelink is from europa.eu
			String sourceUrl = _metadata.get(Metadata.CONTENT_LOCATION);
			if (sourceUrl.contains(EUROPE_ORG_STR)){
				_metadata.set(Metadata.LICENSE_URL, default_Europecomment_in_url);
			}
			LOGGER.debug(sourceUrl + _metadata.get(Metadata.LICENSE_URL));    

			//PDDocument pdDoc=null;
			//try {
			//	pdDoc = PDDocument.load(filename);
			//	PDDocumentInformation pdDocInfo=new PDDocumentInformation();
			//	pdDocInfo=pdDoc.getDocumentInformation();

			//	_metadata.set(Metadata.AUTHOR, pdDocInfo.getAuthor());
			//	_metadata.set(Metadata.TITLE, pdDocInfo.getTitle());
			//	_metadata.set(Metadata.KEYWORDS,pdDocInfo.getKeywords());
			//	_metadata.set(Metadata.PUBLISHER,pdDocInfo.getProducer());

			//	if (pdDoc != null) pdDoc.close();
			//} catch (Exception e){
			//	System.out.println("An exception occured in parsing the PDF Document.");
			//	e.printStackTrace();
			//	if (pdDoc != null) pdDoc.close();
			//}
			_metadata.set(Metadata.AUTHOR, "");
			_metadata.set(Metadata.TITLE, "");
			_metadata.set(Metadata.KEYWORDS,"");
			_metadata.set(Metadata.PUBLISHER,"");
			//LOGGER.info(_metadata.get(Metadata.RESOURCE_NAME_KEY));
			return new ExtendedParsedDatum(_metadata.get(Metadata.RESOURCE_NAME_KEY), null,	content, lang,
					_metadata.get(Metadata.TITLE), outlinks,makeMap(_metadata));
		//} catch (Exception e) {
		//	// Generic exception that's OK to re-throw
		//	throw e;
		//} //catch (NoSuchMethodError e) {
		// throw new RuntimeException("Attempting to use excluded parser");
		// } 
		//catch (Throwable t) {
		//	throw new RuntimeException("Serious shut-down error thrown from PDFBOX", t);
		//}
	}


	/**
	 * Decide if we need to set up our own HtmlMapper, because the link extractor has tags that
	 * aren't part of the default set.
	 * 
	 * @return
	 */
	//private ParseContext makeParseContext() {
	//    ParseContext result = new ParseContext();       
	//    HtmlMapper defaultMapper = DefaultHtmlMapper.INSTANCE;        
	//    result.set(HtmlMapper.class, defaultMapper);
	//    return result;
	//}

	/**
	 * See if a language was set by the parser, from meta tags.
	 * As a last resort falls back to the result from the ProfilingHandler.
	 *  
	 * @param metadata
	 * @param profilingHandler
	 * @return The first language found (two char lang code) or empty string if no language was detected.
	 */
	/*private static String detectLanguage(Metadata metadata, ProfilingHandler profilingHandler) {
        String result = null;

        String dubCoreLang = metadata.get(Metadata.LANGUAGE);
        String httpEquivLang = metadata.get(Metadata.CONTENT_LANGUAGE);

        if (dubCoreLang != null) {
            result = dubCoreLang;
        } else if (httpEquivLang != null) {
            result = httpEquivLang;
        }

        result = getFirstLanguage(result);

        if (result == null) {
            // Language is still unspecified, so use ProfileHandler's result
            LanguageIdentifier langIdentifier = profilingHandler.getLanguage();
            // FUTURE KKr - provide config for specifying required certainty level.
            if (langIdentifier.isReasonablyCertain()) {
                result = langIdentifier.getLanguage();
                LOGGER.trace("Using language specified by profiling handler: " + result);
            } else {
                result = "";
            }

        }

        return result;
    }*/

	private static Map<String, String> makeMap(Metadata metadata) {
		Map<String, String> result = new HashMap<String, String>();

		for (String key : metadata.names()) {
			result.put(key, metadata.get(key));
		}

		return result;
	}


	/*private static String getFirstLanguage(String lang) {
        if (lang != null && lang.length() > 0) {
            // TODO VMa -- DublinCore languages could be specified in a multiple of ways
            // see : http://dublincore.org/documents/2000/07/16/usageguide/qualified-html.shtml#language
            // This means that it is possible to get back 3 character language strings as per ISO639-2
            // For now, we handle just two character language strings and if we do get a 3 character string we 
            // treat it as a "null" language.

            // TODO VMa - what if the length is just one char ?
            if (lang.length() > 2) {
                Matcher m = LANGUAGE_CODE_PATTERN.matcher(lang);

                if (m.matches()) {
                    lang = m.group(1);
                } else {
                    lang = null;
                }
            }
        } 
        return lang;
    }*/

}