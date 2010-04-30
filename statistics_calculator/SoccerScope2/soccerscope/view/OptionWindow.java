/*
 * $Log: OptionWindow.java,v $
 * Revision 1.9  2002/12/26 08:11:32  koji
 * ��Ψ��ɽ��
 *
 * Revision 1.8  2002/10/22 05:15:13  koji
 * ScopePane:�⡼����˥ꥹ�ʡ����Ѱ�
 *
 * Revision 1.7  2002/10/10 09:43:38  koji
 * ͥ���ΰ�η׻��β��� (Direction-Distance Table)
 *
 * Revision 1.6  2002/10/04 10:39:20  koji
 * Tuple,Point,Vector�Υѥå������ѹ���Geometry,Circle,Line,Rectangle�ɲ�
 *
 * Revision 1.5  2002/09/17 11:06:53  koji
 * Statistics->Misc���ѹ�, Option���鿧����ѥͥ����
 *
 * Revision 1.4  2002/09/05 09:46:38  koji
 * ���٥�ȥ�������ǽ���ɲ�
 *
 * Revision 1.3  2002/09/02 07:14:50  koji
 * ButtunManger��Ƴ��
 *
 * Revision 1.2  2002/09/02 07:06:19  taku-ako
 * Geometry��Ϣ�Υ饤�֥������
 *
 * Revision 1.1.1.1  2002/03/01 14:12:52  koji
 * CVS�����
 *
 */

package soccerscope.view;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import soccerscope.model.Scene;
import soccerscope.util.geom.Point2f;
import soccerscope.view.layer.BallLayer;
import soccerscope.view.layer.Layer;
import soccerscope.view.layer.PlayerLayer;

public class OptionWindow extends JFrame implements ScopeWindow,
		ActionListener, MouseListener, MouseMotionListener, ChangeListener {

	public final static String title = "option";

	private ScopePane scopePane;
	private FieldPane fieldPane;
	private Scene scene;

	private Container container = null;
	private JTabbedPane tabbedPane;
	private JButton zoomin;
	private JButton zoomout;
	private JSlider magnifySlider;
	private JLabel magnifyLabel;
	private JSlider playerSlider;
	private JLabel playerLabel;
	private JSlider ballSlider;
	private JLabel ballLabel;

	public OptionWindow(ScopePane scopePane) {
		// frame title
		super("option");

		// ���?����
		this.scopePane = scopePane;
		FieldPane fp = scopePane.getFieldPane();
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		getContentPane().add(tabbedPane);

		// ���ߡ��ʥ�����
		scene = Scene.createScene();

		/* Field����ѥͥ� */
		fieldPane = new FieldPane(scene);
		fieldPane.addForegroundLayer(new PlayerLayer(fieldPane));
		fieldPane.addForegroundLayer(new BallLayer(fieldPane));
		fieldPane.setMagnify(1.5f, 10.0f, 6.0f);
		fieldPane.addMouseListener(this);
		fieldPane.addMouseMotionListener(this);
		JPanel jp1 = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = 2;
		jp1.add(fieldPane, gbc);
		zoomin = new JButton(new ImageIcon(ClassLoader
				.getSystemResource("soccerscope/image/general/ZoomIn24.gif")));
		zoomin.addActionListener(this);
		zoomout = new JButton(new ImageIcon(ClassLoader
				.getSystemResource("soccerscope/image/general/ZoomOut24.gif")));
		zoomout.addActionListener(this);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.weightx = 0.5;
		jp1.add(zoomin, gbc);
		gbc.gridx = 1;
		jp1.add(zoomout, gbc);
		tabbedPane.addTab("Field", jp1);

		/* ��Ψ����ѥͥ� */
		JPanel jp2 = new JPanel(new GridBagLayout());
		GridBagConstraints gbc2 = new GridBagConstraints();
		magnifyLabel = new JLabel("Field " + (int) fp.getMagnify());
		magnifySlider = new JSlider(JSlider.HORIZONTAL, 1, 500, (int) (fp
				.getMagnify() * 10));
		magnifySlider.addChangeListener(this);
		ballLabel = new JLabel("Ball " + (int) fp.getBallMagnify());
		ballSlider = new JSlider(JSlider.HORIZONTAL, 1, 200, (int) (fp
				.getBallMagnify() * 10));
		ballSlider.addChangeListener(this);
		playerLabel = new JLabel("Player " + (int) fp.getPlayerMagnify());
		playerSlider = new JSlider(JSlider.HORIZONTAL, 1, 200, (int) (fp
				.getPlayerMagnify() * 10));
		playerSlider.addChangeListener(this);
		jp2.add(magnifyLabel, gbc2);
		gbc2.gridy = 1;
		jp2.add(magnifySlider, gbc2);
		gbc2.gridy = 2;
		jp2.add(ballLabel, gbc2);
		gbc2.gridy = 3;
		jp2.add(ballSlider, gbc2);
		gbc2.gridy = 4;
		jp2.add(playerLabel, gbc2);
		gbc2.gridy = 5;
		jp2.add(playerSlider, gbc2);
		tabbedPane.addTab("Magnify", jp2);

		/* Layer����ѥͥ� */
		ArrayList layers = new ArrayList();
		layers.addAll(scopePane.getShowLayer());
		layers.addAll(scopePane.getDrawLayer());
		Iterator it = layers.iterator();
		while (it.hasNext()) {
			Layer layer = (Layer) it.next();
			JPanel panel = layer.getConfigPanel();
			if (panel != null)
				tabbedPane.addTab(layer.getLayerName(), panel);
		}

		pack();
	}

	// ScopeWindow
	public void setScene(Scene scene) {
		this.scene = scene;
		fieldPane.setScene(scene);
		FieldPane fp = scopePane.getFieldPane();
		Point2f watchPoint = fp.getWatchPoint();
		Dimension drawSize = fp.getDrawableSize();
		Point2f topleft = fieldPane
				.fieldToScreen(new Point2f(
						watchPoint.x - drawSize.width / 2.0f, watchPoint.y
								- drawSize.height / 2.0f));

		Rectangle r = new Rectangle((int) topleft.x, (int) topleft.y,
				(int) (drawSize.width * fieldPane.getMagnify()),
				(int) (drawSize.height * fieldPane.getMagnify()));
		ArrayList list = new ArrayList();
		list.add(r);
		fieldPane.clearShapeList();
		fieldPane.setShapeList(list);
		repaint();
	}

	// ActionListener
	public void actionPerformed(ActionEvent ae) {
		Object o = ae.getSource();
		if (o == zoomin) {
			scopePane.setMagnify(scopePane.getMagnify() + 5);
			setScene(this.scene);
		} else if (o == zoomout) {
			scopePane.setMagnify(scopePane.getMagnify() - 5);
			setScene(this.scene);
		}
	}

	// MouseListener
	public void mouseClicked(MouseEvent me) {
	}

	public void mouseEntered(MouseEvent me) {
	}

	public void mouseExited(MouseEvent me) {
	}

	public void mousePressed(MouseEvent me) {
		scopePane.setWatchPoint(fieldPane.screenToField(me.getPoint()));
		setScene(this.scene);
	}

	public void mouseReleased(MouseEvent me) {
	}

	// MouseMotionListener
	public void mouseDragged(MouseEvent me) {
		scopePane.setWatchPoint(fieldPane.screenToField(me.getPoint()));
		setScene(this.scene);
	}

	public void mouseMoved(MouseEvent me) {
	}

	// ChangeListener
	public void stateChanged(ChangeEvent e) {
		JSlider o = (JSlider) e.getSource();
		FieldPane fp = scopePane.getFieldPane();

		if (o == magnifySlider) {
			scopePane.setMagnify(o.getValue() / 10.0f);
			magnifyLabel.setText("Field " + o.getValue() / 10.0f);
		} else if (o == playerSlider) {
			fp.setPlayerMagnify(o.getValue() / 10.0f);
			playerLabel.setText("Player " + o.getValue() / 10.0f);
		} else if (o == ballSlider) {
			fp.setBallMagnify(o.getValue() / 10.0f);
			ballLabel.setText("Ball " + o.getValue() / 10.0f);
		}
	}
}
