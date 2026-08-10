package peony.game.ai;

import com.pip.sanguo.editor.ai.EscapeRuleConfig;

import peony.game.buff.FearDebuff;

/**
 * 怪物AI控制逃跑的规则。
 * @author lighthu
 */
public class EscapeRule implements AIRule {
    private StateCreatureAI ai;
    private int escapeHPPercent;
    private int time;
    private boolean escaped;
    
    public EscapeRule(StateCreatureAI ai, EscapeRuleConfig cfg) {
        this.ai = ai;
        this.escapeHPPercent = cfg.hp;
        this.time = cfg.duration;
    }
    
    public void update() {
        if (ai.creature.getThreatCount() == 0) {
            escaped = false;
            return;
        }
        if (escaped) {
            return;
        }
        if (ai.creature.hp < ai.creature.maxhp * (float)escapeHPPercent / 100) {
            escaped = true;
            ai.creature.buffs.addBuff(new FearDebuff(ai.creature, time));
        }
    }
    
    public void init(){
    	escaped = false;
    	escapeHPPercent = 0;
    	time = 0;
    }
}
