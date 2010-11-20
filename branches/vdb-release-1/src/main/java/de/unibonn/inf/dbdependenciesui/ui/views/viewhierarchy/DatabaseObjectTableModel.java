/**
 * $Id: Main.java 41 2009-03-01 16:21:20Z philipp $
 */
package de.unibonn.inf.dbdependenciesui.ui.views.viewhierarchy;

import java.util.Map;

import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.DdlSchema;
import de.unibonn.inf.dbdependenciesui.ui.views.common.AbstractDatabaseObjectTableModel;

/**
 * The table model for the selection of objects from DatabaseTable. This model is used for the user selection of tables.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class DatabaseObjectTableModel extends AbstractDatabaseObjectTableModel {

	private static final long serialVersionUID = 817081349658294349L;

	public DatabaseObjectTableModel(final Map<DatabaseTable, Boolean> selectedTables, final String[] columnsHeader) {

		this.columnsHeader = columnsHeader;

		data = new Object[selectedTables.size()][columnsHeader.length];
		int i = 0;
		for (final DatabaseTable table : selectedTables.keySet()) {
			final String title = table.getTitle();
			final DdlSchema ddlschema = table.getDdlSchemaObject();
			final int cntReferencedBy = ddlschema.getTargetRelations().size();
			final int cntDependOn = ddlschema.getSourceRelations().size();
			final boolean checked = selectedTables.get(table);
			data[i] = new Object[] {
					title, cntReferencedBy, cntDependOn, checked };
			++i;
		}
	}

}
