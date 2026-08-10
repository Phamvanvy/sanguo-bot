package com.pip.itimes.server.bean;

import java.util.Date;


public class VoteContent implements java.io.Serializable {
  
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getVotersid() {
		return votersid;
	}
	public void setVotersid(int votersid) {
		this.votersid = votersid;
	}
	public Date getCreatetime() {
		return createtime;
	}
	public void setCreatetime(Date createtime) {
		this.createtime = createtime;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public boolean isValid() {
		return valid;
	}
	public void setValid(boolean valid) {
		this.valid = valid;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	private int id;
    private int votersid;
    private Date createtime;
    private int type;
    private boolean valid;
    private String content;
}

