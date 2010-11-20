package de.unibonn.inf.dbdependenciesui.ui.views.entityrelations;

import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;
import de.unibonn.inf.dbdependenciesui.ui.views.common.AbstractDatabaseObjectTableModel;
import de.unibonn.inf.dbdependenciesui.ui.views.common.AbstractDatabaseSelectorFrame;

public class DatabaseERDSelectorFrame extends AbstractDatabaseSelectorFrame {
	private static final long serialVersionUID = -5035094047049725L;
	private DatabaseERDTableModel tableModel;

	private final ERDViewData data;

	public DatabaseERDSelectorFrame(final ERDViewData data) {
		super();
		this.data = data;
		initialize();
	}

	@Override
	protected AbstractDatabaseObjectTableModel getTableModel() {
		if (tableModel == null) {
			final String name = "Name";
			final String referencedBy = "Referenced by";
			final String primaryKeys = "Primary Keys";
			final String checked = "Checked";

			tableModel = new DatabaseERDTableModel(data.getSelectedTables(), new String[] {
					name, referencedBy, primaryKeys, checked });
		}
		return tableModel;
	}

	@Override
	protected void executeRefresh() {
		DatabaseERDSelectorFrame.this.setVisible(false);
		for (final DatabaseTable table : data.getSelectedTables().keySet()) {
			data.getSelectedTables().put(table, tableModel.isTableSelected(table.getTitle()));
		}
		data.actionUpdateSelections();
		DatabaseERDSelectorFrame.this.dispose();
	}

}
