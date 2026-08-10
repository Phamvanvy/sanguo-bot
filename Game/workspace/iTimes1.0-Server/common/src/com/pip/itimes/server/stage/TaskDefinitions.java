package com.pip.itimes.server.stage;

import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.collections.primitives.ShortList;
import org.apache.commons.collections.primitives.ArrayShortList;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class TaskDefinitions {

    private static final Map definitions = new TreeMap();
    private static final short[] EMPTY_RELATION = new short[0];

    public static void addTaskDefinition(short taskId,short[] taskIds,String desc){
        TaskDefinition definition = new TaskDefinition();
        definition.taskId = taskId;
        definition.taskIds = taskIds;
        definition.desc = desc;
        definitions.put(new Short(taskId),definition);
    }

    public static short[] getDefinitions(short taskId){
        TaskDefinition relation = (TaskDefinition)definitions.get(new Short(taskId));
        if(relation==null||relation.taskIds==null)
            return EMPTY_RELATION;
        return relation.taskIds;
    }

    public static short[] getDefinitions(short[] taskId){
        ShortList ret = new ArrayShortList();
        for(int i=0;i<taskId.length;i++){
            short[] ids = getDefinitions(taskId[i]);
            for(int j=0;j<ids.length;j++){
                if(!ret.contains(ids[j])){
                    ret.add(ids[j]);
                }
            }
        }
        return ret.toArray();
    }

    public static String getTaskDesc(short taskId,PlayerData player){
        TaskDefinition relation = (TaskDefinition)definitions.get(new Short(taskId));

        String result = "";
        String taskdesc = "";
        if(relation != null && relation.desc != null){
            result = relation.desc;
        }
        result.trim();
        result = result.replace("\n\n", "\n");
        int idx = result.indexOf("TaskObject:");
        
        if(idx >= 0){
            String tmp = result.substring(idx + "TaskObject:".length());
            taskdesc = result.substring(0, idx);
            result = "任务目标：";
            int countint = 0;
            while(!tmp.trim().equals("end") || tmp.trim().length() < "end".length()){
                if (countint > 0){
                	//result += " ;\n                      ";
                	result += " ;\n　　　　　";
                }
                countint++;
                
                int idx1 = tmp.indexOf(",");
                
                if(idx1 < 0){
                    break;
                }
                
                String itemDataStr = tmp.substring(0, idx1);
                
                int idx2 = itemDataStr.indexOf(":");
                String itemIdStr = itemDataStr.substring(0, idx2);
                String itemCountStr = itemDataStr.substring(idx2 + 1);
                int itemId = Integer.parseInt(itemIdStr);
                int itemCount = Integer.parseInt(itemCountStr);
                
                String itemName = Items.getTemplate(itemId).getName();
                int count = 0;
                
                Grid items = player.getItem(itemId, 0);
                
                
                if(items != null){
                    count = items.count;
                }
                
                result += itemName + " " + count + "/" + itemCount;
                
                if(count >= itemCount){
                    result += " (完成)";
                }
                
                tmp = tmp.substring(idx1 + 1);
            }
        }
        taskdesc = taskdesc.replace("\n", "");
        result = result + "\n    " + taskdesc;
        
        if(result != null){
        	char c = 0;
        	for(int i=result.length() - 1; i>0; i--){
        		c = result.charAt(i);
        		if(c != '\n' && c != ' '){
        			result = result.substring(0, i + 1);
        			break;
        		}
        	}
        }
        
        return result;
    }
}
