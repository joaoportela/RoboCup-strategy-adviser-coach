package soccerscope;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.List;

import soccerscope.model.Scene;
import soccerscope.model.SceneSet;
import soccerscope.model.SceneSetMaker;
import soccerscope.util.GameAnalyzer;
import soccerscope.util.analyze.SceneAnalyzer;

public class AssistantCoachRole implements SceneSetMaker.CoachInterface {
	private DatagramSocket socket;
	private InetSocketAddress address;

	public AssistantCoachRole(DatagramSocket socket, InetSocketAddress address) {
		this.socket = socket;
		this.address = address;
	}

	@Override
	public void newScene(Scene current, SceneSet allScenes) {
		try {
			if (current.time % 100 == 0) {
				System.err.println("coach 'is' analyzing scene " + current.time);
				// TODO - something with GameAnalyzer.analyzerList;
				StatisticsAccessFacilitator statistics = new StatisticsAccessFacilitator(GameAnalyzer.analyzerList);

				byte[] message = ("(rcvd " + current.time + ")").getBytes();
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

	private static class StatisticsAccessFacilitator {
		private List<SceneAnalyzer> analyzers;

		// TODO - implement this.
		public StatisticsAccessFacilitator(List<SceneAnalyzer> analyzerList) {
			this.analyzers = analyzerList;
		}
	}

}