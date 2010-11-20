/**
 * $Id: Main.java 41 2009-03-01 16:21:20Z philipp $
 */
package de.unibonn.inf.dbdependenciesui.ui.controller;

import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

import javax.swing.JComponent;

import de.unibonn.inf.dbdependenciesui.Configuration;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseConnection;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseObject;
import de.unibonn.inf.dbdependenciesui.ui.ApplicationView;
import de.unibonn.inf.dbdependenciesui.ui.misc.WaitProgressWindow.WaitProgressTask;
import de.unibonn.inf.dbdependenciesui.ui.views.common.graph.AbstractGraphScene;

/**
 * This is the global view controller object. The ViewController delegates several general common tasks for all
 * ui/view-relevanted objects and events. The acutal implementation is stored in {@link ViewControllerImpl}.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 * @author Marc Kastleiner
 */
public class ViewController extends Observable {

	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(Configuration.LOGGER);

	private static ViewControllerImpl instance;

	/**
	 * Get the singleton instance of this object.
	 * 
	 * @return
	 */
	private synchronized static ViewControllerImpl getInstanceImpl() {
		if (ViewController.instance == null) {
			ViewController.instance = new ViewControllerImpl();
		}
		return ViewController.instance;
	}

	/**
	 * Private constructor (no construction outside of here).
	 */
	private ViewController() {

	}

	/**
	 * Register the application view object.
	 * 
	 * @param applicationView
	 */
	public static void setApplicationView(final ApplicationView applicationView) {
		getInstanceImpl().setApplicationView(applicationView);
	}

	/**
	 * Get the application view object.
	 * 
	 * @return
	 */
	public static ApplicationView getApplicationView() {
		return getInstanceImpl().getApplicationView();
	}

	/**
	 * Get the current view mode.
	 * 
	 * @return
	 */
	public static ViewMode getViewMode() {
		return getInstanceImpl().getViewMode();
	}

	/**
	 * Set a new view mode and notify all observers.
	 * 
	 * @param viewMode
	 */
	public static void setViewMode(final ViewMode viewMode) {
		getInstanceImpl().setViewMode(viewMode);
	}

	/**
	 * Set a new active database connection as name and notify all observers.
	 * 
	 * @param title
	 */
	public static void setDatabaseConnection(final String title) {
		getInstanceImpl().setDatabaseConnection(title);
	}

	/**
	 * Set a new active database connection as name and notify all observers.
	 * 
	 * @param id
	 */
	public static void setDatabaseConnection(final int id) {
		getInstanceImpl().setDatabaseConnection(id);
	}

	/**
	 * Get the current database connection as name.
	 * 
	 * @return
	 */
	public static int getDatabaseConnection() {
		return getInstanceImpl().getDatabaseConnection();
	}

	/**
	 * Load the current database connection.
	 * 
	 * @author Marc Kastleiner
	 * @return database connection
	 */
	public static DatabaseConnection loadDatabaseConnection() {
		return getInstanceImpl().loadDatabaseConnection();
	}

	/**
	 * Check if the current database connection is valid.
	 * 
	 * @return
	 */
	public static boolean isValidConnectionSelected() {
		return getInstanceImpl().isValidConnectionSelected();
	}

	/**
	 * Show the log view window frame.
	 */
	public static void showLogView() {
		getInstanceImpl().showLogView();
	}

	/**
	 * Show the help view window frame.
	 */
	public static void showHelpView() {
		getInstanceImpl().showHelpView();
	}

	public static void showWaitProgress(final WaitProgressTask task) {
		getInstanceImpl().showWaitProgress(task);
	}

	/**
	 * Show the license view window frame.
	 */
	public static void showLicenseView() {
		getInstanceImpl().showLicenseView();
	}

	/**
	 * Show the user settings view window frame.
	 */
	public static void showUserSettingsView() {
		getInstanceImpl().showUserSettingsView();
	}

	/**
	 * Show the license view window frame.
	 */
	public static void showAboutView() {
		getInstanceImpl().showAboutView();
	}

	/**
	 * Show a progress monitor while updating the database schema of the given connection.
	 * 
	 * @param connection
	 */
	public static void updateConnectionSchemaAndShowProgress(final DatabaseConnection connection) {
		getInstanceImpl().updateConnectionSchemaAndShowProgress(connection);
	}

	/**
	 * Add the given object to the observer list.
	 * 
	 * @param observer
	 */
	public static void addObserverObject(final Observer observer) {
		getInstanceImpl().addObserver(observer);
	}

	/**
	 * Export the current graphics object (see {@link JComponent#paint(java.awt.Graphics)}. This interactive action will
	 * display a file dialog specifying the file. The image type is png.
	 * 
	 * @param scene
	 */
	public static void exportGraphAsImage(final AbstractGraphScene scene) {
		getInstanceImpl().exportGraphAsImage(scene);
	}

	/**
	 * Export the current graph as a dot file used by GraphViz or tools that can read this file structure. This
	 * interactive action will display a file dialog specifying the file.
	 * 
	 * @param scene
	 */
	public static void exportGraphAsDot(final AbstractGraphScene scene) {
		getInstanceImpl().exportGraphAsDot(scene);
	}

	/**
	 * Change the viewmode (if necessary) and show the specified object (e.g. a table).
	 * 
	 * @param viewMode
	 * @param object
	 */
	public static void changeViewAndShowObject(final ViewMode viewMode, final DatabaseObject object) {
		getInstanceImpl().changeViewAndShowObject(viewMode, object);
	}

	public static void removeConnectionTab(final String title) {
		getInstanceImpl().removeConnectionTab(title);
	}

	public static void removeAllConnectionTabs() {
		getInstanceImpl().removeAllConnectionTabs();
	}

	public static void removeAllConnectionTabsExceptThis(final String title) {
		getInstanceImpl().removeAllConnectionTabsExceptThis(title);
	}

	public static void setCurrentConnectionTab(final String title) {
		getInstanceImpl().setCurrentConnectionTab(title);
	}

	/**
	 * Types of view modes.
	 */
	public static enum ViewMode {
		CONNECTIONS, HIERARCHY, TRIGGERS, ERD, PROCEDURES
	}

	/**
	 * Types of observe notifications
	 */
	public static enum Notification {
		VIEW_CHANGED, CONNECTION_SELECTED
	}
}
