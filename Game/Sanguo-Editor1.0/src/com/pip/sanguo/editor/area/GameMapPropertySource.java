package com.pip.sanguo.editor.area;

import java.util.List;

import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.Faction;
import com.pip.sanguo.data.map.GameMapInfo;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.property.LocationPropertyDescriptor;
import com.pip.sanguo.editor.property.SoundPropertyDescriptor;

/**
 * 地图属性页。
 * @author lighthu
 */
public class GameMapPropertySource implements IPropertySource {
    private GameAreaEditor owner;
    private GameMapInfo mapInfo;
    
    public GameMapPropertySource(GameAreaEditor owner, GameMapInfo mapInfo) {
        this.owner = owner;
        this.mapInfo = mapInfo;
    }
    
    public Object getEditableValue() {
        return this;
    }

    public IPropertyDescriptor[] getPropertyDescriptors() {
        IPropertyDescriptor[] ret = new IPropertyDescriptor[15];
        ret[0] = new PropertyDescriptor("type", "类型");
        ret[1] = new PropertyDescriptor("id", "ID");
        ret[2] = new TextPropertyDescriptor("name", "场景名称");
        ret[3] = new LocationPropertyDescriptor("renascenceWei", "复活点（魏）");
        ret[4] = new LocationPropertyDescriptor("renascenceShu", "复活点（蜀）");
        ret[5] = new LocationPropertyDescriptor("renascenceWu", "复活点（吴）");
        ret[6] = new ComboBoxPropertyDescriptor("neutral", "是否中立", new String[] { "是", "否" });
        ret[7] = new ComboBoxPropertyDescriptor("allowDuel", "允许决斗", new String[] { "是", "否" });
        ret[8] = new ComboBoxPropertyDescriptor("protect", "屠杀保护", new String[] { "不保护", "被杀1次后保护", "被杀2次后保护" });
        ret[9] = new ComboBoxPropertyDescriptor("allowFollow", "允许跟随", new String[] { "是", "否" });
        ret[10] = new ComboBoxPropertyDescriptor("splitFaction", "隔离阵营", new String[] { "是", "否" });
        ret[11] = new SoundPropertyDescriptor("backgroundMusic", "背景音乐");
        List<DataObject> factions = EditorApplication.getInstance().getProjectData().getDictDataListByType(Faction.class);
        String[] labels = new String[factions.size()];
        for (int i = 0; i < factions.size(); i++) {
            labels[i] = factions.get(i).toString();
        }
        ret[12] = new ComboBoxPropertyDescriptor("faction", "阵营", labels);
        ret[13] = new TextPropertyDescriptor("maxPlayer", "最大玩家数");
        ret[14] = new ComboBoxPropertyDescriptor("oldMap", "过期地图", new String[] { "否", "是" });
        return ret;
    }

    public Object getPropertyValue(Object id) {
        if ("type".equals(id)) {
            return "场景";
        } else if ("id".equals(id)) {
            return mapInfo.getGlobalID() + "(0x" + Integer.toHexString(mapInfo.getGlobalID()) + ")";
        } 
        else if ("name".equals(id)) {
            return mapInfo.name;
        } 
        else if ("renascenceWei".equals(id)) {
            int[] renascence = new int[3];
            System.arraycopy(mapInfo.renascenceWei, 0, renascence, 0, 3);
            return renascence;
        } 
        else if ("renascenceShu".equals(id)) {
            int[] renascence = new int[3];
            System.arraycopy(mapInfo.renascenceShu, 0, renascence, 0, 3);
            return renascence;
        } 
        else if ("renascenceWu".equals(id)) {
            int[] renascence = new int[3];
            System.arraycopy(mapInfo.renascenceWu, 0, renascence, 0, 3);
            return renascence;
        }
        else if ("neutral".equals(id)) {
            return mapInfo.neutral ? new Integer(0) : new Integer(1);
        }
        else if ("allowDuel".equals(id)) {
            return mapInfo.allowDuel ? new Integer(0) : new Integer(1);
        }
        else if ("protect".equals(id)) {
            return new Integer(mapInfo.protect);
        }
        else if ("allowFollow".equals(id)) {
            return mapInfo.allowFollow ? new Integer(0) : new Integer(1);
        }
        else if ("splitFaction".equals(id)) {
            return mapInfo.splitFaction ? new Integer(0) : new Integer(1);
        }
        else if ("backgroundMusic".equals(id)) {
            return new Integer(mapInfo.backgroundMusic);
        }else if ("faction".equals(id)) {
            if (mapInfo.faction == null) {
                return -1;
            }
            return EditorApplication.getInstance().getProjectData().getDictObjectIndex(mapInfo.faction);
        } else if ("maxPlayer".equals(id)) {
            return String.valueOf(mapInfo.maxPlayer);
        } else if ("oldMap".equals(id)) {
            return mapInfo.oldMap ? new Integer(1) : new Integer(0);
        }
        else {
            throw new IllegalArgumentException();
        }
    }

    public boolean isPropertySet(Object id) {
        return false;
    }

    public void resetPropertyValue(Object id) {}

    public void setPropertyValue(Object id, Object value) {
        if ("name".equals(id)) {
            if (!value.equals(mapInfo.name)) {
                mapInfo.name = (String)value;
                owner.setDirty(true);
            }
        }
        else if("renascenceWei".equals(id)){
            int[] newValue = (int[])value;
            if (newValue[0] != mapInfo.renascenceWei[0] || 
                newValue[1] != mapInfo.renascenceWei[1] || 
                newValue[2] != mapInfo.renascenceWei[2]) {
                System.arraycopy(newValue, 0, mapInfo.renascenceWei, 0, 3);
                
                //当另外两个国家的复活点没有设置时，设置为当前一样
                if(mapInfo.renascenceShu[0] == -1){
                    System.arraycopy(newValue, 0, mapInfo.renascenceShu, 0, 3);
                }
                if(mapInfo.renascenceWu[0] == -1){
                    System.arraycopy(newValue, 0, mapInfo.renascenceWu, 0, 3);
                }
                owner.setDirty(true);
            }
        }
        else if("renascenceShu".equals(id)){
            int[] newValue = (int[])value;
            if (newValue[0] != mapInfo.renascenceShu[0] || 
                newValue[1] != mapInfo.renascenceShu[1] || 
                newValue[2] != mapInfo.renascenceShu[2]) {
                System.arraycopy(newValue, 0, mapInfo.renascenceShu, 0, 3);
                //当另外两个国家的复活点没有设置时，设置为当前一样
                if(mapInfo.renascenceWei[0] == -1){
                    System.arraycopy(newValue, 0, mapInfo.renascenceWei, 0, 3);
                }
                if(mapInfo.renascenceWu[0] == -1){
                    System.arraycopy(newValue, 0, mapInfo.renascenceWu, 0, 3);
                }
                owner.setDirty(true);
            }
        }
        else if("renascenceWu".equals(id)){
            int[] newValue = (int[])value;
            if (newValue[0] != mapInfo.renascenceWu[0] || 
                newValue[1] != mapInfo.renascenceWu[1] || 
                newValue[2] != mapInfo.renascenceWu[2]) {
                System.arraycopy(newValue, 0, mapInfo.renascenceWu, 0, 3);
                //当另外两个国家的复活点没有设置时，设置为当前一样
                if(mapInfo.renascenceWei[0] == -1){
                    System.arraycopy(newValue, 0, mapInfo.renascenceWei, 0, 3);
                }
                if(mapInfo.renascenceShu[0] == -1){
                    System.arraycopy(newValue, 0, mapInfo.renascenceShu, 0, 3);
                }
                owner.setDirty(true);
            }
        } else if ("neutral".equals(id)) {
            boolean newValue = new Integer(0).equals(value);
            if (newValue != mapInfo.neutral) {
                mapInfo.neutral = newValue;
                owner.setDirty(true);
            }
        } else if ("allowDuel".equals(id)) {
            boolean newValue = new Integer(0).equals(value);
            if (newValue != mapInfo.allowDuel) {
                mapInfo.allowDuel = newValue;
                owner.setDirty(true);
            }
        } else if ("protect".equals(id)) {
            int newValue = ((Integer)value).intValue();
            if (newValue != mapInfo.protect) {
                mapInfo.protect = newValue;
                owner.setDirty(true);
            }
        } else if ("allowFollow".equals(id)) {
            boolean newValue = new Integer(0).equals(value);
            if (newValue != mapInfo.allowFollow) {
                mapInfo.allowFollow = newValue;
                owner.setDirty(true);
            }
        } else if ("splitFaction".equals(id)) {
            boolean newValue = new Integer(0).equals(value);
            if (newValue != mapInfo.splitFaction) {
                mapInfo.splitFaction = newValue;
                owner.setDirty(true);
            }
        } else if ("backgroundMusic".equals(id)) {
            int newValue = ((Integer)value).intValue();
            if (newValue != mapInfo.backgroundMusic) {
                mapInfo.backgroundMusic = newValue;
                owner.setDirty(true);
            }
        }else if ("faction".equals(id)) {
            int index = ((Integer)value).intValue();
            Faction newValue;
            if (index != -1) {
                newValue = (Faction)EditorApplication.getInstance().getProjectData().getDictDataListByType(Faction.class).get(index);
            } else {
                newValue = null;
            }
            if (newValue != mapInfo.faction) {
                mapInfo.faction = newValue;
                owner.setDirty(true);
            }
        } else if ("maxPlayer".equals(id)) {
            try {
                int v = Integer.parseInt((String)value);
                if (v != mapInfo.maxPlayer) {
                    mapInfo.maxPlayer = v;
                    owner.setDirty(true);
                }
            } catch (Exception e) {
            }
        } else if ("oldMap".equals(id)) {
            boolean newValue = new Integer(1).equals(value);
            if (newValue != mapInfo.oldMap) {
                mapInfo.oldMap = newValue;
                owner.setDirty(true);
            }
        }
    }
}
