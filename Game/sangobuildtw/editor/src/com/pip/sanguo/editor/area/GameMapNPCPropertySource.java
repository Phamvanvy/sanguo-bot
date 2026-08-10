package com.pip.sanguo.editor.area;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.Faction;
import com.pip.sanguo.data.NPCTemplate;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.Period;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.property.NPCFunctionPropertyDescriptor;
import com.pip.sanguo.editor.property.NPCPropertyDescriptor;
import com.pip.sanguo.editor.property.NPCTemplatePropertyDescriptor;

/**
 * 场景出口属性页。
 * @author lighthu
 */
public class GameMapNPCPropertySource implements IPropertySource {
    private GameAreaEditor owner;
    private GameMapNPC mapNPC;
    
    public GameMapNPCPropertySource(GameAreaEditor owner, GameMapNPC mapNPC) {
        this.owner = owner;
        this.mapNPC = mapNPC;
    }
    
    public Object getEditableValue() {
        return this;
    }

    public IPropertyDescriptor[] getPropertyDescriptors() {
        IPropertyDescriptor[] ret = new IPropertyDescriptor[21];
        ret[0] = new PropertyDescriptor("type", "类型");
        ret[1] = new PropertyDescriptor("id", "ID");
        ret[2] = new NPCTemplatePropertyDescriptor("template", "模板");
        ret[3] = new TextPropertyDescriptor("name", "名称");
        List<DataObject> factions = EditorApplication.getInstance().getProjectData().getDictDataListByType(Faction.class);
        String[] labels = new String[factions.size()];
        for (int i = 0; i < factions.size(); i++) {
            labels[i] = factions.get(i).toString();
        }
        ret[4] = new ComboBoxPropertyDescriptor("faction", "阵营", labels);
        ret[5] = new ComboBoxPropertyDescriptor("visible", "是否可见", new String[] { "是", "否" });
        ret[6] = new ComboBoxPropertyDescriptor("canAttack", "是否可被攻击", new String[] { "是", "否" });
        ret[7] = new TextPropertyDescriptor("refreshInterval", "刷新间隔(秒)");
        ret[8] = new ComboBoxPropertyDescriptor("dynamicRefresh", "动态刷新", new String[] { "是", "否" });
        ret[9] = new TextPropertyDescriptor("linkDistance", "呼救距离(码)");
        ret[10] = new ComboBoxPropertyDescriptor("isGuard", "是否卫兵", new String[] { "是", "否" });
        ret[11] = new ComboBoxPropertyDescriptor("isStatic", "是否静态", new String[] { "是", "否" });
        ret[12] = new ComboBoxPropertyDescriptor("canpass", "可通过", new String[] { "是", "否" });
        ret[13] = new TextPropertyDescriptor("liveTime", "存活时间(秒)");
        ret[14] = new NPCPropertyDescriptor("dieRefresh", "死亡后刷新");
        ret[15] = new ComboBoxPropertyDescriptor("functional", "是否功能NPC", new String[] { "是", "否" });
        ret[16] = new NPCFunctionPropertyDescriptor("functions", "功能");
        ret[17] = new ComboBoxPropertyDescriptor("broadcastDie", "死亡后广播", new String[] { "是", "否" });
        ret[18] = new TextPropertyDescriptor("searchName", "寻路名称");
        ret[19] = new TextPropertyDescriptor("period","刷新时间段");
        ret[20] = new TextPropertyDescriptor("revision", "限制版本");
        return ret;
    }

    public Object getPropertyValue(Object id) {
        if ("type".equals(id)) {
            return "NPC";
        } else if ("id".equals(id)) {
            return mapNPC.getGlobalID() + "(0x" + Integer.toHexString(mapNPC.getGlobalID()) + ")";
        } else if ("template".equals(id)) {
            if (mapNPC.template == null) {
                return -1;
            }
            return new Integer(mapNPC.template.id);
        } else if ("name".equals(id)) {
            return mapNPC.name;
        } else if ("faction".equals(id)) {
            if (mapNPC.faction == null) {
                return -1;
            }
            return EditorApplication.getInstance().getProjectData().getDictObjectIndex(mapNPC.faction);
        } else if ("visible".equals(id)) {
            return mapNPC.visible ? 0 : 1;
        } else if ("canAttack".equals(id)) {
            return mapNPC.canAttack ? 0 : 1;
        } else if ("refreshInterval".equals(id)) {
            return String.valueOf(mapNPC.refreshInterval);
        } else if ("dynamicRefresh".equals(id)) {
            return mapNPC.dynamicRefresh ? 0 : 1;
        } else if ("linkDistance".equals(id)) {
            return String.valueOf(mapNPC.linkDistance / 8.0f);
        } else if ("isGuard".equals(id)) {
            return mapNPC.isGuard ? 0 : 1;
        } else if ("isStatic".equals(id)) {
            return mapNPC.isStatic ? 0 : 1;
        } else if ("canpass".equals(id)) {
            return mapNPC.canPass ? 0 : 1;
        } else if ("liveTime".equals(id)) {
            return String.valueOf(mapNPC.liveTime);
        } else if ("dieRefresh".equals(id)) {
            return mapNPC.dieRefresh;
        } else if ("functional".equals(id)) {
            return mapNPC.isFunctional ? 0 : 1;
        } else if ("functions".equals(id)) {
            return new String[] { mapNPC.functionName, mapNPC.functionScript };
        } else if ("broadcastDie".equals(id)) {
            return mapNPC.broadcastDie ? 0 : 1;
        } else if ("searchName".equals(id)) {
            return mapNPC.searchName;
        } else if ("period".equals(id)){
            if(mapNPC.periods.size()==0)
                return "";
            else{
                Period[] ps = new Period[mapNPC.periods.size()];
                mapNPC.periods.toArray(ps);
                return Period.getString(ps);
            }
        } else if ("revision".equals(id)) {
            return mapNPC.revision;
        }else {
            throw new IllegalArgumentException();
        }
    }

    public boolean isPropertySet(Object id) {
        return false;
    }

    public void resetPropertyValue(Object id) {}

    public void setPropertyValue(Object id, Object value) {
        if ("template".equals(id)) {
            int tid = ((Integer)value).intValue();
            NPCTemplate newValue = (NPCTemplate)EditorApplication.getProj().findObject(NPCTemplate.class, tid);
            if (newValue != mapNPC.template) {
                mapNPC.template = newValue;
                owner.setDirty(true);
            }                
        } else if ("name".equals(id)) {
            String newName = (String)value;
            newName = newName.trim();
            if (newName.length() > 0 && !newName.equals(mapNPC.name)) {
                mapNPC.name = newName;
                owner.setDirty(true);
            }
        } else if ("faction".equals(id)) {
            int index = ((Integer)value).intValue();
            Faction newValue;
            if (index != -1) {
                newValue = (Faction)EditorApplication.getInstance().getProjectData().getDictDataListByType(Faction.class).get(index);
            } else {
                newValue = null;
            }
            if (newValue != mapNPC.faction) {
                mapNPC.faction = newValue;
                owner.setDirty(true);
            }
        } else if ("visible".equals(id)) {
            int index = ((Integer)value).intValue();
            boolean newValue = (index == 0);
            if (newValue != mapNPC.visible) {
                mapNPC.visible = newValue;
                owner.setDirty(true);
            }
        } else if ("canAttack".equals(id)) {
            int index = ((Integer)value).intValue();
            boolean newValue = (index == 0);
            if (newValue != mapNPC.canAttack) {
                mapNPC.canAttack = newValue;
                owner.setDirty(true);
            }
        } else if ("refreshInterval".equals(id)) {
            try {
                int newValue = Integer.parseInt((String)value);
                if (newValue != mapNPC.refreshInterval) {
                    mapNPC.refreshInterval = newValue;
                    owner.setDirty(true);
                }
            } catch (Exception e) {
            }
        } else if ("dynamicRefresh".equals(id)) {
            int index = ((Integer)value).intValue();
            boolean newValue = (index == 0);
            if (newValue != mapNPC.dynamicRefresh) {
                mapNPC.dynamicRefresh = newValue;
                owner.setDirty(true);
            }
        } else if ("linkDistance".equals(id)) {
            try {
                int newValue = (int)(Float.parseFloat((String)value) * 8.0f);
                if (newValue != mapNPC.linkDistance) {
                    mapNPC.linkDistance = newValue;
                    owner.setDirty(true);
                }
            } catch (Exception e) {
            }
        } else if ("isGuard".equals(id)) {
            int index = ((Integer)value).intValue();
            boolean newValue = (index == 0);
            if (newValue != mapNPC.isGuard) {
                mapNPC.isGuard = newValue;
                owner.setDirty(true);
            }
        } else if ("isStatic".equals(id)) {
            int index = ((Integer)value).intValue();
            boolean newValue = (index == 0);
            if (newValue != mapNPC.isStatic) {
                mapNPC.isStatic = newValue;
                owner.setDirty(true);
            }
        } else if ("canpass".equals(id)) {
            int index = ((Integer)value).intValue();
            boolean newValue = (index == 0);
            if (newValue != mapNPC.canPass) {
                mapNPC.canPass = newValue;
                owner.setDirty(true);
            }
        } else if ("liveTime".equals(id)) {
            try {
                int newValue = Integer.parseInt((String)value);
                if (newValue != mapNPC.liveTime) {
                    mapNPC.liveTime = newValue;
                    owner.setDirty(true);
                }
            } catch (Exception e) {
            }
        } else if ("dieRefresh".equals(id)) {
            int newValue = ((Integer)value).intValue();
            if (newValue != mapNPC.dieRefresh) {
                mapNPC.dieRefresh = newValue;
                owner.setDirty(true);
            }
        } else if ("functional".equals(id)) {
            int index = ((Integer)value).intValue();
            boolean newValue = (index == 0);
            if (newValue != mapNPC.isFunctional) {
                mapNPC.isFunctional = newValue;
                owner.setDirty(true);
            }
        } else if ("functions".equals(id)) {
            String[] funcs = (String[])value;
            if (!funcs[0].equals(mapNPC.functionName) || !funcs[1].equals(mapNPC.functionScript)) {
                mapNPC.functionName = funcs[0];
                mapNPC.functionScript = funcs[1];
                owner.setDirty(true);
            }
        } else if ("broadcastDie".equals(id)) {
            int index = ((Integer)value).intValue();
            boolean newValue = (index == 0);
            if (newValue != mapNPC.broadcastDie) {
                mapNPC.broadcastDie = newValue;
                owner.setDirty(true);
            }
        } else if ("searchName".equals(id)) {
            String newValue = (String)value;
            if (!newValue.equals(mapNPC.searchName)) {
                mapNPC.searchName = newValue;
                owner.setDirty(true);
            }
        } else if("period".equals(id)){
            try {
                Period[] ps = Period.parse((String)value);
                List<Period> l = new ArrayList<Period>(ps.length);
                for(Period p:ps){
                    l.add(p);
                }
                mapNPC.periods = l;
                owner.setDirty(true);
            }
            catch (Exception e) {
            }
        } else if ("revision".equals(id)) {
            String newValue = (String)value;
            if (!newValue.equals(mapNPC.revision)) {
                mapNPC.revision = newValue;
                owner.setDirty(true);
            }
        }
    }
}
