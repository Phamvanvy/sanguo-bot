package com.pip.sanguo.editor.property;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.pip.sanguo.data.map.GameMapObject;
import com.pip.sanguo.editor.EditorApplication;

/**
 * 属性描述：选择NPC。
 */
public class NPCPropertyDescriptor extends PropertyDescriptor {
    public NPCPropertyDescriptor(Object id, String displayName) {
        super(id, displayName);
    }

    public CellEditor createPropertyEditor(Composite parent) {
        CellEditor editor = new NPCCellEditor(parent);
        if (getValidator() != null) {
            editor.setValidator(getValidator());
        }
        return editor;
    }

    public ILabelProvider getLabelProvider() {
        if (isLabelProviderSet()) {
            return super.getLabelProvider();
        }
        return new NPCLabelProvider();
    }

    /**
     * NPC名字显示，根据NPC ID查找NPC。
     * @author lighthu
     */
    public static class NPCLabelProvider extends LabelProvider {
        public String getText(Object element) {
            int npcID = ((Integer)element).intValue();
            return GameMapObject.toString(EditorApplication.getProj(), npcID);
        }
    }
}
