package com.pip.itimes.server.stage;

public class TongValueEffect extends Effect{
	
	private int Contribution;
	private int TongCredit;
	
	public TongValueEffect(int Contribution, int TongCredit){
		this.Contribution = Contribution;
		this.TongCredit = TongCredit;
	}
	
	public byte getType() {
		return 83;
	}
	
	public int getContribution(){
		return Contribution;
	}
	
	public int getTongCredit(){
		return TongCredit;
	}
}
