package com.pip.itimes.server.stage;

import java.util.List;
import java.util.ArrayList;
import com.pip.itimes.server.util.ListMap;
import java.util.Map;
import java.util.HashMap;

/**
 * @author Jeffery
 * @version 1.0
 */
public class RefreshObjectPool {

    private ListMap objects = new ListMap();
    private Map map = new HashMap();

    public RefreshObjectPool() {
    }

    public void addObject(Object firstKey,Object secondKey,Object o,boolean visible){
        ObjectWrapper wrapper = new ObjectWrapper(o,visible);
        objects.put(firstKey,wrapper);
        map.put(secondKey,wrapper);
    }

    public List getVisibleObjects(Object key){
        List l = objects.getList(key);
        List retList = new ArrayList();
        synchronized(l){
            for(int i=0;i<l.size();i++){
                ObjectWrapper wrapper = (ObjectWrapper)l.get(i);
                if(wrapper.isVisible()){
                    retList.add(wrapper.o);
                }
            }
        }
        return retList;
    }

    public Object getObject(Object key){
        ObjectWrapper wrapper = (ObjectWrapper)map.get(key);
        if(wrapper==null)
            return null;
        return wrapper.o;
    }

    public void setVisible(Object key,boolean visible){
        ObjectWrapper wrapper = (ObjectWrapper)map.get(key);
        if(wrapper!=null){
            wrapper.setVisible(visible);
        }
    }

    public boolean isVisible(Object key){
        ObjectWrapper wrapper = (ObjectWrapper)map.get(key);
        if(wrapper!=null){
            return wrapper.isVisible();
        }
        return false;
    }


    class ObjectWrapper{
        Object o;
        boolean visible;
        public ObjectWrapper(Object o,boolean visible){
            this.o = o;
            this.visible = visible;
        }

        public void setVisible(boolean visible){
            this.visible = visible;
        }

        public boolean isVisible(){
            return visible;
        }
    }

}
