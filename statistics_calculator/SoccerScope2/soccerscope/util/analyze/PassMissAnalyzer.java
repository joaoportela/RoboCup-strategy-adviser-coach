/*
 * $Log: PassMissAnalyzer.java,v $
 * Revision 1.1  2003/01/04 08:35:37  koji
 * �ѥ���Ϣ�����ѥ��ߥ����ɥ�֥롢���줾�����ײ��Ϥ��ɲ�
 *
 * Revision 1.1  2002/12/02 08:48:01  koji
 * �ܡ���򼺤ä�������ѥ��μ������ʿ��X��ɸ�ͤ��ɲ�
 */

package soccerscope.util.analyze;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import soccerscope.model.GameEvent;
import soccerscope.model.Param;
import soccerscope.model.PlayMode;
import soccerscope.model.Scene;
import soccerscope.model.Team;
import soccerscope.util.geom.Point2f;
import soccerscope.util.geom.Rectangle2f;
import soccerscope.util.geom.Vector2f;

import com.jamesmurty.utils.XMLBuilder;

public class PassMissAnalyzer extends SceneAnalyzer implements Xmling {

	public static String NAME = "Pass Miss";

	private Kicker leftKicker;
	private Kicker leftKickerTarget;
	private Kicker rightKicker;
	private Kicker rightKickerTarget;

	List<PassMiss> passMissList;

	public class PassMiss implements Xmling {
		public int side;
		public Kicker sender;
		public Kicker receiver;
		public Zone zone;

		public PassMiss(int side, Kicker s, Kicker r, Zone z) {
			this.side = side;
			this.sender = s;
			this.receiver = r;
			this.zone = z;
		}

		@Override
		public String toString() {
			return this.side + " :: " + +this.sender.time + ": " + this.sender.unum + " -> "
			+ this.receiver.time + ": " + this.receiver.unum;
		}

		public boolean inOffensiveField() {
			// pass is offensive if it is on the opponent side
			return this.zone.side != this.side;
		}

		@Override
		public void xmlElement(XMLBuilder builder) {
			XMLBuilder passmiss = builder.elem("passmiss").attr("team",
					Team.name(this.side)).attr("offensive",
							String.valueOf(this.inOffensiveField()));
			this.sender.xmlElement(passmiss.elem("kick"));
			this.receiver.xmlElement(passmiss.elem("target"));
		}
	}

	/* START - zones stuffs */
	public class Zone {
		Rectangle2f area;
		int countLeft;
		int countRight;

		int side; // side of the field where the zone is...

		public Zone(int side, Rectangle2f a) {
			this.area = a;
			this.side = side;
			this.countLeft = 0;
			this.countRight = 0;
		}

		public boolean contains(Point2f p) {
			return this.area.contains(p);
		}
	}

	ArrayList<Zone> fieldZones;

	private void initZones() {
		this.fieldZones = new ArrayList<Zone>(2);
		this.fieldZones.add(new Zone(Team.LEFT_SIDE, new Rectangle2f(
				new Point2f(-52.5f, -34f), 52.5f, 68f)));
		this.fieldZones.add(new Zone(Team.RIGHT_SIDE, new Rectangle2f(
				new Point2f(0.0f, -34f), 52.5f, 68f)));

	}

	/* END - zones stuffs */

	@Override
	public void init() {
		super.init();
		this.initZones();
		this.passMissList = new LinkedList<PassMiss>();
		this.leftKicker = null;
		this.rightKicker = null;
	}

	@Override
	public String getName() {
		return NAME;
	}

	public class Kicker implements Xmling {
		public int time;
		public int unum;
		public boolean offside;
		public Point2f position;

		public Kicker() {
			this.time = 0;
			this.unum = 0;
			this.position = null;
		}

		public Kicker(int time, int unum, Point2f position, boolean offside) {
			this.time = time;
			this.unum = unum;
			this.position = position;
			this.offside = offside;
		}

		@Override
		public String toString() {
			return "kicker ::" + this.time + ": " + this.unum;
		}

		@Override
		public void xmlElement(XMLBuilder builder) {
			builder.attr("player", String.valueOf(this.unum)).attr("time",
					String.valueOf(this.time))
					.attr("offside",String.valueOf(this.offside));

		}
	}

	@Override
	public GameEvent analyze(Scene scene, Scene prev) {

		if (prev == null) {
			return null;
		}

		Kicker left = null;
		Kicker right = null;

		// �ץ쥤��ʳ���, ��
		if (this.isPlayModeChanged(scene, prev, PlayMode.PM_FreeKick_Left)
				|| this.isPlayModeChanged(scene, prev, PlayMode.PM_KickIn_Left)
				|| this.isPlayModeChanged(scene, prev, PlayMode.PM_GoalKick_Left)
				|| this.isPlayModeChanged(scene, prev, PlayMode.PM_CornerKick_Left)
				|| this.isPlayModeChanged(scene, prev, PlayMode.PM_OffSide_Right)
				|| this.isPlayModeChanged(scene, prev, PlayMode.PM_Back_Pass_Right)
				|| this.isPlayModeChanged(scene, prev,
						PlayMode.PM_Free_Kick_Fault_Right)) {
			left = new Kicker(scene.time, 0, null, false);
		} else if (this.isPlayModeChanged(scene, prev, PlayMode.PM_FreeKick_Right)
				|| this.isPlayModeChanged(scene, prev, PlayMode.PM_KickIn_Right)
				|| this.isPlayModeChanged(scene, prev, PlayMode.PM_GoalKick_Right)
				|| this.isPlayModeChanged(scene, prev, PlayMode.PM_CornerKick_Right)
				|| this.isPlayModeChanged(scene, prev, PlayMode.PM_OffSide_Left)
				|| this.isPlayModeChanged(scene, prev, PlayMode.PM_Back_Pass_Left)
				|| this.isPlayModeChanged(scene, prev,
						PlayMode.PM_Free_Kick_Fault_Left)) {
			right = new Kicker(scene.time, 0, null, false);
		} else if (scene.pmode.pmode != PlayMode.PM_PlayOn) {
			this.leftKicker = null;
			this.rightKicker = null;
			return null;
		}

		boolean leftKickable = false;
		boolean rightKickable = false;
		for (int i = 0; i < Param.MAX_PLAYER; i++) {
			if (scene.player[i].isKickable(scene.ball.pos)) {
				leftKickable = true;
			}
		}
		for (int i = Param.MAX_PLAYER; i < Param.MAX_PLAYER * 2; i++) {
			if (scene.player[i].isKickable(scene.ball.pos)) {
				rightKickable = true;
			}
		}
		for (int i = 0; i < Param.MAX_PLAYER; i++) {
			if (!rightKickable
					&& (scene.player[i].isKicking() && prev.player[i]
					                                               .isKickable(prev.ball.pos))
					                                               || (scene.player[i].isCatching() && prev.player[i]
					                                                                                               .isCatchable(prev.ball.pos))
					                                                                                               || (scene.player[i].isTackling() && prev.player[i]
					                                                                                                                                               .canTackle(prev.ball.pos))) {
				left = new Kicker(scene.time, scene.player[i].unum,
						scene.player[i].pos, scene.player[i].offside);
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
				right = new Kicker(scene.time, scene.player[i].unum,
						scene.player[i].pos, scene.player[i].offside);
			}
		}

		if (left != null && right != null) {
			// leftKicker = null;
			// rightKicker = null;
			left = null;
			right = null;
		}
		if (left != null) {
			if (this.rightKicker != null) {
				// rightKicker missed the pass. lets increment the ball position
				// for the zone count
				this.countUpRight(this.rightKicker, this.rightKickerTarget);
				this.rightKicker = null;
			}
			Kicker target = this.getPassKickTarget(scene, LEFT, left.unum);
			if (target != null) {
				this.leftKicker = left;
				this.leftKickerTarget = target;
			} else {
				this.leftKicker = null;
			}
		}
		if (right != null) {
			if (this.leftKicker != null) {
				// leftKicker missed the pass. lets increment the ball position
				// for the zone count
				this.countUpLeft(this.leftKicker, this.leftKickerTarget);
				this.leftKicker = null;
			}
			Kicker target = this.getPassKickTarget(scene, RIGHT, right.unum);
			if (target != null) {
				this.rightKicker = right;
				this.rightKickerTarget = target;
			} else {
				this.rightKicker = null;
			}
		}

		return null;
	}

	public void countUpLeft(Kicker sender, Kicker receiver) {
		Zone zone = PassMissAnalyzer.whichZone(this.fieldZones, sender.position);
		if (zone == null) {
			return;
		}
		PassMiss pass = new PassMiss(Team.LEFT_SIDE, sender, receiver, zone);
		this.passMissList.add(pass);

		System.out.println("LEFT_PASSMISS START_CYCLE(" + sender.time
				+ ") END_CYCLE(" + receiver.time + ")");
		super.countUpLeft(receiver.time);
	}

	public void countUpRight(Kicker sender, Kicker receiver) {
		Zone zone = PassMissAnalyzer.whichZone(this.fieldZones, sender.position);
		if (zone == null) {
			return;
		}
		PassMiss pass = new PassMiss(Team.RIGHT_SIDE, sender, receiver, zone);
		this.passMissList.add(pass);

		System.out.println("RIGHT_PASSMISS START_CYCLE(" + sender.time
				+ ") END_CYCLE(" + receiver.time + ")");
		super.countUpRight(receiver.time);
	}

	public static Zone whichZone(List<Zone> zones, Point2f coord) {
		for (Zone z : zones) {
			if (z.area.contains(coord)) {
				return z;
			}
		}
		return null;
	}

	/*
	 * predictive function that checks if the player had the intention of
	 * passing to a team-mate. this is used to determine the target kicker in
	 * the event of a pass miss.
	 */
	public Kicker getPassKickTarget(Scene scene, int team, int unum) {
		if (unum == 0) {
			return null;
		}
		int start = 0;
		if (team == RIGHT) {
			start = Param.MAX_PLAYER;
		}

		Point2f tmpp = new Point2f(scene.ball.pos);
		Vector2f tmpv = new Vector2f(scene.ball.vel);
		for (int n = 0; n < 100; n++) {
			for (int i = 0; i < Param.MAX_PLAYER; i++) {
				float dist = scene.player[i + start].pos.dist(tmpp)
				- Param.KICKABLE_R;
				if (scene.player[i + start].estimateMinTimeByDistance(dist,
						0.6f) < n) {
					if (unum == scene.player[i + start].unum) {
						return null;
					} else {
						return new Kicker(scene.time + n, scene.player[i
						                                               + start].unum, scene.player[i + start].pos,
						                                               scene.player[i + start].offside);
					}
				}
			}
			tmpp.add(tmpv); // update ball position
			tmpv.scale(0.94f); // account for ball friction
			if (!this.inField(tmpp)) {
				return null;
			}
		}
		return null;
	}

	public boolean inField(Point2f pos) {
		return -52.5f <= pos.x && pos.x <= 52.5f && -34.0f <= pos.y
		&& pos.y <= 34.0;
	}

	@Override
	public void xmlElement(XMLBuilder builder) {
		int missLeft = this.nPassMiss(Team.LEFT_SIDE);
		int missRight = this.nPassMiss(Team.RIGHT_SIDE);
		builder = builder.elem("passmisses").attr("left",
				String.valueOf(missLeft)).attr("right",
						String.valueOf(missRight));
		for (PassMiss p : this.passMissList) {
			p.xmlElement(builder);
		}

	}

	private int nPassMiss(int side) {
		int count = 0;
		for (PassMiss p : this.passMissList) {
			if (p.side == side) {
				count++;
			}
		}
		return count;
	}
}
