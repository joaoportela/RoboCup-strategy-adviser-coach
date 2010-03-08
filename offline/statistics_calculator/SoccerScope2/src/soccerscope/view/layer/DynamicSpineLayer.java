/*
 * $Header: $
 */

package soccerscope.view.layer;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import soccerscope.model.Param;
import soccerscope.model.Scene;
import soccerscope.model.SceneSet;
import soccerscope.model.Team;
import soccerscope.model.WorldModel;
import soccerscope.util.Color2;
import soccerscope.util.analyze.PassAnalyzer;
import soccerscope.view.FieldPane;

public class DynamicSpineLayer extends Layer implements ItemListener {

	public DynamicSpineLayer(FieldPane fieldPane, boolean enable) {
		super(fieldPane, enable);
		drawLeft = true;
		drawRight = false;
	}

	public String getLayerName() {
		return "Dynamic Spine";
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
		drawLeft = leftBox.isSelected();
		drawRight = rightBox.isSelected();
		fieldPane.repaint();
	}

	private boolean drawLeft;
	private boolean drawRight;
	private BasicStroke stroke1 = new BasicStroke(1);
	private BasicStroke stroke2 = new BasicStroke(2);
	private BasicStroke stroke3 = new BasicStroke(3);
	private BasicStroke stroke4 = new BasicStroke(4);
	private BasicStroke stroke5 = new BasicStroke(5);

	public void draw(Graphics g) {
		if (!enable)
			return;

		Scene scene = fieldPane.getScene();
		SceneSet set = WorldModel.getInstance().getSceneSet();

		ArrayList list = PassAnalyzer.getPassList();
		Iterator it = list.iterator();
		int[][] left = new int[Param.MAX_PLAYER + 1][Param.MAX_PLAYER + 1];
		int[][] right = new int[Param.MAX_PLAYER + 1][Param.MAX_PLAYER + 1];

		while (it.hasNext()) {
			PassAnalyzer.Pass pass = (PassAnalyzer.Pass) it.next();
			if (pass.sender.time >= scene.time)
				continue;
			if (pass.side == Team.LEFT_SIDE && drawLeft) {
				left[pass.sender.unum][pass.receiver.unum]++;
			}
			if (pass.side == Team.RIGHT_SIDE && drawRight) {
				right[pass.sender.unum][pass.receiver.unum]++;
			}
		}

		Graphics2D g2 = (Graphics2D) g;
		if (drawLeft) {
			for (int i = 1; i < Param.MAX_PLAYER + 1; i++)
				for (int j = i + 1; j < Param.MAX_PLAYER + 1; j++) {
					int count = left[i][j] + left[j][i];
					if (count > 10) {
						g2.setColor(Color2.red);
						g2.setStroke(stroke5);
					} else if (count > 5) {
						g2.setColor(Color2.yellow);
						g2.setStroke(stroke3);
					} else if (count > 3) {
						g2.setColor(Color2.green);
						g2.setStroke(stroke1);
					} else if (count > 0) {
						g2.setColor(Color2.lightGray);
						g2.setStroke(stroke1);
					}
					if (count > 0) {
						drawLine(g2, scene.player[i - 1].pos,
								scene.player[j - 1].pos);
					}
				}
		}
		if (drawRight) {
			for (int i = 1; i < Param.MAX_PLAYER + 1; i++)
				for (int j = i + 1; j < Param.MAX_PLAYER + 1; j++) {
					int count = right[i][j] + right[j][i];
					if (count > 10) {
						g2.setColor(Color2.red);
						g2.setStroke(stroke5);
					} else if (count > 5) {
						g2.setColor(Color2.yellow);
						g2.setStroke(stroke3);
					} else if (count > 3) {
						g2.setColor(Color2.green);
						g2.setStroke(stroke1);
					} else if (count > 0) {
						g2.setColor(Color2.lightGray);
						g2.setStroke(stroke1);
					}
					if (count > 0) {
						drawLine(g2,
								scene.player[i - 1 + Param.MAX_PLAYER].pos,
								scene.player[j - 1 + Param.MAX_PLAYER].pos);
					}
				}
		}
		g2.setStroke(new BasicStroke(1));
	}
}
