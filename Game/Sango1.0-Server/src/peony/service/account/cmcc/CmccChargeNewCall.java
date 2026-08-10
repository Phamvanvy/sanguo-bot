package peony.service.account.cmcc;

import org.apache.log4j.Logger;
import peony.game.ErrorHandler;
import peony.game.LogUtil;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.changed.ChangedItem;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.ServiceEvent;
import peony.service.account.Account;
import peony.service.account.AccountAsyncCall;
import peony.service.account.ChargeRegularCall;
import peony.service.account.RecordChargeCall;

/**
 * CMCC点数充值
 * @author dchen
 */
public class CmccChargeNewCall extends AccountAsyncCall {
	
	private static Logger log = Logger.getLogger(CmccChargeNewCall.class);

	protected Player player;
	protected int serial;
	protected int ammount;
	
	
	public CmccChargeNewCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.ammount = packet.getInt();
		this.player = (Player)session.getClient();
	}

	public void callFinish() throws Exception {
		if(success){
			if(message!=null && message instanceof SubLegacyBuyResultMessage){
				SubLegacyBuyResultMessage mess = (SubLegacyBuyResultMessage)message;
				/**
			     * 购买商品结果
			     * requestId		int				请求ID
			     * result			boolean			购买结果，true成功，false失败
			     * balance			int				账户余额(单位1/100点)
			     * cost				int				消耗i币(单位1/100i)(卓望版本总是-1)
			     * msg				String			如果失败，返回错误信息
			     * balance2			long			账户余额(单位1/100i)
			     */
				Account a = (Account)player.session.getIdentity();
				int balance = mess.getBalance();
				long balance2 = mess.getBalance2();
				if(player!=null && balance2>0){
					a.setLongIMoney(balance2);
					String showPrice = player.ibToYuanbao(player.getAccount().getLongIMoney());
		 			player.addStringPropertyChangedItem(ChangedItem.YUANBAO, showPrice, true);
		 			RecordChargeCall call = new RecordChargeCall(null, a.getId(), ammount/10);
					Server.server.getServiceRegistry().getDbService().schedule(call);
					ChargeRegularCall call2 = new ChargeRegularCall(session, a.getId(), ammount/10);
					Server.server.getServiceRegistry().getDbService().schedule(call2);
					Player p = (Player) session.getClient();
					if (p != null) {
						Server.server.getEventManager().fireEvent(
								new ServiceEvent(ServiceEvent.EVENT_CHARGE_SUCCESS, p, ammount/10));
		            }
				}
				
				if(balance2>0){
					Packet pt = new Packet(OpCode.CMCC_CHARGE_NEW_SERVER);
					pt.putInt(serial);
					player.send(pt);
				}else{
					ErrorHandler.sendErrorMessage(session, serial, OpCode.CMCC_CHARGE_NEW_CLIENT, "充值失败");
				}
				log.info("[CMCCCHARGENEW]"+LogUtil.getPlayerLogString(player)+"MONEY["+ammount+"]BALANCE["+balance2+"]OK");
			}
		}else{
			ErrorHandler.sendErrorMessage(session, serial, OpCode.CMCC_CHARGE_NEW_CLIENT, "网络故障,请稍后重试");
		}
	}

	public void run() {
		if(player!=null){
			int accountId = player.accountId;
			String consumeCode = getConsumCode(ammount, player.getAccount().getModel());
			CmccIBuyMessage buyMessage = new CmccIBuyMessage(accountId, "", ammount*3600, consumeCode, player.getAccount().getVersionString(), 
					player.getAccount().getCmccUserId(), 1);
			Server.server.getServiceRegistry().getAccountService().sendAndRegister(buyMessage, this);
			log.info("[CMCCCHARGENEW]"+LogUtil.getPlayerLogString(player)+"MONEY["+ammount+"]CODE["+consumeCode+"]TRY");
		}
	}
	
	public static String getConsumCode(int ammount, String model){
		if(model.contains("Android")){
			switch(ammount){
			case 10:
				return "160121244070";
			case 60:
				return "160121244069";
			}
		}else{
			switch(ammount){
			case 50:
				return "120122041101";
			case 100:
				return "120122041102";
			case 500:
				return "120122041103";
			case 990:
				return "120122041104";
			}
		}
		return "";
	}

}
