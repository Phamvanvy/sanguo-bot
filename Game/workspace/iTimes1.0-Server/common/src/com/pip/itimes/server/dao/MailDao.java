package com.pip.itimes.server.dao;

import java.util.*;

import com.pip.itimes.server.bean.Mail;
import org.hibernate.HibernateException;
import org.hibernate.Query;

public class MailDao extends BaseDao {
    public MailDao() {
        super();
    }

    public void addMail(Mail mail) throws DataAccessException {
        makePersistent(mail);
    }

    public void deleteMail(Mail mail) throws DataAccessException {
        makeTransient(mail);
    }

    public void deleteMail(int id) throws DataAccessException {
        query("delete Mail m where m.id=" + id);
    }

    public void deleteMail(int destId,Date time) throws DataAccessException{
        Query query = getSession().createQuery("delete Mail m where m.destId="+destId+" and m.postTime<:time");
        query.setTimestamp("time",time);
        query.executeUpdate();
    }

    public Mail getMail(int id) throws DataAccessException {
        return (Mail) getObject(Mail.class, new Integer(id));
    }

    public int getMailCount(int userId) throws DataAccessException {
        try {
            Query query = getSession().createQuery(
                    "select count(*) from Mail m where m.destId=:userId and m.postTime<:time");
            query.setInteger("userId", userId);
            query.setTimestamp("time", new Date());
            Long l = (Long) query.uniqueResult();
            return l.intValue();
        } catch (HibernateException ex) {
            return 0;
        }
        finally{
            closeSession();
        }
    }
    public int getMailCount(int userId, int minId) throws DataAccessException {
        try {
            Query query = getSession().createQuery(
                    "select count(*) from Mail m where m.destId=:userId and m.id>:minId");
            query.setInteger("userId", userId);
            query.setInteger("minId", minId);
            Long l = (Long) query.uniqueResult();
            return l.intValue();
        } catch (HibernateException ex) {
            return 0;
        }
        finally{
            closeSession();
        }
    }

    public int getMailCount(int userId,int[] blackList) throws DataAccessException {
        try {
            Query query = getSession().createQuery(
                    "select count(*) from Mail m where m.destId=:userId and m.postTime<:time"+getBlackListString(blackList));
            query.setInteger("userId", userId);
            query.setTimestamp("time", new Date());
            Long l = (Long) query.uniqueResult();
            return l.intValue();
        } catch (HibernateException ex) {
            return 0;
        }
        finally{
            closeSession();
        }
    }

    public List getMailList(int userId, int begin, int count) throws
            DataAccessException {
        try {
            Query query = getSession().createQuery(
                    "from Mail m where m.destId=:userId and m.postTime<:time order by m.postTime desc");
            query.setInteger("userId", userId);
            query.setTimestamp("time", new Date());
            query.setFirstResult(begin);
            query.setMaxResults(count);
            return query.list();
        } catch (HibernateException ex) {
            return new ArrayList();
        } finally {
            closeSession();
        }
    }

    public List getMailList(int userId, int begin, int count, int minId) throws DataAccessException {
		try {
		    Query query = getSession().createQuery(
		            "from Mail m where m.destId=:userId and m.id>:minId order by m.postTime desc");
		    query.setInteger("userId", userId);
		    query.setInteger("minId", minId);
		    query.setFirstResult(begin);
		    query.setMaxResults(count);
		    return query.list();
		} catch (HibernateException ex) {
		    return new ArrayList();
		} finally {
		    closeSession();
		}
	}
    
    public List getMailList(int userId, int begin, int count, int minId, Date startTime, Date endTime) throws DataAccessException {
    	try {
    		Query query = getSession().createQuery(
    		"from Mail m where m.destId=:userId and m.id>:minId and m.postTime >=:startTime and m.postTime <:endTime order by m.postTime desc");
    		query.setInteger("userId", userId);
    		query.setInteger("minId", minId);
    		query.setDate("startTime", startTime);
    		query.setDate("endTime", endTime);
    		query.setFirstResult(begin);
    		query.setMaxResults(count);
    		return query.list();
    	} catch (HibernateException ex) {
    		return new ArrayList();
    	} finally {
    		closeSession();
    	}
    }
    public List getMailList(int userId, int begin, int count,int[] blackList) throws DataAccessException{
        try {
            Query query = getSession().createQuery(
                    "from Mail m where m.destId=:userId and m.postTime<:time"+getBlackListString(blackList)+" order by m.postTime desc");
            query.setInteger("userId", userId);
            query.setTimestamp("time", new Date());
            query.setFirstResult(begin);
            query.setMaxResults(count);
            return query.list();
        } catch (HibernateException ex) {
            return new ArrayList();
        } finally {
            closeSession();
        }
    }
    
    public List getMailAttachmentLimit(int userId, int begin, int count,int[] blackList, int sourceId) throws DataAccessException{
        try {
            Query query = getSession().createQuery(
            	    "from Mail m where m.destId=:userId and m.postTime<:time and m.sourceId=:sourceId and  m.attachment is not null order by m.postTime desc");
            query.setInteger("userId", userId);
            query.setTimestamp("time", new Date());
            query.setInteger("sourceId", sourceId);
            query.setFirstResult(begin);
            query.setMaxResults(count);
            return query.list();
        } catch (HibernateException ex) {
            return new ArrayList();
        } finally {
            closeSession();
        }
    }
    
    
/*    *//**
     * @param userId
     * @param begin
     * @param count
     * @param blackList
     * @param sourceId
     * @return  提取整页附件并删除
     * @throws DataAccessException
     *//*
    public List getMailPageAttachmentLimit(int userId, int begin, int count,int[] blackList) throws DataAccessException{
        try {
            Query query = getSession().createQuery(
            "from Mail m where m.destId=:userId and m.postTime<:time and m.sourceId = -1 order by m.postTime desc");
            query.setInteger("userId", userId);
            query.setTimestamp("time", new Date());
            query.setFirstResult(begin);
            query.setMaxResults(count);
            return query.list();
        } catch (HibernateException ex) {
            return new ArrayList();
        } finally {
            closeSession();
        }
    }*/
    
    private static String getBlackListString(int[] blackList){
        if(blackList.length==0)
            return "";

        StringBuilder sb = new StringBuilder(300);
        sb.append(" and sourceid not in(");
        sb.append(blackList[0]);
        for(int i=1;i<blackList.length;i++){
            sb.append(",");
            sb.append(blackList[i]);
        }
        sb.append(")");
//        System.out.println(sb.toString());
        return sb.toString();
    }

    public List getObsoleteFeeMail() throws DataAccessException{
        try {
            Query query = getSession().createQuery(
                    "from Mail m where m.postTime<:time and m.price>0");
            Date date = new Date(System.currentTimeMillis() -
                                 2L * 86400L * 1000L);
            query.setTimestamp("time", date);
            List l = query.list();
            return l;
        } catch (HibernateException ex) {
            return new ArrayList();
        }
        finally{
            closeSession();
        }
    }
    
    public List getUnvalidMail() throws DataAccessException {
		try {
			Query query = getSession()
					.createQuery(
							"from Mail m where m.validTime<:time");
			query.setFirstResult(0);
			query.setMaxResults(1000);
			Date date =  new Date();
			query.setTimestamp("time", date);
			
			List l = query.list();
			return l;
		} catch (HibernateException ex) {
			return new ArrayList();
		} finally {
			closeSession();
		}
	}

    public List getMailCountAndDestId() throws DataAccessException{
        String hql = "select count(m.destId),m.destId from Mail m group by m.destId having count(m.destId)>100";
        return getList(hql);
    }


    public List getMail(int id,int count) throws DataAccessException{
        String hql = "from Mail m where m.destId = " + id +
            " order by m.postTime asc";
        return getLimitedList(hql,0,count);
    }

    public int getUnReadedMailCount(int playerId) throws DataAccessException {
        try {
            Query query = getSession().createQuery("select count(*) from Mail m where m.destId=:userId and m.postTime<:time and m.readed = false");
            query.setInteger("userId", playerId);
            query.setTimestamp("time", new Date());
            Long l = (Long) query.uniqueResult();
            return l.intValue();
        } catch (HibernateException ex) {
            return 0;
        } finally {
            closeSession();
        }
    }

    public int getUnReadedMailCount(int playerId,int[] blackList) throws DataAccessException {
        try {
            Query query = getSession().createQuery("select count(*) from Mail m where m.destId=:userId and m.postTime<:time and m.readed = false "+getBlackListString(blackList));
            query.setInteger("userId", playerId);
            query.setTimestamp("time", new Date());
            Long l = (Long) query.uniqueResult();
            return l.intValue();
        } catch (HibernateException ex) {
            return 0;
        } finally {
            closeSession();
        }
    }
}
