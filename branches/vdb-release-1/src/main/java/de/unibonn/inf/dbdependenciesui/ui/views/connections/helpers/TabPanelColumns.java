package de.unibonn.inf.dbdependenciesui.ui.views.connections.helpers;

import java.awt.BorderLayout;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.JTable;

import de.unibonn.inf.dbdependenciesui.Configuration;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.Column;
import de.unibonn.inf.dbdependenciesui.misc.Internationalization;
import de.unibonn.inf.dbdependenciesui.ui.factory.ViewFactory;

public class TabPanelColumns extends JPanel {

	private static final Logger log = Logger.getLogger(Configuration.LOGGER);

	private static final long serialVersionUID = 381489754534833764L;

	private final Object[][] data;

	private final String[] columnsHeader = {
			Internationalization.getText("application.connections.table.header.name"),
			Internationalization.getText("application.connections.table.header.type"),
			Internationalization.getText("application.connections.table.header.nullable"),
			Internationalization.getText("application.connections.table.header.primary") };

	private final List<Column> columns;

	private List<String> primarys;

	// private JScrollPane scrollPane;

	public TabPanelColumns(final DatabaseTable object) {
		super(new BorderLayout());
		columns = object.getDdlSchemaObject().getColumns();
		if (!object.getDdlSchemaObject().getPrimaryKeys().isEmpty()) {
			primarys = object.getDdlSchemaObject().getPrimaryKeys().get(0).getColumns();
			if (TabPanelColumns.log.getLevel() == Level.INFO) {
				for (int i = 0; i < primarys.size(); i++) {
					TabPanelColumns.log.info(primarys.get(i));
				}
			}
		}
		data = new Object[columns.size()][5];

		init();
	}

	private void init() {
		for (int i = 0; i < columns.size(); i++) {
			data[i][0] = columns.get(i).getName();
			data[i][1] = createType(columns.get(i));
			data[i][2] = columns.get(i).isNullable();
			if (primarys != null) {
				data[i][3] = primarys.contains(columns.get(i).getName());
			} else {
				data[i][3] = false;
			}
		}

		initializeTable();
	}

	/**
	 * Create and return the technique represention string of the type/length information about the given column.
	 * Example: VARCHAR(10) or DECIMAL (10,2) or DATE or TEXT or BLOB (3000)
	 * 
	 * @param column
	 * @return
	 */
	private String createType(final Column column) {
		String result = column.getType();
		final int size = column.getSize();
		final int digits = column.getFractionalDigits();

		if (size > 0) {
			if (digits > 0) {
				result = String.format("%s (%d,%d)", result, size, digits);
			} else {
				result = String.format("%s (%d)", result, size);
			}
		}

		return result;
	}

	/**
	 * Initialize the panel. If the os system is a mac, itunes stylish tables will installed.
	 */
	private void initializeTable() {
		ConnectionViewMainTableModel tableModel = null;
		// JTable table = null;

		tableModel = new ConnectionViewMainTableModel(data, columnsHeader);
		tableModel.setColumnClasses(String.class, String.class, Boolean.class, Boolean.class);
		final JTable table = ViewFactory.createTable(tableModel);
		ViewFactory.makeSortableTable(table);
		add(ViewFactory.createScrollableTable(table));
	}
}
