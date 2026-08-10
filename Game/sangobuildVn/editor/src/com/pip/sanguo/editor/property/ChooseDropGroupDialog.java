package com.pip.sanguo.editor.property;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;

import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.DataObjectCategory;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.equipment.Equipment;
import com.pip.sanguo.data.item.DropGroup;
import com.pip.sanguo.data.item.DropItem;
import com.pip.sanguo.data.item.DropNode;
import com.pip.sanguo.data.item.Item;
import com.pip.sanguo.data.quest.Quest;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.EditorPlugin;

public class ChooseDropGroupDialog  extends Dialog {
    private Combo comboTaskFlag;
    private ComboViewer taskComboViewer;
    private Text textDropRate;
    private Text dropQuantityMax;
    private Text dropQuantityMin;
    private Text text;
    private String searchCondition;
    
    class TreeLabelProvider extends LabelProvider {
        public String getText(Object element) {
            if (element instanceof ProjectData) {
                return "项目";
            } else if (element instanceof DropNode) {
                switch(((DropNode)element).type){
                case DropNode.TYPE_DROPGROUP:
                    return "掉落组";
                case DropNode.TYPE_EQUIPMENT:
                    return "装备";
                case DropNode.TYPE_ITEM:
                    return "物品";
                default:
                    return "";
                }
            }
            return super.getText(element);
        }
        
        public Image getImage(Object element) {
            if (element instanceof DataObjectCategory) {
                return EditorPlugin.getDefault().getImageRegistry().get("itemtype");
            } 
            else if (element instanceof Item || element instanceof Equipment) {
                return EditorPlugin.getDefault().getImageRegistry().get("item");
            }
            else if (element instanceof DropGroup) {
                return EditorPlugin.getDefault().getImageRegistry().get("dropgroup");
            }
            else if (element instanceof DropItem) {
                return EditorPlugin.getDefault().getImageRegistry().get("dropitem");
            }
            else if (element instanceof DropNode) {
                return EditorPlugin.getDefault().getImageRegistry().get("rootnode");
            }
            return null;
        }
    }
    class TreeContentProvider implements IStructuredContentProvider, ITreeContentProvider {
        private DropNode itemNode = new DropNode(DropNode.TYPE_ITEM);
        private DropNode equNode = new DropNode(DropNode.TYPE_EQUIPMENT);
        private DropNode dropGroupNode = new DropNode(DropNode.TYPE_DROPGROUP);
        
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
        public void dispose() {
        }
        public Object[] getElements(Object inputElement) {
            return getChildren(inputElement);
        }
        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof ProjectData) {
                List<DropNode> retList = new ArrayList<DropNode>();
                retList.add(itemNode);
                retList.add(equNode);
                retList.add(dropGroupNode);
                return retList.toArray();
            } else if (parentElement instanceof DropNode) {
                DropNode node = (DropNode)parentElement;
                Object[] arr;
                if(node.type == DropNode.TYPE_ITEM){
                    //物品根节点
                    arr = EditorApplication.getProj().getCategoryListByType(Item.class).toArray();
                }
                else if(node.type == DropNode.TYPE_EQUIPMENT){
                    //装备根节点
                    arr = EditorApplication.getProj().getCategoryListByType(Equipment.class).toArray();
                }
                else if(node.type == DropNode.TYPE_DROPGROUP){
                    //掉落组根节点
                    arr = EditorApplication.getProj().getCategoryListByType(DropGroup.class).toArray();
                } else {
                    return new Object[0];
                }
                List<Object> retList = new ArrayList<Object>();
                for (Object o : arr) {
                    if (getChildren(o).length > 0) {
                        retList.add(o);
                    }
                }
                return retList.toArray();
            } else if (parentElement instanceof DataObjectCategory) {
                if (searchCondition == null || searchCondition.length() == 0) {
                    return ((DataObjectCategory)parentElement).objects.toArray();
                } else {
                    List<DataObject> list = ((DataObjectCategory)parentElement).objects;
                    List<DataObject> retList = new ArrayList<DataObject>();
                    for (DataObject dobj : list) {
                        if (matchCondition(dobj)) {
                            retList.add(dobj);
                        }
                    }
                    return retList.toArray();            
                }
            }
            return new Object[0];
        }
        
        public Object getParent(Object element) {
            if (element instanceof ProjectData) {
                return null;
            } else if (element instanceof DropNode) {
                return EditorApplication.getProj();
            } else if (element instanceof DataObjectCategory) {
                Class cls = ((DataObjectCategory)element).dataClass;
                if (cls == Item.class) {
                    return itemNode;
                } else if (cls == Equipment.class) {
                    return equNode;
                }
            } else if (element instanceof Equipment) {
                return EditorApplication.getProj().findCategory(Equipment.class, ((Equipment)element).categoryName);
            } else if (element instanceof Item) {
                return EditorApplication.getProj().findCategory(Item.class, ((Item)element).categoryName);
            } else if (element instanceof DropGroup) {
                return dropGroupNode;
            }
            return null;
        }
        public boolean hasChildren(Object element) {
            return (element instanceof ProjectData || element instanceof DropNode|| element instanceof DataObjectCategory);
        }
    }
    private TreeViewer treeViewer;
    private Tree tree;
    private DropNode selectedObject;
    
    /**
     * 选中项目id
     * @return
     */
    public DropNode getSelectedObject() {
        
        return selectedObject;
    }

    public void setSelectedItem(DropNode selectedItem) {
        this.selectedObject = selectedItem;
    }
    
    private boolean matchCondition(DataObject dataobj) {
        if (searchCondition == null || searchCondition.length() == 0) {
            return true;
        }
        if (dataobj.title.indexOf(searchCondition) >= 0 || String.valueOf(dataobj.id).indexOf(searchCondition) >= 0) {
            return true;
        }
        return false;
    }

    /**
     * Create the dialog
     * @param parentShell
     */
    public ChooseDropGroupDialog(Shell parentShell) {
        super(parentShell);
    }

    /**
     * Create contents of the dialog
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        container.setLayout(gridLayout);

        final Label label_1 = new Label(container, SWT.NONE);
        label_1.setText("掉落数量：");

        dropQuantityMin = new Text(container, SWT.BORDER);
        final GridData gd_dropQuantityMin = new GridData(SWT.FILL, SWT.CENTER, true, false);
        dropQuantityMin.setLayoutData(gd_dropQuantityMin);

        dropQuantityMax = new Text(container, SWT.BORDER);
        final GridData gd_dropQuantityMax = new GridData(SWT.FILL, SWT.CENTER, true, false);
        dropQuantityMax.setLayoutData(gd_dropQuantityMax);

        final Label label_2 = new Label(container, SWT.NONE);
        label_2.setText("掉落几率：");

        textDropRate = new Text(container, SWT.BORDER);
        final GridData gd_dropRate = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        textDropRate.setLayoutData(gd_dropRate);
        new Label(container, SWT.NONE);

        final Label label_5 = new Label(container, SWT.NONE);
        label_5.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
        label_5.setText("示例：1% = 百分之一   1%% = 万分之一   1%%% = 百万分之一");

        final Label label_3 = new Label(container, SWT.NONE);
        label_3.setText("是否任务：");

        comboTaskFlag = new Combo(container, SWT.READ_ONLY);
        comboTaskFlag.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        comboTaskFlag.setItems(new String[]{"否","是"});
        comboTaskFlag.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent e) {
                taskComboViewer.getControl().setEnabled(comboTaskFlag.getSelectionIndex() == DropNode.VALUE_YES);
            }
        });

        final Label label_4 = new Label(container, SWT.NONE);
        label_4.setText("所属任务：");

        taskComboViewer = new ComboViewer(container, SWT.READ_ONLY);
        taskComboViewer.setLabelProvider(new LabelProvider(){
            public String getText(Object element){
                if(element == null){
                    return "";
                }
                return element.toString();
            }
        });
        taskComboViewer.setContentProvider(new IStructuredContentProvider(){
            /**
             * 返回所有任务列表
             */
            public Object[] getElements(Object inputElement) {
                return EditorApplication.getProj().getDataListByType(Quest.class).toArray();
            }

            public void dispose() {}

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
        });
        taskComboViewer.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        taskComboViewer.setInput(new Object());

        final Label label = new Label(container, SWT.NONE);
        label.setLayoutData(new GridData());
        label.setText("查找：");

        text = new Text(container, SWT.BORDER);
        text.addModifyListener(new ModifyListener() {
            public void modifyText(final ModifyEvent e) {
                searchCondition = text.getText();
                StructuredSelection sel = (StructuredSelection)treeViewer.getSelection();
                Object selObj = sel.isEmpty() ? null : sel.getFirstElement();
                treeViewer.refresh();
                treeViewer.expandAll();
                if (selObj != null) {
                    sel = new StructuredSelection(selObj);
                    treeViewer.setSelection(sel);
                }
            }
        });
        text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

        treeViewer = new TreeViewer(container, SWT.BORDER);
        treeViewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(final DoubleClickEvent event) {
                StructuredSelection sel = (StructuredSelection)event.getSelection();
                if (sel.isEmpty()) {
                    return;
                }
                Object selObj = sel.getFirstElement();
                if (selObj instanceof Item || selObj instanceof DropGroup /*|| (selObj instanceof DropNode && ((DropNode)selObj).type == DropNode.TYPE_MONEY)*/) {
                    buttonPressed(IDialogConstants.OK_ID);
                } 
                else {
                    if (treeViewer.getExpandedState(selObj)) {
                        treeViewer.collapseToLevel(selObj, 1);
                    } else {
                        treeViewer.expandToLevel(selObj, 1);
                    }
                }
            }
        });
        treeViewer.setLabelProvider(new TreeLabelProvider());
        treeViewer.setContentProvider(new TreeContentProvider());
        tree = treeViewer.getTree();
        tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
        treeViewer.setInput(EditorApplication.getInstance().getProjectData());
        // treeViewer.expandAll();
        
        /**
         * 如果选择框有默认值，则需要设定默认值
         */
        if (selectedObject != null) {
            try {
                switch(selectedObject.type){
                    case DropNode.TYPE_DROPGROUP:{
                        DataObject drip = EditorApplication.getInstance().getProjectData().findObject(DropGroup.class, selectedObject.id);
                        if (drip != null) {
                            searchCondition = drip.title;
                            StructuredSelection sel = new StructuredSelection(drip);
                            treeViewer.setSelection(sel);
                            treeViewer.expandToLevel(drip, 1);
                        }
                        break;
                    }
                    case DropNode.TYPE_EQUIPMENT:{
                        DataObject drip = EditorApplication.getInstance().getProjectData().findEquipment(selectedObject.id);
                        if (drip != null) {
                            searchCondition = drip.title;
                            StructuredSelection sel = new StructuredSelection(drip);
                            treeViewer.setSelection(sel);
                            treeViewer.expandToLevel(drip, 1);
                        }
                        break;
                    }
                    case DropNode.TYPE_ITEM:{
                        Item item = EditorApplication.getInstance().getProjectData().findItem(selectedObject.id);
                        if (item != null) {
                            searchCondition = item.title;
                            StructuredSelection sel = new StructuredSelection(item);
                            treeViewer.setSelection(sel);
                            treeViewer.expandToLevel(item, 1);
                        }
                        break;
                    }
//                    case DropNode.TYPE_MONEY:{
//                        searchCondition = "金钱";
//                        StructuredSelection sel = new StructuredSelection(searchCondition);
//                        treeViewer.setSelection(sel);
//                        break;
//                    }
                }
                
                if(searchCondition != null){
                    text.setText(searchCondition);
                }
                dropQuantityMax.setText(String.valueOf(selectedObject.quantityMax));
                dropQuantityMin.setText(String.valueOf(selectedObject.quantityMin));
                textDropRate.setText(String.valueOf(selectedObject.getRateString()));
                comboTaskFlag.select(selectedObject.isTask?DropNode.VALUE_YES:DropNode.VALUE_NO);
                
                DataObject currentQuset = EditorApplication.getProj().findObject(Quest.class, selectedObject.taskId);
                StructuredSelection selQuest = new StructuredSelection(currentQuset);
                taskComboViewer.setSelection(selQuest);
            } catch (Exception e) {
            }
        }

        return container;
    }

    /**
     * Create contents of the button bar
     * @param parent
     */
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, "确定", true);
        createButton(parent, IDialogConstants.CANCEL_ID, "取消", false);
    }

    /**
     * Return the initial size of the dialog
     */
    @Override
    protected Point getInitialSize() {
        return new Point(622, 644);
    }
    
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("选择掉落");
    }
    
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            StructuredSelection sel = (StructuredSelection)treeViewer.getSelection();
            if (!sel.isEmpty()) {
                if(sel.getFirstElement() instanceof Item || sel.getFirstElement() instanceof DropGroup
                        /*|| (sel.getFirstElement() instanceof DropNode && ((DropNode)sel.getFirstElement()).type == DropNode.TYPE_MONEY)*/){
                    if(selectedObject == null){                        
                        selectedObject = new DropNode();
                    }
                    
                    if(sel.getFirstElement() instanceof Equipment){
                        selectedObject.id = ((DataObject)sel.getFirstElement()).id;
                        selectedObject.type = DropNode.TYPE_EQUIPMENT;
                    }
                    else if(sel.getFirstElement() instanceof Item){
                        selectedObject.id = ((DataObject)sel.getFirstElement()).id;
                        selectedObject.type = DropNode.TYPE_ITEM;
                    }
                    else if(sel.getFirstElement() instanceof DropGroup){
                        selectedObject.id = ((DataObject)sel.getFirstElement()).id;
                        selectedObject.type = DropNode.TYPE_DROPGROUP;
                    }
                    
                    try {
                        selectedObject.quantityMin = Integer.parseInt(dropQuantityMin.getText());
                        selectedObject.quantityMax = Integer.parseInt(dropQuantityMax.getText());
                        
                        int dropRate = selectedObject.getDropRate(textDropRate.getText());
                        switch(dropRate){
                            case DropNode.ERROR_SYMBOL:{
                                throw new Exception("掉落机率符号错误！");
                            }
                            case DropNode.ERROR_VALUE:{
                                throw new Exception("掉落机率数值错误！");
                            }
                            case DropNode.ERROR_OUT_OF_RANGE:{
                                throw new Exception("掉落机率超出范围！");
                            }
                        }
                        selectedObject.dropRate = dropRate;
                        
                        if(selectedObject.quantityMin < 1 || selectedObject.quantityMax < 1
                                || selectedObject.quantityMin > selectedObject.quantityMax){
                            throw new Exception("掉落数量格式错误！");
                        }
                    }
                    catch (Exception e) {
                        MessageDialog.openError(Display.getCurrent().getActiveShell(), "错误！", e.getMessage());
                        return;
                    }
                    
                    selectedObject.isTask = comboTaskFlag.getSelectionIndex() == DropNode.VALUE_YES;
                    if(selectedObject.isTask){
                        StructuredSelection selQuest = (StructuredSelection)taskComboViewer.getSelection();
                        if(selQuest.isEmpty()){
                            /* 用户没有选择一个任务，提示 */
                            MessageDialog.openError(Display.getCurrent().getActiveShell(), "提示！", "请选择一个任务或者把任务状态选为否！");
                            return;
                        }
                        else{
                            selectedObject.taskId = ((DataObject)selQuest.getFirstElement()).id;
                        }
                    }
                    else{
                        selectedObject.taskId = -1;
                    }
                }
            } else {
                MessageDialog.openInformation(getShell(), "提示", "请选择一个掉落物品！");
                return;
            }
        }
        super.buttonPressed(buttonId);
    }

}
