/*
 * $Header: $
 */

package soccerscope.view.layer;

import java.awt.Color;
import java.awt.Graphics;

import soccerscope.model.Param;
import soccerscope.model.Scene;
import soccerscope.util.geom.Point2f;
import soccerscope.util.geom.Vector2f;
import soccerscope.view.FieldPane;

public class NoNoiseLayer extends Layer {

	public NoNoiseLayer(FieldPane fieldPane, boolean enable) {
		super(fieldPane, enable);
		drawPoint = new Point2f();
		vel = new Vector2f();
	}

	public String getLayerName() {
		return "No Noise";
	}

	private int limit = 10;
	private Point2f drawPoint;
	private Vector2f vel;

	public void draw(Graphics g) {
		if (!enable)
			return;

		Scene scene = fieldPane.getScene();
		float ballMagnify = fieldPane.getBallMagnify();
		float playerMagnify = fieldPane.getPlayerMagnify();

		float ballR = Param.BALL_SIZE * ballMagnify;
		// float playerR = Param.PLAYER_SIZE * playerMagnify;

		g.setColor(Color.blue);
		vel.set(scene.ball.vel);
		drawPoint.add(scene.ball.pos, vel);
		for (int offset = limit; offset > 0; offset--) {
			drawCircle(g, drawPoint, ballR);
			vel.scale(Param.BALL_DECAY);
			drawPoint.add(vel);
		}
		drawCircle(g, drawPoint, ballR);
		drawLine(g, scene.ball.pos, drawPoint);
	}
}
