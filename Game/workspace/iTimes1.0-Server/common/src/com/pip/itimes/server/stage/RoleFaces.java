package com.pip.itimes.server.stage;

import java.util.Map;
import java.util.HashMap;

public class RoleFaces {

    private static Map<Integer,RoleFaceData> faces = new HashMap<Integer,RoleFaceData>();

    public static RoleFaceData getRoleFace(int face){
        return faces.get(face);
    }

    public static void addRoleFace(RoleFaceData roleFace){
        faces.put(roleFace.getFace(),roleFace);
    }
}
