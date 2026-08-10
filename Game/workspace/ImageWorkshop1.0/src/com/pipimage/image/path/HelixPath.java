package com.pipimage.image.path;

import java.util.Random;

import com.pipimage.image.PipParticlePath;

/**
 * 螺旋投射轨迹。
 * @author lighthu
 */
public class HelixPath implements PipParticlePath {
	private double speed;			// 投射速度
	private double acceleration;	// 加速度
	private double gravity;			// 重力加速度
	private int angle;				// 投射角度
	private int angleRange;			// 投射角度随机范围 
	
	private static String[] PARAM_NAMES = {
		"投射速度", "加速度", "重力加速度", "投射角度", "投射角度随机范围"
	};
	private static String[] PARAM_DESCS = {
		"从投射点投射时的起始速度(像素/TICK)", 
		"投射加速度(像素/TICK2)", 
		"重力加速度(像素/TICK2)",
		"投射角度(0-359的整数)", 
		"投射角度随机范围(0-180的整数)"
	};
	
	/** 取得轨迹名称 */
	public String getTypeName() {
		return "螺旋";
	}

	/** 取得参数数量 */
	public int getParamCount() {
		return PARAM_NAMES.length;
	}
	
	/** 取得参数名称 */
	public String getParamName(int index) {
		return PARAM_NAMES[index];
	}
	
	/** 取得参数描述 */
	public String getParamDesc(int index) {
		return PARAM_DESCS[index];
	}
	
	/** 取得某个参数的值 */
	public double getParam(int index) {
		switch (index) {
		case 0:
			return speed;
		case 1:
			return acceleration;
		case 2:
			return gravity;
		case 3:
			return angle;
		case 4:
			return angleRange;
		}
		return 0.0;
	}
	
	/** 设置某个参数的值 */
	public void setParam(int index, double value) {
		switch (index) {
		case 0:
			speed = value;
			break;
		case 1:
			acceleration = value;
			break;
		case 2:
			gravity = value;
			break;
		case 3:
			angle = (int)value;
			break;
		case 4:
			angleRange = (int)value;
			break;
		}
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
		double currentSpeed = speed;
		int initAngle = angle;
		if (angleRange > 0) {
			initAngle += rand.nextInt(angleRange * 2) - angleRange;
		}
		double currentAngle = (initAngle % 360) * Math.PI * 2 / 360.0;
		int[][] ret = new int[liveTime][2];
		double curx = x;
		double cury = y;
		for (int i = 0; i < liveTime; i++) {
			ret[i][0] = (int)curx;
			ret[i][1] = (int)cury;
			
			// 根据当前速度和重力加速度计算新的飞行角度
			double angleOff = Math.atan(gravity / currentSpeed);
			currentAngle -= angleOff;
			if (currentAngle < 0) {
				currentAngle += Math.PI * 2;
			} else if (currentAngle >= Math.PI * 2) {
				currentAngle -= Math.PI * 2;
			}
			
			// 根据飞行角度计算下一个点的位置
			curx += currentSpeed * Math.cos(currentAngle);
			cury -= currentSpeed * Math.sin(currentAngle);
			
			// 根据加速度调整速度
			currentSpeed += acceleration;
		}
		return ret;
	}
	
	/** 复制自身 */
	public PipParticlePath dup() {
		HelixPath ret = new HelixPath();
		ret.speed = speed;
		ret.acceleration = acceleration;
		ret.gravity = gravity;
		ret.angle = angle;
		ret.angleRange = angleRange;
		return ret;
	}
}
