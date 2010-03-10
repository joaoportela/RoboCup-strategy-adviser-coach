/*
 * $Header: $
 */

package soccerscope.model;

import java.util.prefs.Preferences;

public class SoccerScopePreferences {

	public static Preferences getPreferences() {
		return Preferences.userRoot().node("/model/soccerscope/uec");
	}

	private SoccerScopePreferences() {
	}
}
