package com.pip.itimes.server.world;


import org.apache.log4j.Logger;
/**
 * @author sky
 * @version 1.0
 */
public class AddLogService{
    static Logger log = Logger.getLogger(AddLogService.class);
    
    public AddLogService() {
        
    }
    public static void log(int id, int type, String msg){
        StringBuffer buff = new StringBuffer();
        buff.append("ID[");
        buff.append(id);
        buff.append("],");
        buff.append("TYPE[");
        buff.append(type);
        buff.append("],");
        if(msg != null){
            buff.append(msg);
        }
        log.info(buff.toString());
    }
}
