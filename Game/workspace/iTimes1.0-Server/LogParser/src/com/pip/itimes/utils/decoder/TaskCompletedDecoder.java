package com.pip.itimes.utils.decoder;

import java.util.*;
import java.util.regex.*;

import com.pip.itimes.utils.*;
import com.pip.itimes.utils.action.TaskCompletedAction;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class TaskCompletedDecoder
    extends RegexLineDecoder {
    public TaskCompletedDecoder() {
        super("ID\\[(\\d+)\\],TYPE\\[108],TaskId\\[(\\d+)\\]Money\\[\\d+\\]Changed\\[(.+)\\]");
        //ID[56001],TYPE[108],TaskId[20104]Money[5125]Changed[03 01 00 09 E9 AD 94 E5 BF 83 E7 9F B3 01 ]
    }

    protected TaskCompletedAction createAction(Matcher matcher, Date date) {
        int source = Integer.parseInt(matcher.group(1));
        int taskId = Integer.parseInt(matcher.group(2));
        IAward[] awards = Utils.getAwardsFromChanged(matcher.group(3));
        return new TaskCompletedAction(source,taskId,awards,date);
    }
}
