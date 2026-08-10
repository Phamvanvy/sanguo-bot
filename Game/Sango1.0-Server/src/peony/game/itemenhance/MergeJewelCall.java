package peony.game.itemenhance;

import java.text.MessageFormat;
import java.util.Random;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.ChatOption;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.GameObject;
import peony.game.ItemTemplate;
import peony.game.LogUtil;
import peony.game.NoEnoughValueException;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.chat.ChatMessage;
import peony.game.chat.ItemChatAttachment;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.ServiceEvent;
import peony.service.tong.TongMember;
import peony.service.tong.TongService;
import peony.service.tong.TongSkill10;
import peony.service.tong.TongSkill13;
import peony.service.tong.TongSkill8;

import com.pip.sanguo.data.item.Item;

/**
 * 镶嵌：请求合成宝石。
 * serial               int                 序列号
 * jewelid              int                 宝石物品ID
 * count                byte                宝石数量
 * autoFixCnt           int                一次性自动合成宝石后得到的宝石数
 * public static final short DECORATE_MERGE_JEWEL_CLIENT = 505;
 * 镶嵌：合成宝石成功。
 * serial               int                 序列号
 * jewelid              int                 如果成功，返回合成后的宝石物品ID
 * jewelicon            byte                如果成功，返回合成后的宝石物品图标
 * jewelname            String              如果成功，返回合成后的宝石物品名称
 * public static final short DECORATE_MERGE_JEWEL_SERVER = 506;
 */
public class MergeJewelCall extends ClientSessionAsyncCall {
	protected final Logger log = Logger.getLogger(MergeJewelCall.class);
	protected int serial;
	protected Player player;
	protected int jewelID;
	protected int jewelCount;
	protected int autoFixCnt;
	protected ItemTemplate mergeItem;
	protected static Random rand = new Random();

	public MergeJewelCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.player = (Player)session.getClient();
		this.jewelID = packet.getInt();
		this.jewelCount = packet.get() & 0xFF;
		this.autoFixCnt = packet.getInt();
	}
	
	protected void go() throws Exception {
		LogUtil.logMergeJewelTry(player, jewelID, jewelCount, autoFixCnt);

	    JewelService js = Server.server.getServiceRegistry().getJewelService();
	    
	    // 检查宝石数量
	    if (jewelCount < 3 || jewelCount > 5) {
	        throw new Exception(peony.Messages.STRING_00549);
	    }
	    
	    if(autoFixCnt < 1){
	    	throw new Exception(peony.Messages.STRING_00549);
	    }else if(autoFixCnt > 1){
	    	int count = player.bag.getGameItemCount(jewelID);
	    	if(count < autoFixCnt*5){
	    		throw new Exception(peony.Messages.STRING_00549);
	    	}
	    }
	    
	    // 扣除物品、金钱、合成符
	    PlayerTransaction tx = player.newTransaction("MJE");
	    GameItem gi = player.bag.removeGameItem(jewelID, GameItem.GENERAL_INSTANCEID, jewelCount*autoFixCnt, tx, true);
	    
	  //检查宝石是否有瑕疵
	    if(gi.template.isFlaw){
	    	throw new Exception(peony.Messages.STRING_00551);
	    }
	    
	    if (gi == null) {
	        tx.rollback();
	        throw new Exception(peony.Messages.STRING_00550);
	    }
	    if (gi.template.itemType != Item.TYPE_JEWEL) {
	        tx.rollback();
	        throw new Exception(peony.Messages.STRING_00552);
	    }
	    if (gi.template.useLevel >= JewelService.JEWEL_LEVELS) {
	        tx.rollback();
	        throw new Exception(peony.Messages.STRING_00553);
	    }
	    int lvl = gi.template.useLevel;
	    int needMoney = js.getMergePrice(lvl)*autoFixCnt;
	    int needMon = Math.round(needMoney*JewelService.mergeJewelRate);
	    try {
	        player.decMoney(needMon, tx, true);
	    } catch (NoEnoughValueException ex) {
	        tx.rollback();
	        throw new Exception(peony.Messages.STRING_00020);
	    }
	    //军团专属科技  合成宝石奥义
	    boolean isNeedMergeItem = true;
	    if(lvl == 1 || lvl == 2 || lvl == 3){
	    	 //军团专属科技  合成宝石奥义
	    	TongService ts = Server.server.getServiceRegistry().getTongService();
	    	TongMember tm = ts.getPlayerInfo(player.id);
	    	if(tm!=null && tm.skills!=null && tm.skills.get(10)!=null){
	    		TongSkill10 tskill = (TongSkill10)tm.skills.get(10);
	    		if(tskill.level > 0){
	    			int ratios = tskill.getRatios();
	    			if(rand.nextInt(100)>=(100 - ratios)){
	    				isNeedMergeItem = false;
	    			}
	    		}
	    	}
	    }
	    if(isNeedMergeItem){
	    	if (player.bag.removeGameItem(js.mergeItemIDs[lvl - 1], GameItem.GENERAL_INSTANCEID, autoFixCnt, tx, true) == null) {
	    		tx.rollback();
	    		throw new Exception(MessageFormat.format(peony.Messages.STRING_00093,js.mergeItems[lvl - 1].name));
	    	}
	    }
	    // 试图合成
	    int rate;
	    if (jewelCount == 3) {
	        rate = JewelService.MERGE_3_SUCC;
	    } else if (jewelCount == 4) {
	        rate = JewelService.MERGE_4_SUCC;
	    } else {
	        rate = JewelService.MERGE_5_SUCC;
	    }
	    if (rand.nextInt(100) > rate) {
	        tx.commit();
	        LogUtil.logMergeJewelOK(player, jewelID, jewelCount, autoFixCnt, false);
	        throw new Exception(peony.Messages.STRING_01746);
	    }
	    
	    // 合成成功，把生成的物品加入背包
	    mergeItem = js.jewels[gi.template.jewelAttrType][gi.template.useLevel];
	    GameItem gi3 = ObjectAccessor.createGameItem(mergeItem, -1);
	    //军团专属科技  合成宝石暴击
	    int count = 1;
	    if(lvl == 1 || lvl == 2){
	    	 //军团专属科技  合成宝石奥义
	    	TongService ts = Server.server.getServiceRegistry().getTongService();
	    	TongMember tm = ts.getPlayerInfo(player.id);
	    	if(tm!=null && tm.skills != null && tm.skills.get(13)!=null){
	    		TongSkill13 tskill = (TongSkill13)tm.skills.get(13);
	    		if(tskill.level > 0){
	    			int ratios = tskill.getRatios();
	    			if(rand.nextInt(100)>=(100 - ratios)){
	    				count = 2;
	    			}
	    		}
	    	}
	    }
        if (!player.bag.addGameItem(gi3, count*autoFixCnt, tx, true)) {
            tx.rollback();
            throw new Exception(peony.Messages.STRING_00555);
        }
        tx.commit();
        LogUtil.logMergeJewelOK(player, jewelID, jewelCount, autoFixCnt, true);
        
        //宝石合成成功事件
        Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_MERGEJEWEL, player));
        for(int i=0; i<autoFixCnt; i++){
	        ItemChatAttachment attItem = new ItemChatAttachment(gi3);
	        if(gi3.template.useLevel==5){
	        	String s = MessageFormat.format(peony.Messages.STRING_00556, player.name,gi3.template.name);
	        	ChatMessage cm = new ChatMessage(ChatOption.FACTION, player.id, player.faction,peony.Messages.STRING_00004,player.faction, s, null);
	    		Server.server.getServiceRegistry().getChatService().addChatMessage(cm);
	        }
	        else if(gi3.template.useLevel==6){
	        	String s = MessageFormat.format(peony.Messages.STRING_00557,player.getFactionName(), player.name);
	        	ChatMessage cm = new ChatMessage(ChatOption.WORLD, player.id, -1,peony.Messages.STRING_00004, s, attItem);
	    		Server.server.getServiceRegistry().getChatService().addChatMessage(cm);
	        }
	        else if(gi3.template.useLevel==7){
	        	String s = MessageFormat.format(peony.Messages.STRING_01747, player.getFactionName(),player.name);
	       	    ChatMessage cm = new ChatMessage(ChatOption.WORLD, player.id, -1,peony.Messages.STRING_00004, s, attItem);
	    		Server.server.getServiceRegistry().getChatService().addChatMessage(cm);
	        }
        }
	}

	public void callFinish() {
	    try {
	        go();
	        
	        // 回送合成成功的包
	        Packet pt = new Packet(OpCode.DECORATE_MERGE_JEWEL_SERVER);
            pt.putInt(serial);
            pt.putInt(mergeItem.id);
            pt.put(mergeItem.showType);
            pt.putString(mergeItem.name);
            session.send(pt);
	    } catch (Exception e) {
	        ErrorHandler.sendErrorMessage(session, serial,
                    OpCode.DECORATE_MERGE_JEWEL_CLIENT, e.getMessage());
	    }
	}

	public void run() {
		addToClientSession();
	}
}
