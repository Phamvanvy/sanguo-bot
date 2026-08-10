package com.pip.sanguo.editor.equipment;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.equipment.AttributeCalculator;
import com.pip.sanguo.data.equipment.Equipment;
import com.pip.sanguo.data.equipment.EquipmentAttribute;
import com.pip.sanguo.data.equipment.SuiteConfig_New;
import com.pip.sanguo.data.item.Item;
import com.pip.sanguo.data.skill.BuffConfig;
import com.pip.sanguo.editor.DataListView;
import com.pip.sanguo.editor.DefaultDataObjectEditor;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.item.ItemTreeViewer;
import com.pip.sanguo.editor.property.BuffCellEditor;
import com.pip.sanguo.editor.property.ChooseItemDialog;
import com.pip.sanguo.editor.quest.RichTextPreviewer;
import com.pip.sanguo.editor.skill.DescriptionPattern;
import com.pip.sanguo.editor.util.ItemChooser;
import com.pip.util.AutoSelectAll;

public class SuiteEditor_New extends DefaultDataObjectEditor {

    class EffectTableCellModifier implements ICellModifier {
        public boolean canModify(Object element, String property) {
            int columnIndex = Integer.parseInt(property.substring(1));
            if (element instanceof SuiteConfig_New.SuiteEffect_New) {
                return true;
            } else {
                return columnIndex == 0;
            }
        }
        public Object getValue(Object element, String property) {
            int columnIndex = Integer.parseInt(property.substring(1));
            if (element instanceof SuiteConfig_New.SuiteEffect_New) {
                SuiteConfig_New.SuiteEffect_New eff = (SuiteConfig_New.SuiteEffect_New)element;
                switch (columnIndex) {
                case 0:
                    return eff.equipName;
                case 1:
                    return new Integer(eff.buffID);
                case 2:
                    return String.valueOf(eff.equipWeight);
                case 3:
                    return String.valueOf(eff.weight);
                case 4:
                    return new Integer(eff.buff2ID); 
                }
                return "";
            } else {
                return "";
            }
        }
        public void modify(Object element, String property, Object value) {
            TableItem ti = (TableItem)element;
            int columnIndex = Integer.parseInt(property.substring(1));
            if (ti.getData() instanceof SuiteConfig_New.SuiteEffect_New) {
                SuiteConfig_New.SuiteEffect_New eff = (SuiteConfig_New.SuiteEffect_New)ti.getData();
                switch (columnIndex) {
                case 0:
                    if (value == null||value.equals("")) {
                        ((SuiteConfig_New)editObject).effects.remove(eff);
                        effectTableViewer.refresh();
                        setDirty(true);
                        updatePreview();
                    } else{
                        effectTableViewer.update(ti.getData(), null);
                        setDirty(true);
                        updatePreview();
                    }
                    break;
                case 1:
                    int newBuffID = ((Integer)value).intValue();
                    if (newBuffID != eff.buffID) {
                        eff.buffID = newBuffID;
                        effectTableViewer.update(ti.getData(), null);
                        setDirty(true);
                        updatePreview();
                    }
                    break;
                case 2:
                    int newLevel = Integer.parseInt((String)value);
                    if (newLevel != eff.equipWeight) {
                        eff.equipWeight = newLevel;
                        effectTableViewer.update(ti.getData(), null);
                        setDirty(true);
                        updatePreview();
                    }
                    break;
                case 3:
                    String value1=(String)value;
                    if(value1.startsWith(">")){
                       value1= value1.substring(1);
                    }
                    int newWeight = Integer.parseInt(value1);
                    if (newWeight != eff.weight) {
                        eff.weight = newWeight;
                        effectTableViewer.update(ti.getData(), null);
                        setDirty(true);
                        updatePreview();
                    }
                    break;
                case 4:
                    int newBuff2ID = ((Integer)value).intValue();
                    if (newBuff2ID != eff.buff2ID) {
                        eff.buff2ID = newBuff2ID;
                        effectTableViewer.update(ti.getData(), null);
                        setDirty(true);
                        updatePreview();
                    }
                    break;
                }
            } else {
                try {
                    int count = Integer.parseInt((String)value);
                    if (count > 0) {
                        SuiteConfig_New.SuiteEffect_New eff = new SuiteConfig_New.SuiteEffect_New();
                        eff.buffID = -1;
                        eff.equipWeight=0;
                        eff.weight=0;
                        ((SuiteConfig_New)editObject).effects.add(eff);
                        effectTableViewer.refresh();
                        setDirty(true);
                        updatePreview();
                    }
                } catch (Exception e) {
                }
            }
        }
    }
    
    class EffectTableLabelProvider extends LabelProvider implements ITableLabelProvider {
        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof SuiteConfig_New.SuiteEffect_New) {
                SuiteConfig_New.SuiteEffect_New eff = (SuiteConfig_New.SuiteEffect_New)element;
                if (columnIndex == 0) {
                    return eff.equipName;
                } else if (columnIndex == 1) {
                    return BuffConfig.toString(EditorApplication.getProj(), eff.buffID);
                } else if(columnIndex==4){
                    return BuffConfig.toString(EditorApplication.getProj(), eff.buff2ID);
                } else if(columnIndex==3){
                    return String.valueOf(eff.weight);
                } else {
                    return String.valueOf(eff.equipWeight);
                }
            } else {
                if (columnIndex == 0) {
                    return "新建...";
                } else {
                    return "";
                }
            }
        }
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }
    }
    
    class EffectTableContentProvider implements IStructuredContentProvider {
        public Object[] getElements(Object inputElement) {
            SuiteConfig_New suite = (SuiteConfig_New)inputElement;
            Object[] ret = new Object[suite.effects.size() + 1];
            suite.effects.toArray(ret);
            ret[ret.length - 1] = "";
            return ret;
        }
        public void dispose() {
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }
    
    class EquipmentTreeLabelProvider extends LabelProvider {
        public String getText(Object element) {
            if (element instanceof Equipment) {
                return ((Equipment)element).id + " " + ((Equipment)element).title;
            } else {
                return element.toString();
            }
        }
        public Image getImage(Object element) {
            return null;
        }
    }
    
    class EquipmentTreeContentProvider implements IStructuredContentProvider, ITreeContentProvider {
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
        public void dispose() {
        }
        public Object[] getElements(Object inputElement) {
            SuiteConfig_New suite = (SuiteConfig_New)inputElement;
            Object[] ret = new Object[suite.equipments.size() + 1];
            suite.equipments.toArray(ret);
            ret[ret.length - 1] = "添加装备...";
            return ret;
        }
        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof SuiteConfig_New) {
                return getElements(parentElement);
            } else {
                return new Object[0];
            }
        }
        public Object getParent(Object element) {
            return null;
        }
        public boolean hasChildren(Object element) {
            return getChildren(element).length > 0;
        }
    }
    
    private Table effectTable;
    private Tree equipmentTree;
    private Text textDescription;
    private Text textTitle;
    private Text textID;
    
    public static final String ID = "com.pip.sanguo.editor.equipment.SuiteEditor_New"; //$NON-NLS-1$
    private TreeViewer equipmentTreeViewer;
    private TableViewer effectTableViewer;
    private RichTextPreviewer previewer;

    /**
     * Create contents of the editor part
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
        label_1.setLayoutData(new GridData());
        label_1.setText("名称：");

        textTitle = new Text(container, SWT.BORDER);
        final GridData gd_textTitle = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textTitle.setLayoutData(gd_textTitle);
        textTitle.addFocusListener(AutoSelectAll.instance);
        textTitle.addModifyListener(this);

        final Label label_2 = new Label(container, SWT.NONE);
        label_2.setLayoutData(new GridData());
        label_2.setText("描述：");

        textDescription = new Text(container, SWT.BORDER);
        final GridData gd_textDescription = new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1);
        textDescription.setLayoutData(gd_textDescription);
        textDescription.addFocusListener(AutoSelectAll.instance);
        textDescription.addModifyListener(this);

        final Composite composite = new Composite(container, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
        final GridLayout gridLayout_1 = new GridLayout();
        gridLayout_1.numColumns = 3;
        composite.setLayout(gridLayout_1);

        final Label label_3 = new Label(composite, SWT.NONE);
        label_3.setText("装备列表：");

        final Label label_4 = new Label(composite, SWT.NONE);
        label_4.setText("套装效果：");

        final Label label_5 = new Label(composite, SWT.NONE);
        label_5.setText("预览：");

        equipmentTreeViewer = new ItemTreeViewer(composite, SWT.FULL_SELECTION | SWT.BORDER | SWT.MULTI | SWT.BORDER);
        equipmentTreeViewer.setLabelProvider(new EquipmentTreeLabelProvider());
        equipmentTreeViewer.setContentProvider(new EquipmentTreeContentProvider());
        equipmentTreeViewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                StructuredSelection sel = (StructuredSelection)event.getSelection();
                if (sel.isEmpty()) {
                    return;
                }
                if (sel.getFirstElement() instanceof String) {
                    // 添加装备
                    ChooseItemDialog dlg = new ChooseItemDialog(getSite().getShell());
                    dlg.setMultiSel(true);
                    dlg.setIncludeItem(false);
                    if (dlg.open() == Dialog.OK) {
                        List<Item> sels = dlg.getSelectedItems();
                        SuiteConfig_New suite = (SuiteConfig_New)editObject;
                        boolean modified = false;
                        for (Item item : sels) {
                            if (suite.equipments.contains(item)) {
                                continue;
                            }
                            if (item instanceof Equipment) {
                                suite.equipments.add((Equipment)item);
                                try {
                                    SuiteConfig_New.SuiteEffect_New eff = new SuiteConfig_New.SuiteEffect_New();
                                    eff.weight=0;
                                    eff.buff2ID=0;
                                    eff.equipName=item.title;
                                        ((SuiteConfig_New)editObject).effects.add(eff);
                                    effectTableViewer.refresh();
                                    setDirty(true);
                                    updatePreview();
                                } catch (Exception e) {
                                }
                                modified = true;
                            }
                        }
                        if (modified) {
                            setDirty(true);
                            equipmentTreeViewer.refresh();
                            updatePreview();
                        }
                    }
                } else {
                    Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
                    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                    DataListView view = (DataListView)page.findView(DataListView.ID);
                    Equipment equ = (Equipment)sel.getFirstElement();
                    if (view != null) {
                        view.editObject(equ);
                    }
                }
            }
        });
        equipmentTree = equipmentTreeViewer.getTree();
        equipmentTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        equipmentTree.addListener(SWT.KeyDown, new Listener() {
            public void handleEvent(Event event) {
                if (event.keyCode == SWT.DEL) {
                    // 删除装备
                    StructuredSelection sel = (StructuredSelection)equipmentTreeViewer.getSelection();
                    if (sel.isEmpty()) {
                        return;
                    }
                    boolean modified = false;
                    SuiteConfig_New suite = (SuiteConfig_New)editObject;
                    Object[] arr = sel.toArray();
                    for (Object o : arr) {
                        if (suite.equipments.contains(o)) {
                            suite.equipments.remove(o);
                            modified = true;
                        }
                    }
                    if (modified) {
                        setDirty(true);
                        equipmentTreeViewer.refresh();
                        updatePreview();
                    }
                }
            }
        });

        effectTableViewer = new TableViewer(composite, SWT.FULL_SELECTION | SWT.BORDER);
        effectTableViewer.setCellModifier(new EffectTableCellModifier());
        effectTableViewer.setLabelProvider(new EffectTableLabelProvider());
        effectTableViewer.setContentProvider(new EffectTableContentProvider());
        effectTable = effectTableViewer.getTable();
        effectTable.setLinesVisible(true);
        effectTable.setHeaderVisible(true);
        effectTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        effectTableViewer.setColumnProperties(new String[] {
                "c0", "c1", "c2", "c3", "c4"
        });
        effectTableViewer.setCellEditors(new CellEditor[] {
                new TextCellEditor(effectTable),
                new BuffCellEditor(effectTable),
                new TextCellEditor(effectTable),
                new TextCellEditor(effectTable),
                new BuffCellEditor(effectTable),
        });
        
        final TableColumn countColumn = new TableColumn(effectTable, SWT.NONE);
        countColumn.setWidth(100);
        countColumn.setText("装备名称");

        final TableColumn effectColumn = new TableColumn(effectTable, SWT.NONE);
        effectColumn.setWidth(100);
        effectColumn.setText("效果");

        final TableColumn levelColumn = new TableColumn(effectTable, SWT.NONE);
        levelColumn.setWidth(100);
        levelColumn.setText("装备权重>=");
        final TableColumn weightColumn = new TableColumn(effectTable, SWT.NONE);
        weightColumn.setWidth(100);
        weightColumn.setText("权重判定条件");
        final TableColumn effect2Column = new TableColumn(effectTable, SWT.NONE);
        effect2Column.setWidth(100);
        effect2Column.setText("效果2");

        final Composite composite_1 = new Composite(composite, SWT.NONE);
        composite_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        composite_1.setLayout(new FillLayout());
        previewer = new RichTextPreviewer(composite_1, SWT.NONE);

        // 设置初始值
        updateView();
        
        setDirty(false);
        setPartName(this.getEditorInput().getName());
        saveStateToUndoBuffer();
    }
    
    private void updateView() {
        // 设置初始值
        SuiteConfig_New dataDef = (SuiteConfig_New)editObject;
        textID.setText(String.valueOf(dataDef.id));
        textTitle.setText(dataDef.title);
        textDescription.setText(dataDef.description);
        equipmentTreeViewer.setInput(dataDef);
        effectTableViewer.setInput(dataDef);
        updatePreview();
    }
    
    private void updatePreview() {
        SuiteConfig_New dataDef = (SuiteConfig_New)editObject;
        StringBuilder sb = new StringBuilder();
        
        // 套装效果
        for (SuiteConfig_New.SuiteEffect_New eff : dataDef.effects) {
//            sb.append("<c00bb00>" + eff.count + "件：");
            BuffConfig bc = (BuffConfig)dataDef.owner.findObject(BuffConfig.class, eff.buffID);
            if (bc == null) {
                sb.append("无效效果</c>\n");
//            } else if (eff.buffLevel < 1 || eff.buffLevel > bc.maxLevel) {
//                sb.append("无效效果</c>\n");
            } else {
                DescriptionPattern pat = new DescriptionPattern(bc);
                String desc = "";//pat.generate(eff.buffLevel);
                sb.append(desc + "</c>\n");
            }
        }
        
        // 套装统计
        sb.append(" \n \n属性统计\n \n");
        sb.append("共" + dataDef.equipments.size() + "件\n");
        int minatk = 0, maxatk = 0, magicatk = 0;
        for (Equipment equ : dataDef.equipments) {
            boolean isWeapon = equ.equipmentType == Equipment.EQUI_TYPE_WEAPON;
            if (isWeapon) {
                minatk += equ.getAttribute(AttributeCalculator.ATTRIBUTE_MINATTACK);
                maxatk += equ.getAttribute(AttributeCalculator.ATTRIBUTE_MAXATTACK);
                magicatk += equ.getAttribute(AttributeCalculator.ATTRIBUTE_MAGICPOWER);
            }
        }
        if (maxatk > 0) {
            sb.append("攻击：" + minatk + "~" + maxatk + "\n");
        }
        if (magicatk > 0) {
            sb.append("法攻：" + magicatk + "\n");
        }
        
        // 防具：护甲
        int armor = 0;
        for (Equipment equ : dataDef.equipments) {
            boolean isWeapon = equ.equipmentType == Equipment.EQUI_TYPE_WEAPON;
            if (!isWeapon) {
                armor += equ.getAttribute(AttributeCalculator.ATTRIBUTE_ARMOR);
            }
        }
        if (armor > 0) {
            sb.append("护甲：" + armor + "\n");
        }
        
        // 其他属性
        sb.append(" \n");
        int[] attrs = new int[AttributeCalculator.ATTRIBUTES.length];
        for (int i = 0; i < AttributeCalculator.ATTRIBUTES.length; i++) {
            EquipmentAttribute attr = AttributeCalculator.ATTRIBUTES[i];
            for (Equipment equ : dataDef.equipments) {
                boolean isWeapon = equ.equipmentType == Equipment.EQUI_TYPE_WEAPON;
                if (isWeapon) {
                    // 武器的攻击和法攻这里不再显示
                    if (i == AttributeCalculator.ATTRIBUTE_ATTACKPOWER || i == AttributeCalculator.ATTRIBUTE_MAGICPOWER) {
                        continue;
                    }
                } else {
                    // 防具的护甲这里不再显示
                    if (i == AttributeCalculator.ATTRIBUTE_ARMOR) {
                        continue;
                    }
                }
                int value = equ.getAttribute(i);
                attrs[i] += value;
            }
            if (attrs[i] > 0) {
                sb.append("<c00BB00>");
                sb.append(attr.shortName);
                sb.append(" +");
                sb.append(attrs[i]);
                sb.append("</c>\n");
            }
        }
        
        previewer.setText(sb.toString());
    }
    
    /**
     * 保存当前编辑数据。
     */
    protected void saveData() throws Exception {
        SuiteConfig_New dataDef = (SuiteConfig_New)editObject;

        // 读取输入：对象ID、标题、描述、类型、级别、价格、俸禄、阵营、增益效果
        try {
            dataDef.id = Integer.parseInt(textID.getText());
        } catch (Exception e) {
            throw new Exception("请输入正确的ID。");
        }
        dataDef.title = textTitle.getText().trim();
        dataDef.description = textDescription.getText();
        
        // 检查输入合法性
        DataObject dobj = EditorApplication.getInstance().getProjectData().findObject(dataDef.getClass(), dataDef.id);
        if (dobj != null && dobj != getSaveTarget()) {
            throw new Exception("ID重复，请重新输入。");
        }
        if (dataDef.title.length() == 0) {
            throw new Exception("请输入标题。");
        }
    }
}
