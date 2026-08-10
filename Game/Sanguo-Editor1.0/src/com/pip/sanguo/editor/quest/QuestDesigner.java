package com.pip.sanguo.editor.quest;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;

import com.pip.propertysheet.PropertySheetEntry;
import com.pip.propertysheet.PropertySheetViewer;
import com.pip.sanguo.data.quest.Quest;
import com.pip.sanguo.data.quest.QuestInfo;
import com.pip.sanguo.data.quest.QuestVariable;
import com.pip.sanguo.data.quest.pqe.PQEUtils;
import com.pip.sanguo.data.quest.pqe.ParserConstants;
import com.pip.sanguo.editor.quest.expr.*;
import com.pip.sanguo.editor.quest.flow.ActionNode;
import com.pip.sanguo.editor.quest.flow.ConditionNode;
import com.pip.sanguo.editor.quest.flow.FlowNode;

public class QuestDesigner extends Composite implements DragSourceListener, IExprListener {
	private static final String ADD_NEW_VAR = "新建变量...";
	private static final String TASK_VAR_NODE = "任务变量";
	
    /**
	 * 左边模板树的数据。
	 * @author lighthu
	 */
	class TemplateTreeContentProvider implements IStructuredContentProvider, ITreeContentProvider {
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
		public void dispose() {}
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}
		public Object[] getChildren(Object parentElement) {
			if (parentElement == questInfo) {
				// 根节点是所有的模板类型
				Object[] ret = new Object[TemplateManager.TEMPLATE_TYPES.length + 1];
				System.arraycopy(TemplateManager.TEMPLATE_TYPES, 0, ret, 0, TemplateManager.TEMPLATE_TYPES.length);
				ret[TemplateManager.TEMPLATE_TYPES.length] = TASK_VAR_NODE;
				return ret;
			} else if (parentElement == TASK_VAR_NODE) {
				// 任务变量列表
				Object[] ret = new Object[questInfo.variables.size() + 1];
				for (int i = 0; i < questInfo.variables.size(); i++) {
					ret[i] = new C_LocalVar(questInfo.variables.get(i).name);
				}
				ret[questInfo.variables.size()] = ADD_NEW_VAR;
				return ret;
			} else {
				// 其他类型
				for (int i = 0; i < TemplateManager.TEMPLATE_TYPES.length; i++) {
					if (TemplateManager.TEMPLATE_TYPES[i] == parentElement) {
						return TemplateManager.TEMPLATES[i];
					}
				}
			}
			return new Object[0];
		}
		public Object getParent(Object element) {
			if (ADD_NEW_VAR.equals(element)) {
				return TASK_VAR_NODE;
			} else if (element instanceof String) {
				return questInfo;
			} else if (element instanceof C_LocalVar) {
				return TASK_VAR_NODE;
			} else {
				for (int i = 0; i < TemplateManager.TEMPLATE_TYPES.length; i++) {
					for (IExpr template : TemplateManager.TEMPLATES[i]) {
						if (template == element) {
							return TemplateManager.TEMPLATE_TYPES[i];
						}
					}
				}
			}
			return null;
		}
		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}
	}
	
	private Tree templateTree;
	private TreeViewer templateTreeViewer;
	private PropertySheetViewer propEditor;
	private QuestFlowViewer flowViewer;
	
	private QuestInfo questInfo;
	private int editMode;          // 0 - 正常模式，1 - 用于编辑条件
    private Button flatModeButton;
	
	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public QuestDesigner(Composite parent, int style, QuestInfo qi) {
		super(parent, style);
		this.questInfo = qi; 
		
		final GridLayout gridLayout = new GridLayout();
		gridLayout.verticalSpacing = 0;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = 0;
		gridLayout.numColumns = 2;
		setLayout(gridLayout);

		templateTreeViewer = new TreeViewer(this, SWT.FULL_SELECTION | SWT.BORDER);
		templateTreeViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(final DoubleClickEvent event) {
        		Object selObj = getSelectedTemplate();
        		if (selObj != null) {
        			if (selObj.equals(ADD_NEW_VAR)) {
        				onNewVar();
        			} else if (selObj instanceof String) {
						if (templateTreeViewer.getExpandedState(selObj)) {
							templateTreeViewer.collapseToLevel(selObj, 1);
						} else {
							templateTreeViewer.expandToLevel(selObj, 1);
						}
        			}
        		}
			}
		});
		templateTreeViewer.setContentProvider(new TemplateTreeContentProvider());
		templateTree = templateTreeViewer.getTree();
		final GridData gd_templateTree = new GridData(SWT.FILL, SWT.FILL, false, true);
		gd_templateTree.widthHint = 242;
		templateTree.setLayoutData(gd_templateTree);
		templateTree.addListener(SWT.KeyDown, new Listener() {
            public void handleEvent(Event event) {
            	if (event.keyCode == SWT.DEL) {
            		Object selObj = getSelectedTemplate();
	        		if (selObj != null) {
	        			if (selObj instanceof C_LocalVar) {
	        				onDelVar(((C_LocalVar)selObj).getName());
	        			}
	        		}
            	}
            }
        });
		templateTreeViewer.setInput(questInfo);

	    final ScrolledComposite scrolledComposite = new ScrolledComposite(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
	    scrolledComposite.getVerticalBar().setPageIncrement(400);
	    scrolledComposite.getVerticalBar().setMaximum(1000);
	    scrolledComposite.getHorizontalBar().setMaximum(1000);
	    scrolledComposite.getHorizontalBar().setPageIncrement(400);
	    scrolledComposite.getVerticalBar().setIncrement(200);
	    scrolledComposite.getHorizontalBar().setIncrement(200);
	    scrolledComposite.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
	    scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 3));

		flowViewer = new QuestFlowViewer(scrolledComposite, questInfo, this);
		scrolledComposite.setContent(flowViewer);
		flowViewer.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                // 当FlowViewer中选中节点改变时，设置新节点的属性编辑器
                FlowNode node = (FlowNode)event.data;
                if (node != null && node instanceof ConditionNode) {
                    ((ConditionNode)node).getCondition().addListener(QuestDesigner.this);
                    propEditor.setInput(new Object[] { ((ConditionNode)node).getCondition() });
                } else if (node != null && node instanceof ActionNode) {
                    ((ActionNode)node).getAction().addListener(QuestDesigner.this);
                    propEditor.setInput(new Object[] { ((ActionNode)node).getAction() });
                } else {
                    propEditor.setInput(new Object[0]);
                }
            }
        });
		flowViewer.addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event event) {
                // 当FlowViewer里的内容改变时，通知Editor设置Dirty标志
                notifyModified();
            }
        });

		final Composite composite_1 = new Composite(this, SWT.NONE);
		final GridData gd_composite_1 = new GridData(SWT.FILL, SWT.FILL, false, false);
		gd_composite_1.heightHint = 240;
		composite_1.setLayoutData(gd_composite_1);
		composite_1.setLayout(new FillLayout());
		
        propEditor = new PropertySheetViewer(composite_1, SWT.NONE | SWT.BORDER, false);
        PropertySheetEntry rootEntry = new PropertySheetEntry();
        propEditor.setRootEntry(rootEntry);

        // 设置DragAndDrop
        final DragSource treeDragSource = new DragSource(templateTree, DND.DROP_COPY);
		treeDragSource.addDragListener(this);
		treeDragSource.setTransfer(new Transfer[] { TextTransfer.getInstance() });

	    flatModeButton = new Button(this, SWT.CHECK);
	    flatModeButton.addSelectionListener(new SelectionAdapter() {
	        public void widgetSelected(final SelectionEvent e) {
	            flowViewer.setFlatShow(flatModeButton.getSelection());
	        }
	    });
	    flatModeButton.setText("回答问题单独显示");
	}
	
	/**
	 * 设置编辑模式。
	 * @param mode 编辑模式
	 * @param initParam 初始公式
	 */
	public void setup(int mode, String initParam) {
	    if (mode == 0) {
	        flowViewer.loadFlowsFromQuest();
	    } else if (mode == 1) {
	        flowViewer.loadFlowsFromExpression(initParam);
	    }
	}
	
	/**
	 * 重新载入任务信息。
	 */
	public void reloadQuest() {
	    flowViewer.loadFlowsFromQuest();
	    flowViewer.layout();
	    flowViewer.redraw();
	    templateTreeViewer.refresh();
	}
	
	/**
	 * 保存任务之前，先进行一次合法性校验检查。
	 */
	public boolean preSaveCheck() {
	    return flowViewer.preSaveCheck();
	}
	
	/**
     * 保存任务。
     */
    public void saveQuest() {
        flowViewer.saveQuest();
    }
    
    /**
     * 条件编辑模式下，保存条件。
     */
    public String saveCondition() {
        return flowViewer.saveCondition();
    }

    /**
     * 任务中表达式参数改变通知处理。
     */
    public void valueChanged(IExpr source) {
        if (source instanceof AbstractNotifyAction) {
            flowViewer.handleNotifyActionChanged(source);
        }
        flowViewer.layout();
        flowViewer.redraw();
        notifyModified();
    }
    
	// 取得模板树中当前选择的节点。如果没有选中任何节点，返回null。
	private Object getSelectedTemplate() {
		IStructuredSelection sel = (IStructuredSelection)templateTreeViewer.getSelection();
		if (sel.isEmpty()) {
			return null;
		} else {
			return sel.getFirstElement();
		}
	}
	
	/**
	 * 取得模板树中当前选中的表达式模板。如果没有选中任何节点，或者选中的不是表达式模板，返回null。
	 */
	public IExpr getSelectedExpr() {
	    Object obj = getSelectedTemplate();
	    if (obj != null && obj instanceof IExpr) {
	        return (IExpr)obj;
	    }
	    return null;
	}
	
	// 新建一个任务变量
	private void onNewVar() {
		InputDialog dlg = new InputDialog(getShell(), "新建变量", "请输入变量名称：", "var", null);
		if (dlg.open() == InputDialog.OK) {
			String name = dlg.getValue().trim();
			if (name.length() == 0) {
				return;
			}
			if (name.startsWith("_")) {
				MessageDialog.openError(getShell(), "错误", "只有系统变量能用下划线开头。");
				return;
			}
			if (name.startsWith("$")) {
			    MessageDialog.openError(getShell(), "错误", "$符号被保留为引用其他任务的变量。");
                return;
			}
            if (name.startsWith("v")) {
                MessageDialog.openError(getShell(), "错误", "v符号被保留为通过索引引用局部变量。");
                return;
            }
			if (name.indexOf(' ') != -1) {
				MessageDialog.openError(getShell(), "错误", "变量名不能包含空格。");
				return;
			}
			for (QuestVariable var : questInfo.variables) {
				if (var.name.equals(name)) {
					MessageDialog.openError(getShell(), "错误", "变量" + name + "已经存在了。");
					return;
				}
			}
			QuestVariable newvar = new QuestVariable();
			newvar.name = name;
			questInfo.variables.add(newvar);
			templateTreeViewer.refresh(TASK_VAR_NODE);
            notifyModified();
		}
	}
	
	// 删除选中的任务变量
	private void onDelVar(String name) {
		int index = -1;
		for (int i = 0; i < questInfo.variables.size(); i++) {
			if (questInfo.variables.get(i).name.equals(name)) {
				index = i;
				break;
			}
		}
		if (index == -1) {
			return;
		}
		questInfo.variables.remove(index);
		templateTreeViewer.refresh(TASK_VAR_NODE);
        notifyModified();
	}
	
	// 通知监听者任务内容已改变
	private void notifyModified() {
	    Event evt = new Event();
	    evt.widget = this;
	    this.notifyListeners(SWT.Modify, evt);
	}

	// 下面的方法实现从模板树中拖出数据的接口。
	
	/**
	 * 拖动开始事件。
	 */
	public void dragStart(DragSourceEvent event) {
		Object selObj = getSelectedTemplate();
		if (selObj != null && selObj instanceof IExpr) {
			event.doit = true;
		} else {
			event.doit = false;
		}
	}
	
	/**
	 * 设置拖动数据。
	 */
	public void dragSetData(DragSourceEvent event) {
		Object selObj = getSelectedTemplate();
		if (selObj != null && selObj instanceof IExpr) {
			IExpr template = (IExpr)selObj;
			event.data = template.createNew(questInfo).getExpression();
		}
	}
	
	/**
	 * 拖动结束事件。不做任何操作。
	 */
	public void dragFinished(DragSourceEvent event) {}
}
