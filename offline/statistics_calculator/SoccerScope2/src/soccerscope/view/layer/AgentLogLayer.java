/*
 * $Header: $
 */

package soccerscope.view.layer;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.AbstractButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import soccerscope.model.Param;
import soccerscope.model.agentplan.AgentPlanCommand;
import soccerscope.view.FieldPane;

public class AgentLogLayer extends Layer {
	private int mask = 8;

	public AgentLogLayer(FieldPane fieldPane, boolean enable) {
		super(fieldPane, enable);
	}

	public String getLayerName() {
		return "AgentLog";
	}

	public JPanel getConfigPanel() {
		JPanel panel = new JPanel();

		for (int i = 0; i < 5; i++) {
			JToggleButton lv = new JToggleButton(String.valueOf((int) (i + 1)));
			lv.addActionListener(new AgentLogLayerActionListener());
			if ((mask & (1 << i + 1)) != 0)
				lv.setSelected(true);
			panel.add(lv);
		}
		return panel;
	}

	public int getLevelMask() {
		return mask;
	}

	public void setLevelMask(int mask) {
		this.mask = mask;
		fieldPane.repaint();
	}

	public void setLevel(int level) {
		this.mask ^= 1 << level;
		fieldPane.repaint();
	}

	public void draw(Graphics g) {
		if (!enable)
			return;

		for (int i = 0; i < Param.MAX_PLAYER; i++) {
			if (fieldPane.getAgentPlan(i) == null)
				continue;

			ArrayList list = fieldPane.getAgentPlan(i);
			Iterator it = list.iterator();
			while (it.hasNext()) {
				AgentPlanCommand apc = (AgentPlanCommand) it.next();
				if ((1 << apc.getLevel() & mask) != 0)
					apc.execute(g, this);
			}
		}
	}

	class AgentLogLayerActionListener implements ActionListener {
		public void actionPerformed(ActionEvent ie) {
			String label = ((AbstractButton) ie.getSource()).getText();
			int level = Integer.parseInt(label);
			setLevel(level);
		}
	}
}
