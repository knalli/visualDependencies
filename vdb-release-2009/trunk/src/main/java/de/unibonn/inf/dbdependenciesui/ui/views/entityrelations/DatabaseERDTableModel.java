package de.unibonn.inf.dbdependenciesui.ui.views.entityrelations;

import java.util.Map;

import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.DdlSchema;
import de.unibonn.inf.dbdependenciesui.ui.views.common.AbstractDatabaseObjectTableModel;

public class DatabaseERDTableModel extends AbstractDatabaseObjectTableModel {

	private static final long serialVersionUID = -2262715029556731567L;

	public DatabaseERDTableModel(final Map<DatabaseTable, Boolean> selectedTable, final String[] columnsHeader) {

		this.columnsHeader = columnsHeader;

		data = new Object[selectedTable.size()][columnsHeader.length];
		int i = 0;
		for (final DatabaseTable table : selectedTable.keySet()) {
			final String title = table.getTitle();
			final DdlSchema ddlschema = table.getDdlSchemaObject();
			final int cntReferencedBy = ddlschema.getForeignKeys().size();
			final int cntPrimaryKeys = ddlschema.getPrimaryKeys().size();
			final boolean checked = selectedTable.get(table);
			data[i] = new Object[] {
					title, cntReferencedBy, cntPrimaryKeys, checked };
			++i;
		}
	}

}
