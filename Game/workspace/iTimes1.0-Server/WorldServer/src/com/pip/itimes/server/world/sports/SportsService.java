package com.pip.itimes.server.world.sports;

import java.util.*;
import com.pip.itimes.server.world.WorldPlayer;
import com.pip.itimes.server.world.MailService;
import com.pip.itimes.server.world.ChatService;
import com.pip.itimes.server.world.BbsService;
import com.pip.itimes.server.stage.Changed;
import java.text.SimpleDateFormat;
import com.pip.itimes.server.stage.Items;
import com.pip.itimes.server.stage.ItemUtils;
import com.pip.itimes.server.stage.IItem;
import org.apache.log4j.Logger;
import com.pip.itimes.server.dao.*;
import com.pip.itimes.server.world.TongService;

public class SportsService implements Runnable {

    private Map<String, Sport> sports = new HashMap<String, Sport>();

    private MailService mailService;
    private ChatService chatService;
    private BbsService bbsService;
    private TongService tongService;

    private static final Logger log = Logger.getLogger(SportsService.class);

    private static SimpleDateFormat format = new SimpleDateFormat("MM月dd日hh点mm分");

    public SportsService() {
        new Thread(this, "Sports").start();
    }

    public void start(long start, long end, long interval, String type, int bbsId) {
        String st = getSportType(type);
        if ("p".equals(st)) {
            PSport sport = new PSport(start, end, interval, type, bbsId);
            sports.put(sport.getName(), sport);
        } else if ("g".equals(st)) {
            GSport sport = new GSport(start, end, interval, type, bbsId);
            sports.put(sport.getName(), sport);
        }//mengjie add climb
        else if ("c".equals(st)){
        	ClimbSport sport = new ClimbSport(start, end, interval, type, bbsId);
            sports.put(sport.getName(), sport);
        }
    }

    protected String getSportType(String type) {
        if (type.startsWith("p-")) {
            return "p";
        } else if (type.startsWith("g-")) {
            return "g";
        }//mengjie add climb
        else if (type.startsWith("c-")) {
        	return "c";
        }
        return "";
    }

    public Sport getSport(String name) {
        return sports.get(name);
    }

    public void play(WorldPlayer player, String name, Changed changed) throws SportException {
        if (player.getTeam() != null)
            throw new SportException("你现在处于组队状态，不能报名");
        Sport sport = getSport(name);
        if (sport != null) {
            sport.play(player, changed);
        } else {
            throw new SportException("比赛没有开始");
        }
    }

    public SportRecord over(WorldPlayer player, String name) throws SportException {
        Sport sport = getSport(name);
        if (sport != null) {
            return sport.over(player);
        } else {
            throw new SportException("比赛没有开始");
        }
    }

    public void run() {
        while (true) {
            try {
                Iterator<Sport> ite = sports.values().iterator();
                long current = System.currentTimeMillis();
                while (ite.hasNext()) {
                    Sport sport = ite.next();
                    if (current >= sport.start && sport.status == Sport.STATUS_INIT) {
                        sport.start();
                    }
                    if (current >= sport.end && sport.status == Sport.STATUS_STARTED) {
                        ite.remove();
                        sport.end();
                        ended(sport);
                    }
                    if ((current - sport.chatTime) > 0) {
                        sport.setNextChatTime();
                        chatService.sendSystemMessage(sport.getChatString());
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            try {
                Thread.sleep(2 * 1000L);
            } catch (InterruptedException ex1) {
            }
        }
    }

    public void ended(Sport sport) {
        SportResult[] ret = sport.getFirst10();
        if (sport instanceof PSport) {
            IItem item1 = Items.getTemplate(560003).newInstance();
            byte[] itemBytes1 = ItemUtils.item2dbAttachment(item1, 1);
            IItem item2 = Items.getTemplate(560004).newInstance();
            byte[] itemBytes2 = ItemUtils.item2dbAttachment(item2, 1);
            IItem item3 = Items.getTemplate(560005).newInstance();
            byte[] itemBytes3 = ItemUtils.item2dbAttachment(item3, 1);
            IItem item4 = Items.getTemplate(560009).newInstance();
            byte[] itemBytes4 = ItemUtils.item2dbAttachment(item4, 1);
            String title = getDateString(sport.start, sport.end) + "个人决赛奖励";
            String content = getDateString(sport.start, sport.end) + "个人决赛中，恭喜你获得了不错的成绩，根据你的名次发放奖章。";
            if (ret.length > 0){
                mailService.sendMail(ret[0].id, ret[0].name, -1, "系统", title, content, itemBytes1, 0, true);
                log.info("ID["+ret[0].id+"]Sport[p]Order[1]");
            }
            if (ret.length > 1){
                mailService.sendMail(ret[1].id, ret[1].name, -1, "系统", title, content, itemBytes2, 0, true);
                log.info("ID["+ret[1].id+"]Sport[p]Order[2]");
            }
            if (ret.length > 2){
                mailService.sendMail(ret[2].id, ret[2].name, -1, "系统", title, content, itemBytes3, 0, true);
                log.info("ID["+ret[2].id+"]Sport[p]Order[3]");
            }
            if (ret.length > 3) {
                for (int i = 3; i < ret.length; i++) {
                    mailService.sendMail(ret[i].id, ret[i].name, -1, "系统", title, content, itemBytes4, 0, true);
                    log.info("ID["+ret[i].id+"]Sport[p]Order["+(i+1)+"]");
                }
            }
            try {
                bbsService.addBbs(sport.bbsId, -1, "系统", getDateString(sport.start, sport.end) + "个人决赛成绩",
                                  getDateString(sport.start, sport.end) + "个人决赛成绩" + getPBbsContent(ret), 100);
            } catch (DataAccessException ex) {
                log.error(ex, ex);
            }
        } else if (sport instanceof GSport) {
            IItem item1 = Items.getTemplate(560006).newInstance();
            byte[] itemBytes1 = ItemUtils.item2dbAttachment(item1, 1);
            IItem item2 = Items.getTemplate(560007).newInstance();
            byte[] itemBytes2 = ItemUtils.item2dbAttachment(item2, 1);
            IItem item3 = Items.getTemplate(560008).newInstance();
            byte[] itemBytes3 = ItemUtils.item2dbAttachment(item3, 1);
            IItem item4 = Items.getTemplate(560010).newInstance();
            byte[] itemBytes4 = ItemUtils.item2dbAttachment(item4, 1);
            Object[] itemBytes = new Object[4];
            IItem item5 = Items.getTemplate(200200).newInstance();
            itemBytes[0] = ItemUtils.item2dbAttachment(item5, 8);
            itemBytes[1] = ItemUtils.item2dbAttachment(item5, 6);
            itemBytes[2] = ItemUtils.item2dbAttachment(item5, 4);
            itemBytes[3] = ItemUtils.item2dbAttachment(item5, 2);
            String title = getDateString(sport.start, sport.end) + "公会决赛奖励";
            String content = getDateString(sport.start, sport.end) + "公会决赛中，恭喜你获得了不错的成绩，根据你的名次发放奖章。";
            String pContent = getDateString(sport.start, sport.end) + "公会决赛中，恭喜你获得了不错的成绩，，你为公会的获胜作出了努力，特此奖励！";
            if (ret.length > 0) {
                int id = tongService.getTongOwnerId(ret[0].id);
                if (id != -1){
                    mailService.sendMail(id, "", -1, "系统", title, content, itemBytes1, 0, true);
                    log.info("ID["+id+"]Sport[g]Order[1]");
                }
                for (int i = 0; i < ret[0].records.length && i < 3; i++) {
                    mailService.sendMail(ret[0].records[i].playerId, ret[0].records[i].playerName, -1, "系统", title,
                                         pContent, (byte[]) itemBytes[0], 0, true);
                }
            }
            if (ret.length > 1) {
                int id = tongService.getTongOwnerId(ret[1].id);
                if (id != -1){
                    mailService.sendMail(id, "", -1, "系统", title, content, itemBytes2, 0, true);
                    log.info("ID["+id+"]Sport[g]Order[2]");
                }
                for (int i = 0; i < ret[1].records.length && i < 3; i++) {
                    mailService.sendMail(ret[1].records[i].playerId, ret[1].records[i].playerName, -1, "系统", title,
                                         pContent, (byte[]) itemBytes[1], 0, true);
                }
            }
            if (ret.length > 2) {
                int id = tongService.getTongOwnerId(ret[2].id);
                if (id != -1){
                    mailService.sendMail(id, "", -1, "系统", title, content, itemBytes3, 0, true);
                    log.info("ID["+id+"]Sport[g]Order[3]");
                }
                for (int i = 0; i < ret[2].records.length && i < 3; i++) {
                    mailService.sendMail(ret[2].records[i].playerId, ret[2].records[i].playerName, -1, "系统", title,
                                         pContent, (byte[]) itemBytes[2], 0, true);
                }
            }
            if (ret.length > 3) {
                for (int i = 3; i < ret.length; i++) {
                    int id = tongService.getTongOwnerId(ret[i].id);
                    if (id != -1){
                        mailService.sendMail(id, "", -1, "系统", title, content, itemBytes4, 0, true);
                        log.info("ID["+id+"]Sport[g]Order["+(i+1)+"]");
                    }
                    for (int j = 0; j < ret[i].records.length && j < 3; j++) {
                        mailService.sendMail(ret[i].records[j].playerId, ret[i].records[j].playerName, -1, "系统", title,
                                             pContent, (byte[]) itemBytes[3], 0, true);
                    }
                }
            }
            try {
                bbsService.addBbs(sport.bbsId, -1, "系统", getDateString(sport.start, sport.end) + "公会决赛成绩",
                                  getDateString(sport.start, sport.end) + "公会决赛成绩" + getPBbsContent(ret), 100);
            } catch (DataAccessException ex) {
                log.error(ex, ex);
            }
        }//mengjie add climb
        else if (sport instanceof ClimbSport) {
        	ret = sport.getFirst20();
        	
            IItem item = Items.getTemplate(200495).newInstance();
            byte[] itemBytes1 = ItemUtils.item2dbAttachment(item, 4);
            byte[] itemBytes2 = ItemUtils.item2dbAttachment(item, 2);
            byte[] itemBytes3 = ItemUtils.item2dbAttachment(item, 1);
            IItem item4 = Items.getTemplate(200496).newInstance();
            byte[] itemBytes4 = ItemUtils.item2dbAttachment(item4, 1);
            String title = getDateString(sport.start, sport.end) + "重阳登高大赛奖励";
            String content = getDateString(sport.start, sport.end) + "重阳登高大赛中，恭喜你获得了不错的成绩，根据你的名次发放奖章。";
            if (ret.length > 0){
                mailService.sendMail(ret[0].id, ret[0].name, -1, "系统", title, content, itemBytes1, 0, true);
                log.info("ID["+ret[0].id+"]Sport[p]Order[1]");
            }
            if (ret.length > 1){
                mailService.sendMail(ret[1].id, ret[1].name, -1, "系统", title, content, itemBytes1, 0, true);
                log.info("ID["+ret[1].id+"]Sport[p]Order[2]");
            }
            if (ret.length > 2){
                mailService.sendMail(ret[2].id, ret[2].name, -1, "系统", title, content, itemBytes1, 0, true);
                log.info("ID["+ret[2].id+"]Sport[p]Order[3]");
            }
            
            if (ret.length > 3){
                mailService.sendMail(ret[3].id, ret[3].name, -1, "系统", title, content, itemBytes2, 0, true);
                log.info("ID["+ret[2].id+"]Sport[p]Order[4]");
            }
            if (ret.length > 4){
                mailService.sendMail(ret[4].id, ret[4].name, -1, "系统", title, content, itemBytes2, 0, true);
                log.info("ID["+ret[2].id+"]Sport[p]Order[5]");
            }
            if (ret.length > 5) {
                for (int i = 5; i < 10; i++) {
                    mailService.sendMail(ret[i].id, ret[i].name, -1, "系统", title, content, itemBytes3, 0, true);
                    log.info("ID["+ret[i].id+"]Sport[p]Order["+(i+1)+"]");
                }
            }
            
            if (ret.length > 10) {
                for (int i = 10; i < ret.length; i++) {
                    mailService.sendMail(ret[i].id, ret[i].name, -1, "系统", title, content, itemBytes4, 0, true);
                    log.info("ID["+ret[i].id+"]Sport[p]Order["+(i+1)+"]");
                }
            }
            try {
                bbsService.addBbs(sport.bbsId, -1, "系统", getDateString(sport.start, sport.end) + "重阳登高大赛成绩",
                                  getPBbsContent(ret), 100);
                chatService.sendSystemMessage(getDateString(sport.start, sport.end) + "重阳登高大赛结束了，已报名但未完成比赛的玩家将自动结束比赛（不计成绩），请各位参赛的玩家到活动专区的公告板查看成绩.");
                
            } catch (DataAccessException ex) {
                log.error(ex, ex);
            }
        }
    }

    public String getPBbsContent(SportResult[] srs) {
        StringBuilder sb = new StringBuilder(2000);
        if (srs.length > 0) {
            sb.append("\n金牌:");
            sb.append(srs[0].name);
            sb.append("\n");
        }
        if (srs.length > 1) {
            sb.append("银牌:");
            sb.append(srs[1].name);
            sb.append("\n");
        }
        if (srs.length > 2) {
            sb.append("铜牌:");
            sb.append(srs[2].name);
            sb.append("\n");
        }
        if (srs.length > 3) {
            sb.append("其他前十选手:");
            int count = srs.length;
            if(count > 10) count = 10;
            for (int i = 3; i < count; i++) {
                sb.append(srs[i].name);
                sb.append(";");
            }
        }
        return sb.toString();
    }

    public String getGBbsContent(SportResult[] srs) {
        StringBuilder sb = new StringBuilder(2000);
        if (srs.length > 0) {
            sb.append("金牌:");
            sb.append(srs[0].name);
            sb.append("\n");
        }
        if (srs.length > 1) {
            sb.append("银牌:");
            sb.append(srs[1].name);
            sb.append("\n");
        }
        if (srs.length > 2) {
            sb.append("铜牌:");
            sb.append(srs[2].name);
            sb.append("\n");
        }
        if (srs.length > 3) {
            sb.append("其他前十公会:");
            for (int i = 3; i < srs.length; i++) {
                sb.append(srs[i].name);
                sb.append(";");
            }
        }
        return sb.toString();
    }

    public String getDateString(long start, long end) {
        Date s = new Date(start);
        Date e = new Date(end);
        return "[" + format.format(s) + "]至[" + format.format(e) + "]";
    }

    public void setChatService(ChatService chatService) {
        this.chatService = chatService;
    }

    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }

    public void setBbsService(BbsService bbsService) {
        this.bbsService = bbsService;
    }

    public void setTongService(TongService tongService) {
        this.tongService = tongService;
    }

}
