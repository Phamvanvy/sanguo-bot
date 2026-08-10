package com.pip.itimes.server.stage;

import java.util.List;

/**
 * @author Jeffery
 * @version 1.0
 */
public class NpcPool {

    private RefreshObjectPool pool = new RefreshObjectPool();

    public NpcPool() {

    }

    public void addNpc(Npc npc,boolean visible){
        short key = (short)(npc.getId()>>16);
        pool.addObject(new Short(key),new Integer(npc.getId()),npc,visible);
    }

    public void addNpc(Npc npc){
        boolean visible = ((npc.getFlag()&0x02)==0?false:true);
        addNpc(npc,visible);
    }

    public void setVisible(int id,boolean visible){
        pool.setVisible(new Integer(id),visible);
    }

    public List getVisibleNpcs(short stageId){
        return pool.getVisibleObjects(new Short(stageId));
    }

    public Npc getNpc(int id){
        return (Npc)pool.getObject(new Integer(id));
    }

    public boolean isVisible(int id){
        return pool.isVisible(new Integer(id));
    }
}
