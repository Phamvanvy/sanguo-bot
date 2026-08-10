package com.pip.itimes.server.stage;

import java.util.Map;
import java.util.HashMap;

/**
 * @author Jeffery
 * @version 1.0
 */
public class MaterialTypes {

    public static Map types = new HashMap();


    public static void addMaterialType(MaterialType type){
        types.put(new Integer(type.getId()),type);
    }

    public static MaterialType getMaterialType(int id){
        return (MaterialType)types.get(new Integer(id));
    }


}
