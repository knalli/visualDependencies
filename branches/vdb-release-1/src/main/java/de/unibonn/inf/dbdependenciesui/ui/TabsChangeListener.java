package de.unibonn.inf.dbdependenciesui.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import de.unibonn.inf.dbdependenciesui.ui.views.connections.helpers.ConnectionTabbedPane;

/**
 * This property change listener is used for notifications between the tabbed pane {@link ConnectionTabbedPane} and the
 * menubar {@link ApplicationViewMenuBar}.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class TabsChangeListener implements PropertyChangeListener {

	private final ApplicationViewMenuBar menuBar;

	public TabsChangeListener(final ApplicationViewMenuBar menuBar) {
		this.menuBar = menuBar;
	}

	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
		final String propertyName = evt.getPropertyName();
		if ("openTabItem".equals(propertyName)) {
			menuBar.addConnectionTab((String) evt.getNewValue());
		} else if ("closeTabItem".equals(propertyName)) {
			menuBar.removeConnectionTab((String) evt.getOldValue());
		} else if ("selectTabItem".equals(propertyName)) {
			menuBar.setCurrentConnectionTab((String) evt.getNewValue());
		}
	}

}
