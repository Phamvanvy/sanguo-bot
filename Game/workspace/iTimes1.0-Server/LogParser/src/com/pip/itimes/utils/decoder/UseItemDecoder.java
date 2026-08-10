package com.pip.itimes.utils.decoder;

import com.pip.itimes.utils.IAction;
import java.util.regex.Matcher;
import java.util.Date;
import com.pip.itimes.utils.action.UseItemAction;
import com.pip.itimes.utils.Utils;
import com.pip.itimes.utils.IAward;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class UseItemDecoder
    extends RegexLineDecoder {


    public UseItemDecoder(){
        super("ID\\[(\\d+)\\],TYPE\\[33\\],SubType\\[1\\]Item\\[(.+)\\]Money\\[\\d+\\]");
        //ID[9108],TYPE[33],SubType[1]Item[00 00 00 06 ]Money[18067]
    }

    protected UseItemAction createAction(Matcher matcher, Date date) {
        int source = Integer.parseInt(matcher.group(1));
        IAward award = Utils.getAwardFromItemString(matcher.group(2));
        return new UseItemAction(source,award,date);
    }
}
