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
	private int window_size = 200;
	private DecisionTree dt;
	// private int myside;
	private int opponentside;
	private String algorithmname;
	private boolean initialized;
	int lasttactic = 0;
	int lastcommcycle = 0;

	public AssistantCoachRole(DatagramSocket socket, InetSocketAddress address,
			String treealgorithm, int window_size) {
		this.initialized = false;
		this.socket = socket;
		this.address = address;
		this.window_size = window_size;

		this.opponentside = Team.RIGHT_SIDE; // the opponent is usually the
		// right team.
		// this.myside=Team.LEFT_SIDE;

		this.dt = null;
		this.algorithmname = treealgorithm;
	}

	private void initialize(Scene current) throws SocketException, IOException {
		if (this.initialized) {
			return;
		}
		System.out.println("left team name:" + current.left.name);
		System.out.println("right team name:" + current.right.name);

		String opponentname;
		if (current.left.name.toLowerCase().startsWith("fcportugal")) {
			// this.myside=Team.LEFT_SIDE;
			this.opponentside = Team.RIGHT_SIDE;
			opponentname = current.right.name;
		} else if (current.left.name.toLowerCase().startsWith("fcportugal")) {
			// this.myside=Team.RIGHT_SIDE;
			this.opponentside = Team.LEFT_SIDE;
			opponentname = current.left.name;
		} else {
			throw new AssertionError("this should never happen...");
		}

		DecisionTreeFactory.DecisionTreeType treetype = DecisionTreeType
		.typeFor(opponentname, this.algorithmname);

		this.dt = DecisionTreeFactory.getDecisionTree(treetype);
		this.dt.init();
		this.changeTactic(this.dt.defaultTactic());

		this.initialized = true;
	}

	@Override
	public void newScene(Scene current, SceneSet allScenes) {
		try {
			if (current.time == 0) {
				this.initialize(current);
				return;
			}

			if (current.time % this.window_size == 0) {
				assert this.initialized : "NEVER do analysis not-initialized";

			System.err.println("assistant coach 'is' analyzing scene "
					+ current.time);
			StatisticsAccessFacilitator statistics = new StatisticsAccessFacilitator(
					GameAnalyzer.analyzerList, this.opponentside,
					current.time);

			int tactic = this.dt.tactic(statistics);
			if (current.time == this.lastcommcycle
					&& tactic == this.lasttactic) {
				// no need to repeat yourself
				return;
			}
			System.err.println(current.time
					+ ") assistant coach is changing tactic to " + tactic);
			this.changeTactic(tactic);

			this.lasttactic = tactic;
			this.lastcommcycle = current.time;
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void changeTactic(int tactic) throws SocketException, IOException {
		byte[] message = ("(ct " + tactic + ")").getBytes();
		DatagramPacket packet;

		packet = new DatagramPacket(message, message.length, this.address);
		this.socket.send(packet);
	}

}