/*
 * $Header: $
 */

package soccerscope;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.swing.JFrame;

import soccerscope.file.LogFileReader;
import soccerscope.model.SceneSet;
import soccerscope.model.SceneSetMaker;
import soccerscope.model.SoccerScopePreferences;
import soccerscope.model.WorldModel;
import soccerscope.net.SoccerServerConnection;
import soccerscope.util.ButtonManager;
import soccerscope.view.InformationWindow;
import soccerscope.view.OptionWindow;
import soccerscope.view.ScenePlayer;
import soccerscope.view.ScopePane;
import soccerscope.view.SelectWindow;
import soccerscope.view.StatisticsWindow;
import soccerscope.view.StatusBar;

public class SoccerScope extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static void main(String args[]) {
		// check if we should run in batch mode.
		if (args.length > 0) {
			if (args[0].equals("--batch") && args.length == 3) {
				String filename = args[1];
				String xmlFilename = args[2];
				try {
					NonGUISoccerScope.run(filename, xmlFilename);
				} catch (Exception e) {
					System.err.println("batch mode failed...");
					e.printStackTrace();
					System.exit(1);
				}
				return;
			} else if(args[0].equals("--udp") && args.length == 2) {
				int port = Integer.parseInt(args[1]);
				try {
					NonGUISoccerScope.run(port);
				} catch (Exception e) {
					System.err.println("udp mode failed...");
					e.printStackTrace();
					System.exit(1);
				}
				return;
			}
			else {
				System.out.format("invalid option %s or wrong"
						+ " number of arguments (0 or 2)\n", args[0]);
				System.out.println("usage [--batch input_file output_file]"
						+ "\nif you just want to use the GUI,"
						+ " don't use any parameters.");
				System.out.println("defaulting to GUI anyway.");
			}
		}
		// the default stuff with GUI...
		new SoccerScope(args);
	}

	public final static String title = "SoccerScope";

	Container container = null;

	private File chooseFile = null;
	private int maxX = Toolkit.getDefaultToolkit().getScreenSize().width;
	private int maxY = Toolkit.getDefaultToolkit().getScreenSize().height;

	// ��������³
	private SoccerServerConnection ssConnection = null;

	// main window component
	private SoccerScopeMenuBar menuBar = null;
	private SoccerScopeToolBar toolBar = null;
	private ScopePane scopePane = null;
	private StatusBar statusBar = null;
	private ScenePlayer scenePlayer = null;

	// �ץ쥤�䡼���� window
	private SelectWindow selectWindow = null;

	// information window
	private InformationWindow infoWindow = null;

	// Statistics window
	private StatisticsWindow statisticsWindow = null;

	// ���ץ���� window
	private OptionWindow optionWindow = null;

	public SoccerScope(String[] args) {
		// frame title
		super("SoccerScope");

		// initialize the world model
		WorldModel.getInstance();

		// get content pane
		this.container = this.getContentPane();
		this.container.setLayout(new GridBagLayout());

		// init mainpanel (ScopePane, fieldPane, infoBar)
		this.initMainPanel();

		// init MenuBar (menuBar)
		this.initMenuBar();

		// init statusBar (statusBar)
		this.initStatusBar();

		// init ToolBar (toolBar, scenePlayer)
		this.initToolBar();

		// init all Button's state
		this.initButtonState();

		// ScenePlayer ��ScopePane�˴�Ϣ�Ť���
		this.scopePane.setScenePlayer(this.scenePlayer);

		// WindowListener
		this.addWindowListener(new WindowEventHander());

		// realize
		this.pack();
		this.setVisible(true);

		// appear center of screen
		this.setLocationRelativeTo(null);
	}

	private void initMainPanel() {
		this.scopePane = new ScopePane(this);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.NORTH;
		this.container.add(this.scopePane, gbc);
	}

	private void initMenuBar() {
		this.menuBar = new SoccerScopeMenuBar(this);
		this.setJMenuBar(this.menuBar);
	}

	private void initToolBar() {
		this.toolBar = new SoccerScopeToolBar(this);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.SOUTHWEST;
		this.container.add(this.toolBar, gbc);

		// �ġ���С�����2
		this.scenePlayer = new ScenePlayer(this.scopePane, this.statusBar, true);
		this.scenePlayer.setFloatable(false);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.SOUTHWEST;
		this.container.add(this.scenePlayer, gbc);
	}

	private void initStatusBar() {
		this.statusBar = new StatusBar(this.scopePane);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.SOUTHWEST;
		this.container.add(this.statusBar, gbc);
	}

	private void initButtonState() {
		ButtonManager.setEnabled("logfile", false);
		ButtonManager.setEnabled("AgentWorldModel", false);
		ButtonManager.setEnabled("AgentLog", false);
		ButtonManager.setEnabled("network", false);
	}

	public void openLogFile(SceneSet sceneSet, String filename) {
		try {
			LogFileReader lfr = new LogFileReader(filename);
			SceneSetMaker ssm = new SceneSetMaker(lfr, sceneSet, this.statusBar
					.getProgressBar());
			ssm.start();
		} catch (IOException ie) {
			System.err.println(ie);
		}
	}

	class WindowEventHander extends WindowAdapter {
		@Override
		public void windowClosing(WindowEvent evt) {
			JFrame f = (JFrame) evt.getSource();
			if (f.getTitle().startsWith(SoccerScope.title)) {
				System.exit(0);
			} else {
				ButtonManager.setSelected(f.getTitle(), false);
			}
		}
	}

	public SoccerScopeMenuBar getSoccerScopeMenuBar() {
		return this.menuBar;
	}

	public SoccerScopeToolBar getSoccerScopeToolBar() {
		return this.toolBar;
	}

	public ScopePane getScopePane() {
		return this.scopePane;
	}

	public ScenePlayer getScenePlayer() {
		return this.scenePlayer;
	}

	public SelectWindow getSelectWindow() {
		if (this.selectWindow == null) {
			this.selectWindow = new SelectWindow();
			this.selectWindow.addWindowListener(new WindowEventHander());
			this.scenePlayer.addScopeWindow(this.selectWindow);
			// ScopePane������������
			this.scopePane.addSoccerObjectSelectObserver(this.selectWindow);

			// SelectWindow������������
			this.selectWindow.addSoccerObjectSelectObserver(this.scopePane);
			if (this.infoWindow != null) {
				this.selectWindow.addSoccerObjectSelectObserver(this.infoWindow);
			}
			// �и����֤�����
			this.selectWindow.setLocation(Math.min(this.maxX - this.selectWindow.getWidth(),
					this.getLocationOnScreen().x + this.getWidth()), Math.min(this.maxY
							- this.selectWindow.getHeight(), this.getLocationOnScreen().y
							+ this.getHeight() - this.selectWindow.getHeight()));
		}
		return this.selectWindow;
	}

	public void setSelectWindow(SelectWindow sw) {
		this.selectWindow = sw;
	}

	public InformationWindow getInformationWindow() {
		if (this.infoWindow == null) {
			this.infoWindow = new InformationWindow();
			this.infoWindow.addWindowListener(new WindowEventHander());
			this.scenePlayer.addScopeWindow(this.infoWindow);
			// ScopePane������������
			this.scopePane.addSoccerObjectSelectObserver(this.infoWindow);

			// SelectWindow������������
			if (this.selectWindow != null) {
				this.selectWindow.addSoccerObjectSelectObserver(this.infoWindow);
			}
			// �и����֤�����
			this.infoWindow.setLocation(Math.min(this.maxX - this.infoWindow.getWidth(),
					this.getLocationOnScreen().x + this.getWidth()
					- this.infoWindow.getWidth()), Math.min(this.maxY
							- this.infoWindow.getHeight(), this.getLocationOnScreen().y
							+ this.getHeight()));
		}
		return this.infoWindow;
	}

	public void setInformationWindow(InformationWindow sw) {
		this.infoWindow = sw;
	}

	public StatisticsWindow getStatisticsWindow() {
		if (this.statisticsWindow == null) {
			this.statisticsWindow = new StatisticsWindow();
			this.statisticsWindow.addWindowListener(new WindowEventHander());
			// �и����֤�����
			this.statisticsWindow.setLocation(Math.min(this.maxX
					- this.statisticsWindow.getWidth(), this.getLocationOnScreen().x),
					Math.min(this.maxY - this.statisticsWindow.getHeight(),
							this.getLocationOnScreen().y + this.getHeight()));
		}
		return this.statisticsWindow;
	}

	public void setStatisticsWindow(StatisticsWindow sw) {
		this.statisticsWindow = sw;
	}

	public OptionWindow getOptionWindow() {
		if (this.optionWindow == null) {
			this.optionWindow = new OptionWindow(this.scopePane);
			this.optionWindow.addWindowListener(new WindowEventHander());
			this.scenePlayer.addScopeWindow(this.optionWindow);
			this.optionWindow.setLocation(Math.min(this.maxX - this.optionWindow.getWidth(),
					this.getLocationOnScreen().x + this.getWidth()), Math.min(this.maxY
							- this.optionWindow.getHeight(), this.getLocationOnScreen().y));
		}
		return this.optionWindow;
	}

	public void setOptionWindow(OptionWindow sw) {
		this.optionWindow = sw;
	}

	public SoccerServerConnection getSoccerServerConnection() {
		return this.ssConnection;
	}

	public void setSoccerServerConnection(SoccerServerConnection ssc) {
		this.ssConnection = ssc;
	}

	public File getChooseFile() {
		if (this.chooseFile == null) {
			Preferences pf = SoccerScopePreferences.getPreferences();
			this.chooseFile = new File(pf.get("lastfile", System
					.getProperty("user.dir")));
		}
		return this.chooseFile;
	}

	public void setChooseFile(File f) {
		this.chooseFile = f;
		Preferences pf = SoccerScopePreferences.getPreferences();
		pf.put("lastfile", f.getPath());
	}

	public String getDataSourceName() {
		if (this.ssConnection != null) {
			return this.ssConnection.getHostName() + ":" + this.ssConnection.getPort();
		}
		if (this.chooseFile != null) {
			return this.chooseFile.getName();
		}
		return "scene";
	}
}
