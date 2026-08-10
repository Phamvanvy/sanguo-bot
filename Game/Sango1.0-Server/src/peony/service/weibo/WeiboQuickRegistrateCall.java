package peony.service.weibo;

import java.util.HashMap;

import com.pip.weibo.WeiBoSystem;
import com.pip.weibo.WeiboSMSProvider;
import peony.common.ClientSessionAsyncCall;
import peony.game.LogUtil;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;

public class WeiboQuickRegistrateCall extends ClientSessionAsyncCall{
	
	protected int serial;
	protected HashMap<String, WeiboSMSProvider> tmp;
	protected Player p;
	protected String providerName;
	protected String SMSNumber;

	public WeiboQuickRegistrateCall(ClientSession session,Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.tmp = new HashMap<String,WeiboSMSProvider>();
		this.p = (Player)session.getClient();
	}

	public void callFinish() throws Exception {
		if(success){
			LogUtil.logWeiboQuickRegistrate(p);
			Packet pt = new Packet(OpCode.QUICKREGISTRATE_WEIBO_SERVER);
			pt.putInt(serial);
			pt.putString(providerName);
			pt.putString(SMSNumber);
			session.send(pt);
		}
	}

	public void run() {
		if(p!=null){
			LogUtil.logWeiboQuickRegistrateTry(p);
			tmp = WeiBoSystem.getSMSProviders();
			String name = WeiboSMSProvider.CMCC;
			if(tmp!=null && tmp.size()>0){
				for(WeiboSMSProvider s : tmp.values()){
					if(s.getProiderName().equals(name)){
						providerName = s.getProiderName();
						SMSNumber = s.getSMSNumber();
						break;
					}
				}
			}
		}
		addToClientSession();
	}
}
