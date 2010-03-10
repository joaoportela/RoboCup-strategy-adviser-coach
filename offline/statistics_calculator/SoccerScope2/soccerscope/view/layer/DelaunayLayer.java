/*
 * $Log: DelaunayLayer.java,v $
 * Revision 1.5  2002/10/04 10:39:23  koji
 * Tuple,Point,Vector�Υѥå������ѹ���Geometry,Circle,Line,Rectangle�ɲ�
 *
 * Revision 1.4  2002/09/12 15:28:31  koji
 * ������������?���ɲ�,ColorTool��Color2�����
 *
 * Revision 1.3  2002/09/02 07:47:15  koji
 * ��꡼���ǥ��쥯�ȥ���Ѱ�
 *
 * Revision 1.2  2002/09/02 07:06:28  taku-ako
 * Geometry��Ϣ�Υ饤�֥������
 *
 * Revision 1.1.1.1  2002/03/01 14:12:53  koji
 * CVS�����
 *
 */

package soccerscope.view.layer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import soccerscope.model.Param;
import soccerscope.model.Scene;
import soccerscope.util.geom.Point2f;
import soccerscope.view.FieldPane;

public class DelaunayLayer extends Layer implements ItemListener {

	public DelaunayLayer(FieldPane fieldPane, boolean enable) {
		super(fieldPane, enable);
		drawLeft = true;
		drawRight = false;
	}

	public String getLayerName() {
		return "Delaunay";
	}

	private JCheckBox leftBox;
	private JCheckBox rightBox;

	public JPanel getConfigPanel() {
		leftBox = new JCheckBox("Left", drawLeft);
		leftBox.addItemListener(this);
		rightBox = new JCheckBox("Right", drawRight);
		rightBox.addItemListener(this);
		JPanel panel = new JPanel();
		panel.add(leftBox);
		panel.add(rightBox);

		return panel;
	}

	public void itemStateChanged(ItemEvent ie) {
		Object o = ie.getSource();
		if (o == leftBox || o == rightBox) {
			drawLeft = leftBox.isSelected();
			drawRight = rightBox.isSelected();
			fieldPane.repaint();
		}
	}

	private Scene scene;
	private boolean drawLeft;
	private boolean drawRight;

	public void draw(Graphics g) {
		if (!enable)
			return;

		scene = fieldPane.getScene();
		float ballMagnify = fieldPane.getBallMagnify();
		float playerMagnify = fieldPane.getPlayerMagnify();

		if (drawLeft) {
			setDelaunay(1, 11);
			for (int i = 1; i < 11 - 1; i++) {
				if (!scene.player[i].isEnable())
					continue;

				Color drawColor = scene.player[i].getColor();
				g.setColor(drawColor);
				int n = i * STORAGEWIDTH + i + 1;
				for (int j = i + 1; j < 11; j++) {
					if (distance[n] >= 0) {
						drawLine(g, scene.player[i].pos, scene.player[j].pos);
					}
					n++;
				}
			}
		}
		if (drawRight) {
			setDelaunay(12, 22);
			for (int i = 12; i < 22 - 1; i++) {
				if (!scene.player[i].isEnable())
					continue;

				Color drawColor = scene.player[i].getColor();
				g.setColor(drawColor);
				int n = i * STORAGEWIDTH + i + 1;
				for (int j = i + 1; j < 22; j++) {
					if (distance[n] >= 0) {
						drawLine(g, scene.player[i].pos, scene.player[j].pos);
					}
					n++;
				}
			}
		}
	}

	private static final int STORAGEWIDTH = Param.MAX_PLAYER * 2 + 4;
	private double distance[] = new double[Param.MAX_PLAYER * 2 * STORAGEWIDTH];

	private void setDelaunay(int minNum, int maxNum) {
		int m, n;
		double a, b, c, x0, y0, x1, y1, x2, y2, x3, y3;
		for (int i = minNum; i < maxNum - 1; i++) {
			Point2f drawPoint0 = new Point2f(scene.player[i].pos);
			Point2f p0 = fieldPane.fieldToScreen(drawPoint0);
			x0 = (double) p0.x;
			y0 = (double) p0.y;
			n = i * STORAGEWIDTH + i + 1;
			for (int j = i + 1; j < maxNum; j++) {
				Point2f drawPoint1 = new Point2f(scene.player[j].pos);
				Point2f p1 = fieldPane.fieldToScreen(drawPoint1);
				x1 = (double) p1.x;
				y1 = (double) p1.y;
				a = b = Math
						.sqrt((x1 - x0) * (x1 - x0) + (y1 - y0) * (y1 - y0));
				loop: for (int k = minNum; k < i; k++) {
					Point2f drawPoint2 = new Point2f(scene.player[k].pos);
					Point2f p2 = fieldPane.fieldToScreen(drawPoint2);
					x2 = (double) p2.x;
					y2 = (double) p2.y;
					m = k * STORAGEWIDTH + k + 1;
					for (int l = k + 1; l < maxNum; l++) {
						c = distance[m];
						if (l != i && l != j && c >= 0) {
							Point2f drawPoint3 = new Point2f(
									scene.player[l].pos);
							Point2f p3 = fieldPane.fieldToScreen(drawPoint3);
							x3 = (double) p3.x;
							y3 = (double) p3.y;
							if (isCross(x0, y0, x1, y1, x2, y2, x3, y3)) {
								if (b > c) {
									a = -1;
									break loop;
								}
							}
						}
						m++;
					}
				}
				distance[n] = a;
				if (a >= 0) {
					for (int k = minNum; k < i; k++) {
						Point2f drawPoint2 = new Point2f(scene.player[k].pos);
						Point2f p2 = fieldPane.fieldToScreen(drawPoint2);
						x2 = (double) p2.x;
						y2 = (double) p2.y;
						m = k * STORAGEWIDTH + k + 1;
						for (int l = k + 1; l < maxNum; l++) {
							Point2f drawPoint3 = new Point2f(
									scene.player[l].pos);
							Point2f p3 = fieldPane.fieldToScreen(drawPoint3);
							x3 = (double) p3.x;
							y3 = (double) p3.y;
							if (l != i && l != j && distance[m] >= 0
									&& isCross(x0, y0, x1, y1, x2, y2, x3, y3))
								distance[m] = -1;
							m++;
						}
					}
				}
				n++;
			}
		}
	}

	private boolean isCross(double x0, double y0, double x1, double y1,
			double x2, double y2, double x3, double y3) {
		double a, b;

		if (x1 == x0)
			a = 10000;
		else
			a = (y1 - y0) / (x1 - x0);
		b = y0 - a * x0;
		if ((y2 - (a * x2 + b)) * (y3 - (a * x3 + b)) < 0) {
			if (x3 == x2)
				a = 10000;
			else
				a = (y3 - y2) / (x3 - x2);
			b = y2 - a * x2;
			if ((y0 - (a * x0 + b)) * (y1 - (a * x1 + b)) < 0)
				return true;
		}
		return false;
	}
}
