package de.unibonn.inf.dbdependenciesui.hibernate.impl;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.logging.Logger;

import javax.transaction.NotSupportedException;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import de.unibonn.inf.dbdependenciesui.Configuration;
import de.unibonn.inf.dbdependenciesui.hibernate.IGenericDAO;

/**
 * This is the abstract, but near complete, implemention of a dao object.
 * 
 * @author Andre Kasper
 * @author Jan Philipp
 * @param <T>
 *            Entity class
 * @param <ID>
 *            Entity's id class.
 */
abstract public class AbstractGenericHibernateDAO<T, ID extends Serializable> implements IGenericDAO<T, ID> {

	protected static final Logger log = Logger.getLogger(Configuration.LOGGER);

	protected Session session;

	protected final Class<T> persistentClass;

	public AbstractGenericHibernateDAO() {
		this(null);
	}

	@SuppressWarnings("unchecked")
	public AbstractGenericHibernateDAO(final Session session) {
		this.session = session;
		// This will retrive the current FIRST annotated class parameter.
		this.persistentClass = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass())
				.getActualTypeArguments()[0];
	}

	abstract public T create();

	public void setSession(final Session session) {
		this.session = session;
	}

	public Class<T> getPersistentClass() {
		return this.persistentClass;
	}

	protected Session getSession() {
		if (this.session == null) { throw new IllegalStateException("Session has not been set on DAO before usage"); }
		return this.session;
	}

	/**
	 * Find all entities of this type. Internally, this is a standard "select * from <code>T</code>".
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<T> findAll() {
		final String query = "from " + this.getPersistentClass().getSimpleName();
		return this.getSession().createQuery(query).list();
	}

	@Override
	public List<T> findByExample(final T exampleInstance) throws NotSupportedException {
		throw new NotSupportedException();
	}

	@Override
	public T findByTitle(final String title, final boolean lock) {
		return this.findByAttributes(lock, Restrictions.like("title", title));
	}

	@Override
	public T findById(final ID id, final boolean lock) {
		// T entity;
		// if (lock) {
		// entity = (T) this.getSession().load(this.getPersistentClass(), id,
		// LockMode.UPGRADE);
		// } else {
		// entity = (T) this.getSession().load(this.getPersistentClass(), id);
		// }
		return this.findByAttributes(lock, Restrictions.like("id", id));
	}

	@Override
	public T findByAttributes(final boolean lock, final Criterion... criterion) {
		T entity;
		try {
			entity = this.findByCriteria(criterion).get(0);
		} catch (final Exception e) {
			log.fine(e.getLocalizedMessage());
			entity = null;
		}
		return entity;
	}

	/**
	 * Create a critera with criterions (restrictions) and return the result list.
	 * 
	 * @param criterion
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected List<T> findByCriteria(final Criterion... criterion) {
		final Criteria crit = this.getSession().createCriteria(this.getPersistentClass());
		for (final Criterion c : criterion) {
			crit.add(c);
		}
		return crit.list();
	}

	@Override
	public T makePersistent(final T entity) {
		return this.makePersistent(entity, true);
	}

	@Override
	public T makePersistent(final T entity, final boolean flushAfterSave) {
		this.getSession().saveOrUpdate(entity);
		if (flushAfterSave) {
			this.getSession().flush();
		}
		return entity;
	}

	@Override
	public void makeTransient(final T entity) {
		this.getSession().delete(entity);
		this.getSession().flush();
	}

}
