package com.pip.sanguo.editor.quest.flow;

/**
 * 流程遍历的监听对象。
 * @author lighthu
 */
public interface IFlowIterator {
    /**
     * 遍历一个节点。
     * @param node
     * @return 如果要停止遍历，返回true。
     */
    public boolean walk(FlowNode node);
}
