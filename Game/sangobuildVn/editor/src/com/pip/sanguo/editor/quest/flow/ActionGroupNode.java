package com.pip.sanguo.editor.quest.flow;

import java.util.*;

import com.pip.sanguo.editor.quest.expr.A_Empty;
import com.pip.sanguo.editor.quest.expr.IExpr;

/**
 * 动作组节点。一个触发器只有一个动作组。
 * @author lighthu
 */
public class ActionGroupNode extends FlowNode {
    /**
     * 判断一个新的表达式模板是否能够插入本节点后。
     * @param expr
     * @return
     */
    public boolean canAccept(IExpr expr) {
        return true;
    }
    
    /**
     * 把一个表达式模板插入本节点后。
     * @param expr
     * @param x 插入位置（相对于Viewer）
     * @param y 插入位置（相对于Viewer）
     * @return 如果插入失败，返回false。
     */
    public boolean accept(IExpr expr, int x, int y) {
        // 查找插入位置
        int insertPos = 0;
        for (FlowNode child : children) {
            if (y >= child.getBounds().y) {
                insertPos++;
            }
        }
        if (expr.isCondition()) {
            // 如果是一个条件表达式，则自动创建2个动作分支
            ConditionNode condNode = new ConditionNode();
            condNode.setCondition(expr);
            ActionNode switch1 = new ActionNode();
            switch1.setAction(new A_Empty());
            condNode.addChild(switch1);
            ActionNode switch2 = new ActionNode();
            switch2.setAction(new A_Empty());
            condNode.addChild(switch2);
            insertChild(insertPos, condNode);
        } else {
            // 创建一个动作节点
            ActionNode actionNode = new ActionNode();
            actionNode.setAction(expr);
            insertChild(insertPos, actionNode);
            
            // 如果动作节点可以有子节点，则预先创建出来
            actionNode.updateChildren();
        }
        return true;
    }
}
