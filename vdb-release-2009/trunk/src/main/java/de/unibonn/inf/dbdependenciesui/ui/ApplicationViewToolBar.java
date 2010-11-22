package de.unibonn.inf.dbdependenciesui.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import com.explodingpixels.macwidgets.LabeledComponentGroup;
import com.explodingpixels.macwidgets.MacButtonFactory;
import com.explodingpixels.macwidgets.UnifiedToolBar;

import de.unibonn.inf.dbdependenciesui.controller.Controller;
import de.unibonn.inf.dbdependenciesui.controller.ThreadExecutor;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseConnection;
import de.unibonn.inf.dbdependenciesui.metadata.MetaDataFactory.Vendor;
import de.unibonn.inf.dbdependenciesui.misc.Internationalization;
import de.unibonn.inf.dbdependenciesui.ui.controller.ViewController;
import de.unibonn.inf.dbdependenciesui.ui.controller.ViewController.Notification;
import de.unibonn.inf.dbdependenciesui.ui.controller.ViewController.ViewMode;
import de.unibonn.inf.dbdependenciesui.ui.factory.ViewFactory;
import de.unibonn.inf.dbdependenciesui.ui.helpers.SystemTools;

/**
 * This is the application's toolbar. The toolbar contains important controls and commands like view changes.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 * @author Marc Kastleiner
 */
public class ApplicationViewToolBar extends JToolBar implements Observer {

	private static final long serialVersionUID = -7939947088387373956L;

	protected JButton btnNewConnection;
	protected JButton btnShowLog;
	protected JButton btnShowHelp;

	protected ButtonGroup viewsGroup;
	protected JToggleButton btnViewConnections;
	protected JToggleButton btnViewHierarchy;
	protected JToggleButton btnViewTriggers;
	protected JToggleButton btnViewErd;
	protected JToggleButton btnViewProc;

	protected final String keyPrefix = "application.toolbar.";

	public ApplicationViewToolBar() {

		if (SystemTools.isMac()) {
			initializeButtons(16, 32);
			initializeForMac();
		} else {
			initializeButtons(32, 32);
			initialize();
		}

		ViewController.addObserverObject(this);
	}

	/**
	 * Create all buttons.
	 * 
	 * @param iconSizeViews
	 *            the size of the left buttons changing the view.
	 * @param iconSizeNormal
	 *            the size of all other buttons.
	 */
	protected void initializeButtons(final int iconSizeViews, final int iconSizeNormal) {
		btnNewConnection = createButton("newconnection", "newconnection", iconSizeNormal);
		btnViewConnections = createToggleButton("connections", ViewMode.CONNECTIONS, iconSizeViews);
		btnViewHierarchy = createToggleButton("hierarchy", ViewMode.HIERARCHY, iconSizeViews);
		btnViewTriggers = createToggleButton("triggers", ViewMode.TRIGGERS, iconSizeViews);
		btnViewErd = createToggleButton("erd", ViewMode.ERD, iconSizeViews);
		btnViewProc = createToggleButton("procedures", ViewMode.PROCEDURES, iconSizeViews);
		btnShowLog = createButton("log", "showlog", iconSizeNormal);
		btnShowHelp = createButton("help", "showhelp", iconSizeNormal);

		btnViewConnections.setSelected(true);
		btnViewHierarchy.setEnabled(false);
		btnViewTriggers.setEnabled(false);
		btnViewErd.setEnabled(false);
		btnViewProc.setEnabled(false);

		btnNewConnection.setFocusable(false);
		btnViewConnections.setFocusable(false);
		btnViewHierarchy.setFocusable(false);
		btnViewTriggers.setFocusable(false);
		btnViewErd.setFocusable(false);
		btnViewProc.setFocusable(false);
		btnShowLog.setFocusable(false);
		btnShowHelp.setFocusable(false);

		viewsGroup = new ButtonGroup();
		viewsGroup.add(btnViewConnections);
		viewsGroup.add(btnViewHierarchy);
		viewsGroup.add(btnViewTriggers);
		viewsGroup.add(btnViewErd);
		viewsGroup.add(btnViewProc);

		final ActionListener al = new MyActionListener();

		btnNewConnection.addActionListener(al);
		btnViewConnections.addActionListener(al);
		btnViewHierarchy.addActionListener(al);
		btnViewTriggers.addActionListener(al);
		btnViewErd.addActionListener(al);
		btnViewProc.addActionListener(al);
		btnShowLog.addActionListener(al);
		btnShowHelp.addActionListener(al);
	}

	/**
	 * Create and return a new toggle button with the given {@link Internationalization}'s key, with the given
	 * {@link ViewMode} as actionCommand and the with given icon size.
	 * 
	 * @param key
	 * @param viewMode
	 * @param iconScale
	 * @return
	 */
	protected JToggleButton createToggleButton(final String key, final ViewMode viewMode, final int iconScale) {
		final JToggleButton button = new JToggleButton(Internationalization.getScaledIcon(keyPrefix + key, iconScale));
		button.setToolTipText(Internationalization.getText(keyPrefix + key + ".tooltip"));
		button.setActionCommand(viewMode.toString());
		return button;
	}

	/**
	 * Create and return a new button with the given {@link Internationalization}'s key, with the given {@link ViewMode}
	 * as actionCommand and the with given icon size.
	 * 
	 * @param key
	 * @param actionCommand
	 * @param iconScale
	 * @return
	 */
	protected JButton createButton(final String key, final String actionCommand, final int iconScale) {
		final JButton button = new JButton(Internationalization.getScaledIcon(keyPrefix + key, iconScale));
		button.setToolTipText(Internationalization.getText(keyPrefix + key + ".tooltip"));
		button.setActionCommand(actionCommand);
		button.setRolloverEnabled(true);
		return button;
	}

	/**
	 * Default initializer. This creates a standard swing {@link JToolBar}.
	 */
	protected void initialize() {
		add(btnNewConnection);
		addSeparator();
		add(btnViewConnections);
		add(btnViewHierarchy);
		add(btnViewTriggers);
		add(btnViewErd);
		add(btnViewProc);
		addSeparator();
		add(btnShowLog);
		add(btnShowHelp);

		setFloatable(false);
		setRollover(true);
	}

	/**
	 * Special initializer for mac os x systems. In fact, this creates an unified toolbar component wrapped by this
	 * {@link JToolBar}. In order to assure correct painting, this override the standard {@link #updateUI()} behavior.
	 */
	protected void initializeForMac() {
		// Create toolbar wrapper class.
		final UnifiedToolBar toolBar = new UnifiedToolBar();
		// Install draggable toolbar (like frame bar).
		toolBar.installWindowDraggerOnWindow(ViewController.getApplicationView());

		btnNewConnection.setText(Internationalization.getText(keyPrefix + "newconnection.tooltip"));
		toolBar.addComponentToLeft(MacButtonFactory.makeUnifiedToolBarButton(btnNewConnection));

		btnViewConnections.putClientProperty("JButton.buttonType", "segmentedTextured");
		btnViewConnections.putClientProperty("JButton.segmentPosition", "first");
		btnViewHierarchy.putClientProperty("JButton.buttonType", "segmentedTextured");
		btnViewHierarchy.putClientProperty("JButton.segmentPosition", "middle");
		btnViewTriggers.putClientProperty("JButton.buttonType", "segmentedTextured");
		btnViewTriggers.putClientProperty("JButton.segmentPosition", "middle");
		btnViewErd.putClientProperty("JButton.buttonType", "segmentedTextured");
		btnViewErd.putClientProperty("JButton.segmentPosition", "middle");
		btnViewProc.putClientProperty("JButton.buttonType", "segmentedTextured");
		btnViewProc.putClientProperty("JButton.segmentPosition", "last");

		final String title = Internationalization.getText(keyPrefix + "titles.views");
		final LabeledComponentGroup labeledComponentGroup = new LabeledComponentGroup(title, getGroupButtons());
		toolBar.addComponentToLeft(labeledComponentGroup.getComponent());

		btnShowLog.setText(Internationalization.getText(keyPrefix + "log.tooltip"));
		toolBar.addComponentToRight(MacButtonFactory.makeUnifiedToolBarButton(btnShowLog));

		btnShowHelp.setIcon(null);
		btnShowHelp.putClientProperty("JButton.buttonType", "help");
		btnShowHelp.setText(null);
		toolBar.addComponentToRight(btnShowHelp);

		setLayout(new BorderLayout());
		this.add(toolBar.getComponent(), BorderLayout.CENTER);
	}

	protected List<JComponent> getGroupButtons() {
		final List<JComponent> buttons = new ArrayList<JComponent>();

		for (final AbstractButton button : Collections.list(viewsGroup.getElements())) {
			buttons.add(button);
		}

		return buttons;
	}

	/**
	 * Overriden method for mac os x compability.
	 */
	@Override
	public void updateUI() {
		// If on mac osx, the toolbar painting is replaced.
		if (!SystemTools.isMac()) {
			super.updateUI();
		} else {}
	}

	@Override
	public void update(final Observable o, final Object arg) {
		if ((arg != null) && (arg instanceof Notification)) {
			final Notification notification = (Notification) arg;

			// Switch between all possible notification event types.
			switch (notification) {
			case CONNECTION_SELECTED:
				// This will check if the view buttons are selectable or not.
				if (ViewController.isValidConnectionSelected()) {
					btnViewHierarchy.setEnabled(true);
					btnViewTriggers.setEnabled(true);
					btnViewErd.setEnabled(true);

					final DatabaseConnection currentConnection = Controller.loadConnection(ViewController
							.getDatabaseConnection(), false);
					final Vendor vendor = currentConnection.getVendor();
					if ((vendor == Vendor.ORACLE) || (vendor == Vendor.ORACLE10)) {
						btnViewProc.setEnabled(true);
					} else {
						btnViewProc.setEnabled(false);
					}

				} else {
					btnViewHierarchy.setEnabled(false);
					btnViewTriggers.setEnabled(false);
					btnViewErd.setEnabled(false);
					btnViewProc.setEnabled(false);
				}
				break;
			case VIEW_CHANGED:
				// Switch between all possible view modes. This will check if the mode is correctly displayed.
				switch (ViewController.getViewMode()) {
				case CONNECTIONS:
					if (!btnViewConnections.isSelected()) {
						btnViewConnections.setSelected(true);
					}
					break;
				case HIERARCHY:
					if (!btnViewHierarchy.isSelected()) {
						btnViewHierarchy.setSelected(true);
					}
					break;
				case TRIGGERS:
					if (!btnViewTriggers.isSelected()) {
						btnViewTriggers.setSelected(true);
					}
					break;
				case ERD:
					if (!btnViewErd.isSelected()) {
						btnViewErd.setSelected(true);
					}
					break;
				case PROCEDURES:
					if (!btnViewProc.isSelected()) {
						btnViewProc.setSelected(true);
					}
				}
			}
		}
	}

	/**
	 * Implementation of an actionlistener for execute the commands.
	 */
	private static class MyActionListener implements ActionListener {

		@Override
		public void actionPerformed(final ActionEvent e) {
			ThreadExecutor.execute(new Runnable() {
				@Override
				public void run() {
					try {
						// Try to determine which viewmode was selected. If an exception
						// occurred, it was a normal string.
						final ViewMode selectedMode = ViewMode.valueOf(e.getActionCommand());

						ViewController.setViewMode(selectedMode);

					} catch (final IllegalArgumentException ex) {
						if ("showlog".equals(e.getActionCommand())) {
							ViewController.showLogView();
						} else if ("showhelp".equals(e.getActionCommand())) {
							ViewController.showHelpView();
						} else if ("newconnection".equals(e.getActionCommand())) {
							ViewFactory.openNewConnectionDialog();
						}
					}
				}

			});

		}

	}
}
