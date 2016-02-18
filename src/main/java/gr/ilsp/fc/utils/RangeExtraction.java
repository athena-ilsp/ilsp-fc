package gr.ilsp.fc.utils;

import java.util.List;

/**
 * Based on code from http://rosettacode.org/wiki/Range_extraction#Java
 */
public class RangeExtraction {

	private static final String DASH = "-";
	private static final String COMMA = ",";
	private static final String EMPTY_STRING = "";
	private static final String COMMA_AT_THE_END = ",$";

	/**
	 * * A format for expressing an ordered list of integers is to use a comma
	 * separated list of either individual integers Or a range of integers
	 * denoted by the starting integer separated from the end integer in the
	 * range by a dash, '-'. (The range includes all integers in the interval
	 * including both endpoints)
	 * 
	 * The range syntax is to be used only for, and for every range that expands
	 * to more than two values.
	 * 
	 * Example The list of integers:
	 * 
	 * -6, -3, -2, -1, 0, 1, 3, 4, 5, 7, 8, 9, 10, 11, 14, 15, 17, 18, 19, 20
	 * 
	 * Is accurately expressed by the range expression:
	 * 
	 * -6,-3-1,3-5,7-11,14,15,17-20
	 * 
	 * @param ints
	 * @return
	 */
	public static String extractRange(List<Integer> ints) {
		StringBuffer rangeString = new StringBuffer();
		int len = ints.size();
		int idx1 = 0;
		int idx2 = 0;
		while (idx1 < len) {
			while (++idx2 < len && ints.get(idx2) - ints.get(idx2 - 1) == 1)
				;

			if (idx2 - idx1 > 2) {
				// System.out.printf("%s-%s,", arr[idx], arr[idx2 - 1]);
				rangeString.append(ints.get(idx1) + DASH + ints.get(idx2 - 1) + COMMA);
				idx1 = idx2;
			} else {
				for (; idx1 < idx2; idx1++)
					rangeString.append(ints.get(idx1) + COMMA);
			}
		}

		return rangeString.toString().replaceAll(COMMA_AT_THE_END, EMPTY_STRING);
	}
}