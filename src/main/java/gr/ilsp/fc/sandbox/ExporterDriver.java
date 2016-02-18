package gr.ilsp.fc.sandbox;

import gr.ilsp.fc.exporter.Exporter;

import java.io.File;
import java.net.URL;

public class ExporterDriver {

//	options.getlength()=0
//	options.getminTokenslength()=80
//	options.getLanguage()=en;hr
//	options.getTopic()=null
//	options.getAlign()=false

//	se.setHTMLOutput(true)
//options.isOfflineXSLT()=true

//outputDirName=c:\tmp\crawl_results\test-Dup_20141031_125133\b0865a5e-4b9a-4d62-979f-bb2936d7a964
//options.getOutputFile()=c:\tmp\crawl_results\output_test_Dup_EN-HR.txt
//	options.getOutputFileHTML()=c:\tmp\crawl_results\output_test_Dup_EN-HR.txt.html

//	mimes=[text/html, text/plain, application/xhtml+xml]
//	options.getTargetedDomain()=null
//	options.getGenre()=null
	
	static int MIN_TOKENS_PER_PAR = 0;
	static int MIN_TOKENS = 80;
	static String lang = "en;hr";

	static File topic = null;
	static boolean styleExport = false;
	static boolean htmlOutput = true;
	static boolean applyOfflineXSLT = true;
	static String[] acceptedMimeTypes = {"text/html", "text/plain", "application/xhtml+xml"} ;
	static String targetedDomain = null;
	static URL genres = null;

	static File crawlDirName =new File( "/home/prokopis/ABU/tz-malilos_20141030_131828/393d307f-c609-4163-ba24-67e013204478/");
	static File outputFile = new File("/home/prokopis/ABU/tz-malilos_20141030_131828/output_test_Dup_EN-HR.txt");
	static File outputFileHtml = new File("/home/prokopis/ABU/tz-malilos_20141030_131828/output_test_Dup_EN-HR.txt.html");

	public static void main(String[] args) {
		Exporter se = new Exporter();
		se.setMIN_TOKENS_PER_PARAGRAPH(MIN_TOKENS_PER_PAR);
		se.setMIN_TOKENS_NUMBER(MIN_TOKENS);
		se.setTargetLanguages(lang.split(";"));
		se.setHTMLOutput(htmlOutput);
		se.setApplyOfflineXSLT(applyOfflineXSLT);
		se.setAcceptedMimeTypes(acceptedMimeTypes);
		se.setTargetedDomain(targetedDomain);
		se.setGenres(genres);
		se.setTopic(topic);
		se.setStyleExport(styleExport); 

		se.setCrawlDirName(crawlDirName);
		se.setOutputFileHTML(outputFileHtml);
		se.setOutputFile(outputFile);	
		se.export(true);
	}
	
}
