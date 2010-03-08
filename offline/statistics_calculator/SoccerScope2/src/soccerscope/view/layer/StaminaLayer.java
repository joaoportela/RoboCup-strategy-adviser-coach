/*
 * $Header: $
 */

package soccerscope.view.layer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import soccerscope.model.Scene;
import soccerscope.util.geom.Point2f;
import soccerscope.util.geom.Tuple2f;
import soccerscope.view.FieldPane;

public class StaminaLayer extends Layer {

	public StaminaLayer(FieldPane fieldPane, boolean enable) {
		super(fieldPane, enable);
	}

	public String getLayerName() {
		return "Stamina";
	}

	private Point2f drawPoint = new Point2f();
	private Tuple2f size = new Tuple2f();
	private Point2f drawOffset = new Point2f();

	public void draw(Graphics g) {
		if (!enable)
			return;

		Graphics2D g2 = (Graphics2D) g;

		Scene scene = fieldPane.getScene();
		float ballMagnify = fieldPane.getBallMagnify();
		float playerMagnify = fieldPane.getPlayerMagnify();
		for (int i = 0; i < MAX_PLAYER * 2; i++) {
			if (!scene.player[i].isEnable())
				continue;

			float playerR = scene.player[i].getPlayerSize() * playerMagnify;
			Color drawColor = scene.player[i].getColor();
			// draw stamina

			g.setColor(Color.black);
			size.set(playerR / 2, playerR * 2);
			drawOffset.set(playerR + size.x, playerR - size.y);
			drawPoint.add(scene.player[i].pos, drawOffset);
			fillRect(g, drawPoint, size);

			if (scene.player[i].stamina > STAMINA_MAX * EFFORT_INC_THR)
				g.setColor(Color.green);
			else if (scene.player[i].stamina > STAMINA_MAX * EFFORT_DEC_THR)
				g
						.setColor(Color
								.getHSBColor(
										(scene.player[i].stamina - STAMINA_MAX
												* EFFORT_DEC_THR)
												/ (3 * (STAMINA_MAX * (EFFORT_INC_THR - EFFORT_DEC_THR))),
										1.0f, 1.0f));
			else
				g.setColor(Color.red);

			size.set(playerR / 2, playerR * 2
					* (scene.player[i].stamina / STAMINA_MAX));
			drawOffset.set(playerR + size.x, playerR - size.y);
			drawPoint.add(scene.player[i].pos, drawOffset);
			fillRect(g, drawPoint, size);
		}
	}
}
