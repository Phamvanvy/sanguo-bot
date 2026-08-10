package peony.service.account.adapter;

import org.apache.log4j.Logger;

import peony.game.Actor;
import peony.game.ErrorHandler;
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

public class QmeQueryBalanceCall extends AccountAsyncCall {
	private static final Logger log = Logger.getLogger(QmeQueryBalanceCall.class);

	public QmeQueryBalanceCall(ClientSession session, Packet pt) {
		super(session);
	}

	private void report(int value) {
		Packet pt = new Packet(OpCode.QME_QUERY_BALANCE_SERVER);
		pt.putInt(value);
		session.send(pt);
	}
	
	public void callFinish() throws Exception {
	}
	
	public void run() {
		try {
			// 向QME平台发起查询请求
			Account acc = (Account)session.getIdentity();
			QmeAccount qmeAcc = (QmeAccount)acc.getMetaInfo();
			report(QmeAdapter.queryBalance(qmeAcc.qbID));
		} catch (Exception e) {
			report(0);
		}
	}
}
