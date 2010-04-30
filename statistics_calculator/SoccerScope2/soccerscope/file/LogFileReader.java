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
	private final String filename;
	private final static int REC_VERSION_1 = 1;
	private final static int REC_VERSION_2 = 2;
	private final static int REC_VERSION_3 = 3;

	public LogFileReader(final String name) throws IOException {

		final FileInputStream fin = new FileInputStream(name);
		if (name.endsWith(".zip")) {
			final ZipInputStream zin = new ZipInputStream(fin);
			//ZipEntry zen =
			zin.getNextEntry();
			this.in = new BufferedInputStream(zin);
		} else if (name.endsWith(".gz")) {
			this.in = new BufferedInputStream(new GZIPInputStream(fin));
		} else {
			this.in = new BufferedInputStream(fin);
		}

		this.filename = name;

		// read logfile header
		final byte[] header = new byte[4];
		try {
			this.in.read(header);
		} catch (final IOException ie) {
			throw ie;
		}

		if (header[0] == 'U' && header[1] == 'L' && header[2] == 'G') {
			this.version = header[3];
		} else {
			throw new IOException("no version info: " + name);
		}

		switch (this.version) {
		case REC_VERSION_1:
			this.type = SceneBuilder.LOGFILE | SceneBuilder.LOGFILE_VER_1;
			break;
		case REC_VERSION_2:
			this.type = SceneBuilder.LOGFILE | SceneBuilder.LOGFILE_VER_2;
			break;
		case REC_VERSION_3:
			this.type = SceneBuilder.LOGFILE | SceneBuilder.LOGFILE_VER_3;
			break;
		default:
			throw new IOException("invalid version: " + this.version + " " + name);
		}
		System.err.print("logfile version" + this.version + ": " + name);
	}

	public String getFilename() {
		return this.filename;
	}

	public int getVersion() {
		return this.version;
	}

	public void close() {
		try {
			this.in.close();
			System.err.println("...done");
		} catch (final IOException ie) {
		}
	}

	public int readLog(final byte[] buffer) throws IOException {
		try {
			return this.readPacket(buffer);
		} catch (final IOException ie) {
			throw ie;
		}
	}

	@Override
	public int readPacket(final byte[] buffer) throws IOException {
		switch (this.version) {
		case REC_VERSION_1:
			return -1;

		case REC_VERSION_2:
			try {
				return this.readLogVer2(buffer);
			} catch (final IOException ie) {
				throw ie;
			}

		case REC_VERSION_3:
			try {
				return this.readLogVer3(buffer);
			} catch (final IOException ie) {
				throw ie;
			}

		default:
			return -1;
		}
	}

	// logging protocol ver3
	private int readLogVer3(final byte[] buffer) throws IOException {
		// read mode
		try {
			if (this.in.read(buffer, 0, 2) == -1) {
				return -1;
			}
		} catch (final IOException ie) {
			throw ie;
		}

		final int mode = this.readShort(buffer, 0);

		if (mode == SceneBuilder.SHOW_MODE) {
			try {
				if (this.in.read(buffer, 2, 1428) == -1) {
					return -1;
				}
				return 2 + 1428;
			} catch (final IOException ie) {
				throw ie;
			}
		} else if (mode == SceneBuilder.MSG_MODE) {
			try {
				// read board info & length of message
				if (this.in.read(buffer, 2, 4) == -1) {
					return -1;
				}

				int len = buffer[4];
				len = this.readShort(buffer, 4);

				if (this.in.read(buffer, 6, len) == -1) {
					return -1;
				}
				return 2 + 2 + 2 + len;
			} catch (final IOException ie) {
				throw ie;
			}
		} else if (mode == SceneBuilder.PM_MODE) {
			try {
				if (this.in.read(buffer, 2, 1) == -1) {
					return -1;
				}
				return 2 + 1;
			} catch (final IOException ie) {
				throw ie;
			}
		} else if (mode == SceneBuilder.TEAM_MODE) {
			try {
				if (this.in.read(buffer, 2, 36) == -1) {
					return -1;
				}
				return 2 + 36;
			} catch (final IOException ie) {
				throw ie;
			}
		} else if (mode == SceneBuilder.PT_MODE) {
			try {
				if (this.in.read(buffer, 2, 88) == -1) {
					return -1;
				}
				return 2 + 88;
			} catch (final IOException ie) {
				throw ie;
			}
		} else if (mode == SceneBuilder.PARAM_MODE) {
			try {
				if (this.in.read(buffer, 2, 396) == -1) {
					return -1;
				}
				return 2 + 396;
			} catch (final IOException ie) {
				throw ie;
			}
		} else if (mode == SceneBuilder.PPARAM_MODE) {
			try {
				if (this.in.read(buffer, 2, 132) == -1) {
					return -1;
				}
				return 2 + 132;
			} catch (final IOException ie) {
				throw ie;
			}
		}

		return 0;
	}

	// logging protocol ver2
	private int readLogVer2(final byte[] buffer) throws IOException {
		// read mode
		try {
			if (this.in.read(buffer, 0, 2) == -1) {
				return -1;
			}
		} catch (final IOException ie) {
			throw ie;
		}

		final int mode = this.readShort(buffer, 0);

		if (mode == SceneBuilder.SHOW_MODE) {
			try {
				if (this.in.read(buffer, 2, 316) == -1) {
					return -1;
				}
				return 2 + 316;
			} catch (final IOException ie) {
				throw ie;
			}
		} else if (mode == SceneBuilder.MSG_MODE) {
			try {
				// read board info & length of message
				if (this.in.read(buffer, 2, 4) == -1) {
					return -1;
				}

				int len = buffer[4];
				len = this.readShort(buffer, 4);

				if (this.in.read(buffer, 6, len) == -1) {
					return -1;
				}
				return 2 + 2 + 2 + len;
			} catch (final IOException ie) {
				throw ie;
			}
		}
		return 0;
	}

	@Override
	public Scene makeScene(final byte[] packet, final PlayMode playmode, final Team left,
			final Team right) {
		final Scene scene = new Scene();
		int offset = 0;

		if (this.version == LogFileReader.REC_VERSION_3) {
			scene.left = new Team(left);
			scene.right = new Team(right);
			scene.pmode = new PlayMode(playmode);

			offset = 2;
			// read ball_t
			scene.ball.mode = Ball.STAND;
			// // x, y (long)
			scene.ball.pos.x = this.readLong(packet, offset) / SceneBuilder.SHOWINFO_SCALE2;
			offset += 4;
			scene.ball.pos.y = this.readLong(packet, offset) / SceneBuilder.SHOWINFO_SCALE2;
			offset += 4;
			// // deltax, deltay (long)
			scene.ball.vel.x = this.readLong(packet, offset) / SceneBuilder.SHOWINFO_SCALE2;
			offset += 4;
			scene.ball.vel.y = this.readLong(packet, offset) / SceneBuilder.SHOWINFO_SCALE2;
			offset += 4;

			// read player_t
			for (int i = 0; i < Param.MAX_PLAYER * 2; i++) {
				// // mode (short)
				scene.player[i].mode = this.readShort(packet, offset);
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
				scene.player[i].type = this.readShort(packet, offset);
				offset += 2;

				// // x, y (long)
				scene.player[i].pos.x = this.readLong(packet, offset)
				/ SceneBuilder.SHOWINFO_SCALE2;
				offset += 4;
				scene.player[i].pos.y = this.readLong(packet, offset)
				/ SceneBuilder.SHOWINFO_SCALE2;
				offset += 4;

				// // deltax, deltay (long)
				scene.player[i].vel.x = this.readLong(packet, offset)
				/ SceneBuilder.SHOWINFO_SCALE2;
				offset += 4;
				scene.player[i].vel.y = this.readLong(packet, offset)
				/ SceneBuilder.SHOWINFO_SCALE2;
				offset += 4;

				// // body_angle (long) (radians)
				scene.player[i].angle = (float) Math.toDegrees(this.readLong(packet,
						offset)
						/ SceneBuilder.SHOWINFO_SCALE2);
				offset += 4;

				// // head_angle (long) (radians)
				scene.player[i].angleNeck = (int) Math.toDegrees(this.readLong(
						packet, offset)
						/ SceneBuilder.SHOWINFO_SCALE2);
				offset += 4;

				// // view_width (long) (radians)
				scene.player[i].angleVisible = (int) Math.toDegrees(this.readLong(
						packet, offset)
						/ SceneBuilder.SHOWINFO_SCALE2);
				offset += 4;

				// // view_quality (short)
				scene.player[i].viewQuality = this.readShort(packet, offset);
				offset += 4;

				// // stamina (long)
				scene.player[i].stamina = (int) (this.readLong(packet, offset) / SceneBuilder.SHOWINFO_SCALE2);
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
				scene.player[i].kickCount = this.readShort(packet, offset);
				offset += 2;

				// // dash_count (short)
				scene.player[i].dashCount = this.readShort(packet, offset);
				offset += 2;

				// // turn_count (short)
				scene.player[i].turnCount = this.readShort(packet, offset);
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
		} else if (this.version == LogFileReader.REC_VERSION_2) {
			offset = 2;
			final PlayMode pplaymode = this.makePlayMode(packet);
			offset += 2;

			final Team tleft = this.makeLeftTeam(packet, offset);
			final Team tright = this.makeRightTeam(packet, offset);
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
			scene.ball.pos.x = this.readShort(packet, offset) / SceneBuilder.SHOWINFO_SCALE;
			offset += 2;
			scene.ball.pos.y = this.readShort(packet, offset) / SceneBuilder.SHOWINFO_SCALE;
			offset += 2;

			// read pos_t[1-22]
			for (int i = 0; i < Param.MAX_PLAYER * 2; i++) {
				int enable;
				int side;
				int unum;
				int index;

				// // enable (short)
				enable = this.readShort(packet, offset);
				offset += 2;

				// // side (short)
				side = this.readShort(packet, offset);
				offset += 2;

				// // unum (short)
				unum = this.readShort(packet, offset);
				offset += 2;

				if (side == Team.LEFT_SIDE) {
					index = (unum - 1);
				} else {
					index = (unum - 1) + Param.MAX_PLAYER;
				}

				scene.player[index].unum = unum;
				scene.player[index].side = side;
				scene.player[index].mode = enable;

				// // angle (short)
				scene.player[index].angle = this.readShort(packet, offset);
				offset += 2;

				// // x, y (short)
				scene.player[index].pos.x = this.readShort(packet, offset)
				/ SceneBuilder.SHOWINFO_SCALE;
				offset += 2;
				scene.player[index].pos.y = this.readShort(packet, offset)
				/ SceneBuilder.SHOWINFO_SCALE;
				offset += 2;
			}
		}

		// read time (short)
		scene.time = this.readShort(packet, offset);
		offset += 2;

		// check offside
		scene.left.offsideline = 0;
		scene.right.offsideline = 0;
		final float leftx[] = new float[Param.MAX_PLAYER + 1];
		final float rightx[] = new float[Param.MAX_PLAYER + 1];
		for (int i = 0; i < Param.MAX_PLAYER; i++) {
			leftx[i] = scene.player[i].pos.x;
			rightx[i] = scene.player[i + Param.MAX_PLAYER].pos.x;
		}
		leftx[Param.MAX_PLAYER] = scene.ball.pos.x;
		rightx[Param.MAX_PLAYER] = scene.ball.pos.x;
		Arrays.sort(leftx);
		Arrays.sort(rightx);
		if (leftx[1] < 0) {
			scene.left.offsideline = leftx[1];
		}
		if (rightx[Param.MAX_PLAYER - 1] > 0) {
			scene.right.offsideline = rightx[Param.MAX_PLAYER - 1];
		}

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
