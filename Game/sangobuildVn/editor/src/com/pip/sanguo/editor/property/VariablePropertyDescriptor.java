package com.pip.sanguo.editor.property;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.pip.sanguo.data.quest.QuestInfo;
import com.pip.sanguo.data.quest.pqe.PQEUtils;

/**
 * 属性描述：变量。变量可能是全局变量，也可以是局部变量。
 * @author lighthu
 */
public class VariablePropertyDescriptor extends PropertyDescriptor {
    protected QuestInfo questInfo;
    protected boolean localOnly;
    
    /**
     * 创建一个变量编辑描述。
     * @param id
     * @param displayName
     * @param qinfo 任务信息（包含任务变量信息）
     * @param localOnly 如果为true，则只显示任务变量
     */
    public VariablePropertyDescriptor(Object id, String displayName, QuestInfo qinfo, boolean localOnly) {
        super(id, displayName);
        questInfo = qinfo;
        this.localOnly = localOnly;
    }

    public CellEditor createPropertyEditor(Composite parent) {
        String[] titles, values;
        if (localOnly) {
            titles = new String[questInfo.variables.size()];
            values = new String[questInfo.variables.size()];
            for (int i = 0; i < questInfo.variables.size(); i++) {
                titles[i] = questInfo.variables.get(i).name;
                values[i] = questInfo.variables.get(i).name;
            }
        } else {
            titles = new String[questInfo.variables.size() + PQEUtils.SYSTEM_VARS.length];
            values = new String[questInfo.variables.size() + PQEUtils.SYSTEM_VARS.length];
            for (int i = 0; i < questInfo.variables.size(); i++) {
                titles[i] = questInfo.variables.get(i).name;
                values[i] = questInfo.variables.get(i).name;
            }
            for (int i = 0; i < PQEUtils.SYSTEM_VARS.length; i++) {
                titles[i + questInfo.variables.size()] = PQEUtils.SYSTEM_VARS[i].description;
                values[i + questInfo.variables.size()] = PQEUtils.SYSTEM_VARS[i].name;
            }
        }
        CellEditor editor = new FreeComboCellEditor(parent, titles, values);
        if (getValidator() != null) {
            editor.setValidator(getValidator());
        }
        return editor;
    }
}
