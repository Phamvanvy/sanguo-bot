package com.pip.sanguo.editor.quest;

import java.util.*;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.pip.sanguo.data.quest.QuestInfo;
import com.pip.sanguo.data.quest.QuestTrigger;
import com.pip.sanguo.data.quest.pqe.ExpressionList;
import com.pip.sanguo.editor.quest.expr.A_Empty;
import com.pip.sanguo.editor.quest.expr.AbstractNotifyAction;
import com.pip.sanguo.editor.quest.expr.C_True;
import com.pip.sanguo.editor.quest.expr.IExpr;
import com.pip.sanguo.editor.quest.expr.TemplateManager;
import com.pip.sanguo.editor.quest.flow.*;
import com.pip.util.SWTUtils;
import com.swtdesigner.SWTResourceManager;

public class QuestFlowViewer extends Canvas implements DropTargetListener, PaintListener {
    private static final int BOX_WIDTH = 130;
    private static final int X_MARGIN = 40;
    
    // 任务信息
    private QuestInfo questInfo;
    // 所有流程
    private List<Flow> flows = new ArrayList<Flow>();
    // 当前所有流程占据的大小
    private Rectangle bestSize;
    // 当前鼠标浮动上的节点
    private FlowNode hoverNode;
    // 当前是否正在拖动模板
    private boolean isDraggingTemplate;
    // 当前选中的节点
    private FlowNode selectedNode;
    // 缓存
    private Image bufferImg;
    // 所属的设计器对象
    private QuestDesigner designer;
    
    // 编辑模式，0 - 普通触发器模式，1 - 编辑条件表达式模式
    private int mode;
    	
	/**
	 * Create the composite
	 * @param parent
	 * @param style
	 */
	public QuestFlowViewer(Composite parent, QuestInfo info, QuestDesigner designer) {
		super(parent, SWT.NO_BACKGROUND);
		setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		addPaintListener(this);
		questInfo = info;
		this.designer = designer;
		
		// 焦点获取
		addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                redraw();
            }

            public void focusLost(FocusEvent e) {
                redraw();
            }
        });
        addTraverseListener(new TraverseListener() {
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_TAB_NEXT
                        || e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
                    e.doit = true;
                }
            };
        });
        
        // 按键处理
        addListener(SWT.KeyDown, new Listener() {
            public void handleEvent(Event event) {
                switch (event.keyCode) {
                    case SWT.DEL:
                        onDelete();
                        break;
                    case SWT.ARROW_UP:
                        traverseUp();
                        break;
                    case SWT.ARROW_RIGHT:
                        traverseRight();
                        break;
                    case SWT.ARROW_LEFT:
                        traverseLeft();
                        break;
                    case SWT.ARROW_DOWN:
                        traverseDown();
                        break;    
                }
            }
        });
        
        // 鼠标移动处理，设置hoverNode
        addMouseMoveListener(new MouseMoveListener() {
            public void mouseMove(MouseEvent e) {
                FlowNode node = findNode(e.x, e.y);
                if (hoverNode != node) {
                    hoverNode = node;
                    isDraggingTemplate = false;
                    redraw();
                }
            }
        });
        
        // 鼠标点击处理，设置selectedNode
        addMouseListener(new MouseAdapter() {
            public void mouseDown(MouseEvent e) {
                if (e.button == 1) {
                    FlowNode node = findNode(e.x, e.y);
                    if (node != null && node != selectedNode) {
                        setSelectedNode(node);
                        redraw();
                    }
                }
            }
            
            public void mouseUp(MouseEvent e) {}
        });
        
        // 设置DND
		final DropTarget templateDropTarget = new DropTarget(this, DND.DROP_COPY);
		templateDropTarget.addDropListener(this);
		templateDropTarget.setTransfer(new Transfer[] { TextTransfer.getInstance() });
		
		setSize(100, 100);
	}
	
	/**
	 * 从当前任务的脚本中解析出任务流程。
	 */
	public void loadFlowsFromQuest() {
	    mode = 0;
        flows = Flow.parseQuest(questInfo);
        if (questInfo.triggers.size() > 0) {
            StringBuffer buf = new StringBuffer();
            buf.append("以下触发器无法被识别(将被删除)：\n");
            for (QuestTrigger trigger : questInfo.triggers) {
                buf.append(trigger.condition + " --> " + trigger.action);
                buf.append("\n");
            }
            MessageDialog.openError(getShell(), "错误", buf.toString());
        }
        selectedNode = null;
        hoverNode = null;
        isDraggingTemplate = false;
	}
	
	/**
	 * 从一个表达式字符串中解析出条件流程。
	 * @param expr
	 */
	public void loadFlowsFromExpression(String expr) {
	    mode = 1;
	    if (expr.trim().length() >= 0) {
    	    QuestTrigger dumbTrigger = new QuestTrigger();
    	    dumbTrigger.condition = expr;
    	    dumbTrigger.action = "1";
    	    Flow flow = Flow.recognize(dumbTrigger, questInfo);
    	    if (flow != null) {
    	        flows.add(flow);
    	    }
	    }
        selectedNode = null;
        hoverNode = null;
        isDraggingTemplate = false;
	}
	
	/**
	 * 对所有任务流程进行重新布局。
	 */
	public void layout() {
	    // 如果是条件编辑模式，进行裁剪，只保留第一个流程的条件部分
	    if (mode == 1) {
	        while (flows.size() > 1) {
	            flows.remove(1);
	        }
	        if (flows.size() > 0) {
    	        ConditionNode startNode = flows.get(0).getStartNode();
    	        while (true) {
    	            while (startNode.getChildren().size() > 0) {
    	                if (startNode.getChildren().get(0) instanceof ConditionNode) {
    	                    while (startNode.getChildren().size() > 1) {
    	                        startNode.getChildren().remove(1);
    	                    }
    	                    break;
    	                } else {
    	                    startNode.getChildren().remove(0);
    	                }
    	            }
    	            if (startNode.getChildren().size() == 0) {
    	                break;
    	            }
    	            startNode = (ConditionNode)startNode.getChildren().get(0);
    	        }
	        }
	    }
	    
	    int y = 14;
	    int x = 14;
	    int maxWidth = 0;
	    
	    // 创建一个临时GC对象，用来计算字符串大小
	    GC bufferGC = null;
        Image tempImg = new Image(getShell().getDisplay(), 1, 1);
        bufferGC = new GC(tempImg);
        bufferGC.setFont(getFont());
	    
        for (Flow flow : flows) {
	        // 对一个流程进行布局
	        layoutNode(flow.getStartNode(), 0, bufferGC);
	        
	        // 计算这个流程的总大小
	        ComputeSizeIterator itor1 = new ComputeSizeIterator();
	        flow.iterate(itor1);
	        Rectangle flowBounds = itor1.getBounds();
	        
	        // 对这个流程的X和Y值进行调整
	        AdjustYIterator itor2 = new AdjustYIterator(y - flowBounds.y);
	        flow.iterate(itor2);
            AdjustXIterator itor3 = new AdjustXIterator(x - flowBounds.x);
            flow.iterate(itor3);
	        
            // 更新统计信息
            y += flowBounds.height + 30;
            if (maxWidth < flowBounds.width) {
                maxWidth = flowBounds.width;
            }
	    }
        int pw = maxWidth + 14 * 2 + 10;
        int ph = y;
        ScrolledComposite scc = findParent();
        if (scc != null) {
            int parw = scc.getBounds().width;
            int parh = scc.getBounds().height;
            if (pw < parw - scc.getVerticalBar().getSize().x - 5) {
                pw = parw - scc.getVerticalBar().getSize().x - 5;
            }
            if (ph < parh - scc.getHorizontalBar().getSize().y - 5) {
                ph = parh - scc.getHorizontalBar().getSize().y - 5;
            }
        }
	    bestSize = new Rectangle(0, 0, pw, ph);
	    this.setSize(bestSize.width, bestSize.height);

	    bufferGC.dispose();
        tempImg.dispose();
	}
	
	/**
	 * 对一个结点及其子结点进行布局，计算最佳显示位置。目前算法每一列的固定宽度为BOX_WIDTH，列之间的距离是X_MARGIN。
	 * @param node 根节点
	 * @param level 根节点在流程中的层数
	 */
	private void layoutNode(FlowNode node, int level, GC gc) {
	    if (node instanceof ActionGroupNode) {     // 动作组节点
	        // 对所有子节点进行单独布局，所有子节点的x位置为0，y位置从0开始递增
	        int y = 0;
	        for (int i = 0; i < node.getChildren().size(); i++) {
	            FlowNode child = node.getChildren().get(i);
	            layoutNode(child, level + 1, gc);
	            ComputeSizeIterator itor1 = new ComputeSizeIterator();
                Flow.iterate(child, itor1);
                Rectangle childBounds = itor1.getBounds();
                
                // 对子节点Y值进行调整
                AdjustYIterator itor2 = new AdjustYIterator(y - childBounds.y);
                Flow.iterate(child, itor2);
                
                // 调整Y值
                y += childBounds.height + 30;
	        }
	        
	        // 动作组节点本身的大小刚够框住所有的一层子节点即可
	        if (node.getChildren().size() == 0) {
	            node.setBounds(new Rectangle(0, 0, BOX_WIDTH, 20));
	        } else {
	            int starty = node.getChildren().get(0).getBounds().y;
	            Rectangle endB = node.getChildren().get(node.getChildren().size() - 1).getBounds();
	            int endy = endB.y + endB.height;
	            node.setBounds(new Rectangle(0, starty, BOX_WIDTH, endy - starty));
	        }
	    } else if (node instanceof ConditionNode || node instanceof ActionNode) {  // 普通节点
	        // 对所有子节点进行单独布局，所有子节点的x位置为BOX_WIDTH+X_MARGIN，y位置从0开始递增
            int y = 0;
            for (int i = 0; i < node.getChildren().size(); i++) {
                FlowNode child = node.getChildren().get(i);
                layoutNode(child, level + 1, gc);
                ComputeSizeIterator itor1 = new ComputeSizeIterator();
                Flow.iterate(child, itor1);
                Rectangle childBounds = itor1.getBounds();
                
                // 对子节点的X和Y值进行调整
                AdjustYIterator itor2 = new AdjustYIterator(y - childBounds.y);
                Flow.iterate(child, itor2);
                AdjustXIterator itor3 = new AdjustXIterator(BOX_WIDTH + X_MARGIN);
                Flow.iterate(child, itor3);
                
                // 调整Y值
                y += childBounds.height + 30;
            }
	        
            // 计算节点本身的大小。如果没有子节点，节点放在0，0即可；如果有子节点，则和子节点对齐。
            String[] lines = SWTUtils.formatText(node.toString(), gc, BOX_WIDTH - 4 * 2);
            int needHei = lines.length * gc.getFontMetrics().getHeight() + 8;
            if (node.getChildren().size() == 0) {
                node.setBounds(new Rectangle(0, 0, BOX_WIDTH, needHei));
            } else {
                int starty = node.getChildren().get(0).getBounds().y;
                Rectangle endB = node.getChildren().get(node.getChildren().size() - 1).getBounds();
                int endy = endB.y + endB.height;
                node.setBounds(new Rectangle(0, (starty + endy) / 2 - needHei / 2, BOX_WIDTH, needHei));
            }
	    }
	}

    /**
     * 绘制控件。
     */
    public void paintControl(PaintEvent e) {
        if (bestSize == null) {
            layout();
        }
        GC bufferGC = null;
        try {
            Point size = getSize();
            if (bufferImg != null && (bufferImg.getBounds().width != size.x || bufferImg.getBounds().height != size.y)) {
                bufferImg.dispose();
                bufferImg = null;
            }
            if (bufferImg == null) {
                bufferImg = new Image(getDisplay(), size.x, size.y);
            }
            bufferGC = new GC(bufferImg);
            bufferGC.setClipping(0, 0, size.x, size.y);
            try {
                paintContent(bufferGC);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            e.gc.drawImage(bufferImg, 0, 0);
        } catch (Throwable e1) {
            e1.printStackTrace();
        } finally {
            if (bufferGC != null) {
                bufferGC.dispose();
            }
        }
    }

    /**
     * 在缓存上绘制内容。
     */
	public void paintContent(GC gc) {
	    Point size = getSize();
	    gc.setBackground(getBackground());
	    gc.setFont(getFont());
	    gc.fillRectangle(0, 0, size.x, size.y);
	    if (isFocusControl()) {
    	    gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
            gc.drawRectangle(0, 0, size.x - 1, size.y - 1);
	    }

	    // 绘制所有的节点
	    PaintIterator itor = new PaintIterator(gc);
	    for (int i = 0; i < flows.size(); i++) {
	        flows.get(i).iterate(itor);
	        
	        if (i != flows.size() - 1) {
    	        // 在流程的下方绘制一条横线来分隔
                ComputeSizeIterator itor1 = new ComputeSizeIterator();
                flows.get(i).iterate(itor1);
                Rectangle flowBounds = itor1.getBounds();
    	        gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
    	        int liney = flowBounds.y + flowBounds.height + 15;
    	        gc.setLineStyle(SWT.LINE_DASH);
    	        gc.drawLine(0, liney, size.x, liney);
    	        gc.setLineStyle(SWT.LINE_SOLID);
	        }
	    }
    }
	
	/**
	 * 保存任务。
	 */
	public void saveQuest() {
	    questInfo.triggers.clear();
	    for (Flow flow : flows) {
	        flow.save(questInfo);
	    }
	}
	
	/**
	 * 条件编辑模式下，保存条件。
	 */
	public String saveCondition() {
	    if (flows.size() == 0) {
	        return "";
	    }
	    StringBuffer buf = new StringBuffer();
	    ConditionNode startNode = flows.get(0).getStartNode();
	    while (true) {
	        if (buf.length() > 0) {
	            buf.append(", ");
	        }
	        buf.append(startNode.getCondition().getExpression());
	        if (startNode.getChildren().size() > 0 && startNode.getChildren().get(0) instanceof ConditionNode) {
	            startNode = (ConditionNode)startNode.getChildren().get(0);
	        } else {
	            break;
	        }
	    }
	    return buf.toString();
	}
	
    // 通知监听者任务内容已改变
    private void notifyModified() {
        Event evt = new Event();
        evt.widget = this;
        this.notifyListeners(SWT.Modify, evt);
    }

    /**
	 * 设置当前选中节点，将会通知所有Selection事件监听者。
	 */
	public void setSelectedNode(FlowNode node) {
	    selectedNode = node;
	    Event evt = new Event();
	    evt.widget = this;
	    evt.data = node;
	    this.notifyListeners(SWT.Selection, evt);
	}
	
	/**
	 * 取得当前选中节点。
	 */
	public FlowNode getSelectedNode() {
	    return selectedNode;
	}
	
	// 删除当前选中的节点。
	private void onDelete() {
	    if (selectedNode == null) {
	        return;
	    }
	    
	    if (selectedNode.getParent() == null) {
            // 如果被删除的是根节点，则把每个子节点都分拆成为一个新的Flow，如果子节点是动作组，则需要
            // 在前面插一个恒真的条件节点。
	        int flowIndex = -1;
	        for (int i = 0; i < flows.size(); i++) {
	            if (flows.get(i).getStartNode() == selectedNode) {
	                flowIndex = i;
	                break;
	            }
	        }
	        if (flowIndex == -1) {
	            MessageDialog.openError(getShell(), "错误", "数据错误，选中节点没有父节点。");
	            return;
	        }
	        flows.remove(flowIndex);
	        for (FlowNode child : selectedNode.getChildren()) {
	            if (child instanceof ActionGroupNode) {
	                ConditionNode fakeNode = new ConditionNode();
	                fakeNode.setCondition(new C_True());
	                fakeNode.addChild(child);
	                Flow newFlow = new Flow();
	                newFlow.setStartNode(fakeNode);
	                flows.add(flowIndex, newFlow);
	                flowIndex++;
	            } else if (child instanceof ConditionNode) {
	                Flow newFlow = new Flow();
	                newFlow.setStartNode((ConditionNode)child);
	                child.setParent(null);
	                flows.add(flowIndex, newFlow);
	                flowIndex++;
	            }
	        }
	        setSelectedNode(null);
	    } else if (selectedNode instanceof ActionGroupNode || selectedNode.getParent() instanceof ActionGroupNode) {
	        // 如果被删除的是是动作组或动作组中的子节点，删除这个节点及其所有子节点
	        int index = selectedNode.getParent().getChildren().indexOf(selectedNode);
	        selectedNode.getParent().getChildren().remove(index);
	        if (index < selectedNode.getParent().getChildren().size()) {
	            setSelectedNode(selectedNode.getParent().getChildren().get(index));
	        } else if (index > 0) {
                setSelectedNode(selectedNode.getParent().getChildren().get(index - 1));
	        } else {
                setSelectedNode(null);
	        }
	    } else if (selectedNode.getParent().getParent() != null && selectedNode.getParent().getParent() instanceof ActionGroupNode) {
	        // 如果被删除的节点的父节点是动作组中的节点，则不能被删除，只能尽可能地把内容清空
	        if (selectedNode.getParent() instanceof ConditionNode) {
	            ((ActionNode)selectedNode).setAction(new A_Empty());
	            selectedNode.getChildren().clear();
	        } else {
	            return;
	        }
	    } else {
	        // 剩余的情况是：被删除节点是流程中的中间节点，这时只需要把本节点的所有子节点都挂到父节点下面即可。
	        selectedNode.getParent().getChildren().remove(selectedNode);
	        for (FlowNode child : selectedNode.getChildren()) {
	            selectedNode.getParent().addChild(child);
	        }
	        setSelectedNode(selectedNode.getParent());
	        
	        // 如果合并后有2个同级的动作组，合并它们。
	        ActionGroupNode existGroup = null;
	        for (int i = 0; i < selectedNode.getChildren().size(); i++) {
	            if (selectedNode.getChildren().get(i) instanceof ActionGroupNode) {
	                ActionGroupNode group = (ActionGroupNode)selectedNode.getChildren().get(i);
	                if (existGroup == null) {
	                    existGroup = group;
	                } else {
	                    for (FlowNode child : group.getChildren()) {
	                        existGroup.addChild(child);
	                    }
	                    selectedNode.getChildren().remove(i);
	                    i--;
	                }
	            }
	        }
	    }
	    
	    layout();
	    redraw();
	    notifyModified();
	}
	
	// 移动光标到上一个兄弟节点
	private void traverseUp() {
	    if (selectedNode == null) {
	        return;
	    }
	    if (selectedNode.getParent() == null) {
	        return;
	    }
	    int index = selectedNode.getParent().getChildren().indexOf(selectedNode);
	    if (index > 0) {
	        setSelectedNode(selectedNode.getParent().getChildren().get(index - 1));
	        ensureNodeVisible(selectedNode);
	        redraw();
	    }
	}
	
	// 移动光标到下一个兄弟节点
	private void traverseDown() {
	    if (selectedNode == null) {
            return;
        }
        if (selectedNode.getParent() == null) {
            return;
        }
        int index = selectedNode.getParent().getChildren().indexOf(selectedNode);
        if (index < selectedNode.getParent().getChildren().size() - 1) {
            setSelectedNode(selectedNode.getParent().getChildren().get(index + 1));
            ensureNodeVisible(selectedNode);
            redraw();
        }
	}
	
	// 移动光标到父节点
	private void traverseLeft() {
	    if (selectedNode == null) {
            return;
        }
        if (selectedNode.getParent() == null) {
            return;
        }
        setSelectedNode(selectedNode.getParent());
        ensureNodeVisible(selectedNode);
        redraw();
	}
	
	// 移动光标到第一个子节点
	private void traverseRight() {
	    if (selectedNode == null) {
	        return;
	    }
	    if (selectedNode.getChildren().size() == 0) {
	        return;
	    }
	    setSelectedNode(selectedNode.getChildren().get(0));
        ensureNodeVisible(selectedNode);
        redraw();
	}

	// 查找所在的滚动窗口
	private ScrolledComposite findParent() {
        Composite parent = this.getParent();
        while (!(parent instanceof ScrolledComposite)) {
            parent = parent.getParent();
        }
        return (ScrolledComposite)parent;
	}
	
	// 滚动窗口以确保选中节点可见
	private void ensureNodeVisible(FlowNode node) {
	    // 计算滚动窗口的大小和位置
	    ScrolledComposite scc = findParent();
	    Point topLeft = scc.getOrigin();
	    int width = scc.getBounds().width;
	    if (scc.getVerticalBar() != null && scc.getVerticalBar().isVisible()) {
	        width -= scc.getVerticalBar().getSize().x;
	    }
	    int height = scc.getBounds().height;
        if (scc.getHorizontalBar() != null && scc.getHorizontalBar().isVisible()) {
            height -= scc.getHorizontalBar().getSize().y;
        }
        
        // 判断当前窗口是否包含选中节点，如果不包含，则滚动之
        Rectangle nbounds = node.getBounds();
        if (nbounds.x < topLeft.x) {
            topLeft.x = nbounds.x;
        }
        if (nbounds.x + nbounds.width > topLeft.x + width) {
            topLeft.x = nbounds.x + nbounds.width - width;
        }
        if (nbounds.y < topLeft.y) {
            topLeft.y = nbounds.y;
        }
        if (nbounds.y + nbounds.height > topLeft.y + height) {
            topLeft.y = nbounds.y + nbounds.height - height;
        }
        if (!topLeft.equals(scc.getOrigin())) {
            scc.setOrigin(topLeft);
        }
	}
	
	// 查找指定鼠标位置上的节点，如果找不到，返回null。
	private FlowNode findNode(int x, int y) {
	    FindNodeIterator itor = new FindNodeIterator(x, y);
	    for (Flow flow : flows) {
	        flow.iterate(itor);
	        if (itor.getFoundNode() != null) {
	            return itor.getFoundNode();
	        }
	    }
	    return null;
	}
	
	/**
	 * 处理带通知事件的动作节点修改事件。需要重排通知ID，并重新生成修改过的节点的子节点列表。
	 * @param expr
	 */
	public void handleNotifyActionChanged(IExpr expr) {
	    FindNotifyActionIterator itor = new FindNotifyActionIterator(expr);
	    for (Flow flow : flows) {
	        flow.iterate(itor);
	        if (itor.getFoundNode() != null) {
	            // 如果必要，重新生成修改节点的通知ID
                AbstractNotifyAction action = (AbstractNotifyAction)expr;
                if (action.notifyID == 0) {
    	            NotifyIDIterator itor2 = new NotifyIDIterator();
                    for (Flow flow2 : flows) {
                        flow2.iterate(itor2);
                    }
                    action.notifyID = itor2.getNextNotifyID();
                }
                
                // 更新其子节点
                itor.getFoundNode().updateChildren();
	            
	            return;
	        }
	    }
	}

	// 下面的方法实现拖动数据接口。
	
	/**
	 * 有拖动数据进入区域事件。
	 */
	public void dragEnter(DropTargetEvent event) {}
	
	/**
	 * 拖动数据离开区域事件。
	 */
	public void dragLeave(DropTargetEvent event) {}
	
	/**
	 * 拖动操作修改事件。
	 */
	public void dragOperationChanged(DropTargetEvent event) {}
	
	/**
	 * 拖动数据在控件区域滑动事件。
	 */
	public void dragOver(DropTargetEvent event) {
		Point topLeft = toDisplay(0, 0);
	    FlowNode node = findNode(event.x - topLeft.x, event.y - topLeft.y);
        if (hoverNode != node) {
            hoverNode = node;
            isDraggingTemplate = true;
            redraw();
        }
	    
	    event.detail = DND.DROP_NONE;
	    IExpr expr = designer.getSelectedExpr();
	    if (expr == null) {
	        return;
	    }
	    if (hoverNode != null) {
	        // 如果拖动到某个节点上，由节点来决定是否允许拖动添加
	        if (hoverNode.canAccept(expr)) {
	            event.detail = DND.DROP_COPY;
	        }
	    } else {
	        // 如果拖动到空处，则只接受条件表达式，这种情况下将会新创建一个流程
	        if (expr.isCondition()) {
	            event.detail = DND.DROP_COPY;
	        }
	    }
	}
	
	/**
	 * 拖动数据被放下事件。
	 */
	public void drop(DropTargetEvent event) {
        Point topLeft = toDisplay(0, 0);
        FlowNode node = findNode(event.x - topLeft.x, event.y - topLeft.y);
        if (hoverNode != node) {
            hoverNode = node;
            isDraggingTemplate = false;
            redraw();
        }

        String exprStr = (String)event.data;
	    ExpressionList list = ExpressionList.fromString(exprStr);
	    IExpr expr = TemplateManager.recognize(list.getExpr(0), questInfo);
	    
	    // 如果拖入的是一个带通知ID的动作，则自动生成通知ID
	    if (expr instanceof AbstractNotifyAction) {
	        NotifyIDIterator itor = new NotifyIDIterator();
	        for (Flow flow : flows) {
	            flow.iterate(itor);
	        }
	        ((AbstractNotifyAction)expr).notifyID = itor.getNextNotifyID();
	    }
	    
	    if (hoverNode != null) {
	        // 如果拖动到某个节点上，则由此节点来决定添加逻辑
	        try {
	            
    	        if (hoverNode.accept(expr, event.x - topLeft.x, event.y - topLeft.y)) {
        	        layout();
                    redraw();
                    notifyModified();
    	        }
	        } catch (Exception e) {
	            MessageDialog.openError(getShell(), "错误", e.toString());
	        }
	    } else {
	        // 如果拖动条件表达式到空处，创建一个新流程
	        if (expr.isCondition()) {
	            Flow newFlow = new Flow();
	            ConditionNode newCond = new ConditionNode();
	            newCond.setCondition(expr);
	            newFlow.setStartNode(newCond);
	            flows.add(newFlow);
	            ActionGroupNode gn = new ActionGroupNode();
	            ActionNode an = new ActionNode();
	            an.setAction(new A_Empty());
	            gn.addChild(an);
	            newCond.addChild(gn);
	            layout();
	            redraw();
	            notifyModified();
                this.ensureNodeVisible(newCond);
	        }
	    }
	    setSelectedNode(selectedNode);
	}
	
	/**
	 * 拖动数据被放下前最后的数据检查。
	 */
	public void dropAccept(DropTargetEvent event) {}

	// 下面是一些流程枚举器类。
	
    /**
     * 用于检测一个流程范围的枚举器。
     * @author lighthu
     */
    class ComputeSizeIterator implements IFlowIterator {
        int minx = Integer.MAX_VALUE;
        int miny = Integer.MAX_VALUE;
        int maxx = Integer.MIN_VALUE;
        int maxy = Integer.MIN_VALUE;
        
        public boolean walk(FlowNode node) {
            if (node.getBounds() != null) {
                Rectangle b = node.getBounds();
                if (b.x < minx) {
                    minx = b.x;
                }
                if (b.y < miny) {
                    miny = b.y;
                }
                if (b.x + b.width > maxx) {
                    maxx = b.x + b.width;
                }
                if (b.y + b.height > maxy) {
                    maxy = b.y + b.height;
                }
            }
            return false;
        }
        
        public Rectangle getBounds() {
            if (minx == Integer.MAX_VALUE) {
                return new Rectangle(0, 0, 0, 0);
            }
            return new Rectangle(minx, miny, maxx - minx, maxy - miny);
        }
    }

    /**
     * 用于调整流程中所有对象Y位置的枚举器。
     * @author lighthu
     */
    class AdjustYIterator implements IFlowIterator {
        int offset;
        
        public AdjustYIterator(int off) {
            offset = off;
        }
        
        public boolean walk(FlowNode node) {
            if (node.getBounds() != null) {
                node.getBounds().y += offset;
            }
            return false;
        }
    }

    /**
     * 用于调整流程中所有对象X位置的枚举器。
     * @author lighthu
     */
    class AdjustXIterator implements IFlowIterator {
        int offset;
        
        public AdjustXIterator(int off) {
            offset = off;
        }
        
        public boolean walk(FlowNode node) {
            if (node.getBounds() != null) {
                node.getBounds().x += offset;
            }
            return false;
        }
    }

    /**
     * 用于查找指定位置上的节点的枚举器。
     * @author lighthu
     */
    class FindNodeIterator implements IFlowIterator {
        int x;
        int y;
        FlowNode groupNode;
        FlowNode node;
        
        public FindNodeIterator(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        public boolean walk(FlowNode node) {
            if (node.getBounds() != null) {
                Rectangle nbounds = node.getBounds();
                if (node instanceof ActionGroupNode) {
                    if (new Rectangle(nbounds.x - 10, nbounds.y - 10, nbounds.width + 20, nbounds.height + 20).contains(x, y)) {
                        groupNode = node;
                    }
                } else if (nbounds.contains(x, y)) {
                    this.node = node;
                    return true;
                }
            }
            return false;
        }
        
        public FlowNode getFoundNode() {
            if (node != null) {
                return node;
            }
            return groupNode;
        }
    }

    /**
     * 用于查找带通知事件的动作节点的枚举器。
     * @author lighthu
     */
    class FindNotifyActionIterator implements IFlowIterator {
        ActionNode foundNode;
        IExpr expr;
        
        public FindNotifyActionIterator(IExpr expr) {
            this.expr = expr;
        }
        
        public boolean walk(FlowNode node) {
            if (node instanceof ActionNode) {
                ActionNode an = (ActionNode)node;
                if (an.getAction() == expr) {
                    foundNode = an;
                    return true;
                }
            }
            return false;
        }
        
        public ActionNode getFoundNode() {
            return foundNode;
        }
    }
    
    /**
     * 用于查找下一个不重复通知ID的枚举器。
     * @author lighthu
     */
    class NotifyIDIterator implements IFlowIterator {
        HashSet<Integer> usedIDs = new HashSet<Integer>();
        
        public boolean walk(FlowNode node) {
            if (node instanceof ActionNode && ((ActionNode)node).getAction() instanceof AbstractNotifyAction) {
                usedIDs.add(((AbstractNotifyAction)((ActionNode)node).getAction()).notifyID);
            }
            return false;
        }
        
        public int getNextNotifyID() {
            int ret = 1;
            while (usedIDs.contains(ret)) {
                ret++;
            }
            return ret;
        }
    }
    
    /**
     * 用于绘制所有节点的枚举器。
     * @author lighthu
     */
    class PaintIterator implements IFlowIterator {
        GC gc;
        
        public PaintIterator(GC gc) {
            this.gc = gc;
        }
        
        public boolean walk(FlowNode node) {
            if (node.getBounds() != null) {
                // 填充底色
                Rectangle nbounds = node.getBounds();
                if (node instanceof ActionGroupNode) {
                    nbounds = new Rectangle(nbounds.x - 10, nbounds.y - 10, nbounds.width + 20, nbounds.height + 20);
                }
                if (hoverNode == node) {
                    SWTUtils.drawRoundRect(gc, nbounds, 0xDAF2FC, 0xF9FDFF, 0xEAF7FF);
                    if (isDraggingTemplate && node instanceof ConditionNode && 
                            (node.getParent() == null || !(node.getParent() instanceof ActionGroupNode))) {
                        Rectangle bounds2 = new Rectangle(nbounds.x + nbounds.width - 20, nbounds.y, 20, nbounds.height);
                        SWTUtils.drawRoundRect(gc, bounds2, 0xDADADA, 0xF9F9F9, 0xE6E6E6);
                    }
                } else if (selectedNode == node) {
                    SWTUtils.drawRoundRect(gc, nbounds, 0x9ADFFE, 0xF2F9FE, 0xD6F0FD);
                } else if (!(node instanceof ActionGroupNode)) {
                    SWTUtils.drawRoundRect(gc, nbounds, 0xDADADA, 0xF9F9F9, 0xE6E6E6);
                }
                if (node instanceof ActionGroupNode) {
                    // 画边界
                    gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
                    gc.setLineStyle(SWT.LINE_DOT);
                    gc.drawRectangle(nbounds);
                    gc.setLineStyle(SWT.LINE_SOLID);
                } else if (node instanceof ConditionNode || node instanceof ActionNode) {
                    // 设置文字颜色
                    if (node instanceof ConditionNode) {
                        gc.setForeground(SWTResourceManager.getColor(0x99, 0x00, 0x00));
                    } else {
                        gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
                    }
                    // 画文字
                    String[] lines = SWTUtils.formatText(node.toString(), gc, nbounds.width - 4 * 2);
                    int x = nbounds.x + 4;
                    int y = nbounds.y + 4;
                    int lh = gc.getFontMetrics().getHeight();
                    for (int i = 0; i < lines.length; i++) {
                        gc.drawText(lines[i], x, y, true);
                        y += lh;
                    }
                }
                
                // 绘制向上一级的连接线
                gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
                gc.setBackground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
                if (node.getParent() != null && !(node.getParent() instanceof ActionGroupNode)) {
                    int startx = nbounds.x - X_MARGIN / 2;
                    int endx = nbounds.x;
                    if (node instanceof ActionGroupNode) {
                        startx += 10;
                    }
                    int thisy = nbounds.y + nbounds.height / 2;
                    gc.drawLine(startx, thisy, endx, thisy);
                    gc.fillPolygon(new int[] { endx, thisy, endx - 4, thisy - 4, endx - 4, thisy + 4});
                }
                
                // 绘制到下一级的连接线
                if (!(node instanceof ActionGroupNode) && node.getChildren().size() > 0) {
                    int startx = nbounds.x + nbounds.width;
                    int endx = startx + X_MARGIN / 2;
                    FlowNode child = node.getChildren().get(0);
                    int starty = child.getBounds().y + child.getBounds().height / 2;
                    child = node.getChildren().get(node.getChildren().size() - 1);
                    int endy = child.getBounds().y + child.getBounds().height / 2;
                    int thisy = nbounds.y + nbounds.height / 2;
                    gc.drawLine(startx, thisy, endx, thisy);
                    gc.drawLine(endx, starty, endx, endy);
                }
            }
            return false;
        }
    }
}
