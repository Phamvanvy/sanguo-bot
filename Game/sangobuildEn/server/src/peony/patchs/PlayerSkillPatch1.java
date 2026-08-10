package peony.patchs;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.TreeMap;

import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.Server;
import peony.game.Skills;
import peony.game.buff.Buff;
import peony.game.skill.Skill;

public class PlayerSkillPatch1 implements Runnable {

	public void run() {
		Server.server.getServiceRegistry().getPlayerService().cache.removeAll();
		for (Player p : ObjectAccessor.players.values()) {
			try {
				process(p, p.skills);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void process(Player p, Skills ss) throws Exception {
		Field f = Skills.class.getDeclaredField("skills");
		f.setAccessible(true);
		TreeMap<Integer,Skill> skills = (TreeMap<Integer,Skill>)f.get(ss);
		f = Skills.class.getDeclaredField("bookSkills");
		f.setAccessible(true);
		TreeMap<Integer,Skill> bookSkills = (TreeMap<Integer,Skill>)f.get(ss);
		
		processMap(p, skills);
		processMap(p, bookSkills);
	}
	
	private void processMap(Player p, Map<Integer, Skill> map) {
		Object[] arr = map.keySet().toArray();
		for (Object o : arr) {
			int id = ((Integer)o).intValue();
			Skill newSkill = ObjectAccessor.getSkill(id);
			if (newSkill != null) {
				map.put(id, newSkill);
				Buff buff = newSkill.newBuff();
				if (buff != null) {
					p.buffs.removeBuff(buff.getId());
					p.buffs.addBuff(buff);
				}
			}
		}
	}
}
