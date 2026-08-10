package peony.game;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ch.javasoft.util.intcoll.IntHashMap;


public class CoolDownList {

	protected IntHashMap<CoolDown> coolDowns = new IntHashMap<CoolDown>();
	protected int commonCDTime;
	
	public void setCommonCD(int t){
		commonCDTime = Time.currTime + t;
	}
	
	/**
	 * 返回冷却时间到了的CD
	 * @return
	 */
	public List<CoolDown> update(){
		List<CoolDown> l = new LinkedList<CoolDown>();
		Iterator<CoolDown> ite = coolDowns.values().iterator();
		while(ite.hasNext()){
			CoolDown cd = ite.next();
			if(cd.endTime<=Time.currTime){
				ite.remove();
				l.add(cd);
			}
		}
		return l;
	}
	
	//设置CoolDown时间，因为可能覆盖前面相同的技能组，所以返回的CoolDown的时间未必是这次设置的时间
	public CoolDown setCoolDown(int id,int startTime,int time){
		CoolDown cd = coolDowns.get(id);
		if(cd!=null){
			cd.endTime = time;
		}else{
			cd = new CoolDown(id,startTime,time);
			coolDowns.put(id, cd);
		}
		return cd;
	}
	
	public CoolDown removeCoolDown(int id){
		return coolDowns.remove(id);
	}
	
	public boolean contains(int id){
		return coolDowns.containsKey(id);
	}
	
	public boolean atCoolDown(int id){
		//commoncd
		if(Time.currTime<commonCDTime)
			return true;
		return contains(id);
	}
	
	public CoolDown getCoolDown(int id){
		return coolDowns.get(id);
	}
	
	@Override
	public CoolDownList clone(){
		CoolDownList ret = new CoolDownList();
		ret.coolDowns = coolDowns.clone();
		return ret;
	}
	
	public byte[] toClientBytes(){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.writeShort(coolDowns.size());
			for(CoolDown cd:coolDowns.values()){
				dos.write(cd.id);
				dos.writeInt(cd.startTime);
				dos.writeInt(cd.endTime);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return baos.toByteArray();		
	}
	
	public byte[] toDBBytes(){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.write(1);//version
			dos.writeShort(coolDowns.size());
			for(CoolDown cd:coolDowns.values()){
				dos.write(cd.id);
				dos.writeLong(Time.currentTimeMillis(cd.endTime));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return baos.toByteArray();
	}
	
	public static CoolDownList fromDBBytes(byte[] bytes) {
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(
				bytes));
		CoolDownList coolDowns = new CoolDownList();
		try {
			dis.read();// version
			int len = dis.readShort();
			for (int i = 0; i < len; i++) {
				int id = dis.read();
				long endTime = dis.readLong();
//				long time = dis.readLong();
				int st = Time.elapseTime(endTime);
//				int t = Time.elapseTime(time);
				if (st >= 500) { // 500毫秒以内的都不加上了，没有意义
					coolDowns.setCoolDown(id, 0, st);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return coolDowns;
	}
}
