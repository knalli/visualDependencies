package de.unibonn.inf.dbdependenciesui.ui.views.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.NumberFormat;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.explodingpixels.macwidgets.HudWidgetFactory;

import de.unibonn.inf.dbdependenciesui.controller.ThreadExecutor;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;
import de.unibonn.inf.dbdependenciesui.misc.Internationalization;
import de.unibonn.inf.dbdependenciesui.ui.controller.ViewController;
import de.unibonn.inf.dbdependenciesui.ui.helpers.SystemTools;
import de.unibonn.inf.dbdependenciesui.ui.views.common.AbstractViewData.Notification;
import de.unibonn.inf.dbdependenciesui.ui.views.common.graph.AbstractGraphScene.LayoutType;

/**
 * This component is a swing element and represents a sidebar element in the graph-view. The class methods
 * {@link #getAvailableLayouts()}, {@link #getDefaultLayout()} and {@link #getViewData()} have to be implemented.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public abstract class AbstractViewSidebar extends JPanel implements Observer {

	protected static final long serialVersionUID = -8421982454496432274L;
	protected AbstractViewData data;
	protected JPanel satelliteArea;
	protected JPanel controlsArea;
	protected ActionChangeListener actionChangeListener;

	protected final Color foregroundColor = new Color(240, 240, 240);
	protected final Color backgroundColor = new Color(178, 178, 178);
	protected JSpinner spnrZoom;
	protected JComboBox btnLayouts;

	public AbstractViewSidebar() {
		initialize();
	}

	/**
	 * Return the corresponding data instance.
	 * 
	 * @return
	 */
	abstract protected AbstractViewData getViewData();

	/**
	 * Return a list of all available layout types.
	 * 
	 * @return
	 */
	abstract protected List<LayoutType> getAvailableLayouts();

	/**
	 * Initialize all layouts.
	 * 
	 * @uses {@link #getAvailableLayouts()}, {@link #getDefaultLayout()}
	 */
	protected void initializeAvailableLayouts() {
		for (final LayoutType type : getAvailableLayouts()) {
			btnLayouts.addItem(type);
		}
		// Set default selected item before set actioncommand -- the change will ignored.
		btnLayouts.setSelectedItem(data.getCurrentLayout());
	}

	protected void initialize() {
		data = getViewData();
		data.addObserver(this);

		actionChangeListener = new ActionChangeListener(this);

		setPreferredSize(new Dimension(210, 500));
		setLayout(new BorderLayout());
		add(getSatelliteArea(), BorderLayout.NORTH);
		add(getControlsArea(), BorderLayout.CENTER);
		add(getHelpArea(), BorderLayout.SOUTH);
		setBackground(Color.darkGray);
	}

	/**
	 * Create and return the panel used for displaying the satellite (initially in the top left corner).
	 * 
	 * @return
	 */
	protected JPanel getSatelliteArea() {
		if (satelliteArea == null) {
			satelliteArea = new JPanel();
			satelliteArea.setSize(new Dimension(210, 210));
			satelliteArea.setPreferredSize(new Dimension(210, 210));
			satelliteArea.setLayout(new FlowLayout());
			satelliteArea.setBorder(BorderFactory.createLineBorder(Color.darkGray, 2));
			satelliteArea.setBackground(backgroundColor);
		}
		return satelliteArea;
	}

	/**
	 * Create and return the panel used for displaying some controls (initially under the satellite).
	 * 
	 * @return
	 */
	protected JPanel getControlsArea() {
		if (controlsArea == null) {
			controlsArea = new JPanel();
			controlsArea.setLayout(new BorderLayout());
			controlsArea.setOpaque(false);

			final JPanel transformPanel = getTransformActionsPanel();
			controlsArea.add(transformPanel, BorderLayout.NORTH);

			final JPanel selectionPanel = getSelectionActionsPanel();
			controlsArea.add(selectionPanel, BorderLayout.CENTER);

			final JPanel exportPanel = getExportActionsPanel();
			controlsArea.add(exportPanel, BorderLayout.SOUTH);

			// final JPanel informationPanel = getControlsInformationPanel();
			// controlsArea.add(informationPanel, BorderLayout.SOUTH);
		}
		return controlsArea;
	}

	/**
	 * Create the transform controls panel. The transform controls are: switching between the "move" (move the complete
	 * viewpoint/graph) and "pick" (drag and drop one object or a set of objects), checkbox for instant saving the new
	 * position and zooming.
	 * 
	 * @return
	 */
	protected JPanel getTransformActionsPanel() {
		final boolean isMac = SystemTools.isMac();

		// Get all translation texts
		final String controlsTitle = Internationalization.getText("application.graph.sidebar.controls.transform.title");
		final String controlsMove = Internationalization.getText("application.graph.sidebar.controls.transform.move");
		final String controlsPick = Internationalization.getText("application.graph.sidebar.controls.transform.pick");
		final String controlsSave = Internationalization.getText("application.graph.sidebar.controls.transform.save");
		final String controlsZoom = Internationalization.getText("application.graph.sidebar.controls.transform.zoom");
		final String controlsReset = Internationalization.getText("application.graph.sidebar.controls.transform.reset");
		final String controlsLayout = Internationalization
				.getText("application.graph.sidebar.controls.transform.layout");

		// Create a titled panel in gridbag layout.
		final JPanel panel = this.getTitledPanel(controlsTitle, new GridBagLayout());

		// Create two toggle buttons (move and pick). They control the transformation mode and displayed side-by-side.
		// They both will be added to a button group.
		final JToggleButton btnMove = new JToggleButton(controlsMove);
		btnMove.addActionListener(actionChangeListener);
		btnMove.setActionCommand(Command.TRANSFORM.toString());
		btnMove.setSelected(true);
		if (isMac) {
			btnMove.putClientProperty("JButton.buttonType", "segmented");
			btnMove.putClientProperty("JButton.segmentPosition", "first");
		}

		final JToggleButton btnPick = new JToggleButton(controlsPick);
		btnPick.addActionListener(actionChangeListener);
		btnPick.setActionCommand(Command.PICK.toString());
		if (isMac) {
			btnPick.putClientProperty("JButton.buttonType", "segmented");
			btnPick.putClientProperty("JButton.segmentPosition", "last");
		}

		final ButtonGroup group = new ButtonGroup();
		group.add(btnMove);
		group.add(btnPick);

		// Create a checkbox (un)force saving the new position of a moved object in the graph. It has one label on the
		// left.
		final JCheckBox cbSavePositions = createCheckbox(controlsSave);
		cbSavePositions.setActionCommand(Command.SAVE_POSITION.toString());
		cbSavePositions.addActionListener(actionChangeListener);
		cbSavePositions.setSelected(true);
		cbSavePositions.setForeground(foregroundColor);
		cbSavePositions.setOpaque(false);

		// Create a label and a spinner for changing the current zoom.
		final JLabel lblZoom = new JLabel(controlsZoom);
		lblZoom.setForeground(foregroundColor);

		final NumberFormat percentFormat = NumberFormat.getPercentInstance();
		percentFormat.setMinimumFractionDigits(2);
		spnrZoom = new JSpinner(new SpinnerNumberModel(100f, 0, 500, 10));
		spnrZoom.addChangeListener(actionChangeListener);
		spnrZoom.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(final FocusEvent e) {}

			@Override
			public void focusLost(final FocusEvent e) {
				// update value if out of focus
				spnrZoom.setValue(spnrZoom.getValue());
			}
		});

		// Create a button reset all positions.
		final JButton btnReset = createButton(controlsReset);
		btnReset.setActionCommand(Command.RESET_POSITIONS.toString());
		btnReset.addActionListener(actionChangeListener);

		// Create a combobox display all available graph layouts.
		final JLabel lblLayouts = new JLabel(controlsLayout);
		lblLayouts.setForeground(foregroundColor);

		btnLayouts = createComboBox();
		initializeAvailableLayouts();
		btnLayouts.setActionCommand(Command.CHANGE_LAYOUT.toString());
		btnLayouts.addActionListener(actionChangeListener);
		lblLayouts.setLabelFor(btnLayouts);
		lblLayouts.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

		final GridBagConstraints row1col1 = new GridBagConstraints();
		final GridBagConstraints row1col2 = new GridBagConstraints();
		final GridBagConstraints row2 = new GridBagConstraints();
		final GridBagConstraints row3col1 = new GridBagConstraints();
		final GridBagConstraints row3col2 = new GridBagConstraints();
		final GridBagConstraints row4 = new GridBagConstraints();
		final GridBagConstraints row5col1 = new GridBagConstraints();
		final GridBagConstraints row5col2 = new GridBagConstraints();

		row1col1.gridx = 0;
		row1col1.gridy = 0;
		row1col1.anchor = GridBagConstraints.EAST;
		row1col2.gridx = GridBagConstraints.RELATIVE;
		row1col2.gridy = 0;
		row1col2.anchor = GridBagConstraints.WEST;

		row2.gridx = 0;
		row2.gridy = 1;
		row2.gridwidth = 2;
		row2.anchor = GridBagConstraints.CENTER;

		row3col1.gridx = 0;
		row3col1.gridy = 2;
		row3col2.gridx = GridBagConstraints.RELATIVE;
		row3col2.gridy = 2;

		row4.gridx = 0;
		row4.gridy = 3;
		row4.gridwidth = 2;
		row4.anchor = GridBagConstraints.NORTH;
		row4.insets = new Insets(10, 10, 10, 10);

		row5col1.gridx = 0;
		row5col1.gridy = 4;
		row5col1.anchor = GridBagConstraints.EAST;
		row5col2.gridx = GridBagConstraints.RELATIVE;
		row5col2.gridy = 4;
		row5col2.anchor = GridBagConstraints.WEST;

		panel.add(btnMove, row1col1);
		panel.add(btnPick, row1col2);
		panel.add(cbSavePositions, row2);
		panel.add(lblZoom, row3col1);
		panel.add(spnrZoom, row3col2);
		panel.add(btnReset, row4);
		panel.add(lblLayouts, row5col1);
		panel.add(btnLayouts, row5col2);

		return panel;
	}

	/**
	 * Create a button object.
	 * 
	 * @param controlsReset
	 * @return
	 */
	private JButton createButton(final String text) {
		final boolean isMac = SystemTools.isMac();
		return isMac ? HudWidgetFactory.createHudButton(text) : new JButton(text);
	}

	/**
	 * Create a checkbox object.
	 * 
	 * @param text
	 * @return
	 */
	private JCheckBox createCheckbox(final String text) {
		final boolean isMac = SystemTools.isMac();
		return isMac ? HudWidgetFactory.createHudCheckBox(text) : new JCheckBox(text);
	}

	/**
	 * Create a combo box object.
	 * 
	 * @param controlsReset
	 * @return
	 */
	private JComboBox createComboBox() {
		final boolean isMac = SystemTools.isMac();
		return isMac ? HudWidgetFactory.createHudComboBox(new DefaultComboBoxModel()) : new JComboBox();
	}

	protected JPanel getSelectionActionsPanel() {
		final String controlsTitle = Internationalization
				.getText("application.graph.sidebar.controls.selections.title");
		final String controlsChoose = Internationalization
				.getText("application.graph.sidebar.controls.selections.choose");
		final String controlsSave = Internationalization.getText("application.graph.sidebar.controls.selections.save");

		final JPanel panel = this.getTitledPanel(controlsTitle, new GridBagLayout());

		final JButton buttonChoose = createButton(controlsChoose);
		buttonChoose.addActionListener(actionChangeListener);
		buttonChoose.setActionCommand(Command.CHOOSE.toString());

		final JCheckBox cbSaveSelections = createCheckbox(controlsSave);
		cbSaveSelections.setActionCommand(Command.SAVE_SELECTION.toString());
		cbSaveSelections.addActionListener(actionChangeListener);
		cbSaveSelections.setSelected(true);
		cbSaveSelections.setForeground(foregroundColor);
		cbSaveSelections.setOpaque(false);

		final GridBagConstraints row1 = new GridBagConstraints();
		final GridBagConstraints row2 = new GridBagConstraints();

		row1.gridx = 0;
		row1.gridy = 0;
		row1.anchor = GridBagConstraints.NORTH;

		row2.gridx = 0;
		row2.gridy = 1;
		row2.anchor = GridBagConstraints.NORTH;

		panel.add(buttonChoose, row1);
		panel.add(cbSaveSelections, row2);

		return panel;
	}

	protected JPanel getExportActionsPanel() {
		final String controlsTitle = Internationalization.getText("application.graph.sidebar.controls.export.title");
		final String controlsExportImage = Internationalization
				.getText("application.graph.sidebar.controls.export.imagepng");
		final String controlsExportDot = Internationalization
				.getText("application.graph.sidebar.controls.export.graphdot");

		final JPanel panel = this.getTitledPanel(controlsTitle);

		final JButton btnExportImage = createButton(controlsExportImage);
		btnExportImage.addActionListener(actionChangeListener);
		btnExportImage.setActionCommand(Command.EXPORT_IMAGE.toString());

		final JButton btnExportDot = createButton(controlsExportDot);
		btnExportDot.addActionListener(actionChangeListener);
		btnExportDot.setActionCommand(Command.EXPORT_DOT.toString());

		final GridBagConstraints row3a = new GridBagConstraints();
		final GridBagConstraints row3b = new GridBagConstraints();

		row3a.gridx = 0;
		row3a.gridy = 2;
		row3a.anchor = GridBagConstraints.EAST;

		row3b.gridx = GridBagConstraints.RELATIVE;
		row3b.gridy = 2;
		row3b.anchor = GridBagConstraints.WEST;

		panel.add(btnExportImage, row3a);
		panel.add(btnExportDot, row3b);

		return panel;
	}

	protected JPanel getTitledPanel(final String title) {
		return this.getTitledPanel(title, new FlowLayout());
	}

	protected JPanel getTitledPanel(final String title, final LayoutManager layoutManager) {
		final JPanel panel = new JPanel(layoutManager);
		final Color color = foregroundColor;
		panel.setOpaque(false);
		final Border border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(color), title,
				TitledBorder.CENTER, TitledBorder.TOP, null, color);
		panel.setBorder(border);

		return panel;
	}

	@Override
	public void update(final Observable o, final Object arg) {
		ThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				if ((arg != null) && (arg instanceof Notification)) {
					final Notification notification = (Notification) arg;
					switch (notification) {
					case NEW_GRAPH:
						final JPanel area = getSatelliteArea();
						area.removeAll();
						area.add(data.getGraphSatelliteView());
						area.updateUI();
						break;

					case CHANGE_ZOOM:
						spnrZoom.setValue(new Double(data.getZoom()));
						spnrZoom.validate();
						break;
					}
				}
			}
		});
	}

	protected JPanel getHelpArea() {
		final JPanel panel = new JPanel();

		final JButton button = new JButton(Internationalization.getScaledIcon(
				"application.graph.sidebar.controls.help", 16));
		button.setToolTipText(Internationalization.getText("application.graph.sidebar.controls.help.tooltip"));
		button.setActionCommand(Command.INFORMATION.toString());
		button.addActionListener(actionChangeListener);

		if (SystemTools.isMac()) {
			button.setIcon(null);
			button.putClientProperty("JButton.buttonType", "help");
		}

		final JLabel label = new JLabel(button.getToolTipText());
		label.setLabelFor(button);

		panel.add(button);
		panel.add(label);

		return panel;
	}

	protected enum Command {
		TRANSFORM, PICK, SAVE_POSITION, CHANGE_ZOOM, CHOOSE, SAVE_SELECTION, RESET_POSITIONS, CHANGE_LAYOUT, EXPORT_IMAGE, EXPORT_DOT, INFORMATION, CHANGE_TABLE
	}

	protected static class ActionChangeListener implements ActionListener, ChangeListener {

		protected final AbstractViewSidebar main;

		public ActionChangeListener(final AbstractViewSidebar instance) {
			main = instance;
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			try {
				this.action(Command.valueOf(e.getActionCommand()), e.getSource());
			} catch (final Exception e1) {
				e1.printStackTrace();
			}
		}

		@Override
		public void stateChanged(final ChangeEvent e) {
			try {
				this.action(Command.CHANGE_ZOOM, e.getSource());
			} catch (final Exception e1) {
				e1.printStackTrace();
			}
		}

		/**
		 * Perform any available event actions.
		 * 
		 * @param command
		 * @param object
		 */
		protected void action(final Command command, final Object object) {
			final AbstractViewData data = main.data;

			switch (command) {
			case CHANGE_ZOOM:
				if (object instanceof JSpinner) {
					/**
					 * Following lines are just for the circumstances that the given object is not a float. E.g. it
					 * could happen, that a cast from integer to float can generate a class cast exception.
					 */
					final Object value = (((JSpinner) object).getValue());
					if (value instanceof Integer) {
						data.actionUpdateZoom(new Float((Integer) value));
					} else if (value instanceof Float) {
						data.actionUpdateZoom((Float) value);
					} else if (value instanceof Double) {
						data.actionUpdateZoom(new Double((Double) value).floatValue());
					}
				}
				break;
			case CHANGE_LAYOUT:
				if (object instanceof JComboBox) {
					final Object value = ((JComboBox) object).getSelectedItem();
					if (value instanceof LayoutType) {
						data.actionUpdateLayout((LayoutType) value);
					}
				}
				break;
			case CHANGE_TABLE:
				if (object instanceof JComboBox) {
					final Object value = ((JComboBox) object).getSelectedItem();
					if (value instanceof DatabaseTable) {
						data.actionUpdateTable((DatabaseTable) value);
					}
				}
				break;
			case CHOOSE:
				if (object instanceof JButton) {
					data.actionChooseObjects();
				}
				break;
			case PICK:
				if (object instanceof JToggleButton) {
					data.actionSetTransformMode(false);
				}
				break;
			case RESET_POSITIONS:
				if (object instanceof JButton) {
					data.actionResetPositions();
				}
				break;
			case SAVE_POSITION:
				if (object instanceof JCheckBox) {
					data.actionSetSavePositions(((JCheckBox) object).isSelected());
				}
				break;
			case SAVE_SELECTION:
				if (object instanceof JCheckBox) {
					data.actionSetSaveSelections(((JCheckBox) object).isSelected());
				}
				break;
			case TRANSFORM:
				if (object instanceof JToggleButton) {
					data.actionSetTransformMode(true);
				}
				break;
			case EXPORT_IMAGE:
				if (object instanceof JButton) {
					data.actionExportAsImage();
				}
				break;
			case EXPORT_DOT:
				if (object instanceof JButton) {
					data.actionExportAsDot();
				}
				break;
			case INFORMATION:
				ViewController.showHelpView();
				break;
			}
		}
	}

}
