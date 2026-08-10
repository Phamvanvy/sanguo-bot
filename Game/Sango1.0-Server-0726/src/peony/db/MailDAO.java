package peony.db;

import java.util.Date;
import java.util.List;

import peony.game.Mail;

import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class MailDAO extends GenericHibernateDAO<Mail, Integer> {

	public Mail getMailById(int id) {
		return (Mail) uniqueResult("from Mail m where m.id=?", id);
	}
	
	@SuppressWarnings("unchecked")
	public List<Mail> getMails(int begin,int count){
		return limitList(
				"from Mail m where m.price>0",
				begin, count);
	}

	@SuppressWarnings("unchecked")
	public List<Mail> getMailListByDestId(int destId, int begin, int count,Date time) {
		return limitList(
				"from Mail m where m.destId=? and m.validTime<? order by m.postTime desc",
				begin, count, destId, time);
	}
	
	@SuppressWarnings("unchecked")
	public List<Mail> getPlayerMailListByDestId(int destId, int begin, int count,Date time) {
		return limitList(
				"from Mail m where m.destId=? and m.validTime<? and m.sourceId!=-1 order by m.postTime desc",
				begin, count, destId, time);
	}
	
	@SuppressWarnings("unchecked")
	public List<Mail> getMailListByRequestPay(int sourceId,int begin, int count,Date time){
		return limitList(
				"from Mail m where m.sourceId=? and m.validTime<? and m.price>0 order by m.postTime desc",
				begin, count, sourceId, time);
	}
	
	public int getRequestPayMailCount(int sourceId,Date time) {
		Long l = (Long) uniqueResult(
				"select count(*) from Mail m where m.sourceId=? and m.validTime<? and m.price>0",
				sourceId, new Date());
		return l.intValue();
	}
	
	@SuppressWarnings("unchecked")
	public List<Mail> getSystemMailList(int destId,int begin, int count,Date time){
		return limitList(
				"from Mail m where m.destId=? and m.validTime<? and m.sourceId=-1 order by m.postTime desc",
				begin, count, destId, time);
	}
	
	@SuppressWarnings("unchecked")
	public List<Mail> getAllSystemMailList(int destId,Date time){
		return list(
				"from Mail m where m.destId=? and m.validTime<? and m.sourceId=-1 order by m.postTime desc",
				destId, time);
	}
	
	public int getSystemMailCount(int destId,Date time) {
		Long l = (Long) uniqueResult(
				"select count(*) from Mail m where m.destId=? and m.validTime<? and m.sourceId=-1",
				destId, new Date());
		return l.intValue();
	}
	
	public int getMailCount(int destId,Date time) {
		Long l = (Long) uniqueResult(
				"select count(*) from Mail m where m.destId=? and m.validTime<? and m.sourceId!=-1",
				destId, new Date());
		return l.intValue();
	}

	@SuppressWarnings("unchecked")
	public List<Mail> getUnFavoriteNoAttachmentMails(int destId, Date time) {
		return list(
				"from Mail m where m.destId=? and m.validTime<? and m.attachment is not null and status<>"
						+ Mail.READED_FAVORITE
						+ " and status<>"
						+ Mail.UNREADED_FAVORITE, destId, time);

	}
	
	@SuppressWarnings("unchecked")
	public List<Mail> getObsoletMails(Date time){
		return list("from Mail m where m.postTime<? and m.price>0 and not (m.attachment is null)",time);
	}
    
	public void deleteUnFavoriteMails(int destId, Date time) {
		update(
				"delete Mail m where m.destId=? and m.validTime<? and status<>"
						+ Mail.READED_FAVORITE + " and status<>"
						+ Mail.UNREADED_FAVORITE + " and m.attachment is null",
				destId, time);
	}
	
	
	@SuppressWarnings("unchecked")
	public List<Mail> getObsoleteNoAttachmentMails(int destId,Date time){
		return list("from Mail m where m.destId=? and m.validTime<? and m.attachment is null and m.status<>"+Mail.READED_FAVORITE+" and m.status<>"+Mail.UNREADED_FAVORITE,
				destId,time);	
	}
	@SuppressWarnings("unchecked")
	public List<Mail> getExpirationAttachmentMails(int begin,int count){
		return limitList("from Mail m where m.expirationTime<=?",begin,count,new Date());	
	}
	
	}
	
	
	
	

