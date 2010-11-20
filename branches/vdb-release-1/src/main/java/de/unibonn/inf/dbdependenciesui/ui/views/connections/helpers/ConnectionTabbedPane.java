package de.unibonn.inf.dbdependenciesui.ui.views.connections.helpers;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JTabbedPane;

import de.unibonn.inf.dbdependenciesui.Configuration;
import de.unibonn.inf.dbdependenciesui.controller.Controller;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseConnection;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseProcedure;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTrigger;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseView;
import de.unibonn.inf.dbdependenciesui.misc.Internationalization;

/**
 * A special tabbed pane containing several tabs about tables' data, columns and definitions.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 * @author Marc Kastleiner
 */
public class ConnectionTabbedPane extends JTabbedPane {

	protected static final Logger log = Logger.getLogger(Configuration.LOGGER);

	private static final long serialVersionUID = 1L;

	private final List<String> tabTitles = new ArrayList<String>();

	private transient DatabaseTable object;
	private transient DatabaseTrigger trigger;
	private transient DatabaseProcedure procedure;

	private String elementTables = null;
	private String elementViews = null;
	private String elementTriggers = null;
	private String elementProcedures = null;
	private String elementData = null;
	private String elementColumns = null;
	private String elementConstraints = null;
	private String elementViewDefinition = null;

	private boolean isTableOrView;
	private boolean isTrigger;

	private DatabaseConnection conn;

	private int amount = 0;

	public ConnectionTabbedPane() {
		super();
		initialize();
	}

	protected void initialize() {
		setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

		elementTables = Internationalization.getText("application.connections.tree.tables");
		elementViews = Internationalization.getText("application.connections.tree.views");
		elementTriggers = Internationalization.getText("application.connections.tree.triggers");
		elementProcedures = Internationalization.getText("application.connections.tree.procedures");
		elementData = Internationalization.getText("application.connections.tree.data");
		elementColumns = Internationalization.getText("application.connections.tree.columns");
		elementConstraints = Internationalization.getText("application.connections.tree.constraints");
		elementViewDefinition = Internationalization.getText("application.connections.tree.viewdefinition");
	}

	/**
	 * Add Welcome Panel w/o a close option.
	 */
	public void addWelcomeTab() {
		final String title = Internationalization.getText("application.menu.tabs.welcome");
		tabTitles.add(title);
		insertTab(title, null, new WelcomePanel(), title, 0);
		setTabComponentAt(amount, new TabbedComponent(this, title, "", title, title, title, null));
		amount++;
		firePropertyChange("openTabItem", null, title);
	}

	/**
	 * Create a new tab with the given parameters unless it exists. The tab will be selected.
	 * 
	 * @param element
	 * @param connection
	 * @param item
	 * @param name
	 */
	public void addTabByParams(final String element, final String connection, final String item, final String name) {

		// Insert a new tab unless it exists already.
		final String title = createTooltip(connection, element, name);

		if (!tabTitles.contains(title)) {
			conn = Controller.loadConnection(connection, true);
			log.info(item);

			// Load object or trigger.
			if (item.equals(elementTables)) {
				findTable(name);
			} else if (item.equals(elementViews)) {
				findView(name);
			} else if (item.equals(elementTriggers)) {
				findTrigger(name);
			} else if (item.equals(elementProcedures)) {
				findProcedure(name);
			}

			// Create panel component.
			if (isTableOrView) {
				addTableViewPanel(element, name);
			} else if (isTrigger) {
				addTriggerViewPanel(name);
			} else {
				addProcedureViewPanel(name);
			}

			final Icon icon = createIcon(element, name, item);
			setTabComponentAt(amount, new TabbedComponent(this, name, conn.getTitle(), element, name, title, icon));

			tabTitles.add(title);

			amount++;

			firePropertyChange("openTabItem", null, title);
		} else {
			setSelectedComponent(getComponentAt(tabTitles.indexOf(title)));
			firePropertyChange("selectTabItem", null, title);
		}
	}

	protected Component getComponentById(final String element, final String connection, final String name) {
		return getComponentById(createTabIdentifier(connection, name, element));
	}

	protected Component getComponentById(final String tabIdentifier) {
		return getComponentAt(tabTitles.indexOf(tabIdentifier));
	}

	protected Component addTriggerViewPanel(final String name) {
		final Component component = new TabPanelTrigger(trigger);
		insertTab(name, null, component, null, getTabCount());
		setSelectedComponent(component);
		return component;
	}

	protected Component addProcedureViewPanel(final String name) {
		final Component component = new TabPanelProcedure(procedure);
		insertTab(name, null, component, null, getTabCount());
		setSelectedComponent(component);
		return component;
	}

	protected Icon createIcon(final String element, final String name, final String item) {

		Icon icon = null;
		if (element.equals(elementData)) {
			icon = Internationalization.getScaledIcon("application.connections.tabs.data", 14);
		} else if (element.equals(elementColumns)) {
			icon = Internationalization.getScaledIcon("application.connections.tabs.columns", 14);
		} else if (element.equals(elementConstraints)) {
			icon = Internationalization.getScaledIcon("application.connections.tabs.constraints", 14);
		} else if (item.equals(elementTables)) {
			icon = Internationalization.getScaledIcon("application.connections.tabs.table", 14);
		} else if (item.equals(elementViews)) {
			icon = Internationalization.getScaledIcon("application.connections.tabs.view", 14);
		} else if (item.equals(elementTriggers)) {
			icon = Internationalization.getScaledIcon("application.connections.tabs.trigger", 14);
		} else if (item.equals(elementProcedures)) {
			icon = Internationalization.getScaledIcon("application.connections.tabs.procedure", 14);
		}

		return icon;
	}

	protected String createTooltip(final String element, final String name) {

		String key = null;
		Object[] args = null;

		if (element.equals(elementData)) {
			key = "application.connections.tabs.data.tooltip";
			args = new Object[] {
					name, object.getConnection().getTitle() };
		} else if (element.equals(elementColumns)) {
			key = "application.connections.tabs.columns.tooltip";
			args = new Object[] {
					name, object.getConnection().getTitle() };
		} else if (element.equals(elementConstraints)) {
			key = "application.connections.tabs.constraints.tooltip";
			args = new Object[] {
					name, object.getConnection().getTitle() };
		} else if (element.equals(elementViewDefinition)) {
			key = "application.connections.tabs.viewdefinition.tooltip";
			args = new Object[] {
					name, object.getConnection().getTitle() };
		} else if ((trigger != null) && (object == null)) {
			key = "application.connections.tabs.common.tooltip";
			args = new Object[] {
					element, trigger.getConnection().getTitle() };
		} else if ((procedure != null) && (object == null)) {
			key = "application.connections.tabs.common.tooltip";
			args = new Object[] {
					element, procedure.getConnection().getTitle() };
		} else {
			key = "application.connections.tabs.common.tooltip";
			args = new Object[] {
					element, object.getConnection().getTitle() };
		}

		return Internationalization.getTextFormatted(key, args);
	}

	protected String createTooltip(final String connection, final String element, final String name) {

		String key = null;
		Object[] args = null;

		if (element.equals(elementData)) {
			key = "application.connections.tabs.data.tooltip";
			args = new Object[] {
					name, connection };
		} else if (element.equals(elementColumns)) {
			key = "application.connections.tabs.columns.tooltip";
			args = new Object[] {
					name, connection };
		} else if (element.equals(elementConstraints)) {
			key = "application.connections.tabs.constraints.tooltip";
			args = new Object[] {
					name, connection };
		} else if (element.equals(elementViewDefinition)) {
			key = "application.connections.tabs.viewdefinition.tooltip";
			args = new Object[] {
					name, connection };
		} else if ((trigger != null) && (object == null)) {
			key = "application.connections.tabs.common.tooltip";
			args = new Object[] {
					element, connection };
		} else if ((procedure != null) && (object == null)) {
			key = "application.connections.tabs.common.tooltip";
			args = new Object[] {
					element, connection };
		} else {
			key = "application.connections.tabs.common.tooltip";
			args = new Object[] {
					element, connection };
		}

		return Internationalization.getTextFormatted(key, args);
	}

	protected Component addTableViewPanel(final String element, final String name) {

		Component component = null;
		if (element.equals(elementData)) {
			component = new TabPanelData(object, conn);
		} else if (element.equals(elementColumns)) {
			component = new TabPanelColumns(object);
		} else if (element.equals(elementConstraints)) {
			component = new TabPanelConstraints(object);
		} else if (element.equals(elementViewDefinition)) {
			component = new TabPanelViewDefinition(object);
		}

		if (component != null) {
			insertTab(name, null, component, null, getTabCount());
			setSelectedComponent(component);
		}

		return component;
	}

	protected void findTable(final String name) {
		for (final DatabaseTable temp : conn.getTables()) {
			if (temp.getTitle().equals(name)) {
				object = temp;
				break;
			}
		}
		ConnectionTabbedPane.log.info(object.getTitle());
		isTableOrView = true;
	}

	protected void findView(final String name) {
		for (final DatabaseView temp : conn.getViews()) {
			if (temp.getTitle().equals(name)) {
				object = temp;
			}
		}
		ConnectionTabbedPane.log.info(object.getTitle());
		isTableOrView = true;
	}

	protected void findTrigger(final String name) {
		for (final DatabaseTrigger temp : conn.getTriggers()) {
			if (temp.getTitle().equals(name)) {
				trigger = temp;
			}
		}
		ConnectionTabbedPane.log.info(trigger.getTitle());
		isTableOrView = false;
		isTrigger = true;
	}

	protected void findProcedure(final String name) {
		for (final DatabaseProcedure temp : conn.getProcedures()) {
			if (temp.getTitle().equals(name)) {
				procedure = temp;
			}
		}
		ConnectionTabbedPane.log.info(procedure.getTitle());
		isTableOrView = false;
		isTrigger = false;
	}

	protected void removeComponentFromListsByIndex(final int index) {
		final String title = tabTitles.get(index);

		remove(index);
		tabTitles.remove(index);
		amount--;

		firePropertyChange("closeTabItem", title, null);
	}

	protected int getComponentIndexByTitle(final String title) {
		return tabTitles.indexOf(title);
	}

	public boolean removeTab(final String connection, final String name, final String element) {
		return removeTabByTitle(createTooltip(connection, element, name));
	}

	public boolean removeTabByTitle(final String title) {
		final int i = getComponentIndexByTitle(title);
		// do not remove the first panel (welcome). UPDATE 15.07.09 first panel removeable

		if (i >= 0) {
			removeComponentFromListsByIndex(i);
			return true;
		}
		return false;
	}

	protected String createTabIdentifier(final String connection, final String name, final String element) {
		return connection + "+" + name + "+" + element;
	}

	public void setSelectedTab(final String connection, final String name, final String element) {
		setSelectedTabByTitle(createTooltip(connection, element, name));
	}

	public void setSelectedTabByTitle(final String title) {
		final int i = getComponentIndexByTitle(title);
		setSelectedIndex(i);
		firePropertyChange("selectTabItem", null, title);
	}

	public void removeAllTabs() {
		final List<String> titles = new ArrayList<String>();
		for (final String title : tabTitles) {
			titles.add(title);
		}

		for (final String title : titles) {
			removeTabByTitle(title);
		}

		addWelcomeTab();
	}

	public void removeAllConnectionTabsExceptThis(final String title) {

		final List<String> titles = new ArrayList<String>();
		for (final String title2 : tabTitles) {
			if (!title2.equals(title)) {
				titles.add(title2);
			}
		}

		for (final String title2 : titles) {
			removeTabByTitle(title2);
		}
	}
}
