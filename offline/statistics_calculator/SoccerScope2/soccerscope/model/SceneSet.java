/*
 * $Header: $
 */

package soccerscope.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import soccerscope.util.GameAnalyzer;
import soccerscope.util.Time;

public class SceneSet {

	private ArrayList<Scene> sceneList;
	private int[] timeTable;
	private boolean[] validTable;
	private ArrayList<GameEvent> eventList;

	/**
	 * ���󥹥ȥ饯��
	 */
	public SceneSet() {
		sceneList = new ArrayList<Scene>(Time.GAME_TIME);
		timeTable = new int[Time.MAX_TIME + 1];
		validTable = new boolean[Time.MAX_TIME + 1];
		for (int i = 0; i < Time.MAX_TIME + 1; i++)
			validTable[i] = false;

		addScene(Scene.createScene());
		eventList = new ArrayList<GameEvent>();
	}

	/**
	 * ������κ��������֤�
	 * 
	 * @return ������κ������
	 */
	public int getLimitTime() {
		return (sceneList.get(sceneList.size() - 1)).time;
	}

	/**
	 * Scene��������դ��ä��롣Scene�λ��郎�����ʤ��,�դ��ä���false���֤���
	 * 
	 * @param scene
	 *            �դ��ä���Scene
	 * @return �������ä���줿��
	 */
	public boolean addScene(Scene scene) {
		if (Time.isValid(scene.time)) {
			if (!validTable[scene.time]) {
				timeTable[scene.time] = sceneList.size();
				validTable[scene.time] = true;
			}
			sceneList.add(scene);
			return true;
		}
		return false;
	}

	/**
	 * ��Ƭ��Scene���֤�
	 * 
	 * @return �ǽ��Scene
	 */
	public Scene firstScene() {
		return sceneList.get(0);
	}

	/**
	 * �����Scene���֤�
	 * 
	 * @return �����Scene
	 */
	public Scene lastScene() {
		return sceneList.get(sceneList.size() - 1);
	}

	/**
	 * ���ꤵ�줿���֤�Scene���֤�
	 * 
	 * @param index
	 *            Scene�ʥ�С�,����=Scene�ʥ�С��Ȥϸ¤�ʤ�
	 * @return index���ܤ�Scene
	 */
	public Scene elementAt(int index) {
		return sceneList.get(index);
	}

	/**
	 * ���ꤵ�줿�����Scene����Ѥ���
	 * 
	 * @param scene
	 *            ���Ѥ�����Scene
	 * @param time
	 *            ����
	 */
	public void setScene(Scene scene, int time) {
		validTable[time] = true;
		sceneList.set(timeTable[time], scene);
	}

	/**
	 * ���ꤵ�줿�����Scene���֤�
	 * 
	 * @param time
	 *            ����
	 * @return ���ꤵ�줿�����Scene
	 */
	public Scene getScene(int time) {
		return sceneList.get(timeTable[time]);
	}

	/**
	 * ���ꤵ�줿�����Scene�����뤫�ɤ������֤�
	 * 
	 * @param time
	 *            ����
	 * @return ���ꤵ�줿�����Scene�����뤫�ɤ���
	 */
	public boolean hasScene(int time) {
		if (Time.isValid(time))
			return validTable[time];
		else
			return false;
	}

	/**
	 * ���ꤵ�줿�����Scene�ʥ�Сݤ��֤�
	 * 
	 * @param time
	 *            ����
	 * @return ���ꤵ�줿�����Scene�ʥ�С�
	 */
	public int getIndex(int time) {
		return timeTable[time];
	}

	/**
	 * ���ꤵ�줿Scene��Scene�ʥ�Сݤ��֤�
	 * 
	 * @param scene
	 *            Scene
	 * @return ���ꤵ�줿Scene��Scene�ʥ�С�
	 */
	public int indexOf(Scene scene) {
		return sceneList.indexOf((Object) scene);
	}

	/**
	 * ���ߤ�Scene�ο����֤�
	 * 
	 * @return ���ߤ�Scene�ο�
	 */
	public int size() {
		return sceneList.size();
	}

	/**
	 * ���٤Ƥ�Scene��õ��
	 */
	public void clear() {
		sceneList.clear();
		timeTable = new int[Time.MAX_TIME + 1];
		validTable = new boolean[Time.MAX_TIME + 1];
		for (int i = 0; i < Time.MAX_TIME + 1; i++)
			validTable[i] = false;
		addScene(Scene.createScene());
		eventList.clear();
	}

	/**
	 * ���ƥ졼�����֤�
	 * 
	 * @return Iterator
	 */
	public Iterator<Scene> iterator() {
		return sceneList.iterator();
	}

	/**
	 * ���Ϥ���
	 */
	public void analyze() {
		eventList.clear();
		eventList = GameAnalyzer.analyze(this);
		Collections.sort(eventList);
	}

	/**
	 * ���ꤷ���������˵����륤�٥�Ȼ�����������
	 * 
	 * @param time
	 *            ����
	 * @return ���٥�Ȼ���
	 */
	public GameEvent getNextEvent(int time) {
		Iterator<GameEvent> it = eventList.iterator();
		GameEvent ge;
		while (it.hasNext()) {
			ge = it.next();
			if (ge.time > time)
				return ge;
		}
		return null;
	}

	/**
	 * ���ꤷ�����������˵����륤�٥�Ȼ�����������
	 * 
	 * @param time
	 *            ����
	 * @return ���٥�Ȼ���
	 */
	public GameEvent getPrevEvent(int time) {
		int size = eventList.size();
		GameEvent ge;
		for (int i = size - 1; i >= 0; i--) {
			ge = eventList.get(i);
			if (ge.time < time)
				return ge;
		}
		return null;
	}
}
