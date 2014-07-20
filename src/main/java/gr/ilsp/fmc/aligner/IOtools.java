package gr.ilsp.fmc.aligner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.UUID;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Static Class with various methods for  
 * reading text files and parsing XML files
 * 
 * @author  Sokratis
 * 
 */
public class IOtools{
    /**
     * Reads the contents of a File
     * @param f The File to read
     * @return An ArrayList of String containing the File contents. Each line corresponds to an entry in the ArrayList 
     */
	public static ArrayList<String> readFileToArray(String fname){
        BufferedReader br=null;
        String nextLine = "";
        ArrayList<String> res=new ArrayList<String>();
        File f=new File(fname);
        try{
            br=new BufferedReader(new InputStreamReader(new FileInputStream(f)));
            while((nextLine = br.readLine())!=null){
            	//if(nextLine.trim().length()>0){
            		res.add(nextLine);
            	//}
            }
            br.close(); //CLOSE THE BufferedReader
        }catch(Exception e){
            System.out.println(e);
        }
        return res;
    }
    /**
     * Reads the contents of a File
     * @param f The File to read
     * @return An ArrayList of String containing the File contents. Each line corresponds to an entry in the ArrayList 
     */
	public static ArrayList<String> readFileToArrayForDcuService(String fname){
        BufferedReader br=null;
        String nextLine = "";
        ArrayList<String> res=new ArrayList<String>();
        File f=new File(fname);
        try{
            br=new BufferedReader(new InputStreamReader(new FileInputStream(f)));
            while((nextLine = br.readLine())!=null){
            	if(nextLine.trim().length()>0){
            		String[] breakline=nextLine.split("	");
            		res.add(breakline[0]);
            	}
            }
            br.close(); //CLOSE THE BufferedReader
        }catch(Exception e){
            System.out.println(e);
        }
        return res;
    }
    /**
     * Reads the contents of a File
     * @param f The File to read
     * @return A StringBuffer containing the File contents 
     */
    public static String readFileToString(String filename){
        BufferedReader br=null;
        String nextLine = "";
        StringBuffer sb = new StringBuffer();
        try{
            br=new BufferedReader(new InputStreamReader(new FileInputStream(new File(filename))));
            while((nextLine = br.readLine())!=null){
                sb.append(nextLine+"\n");
            }
            br.close(); //CLOSE THE BufferedReader
        }catch(Exception e){
            System.out.println(e);
        }
        return sb.toString();
    }
    /**
     * Reads a file from a URL and stores it line by line in an ArrayList
     * @param sUrl The URL of the file
     * @return ArrayList of String storing the file line by line
     */
    public static ArrayList<String> readURLToArray(String sUrl){
    	URL url=null;
    	ArrayList<String> res=new ArrayList<String>();
		try{
			url=new URL(sUrl);
		}catch(MalformedURLException e){
			return new ArrayList<String>();
		}
        BufferedReader in;
		try{
			in=new BufferedReader(new InputStreamReader(url.openStream()));
	        String inputLine;
	        while((inputLine=in.readLine())!=null)
	            res.add(inputLine);
	        in.close();
		}catch(IOException e){
			return new ArrayList<String>();
		}
		return res;
    }
    /**
     * Reads a file from a URL and stores it as a String
     * @param sUrl The URL of the file
     * @return A String with the file content
     */
    public static String readURLToString(String sUrl){
    	URL url=null;
    	StringBuilder sb=new StringBuilder();
		try{
			url=new URL(sUrl);
		}catch(MalformedURLException e){
			return "";
		}
        BufferedReader in;
		try{
			in=new BufferedReader(new InputStreamReader(url.openStream()));
	        String inputLine;
	        while((inputLine=in.readLine())!=null)
	        	sb.append(inputLine);
	        in.close();
		}catch(IOException e){
			return "";
		}
		return sb.toString();
    }
    /**
     * Stores locally a file from a URL
     * @param sUrl The URL of the remote file
     * @param fName The name of the new local file name
     * @return Boolean value with the status of the method
     */
	public static boolean writeUrlToFile(String sUrl, String fName){
    	URL url=null;
		try{
			url=new URL(sUrl);
		}catch(MalformedURLException e){
			return false;
		}
		ReadableByteChannel rbc;
		FileOutputStream fos=null;
		try{
			rbc=Channels.newChannel(url.openStream());
			fos = new FileOutputStream(fName);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		}catch (IOException e){
			return false;
		}finally{
			try {
				if(fos!=null)
					fos.close();
			}catch (IOException e){
				return false;
			}
		}
		return true;
	}
	/**
	 * Save the content of a StringBuffer to a File
	 * @param fName The name of the new file
	 * @param sb The StringBuffer to save
	 */
    public static boolean writeToFile(String fName, StringBuffer sb){
        try{
        	BufferedWriter out=new BufferedWriter(new FileWriter(fName));
			out.write(sb.toString());
	        out.flush();
	        out.close();
		}catch(IOException e){
			return false;
		}
        return true;
    }
    /**
     * Create a folder in the local filesystem
     * @param dirname The name of the new folder 
     * @return TRUE if the folder was created successfully or it existed, else FALSE
     */
    public static boolean createDir(String dirname){
    	File dir=new File(dirname);
    	if(!dir.exists())
    		return dir.mkdirs();
    	return true;
    }
    /**
     * 
     * @param urlORfile The url or the local location of the XML
     * @param sLang The source language
     * @param tLang The target language
     */
    public static String[] parseXmlFile(String urlORfile, String sLang, String tLang){
    	DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
		dbf.setValidating(false);
    	DocumentBuilder db=null;
    	Document doc=null;
    	String[] ret=new String[2];
		try{
			db=dbf.newDocumentBuilder();
			String content="";
			if(urlORfile.startsWith("http"))
				content=IOtools.readURLToString(urlORfile);
			else
				content=IOtools.readFileToString(urlORfile);
			
			doc=db.parse(new InputSource(new StringReader(content)));
		}catch(ParserConfigurationException e){
			e.printStackTrace();
		}catch (SAXException e){
			e.printStackTrace();
		}catch (IOException e){
			e.printStackTrace();
		}
		NodeList translationNodes = doc.getDocumentElement().getElementsByTagName("translation");
        for(int i=0; i<translationNodes.getLength(); i++){
            Node translationNode=translationNodes.item(i);
            Node loc=translationNode.getAttributes().getNamedItem("trans.loc");
            //loc.getTextContent();
            Node lang=translationNode.getAttributes().getNamedItem("xml:lang");
            lang.getTextContent();
            if(lang.getTextContent().compareTo(sLang)==0){
            	ret[0]=loc.getTextContent();
            }
            else if(lang.getTextContent().compareTo(tLang)==0){
            	ret[1]=loc.getTextContent();            	
            }
        }
		return ret;
    }
    /**
     * Creates a TMX file using the hunalign output
     * @param file The local location of the hunalign output
     * @param sLang The source language
     * @param tLang The target language
     * @param slSents ArrayList with all source sentences
     * @param tlSents ArrayList with all target sentences
     * @param outFile The filename to store the tmx output
     * @ret the number of alignments produced
     */
    public static int createTMXfileFromHunalign(String file, String sLang, String tLang, ArrayList<String> slSents, ArrayList<String> tlSents, String outFile){
    	String TMXHEADER="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<tmx version=\"1.1\">\n<header segtype=\"sentence\" srclang=\""+sLang+"\" datatype=\"PlainText\"></header>\n<body>\n";
    	String TMXEND="</body>\n</tmx>";
    	String sout=TMXHEADER;
    	
		ArrayList<String> content=IOtools.readFileToArray(file);
		int alignmentCount=0;
		for(String line:content){
			String[] lineArray=line.split("	");
			int slIndex=Integer.parseInt(lineArray[0]);
			int tlIndex=Integer.parseInt(lineArray[1]);
			float score=0;
			try{
				score=Float.parseFloat(lineArray[2]);
			}catch(java.lang.ArrayIndexOutOfBoundsException e){
				score=0;
			}

            String tmxEntry="";
            try{
            	alignmentCount++;
            	tmxEntry+="<tu tuid=\""+alignmentCount+"\" datatype=\"Text\" score=\""+score+"\">\n";
            	tmxEntry+="\t<tuv xml:lang=\""+sLang+"\">\n";
            	tmxEntry+="\t\t<seg>"+slSents.get(slIndex)+"</seg>\n\t</tuv>\n";
            	tmxEntry+="\t<tuv xml:lang=\""+tLang+"\">\n";
            	tmxEntry+="\t\t<seg>"+tlSents.get(tlIndex)+"</seg>\n\t</tuv>\n";
            	tmxEntry+="</tu>\n";

            	if(slSents.get(slIndex).compareTo("")==0||tlSents.get(tlIndex).compareTo("")==0){
                	tmxEntry="";
                	alignmentCount--;
            	}
            }catch(java.lang.IndexOutOfBoundsException e){
            	tmxEntry="";
            	alignmentCount--;
            }
            sout+=tmxEntry;
        }
        sout+=TMXEND;
        IOtools.writeToFile(outFile, new StringBuffer(sout));
        
        //Create html file from the tmx
        String htmlFile=outFile.replace(".tmx", ".html");
        IOtools.tmxTOhtml(outFile, htmlFile);
        return alignmentCount; 
    }
    
    
    public static void tmxTOhtml(String inFile, String outFile){
		Source xmlSource = new StreamSource(new File(inFile));
		try {
			//InputStream is = new FileInputStream("C:\\Users\\Sokratis\\workspace\\ilsp-fc\\target\\hunalign-1.1\\tmx2html.xsl");
			Source xsltSource = new StreamSource(new URL("http://nlp.ilsp.gr/xslt/ilsp-fc/tmx2html.xsl").openStream());
			TransformerFactory transFact = TransformerFactory.newInstance();
			Transformer transformer = transFact.newTransformer(xsltSource);
			transformer.transform(xmlSource, new StreamResult(new File(outFile)));
		} catch (FileNotFoundException e) {
			System.err.println("xsl file for converting tmx to html not found!");
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public static StringBuffer convertlistTMX_HTML(StringBuffer log2) {
		String[] temp = log2.toString().split("\r\n");
    	StringBuffer log = new StringBuffer();
    	
    	log.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
		for (int ii=0;ii<temp.length;ii++){
			temp[ii]=temp[ii].replace(".tmx",".html");
			log.append("<br />"+"<a href=\""+temp[ii]+"\">\n"+temp[ii]+"</a>"+"\n");
		}
		log.append("</html>");
		return log;
	}
    
    /**
     * Creates a TMX file using the DCU aligner travelling object output
     * @param urlORfile The url or the local location of the alignment output
     * @param sLang The source language
     * @param tLang The target language
     * @param slSents ArrayList with all source sentences
     * @param tlSents ArrayList with all target sentences
     * @param outFile The filename to store the tmx output
     * @ret the number of alignments produced
     */
    public static int createTMXfile(String urlORfile, String sLang, String tLang, ArrayList<String> slSents, ArrayList<String> tlSents, String outFile){
    	DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
		dbf.setValidating(false);
    	DocumentBuilder db=null;
    	Document doc=null;
    	String TMXHEADER="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<tmx version=\"1.1\">\n<header segtype=\"sentence\" srclang=\""+sLang+"\" datatype=\"PlainText\"></header>\n<body>\n";
    	String TMXEND="</body>\n</tmx>";
    	String sout=TMXHEADER;
		try{
			db=dbf.newDocumentBuilder();
			String content="";
			if(urlORfile.startsWith("http"))
				content=IOtools.readURLToString(urlORfile);
			else
				content=IOtools.readFileToString(urlORfile);
			
			doc=db.parse(new InputSource(new StringReader(content)));
		}catch(ParserConfigurationException e){
			e.printStackTrace();
		}catch (SAXException e){
			e.printStackTrace();
		}catch (IOException e){
			e.printStackTrace();
		}
		NodeList translationNodes = doc.getDocumentElement().getElementsByTagName("link");
		int alignmentCount=0;
        for(int i=0; i<translationNodes.getLength(); i++){
            Node translationNode=translationNodes.item(i);
            Node alignScore=translationNode.getAttributes().getNamedItem("type");
            NodeList alignSents = ((Element)translationNode).getElementsByTagName("align");
            String slSent=alignSents.item(0).getAttributes().getNamedItem("xlink:href").getTextContent().replace("#s", "");
            String tlSent=alignSents.item(1).getAttributes().getNamedItem("xlink:href").getTextContent().replace("#s", "");
            int slSentID=Integer.parseInt(slSent);
            int tlSentID=Integer.parseInt(tlSent);
            String tmxEntry="";
            try{
            	alignmentCount++;
            	tmxEntry+="<tu tuid=\""+alignmentCount+"\" datatype=\"Text\" score=\""+alignScore.getTextContent()+"\">\n";
            	tmxEntry+="\t<tuv xml:lang=\""+sLang+"\">\n";
            	tmxEntry+="\t\t<seg>"+slSents.get(slSentID-1)+"</seg>\n\t</tuv>\n";
            	tmxEntry+="\t<tuv xml:lang=\""+tLang+"\">\n";
            	tmxEntry+="\t\t<seg>"+tlSents.get(tlSentID-1)+"</seg>\n\t</tuv>\n";
            	tmxEntry+="</tu>\n";

            	if(slSents.get(slSentID-1).compareTo("")==0||tlSents.get(tlSentID-1).compareTo("")==0){
                	tmxEntry="";
                	alignmentCount--;
            	}
            }catch(java.lang.IndexOutOfBoundsException e){
            	tmxEntry="";
            	alignmentCount--;
            }
            sout+=tmxEntry;
        }
        sout+=TMXEND;
        IOtools.writeToFile(outFile, new StringBuffer(sout));
        
        return alignmentCount; 
    }
    /**
     * Reads an XCES document from a url and extracts the text
     * @param sUrl The URL of the XCES document
     * @return Text of the document as a String
     */
    public static String stripXcesDocument(String sUrl){
    	StringBuffer sb=new StringBuffer();
    	DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
		dbf.setValidating(false);
    	DocumentBuilder db=null;
    	Document doc=null;
		try{
			db=dbf.newDocumentBuilder();
			doc=db.parse(new InputSource(new StringReader(IOtools.readURLToString(sUrl))));
		}catch(ParserConfigurationException e){
			e.printStackTrace();
		}catch (SAXException e){
			e.printStackTrace();
		}catch (IOException e){
			e.printStackTrace();
		}
		NodeList translationNodes = doc.getDocumentElement().getElementsByTagName("p");
        for(int i=0; i<translationNodes.getLength(); i++){
            Node translationNode=translationNodes.item(i);
            Node crawlInfo=translationNode.getAttributes().getNamedItem("crawlinfo");
            //String sCinfo="";
            if(crawlInfo!=null)
            	crawlInfo.getTextContent();
            else{
            	String sText = translationNode.getTextContent();
            	sb.append(sText+System.getProperty("line.separator"));
            }
        }
    	return sb.toString();
    }
    /**
     * Reads an XCES document from a file and extracts the text
     * @param sUrl The file of the XCES document
     * @return Text of the document as a String
     */
    public static String stripXcesDocument(File file){
    	StringBuffer sb=new StringBuffer();
    	DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
		dbf.setValidating(false);
    	DocumentBuilder db=null;
    	Document doc=null;
    	SentenceSplitter sp=new SentenceSplitter();
		try{
			db=dbf.newDocumentBuilder();
			doc=db.parse(file);
		}catch(ParserConfigurationException e){
			e.printStackTrace();
		}catch (SAXException e){
			e.printStackTrace();
		}catch (IOException e){
			e.printStackTrace();
		}
		NodeList translationNodes = doc.getDocumentElement().getElementsByTagName("p");
        for(int i=0; i<translationNodes.getLength(); i++){
            Node translationNode=translationNodes.item(i);
            Node crawlInfo=translationNode.getAttributes().getNamedItem("crawlinfo");
            //String sCinfo="";
            if(crawlInfo!=null)
            	crawlInfo.getTextContent();
            else{
            	String sText = translationNode.getTextContent();
            	//Run the sentence splitter on the paragraph
            	Vector<String> sents=sp.getSentences(sText, 2);
            	sText="";
            	for(String sent:sents)
            		sText+=sent+System.getProperty("line.separator");
            	sb.append(sText+System.getProperty("line.separator"));
            }
        }
    	return sb.toString();
    }
    
    public static String readTMXTranslations(File fTMX){
    	StringBuffer sb=new StringBuffer();
    	DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
		dbf.setValidating(false);
    	DocumentBuilder db=null;
    	Document doc=null;
    	SentenceSplitter sp=new SentenceSplitter();
		try{
			db=dbf.newDocumentBuilder();
			doc=db.parse(fTMX);
		}catch(ParserConfigurationException e){
			e.printStackTrace();
		}catch (SAXException e){
			e.printStackTrace();
		}catch (IOException e){
			e.printStackTrace();
		}
		NodeList translationNodes = doc.getDocumentElement().getElementsByTagName("p");
        for(int i=0; i<translationNodes.getLength(); i++){
            Node translationNode=translationNodes.item(i);
            Node crawlInfo=translationNode.getAttributes().getNamedItem("crawlinfo");
            //String sCinfo="";
            if(crawlInfo!=null)
            	crawlInfo.getTextContent();
            else{
            	String sText = translationNode.getTextContent();
            	//Run the sentence splitter on the paragraph
            	Vector<String> sents=sp.getSentences(sText, 2);
            	sText="";
            	for(String sent:sents)
            		sText+=sent+System.getProperty("line.separator");
            	sb.append(sText+System.getProperty("line.separator"));
            }
        }
    	return sb.toString();
    }
    
    public static void fromTMXtoAppraise(String inputDir, String inputF, String slLang, String tlLang){
    	int count=1;
    	StringBuffer output=new StringBuffer();
    	String input=inputDir+inputF;
    	output.append("<set id=\""+input+"\" source-language=\""+slLang+"\" target-language=\""+tlLang+"\">\n");

    	DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
		dbf.setValidating(false);
    	DocumentBuilder db=null;
    	Document doc=null;
		try{
			db=dbf.newDocumentBuilder();
			doc=db.parse(new File(input));
		}catch(ParserConfigurationException e){
			e.printStackTrace();
		}catch (SAXException e){
			e.printStackTrace();
		}catch (IOException e){
			e.printStackTrace();
		}
    	//Get all tuv's from the tmx file
		NodeList tuvNodes = doc.getDocumentElement().getElementsByTagName("seg");
		String prevSent="";
        for(int i=0; i<tuvNodes.getLength(); i++){
            Node segment=tuvNodes.item(i);
        	String sText = segment.getTextContent().trim();
        	sText=sText.replace(" & ", " &amp; ");
        	sText=sText.replace(" < ", " &lt; ");
        	sText=sText.replace(" > ", " &gt; ");
        	//Run the sentence splitter on the paragraph
        	if(i % 2 == 0)
        		prevSent=sText;
        	else{
        		output.append("<seg id=\""+count+"\" doc-id=\"\">\n");
        		output.append("\t<source>"+prevSent+"</source>\n");
        		output.append("\t<translation system=\"default\">"+sText+"</translation>\n");
        		output.append("</seg>\n");
        		count++;
        	}
        }
        output.append("</set>");
        inputF=inputF.substring(inputF.lastIndexOf("/"), inputF.length());
        System.out.println("Writing file "+"c:/nlpos"+inputF);
        
    	IOtools.writeToFile("c:/nlpos/"+inputF+".appr", output);
    }
	/**
	 * Creates a temp file
	 * @return a temp File instance
	 * @throws IOException
	 */

	public static File createRandomTmpFile() throws IOException{
		final UUID fileName = UUID.randomUUID();
		final File temp = File.createTempFile(fileName.toString(), ".tmp");
		// Delete temp file when program exits.
		temp.deleteOnExit();
		return temp;
	}
    
    
    public static void fromTMXtoAppraiseBatch(ArrayList<String> input, String inputDir, String slLang, String tlLang){
    	for(String inputf: input){
    		IOtools.fromTMXtoAppraise(inputDir, inputf, slLang, tlLang);
    	}
    }
    /**
     * Main method
     * @param arg
     */
    public static void main(String[] arg){
    	String slang=arg[0].toLowerCase();
    	String tlang=arg[1].toLowerCase();
    	String inputd=arg[2];
    	String inputf=arg[3];

    	//Read the file list
    	ArrayList<String> files=IOtools.readFileToArrayForDcuService(inputd+inputf);

    	IOtools.fromTMXtoAppraiseBatch(files, inputd, slang, tlang);
    }
}