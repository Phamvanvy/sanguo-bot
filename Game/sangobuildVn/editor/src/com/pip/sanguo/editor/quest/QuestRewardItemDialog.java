package com.pip.sanguo.editor.quest;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.pip.sanguo.data.quest.QuestRewardItem;
import com.pip.sanguo.data.quest.QuestTarget;
import com.pip.sanguo.data.quest.pqe.ExpressionList;
import com.pip.sanguo.editor.util.ItemChooser;

public class QuestRewardItemDialog extends Dialog {
    private Combo comboType;
    private Text textAmount;
    private Label labelCount, labelItem;
    private ItemChooser itemChooser;
    private QuestRewardItem rewardItem;
    
    /**
     * Create the dialog
     * @param parentShell
     */
    public QuestRewardItemDialog(Shell parentShell, QuestRewardItem reward) {
        super(parentShell);
        this.rewardItem = reward;
    }

    /**
     * Create contents of the dialog
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        container.setLayout(gridLayout);

        final Label label_2 = new Label(container, SWT.NONE);
        label_2.setText("奖励类型：");

        comboType = new Combo(container, SWT.READ_ONLY);
        comboType.setItems(new String[] {"金钱", "经验", "荣誉", "声望", "物品"});
        final GridData gd_comboType = new GridData(SWT.FILL, SWT.CENTER, true, false);
        comboType.setLayoutData(gd_comboType);
        comboType.addModifyListener(new ModifyListener() {
        	public void modifyText(final ModifyEvent e) {
        		onTypeChanged();
        	}
        });

        labelCount = new Label(container, SWT.NONE);
        final GridData gd_labelCount = new GridData();
        labelCount.setLayoutData(gd_labelCount);
        labelCount.setText("奖励数量：");

        textAmount = new Text(container, SWT.BORDER);
        final GridData gd_textAmount = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textAmount.setLayoutData(gd_textAmount);

        labelItem = new Label(container, SWT.NONE);
        labelItem.setText("奖励物品：");

        itemChooser = new ItemChooser(container, SWT.NONE);
        itemChooser.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        
        // 设置初始数据
        switch (rewardItem.rewardType) {
        case QuestRewardItem.REWARD_MONEY:
        	comboType.select(0);
        	break;
        case QuestRewardItem.REWARD_EXP:
        	comboType.select(1);
        	break;
        case QuestRewardItem.REWARD_HONOR:
        	comboType.select(2);
        	break;
        case QuestRewardItem.REWARD_CREDIT:
            comboType.select(3);
            break;
        default:
        	comboType.select(4);
        	break;
        }
        if (rewardItem.rewardType == QuestRewardItem.REWARD_ITEM) {
        	textAmount.setText(String.valueOf(rewardItem.itemCount));
        	itemChooser.setItemID(rewardItem.rewardValue);
        } else {
        	textAmount.setText(String.valueOf(rewardItem.rewardValue));
        	labelItem.setVisible(false);
        	itemChooser.setVisible(false);
        }
        
        return container;
    }
    
    private void onTypeChanged() {
    	int sel = comboType.getSelectionIndex();
    	if (sel == 4) {
    		labelItem.setVisible(true);
    		itemChooser.setVisible(true);
    	} else {
    		labelItem.setVisible(false);
    		itemChooser.setVisible(false);
    	}
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
        return new Point(500, 190);
    }
    
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("任务奖励");
    }

    
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
        	switch (comboType.getSelectionIndex()) {
	        case 0:
	        	rewardItem.rewardType = QuestRewardItem.REWARD_MONEY;
	        	break;
	        case 1:
	        	rewardItem.rewardType = QuestRewardItem.REWARD_EXP;
	        	break;
	        case 2:
	        	rewardItem.rewardType = QuestRewardItem.REWARD_HONOR;
	        	break;
	        case 3:
	            rewardItem.rewardType = QuestRewardItem.REWARD_CREDIT;
                break;
	        default:
	        	rewardItem.rewardType = QuestRewardItem.REWARD_ITEM;
	        	break;
	        }
    		if (rewardItem.rewardType == QuestRewardItem.REWARD_ITEM) {
    			rewardItem.rewardValue = itemChooser.getItemID();
    			
    			//选择物品无效
    			if(rewardItem.rewardValue == -1){
    			    MessageDialog.openError(getShell(), "错误", "选择物品无效。");
    			    return;
    			}
    			
    			try {
    				rewardItem.itemCount = Integer.parseInt(textAmount.getText());
    			} catch (Exception e) {
    				MessageDialog.openError(getShell(), "错误", "数量输入错误。");
    			}
	        } else {
    			try {
    				rewardItem.rewardValue = Integer.parseInt(textAmount.getText());
    			} catch (Exception e) {
    				MessageDialog.openError(getShell(), "错误", "数量输入错误。");
    			}
	        }
        }
        super.buttonPressed(buttonId);
    }
}
