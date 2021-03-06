/*
 * $Header: $
 */

package soccerscope.file;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class PlanLogFileFilter extends FileFilter {

	public final static String log = "log";
	public final static String rcg = "rcg";
	public final static String gz = "gz";
	public final static String zip = "zip";

	public PlanLogFileFilter() {
	}

	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}

		String extension = getExtension(f);
		if (extension != null) {
			if (extension.equals(log)) {
				return true;
			} else {
				return false;
			}
		}

		return false;
	}

	public String getDescription() {
		return "Agent Log";
	}

	/*
	 * Get the extension of a file.
	 */
	public static String getExtension(File f) {
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');

		if (i > 0 && i < s.length() - 1) {
			ext = s.substring(i + 1).toLowerCase();
		}
		return ext;
	}
}
