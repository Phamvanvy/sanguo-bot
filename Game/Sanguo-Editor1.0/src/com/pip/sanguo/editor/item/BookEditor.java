package com.pip.sanguo.editor.item;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import com.pip.sanguo.data.BookChapter;
import com.pip.sanguo.data.BookConfig;
import com.pip.sanguo.editor.DefaultDataObjectEditor;
import com.pip.sanguo.editor.property.BookBasicAttrAdvanceDialog;
import com.pip.util.AutoSelectAll;

public class BookEditor extends DefaultDataObjectEditor{
    private Table table_1;
    private Combo comboClass;
    private Combo comboClass2;
    private Text textUpLimit;
    private Text textName;
    private Text textID;
    private Text textDescription;
    private TableViewer tableViewer_1;
    public static final String ID = "com.pip.sanguo.editor.item.BookEditor"; //$NON-NLS-1$
    
    
    /**
     * 书籍提升属性的列文本：五列分别为:等级,取值,时间
     */
    class BasicAttrTableLabelProvider extends LabelProvider implements ITableLabelProvider {
        
        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof String) {
                if (columnIndex == 0) {
                    return "新等级...";
                } else {
                    return "";
                }
            } else {
                BookChapter attr = (BookChapter)element;
                if (columnIndex == 0) {
                    return String.valueOf(attr.level); 
                } else if (columnIndex == 1) {
                    return String.valueOf(attr.value);
                } else {
                    return String.valueOf(attr.time);
                }
            }
        }
        
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }
    }
    /**
     * 书籍提升属性的内容：每个等级一行，最后一行用空串表示新建选项。
     */
    class BasicAttrTableContentProvider implements IStructuredContentProvider {
        public Object[] getElements(Object inputElement) {
            BookConfig book = (BookConfig)inputElement;
            Object[] ret = new Object[book.basicAttrAdvances.size() + 1];
            book.basicAttrAdvances.toArray(ret);
            ret[book.basicAttrAdvances.size()] = "";
            return ret;
        }
        public void dispose() {
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }

    /**
     * Create contents of the editor part
     * @param parent
     */
    @Override
    public void createPartControl(Composite parent) {
        System.currentTimeMillis();
        Composite container = new Composite(parent, SWT.NONE);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 4;
        container.setLayout(gridLayout);

        final Label label = new Label(container, SWT.NONE);
        label.setText("ID：");

        textID = new Text(container, SWT.BORDER);
        final GridData gd_textID = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textID.setLayoutData(gd_textID);
        textID.addModifyListener(this);

//        final Label label_1 = new Label(container, SWT.NONE);
//        label_1.setLayoutData(new GridData());
//        label_1.setText("名称：");
//
//        textName = new Text(container, SWT.BORDER);
//        final GridData gd_textName = new GridData(SWT.FILL, SWT.CENTER, true, false);
//        textName.setLayoutData(gd_textName);
//        textName.addModifyListener(this);
        
        final Label label_1 = new Label(container, SWT.NONE);
        label_1.setLayoutData(new GridData());
        label_1.setText("名称：");

        textName = new Text(container, SWT.BORDER);
        final GridData gd_textName = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textName.setLayoutData(gd_textName);
        textName.addFocusListener(AutoSelectAll.instance);
        textName.addModifyListener(this);

        final Label label_2 = new Label(container, SWT.NONE);
        label_2.setLayoutData(new GridData());
        label_2.setText("属性：");

        comboClass = new Combo(container, SWT.NONE);
        comboClass.select(4);
        comboClass.setItems(new String[] {"力量", "敏捷", "体力", "智力"});
        final GridData gd_comboClass = new GridData(SWT.FILL, SWT.CENTER, true, false);
        comboClass.setLayoutData(gd_comboClass);
        comboClass.addModifyListener(this);
        
        final Label label_3 = new Label(container, SWT.NONE);
        label_3.setLayoutData(new GridData());
        label_3.setText("等级上限：");

        textUpLimit = new Text(container, SWT.BORDER);
        final GridData gd_textUpLimit = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textUpLimit.setLayoutData(gd_textUpLimit);
        textUpLimit.addModifyListener(this);
        
        final Label label_4 = new Label(container, SWT.NONE);
        label_4.setLayoutData(new GridData());
        label_4.setText("是否预装：");

        comboClass2 = new Combo(container, SWT.NONE);
        comboClass2.select(2);
        comboClass2.setItems(new String[] {"否","是"});
        final GridData gd_comboClass2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        comboClass2.setLayoutData(gd_comboClass2);
        comboClass2.addModifyListener(this);
        
        final Label label_5 = new Label(container, SWT.NONE);
//        label_5.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
//        label_5.setText("物品说明：");
//        
//        textDescription = new Text(container, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.WRAP);
//        final GridData gd_textDescription_1 = new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1);
//        gd_textDescription_1.heightHint = 71;
//        textDescription.setLayoutData(gd_textDescription_1);
        
        label_5.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1));
        label_5.setText("物品描述：");

        textDescription = new Text(container, SWT.MULTI | SWT.BORDER);
        textDescription.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                setDirty(true);
            }
        });
        final GridData gd_tfDescription = new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1);
        gd_tfDescription.heightHint = 77;
        textDescription.setLayoutData(gd_tfDescription);
       

       

        final Label label_6 = new Label(container, SWT.NONE);
        label_6.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
        label_6.setText("属性成长：(初始以及升级后各项基本属性的提升)");
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);

        tableViewer_1 = new TableViewer(container, SWT.FULL_SELECTION | SWT.BORDER);
        tableViewer_1.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(final DoubleClickEvent event) {
                IStructuredSelection sel = (IStructuredSelection)tableViewer_1.getSelection();
                if (!sel.isEmpty()) {
                    onDoubleClick(tableViewer_1, sel.getFirstElement());
                }
            }
        });
        tableViewer_1.setLabelProvider(new BasicAttrTableLabelProvider());
        tableViewer_1.setContentProvider(new BasicAttrTableContentProvider());
        table_1 = tableViewer_1.getTable();
        table_1.setLinesVisible(true);
        table_1.setHeaderVisible(true);
        final GridData gd_table_1 = new GridData(SWT.LEFT, SWT.FILL, true, true, 4, 1);
        gd_table_1.widthHint = 1000;
        table_1.setLayoutData(gd_table_1);
        table_1.addListener(SWT.KeyDown, new Listener() {
            public void handleEvent(Event event) {
                event.doit = !handleKey(tableViewer_1, event.keyCode, event.stateMask);
            }
        });

        final TableColumn levelColumn_1 = new TableColumn(table_1, SWT.NONE);
        levelColumn_1.setWidth(46);
        levelColumn_1.setText("等级");

        final TableColumn strColumn = new TableColumn(table_1, SWT.NONE);
        strColumn.setWidth(40);
        strColumn.setText("成长价值");

        final TableColumn agiColumn = new TableColumn(table_1, SWT.NONE);
        agiColumn.setWidth(40);
        agiColumn.setText("时间(分)");

        tableViewer_1.setInput(this.getEditObject());
        
        // 设置初始值
        updateView();
        
        setDirty(false);
        setPartName(this.getEditorInput().getName());
        saveStateToUndoBuffer();
    }
    
    /**
     * 设置初始值
     */
    public void updateView() {
        BookConfig ret = (BookConfig)editObject;
        textID.setText(String.valueOf(ret.id));
        textName.setText(ret.title);
        comboClass.select(ret.property);
        textUpLimit.setText(String.valueOf(ret.upLimit));
        textDescription.setText(ret.dec);
        comboClass2.select(ret.auto);
    }
    
    /**
     * 保存当前编辑器数据
     */
    protected void saveData() throws Exception {
        BookConfig ret = (BookConfig)editObject;
        try {
            ret.id = Integer.parseInt(textID.getText());
        } catch (Exception e) {
            throw new Exception("请输入正确的ID");
        }
        ret.title = textName.getText().trim();
        ret.property = comboClass.getSelectionIndex();
        ret.auto = comboClass2.getSelectionIndex();
        try {
            ret.upLimit = Integer.parseInt(textUpLimit.getText());
        } catch (Exception e) {
            throw new Exception("请输入正确的法宝最高等级");
        }
        try {
            ret.dec = textDescription.getText();
        } catch (Exception e) {
            throw new Exception("请输入正确的法宝最高等级");
        }
    }
      
    /**
     * 处理Table控件的特殊按键事件。
     * @param viewer 事件来源
     * @param keyCode 键码
     * @param mask 掩码
     * @return 如果不希望这个事件被控件处理，返回true。
     */
    private boolean handleKey(Object viewer, int keyCode, int mask) {
       if (viewer == tableViewer_1) {
            if (keyCode == SWT.DEL) {
                int sel = table_1.getSelectionIndex();
                if (sel != -1 && sel < getBook().basicAttrAdvances.size()) {
                    getBook().basicAttrAdvances.remove(sel);
                    tableViewer_1.refresh();
                    setDirty(true);
                    table_1.setSelection(sel);
                }
                return true;
            }
        }
        return false;
    }
    
    /**
     * 处理Table控件的双击事件。
     * @param viewer 事件来源
     * @param sel 当前选中对象
     */
    private void onDoubleClick(Object viewer, Object sel) {
        if (viewer == tableViewer_1) {
            if ("".equals(sel)) {
                // 新建目标
                BookChapter attr = new BookChapter(getBook());
                if (new BookBasicAttrAdvanceDialog(getSite().getShell(), attr).open() == Dialog.OK) {
                    getBook().basicAttrAdvances.add(attr);
                    tableViewer_1.refresh();
                    setDirty(true);
                }
            } else {
                // 编辑选中目标
                BookChapter attr = (BookChapter)sel;
                if (new BookBasicAttrAdvanceDialog(getSite().getShell(), attr).open() == Dialog.OK) {
                    tableViewer_1.refresh(attr);
                    setDirty(true);
                }
            }
        }
    }
    
    /**
     * 当前编辑的书籍
     * @return 书籍
     */
    private BookConfig getBook() {
        return (BookConfig)getEditObject();
    }
}