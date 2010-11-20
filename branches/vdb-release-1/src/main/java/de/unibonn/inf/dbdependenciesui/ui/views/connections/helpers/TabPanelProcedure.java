package de.unibonn.inf.dbdependenciesui.ui.views.connections.helpers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.text.JTextComponent;

import org.javabuilders.BuildResult;
import org.javabuilders.swing.SwingJavaBuilder;

import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseProcedure;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.ProcedureSchema;

/**
 * @author Marc Kastleiner
 */
public class TabPanelProcedure extends JPanel {

	private static final long serialVersionUID = 1540576067694355415L;

	protected DatabaseProcedure procedure = null;

	protected final BuildResult result;

	protected JTextField ctnName;
	protected JTextField ctnType;
	protected JTextArea ctnBody;
	protected JLabel ctnUsedTables;
	protected JLabel ctnAffectedTables;

	public TabPanelProcedure(final DatabaseProcedure procedure) {
		super();
		this.procedure = procedure;
		result = SwingJavaBuilder.build(this);
		postInitialize();
		makeElementsNotEditable();
	}

	private void postInitialize() {
		if (procedure != null) {
			final ProcedureSchema schema = procedure.getProcedureSchemaObject();

			ctnName.setText(schema.getName());
			ctnType.setText(schema.getType());
			ctnBody.setRows(20);
			ctnBody.setText(schema.getBody());

			StringBuilder sb = new StringBuilder();
			buildAffectedTablesString(sb, schema.getAffectedAlteredTable(), "ALTER");
			buildAffectedTablesString(sb, schema.getAffectedDeletedTable(), "DELETE");
			buildAffectedTablesString(sb, schema.getAffectedInsertedTable(), "INSERT");
			buildAffectedTablesString(sb, schema.getAffectedUpdatedTable(), "UPDATE");
			ctnAffectedTables.setText(sb.toString());

			sb = new StringBuilder();
			for (final String table : schema.getUsedTables()) {
				if (sb.length() > 0) {
					sb.append(", ");
				}
				sb.append(table);
			}
			ctnUsedTables.setText(sb.toString());
		}
	}

	/**
	 * Append/Build at the given StringBuilder object sb a list of the affected tables. Each table will append itself a
	 * information note. E.g. TABLE1 (INSERT), TABLE4 (UPDATE), TABLE3 (DROP), TABLE4 (INSERT)
	 * 
	 * @param sb
	 * @param schema
	 * @param information
	 */
	private void buildAffectedTablesString(final StringBuilder sb, final List<String> affectedTables,
			final String information) {
		for (String table : affectedTables) {
			table = table + " (" + information + ")";
			if (sb.length() > 0) {
				sb.append(", ");
			}
			sb.append(table);
		}
	}

	protected void makeElementsNotEditable() {
		final ActionListener actionListener = new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (e.getSource() instanceof JCheckBox) {
					final JCheckBox checkbox = (JCheckBox) e.getSource();
					checkbox.setSelected(!checkbox.isSelected());
				}
			}
		};

		for (final String key : new String[] {
				"ctnName", "ctnType", "ctnBody" }) {
			final Object component = result.get(key);
			if ((component != null)) {
				if (component instanceof JCheckBox) {
					((JCheckBox) component).addActionListener(actionListener);
				} else if (component instanceof JToggleButton) {
					((JToggleButton) component).setEnabled(false);
				} else if (component instanceof JButton) {
					((JButton) component).setEnabled(false);
				} else if (component instanceof JTextComponent) {
					((JTextComponent) component).setEditable(false);
				}
			}
		}
	}
}
