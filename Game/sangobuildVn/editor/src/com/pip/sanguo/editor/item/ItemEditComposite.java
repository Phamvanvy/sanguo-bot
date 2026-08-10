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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;

import com.pip.propertysheet.PropertySheetEntry;
import com.pip.propertysheet.PropertySheetViewer;
import com.pip.sanguo.data.item.Item;
import com.pip.sanguo.data.item.ItemEffect;
import com.pip.sanguo.editor.DefaultDataObjectEditor;
import com.pip.sanguo.editor.util.Constants;
import com.pip.sanguo.editor.util.IconChooser;
import com.pip.util.AutoSelectAll;

/**
 * 物品属性编辑控件
 * @author Joy Yan
 *
 */
public class ItemEditComposite extends Composite  implements ISelectionChangedListener,SelectionListener{
    
    private Combo comboBatchUseFlag;
    private Combo comboItemType;
    private Text textUseConfirm;
    private Combo comboUseClazz;
    private Text textDistance;
    private Combo comboTaskMuti;
    private Text textSchedule;
    private static final String[] COMBO_AVILIABLE = {"否","战斗中可用","非战斗中可用","任何时候可以使用"};
    private static final String[] COMBO_AUTOUSE = {"","默认自动","自定义自动","后台自动"};
    private static final String[] COMBO_AREA = {"无目标","自己","己方队友","敌方","全部"};
    public static final String[] COMBO_TYPES = { "普通物品", "技能书", "坐骑", "坐骑口粮", "称号", "材料", "打造配方", "宝石", "补给", "杂物", "精炼" ,"卡片", "法宝"};
    public static final int[] TYPE_MAP = { Item.TYPE_NORMAL, Item.TYPE_SKILLBOOK, Item.TYPE_HORSE, 
                                           Item.TYPE_HORSEFOOD, Item.TYPE_TITLE, Item.TYPE_MATERIAL, 
                                           Item.TYPE_FORMULA, Item.TYPE_JEWEL, Item.TYPE_RENEW, 
                                           Item.TYPE_MISC, Item.TYPE_REFINE ,Item.TYPE_CARD, 
                                           Item.TYPE_TALISMAN};
    
    
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
     * 物品冷却时间
     */
    private Text textColdDownTime;
    /**
     * 冷却组
     */
    private Text textColdDownGroup;
    
    /**
     * 是否消耗
     */
    private Combo comboWaste;
    /**
     * 使用次数
     */
    private Text textCount;
    
    /**
     * 物品品质
     */
    private Combo comboQuality;
    /**
     * 物品ID
     */
    private Text textID;
    /**
     * 时间类型
     */
    private Combo comboTimeType;
    /**
     * 使用者等级下限
     */
    private Text textPlayerLevel;
    /**
     * 使用范围
     */
    private Combo comboArea;
    /**
     * 是否可以出售
     */
    private Combo comboSale;
    /**
     * 物品等级
     */
    private Text textLevel;
    
    /**
     * 使用时效
     */
    private Text textTime;
    /**
     * 是否自动使用
     */
    private Combo comboAutoUse;
    /**
     * 是否可用
     */
    private Combo comboAvailable;
    /**
     * 是否是任务物品
     */
    private Combo comboTaskFlag;
    /**
     * 物品在每个物品栏的堆叠数量
     */
    private Text textAddtion;
    /**
     * 是否是实例类型
     */
    private Combo comboInstance;
    /**
     * 属性编辑
     */
    private PropertySheetViewer propertyEditor;
    /**
     * 使用效果列表
     */
    private Combo comboProperty;
    /**
     * 物品现有效果
     */
    private ListViewer effectList;
    
    /**
     * 新建
     */
    private Action addAction;
    
    /**
     * 删除
     */
    private Action deleteAction;
    
    /**
     * 消息处理
     */
    private DefaultDataObjectEditor eventHandle;
    
    /**
     * 需要修改的数据对象
     */
    private Item editObject;
    
    private IconChooser iconChooser;

    public ItemEditComposite(Composite parent, int style, DefaultDataObjectEditor handle) {
        super(parent, style);
        eventHandle = handle;
        
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 4;
        setLayout(gridLayout);
        
        createActions();
        
        final Label label = new Label(this, SWT.NONE);
        label.setLayoutData(new GridData());
        label.setText("ID：");

        textID = new Text(this, SWT.BORDER);
        textID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        textID.setEditable(false);
        textID.addFocusListener(AutoSelectAll.instance);

        final Label label_1 = new Label(this, SWT.NONE);
        label_1.setText("物品名称：");

        textTitle = new Text(this, SWT.BORDER);
        textTitle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        textTitle.addFocusListener(AutoSelectAll.instance);

        final Label label_26 = new Label(this, SWT.NONE);
        label_26.setText("物品类型：");

        comboItemType = new Combo(this, SWT.READ_ONLY);
        comboItemType.setVisibleItemCount(15);
        final GridData gd_comboItemType = new GridData(SWT.FILL, SWT.CENTER, true, false);
        comboItemType.setLayoutData(gd_comboItemType);
        comboItemType.setItems(new String[] {"普通物品", "技能书", "坐骑", "坐骑口粮", "称号", "材料", "打造配方", "宝石", "补给", "杂物", "精炼", "卡片", "法宝"});
        comboItemType.addSelectionListener(this);

        final Label label_27 = new Label(this, SWT.NONE);
        label_27.setText("批量使用：");

        comboBatchUseFlag = new Combo(this, SWT.NONE);
        comboBatchUseFlag.setItems(Constants.COMBO_YES_NO);
        comboBatchUseFlag.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));
        comboBatchUseFlag.addSelectionListener(this);

        final Label label_5 = new Label(this, SWT.NONE);
        label_5.setLayoutData(new GridData());
        label_5.setText("堆叠数量：");

        textAddtion = new Text(this, SWT.BORDER);
        textAddtion.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, false));

        final Label label_6 = new Label(this, SWT.NONE);
        label_6.setText("是否任务物品：");

        comboTaskFlag = new Combo(this, SWT.READ_ONLY);
        comboTaskFlag.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        comboTaskFlag.setItems(Constants.COMBO_YES_NO);
        comboTaskFlag.addSelectionListener(this);

        final Label label_7 = new Label(this, SWT.NONE);
        label_7.setLayoutData(new GridData());
        label_7.setText("是否可使用：");

        comboAvailable = new Combo(this, SWT.READ_ONLY);
        comboAvailable.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        comboAvailable.addSelectionListener(this);
        comboAvailable.setItems(COMBO_AVILIABLE);

        final Label label_3 = new Label(this, SWT.NONE);
        label_3.setText("是否消耗：");

        comboWaste = new Combo(this, SWT.READ_ONLY);
        final GridData gd_comboWaste = new GridData(SWT.FILL, SWT.CENTER, true, false);
        comboWaste.setLayoutData(gd_comboWaste);
        comboWaste.addSelectionListener(this);
        comboWaste.setItems(Constants.COMBO_YES_NO);
        

        final Label label_18 = new Label(this, SWT.NONE);
        label_18.setText("使用次数：");

        textCount = new Text(this, SWT.BORDER);
        final GridData gd_textCount = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textCount.setLayoutData(gd_textCount);

        final Label label_13 = new Label(this, SWT.NONE);
        label_13.setLayoutData(new GridData());
        label_13.setText("物品品质：");

        comboQuality = new Combo(this, SWT.READ_ONLY);
        comboQuality.setVisibleItemCount(10);
        final GridData gd_comboQuality = new GridData(SWT.FILL, SWT.CENTER, true, false);
        comboQuality.setLayoutData(gd_comboQuality);
        comboQuality.setItems(Constants.COMBO_QUALITY);

        final Label label_24 = new Label(this, SWT.NONE);
        label_24.setText("使用职业：");

        comboUseClazz = new Combo(this, SWT.READ_ONLY);
        comboUseClazz.setItems(new String[] {"武将", "刺客", "谋士", "方士", "不限制"});
        comboUseClazz.select(4);
        final GridData gd_comboUseClazz = new GridData(SWT.FILL, SWT.CENTER, true, false);
        comboUseClazz.setLayoutData(gd_comboUseClazz);

        final Label label_25 = new Label(this, SWT.NONE);
        label_25.setText("使用确认：");

        textUseConfirm = new Text(this, SWT.BORDER);
        final GridData gd_textUseConfirm = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textUseConfirm.setLayoutData(gd_textUseConfirm);
        textUseConfirm.addFocusListener(AutoSelectAll.instance);

        final Label label_9 = new Label(this, SWT.NONE);
        label_9.setLayoutData(new GridData());
        label_9.setText("物品等级：");

        textLevel = new Text(this, SWT.BORDER);
        textLevel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

        final Label label_4 = new Label(this, SWT.NONE);
        label_4.setText("是否绑定：");

        comboBind = new Combo(this, SWT.READ_ONLY);
        comboBind.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        comboBind.setItems(Constants.COMBO_BIND);

        final Label label_11 = new Label(this, SWT.NONE);
        label_11.setLayoutData(new GridData());
        label_11.setText("玩家等级：");

        textPlayerLevel = new Text(this, SWT.BORDER);
        textPlayerLevel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

        final Label label_10 = new Label(this, SWT.NONE);
        label_10.setText("使用范围：");

        comboArea = new Combo(this, SWT.READ_ONLY);
        comboArea.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        comboArea.setItems(COMBO_AREA);

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

        final Label label_14 = new Label(this, SWT.NONE);
        label_14.setLayoutData(new GridData());
        label_14.setText("时效类型：");

        comboTimeType = new Combo(this, SWT.READ_ONLY);
        comboTimeType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        comboTimeType.addSelectionListener(this);
        comboTimeType.setItems(Constants.COMBO_TIME_TYPE);

        final Label label_15 = new Label(this, SWT.NONE);
        label_15.setText("使用时效：");

        textTime = new Text(this, SWT.BORDER);
        textTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

        final Label label_12 = new Label(this, SWT.NONE);
        label_12.setLayoutData(new GridData());
        label_12.setText("冷却组：");

        textColdDownGroup = new Text(this, SWT.BORDER);
        final GridData gd_textColdDownGroup = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textColdDownGroup.setLayoutData(gd_textColdDownGroup);

        final Label label_17 = new Label(this, SWT.NONE);
        label_17.setText("冷却时间(毫秒)：");

        textColdDownTime = new Text(this, SWT.BORDER);
        final GridData gd_textColdDownTime = new GridData(SWT.FILL, SWT.CENTER, false, false);
        textColdDownTime.setLayoutData(gd_textColdDownTime);

        final Label label_8 = new Label(this, SWT.NONE);
        label_8.setLayoutData(new GridData());
        label_8.setText("自动使用：");

        comboAutoUse = new Combo(this, SWT.READ_ONLY);
        comboAutoUse.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        comboAutoUse.setItems(COMBO_AUTOUSE);
        comboAutoUse.addSelectionListener(this);

        final Label label_19 = new Label(this, SWT.NONE);
        label_19.setText("是否实例类型：");

        comboInstance = new Combo(this, SWT.READ_ONLY);
        final GridData gd_comboInstance = new GridData(SWT.FILL, SWT.CENTER, true, false);
        comboInstance.setLayoutData(gd_comboInstance);
        comboInstance.setItems(Constants.COMBO_YES_NO);
        comboInstance.addSelectionListener(this);

        final Label label_20 = new Label(this, SWT.NONE);
        label_20.setText("施法时间(毫秒)：");

        textSchedule = new Text(this, SWT.BORDER);
        final GridData gd_textSchedule = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textSchedule.setLayoutData(gd_textSchedule);

        final Label label_21 = new Label(this, SWT.NONE);
        label_21.setText("任务复制：");

        comboTaskMuti = new Combo(this, SWT.READ_ONLY);
        comboTaskMuti.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        comboTaskMuti.setItems(Constants.COMBO_YES_NO);
        comboTaskMuti.addSelectionListener(this);

        final Label label_22 = new Label(this, SWT.NONE);
        label_22.setText("物品图标：");
        
        iconChooser = new IconChooser(this, SWT.NONE, 0);
        iconChooser.setHandler(eventHandle);
        iconChooser.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        final Label label_23 = new Label(this, SWT.NONE);
        label_23.setText("使用距离（码）：");

        textDistance = new Text(this, SWT.BORDER);
        textDistance.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        textDistance.addModifyListener(eventHandle);

        final Label label_12_1 = new Label(this, SWT.NONE);
        label_12_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        label_12_1.setText("物品说明：");

        textDescription = new Text(this, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.WRAP);
        final GridData gd_textDescription_1 = new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1);
        gd_textDescription_1.heightHint = 71;
        textDescription.setLayoutData(gd_textDescription_1);
        // 创建右键菜单
        MenuManager mgr = new MenuManager();
        mgr.add(addAction);
        mgr.add(deleteAction);

        final Group group = new Group(this, SWT.NONE);
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 4, 1));
        final GridLayout groupGridLayout = new GridLayout();
        groupGridLayout.numColumns = 2;
        group.setLayout(groupGridLayout);

        effectList = new ListViewer(group, SWT.BORDER);
        final GridData gd_effectList = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 2);
        gd_effectList.heightHint = 304;
        effectList.getList().setLayoutData(gd_effectList);
        effectList.setContentProvider(new ListContentProvider());
        effectList.setLabelProvider(new ListLabelProvider());
        effectList.addSelectionChangedListener(this);
        Menu menu = mgr.createContextMenu(effectList.getList());
        effectList.getList().setMenu(menu);
        

        comboProperty = new Combo(group, SWT.READ_ONLY);
        comboProperty.setVisibleItemCount(20);
        comboProperty.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        comboProperty.setItems(ItemEffect.EFFECT_NAMES);
        comboProperty.addSelectionListener(this);
        
        propertyEditor = new PropertySheetViewer(group, SWT.NONE, true);
        PropertySheetEntry rootEntry = new PropertySheetEntry();
        propertyEditor.setRootEntry(rootEntry);
        propertyEditor.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        ((GridData)propertyEditor.getControl().getLayoutData()).exclude = false;
        
        
        textID.addModifyListener(eventHandle);
        textTitle.addModifyListener(eventHandle);
        textAddtion.addModifyListener(eventHandle);
        comboTaskFlag.addModifyListener(eventHandle);
        comboAvailable.addModifyListener(eventHandle);
        comboUseClazz.addModifyListener(eventHandle);
        textUseConfirm.addModifyListener(eventHandle);
        comboAutoUse.addModifyListener(eventHandle);
        comboBind.addModifyListener(eventHandle);
        textLevel.addModifyListener(eventHandle);
        comboArea.addModifyListener(eventHandle);
        textPlayerLevel.addModifyListener(eventHandle);
        textPrice.addModifyListener(eventHandle);
        comboTimeType.addModifyListener(eventHandle);
        textTime.addModifyListener(eventHandle);
        textCount.addModifyListener(eventHandle);
        comboSale.addModifyListener(eventHandle);
        textDescription.addModifyListener(eventHandle);
        textSchedule.addModifyListener(eventHandle);
        textColdDownTime.addModifyListener(eventHandle);
        textColdDownGroup.addModifyListener(eventHandle);
        comboBatchUseFlag.addModifyListener(eventHandle);
        }
    
    public void saveItemData(Item itemDataDef) throws Exception{
        itemDataDef.title = textTitle.getText();
        itemDataDef.bind = comboBind.getSelectionIndex();
        
        if(textDescription.getText() != null){
            itemDataDef.description = textDescription.getText();
        }
        else{
            itemDataDef.description = "";
        }
        
        itemDataDef.type = TYPE_MAP[comboItemType.getSelectionIndex()];
        itemDataDef.instance = comboInstance.getSelectionIndex() == 1;
        try {
            /* 当物品是一个实例类型的时候，堆叠数量只能为1 */
            int addition = Integer.parseInt(textAddtion.getText());
            if(itemDataDef.instance && addition > 1){
                throw new Exception("当物品是一个实例类型的时候，堆叠数量只能为1！");
            }
            else if(addition <= 0){
                throw new Exception("叠加数量必须大于0！");
            }
            itemDataDef.addition = addition;
        }
        catch (NumberFormatException e) {
            throw new Exception("叠加数量输入格式错误！");
        }
        itemDataDef.batchUseFlag = comboBatchUseFlag.getSelectionIndex() == Item.ATTRIBUTE_VALUE_YES;
        if (itemDataDef.batchUseFlag) {
            if (comboWaste.getSelectionIndex() <= 0) {
                throw new Exception("只有消耗品能设置为批量使用！");
            }
        }
        itemDataDef.taskFlag = comboTaskFlag.getSelectionIndex() == Item.ATTRIBUTE_VALUE_YES;
        if(itemDataDef.taskFlag){
            if(comboTaskMuti.getSelectionIndex() < 0){
                throw new Exception("请选择任务复制类型！");
            }
            itemDataDef.taskMuti = comboTaskMuti.getSelectionIndex() == Item.ATTRIBUTE_VALUE_YES;
        }
        
        itemDataDef.iconIndex = iconChooser.getIconIndex();
        if(itemDataDef.iconIndex < 0){
            throw new Exception("请选择正确的图标！");
        }
        itemDataDef.quality = comboQuality.getSelectionIndex();
        
        itemDataDef.sale = comboSale.getSelectionIndex() == Item.ATTRIBUTE_VALUE_YES;
        if(itemDataDef.sale){
            try {
                itemDataDef.price = Integer.parseInt(textPrice.getText());
            }
            catch (NumberFormatException e) {
                throw new Exception("价格输入格式错误！");
            }
        }
        
        itemDataDef.available = comboAvailable.getSelectionIndex();
        if(itemDataDef.available != Item.AVAILABLE_NO){
            itemDataDef.useClazz = comboUseClazz.getSelectionIndex();
            itemDataDef.useConfirm = textUseConfirm.getText().trim();
            itemDataDef.waste = comboWaste.getSelectionIndex() == Item.ATTRIBUTE_VALUE_YES;
            
            itemDataDef.area = comboArea.getSelectionIndex();
            try {
                itemDataDef.count = Integer.parseInt(textCount.getText());
            }
            catch (NumberFormatException e) {
                throw new Exception("使用次数格式错误！");
            }
            try {
                itemDataDef.coldDownGroup = Integer.parseInt(textColdDownGroup.getText());
            } catch (Exception e) {
                throw new Exception("冷却组ID输入错误！");
            }
            try {
                itemDataDef.coldDownTime = Integer.parseInt(textColdDownTime.getText());
            }
            catch (NumberFormatException e) {
                throw new Exception("冷却时间输入格式错误！");
            }
            
            itemDataDef.autoUse = comboAutoUse.getSelectionIndex() - 1;
            
            if(textSchedule.getEditable()){                
                try {
                    itemDataDef.schedule = Integer.parseInt(textSchedule.getText());
                }
                catch (NumberFormatException e) {
                    throw new Exception("施法时间输入格式错误！");
                }
            }
            
            try {
                itemDataDef.playerLevel = Integer.parseInt(textPlayerLevel.getText());
            }
            catch (NumberFormatException e) {
                throw new Exception("玩家等级输入格式错误！");
            }
            
            try {
                itemDataDef.level = Integer.parseInt(textLevel.getText());
            }
            catch (NumberFormatException e) {
                throw new Exception("物品等级输入格式错误！");
            }
            
            try {
                itemDataDef.distance = (int)(Float.parseFloat(textDistance.getText()) * 8);
            }
            catch (Exception e1) {
                throw new Exception("使用距离输入格式错误！");
            }
            
            itemDataDef.timeType = comboTimeType.getSelectionIndex();
            if(itemDataDef.timeType > Item.TIME_TYPE_UNDEFINE){
                try {
                    if(itemDataDef.timeType == Item.TIME_TYPE_RELATIVELY){
                        itemDataDef.time = Integer.parseInt(textTime.getText());
                    }
                    else if(itemDataDef.timeType == Item.TIME_TYPE_ABSOLUTELY){
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date date = format.parse(textTime.getText());
                        long t = date.getTime();
                        itemDataDef.time = (int)(t/1000);
                    }
                }
                catch (Exception e) {
                    throw new Exception("时间格式输入格式错误！");
                }
            }
        }

    }
    
    /**
     * 把一个物品对象的数据赋值到对应的界面控件
     * @param item
     */
    private void setItemData(){
        
        textID.setText(String.valueOf(editObject.id));
        textTitle.setText(editObject.title);
        textDescription.setText(editObject.description);
        textAddtion.setText(String.valueOf(editObject.addition));
        for (int i = 0; i < TYPE_MAP.length; i++) {
            if (editObject.type == TYPE_MAP[i]) {
                comboItemType.select(i);
            }
        }
        comboBatchUseFlag.select(editObject.batchUseFlag ? Item.ATTRIBUTE_VALUE_YES : Item.ATTRIBUTE_VALUE_NO);
        comboTaskFlag.select(editObject.taskFlag ? Item.ATTRIBUTE_VALUE_YES : Item.ATTRIBUTE_VALUE_NO);
        if(editObject.taskFlag){
            comboTaskMuti.select(editObject.taskMuti ? Item.ATTRIBUTE_VALUE_YES : Item.ATTRIBUTE_VALUE_NO);
        }
        else{
            comboTaskMuti.setEnabled(false);
        }
        comboBind.select(editObject.bind);
        comboQuality.select(editObject.quality);
        textPlayerLevel.setText(String.valueOf(editObject.playerLevel));
        comboInstance.select(editObject.instance ? Item.ATTRIBUTE_VALUE_YES : Item.ATTRIBUTE_VALUE_NO);
        iconChooser.setIcon(editObject.iconIndex);
        if(editObject.sale){
            comboSale.select(Item.ATTRIBUTE_VALUE_YES);
            textPrice.setText(String.valueOf(editObject.price));            
        }
        else{
            comboSale.select(Item.ATTRIBUTE_VALUE_NO);
            textPrice.setEditable(false);
        }
        
        comboAvailable.select(editObject.available);
        if(editObject.available == Item.AVAILABLE_NO){
            comboUseClazz.setEnabled(false);
            textUseConfirm.setEnabled(false);
            comboWaste.setEnabled(false);
            textCount.setEditable(false);
            comboArea.setEnabled(false);
            textColdDownGroup.setEnabled(false);
            textColdDownTime.setEnabled(false);
            comboAutoUse.setEnabled(false);
            textSchedule.setEditable(false);
            comboTimeType.setEnabled(false);
            textTime.setEditable(false);
            textPlayerLevel.setEditable(false);
            textLevel.setEditable(false);
            textDistance.setEditable(false);
            
            propertyEditor.getControl().setEnabled(false);
            comboProperty.setEnabled(false);
            effectList.getList().setEnabled(false);
        }
        else{
            comboUseClazz.select(editObject.useClazz);
            textUseConfirm.setText(editObject.useConfirm);
            comboWaste.select(editObject.waste ? Item.ATTRIBUTE_VALUE_YES : Item.ATTRIBUTE_VALUE_NO);
            textCount.setText(String.valueOf(editObject.schedule));
            comboArea.select(editObject.area);
            textLevel.setText(String.valueOf(editObject.level));
            textPlayerLevel.setText(String.valueOf(editObject.playerLevel));
            
            textColdDownGroup.setText(String.valueOf(editObject.coldDownGroup));
            textColdDownTime.setText(String.valueOf(editObject.coldDownTime));

            comboAutoUse.select(editObject.autoUse + 1);
            textSchedule.setEditable(true);
            textSchedule.setText(String.valueOf(editObject.schedule));
            textDistance.setText(String.valueOf(editObject.distance / 8f));
            
            comboTimeType.select(editObject.timeType);
            if(editObject.timeType == Item.TIME_TYPE_UNDEFINE){
                textTime.setEditable(false);
            }
            else if(editObject.timeType == Item.TIME_TYPE_ABSOLUTELY){
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
                StringBuffer result = new StringBuffer();
                format.format(new Date((long)editObject.time * 1000), result, new FieldPosition(0));
                textTime.setText(result.toString());
            }
            else{
                textTime.setText(String.valueOf(editObject.time));
            }
            
            effectList.setInput(editObject.effects);
            effectList.refresh();
        }
        
    }
    
    /**
     * 创建使用效果列表的弹出菜单项
     */
    private void createActions() {
        
        addAction = new Action("新建") {
            public void run() {
                onAdd();
            }
        };
        
        deleteAction = new Action("删除") {
            public void run() {
                onDelete();
            }
        };
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
        effectList.refresh();
        effectList.setSelection(new StructuredSelection(newEffect));
        eventHandle.setDirty(true);
    }
    
    /**
     * 删除当前选中效果
     */
    public void onDelete(){
        IStructuredSelection selected = (IStructuredSelection)effectList.getSelection();
        ItemEffect e = (ItemEffect)selected.getFirstElement();
        if(e != null){
            editObject.effects.remove(e);
            effectList.refresh();
            eventHandle.setDirty(true);
        }
    }
    
    /**
     * 列表选择消息处理
     * @param e
     */
    public void selectionChanged(SelectionChangedEvent event) {
        IStructuredSelection selected = (IStructuredSelection)event.getSelection();
        ItemEffect e = (ItemEffect)selected.getFirstElement();
        if(e != null){
            comboProperty.select(e.effectType);
            propertyEditor.setInput(new Object[]{new ItemEffectPropertySource(e, eventHandle)});
        }
    }
    
    /**
     * 
     */
    public void widgetDefaultSelected(SelectionEvent e) {}
    
    /**
     * 下拉列表框选择消息处理
     */
    public void widgetSelected(SelectionEvent e) {
        if(e.getSource() == comboProperty){
            IStructuredSelection selected = (IStructuredSelection)effectList.getSelection();
            ItemEffect curEffect = (ItemEffect)selected.getFirstElement();
            /*用户选择使用效果类型*/
            if(curEffect != null){                
                curEffect.effectType = comboProperty.getSelectionIndex();
                curEffect.resetParam();
                propertyEditor.setInput(new Object[]{new ItemEffectPropertySource(curEffect, eventHandle)});
                effectList.refresh();
                eventHandle.setDirty(true);
            }            
        }
        else if(e.getSource() == comboAvailable){
            if(comboAvailable.getSelectionIndex() == Item.AVAILABLE_NO){
                /* 当物品不可用时 */
                comboUseClazz.setEnabled(false);
                textUseConfirm.setEnabled(false);
                comboWaste.setEnabled(false);
                textCount.setEditable(false);
                comboArea.setEnabled(false);
                textColdDownGroup.setEnabled(false);
                textColdDownTime.setEnabled(false);
                comboAutoUse.setEnabled(false);
                textSchedule.setEditable(false);
                comboTimeType.setEnabled(false);
                textPlayerLevel.setEditable(false);
                textLevel.setEditable(false);
                textDistance.setEditable(false);
                
                effectList.getControl().setEnabled(false);
                propertyEditor.getControl().setEnabled(false);
                comboProperty.setEnabled(false);
            }
            else{
                comboUseClazz.setEnabled(true);
                textUseConfirm.setEnabled(true);
                comboWaste.setEnabled(true);
                textCount.setEditable(true);
                comboArea.setEnabled(true);
                textPlayerLevel.setEditable(true);
                textLevel.setEditable(true);
                comboTimeType.setEnabled(true);
                
                textColdDownGroup.setEnabled(true);
                textColdDownTime.setEnabled(true);
                
                comboAutoUse.setEnabled(true);
                textSchedule.setEditable(true);
                textDistance.setEditable(true);
                effectList.getControl().setEnabled(true);
                effectList.setInput(editObject.effects);
                propertyEditor.getControl().setEnabled(true);
                comboProperty.setEnabled(true);
            }
        }
        else if(e.getSource() == comboTimeType){
            
            if(comboTimeType.getSelectionIndex() > Item.ATTRIBUTE_VALUE_NO){
                /* 当没有使用时效时，时效不可编辑 */
                textTime.setEditable(true);
            }
            else{
                textTime.setEditable(false);
            }
        }
        else if(e.getSource() == comboSale){
            if(comboSale.getSelectionIndex() == Item.ATTRIBUTE_VALUE_YES){
                textPrice.setEditable(true);
            }
            else if(comboSale.getSelectionIndex() == Item.ATTRIBUTE_VALUE_NO){
                textPrice.setEditable(false);
            }
        }
        else if(e.getSource() == comboTaskFlag){
            if(comboTaskFlag.getSelectionIndex() == Item.ATTRIBUTE_VALUE_YES){
                comboTaskMuti.setEnabled(true);
            }
            else if(comboTaskFlag.getSelectionIndex() == Item.ATTRIBUTE_VALUE_NO){
                comboTaskMuti.setEnabled(false);
            }
        } else if (e.getSource() == comboItemType) {
            ((ItemEditor)eventHandle).changeType(TYPE_MAP[comboItemType.getSelectionIndex()]);
        } else {
            eventHandle.setDirty(true);
        }
    }
    
    class ListContentProvider implements IStructuredContentProvider{

        public Object[] getElements(Object inputElement) {
            List el = (List)inputElement;
            return el.toArray();
        }

        public void dispose() {}

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
    }
    
    
    class ListLabelProvider implements ILabelProvider{

        public Image getImage(Object element) {
            return null;
        }

        public String getText(Object element) {
            return element.toString();
        }

        public void addListener(ILabelProviderListener listener) {}

        public void dispose() {}

        public boolean isLabelProperty(Object element, String property) {
            return false;
        }

        public void removeListener(ILabelProviderListener listener) {}
    }
}
