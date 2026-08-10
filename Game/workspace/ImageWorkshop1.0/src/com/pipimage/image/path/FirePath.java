package com.pipimage.image.path;

import java.util.Random;

import com.pipimage.image.PipParticlePath;

/**
 * 模拟火焰离子的上升过程。
 * @author lighthu
 */
public class FirePath implements PipParticlePath {
	private double speed;			// 初始速度
	private int speedRange;			// 速度随机范围
	private double gravitation;		// 引力
	private double valveSpeed;		// 回吸速度阈值
	private double zSpeed;			// z轴速度
	private double zAcceleration;	// z轴加速度
	private int zSpeedRange;		// z轴速度随机范围
	
	private static String[] PARAM_NAMES = {
		"初始速度", "速度随机范围", "回吸速度阈值", "引力", "z轴速度", "z轴加速度", "z轴速度随机范围"
	};
	private static String[] PARAM_DESCS = {
		"初始速度(像素/TICK)", 
		"速度随机范围(像素/TICK)",
		"回吸速度阈值(像素/TICK)",
		"引力(像素/TICK2)",
		"初始z轴速度(像素/TICK)",
		"z轴加速度(像素/TICK2)",
		"z轴速度随机范围(像素/TICK)"
	};
	
	/** 取得轨迹名称 */
	public String getTypeName() {
		return "模拟火焰";
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
			return speedRange;
		case 2:
			return valveSpeed;
		case 3:
			return gravitation;
		case 4:
			return zSpeed;
		case 5:
			return zAcceleration;
		case 6:
			return zSpeedRange;
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
			speedRange = (int)value;
			break;
		case 2:
			valveSpeed = value;
			break;
		case 3:
			gravitation = value;
			break;
		case 4:
			zSpeed = value;
			break;
		case 5:
			zAcceleration = value;
			break;
		case 6:
			zSpeedRange = (int)value;
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
		double initSpeed = speed;
		if (speedRange > 0) {
			initSpeed += rand.nextInt(speedRange * 2) - speedRange;
		}
		double currentSpeed = initSpeed;
		double currentZSpeed = zSpeed;
		if (zSpeedRange > 0) {
			currentZSpeed += rand.nextInt(zSpeedRange * 2) - zSpeedRange;
		}
		double z = 0.0;
		double curx = x;
		double cury = y;
		boolean reverse = false;
		double reverseRate = 0.0f;
		
		int[][] ret = new int[liveTime][2];
		for (int i = 0; i < liveTime; i++) {
			ret[i][0] = (int)curx;
			ret[i][1] = (int)(cury - z);
			
			// 根据速度和z速度调整x和z值
			curx += currentSpeed;
			z += currentZSpeed;
			
			// 根据加速度调整速度
			if (initSpeed > 0) {
				if (currentSpeed < -valveSpeed && !reverse) {
					reverse = true;
					reverseRate = currentSpeed / (curx - x);
				}
				if (reverse) {
					currentSpeed = reverseRate * (curx - x);
				} else {
					currentSpeed -= gravitation;
				}
				if (curx < x) {
					curx = x;
					currentSpeed = 0;
				}
			} else if (initSpeed < 0) {
				if (currentSpeed > valveSpeed && !reverse) {
					reverse = true;
					reverseRate = currentSpeed / (x - curx);
				}
				if (reverse) {
					currentSpeed = reverseRate * (x - curx);
				} else {
					currentSpeed += gravitation;
				}
				if (curx > x) {
					curx = x;
					currentSpeed = 0;
				}
			}
			currentZSpeed += zAcceleration;
		}
		return ret;
	}
	
	/** 复制自身 */
	public PipParticlePath dup() {
		FirePath ret = new FirePath();
		ret.speed = speed;
		ret.speedRange = speedRange;
		ret.valveSpeed = valveSpeed;
		ret.gravitation = gravitation;
		ret.zSpeed = zSpeed;
		ret.zAcceleration = zAcceleration;
		ret.zSpeedRange = zSpeedRange;
		return ret;
	}
}
