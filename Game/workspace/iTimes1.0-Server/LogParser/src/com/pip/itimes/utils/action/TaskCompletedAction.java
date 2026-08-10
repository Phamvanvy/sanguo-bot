package com.pip.itimes.utils.action;

import com.pip.itimes.utils.*;
import java.util.Date;

/**
 * 任务完成
 * @author Jeffrey
 * @version 1.0
 */
public class TaskCompletedAction
    extends AbstractAction {

    private int taskId;
    private IAward[] awards;

    public TaskCompletedAction(int source,int taskId,IAward[] awards,Date date) {
        this.source = source;
        this.taskId = taskId;
        this.awards = awards;
        this.time = date;
    }

    //任务ID
    public int getTaskId(){
        return taskId;
    }

    //完成任务得到的物品
    public IAward[] getAwards(){
        return awards;
    }

    public void accept(IVisitor visitor) {
        visitor.visit(this);
    }
}
