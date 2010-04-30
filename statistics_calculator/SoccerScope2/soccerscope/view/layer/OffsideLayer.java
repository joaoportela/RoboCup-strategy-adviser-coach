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

public class OffsideLayer extends Layer {

	public OffsideLayer(FieldPane fieldPane, boolean enable) {
		super(fieldPane, enable);
	}

	public String getLayerName() {
		return "Offside";
	}

	private Point2f top = new Point2f(0, -Param.PITCH_HALF_WIDTH);
	private Point2f bottom = new Point2f(0, Param.PITCH_HALF_WIDTH);

	public void draw(Graphics g) {
		if (!enable)
			return;

		Scene scene = fieldPane.getScene();
		Color leftColor, rightColor;
		if (scene.right.offside)
			leftColor = Color.red;
		else
			leftColor = Color.green;

		if (scene.left.offside)
			rightColor = Color.red;
		else
			rightColor = Color.green;

		g.setColor(leftColor);
		top.x = scene.left.offsideline;
		bottom.x = scene.left.offsideline;
		drawLine(g, top, bottom);

		g.setColor(rightColor);
		top.x = scene.right.offsideline;
		bottom.x = scene.right.offsideline;
		drawLine(g, top, bottom);

		float playerMagnify = fieldPane.getPlayerMagnify();
		for (int i = 0; i < Param.MAX_PLAYER * 2; i++) {
			if (!scene.player[i].isEnable())
				continue;

			float playerR = scene.player[i].getPlayerSize() * playerMagnify;
			// draw mode
			if (scene.player[i].offside) {
				g.setColor(Color.white);
				drawCircle(g, scene.player[i].pos, playerR + 0.1f);
			}
		}
	}
}
