package com.pip.itimes.server.auth;

import org.apache.mina.common.IoSession;
import com.pip.itimes.net.Packet;
import org.apache.mina.common.IdleStatus;
import com.pip.itimes.net.UWAPData;
import com.pip.itimes.net.ServerConstants;
import com.pip.itimes.server.bean.Account;
import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.net.Session;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class WorldSession extends Session{

    private AccountService accountService;

    public WorldSession(IoSession session) {
        super(session);
    }

    public void setAccountService(AccountService accountService){
        this.accountService = accountService;
    }

    public void closed() {
    }

    public void created() {
    }

    public void handle(Packet packet) {
        try {
            UWAPData data = packet.datas[0];
            byte type = data.getAppType();
            switch (type) {
                case ServerConstants.FORBID:
                    forbid(data);
                    break;
                case ServerConstants.ADMIN_ACCOUNTINFO:
                    accountinfo(data);
                    break;
                case ServerConstants.ADMIN_MODIFYACCOUNT:
                    modifyaccount(data);
                    break;
            }
        } catch (Exception ex) {
        }
    }

    private void modifyaccount(UWAPData data) throws Exception{
        int accountId = data.readInt();
        String password = data.readString();
        Account a = accountService.loadAccountById(accountId);
        a.setPassword(password);
        accountService.saveAccount(a);
    }


    private void accountinfo(UWAPData data) throws Exception {
        int id = data.readInt();
        if (id != -1) {
            Account account = accountService.loadAccountById(id);
            if (account != null) {
                UWAPSegment seg = new UWAPSegment(ServerConstants.
                                                  ADMIN_ACCOUNTINFO,
                                                  data.getSerial(),
                                                  data.getSessionId());
                seg.writeInt(account.getId());
                seg.writeString(account.getUserName());
                seg.writeString(account.getPassword());
                seg.writeString(account.getPhone());
                write(seg);
            }
        } else {
            Account account = accountService.loadAccountByName(data.readString());
            if (account != null) {
                UWAPSegment seg = new UWAPSegment(ServerConstants.
                                                  ADMIN_ACCOUNTINFO,
                                                  data.getSerial(),
                                                  data.getSessionId());
                seg.writeInt(account.getId());
                seg.writeString(account.getUserName());
                seg.writeString(account.getPassword());
                seg.writeString(account.getPhone());
                write(seg);
            }
        }
    }

    private void forbid(UWAPData data) throws Exception{
        byte type = data.readByte();
        int accountId = data.readInt();
        if(type==1){
            String cause = data.readString();
            Account a = accountService.loadAccountById(accountId);
            a.setValid(false);
            a.setCause(cause);
            accountService.saveAccount(a);
        }
        else if(type==2){
            Account a = accountService.loadAccountById(accountId);
            if(!a.getValid()){
                a.setValid(true);
                accountService.saveAccount(a);
            }
        }
    }

    public void opened() {
    }

    public void idle(IdleStatus status) {
    }
}
