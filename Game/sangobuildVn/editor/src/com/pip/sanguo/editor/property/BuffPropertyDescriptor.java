package com.pip.sanguo.editor.property;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.pip.sanguo.data.NPCTemplate;
import com.pip.sanguo.data.map.GameMapObject;
import com.pip.sanguo.data.skill.BuffConfig;
import com.pip.sanguo.data.skill.SkillConfig;
import com.pip.sanguo.editor.EditorApplication;

/**
 * 属性描述：选择BUFF，允许空（不允许选择被动技能BUFF）。
 */
public class BuffPropertyDescriptor extends PropertyDescriptor {
    public BuffPropertyDescriptor(Object id, String displayName) {
        super(id, displayName);
    }

    public CellEditor createPropertyEditor(Composite parent) {
        CellEditor editor = new BuffCellEditor(parent);
        if (getValidator() != null) {
            editor.setValidator(getValidator());
        }
        return editor;
    }

    public ILabelProvider getLabelProvider() {
        if (isLabelProviderSet()) {
            return super.getLabelProvider();
        }
        return new BuffLabelProvider();
    }

    public static class BuffLabelProvider extends LabelProvider {
        public String getText(Object element) {
            int buffID = ((Integer)element).intValue();
            return BuffConfig.toString(EditorApplication.getProj(), buffID);
        }
    }
}
