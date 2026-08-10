package com.pip.itimes.utils.decoder;

import java.util.*;
import java.util.regex.*;

import com.pip.itimes.utils.*;
import com.pip.itimes.utils.action.PkBeginAction;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class PkBeginDecoder
    extends RegexLineDecoder {
    public PkBeginDecoder() {
        super("ID\\[(\\d+)\\]Money\\[(\\d+)\\]Dest\\[(\\d+)\\]Money\\[(\\d+)\\]Wager\\[(\\d+)\\]BEGIN");
        //ID[45555]Money[8871]Dest[47666]Money[10791]Wager[0]BEGIN
    }


    protected PkBeginAction createAction(Matcher matcher, Date date) {
        int source = Integer.parseInt(matcher.group(1));
        int sourceMoney = Integer.parseInt(matcher.group(2));
        int dest = Integer.parseInt(matcher.group(3));
        int destMoney = Integer.parseInt(matcher.group(4));
        int wager = Integer.parseInt(matcher.group(5));
        return new PkBeginAction(source,dest,sourceMoney,destMoney,wager,date);
    }
}
