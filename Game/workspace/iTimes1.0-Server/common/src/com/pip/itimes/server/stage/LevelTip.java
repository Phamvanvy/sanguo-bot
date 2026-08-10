package com.pip.itimes.server.stage;

public class LevelTip {

    private String message;
    private String shout;
    //mengjie add
    private int[] id;
    private int[] count;
    private String[] mailmsg;
    
    private int worldMsg;
    public int getWorldMsg() {
		return worldMsg;
	}

	public void setWorldMsg(int worldMsg) {
		this.worldMsg = worldMsg;
	}

	public LevelTip(String message,String shout,int[] id,int[] count,String[] mailmsg , int worldmsg) {
        this.message = message;
        this.shout = shout;
        //mengjie add
        this.id = id;
        this.count = count;
        this.mailmsg = mailmsg;
        this.worldMsg = worldmsg;
    }

    public String getShout() {
        return shout;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setShout(String shout) {
        this.shout = shout;
    }

    public String getMessage() {
        return message;
    }

	public int[] getId() {
		return id;
	}

	public void setId(int[] id) {
		this.id = id;
	}

	public int[] getCount() {
		return count;
	}

	public void setCount(int[] count) {
		this.count = count;
	}

	public String[] getMailmsg() {
		return mailmsg;
	}

	public void setMailmsg(String[] mailmsg) {
		this.mailmsg = mailmsg;
	}
    
}
