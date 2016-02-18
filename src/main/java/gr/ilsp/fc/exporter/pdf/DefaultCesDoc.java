package gr.ilsp.fc.exporter.pdf;

import java.io.IOException;
import java.io.StringReader;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

public class DefaultCesDoc {

    private static final String CESDOC = "<cesDoc version=\"0.4\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns=\"http://www.xces.org/schema/2003\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
    		+ "<cesHeader version=\"0.4\">"
    		
    		+ "<fileDesc>"
    		
    		+ "<titleStmt>"
    		+ "<title></title>"
    		+ "<respStmt><resp><type>Crawling and normalization</type><name>ILSP</name></resp></respStmt>"
    		+ "</titleStmt>"
    		
    		+ "<publicationStmt>"
    		+ "<distributor></distributor>"
    		+ "<eAddress type=\"web\">project_website</eAddress>"
    		+ "<availability>Under review</availability>"
    		+ "<pubDate></pubDate>"
    		+ "</publicationStmt>"
    		
    		+ "<sourceDesc>"
    		+ "<biblStruct>"
    		+ "<monogr>"
    		+ "<title></title>"
    		+ "<author></author>"
    		+ "<imprint>"
    		+ "<format></format>"
    		+ "<publisher></publisher>"
    		+ "<pubDate></pubDate><eAddress></eAddress>"
    		+ "</imprint>"
    		+ "</monogr></biblStruct></sourceDesc>"
    		+ "</fileDesc>"
    		
    		+ "<profileDesc>"
    		+ "<langUsage><language iso639=\"\"/></langUsage>"
    		+ "<textClass>"
    		+ "<keywords/>"
    		+ "<domain confidence=''>"
    		+ "</domain><subdomain></subdomain>"
    		+ "<genre></genre><subject/>"
    		+ "</textClass>"
    		+ "<annotations>"
    		+ "<annotation>"
    		+ "</annotation>"
    		+ "</annotations>"
    		+ "</profileDesc>"
    		
    		+ "</cesHeader>"
    		+ "<text>"
    		+ "<body>"
    		+ "</body>"
    		+ "</text>"
    		+ "</cesDoc>";

	public static Document getDefaultCesDoc () throws JDOMException, IOException {
		 Document document = new SAXBuilder().build(new InputSource(new StringReader(CESDOC)));
		 return document;
	}
	
	
}
