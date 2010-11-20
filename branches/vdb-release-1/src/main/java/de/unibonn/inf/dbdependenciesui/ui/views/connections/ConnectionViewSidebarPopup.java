package de.unibonn.inf.dbdependenciesui.ui.views.connections;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import de.unibonn.inf.dbdependenciesui.Configuration;
import de.unibonn.inf.dbdependenciesui.controller.ThreadExecutor;
import de.unibonn.inf.dbdependenciesui.hibernate.HibernateDAOFactory;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseConnection;
import de.unibonn.inf.dbdependenciesui.misc.Internationalization;
import de.unibonn.inf.dbdependenciesui.ui.controller.ViewController;
import de.unibonn.inf.dbdependenciesui.ui.factory.ViewFactory;

public class ConnectionViewSidebarPopup extends JPopupMenu {

	private static final Logger log = Logger.getLogger(Configuration.LOGGER);

	private static final long serialVersionUID = 8858023429181711638L;
	private final Map<String, JMenuItem> items = new HashMap<String, JMenuItem>();
	private transient final ActionListener menuListener;

	private Object userObject;

	private int nodeLevel;

	public ConnectionViewSidebarPopup() {
		super();

		menuListener = new ActionListener() {
			public void actionPerformed(final ActionEvent event) {
				final String actionCommand = event.getActionCommand();
				if (actionCommand.equals("connections.add")) {
					ConnectionViewSidebarPopup.this.addConnection();
				} else if (actionCommand.equals("connections.edit")) {
					ConnectionViewSidebarPopup.this.editConnection();
				} else if (actionCommand.equals("connections.remove")) {
					ConnectionViewSidebarPopup.this.removeConnection();
				} else if (actionCommand.equals("connections.hierarchy")) {
					ConnectionViewSidebarPopup.this.showHierarchicView();
				} else if (actionCommand.equals("connections.update")) {
					ConnectionViewSidebarPopup.this.showUpdateSchema();
				}
			}
		};

		initialize();
	}

	private void initialize() {
		createMenuItem("connections.add");
		createMenuItem("connections.edit");
		createMenuItem("connections.remove");
		addSeparator();
		createMenuItem("connections.hierarchy");
		createMenuItem("connections.update");

		setLabel(Internationalization.getText("application.popup.connections.title"));
		setBorder(new BevelBorder(BevelBorder.RAISED));
	}

	/**
	 * Creates a new menu item by given key and actionCommand.
	 * 
	 * @param key
	 * @param actionCommand
	 * @uses {@link Internationalization}
	 */
	private void createMenuItem(final String key) {
		final JMenuItem item = ViewFactory.createPopupMenuItem(key, menuListener);
		item.setHorizontalTextPosition(SwingConstants.RIGHT);
		this.add(item);

		items.put(key, item);
	}

	/**
	 * Hides (inactive) the menu item by given key.
	 * 
	 * @param key
	 */
	private void hideMenuItem(final String key) {
		items.get(key).setEnabled(false);
	}

	/**
	 * Shows (active) the menu item by given key.
	 * 
	 * @param key
	 */
	private void showMenuItem(final String key) {
		items.get(key).setEnabled(true);
	}

	private void addConnection() {
		ConnectionViewSidebarPopup.log.info("Action perform: Add a connection.");
		ViewFactory.openNewConnectionDialog();
	}

	private void editConnection() {
		ConnectionViewSidebarPopup.log.info("Action perform: edit the connection " + userObject);

		if (userObject instanceof DatabaseConnection) {
			ViewFactory.openEditConnectionDialog(((DatabaseConnection) userObject).getId());
		}
	}

	private void removeConnection() {
		ConnectionViewSidebarPopup.log.info("Action perform: Remove the connection " + userObject);

		if (userObject instanceof DatabaseConnection) {
			ViewFactory.openConfirmDeleteConnectionDialog(((DatabaseConnection) userObject).getId());
		}
	}

	protected void showHierarchicView() {
		ThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				ViewController.setViewMode(ViewController.ViewMode.HIERARCHY);
			}
		});
	}

	protected void showUpdateSchema() {
		if (userObject instanceof DatabaseConnection) {
			final DatabaseConnection connection = HibernateDAOFactory.getConnectionDAO().findById(
					((DatabaseConnection) userObject).getId(), false);
			ViewController.updateConnectionSchemaAndShowProgress(connection);
		}
	}

	/**
	 * Extended version of {@link JPopupMenu#show(Component, int, int)}. This modification stores the current selected
	 * level and node for later purpose. It changes the available actions due given selection.
	 * 
	 * @param invoker
	 * @param x
	 * @param y
	 * @param level
	 * @param userObject
	 */
	public void show(final Component invoker, final int x, final int y, final int level, final Object userObject) {
		nodeLevel = level;
		this.userObject = userObject;

		if (nodeLevel == 1) {
			showMenuItem("connections.add");
			hideMenuItem("connections.edit");
			hideMenuItem("connections.remove");
			hideMenuItem("connections.hierarchy");
			hideMenuItem("connections.update");
		} else {
			hideMenuItem("connections.add");
			showMenuItem("connections.edit");
			showMenuItem("connections.remove");
			showMenuItem("connections.hierarchy");
			showMenuItem("connections.update");
		}

		this.setLocation(x + 10, y + 10);

		this.show(invoker, x, y);
	}
}
