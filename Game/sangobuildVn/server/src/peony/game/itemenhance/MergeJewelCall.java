package peony.game.itemenhance;

import java.text.MessageFormat;
import java.util.Random;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.ChatOption;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.ItemTemplate;
import peony.game.LogUtil;
import peony.game.NoEnoughValueException;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.ServiceEvent;

import com.pip.sanguo.data.item.Item;

/**
 * 镶嵌：请求合成宝石。
 * serial               int                 序列号
 * jewelid              int                 宝石物品ID
 * count                byte                宝石数量
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
	protected ItemTemplate mergeItem;
	protected static Random rand = new Random();

	public MergeJewelCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.player = (Player)session.getClient();
		this.jewelID = packet.getInt();
		this.jewelCount = packet.get() & 0xFF;
	}
	
	protected void go() throws Exception {
		LogUtil.logMergeJewelTry(player, jewelID, jewelCount);

	    JewelService js = Server.server.getServiceRegistry().getJewelService();
	    
	    // 检查宝石数量
	    if (jewelCount < 3 || jewelCount > 5) {
	        throw new Exception("宝石数量错误");
	    }
	    
	    // 扣除物品、金钱、合成符
	    PlayerTransaction tx = player.newTransaction("MJE");
	    GameItem gi = player.bag.removeGameItem(jewelID, GameItem.GENERAL_INSTANCEID, jewelCount, tx, true);
	    
	  //检查宝石是否有瑕疵
	    if(gi.template.isFlaw){
	    	throw new Exception("有瑕疵的宝石没有合成功能");
	    }
	    
	    if (gi == null) {
	        tx.rollback();
	        throw new Exception("没有足够的宝石");
	    }
	    if (gi.template.itemType != Item.TYPE_JEWEL) {
	        tx.rollback();
	        throw new Exception("选择物品错误");
	    }
	    if (gi.template.useLevel >= JewelService.JEWEL_LEVELS) {
	        tx.rollback();
	        throw new Exception("你选择的宝石已经是最高级的了");
	    }
	    int lvl = gi.template.useLevel;
	    int needMoney = js.getMergePrice(lvl);
	    try {
	        player.decMoney(needMoney, tx, true);
	    } catch (NoEnoughValueException ex) {
	        tx.rollback();
	        throw new Exception("没有足够的金钱");
	    }
	    if (player.bag.removeGameItem(js.mergeItemIDs[lvl - 1], GameItem.GENERAL_INSTANCEID, 1, tx, true) == null) {
	        tx.rollback();
	        throw new Exception(MessageFormat.format("<cff0000>缺少</c>\nMột phong thư dán có vết máu ",js.mergeItems[lvl - 1].name));
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
	        LogUtil.logMergeJewelOK(player, jewelID, jewelCount, false);
	        throw new Exception("很遗憾，合成失败了");
	    }
	    
	    // 合成成功，把生成的物品加入背包
	    mergeItem = js.jewels[gi.template.jewelAttrType][gi.template.useLevel];
	    GameItem gi3 = ObjectAccessor.createGameItem(mergeItem, -1);
        if (!player.bag.addGameItem(gi3, 1, tx, true)) {
            tx.rollback();
            throw new Exception("Túi đồ đã đầy");
        }
        tx.commit();
        LogUtil.logMergeJewelOK(player, jewelID, jewelCount, true);
        
        //宝石合成成功事件
        Server.server.getEventManager().addEvent(new ServiceEvent(ServiceEvent.EVENT_MERGEJEWEL, player));
        
        if(gi3.template.useLevel==5){
        	String s = MessageFormat.format("{0}成功合成出了一颗闪闪发光的{1}", player.name,gi3.template.name);
        	Server.server.getServiceRegistry().getChatService().sendFactionSystemMessage(player.faction, s);
        }
        else if(gi3.template.useLevel==6){
        	String s = MessageFormat.format("{0}竟然成功的合成出了一颗完美无瑕的{1}", player.name,gi3.template.name);
        	Server.server.getServiceRegistry().getChatService().sendFactionSystemMessage(player.faction, s);
        }
        else if(gi3.template.useLevel==7){
        	String s = MessageFormat.format("{0}的{1}让绝世神石{2}奇迹般的诞生在这个世界上！", player.getFactionName(),player.name,gi3.template.name);
        	Server.server.getServiceRegistry().getChatService().sendSystemMessage(ChatOption.WORLD, "<cFF0000>[系统]</c>\n<cFF0000>[hệ thống]</c>", s);
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
