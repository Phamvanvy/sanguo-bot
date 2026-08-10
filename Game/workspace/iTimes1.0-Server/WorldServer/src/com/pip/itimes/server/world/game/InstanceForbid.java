package com.pip.itimes.server.world.game;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class InstanceForbid {
	// 记录副本进入次数
	public Map<Integer, Integer> instanceCount;		//<instanceId,count>
	// 记录副本进入时间
	public List instanceTime;		//<InstanceIdDate> InstanceIdDate{instancId,Date}
	
	public InstanceForbid() {
		instanceCount = new HashMap<Integer,Integer>();
		instanceTime = new ArrayList();
	}
	
	public class InstanceIdDate{
		int instanceid;
		Date date;
		int id;
	}

	public boolean testGotoInsCount(int instanceId ,int maxTime){
		if(instanceCount.containsKey(instanceId)){
			// add Jeremy:判断每小时里存的副本进入时间超过24小时就清空此条记录
			for(int j = 0;j< instanceTime.size();j++){
				InstanceIdDate iid = (InstanceIdDate)instanceTime.get(j);
				if(instanceId == iid.instanceid){
					if(System.currentTimeMillis() - iid.date.getTime() > 24 * 3600 * 1000L){
						instanceCount.remove(instanceId);
        			}
				}
			}
			int tmpTime = instanceCount.get(instanceId);
			if(tmpTime < maxTime){
				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}
	}
	
	public boolean testGotoInsNew(int instanceId, int id){
		if(instanceCount.containsKey(instanceId)){
			for(int j = 0;j< instanceTime.size();j++){
				InstanceIdDate iid = (InstanceIdDate)instanceTime.get(j);
				if(instanceId == iid.instanceid && id == iid.id){
					return false;
				}
			}
			return true;
		} else {
			return true;
		}
	}
	
	public void addGotoInsCount(int instanceId){
		if(instanceCount.containsKey(instanceId)){
			int tmpTime = instanceCount.get(instanceId);
			instanceCount.put(instanceId, ++tmpTime);
		} else {
			instanceCount.put(instanceId, 1);
		}
	}
	
	public boolean testGotoInsHourTime(int instanceid, Date date){
		if(instanceTime.size() > 0){
			if(instanceTime.size() < 3){
				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}
	}
	
	public boolean testGotoInsHourTime2(int instanceId,int maxTime){
		if(instanceCount.containsKey(instanceId)){
			for(int j = 0;j< instanceTime.size();j++){
				InstanceIdDate iid = (InstanceIdDate)instanceTime.get(j);
				if(instanceId == iid.instanceid){
					if(System.currentTimeMillis() - iid.date.getTime() > 3600 * 1000L){
						instanceCount.remove(instanceId);
        			}
				}
			}
			int tmpTime = instanceCount.get(instanceId);
			if(tmpTime < maxTime){
				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}
	}
	
	public void addGotoInsHourTime(int instanceid, Date date, int id){
		if(instanceTime.size() > 0){
			if(instanceTime.size() < 3){
				InstanceIdDate insD = new InstanceIdDate();
				insD.instanceid = instanceid;
				insD.date = date;
				insD.id = id;
				instanceTime.add(insD);
			}
		} else {
			InstanceIdDate insD = new InstanceIdDate();
			insD.instanceid = instanceid;
			insD.date = date;
			insD.id = id;
			instanceTime.add(insD);
		}
	}
	
	public void removeInstanceHourTime(int instanceid){
		for(int j = 0;j< instanceTime.size();j++){
			InstanceIdDate iid = (InstanceIdDate)instanceTime.get(j);
			if(instanceid == iid.instanceid){
				instanceTime.remove(j);
			}
		}
	}
	
	public void removeInstanceCount(int instanceId){
		if(instanceCount.containsKey(instanceId)){
			int tmpTime = instanceCount.get(instanceId);
			instanceCount.put(instanceId, --tmpTime);
		}
	}
}
