/*
 * $Log: GoalKickAnalyzer.java,v $
 * Revision 1.3  2003/01/04 08:35:36  koji
 * �ѥ���Ϣ�����ѥ��ߥ����ɥ�֥롢���줾�����ײ��Ϥ��ɲ�
 *
 * Revision 1.2  2002/10/15 10:09:48  koji
 * ��������Pass������ȡ�Team/�����̥ơ��֥��ɲ�
 *
 * Revision 1.1  2002/09/12 11:25:42  koji
 * ��ײ��ϡ�������ǽ���ɲá���������ե�����졼�����ե������б�
 *
 *
 */

package soccerscope.util.analyze;

import java.util.LinkedList;
import java.util.List;

import soccerscope.model.GameEvent;
import soccerscope.model.PlayMode;
import soccerscope.model.Scene;
import soccerscope.model.Team;

import com.jamesmurty.utils.XMLBuilder;

public class GoalKickAnalyzer extends SceneAnalyzer implements Xmling {

	public static String NAME = "Goal Kick";

	private final List<GoalKick> goalkicksList = new LinkedList<GoalKick>();

	public static class GoalKick implements Xmling {
		public int side;
		public int time;

		public GoalKick(final int side, final int time) {
			this.side = side;
			this.time = time;
		}

		@Override
		public void xmlElement(final XMLBuilder builder) {
			builder.elem("goalkick").attr("team", Team.name(this.side)).attr(
					"time", String.valueOf(this.time));
		}
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public GameEvent analyze(final Scene scene, final Scene prev) {
		GameEvent ge = null;

		/* �����륭�å��⡼�ɤϻ��郎�ߤޤ�ʤ����ᡢprev�򸫤ơ������륭�å��⡼�ɤˤʤä��ִ֤����򡢼����� */
		if (prev != null) {
			if (this.isPlayModeChanged(scene, prev, PlayMode.PM_GoalKick_Left)) {
				this.countUp(Team.LEFT_SIDE, scene.time);
				ge = new GameEvent(scene.time, GameEvent.GOAL_KICK);
			}
			if (this.isPlayModeChanged(scene, prev, PlayMode.PM_GoalKick_Right)) {
				this.countUp(Team.RIGHT_SIDE, scene.time);
				ge = new GameEvent(scene.time, GameEvent.GOAL_KICK);
			}
		}
		return ge;
	}

	@Override
	public void countUp(final int side, final int time) {
		this.goalkicksList.add(new GoalKick(side, time));
		if (side == Team.LEFT_SIDE) {
			this.countUpLeft(time);
		} else if (side == Team.RIGHT_SIDE) {
			this.countUpRight(time);
		}

	}

	public int getCount(int side) {
		if(side == Team.LEFT_SIDE) {
			return this.lcount;
		}else if (side == Team.RIGHT_SIDE) {
			return this.rcount;
		}
		assert false;
		return 0;
	}

	@Override
	public void xmlElement(final XMLBuilder builder) {
		final XMLBuilder goalkicks = builder.elem("goalkicks").attr("left",
				String.valueOf(this.lcount)).attr("right",
						String.valueOf(this.rcount));

		for (final GoalKick gk : this.goalkicksList) {
			gk.xmlElement(goalkicks);
		}

	}
}
