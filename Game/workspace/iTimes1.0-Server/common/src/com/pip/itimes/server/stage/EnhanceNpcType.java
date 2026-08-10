package com.pip.itimes.server.stage;

public class EnhanceNpcType extends TaskNpcType {
    public EnhanceNpcType(int id, String name, int type) {
        super(id, name, type);
    }
    /**
     * 精炼类型，精炼为1，精炼替换为2
     */
    public int classtype;
    
	public int getClasstype() {
		return classtype;
	}
	public void setClasstype(int classtype) {
		this.classtype = classtype;
	}
}
