/*
 * $Log: StatusBar.java,v $
 * Revision 1.4  2002/10/22 05:15:14  koji
 * ScopePane:�⡼����˥ꥹ�ʡ����Ѱ�
 *
 */

package soccerscope.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JToggleButton;

import soccerscope.view.layer.Layer;

public class StatusBar extends JPanel {

	private FieldPane fieldPane;
	private InfoBar infoBar;

	private JLabel statusLabel;
	private JProgressBar progressBar;

	private Color foregroundColor;
	private Color backgroundColor;

	public StatusBar(ScopePane scopePane) {
		super();

		this.fieldPane = scopePane.getFieldPane();
		this.infoBar = scopePane.getInfoBar();

		statusLabel = new JLabel("status");
		progressBar = new JProgressBar();
		foregroundColor = statusLabel.getForeground();
		backgroundColor = statusLabel.getBackground().darker();

		this.setLayout(new BorderLayout());
		JPanel jp = new JPanel();
		jp.add(statusLabel);
		jp.add(progressBar);
		this.add(jp, BorderLayout.WEST);
		JPanel jp2 = new JPanel();
		ArrayList list = scopePane.getShowLayer();
		Iterator it = list.iterator();
		while (it.hasNext()) {
			Layer layer = (Layer) it.next();
			JToggleButton button = layer.createJToggleButton();
			button.setIcon(new ImageIcon(ClassLoader
					.getSystemResource("soccerscope/image/"
							+ layer.getLayerName() + ".gif")));
			button.setText(null);
			button.setMargin(new Insets(0, 0, 0, 0));
			jp2.add(button);
		}
		this.add(jp2, BorderLayout.CENTER);

		setVisible(true);
	}

	public JProgressBar getProgressBar() {
		return progressBar;
	}

	public void setStatus(String s) {
		statusLabel.setText(s);
	}
}
