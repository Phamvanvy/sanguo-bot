package peony.service.account;

import org.apache.log4j.Logger;

import peony.game.ErrorHandler;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.changed.ChangedItem;
import peony.net.ClientSession;

import com.pip.net.message.ErrorMessage;
import com.pip.net.message.gameaccount.UseIMoneyCardMessage;
import com.pip.net.message.gameaccount.UseIMoneyCardOkMessage;

/**
 * 玩家使用元宝卡之后直接就扣除物品，然后通知认证服务器充值
 * @author Jeffrey
 *
 */
public class IMoneyCardUseCall extends AccountAsyncCall {

	protected Player player;
	protected String cardno;
	protected String password;
	
	private static final Logger log = Logger.getLogger(IMoneyCardUseCall.class);
	
	public IMoneyCardUseCall(ClientSession session,String cardno,String password){
		super(session);
		this.cardno = cardno;
		this.password = password;
		this.player = ((Player)session.getClient());
	}
	
	public void callFinish() throws Exception {
		if(!success){
			log.info("[IMONEYCARDUSE]" + LogUtil.getPlayerLogString(player) + "]FAIL");
            ErrorMessage msg = (ErrorMessage) message;
            ErrorHandler.sendErrorMessage(session, -1, OpCode.USEITEM_CLIENT, ErrorMessages.getErrorMesssage(msg));
		}else{
			log.info("[IMONEYCARDUSE]" + LogUtil.getPlayerLogString(player) + "]CARDNO[" + cardno + "]OK");
			UseIMoneyCardOkMessage msg = (UseIMoneyCardOkMessage) message;
			Player p = (Player)ObjectAccessor.getPlayer(player.id);
			if(p != null){
				p.message(-1, "元宝卡充值成功", -1, -1);
				if (p.session != null) {
					Account account = (Account) p.session.getIdentity();
					if (account != null) {
						account.setLongIMoney(msg.getLongBalance());
						p.addIntPropertyChangedItem(ChangedItem.IMONEY, (int)(account.getLongIMoney() / 100), true, true);
					}
				}
			}
		}
	}

	public void run() {
		Account account = (Account) session.getIdentity();
		log.info("[IMONEYCARDUSE]" + LogUtil.getPlayerLogString(player) + "CARDNO[" + cardno + "]TRY");
		UseIMoneyCardMessage message = new UseIMoneyCardMessage(
				Server.server.gameCode, account.id, account.key, cardno,
				password);
		Server.server.getServiceRegistry().getAccountService().sendAndRegister(
				message, this);
	}

}
