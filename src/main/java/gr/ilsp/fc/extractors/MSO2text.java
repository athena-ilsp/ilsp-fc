package gr.ilsp.fc.extractors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.poi.POITextExtractor;
import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.xmlbeans.XmlException;

public class MSO2text {
	//private static final Logger LOGGER = Logger.getLogger(MSO2text.class);
	private static final String creator = "Creator";
	private static final String publisher = "Publisher";
	private static final String publisherp = "PID_COMPANY";
	private static final String author1 = "PID_AUTHOR";
	private static final String author2 = "PID_LASTAUTHOR";
	private static final String keywords = "Keywords";
	private static final String keywordsp = "PID_KEYWORDS";
	private static final String title = "Title";
	private static final String titlep = "PID_TITLE";
	private static final String subject = "Subject";
	private static final String subjectp = "PID_SUBJECT";
	private static final String text_tag = "<text>";
	private static final String text_tag_en = "</text>";

	public static void main( String[] args ){
		//File infile = new File("C:/Users/vpapa/test/test_20160803_165808/5c9d811d-37ef-4199-ae29-699623a51012/doc/0.doc");
		File infile = new File(args[0]);
		try {
			Map<String, String> docdata = run1(infile);
			if (!docdata.isEmpty())
				FileUtils.writeStringToFile(new File(args[1]), docdata.get("content"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Map<String, String> run1(File infile) {
		Map<String, String> docdata =new HashMap<String, String>();
		InputStream in;
		try {
			in = new FileInputStream(infile);
			POITextExtractor poitex = ExtractorFactory.createExtractor(in);
			String content = poitex.getText();			//System.out.println(poitex.getText());
			POITextExtractor metapoitex  = poitex.getMetadataTextExtractor();
			String metadataString = metapoitex.getText(); //System.out.println(metadataString);
			in.close();
			//System.out.println(metadataString);
			docdata = processDocMetadataData(metadataString, content);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OpenXML4JException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (IllegalArgumentException e){
			e.printStackTrace();
		}catch (Exception e){
			e.printStackTrace();
		}
		return docdata;
	}

	private static Map<String, String> processDocMetadataData(String metadataString, String content) {
		Map<String, String> docdata = new HashMap<String, String>();
		docdata.put("author", "");
		docdata.put("title", "");
		docdata.put("publisher", "");
		docdata.put("keywords",  "");
				
		String[] pars = content.split("\n");
		String cleancontent = "";
		for (String par:pars){
			if (par.isEmpty())
				continue;
			cleancontent = cleancontent+text_tag+par+text_tag_en+"\n";
		}
		docdata.put("content", cleancontent);
		String[] metalines = metadataString.split("\n");
		Map<String, String> meta = new HashMap<String, String>();
		for (String metaline:metalines){
			String[] temp = metaline.split("=");
			if (temp.length!=2 || temp[0].trim().isEmpty())
				continue;
			temp[0] = temp[0].trim(); temp[1] = temp[1].trim();
			meta.put(temp[0], temp[1]);
		}
		String authors = "";
		if (meta.containsKey(creator))
			authors = meta.get(creator).trim(); //docdata.put("author", meta.get(creator));
		if (meta.containsKey(title))
			docdata.put("title", meta.get(title));
		if (meta.containsKey(subject))
			docdata.put("subject", meta.get(subject));
		if (meta.containsKey(keywords))
			docdata.put("keywords", meta.get(keywords));
		if (meta.containsKey(keywordsp))
			docdata.put("keywords", meta.get(keywordsp));
		if (meta.containsKey(subjectp))
			docdata.put("subject", meta.get(subjectp));
		if (meta.containsKey(titlep))
			docdata.put("title", meta.get(titlep));
		if (meta.containsKey(author1))
			authors =  meta.get(author1);
		if (meta.containsKey(author2)){
			if (authors.isEmpty())
				authors =  meta.get(author2);
			else
				authors= authors+","+meta.get(author2);
		}
		docdata.put("author", authors);
		if (meta.containsKey(publisher))
			docdata.put("publisher", meta.get(publisher));
		if (meta.containsKey(publisherp))
			docdata.put("publisher", meta.get(publisherp));
		return docdata;
	}

	/*	public static Map<String, String> run2(File infile) {
		Map<String, String> docdata = new HashMap<String, String>();
		InputStream in;
		try {
			in = new FileInputStream(infile);
			POIFSFileSystem poifs = new POIFSFileSystem(in);
			//XWPFDocument doc = new XWPFDocument(fs);
			HWPFDocument doc = new HWPFDocument(in);
			DirectoryEntry dir = poifs.getRoot();
			SummaryInformation si = null;
			DocumentEntry siEntry = (DocumentEntry)
					dir.getEntry(SummaryInformation.DEFAULT_STREAM_NAME);
			DocumentInputStream dis = new DocumentInputStream(siEntry);
			PropertySet ps = new PropertySet(dis);
			si = new SummaryInformation(ps);
			if (si.getTitle() != null) 
				docdata.put("title",si.getTitle());
			if (si.getAuthor() != null) 
				docdata.put("author",si.getAuthor());
			if (si.getSubject() != null) 
				docdata.put("subject",si.getSubject());
			if (si.getKeywords() != null) 
				docdata.put("keywords",si.getKeywords());
			//if (si.getLastSaveDateTime() != null)
			//	docdata.put("published",si.getLastSaveDateTime().toString());
			//else if (si.getCreateDateTime() != null) 
			//	docdata.put("published",si.getCreateDateTime().toString());
			String content = "";
			//for (XWPFParagraph p : doc.getParagraphs()){ 
			//	content = content+"\n"+p.getParagraphText(); 	//docpars.add(p.getParagraphText());
			//}

			//doc.g


			docdata.put("content",content);
		} catch (FileNotFoundException e) {
			LOGGER.error("problem in creating inoutstream of file "+ infile.getAbsolutePath());
			e.printStackTrace();
		} catch (IOException e) {
			LOGGER.error("problem in reading inoutstream of file "+ infile.getAbsolutePath());
			e.printStackTrace();
		} catch (NoPropertySetStreamException e) {
			LOGGER.error("no property setstream for "+ infile.getAbsolutePath());
			e.printStackTrace();
		} catch (MarkUnsupportedException e) {
			LOGGER.error("MarkUnsupported for file "+ infile.getAbsolutePath());
			e.printStackTrace();
		} catch (UnexpectedPropertySetTypeException e) {
			LOGGER.error("Unexpected PropertySetType for "+ infile.getAbsolutePath());
			e.printStackTrace();
		}
		return docdata;
	}*/

}
