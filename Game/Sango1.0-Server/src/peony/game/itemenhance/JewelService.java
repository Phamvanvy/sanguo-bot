package peony.game.itemenhance;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.ItemTemplate;
import peony.game.ItemUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.Service;
import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.item.Item;

/**
 * 宝石镶嵌相关的服务。
 * @author lighthu
 */
public class JewelService implements Service {
    private static final Logger log = Logger.getLogger(JewelService.class);
    
    public static final int JEWEL_LEVELS = 7;
    public static final int MAX_JEWEL_TYPES = 100;
    
    // 合成成功率
    public static final int MERGE_3_SUCC = 30;
    public static final int MERGE_4_SUCC = 70;
    public static final int MERGE_5_SUCC = 100;
    
    // 镶嵌成功率
    public static final int DECO_SUCC_RATE1 = 30;
    public static final int DECO_SUCC_RATE2 = 60;
    public static final int DECO_SUCC_RATE3 = 100;
    
    // 所有支持的宝石物品ID
    public ItemTemplate[][] jewels;
    // 各级别的宝石合成下一级宝石需要的合成符
    public int[] mergeItemIDs = { 1336, 1336, 1336, 1337, 1337, 1337, 1337 };
    protected ItemTemplate[] mergeItems;
    // 各级别装备所需的打孔符（第一列是级别上限，第二列是打孔符的ID）
    protected int[][] addHoleItemIDs = {
            { 20, 1338 }, 
            { 40, 1339 },
            { 60, 1340 },
            { 255, 1341 }
    };
    protected ItemTemplate[] addHoleItems;
    // 低级镶嵌符ID
    protected int decorateItemID1 = 1342;
    protected ItemTemplate deocrateItem1;
    // 高级镶嵌符ID
    protected int decorateItemID2 = 1343;
    protected ItemTemplate deocrateItem2;
    // 各级别的宝石摘除符ID
    protected int[] removeItemIDs = { 1344, 1345, 1346, 1347, 1348, 1349, 1350 };
    protected ItemTemplate[] removeItems;
    
    public static float addHoleRate = 1.0f;
    public static float addJewelRate = 1.0f;
    public static float mergeJewelRate = 1.0f;
    public static float removeJewelRate = 1.0f;
    public static int[] addHoleSucRate = {10000,9000,3000,1000,400,300,200};
    
    public void startup() throws Exception {
        reload();
    }

    public void shutdown() {
    }
    
    public void reload() {
        ProjectData data = Server.server.getServiceRegistry().getDataService().data;
        
        // 找出所有的宝石物品
        ItemTemplate[][] jewelItems = new ItemTemplate[MAX_JEWEL_TYPES][JEWEL_LEVELS];
        List<DataObject> items = data.getDataListByType(Item.class);
        for (DataObject dobj : items) {
            Item item = (Item)dobj;
            if (item.type == Item.TYPE_JEWEL && !item.isFlaw) {
                jewelItems[item.jewelAttrType][item.playerLevel - 1] = ObjectAccessor.getItemTemplate(item.id);
            }
        }
        jewels = jewelItems;
        
        // 找出所有的合成符
        ItemTemplate[] arr = new ItemTemplate[mergeItemIDs.length];
        for (int i = 0; i < mergeItemIDs.length; i++) {
            arr[i] = ObjectAccessor.getItemTemplate(mergeItemIDs[i]);
            if (arr[i] == null) {
                log.error("JewelService: item not found " + mergeItemIDs[i]);
            }
        }
        mergeItems = arr;
        
        // 找出所有的打孔符
        arr = new ItemTemplate[addHoleItemIDs.length];
        for (int i = 0; i < addHoleItemIDs.length; i++) {
            arr[i] = ObjectAccessor.getItemTemplate(addHoleItemIDs[i][1]);
            if (arr[i] == null) {
                log.error("JewelService: item not found " + addHoleItemIDs[i][1]);
            }
        }
        addHoleItems = arr;
        
        // 找出所有的镶嵌符
        deocrateItem1 = ObjectAccessor.getItemTemplate(decorateItemID1);
        if (deocrateItem1 == null) {
            log.error("JewelService: item not found " + deocrateItem1);
        }
        deocrateItem2 = ObjectAccessor.getItemTemplate(decorateItemID2);
        if (deocrateItem2 == null) {
            log.error("JewelService: item not found " + deocrateItem2);
        }
        
        // 找出所有的摘除符
        arr = new ItemTemplate[removeItemIDs.length];
        for (int i = 0; i < removeItemIDs.length; i++) {
            arr[i] = ObjectAccessor.getItemTemplate(removeItemIDs[i]);
            if (arr[i] == null) {
                log.error("JewelService: item not found " + removeItemIDs[i]);
            }
        }
        removeItems = arr;
    }
    
    /**
     * 取得宝石合成需要的金钱
     * @param level 宝石级别， 1表示1级
     * @return
     */
    public int getMergePrice(int level) {
        return level * level * level * 150;
    }
    
    /**
     * 取得物品对应的打孔符。
     * @param equLevel 装备需求等级
     * @return 对于非法级别，返回null
     */
    public List<ItemTemplate> getAddHoleItem(int equLevel) {
    	List<ItemTemplate> l = new ArrayList<ItemTemplate>(5);
        for (int i = 0; i < addHoleItemIDs.length; i++) {
            if (equLevel <= addHoleItemIDs[i][0]) {
            	l.add(addHoleItems[i]);
//                return addHoleItems[i];
            }
        }
        return l;
    }
    
    /**
     * 计算打孔需要的金钱。
     * @param equLevel 装备需求等级 
     * @param currentHole 装备上当前的孔数
     * @return
     */
    public int getAddHolePrice(int equLevel, int currentHole) {
        return Math.round((addHoleRate * equLevel * equLevel * (currentHole + 1) / 2));
    }
    
    /**
     * 计算打孔成功率。
     * @param currentHole 当前孔数
     * @return 0-10000的数
     */
    public int getAddHoleSuccRate(int currentHole) {
        switch (currentHole) {
        case 0:
            return addHoleSucRate[0];
        case 1:
            return addHoleSucRate[1];
        case 2:
            return addHoleSucRate[2];
        case 3:
            return addHoleSucRate[3];
        case 4:
            return addHoleSucRate[4];
        case 5:
            return addHoleSucRate[5];
        case 6:
            return addHoleSucRate[6];
        default:
            return 100;
        }
    }
    
    /**
     * 计算镶嵌需要的金钱
     * @param level 宝石级别， 1表示1级
     * @return
     */
    public int getDecoratePrice(int level) {
        return (int)(addJewelRate * level * level * 100);
    }
    
    /**
     * 计算取下宝石需要的金钱
     * @param level 宝石级别，1表示1级
     * @return
     */
    public int getRemovePrice(int level) {
        return (int)(removeJewelRate * level * level * 50);
    }
    
    public void showAddHolePrice(ClientSession session,Packet packet){
    	int serial = packet.getInt();
    	int equItemID = packet.getInt();
	    int equInstanceID = packet.getInt();
	    Player player = (Player)session.getClient();
	    if(player != null){
    		Object[] obj = ItemUtil.findPlayerEquipment(player, equItemID,
    				equInstanceID);
    		if (obj == null) {
    			ErrorHandler.sendErrorMessage(session, serial, OpCode.SHOW_DECORADE_PRICE_CLIENT, peony.Messages.STRING_00501);
    			return;
    		}
    		GameItem gi = (GameItem) obj[0];
    		if (gi.template.equipment == null) {
    			ErrorHandler.sendErrorMessage(session, serial, OpCode.SHOW_DECORADE_PRICE_CLIENT, peony.Messages.STRING_00016);
    			return;
    		}
    		if (gi.object == null) {
    			gi.object = new ItemEnhance();
    		}
    		if (!(gi.object instanceof ItemEnhance)) {
    			ErrorHandler.sendErrorMessage(session, serial, OpCode.SHOW_DECORADE_PRICE_CLIENT, peony.Messages.STRING_00017);
    			return;
    		}
    		// 检查是否已达到最大孔位
    		ItemEnhance itemEnh = (ItemEnhance) gi.object;
    		if (itemEnh.addHole + gi.template.equipment.initHole >= itemEnh.addMaxHole
    				+ gi.template.equipment.maxHole) {
    			ErrorHandler.sendErrorMessage(session, serial, OpCode.SHOW_DECORADE_PRICE_CLIENT, peony.Messages.STRING_00502);
    			return;
    		}
    		int needMoney = getAddHolePrice(gi.template.useLevel,
    				itemEnh.addHole + gi.template.equipment.initHole);
    	    Packet pt = new Packet(OpCode.SHOW_DECORADE_PRICE_SERVER);
    	    pt.putInt(serial);
    	    pt.putInt(needMoney);
    	    player.send(pt);
	    }  	    
    }
}
