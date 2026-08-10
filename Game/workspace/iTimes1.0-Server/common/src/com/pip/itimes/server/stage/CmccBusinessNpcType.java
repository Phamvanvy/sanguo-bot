package com.pip.itimes.server.stage;

public class CmccBusinessNpcType extends TaskNpcType {
	public int areaid;//地区（吉林：1）
	public int Businesstype;//业务类型
	public String Businesscode;//业务短信接口
	public String Businessmsg;//定制短信内容
	public CmccBusinessNpcType(int id, String name, int type) {
        super(id, name, type);
    }
	public int getAreaid() {
		return areaid;
	}
	public void setAreaid(int areaid) {
		this.areaid = areaid;
	}
	public int getBusinesstype() {
		return Businesstype;
	}
	public void setBusinesstype(int businesstype) {
		Businesstype = businesstype;
	}
	public String getBusinesscode() {
		return Businesscode;
	}
	public void setBusinesscode(String businesscode) {
		Businesscode = businesscode;
	}
	public String getBusinessmsg() {
		return Businessmsg;
	}
	public void setBusinessmsg(String businessmsg) {
		Businessmsg = businessmsg;
	}
	
}
