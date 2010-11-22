/**
 * $Id: Main.java 41 2009-03-01 16:21:20Z philipp $
 */
package de.unibonn.inf.dbdependenciesui.ui;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.LogRecord;

import javax.swing.table.AbstractTableModel;

import de.unibonn.inf.dbdependenciesui.Configuration;
import de.unibonn.inf.dbdependenciesui.misc.Internationalization;

/**
 * The log table data model.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class LogTableModel extends AbstractTableModel {
	private static final long serialVersionUID = -393838935151720190L;
	private final List<String> columnNames;
	private final DateFormat dateFormatter;

	private final List<LogRecord> records = new ArrayList<LogRecord>();

	public LogTableModel() {
		this(Internationalization.getText("application.log.table.time"),
				Internationalization.getText("application.log.table.level"),
				Internationalization.getText("application.log.table.message"),
				Internationalization.getText("application.log.table.source"));
	}

	public LogTableModel(final String... names) {
		this.columnNames = Arrays.asList(names);
		this.dateFormatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
				DateFormat.MEDIUM, Configuration.LOCALE);
	}

	/**
	 * Add a new log record.
	 * 
	 * @param record
	 */
	public void addLogRecord(final LogRecord record) {
		this.records.add(record);
		this.fireTableRowsInserted(this.getRowCount() - 1,
				this.getRowCount() - 1);
	}

	/**
	 * Return the log record by the given index
	 * 
	 * @param index
	 * @return
	 */
	public LogRecord getLogRecord(final int index) {
		return this.records.get(index);
	}

	@Override
	public String getColumnName(final int column) {
		Object id = null;
		// This test is to cover the case when
		// getColumnCount has been subclassed by mistake ...
		if (column < this.columnNames.size()) {
			id = this.columnNames.get(column);
		}
		return (id == null) ? super.getColumnName(column) : id.toString();
	}

	@Override
	public int getColumnCount() {
		return this.columnNames.size();
	}

	@Override
	public int getRowCount() {
		return this.records.size();
	}

	@Override
	public Object getValueAt(final int rowIndex, final int columnIndex) {
		try {
			final LogRecord record = this.records.get(rowIndex);
			switch (columnIndex) {
			case 0:
				return this.dateFormatter.format(new Date(record.getMillis()));
			case 1:
				return record.getLevel().toString();
			case 2:
				return record.getMessage();
			case 3:
				return record.getSourceMethodName();
			}
		} catch (final Exception e) {
		}
		return null;
	}

	@Override
	public void setValueAt(final Object value, final int rowIndex,
			final int columnIndex) {
	}

	@Override
	public boolean isCellEditable(final int rowIndex, final int columnIndex) {
		return false;
	}
}
