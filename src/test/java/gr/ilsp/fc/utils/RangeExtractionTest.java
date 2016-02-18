package gr.ilsp.fc.utils;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RangeExtractionTest {

	
	private static final Logger logger = LoggerFactory.getLogger(RangeExtractionTest.class);
	
	@Test
	public void testExtractRange() {
	    int[] ints = {-6, -3, -2, -1, 0, 1, 3, 4, 5, 7, 8, 9, 10, 11, 14, 15, 17, 18, 19, 20};
	    List<Integer> intList = new ArrayList<Integer>();
	    for (int index = 0; index < ints.length; index++) {
	        intList.add(ints[index]);
	    }
	    
		String resultStr = "-6,-3-1,3-5,7-11,14,15,17-20";
		
		logger.debug("Expected: " + resultStr);
		logger.debug("Got     : " + RangeExtraction.extractRange(intList));

		assertTrue(resultStr.equals(RangeExtraction.extractRange(intList)));
	}

}
