/**
 * @author leo
 */
package com.pip.log.define;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;

import com.pip.log.define.processor.LogProcessor;

/**
 * @author leo
 *
 */
public class Definitions{
    private static final ConcurrentHashMap<String, LogDefine> id2logDefine = new ConcurrentHashMap<String, LogDefine>();

    @SuppressWarnings("unchecked")
    public static final void loadDefinitions() throws Exception{
        try{
            XMLConfiguration config = new XMLConfiguration("logdefine.xml");
            List<SubnodeConfiguration> list = config.configurationsAt("define");

            for(SubnodeConfiguration node : list){
                //载入LogDefine
                LogDefine define = new LogDefine(node);
                if(id2logDefine.containsKey(define.getId())){
                    throw new Exception("LogDefine duplicated : " + define.getId());
                }
                id2logDefine.put(define.getId(), define);

                //载入LogProcessor
                List<SubnodeConfiguration> list1 = node.configurationsAt("processor");
                for(SubnodeConfiguration node1 : list1){
                    LogProcessor processor = LogProcessor.loadProcessor(define, node1);
                    define.addLogProcessor(processor.getId(), processor);
                }

                //载入LogItem
                List<SubnodeConfiguration> list2 = node.configurationsAt("item");
                for(SubnodeConfiguration node2 : list2){
                    LogItem item = LogItem.loadItem(define, node2);
                    define.addLogItem(item.getId(), item);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        try{
            XMLConfiguration config = new XMLConfiguration("logs.xml");
            List<SubnodeConfiguration> list = config.configurationsAt("gamelog");

            for(SubnodeConfiguration node : list){
                String id = node.getString("id");
                LogDefine define = id2logDefine.get(id);

                //载入loadType
                List<SubnodeConfiguration> list1 = node.configurationsAt("log");
                for(SubnodeConfiguration node1 : list1){
                    LogType type = LogType.loadType(define, node1);
                    define.addLogType(type.getId(), type);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static final LogDefine getLogDefine(String id){
        return id2logDefine.get(id);
    }
}
