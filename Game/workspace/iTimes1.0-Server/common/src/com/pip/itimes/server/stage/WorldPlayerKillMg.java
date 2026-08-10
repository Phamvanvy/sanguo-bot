package com.pip.itimes.server.stage;


import java.util.Date;

public class WorldPlayerKillMg {
	public WorldPlayerKillMg() throws Exception{
        this.MgId = 0;
        this.killtime = 0;
    }
	
	protected int MgId;//
    protected long killtime;//…±π÷ ±º‰
	public int getMgId() {
		return MgId;
	}
	public void setMgId(int mgId) {
		MgId = mgId;
	}
	public long getKilltime() {
		return killtime;
	}
	public void setKilltime(long killtime) {
		this.killtime = killtime;
	}
    
}
