/*
 * $Header: $
 */

package soccerscope.view.layer;

import java.awt.Color;
import java.awt.Graphics;

import soccerscope.model.Param;
import soccerscope.model.Scene;
import soccerscope.util.Color2;
import soccerscope.util.geom.Point2f;
import soccerscope.view.FieldPane;

public class NeckLayer extends Layer {

	public NeckLayer(FieldPane fieldPane, boolean enable) {
		super(fieldPane, enable);
	}

	public String getLayerName() {
		return "Neck";
	}

	private Point2f drawPoint = new Point2f();

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
			Color drawColor = scene.player[i].getColor();

			// draw neck angle
			g.setColor(Color2.mix(drawColor, Color2.forestGreen, 1, 3));
			int a = (int) (-(scene.player[i].angle + scene.player[i].angleNeck) - scene.player[i].angleVisible / 2.0);

			drawPoint.sub(scene.player[i].pos, visibleSize);
			fillArc(g, drawPoint, visibleSize2, a, scene.player[i].angleVisible);
			g.setColor(Color2.mix(drawColor, Color2.forestGreen, 1, 1));
			drawOval(g, drawPoint, visibleSize2);
		}
	}
}
