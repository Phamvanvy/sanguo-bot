package com.pip.sanguo.editor.area;

import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.pip.sanguo.data.map.GameMapExit;
import com.pip.sanguo.data.map.GameMapExitConstraints;
import com.pip.sanguo.data.map.GameMapInfo;
import com.pip.sanguo.editor.property.GameMapExitConstraintsPropertyDescriptor;
import com.pip.sanguo.editor.property.LocationPropertyDescriptor;
import com.pip.util.Utils;

/**
 * 场景出口属性页。
 * @author lighthu
 */
public class GameMapExitPropertySource implements IPropertySource {
    private GameAreaEditor owner;
    private GameMapExit mapExit;
    
    public GameMapExitPropertySource(GameAreaEditor owner, GameMapExit mapExit) {
        this.owner = owner;
        this.mapExit = mapExit;
    }
    
    public Object getEditableValue() {
        return this;
    }

    public IPropertyDescriptor[] getPropertyDescriptors() {
        IPropertyDescriptor[] ret = new IPropertyDescriptor[7];
        ret[0] = new PropertyDescriptor("type", "类型");
        ret[1] = new PropertyDescriptor("id", "ID");
        ret[2] = new LocationPropertyDescriptor("targetLocation", "目标位置");
        ret[3] = new ComboBoxPropertyDescriptor("showName", "显示名称", new String[] { "是", "否" });
        ret[4] = new ComboBoxPropertyDescriptor("exitType", "通道类型", new String[] { "普通", "记录当前位置", "返回记录位置", "寻路用" });
        ret[5] = new TextPropertyDescriptor("positionVarName", "变量名称");
        ret[6] = new GameMapExitConstraintsPropertyDescriptor("constraints", "通过限制");
        return ret;
    }

    public Object getPropertyValue(Object id) {
        if ("type".equals(id)) {
            return "传送点";
        } else if ("id".equals(id)) {
            return mapExit.getGlobalID() + "(0x" + Integer.toHexString(mapExit.getGlobalID()) + ")";
        } else if ("targetLocation".equals(id)) {
            return new int[] { mapExit.targetMap, mapExit.targetX, mapExit.targetY };
        } else if ("showName".equals(id)) {
            return mapExit.showName ? 0 : 1;
        } else if ("exitType".equals(id)) {
            return new Integer(mapExit.exitType);
        } else if ("positionVarName".equals(id)) {
            return mapExit.positionVarName;
        } else if ("constraints".equals(id)) {
            return mapExit.constraints;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public boolean isPropertySet(Object id) {
        return false;
    }

    public void resetPropertyValue(Object id) {}

    public void setPropertyValue(Object id, Object value) {
        if ("targetLocation".equals(id)) {
            int[] newValue = (int[])value;
            if (newValue[0] != mapExit.targetMap || newValue[1] != mapExit.targetX || newValue[2] != mapExit.targetY) {
                mapExit.targetMap = newValue[0];
                mapExit.targetX = newValue[1];
                mapExit.targetY = newValue[2];
                owner.setDirty(true);
            }
        } else if ("showName".equals(id)) {
            boolean newValue = ((Integer)value).intValue() == 0;
            if (newValue != mapExit.showName) {
                mapExit.showName = newValue;
                owner.setDirty(true);
            }
        } else if ("exitType".equals(id)) {
            int newValue = ((Integer)value).intValue();
            if (mapExit.exitType != newValue) {
                mapExit.exitType = newValue;
                owner.setDirty(true);
            }
        } else if ("positionVarName".equals(id)) {
            String newValue = (String)value;
            if (!newValue.equals(mapExit.positionVarName)) {
                mapExit.positionVarName = newValue;
                owner.setDirty(true);
            }
        } else if ("constraints".equals(id)) {
            GameMapExitConstraints newValue = (GameMapExitConstraints)value;
            if (!newValue.equals(mapExit.constraints)) {
                mapExit.constraints = newValue;
                owner.setDirty(true);
            }
        }
    }
}
