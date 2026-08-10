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
import peony.game.Server;
import peony.game.Skills;
import peony.game.Time;
import peony.game.Unit;
import peony.game.UseItemException;
import peony.game.changed.ChangedItem;
import peony.game.chat.ChatService;
import peony.game.nation.CandidateService;
import peony.service.ServiceEvent;

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
		long useTime = 0;
		if(Horse.getFreeTime(item.template.id)>0)
			useTime = Time.currentTimeMillis(Time.currTime) + Horse.getFreeTime(item.template.id);
		createHorse(source, item, useTime);
	}
	
	protected Horse createHorse(Unit source, GameItem item, long useTime) throws UseItemException{
		Player p = (Player) source;
		if (source.type == GameObject.TYPE_PLAYER) {
			Horse h = (Horse) item.object;
			
			if(h == null){
				int i = horseTypes[rnd.nextInt(horseTypes.length)];
				h = ObjectAccessor.createHorse(item.template.id, i, imageId,
						initLevel, name, item.template.showType);
			}
			
//			if (item.object != null) {
//				h = (Horse) item.object;
//				if((h.level - p.level)>MAX_DIFF)
//				throw new UseItemException("马的等级不能超过人物等级30级以上");
//			} else {
//				if((initLevel - p.level)>MAX_DIFF){
//					throw new UseItemException("马的等级不能超过人物等级30级以上");
//				}
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
//			}
			h.refreshProperties(false, null);
			if (!p.horseBag.isFull()) {
				p.horseBag.addHorse(h);
				LogUtil.logGetHorse(p, h);
				//增加坐骑事件
				Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_ADD_HORSE,p));
				if(useTime>0)
					h.freeHorseEndTime = useTime;
				if(h.freeHorseEndTime > 0){
					h.state = 2;
				}else{
					h.setActive();
				}
				h.addIntPropertyChangedItem(p.changed, ChangedItem.HORSE_STATE, h.state, false);
				return h;
			} else if(p.horseBag.maxSize<10){
				throw new UseItemException("不能放下更多的马,请扩展坐骑栏");
			} else{
				throw new UseItemException("不能放下更多的马");
			}
		} else {
			throw new UseItemException("使用目标错误");
		}
	}
	
	public void getHorseAndRide(Unit source, GameItem item, long useTime) throws UseItemException {
		Player p = (Player) source;
		if (source.type == GameObject.TYPE_PLAYER) {
			Horse h = createHorse(source, item, useTime);
			p.horseRide(h.instanceId, 0, -1);
			ChatService chatService = Server.server.getServiceRegistry().getChatService();
			chatService.sendPrivateShout(p.id, 0x0000ff, 6000, p.faction, "恭喜我吧！跨上战马了~~不要太嫉妒我啊！");
		}
	}
	
	public int getHorseImageId(){
		return this.imageId;
	}

	public boolean isAsync(){
		return false;
	}
}
