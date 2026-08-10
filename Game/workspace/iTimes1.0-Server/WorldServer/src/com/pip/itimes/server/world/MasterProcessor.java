package com.pip.itimes.server.world;

import com.pip.itimes.net.ClientConstants;
import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.server.ITimesException;
import com.pip.itimes.server.bean.Master;
import com.pip.itimes.server.stage.Changed;
import com.pip.itimes.server.stage.Command;
import com.pip.itimes.server.stage.IItemTemplate;
import com.pip.itimes.server.stage.ItemUtils;
import com.pip.itimes.server.stage.Items;
import com.pip.itimes.server.stage.NoDoor;
import com.pip.itimes.server.world.game.CampBattlefieldConfig;
import com.pip.itimes.server.world.game.GameMap;

public class MasterProcessor implements CommandProcessor {
	
	public final static int MASTER_CALLCOUNT_MAX = 10;		//最大查找次数
	
	public final static byte MASTER_TYPE_FINDMASTER = 1;		//查找师傅
	public final static byte MASTER_TYPE_MASTERCANCEL = 2;		//师傅不同意收徒弟
	public final static byte MASTER_TYPE_MASTEROK = 3;			//师傅同意收徒弟
	public final static byte MASTER_TYPE_RANDOM = 4;			//随机找师傅
	public final static byte MASTER_TYPE_FINDAPPRENTICE = 5;	//找徒弟
	public final static byte MASTER_TYPE_APPRENTICECANCEL = 6;	//徒弟不同意
	public final static byte MASTER_TYPE_APPRENTICEOK = 7;		//徒弟同意
	public final static byte MASTER_TYPE_PLAYERINFO = 8;			//获取角色信息
	public final static byte MASTER_TYPE_MASTERTOAPPRENTICE = 9;	//师傅传送到徒弟身边
	public final static byte MASTER_TYPE_MASTERTOAPPRENTICEOK = 10;	//可以进行传送
	public final static byte MASTER_TYPE_APPRENTICECALLMASTER = 11;	//徒弟招唤师傅
	public final static byte MASTER_TYPE_APPRENTICECALLMASTERSEND = 12;	//徒弟招唤师傅
	public final static byte MASTER_TYPE_APPRENTICECALLMASTEROK = 13;	//师傅同意徒弟的召唤
	public final static byte MASTER_TYPE_APPRENTICECALLMASTERCANCEL = 14;	//师傅取消徒弟的召唤
	public final static byte MASTER_TYPE_APPRENTICEUNMASTER = 15;		//徒弟解除师徒关系
	public final static byte MASTER_TYPE_APPRENTICEUNMASTEROK = 16;		//徒弟确定解除师徒关系
	public final static byte MASTER_TYPE_APPRENTICEUNMASTERCANCEL = 17;		//徒弟取消解除师徒关系
	public final static byte MASTER_TYPE_MASTERUNAPPRENTICE = 18;		//师傅解除与徒弟的关系
	public final static byte MASTER_TYPE_MASTERUNAPPRENTICEOK = 19;
	public final static byte MASTER_TYPE_MASTERUNAPPRENTICECANCEL = 20;
	public final static byte MASTER_TYPE_MASTERTOAPPRENTICESELECTOK = 21;			//师傅传送到徒儿身边选择确认
	public final static byte MASTER_TYPE_MASTERTOAPPRENTICESELECTCANCEL = 22;		//师傅传送到徒儿身边选择取消
	public final static byte MASTER_TYPE_FINDMASTERRESULT = 23;						//各种找师傅时的结果
	public final static byte MASTER_TYPE_FINDAPPRENTICERESULT = 24;						//各种收徒的结果
	
	private ConnectSession connectSession;
	
	public MasterProcessor(ConnectSession connectSession){
		this.connectSession = connectSession;
	}

	public void process(WorldPlayer player, Command command) throws Exception {
		byte type = Byte.parseByte(command.getParam(0));
		int requestId = 0;
		switch(type){
		case MASTER_TYPE_MASTERCANCEL:			//不同意收徒
			requestId = Integer.parseInt(command.getParam(1));
			MasterService.Request request = connectSession.masterService.cancelRelation(player, requestId);
			connectSession.sendMessage(request.destId, "对方拒绝了你的拜师邀请，很遗憾你们俩没有建立师徒关系。");
			//不同意收徒时 加一次防打扰
            player.setCallCount(player.getCallCount() + 1);
			break;
		case MASTER_TYPE_MASTEROK:				//同意收徒
			requestId = Integer.parseInt(command.getParam(1));
			try{
	            Changed changed1 = new Changed();
	            Changed changed2 = new Changed();
	            Master master = connectSession.masterService.makeRelation(player, requestId, changed1, changed2);
	            //收完徒弟后，需要看看是否还能存在师傅列表中
	            connectSession.playerService.addMasterPlayer(player, null);
	            ConnectSession.log.info("MasterID["+master.getMasterId()+"] PrenticeId["+master.getPrenticeId()+"]Master");
	            if(player.isFull() && !player.hasItem(MasterService.TOP_MASTER_ID)){
	            	IItemTemplate template = Items.getTemplate(MasterService.TOP_MASTER_ID);
	            	connectSession.mailService.sendMail(master.getMasterId(), master.getMasterName(), -1, "系统", "师徒称号", "您收了个徒弟，获得了一个称号。", ItemUtils.item2dbAttachment(template.newInstance(), 1), 0, true);
	            }else{
	            	connectSession.connectService.sendGetItem(changed1, master.getMasterId(), (byte) 20);
	            }
	            WorldPlayer apprentice = connectSession.playerService.getWorldPlayer(master.getPrenticeId());
	            if(apprentice.isFull() && !apprentice.hasItem(MasterService.TOP_ID)){
	            	IItemTemplate template = Items.getTemplate(MasterService.TOP_ID);
	            	connectSession.mailService.sendMail(master.getPrenticeId(), master.getPrenticeName(), -1, "系统", "师徒称号", "您拜了一个师傅，获得了一个称号。", ItemUtils.item2dbAttachment(template.newInstance(), 1), 0, true);
	            }else{
	            	connectSession.connectService.sendGetItem(changed2, master.getPrenticeId(), (byte) 20);
	            }
	            connectSession.sendMessage(master.getMasterId(), master.getPrenticeName() + "已经成为你的徒弟");
	            connectSession.sendMessage(master.getPrenticeId(), master.getMasterName() + "已经成为你的师傅");
	            //同意收徒时 加一次防打扰
	            player.setCallCount(player.getCallCount() + 1);
			}catch(Exception ex){
				connectSession.sendMessage("收徒出错。", command.getSerial(), command.getSessionId());
			}
			break;
		case MASTER_TYPE_APPRENTICECANCEL:			//不同意当徒弟
			requestId = Integer.parseInt(command.getParam(1));
			MasterService.Request request2 = connectSession.masterService.apprenticeCancelRelation(player, requestId);
			connectSession.sendMessage(request2.sourceId, "对方拒绝了你的收徒邀请，很遗憾你们俩没有建立师徒关系。");
			break;
		case MASTER_TYPE_APPRENTICEOK:				//同意当徒弟
			requestId = Integer.parseInt(command.getParam(1));
			try{
	            Changed changed1 = new Changed();
	            Changed changed2 = new Changed();
	            Master master = connectSession.masterService.apprenticeMakeRelation(player, requestId, changed1, changed2);
	            //收完徒弟后，需要看看是否还能存在师傅列表中
	            connectSession.playerService.addMasterPlayer(connectSession.playerService.getWorldPlayer(master.getMasterId()), null);
	            
	            ConnectSession.log.info("MasterID["+master.getMasterId()+"] PrenticeId["+master.getPrenticeId()+"]Master");
	            WorldPlayer masterPlayer = connectSession.playerService.getWorldPlayer(master.getMasterId());
	            if(masterPlayer.isFull() && !masterPlayer.hasItem(MasterService.TOP_MASTER_ID)){
	            	IItemTemplate template = Items.getTemplate(MasterService.TOP_MASTER_ID);
	            	connectSession.mailService.sendMail(master.getMasterId(), master.getMasterName(), -1, "系统", "师徒称号", "您收了个徒弟，获得了一个称号。", ItemUtils.item2dbAttachment(template.newInstance(), 1), 0, true);
	            }else{
	            	connectSession.connectService.sendGetItem(changed1, master.getMasterId(), (byte) 20);
	            }
	            if(player.isFull() && !player.hasItem(MasterService.TOP_ID)){
	            	IItemTemplate template = Items.getTemplate(MasterService.TOP_ID);
	            	connectSession.mailService.sendMail(master.getPrenticeId(), master.getPrenticeName(), -1, "系统", "师徒称号", "您拜了一个师傅，获得了一个称号。", ItemUtils.item2dbAttachment(template.newInstance(), 1), 0, true);
	            }else{
	            	connectSession.connectService.sendGetItem(changed2, master.getPrenticeId(), (byte) 20);
	            }
	            connectSession.sendMessage(master.getMasterId(), master.getPrenticeName() + "已经成为你的徒弟");
	            connectSession.sendMessage(master.getPrenticeId(), master.getMasterName() + "已经成为你的师傅");
			}catch(Exception ex){
				connectSession.sendMessage(ex.getMessage(), command.getSerial(), command.getSessionId());
			}
			break;
		case MASTER_TYPE_APPRENTICECALLMASTEROK:
			requestId = Integer.parseInt(command.getParam(1));
			Master[] apps = connectSession.masterService.getRelation(player);
			if(apps == null || apps.length == 0){
				connectSession.sendMessage("请求错误。", command.getSerial(), command.getSessionId());
				break;
			}
			GameMap map = player.getMap();
			if(map != null){
				NoDoor door = NoDoor.getNoTransfer(map.getMapId());
				if(door != null){
					connectSession.sendMessage(door.getMessage(), command.getSerial(), command.getSessionId());
					break;
				}
			}
			
			WorldPlayer apprentice = null;
			for(int i=0; i<apps.length; i++){
				if(apps[i].getId() == requestId){
					apprentice = connectSession.playerService.getWorldPlayer(apps[i].getPrenticeId());
					if(apprentice == null){
//						connectSession.sendMessage("您的徒弟已经下线了。", command.getSerial(), command.getSessionId());
						throw new ITimesException("您的徒弟已经下线了。",command.getSerial(),command.getSessionId(),(byte)20);
					}
					break;
				}
			}
			map = apprentice.getMap();
			NoDoor door = NoDoor.getNoDoor(map.getMapId());
            if(door!=null){
//            	connectSession.sendMessage("目标地图不允许使用该功能。", command.getSerial(), command.getSessionId());
            	throw new ITimesException("目标地图不允许使用该功能。",command.getSerial(),command.getSessionId(),(byte)20);
            }else{
				connectSession.sendGotoMap(player.getId(),
                        map.getMapId(), (short) (apprentice.getX() / map.getTileWidth()),
                        (short) (apprentice.getY() / map.getTileHeight()));
            }
			break;
		case MASTER_TYPE_APPRENTICECALLMASTERCANCEL:
			requestId = Integer.parseInt(command.getParam(1));
			apps = connectSession.masterService.getRelation(player);
			if(apps == null || apps.length == 0){
				connectSession.sendMessage("请求错误。", command.getSerial(), command.getSessionId());
				break;
			}
			apprentice = null;
			for(int i=0; i<apps.length; i++){
				if(apps[i].getId() == requestId){
					apprentice = connectSession.playerService.getWorldPlayer(apps[i].getPrenticeId());
					if(apprentice == null){
						return;
					}
					break;
				}
			}
			connectSession.sendMessage(apprentice.getId(), "你的师傅现在忙，请稍后再呼叫吧。");
			break;
		case MASTER_TYPE_APPRENTICEUNMASTEROK:		//徒弟同意解除关系
			Master mt = connectSession.masterService.getMasterRelation(player);
			if(mt == null){
				connectSession.sendMessage("不存在师徒关系。", command.getSerial(), command.getSessionId());
				break;
			}
//			WorldPlayer master = connectSession.playerService.loadWorldPlayer(mt.getMasterId());
			WorldPlayer master = connectSession.playerService.getWorldPlayerAndCatch(mt.getMasterId());
			if(master == null){
				connectSession.sendMessage("请求错误。", command.getSerial(), command.getSessionId());
				return;
			}
			//7天内上过线
			if (master.getLastLoginTime().getTime() + 7 * 3600 * 1000 * 24L > System.currentTimeMillis()) {
				if(mt.getIntimacy() >= 100){
					connectSession.sendMessage("你们的亲密度已经超过100了不能随意解除师徒关系了只能使用师徒自由药水了，请慎重考虑哦。", command.getSerial(), command.getSessionId());
				}else{
					if(player.getCredit() < 100){
						connectSession.sendMessage("你的荣誉值不够。", command.getSerial(), command.getSessionId());
					}else{
						Changed changed = new Changed();
						try{
							connectSession.masterService.IllegalUnRelation(player, changed);
							connectSession.playerService.addMasterPlayer(connectSession.playerService.getWorldPlayer(master.getId()), null);
						}catch(Exception e){
//							connectSession.playerService.savePlayer(master);
							connectSession.sendMessage(e.getMessage(), command.getSerial(), command.getSessionId());
							connectSession.playerService.releasePlayer(master);
							return;
						}
						connectSession.connectService.sendGetItem(changed, player.getId(), (byte) 20);
						connectSession.sendMessage("与" + mt.getMasterName() + "的师徒关系已经解除", command.getSerial(), command.getSessionId());
//						master = connectSession.playerService.getWorldPlayer(mt.getMasterId());
						if(master != null && master.online()){
							connectSession.sendMessage(master.getId(), "您的徒儿" + player.getPlayerName() + "与您解除了师徒关系。");
						}else{
							connectSession.mailService.sendMail(mt.getMasterId(), mt.getMasterName(), -1, "系统", "师徒解除", "您的徒儿" + player.getPlayerName() + "与您解除了师徒关系。", null, 0, true);
						}
					}
				}
			}else{
				try {
                    Changed changed = new Changed();
                    ConnectSession.log.info("ID[" + player.getId() + "] SingleUnMaster Money[" + player.getMoeny() + "] Credit[" +
                             player.getCredit() + "] TRY");
                    mt = connectSession.masterService.unRelation(player, changed);
                    connectSession.playerService.addMasterPlayer(connectSession.playerService.getWorldPlayer(master.getId()), null);
                    connectSession.sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte) 20);
                    connectSession.sendMessage(player.getId(), "与" + mt.getMasterName() + "的师徒关系已经解除");
//                    master = connectSession.playerService.getWorldPlayer(mt.getMasterId());
					if(master != null && master.online()){
						connectSession.sendMessage(master.getId(), "您的徒儿" + player.getPlayerName() + "与您解除了师徒关系。");
					}else{
						connectSession.mailService.sendMail(mt.getMasterId(), mt.getMasterName(), -1, "系统", "师徒解除", "您的徒儿" + player.getPlayerName() + "与您解除了师徒关系。", null, 0, true);
					}
                    ConnectSession.log.info("ID[" + player.getId() + "] SingleUnMaster Money[" + player.getMoeny() + "] Credit[" +
                             player.getCredit() + "] SUCCESS");
                } catch (MasterException ex) {
                	ConnectSession.log.info("ID[" + player.getId() + "] SingleUnMaster Money[" + player.getMoeny() + "] Credit[" +
                             player.getCredit() + "] FAIL");
                	connectSession.sendMessage(ex.getMessage(), command.getSerial(), command.getSessionId());
                }
			}
			connectSession.playerService.releasePlayer(master);
			break;
		case MASTER_TYPE_MASTERUNAPPRENTICEOK:		//师傅同意解除关系
			requestId = Integer.parseInt(command.getParam(1));
			apps = connectSession.masterService.getRelation(player);
			if(apps == null || apps.length == 0){
				connectSession.sendMessage("请求错误。", command.getSerial(), command.getSessionId());
				break;
			}
			int appIndex = 0;
			apprentice = null;
			for(int i=0; i<apps.length; i++){
				if(apps[i].getId() == requestId){
					appIndex = i;
//					apprentice = connectSession.playerService.loadWorldPlayer(apps[i].getPrenticeId());
					apprentice = connectSession.playerService.getWorldPlayerAndCatch(apps[i].getPrenticeId());
					if(apprentice == null){
						connectSession.sendMessage("请求错误。", command.getSerial(), command.getSessionId());
						return;
					}
					break;
				}
			}
			//7天内上过线
			if (apprentice.getLastLoginTime().getTime() + 7 * 3600 * 1000 * 24L > System.currentTimeMillis()) {
				if(apps[appIndex].getIntimacy() >= 100){
					connectSession.sendMessage("你们的亲密度已经超过100了不能随意解除师徒关系了只能使用师徒自由药水了，请慎重考虑哦。", command.getSerial(), command.getSessionId());
				}else{
					if(player.getCredit() < 100){
						connectSession.sendMessage("你的荣誉值不够。", command.getSerial(), command.getSessionId());
					}else{
						Changed changed = new Changed();
						try{
							connectSession.masterService.IllegalUnRelation(player, apprentice.getId(), changed);
							connectSession.playerService.addMasterPlayer(player, null);
						}catch(Exception e){
//							connectSession.playerService.savePlayer(apprentice);
							connectSession.sendMessage(e.getMessage(), command.getSerial(), command.getSessionId());
							connectSession.playerService.releasePlayer(apprentice);
							return;
						}
						connectSession.connectService.sendGetItem(changed, player.getId(), (byte) 20);
						connectSession.sendMessage("与" + apps[appIndex].getPrenticeName() + "的师徒关系已经解除", command.getSerial(), command.getSessionId());
//						apprentice = connectSession.playerService.getWorldPlayer(apps[appIndex].getPrenticeId());
						if(apprentice != null && apprentice.online()){
							connectSession.sendMessage(apprentice.getId(), "您的师傅" + player.getPlayerName() + "与您解除了师徒关系。");
						}else{
							connectSession.mailService.sendMail(apps[appIndex].getPrenticeId(), apps[appIndex].getPrenticeName(), -1, "系统", "师徒解除", "您的师傅" + player.getPlayerName() + "与您解除了师徒关系。", null, 0, true);
						}
					}
				}
			}else{
				try {
                    Changed changed = new Changed();
                    ConnectSession.log.info("ID[" + player.getId() + "] SingleUnMaster Money[" + player.getMoeny() + "] Credit[" +
                             player.getCredit() + "] TRY");
                    mt = connectSession.masterService.unRelation(player, apprentice.getId(), changed);
                    connectSession.playerService.addMasterPlayer(player, null);
                    connectSession.sendGetItem(changed, command.getSerial(), command.getSessionId(), (byte) 20);
                    connectSession.sendMessage(player.getId(), "与" + mt.getPrenticeName() + "的师徒关系已经解除");
//                    apprentice = connectSession.playerService.getWorldPlayer(apps[appIndex].getPrenticeId());
					if(apprentice != null && apprentice.online()){
						connectSession.sendMessage(apprentice.getId(), "您的师傅" + player.getPlayerName() + "与您解除了师徒关系。");
					}else{
						connectSession.mailService.sendMail(apps[appIndex].getPrenticeId(), apps[appIndex].getPrenticeName(), -1, "系统", "师徒解除", "您的师傅" + player.getPlayerName() + "与您解除了师徒关系。", null, 0, true);
					}
                    ConnectSession.log.info("ID[" + player.getId() + "] SingleUnMaster Money[" + player.getMoeny() + "] Credit[" +
                             player.getCredit() + "] SUCCESS");
                } catch (MasterException ex) {
                	ConnectSession.log.info("ID[" + player.getId() + "] SingleUnMaster Money[" + player.getMoeny() + "] Credit[" +
                             player.getCredit() + "] FAIL");
                	connectSession.sendMessage(ex.getMessage(), command.getSerial(), command.getSessionId());
                }
			}
			connectSession.playerService.releasePlayer(apprentice);
			break;
		case MASTER_TYPE_MASTERTOAPPRENTICESELECTOK:		//师傅传送到徒儿身边选择确认
			requestId = Integer.parseInt(command.getParam(1));
			apps = connectSession.masterService.getRelation(player);
			if(apps == null || apps.length == 0){
				connectSession.sendMessage("请求错误。", command.getSerial(), command.getSessionId());
				break;
			}
			
			map = player.getMap();
			if(map != null){
				door = NoDoor.getNoTransfer(map.getMapId());
				if(door != null){
					connectSession.sendMessage(door.getMessage(), command.getSerial(), command.getSessionId());
					break;
				}
			}
			
			apprentice = null;
			for(int i=0; i<apps.length; i++){
				if(apps[i].getId() == requestId){
					apprentice = connectSession.playerService.getWorldPlayer(apps[i].getPrenticeId());
					if(apprentice == null){
//						connectSession.sendMessage("您的徒弟已经下线了。", command.getSerial(), command.getSessionId());
						throw new ITimesException("您的徒弟已经下线了。",command.getSerial(),command.getSessionId(),(byte)20);
					}
					break;
				}
			}
			map = apprentice.getMap();
			door = NoDoor.getNoDoor(map.getMapId());
            if(door!=null){
//            	connectSession.sendMessage("目标地图不允许使用该功能。", command.getSerial(), command.getSessionId());
            	throw new ITimesException("目标地图不允许使用该功能。",command.getSerial(),command.getSessionId(),(byte)20);
            }else{
				connectSession.sendGotoMap(player.getId(),
                        map.getMapId(), (short) (apprentice.getX() / map.getTileWidth()),
                        (short) (apprentice.getY() / map.getTileHeight()));
            }
			break;
		case MASTER_TYPE_MASTERTOAPPRENTICESELECTCANCEL:	//师傅传送到徒儿身边选择取消
			break;
		}
	}
	
	/**
	 * 向找到的师傅发送拜师请求
	 * @param connectSession
	 * @param master
	 * @param player
	 */
	public static void sendToFindMaster(ConnectSession connectSession, WorldPlayer master, WorldPlayer player) throws Exception{
		MasterService.Request request = connectSession.masterService.requestRelation(master, player);
		String messageget = player.getPlayerName() + "向您发出拜师邀请，希望能做您的徒弟，请问您是否愿意收他为徒？\n1.同意\n2.不同意";
    	byte[] bytes = connectSession.stageService.getTaskBytes((short) 31010,
                new String[] {
					"2",
					"1",
					messageget,
					"master " + MASTER_TYPE_MASTEROK + " " + request.id,
                	"master " + MASTER_TYPE_MASTERCANCEL + " " + request.id,
				}
		);
        UWAPSegment seg = new UWAPSegment(ClientConstants.
                                          GET_FILE_OK);
        seg.writeShort((short) 31010);
        seg.writeShort((short) 2);
        seg.write(bytes);
        connectSession.connectService.writeTo(seg, master.getId());
	}
	
	public static void sendToFindApprentice(ConnectSession connectSession, WorldPlayer master, WorldPlayer player) throws Exception{
		MasterService.Request request = connectSession.masterService.requestRelation(master, player);
		String messageget = master.getPlayerName() + "想收你为徒，希望你能做他的徒弟，请问你是否愿意拜他为师？\n1.同意\n2.不同意";
    	byte[] bytes = connectSession.stageService.getTaskBytes((short) 31010,
                new String[] {
					"2",
					"1",
					messageget,
					"master " + MASTER_TYPE_APPRENTICEOK + " " + request.id,
                	"master " + MASTER_TYPE_APPRENTICECANCEL + " " + request.id,
				}
		);
        UWAPSegment seg = new UWAPSegment(ClientConstants.
                                          GET_FILE_OK);
        seg.writeShort((short) 31010);
        seg.writeShort((short) 2);
        seg.write(bytes);
        connectSession.connectService.writeTo(seg, player.getId());
	}

}
