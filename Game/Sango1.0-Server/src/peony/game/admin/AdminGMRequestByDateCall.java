package peony.game.admin;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import peony.common.ClientSessionAsyncCall;
import peony.db.DBService;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class AdminGMRequestByDateCall extends ClientSessionAsyncCall {

	protected int serial;
	protected int pageNo;
	protected int pageSize;
	protected int count;
	protected Date date;
	protected Date date2;
	protected List<GMRequest> mails;
	
	public AdminGMRequestByDateCall(ClientSession session,Packet packet){
		super(session);
		this.serial = packet.getInt();
		this.pageNo = packet.getShort();
		this.pageSize = packet.getShort();
		String dateString = packet.getString();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		try {
			date = format.parse(dateString);
		} catch (ParseException e) {
			date = new Date();
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.MILLISECOND, 59);
		date2 = cal.getTime();
	}
	
	public void callFinish() throws Exception {
		if (success) {
			int pageCount = count / pageSize;
			if (count % pageSize != 0)
				pageCount++;
			Packet pt = new Packet(OpCode.ADMIN_GMREQUEST_LIST_BYDATE_SERVER);
			pt.putInt(serial);
			pt.putShort(pageSize);
			pt.putShort(pageNo);
			pt.putInt(count);
			pt.putShort(mails.size());
			for(GMRequest m:mails){
				pt.putInt(m.getId());
				pt.put(m.getType());
				pt.putInt(m.getPlayerId());
				pt.putString(m.getPlayerName());
				pt.putString(m.getCause());
				pt.put(m.getState());
				pt.putString(m.getSolvent());
				pt.putLong(m.getCreateTime().getTime());
				pt.putString(m.getModel());
				pt.putShort(m.getMapId());
				pt.putShort(m.getX());
				pt.putShort(m.getY());
			}
			session.send(pt);
		}else{
			ErrorHandler.sendErrorMessage(session, serial, OpCode.ADMIN_GMREQUEST_LIST_BYDATE_CLIENT, errorMessage);
		}
	}

	public void run() {
		DBService dbService = Server.server.getServiceRegistry().getDbService();
		mails = dbService.gmQuestDAO.getGMRequestsByDate(date, date2, pageSize*pageNo, pageSize);
		count = dbService.gmQuestDAO.getGMRequestByDateCount(date, date2);
		addToClientSession();
	}

}
