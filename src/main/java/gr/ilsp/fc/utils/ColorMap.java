package gr.ilsp.fc.utils;

import java.awt.Color;

/**
 * ColorMap maps numbers in the range 0 to 1 onto a continuous spectrum of two
 * or more colors. The specified colors are spaced evenly over the range, so for
 * instance if 5 colors are provided, they will correspond to values 0, 0.25,
 * 0.5, 0.75, and, 1. Intervening values are mapped onto interpolated colors.
 *
 * <P>
 * <small>Software developed for the BaBar Detector at the SLAC B-Factory. <br>
 * Copyright &copy; 1998 California Institute of Technology.</small>
 *
 * @version $Id: ColorMap.java,v 1.1 1998/05/21 01:38:19 samuel Exp $
 *
 * @author Alex Samuel (Apr 98; originator)
 */

public class ColorMap extends Object {
	// initializers & constructors

	public ColorMap(String name, Color[] colors) throws RuntimeException {
		setColors(colors);
		_name = name;
	}

	// accessors

	public Color[] getColors() {
		return _colors;
	}

	public String getName() {
		return _name;
	}

	public void setColors(Color[] colors) throws RuntimeException {
		if (colors.length < 2)
			throw new RuntimeException(
					"ColorMap must be initialized with at least two colors");

		_colors = colors;
	}

	public void setName(String name) {
		_name = name;
	}

	// members

	/**
	 * Returns the color corresponding to the specified value. The argument is
	 * assumed to fall between 0 and 1 for interpolation. If <code>val</code> is
	 * less than 0 or greater than 1, the start or end colors will be returned,
	 * respectively.
	 *
	 * @param val
	 *            the value to interpolate
	 * @return the corresponding color
	 */
	public Color lookupColor(float val) {
		int length = _colors.length - 1;

		if (val < 0.f)
			return _colors[0];
		if (val >= 1.f)
			return _colors[length];

		int pos = (int) (val * length); // position in colormap
		Color s = _colors[pos];
		Color e = _colors[pos + 1];
		float rem = val * length - pos; // remainder; interpolate between
		return new Color(s.getRed() + (int) (rem * (e.getRed() - s.getRed())),
				s.getGreen() + (int) (rem * (e.getGreen() - s.getGreen())),
				s.getBlue() + (int) (rem * (e.getBlue() - s.getBlue())));
	}

	// data members

	protected Color[] _colors;
	protected String _name;

	// static stuff

	// predefined color maps
	public final static ColorMap[] STANDARD_MAPS = {
			new ColorMap("Rainbow", new Color[] { Color.blue, Color.green,
					Color.yellow, Color.orange, Color.red }),
			new ColorMap("Cool", new Color[] { Color.green, Color.blue,
					new Color(255, 0, 255) }),
			new ColorMap("Warm", new Color[] { Color.red, Color.orange,
					Color.yellow }),
			new ColorMap("Thermal", new Color[] { Color.black, Color.red,
					Color.orange, Color.yellow, Color.green, Color.blue,
					new Color(255, 0, 255), Color.white }) };

	public String toHexString(Color colour)
			throws NullPointerException {
		String hexColour = Integer.toHexString(colour.getRGB() & 0xffffff);
		if (hexColour.length() < 6) {
			hexColour = "000000".substring(0, 6 - hexColour.length())
					+ hexColour;
		}
		return "#" + hexColour;
	}

}
