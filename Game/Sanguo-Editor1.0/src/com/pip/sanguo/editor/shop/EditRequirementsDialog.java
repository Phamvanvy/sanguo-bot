package com.pip.sanguo.editor.shop;

import java.util.Iterator;
import java.util.List;

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
import com.pip.sanguo.data.TeleportSet;

public class EditRequirementsDialog extends Dialog {
    private BuyRequirementEditor[] editors;
    private Shop.BuyRequirement[] editObjects;
    private Object editItem;
    
    /**
     * Create the dialog
     * @param parentShell
     */
    public EditRequirementsDialog(Shell parentShell, Object shopItem) {
        super(parentShell);
        this.editItem = shopItem;
        
        // 固定最多10个额外条件。扣除金钱/i币/荣誉的条件不在这里编辑，而是在商店商品列表中
        // 直接编辑。
        editObjects = new Shop.BuyRequirement[10];
        int index = 0;
        List<Shop.BuyRequirement> list;
        if (shopItem instanceof Shop.ShopItem) {
            list = ((Shop.ShopItem)shopItem).requirements;
        } else {
            list = ((TeleportSet.Teleport)shopItem).requirements;
        }
        for (Shop.BuyRequirement req : list) {
            if ((req.type == Shop.TYPE_HONOR || req.type == Shop.TYPE_IMONEY ||
                    req.type == Shop.TYPE_MONEY || req.type == Shop.TYPE_EXP || 
                    req.type == Shop.TYPE_SALARY || req.type == Shop.TYPE_REPUTATION) && req.deduct) {
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
            List<Shop.BuyRequirement> list;
            if (editItem instanceof Shop.ShopItem) {
                list = ((Shop.ShopItem)editItem).requirements;
            } else {
                list = ((TeleportSet.Teleport)editItem).requirements;
            }
            Iterator<Shop.BuyRequirement> itor = list.iterator();
            while (itor.hasNext()) {
                Shop.BuyRequirement req = itor.next();
                if ((req.type == Shop.TYPE_HONOR || req.type == Shop.TYPE_IMONEY ||
                        req.type == Shop.TYPE_MONEY || req.type == Shop.TYPE_EXP || 
                        req.type == Shop.TYPE_SALARY || req.type == Shop.TYPE_REPUTATION) && req.deduct) {
                    continue;
                }
                itor.remove();
            }
            
            // 添加新条件
            for (int i = 0; i < 10; i++) {
                editors[i].save();
                if (editObjects[i].isValid()) {
                    list.add(editObjects[i].dup());
                }
            }
        }
        super.buttonPressed(buttonId);
    }
    
    public static boolean open(Shell parentShell, Object shopItem) {
        EditRequirementsDialog dlg = new EditRequirementsDialog(parentShell, shopItem);
        if (dlg.open() == OK) {
            return true;
        } else {
            return false;
        }
    }
}
