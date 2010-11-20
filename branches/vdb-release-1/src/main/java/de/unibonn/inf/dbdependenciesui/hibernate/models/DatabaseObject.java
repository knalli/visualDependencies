/**
 * $Id: Main.java 41 2009-03-01 16:21:20Z philipp $
 */
package de.unibonn.inf.dbdependenciesui.hibernate.models;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

/**
 * This is the hibernate object representation of a abstract database object like a table, view or trigger.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 */
@SuppressWarnings("serial")
@MappedSuperclass
public class DatabaseObject implements Serializable, Comparable<DatabaseObject> {

	/**
	 * internal identifier (primary key)
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id = 0;

	/**
	 * representation/display name
	 */
	@Column(nullable = false)
	private String title;

	/**
	 * date of creation
	 */
	@Column(nullable = false)
	private Date created;

	/**
	 * date of modification
	 */
	@Column(nullable = false)
	private Date modified;

	/**
	 * belongs-to-connection
	 */
	@ManyToOne
	private DatabaseConnection connection;

	@Transient
	private transient ViewState state = ViewState.NORMAL;

	public DatabaseObject() {
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
	 * @return the connection
	 */
	public DatabaseConnection getConnection() {
		return connection;
	}

	/**
	 * @param connection
	 *            the connection to set
	 */
	public void setConnection(final DatabaseConnection connection) {
		this.connection = connection;
		this.setModified();
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
	 * @param modified
	 *            the modified to set
	 */
	public void setModified(final Date modified) {
		this.modified = modified;
	}

	/**
	 * @return the state
	 */
	public ViewState getState() {
		return state;
	}

	/**
	 * @param state
	 *            the state to set
	 */
	public void setState(final ViewState state) {
		this.state = state;
	}

	@Override
	public String toString() {
		if ((title != null) && !title.isEmpty()) {
			return title;
		} else {
			return super.toString();
		}
	}

	@Override
	public boolean equals(final Object other) {
		if (other instanceof DatabaseObject) {
			if (title != null) {
				final DatabaseObject other2 = (DatabaseObject) other;
				return (id == other2.getId()) && title.equals(other2.getTitle());
			}
		}
		return super.equals(other);
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	public static enum ViewState {
		NORMAL, INFORMATION, DATA, TRIGGERS, CLOSED
	}

	@Override
	public int compareTo(final DatabaseObject o) {
		return getTitle().compareTo(o.getTitle());
	}
}
