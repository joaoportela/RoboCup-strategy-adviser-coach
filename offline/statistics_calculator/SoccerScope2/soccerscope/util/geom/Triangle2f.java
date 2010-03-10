package soccerscope.util.geom;

public class Triangle2f {
	private Point2f a;
	private Point2f b;
	private Point2f c;

	// this is the cache for the 'contains' method
	private float t;
	private Vector2f ab;
	private Vector2f bc;
	private Vector2f ca;

	public Triangle2f(Point2f a, Point2f b, Point2f c) {
		this.a = a;
		this.b = b;
		this.c = c;

		// fill the cache
		this.ab = new Vector2f(a, b);
		this.bc = new Vector2f(b, c);
		this.ca = new Vector2f(c, a);
		this.t = ab.crossProduct(bc);
	}

	public boolean contains(Point2f p) {
		if (this.t * this.ab.crossProduct(new Vector2f(b, p)) < 0)
			return false;
		if (this.t * this.bc.crossProduct(new Vector2f(c, p)) < 0)
			return false;
		if (this.t * this.ca.crossProduct(new Vector2f(a, p)) < 0)
			return false;
		return true;
	}

}
