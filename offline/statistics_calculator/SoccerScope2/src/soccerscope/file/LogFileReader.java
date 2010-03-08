/*
 * $Header: $
 */

package soccerscope.file;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import soccerscope.model.Ball;
import soccerscope.model.Param;
import soccerscope.model.PlayMode;
import soccerscope.model.Scene;
import soccerscope.model.SceneBuilder;
import soccerscope.model.Team;

public class LogFileReader extends SceneBuilder {

	public final static int BUFFER_SIZE = 2048;

	private InputStream in;
	private int version;
	private String filename;
	private final static int REC_VERSION_1 = 1;
	private final static int REC_VERSION_2 = 2;
	private final static int REC_VERSION_3 = 3;

	public LogFileReader(String name) throws IOException {

		FileInputStream fin = new FileInputStream(name);
		if (name.endsWith(".zip")) {
			ZipInputStream zin = new ZipInputStream(fin);
			ZipEntry zen = zin.getNextEntry();
			in = new BufferedInputStream(zin);
		} else if (name.endsWith(".gz")) {
			in = new BufferedInputStream(new GZIPInputStream(fin));
		} else {
			in = new BufferedInputStream(fin);
		}

		filename = name;

		// read logfile header
		byte[] header = new byte[4];
		try {
			in.read(header);
		} catch (IOException ie) {
			throw ie;
		}

		if (header[0] == 'U' && header[1] == 'L' && header[2] == 'G') {
			version = header[3];
		} else {
			throw new IOException("no version info: " + name);
		}

		switch (version) {
		case REC_VERSION_1:
			type = LOGFILE | LOGFILE_VER_1;
			break;
		case REC_VERSION_2:
			type = LOGFILE | LOGFILE_VER_2;
			break;
		case REC_VERSION_3:
			type = LOGFILE | LOGFILE_VER_3;
			break;
		default:
			throw new IOException("invalid version: " + version + " " + name);
		}
		System.err.print("logfile version" + version + ": " + name);
	}

	public String getFilename() {
		return filename;
	}

	public int getVersion() {
		return version;
	}

	public void close() {
		try {
			in.close();
			System.err.println("...done");
		} catch (IOException ie) {
		}
	}

	public int readLog(byte[] buffer) throws IOException {
		try {
			return readPacket(buffer);
		} catch (IOException ie) {
			throw ie;
		}
	}

	public int readPacket(byte[] buffer) throws IOException {
		switch (version) {
		case REC_VERSION_1:
			return -1;

		case REC_VERSION_2:
			try {
				return readLogVer2(buffer);
			} catch (IOException ie) {
				throw ie;
			}

		case REC_VERSION_3:
			try {
				return readLogVer3(buffer);
			} catch (IOException ie) {
				throw ie;
			}

		default:
			return -1;
		}
	}

	// logging protocol ver3
	private int readLogVer3(byte[] buffer) throws IOException {
		// read mode
		try {
			if (in.read(buffer, 0, 2) == -1) {
				return -1;
			}
		} catch (IOException ie) {
			throw ie;
		}

		int mode = readShort(buffer, 0);

		if (mode == SHOW_MODE) {
			try {
				if (in.read(buffer, 2, 1428) == -1) {
					return -1;
				}
				return 2 + 1428;
			} catch (IOException ie) {
				throw ie;
			}
		} else if (mode == MSG_MODE) {
			try {
				// read board info & length of message
				if (in.read(buffer, 2, 4) == -1) {
					return -1;
				}

				int len = buffer[4];
				len = readShort(buffer, 4);

				if (in.read(buffer, 6, len) == -1) {
					return -1;
				}
				return 2 + 2 + 2 + len;
			} catch (IOException ie) {
				throw ie;
			}
		} else if (mode == PM_MODE) {
			try {
				if (in.read(buffer, 2, 1) == -1) {
					return -1;
				}
				return 2 + 1;
			} catch (IOException ie) {
				throw ie;
			}
		} else if (mode == TEAM_MODE) {
			try {
				if (in.read(buffer, 2, 36) == -1) {
					return -1;
				}
				return 2 + 36;
			} catch (IOException ie) {
				throw ie;
			}
		} else if (mode == PT_MODE) {
			try {
				if (in.read(buffer, 2, 88) == -1) {
					return -1;
				}
				return 2 + 88;
			} catch (IOException ie) {
				throw ie;
			}
		} else if (mode == PARAM_MODE) {
			try {
				if (in.read(buffer, 2, 396) == -1) {
					return -1;
				}
				return 2 + 396;
			} catch (IOException ie) {
				throw ie;
			}
		} else if (mode == PPARAM_MODE) {
			try {
				if (in.read(buffer, 2, 132) == -1) {
					return -1;
				}
				return 2 + 132;
			} catch (IOException ie) {
				throw ie;
			}
		}

		return 0;
	}

	// logging protocol ver2
	private int readLogVer2(byte[] buffer) throws IOException {
		// read mode
		try {
			if (in.read(buffer, 0, 2) == -1) {
				return -1;
			}
		} catch (IOException ie) {
			throw ie;
		}

		int mode = readShort(buffer, 0);

		if (mode == SHOW_MODE) {
			try {
				if (in.read(buffer, 2, 316) == -1) {
					return -1;
				}
				return 2 + 316;
			} catch (IOException ie) {
				throw ie;
			}
		} else if (mode == MSG_MODE) {
			try {
				// read board info & length of message
				if (in.read(buffer, 2, 4) == -1) {
					return -1;
				}

				int len = buffer[4];
				len = readShort(buffer, 4);

				if (in.read(buffer, 6, len) == -1) {
					return -1;
				}
				return 2 + 2 + 2 + len;
			} catch (IOException ie) {
				throw ie;
			}
		}
		return 0;
	}

	public Scene makeScene(byte[] packet, PlayMode playmode, Team left,
			Team right) {
		Scene scene = new Scene();
		int offset = 0;

		if (version == REC_VERSION_3) {
			scene.left = new Team(left);
			scene.right = new Team(right);
			scene.pmode = new PlayMode(playmode);

			offset = 2;
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
				scene.player[i].angle = (float) Math.toDegrees(readLong(packet,
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
				// SHOWINFO_SCALE2;
				offset += 4;

				// // recovery (long)
				// scene.player[i].recovery = readLong(packet, offset) /
				// SHOWINFO_SCALE2;
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
		} else if (version == REC_VERSION_2) {
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
		scene.time = readShort(packet, offset);
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

		return scene;
	}
}
