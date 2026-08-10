package com.pip.itimes.server.world;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class PhoneType {

    public static final int PKG = 1;
    public static final int PKGEM = 2;
    public static final int PKGS = 3;

    private String id;
    private int interval;
    private int filtType;

    public PhoneType() {
    }

    public int getInterval() {
        return interval;
    }

    public String getId() {
        return id;
    }

    public void setFiltType(int filtType) {
        this.filtType = filtType;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getFiltType() {
        return filtType;
    }
}
