/**
 * $Id: Main.java 41 2009-03-01 16:21:20Z philipp $
 */
package de.unibonn.inf.dbdependenciesui.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import de.unibonn.inf.dbdependenciesui.Configuration;
import de.unibonn.inf.dbdependenciesui.TestFixtures;
import de.unibonn.inf.dbdependenciesui.controller.Controller;
import de.unibonn.inf.dbdependenciesui.controller.ThreadExecutor;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseConnection;
import de.unibonn.inf.dbdependenciesui.ui.controller.ViewController;
import de.unibonn.inf.dbdependenciesui.ui.controller.ViewController.Notification;
import de.unibonn.inf.dbdependenciesui.ui.controller.ViewController.ViewMode;
import de.unibonn.inf.dbdependenciesui.ui.factory.ViewFactory;
import de.unibonn.inf.dbdependenciesui.ui.helpers.SystemTools;

/**
 * This is the application's menubar. The menubar contains several global
 * commands and opener calls for new windows. For mac osx, there will be no quit
 * command since this command is already a part of a programm.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class ApplicationViewMenuBar extends JMenuBar implements Observer {
	private static final long serialVersionUID = -8260974070414134287L;

	private static final String FILE = "file";
	private static final String FILE_ADD = "file.addconnection";
	private static final String FILE_QUIT = "file.quit";
	private static final String HELP = "help";
	private static final String HELP_ABOUT = "help.about";
	private static final String HELP_LOG = "help.log";
	private static final String HELP_LICENSE = "help.license";
	private static final String HELP_SETTINGS = "help.settings";
	private static final String HELP_INFORMATION = "help.information";
	private static final String TABS = "tabs";
	private static final String TABS_PREVIOUS = "tabs.previous";
	private static final String TABS_NEXT = "tabs.next";
	private static final String TABS_CLOSE = "tabs.close";
	private static final String TABS_CLOSEALL = "tabs.closeall";
	private static final String TABS_CLOSEOTHERS = "tabs.closeothers";

	private static final String DEBUG = "debug";
	private static final String DEBUG_DEMO1 = "debug.democonnection1";
	private static final String DEBUG_DEMO2 = "debug.democonnection2";

	private JMenu menuItemTabs = null;

	private final List<String> tabs = new ArrayList<String>();
	private final Map<String, JCheckBoxMenuItem> tabItems = new HashMap<String, JCheckBoxMenuItem>();
	private final Map<String, JMenuItem> items = new HashMap<String, JMenuItem>();
	private final ButtonGroup checkBoxGroup = new ButtonGroup();

	private String currentTabTitle;
	private int currentTabIndex;

	private ActionListener generalActionListener;
	private ActionListener tabItemActionListener;

	public ApplicationViewMenuBar() {
		initialize();

		ViewController.addObserverObject(this);
	}

	private void initialize() {

		// Create actionlistener for static menu items.
		generalActionListener = new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final String actionCommand = e.getActionCommand();

				if ((actionCommand == null) || actionCommand.isEmpty()) {
					return;
				}

				if (actionCommand.equals(FILE_ADD)) {
					ViewFactory.openNewConnectionDialog();
				} else if (actionCommand.equals(HELP_LOG)) {
					ViewController.showLogView();
				} else if (actionCommand.equals(HELP_LICENSE)) {
					ViewController.showLicenseView();
				} else if (actionCommand.equals(HELP_SETTINGS)) {
					ViewController.showUserSettingsView();
				} else if (actionCommand.equals(HELP_INFORMATION)) {
					ViewController.showHelpView();
				} else if (actionCommand.equals(HELP_ABOUT)) {
					ViewController.showAboutView();
				} else if (actionCommand == FILE_QUIT) {
					System.exit(0);
				} else if (actionCommand == TABS_CLOSE) {
					ViewController.removeConnectionTab(currentTabTitle);
				} else if (actionCommand == TABS_NEXT) {
					final int index = Math.min(tabs.size() - 1,
							currentTabIndex + 1);
					ViewController.setCurrentConnectionTab(tabs.get(index));
				} else if (actionCommand == TABS_PREVIOUS) {
					final int index = Math.max(currentTabIndex - 1, 0);
					ViewController.setCurrentConnectionTab(tabs.get(index));
				} else if (actionCommand == TABS_CLOSEALL) {
					ViewController.removeAllConnectionTabs();
				} else if (actionCommand == TABS_CLOSEOTHERS) {
					ViewController
							.removeAllConnectionTabsExceptThis(currentTabTitle);
				} else if (actionCommand == DEBUG_DEMO1) {
					ApplicationViewMenuBar.this
							.createDemoConnection(actionCommand);
				} else if (actionCommand == DEBUG_DEMO2) {
					ApplicationViewMenuBar.this
							.createDemoConnection(actionCommand);
				}
			}
		};

		// Create actionlistener for generated tab items.
		tabItemActionListener = new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				ViewController.setCurrentConnectionTab(e.getActionCommand());
			}
		};

		JMenu menu;
		JMenuItem item;

		menu = ViewFactory.createMenu(FILE);
		this.add(menu);
		menu.add(ViewFactory.createMenuItem(FILE_ADD, generalActionListener));
		if (!SystemTools.isMac()) {
			menu.add(ViewFactory.createMenuItem(FILE_QUIT,
					generalActionListener));
		}

		menu = ViewFactory.createMenu(TABS);
		menuItemTabs = menu;
		this.add(menu);
		item = ViewFactory.createMenuItem(TABS_PREVIOUS, generalActionListener);
		items.put(TABS_PREVIOUS, item);
		menu.add(item);
		item = ViewFactory.createMenuItem(TABS_NEXT, generalActionListener);
		items.put(TABS_NEXT, item);
		menu.add(item);
		menu.addSeparator();
		item = ViewFactory.createMenuItem(TABS_CLOSE, generalActionListener);
		items.put(TABS_CLOSE, item);
		menu.add(item);
		item = ViewFactory.createMenuItem(TABS_CLOSEALL, generalActionListener);
		items.put(TABS_CLOSEALL, item);
		menu.add(item);
		item = ViewFactory.createMenuItem(TABS_CLOSEOTHERS,
				generalActionListener);
		items.put(TABS_CLOSEOTHERS, item);
		menu.add(item);
		menu.addSeparator();

		menu = ViewFactory.createMenu(HELP);
		this.add(menu);
		menu.add(ViewFactory.createMenuItem(HELP_ABOUT, generalActionListener));
		menu.add(ViewFactory.createMenuItem(HELP_LOG, generalActionListener));
		menu.add(ViewFactory.createMenuItem(HELP_SETTINGS,
				generalActionListener));
		menu.add(ViewFactory.createMenuItem(HELP_INFORMATION,
				generalActionListener));
		menu.addSeparator();
		menu.add(ViewFactory
				.createMenuItem(HELP_LICENSE, generalActionListener));

		if (Configuration.getBoolean("debugconnection.enabled")) {
			menu = ViewFactory.createMenu(DEBUG);
			this.add(menu);
			menu.add(ViewFactory.createMenuItem(DEBUG_DEMO1,
					generalActionListener));
			menu.add(ViewFactory.createMenuItem(DEBUG_DEMO2,
					generalActionListener));
		}
		checkMenuItemVisibilities();
	}

	private void createDemoConnection(final String actionCommand) {
		ThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				if (actionCommand == ApplicationViewMenuBar.DEBUG_DEMO2) {
					final String title = "Connection "
							+ (Controller.getConnections().size() + 1);

					try {
						final DatabaseConnection connection = TestFixtures
								.createOfflineDatabaseModels02();

						connection.setTitle(title);
						Controller.updateConnection(connection);
					} catch (final IOException e) {
					}
				}
			}
		});
	}

	@Override
	public void update(final Observable o, final Object arg) {
		if ((arg != null) && (arg instanceof Notification)) {
			final Notification notification = (Notification) arg;

			// Switch between all possible notification event types.
			switch (notification) {
			case VIEW_CHANGED:
				// Switch between all possible view modes. This will check if
				// the mode is correctly displayed.
				if (ViewController.getViewMode().equals(ViewMode.CONNECTIONS)) {
					menuItemTabs.setEnabled(true);
				} else {
					menuItemTabs.setEnabled(false);
				}
			}
		}
	}

	public void addConnectionTab(final String title) {
		tabs.add(title);

		final JCheckBoxMenuItem item = new JCheckBoxMenuItem(title);
		item.addActionListener(tabItemActionListener);
		checkBoxGroup.add(item);
		menuItemTabs.add(item);

		tabItems.put(title, item);

		setCurrentConnectionTab(title);
		checkMenuItemVisibilities();
	}

	public void removeConnectionTab(final String title) {
		tabs.remove(title);

		final JCheckBoxMenuItem item = tabItems.get(title);
		checkBoxGroup.remove(item);
		menuItemTabs.remove(item);

		tabItems.remove(title);

		setNextConnectionTab();
		checkMenuItemVisibilities();
	}

	public void removeAllConnectionTabs() {
		tabs.clear();
		for (final JMenuItem item : tabItems.values()) {
			menuItemTabs.remove(item);
			checkBoxGroup.remove(item);
		}
		tabItems.clear();

		checkMenuItemVisibilities();
	}

	public void removeAllConnectionTabsExceptThis(final String title) {
		final List<String> removableItems = new ArrayList<String>();

		for (final String c : tabItems.keySet()) {
			if (!c.equals(title)) {
				removableItems.add(c);
			}
		}

		for (final String c : removableItems) {
			final JMenuItem item = tabItems.get(c);
			menuItemTabs.remove(item);
			tabs.remove(title);
			tabItems.remove(c);
			checkBoxGroup.remove(item);
		}

		checkMenuItemVisibilities();
	}

	public void setCurrentConnectionTab(final String title) {
		currentTabTitle = title;
		currentTabIndex = tabs.indexOf(title);
		tabItems.get(title).setSelected(true);

		checkMenuItemVisibilities();
	}

	public void setNextConnectionTab() {
		// Reset the current connection tab index; maximum is the last item
		// (size-1).
		if (!tabs.isEmpty()) {
			setCurrentConnectionTab(tabs.get(Math.min(tabs.size() - 1,
					currentTabIndex)));
		}
	}

	public void setPreviousConnectionTab() {
		setCurrentConnectionTab(tabs.get(Math.max(currentTabIndex - 1, 0)));
	}

	private void checkMenuItemVisibilities() {
		final int cntTabs = tabs.size();

		setEnabledMenuItem(TABS_PREVIOUS, (currentTabIndex > 0));
		setEnabledMenuItem(TABS_NEXT, (currentTabIndex < cntTabs - 1));
		setEnabledMenuItem(TABS_CLOSE, (currentTabIndex >= 0));
		setEnabledMenuItem(TABS_CLOSEALL, (currentTabIndex >= 0));
		setEnabledMenuItem(TABS_CLOSEOTHERS, (currentTabIndex >= 0));
	}

	private void setEnabledMenuItem(final String key, final boolean enable) {
		if (items.containsKey(key)) {
			items.get(key).setEnabled(enable);
		}
	}
}
