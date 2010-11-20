package de.unibonn.inf.dbdependenciesui.ui.views.connections.helpers;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.unibonn.inf.dbdependenciesui.misc.Internationalization;

/**
 * This panel shows the welcome page.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class WelcomePanel extends JPanel {
	private static final long serialVersionUID = -2139039699973028394L;

	public WelcomePanel() {
		initialize();
	}

	private void initialize() {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// Add logo
		final Icon icon = Internationalization.getIcon("application.menu.tabs.welcome");
		final JLabel imageLabel = new JLabel(icon);
		imageLabel.setOpaque(true);
		imageLabel.setBackground(Color.white);
		imageLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		add(imageLabel, BorderLayout.NORTH);
	}
}
