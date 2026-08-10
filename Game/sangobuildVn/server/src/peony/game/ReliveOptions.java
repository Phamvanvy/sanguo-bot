package peony.game;

import java.util.LinkedList;
import java.util.List;

import peony.net.Packet;

public class ReliveOptions {
	
	/**
	 * 第一个必须是释放
	 */
	protected List<ReliveOption> options = new LinkedList<ReliveOption>();
	
	public int reliveTime;//释放的时间，超过这时间无论用户是否选择复活，都将释放
	
	public ReliveOptions(int reliveTime){
		this.reliveTime  = reliveTime;
	}
	
	public void addOption(ReliveOption option,boolean addReliveTime){
		if(addReliveTime)
			reliveTime += 60*1000;
		if(options.size()>0){
			for(ReliveOption o:options){
				if(o.merge(option)){ 
					return;
				}
			}
		}
		options.add(option);
		
	}
	
	public ReliveOption getFirstOption(){
		if(options.size()>0)
			return options.get(0);
		return null;
	}
	
	public ReliveOption update(){
		if(Time.currTime>=reliveTime){
			return options.get(0);
		}
		return null;
	}
	
	public ReliveOption get(int id){
		for(ReliveOption option:options){
			if(option.id==id)
				return option;
		}
		return null;
	}
	
	public Packet getRelivePacket(){
		Packet pt = new Packet(OpCode.DIE_SERVER);
		pt.putInt(reliveTime);
		pt.put(options.size());
		for(ReliveOption option:options){
			pt.putInt(option.id);
			pt.putString(option.msg);
		}
		return pt;
	}
}
