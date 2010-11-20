/**
 * $Id: Main.java 41 2009-03-01 16:21:20Z philipp $
 */
package de.unibonn.inf.dbdependenciesui.ui;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;

import de.unibonn.inf.dbdependenciesui.Configuration;
import de.unibonn.inf.dbdependenciesui.misc.Internationalization;
import de.unibonn.inf.dbdependenciesui.ui.factory.ViewFactory;

/**
 * The overall standard log view window frame. This log table shows all log records produces by the application. It
 * lists all these records which are defined (see LogLevel). Each row gets a doubleclick event providing a detail
 * information dialog.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class LogView extends JFrame {
	private static final long serialVersionUID = 3931005500785784753L;
	private static LogView instance;
	private LogTableModel tableModel;
	private JTable jTable;
	private JScrollPane scrollpane;

	public static LogView getInstance() {
		if (instance == null) {
			instance = new LogView();
		}
		return instance;
	}

	public LogView() {
		// Hide while loading and set the position to the center of the screen.
		setVisible(false);
		initialize();
		setLocationRelativeTo(null);
	}

	private void initialize() {
		this.setSize(new Dimension(900, 600));

		// Create a LogTableModel and add a new logger handler which delegate
		// the incoming new record to the model.
		tableModel = new LogTableModel();
		Logger.getLogger(Configuration.LOGGER).addHandler(new Handler() {
			@Override
			public void close() throws SecurityException {}

			@Override
			public void flush() {}

			@Override
			public void publish(final LogRecord record) {
				tableModel.addLogRecord(record);
			}
		});

		setTitle(Internationalization.getText("application.log.title"));

		jTable = ViewFactory.createTable(tableModel);
		scrollpane = ViewFactory.createScrollableTable(jTable);
		this.add(scrollpane);

		// Create the double click event for information dialog.
		jTable.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(final MouseEvent e) {
				// Check if this is a double click.
				if ((e.getClickCount() == 2) && !e.isConsumed()) {
					e.consume();
					final int row = jTable.getSelectedRow();

					// Show standard message dialog.
					ViewFactory.showMessageDialog(LogView.this, tableModel.getLogRecord(row).getMessage());
				}
			}
		});

		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
	}
}
