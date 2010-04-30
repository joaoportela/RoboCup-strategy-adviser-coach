/*
 * $Header: $
 */

package soccerscope;

import java.awt.Event;
import java.awt.Frame;
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
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	SoccerScope soccerscope = null;

	// MenuBar Item
	private final JMenuItem openMI;
	private final JMenuItem loadMI;
	private final JMenuItem loadplanMI;
	private final JMenuItem closeMI;
	private final JMenuItem preferenceMI;
	private final JMenuItem quitMI;

	private final JMenuItem connectMI;
	private final JMenuItem connectToHostMI;
	private final JMenuItem disconnectMI;
	private final JMenuItem kickoffMI;
	private final JMenuItem dropballMI;
	private final JMenuItem freekickleftMI;
	private final JMenuItem freekickrightMI;

	private final JCheckBoxMenuItem choiceMI;
	private final JCheckBoxMenuItem detailinfoMI;
	private final JCheckBoxMenuItem statisticsMI;
	private final JCheckBoxMenuItem optionMI;

	private final JCheckBoxMenuItem antialiasMI;

	public SoccerScopeMenuBar(final SoccerScope soccerscope) {
		this.soccerscope = soccerscope;

		final JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic('F');
		this.add(fileMenu);
		final JMenu monitorMenu = new JMenu("Monitor");
		monitorMenu.setMnemonic('M');
		this.add(monitorMenu);
		final JMenu windowMenu = new JMenu("Window");
		windowMenu.setMnemonic('W');
		this.add(windowMenu);
		final JMenu viewMenu = new JMenu("View");
		viewMenu.setMnemonic('V');
		this.add(viewMenu);
		final JMenu analyzeMenu = new JMenu("Analyze");
		analyzeMenu.setMnemonic('A');
		this.add(analyzeMenu);

		// File Menu
		this.openMI = new JMenuItem("Open");
		this.openMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				Event.CTRL_MASK));
		this.openMI.addActionListener(this);
		this.loadMI = new JMenuItem("Load AgentWorldModel");
		this.loadMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
				Event.CTRL_MASK));
		this.loadMI.addActionListener(this);
		this.loadplanMI = new JMenuItem("Load AgentLog");
		this.loadplanMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,
				Event.CTRL_MASK));
		this.loadplanMI.addActionListener(this);
		this.closeMI = new JMenuItem("Close");
		this.closeMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
				Event.CTRL_MASK));
		this.closeMI.addActionListener(this);
		this.preferenceMI = new JMenuItem("Preference");
		this.preferenceMI.addActionListener(this);
		this.quitMI = new JMenuItem("Quit");
		this.quitMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
				Event.CTRL_MASK));
		this.quitMI.addActionListener(this);
		fileMenu.add(this.openMI);
		fileMenu.add(this.loadMI);
		fileMenu.add(this.loadplanMI);
		fileMenu.add(this.closeMI);
		fileMenu.addSeparator();
		fileMenu.add(this.preferenceMI);
		fileMenu.addSeparator();
		fileMenu.add(this.quitMI);
		ButtonManager.addButton("logfile", this.loadMI);
		ButtonManager.addButton("logfile", this.loadplanMI);
		ButtonManager.addButton("logfile", this.closeMI);

		// Monitor Menu
		this.connectMI = new JMenuItem("Connect to Local");
		this.connectMI.addActionListener(this);
		this.connectToHostMI = new JMenuItem("Connect to Host");
		this.connectToHostMI.addActionListener(this);
		this.disconnectMI = new JMenuItem("Disconnect");
		this.disconnectMI.addActionListener(this);
		this.kickoffMI = new JMenuItem("KickOff");
		this.kickoffMI.addActionListener(this);
		this.dropballMI = new JMenuItem("DropBall");
		this.dropballMI.addActionListener(this);
		this.freekickleftMI = new JMenuItem("FreeKickLeft");
		this.freekickleftMI.addActionListener(this);
		this.freekickrightMI = new JMenuItem("FreeKickRight");
		this.freekickrightMI.addActionListener(this);
		monitorMenu.add(this.connectMI);
		monitorMenu.add(this.connectToHostMI);
		monitorMenu.add(this.disconnectMI);
		monitorMenu.addSeparator();
		monitorMenu.add(this.kickoffMI);
		monitorMenu.add(this.dropballMI);
		monitorMenu.add(this.freekickleftMI);
		monitorMenu.add(this.freekickrightMI);

		ButtonManager.addButton("network", this.disconnectMI);
		ButtonManager.addButton("network", this.kickoffMI);
		ButtonManager.addButton("network", this.dropballMI);
		ButtonManager.addButton("network", this.freekickleftMI);
		ButtonManager.addButton("network", this.freekickrightMI);

		// Window Menu
		this.choiceMI = new JCheckBoxMenuItem("Select Player");
		this.choiceMI.addItemListener(this);
		this.choiceMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,
				Event.CTRL_MASK));
		this.detailinfoMI = new JCheckBoxMenuItem("Detail Info");
		this.detailinfoMI.addItemListener(this);
		this.detailinfoMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K,
				Event.CTRL_MASK));
		this.statisticsMI = new JCheckBoxMenuItem("Statistics");
		this.statisticsMI.addItemListener(this);
		this.statisticsMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M,
				Event.CTRL_MASK));
		this.optionMI = new JCheckBoxMenuItem("Option");
		this.optionMI.addItemListener(this);
		this.optionMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,
				Event.CTRL_MASK));
		windowMenu.add(this.choiceMI);
		windowMenu.add(this.detailinfoMI);
		windowMenu.add(this.statisticsMI);
		windowMenu.add(this.optionMI);
		ButtonManager.addButton(SelectWindow.title, this.choiceMI);
		ButtonManager.addButton(InformationWindow.title, this.detailinfoMI);
		ButtonManager.addButton(StatisticsWindow.title, this.statisticsMI);
		ButtonManager.addButton(OptionWindow.title, this.optionMI);

		// View Menu
		final ArrayList<Layer> showLayer = soccerscope.getScopePane().getShowLayer();
		final Iterator<Layer> showit = showLayer.iterator();
		while (showit.hasNext()) {
			viewMenu.add((showit.next()).createJCheckBoxMenuItem());
		}
		viewMenu.addSeparator();
		this.antialiasMI = new JCheckBoxMenuItem("Antialias");
		this.antialiasMI.setSelected(soccerscope.getScopePane().getFieldPane()
				.getDrawOption(FieldPane.DRAW_ANTIALIAS));
		this.antialiasMI.addItemListener(this);
		viewMenu.add(this.antialiasMI);

		// Analyze Menu
		final ArrayList<Layer> drawLayer = soccerscope.getScopePane().getDrawLayer();
		final Iterator<Layer> drawit = drawLayer.iterator();
		while (drawit.hasNext()) {
			analyzeMenu.add((drawit.next()).createJCheckBoxMenuItem());
		}
	}

	// / ActionListener
	public void actionPerformed(final ActionEvent ae) {
		final Object o = ae.getSource();

		// �?�ե�����򳫤�
		if (o == this.openMI) {
			final JFileChooser chooser = new JFileChooser(this.soccerscope.getChooseFile());
			chooser.setFileFilter(new GameLogFileFilter());
			chooser.setSelectedFile(this.soccerscope.getChooseFile());
			final int returnVal = chooser.showOpenDialog(this.soccerscope);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				this.soccerscope.getScenePlayer().stop();
				final File chooseFile = chooser.getSelectedFile();
				if (chooseFile == null) {
					return;
				}
				this.soccerscope.setChooseFile(chooseFile);
				final WorldModel wm = WorldModel.getInstance();
				this.soccerscope.getScenePlayer().head();
				wm.clear();
				this.soccerscope.setTitle("SoccerScope: " + chooseFile.getName());
				this.soccerscope.openLogFile(wm.getSceneSet(), chooseFile.getPath());
				ButtonManager.setEnabled("logfile", true);
			}

		} else if (o == this.loadMI) {
			final JFileChooser chooser = new JFileChooser(this.soccerscope.getChooseFile());
			chooser.setFileFilter(new GameLogFileFilter());
			final int returnVal = chooser.showOpenDialog(this.soccerscope);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				this.soccerscope.getScenePlayer().stop();
				final File chooseFile = chooser.getSelectedFile();
				if (chooseFile == null) {
					return;
				}
				this.soccerscope.setChooseFile(chooseFile);
				final WorldModel wm = WorldModel.getInstance();
				wm.clearAgentWorldModel();
				this.soccerscope.openLogFile(wm.getAgentWorldModel(0), chooseFile
						.getPath());
				ButtonManager.setEnabled("AgentWorldModel", true);
			}

			// ����������ȥץ����ɤ߹���
		} else if (o == this.loadplanMI) {
			final JFileChooser chooser = new JFileChooser(this.soccerscope.getChooseFile());
			chooser.setFileFilter(new PlanLogFileFilter());
			final int returnVal = chooser.showOpenDialog(this.soccerscope);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				this.soccerscope.getScenePlayer().stop();
				final File chooseFile = chooser.getSelectedFile();
				if (chooseFile == null) {
					return;
				}
				this.soccerscope.setChooseFile(chooseFile);
				final WorldModel wm = WorldModel.getInstance();
				wm.clearAgentPlan();
				wm.makeAgentPlan(chooseFile.getPath());
				ButtonManager.setEnabled("AgentLog", true);
			}

			// �?�ե�������Ĥ���
		} else if (o == this.closeMI) {
			this.soccerscope.getScenePlayer().stop();
			this.soccerscope.getScenePlayer().head();
			final WorldModel wm = WorldModel.getInstance();
			wm.clear();
			this.soccerscope.setTitle("SoccerScope");
			ButtonManager.setEnabled("logfile", false);
			ButtonManager.setEnabled("AgentWorldModel", false);
			ButtonManager.setEnabled("AgentLog", false);

			// soccerscope��λ����
		} else if (o == this.quitMI) {
			System.exit(0);

			// ����������?��ɽ������
		} else if (o == this.preferenceMI) {
			final PreferenceDialog pd = new PreferenceDialog(this.soccerscope);
			pd.show();
			this.soccerscope.repaint();

			// �ǥե���ȥۥ��Ȥ���³����
		} else if (o == this.connectMI) {
			try {
				final WorldModel wm = WorldModel.getInstance();
				this.soccerscope
				.setSoccerServerConnection(new SoccerServerConnection());
				final SceneSetMaker ssm = new SceneSetMaker(this.soccerscope
						.getSoccerServerConnection(), wm.getSceneSet());
				try {
					this.soccerscope.getSoccerServerConnection().dispinit();
					Thread.sleep(1000);
					this.soccerscope.getScenePlayer().sync();
					ssm.start();
					ButtonManager.setEnabled("network", true);
				} catch (final InterruptedException iie) {
				} catch (final IOException ie) {
					System.err.println(ie);
				}
			} catch (final SocketException se) {
				System.err.println(se);
			} catch (final UnknownHostException uhe) {
				System.err.println(uhe);
			}

			// ���ꤵ�줿�ۥ��Ȥ���³����
		} else if (o == this.connectToHostMI) {
			final String host = JOptionPane.showInputDialog("Input Host");
			if (host != null) {
				try {
					final WorldModel wm = WorldModel.getInstance();
					this.soccerscope
					.setSoccerServerConnection(new SoccerServerConnection(
							host));
					final SceneSetMaker ssm = new SceneSetMaker(this.soccerscope
							.getSoccerServerConnection(), wm.getSceneSet());
					try {
						this.soccerscope.getSoccerServerConnection().dispinit();
						Thread.sleep(1000);
						this.soccerscope.getScenePlayer().sync();
						ssm.start();
						ButtonManager.setEnabled("network", true);
					} catch (final InterruptedException iie) {
					} catch (final IOException ie) {
						System.err.println(ie);
					}
				} catch (final SocketException se) {
					System.err.println(se);
				} catch (final UnknownHostException uhe) {
					System.err.println(uhe);
				}
			}

			// �����ФȤ���³�����Ǥ���
		} else if (o == this.disconnectMI) {
			try {
				this.soccerscope.getSoccerServerConnection().dispbye();
				this.soccerscope.setSoccerServerConnection(null);
				ButtonManager.setEnabled("network", false);
			} catch (final IOException ie) {
				System.err.println(ie);
			}

			// �����Ф˥��å����ե��ޥ�ɤ�����
		} else if (o == this.kickoffMI) {
			try {
				this.soccerscope.getSoccerServerConnection().dispstart();
			} catch (final IOException ie) {
				System.err.println(ie);
			}

			// �����Ф˥ɥ�åץܡ��륳�ޥ�ɤ�����
		} else if (o == this.dropballMI) {
			try {
				final Scene scene = this.soccerscope.getScenePlayer().getCurrentScene();
				this.soccerscope.getSoccerServerConnection().dispfoul(
						scene.ball.pos.x, scene.ball.pos.y, Team.NEUTRAL);
			} catch (final IOException ie) {
				System.err.println(ie);
			}

			// �����Ф˺�������Υե꡼���å����ޥ�ɤ�����
		} else if (o == this.freekickleftMI) {
			try {
				final Scene scene = this.soccerscope.getScenePlayer().getCurrentScene();
				this.soccerscope.getSoccerServerConnection().dispfoul(
						scene.ball.pos.x, scene.ball.pos.y, Team.LEFT_SIDE);
			} catch (final IOException ie) {
				System.err.println(ie);
			}

			// �����Ф˱�������Υե꡼���å����ޥ�ɤ�����
		} else if (o == this.freekickrightMI) {
			try {
				final Scene scene = this.soccerscope.getScenePlayer().getCurrentScene();
				this.soccerscope.getSoccerServerConnection().dispfoul(
						scene.ball.pos.x, scene.ball.pos.y, Team.RIGHT_SIDE);
			} catch (final IOException ie) {
				System.err.println(ie);
			}

		}
	}

	// / ItemListener
	public void itemStateChanged(final ItemEvent ie) {
		final Object o = ie.getSource();

		// �ץ쥤�䡼�����򥦥���ɥ���ɽ��
		if (o == this.choiceMI) {
			this.soccerscope.getSelectWindow().setVisible(this.choiceMI.isSelected());
			this.soccerscope.getSelectWindow().setState(Frame.NORMAL);

			// �ץ쥤�䡼�ξܺپ����ɽ��
		} else if (o == this.detailinfoMI) {
			this.soccerscope.getInformationWindow().setVisible(
					this.detailinfoMI.isSelected());
			this.soccerscope.getInformationWindow().setState(Frame.NORMAL);

			// ��׾��� ������ɥ�ɽ��
		} else if (o == this.statisticsMI) {
			this.soccerscope.getStatisticsWindow().setVisible(
					this.statisticsMI.isSelected());
			this.soccerscope.getStatisticsWindow().setState(Frame.NORMAL);

			// ���ץ���󥦥���ɥ���ɽ��
		} else if (o == this.optionMI) {
			this.soccerscope.getOptionWindow().setVisible(this.optionMI.isSelected());
			this.soccerscope.getOptionWindow().setState(Frame.NORMAL);

			// ��������ꥢ����ON/OFF
		} else if (o == this.antialiasMI) {
			this.soccerscope.getScopePane().getFieldPane().setDrawOption(
					FieldPane.DRAW_ANTIALIAS, this.antialiasMI.isSelected());
		}
	}
}
