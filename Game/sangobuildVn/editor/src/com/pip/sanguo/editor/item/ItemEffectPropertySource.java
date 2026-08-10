package com.pip.sanguo.editor.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.pip.sanguo.data.Card;
import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.Title;
import com.pip.sanguo.data.item.Item;
import com.pip.sanguo.data.item.ItemEffect;
import com.pip.sanguo.editor.DefaultDataObjectEditor;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.property.CardPropertyDescriptor;
import com.pip.sanguo.editor.property.DropGroupPropertyDescriptor;
import com.pip.sanguo.editor.property.FormulaPropertyDescriptor;
import com.pip.sanguo.editor.property.HorseTypePropertyDescriptor;
import com.pip.sanguo.editor.property.ItemPropertyDescriptor;
import com.pip.sanguo.editor.property.LevelTablePropertyDescriptor;
import com.pip.sanguo.editor.property.LocationPropertyDescriptor;
import com.pip.sanguo.editor.property.QuestPropertyDescriptor;
import com.pip.sanguo.editor.property.SkillPropertyDescriptor;

/**
 * 物品使用效果属性编辑框内容提供类
 * 
 * @author Joy Yan
 *
 */
public class ItemEffectPropertySource implements IPropertySource {
    /**
     * 需要修改的效果数据
     */
    private ItemEffect effect;
    
    private DefaultDataObjectEditor handle;
    
    /**
     * 物品名称列表
     */
    private String[] itemNames;
    /**
     * 物品id列表
     */
    private int[] itemIds;
    /**
     * 所有称号的名称。
     */
    private String[] titleNames;
    /**
     * 所有称号的ID。
     */
    private int[] titleIds;
    
    public ItemEffectPropertySource(ItemEffect effect, DefaultDataObjectEditor editor){
        this.effect = effect;
        this.handle = editor;
        
        /* 所有物品列表 */
        List<DataObject> itemList = EditorApplication.getInstance().getProjectData().getDataListByType(Item.class);
        List<String> itemNamesTmp = new ArrayList<String>();
        List<Integer> itemIdsTmp = new ArrayList<Integer>();
        
        for (DataObject obj : itemList) {
            Item item = (Item)obj;
            itemNamesTmp.add(item.categoryName + "-" + item.toString());
            itemIdsTmp.add(item.id);
        }
        
        itemNames = new String[itemNamesTmp.size()];
        itemNamesTmp.toArray(itemNames);
        
        itemIds = new int[itemIdsTmp.size()];
        for (int i = 0; i < itemIds.length; i++) {
            itemIds[i] = itemIdsTmp.get(i);
        }
        
        // 获取称号列表
        List<DataObject> titleList = EditorApplication.getProj().getDataListByType(Title.class);
        titleNames = new String[titleList.size()];
        titleIds = new int[titleList.size()];
        for (int i = 0; i < titleList.size(); i++) {
            titleNames[i] = titleList.get(i).getTitle();
            titleIds[i] = titleList.get(i).id;
        }
    }

    public Object getEditableValue() {
        // TODO Auto-generated method stub
        return null;
    }
    
    /**
     * 返回用户属性的id和展现类型的一个二元组
     */
    public IPropertyDescriptor[] getPropertyDescriptors() {
        IPropertyDescriptor[] ret = null;
        
        switch(effect.effectType){
        case ItemEffect.TYPE_ADDHP:
            ret = new IPropertyDescriptor[2];
            ret[0] = new TextPropertyDescriptor("amount", "回复量");
            ret[1] = new TextPropertyDescriptor("percent", "回复百分比");
            break;
        case ItemEffect.TYPE_ADDMP:
            ret = new IPropertyDescriptor[2];
            ret[0] = new TextPropertyDescriptor("amount", "回复量");
            ret[1] = new TextPropertyDescriptor("percent", "回复百分比");
            break;
        case ItemEffect.TYPE_SKILL:
            ret = new IPropertyDescriptor[3];
            ret[0] = new SkillPropertyDescriptor("skill", "技能");
            ret[1] = new TextPropertyDescriptor("level", "级别");
            ret[2] = new ComboBoxPropertyDescriptor("usebuffs", "无视Buff", new String[]{"是","否"});
            break;
        case ItemEffect.TYPE_GETITEM:
            ret = new IPropertyDescriptor[2];
            ret[0] = new ItemPropertyDescriptor("item", "物品");
            ret[1] = new TextPropertyDescriptor("count", "数量");
            break;
        case ItemEffect.TYPE_QUEST:
            ret = new IPropertyDescriptor[3];
            ret[0] = new QuestPropertyDescriptor("quest", "任务(魏)");
            ret[1] = new QuestPropertyDescriptor("questshu", "任务(蜀)");
            ret[2] = new QuestPropertyDescriptor("questwu", "任务(吴)");
            break;
        case ItemEffect.TYPE_RELIVE:
            ret = new IPropertyDescriptor[1];
            ret[0] = new TextPropertyDescriptor("percent", "回复比例");
            break;
        case ItemEffect.TYPE_HORSE:
            ret = new IPropertyDescriptor[1];
            ret[0] = new TextPropertyDescriptor("horseid", "坐骑ID");
            break;
        case ItemEffect.TYPE_LEARNSKILL:
            ret = new IPropertyDescriptor[2];
            ret[0] = new SkillPropertyDescriptor("skill", "技能");
            ret[1] = new TextPropertyDescriptor("level", "级别");
            break;
        case ItemEffect.TYPE_EXTEND:
            ret = new IPropertyDescriptor[1];
            ret[0] = new TextPropertyDescriptor("script", "脚本");
            break;
        case ItemEffect.TYPE_TITLE:
            ret = new IPropertyDescriptor[1];
            ret[0] = new ComboBoxPropertyDescriptor("title", "称号", titleNames);
            break;
        case ItemEffect.TYPE_FEEDHORSE:
            ret = new IPropertyDescriptor[1];
            ret[0] = new TextPropertyDescriptor("feedvalue", "饱食度");
            break;
        case ItemEffect.TYPE_GETHORSE:
            ret = new IPropertyDescriptor[4];
            ret[0] = new TextPropertyDescriptor("name", "名称");
            ret[1] = new TextPropertyDescriptor("level", "级别");
            ret[2] = new TextPropertyDescriptor("imageid", "图片ID");
            ret[3] = new HorseTypePropertyDescriptor("horsetype", "可选类型");
            break;
        case ItemEffect.TYPE_DROPGROUP:
            ret = new IPropertyDescriptor[7];
            ret[0] = new DropGroupPropertyDescriptor("dropgroup1", "掉落组1");
            ret[1] = new DropGroupPropertyDescriptor("dropgroup2", "掉落组2");
            ret[2] = new TextPropertyDescriptor("controlvar", "控制变量");
            ret[3] = new TextPropertyDescriptor("switchcount", "切换频率");
            ret[4] = new ComboBoxPropertyDescriptor("mail", "支持飞鸽", new String[] { "否", "是" });
            ret[5] = new ItemPropertyDescriptor("useitem", "消耗物品");
            ret[6] = new TextPropertyDescriptor("useitemcount", "消耗数量");
            break;
        case ItemEffect.TYPE_TELEPORT:
            ret = new IPropertyDescriptor[3];
            ret[0] = new LocationPropertyDescriptor("weilocation", "魏国传送点");
            ret[1] = new LocationPropertyDescriptor("shulocation", "蜀国传送点");
            ret[2] = new LocationPropertyDescriptor("wulocation", "吴国传送点");
            break;
        case ItemEffect.TYPE_FORMULA:
            ret = new IPropertyDescriptor[1];
            ret[0] = new FormulaPropertyDescriptor("formula", "打造配方");
            break;
        case ItemEffect.TYPE_MARRIAGETELEPORT:
        case ItemEffect.TYPE_DIVORCE:
            ret = new IPropertyDescriptor[0];
            break;
        case ItemEffect.TYPE_EXTENDBAG:
            ret = new IPropertyDescriptor[1];
            ret[0] = new TextPropertyDescriptor("count", "扩展数量");
            break;
        case ItemEffect.TYPE_REFRESHSKILLPOINTS:
            ret = new IPropertyDescriptor[0];
            break;
        case ItemEffect.TYPE_EXTENDSTORAGE:
            ret = new IPropertyDescriptor[1];
            ret[0] = new TextPropertyDescriptor("count", "扩展数量");
            break;
        case ItemEffect.TYPE_EXP:
            ret = new IPropertyDescriptor[2];
            ret[0] = new TextPropertyDescriptor("amount", "数量(小于0表示级别比)");
            ret[1] = new LevelTablePropertyDescriptor("table", "配置表");
            break;
        case ItemEffect.TYPE_HORSEEXP:
            ret = new IPropertyDescriptor[2];
            ret[0] = new TextPropertyDescriptor("amount", "数量(小于0表示级别比)");
            ret[1] = new LevelTablePropertyDescriptor("table", "配置表");
            break;
        case ItemEffect.TYPE_HONOR:
            ret = new IPropertyDescriptor[2];
            ret[0] = new TextPropertyDescriptor("amount", "数量(小于0表示级别比)");
            ret[1] = new LevelTablePropertyDescriptor("table", "配置表");
            break;
        case ItemEffect.TYPE_CREDIT:
            ret = new IPropertyDescriptor[2];
            ret[0] = new TextPropertyDescriptor("amount", "数量(小于0表示级别比)");
            ret[1] = new LevelTablePropertyDescriptor("table", "配置表");
            break;
        case ItemEffect.TYPE_MONEY:
            ret = new IPropertyDescriptor[2];
            ret[0] = new TextPropertyDescriptor("amount", "数量(小于0表示级别比)");
            ret[1] = new LevelTablePropertyDescriptor("table", "配置表");
            break;
        case ItemEffect.TYPE_NATIONSKILL:
            ret = new IPropertyDescriptor[1];
            ret[0] = new TextPropertyDescriptor("skillid", "国家技能ID");
            break;
        case ItemEffect.TYPE_REFRESH_PROPERTY:
            ret = new IPropertyDescriptor[0];
            break;
        case ItemEffect.TYPE_REPAIR_EQUIPMENTS:
            ret = new IPropertyDescriptor[0];
            break;
        case ItemEffect.TYPE_IMONEY_CARD:
            ret = new IPropertyDescriptor[0];
            break;
        case ItemEffect.TYPE_RIDDLE:
            ret = new IPropertyDescriptor[0];
            break;
        case ItemEffect.TYPE_UNSIGNUP:
            ret = new IPropertyDescriptor[0];
            break;
        case ItemEffect.TYPE_CLICKEXP:
            ret = new IPropertyDescriptor[0];
            break;
        case ItemEffect.TYPE_TONGBATTLECAR:
        	ret = new IPropertyDescriptor[0];
        	break;
        case ItemEffect.TYPE_ADDKINGHP:
            ret = new IPropertyDescriptor[1];
            ret[0] = new TextPropertyDescriptor("rate", "回复比例");
            break;
        case ItemEffect.TYPE_CLICKGETMONEY:
        	 ret = new IPropertyDescriptor[1];
        	 ret[0] = new TextPropertyDescriptor("money", "金钱");
             break;
        case ItemEffect.TYPE_ACTIVEPOWER:
            ret = new IPropertyDescriptor[1];
            ret[0] = new TextPropertyDescriptor("value", "恢复/扣除值(负数为扣除)");
            break;
        case ItemEffect.TYPE_CONTRIBUTEPOINT:
            ret = new IPropertyDescriptor[1];
            ret[0] = new TextPropertyDescriptor("value", "恢复/扣除值(负数为扣除)");
            break;
        case ItemEffect.TYPE_GETIMONEY:
            ret = new IPropertyDescriptor[1];
            ret[0] = new TextPropertyDescriptor("value", "金额(分)");
            break;
        case ItemEffect.TYPE_USEDECITEM:
            ret = new IPropertyDescriptor[1];
            ret[0] = new TextPropertyDescriptor("itemid", "物品");
            break;
        case ItemEffect.TYPE_LIMITTONG:
            ret = new IPropertyDescriptor[0];
            break;
        case ItemEffect.TYPE_SKILL_POINT:
            ret = new IPropertyDescriptor[1];
            ret[0] = new TextPropertyDescriptor("skillpoint", "技能点");
            break;
        case ItemEffect.TYPE_ACTIVITY:
            ret = new IPropertyDescriptor[1];
            ret[0] = new TextPropertyDescriptor("ty", "活动类型");
            break;
        case ItemEffect.TYPE_TOWERDEFEND:
            ret = new IPropertyDescriptor[0];
            break;
        case ItemEffect.TYPE_TALISMAN:
            ret = new IPropertyDescriptor[1];
            ret[0] = new TextPropertyDescriptor("talismanindex", "法宝ID");
            break;
        case ItemEffect.TYPE_DECIMONEY:
            ret = new IPropertyDescriptor[1];
            ret[0] = new TextPropertyDescriptor("value", "金额(分)");
            break;
        case ItemEffect.TYPE_EXTEND_HORSE_BAG_MAX:
            ret = new IPropertyDescriptor[1];
            ret[0] = new TextPropertyDescriptor("value", "个");
            break;
        }
        return ret;
    }
    
    /**
     * 返回对应属性的取值
     */
    public Object getPropertyValue(Object id) {
        if ("quest".equals(id) || "questshu".equals(id) || "questwu".equals(id)|| 
                "item".equals(id) || "skill".equals(id) || 
                "dropgroup1".equals(id) || "dropgroup2".equals(id) || 
                "useitem".equals(id) || "formula".equals(id) || "card".equals(id)) {
            Integer ret = null;
            
            try {
                String index = (String)effect.param.get(id);
                ret = new Integer(index);
            } catch (Exception e) {
                ret = new Integer(-1);
            }
            return ret;
        } else if ("title".equals(id)) {
            int titleID = Integer.parseInt((String)effect.param.get(id));
            for (int i = 0; i < titleIds.length; i++) {
                if (titleID == titleIds[i]) {
                    return new Integer(i);
                }
            }
            return new Integer(-1);
        } else if ("mail".equals(id)) {
           return Integer.parseInt(effect.param.get(id));
        } else if ("weilocation".equals(id)) {
            int[] value = new int[3];
            value[0] = Integer.parseInt((String)effect.param.get("weimapid"));
            value[1] = Integer.parseInt((String)effect.param.get("weix"));
            value[2] = Integer.parseInt((String)effect.param.get("weiy"));
            return value;
        } else if ("shulocation".equals(id)) {
            int[] value = new int[3];
            value[0] = Integer.parseInt((String)effect.param.get("shumapid"));
            value[1] = Integer.parseInt((String)effect.param.get("shux"));
            value[2] = Integer.parseInt((String)effect.param.get("shuy"));
            return value;
        } else if ("wulocation".equals(id)) {
            int[] value = new int[3];
            value[0] = Integer.parseInt((String)effect.param.get("wumapid"));
            value[1] = Integer.parseInt((String)effect.param.get("wux"));
            value[2] = Integer.parseInt((String)effect.param.get("wuy"));
            return value;
        } else if("usebuffs".equals(id)){
            String newValue = effect.param.get("usebuffs");
            if(newValue==null)
                return 0;
            return Integer.parseInt(newValue);
        } 
        else {
            Object oo = effect.param.get(id);
            if(oo == null){
                return "";
            }
            return oo;
        }
    }

    public boolean isPropertySet(Object id) {return false;}

    public void resetPropertyValue(Object id) {}
    
    /**
     * 用户修改数据操作
     */
    public void setPropertyValue(Object id, Object value) {
        String newValue;
        if ("weilocation".equals(id)) {
            int[] arr = (int[])value;
            if (!String.valueOf(arr[0]).equals(effect.param.get("weimapid")) ||
                    !String.valueOf(arr[1]).equals(effect.param.get("weix")) ||
                    !String.valueOf(arr[2]).equals(effect.param.get("weiy")))
            effect.param.put("weimapid", String.valueOf(arr[0]));
            effect.param.put("weix", String.valueOf(arr[1]));
            effect.param.put("weiy", String.valueOf(arr[2]));
            handle.setDirty(true);
            return;
        }
        if ("shulocation".equals(id)) {
            int[] arr = (int[])value;
            if (!String.valueOf(arr[0]).equals(effect.param.get("shumapid")) ||
                    !String.valueOf(arr[1]).equals(effect.param.get("shux")) ||
                    !String.valueOf(arr[2]).equals(effect.param.get("shuy")))
            effect.param.put("shumapid", String.valueOf(arr[0]));
            effect.param.put("shux", String.valueOf(arr[1]));
            effect.param.put("shuy", String.valueOf(arr[2]));
            handle.setDirty(true);
            return;
        }
        if ("wulocation".equals(id)) {
            int[] arr = (int[])value;
            if (!String.valueOf(arr[0]).equals(effect.param.get("wumapid")) ||
                    !String.valueOf(arr[1]).equals(effect.param.get("wux")) ||
                    !String.valueOf(arr[2]).equals(effect.param.get("wuy")))
            effect.param.put("wumapid", String.valueOf(arr[0]));
            effect.param.put("wux", String.valueOf(arr[1]));
            effect.param.put("wuy", String.valueOf(arr[2]));
            handle.setDirty(true);
            return;
        }
        if ("title".equals(id)) {
            newValue = String.valueOf(titleIds[((Integer)value).intValue()]);
        } else {
            newValue = String.valueOf(value);
        }
        if(!newValue.equals(effect.param.get(id))){
            effect.param.put((String)id, newValue);
            handle.setDirty(true);
        }
        if("usebuffs".equals(id)){
            effect.param.put("usebuffs", ((Integer)value)==0?"0":"1");
            handle.setDirty(true);
            return;
        }
        if("card".equals(id)){
            int cardid = Integer.parseInt(effect.param.get("card"));
//            Card card = EditorApplication.getProj().findCard(cardid);
//            if(card != null){
//                card.itemId = ((Item)handle.getEditObject()).id;
//                try {
//                    EditorApplication.getInstance().getProjectData().saveDataList(Card.class);
//                }
//                catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
        }
    }
    
    public static final Map<String, String> getEffectParams(int type){
        Map<String, String> ret = new HashMap<String, String>();
        
        switch(type){
        case ItemEffect.TYPE_ADDHP:
            ret.put("amount", "0");
            ret.put("percent", "0");
            break;
        case ItemEffect.TYPE_ADDMP:
            ret.put("amount", "0");
            ret.put("percent", "0");
            break;
        case ItemEffect.TYPE_SKILL:
            ret.put("skill", "0");
            ret.put("level", "0");
            ret.put("usebuffs", "0");
            break;
        case ItemEffect.TYPE_GETITEM:
            ret.put("item", "0");
            ret.put("count", "0");
            break;
        case ItemEffect.TYPE_QUEST:
            ret.put("quest", "0");
            ret.put("questshu", "0");
            ret.put("questwu", "0");
            break;
        case ItemEffect.TYPE_RELIVE:
            ret.put("percent", "15.0");
            break;
        case ItemEffect.TYPE_HORSE:
            ret.put("horseid", "0");
            break;
        case ItemEffect.TYPE_LEARNSKILL:
            ret.put("skill", "0");
            ret.put("level", "0");
            break;
        case ItemEffect.TYPE_EXTEND:
            ret.put("script", "");
            break;
        case ItemEffect.TYPE_TITLE:
            ret.put("title", "0");
            break;
        case ItemEffect.TYPE_FEEDHORSE:
            ret.put("feedvalue", "0");
            break;
        case ItemEffect.TYPE_GETHORSE:
            ret.put("name", "未命名");
            ret.put("level", "1");
            ret.put("imageid", "0");
            ret.put("horsetype", "0");
            break;
        case ItemEffect.TYPE_DROPGROUP:
            ret.put("dropgroup1", "-1");
            ret.put("dropgroup2", "-1");
            ret.put("controlvar", "");
            ret.put("switchcount", "0");
            ret.put("mail", "0");
            ret.put("useitem", "-1");
            ret.put("useitemcount", "0");
            break;
        case ItemEffect.TYPE_TELEPORT:
            ret.put("weimapid", "0");
            ret.put("weix", "0");
            ret.put("weiy", "0");
            ret.put("shumapid", "0");
            ret.put("shux", "0");
            ret.put("shuy", "0");
            ret.put("wumapid", "0");
            ret.put("wux", "0");
            ret.put("wuy", "0");
            break;
        case ItemEffect.TYPE_FORMULA:
            ret.put("formula", "0");
            break;
        case ItemEffect.TYPE_MARRIAGETELEPORT:
        case ItemEffect.TYPE_DIVORCE:
            break;
        case ItemEffect.TYPE_EXTENDBAG:
            ret.put("count", "0");
            break;
        case ItemEffect.TYPE_REFRESHSKILLPOINTS:
            break;
        case ItemEffect.TYPE_EXTENDSTORAGE:
            ret.put("count", "0");
            break;
        case ItemEffect.TYPE_EXP:
            ret.put("amount", "0.0");
            ret.put("table", "");
            break;
        case ItemEffect.TYPE_HORSEEXP:
            ret.put("amount", "0.0");
            ret.put("table", "");
            break;
        case ItemEffect.TYPE_HONOR:
            ret.put("amount", "0.0");
            ret.put("table", "");
            break;
        case ItemEffect.TYPE_CREDIT:
            ret.put("amount", "0.0");
            ret.put("table", "");
            break;
        case ItemEffect.TYPE_MONEY:
            ret.put("amount", "0.0");
            ret.put("table", "");
            break;
        case ItemEffect.TYPE_NATIONSKILL:
            ret.put("skillid", "0");
            break;
        case ItemEffect.TYPE_REFRESH_PROPERTY:
            break;
        case ItemEffect.TYPE_REPAIR_EQUIPMENTS:
            break;
        case ItemEffect.TYPE_IMONEY_CARD:
            break;
        case ItemEffect.TYPE_RIDDLE:
            break;
        case ItemEffect.TYPE_UNSIGNUP:
            break;
        case ItemEffect.TYPE_CLICKEXP:
        	break;
        case ItemEffect.TYPE_TONGBATTLECAR:
        	break;
        case ItemEffect.TYPE_ADDKINGHP:
            ret.put("rate", "0");
            break;
        case ItemEffect.TYPE_CLICKGETMONEY:
            ret.put("money", "0");
        	break;
        case ItemEffect.TYPE_ACTIVEPOWER:
            ret.put("value", "0");
            break;
        case ItemEffect.TYPE_CONTRIBUTEPOINT:
            ret.put("value", "0");
            break;
        case ItemEffect.TYPE_GETIMONEY:
            ret.put("value", "0");
            break;
        case ItemEffect.TYPE_USEDECITEM:
            ret.put("itemid", "0");
            break;
        case ItemEffect.TYPE_SKILL_POINT:
            ret.put("skillpoint", "0");
            break;
        case ItemEffect.TYPE_ACTIVITY:
            ret.put("ty", "0");
            break;
        case ItemEffect.TYPE_TOWERDEFEND:
            break;
        case ItemEffect.TYPE_TALISMAN:
            ret.put("talismanindex", "0");
            break;
        case ItemEffect.TYPE_DECIMONEY:
            ret.put("value", "0");
            break;
        case ItemEffect.TYPE_EXTEND_HORSE_BAG_MAX:
            ret.put("value", "0");
            break;
        }
        
        return ret;
    }

}
