package gr.ilsp.fc.utils;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HtmlCompressorTest {

	
	private static final Logger logger = LoggerFactory.getLogger(HtmlCompressorTest.class);
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@Test
	// Placeholder to illustrate use
	public void testCompress() {
		logger.info("HtmlCompression of a web page...");
		
		HtmlCompressor compressor = new HtmlCompressor();
		compressor.setGenerateStatistics(true);
		String html = null;
		String urlstr = "http://nlp.ilsp.gr/xslt/ilspds-fc/1210.xml.html";
		try {
			html = Jsoup.connect(urlstr).get().html();
		} catch (IOException e) {
			logger.warn("Could not download html page " + urlstr);
			// Assert.fail(e.getMessage());	
			return;
		}
		String compressed = compressor.compress(html);
		logger.debug(compressed);

		logger.info(String.format(
        "Compression time: %,d ms, Original size: %,d Kbytes, Compressed size: %,d Kbytes", 
        compressor.getStatistics().getTime(), 
        compressor.getStatistics().getOriginalMetrics().getFilesize()/1024,  
        compressor.getStatistics().getCompressedMetrics().getFilesize()/1024 ));
		
	}

}
