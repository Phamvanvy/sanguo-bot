package peony.patchs;

import peony.game.Creature;
import peony.game.GameObject;
import peony.game.ObjectAccessor;
import peony.game.ai.StateCreatureAI;

public class CreatureAiPatch implements Runnable {

	public void run() {
		for(GameObject o : ObjectAccessor.instanceid2objects.values()){
			if(o!=null && o instanceof Creature){
				Creature c = (Creature)o;
				if(c.template!=null && (c.template.id==1885 || c.template.id==1886 || c.template.id==1887)){
					StateCreatureAI ai = new StateCreatureAI(c);
					ai.config(c.template.aiRules);
				    c.setAI(ai);
				}
			}
		}
	}

}
