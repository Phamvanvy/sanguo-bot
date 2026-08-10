package peony.game.itemenhance;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import peony.game.ItemTemplate;
import peony.game.ObjectAccessor;
import peony.game.Server;
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
        return equLevel * equLevel * (currentHole + 1) / 2;
    }
    
    /**
     * 计算打孔成功率。
     * @param currentHole 当前孔数
     * @return 0-10000的数
     */
    public int getAddHoleSuccRate(int currentHole) {
        switch (currentHole) {
        case 0:
            return 10000;
        case 1:
            return 9000;
        case 2:
            return 3000;
        case 3:
            return 1000;
        case 4:
            return 400;
        case 5:
            return 300;
        case 6:
            return 200;
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
        return level * level * 100;
    }
    
    /**
     * 计算取下宝石需要的金钱
     * @param level 宝石级别，1表示1级
     * @return
     */
    public int getRemovePrice(int level) {
        return level * level * 50;
    }


}
