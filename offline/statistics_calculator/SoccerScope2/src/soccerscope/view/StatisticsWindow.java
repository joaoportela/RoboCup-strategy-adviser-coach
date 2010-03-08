/*
 * $Header: $
 */

package soccerscope.view;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

import soccerscope.model.HeteroParam;
import soccerscope.model.Scene;
import soccerscope.util.GameAnalyzer;

public class StatisticsWindow extends JFrame {

	public final static String title = "statistics";

	private Scene scene;

	public StatisticsWindow() {
		super(title);

		// �����ѿ��ν��
		scene = Scene.createScene();

		JTabbedPane tabbedPane = new JTabbedPane();
		getContentPane().add(tabbedPane);

		/* Statistics */
		JPanel jp1 = new JPanel();
		jp1.setLayout(new BorderLayout());

		JTable table = new JTable(GameAnalyzer.getTableModel());
		jp1.add(table.getTableHeader(), BorderLayout.NORTH);
		jp1.add(table, BorderLayout.CENTER);

		tabbedPane.add("total statistics", jp1);

		/* Left Team Statistics */
		JPanel jp2 = new JPanel();
		jp2.setLayout(new BorderLayout());

		JTable ltable = new JTable(GameAnalyzer.getLeftTableModel());
		jp2.add(ltable.getTableHeader(), BorderLayout.NORTH);
		jp2.add(ltable, BorderLayout.CENTER);

		tabbedPane.add("left statistics", jp2);

		/* Right Team Statistics */
		JPanel jp3 = new JPanel();
		jp3.setLayout(new BorderLayout());

		JTable rtable = new JTable(GameAnalyzer.getRightTableModel());
		jp3.add(rtable.getTableHeader(), BorderLayout.NORTH);
		jp3.add(rtable, BorderLayout.CENTER);

		tabbedPane.add("right statistics", jp3);

		/* Hetero Parameter */
		JPanel jp4 = new JPanel();
		jp4.setLayout(new BorderLayout());

		JTable htable = new JTable(HeteroParam.getTableModel());
		jp4.add(htable.getTableHeader(), BorderLayout.NORTH);
		jp4.add(htable, BorderLayout.CENTER);

		tabbedPane.add("hetero", jp4);

		/* PassChain Table */
		JPanel jp6 = new JPanel();
		jp6.setLayout(new BorderLayout());

		JTable pstable = new JTable(GameAnalyzer.getAnalyzer("Pass Chain")
				.getTableModel());
		jp6.add(pstable.getTableHeader(), BorderLayout.NORTH);
		jp6.add(pstable, BorderLayout.CENTER);

		tabbedPane.add("pass chain", jp6);

		pack();
	}
}
