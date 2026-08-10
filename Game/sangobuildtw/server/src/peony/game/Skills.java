package peony.game;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import peony.game.buff.Buff;
import peony.game.changed.SkillChangedItem;
import peony.game.skill.Skill;

import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.skill.SkillConfig;

public class Skills implements Cloneable{

	public static final int DEFAULT_BOOKSKILL_SIZE = 3;
	protected static SkillComparator comparator = new SkillComparator();
	
	protected TreeMap<Integer,Skill> skills = new TreeMap<Integer,Skill>();
	protected TreeMap<Integer,Skill> bookSkills = new TreeMap<Integer,Skill>();
	protected int bookSkillSize;
	
	public Skills(int bookSkillSize){
		this.bookSkillSize = bookSkillSize;
	}
	
	
	public void clear(){
		skills.clear();
	}
	
//	public List<Skill> refresh(Changed changed,boolean notify){
//		List<Skill> l = new LinkedList<Skill>();
//		Iterator<Skill> ite = skills.values().iterator();
//		while(ite.hasNext()){
//			Skill skill = ite.next();
//			if(skill.getId()!=1&&skill.getLevel()>0){
//				l.add(ObjectAccessor.getSkill(getSkillId(skill.getGroupId(),0)));
//			}
//		}
//		if(l.size()>0){
//			for(Skill skill:l){
//				addSkill(skill,changed,notify);
//			}
//			return true;
//		}
//		return false;
//	}
	
	public int getBookSkillSize(){
		return bookSkillSize;
	}
	
	public int getCurrentBookSkillSize(){
		return bookSkills.size();
	}
	
	public void addBookSkill(Skill skill,Changed changed,boolean notify){
		bookSkills.put(skill.getId(), skill);
		if (changed != null) {
			if (notify) {
				SkillChangedItem changedItem = new SkillChangedItem(skill, true);
				changed.addChangedItem(changedItem);
			}
			SkillChangedItem changedItem = new SkillChangedItem(skill,false);
			changed.addChangedItem(changedItem);
		}
	}
	
	public boolean hasSkill(int id){
		boolean contains = skills.containsKey(id);
		if(!contains){
			return bookSkills.containsKey(id);
		}
		return contains;
	}
	
	public Skill getSkill(int id){
		Skill ret = skills.get(id);
		if(ret==null){
			return bookSkills.get(id);
		}
		return ret;
	}
	
	public Skill getSkillByGroupId(int groupId){
		for(Skill s:skills.values()){
			if(s.getLevel()!=0&&s.getGroupId()==groupId){
				return s;
			}
		}
		for(Skill s:bookSkills.values()){
			if(s.getGroupId()==groupId)
				return s;
		}
		return null;
	}
	
	public Skill getBookSkill(int id){
		return bookSkills.get(id);
	}
	
	public Skill getBookSkilByGroupId(int groupId){
		for(Skill skill:bookSkills.values()){
			if(skill.getGroupId()==groupId)
				return skill;
		}
		return null;
	}
	
	public Skill removeSkill(int groupId,int level){
		int id = getSkillId(groupId,level);
		return skills.remove(id);
	}
	
	public void removeSkillByGroupId(int groupId){
		Iterator<Skill> ite = skills.values().iterator();
		while(ite.hasNext()){
			Skill s = ite.next();
			if(s.getGroupId()==groupId)
				ite.remove();
		}
	}
	
	public Skill removeBookSkill(int groupId,int level){
		int id = getSkillId(groupId,level);
		return bookSkills.remove(id);
	}
	
	public Skill removeBookSkill(int skill){
		return bookSkills.remove(skill);
	}
	
	public void addSkill(Skill skill,Changed changed,boolean notify){
		skills.put(skill.getId(), skill);
		if (changed != null) {
			if (notify) {
				SkillChangedItem changedItem = new SkillChangedItem(skill, true);
				changed.addChangedItem(changedItem);
			}
			SkillChangedItem changedItem = new SkillChangedItem(skill,false);
			changed.addChangedItem(changedItem);
		}
	}
	
	protected void addSkillSlient(Skill skill){
		skills.put(skill.getId(), skill);
	}
	
	
	public List<Skill> getAutoLearSkills(int level){
		List<Skill> ret = new ArrayList<Skill>(3);
		for(Skill skill:skills.values()){
			if(skill.isAutoLearn()&&skill.getLevel()==0&&skill.getNextLevel().getRequireLevel()<=level){
				ret.add(skill);
			}
		}
		return ret;
	}
	
	public Collection<Buff> getAreaBuffs() {
		List<Buff> l = new ArrayList<Buff>(5);
		for (Skill skill : skills.values()) {
			if (skill.getLevel() > 0) {
				Buff buff = skill.getAreaBuff();
				if (buff != null)
					l.add(buff);
			}
		}
		return l;
	}
	
//	public void enhance(PropertyCalculator pc){
//		for(Skill skill:skills.values()){
//			skill.enhance(pc);
//		}
//	}
	
	public List<Skill> getSkills(){
		return new ArrayList<Skill>(skills.values());
	}
	
	public Collection<Skill> getBookSkills(){
		return new ArrayList<Skill>(bookSkills.values());
	}
	
	@Override
	public Skills clone() {
		Skills ret = new Skills(bookSkillSize);
		ret.skills = new TreeMap<Integer,Skill>(skills);
		ret.bookSkills = new TreeMap<Integer,Skill>(bookSkills);
		return ret;
	}
	
	
	public static byte[] getDBBytes(Skills skills){
		ByteArrayOutputStream baos = new ByteArrayOutputStream(200);
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.write(2); //version
			Collection<Skill> ss = skills.getSkills();
			dos.write(ss.size());
			for(Skill skill:ss){
				dos.writeInt(skill.getId());
			}
			Collection<Skill> bs = skills.getBookSkills();
			dos.write(skills.bookSkillSize);
			dos.write(bs.size());
			for(Skill skill:bs){
				dos.writeInt(skill.getId());
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return baos.toByteArray();
	}
	
	public static Skills getSkillsFromDB(byte[] bytes, Player player) {
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		DataInputStream dis = new DataInputStream(bais);
		Skills skills = null;
		try {
			int version = dis.readByte(); // version
			if (version == 1) {
				skills = new Skills(DEFAULT_BOOKSKILL_SIZE);
				int size = dis.read();
//				List<Skill> initSkills = ObjectAccessor.getPlayerInitSkills(player.clazz); //为了新添加的技能用的
//				for(Skill s:initSkills){
//					skills.addSkill(s, null, false);
//				}
				for (int i = 0; i < size; i++) {
					int id = dis.readInt();
					Skill skill = ObjectAccessor.getSkill(id);
//					if (skill.getLevel() > 0) {
//						skills.removeSkillByGroupId(skill.getGroupId()); //如果有技能等级大于0,那么先把0级的技能去掉
//					}
					skills.addSkill(skill, null, false);
				}
				
			}else{
				skills = new Skills(DEFAULT_BOOKSKILL_SIZE);
				int size = dis.read();
//				List<Skill> initSkills = ObjectAccessor.getPlayerInitSkills(player.clazz); //为了新添加的技能用的
//				for(Skill s:initSkills){
//					skills.addSkill(s, null, false);
//				}
				for (int i = 0; i < size; i++) {
					int id = dis.readInt();
					Skill skill = ObjectAccessor.getSkill(id);
//					if (skill.getLevel() > 0) {
//						skills.removeSkillByGroupId(skill.getGroupId()); //如果有技能等级大于0,那么先把0级的技能去掉
//					}

					skills.addSkill(skill, null, false);
				}
				skills.bookSkillSize = dis.read();
				size = dis.read();
				for(int i=0;i<size;i++){
					int id = dis.readInt();
					Skill skill = ObjectAccessor.getSkill(id);
					if(skills.getBookSkilByGroupId(skill.getGroupId())==null){ //原来存在一个bug，导致相同的技能书能学习多次
						skills.addBookSkill(skill, null, false);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return skills;
	}
	
	public static int getSkillId(int groupId,int level){
		return ((groupId<<16)|level);
	}
	
	public byte[] toClientBytes(Unit owner){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try{
			TreeSet<Skill> ss = new TreeSet<Skill>(comparator);
			ss.addAll(skills.values());
			dos.write(skills.size());
			for(Skill skill:skills.values()){
				dos.write(skill.toClientBytes(owner));
			}
			dos.write(bookSkillSize);
			dos.write(getCurrentBookSkillSize());
			for(Skill skill:bookSkills.values()){
				dos.write(skill.toClientBytes(owner));
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return baos.toByteArray();
	}
}

class SkillComparator implements Comparator<Skill>{

	public int compare(Skill o1, Skill o2) {
		ProjectData prj = Server.server.getServiceRegistry().getDataService().data;
		return getIndex(prj,o1) - getIndex(prj,o2);
	}
	
	protected int getIndex(ProjectData prj,Skill s){
		if(s.getGroupId()==0)
			return -1;
		else{
			SkillConfig sc1 = (SkillConfig)prj.findObject(SkillConfig.class, s.getGroupId());
			return sc1.editorIndex;
		}
	}
}
