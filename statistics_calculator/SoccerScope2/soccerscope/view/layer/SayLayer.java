/*
 * $Header: $
 */

package soccerscope.view.layer;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import soccerscope.model.Param;
import soccerscope.model.Scene;
import soccerscope.model.SceneSet;
import soccerscope.model.WorldModel;
import soccerscope.util.Color2;
import soccerscope.util.geom.Point2f;
import soccerscope.view.FieldPane;

public class SayLayer extends Layer {

	public SayLayer(FieldPane fieldPane, boolean enable) {
		super(fieldPane, enable);
	}

	public String getLayerName() {
		return "Say";
	}

	private int lower = 5;
	private Point2f drawPoint = new Point2f();
	private Point2f drawOffset = new Point2f();

	public void draw(Graphics g) {
		if (!enable)
			return;

		Scene scene = fieldPane.getScene();
		float magnify = fieldPane.getMagnify();
		float playerMagnify = fieldPane.getPlayerMagnify();
		// float playerR = Param.PLAYER_SIZE * playerMagnify;

		WorldModel wm = WorldModel.getInstance();
		SceneSet sceneSet = wm.getSceneSet();
		Font f1 = g.getFont();
		Font f2 = f1.deriveFont(22.0f);

		for (int i = 0; i < Param.MAX_PLAYER * 2; i++) {
			if (scene.player[i].isEnable()
					&& scene.player[i].sayMessage != null
					&& scene.player[i].sayMessage.length() != 0) {
				g.setColor(Color.white);
				drawOffset.set(scene.player[i].getPlayerSize() * playerMagnify,
						0);
				drawPoint.add(scene.player[i].pos, drawOffset);
				g.setFont(f2);
				drawString(g, scene.player[i].sayMessage.substring(0, Math.min(
						10, scene.player[i].sayMessage.length() - 1)),
						drawPoint);
				g.setFont(f1);
			}
		}

		for (int offset = lower; offset >= 0; offset--) {
			if (sceneSet.hasScene(scene.time - offset)) {
				Scene tmpScene = sceneSet.getScene(scene.time - offset);
				for (int i = 0; i < Param.MAX_PLAYER * 2; i++) {
					if (tmpScene.player[i].isEnable()
							&& tmpScene.player[i].sayMessage != null
							&& tmpScene.player[i].sayMessage.length() != 0) {
						g.setColor(Color2.mix(Color.white, Color2.forestGreen,
								2, offset));
						drawOffset.set(tmpScene.player[i].getPlayerSize()
								* playerMagnify, 0);
						drawPoint.add(tmpScene.player[i].pos, drawOffset);
						g.setFont(f2);
						drawString(g, tmpScene.player[i].sayMessage.substring(
								0, Math.min(10, tmpScene.player[i].sayMessage
										.length() - 1)), drawPoint);
						g.setFont(f1);

						g.setColor(Color2.mix(tmpScene.player[i].getColor()
								.brighter(), Color2.forestGreen, 2, offset));
						drawCircle(
								g,
								tmpScene.player[i].pos,
								tmpScene.player[i].getPlayerSize()
										* playerMagnify
										* ((float) (offset + 1) / (float) lower)
										* 2);
					}
				}
			}
		}
	}

}
