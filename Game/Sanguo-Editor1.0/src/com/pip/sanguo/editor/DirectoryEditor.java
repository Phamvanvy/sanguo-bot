package com.pip.sanguo.editor;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import com.pip.sanguo.data.DirectoryType;
import com.pip.sanguo.data.clientEvent.EventTrigger;
import com.pip.sanguo.data.item.Item;
import com.pip.sanguo.data.map.GameMapInfo;
import com.pip.sanguo.editor.clientevent.EventTriggerDialog;
import com.pip.sanguo.editor.directory.NewDirectoryLevelScoreDialog;
import com.pip.sanguo.editor.directory.TimeDialog;
import com.pip.sanguo.editor.property.CardDescDialog;
import com.pip.sanguo.editor.property.ChooseItemDialog;
import com.pip.sanguo.editor.property.ChooseLocationDialog;
import com.pip.util.AutoSelectAll;

/**
 * 活动指引编辑器
 * @author dchen
 */
public class DirectoryEditor extends DefaultDataObjectEditor {

    private Text textTime;
    private Text textTitle;
    private Text location;
    private Button buttonLocation;
    private Combo comboFaction;
    private Combo comboClazz;
    private Combo comboDifficulty;
    private Text textWeek;
    private Text textDescription;
    private Text textList;
    private Text textLevelMin;
    private Text textLevelMax;
    private Text textRewardDescription;
    private TableViewer levelScoreViewer;
    private Table table;
    private TableViewer conditionViewer;
    private Table conditionTable;
    private Text textExp;
    private Text textRank;
    private Text textMoney;
    private Text textPearl;
    private Text textItem1;
    private Text textItemCount1;
    private Button buttonItem1;
    private Text textItem2;
    private Text textItemCount2;
    private Button buttonItem2;
    private Text textItem3;
    private Text textItemCount3;
    private Button buttonItem3;
    private Text textItem4;
    private Text textItemCount4;
    private Button buttonItem4;
    private Text textSalary;
    
    public static final String ID = "com.pip.sanguo.editor.DirectoryEditor";
    
    @Override
    public void createPartControl(Composite parent) {
        
        DirectoryType obj = (DirectoryType)editObject;
        
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new FillLayout());

        final CTabFolder tabFolder = new CTabFolder(container, SWT.ARROW_LEFT);

        final CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE);
        tabItem.setText("活动引导信息");

        final CTabItem tabItem_1 = new CTabItem(tabFolder, SWT.NONE);
        tabItem_1.setText("引导附加配置");
        
        tabFolder.setSelection(tabItem);
        
        final Composite composite = new Composite(tabFolder, SWT.NONE);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 6;
        composite.setLayout(gridLayout);
        tabItem.setControl(composite);
        
        final Composite composite_1 = new Composite(tabFolder, SWT.NONE);
        final GridLayout gridLayout_2 = new GridLayout();
        gridLayout_2.numColumns = 6;
        composite_1.setLayout(gridLayout_2);
        tabItem_1.setControl(composite_1);
        
      //composite
        
        final Label idLabel = new Label(composite, SWT.NONE);
        idLabel.setText("标题：");

        textTitle = new Text(composite, SWT.BORDER);
        final GridData gd_textID = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        textTitle.setLayoutData(gd_textID);
        textTitle.addFocusListener(AutoSelectAll.instance);
        textTitle.addModifyListener(this);
        textTitle.setText(obj.title);

        final Label label_2 = new Label(composite, SWT.NONE);
        label_2.setLayoutData(new GridData());
        label_2.setText("目标位置：");
        
        location = new Text(composite, SWT.BORDER);
        final GridData gd_Location = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        location.setLayoutData(gd_Location);
        location.addFocusListener(AutoSelectAll.instance);
        location.addModifyListener(this);
        
        buttonLocation = new Button(composite, SWT.NONE);
        buttonLocation.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                selectLocation(0);
            }
        });
        buttonLocation.setText("...");
        
        final Label label0 = new Label(composite, SWT.NONE);
        label0.setLayoutData(new GridData());
        label0.setText("星期：");
        
        textWeek = new Text(composite, SWT.BORDER);
        final GridData gd_textWeek = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        textWeek.setLayoutData(gd_textWeek);
        textWeek.addFocusListener(AutoSelectAll.instance);
        textWeek.addModifyListener(this);

        final Label label = new Label(composite, SWT.NONE);
        label.setLayoutData(new GridData());
        label.setText("时间段：");

        textTime = new Text(composite, SWT.BORDER);
        final GridData gd_textTitle = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        textTime.setLayoutData(gd_textTitle);
        textTime.addFocusListener(AutoSelectAll.instance);
        textTime.addModifyListener(this);
        
        final Button buttonEditText0 = new Button(composite, SWT.NONE);
        buttonEditText0.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                String content = textTime.getText();
                TimeDialog dlg = new TimeDialog(getSite().getShell(), content);
                dlg.setText(textTime.getText());
                if (dlg.open() == Dialog.OK) {
                    if(textTime.getText()==null || textTime.getText().equals(""))
                        textTime.append(dlg.getText());
                    else
                        textTime.append("," + dlg.getText());
                }
            }
        });
        buttonEditText0.setText("...");
        
        final Label label1 = new Label(composite, SWT.NONE);
        label1.setLayoutData(new GridData());
        label1.setText("阵营：");
        
        comboFaction = new Combo(composite, SWT.READ_ONLY);
        comboFaction.setItems(new String[] {"所有", "魏国", "蜀国", "吴国"});
        final GridData gd_comboFaction = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        comboFaction.select(0);
        comboFaction.setLayoutData(gd_comboFaction);
        comboFaction.addModifyListener(this);
        
        final Label label2 = new Label(composite, SWT.NONE);
        label2.setLayoutData(new GridData());
        label2.setText("活动描述：");
        
        textDescription = new Text(composite, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.WRAP);
        final GridData gd_textDescription = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
        gd_textDescription.widthHint = 396;
        gd_textDescription.heightHint = 13;
        textDescription.setLayoutData(gd_textDescription);
        textDescription.addModifyListener(this);

        final Button buttonEditText = new Button(composite, SWT.NONE);
        buttonEditText.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                String content = textDescription.getText();
                CardDescDialog dlg = new CardDescDialog(getSite().getShell(), content);
                dlg.setText(textDescription.getText());
                if (dlg.open() == Dialog.OK) {
                    textDescription.setText(dlg.getText());
                }
            }
        });
        buttonEditText.setText("...");
        
        conditionViewer = new TableViewer(composite, SWT.FULL_SELECTION | SWT.BORDER);
        conditionViewer.setLabelProvider(new TriggerTableLabelProvider());
        conditionViewer.setContentProvider(new TriggerTableContentProvider());
        conditionTable = conditionViewer.getTable();
        conditionTable.setHeaderVisible(true);
        conditionTable.setLinesVisible(true);
        conditionViewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(final DoubleClickEvent event) {
                IStructuredSelection sel = (IStructuredSelection)conditionViewer.getSelection();
                if (!sel.isEmpty()) {
                    onDoubleClick(conditionViewer, sel.getFirstElement());
                }
            }
        });
        
        final GridData gd_targetTable = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
        conditionTable.setLayoutData(gd_targetTable);
        
        final TableColumn conditionColumn = new TableColumn(conditionTable, SWT.NONE);
        conditionColumn.setWidth(650);
        conditionColumn.setText("完成条件");
        
        final Group groupCombo = new Group(composite, SWT.NONE);
        groupCombo.setText("级别评分");
        groupCombo.setLayout(new FillLayout());
        GridData gd3 = new GridData(SWT.FILL, SWT.FILL, true, true, 4, 2);
        groupCombo.setLayoutData(gd3);
        
        levelScoreViewer = new TableViewer(groupCombo, SWT.FULL_SELECTION | SWT.BORDER);
        levelScoreViewer.setLabelProvider(new LevelScoreLabelProvider());
        levelScoreViewer.setContentProvider(new LevelScoreContentProvider());
        table = levelScoreViewer.getTable();
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        
        levelScoreViewer.setColumnProperties(new String[] {"level", "score"});
        levelScoreViewer.setCellModifier(new FormulaCellModifier(levelScoreViewer));
        levelScoreViewer.setCellEditors(new CellEditor[] {
                new TextCellEditor(table),
                new TextCellEditor(table)
        });
        
        final TableColumn columnType = new TableColumn(table, SWT.NONE);
        columnType.setWidth(330);
        columnType.setText("档别(1-2为1档,3-4为2档,5-6为3档,7-8为4档,9-10为5档)");

        final TableColumn columnValue = new TableColumn(table, SWT.NONE);
        columnValue.setWidth(200);
        columnValue.setText("装备评分");
        
        final Menu menuFormula = new Menu(table);
        table.setMenu(menuFormula);

        final MenuItem newLevelScore = new MenuItem(menuFormula, SWT.NONE);
        newLevelScore.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                addNewLevelScore();
            }
        });
        newLevelScore.setText("新建");
        
        final MenuItem deleteLevelScore = new MenuItem(menuFormula, SWT.NONE);
        deleteLevelScore.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                deleteLevelScore();
            }
        });
        deleteLevelScore.setText("删除");
        
      //composite1
        
        final Label label3 = new Label(composite_1, SWT.NONE);
        label3.setLayoutData(new GridData());
        label3.setText("经验：");
        
        textExp = new Text(composite_1, SWT.BORDER);
        final GridData gd_textExp = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        textExp.setLayoutData(gd_textExp);
        textExp.addFocusListener(AutoSelectAll.instance);
        textExp.addModifyListener(this);
        
        final Label label4 = new Label(composite_1, SWT.NONE);
        label4.setLayoutData(new GridData());
        label4.setText("金钱：");
        
        textMoney = new Text(composite_1, SWT.BORDER);
        final GridData gd_textMoney = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        textMoney.setLayoutData(gd_textMoney);
        textMoney.addFocusListener(AutoSelectAll.instance);
        textMoney.addModifyListener(this);
        
        final Label label5 = new Label(composite_1, SWT.NONE);
        label5.setLayoutData(new GridData());
        label5.setText("战功：");
        
        textRank = new Text(composite_1, SWT.BORDER);
        final GridData gd_textRank = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        textRank.setLayoutData(gd_textRank);
        textRank.addFocusListener(AutoSelectAll.instance);
        textRank.addModifyListener(this);
        
        final Label label6 = new Label(composite_1, SWT.NONE);
        label6.setLayoutData(new GridData());
        label6.setText("珍珠：");
        
        textPearl = new Text(composite_1, SWT.BORDER);
        final GridData gd_textPearl = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        textPearl.setLayoutData(gd_textPearl);
        textPearl.addFocusListener(AutoSelectAll.instance);
        textPearl.addModifyListener(this);
        
        final Label label7 = new Label(composite_1, SWT.NONE);
        label7.setLayoutData(new GridData());
        label7.setText("奖励物品：");
        
        textItem1 = new Text(composite_1, SWT.BORDER);
        final GridData gd_textItem1 = new GridData(SWT.FILL, SWT.LEFT, true, false, 1, 1);
        textItem1.setLayoutData(gd_textItem1);
        textItem1.addFocusListener(AutoSelectAll.instance);
        textItem1.addModifyListener(this);
        buttonItem1 = new Button(composite_1, SWT.NONE);
        buttonItem1.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                selectLocation1(0, textItem1, 1);
            }
        });
        buttonItem1.setText("...");
        
        final Label label8 = new Label(composite_1, SWT.NONE);
        label8.setLayoutData(new GridData());
        label8.setText("物品数量：");
        textItemCount1 = new Text(composite_1, SWT.BORDER);
        final GridData gd_textCount1 = new GridData(SWT.FILL, SWT.LEFT, true, false, 2, 1);
        textItemCount1.setLayoutData(gd_textCount1);
        textItemCount1.addFocusListener(AutoSelectAll.instance);
        textItemCount1.addModifyListener(this);
        
        final Label label9 = new Label(composite_1, SWT.NONE);
        label9.setLayoutData(new GridData());
        label9.setText("奖励物品：");
        
        textItem2 = new Text(composite_1, SWT.BORDER);
        final GridData gd_textItem2 = new GridData(SWT.FILL, SWT.LEFT, true, false, 1, 1);
        textItem2.setLayoutData(gd_textItem2);
        textItem2.addFocusListener(AutoSelectAll.instance);
        textItem2.addModifyListener(this);
        buttonItem2 = new Button(composite_1, SWT.NONE);
        buttonItem2.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                selectLocation1(0, textItem2, 2);
            }
        });
        buttonItem2.setText("...");
        
        final Label label10 = new Label(composite_1, SWT.NONE);
        label10.setLayoutData(new GridData());
        label10.setText("物品数量：");
        textItemCount2 = new Text(composite_1, SWT.BORDER);
        final GridData gd_textCount2 = new GridData(SWT.FILL, SWT.LEFT, true, false, 2, 1);
        textItemCount2.setLayoutData(gd_textCount2);
        textItemCount2.addFocusListener(AutoSelectAll.instance);
        textItemCount2.addModifyListener(this);
        
        final Label label11 = new Label(composite_1, SWT.NONE);
        label11.setLayoutData(new GridData());
        label11.setText("奖励物品：");
        
        textItem3 = new Text(composite_1, SWT.BORDER);
        final GridData gd_textItem3 = new GridData(SWT.FILL, SWT.LEFT, true, false, 1, 1);
        textItem3.setLayoutData(gd_textItem3);
        textItem3.addFocusListener(AutoSelectAll.instance);
        textItem3.addModifyListener(this);
        buttonItem3 = new Button(composite_1, SWT.NONE);
        buttonItem3.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                selectLocation1(0, textItem3, 3);
            }
        });
        buttonItem3.setText("...");
        
        final Label label12 = new Label(composite_1, SWT.NONE);
        label12.setLayoutData(new GridData());
        label12.setText("物品数量：");
        textItemCount3 = new Text(composite_1, SWT.BORDER);
        final GridData gd_textCount3 = new GridData(SWT.FILL, SWT.LEFT, true, false, 2, 1);
        textItemCount3.setLayoutData(gd_textCount3);
        textItemCount3.addFocusListener(AutoSelectAll.instance);
        textItemCount3.addModifyListener(this);
        
        final Label label13 = new Label(composite_1, SWT.NONE);
        label13.setLayoutData(new GridData());
        label13.setText("奖励物品：");
        
        textItem4 = new Text(composite_1, SWT.BORDER);
        final GridData gd_textItem4 = new GridData(SWT.FILL, SWT.LEFT, true, false, 1, 1);
        textItem4.setLayoutData(gd_textItem4);
        textItem4.addFocusListener(AutoSelectAll.instance);
        textItem4.addModifyListener(this);
        buttonItem4 = new Button(composite_1, SWT.NONE);
        buttonItem4.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                selectLocation1(0, textItem4, 4);
            }
        });
        buttonItem4.setText("...");
        
        final Label label14 = new Label(composite_1, SWT.NONE);
        label14.setLayoutData(new GridData());
        label14.setText("物品数量：");
        textItemCount4 = new Text(composite_1, SWT.BORDER);
        final GridData gd_textCount4 = new GridData(SWT.FILL, SWT.LEFT, true, false, 2, 1);
        textItemCount4.setLayoutData(gd_textCount4);
        textItemCount4.addFocusListener(AutoSelectAll.instance);
        textItemCount4.addModifyListener(this);
        
        final Label label15 = new Label(composite_1, SWT.NONE);
        label15.setLayoutData(new GridData());
        label15.setText("奖励描述：");
        
        textRewardDescription = new Text(composite_1, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.WRAP);
        final GridData gd_textRewardDescription = new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1);
        gd_textRewardDescription.widthHint = 396;
        gd_textRewardDescription.heightHint = 13;
        textRewardDescription.setLayoutData(gd_textRewardDescription);
        textRewardDescription.addModifyListener(this);

        final Button buttonEditText1 = new Button(composite_1, SWT.NONE);
        buttonEditText1.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                String content = textRewardDescription.getText();
                CardDescDialog dlg = new CardDescDialog(getSite().getShell(), content);
                dlg.setText(textRewardDescription.getText());
                if (dlg.open() == Dialog.OK) {
                    textRewardDescription.setText(dlg.getText());
                }
            }
        });
        buttonEditText1.setText("...");
        
        final Label label20 = new Label(composite_1, SWT.NONE);
        label20.setLayoutData(new GridData());
        label20.setText("级别下限：");
        
        textLevelMin = new Text(composite_1, SWT.BORDER);
        final GridData gd_textLevelMin = new GridData(SWT.FILL, SWT.LEFT, true, false, 2, 1);
        textLevelMin.setLayoutData(gd_textLevelMin);
        textLevelMin.addFocusListener(AutoSelectAll.instance);
        textLevelMin.addModifyListener(this);
        
        final Label label21 = new Label(composite_1, SWT.NONE);
        label21.setLayoutData(new GridData());
        label21.setText("级别上限：");
        
        textLevelMax = new Text(composite_1, SWT.BORDER);
        final GridData gd_textLevelMax = new GridData(SWT.FILL, SWT.LEFT, true, false, 2, 1);
        textLevelMax.setLayoutData(gd_textLevelMax);
        textLevelMax.addFocusListener(AutoSelectAll.instance);
        textLevelMax.addModifyListener(this);
        
        final Label label22 = new Label(composite_1, SWT.NONE);
        label22.setLayoutData(new GridData());
        label22.setText("活动明细：");
        
        textList = new Text(composite_1, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.WRAP);
        final GridData gd_textList = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
        gd_textList.widthHint = 396;
        gd_textList.heightHint = 13;
        textList.setLayoutData(gd_textDescription);
        textList.addModifyListener(this);

        final Button buttonEditText2 = new Button(composite_1, SWT.NONE);
        buttonEditText2.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                String content = textList.getText();
                CardDescDialog dlg = new CardDescDialog(getSite().getShell(), content);
                dlg.setText(textList.getText());
                if (dlg.open() == Dialog.OK) {
                    textList.setText(dlg.getText());
                }
            }
        });
        buttonEditText2.setText("...");
        
        final Label label23 = new Label(composite_1, SWT.NONE);
        label23.setLayoutData(new GridData());
        label23.setText("难度（星）：");
        
        comboDifficulty = new Combo(composite_1, SWT.READ_ONLY);
        comboDifficulty.setItems(new String[] {"1星", "2星", "3星", "4星", "5星"});
        final GridData gd_comboDifficulty = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        comboDifficulty.select(0);
        comboDifficulty.setLayoutData(gd_comboDifficulty);
        comboDifficulty.addModifyListener(this);
        
        final Label labe24 = new Label(composite_1, SWT.NONE);
        labe24.setLayoutData(new GridData());
        labe24.setText("活动工资：");
        
        textSalary = new Text(composite_1, SWT.BORDER);
        final GridData gd_textSalary = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        textSalary.setLayoutData(gd_textSalary);
        textSalary.addFocusListener(AutoSelectAll.instance);
        textSalary.addModifyListener(this);
        
        final Label label24 = new Label(composite_1, SWT.NONE);
        label24.setLayoutData(new GridData());
        label24.setText("职业：");
        
        comboClazz = new Combo(composite_1, SWT.READ_ONLY);
        comboClazz.setItems(new String[] {"武将", "刺客", "谋士", "方士", "所有"});
        final GridData gd_comboClazz = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        comboClazz.select(0);
        comboClazz.setLayoutData(gd_comboClazz);
        comboClazz.addModifyListener(this);
        
        conditionViewer.setInput(obj);
        levelScoreViewer.setInput(obj);
        
        initData(obj);
    }
    
    private void initData(DirectoryType directory){
        try {
            textTitle.setText(String.valueOf(directory.title));
        }catch (Exception e) {
        }
        try {
            location.setText(GameMapInfo.locationToString(EditorApplication.getProj(), directory.location, false));
        }catch (Exception e) {
            location.setText("");
        }
        try {
            textTime.setText(DirectoryType.parseTime(directory.timeScheduleHour, directory.timeScheduleMin));
        }catch (Exception e) {
        }
        try {
            textDescription.setText(directory.description1);
        }catch (Exception e) {
        }
        try {
            textWeek.setText(DirectoryType.parseWeeks(directory.weeks));
        }catch (Exception e) {
        }
        try {
            textTime.setText(DirectoryType.parseTime(directory.timeScheduleHour, directory.timeScheduleMin));
        }catch (Exception e) {
        }
        try {
            comboFaction.select(directory.faction);
        }catch (Exception e) {
        }
        try {
            comboClazz.select(directory.clazz);
        }catch (Exception e) {
        }
        try {
            comboDifficulty.select(directory.difficulty-1);
        }catch (Exception e) {
        }
        try {
            textExp.setText(String.valueOf(directory.exp));
        }catch (Exception e) {
        }
        try {
            textMoney.setText(String.valueOf(directory.money));
        }catch (Exception e) {
        }
        try {
            textRank.setText(String.valueOf(directory.rank));
        }catch (Exception e) {
        }
        try {
            textPearl.setText(String.valueOf(directory.pearl));
        }catch (Exception e) {
        }
        try {
            Item item = EditorApplication.getInstance().getProjectData().findItemOrEquipment(directory.item1);
            textItem1.setText(item.toString());
        }catch (Exception e) {
        }
        try {
            Item item = EditorApplication.getInstance().getProjectData().findItemOrEquipment(directory.item2);
            textItem2.setText(item.toString());
        }catch (Exception e) {
        }
        try {
            Item item = EditorApplication.getInstance().getProjectData().findItemOrEquipment(directory.item3);
            textItem3.setText(item.toString());
        }catch (Exception e) {
        }
        try {
            Item item = EditorApplication.getInstance().getProjectData().findItemOrEquipment(directory.item4);
            textItem4.setText(item.toString());
        }catch (Exception e) {
        }
        try {
            textItemCount1.setText(String.valueOf(directory.count1));
        }catch (Exception e) {
        }
        try {
            textItemCount2.setText(String.valueOf(directory.count2));
        }catch (Exception e) {
        }
        try {
            textItemCount3.setText(String.valueOf(directory.count3));
        }catch (Exception e) {
        }
        try {
            textItemCount4.setText(String.valueOf(directory.count4));
        }catch (Exception e) {
        }
        try {
            textRewardDescription.setText(String.valueOf(directory.rewardDesc));
        }catch (Exception e) {
        }
        try {
            textLevelMin.setText(String.valueOf(directory.minLevel));
        }catch (Exception e) {
        }
        try {
            textLevelMax.setText(String.valueOf(directory.maxLevel));
        }catch (Exception e) {
        }
        try {
            textList.setText(String.valueOf(directory.directoyList));
        }catch (Exception e) {
        }
        try {
            textSalary.setText(String.valueOf(directory.salary));
        }catch (Exception e) {
        }
    }
    
    private void addNewLevelScore(){
        NewDirectoryLevelScoreDialog dlg = new NewDirectoryLevelScoreDialog(
                getSite().getShell(),"新建级别评测", "请输入等级：", "1", new IInputValidator() {
            public String isValid(String newText) {
                if (newText.trim().length() == 0) {
                    return "等级不能为空。";
                } else {
                    return null;
                }
            }
        });
        if (dlg.open() != InputDialog.OK) {
            return;
        }
        String newname = dlg.getValue();
        DirectoryType d = (DirectoryType)editObject;
        d.scores.put(Integer.parseInt(newname), 60);
        levelScoreViewer.refresh();
        setDirty(true);
    }
    
    private void deleteLevelScore(){
        NewDirectoryLevelScoreDialog dlg = new NewDirectoryLevelScoreDialog(
                getSite().getShell(),"删除级别评测", "请输入等级：", "1", new IInputValidator() {
            public String isValid(String newText) {
                if (newText.trim().length() == 0) {
                    return "等级不能为空。";
                } else {
                    return null;
                }
            }
        });
        if (dlg.open() != InputDialog.OK) {
            return;
        }
        String newname = dlg.getValue();
        DirectoryType d = (DirectoryType)editObject;
        d.scores.remove(Integer.parseInt(newname));
        levelScoreViewer.refresh();
        setDirty(true);
    }
    
    private void selectLocation(int index) {
        int[] locations = new int[3];
        ChooseLocationDialog dlg = new ChooseLocationDialog(getSite().getShell());
        dlg.setLocation(locations);
        if (dlg.open() == Dialog.OK) {
            locations = dlg.getLocation();
            if(locations!=null && locations.length>0 && locations[0]==-1)
                locations[0] = 0;
            ((DirectoryType)editObject).location = locations;
            location.setText(GameMapInfo.locationToString(EditorApplication.getProj(), locations, false));
        }
    }
    
    private void selectLocation1(int index, Text text, int itemIndex) {
        ChooseItemDialog dlg = new ChooseItemDialog(getSite().getShell());
        int itemID = -1;
        dlg.setSelectedItem(itemID);
        if (dlg.open() == Dialog.OK) {
            itemID = dlg.getSelectedItem();
            Item item = EditorApplication.getInstance().getProjectData().findItemOrEquipment(itemID);
            if(item != null){       
                text.setText(item.toString());
                DirectoryType obj = (DirectoryType)getEditObject();
                if(itemIndex==1)
                    obj.item1 = item.id;
                else if(itemIndex==2)
                    obj.item2 = item.id;
                else if(itemIndex==3)
                    obj.item3 = item.id;
                else if(itemIndex==4)
                    obj.item4 = item.id;
            }
        } else {
            return;
        }
    }
    
    /**
     * 保存当前编辑数据。
     */
    protected void saveData() throws Exception {
        DirectoryType dataDef = (DirectoryType)editObject;
        dataDef.title = textTitle.getText();
        dataDef.description1 = textDescription.getText();
        try {
            dataDef.weeks = DirectoryType.parseWeeks(textWeek.getText());
        }catch (Exception e1) {
            throw new Exception("请正确填写星期,格式如\"1,2,4,7\"");
        }
        try {
            dataDef.timeScheduleHour = DirectoryType.parseTime(textTime.getText(), true);
            dataDef.timeScheduleMin = DirectoryType.parseTime(textTime.getText(), false);
        }catch (Exception e1) {
        }
        dataDef.faction = comboFaction.getSelectionIndex();
        dataDef.clazz = comboClazz.getSelectionIndex();
        dataDef.difficulty = comboDifficulty.getSelectionIndex() + 1;
        try {
            dataDef.exp = Integer.parseInt(textExp.getText());
        }catch (Exception e) {
            throw new Exception("请填写经验");
        }
        try {
            dataDef.money = Integer.parseInt(textMoney.getText());
        }catch (Exception e) {
            throw new Exception("请填写金钱");
        }
        try {
            dataDef.rank = Integer.parseInt(textRank.getText());
        }catch (Exception e) {
            throw new Exception("请填写战功");
        }
        try {
            dataDef.pearl = Integer.parseInt(textPearl.getText());
        }catch (Exception e) {
            throw new Exception("请填写珍珠数量");
        }
//        if(dataDef.location==null)
//            throw new Exception("请选择目标位置");
        try {
            dataDef.count1 = Integer.parseInt(textItemCount1.getText());
        }catch (Exception e) {
        }
        try {
            dataDef.count2 = Integer.parseInt(textItemCount2.getText());
        }catch (Exception e) {
        }
        try {
            dataDef.count3 = Integer.parseInt(textItemCount3.getText());
        }catch (Exception e) {
        }
        try {
            dataDef.count4 = Integer.parseInt(textItemCount4.getText());
        }catch (Exception e) {
        }
        try {
            dataDef.rewardDesc = textRewardDescription.getText();
        }catch (Exception e) {
        }
        try {
            dataDef.minLevel = Integer.parseInt(textLevelMin.getText());
        }catch (Exception e) {
        }
        try {
            dataDef.maxLevel = Integer.parseInt(textLevelMax.getText());
        }catch (Exception e) {
        }
        try {
            dataDef.directoyList = textList.getText();
        }catch (Exception e) {
        }
        try {
            dataDef.salary = Integer.parseInt(textSalary.getText());
        }catch (Exception e) {
            throw new Exception("请填写活动工资");
        }
    }
    
    private void onDoubleClick(Object viewer, Object sel) {
        if (viewer == conditionViewer) {
            if ("".equals(sel)) {
                EventTrigger newEvent = new EventTrigger((DirectoryType)editObject);
                EventTriggerDialog dropDialog = new EventTriggerDialog(getSite().getShell(), newEvent, EventTriggerDialog.TYPE_DIRECTORY);
                if (dropDialog.open() == Dialog.OK) {
                    ((DirectoryType)editObject).triggers.add(newEvent);
                    conditionViewer.refresh();
                    setDirty(true);
                }
            } else {
                EventTrigger selGroup = (EventTrigger)sel;
                EventTriggerDialog dropDialog = new EventTriggerDialog(getSite().getShell(), selGroup, EventTriggerDialog.TYPE_DIRECTORY);
                if(dropDialog.open() == IDialogConstants.OK_ID){
                    EventTrigger selNewGroup = dropDialog.getSelectedObject();
                    if (selNewGroup.equals(selGroup)) {
                        selGroup.update(selNewGroup);
                        conditionViewer.refresh();
                        setDirty(true);
                    }
                }
            }
        }
    }
    
    class LevelScoreLabelProvider extends LabelProvider implements ITableLabelProvider {

        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        public String getColumnText(Object element, int columnIndex) {
            int ret = ((Integer)element).intValue();
            DirectoryType d = (DirectoryType)editObject;
            if(columnIndex%2==0)
                return String.valueOf(ret);
            return String.valueOf(d.scores.get(ret));
        }

    }
    
    class LevelScoreContentProvider implements IStructuredContentProvider{

        public Object[] getElements(Object inputElement) {
            DirectoryType d = (DirectoryType)inputElement;
            Integer[] keys = new Integer[d.scores.keySet().size()];
            keys = d.scores.keySet().toArray(keys);
            return keys;
        }

        public void dispose() {
            
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            
        }
        
    }
    
    class FormulaCellModifier implements ICellModifier {

        protected TableViewer formulaViewer;
        
        public FormulaCellModifier(TableViewer formulaViewer){
            this.formulaViewer = formulaViewer;
        }
        
        public boolean canModify(Object element, String property) {
            return "score".equals(property);
        }

        public Object getValue(Object element, String property) {
            if("score".equals(property)){
                DirectoryType de = (DirectoryType)editObject;
                return String.valueOf(de.scores.get(Integer.parseInt(String.valueOf(element))));
            }
            return null;
        }

        public void modify(Object element, String property, Object value) {
            if("score".equals(property) && element instanceof TableItem){
                TableItem tableItem = (TableItem)element;
                int key = Integer.parseInt(tableItem.getText(0));
                DirectoryType de = (DirectoryType)editObject;
                de.scores.put(key, Integer.parseInt(String.valueOf(value)));
                formulaViewer.refresh();
            }
        }
    }
    
    class TriggerTableLabelProvider extends LabelProvider implements ITableLabelProvider {
        
        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof String) {
                if (columnIndex == 0) {
                    return "新条件...";
                } else {
                    return "";
                }
            } else {
                EventTrigger trigger = (EventTrigger)element;
                if (columnIndex == 0) {
                    if(trigger.exprEvent == null){
                        return "无";
                    }else{
                        return trigger.exprEvent.getName(); 
                    }
                }
                return "";
            }
        }
        
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }
    }
    
    class TriggerTableContentProvider implements IStructuredContentProvider {
        
        public Object[] getElements(Object inputElement) {
            DirectoryType direc = (DirectoryType)inputElement;
            Object[] ret = new Object[direc.triggers.size() + 1];
            direc.triggers.toArray(ret);
            ret[direc.triggers.size()] = "";
            return ret;
        }
        
        public void dispose() {
            
        }
        
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            
        }
    }
    
}
