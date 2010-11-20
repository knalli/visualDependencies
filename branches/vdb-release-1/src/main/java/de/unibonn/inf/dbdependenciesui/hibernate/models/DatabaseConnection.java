/**
 * $Id: Main.java 41 2009-03-01 16:21:20Z philipp $
 */
package de.unibonn.inf.dbdependenciesui.hibernate.models;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import de.unibonn.inf.dbdependenciesui.Configuration;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.ConnectionProperties;
import de.unibonn.inf.dbdependenciesui.metadata.MetaDataFactory.Vendor;

/**
 * This is the hibernate object representation of a connection. Each connection has only one set of tables. Note that a
 * view is also a special table. The two lists {@link #getTables()} and {@link #getViews()} will generated on demand but
 * are not real in the internal database table structure. The field properties (string) contains a xml representation of
 * {@link ConnectionProperties}. Use the appropiate methods {@link #getPropertiesObject()} and
 * {@link #setPropertiesObject(ConnectionProperties)}.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "db_connections")
public class DatabaseConnection implements Serializable {
	/**
	 * internal identifier (primary key)
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id = -1;

	/**
	 * representation/display name
	 */
	@Column(nullable = false)
	private String title;

	private String host;

	private int port;

	private String username;

	private String password;

	private String database;

	private String schema;

	private String properties;

	private Boolean isSysdba;

	@Enumerated(EnumType.STRING)
	private Vendor vendor;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@OrderBy(value = "title ASC")
	private Set<DatabaseTable> tables;

	@Transient
	private Set<DatabaseTable> listTables;

	@Transient
	private Set<DatabaseView> listViews;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@OrderBy(value = "title ASC")
	private Set<DatabaseTrigger> triggers;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@OrderBy(value = "title ASC")
	private Set<DatabaseProcedure> procedures;

	/**
	 * date of creation
	 */
	private Date created;

	/**
	 * date of modification
	 */
	private Date modified;

	public DatabaseConnection() {
		setCreated(new Date());
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	@SuppressWarnings("unused")
	private void setId(final int id) {
		this.id = id;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle(final String title) {
		this.title = title;
		this.setModified();
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param host
	 *            the host to set
	 */
	public void setHost(final String host) {
		this.host = host;
		this.setModified();
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port
	 *            the port to set
	 */
	public void setPort(final int port) {
		this.port = port;
		this.setModified();
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username
	 *            the username to set
	 */
	public void setUsername(final String username) {
		this.username = username;
		this.setModified();
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(final String password) {
		this.password = password;
		this.setModified();
	}

	/**
	 * @return the database
	 */
	public String getDatabase() {
		return database;
	}

	/**
	 * @param database
	 *            the database to set
	 */
	public void setDatabase(final String database) {
		this.database = database;
		this.setModified();
	}

	/**
	 * @return the schema
	 */
	public String getSchema() {
		return schema;
	}

	/**
	 * @param schema
	 *            the schema to set
	 */
	public void setSchema(final String schema) {
		this.schema = schema;
		this.setModified();
	}

	/**
	 * @return the properties
	 */
	public String getProperties() {
		return properties;
	}

	/**
	 * @param properties
	 *            the properties to set
	 */
	public void setProperties(final String properties) {
		this.properties = properties;
	}

	/**
	 * @return the created
	 */
	public Date getCreated() {
		return created;
	}

	/**
	 * @param created
	 *            the created to set
	 */
	private void setCreated(final Date created) {
		this.created = created;
		this.setModified();
	}

	/**
	 * @return the modified
	 */
	public Date getModified() {
		return modified;
	}

	public void setModified() {
		modified = new Date();
	}

	/**
	 * @return boolean is sysdba
	 */
	public boolean getSysdba() {
		return (isSysdba == null) ? false : isSysdba;
	}

	/**
	 * @param isSysdba
	 *            sysdba to set
	 */
	public void setSysdba(final boolean isSysdba) {
		this.isSysdba = isSysdba;
		this.setModified();
	}

	/**
	 * @param modified
	 *            the modified to set
	 */
	public void setModified(final Date modified) {
		this.modified = modified;
	}

	private void initializeTables() {
		// If tables is still empty, there is either a table nor a view.
		if (tables == null) {
			tables = new HashSet<DatabaseTable>();
			listTables = new HashSet<DatabaseTable>();
			listViews = new HashSet<DatabaseView>();
		} else {
			listTables = new TreeSet<DatabaseTable>();
			listViews = new TreeSet<DatabaseView>();

			// If contains data, it has to split off.
			for (final DatabaseTable table : tables) {
				if (table instanceof DatabaseView) {
					listViews.add((DatabaseView) table);
				} else {
					listTables.add(table);
				}
			}
		}
	}

	/**
	 * @return the tables
	 */
	public Set<DatabaseTable> getTables() {
		initializeTables();

		return listTables;
	}

	/**
	 * @param tables
	 *            the tables to set
	 */
	@SuppressWarnings("unused")
	private void setTables(final Set<DatabaseTable> tables) {
		this.tables = tables;
		listTables = null;
		listViews = null;
		this.setModified();
	}

	/**
	 * @param table
	 *            the table to add
	 */
	public void addTable(final DatabaseTable table) {
		initializeTables();

		tables.add(table);
		listTables.add(table);
		this.setModified();
	}

	/**
	 * @param table
	 *            the table to remove
	 */
	public void removeTable(final DatabaseTable table) {
		initializeTables();

		tables.remove(table);
		listTables.remove(table);
		this.setModified();
	}

	/**
	 * @return the views
	 */
	public Set<DatabaseView> getViews() {
		initializeTables();

		return listViews;
	}

	/**
	 * @param view
	 *            the view to add
	 */
	public void addView(final DatabaseView view) {
		initializeTables();

		tables.add(view);
		listViews.add(view);
		this.setModified();
	}

	/**
	 * @param view
	 *            the view to remove
	 */
	public void removeView(final DatabaseView view) {
		initializeTables();

		tables.remove(view);
		listViews.remove(view);
		this.setModified();
	}

	/**
	 * @return the triggers
	 */
	public Set<DatabaseTrigger> getTriggers() {
		if (triggers == null) {
			triggers = new HashSet<DatabaseTrigger>();
		}
		return triggers;
	}

	/**
	 * @return the procedures
	 */
	public Set<DatabaseProcedure> getProcedures() {
		if (procedures == null) {
			procedures = new HashSet<DatabaseProcedure>();
		}
		return procedures;
	}

	/**
	 * @param triggers
	 *            the triggers to set
	 */
	@SuppressWarnings("unused")
	private void setTriggers(final Set<DatabaseTrigger> triggers) {
		this.triggers = triggers;
		this.setModified();
	}

	/**
	 * @param procedures
	 *            the procedures to set
	 */
	@SuppressWarnings("unused")
	private void setProcedures(final Set<DatabaseProcedure> procedures) {
		this.procedures = procedures;
		this.setModified();
	}

	/**
	 * @param table
	 *            the table to add
	 */
	public void addTrigger(final DatabaseTrigger trigger) {
		if (triggers == null) {
			triggers = new HashSet<DatabaseTrigger>();
		}
		triggers.add(trigger);
		this.setModified();
	}

	/**
	 * @param table
	 *            the table to add
	 */
	public void addProcedure(final DatabaseProcedure procedure) {
		if (procedures == null) {
			procedures = new HashSet<DatabaseProcedure>();
		}
		procedures.add(procedure);
		this.setModified();
	}

	/**
	 * @param table
	 *            the table to remove
	 */
	public void removeTrigger(final DatabaseTrigger trigger) {
		if (triggers == null) {
			triggers = new HashSet<DatabaseTrigger>();
		}
		triggers.remove(trigger);
		this.setModified();
	}

	/**
	 * @param table
	 *            the table to remove
	 */
	public void removeProcedure(final DatabaseProcedure procedure) {
		if (procedures == null) {
			procedures = new HashSet<DatabaseProcedure>();
		}
		procedures.remove(procedure);
		this.setModified();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof DatabaseConnection) {
			if (title != null) { return (id == ((DatabaseConnection) obj).getId())
					&& title.equals(((DatabaseConnection) obj).getTitle()); }
		}
		return super.equals(obj);
	}

	/**
	 * Removes all relational objects.
	 */
	public void clearObjects() {
		// Load tables & triggers.
		getTables();
		getTriggers();
		getProcedures();

		// Remove all elements.
		listTables.clear();
		listViews.clear();

		tables.clear();
		triggers.clear();
		procedures.clear();
	}

	public ConnectionProperties getPropertiesObject() {
		try {
			final ConnectionProperties result = new ConnectionProperties(properties);
			return result;
		} catch (final Exception e) {
			return new ConnectionProperties();
		}
	}

	/**
	 * @param properties
	 *            the properties to set
	 */
	public void setPropertiesObject(final ConnectionProperties properties) {
		try {
			setProperties(properties.serialize());
		} catch (final Exception e) {
			Logger.getLogger(Configuration.LOGGER).warning("Could not serialize properties.");
		}
	}

	/**
	 * @return the vendor
	 */
	public Vendor getVendor() {
		return vendor;
	}

	/**
	 * @param vendor
	 *            the vendor to set
	 */
	public void setVendor(final Vendor vendor) {
		this.vendor = vendor;
	}

	/**
	 * Get a child table by title.
	 * 
	 * @param title
	 * @return
	 */
	public DatabaseTable getTableByTitle(final String title) {
		initializeTables();

		for (final DatabaseTable table : getTables()) {
			if (table.getTitle().equals(title)) { return table; }
		}

		return null;
	}

	/**
	 * Get a child view by title.
	 * 
	 * @param title
	 * @return
	 */
	public DatabaseView getViewByTitle(final String title) {
		initializeTables();

		for (final DatabaseView view : getViews()) {
			if (view.getTitle().equals(title)) { return view; }
		}

		return null;
	}

	@Override
	public String toString() {
		return title;
	}
}
