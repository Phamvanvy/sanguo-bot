package com.pip.uieditor.model.propertydescriptor;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class FontSizePropertyDescriptor extends PropertyDescriptor {
	static final String[] ITEMS = {"SMALL","MEDIUM","LARGE"};
	
	public FontSizePropertyDescriptor(Object id, String displayName) {
		super(id, displayName);
	}

   public CellEditor createPropertyEditor(Composite parent) {
       CellEditor editor = new FontComboBoxCellEditor(parent, ITEMS,
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
		return new FontLabelProvider();
   }
   
   static class FontLabelProvider extends LabelProvider {
   	
   	
		@Override
		public String getText(Object element) {
			if(element instanceof Integer) {
				return ITEMS[((Integer)element).intValue()];
			}
			return "";
		}
   	
   }
   
   static class FontComboBoxCellEditor extends ComboBoxCellEditor {

		public FontComboBoxCellEditor() {
			super();
		}

		public FontComboBoxCellEditor(Composite parent, String[] items,
				int style) {
			super(parent, items, style);
		}

		public FontComboBoxCellEditor(Composite parent, String[] items) {
			super(parent, items);
		}
		
//		@Override
//		protected Object doGetValue() {
//			return (Integer)super.doGetValue() + 1;
//		}
//
//		@Override
//		protected void doSetValue(Object value) {
//			super.doSetValue((Integer)value + 1);
//		}
		
		
   }
}
