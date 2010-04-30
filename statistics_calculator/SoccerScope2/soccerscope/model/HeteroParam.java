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
		HeteroParam.paramList = new ArrayList<HeteroParam>(22);
		// supporting 22 different player types
		for(int i = 0;  i < 22; ++i) {
			HeteroParam.paramList.add(new HeteroParam());
		}
	}

	public static void put(final int type, final HeteroParam hp) {
		HeteroParam.paramList.set(type, hp);
	}

	public static HeteroParam get(final int type) {
		return HeteroParam.paramList.get(type);
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
		final TableModel dataModel = new AbstractTableModel() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public String getColumnName(final int column) {
				return HeteroParam.colName[column];
			}

			public int getColumnCount() {
				return 12;
			}

			public int getRowCount() {
				return HeteroParam.paramList.size();
			}

			public Object getValueAt(final int row, final int col) {
				if (col == HeteroParam.ID) {
					return new Integer(row);
				}
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
		this.player_speed_max = Param.PLAYER_SPEED_MAX;
		this.stamina_inc_max = Param.STAMINA_INC_MAX;
		this.player_decay = Param.PLAYER_DECAY;
		this.inertia_moment = Param.INERTIA_MOMENT;
		this.dash_power_rate = Param.DASH_POWER_RATE;
		this.player_size = Param.PLAYER_SIZE;
		this.kickable_margin = Param.KICKABLE_MARGIN;
		this.kick_rand = 0.0f;
		this.extra_stamina = 0.0f;
		this.effort_max = Param.EFFORT_MAX;
		this.effort_min = Param.EFFORT_MIN;
	}

	public HeteroParam(final HeteroParam hp) {
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

	public HeteroParam(final float player_speed_max, final float stamina_inc_max,
			final float player_decay, final float inertia_moment, final float dash_power_rate,
			final float player_size, final float kickable_margin, final float kick_rand,
			final float extra_stamina, final float effort_max, final float effort_min) {
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

	public HeteroParam(final float[] params) {
		this.player_speed_max = params[0];
		this.stamina_inc_max = params[1];
		this.player_decay = params[2];
		this.inertia_moment = params[3];
		this.dash_power_rate = params[4];
		this.player_size = params[5];
		this.kickable_margin = params[6];
		this.kick_rand = params[7];
		this.extra_stamina = params[8];
		this.effort_max = params[9];
		this.effort_min = params[10];
	}

	@Override
	public String toString() {
		final NumberFormat form = NumberFormat.getInstance();
		form.setMaximumFractionDigits(3);
		form.setMinimumFractionDigits(3);
		return (form.format(this.player_speed_max) + " "
				+ form.format(this.stamina_inc_max) + " "
				+ form.format(this.player_decay) + " " + form.format(this.inertia_moment)
				+ " " + form.format(this.dash_power_rate) + " "
				+ form.format(this.player_size) + " " + form.format(this.kickable_margin)
				+ " " + form.format(this.kick_rand) + " "
				+ form.format(this.extra_stamina) + " " + form.format(this.effort_max)
				+ " " + form.format(this.effort_min));
	}

	public Object getValueAt(final int col) {
		final NumberFormat form = NumberFormat.getInstance();
		form.setMaximumFractionDigits(3);
		form.setMinimumFractionDigits(3);
		switch (col) {
		case PLAYER_SPEED_MAX:
			return form.format(this.player_speed_max);
		case STAMINA_INC_MAX:
			return form.format(this.stamina_inc_max);
		case PLAYER_DECAY:
			return form.format(this.player_decay);
		case INERTIA_MOMENT:
			return form.format(this.inertia_moment);
		case DASH_POWER_RATE:
			return form.format(this.dash_power_rate);
		case PLAYER_SIZE:
			return form.format(this.player_size);
		case KICKABLE_MARGIN:
			return form.format(this.kickable_margin);
		case KICK_RAND:
			return form.format(this.kick_rand);
		case EXTRA_STAMINA:
			return form.format(this.extra_stamina);
		case EFFORT_MAX:
			return form.format(this.effort_max);
		case EFFORT_MIN:
			return form.format(this.effort_min);
		}
		return " ";
	}
}
