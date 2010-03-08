/*
 * $Header: $
 */

package soccerscope.model;

import java.text.NumberFormat;
import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 * �إƥ?��������ȤΥѥ�᡼�����ݻ�륯�饹
 */
public class HeteroParam {
	private static ArrayList<HeteroParam> paramList;

	static {
		paramList = new ArrayList<HeteroParam>(22);
		// supporting 22 different player types
		for(int i = 0;  i < 22; ++i) {
			paramList.add(new HeteroParam());
		}
	}

	public static void put(int type, HeteroParam hp) {
		paramList.set(type, hp);
	}

	public static HeteroParam get(int type) {
		return paramList.get(type);
	}

	public final static int ID = 0;
	public final static int PLAYER_SPEED_MAX = 1;
	public final static int STAMINA_INC_MAX = 2;
	public final static int PLAYER_DECAY = 3;
	public final static int INERTIA_MOMENT = 4;
	public final static int DASH_POWER_RATE = 5;
	public final static int PLAYER_SIZE = 6;
	public final static int KICKABLE_MARGIN = 7;
	public final static int KICK_RAND = 8;
	public final static int EXTRA_STAMINA = 9;
	public final static int EFFORT_MAX = 10;
	public final static int EFFORT_MIN = 11;

	private final static String colName[] = { "id", "player speed max",
			"stamina inc max", "player decay", "inertia moment",
			"dash power rate", "player size", "kickable margin", "kick rand",
			"extra stamina", "effort max", "effort min" };

	public static TableModel getTableModel() {
		TableModel dataModel = new AbstractTableModel() {
			public String getColumnName(int column) {
				return colName[column];
			}

			public int getColumnCount() {
				return 12;
			}

			public int getRowCount() {
				return paramList.size();
			}

			public Object getValueAt(int row, int col) {
				if (col == ID)
					return new Integer(row);
				return HeteroParam.get(row).getValueAt(col);
			}
		};
		return dataModel;
	}

	public float player_speed_max;
	public float stamina_inc_max;
	public float player_decay;
	public float inertia_moment;
	public float dash_power_rate;
	public float player_size;
	public float kickable_margin;
	public float kick_rand;
	public float extra_stamina;
	public float effort_max;
	public float effort_min;

	public HeteroParam() {
		player_speed_max = Param.PLAYER_SPEED_MAX;
		stamina_inc_max = Param.STAMINA_INC_MAX;
		player_decay = Param.PLAYER_DECAY;
		inertia_moment = Param.INERTIA_MOMENT;
		dash_power_rate = Param.DASH_POWER_RATE;
		player_size = Param.PLAYER_SIZE;
		kickable_margin = Param.KICKABLE_MARGIN;
		kick_rand = 0.0f;
		extra_stamina = 0.0f;
		effort_max = Param.EFFORT_MAX;
		effort_min = Param.EFFORT_MIN;
	}

	public HeteroParam(HeteroParam hp) {
		this.player_speed_max = hp.player_speed_max;
		this.stamina_inc_max = hp.stamina_inc_max;
		this.player_decay = hp.player_decay;
		this.inertia_moment = hp.inertia_moment;
		this.dash_power_rate = hp.dash_power_rate;
		this.player_size = hp.player_size;
		this.kickable_margin = hp.kickable_margin;
		this.kick_rand = hp.kick_rand;
		this.extra_stamina = hp.extra_stamina;
		this.effort_max = hp.effort_max;
		this.effort_min = hp.effort_min;
	}

	public HeteroParam(float player_speed_max, float stamina_inc_max,
			float player_decay, float inertia_moment, float dash_power_rate,
			float player_size, float kickable_margin, float kick_rand,
			float extra_stamina, float effort_max, float effort_min) {
		this.player_speed_max = player_speed_max;
		this.stamina_inc_max = stamina_inc_max;
		this.player_decay = player_decay;
		this.inertia_moment = inertia_moment;
		this.dash_power_rate = dash_power_rate;
		this.player_size = player_size;
		this.kickable_margin = kickable_margin;
		this.kick_rand = kick_rand;
		this.extra_stamina = extra_stamina;
		this.effort_max = effort_max;
		this.effort_min = effort_min;
	}

	public HeteroParam(float[] params) {
		player_speed_max = params[0];
		stamina_inc_max = params[1];
		player_decay = params[2];
		inertia_moment = params[3];
		dash_power_rate = params[4];
		player_size = params[5];
		kickable_margin = params[6];
		kick_rand = params[7];
		extra_stamina = params[8];
		effort_max = params[9];
		effort_min = params[10];
	}

	public String toString() {
		NumberFormat form = NumberFormat.getInstance();
		form.setMaximumFractionDigits(3);
		form.setMinimumFractionDigits(3);
		return (form.format(player_speed_max) + " "
				+ form.format(stamina_inc_max) + " "
				+ form.format(player_decay) + " " + form.format(inertia_moment)
				+ " " + form.format(dash_power_rate) + " "
				+ form.format(player_size) + " " + form.format(kickable_margin)
				+ " " + form.format(kick_rand) + " "
				+ form.format(extra_stamina) + " " + form.format(effort_max)
				+ " " + form.format(effort_min));
	}

	public Object getValueAt(int col) {
		NumberFormat form = NumberFormat.getInstance();
		form.setMaximumFractionDigits(3);
		form.setMinimumFractionDigits(3);
		switch (col) {
		case PLAYER_SPEED_MAX:
			return form.format(player_speed_max);
		case STAMINA_INC_MAX:
			return form.format(stamina_inc_max);
		case PLAYER_DECAY:
			return form.format(player_decay);
		case INERTIA_MOMENT:
			return form.format(inertia_moment);
		case DASH_POWER_RATE:
			return form.format(dash_power_rate);
		case PLAYER_SIZE:
			return form.format(player_size);
		case KICKABLE_MARGIN:
			return form.format(kickable_margin);
		case KICK_RAND:
			return form.format(kick_rand);
		case EXTRA_STAMINA:
			return form.format(extra_stamina);
		case EFFORT_MAX:
			return form.format(effort_max);
		case EFFORT_MIN:
			return form.format(effort_min);
		}
		return " ";
	}
}
