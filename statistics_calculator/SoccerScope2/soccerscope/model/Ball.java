/*
 * $Log: Ball.java,v $
 * Revision 1.1.1.1  2002/03/01 14:12:53  koji
 * CVS�����
 *
 */

package soccerscope.model;

import java.awt.Color;

public class Ball extends SoccerObject {
	// player mode
	public final static int DISABLE = 0x0000;
	public final static int STAND = 0x0001;

	public int mode; // mode(DISABLE or STAND)

	public Ball() {
		super();
		mode = DISABLE;
	}

	public boolean isEnable() {
		return ((mode & STAND) == STAND);
	}

	public Color getColor() {
		return ColorDB.getColor("ball");
	}
}
