package gr.ilsp.fmc.aligner.factory;

import gr.ilsp.fmc.aligner.SentenceSplitter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author prokopis
 *
 */
public abstract class Aligner {

	protected String sourceLang ;
	protected String targetLang ;
	private static final String BOILERPLATE = "boilerplate";
	private static final String CRAWLINFO = "crawlinfo";
	protected static final String PARA_NAME = "p";
	protected boolean useBoilerplateParagraphs = false;
	protected SentenceSplitter sentenceSplitter = new SentenceSplitter();

	public abstract void process(File sourceFile, File targetFile, File tmxFile) throws IOException, Exception;
    public abstract void destroy ();
    
    public void initialize (String sourceLang, String targetLang) {
    	setSourceLang(sourceLang);
    	setTargetLang(targetLang);
    }

	/**
	 * @param file
	 * @param useBoilerplateParagraphs
	 * @return
	 * @throws Exception
	 */
	protected List<String> getParagraphs(File file, boolean useBoilerplateParagraphs) throws Exception {
		List<String> paragraphs = new ArrayList<String>();
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(file);
			NodeList pNodes = doc.getDocumentElement().getElementsByTagName("p");
	        for(int i=0; i< pNodes.getLength(); i++){
	            Element pElement = (Element)pNodes.item(i);;
	            if (pElement.hasAttribute(CRAWLINFO) && pElement.getAttribute(CRAWLINFO).equals(BOILERPLATE) ) {
	            	continue;
	            } else {
	            	paragraphs.add(pElement.getTextContent().trim());
	            }
	        }
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return paragraphs;
	}
	/**
	 * @return the sourceLang
	 */
	public String getSourceLang() {
		return sourceLang;
	}
	/**
	 * @param sourceLang the sourceLang to set
	 */
	public void setSourceLang(String sourceLang) {
		this.sourceLang = sourceLang;
	}
	/**
	 * @return the targetLang
	 */
	public String getTargetLang() {
		return targetLang;
	}
	/**
	 * @param targetLang the targetLang to set
	 */
	public void setTargetLang(String targetLang) {
		this.targetLang = targetLang;
	}

	/**
	 * @return the useBoilerplateParagraphs
	 */
	public boolean isUseBoilerplateParagraphs() {
		return useBoilerplateParagraphs;
	}
	/**
	 * @param useBoilerplateParagraphs the useBoilerplateParagraphs to set
	 */
	public void setUseBoilerplateParagraphs(boolean useBoilerplateParagraphs) {
		this.useBoilerplateParagraphs = useBoilerplateParagraphs;
	}
	
	
	
}
