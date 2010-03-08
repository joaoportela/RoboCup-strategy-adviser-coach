/*
 * $Log: SelectWindow.java,v $
 * Revision 1.3  2002/09/02 07:14:50  koji
 * ButtunManger��Ƴ��
 *
 * Revision 1.2  2002/08/30 10:47:34  koji
 * bmpviewer����Jimi�б�
 *
 * Revision 1.1.1.1  2002/03/01 14:12:52  koji
 * CVS�����
 *
 */

package soccerscope.view;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.TitledBorder;

import soccerscope.model.Param;
import soccerscope.model.Scene;
import soccerscope.model.SoccerObjectID;

public class SelectWindow extends JFrame implements SoccerObjectID,
		ScopeWindow, ActionListener, ItemListener, MouseListener,
		SoccerObjectSelectObserver, SoccerObjectSelector {

	public final static String title = "select";

	private Scene scene;

	private Container container = null;
	private PlayerButton pb[];
	private JCheckBox cb[];
	private JCheckBox ballcb;

	// SoccerObjectSelectObserver�Υꥹ��
	private ArrayList observerList;

	private JPopupMenu popup;
	private JMenuItem selectleft;
	private JMenuItem selectright;
	private JMenuItem deselectleft;
	private JMenuItem deselectright;

	public SelectWindow() {
		// frame title
		super("select");

		// ���?����
		observerList = new ArrayList();

		// ���ߡ��ʥ�����
		scene = Scene.createScene();

		// get content pane
		container = this.getContentPane();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		container.addMouseListener(this);

		JPanel leftp = new JPanel();
		leftp.setLayout(new BoxLayout(leftp, BoxLayout.Y_AXIS));
		leftp.setBorder(new TitledBorder("Left"));
		JPanel rightp = new JPanel();
		rightp.setLayout(new BoxLayout(rightp, BoxLayout.Y_AXIS));
		rightp.setBorder(new TitledBorder("Right"));

		pb = new PlayerButton[Param.MAX_PLAYER * 2];
		cb = new JCheckBox[Param.MAX_PLAYER * 2];
		FlowLayout flowlayout = new FlowLayout(FlowLayout.LEFT, 1, 1);
		for (int i = 0; i < Param.MAX_PLAYER; i++) {
			cb[i] = new JCheckBox();
			cb[i].addItemListener(this);
			pb[i] = new PlayerButton(scene.player[i]);
			pb[i].addActionListener(this);
			JPanel p1 = new JPanel(flowlayout);
			p1.add(cb[i]);
			p1.add(pb[i]);
			leftp.add(p1);
			cb[i + Param.MAX_PLAYER] = new JCheckBox();
			cb[i + Param.MAX_PLAYER].addItemListener(this);
			pb[i + Param.MAX_PLAYER] = new PlayerButton(scene.player[i
					+ Param.MAX_PLAYER]);
			pb[i + Param.MAX_PLAYER].addActionListener(this);
			JPanel p2 = new JPanel(flowlayout);
			p2.add(cb[i + Param.MAX_PLAYER]);
			p2.add(pb[i + Param.MAX_PLAYER]);
			rightp.add(p2);
		}

		JPanel teamp = new JPanel();
		teamp.setLayout(new BoxLayout(teamp, BoxLayout.X_AXIS));
		teamp.add(leftp);
		teamp.add(rightp);
		container.add(teamp);

		ballcb = new JCheckBox("Ball");
		ballcb.addItemListener(this);
		JPanel ballp = new JPanel(flowlayout);
		ballp.setBorder(new TitledBorder("Others"));
		ballp.add(ballcb);
		container.add(ballp);

		pack();

		popup = new JPopupMenu();
		selectleft = new JMenuItem("Select all Left");
		selectleft.addActionListener(this);
		selectright = new JMenuItem("Select all Right");
		selectright.addActionListener(this);
		deselectleft = new JMenuItem("Deselect all Left");
		deselectleft.addActionListener(this);
		deselectright = new JMenuItem("Deselect all Right");
		deselectright.addActionListener(this);
		popup.add(selectleft);
		popup.add(selectright);
		popup.add(deselectleft);
		popup.add(deselectright);
	}

	// ɽ�������������
	public void setScene(Scene scene) {
		this.scene = scene;
		for (int i = 0; i < Param.TOTAL_PLAYER; i++) {
			pb[i].setPlayer(scene.player[i]);
		}
	}

	// observer����Ͽ
	public void addSoccerObjectSelectObserver(SoccerObjectSelectObserver soso) {
		observerList.add(soso);
	}

	// �����Ѳ���observer�����Τ���
	private void deliverStateChanged(int id, boolean flag) {
		Iterator it = observerList.iterator();
		while (it.hasNext())
			((SoccerObjectSelectObserver) it.next()).selectSoccerObject(id,
					flag);
	}

	// �����Ѳ��������
	public void selectSoccerObject(int id, boolean flag) {
		if (id == BALL)
			ballcb.setSelected(flag);
		else
			cb[id].setSelected(flag);
	}

	// ActionListener
	public void actionPerformed(ActionEvent ae) {
		Object o = ae.getSource();
		if (o == selectleft) {
			for (int i = LEFT_1; i <= LEFT_11; i++)
				cb[i].setSelected(true);
		} else if (o == selectright) {
			for (int i = RIGHT_1; i <= RIGHT_11; i++)
				cb[i].setSelected(true);
		} else if (o == deselectleft) {
			for (int i = LEFT_1; i <= LEFT_11; i++)
				cb[i].setSelected(false);
		} else if (o == deselectright) {
			for (int i = RIGHT_1; i <= RIGHT_11; i++)
				cb[i].setSelected(false);
		} else {
			for (int i = 0; i < Param.TOTAL_PLAYER; i++)
				if (o == pb[i])
					cb[i].setSelected(!cb[i].isSelected());
		}
	}

	// ItemListener
	public void itemStateChanged(ItemEvent ie) {
		Object o = ie.getSource();
		if (o == ballcb) {
			deliverStateChanged(BALL, ballcb.isSelected());
			return;
		} else {
			for (int i = 0; i < Param.TOTAL_PLAYER; i++)
				if (o == cb[i]) {
					deliverStateChanged(i, cb[i].isSelected());
					return;
				}
		}
	}

	// MouseListener
	public void mouseClicked(MouseEvent e) {
		// ������å�
		if (e.getModifiers() == InputEvent.META_MASK) {
			popup.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}
}
