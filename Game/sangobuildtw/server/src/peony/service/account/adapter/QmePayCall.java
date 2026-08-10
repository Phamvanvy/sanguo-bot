package peony.service.account.adapter;

import java.text.MessageFormat;

import org.apache.log4j.Logger;

import peony.game.Actor;
import peony.game.LogUtil;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.changed.ChangedItem;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.account.Account;
import peony.service.account.AccountAsyncCall;

import com.pip.net.message.gameaccount.AddBalanceMessage;
import com.pip.net.message.gameaccount.AddBalanceOkMessage;
import com.pip.net.message.gameaccount.LegacyBuyResultMessage;

public class QmePayCall extends AccountAsyncCall {
	private static final Logger log = Logger.getLogger(QmePayCall.class);

	protected int serial;
	protected int money;
	protected String playerName; // 为他人充值的角色名
	protected Player player;
	protected int playerId;
	protected int accountId;

	public QmePayCall(ClientSession session, Packet pt, Player player,String channel) {
		super(session);
		this.serial = pt.getInt();
		this.money = pt.getInt();
		this.playerName = pt.getString();
		this.player = player;
		this.playerId = player.id;
		this.accountId = player.accountId;
		log.info("[QMEPAY]" + LogUtil.getPlayerLogString(player) + "MONEY["
			+ money + "]PLAYERNAME[" + playerName + "]TRY");
	}

	private void report(String msg) {
		Packet pt = new Packet(OpCode.QME_PAY_SERVER);
		pt.putInt(serial);
		pt.putString(msg);
		session.send(pt);
	}
	
	public void callFinish() throws Exception {
		if (success) {
			log.info("[QMEPAY]" + LogUtil.getPlayerLogString(player) + "MONEY["
					+ money + "]PLAYERNAME[" + playerName + "]ChargeOK");
			if (playerName.length() != 0) {
				report(MessageFormat.format("您已為{0}成功購買{1}元寶.", playerName,money));
			} else {
				report(MessageFormat.format("您已成功購買{0}元寶.", money));
				
				// 如果是给自己购买的，同步余额
				AddBalanceOkMessage msg = (AddBalanceOkMessage)message;
				Account a = (Account)player.session.getIdentity();
				int oldIMoney = a.getIMoney();
				a.setIMoney(msg.getValue() + oldIMoney);
				player.addIntPropertyChangedItem(ChangedItem.IMONEY, a.getIMoney() / 100, true, true);
			}
		} else {
			report("購買時發生系統錯誤,請聯系GM處理.");
		}
	}
	
	public void run() {
		if (playerName.length() != 0) {
			Actor actor = Server.server.getServiceRegistry()
					.getActorCacheService().find(playerName);
			if (actor == null) {
				report("沒有找到目標角色");
				return;
			}
			accountId = actor.accountId;
		}
		
		try {
			// 向QME平台发起扣费请求
			Account acc = (Account)session.getIdentity();
			QmeAccount qmeAcc = (QmeAccount)acc.getMetaInfo();
			QmeAdapter.pay(qmeAcc.qbID, money);
			log.info("[QMEPAY]" + LogUtil.getPlayerLogString(player) + "MONEY["
					+ money + "]PLAYERNAME[" + playerName + "]OK");
			
			// 扣费成功，向认证服务器请求添加元宝（1元宝=36i，由于这里单位是0.01i，所以需要乘以3600）
			AddBalanceMessage message = new AddBalanceMessage(accountId, money * 3600, "QME_PAY");
	        Server.server.getServiceRegistry().getAccountService().sendAndRegister(message, this);
		} catch (QmeException e) {
			report(e.getMessage());
		} catch (Exception e1) {
			report("帳號异常");
		}
	}
}
