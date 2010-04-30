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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import soccerscope.model.Player;
import soccerscope.model.Scene;
import soccerscope.util.Color2;
import soccerscope.util.geom.Circle2f;
import soccerscope.util.geom.Geometry;
import soccerscope.util.geom.Line2f;
import soccerscope.util.geom.Point2f;
import soccerscope.util.geom.Tuple2f;
import soccerscope.view.FieldPane;

public class DominantRegionLayer extends Layer implements ActionListener,
		ItemListener {

	public DominantRegionLayer(FieldPane fieldPane, boolean enable) {
		super(fieldPane, enable);
		init();
	}

	public String getLayerName() {
		return "DominantRegion";
	}

	private JTextField gridxText;
	private JTextField gridyText;
	private JComboBox methodBox;
	private JCheckBox timeCheck;
	private JCheckBox countCheck;
	private JTextField countText;

	public JPanel getConfigPanel() {
		JLabel gridxLabel = new JLabel("Grid X");
		JLabel gridyLabel = new JLabel("Grid Y");
		gridxText = new JTextField(String.valueOf(gridx), 4);
		gridyText = new JTextField(String.valueOf(gridy), 4);
		JLabel methodLabel = new JLabel("Method");
		String str[] = { "Simple", "Simple+Est", "DDT", "DDT+Est" };
		methodBox = new JComboBox(str);
		methodBox.setSelectedIndex(3);
		timeCheck = new JCheckBox("Draw Time");
		countCheck = new JCheckBox("Count Region");
		countText = new JTextField(String.valueOf(countR), 4);
		JButton apply = new JButton("Apply");
		gridxText.addActionListener(this);
		gridyText.addActionListener(this);
		methodBox.addItemListener(this);
		timeCheck.addItemListener(this);
		countCheck.addItemListener(this);
		countText.addActionListener(this);
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

		p.add(gridxLabel, gbc1);
		p.add(gridyLabel, gbc1);
		p.add(methodLabel, gbc1);

		p.add(gridxText, gbc2);
		p.add(gridyText, gbc2);
		p.add(methodBox, gbc2);

		gbc1.weighty = 1.0;
		gbc1.gridwidth = 2;
		p.add(timeCheck, gbc1);
		p.add(countCheck, gbc1);
		p.add(countText, gbc1);
		p.add(apply, gbc1);

		return p;
	}

	public void actionPerformed(ActionEvent ae) {
		try {
			gridx = Float.parseFloat(gridxText.getText());
			gridy = Float.parseFloat(gridyText.getText());
			countR = Float.parseFloat(countText.getText());
			init();
			fieldPane.repaint();
		} catch (NumberFormatException nfe) {
		}
	}

	public void itemStateChanged(ItemEvent ie) {
		if (ie.getSource() == methodBox) {
			method = methodBox.getSelectedIndex();
			String str[] = { "Simple", "Simple+Est", "DDT", "DDT+Est" };
			System.out.println(str[method]);
		} else if (ie.getSource() == timeCheck) {
			drawTime = timeCheck.isSelected();
		} else if (ie.getSource() == countCheck) {
			countRegion = countCheck.isSelected();
		}
	}

	private int MAX_TIME = 110;
	private int MAX_TURN = 7;

	private void init() {
		maxx = (int) (PITCH_LENGTH / gridx);
		maxy = (int) (PITCH_WIDTH / gridy);
		field = new Region[maxy][maxx];
		for (int y = 0; y < maxy; y++) {
			field[y] = new Region[maxx];
			for (int x = 0; x < maxx; x++) {
				field[y][x] = new Region();
			}
		}
		DD = new float[MAX_TURN][MAX_PLAYER * 2][MAX_TIME];
		for (int i = 0; i < MAX_TURN; i++) {
			DD[i] = new float[MAX_PLAYER * 2][MAX_TIME];
			for (int j = 0; j < MAX_PLAYER * 2; j++)
				DD[i][j] = new float[MAX_TIME];
		}
	}

	// private float gridx = 4.0f;
	// private float gridy = 4.0f;
	// private float gridx = 2.0f;
	// private float gridy = 2.0f;
	private float gridx = 1.0f;
	private float gridy = 1.0f;
	// private float gridx = 0.5f;
	// private float gridy = 0.5f;
	// private float gridx = 0.25f;
	// private float gridy = 0.25f;
	private Region field[][];
	private int maxx;
	private int maxy;
	private Point2f target = new Point2f();
	private int method = 3;
	private boolean drawTime = false;
	private boolean countRegion = false;
	private float countR = 5.0f;

	public void draw(Graphics g) {
		if (!enable)
			return;

		long time, diff;

		time = System.currentTimeMillis();
		initRegion();
		diff = System.currentTimeMillis();
		System.out.println("init: " + (diff - time));

		time = diff;
		switch (method) {
		case 0:
			calcRegionBySimpleMethod();
			break;
		case 1:
			calcRegionBySimpleMethodWithEstimation();
			break;
		case 2:
			calcRegionByDDT();
			break;
		case 3:
		default:
			calcRegionByDDTWithEstimation();
			break;
		}
		diff = System.currentTimeMillis();
		System.out.println("calc: " + (diff - time));

		time = diff;
		drawRegion(g);
		diff = System.currentTimeMillis();
		System.out.println("draw: " + (diff - time));
	}

	private void drawRegion(Graphics g) {
		Color drawColor;
		Scene scene = fieldPane.getScene();
		ArrayList countRegionList = new ArrayList();
		int cleft, cright, cmid;
		cleft = 0;
		cright = 0;
		cmid = 0;
		if (countRegion) {
			for (int i = 0; i < MAX_PLAYER * 2; i++) {
				if (scene.player[i].isEnable() && fieldPane.isSelected(i))
					countRegionList.add(new Circle2f(scene.player[i].pos,
							countR));
			}
		}

		Tuple2f size = new Tuple2f(gridx, gridy);
		for (int y = 0; y < maxy; y++) {
			for (int x = 0; x < maxx; x++) {
				target.set(-PITCH_HALF_LENGTH + x * gridx, -PITCH_HALF_WIDTH
						+ y * gridy);
				if (field[y][x].idx == Region.UNKNOWN
						|| field[y][x].time == Region.UNKNOWN)
					continue;
				if (drawTime) {
					g.setColor(Color.darkGray);
					drawString(g, Integer.toString(field[y][x].time), target);
				}
				if (field[y][x].idx2 != Region.UNKNOWN)
					g.setColor(Color2.mix(Color2.mix(
							scene.player[field[y][x].idx].getColor(),
							scene.player[field[y][x].idx2].getColor(), 1, 1),
							Color.darkGray, 10, field[y][x].time));
				else
					g.setColor(Color2.mix(scene.player[field[y][x].idx]
							.getColor(), Color.darkGray, 10, field[y][x].time));
				fillRect(g, target, size);
				if (countRegion) {
					Iterator it = countRegionList.iterator();
					count: {
						while (it.hasNext()) {
							if (((Circle2f) it.next()).contains(target)) {
								if (field[y][x].idx2 != Region.UNKNOWN)
									cmid++;
								else if (field[y][x].idx < MAX_PLAYER)
									cleft++;
								else
									cright++;
								break count;
							}
						}
					}
				}
			}
		}
		if (countRegion) {
			System.out.println("left: " + cleft);
			System.out.println("mid: " + cmid);
			System.out.println("right: " + cright);
		}
	}

	class Region {
		public final static int UNKNOWN = -1;
		public int idx;
		public int time;
		public int idx2;

		public Region() {
			idx = UNKNOWN;
			time = UNKNOWN;
			idx2 = UNKNOWN;
		}
	}

	// ��������롣time�ν���ͤ�,��Υ���Ǿ������꤬����¤��������+10������
	private void initRegion() {
		Scene scene = fieldPane.getScene();
		float mindist, dist;
		for (int y = 0; y < maxy; y++) {
			for (int x = 0; x < maxx; x++) {
				field[y][x].idx = Region.UNKNOWN;
				field[y][x].time = Region.UNKNOWN;
				target.set(-PITCH_HALF_LENGTH + x * gridx, -PITCH_HALF_WIDTH
						+ y * gridy);
				mindist = 1000.0f;
				for (int i = 0; i < MAX_PLAYER * 2; i++) {
					if (!scene.player[i].isEnable())
						continue;
					dist = scene.player[i].pos.dist(target);
					if (dist < mindist) {
						field[y][x].idx = i;
						field[y][x].idx2 = Region.UNKNOWN;
						field[y][x].time = scene.player[i]
								.estimateMinTimeByDistance(dist,
										scene.player[i]
												.getDashAccelerationMax()) + 10;
						mindist = dist;
					}
				}
			}
		}
	}

	private Player dPlayer = new Player();

	// ���Ѥ���ˡ�Ƿ׻����롣
	// ���٤Ƥ�����ˤ�������ɸ������ã����ޤǤˤ�������֤�׻�����
	private void calcRegionBySimpleMethod() {
		Scene scene = fieldPane.getScene();
		for (int y = 0; y < maxy; y++) {
			for (int x = 0; x < maxx; x++) {
				target.set(-PITCH_HALF_LENGTH + x * gridx, -PITCH_HALF_WIDTH
						+ y * gridy);

				for (int i = 0; i < MAX_PLAYER * 2; i++) {
					if (!scene.player[i].isEnable())
						continue;

					dPlayer.setPlayer(scene.player[i]);
					int time = turnTo(dPlayer, target);
					time += dashTo(dPlayer, target);
					if (field[y][x].time == Region.UNKNOWN
							|| field[y][x].time > time) {
						field[y][x].time = time;
						field[y][x].idx = i;
						field[y][x].idx2 = Region.UNKNOWN;
					} else if (field[y][x].time == time) {
						field[y][x].idx2 = i;
					}
				}
			}
		}
	}

	private void calcRegionBySimpleMethodWithEstimation() {
		Scene scene = fieldPane.getScene();
		for (int y = 0; y < maxy; y++) {
			for (int x = 0; x < maxx; x++) {
				target.set(-PITCH_HALF_LENGTH + x * gridx, -PITCH_HALF_WIDTH
						+ y * gridy);

				for (int i = 0; i < MAX_PLAYER * 2; i++) {
					if (!scene.player[i].isEnable())
						continue;

					float dist = scene.player[i].pos.dist(target);
					float acc = scene.player[i].getDashAccelerationMax();
					float minTime = scene.player[i].estimateMinTimeByDistance(
							dist, acc);
					if (minTime > field[y][x].time)
						continue;

					dPlayer.setPlayer(scene.player[i]);
					int time = turnTo(dPlayer, target);
					time += dashTo(dPlayer, target);
					if (field[y][x].time == Region.UNKNOWN
							|| field[y][x].time > time) {
						field[y][x].time = time;
						field[y][x].idx = i;
						field[y][x].idx2 = Region.UNKNOWN;
					} else if (field[y][x].time == time) {
						field[y][x].idx2 = i;
					}
				}
			}
		}
	}

	private int turnTo(Player p, Point2f target) {
		int t = 0;
		Circle2f cir = new Circle2f(target, p.getKickable());
		while (true) {
			if (p.pos.dist(target) <= p.getKickable())
				break;

			Line2f pline = p.getLine();
			float angle = p.getAngleFromBody(target);
			if (cir.intersect(pline) && Math.abs(angle) <= 90)
				break;
			float turnAngle = p.getTurnAngle();
			p.step();
			if (Math.abs(angle) <= turnAngle)
				p.angle = p.pos.dir(target);
			else if (angle > 0)
				p.angle = Geometry.normalizeAngle(p.angle + turnAngle);
			else
				p.angle = Geometry.normalizeAngle(p.angle - turnAngle);
			t++;
		}
		return t;
	}

	private int dashTo(Player p, Point2f target) {
		if (p.pos.dist(target) <= p.getKickable())
			return 0;

		Circle2f cir = new Circle2f(target, p.getKickable());
		Line2f pline = p.getLine();
		Point2f pos[] = cir.intersection(pline);
		float dist;
		switch (pos.length) {
		case 2:
			dist = Math.min(p.pos.dist(pos[0]), p.pos.dist(pos[1]));
			break;
		case 1:
			dist = p.pos.dist(pos[0]);
			break;
		default:
			dist = p.pos.dist(pline.projectpoint(target));
			break;
		}
		float acc = p.getDashAccelerationMax();
		return p.calcTimeByDistance(dist, acc);
	}

	// Direction-Distance Table��ȤäƷ׻�����
	// �������꤬��ɸ������ã����Τˤ����������֤ˤ��׻��ξ�ά����
	private void calcRegionByDDT() {
		Scene scene = fieldPane.getScene();
		makeDD();
		for (int y = 0; y < maxy; y++) {
			for (int x = 0; x < maxx; x++) {
				target.set(-PITCH_HALF_LENGTH + x * gridx, -PITCH_HALF_WIDTH
						+ y * gridy);

				for (int i = 0; i < MAX_PLAYER * 2; i++) {
					if (!scene.player[i].isEnable())
						continue;

					dPlayer.setPlayer(scene.player[i]);
					int time = turnTo(dPlayer, target);
					time += dashToUsingDD(dPlayer, target, time, i);
					if (field[y][x].time == Region.UNKNOWN
							|| field[y][x].time > time) {
						field[y][x].time = time;
						field[y][x].idx = i;
						field[y][x].idx2 = Region.UNKNOWN;
					} else if (field[y][x].time == time) {
						field[y][x].idx2 = i;
					}
				}
			}
		}
	}

	private void calcRegionByDDTWithEstimation() {
		Scene scene = fieldPane.getScene();
		makeDD();
		for (int y = 0; y < maxy; y++) {
			for (int x = 0; x < maxx; x++) {
				target.set(-PITCH_HALF_LENGTH + x * gridx, -PITCH_HALF_WIDTH
						+ y * gridy);

				for (int i = 0; i < MAX_PLAYER * 2; i++) {
					if (!scene.player[i].isEnable())
						continue;

					float dist = scene.player[i].pos.dist(target);
					float acc = scene.player[i].getDashAccelerationMax();
					float minTime = scene.player[i].estimateMinTimeByDistance(
							dist, acc);
					if (minTime > field[y][x].time)
						continue;

					dPlayer.setPlayer(scene.player[i]);
					int time = turnTo(dPlayer, target);
					time += dashToUsingDD(dPlayer, target, time, i);
					if (field[y][x].time == Region.UNKNOWN
							|| field[y][x].time > time) {
						field[y][x].time = time;
						field[y][x].idx = i;
						field[y][x].idx2 = Region.UNKNOWN;
					} else if (field[y][x].time == time) {
						field[y][x].idx2 = i;
					}
				}
			}
		}
	}

	private float DD[][][];

	private void makeDD() {
		Scene scene = fieldPane.getScene();
		for (int i = 0; i < MAX_TURN; i++) {
			for (int j = 0; j < MAX_PLAYER * 2; j++) {
				if (!scene.player[j].isEnable())
					continue;
				dPlayer.setPlayer(scene.player[j]);
				for (int k = 0; k < i; k++)
					dPlayer.step();
				float acc = dPlayer.getDashAccelerationMax();
				for (int k = 0; k < MAX_TIME; k++) {
					DD[i][j][k] = dPlayer.calcDistanceAfterNTime(acc, k);
				}
			}
		}
	}

	private int dashToUsingDD(Player p, Point2f target, int turnTime, int idx) {
		if (p.pos.dist(target) <= p.getKickable()) {
			return 0;
		}

		Circle2f cir = new Circle2f(target, p.getKickable());
		Line2f pline = p.getLine();
		Point2f pos[] = cir.intersection(pline);
		float dist;
		switch (pos.length) {
		case 2:
			dist = Math.min(p.pos.dist(pos[0]), p.pos.dist(pos[1]));
			break;
		case 1:
			dist = p.pos.dist(pos[0]);
			break;
		default:
			dist = p.pos.dist(pline.projectpoint(target));
			break;
		}

		int time = search(DD[turnTime][idx], dist);
		// if (p.unum == 1 && p.side == Team.LEFT_SIDE) {
		// System.out.println("time = " + time);
		// System.out.println(target);
		// System.out.println(dist);
		// System.out.println(cir);
		// System.out.println(pline);
		// System.out.println();
		// }
		return time; // search(DD[turnTime][idx], dist);
	}

	private int search(float[] t, float key) {
		int idx = (int) key;
		float tmp = t[idx];
		if (tmp == key)
			return idx;
		else if (tmp < key) {
			while (tmp < key) {
				idx++;
				tmp = t[idx];
			}
		} else {
			while (tmp > key) {
				idx--;
				tmp = t[idx];
			}
			idx++;
		}
		return idx;
	}

}
