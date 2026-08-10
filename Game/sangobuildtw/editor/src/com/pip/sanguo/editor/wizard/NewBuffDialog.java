package com.pip.sanguo.editor.wizard;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.pip.sanguo.data.skill.SkillConfig;
import com.pip.util.AutoSelectAll;

public class NewBuffDialog extends Dialog {
    
    private Combo comboType;
    private Text textName;
    
    private int type;
    private String name;
    
    /**
     * Create the dialog
     * @param parentShell
     */
    public NewBuffDialog(Shell parentShell) {
        super(parentShell);
    }

    /**
     * Create contents of the dialog
     * @param parent
     */
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        final GridLayout gridLayout = new GridLayout();
        container.setLayout(gridLayout);

        final Label label = new Label(container, SWT.NONE);
        label.setText("BUFF名称：");

        textName = new Text(container, SWT.BORDER);
        textName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        textName.setText("新BUFF");
        textName.addFocusListener(AutoSelectAll.instance);

        final Label label_1 = new Label(container, SWT.NONE);
        label_1.setText("BUFF类型：");

        comboType = new Combo(container, SWT.READ_ONLY);
        comboType.setItems(new String[] {"临时BUFF（主动技能、物品）", "永久BUFF（被动技能）", "装备BUFF（套装、称号）" });
        comboType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        comboType.select(0);
        
        return container;
    }
    
    public String getName(){
        return name;
    }
    
    public int getType(){
        return type;
    }
    
    /**
     * Create contents of the button bar
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, "确定", true);
        createButton(parent, IDialogConstants.CANCEL_ID, "取消", false);
    }
    
    /**
     * Return the initial size of the dialog
     */
    @Override
    protected Point getInitialSize() {
        return new Point(427, 209);
    }
    
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("新建BUFF");
    }
    
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            name = textName.getText().trim();
            type = comboType.getSelectionIndex();
            if (name.length() == 0) {
                MessageDialog.openError(getShell(), "错误", "名称不能为空。");
                return;
            }
        }
        super.buttonPressed(buttonId);
    }
}
