package peony.service.enhance;

import java.text.MessageFormat;
import java.util.Arrays;

import org.apache.log4j.Logger;

import peony.common.SyncIbuyCall;
import peony.game.Action;
import peony.game.ChatOption;
import peony.game.EquipmentTemplate;
import peony.game.Equipments;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.Horse;
import peony.game.ItemTemplate;
import peony.game.ItemUtil;
import peony.game.LogUtil;
import peony.game.NoEnoughSpaceException;
import peony.game.NoEnoughValueException;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.TransactionBagGrid;
import peony.game.Unit;
import peony.game.buff.Buff;
import peony.game.buff.BuffUtil;
import peony.game.changed.BindChangedItem;
import peony.game.changed.SkillChangedItem;
import peony.game.chat.ChatMessage;
import peony.game.chat.ChatService;
import peony.game.chat.ItemChatAttachment;
import peony.game.itemenhance.ItemEnhance;
import peony.game.mail.MailService;
import peony.game.suite.SuiteEffect;
import peony.game.suite.SuiteEffects;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.ServiceEvent;
import peony.service.shop.NoItemShopBuy;
import peony.service.shop.ShopService;

import com.pip.sanguo.data.item.Item;

public class EquipLevelUp_ProcessCall extends SyncIbuyCall{
	private static final Logger log = Logger.getLogger(EquipLevelUp_ProcessCall.class);
	Player p = null;
	int serial;
	int itemId ;
	int instanceId ;
	int levelUpType;//升级类型 0无 1战功 2元宝 3材料 4材料不足以元宝补齐
	public static final int TYPE_CREDIT=1;//战功
	public static final int TYPE_IMONEY=2;//元宝
	public static final int TYPE_ITEM=3;//材料
	public static final int TYPE_ITEMWITHIMONEY=4;//材料不走元宝补齐
	
	
	boolean isSaveProp=false;//是否保留属性升级0不保留1保留
	GameItem gameItem;
	Object owner;

	public EquipLevelUp_ProcessCall(ClientSession session,Packet packet) {
		super(session, packet);
		p = (Player) session.getClient();
		serial = packet.getInt();
		itemId = packet.getInt();
		instanceId = packet.getInt();
		levelUpType = packet.get();
		byte savePropIndex=packet.get();
		isSaveProp=(savePropIndex==0?false:true);
	}


	public void process(Object[] o) {
		addToClientSession();
	}

	public void procssFail(Object[] o) {
		error(peony.Messages.STRING_00405);
		addToClientSession();
	}

	public void callFinish() throws Exception {
//		if(success){
//			
//		}else{
//			ErrorHandler.sendErrorMessage(session, serial,
//					OpCode.EQUIP_LEVELUP_CLIENT, errorMessage);
//		}
		
	}
	
	public void run() {
		if (p != null) {
			synchronized (p.ref()) {
				if(!EquipLevelUpInfoCall.canLevelUp){
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.EQUIP_LEVELUP_CLIENT, "此功能暂未开放，敬请期待！");
				}
				Object[] os = ItemUtil.findPlayerEquipment(p, itemId, instanceId);
				if (os != null) {
					gameItem = (GameItem) os[0];
					owner = os[1];
					if (gameItem == null) {
						ErrorHandler.sendErrorMessage(session, serial,
								OpCode.EQUIP_LEVELUP_CLIENT, peony.Messages.STRING_01352);
						return;
					}
					if (!gameItem.template.isEquipment()) {
						ErrorHandler.sendErrorMessage(session, serial,
								OpCode.EQUIP_LEVELUP_CLIENT, peony.Messages.STRING_01353);
						return;
					}
					if(gameItem.template.equipment.limitType==0||gameItem.template.equipment.limitType==1)//暂时屏蔽马装
					if((gameItem.template.equipment.clazz==-1&&!EquipLevelUpInfoCall.isOldEquip(itemId, p.clazz))||(gameItem.template.equipment.clazz!=-1&&gameItem.template.equipment.clazz!=p.clazz)){
						ErrorHandler.sendErrorMessage(session, serial,
								OpCode.EQUIP_LEVELUP_CLIENT, MessageFormat.format("请选择{0}的装备升级！", Unit.CLASS_NAME[p.clazz]));
						return;
					}
					if(!canLevelUp(gameItem)){
						ErrorHandler.sendErrorMessage(session, serial,
								OpCode.EQUIP_LEVELUP_CLIENT, "此装备不允许升级！");
						return;
					}
					log.info("[EQUIPLEVELUPPRE]PLAYERACC["+p.accountId+"]ID["+p.id+"]OLDEQUIP["+LogUtil.getGameItemString(gameItem, 1)+"]");
					if(gameItem.template.equipment.equ.equipLevelUpPriceType==0&&levelUpType==0){//不用材料直接升级
						//处理装备升级
						if(gameItem.template.equipment.equ.equipLevelUpSavePropType==Item.SAVEEQUIPPROP_TYPE_IMONEY&&isSaveProp){
							levelUp(true,false);
						}else{
							levelUp(false,false);
						}
					}else if(gameItem.template.equipment.equ.equipLevelUpPriceType == Item.EQUIPLEVELUP_TYPE_CREDIT&&levelUpType==TYPE_CREDIT){//战功
						levelUpByCredit();
					}else if(gameItem.template.equipment.equ.equipLevelUpPriceType == Item.EQUIPLEVELUP_TYPE_IMONEY&&levelUpType==TYPE_IMONEY){//元宝
						levelUpByIMoney();
					}else if(gameItem.template.equipment.equ.equipLevelUpPriceType == Item.EQUIPLEVELUP_TYPE_MATERIAL){//材料
						isSaveProp=false;//只要是材料升级时就不用花费额外的保存属性
						if(levelUpType==TYPE_ITEM){//用材料升级
							levelUpByMaterail_IsEnough();
						}else if(levelUpType==TYPE_IMONEY){//用元宝升级
							levelUpByMaterial_IMoney();
						}else if(levelUpType==TYPE_ITEMWITHIMONEY){//材料不足
							levelUpByMaterail_NotEnough();
						}
					}else{
						ErrorHandler.sendErrorMessage(session, serial,
								OpCode.EQUIP_LEVELUP_CLIENT, "此装备不允许升级，敬请期待！");
						return;
					}
				} else {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.EQUIP_LEVELUP_CLIENT, peony.Messages.STRING_00173);
				}
			}
		}
	}
	
	/***
	 * 材料不够时用元宝补充
	 */
	public void levelUpByMaterail_NotEnough(){
		if(gameItem!=null&&gameItem.template.equipment.equ.equipLevelUpPriceType!=Item.EQUIPLEVELUP_TYPE_MATERIAL){
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.EQUIP_LEVELUP_CLIENT, "此装备不能使用材料升级！");
			return;
		}
		int hadCount=p.bag.getGameItemCount(EquipLevelUpInfoCall.LEVELUP_USEDITEMID);//背包里已有数量 
		if(hadCount<gameItem.template.equipment.equ.equipLevelUpPriceNum){
			int needCount=gameItem.template.equipment.equ.equipLevelUpPriceNum-p.bag.getGameItemCount(EquipLevelUpInfoCall.LEVELUP_USEDITEMID);
//			ShopService shopService=Server.server.getServiceRegistry().getShopService();
//			int itemMoney=Math.round(shopService.getItemPrice(EquipLevelUpInfoCall.LEVELUP_PRICEIMONEYID))/36;
//			int money=itemMoney*needCount;
			log.info("[EQUIPLEVELUPNOTENOUGHMATERAIL]IMONEYPRE["+p.getIMoney()+"]");
			int shopId = Server.server.getServiceRegistry().getShopService().getShopByItemId(EquipLevelUpInfoCall.LEVELUP_PRICEIMONEYID).id;
			try {
				waitBuy(p, 0, shopId, EquipLevelUpInfoCall.LEVELUP_PRICEIMONEYID, needCount, this);
			} catch (Exception e) {
				return;
			}
			log.info("[EQUIPLEVELUPNOTENOUGHMATERAIL]IMONEYAFTER["+p.getIMoney()+"]");
			if(hadCount>0){
				GameItem item=p.bag.getGameItem(EquipLevelUpInfoCall.LEVELUP_USEDITEMID);
				PlayerTransaction tx = p.newTransaction("EQUIPLEVELUPBYMATERAILUSEITEMCOUNT");
				GameItem item0  = p.bag.removeGameItem(item.template.id, item.instanceId, hadCount, tx, false);
				if(item0!=null)
					tx.commit();
				else{
					tx.rollback();
					return;
				}
			}
			//2.处理装备升级
			levelUp(isSaveProp,true);
			addToClientSession();
		}
	}
	
	/***
	 * 材料升级
	 */
	public void levelUpByMaterail_IsEnough(){
		if(gameItem!=null&&gameItem.template.equipment.equ.equipLevelUpPriceType!=Item.EQUIPLEVELUP_TYPE_MATERIAL){
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.EQUIP_LEVELUP_CLIENT, "此装备不能使用材料升级！");
			return;
		}
		int needItemCount=p.bag.getGameItemCount(EquipLevelUpInfoCall.LEVELUP_USEDITEMID);
		if(needItemCount<gameItem.template.equipment.equ.equipLevelUpPriceNum){
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.EQUIP_LEVELUP_CLIENT, "材料数量不足以用来装备升级！");
			return;
		}
		GameItem item=p.bag.getGameItem(EquipLevelUpInfoCall.LEVELUP_USEDITEMID);
		PlayerTransaction tx = p.newTransaction("EQUIPLEVELUPBYMATERAILUSEITEMCOUNT");
		GameItem item0  = p.bag.removeGameItem(item.template.id, item.instanceId, gameItem.template.equipment.equ.equipLevelUpPriceNum, tx, false);
		if(item0!=null)
			tx.commit();
		else{
			tx.rollback();
			return;
		}
		levelUp(isSaveProp,true);
		addToClientSession();
	}
	/***
	 * 元宝升级，默认保留属性
	 */
	public void levelUpByIMoney(){
		if(gameItem!=null&&gameItem.template.equipment.equ.equipLevelUpPriceType!=Item.EQUIPLEVELUP_TYPE_IMONEY){
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.EQUIPMENT_LEVELUP_ITEMINFO_CLIENT, "此装备不能使用元宝升级！");
			return;
		}
		int priceIMoney=gameItem.template.equipment.equ.equipLevelUpPriceNum;//编辑器设置为元宝升级时直接读取花费的值
		int shopId = Server.server.getServiceRegistry().getShopService().getShopByItemId(NoItemShopBuy.YIBAIYUANBAO).id;
//		ShopService shopService=Server.server.getServiceRegistry().getShopService();
//		int itemMoney=Math.round(shopService.getItemPrice(NoItemShopBuy.SHIYUANBAO))/36;//每个代扣的价格
		try {
			waitBuy(p, 0, shopId, NoItemShopBuy.SHIYUANBAO, priceIMoney/10, this);
		} catch (Exception e) {
			return;
		}
		levelUp(false,true);
		addToClientSession();
	}
	 
	/***
	 * 材料升级时选元宝升级
	 */
	public void levelUpByMaterial_IMoney(){
		if(gameItem!=null&&gameItem.template.equipment.equ.equipLevelUpPriceType!=Item.EQUIPLEVELUP_TYPE_IMONEY&&
				gameItem.template.equipment.equ.equipLevelUpPriceType!=Item.EQUIPLEVELUP_TYPE_MATERIAL){
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.EQUIPMENT_LEVELUP_ITEMINFO_CLIENT, "此装备不能使用元宝升级！");
			return;
		}
		boolean byMaterial=false;
		int priceIMoney=gameItem.template.equipment.equ.equipLevelUpPriceNum;
//		ShopService shopService=Server.server.getServiceRegistry().getShopService();
//		int itemMoney=Math.round(shopService.getItemPrice(EquipLevelUpInfoCall.LEVELUP_PRICEIMONEYID))/36;
//		int imoney=itemMoney*needCount;
		byMaterial=true;
		int shopId = Server.server.getServiceRegistry().getShopService().getShopByItemId(EquipLevelUpInfoCall.LEVELUP_PRICEIMONEYID).id;
		try {
			waitBuy(p, 0, shopId, EquipLevelUpInfoCall.LEVELUP_PRICEIMONEYID, priceIMoney, this);
		} catch (Exception e) {
			return;
		}
		//2.处理装备升级
		levelUp(isSaveProp,byMaterial);
		addToClientSession();
	}
	
	
	/***
	 * 战功升级
	 */
	public void levelUpByCredit(){
		//1.减少相应的战功
		int credit=gameItem.template.equipment.equ.equipLevelUpPriceNum;
		PlayerTransaction tx = p.newTransaction("EQUIPLEVELUPCREDITCHANGE");
		if(p.getCredit()<credit){
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.EQUIP_LEVELUP_CLIENT, peony.Messages.STRING_00432);
			return;
		}
		if(isSaveProp){//判断选择保留属性
			if(gameItem.object!=null){
				ItemEnhance enhance=(ItemEnhance)gameItem.object;
				byte[] itemSrc=enhance.toDBBytes();
				byte[] itemDes=(new ItemEnhance()).toDBBytes();
				if(!Arrays.equals(itemSrc, itemDes)){//有变化时才扣除元宝
					ShopService shopService=Server.server.getServiceRegistry().getShopService();
					int itemMoney=Math.round(shopService.getItemPrice(EquipLevelUpInfoCall.LEVELUP_PRICE_SAVEPROP)/36);
					if(p.getIMoney()<itemMoney){//没有足够元宝时
						ErrorHandler.sendErrorMessage(session, serial,
								OpCode.EQUIP_LEVELUP_CLIENT, peony.Messages.STRING_00924);
						return;
					}
				}
			}
		}
		try {
			p.decCredit(credit, tx, false);
			tx.commit();
		} catch (NoEnoughValueException e) {
			tx.rollback();
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.EQUIP_LEVELUP_CLIENT, peony.Messages.STRING_00432);
			return;
		}
		//2.处理装备升级
		levelUp(isSaveProp,false);
	}
	
	/**
	 * 处理装备升级
	 * @param saveProperties//是否花元宝保存属性
	 *saveWithNoMoney //免费保留属性
	 */
	public void levelUp(boolean saveProperties,boolean saveWithNoMoney){
		GameItem nextLevelItem=ObjectAccessor.createGameItem(gameItem.template.equipment.equ.nextEquipId);
		if(nextLevelItem!=null){
			if(nextLevelItem.bindInstance == -1){//未绑定
				nextLevelItem.bindInstance = 0;
		        BindChangedItem item = new BindChangedItem(nextLevelItem);
		        p.changed.addChangedItem(item);
		        log.info("[BINDNEWEQUIP]PLAYERACC["+p.accountId+"]ID["+p.id+"]NEWQUIPINFO["+LogUtil.getGameItemString(nextLevelItem, 1)+"]");
			}
			if(gameItem.object!=null&&saveProperties){//原装备有属性变化
				ItemEnhance enhance=(ItemEnhance)gameItem.object;
				byte[] itemSrc=enhance.toDBBytes();
				byte[] itemDes=(new ItemEnhance()).toDBBytes();
				if(!Arrays.equals(itemSrc, itemDes)){//有变化时才扣除元宝
					int shopId = Server.server.getServiceRegistry().getShopService().getShopByItemId(EquipLevelUpInfoCall.LEVELUP_PRICE_SAVEPROP).id;
					try {
						waitBuy(p, 0, shopId, EquipLevelUpInfoCall.LEVELUP_PRICE_SAVEPROP, 1, this);
					} catch (Exception e) {
						return;
					}
				}
				if(nextLevelItem.object==null){
					nextLevelItem.object=new ItemEnhance();
				}
				ItemEnhance itemEnhSrc= (ItemEnhance) gameItem.object.clone();
				nextLevelItem.object=itemEnhSrc;
			}else if(gameItem.object!=null&&!saveWithNoMoney){//不保留属性时自动摘除宝石放背包（背包满发飞鸽）
				ItemEnhance itemEnhSrc=(ItemEnhance)gameItem.object.clone();
				int totalHoles = gameItem.template.equipment.initHole + itemEnhSrc.getAddHole();
				int jewelCount = itemEnhSrc.getJewelCount();
				int count0=0;
				int count1=0;
				for(int i=0;i<totalHoles;i++){
					if(i>=jewelCount)
						break;
					int jewelItemId = itemEnhSrc.getJewelID(i);
					int hole = itemEnhSrc.getJewelHole(i);
					GameItem  item=ObjectAccessor.createGameItem(jewelItemId);
					int count = 0;//itemEnhSrc.jewelUpgrades[2*hole+1]+1;
					for(int j=0;j<itemEnhSrc.jewelUpgrades.length;j+=2){
						int holeIndex=itemEnhSrc.jewelUpgrades[j];
						int jewelCountT=itemEnhSrc.jewelUpgrades[j+1];
						if(hole==holeIndex){
							count=jewelCountT+1;
						}
					}
					if(item != null&&count>0){
						PlayerTransaction tx2 = p.newTransaction("EQUIPLEVELUPADDJEWELTOBAG");
						try {
							p.bag.addGameItemComplete(item, count, tx2, true);
							tx2.commit();
							if(count0==0){
								count0++;
								ChatService chat=Server.server.getServiceRegistry().getChatService();
								chat.sendPrivateMessage(p.id, MessageFormat.format("您的{0}装备上的宝石已经放到背包里，请查收！", gameItem.template.name));
							}
						} catch (NoEnoughSpaceException e) {
							tx2.rollback();
							if(count1==0){
								count1++;
								ChatService chat=Server.server.getServiceRegistry().getChatService();
								chat.sendPrivateMessage(p.id, MessageFormat.format("您的{0}装备上的宝石已经放到飞鸽里，请查收！", gameItem.template.name));
							}
							MailService service = Server.server.getServiceRegistry().getMailService();
							service.sendSystemMail(p.id, peony.Messages.STRING_00004, "装备升级摘除宝石", "您刚才升级装备没有保留属性，装备上的宝石已经给你返还回来，请提取附件。", 0, item, count, "EQUIPLEVELUPADDJEWELTOBAG");
						}
						log.info("[EQUIPLEVELUPNOSAVEPROPWITHMONEY]JEWEL["+jewelItemId+"]COUNT["+count+"]");
					}
				}
				nextLevelItem.object=null;
			}
			if(gameItem.object!=null&&saveWithNoMoney){//免费保留属性
				if(nextLevelItem.object==null){
					nextLevelItem.object=new ItemEnhance();
				}
				ItemEnhance itemEnhSrc= (ItemEnhance) gameItem.object.clone();
				nextLevelItem.object=itemEnhSrc;
				log.info("[EQUIPLEVELUPSAVEPROPFREE]GAMEITEMOBJECT["+LogUtil.getGameItemString(nextLevelItem, 1)+"]");
			}
			//3.判断背包还是穿在身上
			if(p.bag.getGameItem(-1, itemId, instanceId)!=null){//如果在背包里的处理方法
				PlayerTransaction txRemoveItem = p.newTransaction("EQUIPLEVELUPREMOVEOLDEQUIP");
				GameItem item = p.bag.removeGameItem(itemId, instanceId, 1, txRemoveItem, false);
				if(item!=null)
					txRemoveItem.commit();
				else
					txRemoveItem.rollback();
				PlayerTransaction txEquipLevelUpAdd = p.newTransaction("EQUIPLEVELUPADDNEWEQUIP");	
				try {
					p.bag.addGameItemComplete(nextLevelItem, 1, txEquipLevelUpAdd, true);
					txEquipLevelUpAdd.commit();
				} catch (NoEnoughSpaceException e) {
					txEquipLevelUpAdd.rollback();
					Server.server.getServiceRegistry().getMailService().sendSystemMail(
							p.id, peony.Messages.STRING_00004, peony.Messages.STRING_01346, "背包已满，升级后的装备发送到您的飞鸽，请及时查收！", 0, nextLevelItem, 1, "EQUIPLEVELUPADDNEWEQUIPBYEMAIL");
				}
			}else if(p.equipments.find(itemId)!=null){//身上
				try {
					equip(nextLevelItem);//更换新装备
				} catch (Exception e) {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.EQUIP_LEVELUP_CLIENT, "升级失败，请重试！");
					return;
				}
			}else{//暂时屏蔽马装
				Horse h =null;// p.horseBag.getHorse(p.horse.instanceId);
				 for (Horse horse : p.horseBag.horses) {
		            GameItem gi = horse.equs.find(itemId, instanceId);
		            if (gi != null) {
		               h=horse;
		            }
			     }
				if (h == null) {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.EQUIP_LEVELUP_CLIENT, "升级失败，请重试！");
						return;
				}
				if(p.horseEquipForEQLevelUp(nextLevelItem, h.instanceId)==-1){
					ErrorHandler.sendErrorMessage(session, serial,
						OpCode.EQUIP_LEVELUP_CLIENT, "升级失败，请重试！");
					return;
				}
			}
			Packet pt = new Packet(OpCode.EQUIP_LEVELUP_SERVER);
			pt.putInt(serial);
			pt.putInt(nextLevelItem.template.id);
			pt.putInt(nextLevelItem.instanceId);
			pt.put(nextLevelItem.template.canLevelUp?1:0);
			p.send(pt);
		}
		if(gameItem!=null&&gameItem.template!=null&&gameItem.template.name!=null&&
				nextLevelItem!=null&&nextLevelItem.template!=null&&nextLevelItem.template.name!=null&&nextLevelItem.template.name.contains("五")){
			String message=MessageFormat.format("{0}把{1}提升至/-1，顿时威仪绝世，动若狂雷，静若孤峰，号令天下，剑指乾坤！", p.name,gameItem.template.name);
			ChatMessage cm = new ChatMessage(ChatOption.WORLD,-1,-1,peony.Messages.STRING_00004,message,new ItemChatAttachment(nextLevelItem));
			ChatService chat=Server.server.getServiceRegistry().getChatService();
			chat.addChatMessage(cm);
//			Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_CHAT,p, new Byte((byte)0).intValue()));
		}
		log.info("[EQUIPLEVELUPNEW]PLAYERACC["+p.accountId+"]ID["+p.id+"]NEWEQUIP["+LogUtil.getGameItemString(nextLevelItem, 1)+"]");
	}
	
	/**是否可升级装备*/
	public boolean canLevelUp(GameItem item){
		if(item!=null&&item.template!=null&&item.template.equipment!=null&&item.template.equipment.equ!=null
				&&item.template.equipment.equ.canLevelUp&&item.template.nextLevelEquipID>0
				/*&&gameItem.template.equipment.equ.equipLevelUpPriceType>0&&
				gameItem.template.equipment.equ.equipLevelUpPriceNum>0*/){
			return true;
		}
		return false;
	}
	
	public void equip(GameItem equ){
		PlayerTransaction tx = p.newTransaction("UEQINEQUIPLEVELUP");
		GameItem item = p.equipments.equip(equ);//p.equipments.unequip(itemId, instanceId);
		if (item != null) {
			try{
				if (item.template.isEquipment()
						&& item.template.equipment != null) {
					EquipmentTemplate template = item.template.equipment;
					if (template.specialEffect != null) {
						p.buffs.removeBuff(template.specialEffect);
					}
					p.unSuiteEffect(template, p.equipments.equs);
				}
				tx.commit();
			}catch(Exception e){
				tx.rollback();
			}
		} else {
			tx.rollback();
			p.message(-1, peony.Messages.STRING_00132, -1, -1);
		}
		EquipmentTemplate template = equ.template.equipment;
		// 添加套装特效
		if (template.specialEffect != null) {
			Buff specialBuff = p.buffs
					.getBuffByID(template.specialEffect.getId());
			if (specialBuff == null) {
				p.buffs.addBuff(template.specialEffect);
			}
		}
		// 添加新装备后添加Buff
		if (template.suiteEffects != null) { // 如果套装效果一样就没必要加了
			if(template.suiteEffects.type==SuiteEffects.TYPE_WEIGHT){
				 // 如果套装效果一样就没必要加了
				SuiteEffect[] suiteEffects = template.suiteEffects.getEffects(); // 添加新装备后可能影响的套装效果
				GameItem[] gameItems = p.equipments.equs; // 得到player身上的装备
				boolean isChangeBuff=false;
				for (SuiteEffect effect : suiteEffects) {
					int suiteEffectType = effect.type;
					Buff buff = effect.buff;
					if(suiteEffectType==SuiteEffect.TYPE_WEIGHT_CALC){//套装效果有权重时
						int weight=0;
						for (GameItem gameItem : gameItems) {
							if (gameItem != null
									&& gameItem.template.isEquipment()) {
								if (gameItem != null
										&& gameItem.template.equipment.suiteEffects != null
										&& gameItem.template.equipment.suiteEffects == template.suiteEffects) {
									int equipWeight=gameItem.template.equipment.suiteEffects.weights.get(gameItem.template.id);
									weight+=equipWeight;
								}
							}
						}
						if(!isChangeBuff){
							isChangeBuff=true;
							p.buffs.removeBuff(effect.buffId);
							buff = BuffUtil.createSuiteBuff(effect.buffId, effect.buffLevel,weight);
							if(weight>0)
								p.buffs.addBuff(buff);
						}
					}
				}
			}else if(template.suiteEffects.type==SuiteEffects.TYPE_NORMAL){
				if(template.suiteEffects != null&&template.suiteEffects.type==SuiteEffects.TYPE_NORMAL){
					 // 如果套装效果一样就没必要加了
					SuiteEffect[] suiteEffects = template.suiteEffects.getEffects(); // 添加新装备后可能影响的套装效果
					GameItem[] gameItems = p.equipments.equs; // 得到player身上的装备
					for (SuiteEffect effect : suiteEffects) {
						int suiteEffectType = effect.type;
						Buff buff = effect.buff;
						if(suiteEffectType==SuiteEffect.TYPE_NORMAL){//套装效果无权重时按老套装执行
							if(p.buffs.isNoSingleSuiteBuff(buff.getId())){//如果要加单例的套装效果必须放到Buffs.noSingleSuiteBuffs里
								buff = BuffUtil.createSuiteBuff(effect.buffId, effect.buffLevel);
							}
							int effectCount = effect.getCount();
							int count = 0;
							if (gameItems != null) {
								for (GameItem gameItem : gameItems) {
									if (gameItem != null
											&& gameItem.template.isEquipment()) {
										if (gameItem != null
												&& gameItem.template.equipment.suiteEffects != null
												&& gameItem.template.equipment.suiteEffects == template.suiteEffects) {
											count++;
										}
									}
								}
							}
							if (count == effectCount) { // 添加新装备后如果正好构成一个套装效果，则添加此套装效果
								p.buffs.addBuff(buff);
							}
						}
					}
				
				}
			}
		}
		p.refreshStar7Buff();
		if (p.clazz == Unit.CLASS_2
				&& Equipments.EQU_INDEXES[equ.template.equipment.minorType - 1] == Equipments.HAND) { // 如果是刺客并且更换的是武器，那么重发一遍自动攻击技能
			SkillChangedItem changedItem = new SkillChangedItem(p.skills
					.getSkill(1), false);
			p.changed.addChangedItem(changedItem);
		}
		// 刷新星辉状态
		p.refreshStarState();
		// 记录玩家动作
		p.addAction(Action.EQUIP);
		p.processCardBuff();
		p.refreshProperties(false);
		//玩家换装事件
		Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_CHANGE_EQUIP,p));
	}
}
