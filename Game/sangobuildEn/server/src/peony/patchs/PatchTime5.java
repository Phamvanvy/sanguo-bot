package peony.patchs;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.List;

import peony.db.PlayerDAO;
import peony.game.CoolDown;
import peony.game.CoolDownList;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.Server;
import peony.game.Time;
import peony.game.buff.Buff;
import peony.game.buff.Buffs;
import ch.javasoft.util.intcoll.IntHashMap;

public class PatchTime5 implements Runnable {

	public void run() {
		try {
			PlayerDAO dao = new PlayerDAO();
			Calendar cal = Calendar.getInstance();
			cal.set(2010, Calendar.AUGUST, 20, 10, 0, 0);
			List<Integer> list = dao.list("select p.id from Player p where p.lastLoginTime > ?", cal.getTime());
			Field f = CoolDownList.class.getDeclaredField("coolDowns");
			f.setAccessible(true);
			Field f1 = Buffs.class.getDeclaredField("buffs");
			f1.setAccessible(true);
			for (int id : list) {
				Player p1 = ObjectAccessor.getPlayer(id);
				System.out.println("try load player " + id);
				boolean needSave = false;
				boolean changed = false;
				if (p1 == null) {
					try {
						p1 = Server.server.getServiceRegistry().getPlayerService().loadPlayerSilent(id);
					} catch (Exception e) {
						e.printStackTrace();
						continue;
					}
					needSave = true;
				}
				IntHashMap<CoolDown> map = (IntHashMap<CoolDown>)f.get(p1.coolDowns);
				for (CoolDown cd : map.values()) {
					if (cd.endTime > Time.currTime + 20 * 3600 * 1000L) {
						System.out.println("cooldown error: playerid=" + p1.id + ", currtime=" + Time.currTime + ", endtime=" + cd.endTime);
						cd.endTime = Time.currTime;
						changed = true;
					}
				}
				List<Buff> buffs = (List<Buff>)f1.get(p1.buffs);
				for (int i = 0; i < buffs.size(); i++) {
					Buff buff = buffs.get(i);
					if (buff.getEndTime() > Time.currTime + 20 * 3600 * 1000L) {
						System.out.println("buff error: playerid=" + p1.id + ", currtime=" + Time.currTime + ", endtime=" + buff.getEndTime());
						p1.buffs.removeBuff(buff);
						i--;
						changed = true;
					}
				}
				if (needSave && changed) {
					System.out.println("save player " + id);
					Server.server.getServiceRegistry().getPlayerService().savePlayer(p1);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
