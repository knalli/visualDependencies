package de.unibonn.inf.dbdependenciesui.ui.views.connections.helpers;

import java.util.Arrays;
import java.util.List;

import javax.swing.table.DefaultTableModel;

public class ConnectionViewMainTableModel extends DefaultTableModel {

	private static final long serialVersionUID = 4773633654485710804L;

	private final String[] columnsHeader;

	private Object[][] data;

	private List<Class<?>> columnClasses = null;

	public ConnectionViewMainTableModel(final Object[][] data, final String[] columnsHeader) {
		super(data, columnsHeader);
		this.data = data;
		this.columnsHeader = columnsHeader;
	}

	public void setData(final Object[][] data) {
		this.data = data;
	}

	@Override
	public String getColumnName(final int col) {
		return columnsHeader[col];
	}

	@Override
	public int getRowCount() {
		// Count the real rows.
		int count = 0;
		if (data != null) {
			for (final Object[] element : data) {
				if (element[0] != null) {
					count++;
				} else {
					break;
				}
			}
		}
		return count;
	}

	@Override
	public Class<?> getColumnClass(final int columnIndex) {
		if (columnClasses != null) {
			if (columnClasses.size() > columnIndex) { return columnClasses.get(columnIndex); }
		}
		return super.getColumnClass(columnIndex);
	}

	public void setColumnClasses(final List<Class<?>> columnClasses) {
		this.columnClasses = columnClasses;
	}

	public void setColumnClasses(final Class<?>... columnClasses) {
		this.setColumnClasses(Arrays.asList(columnClasses));
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
		return false;
	}

	@Override
	public void setValueAt(final Object value, final int row, final int col) {
		data[row][col] = value;
		fireTableCellUpdated(row, col);
	}

}
