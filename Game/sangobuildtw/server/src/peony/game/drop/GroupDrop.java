package peony.game.drop;

import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import peony.game.Gain;
import peony.game.Player;


/**
 * 一个组掉落包括了一系列的不同等级的子掉落,计算掉落的时候需要根据人物等级先算出子掉落
 * @author Jeffrey
 *
 */
public class GroupDrop implements Drop{
	/**
	 * Logger for this class
	 */
	private static final Logger log = Logger.getLogger(GroupDrop.class);

	protected int id;
	protected boolean valid;
	protected List<LeveledGroupDrop> drops = new LinkedList<LeveledGroupDrop>();
	
	public GroupDrop(int id){
		this.id = id;
	}
	
	public int getId(){
		return id;
	}
	
	public boolean isValid() {
		return valid;
	}
	
	public void setValid(boolean value) {
		valid = value;
	}
	
	public void clear() {
	    drops.clear();
	}
	
	public void addDrop(LeveledGroupDrop drop){
		drops.add(drop);
	}
	
	/**
	 * 在计算掉落的时候先确定是那个级别的掉落
	 */
	public void calc(Random rnd,Gain gain) {
		if (!valid) {
			return;
		}
		Player player = gain.getPlayer();
		if(drops.size()==0)
			log.error("[DROPGROUPERROR]ID[" + id + "]");
		if(player!=null){
			for(LeveledGroupDrop drop:drops){
				if(player.level>=drop.minLevel&&player.level<=drop.maxLevel){
				    if (drop.clazz == -1 || drop.clazz == player.clazz) {
    					drop.calc(rnd, gain);
    					return;
				    }
				}
			}
		}
	}

}

