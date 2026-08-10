package com.pip.uieditor.model.propertydescriptor;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

public class IntPropertyDescriptor extends TextPropertyDescriptor {

	public IntPropertyDescriptor(Object id, String displayName) {
		super(id, displayName);
	}
	
    public CellEditor createPropertyEditor(Composite parent) {
        CellEditor editor = new IntCellEditor(parent);
        if (getValidator() != null) {
			editor.setValidator(getValidator());
		}
        return editor;
    }
    
    static class IntCellEditor extends TextCellEditor {

		public IntCellEditor() {
			super();
			initValidator();
		}

		public IntCellEditor(Composite parent, int style) {
			super(parent, style);
			initValidator();
		}

		public IntCellEditor(Composite parent) {
			super(parent);
			initValidator();
		}
		
		protected void initValidator() {
			setValidator(new ICellEditorValidator() {
				@Override
				public String isValid(Object value) {
					if(value instanceof Integer)
						return null;
					else if(value instanceof String) {
						try {
							Integer.parseInt((String)value);
							return null;
						} catch (NumberFormatException e) {
							e.printStackTrace();
						}
					}
					return "±ÿ–Î ‰»Î ˝◊÷";
				}

			});
		}

		@Override
		protected Object doGetValue() {
			return Integer.decode((String)super.doGetValue());
		}

		@Override
		protected void doSetValue(Object value) {
			if(!(value instanceof Integer)) 
				throw new IllegalArgumentException();
			super.doSetValue(value.toString());
		}
    	
		
    }
}
