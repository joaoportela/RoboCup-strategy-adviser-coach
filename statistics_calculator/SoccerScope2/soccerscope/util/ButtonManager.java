/*
 * $Header: $
 */

package soccerscope.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.AbstractButton;

public class ButtonManager {

	private static HashMap map = new HashMap();

	private ButtonManager() {
	};

	public static void addButton(String group, AbstractButton button) {
		ArrayList array;
		boolean enable = true;
		boolean select = false;
		if (map.containsKey(group)) {
			array = (ArrayList) map.get(group);
			enable = ((AbstractButton) array.get(0)).isEnabled();
			select = ((AbstractButton) array.get(0)).isSelected();
		} else {
			array = new ArrayList();
			map.put(group, array);
		}
		array.add(button);
		button.setEnabled(enable);
		button.setSelected(select);
	}

	public static void setEnabled(String group, boolean enable) {
		if (map.containsKey(group)) {
			ArrayList array = (ArrayList) map.get(group);
			Iterator it = array.iterator();
			while (it.hasNext()) {
				((AbstractButton) it.next()).setEnabled(enable);
			}
		}
	}

	public static void setSelected(String group, boolean select) {
		if (map.containsKey(group)) {
			ArrayList array = (ArrayList) map.get(group);
			Iterator it = array.iterator();
			while (it.hasNext()) {
				((AbstractButton) it.next()).setSelected(select);
			}
		}
	}

	public static void printAllKeys() {
		Iterator it = map.keySet().iterator();
		while (it.hasNext()) {
			System.out.println(it.next());
		}
	}
}
