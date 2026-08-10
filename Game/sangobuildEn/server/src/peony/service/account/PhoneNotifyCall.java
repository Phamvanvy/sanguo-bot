package peony.service.account;


import org.apache.log4j.Logger;

import peony.game.Identity;
import peony.game.Server;
import peony.net.ClientSession;

import com.pip.net.message.gameaccount.PhoneNotifyMessage;

public class PhoneNotifyCall extends AccountAsyncCall {
	
	private static final Logger log = Logger.getLogger(PhoneNotifyCall.class);
	
	private String phone;
	
	public PhoneNotifyCall(ClientSession session,String phone){
		super(session);
		this.phone = phone;
	}

	public void callFinish() throws Exception {
		
	}

	public void run() {
		Identity identify = session.getIdentity();
		if (identify != null&&phone!=null&&phone.length()>0) {
			log.info("[PHONENOTIFY]ACCOUNT["+identify.getId()+"]PHONE["+this.phone+"]");
			Server.server.getServiceRegistry().getAccountService().postMessage(
					new PhoneNotifyMessage(identify.getId(), cutPhone(phone)));
		}
	}
	
	
    public static String cutPhone(String info){
        String search = "mobile-no=\"";
        int pos = info.indexOf(search);
        if(pos == -1){
            return "";
        }
        pos += search.length();
        int pos2 = info.indexOf('"', pos);
        if(pos2 == -1){
            return "";
        }
        return info.substring(pos, pos2);
    }	

}
