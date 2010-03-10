/*
 * $Header: $
 */

package soccerscope.view;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.prefs.Preferences;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;

import soccerscope.SoccerScope;
import soccerscope.model.Param;
import soccerscope.model.Scene;
import soccerscope.model.SceneSet;
import soccerscope.model.SoccerObjectID;
import soccerscope.model.SoccerScopePreferences;
import soccerscope.model.WorldModel;
import soccerscope.util.geom.Point2f;
import soccerscope.view.layer.AgentLogLayer;
import soccerscope.view.layer.AgentWorldModelLayer;
import soccerscope.view.layer.BallLayer;
import soccerscope.view.layer.BallPossessionLayer;
import soccerscope.view.layer.ControlRegionLayer;
import soccerscope.view.layer.DominantRegionLayer;
import soccerscope.view.layer.DynamicSpineLayer;
import soccerscope.view.layer.KickableLayer;
import soccerscope.view.layer.Layer;
import soccerscope.view.layer.MotionLayer;
import soccerscope.view.layer.NeckLayer;
import soccerscope.view.layer.NoNoiseLayer;
import soccerscope.view.layer.OffsideLayer;
import soccerscope.view.layer.PassLayer;
import soccerscope.view.layer.PlayerLayer;
import soccerscope.view.layer.SayLayer;
import soccerscope.view.layer.StaminaLayer;
import soccerscope.view.layer.TrajectoryLayer;
import soccerscope.view.layer.UnumLayer;
import soccerscope.view.layer.VelocityLayer;
import soccerscope.view.layer.VisibleLayer;

public class ScopePane extends JPanel implements SoccerObjectID, ScopeWindow,
		AdjustmentListener, SoccerObjectSelectObserver, SoccerObjectSelector {

	private SoccerScope soccerscope;
	private FieldPane fieldPane;
	private InfoBar infoBar;
	private Scene scene;

	// �����?��С��ط�
	private int resolution = 10; // �����?��С��β�����
	private JScrollBar horizBar;
	private JScrollBar vertBar;
	private boolean changeMagnify;

	// �ޥ����⡼��
	private int mouseMode;
	public final static int SELECT_MODE = 1;
	public final static int HAND_MODE = 2;
	public final static int MAGNIFY_MODE = 3;
	public final static int MOVE_MODE = 4;

	private ArrayList showLayer;
	private ArrayList drawLayer;

	// BMP�ե�����ؤν񤭽Ф���
	private int index;
	private boolean recording;
	private BufferedImage bimg;

	// SoccerObjectSelectObserver�Υꥹ��
	private ArrayList observerList;

	// �����ϰ�ɽ����
	private ArrayList shapeList;
	// �구��
	private ArrayList textList;
	private Ruler ruler;

	// ��������ѹ��ˤ������褫
	private boolean isSceneChanged;

	// �����ˤ�륨��������ȥץ��level�ѹ��Τ���
	private AgentLogLayer agentLogLayer;
	private AgentWorldModelLayer agentWorldModelLayer;

	private boolean dogBall;

	public ScopePane(SoccerScope soccerscope) {
		super(new GridBagLayout(), true);
		this.soccerscope = soccerscope;

		index = 0;
		recording = false;
		bimg = null;
		dogBall = false;
		observerList = new ArrayList();
		observerList.add(this);
		shapeList = new ArrayList();
		textList = new ArrayList();

		// ���ߡ��ʥ�����
		scene = Scene.createScene();

		// �������ܡ���
		infoBar = new InfoBar(scene);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.NORTH;
		this.add(infoBar, gbc);

		// �ե������
		fieldPane = new FieldPane(scene);
		DefaultMouseListener dml = new DefaultMouseListener();
		selectModeListener = new SelectModeMouseListener();
		handModeListener = new HandModeMouseListener();
		magnifyModeListener = new MagnifyModeMouseListener();
		fieldPane.addMouseListener(dml);
		fieldPane.addMouseMotionListener(dml);

		setMouseMode(SELECT_MODE);

		// �쥤�䡼����Ͽ
		/*
		 * soccersocpe2�Ǥ�show��draw�ΰ㤤 show : ɽ�������� : �ä˷׻���ɬ�פʤ� draw :
		 * ���褹���� : ����äȷ׻����� or ���褹�뤿������̤ʥǡ���������(���̤˷׻�����)
		 */
		DominantRegionLayer dominantLayer = new DominantRegionLayer(fieldPane,
				false);
		VisibleLayer visibleLayer = new VisibleLayer(fieldPane, false);
		TrajectoryLayer trajectoryLayer = new TrajectoryLayer(fieldPane, false);
		NoNoiseLayer nonoiseLayer = new NoNoiseLayer(fieldPane, false);
		ControlRegionLayer controlLayer = new ControlRegionLayer(fieldPane,
				false);
		DynamicSpineLayer spineLayer = new DynamicSpineLayer(fieldPane, false);
		fieldPane.addBackgroundLayer(dominantLayer);
		fieldPane.addBackgroundLayer(visibleLayer);
		fieldPane.addBackgroundLayer(trajectoryLayer);
		fieldPane.addBackgroundLayer(nonoiseLayer);
		fieldPane.addBackgroundLayer(controlLayer);
		fieldPane.addBackgroundLayer(spineLayer);

		MotionLayer motionLayer = new MotionLayer(fieldPane, false);
		PassLayer tpassLayer = new PassLayer(fieldPane, false);
		BallPossessionLayer ballposLayer = new BallPossessionLayer(fieldPane,
				false);
		NeckLayer neckLayer = new NeckLayer(fieldPane, false);
		KickableLayer kickableLayer = new KickableLayer(fieldPane, false);
		UnumLayer unumLayer = new UnumLayer(fieldPane);
		StaminaLayer staminaLayer = new StaminaLayer(fieldPane, false);
		PlayerLayer playerLayer = new PlayerLayer(fieldPane);
		SayLayer sayLayer = new SayLayer(fieldPane, false);
		OffsideLayer offsideLayer = new OffsideLayer(fieldPane, false);
		BallLayer ballLayer = new BallLayer(fieldPane);
		VelocityLayer velocityLayer = new VelocityLayer(fieldPane, false);
		agentWorldModelLayer = new AgentWorldModelLayer(fieldPane, false);
		agentLogLayer = new AgentLogLayer(fieldPane, false);
		fieldPane.addForegroundLayer(motionLayer);
		fieldPane.addForegroundLayer(tpassLayer);
		fieldPane.addForegroundLayer(ballposLayer);
		fieldPane.addForegroundLayer(neckLayer);
		fieldPane.addForegroundLayer(kickableLayer);
		fieldPane.addForegroundLayer(unumLayer);
		fieldPane.addForegroundLayer(staminaLayer);
		fieldPane.addForegroundLayer(playerLayer);
		fieldPane.addForegroundLayer(sayLayer);
		fieldPane.addForegroundLayer(offsideLayer);
		fieldPane.addForegroundLayer(ballLayer);
		fieldPane.addForegroundLayer(velocityLayer);
		fieldPane.addForegroundLayer(agentWorldModelLayer);
		fieldPane.addForegroundLayer(agentLogLayer);

		// on/off������show�ϥ쥤�䡼
		showLayer = new ArrayList();
		showLayer.add(neckLayer);
		showLayer.add(visibleLayer);
		showLayer.add(kickableLayer);
		showLayer.add(staminaLayer);
		showLayer.add(velocityLayer);
		showLayer.add(motionLayer);
		showLayer.add(sayLayer);
		showLayer.add(agentLogLayer);
		showLayer.add(agentWorldModelLayer);
		// on/off������draw�ϥ쥤�䡼
		drawLayer = new ArrayList();
		drawLayer.add(offsideLayer);
		drawLayer.add(tpassLayer);
		drawLayer.add(ballposLayer);
		drawLayer.add(dominantLayer);
		drawLayer.add(trajectoryLayer);
		drawLayer.add(nonoiseLayer);
		drawLayer.add(controlLayer);
		drawLayer.add(spineLayer);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.NORTH;
		this.add(fieldPane, gbc);

		// ��ľ�����?��С�
		vertBar = new JScrollBar(JScrollBar.VERTICAL);
		vertBar
				.setMinimum((int) (-(Param.PITCH_WIDTH + 2 * Param.PITCH_MARGIN) / 2 * resolution));
		vertBar
				.setMaximum((int) ((Param.PITCH_WIDTH + 2 * Param.PITCH_MARGIN) / 2 * resolution));
		vertBar
				.setValue((int) (-(Param.PITCH_WIDTH + 2 * Param.PITCH_MARGIN) / 2 * resolution));
		vertBar
				.setVisibleAmount((int) ((Param.PITCH_WIDTH + 2 * Param.PITCH_MARGIN) * resolution));
		vertBar.addAdjustmentListener(this);
		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.VERTICAL;
		gbc.anchor = GridBagConstraints.NORTH;
		this.add(vertBar, gbc);

		// ��ʿ�����?��С�
		horizBar = new JScrollBar(JScrollBar.HORIZONTAL);
		horizBar
				.setMinimum((int) (-(Param.PITCH_LENGTH + 2 * Param.PITCH_MARGIN) / 2 * resolution));
		horizBar
				.setMaximum((int) ((Param.PITCH_LENGTH + 2 * Param.PITCH_MARGIN) / 2 * resolution));
		horizBar
				.setValue((int) (-(Param.PITCH_LENGTH + 2 * Param.PITCH_MARGIN) / 2 * resolution));
		horizBar
				.setVisibleAmount((int) ((Param.PITCH_LENGTH + 2 * Param.PITCH_MARGIN) * resolution));
		horizBar.addAdjustmentListener(this);
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.NORTH;
		this.add(horizBar, gbc);

	}

	// show�ϥ쥤�䡼�μ���
	public ArrayList getShowLayer() {
		return showLayer;
	}

	// draw�ϥ쥤�䡼�μ���
	public ArrayList getDrawLayer() {
		return drawLayer;
	}

	// AgentLogLayer�μ���
	public AgentLogLayer getAgentLogLayer() {
		return agentLogLayer;
	}

	// AgentWorldModelLayer�μ���
	public AgentWorldModelLayer getAgentWorldModelLayer() {
		return agentWorldModelLayer;
	}

	// observer����Ͽ
	public void addSoccerObjectSelectObserver(SoccerObjectSelectObserver soso) {
		observerList.add(soso);
		broadCastStateChanged();
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
		fieldPane.setSelected(id, flag);
	}

	public boolean isSelected(int id) {
		return fieldPane.isSelected(id);
	}

	// ���ߤξ��֤�Ʊ�󤹤�
	public void broadCastStateChanged() {
		for (int i = 0; i < MAX_ID; i++)
			deliverStateChanged(i, fieldPane.isSelected(i));
	}

	public void setScenePlayer(ScenePlayer scenePlayer) {
		scenePlayer.addScopeWindow(infoBar);
		scenePlayer.addScopeWindow(fieldPane);
		scenePlayer.addScopeWindow(this);
	}

	public FieldPane getFieldPane() {
		return fieldPane;
	}

	public InfoBar getInfoBar() {
		return infoBar;
	}

	public Scene getScene() {
		return scene;
	}

	// ɽ�������������
	public void setScene(Scene scene) {
		this.scene = scene;
		WorldModel wm = WorldModel.getInstance();
		SceneSet sceneSet = wm.getSceneSet();
		index = sceneSet.indexOf(scene);
		isSceneChanged = true;
		if (dogBall)
			fieldPane.setWatchPoint(scene.ball.pos);
		repaint();
	}

	// Ͽ��⡼�ɤ��ɤ����μ���
	public boolean isRecording() {
		return recording;
	}

	// Ͽ��⡼�ɤ�����
	public void setRecording(boolean flag) {
		recording = flag;
	}

	// �ܡ�����ɤä�����
	public void setBallDogMode(boolean flag) {
		dogBall = flag;
	}

	public void paint(Graphics g) {
		Dimension d = this.getSize();

		// �ҥ���ݡ��ͥ�Ȥκ����褬����
		// ���������ҥ���ݡ��ͥ�Ȥ�bimg����������
		if (bimg == null || bimg.getWidth() != d.width
				|| bimg.getHeight() != d.height) {
			bimg = (BufferedImage) createImage(d.width, d.height);
			Graphics g2 = bimg.getGraphics();
			g2.setClip(g.getClip());
			paintChildren(g2);
			isSceneChanged = false;
		}
		// �ҥ���ݡ��ͥ�Ȥ�����
		if (!isSceneChanged && dragged
				&& (mouseMode == SELECT_MODE || mouseMode == MAGNIFY_MODE)) {
			g.drawImage(bimg, 0, 0, this);
		} else {
			Graphics g2 = bimg.getGraphics();
			g2.setClip(g.getClip());
			paintChildren(g2);
			isSceneChanged = false;
			g.drawImage(bimg, 0, 0, this);
		}

		// shapeList�����Ǥʤ��ʤ�,��������褹��
		// �����g��ľ������
		if (!shapeList.isEmpty()) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setColor(Color.lightGray);
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
					0.5f));
			int sls = shapeList.size();
			for (int i = 0; i < sls; i++)
				g2.draw((Shape) shapeList.get(i));
			g2.setComposite(AlphaComposite
					.getInstance(AlphaComposite.SRC, 1.0f));
		}

		if (!textList.isEmpty()) {
			g.setColor(Color.white);
			int tls = textList.size();
			for (int i = 0; i < tls; i++) {
				Text text = (Text) textList.get(i);
				g.drawString(text.str, text.x, text.y);
			}
		}

		if (ruler != null) {
			ruler.draw(g);
		}

		// Ͽ�褹��
		if (recording) {
			try {
				d.width -= vertBar.getWidth();
				d.height -= horizBar.getHeight();

				DecimalFormat df = new DecimalFormat("0000");
				Preferences pf = SoccerScopePreferences.getPreferences();
				String type = pf.get("PublishType", "png");
				File f = new File("bmps/" + soccerscope.getDataSourceName()
						+ df.format(index) + "." + type);
				javax.imageio.ImageIO.write(bimg.getSubimage(0, 0, d.width,
						d.height), type, f);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// ���ߤγ���Ψ�򥹥��?��С��˥ե����ɥХå�����
	private void updateMagnify() {
		Dimension dSize = fieldPane.getDrawableSize();
		Point2f watchPoint = fieldPane.getWatchPoint();
		changeMagnify = true;
		vertBar.setVisibleAmount(dSize.height * resolution);
		horizBar.setVisibleAmount(dSize.width * resolution);
		vertBar.setValue((int) (watchPoint.y * resolution - dSize.height
				* resolution / 2));
		horizBar.setValue((int) (watchPoint.x * resolution - dSize.width
				* resolution / 2));
		vertBar.setVisibleAmount(dSize.height * resolution);
		horizBar.setVisibleAmount(dSize.width * resolution);
		changeMagnify = false;
	}

	// �����?��С����θ���������������
	public void setWatchPoint(Point2f pos) {
		fieldPane.setWatchPoint(pos);
		updateMagnify();
	}

	// �����?��С����θ��������Ψ������
	public void setMagnify(float magnify) {
		fieldPane.setMagnify(magnify);
		updateMagnify();
	}

	// �����?��С����θ��������Ψ������
	public void setMagnify(float magnify, float ballMagnify, float playerMagnify) {
		fieldPane.setMagnify(magnify, ballMagnify, playerMagnify);
		updateMagnify();
	}

	// �����?��С����θ��������Ψ������
	public void fitMagnify() {
		fieldPane.setWatchPoint(0, 0);
		fieldPane.fitMagnify();
		updateMagnify();
	}

	// ����Ψ�μ���
	public float getMagnify() {
		return fieldPane.getMagnify();
	}

	// AdjustmentListener
	public void adjustmentValueChanged(AdjustmentEvent ae) {
		JScrollBar sBar = (JScrollBar) ae.getSource();

		// magnify�ѹ��ܥ���ˤ�륤�٥�Ȥ�̵�뤹��
		if (changeMagnify)
			return;

		if (sBar == vertBar) {
			int y = sBar.getValue() + sBar.getVisibleAmount() / 2;

			Point2f watchPoint = fieldPane.getWatchPoint();
			watchPoint.y = y / resolution;
			fieldPane.setWatchPoint(watchPoint);

		} else if (sBar == horizBar) {
			int x = sBar.getValue() + sBar.getVisibleAmount() / 2;

			Point2f watchPoint = fieldPane.getWatchPoint();
			watchPoint.x = x / resolution;
			fieldPane.setWatchPoint(watchPoint);

		}
	}

	// �ޥ����⡼�ɤ����ꤹ��
	public void setMouseMode(int mode) {
		mouseMode = mode;
		// �⡼�ɤˤ�äƥޥ�������������Ѥ���
		if (mouseMode == SELECT_MODE) {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			listener = selectModeListener;
		} else if (mouseMode == HAND_MODE) {
			setCursor(new Cursor(Cursor.HAND_CURSOR));
			listener = handModeListener;
		} else if (mouseMode == MAGNIFY_MODE) {
			setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
			listener = magnifyModeListener;
		}
	}

	// �ޥ����⡼�ɤ��������
	public int getMouseMode() {
		return mouseMode;
	}

	class Text {
		String str;
		int x;
		int y;
	}

	private int getSelectedSoccerObjectID(Point2f press) {
		float ballSize = Param.BALL_SIZE * fieldPane.getBallMagnify();
		if (press.distanceLinf(scene.ball.pos) <= ballSize) {
			return BALL;
		}
		for (int i = 0; i < Param.MAX_PLAYER * 2; i++) {
			float playerSize = scene.player[i].getPlayerSize()
					* fieldPane.getPlayerMagnify();
			if (press.distanceLinf(scene.player[i].pos) <= playerSize) {
				return i;
			}
		}
		return MAX_ID;
	}

	interface MouseEventListener extends MouseListener, MouseMotionListener {
	}

	private MouseEventListener listener;
	private SelectModeMouseListener selectModeListener;
	private HandModeMouseListener handModeListener;
	private MagnifyModeMouseListener magnifyModeListener;
	private Point pressPoint;
	private Point releasePoint;
	private Point dragPoint;
	private Point2f selectPoint;
	private Point2f currentPoint;
	private Point2f press;
	private int selectID;
	private boolean moving = false;
	private boolean dragged = false; // �ޥ�����ɥ�å�������

	class DefaultMouseListener implements MouseEventListener {
		public void mouseClicked(MouseEvent me) {
			// ������å�
			if (me.getModifiers() == InputEvent.BUTTON1_MASK) {
				listener.mouseClicked(me);
			}

			// �楯��å�
			else if (me.getModifiers() == InputEvent.BUTTON2_MASK) {

			}

			// ������å�
			else if (me.getModifiers() == InputEvent.BUTTON3_MASK) {
				// �ݥåץ��åץ�˥塼��Ф�
				Point2f press = fieldPane.screenToField(me.getPoint());
				JPopupMenu jpop = new JPopupMenu();
				Iterator showit = showLayer.iterator();
				while (showit.hasNext()) {
					jpop.add(((Layer) showit.next()).createJCheckBoxMenuItem());
				}
				jpop.addSeparator();
				// �ץ쥤�䡼�򥯥�å�������
				int ID = getSelectedSoccerObjectID(press);
				if (ID != MAX_ID) {
					JMenu jm = new JMenu("Property");
					if (ID == BALL) {
						jm.add(new JMenuItem("pos:" + scene.ball.pos));
						jm.add(new JMenuItem("vel:" + scene.ball.vel));
					} else {
						jm.add(new JMenuItem("pos:" + scene.player[ID].pos));
						jm.add(new JMenuItem("vel:" + scene.player[ID].vel));
						jm
								.add(new JMenuItem("angle:"
										+ scene.player[ID].angle));
						jm.add(new JMenuItem("neck:"
								+ scene.player[ID].angleNeck));
						jm
								.add(new JMenuItem("stam:"
										+ scene.player[ID].stamina));
					}
					jpop.add(jm);
					jpop.addSeparator();
				}

				soccerscope.net.SoccerServerConnection ssc = soccerscope
						.getSoccerServerConnection();
				if (ssc != null) {
					jpop.add(new JMenuItem("Drap Ball"));
					jpop.add(new JMenuItem("FreeKick Left"));
					jpop.add(new JMenuItem("FreeKick Right"));
				}

				jpop.pack();
				jpop.show(fieldPane, me.getX(), me.getY());
			}
		}

		public void mousePressed(MouseEvent me) {
			pressPoint = me.getPoint();
			currentPoint = fieldPane.getWatchPoint();

			if (me.getModifiers() == InputEvent.BUTTON1_MASK) {
				listener.mousePressed(me);
			}
		}

		public void mouseReleased(MouseEvent me) {
			if (!shapeList.isEmpty()) {
				shapeList.clear();
				repaint();
			}
			if (!textList.isEmpty()) {
				textList.clear();
				repaint();
			}
			if (ruler != null) {
				ruler = null;
				repaint();
			}

			releasePoint = me.getPoint();

			// �ޥ�����ɥ�å����Ƥʤ���Фʤˤ⤷�ʤ�
			if (!dragged)
				return;

			dragged = false;

			if (me.getModifiers() == InputEvent.BUTTON1_MASK) {
				listener.mouseReleased(me);
			}
		}

		public void mouseEntered(MouseEvent me) {
		}

		public void mouseExited(MouseEvent me) {
		}

		public void mouseDragged(MouseEvent me) {
			dragPoint = me.getPoint();
			dragged = true;

			// ���ɥ�å�
			if (me.getModifiers() == InputEvent.BUTTON1_MASK) {
				listener.mouseDragged(me);
			}

			// ��ɥ�å�
			else if (me.getModifiers() == InputEvent.BUTTON2_MASK) {
				// ���������Ф�
				ruler = new Ruler(pressPoint.x, pressPoint.y
						+ infoBar.getHeight(), dragPoint.x, dragPoint.y
						+ infoBar.getHeight(), fieldPane, infoBar.getHeight());
				repaint();
			}
		}

		public void mouseMoved(MouseEvent me) {
		}
	}

	// SELECT_MODE ���Υ��٥�ȥꥹ�ʡ�
	class SelectModeMouseListener implements MouseEventListener {
		public void mouseClicked(MouseEvent me) {
			Point2f press = fieldPane.screenToField(me.getPoint());
			int ID = getSelectedSoccerObjectID(press);
			if (ID != MAX_ID)
				deliverStateChanged(ID, !fieldPane.isSelected(ID));
		}

		public void mousePressed(MouseEvent me) {

		}

		public void mouseReleased(MouseEvent me) {
			// �����ϰϤη׻�
			Dimension d = new Dimension(
					Math.abs(releasePoint.x - pressPoint.x), Math
							.abs(releasePoint.y - pressPoint.y));
			Point p = new Point(Math.min(pressPoint.x, releasePoint.x), Math
					.min(pressPoint.y, releasePoint.y));
			Rectangle r = new Rectangle(p, d);

			// �ܡ��뤬�����ϰϤ˴ޤޤ�뤫
			float ballSize = Param.BALL_SIZE * fieldPane.getBallMagnify()
					* fieldPane.getMagnify();
			Point2f ballPoint = fieldPane.fieldToScreen(scene.ball.pos);
			Rectangle ballRect = new Rectangle((int) (ballPoint.x - ballSize),
					(int) (ballPoint.y - ballSize), (int) (ballSize * 2),
					(int) (ballSize * 2));
			if (r.contains(ballRect) || r.intersects(ballRect)) {
				deliverStateChanged(BALL, !fieldPane.isSelected(BALL));

			}

			// �ץ쥤�䤬�����ϰϤ˴ޤޤ�뤫
			for (int i = 0; i < Param.MAX_PLAYER * 2; i++) {
				float playerSize = scene.player[i].getPlayerSize()
						* fieldPane.getPlayerMagnify() * fieldPane.getMagnify();
				Point2f pPoint = fieldPane.fieldToScreen(scene.player[i].pos);
				Rectangle pRect = new Rectangle((int) (pPoint.x - playerSize),
						(int) (pPoint.y - playerSize), (int) (playerSize * 2),
						(int) (playerSize * 2));
				if (r.contains(pRect) || r.intersects(pRect)) {
					deliverStateChanged(i, !fieldPane.isSelected(i));
				}
			}
		}

		public void mouseEntered(MouseEvent me) {
		}

		public void mouseExited(MouseEvent me) {
		}

		public void mouseDragged(MouseEvent me) {
			// ���̤������ϰϤ����褹��
			Dimension d = new Dimension(Math.abs(dragPoint.x - pressPoint.x),
					Math.abs(dragPoint.y - pressPoint.y));
			Point p = new Point(Math.min(pressPoint.x, dragPoint.x), Math.min(
					pressPoint.y, dragPoint.y)
					+ infoBar.getHeight());
			shapeList.clear();
			shapeList.add(new Rectangle(p, d));
			repaint();
		}

		public void mouseMoved(MouseEvent me) {
		}
	}

	// HAND_MODE ���Υ��٥�ȥꥹ�ʡ�
	class HandModeMouseListener implements MouseEventListener {
		public void mouseClicked(MouseEvent me) {

		}

		public void mousePressed(MouseEvent me) {
			press = fieldPane.screenToField(me.getPoint());
			selectID = getSelectedSoccerObjectID(press);
		}

		public void mouseReleased(MouseEvent me) {
			// ���ե饤�󥳡����⡼�ɤΤȤ��ϥ���������Ȥ��ư����
			isSceneChanged = true;
			repaint();
		}

		public void mouseEntered(MouseEvent me) {
		}

		public void mouseExited(MouseEvent me) {
		}

		public void mouseDragged(MouseEvent me) {
			if (selectID == MAX_ID) {
				// ��������ư������
				Point2f watchPoint = new Point2f(currentPoint);
				Point2f moveVector = new Point2f(dragPoint.x - pressPoint.x,
						dragPoint.y - pressPoint.y);
				moveVector.scale(1.0f / fieldPane.getMagnify());
				watchPoint.sub(moveVector);
				fieldPane.setWatchPoint(watchPoint);
				updateMagnify();
			} else {
				Point2f movePoint = fieldPane.screenToField(dragPoint);
				movePoint.x = (float) Math.rint(movePoint.x);
				movePoint.y = (float) Math.rint(movePoint.y);
				isSceneChanged = true;
				repaint();
			}
		}

		public void mouseMoved(MouseEvent me) {
		}
	}

	// MAGNIFY_MODE ���Υ��٥�ȥꥹ�ʡ�
	class MagnifyModeMouseListener implements MouseEventListener {
		public void mouseClicked(MouseEvent me) {
			// ����Ψ��10�ܤ���
			Point2f watchPoint = fieldPane.screenToField(me.getPoint());
			fieldPane.setWatchPoint(watchPoint);
			float magnify = fieldPane.getMagnify() + 10;
			setMagnify(magnify);
		}

		public void mousePressed(MouseEvent me) {

		}

		public void mouseReleased(MouseEvent me) {
			// �����������
			Point p = new Point((pressPoint.x + releasePoint.x) / 2,
					(pressPoint.y + releasePoint.y) / 2);
			Point2f watchPoint = fieldPane.screenToField(p);
			fieldPane.setWatchPoint(watchPoint);

			// ����Ψ������
			Dimension d = new Dimension(
					Math.abs(releasePoint.x - pressPoint.x), Math
							.abs(releasePoint.y - pressPoint.y));
			Dimension dSize = fieldPane.getSize();
			float magnify = Math.min((float) dSize.width / (float) d.width,
					(float) dSize.height / (float) d.height)
					* fieldPane.getMagnify();
			setMagnify(magnify);
		}

		public void mouseEntered(MouseEvent me) {
		}

		public void mouseExited(MouseEvent me) {
		}

		public void mouseDragged(MouseEvent me) {
			// ���̤������ϰϤ����褹��
			Dimension d = new Dimension(Math.abs(dragPoint.x - pressPoint.x),
					Math.abs(dragPoint.y - pressPoint.y));
			Point p = new Point(Math.min(pressPoint.x, dragPoint.x), Math.min(
					pressPoint.y, dragPoint.y)
					+ infoBar.getHeight());
			shapeList.clear();
			shapeList.add(new Rectangle(p, d));
			repaint();
		}

		public void mouseMoved(MouseEvent me) {
		}
	}

	public void printOut() {
		for (int i = 0; i < Param.MAX_PLAYER; i++) {
			System.out.println(scene.player[i].pos);
		}
		System.out.println();
	}
}
