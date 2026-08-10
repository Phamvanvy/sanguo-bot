package com.pip.sanguo.editor.wizard;


import java.io.ObjectInputStream.GetField;
import java.util.ArrayList;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.StackLayout;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.DataObjectCategory;
import com.pip.sanguo.data.equipment.Equipment;
import com.pip.sanguo.data.equipment.EquipmentPrefix;
import com.pip.sanguo.data.item.Item;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.util.Constants;

public class NewEquipmentDialog extends Dialog {
    class ListContentProvider implements IStructuredContentProvider {
        public Object[] getElements(Object inputElement) {
            return ((java.util.List)inputElement).toArray();
        }
        public void dispose() {
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }
    
    private Composite randomComposite;
    private ComboViewer comboRandomTypeViewer;
    private Combo comboPlace;
    private Combo comboQuality;
    private Combo comboBindType;
    private Text textLevel;
    private Text textRequireLevel;
    private Text textRandomName;
    
    private EquipmentPrefix defaultPrefix = new EquipmentPrefix(EditorApplication.getProj());
    private ArrayList<EquipmentPrefix> matchedPrefixes;
    
    // 装备名称
    public String name;
    // 新建装备所属分类
    public DataObjectCategory equiType;
    // 部位
    public int place;
    // 物品等级
    public int level = 1;
    // 可装备等级
    public int requireLevel = 1;
    // 物品品质
    public int quality = Item.QUALITY_WHITE;
    // 绑定类型
    public int bindType = 1;
    // 选中前缀
    public EquipmentPrefix[] prefixes = new EquipmentPrefix[] { defaultPrefix };
    
    Button defaultPrefixButton;
    Button[] prefixButtons;
    private Group prefixGroup;
    private DataObjectCategory defaultType;
    
    /**
     * Create the dialog
     * @param parentShell
     */
    public NewEquipmentDialog(Shell parentShell, DataObjectCategory defaultType) {
        super(parentShell);
        this.defaultType = defaultType;
    }
    
    private void rebuildPrefixList() {
        if (prefixButtons != null) {
            getSelectedPrefixes();
            for (Button b : prefixButtons) {
                b.dispose();
            }
        }
        findMatchedPrefixes(level, quality);
        prefixButtons = new Button[matchedPrefixes.size()];
        for (int i = 0; i < matchedPrefixes.size(); i++) {
            prefixButtons[i] = new Button(prefixGroup, SWT.CHECK);
            prefixButtons[i].setText(matchedPrefixes.get(i).title);
            prefixButtons[i].setToolTipText(matchedPrefixes.get(i).getHintString());
        }
        updatePrefixButtons();
        prefixGroup.layout();
    }
    
    private void findMatchedPrefixes(int level, int quality) {
        java.util.List<DataObject> list = EditorApplication.getProj().getDataListByType(EquipmentPrefix.class);
        matchedPrefixes = new ArrayList<EquipmentPrefix>();
        for (DataObject p : list) {
            EquipmentPrefix pp = (EquipmentPrefix)p;
            if (level >= pp.minLevel && level <= pp.maxLevel && quality >= pp.minQuality && quality <= pp.maxQuality) {
                matchedPrefixes.add(pp);
            }
        }
    }
    
    private void getSelectedPrefixes() {
        ArrayList<EquipmentPrefix> selected = new ArrayList<EquipmentPrefix>();
        if (defaultPrefixButton.getSelection()) {
            selected.add(defaultPrefix);
        }
        for (int i = 0; i < prefixButtons.length; i++) {
            if (prefixButtons[i].getSelection()) {
                selected.add(matchedPrefixes.get(i));
            }
        }
        prefixes = new EquipmentPrefix[selected.size()];
        selected.toArray(prefixes);
    }
    
    private void updatePrefixButtons() {
        defaultPrefixButton.setSelection(false);
        for (Button b : prefixButtons) {
            b.setSelection(false);
        }
        for (EquipmentPrefix p : prefixes) {
            if (p == defaultPrefix) {
                defaultPrefixButton.setSelection(true);
            }
            for (int i = 0; i < matchedPrefixes.size(); i++) {
                if (matchedPrefixes.get(i) == p) {
                    prefixButtons[i].setSelection(true);
                }
            }
        }
    }

    /**
     * Create contents of the dialog
     * @param parent
     */
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        container.setLayout(gridLayout);
        
        randomComposite = new Composite(container, SWT.NONE);
        randomComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        final GridLayout randomLayout = new GridLayout();
        randomComposite.setLayout(randomLayout);

        final Label label_2 = new Label(randomComposite, SWT.NONE);
        label_2.setLayoutData(new GridData());
        label_2.setText("装备名称：");

        textRandomName = new Text(randomComposite, SWT.BORDER);
        textRandomName.setText("新装备");
        textRandomName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        final Label label_11 = new Label(randomComposite, SWT.NONE);
        label_11.setLayoutData(new GridData());
        label_11.setText("选择分组：");

        comboRandomTypeViewer = new ComboViewer(randomComposite, SWT.READ_ONLY);
        comboRandomTypeViewer.setContentProvider(new ListContentProvider());
        comboRandomTypeViewer.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        comboRandomTypeViewer.setInput(EditorApplication.getProj().getCategoryListByType(Equipment.class));
        if (defaultType == null) {
            comboRandomTypeViewer.getCombo().select(0);
        } else {
            comboRandomTypeViewer.setSelection(new StructuredSelection(defaultType));
        }

        final Label label_5 = new Label(randomComposite, SWT.NONE);
        label_5.setLayoutData(new GridData());
        label_5.setText("装备部位：");

        comboPlace = new Combo(randomComposite, SWT.READ_ONLY);
        comboPlace.setVisibleItemCount(20);
        comboPlace.setItems(Constants.COMBO_PLACE);
        comboPlace.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        comboPlace.select(1);

        final Label label_4 = new Label(randomComposite, SWT.NONE);
        label_4.setLayoutData(new GridData());
        label_4.setText("物品等级：");

        textLevel = new Text(randomComposite, SWT.BORDER);
        textLevel.setText(String.valueOf(level));
        textLevel.addModifyListener(new ModifyListener() {
            public void modifyText(final ModifyEvent e) {
                try {
                    level = Integer.parseInt(textLevel.getText());
                    rebuildPrefixList();
                } catch (Exception e1) {
                }
            }
        });
        final GridData gd_textLevel = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textLevel.setLayoutData(gd_textLevel);

        final Label label = new Label(randomComposite, SWT.NONE);
        label.setText("可装备等级：");

        textRequireLevel = new Text(randomComposite, SWT.BORDER);
        textRequireLevel.setText("1");
        final GridData gd_textRequireLevel = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textRequireLevel.setLayoutData(gd_textRequireLevel);

        final Label label_6 = new Label(randomComposite, SWT.NONE);
        label_6.setLayoutData(new GridData());
        label_6.setText("品质：");

        comboQuality = new Combo(randomComposite, SWT.READ_ONLY);
        comboQuality.setVisibleItemCount(10);
        comboQuality.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        comboQuality.setItems(Constants.COMBO_QUALITY);
        comboQuality.select(quality);
        comboQuality.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e) {
                onSelect(comboQuality);
            }
        });

        final Label label_1 = new Label(randomComposite, SWT.NONE);
        label_1.setText("绑定类型：");

        comboBindType = new Combo(randomComposite, SWT.READ_ONLY);
        comboBindType.select(1);
        comboBindType.setItems(new String[] {"不绑定", "装备绑定", "拾取绑定"});
        final GridData gd_comboBindType = new GridData(SWT.FILL, SWT.CENTER, true, false);
        comboBindType.setLayoutData(gd_comboBindType);

        prefixGroup = new Group(randomComposite, SWT.NONE);
        prefixGroup.setText("前缀");
        final GridData gd_group = new GridData(SWT.FILL, SWT.FILL, true, true);
        prefixGroup.setLayoutData(gd_group);
        GridLayout gl_group = new GridLayout();
        gl_group.numColumns = 5;
        prefixGroup.setLayout(gl_group);

        defaultPrefixButton = new Button(prefixGroup, SWT.CHECK);
        defaultPrefixButton.setText("无前缀");
        defaultPrefixButton.setSelection(true);
        
        rebuildPrefixList();
        
        final Button buttonSelectAll = new Button(randomComposite, SWT.NONE);
        buttonSelectAll.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                for (Button btn : prefixButtons) {
                    btn.setSelection(true);
                }
            }
        });
        buttonSelectAll.setText("选中全部");
        
        return container;
    }
    
    private void onSelect(Control source) {
        if (source == comboQuality) {
            quality = comboQuality.getSelectionIndex();
            rebuildPrefixList();
        }
    }
    
    private void getCurrentData() throws Exception{
        name = textRandomName.getText();
        if (name.length() == 0) {
            throw new Exception("装备名称不能为空！");
        }
        
        IStructuredSelection typeSelected = (IStructuredSelection)comboRandomTypeViewer.getSelection();
        //必须选择一个装备分组类型
        if (typeSelected.isEmpty()) {
            throw new Exception("请选择一个装备分组类型！");
        } else {                    
            equiType = (DataObjectCategory)typeSelected.getFirstElement();
        }
        place = comboPlace.getSelectionIndex();
        if (place == Equipment.WEAPON_UNDEFINE || place == Equipment.PROTECTOR_UNDEFINE || place == Equipment.JEWELRY_UNDEFINE || place == Equipment.HORSE_UNDEFINE) {
            throw new Exception("请选择一个具体的装备类型！");
        }
        try {
            level = Integer.parseInt(textLevel.getText());
        } catch (Exception e) {
            throw new Exception("物品等级格式错误！");
        }
        if (level < 1) {
            throw new Exception("物品等级必须大于0！");
        }
        try {
            requireLevel = Integer.parseInt(textRequireLevel.getText());
        } catch (Exception e) {
            throw new Exception("可装备等级格式错误！");
        }
        if (requireLevel < 1) {
            throw new Exception("可装备等级必须大于0！");
        }
        quality = comboQuality.getSelectionIndex();
        bindType = comboBindType.getSelectionIndex();
        
        // 至少要选择一个前缀
        getSelectedPrefixes();
        if (prefixes.length == 0) {
            throw new Exception("至少要选择一个前缀。");
        }
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
    protected Point getInitialSize() {
        return new Point(616, 643);
    }
    
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("新建装备");
    }
    
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            try {
                getCurrentData();
            } catch (Exception e) {
                MessageDialog.openInformation(getShell(), "提示：", e.getMessage());
                return;
            }
        }
        super.buttonPressed(buttonId);
    }
    
}
