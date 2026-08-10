package peony.game.itemeffect;

import peony.game.GameItem;
import peony.game.Horse;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Unit;
import peony.game.UseItemException;
import peony.game.buff.BuffUtil;
import peony.game.changed.ChangedItem;
import peony.game.nation.CandidateService;
import peony.net.Packet;

public class KingItemEffect implements ItemEffect {

	public static int KINGITEM_WEAPON = 4358;
	public static int KINGITEM_HORSE = 4359;
	public static String PROPERTY_USEKINGWEAPEN = "usekingweapon";
	public static String PROPERTY_USEKINGHORSE = "usekinghorse";
	public static int KINGWEAPON_BUFFID = 592;
	public static int KINGWEAPON_BUFFID2 = 594;

	public boolean isAsync() {
		return false;
	}

	public void use(Unit source, GameItem item, Unit target,
			PlayerTransaction tx) throws UseItemException {
		if (!ItemUtil.checkUseTarget(source, item, target)) {
			throw new UseItemException(peony.Messages.STRING_00014);
		}
		Player player = (Player) source;
		if (player != null) {
			if (player.isKing() != 1) {
				throw new UseItemException("只有国公才能使用");
			}
			if (item.template.id == KINGITEM_WEAPON) {
				if (player.pool.getInt(PROPERTY_USEKINGWEAPEN, 0) == 0) {
					player.buffs.addBuff(BuffUtil.createSuiteBuff(KINGWEAPON_BUFFID, 1));
					player.pool.setInt(PROPERTY_USEKINGWEAPEN, 1);
					player.message(-1, "效果生效", -1, -1);
				} else {
					Packet pt = new Packet(OpCode.OPENUI_SERVER);
					pt.putString("ui_npc_dialog");
					pt.putString("KING_WEAPON|"+item.template.name);
					player.send(pt); 
				}
			} else if (item.template.id == KINGITEM_HORSE) {
				if (player.pool.getInt(PROPERTY_USEKINGHORSE, 0) == 0) {
					horseChangeImage(player, true);
					player.buffs.addBuff(BuffUtil.createBuff(KINGWEAPON_BUFFID2, 1, player, player, 0));
					player.pool.setInt(PROPERTY_USEKINGHORSE, 1);
					player.message(-1, "效果生效", -1, -1);
				} else {
					Packet pt = new Packet(OpCode.OPENUI_SERVER);
					pt.putString("ui_npc_dialog");
					pt.putString("KING_HORSE|"+item.template.name);
					player.send(pt); 
				}
			}
		}
	}

	public static void horseChangeImage(Player player, boolean change) {
		GameItem gameItem = null;
		if (change) {
				gameItem = ObjectAccessor
						.createGameItem(CandidateService.horseItems[player.clazz]);
		} 
		if (gameItem != null) {
			HorseItemEffect effect = (HorseItemEffect) gameItem.template.useType.effect;
			if (effect != null) {
				int imageId = effect.getHorseImageId();
				if(player.horseBag.horses!=null){
					for(Horse h : player.horseBag.horses){
						if(h!=null && !CandidateService.isKingHorse(h.itemId)){
							LogUtil.logKingItemEffectBefore(player, h);
							h.imageIdChange = h.imageId;
							h.imageId = imageId;
							h.itemIdChange = gameItem.template.id;
							LogUtil.logKingItemEffect(player, h);
							if(player.horse!=null && player.horse.instanceId == h.instanceId){
						         h. addStringPropertyChangedItem(player.changed, ChangedItem.HORSE_IMAGE, String.valueOf(imageId)+"#"+String.valueOf(h.imageId), false);
						         player.ride();
							}
						}
					}
					Packet pt = new Packet(OpCode.HORSE_BAG_SERVER);
					pt.putInt(-1);
					pt.put(player.horseBag.toClientBytes());
					player.send(pt);
				}
			}
		}
	}
	
	public static void kingAddHorse(Player player,Horse h){
		GameItem gameItem = ObjectAccessor
						.createGameItem(CandidateService.horseItems[player.clazz]);
	
		if (gameItem != null) {
			HorseItemEffect effect = (HorseItemEffect) gameItem.template.useType.effect;
			if (effect != null) {
				int imageId = effect.getHorseImageId();
				if(h!=null){
					h.imageIdChange = h.imageId;
					h.imageId = imageId;
					h.itemIdChange = gameItem.template.id;
				}
			}
		}		
	}
	
	public static void notKingResetImage(Player player){
		GameItem gameItem = ObjectAccessor
						.createGameItem(CandidateService.horseItems[player.clazz]);
		if (gameItem != null) {
			HorseItemEffect effect = (HorseItemEffect) gameItem.template.useType.effect;
			if (effect != null) {
				int imageId = effect.getHorseImageId();
				if(player.horseBag.horses!=null){
					for(Horse h : player.horseBag.horses){
						if(h!=null && !CandidateService.isKingHorse(h.itemId)){
							LogUtil.logKingItemEffectBefore(player, h);
							if(h.imageId == imageId){
								GameItem item = ObjectAccessor.createGameItem(h.itemId);
								if(item!=null){
									HorseItemEffect effect2 = (HorseItemEffect) item.template.useType.effect;
									if (effect2 != null) {
										int imId = effect2.getHorseImageId();
										if(h.imageIdChange == imageId){
											h.imageIdChange = imId;
										}
										if(h.imageIdChange!=-1){
										    h.imageId = h.imageIdChange;
										    if(h.imageIdChange == imId){
										    	h.imageIdChange = -1;
										    }else{
										    	h.imageIdChange = imId;
										    }
										}
									}
									LogUtil.logKingItemEffect(player, h);
								}
							}
						}
					}
				}
			}
		}
	}
	
	public static void horseResetImage(Player player){
		if(player.horseBag.horses!=null){
			for(Horse h : player.horseBag.horses){
				if(h!=null && !CandidateService.isKingHorse(h.itemId)){
					LogUtil.logKingItemEffectBefore(player, h);
					int[] BAIMA = {3985,3986,3987,3988};
					if(h.itemId == 2509){
						h.itemId = BAIMA[player.clazz];
					}
					GameItem gameItem = ObjectAccessor.createGameItem(h.itemId);
					if(gameItem!=null){
						HorseItemEffect effect = (HorseItemEffect) gameItem.template.useType.effect;
						if (effect != null) {
							int imageId = effect.getHorseImageId();
							if(h.imageIdChange == 13){
								h.imageIdChange = imageId;
							}
							if(h.imageIdChange!=-1){
							    h.imageId = h.imageIdChange;
							    if(h.imageIdChange == imageId){
							    	h.imageIdChange = -1;
							    }else{
							    	h.imageIdChange = imageId;
							    }
							}else{
								h.imageId = imageId;
							}
							LogUtil.logKingItemEffect(player, h);
							if(player.horse!=null && player.horse.instanceId == h.instanceId){
						         h.addStringPropertyChangedItem(player.changed, ChangedItem.HORSE_IMAGE, String.valueOf(-1)+"#"+String.valueOf(h.imageId), false);
						         player.ride();
							}
						}
					}
				}
			}
			Packet pt = new Packet(OpCode.HORSE_BAG_SERVER);
			pt.putInt(-1);
			pt.put(player.horseBag.toClientBytes());
			player.send(pt);
		}
	}
	
	public static void notKing(Player player){
		if(player!=null){
			if(player.pool.getInt(PROPERTY_USEKINGWEAPEN, 0) == 1){
			     player.buffs.removeBuff(KINGWEAPON_BUFFID);
			}
			player.pool.remove(PROPERTY_USEKINGWEAPEN);
			if(player.pool.getInt(PROPERTY_USEKINGHORSE, 0)==1){
			    horseResetImage(player);
			    player.buffs.removeBuff(KINGWEAPON_BUFFID2);
			}else{
				notKingResetImage(player);
			}
			player.pool.remove(PROPERTY_USEKINGHORSE);
		}
	}
	
	public static void isKing(Player player){
		if(player.pool.getInt(PROPERTY_USEKINGWEAPEN, 0)==1){
			player.buffs.addBuff(BuffUtil.createSuiteBuff(
					KINGWEAPON_BUFFID, 1));
		}
		if(player.pool.getInt(PROPERTY_USEKINGHORSE, 0)==1){
	        player.buffs.addBuff(BuffUtil.createBuff(KINGWEAPON_BUFFID2, 1, player, player, 0));
		}
	}
	
	public boolean needRemove() {
		return false;
	}
}
