package com.pip.itimes.server.world.game;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.pip.itimes.server.world.InstanceDefinition;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class InstanceService {


    private Map id2definitions = new HashMap();
    private Map<Short,InstanceDefinition> mapid2definitions = new HashMap<Short,InstanceDefinition>();
    private Map<Short,InstanceDefinition> entrance2definitions = new HashMap<Short,InstanceDefinition>();

    private static AtomicInteger instanceId = new AtomicInteger(1);

    private Map id2instances = new HashMap();

    public InstanceService() {
    }



    public void addDefinition(InstanceDefinition definition) {
        id2definitions.put(new Integer(definition.getId()), definition);
        short[] maps = definition.getMaps();
        for (int j = 0; j < maps.length; j++) {
            mapid2definitions.put(maps[j], definition);
        }
        entrance2definitions.put(definition.getEntrance(),definition);
    }

    public InstanceDefinition getInstanceDefintionByMap(short mapId){
        return mapid2definitions.get(mapId);
    }

    public InstanceDefinition getInstanceDefinitionByEntrance(short mapId){
        return entrance2definitions.get(mapId);
    }

    public InstanceDefinition getInstanceDefinition(int instanceId){
        return (InstanceDefinition)id2definitions.get(new Integer(instanceId));
    }
    
    public Instance getInstance (int ID) {
        return (Instance) id2instances.get(ID);
    }

    public static int getNewInstanceId(){
            return instanceId.incrementAndGet();
    }

    public void addInstance(Instance instance){
        id2instances.put(new Integer(instance.getId()),instance);
    }


    public void instanceEmpty(Instance instance){
        id2instances.remove(new Integer(instance.getId()));
    }
}
