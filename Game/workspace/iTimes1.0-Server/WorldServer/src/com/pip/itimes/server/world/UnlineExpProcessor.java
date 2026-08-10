package com.pip.itimes.server.world;

import java.util.Date;

import org.apache.log4j.Logger;

import com.pip.itimes.net.ClientConstants;
import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.server.stage.AddLife;
import com.pip.itimes.server.stage.Changed;
import com.pip.itimes.server.stage.Command;
import com.pip.itimes.server.stage.Effect;
import com.pip.itimes.server.stage.IEffectItem;
import com.pip.itimes.server.stage.Items;
import com.pip.itimes.server.world.unline.UnlineExpConfig;

public class UnlineExpProcessor implements CommandProcessor {
	private static final Logger log = Logger.getLogger(ConnectSession.class);
	
	private final int LIFE_ITEM_ID = 201096;		//活力物品的ID
	
	private final byte UNLINE_CMD_NOTICE = 1;		//查看更新公告
	private final byte UNLINE_CMD_GET = 2;			//领取
	private final byte UNLINE_CMD_EXIT = 3;			//退出 各种离开
	private final byte UNLINE_CMD_EXITGAME = 4;		//退出游戏
	private final byte UNLINE_CMD_GETLIFE = 5;		//道具获取活力
	private final byte UNLINE_CMD_GETEXP = 6;		//确定获取离线经验
	private final byte UNLINE_CMD_GETEXPCANCEL = 7;	//取消换取离线经验
	private final byte UNLINE_CMD_LOGINGET = 8;		//登陆时获取
	private final byte UNLINE_CMD_USEITEM = 9;
	private final byte UNLINE_CMD_USECITEMANCEL = 10;//在使用物品时取消 
	private final byte UNLINE_CMD_BUY = 11;		//购买物品
	private final byte UNLINE_CMD_RETURN = 12;	//返回游戏 
	private final byte UNLINE_CMD_CHECK = 13;		//非退出时返回
	
	ConnectSession connectSession;
	
	public UnlineExpProcessor(ConnectSession connectSession){
		this.connectSession = connectSession;
	}
	
	public void process(WorldPlayer player, Command command) throws Exception {
		int type = Integer.parseInt(command.getParam(0));
		byte exittype = 0;
		int exp;
		switch(type){
		case UNLINE_CMD_NOTICE:
			if(command.getParamCount() >= 2){
				exittype = Byte.parseByte(command.getParam(1));
			}
			String message = Server.iMoneyType == Server.IMONEY_TYPE_PIP ? (UnlineExpConfig.getNews() + "\n1.返回") : ("此功能尚未开放，尽请期待！" + "\n1.返回");
			connectSession.sendMessage(message, command.getSerial(), command.getSessionId());
			if(exittype == 1 || exittype == 2){
		    	byte[] bytes = connectSession.stageService.getTaskBytes((short) 31010,
		                new String[] {
							"1",
							"1",
							message,
							"unline_exp 10 " + exittype,
						}
				);
		    	UWAPSegment seg_login = new UWAPSegment(ClientConstants.
                        GET_FILE_OK, command.getSerial(), command.getSessionId());
				seg_login.writeShort((short) 31010);
				seg_login.writeShort((short) 2);
				seg_login.write(bytes);
				connectSession.write(seg_login);
			}else{
				byte[] bytes = connectSession.stageService.getTaskBytes((short) 31010,
		                new String[] {
							"1",
							"1",
							message,
							"unline_exp 13 " + exittype,
						}
				);
		    	UWAPSegment seg_login = new UWAPSegment(ClientConstants.
                        GET_FILE_OK, command.getSerial(), command.getSessionId());
				seg_login.writeShort((short) 31010);
				seg_login.writeShort((short) 2);
				seg_login.write(bytes);
				connectSession.write(seg_login);
			}
			
			break;
		case UNLINE_CMD_GET:
//			if(player.getLevel() >= 100){
//				connectSession.sendMessage("您已经满级了，不需要获得离线经验。", command.getSerial(), command.getSessionId());
//				return;
//			}
			exittype = Byte.parseByte(command.getParam(1));
			exp = player.getUnlineExp();
			if(exp <= 0){
				String messageget = "您现在没有离线经验，无法领取。\n1.返回";
		    	byte[] bytes = connectSession.stageService.getTaskBytes((short) 31010,
		                new String[] {
							"1",
							"1",
							messageget,
		                	"unline_exp 10 " + exittype,
						}
				);
		        UWAPSegment seg_login = new UWAPSegment(ClientConstants.
		                                          GET_FILE_OK, command.getSerial(), command.getSessionId());
		        seg_login.writeShort((short) 31010);
		        seg_login.writeShort((short) 2);
		        seg_login.write(bytes);
		        connectSession.write(seg_login);
				break;
			}
			int lifeValue = player.getAllLife();
			if(lifeValue <= 0){
//				connectSession.sendMessage("您没有足够的活力，活力随在线时长增长您也可以直接选择2增加活力。", command.getSerial(), command.getSessionId());
				String messageget = "您没有足够的活力，活力随在线时长增长，您也可以直接选择2增加活力。\n1.返回";
		    	byte[] bytes = connectSession.stageService.getTaskBytes((short) 31010,
		                new String[] {
							"1",
							"1",
							messageget,
		                	"unline_exp 10 " + exittype,
						}
				);
		        UWAPSegment seg_login = new UWAPSegment(ClientConstants.
		                                          GET_FILE_OK, command.getSerial(), command.getSessionId());
		        seg_login.writeShort((short) 31010);
		        seg_login.writeShort((short) 2);
		        seg_login.write(bytes);
		        connectSession.write(seg_login);
				break;
			}else{
				int lifeHour = player.getAllLife() / 10;
				int expHour = player.getUnlineExp() / UnlineExpConfig.getLevelExp(player.getLevel());
				int hour = Math.min(lifeHour, expHour);
				
				if(hour <= 0){
					String messageget = "您积累的离线经验不够兑换。\n1.返回";
			    	byte[] bytes = connectSession.stageService.getTaskBytes((short) 31010,
			                new String[] {
								"1",
								"1",
								messageget,
			                	"unline_exp 10 " + exittype,
							}
					);
			        UWAPSegment seg_login = new UWAPSegment(ClientConstants.
			                                          GET_FILE_OK, command.getSerial(), command.getSessionId());
			        seg_login.writeShort((short) 31010);
			        seg_login.writeShort((short) 2);
			        seg_login.write(bytes);
			        connectSession.write(seg_login);
			        break;
				}
				
				exp = hour * UnlineExpConfig.getLevelExp(player.getLevel());
				long getExp = 1L * Discount.UNLINE_EXP_PERCENT * exp;
				String messageget = "您将消耗" + (hour * 10) + "的活力换取" + (int) (getExp / 100) + "点离线经验（VIP玩家可获得双倍经验），确认换取吗？\n1.换取离线经验\n2.先不换了";
		    	byte[] bytes = connectSession.stageService.getTaskBytes((short) 31010,
		                new String[] {
							"2",
							"1",
							messageget,
		                	"unline_exp 6 " + exittype,
		                	"unline_exp 7 " + exittype,
						}
				);
		        UWAPSegment seg_login = new UWAPSegment(ClientConstants.
		                                          GET_FILE_OK, command.getSerial(), command.getSessionId());
		        seg_login.writeShort((short) 31010);
		        seg_login.writeShort((short) 2);
		        seg_login.write(bytes);
		        connectSession.write(seg_login);
			}
//			connectSession.sendMessage("您拥有离线经验值" + player.getUnlineExp(), command.getSerial(), command.getSessionId());
			break;
		case UNLINE_CMD_EXIT:
			if(player.getLevel() >= 20){
				connectSession.sendMessage("您可以到系统菜单中查看更新公告，可以在角色状态信息中查看您的活力值。", command.getSerial(), command.getSessionId());
			}
			break;
		case UNLINE_CMD_EXITGAME:
			exittype = Byte.parseByte(command.getParam(1));
			if(exittype != 3){
				UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL, command
						.getSerial(), command.getSessionId());
				seg.writeShort(ClientConstants.EXTEND_UNLINEEXP);
				seg.write(exittype);
				connectSession.write(seg);
			}
			if(player.getVipNewLevel() > 4){
				long limittime = 60 * 60 * 1000L;
            	boolean isSendMessage = false;
            	if(System.currentTimeMillis() - player.getVip5MessageDownClock().getTime() > limittime){//登录时判断离线时间到下次上线是否大于1小时
            		isSendMessage = true;
            	}
            	if(isSendMessage){
            		String msg = player.getPlayerName() + "暂时离开幻想大陆回到神的国度，所有的臣民都在祈祷他再次的降临。";
            		this.connectSession.getChatService().sendWorldMessage(-1, "系统", msg);
            		player.setVip5MessageDownClock(new Date());
            	}
            }
			break;
		case UNLINE_CMD_GETLIFE:
//			if(player.getLevel() >= 100){
//				connectSession.sendMessage("您已经满级，不需要离线经验。", command.getSerial(), command.getSessionId());
//				return;
//			}
			exittype = Byte.parseByte(command.getParam(1));
			if(player.getAllLife() >= UnlineExpConfig.LIFEVALUE_MAX){
				String messageget = "您的活力已经达到上限，不需要使用威哥。\n1.返回";
		    	byte[] bytes = connectSession.stageService.getTaskBytes((short) 31010,
		                new String[] {
							"1",
							"1",
							messageget,
		                	"unline_exp 10 " + exittype,
						}
				);
		        UWAPSegment seg_login = new UWAPSegment(ClientConstants.
		                                          GET_FILE_OK, command.getSerial(), command.getSessionId());
		        seg_login.writeShort((short) 31010);
		        seg_login.writeShort((short) 2);
		        seg_login.write(bytes);
		        connectSession.write(seg_login);
				break;
			}
			if(player.hasItem(LIFE_ITEM_ID)){
				String messageget = "您是否直接使用威哥来增加活力？\n1.使用威哥\n2.下次再说";
		    	byte[] bytes = connectSession.stageService.getTaskBytes((short) 31010,
		                new String[] {
							"2",
							"1",
							messageget,
		                	"unline_exp 9 " + exittype,
		                	"unline_exp 10 " + exittype,
						}
				);
		        UWAPSegment seg_login = new UWAPSegment(ClientConstants.
		                                          GET_FILE_OK, command.getSerial(), command.getSessionId());
		        seg_login.writeShort((short) 31010);
		        seg_login.writeShort((short) 2);
		        seg_login.write(bytes);
		        connectSession.write(seg_login);
			}else{
				if(Server.iMoneyType == Server.IMONEY_TYPE_CMCC){
					connectSession.sendMessage("您可以到瓦伊特南区兑换威哥来增加活力。", command.getSerial(), command.getSessionId());
				}else{
					String messageget = "您当前的活力值为:" + player.getAllLife() + "，威哥使用后可以让您活力四射，增加60点活力，只需" + 
						(Server.iMoneyType == Server.IMONEY_TYPE_QQ ? "50元宝" : "180i币") + "，来一瓶吧？\n1.自动购买\n2.下次再说";
			    	byte[] bytes = connectSession.stageService.getTaskBytes((short) 31010,
			                new String[] {
								"2",
								"1",
								messageget,
			                	"unline_exp 11 " + exittype,
			                	"unline_exp 10 " + exittype,
							}
					);
			        UWAPSegment seg_login = new UWAPSegment(ClientConstants.
			                                          GET_FILE_OK, command.getSerial(), command.getSessionId());
			        seg_login.writeShort((short) 31010);
			        seg_login.writeShort((short) 2);
			        seg_login.write(bytes);
			        connectSession.write(seg_login);
				}
			}
			break;
		case UNLINE_CMD_GETEXP:
			int lifeHour = player.getAllLife() / 10;
			int expHour = player.getUnlineExp() / UnlineExpConfig.getLevelExp(player.getLevel());
			int hour = Math.min(lifeHour, expHour);
			exp = hour * UnlineExpConfig.getLevelExp(player.getLevel());
			if(player.getVipNewLevel() > 0){
				exp *= 2;
			}
			// 极限时会越界，所以先转换下数据类型
			long getExp = 1L * Discount.UNLINE_EXP_PERCENT * exp;
			Changed changed = new Changed();
			int level_tmp = player.getLevel();
			player.addExp((int) (getExp / 100), changed);
			if(level_tmp<player.getLevel()){
                //推荐人通用函数
            	connectSession.playerService.recommendBalance(player, "UnlineExp");
            	//尝试加到师傅的列表中
            	connectSession.playerService.addMasterPlayer(player, changed);
            	connectSession.checkLevelChangedAndSendTips(player, changed, command.getSerial(), command.getSessionId(), level_tmp);
            }
            connectSession.sendGetItem(changed, command.getSerial(), command.getSessionId(),
                        (byte) 12);
            player.setUnlineExp(player.getUnlineExp() - exp);
            
            int onlineLife = player.calcOnlineLife();
            if(onlineLife + player.getLifeValue() > UnlineExpConfig.LIFEVALUE_MAX){
            	int addValue = onlineLife + player.getLifeValue() - UnlineExpConfig.LIFEVALUE_MAX;
            	player.setLifeValue(player.getLifeValue() - addValue);
            }
            player.setLifeValue(player.getLifeValue() - hour * 10);
            
            if(player.getLifeValue() + onlineLife < 0){
            	player.setLifeValue(-onlineLife);
            }
            
            connectSession.sendMessage("您的活力值减少" + (hour * 10) + "点。", command.getSerial(), command.getSessionId());
			break;
		case UNLINE_CMD_GETEXPCANCEL:
		case UNLINE_CMD_USECITEMANCEL:
			exittype = Byte.parseByte(command.getParam(1));
			if(exittype == 3){
				loginGet(player, command.getSerial(), command.getSessionId());
			}else{
				exitUnlineQuestion(connectSession, player, exittype, command.getSerial(), command.getSessionId());
			}
			break;
		case UNLINE_CMD_LOGINGET:
			loginGet(player, command.getSerial(), command.getSessionId());
			break;
		case UNLINE_CMD_USEITEM:
		case UNLINE_CMD_BUY:
			//modfiy 满级同样可以获得离线经验
//			if(player.getLevel() >= 100){
//				connectSession.sendMessage("您已经满级，不需要离线经验。", command.getSerial(), command.getSessionId());
//				return;
//			}
			exittype = Byte.parseByte(command.getParam(1));
			if(player.getAllLife() >= UnlineExpConfig.LIFEVALUE_MAX){
				String messageget = "您的活力已经达到上限，不需要使用威哥。\n1.返回";
		    	byte[] bytes = connectSession.stageService.getTaskBytes((short) 31010,
		                new String[] {
							"1",
							"1",
							messageget,
		                	"unline_exp 10 " + exittype,
						}
				);
		        UWAPSegment seg_login = new UWAPSegment(ClientConstants.
		                                          GET_FILE_OK, command.getSerial(), command.getSessionId());
		        seg_login.writeShort((short) 31010);
		        seg_login.writeShort((short) 2);
		        seg_login.write(bytes);
		        connectSession.write(seg_login);
				break;
			}
			if(player.hasItem(LIFE_ITEM_ID)){
				Changed changed2 = new Changed();
				IEffectItem item = (IEffectItem)Items.getTemplate(LIFE_ITEM_ID).newInstance();
				Effect effects[] = item.getEffects();
				if(effects.length == 1){
					AddLife life = (AddLife)effects[0];
					int lifeValue2 = player.getLifeValue() + life.getValue();
					if(lifeValue2 >= UnlineExpConfig.LIFEVALUE_MAX){
						lifeValue2 = UnlineExpConfig.LIFEVALUE_MAX;
					}
					player.setLifeValue(lifeValue2);
					player.completeRemoveItem(LIFE_ITEM_ID, 1, changed2);
	                connectSession.sendGetItem(changed2, command.getSerial(), command.getSessionId(),
	                            (byte) 12);
	                
	                String messageget = "您消耗了1瓶威哥，增加了60点活力，请重新领取离线经验。\n1.返回";
			    	byte[] bytes = connectSession.stageService.getTaskBytes((short) 31010,
			                new String[] {
								"1",
								"1",
								messageget,
			                	"unline_exp 10 " + exittype,
							}
					);
			        UWAPSegment seg_login = new UWAPSegment(ClientConstants.
			                                          GET_FILE_OK, command.getSerial(), command.getSessionId());
			        seg_login.writeShort((short) 31010);
			        seg_login.writeShort((short) 2);
			        seg_login.write(bytes);
			        connectSession.write(seg_login);
					break;
//	                if(exittype == 3){
//	    				loginGet(player, command.getSerial(), command.getSessionId());
//	    			}else{
//	    				exitUnlineQuestion(connectSession, player, exittype, command.getSerial(), command.getSessionId());
//	    			}
				}
			}else{
				if(type == UNLINE_CMD_BUY){
					//发送自动购买
					
					int buyResult = connectSession.iShopEasy(player, LIFE_ITEM_ID, command.getSerial(), command.getSessionId(), 1, exittype, command.getAppType(), 1);
					String messageget = null;
					switch(buyResult){
					case ConnectSession.ISHOP_ISFULL:
						messageget = "您的背包空间已满，请清理后再试。\n1.返回";
						break;
					case ConnectSession.ISHOP_NOIMONEY:
						messageget = "您的" + (Server.iMoneyType == Server.IMONEY_TYPE_QQ ? "元宝" : "i币") + "不够，请充值再试。\n1.返回";
						break;
					default:
						messageget = "自动购买威哥失败！原因可能是您的可用余额不足（某些充值渠道：如苹果商店兑换i币等无法在此处消费）。您可在商城内尝试购买或重试。\n1.返回";
						break;
					}
//					if(buyResult != 0){
				    	byte[] bytes = connectSession.stageService.getTaskBytes((short) 31010,
				                new String[] {
									"1",
									"1",
									messageget,
				                	"unline_exp 10 " + exittype,
								}
						);
				        UWAPSegment seg_login = new UWAPSegment(ClientConstants.
				                                          GET_FILE_OK, command.getSerial(), command.getSessionId());
				        seg_login.writeShort((short) 31010);
				        seg_login.writeShort((short) 2);
				        seg_login.write(bytes);
				        connectSession.write(seg_login);
//					}
				}else{
					connectSession.sendMessage("您没有相应的物品。", command.getSerial(), command.getSessionId());
				}
			}
			break;
		case UNLINE_CMD_RETURN:
			break;
		case UNLINE_CMD_CHECK:
			connectSession.sendUnlineExp(command.getSerial(), command.getSessionId(), player, true);
			break;
		}
	}
	
	
	public void loginGet(WorldPlayer player, int serial, int sessionId){	 //zjl modify
		String messageget = "您已经积累了" + player.getUnlineExp() + "点离线经验，您当前的活力值为：" + player.getAllLife() + "，可用在线获得的活力领取：\n1.在线获得的活力获取\n2.活力不够，威哥帮你忙\n3.先不换了";
		byte[] bytes = connectSession.stageService.getTaskBytes((short) 31010,
                new String[] {
					"3",
					"1",
					messageget,
                	"unline_exp 2 " + 3,
                	"unline_exp 5 " + 3,
                	"unline_exp 4 " + 3,
				}
		);
        UWAPSegment seg_login = new UWAPSegment(ClientConstants.
                                          GET_FILE_OK, serial, sessionId);
        seg_login.writeShort((short) 31010);
        seg_login.writeShort((short) 2);
        seg_login.write(bytes);
        connectSession.write(seg_login);
	}
	
	public static void exitUnlineQuestion(ConnectSession cs, WorldPlayer player, byte type, int serial, int sessionId){
		String message;
		byte[] bytes;
		if(type == 3 || type == 0){
			message = "<cff0000>绿色游戏，健康生活，幻想员工祝您线上线下生活同样精彩~</c>\n您再也不用担心下线后经验跟不上别人了，因为现在离线也可以累积经验了！（注意：离线经验是有上限的哦~）您现在已经累积了"+
			player.getUnlineExp() + "点离线经验，您当前的活力值为：" + player.getAllLife() +"，可以用在线获得的活力领取：\n1.活力领取\n2.活力不够？威哥帮你忙\n3.下次再领";
			bytes = cs.stageService.getTaskBytes((short) 31010,
	            new String[] {
					"3",
					"1",
					message,
	            	"unline_exp 2 " + type,
	            	"unline_exp 5 " + type,
	            	"unline_exp 4 " + type
				}
			);
		}else{
			message = "<cff0000>绿色游戏，健康生活，幻想员工祝您线上线下生活同样精彩~</c>\n您再也不用担心下线后经验跟不上别人了，因为现在离线也可以累积经验了！（注意：离线经验是有上限的哦~）您现在已经累积了"+
				player.getUnlineExp() + "点离线经验，您当前的活力值为：" + player.getAllLife() + "，可以用在线获得的活力领取：\n1.活力领取\n2.活力不够？威哥帮你忙\n3.下线休息下次再领\n4.返回游戏";
			bytes = cs.stageService.getTaskBytes((short) 31010,
	            new String[] {
					"4",
					"1",
					message,
	            	"unline_exp 2 " + type,
	            	"unline_exp 5 " + type,
	            	"unline_exp 4 " + type,
	            	"unline_exp 12 0"
				}
			);
		}
	    UWAPSegment seg_login = new UWAPSegment(ClientConstants.
	                                      GET_FILE_OK,serial, sessionId);
	    seg_login.writeShort((short) 31010);
	    seg_login.writeShort((short) 2);
	    seg_login.write(bytes);
	    cs.write(seg_login);
	}
	
	public static void exitBuyBag(ConnectSession cs, WorldPlayer player, byte type, int serial, int sessionId){
		String message = "您已经购买了威哥，但由于背包空间已满，物品发到了您的邮箱中，请查收再使用。\n1.返回";
		byte[] bytes = cs.stageService.getTaskBytes((short) 31010,
	            new String[] {
					"1",
					"1",
					message,
	            	"unline_exp 7 " + type,
				}
		);
	    UWAPSegment seg_login = new UWAPSegment(ClientConstants.
	                                      GET_FILE_OK,serial, sessionId);
	    seg_login.writeShort((short) 31010);
	    seg_login.writeShort((short) 2);
	    seg_login.write(bytes);
	    cs.write(seg_login);
	}
	public static void exitBuyError(ConnectSession cs, WorldPlayer player, byte type, int serial, int sessionId){
		if(player == null){
			player = cs.getPlayer(sessionId);
			if(player == null){
				return;
			}
		}
		String message = "自动购买失败。请去商场购买或是重试。\n1.返回";
		byte[] bytes = cs.stageService.getTaskBytes((short) 31010,
	            new String[] {
					"1",
					"1",
					message,
	            	"unline_exp 7 " + type,
				}
		);
	    UWAPSegment seg_login = new UWAPSegment(ClientConstants.
	                                      GET_FILE_OK,serial, sessionId);
	    seg_login.writeShort((short) 31010);
	    seg_login.writeShort((short) 2);
	    seg_login.write(bytes);
	    cs.write(seg_login);
	}
	public static void exitBuyOk(ConnectSession cs, WorldPlayer player, byte type, int serial, int sessionId){
		String messageget = "自动购买成功！您是否直接使用威哥来增加活力？\n1.使用威哥\n2.下次再说";
    	byte[] bytes = cs.stageService.getTaskBytes((short) 31010,
                new String[] {
					"2",
					"1",
					messageget,
                	"unline_exp 9 " + type,
                	"unline_exp 10 " + type,
				}
		);
        UWAPSegment seg_login = new UWAPSegment(ClientConstants.
                                          GET_FILE_OK, serial, sessionId);
        seg_login.writeShort((short) 31010);
        seg_login.writeShort((short) 2);
        seg_login.write(bytes);
        cs.write(seg_login);
	}
}
