package com.pip.itimes.server.bean;

import java.util.Date;

public class ArenaTeamTotalWorldWar {

    private int id;
    private int Type;
    private String servername;
    private String serverid;
    private int arenaid;
    private String arenaname;
    private Date updatetime;
    private int ownerid;
    private String ownername;
    private int arenalevel;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getType() {
		return Type;
	}
	public void setType(int type) {
		Type = type;
	}
	public String getServername() {
		return servername;
	}
	public void setServername(String servername) {
		this.servername = servername;
	}
	
	public String getServerid() {
		return serverid;
	}
	public void setServerid(String serverid) {
		this.serverid = serverid;
	}
	public int getArenaid() {
		return arenaid;
	}
	public void setArenaid(int arenaid) {
		this.arenaid = arenaid;
	}
	public String getArenaname() {
		return arenaname;
	}
	public void setArenaname(String arenaname) {
		this.arenaname = arenaname;
	}
	public Date getUpdatetime() {
		return updatetime;
	}
	public void setUpdatetime(Date updatetime) {
		this.updatetime = updatetime;
	}
	public int getOwnerid() {
		return ownerid;
	}
	public void setOwnerid(int ownerid) {
		this.ownerid = ownerid;
	}
	
	public String getOwnername() {
		return ownername;
	}
	public void setOwnername(String ownername) {
		this.ownername = ownername;
	}
	public int getArenalevel() {
		return arenalevel;
	}
	public void setArenalevel(int arenalevel) {
		this.arenalevel = arenalevel;
	}
    
}
