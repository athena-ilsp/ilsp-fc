package gr.ilsp.fc.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ReadResources {
	protected static Matcher skipLineM = Pattern.compile("^(\\s*)||(#.*)$").matcher("");
	private static final String P_ELE = "p";
	private static final String appXMLext = ".xml";
	private static final String ooi_crawlinfo = "crawlinfo";
	private static final String UNDERSCORE = "_";
	private static final String XML_EXTENSION = ".xml";
	
	public static String extractNodefromXML(String inputString, String ele_name) {
		String result="";
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
			Document doc = db.parse(inputString);
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

	public static String extractNodefromXML(String inputString, String ele_name,boolean hasattr) {
		String result="";
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
			Document doc = db.parse(inputString);
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
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}


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
		return result;
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
		String text="";

		for (int ii=0;ii<files.length;ii++){
			text = ReadResources.extractTextfromXML_clean(files[ii].getAbsolutePath(),P_ELE,ooi_crawlinfo, false);
			StringTokenizer tkzr = new StringTokenizer(text);
			int length_in_tok=tkzr.countTokens();
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
						str=str.replaceAll(" ","");
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
						str=str.replaceAll(" ","");
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
							str=str.replaceAll(" ","");
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
			if (!domain1.isEmpty()){
				if (!domain.contains(domain1))
					domain.add(domain1);
			}
			f = new File(FilenameUtils.concat(tmxFile.getParent(), StringUtils.split(tmxFile.getName(), UNDERSCORE)[1])+XML_EXTENSION);
			domain1 = ReadResources.extractNodefromXML(f.getAbsolutePath(), nodeName, false);
			if (!domain1.isEmpty()){
				if (!domain.contains(domain1))
					domain.add(domain1);
			}
		}
		return domain;
	}

	
	
	
}
