package com.pip.itimes.server.world.taskHelp;

import java.util.Vector;

/**
 * @author wpjiang
 *	用于脚本演示和关联的 ui辅助类
 *	按键规则为 如果有快捷键的话，将键值，对应主菜单消息， 时间等分别插入到 vector最前面
 *	客户端根据要下发的快捷键值来判断是否需要剥离第一个按键
 *	其他的键值等顺序则按照的是先进入主菜单等，依次读出
 */
public class TaskHelp {
	



	public String getUiName() {
		return uiName;
	}


	public void setUiName(String uiName) {
		this.uiName = uiName;
	}


	public short getTaskId() {
		return taskId;
	}


	public void setTaskId(short taskId) {
		this.taskId = taskId;
	}


	public short getTaskRewardId() {
		return taskRewardId;
	}


	public void setTaskRewardId(short taskRewardId) {
		this.taskRewardId = taskRewardId;
	}


	public Vector getKeyVector() {
		return keyVector;
	}


	public void setKeyVector(Vector keyVector) {
		this.keyVector = keyVector;
	}


	public Vector getKeyTimeVector() {
		return keyTimeVector;
	}


	public void setKeyTimeVector(Vector keyTimeVector) {
		this.keyTimeVector = keyTimeVector;
	}


	public Vector getUiWaitMessage() {
		return uiWaitMessage;
	}


	public void setUiWaitMessage(Vector uiWaitMessage) {
		this.uiWaitMessage = uiWaitMessage;
	}


	public int getFastKey() {
		return fastKey;
	}

	public void setFastKey(int fastKey) {
		this.fastKey = fastKey;
	}

	public int getHelpLevel() {
		return helpLevel;
	}


	public void setHelpLevel(int helpLevel) {
		this.helpLevel = helpLevel;
	}
	
	/**
	 * 脚本关联的ui名称
	 */
	private String uiName;
	/**
	 * ui的相关的任务id
	 */
	private short taskId;
	/**
	 * 奖励的相关任务分支id
	 */
	private short taskRewardId;
	
	/**
	 * 用于生成相关的ui演示按键键值
	 */
	private Vector keyVector;
	

	/**
	 * 相关按键的键值等待时间
	 */
	private Vector keyTimeVector;
	
	/**
	 * ui模拟中的每一步等待消息
	 */
	private Vector uiWaitMessage;

	/**
	 * 快捷键键值  -1表示没有快捷键
	 */
	private int fastKey;
	
	/**
	 * 演示需要的等级，因为要演示动画，有的前提需要限制等级， 比如属性点
	 */
	private int helpLevel;

}
