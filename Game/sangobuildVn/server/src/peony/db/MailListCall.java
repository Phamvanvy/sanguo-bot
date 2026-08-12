package peony.db;

import java.util.Date;
import java.util.List;

import peony.common.ClientSessionAsyncCall;
import peony.game.CommonUtil;
import peony.game.ErrorHandler;
import peony.game.Mail;
import peony.game.OpCode;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class MailListCall extends ClientSessionAsyncCall{
	
	private int serial;
	private int playerId;
	private int pageSize;
	private int pageNo;
	private List<Mail> list;
	private int count;

	public MailListCall(ClientSession session,int serial,int playerId,int pageSize,int pageNo){
		super(session);
		this.serial = serial;
		this.playerId = playerId;
		this.pageSize = pageSize;
		this.pageNo = pageNo;
	}

	public void callFinish() throws Exception {
		if (success) {
			int pageCount = count / pageSize;
			if (count % pageSize != 0)
				pageCount++;
			Packet pt = new Packet(OpCode.MAIL_LIST_SERVER);
			pt.putInt(serial);
			pt.putShort(pageSize);
			pt.putShort(pageNo);
			pt.putInt(count);
			pt.putShort(list.size());
			for(Mail m:list){
				pt.putInt(m.getId());
				pt.putInt(m.getSourceId());
				pt.putString(m.getSourceName());
				pt.putString(m.getTitle());
				pt.putString(CommonUtil.getDateString(m.getPostTime()));
				pt.putString(CommonUtil.getRemainTimeString(m.getExpirationTime()));
				pt.put(m.getStatus());
				if(m.getAttachment()!=null){
					pt.put(1);
				}else{
					pt.put(0);
				}
				pt.putInt(m.getPrice());
			}
			session.send(pt);
		}else{
			ErrorHandler.sendErrorMessage(session, serial, OpCode.MAIL_LIST_CLIENT, errorMessage);
		}
	}

	public void run() {
		if (pageSize <= 0 || pageSize > 100 || pageNo < 0 || pageNo > Integer.MAX_VALUE / pageSize) {
			error(null, "Invalid mail pagination");
			addToClientSession();
			return;
		}
		DBService dbService = Server.server.getServiceRegistry().getDbService();
//		Transaction tx = HibernateUtil.getSessionFactory().getCurrentSession()
//		.beginTransaction();
//		try{
			list = dbService.mailDAO.getMailListByDestId(playerId, pageSize*pageNo, pageSize,new Date());
			count = dbService.mailDAO.getMailCount(playerId,new Date());
//			tx.commit();
//		}catch(Exception ex){
//			tx.rollback();
//			ex.printStackTrace();
//			error(ex,"查询邮件列表错误");
//		}
		addToClientSession();
	}
}
