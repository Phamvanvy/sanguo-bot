package peony.game;

import java.util.ArrayList;
import java.util.List;

import peony.game.skill.Skill;
import peony.service.player.KillPlayerService;

public abstract class BaseDieCallback implements DieCallback {
	
	protected int[] canCopyEqupMap = {
			816,832,833,1024,896,880,1056,864,944,1040,752,912,928,849,1072,1088,1104,1152
			,1232,1248,1361,1376
	};

	protected void processReliveOptions(Player player,int[] relivePoint){
		Skill reliveSkill = null;
		if(player.warState==Player.PVEPVPSTATE){
			player.setWarState(Player.PVESTATE);
		}
//		player.removePvpFlag();
		for (Skill skill : player.skills.getSkills()) {
			if ((skill.getType() & Skill.TYPE_RELIVE) != 0
					&& (skill.getTargetType() & Skill.TARGET_FLAG_SELF) != 0
					&& skill.getLevel() != 0
					&& !player.coolDowns.contains(skill.getCDGroup())) { // 被动复活技能
				reliveSkill = skill;
				break;
			}
		}
		player.reliveOptions = new ReliveOptions(Time.currTime + 60 * 1000);
		if(relivePoint==null)
			relivePoint = player.map.map.getRelivePoint(player.faction);
		ReliveOption option = new ReliveOption(ReliveOption.NORMAL, "释放", 14,
				relivePoint[0], relivePoint[1], relivePoint[2]);
		player.reliveOptions.addOption(option, false);
		if (reliveSkill != null) {
			ReliveOption skillReliveOption = new ReliveOption(
					ReliveOption.SKILL_PASSIVE, reliveSkill.getName(), 14,
					player.map.id, player.x, player.y);
			skillReliveOption.skill = reliveSkill;
			player.reliveOptions.addOption(skillReliveOption, false);
		}
		GameItem item = player.bag.getGameItem(ItemUtil.ITEM_RELIVE);
		if(item!=null&&!player.coolDowns.contains(106)){
			ReliveOption itemReliveOption = new ReliveOption(ReliveOption.ITEM,item.template.name,14,player.map.id,player.x,player.y);
			player.reliveOptions.addOption(itemReliveOption, false);
		}
		player.send(player.reliveOptions.getRelivePacket());
	}
	
	protected void processPvpDie(Player player,Unit source){
		// 记录被玩家杀死
		player.dieCause = 1;
		// 所有1分钟内对此玩家造成过仇恨的玩家成为受益人（必须在20码范围内）
		List<Player> benefitPlayers = new ArrayList<Player>();
		int maxWinLevel = 0;
		for (int pid : player.enemyPlayers.keySet()) {
			int t = player.enemyPlayers.get(pid);
			if (t < Time.currTime - 60000) {
				continue;
			}
			Player p = ObjectAccessor.getPlayer(pid);
			if (p == null || !p.inRange(player, 160) || !p.isAlive()) {
				continue;
			}
			benefitPlayers.add(p);
			p.addKillPlayer(player.id); // 每个受益人都获得一个击杀
			if (p.level > maxWinLevel) {
				maxWinLevel = p.level;
			}
		}

		// 计算掉落荣誉，平均分给受益人
		int bsize = benefitPlayers.size();
		int loseCredit = 0;
		if (bsize > 0 && player.credit > 0) {
			int[] credits = getPvpCreditChanged(player, maxWinLevel);
//			int[] credits = CreditUtil.getCredit(maxWinLevel, player.level);
			if (credits[1] < 0) {
				// 扣荣誉和本周荣誉
				loseCredit = credits[1];
				player.setCredit(Math.max(player.credit + credits[1], 0),
						true, "PVP");
				player.setWeekCredit(Math.max(player.weekCredit
						+ credits[1], 0));
			}
			if (credits[0] > 0) {
				// 给受益人加荣誉
				int part1 = credits[0] / bsize;
				int part2 = credits[0] % bsize;
				for (int i = 0; i < bsize; i++) {
					int addValue = part1;
					if (i < part2) {
						addValue++;
					}
					Player p = benefitPlayers.get(i);
					if(p.isBot())
						continue;
					p.setCredit(p.credit + addValue, true, "PVP");
				}
			}
		}
		// 杀人掉装备处理
		boolean canDropEquip = false;
		for(int mapId : canCopyEqupMap){
			if(mapId==player.map.getId())
				canDropEquip = true;
		}
		if(!canDropEquip)
			return;
		KillPlayerService killPlayerService = Server.server.getServiceRegistry().getKillPlayerService();
		int itemId = killPlayerService.getRankPvpDropEquipment(player);
		if(itemId!=-1){
			// 掉装后损失战功乘1.5倍
			player.setCredit(Math.max(player.credit + (int)(loseCredit*0.5f), 0),
					true, "PVPEQU");
			int copyDay = player.pool.getInt(Player.PROPERTY_KILLED_COPYEQUIP_DAY, 0);
			int copyBlueCount = player.pool.getInt(Player.PROPERTY_KILLED_COPYEQUIP_COUNT+"2", 0);
			int copyPurpleCount = player.pool.getInt(Player.PROPERTY_KILLED_COPYEQUIP_COUNT+"3", 0);
			int copyGreenCount = player.pool.getInt(Player.PROPERTY_KILLED_COPYEQUIP_COUNT+"1", 0);
			if(copyDay==Time.day && (copyBlueCount+copyPurpleCount)>=3){
				return;
			}else if(copyDay==Time.day && copyGreenCount>=20){
				return;
			}
			int quality = ObjectAccessor.createGameItem(itemId).template.quality;
			if(copyDay==Time.day){
				player.pool.setInt(Player.PROPERTY_KILLED_COPYEQUIP_COUNT+quality, player.pool.getInt(Player.PROPERTY_KILLED_COPYEQUIP_COUNT+quality, 0)+1);
			}else{
				player.pool.setInt(Player.PROPERTY_KILLED_COPYEQUIP_DAY, Time.day);
				player.pool.setInt(Player.PROPERTY_KILLED_COPYEQUIP_COUNT+quality, 1);
			}
		}
		killPlayerService.rollPvpGainGameItem(benefitPlayers, itemId);
		killPlayerService.rollPvpActivityDrop(benefitPlayers,player);
	}
	
	abstract protected int[] getPvpCreditChanged(Player player,int maxWinLevel);
	
	protected void processPveDie(Player player, Unit source){
		player.dieCause = 2;
	}
}
