package com.pip.sanguo.editor.recast;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.Title;
import com.pip.sanguo.data.Shop.BuyRequirement;
import com.pip.sanguo.data.item.Formula;
import com.pip.sanguo.data.recast.Recast;
import com.pip.sanguo.data.recast.RecastProperty;
import com.pip.sanguo.data.skill.BuffConfig;
import com.pip.sanguo.editor.DefaultDataObjectEditor;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.item.EditFormulaRequirementsDialog;
import com.pip.sanguo.editor.property.ChooseItemDialog;
import com.pip.util.AutoSelectAll;

public class RecastEditor extends DefaultDataObjectEditor {

    private Text textAreaRatio;
    private Text textArea15;
    private Text textArea14;
    private Text textArea13;
    private Text textArea12;
    private Text textArea11;
    private Text textArea10;
    private Text textArea9;
    private Text textArea8;
    private Text textArea7;
    private Text textArea6;
    private Text textArea4;
    private Text textArea3;
    private Text textArea5;
    private Text textArea2;
    private Text textArea1;
    private Text textArea0;
    private Text textArea16;
    private Text textArea17;
    private Text textMaterialNum;
    private Text textMaterial;
    private Text text_17;
    private Text text_16;
    private Text text_15;
    private Text text_14;
    private Text text_13;
    private Text text_12;
    private Text text_11;
    private Text text_10;
    private Text text_9;
    private Text text_8;
    private Text text_7;
    private Text text_6;
    private Text text_5;
    private Text text_4;
    private Text text_3;
    private Text text_2;
    private Text text_1;
    private Text text_0;
    private Text textCost;
    private Text textLevel;
    private Text textName;
    private Text textID;
    private Button button_0;
    private Button button_1;
    private Button button_2;
    private Button button_3;
    private Button button_4;
    private Button button_5;
    private Button button_6;
    private Button button_7;
    private Button button_8;
    private Button button_9;
    private Button button_10;
    private Button button_11;
    private Button button_12;
    private Button button_13;
    private Button button_14;
    private Button button_15;
    private Button button_16;
    private Button button_17;
    public static final String ID = "com.pip.sanguo.editor.recast.RecastEditor"; //$NON-NLS-1$

    /**
     * Create contents of the editor part
     * @param parent
     */
    @Override
    public void createPartControl(Composite parent) {
        System.currentTimeMillis();
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new FormLayout());

        final Label label = new Label(container, SWT.NONE);
        final FormData fd_label = new FormData();
        fd_label.bottom = new FormAttachment(0, 20);
        fd_label.top = new FormAttachment(0, 8);
        fd_label.right = new FormAttachment(0, 29);
        fd_label.left = new FormAttachment(0, 5);
        label.setLayoutData(fd_label);
        label.setText("ID：");

        textID = new Text(container, SWT.BORDER);
        final FormData fd_textID = new FormData();
        fd_textID.bottom = new FormAttachment(0, 23);
        fd_textID.top = new FormAttachment(0, 5);
        fd_textID.right = new FormAttachment(0, 260);
        fd_textID.left = new FormAttachment(0, 97);
        textID.setLayoutData(fd_textID);
        textID.addFocusListener(AutoSelectAll.instance);
        textID.addModifyListener(this);

        final Label label_1 = new Label(container, SWT.NONE);
        final FormData fd_label_1 = new FormData();
        fd_label_1.bottom = new FormAttachment(0, 20);
        fd_label_1.top = new FormAttachment(0, 8);
        fd_label_1.right = new FormAttachment(0, 308);
        fd_label_1.left = new FormAttachment(0, 272);
        label_1.setLayoutData(fd_label_1);
        label_1.setText("名称：");

        textName = new Text(container, SWT.BORDER);
        final FormData fd_textName = new FormData();
        fd_textName.bottom = new FormAttachment(0, 23);
        fd_textName.top = new FormAttachment(0, 5);
        fd_textName.right = new FormAttachment(0, 484);
        fd_textName.left = new FormAttachment(0, 337);
        textName.setLayoutData(fd_textName);
        textName.addFocusListener(AutoSelectAll.instance);
        textName.addModifyListener(this);

        final Label label_3 = new Label(container, SWT.NONE);
        final FormData fd_label_3 = new FormData();
        fd_label_3.bottom = new FormAttachment(0, 43);
        fd_label_3.top = new FormAttachment(0, 31);
        fd_label_3.right = new FormAttachment(0, 89);
        fd_label_3.left = new FormAttachment(0, 5);
        label_3.setLayoutData(fd_label_3);
        label_3.setText("提升物品等级：");

        textLevel = new Text(container, SWT.BORDER);
        final FormData fd_textLevel = new FormData();
        fd_textLevel.bottom = new FormAttachment(0, 47);
        fd_textLevel.top = new FormAttachment(0, 29);
        fd_textLevel.right = new FormAttachment(0, 260);
        fd_textLevel.left = new FormAttachment(0, 97);
        textLevel.setLayoutData(fd_textLevel);
        textLevel.setText("0");
        textLevel.addFocusListener(AutoSelectAll.instance);
        textLevel.addModifyListener(this);

        final Label label_4 = new Label(container, SWT.NONE);
        final FormData fd_label_4 = new FormData();
        fd_label_4.bottom = new FormAttachment(0, 43);
        fd_label_4.top = new FormAttachment(0, 31);
        fd_label_4.right = new FormAttachment(0, 332);
        fd_label_4.left = new FormAttachment(0, 272);
        label_4.setLayoutData(fd_label_4);
        label_4.setText("基础花费：");

        textCost = new Text(container, SWT.BORDER);
        final FormData fd_textCost = new FormData();
        fd_textCost.bottom = new FormAttachment(0, 46);
        fd_textCost.top = new FormAttachment(0, 28);
        fd_textCost.right = new FormAttachment(0, 484);
        fd_textCost.left = new FormAttachment(0, 337);
        textCost.setLayoutData(fd_textCost);
        textCost.setText("0");
        textCost.addFocusListener(AutoSelectAll.instance);
        textCost.addModifyListener(this);

        final Group group_0 = new Group(container, SWT.NONE);
        final FormData fd_group_0 = new FormData();
        fd_group_0.bottom = new FormAttachment(0, 143);
        fd_group_0.top = new FormAttachment(0, 100);
        fd_group_0.right = new FormAttachment(0, 244);
        fd_group_0.left = new FormAttachment(0, 5);
        group_0.setLayoutData(fd_group_0);
        final GridLayout gridLayout_1 = new GridLayout();
        gridLayout_1.numColumns = 3;
        group_0.setLayout(gridLayout_1);
        group_0.setText("0-力量");

        button_0 = new Button(group_0, SWT.CHECK);
        button_0.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                setDirty(true);
            }
        });
        button_0.setText("百分比数值");

        text_0 = new Text(group_0, SWT.BORDER);
        final GridData gd_text_0 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd_text_0.widthHint = 46;
        text_0.setLayoutData(gd_text_0);
        text_0.setText("0");
        text_0.addFocusListener(AutoSelectAll.instance);
        text_0.addModifyListener(this);

        textArea0 = new Text(group_0, SWT.BORDER);
        final GridData gd_textArea0 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textArea0.setLayoutData(gd_textArea0);
        textArea0.addFocusListener(AutoSelectAll.instance);
        textArea0.addModifyListener(this);

        final Group group_1 = new Group(container, SWT.NONE);
        final FormData fd_group_1 = new FormData();
        fd_group_1.bottom = new FormAttachment(0, 143);
        fd_group_1.top = new FormAttachment(0, 100);
        fd_group_1.right = new FormAttachment(0, 488);
        fd_group_1.left = new FormAttachment(0, 250);
        group_1.setLayoutData(fd_group_1);
        group_1.setText("1-敏捷");
        final GridLayout gridLayout_2 = new GridLayout();
        gridLayout_2.numColumns = 3;
        group_1.setLayout(gridLayout_2);

        button_1 = new Button(group_1, SWT.CHECK);
        button_1.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                setDirty(true);
            }
        });
        button_1.setText("百分比数值");

        text_1 = new Text(group_1, SWT.BORDER);
        text_1.setText("0");
        final GridData gd_text_1 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd_text_1.widthHint = 46;
        text_1.setLayoutData(gd_text_1);
        text_1.addFocusListener(AutoSelectAll.instance);
        text_1.addModifyListener(this);

        textArea1 = new Text(group_1, SWT.BORDER);
        final GridData gd_textArea1 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textArea1.setLayoutData(gd_textArea1);
        textArea1.addFocusListener(AutoSelectAll.instance);
        textArea1.addModifyListener(this);

        final Group group_2 = new Group(container, SWT.NONE);
        final FormData fd_group_2 = new FormData();
        fd_group_2.bottom = new FormAttachment(0, 193);
        fd_group_2.top = new FormAttachment(0, 150);
        fd_group_2.right = new FormAttachment(0, 244);
        fd_group_2.left = new FormAttachment(0, 5);
        group_2.setLayoutData(fd_group_2);
        group_2.setText("2-体力");
        final GridLayout gridLayout_3 = new GridLayout();
        gridLayout_3.numColumns = 3;
        group_2.setLayout(gridLayout_3);

        button_2 = new Button(group_2, SWT.CHECK);
        button_2.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                setDirty(true);
            }
        });
        button_2.setText("百分比数值");

        text_2 = new Text(group_2, SWT.BORDER);
        text_2.setText("0");
        final GridData gd_text_2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd_text_2.widthHint = 46;
        text_2.setLayoutData(gd_text_2);
        text_2.addFocusListener(AutoSelectAll.instance);
        text_2.addModifyListener(this);

        textArea2 = new Text(group_2, SWT.BORDER);
        final GridData gd_textArea2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textArea2.setLayoutData(gd_textArea2);
        textArea2.addFocusListener(AutoSelectAll.instance);
        textArea2.addModifyListener(this);

        final Group group_3 = new Group(container, SWT.NONE);
        final FormData fd_group_3 = new FormData();
        fd_group_3.bottom = new FormAttachment(0, 193);
        fd_group_3.top = new FormAttachment(0, 150);
        fd_group_3.right = new FormAttachment(0, 488);
        fd_group_3.left = new FormAttachment(0, 250);
        group_3.setLayoutData(fd_group_3);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        group_3.setLayout(gridLayout);
        group_3.setText("3-智力");

        button_3 = new Button(group_3, SWT.CHECK);
        button_3.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                setDirty(true);
            }
        });
        button_3.setText("百分比数值");

        text_3 = new Text(group_3, SWT.BORDER);
        text_3.setText("0");
        final GridData gd_text_3 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd_text_3.widthHint = 46;
        text_3.setLayoutData(gd_text_3);
        text_3.addFocusListener(AutoSelectAll.instance);
        text_3.addModifyListener(this);

        textArea3 = new Text(group_3, SWT.BORDER);
        final GridData gd_textArea3 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textArea3.setLayoutData(gd_textArea3);
        textArea3.addFocusListener(AutoSelectAll.instance);
        textArea3.addModifyListener(this);

        final Group group_4 = new Group(container, SWT.NONE);
        final FormData fd_group_4 = new FormData();
        fd_group_4.bottom = new FormAttachment(0, 242);
        fd_group_4.top = new FormAttachment(0, 199);
        fd_group_4.right = new FormAttachment(0, 244);
        fd_group_4.left = new FormAttachment(0, 5);
        group_4.setLayoutData(fd_group_4);
        final GridLayout gridLayout_4 = new GridLayout();
        gridLayout_4.numColumns = 3;
        group_4.setLayout(gridLayout_4);
        group_4.setText("4-生命");

        button_4 = new Button(group_4, SWT.CHECK);
        button_4.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                setDirty(true);
            }
        });
        button_4.setText("百分比数值");

        text_4 = new Text(group_4, SWT.BORDER);
        text_4.setText("0");
        final GridData gd_text_4 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd_text_4.widthHint = 46;
        text_4.setLayoutData(gd_text_4);
        text_4.addFocusListener(AutoSelectAll.instance);
        text_4.addModifyListener(this);

        textArea4 = new Text(group_4, SWT.BORDER);
        final GridData gd_textArea4 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textArea4.setLayoutData(gd_textArea4);
        textArea4.addFocusListener(AutoSelectAll.instance);
        textArea4.addModifyListener(this);

        final Group group_5 = new Group(container, SWT.NONE);
        final FormData fd_group_5 = new FormData();
        fd_group_5.bottom = new FormAttachment(0, 242);
        fd_group_5.top = new FormAttachment(0, 199);
        fd_group_5.right = new FormAttachment(0, 488);
        fd_group_5.left = new FormAttachment(0, 250);
        group_5.setLayoutData(fd_group_5);
        final GridLayout gridLayout_5 = new GridLayout();
        gridLayout_5.numColumns = 3;
        group_5.setLayout(gridLayout_5);
        group_5.setText("5-精力");

        button_5 = new Button(group_5, SWT.CHECK);
        button_5.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                setDirty(true);
            }
        });
        button_5.setText("百分比数值");

        text_5 = new Text(group_5, SWT.BORDER);
        text_5.setText("0");
        final GridData gd_text_5 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd_text_5.widthHint = 46;
        text_5.setLayoutData(gd_text_5);
        text_5.addFocusListener(AutoSelectAll.instance);
        text_5.addModifyListener(this);

        textArea5 = new Text(group_5, SWT.BORDER);
        final GridData gd_textArea5 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textArea5.setLayoutData(gd_textArea5);
        textArea5.addFocusListener(AutoSelectAll.instance);
        textArea5.addModifyListener(this);

        final Group group_6 = new Group(container, SWT.NONE);
        final FormData fd_group_6 = new FormData();
        fd_group_6.bottom = new FormAttachment(0, 291);
        fd_group_6.top = new FormAttachment(0, 248);
        fd_group_6.right = new FormAttachment(0, 244);
        fd_group_6.left = new FormAttachment(0, 5);
        group_6.setLayoutData(fd_group_6);
        final GridLayout gridLayout_6 = new GridLayout();
        gridLayout_6.numColumns = 3;
        group_6.setLayout(gridLayout_6);
        group_6.setText("6-暴击");

        button_6 = new Button(group_6, SWT.CHECK);
        button_6.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                setDirty(true);
            }
        });
        button_6.setText("百分比数值");

        text_6 = new Text(group_6, SWT.BORDER);
        text_6.setText("0");
        final GridData gd_text_6 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd_text_6.widthHint = 46;
        text_6.setLayoutData(gd_text_6);
        text_6.addFocusListener(AutoSelectAll.instance);
        text_6.addModifyListener(this);

        textArea6 = new Text(group_6, SWT.BORDER);
        final GridData gd_textArea6 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textArea6.setLayoutData(gd_textArea6);
        textArea6.addFocusListener(AutoSelectAll.instance);
        textArea6.addModifyListener(this);

        final Group group_7 = new Group(container, SWT.NONE);
        final FormData fd_group_7 = new FormData();
        fd_group_7.bottom = new FormAttachment(0, 291);
        fd_group_7.top = new FormAttachment(0, 248);
        fd_group_7.right = new FormAttachment(0, 488);
        fd_group_7.left = new FormAttachment(0, 250);
        group_7.setLayoutData(fd_group_7);
        final GridLayout gridLayout_7 = new GridLayout();
        gridLayout_7.numColumns = 3;
        group_7.setLayout(gridLayout_7);
        group_7.setText("7-命中");

        button_7 = new Button(group_7, SWT.CHECK);
        button_7.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                setDirty(true);
            }
        });
        button_7.setText("百分比数值");

        text_7 = new Text(group_7, SWT.BORDER);
        text_7.setText("0");
        final GridData gd_text_4_1_4 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd_text_4_1_4.widthHint = 46;
        text_7.setLayoutData(gd_text_4_1_4);
        text_7.addFocusListener(AutoSelectAll.instance);
        text_7.addModifyListener(this);

        textArea7 = new Text(group_7, SWT.BORDER);
        final GridData gd_textArea7 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textArea7.setLayoutData(gd_textArea7);
        textArea7.addFocusListener(AutoSelectAll.instance);
        textArea7.addModifyListener(this);

        final Group group_8 = new Group(container, SWT.NONE);
        final FormData fd_group_8 = new FormData();
        fd_group_8.bottom = new FormAttachment(0, 340);
        fd_group_8.top = new FormAttachment(0, 297);
        fd_group_8.right = new FormAttachment(0, 244);
        fd_group_8.left = new FormAttachment(0, 5);
        group_8.setLayoutData(fd_group_8);
        final GridLayout gridLayout_8 = new GridLayout();
        gridLayout_8.numColumns = 3;
        group_8.setLayout(gridLayout_8);
        group_8.setText("8-物闪");

        button_8 = new Button(group_8, SWT.CHECK);
        button_8.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                setDirty(true);
            }
        });
        button_8.setText("百分比数值");

        text_8 = new Text(group_8, SWT.BORDER);
        text_8.setText("0");
        final GridData gd_text_8 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd_text_8.widthHint = 46;
        text_8.setLayoutData(gd_text_8);
        text_8.addFocusListener(AutoSelectAll.instance);
        text_8.addModifyListener(this);

        textArea8 = new Text(group_8, SWT.BORDER);
        final GridData gd_textArea8 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textArea8.setLayoutData(gd_textArea8);
        textArea8.addFocusListener(AutoSelectAll.instance);
        textArea8.addModifyListener(this);

        final Group group_9 = new Group(container, SWT.NONE);
        final FormData fd_group_9 = new FormData();
        fd_group_9.bottom = new FormAttachment(0, 340);
        fd_group_9.top = new FormAttachment(0, 297);
        fd_group_9.right = new FormAttachment(0, 488);
        fd_group_9.left = new FormAttachment(0, 250);
        group_9.setLayoutData(fd_group_9);
        final GridLayout gridLayout_9 = new GridLayout();
        gridLayout_9.numColumns = 3;
        group_9.setLayout(gridLayout_9);
        group_9.setText("9-法闪");

        button_9 = new Button(group_9, SWT.CHECK);
        button_9.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                setDirty(true);
            }
        });
        button_9.setText("百分比数值");

        text_9 = new Text(group_9, SWT.BORDER);
        text_9.setText("0");
        final GridData gd_text_9 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd_text_9.widthHint = 46;
        text_9.setLayoutData(gd_text_9);
        text_9.addFocusListener(AutoSelectAll.instance);
        text_9.addModifyListener(this);

        textArea9 = new Text(group_9, SWT.BORDER);
        final GridData gd_textArea9 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textArea9.setLayoutData(gd_textArea9);
        textArea9.addFocusListener(AutoSelectAll.instance);
        textArea9.addModifyListener(this);

        final Group group_10 = new Group(container, SWT.NONE);
        final FormData fd_group_10 = new FormData();
        fd_group_10.bottom = new FormAttachment(0, 389);
        fd_group_10.top = new FormAttachment(0, 346);
        fd_group_10.right = new FormAttachment(0, 244);
        fd_group_10.left = new FormAttachment(0, 5);
        group_10.setLayoutData(fd_group_10);
        final GridLayout gridLayout_10 = new GridLayout();
        gridLayout_10.numColumns = 3;
        group_10.setLayout(gridLayout_10);
        group_10.setText("10-物攻");

        button_10 = new Button(group_10, SWT.CHECK);
        button_10.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                setDirty(true);
            }
        });
        button_10.setText("百分比数值");

        text_10 = new Text(group_10, SWT.BORDER);
        text_10.setText("0");
        final GridData gd_text_10 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd_text_10.widthHint = 46;
        text_10.setLayoutData(gd_text_10);
        text_10.addFocusListener(AutoSelectAll.instance);
        text_10.addModifyListener(this);

        textArea10 = new Text(group_10, SWT.BORDER);
        final GridData gd_textArea10 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textArea10.setLayoutData(gd_textArea10);
        textArea10.addFocusListener(AutoSelectAll.instance);
        textArea10.addModifyListener(this);

        final Group group_11 = new Group(container, SWT.NONE);
        final FormData fd_group_11 = new FormData();
        fd_group_11.bottom = new FormAttachment(0, 389);
        fd_group_11.top = new FormAttachment(0, 346);
        fd_group_11.right = new FormAttachment(0, 488);
        fd_group_11.left = new FormAttachment(0, 250);
        group_11.setLayoutData(fd_group_11);
        final GridLayout gridLayout_11 = new GridLayout();
        gridLayout_11.numColumns = 3;
        group_11.setLayout(gridLayout_11);
        group_11.setText("11-法攻");

        button_11 = new Button(group_11, SWT.CHECK);
        button_11.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                setDirty(true);
            }
        });
        button_11.setText("百分比数值");

        text_11 = new Text(group_11, SWT.BORDER);
        text_11.setText("0");
        final GridData gd_text_11 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd_text_11.widthHint = 46;
        text_11.setLayoutData(gd_text_11);
        text_11.addFocusListener(AutoSelectAll.instance);
        text_11.addModifyListener(this);

        textArea11 = new Text(group_11, SWT.BORDER);
        final GridData gd_textArea11 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textArea11.setLayoutData(gd_textArea11);
        textArea11.addFocusListener(AutoSelectAll.instance);
        textArea11.addModifyListener(this);

        final Group group_12 = new Group(container, SWT.NONE);
        final FormData fd_group_12 = new FormData();
        fd_group_12.bottom = new FormAttachment(0, 438);
        fd_group_12.top = new FormAttachment(0, 395);
        fd_group_12.right = new FormAttachment(0, 244);
        fd_group_12.left = new FormAttachment(0, 5);
        group_12.setLayoutData(fd_group_12);
        final GridLayout gridLayout_12 = new GridLayout();
        gridLayout_12.numColumns = 3;
        group_12.setLayout(gridLayout_12);
        group_12.setText("12-护甲");

        button_12 = new Button(group_12, SWT.CHECK);
        button_12.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                setDirty(true);
            }
        });
        button_12.setText("百分比数值");

        text_12 = new Text(group_12, SWT.BORDER);
        text_12.setText("0");
        final GridData gd_text_12 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd_text_12.widthHint = 46;
        text_12.setLayoutData(gd_text_12);
        text_12.addFocusListener(AutoSelectAll.instance);
        text_12.addModifyListener(this);

        textArea12 = new Text(group_12, SWT.BORDER);
        final GridData gd_textArea12 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textArea12.setLayoutData(gd_textArea12);
        textArea12.addFocusListener(AutoSelectAll.instance);
        textArea12.addModifyListener(this);

        final Group group_13 = new Group(container, SWT.NONE);
        final FormData fd_group_13 = new FormData();
        fd_group_13.bottom = new FormAttachment(0, 438);
        fd_group_13.top = new FormAttachment(0, 395);
        fd_group_13.right = new FormAttachment(0, 488);
        fd_group_13.left = new FormAttachment(0, 250);
        group_13.setLayoutData(fd_group_13);
        final GridLayout gridLayout_13 = new GridLayout();
        gridLayout_13.numColumns = 3;
        group_13.setLayout(gridLayout_13);
        group_13.setText("13-法防");

        button_13 = new Button(group_13, SWT.CHECK);
        button_13.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                setDirty(true);
            }
        });
        button_13.setText("百分比数值");

        text_13 = new Text(group_13, SWT.BORDER);
        text_13.setText("0");
        final GridData gd_text_13 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd_text_13.widthHint = 46;
        text_13.setLayoutData(gd_text_13);
        text_13.addFocusListener(AutoSelectAll.instance);
        text_13.addModifyListener(this);

        textArea13 = new Text(group_13, SWT.BORDER);
        final GridData gd_textArea13 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textArea13.setLayoutData(gd_textArea13);
        textArea13.addFocusListener(AutoSelectAll.instance);
        textArea13.addModifyListener(this);

        final Group group_14 = new Group(container, SWT.NONE);
        final FormData fd_group_14 = new FormData();
        fd_group_14.bottom = new FormAttachment(0, 487);
        fd_group_14.top = new FormAttachment(0, 444);
        fd_group_14.right = new FormAttachment(0, 244);
        fd_group_14.left = new FormAttachment(0, 5);
        group_14.setLayoutData(fd_group_14);
        final GridLayout gridLayout_14 = new GridLayout();
        gridLayout_14.numColumns = 3;
        group_14.setLayout(gridLayout_14);
        group_14.setText("14-回血");

        button_14 = new Button(group_14, SWT.CHECK);
        button_14.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                setDirty(true);
            }
        });
        button_14.setText("百分比数值");

        text_14 = new Text(group_14, SWT.BORDER);
        text_14.setText("0");
        final GridData gd_text_14 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd_text_14.widthHint = 46;
        text_14.setLayoutData(gd_text_14);
        text_14.addFocusListener(AutoSelectAll.instance);
        text_14.addModifyListener(this);

        textArea14 = new Text(group_14, SWT.BORDER);
        final GridData gd_textArea14 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textArea14.setLayoutData(gd_textArea14);
        textArea14.addFocusListener(AutoSelectAll.instance);
        textArea14.addModifyListener(this);

        final Group group_15 = new Group(container, SWT.NONE);
        final FormData fd_group_15 = new FormData();
        fd_group_15.bottom = new FormAttachment(0, 487);
        fd_group_15.top = new FormAttachment(0, 444);
        fd_group_15.right = new FormAttachment(0, 488);
        fd_group_15.left = new FormAttachment(0, 250);
        group_15.setLayoutData(fd_group_15);
        final GridLayout gridLayout_15 = new GridLayout();
        gridLayout_15.numColumns = 3;
        group_15.setLayout(gridLayout_15);
        group_15.setText("15-回气");

        button_15 = new Button(group_15, SWT.CHECK);
        button_15.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                setDirty(true);
            }
        });
        button_15.setText("百分比数值");

        text_15 = new Text(group_15, SWT.BORDER);
        text_15.setText("0");
        final GridData gd_text_15 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd_text_15.widthHint = 46;
        text_15.setLayoutData(gd_text_15);
        text_15.addFocusListener(AutoSelectAll.instance);
        text_15.addModifyListener(this);

        textArea15 = new Text(group_15, SWT.BORDER);
        final GridData gd_textArea15 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textArea15.setLayoutData(gd_textArea15);
        textArea15.addFocusListener(AutoSelectAll.instance);
        textArea15.addModifyListener(this);

        final Group group_16 = new Group(container, SWT.NONE);
        final FormData fd_group_16 = new FormData();
        fd_group_16.bottom = new FormAttachment(0, 536);
        fd_group_16.top = new FormAttachment(0, 493);
        fd_group_16.right = new FormAttachment(0, 245);
        fd_group_16.left = new FormAttachment(0, 5);
        group_16.setLayoutData(fd_group_16);
        final GridLayout gridLayout_16 = new GridLayout();
        gridLayout_16.numColumns = 3;
        group_16.setLayout(gridLayout_16);
        group_16.setText("16-速度");

        button_16 = new Button(group_16, SWT.CHECK);
        button_16.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                setDirty(true);
            }
        });
        button_16.setText("百分比数值");

        text_16 = new Text(group_16, SWT.BORDER);
        text_16.setText("0");
        final GridData gd_text_16 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd_text_16.widthHint = 46;
        text_16.setLayoutData(gd_text_16);
        text_16.addFocusListener(AutoSelectAll.instance);
        text_16.addModifyListener(this);

        textArea16 = new Text(group_16, SWT.BORDER);
        final GridData gd_textArea16 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textArea16.setLayoutData(gd_textArea16);
        textArea16.addFocusListener(AutoSelectAll.instance);
        textArea16.addModifyListener(this);
        
        Group group_17;
        group_17 = new Group(container, SWT.NONE);
        final FormData fd_group_17 = new FormData();
        fd_group_17.bottom = new FormAttachment(0, 536);
        fd_group_17.top = new FormAttachment(0, 493);
        fd_group_17.right = new FormAttachment(0, 488);
        fd_group_17.left = new FormAttachment(0, 250);
        group_17.setLayoutData(fd_group_17);
        final GridLayout gridLayout_17 = new GridLayout();
        gridLayout_17.numColumns = 3;
        group_17.setLayout(gridLayout_17);
        group_17.setText("17-免暴");

        button_17 = new Button(group_17, SWT.CHECK);
        button_17.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                setDirty(true);
            }
        });
        button_17.setText("百分比数值");

        text_17 = new Text(group_17, SWT.BORDER);
        final GridData gd_text_17 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd_text_17.widthHint = 46;
        text_17.setLayoutData(gd_text_17);
        text_17.setText("0");
        text_17.addFocusListener(AutoSelectAll.instance);
        text_17.addModifyListener(this);

        textArea17 = new Text(group_17, SWT.BORDER);
        final GridData gd_textArea17 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textArea17.setLayoutData(gd_textArea17);
        textArea17.addFocusListener(AutoSelectAll.instance);
        textArea17.addModifyListener(this);

        final Label label_2 = new Label(container, SWT.NONE);
        final FormData fd_label_2 = new FormData();
        fd_label_2.bottom = new FormAttachment(0, 67);
        fd_label_2.top = new FormAttachment(0, 55);
        fd_label_2.right = new FormAttachment(0, 89);
        fd_label_2.left = new FormAttachment(0, 5);
        label_2.setLayoutData(fd_label_2);
        label_2.setText("消耗材料：");

        textMaterial = new Text(container, SWT.BORDER);
        final FormData fd_textMaterial = new FormData();
        fd_textMaterial.bottom = new FormAttachment(0, 71);
        fd_textMaterial.top = new FormAttachment(0, 53);
        fd_textMaterial.right = new FormAttachment(0, 213);
        fd_textMaterial.left = new FormAttachment(0, 97);
        textMaterial.setLayoutData(fd_textMaterial);

        Button buttonMaterial;
        buttonMaterial = new Button(container, SWT.NONE);
        final FormData fd_buttonMaterial = new FormData();
        fd_buttonMaterial.bottom = new FormAttachment(0, 72);
        fd_buttonMaterial.top = new FormAttachment(0, 50);
        fd_buttonMaterial.right = new FormAttachment(0, 262);
        fd_buttonMaterial.left = new FormAttachment(0, 214);
        buttonMaterial.setLayoutData(fd_buttonMaterial);
        buttonMaterial.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                ChooseItemDialog dlg = new ChooseItemDialog(getSite().getShell());
                Recast dataDef = (Recast)editObject;
                dlg.setSelectedItem(dataDef.itemId);
                if (dlg.open() == ChooseItemDialog.OK) {
                    dataDef.itemId = dlg.getSelectedItem();
                    textMaterial.setText(dataDef.owner.findItemOrEquipment(dataDef.itemId).toString());
                    setDirty(true);
                }
            }
        });
        buttonMaterial.setText("选择");

        final Label label_5 = new Label(container, SWT.NONE);
        final FormData fd_label_5 = new FormData();
        fd_label_5.bottom = new FormAttachment(0, 67);
        fd_label_5.top = new FormAttachment(0, 55);
        fd_label_5.right = new FormAttachment(0, 332);
        fd_label_5.left = new FormAttachment(0, 272);
        label_5.setLayoutData(fd_label_5);
        label_5.setText("材料数量：");

        textMaterialNum = new Text(container, SWT.BORDER);
        final FormData fd_textMaterialNum = new FormData();
        fd_textMaterialNum.bottom = new FormAttachment(0, 72);
        fd_textMaterialNum.top = new FormAttachment(0, 54);
        fd_textMaterialNum.right = new FormAttachment(0, 484);
        fd_textMaterialNum.left = new FormAttachment(0, 337);
        textMaterialNum.setLayoutData(fd_textMaterialNum);
        textMaterialNum.setText("0");
        textMaterialNum.addFocusListener(AutoSelectAll.instance);
        textMaterialNum.addModifyListener(this);

        final Label label_6 = new Label(container, SWT.NONE);
        final FormData fd_label_6 = new FormData();
        fd_label_6.bottom = new FormAttachment(0, 92);
        fd_label_6.top = new FormAttachment(0, 80);
        fd_label_6.right = new FormAttachment(0, 89);
        fd_label_6.left = new FormAttachment(0, 5);
        label_6.setLayoutData(fd_label_6);
        label_6.setText("区间比率：");

        textAreaRatio = new Text(container, SWT.BORDER);
        final FormData fd_textAreaRatio = new FormData();
        fd_textAreaRatio.bottom = new FormAttachment(0, 95);
        fd_textAreaRatio.top = new FormAttachment(0, 77);
        fd_textAreaRatio.right = new FormAttachment(0, 260);
        fd_textAreaRatio.left = new FormAttachment(0, 97);
        textAreaRatio.setLayoutData(fd_textAreaRatio);
        textAreaRatio.addFocusListener(AutoSelectAll.instance);
        textAreaRatio.addModifyListener(this);
        
        // 设置初始值
        updateView();
        
        setDirty(false);
        setPartName(this.getEditorInput().getName());
        saveStateToUndoBuffer();
    }
    
    /**
     * 设置初始值
     */
    private void updateView() {
        Recast dataDef = (Recast)editObject;
        textID.setText(String.valueOf(dataDef.id));
        textName.setText(dataDef.title);
        textLevel.setText(String.valueOf(dataDef.level));
        textCost.setText(String.valueOf(dataDef.price));
        if (dataDef.itemId != -1) {
            textMaterial.setText(dataDef.owner.findItemOrEquipment(dataDef.itemId).toString());
        }
        textMaterialNum.setText(String.valueOf(dataDef.itemNum));
        textAreaRatio.setText(dataDef.getAreaRatioText());
        for (RecastProperty property : dataDef.propertys) {
            boolean percent = property.isPercentValue == 1 ? true : false;
            String value = String.valueOf(property.value);
            switch (property.type) {
                case 0:
                    button_0.setSelection(percent);
                    text_0.setText(value);
                    textArea0.setText(property.getAreaText());
                    break;
                case 1:
                    button_1.setSelection(percent);
                    text_1.setText(value);
                    textArea1.setText(property.getAreaText());
                    break;
                case 2:
                    button_2.setSelection(percent);
                    text_2.setText(value);
                    textArea2.setText(property.getAreaText());
                    break;
                case 3:
                    button_3.setSelection(percent);
                    text_3.setText(value);
                    textArea3.setText(property.getAreaText());
                    break;
                case 4:
                    button_4.setSelection(percent);
                    text_4.setText(value);
                    textArea4.setText(property.getAreaText());
                    break;
                case 5:
                    button_5.setSelection(percent);
                    text_5.setText(value);
                    textArea5.setText(property.getAreaText());
                    break;
                case 6:
                    button_6.setSelection(percent);
                    text_6.setText(value);
                    textArea6.setText(property.getAreaText());
                    break;
                case 7:
                    button_7.setSelection(percent);
                    text_7.setText(value);
                    textArea7.setText(property.getAreaText());
                    break;
                case 8:
                    button_8.setSelection(percent);
                    text_8.setText(value);
                    textArea8.setText(property.getAreaText());
                    break;
                case 9:
                    button_9.setSelection(percent);
                    text_9.setText(value);
                    textArea9.setText(property.getAreaText());
                    break;
                case 10:
                    button_10.setSelection(percent);
                    text_10.setText(value);
                    textArea10.setText(property.getAreaText());
                    break;
                case 11:
                    button_11.setSelection(percent);
                    text_11.setText(value);
                    textArea11.setText(property.getAreaText());
                    break;
                case 12:
                    button_12.setSelection(percent);
                    text_12.setText(value);
                    textArea12.setText(property.getAreaText());
                    break;
                case 13:
                    button_13.setSelection(percent);
                    text_13.setText(value);
                    textArea13.setText(property.getAreaText());
                    break;
                case 14:
                    button_14.setSelection(percent);
                    text_14.setText(value);
                    textArea14.setText(property.getAreaText());
                    break;
                case 15:
                    button_15.setSelection(percent);
                    text_15.setText(value);
                    textArea15.setText(property.getAreaText());
                    break;
                case 16:
                    button_16.setSelection(percent);
                    text_16.setText(value);
                    textArea16.setText(property.getAreaText());
                    break;
                case 17:
                    button_17.setSelection(percent);
                    text_17.setText(value);
                    textArea17.setText(property.getAreaText());
                    break;
            }
        }
    }

    /**
     * 保存当前编辑数据。
     */
    protected void saveData() throws Exception {
        Recast dataDef = (Recast)editObject;
        dataDef.propertys.clear();
        try {
            dataDef.id = Integer.parseInt(textID.getText());
        } catch (Exception e) {
            throw new Exception("请输入正确的ID。");
        }
        dataDef.title = textName.getText().trim();
        try {
            dataDef.level = Integer.parseInt(textLevel.getText());
        } catch (Exception e) {
            throw new Exception("请输入正确的提升等级。");
        }
        try {
            dataDef.price = Integer.parseInt(textCost.getText());
        } catch (Exception e) {
            throw new Exception("请输入正确的基础花费。");
        }
        try {
            dataDef.itemNum = Integer.parseInt(textMaterialNum.getText());
        } catch (Exception e) {
            throw new Exception("请输入正确的材料数量。");
        }
        String ratio = textAreaRatio.getText();
        if (ratio != null && ratio.length() > 0) {
            dataDef.setAreaRatio(ratio);
        }
        
        // 重铸性格
        try {
            boolean percent = button_0.getSelection();
            int value = Integer.parseInt(text_0.getText());
            String arra = textArea0.getText();
            addProperty(dataDef, 0, percent, value, arra);
        } catch (Exception e) {
            throw new Exception("请输入正确的力量值。");
        }
        
        try {
            boolean percent = button_1.getSelection();
            int value = Integer.parseInt(text_1.getText());
            String arra = textArea1.getText();
            addProperty(dataDef, 1, percent, value, arra);
        } catch (Exception e) {
            throw new Exception("请输入正确的敏捷值。");
        }
        
        try {
            boolean percent = button_2.getSelection();
            int value = Integer.parseInt(text_2.getText());
            String arra = textArea2.getText();
            addProperty(dataDef, 2, percent, value, arra);
        } catch (Exception e) {
            throw new Exception("请输入正确的体力值。");
        }
        
        try {
            boolean percent = button_3.getSelection();
            int value = Integer.parseInt(text_3.getText());
            String arra = textArea3.getText();
            addProperty(dataDef, 3, percent, value, arra);
        } catch (Exception e) {
            throw new Exception("请输入正确的智力值。");
        }
        
        try {
            boolean percent = button_4.getSelection();
            int value = Integer.parseInt(text_4.getText());
            String arra = textArea4.getText();
            addProperty(dataDef, 4, percent, value, arra);
        } catch (Exception e) {
            throw new Exception("请输入正确的生命值。");
        }
        
        try {
            boolean percent = button_5.getSelection();
            int value = Integer.parseInt(text_5.getText());
            String arra = textArea5.getText();
            addProperty(dataDef, 5, percent, value, arra);
        } catch (Exception e) {
            throw new Exception("请输入正确的精力值。");
        }
        
        try {
            boolean percent = button_6.getSelection();
            int value = Integer.parseInt(text_6.getText());
            String arra = textArea6.getText();
            addProperty(dataDef, 6, percent, value, arra);
        } catch (Exception e) {
            throw new Exception("请输入正确的暴击值。");
        }
        
        try {
            boolean percent = button_7.getSelection();
            int value = Integer.parseInt(text_7.getText());
            String arra = textArea7.getText();
            addProperty(dataDef, 7, percent, value, arra);
        } catch (Exception e) {
            throw new Exception("请输入正确的命中值。");
        }
        
        try {
            boolean percent = button_8.getSelection();
            int value = Integer.parseInt(text_8.getText());
            String arra = textArea8.getText();
            addProperty(dataDef, 8, percent, value, arra);
        } catch (Exception e) {
            throw new Exception("请输入正确的物闪值。");
        }
        
        try {
            boolean percent = button_9.getSelection();
            int value = Integer.parseInt(text_9.getText());
            String arra = textArea9.getText();
            addProperty(dataDef, 9, percent, value, arra);
        } catch (Exception e) {
            throw new Exception("请输入正确的法闪值。");
        }
        
        try {
            boolean percent = button_10.getSelection();
            int value = Integer.parseInt(text_10.getText());
            String arra = textArea10.getText();
            addProperty(dataDef, 10, percent, value, arra);
        } catch (Exception e) {
            throw new Exception("请输入正确的物攻值。");
        }
        
        try {
            boolean percent = button_11.getSelection();
            int value = Integer.parseInt(text_11.getText());
            String arra = textArea11.getText();
            addProperty(dataDef, 11, percent, value, arra);
        } catch (Exception e) {
            throw new Exception("请输入正确的法攻值。");
        }
        
        try {
            boolean percent = button_12.getSelection();
            int value = Integer.parseInt(text_12.getText());
            String arra = textArea12.getText();
            addProperty(dataDef, 12, percent, value, arra);
        } catch (Exception e) {
            throw new Exception("请输入正确的护甲值。");
        }
        
        try {
            boolean percent = button_13.getSelection();
            int value = Integer.parseInt(text_13.getText());
            String arra = textArea13.getText();
            addProperty(dataDef, 13, percent, value, arra);
        } catch (Exception e) {
            throw new Exception("请输入正确的法防值。");
        }
        
        try {
            boolean percent = button_14.getSelection();
            int value = Integer.parseInt(text_14.getText());
            String arra = textArea14.getText();
            addProperty(dataDef, 14, percent, value, arra);
        } catch (Exception e) {
            throw new Exception("请输入正确的回血值。");
        }
        
        try {
            boolean percent = button_15.getSelection();
            int value = Integer.parseInt(text_15.getText());
            String arra = textArea15.getText();
            addProperty(dataDef, 15, percent, value, arra);
        } catch (Exception e) {
            throw new Exception("请输入正确的回气值。");
        }
        
        try {
            boolean percent = button_16.getSelection();
            int value = Integer.parseInt(text_16.getText());
            String arra = textArea16.getText();
            addProperty(dataDef, 16, percent, value, arra);
        } catch (Exception e) {
            throw new Exception("请输入正确的速度值。");
        }
        
        try {
            boolean percent = button_17.getSelection();
            int value = Integer.parseInt(text_17.getText());
            String arra = textArea17.getText();
            addProperty(dataDef, 17, percent, value, arra);
        } catch (Exception e) {
            throw new Exception("请输入正确的免暴值。");
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
     * 设置重铸的性格
     */
    private void addProperty(Recast dataDef, int type, boolean percent, int value, String area) {
        if (value != 0) {
            RecastProperty property = new RecastProperty(dataDef);
            property.type = type;
            property.isPercentValue = percent == true ? 1 : 0;
            property.value = value;
            if (area != null && area.length() > 0) {
                property.setArea(area);
            }
            dataDef.propertys.add(property);
        }
    }
}
