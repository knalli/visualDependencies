package de.unibonn.inf.dbdependenciesui.ui.views.triggers;

import java.util.Map;

import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTrigger;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.TriggerSchema;
import de.unibonn.inf.dbdependenciesui.ui.views.common.AbstractDatabaseObjectTableModel;

public class DatabaseTriggerTableModel extends AbstractDatabaseObjectTableModel {

	private static final long serialVersionUID = -2262715029556731567L;

	public DatabaseTriggerTableModel(final Map<DatabaseTrigger, Boolean> selectedTrigger, final String[] columnsHeader) {

		this.columnsHeader = columnsHeader;

		data = new Object[selectedTrigger.size()][columnsHeader.length];
		int i = 0;
		for (final DatabaseTrigger trigger : selectedTrigger.keySet()) {
			final String title = trigger.getTitle();
			final TriggerSchema ddlschema = trigger.createTriggerSchemaEditableObject();
			final int cntReferencedBy = ddlschema.getAffectedTables().size();
			final String cntDependOn = ddlschema.getTableName();
			final boolean checked = selectedTrigger.get(trigger);
			data[i] = new Object[] {
					title, cntReferencedBy, cntDependOn, checked };
			++i;
		}
	}

}
