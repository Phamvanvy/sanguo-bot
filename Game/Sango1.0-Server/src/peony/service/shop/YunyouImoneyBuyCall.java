package peony.service.shop;

import java.util.concurrent.atomic.AtomicInteger;
import com.bonc.YyGameSDK;
import com.pip.net.message.gameaccount.AddBalanceMessage;
import com.pip.net.message.gameaccount.AddBalanceOkMessage;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.changed.ChangedItem;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.account.AccountAsyncCall;

public class YunyouImoneyBuyCall extends AccountAsyncCall {

	protected Player player;
	protected int serial;
	protected int buyImoney;
	
	protected int decYunyouMoney;
	
	public static AtomicInteger orderMachine = new AtomicInteger(1);
	public static String callBackUrl = "http://www.baidu.com";
	
	public YunyouImoneyBuyCall(ClientSession session, Packet packet) {
		super(session);
		player = (Player)session.getClient();
		serial = packet.getInt();
		buyImoney = packet.getInt();
		decYunyouMoney = buyImoney * 10;
	}

	public void callFinish() throws Exception {
		if (success) {
			// 添加元宝成功
			AddBalanceOkMessage msg = (AddBalanceOkMessage)message;
			long oldIMoney = player.getAccount().getLongIMoney();
			player.getAccount().setLongIMoney(msg.getValue() + oldIMoney);
			String showPrice = player.ibToYuanbao(player.getAccount().getLongIMoney());
 			player.addStringPropertyChangedItem(ChangedItem.YUANBAO, showPrice, true);
 			
 			Packet pt = new Packet(OpCode.YUNYOU_BUYIMONEY_SERVER);
			pt.putInt(serial);
			session.send(pt);
		} else {
			// 添加元宝失败（严重错误！）
			ErrorHandler.sendErrorMessage(session, serial, OpCode.YUNYOU_BUYIMONEY_CLIENT, errorMessage);
		}
	}

	public void run() {
		try {
			YyGameSDK yyGameSDK = new YyGameSDK();
			String order = Integer.toString(orderMachine.incrementAndGet());
			String remark = "";
			boolean flag = yyGameSDK.doPay(player.getAccount().getYySessionId(), order, callBackUrl, String.valueOf(decYunyouMoney), remark);
			if (flag) {
				Server.server.getServiceRegistry().getAccountService().sendAndRegister(new AddBalanceMessage(player.accountId,buyImoney*3600,"YUNYOU","",buyImoney),this);
			} else {
				ErrorHandler.sendErrorMessage(session, serial, OpCode.YUNYOU_BUYIMONEY_CLIENT, "余额不足");
			}
		} catch (Exception e) {
			ErrorHandler.sendErrorMessage(session, serial, OpCode.YUNYOU_BUYIMONEY_CLIENT, "系统错误");
		}
	}

}
