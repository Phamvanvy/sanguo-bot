package com.pipimage.image;

/**
 * 定义一个或一组通过固定规则随机生成的粒子。
 * @author lighthu
 */
public class PipParticleSet {
	/** 粒子组标题 */
	public String title;
	/** 粒子组开始生成时间 */
	public int startTime;
	/** 每循环生成粒子数量 */
	public int generateCount;
	/** 每循环生成粒子数量随机范围 */
	public int generateCountRange;
	/** 两次生成粒子之间的时间间隔 */
	public int generateInterval;
	/** 总共生成次数 */
	public int generateTimes;
	/** 生成粒子的x位置 */
	public int x;
	/** 生成粒子的y位置 */
	public int y;
	/** 生成粒子的x位置随机范围 */
	public int xrange;
	/** 生成粒子的y位置随机范围 */
	public int yrange;
	/** 生成粒子的动画ID */
	public int particleID;
	/** 生成粒子的生存周期 */
	public int liveTime;
	/** 生成粒子的生存周期随机范围 */
	public int liveTimeRange;
	/** 粒子运动轨迹算法 */
	public PipParticlePath path;
	
	public void update(PipParticleSet other) {
		this.title = other.title;
		this.startTime = other.startTime;
		this.generateCount = other.generateCount;
		this.generateCountRange = other.generateCountRange;
		this.generateInterval = other.generateInterval;
		this.generateTimes = other.generateTimes;
		this.x = other.x;
		this.y = other.y;
		this.xrange = other.xrange;
		this.yrange = other.yrange;
		this.particleID = other.particleID;
		this.liveTime = other.liveTime;
		this.liveTimeRange = other.liveTimeRange;
		this.path = other.path.dup();
	}
}
