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

public class UnumLayer extends Layer {

	public UnumLayer(FieldPane fieldPane) {
		super(fieldPane, true);
	}

	public UnumLayer(FieldPane fieldPane, boolean enable) {
		super(fieldPane, enable);
	}

	public String getLayerName() {
		return "Unum";
	}

	private Point2f drawPoint = new Point2f();
	private Point2f drawOffset = new Point2f();

	public void draw(Graphics g) {
		if (!enable)
			return;

		Scene scene = fieldPane.getScene();
		float ballMagnify = fieldPane.getBallMagnify();
		float playerMagnify = fieldPane.getPlayerMagnify();
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
}
