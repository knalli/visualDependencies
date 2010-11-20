/**
 * $Id: Main.java 41 2009-03-01 16:21:20Z philipp $
 */
package de.unibonn.inf.dbdependenciesui.ui.controller;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.ProgressMonitor;
import javax.transaction.NotSupportedException;

import de.unibonn.inf.dbdependenciesui.Configuration;
import de.unibonn.inf.dbdependenciesui.controller.Controller;
import de.unibonn.inf.dbdependenciesui.graph.common.AbstractDatabaseGraph;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseConnection;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseObject;
import de.unibonn.inf.dbdependenciesui.metadata.IMetaData;
import de.unibonn.inf.dbdependenciesui.metadata.MetaDataFactory;
import de.unibonn.inf.dbdependenciesui.metadata.progresstask.ProgressMonitorPropertyChangeListener;
import de.unibonn.inf.dbdependenciesui.metadata.progresstask.Task;
import de.unibonn.inf.dbdependenciesui.misc.Internationalization;
import de.unibonn.inf.dbdependenciesui.ui.AboutView;
import de.unibonn.inf.dbdependenciesui.ui.ApplicationView;
import de.unibonn.inf.dbdependenciesui.ui.HelpView;
import de.unibonn.inf.dbdependenciesui.ui.LicensesView;
import de.unibonn.inf.dbdependenciesui.ui.LogView;
import de.unibonn.inf.dbdependenciesui.ui.controller.ViewController.Notification;
import de.unibonn.inf.dbdependenciesui.ui.controller.ViewController.ViewMode;
import de.unibonn.inf.dbdependenciesui.ui.factory.ViewFactory;
import de.unibonn.inf.dbdependenciesui.ui.helpers.FileDialog;
import de.unibonn.inf.dbdependenciesui.ui.misc.WaitProgressWindow.WaitProgressTask;
import de.unibonn.inf.dbdependenciesui.ui.views.common.graph.AbstractGraphScene;

/**
 * The ViewController is a central delegation component.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class ViewControllerImpl extends Observable {

	private static final Logger log = Logger.getLogger(Configuration.LOGGER);

	private ApplicationView applicationView;

	private ViewMode viewMode = ViewMode.CONNECTIONS;

	private int currentConnectionId = 0;

	/**
	 * Register the application view object.
	 * 
	 * @param applicationView
	 */
	public void setApplicationView(final ApplicationView applicationView) {
		this.applicationView = applicationView;
	}

	/**
	 * Get the application view object.
	 * 
	 * @return
	 */
	public ApplicationView getApplicationView() {
		return applicationView;
	}

	/**
	 * Get the current view mode.
	 * 
	 * @return
	 */
	public ViewMode getViewMode() {
		return viewMode;
	}

	/**
	 * Set a new view mode and notify all observers.
	 * 
	 * @param viewMode
	 */
	public void setViewMode(final ViewMode viewMode) {
		if (!this.viewMode.equals(viewMode)) {
			this.viewMode = viewMode;

			ViewControllerImpl.log.info("ViewMode has changed.");
			setChanged();
			this.notifyObservers(Notification.VIEW_CHANGED);
		}
	}

	/**
	 * Set a new active database connection as name and notify all observers.
	 * 
	 * @param title
	 */
	public void setDatabaseConnection(final String title) {
		final DatabaseConnection connection = Controller.loadConnection(title, false);
		if (connection != null) {
			this.setDatabaseConnection(connection.getId());
		} else {
			this.setDatabaseConnection(0);
		}
	}

	/**
	 * Set a new active database connection as name and notify all observers.
	 * 
	 * @param id
	 */
	public void setDatabaseConnection(final int id) {
		if (((id == 0) && (id != currentConnectionId)) || ((id != 0) && (id != currentConnectionId))) {
			currentConnectionId = id;

			ViewControllerImpl.log.info("DatabaseConnection has changed.");
			setChanged();
			this.notifyObservers(Notification.CONNECTION_SELECTED);
		}
	}

	/**
	 * Get the current database connection as name.
	 * 
	 * @return
	 */
	public int getDatabaseConnection() {
		return currentConnectionId;
	}

	/**
	 * Load the current database connection.
	 * 
	 * @author Marc Kastleiner
	 * @return database connection
	 */
	public DatabaseConnection loadDatabaseConnection() {
		return Controller.loadConnection(currentConnectionId, false);
	}

	/**
	 * Check if the current database connection is valid.
	 * 
	 * @return
	 */
	public boolean isValidConnectionSelected() {
		return currentConnectionId != 0;
	}

	/**
	 * Show the log view window frame.
	 */
	public void showLogView() {
		LogView.getInstance().setVisible(true);
	}

	/**
	 * Show the log view window frame.
	 */
	public void showHelpView() {
		HelpView.getInstance().setVisible(true);
	}

	public void showWaitProgress(final WaitProgressTask task) {
		task.getFrame().execute();
	}

	/**
	 * Show the license view window frame.
	 */
	public void showLicenseView() {
		LicensesView.getInstance().setVisible(true);
	}

	/**
	 * Show the user settings view window frame.
	 */
	public void showUserSettingsView() {
		final String message = Internationalization.getTextFormatted("application.usersettings.text",
				Configuration.SETTINGS_PATH, Configuration.INTERNAL_NAME);
		ViewFactory.showMessageDialog(getApplicationView(), message);
	}

	/**
	 * Show the license view window frame.
	 */
	public void showAboutView() {
		AboutView.getInstance().setVisible(true);
	}

	/**
	 * Show a progress monitor while updating the database schema of the given connection.
	 * 
	 * @param connection
	 */
	public void updateConnectionSchemaAndShowProgress(final DatabaseConnection connection) {
		try {

			if (connection == null) { throw new NotSupportedException("Connection was null."); }

			// Remove old properties.
			connection.setProperties("");

			// Get a metadata object.
			final IMetaData metaData = MetaDataFactory.create(connection.getVendor());

			// Set database connection details.
			metaData.setDatabaseConnection(connection);
			metaData.setReplaceMode(true);

			// Get a task object.
			final Task task = MetaDataFactory.createTask(metaData, connection);

			// Create a progress monitor object (swing).
			final String title = Internationalization.getText(task.getGlobalKey() + "title");
			final ProgressMonitor progressMonitor = new ProgressMonitor(ViewControllerImpl.this.getApplicationView(),
					title, "", 0, 100);
			task.addPropertyChangeListener(new ProgressMonitorPropertyChangeListener(progressMonitor, task));
			progressMonitor.setMillisToPopup(0);
			progressMonitor.setMillisToDecideToPopup(0);

			task.execute();
		} catch (final Exception e) {
			e.printStackTrace();
			ViewControllerImpl.log.warning(e.getLocalizedMessage());
		}
	}

	public void exportGraphAsImage(final AbstractGraphScene scene) {
		final String title = Internationalization.getText("application.graph.sidebar.controls.export.imagepng.dialog");
		final FileDialog fd = new FileDialog(ViewController.getApplicationView(), title, FileDialog.Mode.SAVE, "*.png",
				"PNG");
		final File file = fd.showSaveFileDialog();

		if (file == null) { return; }

		/**
		 * {@link http://jung.sourceforge.net/doc/JUNGVisualizationGuide.html}
		 */

		// use double buffering until now?
		final boolean isDoubleBuffered = scene.getMainView().isDoubleBuffered();

		// turn it off to capture
		scene.getMainView().setDoubleBuffered(false);

		try {
			// Capture: create a BufferedImage
			final int width = scene.getMainView().getWidth();
			final int height = scene.getMainView().getHeight();
			// create the Graphics2D object that paints to it
			final BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			// create a graphics2d (nor graphics) object, because the graph uses this newer version
			final Graphics2D g = bi.createGraphics();

			// use paintAll; if using paint or paintComponent, the background or other elements will missed
			scene.getMainView().printAll(g);

			// and save out the BufferedImage
			ImageIO.write(bi, "png", file);
		} catch (final IOException e) {
			log.log(Level.WARNING, e.getLocalizedMessage());
		} finally {
			// turn double buffering back on
			scene.getMainView().setDoubleBuffered(isDoubleBuffered);
		}
	}

	public void exportGraphAsDot(final AbstractGraphScene scene) {
		final String title = Internationalization.getText("application.graph.sidebar.controls.export.graphdot.dialog");
		final FileDialog fd = new FileDialog(ViewController.getApplicationView(), title, FileDialog.Mode.SAVE, "*.dot",
				"GraphViz Dot");
		final File file = fd.showSaveFileDialog();

		if (file == null) { return; }

		final AbstractDatabaseGraph graph = scene.getGraph();
		try {
			Controller.exportGraphAsDotFile(file, graph);
		} catch (final IOException e) {
			log.log(Level.WARNING, e.getLocalizedMessage());
		}
	}

	public void changeViewAndShowObject(final ViewMode viewMode, final DatabaseObject object) {
		setViewMode(viewMode);

		// If the object is not a table, view or trigger nor the mode is connection.. just ignore it.
		switch (viewMode) {
		case CONNECTIONS:
			setChanged();
			notifyObservers(object);
		}
	}

	public void removeConnectionTab(final String title) {
		getApplicationView().removeConnectionTab(title);
	}

	public void removeAllConnectionTabs() {
		getApplicationView().removeAllConnectionTabs();
	}

	public void removeAllConnectionTabsExceptThis(final String title) {
		getApplicationView().removeAllConnectionTabsExceptThis(title);
	}

	public void setCurrentConnectionTab(final String title) {
		getApplicationView().setCurrentConnectionTab(title);
	}
}
