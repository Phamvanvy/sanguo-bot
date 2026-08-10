package com.pip.server.auth.dao;

import org.hibernate.*;
import org.hibernate.cfg.*;
import org.apache.log4j.Logger;

/**
 * 数据库工具类，提供数据库访问的快捷方法。
 */
public class HibernateUtil {

    public static Logger log = Logger.getLogger(HibernateUtil.class);

    private static Configuration configuration = null;
    private static SessionFactory sessionFactory = null;
    private static final ThreadLocal threadSession = new ThreadLocal();
    private static final ThreadLocal threadTransaction = new ThreadLocal();

    static {
        try {

            configuration = new Configuration();
            sessionFactory = configuration.configure().buildSessionFactory();
        } catch (Throwable ex) {
            log.error("Building SessionFactory failed.", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }


    public static Configuration getConfiguration() {
        return configuration;
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static Session getSession() {
        Session s = (Session) threadSession.get();

        if (s == null) {
            log.debug("Opening new Session for this thread.");

            s = sessionFactory.openSession();

            threadSession.set(s);
        }

        return s;
    }

    public static void closeSession() {
        Session s = (Session) threadSession.get();
        threadSession.set(null);
        if (s != null && s.isOpen()) {
            log.debug("Closing Session of this thread.");
            s.close();
        }
    }

    public static void beginTransaction() {
        Transaction tx = (Transaction) threadTransaction.get();
        if (tx == null) {
            log.debug("Starting new database transaction in this thread.");
            tx = getSession().beginTransaction();
            threadTransaction.set(tx);
        }
    }

    public static void commitTransaction() {
        Transaction tx = (Transaction) threadTransaction.get();
        try {
            if (tx != null && !tx.wasCommitted() && !tx.wasRolledBack()) {
                log.debug("Committing database transaction of this thread.");
                tx.commit();
            }
            threadTransaction.set(null);
        } catch (HibernateException ex) {
            rollbackTransaction();
        }
    }

    public static void rollbackTransaction() {
        Transaction tx = (Transaction) threadTransaction.get();
        try {
            threadTransaction.set(null);
            if (tx != null && !tx.wasCommitted() && !tx.wasRolledBack()) {
                log.debug(
                        "Tyring to rollback database transaction of this thread.");
                tx.rollback();
            }
        } catch (HibernateException ex) {

        } finally {
            closeSession();
        }
    }

}
