package peony.service.account.adapter;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;

import peony.game.Server;
import peony.net.ClientSession;
import peony.service.account.AccountAsyncCall;

public class HangameInviteFriendCall extends AccountAsyncCall{
    private static final Logger log = Logger.getLogger(HangameInviteFriendCall.class);

    private String source;
    private int playerId;
    private String target;

    public HangameInviteFriendCall(ClientSession session, String source, int playerId, String target){
        super(session);

        this.source = source;
        this.playerId = playerId;
        this.target = target;
    }

    public void callFinish() throws Exception{
    }

    public void run(){
        String hangame_add_invite_url = Server.server.getConfig().configurationAt("hangame").getString("add_invite_url");
        
        log.info("[HANGAME_INVITE_FRIEND]SOURCE[" + source + "]PLAYERID[" + playerId + "]TARGET[" + target + "]");
        
        PostMethod method;
        method = new PostMethod(hangame_add_invite_url);
        method.getParams().setContentCharset("utf-8");
        method.addRequestHeader("Connection", "close");
        method.setParameter("source", source);
        method.setParameter("player", String.valueOf(playerId));
        method.setParameter("gamecode", Server.server.gameCode);
        method.setParameter("target", target);

        try{
            HttpClient httpclient = new HttpClient();
            httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
            httpclient.getParams().setSoTimeout(30000);
            int code = httpclient.executeMethod(method);
        }catch(Exception ex){
            log.error(ex, ex);
        }finally{
            method.releaseConnection();
        }
    }
}
