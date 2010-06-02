//package soccerscope.net;
//
//import java.io.IOException;
//import java.net.DatagramSocket;
//
//import soccerscope.model.PlayMode;
//import soccerscope.model.Scene;
//import soccerscope.model.SceneBuilder;
//import soccerscope.model.Team;
//
///**
// * this class was supposed to receive and parse regular coach messages but there
// * was no time to implement it. As such the connection will be done directly to the
// * server as a viewer.
// *
// * @author joao
// *
// */
//public class CoachConnection extends SceneBuilder {
//	public final int BUFFERSIZE = 16 * 2048;
//
//	private DatagramSocket socket;
//
//	public CoachConnection(DatagramSocket sock) {
//		this.socket = sock;
//	}
//
//	@Override
//	public Scene makeScene(byte[] packet, PlayMode playmode, Team left,
//			Team right) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public int readPacket(byte[] packet) throws IOException {
//		// TODO Auto-generated method stub
//		return 0;
//	}
//
//}
