package peony.game;

import java.util.Random;

import org.apache.log4j.Logger;

import peony.game.buff.Buff;
import peony.game.skill.Skill;
import peony.net.Packet;
import peony.service.duel.DuelInstance;
import peony.service.feast.FeastInstance;
import peony.service.feast.FeastInstanceService;
import peony.service.pluginstance.LanternFestivalService;
import peony.service.pluginstance.MayDayFestivalService;
import peony.service.tong.TongMember;
import peony.service.tong.TongService;
import peony.service.tong.TongSkill12;
import peony.service.tong.TongSkill6;
import peony.service.tong.battle.TongBattleFieldInstance;
import peony.service.towerdefend.TowerDefendInstance;

/**
 * 角色复活选项
 * @author Jeffrey
 *
 */
public class ReliveOption {
	
	private static final Logger log = Logger.getLogger(ReliveOption.class);
	
	public static final int NORMAL = 0; //释放
	public static final int SKILL_ACTIVE = 1; //技能主动复活，被别人的技能复活
	public static final int SKILL_PASSIVE = 2; //技能被动复活，被自己的技能复活
	public static final int ITEM = 3;

	
	protected static int ids = 0;
	public int type;
	public int id;
	public String msg; //发给客户端的复活选项
	public int imageId; //客户端复活时的动画Id
	public int mapId; //复活时的地图Id
	public int x, y; //复活时的x,y坐标
	
	public CombatContext context; //主动复活的时候需要这东西
	
	public Skill skill; //被动复活技能，比如幽冥重生
	
	public ReliveOption(int type,String msg, int imageId, int mapId, int x, int y) {
		id = ids++;
		this.type = type;
		this.msg = msg;
		this.imageId = imageId;
		this.mapId = mapId;
		this.x = x;
		this.y = y;
	}
	
	public boolean merge(ReliveOption option){
		if(type==SKILL_ACTIVE&&option.type==SKILL_ACTIVE){  //如果都是主动复活技能，并且后来的等级大于原来的等级，那么merge
			if(context.skill.getLevel()<option.context.skill.getLevel()){
				context = option.context;
				msg = option.msg;
				return true;
			}
		}
		return false;
	}
	
	public void relive(Player p){
		if(type==NORMAL){
			int[] relivePoint = {mapId,x,y};
			try {
				if(p.map.map!=null){
					if(p.map.map.instance!=null&&!(p.map.map.instance instanceof TongBattleFieldInstance)&&
							!(p.map.map.instance instanceof DuelInstance) && !(p.map.map.instance instanceof TowerDefendInstance)){
						relivePoint = p.map.map.getRelivePoint(p.faction);
					} else if(p.map.map.getId() == LanternFestivalService.LANTERN_MAP){//处理元宵活动死亡复活
						LanternFestivalService s = Server.server.getServiceRegistry().getLanternFestivalService();
						relivePoint = s.relivePoint(p);
					} else if(p.map.map.getId() == MayDayFestivalService.MAYDAY_MAP){//处理元宵活动死亡复活
						MayDayFestivalService s = Server.server.getServiceRegistry().getMayDayFestivalService();
						relivePoint = s.relivePoint(p);
					} else if(mapId == MayDayFestivalService.MAYDAY_MAP && p.map.id!=mapId){
						relivePoint = new int[]{p.map.id,p.x,p.y};
					} 
				}
				Server.server.getWorld().addPlayerToMap(p, relivePoint[0], relivePoint[1], relivePoint[2],false);
				p.relive(p.maxhp / 2, p.maxmp / 2);
			} catch (VMapException e) { //不应该出现
				log.error(e,e);
			}
			Packet pt = new Packet(OpCode.RELIVE_SERVER);
			pt.putInt(p.instanceId);
			pt.putInt(p.map.map.getId());
			pt.putInt(p.getVMap().getInstanceId());
//			pt.putInt(relivePoint[1]);
//			pt.putInt(relivePoint[2]);
			pt.putInt(p.x);
			pt.putInt(p.y);
			pt.putInt(imageId);
			p.broadcast(pt, p,null, false,true,false);
//			p.mapCell.broadcastWithRelationCells(null, pt);
			p.send(pt); //因为player刚被加入新地图，还没有在ready状态，收不到此信息
		}else if(type==SKILL_ACTIVE){
			try {
				if(p.getVMap().getId() == FeastInstanceService.MAPID){
					mapId = FeastInstanceService.MAPID;
					x = Server.server.getServiceRegistry().getFeastInstanceService().pos[0];
					y = Server.server.getServiceRegistry().getFeastInstanceService().pos[1];
				}
				if(mapId == MayDayFestivalService.MAYDAY_MAP){//处理五一地图活动
					if(p.getVMap().getId()!=MayDayFestivalService.MAYDAY_MAP || MayDayFestivalService.gameOver(p)){
						mapId = MayDayFestivalService.MAYDAY_OUTMAP;
						x = MayDayFestivalService.MAYDAY_OUTPOINT[0];
						y = MayDayFestivalService.MAYDAY_OUTPOINT[1];
					}
				}
				Server.server.getWorld().addPlayerToMap(p, mapId, x, y,false);
				((CombatEffect)context.skill).finished(context, false);
			} catch (VMapException e) { //不应该出现
				log.error(e,e);
			}
			Packet pt = new Packet(OpCode.RELIVE_SERVER);
			pt.putInt(p.instanceId);
			pt.putInt(p.map.map.getId());
			pt.putInt(p.getVMap().getInstanceId());
			pt.putInt(x);
			pt.putInt(y);
			pt.putInt(imageId);
			p.broadcast(pt, p,null,false,true,false);
//			p.mapCell.broadcastWithRelationCells(null, pt);
			p.send(pt); //因为player刚被加入新地图，还没有在ready状态，收不到此信息
		}else if(type==SKILL_PASSIVE){
			try {
				if(p.getVMap().getId() == FeastInstanceService.MAPID){
					mapId = FeastInstanceService.MAPID;
					x = Server.server.getServiceRegistry().getFeastInstanceService().pos[0];
					y = Server.server.getServiceRegistry().getFeastInstanceService().pos[1];
				}
				if(mapId == MayDayFestivalService.MAYDAY_MAP){//处理五一地图活动
					if(p.getVMap().getId()!=MayDayFestivalService.MAYDAY_MAP || MayDayFestivalService.gameOver(p)){
						mapId = MayDayFestivalService.MAYDAY_OUTMAP;
						x = MayDayFestivalService.MAYDAY_OUTPOINT[0];
						y = MayDayFestivalService.MAYDAY_OUTPOINT[1];
					}
				}
				Server.server.getWorld().addPlayerToMap(p, mapId, x, y,false);
				CombatContext context = new CombatContext(p,p,skill);
				CombatEffect effect = (CombatEffect)skill;
				effect.preHit(context, true);
				effect.finished(context, true);
				p.setCoolDown(skill.getCDGroup(), Time.currTime, Time.currTime+skill.getCDTime(p));
				p.relive(p.hp, p.mp);
			} catch (VMapException e) { //不应该出现
				log.error(e,e);
			}
			Packet pt = new Packet(OpCode.RELIVE_SERVER);
			pt.putInt(p.instanceId);
			pt.putInt(p.map.map.getId());
			pt.putInt(p.getVMap().getInstanceId());
			pt.putInt(x);
			pt.putInt(y);
			pt.putInt(imageId);
			p.broadcast(pt, p,null,false,true,false);
//			p.mapCell.broadcastWithRelationCells(null, pt);
			p.send(pt); //因为player刚被加入新地图，还没有在ready状态，收不到此信息
		}else if(type==ITEM){
			PlayerTransaction tx = p.newTransaction("RLV");
			GameItem item = p.bag.removeGameItem(ItemUtil.ITEM_RELIVE, -1, 1, tx, true);
			tx.commit();
			try {
				Server.server.getWorld().addPlayerToMap(p, p.map.id, p.x, p.y,false);
			} catch (VMapException e) {
				e.printStackTrace();
			}
			int hp = Math.round(p.maxhp * 0.75f);
			int mp = Math.round(p.maxmp * 0.75f);
			
			//军团专属科技  还魂大师
			TongService ts = Server.server.getServiceRegistry().getTongService();
			TongMember tm = ts.getPlayerInfo(p.id);
			if(tm!=null && tm.skills != null && tm.skills.get(12)!=null){
				TongSkill12 tskill = (TongSkill12)tm.skills.get(12);
				if(tskill.level > 0){
					int ratios = tskill.getRatios();
					hp = Math.round(p.maxhp * ratios / 100);
					mp = Math.round(p.maxmp * ratios / 100);
				}
			}
			boolean tenStar = false;
			//特殊处理十星buff效果
			if(p.buffs!=null){
				Buff buff = p.buffs.getBuffByID(582);
				if(buff != null){
					tenStar = true;
				}
			}
			if(!tenStar)
			     p.relive(hp,mp);
			else
				 p.relive2(hp, mp);
			if (item != null) {
				p.setCoolDown(106, Time.currTime,
						Time.currTime + 180*1000);
			}
			Packet pt = new Packet(OpCode.RELIVE_SERVER);
			pt.putInt(p.instanceId);
			pt.putInt(p.map.map.getId());
			pt.putInt(p.getVMap().getInstanceId());
			pt.putInt(p.x);
			pt.putInt(p.y);
			pt.putInt(imageId);
			p.broadcast(pt, p,null,false,true,false);
			p.send(pt);
		}
		p.processCardBuff();
		//处理随从
		if(p!=null && p.attendant!=null){
			p.attendant.follow();
		}
	}
}
