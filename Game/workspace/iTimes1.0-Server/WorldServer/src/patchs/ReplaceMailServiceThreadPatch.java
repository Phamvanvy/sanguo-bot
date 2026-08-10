package patchs;

import java.util.Date;
import java.util.List;

import com.pip.itimes.server.bean.Mail;
import com.pip.itimes.server.dao.DataAccessException;
import com.pip.itimes.server.dao.MailDao;

public class ReplaceMailServiceThreadPatch extends Thread {
	private MailDao dao = new MailDao();
	
	public ReplaceMailServiceThreadPatch() {
		super("ReplaceMailServiceThreadPatch");
	}
	
	public void run() {
		while (true) {
	        try {
	            Thread.sleep(1 * 1000L);
	        } catch (InterruptedException ex) {
	        }
	        try {
                List l = dao.getObsoleteFeeMail();
                for (int i = 0; i < l.size(); i++) {
                    deleteMail((Mail) l.get(i));
                }
	        } catch (Throwable e) {
	        }
	    }
	}
	
	public void deleteMail(Mail mail) throws DataAccessException {
        if (mail.getSourceId() > 0 && mail.getPrice() >= 0 && mail.getAttachment() != null &&
            mail.getAttachment().length > 0) {
            Mail newMail = new Mail();
            newMail.setSourceId(-1);
            newMail.setSourceName("系统");
            newMail.setDestId(mail.getSourceId());
            newMail.setDestName(mail.getSourceName());
            newMail.setTitle("回复:" + mail.getTitle());
            newMail.setAttachment(mail.getAttachment());
            newMail.setPrice(0);
            newMail.setContent("超时未处理");
            newMail.setReaded(false);
            newMail.setPostTime(new Date());
            newMail.setValidTime(new Date(System.currentTimeMillis() + 30L * 24L * 3600L * 1000L));
            addMail(newMail);
        }
        dao.deleteMail(mail);
    }

    public void addMail(Mail mail) {
        try {
            dao.addMail(mail);
        } catch (DataAccessException ex) {
        }
    }
}
