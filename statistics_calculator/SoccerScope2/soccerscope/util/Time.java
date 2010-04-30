/*
 * $Header: $
 */

package soccerscope.util;

public class Time {
	public final static int HALF_TIME = 3000;
	public final static int GAME_TIME = 6000;
	public final static int MAX_TIME = 65535;

	private Time() {
	};

	public static boolean isBeforeKickOff(int time) {
		return time % HALF_TIME == 0;
	}

	public static boolean isFirstHalf(int time) {
		if (time <= HALF_TIME)
			return true;
		else
			return false;
	}

	public static boolean isSecondHalf(int time) {
		if (time > HALF_TIME && time <= HALF_TIME)
			return true;
		else
			return false;
	}

	public static boolean isExtended(int time) {
		if (time > GAME_TIME)
			return true;
		else
			return false;
	}

	public static boolean isSideChanged(int time) {
		if ((time / HALF_TIME) % 2 == 1)
			return true;
		else
			return false;
	}

	public static boolean isValid(int time) {
		if (time >= 0 && time <= MAX_TIME)
			return true;
		else {
			System.err.println("invalid time: " + time);
			return false;
		}
	}
}
