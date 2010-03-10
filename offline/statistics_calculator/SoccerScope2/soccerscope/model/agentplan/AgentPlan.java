/*
 * $Header: $
 */

package soccerscope.model.agentplan;

import soccerscope.model.SceneSet;
import soccerscope.model.WorldModel;

public class AgentPlan {

	private int time;
	private int unum;
	private int level;
	private AgentPlanCommand command;

	// <AgentPlan>:=<Time> <Unum> <Level> <Command>
	public void parse(Context context) throws ParseException {
		time = context.currentNumber();
		context.nextToken();
		unum = context.currentNumber();
		context.nextToken();
		level = context.currentNumber();
		context.nextToken();
		command = new AgentPlanCommand(level, unum);
		WorldModel wm = WorldModel.getInstance();
		SceneSet sceneSet = wm.getSceneSet();
		command.parse(context, sceneSet.getScene(time));
	}

	public int getTime() {
		return time;
	}

	public int getUnum() {
		return unum;
	}

	public int getLevel() {
		return level;
	}

	public AgentPlanCommand getAgentPlanCommand() {
		return command;
	}
}
