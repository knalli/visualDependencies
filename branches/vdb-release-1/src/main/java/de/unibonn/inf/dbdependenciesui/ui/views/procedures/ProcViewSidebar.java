package de.unibonn.inf.dbdependenciesui.ui.views.procedures;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.explodingpixels.macwidgets.HudWidgetFactory;

import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseConnection;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;
import de.unibonn.inf.dbdependenciesui.misc.Internationalization;
import de.unibonn.inf.dbdependenciesui.ui.helpers.SystemTools;
import de.unibonn.inf.dbdependenciesui.ui.views.common.AbstractViewData;
import de.unibonn.inf.dbdependenciesui.ui.views.common.AbstractViewSidebar;
import de.unibonn.inf.dbdependenciesui.ui.views.common.AbstractViewData.Type;
import de.unibonn.inf.dbdependenciesui.ui.views.common.graph.AbstractGraphScene.LayoutType;

/**
 * This sidebar component is designed for the entity-relational-graph view.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class ProcViewSidebar extends AbstractViewSidebar {

	private static final long serialVersionUID = 1847017686980907686L;

	protected JComboBox btnSelectTable;
	private Set<DatabaseTable> tables;

	public ProcViewSidebar() {
		initialize();
	}

	@Override
	protected AbstractViewData getViewData() {
		return ProcViewData.getInstance(Type.DEFAULT);
	}

	@Override
	protected List<LayoutType> getAvailableLayouts() {
		return Arrays.asList(new LayoutType[] {
				LayoutType.CIRCLE_LAYOUT, LayoutType.KK_LAYOUT, LayoutType.SPRING_LAYOUT2 });
	}

	@Override
	protected JPanel getControlsArea() {
		if (controlsArea == null) {
			controlsArea = new JPanel();
			controlsArea.setLayout(new BorderLayout());
			controlsArea.setOpaque(false);

			final JPanel transformPanel = getTransformActionsPanel();
			controlsArea.add(transformPanel, BorderLayout.NORTH);

			final JPanel selectionPanel = getSelectTableActionsPanel();
			controlsArea.add(selectionPanel, BorderLayout.CENTER);

			final JPanel exportPanel = getExportActionsPanel();
			controlsArea.add(exportPanel, BorderLayout.SOUTH);

			// final JPanel informationPanel = getControlsInformationPanel();
			// controlsArea.add(informationPanel, BorderLayout.SOUTH);
		}
		return controlsArea;
	}

	protected void initializeAvailableTables() {
		if (tables == null) {
			final DatabaseConnection connection = ((ProcViewData) getViewData()).getConnection();
			tables = connection.getTables();
		}

		for (final DatabaseTable table : tables) {
			btnSelectTable.addItem(table);
		}
	}

	/**
	 * Create the transform controls panel. The transform controls are: switching between the "move" (move the complete
	 * viewpoint/graph) and "pick" (drag and drop one object or a set of objects), checkbox for instant saving the new
	 * position and zooming.
	 * 
	 * @return
	 */
	protected JPanel getSelectTableActionsPanel() {

		// Get all translation texts
		final String controlsTitle = Internationalization
				.getText("application.graph.sidebar.controls.tableselection.title");
		final String controlsSelection = Internationalization
				.getText("application.graph.sidebar.controls.tableselection.table");

		// Create a titled panel in gridbag layout.
		final JPanel panel = this.getTitledPanel(controlsTitle, new GridBagLayout());

		// Create a combobox display all available graph layouts.
		final JLabel lblSelectTable = new JLabel(controlsSelection);
		lblSelectTable.setForeground(foregroundColor);

		btnSelectTable = createSelectBox();
		initializeAvailableTables();
		btnSelectTable.setActionCommand(Command.CHANGE_TABLE.toString());
		btnSelectTable.addActionListener(actionChangeListener);
		lblSelectTable.setLabelFor(btnSelectTable);
		lblSelectTable.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

		final GridBagConstraints row1col1 = new GridBagConstraints();
		final GridBagConstraints row1col2 = new GridBagConstraints();

		row1col1.gridx = 0;
		row1col1.gridy = 0;
		row1col1.anchor = GridBagConstraints.EAST;
		row1col2.gridx = GridBagConstraints.RELATIVE;
		row1col2.gridy = 0;
		row1col2.anchor = GridBagConstraints.WEST;

		panel.add(lblSelectTable, row1col1);
		panel.add(btnSelectTable, row1col2);

		return panel;
	}

	/**
	 * Create a combo box object.
	 * 
	 * @param controlsReset
	 * @return
	 */
	private JComboBox createSelectBox() {
		final boolean isMac = SystemTools.isMac();
		return isMac ? HudWidgetFactory.createHudComboBox(new DefaultComboBoxModel()) : new JComboBox();
	}
}
