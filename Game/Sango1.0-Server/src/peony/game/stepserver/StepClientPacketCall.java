package peony.game.stepserver;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.mina.common.RuntimeIOException;

import peony.common.ClientSessionAsyncCall;
import peony.game.Creature;
import peony.game.Equipments;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.GameObject;
import peony.game.Horse;
import peony.game.NoEnoughValueException;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Skills;
import peony.game.Time;
import peony.game.TransactionBagGrid;
import peony.game.VMap;
import peony.game.asyncbattle.AsyncBattleService;
import peony.game.attendant.Attendant;
import peony.game.attendant.AttendantFixService;
import peony.game.buff.BuffUtil;
import peony.game.changed.BagChangedItem;
import peony.game.changed.ChangedItem;
import peony.game.changed.EquipChangedItem;
import peony.game.chat.ChatService;
import peony.game.itemeffect.KingItemEffect;
import peony.game.mail.MailService;
import peony.game.nation.CandidateService;
import peony.net.AbstractClientSession;
import peony.net.ClientSession;
import peony.net.DispatchClientSession;
import peony.net.DispatchPacket;
import peony.net.Packet;

public class StepClientPacketCall extends ClientSessionAsyncCall {
	
	public static final Logger log = Logger.getLogger(StepClientPacketCall.class);

	/**发送通知给玩家*/
	public static int TYPE_NOTIFYFINALSPLAYER=12;
	
	/**世界聊通知争霸赛公告*/
	public static int TYPE_BATTLE_FINALS_CHAT=13;
	
	/**通知所有支持者私聊*/
	public static int TYPE_NOTYFYPLAYERSBACKER=14;
	
	/**跨服检测本地玩家是否在线的*/
	public static int TYPE_CHECKPLAYERONLINE_STEPSERVER=15;
	
	/**本地服务器返回给跨服服务器*/
	public static short TYPE_CHECKPLAYERONLINE_STEPCLIENT=16;
	
	protected DispatchClientSession disSession;
	protected DispatchPacket dp;
	
	public StepClientPacketCall(ClientSession session, DispatchClientSession disSession, DispatchPacket dp) {
		super(session);
		this.disSession = disSession;
		this.dp = dp;
	}

	public void callFinish() throws Exception {
		processSpecialPacket(disSession, dp);
	}

	public void run() {
		addToClientSession();
	}
	
	public void processSpecialPacket(DispatchClientSession disSession, DispatchPacket dp){
		if(disSession!=null){
			if(dp.packet.getOpCode()==OpCode.STEPSERVER_BATTLE_SIGNUP_SERVER){
				//返回跨服的报名成功协议
				dp.packet.data.flip();
				int canSign = dp.packet.getInt();
				Player player = (Player)disSession.getClient();
				if(canSign==StepServer.PACKET_SIGN_1){
					if(player!=null){
						Server.server.getServiceRegistry().getStepClient().send(player, player.accountId, player.id, player.session);
						player.message(-1, "报名成功", -1, -1);
					}
				}else{
					int cause = dp.packet.get();
					if(cause==StepServer.PACKET_CAUSE_SIGN_1){
						if(player!=null)
							player.message(-1, "现在不是报名时间", -1, -1);
					}else if(cause==StepServer.PACKET_CAUSE_SIGN_2){
						if(player!=null)
							player.message(-1, "您已经报过名了", -1, -1);
					}else if(cause==StepServer.PACKET_CAUSE_SIGN_3){
						if(player!=null)
							player.message(-1, "您的级别不足", -1, -1);
					}else if(cause==StepServer.PACKET_CAUSE_SIGN_4){
						if(player!=null){
							if(player.stepType==StepServer.STEPBATTLE_TYPE_NORMAL){
								player.message(-1, "您今天已经报过名了，请改天再来", -1, -1);
							}else{
								player.message(-1, "您已报名，请耐心等候。", -1, -1);
							}
						}
					}else if(cause==StepServer.PACKET_CAUSE_SING_OVER3TIMES){
						if(player!=null)
							player.message(-1, "每日仅可参加三次跨服常规赛！", -1, -1);
					}else if(cause==StepServer.PACKET_CAUSE_SING_NOTTOP16){
						if(player!=null)
							player.message(-1, "常规赛已经结束；争霸赛结束后将开启下一赛季。", -1, -1);
					}else if(cause==StepServer.PACKET_CAUSE_SING_NOFINALS16){
						if(player!=null)
							player.message(-1, "常规赛晋级者方可参加争霸战，您未获得资格，还请下赛季再接再厉！", -1, -1);
					}
//					if(player!=null && player.stepType!=StepServer.STEPBATTLE_TYPE_16 && player.stepType!=StepServer.STEPBATTLE_TYPE_TOURNAMENT){
//						Server.server.getServiceRegistry().getStepSessionService().removeAllCachSession(player.accountId, player.id);
//					}
				}
			}else if(dp.packet.getOpCode()==OpCode.STEPSERVER_CLIENTSERVER_INFO_SERVER){
				dp.packet.data.flip();
				int type = dp.packet.getInt();
				String value = dp.packet.getString();
				if(type==StepServer.TYPE_BATTLE_END){
					Player player = (Player)disSession.getClient();
					if(player!=null){
						player.isInStep = false;
						player.minorFaction = 0;
						Server.server.getServiceRegistry().getStepSessionService().removeAllCachSession(player.accountId, player.id);
						sendGift(player, value);
						try {
							player.systemState = Player.SYSTEMSTATE_READY;
							if(player.reliveOptions!=null){
								player.reliveOptions.clearOptions();
								player.reliveOptions = null;
							}
							player.lastPosition = null;
							int[] out = StepClient.outs[player.faction-1];
							if(player.stepType==StepServer.STEPBATTLE_TYPE_16||player.stepType==StepServer.STEPBATTLE_TYPE_TOURNAMENT){
								out=StepClient.outs_16[player.faction-1];
							}
							player.mapCell.addGameObject(player);
							player.moveType = Player.MOVE_ALL;
							player.state &= Player.MASK_CLEAR;
							player.addIntPropertyChangedItem(ChangedItem.STATE, player.state, false, true);
//							player.addIntPropertyChangedItem(ChangedItem.HEAD_SCORE,player.head_score,player.head_score,false);
//							player.addIntPropertyChangedItem(ChangedItem.BODY_SCORE,player.body_score,player.body_score,false);
//							player.addIntPropertyChangedItem(ChangedItem.WEAPON_SCORE,player.weapon_score,player.weapon_score,false);
//							player.addIntPropertyChangedItem(ChangedItem.FLASHLEVEL,player.flashLevel,player.flashLevel,false);
							player.stepSafeTime = Time.currTime + 600000;
							player.goMap(out[0], out[1], out[2]);
							CandidateService candidateService = Server.server.getServiceRegistry().getCandidateService();
							player.buffs.clear();
							//特殊处理坐骑天命套装效果579BUFF
							player.buffs.removeBuff(Horse.jewelBuffId);
							if(player.horse!=null){
								player.horse.processRideBuff(player);
							}
							player.skills.removeSkill(candidateService.getKingSkillGroupId(player.clazz), 1);
							player.skills.removeSkill(candidateService.getKingSkillGroupId(player.clazz), 0);
							if (player.isKing() == 1) {
								player.buffs.addBuff(BuffUtil.createSuiteBuff(216, 1));
								if(ObjectAccessor.getSkill(Skills.getSkillId(candidateService.getKingSkillGroupId(player.clazz), 1))!=null)
										player.addSkill(ObjectAccessor.getSkill(Skills.getSkillId(candidateService.getKingSkillGroupId(player.clazz), 1)));
								 KingItemEffect.isKing(player);
							}
							player.initBuffs();
//							player.loadFinished();
							if(player.getVMap()!=null && player.getVMap().getId()==out[0]){
								List<Creature> creatures = player.getVMap().getAllCreatures();
								for(Creature c : creatures){
									if(c!=null){
										VMap.notifyAppear(c);
									}
								}
							}
							for(GameItem equ : player.equipments.equs){
								if(equ!=null && equ.template!=null && equ.template.equipment!=null){
									int index = Equipments.getIndex(equ.template.equipment.minorType);
									if(player.changed!=null){
										EquipChangedItem changedItem = new EquipChangedItem(index,equ);
										player.changed.addChangedItem(changedItem);
									}
								}
							}
							for(TransactionBagGrid grid : player.bag.getGrids()){
								if(grid!=null){
									BagChangedItem change = new BagChangedItem(grid, grid.getCount());
									if(player.changed!=null)
										player.changed.addChangedItem(change);
								}
							}
							int lastHorseInstId = player.pool.getInt(Player.PROPERTY_LAST_HORSE_INSTANCEID);
							if(lastHorseInstId != 0){
								player.horseRide(lastHorseInstId, 0,-1);
							}
							int attendantInstanceId = player.pool.getInt(Player.PROPERTY_LAST_ATTENDANT_INSTANCEID);
							if(attendantInstanceId!=0){
								Attendant attendant = player.attendantBag.getAttendant(attendantInstanceId);
								if(attendant!=null){
									attendant.follow();
									AttendantFixService attFixService = Server.server.getServiceRegistry().getAttendantFixService();
									attFixService.addBuffOnFollow(player, attendant);
								}
							}
							player.refreshProperties(false);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}else if(type==StepServer.TYPE_BATTLE_START){
					Player player = (Player)disSession.getClient();
					if(player!=null){
						player.isInStep = true;
						player.mapCell.removeGameObject(player);
						if(player.attendant!=null){
							player.attendant.mapCell.removeGameObject(player.attendant);
							VMap.notifyDisappear(player.attendant);
						}
					}
				}else if(type==StepServer.TYPE_BATTLE_BAGCHANGE){
					Player player = (Player)disSession.getClient();
					if(player!=null){
						int itemId = Integer.parseInt(value);
						PlayerTransaction tx = player.newTransaction("STEPBATTLE");
						player.bag.removeGameItemIngoreInstanceId(itemId, 1, tx, false);
						tx.commit();
					}
				}else if(type==StepServer.TYPE_BATTLE_HP){
					Player player = (Player)disSession.getClient();
					if(player!=null)
						player.setHp(Integer.parseInt(value), false);
				}else if(type==StepServer.TYPE_BATTLE_MP){
					Player player = (Player)disSession.getClient();
					if(player!=null)
						player.setMp(Integer.parseInt(value), false);
				/*}else if(type==StepServer.TYPE_FINALBATTLE_SENDREWARD){//发送押注胜利的金币
					MailService mailService=Server.server.getServiceRegistry().getMailService();
					int playerSize=dp.packet.getInt();
					for(int i=0;i<playerSize;i++){
						int playerId=dp.packet.getInt();
						String gameCode=dp.packet.getString();
						int betCoins=dp.packet.getInt();
						int winCoins=dp.packet.getInt();
						if(gameCode.equals(Server.server.gameCode)){//如果是本服务器玩家发送奖励
							if(betCoins==0){//本金为0时表示比赛还没有结束，只返还赢得金钱
								if(winCoins>0){
									winCoins=(int)(winCoins*StepBattleService.returnBetPercentage);
									mailService.sendSystemMail(playerId, peony.Messages.STRING_00004, "争霸赛奖励", MessageFormat.format("您赢得争霸赛资金{0}", winCoins), winCoins,
											null, 0, "STEPBATTLEREWARD");
								}
							}else{//争霸赛结束后返回本金
								mailService.sendSystemMail(playerId, peony.Messages.STRING_00004, "争霸赛押注返还", "恭喜您押注的选手一路披荆斩棘获得冠军。特将本金归还。", betCoins,
										null, 0, "STEPBATTLEREWARD");
							}
						}
					}*/
				}else if(type==TYPE_NOTIFYFINALSPLAYER){//私聊通知玩家
					Player player = (Player)disSession.getClient();
					if(player!=null){
						ChatService cs = Server.server.getServiceRegistry().getChatService();
						cs.sendPrivateMessage(player.id,value);
					}
				}
			}else if(dp.packet.getOpCode()==OpCode.STEPSERVER_BATTLE_SCORE_SERVER){
				Player player = ObjectAccessor.getPlayer(dp.playerId);
				if(player!=null && !player.isInStep)
					Server.server.getServiceRegistry().getStepSessionService().removeAllCachSession(dp.accountId, dp.playerId);
			}else if(dp.packet.getOpCode()==OpCode.OPENUI_SERVER){
				Server.server.getServiceRegistry().getStepClient().uiPackets.put(dp.playerId, dp);
			}else if(dp.packet.getOpCode()==OpCode.STEPBATTLE_FINALS_BETANDWATCH_SERVER){
				dp.packet.data.flip();
				int type = dp.packet.getInt();
				int betCoins=dp.packet.getInt();
				int targetPlayerId=dp.packet.getInt();//观战或押注的playerId
				int targetPlayerAccountId=dp.packet.getInt();//观战或押注的playerAccountId
				String sourcePlayerGameCode=dp.packet.getString();//押注的GameCode
				int hadBetCoins=dp.packet.getInt();//已经押注的金钱
				Player player = (Player)disSession.getClient();
				if(type==StepServer.STEPBATTLE_BETANDWATCH_LESSMONEY){
					if(player!=null)
						ErrorHandler.sendErrorMessage(player.session, 0, OpCode.STEPBATTLE_FINALS_BETANDWATCH_CLIENT, "您没有那么多金钱，还是谨慎投注吧！");
				}else if(type==StepServer.STEPBATTLE_BETANDWATCH_DECMONEYOK){
					Packet pt = new Packet(OpCode.STEPBATTLE_FINALS_BETANDWATCH_CLIENTTOSTEPSERVER);
					pt.putInt(0);
					pt.put(1);//1-押注
					pt.putInt(betCoins);//押注金额
					pt.putInt(targetPlayerId);//被押注的
					pt.putInt(targetPlayerAccountId);//被押注的
					pt.putUTF(sourcePlayerGameCode);//押注的GameCode
					pt.data.flip();
					if(player!=null){
						PlayerTransaction tx = player.newTransaction("STEPBETCOINS");
						try {
							player.decMoney(betCoins, tx, false);
							tx.commit();
							DispatchPacket dpt = new DispatchPacket(((AbstractClientSession)player.session).getId(), pt);
							dpt.accountId = player.accountId;
							dpt.playerId = player.id;
							Server.server.getServiceRegistry().getStepClient().send(dpt, player.accountId, player.id, player.session);
						} catch (NoEnoughValueException e) {
							e.printStackTrace();
						}
					}
					Packet pt1 = new Packet(OpCode.STEPBATTLE_FINALS_BETANDWATCH_SERVER);
					pt1.putInt(0);
					pt1.put(1);
					pt1.putInt(betCoins);
					pt1.putString("押注成功，祝您投注的选手取得良好成绩");
					player.send(pt1);
				}else if(type==StepServer.STEPBATTLE_BETANDWATCH_MORETHEN100WAN){
					if(player!=null){
						if(hadBetCoins<=(StepBattleService.maxBetCoins-100000)){
							ErrorHandler.sendErrorMessage(player.session, 0, OpCode.STEPBATTLE_FINALS_BETANDWATCH_CLIENT, MessageFormat.format("您还对可押注{0}金币", StepBattleService.maxBetCoins-hadBetCoins));
						}else{
							ErrorHandler.sendErrorMessage(player.session, 0, OpCode.STEPBATTLE_FINALS_BETANDWATCH_CLIENT, MessageFormat.format("请谨慎投资，不能超过{0}万", StepBattleService.maxBetCoins/10000));
						}
					}
				}else if(type==StepServer.STEPBATTLE_BETANDWATCH_CANWATCH){//可以预约观战
					Packet pt = new Packet(OpCode.STEPBATTLE_FINALS_BETANDWATCH_CLIENTTOSTEPSERVER);
					pt.putInt(0);
					pt.put(0);//0-观战
					pt.putInt(-1);//押注金额(观战时不传此值)
					pt.putInt(targetPlayerId);//被观战的
					pt.putInt(targetPlayerAccountId);//被观战的
					pt.putUTF(sourcePlayerGameCode);//观战的GameCode
					pt.data.flip();
					DispatchPacket dpt = new DispatchPacket(((AbstractClientSession)player.session).getId(), pt);
					dpt.accountId = player.accountId;
					dpt.playerId = player.id;
					Server.server.getServiceRegistry().getStepClient().send(dpt, player.accountId, player.id, player.session);
					if(player!=null)
						player.message(-1, "预约观战成功", -1, -1);
				}else if(type==StepServer.STEPBATTLE_BETANDWATCH_WATCHCHANGE){//是否更换观战对象
					
				}else if(type==StepServer.STEPBATTLE_BETANDWATCH_HADWATCH){//已经预约无需再约
					if(player!=null)
						ErrorHandler.sendErrorMessage(player.session, 0, OpCode.STEPBATTLE_FINALS_BETANDWATCH_CLIENT, "您已经预约过此人，无需再次预约");
				}else if(type==StepServer.STEPBATTLE_BET_CANNOTBET){
					if(player!=null)
						ErrorHandler.sendErrorMessage(player.session, 0, OpCode.STEPBATTLE_FINALS_BETANDWATCH_CLIENT, "常规战卧虎藏龙，未决出争霸资格之前，不得押注。");
				}
			}
		}else{
			processSignPacket(dp);
		}
	}
	
	public void processSignPacket(DispatchPacket dp){
		if(dp.packet.getOpCode()==OpCode.STEPSERVER_CLIENTSERVER_INFO_SERVER){
			dp.packet.data.flip();
			int type = dp.packet.getInt();
			if(type==StepServer.TYPE_BATTLE_CANSIGN 
					&& (Server.server.revision.equals(Server.REVISION_TYPE_PIP ) 
							|| Server.server.revision.equals(Server.REVISION_TYPE_TW)) 
							&& !Server.isAppSection){
				for (Player player : ObjectAccessor.players.values()) {
					if(player.level>= StepBattleService.minPlayerLevel){
						if(player.map.map.instance!=null)
							continue;
						if(player.map.id==AsyncBattleService.battleMap)
							continue;
						Packet pt = new Packet(OpCode.OPENUI_SERVER);
						pt.putString("ui_npc_dialog");
						pt.putString("STEPSERVER_BATTLE_ENTER");
						player.send(pt);                                                                                                                                                                                                                                                                                                             
					}
				}
			}else if(type==StepServer.TYPE_BATTLE_1V1_CANSIGN 
					&& Server.server.revision.equals(Server.REVISION_TYPE_PIP) 
					&& !Server.isAppSection){
				Server.server.getServiceRegistry().getChatService()
				.sendWorldMessage("单人跨服对抗赛已经开启，大家快去主城易大师处报名！");
			}else if(type==StepServer.TYPE_BATTLE_SENDGIFT){//发送奖励
				List<StepBattleScore> scores = new ArrayList<StepBattleScore>();
				String s = dp.packet.getString();
				int sendGiftType=dp.packet.getInt();
				int size = dp.packet.getByte();
				for(int i=0;i<size;i++){
					int playerId = dp.packet.getInt();
					int accountId = dp.packet.getInt();
					String gameCode = dp.packet.getString();
					StepBattleScore score = new StepBattleScore();
					score.playerid = playerId;
					score.accountId = accountId;
					score.gameCode = gameCode;
					scores.add(score);
				}
				for(StepBattleScore score : scores){
					if(score==null || !score.gameCode.equals(Server.server.gameCode))
						continue;
					if(sendGiftType==0){//普通奖励
						GameItem item = ObjectAccessor.createGameItem(StepBattleService.rewards_16);
							Server.server.getServiceRegistry().getMailService()
							.sendSystemMail(score.playerid, peony.Messages.STRING_00004, "跨服奖励", "恭喜您在一对一跨服对抗赛中取得优异成绩，这是您的战利品。希望您能再接再厉！", 0,
									item, 1, "SENDSTEPREWARDITEM");
					}else if(sendGiftType==1){//16强奖励
						GameItem item = ObjectAccessor.createGameItem(StepBattleService.rewards_16_End);
						Server.server.getServiceRegistry().getMailService()
						.sendSystemMail(score.playerid, peony.Messages.STRING_00004, "跨服奖励", "恭喜您在跨服常规赛中取得胜利，这是您的战利品。并且请您务必于下周日19时等待传送提示，准时参加跨服争霸战。", 0,
								item, 1, "SENDSTEPREWARDITEM2");
					}
				}
			}else if(type==StepServer.TYPE_NOTIFYFINALPLAYERS){
				dp.packet.getString();
				int playerSize=dp.packet.getInt();
				for(int i=0;i<playerSize;i++){
					int playerId=dp.packet.getInt();
					String gameCode=dp.packet.getString();
					if(gameCode.equals(Server.server.gameCode)){
						Packet pt = new Packet(OpCode.OPENUI_SERVER);
						pt.putString("ui_npc_dialog");
						pt.putString("STEPSERVER_CHALLENG_ENTER");
						Player player=ObjectAccessor.getPlayer(playerId);
						if(player!=null){//玩家在线
							player.send(pt); 
							log.info("[STEPNOTIFYPLAYERSIGNUP]PLAYERID["+playerId+"]");
						}
					}
				}
			}else if(type==StepServer.TYPE_SENDTITLE){//发送称号
				String title=dp.packet.getString();
				MailService mailService=Server.server.getServiceRegistry().getMailService();
				int playerId=dp.packet.getInt();
				String gameCode=dp.packet.getString();
				int itemId=dp.packet.getInt();
				if(gameCode.equals(Server.server.gameCode)){//是本地服务的player
					GameItem item = ObjectAccessor.createGameItem(itemId);
					mailService.sendSystemMail(playerId, peony.Messages.STRING_00004, title, "称号奖励", 0,
							item, 1, "STEPBATTLETITLE");
				}
			}else if(type==StepServer.TYPE_FINALBATTLE_SENDREWARD){//发送押注胜利的金币
				MailService mailService=Server.server.getServiceRegistry().getMailService();
				String a=dp.packet.getString();
				String winnerName=dp.packet.getString();
//				boolean noPlayerEnterBattle=a.endsWith("NOPLAYERENTER");//是否是返回所有人本金
				int playerSize=dp.packet.getInt();
				for(int i=0;i<playerSize;i++){
					int playerId=dp.packet.getInt();
					String gameCode=dp.packet.getString();
					int betCoins=dp.packet.getInt();
					int winCoins=dp.packet.getInt();
					if(gameCode.equals(Server.server.gameCode)){//如果是本服务器玩家发送奖励
						if(betCoins==0){//本金为0时表示比赛还没有结束，只返还赢得金钱
							if(winCoins>0){
								if(a.equals("DISCONNPLAYERS")){
									winCoins=(int)(winCoins*StepBattleService.returnBetPercentage);
									mailService.sendSystemMail(playerId, peony.Messages.STRING_00004, "争霸赛押注奖励", MessageFormat.format("恭喜您押注的选手{0}一路披荆斩棘获得冠军!您赢得额外奖金{1}", winnerName,winCoins), winCoins,
											null, 0, "STEPBATTLEREWARD");
								}else{
									winCoins=(int)(winCoins*StepBattleService.returnBetPercentage);
									mailService.sendSystemMail(playerId, peony.Messages.STRING_00004, "争霸赛押注奖励", MessageFormat.format("您所支持的{0}在上一场争霸赛中大获全胜!您赢得争霸赛奖金{1}", winnerName,winCoins), winCoins,
											null, 0, "STEPBATTLEWINNERREWARD");
								}
							}
						}else{//争霸赛结束后返回本金
							if(a.endsWith("NOPLAYERENTER")){
								mailService.sendSystemMail(playerId, peony.Messages.STRING_00004, "争霸赛押注奖励", MessageFormat.format("您押注的争霸赛参赛选手{0}未能准时参赛，特将本金归还。",winnerName), betCoins,
										null, 0, "STEPBATTLEREWARDOWNERBET");
							}else{
								mailService.sendSystemMail(playerId, peony.Messages.STRING_00004, "争霸赛押注奖励", MessageFormat.format("恭喜您押注的选手{0}一路披荆斩棘获得冠军。特将本金归还。",winnerName), betCoins,
										null, 0, "STEPBATTLEREWARDWINNERBET");
							}
						}
					}
				}
			}else if(type==TYPE_BATTLE_FINALS_CHAT
					&& Server.server.revision.equals(Server.REVISION_TYPE_PIP ) 
					&& !Server.isAppSection){
				String hint=dp.packet.getString();
				Server.server.getServiceRegistry().getChatService()
				.sendWorldMessage(hint);
			}else if(type==TYPE_NOTYFYPLAYERSBACKER){//通知所有玩家私聊及内容
				String a=dp.packet.getString();
				String hint=dp.packet.getString();
				int size=dp.packet.getInt();
				for(int i=0;i<size;i++){
					int playerId=dp.packet.getInt();
					String gameCode=dp.packet.getString();
					if(gameCode.equals(Server.server.gameCode)){//是当前服务器的玩家
						Player player = ObjectAccessor.getPlayer(playerId);
						if(player!=null){
							ChatService cs = Server.server.getServiceRegistry().getChatService();
							cs.sendPrivateMessage(player.id,hint);
						}
					}
				}
			}else if(type==TYPE_CHECKPLAYERONLINE_STEPSERVER){//检测本地玩家是否在线
				List<String> returnPlayerInfo=new ArrayList<String>();//所有争霸赛玩家
				List<String> onLinePlayerInfo=new ArrayList<String>();//在线玩家（用于建立session）
				
				int size=dp.packet.getInt();
				for(int i=0;i<size;i++){
					int playerId=dp.packet.getInt();
					String gameCode=dp.packet.getString();
					if(gameCode.equals(Server.server.gameCode)){//是当前服务器的玩家
						Player player = ObjectAccessor.getPlayer(playerId);
						String playerInfoTemp=playerId+","+gameCode+",";
						if(player!=null){//说明在线
							playerInfoTemp=playerInfoTemp+"1";
							returnPlayerInfo.add(playerInfoTemp);
							onLinePlayerInfo.add(playerInfoTemp);//统计在线玩家
							log.info("[TYPE_CHECKPLAYERONLINE]PLAYERID["+playerId+"]GAMECODE["+gameCode+"]ONLINE");
						}else{//不在线
							playerInfoTemp=playerInfoTemp+"-1";
							returnPlayerInfo.add(playerInfoTemp);
							log.info("[TYPE_CHECKPLAYEROFFLINE]PLAYERID["+playerId+"]GAMECODE["+gameCode+"]OFFLINE");
						}
					}
				}
				if(onLinePlayerInfo.size()>0){//本地服务器上在线的争霸赛玩家(只用于重新建立session)
					Packet pt = new Packet((short)-1);
					int returnSize=onLinePlayerInfo.size();
					pt.putInt(returnSize);
					for(String playerInfo:onLinePlayerInfo){
						String[] infos=playerInfo.split(",");
						if(infos!=null){
							int playerIdTemp=Integer.parseInt(infos[0]);
							Player tempPlayer=ObjectAccessor.getPlayer(playerIdTemp);
							DispatchPacket dpt = new DispatchPacket(((AbstractClientSession)tempPlayer.session).getId(), pt);
							dpt.accountId = tempPlayer.accountId;
							dpt.playerId = tempPlayer.id;
							Server.server.getServiceRegistry().getStepClient().send(tempPlayer, tempPlayer.accountId, tempPlayer.id, tempPlayer.session);
						}
					}
				}
				if(returnPlayerInfo.size()>0){//本地服务器上所有的争霸赛玩家状态
					Packet pt = new Packet(OpCode.STEPBATTLE_CHECKSTEPCLIENT_PLAYERSTATE_STEPCLIENT);
					int returnSize=returnPlayerInfo.size();
					pt.putInt(returnSize);
					for(String playerInfo:returnPlayerInfo){
						String[] infos=playerInfo.split(",");
						if(infos!=null){
							int playerIdTemp=Integer.parseInt(infos[0]);
							String gameCode=infos[1];
							int onlineState=Integer.parseInt(infos[2]);
							pt.putInt(playerIdTemp);
							pt.putUTF(gameCode);
							pt.putInt(onlineState);
						}
					}
					pt.data.flip();
					try {
						DispatchPacket dpt = new DispatchPacket(0, pt);
						Server.server.getServiceRegistry().getStepClient().futureOfPacket.getSession().write(dpt);
					} catch (RuntimeIOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public void sendGift(Player player, String value){
		if(player!=null&&!value.equals("")){
			ChatService chatService = Server.server.getServiceRegistry().getChatService();
			int gift = 1000000;
			if(value.equalsIgnoreCase("1")){
				chatService.sendPrivateMessage(player.id, "恭喜你在跨服战场中获得第一名,奖励1000000经验值");
				gift = 1000000;
				Server.server.getServiceRegistry().getChatService().sendWorldMessage(MessageFormat
						.format("{0}的{1}技压群雄，获得了跨服战场第一名", GameObject.FACTION_NAME[player.faction], player.name));
			}else if(value.equalsIgnoreCase("2")){
				chatService.sendPrivateMessage(player.id, "恭喜你在跨服战场中获得第二名,奖励700000经验值");
				gift = 700000;
			}else if(value.equalsIgnoreCase("3")){
				chatService.sendPrivateMessage(player.id, "恭喜你在跨服战场中获得第三名,奖励600000经验值");
				gift = 600000;
			}else if(value.equalsIgnoreCase("4")){
				chatService.sendPrivateMessage(player.id, "恭喜你在跨服战场中获得第四名,奖励500000经验值");
				gift = 500000;
			}else if(value.equalsIgnoreCase("5")){
				chatService.sendPrivateMessage(player.id, "恭喜你在跨服战场中获得第五名,奖励400000经验值");
				gift = 400000;
			}else if(value.equalsIgnoreCase("6")){
				chatService.sendPrivateMessage(player.id, "恭喜你在跨服战场中获得第六名,奖励300000经验值");
				gift = 300000;
			}else if(value.equalsIgnoreCase("7")){
				chatService.sendPrivateMessage(player.id, "恭喜你在跨服战场中获得第七名,奖励200000经验值");
				gift = 200000;
			}else if(value.contains(StepBattleService.STEPBATTLE_HINT_MORETHEN3ROUNDS)){
				String[] para=value.split("\\|");
				if(para[1].equalsIgnoreCase("0")){
					gift = player.level*3000;
					chatService.sendPrivateMessage(player.id, MessageFormat.format("恭喜您本次单人对抗赛取得胜利。获得{0}经验，请再接再厉！", gift));
				}else if(para[1].equalsIgnoreCase("1")){
					gift = player.level*1500;
					chatService.sendPrivateMessage(player.id, MessageFormat.format("本次单人对抗赛您惜败一局。获得{0}经验，请再接再厉！", gift));
				}
				if(para[2].equalsIgnoreCase("3")){
					chatService.sendPrivateMessage(player.id, "跨服战消耗甚大，您今日已参加三场比赛，还请暂且休兵，待明日再战。");
				}
				log.info("[STEPBATTLESENDGIFT]PLAYERID["+player.id+"]LEVEL["+player.level+"]EXP["+gift+"]");
			}
			PlayerTransaction tx = player.newTransaction("STEPBATTLE");
			player.addExp(gift, tx, true);
			tx.commit();
		}
	}

}
