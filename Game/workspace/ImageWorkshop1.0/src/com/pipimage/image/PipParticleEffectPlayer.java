package com.pipimage.image;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.GC;

public class PipParticleEffectPlayer {
	private PipAnimateSet animates;
	private PipParticle[] particles;
	private int currentTime;
	private int startTick;
	private int stopTick;
	private int particlePointer;
	private List<PipParticle> activeParticles;
	
	public PipParticleEffectPlayer(PipAnimateSet animates, PipParticle[] particles, int st, int et) {
		this.animates = animates;
		this.particles = particles;
		this.currentTime = -1;
		this.startTick = st;
		if (this.startTick != -1) {
			this.currentTime = this.startTick - 1;
		}
		this.stopTick = et;
		particlePointer = 0;
		activeParticles = new ArrayList<PipParticle>();
	}
	
	public void step() {
		this.currentTime++;
		while (particlePointer < particles.length && particles[particlePointer].startTime <= this.currentTime) {
			activeParticles.add(particles[particlePointer]);
			particlePointer++;
		}
		for (int i = 0; i < activeParticles.size(); i++) {
			int time = currentTime - activeParticles.get(i).startTime;
			if (time >= activeParticles.get(i).path.length) {
				activeParticles.remove(i);
				i--;
			}
		}
		if (this.stopTick != -1 && this.currentTime >= this.stopTick) {
			activeParticles.clear();
			particlePointer = particles.length;
		}
	}
	
	public boolean isOver() {
		return particlePointer >= particles.length && activeParticles.size() == 0;
	}
	
	public void draw(GC g, int x, int y, double ratio, ImageDrawCache cache) {
		for (PipParticle particle : activeParticles) {
			int time = currentTime - particle.startTime;
			if (particle.path[time][0] == -1000) {
				continue;
			}
			int drawx = (int)(particle.path[time][0] * ratio + x);
	    	int drawy = (int)(particle.path[time][1] * ratio + y);
	    	PipAnimate ani = animates.getAnimate(particle.particleID);
	    	int animateFrame = ani.getFrameAtTime(time);
	    	ani.drawFrame(g, animateFrame, drawx, drawy, ratio);
		}
	}
}
