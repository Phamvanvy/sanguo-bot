package com.pipimage.image;

import java.util.*;
import java.io.*;

import com.pip.util.Point;

import com.pipimage.utils.ImageUtil;
import com.pipimage.utils.Utils;

/**
 * 粒子效果文件中的一个特效。
 */
public class PipParticleEffect {
	public String title;
	/**
	 * 实际绘制时截取效果中的起始时间，-1表示不限制。
	 */
	public int startTick = -1;
	/**
	 * 实际绘制时截取效果中的结束时间（不包含），-1表示不限制。
	 */
	public int stopTick = -1;
	public List<PipParticleSet> particleSets = new ArrayList<PipParticleSet>();
	
	/**
	 * 根据现有的粒子集合设置，生成粒子序列（按时间先后排序）。
	 */
	public PipParticle[] generateParticles(boolean[] visible) {
		List<PipParticle> list = new ArrayList<PipParticle>();
		Random rand = new Random();
		for (int k = 0; k < particleSets.size(); k++) {
			PipParticleSet pset = particleSets.get(k);
			if (visible != null && !visible[k]) {
				continue;
			}
			for (int i = 0; i < pset.generateTimes; i++) {
				int gcount = pset.generateCount;
				if (pset.generateCountRange > 0) {
					gcount += rand.nextInt(pset.generateCountRange * 2) - pset.generateCount; 
				}
				for (int j = 0; j < gcount; j++) {
					PipParticle pp = new PipParticle();
					pp.startTime = pset.startTime + i * pset.generateInterval;
					pp.particleID = pset.particleID;
					int xx = pset.x;
					if (pset.xrange > 0) {
						xx += rand.nextInt(pset.xrange * 2) - pset.xrange;
					}
					int yy = pset.y;
					if (pset.yrange > 0) {
						yy += rand.nextInt(pset.yrange * 2) - pset.yrange;
					}
					int liveTime = pset.liveTime;
					if (pset.liveTimeRange > 0) {
						liveTime += rand.nextInt(pset.liveTimeRange * 2) - pset.liveTimeRange;
					}
					pp.path = pset.path.makePath(xx, yy, liveTime, rand);
					list.add(pp);
				}
			}
		}
		PipParticle[] ret = new PipParticle[list.size()];
		list.toArray(ret);
		Arrays.sort(ret);
		return ret;
	}
}
