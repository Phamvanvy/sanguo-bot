package com.pip.itimes.server.bean;

import java.util.Date;

public class House {

    public static final int RULE_PRIVATE = 0;
    public static final int RULE_FRIENDS = 1;
    public static final int RULE_TEAM = 2;
    public static final int RULE_GUILD = 3;
    public static final int RULE_FREE = 4;
    public static final String[] RULE_STRING = {"拒绝参观","好友参观","队友参观","公会参观","随意参观"};
    //            \uF06C	随意参观：玩家允许所有其他玩家进入自己的房屋。
//            \uF06C	好友参观：玩家允许自己的好友列表中的玩家好友进入游戏。
//            \uF06C	队友参观：玩家允许同一队内的成员自由进入自己的房屋。
//            \uF06C	公会参观：玩家允许同一公会的成员自由进入自己的房屋。
//            \uF06C	拒绝参观：玩家可以拒绝除夫妻成员以外的其他玩家。

    private int id;
    private int playerId;
    private String playerName;
    private int level;
    private int style;
    private int rule;
    private short areaId;
    private int gridSize;
    private int addGridSize;
    private byte[] items;
    private byte[] parts;
    private Date createTime;
    private Date lastTime;
    private String title;
    private int waiterId;
    private int visitedTimes;
    private int usediMoney;
    private int leaveMessageTimes;
    private Date canUseWaiterTime;
    //mengjie add
    private int autoBuyWaiter;

    public House() {
        super();
    }

    public int getStyle() {
        return style;
    }

    public int getRule() {
        return rule;
    }

    public int getPlayerId() {
        return playerId;
    }

    public int getLevel() {
        return level;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setStyle(int style) {
        this.style = style;
    }

    public void setRule(int rule) {
        this.rule = rule;
    }

    public void setPlayerId(int ownerId) {
        this.playerId = ownerId;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public void setAreaId(short areaId) {
        this.areaId = areaId;
    }

    public void setItems(byte[] items) {
        this.items = items;
    }

    public void setGridSize(int gridSize) {
        this.gridSize = gridSize;
    }

    public void setParts(byte[] parts) {
        this.parts = parts;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public void setLastTime(Date lastTime) {
        this.lastTime = lastTime;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setWaiterId(int waiterId) {
        this.waiterId = waiterId;
    }


    public void setVisitedTimes(int visitedTimes) {
        this.visitedTimes = visitedTimes;
    }

    public void setUsediMoney(int usediMoney) {
        this.usediMoney = usediMoney;
    }

    public void setLeaveMessageTimes(int leaveMessageTimes) {
        this.leaveMessageTimes = leaveMessageTimes;
    }

    public void setCanUseWaiterTime(Date canUseWaiterTime) {
        this.canUseWaiterTime = canUseWaiterTime;
    }

    public int getId() {
        return id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public short getAreaId() {
        return areaId;
    }

    public byte[] getItems() {
        return items;
    }

    public int getGridSize() {
        return gridSize;
    }

    public byte[] getParts() {
        return parts;
    }

    public String getPlayerName() {
        return playerName;
    }

    public Date getLastTime() {
        return lastTime;
    }

    public String getTitle() {
        return title;
    }

    public int getWaiterId() {
        return waiterId;
    }


    public int getVisitedTimes() {
        return visitedTimes;
    }

    public int getUsediMoney() {
        return usediMoney;
    }

    public int getLeaveMessageTimes() {
        return leaveMessageTimes;
    }

    public Date getCanUseWaiterTime() {
        return canUseWaiterTime;
    }

	public int getAutoBuyWaiter() {
		return autoBuyWaiter;
	}

	public void setAutoBuyWaiter(int autoBuyWaiter) {
		this.autoBuyWaiter = autoBuyWaiter;
	}

	public int getAddGridSize() {				//扩展格数
		return addGridSize;
	}

	public void setAddGridSize(int addGridSize) {
		this.addGridSize = addGridSize;
	}
	

}
