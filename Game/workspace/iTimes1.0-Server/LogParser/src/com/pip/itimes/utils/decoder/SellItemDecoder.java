package com.pip.itimes.utils.decoder;

import com.pip.itimes.utils.IAction;
import java.util.regex.Matcher;
import java.util.Date;
import com.pip.itimes.utils.action.SellItemAction;
import com.pip.itimes.utils.IAward;
import com.pip.itimes.utils.Utils;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class SellItemDecoder
    extends RegexLineDecoder {

    public SellItemDecoder(){
        super("ID\\[(\\d+)\\],TYPE\\[86\\],SubType\\[2\\]Item\\[(.+)\\]Price\\[(\\d+)\\]");
        //ID[49833],TYPE[86],SubType[1]Item[00 08 D9 A1 ]Price[160]Money[4849]
    }

    protected SellItemAction createAction(Matcher matcher, Date date) {
        int source = Integer.parseInt(matcher.group(1));
        int price = Integer.parseInt(matcher.group(3));
        IAward award = Utils.getAwardFromItemString(matcher.group(2));
        return new SellItemAction(source,price,award,date);
    }
}
