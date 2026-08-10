package com.pip.sanguo.editor.item;

import java.util.Iterator;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.pip.sanguo.data.Shop;
import com.pip.sanguo.data.item.Formula;
import com.pip.sanguo.editor.shop.BuyRequirementEditor;

public class EditFormulaRequirementsDialog extends Dialog {
    private BuyRequirementEditor[] editors;
    private Shop.BuyRequirement[] editObjects;
    private Formula editItem;
    
    /**
     * Create the dialog
     * @param parentShell
     */
    public EditFormulaRequirementsDialog(Shell parentShell, Formula item) {
        super(parentShell);
        this.editItem = item;
        
        // 固定最多10个额外条件。扣除金钱/i币/荣誉的条件不在这里编辑，而是在上层界面中
        // 直接编辑。
        editObjects = new Shop.BuyRequirement[10];
        int index = 0;
        for (Shop.BuyRequirement req : item.requirements) {
            if ((req.type == Shop.TYPE_HONOR || req.type == Shop.TYPE_IMONEY ||
                    req.type == Shop.TYPE_MONEY || req.type == Shop.TYPE_EXP) && req.deduct) {
                continue;
            }
            editObjects[index] = req.dup();
            index++;
        }
        while (index < 10) {
            editObjects[index] = new Shop.BuyRequirement();
            editObjects[index].type = Shop.TYPE_RANK;
            index++;
        }
    }

    /**
     * Create contents of the dialog
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new GridLayout());

        editors = new BuyRequirementEditor[10];
        for (int i = 0; i < 10; i++) {
            editors[i] = new BuyRequirementEditor(container, SWT.NONE, editObjects[i]);
            editors[i].setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        }

        return container;
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
        return new Point(600, 400);
    }
    
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("编辑购买条件");
    }

    
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            // 删除所有旧的条件
            Iterator<Shop.BuyRequirement> itor = editItem.requirements.iterator();
            while (itor.hasNext()) {
                Shop.BuyRequirement req = itor.next();
                if ((req.type == Shop.TYPE_HONOR || req.type == Shop.TYPE_IMONEY ||
                        req.type == Shop.TYPE_MONEY || req.type == Shop.TYPE_EXP) && req.deduct) {
                    continue;
                }
                itor.remove();
            }
            
            // 添加新条件
            for (int i = 0; i < 10; i++) {
                editors[i].save();
                if (editObjects[i].isValid()) {
                    editItem.requirements.add(editObjects[i].dup());
                }
            }
        }
        super.buttonPressed(buttonId);
    }
    
    public static boolean open(Shell parentShell, Formula item) {
        EditFormulaRequirementsDialog dlg = new EditFormulaRequirementsDialog(parentShell, item);
        if (dlg.open() == OK) {
            return true;
        } else {
            return false;
        }
    }
}
