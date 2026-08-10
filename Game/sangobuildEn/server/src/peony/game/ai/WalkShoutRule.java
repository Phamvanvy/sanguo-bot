package peony.game.ai;

import java.text.MessageFormat;

import com.pip.sanguo.editor.ai.WalkShoutRuleConfig;

/**
 * 走到某位置时喊话的规则。
 * @author lighthu
 */
public class WalkShoutRule implements AIRule {
    private StateCreatureAI ai;
    private WalkShoutRuleConfig cfg;
    
    /*
     * 是否已喊过，只能喊话一次。
     */
    private boolean shouted = false;
    
    public void init(){
    	shouted = false;
    }
    
    public WalkShoutRule(StateCreatureAI ai, WalkShoutRuleConfig cfg) {
        this.ai = ai;
        this.cfg = cfg;
    }
    
    public void update() {
        if (!shouted) {
            if (ai.creature.map.id == cfg.mapID && Math.abs(ai.creature.x - cfg.x) < 16 &&
                    Math.abs(ai.creature.y - cfg.y) < 16) {
                shouted = true;
                if (cfg.message!=null && cfg.message.length() > 0) {
                    ai.creature.shout(MessageFormat.format("{0}：{1}", ai.creature.getShowName(),cfg.message), cfg.messageDistance * 8, 
                            cfg.messageColor, cfg.messageTime);
                }
            }
        }
    }
}
