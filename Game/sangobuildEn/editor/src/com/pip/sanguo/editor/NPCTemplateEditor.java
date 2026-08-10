package com.pip.sanguo.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.Animation;
import com.pip.sanguo.data.NPCTemplate;
import com.pip.sanguo.data.NPCType;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.item.DropGroup;
import com.pip.sanguo.data.item.DropNode;
import com.pip.sanguo.data.quest.Quest;
import com.pip.sanguo.editor.ai.AIRuleConfig;
import com.pip.sanguo.editor.ai.EditAIRuleDialog;
import com.pip.sanguo.editor.property.ChooseDropGroupDialog;
import com.pip.sanguo.editor.util.AnimatePreviewer;
import com.pip.sanguo.editor.util.Constants;
import com.pip.util.AutoSelectAll;

public class NPCTemplateEditor extends DefaultDataObjectEditor {
    private Combo comboAnalyzeClazz;
    private Combo comboAnalyzeLevel;
    private Text textCredit;
    class ContentProvider_1 implements IStructuredContentProvider {
        public Object[] getElements(Object inputElement) {
            return ((ProjectData)inputElement).getDataListByType(Animation.class).toArray();
        }
        public void dispose() {
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }
    class ContentProvider implements IStructuredContentProvider {
        public Object[] getElements(Object inputElement) {
            return ((ProjectData)inputElement).getDictDataListByType(NPCType.class).toArray();
        }
        public void dispose() {
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }
    class TableContentProvider implements IStructuredContentProvider {
        public Object[] getElements(Object inputElement) {
            if(inputElement instanceof List){
                return ((List)inputElement).toArray();
            }
            return new Object[0];
        }
        public void dispose() {
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }
    class TableLabelProvider implements ITableLabelProvider{

        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        public String getColumnText(Object element, int columnIndex) {
            if(element instanceof DropNode){
                DropNode node = (DropNode)element;
                switch(columnIndex){
                    case 0:{/* 类型 */
                        switch(((DropNode)element).type){
                            case DropNode.TYPE_DROPGROUP:{
                                return "掉落组";
                            }
                            case DropNode.TYPE_EQUIPMENT:{
                                return "装备";
                            }
                            case DropNode.TYPE_ITEM:{
                                return "物品";
                            }
                        }
                        return "";
                    }
                    case 1:{/* 名称 */
                        if (node.type == DropNode.TYPE_ITEM) {
                            return EditorApplication.getProj().findItem(node.id).toString();
                        }
                        else if (node.type == DropNode.TYPE_DROPGROUP) {
                            return EditorApplication.getProj().findObject(DropGroup.class, node.id).toString();
                        }
                        else if (node.type == DropNode.TYPE_EQUIPMENT) {
                            return EditorApplication.getProj().findEquipment(node.id).toString();
                        }

                        return "";
                    }
                    case 2:{/* 掉落几率 */
                        return String.valueOf(node.getRateString());
                    }
                    case 3:{/* 掉落数量 */
                        return node.quantityMin + "-" + node.quantityMax;
                    }
                    case 4:{/* 任务掉落 */
                        return node.isTask ? "是" : "否";
                    }
                    case 5:{/* 相关任务 */
                        DataObject quest = EditorApplication.getProj().findObject(Quest.class, node.taskId);
                        if(quest != null){                            
                            return quest.toString();
                        }
                    }
                }
            }
            return "";
        }

        public void addListener(ILabelProviderListener listener) {}

        public void dispose() {}

        public boolean isLabelProperty(Object element, String property) {
            return false;
        }

        public void removeListener(ILabelProviderListener listener) {}
    }
    private Text textMagicAP;
    private Text textMagicArmor2;
    private Text textMagicArmor;
    private Text textExp2;
    private Text textMoney2;
    private Text textMagicAP2;
    private Text textPhyAP2;
    private Text textPhyAP;
    private Text textAGI2;
    private Text textINT2;
    private Text textSTA2;
    private Text textSTR2;
    private Text textArmor2;
    private Text textArmor;
    private Text textMP2;
    private Text textHP2;
    private Combo comboClazz;
    private ComboViewer comboImage;
    private ComboViewer comboType;
    private Text textChaseDistance;
    private Text textEyeshot;
    private Text textSpeed;
    private Text textWalkSpeed;
    private Text textAI;
    private Text textINT;
    private Text textAGI;
    private Text textSTR;
    private Text textSTA;
    private Combo comboLevel;
    private Text textMP;
    private Text textHP;
    private Text textDescription;
    private Text textTitle;
    private Text textCollectParam;
    private Text textQuestId;
    private Text textCollectTime;
    private Text textMoney;
    private Text textID;
    private AnimatePreviewer previewer;
    private Text textExp;
    private Table table;
    
    private TableViewer dropGroupTable;
    private Action addDropGroup;
    private Action delDropGroup;
    
    public static final String ID = "com.pip.sanguo.editor.NPCEditor"; //$NON-NLS-1$
    private Combo comboTypeCtrl;
    private Combo comboImageCtrl;


    private Button buttonPowerful;

    /**
     * Create contents of the editor part
     * @param parent
     */
    @Override
    public void createPartControl(Composite parent) {
        createActions();
        Composite container = new Composite(parent, SWT.NONE);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 9;
        container.setLayout(gridLayout);

        final Label label = new Label(container, SWT.NONE);
        label.setText("ID：");

        textID = new Text(container, SWT.BORDER);
        final GridData gd_textID = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        textID.setLayoutData(gd_textID);
        textID.addFocusListener(AutoSelectAll.instance);
        textID.addModifyListener(this);

        final Label label_1 = new Label(container, SWT.NONE);
        label_1.setLayoutData(new GridData());
        label_1.setText("名称：");

        textTitle = new Text(container, SWT.BORDER);
        final GridData gd_textTitle = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        textTitle.setLayoutData(gd_textTitle);
        textTitle.addFocusListener(AutoSelectAll.instance);
        textTitle.addModifyListener(this);

        final Label label_2 = new Label(container, SWT.NONE);
        label_2.setLayoutData(new GridData());
        label_2.setText("描述：");

        textDescription = new Text(container, SWT.BORDER);
        final GridData gd_textDescription = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        textDescription.setLayoutData(gd_textDescription);
        textDescription.addFocusListener(AutoSelectAll.instance);
        textDescription.addModifyListener(this);

        final Label label_4 = new Label(container, SWT.NONE);
        label_4.setLayoutData(new GridData());
        label_4.setText("图片：");

        comboImage = new ComboViewer(container, SWT.READ_ONLY);
        comboImage.setContentProvider(new ContentProvider_1());
        comboImage.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(final SelectionChangedEvent event) {
                setDirty(true);
                updatePreviewer();
            }
        });
        comboImageCtrl = comboImage.getCombo();
        comboImageCtrl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        comboImageCtrl.setVisibleItemCount(10);
        comboImage.setInput(EditorApplication.getProj());

        final Label label_3 = new Label(container, SWT.NONE);
        label_3.setLayoutData(new GridData());
        label_3.setText("类型：");

        comboType = new ComboViewer(container, SWT.READ_ONLY);
        comboType.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(final SelectionChangedEvent event) {
                IStructuredSelection selected = (IStructuredSelection)event.getSelection();
                NPCType selectType = (NPCType)selected.getFirstElement();
                setType(selectType);
                setDirty(true);
            }
        });
        comboType.setContentProvider(new ContentProvider());
        comboTypeCtrl = comboType.getCombo();
        comboTypeCtrl.setVisibleItemCount(10);
        comboTypeCtrl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        comboType.setInput(EditorApplication.getProj());

        final Label label_7 = new Label(container, SWT.NONE);
        label_7.setLayoutData(new GridData());
        label_7.setText("级别：");

        String[] items = new String[Constants.LEVEL_EXP.length];
        for (int i = 0; i < Constants.LEVEL_EXP.length; i++) {
            items[i] = String.valueOf(i);
        }

        comboLevel = new Combo(container, SWT.READ_ONLY);
        comboLevel.setVisibleItemCount(50);
        comboLevel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        comboLevel.setItems(items);
        comboLevel.addModifyListener(this);
        comboLevel.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e) {
                setLevel(comboLevel.getSelectionIndex());
                setDirty(true);
            }
        });

        final Label label_21 = new Label(container, SWT.NONE);
        label_21.setText("职业：");

        comboClazz = new Combo(container, SWT.READ_ONLY);
        comboClazz.setItems(new String[] {"武将", "刺客", "谋士", "方士"});
        final GridData gd_comboClazz = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        comboClazz.setLayoutData(gd_comboClazz);
        comboClazz.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e) {
                setClazz(comboClazz.getSelectionIndex());
                setDirty(true);
            }
        });

        buttonPowerful = new Button(container, SWT.CHECK);
        final GridData gd_buttonPowerful = new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1);
        buttonPowerful.setLayoutData(gd_buttonPowerful);
        buttonPowerful.setText("这是一只精英怪物");
        buttonPowerful.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e) {
                ((NPCTemplate)getEditObject()).powerful = buttonPowerful.getSelection();
                setDirty(true);
            }
        });
        
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);

        final Label label_5 = new Label(container, SWT.NONE);
        label_5.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        label_5.setText("生命：");

        textHP = new Text(container, SWT.BORDER);
        textHP.setEditable(false);
        final GridData gd_textHP = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textHP.setLayoutData(gd_textHP);
        textHP.addFocusListener(AutoSelectAll.instance);
        textHP.addModifyListener(this);

        textHP2 = new Text(container, SWT.BORDER);
        final GridData gd_textHP2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textHP2.setLayoutData(gd_textHP2);
        textHP2.addFocusListener(AutoSelectAll.instance);
        textHP2.addModifyListener(this);

        final Label label_6 = new Label(container, SWT.NONE);
        label_6.setText("法力：");

        textMP = new Text(container, SWT.BORDER);
        textMP.setEditable(false);
        final GridData gd_textMP = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textMP.setLayoutData(gd_textMP);
        textMP.addFocusListener(AutoSelectAll.instance);
        textMP.addModifyListener(this);

        textMP2 = new Text(container, SWT.BORDER);
        final GridData gd_textMP2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textMP2.setLayoutData(gd_textMP2);
        textMP2.addFocusListener(AutoSelectAll.instance);
        textMP2.addModifyListener(this);

        final Label label_22 = new Label(container, SWT.NONE);
        label_22.setText("护甲：");

        textArmor = new Text(container, SWT.BORDER);
        textArmor.setEditable(false);
        final GridData gd_textArmor = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textArmor.setLayoutData(gd_textArmor);
        textArmor.addFocusListener(AutoSelectAll.instance);
        textArmor.addModifyListener(this);

        textArmor2 = new Text(container, SWT.BORDER);
        final GridData gd_textArmor2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textArmor2.setLayoutData(gd_textArmor2);
        textArmor2.addFocusListener(AutoSelectAll.instance);
        textArmor2.addModifyListener(this);

        final Label label_9 = new Label(container, SWT.NONE);
        label_9.setLayoutData(new GridData());
        label_9.setText("力量：");

        textSTR = new Text(container, SWT.BORDER);
        textSTR.setEditable(false);
        final GridData gd_textSTR = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textSTR.setLayoutData(gd_textSTR);
        textSTR.addFocusListener(AutoSelectAll.instance);
        textSTR.addModifyListener(this);

        textSTR2 = new Text(container, SWT.BORDER);
        final GridData gd_textSTR2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textSTR2.setLayoutData(gd_textSTR2);
        textSTR2.addFocusListener(AutoSelectAll.instance);
        textSTR2.addModifyListener(this);

        final Label label_8 = new Label(container, SWT.NONE);
        label_8.setLayoutData(new GridData());
        label_8.setText("耐力：");

        textSTA = new Text(container, SWT.BORDER);
        textSTA.setEditable(false);
        final GridData gd_textSTA = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textSTA.setLayoutData(gd_textSTA);
        textSTA.addFocusListener(AutoSelectAll.instance);
        textSTA.addModifyListener(this);

        textSTA2 = new Text(container, SWT.BORDER);
        final GridData gd_textSTA2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textSTA2.setLayoutData(gd_textSTA2);
        textSTA2.addFocusListener(AutoSelectAll.instance);
        textSTA2.addModifyListener(this);

        final Label label_11 = new Label(container, SWT.NONE);
        label_11.setLayoutData(new GridData());
        label_11.setText("智力：");

        textINT = new Text(container, SWT.BORDER);
        textINT.setEditable(false);
        final GridData gd_textINT = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textINT.setLayoutData(gd_textINT);
        textINT.addFocusListener(AutoSelectAll.instance);
        textINT.addModifyListener(this);

        textINT2 = new Text(container, SWT.BORDER);
        final GridData gd_textINT2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textINT2.setLayoutData(gd_textINT2);
        textINT2.addFocusListener(AutoSelectAll.instance);
        textINT2.addModifyListener(this);

        final Label label_10 = new Label(container, SWT.NONE);
        label_10.setLayoutData(new GridData());
        label_10.setText("敏捷：");

        textAGI = new Text(container, SWT.BORDER);
        textAGI.setEditable(false);
        final GridData gd_textAGI = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textAGI.setLayoutData(gd_textAGI);
        textAGI.addFocusListener(AutoSelectAll.instance);
        textAGI.addModifyListener(this);

        textAGI2 = new Text(container, SWT.BORDER);
        final GridData gd_textAGI2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textAGI2.setLayoutData(gd_textAGI2);
        textAGI2.addFocusListener(AutoSelectAll.instance);
        textAGI2.addModifyListener(this);

        final Label label_23 = new Label(container, SWT.NONE);
        label_23.setText("武器物攻：");

        textPhyAP = new Text(container, SWT.BORDER);
        textPhyAP.setEditable(false);
        final GridData gd_textPhyAP = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textPhyAP.setLayoutData(gd_textPhyAP);
        textPhyAP.addFocusListener(AutoSelectAll.instance);
        textPhyAP.addModifyListener(this);

        textPhyAP2 = new Text(container, SWT.BORDER);
        final GridData gd_textPhyAP2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textPhyAP2.setLayoutData(gd_textPhyAP2);
        textPhyAP2.addFocusListener(AutoSelectAll.instance);
        textPhyAP2.addModifyListener(this);

        final Label label_24 = new Label(container, SWT.NONE);
        label_24.setText("武器法攻：");

        textMagicAP = new Text(container, SWT.BORDER);
        textMagicAP.setEditable(false);
        final GridData gd_textMagicAP = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textMagicAP.setLayoutData(gd_textMagicAP);
        textMagicAP.addFocusListener(AutoSelectAll.instance);
        textMagicAP.addModifyListener(this);

        textMagicAP2 = new Text(container, SWT.BORDER);
        final GridData gd_textMagicAP2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textMagicAP2.setLayoutData(gd_textMagicAP2);
        textMagicAP2.addFocusListener(AutoSelectAll.instance);
        textMagicAP2.addModifyListener(this);

        final Label label_17 = new Label(container, SWT.NONE);
        label_17.setLayoutData(new GridData());
        label_17.setText("金钱：");

        textMoney = new Text(container, SWT.BORDER);
        textMoney.setEditable(false);
        textMoney.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        textMoney.addFocusListener(AutoSelectAll.instance);
        textMoney.addModifyListener(this);

        textMoney2 = new Text(container, SWT.BORDER);
        final GridData gd_textMoney2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textMoney2.setLayoutData(gd_textMoney2);
        textMoney2.addFocusListener(AutoSelectAll.instance);
        textMoney2.addModifyListener(this);

        final Label label_16 = new Label(container, SWT.NONE);
        label_16.setLayoutData(new GridData());
        label_16.setText("经验：");

        textExp = new Text(container, SWT.BORDER);
        textExp.setEditable(false);
        textExp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        textExp.addFocusListener(AutoSelectAll.instance);
        textExp.addModifyListener(this);

        textExp2 = new Text(container, SWT.BORDER);
        final GridData gd_textExp2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textExp2.setLayoutData(gd_textExp2);
        textExp2.addFocusListener(AutoSelectAll.instance);
        textExp2.addModifyListener(this);

        final Label label_25 = new Label(container, SWT.NONE);
        label_25.setText("法防：");

        textMagicArmor = new Text(container, SWT.BORDER);
        textMagicArmor.setEditable(false);
        final GridData gd_textMagicArmor = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textMagicArmor.setLayoutData(gd_textMagicArmor);
        textMagicArmor.addFocusListener(AutoSelectAll.instance);
        textMagicArmor.addModifyListener(this);

        textMagicArmor2 = new Text(container, SWT.BORDER);
        final GridData gd_textMagicArmor2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textMagicArmor2.setLayoutData(gd_textMagicArmor2);
        textMagicArmor2.addFocusListener(AutoSelectAll.instance);
        textMagicArmor2.addModifyListener(this);

        final Label label_27 = new Label(container, SWT.NONE);
        label_27.setText("声望：");

        textCredit = new Text(container, SWT.BORDER);
        final GridData gd_textCredit = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        textCredit.setLayoutData(gd_textCredit);
        textCredit.addFocusListener(AutoSelectAll.instance);
        textCredit.addModifyListener(this);
        
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);

        final Label label_12 = new Label(container, SWT.NONE);
        label_12.setText("AI脚本：");

        textAI = new Text(container, SWT.WRAP | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER);
        textAI.setEditable(false);
        final GridData gd_textAI = new GridData(SWT.FILL, SWT.FILL, true, true, 7, 1);
        textAI.setLayoutData(gd_textAI);
        textAI.addFocusListener(AutoSelectAll.instance);
        textAI.addModifyListener(this);

        final Button buttonEditAI = new Button(container, SWT.NONE);
        buttonEditAI.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                NPCTemplate nt = (NPCTemplate)getEditObject();
                EditAIRuleDialog dlg = new EditAIRuleDialog(getSite().getShell());
                List<AIRuleConfig> nrl = new ArrayList<AIRuleConfig>();
                for (AIRuleConfig rc : nt.aiRules) {
                    nrl.add(rc.duplicate());
                }
                dlg.setEditTarget(nt.aiClass, nrl);
                if (dlg.open() == EditAIRuleDialog.OK) {
                    nt.aiClass = dlg.getAIClass();
                    nt.aiRules = dlg.getAIRules();
                    textAI.setText(nt.getAIClassDesc());
                }
            }
        });
        final GridData gd_buttonEditAI = new GridData();
        buttonEditAI.setLayoutData(gd_buttonEditAI);
        buttonEditAI.setText("编辑...");

        final Label label_13 = new Label(container, SWT.NONE);
        label_13.setText("速度(像素/秒)：");

        textSpeed = new Text(container, SWT.BORDER);
        final GridData gd_textSpeed = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textSpeed.setLayoutData(gd_textSpeed);
        textSpeed.addFocusListener(AutoSelectAll.instance);
        textSpeed.addModifyListener(this);

        final Label label_26 = new Label(container, SWT.NONE);
        label_26.setText("巡逻速度：");

        textWalkSpeed = new Text(container, SWT.BORDER);
        final GridData gd_textWalkSpeed = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textWalkSpeed.setLayoutData(gd_textWalkSpeed);
        textWalkSpeed.addFocusListener(AutoSelectAll.instance);
        textWalkSpeed.addModifyListener(this);

        final Label label_14 = new Label(container, SWT.NONE);
        label_14.setLayoutData(new GridData());
        label_14.setText("视野(码)：");

        textEyeshot = new Text(container, SWT.BORDER);
        final GridData gd_textEyeshot = new GridData(SWT.FILL, SWT.CENTER, true, false);
        textEyeshot.setLayoutData(gd_textEyeshot);
        textEyeshot.addFocusListener(AutoSelectAll.instance);
        textEyeshot.addModifyListener(this);

        final Label label_15 = new Label(container, SWT.NONE);
        label_15.setLayoutData(new GridData());
        label_15.setText("追击距离(码)：");

        textChaseDistance = new Text(container, SWT.BORDER);
        final GridData gd_textChaseDistance = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        textChaseDistance.setLayoutData(gd_textChaseDistance);
        textChaseDistance.addFocusListener(AutoSelectAll.instance);
        textChaseDistance.addModifyListener(this);

        final Label label_19 = new Label(container, SWT.NONE);
        label_19.setLayoutData(new GridData());
        label_19.setText("任务：");

        textQuestId = new Text(container, SWT.BORDER);
        textQuestId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
        textQuestId.addFocusListener(AutoSelectAll.instance);
        textQuestId.addModifyListener(this);

        final Label label_18 = new Label(container, SWT.NONE);
        label_18.setLayoutData(new GridData());
        label_18.setText("采集时间(毫秒)：");

        textCollectTime = new Text(container, SWT.BORDER);
        textCollectTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        textCollectTime.addFocusListener(AutoSelectAll.instance);
        textCollectTime.addModifyListener(this);

        final Label label_20 = new Label(container, SWT.NONE);
        label_20.setLayoutData(new GridData());
        label_20.setText("采集技能等级：");

        textCollectParam = new Text(container, SWT.BORDER);
        textCollectParam.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        textCollectParam.addFocusListener(AutoSelectAll.instance);
        textCollectParam.addModifyListener(this);

        final Group dropGroup = new Group(container, SWT.NONE);
        dropGroup.setText("掉落组");
        dropGroup.setLayout(new FillLayout());
        final GridData gd_dropGroup = new GridData(SWT.FILL, SWT.FILL, false, true, 5, 1);
        gd_dropGroup.widthHint = 230;
        dropGroup.setLayoutData(gd_dropGroup);

        dropGroupTable = new TableViewer(dropGroup, SWT.FULL_SELECTION | SWT.MULTI | SWT.BORDER);
        table = dropGroupTable.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        dropGroupTable.setLabelProvider(new TableLabelProvider());
        dropGroupTable.setContentProvider(new TableContentProvider());
        dropGroupTable.addDoubleClickListener(new IDoubleClickListener(){
            public void doubleClick(final DoubleClickEvent event) {
                IStructuredSelection sel = (IStructuredSelection)dropGroupTable.getSelection();
                if (!sel.isEmpty()) {
                    onDoubleClick(sel);
                }
            }
        });
        
        MenuManager mgr = new MenuManager();
        mgr.add(addDropGroup);
        mgr.add(delDropGroup);
        Menu menu = mgr.createContextMenu(dropGroupTable.getControl());
        dropGroupTable.getControl().setMenu(menu);

        final TableColumn newColumnTableColumn = new TableColumn(table, SWT.CENTER);
        newColumnTableColumn.setWidth(60);
        newColumnTableColumn.setText("类型");

        final TableColumn newColumnTableColumn_3 = new TableColumn(table, SWT.CENTER);
        newColumnTableColumn_3.setWidth(150);
        newColumnTableColumn_3.setText("名称");

        final TableColumn newColumnTableColumn_1 = new TableColumn(table, SWT.CENTER);
        newColumnTableColumn_1.setWidth(80);
        newColumnTableColumn_1.setText("掉落几率");

        final TableColumn newColumnTableColumn_2 = new TableColumn(table, SWT.CENTER);
        newColumnTableColumn_2.setWidth(80);
        newColumnTableColumn_2.setText("掉落数量");
        
        final TableColumn newColumnTableColumn_4 = new TableColumn(table, SWT.CENTER);
        newColumnTableColumn_4.setWidth(80);
        newColumnTableColumn_4.setText("任务掉落");
        
        final TableColumn newColumnTableColumn_5 = new TableColumn(table, SWT.CENTER);
        newColumnTableColumn_5.setWidth(100);
        newColumnTableColumn_5.setText("相关任务");

        
        final Group groupPreview = new Group(container, SWT.NONE);
        groupPreview.setText("预览");
        final GridData gd_groupPreview = new GridData(SWT.FILL, SWT.FILL, true, false, 4, 2);
        gd_groupPreview.widthHint = 254;
        groupPreview.setLayoutData(gd_groupPreview);
        groupPreview.setLayout(new FillLayout());
        
        previewer = new AnimatePreviewer(groupPreview, SWT.NONE);
        previewer.setListVisible(false);
        previewer.setEditEnable(false);

        final Composite composite = new Composite(container, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 5, 1));
        final GridLayout gridLayout_1 = new GridLayout();
        gridLayout_1.numColumns = 5;
        composite.setLayout(gridLayout_1);

        final Button setDefaultDropButton = new Button(composite, SWT.NONE);
        setDefaultDropButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                setDefaultDrop();
            }
        });
        setDefaultDropButton.setText("设置标准掉落");

        final Button dropAnalyseButton1 = new Button(composite, SWT.NONE);
        dropAnalyseButton1.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                onAnalyzeDrop1();
            }
        });
        dropAnalyseButton1.setText("任务掉率分析");

        final Button dropAnalyseButton2 = new Button(composite, SWT.NONE);
        
        dropAnalyseButton2.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                onAnalyzeDrop2();
            }
        });
        dropAnalyseButton2.setText("物品掉率分析");

        comboAnalyzeLevel = new Combo(composite, SWT.READ_ONLY);
        comboAnalyzeLevel.setVisibleItemCount(50);
        comboAnalyzeLevel.setItems(new String[] {"1级", "2级", "3级", "4级", "5级", "6级", "7级", "8级", "9级", "10级", "11级", "12级", "13级", "14级", "15级", "16级", "17级", "18级", "19级", "20级", "21级", "22级", "23级", "24级", "25级", "26级", "27级", "28级", "29级", "30级", "31级", "32级", "33级", "34级", "35级", "36级", "37级", "38级", "39级", "40级", "41级", "42级", "43级", "44级", "45级", "46级", "47级", "48级", "49级", "50级", "51级", "52级", "53级", "54级", "55级", "56级", "57级", "58级", "59级", "60级", "61级", "62级", "63级", "64级", "65级", "66级", "67级", "68级", "69级", "70级", "71级", "72级", "73级", "74级", "75级", "76级", "77级", "78级", "79级", "80级", "81级", "82级", "83级", "84级", "85级", "86级", "87级", "88级", "89级", "90级", "91级", "92级", "93级", "94级", "95级", "96级", "97级", "98级", "99级", "100级"});
        comboAnalyzeLevel.select(0);
        final GridData gd_comboAnalyzeLevel = new GridData(SWT.FILL, SWT.CENTER, true, false);
        comboAnalyzeLevel.setLayoutData(gd_comboAnalyzeLevel);

        comboAnalyzeClazz = new Combo(composite, SWT.READ_ONLY);
        comboAnalyzeClazz.setItems(new String[] {"武将", "刺客", "谋士", "方士"});
        comboAnalyzeClazz.select(0);
        final GridData gd_comboAnalyzeClazz = new GridData(SWT.FILL, SWT.CENTER, true, false);
        comboAnalyzeClazz.setLayoutData(gd_comboAnalyzeClazz);
        
        updateView();
        
        setDirty(false);
        setPartName(this.getEditorInput().getName());
        saveStateToUndoBuffer();
    }
    
    private void updateView() {
        // 设置初始值
        NPCTemplate dataDef = (NPCTemplate)editObject;
        textID.setText(String.valueOf(dataDef.id));
        textTitle.setText(dataDef.title);
        textDescription.setText(dataDef.description);
        comboTypeCtrl.select(dataDef.owner.getDictObjectIndex(dataDef.type));
        if (dataDef.image != null) {
            comboImageCtrl.select(dataDef.owner.getObjectIndex(dataDef.image));
        }
        if (dataDef.type.id == 3) {
            comboLevel.select(dataDef.level);
            textQuestId.setText(String.valueOf(dataDef.questId));
            textCollectParam.setText(String.valueOf(dataDef.collectParam));
            textCollectTime.setText(String.valueOf(dataDef.collectTime));
        } else {
            comboClazz.select(dataDef.clazz);
            buttonPowerful.setSelection(dataDef.powerful);
            comboLevel.select(dataDef.level);
            
            textHP.setText(String.valueOf(dataDef.getStandardHP()));
            textHP2.setText(String.valueOf(dataDef.hp - dataDef.getStandardHP()));
            textMP.setText(String.valueOf(dataDef.getStandardMP()));
            textMP2.setText(String.valueOf(dataDef.mp - dataDef.getStandardMP()));
            textArmor.setText(String.valueOf(dataDef.getStandardArmor()));
            textArmor2.setText(String.valueOf(dataDef.armor - dataDef.getStandardArmor()));
            textMagicArmor.setText(String.valueOf(dataDef.getStandardMagicArmor()));
            textMagicArmor2.setText(String.valueOf(dataDef.magicArmor - dataDef.getStandardMagicArmor()));
            textSTR.setText(String.valueOf(dataDef.getStandardSTR()));
            textSTR2.setText(String.valueOf(dataDef.str - dataDef.getStandardSTR()));
            textSTA.setText(String.valueOf(dataDef.getStandardSTA()));
            textSTA2.setText(String.valueOf(dataDef.sta - dataDef.getStandardSTA()));
            textINT.setText(String.valueOf(dataDef.getStandardINT()));
            textINT2.setText(String.valueOf(dataDef.inte - dataDef.getStandardINT()));
            textAGI.setText(String.valueOf(dataDef.getStandardAGI()));
            textAGI2.setText(String.valueOf(dataDef.agi - dataDef.getStandardAGI()));
            textPhyAP.setText(dataDef.getStandardWeaponAP1() + "-" + dataDef.getStandardWeaponAP2());
            textPhyAP2.setText(String.valueOf(dataDef.weaponAP2 - dataDef.getStandardWeaponAP2()));
            textMagicAP.setText(String.valueOf(dataDef.getStandardWeaponMagicAP()));
            textMagicAP2.setText(String.valueOf(dataDef.weaponMagicAP - dataDef.getStandardWeaponMagicAP()));
            
            textAI.setText(dataDef.getAIClassDesc());
            textSpeed.setText(String.valueOf(dataDef.speed));
            textWalkSpeed.setText(String.valueOf(dataDef.walkSpeed));
            textEyeshot.setText(String.valueOf(dataDef.eyeshot / 8.0));
            textChaseDistance.setText(String.valueOf(dataDef.chaseDistance / 8.0));
        }
        textMoney.setText(String.valueOf(dataDef.getStandardMoney()));
        textMoney2.setText(String.valueOf(dataDef.money - dataDef.getStandardMoney()));
        textExp.setText(String.valueOf(dataDef.getStandardExp()));
        textExp2.setText(String.valueOf(dataDef.exp - dataDef.getStandardExp()));
        textCredit.setText(String.valueOf(dataDef.credit));
        dropGroupTable.setInput(dataDef.dropGroups);
        setType(dataDef.type);
        updatePreviewer();
    }
    
    private void setType(NPCType type){
        NPCTemplate dataDef = (NPCTemplate)editObject;
        boolean flag = (type.id != 3);
        if (!flag) {
            textMoney.setText("0");
            textExp.setText("0");
        } else {
            textMoney.setText(String.valueOf(dataDef.getStandardMoney()));
            textExp.setText(String.valueOf(dataDef.getStandardExp()));
        }
        
        comboClazz.setEnabled(flag);
        buttonPowerful.setEnabled(flag);
//        comboLevel.setEnabled(flag);
        textHP.setEnabled(flag);
        textHP2.setEnabled(flag);
        textMP.setEnabled(flag);
        textMP2.setEnabled(flag);
        textArmor.setEnabled(flag);
        textArmor2.setEnabled(flag);
        textMagicArmor.setEnabled(flag);
        textMagicArmor2.setEnabled(flag);
        textSTR.setEnabled(flag);
        textSTR2.setEnabled(flag);
        textSTA.setEnabled(flag);
        textSTA2.setEnabled(flag);
        textINT.setEnabled(flag);
        textINT2.setEnabled(flag);
        textAGI.setEnabled(flag);
        textAGI2.setEnabled(flag);
        textPhyAP.setEnabled(flag);
        textPhyAP2.setEnabled(flag);
        textMagicAP.setEnabled(flag);
        textMagicAP2.setEnabled(flag);
        textAI.setEnabled(flag);
        textSpeed.setEnabled(flag);
        textWalkSpeed.setEnabled(flag);
        textEyeshot.setEnabled(flag);
        textChaseDistance.setEnabled(flag);
        textCollectParam.setEnabled(!flag);
        textCollectTime.setEnabled(!flag);
        textQuestId.setEnabled(!flag);
    }
    
    private void setLevel(int level) {
        NPCTemplate dataDef = (NPCTemplate)editObject;
        dataDef.level = level;
        textHP.setText(String.valueOf(dataDef.getStandardHP()));
        textMP.setText(String.valueOf(dataDef.getStandardMP()));
        textArmor.setText(String.valueOf(dataDef.getStandardArmor()));
        textMagicArmor.setText(String.valueOf(dataDef.getStandardMagicArmor()));
        textSTR.setText(String.valueOf(dataDef.getStandardSTR()));
        textSTA.setText(String.valueOf(dataDef.getStandardSTA()));
        textINT.setText(String.valueOf(dataDef.getStandardINT()));
        textAGI.setText(String.valueOf(dataDef.getStandardAGI()));
        textMoney.setText(String.valueOf(dataDef.getStandardMoney()));
        textExp.setText(String.valueOf(dataDef.getStandardExp()));
        textPhyAP.setText(String.valueOf(dataDef.getStandardWeaponAP1() + "-" + dataDef.getStandardWeaponAP2()));
        textMagicAP.setText(String.valueOf(dataDef.getStandardWeaponMagicAP()));
    }
    
    private void setClazz(int clazz) {
        NPCTemplate dataDef = (NPCTemplate)editObject;
        dataDef.clazz = clazz;
        textHP.setText(String.valueOf(dataDef.getStandardHP()));
        textMP.setText(String.valueOf(dataDef.getStandardMP()));
        textArmor.setText(String.valueOf(dataDef.getStandardArmor()));
        textMagicArmor.setText(String.valueOf(dataDef.getStandardMagicArmor()));
        textSTR.setText(String.valueOf(dataDef.getStandardSTR()));
        textSTA.setText(String.valueOf(dataDef.getStandardSTA()));
        textINT.setText(String.valueOf(dataDef.getStandardINT()));
        textAGI.setText(String.valueOf(dataDef.getStandardAGI()));
        textMoney.setText(String.valueOf(dataDef.getStandardMoney()));
        textExp.setText(String.valueOf(dataDef.getStandardExp()));
        textPhyAP.setText(String.valueOf(dataDef.getStandardWeaponAP1() + "-" + dataDef.getStandardWeaponAP2()));
        textMagicAP.setText(String.valueOf(dataDef.getStandardWeaponMagicAP()));
    }
    
    /**
     * 为NPC新增一个掉落组
     */
    private void onAdd(){
        ChooseDropGroupDialog dropDialog = new ChooseDropGroupDialog(getSite().getShell());
        if(dropDialog.open() == IDialogConstants.OK_ID){
            DropNode node = (DropNode)dropDialog.getSelectedObject().duplicate();
            
            NPCTemplate dataDef = (NPCTemplate)editObject;
            dataDef.dropGroups.add(node);
            dropGroupTable.refresh();
            
            setDirty(true);
        }
    }
    
    /**
     * 为NPC删除一个掉落组
     */
    private void onDelete(){
        StructuredSelection selected = (StructuredSelection)dropGroupTable.getSelection();
        Object[] sels = selected.toArray();
        for (int i = 0; i < sels.length; i++) {
            DropNode selGroup = (DropNode)sels[i];
            NPCTemplate dataDef = (NPCTemplate)editObject;
            dataDef.dropGroups.remove(selGroup);
            dropGroupTable.refresh();
            setDirty(true);
        }
    }
    
    /**
     * 列表双击事件
     */
    private void onDoubleClick(Object selected){
        StructuredSelection sel = (StructuredSelection)selected;
        DropNode selGroup = (DropNode)sel.getFirstElement();
        
        ChooseDropGroupDialog dropDialog = new ChooseDropGroupDialog(getSite().getShell());
        dropDialog.setSelectedItem(selGroup);
        if(dropDialog.open() == IDialogConstants.OK_ID){
            DropNode selNewGroup = dropDialog.getSelectedObject();
            if (selNewGroup.equals(selGroup)) {
                selGroup.update(selNewGroup);
                dropGroupTable.refresh();
                setDirty(true);
            }
        }
    }
    
    /**
     * 创建掉落列表菜单
     */
    private void createActions(){
        addDropGroup = new Action("新增"){
            public void run(){
                onAdd();
            }
        };
        
        delDropGroup = new Action("删除"){
            public void run(){
                onDelete();
            }
        };
    }

    /**
     * 保存当前编辑数据。
     */
    protected void saveData() throws Exception {
        NPCTemplate dataDef = (NPCTemplate)editObject;

        // 读取输入：对象ID、标题、描述、类型、生命、法力、级别、耐力、力量、敏捷、智力、AI
        try {
            dataDef.id = Integer.parseInt(textID.getText());
        } catch (Exception e) {
            throw new Exception("请输入正确的ID。");
        }
        dataDef.title = textTitle.getText().trim();
        dataDef.description = textDescription.getText();
        try {       
            StructuredSelection sel = (StructuredSelection)comboImage.getSelection();
            dataDef.image = (Animation)sel.getFirstElement();
            if (dataDef.image == null) {
                throw new Exception();
            }
        } catch (Exception e) {
            throw new Exception("请选择一个图片。");
        }
        try {
            StructuredSelection sel = (StructuredSelection)comboType.getSelection();
            dataDef.type = (NPCType)sel.getFirstElement();
            if (dataDef.type == null) {
                throw new Exception();
            }
        } catch (Exception e) {
            throw new Exception("请选择一个类型。");
        }
        
        if(dataDef.type.id == 3){
            dataDef.level = comboLevel.getSelectionIndex();
            try {
                dataDef.collectTime = Integer.parseInt(textCollectTime.getText());
            } catch (Exception e) {
                throw new Exception("请输入正确的采集时间。");
            }
            try {
                dataDef.collectParam = Integer.parseInt(textCollectParam.getText());
            } catch (Exception e) {
            }
            
            try {
                dataDef.questId = Integer.parseInt(textQuestId.getText());
            } catch (Exception e) {
                throw new Exception("请输入正确的任务ID。");
            }
            try {
                dataDef.exp = Integer.parseInt(textExp2.getText());
            } catch (Exception e) {
                throw new Exception("请输入正确的经验值。");
            }
            try {
                dataDef.money = Integer.parseInt(textMoney2.getText());
            } catch (Exception e) {
                throw new Exception("请输入正确的金钱。");
            }
        }
        else{
            dataDef.level = comboLevel.getSelectionIndex();
            dataDef.clazz = comboClazz.getSelectionIndex();
            dataDef.powerful = buttonPowerful.getSelection();

            try {
                dataDef.hp = dataDef.getStandardHP() + Integer.parseInt(textHP2.getText());
            } catch (Exception e) {
                throw new Exception("请输入正确的生命。");
            }
            try {
                dataDef.mp = dataDef.getStandardMP() + Integer.parseInt(textMP2.getText());
            } catch (Exception e) {
                throw new Exception("请输入正确的法力。");
            }
            try {
                dataDef.armor = dataDef.getStandardArmor() + Integer.parseInt(textArmor2.getText());
            } catch (Exception e) {
                throw new Exception("请输入正确的法力。");
            }
            try {
                dataDef.magicArmor = dataDef.getStandardMagicArmor() + Integer.parseInt(textMagicArmor2.getText());
            } catch (Exception e) {
                throw new Exception("请输入正确的法力。");
            }
            try {
                dataDef.str = dataDef.getStandardSTR() + Integer.parseInt(textSTR2.getText());
            } catch (Exception e) {
                throw new Exception("请输入正确的力量。");
            }
            try {
                dataDef.sta = dataDef.getStandardSTA() + Integer.parseInt(textSTA2.getText());
            } catch (Exception e) {
                throw new Exception("请输入正确的耐力。");
            }
            try {
                dataDef.agi = dataDef.getStandardAGI() + Integer.parseInt(textAGI2.getText());
            } catch (Exception e) {
                throw new Exception("请输入正确的敏捷。");
            }
            try {
                dataDef.inte = dataDef.getStandardINT() + Integer.parseInt(textINT2.getText());
            } catch (Exception e) {
                throw new Exception("请输入正确的智力。");
            }
            try {
                dataDef.weaponAP1 = dataDef.getStandardWeaponAP1() + Integer.parseInt(textPhyAP2.getText());
                dataDef.weaponAP2 = dataDef.getStandardWeaponAP2() + Integer.parseInt(textPhyAP2.getText());
            } catch (Exception e) {
                throw new Exception("请输入正确的武器物理攻击力。");
            }
            try {
                dataDef.weaponMagicAP = dataDef.getStandardWeaponMagicAP() + Integer.parseInt(textMagicAP2.getText());
            } catch (Exception e) {
                throw new Exception("请输入正确的武器法术攻击力。");
            }
            
            // dataDef.aiClass = textAI.getText().trim();
            
            try {
                dataDef.speed = Integer.parseInt(textSpeed.getText());
            } catch (Exception e) {
                throw new Exception("请输入正确的速度。");
            }
            try {
                dataDef.walkSpeed = Integer.parseInt(textWalkSpeed.getText());
            } catch (Exception e) {
                throw new Exception("请输入正确的速度。");
            }
            try {
                dataDef.eyeshot = (int)(Double.parseDouble(textEyeshot.getText()) * 8);
            } catch (Exception e) {
                throw new Exception("请输入正确的视野。");
            }
            try {
                dataDef.chaseDistance = (int)(Double.parseDouble(textChaseDistance.getText()) * 8);
            } catch (Exception e) {
                throw new Exception("请输入正确的追击距离。");
            }
            try {
                dataDef.exp = dataDef.getStandardExp() + Integer.parseInt(textExp2.getText());
            } catch (Exception e) {
                throw new Exception("请输入正确的经验值。");
            }
            try {
                dataDef.money = dataDef.getStandardMoney() + Integer.parseInt(textMoney2.getText());
            } catch (Exception e) {
                throw new Exception("请输入正确的金钱。");
            }
            try {
                dataDef.credit = Integer.parseInt(textCredit.getText());
            } catch (Exception e) {
                throw new Exception("请输入正确的声望。");
            }
        }
        
        // 检查输入合法性
        DataObject dobj = EditorApplication.getInstance().getProjectData().findObject(dataDef.getClass(), dataDef.id);
        if (dobj != null && dobj != getSaveTarget()) {
            throw new Exception("ID重复，请重新输入。");
        }
        if (dataDef.title.length() == 0) {
            throw new Exception("请输入标题。");
        }
        
        EditAIRuleDialog dlg = new EditAIRuleDialog(getSite().getShell());
        dlg.setEditTarget(dataDef.aiClass, dataDef.aiRules);
        dlg.checkRules();
    }
    
    
    private void updatePreviewer() {
        StructuredSelection sel = (StructuredSelection)comboImage.getSelection();
        Animation imgDef = (Animation)sel.getFirstElement();
        if (imgDef != null) {
            previewer.setAnimateFile(imgDef.source);
        }
    }
    
    private void onAnalyzeDrop1() {
        try {
            DropAnalyzeDialog dlg = new DropAnalyzeDialog(getSite().getShell(), (NPCTemplate)editObject, true, 
                    comboAnalyzeLevel.getSelectionIndex() + 1, comboAnalyzeClazz.getSelectionIndex());
            dlg.open();
        } catch (Exception e) {
            e.printStackTrace();
            MessageDialog.openError(getSite().getShell(), "错误", e.toString());
        }
    }

    private void onAnalyzeDrop2() {
        try {
            DropAnalyzeDialog dlg = new DropAnalyzeDialog(getSite().getShell(), (NPCTemplate)editObject, false,
                    comboAnalyzeLevel.getSelectionIndex() + 1, comboAnalyzeClazz.getSelectionIndex());
            dlg.open();
        } catch (Exception e) {
            e.printStackTrace();
            MessageDialog.openError(getSite().getShell(), "错误", e.toString());
        }
    }
    
    private void setDefaultDrop() {
        NPCTemplate dataDef = (NPCTemplate)editObject;
        
        for (int i = 0; i < dataDef.dropGroups.size(); i++) {
            DropNode dnode = dataDef.dropGroups.get(i);
            if (dnode.type == DropNode.TYPE_DROPGROUP) {
                dataDef.dropGroups.remove(i);
                i--;
            }
        }
        
        int[] config = EditorApplication.getProj().npcTemplateConfig.getDropConfig(dataDef.level, dataDef.powerful);
        if (config != null) {
            for (int i = 0; i < config.length; i += 2) {
                int gid = config[i];
                int rate = config[i + 1];
                DropNode dnode = new DropNode(DropNode.TYPE_DROPGROUP);
                dnode.id = gid;
                dnode.quantityMax = 1;
                dnode.quantityMin = 1;
                dnode.dropRate = rate * 100;
                dnode.isTask = false;
                dnode.taskId = -1;
                dataDef.dropGroups.add(dnode);
                dropGroupTable.refresh();
            }
        }
        setDirty(true);
    }
}
