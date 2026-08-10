package com.pip.sanguo.editor.property;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.pip.sanguo.data.item.Item;
import com.pip.sanguo.editor.EditorApplication;

public class ItemPropertyDescriptor extends PropertyDescriptor{

    public ItemPropertyDescriptor(Object id, String displayName) {
        super(id, displayName);
    }
    
    public CellEditor createPropertyEditor(Composite parent) {
        CellEditor editor = new ItemCellEditor(parent);
        if (getValidator() != null) {
            editor.setValidator(getValidator());
        }
        return editor;
    }

    public ILabelProvider getLabelProvider() {
        if (isLabelProviderSet()) {
            return super.getLabelProvider();
        }
        return new ItemProvider();
    }

    /**
     * 物品名称，根据物品ID查找物品。
     * @author Joy
     */
    public static class ItemProvider extends LabelProvider {
        public String getText(Object element) {
            int itemId = ((Integer)element).intValue();
            Item item = EditorApplication.getInstance().getProjectData().findItemOrEquipment(itemId);
            if(item != null){                
                return item.toString();
            }
            else{
                return "无效的物品";
            }
        }
    }

}
