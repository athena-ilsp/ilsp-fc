package gr.ilsp.fmc.utils;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class LineTypeGuesserTest {
	
	@Before
	public void setUp() throws Exception {

	}

	@Test
	public void testIsDig() {
		assertTrue(LineTypeGuesser.isDig("1"));
		assertTrue(LineTypeGuesser.isDig("10,1%"));		
		assertTrue(LineTypeGuesser.isDig("10.1%"));
		assertTrue(LineTypeGuesser.isDig("10.1‰"));
		assertTrue(LineTypeGuesser.isDig("10.1‱"));
		assertTrue(LineTypeGuesser.isDig("200.000"));		
		assertTrue(LineTypeGuesser.isDig("200,000€"));
		assertTrue(LineTypeGuesser.isDig("12.1°"));
		assertTrue(LineTypeGuesser.isDig("10:10"));		
		assertTrue(LineTypeGuesser.isDig("10'"));	
		assertTrue(LineTypeGuesser.isDig("77’"));
	}
	
	@Test
	public void testIsDigitsOnlyLine() {
		assertTrue(LineTypeGuesser.isDigitsOnlyLine("200.000 2 10,1% 10.1% 10.1‰ 10.1‱ 200.000 200,000€ 12.1° 10:10 10'77’   "));		
	}

}
