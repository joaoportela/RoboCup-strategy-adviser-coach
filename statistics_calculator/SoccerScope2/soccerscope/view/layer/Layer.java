/*
 * $Header: $
 */

package soccerscope.view.layer;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.PathIterator;

import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import soccerscope.model.Param;
import soccerscope.util.ButtonManager;
import soccerscope.util.geom.Point2f;
import soccerscope.util.geom.Tuple2f;
import soccerscope.view.FieldPane;

public abstract class Layer implements Param {
	protected FieldPane fieldPane;
	protected boolean enable;

	public Layer(FieldPane fieldPane, boolean enable) {
		this.fieldPane = fieldPane;
		this.enable = enable;
	}

	public abstract void draw(Graphics g);

	public abstract String getLayerName();

	// ���Υ쥤��˴ؤ�������ѥͥ���֤�
	public JPanel getConfigPanel() {
		return null;
	}

	public boolean isEnabled() {
		return enable;
	}

	public void setEnabled(boolean enable) {
		ButtonManager.setSelected(getLayerName(), enable);
		fieldPane.repaint();
	}

	public JCheckBox createJCheckBox() {
		JCheckBox checkBox = new JCheckBox(getLayerName(), enable);
		checkBox.addActionListener(new LayerAction());
		ButtonManager.addButton(getLayerName(), checkBox);
		return checkBox;
	}

	public JCheckBoxMenuItem createJCheckBoxMenuItem() {
		JCheckBoxMenuItem checkBoxMI = new JCheckBoxMenuItem(getLayerName(),
				enable);
		checkBoxMI.addActionListener(new LayerAction());
		ButtonManager.addButton(getLayerName(), checkBoxMI);
		return checkBoxMI;
	}

	public JToggleButton createJToggleButton() {
		JToggleButton button = new JToggleButton(getLayerName(), enable);
		button.addActionListener(new LayerAction());
		ButtonManager.addButton(getLayerName(), button);
		return button;
	}

	class LayerAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			enable = ((AbstractButton) e.getSource()).isSelected();
			setEnabled(enable);
		}
	}

	/****** �ե�����ɺ�ɸ�Ϥ����ˤȤ�����᥽�åɷ� *****/
	public void drawString(Graphics g, String s, Point2f point) {
		g.drawString(s, fieldPane.F2SX(point.x), fieldPane.F2SY(point.y));
	}

	public void drawLine(Graphics g, Point2f p1, Point2f p2) {
		int x1 = fieldPane.F2SX(p1.x);
		int y1 = fieldPane.F2SY(p1.y);
		int x2 = fieldPane.F2SX(p2.x);
		int y2 = fieldPane.F2SY(p2.y);
		// if (x1 < 0 || y1 < 0 || x2 < 0 || y2 < 0) return ;
		g.drawLine(x1, y1, x2, y2);
	}

	public void drawRect(Graphics g, Point2f topleft, Tuple2f size) {
		g.drawRect(fieldPane.F2SX(topleft.x), fieldPane.F2SY(topleft.y),
				fieldPane.F2SSX(size.x), fieldPane.F2SSY(size.y));
	}

	public void fillRect(Graphics g, Point2f topleft, Tuple2f size) {
		g.fillRect(fieldPane.F2SX(topleft.x), fieldPane.F2SY(topleft.y),
				fieldPane.F2SSX(size.x), fieldPane.F2SSY(size.y));
	}

	public void drawOval(Graphics g, Point2f topleft, Tuple2f size) {
		g.drawOval(fieldPane.F2SX(topleft.x), fieldPane.F2SY(topleft.y),
				fieldPane.F2SSX(size.x), fieldPane.F2SSY(size.y));
	}

	public void fillOval(Graphics g, Point2f topleft, Tuple2f size) {
		g.fillOval(fieldPane.F2SX(topleft.x), fieldPane.F2SY(topleft.y),
				fieldPane.F2SSX(size.x), fieldPane.F2SSY(size.y));
	}

	public void drawArc(Graphics g, Point2f topleft, Tuple2f size,
			int startAngle, int arcAngle) {
		g.drawArc(fieldPane.F2SX(topleft.x), fieldPane.F2SY(topleft.y),
				fieldPane.F2SSX(size.x), fieldPane.F2SSY(size.y), startAngle,
				arcAngle);
	}

	public void fillArc(Graphics g, Point2f topleft, Tuple2f size,
			int startAngle, int arcAngle) {
		g.fillArc(fieldPane.F2SX(topleft.x), fieldPane.F2SY(topleft.y),
				fieldPane.F2SSX(size.x), fieldPane.F2SSY(size.y), startAngle,
				arcAngle);
	}

	public void drawCircle(Graphics g, Point2f point, float r) {
		g.drawOval(fieldPane.F2SX(point.x - r), fieldPane.F2SY(point.y - r),
				fieldPane.F2SSX(r * 2), fieldPane.F2SSY(r * 2));
	}

	public void fillCircle(Graphics g, Point2f point, float r) {
		g.fillOval(fieldPane.F2SX(point.x - r), fieldPane.F2SY(point.y - r),
				fieldPane.F2SSX(r * 2), fieldPane.F2SSY(r * 2));
	}

	// ������濴���濴�Ȥ�����ž
	public void drawRect(Graphics g, Point2f topleft, Tuple2f size, float rotate) {
		Graphics2D g2 = (Graphics2D) g;
		g2.translate(fieldPane.F2SX(topleft.x + size.x / 2), fieldPane
				.F2SY(topleft.y + size.y / 2));
		g2.rotate(Math.toRadians(rotate));
		g2.drawRect(fieldPane.F2SSX(-size.x / 2), fieldPane.F2SSY(-size.y / 2),
				fieldPane.F2SSX(size.x), fieldPane.F2SSY(size.y));
		g2.rotate(Math.toRadians(-rotate));
		g2.translate(-fieldPane.F2SX(topleft.x + size.x / 2), -fieldPane
				.F2SY(topleft.y + size.y / 2));
	}

	// ������濴���濴�Ȥ�����ž
	public void fillRect(Graphics g, Point2f topleft, Tuple2f size, float rotate) {
		Graphics2D g2 = (Graphics2D) g;
		g2.translate(fieldPane.F2SX(topleft.x + size.x / 2), fieldPane
				.F2SY(topleft.y + size.y / 2));
		g2.rotate(Math.toRadians(rotate));
		g2.fillRect(fieldPane.F2SSX(-size.x / 2), fieldPane.F2SSY(-size.y / 2),
				fieldPane.F2SSX(size.x), fieldPane.F2SSY(size.y));
		g2.rotate(Math.toRadians(-rotate));
		g2.translate(-fieldPane.F2SX(topleft.x + size.x / 2), -fieldPane
				.F2SY(topleft.y + size.y / 2));
	}

	// center���濴�Ȥ��Ʋ�ž
	public void drawRect(Graphics g, Point2f topleft, Tuple2f size,
			float rotate, Point2f center) {
		Graphics2D g2 = (Graphics2D) g;
		g2.translate(fieldPane.F2SX(center.x), fieldPane.F2SY(center.y));
		g2.rotate(Math.toRadians(rotate));
		g2.drawRect(fieldPane.F2SSX(topleft.x - center.x), fieldPane
				.F2SSY(topleft.y - center.y), fieldPane.F2SSX(size.x),
				fieldPane.F2SSY(size.y));
		g2.rotate(Math.toRadians(-rotate));
		g2.translate(-fieldPane.F2SX(center.x), -fieldPane.F2SY(center.y));
	}

	// center���濴�Ȥ��Ʋ�ž
	public void fillRect(Graphics g, Point2f topleft, Tuple2f size,
			float rotate, Point2f center) {
		Graphics2D g2 = (Graphics2D) g;
		g2.translate(fieldPane.F2SX(center.x), fieldPane.F2SY(center.y));
		g2.rotate(Math.toRadians(rotate));
		g2.fillRect(fieldPane.F2SSX(topleft.x - center.x), fieldPane
				.F2SSY(topleft.y - center.y), fieldPane.F2SSX(size.x),
				fieldPane.F2SSY(size.y));
		g2.rotate(Math.toRadians(-rotate));
		g2.translate(-fieldPane.F2SX(center.x), -fieldPane.F2SY(center.y));
	}

	// �ʱߤ��濴���濴�Ȥ�����ž
	public void drawOval(Graphics g, Point2f topleft, Tuple2f size, float rotate) {
		Graphics2D g2 = (Graphics2D) g;
		g2.translate(fieldPane.F2SX(topleft.x + size.x / 2), fieldPane
				.F2SY(topleft.y + size.y / 2));
		g2.rotate(Math.toRadians(rotate));
		g2.drawOval(fieldPane.F2SSX(-size.x / 2), fieldPane.F2SSY(-size.y / 2),
				fieldPane.F2SSX(size.x), fieldPane.F2SSY(size.y));
		g2.rotate(Math.toRadians(-rotate));
		g2.translate(-fieldPane.F2SX(topleft.x + size.x / 2), -fieldPane
				.F2SY(topleft.y + size.y / 2));
	}

	private float coords[] = new float[6];
	private Point2f startPos = new Point2f();
	private Point2f endPos = new Point2f();

	public void drawPath(Graphics g, PathIterator pit) {
		int type;
		do {
			type = pit.currentSegment(coords);
			switch (type) {
			case PathIterator.SEG_MOVETO:
				startPos.set(coords[0], coords[1]);
				break;
			case PathIterator.SEG_LINETO:
				endPos.set(coords[0], coords[1]);
				drawLine(g, startPos, endPos);
				startPos.set(endPos);
				break;
			case PathIterator.SEG_QUADTO:
				break;
			case PathIterator.SEG_CUBICTO:
				break;
			case PathIterator.SEG_CLOSE:
				return;
			default:
				return;
			}
			pit.next();
		} while (!pit.isDone());
	}

	public void drawImage(Graphics g, Image img, Point2f point) {
		g.drawImage(img, fieldPane.F2SX(point.x), fieldPane.F2SY(point.y),
				fieldPane);
	}

	public void drawImage(Graphics g, Image img, Point2f point, int offsetx,
			int offsety) {
		g.drawImage(img, fieldPane.F2SX(point.x) + offsetx, fieldPane
				.F2SY(point.y)
				+ offsety, fieldPane);
	}

}
