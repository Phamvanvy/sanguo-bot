package com.pip.itimes.server.stage;

/**
 * ¹ã¶«¿áÏÄ½ğÔ¿³×
 * @author yufengchen
 *
 */
public class SummerKeyEffect extends Effect {

	private int group1;
    private int group2;
    
    public SummerKeyEffect(int group1,int group2) {
        this.group1 = group1;
        this.group2 = group2;
    }
	@Override
	public byte getType() {
		// TODO Auto-generated method stub
		return 62;
	}
	
	public int getGroup1(){
        return group1;
    }

    public int getGroup2(){
        return group2;
    }

}
