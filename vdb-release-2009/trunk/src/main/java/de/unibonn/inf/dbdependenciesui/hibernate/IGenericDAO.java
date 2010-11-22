package de.unibonn.inf.dbdependenciesui.hibernate;

import java.io.Serializable;
import java.util.List;

import javax.transaction.NotSupportedException;

import org.hibernate.Session;
import org.hibernate.criterion.Criterion;

/**
 * This is the main interface of any dao object. A data access object is a design pattern used for capselung the actual
 * data access from the user. In this case, the application should not touch any hibernate relevant issues and interact
 * only with a dao or, better, the main controller. All methods are typesafe and work for their special purpose.
 * Examples:
 * <ul>
 * <li>dao.findById(1) will return the entity with the identifier (column id)</li>
 * <li>dao.findByTitle("title") will return the entity with that title)</li>
 * <li>dao.makePersistent(entity) will save/update the entity</li>
 * <li>dao.findAll() will return a list of all entities</li>
 * <li>dao.makeTransient(entity) will remove/delete the entity</li>
 * <li>dao.create() factory like creation of a new transient entity</li>
 *</ul>
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 * @param <T>
 *            Entity class
 * @param <ID>
 *            Entity's id class.
 */
public interface IGenericDAO<T, ID extends Serializable> {

	T findById(ID id, boolean lock);

	T findByTitle(String title, boolean lock);

	T findByAttributes(final boolean lock, final Criterion... criterion);

	List<T> findAll();

	List<T> findByExample(T exampleInstance) throws NotSupportedException;

	T makePersistent(T entity);

	T makePersistent(T entity, boolean flushAfterSave);

	void makeTransient(T entity);

	T create();

	void setSession(Session session);
}
