package com.pipimage.image.path;

import java.util.Random;

import com.pipimage.image.PipParticlePath;

/**
 * 盘旋上升轨迹。
 * @author lighthu
 */
public class Helix2Path implements PipParticlePath {
	private double distance;		// 离心距离
	private double leaveSpeed;		// 离心速度
	private double leaveAcceleration;	// 离心加速度
	private int angle;				// 初始角度
	private int angleRange;			// 初始角度随机范围
	private double angleSpeed;		// 角速度
	private double angleAcceleration;	// 角加速度
	private double zSpeed;			// z轴速度
	private double zAcceleration;	// z轴加速度
	private int showPart;			// 0 - 全部显示，1 - 只显示前半部分，2 - 只显示后半部分
	
	private static String[] PARAM_NAMES = {
		"离心距离", "离心速度", "离心加速度", "初始角度", "初始角度随机范围", "角速度", "角加速度", "z轴速度", "z轴加速度", "显示部分"
	};
	private static String[] PARAM_DESCS = {
		"初始离心距离(像素)", 
		"初始离心速度(像素/TICK)", 
		"离心加速度(像素/TICK2)",
		"初始角度(0-360的整数)",
		"初始角度随机范围(0-180的整数)",
		"初始角速度(度/TICK)",
		"角加速度(度/TICK2)",
		"初始z轴速度(像素/TICK)",
		"z轴加速度(像素/TICK2)",
		"显示部分(0全部1前半2后半)"
	};
	
	/** 取得轨迹名称 */
	public String getTypeName() {
		return "螺旋上升";
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
			return distance;
		case 1:
			return leaveSpeed;
		case 2:
			return leaveAcceleration;
		case 3:
			return angle;
		case 4:
			return angleRange;
		case 5:
			return angleSpeed;
		case 6:
			return angleAcceleration;
		case 7:
			return zSpeed;
		case 8:
			return zAcceleration;
		case 9:
			return showPart;
		}
		return 0.0;
	}
	
	/** 设置某个参数的值 */
	public void setParam(int index, double value) {
		switch (index) {
		case 0:
			distance = value;
			break;
		case 1:
			leaveSpeed = value;
			break;
		case 2:
			leaveAcceleration = value;
			break;
		case 3:
			angle = (int)value;
			break;
		case 4:
			angleRange = (int)value;
			break;
		case 5:
			angleSpeed = value;
			break;
		case 6:
			angleAcceleration = value;
			break;
		case 7:
			zSpeed = value;
			break;
		case 8:
			zAcceleration = value;
			break;
		case 9:
			showPart = (int)value;
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
		double currentDistance = distance;
		int initAngle = angle;
		if (angleRange > 0) {
			initAngle += rand.nextInt(angleRange * 2) - angleRange;
		}
		double currentAngle = (initAngle % 360) * Math.PI * 2 / 360.0;
		double z = 0.0;
		double currentZSpeed = zSpeed;
		double currentLeaveSpeed = leaveSpeed;
		double currentAngleSpeed = angleSpeed;
		
		int[][] ret = new int[liveTime][2];
		for (int i = 0; i < liveTime; i++) {
			// 根据当前离心距离和角度，计算相对圆心的x和y轴距离
			double xoff = currentDistance * Math.cos(currentAngle);
			double yoff = -currentDistance * Math.sin(currentAngle);
			ret[i][0] = (int)(x + xoff);
			ret[i][1] = (int)(y + yoff / 2 - z);
			if (showPart == 1 && yoff < 0) {
				// 只显示前半部分
				ret[i][0] = -1000;
				ret[i][1] = -1000;
			} else if (showPart == 2 && yoff > 0) {
				// 只显示后半部分
				ret[i][0] = -1000;
				ret[i][1] = -1000;
			}
			
			// 根据离心速度调整离心距离
			currentDistance += currentLeaveSpeed;
			if (currentDistance < 0) {
				currentDistance = 0;
			}
			
			// 根据角速度调整角度
			currentAngle += currentAngleSpeed * Math.PI * 2 / 360.0;
			
			// 根据Z速度调整Z位置
			z += currentZSpeed;
			
			// 根据加速度调整速度
			currentLeaveSpeed += leaveAcceleration;
			currentAngleSpeed += angleAcceleration;
			currentZSpeed += zAcceleration;
		}
		return ret;
	}
	
	/** 复制自身 */
	public PipParticlePath dup() {
		Helix2Path ret = new Helix2Path();
		ret.distance = distance;
		ret.leaveSpeed = leaveSpeed;
		ret.leaveAcceleration = leaveAcceleration;
		ret.angle = angle;
		ret.angleRange = angleRange;
		ret.angleSpeed = angleSpeed;
		ret.angleAcceleration = angleAcceleration;
		ret.zSpeed = zSpeed;
		ret.zAcceleration = zAcceleration;
		ret.showPart = showPart;
		return ret;
	}
}
