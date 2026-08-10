package peony.service.account;


import org.apache.log4j.Logger;

import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.IMoneyCard;
import peony.game.ItemUtil;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.changed.ChangedItem;
import peony.net.ClientSession;
import peony.net.Packet;

import com.pip.net.message.ErrorMessage;
import com.pip.net.message.gameaccount.CreateIMoneyCardMessage;
import com.pip.net.message.gameaccount.CreateIMoneyCardOkMessage;


/**
 * 生成元宝卡Call。在收到用户生成元宝卡请求之后，程序将会通知认证服务器，让认证服务器生成对应金额的元宝卡。
 * 在认证服务器成功生成元宝卡消息返回之后，本地新建一个元宝卡物品，并且将元宝卡的卡号和密码附带在GameItemObject之中。
 * 每张元宝卡都有唯一的InstanceId。
 * 如果玩家当前在线，那么加到玩家的背包中。如果不在线，那么给玩家发信
 * @author Jeffrey
 *
 */
public class IMoneyCardCall extends AccountAsyncCall {
	
	private static final Logger log = Logger.getLogger(IMoneyCardCall.class);

	protected int serial;
	protected int type;
	protected int playerId;
	
	protected static int[] AMOUNT = {360000};
	
	public IMoneyCardCall(ClientSession session, Packet packet){
		super(session);
		this.serial = packet.getInt();
		this.type = packet.getInt();
	}
	
	public void callFinish() throws Exception {
		if(!success){
			log.info("[IMONEYCARD]ID["+playerId+"]FAIL");
            ErrorMessage msg = (ErrorMessage) message;
            ErrorHandler.sendErrorMessage(session, serial, OpCode.IMONEYCARD_CREATE_CLIENT, ErrorMessages.getErrorMesssage(msg));
		}else{
			CreateIMoneyCardOkMessage msg = (CreateIMoneyCardOkMessage) message;
			log.info("[IMONEYCARD]ID["+playerId+"]CARDNO[" + msg.getCardno() + "]OK");
			GameItem item = ObjectAccessor.createGameItem(ItemUtil.ITEM_IMONEY_CARD_10);
			IMoneyCard card = new IMoneyCard(AMOUNT[type],msg.getCardno(),msg.getPassword());
			item.object = card;
			boolean added = false;
			Player p = (Player)ObjectAccessor.getPlayer(playerId);
			if(p != null){
				PlayerTransaction tx = p.newTransaction("IMC");
				if(p.bag.addGameItem(item, 1, tx, true)){
					tx.commit();
					added = true;
				}else{
					tx.rollback();
				}
				Packet pt = new Packet(OpCode.IMONEYCARD_CREATE_SERVER);
				pt.putInt(serial);
				p.send(pt);
				if (p.session != null) {
					Account account = (Account) p.session.getIdentity();
					if (account != null) {
						account.setLongIMoney(msg.getLongBalance());
						p.addIntPropertyChangedItem(ChangedItem.IMONEY, (int)(account.getLongIMoney() / 100), true, true);
					}
				}
			}
			if(!added){ //如果没有加入到背包中，那么邮寄
				Server.server.getServiceRegistry().getMailService().sendSystemMail(playerId, "系统", "元宝卡", "", 0, item, 1, "IMCARD");
				if(p != null){
					p.message(-1, "你兑换的元宝卡已经邮寄到信箱中，请注意查收。", -1, -1);
				}
			}
		}
	}

	public void run() {
		Player p = (Player)session.getClient();
		if(p != null){
			this.playerId = p.id;
			Account account = (Account)session.getIdentity();
			log.info("[IMONEYCARD]"+LogUtil.getPlayerLogString(p)+"TRY");
	        CreateIMoneyCardMessage message = new CreateIMoneyCardMessage(Server.server.gameCode,account.getId(),account.getKey(),AMOUNT[this.type]);
	        Server.server.getServiceRegistry().getAccountService().sendAndRegister(message, this);
		}
	}

}
