package de.unibonn.inf.dbdependenciesui.ui.views.connections;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;

import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseConnection;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseProcedure;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTrigger;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseView;
import de.unibonn.inf.dbdependenciesui.misc.Internationalization;

/**
 * This is a special tree cell renderer. It is used for special icons.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class ConnectionTreeCellRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = -1535330155258674566L;

	private final Icon iconConnections;
	private final Icon iconConnection;
	private final Icon iconTable;
	private final Icon iconTrigger;
	private final Icon iconProcedure;
	private final Icon iconView;
	private final Icon iconTableData;
	private final Icon iconTableColumns;
	private final Icon iconTableConstraints;

	private final String titleTables;
	private final String titleViews;
	private final String titleTriggers;
	private final String titleProcedures;
	private final String titleTableColumns;
	private final String titleTableData;
	private final String titleTableConstraints;
	private final String titleViewDefinition;

	public ConnectionTreeCellRenderer() {
		super();

		iconConnections = Internationalization.getScaledIcon("application.connections.tree.connections", 16);
		iconConnection = Internationalization.getScaledIcon("application.connections.tree.connection", 16);
		iconTable = Internationalization.getScaledIcon("application.connections.tree.table", 16);
		iconTrigger = Internationalization.getScaledIcon("application.connections.tree.trigger", 16);
		iconProcedure = Internationalization.getScaledIcon("application.connections.tree.procedure", 16);
		iconView = Internationalization.getScaledIcon("application.connections.tree.view", 16);
		iconTableData = Internationalization.getScaledIcon("application.connections.tree.table.data", 16);
		iconTableColumns = Internationalization.getScaledIcon("application.connections.tree.table.columns", 16);
		iconTableConstraints = Internationalization.getScaledIcon("application.connections.tree.table.constraints", 16);
		titleTables = Internationalization.getText("application.connections.tree.tables");
		titleViews = Internationalization.getText("application.connections.tree.views");
		titleTriggers = Internationalization.getText("application.connections.tree.triggers");
		titleProcedures = Internationalization.getText("application.connections.tree.procedures");
		titleTableColumns = Internationalization.getText("application.connections.tree.columns");
		titleTableData = Internationalization.getText("application.connections.tree.data");
		titleTableConstraints = Internationalization.getText("application.connections.tree.constraints");
		titleViewDefinition = Internationalization.getText("application.connections.tree.viewdefinition");
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.DefaultTreeCellRenderer#getTreeCellRendererComponent (javax.swing.JTree, java.lang.Object,
	 * boolean, boolean, boolean, int, boolean)
	 */
	@Override
	public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean sel,
			final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {

		// Call super method
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

		// Try to load user object
		Object object = null;
		if (value instanceof DefaultMutableTreeNode) {
			object = ((DefaultMutableTreeNode) value).getUserObject();
		}

		// If this is the root node, it will be first row (=0).
		// Otherwise we have to try find out class type of the userobject.
		// In the case that is not any known entity but a string, it would
		// probably be a topic like tables, view and so on. For further
		// determination we compare the title. If it a table name etc. it
		// compares the parent user object with the corresponding title.
		if (row == 0) {
			setIcon(iconConnections);
		} else {
			if (object instanceof DatabaseConnection) {
				setIcon(iconConnection);
			} else if (object instanceof DatabaseView) {
				setIcon(iconView);
			} else if (object instanceof DatabaseTable) {
				setIcon(iconTable);
			} else if (object instanceof DatabaseTrigger) {
				setIcon(iconTrigger);
			} else if (object instanceof DatabaseProcedure) {
				setIcon(iconProcedure);
			} else if (object instanceof String) {
				if (object.equals(titleTables)) {
					setIcon(iconTable);
				} else if (titleTableData.equals(object)) {
					setIcon(iconTableData);
				} else if (titleTableColumns.equals(object)) {
					setIcon(iconTableColumns);
				} else if (titleTableConstraints.equals(object)) {
					setIcon(iconTableConstraints);
				} else if (titleViewDefinition.equals(object)) {
					setIcon(iconView);
				} else if (titleTriggers.equals(object)) {
					setIcon(iconTrigger);
				} else if (titleProcedures.equals(object)) {
					setIcon(iconProcedure);
				} else if (titleViews.equals(object)) {
					setIcon(iconView);
				} else {
					if (value instanceof DefaultMutableTreeNode) {
						final TreeNode parent = ((DefaultMutableTreeNode) value).getParent();
						if (parent instanceof DefaultMutableTreeNode) {
							final Object parentObject = ((DefaultMutableTreeNode) parent).getUserObject();
							if (parentObject instanceof String) {
								if (titleTables.equals(parentObject)) {
									setIcon(iconTable);
								} else if (titleTriggers.equals(parentObject)) {
									setIcon(iconTrigger);
								} else if (titleViews.equals(parentObject)) {
									setIcon(iconView);
								}
							}
						}
					}
				}
			}
		}

		return this;
	}

}
