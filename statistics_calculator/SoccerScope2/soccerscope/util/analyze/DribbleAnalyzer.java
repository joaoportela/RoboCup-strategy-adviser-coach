/*
 * $Log: DribbleAnalyzer.java,v $
 * Revision 1.1  2003/01/04 08:35:36  koji
 * �ѥ���Ϣ�����ѥ��ߥ����ɥ�֥롢���줾�����ײ��Ϥ��ɲ�
 *
 */

package soccerscope.util.analyze;

import java.util.ArrayList;

import soccerscope.model.GameEvent;
import soccerscope.model.Param;
import soccerscope.model.PlayMode;
import soccerscope.model.Player;
import soccerscope.model.Scene;

public class DribbleAnalyzer extends SceneAnalyzer {

	public static String NAME = "Dribble";

	private ArrayList<Dribbler> dribbleLeftList = new ArrayList<Dribbler>();
	private ArrayList<Dribbler> dribbleRightList = new ArrayList<Dribbler>();
	private Dribbler leftDribbler;
	private Dribbler rightDribbler;

	public void init() {
		super.init();
		dribbleLeftList.clear();
		dribbleRightList.clear();
		leftDribbler = null;
		rightDribbler = null;
	}

	public String getName() {
		return NAME;
	}

	public class Dribbler {
		public int time;
		public Player player;
		public boolean counted;

		public Dribbler() {
			time = 0;
			player = null;
			counted = false;
		}

		public Dribbler(int time, Player player) {
			this.time = time;
			this.player = player;
			this.counted = false;
		}

		public void set(Dribbler k) {
			time = k.time;
			player = k.player;
			counted = k.counted;
		}

		public String toString() {
			return "dribbler ::" + time + ": " + player.unum;
		}
	}

	public GameEvent analyze(Scene scene, Scene prev) {

		if (prev == null)
			return null;

		// �ץ쥤��ʳ���, ��
		if (scene.pmode.pmode != PlayMode.PM_PlayOn) {
			leftDribbler = null;
			rightDribbler = null;
			return null;
		}

		Dribbler left = null;
		Dribbler right = null;

		for (int i = 0; i < Param.MAX_PLAYER; i++) {
			if ((scene.player[i].isKicking() && prev.player[i]
					.isKickable(prev.ball.pos))
					|| (scene.player[i].isCatching() && prev.player[i]
							.isCatchable(prev.ball.pos))
					|| (scene.player[i].isTackling() && prev.player[i]
							.canTackle(prev.ball.pos))) {
				left = new Dribbler(scene.time, scene.player[i]);
			}
		}
		for (int i = Param.MAX_PLAYER; i < Param.MAX_PLAYER * 2; i++) {
			if ((scene.player[i].isKicking() && prev.player[i]
					.isKickable(prev.ball.pos))
					|| (scene.player[i].isCatching() && prev.player[i]
							.isCatchable(prev.ball.pos))
					|| (scene.player[i].isTackling() && prev.player[i]
							.canTackle(prev.ball.pos))) {
				right = new Dribbler(scene.time, scene.player[i]);
			}
		}

		if (left != null && right != null) {
			leftDribbler = null;
			rightDribbler = null;
			left = null;
			right = null;
		}
		if (left != null) {
			if (leftDribbler != null
					&& leftDribbler.player.unum == left.player.unum
					&& !leftDribbler.counted
					&& doDashed(left.player, leftDribbler.player)) {
				countUpLeft(leftDribbler.time);
				dribbleLeftList.add(leftDribbler);
				leftDribbler.counted = true;
				left.counted = true;
			}
			if (leftDribbler != null
					&& leftDribbler.player.unum == left.player.unum
					&& leftDribbler.counted)
				left.counted = true;
			leftDribbler = left;
			if (right == null)
				rightDribbler = null;
		}
		if (right != null) {
			if (rightDribbler != null
					&& rightDribbler.player.unum == right.player.unum
					&& !rightDribbler.counted
					&& doDashed(right.player, rightDribbler.player)) {
				countUpRight(rightDribbler.time);
				dribbleRightList.add(rightDribbler);
				rightDribbler.counted = true;
				right.counted = true;
			}
			if (rightDribbler != null
					&& rightDribbler.player.unum == right.player.unum
					&& rightDribbler.counted)
				right.counted = true;
			rightDribbler = right;
			if (left == null)
				leftDribbler = null;
		}

		return null;
	}
}
