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
 *  Ù–‘√Ë ˆ£∫—°‘ÒŒª÷√°£
 */
public class LocationPropertyDescriptor extends PropertyDescriptor {
    public LocationPropertyDescriptor(Object id, String displayName) {
        super(id, displayName);
    }

    public CellEditor createPropertyEditor(Composite parent) {
        CellEditor editor = new LocationCellEditor(parent);
        if (getValidator() != null) {
            editor.setValidator(getValidator());
        }
        return editor;
    }

    public ILabelProvider getLabelProvider() {
        if (isLabelProviderSet()) {
            return super.getLabelProvider();
        }
        return new LocationLabelProvider();
    }

    public static class LocationLabelProvider extends LabelProvider {
        public String getText(Object element) {
            int[] location = (int[])element;
            return GameMapInfo.locationToString(EditorApplication.getProj(), location, false);
        }
    }
}
