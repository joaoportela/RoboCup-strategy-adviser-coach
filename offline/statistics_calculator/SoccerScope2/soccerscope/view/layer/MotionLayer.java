/*
 * $Header: $
 */

package soccerscope.view.layer;

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
import soccerscope.util.Color2;
import soccerscope.view.FieldPane;

// ʪ�ΤΥ⡼���������褹��쥤�䡼
public class MotionLayer extends Layer implements ActionListener {

	public MotionLayer(FieldPane fieldPane, boolean enable) {
		super(fieldPane, enable);
	}

	public String getLayerName() {
		return "Motion";
	}

	private JTextField upperText;
	private JTextField lowerText;

	public JPanel getConfigPanel() {
		JLabel upperLabel = new JLabel("after");
		JLabel lowerLabel = new JLabel("before");
		upperText = new JTextField(String.valueOf(upper), 4);
		lowerText = new JTextField(String.valueOf(lower), 4);
		JButton apply = new JButton("Apply");
		upperText.addActionListener(this);
		lowerText.addActionListener(this);
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

		p.add(lowerLabel, gbc1);
		p.add(upperLabel, gbc1);

		p.add(lowerText, gbc2);
		p.add(upperText, gbc2);

		gbc1.weighty = 1.0;
		gbc1.gridwidth = 2;
		p.add(apply, gbc1);

		return p;
	}

	public void actionPerformed(ActionEvent ae) {
		try {
			upper = Integer.parseInt(upperText.getText());
			lower = Integer.parseInt(lowerText.getText());
			fieldPane.repaint();
		} catch (NumberFormatException nfe) {
		}
	}

	private int upper = 5;
	private int lower = 5;

	public void draw(Graphics g) {
		if (!enable)
			return;

		Scene scene = fieldPane.getScene();
		float ballMagnify = fieldPane.getBallMagnify();
		float playerMagnify = fieldPane.getPlayerMagnify();

		float ballR = Param.BALL_SIZE * ballMagnify;
		float playerR = Param.PLAYER_SIZE * playerMagnify;

		WorldModel wm = WorldModel.getInstance();
		SceneSet sceneSet = wm.getSceneSet();

		for (int offset = upper; offset > 0; offset--) {
			if (sceneSet.hasScene(scene.time + offset)) {
				Scene tmpScene = sceneSet.getScene(scene.time + offset);
				if (tmpScene.ball.isEnable()
						&& fieldPane.isSelected(SoccerObjectID.BALL)) {
					g.setColor(Color2.mix(tmpScene.ball.getColor(),
							Color2.forestGreen, 1, offset));
					fillCircle(g, tmpScene.ball.pos, ballR);
				}
				for (int i = 0; i < Param.MAX_PLAYER * 2; i++) {
					if (!tmpScene.player[i].isEnable()
							|| !fieldPane.isSelected(i))
						continue;
					g.setColor(Color2.mix(tmpScene.player[i].getColor(),
							Color2.forestGreen, 1, offset));
					fillCircle(g, tmpScene.player[i].pos, tmpScene.player[i]
							.getPlayerSize()
							* playerMagnify);
				}
			}
		}
		for (int offset = lower; offset > 0; offset--) {
			if (sceneSet.hasScene(scene.time - offset)) {
				Scene tmpScene = sceneSet.getScene(scene.time - offset);
				if (tmpScene.ball.isEnable()
						&& fieldPane.isSelected(SoccerObjectID.BALL)) {
					g.setColor(Color2.mix(tmpScene.ball.getColor(),
							Color2.forestGreen, 1, offset));
					fillCircle(g, tmpScene.ball.pos, ballR);
				}
				for (int i = 0; i < Param.MAX_PLAYER * 2; i++) {
					if (!tmpScene.player[i].isEnable()
							|| !fieldPane.isSelected(i))
						continue;
					g.setColor(Color2.mix(tmpScene.player[i].getColor(),
							Color2.forestGreen, 1, offset));
					fillCircle(g, tmpScene.player[i].pos, tmpScene.player[i]
							.getPlayerSize()
							* playerMagnify);
				}
			}
		}
	}
}
