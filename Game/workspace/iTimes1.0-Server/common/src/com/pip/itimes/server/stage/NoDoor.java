package com.pip.itimes.server.stage;

import java.util.Map;
import java.util.HashMap;

public class NoDoor {

    private String message;
    private short mapId;

    public NoDoor(short mapId,String message) {
        this.mapId = mapId;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public short getMapId() {
        return mapId;
    }

    private static final Map<Short,NoDoor> noDoors = new HashMap<Short,NoDoor>();
    private static final Map<Short,NoDoor> noTransfer = new HashMap<Short,NoDoor>();

    public static NoDoor getNoDoor(short mapId){
        return noDoors.get(mapId);
    }

    public static void addNoDoor(NoDoor noDoor){
        noDoors.put(noDoor.getMapId(),noDoor);
    }
    
    public static NoDoor getNoTransfer(short mapId){
    	return noTransfer.get(mapId);
    }
    
    public static void addNoTransfer(NoDoor noDoor){
    	noTransfer.put(noDoor.getMapId(),noDoor);
    }
}
