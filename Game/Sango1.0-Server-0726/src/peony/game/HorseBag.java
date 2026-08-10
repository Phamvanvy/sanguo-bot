package peony.game;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import peony.game.changed.HorseBagChangedItem;
import peony.game.itemeffect.GetHorseExpEffect;
import peony.net.Packet;
import peony.service.fame.Fame;
import peony.service.fame.FameService;

public class HorseBag {
	
	private static final Logger log = Logger.getLogger(HorseBag.class);

	public int maxSize;
	public List<Horse> horses;
	public Unit owner;

	public HorseBag(Unit owner) {
		this(owner, 5);
	}

	public HorseBag(Unit owner, int maxSize) {
		this.owner = owner;
		this.maxSize = maxSize;
		horses = new ArrayList<Horse>(maxSize);
	}
	
	public List<Horse> getAgentHorses(){
		List<Horse> ret = new ArrayList<Horse>(3);
		for(Horse h:horses){
			if(h.agentHorse==1){
				ret.add(h);
			}
		}
		return ret;
	}

	@Override
	public HorseBag clone(){
		HorseBag bag = new HorseBag(owner,maxSize);
		for(Horse h:horses){
			bag.horses.add(h);
		}
		return bag;
	}
	
	public boolean isFull(){
		return horses.size()>=maxSize;
	}
	
	public void addHorse(Horse h){
		horses.add(h);
		if(owner.changed!=null){
			owner.changed.addChangedItem(new HorseBagChangedItem(h,1));
		}
	}
	
	public boolean removeHorse(int instanceId){
		Iterator<Horse> ite = horses.iterator();
		while(ite.hasNext()){
			Horse h = ite.next();
			if(h.instanceId == instanceId){
				ite.remove();
				if(owner.changed!=null){
					owner.changed.addChangedItem(new HorseBagChangedItem(h,0));
				}
				return true;
			}
		}
		return false;
	}
	
	public Horse getHorse(int instanceId) {
		for (Horse h : horses) {
			if (h.instanceId == instanceId)
				return h;
		}
		return null;
	}

	public void throwHorse(int instanceId, int serial) {
		Horse horse = null;
		Iterator<Horse> ite = horses.iterator();
		while (ite.hasNext()) {
			Horse h = ite.next();
			if (h.instanceId == instanceId) {
				horse = h;
				ite.remove();
				if(owner.changed!=null){
					owner.changed.addChangedItem(new HorseBagChangedItem(h,0));
				}
				break;
			}
		}
		if (horse != null) {
			if (owner.horse == horse) {
				if(owner instanceof Player){
					((Player)owner).horseUnride(-1);
				}else{
					owner.horse = null;
					horse.unRide(owner);
				}
			}
			if (owner.type == GameObject.TYPE_PLAYER) {
				// 记录日志
				LogUtil.logThrowHorse((Player)owner, horse, "DEL");
				
				Packet pt = new Packet(OpCode.HORSE_THROW_SERVER);
				pt.putInt(serial);
				((Player)owner).send(pt);
			}
		}
	}

	public byte[] toClientBytes() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.write(maxSize);
			dos.write(horses.size());
			for (Horse h : horses) {
				dos.write(h.toClientBytes(owner));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return baos.toByteArray();
	}
	
	public byte[] toDBBytes(){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.write(1);//version
			dos.write(maxSize);
			dos.write(horses.size());
			for (Horse h : horses) {
				dos.write(h.toDBBytes());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return baos.toByteArray();
		
	}
	
	public static HorseBag fromDBBytes(byte[] bytes,Player owner){
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		DataInputStream dis = new DataInputStream(bais);
		try{
			dis.read();
			int maxSize = dis.read();
			HorseBag bag = new HorseBag(owner,maxSize);
			int size = dis.read();
			for(int i=0;i<size;i++){
				Horse h = Horse.fromDBBytes(dis);
				Horse cHorse = bag.getHorse(h.instanceId);
				if (cHorse != null) { //曾经出现过一个bug，导致骑马的时候打宝石会复制马20100624
					for(int j=0;j<cHorse.equs.equs.length;j++){
						if(cHorse.equs.equs[j]==null&&h.equs.equs[j]!=null){
							cHorse.equs.equs[j] = h.equs.equs[j];
						}
					}
					LogUtil.logThrowHorse(owner, h, "BUG20100624");
				} else {
					h.refreshProperties(false, owner);
					bag.horses.add(h);
				}
			}
			return bag;
		}catch(Exception e){
			e.printStackTrace();
			return new HorseBag(owner);
		}
	}
	public static HorseBag fromDBBytes(byte[] bytes,Fame fame){
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		DataInputStream dis = new DataInputStream(bais);
		Player owner = FameService.statuePlayer.get(fame.playerId);
		try{
			dis.read();
			int maxSize = dis.read();
			HorseBag bag = new HorseBag(owner,maxSize);
			int size = dis.read();
			for(int i=0;i<size;i++){
				Horse h = Horse.fromDBBytes(dis);
				Horse cHorse = bag.getHorse(h.instanceId);
				if (cHorse != null) { //曾经出现过一个bug，导致骑马的时候打宝石会复制马20100624
					for(int j=0;j<cHorse.equs.equs.length;j++){
						if(cHorse.equs.equs[j]==null&&h.equs.equs[j]!=null){
							cHorse.equs.equs[j] = h.equs.equs[j];
						}
					}
					LogUtil.logThrowHorse(owner, h, "BUG20100624");
				} else {
					h.refreshProperties(false, owner);
					bag.horses.add(h);
				}
			}
			return bag;
		}catch(Exception e){
			e.printStackTrace();
			return new HorseBag(owner);
		}
	}
	
	/**
	 * 扩展坐骑栏所需金钱
	 * @return
	 */
	public int getExtendHorsebagMoney(){
		switch(maxSize){
			case 5:
				return 200000;
			case 6:
				return 400000;
			case 7:
				return 800000;
			case 8:
				return 1600000;
			case 9:
				return 3200000;
		}
		return -1;
	}
}
