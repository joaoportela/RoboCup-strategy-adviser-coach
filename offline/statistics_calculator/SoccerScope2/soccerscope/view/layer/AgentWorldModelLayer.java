/*
 * $Header: $
 */

package soccerscope.view.layer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import soccerscope.model.Param;
import soccerscope.model.Player;
import soccerscope.model.Scene;
import soccerscope.util.Color2;
import soccerscope.util.geom.Point2f;
import soccerscope.util.geom.Vector2f;
import soccerscope.view.FieldPane;

public class AgentWorldModelLayer extends Layer {

	public AgentWorldModelLayer(FieldPane fieldPane, boolean enable) {
		super(fieldPane, enable);
	}

	public String getLayerName() {
		return "AgentWorldModel";
	}

	private boolean dVisible = false;
	private boolean dNeck = false;
	private boolean dKickable = false;
	private boolean dUnum = false;
	private boolean dVelocity = false;
	private JCheckBox unumCB;
	private JCheckBox neckCB;
	private JCheckBox visibleCB;
	private JCheckBox kickableCB;
	private JCheckBox velocityCB;

	public JPanel getConfigPanel() {
		JPanel panel = new JPanel();

		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		unumCB = new JCheckBox("Unum");
		neckCB = new JCheckBox("Neck");
		visibleCB = new JCheckBox("Visible");
		kickableCB = new JCheckBox("Kickable");
		velocityCB = new JCheckBox("Velocity");
		AgentWorldModelLayerActionListener awmlal = new AgentWorldModelLayerActionListener();
		unumCB.addActionListener(awmlal);
		neckCB.addActionListener(awmlal);
		visibleCB.addActionListener(awmlal);
		kickableCB.addActionListener(awmlal);
		velocityCB.addActionListener(awmlal);
		panel.add(unumCB);
		panel.add(neckCB);
		panel.add(visibleCB);
		panel.add(kickableCB);
		panel.add(velocityCB);

		return panel;
	}

	class AgentWorldModelLayerActionListener implements ActionListener {
		public void actionPerformed(ActionEvent ie) {
			Object o = ie.getSource();
			boolean b = ((AbstractButton) o).isSelected();
			if (o == unumCB)
				dUnum = b;
			else if (o == neckCB)
				dNeck = b;
			else if (o == visibleCB)
				dVisible = b;
			else if (o == kickableCB)
				dKickable = b;
			else if (o == velocityCB)
				dVelocity = b;
		}
	}

	private Scene scene;
	private float ballMagnify;
	private float playerMagnify;

	public void draw(Graphics g) {
		if (!enable)
			return;

		scene = fieldPane.getAgentScene();
		ballMagnify = fieldPane.getBallMagnify();
		playerMagnify = fieldPane.getPlayerMagnify();

		if (scene == null)
			return;

		Graphics2D g2 = (Graphics2D) g;
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
				0.3f));

		if (dVisible)
			drawVisible(g2);
		if (dNeck)
			drawNeck(g2);
		if (dKickable)
			drawKickable(g2);
		if (dUnum)
			drawUnum(g2);
		drawPlayer(g2);
		drawBall(g2);
		if (dVelocity)
			drawVelocity(g2);

		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
				1.0f));
	}

	private Point2f drawPoint = new Point2f();
	private Point2f drawSize = new Point2f();
	private Point2f drawOffset = new Point2f();
	private Point2f nextPos = new Point2f();
	private Point2f accPos = new Point2f();
	private Vector2f diff = new Vector2f();

	private void drawVisible(Graphics g) {
		for (int i = 0; i < Param.MAX_PLAYER * 2; i++) {
			if (!scene.player[i].isEnable() || !fieldPane.isSelected(i))
				continue;

			float playerR = scene.player[i].getPlayerSize() * playerMagnify;
			Color drawColor = scene.player[i].getColor();

			int a = (int) (-(scene.player[i].angle + scene.player[i].angleNeck) - scene.player[i].angleVisible / 2.0);

			// ���ֹ��Ụ̃���狼��ʤ����ꥢ
			drawPoint.sub(scene.player[i].pos, pitchSize2);
			g
					.setColor(Color2.mix(drawColor,
							Color2.forestGreen.darker(), 1, 2));
			fillArc(g, drawPoint, pitchSize4, a, scene.player[i].angleVisible);

			// Ụ̃��狼��ʤ��ʤäƤ��륨�ꥢ
			drawPoint.sub(scene.player[i].pos, teamTooFarSize);
			g.setColor(Color2.mix(drawColor, Color2.forestGreen.darker(), 1,
					1.5));
			fillArc(g, drawPoint, teamTooFarSize2, a,
					scene.player[i].angleVisible);

			// ���ֹ椬�狼��ʤ��ʤäƤ��륨�ꥢ
			drawPoint.sub(scene.player[i].pos, unumTooFarSize);
			g
					.setColor(Color2.mix(drawColor,
							Color2.forestGreen.darker(), 1, 1));
			fillArc(g, drawPoint, unumTooFarSize2, a,
					scene.player[i].angleVisible);

			// visible distance
			drawPoint.sub(scene.player[i].pos, visibleSize);
			fillOval(g, drawPoint, visibleSize2);
		}
	}

	private void drawNeck(Graphics g) {
		for (int i = 0; i < Param.MAX_PLAYER * 2; i++) {
			if (!scene.player[i].isEnable())
				continue;

			float playerR = scene.player[i].getPlayerSize() * playerMagnify;
			Color drawColor = scene.player[i].getColor();

			// draw neck angle
			g.setColor(Color2.mix(drawColor, Color2.forestGreen, 1, 3));
			int a = (int) (-((int) scene.player[i].angle + scene.player[i].angleNeck) - scene.player[i].angleVisible / 2.0);

			drawPoint.sub(scene.player[i].pos, visibleSize);
			fillArc(g, drawPoint, visibleSize2, a, scene.player[i].angleVisible);
			g.setColor(Color2.mix(drawColor, Color2.forestGreen, 1, 1));
			drawOval(g, drawPoint, visibleSize2);
		}
	}

	private void drawKickable(Graphics g) {
		for (int i = 0; i < Param.MAX_PLAYER * 2; i++) {
			if (!scene.player[i].isEnable())
				continue;

			float playerR = scene.player[i].getPlayerSize() * playerMagnify;
			Color drawColor = scene.player[i].getColor();

			// draw kickable and catchable area
			float drawR;
			drawR = Param.KICKABLE_R;
			if (scene.player[i].isKicking()) {
				if (scene.player[i].isKickFault())
					g.setColor(drawColor.darker().darker());
				else
					g.setColor(drawColor.darker());
				fillCircle(g, scene.player[i].pos, drawR);
			}
			g.setColor(drawColor);
			drawCircle(g, scene.player[i].pos, drawR);

			if (scene.player[i].isGoalie()) {
				drawR = Param.GOALIE_CATCHABLE_AREA_LENGTH;
				if (scene.player[i].isCatching()) {
					if (scene.player[i].isCatchFault())
						g.setColor(drawColor.darker().darker());
					else
						g.setColor(drawColor.darker());
					fillCircle(g, scene.player[i].pos, drawR);
				}
				g.setColor(drawColor);
				drawCircle(g, scene.player[i].pos, drawR);
			}

			if (scene.player[i].isTackling()) {
				if (scene.player[i].isTackleFault())
					g.setColor(drawColor.darker().darker());
				else
					g.setColor(drawColor.darker());
				fillRect(g, new Point2f(scene.player[i].pos.x,
						scene.player[i].pos.y - Param.TACKLE_WIDTH / 2),
						new Point2f(Param.TACKLE_DIST, Param.TACKLE_WIDTH),
						(int) scene.player[i].angle, scene.player[i].pos);
			}

			g.setColor(drawColor);
			drawRect(g, new Point2f(scene.player[i].pos.x,
					scene.player[i].pos.y - Param.TACKLE_WIDTH / 2),
					new Point2f(Param.TACKLE_DIST, Param.TACKLE_WIDTH),
					(int) scene.player[i].angle, scene.player[i].pos);

			if (scene.player[i].isKickable(scene.ball.pos)) {
				Vector2f acc = scene.player[i].getKickAccelerate(
						scene.ball.pos, 100.0f, 0.0f);
				float kickableRange = acc.length();
				nextPos.add(scene.ball.pos, scene.ball.vel);
				drawCircle(g, nextPos, kickableRange);
			}

			diff.sub(scene.ball.pos, scene.player[i].pos);
			Vector2f tmp = diff.rotate(-scene.player[i].angle);

			if (tmp.x > 0.0f && tmp.x <= Param.TACKLE_DIST
					&& Math.abs(tmp.y) <= Param.TACKLE_WIDTH / 2.0f) {

				// fail_prob = ( player_2_ball.x / tackle_dist )^6
				// + ( |player_2_ball.y| / 1.25 )^6;

				float prob = (float) (1.0 - (Math.pow(
						tmp.x / Param.TACKLE_DIST, Param.TACKLE_EXPONENT) + Math
						.pow(Math.abs(tmp.y) / (Param.TACKLE_WIDTH / 2.0f),
								Param.TACKLE_EXPONENT)));
				Point2f drawPoint = new Point2f();
				Point2f drawOffset = new Point2f(0.0f, playerR * 2.0f);
				drawPoint.add(scene.player[i].pos, drawOffset);
				drawString(g, String.valueOf(prob), drawPoint);
			}
		}
	}

	private void drawUnum(Graphics g) {
		for (int i = 0; i < Param.MAX_PLAYER * 2; i++) {
			if (!scene.player[i].isEnable())
				continue;

			float playerR = scene.player[i].getPlayerSize() * playerMagnify;

			// draw unum
			g.setColor(Color.white);
			drawOffset.set(0, -playerR);
			drawPoint.add(scene.player[i].pos, drawOffset);
			drawString(g, Integer.toString(scene.player[i].unum), drawPoint);
		}
	}

	private void drawPlayer(Graphics g) {
		for (int i = 0; i < Param.MAX_PLAYER * 2; i++) {
			if (!scene.player[i].isEnable())
				continue;

			float playerR = scene.player[i].getPlayerSize() * playerMagnify;
			Color drawColor = scene.player[i].getColor();

			// draw mode
			g.setColor(Color.black);

			if (scene.player[i].isCollision()) {
				drawCircle(g, scene.player[i].pos, playerR + 0.1f);
			}

			if (scene.player[i].isKicking() || scene.player[i].isCatching()) {
				g.setColor(drawColor.darker());
			} else if (scene.player[i].isKickFault()
					|| scene.player[i].isCatchFault()) {
				g.setColor(drawColor.darker().darker());
			}
			fillCircle(g, scene.player[i].pos, playerR);

			if (scene.player[i].isGoalie()
					&& scene.player[i].catchDir != Player.ERROR_DIR) {
				g.setColor(drawColor.brighter());
				drawRect(g, new Point2f(scene.player[i].pos.x,
						scene.player[i].pos.y
								- Param.GOALIE_CATCHABLE_AREA_WIDTH / 2),
						new Point2f(Param.GOALIE_CATCHABLE_AREA_LENGTH,
								Param.GOALIE_CATCHABLE_AREA_WIDTH),
						(int) scene.player[i].angle + scene.player[i].catchDir,
						scene.player[i].pos);
			}

			// draw body angle
			g.setColor(drawColor);
			drawSize.set(playerR, playerR);
			drawPoint.sub(scene.player[i].pos, drawSize);
			drawSize.scale(2);
			fillArc(g, drawPoint, drawSize,
					-((int) scene.player[i].angle + 90), 180);

			// draw body
			g.setColor(drawColor);
			drawCircle(g, scene.player[i].pos, playerR);
		}
	}

	private void drawBall(Graphics g) {
		if (!scene.ball.isEnable())
			return;

		float ballR = Param.BALL_SIZE * ballMagnify;
		g.setColor(scene.ball.getColor());
		fillCircle(g, scene.ball.pos, ballR);
	}

	private void drawVelocity(Graphics g) {
		// draw ball velocity
		if (scene.ball.isEnable()) {

			float ballR = Param.BALL_SIZE * ballMagnify;

			g.setColor(Color.red);
			nextPos.add(scene.ball.pos, scene.ball.vel);
			drawLine(g, scene.ball.pos, nextPos);
			drawCircle(g, nextPos, ballR);
			if (scene.ball.acc.x != 0.0f || scene.ball.acc.y != 0.0f) {
				g.setColor(Color.white);
				accPos.add(nextPos, scene.ball.acc);
				drawLine(g, nextPos, accPos);
				drawCircle(g, accPos, ballR);
			}
		}

		// draw player velocity
		for (int i = 0; i < Param.MAX_PLAYER * 2; i++) {
			if (!scene.player[i].isEnable())
				continue;

			float playerR = scene.player[i].getPlayerSize() * playerMagnify;
			Color drawColor = scene.player[i].getColor();

			g.setColor(Color.red);
			nextPos.add(scene.player[i].pos, scene.player[i].vel);
			drawLine(g, scene.player[i].pos, nextPos);
			drawCircle(g, nextPos, playerR);
			if (scene.player[i].acc.x != 0.0f || scene.player[i].acc.y != 0.0f) {
				g.setColor(Color.white);
				accPos.add(nextPos, scene.player[i].acc);
				drawLine(g, nextPos, accPos);
				drawCircle(g, accPos, playerR);
			}
		}
	}
}
