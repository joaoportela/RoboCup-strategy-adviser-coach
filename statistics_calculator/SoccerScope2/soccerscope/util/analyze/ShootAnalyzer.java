/*
 * $Log: ShootAnalyzer.java,v $
 * Revision 1.7  2003/01/04 08:35:37  koji
 * �ѥ���Ϣ�����ѥ��ߥ����ɥ�֥롢���줾�����ײ��Ϥ��ɲ�
 *
 * Revision 1.6  2002/10/15 10:09:49  koji
 * ��������Pass������ȡ�Team/�����̥ơ��֥��ɲ�
 *
 * Revision 1.5  2002/10/11 10:41:36  koji
 * �Х�����(HeteroParam),GoalȽ������
 *
 * Revision 1.4  2002/10/04 10:39:14  koji
 * Tuple,Point,Vector�Υѥå������ѹ���Geometry,Circle,Line,Rectangle�ɲ�
 *
 * Revision 1.3  2002/09/17 07:46:24  koji
 * WatchBall��ZoomBall�Υ��������ѹ���Publish�ѤΥѥͥ��ɲá�������ν���
 * �Υ�����򥫥���Ȥ��Ƥ��ʤ��ä��Τ���
 *
 * Revision 1.2  2002/09/13 11:46:09  koji
 * fixed missing to update HeteroParam
 *
 * Revision 1.1  2002/09/12 11:25:43  koji
 * ��ײ��ϡ�������ǽ���ɲá���������ե�����졼�����ե������б�
 *
 *
 */

package soccerscope.util.analyze;

import soccerscope.model.GameEvent;
import soccerscope.model.Param;
import soccerscope.model.PlayMode;
import soccerscope.model.Scene;
import soccerscope.util.geom.Line2f;
import soccerscope.util.geom.Point2f;
import soccerscope.util.geom.Vector2f;

public class ShootAnalyzer extends SceneAnalyzer {

	public static String NAME = "Shoot";

	private int ltime;
	private int lgoal;
	//private boolean lkick;
	private boolean lshoot;
	private int rtime;
	private int rgoal;
	//private boolean rkick;
	private boolean rshoot;

	public void init() {
		super.init();
		ltime = 0;
		lgoal = 0;
		//lkick = false;
		lshoot = false;
		rtime = 0;
		rgoal = 0;
		//rkick = false;
		rshoot = false;
	}

	public String getName() {
		return NAME;
	}

	public GameEvent analyze(Scene scene, Scene prev) {
		GameEvent ge = null;

		if (lshoot) {
			if (scene.pmode.pmode == PlayMode.PM_AfterGoal_Left
					|| scene.pmode.pmode == PlayMode.PM_GoalKick_Right
					|| (scene.pmode.pmode == PlayMode.PM_FreeKick_Right && scene
							.isGoalieCatching())) {
				countUpLeft(ltime);
				ge = new GameEvent(ltime, GameEvent.SHOOT);
				lshoot = false;
			}
			for (int i = Param.MAX_PLAYER; i < Param.MAX_PLAYER * 2; i++) {
				if (scene.player[i].isKicking() || scene.player[i].isTackling()) {
					if (inRightGoalArea(scene.player[i].pos)) {
						countUpLeft(ltime);
						lgoal++;
						ge = new GameEvent(ltime, GameEvent.SHOOT);
						lshoot = false;
					}
				}
			}
			if (scene.pmode.pmode == PlayMode.PM_TimeOver) {
				Line2f btraj = new Line2f(prev.ball.pos, scene.ball.pos);
				Line2f rightGoalLine = new Line2f(Param.topRightCorner,
						Param.bottomRightCorner);
				if (btraj.intersect(rightGoalLine)) {
					Point2f p = btraj.intersection(rightGoalLine);
					if (Math.abs(p.y) <= Param.GOAL_WIDTH / 2
							&& ((prev.ball.pos.x <= p.x && p.x <= scene.ball.pos.x) || (scene.ball.pos.x <= p.x && p.x <= prev.ball.pos.x))) {
						countUpLeft(ltime);
						lgoal++;
						ge = new GameEvent(ltime, GameEvent.SHOOT);
						lshoot = false;
					}
				}
			}
		}
		if (rshoot) {
			if (scene.pmode.pmode == PlayMode.PM_AfterGoal_Right
					|| scene.pmode.pmode == PlayMode.PM_GoalKick_Left
					|| (scene.pmode.pmode == PlayMode.PM_FreeKick_Left && scene
							.isGoalieCatching())) {
				countUpRight(rtime);
				ge = new GameEvent(rtime, GameEvent.SHOOT);
				rshoot = false;
			}
			for (int i = 0; i < Param.MAX_PLAYER; i++) {
				if (scene.player[i].isKicking() || scene.player[i].isTackling()) {
					if (inLeftGoalArea(scene.player[i].pos)) {
						countUpRight(rtime);
						rgoal++;
						ge = new GameEvent(rtime, GameEvent.SHOOT);
						rshoot = false;
					}
				}
			}
			if (scene.pmode.pmode == PlayMode.PM_TimeOver) {
				Line2f btraj = new Line2f(prev.ball.pos, scene.ball.pos);
				Line2f leftGoalLine = new Line2f(Param.topLeftCorner,
						Param.bottomLeftCorner);
				if (btraj.intersect(leftGoalLine)) {
					Point2f p = btraj.intersection(leftGoalLine);
					if (Math.abs(p.y) <= Param.GOAL_WIDTH / 2
							&& ((prev.ball.pos.x <= p.x && p.x <= scene.ball.pos.x) || (scene.ball.pos.x <= p.x && p.x <= prev.ball.pos.x))) {
						countUpRight(rtime);
						rgoal++;
						ge = new GameEvent(rtime, GameEvent.SHOOT);
						rshoot = false;
					}
				}
			}
		}
		for (int i = 0; i < Param.MAX_PLAYER; i++) {
			if (scene.player[i].isKicking()) {
				//lkick = true;
				if (scene.ball.pos.dir(rightGoalTop) <= scene.ball.vel.th()
						&& scene.ball.pos.dir(rightGoalBottom) >= scene.ball.vel
								.th()) {
					Point2f pos = new Point2f();
					pos.add(scene.ball.pos, Vector2f.polar2vector(getFinalDist(
							scene.ball.vel.r(), 0.94f, 0.1f), scene.ball.vel
							.th()));
					if (pos.x >= 52.5) {
						lshoot = true;
						ltime = scene.time;
					}
				} else {
					lshoot = false;
				}
			}
		}
		for (int i = Param.MAX_PLAYER; i < Param.MAX_PLAYER * 2; i++) {
			if (scene.player[i].isKicking()) {
				//rkick = true;
				if (normalize(scene.ball.pos.dir(leftGoalTop)) >= normalize(scene.ball.vel
						.th())
						&& normalize(scene.ball.pos.dir(leftGoalBottom)) <= normalize(scene.ball.vel
								.th())) {
					Point2f pos = new Point2f();
					pos.add(scene.ball.pos, Vector2f.polar2vector(getFinalDist(
							scene.ball.vel.r(), 0.94f, 0.1f), scene.ball.vel
							.th()));
					if (pos.x <= -52.5) {
						rshoot = true;
						rtime = scene.time;
					}
				} else {
					rshoot = false;
				}
			}
		}

		return ge;
	}

	private static Point2f leftGoalTop = new Point2f(-52.5f, -8.16f);
	private static Point2f leftGoalBottom = new Point2f(-52.5f, 8.16f);
	private static Point2f rightGoalTop = new Point2f(52.5f, -8.16f);
	private static Point2f rightGoalBottom = new Point2f(52.5f, 8.16f);

	private boolean inLeftGoalArea(Point2f pos) {
		return -52.5f <= pos.x && pos.x <= -47.0f && -9.16 <= pos.y
				&& pos.y <= 9.16;
	}

	private boolean inRightGoalArea(Point2f pos) {
		return 47.0f <= pos.x && pos.x <= 52.5f && -9.16 <= pos.y
				&& pos.y <= 9.16;
	}
}
