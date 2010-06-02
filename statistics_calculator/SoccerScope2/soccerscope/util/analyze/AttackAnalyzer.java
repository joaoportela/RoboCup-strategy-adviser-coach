/**
 * Objects of this class are supposed to analyze
 * the game and detect when an attack is done
 */
package soccerscope.util.analyze;

import java.util.ArrayList;

import soccerscope.model.GameEvent;
import soccerscope.model.Param;
import soccerscope.model.PlayMode;
import soccerscope.model.Player;
import soccerscope.model.Scene;
import soccerscope.model.SceneSet;
import soccerscope.model.Team;
import soccerscope.model.WorldModel;
import soccerscope.util.geom.Point2f;
import soccerscope.util.geom.Rectangle2f;

import com.jamesmurty.utils.XMLBuilder;

/**
 * @author João Portela
 * 
 */
public class AttackAnalyzer extends SceneAnalyzer implements Xmling {

	public static final String NAME = "Attack analyzer";

	/*
	 * attack definition
	 */
	public enum AttackType {
		SLOW, MEDIUM, FAST, BROKEN
	};

	public class Attack implements Xmling {
		public static final int ATTACK_MIN_TIME = 30;

		private static final float FAST_MIN_VELOCITY = 36f / 40f;
		private static final float MEDIUM_MIN_VELOCITY = 36f / 90f;

		private int fastAttackMaxTime(float distance) {
			return (int) (distance / FAST_MIN_VELOCITY);
		}

		private int mediumAttackMaxTime(float distance) {
			return (int) ( distance / MEDIUM_MIN_VELOCITY);
		}

		int startTime; // start time of the attack
		Point2f startPos; // position in which the attack started
		int endTime; // end time of the attack
		int dangerousStartTime; // start time of the dangerous part of the
		// attack
		int team; // team doing the attack

		public Attack(int startTime, int team, Point2f startPos) {
			this.startTime = startTime;
			this.dangerousStartTime = -1;
			this.endTime = -1;
			this.team = team;
			this.startPos = startPos;
		}

		private boolean validTeam() {
			return (this.team == Team.LEFT_SIDE || this.team == Team.RIGHT_SIDE);
		}

		public boolean valid() {
			if (this.validTeam()) {
				if (this.startTime > 0 && this.endTime > this.startTime) {
					return (this.endTime - this.startTime) > ATTACK_MIN_TIME;
				}
			}
			return false;
		}

		public AttackType attackType() {
			if (this.dangerous()) {
				int timeToDangerZone = this.timeToDangerZone();
				float distanceToDangerZone = this.distanceToDangerZone();
				if (timeToDangerZone < this.fastAttackMaxTime(distanceToDangerZone)) {
					return AttackType.FAST;
				} else if (timeToDangerZone < this.mediumAttackMaxTime(distanceToDangerZone)) {
					return AttackType.MEDIUM;
				}
				return AttackType.SLOW;
			}
			return AttackType.BROKEN;
		}

		private float distanceToDangerZone() {
			Point2f startPos = WorldModel.getInstance().getSceneSet().getScene(
					this.startTime).ball.pos;
			Point2f endPos = WorldModel.getInstance().getSceneSet().getScene(
					this.endTime).ball.pos;
			float distance = 0;
			distance = Math.abs(endPos.x - startPos.x);
			return distance;
		}

		public int timeToDangerZone() {
			if (this.dangerous()) {
				return this.dangerousStartTime - this.startTime;
			} else {
				return -1;
			}
		}

		public boolean dangerous() {
			if (this.valid()) {
				if (this.dangerousStartTime >= this.startTime
						&& this.endTime > this.dangerousStartTime) {
					return true;
				}
			}
			return false;

		}

		@Override
		public void xmlElement(XMLBuilder builder) {
			builder.elem("attack")
			.attr("start", String.valueOf(this.startTime))
			.attr("end", String.valueOf(this.endTime))
			.attr("team", Team.name(this.team))
			.attr("dangerous", String.valueOf(this.dangerous()))
			.attr("type",this.attackType().toString());
			//				.attr("dangerousstarttime", String.valueOf(this.dangerousStartTime))
			//				.attr("distanceToDangerZone", String.valueOf(this.distanceToDangerZone()))
			//				.attr("timeToDangerZone", String.valueOf(this.timeToDangerZone()));
		}

	}

	/*
	 * Zone definition
	 */
	public class Zone {
		Rectangle2f area;
		int field;

		public Zone(int field, Rectangle2f a) {
			this.area = a;
			this.field = field;
		}

		public boolean contains(Point2f p) {
			return this.area.contains(p);
		}
	}

	Attack attack;
	ArrayList<Attack> attacks;

	ArrayList<Zone> lastDangerZones;

	// ArrayList<Zone> fieldZones;

	private void initZones() {
		this.lastDangerZones = new ArrayList<Zone>(2);
		this.lastDangerZones.add(new Zone(Team.LEFT_SIDE, new Rectangle2f(
				new Point2f(-52.5f, -34f), 16.5f, 68f)));
		this.lastDangerZones.add(new Zone(Team.RIGHT_SIDE, new Rectangle2f(
				new Point2f(36f, -34f), 16.5f, 68f)));

		// this.fieldZones = new ArrayList<Zone>(2);
		// this.fieldZones.add(new Zone(Team.LEFT_SIDE, new Rectangle2f(
		// new Point2f(-52.5f, -34f), 52.5f, 68f)));
		// this.fieldZones.add(new Zone(Team.RIGHT_SIDE, new Rectangle2f(
		// new Point2f(0f, -34f), 52.5f, 68f)));
	}

	public AttackAnalyzer() {
		this.initZones();
	}

	@Override
	public void init() {
		this.attack = new Attack(-1, Team.NEUTRAL, new Point2f(-1f, -1f));
		this.attacks = new ArrayList<Attack>(10); // 10 => estimated value
	}

	@Override
	public GameEvent analyze(Scene scene, Scene prev) {

		if (prev == null || scene == null) {
			return null;
		}

		GameEvent gevent = null;

		// we can only attack during PlayOn mode.
		if (scene.pmode.pmode != PlayMode.PM_PlayOn) {

			// we are not longer PlayOn but the team already had a valid attack,
			// store it
			gevent = this.validateAttack();

			this.attack = new Attack(-1, Team.NEUTRAL, scene.ball.pos);
		}

		int dangerousField = Team.NEUTRAL;
		// note: zones should be non-overlapping
		for (Zone z : this.lastDangerZones) {
			if (z.contains(scene.ball.pos)) {
				// if the attack is in the last quarter of the field
				// store it
				dangerousField = z.field;
				break;
			}
		}

		int opposing_team = Team.NEUTRAL;
		int my_team = Team.NEUTRAL;

		// left attacking (coordinates increase)
		if (scene.ball.pos.inFrontOf(prev.ball.pos)) {
			my_team = Team.LEFT_SIDE;
		}
		// right attacking (coordinates decrease)
		else if (scene.ball.pos.isBehind(prev.ball.pos)) {
			my_team = Team.RIGHT_SIDE;
		} else {// if the ball is stopped continue to count the attack
			// for the attacking team
			my_team = this.attack.team;
		}

		if (my_team == Team.LEFT_SIDE) {
			opposing_team = Team.RIGHT_SIDE;
		} else if (my_team == Team.RIGHT_SIDE) {
			opposing_team = Team.LEFT_SIDE;
		}

		if (this.attack.team != Team.NEUTRAL) {
			if (dangerousField == opposing_team) {
				// if (this.attack.team != Team.NEUTRAL)
				// System.out.println("IN DANGER ZONE!!!" + scene.time + "team:"
				// + Team.name(this.attack.team) + " "
				// + this.attack.startTime);
				// if this is not already a dangerous attack
				if (this.attack.dangerousStartTime < 0) {
					// make it a dangerous attack
					this.attack.dangerousStartTime = scene.time;
				}
			}
		}

		// my_team was not previously attacking.
		if (this.attack.team != my_team && this.attack.team != Team.NEUTRAL) {
			// this.attack.team may still be attacking
			if (AttackAnalyzer.mayStillBeAttacking(this.attack.team, dangerousField, scene)) {
				// since we may still be attacking don't let the rest of the
				// code terminate the attack
				return null;
			}
		}

		// my_team was not previously attacking.
		if (this.attack.team != my_team) {

			// Opposing side just finished a valid attack?
			gevent = this.validateAttack();

			// if we are starting an attack
			if (AttackAnalyzer.getKickTeam(scene.time) == my_team) {
				// we where not previously attacking but we are now.
				this.attack = new Attack(scene.time, my_team, scene.ball.pos);
			} else {
				// create an invalid attack
				this.attack = new Attack(-1, Team.NEUTRAL, scene.ball.pos);
			}
		}

		// "always" update the attack time
		this.attack.endTime = scene.time;

		return gevent;
	}

	private static boolean mayStillBeAttacking(int my_team, int field,
			Scene scene) {
		int opp_team_start = -1;
		int opp_team_end = -1;
		int opposing_team = Team.NEUTRAL;

		if (my_team == Team.LEFT_SIDE) {
			opposing_team = Team.RIGHT_SIDE;
			opp_team_start = Param.MAX_PLAYER;
			opp_team_end = Param.MAX_PLAYER * 2;
		} else if (my_team == Team.RIGHT_SIDE) {
			opposing_team = Team.LEFT_SIDE;
			opp_team_start = 0;
			opp_team_end = Param.MAX_PLAYER;
		}

		// in the danger zone we could still have an attack
		if (field == opposing_team) {
			// System.out.println("WAITING (in oppteamfield) t(" + scene.time
			// + ") team(" + my_team + ")");
			int kickteam = AttackAnalyzer.getKickTeam(scene.time);
			// if the ball is going back but was kicked by the correct
			// team...
			if (my_team == kickteam) {
				// System.out.println("WAITING (kicked by my team) t("
				// + scene.time + ") team(" + my_team + ")");

				for (int iter = opp_team_start; iter < opp_team_end; iter++) {
					Player player = scene.player[iter];
					Point2f ballpos = scene.ball.pos;
					// did the opposing team got the ball?
					if (player.isCatchable(ballpos)
							|| player.canTackle(ballpos)
							|| player.isKickable(ballpos)) {
						// System.out.println("NOT WAITING (player " +
						// player.unum
						// + " of team " + player.side
						// + " has the ball) t(" + scene.time + ") team("
						// + my_team + ")");
						// the attack is not continuing.
						return false;
					}
				}
				// TODO - adicionar a cena de ainda estarmos a disputar a
				// bola ver - 200707031200-Oxsy_31-vs-Brasil2D_0.rcg.gz por
				// volta do 4640
				// System.out.println("WAITING TO SEE (3) t(" + scene.time +
				// ")");
				// if the opponent does not have the ball
				// let's keep waiting to see...
				return true;
			}
		}

		return false;
	}

	private GameEvent validateAttack() {
		// if the attack is not valid ignore
		if (!this.attack.valid()) {
			return null;
		}

		if (this.attack.dangerous()) {
			System.out
			.println("ATTACK_FINISHED START(" + this.attack.startTime
					+ ") FINISH(" + this.attack.endTime
					+ ") dangerous(" + this.attack.dangerousStartTime
					+ ")  type(" + this.attack.attackType()
					+ ") startPos(" + this.attack.startPos + ") team("
					+ this.attack.team + ")");
		} else {
			System.out.println("ATTACK_FINISHED START(" + this.attack.startTime
					+ ") FINISH(" + this.attack.endTime + ") type("
					+ this.attack.attackType() + ") team(" + this.attack.team
					+ ")");
		}

		this.attacks.add(this.attack);
		this.countUp(this.attack.team, this.attack.endTime);

		// - generate GAMEEVENT? - probably not...
		return null;
	}

	private static int getKickTeam(int time) {
		SceneSet scene_set = WorldModel.getInstance().getSceneSet();
		for (int t = time; t > 0; t--) {
			Scene scene = scene_set.getScene(t);
			Scene prev = scene_set.getScene(t - 1);

			if (scene == null || prev == null) {
				System.err.println("UPS! cannot calculate kicker...");
				return Team.NEUTRAL;
			}

			for (int i = 0; i < Param.MAX_PLAYER * 2; i++) {
				if ((scene.player[i].isKicking() && prev.player[i]
				                                                .isKickable(prev.ball.pos))
				                                                || (scene.player[i].isCatching() && prev.player[i]
				                                                                                                .isCatchable(prev.ball.pos))
				                                                                                                || (scene.player[i].isTackling() && prev.player[i]
				                                                                                                                                                .canTackle(prev.ball.pos))) {
					// we found the culprit! check the zone and return it!
					return scene.player[i].side;
				}
			}
		}

		return Team.NEUTRAL;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void xmlElement(XMLBuilder builder) {
		XMLBuilder attacksBuilder = builder.elem("attacks").attr("left",
				String.valueOf(this.lcount)).attr("right", String.valueOf(this.rcount));
		for (Attack attack : this.attacks) {
			attack.xmlElement(attacksBuilder);
		}
	}

}
