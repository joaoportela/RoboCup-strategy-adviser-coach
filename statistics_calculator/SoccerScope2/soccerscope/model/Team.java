/*
 * $Header: $
 */

package soccerscope.model;

public class Team {
	public final static int LEFT_SIDE = 1;
	public final static int NEUTRAL = 0;
	public final static int RIGHT_SIDE = -1;

	public String name;
	public int score;
	public float offsideline;
	public boolean offside;

	public Team() {
		name = "";
		score = 0;
		offsideline = 0.0f;
		offside = false;
	}

	public Team(Team team) {
		this.name = new String(team.name);
		this.score = team.score;
		this.offsideline = team.offsideline;
		this.offside = team.offside;
	}

	public Team(String name, int score, int side) {
		this.name = new String(name);
		this.score = score;
		offsideline = 0.0f;
		offside = false;
	}

	public Team(int side) {
		score = 0;
		offsideline = 0.0f;
		offside = false;

		if (side == LEFT_SIDE) {
			name = "left";
		} else if (side == NEUTRAL) {
			name = "";
		} else if (side == RIGHT_SIDE) {
			name = "right";
		}
	}

	public static String name(int side) {
		if (side == Team.LEFT_SIDE)
			return "LEFT_SIDE";
		if (side == Team.RIGHT_SIDE) {
			return "RIGHT_SIDE";
		}
		return "NEUTRAL";
	}
	
	public static int[] firstAndLastPlayerIndexes(int team) {
		if (team == Team.LEFT_SIDE) {
			return new int[] { 0, Param.MAX_PLAYER };
		}
		if (team == Team.RIGHT_SIDE) {
			return new int[] { Param.MAX_PLAYER, Param.MAX_PLAYER * 2 };
		}

		return new int[] { -1, -1 };
	}
	
	public static int opposingTeam(int side) {
		if (side == Team.LEFT_SIDE) {
			return Team.RIGHT_SIDE;
		} else if (side == Team.RIGHT_SIDE) {
			return Team.LEFT_SIDE;
		}
		return Team.NEUTRAL;
	}
}
