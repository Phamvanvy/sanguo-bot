package com.pip.sanguo.editor.property;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.pip.sanguo.data.quest.QuestInfo;
import com.pip.sanguo.data.quest.pqe.PQEUtils;

/**
 * 属性描述：混合格式文本。
 * @author lighthu
 */
public class RichTextPropertyDescriptor extends PropertyDescriptor {
    protected QuestInfo questInfo;
    
    /**
     * 创建一个变量编辑描述。
     * @param id
     * @param displayName
     * @param qinfo 任务信息（包含任务变量信息）
     */
    public RichTextPropertyDescriptor(Object id, String displayName, QuestInfo qinfo) {
        super(id, displayName);
        questInfo = qinfo;
    }

    public CellEditor createPropertyEditor(Composite parent) {
        CellEditor editor = new RichTextCellEditor(parent, questInfo);
        if (getValidator() != null) {
            editor.setValidator(getValidator());
        }
        return editor;
    }
}
