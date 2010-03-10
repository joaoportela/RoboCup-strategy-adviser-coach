/*
 * $Header: $
 */

package soccerscope;

import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import soccerscope.file.GameLogFileFilter;
import soccerscope.file.PlanLogFileFilter;
import soccerscope.model.Scene;
import soccerscope.model.SceneSetMaker;
import soccerscope.model.Team;
import soccerscope.model.WorldModel;
import soccerscope.net.SoccerServerConnection;
import soccerscope.util.ButtonManager;
import soccerscope.view.FieldPane;
import soccerscope.view.InformationWindow;
import soccerscope.view.OptionWindow;
import soccerscope.view.PreferenceDialog;
import soccerscope.view.SelectWindow;
import soccerscope.view.StatisticsWindow;
import soccerscope.view.layer.Layer;

public class SoccerScopeMenuBar extends JMenuBar implements ActionListener,
		ItemListener {
	SoccerScope soccerscope = null;

	// MenuBar Item
	private JMenuItem openMI;
	private JMenuItem loadMI;
	private JMenuItem loadplanMI;
	private JMenuItem closeMI;
	private JMenuItem preferenceMI;
	private JMenuItem quitMI;

	private JMenuItem connectMI;
	private JMenuItem connectToHostMI;
	private JMenuItem disconnectMI;
	private JMenuItem kickoffMI;
	private JMenuItem dropballMI;
	private JMenuItem freekickleftMI;
	private JMenuItem freekickrightMI;

	private JCheckBoxMenuItem choiceMI;
	private JCheckBoxMenuItem detailinfoMI;
	private JCheckBoxMenuItem statisticsMI;
	private JCheckBoxMenuItem optionMI;

	private JCheckBoxMenuItem antialiasMI;

	public SoccerScopeMenuBar(SoccerScope soccerscope) {
		this.soccerscope = soccerscope;

		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic('F');
		this.add(fileMenu);
		JMenu monitorMenu = new JMenu("Monitor");
		monitorMenu.setMnemonic('M');
		this.add(monitorMenu);
		JMenu windowMenu = new JMenu("Window");
		windowMenu.setMnemonic('W');
		this.add(windowMenu);
		JMenu viewMenu = new JMenu("View");
		viewMenu.setMnemonic('V');
		this.add(viewMenu);
		JMenu analyzeMenu = new JMenu("Analyze");
		analyzeMenu.setMnemonic('A');
		this.add(analyzeMenu);

		// File Menu
		openMI = new JMenuItem("Open");
		openMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				Event.CTRL_MASK));
		openMI.addActionListener(this);
		loadMI = new JMenuItem("Load AgentWorldModel");
		loadMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
				Event.CTRL_MASK));
		loadMI.addActionListener(this);
		loadplanMI = new JMenuItem("Load AgentLog");
		loadplanMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,
				Event.CTRL_MASK));
		loadplanMI.addActionListener(this);
		closeMI = new JMenuItem("Close");
		closeMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
				Event.CTRL_MASK));
		closeMI.addActionListener(this);
		preferenceMI = new JMenuItem("Preference");
		preferenceMI.addActionListener(this);
		quitMI = new JMenuItem("Quit");
		quitMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
				Event.CTRL_MASK));
		quitMI.addActionListener(this);
		fileMenu.add(openMI);
		fileMenu.add(loadMI);
		fileMenu.add(loadplanMI);
		fileMenu.add(closeMI);
		fileMenu.addSeparator();
		fileMenu.add(preferenceMI);
		fileMenu.addSeparator();
		fileMenu.add(quitMI);
		ButtonManager.addButton("logfile", loadMI);
		ButtonManager.addButton("logfile", loadplanMI);
		ButtonManager.addButton("logfile", closeMI);

		// Monitor Menu
		connectMI = new JMenuItem("Connect to Local");
		connectMI.addActionListener(this);
		connectToHostMI = new JMenuItem("Connect to Host");
		connectToHostMI.addActionListener(this);
		disconnectMI = new JMenuItem("Disconnect");
		disconnectMI.addActionListener(this);
		kickoffMI = new JMenuItem("KickOff");
		kickoffMI.addActionListener(this);
		dropballMI = new JMenuItem("DropBall");
		dropballMI.addActionListener(this);
		freekickleftMI = new JMenuItem("FreeKickLeft");
		freekickleftMI.addActionListener(this);
		freekickrightMI = new JMenuItem("FreeKickRight");
		freekickrightMI.addActionListener(this);
		monitorMenu.add(connectMI);
		monitorMenu.add(connectToHostMI);
		monitorMenu.add(disconnectMI);
		monitorMenu.addSeparator();
		monitorMenu.add(kickoffMI);
		monitorMenu.add(dropballMI);
		monitorMenu.add(freekickleftMI);
		monitorMenu.add(freekickrightMI);

		ButtonManager.addButton("network", disconnectMI);
		ButtonManager.addButton("network", kickoffMI);
		ButtonManager.addButton("network", dropballMI);
		ButtonManager.addButton("network", freekickleftMI);
		ButtonManager.addButton("network", freekickrightMI);

		// Window Menu
		choiceMI = new JCheckBoxMenuItem("Select Player");
		choiceMI.addItemListener(this);
		choiceMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,
				Event.CTRL_MASK));
		detailinfoMI = new JCheckBoxMenuItem("Detail Info");
		detailinfoMI.addItemListener(this);
		detailinfoMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K,
				Event.CTRL_MASK));
		statisticsMI = new JCheckBoxMenuItem("Statistics");
		statisticsMI.addItemListener(this);
		statisticsMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M,
				Event.CTRL_MASK));
		optionMI = new JCheckBoxMenuItem("Option");
		optionMI.addItemListener(this);
		optionMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,
				Event.CTRL_MASK));
		windowMenu.add(choiceMI);
		windowMenu.add(detailinfoMI);
		windowMenu.add(statisticsMI);
		windowMenu.add(optionMI);
		ButtonManager.addButton(SelectWindow.title, choiceMI);
		ButtonManager.addButton(InformationWindow.title, detailinfoMI);
		ButtonManager.addButton(StatisticsWindow.title, statisticsMI);
		ButtonManager.addButton(OptionWindow.title, optionMI);

		// View Menu
		ArrayList showLayer = soccerscope.getScopePane().getShowLayer();
		Iterator showit = showLayer.iterator();
		while (showit.hasNext()) {
			viewMenu.add(((Layer) showit.next()).createJCheckBoxMenuItem());
		}
		viewMenu.addSeparator();
		antialiasMI = new JCheckBoxMenuItem("Antialias");
		antialiasMI.setSelected(soccerscope.getScopePane().getFieldPane()
				.getDrawOption(FieldPane.DRAW_ANTIALIAS));
		antialiasMI.addItemListener(this);
		viewMenu.add(antialiasMI);

		// Analyze Menu
		ArrayList drawLayer = soccerscope.getScopePane().getDrawLayer();
		Iterator drawit = drawLayer.iterator();
		while (drawit.hasNext()) {
			analyzeMenu.add(((Layer) drawit.next()).createJCheckBoxMenuItem());
		}
	}

	// / ActionListener
	public void actionPerformed(ActionEvent ae) {
		Object o = ae.getSource();

		// �?�ե�����򳫤�
		if (o == openMI) {
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

		} else if (o == loadMI) {
			JFileChooser chooser = new JFileChooser(soccerscope.getChooseFile());
			chooser.setFileFilter(new GameLogFileFilter());
			int returnVal = chooser.showOpenDialog(soccerscope);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				soccerscope.getScenePlayer().stop();
				File chooseFile = chooser.getSelectedFile();
				if (chooseFile == null)
					return;
				soccerscope.setChooseFile(chooseFile);
				WorldModel wm = WorldModel.getInstance();
				wm.clearAgentWorldModel();
				soccerscope.openLogFile(wm.getAgentWorldModel(0), chooseFile
						.getPath());
				ButtonManager.setEnabled("AgentWorldModel", true);
			}

			// ����������ȥץ����ɤ߹���
		} else if (o == loadplanMI) {
			JFileChooser chooser = new JFileChooser(soccerscope.getChooseFile());
			chooser.setFileFilter(new PlanLogFileFilter());
			int returnVal = chooser.showOpenDialog(soccerscope);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				soccerscope.getScenePlayer().stop();
				File chooseFile = chooser.getSelectedFile();
				if (chooseFile == null)
					return;
				soccerscope.setChooseFile(chooseFile);
				WorldModel wm = WorldModel.getInstance();
				wm.clearAgentPlan();
				wm.makeAgentPlan(chooseFile.getPath());
				ButtonManager.setEnabled("AgentLog", true);
			}

			// �?�ե�������Ĥ���
		} else if (o == closeMI) {
			soccerscope.getScenePlayer().stop();
			soccerscope.getScenePlayer().head();
			WorldModel wm = WorldModel.getInstance();
			wm.clear();
			soccerscope.setTitle("SoccerScope");
			ButtonManager.setEnabled("logfile", false);
			ButtonManager.setEnabled("AgentWorldModel", false);
			ButtonManager.setEnabled("AgentLog", false);

			// soccerscope��λ����
		} else if (o == quitMI) {
			System.exit(0);

			// ����������?��ɽ������
		} else if (o == preferenceMI) {
			PreferenceDialog pd = new PreferenceDialog(soccerscope);
			pd.show();
			soccerscope.repaint();

			// �ǥե���ȥۥ��Ȥ���³����
		} else if (o == connectMI) {
			try {
				WorldModel wm = WorldModel.getInstance();
				soccerscope
						.setSoccerServerConnection(new SoccerServerConnection());
				SceneSetMaker ssm = new SceneSetMaker(soccerscope
						.getSoccerServerConnection(), wm.getSceneSet());
				try {
					soccerscope.getSoccerServerConnection().dispinit();
					Thread.currentThread().sleep(1000);
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

			// ���ꤵ�줿�ۥ��Ȥ���³����
		} else if (o == connectToHostMI) {
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
						Thread.currentThread().sleep(1000);
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

			// �����ФȤ���³�����Ǥ���
		} else if (o == disconnectMI) {
			try {
				soccerscope.getSoccerServerConnection().dispbye();
				soccerscope.setSoccerServerConnection(null);
				ButtonManager.setEnabled("network", false);
			} catch (IOException ie) {
				System.err.println(ie);
			}

			// �����Ф˥��å����ե��ޥ�ɤ�����
		} else if (o == kickoffMI) {
			try {
				soccerscope.getSoccerServerConnection().dispstart();
			} catch (IOException ie) {
				System.err.println(ie);
			}

			// �����Ф˥ɥ�åץܡ��륳�ޥ�ɤ�����
		} else if (o == dropballMI) {
			try {
				Scene scene = soccerscope.getScenePlayer().getCurrentScene();
				soccerscope.getSoccerServerConnection().dispfoul(
						scene.ball.pos.x, scene.ball.pos.y, Team.NEUTRAL);
			} catch (IOException ie) {
				System.err.println(ie);
			}

			// �����Ф˺�������Υե꡼���å����ޥ�ɤ�����
		} else if (o == freekickleftMI) {
			try {
				Scene scene = soccerscope.getScenePlayer().getCurrentScene();
				soccerscope.getSoccerServerConnection().dispfoul(
						scene.ball.pos.x, scene.ball.pos.y, Team.LEFT_SIDE);
			} catch (IOException ie) {
				System.err.println(ie);
			}

			// �����Ф˱�������Υե꡼���å����ޥ�ɤ�����
		} else if (o == freekickrightMI) {
			try {
				Scene scene = soccerscope.getScenePlayer().getCurrentScene();
				soccerscope.getSoccerServerConnection().dispfoul(
						scene.ball.pos.x, scene.ball.pos.y, Team.RIGHT_SIDE);
			} catch (IOException ie) {
				System.err.println(ie);
			}

		}
	}

	// / ItemListener
	public void itemStateChanged(ItemEvent ie) {
		Object o = ie.getSource();

		// �ץ쥤�䡼�����򥦥���ɥ���ɽ��
		if (o == choiceMI) {
			soccerscope.getSelectWindow().setVisible(choiceMI.isSelected());
			soccerscope.getSelectWindow().setState(JFrame.NORMAL);

			// �ץ쥤�䡼�ξܺپ����ɽ��
		} else if (o == detailinfoMI) {
			soccerscope.getInformationWindow().setVisible(
					detailinfoMI.isSelected());
			soccerscope.getInformationWindow().setState(JFrame.NORMAL);

			// ��׾��� ������ɥ�ɽ��
		} else if (o == statisticsMI) {
			soccerscope.getStatisticsWindow().setVisible(
					statisticsMI.isSelected());
			soccerscope.getStatisticsWindow().setState(JFrame.NORMAL);

			// ���ץ���󥦥���ɥ���ɽ��
		} else if (o == optionMI) {
			soccerscope.getOptionWindow().setVisible(optionMI.isSelected());
			soccerscope.getOptionWindow().setState(JFrame.NORMAL);

			// ��������ꥢ����ON/OFF
		} else if (o == antialiasMI) {
			soccerscope.getScopePane().getFieldPane().setDrawOption(
					FieldPane.DRAW_ANTIALIAS, antialiasMI.isSelected());
		}
	}
}
