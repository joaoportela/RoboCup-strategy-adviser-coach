/*
 * $Log: Ruler.java,v $
 * Revision 1.2  2002/10/04 10:39:20  koji
 * Tuple,Point,Vector�Υѥå������ѹ���Geometry,Circle,Line,Rectangle�ɲ�
 *
 *
 */

package soccerscope.view;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;

public class Ruler {
	private Line2D distanceLine;
	private Line2D p1xLine, p1yLine, p2xLine, p2yLine;
	private Point disStrPoint, pxStrPoint, pyStrPoint;
	private double distance, xDistance, yDistance;
	private int p1x, p1y, p2x, p2y;
	private FieldPane fieldPane;

	public Ruler(int p1x, int p1y, int p2x, int p2y, FieldPane fieldPane,
			int inset) {
		this.p1x = p1x;
		this.p1y = p1y;
		this.p2x = p2x;
		this.p2y = p2y;
		this.fieldPane = fieldPane;

		Dimension d = fieldPane.getSize();

		distanceLine = new Line2D.Float(p1x, p1y, p2x, p2y);
		disStrPoint = new Point((p1x + p2x) / 2, (p1y + p2y) / 2);
		distance = createDistance(p1x, p1y, p2x, p2y);

		p1xLine = new Line2D.Float(0, p2y, d.width, p2y);
		p2xLine = new Line2D.Float(0, p1y, d.width, p1y);
		pxStrPoint = new Point((p1x + p2x) / 2, p1y);
		xDistance = createDistance(p1x, p1y, p2x, p1y);

		p1yLine = new Line2D.Float(p2x, inset, p2x, d.height + inset);
		p2yLine = new Line2D.Float(p1x, inset, p1x, d.height + inset);
		pyStrPoint = new Point(p2x, (p1y + p2y) / 2);
		yDistance = createDistance(p2x, p1y, p2x, p2y);
	}

	private double createDistance(int x1, int y1, int x2, int y2) {
		return Point2D.distance(x1 / fieldPane.getMagnify(), y1
				/ fieldPane.getMagnify(), x2 / fieldPane.getMagnify(), y2
				/ fieldPane.getMagnify());
	}

	public void draw(Graphics g) {
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(Color.cyan.darker());
		Arc2D arc;
		String angleStr;
		if ((p1x < p2x) && (p1y < p2y)) {
			/*
			 * 
			 * . p1
			 * 
			 * 
			 * . p2
			 */
			arc = createArc2D(0d, -Math.toDegrees(Math.atan2(yDistance,
					xDistance)));
			angleStr = df.format(Math.toDegrees(Math
					.atan2(yDistance, xDistance)));
		} else if ((p1x > p2x) && (p1y < p2y)) {
			/*
			 * 
			 * . p1
			 * 
			 * 
			 * . p2
			 */
			arc = createArc2D(0d, -180
					+ Math.toDegrees(Math.atan2(yDistance, xDistance)));
			angleStr = df.format(90 + Math.toDegrees(Math.atan2(xDistance,
					yDistance)));
		} else if ((p1x > p2x) && (p1y > p2y)) {
			/*
			 * 
			 * . p2
			 * 
			 * 
			 * . p1
			 */
			arc = createArc2D(0d, 180 - Math.toDegrees(Math.atan2(yDistance,
					xDistance)));
			angleStr = df.format(-90
					- Math.toDegrees(Math.atan2(xDistance, yDistance)));
		} else {
			/*
			 * 
			 * . p2
			 * 
			 * 
			 * . p1
			 */
			arc = createArc2D(0d, Math.toDegrees(Math.atan2(yDistance,
					xDistance)));
			angleStr = df.format(-Math.toDegrees(Math.atan2(yDistance,
					xDistance)));
		}

		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
				0.5f));
		g2.setColor(Color.cyan.darker());
		g2.fill(arc);
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 1.0f));
		g2.setColor(Color.cyan);
		g2.drawString(angleStr, p1x, p1y);
		g2.setColor(Color.white);
		g2.drawString(df.format(distance), disStrPoint.x, disStrPoint.y);
		g2.draw(distanceLine);
		g2.draw(p1xLine);
		g2.draw(p2xLine);
		g2.drawString(df.format(xDistance), pxStrPoint.x, pxStrPoint.y);
		g2.draw(p1yLine);
		g2.draw(p2yLine);
		g2.drawString(df.format(yDistance), pyStrPoint.x, pyStrPoint.y);
	}

	private Arc2D createArc2D(double start, double end) {
		double arcR = 60;
		return new Arc2D.Double((double) p1x - arcR / 2, (double) p1y - arcR
				/ 2, arcR, arcR, start, end, Arc2D.PIE);
	}
}
