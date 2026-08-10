package com.pip.servermgr.report;

public interface IPlayer {
	/**
	 * 取得某个统计项数据。
	 * @param type 参见DataFilter里的常量
	 * @return 可能是Boolean, Integer, Float
	 */
	public Object getValue(int type);
}
