package gr.ilsp.fc.bitext;

import gr.ilsp.fc.utils.PrettyPrintHandler;
import gr.ilsp.fc.utils.XSLTransformer;

import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
//import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
//import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.codehaus.stax2.XMLOutputFactory2;
import org.codehaus.stax2.XMLStreamWriter2;

public class WriteBitexts {
	private static final Logger LOGGER = Logger.getLogger(Bitexts.class);
	private static final String cesDocVersion = "1.0";
	private static String cesNameSpace = "http://www.w3.org/1999/xlink";
	private static String cesNameSpace1 = "http://www.xces.org/schema/2003";
	private static String cesNameSpace2 = "http://www.w3.org/2001/XMLSchema-instance";
	//private static final String xmlDirName = "xml";
	private static final String appXMLext = ".xml";
	private static final String appXMLHTMLext = ".xml.html";
	private static XSLTransformer xslTransformer = null;
	private static String cesAlignURL="http://nlp.ilsp.gr/xslt/ilsp-fc/cesAlign.xsl";	
	private static String XMLNS= "<html xmlns=\"http://www.w3.org/1999/xhtml\">";
	private static final String UNDERSCORE_STR = "_";

	/**
	 * Generates the cesAlign files and their transformed files based on the detected pairs (bitexts) 
	 * @param outdir in this directory the cesAlign files will be stored
	 * @param bitexts holds the detected pairs (file1, file2, lang1, lang2, pairing method)
	 * @param cesAlign
	 * @param oxslt transformed cesAlign should be generated
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	public static void writeXMLs(String resultsDir,ArrayList<String[]> bitexts,	boolean oxslt)  {

		if (oxslt) {
			try {
				xslTransformer = new XSLTransformer(cesAlignURL);
			} catch (TransformerConfigurationException e1) {
				e1.printStackTrace();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		File cesAlignFile = null;
		File cesAlignHtmlFile = null;
		for (int ii=0;ii<bitexts.size();ii++){
			String f1=bitexts.get(ii)[0];
			String f2=bitexts.get(ii)[1];
			String l1=bitexts.get(ii)[2];
			String l2=bitexts.get(ii)[3];
			String confid=bitexts.get(ii)[4];

			cesAlignFile = new File(FilenameUtils.concat(resultsDir, 
					f1+UNDERSCORE_STR+f2+UNDERSCORE_STR+confid.substring(0, 1)+appXMLext));

			writeCesAlign(cesAlignFile, 
					new File(FilenameUtils.concat(resultsDir, f1+appXMLext)), 
					new File(FilenameUtils.concat(resultsDir, f2+appXMLext)), 
					l1,l2,confid);

			if (xslTransformer!=null) {
				cesAlignHtmlFile = new File(FilenameUtils.removeExtension(cesAlignFile.getAbsolutePath()) + appXMLHTMLext);
				LOGGER.debug(cesAlignFile+" -> "+cesAlignHtmlFile);			        
				try {
					xslTransformer.transform(cesAlignFile, cesAlignHtmlFile);
					//String cesAlignText = ReadResources.readFileAsString(cesAlignHtmlFile.getAbsolutePath());
					String cesAlignText = FileUtils.readFileToString(cesAlignHtmlFile);
					cesAlignText = cesAlignText.replace(FilenameUtils.concat(resultsDir, f1).replace("\\", "/")+appXMLext, f1+appXMLHTMLext);
					cesAlignText = cesAlignText.replace(FilenameUtils.concat(resultsDir, f2).replace("\\", "/")+appXMLext, f2+appXMLHTMLext);
					OutputStreamWriter tmpwrt;
					tmpwrt = new OutputStreamWriter(new FileOutputStream(cesAlignHtmlFile.getAbsolutePath()),"UTF-8");
					tmpwrt.write(cesAlignText);
					tmpwrt.close();
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TransformerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		LOGGER.info("CesAlign Files generated.");
	}

	/*	private static void writeCesAlign_OR(File cesAlignFile, File f1, File f2,
			String l1, String l2, String confid) {

		XMLOutputFactory2 xof = (XMLOutputFactory2) XMLOutputFactory2.newInstance();
		OutputStreamWriter wrt;
		try {
			wrt = new OutputStreamWriter(new FileOutputStream(cesAlignFile),"UTF-8");
			XMLStreamWriter2 xtw = (XMLStreamWriter2) xof.createXMLStreamWriter(wrt);
			xtw.writeStartDocument();
			xtw.writeStartElement("cesAlign");
			xtw.writeAttribute("version", "1.0");
			xtw.writeAttribute("xmlns:xlink", cesNameSpace );
			xtw.writeAttribute("xmlns", cesNameSpace1 );
			xtw.writeAttribute("xmlns:xsi", cesNameSpace2 );
			xtw.writeCharacters("\n");
			createHeader(xtw, f1, f2, l1, l2,confid);
			xtw.writeCharacters("\n");xtw.writeEndDocument();
			xtw.flush();
			xtw.close();
			wrt.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/


	/**
	 * Generates a CesAlign file by using file1 in lang1 AND file2 in lang2, confid (refers to confidence of pairing method) is not used by now
	 * @param cesAlignFile
	 * @param f1
	 * @param f2
	 * @param l1
	 * @param l2
	 * @param confid
	 * @param cesAlign
	 */
	private static void writeCesAlign(File cesAlignFile, File f1, File f2,
			String l1, String l2, String confid) {

		XMLOutputFactory2 xof = (XMLOutputFactory2) XMLOutputFactory2.newInstance();
		XMLStreamWriter2 xtw1 = null;
		XMLStreamWriter2 xtw = null;
		OutputStreamWriter wrt = null;
		try {
			wrt = new OutputStreamWriter(new FileOutputStream(cesAlignFile.getAbsolutePath()),"UTF-8");
			xtw1 = (XMLStreamWriter2)
					xof.createXMLStreamWriter(wrt);
			PrettyPrintHandler handler = new PrettyPrintHandler(xtw1 );
			xtw = (XMLStreamWriter2)
					Proxy.newProxyInstance(XMLStreamWriter2.class.getClassLoader(),
							new Class[] { XMLStreamWriter2.class }, handler);

			xtw.writeStartDocument();
			xtw.writeStartElement("cesAlign");
			xtw.writeAttribute("version", "1.0");
			xtw.writeAttribute("xmlns:xlink", cesNameSpace );
			xtw.writeAttribute("xmlns", cesNameSpace1 );
			xtw.writeAttribute("xmlns:xsi", cesNameSpace2 );
			createHeader(xtw, f1, f2, l1, l2,confid);
			xtw.writeEndElement();
			xtw.writeEndDocument();
			xtw.flush();
			xtw.close();
			xtw1.close();
			wrt.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Generates the header (this is the main part) of a CesAlign file. Confidence (confid) is not used by now 
	 * @param xtw
	 * @param f1
	 * @param f2
	 * @param l1
	 * @param l2
	 * @param confid
	 * @throws XMLStreamException
	 */
	private static void createHeader(XMLStreamWriter2 xtw, File f1, 
			File f2,String l1, String l2, String confid) throws XMLStreamException {

		xtw.writeStartElement("cesHeader");
		xtw.writeAttribute("version", cesDocVersion);
		xtw.writeStartElement("profileDesc");
		xtw.writeStartElement("translations");
		//xtw.writeAttribute("confidence",confid );
		xtw.writeStartElement("translation");
		xtw.writeAttribute("trans.loc", f1.getAbsolutePath().replace("\\","/"));
		xtw.writeAttribute("xml:lang", l1);
		xtw.writeAttribute("wsd", "UTF-8");
		xtw.writeAttribute("n", "1");
		xtw.writeEndElement();
		xtw.writeStartElement("translation");
		xtw.writeAttribute("trans.loc", f2.getAbsolutePath().replace("\\","/"));
		xtw.writeAttribute("xml:lang", l2);
		xtw.writeAttribute("wsd", "UTF-8");
		xtw.writeAttribute("n", "2");
		xtw.writeEndElement();
		xtw.writeEndElement();
		xtw.writeEndElement();
	}


	/**
	 * Creates file(s) called (outputFile, outputFileHTML) with list(s) of cesDocs or transformed CesDocs
	 * bitexts holds file1, file2, lang1, lang2 and pairing method. 
	 * @param outputDirName
	 * @param outputFile
	 * @param outputFileHTML
	 * @param bitexts
	 */
	public static void writeOutList(File outputDirName, File outputFile, 
			File outputFileHTML, ArrayList<String[]> bitexts ) {
		String filename, fullfilename;
		LOGGER.debug("CesAlignListFile:"+"\t"+outputFile.getAbsolutePath());
		List<String> cesAlignFiles = new ArrayList<String>();
		for (int ii=bitexts.size()-1;ii>-1;ii--){
			filename=bitexts.get(ii)[0]+UNDERSCORE_STR+bitexts.get(ii)[1]+UNDERSCORE_STR+bitexts.get(ii)[4].substring(0, 1)+appXMLext;
			cesAlignFiles.add(FilenameUtils.concat(outputDirName.getAbsolutePath(),filename).replace("\\","/"));
		}
		try {
			FileUtils.writeLines(outputFile, cesAlignFiles);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			System.err.println("Problem in writing file containing the list of paths of cesAlign files");
			e1.printStackTrace();
		}
		
		if (outputFileHTML!=null){
			LOGGER.debug("Rendered CesAlignListFile:"+"\t"+outputFileHTML.getAbsolutePath());
			List<String> cesAlignHTMLFiles = new ArrayList<String>();
			cesAlignHTMLFiles.add(XMLNS);
			for (int ii=bitexts.size()-1;ii>-1;ii--){
				filename= bitexts.get(ii)[0]+UNDERSCORE_STR+bitexts.get(ii)[1]+UNDERSCORE_STR+bitexts.get(ii)[4].substring(0, 1)+appXMLHTMLext;
				fullfilename = FilenameUtils.concat(outputDirName.getAbsolutePath(), filename).replace("\\","/");
				cesAlignHTMLFiles.add("<br />"+"<a href=\""+fullfilename+"\">\n"+filename+"</a>");
			}
			cesAlignHTMLFiles.add("</html>");
			try {
				FileUtils.writeLines(outputFileHTML, cesAlignHTMLFiles);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				System.err.println("Problem in writing file containing the list of links pointing to rendered cesAlign files");
				e1.printStackTrace();
			}
		}
	}
	
	/*try {
	Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile),"UTF-8"));
	LOGGER.info("items in document pair list:\t"+bitexts.size());
	for (int ii=bitexts.size()-1;ii>-1;ii--){
		LOGGER.info(ii);
		filename=bitexts.get(ii)[0]+UNDERSCORE_STR+bitexts.get(ii)[1]+UNDERSCORE_STR+bitexts.get(ii)[4].substring(0, 1)+appXMLext;
		LOGGER.info("CesAlignFileName:\t"+filename);
		LOGGER.info("CesAlign in ListFile:"+"\t"+
				FilenameUtils.concat(outputDirName.getAbsolutePath(),filename).replace("\\","/"));
		out.write(FilenameUtils.concat(outputDirName.getAbsolutePath(),filename).replace("\\","/")+"\n");
	}
	out.close();
} catch (IOException e){
	System.err.println("Problem in writing the output file i.e. the list of paths of cesAlign files.");
	e.printStackTrace();
}*/
	
	/*try {
		Writer out1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFileHTML),"UTF-8"));
		if (bitexts.size()>0){
			out1.write(XMLNS);
			for (int ii=bitexts.size()-1;ii>-1;ii--){
				filename= bitexts.get(ii)[0]+UNDERSCORE_STR+bitexts.get(ii)[1]+UNDERSCORE_STR+bitexts.get(ii)[4].substring(0, 1)+appXMLHTMLext;
				fullfilename = FilenameUtils.concat(outputDirName.getAbsolutePath(), filename).replace("\\","/");
				out1.write("<br />"+"<a href=\""+fullfilename+"\">\n"+filename+"</a>"+"\n");
			}
			out1.write("</html>");
		}
		out1.close();
	} catch (IOException e){
		System.err.println("Problem in writing the output file i.e. the list of urls pointing to cesAlign files.");
		e.printStackTrace();
	}	*/
}
