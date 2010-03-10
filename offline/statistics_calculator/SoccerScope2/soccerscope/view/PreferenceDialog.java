/*
 * $Header: $
 */
package soccerscope.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.prefs.Preferences;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import soccerscope.SoccerScope;
import soccerscope.model.ColorDB;
import soccerscope.model.GameEvent;
import soccerscope.model.SoccerScopePreferences;
import soccerscope.util.Color2;

public class PreferenceDialog implements ActionListener, ItemListener {

	private SoccerScope soccerscope;

	private JDialog jd;

	private JButton ok;
	private JButton cancel;

	/* GameEvent */
	private JCheckBox event[];
	private JTextField eventOffset[];

	/* Color */
	private JComboBox ballColor;
	private BallIcon ballIcon;
	private JComboBox leftPlayerColor;
	private PlayerIcon leftPlayerIcon;
	private JComboBox leftGoalieColor;
	private PlayerIcon leftGoalieIcon;
	private JComboBox rightPlayerColor;
	private PlayerIcon rightPlayerIcon;
	private JComboBox rightGoalieColor;
	private PlayerIcon rightGoalieIcon;

	/* Publish */
	private JComboBox typeBox;

	public PreferenceDialog(SoccerScope soccerscope) {
		this.soccerscope = soccerscope;

		jd = new JDialog(soccerscope, "Preference", true);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP,
				JTabbedPane.SCROLL_TAB_LAYOUT);
		ok = new JButton("OK");
		ok.addActionListener(this);
		cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		JPanel jp = new JPanel();
		jp.setLayout(new FlowLayout(FlowLayout.RIGHT));
		jp.add(Box.createHorizontalStrut(200));
		jp.add(ok);
		jp.add(cancel);
		jd.getContentPane().setLayout(new BorderLayout());
		jd.getContentPane().add(tabbedPane, BorderLayout.CENTER);
		jd.getContentPane().add(jp, BorderLayout.SOUTH);

		/* GameEvent Tab */
		event = new JCheckBox[GameEvent.MAX];
		eventOffset = new JTextField[GameEvent.MAX];
		for (int i = GameEvent.MIN; i < GameEvent.MAX; i++) {
			event[i] = new JCheckBox(GameEvent.getEventName(i));
			eventOffset[i] = new JTextField("0", 4);
		}

		JPanel jp1 = new JPanel();
		jp1.setLayout(new GridBagLayout());
		GridBagConstraints gbc1 = new GridBagConstraints();
		GridBagConstraints gbc2 = new GridBagConstraints();
		gbc1.gridx = 0;
		gbc1.gridy = GridBagConstraints.RELATIVE;
		gbc1.ipadx = 5;
		gbc1.ipady = 5;
		gbc1.fill = GridBagConstraints.HORIZONTAL;
		gbc1.anchor = GridBagConstraints.NORTHWEST;

		gbc2.gridx = 1;
		gbc2.gridy = GridBagConstraints.RELATIVE;
		gbc2.ipadx = 5;
		gbc2.ipady = 5;
		gbc2.fill = GridBagConstraints.HORIZONTAL;
		gbc2.anchor = GridBagConstraints.NORTHWEST;

		jp1.add(new JLabel("EventType"), gbc1);
		jp1.add(new JLabel("play offset"), gbc2);

		for (int i = GameEvent.MIN; i < GameEvent.MAX; i++) {
			jp1.add(event[i], gbc1);
			jp1.add(eventOffset[i], gbc2);
		}

		tabbedPane.add("GameEvent", jp1);

		/* Color */
		String colors[] = { "black", "blue", "cyan", "darkGray", "gray",
				"green", "lightGray", "magenta", "orange", "pink", "red",
				"white", "yellow", "gold", "purple", "snow", "custom" };
		ballColor = new JComboBox(colors);
		ballIcon = new BallIcon(ColorDB.getColor("ball"));
		leftPlayerColor = new JComboBox(colors);
		leftPlayerIcon = new PlayerIcon(ColorDB.getColor("team_l_color"));
		leftGoalieColor = new JComboBox(colors);
		leftGoalieIcon = new PlayerIcon(ColorDB.getColor("goalie_l_color"));
		rightPlayerColor = new JComboBox(colors);
		rightPlayerIcon = new PlayerIcon(ColorDB.getColor("team_r_color"));
		rightGoalieColor = new JComboBox(colors);
		rightGoalieIcon = new PlayerIcon(ColorDB.getColor("goalie_r_color"));

		JPanel jp2 = new JPanel();
		jp2.setLayout(new GridBagLayout());
		jp2.add(new JLabel("Ball", ballIcon, JLabel.LEFT), gbc1);
		jp2.add(ballColor, gbc2);
		jp2.add(new JLabel("Left Player", leftPlayerIcon, JLabel.LEFT), gbc1);
		jp2.add(leftPlayerColor, gbc2);
		jp2.add(new JLabel("Left Goalie", leftGoalieIcon, JLabel.LEFT), gbc1);
		jp2.add(leftGoalieColor, gbc2);
		jp2.add(new JLabel("Right Player", rightPlayerIcon, JLabel.LEFT), gbc1);
		jp2.add(rightPlayerColor, gbc2);
		jp2.add(new JLabel("Right Goalie", rightGoalieIcon, JLabel.LEFT), gbc1);
		jp2.add(rightGoalieColor, gbc2);

		tabbedPane.add("Color", jp2);

		/* Publish */
		JPanel jp3 = new JPanel();

		String type[] = javax.imageio.ImageIO.getWriterFormatNames();
		typeBox = new JComboBox(type);
		jp3.add(typeBox);

		tabbedPane.add("Publish", jp3);

		jd.pack();
	}

	public void show() {

		/* GameEvent Tab */
		ScenePlayer sp = soccerscope.getScenePlayer();
		for (int i = GameEvent.MIN; i < GameEvent.MAX; i++) {
			event[i].setSelected(sp.getEventEnabled(i));
			eventOffset[i].setText(String.valueOf(sp.getEventOffset(i)));
		}

		/* Color */
		if (Color2.getColorName(ColorDB.getColor("ball")) != null)
			ballColor.setSelectedItem(Color2.getColorName(ColorDB
					.getColor("ball")));
		else
			ballColor.setSelectedItem("custom");
		if (Color2.getColorName(ColorDB.getColor("team_l_color")) != null)
			leftPlayerColor.setSelectedItem(Color2.getColorName(ColorDB
					.getColor("team_l_color")));
		else
			leftPlayerColor.setSelectedItem("custom");
		if (Color2.getColorName(ColorDB.getColor("goalie_l_color")) != null)
			leftGoalieColor.setSelectedItem(Color2.getColorName(ColorDB
					.getColor("goalie_l_color")));
		else
			leftGoalieColor.setSelectedItem("custom");
		if (Color2.getColorName(ColorDB.getColor("team_r_color")) != null)
			rightPlayerColor.setSelectedItem(Color2.getColorName(ColorDB
					.getColor("team_r_color")));
		else
			rightPlayerColor.setSelectedItem("custom");
		if (Color2.getColorName(ColorDB.getColor("goalie_r_color")) != null)
			rightGoalieColor.setSelectedItem(Color2.getColorName(ColorDB
					.getColor("goalie_r_color")));
		else
			rightGoalieColor.setSelectedItem("custom");
		ballColor.addItemListener(this);
		leftPlayerColor.addItemListener(this);
		leftGoalieColor.addItemListener(this);
		rightPlayerColor.addItemListener(this);
		rightGoalieColor.addItemListener(this);

		/* Publish */
		Preferences pf = SoccerScopePreferences.getPreferences();
		String ptype = pf.get("PublishType", "png");
		if (ptype != null)
			typeBox.setSelectedItem(ptype);

		jd.setLocationRelativeTo(null);
		jd.show();
	}

	public void actionPerformed(ActionEvent ae) {
		Object o = ae.getSource();

		if (o == cancel) {
			jd.dispose();
			return;
		}

		Preferences pf = SoccerScopePreferences.getPreferences();

		/* GameEvent Tab */
		ScenePlayer sp = soccerscope.getScenePlayer();
		for (int i = GameEvent.MIN; i < GameEvent.MAX; i++) {
			sp.setEventEnabled(i, event[i].isSelected());
			try {
				int offset = Integer.parseInt(eventOffset[i].getText());
				sp.setEventOffset(i, offset);
			} catch (NumberFormatException nfe) {
				System.err.println("invalid integer: "
						+ GameEvent.getEventName(i) + "Offset("
						+ eventOffset[i].getText() + ")");
			}
		}

		/* Color */
		ColorDB.putColor("ball", ballIcon.getColor());
		ColorDB.putColor("team_l_color", leftPlayerIcon.getColor());
		ColorDB.putColor("goalie_l_color", leftGoalieIcon.getColor());
		ColorDB.putColor("team_r_color", rightPlayerIcon.getColor());
		ColorDB.putColor("goalie_r_color", rightGoalieIcon.getColor());

		/* Publish */
		pf.put("PublishType", (String) typeBox.getSelectedItem());

		jd.dispose();
	}

	public void itemStateChanged(ItemEvent ie) {
		if (ie.getStateChange() != ItemEvent.SELECTED)
			return;

		Object o = ie.getSource();

		String colorname = (String) ((JComboBox) o).getSelectedItem();
		Color c;
		if (colorname == "custom") {
			c = JColorChooser.showDialog(null, "Select Color", Color.white);
		} else {
			c = Color2.getColor(colorname);
		}
		if (o == ballColor) {
			ballIcon.setColor(c);

		} else if (o == leftPlayerColor) {
			leftPlayerIcon.setColor(c);

		} else if (o == leftGoalieColor) {
			leftGoalieIcon.setColor(c);

		} else if (o == rightPlayerColor) {
			rightPlayerIcon.setColor(c);

		} else if (o == rightGoalieColor) {
			rightGoalieIcon.setColor(c);

		}
		jd.repaint();
	}
}
