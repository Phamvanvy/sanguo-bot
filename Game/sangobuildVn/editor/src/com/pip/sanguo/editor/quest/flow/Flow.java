package com.pip.sanguo.editor.quest.flow;

import java.util.*;

import com.pip.sanguo.data.quest.*;
import com.pip.sanguo.data.quest.pqe.*;
import com.pip.sanguo.editor.quest.expr.*;

/**
 * 任务流程。一个流程从一个条件判断开始，可以引出不同的条件判断分支，最终每个分支以一个动作组结束。
 * 动作组中的某些特殊动作还可以继续引出判断分支。最终一个流程可能会需要多个触发器来存储。
 * @author lighthu
 */
public class Flow {
    // 开始条件节点
    protected ConditionNode startNode;

    /**
     * 取得开始条件节点。
     * @return
     */
    public ConditionNode getStartNode() {
        return startNode;
    }

    /**
     * 设置开始条件节点。 
     * @param startNode
     */
    public void setStartNode(ConditionNode startNode) {
        this.startNode = startNode;
    }
    
    /**
     * 把整个任务流程用1个或多个触发器来存储。
     * @return
     */
    public void save(QuestInfo questInfo) {
        // 第一步，遍历整个流程找出所有的动作组，每个动作组对应于一个触发器。
        ActionGroupIterator itor = new ActionGroupIterator();
        iterate(itor);
        List<ActionGroupNode> actionGroups = itor.getActionGroups();
        
        // 每个动作组生成一个Trigger对象
        for (ActionGroupNode group : actionGroups) {
            questInfo.triggers.add(saveTrigger(group));
        }
    }
    
    // 把一个动作组保存为一个触发器。
    private QuestTrigger saveTrigger(ActionGroupNode group) {
        // 从触发器节点开始向前寻找，每一个父节点都是一个条件，直到找到一个根节点
        List<ConditionNode> conditionList = new ArrayList<ConditionNode>();
        FlowNode parent = group.getParent();
        while (parent != null) {
            if (parent instanceof ConditionNode) {
                ConditionNode cn = (ConditionNode)parent;
                conditionList.add(0, cn);
                IExpr cond = cn.getCondition();
                if (cond instanceof C_CloseChat || cond instanceof C_CloseMessage ||
                    cond instanceof C_AnswerQuestion) {
                    break;
                }
            } else {
                throw new IllegalArgumentException("动作组必须从一个条件判断发起。");
            }
            parent = parent.getParent();
        }
        
        // 把所有的条件拼起来作为触发器条件
        if (conditionList.size() == 0) {
            throw new IllegalArgumentException("动作组必须从一个条件判断发起。");
        }
        StringBuffer buf = new StringBuffer();
        for (ConditionNode cn : conditionList) {
            if (buf.length() > 0) {
                buf.append(", ");
            }
            buf.append(cn.getCondition().getExpression());
        }
        QuestTrigger ret = new QuestTrigger();
        ret.condition = buf.toString();
        
        // 把动作组中的所有动作拼起来作为触发器动作，要考虑@If的特殊情况
        buf.setLength(0);
        for (FlowNode an : group.getChildren()) {
            if (buf.length() > 0) {
                buf.append(", ");
            }
            if (an instanceof ActionNode) {
                buf.append(((ActionNode)an).getAction().getExpression());
            } else if (an instanceof ConditionNode) {
                ConditionNode cn = (ConditionNode)an;
                if (cn.getChildren().size() == 0) {
                    buf.append(cn.getCondition().getExpression());
                } else if (cn.getChildren().size() == 2 && cn.getChildren().get(0) instanceof ActionNode &&
                        cn.getChildren().get(1) instanceof ActionNode) {
                    buf.append("If(");
                    buf.append(cn.getCondition().getExpression());
                    buf.append(", ");
                    buf.append(((ActionNode)cn.getChildren().get(0)).getAction().getExpression());
                    buf.append(", ");
                    buf.append(((ActionNode)cn.getChildren().get(1)).getAction().getExpression());
                    buf.append(")");
                } else {
                    throw new IllegalArgumentException("动作组中的条件必须带有两个动作子节点。");
                }
            }
        }
        ret.action = buf.toString();
        return ret;
    }
    
    /**
     * 分析一个任务脚本，把触发器转换为一个或多个任务流程。
     * @param questInfo 任务信息。凡是被识别的触发器都会从triggers数组中移除。
     * @return 流程对象的数组。
     */
    public static List<Flow> parseQuest(QuestInfo questInfo) {
        List<Flow> retList = new ArrayList<Flow>();
        while (true) {
            // 找出一个流程，如果找不到，则返回。
            Flow flow = recognize(questInfo);
            if (flow == null) {
                return retList;
            }
            
            // 如果找到一个流程，加入返回数组中
            retList.add(flow);
        }
    }
    
    /**
     * 从一组触发器中识别出一个流程来。
     * @param questInfo 任务信息，被识别的触发器将从触发器列表中删除
     * @return 一个可能包含多个触发器的任务流程。如果一个流程都识别不出来，返回null。
     */
    public static Flow recognize(QuestInfo questInfo) {
        if (questInfo.triggers.size() == 0) {
            return null;
        }
        
        // 挑选出一个能够作为流程起始的触发器开始识别。要作为一个流程的起始，这个触发器必须要有至少一个
        // 条件，并且这个条件不能是C_CloseMessage，C_CloseChat或C_AnswerQuestion中的一个。
        QuestTrigger startTrigger = null;
        for (QuestTrigger t : questInfo.triggers) {
            ExpressionList exprs = ExpressionList.fromString(t.condition);
            if (exprs.getExprCount() == 0) {
                continue;
            }
            IExpr expr = TemplateManager.recognize(exprs.getExpr(0), questInfo);
            if (expr instanceof C_CloseMessage || expr instanceof C_CloseChat || expr instanceof C_AnswerQuestion) {
                continue;
            }
            startTrigger = t;
            break;
        }
        
        // 如果所有的触发器都不满足作为流程起始节点的条件，则这些触发器都被放弃
        if (startTrigger == null) {
            return null;
        }
        
        // 把找到的起始触发器解析为一个流程
        Flow basicFlow = recognize(startTrigger, questInfo);
        questInfo.triggers.remove(startTrigger);
        
        // 循环检查所有其他触发器是否和这个流程进行合并，直到找不到可合并的触发器为止
        while (true) {
            int addCount = 0;
            for (int i = 0; i < questInfo.triggers.size(); i++) {
                QuestTrigger t = questInfo.triggers.get(i);
                Flow subFlow = recognize(t, questInfo);
                if (mergeFlow(basicFlow, subFlow)) {
                    addCount++;
                    questInfo.triggers.remove(i);
                    i--;
                } else if (mergeFlow(subFlow, basicFlow)) {
                    addCount++;
                    basicFlow = subFlow;
                    questInfo.triggers.remove(i);
                    i--;
                }
            }
            if (addCount == 0) {
                break;
            }
        }
        
        // 扫描流程中所有可能有子节点的ActionNode，自动生成其子节点。
        UpdateChildrenIterator itor = new UpdateChildrenIterator();
        basicFlow.iterate(itor);
        
        return basicFlow;
    }
    
    /**
     * 从一个Trigger对象中识别出一个流程对象。所有的条件被串联起来，最后指向一个动作组，包含所有触发器
     * 动作。@If函数调用被特殊处理。
     * @param trigger
     * @param questInfo
     * @return 如果无法识别，返回null。
     */
    public static Flow recognize(QuestTrigger trigger, QuestInfo questInfo) {
        // 检查是否有条件，没有条件的触发器是不能存在的
        ExpressionList exprs = ExpressionList.fromString(trigger.condition);
        if (exprs.getExprCount() == 0) {
            return null;
        }
        
        // 把所有条件串联起来
        ConditionNode startNode = new ConditionNode();
        startNode.setCondition(TemplateManager.recognize(exprs.getExpr(0), questInfo));
        ConditionNode currentNode = startNode;
        for (int i = 1; i < exprs.getExprCount(); i++) {
            ConditionNode newNode = new ConditionNode();
            newNode.setCondition(TemplateManager.recognize(exprs.getExpr(i), questInfo));
            currentNode.addChild(newNode);
            currentNode = newNode;
        }
        
        // 把所有的动作创建到一个动作组中去，并挂在最后一个条件的后面
        ActionGroupNode groupNode = new ActionGroupNode();
        currentNode.addChild(groupNode);
        exprs = ExpressionList.fromString(trigger.action);
        for (int i = 0; i < exprs.getExprCount(); i++) {
            Expression expr = exprs.getExpr(i);
            
            // @If函数特殊处理
            if (expr.getRightExpr() == null && expr.getLeftExpr().type == Expr0.TYPE_FUNC) {
                FunctionCall fcall = expr.getLeftExpr().getFunctionCall();
                if ("If".equals(fcall.funcName) && fcall.getParamCount() == 3) {
                    Expression condExpr = fcall.getParam(0);
                    Expression trueAction = fcall.getParam(1);
                    Expression falseAction = fcall.getParam(2);
                    
                    ConditionNode ifNode = new ConditionNode();
                    ifNode.setCondition(TemplateManager.recognize(condExpr, questInfo));
                    groupNode.addChild(ifNode);
                    
                    ActionNode trueNode = new ActionNode();
                    trueNode.setAction(TemplateManager.recognize(trueAction, questInfo));
                    ifNode.addChild(trueNode);

                    ActionNode falseNode = new ActionNode();
                    falseNode.setAction(TemplateManager.recognize(falseAction, questInfo));
                    ifNode.addChild(falseNode);
                    continue;
                }
            }
            
            // 其他节点一律作为动作处理
            ActionNode newAction = new ActionNode();
            newAction.setAction(TemplateManager.recognize(expr, questInfo));
            groupNode.addChild(newAction);
        }
        
        // 创建一个Flow对象，以起始条件作为根节点。
        Flow ret = new Flow();
        ret.setStartNode(startNode);
        return ret;
    }
    
    /**
     * 尝试把一个新的流程挂接到已有的流程中。挂接的情况有以下几种：
     * 1. 新流程的起始1个或多个条件和已有流程相同，这种情况下可建立在最后一个相同流程后建立分支。
     * 2. 新流程的所有条件都和已有流程相同，这种情况下把动作组合并即可。
     * 3. 新流程以C_CloseChat，C_CloseMessage，C_AnswerQuestion其中的一个条件开始，且通知ID匹配
     * 已有流程的动作组中对应的一个A_Chat，A_Message，A_Question动作，这种情况下把新的流程完整
     * 挂接到此动作后。
     * @param basicFlow 已有流程
     * @param newFlow 被合并的流程
     * @return 如果合并成功，返回true，否则返回false。
     */
    private static boolean mergeFlow(Flow basicFlow, Flow newFlow) {
        IExpr cond = newFlow.getStartNode().getCondition();
        if (cond instanceof C_CloseChat) {
            // C_CloseChat条件匹配A_Chat动作
            int notifyID = ((C_CloseChat)cond).constant;
            FlowNode chatNode = basicFlow.findChat(notifyID);
            if (chatNode != null) {
                chatNode.addChild(newFlow.getStartNode());
                return true;
            }
        } else if (cond instanceof C_CloseMessage) {
            // C_CloseMessage条件匹配A_Message动作
            int notifyID = ((C_CloseMessage)cond).constant;
            FlowNode messageNode = basicFlow.findMessage(notifyID);
            if (messageNode != null) {
                messageNode.addChild(newFlow.getStartNode());
                return true;
            }
        } else if (cond instanceof C_AnswerQuestion) {
            // C_AnswerQuestion条件匹配A_Question动作
            int notifyID = ((C_AnswerQuestion)cond).param1;
            FlowNode questionNode = basicFlow.findQuestion(notifyID);
            if (questionNode != null) {
                questionNode.addChild(newFlow.getStartNode());
                return true;
            }
        } else {
            // 除了上述3种特殊情况，比较开始条件是否匹配
            ConditionNode newCond = newFlow.getStartNode();
            ConditionNode existCond = basicFlow.getStartNode();
            if (isConditionEqual(newCond, existCond)) {
                // 循环比较，直到找到第一个不相等的节点为止
                while (true) {
                    // 如果下一个节点还是条件，检查这个条件是否能和已有流程的下一个条件匹配
                    if (newCond.getChildren().size() == 1 && newCond.getChildren().get(0) instanceof ConditionNode) {
                        ConditionNode newCond2 = (ConditionNode)newCond.getChildren().get(0);
                        boolean found = false;
                        for (int i = 0; i < existCond.getChildren().size(); i++) {
                            FlowNode n = existCond.getChildren().get(i);
                            if (n instanceof ConditionNode && isConditionEqual((ConditionNode)n, newCond2)) {
                                // 匹配了，继续向下查找
                                found = true;
                                existCond = (ConditionNode)n;
                                break;
                            }
                        }
                        if (found) {
                            newCond = newCond2;
                            continue;
                        }
                    }
                    
                    // 没有匹配了。如果新旧节点的下一节点都是动作组了，则合并两个动作组，否则把新节点的后续节点
                    // 挂成旧节点的一个新子节点。
                    if (newCond.getChildren().size() == 1 && newCond.getChildren().get(0) instanceof ActionGroupNode) {
                        ActionGroupNode newGroup = (ActionGroupNode)newCond.getChildren().get(0);
                        for (int i = 0; i < existCond.getChildren().size(); i++) {
                            FlowNode n = existCond.getChildren().get(i);
                            if (n instanceof ActionGroupNode) {
                                // 都是动作组，合并后完成合并
                                for (FlowNode nn : newGroup.getChildren()) {
                                    nn.setParent(n);
                                }
                                ((ActionGroupNode)n).getChildren().addAll(newGroup.getChildren());
                                return true;
                            }
                        }
                    }
                    for (FlowNode child : newCond.getChildren()) {
                        existCond.addChild(child);
                    }
                    return true;
                }
            }
        }
        return false;
    }
    
    // 在流程中查找指定通知ID的对话动作。
    private FlowNode findChat(int nid) {
        NotifyActionIterator itor = new NotifyActionIterator(A_Chat.class, nid);
        iterate(itor);
        return itor.getFoundNode();
    }
    
    // 在流程中查找指定通知ID的显示消息动作。
    private FlowNode findMessage(int nid) {
        NotifyActionIterator itor = new NotifyActionIterator(A_Message.class, nid);
        iterate(itor);
        return itor.getFoundNode();
    }

    // 在流程中查找指定通知ID的提问动作。
    private FlowNode findQuestion(int nid) {
        NotifyActionIterator itor = new NotifyActionIterator(A_Question.class, nid);
        iterate(itor);
        return itor.getFoundNode();
    }
    
    /**
     * 判断两个条件是否等效。
     * @param cond1
     * @param cond2
     * @return
     */
    private static boolean isConditionEqual(ConditionNode cond1, ConditionNode cond2) {
        return cond1.getCondition().getExpression().equals(cond2.getCondition().getExpression());
    }
    
    /**
     * 广度遍历一个流程的所有节点。
     * @param itor
     */
    public void iterate(IFlowIterator itor) {
        iterate(startNode, itor);
    }
    
    /**
     * 广度遍历一个结点的所有子结点。
     * @param startNode
     * @param itor
     */
    public static void iterate(FlowNode startNode, IFlowIterator itor) {
        List<FlowNode> stack = new ArrayList<FlowNode>();
        stack.add(startNode);
        while (stack.size() > 0) {
            FlowNode n = stack.remove(0);
            if (itor.walk(n)) {
                return;
            }
            stack.addAll(n.getChildren());
        }
    }

    /**
     * 用于查找流程中所有动作组的枚举器。
     * @author lighthu
     */
    class ActionGroupIterator implements IFlowIterator {
        List<ActionGroupNode> actionGroups = new ArrayList<ActionGroupNode>();
        
        public boolean walk(FlowNode node) {
            if (node instanceof ActionGroupNode && ((ActionGroupNode)node).getChildren().size() > 0) {
                actionGroups.add((ActionGroupNode)node);
            }
            return false;
        }
        
        public List<ActionGroupNode> getActionGroups() {
            return actionGroups;
        }
    }
    
    /**
     * 用于查找流程中指定通知ID的动作的枚举器。
     * @author lighthu
     */
    class NotifyActionIterator implements IFlowIterator {
        FlowNode foundNode;
        Class nodeClass;
        int notifyID;
        
        public NotifyActionIterator(Class cls, int notifyID) {
            nodeClass = cls;
            this.notifyID = notifyID;
        }
        
        public boolean walk(FlowNode node) {
            if (node instanceof ActionNode) {
                IExpr expr = ((ActionNode)node).getAction();
                if (nodeClass.isInstance(expr)) {
                    if (((AbstractNotifyAction)expr).notifyID == notifyID) {
                        foundNode = node;
                        return false;
                    }
                }
            }
            return false;
        }
        
        public FlowNode getFoundNode() {
            return foundNode;
        }
    }
    
    /**
     * 用于自动生成所有ActionNode子节点的枚举器。
     * @author lighthu
     */
    static class UpdateChildrenIterator implements IFlowIterator {
        public boolean walk(FlowNode node) {
            if (node instanceof ActionNode) {
                ((ActionNode)node).updateChildren();
            }
            return false;
        }
    }
}
