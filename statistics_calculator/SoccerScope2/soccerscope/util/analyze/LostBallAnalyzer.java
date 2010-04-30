/*
 * $Log: LostBallAnalyzer.java,v $
 * Revision 1.2  2003/01/04 08:35:36  koji
 * �ѥ���Ϣ�����ѥ��ߥ����ɥ�֥롢���줾�����ײ��Ϥ��ɲ�
 *
 * Revision 1.1  2002/12/02 08:48:01  koji
 * �ܡ���򼺤ä�������ѥ��μ������ʿ��X��ɸ�ͤ��ɲ�
 */

package soccerscope.util.analyze;

import soccerscope.model.GameEvent;
import soccerscope.model.Param;
import soccerscope.model.PlayMode;
import soccerscope.model.Scene;

public class LostBallAnalyzer extends SceneAnalyzer {

	public static String NAME = "Lost Ball";

	private Kicker leftKicker;
	private Kicker rightKicker;

	public void init() {
		super.init();
		leftKicker = null;
		rightKicker = null;
	}

	public String getName() {
		return NAME;
	}

	public class Kicker {
		public int time;
		public int unum;

		public Kicker() {
			time = 0;
			unum = 0;
		}

		public Kicker(int time, int unum) {
			this.time = time;
			this.unum = unum;
		}

		public void set(Kicker k) {
			time = k.time;
			unum = k.unum;
		}

		public String toString() {
			return "kicker ::" + time + ": " + unum;
		}
	}

	public GameEvent analyze(Scene scene, Scene prev) {

		if (prev == null)
			return null;

		Kicker left = null;
		Kicker right = null;

		// �ץ쥤��ʳ���, ��
		if (isPlayModeChanged(scene, prev, PlayMode.PM_FreeKick_Left)
				|| isPlayModeChanged(scene, prev, PlayMode.PM_KickIn_Left)
				|| isPlayModeChanged(scene, prev, PlayMode.PM_GoalKick_Left)
				|| isPlayModeChanged(scene, prev, PlayMode.PM_CornerKick_Left)
				|| isPlayModeChanged(scene, prev, PlayMode.PM_OffSide_Right)
				|| isPlayModeChanged(scene, prev, PlayMode.PM_Back_Pass_Right)
				|| isPlayModeChanged(scene, prev,
						PlayMode.PM_Free_Kick_Fault_Right)) {
			left = new Kicker(scene.time, 0);
		} else if (isPlayModeChanged(scene, prev, PlayMode.PM_FreeKick_Right)
				|| isPlayModeChanged(scene, prev, PlayMode.PM_KickIn_Right)
				|| isPlayModeChanged(scene, prev, PlayMode.PM_GoalKick_Right)
				|| isPlayModeChanged(scene, prev, PlayMode.PM_CornerKick_Right)
				|| isPlayModeChanged(scene, prev, PlayMode.PM_OffSide_Left)
				|| isPlayModeChanged(scene, prev, PlayMode.PM_Back_Pass_Left)
				|| isPlayModeChanged(scene, prev,
						PlayMode.PM_Free_Kick_Fault_Left)) {
			right = new Kicker(scene.time, 0);
		} else if (scene.pmode.pmode != PlayMode.PM_PlayOn) {
			leftKicker = null;
			rightKicker = null;
			return null;
		}

		boolean leftKickable = false;
		boolean rightKickable = false;
		for (int i = 0; i < Param.MAX_PLAYER; i++) {
			if (scene.player[i].isKickable(scene.ball.pos))
				leftKickable = true;
		}
		for (int i = Param.MAX_PLAYER; i < Param.MAX_PLAYER * 2; i++) {
			if (scene.player[i].isKickable(scene.ball.pos))
				rightKickable = true;
		}
		for (int i = 0; i < Param.MAX_PLAYER; i++) {
			if (!rightKickable
					&& (scene.player[i].isKicking() && prev.player[i]
							.isKickable(prev.ball.pos))
					|| (scene.player[i].isCatching() && prev.player[i]
							.isCatchable(prev.ball.pos))
					|| (scene.player[i].isTackling() && prev.player[i]
							.canTackle(prev.ball.pos))) {
				left = new Kicker(scene.time, scene.player[i].unum);
			}
		}
		for (int i = Param.MAX_PLAYER; i < Param.MAX_PLAYER * 2; i++) {
			if (!leftKickable
					&& (scene.player[i].isKicking() && prev.player[i]
							.isKickable(prev.ball.pos))
					|| (scene.player[i].isCatching() && prev.player[i]
							.isCatchable(prev.ball.pos))
					|| (scene.player[i].isTackling() && prev.player[i]
							.canTackle(prev.ball.pos))) {
				right = new Kicker(scene.time, scene.player[i].unum);
			}
		}

		if (left != null && right != null) {
			// leftKicker = null;
			// rightKicker = null;
			left = null;
			right = null;
		}
		if (left != null) {
			leftKicker = left;
			if (rightKicker != null) {
				rightKicker = null;
				countUpRight(scene.time);
			}
		}
		if (right != null) {
			rightKicker = right;
			if (leftKicker != null) {
				leftKicker = null;
				countUpLeft(scene.time);
			}
		}

		return null;
	}
}
