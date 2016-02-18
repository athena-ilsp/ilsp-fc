package gr.ilsp.fc.exporter;

import java.io.File;
import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.apache.commons.io.FilenameUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XSLTransformerTest {
	private final static String APPNAME = XSLTransformerTest.class.getSimpleName();
	static Logger logger = LoggerFactory.getLogger(APPNAME);
	XSLTransformer xslTransformer ;

	@Test
	public void testTransform() {
		logger.info("Testing the TMX to HTML conversion...");
		try {
			xslTransformer = new XSLTransformer("http://nlp.ilsp.gr/xslt/ilsp-fc/mergedtmx2html.xsl");
			//File tmxfile = new File("src/test/resources/exporter/eng-1_ita-2_u.tmx");
			File tmxfile = new File("src/test/resources/exporter/merged.tmx");
			File htmlTmxFile =   new File(FilenameUtils.removeExtension(tmxfile.getAbsolutePath()) + ".html");
			xslTransformer.setBaseDir(tmxfile.getParent());
			xslTransformer.transform(tmxfile, htmlTmxFile);
			logger.info("Done testing the TMX to HTML conversion.");
		} catch (IOException |TransformerException e) {
			logger.error("Could not transform this tmx. Skipping.");
			e.printStackTrace();
		}
	}
}
