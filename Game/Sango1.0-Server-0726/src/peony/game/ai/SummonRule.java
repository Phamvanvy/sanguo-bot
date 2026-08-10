package peony.game.ai;

import java.text.MessageFormat;

import org.apache.commons.math.random.RandomDataImpl;
import org.apache.log4j.Logger;

import peony.game.Creature;
import peony.game.Server;
import peony.game.Time;
import peony.game.VMapUtil;

import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.map.GameMapObject;
import com.pip.sanguo.editor.ai.SummonRuleConfig;

/**
 * 怪物AI控制技能攻击的规则。
 * 
 * @author lighthu
 */
public class SummonRule implements AIRule {
    private static Logger log = Logger.getLogger(SummonRule.class);
    private static RandomDataImpl rand = new RandomDataImpl();

    private StateCreatureAI ai;
    private SummonRuleConfig cfg;

    private int lastCastTime;
    private int castCount;
    private int nextInterval;
    private boolean inBattle;

    public SummonRule(StateCreatureAI ai, SummonRuleConfig cfg) {
        this.ai = ai;
        this.cfg = cfg;
        if (cfg.interval < 2) {
            cfg.interval = 2;
        }
    }
    
    public void init(){
        castCount = 0;
        inBattle = false;
    }

    public void update() {
        if (ai.creature.getThreatCount() == 0) {
            castCount = 0;
            inBattle = false;
            return;
        } else if (inBattle == false) {
            inBattle = true;
            nextInterval = cfg.interval;
        }
        if (castCount >= cfg.useTimes) {
            return;
        }
        if (ai.creature.attack != null) {
            return;
        }
        if (Time.currTime - lastCastTime < nextInterval) {
            return;
        }
        if (ai.creature.hp <= ai.creature.maxhp * (float)cfg.hp / 100) { 
            // 召唤小怪
            try {
                ProjectData proj = Server.server.getServiceRegistry().getDataService().data;
                for (int i = 0; i < cfg.monsters.length; i++) {
                    if (cfg.monsters[i] == 0) {
                        continue;
                    }
                    GameMapObject gmo = GameMapObject.findByID(proj, cfg.monsters[i]);
                    if (gmo != null && gmo instanceof GameMapNPC) {
                        Creature c1 = (Creature)VMapUtil.addCreature(ai.creature.getVMap(), (GameMapNPC)gmo, true, cfg.liveTime, Server.server.revision);
                        c1.cloneInitThreats(ai.creature);
                    }
                }
            } catch (Exception e) {
                log.error(e, e);
            }
            
            lastCastTime = Time.currTime;
            castCount++;
            nextInterval = cfg.interval;
            if (cfg.message != null && cfg.message.length() > 0) {
            	ai.creature.shout(MessageFormat.format("{0}：{1}", ai.creature.getShowName(),cfg.message), cfg.messageDistance * 8, 
            	        cfg.messageColor, cfg.messageTime);
            }
        }
    }
}
