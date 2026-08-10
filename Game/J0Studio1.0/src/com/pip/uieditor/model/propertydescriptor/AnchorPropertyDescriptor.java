package com.pip.uieditor.model.propertydescriptor;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;


public class AnchorPropertyDescriptor extends PropertyDescriptor {
	static final String[] ITEMS = {"居中", "左上", "上", "右上", "右", "右下", "下", "左下", "左"};
	
	public AnchorPropertyDescriptor(Object id, String displayName) {
		super(id, displayName);
	}

   public CellEditor createPropertyEditor(Composite parent) {
       CellEditor editor = new AnchorComboBoxCellEditor(parent, ITEMS,
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
		return new AnchorLabelProvider();
   }
   
   static class AnchorLabelProvider extends LabelProvider {
   	
   	
		@Override
		public String getText(Object element) {
			if(element instanceof Integer) {
				return ITEMS[((Integer)element).intValue()];
			}
			return "";
		}
   	
   }
   
   static class AnchorComboBoxCellEditor extends ComboBoxCellEditor {

		public AnchorComboBoxCellEditor() {
			super();
		}

		public AnchorComboBoxCellEditor(Composite parent, String[] items,
				int style) {
			super(parent, items, style);
		}

		public AnchorComboBoxCellEditor(Composite parent, String[] items) {
			super(parent, items);
		}
   }
}



