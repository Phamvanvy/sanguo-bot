package peony.game.ai;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math.random.RandomDataImpl;
import org.apache.log4j.Logger;

import peony.game.ObjectAccessor;
import peony.game.Skills;
import peony.game.Time;
import peony.game.skill.Skill;

import com.pip.sanguo.editor.ai.AITargetType;
import com.pip.sanguo.editor.ai.SkillAttackRuleConfig;

/**
 * 怪物AI控制技能攻击的规则。
 * @author lighthu
 */
public class SkillAttackRule implements AIRule {
	
	private static final Logger log = Logger.getLogger(SkillAttackRule.class);
    private static RandomDataImpl rand = new RandomDataImpl();
    
    private StateCreatureAI ai;
    private SkillAttackRuleConfig cfg;
    
    private List<Skill> skills;
    private List<AITargetType> targetTypes;
    private int lastCastTime;
    private int castCount;
    private int nextInterval;
    private boolean inBattle;
    
    // 等待施放的技能
    private List<Skill> pendingSkills;
    private List<AITargetType> pendingTargetTypes;
    
    public void init(){
        castCount = 0;
        inBattle = false;
        pendingSkills = null;
        pendingTargetTypes = null;
    }
    
    public SkillAttackRule(StateCreatureAI ai, SkillAttackRuleConfig cfg) {
        this.ai = ai;
        this.cfg = cfg;
        if (cfg.interval < 2) {
            cfg.interval = 2;
        }
        int skillID = Skills.getSkillId(cfg.skill, cfg.skillLevel);
        Skill skill = ObjectAccessor.getSkill(skillID);
        if (skill == null) {
        	log.error("[SKILLNOTFOUND]SKILL["+ skillID + "]CREATURE[" + ai.creature.template.id + "]");
            throw new IllegalArgumentException();
        }
        skills = new ArrayList<Skill>();
        skills.add(skill);
        targetTypes = new ArrayList<AITargetType>();
        targetTypes.add(cfg.targetType);
        if (cfg.skill2 != 0) {
            skillID = Skills.getSkillId(cfg.skill2, cfg.skillLevel2);
            skill = ObjectAccessor.getSkill(skillID);
            if (skill == null) {
                throw new IllegalArgumentException();
            }
            skills.add(skill);
            targetTypes.add(cfg.targetType2);
        }
        if (cfg.skill3 != 0) {
            skillID = Skills.getSkillId(cfg.skill3, cfg.skillLevel3);
            skill = ObjectAccessor.getSkill(skillID);
            if (skill == null) {
                throw new IllegalArgumentException();
            }
            skills.add(skill);
            targetTypes.add(cfg.targetType3);
        }
    }
    
    public void update() {
        if (ai.creature.getThreatCount() == 0) {
            castCount = 0;
            inBattle = false;
            pendingSkills = null;
            pendingTargetTypes = null;
            return;
        } else if (inBattle == false) {
            inBattle = true;
            lastCastTime = Time.currTime;
            nextInterval = cfg.firstInterval;
        }
        if (ai.creature.attack != null || ai.creature.isChaosState()) {
            return;
        }
        if (pendingSkills != null) {
            // 尝试施放技能
            ai.tryAttack(pendingSkills.get(0), pendingTargetTypes.get(0));
            pendingSkills.remove(0);
            pendingTargetTypes.remove(0);
            if (pendingSkills.size() == 0) {
                pendingSkills = null;
                pendingTargetTypes = null;
            }
            return;
        }
        if (castCount >= cfg.useTimes) {
            return;
        }
        if (Time.currTime - lastCastTime < nextInterval) {
            return;
        }
        if (ai.creature.hp <= ai.creature.maxhp * (float)cfg.hp / 100) {
            // 启动技能序列
            pendingSkills = new ArrayList<Skill>();
            pendingSkills.addAll(skills);
            pendingTargetTypes = new ArrayList<AITargetType>();
            pendingTargetTypes.addAll(targetTypes);
            lastCastTime = Time.currTime;
            castCount++;
            if (cfg.intervalDeviation > 0) {
				nextInterval = (int) rand.nextGaussian(cfg.interval,
						cfg.intervalDeviation);
			} else {
			    nextInterval = cfg.interval;
			}
            if (cfg.message != null && cfg.message.length() > 0) {
                ai.creature.shout(MessageFormat.format("{0}：{1}", ai.creature.getShowName(),cfg.message), cfg.messageDistance * 8, 
                        cfg.messageColor, cfg.messageTime);
            }
        }
    }
}
