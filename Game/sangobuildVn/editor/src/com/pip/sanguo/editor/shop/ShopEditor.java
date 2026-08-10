package com.pip.sanguo.editor.shop;

import java.util.HashSet;

import javax.swing.JOptionPane;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.jdom.Element;

import com.pip.sanguo.data.Animation;
import com.pip.sanguo.data.CmccConfig;
import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.Rank;
import com.pip.sanguo.data.Shop;
import com.pip.sanguo.data.Shop.ShopItem;
import com.pip.sanguo.data.item.Item;
import com.pip.sanguo.editor.DefaultDataObjectEditor;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.EditorPlugin;
import com.pip.sanguo.editor.property.ItemCellEditor;
import com.pip.util.AutoSelectAll;

public class ShopEditor extends DefaultDataObjectEditor {
    /*
     * 物品列表编辑控制。
     */
    class ItemListCellModifier implements ICellModifier {
        public boolean canModify(Object element, String property) {
            if (element instanceof Shop.ShopItem) {
                Shop.ShopItem item = (Shop.ShopItem)element;
                int index = Integer.parseInt(property.substring(1));
                if (item.item == null) {
                    return index == 0;
                } else {
                    return true;
                }
            } else {
                return false;
            }
        }
        
        public Object getValue(Object element, String property) {
            if (element instanceof Shop.ShopItem) {
                Shop.ShopItem item = (Shop.ShopItem)element;
                int index = Integer.parseInt(property.substring(1));
                switch (index) {
                case 0:    // 物品名称
                    if (item.item == null) {
                        return new Integer(0);
                    } else {
                        return new Integer(item.item.id);
                    }
                case 1:    // 限量
                    return String.valueOf(item.count);
                case 2:    // 刷新间隔
                    return String.valueOf(item.refresh / 1000);
                case 3:    // 折扣率
                    return String.valueOf(item.discount);
                case 4:    // 限制购买数量
                    return String.valueOf(item.buyLimit);
                case 5:    // 金钱价格
                    return String.valueOf(getItemPrice(item, Shop.TYPE_MONEY));
                case 6:    // i币价格
                    return String.valueOf(getItemPrice(item, Shop.TYPE_IMONEY));
                case 7:    // 荣誉价格
                    return String.valueOf(getItemPrice(item, Shop.TYPE_HONOR));
                case 8:    // 经验价格
                    return String.valueOf(getItemPrice(item, Shop.TYPE_EXP));
                case 9:    // 促销类型
                    return String.valueOf(item.promotionType);
                case 10:   // 附加条件
                {
                    return item;
                }
                default:
                    return "";
                }
            }
            return "";
        }

        public void modify(Object element, String property, Object value) {
            TableItem ti = (TableItem)element;
            if (ti.getData() instanceof Shop.ShopItem) {
                Shop.ShopItem item = (Shop.ShopItem)ti.getData();
                int index = Integer.parseInt(property.substring(1));
                switch (index) {
                case 0:    // 物品名称
                {
                    int newItemID = ((Integer)value).intValue();
                    Item newItem = ((Shop)editObject).owner.findItemOrEquipment(newItemID);
                    if (newItem == null) {
                        return;
                    }
                    if (item.item != null) {
                        // 修改物品
                        if (item.item.id == newItemID) {
                            return;
                        }
                        item.item = newItem;
                        
                        // 当更换物品的时候，重新设置推荐价格为出售价格的2倍
                        setItemPrice(item, Shop.TYPE_MONEY, newItem.price * 2);
                        itemListViewer.update(item, null);
                    } else {
                        // 添加物品
                        item = new Shop.ShopItem();
                        item.item = newItem;
                        setItemPrice(item, Shop.TYPE_MONEY, newItem.price * 2);
                        ((Shop)editObject).items.add(item);
                        itemListViewer.refresh();
                    }
                    setDirty(true);
                    break;
                }
                case 1:    // 限量
                {
                    try {
                        int newValue = Integer.parseInt((String)value);
                        if (newValue < 0 || newValue == item.count) {
                            return;
                        }
                        item.count = newValue;
                        itemListViewer.update(item, null);
                        setDirty(true);
                    } catch (Exception e) {
                        return;
                    }
                    break;
                }
                case 2:    // 刷新间隔
                {
                    try {
                        int newValue = Integer.parseInt((String)value);
                        if (newValue < 0 || newValue == item.refresh / 1000) {
                            return;
                        }
                        item.refresh = newValue * 1000;
                        itemListViewer.update(item, null);
                        setDirty(true);
                    } catch (Exception e) {
                        return;
                    }
                    break;
                }
                case 3:    // 折扣率
                {
                    try {
                        int newValue = Integer.parseInt((String)value);
                        if (newValue < 0 || newValue == item.discount) {
                            return;
                        }
                        item.discount = newValue;
                        itemListViewer.update(item, null);
                        setDirty(true);
                    } catch (Exception e) {
                        return;
                    }
                    break;
                }
                case 4:    // 限制购买数量
                {
                    try {
                        int newValue = Integer.parseInt((String)value);
                        if (newValue < 0 || newValue == item.buyLimit) {
                            return;
                        }
                        item.buyLimit = newValue;
                        itemListViewer.update(item, null);
                        setDirty(true);
                    } catch (Exception e) {
                        return;
                    }
                    break;
                }
                case 5:    // 金钱价格
                {
                    try {
                        int newValue = Integer.parseInt((String)value);
                        if (newValue < 0) {
                            return;
                        }
                        if (!setItemPrice(item, Shop.TYPE_MONEY, newValue)) {
                            return;
                        }
                        itemListViewer.update(item, null);
                        setDirty(true);
                    } catch (Exception e) {
                        return;
                    }
                    break;
                }
                case 6:    // i币价格
                {
                    try {
                        int newValue = Integer.parseInt((String)value);
                        if (newValue < 0) {
                            return;
                        }
                        if (!setItemPrice(item, Shop.TYPE_IMONEY, newValue)) {
                            return;
                        }
                        itemListViewer.update(item, null);
                        setDirty(true);
                    } catch (Exception e) {
                        return;
                    }
                    break;
                }
                case 7:    // 荣誉价格
                {
                    try {
                        int newValue = Integer.parseInt((String)value);
                        if (newValue < 0) {
                            return;
                        }
                        if (!setItemPrice(item, Shop.TYPE_HONOR, newValue)) {
                            return;
                        }
                        itemListViewer.update(item, null);
                        setDirty(true);
                    } catch (Exception e) {
                        return;
                    }
                    break;
                }
                case 8:    // 经验价格
                {
                    try {
                        int newValue = Integer.parseInt((String)value);
                        if (newValue < 0) {
                            return;
                        }
                        if (!setItemPrice(item, Shop.TYPE_EXP, newValue)) {
                            return;
                        }
                        itemListViewer.update(item, null);
                        setDirty(true);
                    } catch (Exception e) {
                        return;
                    }
                    break;
                }
                case 9:     // 促销类型
                {
                    try {
                        int newValue = Integer.parseInt((String)value);
                        if (newValue < 0) {
                            return;
                        }
                        item.promotionType = newValue;
                        itemListViewer.update(item, null);
                        setDirty(true);
                    } catch (Exception e) {
                        return;
                    }
                }
                case 10:    // 附加条件
                {
                    if (value != null) {
                        itemListViewer.update(item, null);
                        setDirty(true);
                    }
                    break;
                }
                }
            }
        }
    }
    /*
     * 商店物品列表文本提供者。
     */
    class ShopItemListLabelProvider extends LabelProvider implements ITableLabelProvider {
        public String getColumnText(Object element, int columnIndex) {
            Shop.ShopItem item = (Shop.ShopItem) element;
            if (item.item == null) {
                // 这一行是用于新增物品的，特殊处理
                if (columnIndex == 0) {
                    return "点这里添加...";
                } else {
                    return "";
                }
            }
            switch (columnIndex) {
            case 0:    // 物品名称
                return item.item.toString();
            case 1:    // 限量
                return String.valueOf(item.count);
            case 2:    // 刷新间隔
                return String.valueOf(item.refresh / 1000);
            case 3:    // 折扣率
                return String.valueOf(item.discount);
            case 4:    // 限制购买数量
                return String.valueOf(item.buyLimit);
            case 5:    // 金钱价格
                return String.valueOf(getItemPrice(item, Shop.TYPE_MONEY));
            case 6:    // i币价格
                return String.valueOf(getItemPrice(item, Shop.TYPE_IMONEY));
            case 7:    // 荣誉价格
                return String.valueOf(getItemPrice(item, Shop.TYPE_HONOR));
            case 8:    // 经验价格
                return String.valueOf(getItemPrice(item, Shop.TYPE_EXP));
            case 9:
                return String.valueOf(item.promotionType);
            case 10:    // 附加条件
            {
                return Shop.BuyRequirement.toString(item.requirements);
            }
            default:
                return "";
            }
        }

        public Image getColumnImage(Object element, int columnIndex) {
            if (columnIndex == 0) {
                return EditorPlugin.getDefault().getImageRegistry().get("empty");
            }
            return null;
        }
    }

    /*
     * 商店物品列表内容提供者。
     */
    class ShopItemListContentProvider implements IStructuredContentProvider {
        public Object[] getElements(Object inputElement) {
            Shop shop = (Shop)inputElement;
            Object[] ret = new Object[shop.items.size() + 1];
            shop.items.toArray(ret);
            Shop.ShopItem newItem = new Shop.ShopItem();
            ret[ret.length - 1] = newItem;
            return ret;
        }

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }

    private TableViewer itemListViewer;
    private Table itemList;
    private TableColumn itemColumn, countColumn, refreshColumn, discountColumn, buyLimitColumn,
            moneyPriceColumn, imoneyPriceColumn, honorPriceColumn, expPriceColumn, promotionColumn, conditionColumn;
    private Text textDesc;
    private Text textTitle;
    private Text textID;
    public static final String ID = "com.pip.sanguo.editor.shop.ShopEditor"; //$NON-NLS-1$

    /**
     * Create contents of the editor part
     * 
     * @param parent
     */
    @Override
    public void createPartControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 4;
        container.setLayout(gridLayout);

        final Label label = new Label(container, SWT.NONE);
        label.setText("ID：");

        textID = new Text(container, SWT.BORDER);
        final GridData gd_textID = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textID.setLayoutData(gd_textID);
        textID.addFocusListener(AutoSelectAll.instance);
        textID.addModifyListener(this);

        final Label label_1 = new Label(container, SWT.NONE);
        label_1.setText("标题：");

        textTitle = new Text(container, SWT.BORDER);
        final GridData gd_textTitle = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textTitle.setLayoutData(gd_textTitle);
        textTitle.addFocusListener(AutoSelectAll.instance);
        textTitle.addModifyListener(this);

        final Label label_2 = new Label(container, SWT.NONE);
        label_2.setText("描述：");

        textDesc = new Text(container, SWT.BORDER);
        final GridData gd_textDesc = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
        textDesc.setLayoutData(gd_textDesc);
        textDesc.addFocusListener(AutoSelectAll.instance);
        textDesc.addModifyListener(this);

        final Label labelTip = new Label(container, SWT.NONE);
        final GridData gd_labelTip = new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1);
        labelTip.setLayoutData(gd_labelTip);
        labelTip.setText("促销类型：0-不促销  1-热销  2-打折  3-推荐  4-新品  5-给力");

        itemListViewer = new TableViewer(container, SWT.FULL_SELECTION | SWT.BORDER);
        itemListViewer.setLabelProvider(new ShopItemListLabelProvider());
        itemListViewer.setContentProvider(new ShopItemListContentProvider());
        itemList = itemListViewer.getTable();
        itemList.setLinesVisible(true);
        itemList.setHeaderVisible(true);
        final GridData gd_itemList = new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1);
        itemList.setLayoutData(gd_itemList);
        itemList.addListener(SWT.KeyDown, new Listener() {
            public void handleEvent(Event event) {
                if ((event.stateMask & SWT.SHIFT) != 0) {
                    if (event.keyCode == SWT.ARROW_UP) {
                        onMoveUpItem();
                        event.doit = false;
                    } else if (event.keyCode == SWT.ARROW_DOWN) {
                        onMoveDownItem();
                        event.doit = false;
                    }
                }
                if (event.keyCode == SWT.DEL) {
                    onDelItem();
                }
            }
        });

        itemColumn = new TableColumn(itemList, SWT.NONE);
        itemColumn.setWidth(160);
        itemColumn.setText("物品");

        countColumn = new TableColumn(itemList, SWT.NONE);
        countColumn.setWidth(80);
        countColumn.setText("限量");

        refreshColumn = new TableColumn(itemList, SWT.NONE);
        refreshColumn.setWidth(80);
        refreshColumn.setText("刷新(秒)");

        discountColumn = new TableColumn(itemList, SWT.NONE);
        discountColumn.setWidth(80);
        discountColumn.setText("折扣率%");

        buyLimitColumn = new TableColumn(itemList, SWT.NONE);
        buyLimitColumn.setWidth(80);
        buyLimitColumn.setText("限制购买");

        moneyPriceColumn = new TableColumn(itemList, SWT.NONE);
        moneyPriceColumn.setWidth(80);
        moneyPriceColumn.setText("金钱价格");

        imoneyPriceColumn = new TableColumn(itemList, SWT.NONE);
        imoneyPriceColumn.setWidth(80);
        imoneyPriceColumn.setText("i币价格");

        honorPriceColumn = new TableColumn(itemList, SWT.NONE);
        honorPriceColumn.setWidth(80);
        honorPriceColumn.setText("荣誉价格");
        
        expPriceColumn = new TableColumn(itemList, SWT.NONE);
        expPriceColumn.setWidth(80);
        expPriceColumn.setText("经验价格");

        promotionColumn = new TableColumn(itemList, SWT.NONE);
        promotionColumn.setWidth(80);
        promotionColumn.setText("促销类型");

        conditionColumn = new TableColumn(itemList, SWT.NONE);
        conditionColumn.setWidth(300);
        conditionColumn.setText("附加条件");
        
        itemListViewer.setColumnProperties(new String[] {
                "c0", "c1", "c2", "c3", "c4", "c5", "c6", "c7", "c8", "c9", "c10"
        });
        itemListViewer.setCellModifier(new ItemListCellModifier());
        itemListViewer.setCellEditors(new CellEditor[] {
                new ItemCellEditor(itemList),
                new TextCellEditor(itemList),
                new TextCellEditor(itemList),
                new TextCellEditor(itemList),
                new TextCellEditor(itemList),
                new TextCellEditor(itemList),
                new TextCellEditor(itemList),
                new TextCellEditor(itemList),
                new TextCellEditor(itemList),
                new TextCellEditor(itemList),
                new BuyRequirementsCellEditor(itemList)
        });

        // 设置初始值
        Shop dataDef = (Shop) editObject;
        textID.setText(String.valueOf(dataDef.id));
        textTitle.setText(dataDef.title);
        textDesc.setText(dataDef.description);
        itemListViewer.setInput(dataDef);

        setDirty(false);
        setPartName(this.getEditorInput().getName());
        saveStateToUndoBuffer();
        
//        for (ShopItem item : dataDef.items) {
//            if (getItemPrice(item, Shop.TYPE_IMONEY) > 0) {
//                System.out.print(item.item.title);
//                System.out.print("\t");
//                System.out.print(getItemPrice(item, Shop.TYPE_IMONEY) * 1.0f / 36);
//                System.out.print("\t");
//                System.out.print(item.item.description);
//                System.out.print("\t");
//                System.out.println("選單->道具賣場->" + dataDef.title);
//            }
//        }
    }

    /**
     * 保存当前编辑数据。
     */
    protected void saveData() throws Exception {
        Shop dataDef = (Shop) editObject;
        
        // 读取输入：对象ID、标题、描述
        try {
            dataDef.id = Integer.parseInt(textID.getText());
        } catch (Exception e) {
            throw new Exception("请输入正确的ID。");
        }
        dataDef.title = textTitle.getText().trim();
        dataDef.description = textDesc.getText();

        // 检查输入合法性
        DataObject dobj = EditorApplication.getInstance().getProjectData().findObject(dataDef.getClass(), dataDef.id);
        if (dobj != null && dobj != getSaveTarget()) {
            throw new Exception("ID重复，请重新输入。");
        }
        if (dataDef.title.length() == 0) {
            throw new Exception("请输入标题。");
        }
        HashSet<Integer> usedItems = new HashSet<Integer>();
        for (Shop.ShopItem item : dataDef.items) {
            if (usedItems.contains(item.item.id)) {
                throw new Exception("一个商店中不允许出现重复的物品。");
            }
            usedItems.add(item.item.id);
        }
        
        // 如果有配置消费代码，检查消费代码和物品的匹配是否正确
        String errorInfo = "";
        CmccConfig cmccConfig = EditorApplication.getInstance().getProjectData().cmccConfig;
        for (Shop.ShopItem item : dataDef.items) {
            int iprice = getItemPrice(item, Shop.TYPE_IMONEY);
            String code = getItemConsumeCode(item);
            String thisError = null;
            if (iprice == 0 && code != null) {
                thisError = item.item.title + "：配置了消费代码，但没有配置i币价格。"; 
            } else if (code != null) {
                if (cmccConfig == null) {
                    thisError = item.item.title + "：cmcc_config.xml没有正确载入，无法验证消费代码。";
                } else if (cmccConfig.getPrice(code) * 36 / 10 != iprice) {
                    if (cmccConfig.getPrice(code) == 0) {
                        thisError = item.item.title + "：消费代码" + code + "无效。";
                    } else {
                        thisError = item.item.title + "：消费代码" + code + "的价格为" + (cmccConfig.getPrice(code) * 36 / 10) + "，但填写的i币价格为" + iprice + "。";
                    }
                } else if (!item.item.title.equals(cmccConfig.getItemName(code))) {
                    thisError = item.item.title + "：消费代码" + code + "的名称为" + cmccConfig.getItemName(code) + "。";
                }
            }
            if (thisError != null) {
                if (errorInfo.length() > 0) {
                    errorInfo += "\n";
                }
                errorInfo += thisError;
            }
        }
        if (errorInfo.length() > 0) {
            errorInfo = "发现消费代码错误，请确认是否继续：\n" + errorInfo;
            if (!MessageDialog.openConfirm(getSite().getShell(), "消费代码错误", errorInfo)) {
                throw new Exception("操作已取消。");
            }
        }
    }

    /*
     * 保存undo状态
     */
    protected Object saveState() {
        return editObject.save();
    }

    /*
     * 恢复保存的转台
     */
    protected void loadState(Object stateObj) {
        editObject.load((Element) stateObj);
        itemListViewer.refresh();
    }

    /**
     * 取得一个物品指定类型货币的价格。
     * @param item 物品
     * @param type 货币类型
     * @return 如果一个需求项设置为扣除，且货币类型匹配，返回其数量。
     */
    private int getItemPrice(Shop.ShopItem item, int type) {
        for (Shop.BuyRequirement req : item.requirements) {
            if (req.type == type && req.deduct) {
                return req.amount;
            }
        }
        return 0;
    }
    
    /**
     * 设置一个物品的指定类型货币的价格。
     * @param item 物品
     * @param type 货币类型
     * @param value 新价格
     * @return 如果价格和原来的相同，返回false
     */
    private boolean setItemPrice(Shop.ShopItem item, int type, int value) {
        for (Shop.BuyRequirement req : item.requirements) {
            if (req.type == type && req.deduct) {
                if (req.amount == value) {
                    return false;
                } else {
                    req.amount = value;
                    return true;
                }
            }
        }
        if (value == 0) {
            return false;
        } else {
            Shop.BuyRequirement req = new Shop.BuyRequirement();
            req.type = type;
            req.amount = value;
            req.deduct = true;
            item.requirements.add(req);
            return true;
        }
    }

    /*
     * 取得一个物品的消费代码（如果指定了的话）。
     * @param item 物品
     */
    private String getItemConsumeCode(Shop.ShopItem item) {
        for (Shop.BuyRequirement req : item.requirements) {
            if (req.type == Shop.TYPE_CONSUMECODE) {
                return req.varName;
            }
        }
        return null;
    }
    
    /*
     * 把当前选中的物品上移
     */
    private void onMoveUpItem() {
        int sel = itemList.getSelectionIndex();
        if (sel > 0) {
            Shop shop = (Shop)editObject;
            Shop.ShopItem item = shop.items.remove(sel);
            shop.items.add(sel - 1, item);
            itemListViewer.refresh();
            itemList.setSelection(sel - 1);
            setDirty(true);
        }
    }
    
    /*
     * 把当前选中的物品下移
     */
    private void onMoveDownItem() {
        int sel = itemList.getSelectionIndex();
        Shop shop = (Shop)editObject;
        if (sel != -1 && sel < shop.items.size() - 1) {
            Shop.ShopItem item = shop.items.remove(sel);
            shop.items.add(sel + 1, item);
            itemListViewer.refresh();
            itemList.setSelection(sel + 1);
            setDirty(true);
        }
    }
    
    /*
     * 删除选中的物品
     */
    private void onDelItem() {
        int sel = itemList.getSelectionIndex();
        Shop shop = (Shop)editObject;
        if (sel != -1 && sel < shop.items.size()) {
            shop.items.remove(sel);
            itemListViewer.refresh();
            itemList.setSelection(sel);
            setDirty(true);
        }
    }
}
