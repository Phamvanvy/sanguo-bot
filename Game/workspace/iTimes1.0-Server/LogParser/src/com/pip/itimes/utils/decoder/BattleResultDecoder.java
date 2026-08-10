package com.pip.itimes.utils.decoder;

import com.pip.itimes.utils.IAction;
import java.util.regex.Matcher;
import java.util.Date;
import com.pip.itimes.utils.Utils;
import com.pip.itimes.utils.IAward;
import com.pip.itimes.utils.action.BattleResultAction;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class BattleResultDecoder
    extends RegexLineDecoder {

    public BattleResultDecoder(){
        super("ID\\[(\\d+)\\],TYPE\\[34\\],MgId\\[(\\d+)\\]Changed\\[(.+)\\]Money\\[\\d+\\]");
        //ID[1169],TYPE[34],MgId[263266304]Changed[01 03 05 00 00 1E FD 06 00 00 05 6C 19 00 00 05 7B 08 01 00 00 00 6E 00 00 00 E6 00 0C E7 A5 9E E6 B0 94 E5 AE 9D E8 B4 9D 05 01 00 01 00 00 00 00 00 00 00 00 32 00 05 00 05 00 14 00 14 00 00 00 AA 00 00 00 6E 05 03 F2 03 EE 03 FC 03 F9 03 F3 0A 03 00 0F 42 60 00 00 3F 11 00 63 00 0F 42 63 00 00 3F 19 00 2A 00 0F 42 65 00 00 3F 60 00 33 ]Money[234429]
    }

    protected BattleResultAction createAction(Matcher matcher, Date date) {
        int source = Integer.parseInt(matcher.group(1));
        int monsterId = Integer.parseInt(matcher.group(2));
        IAward[] awards = Utils.getAwardsFromChanged(matcher.group(3));
        return new BattleResultAction(source,monsterId,awards,date);
    }
}
