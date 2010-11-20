/**
 * $Id: Main.java 41 2009-03-01 16:21:20Z philipp $
 */
package de.unibonn.inf.dbdependenciesui.ui.views.connections;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.javabuilders.BuildResult;
import org.javabuilders.swing.SwingJavaBuilder;

import de.unibonn.inf.dbdependenciesui.Configuration;
import de.unibonn.inf.dbdependenciesui.controller.Controller;
import de.unibonn.inf.dbdependenciesui.controller.ThreadExecutor;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseConnection;
import de.unibonn.inf.dbdependenciesui.metadata.IMetaData;
import de.unibonn.inf.dbdependenciesui.metadata.MetaDataFactory;
import de.unibonn.inf.dbdependenciesui.metadata.MetaDataFactory.Vendor;
import de.unibonn.inf.dbdependenciesui.misc.Internationalization;
import de.unibonn.inf.dbdependenciesui.ui.controller.ViewController;
import de.unibonn.inf.dbdependenciesui.ui.factory.ViewFactory;

/**
 * This is a dialog for adding or editing a connection. The dialog is constructed with the SwingJavaBuilder, a free
 * builder framework for swing elements. The actual layout for the components is defined in a yaml-file (see {@file
 *  ConnectionFormEditorDialog.yaml}
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class ConnectionFormEditorDialog extends JDialog {
	protected static final Logger log = Logger.getLogger(Configuration.LOGGER);

	protected static final long serialVersionUID = -482093218704480176L;

	protected final BuildResult result;

	protected DatabaseConnection entity;

	protected boolean editMode = false;
	protected boolean invalidTitle = false;

	protected JTextField title;
	protected JLabel statusIcon;
	protected JLabel statusText;
	protected JLabel feedbackText;
	protected JCheckBox updateSchema;
	protected JCheckBox checkSysdba;
	protected JComboBox vendors;

	protected JPasswordField password;

	protected boolean innerActionResult = false;

	public ConnectionFormEditorDialog() {
		this(0);
	}

	public ConnectionFormEditorDialog(final int connectionId) {
		super(ViewController.getApplicationView(), JDialog.ModalityType.DOCUMENT_MODAL);

		result = SwingJavaBuilder.build(this);
		postInitialize();

		editMode = (connectionId > 0);

		if (!editMode) {
			// Hide the checkbox because it is now irrelevant.
			updateSchema.setVisible(false);
			return;
		}

		// Load the entity and set all values.
		ThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				setEditableFields(false);
				entity = Controller.loadConnection(connectionId, true);
				log.info("Connection loaded: " + entity.getId() + ", " + entity.getTitle());

				setFields();
				setEditableFields(true);
			}
		});
	}

	protected void postInitialize() {
		// Find possible accelerators
		for (final String key : new String[] {
				"btnReset", "btnCancel", "btnTest", "btnSave" }) {
			final Object component = result.get(key);
			if (component instanceof JButton) {
				ViewFactory.initializeAccelerator((JButton) component);
			}
		}

		// Initialize the status fields
		if (statusIcon != null) {
			statusIcon.setIcon(Internationalization.getIcon("application.connection.status"));

			statusIcon.setVisible(false);
			statusText.setVisible(false);
			feedbackText.setVisible(false);
		}

		// Add alll known vendors
		for (final Vendor vendor : Vendor.values()) {
			vendors.addItem(vendor);
		}

		// Add a key event (pressing esc will close the window)
		ViewFactory.registerDisposeWindowOnEscape(this, getRootPane());

		// Add an event for title input checking if the title is unique. Otherwise the user will informed about a
		// conflict.
		title.addFocusListener(new FocusListener() {

			@Override
			public void focusGained(final FocusEvent e) {
				checkTitle();
			}

			@Override
			public void focusLost(final FocusEvent e) {
				checkTitle();
			}

			/**
			 * If adding a new connection, the connection's title should not exist.
			 */
			protected void checkTitle() {

				if (title.getText().isEmpty()) { return; }

				// Three possible states:
				// First: This is a edit mode. There is one other connection
				// with this title. The id is the same as the edit one.
				// Second: This is a edit mode. There is one other connection
				// with this title. The id is NOT the same as the edit one.
				// Third: This is not the edit mode. There is no other
				// connection with that title.

				final DatabaseConnection connection = Controller.loadConnection(title.getText(), false);

				if (editMode) {
					if ((connection == null) || ((connection != null) && (connection.getId() == entity.getId()))) {
						invalidTitle = false;
						statusText.setVisible(false);
					} else {
						invalidTitle = true;
						statusText.setText(Internationalization.getText("application.connection.status.invalidTitle"));
						statusText.setVisible(true);
					}
				} else {
					if (connection == null) {
						invalidTitle = false;
						statusText.setVisible(false);
					} else {
						invalidTitle = true;
						statusText.setText(Internationalization.getText("application.connection.status.invalidTitle"));
						statusText.setVisible(true);
					}
				}
			}
		});
	}

	/**
	 * Set editable of all fields.
	 */
	protected void setEditableFields(final boolean editable) {
		for (final String key : new String[] {
				"title", "host", "port", "username", "password", "database", "schema" }) {
			final Object component = result.get(key);
			if (component instanceof JTextField) {
				((JTextField) component).setEditable(editable);
			}
		}
		vendors.setEditable(editable);
	}

	/**
	 * Clear all fields.
	 */
	protected void clearFields() {
		for (final String key : new String[] {
				"title", "host", "port", "username", "password", "database", "schema" }) {
			set(key, "");
		}

		vendors.setSelectedIndex(0);
	}

	/**
	 * Read the connection entity's values.
	 */
	protected void setFields() {
		set("title", entity.getTitle());
		set("host", entity.getHost());
		set("port", entity.getPort() + "");
		set("username", entity.getUsername());
		set("password", entity.getPassword());
		set("database", entity.getDatabase());
		set("schema", entity.getSchema());

		checkSysdba.setSelected(entity.getSysdba());
		vendors.setSelectedItem(entity.getVendor());
	}

	protected void disableButtons() {
		for (final String key : new String[] {
				"btnReset", "btnCancel", "btnTest", "btnSave" }) {
			final Object component = result.get(key);
			if (component instanceof JButton) {
				((JButton) component).setEnabled(false);
			}
		}
	}

	protected void enableButtons() {
		for (final String key : new String[] {
				"btnReset", "btnCancel", "btnTest", "btnSave" }) {
			final Object component = result.get(key);
			if (component instanceof JButton) {
				((JButton) component).setEnabled(true);
			}
		}
	}

	protected void resetDialog() {
		clearFields();
	}

	protected boolean testConnection() {
		return this.testConnection(false);
	}

	/**
	 * In a new thread, try to connect to the database connection configured by the input fields. If wait is set true
	 * the main thread must wait for the test thread.
	 * 
	 * @param wait
	 * @return
	 */
	protected boolean testConnection(final boolean wait) {
		innerActionResult = false;

		final Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				disableButtons();
				statusIcon.setVisible(true);
				statusText.setText(Internationalization.getText("application.connection.status"));
				statusText.setVisible(true);
				feedbackText.setVisible(true);

				// User/Password/URL for database connection
				final String username = get("username");
				final String password = get("password");
				final String host = get("host");
				final int port = Integer.parseInt(get("port"));
				final String database = get("database");
				final String schema = get("schema");
				final boolean isSysdba = checkSysdba.isSelected();

				final IMetaData metaObject = MetaDataFactory.create((Vendor) (vendors.getSelectedItem()));
				boolean success = false;
				try {
					final DatabaseConnection connection = new DatabaseConnection();
					connection.setHost(host);
					connection.setPort(port);
					connection.setUsername(username);
					connection.setPassword(password);
					connection.setSchema(schema);
					connection.setDatabase(database);
					connection.setSysdba(isSysdba);
					connection.setVendor(metaObject.getVendor());
					metaObject.setDatabaseConnection(connection);
					success = metaObject.checkConnect();
				} catch (final Exception e) {
					feedbackText.setText("<html>" + e.getLocalizedMessage() + "</html>");
				}
				if (success) {
					statusText.setText(Internationalization.getText("application.connection.status.success"));
					feedbackText.setVisible(false);
					innerActionResult = true;
				} else {
					statusText.setText(Internationalization.getText("application.connection.status.failure"));
					innerActionResult = false;
				}
				statusIcon.setVisible(false);
				enableButtons();
			}

		});

		thread.start();
		if (wait) {
			try {
				thread.join();
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}

		return innerActionResult;
	}

	protected void saveConnection() {
		innerActionResult = false;

		// Do not save if an invalid title.
		if (invalidTitle) { return; }

		ThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				if (!testConnection(true)) { return; }

				disableButtons();
				statusText.setVisible(true);
				statusText.setText(Internationalization.getSetting("application.connection.status.saving"));

				if (entity == null) {
					entity = Controller.createNewConnection((Vendor) vendors.getSelectedItem(), get("title"),
							ConnectionFormEditorDialog.this.get("host"), get("port"), get("username"), get("password"),
							get("database"), get("schema"), checkSysdba.isSelected());

					ViewController.updateConnectionSchemaAndShowProgress(entity);

				} else {
					entity.setVendor((Vendor) vendors.getSelectedItem());
					entity.setTitle(get("title"));
					entity.setHost(get("host"));
					entity.setPort(Integer.parseInt(get("port")));
					entity.setUsername(get("username"));
					entity.setPassword(get("password"));
					entity.setDatabase(get("database"));
					entity.setSchema(get("schema"));
					entity.setSysdba(checkSysdba.isSelected());
					Controller.updateConnection(entity);

					// At this place, we could perform a refresh. Delete all
					// tables, views and triggers and rebuild structure.
					if (updateSchema.isSelected()) {
						ViewController.updateConnectionSchemaAndShowProgress(entity);
					}
				}

				statusText.setVisible(false);
				enableButtons();

				closeDialog();
			}
		});
	}

	/**
	 * Try to resolve the corresponding swing text field object (created by swing builder) and redefine the text value.
	 * 
	 * @param key
	 * @return
	 */
	protected String set(final String key, final String value) {
		final Object component = result.get(key);
		if (component instanceof JTextField) {
			((JTextField) component).setText(value);
		}
		return null;
	}

	/**
	 * Try to resolve the corresponding swing text field object (created by swing builder) and return the text value.
	 * 
	 * @param key
	 * @return
	 */
	protected String get(final String key) {
		final Object component = result.get(key);
		if (component instanceof JTextField) { return ((JTextField) component).getText(); }
		return null;
	}

	protected void closeDialog() {
		dispose();
	}

	protected void cancelDialog() {
		dispose();
	}
}
