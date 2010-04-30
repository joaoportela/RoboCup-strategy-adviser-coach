/*
 * $Header: $
 */

package soccerscope.model;

import java.awt.Color;
import java.util.Hashtable;
import java.util.prefs.Preferences;

import soccerscope.util.Color2;

public class ColorDB {
	private static Hashtable<String, Color> colordb;

	private ColorDB() {
	}

	static {
		colordb = new Hashtable<String, Color>();
		// ball color
		loadColor("ball", "snow");
		colordb.put("ball_vel", Color.red);

		// player color
		loadColor("team_l_color", "gold");
		loadColor("goalie_l_color", "green");
		loadColor("team_r_color", "red");
		loadColor("goalie_r_color", "purple");
		colordb.put("nuetral_player", Color.white);
		colordb.put("nuetral_goalie", Color.white);

		// field color
		colordb.put("field", Color2.indianRed);
		colordb.put("pitch", Color2.forestGreen);
		colordb.put("line", Color2.mix(Color2.snow, Color2.forestGreen, 1, 1));
		colordb.put("goal", Color.black);
	}

	private static void loadColor(String name, String colorName) {
		Preferences pf = SoccerScopePreferences.getPreferences();
		String tlc = pf.get(name, colorName);
		Color c = Color2.getColor(tlc);
		try {
			if (c == null)
				c = new Color(Integer.parseInt(tlc, 16));
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
			c = Color2.getColor(colorName);
		}
		putColor(name, c);
	}

	public static boolean contains(String name) {
		return colordb.contains(name);
	}

	public static Color getColor(String name) {
		return colordb.get(name);
	}

	public static void putColor(String name, Color color) {
		colordb.put(name, color);
		Preferences pf = SoccerScopePreferences.getPreferences();
		String colorName = Color2.getColorName(color);
		if (colorName == null)
			colorName = Integer.toHexString(color.getRGB() & 0x00FFFFFF);
		pf.put(name, colorName);
	}
}
