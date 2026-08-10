package com.pip.itimes.utils.decoder;

import com.pip.itimes.utils.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Date;

/**
 * @author Jeffrey
 * @version 1.0
 */
public abstract class RegexLineDecoder
    extends AbstractLineDecoder {

    private Pattern pattern;

    public RegexLineDecoder(String regex) {
        pattern = Pattern.compile(regex);
    }

    public IAction createAction(String line,Date date) {
        Matcher matcher = pattern.matcher(line);
        if(matcher.matches()){
            return createAction(matcher,date);
        }else{
            return null;
        }
    }

    abstract protected IAction createAction(Matcher matcher,Date date);
}
