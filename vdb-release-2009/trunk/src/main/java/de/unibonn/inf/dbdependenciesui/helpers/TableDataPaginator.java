package de.unibonn.inf.dbdependenciesui.helpers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import de.unibonn.inf.dbdependenciesui.Configuration;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseConnection;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;
import de.unibonn.inf.dbdependenciesui.metadata.MetaDataFactory;

/**
 * This class supports the scrolling/browsing the table data. Each page has a specified amount of rows defined with
 * rowsPerPage.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class TableDataPaginator {
	private static final Logger log = Logger.getLogger(Configuration.LOGGER);

	private Connection connection;
	private final DatabaseTable table;
	private Statement statement;
	private ResultSet resultSet;
	private ResultSetMetaData metadata;
	private int columnCount;

	private int rowsPerPage = 200;
	private int insertedRows = 0;

	private Object[][] data;

	private boolean hasNext;

	private int currentPage;

	private String[] columnsHeader;

	public TableDataPaginator(final DatabaseConnection databaseConnection, final DatabaseTable table) {
		try {
			connection = MetaDataFactory.getConnectionBuilder(databaseConnection.getVendor()).createConnection(
					databaseConnection);
		} catch (final SQLException e) {
			connection = null;
		}
		this.table = table;
	}

	/**
	 * Define how many rows per page will loaded and displayed.
	 * 
	 * @param value
	 */
	public void setRowsPerPage(final int value) {
		rowsPerPage = value;
	}

	/**
	 * Open the table with a standard select-query and fetch meta data information about column headers and size. This
	 * will change the columns header object.
	 * 
	 * @throws SQLException
	 */
	public void openTable() throws SQLException {
		if (connection == null) { throw new SQLException("Connection is null"); }
		final String query = String.format("SELECT * FROM %s", table.getTitle());

		statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
		resultSet = statement.executeQuery(query);
		metadata = resultSet.getMetaData();
		columnCount = metadata.getColumnCount();

		columnsHeader = new String[columnCount];
		for (int i = 0; i < columnCount; i++) {
			columnsHeader[i] = metadata.getColumnName((i + 1));
		}
	}

	/**
	 * Close the connection.
	 */
	public void closeTable() {
		try {
			resultSet.close();
		} catch (final SQLException e) {}
		try {
			statement.close();
		} catch (final SQLException e) {}
	}

	/**
	 * Load table rows according given page. The techniqual details are like a standard sql query LIMIT x OFFSET y when
	 * x is rowsPerPage and y is page*rowsperPage. This will replace the data-object.
	 * 
	 * @param page
	 * @throws SQLException
	 */
	public void loadTableRowsByPage(final int page) throws SQLException {
		if (connection == null) { throw new SQLException("Connection is null"); }

		if (page == 1) {
			resultSet.beforeFirst();
		} else {
			resultSet.absolute(rowsPerPage * (page - 1));
		}

		currentPage = page;

		insertedRows = 0;
		data = new Object[rowsPerPage][columnCount];

		while ((insertedRows < rowsPerPage) && resultSet.next()) {
			for (int i = 0; i < columnCount; i++) {
				try {
					data[insertedRows][i] = resultSet.getString((i + 1));
				} catch (final Exception e) {
					log.warning("Problem occured while set data of " + (i + 1));
				}
			}
			insertedRows++;
		}
		hasNext = !resultSet.isAfterLast();
	}

	/**
	 * Return the latest data object.
	 * 
	 * @return
	 */
	public Object[][] getData() {
		return data;
	}

	/**
	 * Return the columns header object. Affected by {@link #openTable()}.
	 * 
	 * @return
	 */
	public String[] getColumnsHeader() {
		return columnsHeader;
	}

	/**
	 * Return if the current page is the first one.
	 * 
	 * @return
	 */
	public boolean isFirstPage() {
		return currentPage == 1;
	}

	/**
	 * Return if the current page is the last one.
	 * 
	 * @return
	 */
	public boolean isLastPage() {
		return !hasNext;
	}

	/**
	 * Return the current page.
	 * 
	 * @return
	 */
	public int getCurrentPage() {
		return currentPage;
	}
}
