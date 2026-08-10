package com.pip.sanguo.editor.ai;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.pip.propertysheet.PropertySheetEntry;
import com.pip.propertysheet.PropertySheetViewer;
import com.pip.sanguo.data.item.ItemEffect;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;
import com.pip.sanguo.data.quest.pqe.PQEUtils;
import com.pip.sanguo.data.skill.SkillConfig;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.item.ItemEffectPropertySource;
import com.pip.sanguo.editor.property.ItemPropertyDescriptor;
import com.pip.sanguo.editor.property.QuestPropertyDescriptor;
import com.pip.sanguo.editor.property.SkillPropertyDescriptor;

public class EditAIRuleDialog extends Dialog {
    class RuleListCellModifier implements ICellModifier {
        public boolean canModify(Object element, String property) {
            return true;
        }
        
        public Object getValue(Object element, String property) {
            if (element instanceof String) {
                return new Integer(0);
            } else {
                int index = ((Integer)element).intValue();
                AIRuleConfig rule = rules.get(index);
                if (rule instanceof EscapeRuleConfig) {
                    return new Integer(1);
                } else if (rule instanceof SkillAttackRuleConfig) {
                    return new Integer(2);
                } else if (rule instanceof SummonRuleConfig) {
                    return new Integer(3);
                } else if (rule instanceof WalkShoutRuleConfig) {
                    return new Integer(4);
                }
                return null;
            }
        }

        public void modify(Object element, String property, Object value) {
            TableItem ti = (TableItem)element;
            int newType = ((Integer)value).intValue();
            if (ti.getData() instanceof String) {
                if (newType != 0) {
                    if (newType == 1) {
                        rules.add(new EscapeRuleConfig());
                    } else if (newType == 2) {
                        rules.add(new SkillAttackRuleConfig());
                    } else if (newType == 3) {
                        rules.add(new SummonRuleConfig());
                    } else if (newType == 4) {
                        rules.add(new WalkShoutRuleConfig());
                    }
                    ruleListViewer.refresh();
                }
            } else {
                int index = ((Integer)ti.getData()).intValue();
                if (newType == 0) {
                    rules.remove(index);
                    ruleListViewer.refresh();
                } else {
                    AIRuleConfig oldRule = rules.get(index);
                    if (newType == 1) {
                        if (!(oldRule instanceof EscapeRuleConfig)) {
                            rules.set(index, new EscapeRuleConfig());
                        }
                    } else if (newType == 2) {
                        if (!(oldRule instanceof SkillAttackRuleConfig)) {
                            rules.set(index, new SkillAttackRuleConfig());
                        }
                    } else if (newType == 3) {
                        if (!(oldRule instanceof SummonRuleConfig)) {
                            rules.set(index, new SummonRuleConfig());
                        }
                    } else if (newType == 4) {
                        if (!(oldRule instanceof WalkShoutRuleConfig)) {
                            rules.set(index, new WalkShoutRuleConfig());
                        }
                    }
                    ruleListViewer.update(ti.getData(), null);
                }
            }
        }
    }
    class RuleListContentProvider implements IStructuredContentProvider {
        public Object[] getElements(Object inputElement) {
            Object[] ret = new Object[rules.size() + 1];
            for (int i = 0; i < rules.size(); i++) {
                ret[i] = new Integer(i);
            }
            ret[rules.size()] = "";
            return ret;
        }
        public void dispose() {
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }
    class RuleListLabelProvider extends LabelProvider implements ITableLabelProvider {
        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof String) {
                return "新规则...";
            }
            int index = ((Integer)element).intValue();
            return rules.get(index).toString();
        }
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }
    }
    
    private Table ruleList;
    private Combo comboType;
    private TableViewer ruleListViewer;
    private PropertySheetViewer propertyEditor;

    private int aiType;
    private List<AIRuleConfig> rules = new ArrayList<AIRuleConfig>();
    
    /**
     * Create the dialog
     * @param parentShell
     */
    public EditAIRuleDialog(Shell parentShell) {
        super(parentShell);
    }
    
    public void setEditTarget(String clsName, List<AIRuleConfig> rules) {
        if (clsName.length() == 0) {
            aiType = 0;
        } else if (clsName.equals("empty")) {
            aiType = 1;
        } else if (clsName.equals("gohome")) {
            aiType = 2;
        } else if (clsName.equals("passive")) {
            aiType = 3;
        } else if (clsName.equals("general")) {
            aiType = 0;
        } else {
            throw new IllegalArgumentException("不支持的AI类型");
        }
        this.rules = rules;
    }
    
    public void checkRules() throws Exception {
        for (AIRuleConfig rule : rules) {
            if (rule instanceof SkillAttackRuleConfig) {
                SkillAttackRuleConfig sarc = (SkillAttackRuleConfig)rule;
                SkillConfig skill = (SkillConfig)EditorApplication.getProj().findObject(SkillConfig.class, sarc.skill);
                if (skill == null) {
                    throw new Exception("技能不存在：" + sarc.skill);
                }
                if (sarc.skillLevel <= 0 || sarc.skillLevel > skill.maxLevel) {
                    throw new Exception("技能" + skill.title + "级别设置错误：" + sarc.skillLevel);
                }
                if (sarc.skill2 != 0) {
                    skill = (SkillConfig)EditorApplication.getProj().findObject(SkillConfig.class, sarc.skill2);
                    if (skill == null) {
                        throw new Exception("技能不存在：" + sarc.skill2);
                    }
                    if (sarc.skillLevel2 <= 0 || sarc.skillLevel2 > skill.maxLevel) {
                        throw new Exception("技能" + skill.title + "级别设置错误：" + sarc.skillLevel2);
                    }
                }
                if (sarc.skill3 != 0) {
                    skill = (SkillConfig)EditorApplication.getProj().findObject(SkillConfig.class, sarc.skill3);
                    if (skill == null) {
                        throw new Exception("技能不存在：" + sarc.skill3);
                    }
                    if (sarc.skillLevel3 <= 0 || sarc.skillLevel3 > skill.maxLevel) {
                        throw new Exception("技能" + skill.title + "级别设置错误：" + sarc.skillLevel3);
                    }
                }
            } else if (rule instanceof SummonRuleConfig) {
                SummonRuleConfig src = (SummonRuleConfig)rule;
                for (int i = 0; i < src.monsters.length; i++) {
                    if (src.monsters[i] == 0) {
                        continue;
                    }
                    GameMapObject gmo = GameMapObject.findByID(EditorApplication.getProj(), src.monsters[i]);
                    if (gmo == null || !(gmo instanceof GameMapNPC)) {
                        throw new Exception("召唤目标不存在：" + src.monsters[i]);
                    }
                }
            }
        }
    }
    
    public String getAIClass() {
        switch (aiType) {
        case 0:
            return "general";
        case 1:
            return "empty";
        case 2:
            return "gohome";
        case 3:
            return "passive";
        default:
            return "general";
        }
    }

    public List<AIRuleConfig> getAIRules() {
        return rules;
    }
    
    /**
     * Create contents of the dialog
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        container.setLayout(gridLayout);

        final Label label = new Label(container, SWT.NONE);
        label.setText("AI类型：");

        comboType = new Combo(container, SWT.READ_ONLY);
        comboType.setItems(new String[] {"带简单规则的通用AI", "无任何动作", "回家AI", "被动挨打"});
        final GridData gd_comboType = new GridData(SWT.FILL, SWT.CENTER, true, false);
        comboType.setLayoutData(gd_comboType);
        comboType.select(aiType);
        comboType.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                int sel = comboType.getSelectionIndex();
                if (sel == 1) {
                    ruleList.setEnabled(false);
                    propertyEditor.getControl().setEnabled(false);
                } else {
                    ruleList.setEnabled(true);
                    propertyEditor.getControl().setEnabled(true);
                }
            }
        });

        final Composite composite = new Composite(container, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
        final GridLayout gridLayout_1 = new GridLayout();
        gridLayout_1.marginHeight = 0;
        gridLayout_1.marginWidth = 0;
        gridLayout_1.numColumns = 2;
        composite.setLayout(gridLayout_1);

        ruleListViewer = new TableViewer(composite, SWT.FULL_SELECTION | SWT.BORDER);
        ruleListViewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(final SelectionChangedEvent event) {
                int index = ruleList.getSelectionIndex();
                if (index < 0 || index >= rules.size()) {
                    propertyEditor.setInput(new Object[0]);
                } else {
                    propertyEditor.setInput(new Object[]{ new AIRuleConfigPropertySource(rules.get(index)) });
                }
            }
        });
        ruleListViewer.setContentProvider(new RuleListContentProvider());
        ruleListViewer.setLabelProvider(new RuleListLabelProvider());
        ruleList = ruleListViewer.getTable();
        ruleList.setLinesVisible(true);
        ruleList.setHeaderVisible(true);
        final GridData gd_ruleList = new GridData(SWT.FILL, SWT.FILL, true, true);
        ruleList.setLayoutData(gd_ruleList);

        final TableColumn ruleColumn = new TableColumn(ruleList, SWT.NONE);
        ruleColumn.setWidth(141);
        ruleColumn.setText("规则列表");

        new TableColumn(ruleList, SWT.NONE);
        
        ruleListViewer.setColumnProperties(new String[] {
                "c0"
        });
        ruleListViewer.setCellModifier(new RuleListCellModifier());
        ruleListViewer.setCellEditors(new CellEditor[] {
                new ComboBoxCellEditor(ruleList, new String[] {
                    "删除", "逃跑", "施放技能", "召唤", "到指定位置喊话"
                }) {
                    public int getStyle() {
                        return SWT.READ_ONLY;
                    }
                }
        });
        ruleListViewer.setInput(this);

        final Composite paramComposite = new Composite(composite, SWT.NONE);
        final GridData gd_paramComposite = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd_paramComposite.widthHint = 200;
        paramComposite.setLayoutData(gd_paramComposite);
        paramComposite.setLayout(new FillLayout());
        
        propertyEditor = new PropertySheetViewer(paramComposite, SWT.NONE, true);
        PropertySheetEntry rootEntry = new PropertySheetEntry();
        propertyEditor.setRootEntry(rootEntry);
        
        if (aiType == 1) {
            ruleList.setEnabled(false);
            propertyEditor.getControl().setEnabled(false);
        } else {
            ruleList.setEnabled(true);
            propertyEditor.getControl().setEnabled(true);
        }
                
        //
        return container;
    }

    /**
     * Create contents of the button bar
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, "确定", true);
        createButton(parent, IDialogConstants.CANCEL_ID, "取消", false);
    }

    /**
     * Return the initial size of the dialog
     */
    @Override
    protected Point getInitialSize() {
        return new Point(582, 436);
    }
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("编辑AI规则");
    }
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            aiType = comboType.getSelectionIndex();
        }
        super.buttonPressed(buttonId);
    }
}
