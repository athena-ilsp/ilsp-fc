/*
 * Copyright (c) 2010 TransPac Software, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy 
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights 
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell 
 * copies of the Software, and to permit persons to whom the Software is 
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package gr.ilsp.fc.exporter;

import gr.ilsp.fc.classifier.Classifier;
import gr.ilsp.fc.cleaner.CleanerUtils;
import gr.ilsp.fc.datums.ClassifierDatum;
import gr.ilsp.fc.datums.CrawlDbDatum;
import gr.ilsp.fc.datums.ExtendedParsedDatum;
import gr.ilsp.fc.extractors.MSO2text;
import gr.ilsp.fc.extractors.Pdf2text;
import gr.ilsp.fc.langdetect.LangDetectUtils;
import gr.ilsp.fc.main.Crawl;
import gr.ilsp.fc.main.WriteResources;
import gr.ilsp.fc.utils.AnalyzerFactory;
import gr.ilsp.fc.utils.ContentNormalizer;
import gr.ilsp.fc.utils.CrawlConfig;
import gr.ilsp.fc.utils.FCStringUtils;
import gr.ilsp.fc.utils.FcFileUtils;
import gr.ilsp.fc.utils.ISOLangCodes;
import gr.ilsp.fc.utils.PrettyPrintHandler;
import gr.ilsp.fc.utils.TempUtils;
import gr.ilsp.fc.utils.TopicTools;
//import gr.ilsp.fc.genreclassifier.GenreClassifier;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.JobConf;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.tika.metadata.Metadata;
import org.codehaus.stax2.XMLOutputFactory2;
import org.codehaus.stax2.XMLStreamWriter2;

import bixo.datum.FetchedDatum;
import bixo.utils.CrawlDirUtils;
import cascading.scheme.SequenceFile;
import cascading.tap.Hfs;
import cascading.tap.Tap;
import cascading.tuple.TupleEntry;
import cascading.tuple.TupleEntryIterator;

@SuppressWarnings("deprecation")
public class Exporter {
	private static final Logger LOGGER = Logger.getLogger(Exporter.class);
	//private static String VAR_RES_CACHE = "/var/lib/tomcat6/webapps/soaplab2-results/";
	//private static final String HTTP_PATH = "http://nlp.ilsp.gr/soaplab2-results/";	
	private static final String cesDocVersion = "0.4";
	private static final String cesNameSpace = "http://www.w3.org/1999/xlink";
	private static final String cesNameSpace1 = "http://www.xces.org/schema/2003";
	private static final String cesNameSpace2 = "http://www.w3.org/2001/XMLSchema-instance";
	//private static final String XMLlist = ".xmllist.txt";

	private static final String xml_type="xml";
	private static final String pdf_type="pdf";
	private static final String doc_type="doc";
	private static final String tag_type="type";
	private static final String tag_crawlinfo="crawlinfo";
	private static final String attr_boilerplateV = "boilerplate";
	private static final String attr_titleV = "title";
	private static final String attr_lengthV = "ooi-length";
	private static final String attr_langV = "ooi-lang";
	private static final String attr_negV = "ooi-neg";
	private static final String appXMLext = ".xml";
	private static final String appHTMLext=".html";
	private static final String appPDFext=".pdf";
	private static final String appDOCext =".doc";
	private static final String separator = ";";
	private static final String HYPHEN="-";
	private static final String p_type = "p";
	private static final String morethan = ">" ; 
	private static final String lessthan = "<" ; 
	private static final String text_st = "<text";
	private static final String text_tag = "<text>";
	private static final String text_tag_en = "</text>";
	private static final String boiler_tag = "<boiler>";
	private static final String boiler_st = "<boiler";
	private static final String XMLlist = ".xmllist.txt";
	private static final String CSV = ".csv";
	private static final String XMLHTMLlist = ".xmllist.html";
	private static final String pdfmime = "pdf";
	private static final String docmime = "word";

	private static String year = Integer.toString(Calendar.getInstance().get(Calendar.YEAR));

	private static int MIN_TOKENS_PER_PARAGRAPH;
	private static int depth=10000;
	private static int MIN_TOKENS_NUMBER;
	private static String[] targetlanguages;

	public static CompositeConfiguration config;
	private static File negWordsFile;
	private static File crawlDirName;
	private static File topic;
	private static File outputDir=null;
	private static File outBaseName = null;
	private static File outputFile = null;
	private static File outputFileHTML = null;
	private static boolean textExport = false;

	private static boolean applyOfflineXSLT = false;
	public static XSLTransformer xslTransformer = null;
	private static boolean offline = false;
	private static boolean httrack = false;
	private static String[] mimetypes;
	private static String targeteddomain;
	private static URL genres;
	private static ExporterOptions options = null;
	static Analyzer analyzer = null;
	static AnalyzerFactory analyzerFactory = new AnalyzerFactory();
	//private static HashMap<String, String> genres_keys = null;
	private static List<File> xmlFiles = new ArrayList<File>();

	private static String researchProject = "ILSP";

	private static String identifiedlanguage;
	private static String title = "";
	private static String cleanText = "";
	private static String htmlText = "";
	private static String genre="";
	private static String format = "";	
	private static String subdomains = "";
	//private static String contentEncoding = "";
	private static String author ="";
	private static String licenseURL="";
	private static String extfilename="";
	private static String publisher="";
	private static String url = "";
	private static double relscore;

	private static boolean _sort_type = false;
	private static ArrayList<String> terms = null;
	private static Map<String,String> meta = null;
	private Map<String, String> urlsToIds;
	private static Map<String, Integer> langnumMap = new HashMap<String, Integer>(); 

	private static void processCrawlDb(JobConf conf, Path curDirPath, boolean exportDb) throws IOException {
		//TupleEntryIterator iter;
		int totalEntries;
		Path crawlDbPath = new Path(curDirPath, CrawlConfig.CRAWLDB_SUBDIR_NAME);
		Tap crawldbTap = new Hfs(new SequenceFile(CrawlDbDatum.FIELDS), crawlDbPath.toUri().toString());
		TupleEntryIterator iter = crawldbTap.openForRead(conf);
		totalEntries = 0;
		int fetchedUrls = 0;
		int unfetchedUrls = 0;
		LOGGER.info("!!!! PRINTING CRAWLDB !!!!");
		while (iter.hasNext()) {
			TupleEntry entry = iter.next();
			totalEntries += 1;			
			CrawlDbDatum datum = new CrawlDbDatum(entry);
			if (exportDb) {
				LOGGER.info(datum.toString());
			}
			if (datum.getLastFetched() == 0) {
				unfetchedUrls += 1;
			} else {
				fetchedUrls += 1;
			}
		}
		//LOGGER.info("!!!! PRINTING CLASSIFIED !!!!");
		int prevLoop = -1;
		Path crawlDirPath = curDirPath.getParent();
		FileSystem fs = crawlDirPath.getFileSystem(conf);
		while ((curDirPath = CrawlDirUtils.findNextLoopDir(fs, crawlDirPath, prevLoop)) != null) {
			int curLoop = CrawlDirUtils.extractLoopNumber(curDirPath);
			if (curLoop != prevLoop + 1) {
				LOGGER.warn(String.format("Missing directories between %d and %d", prevLoop, curLoop));
			}
			//Path classifiedPath = new Path(curDirPath, CrawlConfig.CLASSIFIER_SUBDIR_NAME);
			//Tap classifiedTap = new Hfs(new SequenceFile(ClassifierDatum.FIELDS),classifiedPath.toUri().toString());
			//iter = classifiedTap.openForRead(conf);
			prevLoop = curLoop;
		}

		if (!exportDb) {
			LOGGER.info(String.format("%d fetched URLs", fetchedUrls));
			LOGGER.info(String.format("%d unfetched URLs", unfetchedUrls));
			LOGGER.info("Total URLs: " + totalEntries);
			LOGGER.info("");
		}
		//fs.close();
	}

	public Map<String,Integer> export(boolean loadProfile) {
		long start = System.currentTimeMillis();
		LOGGER.info("------------Exporting cesDoc Files------------");
		for (int ii=0;ii<targetlanguages.length;ii++){
			targetlanguages[ii] = ISOLangCodes.get3LetterCode(targetlanguages[ii]);
			langnumMap.put(targetlanguages[ii], 0);
		}
		outputFile = new File(outBaseName.getAbsolutePath()+XMLlist);
		if (applyOfflineXSLT)
			outputFileHTML = new File(outBaseName.getAbsolutePath()+XMLHTMLlist);
		//get array of forbidden words
		List<String> neg_words = null ;
		if (getNegWordsFile() != null) {
			try {
				neg_words = FileUtils.readLines(getNegWordsFile());
			} catch (IOException e) {
				LOGGER.info("problem in reading file with negative words");
				e.printStackTrace();
			}
		}
		//URL genreFile = getGenres();
		//genres_keys = GenreClassifier.Genres_keywords(genreFile);	
		File topicFile = getTopic();
		ArrayList<String[]> topic = null;
		if (topicFile!=null)
			topic=TopicTools.analyzeTopic(topicFile,targetlanguages); 
		String[] ext = {xml_type}; 

		if (offline)// OFFLINE PROCESS SHOULD BE FIXED
			offlineExport(neg_words, ext, topic);
		else{
			if (!new File(FilenameUtils.concat(crawlDirName.getAbsolutePath(),pdf_type)).exists())
				LOGGER.info("No pdf files fetched");
			else{
				File[] files = new File(FilenameUtils.concat(crawlDirName.getAbsolutePath(),pdf_type)).listFiles();
				LOGGER.info(files.length + " pdf files fetched");
			}
			if (!new File(FilenameUtils.concat(crawlDirName.getAbsolutePath(),doc_type)).exists())
				LOGGER.info("No doc files fetched");
			else{
				File[] files = new File(FilenameUtils.concat(crawlDirName.getAbsolutePath(),doc_type)).listFiles();
				LOGGER.info(files.length + " doc files fetched");
			}
			try {
				JobConf conf = new JobConf();
				Path crawlDirPath = new Path(crawlDirName.getAbsolutePath());
				FileSystem fs = crawlDirPath.getFileSystem(conf);
				if (!fs.exists(crawlDirPath)) {
					LOGGER.error("Prior crawl output directory does not exist: " + crawlDirName);
					System.exit(-1);
				}
				// Skip Hadoop/Cascading DEBUG messages.
				Logger.getRootLogger().setLevel(Level.INFO);
				//Exporting
				int prevLoop = -1;
				Path curDirPath = null;
				int id = 1;
				while ((curDirPath = CrawlDirUtils.findNextLoopDir(fs, crawlDirPath, prevLoop)) != null) {
					LOGGER.info("current rundir: " +curDirPath);
					id = exportToXml(conf,curDirPath, id,topic,targeteddomain, urlsToIds, neg_words);
					LOGGER.debug("Current loop path in xml export is " + curDirPath );
					int curLoop = CrawlDirUtils.extractLoopNumber(curDirPath);
					if (curLoop != prevLoop + 1) 
						LOGGER.warn(String.format("Missing directories between %d and %d", prevLoop, curLoop));
					prevLoop = curLoop;
					//if (curLoop>depth)
					//	break;
				}
				//xmlFiles = FcFileUtils.getFilesList(new File(FilenameUtils.concat(crawlDirName.getAbsolutePath(),xml_type)), "", appXMLext);
				File tf = new File(FilenameUtils.concat(crawlDirName.getAbsolutePath(),xml_type));
				if (!tf.exists())
					tf.mkdir();
				xmlFiles =  (List<File>) FileUtils.listFiles(tf, ext, true);
				//Path latestCrawlDirPath = CrawlDirUtils.findLatestLoopDir(fs, crawlDirPath);
				//processCrawlDb(conf, latestCrawlDirPath, true);	//exportDb
				//fs.close();
			} catch (Throwable t) {
				LOGGER.error("Exception running tool", t);
				System.exit(-1);
			}
		}
		LOGGER.info("CesDoc files generated: "+ xmlFiles.size());
		LOGGER.info("Completed in " + (System.currentTimeMillis()-start) + " milliseconds.");
		if (outputFile!=null){
			WriteResources.WriteTextList(xmlFiles, outputFile);
			LOGGER.info("Created list of cesDoc in "+ outputFile.getAbsolutePath());
			if (applyOfflineXSLT){
				WriteResources.WriteHTMLList(xmlFiles, new File(outputFileHTML.getAbsolutePath()));
				LOGGER.info("Created list of rendered cesDoc in "+ outputFileHTML.getAbsolutePath());
			}
		}
		return langnumMap;
	}

	public static void generateCSV(File file, Map<String, Integer> map) {
		Set<String> keys=map.keySet();
		Iterator<String> it = keys.iterator();
		String file_key, csvtext="";
		while (it.hasNext()){
			file_key = it.next();
			csvtext = csvtext+file_key+"\t"+map.get(file_key)+"\n";
		}
		try {
			FileUtils.writeStringToFile(file, csvtext);
		} catch (IOException e) {
			LOGGER.error("problem in writing the file "+file.getAbsolutePath());
			e.printStackTrace();
		}
	}

	private void offlineExport(List<String> neg_words, String[] ext, ArrayList<String[]> topic) {
		String[] mimes = config.getStringArray("fetcher.valid_mime_types.mime_type[@value]");			
		Set<String> validMimeTypes = new HashSet<String>();
		for (String s: mimes) validMimeTypes.add(s);

		if (!outputDir.exists())
			outputDir.mkdirs();

		int id=0;
		if (!crawlDirName.exists()){
			LOGGER.error("The directory " +crawlDirName.getAbsolutePath() + " does not exist.");
			System.exit(0);
		}
		boolean keepBoiler=true;
		List<File> files = (List<File>) FileUtils.listFiles(crawlDirName, null, false);
		for (File file:files){
			String maincontent="";
			format =  getValidFormat(FcFileUtils.MimeDetect(file.getAbsolutePath()));
			if (!validMimeTypes.contains(format))
				continue;
			if (format.contains(pdfmime) || format.contains(docmime)){
				Map<String, String> data = new HashMap<String, String>();
				if (format.contains(pdfmime))
					data = Pdf2text.run1(file, _sort_type);
				if (format.contains(docmime))
					data = MSO2text.run1(file);
				cleanText = data.get("content");
				if (StringUtils.isBlank(cleanText)){
					LOGGER.info("No text extracted from " + file.getAbsolutePath());
					continue;
				}
				cleanText = ContentNormalizer.normalizeText(cleanText);
				maincontent = cleanText;
				maincontent = maincontent.replaceAll(text_tag, "");
				maincontent = maincontent.replaceAll(text_tag_en, "");
				identifiedlanguage =  LangDetectUtils.detectLanguage(maincontent.toLowerCase());
				if (!LangDetectUtils.istargetedlang(identifiedlanguage, targetlanguages)){
					LOGGER.info(file.getAbsolutePath()+ " not in targeted languages.");
					continue;
				}
				int length_in_tok=FCStringUtils.countTokens(maincontent, identifiedlanguage);
				//int length_in_tok=maincontent.length();
				if (length_in_tok<MIN_TOKENS_NUMBER){
					LOGGER.info("very short text extracted from " + file.getAbsolutePath());
					continue;		
				}
				LOGGER.debug(file.getAbsolutePath()+ " processed.");
				//LOGGER.debug("Writing: " +identifiedlanguage+HYPHEN+ id + "\t" + url);
				author = data.get("author");
				title = data.get("title");
				publisher = data.get("publisher");
				String termsArray = data.get("keywords");
				terms = new ArrayList<String>();
				if (termsArray!=null){
					for (String s: termsArray.split(",|;|:"))
						terms.add(s.trim());
				}
				if (topic!=null){//check domainess //FIXME terms.toString()
					if (Classifier.classifyText(title, terms.toString(), "", maincontent, identifiedlanguage, url, length_in_tok)==null)
						continue;
				}
				htmlText =""; 
				url = file.getName();
			}else{
				try {
					byte[] array = FileUtils.readFileToByteArray(file);
					InputStream input = new ByteArrayInputStream(array);
					cleanText = CleanerUtils.getContent(input, keepBoiler);
					String identifiedlanguage = LangDetectUtils.detectLanguage(CleanerUtils.cleanContent(cleanText));
					if (!LangDetectUtils.istargetedlang(identifiedlanguage,targetlanguages))
						continue;
					if (httrack){//FIXME
						input.reset();
						url = TempUtils.handleCopiedSite(input);
					}
				} catch (IOException e1) {
					LOGGER.error("problem in getting byte[] for file "+ file.getAbsolutePath());
					e1.printStackTrace();
				}
			}
			XMLExporter(new File(FilenameUtils.concat(outputDir.getAbsolutePath(),identifiedlanguage+"-"+id)), format, title, url, targetlanguages,
					identifiedlanguage, cleanText, "", author, publisher, targeteddomain, subdomains, terms, topic, neg_words, licenseURL, genre,
					relscore, extfilename);
			id++;
		}
		//xmlFiles =(List<File>) FileUtils.listFiles(crawlDirName, ext, false);
		xmlFiles =(List<File>) FileUtils.listFiles(outputDir, ext, false);
	}

	private static int exportToXml(JobConf conf, Path curDirPath,  int id, ArrayList<String[]> topic, String targeteddomain, Map<String, String> urlsToIds, List<String> neg_words) throws IOException {
		TupleEntryIterator iter;
		initValues();
		//List<String> urls = new ArrayList<String>();
		Path parseDbPath = new Path(curDirPath, CrawlConfig.PARSE_SUBDIR_NAME);
		Tap parseDbTap = new Hfs(new SequenceFile(ExtendedParsedDatum.FIELDS), parseDbPath.toUri().toString());	
		Path contentPath = new Path(curDirPath,CrawlConfig.CONTENT_SUBDIR_NAME);
		Tap contentDbTap = new Hfs(new SequenceFile(FetchedDatum.FIELDS), contentPath.toUri().toString());
		Path classifierPath = new Path(curDirPath, CrawlConfig.CLASSIFIER_SUBDIR_NAME);
		Tap classifierDbTap = new Hfs(new SequenceFile(ClassifierDatum.FIELDS), classifierPath.toUri().toString());
		TupleEntryIterator classIter = classifierDbTap.openForRead(conf);
		TupleEntryIterator classIter1 = classifierDbTap.openForRead(conf);

		/*Path crawlDbPath = new Path(curDirPath, CrawlConfig.CRAWLDB_SUBDIR_NAME);
		Tap crawldbTap = new Hfs(new SequenceFile(CrawlDbDatum.FIELDS), crawlDbPath.toUri().toString());
		TupleEntryIterator itercrawl = crawldbTap.openForRead(conf);
		while (itercrawl.hasNext()) {
			TupleEntry entry = itercrawl.next();
			CrawlDbDatum datumcrawl = new CrawlDbDatum(entry);
			//System.out.println(datum.getLastStatus().toString());
			if (datumcrawl.getLastFetched() == 0) 
				continue;
			else{
				if (datumcrawl.getCrawlDepth()<=depth) 
					urls.add(datumcrawl.getUrl());
			}
		}*/

		Path xmlPath = null; //FIXME PUT IT OUT OF THE LOOP
		if (outputDir==null)
			xmlPath = new Path(curDirPath.getParent(), CrawlConfig.XML_SUBDIR_NAME);
		else 
			xmlPath = new Path(outputDir.getAbsolutePath());

		FileSystem fs = xmlPath.getFileSystem(conf);
		if (!fs.exists(xmlPath))
			fs.mkdirs(xmlPath);
		TupleEntryIterator contentIter = contentDbTap.openForRead(conf);
		iter = parseDbTap.openForRead(conf);

		while (iter.hasNext()) {
			TupleEntry entry = iter.next();
			ExtendedParsedDatum datum = new ExtendedParsedDatum(entry);
			meta = datum.getParsedMeta();
			url = datum.getUrl();
			//if (!urls.contains(url)){
			//	System.out.println("not fetched?:\t"+url);
			//}else{
			//	System.out.println("fetched:\t"+url);
			//}
			/*CrawlDbDatum crawldatum = new CrawlDbDatum(entry);
			LOGGER.info(url+"\t"+(crawldatum.getCrawlDepth()));
			if (crawldatum.getCrawlDepth()>depth)
				continue;*/

			licenseURL = meta.get(Metadata.LICENSE_URL);			if (licenseURL==null){licenseURL="";}
			format = meta.get("Content-Type");				format = getValidFormat(format);
			if (format.contains(pdfmime) || format.contains(docmime)){
				if (!getEXTFileInfo( id, topic, format))
					continue;
				htmlText =""; 
			}else
				getHTMLInfo(datum, meta, id, curDirPath, classIter, classIter1, contentIter);

			if (StringUtils.isBlank(identifiedlanguage))	//FIXME this should not happen
				continue;

			if (langnumMap.containsKey(identifiedlanguage))
				langnumMap.put(identifiedlanguage, langnumMap.get(identifiedlanguage)+1);

			if (XMLExporter(xmlPath,format, title, url, targetlanguages, identifiedlanguage, htmlText, cleanText,id, "", author,
					publisher, targeteddomain, subdomains, terms, topic, neg_words, licenseURL, genre,relscore, extfilename)){
				if (urlsToIds!=null)
					urlsToIds.put(datum.getUrl(), identifiedlanguage+HYPHEN+id);
			}
			id++;
			//LOGGER.info("EXPORTED:\t"+url+"\t"+identifiedlanguage);
			LOGGER.debug("EXPORTED:\t"+url+"\t"+identifiedlanguage);
			if (textExport) TextExporter(xmlPath,cleanText,id-1, identifiedlanguage);
		}
		iter.close();
		classIter.close();
		contentIter.close();
		return id;		
	}


	private static boolean getEXTFileInfo(int id, ArrayList<String[]> topic2, String format) {
		boolean done=true;
		extfilename = meta.get("comment");
		if (!new File(extfilename).exists())
			return false;
		LOGGER.debug(extfilename);

		Map<String, String> data = new HashMap<String, String>();
		if (format.contains(docmime))
			data = MSO2text.run1(new File(extfilename));
		if (format.contains(pdfmime))
			data = Pdf2text.run1(new File(extfilename), _sort_type);
		if (data.isEmpty()){
			LOGGER.info("Text Conversion failed." + extfilename);
			return false;
		}
		cleanText = data.get("content");
		if (StringUtils.isBlank(cleanText)){
			LOGGER.info("Text Conversion failed." + extfilename);
			return false;
		}
		cleanText = ContentNormalizer.normalizeText(cleanText);
		String maincontent = cleanText;
		maincontent = maincontent.replaceAll(text_tag, "");
		maincontent = maincontent.replaceAll(text_tag_en, "");
		identifiedlanguage =  LangDetectUtils.detectLanguage(maincontent.toLowerCase());
		if (!LangDetectUtils.istargetedlang(identifiedlanguage, targetlanguages))
			return false;
		int length_in_tok=FCStringUtils.countTokens(maincontent, identifiedlanguage);
		//int length_in_tok=maincontent.length();
		if (length_in_tok<MIN_TOKENS_NUMBER)
			return false;
		LOGGER.debug(extfilename+ " processed.");
		LOGGER.debug("Writing: " +identifiedlanguage+HYPHEN+ id + "\t" + url);

		author = data.get("author");
		title = data.get("title");
		publisher = data.get("publisher");
		String termsArray = data.get("keywords");
		terms = new ArrayList<String>();
		if (termsArray!=null){
			for (String s: termsArray.split(",|;|:"))
				terms.add(s.trim());
		}
		if (topic!=null)//check domainess //FIXME terms.toString()
			Classifier.classifyText(title, terms.toString(), "", maincontent, identifiedlanguage, url, length_in_tok); 

		return done;
	}

	/*private static boolean getDOCInfo(int id, ArrayList<String[]> topic2) {
		boolean done=true;
		extfilename = meta.get("comment");
		if (!new File(extfilename).exists())
			return false;
		LOGGER.info(extfilename);
		Map<String, String> data = MSO2text.run1(new File(extfilename));
		cleanText = data.get("content");
		if (StringUtils.isBlank(cleanText)){
			LOGGER.info("DOC to Text Conversion failed." + extfilename);
			return false;
		}
		cleanText = ContentNormalizer.normalizeText(cleanText);
		String maincontent = cleanText;
		maincontent = maincontent.replaceAll(text_tag, "");
		maincontent = maincontent.replaceAll(text_tag_en, "");
		int length_in_tok=FCStringUtils.countTokens(maincontent);
		if (length_in_tok<MIN_TOKENS_NUMBER)
			return false;		
		identifiedlanguage =  LangDetectUtils.detectLanguage(maincontent.toLowerCase());
		if (!LangDetectUtils.istargetedlang(identifiedlanguage, targetlanguages))
			return false;
		LOGGER.info(extfilename+ " processed.");
		LOGGER.debug("Writing: " +identifiedlanguage+HYPHEN+ id + "\t" + url);
		author = data.get("author");
		title = data.get("title");
		publisher = data.get("publisher");
		String termsArray = data.get("keywords");
		terms = new ArrayList<String>();
		if (termsArray!=null){
			for (String s: termsArray.split(",|;|:"))
				terms.add(s.trim());
		}
		if (topic!=null)//check domainess //FIXME terms.toString()
			Classifier.classifyText(title, terms.toString(), "", maincontent, identifiedlanguage, url, length_in_tok); 

		return done;
	}*/

	/*	private static boolean getPDFInfo(int id, ArrayList<String[]> topic ) {
		boolean done=true;
		extfilename = meta.get("comment"); //pdfname = pdfname.replaceAll("/var/www/html/elrc8/ministries/eng-slk", "C:/Users/vpapa/ELRC/public_admin/ENG-SLK");
		if (!new File(extfilename).exists())
			return false;
		LOGGER.info(extfilename);
		Map<String, String> data = Pdf2text.run1(new File(extfilename), _sort_type);
		cleanText = data.get("content");		//cleanText = Pdf2text.run1(new File(extfilename), _sort_type);
		if (StringUtils.isBlank(cleanText)){
			LOGGER.info("PDF to Text Conversion failed." + extfilename);
			return false;
		}
		cleanText = ContentNormalizer.normalizeText(cleanText);
		String maincontent = cleanText;
		maincontent = maincontent.replaceAll(text_tag, "");
		maincontent = maincontent.replaceAll(text_tag_en, "");
		int length_in_tok=FCStringUtils.countTokens(maincontent);
		if (length_in_tok<MIN_TOKENS_NUMBER){
			return false;		
		}
		identifiedlanguage =  LangDetectUtils.detectLanguage(maincontent.toLowerCase());
		if (!LangDetectUtils.istargetedlang(identifiedlanguage, targetlanguages)){
			return false;
		}
		LOGGER.info(extfilename+ " processed.");
		LOGGER.debug("Writing: " +identifiedlanguage+HYPHEN+ id + "\t" + url);
		author = data.get("author");
		title = data.get("title");
		publisher = data.get("publisher");
		String termsArray = data.get("keywords");
		terms = new ArrayList<String>();
		if (termsArray!=null){
			for (String s: termsArray.split(",|;|:"))
				terms.add(s.trim());
		}
		if (topic!=null)//check domainess 
			Classifier.classifyText(title, terms.toString(), "", maincontent, identifiedlanguage, url, length_in_tok); 

		PDDocument pdDoc=null;
		try {
			pdDoc = PDDocument.load(extfilename);
			PDDocumentInformation pdDocInfo=new PDDocumentInformation();
			pdDocInfo=pdDoc.getDocumentInformation();
			author= ContentNormalizer.normalizeText(pdDocInfo.getAuthor());
			title = ContentNormalizer.normalizeText(pdDocInfo.getTitle());
			publisher = ContentNormalizer.normalizeText(pdDocInfo.getProducer());
			String termsArray = pdDocInfo.getKeywords();
			terms = new ArrayList<String>();
			if (termsArray!=null){
				for (String s: termsArray.split(",|;|:"))
					terms.add(s.trim());
			}
			if (topic!=null){//check domainess
				Classifier.classifyText(title, terms.toString(), "", maincontent, identifiedlanguage, url, length_in_tok);
			}
			if (pdDoc != null)
				pdDoc.close();
		} catch (Exception e){
			System.out.println("An exception occured in parsing the PDF Document.");
			e.printStackTrace();
		}
		return done;
	}*/

	/**
	 * Initialization of parameters holding content and metadata 
	 */
	private static void initValues() {
		identifiedlanguage="";		title = "";		cleanText = "";	 htmlText = ""; genre=""; format = "";  // pubdate="";
		subdomains = "";author =""; licenseURL=""; extfilename=""; publisher=""; url = "";
		relscore=0; 
		terms = null;
		meta = null;
	}

	/**
	 * Get information from a stored datum for HTML 
	 * @param datum
	 * @param meta2
	 * @param id
	 * @param curDirPath
	 * @param classIter
	 * @param classIter1
	 * @param contentIter
	 */
	private static void getHTMLInfo(ExtendedParsedDatum datum, Map<String, String> meta2, int id, Path curDirPath, TupleEntryIterator classIter, TupleEntryIterator classIter1, TupleEntryIterator contentIter) {

		identifiedlanguage = datum.getLanguage();
		LOGGER.debug("Writing: " +identifiedlanguage+HYPHEN+ id + "\t" + url);
		title = datum.getTitle();
		if (title==null) title = "";
		cleanText = datum.getParsedText();
		cleanText = ContentNormalizer.normalizeText(cleanText);
		author = ContentNormalizer.normalizeText(meta.get("Author"));
		publisher = ContentNormalizer.normalizeText(meta.get("Publisher"));
		String termsArray = meta.get("keywords");
		terms = new ArrayList<String>();
		if (termsArray!=null){
			for (String s: termsArray.split(",|;|:"))
				terms.add(s.trim());
		}
		subdomains = getSubdomains(url, curDirPath,classIter);
		relscore = getRelscore(url, curDirPath,classIter1);
		htmlText = meta.get("Comments");
		//genre = GenreClassifier.GenreClassifier_keywords(genres_keys, url, title);	
	}

	/**
	 * generates a text file containing the main text of the webpage  
	 * @param outpath
	 * @param text
	 * @param id
	 * @param identifiedlanguage
	 */
	public static void TextExporter(Path outpath, String text, int id, String identifiedlanguage){
		Path txt_file = new Path(outpath,identifiedlanguage+HYPHEN+id+".txt");
		try {
			BufferedWriter wrt = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(txt_file.toUri().getPath()),"UTF-8"));
			text = text.replaceAll("<boiler>.*</boiler>\r\n", "");
			text = text.replaceAll("<[^<]*>", "");
			wrt.write(text);
			wrt.close();			
		} catch (UnsupportedEncodingException e) {
			LOGGER.error(e.getMessage());			
		} catch (FileNotFoundException e) {
			LOGGER.error(e.getMessage());
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		}
	}


	public static Boolean XMLExporter(Path outputdir, String format, String title, String eAddress,
			String[] langs, String identifiedlanguage, String html_text, String cleaned_text, int id, String pubDate, String author,String publisher,  
			String domain, String subdomain, ArrayList<String> terms, ArrayList<String[]> topic,
			List<String> neg_words, String licenseURL, String genre,
			double domain_confidence, String pdfname) { //throws Exception {

		if (html_text==null){
			//Why does it happen when there is no content? 
			LOGGER.warn("HTML content of "+eAddress + " cannot be stored.");
			html_text=""; //for avoiding future catches
		}
		//Filename of files to be written.
		String temp_id=Integer.toString(id);
		String html_filename="";

		if (format.contains(pdfmime) ){
			html_filename = identifiedlanguage+HYPHEN+temp_id + appPDFext;			html_text = cleaned_text;
		}else{
			if (format.contains(docmime) ){
				html_filename = identifiedlanguage+HYPHEN+temp_id + appDOCext;		html_text = cleaned_text;
			}else
				html_filename = identifiedlanguage+HYPHEN+temp_id + appHTMLext;
		}

		Path xml_file = new Path("file", "",
				(FilenameUtils.concat(outputdir.toUri().getPath(),  identifiedlanguage+HYPHEN+temp_id + appXMLext )));

		Path annotation = new Path(outputdir,html_filename);
		if (format.contains(pdfmime)){
			try {
				//FcFileUtils.copy(pdfname, FilenameUtils.concat(outputdir.toUri().getPath(),  identifiedlanguage+HYPHEN+temp_id + appPDFext));
				FileUtils.copyFile(new File(pdfname), new File(FilenameUtils.concat(outputdir.toUri().getPath(),  identifiedlanguage+HYPHEN+temp_id + appPDFext)));
			} catch (IOException e) {
				LOGGER.info("source PDF file is not stored.");
				e.printStackTrace();
			}
		}else{
			if (format.contains(docmime)){
				try {
					//FcFileUtils.copy(pdfname, FilenameUtils.concat(outputdir.toUri().getPath(),  identifiedlanguage+HYPHEN+temp_id + appDOCext));
					FileUtils.copyFile(new File(pdfname), new File(FilenameUtils.concat(outputdir.toUri().getPath(),  identifiedlanguage+HYPHEN+temp_id + appDOCext)));	
				} catch (IOException e) {
					LOGGER.info("source DOC file is not stored.");
					e.printStackTrace();
				}
			}else{
				OutputStreamWriter tmpwrt=null;
				try {
					tmpwrt = new OutputStreamWriter(new FileOutputStream(annotation.toUri().getPath()),"UTF-8");
					tmpwrt.write(html_text);
					tmpwrt.flush();
					//tmpwrt.close();
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}finally{
					try {
						tmpwrt.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		//Write the XML file
		int parId = 1;
		XMLOutputFactory2 xof = (XMLOutputFactory2) XMLOutputFactory2.newInstance();
		XMLStreamWriter2 xtw1 = null;
		XMLStreamWriter2 xtw = null;
		OutputStreamWriter wrt = null;
		try {
			wrt = new OutputStreamWriter(new FileOutputStream(xml_file.toUri().getPath()),"UTF-8");
			xtw1 = (XMLStreamWriter2)
					xof.createXMLStreamWriter(wrt);
			PrettyPrintHandler handler = new PrettyPrintHandler(xtw1 );
			xtw = (XMLStreamWriter2)
					Proxy.newProxyInstance(XMLStreamWriter2.class.getClassLoader(),
							new Class[] { XMLStreamWriter2.class }, handler);
			xtw.writeStartDocument();

			xtw.writeStartElement("cesDoc");
			xtw.writeAttribute("version", "0.4");
			xtw.writeAttribute("xmlns:xlink", cesNameSpace );
			xtw.writeAttribute("xmlns", cesNameSpace1 );
			xtw.writeAttribute("xmlns:xsi", cesNameSpace2 );

			createHeader(xtw, eAddress, pubDate, author, publisher, identifiedlanguage, title, domain, terms, 
					annotation.getName(), format, subdomain, licenseURL,genre,domain_confidence);

			xtw.writeStartElement("text");
			xtw.writeStartElement("body");
			String identifiedlanguagePreLine ="";
			String foundt ="";
			try {
				String lines[] = cleaned_text.split("\n");
				for (String line : lines ) {
					line = line.trim();
					if (line.isEmpty()) continue;
					if (line.toCharArray()[0]==160)continue;
					int fir = line.indexOf(morethan);
					int las = line.lastIndexOf(lessthan);
					if (fir<0 || las<0 || fir>=las) continue;
					String linecontent = line.substring(fir, las);
					if (linecontent.isEmpty()) continue;
					xtw.writeStartElement(p_type);
					xtw.writeAttribute("id", (p_type+parId));
					if (line.substring(0,7).equals(boiler_st)) {
						if (line.substring(0, 8).equals(boiler_tag)) {
							xtw.writeAttribute(tag_crawlinfo,attr_boilerplateV);
							line = line.substring(8, line.length()-9).trim();
						}
						else if (line.substring(0, 15).equals("<boiler type='t")) {
							xtw.writeAttribute(tag_crawlinfo,attr_boilerplateV);
							xtw.writeAttribute(tag_type,attr_titleV);
							line = line.substring(21, line.length()-9).trim();
						}
						else if (line.substring(0, 15).equals("<boiler type='h")) {
							xtw.writeAttribute(tag_crawlinfo,attr_boilerplateV);
							xtw.writeAttribute(tag_type,"heading");
							line = line.substring(23, line.length()-9).trim();
						}
						else if (line.substring(0, 15).equals("<boiler type='l")) {
							xtw.writeAttribute(tag_crawlinfo,attr_boilerplateV);
							xtw.writeAttribute(tag_type,"listitem");
							line = line.substring(24, line.length()-9).trim();
						}
					}else if (line.substring(0, 5).equals(text_st)){
						if (line.substring(0,6).equals(text_tag)) {
							line = line.substring(6, line.length()-7).trim();
							if (!FCStringUtils.isLong(line,MIN_TOKENS_PER_PARAGRAPH))
								xtw.writeAttribute(tag_crawlinfo, attr_lengthV);
							else if (langs.length>0){
								identifiedlanguagePreLine = LangDetectUtils.detectLanguage(line.toLowerCase());
								if (!identifiedlanguagePreLine.equals(identifiedlanguage))
									xtw.writeAttribute(tag_crawlinfo, attr_langV);
								else if (TopicTools.findWords(line, neg_words))
									xtw.writeAttribute(tag_crawlinfo, attr_negV);
								else {
									foundt = TopicTools.findTopicTerms(line, topic, identifiedlanguage);
									if (!foundt.isEmpty())
										xtw.writeAttribute("topic", foundt);
								}
							}
						}else if (line.substring(0,13).equals("<text type='t")) {
							line = line.substring(19, line.length()-7).trim();
							if (!FCStringUtils.isLong(line,MIN_TOKENS_PER_PARAGRAPH)){
								xtw.writeAttribute(tag_crawlinfo, attr_lengthV);
								xtw.writeAttribute(tag_type,attr_titleV);
							}
							else if (langs.length>0){
								identifiedlanguagePreLine = LangDetectUtils.detectLanguage(line.toLowerCase());
								if (!identifiedlanguagePreLine.equals(identifiedlanguage)){
									xtw.writeAttribute(tag_crawlinfo, attr_langV);
									xtw.writeAttribute(tag_type,attr_titleV);
								}
								else if (TopicTools.findWords(line, neg_words)){
									xtw.writeAttribute(tag_crawlinfo, attr_negV);
									xtw.writeAttribute(tag_type,attr_titleV);
								}
								else {
									foundt = TopicTools.findTopicTerms(line, topic, identifiedlanguage);
									xtw.writeAttribute(tag_type,attr_titleV);
									if (!foundt.isEmpty()){
										xtw.writeAttribute("topic", foundt);
									}
								}
							}
						}else if (line.substring(0,13).equals("<text type='l")) {
							line = line.substring(22, line.length()-7).trim();
							if (!FCStringUtils.isLong(line,MIN_TOKENS_PER_PARAGRAPH)){
								xtw.writeAttribute(tag_crawlinfo, attr_lengthV);
								xtw.writeAttribute(tag_type,"listitem");
							}
							else if (langs.length>0){
								identifiedlanguagePreLine = LangDetectUtils.detectLanguage(line.toLowerCase());
								if (!identifiedlanguagePreLine.equals(identifiedlanguage)){
									xtw.writeAttribute(tag_crawlinfo, attr_langV);
									xtw.writeAttribute(tag_type,"listitem");
								}
								else if (TopicTools.findWords(line, neg_words)){
									xtw.writeAttribute(tag_crawlinfo, attr_negV);
									xtw.writeAttribute(tag_type,"listitem");
								}
								else {
									foundt = TopicTools.findTopicTerms(line, topic, identifiedlanguage);
									xtw.writeAttribute(tag_type,"listitem");
									if (!foundt.isEmpty()){
										xtw.writeAttribute("topic", foundt);
									}
								}
							}
						}else if (line.substring(0,13).equals("<text type='h")) {
							line = line.substring(21, line.length()-7).trim();
							if (!FCStringUtils.isLong(line,MIN_TOKENS_PER_PARAGRAPH)){
								xtw.writeAttribute(tag_crawlinfo, attr_lengthV);
								xtw.writeAttribute(tag_type,"heading");
							}
							else if (langs.length>0){
								identifiedlanguagePreLine = LangDetectUtils.detectLanguage(line.toLowerCase());
								if (!identifiedlanguagePreLine.equals(identifiedlanguage)){
									xtw.writeAttribute(tag_crawlinfo, attr_langV);
									xtw.writeAttribute(tag_type,"heading");
								}
								else if (TopicTools.findWords(line, neg_words)){
									xtw.writeAttribute(tag_crawlinfo, attr_negV);
									xtw.writeAttribute(tag_type,"heading");
								}
								else {
									foundt = TopicTools.findTopicTerms(line, topic, identifiedlanguage);
									xtw.writeAttribute(tag_type,"heading");
									if (!foundt.isEmpty()){
										xtw.writeAttribute("topic", foundt);
									}
								}
							}
						}
					}else {
						if (line.trim().length()<=1) continue;

						if (!FCStringUtils.isLong(line, MIN_TOKENS_PER_PARAGRAPH )) {
							xtw.writeAttribute(tag_type,"length");
						}else {
							identifiedlanguagePreLine = LangDetectUtils.detectLanguage(line.toLowerCase());
							if (!identifiedlanguagePreLine.equals(identifiedlanguage)){
								xtw.writeAttribute(tag_type, "lang");
							} else {
								if (TopicTools.findWords(line,neg_words)){	//contain "forbidden" words
									xtw.writeAttribute(tag_type, "content");
								}
								else {
									//does the paragraph contain terms?
									if (topic!=null) {
										String[] tempstr = new String[1];		
										String term;
										ArrayList<String> stems =new ArrayList<String>();
										try {
											stems = TopicTools.getStems(line, identifiedlanguagePreLine);
										} catch (IOException e) {
											e.printStackTrace();
										} 
										String par_text="";
										for (String st:stems){
											par_text=par_text.concat(" "+st);
										}
										par_text = par_text.trim();
										Boolean found = false;

										for (int ii=0;ii<topic.size();ii++){ //for each row of the topic
											tempstr=topic.get(ii);
											term = tempstr[1];
											Pattern pattern = Pattern.compile(" "+term+" ");	
											Matcher matcher = pattern.matcher(" "+par_text+" ");
											if (matcher.find()){
												found=true;
												break;
											}
										}
										if (!found){//does not contain terms
											xtw.writeAttribute(tag_type,"terms");
										}
									}
								}
							}
						}
					}
					xtw.writeCharacters(line.trim());
					xtw.writeEndElement();
					parId++;
				}							
			} catch (Exception e) {
				LOGGER.info("Could not write file with id " + temp_id);
				return false;
			}
			xtw.writeEndElement();
			xtw.writeEndElement();
			xtw.writeEndElement();
			xtw.flush();
			xtw.close();
			xtw1.close();
			wrt.close();
		} catch (XMLStreamException e) {
			LOGGER.error("Could not write XML " + xml_file);
			LOGGER.error(e.getMessage());
			return false;			
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (Exporter.applyOfflineXSLT==true) {
			File inFile = new File(xml_file.toUri());
			File outFile = new File(FilenameUtils.removeExtension(inFile.getAbsolutePath()) + appXMLext+appHTMLext);
			try {
				Exporter.xslTransformer.transform(inFile, outFile);
			} catch (MalformedURLException e) {
				LOGGER.warn("Could not transform " + inFile.getAbsolutePath() + " to " + outFile.getAbsolutePath());
			} catch (TransformerException e) {
				e.printStackTrace();
				LOGGER.warn("Could not transform " + inFile.getAbsolutePath() + " to " + outFile.getAbsolutePath());
			}
		}
		return true;
	}

	/**
	 * Writes the Header section of the cesDocFile
	 * @param xtw
	 * @param url
	 * @param pubDate
	 * @param author
	 * @param publisher
	 * @param language
	 * @param title
	 * @param domain
	 * @param terms
	 * @param htmlFilename
	 * @param file_format
	 * @param subdomain
	 * @param licenseURL
	 * @param genre
	 * @param domain_confidence
	 * @throws XMLStreamException
	 */
	private static void createHeader(XMLStreamWriter2 xtw, String url, String pubDate,String author,String publisher,
			String language, String title, String domain, ArrayList<String> terms, 
			String htmlFilename, String file_format, String subdomain, String licenseURL, 
			String genre, double domain_confidence) throws XMLStreamException {
		xtw.writeStartElement("cesHeader");
		xtw.writeAttribute("version", cesDocVersion);
		xtw.writeStartElement("fileDesc");
		xtw.writeStartElement("titleStmt");
		xtw.writeStartElement(attr_titleV);
		xtw.writeCharacters(title.toString());
		xtw.writeEndElement();
		xtw.writeStartElement("respStmt");
		xtw.writeStartElement("resp");
		xtw.writeStartElement(tag_type);
		xtw.writeCharacters("Crawling and normalization");
		xtw.writeEndElement();
		xtw.writeStartElement("name");
		xtw.writeCharacters("ILSP");
		xtw.writeEndElement();
		xtw.writeEndElement();
		xtw.writeEndElement();
		xtw.writeEndElement();
		xtw.writeStartElement("publicationStmt");   
		xtw.writeStartElement("distributor");
		xtw.writeCharacters(researchProject  +" project");
		xtw.writeEndElement();
		xtw.writeStartElement("eAddress");
		xtw.writeAttribute(tag_type, "web");
		xtw.writeCharacters("project_website");
		xtw.writeEndElement();

		xtw.writeStartElement("availability");
		if (StringUtils.isEmpty(licenseURL)){
			xtw.writeAttribute("status", "Under review");
			xtw.writeStartElement("license");
		} else {
			xtw.writeStartElement("license");
			String[] t1 =  licenseURL.split(separator);
			if (t1.length>1)
				xtw.writeAttribute("target", t1[1]);
			xtw.writeCharacters(t1[0]);
		}
		xtw.writeEndElement();
		xtw.writeEndElement();

		xtw.writeStartElement("pubDate");
		xtw.writeCharacters(year);
		xtw.writeEndElement();
		xtw.writeEndElement();

		xtw.writeStartElement("sourceDesc");
		xtw.writeStartElement("biblStruct");
		xtw.writeStartElement("monogr");
		xtw.writeStartElement(attr_titleV);
		xtw.writeCharacters(title.toString());
		xtw.writeEndElement();
		xtw.writeStartElement("author");

		xtw.writeCharacters(author);
		xtw.writeEndElement();
		xtw.writeStartElement("imprint");

		xtw.writeStartElement("format");
		xtw.writeCharacters(file_format);
		xtw.writeEndElement();

		xtw.writeStartElement("publisher");
		xtw.writeCharacters(publisher);
		xtw.writeEndElement();
		xtw.writeStartElement("pubDate");
		xtw.writeCharacters(pubDate);
		xtw.writeEndElement();
		xtw.writeStartElement("eAddress");
		xtw.writeCharacters(url);
		xtw.writeEndElement();


		if (StringUtils.isEmpty(licenseURL)){
			xtw.writeStartElement("availability");
			xtw.writeAttribute("status", "unknown");
			xtw.writeStartElement("license");
			xtw.writeEndElement();
			xtw.writeEndElement();
		} else {
			xtw.writeStartElement("availability");
			xtw.writeStartElement("license");
			String[] t1 =  licenseURL.split(separator);
			if (t1.length>1)
				xtw.writeAttribute("target", t1[1]);
			xtw.writeCharacters(t1[0]);
			xtw.writeEndElement();
			xtw.writeEndElement();
		}

		xtw.writeEndElement();

		xtw.writeEndElement();
		xtw.writeEndElement();
		xtw.writeEndElement();
		xtw.writeEndElement();
		xtw.writeStartElement("profileDesc");

		xtw.writeStartElement("langUsage");
		xtw.writeStartElement("language");
		//language=ISOLangCodes.get3LetterCode(language);
		xtw.writeAttribute("iso639", language!=null?language:"");
		xtw.writeEndElement();
		xtw.writeEndElement();

		xtw.writeStartElement("textClass");

		// FIXME genre
		//		<taxonomy>
		//		 <category xml:id="news">
		//		  <catDesc>Newspapers</catDesc>
		//		 </category>
		//		 <category xml:id="prov">
		//		  <catDesc>Provincial</catDesc>
		//		 </category>
		//		 <category xml:id="sales2">
		//		  <catDesc>Low to average annual sales</catDesc>
		//		 </category>
		//		</taxonomy>

		//		 <catRef target="#acprose"/>
		//		 <classCode scheme="http://www.udcc.org">001.9</classCode>
		//		 <keywords scheme="http://authorities.loc.gov">
		//		  <list>
		//		   <item>End of the world</item>
		//		   <item>History - philosophy</item>
		//		  </list>
		//		 </keywords>
		//		</textClass>

		if (terms!=null){
			xtw.writeStartElement("keywords");
			for (String term:terms) {
				xtw.writeStartElement("keyTerm");
				xtw.writeCharacters(term);
				xtw.writeEndElement();
			}
			xtw.writeEndElement();
		}
		xtw.writeStartElement("domain");
		if (domain_confidence>0)
			xtw.writeAttribute("confidence", Double.toString(domain_confidence));
		if (domain==null){	domain="";}
		xtw.writeCharacters(domain);
		xtw.writeEndElement();
		xtw.writeStartElement("subdomain");
		xtw.writeCharacters(subdomain!=null?subdomain:"");
		xtw.writeEndElement();
		xtw.writeStartElement("genre");
		xtw.writeCharacters(genre!=null?genre:"");
		xtw.writeEndElement();
		xtw.writeStartElement("subject");
		xtw.writeEndElement();
		xtw.writeEndElement();
		xtw.writeStartElement("annotations");
		xtw.writeStartElement("annotation");
		xtw.writeCharacters(htmlFilename);		
		xtw.writeEndElement();

		xtw.writeEndElement();
		xtw.writeEndElement();
		xtw.writeEndElement();// cesHeader
	}

	public static void main(String[] args) {
		Exporter se = new Exporter();
		options = new ExporterOptions();
		options.parseOptions(args);
		se.setRunOffLine(options.getRunOffLine());
		se.setMIN_TOKENS_PER_PARAGRAPH(options.get_length());
		se.setMIN_TOKENS_NUMBER(options.get_minTokenslength());
		se.setCrawlDirName (options.get_inputdir());
		se.setBaseName(options.getBaseName());
		se.setTargetLanguages(options.get_language());
		se.setTopic(options.get_topic());
		se.setTargetedDomain(options.getTargetDomain());
		se.setNegWordsFile(options.get_negwords());
		se.setOutputDir(options.get_outputdir());
		se.setTextExport(options.get_textexport());
		se.setApplyOfflineXSLT(options.applyOfflineXSLT());
		se.setUsedHttrack(options.usedHttrack());
		config = getConfig(options.getConfig());
		mimetypes = config.getStringArray("fetcher.valid_mime_types.mime_type[@value]");	
		se.setAcceptedMimeTypes(mimetypes);
		LangDetectUtils.loadCybozuLangIdentifier();
		langnumMap = se.export(true);
		if (depth<10000)
			generateCSV(new File(outBaseName.getAbsolutePath()+CSV), langnumMap);
	}


	/**
	 * Loads the default configuration file and checks if user supplied a custom one.
	 * @param type
	 * @param confFile
	 * @return
	 */
	private static CompositeConfiguration getConfig(String confFile) {
		config = new CompositeConfiguration();
		URL default_config = Crawl.class.getClassLoader().getResource("FBC_config.xml");;

		if (confFile!=null){
			String custom_config = confFile;
			try {
				XMLConfiguration xml_custom_config = new XMLConfiguration(custom_config);
				xml_custom_config.setValidating(true);
				config.addConfiguration(xml_custom_config);
			} catch (ConfigurationException e) {
				LOGGER.error("Invalid configuration file: " + custom_config);
			}
		}
		try {			
			config.addConfiguration(new XMLConfiguration(default_config));				
		} catch (ConfigurationException e1) {
			// Shouldn't happen
			LOGGER.error("Problem with default configuration file.");
		}
		return config;
	}

	private static double getRelscore(String url, Path curDirPath,
			TupleEntryIterator contentIter) {
		while (contentIter.hasNext()){
			TupleEntry entry = contentIter.next();
			ClassifierDatum datum = new ClassifierDatum(entry);

			if (datum.getUrl().equals(url)) {
				//double relscore = datum.getSubClasses();
				double relscore = datum.getTotRelScore();
				if (relscore < 1) 
					return relscore;
				else
					return 1;
			}
		}		
		return 0;
	}

	private static String getSubdomains(String url, Path curDirPath, TupleEntryIterator contentIter){
		String subdomains = "";
		while (contentIter.hasNext()){
			TupleEntry entry = contentIter.next();
			ClassifierDatum datum = new ClassifierDatum(entry);

			if (datum.getUrl().equals(url)) {
				String[] subclasses = datum.getSubClasses();
				for (String s: subclasses){
					subdomains=subdomains.concat(s + ";");
					//LOGGER.info(url + " " + s + " " + subscores[count][0]);
					//count++;
				}
				if (subdomains == "") return subdomains;
				return subdomains.substring(0,subdomains.length()-1);
			}
		}		
		return null;
	}

	private static String getValidFormat(String format){
		String result = format;
		if (format.contains(";")){
			result = format.split(separator)[0];
		}
		return result;
	}
	public void setMIN_TOKENS_PER_PARAGRAPH(int mIN_TOKENS_PER_PARAGRAPH) {
		MIN_TOKENS_PER_PARAGRAPH = mIN_TOKENS_PER_PARAGRAPH;
	}
	public void setMIN_TOKENS_NUMBER(int mIN_TOKENS_NUMBER) {
		MIN_TOKENS_NUMBER = mIN_TOKENS_NUMBER;
	}
	public void setDepth(int d) {
		depth = d;
	}
	public static File getCrawlDirName() {
		return crawlDirName;
	}
	public static boolean getRunOffLine1(){
		return offline;
	}
	public void setRunOffLine(boolean offline){
		Exporter.offline = offline;
	}
	public void setUsedHttrack(boolean httrack){
		Exporter.httrack = httrack;
	}
	public void setCrawlDirName(File crawlDirName) {
		Exporter.crawlDirName = crawlDirName;
	}

	public void setTargetLanguages(String[] targetlanguages) {
		Exporter.targetlanguages = targetlanguages;
	}
	public static String[] getLanguage() {
		return targetlanguages;
	}
	public static File getTopic() {
		return topic;
	}
	public void setTopic(File topic) {
		Exporter.topic = topic;
	}
	public static File getNegWordsFile() {
		return negWordsFile;
	}
	public void setNegWordsFile(File file) {
		Exporter.negWordsFile = file;
	}
	public void setBaseName(File outBaseName) {
		Exporter.outBaseName  = outBaseName;
	}
	public void setOutputDir(File outputDir) {
		Exporter.outputDir = outputDir;
	}
	public void setTextExport(boolean textexport){
		Exporter.textExport = textexport;
	}
	public void setAcceptedMimeTypes(String[] mimes){
		Exporter.mimetypes = mimes;
	}
	public void setTargetedDomain(String targeteddomain){
		Exporter.targeteddomain = targeteddomain;
	}
	public void setGenres(URL url){
		Exporter.genres = url;
	}
	public static URL getGenres() {
		return genres;
	}
	public static String getResearchProject() {
		return researchProject;
	}
	public static void setResearchProject(String researchProject) {
		Exporter.researchProject = researchProject;
	}
	public void setUrlsToIds(Map<String, String> urlsToIds) {
		this.urlsToIds = urlsToIds;
	}
	public Map<String, String> getUrlsToIds() {
		return urlsToIds;
	}
	public boolean isApplyOfflineXSLT() {
		return applyOfflineXSLT;
	}
	public void setApplyOfflineXSLT(boolean applyOfflineXSLT) {
		Exporter.applyOfflineXSLT = applyOfflineXSLT;
		if (applyOfflineXSLT==true) {
			try {
				Exporter.xslTransformer = new XSLTransformer();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformerConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (Exporter.xslTransformer==null) {
				LOGGER.warn("Cannot initialize xslTransformer. Will not transform XML files using xslt.");
				Exporter.applyOfflineXSLT=false;
			}
		}
	}

	public static Boolean XMLExporter(File file, String format, String title, String eAddress,
			String[] langs, String identifiedlanguage, String cleaned_text, String pubDate, String author,String publisher,  
			String domain, String subdomain, ArrayList<String> terms, ArrayList<String[]> topic, List<String> neg_words, String licenseURL, String genre,
			double domain_confidence, String pdfname) { //throws Exception {

		//Write the XML file
		File xml_file =new File(FilenameUtils.concat(file.getParent(), FilenameUtils.getBaseName(file.getName())+appXMLext));
		int parId = 1;
		XMLOutputFactory2 xof = (XMLOutputFactory2) XMLOutputFactory2.newInstance();
		XMLStreamWriter2 xtw1 = null;
		XMLStreamWriter2 xtw = null;
		OutputStreamWriter wrt = null;
		try {
			wrt = new OutputStreamWriter(new FileOutputStream(xml_file),"UTF-8");
			xtw1 = (XMLStreamWriter2)
					xof.createXMLStreamWriter(wrt);
			PrettyPrintHandler handler = new PrettyPrintHandler(xtw1 );
			xtw = (XMLStreamWriter2)
					Proxy.newProxyInstance(XMLStreamWriter2.class.getClassLoader(),
							new Class[] { XMLStreamWriter2.class }, handler);
			xtw.writeStartDocument();

			xtw.writeStartElement("cesDoc");
			xtw.writeAttribute("version", "0.4");
			xtw.writeAttribute("xmlns:xlink", cesNameSpace );
			xtw.writeAttribute("xmlns", cesNameSpace1 );
			xtw.writeAttribute("xmlns:xsi", cesNameSpace2 );

			createHeader(xtw, eAddress, pubDate, author, publisher, identifiedlanguage, title, domain, terms, 
					file.getName(), format, subdomain, licenseURL,genre,domain_confidence);

			xtw.writeStartElement("text");
			xtw.writeStartElement("body");
			String identifiedlanguagePreLine ="";
			String foundt ="";
			try {
				String lines[] = cleaned_text.split("\n");
				for (String line : lines ) {
					line = line.trim();
					if (line.isEmpty()) continue;
					if (line.toCharArray()[0]==160)continue;
					int fir = line.indexOf(morethan);
					int las = line.lastIndexOf(lessthan);
					if (fir<0 || las<0 || fir>=las) continue;
					String linecontent = line.substring(fir, las);
					if (linecontent.isEmpty()) continue;
					xtw.writeStartElement(p_type);
					xtw.writeAttribute("id", (p_type+parId));
					if (line.substring(0,7).equals(boiler_st)) {
						if (line.substring(0, 8).equals(boiler_tag)) {
							xtw.writeAttribute(tag_crawlinfo,attr_boilerplateV);
							line = line.substring(8, line.length()-9).trim();
						}
						else if (line.substring(0, 15).equals("<boiler type='t")) {
							xtw.writeAttribute(tag_crawlinfo,attr_boilerplateV);
							xtw.writeAttribute(tag_type,attr_titleV);
							line = line.substring(21, line.length()-9).trim();
						}
						else if (line.substring(0, 15).equals("<boiler type='h")) {
							xtw.writeAttribute(tag_crawlinfo,attr_boilerplateV);
							xtw.writeAttribute(tag_type,"heading");
							line = line.substring(23, line.length()-9).trim();
						}
						else if (line.substring(0, 15).equals("<boiler type='l")) {
							xtw.writeAttribute(tag_crawlinfo,attr_boilerplateV);
							xtw.writeAttribute(tag_type,"listitem");
							line = line.substring(24, line.length()-9).trim();
						}
					}else if (line.substring(0, 5).equals(text_st)){
						if (line.substring(0,6).equals(text_tag)) {
							line = line.substring(6, line.length()-7).trim();
							if (!FCStringUtils.isLong(line,MIN_TOKENS_PER_PARAGRAPH))
								xtw.writeAttribute(tag_crawlinfo, attr_lengthV);
							else if (langs.length>0){
								identifiedlanguagePreLine = LangDetectUtils.detectLanguage(line.toLowerCase());
								if (!identifiedlanguagePreLine.equals(identifiedlanguage))
									xtw.writeAttribute(tag_crawlinfo, attr_langV);
								else if (TopicTools.findWords(line, neg_words))
									xtw.writeAttribute(tag_crawlinfo, attr_negV);
								else {
									foundt = TopicTools.findTopicTerms(line, topic, identifiedlanguage);
									if (!foundt.isEmpty())
										xtw.writeAttribute("topic", foundt);
								}
							}
						}else if (line.substring(0,13).equals("<text type='t")) {
							line = line.substring(19, line.length()-7).trim();
							if (!FCStringUtils.isLong(line,MIN_TOKENS_PER_PARAGRAPH)){
								xtw.writeAttribute(tag_crawlinfo, attr_lengthV);
								xtw.writeAttribute(tag_type,attr_titleV);
							}
							else if (langs.length>0){
								identifiedlanguagePreLine = LangDetectUtils.detectLanguage(line.toLowerCase());
								if (!identifiedlanguagePreLine.equals(identifiedlanguage)){
									xtw.writeAttribute(tag_crawlinfo, attr_langV);
									xtw.writeAttribute(tag_type,attr_titleV);
								}
								else if (TopicTools.findWords(line, neg_words)){
									xtw.writeAttribute(tag_crawlinfo, attr_negV);
									xtw.writeAttribute(tag_type,attr_titleV);
								}
								else {
									foundt = TopicTools.findTopicTerms(line, topic, identifiedlanguage);
									xtw.writeAttribute(tag_type,attr_titleV);
									if (!foundt.isEmpty()){
										xtw.writeAttribute("topic", foundt);
									}
								}
							}
						}else if (line.substring(0,13).equals("<text type='l")) {
							line = line.substring(22, line.length()-7).trim();
							if (!FCStringUtils.isLong(line,MIN_TOKENS_PER_PARAGRAPH)){
								xtw.writeAttribute(tag_crawlinfo, attr_lengthV);
								xtw.writeAttribute(tag_type,"listitem");
							}
							else if (langs.length>0){
								identifiedlanguagePreLine = LangDetectUtils.detectLanguage(line.toLowerCase());
								if (!identifiedlanguagePreLine.equals(identifiedlanguage)){
									xtw.writeAttribute(tag_crawlinfo, attr_langV);
									xtw.writeAttribute(tag_type,"listitem");
								}
								else if (TopicTools.findWords(line, neg_words)){
									xtw.writeAttribute(tag_crawlinfo, attr_negV);
									xtw.writeAttribute(tag_type,"listitem");
								}
								else {
									foundt = TopicTools.findTopicTerms(line, topic, identifiedlanguage);
									xtw.writeAttribute(tag_type,"listitem");
									if (!foundt.isEmpty()){
										xtw.writeAttribute("topic", foundt);
									}
								}
							}
						}else if (line.substring(0,13).equals("<text type='h")) {
							line = line.substring(21, line.length()-7).trim();
							if (!FCStringUtils.isLong(line,MIN_TOKENS_PER_PARAGRAPH)){
								xtw.writeAttribute(tag_crawlinfo, attr_lengthV);
								xtw.writeAttribute(tag_type,"heading");
							}
							else if (langs.length>0){
								identifiedlanguagePreLine = LangDetectUtils.detectLanguage(line.toLowerCase());
								if (!identifiedlanguagePreLine.equals(identifiedlanguage)){
									xtw.writeAttribute(tag_crawlinfo, attr_langV);
									xtw.writeAttribute(tag_type,"heading");
								}
								else if (TopicTools.findWords(line, neg_words)){
									xtw.writeAttribute(tag_crawlinfo, attr_negV);
									xtw.writeAttribute(tag_type,"heading");
								}
								else {
									foundt = TopicTools.findTopicTerms(line, topic, identifiedlanguage);
									xtw.writeAttribute(tag_type,"heading");
									if (!foundt.isEmpty()){
										xtw.writeAttribute("topic", foundt);
									}
								}
							}
						}
					}else {
						if (line.trim().length()<=1) continue;

						if (!FCStringUtils.isLong(line, MIN_TOKENS_PER_PARAGRAPH )) {
							xtw.writeAttribute(tag_type,"length");
						}else {
							identifiedlanguagePreLine = LangDetectUtils.detectLanguage(line.toLowerCase());
							if (!identifiedlanguagePreLine.equals(identifiedlanguage)){
								xtw.writeAttribute(tag_type, "lang");
							} else {
								if (TopicTools.findWords(line,neg_words)){	//contain "forbidden" words
									xtw.writeAttribute(tag_type, "content");
								}
								else {
									//does the paragraph contain terms?
									if (topic!=null) {
										String[] tempstr = new String[1];		
										String term;
										ArrayList<String> stems =new ArrayList<String>();
										try {
											stems = TopicTools.getStems(line, identifiedlanguagePreLine);
										} catch (IOException e) {
											e.printStackTrace();
										} 
										String par_text="";
										for (String st:stems){
											par_text=par_text.concat(" "+st);
										}
										par_text = par_text.trim();
										Boolean found = false;

										for (int ii=0;ii<topic.size();ii++){ //for each row of the topic
											tempstr=topic.get(ii);
											term = tempstr[1];
											Pattern pattern = Pattern.compile(" "+term+" ");	
											Matcher matcher = pattern.matcher(" "+par_text+" ");
											if (matcher.find()){
												found=true;
												break;
											}
										}
										if (!found){//does not contain terms
											xtw.writeAttribute(tag_type,"terms");
										}
									}
								}
							}
						}
					}
					xtw.writeCharacters(line.trim());
					xtw.writeEndElement();
					parId++;
				}							
			} catch (Exception e) {
				LOGGER.info("Could not write file " + xml_file);
				return false;
			}
			xtw.writeEndElement();
			xtw.writeEndElement();
			xtw.writeEndElement();
			xtw.flush();
			xtw.close();
			xtw1.close();
			wrt.close();
		} catch (XMLStreamException e) {
			LOGGER.error("Could not write XML " + xml_file);
			LOGGER.error(e.getMessage());
			return false;			
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (Exporter.applyOfflineXSLT==true) {
			File outFile = new File(FilenameUtils.removeExtension(xml_file.getAbsolutePath()) + appXMLext+appHTMLext);
			try {
				Exporter.xslTransformer.transform(xml_file, outFile);
			} catch (MalformedURLException e) {
				LOGGER.warn("Could not transform " + xml_file.getAbsolutePath() + " to " + outFile.getAbsolutePath());
			} catch (TransformerException e) {
				e.printStackTrace();
				LOGGER.warn("Could not transform " + xml_file.getAbsolutePath() + " to " + outFile.getAbsolutePath());
			}
		}
		return true;
	}

}
