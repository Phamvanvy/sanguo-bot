package com.pip.itimes.server.stage;

import java.util.List;

/**
 * @author Jeffery
 * @version 1.0
 */
public class ResourcePool {

    private RefreshObjectPool pool = new RefreshObjectPool();

    public ResourcePool() {
    }

    public void addResource(Resource resource,boolean visible){
        short key = (short)(resource.getId()>>16);
        pool.addObject(new Short(key),new Integer(resource.getId()),resource,visible);
    }

    public void addResource(Resource resource){
        addResource(resource,true);
    }

    public void setVisible(int id,boolean visible){
        pool.setVisible(new Integer(id),visible);
    }

    public List getVisibleResource(short stageId){
        return pool.getVisibleObjects(new Short(stageId));
    }

    public Resource getResource(int id){
        return (Resource)pool.getObject(new Integer(id));
    }

    public boolean isVisible(int id){
        return pool.isVisible(new Integer(id));
    }
}
