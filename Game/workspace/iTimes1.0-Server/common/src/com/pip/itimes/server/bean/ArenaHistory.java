package com.pip.itimes.server.bean;

import java.util.Date;

public class ArenaHistory {

    private int id;
    private Date lastrecordtime;

    public ArenaHistory() {
    }

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getLastrecordtime() {
		return lastrecordtime;
	}

	public void setLastrecordtime(Date lastrecordtime) {
		this.lastrecordtime = lastrecordtime;
	}

}
