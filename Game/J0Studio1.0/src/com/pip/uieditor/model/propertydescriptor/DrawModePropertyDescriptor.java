package com.pip.uieditor.model.propertydescriptor;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class DrawModePropertyDescriptor extends PropertyDescriptor{
	static final String[] ITEMS = { "NONE", "FILL", "SCALE" };
	
	public DrawModePropertyDescriptor(Object id, String displayName) {
		super(id, displayName);
	}

   public CellEditor createPropertyEditor(Composite parent) {
       CellEditor editor = new TransComboBoxCellEditor(parent, ITEMS,
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
		return new DrawModeLabelProvider();
   }
   
   static class DrawModeLabelProvider extends LabelProvider {
   	
   	
		@Override
		public String getText(Object element) {
			if(element instanceof Integer) {
				return ITEMS[((Integer)element).intValue()];
			}
			return "";
		}
   	
   }
   
   static class TransComboBoxCellEditor extends ComboBoxCellEditor {

		public TransComboBoxCellEditor() {
			super();
		}

		public TransComboBoxCellEditor(Composite parent, String[] items,
				int style) {
			super(parent, items, style);
		}

		public TransComboBoxCellEditor(Composite parent, String[] items) {
			super(parent, items);
		}
   }
}
