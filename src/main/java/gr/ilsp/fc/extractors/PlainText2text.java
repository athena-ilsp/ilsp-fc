package gr.ilsp.fc.extractors;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import gr.ilsp.nlp.commons.Constants;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.txt.TXTParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;


public class PlainText2text {
	private static final Logger LOGGER = Logger.getLogger(PlainText2text.class);
	private static final String attr_publisher = "publisher";
	private static final String attr_author = "author";
	private static final String attr_keywords = "keywords";
	private static final String attr_title = "title";
	private static final String attr_content = "content";
	private static final String text_tag = "<text>";
	private static final String text_tag_en = "</text>";

	public static void main( String[] args ){
		//File infile = new File("C:/Users/vpapa/test/test_20160803_165808/5c9d811d-37ef-4199-ae29-699623a51012/doc/0.doc");
		File infile = new File(args[0]);
		try {
			Map<String, String> txtdata = run1(infile);
			if (!txtdata.isEmpty())
				FileUtils.writeStringToFile(new File(args[1]), txtdata.get(attr_content), Constants.UTF8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Text and metadata extraction from plain text file 
	 * @param file
	 * @return
	 */
	public static Map<String, String> run1(File file) {
		Map<String, String> txtdata =new HashMap<String, String>();
		txtdata.put(attr_author, "");
		txtdata.put(attr_title, "");
		txtdata.put(attr_publisher, "");
		txtdata.put(attr_keywords,  "");

		try {
			String content = FileUtils.readFileToString(file, Constants.UTF8);
			String[] pars = content.split("\n");
			String cleancontent = "";
			for (String par:pars){
				if (par.isEmpty())
					continue;
				cleancontent = cleancontent+text_tag+par+text_tag_en+"\n";
			}
			txtdata.put(attr_content, cleancontent);

			ContentHandler handler = new BodyContentHandler(1000000000);
			Metadata metadata = new Metadata();
			FileInputStream inputstream = new FileInputStream(file);
			TXTParser txtparser = new TXTParser();
			txtparser.parse(inputstream, handler, metadata,new ParseContext());
			//String[] metadataNames = metadata.names();
			//for(String name : metadataNames) {
			//	System.out.println(name + ":   " + metadata.get(name));  
			//}

			String author = metadata.get(attr_author);	
			if (author!=null)	
				txtdata.put(attr_author, author);
			String title = metadata.get(attr_title);	
			if (title!=null)	
				txtdata.put(attr_title, title);	
			String publisher = metadata.get(attr_publisher);
			if (publisher!=null)	
				txtdata.put(attr_publisher, publisher);	
			String termsArray = metadata.get(attr_keywords);	
			List<String> terms = new ArrayList<String>();
			if (termsArray!=null){
				for (String s: termsArray.split(",|;|:"))
					terms.add(s.trim());
				txtdata.put(attr_keywords, terms.toString());
			}
		} catch (IOException e) {
			LOGGER.error("problem in reading "+ file.getAbsolutePath());
			e.printStackTrace();
		} catch (SAXException e) {
			LOGGER.error("problem in extracting metadata of "+ file.getAbsolutePath());
			e.printStackTrace();
		} catch (TikaException e) {
			LOGGER.error("problem in extracting metadata of "+ file.getAbsolutePath());
			e.printStackTrace();
		}
		return txtdata;
	}

}
