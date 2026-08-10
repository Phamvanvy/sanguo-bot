package peony.service.account.adapter;

import org.apache.log4j.Logger;
import peony.game.OpCode;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.account.Account;
import peony.service.account.AccountAsyncCall;

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
