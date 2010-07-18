/*
 * $Header: $
 */

package soccerscope.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import soccerscope.model.GameEvent;
import soccerscope.model.Scene;
import soccerscope.model.SceneSet;
import soccerscope.util.analyze.AttackAnalyzer;
import soccerscope.util.analyze.BackPassAnalyzer;
import soccerscope.util.analyze.BallAverageAnalyzer;
import soccerscope.util.analyze.BallPossessionByZonesAnalyzer;
import soccerscope.util.analyze.CornerKickAnalyzer;
import soccerscope.util.analyze.DribbleAnalyzer;
import soccerscope.util.analyze.FreeKickAnalyzer;
import soccerscope.util.analyze.FreeKickFaultAnalyzer;
import soccerscope.util.analyze.GoalAnalyzer;
import soccerscope.util.analyze.GoalKickAnalyzer;
import soccerscope.util.analyze.GoalOpportunityAnalyzer;
import soccerscope.util.analyze.GoalieCatchAnalyzer;
import soccerscope.util.analyze.KickInAnalyzer;
import soccerscope.util.analyze.LostBallAnalyzer;
import soccerscope.util.analyze.OffsideAnalyzer;
import soccerscope.util.analyze.PassAnalyzer;
import soccerscope.util.analyze.PassChainAnalyzer;
import soccerscope.util.analyze.PassDistAnalyzer;
import soccerscope.util.analyze.PassMissAnalyzer;
import soccerscope.util.analyze.PassVelAnalyzer;
import soccerscope.util.analyze.PassXAnalyzer;
import soccerscope.util.analyze.SceneAnalyzer;
import soccerscope.util.analyze.ShootAnalyzer;
import soccerscope.util.analyze.TopXAnalyzer;
import soccerscope.util.analyze.WingVariationByPass;
import soccerscope.util.analyze.WingVariationByPossession;

public class GameAnalyzer {

	public static List<SceneAnalyzer> analyzerList;

	private final static String colName[] = { " ", "Left Team", "Right Team" };

	private GameAnalyzer() {
	};

	static {
		analyzerList = new ArrayList<SceneAnalyzer>();
		analyzerList.add(new GoalAnalyzer());
		analyzerList.add(new ShootAnalyzer());
		analyzerList.add(new DribbleAnalyzer());
		analyzerList.add(new GoalieCatchAnalyzer());
		analyzerList.add(new GoalKickAnalyzer());
		analyzerList.add(new OffsideAnalyzer());
		analyzerList.add(new BackPassAnalyzer());
		analyzerList.add(new CornerKickAnalyzer());
		analyzerList.add(new FreeKickAnalyzer());
		analyzerList.add(new FreeKickFaultAnalyzer());
		analyzerList.add(new KickInAnalyzer());
		// analyzerList.add(new BallPossessionAnalyzer());
		analyzerList.add(new BallPossessionByZonesAnalyzer());
		analyzerList.add(new PassAnalyzer());
		analyzerList.add(new PassMissAnalyzer());
		analyzerList.add(new LostBallAnalyzer());
		analyzerList.add(new PassXAnalyzer());
		analyzerList.add(new TopXAnalyzer());
		analyzerList.add(new PassDistAnalyzer());
		analyzerList.add(new PassVelAnalyzer());
		analyzerList.add(new PassChainAnalyzer());
		analyzerList.add(new WingVariationByPass());
		analyzerList.add(new WingVariationByPossession());
		analyzerList.add(new BallAverageAnalyzer());
		analyzerList.add(new AttackAnalyzer());
		analyzerList.add(new GoalOpportunityAnalyzer());
	}

	public static void init() {
		Iterator<SceneAnalyzer> it = analyzerList.iterator();
		while (it.hasNext()) {
			it.next().init();
		}
	}

	public static ArrayList<GameEvent> analyze(SceneSet set) {
		ArrayList<GameEvent> eventList = new ArrayList<GameEvent>();
		Iterator<Scene> sceneit = set.iterator();
		Scene scene = null;
		Scene prev = null;
		GameEvent ge;

		GameAnalyzer.init();
		while (sceneit.hasNext()) {
			prev = scene;
			scene = sceneit.next();
			Iterator<SceneAnalyzer> it = analyzerList.iterator();
			while (it.hasNext()) {
				ge = it.next().analyze(scene, prev);
				if (ge != null && !eventList.contains(ge)) {
					eventList.add(ge);
				}
			}
		}
		return eventList;
	}

	/**
	 * Analyzes the scene 'scene'. Since this is a live analysis it assumes that
	 * the previous scenes have already been analyzed, and that all the analysis
	 * request are done in order.
	 * 
	 * @param prev
	 *            - the previous scene of the scene being analyzed.
	 * @param scene
	 *            - the scene to be analyzed.
	 * @return a list of events resulting of the analysis.
	 */
	public static ArrayList<GameEvent> analyzeLive(Scene scene, Scene prev) {
		ArrayList<GameEvent> eventList = new ArrayList<GameEvent>();
		GameEvent ge;

		Iterator<SceneAnalyzer> it = analyzerList.iterator();
		while (it.hasNext()) {
			ge = it.next().analyze(scene, prev);
			if (ge != null && !eventList.contains(ge)) {
				eventList.add(ge);
			}
		}
		return eventList;

	}

	public static SceneAnalyzer getAnalyzer(String name) {
		Iterator<SceneAnalyzer> it = analyzerList.iterator();
		while (it.hasNext()) {
			SceneAnalyzer sa = it.next();
			if (name.compareTo(sa.getName()) == 0) {
				return sa;
			}
		}
		return null;
	}

	public static TableModel getTableModel() {
		TableModel dataModel = new AbstractTableModel() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public String getColumnName(int col) {
				switch (col) {
				case SceneAnalyzer.ROW_NAME:
				case SceneAnalyzer.LEFT:
				case SceneAnalyzer.RIGHT:
					return colName[col];
				default:
					return " ";
				}
			}

			public int getColumnCount() {
				return SceneAnalyzer.COL_MAX;
			}

			public int getRowCount() {
				return analyzerList.size();
			}

			public Object getValueAt(int row, int col) {
				return (analyzerList.get(row)).getValueAt(col);
			}
		};

		return dataModel;
	}

	public final static int timeSlice = 1000;

	public static TableModel getLeftTableModel() {
		TableModel dataModel = new AbstractTableModel() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public String getColumnName(int col) {
				if (col == 0) {
					return " ";
				}
				return ((col - 1) * timeSlice + 1) + " - " + (col * timeSlice);
			}

			public int getColumnCount() {
				return Time.GAME_TIME / timeSlice + 1;
			}

			public int getRowCount() {
				return analyzerList.size();
			}

			public Object getValueAt(int row, int col) {
				if (col == 0) {
					return (analyzerList.get(row))
					.getValueAt(col);
				}
				return (analyzerList.get(row)).getLeftValueAt(
						((col - 1) * timeSlice + 1), (col * timeSlice));
			}
		};

		return dataModel;
	}

	public static TableModel getRightTableModel() {
		TableModel dataModel = new AbstractTableModel() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public String getColumnName(int col) {
				if (col == 0) {
					return " ";
				}
				return ((col - 1) * timeSlice + 1) + " - " + (col * timeSlice);
			}

			public int getColumnCount() {
				return Time.GAME_TIME / timeSlice + 1;
			}

			public int getRowCount() {
				return analyzerList.size();
			}

			public Object getValueAt(int row, int col) {
				if (col == 0) {
					return (analyzerList.get(row))
					.getValueAt(col);
				}
				return (analyzerList.get(row)).getRightValueAt(
						((col - 1) * timeSlice + 1), (col * timeSlice));
			}
		};

		return dataModel;
	}
}
