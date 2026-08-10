package com.pip.gui;

import com.pip.ui.VM;
import com.pip.ui.VMGame;

public class GScrollBar extends GWidget implements IGCycle{
	public static final int GSB_ALIGN_NONE   = 0;
	public static final int GSB_ALIGN_CENTER = 1;
	public static final int GSB_ALIGN_RIGHT  = 2;
	
	int scrollPos;
	int tick;
	boolean needCycle;
	int maxScrollDis;  //×î´ó¹ö¶¯¾àÀë
	
	int align;
	
	public GScrollBar(VMGame _vmGame, int self, int[] vmData, String name) {
		super(_vmGame, self, vmData, name);
	}
	
	public GWidget getClone(VMGame _vmGame) {
		GScrollBar gScrollBar = new GScrollBar(_vmGame, 0, getVMDataCopy(), name);
		setCloneData(gScrollBar);
		gScrollBar.scrollPos = this.scrollPos;
		gScrollBar.tick = this.tick;
		gScrollBar.needCycle = this.needCycle;
		gScrollBar.align = this.align;
		
		return gScrollBar;
	}
	
	public void reset() {
		scrollPos = 0;
		tick = 0;
		maxScrollDis = 0;
	}
	
	public void setAlign(int align) {
		this.align = align;
	}
	
	public int getMaxScrollDis(){
		return maxScrollDis;
	}
	
	public void setMaxScrollDis(int maxScrollDis) {
		this.maxScrollDis = maxScrollDis;
	}
	
	public int getScrollPos() {
		return scrollPos;
	}
	
	public void setScrollPos(int scrollPos) {
		this.scrollPos = scrollPos;
	}
	
	public int getTick() {
		return tick;
	}

	public void cycle() {
		tick ++;
	}

	public boolean needExecuteCycle() {
		return needCycle;
	}

	public void setNeedExecuteCycle(boolean needExecuteCycle) {
		this.needCycle = needExecuteCycle;
	}

	public void setSpeed(int speed) {
		
	}
	
	
}
