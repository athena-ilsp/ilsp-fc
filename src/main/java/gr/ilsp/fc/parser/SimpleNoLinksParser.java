package gr.ilsp.fc.parser;

import gr.ilsp.fc.crawl.Crawler;
import gr.ilsp.fc.datums.ExtendedParsedDatum;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


//import org.apache.pdfbox.pdfparser.PDFParser;
//import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.Parser;
import org.apache.tika.utils.CharsetUtils;

import bixo.config.ParserPolicy;
import bixo.datum.FetchedDatum;
import bixo.fetcher.HttpHeaderNames;
import bixo.parser.BaseContentExtractor;
import bixo.parser.SimpleContentExtractor;
import bixo.utils.HttpUtils;
import bixo.utils.IoUtils;
import gr.ilsp.fc.langdetect.LangDetector;
import gr.ilsp.fc.utils.EncodingDetector;


public class SimpleNoLinksParser implements Serializable, Callable<ExtendedParsedDatum> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6644451796914124629L;

	private static final Logger LOGGER = Logger.getLogger(SimpleNoLinksParser.class);

	private boolean _extractLanguage = true;
	protected BaseContentExtractor _contentExtractor;
	protected LangDetector _langDetector;
	private transient Parser _parser;
	//private transient Parser _parser1;
	private ParserPolicy _policy;
	private FetchedDatum _datum;

	private boolean _keepBoiler = false;
	private boolean _extractLinks = true;
	private String[] _targeted_langs;
	private HashMap<String,String> _maplangs;
	private List<String[]> _tranlistAttrs;
	private String _storedir_path;
	private  String _urlfilterstr;
	private static String pdfmime = "application/pdf";
	private static String docmime = "application/msword";
	private static String docmime1 = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

	public SimpleNoLinksParser(FetchedDatum datum) {
		this();
		_datum = datum;    	
	}
	public SimpleNoLinksParser() {
		this(new ParserPolicy());
	}
	public SimpleNoLinksParser(boolean keepBoiler, String storedir_path, HashMap<String,String> maplangs, 
			List<String[]> tranlistAttrs, String[] targeted_langs, LangDetector langDetector, String urlfilterstr, boolean extractLinks) {    	
		this(new ParserPolicy());
		_keepBoiler  = keepBoiler;
		_storedir_path = storedir_path;
		_targeted_langs = targeted_langs;
		_maplangs = maplangs;
		_tranlistAttrs = tranlistAttrs;
		_urlfilterstr = urlfilterstr;
		_extractLinks = extractLinks;
		_langDetector = langDetector;
	}
	public SimpleNoLinksParser(ParserPolicy parserPolicy) {
		this(new SimpleContentExtractor(),  parserPolicy);
	}

	/**
	 * @param contentExtractor to use instead of new {@link SimpleContentExtractor}()
	 * @param linkExtractor to use instead of new {@link SimpleLinkExtractor}()
	 * @param parserPolicy to customize operation of the parser
	 * <BR><BR><B>Note:</B> There is no need to construct your own
	 * {@link SimpleLinkExtractor} simply to control the set of link tags
	 * and attributes it processes. Instead, use {@link ParserPolicy#setLinkTags}
	 * and {@link ParserPolicy#setLinkAttributeTypes}, and then pass this policy
	 * to {@link SimpleParser#SimpleParser(ParserPolicy)}.
	 */
	public SimpleNoLinksParser(BaseContentExtractor contentExtractor, ParserPolicy parserPolicy) {
		_policy = parserPolicy;
		_contentExtractor = contentExtractor;        
	}

	protected synchronized void init() {
		if (_parser == null) {
			_parser = getTikaParser();
		}
		_contentExtractor.reset();        
	}


	public Parser getTikaParser() {
		return new AutoDetectParser();
	}

	public void setExtractLanguage(boolean extractLanguage) {
		_extractLanguage = extractLanguage;
	}

	public boolean isExtractLanguage() {
		return _extractLanguage;
	}


	public ExtendedParsedDatum parse(FetchedDatum fetchedDatum) throws Exception {
		init();

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace(String.format("Parsing %s", fetchedDatum.getUrl()));
		}
		LOGGER.debug(fetchedDatum.getUrl());
		// Provide clues to the parser about the format of the content.
		Metadata metadata = new Metadata();
		metadata.add(Metadata.RESOURCE_NAME_KEY, fetchedDatum.getUrl());
		metadata.add(Metadata.CONTENT_TYPE, fetchedDatum.getContentType());
		String encoding1 = getCharset(fetchedDatum);

		InputStream is = new ByteArrayInputStream(fetchedDatum.getContentBytes(), 0, fetchedDatum.getContentLength());
		EncodingDetector encdet = new EncodingDetector();
		encdet.initialize();
		String encoding2="";
		Charset charset2 = encdet.detect(is);
		if (charset2!=null){
			encoding2 = charset2.name();
		}
		if (encoding1!=null && encoding2!=null){
			if (!encoding1.equals(encoding2)){
				LOGGER.warn("ENCODING ISSUE:\t"+fetchedDatum.getUrl()+"\t"+encoding1 +"\t"+encoding2);		
				Crawler.incrementEncodingIssues();	
			}
		}
		metadata.add(Metadata.CONTENT_LANGUAGE, getLanguage(fetchedDatum, encoding1));
		//metadata.add(Metadata.CONTENT_ENCODING, charset2);
		metadata.add(Metadata.CONTENT_ENCODING, encoding1);

		metadata.add(Metadata.CONTENT_LENGTH, Integer.toString(fetchedDatum.getContentLength()));
		try {
			URL baseUrl = getContentLocation(fetchedDatum);
			LOGGER.debug(baseUrl +"\t"+ metadata.get("Content-Type"));
			metadata.add(Metadata.CONTENT_LOCATION, baseUrl.toExternalForm());
			Callable<ExtendedParsedDatum> callable;
			if (metadata.get("Content-Type").contains(pdfmime)){	
				_parser=null; 
				LOGGER.debug("pdf reached");
				callable = new PdfboxCallableParser(_parser, _contentExtractor,  is, metadata, isExtractLanguage(), _keepBoiler, _storedir_path);
			}else{
				if (metadata.get("Content-Type").contains(docmime) || metadata.get("Content-Type").contains(docmime1)){
					_parser=null; 
					LOGGER.debug("msword reached");
					callable = new MsWordCallableParser(_parser, _contentExtractor,  is, metadata, isExtractLanguage(), _keepBoiler, _storedir_path);
				}else{
					callable = new TikaCallableParser(_parser, _contentExtractor,  is, metadata, isExtractLanguage(), _maplangs,
							_tranlistAttrs, _targeted_langs, _langDetector, _keepBoiler, _urlfilterstr,_extractLinks);
					//callable = new TikaCallableParser(_parser, _contentExtractor,  is, metadata, isExtractLanguage(), _maplangs, _tranlistAttrs, _targeted_langs, _keepBoiler);
				}
			}
			//Callable<ExtendedParsedDatum> c = new TikaCallableParser(_parser, _contentExtractor,  is, metadata, isExtractLanguage(), _keepBoiler);
			FutureTask<ExtendedParsedDatum> task = new FutureTask<ExtendedParsedDatum>(callable);
			Thread t = new Thread(task);
			t.start();

			ExtendedParsedDatum result;
			try {
				//if (metadata.get("Content-Type").equals("application/pdf")){
				//	result = task.get(300000, TimeUnit.MILLISECONDS);
				//}else{
				//	result = task.get(30000, TimeUnit.MILLISECONDS);
				//}
				//if (!metadata.get("Content-Type").equals("application/pdf")){
				result = task.get(getParserPolicy().getMaxParseDuration(), TimeUnit.MILLISECONDS);
				//}
			} catch (TimeoutException e) {
				LOGGER.error("Exception thrown when a blocking operation times out");
				task.cancel(true);
				t.interrupt();
				throw e;
			} finally {
				t = null;
			}
			//result.setParsedText(ArticleExtractor.INSTANCE.getText(new InputStreamReader(is)));
			// TODO KKr Should there be a BaseParser to take care of copying
			// these two fields?
			result.setHostAddress(fetchedDatum.getHostAddress());
			result.setPayload(fetchedDatum.getPayload());
			return result;
		} finally {
			IoUtils.safeClose(is);
		}
	}

	protected URL getContentLocation(FetchedDatum fetchedDatum) throws MalformedURLException {
		URL baseUrl = new URL(fetchedDatum.getFetchedUrl());

		// See if we have a content location from the HTTP headers that we should use as
		// the base for resolving relative URLs in the document.
		String clUrl = fetchedDatum.getHeaders().getFirst(HttpHeaderNames.CONTENT_LOCATION);
		if (clUrl != null) {
			// FUTURE KKr - should we try to keep processing if this step fails, but
			// refuse to resolve relative links?
			baseUrl = new URL(baseUrl, clUrl);
		}
		return baseUrl;
	}

	/**
	 * Extract encoding from content-type
	 * 
	 * If a charset is returned, then it's a valid/normalized charset name that's
	 * supported on this platform.
	 * 
	 * @param datum
	 * @return charset in response headers, or null
	 */
	protected String getCharset(FetchedDatum datum) {

		String enc1 = CharsetUtils.clean(HttpUtils.getCharsetFromContentType(datum.getContentType()));

		return enc1;
	}

	/**
	 * Extract language from (first) explicit header
	 * 
	 * @param fetchedDatum
	 * @param charset 
	 * @return first language in response headers, or null
	 */
	protected String getLanguage(FetchedDatum fetchedDatum, String charset) {
		return fetchedDatum.getHeaders().getFirst(HttpHeaderNames.CONTENT_LANGUAGE);
	}


	public ParserPolicy getParserPolicy() {
		return _policy;
	}

	@Override
	public ExtendedParsedDatum call() throws Exception {
		// TODO Auto-generated method stub
		return parse(_datum);
	}
}
