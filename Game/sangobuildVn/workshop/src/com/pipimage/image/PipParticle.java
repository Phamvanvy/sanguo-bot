package com.pipimage.image;

/**
 * 定义一个粒子。
 * @author lighthu
 */
public class PipParticle implements Comparable<PipParticle> {
	/** 粒子出现的时间 */
	public int startTime;
	/** 粒子对应的粒子动画ID */
	public int particleID;
	/** 粒子的轨迹，第一个点是出现点，等path中每个点都走完，粒子消失 */
	public int[][] path;
	
	public int compareTo(PipParticle o) {
		if (startTime < o.startTime) {
			return -1;
		} else if (startTime == o.startTime) {
			return 0;
		} else {
			return 1;
		}
	}
}
