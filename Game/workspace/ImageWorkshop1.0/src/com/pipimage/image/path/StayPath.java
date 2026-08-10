package com.pipimage.image.path;

import java.util.Random;

import com.pipimage.image.PipParticlePath;

/**
 * 原地不动的轨迹。
 * @author lighthu
 */
public class StayPath implements PipParticlePath {
	/** 取得轨迹名称 */
	public String getTypeName() {
		return "静止";
	}

	/** 取得参数数量 */
	public int getParamCount() {
		return 0;
	}
	
	/** 取得参数名称 */
	public String getParamName(int index) {
		return "";
	}
	
	/** 取得参数描述 */
	public String getParamDesc(int index) {
		return "";
	}
	
	/** 取得某个参数的值 */
	public double getParam(int index) {
		return 0.0;
	}
	
	/** 设置某个参数的值 */
	public void setParam(int index, double value) {
	}
	
	/** 
	 * 生成一个粒子的运动轨迹，返回的数组中第一个元素是起始位置。
	 * @param x 起始x位置
	 * @param y 起始y位置
	 * @param liveTime 生存时间
	 * @param rand 随机数生成器
	 * @return 每个元素是2个整数，对应轨迹上的一个点
	 */
	public int[][] makePath(int x, int y, int liveTime, Random rand) {
		int[][] ret = new int[liveTime][2];
		for (int i = 0; i < liveTime; i++) {
			ret[i][0] = x;
			ret[i][1] = y;
		}
		return ret;
	}
	
	/** 复制自身 */
	public PipParticlePath dup() {
		return new StayPath();
	}
}
