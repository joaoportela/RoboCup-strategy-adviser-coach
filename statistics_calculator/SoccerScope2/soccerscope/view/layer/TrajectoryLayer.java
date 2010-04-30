/*
 * $Header: $
 */

package soccerscope.view.layer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import soccerscope.model.Param;
import soccerscope.model.Scene;
import soccerscope.model.SceneSet;
import soccerscope.model.SoccerObjectID;
import soccerscope.model.WorldModel;
import soccerscope.util.geom.Point2f;
import soccerscope.view.FieldPane;

public class TrajectoryLayer extends Layer implements ActionListener {

	public TrajectoryLayer(FieldPane fieldPane, boolean enable) {
		super(fieldPane, enable);
	}

	public String getLayerName() {
		return "Trajectory";
	}

	private JTextField startText;
	private JTextField endText;

	public JPanel getConfigPanel() {
		JLabel startLabel = new JLabel("Start");
		JLabel endLabel = new JLabel("End");
		startText = new JTextField(String.valueOf(start), 4);
		endText = new JTextField(String.valueOf(end), 4);
		JButton apply = new JButton("Apply");
		startText.addActionListener(this);
		endText.addActionListener(this);
		apply.addActionListener(this);

		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		GridBagConstraints gbc1 = new GridBagConstraints();
		GridBagConstraints gbc2 = new GridBagConstraints();
		gbc1.anchor = GridBagConstraints.NORTHWEST;
		gbc1.ipadx = 5;
		gbc1.ipady = 5;
		gbc1.gridx = 0;
		gbc1.weightx = 0.0;
		gbc2.anchor = GridBagConstraints.NORTHWEST;
		gbc2.gridx = 1;
		gbc2.weightx = 1.0;

		p.add(startLabel, gbc1);
		p.add(endLabel, gbc1);

		p.add(startText, gbc2);
		p.add(endText, gbc2);

		gbc1.weighty = 1.0;
		gbc1.gridwidth = 2;
		p.add(apply, gbc1);

		return p;
	}

	public void actionPerformed(ActionEvent ae) {
		try {
			start = Integer.parseInt(startText.getText());
			end = Integer.parseInt(endText.getText());
			fieldPane.repaint();
		} catch (NumberFormatException nfe) {
		}
	}

	private int start = 0;
	private int end = 5;

	public void draw(Graphics g) {
		if (!enable)
			return;

		Scene scene = fieldPane.getScene();
		SceneSet sceneSet = WorldModel.getInstance().getSceneSet();
		float ballMagnify = fieldPane.getBallMagnify();
		float ballMoveMax = Param.BALL_SPEED_MAX + Param.BALL_SPEED_MAX
				* Param.BALL_RAND;
		float playerMagnify = fieldPane.getPlayerMagnify();

		if (scene.ball.isEnable() && fieldPane.isSelected(SoccerObjectID.BALL)) {
			Point2f p1 = scene.ball.pos;
			Point2f p2;
			g.setColor(scene.ball.getColor());
			for (int i = scene.time - 1; i > start; i--) {
				if (sceneSet.hasScene(i)) {
					p2 = sceneSet.getScene(i).ball.pos;
					if (p1.distance(p2) <= ballMoveMax)
						drawLine(g, p1, p2);
					p1 = p2;
				}
			}
		}

		for (int j = 0; j < Param.MAX_PLAYER * 2; j++) {
			if (!scene.player[j].isEnable() || !fieldPane.isSelected(j))
				continue;

			float playerR = scene.player[j].getPlayerSize() * playerMagnify;
			float playerMoveMax = scene.player[j].getMoveMax();
			Color drawColor = scene.player[j].getColor();
			Point2f p1 = scene.player[j].pos;
			Point2f p2;
			g.setColor(drawColor);
			for (int i = scene.time - 1; i > start; i--) {
				if (sceneSet.hasScene(i)) {
					p2 = sceneSet.getScene(i).player[j].pos;
					if (p1.distance(p2) <= playerMoveMax)
						drawLine(g, p1, p2);
					p1 = p2;
				}
			}
		}

	}
}
