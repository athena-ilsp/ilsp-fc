package gr.ilsp.fc.parser;

import static org.junit.Assert.fail;
import gr.ilsp.fc.parser.ExtendedLinksExtractor;
import gr.ilsp.fc.parser.ExtendedOutlink;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.TeeContentHandler;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
public class ExtendedLinksExtractorTest {

	private static final Logger logger = LoggerFactory.getLogger(ExtendedLinksExtractorTest.class);

	private Metadata metadata;
	private ParseContext context;  
	private Parser parser;  
	private ContentHandler contentHandler;
	
	@Before
	public void setup() {
		context = new ParseContext();  
		contentHandler = new TeeContentHandler();        
		parser  = new AutoDetectParser();
		context.set(Parser.class, parser);  
	}

	public void testGetLinks(String urlString, String linkToBeExtracted, String testName) {
		URL url=null;
		try {
			url = new URL(urlString);
			InputStream input = new BufferedInputStream(url.openConnection().getInputStream());
			metadata = new Metadata();
			parser.parse(input, contentHandler , metadata, context);
			metadata.add(Metadata.CONTENT_LOCATION, url.toExternalForm()); 
			// Hardcoded to avoid calling bixo fetchedDatum.getHeaders().getFirst(HttpHeaderNames.CONTENT_LOCATION);
			input.mark(0);
			ExtendedOutlink[] rankedLinks =  ExtendedLinksExtractor.getLinks(input, metadata, null,null);
			input.close();
			boolean found = false;
			for (int i = 0; i < rankedLinks.length; i++) {
				if (rankedLinks[i].getToUrl().equals(linkToBeExtracted)) {
					logger.info("Found link: " + rankedLinks[i].toString());
					found = true;
					break;
				}
			}
			if (!found) {
				fail ("Did not find link: " + linkToBeExtracted);
			}
		} catch (Exception e) {
			//e.printStackTrace();
			logger.warn("Cannot open/process URL " + urlString);
			fail ("Did not fetch seed: " + urlString);
		}
	}

	//@Test
	public void testGetAltLinksWithHreflang() {
		testGetLinks(  
				"http://www.citizensinformation.ie/en/housing/renting_a_home/tenants_rights_and_obligations.html",
				"http://www.citizensinformation.ie/ga/housing/renting_a_home/tenants_rights_and_obligations.html",
				"Test that links contain links with hreflang attributes");
		testGetLinks(  
				"http://www.fold.lv/en/2015/07/the-entomology-and-wanderlust-of-one-wolf/",
				"http://www.fold.lv/2015/07/one-wolf-entomologija-un-celotaja-gars/", 
				"Test that links contain links with hreflang attributes");
	}
	
	// @Test //Uncomment to test. Comment to speed up building
	public void testGetLinks() {
		/* testGetLinks(
				"http://www.hermitage.nl",
				"http://www.hermitage.nl/en/pers/",
				"Test that link can be extracted from URL");*/
		/* testGetLinks(
				"http://www.fehergolyamuzeum.hu/mainpage_en",
				"http://www.fehergolyamuzeum.hu/about_the_stork/the_white_stork/the_white_stork_detailed",
				"Test that link can be extracted from URL");*/
		/* testGetLinks(
				"http://www.fehergolyamuzeum.hu/mainpage_hu",
				"http://www.fehergolyamuzeum.hu/turizmus/programajanlatok/programajanlatok_reszletes",
				"Test that link can be extracted from URL");*/
		/*testGetLinks(
				"https://www.tirol.gv.at/gesundheit-vorsorge/krankenanstalten/",
				"https://www.tirol.gv.at/gesundheit-vorsorge/krankenanstalten/finanzierung/",
				"Test that link can be extracted from URL");*/
		/*testGetLinks(
				"http://www.bauhaus-dessau.de/de/index.html",
				"http://www.bauhaus-dessau.de/de/kalender/liste.html",
				"Test that link can be extracted from URL");*/
		/*testGetLinks(  
				"http://www.ilsp.gr/",
				"http://www.ilsp.gr/el/profile/68-welcome/15-welcome",
				"Test that link can be extracted from URL"); */
		/*testGetLinks(  
				"http://www.svb.nl/int/nl/index.jsp",
				"http://www.svb.nl/gaia/mijnsvb/pages/entree.jsf",
				"Test that link can be extracted from URL");*/
		/*testGetLinks(  
				"http://www.svb.nl/int/nl/kinderbijslag/veranderingen_in_gezin/uw_kind_gaat_uit_huis/index.jsp",
				"http://www.svb.nl/int/nl/kinderbijslag/veranderingen_in_gezin/bijdrage_aan_het_onderhoud/index.jsp",
				"Test that link can be extracted from URL");*/
		/* ----testGetLinks(
				"https://www.zamek-buchlovice.cz/en/about/history",
				"https://www.zamek-buchlovice.cz/cs/informace-pro-navstevniky",
				"Test that link can be extracted from URL");*/
		/* ----testGetLinks(
				"http://www.sziklakorhaz.eu/en", 
				"http://www.sziklakorhaz.eu/en/news",
				"Test that link can be extracted from URL");*/
		/* testGetLinks(
				"http://www.svb.nl/int/nl/index.jsp",
				"http://www.svb.nl/gaia/mijnsvb/pages/entree.jsf",
				"Test that link can be extracted from URL");
		 testGetLinks(  
				"http://www.svb.nl/int/nl/index.jsp",
				"http://www.svb.nl/gaia/mijnsvb/pages/entree.jsf",
				"Test that link can be extracted from URL");
		testGetLinks(  
				"http://www.svb.nl/int/nl/kinderbijslag/veranderingen_in_gezin/uw_kind_gaat_uit_huis/index.jsp",
				"http://www.svb.nl/int/nl/kinderbijslag/veranderingen_in_gezin/bijdrage_aan_het_onderhoud/index.jsp",
				"Test that link can be extracted from URL");*/
	}

}


//URL baseUrl = new URL(_metadata.get(Metadata.CONTENT_LOCATION));
//URLConnection urlc=baseUrl.openConnection(); 
//InputStream inputstream=urlc.getInputStream();
//byte[] buffer = new byte[1024];
//int numRead;
//long numWritten = 0;
//while ((numRead = inputstream.read(buffer)) != -1) {
//	out.write(buffer, 0, numRead);
//	numWritten += numRead;
//}
//inputstream.close();
//out.close();
//LOGGER.info(filename + " saved.");
////System.out.println(filename);
//content = Pdf2text.run1(new File(filename), _sort_type);
////System.out.println(content);
//if (content==null){
//	LOGGER.info("PDF to Text Conversion failed.");
//}else{
//	content = ContentNormalizer.normalizeText(content);
//}
//
//String lang = "";
//
//_metadata.set(Metadata.CONTENT_ENCODING, "UTF-8");
//_metadata.set(Metadata.COMMENT,filename);
//ExtendedOutlink[] outlinks = ExtendedLinksExtractor.getLinks(_input,_metadata);
