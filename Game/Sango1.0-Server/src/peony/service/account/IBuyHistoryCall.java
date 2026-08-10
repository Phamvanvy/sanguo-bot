package peony.service.account;

import java.util.List;

import com.pip.net.message.ErrorMessage;

import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.account.cmcc.CmccHistory;
import peony.service.account.cmcc.CmccHistoryMessage;
import peony.service.account.cmcc.CmccHistoryOkMessage;

public class IBuyHistoryCall extends AccountAsyncCall {
	
	protected int serial;
	protected int type;
	protected String startDate,endDate;
	protected int startSeq;
	protected int pageSize;
	protected int timeType;
	protected int queryType;
	protected String cmccUserId;
//    * type				byte			1消费历史，2充值历史
//    * accountId		int				帐号ID，如为-1表示可忽略此参数
//    * startDate        String          起始日期
//    * endDate          String          结束日期
//    * startSeq         int             起始记录号，1表示第一条
//    * pageSize         int             每页数据条数
//    * timeType         int             查询时间类型，可选，0 - 当日，1 - 指定月，2 - 10天内
//    * queryType        int             查询类型，可选，充值历史：0 - 全部；消费历史：0 - 查询所有客户端网游，1 - 查询所有WAP网游，2 - 查询自己
//    * cmccUserId       String          卓望平台用户ID（如果accountId不为-1，此条可选）
	
	public IBuyHistoryCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.type = packet.getByte();
		this.startDate = packet.getString();
		this.endDate = packet.getString();
		this.startSeq = packet.getInt();
		this.pageSize = packet.getInt();
		this.timeType = packet.getInt();
		this.queryType = packet.getInt();
		this.cmccUserId = packet.getString();
	}

	public void callFinish() throws Exception {
		if (success) {
			CmccHistoryOkMessage msg = (CmccHistoryOkMessage)message;
			List<CmccHistory> l = msg.getHistorys();
			Packet pt = new Packet(OpCode.IBUY_HISTORY_SERVER);
			pt.putInt(this.serial);
			pt.putInt(l.size());
			for(CmccHistory h:l){
				pt.putInt(h.count);
				pt.putString(h.info);
			}
			session.send(pt);
		} else {
			ErrorHandler.sendErrorMessage(session, serial, OpCode.IBUY_HISTORY_CLIENT, ErrorMessages.getErrorMesssage((ErrorMessage)message));
		}
	}

	public void run() {
		Server.server.getServiceRegistry().getAccountService().sendAndRegister(
				new CmccHistoryMessage(type, startDate, endDate, startSeq,
						pageSize, timeType, queryType, cmccUserId), this);
	}

}
