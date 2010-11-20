package de.unibonn.inf.dbdependenciesui.ui.views.triggers;

import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTrigger;
import de.unibonn.inf.dbdependenciesui.ui.views.common.AbstractDatabaseObjectTableModel;
import de.unibonn.inf.dbdependenciesui.ui.views.common.AbstractDatabaseSelectorFrame;

public class DatabaseTriggerSelectorFrame extends AbstractDatabaseSelectorFrame {

	private static final long serialVersionUID = -5035094047049725L;
	private DatabaseTriggerTableModel tableModel;

	private final TriggerViewData data;

	public DatabaseTriggerSelectorFrame(final TriggerViewData data) {
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

			tableModel = new DatabaseTriggerTableModel(data.getSelectedTrigger(), new String[] {
					name, referencedBy, dependOn, checked });
		}
		return tableModel;
	}

	@Override
	protected void executeRefresh() {
		DatabaseTriggerSelectorFrame.this.setVisible(false);
		for (final DatabaseTrigger trigger : data.getSelectedTrigger().keySet()) {
			data.getSelectedTrigger().put(trigger, tableModel.isTableSelected(trigger.getTitle()));
		}
		data.actionUpdateSelections();
		DatabaseTriggerSelectorFrame.this.dispose();
	}

}
