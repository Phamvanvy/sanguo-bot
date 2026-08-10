package com.pip.itimes.server.world;


/**
 * @author Jeffrey
 * @version 1.0
 */
public class Version {

    public static final int STATUS_CURRENT = 0;
    public static final int STATUS_OBSOLETE = 1;
    public static final int STATUS_CANCELED = 2;

    private String id;
    private int status;
    private String[] charge;
    private String feeplan;
    private boolean canReg;
    private int maxLevel;
    private String description;
    private String message;
    private int dataVersion;

    public int getDataVersion() {
		return dataVersion;
	}

	public void setDataVersion(int dataVersion) {
		this.dataVersion = dataVersion;
	}

	public Version() {
    }

    public int getStatus() {
        return status;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public String getId() {
        return id;
    }

    public String getFeeplan() {
        return feeplan;
    }

    public void setCanReg(boolean canReg) {
        this.canReg = canReg;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setFeeplan(String feeplan) {
        this.feeplan = feeplan;
    }


    public void setDescription(String description) {
        this.description = description;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setCharge(String[] charge) {
        this.charge = charge;
    }

    public boolean isCanReg() {
        return canReg;
    }

    public String getDescription() {
        return description;
    }

    public String getMessage() {
        return message;
    }

    public String[] getCharge() {
        return charge;
    }
}
