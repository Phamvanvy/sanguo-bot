package peony.game.bbs;

import java.util.List;

import peony.common.ClientSessionAsyncCall;
import peony.game.CommonUtil;
import peony.game.OpCode;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class BbsListCall extends ClientSessionAsyncCall {
	
	protected int serial;
	protected int pageNo;
	protected int pageSize;

	
	public BbsListCall(ClientSession session,Packet packet){
		super(session);
		this.serial = packet.getInt();
		this.pageSize = packet.getShort();
		this.pageNo = packet.getShort();
	}
	
	public void callFinish() throws Exception {

	}

	public void run() {
		List<Bbs> list;
		if(pageSize == -1){
			list = Server.server.getServiceRegistry().getDbService().bbsDAO.getBbsOrder();
		}else{
			list = Server.server.getServiceRegistry().getDbService().bbsDAO.getBbs(pageSize*pageNo, pageSize);
		}
		int count = Server.server.getServiceRegistry().getDbService().bbsDAO.getBbsCount();
		int pageCount = count / pageSize;
		if (count % pageSize != 0)
			pageCount++;
		Packet pt = new Packet(OpCode.BBS_LIST_SERVER);
		pt.putInt(serial);
		pt.putShort(pageSize);
		pt.putShort(pageNo);
		pt.putInt(count);
		pt.putShort(list.size());
		for(Bbs b:list){
			pt.putInt(b.id);
			pt.putString(b.title);
			pt.putString(CommonUtil.getDateString(b.createTime));
		}
		session.send(pt);
	}

}
