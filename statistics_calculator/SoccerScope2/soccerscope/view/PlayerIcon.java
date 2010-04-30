/*
 * $Log: PlayerIcon.java,v $
 * Revision 1.1  2002/09/12 15:28:29  koji
 * ������������?���ɲ�,ColorTool��Color2�����
 *
 *
 */

package soccerscope.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

public class PlayerIcon implements Icon {

	private Color color;

	public PlayerIcon(Color c) {
		color = c;
	}

	public void setColor(Color c) {
		color = c;
	}

	public Color getColor() {
		return color;
	}

	public int getIconHeight() {
		return 18;
	}

	public int getIconWidth() {
		return 18;
	}

	public void paintIcon(Component c, Graphics g, int x, int y) {
		// fill back
		g.setColor(Color.black);
		g.fillOval(0, 0, 16, 16);

		// body
		g.setColor(color);
		g.drawOval(0, 0, 16, 16);

		// angle_body
		g.fillArc(0, 0, 16, 16, -90, 180);
	}
}
