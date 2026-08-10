package peony.service.jetty;

import java.io.IOException;
import java.text.MessageFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import peony.game.GameItem;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.Server;

public class HangameInviteFriendServlet extends HttpServlet{
    private Logger log = Logger.getLogger(HangameInviteFriendServlet.class);

    private static final int inviteeBonusItemId = 665;
    private static final int inviterBonusItemId = 1183;

    private static final String HANGAME_INVITE_FRIEND_COUNT = "HANGAME_INVITE_FRIEND_COUNT";

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        request.setCharacterEncoding("UTF-8");

        String type = request.getParameter("type");
        String playerId = request.getParameter("player");

        Player p = Server.server.getServiceRegistry().getPlayerService().loadPlayerSilent(Integer.parseInt(playerId));

        if(p == null){
            log.info("[HANGAME_INVITE_FRIEND_BONUS_CALLBACK]TYPE[" + type + "]PLAYERID[" + playerId + "]FAIL");
        }else{
            if("0".equals(type)){
                //inviter bonus
                String targetUserId = request.getParameter("target");
                String todaysucc = request.getParameter("todaysucc");
                String totalsucc = request.getParameter("totalsucc");

                int inviteCount = p.pool.getInt(HANGAME_INVITE_FRIEND_COUNT);
                inviteCount++;
                p.pool.setInt(HANGAME_INVITE_FRIEND_COUNT, inviteCount);
                int itemCount = -1;

                switch(inviteCount){
                    case 1:
                        itemCount = 3;
                        break;
                    case 5:
                        itemCount = 20;
                        break;
                    case 10:
                        itemCount = 45;
                        break;
                    case 15:
                        itemCount = 70;
                        break;
                    case 20:
                        itemCount = 99;
                        break;
                }

                if(itemCount > 0){
                    try{
                        GameItem item = ObjectAccessor.createGameItem(inviterBonusItemId);
                        Server.server.getServiceRegistry().getMailService()
                                        .sendSystemMail(p.id, "系统", "邀请好友奖励", MessageFormat.format("您已成功的邀请了{0}名好友来一起游戏，这是给您的奖励!", inviteCount), 0, item, itemCount, "HANGAME");
                    }catch(Exception e){
                        log.error(e, e);
                    }
                }

                log.info("[HANGAME_INVITE_FRIEND_BONUS_CALLBACK]TYPE[" + type + "]PLAYERID[" + playerId + "]TARGET[" + targetUserId + "]" + "]TODAYSUCC[" + todaysucc + "]TOTALSUCC[" + totalsucc + "]");
            }else{
                //invitee bonus
                try{
                    GameItem item = ObjectAccessor.createGameItem(inviteeBonusItemId);
                    Server.server.getServiceRegistry().getMailService().sendSystemMail(p.id, "系统", "邀请好友奖励", "感谢您进入明珠三国的世界，这是给您的奖励!", 0, item, 10, "HANGAME");
                }catch(Exception e){
                    log.error(e, e);
                }

                log.info("[HANGAME_INVITE_FRIEND_BONUS_CALLBACK]TYPE[" + type + "]PLAYERID[" + playerId + "]");
            }
        }

        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("1");
    }
}
