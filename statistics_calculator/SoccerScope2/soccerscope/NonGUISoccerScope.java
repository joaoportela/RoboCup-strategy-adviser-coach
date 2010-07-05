package soccerscope;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Properties;

import soccerscope.file.LogFileReader;
import soccerscope.model.Player;
import soccerscope.model.Scene;
import soccerscope.model.SceneBuilder;
import soccerscope.model.SceneSet;
import soccerscope.model.SceneSetMaker;
import soccerscope.model.Team;
import soccerscope.model.WorldModel;
import soccerscope.net.SoccerServerConnection;
import soccerscope.util.GameAnalyzer;
import soccerscope.util.analyze.SceneAnalyzer;
import soccerscope.util.analyze.Xmling;

import com.jamesmurty.utils.XMLBuilder;

public class NonGUISoccerScope {

	public static void run(final String filename, final String xmlFilename)
	throws Exception {
		// initiate the world model...
		final WorldModel wm = WorldModel.getInstance();
		wm.clear(); // don't think this is necessary... but ok...
		final File file = new File(filename);
		if (file == null || !file.canRead()) {
			throw new FileNotFoundException(String.format(
					"invalid file '%s' or permissions...\n", filename));
		}
		// open the log file and analyze it (by doing SceneSet.analyze())
		NonGUISoccerScope.openAndAnalyzeLogFile(wm.getSceneSet(), file
				.getPath());
		System.out.println("final calculations and xml output:");
		NonGUISoccerScope.printXML(wm.getSceneSet(), xmlFilename);
		System.out.println("DONE!");
	}

	private static void openAndAnalyzeLogFile(final SceneSet sceneSet,
			final String filename) throws IOException, InterruptedException {
		final SceneBuilder lfr = new LogFileReader(filename);
		final SceneSetMaker ssm = new SceneSetMaker(lfr, sceneSet);
		ssm.run();
	}

	private static void printXML(final SceneSet sceneSet,
			final String xmlFilename) throws Exception {
		final XMLBuilder builder = XMLBuilder.create("analysis");
		builder.attr("version", "1.03");

		final Scene lscene = WorldModel.getInstance().getSceneSet().lastScene();

		final XMLBuilder left = builder.elem("leftteam").attr("name",
				lscene.left.name);
		int[] plindex = Team.firstAndLastPlayerIndexes(Team.LEFT_SIDE);
		for (int iter = plindex[0]; iter < plindex[1]; iter++) {
			final Player p = lscene.player[iter];
			left.elem("player").attr("unum", String.valueOf(p.unum)).attr(
					"viewQuality", p.viewStr()).attr("type", p.typeStr());
		}

		final XMLBuilder right = builder.elem("rightteam").attr("name",
				lscene.right.name);
		plindex = Team.firstAndLastPlayerIndexes(Team.RIGHT_SIDE);
		for (int iter = plindex[0]; iter < plindex[1]; iter++) {
			final Player p = lscene.player[iter];
			right.elem("player").attr("unum", String.valueOf(p.unum)).attr(
					"viewQuality", p.viewStr()).attr("type", p.typeStr());
		}

		for (final SceneAnalyzer analyzer : GameAnalyzer.analyzerList) {
			// all the scene analyzers that can output to XML will do it...
			if (Xmling.class.isInstance(analyzer)) {
				((Xmling) analyzer).xmlElement(builder);
			}
		}

		final Properties outputProperties = new Properties();
		// Explicitly identify the output as an XML document
		outputProperties.put(javax.xml.transform.OutputKeys.METHOD, "xml");

		// Pretty-print the XML output (doesn't work in all cases)
		outputProperties.put(javax.xml.transform.OutputKeys.INDENT, "yes");

		// Get 2-space indenting when using the Apache transformer
		outputProperties.put("{http://xml.apache.org/xslt}indent-amount", "2");

		// output file...
		final PrintWriter out = new PrintWriter(new File(xmlFilename));

		builder.toWriter(out, outputProperties);
		out.flush();
	}

	public static void run(int port, String treealgorithm, int window_size) throws SocketException, IOException,
	InterruptedException {

		DatagramSocket sock = new DatagramSocket();

		final String host = "localhost";
		InetSocketAddress address = new InetSocketAddress(host, port);
		NonGUISoccerScope.sendStartPacket(sock, address);
		NonGUISoccerScope.openUDPViewerConnectionAndDoLiveAnalysis(sock,
				address, treealgorithm, window_size);

	}

	private static void sendStartPacket(DatagramSocket sock,
			InetSocketAddress address) throws IOException {
		final byte[] message = "(start)".getBytes();

		// send "(start)"
		// Initialize a datagram packet with data and address
		DatagramPacket packet = new DatagramPacket(message, message.length,
				address);
		sock.send(packet);

	}

	private static void openUDPViewerConnectionAndDoLiveAnalysis(
			DatagramSocket socket, InetSocketAddress address, String treealgorithm, int window_size)
	throws InterruptedException, IOException {
		final int BUFFERSIZE = 16 * 2048;
		final String ssHost = "localhost";
		final WorldModel wm = WorldModel.getInstance();
		wm.clear();

		SoccerServerConnection ssconnection = new SoccerServerConnection(ssHost);
		SceneSetMaker ssm = new SceneSetMaker(ssconnection, wm.getSceneSet(),
				new AssistantCoachRole(socket, address, treealgorithm, window_size));
		ssconnection.dispinit();
		Thread.sleep(1000);
		ssm.start();

		// since we are connecting as a viewer we can just wait for the "(end)"
		// here by blocking.
		while (true) {
			byte[] buf = new byte[BUFFERSIZE];
			DatagramPacket pack = new DatagramPacket(buf, buf.length);
			socket.receive(pack);
			String message = (new String(buf)).trim();
			if (message.equals("(end)")) {
				System.err.println("terminating...");
				// echo the back the "(end)" message
				socket.send(pack);
				ssconnection.dispbye();
				ssm.finish();
				break;
			} else {
				System.err.println("unkown message: '" + message + "'");
			}
		}
		System.err.println("waiting for SceneSetMaker to terminate");
		ssm.join();
	}

	// private static void openUDPCoachConnection(DatagramSocket sock) {
	// final WorldModel wm = WorldModel.getInstance();
	// wm.clear(); // don't think this is necessary... but ok...
	//
	// SceneBuilder cconnection = new CoachConnection(sock);
	// SceneSetMaker ssm = new SceneSetMaker(cconnection, wm.getSceneSet());
	// ssm.run();
	// }

}
