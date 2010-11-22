/**
 * $Id: Main.java 41 2009-03-01 16:21:20Z philipp $
 */
package de.unibonn.inf.dbdependenciesui.ui.views.viewhierarchy;

import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;
import de.unibonn.inf.dbdependenciesui.ui.views.common.AbstractDatabaseObjectTableModel;
import de.unibonn.inf.dbdependenciesui.ui.views.common.AbstractDatabaseSelectorFrame;

/**
 * This frame displays the table selector. The user can select which tables will used by the model2graph-transformer.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class DatabaseTableSelectorFrame extends AbstractDatabaseSelectorFrame {
	private static final long serialVersionUID = -6767981284671755695L;
	private DatabaseObjectTableModel tableModel;

	private final HierarchicalViewData data;

	public DatabaseTableSelectorFrame(final HierarchicalViewData data) {
		super();
		this.data = data;
		initialize();
	}

	@Override
	protected AbstractDatabaseObjectTableModel getTableModel() {
		if (tableModel == null) {
			final String name = "Name";
			final String referencedBy = "Referenced by";
			final String dependOn = "Depend on";
			final String checked = "Checked";

			tableModel = new DatabaseObjectTableModel(data.getSelectedTables(), new String[] {
					name, referencedBy, dependOn, checked });
		}
		return tableModel;
	}

	@Override
	protected void executeRefresh() {
		DatabaseTableSelectorFrame.this.setVisible(false);
		for (final DatabaseTable table : data.getSelectedTables().keySet()) {
			data.getSelectedTables().put(table, tableModel.isTableSelected(table.getTitle()));
		}
		data.actionUpdateSelections();
		DatabaseTableSelectorFrame.this.dispose();
	}

}
