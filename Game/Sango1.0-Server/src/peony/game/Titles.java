package peony.game;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import peony.game.buff.Buff;
import peony.game.changed.ChangedItem;

public class Titles {
	
	private static final Logger log = Logger.getLogger(Titles.class);
	
	public Player owner;
	protected Title currentTitle; //佩戴的称号
	protected Title currentEquipTitle; //装备的称号
	public Map<Integer,Title> titles = new TreeMap<Integer,Title>();
	protected long lastSalaryTime;
	public Map<Integer, Integer> validTimes = new HashMap<Integer, Integer>(); //记录所有称号的过期时间
	
	public Titles(Player owner){
		this.owner = owner;
	}
	
	@Override
	public Titles clone(){
		Titles t = new Titles(owner);
		t.currentTitle = currentTitle;
		t.currentEquipTitle = currentEquipTitle;
		t.lastSalaryTime = lastSalaryTime;
		for(Title tt:titles.values()){
			titles.put(tt.id, tt);
		}
		return t;
	}
	
	public Title getCurrentTitle(){
		return currentTitle;
	}
	
	public Title getCurrentEquipTitle(){
		return currentEquipTitle;
	}
	
	public String getCurrentTitleString(){
		return currentTitle==null?"":currentTitle.name;
	}
	
	public String getCurrentEquipTitleString(){
		return currentEquipTitle==null?"":currentEquipTitle.name;
	}
	
	public int getCurrentTitleId(){
		return currentTitle==null?-1:currentTitle.id;
	}
	
	public int getCurrentEquipTitleId(){
		return currentEquipTitle==null?-1:currentEquipTitle.id;
	}
	
	public void setLastSalaryTime(long time){
		this.lastSalaryTime = time;
	}
	
	/**
	 * 装备称号
	 * @param titleId
	 * @return
	 */
	public Title changeEquipTitle(int titleId){
		if(currentEquipTitle!=null&&currentEquipTitle.id==titleId)
			throw new IllegalArgumentException();
		Title t = titles.get(titleId);
		if(t==null){
			return t;
		}
		if(currentEquipTitle!=null&&currentEquipTitle.buffId!=-1){ //如果有buff效果，那么移除原来的buff
			owner.buffs.removeBuff(currentEquipTitle.buffId);
		}
		currentEquipTitle = t;
		Buff buff = t.newBuff(owner);
		if(buff!=null){
			owner.buffs.addBuff(buff);
		}
//		owner.moveExtended |= GameObject.MOVEEXT_TITLE;
//		owner.addStringPropertyChangedItem(ChangedItem.TITLE, currentEquipTitle.name, false);
		if(owner!=null){
			if(owner.horse!=null)
				owner.horse.refreshProperties(false, owner);
			owner.refreshProperties(false);
		}
		return currentEquipTitle;
	}
	
	/**
	 * 展示称号
	 * @param titleId
	 * @return
	 */
	public Title changeShowTitle(int titleId){
		if(currentTitle!=null&&currentTitle.id==titleId)
			throw new IllegalArgumentException();
		Title t = titles.get(titleId);
		if(t == null){
			return t;
		}
			currentTitle = t;
			owner.moveExtended |= GameObject.MOVEEXT_TITLE;
			owner.addStringPropertyChangedItem(ChangedItem.TITLE, currentTitle.name, false);
		return currentTitle;
	}
	
	/**
	 * 隐藏或卸下称号
	 * @param isHideTitle  是否隐藏称号
	 * @return
	 */
	public Title removeCurrentTitle(boolean isHideTitle){
		Title t = null;
		if(!isHideTitle){
			t = currentEquipTitle;
			currentEquipTitle = null;
			if(t!=null&&t.buffId!=-1){
				owner.buffs.removeBuff(t.buffId);
			}
//			owner.moveExtended |= GameObject.MOVEEXT_TITLE;
//			owner.addStringPropertyChangedItem(ChangedItem.TITLE, "", false);
			if(owner!=null){
				if(owner.horse!=null)
					owner.horse.refreshProperties(false, owner);
				owner.refreshProperties(false);
			}
		} else {
			t = currentTitle;
			currentTitle = null;
			owner.moveExtended |= GameObject.MOVEEXT_TITLE;
			owner.addStringPropertyChangedItem(ChangedItem.TITLE, "", false);
		}
		return t;
	}
	
	public void addTitle(Title title){
		titles.put(title.id, title);
	}
	
	
	public Title removeTitle(int titleId){
		if(currentEquipTitle!=null&&currentEquipTitle.id==titleId){
			if(currentEquipTitle.buffId!=-1){
				owner.buffs.removeBuff(currentEquipTitle.buffId);
			}
			currentEquipTitle = null;
		}
		return titles.remove(titleId);
	}
	
	public boolean hasTitle(int titleId){
		return titles.containsKey(titleId);
	}
	
	/**
	 * 根据称号ID获取此称号的过期时间
	 * @param titleId
	 * @return
	 */
	public int getVliadTime(int titleId){
		Integer time = validTimes.get(titleId);
		if(time==null)
			return 0;
		return time.intValue();
	}
	
	public void setValidTime(int titleId, int validTime){
		validTimes.put(titleId, validTime);
	}
	
	public byte[] toDBBytes(){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try{
			dos.writeLong(lastSalaryTime);
			dos.writeShort(currentTitle==null?-1:currentTitle.id);
			dos.writeShort(titles.size());
			for(Title t:titles.values()){
				dos.writeShort(t.id);
			}
			dos.writeShort(currentEquipTitle==null?-1:currentEquipTitle.id);
			dos.writeShort(validTimes.size());
			for(int titleId : validTimes.keySet()){
				dos.writeShort(titleId);
				dos.writeInt(validTimes.get(titleId));
			}
		}catch(Exception e){
			
		}
		return baos.toByteArray();
	}
	
	public static Titles fromDBBytes(byte[] bytes,Player owner){
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		DataInputStream dis = new DataInputStream(bais);
		Titles titles = new Titles(owner);
		try{
			titles.lastSalaryTime = dis.readLong();
			int currentTitleId = dis.readShort();
			Title currentTitle = TitleUtil.getTitle(currentTitleId);
			titles.currentTitle = currentTitle;
			int size = dis.readShort();
			for(int i=0;i<size;i++){
				titles.addTitle(TitleUtil.getTitle(dis.readShort()));
			}
			try{
				int currentEquipTitleId = dis.readShort();
				Title currentEquipTitle = TitleUtil.getTitle(currentEquipTitleId);
				titles.currentEquipTitle = currentEquipTitle;
			} catch (Exception e){
			}
			try {
				int size1 = dis.readShort();
				for(int i=0;i<size1;i++){
					int titleId = dis.readShort();
					int validTime = dis.readInt();
					titles.validTimes.put(titleId, validTime);
				}
			} catch (Exception e) {
			}
		}catch(Exception e){
			log.error(e,e);
		}
		return titles;
	}
	
	public byte[] toClientBytes(){
		List<Title> l1 = new ArrayList<Title>(titles.size());
		List<Title> l2 = new ArrayList<Title>(titles.size());
		List<Title> l3 = new ArrayList<Title>(titles.size());
		for(Title t:titles.values()){
			if(t.type==Title.TYPE_OTHER)
				l1.add(t);
			else if(t.type==Title.TYPE_OFFICIAL){
				l2.add(t);
			}
			else if(t.type==Title.TYPE_COUNTRY){
				l3.add(t);
			}
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try{
			dos.writeShort(currentTitle==null?-1:currentTitle.id);
			dos.writeShort(currentEquipTitle==null?-1:currentEquipTitle.id);
//			dos.writeInt((int)(lastSalaryTime/1000));
			dos.write(l1.size());
			for(Title t:l1){
				dos.write(t.toClientBytes());
			}
			dos.write(l2.size());
			for(Title t:l2){
				dos.write(t.toClientBytes());
			}
			dos.write(l3.size());
			for(Title t:l3){
				dos.write(t.toClientBytes());
			}
//			dos.writeShort(titles.size());
//			for(Title t:titles.values()){
//				dos.write(t.toClientBytes());
//			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return baos.toByteArray();
	}
}
