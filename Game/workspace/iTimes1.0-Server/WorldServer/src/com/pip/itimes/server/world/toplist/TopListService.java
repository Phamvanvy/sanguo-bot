package com.pip.itimes.server.world.toplist;


import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import com.pip.itimes.server.world.CampBattleService;
import com.pip.itimes.server.world.ChatService;
import com.pip.itimes.server.world.MailService;
import com.pip.itimes.server.world.PlayerService;
import com.pip.itimes.server.world.Server;
import com.pip.itimes.server.world.ShoutService;
import com.pip.itimes.server.world.TongService;
import com.pip.itimes.server.world.TwelfthLunarService;
import com.pip.itimes.server.world.VoteService;
import com.pip.itimes.server.world.WorldPlayer;
import com.pip.itimes.server.world.chr.ChristmasConfig;
import com.pip.itimes.server.world.game.HouseInstanceModel;

public class TopListService implements Runnable{
    private static final Logger log = Logger.getLogger(TopListService.class);

    public TongTopList tongTopList;
    public PlayerTopList playerTopList;
    public HouseTopList houseTopList;
    private PlayerService playerService;
    private ChatService chatService;
    private VoteService voteService;
    private CampBattleService campBattleService;
    private ShoutService shoutService;
    private TwelfthLunarService twelfthLunarService;
    public static final String TOP_LIST_NO_DATA_MESSAGE = "暂无数据，请稍后再试";
    
    public TopListService(){
        tongTopList = new TongTopList();
        playerTopList = new PlayerTopList();
        houseTopList = new HouseTopList();
    }

    public void start(){
        new Thread(this).start();
    }
    
    public void setTongService(TongService tongService){
        tongTopList.setTongService(tongService);
    }
    
    public void setPlayerService(PlayerService playerService){
        playerTopList.setPlayerServcie(playerService);
        this.playerService = playerService;
    }
    public void setMailService(MailService mailService){
        playerTopList.setMailService(mailService);
    }
    
    public void setVoteService (VoteService voteService) {
    	this.voteService = voteService;
    }
    
    public void setHouseInstanceModel(HouseInstanceModel houseInstanceModel){
        houseTopList.setHouseInstanceModel(houseInstanceModel);
    }
    
	public void setChatService(ChatService chatService) {
		this.chatService = chatService;
	}
	
	public void setCampBattleService (CampBattleService campBattleService) {
		this.campBattleService = campBattleService;
	}
	
	public void setShoutService (ShoutService shoutService) {
		this.shoutService = shoutService;
	}
	
	public void setTwelfthLunarService (TwelfthLunarService twelfthLunarService) {
		this.twelfthLunarService = twelfthLunarService;
	}

	public void run(){
        while(true){
            try{
                Thread.sleep(2 * 60 * 1000L);
                
                twelfthLunarService.setCurrentSegment(twelfthLunarService.checkEffectivePeriod());
                twelfthLunarService.prizesAfterEvent();
                shoutService.setCurrentSegment(shoutService.checkEffectivePeriod());
                voteService.prizesAfterEvent();
                ChristmasConfig.setCurrentSegment(ChristmasConfig.calcCurrentSegment());
                campBattleService.sendPrizes();
                tongTopList.processTopList();
                playerTopList.processTopList();
                houseTopList.processTopList();
                
                //上线5分钟后，提示技能点
	            WorldPlayer[] p = playerService.getPlayers();
	            for(int i=0;i<p.length;i++){
              	//在线
              	if (p[i].getState() == WorldPlayer.ONLINE && p[i].getPlayer() != null && Server.playerid_point_info.containsKey(p[i].getId())){
              		//未分配属性点大于0
              		if ( p[i].getPoint() > 0){
              			Date date_tmp = new Date();
              			//登陆5分钟
              			if ((date_tmp.getTime() - p[i].getLastLoginTime().getTime()) > 5 * 60 * 1000L){
//              				byte[] bytes = Server.instance.stageService.getTaskBytes((short) 31019, new String[] {"您还未分配战斗技能点，想变得更强就赶紧按菜单中的人物中的战斗技能进行操作吧。"});
              				chatService.sendPrivateMessage(-1,"系统",p[i].getId(),"您有"+Server.playerid_point_info.get(p[i].getId())+"点战斗技能点尚未分配，在人物-战斗技能菜单中可以进行分配，学会技能以后角色在战斗中将表现的更加出色！");
              				Server.playerid_point_info.remove(p[i].getId());
              			}
              		}
              	}
              }

                //cmcc
//                if(Server.iMoneyType == Server.IMONEY_TYPE_CMCC){
//                	WorldPlayer[] p = playerService.getPlayers();
//                    for(int i=0;i<p.length;i++){
//                    	//在线
//                    	if (p[i].getState() == WorldPlayer.ONLINE && p[i].getPlayer() != null){
//                    		//吉林用户
//                    		if ( !"".equals(p[i].Cmcc_list)){
//                    			Date date_tmp = new Date();
//                    			//登陆5分钟
//                    			if ((date_tmp.getTime() - p[i].getLastLoginTime().getTime()) > 5 * 60 * 1000L){
//                    				if (!Server.cmcc_jilin_playerid.containsKey(p[i].Cmcc_list)){
//                    					Server.cmcc_jilin_count++;
//                    					int jilin_count = 0;
//                    					jilin_count = Server.cmcc_jilin_count * 2;
////                    					jilin_count = Server.cmcc_jilin_count * 10 + (Server.cmcc_jilin_count % 2 != 0? 4: 8);
//                    					
////                    					if (Server.cmcc_jilin_count<10){
////                    						jilin_count = Server.cmcc_jilin_count + 10;
////                    					}else if (Server.cmcc_jilin_count<100){
////                    						jilin_count = Server.cmcc_jilin_count + 100;
////                    					}else if (Server.cmcc_jilin_count<1000){
////                    						jilin_count = Server.cmcc_jilin_count + 1000;
////                    					}else if (Server.cmcc_jilin_count<10000){
////                    						jilin_count = Server.cmcc_jilin_count + 10000;
////                    					}
//                    					
//                        				if (jilin_count<5000){
//                        					UWAPSegment seg_cmcc = new UWAPSegment(ServerConstants.CMCC_SEND_MESSAGE);
//                        					int mod_tmp = jilin_count%100;
//                            				if (mod_tmp == 88){
//                            					//尾号是88
//                            					/**
//                        					     * 卓望版本，向用户发送短信通知。
//                        					     * userId           String          用户登录ID
//                        					     * message          String          通知消息
//                        					     */
//                                            	seg_cmcc.writeString(p[i].Cmcc_list); 
//                                            	seg_cmcc.writeString("尊敬的用户,感谢您一直支持我们的游戏,幻想最新圣诞大餐优先奉献,宠物,经验,奖品不断,登陆游戏,即可获得圣诞奖励！游戏中使用推荐好友功能,更可获得丰富奖品！");
//                                                Server.instance.authSession.write(seg_cmcc);
////                                                
////                                                seg_cmcc = new UWAPSegment(ServerConstants.CMCC_SEND_MESSAGE);
////                                                seg_cmcc.writeString(p[i].Cmcc_list); 
////                                            	seg_cmcc.writeString("您是今天第"+jilin_count+"位登陆幻想的吉林移动用户，得到50元话费，所获话费将由吉林移动于10个工作日内存入您帐户。欢迎您明天继续登陆，争取再次获奖，客服电话10086");
////                                                Server.instance.authSession.write(seg_cmcc);
////                                                
////                                                seg_cmcc = new UWAPSegment(ServerConstants.CMCC_SEND_MESSAGE);
////                                                seg_cmcc.writeString(p[i].Cmcc_list); 
////                                            	seg_cmcc.writeString("发短信“hx”到10658838，点击回复信息可第一时间下载最新版幻想i时代");
////                                                Server.instance.authSession.write(seg_cmcc);
//                                                
//                            				}else{
//                            					int mod_tmp1 = jilin_count%10;
//                            					if (mod_tmp1 == 8){
//                            						//尾号是8
//                            						/**
//                            					     * 卓望版本，向用户发送短信通知。
//                            					     * userId           String          用户登录ID
//                            					     * message          String          通知消息
//                            					     */   
//                            						seg_cmcc.writeString(p[i].Cmcc_list); 
//                                                	seg_cmcc.writeString("尊敬的用户,感谢您一直支持我们的游戏,幻想最新圣诞大餐优先奉献,宠物,经验,奖品不断,登陆游戏,即可获得圣诞奖励！游戏中使用推荐好友功能,更可获得丰富奖品！");
//                                                    Server.instance.authSession.write(seg_cmcc);
////                            						seg_cmcc.writeString(p[i].Cmcc_list); 
////                                                	seg_cmcc.writeString("亲爱的用户, “新手升级得话费”、“推荐好友得话费”活动延期至12月15日，最后一批中奖用户话费赠送时间为12月25日前。");
////                                                    Server.instance.authSession.write(seg_cmcc);
////                                                    seg_cmcc = new UWAPSegment(ServerConstants.CMCC_SEND_MESSAGE);
////                                                    seg_cmcc.writeString(p[i].Cmcc_list); 
////                            						seg_cmcc.writeString("您是今天第"+jilin_count+"位登陆幻想的吉林移动用户，得到10元话费，所获话费将由吉林移动于10个工作日内存入您帐户。欢迎您明天继续登陆，争取再次获奖，客服电话10086");
////                                                    Server.instance.authSession.write(seg_cmcc);
////                                                    seg_cmcc = new UWAPSegment(ServerConstants.CMCC_SEND_MESSAGE);
////                                                    seg_cmcc.writeString(p[i].Cmcc_list); 
////                                                	seg_cmcc.writeString("幻想推荐好友功能已上线，登陆游戏后点人物背包中的“推荐符”即可根据提示向好友推荐游戏，被推荐的好友将收到游戏推荐信息，点击该信息即可下载最新版游戏。");
////                                                    Server.instance.authSession.write(seg_cmcc);
//                            					}else{
//                            						//其他
//                            						/**
//                            					     * 卓望版本，向用户发送短信通知。
//                            					     * userId           String          用户登录ID
//                            					     * message          String          通知消息
//                            					     */
//                            						seg_cmcc.writeString(p[i].Cmcc_list); 
//                                                	seg_cmcc.writeString("尊敬的用户,感谢您一直支持我们的游戏,幻想最新圣诞大餐优先奉献,宠物,经验,奖品不断,登陆游戏,即可获得圣诞奖励！游戏中使用推荐好友功能,更可获得丰富奖品！");
//                                                    Server.instance.authSession.write(seg_cmcc);
////                            						seg_cmcc.writeString(p[i].Cmcc_list); 
////                                                	seg_cmcc.writeString("亲爱的用户,登陆“幻想i时代”游戏中的移动营业厅,可享受各项移动业务,优惠多,实惠多,赶快行动吧!");
////                                                    Server.instance.authSession.write(seg_cmcc);
////                                                    seg_cmcc = new UWAPSegment(ServerConstants.CMCC_SEND_MESSAGE);
////                            						seg_cmcc.writeString(p[i].Cmcc_list); 
////                            						seg_cmcc.writeString("您是今天第"+jilin_count+"位登陆幻想i时代的吉林移动用户，12月15日前每天前5000名吉林移动登陆幻想i时代的有效登陆用户（登陆5分钟以上）中，");
////                                                    Server.instance.authSession.write(seg_cmcc);
////                                                    seg_cmcc = new UWAPSegment(ServerConstants.CMCC_SEND_MESSAGE);
////                                                    seg_cmcc.writeString(p[i].Cmcc_list); 
////                                                	seg_cmcc.writeString("序号尾号为“8”的用户将得到10元话费，“88”用户将得到50元话费，欢迎每天登陆，恭祝您获奖！客服电话10086。");
////                                                    Server.instance.authSession.write(seg_cmcc);
////                                                    seg_cmcc = new UWAPSegment(ServerConstants.CMCC_SEND_MESSAGE);
////                                                    seg_cmcc.writeString(p[i].Cmcc_list); 
////                                                	seg_cmcc.writeString("发短信“hx”到10658838，点击回复信息可第一时间下载最新版幻想i时代");
////                                                    Server.instance.authSession.write(seg_cmcc);
//                            					}
//                            				}
//                            			}
//                            			//发短信
//                            			log.info("JILIN CMCC PLAYERLOGIN ID[" + p[i].getId() + "]Accountid[" +
//                            					p[i].getAccountId() +"]cmccUserId[" + p[i].Cmcc_list +"]" +
//                                        				"jilincount["+Server.cmcc_jilin_count+"]---COUNT["+jilin_count+"]CMCC JILIN");
//                            			Server.cmcc_jilin_playerid.put(p[i].Cmcc_list, Server.cmcc_jilin_count);
//                            			p[i].Cmcc_list = "";
//                            			
//                    				}else{
//                    					p[i].Cmcc_list = "";
//                    				}
//                    			}
//                    		}
//                    	}
//                    }
//                }
            }catch(Exception e){
                log.error(e, e);
            }
        }
    }
    public void saveCmccJILINxml(int cmccjilincount,Map<String,Integer> cmcc_jilin_playerid){
    	Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement("jilins");
        for (String s : cmcc_jilin_playerid.keySet()) {
            Element elem = root.addElement("jilin");
            elem.addAttribute("id", s);
        }
        Element elem = root.addElement("count");
        elem.addAttribute("id", String.valueOf(cmccjilincount));
        try {
			saveDocument(doc, new FileWriter("cmcc_jilin.xml"));
		} catch (IOException e) {
			log.error(e, e);
		}

    }
    public void saveCmccFUJIANxml(Map<String,Integer> cmcc_fujian_playerid,int totalmoney){
    	Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement("fujian");
        for (String s : cmcc_fujian_playerid.keySet()) {
            Element elem = root.addElement("fujian");
            elem.addAttribute("userid", s);
            elem.addAttribute("money", cmcc_fujian_playerid.get(s).toString());
        }
        Element elem = root.addElement("totalmoney");
        elem.addAttribute("money", String.valueOf(totalmoney));
        try {
			saveDocument(doc, new FileWriter("cmcc_fujian.xml"));
		} catch (IOException e) {
			log.error(e, e);
		}

    }
    public static void saveDocument(Document doc,Writer w){
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("GBK");
        XMLWriter writer = new XMLWriter(w, format);
        try {
			writer.write(doc);
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			 try {
				writer.close();
			} catch (IOException e) {
			}
		}
       
    }
}
