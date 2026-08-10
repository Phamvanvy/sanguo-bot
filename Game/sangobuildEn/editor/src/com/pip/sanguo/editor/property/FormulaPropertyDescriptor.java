package com.pip.sanguo.editor.property;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.pip.sanguo.data.NPCTemplate;
import com.pip.sanguo.data.item.Formula;
import com.pip.sanguo.data.map.GameMapObject;
import com.pip.sanguo.data.skill.SkillConfig;
import com.pip.sanguo.editor.EditorApplication;

/**
 * 属性描述：选择打造配方。
 */
public class FormulaPropertyDescriptor extends PropertyDescriptor {
    public FormulaPropertyDescriptor(Object id, String displayName) {
        super(id, displayName);
    }

    public CellEditor createPropertyEditor(Composite parent) {
        CellEditor editor = new FormulaCellEditor(parent);
        if (getValidator() != null) {
            editor.setValidator(getValidator());
        }
        return editor;
    }

    public ILabelProvider getLabelProvider() {
        if (isLabelProviderSet()) {
            return super.getLabelProvider();
        }
        return new FormulaLabelProvider();
    }

    public static class FormulaLabelProvider extends LabelProvider {
        public String getText(Object element) {
            int formulaID = ((Integer)element).intValue();
            return Formula.toString(EditorApplication.getProj(), formulaID);
        }
    }
}
