package com.pip.sanguo.editor.card;

import java.awt.BorderLayout;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

import com.pip.propertysheet.PropertySheetEntry;
import com.pip.propertysheet.PropertySheetViewer;
import com.pip.sanguo.data.Card;
import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.Rank;
import com.pip.sanguo.data.Title;
import com.pip.sanguo.data.Card.DropObject;
import com.pip.sanguo.data.Card.Material;
import com.pip.sanguo.data.item.DropGroup;
import com.pip.sanguo.data.item.Item;
import com.pip.sanguo.editor.DefaultDataObjectEditor;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.property.ChooseCardDialog;
import com.pip.sanguo.editor.property.ChooseDropGroupDialog2;
import com.pip.sanguo.editor.property.ChooseItemDialog;
import com.pip.sanguo.editor.property.ChooseRankDialog;
import com.pip.sanguo.editor.property.ChooseTitleDialog;
import com.pip.util.FileWatcher;
import com.pip.util.IFileModificationListener;

/**
 * 编辑卡片系列的信息，包括id，系列类型，和标题
 * 
 * @author zlguo
 * 
 */
public class CardEditor extends DefaultDataObjectEditor implements SelectionListener, Runnable,
        IFileModificationListener {

    private Table table2;
    private Table table;
    public String title;
    public Canvas canvas;
    public Object[] images;
    public Point stdPoint = new Point(10, 10);
    public int currFocusIndex = 0;
    public static Color imgBoxColor;
    public java.util.List<Rectangle> imageRects = new ArrayList<Rectangle>();
    
    Card cd ;

    public static final String ID = "com.pip.sanguo.editor.card.CardEditor"; //$NON-NLS-1$
    /**
     * 基本属性页
     */
    private PropertySheetViewer propEditor;
    private TableViewer formulaViewer;
    private TableViewer dropViewer;
    private boolean playAnimate;
    private boolean disposed;
    private Thread animateThread;
    private Display display;
    private Button[] radio;
    
    public static List<Rank> ranks;
    
    
    /**
     * Create contents of the editor part
     * 
     * @param parent
     */
    @Override
    public void createPartControl(Composite parent) {
        Card obj = (Card) editObject;
        cd = obj;
        display = getSite().getShell().getDisplay();

        // 底板
        final Composite composite = new Composite(parent, SWT.NONE);
        GridLayout gl = new GridLayout(2, true);
        composite.setLayout(gl);

        final Group groupPreview = new Group(composite, SWT.NONE);
        groupPreview.setLayout(new swing2swt.layout.BorderLayout(0, 0));
        //groupPreview.setLayout(new BorderLayout(0, 0));
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        groupPreview.setText("预览");
        groupPreview.setLayoutData(gd);

        // final Composite compositePreview = new Composite(groupPreview,
        // SWT.NONE);
        // compositePreview.setLayout(new BorderLayout(0, 0));

        // 图片视图
        canvas = new Canvas(groupPreview, SWT.NONE);
        canvas.addPaintListener(new PaintListener() {
            public void paintControl(final PaintEvent e) {
                paint(e.gc);
            }
        });

        final Composite compositeSelect = new Composite(groupPreview, SWT.NONE);
        compositeSelect.setLayoutData(BorderLayout.SOUTH);
        compositeSelect.setLayout(new RowLayout());
        radio = new Button[2];
        radio[0] = new Button(compositeSelect, SWT.RADIO);
        radio[0].addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                canvas.redraw();
            }
        });
        radio[0].setText("176x208");

        radio[1] = new Button(compositeSelect, SWT.RADIO);
        radio[1].addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                canvas.redraw();
            }
        });
        radio[1].setText("240x320");
        radio[1].setSelection(true);

        final Button buttonFile = new Button(compositeSelect, SWT.NONE);
        buttonFile.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                FileDialog fd = new FileDialog(display.getActiveShell());
                fd.setFilterNames(new String[]{"可移植网络图形     *.png"});
                fd.setFilterExtensions(new String[]{"*.png"});
                String path = fd.open();
                if(path != null){
                    Card card = (Card)editObject;
                    int index = 0;
                    for (int i = 0; i < radio.length; i++) {
                        if(radio[i].getSelection()){
                            index = i;
                            break;
                        }
                    }
                    String dest = card.owner.baseDir.getPath() + Card.PATH_NAMES[index] + "card" + card.id + ".png";
                    cd.res = "card" + card.id + ".png";
                    if(copyFile(path, dest)){
                        Image img = new Image(display,dest);
                        images[index] = img; 
                        canvas.redraw();
                    }
                    setDirty(true);
                }
                
            }
        });
        buttonFile.setText("新资源");

        final Button buttonFileOld = new Button(compositeSelect, SWT.NONE);
        buttonFileOld.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                ChooseCardDialog dlg = new ChooseCardDialog(display.getActiveShell());
                dlg.setSelectedItem(-1);
                if (dlg.open() == Dialog.OK) {
                    int cardId = dlg.getSelectedItem();
                    Card item = EditorApplication.getInstance().getProjectData().findCard(cardId);
                    cd.res = item.res;
                    for (int i = 0; i < radio.length; i++) {
                        String dest = cd.owner.baseDir.getPath() + Card.PATH_NAMES[i] + cd.res;
                        Image img = new Image(display,dest);
                        images[i] = img; 
                        canvas.redraw();
                    }
                    setDirty(true);
                }
            }
        });
        buttonFileOld.setText("重用资源");

        final Group groupPrimary = new Group(composite, SWT.NONE);
        groupPrimary.setText("基本信息");
        groupPrimary.setLayout(new FillLayout());
        GridData gd2 = new GridData(SWT.FILL, SWT.FILL, true, true);
        groupPrimary.setLayoutData(gd2);

        Composite compositePrimary = new Composite(groupPrimary, SWT.NONE);
        compositePrimary.setLayout(new FillLayout());

        // propComposite.setSize(1000, 2000);

        propEditor = new PropertySheetViewer(compositePrimary, SWT.NONE, true);

        final Group groupCombo = new Group(composite, SWT.NONE);
        groupCombo.setText(" 合成配方");
        groupCombo.setLayout(new FillLayout());
        GridData gd3 = new GridData(SWT.FILL, SWT.FILL, true, true);
        groupCombo.setLayoutData(gd3);

        final Group groupEffects = new Group(composite, SWT.NONE);
        groupEffects.setEnabled(false);
        groupEffects.setText("使用效果");
        groupEffects.setLayout(new FillLayout());
        GridData gd4 = new GridData(SWT.FILL, SWT.FILL, true, true);
        groupEffects.setLayoutData(gd4);

        final Composite compositeEffects = new Composite(groupEffects, SWT.NONE);
        compositeEffects.setEnabled(false);
        compositeEffects.setLayout(new FillLayout());
        PropertySheetEntry rootEntry = new PropertySheetEntry();
        propEditor.setRootEntry(rootEntry);
        
        CardPropertySource cps = new CardPropertySource(this, obj);
        propEditor.setInput(new Object[] { cps });

        formulaViewer = new TableViewer(groupCombo, SWT.FULL_SELECTION | SWT.BORDER);
        formulaViewer.setLabelProvider(new FormulaLabelProvider());
        formulaViewer.setContentProvider(new FormulaContentProvider());
        table = formulaViewer.getTable();
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        

        final TableColumn columnType = new TableColumn(table, SWT.NONE);
        columnType.setWidth(200);
        columnType.setText("种类");

        final TableColumn columnID = new TableColumn(table, SWT.NONE);
        columnID.setWidth(200);
        columnID.setText("ID");

        final TableColumn columnValue = new TableColumn(table, SWT.NONE);
        columnValue.setWidth(200);
        columnValue.setText("数值");

        formulaViewer.setColumnProperties(new String[] {
                "c0", "c1", "c2"
        });
        formulaViewer.setCellModifier(new FormulaCellModifier());
        formulaViewer.setCellEditors(new CellEditor[] {
                new TextCellEditor(table),
                new TextCellEditor(table),
                new TextCellEditor(table)
        });

        final Menu menuFormula = new Menu(table);
        table.setMenu(menuFormula);
        table.setMenu(menuFormula);

        final MenuItem itemAddCard = new MenuItem(menuFormula, SWT.NONE);
        itemAddCard.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                addNewMaterial(Card.MATERIAL_TYPE_CARD);
            }
        });
        itemAddCard.setText("扣除卡片");

        final MenuItem itemAddItem = new MenuItem(menuFormula, SWT.NONE);
        itemAddItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                addNewMaterial(Card.MATERIAL_TYPE_ITEM);
            }
        });
        itemAddItem.setText("扣除物品");

        final MenuItem itemAddMoney = new MenuItem(menuFormula, SWT.NONE);
        itemAddMoney.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                addNewMaterial(Card.MATERIAL_TYPE_MONEY);
            }
        });
        itemAddMoney.setText("扣除金钱");

        final MenuItem itemAddFame = new MenuItem(menuFormula, SWT.NONE);
        itemAddFame.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                addNewMaterial(Card.MATERIAL_TYPE_FAME);
            }
        });
        itemAddFame.setText("声望要求");

        final MenuItem itemAddLevel = new MenuItem(menuFormula, SWT.NONE);
        itemAddLevel.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                addNewMaterial(Card.MATERIAL_TYPE_LEVEL);
            }
        });
        itemAddLevel.setText("等级要求");

        final MenuItem itemAddMilitaryRank = new MenuItem(menuFormula, SWT.NONE);
        itemAddMilitaryRank.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                addNewMaterial(Card.MATERIAL_TYPE_MILITARY_RANK);
            }
        });
        itemAddMilitaryRank.setText("军衔要求");

        final MenuItem itemAddContribution = new MenuItem(menuFormula, SWT.NONE);
        itemAddContribution.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                addNewMaterial(Card.MATERIAL_TYPE_CONTRIBUTION);
            }
        });
        itemAddContribution.setText("战功要求");

        final MenuItem itemAddTitle = new MenuItem(menuFormula, SWT.NONE);
        itemAddTitle.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                addNewMaterial(Card.MATERIAL_TYPE_TITLE);
            }
        });
        itemAddTitle.setText("称号要求");

        new MenuItem(menuFormula, SWT.SEPARATOR);

        final MenuItem itemDelete = new MenuItem(menuFormula, SWT.NONE);
        itemDelete.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                deleteMaterial();
            }
        });
        itemDelete.setText("删除条目");

        final MenuItem itemClear = new MenuItem(menuFormula, SWT.NONE);
        itemClear.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
               clearMaterial();
            }
        });
        itemClear.setText("清空列表");

        
        formulaViewer.setInput(obj);

        dropViewer = new TableViewer(compositeEffects, SWT.FULL_SELECTION | SWT.BORDER);
        dropViewer.setLabelProvider(new DropLabelProvider());
        dropViewer.setContentProvider(new DropContentProvider());
        table2 = dropViewer.getTable();
        table2.setEnabled(false);
        table2.setLinesVisible(true);
        table2.setHeaderVisible(true);

        final TableColumn columnType2 = new TableColumn(table2, SWT.NONE);
        columnType2.setWidth(120);
        columnType2.setText("种类");

        final TableColumn columnID2 = new TableColumn(table2, SWT.NONE);
        columnID2.setWidth(200);
        columnID2.setText("ID");

        final TableColumn columnValue2 = new TableColumn(table2, SWT.NONE);
        columnValue2.setWidth(100);
        columnValue2.setText("数值");

        final TableColumn columnRate = new TableColumn(table2, SWT.NONE);
        columnRate.setWidth(100);
        columnRate.setText("几率(1~100)");

        final Menu menuDrop = new Menu(table2);
        table2.setMenu(menuDrop);
        table2.setMenu(menuDrop);

        final MenuItem itemDropItem = new MenuItem(menuDrop, SWT.NONE);
        itemDropItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                addNewDrop(Card.DROP_TYPE_ITEM);
            }
        });
        itemDropItem.setText("掉落物品");

        final MenuItem itemDropMoney = new MenuItem(menuDrop, SWT.NONE);
        itemDropMoney.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                addNewDrop(Card.DROP_TYPE_MONEY);
            }
        });
        itemDropMoney.setText("掉落金钱");

        final MenuItem itemDropExp = new MenuItem(menuDrop, SWT.NONE);
        itemDropExp.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                addNewDrop(Card.DROP_TYPE_EXP);
            }
        });
        itemDropExp.setText("增加经验");

        final MenuItem itemDropFame = new MenuItem(menuDrop, SWT.NONE);
        itemDropFame.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                addNewDrop(Card.DROP_TYPE_FAME);
            }
        });
        itemDropFame.setText("增加声望");

        final MenuItem itemDropMilitaryRank = new MenuItem(menuDrop, SWT.NONE);
        itemDropMilitaryRank.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                addNewDrop(Card.DROP_TYPE_MILITARY_RANK);
            }
        });
        itemDropMilitaryRank.setText("增加军衔");

        final MenuItem itemDropContribution = new MenuItem(menuDrop, SWT.NONE);
        itemDropContribution.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                addNewDrop(Card.DROP_TYPE_CONTRIBUTION);
            }
        });
        itemDropContribution.setText("增加战功");

        final MenuItem itemDropTitle = new MenuItem(menuDrop, SWT.NONE);
        itemDropTitle.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                addNewDrop(Card.DROP_TYPE_TITLE);
            }
        });
        itemDropTitle.setText("增加称号");

        final MenuItem itemDropGroup = new MenuItem(menuDrop, SWT.NONE);
        itemDropGroup.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                addNewDrop(Card.DROP_TYPE_DROP_GROUP);
            }
        });
        itemDropGroup.setText("掉落组");

        new MenuItem(menuDrop, SWT.SEPARATOR);

        final MenuItem itemDeleteDrop = new MenuItem(menuDrop, SWT.NONE);
        itemDeleteDrop.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                deleteDrop();
            }
        });
        itemDeleteDrop.setText("删除条目");

        final MenuItem itemClearDrop = new MenuItem(menuDrop, SWT.NONE);
        itemClearDrop.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                clearDrop();
            }
        });
        itemClearDrop.setText("清空列表");

        dropViewer.setColumnProperties(new String[]{"c0","c1","c2","c3"});
        dropViewer.setCellModifier(new DropCellModifier());
        dropViewer.setCellEditors(new CellEditor[] {
                new TextCellEditor(table2),
                new TextCellEditor(table2),
                new TextCellEditor(table2),
                new TextCellEditor(table2)
        });
        dropViewer.setInput(obj);
        
        setDirty(false);
        setPartName(obj.title);
        saveStateToUndoBuffer();

        // set object
        images = new Object[Card.PATH_NAMES.length];
        for (int i = 0; i < Card.PATH_NAMES.length; i++) {
            String path = obj.owner.baseDir.getPath() + Card.PATH_NAMES[i] + cd.res;
            File f = new File(path);
            Object img;
            if(f.exists() && !cd.res.equals("")){
                img = new Image(display, path);
            } else {
                img = obj.title;
            }
            images[i] = img;
        }
        
    }
    
    
    public static boolean copyFile(String src,String dest){
        try {
            FileOutputStream fos = new FileOutputStream(dest);
            BufferedOutputStream bw = new BufferedOutputStream(fos);
            DataOutputStream dos = new DataOutputStream(bw);
            byte[] b = new byte[256];
            FileInputStream fis = new FileInputStream(src);
            BufferedInputStream bis = new BufferedInputStream(fis);
            DataInputStream dis = new DataInputStream(bis);
            int readBytes = -1;
            while((readBytes = dis.read(b)) != -1){
                dos.write(b, 0, readBytes);
            }
            
            dis.close();
            
            dos.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void paint(GC gc) {
        int currIndex = 0;
        for (int i = 0; i < radio.length; i++) {
            if(radio[i].getSelection()){
                currIndex = i;
                break;
            }
        }
        if (imgBoxColor == null) {
            imgBoxColor = new Color(display, 0x0, 0x0, 0x0);
        }
        Object obj = images[currIndex];
        if(obj instanceof Image){
            Image img = (Image)obj;
            gc.drawImage(img, 6, 6);
            Rectangle rec = img.getBounds();
            gc.setForeground(imgBoxColor);
            gc.drawRectangle(5 + rec.x,5 + rec.y,rec.width,rec.height);
            gc.drawRectangle(5 + rec.x - 1,5 + rec.y - 1,rec.width + 2,rec.height + 2);
        } else {
            gc.setForeground(new Color(display,0xff,0x00,0x00));
            Rectangle rec = new Rectangle(5,5,240,320);
            String txt = "未设定此规格图片";
            int fontHeight = gc.getFontMetrics().getHeight();
            int strWidth = getStringWidth(gc, txt);
            gc.drawString(txt, rec.x + (rec.width - strWidth) / 2, rec.y + (rec.height - fontHeight) / 2);
            gc.setForeground(imgBoxColor);
            gc.drawRectangle(rec);
            gc.drawRectangle(rec.x - 1,rec.y - 1,rec.width + 2,rec.height + 2);
        }
        
        
    }

    public static int getStringWidth(GC gc,String str){
        int len = 0;
        for(int i=0;i<str.length();i++){
            char ch = str.charAt(i);
            len += gc.getAdvanceWidth(ch);
        }
        return len;
    }
    private int searchImageIndex(Canvas canvas, int x, int y) {
        int ret = -1;
        if (images != null) {
            int xoffset = 0;
            int yoffset = 0;
            Rectangle rec = canvas.getClientArea();
            int hGap = 10;
            int vGap = 20;
            for (int i = 0; i < images.length; i++) {
                Object obj = images[i];
                int w = 0;
                int h = 0;
                if (obj instanceof Image) {
                    Image img = (Image) obj;
                    w = img.getImageData().width;
                    h = img.getImageData().height;
                }
                else {
                    w = 240;
                    h = 320;
                }

                if (x >= stdPoint.x + xoffset && x <= stdPoint.x + xoffset + w && y >= stdPoint.y + yoffset
                        && y <= stdPoint.y + yoffset + h) {
                    ret = i;
                    break;
                }

                xoffset += w + hGap;
                if (stdPoint.x + xoffset + w + hGap > rec.x + rec.width) {
                    yoffset += h + vGap;
                    xoffset = 0;
                }
            }
        }
        return ret;
    }

    public static String getCardImagePath(Card obj, int cardid) {
        return obj.owner.baseDir.getPath() + File.separatorChar + Card.PATH_NAMES[Card.PATH_240x320] +
                + File.separatorChar + "card" + cardid + ".png";
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);

    }

    /**
     * 保存当前编辑数据。
     */
    protected void saveData() throws Exception {
        Card dataDef = (Card) editObject;

        // 读取输入：对象ID、标题、描述
        try {
        }
        catch (Exception e) {
            throw new Exception("请输入正确的ID。");
        }

        // 检查输入合法性
        DataObject dobj = EditorApplication.getInstance().getProjectData().findObject(dataDef.getClass(), dataDef.id);
        if (dobj != null && dobj != getSaveTarget()) {
            throw new Exception("ID重复，请重新输入。");
        }
        if (dataDef.title.length() == 0) {
            throw new Exception("请输入标题。");
        }

    }

    /**
     * 保存当前编辑状态成一个对象。派生类应覆盖此方法。
     */
//    protected Object saveState() {
//        try {
//            // TODO:
//            return true;
//        }
//        catch (Exception e) {
//            return null;
//        }
//    }

    @Override
    public void dispose() {
        super.dispose();
        disposed = true;
        try {
            animateThread.join();
        }
        catch (Exception e) {
        }
        FileWatcher.unwatch(this);
    }

    public void widgetDefaultSelected(SelectionEvent e) {
    }

    // 驱动动画
    public void run() {
        while (!disposed) {
            if (this.playAnimate) {
                try {
                    display.asyncExec(new Runnable() {
                        public void run() {
                            try {
                            }
                            catch (Exception e) {
                            }
                        }
                    });
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(100);
            }
            catch (Exception e) {
            }
        }
    }

    public void widgetSelected(SelectionEvent e) {
        // TODO Auto-generated method stub

    }

    public void fileModified(File f) {
        // TODO Auto-generated method stub

    }

    class FormulaLabelProvider extends LabelProvider implements ITableLabelProvider {

        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        public String getColumnText(Object element, int columnIndex) {
            Material obj = (Material)element;
            String ret = "";
            switch (columnIndex) {
                case 0: {
                    ret = Card.MATERIAL_NAMES[obj.type];
                }
                    break;
                case 1: {
                    ret = getItemName(obj.type,obj.itemId);
                }
                    break;
                case 2: {
                    ret = String.valueOf(obj.value);
                }
                    break;

            }
            return ret;
        }

    }
    
    class FormulaContentProvider implements IStructuredContentProvider{

        public Object[] getElements(Object inputElement) {
            Card card = (Card)inputElement;
            return card.materials;
        }

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
        
    }
    
    class FormulaCellModifier implements ICellModifier {

        public boolean canModify(Object element, String property) {
            return "c2".equals(property);
        }

        public Object getValue(Object element, String property) {
            if("c2".equals(property) && element instanceof Material){
                Material mt = (Material)element;
                return String.valueOf(mt.value);
            }
            return null;
        }

        public void modify(Object element, String property, Object value) {
            if("c2".equals(property) && element instanceof Material){
                Material mt = (Material)element;
                mt.value = Integer.parseInt((String)value);
            }
        }
        
    }
    
    class DropLabelProvider extends LabelProvider implements ITableLabelProvider {

        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        public String getColumnText(Object element, int columnIndex) {
            DropObject obj = (DropObject)element;
            String ret = "";
            switch (columnIndex) {
                case 0: {
                    ret = Card.DROP_NAMES[obj.type];
                }
                    break;
                case 1: {
                    ret = getDropItemName(obj.type, obj.itemId);
                }
                    break;
                case 2: {
                    ret = String.valueOf(obj.value);
                }
                    break;
                case 3: {
                    ret = String.valueOf(obj.rate);
                }
                    break;

            }
            return ret;
        }

    }
    
    class DropContentProvider implements IStructuredContentProvider{

        public Object[] getElements(Object inputElement) {
            Card card = (Card)inputElement;
            return card.dropObjects;
        }

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
        
    }
    
    class DropCellModifier implements ICellModifier {

        public boolean canModify(Object element, String property) {
            return "c2".equals(property) || "c3".equals(property);
        }

        public Object getValue(Object element, String property) {
            if("c2".equals(property) && element instanceof DropObject){
                DropObject mt = (DropObject)element;
                return String.valueOf(mt.value);
            } else if("c3".equals(property) && element instanceof DropObject){
                DropObject mt = (DropObject)element;
                return String.valueOf(mt.rate);
            }
            return null;
        }

        public void modify(Object element, String property, Object value) {
            if("c2".equals(property) && element instanceof DropObject){
                DropObject mt = (DropObject)element;
                mt.value = Integer.parseInt((String)value);
            } else if("c3".equals(property) && element instanceof DropObject){
                DropObject mt = (DropObject)element;
                mt.rate = Integer.parseInt((String)value);
            }
        }
        
    }
    
    /**
     * 获得不同材料分类的表格显示
     * @param type
     * @param id
     * @return
     */
    public static String getItemName(int type,int id){
        String ret = "";
        DataObject obj;
        switch(type){
            case Card.MATERIAL_TYPE_CARD:
                obj = EditorApplication.getProj().findCard(id);
                if(obj != null){
                    ret = obj.title;
                }
                break;
            case Card.MATERIAL_TYPE_ITEM:{
                obj = EditorApplication.getProj().findItem(id);
                if(obj != null){
                    ret = obj.title;
                }
            }
                break;
            case Card.MATERIAL_TYPE_TITLE:{
                ret = Title.toString(EditorApplication.getProj(), id);
            }
                break;
            case Card.MATERIAL_TYPE_MILITARY_RANK:{
                obj = EditorApplication.getProj().findDictObject(Rank.class, id);
                if(obj != null){
                    ret = obj.getTitle();
                }
                
            }
                break;
            default:
                ret = Card.MATERIAL_NAMES[type];
                break;
        }
        return ret;
    }
    
    /**
     * 
     * @param type
     */
    public void addNewMaterial(int type){
        Card obj = (Card)editObject;
        Material ret = obj.newMaterial();
        ret.type = type;
        ret.value = 1;
        switch(type){
            case Card.MATERIAL_TYPE_CARD:{
                ChooseCardDialog dlg = new ChooseCardDialog(display.getActiveShell());
                int cardId = -1;
                dlg.setSelectedItem(cardId);
                if (dlg.open() == Dialog.OK) {
                    cardId = dlg.getSelectedItem();
                    Card item = EditorApplication.getProj().findCard(cardId);
                    if(item != null){       
                        ret.itemId = item.id;
                    }
                } else {
                    return;
                }
            }
                break;
            case Card.MATERIAL_TYPE_ITEM:{
                ChooseItemDialog dlg = new ChooseItemDialog(display.getActiveShell());
                int itemID = -1;
                dlg.setSelectedItem(itemID);
                if (dlg.open() == Dialog.OK) {
                    itemID = dlg.getSelectedItem();
                    Item item = EditorApplication.getInstance().getProjectData().findItemOrEquipment(itemID);
                    if(item != null){       
                        ret.itemId = item.id;
                    }
                } else {
                    return;
                }
            }
                break;
            case Card.MATERIAL_TYPE_TITLE:{
                ChooseTitleDialog dlg = new ChooseTitleDialog(display.getActiveShell());
                int titleID = -1;
                dlg.setSelectedTitle(titleID);
                if (dlg.open() == Dialog.OK) {
                    titleID = dlg.getSelectedTitle();
                } else {
                    return;
                }
                if(titleID != -1){
                    ret.itemId = titleID;
                    ret.value = -1;
                }
            }
                break;
            case Card.MATERIAL_TYPE_MILITARY_RANK:{
                ChooseRankDialog dlg = new ChooseRankDialog(display.getActiveShell());
                int rank = -1;
                
                if (dlg.open() == Dialog.OK) {
                    rank = dlg.getSelectedItem();
                } else {
                    return;
                }
                if(rank != -1){
                    ret.itemId = rank;
                    ret.value = -1;
                }
            }
                break;
            default:
                break;
        }
        ret.name = getItemName(type, ret.itemId);
        //检查重复
        boolean dup = false;
        for (int i = 0; i < obj.materials.length; i++) {
            if(obj.materials[i].compareTo(ret) == 0){
                dup = true;
                break;
            }
        }
        if(dup == false){
            Material[] mts = new Material[obj.materials.length + 1];
            System.arraycopy(obj.materials, 0, mts, 0, obj.materials.length);
            mts[mts.length - 1] = ret;
            Arrays.sort(mts);
            obj.materials = mts;
            formulaViewer.refresh();
            setDirty(true);
            System.gc();
        }
    }
    
    public void deleteMaterial(){
        int index = table.getSelectionIndex();
        Material[] mts = new Material[cd.materials.length - 1];
        int newIdx = 0;
        for (int i = 0; i < cd.materials.length; i++) {
            if(i != index){
                mts[newIdx++] = cd.materials[i]; 
            }
        }
        cd.materials = mts;
        formulaViewer.refresh();
        setDirty(true);
        System.gc();
    }
    
    public void clearMaterial(){
        cd.materials = new Material[0];
        formulaViewer.refresh();
        setDirty(true);
        System.gc();
    }
    
    /**
     * 获得不同材料分类的表格显示
     * @param type
     * @param id
     * @return
     */
    public static String getDropItemName(int type,int id){
        String ret = "";
        DataObject obj;
        switch(type){
            case Card.DROP_TYPE_ITEM:{
                obj = EditorApplication.getProj().findItem(id);
                if(obj != null){
                    ret = obj.title;
                }
            }
                break;
            case Card.DROP_TYPE_TITLE:{
                ret = Title.toString(EditorApplication.getProj(), id);
            }
                break;
            case Card.DROP_TYPE_MILITARY_RANK:{
                obj = EditorApplication.getProj().findDictObject(Rank.class, id);
                if(obj != null){
                    ret = obj.getTitle();
                }
            }
                break;
            case Card.DROP_TYPE_DROP_GROUP:{
                obj = EditorApplication.getProj().findObject(DropGroup.class, id);
                if(obj != null){
                    ret = obj.getTitle();
                }
            }
                break;
            default:
                ret = Card.DROP_NAMES[type];
                break;
        }
        return ret;
    }
    
    /**
     * 
     * @param type
     */
    public void addNewDrop(int type){
        DropObject ret = cd.newDropObject();
        ret.type = type;
        ret.value = 1;
        ret.rate = 100;
        switch(type){
            case Card.DROP_TYPE_ITEM:{
                ChooseItemDialog dlg = new ChooseItemDialog(display.getActiveShell());
                int itemID = -1;
                dlg.setSelectedItem(itemID);
                if (dlg.open() == Dialog.OK) {
                    itemID = dlg.getSelectedItem();
                    Item item = EditorApplication.getInstance().getProjectData().findItemOrEquipment(itemID);
                    if(item != null){       
                        ret.itemId = item.id;
                    }
                } else {
                    return;
                }
            }
                break;
            case Card.DROP_TYPE_TITLE:{
                ChooseTitleDialog dlg = new ChooseTitleDialog(display.getActiveShell());
                int titleID = -1;
                dlg.setSelectedTitle(titleID);
                if (dlg.open() == Dialog.OK) {
                    titleID = dlg.getSelectedTitle();
                } else {
                    return;
                }
                if(titleID != -1){
                    ret.itemId = titleID;
                    ret.value = -1;
                }
            }
                break;
            case Card.DROP_TYPE_MILITARY_RANK:{
                ChooseRankDialog dlg = new ChooseRankDialog(display.getActiveShell());
                int rank = -1;
                
                if (dlg.open() == Dialog.OK) {
                    rank = dlg.getSelectedItem();
                } else {
                    return;
                }
                if(rank != -1){
                    ret.itemId = rank;
                    ret.value = -1;
                }
            }
                break;
            case Card.DROP_TYPE_DROP_GROUP:{
                ChooseDropGroupDialog2 dropDialog = new ChooseDropGroupDialog2(getSite().getShell());
                if(dropDialog.open() == IDialogConstants.OK_ID){
                    ret.itemId = dropDialog.getSelectedDropGroup();
                } else {
                    return;
                }
            }
                break;
            default:
                break;
        }
        ret.name = getDropItemName(type, ret.itemId);
        DropObject[] mts = new DropObject[cd.dropObjects.length + 1];
        System.arraycopy(cd.dropObjects, 0, mts, 0, cd.dropObjects.length);
        mts[mts.length - 1] = ret;
        cd.dropObjects = mts;
        Arrays.sort(cd.dropObjects);
        dropViewer.refresh();
        setDirty(true);
        System.gc();
    }
    
    public void deleteDrop(){
        int index = table2.getSelectionIndex();
        DropObject[] dos = new DropObject[cd.dropObjects.length - 1];
        int newIdx = 0;
        for (int i = 0; i < cd.dropObjects.length; i++) {
            if(i != index){
                dos[newIdx++] = cd.dropObjects[i]; 
            }
        }
        cd.dropObjects = dos;
        dropViewer.refresh();
        setDirty(true);
        System.gc();
    }
    
    public void clearDrop(){
        cd.dropObjects = new DropObject[0];
        dropViewer.refresh();
        setDirty(true);
        System.gc();
    }
    
}
