package peony.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import peony.game.attendant.Attendant;
import peony.game.party.Party;
import peony.game.party.PartyMember;

/**
 * 战斗贡献记录。本类在怪物进入战斗时创建，用来记录整个战斗过程中所有敌人（包括NPC和玩家）对
 * 此怪物造成威胁的记录。具体规则见掉落规则设定文档。
 * @author lighthu
 */
public class BattleContributionList {
    // 对应怪物
    protected Creature creature;
    // 个人贡献表，记录所有NPC和未组队的玩家的贡献值
    protected HashMap<GameObjectRef, Float> personalContribs;
    // 队伍贡献表，记录所有参与战斗的玩家对队伍的贡献值。key是队伍ID，value是队伍中所有人员的贡献。
    protected HashMap<Integer, HashMap<GameObjectRef, Float>> partyContribs;
    // 国家贡献度
    protected HashMap<Integer, Float> nationContribs;
    // 当前拥有怪物掉落的玩家或队伍，如果是玩家，这个对象是GameObjectRef，否则是Party
    public Object owner;
    // 上次检查时间，每3秒检查一次
    protected int lastUpdateTime;
    
    public BattleContributionList(Creature creature) {
        this.creature = creature;
        personalContribs = new HashMap<GameObjectRef, Float>();
        partyContribs = new HashMap<Integer, HashMap<GameObjectRef, Float>>();
        nationContribs = new HashMap<Integer, Float>();
    }
    
    /**
     * 取得当前怪物拥有者。
     */
    public Object getOwner() {
        return owner;
    }
    
    /**
     * 处理怪物被攻击事件。如果当前没有拥有者，攻击将决定新的拥有者。
     * @param source
     */
    public void attacked(Unit source) {
        if (owner == null && ( source.type == GameObject.TYPE_PLAYER || source.type == GameObject.TYPE_ATTENDANT)) {
        	Player p = null;
        	if(source.type == GameObject.TYPE_ATTENDANT){
        		Attendant att = (Attendant)source;
        		p = att.owner;
        	} else {
            p = (Player)source;
        	}
            if (p.party != null) {
                owner = p.party;
            } else {
                owner = p.ref();
            }
            creature.moveType |= GameObject.MOVE_OWNER|GameObject.MOVE_DETAIL;
        }
    }
    
    /**
     * 记录新的仇恨值。
     * @param u
     * @param threatValue
     * @param direct
     */
    public void newThreat(Unit u, float threatValue) {
        if (u.type == GameObject.TYPE_CREATURE) {
            // 如果是怪物，直接记录到个人贡献表中
            addThreatToList(personalContribs, u.ref(), threatValue);
        } else if (u.type == GameObject.TYPE_PLAYER){
            Player p = (Player)u;
            GameObjectRef pref = p.ref();
            
            // 如果玩家没有队伍，则记录为个人贡献；如果有队伍，则把过去的个人贡献以及新的
            // 贡献都记为队伍贡献。
            if (p.party == null) {
                addThreatToList(personalContribs, pref, threatValue);
                addNationContribute(threatValue, p.faction);
            } else {
                Float oldValue = personalContribs.get(pref);
                if (oldValue != null) {
                    threatValue += oldValue.floatValue();
                    personalContribs.remove(pref);
                }
                HashMap<GameObjectRef, Float> partyMap = partyContribs.get(p.party.id);
                if (partyMap == null) {
                    partyMap = new HashMap<GameObjectRef, Float>();
                    partyContribs.put(p.party.id, partyMap);
                }
                addThreatToList(partyMap, pref, threatValue);
                addNationContribute(threatValue, p.faction);
            }
            
            // 如果玩家是怪物的拥有者，并且他的队伍现在不为空，说明他在战斗过程中加入了一个
            // 队伍，这时把怪物的拥有者切换为他的队伍
            if (owner != null && pref.equals(owner) && p.party != null) {
                owner = p.party;
                creature.moveType |= GameObject.MOVE_OWNER|GameObject.MOVE_DETAIL;
            }
        } else if(u instanceof Attendant){
        	Attendant attendant = (Attendant)u;
        	Player owner = attendant.owner;
        	if(owner!=null){
        		addNationContribute(threatValue, owner.faction);
        	}
        }
    }
    
    /*
     * 在一个贡献记录表中加入新的数据。
     */
    private void addThreatToList(HashMap<GameObjectRef, Float> map, GameObjectRef u, float value) {
        Float oldValue = map.get(u);
        if (oldValue == null) {
            map.put(u, value);
        } else {
            map.put(u, oldValue.floatValue() + value);
        }
    }
    
    private void addNationContribute(float value, int faction){
    	if(faction>0 && faction<4){
        	Float oldFactionValue = nationContribs.get(faction);
            if(oldFactionValue==null){
            	nationContribs.put(faction, value);
            }else{
            	nationContribs.put(faction, oldFactionValue.floatValue()+value);
            }
        }
    }
 
    /**
     * 每3秒检查一次，如果怪物的当前受益人（如果是队伍，则计算所有对此队伍有贡献的玩家，即使这个玩
     * 家已经离开队伍）全部离开怪物的仇恨表，更新拥有者信息。
     * @param diffTime
     */
    public void update(int diffTime) {
        if (Time.currTime - lastUpdateTime > 3000) {
            lastUpdateTime = Time.currTime;
            if (owner != null) {
                // 检查是否还有受益人存在于仇恨列表中
                boolean ownerExists = false;
                if (owner instanceof GameObjectRef) {
                    ownerExists = creature.threats.contains((GameObjectRef)owner);
                } else {
                    Party party = (Party)owner;
                    HashMap<GameObjectRef, Float> partyMap = partyContribs.get(party.id);
                    if (partyMap != null) {
                        for (GameObjectRef ref : partyMap.keySet()) {
                            ownerExists = creature.threats.contains(ref);
                            if (ownerExists) {
                                break;
                            }
                        }
                    }
                }
                
                // 如果没有受益人存在于仇恨表了，分2种情况：
                // 9.1 如果此时怪物生命值 > 20%，则清除怪物受益人信息，下一个攻击怪物的玩家或队伍成为新的受益人。
                // 9.2 如果此时怪物生命值 <= 20%，则受益人不变。
                if (!ownerExists && creature.hp >= creature.maxhp / 5) {
                    owner = null;
                    creature.moveType |= GameObject.MOVE_OWNER|GameObject.MOVE_DETAIL;
                }
            }
        }
    }
    
    /*
     * 计算玩家的总贡献。包括个人玩家贡献和队伍贡献。
     * @return
     */
    private float getPlayerContribution() {
        float ret = 0.0f;
        for (GameObjectRef ref : personalContribs.keySet()) {
            if (ref.type == GameObject.TYPE_PLAYER) {
                ret += personalContribs.get(ref);
            }
        }
        for (HashMap<GameObjectRef, Float> map : partyContribs.values()) {
            for (float v : map.values()) {
                ret += v;
            }
        }
        return ret;
    }
    
    public int getMaxContributionFaction(){
    	int maxContributionFaction = 0;
    	float maxContribution = 0;
    	Float value = nationContribs.get(GameObject.FACTION_WEI);
    	if(value!=null){
    		maxContribution = value.floatValue();
    		maxContributionFaction = GameObject.FACTION_WEI;
    	}
    	value = nationContribs.get(GameObject.FACTION_SHU);
    	if(value!=null && value>maxContribution){
    		maxContribution = value.floatValue();
    		maxContributionFaction = GameObject.FACTION_SHU;
    	}
    	value = nationContribs.get(GameObject.FACTION_WU);
    	if(value!=null && value>maxContribution){
    		maxContribution = value.floatValue();
    		maxContributionFaction = GameObject.FACTION_WU;
    	}
    	return maxContributionFaction;
    }
    
    /*
     * 计算NPC的总贡献。
     */
    private float getNPCContribution() {
        float ret = 0.0f;
        for (GameObjectRef ref : personalContribs.keySet()) {
            if (ref.type == GameObject.TYPE_CREATURE) {
                ret += personalContribs.get(ref);
            }
        }
        return ret;
    }
    
    /**
     * 怪物死亡，检查是否有符合条件的受益人。规则如下：
     * 10.1 如果受益人是玩家，则此人作为唯一最终受益人。
     * 10.2 如果受益人是队伍，则在战斗过程中所有对此队伍有过贡献的人，以及在战斗结束时在队伍中的人都算为最终受益人。
     * 10.3 受益人必须在怪物死亡位置周围40码范围内。
     * 11.1 如果没有符合条件的最终受益人，则怪物无掉落。
     * 11.2 如果所有玩家的贡献值总和小于NPC的贡献值总和，则怪物无掉落。
     * @return 如果怪物符合掉落条件，并且有符合条件的受益人存在，返回受益人列表，否则返回null。
     */
    public List<Player> checkOwners() {
        // 11.1 计算所有玩家的贡献值和NPC的贡献值，如果前者小于后者，返回null
        if (getPlayerContribution() < getNPCContribution()) {
            return null;
        }
        if (owner == null) {
            return null;
        }
        
        List<Player> ret = new ArrayList<Player>();
        Set<Integer> foundIDs = new HashSet<Integer>();
        if (owner instanceof GameObjectRef) {
            // 10.1 如果受益人是玩家，则此人作为唯一最终受益人。
            Player p = (Player)ObjectAccessor.getGameObject((GameObjectRef)owner);
            if (p != null && p.inRange(creature, 320)) {
                ret.add(p);
            }
        } else {
            Party party = (Party)owner;
            
            // 10.2 如果受益人是队伍，则在战斗过程中所有对此队伍有过贡献的人，以及在战斗结束时在队伍中的人都算为最终受益人。
            HashMap<GameObjectRef, Float> partyMap = partyContribs.get(party.id);
            if (partyMap != null) {
                for (GameObjectRef ref: partyMap.keySet()) {
                    Player p = (Player)ObjectAccessor.getGameObject(ref);
                    if (p != null && p.inRange(creature, 320)) {
                        ret.add(p);
                        foundIDs.add(p.id);
                    }
                }
            }
            
            // 当前在队伍中的人都可能作为受益人
            synchronized (party) {
	            for (PartyMember m : party.members) {
	                if (!foundIDs.contains(m.player.id) && m.player.inRange(creature, 320)) {
	                    ret.add(m.player);
	                    foundIDs.add(m.player.id);
	                }
	            }
            }
        }
        
        // 11.1 如果没有符合条件的最终受益人，则怪物无掉落。
        if (ret.size() == 0) {
            return null;
        } else {
            return ret;
        }
    }
    
    /**
     * 取得所有在战斗中贡献过仇恨的单位。
     */
    public Set<GameObjectRef> getAllContributors() {
    	return personalContribs.keySet();
    }
}
