package com.pip.sanguo.editor.shop;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.Rank;
import com.pip.sanguo.data.Shop;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.util.ItemChooser;

/**
 * 编辑一个购买需求的组件。
 * @author lighthu
 */
public class BuyRequirementEditor extends Composite {
    private Text textVarDesc;
    private Text textVarValue;
    private Text textVarName;
    private Text textAmount;
    private ItemChooser itemChooser;
    private Combo comboRank;
    private Combo comboType;
    private Label labelItem, labelAmount, labelRank;
    private Button buttonDeduct;
    private Shop.BuyRequirement editObject;
    private static final int[] TYPE_MAPPING = { Shop.TYPE_RANK, Shop.TYPE_ITEM, Shop.TYPE_HONOR, Shop.TYPE_MONEY, Shop.TYPE_IMONEY, Shop.TYPE_VARIABLE, Shop.TYPE_LEVEL, Shop.TYPE_CONSUMECODE, Shop.TYPE_EXP };
    private Label labelVarName;
    private Label labelVarValue;
    private Label labelVarDesc;
        
    /**
     * Create the composite
     * @param parent
     * @param style
     */
    public BuyRequirementEditor(Composite parent, int style, Shop.BuyRequirement editObj) {
        super(parent, style);
        this.editObject = editObj;
        
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 15;
        setLayout(gridLayout);

        final Label label = new Label(this, SWT.NONE);
        label.setText("类型：");

        comboType = new Combo(this, SWT.READ_ONLY);
        comboType.setVisibleItemCount(10);
        comboType.setItems(new String[] {"军衔", "物品", "荣誉", "金钱", "i币", "属性变量", "级别", "消费代码", "经验"});
        final GridData gd_comboType = new GridData(SWT.FILL, SWT.CENTER, true, false);
        comboType.setLayoutData(gd_comboType);
        comboType.addModifyListener(new ModifyListener() {
            public void modifyText(final ModifyEvent e) {
                updateType();
            }
        });

        labelItem = new Label(this, SWT.NONE);
        final GridData gd_labelItem = new GridData();
        labelItem.setLayoutData(gd_labelItem);
        labelItem.setText("物品：");

        itemChooser = new ItemChooser(this, SWT.NONE);
        final GridData gd_itemChooser = new GridData(SWT.FILL, SWT.CENTER, true, false);
        itemChooser.setLayoutData(gd_itemChooser);

        labelAmount = new Label(this, SWT.NONE);
        labelAmount.setLayoutData(new GridData());
        labelAmount.setText("数量：");

        textAmount = new Text(this, SWT.BORDER);
        final GridData gd_textAmount = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textAmount.setLayoutData(gd_textAmount);

        buttonDeduct = new Button(this, SWT.CHECK);
        buttonDeduct.setLayoutData(new GridData());
        buttonDeduct.setText("扣除");

        labelRank = new Label(this, SWT.NONE);
        labelRank.setLayoutData(new GridData());
        labelRank.setText("军衔：");

        comboRank = new Combo(this, SWT.READ_ONLY);
        final GridData gd_comboRank = new GridData(SWT.FILL, SWT.CENTER, true, false);
        comboRank.setLayoutData(gd_comboRank);
        
        // 设置军衔列表
        List<DataObject> ranks = EditorApplication.getProj().getDictDataListByType(Rank.class);
        String[] rankNames = new String[ranks.size()];
        for (int i = 0; i < rankNames.length; i++) {
            rankNames[i] = ((Rank)ranks.get(i)).title;
        }
        comboRank.setItems(rankNames);
        comboRank.select(0);

        labelVarName = new Label(this, SWT.NONE);
        labelVarName.setLayoutData(new GridData());
        labelVarName.setText("变量名：");

        textVarName = new Text(this, SWT.BORDER);
        final GridData gd_textVarName = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textVarName.setLayoutData(gd_textVarName);

        labelVarValue = new Label(this, SWT.NONE);
        labelVarValue.setLayoutData(new GridData());
        labelVarValue.setText("达到：");

        textVarValue = new Text(this, SWT.BORDER);
        final GridData gd_textVarValue = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textVarValue.setLayoutData(gd_textVarValue);

        labelVarDesc = new Label(this, SWT.NONE);
        labelVarDesc.setLayoutData(new GridData());
        labelVarDesc.setText("描述：");

        textVarDesc = new Text(this, SWT.BORDER);
        final GridData gd_textVarDesc = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textVarDesc.setLayoutData(gd_textVarDesc);
        
        update();
    }
    
    private void updateType() {
        int type = TYPE_MAPPING[comboType.getSelectionIndex()];
        switch (type) {
        case Shop.TYPE_HONOR:
        case Shop.TYPE_IMONEY:
        case Shop.TYPE_MONEY:
        case Shop.TYPE_EXP:
            showControl(labelAmount);
            showControl(textAmount);
            hideControl(labelItem);
            hideControl(itemChooser);
            hideControl(buttonDeduct);
            hideControl(labelRank);
            hideControl(comboRank);
            hideControl(labelVarName);
            hideControl(textVarName);
            hideControl(labelVarValue);
            hideControl(textVarValue);
            hideControl(labelVarDesc);
            hideControl(textVarDesc);
            break;
        case Shop.TYPE_ITEM:
            showControl(labelAmount);
            showControl(textAmount);
            showControl(labelItem);
            showControl(itemChooser);
            showControl(buttonDeduct);
            hideControl(labelRank);
            hideControl(comboRank);
            hideControl(labelVarName);
            hideControl(textVarName);
            hideControl(labelVarValue);
            hideControl(textVarValue);
            hideControl(labelVarDesc);
            hideControl(textVarDesc);
            break;
        case Shop.TYPE_RANK:
            hideControl(labelAmount);
            hideControl(textAmount);
            hideControl(labelItem);
            hideControl(itemChooser);
            hideControl(buttonDeduct);
            showControl(labelRank);
            showControl(comboRank);
            hideControl(labelVarName);
            hideControl(textVarName);
            hideControl(labelVarValue);
            hideControl(textVarValue);
            hideControl(labelVarDesc);
            hideControl(textVarDesc);
            break;
        case Shop.TYPE_VARIABLE:
            hideControl(labelAmount);
            hideControl(textAmount);
            hideControl(labelItem);
            hideControl(itemChooser);
            hideControl(buttonDeduct);
            hideControl(labelRank);
            hideControl(comboRank);
            showControl(labelVarName);
            showControl(textVarName);
            showControl(labelVarValue);
            showControl(textVarValue);
            showControl(labelVarDesc);
            showControl(textVarDesc);
            break;
        case Shop.TYPE_LEVEL:
            showControl(labelAmount);
            showControl(textAmount);
            hideControl(labelItem);
            hideControl(itemChooser);
            hideControl(buttonDeduct);
            hideControl(labelRank);
            hideControl(comboRank);
            hideControl(labelVarName);
            hideControl(textVarName);
            hideControl(labelVarValue);
            hideControl(textVarValue);
            hideControl(labelVarDesc);
            hideControl(textVarDesc);
            break;
        case Shop.TYPE_CONSUMECODE:
            hideControl(labelAmount);
            hideControl(textAmount);
            hideControl(labelItem);
            hideControl(itemChooser);
            hideControl(buttonDeduct);
            hideControl(labelRank);
            hideControl(comboRank);
            hideControl(labelVarName);
            showControl(textVarName);
            hideControl(labelVarValue);
            hideControl(textVarValue);
            hideControl(labelVarDesc);
            hideControl(textVarDesc);
            break;
        }
        layout();
    }
    
    private void hideControl(Control obj) {
        obj.setVisible(false);
        ((GridData)obj.getLayoutData()).exclude = true;
    }
    
    private void showControl(Control obj) {
        obj.setVisible(true);
        ((GridData)obj.getLayoutData()).exclude = false;
    }

    /**
     * 初始化界面数值。
     */
    public void update() {
        for (int i = 0; i < TYPE_MAPPING.length; i++) {
            if (editObject.type == TYPE_MAPPING[i]) {
                comboType.select(i);
                break;
            }
        }
        if (editObject.type != Shop.TYPE_RANK) {
            textAmount.setText(String.valueOf(editObject.amount));
        }
        if (editObject.type == Shop.TYPE_ITEM && editObject.item != null) {
            itemChooser.setItemID(editObject.item.id);
            buttonDeduct.setSelection(editObject.deduct);
        }
        if (editObject.type == Shop.TYPE_RANK) {
            comboRank.select(editObject.amount);
        }
        if (editObject.type == Shop.TYPE_VARIABLE) {
            textVarName.setText(editObject.varName);
            textVarValue.setText(String.valueOf(editObject.amount));
            textVarDesc.setText(editObject.varDesc);
        }
        if (editObject.type == Shop.TYPE_LEVEL) {
            textAmount.setText(String.valueOf(editObject.amount));
        }
        if (editObject.type == Shop.TYPE_CONSUMECODE) {
            textVarName.setText(editObject.varName);
        }
        updateType();
    }
    
    /**
     * 保存当前的结果。
     */
    public void save() {
        editObject.type = TYPE_MAPPING[comboType.getSelectionIndex()];
        switch (editObject.type) {
        case Shop.TYPE_HONOR:
        case Shop.TYPE_IMONEY:
        case Shop.TYPE_MONEY:
        case Shop.TYPE_EXP:
            try {
                editObject.amount = Integer.parseInt(textAmount.getText());
            } catch (Exception e) {
                editObject.amount = 0;
            }
            editObject.item = null;
            editObject.deduct = false;
            break;
        case Shop.TYPE_ITEM:
            try {
                editObject.amount = Integer.parseInt(textAmount.getText());
            } catch (Exception e) {
                editObject.amount = 0;
            }
            editObject.item = EditorApplication.getProj().findItemOrEquipment(itemChooser.getItemID());
            editObject.deduct = buttonDeduct.getSelection();
            break;
        case Shop.TYPE_RANK:
            editObject.amount = comboRank.getSelectionIndex();
            editObject.item = null;
            editObject.deduct = false;
            break;
        case Shop.TYPE_VARIABLE:
            editObject.varName = textVarName.getText();
            try {
                editObject.amount = Integer.parseInt(textVarValue.getText());
            } catch (Exception e) {
                editObject.amount = 0;
            }
            editObject.varDesc = textVarDesc.getText();
            break;
        case Shop.TYPE_LEVEL:
            try {
                editObject.amount = Integer.parseInt(textAmount.getText());
            } catch (Exception e) {
                editObject.amount = 0;
            }
            editObject.item = null;
            editObject.deduct = false;
            break;
        case Shop.TYPE_CONSUMECODE:
            editObject.varName = textVarName.getText();
            break;
        }
    }
}
