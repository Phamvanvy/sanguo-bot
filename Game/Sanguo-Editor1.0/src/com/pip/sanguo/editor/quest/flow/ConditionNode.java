package com.pip.sanguo.editor.quest.flow;

import java.util.List;

import com.pip.sanguo.editor.quest.expr.C_True;
import com.pip.sanguo.editor.quest.expr.IExpr;

/**
 * 用于表示一个条件判断的节点。
 * @author lighthu
 */
public class ConditionNode extends FlowNode {
    // 这个节点对应的判断表达式
    protected IExpr condition;
    
    /**
     * 设置判断表达式。
     * @param expr
     */
    public void setCondition(IExpr expr) {
        condition = expr;
    }
    
    /**
     * 取得判断表达式。
     * @return
     */
    public IExpr getCondition() {
        return condition;
    }

    /**
     * 转换为字符串。
     */
    public String toString() {
        return condition.toNatureString();
    }

    /**
     * 判断一个新的表达式模板是否能够插入本节点后。
     * @param expr
     * @return
     */
    public boolean canAccept(IExpr expr) {
        // 不在动作组中的判断条件可以带有任意多个后续判断或后续动作（后续动作被组织成一个动作组）
        if (parent == null || !(parent instanceof ActionGroupNode)) {
            return true;
        }
        
        // 在动作组中的判断条件应该有且只有2个动作作为分支
        if (children.size() < 2 && !expr.isCondition()) {
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
        if ((condition instanceof C_True) && expr.isCondition()) {
            condition = expr;
            return true;
        }
        
        // 不在动作组中的判断条件可以带有任意多个后续判断或后续动作（后续动作被组织成一个动作组）
        if (parent == null || !(parent instanceof ActionGroupNode)) {
            if (expr.isCondition()) {
                ConditionNode condNode = new ConditionNode();
                condNode.setCondition(expr);
                if (x >= bounds.x + bounds.width - 20) {
                    // 如果在末位段，新条件加为其他子节点的兄弟 
                    addChild(condNode);
                } else {
                    // 正常情况，新条件加为现有子节点的父节点
                    for (FlowNode child : children) {
                        condNode.addChild(child);
                    }
                    children.clear();
                    addChild(condNode);
                }
            } else {
                ActionGroupNode group = null;
                for (FlowNode child : children) {
                    if (child instanceof ActionGroupNode) {
                        group = (ActionGroupNode)child;
                        break;
                    }
                }
                if (group == null) {
                    group = new ActionGroupNode();
                    addChild(group);
                }
                ActionNode actionNode = new ActionNode();
                actionNode.setAction(expr);
                group.addChild(actionNode);
                actionNode.updateChildren(flatMode, flows);
            }
            return true;
        } else {
            // 在动作组中的判断条件应该有且只有2个动作作为分支
            if (children.size() < 2 && !expr.isCondition()) {
                ConditionNode condNode = new ConditionNode();
                condNode.setCondition(expr);
                addChild(condNode);
                return true;
            }
        }
        
        return false;
    }
}
