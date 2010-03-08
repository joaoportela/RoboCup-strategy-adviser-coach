/*
 * $Header: $
 */

package soccerscope.view.layer;

import java.awt.Graphics;

import javax.swing.JPanel;

import soccerscope.view.FieldPane;

public class TemplateLayer extends Layer {

	public TemplateLayer(FieldPane fieldPane, boolean enable) {
		super(fieldPane, enable);
	}

	public String getLayerName() {
		return "";
	}

	public JPanel getConfigPanel() {
		return null;
	}

	public void draw(Graphics g) {
		if (!enable)
			return;
	}
}
