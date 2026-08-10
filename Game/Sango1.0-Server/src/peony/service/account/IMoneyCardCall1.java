package peony.service.account;

import org.apache.log4j.Logger;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.changed.ChangedItem;
import peony.net.ClientSession;
import com.pip.net.message.ErrorMessage;
import com.pip.net.message.gameaccount.UseIMoneyCardMessage;
import com.pip.net.message.gameaccount.UseIMoneyCardOkMessage;

public class IMoneyCardCall1 extends AccountAsyncCall {

	protected Player player;
	protected String cardno;
	protected String password;
	protected GameItem item;
	
	private static final Logger log = Logger.getLogger(IMoneyCardUseCall.class);
	
	public IMoneyCardCall1(ClientSession session,String cardno,String password,GameItem item){
		super(session);
		this.cardno = cardno;
		this.password = password;
		this.player = ((Player)session.getClient());
		this.item = item;
	}
	
	public void callFinish() throws Exception {
		if(!success){
			log.info("[IMONEYCARDUSE]" + LogUtil.getPlayerLogString(player) + "]FAIL");
            ErrorMessage msg = (ErrorMessage) message;
            ErrorHandler.sendErrorMessage(session, -1, OpCode.USEITEM_CLIENT, ErrorMessages.getErrorMesssage(msg));
            if(player!=null){
            	player.message(-1, ErrorMessages.getErrorMesssage(msg), -1, -1);
            	PlayerTransaction tx = player.newTransaction("IMONEYCARD");
    			GameItem item0 = player.bag.removeGameItem(item.template.id, item.instanceId, 1, tx, false);
    			if(item0!=null){
    				tx.commit();
    				log.info("[IMCARDFAILREMOVE]"+LogUtil.getPlayerLogString(player)+"CARDNO["+cardno+"]PASS["+password+"]");
    			}else{
    				tx.rollback();
    			}
            }
		}else{
			log.info("[IMONEYCARDUSE]" + LogUtil.getPlayerLogString(player) + "]CARDNO[" + cardno + "]OK");
			UseIMoneyCardOkMessage msg = (UseIMoneyCardOkMessage) message;
			Player p = (Player)ObjectAccessor.getPlayer(player.id);
			if(p==null)
				p = Server.server.getServiceRegistry().getPlayerService().loadPlayerSilent(player.id);
			if(p!=null){
				p.message(-1, peony.Messages.STRING_00870, -1, -1);
				if (p.session != null) {
					Account account = (Account) p.session.getIdentity();
					if (account != null) {
						account.setLongIMoney(msg.getLongBalance());
						//p.addIntPropertyChangedItem(ChangedItem.IMONEY, (int)(account.getLongIMoney() / 100), true, true);
						String showPrice = p.ibToYuanbao(account.getLongIMoney());
			 			p.addStringPropertyChangedItem(ChangedItem.YUANBAO, showPrice, true);
					}
				}
				PlayerTransaction tx = p.newTransaction("IMONEYCARD");
				GameItem item0 = p.bag.removeGameItem(item.template.id, item.instanceId, 1, tx, false);
				if(item0!=null){
					tx.commit();
				}else{
					tx.rollback();
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
