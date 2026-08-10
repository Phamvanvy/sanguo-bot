package com.pip.itimes.utils.decoder;

import com.pip.itimes.utils.IAction;
import java.util.regex.Matcher;
import java.util.Date;
import com.pip.itimes.utils.action.FeedAction;
import com.pip.itimes.utils.Utils;
import com.pip.itimes.utils.IAward;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class FeedDecoder
    extends RegexLineDecoder {

    public FeedDecoder(){
        super("ID\\[(\\d+)\\],TYPE\\[-117\\],ItemId\\[(.+)\\]");
        //ID[9108],TYPE[-117],ItemId[00 08 D9 A1 ]
    }

    protected FeedAction createAction(Matcher matcher, Date date) {
        int source  = Integer.parseInt(matcher.group(1));
        IAward award = Utils.getAwardFromItemString(matcher.group(2));
        return new FeedAction(source,award,date);
    }
}
