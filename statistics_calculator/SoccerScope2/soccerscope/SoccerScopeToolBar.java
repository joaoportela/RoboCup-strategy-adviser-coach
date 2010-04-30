/*
 * $Header: $
 */

package soccerscope;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.table.TableModel;

import soccerscope.file.GameLogFileFilter;
import soccerscope.model.Scene;
import soccerscope.model.SceneSetMaker;
import soccerscope.model.WorldModel;
import soccerscope.net.SoccerServerConnection;
import soccerscope.util.ButtonManager;
import soccerscope.util.GameAnalyzer;
import soccerscope.view.FieldPane;
import soccerscope.view.ScopePane;

public class SoccerScopeToolBar extends JToolBar implements ActionListener,
		ItemListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private SoccerScope soccerscope;

	// ToolBar Item
	private JButton openButton;
	private JButton printButton;
	private JButton connectButton;
	private JButton disconnectButton;
	private JButton x6Button;
	private JButton x10Button;
	private JButton x24Button;
	private JButton x46Button;
	private JButton fitButton;
	private JButton sxgaButton;
	private JButton zoominButton;
	private JButton zoomoutButton;
	private JButton zoomBallButton;
	private JToggleButton dogBallButton;
	private ButtonGroup mouseButtonGroup;
	private JToggleButton selectButton;
	private JToggleButton magnifyButton;
	private JToggleButton handButton;

	public SoccerScopeToolBar(SoccerScope soccerscope) {
		this.soccerscope = soccerscope;
		//ScopePane scopePane = soccerscope.getScopePane();
		//FieldPane fieldPane = scopePane.getFieldPane();

		this.setFloatable(false);

		openButton = new JButton(new ImageIcon(ClassLoader
				.getSystemResource("soccerscope/image/general/Open24.gif")));
		printButton = new JButton(new ImageIcon(ClassLoader
				.getSystemResource("soccerscope/image/general/Print24.gif")));
		connectButton = new JButton(new ImageIcon(ClassLoader
				.getSystemResource("soccerscope/image/connect_creating.gif")));
		disconnectButton = new JButton(new ImageIcon(ClassLoader
				.getSystemResource("soccerscope/image/connect_no.gif")));

		// ����Ψ�Υܥ���
		x6Button = new JButton("x6");
		x10Button = new JButton("x10");
		x24Button = new JButton("x24");
		x46Button = new JButton("x46");
		fitButton = new JButton("fit");
		sxgaButton = new JButton("SXGA");
		zoominButton = new JButton(new ImageIcon(ClassLoader
				.getSystemResource("soccerscope/image/general/ZoomIn24.gif")));
		zoomoutButton = new JButton(new ImageIcon(ClassLoader
				.getSystemResource("soccerscope/image/general/ZoomOut24.gif")));
		zoomBallButton = new JButton(new ImageIcon(ClassLoader
				.getSystemResource("soccerscope/image/ZoomBall24.gif")));
		dogBallButton = new JToggleButton(new ImageIcon(ClassLoader
				.getSystemResource("soccerscope/image/WatchBall24.gif")));

		// �ޥ����⡼�ɤΥܥ���
		selectButton = new JToggleButton(new ImageIcon(ClassLoader
				.getSystemResource("soccerscope/image/cursor.gif")), true);
		selectButton.setMargin(new Insets(4, 4, 4, 4));
		magnifyButton = new JToggleButton(new ImageIcon(ClassLoader
				.getSystemResource("soccerscope/image/general/Zoom24.gif")));
		magnifyButton.setMargin(new Insets(0, 0, 0, 0));
		handButton = new JToggleButton(new ImageIcon(ClassLoader
				.getSystemResource("soccerscope/image/palette_hand.gif")));
		handButton.setMargin(new Insets(4, 4, 4, 4));
		mouseButtonGroup = new ButtonGroup();
		mouseButtonGroup.add(selectButton);
		mouseButtonGroup.add(magnifyButton);
		mouseButtonGroup.add(handButton);

		this.add(openButton);
		this.add(printButton);
		this.addSeparator();
		this.add(connectButton);
		this.add(disconnectButton);
		this.addSeparator();
		this.add(x6Button);
		this.add(x10Button);
		this.add(x24Button);
		this.add(x46Button);
		this.add(fitButton);
		this.add(sxgaButton);
		this.add(zoominButton);
		this.add(zoomoutButton);
		this.add(zoomBallButton);
		this.add(dogBallButton);
		this.addSeparator();
		this.add(selectButton);
		this.add(magnifyButton);
		this.add(handButton);
		openButton.addActionListener(this);
		printButton.addActionListener(this);
		connectButton.addActionListener(this);
		disconnectButton.addActionListener(this);
		x6Button.addActionListener(this);
		x10Button.addActionListener(this);
		x24Button.addActionListener(this);
		x46Button.addActionListener(this);
		fitButton.addActionListener(this);
		sxgaButton.addActionListener(this);
		zoominButton.addActionListener(this);
		zoomoutButton.addActionListener(this);
		zoomBallButton.addActionListener(this);
		dogBallButton.addItemListener(this);
		selectButton.addItemListener(this);
		magnifyButton.addItemListener(this);
		handButton.addItemListener(this);

		ButtonManager.addButton("network", disconnectButton);
	}

	// / ActionListener
	public void actionPerformed(ActionEvent ae) {
		Object o = ae.getSource();
		ScopePane scopePane = soccerscope.getScopePane();
		FieldPane fieldPane = scopePane.getFieldPane();

		if (o == openButton) {
			// �?�ե�����򳫤�
			JFileChooser chooser = new JFileChooser(soccerscope.getChooseFile());
			chooser.setFileFilter(new GameLogFileFilter());
			chooser.setSelectedFile(soccerscope.getChooseFile());
			int returnVal = chooser.showOpenDialog(soccerscope);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				soccerscope.getScenePlayer().stop();
				File chooseFile = chooser.getSelectedFile();
				if (chooseFile == null)
					return;
				soccerscope.setChooseFile(chooseFile);
				WorldModel wm = WorldModel.getInstance();
				soccerscope.getScenePlayer().head();
				wm.clear();
				soccerscope.setTitle("SoccerScope: " + chooseFile.getName());
				soccerscope.openLogFile(wm.getSceneSet(), chooseFile.getPath());
				ButtonManager.setEnabled("logfile", true);
			}

		} else if (o == connectButton) {
			// ���ꤵ�줿�ۥ��Ȥ���³����
			String host = JOptionPane.showInputDialog("Input Host");
			if (host != null) {
				try {
					WorldModel wm = WorldModel.getInstance();
					soccerscope
							.setSoccerServerConnection(new SoccerServerConnection(
									host));
					SceneSetMaker ssm = new SceneSetMaker(soccerscope
							.getSoccerServerConnection(), wm.getSceneSet());
					try {
						soccerscope.getSoccerServerConnection().dispinit();
						Thread.sleep(1000);
						soccerscope.getScenePlayer().sync();
						ssm.start();
						ButtonManager.setEnabled("network", true);
					} catch (InterruptedException iie) {
					} catch (IOException ie) {
						System.err.println(ie);
					}
				} catch (SocketException se) {
					System.err.println(se);
				} catch (UnknownHostException uhe) {
					System.err.println(uhe);
				}
			}

		} else if (o == disconnectButton) {
			// �����ФȤ���³�����Ǥ���
			try {
				soccerscope.getSoccerServerConnection().dispbye();
				soccerscope.setSoccerServerConnection(null);
				ButtonManager.setEnabled("network", false);
			} catch (IOException ie) {
				System.err.println(ie);
			}

		} else if (o == printButton) {
			// ���ߤ�����ΰ��֤�ɸ����Ϥ˽��Ϥ���
			// scopePane.printOut();
			TableModel tb = GameAnalyzer.getTableModel();
			System.out.println();
			for (int i = 0; i < tb.getRowCount(); i++) {
				for (int j = 0; j < tb.getColumnCount(); j++) {
					System.out.print(tb.getValueAt(i, j) + ", ");
				}
				System.out.println();
			}

			// tb = GameAnalyzer.getLeftTableModel();
			// System.out.println();
			// for (int i = 0; i < tb.getRowCount(); i++) {
			// for (int j = 0; j < tb.getColumnCount(); j++) {
			// System.out.print(tb.getValueAt(i, j) + ", ");
			// }
			// System.out.println();
			// }

			// tb = GameAnalyzer.getRightTableModel();
			// System.out.println();
			// for (int i = 0; i < tb.getRowCount(); i++) {
			// for (int j = 0; j < tb.getColumnCount(); j++) {
			// System.out.print(tb.getValueAt(i, j) + ", ");
			// }
			// System.out.println();
			// }

		} else if (o == x6Button) {
			scopePane.setMagnify(6.0f, 7.2f, 3.6f);
		} else if (o == x10Button) {
			scopePane.setMagnify(10.0f, 5.0f, 2.0f);
		} else if (o == x24Button) {
			scopePane.setMagnify(24.0f, 1.8f, 1.3f);
		} else if (o == x46Button) {
			scopePane.setMagnify(46.0f, 1.0f, 1.0f);
		} else if (o == fitButton) {
			scopePane.fitMagnify();
		} else if (o == zoomBallButton) {
			Scene s = soccerscope.getScenePlayer().getCurrentScene();
			if (fieldPane.getWatchPoint().equals(s.ball.pos)
					&& fieldPane.getMagnify() == 46.0f) {
				scopePane.fitMagnify();
			} else {
				scopePane.getFieldPane().setWatchPoint(s.ball.pos);
				scopePane.setMagnify(46.0f, 1.0f, 1.0f);
			}
		} else if (o == sxgaButton) {
			scopePane.setMagnify(10.5f, 6.0f, 3.0f);
		} else if (o == zoominButton) {
			scopePane.setMagnify(scopePane.getMagnify() + 5);
		} else if (o == zoomoutButton) {
			scopePane.setMagnify(scopePane.getMagnify() - 5);
		}
	}

	// / ItemListener
	public void itemStateChanged(ItemEvent ie) {
		Object o = ie.getSource();
		ScopePane scopePane = soccerscope.getScopePane();

		if (o == selectButton) {
			scopePane.setMouseMode(ScopePane.SELECT_MODE);

		} else if (o == magnifyButton) {
			scopePane.setMouseMode(ScopePane.MAGNIFY_MODE);

		} else if (o == handButton) {
			scopePane.setMouseMode(ScopePane.HAND_MODE);

		} else if (o == dogBallButton) {
			scopePane.setBallDogMode(dogBallButton.isSelected());
		}
	}

}
