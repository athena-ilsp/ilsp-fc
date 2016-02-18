package gr.ilsp.fc.utils;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EurovocTest {

	private static final Logger logger = LoggerFactory.getLogger(EurovocTest.class);

	static Eurovoc eurovoc = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		logger.info("Initializing Eurovoc");
		eurovoc = Eurovoc.INSTANCE;
	}

	@Test
	public void testGetIdentifier() {
		logger.info("Testing getIdentifier()");
		assertTrue("ThesaurusConcept-317".equals(eurovoc.getIdentifier("culture")));
		assertTrue("ThesaurusConcept-317".equals(eurovoc.getIdentifier("Culture")));
		assertTrue("ThesaurusConcept-317".equals(eurovoc.getIdentifier("culturE")));
		assertTrue("ThesaurusConcept-317".equals(eurovoc.getIdentifier("CULTURE")));
		assertTrue(eurovoc.getIdentifier("CULTUREs")==null);

		assertTrue("Domain-10".equals(eurovoc.getIdentifier("EUROPEAN UNION")));
		assertTrue("Domain-10".equals(eurovoc.getIdentifier("european Union")));
		assertTrue("Domain-10".equals(eurovoc.getIdentifier("European Union")));
		assertTrue("Domain-10".equals(eurovoc.getIdentifier(" european union ")));

		assertTrue("Domain-24".equals(eurovoc.getIdentifier("Finance")));
		assertTrue("Domain-24".equals(eurovoc.getIdentifier("FINANCE")));
		assertTrue("Domain-24".equals(eurovoc.getIdentifier(" FINANCE ")));

		assertTrue("Domain-20".equals(eurovoc.getIdentifier("trade")));
		assertTrue("MicroThesaurus-2021".equals(eurovoc.getIdentifier("interNatioNAL trade")));
	
	}

	@Test
	public void testGetConceptLabel() {
		logger.info("Testing getConceptDesc() from eurovoc");
		assertTrue("culture".equals(eurovoc.getConceptLabel("ThesaurusConcept-317")));
		assertTrue(eurovoc.getConceptLabel("317")==null);

		assertTrue("FINANCE".equals(eurovoc.getConceptLabel("Domain-24")));


	}

	
	@Test
	public void testGetCanonicalLabel() {
		logger.info("Testing getCanonicalLabel from eurovoc");
		assertTrue("culture".equals(eurovoc.getCanonicalLabel("CultUre")));
		assertTrue(eurovoc.getCanonicalLabel("CULTUREs")==null);
	}

	
}
