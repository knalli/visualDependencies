/**
 * $Id: Main.java 41 2009-03-01 16:21:20Z philipp $
 */
package de.unibonn.inf.dbdependenciesui.metadata;

import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseConnection;
import de.unibonn.inf.dbdependenciesui.metadata.impl.mysql5.MySqlAnalyzerImpl;
import de.unibonn.inf.dbdependenciesui.metadata.impl.mysql5.MySqlMetaDataImpl;
import de.unibonn.inf.dbdependenciesui.metadata.impl.mysql5.MySqlProcedureSqlParserImpl;
import de.unibonn.inf.dbdependenciesui.metadata.impl.mysql5.MySqlTriggerSqlParserImpl;
import de.unibonn.inf.dbdependenciesui.metadata.impl.mysql5.MySqlViewSqlParserImpl;
import de.unibonn.inf.dbdependenciesui.metadata.impl.oracle10.Oracle10AnalyzerImpl;
import de.unibonn.inf.dbdependenciesui.metadata.impl.oracle10.Oracle10MetaDataImpl;
import de.unibonn.inf.dbdependenciesui.metadata.impl.oracle10.Oracle10ProcedureSqlParserImpl;
import de.unibonn.inf.dbdependenciesui.metadata.impl.oracle10.Oracle10TriggerSqlParserImpl;
import de.unibonn.inf.dbdependenciesui.metadata.impl.oracle10.Oracle10ViewSqlParserImpl;
import de.unibonn.inf.dbdependenciesui.metadata.impl.oracle11.Oracle11AnalyzerImpl;
import de.unibonn.inf.dbdependenciesui.metadata.impl.oracle11.Oracle11MetaDataImpl;
import de.unibonn.inf.dbdependenciesui.metadata.impl.oracle11.Oracle11ProcedureSqlParserImpl;
import de.unibonn.inf.dbdependenciesui.metadata.impl.oracle11.Oracle11TriggerSqlParserImpl;
import de.unibonn.inf.dbdependenciesui.metadata.impl.oracle11.Oracle11ViewSqlParserImpl;
import de.unibonn.inf.dbdependenciesui.metadata.progresstask.Task;

/**
 * This is the factory for metadata implementations. It provides a metadata, analyzer, view parser and trigger parser
 * for each vendor. At least it can create a meta data task - e.g. used in a progress monitor.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
public class MetaDataFactory {

	private MetaDataFactory() {}

	/**
	 * Create and return a new meta data implementation for the given vendor type.
	 * 
	 * @param vendor
	 * @return
	 */
	public static IMetaData create(final Vendor vendor) {
		switch (vendor) {
		case ORACLE:
			return new Oracle11MetaDataImpl();
		case MYSQL:
			return new MySqlMetaDataImpl();
		case ORACLE10:
			return new Oracle10MetaDataImpl();
		}
		throw new NoClassDefFoundError();
	}

	/**
	 * Create and return a sql connection builder.
	 * 
	 * @param vendor
	 * @return
	 */
	public static IConnectionBuilder getConnectionBuilder(final Vendor vendor) {
		switch (vendor) {
		case ORACLE:
			return new Oracle11MetaDataImpl().getConnectionBuilder();
		case MYSQL:
			return new MySqlMetaDataImpl().getConnectionBuilder();
		case ORACLE10:
			return new Oracle10MetaDataImpl().getConnectionBuilder();
		}
		throw new NoClassDefFoundError();
	}

	/**
	 * Create and return a new analyzer implemenation for the given vendor type.
	 * 
	 * @param vendor
	 * @return
	 */
	public static IAnalyzer createAnalyzer(final Vendor vendor) {
		switch (vendor) {
		case ORACLE:
			return new Oracle11AnalyzerImpl();
		case MYSQL:
			return new MySqlAnalyzerImpl();
		case ORACLE10:
			return new Oracle10AnalyzerImpl();
		}
		throw new NoClassDefFoundError();
	}

	/**
	 * Create and return a new view parser implementation for the given vendor type.
	 * 
	 * @param vendor
	 * @return
	 */
	public static IViewSqlParser createViewSqlParser(final Vendor vendor) {
		switch (vendor) {
		case ORACLE:
			return new Oracle11ViewSqlParserImpl();
		case MYSQL:
			return new MySqlViewSqlParserImpl();
		case ORACLE10:
			return new Oracle10ViewSqlParserImpl();
		}
		throw new NoClassDefFoundError();
	}

	/**
	 * Create and return a new trigger parser implementation for the given vendor type.
	 * 
	 * @param vendor
	 * @return
	 */
	public static ITriggerSqlParser createTriggerSqlParser(final Vendor vendor) {
		switch (vendor) {
		case ORACLE:
			return new Oracle11TriggerSqlParserImpl();
		case MYSQL:
			return new MySqlTriggerSqlParserImpl();
		case ORACLE10:
			return new Oracle10TriggerSqlParserImpl();
		}
		return null;
	}

	/**
	 * Create and return a new procedure parser implementation for the given vendor type.
	 * 
	 * @param vendor
	 * @return
	 */
	public static IProcedureSqlParser createProcedureSqlParser(final Vendor vendor) {
		switch (vendor) {
		case ORACLE:
			return new Oracle11ProcedureSqlParserImpl();
		case MYSQL:
			return new MySqlProcedureSqlParserImpl();
		case ORACLE10:
			return new Oracle10ProcedureSqlParserImpl();
		}
		return null;
	}

	/**
	 * Create and return a new meta data task.
	 * 
	 * @param metaData
	 * @param connection
	 * @return
	 */
	public static Task createTask(final IMetaData metaData, final DatabaseConnection connection) {
		return new Task(metaData, connection);
	}

	/**
	 * Create a vendor specific url. Example for Oracle Thin client: domain.tld, 1521, dbase result is:
	 * jdbc:oracle:thin:@domain.tld:1521:dbase
	 * 
	 * @param vendor
	 * @param host
	 * @param port
	 * @param database
	 * @return
	 */
	public static String createUrl(final Vendor vendor, final String host, final int port, final String database) {
		switch (vendor) {
		case ORACLE:
			return new Oracle11MetaDataImpl().createUrl(host, port, database);
		case ORACLE10:
			return new Oracle10MetaDataImpl().createUrl(host, port, database);
		case MYSQL:
			return new MySqlMetaDataImpl().createUrl(host, port, database);
		}
		return null;
	}

	/**
	 * Definition of supported database vendors.
	 */
	public static enum Vendor {
		ORACLE, ORACLE10, MYSQL;

		@Override
		public String toString() {
			switch (this) {
			case ORACLE:
				return "Oracle 11g";
			case ORACLE10:
				return "Oracle 9i/10g";
			case MYSQL:
				return "MySQL";
			default:
				return "Unknown";
			}
		}
	}
}
