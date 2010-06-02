package soccerscope;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

import soccerscope.model.Scene;
import soccerscope.model.SceneSet;
import soccerscope.model.SceneSetMaker;

public class AssistantCoachRole implements SceneSetMaker.CoachAnalyzerInterface {
	private DatagramSocket socket;
	private InetSocketAddress address;

	public AssistantCoachRole(DatagramSocket socket, InetSocketAddress address) {
		this.socket = socket;
		this.address = address;
	}

	@Override
	public void analyze(Scene current, SceneSet allScenes) {
		try {
			if (current.time % 100 == 0) {
				System.err
				.println("coach 'is' analyzing scene " + current.time);
				// TODO - something with GameAnalyzer.analyzerList;

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

}