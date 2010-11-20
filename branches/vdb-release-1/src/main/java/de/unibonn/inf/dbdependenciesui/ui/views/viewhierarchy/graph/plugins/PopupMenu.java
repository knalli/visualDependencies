package de.unibonn.inf.dbdependenciesui.ui.views.viewhierarchy.graph.plugins;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import de.unibonn.inf.dbdependenciesui.controller.ThreadExecutor;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTrigger;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseView;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.Column;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.Relation;
import de.unibonn.inf.dbdependenciesui.ui.controller.ViewController;
import de.unibonn.inf.dbdependenciesui.ui.factory.ViewFactory;
import de.unibonn.inf.dbdependenciesui.ui.views.viewhierarchy.HierarchicalViewData;
import edu.uci.ics.jung.visualization.VisualizationViewer;

public class PopupMenu extends JPopupMenu {
	protected static final long serialVersionUID = 8118370265197014992L;
	protected final VisualizationViewer<DatabaseTable, Relation> viewer;
	protected ActionListener actionListener;
	protected final DatabaseTable selectedTable;
	protected final HierarchicalViewData data;

	public PopupMenu(final VisualizationViewer<DatabaseTable, Relation> vv, final DatabaseTable selectedTable,
			final HierarchicalViewData data) {
		super();
		viewer = vv;
		this.selectedTable = selectedTable;
		this.data = data;

		initializeActionListener();
		initialize();
	}

	protected void initializeActionListener() {
		actionListener = new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				Command command = null;
				try {
					command = Command.valueOf(e.getActionCommand());
				} catch (final Exception ex) {
					return;
				}

				switch (command) {
				case CLOSE:
					data.getSelectedTables().put(selectedTable, false);
					data.actionUpdateSelections();
					break;
				case SHOW_DATA:
					ThreadExecutor.execute(new Runnable() {
						@Override
						public void run() {
							ViewController.changeViewAndShowObject(ViewController.ViewMode.CONNECTIONS, selectedTable);
						}
					});
					break;
				case SHOW_SOURCECODE:
					if (selectedTable instanceof DatabaseView) {
						ViewFactory.showMessageDialog(null, ((DatabaseView) selectedTable).getSelectStatement());
					}
					break;
				}
			}
		};
	}

	/**
	 * Initialize the popup menu and adds the menu items.
	 */
	protected void initialize() {
		final String keyData = "hierarchicalview.vertex.data";
		final String keySourcecode = "hierarchicalview.vertex.sourcode";
		final String keyClose = "hierarchicalview.vertex.close";
		final String keyTriggers = "hierarchicalview.vertex.triggers";
		final String keyColumns = "hierarchicalview.vertex.columns";

		JMenuItem item;

		// Show data item and command.
		item = ViewFactory.createPopupMenuItem(keyData, actionListener);
		item.setActionCommand(Command.SHOW_DATA.toString());
		add(item);

		// Show source code item and command.
		item = ViewFactory.createPopupMenuItem(keySourcecode, actionListener);
		item.setActionCommand(Command.SHOW_SOURCECODE.toString());
		add(item);

		// Show triggers sub menu w/o command if at least one trigger exists.
		final List<DatabaseTrigger> triggers = selectedTable.getDdlSchemaObject().getTriggers();
		if (triggers.size() > 0) {
			final JMenu menu = ViewFactory.createMenu(keyTriggers, ViewFactory.applicationPopupMenuPrefix);
			for (final DatabaseTrigger trigger : triggers) {
				menu.add(trigger.getTitle());
			}
			add(menu);
		}

		// Show columns if exist (but should, actually).
		final List<Column> columns = selectedTable.getDdlSchemaObject().getColumns();
		if (!columns.isEmpty()) {
			final JMenu menu = ViewFactory.createPopupMenu(keyColumns);
			for (final Column column : columns) {
				item = new JMenuItem(column.toString());
				item.setActionCommand(Command.SHOW_COLUMN.toString());
				item.addActionListener(actionListener);
				menu.add(item);
			}
			add(menu);
		}

		addSeparator();

		// Remove object and command.
		item = ViewFactory.createPopupMenuItem(keyClose, actionListener);
		item.setActionCommand(Command.CLOSE.toString());
		add(item);
	}

	public static enum Command {
		SHOW_DATA, SHOW_SOURCECODE, CLOSE, SHOW_COLUMN
	}
}
