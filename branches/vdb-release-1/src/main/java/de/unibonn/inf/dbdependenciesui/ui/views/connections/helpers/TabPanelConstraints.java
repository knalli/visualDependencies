package de.unibonn.inf.dbdependenciesui.ui.views.connections.helpers;

import java.awt.BorderLayout;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.JTable;

import de.unibonn.inf.dbdependenciesui.Configuration;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.ForeignKey;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.Index;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.PrimaryKey;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.UniqueKey;
import de.unibonn.inf.dbdependenciesui.misc.Internationalization;
import de.unibonn.inf.dbdependenciesui.ui.factory.ViewFactory;

public class TabPanelConstraints extends JPanel {

	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(Configuration.LOGGER);

	private static final long serialVersionUID = 381489754534833764L;

	private final Object[][] data;

	private final String[] columnsHeader = {
			Internationalization.getText("application.connections.table.header.constraintname"),
			Internationalization.getText("application.connections.table.header.constrainttype"),
			Internationalization.getText("application.connections.table.header.columns"),
			Internationalization.getText("application.connections.table.header.referencedtable"),
			Internationalization.getText("application.connections.table.header.referencedcolumn") };

	private int columnSize = 0;

	// private JScrollPane scrollPane;

	private final List<PrimaryKey> primarys;
	private final List<UniqueKey> uniques;
	private final List<ForeignKey> foreigns;
	private final List<Index> indices;

	public TabPanelConstraints(final DatabaseTable table) {
		super(new BorderLayout());
		primarys = table.getDdlSchemaObject().getPrimaryKeys();
		uniques = table.getDdlSchemaObject().getUniqueKeys();
		foreigns = table.getDdlSchemaObject().getForeignKeys();
		indices = table.getDdlSchemaObject().getIndices();

		columnSize = (foreigns.size() + uniques.size() + primarys.size() + indices.size());

		data = new Object[columnSize][5];

		init();
	}

	private void init() {
		boolean index = true;
		int i = 0;

		if (primarys != null) {
			for (final PrimaryKey key : primarys) {
				data[i][0] = key.getName();
				data[i][1] = Internationalization.getText("application.connections.table.content.primary");
				for (final String prim : key.getColumns()) {
					if (index) {
						data[i][2] = prim;
					} else {
						data[i][2] = data[i][2] + ", " + prim;
					}
					index = false;
				}
				i++;
			}
		}

		if (uniques != null) {
			for (final UniqueKey key : uniques) {
				data[i][0] = key.getName();
				data[i][1] = Internationalization.getText("application.connections.table.content.unique");
				index = true;
				for (final String column : key.getColumns()) {
					if (index) {
						data[i][2] = column;
					} else {
						data[i][2] = data[i][2] + ", " + column;
					}
					index = false;
				}
				i++;
			}
		}

		if (indices != null) {
			for (final Index key : indices) {
				data[i][0] = key.getName();
				data[i][1] = Internationalization.getText("application.connections.table.content.index");
				index = true;
				for (final String column : key.getColumns()) {
					if (index) {
						data[i][2] = column;
					} else {
						data[i][2] = data[i][2] + ", " + column;
					}
					index = false;
				}
				i++;
			}
		}

		if (foreigns != null) {
			for (final ForeignKey key : foreigns) {
				data[i][0] = key.getName();
				data[i][1] = Internationalization.getText("application.connections.table.content.foreign");
				data[i][2] = key.getColumn();
				data[i][3] = key.getReferToTable();
				data[i][4] = key.getReferToColumn();
				i++;
			}
		}

		initializeTable();
	}

	/**
	 * Initialize the panel. If the os system is a mac, itunes stylish tables will installed.
	 */
	private void initializeTable() {
		ConnectionViewMainTableModel tableModel = null;
		// JTable table = null;

		tableModel = new ConnectionViewMainTableModel(data, columnsHeader);
		tableModel.setColumnClasses(String.class, String.class, String.class, String.class, String.class);
		final JTable table = ViewFactory.createTable(tableModel);
		ViewFactory.makeSortableTable(table);
		add(ViewFactory.createScrollableTable(table));
	}
}
