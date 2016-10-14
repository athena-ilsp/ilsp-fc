package gr.ilsp.fc.utils;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidateUtilsTest {

	private final static String APPNAME = ValidateUtilsTest.class.getSimpleName();
	static Logger logger = LoggerFactory.getLogger(APPNAME);

		
	@Test
	public void testEmailvalidator() throws Exception {
		String email = "chamber@ccci.org.cy";
		assertTrue(ValidateUtils.isValidEmailAddress(email));
		email = "The terrorists seek to destabilise entire nations and regions and "
				+ "to achieve their evil goals they are ready to use any means, conventional, chemical, biological and even nuclear weapons";
		assertFalse(ValidateUtils.isValidEmailAddress(email));
	}
}
