/**
 * $Id: Main.java 41 2009-03-01 16:21:20Z philipp $
 */
package de.unibonn.inf.dbdependenciesui.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Dialog.ModalExclusionType;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JSplitPane;

import com.explodingpixels.macwidgets.MacUtils;
import com.explodingpixels.widgets.WindowUtils;

import de.unibonn.inf.dbdependenciesui.Configuration;
import de.unibonn.inf.dbdependenciesui.controller.ThreadExecutor;
import de.unibonn.inf.dbdependenciesui.misc.Icons;
import de.unibonn.inf.dbdependenciesui.misc.Internationalization;
import de.unibonn.inf.dbdependenciesui.ui.controller.ViewController;
import de.unibonn.inf.dbdependenciesui.ui.controller.ViewController.Notification;
import de.unibonn.inf.dbdependenciesui.ui.controller.ViewController.ViewMode;
import de.unibonn.inf.dbdependenciesui.ui.helpers.SystemTools;
import de.unibonn.inf.dbdependenciesui.ui.views.connections.ConnectionViewMain;
import de.unibonn.inf.dbdependenciesui.ui.views.connections.ConnectionViewSidebar;
import de.unibonn.inf.dbdependenciesui.ui.views.entityrelations.ERDViewMain;
import de.unibonn.inf.dbdependenciesui.ui.views.entityrelations.ERDViewSidebar;
import de.unibonn.inf.dbdependenciesui.ui.views.procedures.ProcViewMain;
import de.unibonn.inf.dbdependenciesui.ui.views.procedures.ProcViewSidebar;
import de.unibonn.inf.dbdependenciesui.ui.views.triggers.TriggerViewMain;
import de.unibonn.inf.dbdependenciesui.ui.views.triggers.TriggerViewSidebar;
import de.unibonn.inf.dbdependenciesui.ui.views.viewhierarchy.HierarchicalViewMain;
import de.unibonn.inf.dbdependenciesui.ui.views.viewhierarchy.HierarchicalViewSidebar;

/**
 * The main application view.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 * @author Marc Kastleiner
 */
public class ApplicationView extends JFrame implements Observer {

	private static final Logger log = Logger.getLogger(Configuration.LOGGER);

	private static final long serialVersionUID = -1319902777570670352L;
	private JSplitPane jSplitPane;
	private ConnectionViewSidebar connectionViewSidebar;
	private ConnectionViewMain connectionViewMain;
	private HierarchicalViewSidebar hierarchicViewSidebar;
	private HierarchicalViewMain hierarchicViewMain;
	private TriggerViewSidebar triggerViewSidebar;
	private TriggerViewMain triggerViewMain;
	private ERDViewSidebar erdViewSidebar;
	private ERDViewMain erdViewMain;
	private ProcViewSidebar procViewSidebar;
	private ProcViewMain procViewMain;

	private final Map<ViewMode, JComponent> viewSidebars = new HashMap<ViewMode, JComponent>();
	private final Map<ViewMode, JComponent> viewMains = new HashMap<ViewMode, JComponent>();

	protected final int standardDividerLocation = 220;

	private ViewMode currentViewMode;

	private ApplicationViewMenuBar menuBar;

	public ApplicationView() {
		// hide while loading
		setVisible(false);

		ViewController.setApplicationView(this);

		setModalExclusionType(ModalExclusionType.NO_EXCLUDE);

		// init
		initialize();

		// position mid/center
		setLocationRelativeTo(null);

		ViewController.addObserverObject(this);
	}

	private void initialize() {
		setSize(new Dimension(Configuration.APPLICATION_WIDTH, Configuration.APPLICATION_HEIGHT));

		// OSX User Guideline: Frame's titlebar and toolbar are seamless.
		// Additionally, the toolbar allows mouse drags.
		if (SystemTools.isMac()) {
			MacUtils.makeWindowLeopardStyle(getRootPane());
			WindowUtils.createAndInstallRepaintWindowFocusListener(this);
		}

		setIconImage(Icons.createImage(Internationalization.getText("application.icon")));

		menuBar = new ApplicationViewMenuBar();
		setJMenuBar(menuBar);

		setTitle(Internationalization.getText("application.title"));

		// set system close operation (exit) if frame/window is closed
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// add the toolbar at the top of the panel (page start)
		add(new ApplicationViewToolBar(), BorderLayout.PAGE_START);

		add(getJSplitPane());
		setViewMode(ViewMode.CONNECTIONS);

		initializeResizeEvents();
	}

	/**
	 * Add the given pair (viewMode, component) to the sidebar's storage.
	 * 
	 * @param viewMode
	 * @param component
	 */
	private void registerSidebarComponent(final ViewMode viewMode, final JComponent component) {
		if (!viewSidebars.containsKey(viewMode)) {
			component.setBorder(BorderFactory.createEmptyBorder());
			viewSidebars.put(viewMode, component);
		}
	}

	/**
	 * Add the given pair (viewMode, component) to the main's storage.
	 * 
	 * @param viewMode
	 * @param component
	 */
	private void registerMainComponent(final ViewMode viewMode, final JComponent component) {
		if (!viewMains.containsKey(viewMode)) {
			component.setBorder(BorderFactory.createEmptyBorder());
			viewMains.put(viewMode, component);
		}
	}

	private JSplitPane getJSplitPane() {
		if (jSplitPane == null) {
			jSplitPane = new JSplitPane();
			jSplitPane.setBorder(BorderFactory.createEmptyBorder());
		}
		return jSplitPane;
	}

	private ConnectionViewSidebar getConnectionViewSidebar() {
		if (connectionViewSidebar == null) {
			connectionViewSidebar = new ConnectionViewSidebar();
		}
		return connectionViewSidebar;
	}

	private ConnectionViewMain getConnectionViewMain() {
		if (connectionViewMain == null) {
			connectionViewMain = ConnectionViewMain.getInstance();
			final TabsChangeListener tabChangeListener = new TabsChangeListener(menuBar);
			connectionViewMain.getConnectionTabbedPane().addPropertyChangeListener(tabChangeListener);
			connectionViewMain.getConnectionTabbedPane().addWelcomeTab();
		}
		return connectionViewMain;
	}

	private HierarchicalViewSidebar getHierarchicViewSidebar() {
		if (hierarchicViewSidebar == null) {
			hierarchicViewSidebar = new HierarchicalViewSidebar();
		}
		return hierarchicViewSidebar;
	}

	private HierarchicalViewMain getHierarchicViewMain() {
		if (hierarchicViewMain == null) {
			hierarchicViewMain = new HierarchicalViewMain();
		}
		return hierarchicViewMain;
	}

	private TriggerViewSidebar getTriggerViewSidebar() {
		if (triggerViewSidebar == null) {
			triggerViewSidebar = new TriggerViewSidebar();
		}
		return triggerViewSidebar;
	}

	private TriggerViewMain getTriggerViewMain() {
		if (triggerViewMain == null) {
			triggerViewMain = new TriggerViewMain();
		}
		return triggerViewMain;
	}

	private ERDViewSidebar getERDViewSidebar() {
		if (erdViewSidebar == null) {
			erdViewSidebar = new ERDViewSidebar();
		}
		return erdViewSidebar;
	}

	private ERDViewMain getERDViewMain() {
		if (erdViewMain == null) {
			erdViewMain = new ERDViewMain();
		}
		return erdViewMain;
	}

	private ProcViewSidebar getProcViewSidebar() {
		if (procViewSidebar == null) {
			procViewSidebar = new ProcViewSidebar();
		}
		return procViewSidebar;
	}

	private ProcViewMain getProcViewMain() {
		if (procViewMain == null) {
			procViewMain = new ProcViewMain();
		}
		return procViewMain;
	}

	/**
	 * Activate a new view mode. All affected components will replaced if exist.
	 * 
	 * @param newViewMode
	 */
	public void setViewMode(final ViewMode newViewMode) {
		if ((newViewMode != null) && !newViewMode.equals(currentViewMode)) {
			registerViewModeComponents(newViewMode);

			// The old divider location will temporarily saved because it seems
			// that swing "forget" the location when replacing elements.
			// final int oldDivider = getJSplitPane().getDividerLocation();

			getJSplitPane().setLeftComponent(viewSidebars.get(newViewMode));
			getJSplitPane().setRightComponent(viewMains.get(newViewMode));

			getJSplitPane().setDividerLocation(standardDividerLocation);
			getJSplitPane().setDividerSize(0);

		}
		currentViewMode = newViewMode;
		ApplicationView.log.info("New view mode is: " + newViewMode);

		// Update the text of the window
		refreshTitleBar();
	}

	/**
	 * Create all components by the given view mode if not exist.
	 * 
	 * @param viewMode
	 */
	private void registerViewModeComponents(final ViewMode viewMode) {
		switch (viewMode) {
		case CONNECTIONS:
			registerSidebarComponent(viewMode, getConnectionViewSidebar());
			registerMainComponent(viewMode, getConnectionViewMain());
			break;
		case HIERARCHY:
			registerSidebarComponent(viewMode, getHierarchicViewSidebar());
			registerMainComponent(viewMode, getHierarchicViewMain());
			break;
		case TRIGGERS:
			registerSidebarComponent(viewMode, getTriggerViewSidebar());
			registerMainComponent(viewMode, getTriggerViewMain());
			break;
		case ERD:
			registerSidebarComponent(viewMode, getERDViewSidebar());
			registerMainComponent(viewMode, getERDViewMain());
			break;
		case PROCEDURES:
			registerSidebarComponent(viewMode, getProcViewSidebar());
			registerMainComponent(viewMode, getProcViewMain());
			break;
		}
	}

	private void initializeResizeEvents() {}

	/**
	 * Update the titlebar depending the currentViewMode.
	 */
	private void refreshTitleBar() {
		String suffix = "Unknown";

		ViewMode currentViewMode = this.currentViewMode;
		if (currentViewMode == null) {
			currentViewMode = ViewMode.CONNECTIONS;
		}

		switch (currentViewMode) {
		case CONNECTIONS:
			suffix = Internationalization.getText("application.functions.connections");
			break;
		case ERD:
			suffix = Internationalization.getText("application.functions.erd");
			break;
		case PROCEDURES:
			suffix = Internationalization.getText("application.functions.procedures");
			break;
		case HIERARCHY:
			suffix = Internationalization.getText("application.functions.hierarchy");
			break;
		case TRIGGERS:
			suffix = Internationalization.getText("application.functions.triggers");
			break;
		}

		setTitle(Internationalization.getTextFormatted("application.title.detail", suffix));
	}

	@Override
	public void update(final Observable o, final Object arg) {
		if ((arg != null) && (arg instanceof Notification)) {
			final Notification notification = (Notification) arg;

			ThreadExecutor.execute(new Runnable() {

				@Override
				public void run() {
					switch (notification) {
					case VIEW_CHANGED:
						setViewMode(ViewController.getViewMode());
						break;
					}
				}
			});
		}
	}

	public void removeConnectionTab(final String title) {
		getConnectionViewMain().getConnectionTabbedPane().removeTabByTitle(title);
	}

	public void removeAllConnectionTabs() {
		getConnectionViewMain().getConnectionTabbedPane().removeAllTabs();
	}

	public void removeAllConnectionTabsExceptThis(final String title) {
		getConnectionViewMain().getConnectionTabbedPane().removeAllConnectionTabsExceptThis(title);
	}

	public void setCurrentConnectionTab(final String title) {
		getConnectionViewMain().getConnectionTabbedPane().setSelectedTabByTitle(title);
	}
}
