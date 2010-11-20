package de.unibonn.inf.dbdependenciesui;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.transaction.NotSupportedException;

import de.unibonn.inf.dbdependenciesui.controller.Controller;
import de.unibonn.inf.dbdependenciesui.graph.viewhierarchy.DatabaseGraph;
import de.unibonn.inf.dbdependenciesui.graph.viewhierarchy.DatabaseModelGraphTransformer;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseConnection;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseTrigger;
import de.unibonn.inf.dbdependenciesui.hibernate.models.DatabaseView;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.DdlSchemaEditable;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.Relation;
import de.unibonn.inf.dbdependenciesui.metadata.IMetaData;
import de.unibonn.inf.dbdependenciesui.metadata.MetaDataFactory;
import de.unibonn.inf.dbdependenciesui.metadata.MetaDataFactory.Vendor;

final public class TestFixtures {

	private TestFixtures() {
		try {
			if (Boolean.valueOf(Configuration
					.getSetting("debugconnection.enabled"))) {
				REAL_DB_CONN_HOST = Configuration
						.getSetting("debugconnection.host");
				REAL_DB_CONN_PORT = Integer.parseInt(Configuration
						.getSetting("debugconnection.port"));
				REAL_DB_CONN_USER = Configuration
						.getSetting("debugconnection.username");
				REAL_DB_CONN_PASS = Configuration
						.getSetting("debugconnection.password");
				REAL_DB_CONN_SCHM = Configuration
						.getSetting("debugconnection.schema");
				REAL_DB_ENABLED = true;
			}
		} catch (Exception e) {
			System.err.println("TestFixtures are not available.");
		}
	}

	public static  boolean REAL_DB_ENABLED = false;
	
	public static String REAL_DB_CONN_HOST = "";
	public static int REAL_DB_CONN_PORT = 1521;
	public static String REAL_DB_CONN_USER = "";
	public static String REAL_DB_CONN_PASS = "";
	public static String REAL_DB_CONN_SCHM = "";

	public static final DatabaseConnection createOfflineDatabaseModels01() {
		final DatabaseConnection connection = new DatabaseConnection();
		connection.setTitle("Connection Test #1");

		final DatabaseTable table1 = new DatabaseTable();
		table1.setTitle("TABLE_01");

		final DatabaseTable table2 = new DatabaseTable();
		table2.setTitle("TABLE_02");

		final DatabaseTable table3 = new DatabaseTable();
		table3.setTitle("TABLE_03");

		final DatabaseView view1 = new DatabaseView();
		view1.setTitle("VIEW_1");

		final DatabaseView view2 = new DatabaseView();
		view2.setTitle("VIEW_2");

		final DatabaseView view3 = new DatabaseView();
		view3.setTitle("VIEW_3");

		connection.addTable(table1);
		connection.addTable(table2);
		connection.addTable(table3);
		connection.addView(view1);
		connection.addView(view2);
		connection.addView(view3);

		DdlSchemaEditable ddlschema;
		ddlschema = table1.createDdlSchemaEditableObject();
		ddlschema.addRelation(new Relation(view1, table1, false));
		table1.setDdlSchemaObject(ddlschema);

		ddlschema = table2.createDdlSchemaEditableObject();
		ddlschema.addRelation(new Relation(view1, table2, false));
		ddlschema.addRelation(new Relation(view2, table2, false));
		table2.setDdlSchemaObject(ddlschema);

		ddlschema = table3.createDdlSchemaEditableObject();
		ddlschema.addRelation(new Relation(view1, table3, false));
		table3.setDdlSchemaObject(ddlschema);

		ddlschema = view1.createDdlSchemaEditableObject();
		ddlschema.addRelation(new Relation(view1, table1, true));
		ddlschema.addRelation(new Relation(view1, table2, true));
		ddlschema.addRelation(new Relation(view1, table3, true));
		ddlschema.addRelation(new Relation(view2, view1, false));
		ddlschema.addRelation(new Relation(view3, view1, false));
		view1.setDdlSchemaObject(ddlschema);

		ddlschema = view2.createDdlSchemaEditableObject();
		ddlschema.addRelation(new Relation(view2, view1, true));
		ddlschema.addRelation(new Relation(view2, table2, true));
		ddlschema.addRelation(new Relation(view3, view2, false));
		view2.setDdlSchemaObject(ddlschema);

		ddlschema = view3.createDdlSchemaEditableObject();
		ddlschema.addRelation(new Relation(view3, view2, true));
		ddlschema.addRelation(new Relation(view3, view1, true));
		view3.setDdlSchemaObject(ddlschema);

		return connection;
	}

	public static final DatabaseConnection createOnlineDatabaseModels02()
			throws NotSupportedException {
		
		if (!REAL_DB_ENABLED) {
			throw new NotSupportedException(
					"Debug connection is not available.");
		}
		
		DatabaseConnection connection;
		final String title = "Connection Test #2";

		connection = new DatabaseConnection();
		connection.setTitle(title);
		connection.setHost(TestFixtures.REAL_DB_CONN_HOST);
		connection.setDatabase(TestFixtures.REAL_DB_CONN_SCHM);
		connection.setPort(TestFixtures.REAL_DB_CONN_PORT);
		connection.setUsername(TestFixtures.REAL_DB_CONN_USER);
		connection.setPassword(TestFixtures.REAL_DB_CONN_PASS);
		connection.setSysdba(false);
		Controller.updateConnection(connection, false);

		final IMetaData metaData = MetaDataFactory.create(Vendor.ORACLE);

		metaData.setReplaceMode(true);
		metaData.setDatabaseConnection(connection);

		metaData.connect();
		metaData.initialize();
		metaData.analyze();
		metaData.parseViews();
		Controller.updateConnection(connection, true);

		return connection;
	}

	public static final DatabaseConnection createOfflineDatabaseModels02()
			throws IOException {
		final String title = "Connection Test #2 (Offline)";

		final DatabaseConnection conn = TestFixtures
				.readDatabaseConnection("Connection Test #2");
		if (conn == null) {
			throw new IOException("Null object");
		}

		final DatabaseConnection connection = new DatabaseConnection();
		connection.setTitle(title);
		connection.setHost(conn.getHost());
		connection.setDatabase(conn.getDatabase());
		connection.setPort(conn.getPort());
		connection.setUsername(conn.getUsername());
		connection.setPassword(conn.getPassword());
		connection.setPropertiesObject(conn.getPropertiesObject());
		connection.setSchema(conn.getSchema());
		connection.setVendor(conn.getVendor());
		connection.setSysdba(false);
		Controller.updateConnection(connection, false);

		for (final DatabaseTable table : conn.getTables()) {
			final DatabaseTable table2 = new DatabaseTable();
			table2.setConnection(connection);
			table2.setDdlSchemaObject(table.getDdlSchemaObject());
			table2.setTitle(table.getTitle());
			connection.addTable(table2);
		}

		for (final DatabaseView view : conn.getViews()) {
			final DatabaseView view2 = new DatabaseView();
			view2.setConnection(connection);
			view2.setDdlSchemaObject(view.getDdlSchemaObject());
			view2.setTitle(view.getTitle());
			view2.setMaterialized(view.isMaterialized());
			view2.setSelectStatement(view.getSelectStatement());
			connection.addView(view2);
		}

		for (final DatabaseTrigger trigger : conn.getTriggers()) {
			final DatabaseTrigger trigger2 = new DatabaseTrigger();
			trigger2.setConnection(connection);
			trigger2.setTriggerSchemaObject(trigger.getTriggerSchemaObject());
			trigger2.setTitle(trigger.getTitle());
			connection.addTrigger(trigger2);
		}

		Controller.updateConnection(connection, true);

		return connection;
	}

	public static final DatabaseGraph createOnlineDatabaseGraph01() {
		return new DatabaseModelGraphTransformer(
				TestFixtures.createOfflineDatabaseModels01()).getGraph();
	}

	public static final DatabaseGraph createOnlineDatabaseGraph02()
			throws NotSupportedException {
		return new DatabaseModelGraphTransformer(
				TestFixtures.createOnlineDatabaseModels02()).getGraph();
	}

	public static void writeDatabaseConnection(
			final DatabaseConnection connection) {
		final String title = connection.getTitle().replaceAll("\\s", "_")
				.replaceAll("[\\/:*?\"<>|\\#]", "");
		final String fileName = "src/resources/" + title + ".ser";
		writeObject(fileName, connection);
	}

	public static DatabaseConnection readDatabaseConnection(String title) {
		title = title.replaceAll("\\s", "_").replaceAll("[\\/:*?\"<>|\\#]", "");
		final String fileName = "resources/" + title + ".ser";
		return TestFixtures.<DatabaseConnection> readObject(fileName);
	}

	@SuppressWarnings("unchecked")
	private static <T> T readObject(final String fileName) {
		T object = null;
		InputStream is = null;
		ObjectInputStream in = null;
		try {
			is = Main.class.getClassLoader().getResourceAsStream(fileName);
			in = new ObjectInputStream(is);
			object = (T) in.readObject();
			in.close();
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return object;
	}

	private static void writeObject(final String fileName, final Object object) {
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try {
			fos = new FileOutputStream(fileName);
			out = new ObjectOutputStream(fos);
			out.writeObject(object);
			out.close();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
}
