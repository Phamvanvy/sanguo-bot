package com.pip.uieditor.model.propertydescriptor;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.pip.uieditor.util.NumberUtil;

public class DimensionPropertyDescriptor extends TextPropertyDescriptor {
	
	public DimensionPropertyDescriptor(Object id, String displayName) {
		super(id, displayName);
	}
	
	public CellEditor createPropertyEditor(Composite parent) {
		return new DimensionCellEditor(parent);
	}
	
	public ILabelProvider getLabelProvider() {
		return new LabelProvider() {

			@Override
			public String getText(Object element) {
				Dimension dim = (Dimension)element;
				if(dim != null) {
					return dim.width + "," + dim.height;
				}
				return null;
			}
			
		};
	}
}

class DimensionCellEditor extends TextCellEditor {
	public DimensionCellEditor(Composite parent) {
		super(parent);
//		super.setValidator(new ICellEditorValidator() {
//			
//			@Override
//			public String isValid(Object value) {
//				if(NumberUtil.parseInts((String)value).length != 2)
//					return "∏Ò Ω¥ÌŒÛ";
//				return null;
//			}
//		});
	}

	@Override
	protected Object doGetValue() {
		try{
			int[] vs = NumberUtil.parseInts((String)super.doGetValue());
			return new Dimension(vs[0], vs[1]);
		} catch(Exception e){
			return new Dimension(0,0);
		}
	}

	@Override
	protected void doSetValue(Object value) {
		Dimension p = (Dimension)value;
		super.doSetValue(p.width + "," + p.height);
	}
}
