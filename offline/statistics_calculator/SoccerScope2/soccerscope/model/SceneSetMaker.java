/*
 * $Header: $
 */

package soccerscope.model;

import java.io.IOException;
import javax.swing.JProgressBar;
import soccerscope.util.Time;

public class SceneSetMaker extends Thread {

	private SceneBuilder builder;
	private SceneSet sceneSet;
	private JProgressBar progress = null;
	private boolean liveAnalysis = true;

	public SceneSetMaker(SceneBuilder builder, SceneSet sceneSet) {
		this.builder = builder;
		this.sceneSet = sceneSet;
	}

	public SceneSetMaker(SceneBuilder builder, SceneSet sceneSet,
			JProgressBar progress) {
		this.builder = builder;
		this.sceneSet = sceneSet;
		this.progress = progress;
	}

	public void run() {
		if((builder.type & SceneBuilder.LOGFILE) == SceneBuilder.LOGFILE) {
			// no need to do live analysis for log files
			this.liveAnalysis = false;
		}
		Scene scene = sceneSet.lastScene();
		Team left = scene.left;
		Team right = scene.right;
		PlayMode playmode = scene.pmode;
		byte[] packet = new byte[4096];
		if (progress != null) {
			progress.setMaximum(Time.GAME_TIME);
			progress.setMinimum(0);
			progress.setValue(0);
		}
		if (liveAnalysis) {
			sceneSet.init();
		}
		System.out.println();
		while (true) {
			int size;
			try {
				size = builder.readPacket(packet);
				if (size == -1) {
					if (!liveAnalysis) {
						System.err.println("doing non-live analysis");
						// if we are doing live analysis we don't need to do the
						// analysis at the end.
						sceneSet.analyze();
					}
					if (progress != null)
						progress.setValue(0);
					// Toolkit.getDefaultToolkit().beep();
					return;

				}
			} catch (IOException ie) {
				System.err.println(ie);
				return;
			}

			switch (builder.getPacketType(packet)) {
			case SceneBuilder.SHOW_MODE:
				scene = builder.makeScene(packet, playmode, left, right);
				sceneSet.addScene(scene, liveAnalysis);
				if (scene.time == 6000)
					System.err.println(scene.time + "!: pmode(" + scene.pmode
							+ ") ");
				System.err.print("\r" + scene.time);
				if (progress != null)
					progress.setValue(scene.time);
				break;

			case SceneBuilder.MSG_MODE:
				builder.addMessage(scene, new String(packet, 6, size - 7));
				break;

			case SceneBuilder.DRAW_MODE:
				break;

			case SceneBuilder.BLANK_MODE:
				break;

			case SceneBuilder.PM_MODE:
				playmode = builder.makePlayMode(packet);
				break;

			case SceneBuilder.TEAM_MODE:
				left = builder.makeLeftTeam(packet, 2);
				right = builder.makeRightTeam(packet, 2);
				break;

			case SceneBuilder.PT_MODE:
				builder.makeHeteroParam(packet);
				break;

			case SceneBuilder.PARAM_MODE:
				builder.printServerParameter(packet);
				break;

			case SceneBuilder.PPARAM_MODE:
				builder.printPlayerParameter(packet);
				break;
			}
		}
	}
}
