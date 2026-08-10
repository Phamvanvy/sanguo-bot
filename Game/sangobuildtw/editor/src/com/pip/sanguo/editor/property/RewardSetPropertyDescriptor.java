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
 * 属性描述：分支奖励。
 * @author lighthu
 */
public class RewardSetPropertyDescriptor extends PropertyDescriptor {
    protected QuestInfo questInfo;
    
    /**
     * 创建一个变量编辑描述。
     * @param id
     * @param displayName
     * @param qinfo 任务信息（包含任务变量信息）
     * @param localOnly 如果为true，则只显示任务变量
     */
    public RewardSetPropertyDescriptor(Object id, String displayName, QuestInfo qinfo) {
        super(id, displayName);
        questInfo = qinfo;
    }

    public CellEditor createPropertyEditor(Composite parent) {
        String[] titles = new String[questInfo.owner.rewards.size()];
        for (int i = 0; i < titles.length; i++) {
            titles[i] = questInfo.owner.rewards.get(i).toString();
        }
        CellEditor editor = new ComboBoxCellEditor(parent, titles, SWT.READ_ONLY);
        if (getValidator() != null) {
            editor.setValidator(getValidator());
        }
        return editor;
    }

    public ILabelProvider getLabelProvider() {
        if (isLabelProviderSet()) {
            return super.getLabelProvider();
        }
        return new RewardSetLabelProvider();
    }

    /**
     * 提问选项集合的显示提供者。它会把多个选项用\n分开拼成一个大字符串。
     * @author lighthu
     */
    public class RewardSetLabelProvider extends LabelProvider {
        public String getText(Object element) {
            int id = ((Integer)element).intValue();
            if (id >= 0 && id < questInfo.owner.rewards.size()) {
                return questInfo.owner.rewards.get(id).toString();
            }
            return "空";
        }
    }
}
