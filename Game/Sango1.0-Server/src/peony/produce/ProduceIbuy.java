package peony.produce;

import java.util.List;
import java.util.Random;

import com.pip.sanguo.data.Shop;
import com.pip.sanguo.data.Shop.BuyRequirement;
import com.pip.sanguo.data.item.Formula;

import peony.game.Action;
import peony.game.ErrorHandler;
import peony.game.Gain;
import peony.game.GainItem;
import peony.game.GameItem;
import peony.game.GameObjectRef;
import peony.game.LogUtil;
import peony.game.NoEnoughSpaceException;
import peony.game.NoEnoughValueException;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.drop.GroupDrop;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.ServiceEvent;
import peony.service.shop.IBuyObject;
import peony.service.shop.ShopException;
import peony.service.tong.TongMember;
import peony.service.tong.TongService;
import peony.service.tong.TongSkill7;
import peony.service.tong.TongSkill8;

public class ProduceIbuy implements IBuyObject {

	List<BuyRequirement> requirements;
	GameObjectRef player;
	int productType;
	int groupId;
	ClientSession session;
	int serial;
	Packet packet1;
	int itemId;
	Formula formula;
	int practice;
	int movePoint;
	int count;
	Object output; // GameItem or GainItem[]
	public static Random rnd = new Random();
	
	public ProduceIbuy(List<BuyRequirement> requirements, Player player, int productType, int groupId, 
			ClientSession session, int serial,Packet packet1,int itemId,Formula formula
			,int practice,int movePoint){
		this.requirements = requirements;
		this.player = player.ref();
		this.groupId = groupId;
		this.session = session;
		this.serial = serial;
		this.packet1 = packet1;
		this.itemId = itemId;
		this.formula = formula;
		this.practice = practice;
		this.movePoint = movePoint;
		this.productType = productType;
	}
	
	public void commit() {
		
	}

	public int getCount() {
		return 1;
	}

	public int getDiscount() {
		return 100;
	}

	public List<BuyRequirement> getRequirements() {
		return requirements;
	}

	public void lock() throws ShopException {
		
	}

	public void notifyClient(boolean succ, String message) {
		if(succ){
			
		}else{
			ErrorHandler.sendErrorMessage(session, serial, OpCode.PRODUCE_CLIENT, message);
		}
	}

	public void receive(PlayerTransaction tx, boolean supportMail)
			throws ShopException {
		 Player p = (Player)ObjectAccessor.getGameObject(player);
	        if (p == null) {
	            throw new ShopException(peony.Messages.STRING_00530);
	        }
	        int outType = -1; // 1为角色装备，2为坐骑装备
	        if (productType == 1) {
				GroupDrop groupDrop = ObjectAccessor.groupDrops.get(groupId);
				Gain gain = new Gain(p);
				groupDrop.calc(new Random(), gain);
				try {
					p.addGainComplete(gain, tx, false);
					count = gain.getGainItems().length;
					if(movePoint > 0){
						try {
							p.decActivePower(movePoint);
						} catch (NoEnoughValueException e) {
							ErrorHandler.sendErrorMessage(session, serial, OpCode.PRODUCE_CLIENT,peony.Messages.STRING_00777);
							throw new ShopException(peony.Messages.STRING_00777); 
						}
					}
				} catch (NoEnoughSpaceException e) {
//					tx.rollback();
					ErrorHandler.sendErrorMessage(session, serial, OpCode.PRODUCE_CLIENT, peony.Messages.STRING_00119);
					throw new ShopException(peony.Messages.STRING_00555);
				}
				output = gain.getGainItems();
				packet1.put(((GainItem[])output).length);
				boolean isEquipment = false;
				for(GainItem gainItem : ((GainItem[])output)){
					if(gainItem != null){
						if(!isEquipment && gainItem.getItem().template.isHorseEquipment()){
							outType = 1;
							isEquipment = true;
						} else if(!isEquipment && gainItem.getItem().template.isEquipment()){
							outType = 0;
							isEquipment = true;
						}
						packet1.put(gainItem.getItem().toClientBytes());
						packet1.put(1);
					}
				}
			} else {
				if (itemId > 0) {
					GameItem gameItem = ObjectAccessor.createGameItem(itemId); 
					int rankNum = ProduceService.getRandomAmount(formula.minAmount, formula.maxAmount);
					count = rankNum;
					try {
						p.bag.addGameItemComplete(gameItem, rankNum, tx, false);
						if(movePoint > 0){
							try {
								p.decActivePower(movePoint);
							} catch (NoEnoughValueException e) {
								ErrorHandler.sendErrorMessage(session, serial, OpCode.PRODUCE_CLIENT,peony.Messages.STRING_00777);
								throw new ShopException(peony.Messages.STRING_00777); 
							}
						}
					} catch (NoEnoughSpaceException e) {
						ErrorHandler.sendErrorMessage(session, serial, OpCode.PRODUCE_CLIENT, peony.Messages.STRING_00119);
						throw new ShopException(peony.Messages.STRING_00555);
					}
					packet1.put(1);
					packet1.put(gameItem.toClientBytes());
					packet1.put(rankNum);
//					output = new GameItem[] { gameItem };
					output = gameItem;
				}
			}
			int x = ProduceService.enhancePractice(p,formula.level, ProduceService.getPracticeLevel(p.level, practice), 
					p.pool.getInt(Player.PROPERTY_PRODUCE_ABILITY), p.level);
			//军团专属科技  打造暴击
			TongService ts = Server.server.getServiceRegistry().getTongService();
			TongMember tm = ts.getPlayerInfo(p.id);
			if(tm!=null && tm.skills != null && tm.skills.get(8)!=null){
				TongSkill8 tskill = (TongSkill8)tm.skills.get(8);
				if(tskill.level > 0){
					int ratios = tskill.getRatios();
					if(rnd.nextInt(100)>=(100 - ratios)){
						x *= 2;
					}
				}
			}
			packet1.putShort(x);
			practice += x;
			p.pool.setInt(Player.PROPERTY_PRODUCE_ABILITY, practice);
			packet1.putString(ProduceService.getLevelToString(ProduceService.getPracticeLevel(p.level, practice)));
			p.send(packet1);
			Server.server.getEventManager().fireEvent(new ServiceEvent(ServiceEvent.EVENT_PRODUCE,player.id,outType,ProduceService.getPracticeLevel(p.level, practice),formula.level,output));
			// 记录玩家动作
			p.addAction(Action.PRODUCE);
	}

	public void rollback() {
		
	}

	@Override
	public String toString() {
		return "TYPE[PRODUCE]FID[" + formula.id + "]";
    }
	
	/**
     * 记录购买成功日志。
     */
    public void log() {
    	if (output != null) {
			if (output instanceof GameItem) {
				LogUtil.logProduce(player, formula.id, (GameItem)output, this);
			} else {
				LogUtil.logProduce(player, formula.id, (GainItem[])output, this);
			}
    	}
    }
    
    public String getCause() {
    	return "MKE";
    }

    /**
     * 是否允许使用不被信任的元宝购买。
     */
    public boolean allowUntrustIMoney() {
    	return true;
    }

	public boolean allowUseBindImoney() {
		return true;
	}
}
