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
	private int side;

	public AssistantCoachRole(DatagramSocket socket, InetSocketAddress address) {
		this.socket = socket;
		this.address = address;

		// TODO - a way to figure out the TreeType and the side
		DecisionTreeFactory.DecisionTreeType treetype = DecisionTreeType.RANDOMFOREST_BAHIA;
		this.side = Team.LEFT_SIDE;

		this.dt = DecisionTreeFactory.getDecisionTree(treetype);
	}

	private int k_to_tactic(int k) {
		return 1;
	}

	@Override
	public void newScene(Scene current, SceneSet allScenes) {
		try {
			if (current.time % this.INTERVAL == 0) {
				System.err.println("assistant coach 'is' analyzing scene "
						+ current.time);
				StatisticsAccessFacilitator statistics = new StatisticsAccessFacilitator(
						GameAnalyzer.analyzerList, this.side, current.time);
				int k=this.dt.whichK(statistics);
				int tactic = this.k_to_tactic(k);

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