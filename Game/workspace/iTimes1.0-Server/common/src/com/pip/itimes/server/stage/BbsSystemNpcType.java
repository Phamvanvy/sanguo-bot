package com.pip.itimes.server.stage;

public class BbsSystemNpcType extends TaskNpcType {
	private int bbsSystemId ;
    
	public int getBbsSystemId() {
		return bbsSystemId;
	}

	public void setBbsSystemId(int bbsSystemId) {
		this.bbsSystemId = bbsSystemId;
	}

	public BbsSystemNpcType(int id, String name, int type) {
        super(id, name, type);
    }
	
	
}
