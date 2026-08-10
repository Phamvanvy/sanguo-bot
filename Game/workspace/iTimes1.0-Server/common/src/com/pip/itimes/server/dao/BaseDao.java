package com.pip.itimes.server.dao;

import java.util.*;
import org.hibernate.*;
import java.io.Serializable;

public class BaseDao {


    public BaseDao() {

    }

    public void makePersistent(Object o) throws DataAccessException {
        Session session = null;
        Transaction tx = null;
        try {
            session = getSession();
            tx = session.beginTransaction();
            session.saveOrUpdate(o);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw new DataAccessException(e);
        } finally {
            closeSession();
        }
    }

    public void makeTransient(Object o) throws DataAccessException {
        Session session = null;
        Transaction tx = null;
        try {
            session = getSession();
            tx = session.beginTransaction();
            session.delete(o);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw new DataAccessException(e);
        } finally {
            closeSession();
        }
    }

    public void query(String sql) throws DataAccessException {
        Session session = null;
        Transaction tx = null;
        try {
            session = getSession();
            tx = session.beginTransaction();
            session.createQuery(sql).executeUpdate();
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw new DataAccessException(e);
        } finally {
            closeSession();
        }
    }

    public Object uniqueResult(String sql) throws DataAccessException {
        try {
            return getSession().createQuery(sql).uniqueResult();
        } catch (HibernateException e) {
            throw new DataAccessException(e);
        } finally {
            closeSession();
        }
    }

    public List getList(String sql) throws DataAccessException {
        try {
            return getSession().createQuery(sql).list();
        } catch (HibernateException e) {
            throw new DataAccessException(e);
        } finally {
            closeSession();
        }
    }

    public List getLimitedList(String sql, int begin, int count) throws
            DataAccessException {
        try {
            Query query = getSession().createQuery(sql);
            query.setFirstResult(begin);
            query.setMaxResults(count);
            return query.list();
        } catch (HibernateException e) {
            throw new DataAccessException(e);
        } finally {
            closeSession();
        }
    }

    public int getCount(String sql) throws DataAccessException {
        try {
            Long ret = (Long) uniqueResult("select count(*) " + sql);
            return ret.intValue();
        } catch (DataAccessException e) {
            throw new DataAccessException(e);
        } finally {
            closeSession();
        }
    }

    public Object getObject(Class clazz, Integer id) throws DataAccessException {
        try {
            return getSession().get(clazz, id);
        } catch (HibernateException e) {
            return new DataAccessException(e);
        } finally {
            closeSession();
        }
    }

    public Session getSession() {
        return HibernateUtil.getSession();
    }

    public void closeSession() {
        HibernateUtil.closeSession();
    }


    public void evict(Class clazz,Serializable ser){
        HibernateUtil.getSessionFactory().evict(clazz,ser);
    }

}
