package com.pip.itimes.utils.decoder;

import java.util.*;
import java.util.regex.*;

import com.pip.itimes.utils.*;
import com.pip.itimes.utils.action.LogoutAction;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class LogoutDecoder
    extends RegexLineDecoder {
    public LogoutDecoder() {
        super("ID\\[(\\d+)\\]Level\\[(\\d+)\\]Logout");
        //ID[9108]Level[45]Logout
    }

    protected LogoutAction createAction(Matcher matcher, Date date) {
        int source = Integer.parseInt(matcher.group(1));
        int level = Integer.parseInt(matcher.group(2));
        return new LogoutAction(source,level,date);
    }
}
