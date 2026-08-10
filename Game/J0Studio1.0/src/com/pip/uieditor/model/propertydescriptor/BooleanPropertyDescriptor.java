package com.pip.uieditor.model.propertydescriptor;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class BooleanPropertyDescriptor extends PropertyDescriptor {

	 static final String[] ITEMS = {"NO", "YES"};
	
    public BooleanPropertyDescriptor(Object id, String displayName) {
        super(id, displayName);
    }

    public CellEditor createPropertyEditor(Composite parent) {
        CellEditor editor = new BooleanComboBoxCellEditor(parent, ITEMS,
                SWT.READ_ONLY);
        if (getValidator() != null) {
			editor.setValidator(getValidator());
		}
        return editor;
    }

    public ILabelProvider getLabelProvider() {
        if (isLabelProviderSet()) {
			return super.getLabelProvider();
		}
		return new BooleanLabelProvider();
    }
    
    static class BooleanLabelProvider extends LabelProvider {
    	
    	
		@Override
		public String getText(Object element) {
			if(element instanceof Boolean) {
				if(element.equals(Boolean.TRUE)) 
					return ITEMS[1];
				else 
					return ITEMS[0];
			}
			return "";
		}
    	
    }
    
    static class BooleanComboBoxCellEditor extends ComboBoxCellEditor {

		public BooleanComboBoxCellEditor() {
			super();
		}

		public BooleanComboBoxCellEditor(Composite parent, String[] items,
				int style) {
			super(parent, items, style);
		}

		public BooleanComboBoxCellEditor(Composite parent, String[] items) {
			super(parent, items);
		}

		@Override
		protected Object doGetValue() {
			Integer index = (Integer)super.doGetValue();
			return index.intValue() == 0 ? Boolean.FALSE : Boolean.TRUE;
		}

		@Override
		protected void doSetValue(Object value) {
			if(value == null)
				return;
			int index = value.equals(Boolean.FALSE) ? 0 : 1;
			super.doSetValue(index);
		}
    	
		
    }
}
