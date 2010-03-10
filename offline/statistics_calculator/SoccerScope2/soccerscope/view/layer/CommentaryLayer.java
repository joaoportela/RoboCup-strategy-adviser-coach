/*
 * $Log: CommentaryLayer.java,v $
 * Revision 1.5  2002/10/04 10:39:23  koji
 * Tuple,Point,Vector�Υѥå������ѹ���Geometry,Circle,Line,Rectangle�ɲ�
 *
 * Revision 1.4  2002/09/12 15:28:31  koji
 * ������������?���ɲ�,ColorTool��Color2�����
 *
 * Revision 1.3  2002/09/05 10:21:25  taku-ako
 * �إƥ�ץ쥤�䡼���б�
 *
 * Revision 1.2  2002/09/02 07:06:27  taku-ako
 * Geometry��Ϣ�Υ饤�֥������
 *
 * Revision 1.1.1.1  2002/03/01 14:12:53  koji
 * CVS�����
 *
 */

package soccerscope.view.layer;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import soccerscope.model.Param;
import soccerscope.model.PlayMode;
import soccerscope.model.Player;
import soccerscope.model.Scene;
import soccerscope.util.geom.Point2f;
import soccerscope.util.geom.Vector2f;
import soccerscope.view.FieldPane;

public class CommentaryLayer extends Layer {

	public CommentaryLayer(FieldPane fieldPane, boolean enable) {
		super(fieldPane, enable);
		ic = new IsCentering();
		kickerlist = new PlayerList();
	}

	public String getLayerName() {
		return "Commentary";
	}

	// Commentary ��
	private int preLastKickerId = -1;
	private int lastKickerId = -1;
	private Scene lastKickScene;
	private PlayerList kickerlist;
	private IsCentering ic;

	public void draw(Graphics g) {
		if (!enable)
			return;

		Scene scene = fieldPane.getScene();
		float ballMagnify = fieldPane.getBallMagnify();
		float playerMagnify = fieldPane.getPlayerMagnify();

		float playerR = Param.PLAYER_SIZE * playerMagnify;
		for (int i = 0; i < Param.MAX_PLAYER * 2; i++) {
			playerR = scene.player[i].getPlayerSize() * playerMagnify;
			if (!scene.player[i].isEnable())
				continue;

			if (scene.player[i].isKicking()) {
				lastKickerId = i;
				kickerlist.add(scene.player[i]);
				lastKickScene = scene;
			}
		}

		if ((scene.pmode.pmode == PlayMode.PM_PlayOn) && (lastKickerId > -1)) {

			// centering
			if (ic.isCentering(scene.player[lastKickerId].pos, scene)) {
				playerR = scene.player[lastKickerId].getPlayerSize()
						* playerMagnify;
				Point2f drawPoint = new Point2f(scene.player[lastKickerId].pos);
				drawPoint.add(new Point2f(playerR, playerR));
				g.setFont(boldFont);
				g.setColor(Color.cyan);
				drawString(g, "Centering", drawPoint);
			}
			Vector2f playerangle = new Vector2f((float) Math
					.cos((Math.PI / 180)
							* (lastKickScene.player[lastKickerId].angle)),
					(float) Math.sin((Math.PI / 180)
							* (lastKickScene.player[lastKickerId].angle)));
			if (lastKickScene.ball.vel.length() < 0.2f) {
				playerangle = new Vector2f(scene.ball.vel);
				// System.out.println("Henkokiu");
			}

			if ((lastKickScene != null)
					&& kickerlist.isDribble()
					&& (scene.player[lastKickerId].vel.length() > 0.01f)
					&& ((scene.player[lastKickerId].pos)
							.distance(scene.ball.pos) > .3f)
					&& (scene.ball.vel.angle(playerangle) < 1.0f)) {
				playerR = scene.player[lastKickerId].getPlayerSize()
						* playerMagnify;
				Point2f drawPoint = new Point2f(scene.player[lastKickerId].pos);
				drawPoint.add(new Point2f(playerR, playerR));
				g.setFont(boldFont);
				g.setColor(scene.player[lastKickerId].getColor());

				if (lastKickScene.ball.vel.length() < 1.5f) {
					drawString(g, "Dribble", drawPoint);
				} else {
					// drawString(g, "Pass", drawPoint);
				}
			}
			// System.out.println("player: " +
			// scene.player[lastKickerId].vel.length());
			// System.out.println("ball.vel.length:   " +
			// scene.ball.vel.length());
			// System.out.println("player distance ball: " +
			// (scene.player[lastKickerId].pos).distance(scene.ball.pos));
			// System.out.println("ball.vel>>>>>>" + scene.ball.vel);
			// System.out.println("player.vel>>>>>>" +
			// scene.player[lastKickerId].vel.length());
			// System.out.println("ballvel.angle(player.vel)>>>>>>" +
			// scene.ball.vel.angle(playerangle));
			// System.out.println("");

			// System.out.println(scene.player[lastKickerId].pos.distance(scene.ball.pos));
			// drawRectByPoint(g, scene.player[lastKickerId].pos,
			// scene.ball.pos);
		}
		g.setFont(plainFont);
	}

	private final static Font boldFont = new Font("", Font.BOLD, 16);
	private final static Font plainFont = new Font("", Font.PLAIN, 12);

	private final static Point2f centeringAreaSize = new Point2f(
			Param.PENALTY_AREA_LENGTH + 5f,
			((Param.PITCH_WIDTH - Param.PENALTY_AREA_WIDTH) / 2));
	private final static Point2f centeringGoalAreaSize = new Point2f(
			Param.PENALTY_AREA_LENGTH + 5f, (goalAreaSize.y));

	private final static Point2f centeringAreaCornerNW = new Point2f(
			topLeftCorner);
	private final static Point2f centeringAreaCornerSW = new Point2f(
			bottomLeftCorner.x, bottomLeftCorner.y - centeringAreaSize.y);
	private final static Point2f centeringAreaCornerNE = new Point2f(
			topRightCorner.x - centeringAreaSize.x, topRightCorner.y);
	private final static Point2f centeringAreaCornerSE = new Point2f(
			bottomRightCorner.x - centeringAreaSize.x, bottomRightCorner.y
					- centeringAreaSize.y);
	private final static Point2f centeringAreaCornerGL = new Point2f(
			leftGoalArea);
	private final static Point2f centeringAreaCornerGR = new Point2f(
			topRightCorner.x - centeringGoalAreaSize.x, rightGoalArea.y);

	private final CenteringArea centeringAreaNW = new CenteringArea(
			centeringAreaCornerNW, centeringAreaSize);
	private final CenteringArea centeringAreaSW = new CenteringArea(
			centeringAreaCornerSW, centeringAreaSize);
	private final CenteringArea centeringAreaNE = new CenteringArea(
			centeringAreaCornerNE, centeringAreaSize);
	private final CenteringArea centeringAreaSE = new CenteringArea(
			centeringAreaCornerSE, centeringAreaSize);
	private final CenteringArea centeringAreaGL = new CenteringArea(
			centeringAreaCornerGL, centeringGoalAreaSize);
	private final CenteringArea centeringAreaGR = new CenteringArea(
			centeringAreaCornerGR, centeringGoalAreaSize);

	class PlayerList {
		List playerlist = new ArrayList();
		static final int size = 3;

		public void add(Player player) {
			playerlist.add(player);
			if (playerlist.size() == size + 1) {
				playerlist.remove(0);
			}
		}

		public boolean isDribble() {
			// System.out.println("LIST>>>>>>>>>" + playerlist.toString());
			if (playerlist.size() < size) {
				return false;
			} else {
				Player[] player = new Player[size];
				for (int i = 0; i < size; i++) {
					player[i] = (Player) (playerlist.get(i));
				}
				boolean isSameUnum = ((player[0].side == player[1].side)
						&& (player[0].unum == player[1].unum)
						&& (player[0].side == player[2].side) && (player[0].unum == player[2].unum));
				boolean isSamePosition = ((player[0].pos
						.distance(player[1].pos) < 0.1f) && (player[1].pos
						.distance(player[2].pos) < 0.1f));

				return (isSameUnum && !isSamePosition);
			}
		}
	}

	class IsCentering {
		public IsCentering() {
		}

		public boolean isCentering(Point2f p, Scene scene) {
			Scene tscene = scene;
			Point2f p1 = new Point2f(tscene.ball.pos);
			Vector2f vel = new Vector2f(tscene.ball.vel);
			for (int i = 0; i < 15; i++) {
				vel.scale(Param.BALL_DECAY);
				p1.add(vel);
			}

			return ((centeringAreaNW.contains(p) || centeringAreaSW.contains(p)
					|| centeringAreaNE.contains(p) || centeringAreaSE
					.contains(p)) && (centeringAreaGR.contains(p1) || centeringAreaGL
					.contains(p1)));
		}
	}

	class CenteringArea {
		Point2f topleft, size;

		public CenteringArea(Point2f topleft, Point2f size) {
			this.topleft = topleft;
			this.size = size;
		}

		boolean contains(Point2f p) {
			return ((p.x > topleft.x) && (p.x < (topleft.x + size.x))
					&& (p.y > topleft.y) && (p.y < (topleft.y + size.y)));
		}

		void draw(Graphics g) {
			drawRect(g, topleft, size);
			/*
			 * Point2f p = fieldToScreen(topleft); Point2f s = new
			 * Point2f(size); s.scale(magnify); g.drawRect((int)p.x, (int)p.y,
			 * (int)s.x, (int)s.y);
			 */
		}
	}

	private void drawRectByPoint(Graphics g, Point2f playerPos, Point2f ballPos) {
		Point2f p1 = fieldPane.fieldToScreen(playerPos);
		Point2f p2 = fieldPane.fieldToScreen(ballPos);

		g
				.drawRect((int) floatWhichMin(p1.x, p2.x), (int) floatWhichMin(
						p1.y, p2.y), (int) Math.abs(p1.x - p2.x), (int) Math
						.abs(p1.y - p2.y));
	}

	private float floatWhichMin(float x, float y) {
		if (x <= y) {
			return x;
		} else {
			return y;
		}
	}
}
