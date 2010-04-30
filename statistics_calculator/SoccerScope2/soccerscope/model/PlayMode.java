/*
 * $Header: $
 */

package soccerscope.model;

public class PlayMode {
	public final static int PM_Null = 0;
	public final static int PM_BeforeKickOff = 1;
	public final static int PM_TimeOver = 2;
	public final static int PM_PlayOn = 3;
	public final static int PM_KickOff_Left = 4;
	public final static int PM_KickOff_Right = 5;
	public final static int PM_KickIn_Left = 6;
	public final static int PM_KickIn_Right = 7;
	public final static int PM_FreeKick_Left = 8;
	public final static int PM_FreeKick_Right = 9;
	public final static int PM_CornerKick_Left = 10;
	public final static int PM_CornerKick_Right = 11;
	public final static int PM_GoalKick_Left = 12;
	public final static int PM_GoalKick_Right = 13;
	public final static int PM_AfterGoal_Left = 14;
	public final static int PM_AfterGoal_Right = 15;
	public final static int PM_Drop_Ball = 16;
	public final static int PM_OffSide_Left = 17;
	public final static int PM_OffSide_Right = 18;
	public final static int PM_PK_Left = 19;
	public final static int PM_PK_Right = 20;
	public final static int PM_FirstHalfOver = 21;
	public final static int PM_Pause = 22;
	public final static int PM_Human = 23;
	public final static int PM_Foul_Charge_Left = 24;
	public final static int PM_Foul_Charge_Right = 25;
	public final static int PM_Foul_Push_Left = 26;
	public final static int PM_Foul_Push_Right = 27;
	public final static int PM_Foul_MultipleAttacker_Left = 28;
	public final static int PM_Foul_MultipleAttacker_Right = 29;
	public final static int PM_Foul_BallOut_Left = 30;
	public final static int PM_Foul_BallOut_Right = 31;
	public final static int PM_Back_Pass_Left = 32;
	public final static int PM_Back_Pass_Right = 33;
	public final static int PM_Free_Kick_Fault_Left = 34;
	public final static int PM_Free_Kick_Fault_Right = 35;
	public final static int PM_MAX = 36;

	public int pmode;

	public PlayMode() {
		pmode = PM_BeforeKickOff;
	}

	public PlayMode(PlayMode playmode) {
		this.pmode = playmode.pmode;
	}

	public PlayMode(int pmode) {
		this.pmode = pmode;
	}

	public String toString() {
		return toString(pmode);
	}

	public static String toString(int pmode) {
		String pmodeString = "";

		switch (pmode) {
		case PM_Null:
			pmodeString = "NULL";
			break;
		case PM_BeforeKickOff:
			pmodeString = "Before Kick Off";
			break;
		case PM_TimeOver:
			pmodeString = "Time Over";
			break;
		case PM_PlayOn:
			pmodeString = "Play On";
			break;
		case PM_KickOff_Left:
			pmodeString = "Kick Off Left";
			break;
		case PM_KickOff_Right:
			pmodeString = "Kick Off Right";
			break;
		case PM_KickIn_Left:
			pmodeString = "Kick In Left";
			break;
		case PM_KickIn_Right:
			pmodeString = "Kick In Right";
			break;
		case PM_FreeKick_Left:
			pmodeString = "FreeKick Left";
			break;
		case PM_FreeKick_Right:
			pmodeString = "FreeKick Right";
			break;
		case PM_CornerKick_Left:
			pmodeString = "CornerKick Left";
			break;
		case PM_CornerKick_Right:
			pmodeString = "CornerKick Right";
			break;
		case PM_GoalKick_Left:
			pmodeString = "GoalKick Left";
			break;
		case PM_GoalKick_Right:
			pmodeString = "GoalKick Right";
			break;
		case PM_AfterGoal_Left:
		case PM_AfterGoal_Right:
			pmodeString = "GOAL";
			break;
		case PM_Drop_Ball:
			pmodeString = "drop ball";
			break;
		case PM_OffSide_Left:
		case PM_OffSide_Right:
			pmodeString = "OffSide";
			break;
		case PM_PK_Left:
			pmodeString = "PK Left";
			break;
		case PM_PK_Right:
			pmodeString = "PK Right";
			break;
		case PM_FirstHalfOver:
			pmodeString = "First Half Over";
			break;
		case PM_Pause:
			pmodeString = "pause";
			break;
		case PM_Human:
			pmodeString = "human judge";
			break;
		case PM_Foul_Charge_Left:
			pmodeString = "foul change left";
			break;
		case PM_Foul_Charge_Right:
			pmodeString = "foul change right";
			break;
		case PM_Foul_Push_Left:
			pmodeString = "foul push left";
			break;
		case PM_Foul_Push_Right:
			pmodeString = "foul push right";
			break;
		case PM_Foul_MultipleAttacker_Left:
			pmodeString = "foul multiple attack left";
			break;
		case PM_Foul_MultipleAttacker_Right:
			pmodeString = "foul multiple attack right";
			break;
		case PM_Foul_BallOut_Left:
			pmodeString = "foul ballout left";
			break;
		case PM_Foul_BallOut_Right:
			pmodeString = "foul ballout right";
			break;
		case PM_Back_Pass_Left:
			pmodeString = "Back Pass Left";
			break;
		case PM_Back_Pass_Right:
			pmodeString = "Back Pass Right";
			break;
		case PM_Free_Kick_Fault_Left:
			pmodeString = "FreeKick Fault Left";
			break;
		case PM_Free_Kick_Fault_Right:
			pmodeString = "FreeKick Fault Right";
			break;
		case PM_MAX:
			pmodeString = "";
			break;
		}

		return pmodeString;
	}
}
