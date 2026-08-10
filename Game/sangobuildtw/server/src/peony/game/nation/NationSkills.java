package peony.game.nation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;


public class NationSkills {
	
	
	public Map<Integer,NationSkill> skills = new TreeMap<Integer,NationSkill>();
	
	/**
	 * 在载入一个国家的技能以后需要进行init操作，传入的参数是当前初始就应该存在的国家技能，用在添加新的国家技能以后可以即时的更新
	 * @param initSkills
	 */
	public void init(Collection<NationSkill> initSkills){
		for(NationSkill skill:initSkills){
			if(skills.get(skill.id)==null){
				skills.put(skill.id, skill.clone());
			}
		}
	}
	
	public NationSkill get(int id){
		return skills.get(id);
	}
	
	public int size(){
		return skills.size();
	}

	public byte[] toDBBytes() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.write(1);// version
			dos.writeInt(skills.size());
			for (NationSkill skill : skills.values()) {
				dos.writeInt(skill.id);
				dos.write(skill.level);
				dos.writeInt(skill.upgradeDay);
				dos.writeInt(skill.maintainDay);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return baos.toByteArray();
	}

	public static NationSkills fromDBBytes(byte[] bytes) {
		if(bytes==null){
			NationSkills ret = new NationSkills();
			return ret;
		}
		NationSkills ret = null;
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		DataInputStream dis = new DataInputStream(bais);
		try {
			int version = dis.readByte();// version
			if (version == 1) {
				ret = new NationSkills();
				int size = dis.readInt();
				for (int i = 0; i < size; i++) {
					int id = dis.readInt();
					int level = dis.read();
					int upgradeDay = dis.readInt();
					int maintainDay = dis.readInt();
					NationSkill skill = NationService.newNationSkill(id, level);
					skill.upgradeDay = upgradeDay;
					skill.maintainDay = maintainDay;
					ret.skills.put(skill.id, skill);
				}
			}
		}catch(Exception e){
			throw new IllegalArgumentException();
		}
		return ret;
	}
	
	public NationSkills clone(){
		NationSkills ret = new NationSkills();
		ret.skills.putAll(skills);
		return ret;
	}
}