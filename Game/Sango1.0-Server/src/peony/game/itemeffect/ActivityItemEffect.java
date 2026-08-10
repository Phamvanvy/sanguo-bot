package peony.game.itemeffect;

import java.text.MessageFormat;
import peony.game.Gain;
import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Time;
import peony.game.Unit;
import peony.game.UseItemException;
import peony.game.changed.ChangedItem;
import peony.net.Packet;
import peony.service.MonthlyPayService;

public class ActivityItemEffect implements ItemEffect{
	protected int type;
	protected int value;
	protected float addValue;
	protected boolean needRemove = false;
	
	protected static int ITEM_WORLDTRAVEL = 0;
	protected static int ITEM_QUESTCREDIT = 1;
	protected static int ITEM_HORSEFIX = 2;
	protected static int ITEM_HORSECHANGE = 3;
	protected static int ITEM_IMONEYDISCOUNT = 4;
	protected static String[] factionName = {"世界畅游","N倍战功","坐骑合成打折","坐骑幻化打折","商城打折"};
	
	protected static int HOUR = 60*60*1000;//一小时
	public static String[] itemProperties = {"PROPERTY_ITEM_WORLDTRAVEL","PROPERTY_ITEM_CREDIT","PROPERTY_ITEM_HORSEFIX","PROPERTY_ITEM_HORSECHANGE","PROPERTY_ITEM_IMONEYDISCOUNT"};
	public static String[] valueProperties = {"PROPERTY_VALUE_WORLDTRAVEL","PROPERTY_VALUE_CREDIT","PROPERTY_VALUE_HORSEFIX","PROPERTY_VALUE_HORSECHANGE","PROPERTY_VALUE_IMONEYDISCOUNT"};
	public static String[] checkProperties = {"PROPERTY_LASTTIME_WORLDTRAVEL","PROPERTY_LASTTIME_CREDIT","PROPERTY_LASTTIME_HORSEFIX","PROPERTY_LASTTIME_HORSECHANGE","PROPERTY_COUNT_IMONEYDISCOUNT"};
	public static String[] addValueProperties = {"PROPERTY_ADDVALUE_WORLDTRAVEL","PROPERTY_ADDVALUE_CREDIT","PROPERTY_ADDVALUE_HORSEFIX","PROPERTY_ADDVALUE_HORSECHANGE","PROPERTY_ADDVALUE_IMONEYDISCOUNT"};
	public ActivityItemEffect(int type,int value,float addValue){
		this.type = type;
		this.value = value;
		this.addValue = addValue;
	}
	public boolean isAsync() {
		return false;
	}

	public void use(Unit source, GameItem item, Unit target,
			PlayerTransaction tx) throws UseItemException {
		Player p = (Player)source;
		if(p!=null){
			if(type == ITEM_WORLDTRAVEL){
//				if(p.monthPay.containsKey(MonthlyPayService.MONTHPAY_TYPE_TELEPORT) && p.monthPay.get(MonthlyPayService.MONTHPAY_TYPE_TELEPORT)!=0){
//					Packet pt = new Packet(OpCode.OPENUI_SERVER);
//					pt.putString("ui_npc_dialog");
//					String msg = MessageFormat.format("您已经开通传送包月功能，请问是否继续使用{0}的效果？", item.template.name);
//					pt.putString("DOUBLE_NINTH_FESTIVAL|"+msg+"|"+String.valueOf(item.template.id)+"|"+String.valueOf(type)+"|"+String.valueOf(value)+"|"+String.valueOf(addValue));
//					p.send(pt); 
//				    return;
//				}
			}
			int v = p.pool.getInt(valueProperties[type], 0);
			if(type == ITEM_IMONEYDISCOUNT){
				int usedCount = p.pool.getInt(checkProperties[type], 0); 
				if(usedCount>=v){
					usedCount = 0;
					p.pool.remove(checkProperties[type]);
					v = 0;
					p.pool.remove(valueProperties[type]);
					p.pool.remove(itemProperties[type]);
				}
			}else{
				long oriTime = p.pool.getLong(checkProperties[type], 0);
				if(Time.currentTimeMillis(Time.currTime)>=oriTime){
					p.pool.remove(checkProperties[type]);
					v = 0;
					p.pool.remove(valueProperties[type]);
					p.pool.remove(itemProperties[type]);
				}
			}
			if(v==0){
				checkAdd(p,item.template.id,item.instanceId,type,value,String.valueOf(addValue),false);
				needRemove = true;
			}
		}
	}
	
	
	public int getType(){
		return this.type;
	}
	
	public int getValue(){
		return this.value;
	}
	
	public float getAddValue(){
		return this.addValue;
	}
	
	public static boolean confirm(Player p,int itemId,int gridId){
		if(p!=null){
			GameItem item = ObjectAccessor.createGameItem(itemId);
			int type = -1;
			int value = 0;
			float addValue = 0.0f;
			if(item.template.useType.effect!=null && item.template.useType.effect instanceof ActivityItemEffect){
			ActivityItemEffect effect = (ActivityItemEffect)item.template.useType.effect;
				if(effect!=null){
					type = effect.getType();
					value = effect.getValue();
					addValue = effect.getAddValue();
				}
			}
			if(type<0)
				return false;
			if(type == ITEM_WORLDTRAVEL){
//				if(p.monthPay.containsKey(MonthlyPayService.MONTHPAY_TYPE_TELEPORT) && p.monthPay.get(MonthlyPayService.MONTHPAY_TYPE_TELEPORT)!=0){
//					Packet pt = new Packet(OpCode.OPENUI_SERVER);
//					pt.putString("ui_npc_dialog");
//					String msg = MessageFormat.format("您已经开通传送包月功能，请问是否继续使用{0}的效果？", item.template.name);
//					pt.putString("DOUBLE_NINTH_FESTIVAL|"+msg+"|"+String.valueOf(item.template.id)+"|"+String.valueOf(type)+"|"+String.valueOf(value)+"|"+String.valueOf(addValue));
//					p.send(pt); 
//				    return;
//				}
			}
			int v = p.pool.getInt(valueProperties[type], 0);
			int oriItemId = p.pool.getInt(itemProperties[type],0);
			if(type == ITEM_IMONEYDISCOUNT){
				int usedCount = p.pool.getInt(checkProperties[type], 0); 
				if(usedCount>=v){
					usedCount = 0;
					p.pool.remove(checkProperties[type]);
					v = 0;
					p.pool.remove(valueProperties[type]);
					p.pool.remove(itemProperties[type]);
				}
			}else{
				long oriTime = p.pool.getLong(checkProperties[type], 0);
				if(Time.currentTimeMillis(Time.currTime)>=oriTime){
					p.pool.remove(checkProperties[type]);
					v = 0;
					p.pool.remove(valueProperties[type]);
					p.pool.remove(itemProperties[type]);
				}
			}
			if(v!=0){
				GameItem oriItem = ObjectAccessor.createGameItem(oriItemId);
				Packet pt = new Packet(OpCode.OPENUI_SERVER);
				pt.putString("ui_npc_dialog");
				String msg = MessageFormat.format("您要使用{0}效果将叠加{1}效果，请问是否继续？", item.template.name,oriItem.template.name);
				if(oriItemId == item.template.id){
					msg =MessageFormat.format("您尚有{0}效果在身，是否叠加到原有的{1}效果？", item.template.name,oriItem.template.name);
				}
				pt.putString("DOUBLE_NINTH_FESTIVAL|"+msg+"|"+String.valueOf(item.template.id)+"|"+gridId+"|"+String.valueOf(type)+"|"+String.valueOf(value)+"|"+String.valueOf(addValue));
				p.send(pt); 
				return true;
			}
		}
		return false;
	}
	
	/** 效果生效 */
	public static void checkAdd(Player p,int itemId,int gridId,int type,int value,String addValue,boolean createTransaction){
		int oldItem = p.pool.getInt(itemProperties[type],0);
		p.pool.setInt(itemProperties[type], itemId);
		int oldValue = p.pool.getInt(valueProperties[type],0);
		p.pool.setInt(valueProperties[type], value);
		if(type<ITEM_IMONEYDISCOUNT){
			long lastTime = Time.currentTimeMillis(Time.currTime);
			long oldLastTime = p.pool.getLong(checkProperties[type], 0);
			if(lastTime<=oldLastTime ){
				oldLastTime -= lastTime;
			}else{
				oldLastTime = 0;
			}
			p.pool.setLong(checkProperties[type], oldLastTime+value*HOUR+lastTime); 
			GameItem item = ObjectAccessor.createGameItem(itemId);
			String t ="";
			if(value/24>0){
				t = String.valueOf(value/24)+"天";
			}else{
				t = String.valueOf(value)+"小时";
			}
			String message = MessageFormat.format("您使用了{0}，您的{1}功能增加了{2}。", item.template.name,factionName[type],t);
			p.message(-1, message, -1, -1);
		}else if(type == ITEM_IMONEYDISCOUNT){
			int usedCount = p.pool.getInt(checkProperties[type], 0); 
			int newValue = p.pool.getInt(valueProperties[type],0);
			p.pool.setInt(valueProperties[type], newValue+oldValue - usedCount);
			GameItem item = ObjectAccessor.createGameItem(itemId);
			String message = MessageFormat.format("您使用了{0}，您的元宝打折功能增加了{1}次", item.template.name,newValue);
			p.message(-1, message, -1, -1);
		}
		if(type==ITEM_WORLDTRAVEL){
			if(p.monthPay.get(MonthlyPayService.MONTHPAY_TYPE_TELEPORT)==0 && oldItem==0){
				GameItem item  = ObjectAccessor.createGameItem(MonthlyPayService.MONTHPAY_TYPE_TELEPORT);
				p.addIntPropertyChangedItem(ChangedItem.TIMEOUT,item.template.id,false,true);
			}
		}else{
			p.pool.setString(addValueProperties[type], String.valueOf(addValue)); 
		}
	}
	

	public static int addValue(Player p,int value,int type){
		int ret = value;
		boolean checkAdd = false;
		int active = p.pool.getInt(valueProperties[type], 0);
		String tempValue = p.pool.getString(addValueProperties[type]);
		if(type<ITEM_IMONEYDISCOUNT){
			long lastTime = p.pool.getLong(checkProperties[type], 0);
			if(active>0 && Time.currentTimeMillis(Time.currTime)<lastTime){
				checkAdd = true;
			}
		}else if(type == ITEM_IMONEYDISCOUNT){
			int count = p.pool.getInt(checkProperties[type],0);
			if(active>0 && count<active){
				checkAdd = true;
//				count++;
//				p.pool.setInt(checkProperties[type],count);
			}
		}
		if(checkAdd){
			float addValue = Float.parseFloat(tempValue);
			ret = (int)Math.round(value*addValue);
		}else{
			p.pool.remove(itemProperties[type]);
			p.pool.remove(valueProperties[type]);
			p.pool.remove(checkProperties[type]);
			p.pool.remove(addValueProperties[type]);
		}
		return ret;
	}
	
	public static void addCount(Player p){
		int active = p.pool.getInt(valueProperties[ITEM_IMONEYDISCOUNT], 0);
		int count = p.pool.getInt(checkProperties[ITEM_IMONEYDISCOUNT],0);
		if(active>0 && count<active){
			count++;
			p.pool.setInt(checkProperties[ITEM_IMONEYDISCOUNT],count);
		}
	}
	
	public static void removeProperty(Player p){
		for(int i=1;i<valueProperties.length;i++){
			boolean remove = true;
			int itemId = p.pool.getInt(valueProperties[i],0);
			if(itemId>0){
				if(i == ITEM_IMONEYDISCOUNT){
					int count = p.pool.getInt(checkProperties[i], 0);
					int totalCount = p.pool.getInt(valueProperties[i],0);
					if(count<totalCount){
						remove = false;
					}
				}else{
					long lastTime = p.pool.getLong(checkProperties[i], 0);
					if(Time.currentTimeMillis(Time.currTime)<lastTime){
						remove = false;
					}
				}
			}
			if(remove){
				p.pool.remove(itemProperties[i]);
				p.pool.remove(valueProperties[i]);
				p.pool.remove(checkProperties[i]);
				p.pool.remove(addValueProperties[i]);
			}
		}
	}
	
	public static void getQuestCredit(Player player,Gain gain){
		int value = gain.getCredit();
		if(value>0){
		    int tempValue = addValue(player,value,ITEM_QUESTCREDIT);
		    gain.setCredit(tempValue);
		}
	}
	
	/** 坐骑合成*/
	public static int processHorseFix(Player player,int value){
		int ret = addValue(player,value,ITEM_HORSEFIX);
		return ret;
	}
	
	/** 坐骑幻化*/
	public static int processHorseChange(Player player,int value){
		int ret = addValue(player,value,ITEM_HORSECHANGE);
		return ret;
	}
	
	/** 商城打折卡*/
	public static int processImoneyDiscount(Player player,int value){
		int ret = addValue(player,value,ITEM_IMONEYDISCOUNT);
		return ret;
	}
	
	public static int processWorldTeleport(Player player){
		int ret=0;
		int type = ITEM_WORLDTRAVEL;
		int itemId = player.pool.getInt(itemProperties[type],0);
		long lastTime = player.pool.getLong(checkProperties[type],0);
		if(itemId!=0 && Time.currentTimeMillis(Time.currTime)<lastTime){
			ret = 1;
		}else{
			player.pool.remove(itemProperties[type]);
			player.pool.remove(valueProperties[type]);
			player.pool.remove(checkProperties[type]);
			player.pool.remove(addValueProperties[type]);
		}
		return ret;
	}
	
	public static void updateWorldTeleport(Player player){
		int type = ITEM_WORLDTRAVEL;
		int itemId = player.pool.getInt(itemProperties[type],0);
		long lastTime = player.pool.getLong(checkProperties[type],0);
		if(itemId>0){
			if(Time.currentTimeMillis(Time.currTime)>lastTime){
				player.pool.remove(itemProperties[type]);
				player.pool.remove(valueProperties[type]);
				player.pool.remove(checkProperties[type]);
				player.pool.remove(addValueProperties[type]);
				if(player.monthPay.get(MonthlyPayService.MONTHPAY_TYPE_TELEPORT)==0){
					GameItem item  = ObjectAccessor.createGameItem(MonthlyPayService.MONTHPAY_TYPE_TELEPORT);
					player.addIntPropertyChangedItem(ChangedItem.TIMEOUT,item.template.id,false,true);
				}
			}
		}else{
			player.pool.remove(itemProperties[type]);
			player.pool.remove(checkProperties[type]);
		}
	}
	
	public static boolean hasTeleportEffect(Player player){
		int value = ActivityItemEffect.processWorldTeleport(player);
		if(value >0){
			return true;
		}
		return false;
	}
	public boolean needRemove() {
		if(needRemove)
			return true;
		return false;
	}
}
