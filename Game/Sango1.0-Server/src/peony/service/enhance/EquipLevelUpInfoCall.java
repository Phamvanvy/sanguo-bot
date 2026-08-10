package peony.service.enhance;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;


import peony.common.ClientSessionAsyncCall;
import peony.game.EquipmentTemplate;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.ItemTemplate;
import peony.game.ItemUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.buff.Buff;
import peony.game.buff.BuffUtil;
import peony.game.itemenhance.ItemEnhance;
import peony.game.suite.SuiteEffect;
import peony.game.suite.SuiteEffects;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.shop.NoItemShopBuy;
import peony.service.shop.ShopService;

/****
 * 可升级装备信息返回
 * @author chunhui.shao
 *
 */
public class EquipLevelUpInfoCall extends ClientSessionAsyncCall{
	
	public static final int LEVELUP_PRICEIMONEYID=2754;//代扣6元宝
	public static final int LEVELUP_USEDITEMID=4742;//升级需要材料
	public static final int LEVELUP_PRICE_SAVEPROP=4349;//保留属性代扣物品
	
	public String oldEquipSuiteDesc="";
	
	public static final int buffIds[]={
		650,654,655,656,
		662,659,660,661,662,663,664,665,666,667,668,669,670,
		671,672,673,674,675,676,677,678,679,680,681,682
		,695,696,697,698
	};
	public static final int suiteIds[][]={//【职业】【ItemId】
		{1008629,1008657,1008694,1008722,1008750},
		{1008635,1008663,1008700,1008728,1008756},
		{1008642,1008670,1008707,1008735,1008763},
		{1008649,1008677,1008714,1008742,1008770},
	};
	public static final int suiteHorse[][]={
		{1008805,1008826,1008847,1008868,1008889},//灭魂奔袭套装
		{1008812,1008833,1008854,1008875,1008896},//灭魂绝杀套装
		{1008819,1008840,1008861,1008882,1008903},//灭魂智囊套装
	};
	/**区分老装备职业*/
	public static final int oldEquipId[][]={//【职业】【老装备ID】
		{1008167,1008168,1008169,1008170,1008171,1008172,1008191,1008283,1008284,1008285,1008286,1008287,1008288,1008282},
		{1008289,1008290,1008291,1008292,1008293,1008294,1008295,1008173,1008174,1008175,1008176,1008177,1008178,1008194},
		{1008296,1008297,1008298,1008299,1008300,1008301,1008302,1008179,1008180,1008181,1008182,1008183,1008184,1008192},
		{1008303,1008304,1008305,1008308,1008309,1008310,1008311,1008312,1008185,1008186,1008187,1008188,1008189,1008190,1008193,1008195},
	};
	
	public static boolean isOldEquip(int itemId,int clazz){
		for(int equipid:oldEquipId[clazz]){
			if(equipid==itemId){
				return true;
			}
		}
		return false;
	}
	
	public static final String num_Chinese="零一二三四五六七八九十";
	
	public String equipLevelUpExplain=
		 "1、只有勇魂套装，天命套装，灭魂套装才可以进行升级。\n"
		+"2、勇魂套装升级后将成为天命套装，升级时将消耗一定数量的战功，保留原装备上的孔位、资质、星级、宝石、强化，将扣除{0}元宝，保留属性并成功升级后，原装备将消失，新装备将继承原装备身上的孔位、资质、星级、宝石、强化属性。不保留时，<cff0000>原装备身上的宝石将会返还给背包里或飞鸽里，其它属性将消失。</c>\n"
		+"3、天命套装免费升级为灭魂套装零阶，<cff0000>若保留原装备上的孔位、资质、星级、宝石、强化，每件装备将扣除{0}元宝。</c>\n"
		+"4、灭魂套装一共五阶，每升一阶系统都自动为您<cff0000>免费保留装备上的孔位、资质、星级、宝石、强化属性，</c>您在升阶的时候可以选择用玄武石升阶或消费元宝升阶，阶级越高消耗玄武石或元宝数量会相应增多。\n"
		+"5、打造的装备升级后将会绑定。\n"
		+"6、灭魂系列人物装备只可穿戴在人物身上，暂不能为随从穿戴。\n"
		+"7、您可以通过以下途径获得玄武石：\n"
		+"1）每天可以在南海公告牌上接取3个“玄武石任务”，每个任务奖励1块玄武石。\n"
		+"2）工资数量达到10点，20点，30点，40点都将获得1块玄武石。\n"
		+"3）每天的整点都将在南海地图刷新10支西域特贡商队，每消灭1支商队后可获得1块玄武石。当您已经消灭3支商队并且获得3块玄武石，再消灭商队无论单人还是组队将不会再获得玄武石。\n"
		+"4）玩家可用工资在工资商店兑换玄武石，每日可兑换2个。\n"
		+"5）每人每天最多可获得12块玄武石。"
		;
	public String equipLevelUpExplainHorse=
		 "1、只有天命套装，灭魂套装才可以进行升级。\n"
		+"2、天命套装免费升级为灭魂套装零阶，<cff0000>若保留原装备上的孔位、资质、星级、宝石、强化，每件装备将扣除{0}元宝。</c>\n"
		+"3、灭魂套装一共五阶，每升一阶系统都自动为您<cff0000>免费保留装备上的孔位、资质、星级、宝石、强化属性，</c>您在升阶的时候可以选择用玄武石升阶或消费元宝升阶，阶级越高消耗玄武石或元宝数量会相应增多。\n"
		+"4、您可以通过以下途径获得玄武石：\n"
		+"1）每天可以在南海公告牌上接取3个“玄武石任务”，每个任务奖励1块玄武石。\n"
		+"2）工资数量达到10点，20点，30点，40点都将获得1块玄武石。\n"
		+"3）每天的整点都将在南海地图刷新10支西域特贡商队，每消灭1支商队后可获得1块玄武石。当您已经消灭3支商队并且获得3块玄武石，再消灭商队无论单人还是组队将不会再获得玄武石。\n"
		+"4）玩家可用工资在工资商店兑换玄武石，每日可兑换2个。\n"
		+"5）每人每天最多可获得12块玄武石。"
		;
	
	public static boolean isLevelUpEquip(int buffId){
		for(int id:buffIds){
			if(id==buffId){
				return true;
			}
		}
		return false;
	}
	
	public static final boolean canLevelUp=true;//装备升级开关,26号更新时设为false
	

	Player p = null;
	int serial;
	int itemId ;
	int instanceId ;
	GameItem gameItem;
	Object owner;
	
	public EquipLevelUpInfoCall(ClientSession session,Packet packet) {
		super(session);
		p = (Player) session.getClient();
		serial = packet.getInt();
		itemId = packet.getInt();
		instanceId = packet.getInt();
	}

	
	public void callFinish() throws Exception {
		
	}

	
	public void run() {
		if(p!=null){
			if(!canLevelUp){
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.EQUIPMENT_LEVELUP_ITEMINFO_CLIENT, "此功能暂未开放，敬请期待！");
			}
			Object[] os = ItemUtil.findPlayerEquipment(p, itemId, instanceId);
			if (os != null) {
				gameItem = (GameItem) os[0];
				owner = os[1];
				if (gameItem == null) {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.EQUIPMENT_LEVELUP_ITEMINFO_CLIENT, peony.Messages.STRING_01352);
					return;
				}
				if (!gameItem.template.isEquipment()) {
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.EQUIPMENT_LEVELUP_ITEMINFO_CLIENT, peony.Messages.STRING_01353);
					return;
				}
				if(!canLevelUp(gameItem)){
					ErrorHandler.sendErrorMessage(session, serial,
							OpCode.EQUIPMENT_LEVELUP_ITEMINFO_CLIENT, "此装备不允许升级！");
					return;
				}
				GameItem nextLevelItem=ObjectAccessor.createGameItem(gameItem.template.equipment.equ.nextEquipId);
				if(nextLevelItem.bindInstance == -1){//未绑定
					nextLevelItem.bindInstance = 0;
				}
				if(gameItem.object!=null){
					nextLevelItem.object=gameItem.object;
				}
				Packet pt = new Packet(OpCode.EQUIPMENT_LEVELUP_ITEMINFO_SERVER);
				pt.putInt(serial);
				//原装备是否有套装信息
				ItemTemplate itemT = ObjectAccessor.getItemTemplate(gameItem.template.id);
				if(itemT!=null&&itemT.equipment!=null&&itemT.equipment.suiteEffects!=null){
					if(itemT.equipment.suiteEffects.type==SuiteEffects.TYPE_NORMAL){
						int count=1;
						for(SuiteEffect suite:itemT.equipment.suiteEffects.getEffects()){
							if(suite!=null&&suite.getCount()==count){
								break;
							}else{
								count++;
							}
						}
						if(count==1){
							pt.put(1);
							processSuiteInfo(pt, gameItem);
						}else{
							pt.put(1);
							pt.putString("");
							pt.putString("");
							pt.put(0);
							pt.putString("");
							pt.put(0);
							pt.putString("");
							pt.put(0);
							pt.putString("");
							pt.put(0);
						}
					}else if(itemT.equipment.suiteEffects.type==SuiteEffects.TYPE_WEIGHT){
						pt.put(1);
						processSuiteInfo(pt, gameItem);
					}
				}
//				if(isGameItemHadSuite(gameItem)){
//					pt.put(1);
//					processSuiteInfo(pt, gameItem);
//				}else{
//					pt.put(0);
//				}
				pt.put(nextLevelItem.toClientBytes());
				//升级后的装备是否有套装信息
				if(isGameItemHadSuite(nextLevelItem)){
					pt.put(1);
					processSuiteInfo(pt, nextLevelItem);
				}else{
					pt.put(1);
					pt.putString("");
					pt.putString("");
					pt.put(0);
					pt.putString("");
					pt.put(0);
					pt.putString("");
					pt.put(0);
					pt.putString("");
					pt.put(0);
				}
				int type=gameItem.template.equipment.equ.equipLevelUpPriceType;
				GameItem needItem=ObjectAccessor.createGameItem(LEVELUP_USEDITEMID);
				if(type==3&&p.bag.getGameItemCount(LEVELUP_USEDITEMID)<gameItem.template.equipment.equ.equipLevelUpPriceNum){
					type=4;
				}
				pt.put(type);
				pt.putInt(gameItem.template.equipment.equ.equipLevelUpPriceNum);
				if(type==4){//材料不足时
					int needCount=gameItem.template.equipment.equ.equipLevelUpPriceNum-p.bag.getGameItemCount(LEVELUP_USEDITEMID);
					
					ShopService shopService=Server.server.getServiceRegistry().getShopService();
					float itemMoney=shopService.getItemPrice(LEVELUP_PRICEIMONEYID)/36;
					NumberFormat numberFormat = NumberFormat.getInstance();
					numberFormat.setMaximumFractionDigits(1);
					String money="";
					if(Server.server.revision.equalsIgnoreCase(Server.REVISION_TYPE_TW)){
						money=numberFormat.format(itemMoney*needCount);
					}else{
						money=Math.round(itemMoney*needCount)+"";
					}
					pt.putUTF(MessageFormat.format("您将要把<cff0000>{0}</c>升级到<cff0000>{1}</c>，共需要<cff0000>{2}</c>个<cff0000>{3}</c>，您现在拥有<cff0000>{4}</c>个<cff0000>{3}</c>。请选择您喜欢的升级方式。",
							gameItem.template.name,
							nextLevelItem.template.name, 
							gameItem.template.equipLevelUpPriceNum,
							needItem.template.name,
//							gameItem.template.equipment.equ.equipLevelUpPriceNum*itemMoney,
							p.bag.getGameItemCount(LEVELUP_USEDITEMID)
//							money+""
							/*p.getIMoney()+""*/));
					pt.putUTF(MessageFormat.format("您将要把<cff0000>{0}</c>升级到<cff0000>{1}</c>，本次升级还需要<cff0000>{2}</c>个<cff0000>{3}</c>，需消耗<cff0000>{4}元宝</c>补齐材料，您当前元宝为<cff0000>{5}</c>，是否继续？",gameItem.template.name,nextLevelItem.template.name, needCount,needItem.template.name,money+"",p.getIMoney()+""));
					int allMoney=Math.round(gameItem.template.equipment.equ.equipLevelUpPriceNum*itemMoney);
					pt.putUTF(MessageFormat.format("您将要把<cff0000>{0}</c>升级到<cff0000>{1}</c>，需消耗<cff0000>{2}元宝</c>，您当前元宝为<cff0000>{3}</c>，是否继续？", gameItem.template.name,nextLevelItem.template.name, allMoney+"",
							p.getIMoney()+""));
				}else if(type==3){//材料充足时
					int needCount=gameItem.template.equipment.equ.equipLevelUpPriceNum;
					ShopService shopService=Server.server.getServiceRegistry().getShopService();
					float itemMoney=shopService.getItemPrice(LEVELUP_PRICEIMONEYID)/36;
					int money=Math.round(itemMoney*needCount);
					pt.putUTF(MessageFormat.format("您将要把<cff0000>{0}</c>升级到<cff0000>{1}</c>，共需要<cff0000>{2}</c>个<cff0000>{3}</c>，您现在拥有<cff0000>{4}</c>个<cff0000>{3}</c>。请选择您喜欢的升级方式。",
							gameItem.template.name,
							nextLevelItem.template.name, 
							gameItem.template.equipLevelUpPriceNum,
							needItem.template.name,
							p.bag.getGameItemCount(LEVELUP_USEDITEMID)
							));
					pt.putUTF("");
					pt.putUTF(MessageFormat.format("您将要把<cff0000>{0}</c>升级到<cff0000>{1}</c>，需消耗<cff0000>{2}元宝</c>，您当前元宝为<cff0000>{3}</c>，是否继续？", gameItem.template.name,nextLevelItem.template.name, money+"",p.getIMoney()+""));
				}else if(type==2){//元宝升级
					int money=gameItem.template.equipment.equ.equipLevelUpPriceNum;
					if(Server.server.revision.equalsIgnoreCase(Server.REVISION_TYPE_TW)){
						money*=0.6;
					}
					pt.putUTF("");
					pt.putUTF(MessageFormat.format("您将要把<cff0000>{0}</c>升级到<cff0000>{1}</c>，需消耗<cff0000>{2}元宝</c>，您当前元宝为<cff0000>{3}</c>，是否继续？", gameItem.template.name,nextLevelItem.template.name, money+"",p.getIMoney()+""));
				}else{
					pt.putUTF("");
					pt.putUTF("");
				}
				byte savePropTypeTemp=(byte)gameItem.template.equipment.equ.equipLevelUpSavePropType;
				if(type==4 || type==3){//材料升级时默认保留属性
					savePropTypeTemp=0;
				}else{
					ItemEnhance enhance=(ItemEnhance)gameItem.object;
					if(enhance==null){//没有属性变化时情况1.客户端不提示保留属性
						savePropTypeTemp=99;
					}else{//没有属性变化时情况2（当宝石拆除时，object不会变为空，只能比较数据是否有变化）
						byte[] itemSrc=enhance.toDBBytes();
						byte[] itemDes=(new ItemEnhance()).toDBBytes();
						if(Arrays.equals(itemSrc, itemDes)){
							savePropTypeTemp=99;
						}
					}
				}
				pt.put(savePropTypeTemp);
				ShopService shopService=Server.server.getServiceRegistry().getShopService();
				int itemMoney=Math.round(shopService.getItemPrice(LEVELUP_PRICE_SAVEPROP)/36);
				pt.putInt(itemMoney);//gameItem.template.equipment.equ.equipLeveUpSavePropPriceNum);
				String explain=MessageFormat.format(this.equipLevelUpExplain, itemMoney);
				if(nextLevelItem.template.equipment.minorType>=21){//暂时屏蔽马装
					explain=MessageFormat.format(this.equipLevelUpExplainHorse, itemMoney);
				}
				pt.putUTF(explain);
				p.send(pt);
			}else {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.EQUIPMENT_LEVELUP_ITEMINFO_CLIENT, peony.Messages.STRING_00173);
			}
		}
	}
	
	public boolean isGameItemHadSuite(GameItem item){
		if(item!=null){
			ItemTemplate itemT = ObjectAccessor.getItemTemplate(item.template.id);
			if(itemT.equipment.suiteEffects!=null){
				return true;
			}
		}
		return false;
	}
	
	public void processSuiteInfo(Packet pt,GameItem  item1){
		ItemTemplate item = ObjectAccessor.getItemTemplate(item1.template.id);
		EquipmentTemplate equipmentTemplate = item.equipment;
		pt.putString(equipmentTemplate.specialEffect == null ? ""
				: equipmentTemplate.specialEffect.getDesc());
		if (equipmentTemplate.suiteEffects == null) {
			pt.putString("");
			pt.put(0);
			pt.putString("");
			pt.put(0);
			pt.putString("");
			pt.put(0);
			pt.putString("");
			pt.put(0);
		} else {
			if(equipmentTemplate.suiteEffects.type==SuiteEffects.TYPE_NORMAL){
				pt.putString(equipmentTemplate.suiteEffects.getName());
				pt.put(1);
				pt.putString(item.name);
				pt.put(1);
				List<Integer> equips = equipmentTemplate.suiteEffects.getEquips();
				int total=equips.size();
				pt.putString(1 + "/" + 7);
				pt.put(1);
				SuiteEffect[] effects2 = equipmentTemplate.suiteEffects.getEffects();
				if(effects2.length==0){
					pt.putString(MessageFormat.format("套装({0}/{1}){2}", 1,7,"没有套装效果!"));
				}else{
					Buff bufTemp=BuffUtil.createSuiteBuff(effects2[0].buff.getId(),1);
					String sb = "";
					String[] desc=bufTemp.getDesc().split("；");
					int count=0;
					for(String str:desc){
						if(count!=0){
							sb+="；\n";
						}
						sb+=str;
						count++;
					}
					pt.putString(MessageFormat.format("套装({0}/{1})\n{2}", 1,7,sb.toString()));
				}
				pt.put(1);
			}else if(equipmentTemplate.suiteEffects.type==SuiteEffects.TYPE_WEIGHT){
				pt.putString(equipmentTemplate.suiteEffects.getName());
				pt.put(1);
				pt.putString(item.name);
				pt.put(1);
				List<Integer> equips = equipmentTemplate.suiteEffects.getEquips();
				int total=equips.size();
				pt.putString(1 + "/" + 7);
				pt.put(1);
				SuiteEffect[] effects2 = equipmentTemplate.suiteEffects.getEffects();
				if(effects2.length==0){
					pt.putString(MessageFormat.format("套装({0}/{1}){2}", 1,7,"没有套装效果!"));
				}else{
					int weight=equipmentTemplate.suiteEffects.weights.get(equipmentTemplate.equ.id);
					Buff bufTemp=BuffUtil.createSuiteBuff(equipmentTemplate.suiteEffects.getEffects()[0].buffId,1,weight);
					String sb = "";
					String[] desc=bufTemp.getDesc().split("；");
					String[] descOldEquip=null;
					if(oldEquipSuiteDesc.equals("")){//没有时说明是旧装备不需要比较
						oldEquipSuiteDesc=bufTemp.getDesc();
					}else{//如果是新装备才进行比较
						descOldEquip=oldEquipSuiteDesc.split("；");
					}
					int count=0;
					for(int i=0;i<desc.length;i++){
						String str=desc[i];
						if(desc!=null&&descOldEquip!=null&&desc.length==descOldEquip.length&&descOldEquip!=null&&Server.server.revision.equalsIgnoreCase(Server.REVISION_TYPE_PIP)){
							if(!str.equals(descOldEquip[i])){
								str=getComString(descOldEquip[i], str);
							}
						}
						if(count!=0){
							sb+="；\n";
						}
						sb+=str;
						count++;
					}
					pt.putString(MessageFormat.format("套装({0}/{1})\n{2}", 1,7,sb.toString()));
				}
				pt.put(1);
			}
		}
	}
	//  \D以非数字为表达式取所有数字   \d以数字为表达式取所有非数字
	public String getComString(String oldStr,String newStr){
		String redStr="";
	      String [] a = oldStr.split("[^.0-9]+");
	      String [] b = newStr.split("[^.0-9]+");
	      if(a.length==b.length){
	    	  for(int i=0;i<a.length;i++){
	    		  if(a[i].matches("[\\d|(\\.\\d+)?]+")&&b[i].matches("[\\d|(\\.\\d+)?]+")){
	    			  float aDigtal=Float.parseFloat(a[i]);
	    			  float bDigtal=Float.parseFloat(b[i]);
		    		  if(bDigtal>aDigtal){
		    			  b[i]="<cff0000>"+b[i]+"</c>";
		    		  }
	    		  }
	    	  }
	      }
	      String[] bDes=newStr.split("[\\d|(\\.\\d+)?]+");
	      if(!newStr.startsWith(newStr.split("[\\d|(\\.\\d+)?]+")[0])){//如果数字不是开头
	    	  for(int i=0;i<bDes.length;i++){
	    		  String bTempStr="";
	    		  if(i+1<b.length){
	    			  bTempStr=bDes[i]+b[i+1];
	    		  }else{
	    			  bTempStr=bDes[i];
	    		  }
	    		  redStr+=bTempStr;
	    	  }
	      }else{
	    	  for(int i=0;i<bDes.length;i++){
	    		  String bTempStr="";
	    		  if(i+1<b.length){
	    			  bTempStr=bDes[i]+b[i+1];
	    		  }else{
	    			  bTempStr=bDes[i];
	    		  }
	    		  redStr+=bTempStr;
	    	  }
	      }
	      return redStr;
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

}
