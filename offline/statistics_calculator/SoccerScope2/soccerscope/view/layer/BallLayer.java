/*
 * $Header: $
 */

package soccerscope.view.layer;

import java.awt.Graphics;

import soccerscope.model.Param;
import soccerscope.model.Scene;
import soccerscope.model.SoccerObjectID;
import soccerscope.view.FieldPane;

// �ܡ�������褹��쥤�䡼
public class BallLayer extends Layer implements SoccerObjectID {

	public BallLayer(FieldPane fieldPane) {
		super(fieldPane, true);
	}

	public String getLayerName() {
		return "Ball";
	}

	public void draw(Graphics g) {
		Scene scene = fieldPane.getScene();
		float ballMagnify = fieldPane.getBallMagnify();

		if (!scene.ball.isEnable())
			return;

		float ballR = Param.BALL_SIZE * ballMagnify;
		g.setColor(scene.ball.getColor());
		fillCircle(g, scene.ball.pos, ballR);
	}
}
