package com.pip.itimes.utils;

import java.util.Date;

/**
 * @author Jeffrey
 * @version 1.0
 */
public interface ILineDecoder {
    public boolean match(String line,Date date,IVisitor vistor);
}
