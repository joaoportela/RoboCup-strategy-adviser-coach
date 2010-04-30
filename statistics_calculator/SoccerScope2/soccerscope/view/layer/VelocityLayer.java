/*
 * $Header: $
 */

package soccerscope.view.layer;

import java.awt.Color;
import java.awt.Graphics;

import soccerscope.model.Param;
import soccerscope.model.Scene;
import soccerscope.util.geom.Point2f;
import soccerscope.view.FieldPane;

public class VelocityLayer extends Layer {

	public VelocityLayer(FieldPane fieldPane, boolean enable) {
		super(fieldPane, enable);
	}

	public String getLayerName() {
		return "Velocity";
	}

	private Point2f nextPos = new Point2f();
	private Point2f accPos = new Point2f();

	public void draw(Graphics g) {
		if (!enable)
			return;

		Scene scene = fieldPane.getScene();
		float ballMagnify = fieldPane.getBallMagnify();
		float playerMagnify = fieldPane.getPlayerMagnify();
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
