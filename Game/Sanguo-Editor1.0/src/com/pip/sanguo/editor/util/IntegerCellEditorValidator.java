package com.pip.sanguo.editor.util;

import org.eclipse.jface.viewers.ICellEditorValidator;

public class IntegerCellEditorValidator implements ICellEditorValidator {
    public String isValid(Object value) {
        try {
            String v = (String)value;
            if (v.startsWith("a")) {
                v = v.substring(1);
            }
            Integer.parseInt(v);
            return null;
        } catch (Exception e) {
            return "∏Ò Ω¥ÌŒÛ";
        }
    }
}
