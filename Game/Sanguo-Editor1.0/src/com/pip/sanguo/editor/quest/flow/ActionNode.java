package com.pip.sanguo.editor.quest.flow;

import java.util.List;

import com.pip.sanguo.editor.quest.QuestFlowViewer;
import com.pip.sanguo.editor.quest.expr.*;

/**
 * 用于表示一个动作的节点。
 * @author lighthu
 */
public class ActionNode extends FlowNode {
    // 这个节点对应的动作表达式
    protected IExpr action;
    
    /**
     * 设置动作表达式。
     * @param expr
     */
    public void setAction(IExpr expr) {
        action = expr;
    }
    
    /**
     * 取得动作表达式。
     * @return
     */
    public IExpr getAction() {
        return action;
    }
    
    /**
     * 转换为字符串。
     */
    public String toString() {
        return action.toNatureString();
    }
    
    /**
     * 取得这个节点可以拥有的最大子节点数。
     */
    public int getMaxChildren() {
        // Message和Chat动作如果设置了通知ID，就可以拥有一个子节点。Question动作如果设置了通知ID，就可以拥
        // 有和选项数目相同数量的字节点。
        if (action instanceof A_Chat) {
            A_Chat chatAction = (A_Chat)action;
            if (chatAction.notifyID != -1) {
                return 1;
            }
        } else if (action instanceof A_Message) {
            A_Message messageAction = (A_Message)action;
            if (messageAction.notifyID != -1) {
                return 1;
            }
        } else if (action instanceof A_Question) {
            A_Question questionAction = (A_Question)action;
            if (questionAction.notifyID != -1) {
                return questionAction.options.split("\n").length;
            }
        }
        return 0;
    }
    
    /**
     * 创建一个子节点。如果子节点不允许被创建，返回null。
     * @param index
     * @return
     */
    public IExpr createChild(int index) {
        // Message和Chat动作如果设置了通知ID，就可以拥有一个子节点。Question动作如果设置了通知ID，就可以拥
        // 有和选项数目相同数量的字节点。
        if (action instanceof A_Chat) {
            A_Chat chatAction = (A_Chat)action;
            if (chatAction.notifyID != -1 && index == 0) {
                C_CloseChat ret = new C_CloseChat();
                ret.constant = chatAction.notifyID;
                return ret;
            }
        } else if (action instanceof A_Message) {
            A_Message messageAction = (A_Message)action;
            if (messageAction.notifyID != -1 && index == 0) {
                C_CloseMessage ret = new C_CloseMessage();
                ret.constant = messageAction.notifyID;
                return ret;
            }
        } else if (action instanceof A_Question) {
            A_Question questionAction = (A_Question)action;
            if (questionAction.notifyID != -1) {
                C_AnswerQuestion ret = new C_AnswerQuestion();
                ret.param1 = questionAction.notifyID;
                ret.param2 = index;
                return ret;
            }
        }
        return null;
    }
    
    /**
     * 重新调整子节点以适应当前参数的变化。
     */
    public void updateChildren(boolean flatMode, List<Flow> flows) {
        // Message和Chat动作如果设置了通知ID，就可以拥有一个子节点。Question动作如果设置了通知ID，就可以拥
        // 有和选项数目相同数量的字节点。
        if (action instanceof A_Chat) {
            A_Chat chatAction = (A_Chat)action;
            if (chatAction.notifyID == -1) {
                children.clear();
                return;
            }
            while (children.size() > 1) {
                children.remove(children.size() - 1);
            }
            if (children.size() == 0) {
                ConditionNode newChild = new ConditionNode();
                newChild.setCondition(new C_CloseChat());
                newChild.addChild(new ActionGroupNode());
                addChild(newChild);
            }
            C_CloseChat chatCond = (C_CloseChat)((ConditionNode)children.get(0)).getCondition();
            chatCond.constant = chatAction.notifyID;
        } else if (action instanceof A_Message) {
            A_Message msgAction = (A_Message)action;
            if (msgAction.notifyID == -1) {
                children.clear();
                return;
            }
            while (children.size() > 1) {
                children.remove(children.size() - 1);
            }
            if (children.size() == 0) {
                ConditionNode newChild = new ConditionNode();
                newChild.setCondition(new C_CloseMessage());
                newChild.addChild(new ActionGroupNode());
                addChild(newChild);
            }
            C_CloseMessage msgCond = (C_CloseMessage)((ConditionNode)children.get(0)).getCondition();
            msgCond.constant = msgAction.notifyID;
        } else if (action instanceof A_Question) {
            A_Question qstAction = (A_Question)action;
            if (!flatMode) {
                // 在非摊平模式下，所有提问的回答都是这个提问节点的子节点。
                
                // 如果notifyID修改为-1，那么所有回答节点都要删除。
                if (qstAction.notifyID == -1) {
                    children.clear();
                    return;
                }
                
                // 每一个问题对应一个回答节点，下面的循环找出匹配的
                int optionCount = qstAction.options.split("\n").length;
                boolean[] flags = new boolean[optionCount];
                for (int i = 0; i < children.size(); i++) {
                    IExpr expr = ((ConditionNode)children.get(i)).getCondition();
                    if (!(expr instanceof C_AnswerQuestion) || ((C_AnswerQuestion)expr).param2 >= optionCount) {
                        children.remove(i);
                        i--;
                    } else {
                        flags[((C_AnswerQuestion)expr).param2] = true;
                        ((C_AnswerQuestion)expr).param1 = qstAction.notifyID;
                    }
                }
                
                // 对于没有匹配的，创建新的节点。
                for (int i = 0; i < optionCount; i++) {
                    if (!flags[i]) {
                        ConditionNode newChild = new ConditionNode();
                        C_AnswerQuestion aq = new C_AnswerQuestion();
                        aq.param1 = qstAction.notifyID;
                        aq.param2 = i;
                        newChild.setCondition(aq);
                        newChild.addChild(new ActionGroupNode());
                        addChild(newChild);
                    }
                }
            } else {
                // 在摊平模式下，所有提问的回答都是一个独立的Flow。
                
                // 如果notifyID修改为-1，无后续节点。
                if (qstAction.notifyID == -1) {
                    return;
                }
                
                // 每一个问题对应一个回答Flow，下面的循环找出匹配的
                int optionCount = qstAction.options.split("\n").length;
                boolean[] flags = new boolean[optionCount];
                for (int i = 0; i < flows.size(); i++) {
                    IExpr expr = flows.get(i).getStartNode().getCondition();
                    if (expr instanceof C_AnswerQuestion) {
                        C_AnswerQuestion ca = (C_AnswerQuestion)expr;
                        if (ca.param1 == qstAction.notifyID) {
                            if (ca.param2 >= optionCount) {
                                flows.remove(i);
                                i--;
                            } else {
                                flags[ca.param2] = true;
                            }
                        }
                    }
                }
                
                // 对于没有匹配的，创建新的节点。
                for (int i = 0; i < optionCount; i++) {
                    if (!flags[i]) {
                        ConditionNode newChild = new ConditionNode();
                        C_AnswerQuestion aq = new C_AnswerQuestion();
                        aq.param1 = qstAction.notifyID;
                        aq.param2 = i;
                        newChild.setCondition(aq);
                        newChild.addChild(new ActionGroupNode());
                        Flow newFlow = new Flow();
                        newFlow.setStartNode(newChild);
                        flows.add(newFlow);
                    }
                }
            }
        }
    }

    /**
     * 判断一个新的表达式模板是否能够插入本节点后。
     * @param expr
     * @return
     */
    public boolean canAccept(IExpr expr) {
        // 只允许一种情况：把一个新动作拖进占位动作节点
        if ((action instanceof A_Empty) && !expr.isCondition()) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 把一个表达式模板插入本节点后。
     * @param expr
     * @param x 插入位置（相对于Viewer）
     * @param y 插入位置（相对于Viewer）
     * @return 如果插入失败，返回false。
     */
    public boolean accept(IExpr expr, int x, int y, boolean flatMode, List<Flow> flows) {
        // 如果当前节点是一个占位符，则直接替换表达式
        if ((action instanceof A_Empty) && !expr.isCondition()) {
            action = expr;
            updateChildren(flatMode, flows);
            return true;
        }
        
        return false;
    }
}
