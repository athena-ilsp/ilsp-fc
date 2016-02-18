package gr.ilsp.fc.aligner.factory;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlignerTest {

	private static final Logger logger = LoggerFactory.getLogger(AlignerTest.class);
	private Aligner aligner;
	private AlignerFactory alignerFactory ;
	private String alignerStr = "maligna";
	private String l1 = "ell";
	private String l2 = "eng";
	private Properties properties = new Properties();

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		logger.info("Initializing " + alignerStr + "aligner " );
		alignerFactory = new AlignerFactory();
		aligner = new MalignaAligner();
		aligner = alignerFactory.getAligner(alignerStr);
		properties.setProperty("sourceLang", l1);			
		properties.setProperty("targetLang", l2);
		aligner.initialize(properties);
	}

	@Test
	public void testProcess() {
		try {
			File ellFile = File.createTempFile("ell-", ".xml"); 
			File engFile = File.createTempFile("eng-", ".xml");
			File tmxFile = File.createTempFile("ell-eng-", ".tmx");
			ellFile.deleteOnExit();
			engFile.deleteOnExit();
			tmxFile.deleteOnExit();
			FileUtils.copyURLToFile(this.getClass().getResource("/aligner/gv-ell-20110426-987.xml"), ellFile);
			FileUtils.copyURLToFile(this.getClass().getResource("/aligner/gv-eng-20110325-211008.xml"), engFile);
			logger.info("Testing " + alignerStr + " aligner " + ellFile + "<->" + engFile);
			AlignmentStats alignmentStats = aligner.process(ellFile, engFile, tmxFile);
			Float zeroToOne = alignmentStats.getZeroToOneAlignmentsSize()/(float)alignmentStats.getAlignmentsSize();
			if (zeroToOne > 0.1) {
				fail("0-1/total " + zeroToOne + " > 0.1");
			} else {
				logger.info("0-1/total alignments is " + zeroToOne );		
			}
			
		} catch (Exception e) {
			fail("Could not align files /aligner/gv-ell-20110426-987.xml and /aligner/gv-eng-20110325-211008.xml");
		}
	
	}



}
