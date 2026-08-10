package peony.game.itemeffect;

import java.text.MessageFormat;
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
						initLevel, name, item.template.showImage, item.template.showType);
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
						h.addSkill(ObjectAccessor.getSkill(Skills.getSkillId(skillId, 2)), p);
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
					if(h.indexOfFreeHorses(item.template.id)!=-1){
						if(!h.isActive()){
						    h.addStringPropertyChangedItem(p.changed, ChangedItem.HORSE_NAME, h.name+peony.Messages.STRING_00883, false);
						    h.freeHorseEndTime = -1;
						}
					}else{
					    h.setActive();
					}
				}
				if(!CandidateService.isKingHorse(item.template.id) ){
					if(p.isKing()==1){
						if(p.pool.getInt(KingItemEffect.PROPERTY_USEKINGHORSE, 0)==1)
				           KingItemEffect.kingAddHorse(p,h);
					}else{
						KingItemEffect.notKingResetImage(p);
					}
				}
				h.addIntPropertyChangedItem(p.changed, ChangedItem.HORSE_STATE, h.state, false);
				return h;
			} else if(p.horseBag.maxSize<10){
				String msg = MessageFormat.format("坐骑栏已满\n请到坐骑管理界面扩充\n需要花费<cff0000>{0}</c>金钱\n或购买使用高级坐骑栏扩展符",p.horseBag.getExtendHorsebagMoney());
				throw new UseItemException(msg);
			} else{
				GameItem needItem = ObjectAccessor.createGameItem(2395);
				String msg = MessageFormat.format("坐骑栏已满\n请使用<cff0000>{0}</c>",needItem.template.name);
				throw new UseItemException(msg);
			}
		} else {
			throw new UseItemException(peony.Messages.STRING_00985);
		}
	}
	
	public void getHorseAndRide(Unit source, GameItem item, long useTime) throws UseItemException {
		Player p = (Player) source;
		if (source.type == GameObject.TYPE_PLAYER) {
			Horse h = createHorse(source, item, useTime);
			p.horseRide(h.instanceId, 0, -1);
//			ChatService chatService = Server.server.getServiceRegistry().getChatService();
//			chatService.sendPrivateShout(p.id, 0x0000ff, 6000, p.faction, peony.Messages.STRING_00986);
		}
	}
	
	public int getHorseImageId(){
		return this.imageId;
	}

	public boolean isAsync(){
		return false;
	}
	
	public boolean needRemove() {
		return false;
	}
}
