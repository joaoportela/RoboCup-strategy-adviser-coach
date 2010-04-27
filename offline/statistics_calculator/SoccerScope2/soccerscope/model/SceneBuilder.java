/*
 * $Header: $
 */

package soccerscope.model;

import java.io.IOException;
import java.util.StringTokenizer;

import soccerscope.util.Time;

public abstract class SceneBuilder {
	public final static int NO_SOURCE = 0x000;
	public final static int SERVER = 0x001;
	public final static int MONITOR_PROTOCOL_1 = 0x002;
	public final static int MONITOR_PROTOCOL_2 = 0x004;
	public final static int LOGFILE = 0x010;
	public final static int LOGFILE_VER_1 = 0x020;
	public final static int LOGFILE_VER_2 = 0x040;
	public final static int LOGFILE_VER_3 = 0x080;
	protected int type;

	public int getType() {
		return type;
	}

	public final static int NO_INFO = 0;
	public final static int SHOW_MODE = 1;
	public final static int MSG_MODE = 2;
	public final static int DRAW_MODE = 3;
	public final static int BLANK_MODE = 4;
	public final static int PM_MODE = 5;
	public final static int TEAM_MODE = 6;
	public final static int PT_MODE = 7;
	public final static int PARAM_MODE = 8;
	public final static int PPARAM_MODE = 9;

	public static String packetTypeToString(int type) {
		switch (type) {
		case SceneBuilder.NO_INFO:
			return "NO_INFO";
		case SceneBuilder.SHOW_MODE:
			return "SHOW_MODE";
		case SceneBuilder.MSG_MODE:
			return "MSG_MODE";
		case SceneBuilder.DRAW_MODE:
			return "DRAW_MODE";
		case SceneBuilder.BLANK_MODE:
			return "BLANK_MODE";
		case SceneBuilder.PM_MODE:
			return "PM_MODE";
		case SceneBuilder.TEAM_MODE:
			return "TEAM_MODE";
		case SceneBuilder.PT_MODE:
			return "PT_MODE";
		case SceneBuilder.PARAM_MODE:
			return "PARAM_MODE";
		case SceneBuilder.PPARAM_MODE:
			return "PPARAM_MODE";
		default:
			throw new IllegalArgumentException(type + " argument is not valid");
		}
	}

	public int getPacketType(byte[] packet) {
		return readShort(packet, 0);
	};

	public final static float SHOWINFO_SCALE = 16.0f;
	public final static float SHOWINFO_SCALE2 = 65536.0f;

	abstract public int readPacket(byte[] packet) throws IOException;

	abstract public Scene makeScene(byte[] packet, PlayMode playmode,
			Team left, Team right);

	public PlayMode makePlayMode(byte[] packet) {
		PlayMode playmode = new PlayMode();

		// pmode (char)
		playmode.pmode = readChar(packet, 2);
		return playmode;
	}

	public Team makeLeftTeam(byte[] packet, int offset) {
		Team team = new Team(Team.LEFT_SIDE);

		int i;
		for (i = 0; i < 16; i++)
			if (readChar(packet, i + offset) == 0)
				break;
		team.name = new String(packet, offset, i);
		offset += 16;
		team.score = readShort(packet, offset);
		offset += 2;
		return team;
	}

	public Team makeRightTeam(byte[] packet, int offset) {
		Team team = new Team(Team.RIGHT_SIDE);

		offset += 16;
		offset += 2;

		int i;
		for (i = 0; i < 16; i++)
			if (readChar(packet, i + offset) == 0)
				break;
		team.name = new String(packet, offset, i);
		offset += 16;
		team.score = readShort(packet, offset);
		offset += 2;
		return team;
	}

	public void addMessage(Scene scene, String message) {
		// kick-off���ϼΤ�
		if (scene.time <= 0 || scene.time == Time.MAX_TIME) {
			return;
		}

		// System.out.println(scene.time + " " + message);

		// header sender command���ڤ�ʬ��
		StringTokenizer messageTokenizer = new StringTokenizer(message, " ");
		String header = messageTokenizer.nextToken();
		if (header.compareTo("Recv") != 0)
			return;
		String sender = messageTokenizer.nextToken();
		StringBuffer command = new StringBuffer(messageTokenizer.nextToken());
		while (messageTokenizer.hasMoreTokens()) {
			command.append(" " + messageTokenizer.nextToken());
		}

		// System.out.println("header: " + header);
		// System.out.println("sender: " + sender);
		// System.out.println("command: " + command);

		int lastindex = sender.lastIndexOf(':');
		String coach = sender.substring(0, lastindex);
		if (coach.compareTo("Coach") == 0) {
			return;
		}

		// sender �����
		int endindex = sender.lastIndexOf('_');
		String teamname = sender.substring(0, endindex);
		sender = sender.replace(':', ' ');
		sender = sender.replace('_', ' ');
		sender = sender.trim();
		int unum;
		try {
			unum = Integer.parseInt(sender.substring(endindex + 1));
		} catch (NumberFormatException ne) {
			return;
		}
		Player player;
		if (teamname.compareTo(scene.left.name) == 0) {
			player = scene.player[unum - 1];
		} else {
			player = scene.player[unum - 1 + Param.MAX_PLAYER];
		}

		// command �����
		StringTokenizer commandTokenizer = new StringTokenizer(command
				.toString(), " ");
		String commandType = commandTokenizer.nextToken();
		String arg1 = null, arg2 = null;
		if (commandType.compareTo("(say") == 0) {
			arg1 = command.substring(new String("(say ").length(), command
					.length() - 2);
			player.sayMessage = arg1;

		} else if (commandType.compareTo("(dash") == 0) {
			// ���ޥ�ɰ���ν���
			arg1 = commandTokenizer.nextToken();
			arg1 = arg1.replace(')', ' ');
			arg1 = arg1.trim();
			try {
				player.acc = player.getDashAccelerate(Float.parseFloat(arg1));
			} catch (NumberFormatException ne) {
				return;
			}

		} else if (commandType.compareTo("(kick") == 0) {
			// ���ޥ�ɰ���ν���
			arg1 = commandTokenizer.nextToken();
			arg1 = arg1.replace(')', ' ');
			arg1 = arg1.trim();
			if (commandTokenizer.hasMoreTokens()) {
				arg2 = commandTokenizer.nextToken();
				arg2 = arg2.replace(')', ' ');
				arg2 = arg2.trim();
			}
			// ���å��ˤ���®�٤η׻�
			if (player.isKickable(scene.ball.pos)) {
				try {
					scene.ball.acc.add(player.getKickAccelerate(scene.ball.pos,
							Float.parseFloat(arg1), Float.parseFloat(arg2)));
					if (scene.ball.acc.length() > Param.BALL_ACCEL_MAX) {
						scene.ball.acc = scene.ball.acc.normalize();
						scene.ball.acc.scale(Param.BALL_ACCEL_MAX);
					}
				} catch (NumberFormatException ne) {
					return;
				}
			}

		} else if (commandType.compareTo("(catch") == 0) {
			// ���ޥ�ɰ���ν���
			arg1 = commandTokenizer.nextToken();
			arg1 = arg1.replace(')', ' ');
			arg1 = arg1.trim();
			try {
				player.catchDir = Float.parseFloat(arg1);
			} catch (NumberFormatException ne) {
				return;
			}

		} else if (commandType.compareTo("(turn") == 0) {

		} else if (commandType.compareTo("(turn_neck") == 0) {

		} else if (commandType.compareTo("(tackle") == 0) {

		} else {

		}
	}

	public void makeHeteroParam(byte[] packet) {
		int offset = 0;

		offset += 2;
		int id = 0;
		if ((type & LOGFILE) == LOGFILE) {
			id = readShort(packet, offset);
			offset += 2;
			offset += 2; // padding
		} else if ((type & SERVER) == SERVER) {
			offset += 2; // padding
			id = readShort(packet, offset);
			offset += 2;
			offset += 2; // padding
		}
		double player_speed_max = readLong(packet, offset) / 65535.0;
		offset += 4;
		double stamina_inc_max = readLong(packet, offset) / 65535.0;
		offset += 4;
		double player_decay = readLong(packet, offset) / 65535.0;
		offset += 4;
		double inertia_moment = readLong(packet, offset) / 65535.0;
		offset += 4;
		double dash_power_rate = readLong(packet, offset) / 65535.0;
		offset += 4;
		double player_size = readLong(packet, offset) / 65535.0;
		offset += 4;
		double kickable_margin = readLong(packet, offset) / 65535.0;
		offset += 4;
		double kick_rand = readLong(packet, offset) / 65535.0;
		offset += 4;
		double extra_stamina = readLong(packet, offset) / 65535.0;
		offset += 4;
		double effort_max = readLong(packet, offset) / 65535.0;
		offset += 4;
		double effort_min = readLong(packet, offset) / 65535.0;
		offset += 4;

		HeteroParam.put(id, new HeteroParam((float) player_speed_max,
				(float) stamina_inc_max, (float) player_decay,
				(float) inertia_moment, (float) dash_power_rate,
				(float) player_size, (float) kickable_margin,
				(float) kick_rand, (float) extra_stamina, (float) effort_max,
				(float) effort_min));

	}

	public void printServerParameter(byte[] packet) {

	}

	public void printPlayerParameter(byte[] packet) {

	}

	// 4byte�������ɤ߹���
	protected long readLong(byte[] buf, int offset) {
		return ((buf[offset] & 0xff) << 24) | ((buf[offset + 1] & 0xff) << 16)
				| ((buf[offset + 2] & 0xff) << 8) | ((buf[offset + 3] & 0xff));
	}

	// 2byte�������ɤ߹���
	protected int readShort(byte[] buf, int offset) {
		return (short) ((buf[offset] & 0xff) << 8) | (buf[offset + 1] & 0xff);
	}

	// 2byte���̵���������ɤ߹���
	protected int readUnsignedShort(byte[] buf, int offset) {
		return ((buf[offset] & 0xff) << 8) | (buf[offset + 1] & 0xff);
	}

	// 1byte�������ɤ߹���
	protected int readChar(byte[] buf, int offset) {
		return (buf[offset] & 0xff);
	}
}
