package gr.ilsp.fmc.main;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ReadResources {
	
    public static ArrayList<String> readFileLineByLine(String filename) throws IOException {
    	ArrayList<String> param=new ArrayList<String>();
        //URL svURL = ReadResources.class.getClassLoader().getResource(filename);
        //BufferedReader in = new BufferedReader(new InputStreamReader(svURL.openStream()));
        //BufferedReader in = new BufferedReader(new InputStreamReader(filename));
        String inputLine;
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
        
        while ((inputLine = in.readLine()) != null) {
             //System.out.println(inputLine);
             param.add(inputLine);
        }
        in.close();
        return param;
    }
        
    public static String readFileAsString(String filePath) throws java.io.IOException{
		byte[] buffer = new byte[(int) new File(filePath).length()];
		BufferedInputStream f = new BufferedInputStream(new FileInputStream(filePath));
		f.read(buffer);
		f.close();
		return new String(buffer);
	}
        
    public static void writetextfile(String filename,String text) {
		Writer out;
		try {
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename),"UTF-8"));
			out.write(text.trim());
			out.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			System.err.println("Error in writing the output text file. The encoding is not supported.");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.println("Error in writing the output text file. The file does not exist.");
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error in writing the output text file.");
		}
	}
 
    public static String extractLangfromXML(String inputString, String ele_name, String attr_name) {
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
				if (NameElement.hasAttribute(attr_name)){
					result+=NameElement.getAttributeNode(attr_name).getTextContent();
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
    
    public static String extractTextfromXML_clean
    (String inputString, String ele_name, String attr_name, boolean included) {
    	//System.out.println(inputString);
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
    
    
    public static String extractAttrfromXML
    (String inputString, String ele_name, String attr_name, boolean included) {
    	//System.out.println(inputString);
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
				if (included){
					if (NameElement.hasAttribute(attr_name)){
						result+=NameElement.getAttribute(attr_name);
						result+="\n";
					}
				}else{
					//if (!NameElement.hasAttribute(attr_name)){
					//	result+=NameElement.getTextContent();
					//	result+="\n";
					//}
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
    
    
    public static void copy(String fromFileName, String toFileName)
			throws IOException {
		File fromFile = new File(fromFileName);
		File toFile = new File(toFileName);

		if (!fromFile.exists())
			throw new IOException("FileCopy: " + "no such source file: "
					+ fromFileName);
		if (!fromFile.isFile())
			throw new IOException("FileCopy: " + "can't copy directory: "
					+ fromFileName);
		if (!fromFile.canRead())
			throw new IOException("FileCopy: " + "source file is unreadable: "
					+ fromFileName);

		if (toFile.isDirectory())
			toFile = new File(toFile, fromFile.getName());

		if (toFile.exists()) {
			if (!toFile.canWrite())
				throw new IOException("FileCopy: "
						+ "destination file is unwriteable: " + toFileName);
			//System.out.print("Overwrite existing file " + toFile.getName()
			//		+ "? (Y/N): ");
			//System.out.flush();
			//BufferedReader in = new BufferedReader(new InputStreamReader(
			//		System.in));
			//String response = in.readLine();
			String response ="Y";
			if (!response.equals("Y") && !response.equals("y"))
				throw new IOException("FileCopy: "
						+ "existing file was not overwritten.");
		} else {
			String parent = toFile.getParent();
			if (parent == null)
				parent = System.getProperty("user.dir");
			File dir = new File(parent);
			if (!dir.exists())
				throw new IOException("FileCopy: "
						+ "destination directory doesn't exist: " + parent);
			if (dir.isFile())
				throw new IOException("FileCopy: "
						+ "destination is not a directory: " + parent);
			if (!dir.canWrite())
				throw new IOException("FileCopy: "
						+ "destination directory is unwriteable: " + parent);
		}
		FileUtils.copyFile(fromFile,toFile);
	}
    
}
