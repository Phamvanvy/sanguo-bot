package com.pip.sanguo.editor.property;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.pip.sanguo.data.NPCTemplate;
import com.pip.sanguo.data.map.GameMapObject;
import com.pip.sanguo.data.quest.Quest;
import com.pip.sanguo.editor.EditorApplication;

/**
 *  Ù–‘√Ë ˆ£∫—°‘ÒNPCƒ£∞Â°£
 */
public class QuestPropertyDescriptor extends PropertyDescriptor {
    public QuestPropertyDescriptor(Object id, String displayName) {
        super(id, displayName);
    }

    public CellEditor createPropertyEditor(Composite parent) {
        CellEditor editor = new QuestCellEditor(parent);
        if (getValidator() != null) {
            editor.setValidator(getValidator());
        }
        return editor;
    }

    public ILabelProvider getLabelProvider() {
        if (isLabelProviderSet()) {
            return super.getLabelProvider();
        }
        return new QuestLabelProvider();
    }

    public static class QuestLabelProvider extends LabelProvider {
        public String getText(Object element) {
            int questID = ((Integer)element).intValue();
            return Quest.toString(EditorApplication.getProj(), questID);
        }
    }
}
