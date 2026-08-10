package com.pip.sanguo.editor.property;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.pip.sanguo.data.NPCTemplate;
import com.pip.sanguo.data.Title;
import com.pip.sanguo.data.map.GameMapObject;
import com.pip.sanguo.data.skill.BuffConfig;
import com.pip.sanguo.data.skill.SkillConfig;
import com.pip.sanguo.editor.EditorApplication;

/**
 *  Ù–‘√Ë ˆ£∫—°‘Ò≥∆∫≈°£
 */
public class TitlePropertyDescriptor extends PropertyDescriptor {
    public TitlePropertyDescriptor(Object id, String displayName) {
        super(id, displayName);
    }

    public CellEditor createPropertyEditor(Composite parent) {
        CellEditor editor = new TitleCellEditor(parent);
        if (getValidator() != null) {
            editor.setValidator(getValidator());
        }
        return editor;
    }

    public ILabelProvider getLabelProvider() {
        if (isLabelProviderSet()) {
            return super.getLabelProvider();
        }
        return new TitleLabelProvider();
    }

    public static class TitleLabelProvider extends LabelProvider {
        public String getText(Object element) {
            int titleID = ((Integer)element).intValue();
            return Title.toString(EditorApplication.getProj(), titleID);
        }
    }
}
