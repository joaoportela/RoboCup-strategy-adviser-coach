/*
 * $Header: $
 */

package soccerscope.model;

public class GameEvent implements Comparable<GameEvent> {
	public final static int MIN = 1;
	public final static int GOAL = 1;
	public final static int GOAL_KICK = 2;
	public final static int GOALIE_CATCH = 3;
	public final static int OFFSIDE = 4;
	public final static int BACKPASS = 5;
	public final static int FREE_KICK_FAULT = 6;
	public final static int CORNER_KICK = 7;
	public final static int FREE_KICK = 8;
	public final static int KICK_IN = 9;
	public final static int SHOOT = 10;
	public final static int PASS = 11;
	public final static int MAX = 12;
	private final static String name[] = { " ", "Goal", "Goal_Kick",
			"Goalie_Catch", "Offside", "Back_Pass", "Free_Kick_Fault",
			"Corner_Kick", "Free_Kick", "Kick_In", "Shoot", "Pass" };

	public int time;
	public int type;

	public GameEvent(int time, int type) {
		this.time = time;
		this.type = type;
	}

	public boolean equals(Object o) {
		return (time == ((GameEvent) o).time) && (type == ((GameEvent) o).type);
	}

	public int compareTo(GameEvent obj) {
		return time - obj.time;
	}

	public String getEventName() {
		return name[type];
	}

	public static String getEventName(int type) {
		return name[type];
	}

	public String toString() {
		return String.valueOf(time) + ": " + name[type];
	}
}
