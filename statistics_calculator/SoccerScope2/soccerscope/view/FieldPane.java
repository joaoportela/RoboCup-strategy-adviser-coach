/*
 * $Header: $
 */

package soccerscope.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import javax.swing.JComponent;

import soccerscope.model.Param;
import soccerscope.model.Scene;
import soccerscope.model.SoccerObjectID;
import soccerscope.util.geom.Point2f;
import soccerscope.view.layer.Layer;
import soccerscope.view.layer.PitchLayer;
import soccerscope.view.layer.PitchLineLayer;

public class FieldPane extends JComponent implements SoccerObjectID,
		ScopeWindow {

	/** public ��� **/
	public final static int DRAW_ANTIALIAS = 1;
	public final static int DRAW_OPTION_MAX = 2;

	/* ���������ѿ� */
	private boolean[] selectedTable;
	private boolean[] drawTable;

	// ɽ�������륷����
	private Scene scene;
	private Scene agentScene; // FieldPane�Ǥ�1�Ĥ�agentWorldModel�������ݡ��Ȥ��ʤ����Ȥˤ���
	private ArrayList[] agentPlan;

	// ����Ψ
	private float magnify = 6.0f; // ���Τγ���Ψ
	private float playerMagnify = 3.6f; // �ץ쥤��γ���Ψ
	private float ballMagnify = 7.2f; // �ܡ���γ���Ψ

	// ����������ѿ������֥�������
	private Point2f centerF; // �����(�ե�����ɺ�ɸ��)
	private Point2f centerS; // �����̤������(���̺�ɸ��)
	private ArrayList backgroundList;
	private ArrayList foregroundList;
	private ArrayList shapeList;

	public FieldPane(Scene scene) {
		super();

		this.scene = scene;
		agentScene = null;

		agentPlan = new ArrayList[Param.MAX_PLAYER];
		for (int i = 0; i < Param.MAX_PLAYER; i++)
			agentPlan[i] = null;

		selectedTable = new boolean[MAX_ID];
		for (int i = 0; i < MAX_ID; i++)
			selectedTable[i] = false;

		drawTable = new boolean[DRAW_OPTION_MAX];
		drawTable[DRAW_ANTIALIAS] = false;

		centerF = new Point2f(0.0f, 0.0f);
		centerS = new Point2f();

		backgroundList = new ArrayList();
		foregroundList = new ArrayList();
		shapeList = null;

		backgroundList.add(new PitchLayer(this));
		foregroundList.add(new PitchLineLayer(this));
	}

	// ɽ�������������
	public void setScene(Scene scene) {
		this.scene = scene;
	}

	// ɽ��������μ���
	public Scene getScene() {
		return scene;
	}

	// ����������ȥ��ɤ�����
	public void setAgentScene(Scene scene) {
		agentScene = scene;
	}

	// ����������ȥ��ɤμ���
	public Scene getAgentScene() {
		return agentScene;
	}

	// ����������ȥץ�������
	public void setAgentPlan(ArrayList list, int index) {
		agentPlan[index] = list;
	}

	// ����������ȥץ��μ���
	public ArrayList getAgentPlan(int index) {
		return agentPlan[index];
	}

	// ����������ȥץ����˴�
	public void clearAgentPlan(int index) {
		agentPlan[index] = null;
	}

	// �Хå������ɥ쥤�䡼���ɲ�
	public void addBackgroundLayer(Layer layer) {
		backgroundList.add(layer);
	}

	// �ե��������ɥ쥤�䡼���ɲ�
	public void addForegroundLayer(Layer layer) {
		foregroundList.add(layer);
	}

	// ���褹��޷��ꥹ�Ȥ�����
	public void setShapeList(ArrayList list) {
		shapeList = list;
		repaint();
	}

	// ���褹��޷��ꥹ�Ȥ�õ�
	public void clearShapeList() {
		if (shapeList != null) {
			shapeList.clear();
			shapeList = null;
			repaint();
		}
	}

	public void setSelected(int id, boolean flag) {
		selectedTable[id] = flag;
		repaint();
	}

	public boolean isSelected(int id) {
		return selectedTable[id];
	}

	// ���襪�ץ���������
	public void setDrawOption(int key, boolean flag) {
		if (key < DRAW_OPTION_MAX)
			drawTable[key] = flag;
		repaint();
	}

	// ���襪�ץ����μ���
	public boolean getDrawOption(int key) {
		if (key < DRAW_OPTION_MAX)
			return drawTable[key];
		return false;
	}

	// ����Ψ�μ���
	public float getMagnify() {
		return magnify;
	}

	// �ץ쥤�䡼�γ���Ψ�μ���
	public float getPlayerMagnify() {
		return playerMagnify;
	}

	// �ܡ���γ���Ψ�μ���
	public float getBallMagnify() {
		return ballMagnify;
	}

	// ����Ψ�����ꤹ��
	// �ץ쥤��ȥܡ���γ���Ψ�ϼ�ư�Ƿ׻�
	public void setMagnify(float magnify) {
		this.magnify = Math.max(1, magnify);
		float playerWidth = Param.PLAYER_SIZE * this.magnify;
		float ballWidth = Param.BALL_SIZE * this.magnify;
		// System.out.println(playerWidth + " " + ballWidth);
		playerMagnify = Math.max(1, 6 / playerWidth);
		ballMagnify = Math.max(1, 3 / ballWidth);
		// System.out.println(playerMagnify + " " + ballMagnify);
		repaint();
	}

	// ����Ψ�����ꤹ��
	public void setMagnify(float magnify, float ballMagnify, float playerMagnify) {
		this.magnify = Math.max(1, magnify);
		this.ballMagnify = Math.max(1, ballMagnify);
		this.playerMagnify = Math.max(1, playerMagnify);
		repaint();
	}

	// �ե���������Τ����̥������ˤ����ޤ�褦�˳���Ψ�����ꤹ��
	public void fitMagnify() {
		Dimension d = this.getSize();
		int width = (int) (Param.PITCH_LENGTH + 2.0 * Param.PITCH_MARGIN);
		int height = (int) (Param.PITCH_WIDTH + 2.0 * Param.PITCH_MARGIN);
		magnify = Math.min(d.width / width, d.height / height);
		setMagnify(magnify);
		repaint();
	}

	// �ե���������Τ����ꥵ�����ˤ����ޤ�褦�˳���Ψ�����ꤹ��
	public void fitMagnify(Dimension d) {
		int width = (int) (Param.PITCH_LENGTH + 2.0 * Param.PITCH_MARGIN);
		int height = (int) (Param.PITCH_WIDTH + 2.0 * Param.PITCH_MARGIN);
		magnify = Math.min(d.width / width, d.height / height);
		setMagnify(magnify);
		repaint();
	}

	public void setPlayerMagnify(float playerMagnify) {
		this.playerMagnify = playerMagnify;
		repaint();
	}

	public void setBallMagnify(float ballMagnify) {
		this.ballMagnify = ballMagnify;
		repaint();
	}

	// �ե�����ɺ�ɸ�ϤؤΥ��ե����Ѵ����������
	public AffineTransform getAffineTransform() {
		return new AffineTransform(magnify, 0, 0, magnify, -centerF.x * magnify
				+ centerS.x, -centerF.y * magnify + centerS.y);
	}

	// �ե�����ɺ�ɸ������̺�ɸ���Ѵ�����
	public Point2f fieldToScreen(Point2f p) {
		Point2f p1 = new Point2f(p);
		p1.sub(centerF);
		p1.scale(magnify);
		p1.add(centerS);
		return p1;
	}

	// �ե�����ɺ�ɸ������̺�ɸ���Ѵ�����
	// �侩����Ƥ��ޤ���(Layer��drawRect�ʤɤΤ߻���)
	public int F2SX(float fx) {
		return (int) ((fx - centerF.x) * magnify + centerS.x);
	}

	public int F2SY(float fy) {
		return (int) ((fy - centerF.y) * magnify + centerS.y);
	}

	public int F2SSX(float fx) {
		return (int) (fx * magnify);
	}

	public int F2SSY(float fy) {
		return (int) (fy * magnify);
	}

	// ���̺�ɸ����ե�����ɺ�ɸ���Ѵ�����
	public Point2f screenToField(Point2f p) {
		Point2f p1 = new Point2f(p);
		p1.sub(centerS);
		p1.scale(1.0f / magnify);
		p1.add(centerF);
		return p1;
	}

	// ���̺�ɸ����ե�����ɺ�ɸ���Ѵ�����
	public Point2f screenToField(Point p) {
		Point2f p1 = new Point2f(p.x, p.y);
		p1.sub(centerS);
		p1.scale(1.0f / magnify);
		p1.add(centerF);
		return p1;
	}

	// �����(�ե�����ɺ�ɸ��)�����ꤹ��
	public void setWatchPoint(float x, float y) {
		this.centerF.set(x, y);
		repaint();
	}

	// �����(�ե�����ɺ�ɸ��)�����ꤹ��
	public void setWatchPoint(Point2f p) {
		this.centerF.set(p);
		repaint();
	}

	// �����(�ե�����ɺ�ɸ��)�����ꤹ��
	public void setWatchPoint(Point p) {
		this.centerF.set(screenToField(p));
		repaint();
	}

	// �����(�ե�����ɺ�ɸ��)���֤�
	public Point2f getWatchPoint() {
		return new Point2f(centerF);
	}

	// �ե�����ɺ�ɸ�ϤǤ������ϰϤ򤫤���
	public Dimension getDrawableSize() {
		Dimension d = this.getSize();
		int width = (int) (d.width / magnify);
		int height = (int) (d.height / magnify);
		return new Dimension(width, height);
	}

	public Dimension getPreferredSize() {
		int width = (int) ((Param.PITCH_LENGTH + 2.0 * Param.PITCH_MARGIN) * magnify);
		int height = (int) ((Param.PITCH_WIDTH + 2.0 * Param.PITCH_MARGIN) * magnify);
		return new Dimension(width, height);
	}

	public Dimension getMinimumSize() {
		return getPreferredSize();
	}

	// ���̤�����
	public void paintComponent(Graphics g) {
		if (scene == null)
			return;

		Dimension d = this.getSize();
		centerS.x = d.width / 2;
		centerS.y = d.height / 2;

		Graphics2D g2 = (Graphics2D) g;
		if (drawTable[DRAW_ANTIALIAS]) {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
		}

		long time = System.currentTimeMillis();

		int bs = backgroundList.size();
		for (int i = 0; i < bs; i++) {
			// System.out.println((System.currentTimeMillis() - time)
			// + " " + ((Layer)backgroundList.get(i)).getLayerName());
			((Layer) backgroundList.get(i)).draw(g2);
		}

		int fs = foregroundList.size();
		for (int i = 0; i < fs; i++) {
			// System.out.println((System.currentTimeMillis() - time)
			// + " " + ((Layer)foregroundList.get(i)).getLayerName());
			((Layer) foregroundList.get(i)).draw(g2);
		}
		// System.out.println((System.currentTimeMillis() - time));

		if (shapeList != null) {
			g2.setColor(Color.lightGray);
			int sls = shapeList.size();
			for (int i = 0; i < sls; i++)
				g2.draw((Shape) shapeList.get(i));
		}
	}

}
