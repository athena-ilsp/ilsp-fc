package gr.ilsp.fc.cleaner;

import gr.ilsp.fc.main.ReadResources;
import gr.ilsp.fc.utils.ContentNormalizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.apache.tika.metadata.Metadata;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.kohlschutter.boilerpipe.BoilerpipeProcessingException;
import com.kohlschutter.boilerpipe.document.TextBlock;
import com.kohlschutter.boilerpipe.document.TextDocument;
import com.kohlschutter.boilerpipe.extractors.ArticleExtractor;
import com.kohlschutter.boilerpipe.extractors.ExtractorBase;
import com.kohlschutter.boilerpipe.extractors.NumWordsRulesExtractor;


public class CleanerUtils {
	private static final Logger LOGGER = Logger.getLogger(CleanerUtils.class);
	private static final String LANGUAGE_ELE = "language";
	private static final String LANGUAGE_ATT = "iso639";
	private static final String P_ELE = "p";
	private static final String morethan = ">" ; 
	private static final String lessthan = "<" ; 
	private static final String text_st = "<text";
	private static final String boiler_st = "<boiler";
	private static final String text_en = "text>";
	private static final String boiler_en = "boiler>";

	/**
	 * Returns the main content of a fetched webpage.
	 * @param input
	 * @param metadata
	 * @param keepBoiler  (annotated as boilerplate and text if true, or only text parts if false)
	 * @return
	 */
	public static String getContent(InputStream input, Metadata metadata, boolean keepBoiler) {
			ExtractorBase  arcExtr = ArticleExtractor.INSTANCE;
			ExtractorBase  numExtr = NumWordsRulesExtractor.INSTANCE;
			return getContent(numExtr, arcExtr, input, metadata, keepBoiler) ;
	}

	
	public static String getContent(InputStream input, boolean keepBoiler) {
		ExtractorBase  arcExtr = ArticleExtractor.INSTANCE;
		ExtractorBase  numExtr = NumWordsRulesExtractor.INSTANCE;
		return getContent(numExtr, arcExtr, input, keepBoiler) ;
}
	
	/**
	 * Returns the main content of a fetched webpage using a combination of NumofWords boilerpipeExtractor and Aritcle boilerpipeExtractor
	 * @param boilerpipeExtractor
	 * @param input
	 * @param metadata
	 * @param keepBoiler
	 * @return
	 */
	public static String getContent(ExtractorBase  numExtr, ExtractorBase  arcExtr, InputStream input, Metadata metadata, boolean keepBoiler) {
		String content="";
		try {
			input.reset();
			BufferedReader reader = new BufferedReader(new InputStreamReader(input,metadata.get(Metadata.CONTENT_ENCODING)));
			String[] content1 = numExtr.getText(reader, true).split("\n");
			input.reset();
			reader = new BufferedReader(new InputStreamReader(input,metadata.get(Metadata.CONTENT_ENCODING)));
			String[] content2 = arcExtr.getText(reader, false).split("\n");
			content = combineNumArt(content1, content2);
			if (!keepBoiler) {
				content = ContentNormalizer.removeBoilerPars(content);
			}
			reader.close();
			reader=null;
		} catch (IOException e) {
			LOGGER.warn("Problem in reading content of webpage " +metadata.get(Metadata.LOCATION));
			e.printStackTrace();
		} catch (BoilerpipeProcessingException e) {
			LOGGER.warn("Problem in calling Boilerpipe for webpage " +metadata.get(Metadata.LOCATION));
			e.printStackTrace();
		}
		//if (keepBoiler) content = CleanerUtils.cleanContent(content);
		content = ContentNormalizer.normalizeText(content);
		return content;
	}
	
	
	/**
	 * Returns the main content of a fetched webpage using a combination of NumofWords boilerpipeExtractor and Aritcle boilerpipeExtractor
	 * @param boilerpipeExtractor
	 * @param input
	 * @param metadata
	 * @param keepBoiler
	 * @return
	 */
	public static String getContent(ExtractorBase  numExtr, ExtractorBase  arcExtr, InputStream input, boolean keepBoiler) {
		String content="";
		try {
			input.reset();
			BufferedReader reader = new BufferedReader(new InputStreamReader(input,"UTF-8"));
			String[] content1 = numExtr.getText(reader, true).split("\n");
			input.reset();
			reader = new BufferedReader(new InputStreamReader(input,"UTF-8"));
			String[] content2 = arcExtr.getText(reader, false).split("\n");
			content = combineNumArt(content1, content2);
			if (!keepBoiler) {
				content = ContentNormalizer.removeBoilerPars(content);
			}
			reader.close();
			reader=null;
		} catch (IOException e) {
			LOGGER.warn("Problem in reading content of webpage ");
			e.printStackTrace();
		} catch (BoilerpipeProcessingException e) {
			LOGGER.warn("Problem in calling Boilerpipe for webpage ");
			e.printStackTrace();
		}
		//if (keepBoiler) content = CleanerUtils.cleanContent(content);
		content = ContentNormalizer.normalizeText(content);
		return content;
	}
	
	
	
	
	/**
	 * changes the output of NumWordExtractor according to ArticleExtractor  
	 * @param numExtr_text
	 * @param articExtr_text
	 * @return
	 */
	private static String combineNumArt(String[] numExtr_text, String[] articExtr_text) {
		String res="", temp;
		for (int jj=0;jj<numExtr_text.length;jj++){
			if (numExtr_text[jj].startsWith(text_st)){
				numExtr_text[jj] = numExtr_text[jj].replaceAll(text_st, boiler_st );
				numExtr_text[jj] = numExtr_text[jj].replaceAll(text_en, boiler_en);
			}
		}
		int lim=0;
		for (int ii=0;ii<articExtr_text.length;ii++){
			int a = articExtr_text[ii].indexOf(morethan);
			int b = articExtr_text[ii].lastIndexOf(lessthan);
			if (a<0) 
				a=0;
			else
				a=a+1;
			if (b<=0) 
				b=articExtr_text[ii].length();
			if (b<a){
				a=0;
			}
			temp = articExtr_text[ii].substring(a, b);
			for (int jj=lim;jj<numExtr_text.length;jj++){
				if (numExtr_text[jj].contains(morethan+temp+lessthan)){
					numExtr_text[jj] = numExtr_text[jj].replaceAll(boiler_st, text_st);
					numExtr_text[jj] = numExtr_text[jj].replaceAll(boiler_en, text_en);
					lim=jj+1;
					break;
				}
			}
		}
		for (int jj=0;jj<numExtr_text.length;jj++){
			res=res+numExtr_text[jj]+"\n";
		}
		res=res.substring(0, res.length()-1);
		return res;
	}

	
	
	
	
	
	
/*	*//**
	 * Returns the main content of a fetched webpage.
	 * @param input
	 * @param metadata
	 * @param keepBoiler  (annotated as boilerplate and text if true, or only text parts if false)
	 * @return
	 *//*
	public static String getContentOld(InputStream input, Metadata metadata, boolean keepBoiler) {
		String content="";
		try {
			input.reset();
			//BufferedReader reader = new BufferedReader(new InputStreamReader(input,metadata.get(Metadata.CONTENT_ENCODING)));            

			InputSource source = new InputSource(new InputStreamReader(input,metadata.get(Metadata.CONTENT_ENCODING)));
			BoilerpipeSAXInput boilerp;
			try {
				boilerp = new BoilerpipeSAXInput(source);
				TextDocument textDocument = boilerp.getTextDocument();
				if (keepBoiler) {
					ArticleExtractor.INSTANCE.getText(textDocument);
					String res = getBoilerpipePars(textDocument,keepBoiler);
					//content = textDocument.getText(true, true);
					//String res = HTMLHighlighter.newHighlightingInstance().process(textDocument, source);

				}else{
					content = ArticleExtractor.INSTANCE.getText(textDocument);
				}
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			System.out.println(content);
			if (keepBoiler) {   
				// FIXME Create Parsers of BoilePipe's extractor 
				// FIXME, papadopoule
				content = NumWordsRulesExtractor.INSTANCE.getText(reader);
				content = ArticleExtractor.INSTANCE.getText(reader);
				System.out.println(content);
			} else {
				content = NumWordsRulesExtractor.INSTANCE.getText(reader);
				//content = ArticleExtractor.INSTANCE.getText(reader);
			}
			//reader.close();
			//reader=null;
		} catch (IOException e) {
			LOGGER.warn("Problem in reading content of webpage " +metadata.get(Metadata.LOCATION));
			e.printStackTrace();
		} catch (BoilerpipeProcessingException e) {
			LOGGER.warn("Problem in calling Boilerpipe for webpage " +metadata.get(Metadata.LOCATION));
			e.printStackTrace();
			//		} catch (de.l3s.boilerpipe.BoilerpipeProcessingException e) {
			//			LOGGER.warn("Problem in calling extractor of Boilerpipe for webpage " +metadata.get(Metadata.LOCATION));
			//			e.printStackTrace();
		}
		if (keepBoiler) content = CleanerUtils.cleanContent(content);
		content = ContentNormalizer.normalizeText(content);
		return content;
	}*/



	public static String getBoilerpipePars(TextDocument textDocument, boolean includeNonContent) {
		StringBuilder sb = new StringBuilder();
		String temp = "";
List<TextBlock> blocks =textDocument.getTextBlocks();
		LOOP: for (TextBlock block : blocks) {
			//System.err.println(block.toString());
			String type = "";
			String typeAtt = "";

			Set <String> labels = block.getLabels();
			if (labels!= null) {
				Iterator<String> iter = block.getLabels().iterator();
				while (iter.hasNext()) {
					type =  iter.next() + " ";
				}
			}
			type=type.trim();

			if (!type.isEmpty()) {
				typeAtt = " type=\'"+type+"\'";
			}

			// System.err.println(block);
			if(block.isContent()) {
				//if(!textDocument) {
				//	continue LOOP;
				//}
				if(!includeNonContent) {
					temp = block.getText();
				} 
				else{
					temp = "<text" + typeAtt + ">"+block.getText().trim()+"</text>";	
				}
			} else {
				if(!includeNonContent) {
					continue LOOP;
				}
				temp = "<boiler" + typeAtt + ">"+block.getText().trim()+"</boiler>";
			}
			//sb.append(block.getText());
			sb.append(temp);
			sb.append('\n');
		}
		return sb.toString();
	}


	/**
	 * Removes special patterns (structural info) added during cleaning 
	 * @param content
	 * @return
	 */
	public static String cleanContent(String content){
		String result = "";
		String REGEX = "<text.*>.*</text>";
		String REPLACE = " ";
		Pattern p = Pattern.compile(REGEX);
		Matcher m = p.matcher(content);
		String text = "";
		while (m.find()){
			text = m.group();
			text = text.replaceAll(" type='listelem'","");
			text = text.replaceAll(" type='title'","");
			text = text.replaceAll(" type='heading'","");
			text = text.replaceAll("</?text>", REPLACE);
			result = result.concat(text);
		}
		return result;
	}


	/**
	 * holds filenames of files in which the paragraph occur, and ids of this paragraph in each file
	 * @author vpapa
	 *
	 */
	public static class ParsAttr {
		public List<String> filenames;
		public List<Double> par_startids;
		public List<Double> par_endids;

		public ParsAttr(List<String> filenames, List<Double> par_startids, List<Double> par_endids) {
			this.filenames = filenames;
			this.par_startids = par_startids;
			this.par_endids = par_endids;
		}
	}

	/**
	 * Gets a list of CesDoc Files and classifies them regarding the identified language
	 * Returns a hashMap (key stands for the identified language and value holds the files in this language)
	 * @param files
	 * @return
	 */
	public static HashMap<String, ArrayList<File>> getFilesPerLang(ArrayList<File> files) {
		HashMap<String, ArrayList<File>> langs_files= new HashMap<String, ArrayList<File>>();
		String lang;
		ArrayList<File> temp = new ArrayList<File>();
		for (File file:files){
			//lang = ReadResources.extractLangfromXML(file.getAbsolutePath(),LANGUAGE_ELE, LANGUAGE_ATT);
			lang = ReadResources.extractAttrfromXML(file.getAbsolutePath(), LANGUAGE_ELE, LANGUAGE_ATT, true, false);
			if 	(langs_files.containsKey(lang)){
				temp = langs_files.get(lang);	
			}else{
				temp = new ArrayList<File>();
			}
			temp.add(file);
			langs_files.put(lang, temp);
		}
		return langs_files;
	}


	/**
	 * Gets a list of CesDoc files, reads them and gets the "crawlinfo=boilerplate" paragraphs in all these cesDoc files
	 * Returns a HashMap (key is for the paragraphs and value is the frequency of each paragraph)
	 * @param files
	 * @return
	 */

	public static Set<String> getBoilerPars(ArrayList<File> files) {
		Set<String> boilerpars  =new HashSet<String>();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {	
			db = dbf.newDocumentBuilder();
			for (File file:files){
				Document doc = db.parse(file.getAbsolutePath());
				doc.getDocumentElement().normalize();
				NodeList nodeLstP = doc.getElementsByTagName(P_ELE);
				for(int s=0; s<nodeLstP.getLength() ; s++){
					Element NameElement = (Element)nodeLstP.item(s);
					if (NameElement.hasAttribute("crawlinfo")){
						if (NameElement.getAttributeNode("crawlinfo").getTextContent().equals("boilerplate")){
							if (!NameElement.getTextContent().trim().isEmpty()){
								boilerpars.add(NameElement.getTextContent());
							}
						}
					}
				}
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return boilerpars;
	}


	/**
	 * Gets a list of CesDoc files, reads them and counts the frequency of each paragraph in all these cesDoc files
	 * Returns a HashMap (key is for the paragraphs and value is the frequency of each paragraph)
	 * @param files
	 * @return
	 */

	public static HashMap<String, ParsAttr> getParsAttrs(ArrayList<File> files, String attr, String val, boolean has) {
		String paragraph;
		HashMap<String, ParsAttr> pars_attrs  =new HashMap<String, ParsAttr>();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {	
			db = dbf.newDocumentBuilder();
			for (File file:files){
				List<String> filenames = new ArrayList<String>();;
				List<Double> par_startids = new ArrayList<Double>();
				List<Double> par_endids = new ArrayList<Double>();
				Document doc = db.parse(file.getAbsolutePath());
				doc.getDocumentElement().normalize();
				NodeList nodeLstP = doc.getElementsByTagName(P_ELE);
				boolean found=false;
				for(int s=0; s<nodeLstP.getLength() ; s++){
					found = false;
					Element NameElement = (Element)nodeLstP.item(s);
					paragraph = NameElement.getTextContent().trim();
					if (paragraph.isEmpty()){
						continue;
					}
					if (has){
						if (NameElement.hasAttribute(attr)){
							if (NameElement.getAttributeNode(attr).getTextContent().equals(val)){
								found = true;
							}
						}
					}else{
						if (!NameElement.hasAttribute(attr)){
							found=true;
						}else{
							if (!NameElement.getAttributeNode(attr).getTextContent().equals(val)){
								found = true;
							}
						}
					}
					if (found){
						if (pars_attrs.containsKey(paragraph)){
							ParsAttr temp = pars_attrs.get(paragraph);
							filenames =temp.filenames; 
							par_startids =temp.par_startids;
							par_endids =temp.par_endids;
							if (!filenames.contains(file.getName())){
								filenames.add(file.getName());
								par_startids.add((double)s+1);
								par_endids.add((double)(nodeLstP.getLength()-s-1));
								pars_attrs.put(paragraph, temp);
							}
						}else{
							filenames = new ArrayList<String>();;
							par_startids = new ArrayList<Double>();
							par_endids = new ArrayList<Double>();
							filenames.add(file.getName());
							par_startids.add((double)s+1);
							par_endids.add((double)(nodeLstP.getLength()-s-1));
							ParsAttr temp = new ParsAttr(filenames, par_startids, par_endids);
							pars_attrs.put(paragraph, temp);
						}
					}
				}
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pars_attrs;
	}


	public static Set<String> mergeSets(Set<String> ilspboilerpars,	Set<String> temp) {

		return ilspboilerpars;
	}

}
