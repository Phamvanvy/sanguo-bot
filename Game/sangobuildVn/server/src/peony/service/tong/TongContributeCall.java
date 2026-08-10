package peony.service.tong;

import java.text.MessageFormat;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.NoEnoughValueException;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class TongContributeCall extends ClientSessionAsyncCall {

	int serial;
	int money;
	
	public TongContributeCall(ClientSession session, Packet packet){
		super(session);
		this.serial = packet.getInt();
		this.money = packet.getInt();
	}
	
	public void callFinish() throws Exception {
		
	}

	public void run() {
		if(money<=0)
			return;
		Player p = (Player)session.getClient();
		if(p != null){
			if(money < 5000){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.TONG_CONTRIBUTE_CLIENT, "捐献金额必须大于5000");
				return;
			}
			TongService tongService = Server.server.getServiceRegistry().getTongService();
			Tong tong = tongService.getPlayerTong(p.id);
			if(tong == null){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.TONG_CONTRIBUTE_CLIENT, "你没有所属的帮派");
				return;
			}
			PlayerTransaction tx = p.newTransaction("TCL");
			try {
				p.decMoney(money, tx, false);
				tx.commit();
				tong.addMoney(money);
//				int gainHorner = (int)(money/5000); 
				TongMember member = tongService.getPlayerInfo(p.id);
//				if(gainHorner>0){
//					member.honor += gainHorner;
//					//存数据库
//					TongMemberDAO dao = Server.server.getServiceRegistry().getDbService().tongMemberDAO;
//					dao.updateEntity(member);
//					Server.server.getServiceRegistry().getChatService().sendGuildSystemMessage(
//							"收到"+TongService.getDutyName(member.duty)+p.name+"捐奉"+money
//							+"，特此嘉勉，奖励军功"+gainHorner+"点。", tong.id);
//				}else{
//					Server.server.getServiceRegistry().getChatService().sendGuildSystemMessage(
//							"收到"+TongService.getDutyName(member.duty)+p.name+"捐奉"+money
//							+"，特此嘉勉。", tong.id);
//				}
				Server.server.getServiceRegistry().getChatService().sendGuildSystemMessage(
						MessageFormat.format("收到{0}捐奉{1}，特此嘉勉。", TongService.getDutyName(member.duty)+p.name,money), tong.id);
				Packet pt = new Packet(OpCode.TONG_CONTRIBUTE_SERVER);
				pt.putInt(serial);
				p.send(pt);
			} catch (NoEnoughValueException e) {
				tx.rollback();
				ErrorHandler.sendErrorMessage(session, serial, OpCode.TONG_CONTRIBUTE_CLIENT, "没有足够的金钱捐献");
			}
		}
	}

}
