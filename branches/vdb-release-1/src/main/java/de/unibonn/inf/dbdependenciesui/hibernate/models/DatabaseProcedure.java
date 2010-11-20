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
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.ProcedureSchema;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.ProcedureSchemaEditable;

/**
 * This is the hibernate object representation of a trigger.
 * 
 * @author Marc Kastleiner
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "db_procedures")
@Inheritance(strategy = InheritanceType.JOINED)
public class DatabaseProcedure extends DatabaseObject {

	private String procedureSchema;

	@Transient
	private transient ProcedureSchema cachedProcedureSchema;

	/**
	 * @return the procedureSchema
	 */
	public String getProcedureSchema() {
		return procedureSchema;
	}

	/**
	 * @param procedureSchema
	 *            the procedureSchema to set
	 */
	protected void setProcedureSchema(final String procedureSchema) {
		this.procedureSchema = procedureSchema;
		this.setModified();
	}

	/**
	 * @return procedureSchema
	 * @throws IllegalArgumentException
	 */
	public ProcedureSchema getProcedureSchemaObject() throws IllegalArgumentException {
		if (cachedProcedureSchema == null) {
			try {
				cachedProcedureSchema = new ProcedureSchema(procedureSchema);
			} catch (final Exception e) {
				cachedProcedureSchema = new ProcedureSchema();
			}
			cachedProcedureSchema.setConnection(getConnection());
		}
		return cachedProcedureSchema;
	}

	/**
	 * @return ddlschema
	 * @throws IllegalArgumentException
	 */
	public ProcedureSchemaEditable createProcedureSchemaEditableObject() throws IllegalArgumentException {
		ProcedureSchemaEditable result = null;
		try {
			result = new ProcedureSchemaEditable(procedureSchema);
		} catch (final Exception e) {
			result = new ProcedureSchemaEditable();
		}
		result.setConnection(getConnection());
		cachedProcedureSchema = result;
		return result;
	}

	/**
	 * @param ddlschema
	 *            the ddlschema to set
	 */
	public void setProcedureSchemaObject(final ProcedureSchema procedureSchema) {
		try {
			setProcedureSchema(procedureSchema.serialize());
			cachedProcedureSchema = procedureSchema;
		} catch (final Exception e) {
			Logger.getLogger(Configuration.LOGGER).warning("Could not serialize ddlschema.");
		}
	}
}
