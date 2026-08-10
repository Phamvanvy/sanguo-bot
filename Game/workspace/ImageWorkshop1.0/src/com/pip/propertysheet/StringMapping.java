package com.pip.propertysheet;

import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.pip.util.Distance;

public class StringMapping implements IPropertySource, IPropertySheetEnable {
    public String[] targets;
    public String[] choices;
    public int[] mapping;
    
    private class StringSelectorDescriptor extends ComboBoxPropertyDescriptor {
        public StringSelectorDescriptor(int index) {
            super(new Integer(index), targets[index], choices);
        }
        
        public CellEditor createPropertyEditor(Composite parent) {
            ComboBoxCellEditor editor = (ComboBoxCellEditor)super.createPropertyEditor(parent);
            CCombo combo = (CCombo)editor.getControl();
            combo.setVisibleItemCount(20);
            return editor;
        }
    }
    
    public void init(String[] left, String[] right) {
        targets = left;
        choices = right;
        mapping = new int[left.length];
        Distance dist = new Distance();
        for (int i = 0; i < mapping.length; i++) {
            String find = left[i];
            int minDist = 100000, minIndex = -1;
            for (int j = 0; j < right.length; j++) {
                int distValue = dist.LD(find, right[j]);
                if (distValue < minDist) {
                    minDist = distValue;
                    minIndex = j;
                    if (minDist == 0) {
                        break;
                    }
                }
            }
            if (minDist > find.length() / 3) {
                mapping[i] = 0;
            } else {
                mapping[i] = minIndex;
            }
        }
    }
    
    public Object getEditableValue() {
        return "StringMapping";
    }

    public IPropertyDescriptor[] getPropertyDescriptors() {
        IPropertyDescriptor[] ret = new IPropertyDescriptor[targets.length];
        for (int i = 0; i < targets.length; i++) {
            ret[i] = new StringSelectorDescriptor(i);
        }
        return ret;
    }

    public Object getPropertyValue(Object id) {
        int index = ((Integer)id).intValue();
        return new Integer(mapping[index]);
    }

    public boolean isPropertySet(Object id) {
        return false;
    }

    public void resetPropertyValue(Object id) {}

    public void setPropertyValue(Object id, Object value) {
        try {
            int index = ((Integer)id).intValue();
            int value1 = ((Integer)value).intValue();
            if (value1 >= 0 && value1 < choices.length) {
                mapping[index] = value1;
            }
        } catch (Exception e) {
        }
    }
    
    public IPropertySource getPropertySource() {
        return this;
    }
}
