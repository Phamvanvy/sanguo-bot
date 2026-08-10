package com.pip.itimes.utils.decoder;

import com.pip.itimes.utils.IAction;
import java.util.regex.Matcher;
import java.util.Date;
import com.pip.itimes.utils.action.LoginAction;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class LoginDecoder
    extends RegexLineDecoder {
    public LoginDecoder(){
        super("Login ID\\[(\\d+)\\]Level\\[(\\d+)\\]Money\\[\\d+\\]Exp\\[\\d+\\]Agility\\[\\d+\\]Strength\\[\\d+\\]Vitality\\[\\d+\\]Intelligence\\[\\d+\\]");
        //Login ID[9108]Level[45]Money[16674]Exp[5054]Agility[77]Strength[45]Vitality[47]Intelligence[55]
    }

    protected LoginAction createAction(Matcher matcher, Date date) {
        int source = Integer.parseInt(matcher.group(1));
        int level = Integer.parseInt(matcher.group(2));
        return new LoginAction(source,level,date);
    }
}
