package gr.ilsp.fc.utils;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class FCStringUtilstest {

	
	@Before
	public void setUp() throws Exception {

	}

	@Test
	public void test() {
		String str="ab a, c. ? 0,8/7";
		assertTrue(FCStringUtils.getGraphemeLength(str)==str.length());
		System.out.println(FCStringUtils.getWordLength(str));
		assertTrue(FCStringUtils.getWordLength(str)==3);
		List<String> parts = FCStringUtils.getParts(str, "");
		assertTrue(FCStringUtils.getPartLength(str, "")==5);
	}
	
}
