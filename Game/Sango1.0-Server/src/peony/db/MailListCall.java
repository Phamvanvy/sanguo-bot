package peony.db;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import peony.common.ClientSessionAsyncCall;
import peony.game.CommonUtil;
import peony.game.ErrorHandler;
import peony.game.Mail;
import peony.game.OpCode;
import peony.game.Player;
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
	private int type;
	protected Player player;
	
	public static int MAIL_SYSTEM = 0;//系统邮件
	public static int MAIL_PLAYER = 1;//玩家邮件
	public static int MAIL_PAY = 2;//需要对方付费的邮件
	
	public static int MAIL_LIST_TYPE = 0;

	public MailListCall(ClientSession session,int serial,int type,int playerId,int pageSize,int pageNo){
		super(session);
		this.serial = serial;
		this.type = type;
		this.playerId = playerId;
		this.pageSize = pageSize;
		this.pageNo = pageNo;
		this.player = (Player)session.getClient();
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
			pt.putShort(list==null ? 0 : list.size());
			if(list!=null){
				for(Mail m:list){
					pt.putInt(m.getId());
					pt.putInt(m.getSourceId());
					pt.putInt(m.getDestId());
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
			}
			session.send(pt);
		}else{
			ErrorHandler.sendErrorMessage(session, serial, OpCode.MAIL_LIST_CLIENT, errorMessage);
		}
	}

	public void run() {
		DBService dbService = Server.server.getServiceRegistry().getDbService();
//		Transaction tx = HibernateUtil.getSessionFactory().getCurrentSession()
//		.beginTransaction();
//		try{
		if(type == MAIL_PLAYER){
			if(MAIL_LIST_TYPE!=0){
				list = dbService.mailDAO.getPlayerMailListByDestId(playerId, pageSize*pageNo, pageSize,new Date());
				count = dbService.mailDAO.getMailCount(playerId,new Date());
			}else{
				count = dbService.mailDAO.getMailCount(playerId,new Date());
				List<Mail> allList = dbService.mailDAO.getPlayerMailListByDestId(playerId, 0, count,new Date());
				allList = filterMailList(allList);
//				int pageSize1 = pageSize;
//				while((list=getList(allList, pageNo, pageSize1))==null){
//					pageSize1--;
//					list = getList(allList, pageNo, pageSize1);
//				}
				//分页
				int startIndex = pageNo * pageSize;
				int endIndex = startIndex + pageSize;
				if(endIndex > allList.size()){
					endIndex = allList.size();
				}
				if(startIndex >= 0 && startIndex < allList.size() && endIndex >= 0 && endIndex <= allList.size() && startIndex < endIndex){
					list = allList.subList(startIndex, endIndex);
				} else {
					if(allList.size() == 0){
//						ErrorHandler.sendErrorMessage(session, serial, OpCode.MAIL_LIST_CLIENT, "暂无信件");
					} else {
						ErrorHandler.sendErrorMessage(session, serial, OpCode.MAIL_LIST_CLIENT, "已经是最后一页");
						return;
					}
					
					
				}
				
				
				count = allList.size();
			}
		}else if(type == MAIL_PAY){
			list = dbService.mailDAO.getMailListByRequestPay(playerId, pageSize*pageNo, pageSize,new Date());
			count = dbService.mailDAO.getRequestPayMailCount(playerId,new Date());
		}else if(type == MAIL_SYSTEM){
			list = dbService.mailDAO.getSystemMailList(playerId, pageSize*pageNo, pageSize,new Date());
			count = dbService.mailDAO.getSystemMailCount(playerId,new Date());
		}else{
			list = dbService.mailDAO.getMailListByDestId(playerId, pageSize*pageNo, pageSize,new Date());
			count = dbService.mailDAO.getMailCount(playerId,new Date());
		}
//			tx.commit();
//		}catch(Exception ex){
//			tx.rollback();
//			ex.printStackTrace();
//			error(ex,"查询邮件列表错误");
//		}
		addToClientSession();
	}
	
	public List<Mail> getList(List<Mail> list, int pageNo, int pageSize){
		try {
			List<Mail> returnList = list.subList(pageSize*pageNo, pageSize*pageNo+pageSize);
			return returnList;
		} catch (Exception e) {
			e.getMessage();
			e.printStackTrace();
			return null;
		}
	}
	
	protected List<Mail> filterMailList(List<Mail> list){
		Iterator<Mail> it = list.iterator();
		while(it.hasNext()){
			Mail m = it.next();
			if(m!=null && player.relations!=null && player.relations.blackList!=null 
					&& player.relations.blackList.findPlayer(m.getSourceId())!=-1){
				it.remove();
			}
		}
		return list;
	}
	
}
