package peony.alchemy;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Random;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.chat.ChatService;
import peony.game.mail.MailService;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.shop.NoItemShopBuy;
import peony.service.shop.ShopService;

public class AlchemyCall extends ClientSessionAsyncCall{
	private static final Logger log = Logger.getLogger(AlchemyCall.class);
	public static final int ALCHEMY_EXP=0;//经验修炼
	public static final int ALCHEMY_BREAKLEVEL=1;//突破重天
	
	public static int IMONEY=5;
	public static int IMONEY100=500;
	
	protected Player player;
	protected int alchemyType;
	protected Packet packet;
	protected int serial;
	
	
	protected Random rnd=new Random();
	
	public int alchemyExpAdd;//修炼经验值
	public float attackPowerupAdd;//物攻
	public float spellPowerAdd;//法攻
	public float hpAdd;//生命
	public float defenseAdd;//护甲
	public float spellDefenseAdd;//法防
	public float jewelEnhanceAdd;//宝石光效
	
	public int breakLevelUseExp;//突破重天使用的经验
	
	public static final String chinaNums="一二三四五六七八九十";
	
	
	public AlchemyCall(ClientSession session,Packet packet,int alchemyType) {
		super(session);
		this.player=(Player)session.getClient();
		this.alchemyType=alchemyType;
		if(packet!=null){
			this.packet=packet;
			serial=this.packet.getInt();
		}
	}


	public void callFinish() throws Exception {
		
	}
	
	/**修炼计算*/
	public void alchemyedCurrentLevel(AlchemyLevelData alchemy){
		if(alchemy.acupointLevel>10){
			alchemy.acupointLevel=1;
			alchemy.acupointNum++;
			if(alchemy.acupointNum>8){
				alchemy.acupointNum=0;
				alchemy.pulseIndex++;
			}
			AlchemyService service=Server.server.getServiceRegistry().getAlchemyService();
			int needAlcExp=service.getCurrentAlchemyNeedExp(player.alchemy.practiceLevel, player.alchemy.pulseIndex);
			player.pool.setInt(AlchemyService.ALCHEMYEXP_USECALCULATE, player.pool.getInt(AlchemyService.ALCHEMYEXP_USECALCULATE, 0)-needAlcExp);
		}
	}
	/**人物经验修炼*/
	public void alchemyProcess(int type){
		synchronized (player.alchemy) {
			this.alchemyExpAdd=0;
			AlchemyService service=Server.server.getServiceRegistry().getAlchemyService();
			int alchemyExp=AlchemyService.ALCHEMY_EXP_ONCE;
			int alchemyCount=0;
			if(type==ALCHEMY_EXP&&packet!=null){//使用修炼经验丹时不减少修炼次数和人物经验
				int playerNeedExp=service.getDecPlayerExp(player.level);
				PlayerTransaction tx=player.newTransaction("ALCHEMYBYPLAYEREXP");
				player.decExp(playerNeedExp, tx, false);//真正减少经验
				tx.commit();
				//减少用于计算今天增加的值
				long playerexp=player.pool.getLong(AlchemyService.PLAYEREXP_TODAYADD,0)-playerNeedExp;
				player.pool.setLong(AlchemyService.PLAYEREXP_TODAYADD, playerexp<=0?0:playerexp);
				player.pool.setInt(AlchemyService.ALCHEMYCOUNT_PLAYEREXP, player.pool.getInt(AlchemyService.ALCHEMYCOUNT_PLAYEREXP)-1);//每次登录时赋值
				player.alchemy.alchemyCount=player.pool.getInt(AlchemyService.ALCHEMYCOUNT_PLAYEREXP);
				alchemyCount=1;
				if(player.alchemy.alchemyCount<=0){
					player.alchemy.alchemyCount=0;
				}
			}else if(packet == null){
				alchemyCount=1;
			}
			log.info("[ALCHEMYSTART]TYPE["+type+"]ALCHEMYCOUNT["+alchemyCount+"]PLAYERNAME["+player.name+"]ACC["+player.accountId+"]ID["
					+player.id+"]faction["+player.faction+"]PRACTICELEVEL["+player.alchemy.practiceLevel+
					"]PULSEINDEX["+player.alchemy.pulseIndex+
					"]ACUPOINTNUM["+player.alchemy.acupointNum+
					"]ACUPOINTLEVEL["+player.alchemy.acupointLevel+
					"]alchemyExp["+player.alchemy.alchemyExp+
					"]restExp["+player.alchemy.restExp+"]"
			);
			
			for(int i=0;i<alchemyCount;i++){
				this.alchemyExpAdd+=alchemyExp;//本次修炼获得的修炼经验
				//保存修炼经验值
				player.pool.setInt(AlchemyService.ALCHEMYEXP, player.pool.getInt(AlchemyService.ALCHEMYEXP,0)+alchemyExp);
				player.pool.setInt(AlchemyService.ALCHEMYEXP_USECALCULATE, player.pool.getInt(AlchemyService.ALCHEMYEXP_USECALCULATE,0)+alchemyExp*10);
				//处理升级
				int playerAlchemyExp=player.pool.getInt(AlchemyService.ALCHEMYEXP_USECALCULATE);
				int needAlcExp=service.getCurrentAlchemyNeedExp(player.alchemy.practiceLevel, player.alchemy.pulseIndex)/*/10*/;
				
				if(!(player.alchemy.acupointNum==8&&player.alchemy.practiceLevel==player.alchemy.pulseIndex&&player.alchemy.acupointLevel==10)){
					if(playerAlchemyExp>=needAlcExp){//如果可以升级穴位
						player.alchemy.acupointLevel++;
						//修炼经验值相应减少
						if(player.alchemy.acupointLevel<11){
							player.pool.setInt(AlchemyService.ALCHEMYEXP_USECALCULATE, player.pool.getInt(AlchemyService.ALCHEMYEXP_USECALCULATE, 0)-needAlcExp);
						}
						if(player.alchemy.acupointLevel==10){
							player.pool.setInt(AlchemyService.ALCHEMYEXP, player.pool.getInt(AlchemyService.ALCHEMYEXP)-service.getCurrentAlchemyNeedExp(player.alchemy.practiceLevel, player.alchemy.pulseIndex));
						}
						alchemyedCurrentLevel(player.alchemy);
					}
				}else{
					player.alchemy.restExp=player.pool.getInt(AlchemyService.ALCHEMYEXP);//留存经验
				}
			}
			
			log.info("[ALCHEMYEND]TYPE["+type+"]ALCHEMYCOUNT["+alchemyCount+"]PLAYERNAME["+player.name+"]ACC["+player.accountId+"]ID["
					+player.id+"]faction["+player.faction+"]PRACTICELEVEL["+player.alchemy.practiceLevel+
					"]PULSEINDEX["+player.alchemy.pulseIndex+
					"]ACUPOINTNUM["+player.alchemy.acupointNum+
					"]ACUPOINTLEVEL["+player.alchemy.acupointLevel+
					"]alchemyExp["+player.alchemy.alchemyExp+
					"]restExp["+player.alchemy.restExp+"]"
			);
			player.refreshProperties(false);
			if(packet!=null){
				updateAlchemyData(type);
			}
		}
	}
	
	/**更新修炼数据*/
	public void updateAlchemyData(int type){
		float attackPowerupOld=player.alchemy.attackPowerup;//物攻
		float spellPowerOld=player.alchemy.spellPower;//法攻
		float hpOld=player.alchemy.hp;//生命
		float defenseOld=player.alchemy.defense;//护甲
		float spellDefenseOld=player.alchemy.spellDefense;//法防
		float jewelEnhanceOld=player.alchemy.jewelEnhance;//宝石光效
		
		int practiceLevel=player.alchemy.practiceLevel;
		int pulseIndex=player.alchemy.pulseIndex;
		int acupointNum=player.alchemy.acupointNum;
		int acupointLevel=player.alchemy.acupointLevel;
		
		switch(player.alchemy.practiceLevel){
		case 4:
			player.alchemy.jewelEnhance=AlchemyService.getProperties_Value(AlchemyService.JEWEL, practiceLevel, pulseIndex, acupointNum, acupointLevel);
		case 3:
			player.alchemy.spellDefense=AlchemyService.getProperties_Value(AlchemyService.SPELLDEF, practiceLevel, pulseIndex, acupointNum, acupointLevel);
		case 2:
			player.alchemy.defense=AlchemyService.getProperties_Value(AlchemyService.DEFENSE, practiceLevel, pulseIndex, acupointNum, acupointLevel);
		case 1:
			player.alchemy.hp=AlchemyService.getProperties_Value(AlchemyService.HP, practiceLevel, pulseIndex, acupointNum, acupointLevel);
		case 0:
			player.alchemy.attackPowerup=AlchemyService.getProperties_Value(AlchemyService.ATTACK, practiceLevel, pulseIndex, acupointNum, acupointLevel)
										+AlchemyService.BREAKLEVEL_ADDATTACK*practiceLevel;
			player.alchemy.spellPower=AlchemyService.getProperties_Value(AlchemyService.SPELLATTACK, practiceLevel, pulseIndex, acupointNum, acupointLevel)
										+AlchemyService.BREAKLEVEL_ADDSPELLPOWER*practiceLevel;
			break;
		}
		
		this.attackPowerupAdd=player.alchemy.attackPowerup-attackPowerupOld;
		this.spellPowerAdd=player.alchemy.spellPower-spellPowerOld;
		this.hpAdd=player.alchemy.hp-hpOld;
		this.defenseAdd=player.alchemy.defense-defenseOld;
		this.spellDefenseAdd=player.alchemy.spellDefense-spellDefenseOld;
		this.jewelEnhanceAdd=player.alchemy.jewelEnhance-jewelEnhanceOld;
		
		
		short opcode=OpCode.ALCHEMY_BYPLAYEREXP_SERVER;
		if(type==ALCHEMY_EXP){
			opcode=OpCode.ALCHEMY_BYPLAYEREXP_SERVER;
		}else if(type==ALCHEMY_BREAKLEVEL){
			opcode=OpCode.ALCHEMY_BREAKLEVEL_SERVER;
		}
		ChatService cs = Server.server.getServiceRegistry().getChatService();
		if(player!=null&&type==ALCHEMY_BREAKLEVEL){
			cs.sendWorldMessage(MessageFormat.format("恭喜{0}突破到了{1}重天，开启了全新的修炼历程，并额外获得了巨大的属性提升。", player.name,chinaNums.charAt(player.alchemy.practiceLevel)));
		}	
		//发送5重天称号奖励
		if(player.alchemy!=null&&player.alchemy.practiceLevel==4&&player.alchemy.pulseIndex==4&&player.alchemy.acupointNum==8&&player.alchemy.acupointLevel==10){
			MailService mailService=Server.server.getServiceRegistry().getMailService();
			GameItem  titlesItem= ObjectAccessor.createGameItem(AlchemyService.ALCHEMY_TITLE);
			mailService.sendSystemMail(player.id, peony.Messages.STRING_00004, "人物修炼奖励称号", "恭喜您打通了所有的经脉，这是给您的奖励。", 0,
					titlesItem, 1, "ALCHEMYTITLE");
			if(player!=null){
				cs.sendWorldMessage(MessageFormat.format("恭喜{0}修炼达到了五重天的境界获得了百炼金身的称号，在成为强者的路上又前进了一大步", player.name));
				cs.sendPrivateMessage(player.id,"恭喜你完成了所有经脉修炼，给你的奖励已发放飞鸽，请注意查收。");
			}
			log.info("[ALCHEMYTITLESEND]PLAYERID["+player.id+"]ACC["+player.accountId+"]");
		}
		sendAlchemyCode(opcode);
	}
	
	/**突破重天*/
	public void alchemyProcess_BreakLevel(){
		synchronized (player.alchemy) {
			if(player.alchemy.practiceLevel<4&&player.alchemy.practiceLevel==player.alchemy.pulseIndex&&player.alchemy.acupointLevel==10&&player.alchemy.acupointNum==8
					&&player.bag.getGameItemCount(AlchemyService.ALCHEMYBREAKLEVELITEM)>0
					&&!player.alchemy.levelBreak[player.alchemy.practiceLevel]){
				breakLevelUseExp=player.alchemy.restExp;
				PlayerTransaction tx = player.newTransaction("BREAKLEVELREMOVEITEM");
				GameItem  item=player.bag.removeGameItemIngoreInstanceId(AlchemyService.ALCHEMYBREAKLEVELITEM, 1, tx, true);
				if(item!=null)
					tx.commit();
				else
					tx.rollback();
				player.alchemy.levelBreak[player.alchemy.practiceLevel]=true;
				player.alchemy.practiceLevel++;
				player.alchemy.pulseIndex=0;
				player.alchemy.acupointNum=0;
				player.alchemy.acupointLevel=0;
				
				log.info("[BREAKLEVELSTART]PLAYERNAME["+player.name+"]ACC["+player.accountId+"]ID["
						+player.id+"]faction["+player.faction+"]PRACTICELEVEL["+player.alchemy.practiceLevel+
						"]PULSEINDEX["+player.alchemy.pulseIndex+
						"]ACUPOINTNUM["+player.alchemy.acupointNum+
						"]ACUPOINTLEVEL["+player.alchemy.acupointLevel+
						"]alchemyExp["+player.alchemy.alchemyExp+
						"]restExp["+player.alchemy.restExp+"]");
				
				//下一重天自动升级到可以升级到的穴位
				AlchemyService service=Server.server.getServiceRegistry().getAlchemyService();
				int needAlcExp=service.getCurrentAlchemyNeedExp(player.alchemy.practiceLevel, player.alchemy.pulseIndex)/*/10*/;//每级需要的修炼经验
				int alchemyExpCalc=player.pool.getInt(AlchemyService.ALCHEMYEXP_USECALCULATE, 0);
				int alchemyCount=0;
				for(;alchemyExpCalc>=needAlcExp;){
					alchemyCount++;
					//处理升级
					int playerAlchemyExp=player.pool.getInt(AlchemyService.ALCHEMYEXP_USECALCULATE);
					if(!(player.alchemy.acupointNum==8&&player.alchemy.practiceLevel==player.alchemy.pulseIndex&&player.alchemy.acupointLevel==10)){
						if(playerAlchemyExp>=needAlcExp){//如果可以升级穴位
							player.alchemy.acupointLevel++;
							//修炼经验值相应减少
							if(player.alchemy.acupointLevel<11){
								player.pool.setInt(AlchemyService.ALCHEMYEXP_USECALCULATE, player.pool.getInt(AlchemyService.ALCHEMYEXP_USECALCULATE, 0)-needAlcExp);
							}
							if(player.alchemy.acupointLevel==10){
								player.pool.setInt(AlchemyService.ALCHEMYEXP, player.pool.getInt(AlchemyService.ALCHEMYEXP)-service.getCurrentAlchemyNeedExp(player.alchemy.practiceLevel, player.alchemy.pulseIndex));
							}
							alchemyedCurrentLevel(player.alchemy);
							alchemyExpCalc=player.pool.getInt(AlchemyService.ALCHEMYEXP_USECALCULATE, 0);
							needAlcExp=service.getCurrentAlchemyNeedExp(player.alchemy.practiceLevel, player.alchemy.pulseIndex);
						}
					}else{
						break;
					}
				}
				if(player.alchemy.acupointNum==8&&player.alchemy.practiceLevel==player.alchemy.pulseIndex&&player.alchemy.acupointLevel==10){
					player.alchemy.restExp=player.pool.getInt(AlchemyService.ALCHEMYEXP);//留存经验
				}else{
					player.alchemy.restExp=0;
				}
				player.refreshProperties(false);
				log.info("[BREAKLEVELEND]ALCHEMYCOUNT["+alchemyCount+"]PLAYERNAME["+player.name+"]ACC["+player.accountId+"]ID["
						+player.id+"]faction["+player.faction+"]PRACTICELEVEL["+player.alchemy.practiceLevel+
						"]PULSEINDEX["+player.alchemy.pulseIndex+
						"]ACUPOINTNUM["+player.alchemy.acupointNum+
						"]ACUPOINTLEVEL["+player.alchemy.acupointLevel+
						"]alchemyExp["+player.alchemy.alchemyExp+
						"]restExp["+player.alchemy.restExp+"]");
				updateAlchemyData(ALCHEMY_BREAKLEVEL);
			}else if(player.alchemy.practiceLevel==4){
				ErrorHandler.sendErrorMessage(session, serial, OpCode.ALCHEMY_BREAKLEVEL_CLIENT, "您已经打通了全部五重天，无需继续突破。");
			}else{
				ErrorHandler.sendErrorMessage(session, serial, OpCode.ALCHEMY_BREAKLEVEL_CLIENT, "您还没有达到突破本重天的条件!");
			}
		}
	}
	

	public void run() {
		if(alchemyType==ALCHEMY_EXP){
			try {
				alchemyProcess(ALCHEMY_EXP);
			} catch (Exception e) {
				String err=e.toString();
				ErrorHandler.sendErrorMessage(session, serial, OpCode.ALCHEMY_BYPLAYEREXP_CLIENT, err);
			}
		}else if(alchemyType==ALCHEMY_BREAKLEVEL){
			try {
				alchemyProcess_BreakLevel();
			} catch (Exception e) {
				String err=e.toString();
				ErrorHandler.sendErrorMessage(session, serial, OpCode.ALCHEMY_BREAKLEVEL_CLIENT, err);
			}
		}
		addToClientSession();
	}
	
	public void sendAlchemyCode(short opcode){
		Packet pt = new Packet(opcode);
		pt.putInt(serial);
		pt.put(player.alchemy.practiceLevel);//重天数
		pt.put(player.alchemy.pulseIndex);//脉数
		pt.put(player.alchemy.acupointNum);//穴位
		pt.put(player.alchemy.acupointLevel);//穴位等级
		//已经修炼的经验值
		int alchemyExp=player.pool.getInt(AlchemyService.ALCHEMYEXP);
		if((!player.alchemy.levelBreak[player.alchemy.practiceLevel])&&
				player.alchemy.acupointNum==8&&player.alchemy.practiceLevel==player.alchemy.pulseIndex&&player.alchemy.acupointLevel==10){
			alchemyExp=0;
		}
		pt.putInt(alchemyExp);
		AlchemyService service = Server.server.getServiceRegistry().getAlchemyService();
		int needExp=service.getCurrentAlchemyNeedExp(player.alchemy.practiceLevel, player.alchemy.pulseIndex);
		pt.putInt(needExp);//当前需要总经验
		
		//字符串发送UI左面显示信息
		long needPlayerExp=0;//下一级需要人物经验
		long playerExpToday=player.pool.getLong(AlchemyService.PLAYEREXP_TODAYADD, 0);
		if(playerExpToday<service.getDecPlayerExp(player.level)){
			needPlayerExp=service.getDecPlayerExp(player.level)-playerExpToday;
		}
		if(player.alchemy.alchemyCount<=0){
			needPlayerExp=service.getDecPlayerExp(player.level);
		}
		
		int restExp=player.alchemy.restExp;//留存经验逻辑，先显示修炼总经验
		pt.putInt(restExp);
		int alchemyCount=player.alchemy.alchemyCount;//修炼剩余次数
		int attackPowerup=(int)player.alchemy.attackPowerup;//物攻
		int spellPower=(int)player.alchemy.spellPower;//法攻
		int hp=(int)player.alchemy.hp;//生命
		int defense=(int)player.alchemy.defense;//护甲
		int spellDefense=(int)player.alchemy.spellDefense;//法防
		NumberFormat numberFormat = NumberFormat.getInstance();
		numberFormat.setMaximumFractionDigits(3);
		String jewelEnghance=numberFormat.format(player.alchemy.jewelEnhance);//宝石光效
		
		String info0=AlchemyService.alchemyPropertyChangeInfo;
		if(!(player.alchemy.practiceLevel==4&&player.alchemy.pulseIndex==4&&player.alchemy.acupointNum==8&&player.alchemy.acupointLevel==10)){
			info0+=AlchemyService.alchemyByPlayerExpInfo;
		}
		ShopService shopService=Server.server.getServiceRegistry().getShopService();
		int itemMoney=Math.round(shopService.getItemPrice(NoItemShopBuy.WUYUANBAO))/36;
		String info=MessageFormat.format(info0,
										 attackPowerup+"",spellPower+"",
										 defense+"",spellDefense+"",
										 hp+"",jewelEnghance,
										 itemMoney+"",(itemMoney*100)+"",
										 needPlayerExp+"",alchemyCount+""
										 );
		pt.putUTF(info);
			StringBuffer changeInfo=new StringBuffer();
			if(this.alchemyType==ALCHEMY_BREAKLEVEL){
				int useExp=breakLevelUseExp-player.pool.getInt(AlchemyService.ALCHEMYEXP);
				if(useExp>0){
					changeInfo.append(MessageFormat.format("使用了留存经验{0}\n",useExp+""));
				}
			}else{
				changeInfo.append(MessageFormat.format("本次修炼为您增长了{0}点修炼经验！\n",this.alchemyExpAdd+""));
			}
			if(this.attackPowerupAdd>0||this.spellPowerAdd>0||this.hpAdd>0||this.defenseAdd>0||this.spellDefenseAdd>0||this.jewelEnhanceAdd!=0){
				changeInfo.append("修炼使你的功力大涨，为你增加了以下属性：\n");
			}
			if(this.attackPowerupAdd>0){
				changeInfo.append("物攻：<cff0000>+"+(int)this.attackPowerupAdd+"</c>\n");
			}
			if(this.spellPowerAdd>0){
				changeInfo.append("法攻：<cff0000>+"+(int)this.spellPowerAdd+"</c>\n");
			}
			if(this.hpAdd>0){
				changeInfo.append("生命值：<cff0000>+"+(int)this.hpAdd+"</c>\n");
			}
			if(this.defenseAdd>0){
				changeInfo.append("护甲：<cff0000>+"+(int)this.defenseAdd+"</c>\n");
			}
			if(this.spellDefenseAdd>0){
				changeInfo.append("法防：<cff0000>+"+(int)this.spellDefenseAdd+"</c>\n");
			}
			if(this.jewelEnhanceAdd!=0){
				String jewelAdd=numberFormat.format(this.jewelEnhanceAdd);
				changeInfo.append("宝石效果：<cff0000>+"+jewelAdd+"%</c>\n");
			}
			
			if(this.alchemyType==ALCHEMY_BREAKLEVEL){
				changeInfo.append("其中包含突破重天为您额外增加了<cff0000>150</c>点物攻和<cff0000>100</c>点法攻。\n");
			}
			
			pt.putUTF(changeInfo.toString());
//		}
		player.send(pt);
		
		//当天不再提示突破重天
		if(player.pool.getInt(AlchemyService.ALCHEMY_HINT_TODAY, 0)==0){
			player.pool.setInt(AlchemyService.ALCHEMY_HINT_TODAY, -1);
		}
	}

}
