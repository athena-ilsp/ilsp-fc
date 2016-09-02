/**
 * 
 */
package gr.ilsp.fc.parser;

import gr.ilsp.fc.cleaner.CleanerUtils;
import gr.ilsp.fc.datums.ExtendedParsedDatum;
import gr.ilsp.fc.langdetect.LangDetectUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
//import org.apache.tika.detect.DefaultDetector;
//import org.apache.tika.detect.Detector;
//import org.apache.tika.mime.MimeTypes;
import org.apache.tika.language.ProfilingHandler;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.html.DefaultHtmlMapper;
import org.apache.tika.parser.html.HtmlMapper;
import org.apache.tika.sax.TeeContentHandler;

import bixo.parser.BaseContentExtractor;


public class TikaCallableParser implements Callable<ExtendedParsedDatum> {
	//private static final Logger LOGGER = Logger.getLogger(TikaCallableParser.class);

	private static final String LICENSES_STR = "/licenses/";
	private static final String HTTP_PROTOCOL = "http";
	private static final String HTTPS_PROTOCOL = "https";
	private static final String CREATIVECOMMONS_ORG_STR = "creativecommons.org";
	private static final String EUROPE_ORG_STR = "europa.eu";
	//private Matcher CCMatcher = Pattern.compile(".*Creative Commons.*", Pattern.CASE_INSENSITIVE).matcher("");
	//private static final String CC_pattern= "Creative Commons";
	//private static final String default_CCurl_in_text= "http://creativecommons.org/licenses/by/3.0/";
	private static final String default_CCcomment_in_url = "Distributed under a Creative Commons license";
	private static final String default_Europecomment_in_url = "©European Union, 1995-2014. Reuse is authorised, provided the source is acknowledged.";
	/*private static final String default_CCcomment_in_text = "Distributed under a Creative Commons license (auto detected in document)";*/
	//private static final String default_CCcomment_in_text = "Distributed under a Creative Commons license";
	private static final String text_cc_separ = ";";
	//private static final String REL_LICENSE_LOCATION = "rel";
	private static final Logger LOGGER = Logger.getLogger(TikaCallableParser.class);
	// Simplistic language code pattern used when there are more than one languages specified
	// FUTURE KKr - improve this to handle en-US, and "eng" for those using old-style language codes.
	private static final Pattern LANGUAGE_CODE_PATTERN = Pattern.compile("([a-z]{2})([,;-]).*");
	private static final String LINK_CANONICAL = "link[canonical]";

	private Parser _parser;
	private BaseContentExtractor _contentExtractor;
	private InputStream _input;
	private Metadata _metadata;
	private boolean _extractLanguage;
	private boolean _keepBoiler = false;
	private String[] _targeted_langs;
	private HashMap<String,String> _maplangs;
	private List<String[]> _tranlistAttrs;

	public TikaCallableParser(Parser parser, BaseContentExtractor contentExtractor, InputStream input, Metadata metadata, HashMap<String,String> maplangs, 
			List<String[]> tranlistAttrs, String[] targeted_langs) {
		this(parser, contentExtractor, input, metadata, true, maplangs, tranlistAttrs, targeted_langs, false);
	}

	public TikaCallableParser(Parser parser, BaseContentExtractor contentExtractor, 
			InputStream input, Metadata metadata, boolean extractLanguage, HashMap<String,String> maplangs, List<String[]> tranlistAttrs, String[] targeted_langs, boolean keepBoiler) {
		_parser = parser;
		_contentExtractor = contentExtractor;
		_input = input;
		_metadata = metadata;
		_extractLanguage = extractLanguage;
		_targeted_langs = targeted_langs;
		_keepBoiler = keepBoiler;
		_maplangs = maplangs;
		_tranlistAttrs = tranlistAttrs;
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
			//ISSUE with Content-Encoding reported error by meta in content while it is correct
			//from the HTTp-response. This is official TIKA bug (https://issues.apache.org/jira/browse/TIKA-539)
			//Temp solution until bug is resolved: keep the HTTP response META (if exists) and overwrite
			String respoCharset = _metadata.get(Metadata.CONTENT_ENCODING);
			_parser.parse(_input, teeContentHandler, _metadata, makeParseContext());
		
			//FIXME set another property to HTMLSource or in the fetchedDatum. Check if we keep HTML source twice
			_input.reset();
			BufferedReader reader = new BufferedReader(new InputStreamReader(_input,_metadata.get(Metadata.CONTENT_ENCODING)));
			/*System.out.println(IOUtils.toString(reader));
			_metadata.set(Metadata.COMMENTS, IOUtils.toString(reader));*/
			StringBuilder builder = new StringBuilder();
			String aux = "";
			while ((aux = reader.readLine()) != null) {
			    //builder.append(aux+" ");
			    builder.append(aux);
			}
			_metadata.set(Metadata.COMMENTS, builder.toString());

			//FIXME Why we check this? 
			//if (_metadata.get(Metadata.CONTENT_ENCODING)!=null && respoCharset!=_metadata.get(Metadata.CONTENT_ENCODING))
			if (respoCharset!=null && respoCharset!=_metadata.get(Metadata.CONTENT_ENCODING))
				_metadata.set(Metadata.CONTENT_ENCODING, respoCharset);

			if (!_metadata.get(Metadata.CONTENT_LOCATION).equals(_metadata.get(Metadata.RESOURCE_NAME_KEY))){
				//_metadata.set(Metadata.RESOURCE_NAME_KEY, _metadata.get(Metadata.CONTENT_LOCATION));
				LOGGER.debug("KEY: "+_metadata.get(Metadata.RESOURCE_NAME_KEY)+"\tLOC: "+_metadata.get(Metadata.CONTENT_LOCATION));	
				_metadata.set(Metadata.CONTENT_LOCATION, _metadata.get(Metadata.RESOURCE_NAME_KEY));
			}
			//String lang = _extractLanguage ? _metadata.get(Metadata.CONTENT_LANGUAGE) : null;
			/*String lang = _extractLanguage ? detectLanguageFromMetadata(_metadata, profilingHandler) : null;
			String content = "";
			if (lang!=null ){//language is extracted from metadata
				boolean match = LangDetectUtils.istargetedlang(lang,_targeted_langs);
				if (match) //get main content annotated as bolierplate or text
					content = CleanerUtils.getContent(_input, _metadata, _keepBoiler);
			}else{ //identify language with language identifiers
				content = CleanerUtils.getContent(_input, _metadata, _keepBoiler);
				lang = LangDetectUtils.detectLanguage(CleanerUtils.cleanContent(content));
			}*/
			String content = CleanerUtils.getContent(_input, _metadata, _keepBoiler);  
			String lang = LangDetectUtils.detectLanguage(CleanerUtils.cleanContent(content));

			ExtendedOutlink[] outlinks = ExtendedLinksExtractor.getLinks(_input,_metadata,_maplangs, _tranlistAttrs);
			if (outlinks.length==1 && outlinks[0].getAnchor().equals(LINK_CANONICAL)) {
				return new ExtendedParsedDatum(_metadata.get(Metadata.RESOURCE_NAME_KEY), null, "", lang,
						_metadata.get(Metadata.TITLE), outlinks,makeMap(_metadata));
			}

			// Check if the  sourcelink is from europa.eu
			boolean found_license=false;
			if (_metadata.get(Metadata.CONTENT_LOCATION).contains(EUROPE_ORG_STR)){
				_metadata.set(Metadata.LICENSE_URL, default_Europecomment_in_url);
				found_license=true;
			}
			if (!found_license){
				// Check each link for creative commons licenses
				for (ExtendedOutlink extendedOutlink : outlinks) {
					try {
						URL url = new URL(extendedOutlink.getToUrl().toString());//.getAnchor());              	// resolve the url
						// check that it's a CC license URL
						if ((HTTP_PROTOCOL.equalsIgnoreCase(url.getProtocol())
								| HTTPS_PROTOCOL.equalsIgnoreCase(url.getProtocol())) &&
								CREATIVECOMMONS_ORG_STR.equalsIgnoreCase(url.getHost()) &&
								url.getPath() != null &&
								url.getPath().startsWith(LICENSES_STR) &&
								url.getPath().length() > LICENSES_STR.length()) {
							_metadata.set(Metadata.LICENSE_URL, default_CCcomment_in_url+text_cc_separ+url.toString());
							found_license=true;
							break;
						}
					} catch (Exception e) {
						LOGGER.debug("reached");
					}
				}
				//if a creative commons license is mentioned in the content 
				//if (!found_license){
				//	if (content.contains(CC_pattern)){
				//		_metadata.set(Metadata.LICENSE_URL, default_CCcomment_in_text+text_cc_separ+default_CCurl_in_text);	
				//	}
				//}
			}
			LOGGER.debug(_metadata.get(Metadata.CONTENT_LOCATION) + _metadata.get(Metadata.LICENSE_URL));
			return new ExtendedParsedDatum(_metadata.get(Metadata.RESOURCE_NAME_KEY), null, content, lang,
					_metadata.get(Metadata.TITLE), outlinks,makeMap(_metadata));
		} catch (Exception e) {
			// Generic exception that's OK to re-throw
			throw e;
		} catch (NoSuchMethodError e) {
			throw new RuntimeException("Attempting to use excluded parser");
		} catch (Throwable t) {
			// Make sure nothing inside Tika can kill us
			throw new RuntimeException("Serious shut-down error thrown from Tika", t);
		}
	}

	/**
	 * Decide if we need to set up our own HtmlMapper, because the link extractor has tags that
	 * aren't part of the default set.
	 * 
	 * @return
	 */
	private ParseContext makeParseContext() {
		ParseContext result = new ParseContext();       
		HtmlMapper defaultMapper = DefaultHtmlMapper.INSTANCE;        
		result.set(HtmlMapper.class, defaultMapper);
		return result;
	}

	/**
	 * See if a language was set by the parser, from meta tags.
	 * As a last resort falls back to the result from the ProfilingHandler.
	 *  
	 * @param metadata
	 * @param profilingHandler
	 * @return The first language found (two char lang code) or empty string if no language was detected.
	 */
	private static String detectLanguageFromMetadata(Metadata metadata, ProfilingHandler profilingHandler) {
		String result = null;

		String dubCoreLang = metadata.get(Metadata.LANGUAGE);
		String httpEquivLang = metadata.get(Metadata.CONTENT_LANGUAGE);

		if (dubCoreLang != null) {
			result = dubCoreLang;
		} else if (httpEquivLang != null) {
			result = httpEquivLang;
		}

		result = getFirstLanguage(result);
		//TODO Do we need this?
		/*if (result == null) {
            // Language is still unspecified, so use ProfileHandler's result
            LanguageIdentifier langIdentifier = profilingHandler.getLanguage();
            // FUTURE KKr - provide config for specifying required certainty level.
            if (langIdentifier.isReasonablyCertain()) {
                result = langIdentifier.getLanguage();
                LOGGER.trace("Using language specified by profiling handler: " + result);
            } else {
                result = "";
            }

        }*/
		if (result!=null)
			result = result.toLowerCase();

		return result;
	}

	private static String getFirstLanguage(String lang) {
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
	}

	private static Map<String, String> makeMap(Metadata metadata) {
		Map<String, String> result = new HashMap<String, String>();

		for (String key : metadata.names()) {
			result.put(key, metadata.get(key));
		}

		return result;
	}

}