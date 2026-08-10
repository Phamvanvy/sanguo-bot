package com.pip.itimes.server.stage;

/**
 * 家园仓库的包格扩展效果
 * @author yufengchen
 *
 */
public class AddGridEffectHouse extends Effect {

	 private int value;			// 扩展的格子数
	 
	 public AddGridEffectHouse(int value){
		 this.value = value;
	 }
	public int getValue() {
		return value;
	}
	@Override
	public byte getType() {
		// TODO Auto-generated method stub
		return 57;
	}

}
