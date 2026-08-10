package com.pip.itimes.utils.decoder;

import com.pip.itimes.utils.IAction;
import java.util.regex.Matcher;
import java.util.Date;
import com.pip.itimes.utils.Utils;
import com.pip.itimes.utils.IAward;
import com.pip.itimes.utils.action.TeamBattleResultAction;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class TeamBattleResultDecoder
    extends RegexLineDecoder {

    public TeamBattleResultDecoder(){
        super("ID\\[(\\d+)\\],TYPE\\[52\\],MgId\\[(\\d+)\\]SubType\\[end\\]Changed\\[(.+)\\]Money\\[\\d+\\]");
        //ID[9108],TYPE[52],MgId[107094019]SubType[end]Changed[01 01 06 00 00 00 11 0A 03 00 0F 43 1D 00 5C 0A E4 00 40 00 0F 42 AA 00 10 6B 82 00 1F 00 0F 43 26 00 3C 34 5C 00 54 ]Money[15628]
    }


    protected TeamBattleResultAction createAction(Matcher matcher, Date date) {
        int source = Integer.parseInt(matcher.group(1));
        int monsterId = Integer.parseInt(matcher.group(2));
        IAward[] awards = Utils.getAwardsFromChanged(matcher.group(3));
        return new TeamBattleResultAction(source,monsterId,awards,date);
    }
}
