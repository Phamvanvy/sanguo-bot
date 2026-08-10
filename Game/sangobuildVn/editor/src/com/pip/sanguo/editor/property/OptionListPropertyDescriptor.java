package com.pip.sanguo.editor.property;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

/**
 * 属性描述：提问选项列表。提问选项列表是用\n分隔开的多个字符串。
 */
public class OptionListPropertyDescriptor extends PropertyDescriptor {
    public OptionListPropertyDescriptor(Object id, String displayName) {
        super(id, displayName);
    }

    public CellEditor createPropertyEditor(Composite parent) {
        CellEditor editor = new OptionListCellEditor(parent);
        if (getValidator() != null) {
            editor.setValidator(getValidator());
        }
        return editor;
    }

    public ILabelProvider getLabelProvider() {
        if (isLabelProviderSet()) {
            return super.getLabelProvider();
        }
        return new OptionListLabelProvider();
    }

    /**
     * 提问选项集合的显示提供者。它会把多个选项用\n分开拼成一个大字符串。
     * @author lighthu
     */
    public static class OptionListLabelProvider extends LabelProvider {
        public String getText(Object element) {
            return ((String)element).replace("\n", "，");
        }
    }
}
