package com.pip.itimes.server.stage;

public class RecommendedNpcType extends TaskNpcType {
	public int areaId;
	public String url_name;
	public RecommendedNpcType(int id, String name, int type) {
        super(id, name, type);
    }
	public int getAreaId() {
		return areaId;
	}
	public void setAreaId(int areaId) {
		this.areaId = areaId;
	}
	public String getUrl_name() {
		return url_name;
	}
	public void setUrl_name(String url_name) {
		this.url_name = url_name;
	}
	
}
