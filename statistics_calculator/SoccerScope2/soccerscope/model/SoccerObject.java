/*
 * $Header: $
 */

package soccerscope.model;

import soccerscope.util.geom.Point2f;
import soccerscope.util.geom.Vector2f;

public class SoccerObject {
	public Point2f pos; // position
	public Vector2f vel; // velocity
	public Vector2f acc; // acceleration

	public SoccerObject() {
		pos = new Point2f();
		vel = new Vector2f();
		acc = new Vector2f();
	}
}
