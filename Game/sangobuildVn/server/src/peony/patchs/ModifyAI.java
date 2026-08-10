package peony.patchs;

import java.lang.reflect.Field;

import peony.game.Creature;
import peony.game.GameObject;
import peony.game.ObjectAccessor;
import peony.game.ai.AIRule;
import peony.game.ai.StateCreatureAI;

public class ModifyAI implements Runnable {

	public void run() {
		for (GameObject o : ObjectAccessor.instanceid2objects.values()) {
			if (o.type == GameObject.TYPE_CREATURE) {
				Creature c = (Creature) o;
				if (c.template.id == 219 || c.template.id == 3
						|| c.template.id == 8) {
					try {
						Field field = Creature.class.getDeclaredField("ai");
						field.setAccessible(true);
						StateCreatureAI ai = (StateCreatureAI)field.get(c);
						Field field1 = StateCreatureAI.class.getDeclaredField("rules");
						field1.setAccessible(true);
						AIRule[] rules = (AIRule[])field1.get(ai);
						AIRule[] newRules = new AIRule[rules.length-1];
						System.arraycopy(rules, 0, newRules, 0, newRules.length);
						field1.set(ai, newRules);
						System.out.println("ok"+c.template.id);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		}
	}

}
