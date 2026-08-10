package com.pip.gui;

public interface IGCycle {
	
	public boolean needExecuteCycle();
	
	public void setNeedExecuteCycle(boolean needExecuteCycle);
	
	public void cycle();	
	
	public void setSpeed(int speed);
	
}
