package com.pip.sanguo.editor.property;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.pip.sanguo.data.NPCTemplate;
import com.pip.sanguo.data.map.GameMapObject;
import com.pip.sanguo.editor.EditorApplication;

/**
 *  Ù–‘√Ë ˆ£∫—°‘ÒNPCƒ£∞Â°£
 */
public class NPCTemplatePropertyDescriptor extends PropertyDescriptor {
    public NPCTemplatePropertyDescriptor(Object id, String displayName) {
        super(id, displayName);
    }

    public CellEditor createPropertyEditor(Composite parent) {
        CellEditor editor = new NPCTemplateCellEditor(parent);
        if (getValidator() != null) {
            editor.setValidator(getValidator());
        }
        return editor;
    }

    public ILabelProvider getLabelProvider() {
        if (isLabelProviderSet()) {
            return super.getLabelProvider();
        }
        return new NPCTemplateLabelProvider();
    }

    public static class NPCTemplateLabelProvider extends LabelProvider {
        public String getText(Object element) {
            int templateID = ((Integer)element).intValue();
            return NPCTemplate.toString(EditorApplication.getProj(), templateID);
        }
    }
}
