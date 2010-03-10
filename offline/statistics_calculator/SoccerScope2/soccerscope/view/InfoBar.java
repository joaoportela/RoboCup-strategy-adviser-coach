/*
 * $Log: InfoBar.java,v $
 * Revision 1.2  2002/09/12 15:28:29  koji
 * ������������?���ɲ�,ColorTool��Color2�����
 *
 * Revision 1.1.1.1  2002/03/01 14:12:52  koji
 * CVS�����
 *
 */

package soccerscope.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.JComponent;

import soccerscope.model.ColorDB;
import soccerscope.model.Scene;

public class InfoBar extends JComponent implements ScopeWindow {

	private FontMetrics fm;
	private int margin;

	private Scene scene;

	public InfoBar(Scene scene) {
		super();

		this.scene = scene;
	}

	public Dimension getPreferredSize() {
		return getMinimumSize();
	}

	public Dimension getMinimumSize() {
		if (fm == null) {
			fm = this.getFontMetrics(this.getFont());
			margin = fm.getHeight() / 4;
		}
		return new Dimension(0, fm.getHeight() + margin * 2);
	}

	public void setScene(Scene scene) {
		this.scene = scene;
	}

	public void paint(Graphics g) {
		if (fm == null) {
			fm = this.getFontMetrics(this.getFont());
			margin = fm.getHeight() / 4;
		}

		if (scene == null)
			return;

		Dimension d = getSize();
		String s;

		// draw left team name and score
		g.setColor(ColorDB.getColor("team_l_color"));
		g.fillRect(0, 0, d.width / 4, d.height);
		g.setColor(Color.black);
		s = scene.left.name + " :  " + scene.left.score;
		g.drawString(s, 0 + margin, fm.getAscent() + margin);

		// draw playmode
		g.setColor(Color.white);
		g.fillRect(d.width / 4, 0, (d.width / 4) * 2, d.height);
		g.setColor(Color.black);
		s = scene.pmode.toString();
		g.drawString(s, (d.width / 4) * 2 - fm.stringWidth(s) - margin, fm
				.getAscent()
				+ margin);

		// draw time
		s = Integer.toString(scene.time);
		g.drawString(s, (d.width / 4) * 2 + margin, fm.getAscent() + margin);

		// draw right tean name and score
		g.setColor(ColorDB.getColor("team_r_color"));
		g.fillRect((d.width / 4) * 3, 0, d.width, d.height);
		g.setColor(Color.black);
		s = scene.right.name + " :  " + scene.right.score;
		g.drawString(s, d.width - fm.stringWidth(s) - margin, fm.getAscent()
				+ margin);

		// draw border
		g.setColor(Color.black);
		g.drawRect(0, 0, d.width - 1, d.height - 1);
	}
}
