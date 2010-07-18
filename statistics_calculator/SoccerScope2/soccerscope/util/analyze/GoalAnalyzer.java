/*
 * $Log: GoalAnalyzer.java,v $
 * Revision 1.7  2003/01/04 08:35:36  koji
 * �ѥ���Ϣ�����ѥ��ߥ����ɥ�֥롢���줾�����ײ��Ϥ��ɲ�
 *
 * Revision 1.6  2002/10/15 10:09:48  koji
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
 * Revision 1.1  2002/09/12 11:25:42  koji
 * ��ײ��ϡ�������ǽ���ɲá���������ե�����졼�����ե������б�
 *
 *
 */

package soccerscope.util.analyze;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import soccerscope.model.GameEvent;
import soccerscope.model.Param;
import soccerscope.model.PlayMode;
import soccerscope.model.Scene;
import soccerscope.model.SceneSet;
import soccerscope.model.Team;
import soccerscope.model.WorldModel;
import soccerscope.util.geom.Line2f;
import soccerscope.util.geom.Point2f;
import soccerscope.util.geom.Rectangle2f;
import soccerscope.util.geom.Vector2f;

import com.jamesmurty.utils.XMLBuilder;

public class GoalAnalyzer extends SceneAnalyzer implements Xmling {

	public static String NAME = "Goal";
	public List<Goal> goalList = new LinkedList<Goal>();
	public List<GoalMiss> goalMissList = new LinkedList<GoalMiss>();
	public int lmissCount;
	public int rmissCount;

	class Goal implements Xmling {
		Kicker kicker;
		int time;

		public Goal(Kicker k, int time) {
			this.kicker = k;
			this.time = time;
		}

		@Override
		public void xmlElement(XMLBuilder builder) {
			XMLBuilder goal = builder.elem("goal").attr("time",
					String.valueOf(this.time)).attr("team",
							Team.name(this.kicker.team));
			this.kicker.xmlElement(goal);
		}
	}

	enum MissType {
		GOALIE_CATCHED, OUTSIDE, FAR_OUTSIDE
	};

	class GoalMiss implements Xmling {
		Kicker kicker;
		MissType missType;
		int time;

		public GoalMiss(Kicker k, MissType type, int time) {
			this.kicker = k;
			this.missType = type;
			this.time = time;
		}

		@Override
		public void xmlElement(XMLBuilder builder) {
			XMLBuilder goalmiss = builder.elem("goalmiss").attr("time",
					String.valueOf(this.time)).attr("type",
							this.missType.toString()).attr("team",
									Team.name(this.kicker.team));
			this.kicker.xmlElement(goalmiss);
		}
	}

	@Override
	public String getName() {
		return NAME;
	}

	public class Kicker implements Xmling {
		public int time;
		public int unum;
		public Point2f pos;
		public Zone shotZone;
		private int team;

		public Kicker() {
			this.time = 0;
			this.unum = 0;
			this.pos = null;
			this.shotZone = null;
		}

		public Kicker(int time, int unum, int team, Point2f pos, Zone shotZone) {
			this.time = time;
			this.unum = unum;
			this.pos = pos;
			this.shotZone = shotZone;
			this.team = team;
		}

		@Override
		public String toString() {
			return "kicker ::" + this.time + ": " + this.unum + ": " + this.team + ": "
			+ this.kickZone();

		}

		public String kickZone() {
			if (this.shotZone != null) {
				return this.shotZone.areaType.toString();
			}
			return AreaType.FAR_SHOT.toString();
		}

		@Override
		public void xmlElement(XMLBuilder builder) {
			builder.elem("kick").attr("player", String.valueOf(this.unum))
			.attr("time", String.valueOf(this.time)).attr("team",
					Team.name(this.team)).attr("zone", this.kickZone());
		}
	}

	// GOAL_AREA => pequena area
	// PENALTY_AREA => grande area
	public enum AreaType {
		GOAL_AREA, PENALTY_AREA, FAR_SHOT
	};

	public class Zone {
		public Rectangle2f area;
		public AreaType areaType;

		// team is one of Team.LEFT_SIDE; Team.RIGHT_SIDE; Team.NEUTRAL
		public int team;

		public Zone(int team, AreaType areaType, Rectangle2f area) {
			this.area = area;
			this.team = team;
			this.areaType = areaType;
		}

		public boolean contains(Point2f p) {
			return this.area.contains(p);
		}

	}

	private ArrayList<Zone> zones;

	private void initZones() {
		this.zones = new ArrayList<Zone>(2);

		// Attention! the order in which the areas are added is crucial if they
		// overlap!

		// LEFT_SIDE
		this.zones.add(new Zone(Team.LEFT_SIDE, AreaType.GOAL_AREA,
				new Rectangle2f(new Point2f(-52.5f, -9.15f), 5.5f, 18.32f)));
		this.zones.add(new Zone(Team.LEFT_SIDE, AreaType.PENALTY_AREA,
				new Rectangle2f(new Point2f(-52.5f, -20.15f), 16.5f, 40.3f)));

		// RIGHT_SIDE
		this.zones.add(new Zone(Team.RIGHT_SIDE, AreaType.GOAL_AREA,
				new Rectangle2f(new Point2f(47f, 9.15f), 5.5f, 18.32f)));
		this.zones.add(new Zone(Team.RIGHT_SIDE, AreaType.PENALTY_AREA,
				new Rectangle2f(new Point2f(36f, -20.15f), 16.5f, 40.3f)));
	}

	@Override
	public void init() {
		super.init();
		this.initZones();
		this.lmissCount = 0;
		this.rmissCount = 0;
		this.goalList.clear();
		this.goalMissList.clear();
	}

	@Override
	public GameEvent analyze(Scene scene, Scene prev) {
		GameEvent ge = null;
		/* ������⡼�ɤϻ��郎�ߤޤ뤿�ᡢprev�򸫤ơ�������⡼�ɤˤʤä��ִ֤����򡢼����� */
		// Standard goal
		if (prev != null) {
			if (this.isPlayModeChanged(scene, prev, PlayMode.PM_AfterGoal_Left)) {
				this.scoredGoalLeft(scene.time);
				ge = new GameEvent(scene.time, GameEvent.GOAL);
			}
			if (this.isPlayModeChanged(scene, prev, PlayMode.PM_AfterGoal_Right)) {
				this.scoredGoalRight(scene.time);
				ge = new GameEvent(scene.time, GameEvent.GOAL);
			}
		}

		// Goal after timeout - is this wise?
		if (scene.pmode.pmode == PlayMode.PM_TimeOver) {

			// crossing right goal line means goal for left team
			Line2f rightGoalLine = new Line2f(Param.topRightCorner,
					Param.bottomRightCorner);
			if (this.crossedLine(prev.ball.pos, scene.ball.pos, rightGoalLine)) {
				this.scoredGoalLeft(scene.time);
				ge = new GameEvent(scene.time, GameEvent.GOAL);
			}

			// figure it out yourself (hint: look at the previous comment)
			Line2f leftGoalLine = new Line2f(Param.topLeftCorner,
					Param.bottomLeftCorner);
			if (this.crossedLine(prev.ball.pos, scene.ball.pos, leftGoalLine)) {
				this.scoredGoalRight(scene.time);
				ge = new GameEvent(scene.time, GameEvent.GOAL);
			}
		}

		// if a goal was scored do not try to find the goal miss
		if (ge != null) {
			return ge;
		}

		// ###########################
		// # now for the goal miss!! #
		// ###########################

		// check if the goalie catched the ball
		if (prev != null && scene.isGoalieCatching()) {
			if (this.isPlayModeChanged(scene, prev, PlayMode.PM_FreeKick_Left)) {
				// left catched, if the right kicked we have a missed
				// opportunity
				Kicker k = this.getKickFault(prev.time);
				if (k.team == Team.RIGHT_SIDE) {
					this.goalMissed(k, MissType.GOALIE_CATCHED, scene.time);
				}
			}
			if (this.isPlayModeChanged(scene, prev, PlayMode.PM_FreeKick_Right)) {
				// right catched, if the left kicked we have a missed
				// opportunity
				Kicker k = this.getKickFault(prev.time);
				if (k.team == Team.LEFT_SIDE) {
					this.goalMissed(k, MissType.GOALIE_CATCHED, scene.time);
				}
			}
		}

		int missedGoalTeam = Team.NEUTRAL;
		Line2f relevantGoalLine = null;
		// the other miss is if the ball crossed the goal line (but was not
		// inside the actual goal)
		if (this.isPlayModeChanged(scene, prev, PlayMode.PM_GoalKick_Left)) {
			missedGoalTeam = Team.RIGHT_SIDE;
			relevantGoalLine = new Line2f(Param.topLeftCorner,
					Param.bottomLeftCorner);
		}
		if (this.isPlayModeChanged(scene, prev, PlayMode.PM_GoalKick_Right)) {
			missedGoalTeam = Team.LEFT_SIDE;
			relevantGoalLine = new Line2f(Param.topRightCorner,
					Param.bottomRightCorner);
		}

		if (missedGoalTeam != Team.NEUTRAL && relevantGoalLine != null) {
			Point2f next_ball_pos = new Point2f(prev.ball.pos);
			Vector2f ballvel = new Vector2f(prev.ball.vel);
			next_ball_pos.add(ballvel); // update ball position
			// we know it wasn't a goal because we are in a goal kick and
			// we opted to count as an attempted shot at the goal, a goal that
			// misses by less than double of the goal width (Param.GOAL_WIDTH is
			// not dividing by 2)
			if (this.crossedLine(prev.ball.pos, next_ball_pos,
					relevantGoalLine, Param.GOAL_WIDTH)) {
				Kicker k = this.getKickFault(prev.time);
				// kicker must be in the correct team
				if (k.team == missedGoalTeam) {
					this.goalMissed(k, MissType.OUTSIDE, scene.time);
				}
			}else if(this.crossedLine(prev.ball.pos, next_ball_pos,
					relevantGoalLine, 34.0f)) {
				Kicker k = this.getKickFault(prev.time);
				// kicker must be in the correct team
				if (k.team == missedGoalTeam) {
					this.goalMissed(k, MissType.FAR_OUTSIDE, scene.time);
				}
			}
		}
		return ge;
	}

	private boolean crossedLine(Point2f prevPos, Point2f currPos, Line2f line) {
		return this.crossedLine(prevPos, currPos, line, Param.GOAL_WIDTH / 2);
	}

	private boolean crossedLine(Point2f prevPos, Point2f currPos, Line2f line,
			float distance_to_middle) {
		Line2f traj = new Line2f(prevPos, currPos);
		// the next 3 if's check if the positions(prevPos,currPos) intersect the
		// line
		// 1st: check if the trajectory line crosses the goal line
		// 2nd: check if the line segment was crossed
		// 3rd: check if the crossing occurred between the said positions
		if (traj.intersect(line)) {
			Point2f p = traj.intersection(line);
			if (Math.abs(p.y) <= distance_to_middle) {
				if ((prevPos.x <= p.x && p.x <= currPos.x)
						|| (currPos.x <= p.x && p.x <= prevPos.x)) {
					return true;
				}
			}
		}
		return false;
	}

	public void scoredGoalLeft(int time) {
		Kicker k = this.getKickFault(time);
		this.goalScored(k, time);
		super.countUpLeft(time);
	}

	public void scoredGoalRight(int time) {
		Kicker k = this.getKickFault(time);
		this.goalScored(k, time);
		super.countUpRight(time);
	}

	private void goalScored(Kicker k, int time) {
		System.out.println("GOAL! fault-> " + k);
		this.goalList.add(new Goal(k, time));
	}

	private void goalMissed(Kicker k, MissType type, int time) {
		if (k.team == Team.LEFT_SIDE) {
			this.lmissCount++;
		} else if (k.team == Team.RIGHT_SIDE) {
			this.rmissCount++;
		}
		System.out
		.println("GOAL MISSED(" + type.toString() + ") kicker-> " + k);
		this.goalMissList.add(new GoalMiss(k, type, time));
	}

	private Kicker getKickFault(int time) {
		SceneSet scene_set = WorldModel.getInstance().getSceneSet();
		for (int t = time; t > 0; t--) {
			Scene scene = scene_set.getScene(t);
			Scene prev = scene_set.getScene(t - 1);

			if (scene == null || prev == null) {
				System.err.println("UPS! cannot calculate kicker...");
				return null;
			}

			for (int i = 0; i < Param.MAX_PLAYER * 2; i++) {
				if ((scene.player[i].isKicking() && prev.player[i]
				                                                .isKickable(prev.ball.pos))
				                                                || (scene.player[i].isCatching() && prev.player[i]
				                                                                                                .isCatchable(prev.ball.pos))
				                                                                                                || (scene.player[i].isTackling() && prev.player[i]
				                                                                                                                                                .canTackle(prev.ball.pos))) {
					// we found the culprit! check the zone and return it!
					for (Zone z : this.zones) {
						if (z.contains(scene.player[i].pos)) {
							return new Kicker(scene.time, scene.player[i].unum,
									scene.player[i].side, scene.player[i].pos,
									z);
						}
					}
					// outside the defined zones
					return new Kicker(scene.time, scene.player[i].unum,
							scene.player[i].side, scene.player[i].pos, null);
				}
			}

		}

		return null;
	}

	@Override
	public void xmlElement(XMLBuilder builder) {

		XMLBuilder goals = builder.elem("goals").attr("left",
				String.valueOf(this.lcount)).attr("right", String.valueOf(this.rcount));
		for (Goal g : this.goalList) {
			g.xmlElement(goals);
		}

		XMLBuilder goalmiss = builder.elem("goalmisses").attr("left",
				String.valueOf(this.lmissCount)).attr("right",
						String.valueOf(this.rmissCount));
		for (GoalMiss g : this.goalMissList) {
			g.xmlElement(goalmiss);
		}

	}

	public int nGoals(int side) {
		if(side == Team.LEFT_SIDE) {
			return this.lcount;
		}else if (side == Team.RIGHT_SIDE) {
			return this.rcount;
		}
		assert false;
		return 0;
	}
}
