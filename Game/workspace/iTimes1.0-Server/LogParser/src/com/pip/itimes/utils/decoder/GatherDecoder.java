package com.pip.itimes.utils.decoder;

import java.util.*;
import java.util.regex.*;

import com.pip.itimes.utils.*;
import com.pip.itimes.utils.action.GatherAction;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class GatherDecoder
    extends RegexLineDecoder {
    public GatherDecoder() {
        super("ID\\[(\\d+)\\],TYPE\\[97\\],Changed\\[(.+)\\]");
        //ID[56652],TYPE[97],Changed[01 01 21 00 00 00 02 02 01 30 02 ]
    }

    protected GatherAction createAction(Matcher matcher, Date date) {
        int source = Integer.parseInt(matcher.group(1));
        IAward[] awards = Utils.getAwardsFromChanged(matcher.group(2));
        return new GatherAction(source,awards,date);
    }
}
