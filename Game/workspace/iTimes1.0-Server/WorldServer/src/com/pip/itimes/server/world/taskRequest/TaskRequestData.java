package com.pip.itimes.server.world.taskRequest;

import java.util.Hashtable;

public class TaskRequestData {
	public static Hashtable<Integer, TaskRequest> taskRequests = new Hashtable<Integer, TaskRequest>(); 
	
	static public TaskRequest endTaskRequest(short taskid){
		if(taskRequests.containsKey(new Integer(taskid))){
			TaskRequest request = taskRequests.get(new Integer(taskid));
			return request;
		}
		return null;
	}
	
}
