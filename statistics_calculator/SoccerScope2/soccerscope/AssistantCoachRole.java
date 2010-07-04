package soccerscope;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

import soccerscope.decision.DecisionTreeFactory;
import soccerscope.decision.StatisticsAccessFacilitator;
import soccerscope.decision.DecisionTreeFactory.DecisionTree;
import soccerscope.decision.DecisionTreeFactory.DecisionTreeType;
import soccerscope.model.Scene;
import soccerscope.model.SceneSet;
import soccerscope.model.SceneSetMaker;
import soccerscope.model.Team;
import soccerscope.util.GameAnalyzer;

public class AssistantCoachRole implements SceneSetMaker.CoachInterface {
	private DatagramSocket socket;
	private InetSocketAddress address;

	/**
	 * time interval at which the coach should make a decision
	 */
	private final int INTERVAL = 1000;
	private DecisionTree dt;
	private int opponentside;

	public AssistantCoachRole(DatagramSocket socket, InetSocketAddress address) {
		this.socket = socket;
		this.address = address;

		// TODO - a way to figure out the TreeType and the side
		DecisionTreeFactory.DecisionTreeType treetype = DecisionTreeType.RANDOMFOREST_BAHIA;
		this.opponentside = Team.RIGHT_SIDE; // the opponent is usually the right team.

		this.dt = DecisionTreeFactory.getDecisionTree(treetype);
	}

	@Override
	public void newScene(Scene current, SceneSet allScenes) {
		try {
			if(current.time == 0) {
				return; // skip
			}
			// a bit after the match started print the teams names.
			if(current.time == 2) {
				System.out.println("left team name:" + current.left.name);
				System.out.println("right team name:" + current.right.name);

				// should i figure out the side now??

				// should i inform of the strategy now??
			}
			if (current.time % this.INTERVAL == 0) {
				System.err.println("assistant coach 'is' analyzing scene "
						+ current.time);
				StatisticsAccessFacilitator statistics = new StatisticsAccessFacilitator(
						GameAnalyzer.analyzerList, this.opponentside, current.time);

				int tactic=this.dt.Tactic(statistics);

				byte[] message = ("(tactic " + tactic + ")").getBytes();
				DatagramPacket packet;

				packet = new DatagramPacket(message, message.length,
						this.address);
				this.socket.send(packet);

			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}