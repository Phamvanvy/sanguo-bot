package com.pip.itimes.server.connect;

import com.pip.itimes.net.ClientConstants;
import com.pip.itimes.net.Packet;
import com.pip.itimes.net.ServerConstants;
import com.pip.itimes.net.SessionRegistry;
import com.pip.itimes.net.UWAPData;
import com.pip.itimes.net.UWAPSegment;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoSession;
import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import com.pip.itimes.net.Session;

public class AuthSession extends Session {

    private static final Logger log = Logger.getLogger(AuthSession.class);

    private SessionRegistry clientRegistry;
    private Configuration configuration;
    private ClientService clientService;

    public AuthSession(IoSession session){
        super(session);
    }

    public void setConfiguration(Configuration configuration){
        this.configuration = configuration;
    }

    public void setClientService(ClientService clientService){
        this.clientService = clientService;
    }

    public void setClientRegistry(SessionRegistry clientRegistry) {
        this.clientRegistry = clientRegistry;
    }

    private static int segNum = 0;

    public void handle(Packet packet) {

        try {
            UWAPData data = packet.datas[0];
            byte type = data.getAppType();
            log.debug("receive auth seg:" + (type));
            switch (type) {
                case ClientConstants.ERROR:
                    dispatchTo(packet);
                    break;
                case ClientConstants.ACCOUNT_REG_OK:
                    dispatchTo(packet);
                    break;
                case ClientConstants.LOGIN_OK:
                    dispatchTo(packet);
                    break;
                case ServerConstants.RELOGIN_RESULT:
                    dispatchTo(packet);
                    break;
                case ClientConstants.QUICK_REG:
                    dispatchTo(packet);
                    break;
                case ServerConstants.FORCELOGOUT:
                    forceLogout(packet);
                    break;
                case ClientConstants.CMCC_CHARGE:
                    dispatchTo(packet);
                    break;
            }
        } catch (Exception ex) {
            log.error(ex,ex);
        }
    }

    public void created() {

    }


    private void forceLogout(Packet packet) throws Exception{
        UWAPData data = packet.datas[0];
        int accountId = data.readInt();
        ClientSession clientSession = clientService.getClientByAccountId(accountId);
        if(clientSession!=null){
            clientSession.forceClose = true;
            clientSession.close();
        }
    }

    private void dispatchTo(Packet packet) {
        UWAPData data = packet.datas[0];
        ClientSession session = (ClientSession) clientRegistry.getSession(data.
                getSessionId());
        if (session != null) {
            session.handleServer(packet);
        } else {
            try {
                int accountId = data.readInt();
                UWAPSegment seg = new UWAPSegment(ServerConstants.
                                                  PLAYER_LOGOUT, -1,
                                                  data.getSessionId());
                seg.writeInt(accountId);
                seg.writeInt(0);
                seg.writeBoolean(true);
                write(seg);
            } catch (IllegalAccessException ex) {
            }

        }
    }

    public void idle(IdleStatus status){

    }

    public void opened() {
        UWAPSegment seg = new UWAPSegment(ServerConstants.SERVER_LOGIN);
        String serverId = (String) configuration.getProperty(ServerConstants.SERVERID);
        String serverName = (String) configuration.getProperty(ServerConstants.
                SERVERPASSWORD);
        seg.writeString(serverId);
        seg.writeString(serverName);
        write(seg);
    }

    public void closed() {

    }

    public void playerLoginOk(ClientSession stub,UWAPData data){
        UWAPSegment seg = new UWAPSegment(ClientConstants.PLAYER_LOGIN,
                                          data.getSerial(), stub.getSessionId());
        seg.writeInt(stub.getAccountId());
        seg.writeInt(stub.getPlayerId());
        write(seg);
    }

}
