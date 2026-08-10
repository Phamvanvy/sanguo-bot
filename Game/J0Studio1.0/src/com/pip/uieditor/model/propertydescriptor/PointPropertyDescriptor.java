package com.pip.uieditor.model.propertydescriptor;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.pip.uieditor.util.NumberUtil;

public class PointPropertyDescriptor extends PropertyDescriptor {

	public PointPropertyDescriptor(Object id, String displayName) {
		super(id, displayName);
	}
	
	public CellEditor createPropertyEditor(Composite parent) {
		return new PointCellEditor(parent);
	}
	
	public ILabelProvider getLabelProvider() {
		return new LabelProvider() {
			@Override
			public String getText(Object element) {
				Point point = (Point)element;
				if(point != null) {
					return point.x + "," + point.y;
				}
				return null;
			}
		};
	}
}

class PointCellEditor extends TextCellEditor {
	public PointCellEditor(Composite parent) {
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
		int[] vs = NumberUtil.parseInts((String)super.doGetValue());
		return new Point(vs[0], vs[1]);
	}

	@Override
	protected void doSetValue(Object value) {
		Point p = (Point)value;
		super.doSetValue(p.x + "," + p.y);
	}
	
}
