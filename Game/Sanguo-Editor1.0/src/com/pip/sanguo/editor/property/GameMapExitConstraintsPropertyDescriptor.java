package com.pip.sanguo.editor.property;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.pip.sanguo.data.map.GameMapExitConstraints;
import com.pip.sanguo.data.map.GameMapInfo;
import com.pip.sanguo.data.map.GameMapObject;
import com.pip.sanguo.editor.EditorApplication;

/**
 * 属性描述：出口限制。
 */
public class GameMapExitConstraintsPropertyDescriptor extends PropertyDescriptor {
    public GameMapExitConstraintsPropertyDescriptor(Object id, String displayName) {
        super(id, displayName);
    }

    public CellEditor createPropertyEditor(Composite parent) {
        CellEditor editor = new GameMapExitConstraintsCellEditor(parent);
        if (getValidator() != null) {
            editor.setValidator(getValidator());
        }
        return editor;
    }

    public ILabelProvider getLabelProvider() {
        if (isLabelProviderSet()) {
            return super.getLabelProvider();
        }
        return new GameMapExitConstraintsLabelProvider();
    }

    public static class GameMapExitConstraintsLabelProvider extends LabelProvider {
        public String getText(Object element) {
            GameMapExitConstraints c = (GameMapExitConstraints)element;
            return c.toString();
        }
    }
}
