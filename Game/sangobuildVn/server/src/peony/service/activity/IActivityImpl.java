package peony.service.activity;

import peony.service.Service;

public interface IActivityImpl extends Service {
	/**
	 * 获得隶属的活动对象。
	 */
	public Activity getActivity();
	/**
	 * 把当前活动状态保存起来。用于服务器shutdown的时候保存状态。
	 */
	public void save();
	/**
	 * 载入先前保存的状态。用于服务器startup的时候载入上次状态。
	 */
	public void load();
	/**
	 * 清除所有和此活动有关的存储数据。
	 */
	public void clear();
}
