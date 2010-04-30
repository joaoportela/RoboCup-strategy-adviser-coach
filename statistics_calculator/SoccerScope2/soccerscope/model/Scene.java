/*
 * $Header: $
 */

package soccerscope.model;

import java.util.Iterator;
import java.util.Vector;

import soccerscope.util.Time;

public class Scene implements Comparable<Scene> {
	public int time;
	public PlayMode pmode;
	public Ball ball;
	public Player[] player;
	public Team left;
	public Team right;

	public Scene() {
		time = Time.MAX_TIME;
		pmode = new PlayMode();
		ball = new Ball();
		player = new Player[Param.MAX_PLAYER * 2];
		for (int i = 0; i < Param.MAX_PLAYER * 2; i++)
			player[i] = new Player();
		left = new Team(Team.LEFT_SIDE);
		right = new Team(Team.RIGHT_SIDE);
	}

	public static Scene createScene() {
		Scene testScene = new Scene();
		testScene.time = Time.MAX_TIME;
		testScene.pmode.pmode = PlayMode.PM_BeforeKickOff;
		testScene.ball.pos.set(0, 0);
		testScene.ball.mode = Ball.STAND;
		testScene.left.name = "TeamLeft";
		testScene.left.score = 0;
		testScene.right.name = "TeamRight";
		testScene.right.score = 0;
		for (int i = 0; i < Param.MAX_PLAYER; i++) {
			testScene.player[i].mode = Player.STAND;
			testScene.player[i].side = Team.LEFT_SIDE;
			testScene.player[i].unum = i + 1;
			testScene.player[i].stamina = 4000;
			testScene.player[i].pos.set(-Param.PITCH_MARGIN * 2 / 3 * (i + 1),
					-(Param.PITCH_HALF_WIDTH + Param.PITCH_MARGIN / 2));
		}
		for (int i = Param.MAX_PLAYER; i < Param.MAX_PLAYER * 2; i++) {
			testScene.player[i].mode = Player.STAND;
			testScene.player[i].side = Team.RIGHT_SIDE;
			testScene.player[i].unum = i - Param.MAX_PLAYER + 1;
			testScene.player[i].stamina = 4000;
			testScene.player[i].pos.set(Param.PITCH_MARGIN * 2 / 3
					* (i - Param.MAX_PLAYER + 1),
					-(Param.PITCH_HALF_WIDTH + Param.PITCH_MARGIN / 2));
		}
		testScene.player[0].mode = Player.STAND | Player.GOALIE;
		testScene.player[11].mode = Player.STAND | Player.GOALIE;

		return testScene;
	}

	public Iterator<Player> iterator() {
		Vector<Player> v = new Vector<Player>();
		for (int i = 0; i < Param.MAX_PLAYER * 2; i++)
			v.add(player[i]);
		return v.iterator();
	}

	public boolean isGoalieCatching() {
		for (int i = 0; i < Param.MAX_PLAYER * 2; i++)
			if (player[i].isCatching())
				return true;
		return false;
	}

	public int compareTo(Scene obj) {
		return this.time - obj.time;
	}
}
