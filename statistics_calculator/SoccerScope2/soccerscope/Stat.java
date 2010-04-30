/*
 * $Log: Stat.java,v $
 * Revision 1.1  2003/01/14 05:53:13  koji
 * �?�ե�������ײ����ѽ���
 *
 */

package soccerscope;

import javax.swing.table.TableModel;

import soccerscope.file.LogFileReader;
import soccerscope.model.SceneSetMaker;
import soccerscope.model.WorldModel;
import soccerscope.util.GameAnalyzer;

public class Stat {
	public static void main(String args[]) {
		// init worldmodel
		WorldModel wm = WorldModel.getInstance();

		for (int k = 0; k < args.length; k++) {
			try {
				wm.clear();
				LogFileReader lfr = new LogFileReader(args[k]);
				SceneSetMaker ssm = new SceneSetMaker(lfr, wm.getSceneSet());
				ssm.start();
				ssm.join();
				System.err.println();
				TableModel tb = GameAnalyzer.getTableModel();
				System.out.println(args[k]);
				for (int i = 1; i < tb.getColumnCount(); i++) {
					for (int j = 0; j < tb.getRowCount(); j++) {
						System.out.print(tb.getValueAt(j, i) + ", ");
					}
					System.out.println();
				}
			} catch (Exception ie) {
				System.err.println(ie);
			}
		}
	}
}
