/*
 * $Header: $
 */

package soccerscope.view.layer;

import java.awt.Dimension;
import java.awt.Graphics;

import soccerscope.util.Color2;
import soccerscope.view.FieldPane;

// ���å����ե���������Τ����褹��쥤�䡼
public class PitchLayer extends Layer {

	public PitchLayer(FieldPane fieldPane) {
		super(fieldPane, true);
	}

	public String getLayerName() {
		return "Pitch";
	}

	public void draw(Graphics g) {
		Dimension d = fieldPane.getSize();
		g.setColor(Color2.indianRed);
		g.fillRect(0, 0, d.width, d.height);
		g.setColor(Color2.forestGreen);
		fillRect(g, topLeftCorner, pitchSize);
	}
}
