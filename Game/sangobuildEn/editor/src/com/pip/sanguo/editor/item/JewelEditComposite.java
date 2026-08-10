package com.pip.sanguo.editor.item;

import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;

import com.pip.propertysheet.PropertySheetEntry;
import com.pip.propertysheet.PropertySheetViewer;
import com.pip.sanguo.data.equipment.AttributeCalculator;
import com.pip.sanguo.data.item.Item;
import com.pip.sanguo.data.item.ItemEffect;
import com.pip.sanguo.editor.DefaultDataObjectEditor;
import com.pip.sanguo.editor.util.Constants;
import com.pip.sanguo.editor.util.IconChooser;
import com.pip.util.AutoSelectAll;

/**
 * 宝石属性编辑控件
 * @author Joy Yan
 *
 */
public class JewelEditComposite extends Composite  implements SelectionListener, ModifyListener {
    private Combo comboJewelLevel;
    private Combo comboJewelAttr;
    private Combo comboJewelType;
    private Combo comboItemType;
    /**
     * 是否有瑕疵
     */
    private Button isFlawButton;
    
    private Text textDescription;
    /**
     * 是否绑定
     */
    private Combo comboBind;
    
    /**
     * 出售价格
     */
    private Text textPrice;
    
    /**
     * 物品名称
     */
    private Text textTitle;
    
    /**
     * 物品品质
     */
    private Combo comboQuality;
    /**
     * 物品ID
     */
    private Text textID;
    /**
     * 是否可以出售
     */
    private Combo comboSale;
    /**
     * 物品等级
     */
    private Text textLevel;
    
    /**
     * 物品在每个物品栏的堆叠数量
     */
    private Text textAddtion;

    /**
     * 消息处理
     */
    private DefaultDataObjectEditor eventHandle;
    
    /**
     * 需要修改的数据对象
     */
    private Item editObject;
    
    private IconChooser iconChooser;
    private boolean updating = false;
    

    public JewelEditComposite(Composite parent, int style, DefaultDataObjectEditor handle) {
        super(parent, style);
        eventHandle = handle;
        
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 4;
        setLayout(gridLayout);
        
        final Label label = new Label(this, SWT.NONE);
        label.setLayoutData(new GridData());
        label.setText("ID：");

        textID = new Text(this, SWT.BORDER);
        textID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        textID.setEditable(false);
        textID.addFocusListener(AutoSelectAll.instance);
        textID.addModifyListener(eventHandle);

        final Label label_1 = new Label(this, SWT.NONE);
        label_1.setText("物品名称：");

        textTitle = new Text(this, SWT.BORDER);
        textTitle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        textTitle.addFocusListener(AutoSelectAll.instance);
        textTitle.addModifyListener(eventHandle);

        final Label label_26 = new Label(this, SWT.NONE);
        label_26.setText("物品类型：");

        comboItemType = new Combo(this, SWT.READ_ONLY);
        comboItemType.setVisibleItemCount(15);
        final GridData gd_comboItemType = new GridData(SWT.FILL, SWT.CENTER, true, false);
        comboItemType.setLayoutData(gd_comboItemType);
        comboItemType.setItems(ItemEditComposite.COMBO_TYPES);
        comboItemType.addSelectionListener(this);

        final Label label_7 = new Label(this, SWT.NONE);
        label_7.setText("宝石等级：");

        comboJewelLevel = new Combo(this, SWT.READ_ONLY);
        comboJewelLevel.setItems(new String[] {"1级", "2级", "3级", "4级", "5级", "6级", "7级"});
        comboJewelLevel.setVisibleItemCount(10);
        comboJewelLevel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        comboJewelLevel.addSelectionListener(this);

        final Label label_5 = new Label(this, SWT.NONE);
        label_5.setLayoutData(new GridData());
        label_5.setText("堆叠数量：");

        textAddtion = new Text(this, SWT.BORDER);
        textAddtion.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
        textAddtion.addFocusListener(AutoSelectAll.instance);
        textAddtion.addModifyListener(eventHandle);

        final Label label_13 = new Label(this, SWT.NONE);
        label_13.setLayoutData(new GridData());
        label_13.setText("物品品质：");

        comboQuality = new Combo(this, SWT.READ_ONLY);
        comboQuality.setVisibleItemCount(10);
        final GridData gd_comboQuality = new GridData(SWT.FILL, SWT.CENTER, true, false);
        comboQuality.setLayoutData(gd_comboQuality);
        comboQuality.setItems(Constants.COMBO_QUALITY);
        comboQuality.addSelectionListener(this);

        final Label label_9 = new Label(this, SWT.NONE);
        label_9.setLayoutData(new GridData());
        label_9.setText("物品等级：");

        textLevel = new Text(this, SWT.BORDER);
        textLevel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        textLevel.addFocusListener(AutoSelectAll.instance);
        textLevel.addModifyListener(this);

        final Label label_4 = new Label(this, SWT.NONE);
        label_4.setText("是否绑定：");

        comboBind = new Combo(this, SWT.READ_ONLY);
        comboBind.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        comboBind.setItems(Constants.COMBO_BIND);
        comboBind.addSelectionListener(this);

        final Label label_16 = new Label(this, SWT.NONE);
        label_16.setLayoutData(new GridData());
        label_16.setText("能否出售：");

        comboSale = new Combo(this, SWT.READ_ONLY);
        comboSale.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        comboSale.addSelectionListener(this);
        comboSale.setItems(Constants.COMBO_YES_NO);

        final Label label_2 = new Label(this, SWT.NONE);
        label_2.setText("出售价格：");
        
        textPrice = new Text(this, SWT.BORDER);
        textPrice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        textPrice.addFocusListener(AutoSelectAll.instance);
        textPrice.addModifyListener(eventHandle);

        final Label label_3 = new Label(this, SWT.NONE);
        label_3.setText("宝石类型：");

        comboJewelType = new Combo(this, SWT.READ_ONLY);
        comboJewelType.setItems(new String[] {"人物", "坐骑"});
        final GridData gd_comboJewelType = new GridData(SWT.FILL, SWT.CENTER, true, false);
        comboJewelType.setLayoutData(gd_comboJewelType);
        comboJewelType.addSelectionListener(this);

        final Label label_6 = new Label(this, SWT.NONE);
        label_6.setText("宝石属性：");

        comboJewelAttr = new Combo(this, SWT.READ_ONLY);
        comboJewelAttr.setVisibleItemCount(30);
        final GridData gd_comboJewelAttr = new GridData(SWT.FILL, SWT.CENTER, true, false);
        comboJewelAttr.setLayoutData(gd_comboJewelAttr);
        String[] attrs = new String[AttributeCalculator.ATTRIBUTES.length];
        for (int i = 0; i < AttributeCalculator.ATTRIBUTES.length; i++) {
            attrs[i] = AttributeCalculator.ATTRIBUTES[i].name;
        }
        comboJewelAttr.setItems(attrs);
        comboJewelAttr.addSelectionListener(this);

        final Label label_22 = new Label(this, SWT.NONE);
        label_22.setText("物品图标：");
        
        iconChooser = new IconChooser(this, SWT.NONE, 0);
        iconChooser.setHandler(eventHandle);
        iconChooser.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        isFlawButton = new Button(this, SWT.CHECK);
        isFlawButton.addSelectionListener(this);
        isFlawButton.setText("是否有瑕疵");
        new Label(this, SWT.NONE);

        final Label label_12_1 = new Label(this, SWT.NONE);
        label_12_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        label_12_1.setText("物品说明：");

        textDescription = new Text(this, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.WRAP);
        final GridData gd_textDescription_1 = new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1);
        gd_textDescription_1.heightHint = 71;
        textDescription.setLayoutData(gd_textDescription_1);
        textDescription.addModifyListener(eventHandle);
    }
    
    public void saveItemData(Item itemDataDef) throws Exception {
        itemDataDef.title = textTitle.getText();
        itemDataDef.bind = comboBind.getSelectionIndex();
        itemDataDef.description = textDescription.getText();
        itemDataDef.type = ItemEditComposite.TYPE_MAP[comboItemType.getSelectionIndex()];
        itemDataDef.playerLevel = comboJewelLevel.getSelectionIndex() + 1;
        try {
            itemDataDef.addition = Integer.parseInt(textAddtion.getText());
        } catch (NumberFormatException e) {
            throw new Exception("叠加数量输入格式错误！");
        }
        itemDataDef.iconIndex = iconChooser.getIconIndex();
        if (itemDataDef.iconIndex < 0) {
            throw new Exception("请选择正确的图标！");
        }
        itemDataDef.quality = comboQuality.getSelectionIndex();
        itemDataDef.sale = comboSale.getSelectionIndex() == Item.ATTRIBUTE_VALUE_YES;
        if (itemDataDef.sale) {
            try {
                itemDataDef.price = Integer.parseInt(textPrice.getText());
            } catch (NumberFormatException e) {
                throw new Exception("价格输入格式错误！");
            }
        }
        try {
            itemDataDef.level = Integer.parseInt(textLevel.getText());
        } catch (Exception e) {
            throw new Exception("级别输入错误！");
        }
        itemDataDef.isHorseJewel = comboJewelType.getSelectionIndex() == 1;
        itemDataDef.setJewelAttrType(comboJewelAttr.getSelectionIndex());
        if (!itemDataDef.isHorseJewel && itemDataDef.jewelAttrType == AttributeCalculator.ATTRIBUTE_SPEED) {
            throw new Exception("只有坐骑宝石才能加速度。");
        }

        itemDataDef.instance = false;
        itemDataDef.taskFlag = false;
        itemDataDef.available = Item.AVAILABLE_NO;
        itemDataDef.isFlaw = isFlawButton.getSelection();
    }
     
    /**
     * 把一个物品对象的数据赋值到对应的界面控件
     * @param item
     */
    private void setItemData(){
        updating = true;
        textID.setText(String.valueOf(editObject.id));
        textTitle.setText(editObject.title);
        comboBind.select(editObject.bind);
        textDescription.setText(editObject.description);
        for (int i = 0; i < ItemEditComposite.TYPE_MAP.length; i++) {
            if (editObject.type == ItemEditComposite.TYPE_MAP[i]) {
                comboItemType.select(i);
            }
        }
        comboJewelLevel.select(editObject.playerLevel - 1);
        textAddtion.setText(String.valueOf(editObject.addition));
        iconChooser.setIcon(editObject.iconIndex);
        comboQuality.select(editObject.quality);
        if(editObject.sale){
            comboSale.select(Item.ATTRIBUTE_VALUE_YES);
            textPrice.setText(String.valueOf(editObject.price));            
        }
        else{
            comboSale.select(Item.ATTRIBUTE_VALUE_NO);
            textPrice.setEditable(false);
        }
        textLevel.setText(String.valueOf(editObject.level));
        comboJewelType.select(editObject.isHorseJewel ? 1 : 0);
        comboJewelAttr.select(editObject.jewelAttrType);
        isFlawButton.setSelection(editObject.isFlaw);
        updating = false;
    }
    
    public void setInput(Item input){
        editObject = input;
        setItemData();
    }
    
    /**
     * 为物品增加一个使用效果
     */
    public void onAdd(){
        ItemEffect newEffect = new ItemEffect();
        editObject.effects.add(newEffect);
        eventHandle.setDirty(true);
    }
    
    /*
     * 重新计算宝石属性。
     */
    public void recalcJewelAttr() {
        if (!updating) {
            editObject.resetDescription();
            textDescription.setText(editObject.description);
        }
    }
    
    public void modifyText(ModifyEvent e) {
        if (e.getSource() == textLevel) {
            try {
                editObject.level = Integer.parseInt(textLevel.getText());
                recalcJewelAttr();
            } catch (Exception e1) {
            }
        }
    }
    
    public void widgetDefaultSelected(SelectionEvent e) {}
    
    /**
     * 下拉列表框选择消息处理
     */
    public void widgetSelected(SelectionEvent e) {
        if (e.getSource() == comboItemType) {
            ((ItemEditor)eventHandle).changeType(ItemEditComposite.TYPE_MAP[comboItemType.getSelectionIndex()]);
        }
        else if(e.getSource() == comboSale){
            if(comboSale.getSelectionIndex() == Item.ATTRIBUTE_VALUE_YES){
                textPrice.setEditable(true);
            }
            else if(comboSale.getSelectionIndex() == Item.ATTRIBUTE_VALUE_NO){
                textPrice.setEditable(false);
            }
        } else if (e.getSource() == comboJewelAttr) {
            editObject.setJewelAttrType(comboJewelAttr.getSelectionIndex());
            if(isFlawButton.getSelection() == true){
                editObject.isFlaw = true;
            } else {
                editObject.isFlaw = false;
            }
            recalcJewelAttr();
        } else if (e.getSource() == isFlawButton) {
            if(isFlawButton.getSelection() == true){
                editObject.isFlaw = true;
            } else {
                editObject.isFlaw = false;
            }
            editObject.setJewelAttrType(comboJewelAttr.getSelectionIndex());
            recalcJewelAttr();
        } else {
            eventHandle.setDirty(true);
        }
    }
}
