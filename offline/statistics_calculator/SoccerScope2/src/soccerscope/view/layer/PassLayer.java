/*
 * $Header: $
 */

package soccerscope.view.layer;

import java.awt.Graphics;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import soccerscope.model.ColorDB;
import soccerscope.model.Scene;
import soccerscope.model.SceneSet;
import soccerscope.model.Team;
import soccerscope.model.WorldModel;
import soccerscope.util.analyze.PassAnalyzer;
import soccerscope.util.geom.Point2f;
import soccerscope.view.FieldPane;

public class PassLayer extends Layer implements ItemListener {

	public PassLayer(FieldPane fieldPane, boolean enable) {
		super(fieldPane, enable);
		drawLeft = true;
		drawRight = false;
	}

	public String getLayerName() {
		return "Pass";
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

	public void draw(Graphics g) {
		if (!enable)
			return;

		Scene scene = fieldPane.getScene();
		SceneSet set = WorldModel.getInstance().getSceneSet();

		ArrayList list = PassAnalyzer.getPassList();
		Iterator it = list.iterator();

		while (it.hasNext()) {
			PassAnalyzer.Pass pass = (PassAnalyzer.Pass) it.next();
			if (pass.sender.time >= scene.time)
				continue;
			if (pass.side == Team.LEFT_SIDE && drawLeft) {
				int offset = -1;
				Point2f spos = set.getScene(pass.sender.time).player[pass.sender.unum
						+ offset].pos;
				Point2f epos = set.getScene(pass.receiver.time).player[pass.receiver.unum
						+ offset].pos;
				g.setColor(ColorDB.getColor("team_l_color").darker());
				drawLine(g, spos, epos);
			}
			if (pass.side == Team.RIGHT_SIDE && drawRight) {
				int offset = MAX_PLAYER - 1;
				Point2f spos = set.getScene(pass.sender.time).player[pass.sender.unum
						+ offset].pos;
				Point2f epos = set.getScene(pass.receiver.time).player[pass.receiver.unum
						+ offset].pos;
				g.setColor(ColorDB.getColor("team_r_color").darker());
				drawLine(g, spos, epos);
			}
		}
	}
}
