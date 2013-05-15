/**
 * 
 */
package gr.ilsp.fmc.parser;


import gr.ilsp.fmc.datums.ExtendedParsedDatum;
import gr.ilsp.fmc.utils.ContentNormalizer;

//import java.io.BufferedReader;
import java.io.InputStream;
//import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.util.PDFTextStripper;

import org.apache.tika.language.ProfilingHandler;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.sax.TeeContentHandler;
//import org.apache.tika.detect.DefaultDetector;
//import org.apache.tika.detect.Detector;
//import org.apache.tika.mime.MimeTypes;

//import de.l3s.boilerpipe.extractors.NumWordsRulesExtractor;
//import de.l3s.boilerpipe.extractors.ArticleExtractor;




import bixo.parser.BaseContentExtractor;

public class PdfboxCallableParser implements Callable<ExtendedParsedDatum> {
    //private static final Logger LOGGER = Logger.getLogger(PdfboxCallableParser.class);
    
    private PDFParser _parser;
    private BaseContentExtractor _contentExtractor;
    private InputStream _input;
    private Metadata _metadata;
    private boolean _extractLanguage;
	//private boolean _keepBoiler = false;

	
	//private static final Detector DETECTOR = new DefaultDetector(
	//        MimeTypes.getDefaultMimeTypes());
	
    public PdfboxCallableParser(PDFParser parser, BaseContentExtractor contentExtractor, InputStream input, Metadata metadata) {
        this(parser, contentExtractor, input, metadata, true, false);
    }
    
    public PdfboxCallableParser(PDFParser parser, BaseContentExtractor contentExtractor, InputStream input, Metadata metadata, boolean extractLanguage, boolean keepBoiler) {
        _parser = parser;
        _contentExtractor = contentExtractor;
        _input = input;
        _metadata = metadata;
        _extractLanguage = extractLanguage;
        //_keepBoiler = keepBoiler;     
    }
    
    @Override
    public ExtendedParsedDatum call() throws Exception {
        try {        	 	        	
            TeeContentHandler teeContentHandler;
            ProfilingHandler profilingHandler = null;
            
            if (_extractLanguage) {
                profilingHandler = new ProfilingHandler();
                teeContentHandler = new TeeContentHandler(_contentExtractor,  profilingHandler);
            } else {
                teeContentHandler = new TeeContentHandler(_contentExtractor);
            }
            
            //String respoCharset = _metadata.get(Metadata.CONTENT_ENCODING);
            //_parser.parse(_input, teeContentHandler, _metadata, makeParseContext());
            //if (respoCharset!=null && respoCharset!=_metadata.get(Metadata.CONTENT_ENCODING))
            //	_metadata.set(Metadata.CONTENT_ENCODING, respoCharset);
            
            _metadata.set(Metadata.CONTENT_ENCODING, "UTF-8");
            ExtendedOutlink[] outlinks = ExtendedLinksExtractor.getLinks(_input,_metadata);
            //String lang = _extractLanguage ? detectLanguage(_metadata, profilingHandler) : "";
            String lang = "";
            _input.reset();
            String content = "";
            
            URL baseUrl = new URL(_metadata.get(Metadata.CONTENT_LOCATION));
            URLConnection urlc; 
			InputStream inputstream;
			//try {
			urlc = baseUrl.openConnection();
			//urlc.setConnectTimeout(5000);
			//urlc.setReadTimeout(100000);
			inputstream = urlc.getInputStream();
    		
    		_parser = new PDFParser(inputstream);
    		//_parser = new PDFParser(_input);
    		try {
    			_parser.parse();
    			COSDocument cosDoc = _parser.getDocument();
    			PDFTextStripper pdfStripper = new PDFTextStripper();
    			PDDocument pdDoc = new PDDocument(cosDoc);
    			PDDocumentInformation pdDocInfo=new PDDocumentInformation();
    			pdDocInfo=pdDoc.getDocumentInformation();
    			//System.out.println("author:"+pdDocInfo.getAuthor());
    			//System.out.println("title:"+pdDocInfo.getTitle());
    			//int page_nums=pdDoc.getNumberOfPages();
    			Map skata=pdDoc.getPageMap();
    			
    			_metadata.set(Metadata.AUTHOR, pdDocInfo.getAuthor());
    			_metadata.set(Metadata.TITLE, pdDocInfo.getTitle());
    			_metadata.set(Metadata.KEYWORDS,pdDocInfo.getKeywords());
    			_metadata.set(Metadata.PUBLISHER,pdDocInfo.getProducer());
    			_metadata.set(Metadata.DATE,pdDocInfo.getModificationDate().toString());
    			
    			content = pdfStripper.getText(pdDoc);
    			//System.out.println(content);
    			content = ContentNormalizer.normalizePdfText(content);
    			//System.out.println(content);
    			if (content==null)
    				System.out.println("PDF to Text Conversion failed.");
    			 if (cosDoc != null) cosDoc.close();
    			 if (pdDoc != null) pdDoc.close();
    			 //inputstream.close();
    		} catch (Exception e){
    			System.out.println("An exception occured in parsing the PDF Document.");
    			e.printStackTrace();
    		}
    		  		
            return new ExtendedParsedDatum(_metadata.get(Metadata.RESOURCE_NAME_KEY), null, /*_contentExtractor.getContent()*/content, lang,
                    _metadata.get(Metadata.TITLE), outlinks,makeMap(_metadata));
        } catch (Exception e) {
            // Generic exception that's OK to re-throw
            throw e;
        } catch (NoSuchMethodError e) {
            throw new RuntimeException("Attempting to use excluded parser");
        } catch (Throwable t) {
            throw new RuntimeException("Serious shut-down error thrown from Tika", t);
        }
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