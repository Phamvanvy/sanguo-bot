package com.pip.sanguo.editor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.Animation;
import com.pip.sanguo.data.Faction;
import com.pip.sanguo.data.GiftGroup;
import com.pip.sanguo.data.NPCTemplate;
import com.pip.sanguo.data.NPCType;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.Shop;
import com.pip.sanguo.data.Title;
import com.pip.sanguo.data.item.DropGroup;
import com.pip.sanguo.data.item.DropNode;
import com.pip.sanguo.data.item.Item;
import com.pip.sanguo.data.quest.Quest;
import com.pip.sanguo.data.skill.BuffConfig;
import com.pip.sanguo.editor.ai.AIRuleConfig;
import com.pip.sanguo.editor.ai.EditAIRuleDialog;
import com.pip.sanguo.editor.property.ChooseDropGroupDialog;
import com.pip.sanguo.editor.property.ItemCellEditor;
import com.pip.sanguo.editor.shop.BuyRequirementsCellEditor;
import com.pip.sanguo.editor.util.AnimatePreviewer;
import com.pip.sanguo.editor.util.Constants;
import com.pip.util.AutoSelectAll;

public class GiftGroupEditor extends DefaultDataObjectEditor {
    /*
     * 物品列表编辑控制。
     */
    private Text textNeedVar;
    class ItemListCellModifier implements ICellModifier {
        public boolean canModify(Object element, String property) {
            Object[] info = (Object[])element;
            GiftGroup.GiftDef gd = (GiftGroup.GiftDef)info[0];
            int index = ((Integer)info[1]).intValue();
            int columnIndex = Integer.parseInt(property.substring(1));
            if (index >= gd.needItems.size() + gd.giveItems.size()) {
                // 最后一行
                return columnIndex == 0;
            } else {
                return true;
            }
        }
        
        public Object getValue(Object element, String property) {
            Object[] info = (Object[])element;
            GiftGroup.GiftDef gd = (GiftGroup.GiftDef)info[0];
            int index = ((Integer)info[1]).intValue();
            int columnIndex = Integer.parseInt(property.substring(1));
            if (index >= gd.needItems.size() + gd.giveItems.size()) {
                // 最后一行
                if (columnIndex == 0) {
                    return new Integer(2);
                }
            } else {
                Shop.BuyRequirement item = null;
                boolean isNeed = false;
                if (index < gd.needItems.size()) {
                    item = gd.needItems.get(index);
                    isNeed = true;
                } else if (index < gd.needItems.size() + gd.giveItems.size()) {
                    item = gd.giveItems.get(index - gd.needItems.size());
                    isNeed = false;
                }
                switch (columnIndex) {
                case 0:     // 需求/给予
                    if (isNeed) {
                        return 0;
                    } else {
                        return 1;
                    }
                case 1:     // 类型
                    switch (item.type) {
                    case Shop.TYPE_MONEY:
                        return 0;
                    case Shop.TYPE_HONOR:
                        return 1;
                    case Shop.TYPE_ITEM:
                        return 2;
                    case Shop.TYPE_VARIABLE:
                        return 3;
                    }
                    return "";
                case 2:     // 数量
                    return String.valueOf(item.amount);
                case 3:     // 物品
                    if (item.type == Shop.TYPE_ITEM) {
                        if (item.item == null) {
                            return -1;
                        } else {
                            return item.item.id;
                        }
                    } else {
                        return -1;
                    }
                case 4:     // 变量名
                    if (item.type == Shop.TYPE_VARIABLE) {
                        return item.varName;
                    } else {
                        return "";
                    }
                }
            }
            return "";
        }

        public void modify(Object element, String property, Object value) {
            TableItem ti = (TableItem)element;
            Object[] info = (Object[])ti.getData();
            GiftGroup.GiftDef gd = (GiftGroup.GiftDef)info[0];
            int index = ((Integer)info[1]).intValue();
            int columnIndex = Integer.parseInt(property.substring(1));
            if (index >= gd.needItems.size() + gd.giveItems.size()) {
                // 最后一行
                if (columnIndex == 0) {
                    int action = ((Integer)value).intValue();
                    if (action != 2) {
                        // 新增项目
                        Shop.BuyRequirement item = new Shop.BuyRequirement();
                        item.type = Shop.TYPE_ITEM;
                        item.amount = 1;
                        if (action == 0) {
                            gd.needItems.add(item);
                        } else {
                            gd.giveItems.add(item);
                        }
                        itemTableViewer.refresh();
                        setDirty(true);
                    }
                }
            } else {
                Shop.BuyRequirement item = null;
                boolean isNeed = false;
                if (index < gd.needItems.size()) {
                    item = gd.needItems.get(index);
                    isNeed = true;
                } else if (index < gd.needItems.size() + gd.giveItems.size()) {
                    item = gd.giveItems.get(index - gd.needItems.size());
                    isNeed = false;
                }
                switch (columnIndex) {
                case 0:     // 需求/给予
                    int newAction = ((Integer)value).intValue();
                    if (newAction == 2) {
                        // 删除
                        if (isNeed) {
                            gd.needItems.remove(item);
                        } else {
                            gd.giveItems.remove(item);
                        }
                        itemTableViewer.refresh();
                        setDirty(true);
                    } else if (isNeed && newAction == 1) {
                        // 从需求表转到给予表
                        gd.needItems.remove(item);
                        gd.giveItems.add(item);
                        itemTableViewer.refresh();
                        setDirty(true);
                    } else if (!isNeed && newAction == 2) {
                        // 从给予表转到需求表
                        gd.giveItems.remove(item);
                        gd.needItems.add(item);
                        itemTableViewer.refresh();
                        setDirty(true);
                    }
                    break;
                case 1:     // 类型
                    int newIndex = ((Integer)value).intValue();
                    int newType = new int[] { Shop.TYPE_MONEY, Shop.TYPE_HONOR, Shop.TYPE_ITEM, Shop.TYPE_VARIABLE }[newIndex];
                    if (newType != item.type) {
                        item.type = newType;
                        itemTableViewer.update(ti.getData(), null);
                        setDirty(true);
                    }
                    break;
                case 2:     // 数量
                    int newAmount = Integer.parseInt((String)value);
                    if (newAmount != item.amount) {
                        item.amount = newAmount;
                        itemTableViewer.update(ti.getData(), null);
                        setDirty(true);
                    }
                    break;
                case 3:     // 物品
                    int newItemID = ((Integer)value).intValue();
                    Item newItem = EditorApplication.getProj().findItemOrEquipment(newItemID);
                    if (newItem != item.item) {
                        item.item = newItem;
                        itemTableViewer.update(ti.getData(), null);
                        setDirty(true);
                    }
                    break;
                case 4:    // 变量名
                    String newVarName = (String)value;
                    if (!newVarName.equals(item.varName)) {
                        item.varName = newVarName;
                        itemTableViewer.update(ti.getData(), null);
                        setDirty(true);
                    }
                    break;
                case 5:    // 变量描述
                    String newVarDesc = (String)value;
                    if (!newVarDesc.equals(item.varDesc)) {
                        item.varDesc = newVarDesc;
                        itemTableViewer.update(ti.getData(), null);
                        setDirty(true);
                    }
                    break;
                }
            }
        }
    }

    class ItemTableLabelProvider extends LabelProvider implements ITableLabelProvider {
        public String getColumnText(Object element, int columnIndex) {
            Object[] info = (Object[])element;
            GiftGroup.GiftDef gd = (GiftGroup.GiftDef)info[0];
            int index = ((Integer)info[1]).intValue();
            Shop.BuyRequirement item = null;
            boolean isNeed = false;
            if (index < gd.needItems.size()) {
                item = gd.needItems.get(index);
                isNeed = true;
            } else if (index < gd.needItems.size() + gd.giveItems.size()) {
                item = gd.giveItems.get(index - gd.needItems.size());
                isNeed = false;
            }
            if (item == null) {
                // 最后一行
                if (columnIndex == 0) {
                    return "添加项...";
                } else {
                    return "";
                }
            }
            switch (columnIndex) {
            case 0:     // 需求/给予
                if (isNeed) {
                    return "需求";
                } else {
                    return "给予";
                }
            case 1:     // 类型
                switch (item.type) {
                case Shop.TYPE_MONEY:
                    return "金钱";
                case Shop.TYPE_HONOR:
                    return "战功";
                case Shop.TYPE_ITEM:
                    return "物品";
                case Shop.TYPE_VARIABLE:
                    return "属性变量";
                case Shop.TYPE_EXP:
                    return "经验";
                }
                return "";
            case 2:     // 数量
                return String.valueOf(item.amount);
            case 3:     // 物品
                if (item.type == Shop.TYPE_ITEM) {
                    if (item.item == null) {
                        return "";
                    } else {
                        return item.item.toString();
                    }
                } else {
                    return "";
                }
            case 4:     // 变量名
                if (item.type == Shop.TYPE_VARIABLE) {
                    return item.varName;
                } else {
                    return "";
                }
            case 5:     // 变量描述
                if (item.type == Shop.TYPE_VARIABLE) {
                    return item.varDesc;
                } else {
                    return "";
                }
            }
            return "";
        }
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }
    }
    
    class ItemTableContentProvider implements IStructuredContentProvider {
        public Object[] getElements(Object inputElement) {
            GiftGroup.GiftDef gd = (GiftGroup.GiftDef)inputElement;
            int count = gd.giveItems.size() + gd.needItems.size() + 1;
            Object[] ret = new Object[count];
            for (int i = 0; i < count; i++) {
                ret[i] = new Object[] { gd, i };
            }
            return ret;
        }
        public void dispose() {
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }
    
    class GiftListContentProvider implements IStructuredContentProvider {
        public Object[] getElements(Object inputElement) {
            GiftGroup dataDef = (GiftGroup)inputElement;
            return dataDef.gifts.toArray();
        }
        public void dispose() {
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }
    
    private Table itemTable;
    private Text textEndLevel;
    private Text textBeginLevel;
    private List giftList;
    private Text textBagFullMessage;
    private Text textGiveMessage;
    private Text textNeedMessage;
    private Text textTimeMessage;
    private Text textTimeSpaceMessage;
    private Text textRepeatMessage;
    private Text textMaxMessage;
    private Text textGiftDesc;
    private Text textGroupTitle;
    private Text textErrorMessage;
    private Text textTimeSpace;
    private Text textRepeatTimes;
    private Combo comboCycleType;
    private Text textEndDate;
    private Text textBeginDate;
    private Text textBeginDay;
    private Text textEndDay;
    private Text textBeginTime;
    private Text textEndTime;
    private Text textCycleAmount;
    private Text textMaxTimes;
    private Text textTitle;
    private Text textID;
    
    public static final String ID = "com.pip.sanguo.editor.GiftGroupEditor"; //$NON-NLS-1$
    private Button buttonValid;
    private ListViewer giftListViewer;
    private TableViewer itemTableViewer;
    
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    /**
     * Create contents of the editor part
     * @param parent
     */
    @Override
    public void createPartControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 9;
        container.setLayout(gridLayout);

        final Label label = new Label(container, SWT.NONE);
        label.setText("ID：");

        textID = new Text(container, SWT.BORDER);
        final GridData gd_textID = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textID.setLayoutData(gd_textID);
        textID.addFocusListener(AutoSelectAll.instance);
        textID.addModifyListener(this);

        final Label label_1 = new Label(container, SWT.NONE);
        label_1.setLayoutData(new GridData());
        label_1.setText("名称：");

        textTitle = new Text(container, SWT.BORDER);
        final GridData gd_textTitle = new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1);
        textTitle.setLayoutData(gd_textTitle);
        textTitle.addFocusListener(AutoSelectAll.instance);
        textTitle.addModifyListener(this);

        buttonValid = new Button(container, SWT.CHECK);
        buttonValid.addSelectionListener(new SelectionAdapter() {
            public void widgetDefaultSelected(final SelectionEvent e) {
            }
            public void widgetSelected(final SelectionEvent e) {
                setDirty(true);
            }
        });
        final GridData gd_buttonValid = new GridData();
        buttonValid.setLayoutData(gd_buttonValid);
        buttonValid.setText("是否有效");

        final Label label_3 = new Label(container, SWT.NONE);
        label_3.setLayoutData(new GridData());
        label_3.setText("开始日期：");

        textBeginDate = new Text(container, SWT.BORDER);
        final GridData gd_textBeginDate = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textBeginDate.setLayoutData(gd_textBeginDate);
        textBeginDate.addFocusListener(AutoSelectAll.instance);
        textBeginDate.addModifyListener(this);

        final Label label_7 = new Label(container, SWT.NONE);
        label_7.setLayoutData(new GridData());
        label_7.setText("结束日期：");

        textEndDate = new Text(container, SWT.BORDER);
        final GridData gd_textEndDate = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        textEndDate.setLayoutData(gd_textEndDate);
        textEndDate.addFocusListener(AutoSelectAll.instance);
        textEndDate.addModifyListener(this);

        final Label label_2 = new Label(container, SWT.NONE);
        label_2.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1));
        label_2.setText("注：日期格式YYYYMMDD，空表示不限制");

        final Label label_13 = new Label(container, SWT.NONE);
        label_13.setText("累计领取次数上限：");

        textMaxTimes = new Text(container, SWT.BORDER);
        final GridData gd_textMaxTimes = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textMaxTimes.setLayoutData(gd_textMaxTimes);
        textMaxTimes.addFocusListener(AutoSelectAll.instance);
        textMaxTimes.addModifyListener(this);

        final Label label_14 = new Label(container, SWT.NONE);
        label_14.setText("领取周期：");

        textCycleAmount = new Text(container, SWT.BORDER);
        final GridData gd_textCycleAmount = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textCycleAmount.setLayoutData(gd_textCycleAmount);
        textCycleAmount.addFocusListener(AutoSelectAll.instance);
        textCycleAmount.addModifyListener(this);

        comboCycleType = new Combo(container, SWT.READ_ONLY);
        comboCycleType.setItems(new String[] {"小时", "天", "周", "月"});
        final GridData gd_comboCycleType = new GridData(SWT.FILL, SWT.CENTER, true, false);
        comboCycleType.setLayoutData(gd_comboCycleType);
        comboCycleType.addModifyListener(this);

        final Label label_5 = new Label(container, SWT.NONE);
        label_5.setText("期内领取次数上限：");

        textRepeatTimes = new Text(container, SWT.BORDER);
        final GridData gd_textRepeatTimes = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textRepeatTimes.setLayoutData(gd_textRepeatTimes);
        textRepeatTimes.addFocusListener(AutoSelectAll.instance);
        textRepeatTimes.addModifyListener(this);

        final Label label_4 = new Label(container, SWT.NONE);
        label_4.setText("领取间隔(秒)：");

        textTimeSpace = new Text(container, SWT.BORDER);
        final GridData gd_textTimeSpace = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textTimeSpace.setLayoutData(gd_textTimeSpace);
        textTimeSpace.addFocusListener(AutoSelectAll.instance);
        textTimeSpace.addModifyListener(this);

        final Label label_6 = new Label(container, SWT.NONE);
        label_6.setText("期内起始日：");

        textBeginDay = new Text(container, SWT.BORDER);
        final GridData gd_textBeginDay = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textBeginDay.setLayoutData(gd_textBeginDay);
        textBeginDay.addFocusListener(AutoSelectAll.instance);
        textBeginDay.addModifyListener(this);

        final Label label_71 = new Label(container, SWT.NONE);
        label_71.setText("期内结束日(含)：");

        textEndDay = new Text(container, SWT.BORDER);
        textEndDay.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        textEndDay.addFocusListener(AutoSelectAll.instance);
        textEndDay.addModifyListener(this);

        final Label label_72 = new Label(container, SWT.NONE);
        label_72.setText("开始时间：");

        textBeginTime = new Text(container, SWT.BORDER);
        textBeginTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        textBeginTime.addFocusListener(AutoSelectAll.instance);
        textBeginTime.addModifyListener(this);

        final Label label_73 = new Label(container, SWT.NONE);
        label_73.setText("结束时间：");

        textEndTime = new Text(container, SWT.BORDER);
        textEndTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        textEndTime.addFocusListener(AutoSelectAll.instance);
        textEndTime.addModifyListener(this);

        final Label label_11 = new Label(container, SWT.NONE);
        label_11.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 9, 1));
        label_11.setText("领取次数上限、领取间隔、期内起始日、期内结束日可用-1表示不限制。开始时间和结束时间的格式为HHMM，空表示不限制。");

        final Label label_15 = new Label(container, SWT.NONE);
        label_15.setText("奖品组标题：");

        textGroupTitle = new Text(container, SWT.BORDER);
        final GridData gd_textGroupTitle = new GridData(SWT.FILL, SWT.CENTER, true, false, 8, 1);
        textGroupTitle.setLayoutData(gd_textGroupTitle);
        textGroupTitle.addFocusListener(AutoSelectAll.instance);
        textGroupTitle.addModifyListener(this);

        final Label label_16 = new Label(container, SWT.NONE);
        label_16.setText("奖品描述：");

        textGiftDesc = new Text(container, SWT.BORDER);
        final GridData gd_textGiftDesc = new GridData(SWT.FILL, SWT.CENTER, true, false, 8, 1);
        textGiftDesc.setLayoutData(gd_textGiftDesc);
        textGiftDesc.addFocusListener(AutoSelectAll.instance);
        textGiftDesc.addModifyListener(this);

        final Label label_12 = new Label(container, SWT.NONE);
        label_12.setText("出错消息：");

        textErrorMessage = new Text(container, SWT.BORDER);
        final GridData gd_textErrorMessage = new GridData(SWT.FILL, SWT.CENTER, true, false, 8, 1);
        textErrorMessage.setLayoutData(gd_textErrorMessage);
        textErrorMessage.addFocusListener(AutoSelectAll.instance);
        textErrorMessage.addModifyListener(this);

        final Label label_17 = new Label(container, SWT.NONE);
        label_17.setText("达到总次数上限：");

        textMaxMessage = new Text(container, SWT.BORDER);
        final GridData gd_textMaxMessage = new GridData(SWT.FILL, SWT.CENTER, true, false, 8, 1);
        textMaxMessage.setLayoutData(gd_textMaxMessage);
        textMaxMessage.addFocusListener(AutoSelectAll.instance);
        textMaxMessage.addModifyListener(this);

        final Label label_18 = new Label(container, SWT.NONE);
        label_18.setText("达到期内次数上限：");

        textRepeatMessage = new Text(container, SWT.BORDER);
        final GridData gd_textRepeatMessage = new GridData(SWT.FILL, SWT.CENTER, true, false, 8, 1);
        textRepeatMessage.setLayoutData(gd_textRepeatMessage);
        textRepeatMessage.addFocusListener(AutoSelectAll.instance);
        textRepeatMessage.addModifyListener(this);

        final Label label_181 = new Label(container, SWT.NONE);
        label_181.setText("没到最小间隔时间：");

        textTimeSpaceMessage = new Text(container, SWT.BORDER);
        final GridData gd_textTimeSpaceMessage = new GridData(SWT.FILL, SWT.CENTER, true, false, 8, 1);
        textTimeSpaceMessage.setLayoutData(gd_textTimeSpaceMessage);
        textTimeSpaceMessage.addFocusListener(AutoSelectAll.instance);
        textTimeSpaceMessage.addModifyListener(this);

        final Label label_19 = new Label(container, SWT.NONE);
        label_19.setText("没到领取时间：");

        textTimeMessage = new Text(container, SWT.BORDER);
        final GridData gd_textTimeMessage = new GridData(SWT.FILL, SWT.CENTER, true, false, 8, 1);
        textTimeMessage.setLayoutData(gd_textTimeMessage);
        textTimeMessage.addFocusListener(AutoSelectAll.instance);
        textTimeMessage.addModifyListener(this);

        final Label label_20 = new Label(container, SWT.NONE);
        label_20.setText("需求物品不足：");

        textNeedMessage = new Text(container, SWT.BORDER);
        final GridData gd_textNeedMessage = new GridData(SWT.FILL, SWT.CENTER, true, false, 8, 1);
        textNeedMessage.setLayoutData(gd_textNeedMessage);
        textNeedMessage.addFocusListener(AutoSelectAll.instance);
        textNeedMessage.addModifyListener(this);

        final Label label_8 = new Label(container, SWT.NONE);
        label_8.setText("需求变量不满足：");

        textNeedVar = new Text(container, SWT.BORDER);
        final GridData gd_textNeedVar = new GridData(SWT.FILL, SWT.CENTER, true, false, 8, 1);
        textNeedVar.setLayoutData(gd_textNeedVar);
        textNeedVar.addFocusListener(AutoSelectAll.instance);
        textNeedVar.addModifyListener(this);

        final Label label_21 = new Label(container, SWT.NONE);
        label_21.setText("发奖成功：");

        textGiveMessage = new Text(container, SWT.BORDER);
        final GridData gd_textGiveMessage = new GridData(SWT.FILL, SWT.CENTER, true, false, 8, 1);
        textGiveMessage.setLayoutData(gd_textGiveMessage);
        textGiveMessage.addFocusListener(AutoSelectAll.instance);
        textGiveMessage.addModifyListener(this);

        final Label label_22 = new Label(container, SWT.NONE);
        label_22.setText("背包满：");

        textBagFullMessage = new Text(container, SWT.BORDER);
        final GridData gd_textBagFullMessage = new GridData(SWT.FILL, SWT.CENTER, true, false, 8, 1);
        textBagFullMessage.setLayoutData(gd_textBagFullMessage);
        textBagFullMessage.addFocusListener(AutoSelectAll.instance);
        textBagFullMessage.addModifyListener(this);

        final Label label_23 = new Label(container, SWT.WRAP);
        label_23.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 9, 1));
        label_23.setText("上面的描述文字可带变量，变量的格式为${变量名}。可用的变量包括：\nbeginlevel最小级别，endlevel最大级别，needitem需求物品，giveitem兑换物品，acount已领取次数，\nrcount本期已领取次数，max最大领取次数，repeat本期最大领取次数，needtime领取时间范围，timespace最小领取间隔。");

        giftListViewer = new ListViewer(container, SWT.BORDER);
        giftListViewer.setContentProvider(new GiftListContentProvider());
        giftListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(final SelectionChangedEvent event) {
                StructuredSelection sel = (StructuredSelection)giftListViewer.getSelection();
                if (sel.isEmpty()) {
                    itemTable.setVisible(false);
                    return;
                }
                GiftGroup.GiftDef gd = (GiftGroup.GiftDef)sel.getFirstElement();
                textBeginLevel.setText(String.valueOf(gd.beginLevel));
                textEndLevel.setText(String.valueOf(gd.endLevel));
                itemTable.setVisible(true);
                itemTableViewer.setInput(gd);
            }
        });
        giftList = giftListViewer.getList();
        final GridData gd_giftList = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
        giftList.setLayoutData(gd_giftList);

        itemTableViewer = new TableViewer(container, SWT.FULL_SELECTION | SWT.BORDER);
        itemTableViewer.setLabelProvider(new ItemTableLabelProvider());
        itemTableViewer.setContentProvider(new ItemTableContentProvider());
        itemTable = itemTableViewer.getTable();
        itemTable.setVisible(false);
        itemTable.setLinesVisible(true);
        itemTable.setHeaderVisible(true);
        final GridData gd_itemTable = new GridData(SWT.FILL, SWT.FILL, true, true, 6, 2);
        itemTable.setLayoutData(gd_itemTable);

        final TableColumn giveColumn = new TableColumn(itemTable, SWT.NONE);
        giveColumn.setWidth(100);
        giveColumn.setText("需求/给予");

        final TableColumn typeColumn = new TableColumn(itemTable, SWT.NONE);
        typeColumn.setWidth(100);
        typeColumn.setText("类型");

        final TableColumn amountColumn = new TableColumn(itemTable, SWT.NONE);
        amountColumn.setWidth(100);
        amountColumn.setText("数量");

        final TableColumn itemColumn = new TableColumn(itemTable, SWT.NONE);
        itemColumn.setWidth(100);
        itemColumn.setText("物品");
        
        final TableColumn varNameColumn = new TableColumn(itemTable, SWT.NONE);
        varNameColumn.setWidth(100);
        varNameColumn.setText("变量");

        final TableColumn varDescColumn = new TableColumn(itemTable, SWT.NONE);
        varDescColumn.setWidth(150);
        varDescColumn.setText("条件描述");
        
        itemTableViewer.setColumnProperties(new String[] {
                "c0", "c1", "c2", "c3", "c4", "c5"
        });
        itemTableViewer.setCellModifier(new ItemListCellModifier());
        itemTableViewer.setCellEditors(new CellEditor[] {
                new ComboBoxCellEditor(itemTable, new String[] {
                    "需求", "给予", "删除"
                }) {
                    public int getStyle() {
                        return SWT.READ_ONLY;
                    }
                },
                new ComboBoxCellEditor(itemTable, new String[] {
                    "金钱", "战功", "物品", "属性变量"
                }) {
                    public int getStyle() {
                        return SWT.READ_ONLY;
                    }
                },
                new TextCellEditor(itemTable),
                new ItemCellEditor(itemTable),
                new TextCellEditor(itemTable),
                new TextCellEditor(itemTable)
        });

        final Composite composite = new Composite(container, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
        final GridLayout gridLayout_1 = new GridLayout();
        gridLayout_1.numColumns = 7;
        composite.setLayout(gridLayout_1);

        textBeginLevel = new Text(composite, SWT.BORDER);
        final GridData gd_textBeginLevel = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textBeginLevel.setLayoutData(gd_textBeginLevel);
        textBeginLevel.addFocusListener(AutoSelectAll.instance);

        final Label label_24 = new Label(composite, SWT.NONE);
        label_24.setText("-");

        textEndLevel = new Text(composite, SWT.BORDER);
        final GridData gd_textEndLevel = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textEndLevel.setLayoutData(gd_textEndLevel);
        textEndLevel.addFocusListener(AutoSelectAll.instance);

        final Label label_25 = new Label(composite, SWT.NONE);
        label_25.setText("级");

        final Button buttonAdd = new Button(composite, SWT.NONE);
        buttonAdd.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                int beginLevel = 0, endLevel = 0;
                try {
                    beginLevel = Integer.parseInt(textBeginLevel.getText());
                    endLevel = Integer.parseInt(textEndLevel.getText());
                    if (beginLevel <= 0 || beginLevel > 200 || endLevel <= 0 || endLevel > 200) {
                        throw new Exception();
                    }
                } catch (Exception e1) {
                    MessageDialog.openError(getSite().getShell(), "错误", "级别输入错误。");
                    return;
                }
                GiftGroup dataDef = (GiftGroup)editObject;
                GiftGroup.GiftDef gd = new GiftGroup.GiftDef();
                while (true) {
                    gd.id++;
                    boolean found = false;
                    for (GiftGroup.GiftDef gd1 : dataDef.gifts) {
                        if (gd1.id == gd.id) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        break;
                    }
                }
                gd.beginLevel = beginLevel;
                gd.endLevel = endLevel;
                dataDef.gifts.add(gd);
                setDirty(true);
                giftListViewer.refresh();
            }
        });
        buttonAdd.setText("添加");

        final Button buttonUpdate = new Button(composite, SWT.NONE);
        buttonUpdate.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                int beginLevel = 0, endLevel = 0;
                try {
                    beginLevel = Integer.parseInt(textBeginLevel.getText());
                    endLevel = Integer.parseInt(textEndLevel.getText());
                    if (beginLevel <= 0 || beginLevel > 200 || endLevel <= 0 || endLevel > 200) {
                        throw new Exception();
                    }
                } catch (Exception e1) {
                    MessageDialog.openError(getSite().getShell(), "错误", "级别输入错误。");
                    return;
                }
                StructuredSelection sel = (StructuredSelection)giftListViewer.getSelection();
                if (sel.isEmpty()) {
                    return;
                }
                GiftGroup.GiftDef gd1 = (GiftGroup.GiftDef)sel.getFirstElement();
                gd1.beginLevel = beginLevel;
                gd1.endLevel = endLevel;
                setDirty(true);
                giftListViewer.refresh();
            }
        });
        buttonUpdate.setText("更新");

        final Button buttonDelete = new Button(composite, SWT.NONE);
        buttonDelete.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                GiftGroup dataDef = (GiftGroup)editObject;
                StructuredSelection sel = (StructuredSelection)giftListViewer.getSelection();
                if (sel.isEmpty()) {
                    return;
                }
                GiftGroup.GiftDef gd1 = (GiftGroup.GiftDef)sel.getFirstElement();
                dataDef.gifts.remove(gd1);
                setDirty(true);
                giftListViewer.refresh();
            }
        });
        buttonDelete.setText("删除");
        
        // 设置初始值
        updateView();
        
        setDirty(false);
        setPartName(this.getEditorInput().getName());
        saveStateToUndoBuffer();
    }
    
    private void updateView() {
        // 设置初始值
        GiftGroup dataDef = (GiftGroup)editObject;
        textID.setText(String.valueOf(dataDef.id));
        textTitle.setText(dataDef.title);
        buttonValid.setSelection(dataDef.valid);
        if (dataDef.beginDate == null) {
            textBeginDate.setText("");
        } else {
            textBeginDate.setText(dateFormat.format(dataDef.beginDate));
        }
        if (dataDef.endDate == null) {
            textEndDate.setText("");
        } else {
            textEndDate.setText(dateFormat.format(dataDef.endDate));
        }
        textMaxTimes.setText(String.valueOf(dataDef.maxTimes));
        textCycleAmount.setText(String.valueOf(dataDef.cycle.amount));
        comboCycleType.select(dataDef.cycle.type);
        textRepeatTimes.setText(String.valueOf(dataDef.repeatTimes));
        textTimeSpace.setText(String.valueOf(dataDef.timeSpace));
        textBeginDay.setText(String.valueOf(dataDef.beginDay));
        textEndDay.setText(String.valueOf(dataDef.endDay));
        textBeginDay.setEnabled(dataDef.supportDaySetting());
        textEndDay.setEnabled(dataDef.supportDaySetting());
        textBeginTime.setText(minuteToStr(dataDef.beginTime));
        textEndTime.setText(minuteToStr(dataDef.endTime));
        
        textGroupTitle.setText(dataDef.groupMessage);
        textGiftDesc.setText(dataDef.giftMessage);
        textErrorMessage.setText(dataDef.errorMessage);
        textMaxMessage.setText(dataDef.maxExceedMessage);
        textRepeatMessage.setText(dataDef.repeatExceedMessage);
        textTimeSpaceMessage.setText(dataDef.timeSpaceMessage);
        textTimeMessage.setText(dataDef.timeErrorMessage);
        textNeedMessage.setText(dataDef.needItemMessage);
        textNeedVar.setText(dataDef.needVarMessage);
        textGiveMessage.setText(dataDef.giveOKMessage);
        textBagFullMessage.setText(dataDef.bagFullMessage);
        
        giftListViewer.setInput(dataDef);
    }
    
    /**
     * 保存当前编辑数据。
     */
    protected void saveData() throws Exception {
        GiftGroup dataDef = (GiftGroup)editObject;

        // 读取输入
        try {
            dataDef.id = Integer.parseInt(textID.getText());
        } catch (Exception e) {
            throw new Exception("请输入正确的ID。");
        }
        dataDef.title = textTitle.getText().trim();
        dataDef.valid = buttonValid.getSelection();
        try {
            dataDef.beginDate = strToDate(textBeginDate.getText());
            if (dataDef.beginDate == null) {
                throw new Exception();
            }
        } catch (Exception e) {
            throw new Exception("请输入正确的起始日期（不能为空）。");
        }
        try {
            dataDef.endDate = strToDate(textEndDate.getText());
        } catch (Exception e) {
            throw new Exception("请输入正确的结束日期。");
        }
        try {
            dataDef.maxTimes = Integer.parseInt(textMaxTimes.getText());
        } catch (Exception e) {
            throw new Exception("请输入正确的最大领取次数。");
        }
        try {
            dataDef.cycle.type = comboCycleType.getSelectionIndex();
            dataDef.cycle.amount = Integer.parseInt(textCycleAmount.getText());
            if (dataDef.cycle.amount <= 0) {
                throw new Exception();
            }
        } catch (Exception e) {
            throw new Exception("请输入正确的周期。");
        }
        try {
            dataDef.repeatTimes = Integer.parseInt(textRepeatTimes.getText());
        } catch (Exception e) {
            throw new Exception("请输入正确的期内领取次数。");
        }
        try {
            dataDef.timeSpace = Integer.parseInt(textTimeSpace.getText());
        } catch (Exception e) {
            throw new Exception("请输入正确的期内领取间隔。");
        }
        
        if (dataDef.supportDaySetting()) {
            try {
                dataDef.beginDay = Integer.parseInt(textBeginDay.getText());
                dataDef.endDay = Integer.parseInt(textEndDay.getText());
            } catch (Exception e) {
                throw new Exception("请输入正确的日期限制。");
            }
        } else {
            dataDef.beginDay = -1;
            dataDef.endDay = -1;
        }
        try {
            dataDef.beginTime = strToMinute(textBeginTime.getText());
            dataDef.endTime = strToMinute(textEndTime.getText());
        } catch (Exception e) {
            throw new Exception("请输入正确的时间限制。");
        }
        
        dataDef.groupMessage = textGroupTitle.getText();
        dataDef.giftMessage = textGiftDesc.getText();
        dataDef.errorMessage = textErrorMessage.getText();
        dataDef.maxExceedMessage = textMaxMessage.getText();
        dataDef.repeatExceedMessage = textRepeatMessage.getText();
        dataDef.timeSpaceMessage = textTimeSpaceMessage.getText();
        dataDef.timeErrorMessage = textTimeMessage.getText();
        dataDef.needItemMessage = textNeedMessage.getText();
        dataDef.needVarMessage = textNeedVar.getText();
        dataDef.giveOKMessage = textGiveMessage.getText();
        dataDef.bagFullMessage = textBagFullMessage.getText();
        
        // 检查输入合法性
        DataObject dobj = EditorApplication.getInstance().getProjectData().findObject(dataDef.getClass(), dataDef.id);
        if (dobj != null && dobj != getSaveTarget()) {
            throw new Exception("ID重复，请重新输入。");
        }
        if (dataDef.title.length() == 0) {
            throw new Exception("请输入标题。");
        }
    }
    
    public void modifyText(final ModifyEvent e) {
        super.modifyText(e);
        if (e.getSource() == textCycleAmount || e.getSource() == comboCycleType) {
            int t = comboCycleType.getSelectionIndex();
            int a = 0;
            try {
                a = Integer.parseInt(textCycleAmount.getText());
            } catch (Exception e1) {
            }
            textBeginDay.setEnabled(GiftGroup.supportDaySetting(t, a));
            textEndDay.setEnabled(GiftGroup.supportDaySetting(t, a));
        }
    }
    
    private String minuteToStr(int min) {
        if (min == -1) {
            return "";
        }
        int h = min / 60;
        int m = min % 60;
        return h + ":" + m;
    }
    
    private int strToMinute(String str) throws Exception {
        if (str.length() == 0) {
            return -1;
        }
        String[] sec = str.split(":");
        if (sec.length != 2) {
            throw new Exception();
        }
        int h = Integer.parseInt(sec[0]);
        int m = Integer.parseInt(sec[1]);
        if (h < 0 || h > 24) {
            throw new Exception();
        }
        if (m < 0 || m > 59) {
            throw new Exception();
        }
        if (h == 24 && m != 0) {
            throw new Exception();
        }
        return h * 60 + m;
    }
    
    private Date strToDate(String str) throws Exception {
        if (str.length() == 0) {
            return null;
        }
        return dateFormat.parse(str);
    }
}
