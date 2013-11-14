package gr.ilsp.fmc.exporter.pdf;

import java.io.IOException;
import java.io.StringReader;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

public class DefaultCesAlign {

    private static final String CESALIGN = 
    		
    		"<cesAlign version='1.0' "
    		+ "xmlns:xlink='http://www.w3.org/1999/xlink' xmlns='http://www.xces.org/schema/2003' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>"
    		+ "<cesHeader version='1.0'>"
    		+ "<profileDesc>"
    		+ "<translations>"
    		+ "<translation trans.loc='' xml:lang='' wsd='UTF-8' n=''/>"
    		+ "<translation trans.loc='' xml:lang='' wsd='UTF-8' n=''/>"    		
    		+ "</translations>"
    		+ "</profileDesc>"
    		+ "</cesHeader>"
    		+ "</cesAlign>";


	public static Document getDefaultCesAlign () throws JDOMException, IOException {
		 Document document = new SAXBuilder().build(new InputSource(new StringReader(CESALIGN)));
		 return document;
	}
	
	
}
