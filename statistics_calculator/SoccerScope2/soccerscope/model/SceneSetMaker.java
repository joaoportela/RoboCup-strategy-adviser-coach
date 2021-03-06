/*
 * $Header: $
 */

package soccerscope.model;

import java.io.IOException;

import javax.swing.JProgressBar;

import soccerscope.util.Time;

public class SceneSetMaker extends Thread {
	public static interface CoachInterface {
		public void newScene(Scene current, SceneSet allScenes);
	}

	private SceneBuilder builder;
	private SceneSet sceneSet;
	private JProgressBar progress = null;
	private boolean liveAnalysis = true;
	private CoachInterface coach = null;
	private boolean running = true;

	public SceneSetMaker(SceneBuilder builder, SceneSet sceneSet) {
		this.builder = builder;
		this.sceneSet = sceneSet;
	}

	public SceneSetMaker(SceneBuilder builder, SceneSet sceneSet,
			CoachInterface coachAnalyzer) {
		this(builder, sceneSet);
		this.coach = coachAnalyzer;
	}

	public SceneSetMaker(SceneBuilder builder, SceneSet sceneSet,
			JProgressBar progress) {
		this(builder, sceneSet);
		this.progress = progress;
	}

	@Override
	public void run() {
		if ((this.builder.type & SceneBuilder.LOGFILE) == SceneBuilder.LOGFILE) {
			// no need to do live analysis for log files
			this.liveAnalysis = false;
		}
		Scene scene = this.sceneSet.lastScene();
		Team left = scene.left;
		Team right = scene.right;
		PlayMode playmode = scene.pmode;
		byte[] packet = new byte[4096];
		if (this.progress != null) {
			this.progress.setMaximum(Time.GAME_TIME);
			this.progress.setMinimum(0);
			this.progress.setValue(0);
		}
		if (this.liveAnalysis) {
			this.sceneSet.init();
		}
		System.out.println();
		//System.err.println("variables: this.liveAnalysis(" + this.liveAnalysis
		//		+ ") this.coach(" + this.coach + ")");
		while (this.isrunning()) {
			int size;
			try {
				size = this.builder.readPacket(packet);
				if (size == -1) {
					if (!this.liveAnalysis) {
						System.err.println("doing non-live analysis");
						// if we are doing live analysis we don't need to do the
						// analysis at the end.
						this.sceneSet.analyze();
					}
					if (this.progress != null) {
						this.progress.setValue(0);
					}
					// Toolkit.getDefaultToolkit().beep();
					return;

				}
			} catch (IOException ie) {
				System.err.println(ie);
				return;
			}

			switch (this.builder.getPacketType(packet)) {
			case SceneBuilder.SHOW_MODE:
				scene = this.builder.makeScene(packet, playmode, left, right);
				this.sceneSet.addScene(scene, this.liveAnalysis);
				if (this.coach != null && this.liveAnalysis) {
					this.coach.newScene(scene, this.sceneSet);
				}
				System.err.print("\r" + scene.time);
				if (this.progress != null) {
					this.progress.setValue(scene.time);
				}
				break;

			case SceneBuilder.MSG_MODE:
				this.builder.addMessage(scene, new String(packet, 6, size - 7));
				break;

			case SceneBuilder.DRAW_MODE:
				break;

			case SceneBuilder.BLANK_MODE:
				break;

			case SceneBuilder.PM_MODE:
				playmode = this.builder.makePlayMode(packet);
				break;

			case SceneBuilder.TEAM_MODE:
				left = this.builder.makeLeftTeam(packet, 2);
				right = this.builder.makeRightTeam(packet, 2);
				break;

			case SceneBuilder.PT_MODE:
				this.builder.makeHeteroParam(packet);
				break;

			case SceneBuilder.PARAM_MODE:
				this.builder.printServerParameter(packet);
				break;

			case SceneBuilder.PPARAM_MODE:
				this.builder.printPlayerParameter(packet);
				break;
			}
		}
	}

	public synchronized void finish() {
		this.running = false;
	}

	public synchronized boolean isrunning() {
		return this.running;
	}
}
