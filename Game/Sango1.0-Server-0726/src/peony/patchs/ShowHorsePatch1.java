package peony.patchs;

import peony.game.Horse;
import peony.game.ObjectAccessor;
import peony.game.Player;

public class ShowHorsePatch1 implements Runnable{
	public void run(){
		Player p = ObjectAccessor.getPlayer(7426);
		for(Horse h:p.horseBag.horses){
			StringBuffer sb = new StringBuffer(200);
			sb.append("ID[");
			sb.append(h.template.id);
			sb.append("]");
			sb.append("NAME[");
			sb.append(h.name);
			sb.append("]");
			sb.append("ICON[");
			sb.append(h.iconId);
			sb.append("]");
			sb.append("INSTANCEID[");
			sb.append(h.instanceId);
			sb.append("]");
			System.out.println(sb.toString());
		}
	}
}
