/*
 * $Header: $
 */

package soccerscope.view.layer;

import java.awt.Color;
import java.awt.Graphics;

import soccerscope.model.Param;
import soccerscope.model.Scene;
import soccerscope.util.geom.Point2f;
import soccerscope.view.FieldPane;

public class VoronoiLayer extends Layer {
	private static Point2f tsp = new Point2f();
	private static Point2f tep = new Point2f();

	public VoronoiLayer(FieldPane fieldPane, boolean enable) {
		super(fieldPane, enable);
	}

	public String getLayerName() {
		return "Voronoi";
	}

	Scene scene;

	public void draw(Graphics g) {
		if (!enable)
			return;
		scene = fieldPane.getScene();

		setVoronoi();

		g.setColor(Color.white);
		for (int i = 0; i < Param.MAX_PLAYER * 2; i++) {
			if (!scene.player[i].isEnable())
				continue;
			// draw voronoi
			int n = i * STORAGEWIDTH + i + 1;
			for (int j = i + 1; j < Param.MAX_PLAYER * 2 + 4; j++) {
				if (sx[n] > -Double.MAX_VALUE) {
					tsp.set((float) sx[n] - ADD, (float) sy[n] - ADD);
					tep.set((float) ex[n] - ADD, (float) ey[n] - ADD);
					drawLine(g, tsp, tep);
				}
				n++;
			}
		}
	}

	private static final double BORDERWIDTH = .005;
	private static final int STORAGEWIDTH = Param.MAX_PLAYER * 2 + 4;
	private static final int ADD = 1000;
	private static double sx[] = new double[Param.MAX_PLAYER * 2 * STORAGEWIDTH];
	private static double sy[] = new double[Param.MAX_PLAYER * 2 * STORAGEWIDTH];
	private static double ex[] = new double[Param.MAX_PLAYER * 2 * STORAGEWIDTH];
	private static double ey[] = new double[Param.MAX_PLAYER * 2 * STORAGEWIDTH];
	private static final float appW = ADD + bottomRightCorner.x;
	private static final float appH = ADD + bottomRightCorner.y;
	private static final float appWe = ADD + topLeftCorner.x;
	private static final float appHe = ADD + topLeftCorner.y;

	private void setVoronoi() {
		int m, n;
		double a, b, a0, b0, a1, b1, x, y, x0, y0, x1, y1;

		for (int i = 0; i < Param.MAX_PLAYER * 2; i++) {
			x0 = scene.player[i].pos.x + ADD;
			y0 = scene.player[i].pos.y + ADD;

			n = i * STORAGEWIDTH + i + 1;

			for (int j = i + 1; j < Param.MAX_PLAYER * 2; j++) {
				x1 = scene.player[j].pos.x + ADD;
				y1 = scene.player[j].pos.y + ADD;

				if (x1 == x0) {
					a = 0;
				} else if (y1 == y0) {
					a = 10000;
				} else {
					a = -1 / ((y1 - y0) / (x1 - x0));
				}

				b = (y0 + y1) / 2 - a * (x0 + x1) / 2;

				if (a > -1 && a <= 1) {
					sx[n] = 0;
					sy[n] = a * sx[n] + b;
					ex[n] = appW - 1;
					ey[n] = a * ex[n] + b;
				} else {
					sy[n] = 0;
					sx[n] = (sy[n] - b) / a;
					ey[n] = appH - 1;
					ex[n] = (ey[n] - b) / a;
				}
				n++;
			}

			/* nouth line */
			sx[n] = appWe - BORDERWIDTH;
			sy[n] = appHe - BORDERWIDTH;
			ex[n] = appW - BORDERWIDTH;
			ey[n] = appHe - BORDERWIDTH;
			n++;

			/* west line */
			sx[n] = appWe - BORDERWIDTH + .01;
			sy[n] = appHe - BORDERWIDTH;
			ex[n] = appWe - BORDERWIDTH;
			ey[n] = appH - BORDERWIDTH;
			n++;

			/* east line */
			sx[n] = appW - BORDERWIDTH;
			sy[n] = appHe - BORDERWIDTH;
			ex[n] = appW - BORDERWIDTH - .01;
			ey[n] = appH - BORDERWIDTH;
			n++;

			/* south line */
			sx[n] = appWe - BORDERWIDTH;
			sy[n] = appH - BORDERWIDTH;
			ex[n] = appW - BORDERWIDTH;
			ey[n] = appH - BORDERWIDTH;
		}
		for (int i = 0; i < Param.MAX_PLAYER * 2; i++) {
			x0 = scene.player[i].pos.x + ADD;
			y0 = scene.player[i].pos.y + ADD;

			for (int j = 0; j < Param.MAX_PLAYER * 2 + 4; j++) {
				if (j == i)
					continue;

				if (j > i) {
					n = i * STORAGEWIDTH + j;
				} else {
					n = j * STORAGEWIDTH + i;
				}
				if (sx[n] > -Double.MAX_VALUE) {
					a0 = (ey[n] - sy[n]) / (ex[n] - sx[n]);
					b0 = sy[n] - a0 * sx[n];
					for (int k = i + 1; k < Param.MAX_PLAYER * 2 + 4; k++) {
						if (k == j)
							continue;

						m = i * STORAGEWIDTH + k;
						if (sx[m] > -Double.MAX_VALUE) {
							a1 = (ey[m] - sy[m]) / (ex[m] - sx[m]);
							b1 = sy[m] - a1 * sx[m];
							x = -(b1 - b0) / (a1 - a0);
							y = a0 * x + b0;

							if ((a0 * x0 + b0 - y0) * (a0 * sx[m] + b0 - sy[m]) < 0) {
								sx[m] = x;
								sy[m] = y;
							}

							if ((a0 * x0 + b0 - y0) * (a0 * ex[m] + b0 - ey[m]) < 0) {
								if (sx[m] == x) {
									sx[m] = -Double.MAX_VALUE;
								} else {
									ex[m] = x;
									ey[m] = y;
								}
							}
						}
					}
				}
			}
		}
	}
}
