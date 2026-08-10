package com.pip.itimes.utils.decoder;

import com.pip.itimes.utils.ILineDecoder;
import com.pip.itimes.utils.IVisitor;
import com.pip.itimes.utils.IAction;
import java.util.Date;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: </p>
 *
 * <p>Company: </p>
 *
 * @author Jeffrey
 * @version 1.0
 */
public abstract class  AbstractLineDecoder
    implements ILineDecoder {


    public boolean match(String line, Date date,IVisitor visitor) {
        IAction action = createAction(line,date);
        if(action!=null){
            action.accept(visitor);
            return true;
        }
        return false;
    }

    public abstract IAction createAction(String line,Date date);
}
