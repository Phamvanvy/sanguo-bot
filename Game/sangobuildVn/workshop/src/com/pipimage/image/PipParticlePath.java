package com.pipimage.image;

import java.util.Random;

/**
 * 定义一个粒子的移动轨迹生成算法。
 * @author lighthu
 */
public interface PipParticlePath {
	/** 取得轨迹名称 */
	String getTypeName();
	/** 取得参数数量 */
	int getParamCount();
	/** 取得参数名称 */
	String getParamName(int index);
	/** 取得参数描述 */
	String getParamDesc(int index);
	/** 取得某个参数的值 */
	double getParam(int index);
	/** 设置某个参数的值 */
	void setParam(int index, double value);
	/** 
	 * 生成一个粒子的运动轨迹，返回的数组中第一个元素是起始位置。
	 * @param x 起始x位置
	 * @param y 起始y位置
	 * @param liveTime 生存时间
	 * @param rand 随机数生成器
	 * @return 每个元素是2个整数，对应轨迹上的一个点
	 */
	int[][] makePath(int x, int y, int liveTime, Random rand);
	/** 复制自身 */
	PipParticlePath dup();
}
