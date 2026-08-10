package com.pip.itimes.server.bean;

import java.util.Date;

public class ArenaTeam {

    private int id;
    private int Type;
    private String arenaname;
    private Date createtime;
    private int owner;
    private String slogan;
    private int arenalevel;
    private Date lastrepairtime;
    private int memebercount;
    private boolean valid;
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
	public String getArenaname() {
		return arenaname;
	}
	public void setArenaname(String arenaname) {
		this.arenaname = arenaname;
	}
	public Date getCreatetime() {
		return createtime;
	}
	public void setCreatetime(Date createtime) {
		this.createtime = createtime;
	}
	public int getOwner() {
		return owner;
	}
	public void setOwner(int owner) {
		this.owner = owner;
	}
	public String getSlogan() {
		return slogan;
	}
	public void setSlogan(String slogan) {
		this.slogan = slogan;
	}
	public int getArenalevel() {
		return arenalevel;
	}
	public void setArenalevel(int arenalevel) {
		this.arenalevel = arenalevel;
	}
	public Date getLastrepairtime() {
		return lastrepairtime;
	}
	public void setLastrepairtime(Date lastrepairtime) {
		this.lastrepairtime = lastrepairtime;
	}
	public int getMemebercount() {
		return memebercount;
	}
	public void setMemebercount(int memebercount) {
		this.memebercount = memebercount;
	}
	public boolean isValid() {
		return valid;
	}
	public void setValid(boolean valid) {
		this.valid = valid;
	}
    

}
