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
package gr.ilsp.fmc.exporter;

import gr.ilsp.fmc.datums.ClassifierDatum;
import gr.ilsp.fmc.datums.CrawlDbDatum;
import gr.ilsp.fmc.datums.ExtendedParsedDatum;
import gr.ilsp.fmc.main.SimpleCrawlHFS;
import gr.ilsp.fmc.utils.AnalyzerFactory;
import gr.ilsp.fmc.utils.ContentNormalizer;
import gr.ilsp.fmc.utils.CrawlConfig;
import gr.ilsp.fmc.utils.DirUtils;
import gr.ilsp.fmc.utils.JarUtils;
import gr.ilsp.fmc.utils.LithuanianAnalyzer;
import gr.ilsp.fmc.utils.PrettyPrintHandler;
import gr.ilsp.fmc.utils.TopicTools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
//import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.FilenameUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.JobConf;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.tika.metadata.Metadata;
import org.codehaus.stax2.XMLOutputFactory2;
import org.codehaus.stax2.XMLStreamWriter2;

import bixo.datum.FetchedDatum;
import bixo.datum.UrlStatus;
import bixo.utils.CrawlDirUtils;
import cascading.scheme.SequenceFile;
import cascading.scheme.TextLine;
import cascading.tap.Hfs;
import cascading.tap.Tap;
import cascading.tuple.TupleEntry;
import cascading.tuple.TupleEntryIterator;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

@SuppressWarnings("deprecation")
public class SampleExporter {
	private static final Logger LOGGER = Logger.getLogger(SampleExporter.class);
	//private static int minTokensNumber=200;
	//private static int minTokensNumber;
	//private static String VAR_RES_CACHE = "/var/lib/tomcat6/webapps/soaplab2-results/";
	//private static final String HTTP_PATH = "http://nlp.ilsp.gr/soaplab2-results/";	
	private static final String cesDocVersion = "0.4";
	private static String cesNameSpace = "http://www.w3.org/1999/xlink";
	private static String cesNameSpace1 = "http://www.xces.org/schema/2003";
	private static String cesNameSpace2 = "http://www.w3.org/2001/XMLSchema-instance";
	
	private static String year = Integer.toString(Calendar.getInstance().get(Calendar.YEAR));//FIXME

	private static int MIN_TOKENS_PER_PARAGRAPH;
	private static int MIN_TOKENS_NUMBER;
	private static String crawlDirName;
	private static String language;
	private static String topic;
	private static String negWordsFile;
	private static String outputDir="";
	private static boolean textExport = false;

	private static boolean applyOfflineXSLT = false;
	private static XSLTransformer xslTransformer = null;

	private static boolean cesdoc = false;
	private static boolean html = false;
	private static String[] mimetypes;
	private static String targeteddomain;
	private static SampleExporterOptions options = null;
	static Analyzer analyzer = null;
	static AnalyzerFactory analyzerFactory = new AnalyzerFactory();
	private static ArrayList<String> topicTermsAll = null;
	private static ArrayList<File> xmlFiles = new ArrayList<File>();
	private static String outputFile = null;
	private static String outputFileHTML = null;
	private static String researchProject = "ILSP";
	//private static String fs1 = System.getProperty("file.separator");
	
	private static void processStatus(JobConf conf, Path curDirPath) throws IOException {
		Path statusPath = new Path(curDirPath, CrawlConfig.STATUS_SUBDIR_NAME);
		Tap statusTap = new Hfs(new TextLine(), statusPath.toUri().toString());

		TupleEntryIterator iter = statusTap.openForRead(conf);

		UrlStatus[] statusValues = UrlStatus.values();
		int[] statusCounts = new int[statusValues.length];
		int totalEntries = 0;
		while (iter.hasNext()) {
			TupleEntry entry = iter.next();
			totalEntries += 1;

			// STATUS_FN, HEADERS_FN, EXCEPTION_FN, STATUS_TIME_FN, HOST_ADDRESS_FN).append(getSuperFields(StatusDatum.class)
			String statusLine = entry.getString("line");
			String[] pieces = statusLine.split("\t");
			UrlStatus status = UrlStatus.valueOf(pieces[0]);
			statusCounts[status.ordinal()] += 1;
		}

		for (int i = 0; i < statusCounts.length; i++) {
			if (statusCounts[i] != 0) {
				LOGGER.info(String.format("Status %s: %d", statusValues[i].toString(), statusCounts[i]));
			}
		}
		LOGGER.info("Total status: " + totalEntries);
		LOGGER.info("");
	}

	private static void processCrawlDb(JobConf conf, Path curDirPath, boolean exportDb) throws IOException {
		TupleEntryIterator iter;
		int totalEntries;
		Path crawlDbPath = new Path(curDirPath, CrawlConfig.CRAWLDB_SUBDIR_NAME);
		Tap crawldbTap = new Hfs(new SequenceFile(CrawlDbDatum.FIELDS), crawlDbPath.toUri().toString());
		iter = crawldbTap.openForRead(conf);
		totalEntries = 0;
		int fetchedUrls = 0;
		int unfetchedUrls = 0;
		/*LOGGER.info("!!!! PRINTING CRAWLDB !!!!");
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
		}*/
		//LOGGER.info("!!!! PRINTING CLASSIFIED !!!!");
		int prevLoop = -1;
		Path crawlDirPath = curDirPath.getParent();
		FileSystem fs = crawlDirPath.getFileSystem(conf);
		while ((curDirPath = CrawlDirUtils.findNextLoopDir(fs, crawlDirPath, prevLoop)) != null) {
			int curLoop = CrawlDirUtils.extractLoopNumber(curDirPath);
			if (curLoop != prevLoop + 1) {
				LOGGER.warn(String.format("Missing directories between %d and %d", prevLoop, curLoop));
			}

			Path classifiedPath = new Path(curDirPath, CrawlConfig.CLASSIFIER_SUBDIR_NAME);
			Tap classifiedTap = new Hfs(new SequenceFile(ClassifierDatum.FIELDS),classifiedPath.toUri().toString());
			iter = classifiedTap.openForRead(conf);
//			while (iter.hasNext()) {
//				TupleEntry entry = iter.next();
//				ClassifierDatum datum = new ClassifierDatum(entry);
//				//LOGGER.info(datum.toString());
//			}
			prevLoop = curLoop;
		}

		if (!exportDb) {
			LOGGER.info(String.format("%d fetched URLs", fetchedUrls));
			LOGGER.info(String.format("%d unfetched URLs", unfetchedUrls));
			LOGGER.info("Total URLs: " + totalEntries);
			LOGGER.info("");
		}
	}



	public void export(boolean loadProfile) {

		long start = System.currentTimeMillis();
		try {
			JobConf conf = new JobConf();
			Path crawlDirPath = new Path(crawlDirName);
			FileSystem fs = crawlDirPath.getFileSystem(conf);

			if (!fs.exists(crawlDirPath)) {
				System.err.println("Prior crawl output directory does not exist: " + crawlDirName);
				System.exit(-1);
			}

			//LanguageIdentifier initialization	
			if (loadProfile){
				URL urldir = SimpleCrawlHFS.class.getResource("/profiles");
				LOGGER.debug(urldir );
				if (urldir.getProtocol()=="jar"){
					File tempDir = DirUtils.createTempDir();
					LOGGER.debug(tempDir );
					JarUtils.copyResourcesRecursively(urldir, tempDir);
					try {
						DetectorFactory.loadProfile(tempDir);				
					} catch (LangDetectException e1) {
						LOGGER.error(e1.getMessage());
					} 
				} else {
					try {
						DetectorFactory.loadProfile(new File(urldir.toURI()));
					} catch (LangDetectException e) {
						LOGGER.error(e.getMessage());
					} catch (URISyntaxException e) {
						LOGGER.error(e.getMessage());
					}

				}
			}

			// Skip Hadoop/Cascading DEBUG messages.
			Logger.getRootLogger().setLevel(Level.INFO);
			boolean exportAllXmls = true;

			if (exportAllXmls) {
				int prevLoop = -1;
				Path curDirPath = null;
				int id = 1;
				//Path xmlPath = new Path(crawlDirPath,CrawlConfig.XML_SUBDIR_NAME);						
				//if (fs.exists(xmlPath))
				//	fs.delete(xmlPath);
				//fs.mkdirs(xmlPath);
				String topicFile = getTopic();
				ArrayList<String[]> topic = null;
				if (topicFile!=null) {
					topic=TopicTools.analyzeTopic(topicFile,language, conf);
					topicTermsAll = TopicTools.analyzeTopicALL(topic);
				}
				while ((curDirPath = CrawlDirUtils.findNextLoopDir(fs, crawlDirPath, prevLoop)) != null) {
					id = exportToXml(conf,curDirPath,language, id,topic,targeteddomain);
					int curLoop = CrawlDirUtils.extractLoopNumber(curDirPath);
					if (curLoop != prevLoop + 1) {
						LOGGER.warn(String.format("Missing directories between %d and %d", prevLoop, curLoop));
					}

					prevLoop = curLoop;
				}
				
				LOGGER.info("CesDoc files generated: "+ xmlFiles.size());
				LOGGER.info("Completed in " + (System.currentTimeMillis()-start) + " milliseconds.");

				OutputStreamWriter xmlFileListWrt;
				xmlFileListWrt = new OutputStreamWriter(new FileOutputStream(outputFile),"UTF-8");
				for (File xmlFile: xmlFiles) {
					//String ttt = xmlFile.replace(VAR_RES_CACHE,HTTP_PATH);
					//Path tempPath = new Path(xmlFile);//xmlFileListWrt.write(tempfile.getAbsolutePath()+"\n");
					xmlFileListWrt.write(xmlFile.getAbsolutePath()+"\n");
				}
				xmlFileListWrt.close();
				
				if (html){
					if (xmlFiles.size()>0){
						String outputfile1 =outputFileHTML;
						OutputStreamWriter xmlFileListWrt1;
						xmlFileListWrt1 = new OutputStreamWriter(new FileOutputStream(outputfile1),"UTF-8");
						xmlFileListWrt1.write("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n");
						String ttt;
						for (File xmlFile: xmlFiles) {
							URL fileURL = xmlFile.toURI().toURL();
							//xmlFile1=xmlFile+".html";
							if (applyOfflineXSLT)
								ttt= "<a href=\""+fileURL+".html\">\n"+fileURL+".html</a>";
							else
								ttt= "<a href=\""+fileURL+"\">\n"+fileURL+"</a>";
							//<a href="https://issues.apache.org/jira/browse/NUTCH-721" target="_blank">NUTCH-721</a>
							xmlFileListWrt1.write("<br />"+ttt+"\n");
						}

						xmlFileListWrt1.write("</html>");
						xmlFileListWrt1.close();
					}
				}
			}

			boolean exportDb = true;
			if (exportDb) {
				Path latestCrawlDirPath = CrawlDirUtils.findLatestLoopDir(fs, crawlDirPath);
				processCrawlDb(conf, latestCrawlDirPath, exportDb);	
				//exportToXml(conf, latestCrawlDirPath, "el");
			} else {
				int prevLoop = -1;
				Path curDirPath = null;
				while ((curDirPath = CrawlDirUtils.findNextLoopDir(fs, crawlDirPath, prevLoop)) != null) {
					String curDirName = curDirPath.toUri().toString();
					LOGGER.info("");
					LOGGER.info("================================================================");
					LOGGER.info("Processing " + curDirName);
					LOGGER.info("================================================================");

					int curLoop = CrawlDirUtils.extractLoopNumber(curDirPath);
					if (curLoop != prevLoop + 1) {
						LOGGER.warn(String.format("Missing directories between %d and %d", prevLoop, curLoop));
					}

					prevLoop = curLoop;

					// Process the status and crawldb in curPath
					processStatus(conf, curDirPath);
					processCrawlDb(conf, curDirPath, exportDb);

				}
			}
			//fs.close();
		} catch (Throwable t) {
			LOGGER.error("Exception running tool", t);
			System.exit(-1);
		}

	}

	public static void main(String[] args) {
		SampleExporter se = new SampleExporter();
		options = new SampleExporterOptions();
		options.parseOptions(args);
		se.setMIN_TOKENS_PER_PARAGRAPH(options.get_length());
		se.setMIN_TOKENS_NUMBER(options.get_minTokenslength());
		se.setCrawlDirName (options.get_inputdir());
		se.setOutputFile(options.get_inputdir() + System.getProperty("file.separator") + "outputlist.txt");
		
		if (options.get_topic()!=null) {
			se.setTopic(options.get_topic());
		}
		se.setLanguage(options.get_language());
		if (options.get_topic()!=null) {
			se.setTopic(options.get_topic());
		}
		if (options.get_negwords()!=null) {
			se.setNegWordsFile(options.get_negwords());
		}
		if (options.get_outputdir()!=null) {
			se.setOutputDir(options.get_outputdir());
		}
		if (options.get_textexport()) {
			se.setTextExport(true);
		}
		if (options.get_style()) {
			se.setStyleExport(options.get_style());
		}
		se.export(true);

	}

	private static int exportToXml(JobConf conf, Path curDirPath, String language,
			int id, ArrayList<String[]> topic, String targeteddomain) throws IOException {
		TupleEntryIterator iter;
		String title = "";
		String cleanText = "";
		String htmlText = "";
		String genre="";
		String format = "";	
		String subdomains = "";
		String contentEncoding = "";
		String author ="";
		String licenseURL="";
		double relscore;
		//String pubdate="";
		String publisher="";
		
		ArrayList<String> terms = null;
		String url = "";
		Map<String,String> meta = null;
		//LOGGER.setLevel(Level.DEBUG);
		Path parseDbPath = new Path(curDirPath, CrawlConfig.PARSE_SUBDIR_NAME);
		Tap parseDbTap = new Hfs(new SequenceFile(ExtendedParsedDatum.FIELDS), parseDbPath.toUri().toString());	
		Path contentPath = new Path(curDirPath,CrawlConfig.CONTENT_SUBDIR_NAME);
		Tap contentDbTap = new Hfs(new SequenceFile(FetchedDatum.FIELDS), contentPath.toUri().toString());
		Path classifierPath = new Path(curDirPath, CrawlConfig.CLASSIFIER_SUBDIR_NAME);
		Tap classifierDbTap = new Hfs(new SequenceFile(ClassifierDatum.FIELDS), classifierPath.toUri().toString());
		TupleEntryIterator classIter = classifierDbTap.openForRead(conf);
		TupleEntryIterator classIter1 = classifierDbTap.openForRead(conf);
		Path xmlPath = null;
		if (outputDir.length()==0)
			xmlPath = new Path(curDirPath.getParent(), CrawlConfig.XML_SUBDIR_NAME);
		else 
			xmlPath = new Path(outputDir);
		FileSystem fs = xmlPath.getFileSystem(conf);
		if (!fs.exists(xmlPath)) fs.mkdirs(xmlPath);
		TupleEntryIterator contentIter = contentDbTap.openForRead(conf);
		iter = parseDbTap.openForRead(conf);
		
		String[] neg_words = null ;
		if (getNegWordsFile() != null) {
			String neg_words_filename = getNegWordsFile();
			neg_words = getForbiddenwords(neg_words_filename);
		}

		while (iter.hasNext()) {
			TupleEntry entry = iter.next();
			ExtendedParsedDatum datum = new ExtendedParsedDatum(entry);			
			url = datum.getUrl();			
			LOGGER.debug("Writing: " + id + " " + url);
			title = datum.getTitle();
			if (title==null) title = "";
			cleanText = datum.getParsedText();
			cleanText = ContentNormalizer.normalizeText(cleanText);
			meta = datum.getParsedMeta();
			author=meta.get("Author");
			publisher=meta.get("Publisher");
			String termsArray = meta.get("keywords");
			terms = new ArrayList<String>();
			if (termsArray!=null){
				//termsArray = termsArray.replace(",","");
				for (String s: termsArray.split(",|;|:"))
					terms.add(s.trim());
			}
			contentEncoding = meta.get("Content-Encoding");
			format = meta.get("Content-Type");	
			format = validFormat(format);
			htmlText = getHtml(url,curDirPath,contentIter, contentEncoding);
			subdomains = getSubdomains(url, curDirPath,classIter);
			//classIter = classifierDbTap.openForRead(conf);
			relscore = getRelscore(url, curDirPath,classIter1);
			licenseURL = meta.get(Metadata.LICENSE_URL);

			//if (format.contains("text/html"))
			//	cleanText = ContentNormalizer.normalizeText(cleanText);
			//if (format.contains("application/pdf"))
			//	cleanText = ContentNormalizer.normalizePdfText(cleanText);
			//if (format.contains("text/html"))
			//	htmlText = getHtml(url,curDirPath,contentIter, contentEncoding);
			//if (format.contains("application/pdf")){
			//	LOGGER.info("PDF should be created"); //FIXME (examine if we get the content required to create the pdf file
			//	htmlText = getHtml(url,curDirPath,contentIter, contentEncoding);
			//}
			if (XMLExporter(xmlPath,format, title, url, language, htmlText, cleanText,id, "", author, publisher, targeteddomain, subdomains, terms, topic, neg_words, licenseURL, genre,relscore ))
				id++;
			if (textExport) TextExporter(xmlPath,cleanText,id-1);
		}
		iter.close();
		classIter.close();
		contentIter.close();

		return id;		
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

	private static String getHtml(String url, Path curDirPath, TupleEntryIterator contentIter, String contentEncoding){
		String htmltext = "";
		BufferedReader reader = null;
		while (contentIter.hasNext()){
			TupleEntry entry = contentIter.next();
			FetchedDatum datum = new FetchedDatum(entry);

			if (datum.getUrl().equals(url)) {
				InputStream is = new ByteArrayInputStream(datum.getContentBytes(), 0, datum.getContentLength());
				try {
					reader = new BufferedReader(new InputStreamReader(is,contentEncoding));
					String line = "";
					while ((line=reader.readLine())!=null)
						htmltext=htmltext.concat(line + "\r\n");
					reader.close();
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally { 
					try {
						is.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return htmltext;
			}
		}		
		return null;
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

	private static String validFormat(String format){
		String result = format;
		if (format.contains(";")){
			result = format.split(";")[0];
		}
		return result;
	}

	public static String[] getForbiddenwords(String filename){
		File words_file = new File(filename);

		if (words_file.exists()){
			ArrayList<String> words1 = new ArrayList<String>();
			try {
				BufferedReader in = new BufferedReader(new FileReader(words_file));
				String str; int count=0;
				while ((str = in.readLine()) != null) {
					words1.add(str);
					count++; 
				}
				in.close();
				String[] words = new String[count];
				System.arraycopy(words1.toArray(), 0, words, 0, count);
				return words;
			} catch (IOException e) {
				System.err.println("Problem in reading the file with the forbidden words");
				//String[] words = null;
				return null;
			}
		}else{
			return null;
		}
	}


	public static void TextExporter(Path outpath, String text, int id){
		Path txt_file = new Path(outpath,id+".txt");
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
			String lang, String html_text, String cleaned_text, int id, String pubDate, String author,String publisher,  
			String domain, String subdomain, ArrayList<String> terms, ArrayList<String[]> topic,
			String[] neg_words, String licenseURL, String genre,
			double domain_confidence) { //throws Exception {

		String maincontent=""; 
		//FIXME The handling of different mime types should change.
		//The accepted MIME types are declared in the crawler's config file.
		boolean validformat=false;
		for (int ii=0;ii<mimetypes.length;ii++){
			if (format.equals(mimetypes[ii])){
				validformat=true;
				break;
			}
		}
		if (!validformat)
			return false;
			
		if (format.contains("application/pdf"))
			maincontent=cleaned_text;
		else{ // (format.contains("text/html")) 

			String[] temp = cleaned_text.split("\n");
			for (int jj=0;jj<temp.length;jj++){
				if (!temp[jj].contains("<boiler") && !temp[jj].contains("</boiler>")){
					maincontent=maincontent+temp[jj]+"\n"; 
				}
			}
			maincontent = maincontent.replaceAll("<text>", "");
			maincontent = maincontent.replaceAll("</text>", "");
			maincontent = maincontent.replaceAll("<text type.*>", "");
		}
		
		StringTokenizer tkzr = new StringTokenizer(maincontent);
		LOGGER.debug(eAddress);
		LOGGER.debug(tkzr.countTokens());
		if (tkzr.countTokens()<MIN_TOKENS_NUMBER){
				//System.out.println("CUT: "+ eAddress);
				//System.out.println("NoT: "+ tkzr.countTokens());
			return false;		
		}
		String foundt ="";
		String langidentified ="";
		String langidentified_total ="";
		langidentified_total = checkLang(maincontent.toLowerCase());
		String[] langs = lang.split(";");
		boolean match = false;
		for (String langi:langs){
			if (langidentified_total.equals(langi)){
				match = true;
				break;
			}
		}
		if (!match)
			return false;
		//Filename of files to be written.
		String temp_id=Integer.toString(id);
		String html_filename="";
		//if (format.contains("text/html"))
		//	html_filename = temp_id+".html";

		if (format.contains("application/pdf")){
			html_filename = temp_id+".pdf";
			html_text=cleaned_text;
		}else{
			html_filename = temp_id+".html";
		}
		//Path xml_file1 = new Path(outputdir,temp_id+".xml");
		Path xml_file = new Path("file", "",
				(FilenameUtils.concat(outputdir.toUri().getPath(),  temp_id + ".xml" )));

		Path annotation = new Path(outputdir,html_filename);
		OutputStreamWriter tmpwrt;
		try {
			tmpwrt = new OutputStreamWriter(new FileOutputStream(annotation.toUri().getPath()),"UTF-8");
			tmpwrt.write(html_text);
			tmpwrt.close();
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			//throw new Exception("Problem in encoding during writing HTML");
		} catch (IOException e) {
			e.printStackTrace();
			//throw new Exception("Problem in encoding during writing HTML");
		}

		//Write the XML file
		int parId = 1;

		XMLOutputFactory2 xof = (XMLOutputFactory2) XMLOutputFactory2.newInstance();
		XMLStreamWriter2 xtw1 = null;
		XMLStreamWriter2 xtw = null;
		OutputStreamWriter wrt = null;

		try {
			wrt = new OutputStreamWriter(new FileOutputStream(xml_file.toUri().getPath()),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			xtw1 = (XMLStreamWriter2)
			xof.createXMLStreamWriter(wrt);
			PrettyPrintHandler handler = new PrettyPrintHandler(
					xtw1 );
			xtw = (XMLStreamWriter2)
			Proxy.newProxyInstance(XMLStreamWriter2.class.getClassLoader(),
					new Class[] { XMLStreamWriter2.class }, handler);
			xtw.writeStartDocument();
			
			//if (cesdoc){
			//	xtw.writeProcessingInstruction("xml-stylesheet href='http://nlp.ilsp.gr/panacea/xces-xslt/cesDoc.xsl' type='text/xsl'");
			//}
			xtw.writeStartElement("cesDoc");
			xtw.writeAttribute("version", "0.4");
			//xtw.writeAttribute("xmlns","http://www.xces.org/schema/2003");
			//xtw.writeAttribute("xmlns:xlink","http://www.w3.org/1999/xlink");
			//xtw.writeAttribute("xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance");

			xtw.writeAttribute("xmlns:xlink", cesNameSpace );
			xtw.writeAttribute("xmlns", cesNameSpace1 );
			xtw.writeAttribute("xmlns:xsi", cesNameSpace2 );

			//createHeader(xtw, eAddress, pubDate, lang, title, domain, terms, annotation.toUri().getPath(), format, subdomain);
			createHeader(xtw, eAddress, pubDate, author, publisher, langidentified_total, title, domain, terms, 
					annotation.getName(), format, subdomain, licenseURL,genre,domain_confidence);


			//System.err.println("Now working on file:+fileNo);
			xtw.writeStartElement("text");
			xtw.writeStartElement("body");
			try {

				String lines[] = cleaned_text.split("\n");
				for (String line : lines ) {
					if (line.length()==0) continue;
					if (line.toCharArray()[0]==160)continue;
					xtw.writeStartElement("p");
					xtw.writeAttribute("id", ("p"+parId));
					//if (line.substring(0, 8).equals("<boiler>")) {
					if (line.substring(0,7).equals("<boiler")) {
						if (line.substring(0, 8).equals("<boiler>")) {
							xtw.writeAttribute("crawlinfo","boilerplate");
							line = line.substring(8, line.length()-9);
						}
						else if (line.substring(0, 15).equals("<boiler type='t")) {
							xtw.writeAttribute("crawlinfo","boilerplate");
							xtw.writeAttribute("type","title");
							line = line.substring(21, line.length()-9);
						}
						else if (line.substring(0, 15).equals("<boiler type='h")) {
							xtw.writeAttribute("crawlinfo","boilerplate");
							xtw.writeAttribute("type","heading");
							line = line.substring(23, line.length()-9);
						}
						else if (line.substring(0, 15).equals("<boiler type='l")) {
							xtw.writeAttribute("crawlinfo","boilerplate");
							xtw.writeAttribute("type","listitem");
							line = line.substring(24, line.length()-9);
						}
					}
					else if (line.substring(0, 5).equals("<text")){
						if (line.substring(0,6).equals("<text>")) {
							line = line.substring(6, line.length()-7);
							if (!countWords(line,MIN_TOKENS_PER_PARAGRAPH))
								xtw.writeAttribute("crawlinfo", "ooi-length");
							else if (!lang.isEmpty()){
								langidentified = checkLang(line.toLowerCase());
								//vpapa
								if (!langidentified.equals(langidentified_total))
									xtw.writeAttribute("crawlinfo", "ooi-lang");
								else if (findWords(line, neg_words))
									xtw.writeAttribute("crawlinfo", "ooi-neg");
								else {
									//vpapa
									//foundt = findTopicTerms(line, topic, lang, topicTermsAll);
									foundt = findTopicTerms(line, topic, langidentified_total, topicTermsAll);
									//xtw.writeAttribute("crawlinfo","text");
									//xtw.writeAttribute("type","text");
									if (!foundt.isEmpty())
										xtw.writeAttribute("topic", foundt);
								}
							}
						}
						else if (line.substring(0,13).equals("<text type='t")) {
							line = line.substring(19, line.length()-7);
							if (!countWords(line,MIN_TOKENS_PER_PARAGRAPH)){
								xtw.writeAttribute("crawlinfo", "ooi-length");
								xtw.writeAttribute("type","title");
							}
							else if (!lang.isEmpty()){
								langidentified = checkLang(line.toLowerCase());
								//vpapa
								if (!langidentified.equals(langidentified_total)){
									xtw.writeAttribute("crawlinfo", "ooi-lang");
									xtw.writeAttribute("type","title");
								}
								else if (findWords(line, neg_words)){
									xtw.writeAttribute("crawlinfo", "ooi-neg");
									xtw.writeAttribute("type","title");
								}
								else {
									//vpapa
									//foundt = findTopicTerms(line, topic, lang, topicTermsAll);
									foundt = findTopicTerms(line, topic, langidentified_total, topicTermsAll);
									//xtw.writeAttribute("crawlinfo","text");
									xtw.writeAttribute("type","title");
									if (!foundt.isEmpty()){
										xtw.writeAttribute("topic", foundt);
									}
								}
							}
						}
						else if (line.substring(0,13).equals("<text type='l")) {
							line = line.substring(22, line.length()-7);
							if (!countWords(line,MIN_TOKENS_PER_PARAGRAPH)){
								xtw.writeAttribute("crawlinfo", "ooi-length");
								xtw.writeAttribute("type","listitem");
							}
							else if (!lang.isEmpty()){
								langidentified = checkLang(line.toLowerCase());
								//vpapa
								if (!langidentified.equals(langidentified_total)){
									xtw.writeAttribute("crawlinfo", "ooi-lang");
									xtw.writeAttribute("type","listitem");
								}
								else if (findWords(line, neg_words)){
									xtw.writeAttribute("crawlinfo", "ooi-neg");
									xtw.writeAttribute("type","listitem");
								}
								else {
									//vpapa
									//foundt = findTopicTerms(line, topic, lang, topicTermsAll);
									foundt = findTopicTerms(line, topic, langidentified_total, topicTermsAll);
									//xtw.writeAttribute("crawlinfo","text");
									xtw.writeAttribute("type","listitem");
									if (!foundt.isEmpty()){
										xtw.writeAttribute("topic", foundt);
									}
								}
							}
						}
						else if (line.substring(0,13).equals("<text type='h")) {
							line = line.substring(21, line.length()-7);
							if (!countWords(line,MIN_TOKENS_PER_PARAGRAPH)){
								xtw.writeAttribute("crawlinfo", "ooi-length");
								xtw.writeAttribute("type","heading");
							}
							else if (!lang.isEmpty()){
								langidentified = checkLang(line.toLowerCase());
								//vpapa
								if (!langidentified.equals(langidentified_total)){
									xtw.writeAttribute("crawlinfo", "ooi-lang");
									xtw.writeAttribute("type","heading");
								}
								else if (findWords(line, neg_words)){
									xtw.writeAttribute("crawlinfo", "ooi-neg");
									xtw.writeAttribute("type","heading");
								}
								else {
									//vpapa
									//foundt = findTopicTerms(line, topic, lang, topicTermsAll);
									foundt = findTopicTerms(line, topic, langidentified_total, topicTermsAll);
									//xtw.writeAttribute("crawlinfo","text");
									xtw.writeAttribute("type","heading");
									if (!foundt.isEmpty()){
										xtw.writeAttribute("topic", foundt);
									}
								}
							}
						}
					}
					else {
						//if (line.substring(0, 6).equals("<text>"))
						//if (line.indexOf("<text>")==0)
						//	line = line.substring(6, line.length()-7);													
						if (line.trim().length()<=1) continue;
						//xtw.writeStartElement("p");
						//xtw.writeAttribute("id",("p"+parId));

						if (!countWords(line, MIN_TOKENS_PER_PARAGRAPH )) {
							xtw.writeAttribute("type","length");
						}
						else {
							//String langidentified ="";
							//LanguageIdentifier LangI=new LanguageIdentifier(line); 							
							//langidentified = LangI.getLanguage();
							Detector detector = null;			
							try {
								detector = DetectorFactory.create();
								detector.append(line);
								langidentified = detector.detect();										
							} catch (LangDetectException e) {
								//LOGGER.error(e.getMessage());
							}
							//vpapa
							if (!langidentified.equals(langidentified_total)){
								//not in the right language
								xtw.writeAttribute("type", "lang");
							} else {
								if (findWords(line,neg_words)){	//contain "forbidden" words
									xtw.writeAttribute("type", "content");
								}
								else {
									//does the paragraph contain terms?
									if (topic!=null) {
										String[] tempstr = new String[1];		
										String term;
										ArrayList<String> stems =new ArrayList<String>();
										try {
											stems = TopicTools.analyze(line, langidentified);
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
											xtw.writeAttribute("type","terms");
										}
									}
								}
							}
						}
					}
					xtw.writeCharacters(line);
					xtw.writeEndElement();
					parId++;
				}							
			} catch (Exception e) {
				//LOGGER.error("Could not write file with id " + temp_id);	
				LOGGER.info("Could not write file with id " + temp_id);
				//LOGGER.error(e.getMessage());
				//e.printStackTrace();
				return false;
			}
			xtw.writeEndElement();
			xtw.writeEndElement();
			xtw.writeEndElement();
			xtw.flush();							
		} catch (XMLStreamException e) {
			LOGGER.error("Could not write XML " + xml_file);
			LOGGER.error(e.getMessage());
			return false;			
		} finally {
			try {				
				xtw.close();
				xtw1.close();
				wrt.close();
			} catch (XMLStreamException e) {
				LOGGER.error(e.getMessage());
			} catch (IOException e) {
				LOGGER.error(e.getMessage());
			}
		}
		//xmlFiles.add(xml_file.toString());
		xmlFiles.add(new File(xml_file.toUri()));
		//xmlFiles.add(xml_file.toUri().toString());

		if (SampleExporter.applyOfflineXSLT==true) {
			File inFile = new File(xml_file.toUri());
			File outFile = new File(FilenameUtils.removeExtension(inFile.getAbsolutePath()) + ".xml.html");			
			try {
				SampleExporter.xslTransformer.transform(inFile, outFile);
			} catch (TransformerException e) {
				e.printStackTrace();
				LOGGER.warn("Could not transform " + inFile.getAbsolutePath() + " to " + outFile.getAbsolutePath());
			}
		}
		
		return true;
	}

	/*private static String checkLang1(String partOfLine) {
		String langidentified ="";
		LanguageIdentifier LangI=new LanguageIdentifier(partOfLine); 
		langidentified = LangI.getLanguage();
		//if (!langidentified.equals(targetLang)){
		Detector detector = null;			
		try {
			detector = DetectorFactory.create();
			detector.append(partOfLine);
			langidentified = detector.detect();										
		} catch (LangDetectException e) {
			//comment this exception
			//LOGGER.info("language is not identified for this part of text.");
			//LOGGER.error(e.getMessage());

		}
		//}
		return langidentified;
	}*/

	private static String checkLang(String partOfLine) {
		String langidentified ="";
		//LanguageIdentifier LangI=new LanguageIdentifier(partOfLine); 
		//langidentified = LangI.getLanguage();
		//added comment below and remove comment from above to change language identifiers
		//if (!langidentified.equals(targetLang)){
		Detector detector = null;			
		try {
			detector = DetectorFactory.create();
			detector.append(partOfLine);
			langidentified = detector.detect();										
		} catch (LangDetectException e) {
			//comment this exception
			//LOGGER.info("language is not identified for this part of text.");
			//LOGGER.error(e.getMessage());

		}
		//}
		return langidentified;
	}

	public static String findTopicTerms(String part_of_text,
			ArrayList<String[]> topic_terms, String lang, ArrayList<String> topic_termsALL){
		String found="";
		if (topic_terms==null || lang.isEmpty())
			return found;
		//vpapa
		boolean in_targ_langs=false;
		String[] langs = language.split(";");
		for (int ii=0;ii<langs.length;ii++){
			if (langs[ii].equals(lang)){
				in_targ_langs=true;
				break;
			}
		}
		if (!in_targ_langs)
			return found;


		String[] tempstr = new String[1];		String term;
		ArrayList<String> stems =new ArrayList<String>();
		try {
			stems = analyze(part_of_text, lang);
		} catch (IOException e) {
			e.printStackTrace();
		} 
		String par_text="", term_lang ;
		for (String st:stems){
			par_text+=" "+st;
		}
		par_text = par_text.trim();
		double weight=0.0;

		for (int ii=0;ii<topic_terms.size();ii++){ //for each row of the topic
			tempstr=topic_terms.get(ii);
			weight=Double.parseDouble(tempstr[0]);
			term = tempstr[1];
			//vpapa
			term_lang=tempstr[3];
			if (!term_lang.equals(lang))
				continue;
			Pattern pattern = Pattern.compile(" "+term+" ");	
			Matcher matcher = pattern.matcher(" "+par_text+" ");
			if (matcher.find() & weight>0){
				//found=found+";"+topic_termsALL.get(ii); 
				//found=found+";"+term; 
				found=found+";"+tempstr[4];
			}
		}
		if (!found.isEmpty())
			found=found.substring(1);
		return found;
	}

	public static ArrayList<String> analyze(String text, String lang) throws IOException  {
		ArrayList<String> stems = new ArrayList<String>();
		if (lang.equals("lt")){
			stems = LithuanianAnalyzer.analyze(text);
		}
		else{
			try {
				analyzer = analyzerFactory.getAnalyzer(lang);
			} catch (Exception e) {
				//logger.fatal("Cannot initialize analyzer for lang " + lang);
				e.printStackTrace();
				return null;
			}
			TokenStream tokenStream = analyzer.tokenStream("contents", new StringReader(text));
			CharTermAttribute termAtt = (CharTermAttribute) tokenStream.addAttribute(CharTermAttribute.class);
			while (tokenStream.incrementToken()) {
				stems.add(termAtt.toString());
			}
		}
		return stems;
	}


	public static Boolean countWords(String par_text, int thresh){
		StringTokenizer st = new StringTokenizer(par_text);
		int count=st.countTokens();
		Boolean type=true;
		if (count<thresh){	
			type=false;
		}
		return type;		
	}

	public static Boolean findWords(String par_text, String[] words){
		if (words==null) 
			return false;
		Boolean type=false; int ind=0;
		for (int ii=0;ii<words.length;ii++){
			ind =par_text.indexOf(words[ii]); 
			if (ind>0){
				type=true;
				break;
			}
		}
		return type;		
	}

	private static void createHeader(XMLStreamWriter2 xtw, String url, String pubDate,String author,String publisher,
			String language, String title, String domain, ArrayList<String> terms, 
			String htmlFilename, String file_format, String subdomain, String licenseURL, 
			String genre, double domain_confidence) throws XMLStreamException {
		xtw.writeStartElement("cesHeader");
		xtw.writeAttribute("version", cesDocVersion);
		xtw.writeStartElement("fileDesc");
		xtw.writeStartElement("titleStmt");
		xtw.writeStartElement("title");
		xtw.writeCharacters(title.toString());
		xtw.writeEndElement();
		xtw.writeStartElement("respStmt");
		xtw.writeStartElement("resp");
		xtw.writeStartElement("type");
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
		//xtw.writeCharacters("Panacea project");
		xtw.writeCharacters(researchProject  +" project");
		xtw.writeEndElement();
		xtw.writeStartElement("eAddress");
		xtw.writeAttribute("type", "web");
		//xtw.writeCharacters("http://www.panacea-lr.eu/");
		xtw.writeCharacters("project_website");
		xtw.writeEndElement();
		xtw.writeStartElement("availability");
		if (licenseURL!="" & licenseURL!=null) {
			xtw.writeStartElement("license");
			xtw.writeAttribute("target", licenseURL);
			xtw.writeCharacters("Distributed under a Creative Commons license");
			xtw.writeEndElement();
		} else {
			xtw.writeCharacters("Under review");
		}
		xtw.writeEndElement();
		xtw.writeStartElement("pubDate");
		xtw.writeCharacters(year);
		xtw.writeEndElement();
		xtw.writeEndElement();

		xtw.writeStartElement("sourceDesc");
		xtw.writeStartElement("biblStruct");
		xtw.writeStartElement("monogr");
		xtw.writeStartElement("title");
		xtw.writeCharacters(title.toString());
		xtw.writeEndElement();
		xtw.writeStartElement("author");
		if (author==null){author="";}
		xtw.writeCharacters(author);
		xtw.writeEndElement();
		xtw.writeStartElement("imprint");
		
		xtw.writeStartElement("format");
		xtw.writeCharacters(file_format);
		xtw.writeEndElement();
		
		xtw.writeStartElement("publisher");
		if (publisher==null){publisher="";}
		xtw.writeCharacters(publisher);
		xtw.writeEndElement();
		xtw.writeStartElement("pubDate");
		xtw.writeCharacters(pubDate);
		xtw.writeEndElement();
		xtw.writeStartElement("eAddress");
		xtw.writeCharacters(url);
		xtw.writeEndElement();
		xtw.writeEndElement();

		xtw.writeEndElement();
		xtw.writeEndElement();
		xtw.writeEndElement();
		xtw.writeEndElement();
		xtw.writeStartElement("profileDesc");

		xtw.writeStartElement("langUsage");
		xtw.writeStartElement("language");
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




	public void setMIN_TOKENS_PER_PARAGRAPH(int mIN_TOKENS_PER_PARAGRAPH) {
		MIN_TOKENS_PER_PARAGRAPH = mIN_TOKENS_PER_PARAGRAPH;
	}
	public void setMIN_TOKENS_NUMBER(int mIN_TOKENS_NUMBER) {
		MIN_TOKENS_NUMBER = mIN_TOKENS_NUMBER;
	}
	public static String getCrawlDirName() {
		return crawlDirName;
	}

	public void setCrawlDirName(String crawlDirName) {
		SampleExporter.crawlDirName = crawlDirName;
	}

	public void setLanguage(String language) {
		SampleExporter.language = language;
	}

	public static String getLanguage() {
		return language;
	}

	public static String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		SampleExporter.topic = topic;
	}

	public static String getNegWordsFile() {
		return negWordsFile;
	}

	public void setNegWordsFile(String negWordsFile) {
		SampleExporter.negWordsFile = negWordsFile;
	}

	public void setOutputFile(String outputFile) {
		SampleExporter.outputFile  = outputFile;
	}

	public void setOutputFileHTML(String outputFileHTML) {
		SampleExporter.outputFileHTML  = outputFileHTML;
	}

	public void setOutputDir(String outputDir) {
		SampleExporter.outputDir = outputDir;
	}
	public void setTextExport(boolean textexport){
		SampleExporter.textExport = textexport;
	}
	
	public void setStyleExport(boolean cesdoc){
		SampleExporter.cesdoc = cesdoc;
	}
	public void setHTMLOutput(boolean html){
		SampleExporter.html = html;
	}
	public void setAcceptedMimeTypes(String[] mimes){
		SampleExporter.mimetypes = mimes;
	}
	public void setTargetedDomain(String targeteddomain){
		SampleExporter.targeteddomain = targeteddomain;
	}
	/**
	 * @return the researchProject
	 */
	public static String getResearchProject() {
		return researchProject;
	}

	/**
	 * @param researchProject the researchProject to set
	 */
	public static void setResearchProject(String researchProject) {
		SampleExporter.researchProject = researchProject;
	}

	/**
	 * @return the applyOfflineXSLT
	 */
	public boolean isApplyOfflineXSLT() {
		return applyOfflineXSLT;
	}

	/**
	 * @param applyOfflineXSLT the applyOfflineXSLT to set
	 */
	public void setApplyOfflineXSLT(boolean applyOfflineXSLT) {
		SampleExporter.applyOfflineXSLT = applyOfflineXSLT;
		if (applyOfflineXSLT==true) {
			try {
				SampleExporter.xslTransformer = new XSLTransformer();
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
			if (SampleExporter.xslTransformer==null) {
				LOGGER.warn("Cannot initialize xslTransformer. Will not transorm XML files using xslt.");
				SampleExporter.applyOfflineXSLT=false;
			}
		}
	}


	/*	public static Boolean XMLExporter_OR(Path outputdir, String format, String title, String eAddress,
	String lang, String html_text, String cleaned_text, int id, String pubDate, String domain, String subdomain,
	ArrayList<String> terms, ArrayList<String[]> topic, String[] neg_words) { //throws Exception {
StringTokenizer tkzr = new StringTokenizer(cleaned_text);
if (tkzr.countTokens()<minTokensNumber){
	//System.out.println("CUT: "+ eAddress);
	return false;		
}

//Filename of files to be written.
String temp_id=Integer.toString(id);
String html_filename = temp_id+".html";
Path xml_file = new Path(outputdir,temp_id+".xml");
Path annotation = new Path(outputdir,html_filename);
OutputStreamWriter tmpwrt;
try {
	tmpwrt = new OutputStreamWriter(new FileOutputStream(annotation.toUri().getPath()),"UTF-8");
	tmpwrt.write(html_text);
	tmpwrt.close();
} catch (UnsupportedEncodingException e1) {
	e1.printStackTrace();
} catch (FileNotFoundException e1) {
	e1.printStackTrace();
	//throw new Exception("Problem in encoding during writing HTML");
} catch (IOException e) {
	e.printStackTrace();
	//throw new Exception("Problem in encoding during writing HTML");
}


//Write the XML file
int parId = 1;

XMLOutputFactory2 xof = (XMLOutputFactory2) XMLOutputFactory2.newInstance();
XMLStreamWriter2 xtw1 = null;
XMLStreamWriter2 xtw = null;
OutputStreamWriter wrt = null;

try {
	wrt = new OutputStreamWriter(new FileOutputStream(xml_file.toUri().getPath()),"UTF-8");
} catch (UnsupportedEncodingException e) {
	e.printStackTrace();
} catch (FileNotFoundException e) {
	e.printStackTrace();
}
try {
	xtw1 = (XMLStreamWriter2)
	xof.createXMLStreamWriter(wrt);
	PrettyPrintHandler handler = new PrettyPrintHandler(
			xtw1 );
	xtw = (XMLStreamWriter2)
	Proxy.newProxyInstance(XMLStreamWriter2.class.getClassLoader(),
			new Class[] { XMLStreamWriter2.class }, handler);
	xtw.writeStartDocument();
	xtw.writeStartElement("cesDoc");
	xtw.writeAttribute("version", "0.4");
	xtw.writeAttribute("xmlns",
	"http://www.xces.org/schema/2003");
	xtw.writeAttribute("xmlns:xlink",
	"http://www.w3.org/1999/xlink");
	xtw.writeAttribute("xmlns:xsi",
	"http://www.w3.org/2001/XMLSchema-instance");
	createHeader(xtw, eAddress, pubDate, lang, title, domain, terms, annotation.toUri().getPath(), format, subdomain);
	//System.err.println("Now working on file:+fileNo);
	xtw.writeStartElement("text");
	xtw.writeStartElement("body");
	try {

		String lines[] = cleaned_text.split("\n");
		for (String line : lines ) {
			if (line.length()==0) continue;
			if (line.toCharArray()[0]==160)continue;

			//if (line.substring(0, 8).equals("<boiler>")) {
			if (line.indexOf("<boiler>")==0){
				line = line.substring(8, line.length()-9);
				if (line.trim().length()<=1) continue;						
				xtw.writeStartElement("p");
				xtw.writeAttribute("id",("p"+parId));
				xtw.writeAttribute("type","boilerplate");







			}else {
				//if (line.substring(0, 6).equals("<text>"))
				if (line.indexOf("<text>")==0)
					line = line.substring(6, line.length()-7);													
				if (line.trim().length()<=1) continue;
				xtw.writeStartElement("p");
				xtw.writeAttribute("id",("p"+parId));

				if (!countWords(line, MIN_TOKENS_PER_PARAGRAPH )) {
					xtw.writeAttribute("type","length");
				}
				else {
					String langidentified ="";
					//LanguageIdentifier LangI=new LanguageIdentifier(line); 							
					//langidentified = LangI.getLanguage();
					Detector detector = null;			
					try {
						detector = DetectorFactory.create();
						detector.append(line);
						langidentified = detector.detect();										
					} catch (LangDetectException e) {
						//LOGGER.error(e.getMessage());
					}
					if (!langidentified.equals(lang)){
						//not in the right language
						xtw.writeAttribute("type", "lang");
					} else {
						if (findWords(line,neg_words)){	//contain "forbidden" words
							xtw.writeAttribute("type", "content");
						}
						else {
							//does the paragraph contain terms?
							String[] tempstr = new String[1];		
							String term;
							ArrayList<String> stems =new ArrayList<String>();
							try {
								stems = TopicTools.analyze(line, langidentified);
							} catch (IOException e) {
								e.printStackTrace();
							} 
							String par_text="";
							for (String st:stems){
								par_text=par_text.concat(" "+st);
							}
							par_text = par_text.trim();
							Boolean found = false;
							if (topic!=null) {
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
									xtw.writeAttribute("type","terms");
								}
							}
						}
					}
				}
			}
			xtw.writeCharacters(line);
			xtw.writeEndElement();
			parId++;
		}							
	} catch (Exception e) {
		LOGGER.error("Could not write file with id " + temp_id);	
		LOGGER.error(e.getMessage());
		e.printStackTrace();
		return false;
	}
	xtw.writeEndElement();
	xtw.writeEndElement();
	xtw.writeEndElement();
	xtw.flush();							
} catch (XMLStreamException e) {
	LOGGER.error("Could not write XML " + xml_file);
	LOGGER.error(e.getMessage());
	return false;			
} finally {
	try {				
		xtw.close();
		xtw1.close();
		wrt.close();
	} catch (XMLStreamException e) {
		LOGGER.error(e.getMessage());
	} catch (IOException e) {
		LOGGER.error(e.getMessage());
	}
}

return true;
}*/

	
	
}
