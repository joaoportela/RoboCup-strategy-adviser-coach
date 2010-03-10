/*
 * $Log: InformationWindow.java,v $
 * Revision 1.6  2002/10/11 10:41:40  koji
 * �Х�����(HeteroParam),GoalȽ������
 *
 * Revision 1.5  2002/10/04 10:39:20  koji
 * Tuple,Point,Vector�Υѥå������ѹ���Geometry,Circle,Line,Rectangle�ɲ�
 *
 * Revision 1.4  2002/09/02 07:14:49  koji
 * ButtunManger��Ƴ��
 *
 * Revision 1.3  2002/09/02 07:06:19  taku-ako
 * Geometry��Ϣ�Υ饤�֥������
 *
 * Revision 1.2  2002/08/30 10:47:33  koji
 * bmpviewer����Jimi�б�
 *
 * Revision 1.1.1.1  2002/03/01 14:12:52  koji
 * CVS�����
 *
 */

package soccerscope.view;

import java.awt.Container;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.NumberFormat;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import soccerscope.model.Param;
import soccerscope.model.Player;
import soccerscope.model.Scene;
import soccerscope.model.SoccerObjectID;
import soccerscope.model.agentplan.CommentDB;
import soccerscope.util.geom.Point2f;
import soccerscope.util.geom.Tuple2f;

public class InformationWindow extends JFrame implements ScopeWindow,
		SoccerObjectID, SoccerObjectSelectObserver, MouseListener {

	public final static String title = "information";

	private Scene scene;

	private Container container = null;
	private JTabbedPane tabbedPane;
	private int leftNumber;
	private int rightNumber;
	private NumberFormat nf;

	private String tuple2String(Tuple2f tuple) {
		return "(" + nf.format(tuple.x) + ", " + nf.format(tuple.y) + ")";
	}

	public InformationWindow() {
		// frame title
		super("information");

		// �����ѿ��ν��
		leftNumber = LEFT_1;
		rightNumber = RIGHT_1;
		nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);

		// ���ߡ��ʥ�����
		scene = Scene.createScene();

		// get content pane
		container = this.getContentPane();

		tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Left", createLeftPlayerPanel());
		tabbedPane.addTab("Right", createRightPlayerPanel());
		tabbedPane.addTab("Other", createOtherPanel());
		container.add(tabbedPane);

		pack();
		setResizable(false);
	}

	private PlayerButton lpButton;
	private JLabel lpos;
	private JLabel lvel;
	private JLabel lacc;
	private JLabel langle;
	private JLabel lneck;
	private JLabel lvisible;
	private JTextField lmessage;
	private JTextArea lcomment;

	private JPanel createLeftPlayerPanel() {
		JPanel jp = new JPanel();
		jp.setLayout(new BoxLayout(jp, BoxLayout.X_AXIS));
		JPanel jpw = new JPanel();
		jpw.setLayout(new BoxLayout(jpw, BoxLayout.Y_AXIS));
		JPanel jpc = new JPanel();
		jpc.setLayout(new BoxLayout(jpc, BoxLayout.Y_AXIS));
		JPanel jpe = new JPanel();
		jpe.setLayout(new BoxLayout(jpe, BoxLayout.Y_AXIS));
		jp.add(jpw);
		jp.add(Box.createHorizontalStrut(4));
		jp.add(jpc);
		jp.add(Box.createHorizontalStrut(16));
		jp.add(jpe);
		lpButton = new PlayerButton(new Player());
		lpButton.addMouseListener(this);
		JPanel pButtonPanel = new JPanel();
		pButtonPanel.add(lpButton);
		jpw.add(pButtonPanel);
		lpos = new JLabel(new Point2f().toString());
		lvel = new JLabel(new Point2f().toString());
		lacc = new JLabel(new Point2f().toString());
		jpc.add(new JLabel("Position"));
		jpc.add(lpos);
		jpc.add(new JLabel("Velocity"));
		jpc.add(lvel);
		jpc.add(new JLabel("Acceleration"));
		jpc.add(lacc);
		langle = new JLabel("0");
		lneck = new JLabel("0");
		lvisible = new JLabel("0");
		jpe.add(new JLabel("BodyAngle"));
		jpe.add(langle);
		jpe.add(new JLabel("NeckAngle"));
		jpe.add(lneck);
		jpe.add(new JLabel("VisibleAngle"));
		jpe.add(lvisible);

		JPanel leftplayerPanel = new JPanel();
		leftplayerPanel.setLayout(new BoxLayout(leftplayerPanel,
				BoxLayout.Y_AXIS));
		lmessage = new JTextField();
		lcomment = new JTextArea(4, 0);
		JScrollPane jsp = new JScrollPane(lcomment);
		leftplayerPanel.add(jp);
		leftplayerPanel.add(lmessage);
		leftplayerPanel.add(jsp);
		return leftplayerPanel;
	}

	private PlayerButton rpButton;
	private JLabel rpos;
	private JLabel rvel;
	private JLabel racc;
	private JLabel rangle;
	private JLabel rneck;
	private JLabel rvisible;
	private JTextField rmessage;
	private JTextArea rcomment;

	private JPanel createRightPlayerPanel() {
		JPanel jp = new JPanel();
		jp.setLayout(new BoxLayout(jp, BoxLayout.X_AXIS));
		JPanel jpw = new JPanel();
		jpw.setLayout(new BoxLayout(jpw, BoxLayout.Y_AXIS));
		JPanel jpc = new JPanel();
		jpc.setLayout(new BoxLayout(jpc, BoxLayout.Y_AXIS));
		JPanel jpe = new JPanel();
		jpe.setLayout(new BoxLayout(jpe, BoxLayout.Y_AXIS));
		jp.add(jpw);
		jp.add(Box.createHorizontalStrut(4));
		jp.add(jpc);
		jp.add(Box.createHorizontalStrut(16));
		jp.add(jpe);
		rpButton = new PlayerButton(new Player());
		rpButton.addMouseListener(this);
		JPanel pButtonPanel = new JPanel();
		pButtonPanel.add(rpButton);
		jpw.add(pButtonPanel);
		rpos = new JLabel(new Point2f().toString());
		rvel = new JLabel(new Point2f().toString());
		racc = new JLabel(new Point2f().toString());
		jpc.add(new JLabel("Position"));
		jpc.add(rpos);
		jpc.add(new JLabel("Velocity"));
		jpc.add(rvel);
		jpc.add(new JLabel("Acceleration"));
		jpc.add(racc);
		rangle = new JLabel("0");
		rneck = new JLabel("0");
		rvisible = new JLabel("0");
		jpe.add(new JLabel("BodyAngle"));
		jpe.add(rangle);
		jpe.add(new JLabel("NeckAngle"));
		jpe.add(rneck);
		jpe.add(new JLabel("VisibleAngle"));
		jpe.add(rvisible);

		JPanel rightplayerPanel = new JPanel();
		rightplayerPanel.setLayout(new BoxLayout(rightplayerPanel,
				BoxLayout.Y_AXIS));
		rmessage = new JTextField();
		rcomment = new JTextArea(4, 0);
		JScrollPane jsp = new JScrollPane(rcomment);
		rightplayerPanel.add(jp);
		rightplayerPanel.add(rmessage);
		rightplayerPanel.add(jsp);
		return rightplayerPanel;
	}

	private JLabel ballpos;
	private JLabel ballvel;
	private JLabel ballacc;

	private JPanel createOtherPanel() {
		JPanel otherPanel = new JPanel();
		otherPanel.setLayout(new BoxLayout(otherPanel, BoxLayout.Y_AXIS));
		otherPanel.setBorder(new TitledBorder("Ball"));
		ballpos = new JLabel(new Point2f().toString());
		ballvel = new JLabel(new Point2f().toString());
		ballacc = new JLabel(new Point2f().toString());
		otherPanel.add(new JLabel("Position"));
		otherPanel.add(ballpos);
		otherPanel.add(new JLabel("Velocity"));
		otherPanel.add(ballvel);
		otherPanel.add(new JLabel("Acceleration"));
		otherPanel.add(ballacc);
		return otherPanel;
	}

	private void updatePane() {
		lpButton.setPlayer(scene.player[leftNumber]);
		lpos.setText(tuple2String(scene.player[leftNumber].pos));
		lvel.setText(tuple2String(scene.player[leftNumber].vel));
		lacc.setText(tuple2String(scene.player[leftNumber].acc));
		langle.setText(String.valueOf(scene.player[leftNumber].angle));
		lneck.setText(String.valueOf(scene.player[leftNumber].angleNeck));
		lvisible.setText(String.valueOf(scene.player[leftNumber].angleVisible));
		lmessage.setText(scene.player[leftNumber].sayMessage);
		lmessage.setCaretPosition(0);
		lcomment.setText(CommentDB.getComment(scene.time, leftNumber));
		rpButton.setPlayer(scene.player[rightNumber]);
		rpos.setText(tuple2String(scene.player[rightNumber].pos));
		rvel.setText(tuple2String(scene.player[rightNumber].vel));
		racc.setText(tuple2String(scene.player[rightNumber].acc));
		rangle.setText(String.valueOf(scene.player[rightNumber].angle));
		rneck.setText(String.valueOf(scene.player[rightNumber].angleNeck));
		rvisible
				.setText(String.valueOf(scene.player[rightNumber].angleVisible));
		rmessage.setText(scene.player[rightNumber].sayMessage);
		rmessage.setCaretPosition(0);
		rcomment.setText(CommentDB.getComment(scene.time, rightNumber));
		ballpos.setText(scene.ball.pos.toString());
		ballvel.setText(scene.ball.vel.toString());
		ballacc.setText(scene.ball.acc.toString());
	}

	public void setScene(Scene scene) {
		this.scene = scene;
		updatePane();
	}

	// �����Ѳ��������
	public void selectSoccerObject(int id, boolean flag) {
		if (id <= LEFT_11)
			leftNumber = id;
		else if (id <= RIGHT_11)
			rightNumber = id;
		updatePane();
	}

	// MouseListener
	public void mouseClicked(MouseEvent e) {
		Object o = e.getSource();
		// ������å�
		// ���ֹ�򥤥󥯥����
		if (e.getModifiers() == InputEvent.BUTTON1_MASK) {
			if (o == lpButton) {
				leftNumber = (leftNumber + 1) % Param.MAX_PLAYER;
			} else if (o == rpButton) {
				rightNumber = (rightNumber + 1) % Param.MAX_PLAYER
						+ Param.MAX_PLAYER;
			}
		}
		// ������å�
		// ���ֹ��ǥ������
		else if (e.getModifiers() == InputEvent.META_MASK) {
			if (o == lpButton) {
				leftNumber = (leftNumber - 1 + Param.MAX_PLAYER)
						% Param.MAX_PLAYER;
			} else if (o == rpButton) {
				rightNumber = (rightNumber - 1) % Param.MAX_PLAYER
						+ Param.MAX_PLAYER;
			}
		}
		updatePane();
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
