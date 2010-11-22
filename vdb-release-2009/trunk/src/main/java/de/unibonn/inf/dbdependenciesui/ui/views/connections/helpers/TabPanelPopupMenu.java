package de.unibonn.inf.dbdependenciesui.ui.views.connections.helpers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import de.unibonn.inf.dbdependenciesui.ui.controller.ViewController;
import de.unibonn.inf.dbdependenciesui.ui.factory.ViewFactory;

public class TabPanelPopupMenu extends JPopupMenu {
	private static final long serialVersionUID = -3217758540958765413L;

	private static final String TABS = "tabs";
	private static final String TABS_PREVIOUS = "tabs.previous";
	private static final String TABS_NEXT = "tabs.next";
	private static final String TABS_CLOSE = "tabs.close";
	private static final String TABS_CLOSEALL = "tabs.closeall";
	private static final String TABS_CLOSEOTHERS = "tabs.closeothers";

	private String tabTitle;

	public TabPanelPopupMenu() {
		initialize();
	}

	private void initialize() {
		final ActionListener actionListener = new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final String actionCommand = e.getActionCommand();

				if ((actionCommand == null) || actionCommand.isEmpty()) { return; }

				if (actionCommand == TABS_CLOSE) {
					ViewController.removeConnectionTab(tabTitle);
				} else if (actionCommand == TABS_CLOSEALL) {
					ViewController.removeAllConnectionTabs();
				} else if (actionCommand == TABS_CLOSEOTHERS) {
					ViewController.removeAllConnectionTabsExceptThis(tabTitle);
				}
			}
		};

		JMenuItem item;
		item = ViewFactory.createMenuItem(TABS_CLOSE, actionListener);
		add(item);
		item = ViewFactory.createMenuItem(TABS_CLOSEALL, actionListener);
		add(item);
		item = ViewFactory.createMenuItem(TABS_CLOSEOTHERS, actionListener);
		add(item);
	}

	public void setTabTitle(final String tabTitle) {
		this.tabTitle = tabTitle;
	}

}
