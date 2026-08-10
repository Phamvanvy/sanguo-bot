package com.pip.itimes.utils.decoder;

import java.util.*;
import java.util.regex.*;

import com.pip.itimes.utils.*;
import com.pip.itimes.utils.action.*;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class ThrowItemDecoder
    extends RegexLineDecoder {

    public ThrowItemDecoder(){
        super("ID\\[(\\d+)\\],TYPE\\[33\\],SubType\\[2\\]Item\\[(.+)\\]Money\\[\\d+\\]");
        //ID[9108],TYPE[33],SubType[2]Item[00 00 00 06 ]Money[18067]
    }

    protected ThrowItemAction createAction(Matcher matcher, Date date) {
        int source = Integer.parseInt(matcher.group(1));
        IAward award = Utils.getAwardFromItemString(matcher.group(2));
        return new ThrowItemAction(source,award,date);
    }
}
