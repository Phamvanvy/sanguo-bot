package peony.game.itemeffect;

import java.util.Random;

import org.apache.log4j.Logger;

import peony.game.GameItem;
import peony.game.GameObject;
import peony.game.Horse;
import peony.game.ItemEffect;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Skills;
import peony.game.Unit;
import peony.game.UseItemException;
import peony.game.nation.CandidateService;

public class HorseItemEffect implements ItemEffect {
	
	private static final Logger log = Logger.getLogger(HorseItemEffect.class);

	protected int imageId;
	protected int[] horseTypes;
	protected int initLevel;
	protected String name;
	//0 武将 1 刺客 2 谋士 3 方士
	private int[][] horseSkills = {{72,73,74,75,102,106},{77,78,80,81,102,106},{82,83,84,85,104,110},{87,89,90,91,104,110}};

	protected static final Random rnd = new Random();
	
	public static int MAX_DIFF = 30;

	public HorseItemEffect(int imageId, int[] horseTypes, int initLevel,
			String name) {
		this.imageId = imageId;
		this.horseTypes = horseTypes;
		this.initLevel = initLevel;
		this.name = name;
	}

	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx) throws UseItemException{
		Player p = (Player) source;
		if (source.type == GameObject.TYPE_PLAYER) {
			Horse h = null;
			if (item.object != null) {
				h = (Horse) item.object;
				if((h.level - p.level)>MAX_DIFF)
				throw new UseItemException("馬的等級不能超過人物等級30級以上");
			} else {
				if((initLevel - p.level)>MAX_DIFF){
					throw new UseItemException("馬的等級不能超過人物等級30級以上");
				}
				int i = horseTypes[rnd.nextInt(horseTypes.length)];
				h = ObjectAccessor.createHorse(item.template.id, i, imageId,
						initLevel, name, item.template.showType);
				if(CandidateService.isKingHorse(item.template.id)){
					int[] skills = this.horseSkills[p.clazz];
					h.skills.clear();
					for(int skillId : skills){
						h.addSkill(ObjectAccessor.getSkill(Skills.getSkillId(skillId, 1)), p);
					}
				}
//				Skill skill = HorseUtil.getSkill(p.faction, new int[0]);
//				h.addSkill(skill, p);
//				h.setLevel(initLevel, p);
			}
			h.refreshProperties(false, null);
			if (!p.horseBag.isFull()) {
				p.horseBag.addHorse(h);
				LogUtil.logGetHorse(p, h);
			} else if(p.horseBag.maxSize<10){
				throw new UseItemException("不能放下更多的馬,請擴展坐騎欄");
			} else{
				throw new UseItemException("不能放下更多的馬");
			}
		} else {
			throw new UseItemException("使用目標錯誤");
		}
	}

	public boolean isAsync(){
		return false;
	}
}
