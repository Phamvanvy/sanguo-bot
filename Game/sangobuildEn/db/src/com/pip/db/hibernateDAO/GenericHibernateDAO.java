package com.pip.db.hibernateDAO;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Example;

import com.pip.db.GenericDAO;

/**
 * Implements the generic CRUD data access operations using Hibernate APIs.
 * <p>
 * To write a DAO, subclass and parameterize this class with your persistent class.
 * Of course, assuming that you have a traditional 1:1 appraoch for Entity:DAO design.
 * <p>
 * You have to inject a current Hibernate <tt>Session</tt> to use a DAO. Otherwise, this
 * generic implementation will use <tt>HibernateUtil.getSessionFactory()</tt> to obtain the
 * curren <tt>Session</tt>.
 *
 * @see HibernateDAOFactory
 *
 * @author Christian Bauer
 */
public abstract class GenericHibernateDAO<T, ID extends Serializable>
        implements GenericDAO<T, ID> {

    private Class<T> persistentClass;
    

    public GenericHibernateDAO() {
        this.persistentClass = (Class<T>) ((ParameterizedType) getClass()
                                .getGenericSuperclass()).getActualTypeArguments()[0];
     }

    protected Session getSession() {
    	return HibernateUtil.getSessionFactory().getCurrentSession();
    }

    public Class<T> getPersistentClass() {
        return persistentClass;
    }

    @SuppressWarnings("unchecked")
    public T findById(ID id, boolean lock) {
        T entity;
        if (lock)
            entity = (T) getSession().load(getPersistentClass(), id, LockMode.UPGRADE);
        else
            entity = (T) getSession().load(getPersistentClass(), id);

        return entity;
    }

    @SuppressWarnings("unchecked")
    public List<T> findAll() {
        return findByCriteria();
    }

    @SuppressWarnings("unchecked")
    public List<T> findByExample(T exampleInstance, String... excludeProperty) {
        Criteria crit = getSession().createCriteria(getPersistentClass());
        Example example =  Example.create(exampleInstance);
        for (String exclude : excludeProperty) {
            example.excludeProperty(exclude);
        }
        crit.add(example);
        return crit.list();
    }

    @SuppressWarnings("unchecked")
    public T makePersistent(T entity) {
    	Transaction tx = getSession().beginTransaction();
		try {
			getSession().saveOrUpdate(entity);
			tx.commit();
			return entity;
		} catch (Exception ex) {
			tx.rollback();
			throw new RuntimeException(ex);
		}
    }

    public void makeTransient(T entity) {
    	Transaction tx = getSession().beginTransaction();
    	try{
    		getSession().delete(entity);
    		tx.commit();
    	}catch(Exception ex){
    		tx.rollback();
    		throw new RuntimeException(ex);
    	}
    }

    public void flush() {
        getSession().flush();
    }

    public void clear() {
        getSession().clear();
    }
    
    public T newEntity(T entity) {
		Transaction tx = getSession().beginTransaction();
		try {
			getSession().save(entity);
			tx.commit();
			return entity;
		} catch (Exception ex) {
			tx.rollback();
			throw new RuntimeException(ex);
		}
	}
    
    public T updateEntity(T entity) {
		Transaction tx = getSession().beginTransaction();
		try {
			getSession().update(entity);
			tx.commit();
			return entity;
		} catch (Exception ex) {
			tx.rollback();
			throw new RuntimeException(ex);
		}
	}
    
    public Object update(String hql,Object... values){
    	Transaction tx = getSession().beginTransaction();
		try {
			Query query = getSession().createQuery(hql);
			if (values != null) {
				for (int i = 0; i < values.length; i++) {
					query.setParameter(i, values[i]);
				}
			}
			Object ret = query.executeUpdate();
			tx.commit();
			return ret;
		} catch (Exception ex) {
			tx.rollback();
			throw new RuntimeException(ex);
		}
    }
    
    public Object uniqueResult(String hql,Object... values){
    	Transaction tx = getSession().beginTransaction();
		try {
			Query query = getSession().createQuery(hql);
			if (values != null) {
				for (int i = 0; i < values.length; i++) {
					query.setParameter(i, values[i]);
				}
			}
			Object ret = query.uniqueResult();
			tx.commit();
			return ret;
		} catch (Exception ex) {
			tx.rollback();
			throw new RuntimeException(ex);
		}
    }
    
    @SuppressWarnings("unchecked")
    public List list(String hql,Object... values){
    	Transaction tx = getSession().beginTransaction();
		try {
			Query query = getSession().createQuery(hql);
			if (values != null) {
				for (int i = 0; i < values.length; i++) {
					query.setParameter(i, values[i]);
				}
			}
			List ret = query.list();
			tx.commit();
			return ret;
		} catch (Exception ex) {
			tx.rollback();
			throw new RuntimeException(ex);
		}
    }
    
    @SuppressWarnings("unchecked")
    public List limitList(String hql,int begin,int count,Object... values){
    	Transaction tx = getSession().beginTransaction();
		try {
			Query query = getSession().createQuery(hql);
			if (values != null) {
				for (int i = 0; i < values.length; i++) {
					query.setParameter(i, values[i]);
				}
			}
	        query.setFirstResult(begin);
	        query.setMaxResults(count);
			List ret = query.list();
			tx.commit();
			return ret;
		} catch (Exception ex) {
			tx.rollback();
			throw new RuntimeException(ex);
		}
    }
    
    public void delete(String hql,Object... values){
    	Transaction tx = getSession().beginTransaction();
    	try{
		Query query = getSession().createQuery(hql);
		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				query.setParameter(i, values[i]);
			}
		}
		query.executeUpdate();
		tx.commit();
    	} catch (Exception ex){
    		tx.rollback();
    		throw new RuntimeException(ex);
    	}
    }
    
    public void bulkUpdate(String hql) {
		Transaction tx = getSession().beginTransaction();
		try {
			Query query = getSession().createQuery(hql);
			query.executeUpdate();
			tx.commit();
		} catch (Exception ex) {
			tx.rollback();
			throw new RuntimeException(ex);
		}
	}

    /**
     * Use this inside subclasses as a convenience method.
     */
    @SuppressWarnings("unchecked")
    protected List<T> findByCriteria(Criterion... criterion) {
        Criteria crit = getSession().createCriteria(getPersistentClass());
        for (Criterion c : criterion) {
            crit.add(c);
        }
        return crit.list();
   }

//
//	private static Log log = LogFactory.getLog(GenericHibernateDAO.class);
//
//	public static Object execute(HibernateCallback callback) throws HibernateException {
//		Session session = null;
//		Transaction tx = null;
//		try {
//			session = HibernateUtil.getSessionFactory().getCurrentSession();
//			tx = session.beginTransaction();
//			Object result = callback.doInHibernate(session);
//			tx.commit();
////			session.flush();
//			return result;
//		} catch (HibernateException e) {
//			tx.rollback();
//			log.error("GenericHibernateDAO execute throws HibernateException and rollback");
//			throw e;
//		} 
//		catch (SQLException ex) {
//			tx.rollback();
//			log.error("GenericHibernateDAO execute throws SQLException and rollback");
//			throw ex;
//		}catch (RuntimeException ex) {
//			log.error("GenericHibernateDAO execute Callback code threw application exception...");
//	        throw ex;
//		 }
//	}
//
//	// -------------------------------------------------------------------------
//	// Convenience finder methods for HQL strings
//	// -------------------------------------------------------------------------
//
//	public List find(String queryString) throws HibernateException {
//		return find(queryString, (Object[]) null);
//	}
//
//	public List find(String queryString, Object value) throws HibernateException {
//		return find(queryString, new Object[] { value });
//	}
//
//	public List find(final String queryString, final Object... values)
//			throws HibernateException {
//		return (List) execute(new HibernateCallback() {
//			public Object doInHibernate(Session session)
//					throws HibernateException {
//				Query queryObject = session.createQuery(queryString);
//				// prepareQuery(queryObject);
//				if (values != null) {
//					for (int i = 0; i < values.length; i++) {
//						queryObject.setParameter(i, values[i]);
//					}
//				}
//				return queryObject.list();
//			}
//		});
//	}
//
//	// -------------------------------------------------------------------------
//	// Convenience methods for storing individual objects
//	// -------------------------------------------------------------------------
//
//	public void lock(final Object entity, final LockMode lockMode)
//			throws HibernateException {
//		execute(new HibernateCallback() {
//			public Object doInHibernate(Session session)
//					throws HibernateException {
//				session.lock(entity, lockMode);
//				return null;
//			}
//		});
//	}
//
//	public void lock(final String entityName, final Object entity,
//			final LockMode lockMode) throws HibernateException {
//
//		execute(new HibernateCallback() {
//			public Object doInHibernate(Session session)
//					throws HibernateException {
//				session.lock(entityName, entity, lockMode);
//				return null;
//			}
//		});
//	}
//
//	public Serializable save(final Object entity) throws HibernateException {
//		return (Serializable) execute(new HibernateCallback() {
//			public Object doInHibernate(Session session)
//					throws HibernateException {
//				// checkWriteOperationAllowed(session);
//				return session.save(entity);
//			}
//		});
//	}
//
//	public Serializable save(final String entityName, final Object entity)
//			throws HibernateException {
//		return (Serializable) execute(new HibernateCallback() {
//			public Object doInHibernate(Session session)
//					throws HibernateException {
//				// checkWriteOperationAllowed(session);
//				return session.save(entityName, entity);
//			}
//		});
//	}
//
//	public void update(Object entity) throws HibernateException {
//		update(entity, null);
//	}
//
//	public void update(final Object entity, final LockMode lockMode)
//			throws HibernateException {
//		execute(new HibernateCallback() {
//			public Object doInHibernate(Session session)
//					throws HibernateException {
//				// checkWriteOperationAllowed(session);
//				session.update(entity);
//				if (lockMode != null) {
//					session.lock(entity, lockMode);
//				}
//				return null;
//			}
//		});
//	}
//
//	public void update(String entityName, Object entity) throws HibernateException {
//		update(entityName, entity, null);
//	}
//
//	public void update(final String entityName, final Object entity,
//			final LockMode lockMode) throws HibernateException {
//
//		execute(new HibernateCallback() {
//			public Object doInHibernate(Session session)
//					throws HibernateException {
//				// checkWriteOperationAllowed(session);
//				session.update(entityName, entity);
//				if (lockMode != null) {
//					session.lock(entity, lockMode);
//				}
//				return null;
//			}
//		});
//	}
//
//	public void saveOrUpdate(final Object entity) throws HibernateException {
//		execute(new HibernateCallback() {
//			public Object doInHibernate(Session session)
//					throws HibernateException {
//				// checkWriteOperationAllowed(session);
//				session.saveOrUpdate(entity);
//				return null;
//			}
//		});
//	}
//
//	public void saveOrUpdate(final String entityName, final Object entity)
//			throws HibernateException {
//		execute(new HibernateCallback() {
//			public Object doInHibernate(Session session)
//					throws HibernateException {
//				// checkWriteOperationAllowed(session);
//				session.saveOrUpdate(entityName, entity);
//				return null;
//			}
//		});
//	}
//
//	public void saveOrUpdateAll(final Collection entities) throws HibernateException {
//		execute(new HibernateCallback() {
//			public Object doInHibernate(Session session)
//					throws HibernateException {
//				// checkWriteOperationAllowed(session);
//				for (Object entity : entities) {
//					session.saveOrUpdate(entity);
//				}
//				return null;
//			}
//		});
//	}
//
//	// public void replicate(final Object entity, final ReplicationMode
//	// replicationMode)
//	// /*throws DataAccessException*/ {
//	//
//	// execute(new HibernateCallback() {
//	// public Object doInHibernate(Session session) throws HibernateException {
//	// //checkWriteOperationAllowed(session);
//	// session.replicate(entity, replicationMode);
//	// return null;
//	// }
//	// });
//	// }
//	//
//	// public void replicate(final String entityName, final Object entity, final
//	// ReplicationMode replicationMode)
//	// /*throws DataAccessException*/ {
//	//
//	// execute(new HibernateCallback() {
//	// public Object doInHibernate(Session session) throws HibernateException {
//	// //checkWriteOperationAllowed(session);
//	// session.replicate(entityName, entity, replicationMode);
//	// return null;
//	// }
//	// });
//	// }
//
//	public void persist(final Object entity) throws HibernateException {
//		execute(new HibernateCallback() {
//			public Object doInHibernate(Session session)
//					throws HibernateException {
//				// checkWriteOperationAllowed(session);
//				session.persist(entity);
//				return null;
//			}
//		});
//	}
//
//	public void persist(final String entityName, final Object entity)
//			throws HibernateException {
//		execute(new HibernateCallback() {
//			public Object doInHibernate(Session session)
//					throws HibernateException {
//				// checkWriteOperationAllowed(session);
//				session.persist(entityName, entity);
//				return null;
//			}
//		});
//	}
//
//	public Object merge(final Object entity) throws HibernateException {
//		return execute(new HibernateCallback() {
//			public Object doInHibernate(Session session)
//					throws HibernateException {
//				// checkWriteOperationAllowed(session);
//				return session.merge(entity);
//			}
//		});
//	}
//
//	public Object merge(final String entityName, final Object entity)
//			throws HibernateException {
//		return execute(new HibernateCallback() {
//			public Object doInHibernate(Session session)
//					throws HibernateException {
//				// checkWriteOperationAllowed(session);
//				return session.merge(entityName, entity);
//			}
//		});
//	}
//
//	public void delete(Object entity) throws HibernateException {
//		delete(entity, null);
//	}
//
//	public void delete(final Object entity, final LockMode lockMode)
//			throws HibernateException {
//		execute(new HibernateCallback() {
//			public Object doInHibernate(Session session)
//					throws HibernateException {
//				// checkWriteOperationAllowed(session);
//				if (lockMode != null) {
//					session.lock(entity, lockMode);
//				}
//				session.delete(entity);
//				return null;
//			}
//		});
//	}
//
//	public void delete(String entityName, Object entity) throws HibernateException {
//		delete(entityName, entity, null);
//	}
//
//	public void delete(final String entityName, final Object entity,
//			final LockMode lockMode) throws HibernateException {
//
//		execute(new HibernateCallback() {
//			public Object doInHibernate(Session session)
//					throws HibernateException {
//				// checkWriteOperationAllowed(session);
//				if (lockMode != null) {
//					session.lock(entityName, entity, lockMode);
//				}
//				session.delete(entityName, entity);
//				return null;
//			}
//		});
//	}
//
//	public void deleteAll(final Collection entities) throws HibernateException {
//		execute(new HibernateCallback() {
//			public Object doInHibernate(Session session)
//					throws HibernateException {
//				// //checkWriteOperationAllowed(session);
//				for (Object entity : entities) {
//					session.delete(entity);
//				}
//				return null;
//			}
//		});
//	}
//
//	public void Tflush() throws HibernateException {
//		execute(new HibernateCallback() {
//			public Object doInHibernate(Session session)
//					throws HibernateException {
//				session.flush();
//				return null;
//			}
//		});
//	}
//
//	public void Tclear() throws HibernateException {
//		execute(new HibernateCallback() {
//			public Object doInHibernate(Session session) {
//				session.clear();
//				return null;
//			}
//		});
//	}
}
