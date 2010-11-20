/**
 * $Id: Main.java 41 2009-03-01 16:21:20Z philipp $
 */
package de.unibonn.inf.dbdependenciesui.hibernate.models;

import java.util.logging.Logger;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.Transient;

import de.unibonn.inf.dbdependenciesui.Configuration;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.DdlSchema;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.DdlSchemaEditable;

/**
 * This is the hibernate object representation of a table. The field ddlschema (string) contains a xml representation of
 * {@link DdlSchema}. Use the appropiate methods {@link #getDdlSchemaObject()} and
 * {@link #setDdlSchemaObject(DdlSchema)}.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "db_tables")
@Inheritance(strategy = InheritanceType.JOINED)
public class DatabaseTable extends DatabaseObject {

	private String ddlschema;

	@Transient
	private transient DdlSchema cachedDdlschema;

	public DatabaseTable() {
		super();
	}

	/**
	 * @return the ddlschema
	 */
	public String getDdlSchema() {
		return ddlschema;
	}

	/**
	 * @param ddlSchema
	 *            the ddlschema to set
	 */
	protected void setDdlSchema(final String ddlSchema) {
		ddlschema = ddlSchema;
		this.setModified();
	}

	/**
	 * @return ddlschema
	 * @throws IllegalArgumentException
	 */
	public DdlSchema getDdlSchemaObject() throws IllegalArgumentException {
		if (cachedDdlschema == null) {
			try {
				cachedDdlschema = new DdlSchema(ddlschema);
			} catch (final Exception e) {
				cachedDdlschema = new DdlSchema();
			}
			cachedDdlschema.setConnection(getConnection());
		}
		return cachedDdlschema;
	}

	/**
	 * @return ddlschema
	 * @throws IllegalArgumentException
	 */
	public DdlSchemaEditable createDdlSchemaEditableObject() throws IllegalArgumentException {
		DdlSchemaEditable result = null;
		try {
			result = new DdlSchemaEditable(ddlschema);
		} catch (final Exception e) {
			result = new DdlSchemaEditable();
		}
		result.setConnection(getConnection());

		cachedDdlschema = result;
		return result;
	}

	/**
	 * @param ddlschema
	 *            the ddslschema to set
	 */
	public void setDdlSchemaObject(final DdlSchema ddlschema) {
		try {
			setDdlSchema(ddlschema.serialize());
			cachedDdlschema = ddlschema;
		} catch (final Exception e) {
			Logger.getLogger(Configuration.LOGGER).warning("Could not serialize ddlschema.");
		}
	}
}
