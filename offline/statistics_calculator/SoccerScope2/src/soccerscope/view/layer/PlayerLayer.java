/*
 * $Header: $
 */

package soccerscope.view.layer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.ImageIcon;

import soccerscope.model.Player;
import soccerscope.model.Scene;
import soccerscope.util.geom.Point2f;
import soccerscope.view.FieldPane;

// �ץ쥤�䡼�����褹��쥤�䡼
public class PlayerLayer extends Layer {

	public PlayerLayer(FieldPane fieldPane) {
		super(fieldPane, true);
	}

	public String getLayerName() {
		return "Player";
	}

	private Point2f drawPoint = new Point2f();
	private Point2f drawSize = new Point2f();
	private Image selectImage = new ImageIcon(ClassLoader
			.getSystemResource("soccerscope/image/select.gif")).getImage();

	public void draw(Graphics g) {
		Scene scene = fieldPane.getScene();
		float ballMagnify = fieldPane.getBallMagnify();
		float playerMagnify = fieldPane.getPlayerMagnify();

		for (int i = 0; i < MAX_PLAYER * 2; i++) {
			if (!scene.player[i].isEnable())
				continue;

			float playerR = scene.player[i].getPlayerSize() * playerMagnify;
			Color drawColor = scene.player[i].getColor();

			// draw mode
			g.setColor(Color.black);

			if (scene.player[i].isCollision()) {
				drawCircle(g, scene.player[i].pos, playerR + 0.1f);
			}

			if (scene.player[i].isKicking() || scene.player[i].isTackling()
					|| scene.player[i].isCatching()) {
				g.setColor(drawColor.darker());
			} else if (scene.player[i].isKickFault()
					|| scene.player[i].isTackleFault()
					|| scene.player[i].isCatchFault()) {
				g.setColor(drawColor.darker().darker());
			}
			fillCircle(g, scene.player[i].pos, playerR);

			if (scene.player[i].isGoalie()
					&& scene.player[i].catchDir != Player.ERROR_DIR) {
				g.setColor(drawColor.brighter());
				drawRect(g,
						new Point2f(scene.player[i].pos.x,
								scene.player[i].pos.y
										- GOALIE_CATCHABLE_AREA_WIDTH / 2),
						new Point2f(GOALIE_CATCHABLE_AREA_LENGTH,
								GOALIE_CATCHABLE_AREA_WIDTH),
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

			if (fieldPane.isSelected(i)) {
				drawPoint.set(0, -playerR);
				drawPoint.add(scene.player[i].pos);
				drawImage(g, selectImage, drawPoint, -selectImage
						.getWidth(null) / 2, -selectImage.getHeight(null));
			}
		}
	}
}
