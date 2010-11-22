package de.unibonn.inf.dbdependenciesui.ui.views.common;

import javax.swing.table.DefaultTableModel;

public abstract class AbstractDatabaseObjectTableModel extends DefaultTableModel {

	private static final long serialVersionUID = -1757258655911115797L;

	protected String[] columnsHeader;

	protected Object[][] data;

	@Override
	public String getColumnName(final int col) {
		return columnsHeader[col];
	}

	@Override
	public Class<?> getColumnClass(final int columnIndex) {
		if (columnIndex == 0) {
			return String.class;
		} else if ((columnIndex == 1) || (columnIndex == 2)) {
			return Integer.class;
		} else if (columnIndex == 3) {
			return Boolean.class;
		} else {
			return super.getColumnClass(columnIndex);
		}
	}

	/**
	 * Return the current column index of the selection-boolean.
	 * 
	 * @return
	 */
	public int getBooleanColumnIndex() {
		return 3;
	}

	@Override
	public int getRowCount() {
		return data != null ? data.length : 0;
	}

	@Override
	public int getColumnCount() {
		return columnsHeader.length;
	}

	@Override
	public Object getValueAt(final int row, final int col) {
		return data[row][col];
	}

	@Override
	public boolean isCellEditable(final int row, final int col) {
		return col == 3;
	}

	@Override
	public void setValueAt(final Object value, final int row, final int col) {
		data[row][col] = value;
		fireTableCellUpdated(row, col);
	}

	@Override
	public void fireTableCellUpdated(final int row, final int column) {
		super.fireTableCellUpdated(row, column);
	}

	/**
	 * Return if the given table title is selected.
	 * 
	 * @param title
	 * @return
	 */
	// TODO abstract
	public boolean isTableSelected(final String title) {
		for (final Object[] element : data) {
			if (element[0].equals(title)) { return (Boolean) element[3]; }
		}

		return false;
	}

}
