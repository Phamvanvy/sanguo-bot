package com.pip.sanguo.editor.property;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.pip.sanguo.data.map.GameMapInfo;
import com.pip.sanguo.data.map.GameMapObject;
import com.pip.sanguo.editor.EditorApplication;

/**
 *  Ù–‘√Ë ˆ£∫—°‘Ò≥°æ∞°£
 */
public class ScenePropertyDescriptor extends PropertyDescriptor {
    public ScenePropertyDescriptor(Object id, String displayName) {
        super(id, displayName);
    }

    public CellEditor createPropertyEditor(Composite parent) {
        CellEditor editor = new SceneCellEditor(parent);
        if (getValidator() != null) {
            editor.setValidator(getValidator());
        }
        return editor;
    }

    public ILabelProvider getLabelProvider() {
        if (isLabelProviderSet()) {
            return super.getLabelProvider();
        }
        return new SceneLabelProvider();
    }

    public static class SceneLabelProvider extends LabelProvider {
        public String getText(Object element) {
            int sceneID = ((Integer)element).intValue();
            return GameMapInfo.toString(EditorApplication.getProj(), sceneID);
        }
    }
}
