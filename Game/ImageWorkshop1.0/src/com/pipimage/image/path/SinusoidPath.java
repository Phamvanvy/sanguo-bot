package com.pipimage.image.path;

import java.util.Random;

import com.pipimage.image.PipParticlePath;

/**
 * 正弦曲线轨迹。
 * @author lighthu
 */
public class SinusoidPath implements PipParticlePath {
	private double peak;			// 峰值
	private double peakSpeed;		// 峰值变化速度
	private double xSpeed;			// x轴移动速度
	private int xSpeedRange;		// x轴速度随机范围
	private double xAcceleration;	// x轴加速度
	private double xRate;			// x转换角度比例
	private int angle;				// 角度
	private int angleRange;			// 角度随机范围
	
	private static String[] PARAM_NAMES = {
		"峰值", "峰值变化速度", "x轴速度", "x轴速度随机范围", "x轴加速度", "x转换比例", "角度", "角度随机范围"
	};
	private static String[] PARAM_DESCS = {
		"峰值(像素)", 
		"峰值变化速度(像素/TICK)",
		"x轴速度(像素/TICK)",
		"x轴速度随机范围(像素/TICK)", 
		"x轴加速度(像素/TICK2)",
		"x转换比例(角度/像素)",
		"角度(0-360)",
		"角度随机范围(0-180)"
	};
	
	/** 取得轨迹名称 */
	public String getTypeName() {
		return "正弦曲线";
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
			return peak;
		case 1:
			return peakSpeed;
		case 2:
			return xSpeed;
		case 3:
			return xSpeedRange;
		case 4:
			return xAcceleration;
		case 5:
			return xRate;
		case 6:
			return angle;
		case 7:
			return angleRange;
		}
		return 0.0;
	}
	
	/** 设置某个参数的值 */
	public void setParam(int index, double value) {
		switch (index) {
		case 0:
			peak = value;
			break;
		case 1:
			peakSpeed = value;
			break;
		case 2:
			xSpeed = value;
			break;
		case 3:
			xSpeedRange = (int)value;
			break;
		case 4:
			xAcceleration = value;
			break;
		case 5:
			xRate = value;
			break;
		case 6:
			angle = (int)value;
			break;
		case 7:
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
		int initAngle = angle;
		if (angleRange > 0) {
			initAngle += rand.nextInt(angle * 2) - angle;
		}
		double currentAngle = (initAngle % 360) * Math.PI * 2 / 360.0;
		double currentPeak = peak;
		double currentXSpeed = xSpeed;
		if (xSpeedRange > 0) {
			currentXSpeed += rand.nextInt(xSpeedRange * 2) - xSpeedRange;
		}
		double curx = 0;

		int[][] ret = new int[liveTime][2];
		for (int i = 0; i < liveTime; i++) {
			// 根据curx，currentPeak计算cury
			double cury = Math.sin(curx * xRate * Math.PI * 2 / 360.0) * currentPeak;

			// 根据curx和cury计算按角度翻转后的数值
			double d = Math.sqrt(curx * curx + cury * cury);
			double angle = Math.atan(cury / curx);
			angle += currentAngle;
			double nx = d * Math.cos(angle);
			double ny = d * Math.sin(angle);
			
			ret[i][0] = (int)(x + nx);
			ret[i][1] = (int)(y - ny);
			
			// 调整peak和x
			currentPeak += peakSpeed;
			curx += currentXSpeed;
			currentXSpeed += xAcceleration;
		}
		return ret;
	}
	
	/** 复制自身 */
	public PipParticlePath dup() {
		SinusoidPath ret = new SinusoidPath();
		ret.peak = peak;
		ret.peakSpeed = peakSpeed;
		ret.xSpeed = xSpeed;
		ret.xSpeedRange = xSpeedRange;
		ret.xAcceleration = xAcceleration;
		ret.xRate = xRate;
		ret.angle = angle;
		ret.angleRange = angleRange;
		return ret;
	}
}
