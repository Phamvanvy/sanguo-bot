package peony.game.drop;

import java.util.List;

import peony.game.DataService;
import peony.game.ItemTemplate;
import peony.game.ObjectAccessor;
import peony.game.Server;

import ch.javasoft.util.intcoll.IntHashMap;

import com.pip.sanguo.data.DataChangeHandler;
import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.item.DropGroup;
import com.pip.sanguo.data.item.DropItem;
import com.pip.sanguo.data.item.SubDropGroup;

public class DropGroupUtil implements DataChangeHandler {
    protected static IntHashMap<IntHashMap<Drop>> levelWorldDrop = new IntHashMap<IntHashMap<Drop>>();
    
    public static Drop[] getWorldDrop(int monsterLevel) {
        IntHashMap<Drop> ihm = levelWorldDrop.get(monsterLevel);
        if (ihm == null) {
            return new Drop[0];
        } else {
            Drop[] ret = new Drop[ihm.size()];
            ihm.values().toArray(ret);
            return ret;
        }
    }
    
	public static void load(){
		DataService dataService = Server.server.getServiceRegistry().getDataService();
		List groups = dataService.data.getDataListByType(DropGroup.class);
		for (Object o : groups) {
			DropGroup group = (DropGroup) o;
			GroupDrop drop = getGroupDrop(group.id);
			translateDropGroup(group, drop);
		}
	}
	
	private static void translateDropGroup(DropGroup group, GroupDrop drop) {
        List<SubDropGroup> subs = group.subGroup;
        drop.setValid(group.valid);
        for (SubDropGroup sub : subs) {
            LeveledGroupDrop lgd = new LeveledGroupDrop(sub.levelMin,
                    sub.levelMax, sub.job, group.quantityMin, group.quantityMax);
            for (DropItem dropItem : sub.dropGroup) {   
                if (dropItem.dropType == DropItem.DROP_TYPE_MONEY) {
                    MoneyDrop d = new MoneyDrop( -1,
                            dropItem.quantityMin, dropItem.quantityMax);
                    WeightGroupDrop wd = new WeightGroupDrop(
                            dropItem.dropWeight, d);
                    lgd.addGroupDrop(wd);
                } else if (dropItem.dropType == DropItem.DROP_TYPE_EXP) {
                    ExpDrop d = new ExpDrop( -1,
                            dropItem.quantityMin, dropItem.quantityMax);
                    WeightGroupDrop wd = new WeightGroupDrop(
                            dropItem.dropWeight, d);
                    lgd.addGroupDrop(wd);
                } else if (dropItem.dropType == DropItem.DROP_TYPE_ITEM
                        || dropItem.dropType == DropItem.DROP_TYPE_EQUI) {
                    ItemTemplate template = ObjectAccessor
                            .getItemTemplate(dropItem.dropID);
//                      if (template == null)
//                          throw new IllegalArgumentException();
                    ItemDrop d = new ItemDrop(template, -1,
                            dropItem.quantityMin, dropItem.quantityMax);
                    WeightGroupDrop wd = new WeightGroupDrop(
                            dropItem.dropWeight, d);
                    lgd.addGroupDrop(wd);
                } else if(dropItem.dropType== DropItem.DROP_TYPE_DROPGROUP){
                    GroupDrop d = getGroupDrop(dropItem.dropID);
                    WeightGroupDrop wd = new WeightGroupDrop(
                            dropItem.dropWeight, d);
                    lgd.addGroupDrop(wd);
                } else{
                    throw new IllegalArgumentException();
                }
            }
            drop.addDrop(lgd);
        }
        
        // 处理世界掉落组
        if (group.groupType == DropGroup.GROUP_TYPE_WORLD) {
            int minLevel = group.minMonsterLevel;
            int maxLevel = group.maxMonsterLevel;
            int rate = group.dropRate;
            RateDrop rd = new RateDrop(rate * 100, drop);
            for (int i = minLevel; i <= maxLevel; i++) {
                IntHashMap<Drop> l = levelWorldDrop.get(i);
                if (l == null) {
                    l = new IntHashMap<Drop>();
                    levelWorldDrop.put(i, l);
                }
                l.put(group.id, rd);
            }
        }
	}
	
	protected static GroupDrop getGroupDrop(int id){
		GroupDrop ret = ObjectAccessor.getGroupDrop(id);
		if (ret == null) {
			ret = new GroupDrop(id);
			ObjectAccessor.addGroupDrop(ret);
		}
		return ret;
	}

    /**
     * 添加新对象通知。
     * @param obj 新添加的对象
     */
    public void dataObjectAdded(DataObject obj) {
        if (obj instanceof DropGroup) {
            DropGroup group = (DropGroup)obj;
            GroupDrop drop = getGroupDrop(group.id);
            translateDropGroup(group, drop);
        }
    }
    
    /**
     * 对象被删除通知。
     * @param obj 被删除的老对象
     */
    public void dataObjectRemoved(DataObject obj) {
        if (obj instanceof DropGroup) {
            GroupDrop dg = ObjectAccessor.groupDrops.remove(obj.id);
            dg.setValid(false);
            for (IntHashMap<Drop> ihm : levelWorldDrop.values()) {
                ihm.remove(obj.id);
            }
        }
    }
    
    /**
     * 对象即将被修改通知。
     * @param obj 修改前的对象
     */
    public void dataObjectChanging(DataObject obj) {
    }
    
    /**
     * 对象被修改通知。
     * @param newobj 修改后的新对象
     */
    public void dataObjectChanged(DataObject newobj) {
        if (newobj instanceof DropGroup) {
            DropGroup group = (DropGroup)newobj;
            GroupDrop drop = getGroupDrop(group.id);
            drop.clear();
            translateDropGroup(group, drop);
        }
    }
}
