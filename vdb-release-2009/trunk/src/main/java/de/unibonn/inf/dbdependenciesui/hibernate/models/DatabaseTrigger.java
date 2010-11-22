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
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.TriggerSchema;
import de.unibonn.inf.dbdependenciesui.hibernate.models.helpers.TriggerSchemaEditable;

/**
 * This is the hibernate object representation of a trigger.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "db_triggers")
@Inheritance(strategy = InheritanceType.JOINED)
public class DatabaseTrigger extends DatabaseObject {

	private String triggerschema;

	@Transient
	private transient TriggerSchema cachedTriggerSchema;

	/**
	 * @return the triggerschema
	 */
	public String getTriggerSchema() {
		return triggerschema;
	}

	/**
	 * @param triggerschema
	 *            the triggerschema to set
	 */
	protected void setTriggerSchema(final String triggerschema) {
		this.triggerschema = triggerschema;
		this.setModified();
	}

	/**
	 * @return triggerschema
	 * @throws IllegalArgumentException
	 */
	public TriggerSchema getTriggerSchemaObject() throws IllegalArgumentException {
		if (cachedTriggerSchema == null) {
			try {
				cachedTriggerSchema = new TriggerSchema(triggerschema);
			} catch (final Exception e) {
				cachedTriggerSchema = new TriggerSchema();
			}
			cachedTriggerSchema.setConnection(getConnection());
		}
		return cachedTriggerSchema;
	}

	/**
	 * @return ddlschema
	 * @throws IllegalArgumentException
	 */
	public TriggerSchemaEditable createTriggerSchemaEditableObject() throws IllegalArgumentException {
		TriggerSchemaEditable result = null;
		try {
			result = new TriggerSchemaEditable(triggerschema);
		} catch (final Exception e) {
			result = new TriggerSchemaEditable();
		}
		result.setConnection(getConnection());
		cachedTriggerSchema = result;
		return result;
	}

	/**
	 * @param ddlschema
	 *            the ddslschema to set
	 */
	public void setTriggerSchemaObject(final TriggerSchema triggerschema) {
		try {
			setTriggerSchema(triggerschema.serialize());
			cachedTriggerSchema = triggerschema;
		} catch (final Exception e) {
			Logger.getLogger(Configuration.LOGGER).warning("Could not serialize ddlschema.");
		}
	}
}
