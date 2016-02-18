package gr.ilsp.fc.utils;


import java.awt.Color;

import org.apache.log4j.Logger;


public class ColorUtils {
	private static final Logger LOGGER = Logger.getLogger(ColorUtils.class);
	static double factor1 = 1;
	static double factor2 = 0.5;
	static double factor3 = 0.25;

	public static void main(String[] args){
		Color lc = new Color(100,100,100);
		Color hc = new Color(150,255,200);
		double lv = 5000;
		double hv = 10000;
		double value = 7500;
		Color c = getFactoredColor(lc, hc, lv, hv, value, factor1, factor2, factor3);
		System.out.println(c.getRed());
		System.out.println(c.getGreen());
		System.out.println(c.getBlue());
	}

	/**
	 * Maps the value at a color ranging between lc (corresponding to lv) and hc(corresponding to hv) according to the factors 
	 * @param lc	: RGB color corresponding to lv
	 * @param hc	: RGB color corresponding to hv
	 * @param lv	: the lowest value in user's collection
	 * @param hv	: the highest value in user's collection
	 * @param value	: the value to be mapped at a color
	 * @param factor1	: controls the step of changing Red component   
	 * @param factor2	: controls the step of changing Green component 
	 * @param factor3	: controls the step of changing Blue component 
	 * @return
	 */

	public static Color getFactoredColor( Color lc, Color hc, double lv, double hv, double value, double factor1, double factor2, double factor3) {
		if (factor1>1 || factor1<0 || factor2>1 || factor2<0 || factor3>1 || factor3<0 ){
			LOGGER.error("factors should be between 0 and 1");
			return null;
		}
		double range = hv-lv+1;
		value = value-lv+1;
		if (value>=range)
			return hc;
		if (value<=0)
			return lc;

		double range1 = hc.getRed()-lc.getRed()+1;
		if (range1<0)	{	range1=0;	}
		double range2 = hc.getGreen()-lc.getGreen()+1;
		if (range2<0)	{	range2=0;	}
		double range3 = hc.getBlue()-lc.getBlue()+1;
		if (range3<0)	{	range3=0;	}
		double factor = value/range;
		int nv1 = (int)( factor* range1*factor1 + lc.getRed());
		int nv2 = (int)( factor* range2*factor2 + lc.getGreen());
		int nv3 = (int)( factor* range3*factor3 + lc.getBlue());

		return new Color(nv1, nv2,nv3);
	}
	
	public static String toHexString(Color colour) throws NullPointerException {
		String hexColour = Integer.toHexString(colour.getRGB() & 0xffffff);
		if (hexColour.length() < 6) {
			hexColour = "000000".substring(0, 6 - hexColour.length())
					+ hexColour;
		}
		return "#" + hexColour;
	}

	public static Color getContrastColor(Color color) {
	    int d = 0;
	    // Counting the perceptive luminance - human eye favors green color... 
	    double a = 1 - ( 0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue())/255;
	    if (a < 0.5) {
	       d = 0; // bright colors - black font
	    } else {
	       d = 255; // dark colors - white font
	    }
	    return new Color(d, d, d);
	}	
}
