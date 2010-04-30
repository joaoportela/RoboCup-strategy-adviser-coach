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

public class VisibleLayer extends Layer {

	public VisibleLayer(FieldPane fieldPane, boolean enable) {
		super(fieldPane, enable);
	}

	public String getLayerName() {
		return "Visible";
	}

	private Point2f drawPoint = new Point2f();

	public void draw(Graphics g) {
		if (!enable)
			return;

		Scene scene = fieldPane.getScene();
		float ballMagnify = fieldPane.getBallMagnify();
		float playerMagnify = fieldPane.getPlayerMagnify();

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
}
