package peony.service.award;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.LogUtil;
import peony.game.NoEnoughValueException;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.account.Account;
import peony.service.account.AccountService;
import peony.service.shop.NoItemShopBuy;
import peony.service.shop.NoItemShopBuyI;
import peony.service.shop.ShopException;
import peony.service.shop.ShopService;

/**
 * 领奖
 * @author pmeng
 */
public class AwardGetCall extends ClientSessionAsyncCall implements NoItemShopBuyI{

	int serial;
	
	int closeId1;
	
	int closeId2;
	
	AwardService aService =  Server.server.getServiceRegistry().getAwardService();
	
	Player p;
	
	int awardId;
	
	public AwardGetCall(ClientSession session,Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.closeId1 = packet.getInt();
		this.closeId2 = packet.getInt();
		p = (Player)session.getClient();
	}

	public void callFinish() throws Exception {
		if(success){
			try {
				awardId = aService.getAward(p, closeId1, closeId2);
			} catch (AwardException e) {
				error(e.getMessage());
				return;
			}
			Packet pt = new Packet(OpCode.GET_AWARD_SERVER); 
			pt.putInt(serial);
			pt.putInt(awardId);
			p.send(pt);
		}else{
			ErrorHandler.sendErrorMessage(session, serial, OpCode.GET_AWARD_CLIENT,errorMessage);
		}
	}

	public void run() {
		if(p!=null){
			int count = p.pool.getInt(AwardService.PROPERTY_GETAWARD_NUM,0);
			LogUtil.logAward(p, count, awardId);
		    if((p.vipLevel < 3&&count >= 1)||(p.vipLevel >= 3&&count >= 2)){
		    	ErrorHandler.sendErrorMessage(session, serial, OpCode.GET_AWARD_CLIENT,"今天抽奖次数已用完，请明天再来");
		    	return;
		    }
			if(closeId1 == -1 && closeId2 == -1){
				addToClientSession();
				return;
			}
			if(closeId1 != -1 && closeId2 == -1){
				//扣J币
				PlayerTransaction tx = p.newTransaction("AWARD");
				try {
					p.decMoney(AwardService.FIRST_CLOSE, tx, true);
					aService.setCloseId(p, closeId1, -1);
					tx.commit();
					addToClientSession();
					return;
				} catch (NoEnoughValueException e) {
					tx.rollback();
					error(peony.Messages.STRING_00158);
//					aService.resetAwardItemId(p);
					aService.setCloseId(p, -1, -1);
					addToClientSession();
					return;
				}
			}
			if(closeId1 != -1 && closeId2 != -1){
				int money = p.money;
				AccountService as = Server.server.getServiceRegistry()
				.getAccountService();
				Account account = as.getAccount(p.accountId);
				long iMoney = account.getLongIMoney() / 100;
				if(money < AwardService.FIRST_CLOSE||iMoney < 2 * 36){
					error(peony.Messages.STRING_01821);
//					aService.resetAwardItemId(p);
					addToClientSession();
					return;
				}
				//扣J币
				PlayerTransaction tx = p.newTransaction("AWARD");
				try {
					p.decMoney(AwardService.FIRST_CLOSE, tx, true);
					aService.setCloseId(p, closeId1, -1);
					tx.commit();
				} catch (NoEnoughValueException e) {
					tx.rollback();
					error(peony.Messages.STRING_00158);
//					aService.resetAwardItemId(p);
					aService.setCloseId(p, -1, -1);
					addToClientSession();
					return;
				}
				try {
					ShopService service = Server.server.getServiceRegistry().getShopService();
					int shopId = service.getShopByItemId(NoItemShopBuy.LIANGYUANBAO).id;
					NoItemShopBuy dib = new NoItemShopBuy(p,serial,shopId,NoItemShopBuy.LIANGYUANBAO,1,this,null);
					Server.server.getServiceRegistry().getShopService().buy(p, dib);
					aService.setCloseId(p, closeId1, closeId2);
				} catch (ShopException e) {
					error(peony.Messages.STRING_01822);
					aService.setCloseId(p, -1, -1);
					addToClientSession();
					return;
				}
			}
		}
	}

	public void process(Object[] o) {
		//失败之后补J币
		addToClientSession();
	}

	public void procssFail(Object[] o) {
		error(peony.Messages.STRING_01665);
		addToClientSession();
	}

}
