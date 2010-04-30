/*
 * $Header: $
 */

package soccerscope.model;

import soccerscope.util.geom.Geometry;
import soccerscope.util.geom.Line2f;
import soccerscope.util.geom.Point2f;
import soccerscope.util.geom.Vector2f;

public class Player extends SoccerObject {
	// player mode
	public final static int DISABLE = 0x0000;
	public final static int STAND = 0x0001;
	public final static int KICK = 0x0002;
	public final static int KICK_FAULT = 0x0004;
	public final static int GOALIE = 0x0008;
	public final static int CATCH = 0x0010;
	public final static int CATCH_FAULT = 0x0020;
	public final static int BALL_TO_PLAYER = 0x0040;
	public final static int PLAYER_TO_BALL = 0x0080;
	public final static int DISCARD = 0x0100;
	public final static int LOST = 0x0200;
	public final static int BALL_COLLIDE = 0x0400;
	public final static int PLAYER_COLLIDE = 0x0800;
	public final static int TACKLE = 0x1000;
	public final static int TACKLE_FAULT = 0x2000;
	public final static int BACK_PASS = 0x4000;
	public final static int FREE_KICK_FAULT = 0x8000;

	// player type
	public final static int PT_NULL = 0;
	public final static int PT_DEFAULT = 0;

	// view quality
	public final static int LOW = 0;
	public final static int HIGH = 1;

	// error direction
	public final static int ERROR_DIR = 1024;

	public int side;
	public int unum;
	public int stamina;
	public float angle; // degree
	public int angleNeck; // degree
	public int angleVisible; // degree
	public int viewQuality;
	public String sayMessage;
	public float catchDir;
	public boolean offside;
	public int mode;
	public int type; // player type
	/*
	 * public double effort; public double recovery;
	 */
	public int kickCount;
	public int dashCount;
	public int turnCount;

	/*
	 * public int sayCount; public int turnNeckCount; public int catchCount;
	 * public int moveCount; public int changeViewCount;
	 */

	public Player() {
		super();
		side = Team.NEUTRAL;
		unum = 0;
		stamina = 0;
		angle = 0;
		angleNeck = 0;
		angleVisible = 90;
		viewQuality = HIGH;
		catchDir = ERROR_DIR;
		offside = false;
		mode = DISABLE;
		type = PT_DEFAULT;
		/*
		 * effort = 1.0; recovery = 1.0; kickCount = 0; dashCount = 0; sayCount
		 * = 0; turnNeckCount = 0; catchCount = 0; changeViewCount = 0;
		 */
	}

	public void setPlayer(Player p) {
		pos.set(p.pos);
		vel.set(p.vel);
		acc.set(p.acc);
		side = p.side;
		unum = p.unum;
		stamina = p.stamina;
		angle = p.angle;
		angleNeck = p.angleNeck;
		angleVisible = p.angleVisible;
		viewQuality = p.viewQuality;
		catchDir = p.catchDir;
		offside = p.offside;
		mode = p.mode;
		type = p.type;
	}

	public String typeStr() {
		if(this.type == PT_DEFAULT)
			return "PT_DEFAULT";
		if(this.type > PT_DEFAULT)
			return "PT_HETRO_" + this.type;
		return "UNKNOWN";
	}

	public String viewStr() {
		if(this.viewQuality == LOW) 
			return "LOW";
		if(this.viewQuality == HIGH)
			return "HIGH"; 
		return "UNKOWN";
	}

	public boolean isEnable() {
		return ((mode & STAND) == STAND);
	}

	public boolean isGoalie() {
		return ((mode & GOALIE) == GOALIE);
	}

	public boolean isKicking() {
		return ((mode & KICK) == KICK);
	}

	public boolean isKickFault() {
		return ((mode & KICK_FAULT) == KICK_FAULT);
	}

	public boolean isCatching() {
		return ((mode & CATCH) == CATCH);
	}

	public boolean isCatchFault() {
		return ((mode & CATCH_FAULT) == CATCH_FAULT);
	}

	public boolean isCollision() {
		return ((mode & BALL_COLLIDE) == BALL_COLLIDE)
				|| ((mode & PLAYER_COLLIDE) == PLAYER_COLLIDE);
	}

	public boolean isTackling() {
		return ((mode & TACKLE) == TACKLE);
	}

	public boolean isTackleFault() {
		return ((mode & TACKLE_FAULT) == TACKLE_FAULT);
	}

	public float getPlayerSize() {
		return HeteroParam.get(type).player_size;
	}

	public java.awt.Color getColor() {
		switch (side) {
		case Team.LEFT_SIDE:
			if (isGoalie())
				return ColorDB.getColor("goalie_l_color");
			else
				return ColorDB.getColor("team_l_color");
		case Team.NEUTRAL:
			if (isGoalie())
				return ColorDB.getColor("nuetral_goalie");
			else
				return ColorDB.getColor("nuetral_player");
		case Team.RIGHT_SIDE:
			if (isGoalie())
				return ColorDB.getColor("goalie_r_color");
			else
				return ColorDB.getColor("team_r_color");
		}
		return java.awt.Color.white;
	}

	public boolean isOffside(float line) {
		return side == Team.LEFT_SIDE && pos.x > line
				|| side == Team.RIGHT_SIDE && pos.x < line;
	}

	public boolean isKickable(Point2f ball) {
		return pos.distance(ball) <= (Param.BALL_SIZE + getPlayerSize() + HeteroParam
				.get(type).kickable_margin);
	}

	public boolean isCatchable(Point2f ball) {
		return pos.distance(ball) <= Param.GOALIE_CATCHABLE_AREA_LENGTH;
	}

	public boolean canTackle(Point2f ball) {
		Vector2f rb = toRelative(ball);
		return (rb.x > 0 && rb.x <= Param.TACKLE_DIST && Math.abs(rb.y) <= Param.TACKLE_WIDTH / 2);
	}

	public void step() {
		pos.add(vel);
		pos.add(acc);
		vel.add(acc);
		vel.scale(HeteroParam.get(type).player_decay);
		acc.set(0, 0);
	}

	public float getKickable() {
		return (Param.BALL_SIZE + getPlayerSize() + HeteroParam.get(type).kickable_margin);
	}

	public float getMoveMax() {
		return (1 + Param.PLAYER_RAND) * HeteroParam.get(type).player_speed_max;
	}

	public float getTurnAngle() {
		return 180 / (1 + HeteroParam.get(type).inertia_moment * vel.r());
	}

	public float getDashAccelerationMax() {
		return HeteroParam.get(type).dash_power_rate
				* HeteroParam.get(type).effort_max * Param.POWER_MAX;
	}

	public Vector2f getVector() {
		return Vector2f.polar2vector(getPlayerSize(), angle);
	}

	public Line2f getLine() {
		Point2f t = new Point2f(pos);
		t.add(getVector());
		return new Line2f(pos, t);
	}

	public Vector2f toRelative(Point2f p) {
		return new Vector2f(pos, p).rotate(-angle);
	}

	public float getAngleFromBody(Point2f p) {
		return Geometry.normalizeAngle(pos.dir(p) - angle);
	}

	public float getAngleFromNeck(Point2f p) {
		return Geometry.normalizeAngle(pos.dir(p) - angle - angleNeck);
	}

	public Vector2f getDashAccelerate(float power) {
		// effort == 1 �Ȥ��Ʒ׻�
		return Vector2f.polar2vector(HeteroParam.get(type).dash_power_rate
				* power, angle);
	}

	public Vector2f getKickAccelerate(Point2f ball, float power, float dir) {
		Vector2f diff = new Vector2f();
		diff.sub(ball, pos);
		float effectiveKickPowerRate = Param.KICK_POWER_RATE;
		effectiveKickPowerRate *= 1.0
				- (diff.length() - Param.BALL_SIZE - getPlayerSize())
				/ HeteroParam.get(type).kickable_margin * 0.25
				- Math.abs(Geometry.normalizeAngle(diff.th() - angle)) / 180.0
				* 0.25;
		return Vector2f.polar2vector(effectiveKickPowerRate * power, Geometry
				.normalizeAngle(angle + dir));
	}

	public Vector2f getFinalMove() {
		return Vector2f.polar2vector(calcFinalDistance(), vel.th());
	}

	public float getTackleProb(Point2f p) {
		Vector2f tmp = toRelative(p);
		float prob = (float) (1.0 - (Math.pow(tmp.x / Param.TACKLE_DIST,
				Param.TACKLE_EXPONENT) + Math.pow(Math.abs(tmp.y)
				/ (Param.TACKLE_WIDTH / 2.0f), Param.TACKLE_EXPONENT)));
		return prob;
	}

	// ����t���®�� (a == 0)(t >= 0)
	public float calcVelocityAfterNTime(int t) {
		float v = vel.r();
		float d = HeteroParam.get(type).player_decay;
		return v * (float) Math.pow(d, t);
	}

	// ����t��ΰ�ư��Υ (a == 0)(t >= 0)
	public float calcDistanceAfterNTime(int t) {
		float v = vel.r();
		float d = HeteroParam.get(type).player_decay;
		return v * (1 - (float) Math.pow(d, t)) / (1 - d);
	}

	// ����t���®�� (a != 0)(t >= 0)
	public float calcVelocityAfterNTime(float a, int t) {
		float d = HeteroParam.get(type).player_decay;
		return calcVelocityAfterNTime(t) + a
				* ((1 - (float) Math.pow(d, t + 1)) / (1 - d) - 1);
	}

	// ����t��ΰ�ư��Υ (a != 0)(t >= 0)
	public float calcDistanceAfterNTime(float a, int t) {
		float d = HeteroParam.get(type).player_decay;
		return calcDistanceAfterNTime(t) + (a / (1 - d))
				* (t + 1 - (1 - (float) Math.pow(d, t + 1)) / (1 - d));
	}

	// �ǽ�®�� (t -> ��)(a != 0)
	public float calcFinalVelocity(float a) {
		float d = HeteroParam.get(type).player_decay;
		return a * d / (1 - d);
	}

	// �ǽ���ư��Υ (t -> ��)(a == 0)
	public float calcFinalDistance() {
		float v = vel.r();
		float d = HeteroParam.get(type).player_decay;
		return v / (1 - d);
	}

	// ®��vt��ã����Τˤ�������� (a == 0)(vt < v)
	public int calcTimeByVelocity(float vt) {
		float v = vel.r();
		float d = HeteroParam.get(type).player_decay;
		return (int) Math.ceil(((float) Math.log(vt) / (float) Math.log(d))
				- ((float) Math.log(v) / (float) Math.log(d)));
	}

	// ��Υdist��ư����Τˤ�������� (a == 0)(dist >= 0)
	public int calcTimeByDistance(float dist) {
		float v = vel.r();
		float d = HeteroParam.get(type).player_decay;
		return (int) Math.ceil(((float) Math.log(1 - dist * (1 - d) / v))
				/ ((float) Math.log(d)));
	}

	// ®��vt��ã����Τˤ�������� (a != 0)(max(a, v) < vt <= speed_max)
	public int calcTimeByVelocity(float vt, float a) {
		float v = vel.r();
		float d = HeteroParam.get(type).player_decay;
		return (int) Math.ceil(((float) Math.log((vt - a / (1 - d) + a)
				/ (v / d - a / (1 - d))) / (float) Math.log(d)) - 1);
	}

	// ��Υdist��ư����Τˤ�������� (a != 0)(dist >= 0)
	public int calcTimeByDistance(float dist, float a) {
		int time = (int) dist;
		float tmp_dist = calcDistanceAfterNTime(a, time);
		if (tmp_dist == dist)
			return time;
		else if (tmp_dist < dist) {
			while (tmp_dist < dist) {
				time++;
				tmp_dist = calcDistanceAfterNTime(a, time);
			}
		} else {
			while (tmp_dist > dist) {
				time--;
				tmp_dist = calcDistanceAfterNTime(a, time);
			}
			time++;
		}
		return time;
	}

	// ��Υdist��ư����Τ˺���¤����������� (a != 0)(dist >= 0)
	// =�ȥåץ��ԡ��ɤξ��֤����ϥ��å��夷�Ƥ��������
	public int estimateMinTimeByDistance(float dist, float a) {
		float d = HeteroParam.get(type).player_decay;
		return (int) Math.ceil(dist / (a + (a * d) / (1 - d)));
	}

	public static void main(String arg[]) {
		Player p = new Player();
		float acc = p.getDashAccelerationMax();

		long time, diff;

		time = System.currentTimeMillis();
		for (int i = 0; i < 22; i++)
			for (float d = 0.0f; d < 100; d += 0.5f) {
				p.calcTimeByDistance(d, acc);
			}
		diff = System.currentTimeMillis();
		System.out.println("normal: " + (diff - time));

		time = diff;
		float table[] = new float[120];
		for (int i = 0; i < 120; i++)
			table[i] = p.calcDistanceAfterNTime(acc, i);
		diff = System.currentTimeMillis();
		System.out.println("make: " + (diff - time));

		time = diff;
		for (int i = 0; i < 22; i++)
			for (float d = 0.0f; d < 100; d += 0.5f) {
				p.search(table, d);
			}
		diff = System.currentTimeMillis();
		System.out.println("cache: " + (diff - time));
	}

	public int search(float[] t, float key) {
		int idx = (int) key;
		float tmp = t[idx];
		if (tmp == key)
			return idx;
		else if (tmp < key) {
			while (tmp < key) {
				idx++;
				tmp = t[idx];
			}
		} else {
			while (tmp > key) {
				idx--;
				tmp = t[idx];
			}
			idx++;
		}
		return idx;
	}
}

