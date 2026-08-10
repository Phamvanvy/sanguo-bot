package com.pip.sanguo.editor.property;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.pip.sanguo.data.HorseType;
import com.pip.sanguo.data.map.GameMapObject;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.util.Utils;

/**
 * 属性描述：选择坐骑类型。
 */
public class HorseTypePropertyDescriptor extends PropertyDescriptor {
    public HorseTypePropertyDescriptor(Object id, String displayName) {
        super(id, displayName);
    }

    public CellEditor createPropertyEditor(Composite parent) {
        CellEditor editor = new HorseTypeCellEditor(parent);
        if (getValidator() != null) {
            editor.setValidator(getValidator());
        }
        return editor;
    }

    public ILabelProvider getLabelProvider() {
        if (isLabelProviderSet()) {
            return super.getLabelProvider();
        }
        return new HorseTypeLabelProvider();
    }

    public static class HorseTypeLabelProvider extends LabelProvider {
        public String getText(Object element) {
            String[] secs = Utils.splitString((String)element, ',');
            if (secs.length == 0 && secs[0].length() == 0) {
                secs = new String[0];
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < secs.length; i++) {
                if (i > 0) {
                    sb.append(",");
                }
                HorseType ht = (HorseType)EditorApplication.getProj().findObject(HorseType.class, Integer.parseInt(secs[i]));
                sb.append(ht.title);
            }
            return sb.toString();
        }
    }
}
