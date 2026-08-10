package com.pip.itimes.server.stage;

import java.util.List;

/**
 * @author Jeffery
 * @version 1.0
 */
public class MonsterGroupPool {

    private RefreshObjectPool pool = new RefreshObjectPool();

    public MonsterGroupPool() {
    }


    public void addMonsterGroup(MonsterGroup mg,boolean visible){
        short key = (short)(mg.getId()>>16);
        pool.addObject(new Short(key),new Integer(mg.getId()),mg,visible);
    }

    public void addMonsterGroup(MonsterGroup mg){
        boolean visible = ((mg.getType()&0x02)==0?false:true);
        addMonsterGroup(mg,visible);
    }

    public void setVisible(int id,boolean visible){
        pool.setVisible(new Integer(id),visible);
    }

    public List getVisibleMonsterGroup(short stageId){
        return pool.getVisibleObjects(new Short(stageId));
    }

    public MonsterGroup getMonsterGroup(int id){
        return (MonsterGroup)pool.getObject(new Integer(id));
    }

    public boolean isVisible(int id){
        return pool.isVisible(new Integer(id));
    }
}
