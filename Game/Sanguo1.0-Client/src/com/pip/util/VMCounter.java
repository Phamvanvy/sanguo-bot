package com.pip.util;

import java.util.Enumeration;
import java.util.Hashtable;

import com.pip.common.Tool;
import com.pip.common.Utilities;

//计时器类
public class VMCounter {
	private static Hashtable counters = new Hashtable();
	private static Tool keyMaker = new Tool();
	
	private int counterTime; //计时时间
	private long startTime;  //开始时间
	public static final byte COUNTER_TYPE_SERVER_TIME_BASED = 0;
	public static final byte COUNTER_TYPE_CLIENT_TIME_BASED = 1;
	private byte type = 0;
	
	private VMCounter(int counterTime) {
		this.counterTime = counterTime;
	}
	
	/**
	 * 创建一个计时器，并添加到计时器组中
	 * 
	 * @param countTime 计时时间
	 * @return VMCounter对象的key
	 */
	public static int createVMCounter(int counterTime) {
		VMCounter vc = new VMCounter(Math.abs(counterTime));
		if(counterTime > 0){
			vc.type = COUNTER_TYPE_SERVER_TIME_BASED;
			vc.startTime = Utilities.getServerTime();
		} else {
			vc.type = COUNTER_TYPE_CLIENT_TIME_BASED;
			vc.startTime = Tool.getSystemTime();
		}
		
		int key = keyMaker.nextKey();
		counters.put(new Integer(key), vc);
		
		return key;
	}
	
	/**
	 * 创建一个计时器，并添加到计时器组中
	 * @param startTime 开始时间
	 * @param counterTime 计时时间
	 * @return VMCounter对象的key
	 */
	public static int createVMCounter(int startTime, int counterTime) {
	    int key = createVMCounter(counterTime);
	    setCounter(key, startTime, counterTime);
	    return key;
	}
	
	
	/**
	 * 删除一个计时器
	 * @param key
	 */
	public static void removeVMCounter(int key) {
		counters.remove(new Integer(key));
	}
	
	/**
	 * 删除所有计时器
	 */
	public static void removeAllVMCounters() {
		counters.clear();
	}
	
	public static void cycle() {
		Enumeration enum2 = counters.keys();
		Hashtable newCounters = new Hashtable();
		
		while(enum2.hasMoreElements()) {
			Object key = enum2.nextElement();
			VMCounter counter = (VMCounter)counters.get(key);
			if(counter.type == COUNTER_TYPE_SERVER_TIME_BASED){
				if((Utilities.getServerTime() - counter.startTime) <  counter.counterTime) {
					newCounters.put(key, counter);
				}
			} else {
				if((Tool.getSystemTime() - counter.startTime) <  counter.counterTime) {
					newCounters.put(key, counter);
				}
			}

		}
		
		counters = newCounters;
	}
	
	/**
	 * 逝去时间占全部时间的百分比， -1表示已经超时
	 * @return
	 */
	public static int getProcess(int key) {
		VMCounter vc = (VMCounter)counters.get(new Integer(key));
		if(vc != null) {
			if(vc.type == COUNTER_TYPE_SERVER_TIME_BASED){
				return (int)(Utilities.getServerTime() - vc.startTime) * 100 / vc.counterTime;	
			} else {
				return (int)(Tool.getSystemTime() - vc.startTime) * 100 / vc.counterTime;	
			}
		} else {
			return -1;
		}			
	}
	
	/**
	 * 
	 * @return 逝去的时间, -1表示已经超时
	 */
	public static int getElapseTime(int key) {
		VMCounter vc = (VMCounter)counters.get(new Integer(key));
		if(vc != null) {
			if(vc.type == COUNTER_TYPE_SERVER_TIME_BASED){
				return (int)(Utilities.getServerTime() - vc.startTime);	
			} else {
				return (int)(Tool.getSystemTime() - vc.startTime);	
			}
		} else {
			return -1;
		}	
	}
	
	/**
	 * 
	 * @return 剩余的秒数, 小于0表示已经超时
	 */
	public static int getSaveTimeSec(int key) {
		VMCounter vc = (VMCounter)counters.get(new Integer(key));
		if(vc != null) {
			if(vc.type == COUNTER_TYPE_SERVER_TIME_BASED){
				return (vc.counterTime - (int)(Utilities.getServerTime() - vc.startTime)) / 1000;
			} else {
				return (vc.counterTime - (int)(Tool.getSystemTime() - vc.startTime)) / 1000;
			}
		} else {
			return -1;
		}	
	}
	
	/**
	 * 
	 * @return 剩余的毫秒数, 小于0表示已经超时
	 */
	public static int getSaveTimeMillis(int key) {
		VMCounter vc = (VMCounter)counters.get(new Integer(key));
		if(vc != null) {
			if(vc.type == COUNTER_TYPE_SERVER_TIME_BASED){
				return vc.counterTime - (int)(Utilities.getServerTime() - vc.startTime);	
			} else {
				return vc.counterTime - (int)(Tool.getSystemTime() - vc.startTime);	
			}
		} else {
			return -1;
		}	
	}
	
	/**
	 * 设置计时器时间
	 * @param key
	 * @param startTime
	 * @param counterTime
	 */
	public static void setCounter(int key, int startTime, int counterTime){
	    VMCounter vc = (VMCounter)counters.get(new Integer(key));
        if(vc != null) {
            vc.startTime = startTime;
            vc.counterTime = counterTime;
        }
	}
}
