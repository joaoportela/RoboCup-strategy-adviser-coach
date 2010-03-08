/*
 * $Header: $
 */

package soccerscope.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import soccerscope.model.agentplan.AgentPlan;
import soccerscope.model.agentplan.AgentPlanCommand;
import soccerscope.model.agentplan.CommentDB;
import soccerscope.model.agentplan.Context;
import soccerscope.model.agentplan.ParseException;

public class WorldModel {

	private static WorldModel instance = new WorldModel();
	private SceneSet sceneSet;
	private SceneSet[] agentwm;
	private Hashtable agentplanMap;

	public static WorldModel getInstance() {
		return instance;
	}

	private WorldModel() {
		sceneSet = new SceneSet();
		agentwm = new SceneSet[Param.MAX_PLAYER];
		for (int i = 0; i < Param.MAX_PLAYER; i++) {
			agentwm[i] = null;
		}
		agentplanMap = new Hashtable();
	}

	// ���٤ƤΥ��ꥢ
	public void clear() {
		sceneSet.clear();
		for (int i = 0; i < Param.MAX_PLAYER; i++) {
			if (agentwm[i] != null)
				agentwm[i].clear();
			agentwm[i] = null;
		}
		agentplanMap.clear();
		CommentDB.clear();
		// Runtime runtime = Runtime.getRuntime();
		// System.out.println("before gc");
		// System.out.println("free:  " + runtime.freeMemory());
		// System.out.println("total: " + runtime.totalMemory());
		System.gc();
		// System.out.println("after gc");
		// System.out.println("free:  " + runtime.freeMemory());
		// System.out.println("total: " + runtime.totalMemory());
	}

	// ����������ȥ��ɥ�ǥ�����Υ��ꥢ
	public void clearAgentWorldModel() {
		for (int i = 0; i < Param.MAX_PLAYER; i++) {
			if (agentwm[i] != null)
				agentwm[i].clear();
			agentwm[i] = null;
		}
	}

	// ����������ȥץ������Υ��ꥢ
	public void clearAgentPlan() {
		agentplanMap.clear();
		CommentDB.clear();
	}

	public SceneSet getSceneSet() {
		return sceneSet;
	}

	public boolean hasAgentWorldModel(int index) {
		if (agentwm[index] != null)
			return true;
		else
			return false;
	}

	public SceneSet getAgentWorldModel(int index) {
		if (agentwm[index] == null)
			agentwm[index] = new SceneSet();
		return agentwm[index];
	}

	// ����������ȥץ��
	public void makeAgentPlan(String filename) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));
			AgentPlan plan = new AgentPlan();
			for (;;) {
				String str = in.readLine();
				if (str == null)
					break;
				try {
					plan.parse(new Context(str));
					int time = plan.getTime();
					int unum = plan.getUnum();
					Integer key = createKey(time, unum);
					AgentPlanCommand command = plan.getAgentPlanCommand();

					if (command.getType() == AgentPlanCommand.COMMENT) {
						CommentDB.putComment(command.getComment(), time,
								unum - 1);
					} else if (agentplanMap.containsKey(key)) {
						ArrayList list = (ArrayList) agentplanMap.get(key);
						list.add(command);
					} else {
						ArrayList list = new ArrayList();
						list.add(command);
						agentplanMap.put(key, list);
					}
				} catch (ParseException e) {
					System.err.println(e);
					System.err.println(str);
				}
			}
			in.close();
		} catch (IOException ie) {
			System.err.println(ie);
		}
	}

	public Integer createKey(int time, int unum) {
		return new Integer(time * 100 + unum);
	}

	public boolean hasAgentPlan(int time, int unum) {
		return agentplanMap.containsKey(createKey(time, unum));
	}

	public boolean hasAgentPlan(Integer key) {
		return agentplanMap.containsKey(key);
	}

	public ArrayList getAgentPlan(int time, int unum) {
		Integer key = createKey(time, unum);
		if (agentplanMap.containsKey(key)) {
			return (ArrayList) agentplanMap.get(key);
		} else
			return null;
	}

	public ArrayList getAgentPlan(Integer key) {
		if (agentplanMap.containsKey(key)) {
			return (ArrayList) agentplanMap.get(key);
		} else
			return null;
	}
}
