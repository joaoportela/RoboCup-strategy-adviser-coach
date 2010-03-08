/*
 * $Log: ScenePlayer.java,v $
 * Revision 1.14  2002/10/22 05:15:14  koji
 * ScopePane:�⡼����˥ꥹ�ʡ����Ѱ�
 *
 * Revision 1.13  2002/10/11 10:41:41  koji
 * �Х�����(HeteroParam),GoalȽ������
 *
 * Revision 1.12  2002/10/10 09:43:38  koji
 * ͥ���ΰ�η׻��β��� (Direction-Distance Table)
 *
 * Revision 1.11  2002/10/01 14:11:09  koji
 * support Preference, gif format
 *
 * Revision 1.10  2002/09/20 11:09:04  koji
 * ���󥹥ȡ���ǥ��쥯�ȥ�˶����ޤ�Ǥ���ȡ�soccerscope.conf���ɤ߹�
 * �ޤ�ʤ��Τ���
 *
 * Revision 1.9  2002/09/12 11:25:45  koji
 * ��ײ��ϡ�������ǽ���ɲá���������ե�����졼�����ե������б�
 *
 * Revision 1.8  2002/09/09 09:56:41  koji
 * Preference��GUI����,soccerscope.conf��Ƴ��
 *
 * Revision 1.7  2002/09/05 09:46:38  koji
 * ���٥�ȥ�������ǽ���ɲ�
 *
 * Revision 1.6  2002/09/02 07:14:50  koji
 * ButtunManger��Ƴ��
 *
 * Revision 1.5  2002/08/30 10:47:34  koji
 * bmpviewer����Jimi�б�
 *
 * Revision 1.4  2002/07/12 08:44:18  koji
 * ���?��ƥ���
 *
 * Revision 1.3  2002/06/27 07:31:41  koji
 * SpinButton�Υƥ���
 *
 * Revision 1.2  2002/05/14 08:24:51  koji
 * �ܡ���ؤΥ�������ɲ�
 *
 * Revision 1.1.1.1  2002/03/01 14:12:52  koji
 * CVS�����
 *
 */

package soccerscope.view;

import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.Vector;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ComponentInputMap;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.Timer;

import soccerscope.model.GameEvent;
import soccerscope.model.Param;
import soccerscope.model.Scene;
import soccerscope.model.SceneSet;
import soccerscope.model.SoccerScopePreferences;
import soccerscope.model.WorldModel;

public class ScenePlayer extends JToolBar implements ActionListener {

	private FontMetrics fm;
	private int margin;
	private ScopePane scopePane;
	private StatusBar statusBar;
	private Vector<ScopeWindow> scopeWindowVector;

	private Timer timer;
	private int index; // scene index (not simulation time)
	private int orient;
	private int interval;
	private boolean enable[];
	private int offset[];

	private final static int PLAY = 0;
	private final static int STOP = 1;
	private final static int REVERSE = 2;
	private final static int SYNC = 3;

	private JToggleButton recordButton;
	private JButton prevEventButton;
	private JButton quickReverseButton;
	private JButton reverseButton;
	private JButton stepReverseButton;
	private JButton stopButton;
	private JButton stepPlayButton;
	private JButton playButton;
	private JButton quickPlayButton;
	private JButton nextEventButton;
	private JTextField timeField;
	private JButton setTimeButton;
	private JButton syncButton;

	public ScenePlayer(ScopePane scopePane, StatusBar statusBar,
			boolean canPublish) {
		super();

		this.scopePane = scopePane;
		this.statusBar = statusBar;
		scopeWindowVector = new Vector<ScopeWindow>();

		index = 0;
		orient = STOP;
		interval = 100;
		timer = new Timer(interval, new TimerListener());
		timer.stop();
		enable = new boolean[GameEvent.MAX];
		offset = new int[GameEvent.MAX];

		Preferences pf = SoccerScopePreferences.getPreferences();
		for (int i = GameEvent.MIN; i < GameEvent.MAX; i++) {
			enable[i] = pf.getBoolean("GameEvent" + GameEvent.getEventName(i),
					false);
			offset[i] = pf.getInt("GameEvent" + GameEvent.getEventName(i)
					+ "Offset", 0);
		}

		statusBar.setStatus("stop");

		recordButton = new JToggleButton(new ImageIcon(ClassLoader
				.getSystemResource("soccerscope/image/general/Stop24.gif")),
				false);
		prevEventButton = new JButton(new ImageIcon(ClassLoader
				.getSystemResource("soccerscope/image/SearchPrev24.gif")));
		quickReverseButton = new JButton(new ImageIcon(ClassLoader
				.getSystemResource("soccerscope/image/media/Rewind24.gif")));
		reverseButton = new JButton(new ImageIcon(ClassLoader
				.getSystemResource("soccerscope/image/media/PlayBack24.gif")));
		stepReverseButton = new JButton(new ImageIcon(ClassLoader
				.getSystemResource("soccerscope/image/media/StepBack24.gif")));
		stopButton = new JButton(new ImageIcon(ClassLoader
				.getSystemResource("soccerscope/image/media/Pause24.gif")));
		stepPlayButton = new JButton(
				new ImageIcon(
						ClassLoader
								.getSystemResource("soccerscope/image/media/StepForward24.gif")));
		playButton = new JButton(new ImageIcon(ClassLoader
				.getSystemResource("soccerscope/image/media/Play24.gif")));
		quickPlayButton = new JButton(
				new ImageIcon(
						ClassLoader
								.getSystemResource("soccerscope/image/media/FastForward24.gif")));
		nextEventButton = new JButton(new ImageIcon(ClassLoader
				.getSystemResource("soccerscope/image/SearchNext24.gif")));
		timeField = new JTextField("0", 5);
		setTimeButton = new JButton("jump");
		syncButton = new JButton("sync");

		JPanel jp = new JPanel();
		jp.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
		this.add(recordButton);
		this.add(prevEventButton);
		this.add(quickReverseButton);
		this.add(reverseButton);
		this.add(stepReverseButton);
		this.add(stopButton);
		this.add(stepPlayButton);
		this.add(playButton);
		this.add(quickPlayButton);
		this.add(nextEventButton);
		jp.add(timeField);
		jp.add(setTimeButton);
		jp.add(syncButton);
		this.add(jp);

		recordButton.addActionListener(this);
		prevEventButton.addActionListener(this);
		quickReverseButton.addActionListener(this);
		reverseButton.addActionListener(this);
		stepReverseButton.addActionListener(this);
		stopButton.addActionListener(this);
		stepPlayButton.addActionListener(this);
		playButton.addActionListener(this);
		quickPlayButton.addActionListener(this);
		nextEventButton.addActionListener(this);
		timeField.addActionListener(this);
		setTimeButton.addActionListener(this);
		syncButton.addActionListener(this);

		if (!canPublish)
			recordButton.setEnabled(false);

		ActionMap am = getActionMap();
		ComponentInputMap im = new ComponentInputMap(this);

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK),
				"forward");
		am.put("forward", new ForwardAction());
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_B, Event.CTRL_MASK),
				"backward");
		am.put("backward", new BackwardAction());
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, Event.CTRL_MASK), "next");
		am.put("next", new NextAction());
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK), "prev");
		am.put("prev", new PrevAction());

		setActionMap(am);
		setInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW, im);

		setVisible(true);
	}

	class ForwardAction extends AbstractAction {
		public void actionPerformed(ActionEvent ae) {
			stepPlay();
		}
	}

	class BackwardAction extends AbstractAction {
		public void actionPerformed(ActionEvent ae) {
			stepReverse();
		}
	}

	class NextAction extends AbstractAction {
		public void actionPerformed(ActionEvent ae) {
			nextEvent(getCurrentTime());
		}
	}

	class PrevAction extends AbstractAction {
		public void actionPerformed(ActionEvent ae) {
			prevEvent(getCurrentTime());
		}
	}

	// Scene���������������ɥ�����Ͽ
	public void addScopeWindow(ScopeWindow swindow) {
		scopeWindowVector.add(swindow);
		setScene();
	}

	private void setScene() {
		WorldModel wm = WorldModel.getInstance();
		SceneSet sceneSet = wm.getSceneSet();

		// ���������å�
		if (index < 0)
			index = 0;
		if (index >= sceneSet.size())
			index = sceneSet.size() - 1;

		// �����������
		Scene scene = sceneSet.elementAt(index);
		Iterator<ScopeWindow> it = scopeWindowVector.iterator();
		while (it.hasNext()) {
			it.next().setScene(scene);
		}

		FieldPane fieldPane = scopePane.getFieldPane();
		// ����������ȥ��ɥ�ǥ������
		for (int i = 0; i < Param.MAX_PLAYER; i++) {
			if (wm.hasAgentWorldModel(i)) {
				SceneSet awm = wm.getAgentWorldModel(i);
				if (awm.hasScene(scene.time)) {
					Scene agentScene = awm.getScene(scene.time);
					fieldPane.setAgentScene(agentScene);
				}
			}
		}
		// ����������ȥץ�������
		for (int i = 0; i < Param.MAX_PLAYER; i++) {
			if (wm.hasAgentPlan(scene.time, i + 1))
				fieldPane.setAgentPlan(wm.getAgentPlan(scene.time, i + 1), i);
			else
				fieldPane.clearAgentPlan(i);
		}

	}

	public int getCurrentTime() {
		WorldModel wm = WorldModel.getInstance();
		SceneSet sceneSet = wm.getSceneSet();
		return sceneSet.elementAt(index).time;
	}

	public Scene getCurrentScene() {
		WorldModel wm = WorldModel.getInstance();
		SceneSet sceneSet = wm.getSceneSet();
		return sceneSet.elementAt(index);
	}

	public void quickReverse() {
		statusBar.setStatus("reverse");
		orient = REVERSE;
		interval = 50;
		timer.setDelay(interval);
		timer.restart();
	}

	public void reverse() {
		statusBar.setStatus("reverse");
		orient = REVERSE;
		interval = 100;
		timer.setDelay(interval);
		timer.restart();
	}

	public void stepReverse() {
		statusBar.setStatus("stop");
		orient = STOP;
		timer.stop();
		if (index > 0)
			index--;
		setScene();
	}

	public void stop() {
		statusBar.setStatus("stop");
		orient = STOP;
		timer.stop();
	}

	public void stepPlay() {
		statusBar.setStatus("stop");
		orient = STOP;
		timer.stop();
		WorldModel wm = WorldModel.getInstance();
		SceneSet sceneSet = wm.getSceneSet();
		if (index < sceneSet.size())
			index++;
		setScene();
	}

	public void play() {
		statusBar.setStatus("play");
		orient = PLAY;
		interval = 100;
		timer.setDelay(interval);
		timer.restart();
	}

	public void quickPlay() {
		statusBar.setStatus("play");
		orient = PLAY;
		interval = 10;
		timer.setDelay(interval);
		timer.restart();
	}

	public void skip(int time) {
		WorldModel wm = WorldModel.getInstance();
		SceneSet sceneSet = wm.getSceneSet();
		if (sceneSet.hasScene(time))
			index = sceneSet.getIndex(time);
		setScene();
	}

	public void head() {
		index = 0;
		setScene();
	}

	public void tail() {
		WorldModel wm = WorldModel.getInstance();
		SceneSet sceneSet = wm.getSceneSet();
		index = sceneSet.size() - 1;
		setScene();
	}

	public void sync() {
		statusBar.setStatus("sync");
		orient = SYNC;
		interval = 100;
		timer.setDelay(interval);
		timer.restart();
	}

	public void prevEvent(int time) {
		WorldModel wm = WorldModel.getInstance();
		SceneSet sceneSet = wm.getSceneSet();
		GameEvent ge = sceneSet.getPrevEvent(time);
		if (ge == null)
			return;
		if (enable[ge.type]) {
			int skiptime = ge.time + offset[ge.type];
			if (time <= skiptime)
				prevEvent(ge.time);
			skip(ge.time + offset[ge.type]);
			statusBar.setStatus(ge.getEventName());
		} else
			prevEvent(ge.time);
	}

	public void nextEvent(int time) {
		WorldModel wm = WorldModel.getInstance();
		SceneSet sceneSet = wm.getSceneSet();
		GameEvent ge = sceneSet.getNextEvent(time);
		if (ge == null)
			return;
		if (enable[ge.type]) {
			int skiptime = ge.time + offset[ge.type];
			if (time >= skiptime)
				nextEvent(ge.time);
			skip(ge.time + offset[ge.type]);
			statusBar.setStatus(ge.getEventName());
		} else
			nextEvent(ge.time);
	}

	public void setEventEnabled(int type, boolean b) {
		enable[type] = b;
		Preferences pf = SoccerScopePreferences.getPreferences();
		pf.putBoolean("GameEvent" + GameEvent.getEventName(type), b);
	}

	public boolean getEventEnabled(int type) {
		return enable[type];
	}

	public void setEventOffset(int type, int n) {
		offset[type] = n;
		Preferences pf = SoccerScopePreferences.getPreferences();
		pf.putInt("GameEvent" + GameEvent.getEventName(type) + "Offset", n);
	}

	public int getEventOffset(int type) {
		return offset[type];
	}

	public void actionPerformed(ActionEvent ae) {
		Object o = ae.getSource();

		if (o == recordButton) {
			scopePane.setRecording(recordButton.isSelected());
		} else if (o == prevEventButton) {
			prevEvent(getCurrentTime());
		} else if (o == quickReverseButton) {
			quickReverse();
		} else if (o == reverseButton) {
			reverse();
		} else if (o == stepReverseButton) {
			stepReverse();
		} else if (o == stopButton) {
			stop();
		} else if (o == stepPlayButton) {
			stepPlay();
		} else if (o == playButton) {
			play();
		} else if (o == quickPlayButton) {
			quickPlay();
		} else if (o == nextEventButton) {
			nextEvent(getCurrentTime());
		} else if (o == timeField || o == setTimeButton) {
			try {
				int settime = Integer.parseInt(timeField.getText());
				skip(settime);
			} catch (NumberFormatException nfe) {
				return;
			}
		} else if (o == syncButton) {
			sync();
		}
	}

	class TimerListener implements ActionListener {
		public void actionPerformed(ActionEvent ae) {
			WorldModel wm = WorldModel.getInstance();
			SceneSet sceneSet = wm.getSceneSet();

			if (orient == PLAY) {
				if (index < sceneSet.size() - 1)
					index++;
				else
					stop();
				setScene();
			} else if (orient == REVERSE) {
				if (index > 0)
					index--;
				else
					stop();
				setScene();
			} else if (orient == SYNC) {
				tail();
			}
		}
	}
}
