/*
 * $Header: $
 */

package soccerscope.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.net.SocketTimeoutException;

import soccerscope.model.Ball;
import soccerscope.model.Param;
import soccerscope.model.PlayMode;
import soccerscope.model.Scene;
import soccerscope.model.SceneBuilder;
import soccerscope.model.Team;

public class SoccerServerConnection extends SceneBuilder {

	public static int DEFAULT_PORT = 6000;
	public static String DEFAULT_HOST = "localhost";
	public static int DEFAULT_PROTOCOL = SceneBuilder.MONITOR_PROTOCOL_2;

	public boolean timeover = false;
	private InetAddress address;
	private String host;
	private int port;
	private DatagramSocket socket;
	private boolean timeoutset;

	public SoccerServerConnection() throws UnknownHostException,
			SocketException {
		this(DEFAULT_HOST, DEFAULT_PORT, SERVER | DEFAULT_PROTOCOL);
	}

	public SoccerServerConnection(String hostname) throws UnknownHostException,
			SocketException {
		this(hostname, DEFAULT_PORT, SERVER | DEFAULT_PROTOCOL);
	}

	public SoccerServerConnection(String hostname, int port, int protocol)
			throws UnknownHostException, SocketException {
		type = SERVER | protocol;
		this.host = hostname;
		this.port = port;
		try {
			address = InetAddress.getByName(hostname);
		} catch (UnknownHostException uhe) {
			throw uhe;
		}
		try {
			socket = new DatagramSocket();
		} catch (SocketException se) {
			throw se;
		}
	}

	public String getHostName() {
		return host;
	}

	public int getPort() {
		return port;
	}

	private void sendMessage(String msg) throws IOException {
		byte[] rawMsg = msg.getBytes();
		DatagramPacket packet = new DatagramPacket(rawMsg, rawMsg.length,
				address, port);
		try {
			socket.send(packet);
		} catch (IOException ie) {
			throw ie;
		}
	}

	public void dispinit() throws IOException {
		String msg = new String("(dispinit)");
		if ((type & MONITOR_PROTOCOL_2) == MONITOR_PROTOCOL_2) {
			msg = new String("(dispinit version 2)");
		}
		try {
			sendMessage(msg);
		} catch (IOException ie) {
			throw ie;
		}
	}

	public void dispbye() throws IOException {
		try {
			sendMessage("(dispbye)");
		} catch (IOException ie) {
			throw ie;
		}
	}

	public void dispstart() throws IOException {
		try {
			sendMessage("(dispstart)");
		} catch (IOException ie) {
			throw ie;
		}
	}

	public void dispfoul(float x, float y, int side) throws IOException {
		try {
			sendMessage("(dispfoul " + (int) (x * SHOWINFO_SCALE) + " "
					+ (int) (y * SHOWINFO_SCALE) + " " + side + ")");
		} catch (IOException ie) {
			throw ie;
		}
	}

	public void dispdiscard(int side, int unum) throws IOException {
		try {
			sendMessage("(dispdiscard " + side + " " + unum + ")");
		} catch (IOException ie) {
			throw ie;
		}
	}

	public void dispplayer(int side, int unum, float x, float y, int angle)
			throws IOException {
		try {
			sendMessage("(dispplayer " + side + " " + unum + " "
					+ (int) (x * SHOWINFO_SCALE) + " "
					+ (int) (y * SHOWINFO_SCALE) + " " + angle + ")");
		} catch (IOException ie) {
			throw ie;
		}
	}

	public int readPacket(byte[] packet) throws IOException {
		DatagramPacket udppacket = new DatagramPacket(packet, packet.length);
		try {
			if (this.timeover && !this.timeoutset) {
				// we already got a timeover. it is very likely that
				// the server will disconnect soon. NOTE: there should be
				// annother way to do this but this works. ;)
				socket.setSoTimeout(1000);
				this.timeoutset = true;
			}
			if (!this.timeover && this.timeoutset) {
				//ups... unset the timeout.
				socket.setSoTimeout(0);
				this.timeoutset = false;
			}

			socket.receive(udppacket);

		} catch (SocketTimeoutException te) {
			return -1;
		} catch (IOException ie) {
			throw ie;
		}

		return udppacket.getLength();
	}

	public Scene makeScene(byte[] packet, PlayMode playmode, Team left,
			Team right) {
		Scene scene = new Scene();
		int offset = 0;

		if ((type & MONITOR_PROTOCOL_2) == MONITOR_PROTOCOL_2) {
			offset = 4;
			scene.pmode = new PlayMode();
			scene.pmode.pmode = readChar(packet, offset);
			offset += 2;
			scene.left = makeLeftTeam(packet, offset);
			scene.right = makeRightTeam(packet, offset);
			offset += 36;

			offset += 2;

			// read ball_t
			scene.ball.mode = Ball.STAND;
			// // x, y (long)
			scene.ball.pos.x = readLong(packet, offset) / SHOWINFO_SCALE2;
			offset += 4;
			scene.ball.pos.y = readLong(packet, offset) / SHOWINFO_SCALE2;
			offset += 4;
			// // deltax, deltay (long)
			scene.ball.vel.x = readLong(packet, offset) / SHOWINFO_SCALE2;
			offset += 4;
			scene.ball.vel.y = readLong(packet, offset) / SHOWINFO_SCALE2;
			offset += 4;

			// read player_t
			for (int i = 0; i < Param.MAX_PLAYER * 2; i++) {
				// // mode (short)
				scene.player[i].mode = readShort(packet, offset);
				offset += 2;

				// // unum
				if (i < Param.MAX_PLAYER) {
					scene.player[i].unum = i + 1;
					scene.player[i].side = Team.LEFT_SIDE;
				} else {
					scene.player[i].unum = i - Param.MAX_PLAYER + 1;
					scene.player[i].side = Team.RIGHT_SIDE;
				}

				// // type (short)
				scene.player[i].type = readShort(packet, offset);
				offset += 2;

				// // x, y (long)
				scene.player[i].pos.x = readLong(packet, offset)
						/ SHOWINFO_SCALE2;
				offset += 4;
				scene.player[i].pos.y = readLong(packet, offset)
						/ SHOWINFO_SCALE2;
				offset += 4;

				// // deltax, deltay (long)
				scene.player[i].vel.x = readLong(packet, offset)
						/ SHOWINFO_SCALE2;
				offset += 4;
				scene.player[i].vel.y = readLong(packet, offset)
						/ SHOWINFO_SCALE2;
				offset += 4;

				// // body_angle (long) (radians)
				scene.player[i].angle = (int) Math.toDegrees(readLong(packet,
						offset)
						/ SHOWINFO_SCALE2);
				offset += 4;

				// // head_angle (long) (radians)
				scene.player[i].angleNeck = (int) Math.toDegrees(readLong(
						packet, offset)
						/ SHOWINFO_SCALE2);
				offset += 4;

				// // view_width (long) (radians)
				scene.player[i].angleVisible = (int) Math.toDegrees(readLong(
						packet, offset)
						/ SHOWINFO_SCALE2);
				offset += 4;

				// // view_quality (short)
				scene.player[i].viewQuality = readShort(packet, offset);
				offset += 4;

				// // stamina (long)
				scene.player[i].stamina = (int) (readLong(packet, offset) / SHOWINFO_SCALE2);
				offset += 4;

				// // effort (long)
				// scene.player[i].effort = readLong(packet, offset) /
				// Param.SHOWINFO_SCALE2;
				offset += 4;

				// // recovery (long)
				// scene.player[i].recovery = readLong(packet, offset) /
				// Param.SHOWINFO_SCALE2;
				offset += 4;

				// // kick_count (short)
				scene.player[i].kickCount = readShort(packet, offset);
				offset += 2;

				// // dash_count (short)
				scene.player[i].dashCount = readShort(packet, offset);
				offset += 2;

				// // turn_count (short)
				scene.player[i].turnCount = readShort(packet, offset);
				offset += 2;

				// // say_count (short)
				// scene.player[i].sayCount = readShort(packet, offset);
				offset += 2;

				// // turnneck_count (short)
				// scene.player[i].turnNeckCount = readShort(packet, offset);
				offset += 2;

				// // catch_count (short)
				// scene.player[i].catchCount = readShort(packet, offset);
				offset += 2;

				// // move_count (short)
				// scene.player[i].moveCount = readShort(packet, offset);
				offset += 2;

				// // chg_view_count (short)
				// scene.player[i].changeViewCount = readShort(packet, offset);
				offset += 2;
			}

		} else if ((type & MONITOR_PROTOCOL_1) == MONITOR_PROTOCOL_1) {
			offset = 2;
			PlayMode pplaymode = makePlayMode(packet);
			offset += 2;

			Team tleft = makeLeftTeam(packet, offset);
			Team tright = makeRightTeam(packet, offset);
			offset += 36;

			scene.left = new Team(tleft);
			scene.right = new Team(tright);
			scene.pmode = new PlayMode(pplaymode);

			// read ball info (pos_t[0])
			// // enable (short)
			scene.ball.mode = Ball.STAND;
			offset += 2;

			// // side (short)
			offset += 2;

			// // unum (short)
			offset += 2;

			// // angle (short)
			offset += 2;

			// // x, y (short)
			scene.ball.pos.x = readShort(packet, offset) / SHOWINFO_SCALE;
			offset += 2;
			scene.ball.pos.y = readShort(packet, offset) / SHOWINFO_SCALE;
			offset += 2;

			// read pos_t[1-22]
			for (int i = 0; i < Param.MAX_PLAYER * 2; i++) {
				int enable;
				int side;
				int unum;
				int index;

				// // enable (short)
				enable = readShort(packet, offset);
				offset += 2;

				// // side (short)
				side = readShort(packet, offset);
				offset += 2;

				// // unum (short)
				unum = readShort(packet, offset);
				offset += 2;

				if (side == Team.LEFT_SIDE)
					index = (unum - 1);
				else
					index = (unum - 1) + Param.MAX_PLAYER;

				scene.player[index].unum = unum;
				scene.player[index].side = side;
				scene.player[index].mode = enable;

				// // angle (short)
				scene.player[index].angle = readShort(packet, offset);
				offset += 2;

				// // x, y (short)
				scene.player[index].pos.x = readShort(packet, offset)
						/ SHOWINFO_SCALE;
				offset += 2;
				scene.player[index].pos.y = readShort(packet, offset)
						/ SHOWINFO_SCALE;
				offset += 2;
			}
		}
		// read time (short)
		scene.time = readUnsignedShort(packet, offset);
		offset += 2;

		// check offside
		scene.left.offsideline = 0;
		scene.right.offsideline = 0;
		float leftx[] = new float[Param.MAX_PLAYER + 1];
		float rightx[] = new float[Param.MAX_PLAYER + 1];
		for (int i = 0; i < Param.MAX_PLAYER; i++) {
			leftx[i] = scene.player[i].pos.x;
			rightx[i] = scene.player[i + Param.MAX_PLAYER].pos.x;
		}
		leftx[Param.MAX_PLAYER] = scene.ball.pos.x;
		rightx[Param.MAX_PLAYER] = scene.ball.pos.x;
		Arrays.sort(leftx);
		Arrays.sort(rightx);
		if (leftx[1] < 0)
			scene.left.offsideline = leftx[1];
		if (rightx[Param.MAX_PLAYER - 1] > 0)
			scene.right.offsideline = rightx[Param.MAX_PLAYER - 1];

		scene.left.offside = false;
		scene.right.offside = false;
		for (int i = 0; i < Param.MAX_PLAYER; i++) {
			if (scene.player[i].pos.x > scene.right.offsideline) {
				scene.player[i].offside = true;
				scene.left.offside = true;
			} else {
				scene.player[i].offside = false;
			}
			if (scene.player[i + Param.MAX_PLAYER].pos.x < scene.left.offsideline) {
				scene.player[i + Param.MAX_PLAYER].offside = true;
				scene.right.offside = true;
			} else {
				scene.player[i + Param.MAX_PLAYER].offside = false;
			}
		}

		if (scene.pmode.pmode == PlayMode.PM_TimeOver) {
			this.timeover = true;
		} else if (this.timeover) {
			System.err
					.println("WARNING: after a timeover scene occurred a non-timeover scene");
			this.timeover = false;
		}

		return scene;
	}

}
