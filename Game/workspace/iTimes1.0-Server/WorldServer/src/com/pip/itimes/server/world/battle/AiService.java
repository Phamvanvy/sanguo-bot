package com.pip.itimes.server.world.battle;

import com.pip.itimes.server.world.battle.ai.GenericAi;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class AiService {

    public static final GenericAi genericAi = new GenericAi();

    public static final Map ais = new HashMap();

    public static final String AI_PACKAGE = "com.pip.itimes.server.world.battle.ai.";

    public static IMonsterAI getAi(String aiClass){
        if (aiClass == null||aiClass.length()==0) {
            return genericAi;
        }
        Class c = getAiClass(aiClass);
        if (c != null)
            try {
                return (IMonsterAI)c.newInstance();
            } catch(Exception ex){
            }
        return genericAi;
    }

    public static Class getAiClass(String aiClass){

        Class c = (Class)ais.get(aiClass);
        if(c==null){
            String clazz = AI_PACKAGE+aiClass;
            try {
                c = Class.forName(clazz);
                if (c != null) {
                    ais.put(aiClass, c);
                }
            } catch (ClassNotFoundException ex) {
            }
        }
        return c;
    }

}
