package com.pip.servermgr.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 服务器执行时间超长的报告。
 * @author lighthu
 */
public class LongLogReport {
	public HashMap<Integer, List<Integer>> opcodeTime = new HashMap<Integer, List<Integer>>();
	public HashMap<String, List<Integer>> callTime = new HashMap<String, List<Integer>>();
	
	public void addOpCodeTime(int code, int time) {
		List<Integer> list = opcodeTime.get(code);
		if (list == null) {
			list = new ArrayList<Integer>();
			opcodeTime.put(code, list);
		}
		list.add(time);
	}
	
	public void addCallTime(String call, int time) {
		List<Integer> list = callTime.get(call);
		if (list == null) {
			list = new ArrayList<Integer>();
			callTime.put(call, list);
		}
		list.add(time);
	}
}
