package com.pip.sanguo.editor.quest.flow;

import java.util.*;

import org.eclipse.swt.graphics.Rectangle;

import com.pip.sanguo.editor.quest.QuestFlowViewer;
import com.pip.sanguo.editor.quest.expr.IExpr;

/**
 * 任务流程中的节点的共同父类。
 * @author lighthu
 */
public abstract class FlowNode {
    // 父节点
    protected FlowNode parent;
    // 子结点
    protected List<FlowNode> children = new ArrayList<FlowNode>();
    // 在屏幕上布局的位置
    protected Rectangle bounds;
    
    /**
     * 取得父节点。
     * @return
     */
    public FlowNode getParent() {
        return parent;
    }
    
    /**
     * 设置父节点。
     * @param parent
     */
    public void setParent(FlowNode parent) {
        this.parent = parent;
    }
    
    /**
     * 取得子节点列表。
     * @return
     */
    public List<FlowNode> getChildren() {
        return children;
    }
    
    /**
     * 添加一个子节点。
     */
    public void addChild(FlowNode node) {
        node.setParent(this);
        children.add(node);
    }
    
    /**
     * 插入一个子节点。
     */
    public void insertChild(int pos, FlowNode node) {
        node.setParent(this);
        children.add(pos, node);
    }
    
    /**
     * 设置显示位置。
     * @param b
     */
    public void setBounds(Rectangle b) {
        bounds = b;
    }
    
    /**
     * 取得显示位置。
     * @return
     */
    public Rectangle getBounds() {
        return bounds;
    }
    
    /**
     * 判断一个新的表达式模板是否能够插入本节点后。
     * @param expr
     * @return
     */
    public boolean canAccept(IExpr expr) {
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
        return false;
    }
}
