/*
 * $Header: $
 */

package soccerscope.view.layer;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import soccerscope.model.Param;
import soccerscope.model.Scene;
import soccerscope.util.geom.Point2f;
import soccerscope.util.geom.Vector2f;
import soccerscope.view.FieldPane;

public class ControlRegionLayer extends Layer implements ChangeListener {

	public ControlRegionLayer(FieldPane fieldPane, boolean enable) {
		super(fieldPane, enable);
	}

	public String getLayerName() {
		return "ControlRegion";
	}

	private JSlider slider;
	private JLabel label;

	public JPanel getConfigPanel() {
		JPanel jp = new JPanel();
		label = new JLabel("1");
		slider = new JSlider(0, 20, 1);
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		slider.setPaintTrack(true);
		slider.setMajorTickSpacing(5);
		slider.addChangeListener(this);
		jp.add(label);
		jp.add(slider);
		return jp;
	}

	public void stateChanged(ChangeEvent e) {
		time = slider.getValue();
		label.setText(String.valueOf(time));
		fieldPane.repaint();
	}

	private int time = 1;
	private Point2f drawPoint = new Point2f();
	private Point2f drawSize = new Point2f();

	public void draw(Graphics g) {
		if (!enable)
			return;

		Scene scene = fieldPane.getScene();
		float playerMagnify = fieldPane.getPlayerMagnify();

		for (int i = 0; i < Param.MAX_PLAYER * 2; i++) {
			if (!scene.player[i].isEnable() || !fieldPane.isSelected(i))
				continue;

			float playerR = scene.player[i].getPlayerSize() * playerMagnify;
			Color drawColor = scene.player[i].getColor();

			float dist = scene.player[i].calcDistanceAfterNTime(scene.player[i]
					.getDashAccelerationMax(), time);

			g.setColor(drawColor);
			Point2f ppos = new Point2f(scene.player[i].pos);
			ppos.add(Vector2f.polar2vector(dist, scene.player[i].angle));
			drawCircle(g, ppos, scene.player[i].getKickable());
			drawCircle(g, scene.player[i].pos, scene.player[i].getKickable());
			drawRect(g, new Point2f(scene.player[i].pos.x,
					scene.player[i].pos.y - scene.player[i].getKickable()),
					new Point2f(dist, scene.player[i].getKickable() * 2),
					(int) scene.player[i].angle, scene.player[i].pos);

		}
	}
}
