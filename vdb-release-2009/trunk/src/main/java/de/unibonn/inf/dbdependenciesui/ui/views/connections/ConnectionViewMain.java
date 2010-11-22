/**
 * $Id: Main.java 41 2009-03-01 16:21:20Z philipp $
 */
package de.unibonn.inf.dbdependenciesui.ui.views.connections;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.logging.Logger;

import javax.swing.JPanel;

import de.unibonn.inf.dbdependenciesui.Configuration;
import de.unibonn.inf.dbdependenciesui.ui.views.connections.helpers.ConnectionTabbedPane;

/**
 * The main panel of the connection view displays all available connections and their contents.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class ConnectionViewMain extends JPanel {

	protected static final Logger log = Logger.getLogger(Configuration.LOGGER);

	private static final long serialVersionUID = 1946737732252046428L;

	private static ConnectionViewMain main = null;

	private ConnectionTabbedPane tabbedPane;

	public static ConnectionViewMain getInstance() {
		if (ConnectionViewMain.main == null) {
			ConnectionViewMain.main = new ConnectionViewMain();
		}
		return ConnectionViewMain.main;
	}

	private ConnectionViewMain() {
		super();
		initialize();
	}

	private void initialize() {
		setLayout(new BorderLayout());
		tabbedPane = new ConnectionTabbedPane();
		tabbedPane.setPreferredSize(new Dimension(650, 500));
		add(tabbedPane, BorderLayout.CENTER);
	}

	public void addTab(final String tabbedName, final String connection, final String item, final String element) {
		tabbedPane.addTabByParams(tabbedName, connection, item, element);
	}

	public ConnectionTabbedPane getConnectionTabbedPane() {
		return tabbedPane;
	}

	public static enum TabMode {
		COLUMNS, DATA, CONSTAINTS
	}
}
