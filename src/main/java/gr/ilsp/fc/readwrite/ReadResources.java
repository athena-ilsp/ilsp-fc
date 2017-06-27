package gr.ilsp.fc.readwrite;

import gr.ilsp.fc.utils.ContentNormalizer;
import gr.ilsp.fc.utils.FCStringUtils;
import gr.ilsp.nlp.commons.Constants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ReadResources {
	private static final Logger LOGGER = Logger.getLogger(ReadResources.class);
	protected static Matcher skipLineM = Pattern.compile("^(\\s*)||(#.*)$").matcher("");
	private static final String P_ELE = "p";
	private static final String L_ELE = "language";
	private static final String L_ATTR = "iso639";
	private static final String appXMLext = ".xml";
	private static final String ooi_crawlinfo = "crawlinfo";
	private static final String UNDERSCORE = "_";
	private static final String XML_EXTENSION = ".xml";
	private static final String LANG_KEYS_RESOURCE = "langKeys.txt" ;
	
	public static String extractNodefromXML(String infile, String ele_name) {
		String result="";
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
			Document doc = db.parse(infile);
			doc.getDocumentElement().normalize();
			NodeList nodeLstP = doc.getElementsByTagName(ele_name);
			for(int s=0; s<nodeLstP.getLength() ; s++){
				Element NameElement = (Element)nodeLstP.item(s);
				result+=NameElement.getTextContent();
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	
	public static List<String> extractListFromNodesOfXML(String infile, String ele_name,boolean hasattr) {
		List<String> result=new ArrayList<String>();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
			Document doc = db.parse(infile);
			doc.getDocumentElement().normalize();
			NodeList nodeLstP = doc.getElementsByTagName(ele_name);
			for(int s=0; s<nodeLstP.getLength() ; s++){
				Element NameElement = (Element)nodeLstP.item(s);
				if (hasattr==NameElement.hasAttributes()){
					String temp =NameElement.getTextContent(); 
					if (temp.contains(Constants.NEWLINE))
						temp = temp.replaceAll(Constants.NEWLINE, Constants.SPACE);
					result.add(ContentNormalizer.normalizeText(temp));	
				}
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	
	
	public static String extractNodefromXML(String infile, String ele_name,boolean hasattr) {
		String result="";
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
			Document doc = db.parse(infile);
			doc.getDocumentElement().normalize();
			NodeList nodeLstP = doc.getElementsByTagName(ele_name);
			for(int s=0; s<nodeLstP.getLength() ; s++){
				Element NameElement = (Element)nodeLstP.item(s);
				if (hasattr==NameElement.hasAttributes())
					result+=NameElement.getTextContent();	
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}


	public static String extractTextfromXML_clean(String inputFile, String ele_name, String attr_name, boolean included) {
		//System.out.println(inputString);
		String result="";
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
			Document doc = db.parse(inputFile);
			doc.getDocumentElement().normalize();
			NodeList nodeLstP = doc.getElementsByTagName(ele_name);
			for(int s=0; s<nodeLstP.getLength() ; s++){
				Element NameElement = (Element)nodeLstP.item(s);
				if (included){
					if (NameElement.hasAttribute(attr_name)){
						result+=NameElement.getTextContent();
						result+="\n";
					}
				}else{
					if (!NameElement.hasAttribute(attr_name)){
						result+=NameElement.getTextContent();
						result+="\n";
					}
				}
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			LOGGER.warn("problem in reading : "+ inputFile);
			e.printStackTrace();
			return "";
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

/**
 * Extracts the value of an attribute of an element in a file  
 * @param inputFile
 * @param ele_name
 * @param attr_name
 * @param included
 * @param onlyFirstOccur
 * @return
 */
	public static String extractAttrfromXML(String inputFile, String ele_name, String attr_name, boolean included, boolean onlyFirstOccur) {
		String result="";
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
			Document doc = db.parse(inputFile);
			doc.getDocumentElement().normalize();
			NodeList nodeLstP = doc.getElementsByTagName(ele_name);
			for(int s=0; s<nodeLstP.getLength() ; s++){
				Element NameElement = (Element)nodeLstP.item(s);
				if (included){
					if (NameElement.hasAttribute(attr_name)){
						result+=NameElement.getAttribute(attr_name);
						if (onlyFirstOccur){
							return result;
						}
						result+="\n";
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
		return result.trim();
	}


	public static List<String> readFileLineByLine(URL genreFile) {
		List<String> param=new ArrayList<String>();
		String inputLine;
		BufferedReader in;
		try {
			in = new BufferedReader(new InputStreamReader(genreFile.openStream()));
			while ((inputLine = in.readLine()) != null) {
				param.add(inputLine);
			}
			in.close();
		} catch (IOException e) {

			e.printStackTrace();
		}
		return param;
	}

	public static String extractNumsfromXML(String inputfile, String ele_name, String attr_name, String ooiType, boolean included ) {
		String numstring="";
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
			Document doc = db.parse(inputfile);
			doc.getDocumentElement().normalize();
			NodeList nodeLstP = doc.getElementsByTagName(ele_name);
			for(int s=0; s<nodeLstP.getLength() ; s++){
				Element NameElement = (Element)nodeLstP.item(s);
				if (included){
					if (NameElement.hasAttribute(attr_name)){
						numstring+=NameElement.getTextContent().replaceAll("\\D+","");
					}
				}else{
					if (!NameElement.hasAttribute(attr_name)){
						numstring+=NameElement.getTextContent().replaceAll("\\D+","");
					}else{
						if (!NameElement.getAttributeNode(attr_name).getTextContent().equals(ooiType)){
							//System.out.println(NameElement.getTextContent());
							numstring+=NameElement.getTextContent().replaceAll("\\D+","");
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
		return numstring;
	}

	/**
	 * count tokens in paragraphs (with no crawlinfo attribute) of the cesDoc Files in the targetDir 
	 * @param outputDirName
	 * @return
	 */
	public static int countToksinDir(File targetDir) {
		int total_tokens=0;

		FilenameFilter filter = new FilenameFilter() {			
			public boolean accept(File arg0, String arg1) {
				return (arg1.substring(arg1.length()-4).equals(appXMLext));
			}
		};
		File[] files=targetDir.listFiles(filter);
		String text="", lang;
		int length_in_tok=0;
		for (int ii=0;ii<files.length;ii++){
			text = ReadResources.extractTextfromXML_clean(files[ii].getAbsolutePath(),P_ELE,ooi_crawlinfo, false);
			lang = ReadResources.extractAttrfromXML(files[ii].getAbsolutePath(), L_ELE, L_ATTR,true,false);
			length_in_tok = FCStringUtils.countTokens(text,lang);
			total_tokens=total_tokens+length_in_tok;
		}
		return total_tokens;
	}

	
	public static String extractSymbolsfromXML(String inputfile, String ele_name,	String attr_name, String ooiType, boolean included) {
		String symstring="";
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
			Document doc = db.parse(inputfile);
			doc.getDocumentElement().normalize();
			NodeList nodeLstP = doc.getElementsByTagName(ele_name);
			String str="";
			for(int s=0; s<nodeLstP.getLength() ; s++){
				Element NameElement = (Element)nodeLstP.item(s);
				if (included){
					if (NameElement.hasAttribute(attr_name)){
						str = NameElement.getTextContent();
						str=str.replaceAll("[^\\P{L}]","");
						str=str.replaceAll("[^\\P{N}]","");
						str=str.replaceAll(Constants.SPACE,"");
						str=str.replaceAll("\\.","");
						str=str.replaceAll("„","");
						str=str.replaceAll("“","");
						str=str.replaceAll("”","");
						str=str.replaceAll("'","");
						str=str.replaceAll("‘","");
						str.replaceAll("\"","");
						str=str.replaceAll(",","");
						symstring+=str;
					}
				}else{
					if (!NameElement.hasAttribute(attr_name)){
						str = NameElement.getTextContent();
						str=str.replaceAll("[^\\P{L}]","");
						str=str.replaceAll("[^\\P{N}]","");
						str=str.replaceAll(Constants.SPACE,"");
						str=str.replaceAll("\\.","");
						str=str.replaceAll("„","");
						str=str.replaceAll("“","");
						str=str.replaceAll("”","");
						str=str.replaceAll("'","");
						str=str.replaceAll("‘","");
						str.replaceAll("\"","");
						str=str.replaceAll(",","");
						symstring+=str;
					}else{
						if (!NameElement.getAttributeNode(attr_name).getTextContent().equals(ooiType)){
							str = NameElement.getTextContent();
							str=str.replaceAll("[^\\P{L}]","");
							str=str.replaceAll("[^\\P{N}]","");
							str=str.replaceAll(Constants.SPACE,"");
							str=str.replaceAll("\\.","");
							str=str.replaceAll("„","");
							str=str.replaceAll("“","");
							str=str.replaceAll("”","");
							str=str.replaceAll("'","");
							str=str.replaceAll("‘","");
							str.replaceAll("\"","");
							str=str.replaceAll(",",""); 
							symstring+=str;
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
		return symstring;
	}
	
	/**
	 * Extracts value nodeName from cesDocs involved in tmxfiles
	 * @param tmxfiles
	 * @param nodeName
	 * @return
	 */
	public static List<String> extactValueFromDocPair(List<File> tmxfiles, String nodeName) {
		List<String> domain=new ArrayList<String>();
		for (File tmxFile:tmxfiles){
			File f = new File(FilenameUtils.concat(tmxFile.getParent(), StringUtils.split(tmxFile.getName(), UNDERSCORE)[0])+XML_EXTENSION);
			String domain1 = ReadResources.extractNodefromXML(f.getAbsolutePath(), nodeName, false);
			if (domain1.isEmpty())
				domain1 = ReadResources.extractNodefromXML(f.getAbsolutePath(), nodeName, true);
			if (!domain1.isEmpty()){
				if (!domain.contains(domain1))
					domain.add(domain1);
			}
			f = new File(FilenameUtils.concat(tmxFile.getParent(), StringUtils.split(tmxFile.getName(), UNDERSCORE)[1])+XML_EXTENSION);
			domain1 = ReadResources.extractNodefromXML(f.getAbsolutePath(), nodeName, false);
			if (!domain1.isEmpty())
				domain1 = ReadResources.extractNodefromXML(f.getAbsolutePath(), nodeName, true);
			if (!domain1.isEmpty()){
				if (!domain.contains(domain1))
					domain.add(domain1);
			}
		}
		return domain;
	}

	/**
	 * Extracts value nodeName from cesDocs
	 * @param tmxfiles
	 * @param nodeName
	 * @return
	 */
	public static List<String> extactValueFromCesDoc(List<File> cesDocFiles, String nodeName) {
		List<String> domain=new ArrayList<String>();
		for (File cesDocFile:cesDocFiles){
			String domain1 = ReadResources.extractNodefromXML(cesDocFile.getAbsolutePath(), nodeName, false);
			if (!domain1.isEmpty()){
				if (!domain.contains(domain1))
					domain.add(domain1);
			}
			domain1 = ReadResources.extractNodefromXML(cesDocFile.getAbsolutePath(), nodeName, false);
			if (!domain1.isEmpty()){
				if (!domain.contains(domain1))
					domain.add(domain1);
			}
		}
		return domain;
	}
	
	public static String checkBOM(String line){
		//checks BOM
		byte[] bts;
		try {
			bts = line.getBytes("UTF-8");
			if (bts[0] == (byte) 0xEF && bts[1] == (byte) 0xBB && bts[2]==(byte) 0xBF) {
				byte[] bts2 = new byte[bts.length-3];
				for (int i = 3; i<bts.length;i++)
					bts2[i-3]=bts[i];
				line = new String(bts2);
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return line;
	}
	
	public static String getSupportedLanguages() {
		String supportedlangs = "";
		try {
			URL svURL = ReadResources.class.getClassLoader().getResource(LANG_KEYS_RESOURCE);
			BufferedReader in = new BufferedReader(new InputStreamReader(svURL.openStream()));
			String str;
			while ((str = in.readLine()) != null) {
				supportedlangs=supportedlangs+Constants.SEMICOLON+str.subSequence(0, str.indexOf(">")).toString();
			}
			in.close();
		} catch (IOException e) {
			LOGGER.error("Problem in reading the file for langKeys.");
		}
		return supportedlangs.substring(1);
	}
	
	
}
