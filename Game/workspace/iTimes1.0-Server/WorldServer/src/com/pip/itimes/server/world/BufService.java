package com.pip.itimes.server.world;


import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.hibernate.util.EqualsHelper;

import com.pip.accountskeleton.AccountSkeleton;
import com.pip.itimes.net.ClientConstants;
import com.pip.itimes.net.UWAPSegment;
import com.pip.itimes.server.bean.Friends;
import com.pip.itimes.server.bean.HopeGrass;
import com.pip.itimes.server.bean.Master;
import com.pip.itimes.server.bean.Mate;
import com.pip.itimes.server.bean.Treasure;
import com.pip.itimes.server.camp.CampConfig;
import com.pip.itimes.server.camp.CampData;
import com.pip.itimes.server.camp.CampSkillData;
import com.pip.itimes.server.camp.CampSkillLevel;
import com.pip.itimes.server.stage.*;
import com.pip.itimes.server.util.IDGenerator;
import com.pip.itimes.server.util.Utils;
import com.pip.itimes.server.world.ItemGroup.BossBattlePlayer;
import com.pip.itimes.server.world.ItemGroup.PetEvolutionTop;
import com.pip.itimes.server.world.ItemGroup.PetEvolutionTopData;
import com.pip.itimes.server.world.camp.CampMainService;
import com.pip.itimes.server.world.equmodle.EquModleConfig;
import com.pip.itimes.server.world.equmodle.EquModleData;
import com.pip.itimes.server.world.game.CampBattlefieldInstance;
import com.pip.itimes.server.world.game.GameMap;
import com.pip.itimes.server.world.game.HouseException;
import com.pip.itimes.server.world.game.HouseInstance;
import com.pip.itimes.server.world.game.HouseInstanceModel;
import com.pip.itimes.server.world.game.InstanceException;
import com.pip.itimes.server.world.game.WorldService;
import com.pip.itimes.server.world.question.QuestionControl;
import com.pip.itimes.server.world.transfer.Equipment;
import com.pip.itimes.server.world.unline.UnlineExpConfig;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class BufService implements Runnable {

//    private int bufId = 0;

    private AtomicInteger bufId = new AtomicInteger(0);


    private ConcurrentHashMap players = new ConcurrentHashMap();

    private ConnectService connectService;
    private StageService stageService;
    private TreasureService treasureService;
    private HopeGrassService hopeGrassService;
    private MateService mateService;
    private PlayerService playerService;
    private MasterService masterService;
    private HouseInstanceModel houseModel;
    private MailService mailService;
    private ChatService chatService;
    private FriendsService friendsService;
    private AddLogService addLogService;
    private CampMainService campMainService;
    private PositionService positionService;
    private WorldService worldService;
    private Random rnd = new Random();
    private AccountSkeleton accountSkeleton;
    private PhizService phizService;
    private TongService tongService;
    
    private static final Logger log = Logger.getLogger(BufService.class);

    public BufService() {
        new Thread(this).start();
    }

    public void setConnectService(ConnectService connectService) {
        this.connectService = connectService;
    }

    public void setStageService(StageService stageService) {
        this.stageService = stageService;
    }

    public void setTreasureService(TreasureService treasureService) {
        this.treasureService = treasureService;
    }

    public void setHopeGrassService(HopeGrassService hopeGrassService) {
        this.hopeGrassService = hopeGrassService;
    }

    public void setMateService(MateService mateService) {
        this.mateService = mateService;
    }

    public void setMasterService(MasterService masterService) {
        this.masterService = masterService;
    }

    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
    }

    public void setHouseModel(HouseInstanceModel houseModel){
        this.houseModel = houseModel;
    }

    public void setMailService(MailService mailService){
        this.mailService = mailService;
    }

    public void setFriendsService(FriendsService friendsService) {
		this.friendsService = friendsService;
	}
    
    public void setAddLogService(AddLogService addLogService) {
		this.addLogService = addLogService;
	}
    
    public void setAccountSkeleton(AccountSkeleton accountSkeleton){
        this.accountSkeleton = accountSkeleton;
    }
    
	public void setCampMainService(CampMainService campMainService) {
		this.campMainService = campMainService;
	}
	
	public void setPositionService(PositionService positionService){
		this.positionService = positionService;
	}
	
	public void setWorldService(WorldService worldService){
		this.worldService = worldService;
	}
	
	public void setPhizService(PhizService phizService){
		this.phizService = phizService;
	}
	
	public void setTongService(TongService tongService){
		this.tongService = tongService;
	}
	
	public int[] playerUseItem(WorldPlayer player, IEffectItem item, Changed changed,boolean mailitem,Client playerclient) throws UseItemException {
		synchronized (this) {
            boolean hasItem = player.hasItem(item, 1);
            boolean ok = false;
            boolean needRemove = true;
            boolean canUseBuff = true;
            int petId = -1;
            if (hasItem) {
// if(item.getType()!=IItem.TYPE_BASIC)
// changed.addItem(item.getItemId(), -1);
                Effect[] effects = item.getEffects();
                int arrBuffPro[]=player.getNewBuf(effects);
                int tmpInd=0;
                for (int i = 0; i < effects.length; i++) {
                    if (effects[i].getType() == 1) {
                        PropertyEffect effect = (PropertyEffect) effects[i];
                        if (effect.getTime() != 0) {
                            Buf buf = new Buf(bufId.incrementAndGet(), effect.getProperty(),
                                              effect.getValue(), effect.getTime(),
                                              effect.getUnit());
                            if(buf.getProperty() == Buf.ENHANCE){//简练buf
                            	Buf enhanceBuf = player.getBuf(Buf.ENHANCE);
                            	if(enhanceBuf != null){
                            		sendMessage(player.getId(), "你的精炼成功率提升还没有使用，请使用后再使用该物品");
                            		int[] ret = new int[2];
                                    ret[0] = 0;
                                    ret[1] = petId;
                            		return ret;
                            	}
                            }
//                            Buf playerbuf[] = player.getBufs();
//                            for(int n = 0; n < playerbuf.length; n++){
//                            	if(playerbuf[n].getProperty() == buf.getProperty()){//存在同一种属性buff
//                            		int tmpvaule = playerbuf[n].getValue();
//                            		if(tmpvaule > buf.getValue()){//当前存在的属性值大于后加的值
//                            			canUseBuff = false;
//                            			break;
//                            		}
//                            	}
//                            }
                            if(canUseBuff){
                            	//lisen modify
                            	//player.addBuf(buf, changed);
                            	tmpInd=player.addBufNew(buf, changed, arrBuffPro,tmpInd);
                            	//end
                            	buf.setTimestamp(System.currentTimeMillis());
                            	/*connectService.sendGetItem(changed, player.getId(), (byte) 4);*/
                            	needRemove = true;
                            } else {
                            	needRemove = false;
                            }
// players.add(player);
                            if (buf.getProperty() < 0) {
                                sendMessage(player.getId(), item.getDesc());
                            }
                            ok = true;
                            
                        } else {
                        	//血蓝全满不使用
                        	ok = true;
                        	if(player.getHp() >= player.getMaxHp() && player.getMp() >= player.getMaxMp()){
                        		needRemove = false;
                        		if(i == effects.length - 1){
                        			sendMessage(player.getId(), "你血蓝全满，无需使用药品");
                        		}
                        	}else{
	                            if (effect.getProperty() == Changed.HP) {
	                            	if(effects.length == 1 && player.getHp() >= player.getMaxHp()){
	                            		needRemove = false;
	                            	}else{
		                                player.addHp(effect.getValue());
		                                if (item.getType() == IItem.TYPE_EXTENDED) {
		                                    changed.addProperty(Changed.HP, effect.getValue());
		                                }
		                                needRemove = true;
	                            	}
	                            } else if (effect.getProperty() == Changed.MP) {
	                            	if(effects.length == 1 && player.getMp() >= player.getMaxMp()){
	                            		needRemove = false;
	                            	}else{
		                                player.addMp(effect.getValue());
		                                if (item.getType() == IItem.TYPE_EXTENDED) {
		                                    changed.addProperty(Changed.MP, effect.getValue());
		                                }
		                                needRemove = true;
	                            	}
	                            }
                        	}
                        }
                    } else if (effects[i].getType() == 2) {
                        GenEffect effect = (GenEffect) effects[i];
                        if (effect.getItemType() == IItem.TYPE_EQU) {
                            IEquipment equ = (IEquipment) Items.getTemplate(effect.getItemId()).newInstance();
                            IEquipment nEqu = (IEquipment) player.completeAddItem(equ, 1, null, player.getClientDataVersion());
                            if (nEqu != null) {
                                changed.addEquipment(nEqu);
                                ok = true;
                                needRemove = true;
                            }else{
                            	if (mailitem){
                            		equ.setBinded(true);
                                    byte[] att = ItemUtils.item2dbAttachment(equ,
                                            1);
                                    mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
                                                        equ.getName(), "", att, 0, true);
                                    sendMessage(player.getId(), "你的背包满了，已经把物品邮寄到你的邮箱!");
                                    ok = true;
                                    needRemove = true;
                            	}else{
                            		ok = false;
                                    needRemove = false;
                            	}
                                
                            }
                        } else {
                            int count = effect.getCount();
                            IItem it = Items.getTemplate(effect.getItemId()).newInstance();
                            if (player.completeAddItem(it, count, changed, player.getClientDataVersion()) != null) {
                                ok = true;
                                needRemove = true;
                            } else {
                            	if (mailitem){
                            		needRemove = true;
                                    ok = true;
                                    it.setBinded(true);
                                    byte[] att = ItemUtils.item2dbAttachment(it,
                                            count);
                                    mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
                                                        it.getName() + "*" + count, "", att, 0, true);
                                    sendMessage(player.getId(), "你的背包满了，已经把物品邮寄到你的邮箱!");
                            	}else{
                            		ok = false;
                                    needRemove = false;
                            	}
                            }
                        }
                    } else if (effects[i].getType() == 6) {
                        AddGridEffect effect = (AddGridEffect) effects[i];
                        if (player.getAddedGridSize() >= effect.getValue()) {
                            throw new UseItemException("你已经使用过更高级别的扩容物品，扩容数量为" + player.getAddedGridSize());
                        }
                        player.setAddedGridSize(effect.getValue());
// player.setAddedGridSize(player.getAddedGridSize() + effect.getValue());
                        changed.setProperty(Changed.GRIDSIZE, player.getAllGridSize());
                        ok = true;
                    } else if (effects[i].getType() == 7) {
                        TitleEffect effect = (TitleEffect) effects[i];
                        player.setTitle(effect.getTitle());
                        changed.setProperty(Changed.TITLE_STRING, effect.getTitle());
                        sendMessage(player.getId(), "换称号成功！");
                        int ret = player.isCanChangeRoleTitle(effect.getTitle());
                        if(ret == 0){		//橱窗里无此称号
                        	player.completeAddRoleTitle(effect.getTitle());			//添加到称号橱窗里
                        }
                        ok = true;
                        needRemove = true;
                    } else if (effects[i].getType() == 8) {
                        byte[] bytes = stageService.getTaskBytes((short) 31001,
                                new String[] {"是否修改称号?\n1.是\n2.否",
                                "输入称号:", "title " + item.getItemId() + " "});
                        UWAPSegment seg = new UWAPSegment(ClientConstants.
                                GET_FILE_OK);
                        seg.writeShort((short) 31001);
                        seg.writeShort((short) 2);
                        seg.write(bytes);
                        connectService.writeTo(seg, player.getId());
                        needRemove = true;
                        ok = true;
                    } else if (effects[i].getType() == 3) {
                    	boolean canTransfer = true;
                    	GameMap map = player.getMap();
            			if(map != null){
            				NoDoor door = NoDoor.getNoTransfer(map.getMapId());
            				if(door != null){
            					sendMessage(player.getId(), door.getMessage());
            					canTransfer = false;
            				}
            			}
            			if(canTransfer){
	                        MoveEffect effect = (MoveEffect) effects[i];
	                        if (effect.getMapId() == -1)
	                            sendGotoMap(player.getId(), (short) - 1, (short) - 1, (short) - 1);
	                        else{
	                        	if(player.getClientDataVersion() > 0){
	                        		sendGotoMap(player.getId(), effect.getNewMapId(), effect.getNewX(), effect.getNewY());
	                        	}else{
	                        		sendGotoMap(player.getId(), effect.getMapId(), effect.getX(), effect.getY());
	                        	}
	                        }
            			}else{
            				needRemove = false;
            			}
            			ok = true;
                    } else if (effects[i].getType() == 10) { // 藏宝图
                        TreasureEffect effect = (TreasureEffect) effects[i];
                        try {
                        	Treasure treasure = null;
                        	if (effect.getShovelId() != -1){//指定挖宝铲的宝藏
                        		treasure = treasureService.getTreasure_bykey(player.getId(),effect.getShovelId());                        		
                        	}else{
                        		treasure = treasureService.getTreasure(player.getId());                                
                        	}
                        	if (treasure != null) {
                                Scene scene = stageService.getScene(treasure.getMapId());
                            	byte[] bytes = stageService.getTaskBytes((short) 31002,
                            			new String[] {"宝藏已在" + scene.getName() + "您是否要替换当前宝藏?替换后原有宝藏会消失。\n1.是\n2.否",
                                        "treasure 0 " + item.getItemId() + " "});
                                UWAPSegment seg = new UWAPSegment(ClientConstants.
                                        GET_FILE_OK);
                                seg.writeShort((short) 31002);
                                seg.writeShort((short) 2);
                                seg.write(bytes);
                                connectService.writeTo(seg, player.getId());
                                needRemove = false;
                                ok = true;
                            } else {
	                            treasureService.createTreasure(player.getId(),
	                                    effect.getMapId(), effect.getMinX(), effect.getMaxX(),
	                                    effect.getMinY(), effect.getMaxY(), effect.getItemGroupId(), effect.getShovelId());
	                            Scene scene = stageService.getScene(effect.getMapId());
	                            sendMessage(player.getId(), "宝藏在" + scene.getName() + "快去寻找吧!");
	                            ok = true;
	                            needRemove = true;
                            }
                        } catch (TreasureException ex) {
                            Treasure treasure = treasureService.getTreasure(player.getId());
                            if (treasure != null) {
                                Scene scene = stageService.getScene(treasure.getMapId());
                                sendMessage(player.getId(), "已经存在宝藏在" + scene.getName() + "快去寻找吧!");
                            }
                            ok = true;
                            needRemove = false;
                        }
                    } else if (effects[i].getType() == 11) { // 挖宝
                    	TreasureFinderEffect effect = (TreasureFinderEffect) effects[i];
                        Treasure treasure = treasureService.getTreasure_bykey(player.getId(),item.getItemId());
                        if (treasure == null) {
                            sendMessage(player.getId(), "您还没有发现任何宝藏，请先使用藏宝图找到宝藏地点。");
                            needRemove = false;
                            ok = true;
                        } else {
                        	if (treasure.getShovelId() == -1 || treasure.getShovelId() == item.getItemId()) {
	                    		if (treasure.getMapId() == player.getMapId()) {
	                                short x = player.getX();
	                                short y = player.getY();
	                                if (treasure.isValid(x, y)) {
	                                	DropGroup group = DropGroups.getDropGroup(
                                				treasure.getItemGroupId(), player.getLevel());
                                		if (group != null) {
                                			int rate = rnd.nextInt(group.getRate());
                                			DropItem dropItem = group.calcDropItem(
                                					rate);
                                			int count = getCount(rnd,
                                					dropItem.getMin(),
                                					dropItem.getMax());
                                			int addCount = player.addItem(dropItem.getItem(), count,
                                					changed, player.getClientDataVersion());
                                			if(addCount <= 0){//改成发邮件,不扣挖宝铲
                            					IItem iit = Items.getTemplate(dropItem.getItem().getItemId()).newInstance();
                            					byte[] att = ItemUtils.item2dbAttachment(iit,count);
                            					mailService.sendMail(player.getId(),
                            							player.getPlayerName(), -1, "挖宝",
                            							iit.getName() + "*" + count, "挖宝相关物品", att, 0,
                            							true);
                            					sendMessage(player.getId(), "背包已满，物品已经发送到邮箱");
                            					treasureService.deleteTreasure(treasure);
                            					needRemove = false;
                                			}else{
	                                			log.info("ID[" + player.getId() + "]TreasureItem[" + dropItem.getItem().getItemId() +
	                                					"]Count[" + count + "]");
	                                			treasureService.deleteTreasure(treasure);
	                                			sendMessage(player.getId(), "恭喜!你幸运地挖到了宝藏.");
	                                			if (effect.getDelete() == 1){
	                                				needRemove = true;
	                                			}else{
	                                				needRemove = false;
	                                			}
                                			}
                                		}
	                                } else {
	                                    sendMessage(player.getId(), "宝藏在当前区域的" + treasure.getNotifyMessage(x, y));
	                                    needRemove = false;
	                                }
	                                ok = true;
	                            }else{
	                                Scene scene = stageService.getScene(treasure.getMapId());
	                                sendMessage(player.getId(), "宝藏在" + scene.getName() + "，请先从世界地图传送过去再挖吧!");
	                                ok = true;
	                                needRemove = false;
	                            }
                        	} else {
                        		IItem tmpItem = Items.getTemplate(treasure.getShovelId()).newInstance();
                        		if (tmpItem != null) {
                        			sendMessage(player.getId(), "需要" + tmpItem.getName() + "才能挖掘宝藏!");
                        		}
                        		ok = true;
                                needRemove = false;
                        	}
                        }
                    } else if (effects[i].getType() == 12) { // 种植希望草种子
                        HopeGrassEffect effect = (HopeGrassEffect) effects[i];
                        short x = player.getX();
                        short y = player.getY();
                        short mapId = player.getMapId();
                        InstanceDefinition instance = worldService.getInstanceDefinition(mapId);
                        if(instance == null){
                        	HopeGrass grass = hopeGrassService.createHopeGrass(
                        			player.getId(), effect.getItemGroupId(), mapId,
                        			(short) (x / 32), (short) (y / 32),
                        			effect.getValidTime(), effect.getObsoleteTime(), effect.getGrassType(), effect.getRatio(),
                        			effect.getGrouprnd());
                        	if (grass == null)
                        		throw new UseItemException("种植失败");
                        	if (effect.getGrassType() == 0)
                        		sendMessage(player.getId(), "你偷偷的在此挖了个坑,种下了一颗种子.");
                        	else if (effect.getGrassType() == 1) {
                        		sendMessage(player.getId(), "你将漂流瓶轻轻的放在此地.");
                        	}else if (effect.getGrassType() == 2) {
                        		sendMessage(player.getId(), "你轻轻的种下了菊花种子.");
                        	}
                        }else{
                        	if (effect.getGrassType() == 0)
                        		sendMessage(player.getId(), "你所在的区域无法播种。");
                        	else if (effect.getGrassType() == 1) {
                        		sendMessage(player.getId(), "你所在的区域无法放置漂流瓶。");
                        	}else if (effect.getGrassType() == 2) {
                        		sendMessage(player.getId(), "你所在的区域无法播种。");
                        	}
                        	needRemove = false;
                        }
                        ok = true;
                    } else if (effects[i].getType() == 13) { // 挖希望草种子
                        short x = player.getX();
                        short y = player.getY();
                        short mapId = player.getMapId();
                        HopeGrassFinderEffect effect = (HopeGrassFinderEffect) effects[i];
                        List grasses = hopeGrassService.getHopeGrass(mapId, (short) (x / 32), (short) (y / 32),
                                effect.getGrassType());
                        if (grasses.size() == 0) {
                        	//毒瘤
                        	if (effect.getGrassType() == 1) {
                        		List grasses2 = hopeGrassService.getHopeGrass(mapId, (short) (x / 32), (short) (y / 32),
                                        2);
                        		if (grasses2 == null){
                        			throw new UseItemException("这里什么也没有。");
                        		}else{
                        			Iterator ite = grasses2.iterator();
                                    long current = System.currentTimeMillis();
                                    if (!ite.hasNext()){
                                    	throw new UseItemException("这里什么也没有。");
                                    }
                                    while (ite.hasNext()) {
                                        HopeGrass grass2 = (HopeGrass) ite.next();
                                        if (current >= grass2.getValidTime().getTime() && current <= grass2.getObsoleteTime().getTime()) {
                                            if (grass2.getGrassType() == 2) {
                                                if (grass2.getPlayerId() == player.getId()) {
                                                    if (player.getMaxLevel() > player.getLevel()) {
                                                        int exp = BathHouse.EXP[player.getLevel()] * grass2.getRatio() * 6 / 100;
                                                        int level_tmp = player.getLevel();
                                                        player.addExp(exp, changed);
                                                        if(level_tmp < player.getLevel()){
                                                        	//推荐人通用函数
                                                        	playerService.recommendBalance(player, "hopegrass2");
                                                        	//尝试加到师傅的列表中
                                                        	playerService.addMasterPlayer(player, changed);
                                                        	
                                                        	if(masterService.isPrentice(player)){
                                                        		try{
                                                        			Changed changed1 = new Changed(), changed2 = new Changed();
                                                        			masterService.unRelation(player, changed1, changed2);
                                                        			
                                                        		}catch(Exception e){
                                                        		}
                                                        	}
                                                        }
//                                                        sendMessage(player.getId(),
//                                                                player.getPlayerName() + ",你找到了自己的漂流瓶!继续寻找他人的吧,这样才能交到朋友!");
                                                    }else{
                                                    	player.addCredit(8, changed);
                                                    }
                                                    if (Utils.hit(rnd,grass2.getGrouprnd(),100)){
                                                    	//几率掉落
                                                    	DropGroup group = DropGroups.getDropGroup(
                                                                grass2.getItemGroupId(),player.getLevel());
                                                    	if(group != null){
	                                                        int rate = rnd.nextInt(group.getRate());
	                                                        DropItem dropItem = group.calcDropItem(
	                                                                rate);
	                                                        int count = getCount(rnd,
	                                                                dropItem.getMin(),
	                                                                dropItem.getMax());
	                                                        IItem di = dropItem.getItem().newInstance();
	                                                        if (player.completeAddItem(di, count,changed, player.getClientDataVersion()) == null){
	                                                        	byte[] att = ItemUtils.item2dbAttachment(di,
	                                                                    count);
	                                                            mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
	                                                                                di.getName(), "", att, 0, true);
	                                                            sendMessage(player.getId(),"你的背包满了，已经把物品邮寄到你的邮箱!");
	                                                        }
	                                                        //mengjie add
	                                                        int item_id = 0;
	                                                        item_id = di.getItemId();
	                                                        String tmp = "";
	                                                        if (grass2.getGrassType() == 1)
	                                                        	tmp = "水晶之恋漂流瓶";
	                                                        else  if (grass2.getGrassType() == 2)
	                                                        	tmp = "菊花";
	                                                        String item_msg = Items.getMessage(item_id,8,player.getPlayerName(),di.getName(),tmp);
	                                                        if (item_msg != null){
	                                                        	chatService.sendWorldMessage(-1, "系统", item_msg);
	                                                        }
                                                    	}
                                                    }
                                                    if (grass2.getGrassType() == 1)
                                                    sendMessage(player.getId(),
                                                            player.getPlayerName() + ",你找到了自己的漂流瓶!继续寻找他人的吧,这样才能交到朋友!");
                                                    else  if (grass2.getGrassType() == 2)
                                                    sendMessage(player.getId(),
                                                            player.getPlayerName() + ",你挖到到了自己种菊花!继续寻找他人的吧,这样才能交到朋友!");

                                                } else {
                                                    WorldPlayer source = null;
                                                    boolean acquire = true;
                                                    try {
                                                    	source = playerService.getWorldPlayerAndCatch(grass2.getPlayerId());
                                                    } catch (Exception ex1) {
                                                    	acquire = false;
                                                    }
                                                    if (player.getMaxLevel() > player.getLevel()) {
                                                        int exp = BathHouse.EXP[player.getLevel()] * grass2.getRatio() * 2 / 100;
                                                        int level_tmp = player.getLevel();
                                                        player.addExp(exp, changed);
                                                        if (level_tmp<player.getLevel()){
	                                                        //推荐人通用函数
	                                                    	playerService.recommendBalance(player, "hopegrass2");
	                                                    	//尝试加到师傅的列表中
                                                        	playerService.addMasterPlayer(player, changed);
                                                        }
//                                                        sendMessage(player.getId(),
//                                                                (source != null ? source.getPlayerName() : "无名氏") +
//                                                                "留言：今生有缘，愿咱们成为朋友！");
                                                    }else{
                                                    	player.addCredit(2, changed);
                                                    }
                                                    //player.addCredit(2, changed);
                                                    if(grass2.getGrassType() == 1)
                                                    	sendMessage(player.getId(),
                                                                (source != null ? source.getPlayerName() : "无名氏") +
                                                                "留言：今生有缘，愿咱们成为朋友！");
                                                    else  if (grass2.getGrassType() == 2)
                                                    	sendMessage(player.getId(),
                                                                (source != null ? source.getPlayerName() : "无名氏") +
                                                                "种的菊花被你挖到了，愿咱们成为朋友！");
                                                    if (Utils.hit(rnd,grass2.getGrouprnd(),100)){
                                                    	//几率掉落
                                                    	DropGroup group = DropGroups.getDropGroup(
                                                                grass2.getItemGroupId(),player.getLevel());
                                                    	if(group != null){
	                                                        int rate = rnd.nextInt(group.getRate());
	                                                        DropItem dropItem = group.calcDropItem(
	                                                                rate);
	                                                        int count = getCount(rnd,
	                                                                dropItem.getMin(),
	                                                                dropItem.getMax());
	                                                        IItem di = dropItem.getItem().newInstance();
	                                                        if (player.completeAddItem(di, count,changed, player.getClientDataVersion()) == null){
	                                                        	byte[] att = ItemUtils.item2dbAttachment(di,
	                                                                    count);
	                                                            mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
	                                                                                di.getName(), "", att, 0, true);
	                                                            sendMessage(player.getId(),"你的背包满了，已经把物品邮寄到你的邮箱!");
	                                                        }
	                                                      //mengjie add
	                                                        int item_id = 0;
	                                                        item_id = di.getItemId();
	                                                        String tmp = "";
	                                                        if (grass2.getGrassType() == 1)
	                                                        	tmp = "水晶之恋漂流瓶";
	                                                        else  if (grass2.getGrassType() == 2)
	                                                        	tmp = "菊花";
	                                                        String item_msg = Items.getMessage(item_id,8,player.getPlayerName(),di.getName(),tmp);
	                                                        if (item_msg != null){
	                                                        	chatService.sendWorldMessage(-1, "系统", item_msg);
	                                                        }
                                                    	}
                                                    }
                                                    if (source != null) {
                                                        Changed changed1 = new Changed();
                                                        if (source.getMaxLevel() > source.getLevel()) {
                                                            int exp1 = BathHouse.EXP[source.getLevel()] * grass2.getRatio() * 8 /
                                                                    100;
//                                                            Changed changed1 = new Changed();
                                                            int level_tmp = source.getLevel();
                                                            source.addExp(exp1, changed1);
                                                            if(level_tmp<source.getLevel()){
	                                                            //推荐人通用函数
	                                                        	playerService.recommendBalance(source, "hopegrass2");
	                                                        	//尝试加到师傅的列表中
	                                                        	playerService.addMasterPlayer(source, changed1);
                                                            }
//                                                            if (source.online())
//                                                                connectService.sendGetItem(changed1, source.getId(), (byte) 50);
                                                        }
//                                                        Changed changed1 = new Changed();
                                                        else{
                                                        	source.addCredit(10, changed1);
                                                        }
                                                        //source.addCredit(10, changed1);
                                                        if (source.online()){
                                                            connectService.sendGetItem(changed1, source.getId(), (byte) 50);
                                                        }
                                                        if(grass2.getGrassType() == 1){
                                                        	sendMessage(source.getId(),(player != null ? player.getPlayerName() : "无名氏") +
                                                        			"挖到你所埋的漂流瓶啦,真是有缘啊！");
                                                        }
                                                        else  if (grass2.getGrassType() == 2){
                                                        		sendMessage(source.getId(),(player != null ? player.getPlayerName() : "无名氏") +
                                                    				"摘到了你种的菊花啦,真是有缘啊！");
                                                        }
                                                    }
                                                    if(acquire){
                                                    	playerService.releasePlayer(source);
                                                    }
                                                }
                                            }
                                            hopeGrassService.deleteHopeGrass(grass2);
                                        } else if (current < grass2.getValidTime().getTime()) {
                                            if(grass2.getGrassType() == 1)
                                                sendMessage(player.getId(), "貌似漂流瓶中有个字条，但看不清楚.");
                                            else
                                                sendMessage(player.getId(), "这里的菊花还没有盛开.");
                                        } else if (current > grass2.getValidTime().getTime()) {
                                            if (grass2.getGrassType() == 1)
                                                sendMessage(player.getId(), "漂流瓶已经破碎，字条上字迹再也看不清了!");
                                            else if (grass2.getGrassType() == 2)
                                            	sendMessage(player.getId(), "这里的菊花已经枯萎了!");
                                            hopeGrassService.deleteHopeGrass(grass2);
                                        }
                                    }
                        		}
                        	}else
                            if (effect.getGrassType() == 0)
                                throw new UseItemException("没发现可收获的物品");
                            else 
                                throw new UseItemException("这里没有发现漂流瓶。");
                        }else{
                        	Iterator ite = grasses.iterator();
                            long current = System.currentTimeMillis();
                            while (ite.hasNext()) {
                                HopeGrass grass = (HopeGrass) ite.next();
                                if (current >= grass.getValidTime().getTime() && current <= grass.getObsoleteTime().getTime()) {
                                    if (grass.getGrassType() == 0) {
                                        DropGroup group = DropGroups.getDropGroup(
                                                grass.getItemGroupId(),player.getLevel());
                                        if(group != null){
	                                        int rate = rnd.nextInt(group.getRate());
	                                        DropItem dropItem = group.calcDropItem(
	                                                rate);
	                                        int count = getCount(rnd,
	                                                dropItem.getMin(),
	                                                dropItem.getMax());
	                                        IItem tmpItem = dropItem.getItem().newInstance();
	                                        sendMessage(player.getId(), "恭喜!种瓜得瓜,你终于有收获啦!");
	                                        if (player.completeAddItem(tmpItem, count,changed, player.getClientDataVersion()) == null){
                                            	byte[] arrAtt = ItemUtils.item2dbAttachment(tmpItem,count);
                                                mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
                                                		tmpItem.getName(), "", arrAtt, 0, true);
                                                sendMessage(player.getId(),"你的背包满了，已经把物品邮寄到你的邮箱!");
                                            }
	                                        String item_msg = Items.getMessage(tmpItem.getItemId(),8,player.getPlayerName(),tmpItem.getName(),"");
                                            if (item_msg != null){
                                            	chatService.sendWorldMessage(-1, "系统", item_msg);
                                            }
//	                                        player.addItem(dropItem.getItem(), count,
//	                                                changed, player.getClientDataVersion());
                                        }
                                    } else if (grass.getGrassType() > 0) {
                                        if (grass.getPlayerId() == player.getId()) {
                                            if (player.getMaxLevel() > player.getLevel()) {
                                                int exp = BathHouse.EXP[player.getLevel()] * grass.getRatio() * 6 / 100;
                                                int level_tmp = player.getLevel();
                                                player.addExp(exp, changed);
                                                if(level_tmp<player.getLevel()){
                                                	//推荐人通用函数
                                                	playerService.recommendBalance(player, "hopegrass");
                                                	//尝试加到师傅的列表中
                                                	playerService.addMasterPlayer(player, changed);
                                                }
//                                                sendMessage(player.getId(),
//                                                        player.getPlayerName() + ",你找到了自己的漂流瓶!继续寻找他人的吧,这样才能交到朋友!");
                                            }else{
                                            	player.addCredit(8, changed);
                                            }
                                            if (Utils.hit(rnd,grass.getGrouprnd(),100)){
                                            	//几率掉落
                                            	DropGroup group = DropGroups.getDropGroup(
                                                        grass.getItemGroupId(),player.getLevel());
                                            	if(group != null){
	                                                int rate = rnd.nextInt(group.getRate());
	                                                DropItem dropItem = group.calcDropItem(
	                                                        rate);
	                                                int count = getCount(rnd,
	                                                        dropItem.getMin(),
	                                                        dropItem.getMax());
	                                                IItem di = dropItem.getItem().newInstance();
	                                                if (player.completeAddItem(di, count,changed, player.getClientDataVersion()) == null){
	                                                	byte[] att = ItemUtils.item2dbAttachment(di,
	                                                            count);
	                                                    mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
	                                                                        di.getName(), "", att, 0, true);
	                                                    sendMessage(player.getId(),"你的背包满了，已经把物品邮寄到你的邮箱!");
	                                                }
	                                              //mengjie add
	                                                int item_id = 0;
	                                                item_id = di.getItemId();
	                                                String item_msg = Items.getMessage(item_id,8,player.getPlayerName(),di.getName(),"水晶之恋漂流瓶");
	                                                if (item_msg != null){
	                                                	chatService.sendWorldMessage(-1, "系统", item_msg);
	                                                }
                                            	}
                                            }
                                            if (effect.getGrassType() == 1)
                                            sendMessage(player.getId(),
                                                    player.getPlayerName() + ",你找到了自己的漂流瓶!继续寻找他人的吧,这样才能交到朋友!");
                                            else  if (effect.getGrassType() == 2)
                                            sendMessage(player.getId(),
                                                    player.getPlayerName() + ",你挖到到了自己种漂流瓶!继续寻找他人的吧,这样才能交到朋友!");

                                        } else {
                                            WorldPlayer source = null;
//                                            boolean acquire = false;
//                                            try {
//                                                source = playerService.loadWorldPlayer(grass.getPlayerId());
//                                                playerService.acquire(source);
//                                                acquire = true;
//                                            } catch (Exception ex1) {
//                                            }
                                            source = playerService.getWorldPlayerAndCatch(grass.getPlayerId());
                                            if (player.getMaxLevel() > player.getLevel()) {
                                                int exp = BathHouse.EXP[player.getLevel()] * grass.getRatio() * 2 / 100;
                                                int level_tmp = player.getLevel();
                                                player.addExp(exp, changed);
                                                if(level_tmp<player.getLevel()){
                                                	//推荐人通用函数
                                                	playerService.recommendBalance(player, "hopegrass");
                                                	//尝试加到师傅的列表中
                                                	playerService.addMasterPlayer(player, changed);
                                                }
//                                                sendMessage(player.getId(),
//                                                        (source != null ? source.getPlayerName() : "无名氏") +
//                                                        "留言：今生有缘，愿咱们成为朋友！");
                                            }else{
                                            	player.addCredit(2, changed);
                                            }
                                            //player.addCredit(2, changed);
                                            if(effect.getGrassType() == 1)
                                            	sendMessage(player.getId(),
                                                        (source != null ? source.getPlayerName() : "无名氏") +
                                                        "留言：今生有缘，愿咱们成为朋友！");
                                            else  if (effect.getGrassType() == 2)
                                            	sendMessage(player.getId(),
                                                        (source != null ? source.getPlayerName() : "无名氏") +
                                                        "种的菊花被你挖到了，愿咱们成为朋友！");
                                            if (Utils.hit(rnd,grass.getGrouprnd(),100)){
                                            	//几率掉落
                                            	DropGroup group = DropGroups.getDropGroup(
                                                        grass.getItemGroupId(),player.getLevel());
                                            	if(group != null){
	                                                int rate = rnd.nextInt(group.getRate());
	                                                DropItem dropItem = group.calcDropItem(
	                                                        rate);
	                                                int count = getCount(rnd,
	                                                        dropItem.getMin(),
	                                                        dropItem.getMax());
	                                                IItem di = dropItem.getItem().newInstance();
	                                                if (player.completeAddItem(di, count,changed, player.getClientDataVersion()) == null){
	                                                	byte[] att = ItemUtils.item2dbAttachment(di,
	                                                            count);
	                                                    mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
	                                                                        di.getName(), "", att, 0, true);
	                                                    sendMessage(player.getId(),"你的背包满了，已经把物品邮寄到你的邮箱!");
	                                                }
	                                              //mengjie add
	                                                int item_id = 0;
	                                                item_id = di.getItemId();
	                                                String item_msg = Items.getMessage(item_id,8,player.getPlayerName(),di.getName(),"水晶之恋漂流瓶");
	                                                if (item_msg != null){
	                                                	chatService.sendWorldMessage(-1, "系统", item_msg);
	                                                }
                                            	}
                                            }
                                            if (source != null) {
                                                Changed changed1 = new Changed();
                                                if (source.getMaxLevel() > source.getLevel()) {
                                                    int exp1 = BathHouse.EXP[source.getLevel()] * grass.getRatio() * 8 /
                                                            100;
//                                                    Changed changed1 = new Changed();
                                                    int level_tmp = source.getLevel();
                                                    source.addExp(exp1, changed1);
                                                    if(level_tmp<source.getLevel()){
                                                    	//推荐人通用函数
                                                    	playerService.recommendBalance(source, "hopegrass");
                                                    	//尝试加到师傅的列表中
                                                    	playerService.addMasterPlayer(source, changed1);
                                                    }
//                                                    if (source.online())
//                                                        connectService.sendGetItem(changed1, source.getId(), (byte) 50);
                                                }
//                                                Changed changed1 = new Changed();
                                                else{
                                                	source.addCredit(10, changed1);
                                                }
                                                //source.addCredit(10, changed1);
                                                if (source.online())
                                                    connectService.sendGetItem(changed1, source.getId(), (byte) 50);
                                            	if(effect.getGrassType() == 1)
                                            		sendMessage(source.getId(),(player != null ? player.getPlayerName() : "无名氏") +
                                            			"挖到你所埋的漂流瓶啦,真是有缘啊！");
                                            	else  if (effect.getGrassType() == 2)
                                            		sendMessage(source.getId(),(player != null ? player.getPlayerName() : "无名氏") +
                                        				"摘到了你种的菊花啦,真是有缘啊！");
//                                                if(acquire){
//                                                    playerService.release(source);
//                                                }
                                            	playerService.releasePlayer(source);
                                            }
                                        }
                                    }
                                    hopeGrassService.deleteHopeGrass(grass);
                                } else if (current < grass.getValidTime().getTime()) {
                                    if (grass.getGrassType() == 0)
                                        sendMessage(player.getId(), "貌似一根小芽破土而出,但不知何时能长大.");
                                    else if(grass.getGrassType() == 1)
                                        sendMessage(player.getId(), "貌似漂流瓶中有个字条，但看不清楚.");
                                    else if(grass.getGrassType() == 2)
                                        sendMessage(player.getId(), "这里的菊花还没有盛开.");
                                } else if (current > grass.getValidTime().getTime()) {
                                    if (grass.getGrassType() == 0)
                                        sendMessage(player.getId(), "真可惜！种子枯萎了,不能收获了!");
                                    else if (grass.getGrassType() == 1)
                                        sendMessage(player.getId(), "漂流瓶已经破碎，字条上字迹再也看不清了!");
                                    else if (grass.getGrassType() == 2)
                                    	sendMessage(player.getId(), "这里的菊花已经枯萎了!");
                                    hopeGrassService.deleteHopeGrass(grass);
                                }
                            }
                        }
                        needRemove = false;
                        ok = true;
                    } else if (effects[i].getType() == 14) { // 表情物品
                        byte[] bytes = stageService.getTaskBytes((short) 31001,
                                new String[] {"输入信息?\n1.是\n2.否",
                                "信息:", "send_message " + item.getItemId() + " "});
                        UWAPSegment seg = new UWAPSegment(ClientConstants.
                                GET_FILE_OK);
                        seg.writeShort((short) 31001);
                        seg.writeShort((short) 2);
                        seg.write(bytes);
                        connectService.writeTo(seg, player.getId());
                        needRemove = false;
                        ok = true;
                    } else if (effects[i].getType() == 15) { // 幸运面包
//						LuckyBufEffect effect = (LuckyBufEffect) effects[i];
//						LuckyBuf buf = new LuckyBuf();
//						buf.setExpRatio(effect.getExpRadio());
//						buf.setMoneyRatio(effect.getMoneyRadio());
//						buf.setValidTime(System.currentTimeMillis() + effect.getValidTime() * 1000L);
//						player.setLuckyBuf(buf);
//						sendMessage(player.getId(), "弗艾蒂斯幸运之神将运气降临在你的身上!");
//						ok = true;
                    } else if (effects[i].getType() == 16) { // 幸运时间
                        sendMessage(player.getId(), "你今天的开始幸运时间为:" + Utils.getLuckyTimeString(player.getSiderealTime()));
                        ok = true;
                    } else if (effects[i].getType() == 17) { // 窥视面包
                        byte[] bytes = stageService.getTaskBytes((short) 31001,
                                new String[] {"可以偷窥的哦,请仔细输入对方的名字?\n1.是\n2.否",
                                "名字:", "look_package " + item.getItemId() + " "});
                        UWAPSegment seg = new UWAPSegment(ClientConstants.
                                GET_FILE_OK);
                        seg.writeShort((short) 31001);
                        seg.writeShort((short) 2);
                        seg.write(bytes);
                        connectService.writeTo(seg, player.getId());
                        needRemove = false;
                        ok = true;
                    } else if (effects[i].getType() == 18) {
                        byte[] bytes = stageService.getTaskBytes((short) 31001,
                                new String[] {"输入传送的地名?\n1.是\n2.否",
                                "地名:", "freemove " + item.getItemId() + " "});
                        UWAPSegment seg = new UWAPSegment(ClientConstants.
                                GET_FILE_OK);
                        seg.writeShort((short) 31001);
                        seg.writeShort((short) 2);
                        seg.write(bytes);
                        connectService.writeTo(seg, player.getId());
                        needRemove = false;
                        ok = true;
                    } else if (effects[i].getType() == 19) { // 爱之神光
                        Mate mate = mateService.getMate(player);
                        if (mate == null) {
                            needRemove = true;
                            ok = true;
                            sendMessage(player.getId(), "你没有伴侣");
                        } else {
                            int destId = mate.getHusbandId();
                            if (mate.getHusbandId() == player.getId()) {
                                destId = mate.getWifeId();
                            }
                            WorldPlayer matePlayer = playerService.
                                    getWorldPlayer(destId);
                            if (matePlayer == null) {
                                needRemove = false;
                                ok = true;
                                sendMessage(player.getId(), "你的伴侣不在线");
                            } else {
                                GameMap map = matePlayer.getMap();
                                if (map == null) {
                                    sendMessage(player.getId(), " 伴侣位置信息错误");
                                    needRemove = false;
                                    ok = true;
                                } else {
                                    if (map.getInstance() != null) {
                                        sendMessage(player.getId(), " 伴侣位置信息错误");
                                        needRemove = false;
                                        ok = true;
                                    } else {
                                    	boolean canTransfer = true;
                                    	GameMap playerMap = player.getMap();
                                    	if(playerMap != null){
                            				NoDoor door = NoDoor.getNoTransfer(playerMap.getMapId());
                            				if(door != null){
                            					sendMessage(player.getId(), door.getMessage());
                            					canTransfer = false;
                            				}
                            			}
                                    	if(canTransfer){
	                                        NoDoor door = NoDoor.getNoDoor(map.getMapId());
	                                        if (door != null) {
	                                            sendMessage(player.getId(), door.getMessage());
	                                            needRemove = false;
	                                            ok = true;
	                                        }else{
	                                            sendGotoMap(player.getId(),
	                                                    map.getMapId(), (short) (matePlayer.getX() / map.getTileWidth()),
	                                                    (short) (matePlayer.getY() / map.getTileHeight()));
	                                            needRemove = true;
	                                            ok = true;
	                                        }
                                    	}else{
                                    		ok = true;
                                    		needRemove = false;
                                    	}
                                    }
                                }
                            }
                        }
                    } else if (effects[i].getType() == 20) { // 伴侣称谓
                        Mate mate = mateService.getMate(player);
                        if (mate == null) {
                            needRemove = true;
                            ok = true;
                            sendMessage(player.getId(), "你没有伴侣");
                        } else {
                            String title = "";

                            if (mate.getHusbandId() == player.getId()) {
                                title = "[" + mate.getWifeName() + "的丈夫]";
                            } else {
                                title = "[" + mate.getHusbandName() + "的妻子]";
                            }

                            player.setTitle(title);
                            int ret = player.isCanChangeRoleTitle(title);
                            if(ret == 0){		//橱窗里无此称号
                            	player.completeAddRoleTitle(title);			//添加到称号橱窗里
                            }
                            changed.setProperty(Changed.TITLE_STRING, title);

                            needRemove = true;
                            ok = true;
                        }
                    } else if (effects[i].getType() == 21) { // 师徒称谓
                        Master masterRelation = masterService.getMasterRelation(player);
                        if (masterRelation == null) {
                            needRemove = true;
                            ok = true;
                            sendMessage(player.getId(), "你没有师傅");
                        } else {
                            needRemove = true;
                            ok = true;
                            
                            String title = "[" + masterRelation.getMasterName() + "的徒弟]";
                            player.setTitle(title);
                            
                            //放进形象称号柜中
                            if(player.isCanChangeRoleTitle(title) == 0){
                            	player.completeAddRoleTitle(title);
                            }

                            changed.setProperty(Changed.TITLE_STRING, title);
                        }
                    } else if (effects[i].getType() == 22) { // 洗点
                    	if (player.getMap() != null && player.getMap().getInstance() != null
                        		&& player.getMap().getInstance() instanceof CampBattlefieldInstance) {
                        	sendMessage(player.getId(), "战场中禁止使用。");
                        	needRemove = false;
                    	}else{
		                    player.resetProperties(changed);
		                    needRemove = true;
                    	}
                        ok = true;
                    } else if (effects[i].getType() == 23) { // 获得老师标题
                        String title = masterService.getMasterTitle(player);
                        player.setTitle(title);
                        
                        //放进形象称号柜中
                        if(player.isCanChangeRoleTitle(title) == 0){
                        	player.completeAddRoleTitle(title);
                        }
                        
                        changed.setProperty(Changed.TITLE_STRING, title);
                        needRemove = true;
                        ok = true;
                    } else if (effects[i].getType() == 24) { // 小队传送
                        Team team = player.getTeam();
                        if (team == null) {
                            needRemove = false;
                            ok = false;
                        } else {
                            if (team.getLeader() == player) {
                                needRemove = false;
                                ok = false;
                            } else {
                                WorldPlayer leader = (WorldPlayer)team.getLeader();
                                GameMap map = leader.getMap();
                                if (map == null) {
                                    sendMessage(player.getId(), " 队长位置信息错误");
                                    needRemove = false;
                                    ok = true;
                                } else {
                                    if (map.getInstance() != null) {
                                        sendMessage(player.getId(), " 队长位置信息错误");
                                        needRemove = false;
                                        ok = true;
                                    } else {
                                    	boolean canTransfer = true;
                                    	GameMap playerMap = player.getMap();
                                    	if(playerMap != null){
                            				NoDoor door = NoDoor.getNoTransfer(playerMap.getMapId());
                            				if(door != null){
                            					sendMessage(player.getId(), door.getMessage());
                            					canTransfer = false;
                            				}
                            			}
                                    	if(canTransfer){
	                                        NoDoor door = NoDoor.getNoDoor(map.getMapId());
	                                        if(door!=null){
	                                            sendMessage(player.getId(),door.getMessage());
	                                            needRemove = false;
	                                            ok = true;
	                                        }else{
	                                            sendGotoMap(player.getId(),
	                                                    map.getMapId(), (short) (leader.getX() / map.getTileWidth()),
	                                                    (short) (leader.getY() / map.getTileHeight()));
	                                            needRemove = true;
	                                            ok = true;
	                                        }
                                    	}else{
                                    		ok = true;
                                    		needRemove = false;
                                    	}
                                    }
                                }
                            }
                        }

                    } else if (effects[i].getType() == 25) { // 变性
                        if (player.getLevel() > 20) {
                            sendMessage(player.getId(), "超过20级就不能再改变性别了哦");
                            needRemove = false;
                            ok = true;
                        } else {
                            if (player.getSex() == 0) {
                                player.setSex((byte) 1);
                                if(player.getCamp() == 0 || player.getFace() <= 1){
    	        	            	player.setFace((short) 1);
    	        	            }else{
    	        	            	if(player.getCamp() == 1){// 黑暗阵营
    	        	            		player.setFace((short) 31);
    	        	            	}else{
    	        	            		player.setFace((short) 29);
    	        	            	}
    	        	            }
                                changed.setProperty(Changed.SEX, player.getSex());
                                Client client = player.getClient();
    	                    	if(client != null && client.getDataVersion() > 0){
    	                    		Changed changed2 = new Changed();
    	                    		changed2.setProperty(Changed.FACE, player.getFace());
    	                    		connectService.sendGetItem(changed2, player.getId(), (byte) 33);
    	                    	}else{
    	                    		sendMessage(player.getId(), "性别已更改,将在您下次登陆时生效。");
    	                    	}
//                                sendMessage(player.getId(), "变性成功,需要下线才能生效");
                                needRemove = true;
                                ok = true;
                            } else {
                                player.setSex((byte) 0);
    	        	            if(player.getCamp() == 0 || player.getFace() <= 1){
    	        	            	player.setFace((short) 0);
    	        	            }else{
    	        	            	if(player.getCamp() == 1){// 黑暗阵营
    	        	            		player.setFace((short) 30);
    	        	            	}else{
    	        	            		player.setFace((short) 28);
    	        	            	}
    	        	            }
                                changed.setProperty(Changed.SEX, player.getSex());
                                Client client = player.getClient();
    	        	            if(client != null && client.getDataVersion() > 0){
    	        	            	Changed changed2 = new Changed();
    	        	            	changed2.setProperty(Changed.FACE, player.getFace());
    	                    		connectService.sendGetItem(changed2, player.getId(), (byte) 33);
    	                    	}else{
    	                    		sendMessage(player.getId(), "性别已更改,将在您下次登陆时生效。");
    	                    	}
    	        	            
//                                sendMessage(player.getId(), "变性成功,需要下线才能生效");
                                needRemove = true;
                                ok = true;
                            }
                            player.changeRoleFace();			//人物橱窗里的形象变更
                        }
                    } else if (effects[i].getType() == 26) { // 解除婚姻
                        Mate mate = mateService.getMate(player);
                        if (mate == null) {
                            sendMessage(player.getId(), "你还不能使用此物品");
                            needRemove = false;
                            ok = true;
                        } else {
                            needRemove = false;
                            ok = true;
                            byte[] bytes = stageService.getTaskBytes((short) 31002,
                                    new String[] {"你要解除的婚姻关系么?\n1.没错\n2.没有的事",
                                    "itemunmarry"});
                            UWAPSegment seg = new UWAPSegment(ClientConstants.
                                    GET_FILE_OK);
                            seg.writeShort((short) 31002);
                            seg.writeShort((short) 2);
                            seg.write(bytes);
                            connectService.writeTo(seg, player.getId());
                        }
                    } else if (effects[i].getType() == 27) { // 解除师徒
                        needRemove = false;
                        ok = true;
                        Master[] masters = masterService.getRelation(player);
                        if (masters != null) {
                            byte[] bytes = stageService.getTaskBytes((short) 31010, getUnRelationString(masters));
                            UWAPSegment seg = new UWAPSegment(ClientConstants.
                                    GET_FILE_OK);
                            seg.writeShort((short) 31010);
                            seg.writeShort((short) 2);
                            seg.write(bytes);
                            connectService.writeTo(seg, player.getId());
                        } else {
                            Master master = masterService.getMasterRelation(player);
                            if (master != null) {
                                /*byte[] bytes = stageService.getTaskBytes((short) 31003,
                                        new String[] {
                                        "你要强制解除师徒关系吗?\n1.是\n2.否",
                                        "item_single_unmaster"});*/
                            	byte[] bytes = stageService.getTaskBytes((short) 31050,
                                        new String[] {
                                        "你要强制解除师徒关系吗?\n1.是\n2.否",
                                        "item_single_unmaster"});
                                UWAPSegment seg = new UWAPSegment(ClientConstants.
                                        GET_FILE_OK);
                                /*seg.writeShort((short) 31003);*/
                                seg.writeShort((short) 31050);
                                seg.writeShort((short) 2);
                                seg.write(bytes);
                                connectService.writeTo(seg, player.getId());
                            } else {
                                sendMessage(player.getId(), "你没有师徒关系存在");
                            }
                        }
                    } else if (effects[i].getType() == 28) { // 提问
                        needRemove = false;
                        ok = true;
                        InvestigationEffect effect = (InvestigationEffect) effects[i];
                        byte[] bytes = stageService.getTaskBytes((short) effect.getScriptId(), effect.getParameters());
                        UWAPSegment seg = new UWAPSegment(ClientConstants.
                                GET_FILE_OK);
                        seg.writeShort((short) effect.getScriptId());
                        seg.writeShort((short) 2);
                        seg.write(bytes);
                        connectService.writeTo(seg, player.getId());
                    } else if (effects[i].getType() == 29) {
                        Buf[] buf = player.getBufs();
                        if (buf.length == 0) {
                            sendMessage(player.getId(), "不存在任何效果");
                            ok = true;
                            needRemove = false;
                        } else {
                            UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST);
                            seg.writeShort((short) 9);
                            seg.writeString("效果列表");
                            seg.write((byte) 0);
                            seg.writeShort((short) buf.length);
                            for (int j = 0; j < buf.length; j++) {
                                seg.writeInt(i);
                                seg.writeString(Buf.getBufString(buf[j]));
                                seg.writeInt(Utils.CLR_WHITE);
                            }
                            connectService.writeTo(seg, player.getId());
// byte[] bytes = stageService.getTaskBytes((short) 30028);
// seg = new UWAPSegment(ClientConstants.
// GET_FILE_OK);
// seg.writeShort((short) 30028);
// seg.writeShort((short) 2);
// seg.write(bytes);
// connectService.writeTo(seg,player.getId());
                            ok = true;
                            needRemove = true;
                        }

                    } else if (effects[i].getType() == 30) {//宠物召唤符
                        PetEffect effect = (PetEffect) effects[i];
                        Pet pet = new Pet();
                        pet.setId(IDGenerator.getPetId());
                        int petType = effect.getPetType();
                        if (petType < 1 || petType > 6) {	// 随机宠物
                        	pet.setPetType(rnd.nextInt(6) + 1);
                        } else {	// 宠物类型1-6
                        	pet.setPetType(effect.getPetType());
                        }
                        pet.setBaby(effect.isBaby());
                        if (effect.isBaby()){
                        	Utils.initPet(pet, 50, 0 , effect.getLevel());
                        }else{
                        	Utils.initPet(pet, 4 * effect.getLevel(), 0 , effect.getLevel());
                        }
                        pet.setLevel(effect.getLevel());
                        pet.setItemId(101);
                        pet.setCurrentPoint(0);
                        pet.setExp(0);
                        pet.setFavor(50);
// pet.setPoint(20);
                        pet.setHp(pet.getMaxHp());
                        pet.setMp(pet.getMaxMp());
                        Ability[] abs = Utils.getPetAbilities(pet.
                                getPetType());
                        for (int j = 0; j < abs.length; j++) {
                            pet.addAbility(abs[j]);
                        }
                        if (player.addPet(pet, changed) == 1) {
                        	petId = pet.getId();
                            needRemove = true;
                            ok = true;
                        } else {
                            needRemove = false;
                            ok = true;
                            sendMessage(player.getId(), "宠物栏已经满了。");
                        }
                    } else if (effects[i].getType() == 31) { // 答题器
                        QuestionControl control = new QuestionControl(player);
                        control.setTypeID(3);
                        ok = true;
                        needRemove = false;

                        switch (control.getQuestionState()) {
                            case QuestionControl.Question_Begin:
                                connectService.writeTo(QuestionControl.getQuestionBeginSegment(control, player, stageService, 0, 0), player.getId());
                                
                                ok = true;
                                needRemove = true;
                                break;
                            case QuestionControl.Question_Goon:
                                connectService.writeTo(QuestionControl.getQuestionGoonSegment(control, player, stageService, 0, 0), player.getId());
                                ok = true;
                                needRemove = true;

                                break;
                            case QuestionControl.Question_Succeed:
                                connectService.writeTo(QuestionControl.getQuestionSucceedSegment(control, player, stageService, 0, 0), player.getId());

                                break;
                            case QuestionControl.Question_Error:
                                connectService.writeTo(QuestionControl.getQuestionErrorSegment(control, player, stageService, 0, 0), player.getId());

                                break;
                            case QuestionControl.Question_Wait:
                                connectService.writeTo(QuestionControl.getQuestionWaitSegment(control, player, stageService, 0, 0), player.getId());

                                break;
                            default:
                        }
                    } else if (effects[i].getType() == 32) { // 答题清除器
                        QuestionControl control = new QuestionControl(player);

                        switch (control.getQuestionState()) {
                            case QuestionControl.Question_Wait:
                                connectService.writeTo(QuestionControl.getQuestionWaitSegment(control, player, stageService, 0, 0), player.getId());
                                ok = true;
                                needRemove = false;

                                break;
                            case QuestionControl.Question_Begin:
                                sendMessage(player.getId(), "现在可以答题,不需要使用答题机会果!");
                                ok = true;
                                needRemove = false;

                                break;
                            case QuestionControl.Question_Goon:
                                sendMessage(player.getId(), "现在可以答题,不需要使用答题机会果!");
                                ok = true;
                                needRemove = false;

                                break;
                            case QuestionControl.Question_Succeed:
                            case QuestionControl.Question_Error:
                                control.clearQuestionState();
                                sendMessage(player.getId(), "可以答题了!推荐到瓦伊特杂货店商人处购买随身答题果直接答题,或到浴场智慧水晶处免费答题.（愚人节活动在瓦伊特镇的大愚若智处答题）");
                                ok = true;
                                needRemove = true;

                                break;
                            default:
                        }
                    } else if (effects[i].getType() == 33) { // 智多星
                        ok = true;
                        needRemove = true;
                    } else if (effects[i].getType() == 34) { // 指路宝典
                    	String string="记得每天都要打开看看哦，我会告诉你很多幻想中的事情:\n1.领取每日奖励\n2.当前我能做什么\n3.幻想活动手册\n4.幻想精彩玩点介绍" +
                    			"\n5.账号安全\n6.近期游戏公告\n7.游戏小窍门\n8.当前游戏时间\n9.下次再使用";
                    	String[] sendCommandString = new String[12];
                    	sendCommandString[0] = "9";
                    	sendCommandString[1] = "1";
                    	sendCommandString[2] = string;
                    	for(int k= 0;k < 9; k++){
                    		sendCommandString[3+k] = "directway "+(k+1);
                    	}
                    	byte[] bytes = stageService.getTaskBytes((short) 31010, sendCommandString);
                        UWAPSegment seg = new UWAPSegment(ClientConstants.
                                GET_FILE_OK);
                        seg.writeShort((short) 31010);
                        seg.writeShort((short) 2);
                        seg.write(bytes);
                        connectService.writeTo(seg, player.getId());
		                needRemove = false;
		                ok = true;
                        /*if (player.getMap() == null) {
                            sendMessage(player.getId(), "请10秒后再试");
                            ok = true;
                            needRemove = false;
                        } else {
                            Suggest suggest = null;
                            boolean find = false;
                            for (int s = 0; s < Suggest.suggest.size(); s++) {
                                suggest = (Suggest) Suggest.suggest.elementAt(s);
                                for (int m = 0; m < suggest.map.length; m++) {
                                    if (suggest.map[m] == player.getMapId()) {
                                        find = true;
                                        break;
                                    }
                                }
                                if (find)
                                    break;
                            }
                            if (!find) {
                                sendMessage(player.getId(), "本地指南暂无内容,请联系GM!");
                                ok = true;
                                needRemove = false;
                                break;
                            }
                            Object[] o;
                            String su = null;
                            for (int l = 0; l < suggest.level.size(); l++) {
                                o = (Object[]) suggest.level.elementAt(l);
                                if (player.getLevel() >= ((Integer) (o[0])).intValue() &&
                                    player.getLevel() <= ((Integer) (o[1])).intValue()) {
                                    su = (String) o[2];
                                    break;
                                }
                            }
                            if (su == null) {
                                sendMessage(player.getId(), "本地指南暂无内容,请联系GM!");
                                ok = true;
                                needRemove = false;
                                break;
                            }
//                            sendMessage(player.getId(),
//                                        MessageFormat.format(su, new Object[] {player.getLevel(),
//                                    player.getMap().getName()}));

                            String str = MessageFormat.format(su, new Object[] {player.getLevel(),player.getMap().getName()});

                            UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST);
                            seg.writeShort((short) 10001);
                            seg.writeString("指路宝典");
                            seg.write((byte) 2);
                            seg.writeShort((short)1);
                            seg.writeInt(0);
                            seg.writeString(str);
                            seg.writeInt(Utils.CLR_WHITE);
                            connectService.writeTo(seg, player.getId());
                            ok = true;
                            needRemove = false;
                        }*/
                    } else if (effects[i].getType() == 35) { // 房屋push，多人
                        byte[] bytes = stageService.getTaskBytes((short) 31002,
                                new String[] {"你将向此地区所有玩家发出参观家园的邀请,你的家园将自动设置为自由参观,是否继续?\n1.继续\n2.取消",
                                "housepush"});
                        UWAPSegment seg = new UWAPSegment(ClientConstants.
                                GET_FILE_OK);
                        seg.writeShort((short) 31002);
                        seg.writeShort((short) 2);
                        seg.write(bytes);
                        connectService.writeTo(seg, player.getId());
                        needRemove = false;
                        ok = true;
                    } else if (effects[i].getType() == 36) {
                        byte[] bytes = stageService.getTaskBytes((short) 31001,
                                new String[] {"请输入对方的名字\n1.是\n2.否",
                                "名字:", "privatehousepush "});
                        UWAPSegment seg = new UWAPSegment(ClientConstants.
                                GET_FILE_OK);
                        seg.writeShort((short) 31001);
                        seg.writeShort((short) 2);
                        seg.write(bytes);
                        connectService.writeTo(seg, player.getId());
                        needRemove = false;
                        ok = true;
                    } else if (effects[i].getType() == 37) {
                        HouseData hd;
						try {
							hd = houseModel.getHouseByPlayerId(player.getId());
							if(hd==null){
	                            needRemove = false;
	                            ok = true;
	                            sendMessage(player.getId(),"你没有家园!");
	                        }
	                        try {
	                        	boolean canTransfer = true;
                            	GameMap playerMap = player.getMap();
                            	if(playerMap != null){
                    				NoDoor door = NoDoor.getNoTransfer(playerMap.getMapId());
                    				if(door != null){
                    					sendMessage(player.getId(), door.getMessage());
                    					canTransfer = false;
                    				}
                    			}
                            	if(canTransfer){
		                            HouseInstance instance = houseModel.preTry(player, player.getId());
		                            houseModel.UsedItem(player);
		                            HouseInstance hi = houseModel.tryGotoInstance(instance.getId(), player, -1);
		                            if (hi != null) {
		                                InstanceDefinition idf = instance.getDefinition();
		                                sendGotoMap(player.getId(), idf.getMap(), idf.getX(),
		                                            idf.getY());
	
		                            }
		                            needRemove = true;
		                            ok = true;
                            	}else{
                            		needRemove = false;
                            		ok = true;
                            	}
	                        } catch (InstanceException ex2) {
	                        	needRemove = false;
	                        	ok = false;
	                        } catch (HouseException ex2) {
	                        	needRemove = false;
	                        	ok = false;
	                        }
						} catch (Exception e) {
							sendMessage(player.getId(),"你没有家园!");
							log.info("ID[" + player.getId() + "][回家卷轴使用失败]"+e);
							needRemove = false;
                        	ok = false;
						}
                    } else if (effects[i].getType() == 38) {
                        EnemyEffect effect = (EnemyEffect) effects[i];
                        int level = effect.getLevel();
                        /*if(level==0){//记录最新20名的仇人玩家角色名（被杀次数）
                            Enemy[] enemys = player.getEnemys();
                            Arrays.sort(enemys);
                            UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST);
                            seg.writeShort((short) 9);
                            seg.writeString("仇人榜");
                            seg.write((byte) 0);
                            seg.writeShort((short) enemys.length);
                            for (int j = 0; j < enemys.length; j++) {
                                seg.writeInt(j);
                                seg.writeString(enemys[j].name + "[仇恨值:" + enemys[j].times+"]");
                                seg.writeInt(Utils.CLR_WHITE);
                            }
                            connectService.writeTo(seg,player.getId());
                            needRemove = true;
                            ok = true;
                        }
                        else if(level==1){//记录最新20名的仇人玩家角色名（被杀次数）和离在线状态
                            Enemy[] enemys = player.getEnemys();
                           Arrays.sort(enemys);
                           UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST);
                           seg.writeShort((short) 9);
                           seg.writeString("仇人榜");
                           seg.write((byte) 0);
                           seg.writeShort((short) enemys.length);
                           for (int j = 0; j < enemys.length; j++) {
                               WorldPlayer p = playerService.getWorldPlayer(enemys[j].id);
                               String onlineString = "";
                               if(p!=null&&p.online()){
                                   onlineString = "[在线]";
                               }else{
                                   onlineString = "[离线]";
                               }
                               seg.writeInt(j);
                               seg.writeString(enemys[j].name + "[仇恨值:" + enemys[j].times+"]"+onlineString);
                               seg.writeInt(Utils.CLR_WHITE);
                           }
                           connectService.writeTo(seg,player.getId());
                           needRemove = true;
                           ok = true;
                        }
                        else */
                        //低级，中级，高级仇人录都升级为高级仇人录
                        if(level==2 || level ==1 || level ==0){//记录最新20名的仇人玩家角色名（被杀次数）。在线的显示目前所在场景名称，不在线的显示离线状态
                           Enemy[] enemys = player.getEnemys();
                           Arrays.sort(enemys);
                           UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST);
                           seg.writeShort((short) 10245);
                           seg.writeString("仇人榜");
                           seg.write((byte) 3);
                           seg.writeShort((short)(enemys.length < 20?enemys.length:20));
                           for (int j = 0; j < enemys.length && j<20; j++) {
                               WorldPlayer p = playerService.getWorldPlayer(enemys[j].id);
                               String onlineString = "";
                               if(p!=null&&p.online()){
                                   GameMap m = p.getMap();
                                   if(m!=null)
                                       onlineString = "["+m.getName()+"]";
                               }else{
                                   onlineString = "[离线]";
                               }
                               seg.writeInt(enemys[j].id);
                               seg.writeString(enemys[j].name + "[仇恨值:" + enemys[j].times+"]"+onlineString);
                               seg.writeInt(Utils.CLR_WHITE);
                           }
                           seg.write((byte) 2);
                           seg.writeString("追踪仇人");
                           seg.writeString("fllowenemys " + item.getItemId());
                           seg.writeString("删除仇人");
                           seg.writeString("deleteenemys");
                           connectService.writeTo(seg,player.getId());
                           needRemove = false;
                           ok = true;
                        }
                    } else if(effects[i].getType()==39){ //使用钥匙
                        KeyEffect effect = (KeyEffect)effects[i];
                        if(effect.getBoxId() != -1 && !player.hasItem(effect.getBoxId(),1)){
                            needRemove = false;
                            ok = true;
                            sendMessage(player.getId(),effect.getMsg());
                        }else{
                        	if (mailitem) {
                        		if (effect.getBoxId() == -1 || player.completeRemoveItem(effect.getBoxId(), 1, changed) != null) {
                                    DropGroup group = null;
                                    if (player.getBoxCount()%11 == 1) {//1个垃圾组；10个精品组
                                    	group = DropGroups.getDropGroup(effect.getGroup1(),player.getLevel());
                                    } else {
                                    	group = DropGroups.getDropGroup(effect.getGroup2(),player.getLevel());
                                    }
                                  //jwp add 宝箱掉落5个垃圾，一个精品
//                                    if(player.getBoxCount() < 5 || (player.getBoxCount()/5)%3 == 0){
//                                    //if(player.getBoxCount()<10||((player.getBoxCount()/10)%2)==0){
//                                        group = DropGroups.getDropGroup(effect.getGroup1(),player.getLevel());
//                                    }else{
//                                        group = DropGroups.getDropGroup(effect.getGroup2(),player.getLevel());
//                                    }
                                    if(group != null){
	                                    int rate = rnd.nextInt(group.getRate());
	                                    DropItem dropItem = group.calcDropItem(
	                                            rate);
	                                    int count = getCount(rnd,
	                                            dropItem.getMin(),
	                                            dropItem.getMax());
	                                    IItem di = dropItem.getItem().newInstance();
	                                    if(player.completeAddItem(di,count,changed, player.getClientDataVersion())==null){
	                                        byte[] att = ItemUtils.item2dbAttachment(di,
	                                                count);
	                                        mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
	                                                            di.getName(), "", att, 0, true);
	                                        sendMessage(player.getId(),"你的背包满了，已经把物品邮寄到你的邮箱!");
	                                    }
	                                    log.info("ID[" + player.getId() + "]OpenBox[" + di.getItemId() +
	                                            "]Count[" + count + "]OK");
	                                    player.addBoxCount(1);
	                                    //mengjie add
	                                    int item_id = 0;
	                                    item_id = di.getItemId();
	                                    String item_msg = "";
	                                    if(effect.getBoxId() == -1 || effect.getBoxId() == 200223){
	                                    	if (item.getItemId() == 200828) {
	                                    		item_msg = Items.getMessage(item_id,1,player.getPlayerName(),di.getName(), item.getName());
	                                    	} else {
	                                    		item_msg = Items.getMessage(item_id,1,player.getPlayerName(),di.getName(),"宝箱钥匙");
	                                    	}
	                                    }else{
	                                    	item_msg = Items.getMessage(item_id,1,player.getPlayerName(),di.getName(),"圣诞千层糕");
	                                    }
	                                    if (item_msg != null){
	                                    	chatService.sendWorldMessage(-1, "系统", item_msg);
	                                    }
                                    }
                                    //额外给经验
                                    if (player.getLevel()<100){
                                    	if (Utils.hit(rnd,5,100)){
                                    		//精华蛋
                                    		IItemTemplate itemtemplate = Items.getTemplate(550034);
                                    		if (itemtemplate != null) {
                        	                    IItem item_tmp = player.completeAddItem(itemtemplate.
                        	                            newInstance(), 1, changed, player.getClientDataVersion());
                        	                    if (item_tmp == null) {

                        	                    	connectService.sendMessage(player.getId(),
                        	                    			"由于背包满，您的宝箱奖励品已经邮寄到邮箱中，请注意查收。");
                        	                    	byte[] att = ItemUtils.item2dbAttachment(itemtemplate.newInstance(),
                        	                        		1);
                        	                        mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
                        	                        		itemtemplate.getName() + "*" + 1, "", att, 0, true);

                        	                    }
                                    		}
//                                    		player.addExp(exp*3, changed);
                                    		chatService.sendPrivateMessage(-1,"系统",player.getId(),
        									"恭喜你获得"+itemtemplate.getName()+"，活动期间有一定机率获得额外的奖励哦");
                                    	}else if (Utils.hit(rnd,10,95)){
                                        	//获得经验
                                        	int exp = (BathHouse.EXP[player.getLevel()] * 360) / 100;
                                        	if(Utils.hit(rnd,10,100)){
                                        		//高级宝箱经验果
                                        		IItemTemplate itemtemplate = Items.getTemplate(550047);
                                        		if (itemtemplate != null) {
                            	                    IItem item_tmp = player.completeAddItem(itemtemplate.
                            	                            newInstance(), 1, changed, player.getClientDataVersion());
                            	                    if (item_tmp == null) {

                            	                    	connectService.sendMessage(player.getId(),
                            	                    			"由于背包满，您的宝箱奖励品已经邮寄到邮箱中，请注意查收。");
                            	                    	byte[] att = ItemUtils.item2dbAttachment(itemtemplate.newInstance(),
                            	                        		1);
                            	                        mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
                            	                        		itemtemplate.getName() + "*" + 1, "", att, 0, true);

                            	                    }
                                        		}
                                        		chatService.sendPrivateMessage(-1,"系统",player.getId(),
            									"恭喜你获得"+itemtemplate.getName()+"，活动期间有一定机率获得额外的经验奖励哦");
                                        	}else if (Utils.hit(rnd,30,90)){
                                        		IItemTemplate itemtemplate = Items.getTemplate(550046);
                                        		if (itemtemplate != null) {
                            	                    IItem item_tmp = player.completeAddItem(itemtemplate.
                            	                            newInstance(), 1, changed, player.getClientDataVersion());
                            	                    if (item_tmp == null) {

                            	                    	connectService.sendMessage(player.getId(),
                            	                    			"由于背包满，您的宝箱奖励品已经邮寄到邮箱中，请注意查收。");
                            	                    	byte[] att = ItemUtils.item2dbAttachment(itemtemplate.newInstance(),
                            	                        		1);
                            	                        mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
                            	                        		itemtemplate.getName() + "*" + 1, "", att, 0, true);

                            	                    }
                                        		}
//                                        		player.addExp(exp*2, changed);
                                        		chatService.sendPrivateMessage(-1,"系统",player.getId(),
                                        		"恭喜你获得"+itemtemplate.getName()+"，活动期间有一定机率获得额外的经验奖励哦");
                                        	}else{
                                        		IItemTemplate itemtemplate = Items.getTemplate(550045);
                                        		if (itemtemplate != null) {
                            	                    IItem item_tmp = player.completeAddItem(itemtemplate.
                            	                            newInstance(), 1, changed, player.getClientDataVersion());
                            	                    if (item_tmp == null) {

                            	                    	connectService.sendMessage(player.getId(),
                            	                    			"由于背包满，您的宝箱奖励品已经邮寄到邮箱中，请注意查收。");
                            	                    	byte[] att = ItemUtils.item2dbAttachment(itemtemplate.newInstance(),
                            	                        		1);
                            	                        mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
                            	                        		itemtemplate.getName() + "*" + 1, "", att, 0, true);

                            	                    }
                                        		}
//                                        		player.addExp(exp, changed);
                                        		chatService.sendPrivateMessage(-1,"系统",player.getId(),
            									"恭喜你获得"+itemtemplate.getName()+"，活动期间有一定机率获得额外的经验奖励哦");
                                        	}
                                        }
                                    }else{
                                    	if (Utils.hit(rnd,5,100)){
                                    		//精华蛋
                                    		IItemTemplate itemtemplate = Items.getTemplate(550034);
                                    		if (itemtemplate != null) {
                        	                    IItem item_tmp = player.completeAddItem(itemtemplate.
                        	                            newInstance(), 1, changed, player.getClientDataVersion());
                        	                    if (item_tmp == null) {

                        	                    	connectService.sendMessage(player.getId(),
                        	                    			"由于背包满，您的宝箱奖励品已经邮寄到邮箱中，请注意查收。");
                        	                    	byte[] att = ItemUtils.item2dbAttachment(itemtemplate.newInstance(),
                        	                        		1);
                        	                        mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
                        	                        		itemtemplate.getName() + "*" + 1, "", att, 0, true);

                        	                    }
                                    		}
//                                    		player.addExp(exp*3, changed);
                                    		chatService.sendPrivateMessage(-1,"系统",player.getId(),
        									"恭喜你获得"+itemtemplate.getName()+"，活动期间有一定机率获得额外的奖励哦");
                                    	}else if (Utils.hit(rnd,5,95)){
                                			player.addCredit(12,changed);
                                			chatService.sendPrivateMessage(-1,"系统",player.getId(),
                									"恭喜你获得12点荣誉，活动期间有一定机率获得额外的荣誉奖励哦");
                                		}
                                	}
                                    //mengjie add end
                                    needRemove = true;
                                    ok = true;
                                }else{
                                    needRemove = false;
                                    ok = true;
                                    sendMessage(player.getId(),effect.getMsg());
                                }
                        	}else{
                        		if (effect.getBoxId() == -1 || player.completeRemoveItem(effect.getBoxId(), 1, changed) != null) {
                                    DropGroup group = null;
                                    int mail_tmp = 0;
                                    if (player.getBoxCount()%11 == 1) {//1个垃圾组；10个精品组
                                    	group = DropGroups.getDropGroup(effect.getGroup1(),player.getLevel());
                                    }else{
                                    	group = DropGroups.getDropGroup(effect.getGroup2(),player.getLevel());
                                    }
//                                    if(player.getBoxCount() < 5 || (player.getBoxCount()/5)%3 == 0){
//                                        //if(player.getBoxCount()<10||((player.getBoxCount()/10)%2)==0){
//                                        group = DropGroups.getDropGroup(effect.getGroup1(),player.getLevel());
//                                    }else{
//                                        group = DropGroups.getDropGroup(effect.getGroup2(),player.getLevel());
//                                    }
                                    if(group != null){
	                                    int rate = rnd.nextInt(group.getRate());
	                                    DropItem dropItem = group.calcDropItem(
	                                            rate);
	                                    int count = getCount(rnd,
	                                            dropItem.getMin(),
	                                            dropItem.getMax());
	                                    IItem di = dropItem.getItem().newInstance();
	                                    if(player.completeAddItem(di,count,changed, player.getClientDataVersion())==null){
	                                        byte[] att = ItemUtils.item2dbAttachment(di,
	                                                count);
	                                        mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
	                                                            di.getName(), "", att, 0, true);
	                                        sendMessage(player.getId(),"你的背包满了，已经把物品邮寄到你的邮箱!");
	                                        mail_tmp =1;
	                                    }
	                                    player.addBoxCount(1);
	                                    //mengjie add
	                                    int item_id = 0;
	                                    item_id = di.getItemId();
	                                    String item_msg = "";
	                                    if (effect.getBoxId() == -1 || effect.getBoxId() == 200223) {
	                                    	if (item.getItemId() == 200828) {
	                                    		item_msg = Items.getMessage(item_id,1,player.getPlayerName(),di.getName(), item.getName());
	                                    	} else {
	                                    		item_msg = Items.getMessage(item_id,1,player.getPlayerName(),di.getName(),"宝箱钥匙");
	                                    	}
	                                    } else {
	                                    	item_msg = Items.getMessage(item_id,1,player.getPlayerName(),di.getName(),"圣诞千层糕");
	                                    }
	                                    if (item_msg != null) {
	                                    	chatService.sendWorldMessage(-1, "系统", item_msg);
	                                    }
                                    }
                                  //额外给经验
                                    if (player.getLevel()<100){
                                    	if (Utils.hit(rnd,5,100)){
                                    		//精华蛋
                                    		IItemTemplate itemtemplate = Items.getTemplate(550034);
                                    		if (itemtemplate != null) {
                        	                    IItem item_tmp = player.completeAddItem(itemtemplate.
                        	                            newInstance(), 1, changed, player.getClientDataVersion());
                        	                    if (item_tmp == null) {

                        	                    	connectService.sendMessage(player.getId(),
                        	                    			"由于背包满，您的宝箱奖励品已经邮寄到邮箱中，请注意查收。");
                        	                    	byte[] att = ItemUtils.item2dbAttachment(itemtemplate.newInstance(),
                        	                        		1);
                        	                        mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
                        	                        		itemtemplate.getName() + "*" + 1, "", att, 0, true);

                        	                    }
                                    		}
//                                    		player.addExp(exp*3, changed);
                                    		chatService.sendPrivateMessage(-1,"系统",player.getId(),
        									"恭喜你获得"+itemtemplate.getName()+"，活动期间有一定机率获得额外的奖励哦");
                                    	}else if (Utils.hit(rnd,10,95)){
                                        	//获得经验
                                        	int exp = (BathHouse.EXP[player.getLevel()] * 360) / 100;
                                        	if(Utils.hit(rnd,10,100)){
                                        		IItemTemplate itemtemplate = Items.getTemplate(550047);
                                        		if (itemtemplate != null) {
                            	                    IItem item_tmp = player.completeAddItem(itemtemplate.
                            	                            newInstance(), 1, changed, player.getClientDataVersion());
                            	                    if (item_tmp == null) {

                            	                    	connectService.sendMessage(player.getId(),
                            	                    			"由于背包满，您的宝箱奖励品已经邮寄到邮箱中，请注意查收。");
                            	                    	byte[] att = ItemUtils.item2dbAttachment(itemtemplate.newInstance(),
                            	                        		1);
                            	                        mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
                            	                        		itemtemplate.getName() + "*" + 1, "", att, 0, true);

                            	                    }
                                        		}
//                                        		player.addExp(exp*3, changed);
                                        		chatService.sendPrivateMessage(-1,"系统",player.getId(),
            									"恭喜你获得"+itemtemplate.getName()+"，活动期间有一定机率获得额外的经验奖励哦");
                                        	}else if (Utils.hit(rnd,30,90)){
                                        		IItemTemplate itemtemplate = Items.getTemplate(550046);
                                        		if (itemtemplate != null) {
                            	                    IItem item_tmp = player.completeAddItem(itemtemplate.
                            	                            newInstance(), 1, changed, player.getClientDataVersion());
                            	                    if (item_tmp == null) {

                            	                    	connectService.sendMessage(player.getId(),
                            	                    			"由于背包满，您的宝箱奖励品已经邮寄到邮箱中，请注意查收。");
                            	                    	byte[] att = ItemUtils.item2dbAttachment(itemtemplate.newInstance(),
                            	                        		1);
                            	                        mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
                            	                        		itemtemplate.getName() + "*" + 1, "", att, 0, true);

                            	                    }
                                        		}
//                                        		player.addExp(exp*2, changed);
                                        		chatService.sendPrivateMessage(-1,"系统",player.getId(),
            									"恭喜你获得"+itemtemplate.getName()+"，活动期间有一定机率获得额外的经验奖励哦");
                                        	}else{
                                        		IItemTemplate itemtemplate = Items.getTemplate(550045);
                                        		if (itemtemplate != null) {
                            	                    IItem item_tmp = player.completeAddItem(itemtemplate.
                            	                            newInstance(), 1, changed, player.getClientDataVersion());
                            	                    if (item_tmp == null) {

                            	                    	connectService.sendMessage(player.getId(),
                            	                    			"由于背包满，您的宝箱奖励品已经邮寄到邮箱中，请注意查收。");
                            	                    	byte[] att = ItemUtils.item2dbAttachment(itemtemplate.newInstance(),
                            	                        		1);
                            	                        mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
                            	                        		itemtemplate.getName() + "*" + 1, "", att, 0, true);

                            	                    }
                                        		}
//                                        		player.addExp(exp, changed);
                                        		chatService.sendPrivateMessage(-1,"系统",player.getId(),
            									"恭喜你获得"+itemtemplate.getName()+"，活动期间有一定机率获得额外的经验奖励哦");
                                        	}
                                        }
                                    }else{
                                    	if (Utils.hit(rnd,5,100)){
                                    		//精华蛋
                                    		IItemTemplate itemtemplate = Items.getTemplate(550034);
                                    		if (itemtemplate != null) {
                        	                    IItem item_tmp = player.completeAddItem(itemtemplate.
                        	                            newInstance(), 1, changed, player.getClientDataVersion());
                        	                    if (item_tmp == null) {

                        	                    	connectService.sendMessage(player.getId(),
                        	                    			"由于背包满，您的宝箱奖励品已经邮寄到邮箱中，请注意查收。");
                        	                    	byte[] att = ItemUtils.item2dbAttachment(itemtemplate.newInstance(),
                        	                        		1);
                        	                        mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
                        	                        		itemtemplate.getName() + "*" + 1, "", att, 0, true);

                        	                    }
                                    		}
//                                    		player.addExp(exp*3, changed);
                                    		chatService.sendPrivateMessage(-1,"系统",player.getId(),
        									"恭喜你获得"+itemtemplate.getName()+"，活动期间有一定机率获得额外的奖励哦");
                                    	}else if (Utils.hit(rnd,5,95)){
                                			player.addCredit(12,changed);
                                			chatService.sendPrivateMessage(-1,"系统",player.getId(),
                									"恭喜你获得12点荣誉，活动期间有一定机率获得额外的荣誉奖励哦");
                                		}
                                	}
                                    //mengjie add end
                                    needRemove = true;
                                    if (mail_tmp == 1){
                                    	ok = false;
                                    }else{
                                    	ok = true;
                                    }
                                    
                                }else{
                                    needRemove = false;
                                    ok = false;
                                    sendMessage(player.getId(),effect.getMsg());
                                }
                        	}
                            
                        }
                    } else if(effects[i].getType()==40){
                        ResetSinglePropertyEffect effect = (ResetSinglePropertyEffect)effects[i];
                        if(effect.getProperty()==1){  //strength
                            player.resetStrength(changed);
                            needRemove = true;
                            ok = true;
                        }
                        else if(effect.getProperty()==2){ //agility
                            player.resetAgility(changed);
                            needRemove = true;
                            ok = true;
                        }
                        else if(effect.getProperty()==3){ //vitality
                            player.resetVitality(changed);
                            needRemove = true;
                            ok = true;
                        }
                        else if(effect.getProperty()==4){  //intelligence
                            player.resetIntelligence(changed);
                            needRemove = true;
                            ok = true;
                        }
                    } else if(effects[i].getType()==41){ //addexp
                        AddExpEffect effect = (AddExpEffect)effects[i];
                        
                        if (effect.getCount()< 0 ){ // 降级，count=等级
                        	int point = player.getPoint();
                        	int downLevel = (int) (player.getLevel() - effect.getCount());
                            int pointTemp = (int) (effect.getCount() / 2);
                            if ((player.getLevel() % 2 == 0) && downLevel % 2 != 0)
                            	pointTemp++;
                            if (point < pointTemp){
                            	needRemove = false;
                                ok = true;
                            	sendMessage(player.getId(),"您没有足够的战斗技能点，无法降一级!请先遗忘战斗技能。");
                        	}else if (player.getLeavePoints() <= 0){
                        		needRemove = false;
                                ok = true;
                        		sendMessage(player.getId(),"您没有足够的可分配属性点，无法降一级!可以用复生神光来洗点哦。");
                        	}else{
                        		needRemove = true;
                                ok = true;
	                        	int level = (int) (0 - effect.getCount());
	                        	player.downGrade(level, changed);
                        	}
                        }else{
                        	needRemove = true;
                            ok = true;
                            //modify 玩家100级使用经验包继续有效
                        	if (player.getMaxLevel() > player.getLevel()) {
                            	int level_tmp = player.getLevel();
                                int exp = (int)(BathHouse.EXP[player.getLevel()] * effect.getCount());
                                player.addExp(exp, changed);
                                if(level_tmp<player.getLevel()){
                                    //推荐人通用函数
                                	playerService.recommendBalance(player, "use addexp");
                                	//尝试加到师傅的列表中
                                	playerService.addMasterPlayer(player, changed);
                                }
                            }else{
                            	int level_tmp = player.getLevel();
                                int exp = (int)(BathHouse.EXP[player.getMaxLevel() - 1] * effect.getCount());
                                player.addExp(exp, changed);
                                if(level_tmp<player.getLevel()){
                                    //推荐人通用函数
                                	playerService.recommendBalance(player, "use addexp");
                                	//尝试加到师傅的列表中
                                	playerService.addMasterPlayer(player, changed);
                                }
                            }
                        }
                        
                    } else if(effects[i].getType()==42){ //addcredit
                        AddCreditEffect effect = (AddCreditEffect)effects[i];
                        needRemove = true;
                        ok = true;
                        int value = Utils.getCount(rnd,effect.getMin(),effect.getMax());
                        player.addCredit(value,changed);
                    //mengjie add
                    }else if(effects[i].getType()==43){ //钱袋功能
                    	//加j币
                    	MoneypackageEffect effect = (MoneypackageEffect)effects[i];
                    	int moneyadd = 0;
                    	if (effect.getMoney()>0){
                    		moneyadd = effect.getMoney();
                    	}else{
                    		int moneyint = 0;
                            moneyint = Utils.getMoney_package(player.getLevel(), rnd);
                            moneyadd = moneyint * effect.getPercent()/ 100;
                    	}
                    	player.setMoeny(player.getMoeny() + moneyadd);
                        changed.addProperty(Changed.MONEY, moneyadd);
                    	needRemove = true;
                        ok = true;
                    }else if(effects[i].getType()==44){ //钥匙链功能
                    	KeysEffect effect = (KeysEffect)effects[i];
                        if(effect.getBoxId() != -1 && !player.hasItem(effect.getBoxId(),effect.getCount())){
                        	if (mailitem){
                        		needRemove = false;
                                ok = true;
                                sendMessage(player.getId(),"你没有" + effect.getCount() + "个宝箱!");
                        	}else{
                        		needRemove = false;
                                ok = false;
                                sendMessage(player.getId(),"你没有" + effect.getCount() + "个宝箱!");
                        	}
                            
                        }else{
                        	int sum = 0;
                        	if (mailitem){
                        		if(effect.getBoxId() == -1 || player.completeRemoveItem(effect.getBoxId(),effect.getCount(),changed)!=null){
                        			for(int k = 0;k<effect.getCount();k++){
                                		DropGroup group = null;
                                		if (player.getBoxCount()%11 == 1){//1个垃圾组；10个精品组
                                        	group = DropGroups.getDropGroup(effect.getGroup1(),player.getLevel());
                                        }else{
                                        	group = DropGroups.getDropGroup(effect.getGroup2(),player.getLevel());
                                        }
//                                		if(player.getBoxCount() < 5 || (player.getBoxCount()/5)%3 == 0){
//                                              //if(player.getBoxCount()<10||((player.getBoxCount()/10)%2)==0){
//                                            group = DropGroups.getDropGroup(effect.getGroup1(),player.getLevel());
//                                        }else{
//                                            group = DropGroups.getDropGroup(effect.getGroup2(),player.getLevel());
//                                        }
                                		if(group != null){
	                                        int rate = rnd.nextInt(group.getRate());
	                                        DropItem dropItem = group.calcDropItem(
	                                                rate);
	                                        int count = getCount(rnd,
	                                                dropItem.getMin(),
	                                                dropItem.getMax());
	                                        IItem di = dropItem.getItem().newInstance();
	                                        if(player.completeAddItem(di,count,changed, player.getClientDataVersion())==null){
	                                        	byte[] att = ItemUtils.item2dbAttachment(di,
	                                                    count);
	                                            mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
	                                                                di.getName(), "", att, 0, true);
	                                            if (sum == 0) {
	                                            	sendMessage(player.getId(),"你的背包满了，已经把物品邮寄到你的邮箱!");
	                                            	sum ++;
	                                            }
                                            	
	                                        }
	                                        log.info("ID[" + player.getId() + "]OpenBox[" + di.getItemId() +
	                                                "]Count[" + count + "]OK");
	                                        player.addBoxCount(1);
	                                        int item_id = 0;
	                                        item_id = di.getItemId();
	                                        String item_msg = Items.getMessage(item_id,1,player.getPlayerName(),di.getName(),"钥匙串");
	                                        if (item_msg != null){
	                                        	chatService.sendWorldMessage(-1, "系统", item_msg);
	                                        }
                                		}
                                      //额外给经验
                                    	if (player.getLevel()<100){
                                    		if (Utils.hit(rnd,5,100)){
                                        		//精华蛋
                                        		IItemTemplate itemtemplate = Items.getTemplate(550034);
                                        		if (itemtemplate != null) {
                            	                    IItem item_tmp = player.completeAddItem(itemtemplate.
                            	                            newInstance(), 1, changed, player.getClientDataVersion());
                            	                    if (item_tmp == null) {
                            	                    	if (sum == 0) {
                            	                    		connectService.sendMessage(player.getId(),
                            	                    		"由于背包满，您的宝箱奖励品已经邮寄到邮箱中，请注意查收。");
                            	                    		sum++;
                            	                    	}
                            	                    	byte[] att = ItemUtils.item2dbAttachment(itemtemplate.newInstance(),
                            	                        		1);
                            	                        mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
                            	                        		itemtemplate.getName() + "*" + 1, "", att, 0, true);

                            	                    }
                                        		}
//                                        		player.addExp(exp*3, changed);
                                        		chatService.sendPrivateMessage(-1,"系统",player.getId(),
            									"恭喜你获得"+itemtemplate.getName()+"，活动期间有一定机率获得额外的奖励哦");
                                        	}else if (Utils.hit(rnd,10,95)){
                                            	//获得经验
                                            	int exp = (BathHouse.EXP[player.getLevel()] * 360) / 100;
                                            	if(Utils.hit(rnd,10,100)){
                                            		IItemTemplate itemtemplate = Items.getTemplate(550047);
                                            		if (itemtemplate != null) {
                                	                    IItem item_tmp = player.completeAddItem(itemtemplate.
                                	                            newInstance(), 1, changed, player.getClientDataVersion());
                                	                    if (item_tmp == null) {
                                	                    	if (sum == 0) {
                                	                    		connectService.sendMessage(player.getId(),
                                	                    		"由于背包满，您的宝箱奖励品已经邮寄到邮箱中，请注意查收。");
                                	                    		sum ++;
                                	                    	}
                                	                    	byte[] att = ItemUtils.item2dbAttachment(itemtemplate.newInstance(),
                                	                        		1);
                                	                        mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
                                	                        		itemtemplate.getName() + "*" + 1, "", att, 0, true);

                                	                    }
                                            		}
//                                            		player.addExp(exp*3, changed);
                                            		chatService.sendPrivateMessage(-1,"系统",player.getId(),
                									"恭喜你获得"+itemtemplate.getName()+"，活动期间有一定机率获得额外的经验奖励哦");
                                            	}else if (Utils.hit(rnd,30,90)){
                                            		IItemTemplate itemtemplate = Items.getTemplate(550046);
                                            		if (itemtemplate != null) {
                                	                    IItem item_tmp = player.completeAddItem(itemtemplate.
                                	                            newInstance(), 1, changed, player.getClientDataVersion());
                                	                    if (item_tmp == null) {
                                	                    	if (sum == 0) {
                                	                    		connectService.sendMessage(player.getId(),
                                	                    		"由于背包满，您的宝箱奖励品已经邮寄到邮箱中，请注意查收。");
                                	                    		sum ++;
                                	                    	}
                                	                    	byte[] att = ItemUtils.item2dbAttachment(itemtemplate.newInstance(),
                                	                        		1);
                                	                        mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
                                	                        		itemtemplate.getName() + "*" + 1, "", att, 0, true);

                                	                    }
                                            		}
//                                            		player.addExp(exp*2, changed);
                                            		chatService.sendPrivateMessage(-1,"系统",player.getId(),
                									"恭喜你获得"+itemtemplate.getName()+"，活动期间有一定机率获得额外的经验奖励哦");
                                            	}else{
                                            		IItemTemplate itemtemplate = Items.getTemplate(550045);
                                            		if (itemtemplate != null) {
                                	                    IItem item_tmp = player.completeAddItem(itemtemplate.
                                	                            newInstance(), 1, changed, player.getClientDataVersion());
                                	                    if (item_tmp == null) {
                                	                    	if (sum == 0) {
                                	                    		connectService.sendMessage(player.getId(),
                                	                    		"由于背包满，您的宝箱奖励品已经邮寄到邮箱中，请注意查收。");
                                	                    		sum ++;
                                	                    	}
                                	                    	byte[] att = ItemUtils.item2dbAttachment(itemtemplate.newInstance(),
                                	                        		1);
                                	                        mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
                                	                        		itemtemplate.getName() + "*" + 1, "", att, 0, true);

                                	                    }
                                            		}
//                                            		player.addExp(exp, changed);
                                            		chatService.sendPrivateMessage(-1,"系统",player.getId(),
                									"恭喜你获得"+itemtemplate.getName()+"，活动期间有一定机率获得额外的经验奖励哦");
                                            	}
                                            }
                                    	}else{
                                    		if (Utils.hit(rnd,5,100)){
                                        		//精华蛋
                                        		IItemTemplate itemtemplate = Items.getTemplate(550034);
                                        		if (itemtemplate != null) {
                            	                    IItem item_tmp = player.completeAddItem(itemtemplate.
                            	                            newInstance(), 1, changed, player.getClientDataVersion());
                            	                    if (item_tmp == null) {
                            	                    	if (sum == 0) {
                            	                    		connectService.sendMessage(player.getId(),
                            	                    		"由于背包满，您的宝箱奖励品已经邮寄到邮箱中，请注意查收。");
                            	                    		sum ++;
                            	                    	}
                            	                    	byte[] att = ItemUtils.item2dbAttachment(itemtemplate.newInstance(),
                            	                        		1);
                            	                        mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
                            	                        		itemtemplate.getName() + "*" + 1, "", att, 0, true);

                            	                    }
                                        		}
//                                        		player.addExp(exp*3, changed);
                                        		chatService.sendPrivateMessage(-1,"系统",player.getId(),
            									"恭喜你获得"+itemtemplate.getName()+"，活动期间有一定机率获得额外的奖励哦");
                                        	}else if (Utils.hit(rnd,5,95)){
                                    			player.addCredit(12,changed);
                                    			chatService.sendPrivateMessage(-1,"系统",player.getId(),
            										"恭喜你获得12点荣誉，活动期间有一定机率获得额外的荣誉奖励哦");
                                    		}
                                    	}
                                	}
                                    needRemove = true;
                                    ok = true;
                                }else{
                                    needRemove = false;
                                    ok = true;
                                    sendMessage(player.getId(),"你没有" + effect.getCount() + "个宝箱!");
                                }
                        	}else{
                        		if(effect.getBoxId() == -1 || player.completeRemoveItem(effect.getBoxId(),effect.getCount(),changed)!=null){
                        			int mail_tmp = 0;
                                	for(int k = 0;k<effect.getCount();k++){
                                		DropGroup group = null;
                                		if (player.getBoxCount()%11 == 1){//1个垃圾组；10个精品组
                                        	group = DropGroups.getDropGroup(effect.getGroup1(),player.getLevel());
                                        }else{
                                        	group = DropGroups.getDropGroup(effect.getGroup2(),player.getLevel());
                                        }
//                                		if(player.getBoxCount() < 5 || (player.getBoxCount()/5)%3 == 0){
//                                              //if(player.getBoxCount()<10||((player.getBoxCount()/10)%2)==0){
//                                            group = DropGroups.getDropGroup(effect.getGroup1(),player.getLevel());
//                                        }else{
//                                            group = DropGroups.getDropGroup(effect.getGroup2(),player.getLevel());
//                                        }
                                		if(group != null){
	                                        int rate = rnd.nextInt(group.getRate());
	                                        DropItem dropItem = group.calcDropItem(
	                                                rate);
	                                        int count = getCount(rnd,
	                                                dropItem.getMin(),
	                                                dropItem.getMax());
	                                        IItem di = dropItem.getItem().newInstance();
	                                        if(player.completeAddItem(di,count,changed, player.getClientDataVersion())==null){
	                                            byte[] att = ItemUtils.item2dbAttachment(di,
	                                                    count);
	                                            mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
	                                                                di.getName(), "", att, 0, true);
	                                            if (sum == 0) {
	                                            	sendMessage(player.getId(),"你的背包满了，已经把物品邮寄到你的邮箱!");
	                                            	sum ++;
	                                            }
	                                            mail_tmp = 1;
	                                        }
	                                        player.addBoxCount(1);
	                                        int item_id = 0;
	                                        item_id = di.getItemId();
	                                        String item_msg = Items.getMessage(item_id,1,player.getPlayerName(),di.getName(),"钥匙串");
	                                        if (item_msg != null){
	                                        	chatService.sendWorldMessage(-1, "系统", item_msg);
	                                        }
                                		}
                                      //额外给经验
                                        if (player.getLevel()<100){
                                        	if (Utils.hit(rnd,5,100)){
                                        		//精华蛋
                                        		IItemTemplate itemtemplate = Items.getTemplate(550034);
                                        		if (itemtemplate != null) {
                            	                    IItem item_tmp = player.completeAddItem(itemtemplate.
                            	                            newInstance(), 1, changed, player.getClientDataVersion());
                            	                    if (item_tmp == null) {
                            	                    	if (sum == 0) {
                            	                    		connectService.sendMessage(player.getId(),
                            	                    		"由于背包满，您的宝箱奖励品已经邮寄到邮箱中，请注意查收。");
                            	                    		sum ++;
                            	                    	}
                            	                    	byte[] att = ItemUtils.item2dbAttachment(itemtemplate.newInstance(),
                            	                        		1);
                            	                        mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
                            	                        		itemtemplate.getName() + "*" + 1, "", att, 0, true);

                            	                    }
                                        		}
//                                        		player.addExp(exp*3, changed);
                                        		chatService.sendPrivateMessage(-1,"系统",player.getId(),
            									"恭喜你获得"+itemtemplate.getName()+"，活动期间有一定机率获得额外的奖励哦");
                                        	}else if (Utils.hit(rnd,10,95)){
                                            	//获得经验
                                            	int exp = (BathHouse.EXP[player.getLevel()] * 360) / 100;
                                            	if(Utils.hit(rnd,10,100)){
                                            		IItemTemplate itemtemplate = Items.getTemplate(550047);
                                            		if (itemtemplate != null) {
                                	                    IItem item_tmp = player.completeAddItem(itemtemplate.
                                	                            newInstance(), 1, changed, player.getClientDataVersion());
                                	                    if (item_tmp == null) {
                                	                    	if (sum == 0) {
                                	                    		connectService.sendMessage(player.getId(),
                                	                    		"由于背包满，您的宝箱奖励品已经邮寄到邮箱中，请注意查收。");
                                	                    		sum ++;
                                	                    	}
                                	                    	byte[] att = ItemUtils.item2dbAttachment(itemtemplate.newInstance(),
                                	                        		1);
                                	                        mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
                                	                        		itemtemplate.getName() + "*" + 1, "", att, 0, true);

                                	                    }
                                            		}
//                                            		player.addExp(exp*3, changed);
                                            		chatService.sendPrivateMessage(-1,"系统",player.getId(),
                									"恭喜你获得"+itemtemplate.getName()+"，活动期间有一定机率获得额外的经验奖励哦");
                                            	}else if (Utils.hit(rnd,30,90)){
                                            		IItemTemplate itemtemplate = Items.getTemplate(550046);
                                            		if (itemtemplate != null) {
                                	                    IItem item_tmp = player.completeAddItem(itemtemplate.
                                	                            newInstance(), 1, changed, player.getClientDataVersion());
                                	                    if (item_tmp == null) {
                                	                    	if (sum == 0) {
                                	                    		connectService.sendMessage(player.getId(),
                                	                    		"由于背包满，您的宝箱奖励品已经邮寄到邮箱中，请注意查收。");
                                	                    		sum ++;
                                	                    	}
                                	                    	byte[] att = ItemUtils.item2dbAttachment(itemtemplate.newInstance(),
                                	                        		1);
                                	                        mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
                                	                        		itemtemplate.getName() + "*" + 1, "", att, 0, true);

                                	                    }
                                            		}
//                                            		player.addExp(exp*2, changed);
                                            		chatService.sendPrivateMessage(-1,"系统",player.getId(),
                									"恭喜你获得"+itemtemplate.getName()+"，活动期间有一定机率获得额外的经验奖励哦");
                                            	}else{
                                            		IItemTemplate itemtemplate = Items.getTemplate(550045);
                                            		if (itemtemplate != null) {
                                	                    IItem item_tmp = player.completeAddItem(itemtemplate.
                                	                            newInstance(), 1, changed, player.getClientDataVersion());
                                	                    if (item_tmp == null) {
                                	                    	if (sum == 0) {
                                	                    		connectService.sendMessage(player.getId(),
                                	                    		"由于背包满，您的宝箱奖励品已经邮寄到邮箱中，请注意查收。");
                                	                    		sum ++;
                                	                    	}
                                	                    	byte[] att = ItemUtils.item2dbAttachment(itemtemplate.newInstance(),
                                	                        		1);
                                	                        mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
                                	                        		itemtemplate.getName() + "*" + 1, "", att, 0, true);

                                	                    }
                                            		}
//                                            		player.addExp(exp, changed);
                                            		chatService.sendPrivateMessage(-1,"系统",player.getId(),
                									"恭喜你获得"+itemtemplate.getName()+"，活动期间有一定机率获得额外的经验奖励哦");
                                            	}
                                            }
                                        }else{
                                        	if (Utils.hit(rnd,5,100)){
                                        		//精华蛋
                                        		IItemTemplate itemtemplate = Items.getTemplate(550034);
                                        		if (itemtemplate != null) {
                            	                    IItem item_tmp = player.completeAddItem(itemtemplate.
                            	                            newInstance(), 1, changed, player.getClientDataVersion());
                            	                    if (item_tmp == null) {
                            	                    	if (sum == 0) {
                            	                    		connectService.sendMessage(player.getId(),
                            	                    		"由于背包满，您的宝箱奖励品已经邮寄到邮箱中，请注意查收。");
                            	                    		sum ++;
                            	                    	}
                            	                    	byte[] att = ItemUtils.item2dbAttachment(itemtemplate.newInstance(),
                            	                        		1);
                            	                        mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
                            	                        		itemtemplate.getName() + "*" + 1, "", att, 0, true);

                            	                    }
                                        		}
//                                        		player.addExp(exp*3, changed);
                                        		chatService.sendPrivateMessage(-1,"系统",player.getId(),
            									"恭喜你获得"+itemtemplate.getName()+"，活动期间有一定机率获得额外的奖励哦");
                                        	}else if (Utils.hit(rnd,5,95)){
                                    			player.addCredit(12,changed);
                                    			chatService.sendPrivateMessage(-1,"系统",player.getId(),
                    									"恭喜你获得12点荣誉，活动期间有一定机率获得额外的荣誉奖励哦");
                                    		}
                                    	}
                                        //mengjie add end
                                	}
                                    needRemove = true;
                                    if (mail_tmp == 1){
                                    	ok = false;
                                    }else{
                                    	ok = true;
                                    }
                                }else{
                                    needRemove = false;
                                    ok = false;
                                    sendMessage(player.getId(),"你没有" + effect.getCount() + "个宝箱!");
                                }
                        	}
                            
                        }
                    }else if(effects[i].getType()==45){ //世界地图卷轴
                    	if(player!=null){
	                    	WorldMap wmap = WorldMap.getWorldMapbypointid(player.getMapId());
	                    	if(wmap != null){
	                    		boolean canTransfer = true;
	                    		GameMap playerMap = player.getMap();
                            	if(playerMap != null){
                    				NoDoor door = NoDoor.getNoTransfer(playerMap.getMapId());
                    				if(door != null){
                    					sendMessage(player.getId(), door.getMessage());
                    					canTransfer = false;
                    				}
                    			}
                            	if(canTransfer){
		                    		player.setJumpMapId(player.getMapId());
		                    		GameMap map = player.getMap();
		                    		if(map != null){
		                    			if (map.getInstance() == null) {
				                    		player.setJumpX((short)(player.getX() / map.getTileWidth()));
				                    		player.setJumpY((short)(player.getY() / map.getTileHeight()));
				                    		sendGotoMap(player.getId(), (short)177, (short)wmap.getX(), (short)wmap.getY());
				                    		needRemove = false;
				                            ok = true;
			                    		}else{
			                    			sendMessage(player.getId(),"你所在的位置不能使用世界地图卷轴!");
				                    		needRemove = false;
				                            ok = true;
			                    		}
		                    		}else{
		                    			sendMessage(player.getId(),"你所在的位置不能使用世界地图卷轴!");
			                    		needRemove = false;
			                            ok = true;
		                    		}
                            	}else{
                            		needRemove = false;
		                            ok = true;
                            	}
	                    	}else{
	                    		sendMessage(player.getId(),"你所在的位置不能使用世界地图卷轴!");
	                    		needRemove = false;
	                            ok = true;
	                    	}
                    	}
                    }else if(effects[i].getType()==46){ //保护盾功能
                    	//加额外属性
                    	SaveShieldEffect effect = (SaveShieldEffect)effects[i];
                    	if (effect.getTime() != 0) {
                            Buf buf = new Buf(bufId.incrementAndGet(), effect.getProperty(),
                                              effect.getValue(), effect.getTime(),
                                              effect.getUnit());
                            //lisen modify
                            //player.addBuf(buf, changed);
                            tmpInd=player.addBufNew(buf, changed, arrBuffPro,tmpInd);
                            //end
                            buf.setTimestamp(System.currentTimeMillis());
                            if (buf.getProperty() < 0) {
                                sendMessage(player.getId(), item.getDesc());
                            }
                        } else {
                            if (effect.getProperty() == Changed.HP) {
                                player.addHp(effect.getValue());
                                if (item.getType() == IItem.TYPE_EXTENDED) {
                                    changed.addProperty(Changed.HP, effect.getValue());
                                }
                            } else if (effect.getProperty() == Changed.MP) {
                                player.addMp(effect.getValue());
                                if (item.getType() == IItem.TYPE_EXTENDED) {
                                    changed.addProperty(Changed.MP, effect.getValue());
                                }
                            }
                        }
                    	if(player.getLevel() == 100){
                    	}else{
                    	//偷袭盾
                            Buf buf = new Buf( -1, Buf.GUARD, 0, 3600 * 6, Buf.UNIT_SECOND);
                            player.addBuf(buf,null);
//                            player.setDeadTime(2);
                            changed.setProperty(Changed.GUARDSTATE, 1);
                            player.setNeedRefreshPosition(true);
                            buf.setTimestamp(System.currentTimeMillis());
                    	}  
                    	needRemove = true;
                    	ok = true;
                    //mengjie add end
                    }else if(effects[i].getType() == 47){ //砸蛋道具
                        EggEffect effect = (EggEffect)effects[i];
                        
                        DropGroup group = DropGroups.getDropGroup(effect.getGroup(),player.getLevel());
                        if(group != null){
	                        int rate = rnd.nextInt(group.getRate());
	                        DropItem dropItem = group.calcDropItem(rate);
	                        int count = getCount(rnd, dropItem.getMin(), dropItem.getMax());
	                        IItem di = dropItem.getItem().newInstance();
	                        if(dropItem.getItem() instanceof EquipmentTemplate){
	                        	IEquipment tmpEqu = (IEquipment)di;
	                        	tmpEqu.setDataVersion(player.getClientDataVersion());
	                        }
	                        //发公告
	                        int item_id = 0;
	                        item_id = di.getItemId();
	                        String item_msg = "";
	                        if(player.completeAddItem(di,count,changed, player.getClientDataVersion())==null){
	                        	if (mailitem){
	                        		byte[] att = ItemUtils.item2dbAttachment(di, count);
	                                mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", di.getName(), "精美物品", att, 0, true);
	                                sendMessage(player.getId(),"你的背包满了，已经把物品邮寄到你的邮箱!");
	                                //发邮件中的物品再发一次公告
	                                if(item.getItemId() == 201628){//庆典祝福
	                                	String itemname = Utils.getClientItemColor(dropItem.getItem().getQuality()) + dropItem.getItem().getName() + "</c>";
		                        		int length = ((Integer)di.getItemId()).toString().length();
	                            		String appendString = "/s 1#" + length + " " + di.getItemId();
	                                	item_msg = player.getPlayerName() + "参加了周年庆典祝福活动，获得明珠天使赐福的" + itemname + appendString;
	                                	chatService.sendWorldMessage(-1, "系统", item_msg);
	                                }else{
	                                	IItem iitem = null;
	                                	if(dropItem.getItem() instanceof EquipmentTemplate){
	                                		iitem = di;
	                                	}
	                                	if(item.getItemId() == 201621){//新宝箱
	                                		item_msg = Items.getMessage(item_id,9,player.getPlayerName(),di.getName(),item.getName(), iitem);
	                                	}else if(item.getItemId() == 201691){//诺亚方舟站票
	                                		/*Calendar calendar = Calendar.getInstance();
	                                		calendar.set(2013, 1,12,0,0,0);
	                                		long start = calendar.getTimeInMillis();
	                                		calendar.set(2013,1,12,24,0,0);
	                                		long end = calendar.getTimeInMillis();
	                                		if(System.currentTimeMillis()>=start && System.currentTimeMillis()<= end){
	                                			item_msg = Items.getMessage(item_id,11,player.getPlayerName(),di.getName(),item.getName(), iitem);
	                                		}else{*/
//	                                			 int[] ret = new int[2];
//	                                	            ret[0] = 0;
//	                                	            ret[1] = petId;
//	                                	            return ret;
	                                		/*}*/
	                                		item_msg = Items.getMessage(item_id,11,player.getPlayerName(),di.getName(),item.getName(), iitem);

	                                	}
	                                	else{
	                                		item_msg = Items.getMessage(item_id,7,player.getPlayerName(),di.getName(),item.getName(), iitem);
	                                	}
	                                	if (item_msg != null){
	                                		chatService.sendWorldMessage(-1, "系统", item_msg, iitem);
	                                	}
	                                }
	                                needRemove = true;
	                                ok = true;
	                        	}else{
	                        		needRemove = false;
	                                ok = false;
	                        	}
	                        }else{
	                        	if(item.getItemId() == 201628){//庆典祝福
	                        		String itemname = Utils.getClientItemColor(dropItem.getItem().getQuality()) + dropItem.getItem().getName() + "</c>";
	                        		int length = ((Integer)di.getItemId()).toString().length();
                            		String appendString = "/s 1#" + length + " " + di.getItemId();
                                	item_msg = player.getPlayerName() + "参加了周年庆典祝福活动，获得明珠天使赐福的" + itemname + appendString;
                                	chatService.sendWorldMessage(-1, "系统", item_msg);
                                }else{
                                	IItem iitem = null;
                                	if(dropItem.getItem() instanceof EquipmentTemplate){
                                		iitem = di;
                                	}
                                	if(item.getItemId() == 201621){//新宝箱
                                		item_msg = Items.getMessage(item_id,9,player.getPlayerName(),di.getName(),item.getName(), iitem);
                                	}else if(item.getItemId() == 201691){//诺亚方舟站票
                                		/*Calendar calendar = Calendar.getInstance();
                                		calendar.set(2013, 1,12,0,0,0);
                                		long start = calendar.getTimeInMillis();
                                		calendar.set(2013,1,12,24,0,0);
                                		long end = calendar.getTimeInMillis();
                                		if(System.currentTimeMillis()>=start && System.currentTimeMillis()<= end){
                                			item_msg = Items.getMessage(item_id,11,player.getPlayerName(),di.getName(),item.getName(), iitem);
                                		}else{*/
                            			item_msg = Items.getMessage(item_id,11,player.getPlayerName(),di.getName(),item.getName(), iitem);

                                		}

                                	/*}*/
                                	else{
                                		item_msg = Items.getMessage(item_id,7,player.getPlayerName(),di.getName(),item.getName(), iitem);
                                	}
                                	if (item_msg != null){
                                		chatService.sendWorldMessage(-1, "系统", item_msg, iitem);
                                	}
                                }
	                        	needRemove = true;
	                            ok = true;
	                        }
	                        log.info("ID[" + player.getId() + "] Get Item itemid[" +item_id +"] itemName[" + di.getName() + "]Changed[" +Utils.getHexdump(changed.toBytes()) +"] Egg");
                        }else{
                        	needRemove = true;
                        	ok = true;
                        }
                    }else if (effects[i].getType() == 48){//改名符
                    	byte[] bytes = stageService.getTaskBytes((short) 31001,
                                new String[] {"是否修改角色名字?\n1.是\n2.否",
                                "输入角色名:", "renamemerger " + item.getItemId() + " "});
                        UWAPSegment seg = new UWAPSegment(ClientConstants.
                                GET_FILE_OK);
                        seg.writeShort((short) 31001);
                        seg.writeShort((short) 2);
                        seg.write(bytes);
                        connectService.writeTo(seg, player.getId());
                        needRemove = false;
                        ok = true;
                    } else if(effects[i].getType()==49){ //addpetexp
                        AddPetExpEffect effect = (AddPetExpEffect)effects[i];
                        Pet pet = player.getPet();
                        if(pet != null){
	                        if (pet.getLevel() > 99){
	                        	sendMessage(player.getId(),"您的宠物已经满级了哦，不能再食用了。");
	                        	needRemove = false;
	                            ok = true;
	                        }else{
	                        	if (player.tryAddPetExp(BathHouse.PET_EXP[pet.getLevel()]*effect.getCount(), changed)) {
	                        		needRemove = true;
	                        	} else {
	                        		sendMessage(player.getId(),"宠物等级不能超过人物等级，不能再食用了。");
	                        		needRemove = false;
	                        	}
	                        	ok = true;
	                        }
	                        
                        }else{
                        	//未装备宠物
//                        	IItem di = null;
//                        	if (player.getLevel() == 100){
//                        		di = Items.getTemplate(200297).newInstance();
//                        	}else{
//                        		di = Items.getTemplate(200296).newInstance();
//                        	}
//                        	//IItem di = Items.getTemplate(id).getItem().newInstance();
//                            if(player.completeAddItem(di,1,changed)==null){
//                                byte[] att = ItemUtils.item2dbAttachment(di, 1);
//                                mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", di.getName(), "", att, 0, true);
//                                sendMessage(player.getId(),"你的背包满了，已经把物品邮寄到你的邮箱!");
//                            }
                        	sendMessage(player.getId(),"请装备上宠物后再使用此功能。");
                        	needRemove = false;
                            ok = true;
                        }
                    }else if (effects[i].getType() == 50){//N级可用礼包
                    	LevellimitEffect effect = (LevellimitEffect)effects[i];
                    	if (player.getLevel()<effect.getLevel()){
                    		sendMessage(player.getId(),"您还没有升到" + effect.getLevel() + "级,不能用哦，加油吧。");
                    		needRemove = false;
                            ok = true;
                            break;
                    	}else{
                    		IItem it = Items.getTemplate(effect.getItemid()).newInstance();
                    		if(player.completeAddItem(it,effect.getCount(),changed, player.getClientDataVersion())==null){
                    			if (mailitem){
                    				byte[] att = ItemUtils.item2dbAttachment(it, effect.getCount());
                                    mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", it.getName(), "", att, 0, true);
                                    sendMessage(player.getId(),"你的背包满了，已经把物品邮寄到你的邮箱!");
                                    needRemove = true;
                                    ok = true;
                    			}else{
                    				needRemove = false;
                                    ok = false;
                    			}
                            }else{
                            	needRemove = true;
                                ok = true;
                            }
                    		
                    	}
                    }else if (effects[i].getType() == 51){//增加好友度
                    	if(0 == player.getFriends().length){
                    		sendMessage(player.getId(),"您还没有好友,请有了好友再使用吧！");
                    		needRemove = false;
                            ok = true;
                    	}else{
                    		Friend[] friends = player.getFriends();
                    		UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST);
                            seg.writeShort((short) 10233);
                            seg.writeString("好友列表");
                            seg.write((byte) 3);
                            seg.writeShort((short) friends.length);
                            for (i = 0; i < friends.length; i++) {
                                seg.writeInt(friends[i].getId());
                                String  tempNameString ;
                                tempNameString = friends[i].getName();
                                WorldPlayer dest = playerService.getWorldPlayer(friends[i].getId());
                                if (dest!=null && dest.online()) {//在线
                                	tempNameString = tempNameString.concat(" 在线 好友度 ");
                                }else{//离线
                                	tempNameString = tempNameString.concat(" 离线 好友度 ");
                                }
                                tempNameString = tempNameString+friends[i].getFavorite();
                                seg.writeString(tempNameString);
                                seg.writeInt(Utils.CLR_WHITE);
                            }
                            seg.write((byte) 1);
                            seg.writeString("使用");
                            seg.writeString("addFriendFavorite");
                            
                            connectService.writeTo(seg, player.getId());
                            needRemove = false;
                            ok = true;
                    	}
                    }else if (effects[i].getType() == 52){//推荐符
                    	if (Server.iMoneyType == Server.IMONEY_TYPE_CMCC){
                    		byte[] bytes = stageService.getTaskBytes((short) 31001,
                                    new String[] {"推荐自己的吉林移动号码的好友玩明珠幻想吧,一起游戏才更有意思！在我这里直接输入他的手机号，就可以喽~~\n1.现在就输入\n2.等等，我先查查电话簿",
                                    "朋友的手机号:", "recommended "});
                            UWAPSegment seg = new UWAPSegment(ClientConstants.
                                    GET_FILE_OK);
                            seg.writeShort((short) 31001);
                            seg.writeShort((short) 2);
                            seg.write(bytes);
                            connectService.writeTo(seg, player.getId());
                            log.info("Recommended ID[" + player.getId() + "]Accountid[" +
                            		player.getAccountId() +"]TRY BY items");
                            needRemove = false;
                            ok = true;
                    	}else{
                    		sendMessage(player.getId(),"只有移动服务器才可以使用该物品。");
                    		needRemove = false;
                            ok = true;
                    	}
                    }else if (effects[i].getType() == 53){//超Q工资卡
//                    	if ((player.isSubscribe()) && (Server.iMoneyType == Server.IMONEY_TYPE_QQ)){
                    	SuperQimoneyEffect effect = (SuperQimoneyEffect)effects[i];
                    	if (effect.getOuttype() == 1){
                    		if (Server.iMoneyType == Server.IMONEY_TYPE_QQ){
                    			log.info("ID[" + player.getId() + "]ADD imoney QUAN[" +
                    					effect.getImoney() +"]TRY BY items");
                    			addLogService.log.info("ID[" + player.getId() + "]ADD imoney QUAN[" +
                    					effect.getImoney() +"]TRY BY items");
                    			try {
	                        		Friends friendstmp = friendsService.getFriends(player.getId());
	    	        				if (friendstmp != null){
	    	        					friendsService.addfriendimoney(player.getId(), effect.getImoney());
	    	        				}else{
	    	        					friendstmp = new Friends();
	    	        					friendstmp.setFriendplayerid(-1);
	    	        					friendstmp.setImoney(effect.getImoney());
	    	        					friendstmp.setLevel(player.getLevel());
	    	        					friendstmp.setPlayerid(player.getId());
	    	        					friendstmp.setPlayername(player.getPlayerName());
	    	        					friendstmp.setValid((byte) 0);
	    	        					friendsService.addfriendbyfriend(friendstmp);
	    	        				}
                    			} catch (BuyException e) {
                    				log.info("ID[" + player.getId() + "][超q工资卡使用失败]"+e);
        							needRemove = false;
                                	ok = false;
								}
                        		log.info("ID[" + player.getId() + "]ADD imoney QUAN[" +
                    					effect.getImoney() +"]OK");
                        		sendMessage(player.getId(),"工资卡使用成功，请查收您的代金券。");
                                needRemove = true;
                                ok = true;
                        	}else{
                        		sendMessage(player.getId(),"只有QQ服务器才可以使用该物品。");
                        		needRemove = false;
                                ok = true;
                        	}
                    	}else{
                    		needRemove = false;
                            ok = true;
                    	}
                    	
                    } else if(effects[i].getType()==54){ //CMCC区送话费奖励
                    	CMCCKeyEffect effect = (CMCCKeyEffect)effects[i];
                    	if(effect.getCmcctype() == 1){//广东移动金钥匙
                    		if(!player.hasItem(effect.getBoxId(),1)){
                                needRemove = false;
                                ok = true;
                                sendMessage(player.getId(),effect.getMsg());
                            }else{
                        		if(player.completeRemoveItem(effect.getBoxId(),1,changed)!=null){
                        			Calendar cal = Calendar.getInstance();
                        			cal.set(2010, 6, 1, 0, 10, 0);
                      		        boolean phoneFee = true; //送话费标志 超过2010年7月1号00时10分00秒后，不再送话费
                      		        if(cal.getTime().getTime() > System.currentTimeMillis()){
                      		        	phoneFee = false;
                      		        }
                                    //话费奖励及其他特殊处理
                      		        if (effect.getGroup1() == -1){//话费奖励。特殊规则给与
                        				Date date1 = new Date();
                        		    	String str = DateFormat.getDateTimeInstance().format(date1);
                        		    	if(Server.iMoneyType == Server.IMONEY_TYPE_CMCC){
                    						//活动结束，遗留物品奖励
                    						DropGroup group = null;
                    						group = DropGroups.getDropGroup(effect.getGroup2(),player.getLevel());
                    						if(group != null){
                        						int rate = rnd.nextInt(group.getRate());
                                                DropItem dropItem = group.calcDropItem(
                                                        rate);
                                                int count = getCount(rnd,
                                                        dropItem.getMin(),
                                                        dropItem.getMax());
                                                IItem di = dropItem.getItem().newInstance();
                                                byte[] att = ItemUtils.item2dbAttachment(di,count);
                                                sendMessage(player.getId(),"您的获奖通知或奖品已用精灵速递的形式发放给您，请您查收。感谢您对广东移动举办的“幸运寻宝，大礼送不停”活动的支持！");
                        						log.info("GUANGDONG CMCC PLAYER ID[" + player.getId() + "]Accountid[" +
                                    					player.getAccountId() +"]cmccUserId[" + player.Cmcc_list +"]" +
                                                				"count["+Server.cmcc_jilin_count+"]item["+di.getName()+"]---CMCC GUANGDONG");
                								mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
                										"系统提示", "感谢您对广东移动举办的“幸运寻宝，大礼送不停”活动的支持！" +
                                                		"恭喜您在"+str+"获得“"+di.getName()+"”奖励！奖励在邮件附件中。" +
                                                				"活动仍在火爆举行，祝您好运连连，惊喜不断！活动解释权归中国移动通信集团广东有限公司所有", att, 0, true);
                    						}
                        					needRemove = true;
                                            ok = true;
                        				}else{
                        					sendMessage(player.getId(),"此功能您还不能使用。");
                        					needRemove = true;
                                            ok = true;
                        				}
                        			}
                                }else{
                                    needRemove = false;
                                    ok = true;
                                    sendMessage(player.getId(),effect.getMsg());
                                }
                            }
                    	}else if (effect.getCmcctype() == 2){//广东幸运礼券
                    		//话费奖励及其他特殊处理
                			if (effect.getGroup1() == -1){//话费奖励。特殊规则给与
                				Date date1 = new Date();
                		    	String str = DateFormat.getDateTimeInstance().format(date1);
                		    	
                				if(Server.iMoneyType == Server.IMONEY_TYPE_CMCC){
//                					if (( !"".equals(player.Cmcc_list)) && (!Server.cmcc_jilin_playerid.containsKey(player.Cmcc_list))){
//                						Server.cmcc_jilin_count++;
//                						//本日第一次使用的
//                						Server.cmcc_jilin_playerid.put(player.Cmcc_list, Server.cmcc_jilin_count);
//                						if (Server.cmcc_jilin_count<=498){
//                							int mod_tmp = Server.cmcc_jilin_count;
//                							if (mod_tmp <= 59){//8元话费。只发1次
//                								sendMessage(player.getId(),"您的获奖通知或奖品已用精灵速递的形式发放给您，请您查收。感谢您对广东移动举办的“幸运寻宝，大礼送不停”活动的支持！");
//                								log.info("GUANGDONG CMCC PLAYER ID[" + player.getId() + "]Accountid[" +
//                                    					player.getAccountId() +"]cmccUserId[" + player.Cmcc_list +"]" +
//                                                				"count["+Server.cmcc_jilin_count+"]money[8]---CMCC GUANGDONG");
//                								mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
//                                                        "系统提示", "感谢您对广东移动举办的“网游火爆送好礼，惊喜百分百”活动的支持！" +
//                                                        		"恭喜您在"+str+"获得8元话费奖励！话费奖励将在下个月统一送出。" +
//                                                        				"活动仍在火爆举行，祝您好运连连，惊喜不断！活动解释权归中国移动通信集团广东有限公司所有", null, 0, true);
//                							}else{
//                								mod_tmp = Server.cmcc_jilin_count;
//                								if(mod_tmp <= 166 ){//5元话费
//                									sendMessage(player.getId(),"您的获奖通知或奖品已用精灵速递的形式发放给您，请您查收。感谢您对广东移动举办的“幸运寻宝，大礼送不停”活动的支持！");
//                									log.info("GUANGDONG CMCC PLAYER ID[" + player.getId() + "]Accountid[" +
//                                        					player.getAccountId() +"]cmccUserId[" + player.Cmcc_list +"]" +
//                                                    				"count["+Server.cmcc_jilin_count+"]money[5]---CMCC GUANGDONG");
//                    								mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
//                                                            "系统提示", "感谢您对广东移动举办的“网游火爆送好礼，惊喜百分百”活动的支持！" +
//                                                            		"恭喜您在"+str+"获得5元话费奖励！话费奖励将在下个月统一送出。" +
//                                                            				"活动仍在火爆举行，祝您好运连连，惊喜不断！活动解释权归中国移动通信集团广东有限公司所有", null, 0, true);
//                								}else{
//                									sendMessage(player.getId(),"您的获奖通知或奖品已用精灵速递的形式发放给您，请您查收。感谢您对广东移动举办的“网游火爆送好礼，惊喜百分百”活动的支持！");
//                									log.info("GUANGDONG CMCC PLAYER ID[" + player.getId() + "]Accountid[" +
//                                        					player.getAccountId() +"]cmccUserId[" + player.Cmcc_list +"]" +
//                                                    				"count["+Server.cmcc_jilin_count+"]money[2]---CMCC GUANGDONG");
//                    								mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
//                                                            "系统提示", "感谢您对广东移动举办的“网游火爆送好礼，惊喜百分百”活动的支持！" +
//                                                            		"恭喜您在"+str+"获得2元话费奖励！话费奖励将在下个月统一送出。" +
//                                                            				"活动仍在火爆举行，祝您好运连连，惊喜不断！活动解释权归中国移动通信集团广东有限公司所有", null, 0, true);
//                    							}
//                							}
//                							
//                						}else{
//            								DropGroup group = null;
//                    						group = DropGroups.getDropGroup(effect.getGroup2(),player.getLevel());
//                    						if(group != null){
//                        						int rate = rnd.nextInt(group.getRate());
//                                                DropItem dropItem = group.calcDropItem(
//                                                        rate);
//                                                int count = getCount(rnd,
//                                                        dropItem.getMin(),
//                                                        dropItem.getMax());
//                                                IItem di = dropItem.getItem().newInstance();
//                                                byte[] att = ItemUtils.item2dbAttachment(di,count);
//                                                sendMessage(player.getId(),"您的获奖通知或奖品已用精灵速递的形式发放给您，请您查收。感谢您对广东移动举办的“网游火爆送好礼，惊喜百分百”活动的支持！");
//                        						log.info("GUANGDONG CMCC PLAYER ID[" + player.getId() + "]Accountid[" +
//                                    					player.getAccountId() +"]cmccUserId[" + player.Cmcc_list +"]" +
//                                                				"count["+Server.cmcc_jilin_count+"]item["+di.getName()+"]---CMCC GUANGDONG");
//                								mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
//                										"系统提示", "感谢您对广东移动举办的“网游火爆送好礼，惊喜百分百”活动的支持！" +
//                                                		"恭喜您在"+str+"获得“"+di.getName()+"”奖励！奖励在邮件附件中。" +
//                                                				"活动仍在火爆举行，祝您好运连连，惊喜不断！活动解释权归中国移动通信集团广东有限公司所有", att, 0, true);
//                    						}
//            							
//                						}
//                					}else{
                						//领过一次或取不到所属地
                						DropGroup group = null;
                						group = DropGroups.getDropGroup(effect.getGroup2(),player.getLevel());
                						if(group != null){
                    						int rate = rnd.nextInt(group.getRate());
                                            DropItem dropItem = group.calcDropItem(
                                                    rate);
                                            int count = getCount(rnd,
                                                    dropItem.getMin(),
                                                    dropItem.getMax());
                                            IItem di = dropItem.getItem().newInstance();
                                            byte[] att = ItemUtils.item2dbAttachment(di,count);
                                            sendMessage(player.getId(),"您的获奖通知或奖品已用精灵速递的形式发放给您，请您查收。感谢您对广东移动举办的“网游火爆送好礼，惊喜百分百”活动的支持！");
                    						log.info("GUANGDONG CMCC PLAYER ID[" + player.getId() + "]Accountid[" +
                                					player.getAccountId() +"]cmccUserId[" + player.Cmcc_list +"]" +
                                            				"count["+Server.cmcc_jilin_count+"]item["+di.getName()+"]---CMCC GUANGDONG");
            								mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
            										"系统提示", "感谢您对广东移动举办的“网游火爆送好礼，惊喜百分百”活动的支持！" +
                                            		"恭喜您在"+str+"获得“"+di.getName()+"”奖励！奖励在邮件附件中。" +
                                            				"活动仍在火爆举行，祝您好运连连，惊喜不断！活动解释权归中国移动通信集团广东有限公司所有", att, 0, true);
                						}
//                					}                    						
                					needRemove = true;
                                    ok = true;
                				}else{
                					sendMessage(player.getId(),"此功能您还不能使用。");
                					needRemove = true;
                                    ok = true;
                				}
                			}
                    	}else if (effect.getCmcctype() == 3){//福建亚运活动
                    		//中奖机会只有 500 311 131 三种。
                    		boolean Openflag = true;
                    		if (Openflag){
	                    		Date date1 = new Date();
	            		    	String str = DateFormat.getDateTimeInstance().format(date1);
	                    		boolean itemflag = true;
	    						boolean groupflag = false;
	    						String cityname = playerclient.cityname;
	                			if (effect.getGroup1() == -1){//话费奖励。特殊规则给与
	                				if(Server.iMoneyType == Server.IMONEY_TYPE_CMCC){
	                					if (cityname!=null && Server.CMCC_fujian_cityname.contains(cityname)){//福建移动
	                						itemflag = true;
	                						if (Server.cmcc_fujian_totalmoney >= 60000){//最大金额60000
//	                							if (Utils.hit(rnd, 50, 100)){//50%话费
	                								if (( !"".equals(player.Cmcc_list)) && (!Server.cmcc_fujian_playerid.containsKey(player.Cmcc_list))){
	            										if (Server.cmcc_fujian_playerid.get(player.Cmcc_list) >= 5){
	            											groupflag = true;
	            										}else{
	            											//第N次使用（N>1）
	            											int tmp_money = 5 - Server.cmcc_fujian_playerid.get(player.Cmcc_list);
	            											if (tmp_money >= 3){//可以中3元话费
	            												sendMessage(player.getId(),"您的获奖通知或奖品已用精灵速递的形式发放给您，请您查收。感谢您对福建移动举办的“亚运激情，大礼无限”活动的支持！");
	                            								log.info("FUJIAN CMCC PLAYER ID[" + player.getId() + "]Accountid[" +
	                                                					player.getAccountId() +"]cmccUserId[" + player.Cmcc_list +"]useridmoney[" 
	                                                						+ Server.cmcc_fujian_playerid.get(player.Cmcc_list) 
	                                                						+ "totalmoney["+Server.cmcc_fujian_totalmoney+"]money[3]---CMCC FUJIAN");
	                            								mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
	                                                                    "系统提示", "感谢您对福建移动举办的“亚运激情，大礼无限”活动的支持！" +
	                                                                    "恭喜您在"+str+"获得3元话费活动奖励！奖励将在次月送出，届时请您留意短信通知。" +
	                                                                    "活动仍在火爆举行，祝您好运连连，惊喜不断！中国移动通信集团福建有限公司所有。", null, 0, true);
	                            								//
	                            								Server.cmcc_fujian_totalmoney = Server.cmcc_fujian_totalmoney + 3;
	                            								Server.cmcc_fujian_playerid.remove(player.Cmcc_list);
	                            								Server.cmcc_fujian_playerid.put(player.Cmcc_list, tmp_money+3);
	            											}else{//1元话费
	            												sendMessage(player.getId(),"您的获奖通知或奖品已用精灵速递的形式发放给您，请您查收。感谢您对福建移动举办的“亚运激情，大礼无限”活动的支持！");
	                            								log.info("FUJIAN CMCC PLAYER ID[" + player.getId() + "]Accountid[" +
	                                                					player.getAccountId() +"]cmccUserId[" + player.Cmcc_list +"]useridmoney[" 
	                                                						+ Server.cmcc_fujian_playerid.get(player.Cmcc_list) 
	                                                						+ "totalmoney["+Server.cmcc_fujian_totalmoney+"]money[1]---CMCC FUJIAN");
	                            								mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
	                                                                    "系统提示", "感谢您对福建移动举办的“亚运激情，大礼无限”活动的支持！" +
	                                                                    "恭喜您在"+str+"获得1元话费活动奖励！奖励将在次月送出，届时请您留意短信通知。" +
	                                                                    "活动仍在火爆举行，祝您好运连连，惊喜不断！中国移动通信集团福建有限公司所有。", null, 0, true);
	                            								//
	                            								Server.cmcc_fujian_totalmoney = Server.cmcc_fujian_totalmoney + 1;
	                            								Server.cmcc_fujian_playerid.remove(player.Cmcc_list);
	                            								Server.cmcc_fujian_playerid.put(player.Cmcc_list, tmp_money+1);
	            											}
	            										}
	            									}else if ( !"".equals(player.Cmcc_list)){//第一次抽奖
	            										if (Utils.hit(rnd, 50, 100)){//50%的几率中3元
	            											sendMessage(player.getId(),"您的获奖通知或奖品已用精灵速递的形式发放给您，请您查收。感谢您对福建移动举办的“亚运激情，大礼无限”活动的支持！");
	                        								log.info("FUJIAN CMCC PLAYER ID[" + player.getId() + "]Accountid[" +
	                                            					player.getAccountId() +"]cmccUserId[" + player.Cmcc_list +"]useridmoney[" 
	                                            						+ Server.cmcc_fujian_playerid.get(player.Cmcc_list) 
	                                            						+ "totalmoney["+Server.cmcc_fujian_totalmoney+"]money[3]---CMCC FUJIAN");
	                        								mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
	                                                                "系统提示", "感谢您对福建移动举办的“亚运激情，大礼无限”活动的支持！" +
	                                                                "恭喜您在"+str+"获得3元话费活动奖励！奖励将在次月送出，届时请您留意短信通知。" +
	                                                                "活动仍在火爆举行，祝您好运连连，惊喜不断！中国移动通信集团福建有限公司所有。", null, 0, true);
	                        								//
	                        								Server.cmcc_fujian_totalmoney = Server.cmcc_fujian_totalmoney + 3;
	                        								Server.cmcc_fujian_playerid.put(player.Cmcc_list, 3);
	        											}else if (Utils.hit(rnd, 50, 100)){//25%的几率中5元
	        												sendMessage(player.getId(),"您的获奖通知或奖品已用精灵速递的形式发放给您，请您查收。感谢您对福建移动举办的“亚运激情，大礼无限”活动的支持！");
	                        								log.info("FUJIAN CMCC PLAYER ID[" + player.getId() + "]Accountid[" +
	                                            					player.getAccountId() +"]cmccUserId[" + player.Cmcc_list +"]useridmoney[" 
	                                            						+ Server.cmcc_fujian_playerid.get(player.Cmcc_list) 
	                                            						+ "totalmoney["+Server.cmcc_fujian_totalmoney+"]money[5]---CMCC FUJIAN");
	                        								mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
	                                                                "系统提示", "感谢您对福建移动举办的“亚运激情，大礼无限”活动的支持！" +
	                                                                "恭喜您在"+str+"获得5元话费活动奖励！奖励将在次月送出，届时请您留意短信通知。" +
	                                                                "活动仍在火爆举行，祝您好运连连，惊喜不断！中国移动通信集团福建有限公司所有。", null, 0, true);
	                        								//
	                        								Server.cmcc_fujian_totalmoney = Server.cmcc_fujian_totalmoney + 5;
	                        								Server.cmcc_fujian_playerid.put(player.Cmcc_list, 5);
	        											}else{//剩下25%中1元
	        												sendMessage(player.getId(),"您的获奖通知或奖品已用精灵速递的形式发放给您，请您查收。感谢您对福建移动举办的“亚运激情，大礼无限”活动的支持！");
	                        								log.info("FUJIAN CMCC PLAYER ID[" + player.getId() + "]Accountid[" +
	                                            					player.getAccountId() +"]cmccUserId[" + player.Cmcc_list +"]useridmoney[" 
	                                            						+ Server.cmcc_fujian_playerid.get(player.Cmcc_list) 
	                                            						+ "totalmoney["+Server.cmcc_fujian_totalmoney+"]money[1]---CMCC FUJIAN");
	                        								mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
	                                                                "系统提示", "感谢您对福建移动举办的“亚运激情，大礼无限”活动的支持！" +
	                                                                "恭喜您在"+str+"获得1元话费活动奖励！奖励将在次月送出，届时请您留意短信通知。" +
	                                                                "活动仍在火爆举行，祝您好运连连，惊喜不断！中国移动通信集团福建有限公司所有。", null, 0, true);
	                        								//
	                        								Server.cmcc_fujian_totalmoney = Server.cmcc_fujian_totalmoney + 1;
	                        								Server.cmcc_fujian_playerid.put(player.Cmcc_list, 1);
	        											}
	            									}
//	                							}else{
//	                								groupflag = true;
//	                							}
	                						}else{
	            								groupflag = true;
	            							}
	                					}else{
	        								groupflag = true;
	        							}
	                					if (groupflag){
	                						DropGroup group = null;
	                						group = DropGroups.getDropGroup(effect.getGroup2(),player.getLevel());
	                						if(group != null){
	                    						int rate = rnd.nextInt(group.getRate());
	                                            DropItem dropItem = group.calcDropItem(
	                                                    rate);
	                                            int count = getCount(rnd,
	                                                    dropItem.getMin(),
	                                                    dropItem.getMax());
	                                            IItem di = dropItem.getItem().newInstance();
	                                            byte[] att = ItemUtils.item2dbAttachment(di,count);
	                                            sendMessage(player.getId(),"您的获奖通知或奖品已用精灵速递的形式发放给您，请您查收。感谢您对福建移动举办的“亚运激情，大礼无限”活动的支持！");
	                                            log.info("FUJIAN CMCC PLAYER ID[" + player.getId() + "]Accountid[" +
	                                					player.getAccountId() +"]cmccUserId[" + player.Cmcc_list +"]" +
	                                					"totalmoney["+Server.cmcc_fujian_totalmoney+"]item["+di.getName()+"]---CMCC FUJIAN");
	            								mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
	            										"系统提示", "感谢您对福建移动举办的“亚运激情，大礼无限”活动的支持！" +
	            										"恭喜您在"+str+"获得惊喜道具奖励1份！奖励在邮件附件中，请注意查收！" +
	            										"活动仍在火爆举行，祝您好运连连，惊喜不断！活动最终解释权归中国移动通信集团福建有限公司所有。", att, 0, true);
	                						}
	                					}
	                					if (itemflag){
	                						IItemTemplate itemtemplate = Items.getTemplate(effect.getItemId());
	                						if (itemtemplate != null){
	                							IItem di = itemtemplate.newInstance();
	                    						byte[] att = ItemUtils.item2dbAttachment(di,1);
	                                            mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
	            										"系统提示", "感谢您对福建移动举办的“亚运激情，大礼无限”活动的支持！" +
	            										"恭喜您在"+str+"获得惊喜道具奖励1份！奖励在邮件附件中，请注意查收！" +
	            										"活动仍在火爆举行，祝您好运连连，惊喜不断！活动最终解释权归中国移动通信集团福建有限公司所有。", att, 0, true);
	                						}
	                						
	                					}
	                					needRemove = true;
	                                    ok = true;
	                				}else{
	                					sendMessage(player.getId(),"此功能您还不能使用。");
	                					needRemove = true;
	                                    ok = true;
	                				}
	                			}
                    		}else{
                        		sendMessage(player.getId(),"此功能您还不能使用。");
            					needRemove = false;
                                ok = true;
                        	}
                    	}else{//其他 CMCC
                    		
                    	}
                    }else if(effects[i].getType()==55){//提示语通用物品
                    	
                    	MassageEffect effect = (MassageEffect)effects[i];
                    	if(effect.getDeleteflag() == 0){
                    		needRemove = false;
                    	}else{
                    		needRemove = true;
                    	}
                        ok = true;
                        sendMessage(player.getId(),effect.getMsg());
                    } else if (effects[i].getType() == 57) {				//扩展家园的仓库
                        AddGridEffectHouse effect = (AddGridEffectHouse) effects[i];
                        HouseData hd;
						try {
							hd = houseModel.getHouseByPlayerId(player.getId());
							if( hd == null ){
								ok = false;
								throw new UseItemException("你还没有家园，不能对仓库进行扩充！");	
	                        }
						} catch (Exception e) {
							ok = false;
 							throw new UseItemException("你还没有家园，不能对仓库进行扩充！");
						}
						if (hd.getAddGridSize() >= effect.getValue()) {
							ok = false;
							throw new UseItemException("你已经使用过同等级别或更高级别的扩容物品，扩容数量为" + hd.getAddGridSize());
                        }
                        hd.setAAGirdSize(effect.getValue());
                        playerService.checkPlayer(player);
            			houseModel.saveHouse(hd);
                        ok = true;
                    } else if (effects[i].getType() == 58) {	// 宠物铠化石
                    	Pet p = player.getPet();
                    	if (p == null) {
                    		needRemove = false;
                    		throw new UseItemException("铠化石无法找到可以解开的封印，请您携带宠物后在试一次。");
                    	}
                    	PetArmorGemstoneEffect effect = (PetArmorGemstoneEffect) effects[i];
                    	int[] equipsFlag = p.getUsedEquipmentinfo();
                    	if (equipsFlag[effect.getParts()] != 2) {
                    		needRemove = false;
                    		throw new UseItemException("铠化石气愤的说：“是谁已经把封印解开了？”当前没有发生任何改变。");
                    	} else {
                    		Random rdm = new Random();
                    		try{
                    			int rate = effect.getProbability();
                    			Buf buf = player.getBuf(Buf.PETArmorGemstone);
            	    			if(buf != null){
            	    				rate = rate*(100 + buf.getValue())/100;
            	    			}
                    			if (Utils.hit(rdm,rate,100)) {
                    				p.setUsedEquipmentsinfo(effect.getParts(), (byte) effect.getValue());
    	                    		UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL);
    	                        	seg.writeShort(ClientConstants.EXTEND_PROTOCOL_PETEQU_LOGIN);
    	                        	seg.writeInt(p.getId());
    	                        	Grid[] usedEquipmentsTemp = p.getUsedEquipments();
    	                    		if (usedEquipmentsTemp != null){
    	                    			for (int j = 0; j < p.getUsedEquipmentinfo().length; j++){
    	                    				seg.write((byte) p.getUsedEquipmentinfo()[j]);
    	                    				if (usedEquipmentsTemp[j] != null) {
    	                    					if (p.getUsedEquipmentinfo()[j] == 1) {
    	                    						IEquipment equtmp = (IEquipment) usedEquipmentsTemp[j].item;
    	                    						equtmp.setDataVersion(player.getClientDataVersion());
    	                    						seg.write(equtmp.toClientBytesWithLevel(p.getLevel()));
    	                    					}
    	                    				}
    	                				}
    	                    		} else {
    	                    			for (int j = 0;j < p.getUsedEquipmentinfo().length; j++){
    	                    				seg.write((byte) p.getUsedEquipmentinfo()[j]);
    	                    			}
    	                    		}
    	                    		// 下发宠物升级经验
    	                    		seg.writeInt(Utils.getPetUpLevelExp(p.getLevel()));
    	                    		
    	                    		//发送宠物阵营宝石效果
    	                    		CampData campData = campMainService.getCampData(player.getCamp());
    	                    		int value = 0;
    	                    		if(campData != null){
    	                		    	List<CampSkillData> list = campData.getSkillDataList();
    	                		    	for(int a = 0; a < list.size(); a++){
    	                		    		CampSkillData temp = (CampSkillData) list.get(a);
    	                		    		CampSkillLevel temp1 = CampConfig.campSkills.get(temp.getEffect()).getLevel(temp.getLevel());
    	                		    		
    	                		    		if(temp1 == null || temp1.getParm1() == 0){
    	                		    			continue;
    	                		    		}else{
    	                		    			if(temp.getEffect() == Buf.CAMP_STONE){//阵营科技中只有阵营宝石buff取值下发
    	                		    				value = temp1.getParm1();
    	                		    				break;
    	                		    			}
    	                		    		}
    	                		    	}
    	                		    }
    	                    		seg.writeInt(value);
    	                    		connectService.writeTo(seg, player.getId());
    	                			sendMessage(player.getId(), "铠化石消失在绚丽的白光中...恭喜您，该部位可以铠化了!");
    	                    		ok = true;
    	                			needRemove = true;
                    			} else {
                    				sendMessage(player.getId(), "铠化石逃跑了...很遗憾，封印没有解开!");
                    				ok = true;
                    				needRemove = true;
                    			}
                			} catch (Exception e) {
                            	log.debug(e, e);
                            }
                    	}
                    } else if (effects[i].getType() == 59){ // 换装物品
                        String model = player.getModel();
                        if ("NK-6681".equals(model) || "MotoV300".equals(model) || "NK-40-2".equals(model)) {
                    		needRemove = false;
                    		throw new UseItemException("您的机型不支持此功能");
                        }
                        DressItemEffect effect = (DressItemEffect)effects[i];
                        RoleFaceData face = RoleFaces.getRoleFace(effect.getFaceId());
                        if (face == null) {
                        	needRemove = false;
                        	canUseBuff = false;
                            sendMessage(player.getId(), "此形象暂时不存在");
                            /*} else if(player.getFace() == effect.getFaceId()){
                        	needRemove = false;
                        	canUseBuff = false;
                        	sendMessage(player.getId(), "您已经是"+face.getName()+"的形象了,无需更换。");*/
                        } else {
                            if (player.getSex() % 2 != face.getFace() % 2) {
                                sendMessage(player.getId(), "你不允许使用此形象");
                                needRemove = false;
                                canUseBuff = false;
                            } else {
//                        		// buff重计
//                             	RoleFaceData selfFace = RoleFaces.getRoleFace(player.getFace());
//                                IItemTemplate oldItemtemplate = Items.getTemplate(selfFace.getItemId());
//                                IItem  SelfFaceItem = oldItemtemplate.newInstance();
//                                Effect[] effectFace = ((IEffectItem)SelfFaceItem).getEffects();
//                                int oldFaceProperty = 0;
//                                int oldFaceBuffTime = 0;
//                                for (int m = 0; m < effectFace.length; m++) {
//	     							if (effectFace[m].getType() == 1) {
//	     								PropertyEffect effectOld = (PropertyEffect) effectFace[m];
//	     								oldFaceProperty = effectOld.getProperty();
//	     								oldFaceBuffTime = effectOld.getTime();
//	     								// added by Jeremy:遍历下目前所有buff
//	     								Buf[] bufArray = player.getBufs();
//	     								for (int n = 0; n < bufArray.length; n++) {
//	     									if (bufArray[n].getProperty() == oldFaceProperty) {
//	     										long now = new Date().getTime();
//	     										long checkTime = (((bufArray[n].getTimestamp() + bufArray[n].getTime() * 1000L) - now) / 1000L);
//	     										if (checkTime > oldFaceBuffTime) {
//	     											player.removeBuf(bufArray[n],changed);
//	     											Buf bufReplace = new Buf(
//	     													bufId.incrementAndGet(),
//	     													(byte) oldFaceProperty,
//	     													effectOld.getValue(),
//	     													(int) (checkTime - oldFaceBuffTime),
//	     													effectOld.getUnit());
//	     											bufReplace.setTimestamp(now);
//	     											player.addBuf(bufReplace, changed);
//	     										} else {
//	     											player.removeBuf(bufArray[n],
//	     													changed);
//	     										}
//	     									}
//	     								}
//	     							}
//     						  }
//        	                  // 更换形象
//                              player.setFace((short)face.getFace());
								int error = player.isCanBuyFace(face.getFace());
								if (error == 1) {
									sendMessage(player.getId(), "已经把你的形象放到形象橱窗里了！");
								//                                  canUseBuff = false;
								    needRemove = true;
								} else if (error == 0) {
									if (player.completeAddRoleFace(face.getFace(), 1, changed, face.getDuration()) != null) {// 添加到橱窗里
										player.resetImage();
										playerService.savePlayer(player);
									}			
								//                            	  canUseBuff = false;
								  /*changed = new Changed();*/
								//                                  changed.setProperty(Changed.FACE, player.getFace());
								//                                  /*connectService.sendGetItem(changed, player.getId(), (byte) 20);*/
									sendMessage(player.getId(), "已经把你的形象放到形象橱窗里了！");
								    needRemove = true;
								} else if (error == 2) {												// 续费
									if (player.completeAddRoleFace(face.getFace(), 1, changed, face.getDuration()) != null) {// 添加到橱窗里
										player.resetImage();
										playerService.savePlayer(player);
									}
									sendMessage(player.getId(), "已经把你的形象放到形象橱窗里了！");
								    needRemove = true;
								}
                            } 
						}
                        ok = true;		//?
                    } else if(effects[i].getType() == 60){		// 控制删除物品
                        RemoveItemEffect effect = (RemoveItemEffect)effects[i];
                        if(effect.getRemoveItem() == 1){
                        	needRemove = false;
                        } else if(effect.getRemoveItem() == 0){
                        	needRemove = true;
                        }
                        ok = true;
                    }else if(effects[i].getType() == 61){
	                     DropGroupListEffect effect = (DropGroupListEffect)effects[i]; 
	                     DropGroup group = DropGroups.getDropGroup(effect.getGroup(),player.getLevel());
	                     ArrayList list = (ArrayList)group.getDropItems();				// 获得掉落组的列表
	                     UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST);
	                     seg.writeShort((short) 4);
	                     seg.writeString("物品列表");
	                     seg.write((byte)4);
	                     seg.writeShort((short) list.size());
	                     for (int j = 0; j < list.size(); j++) {
	                    	 DropItem dropItem = (DropItem)list.get(j);
	                         seg.writeInt(dropItem.getItem().getItemId());					// 客户端返回的
	                         seg.writeString(dropItem.getItem().getName());
	                         seg.writeInt(Utils.CLR_WHITE);
	                     }
	                     seg.write((byte) 1);
	                     seg.writeString("领取物品");
	//                     seg.writeString("addItem " + item.getItemId() + " "+ effect.getCount());
	                     seg.writeString("usePackage " + item.getItemId());
	                     connectService.writeTo(seg,player.getId());
	                     ok = true;
	                     needRemove = false;
                    }else if(effects[i].getType() == 62){
                    	 //预留
                    	
                                     
                    } else if (effects[i].getType() == 63) { // 狮子吼
                    	byte[] bytes = stageService.getTaskBytes((short) 31052,
                                new String[] {"狮子吼", "send_message " + item.getItemId() + " "});
                        UWAPSegment seg = new UWAPSegment(ClientConstants.
                                GET_FILE_OK);
                        seg.writeShort((short) 31052);
                        seg.writeShort((short) 2);
                        seg.write(bytes);
                        connectService.writeTo(seg, player.getId());
                        needRemove = false;
                        ok = true;
                    }  else if (effects[i].getType() == 64) { // 发放阵营形象
                        String model = player.getModel();
                        if ("NK-6681".equals(model) || "MotoV300".equals(model) || "NK-40-2".equals(model)) {
                    		needRemove = false;
                    		throw new UseItemException("您的机型不支持此功能");
                        }
                        SendCampSuitEffect effect = (SendCampSuitEffect)effects[i];
                        int sex = effect.getSex();
                        int camp = effect.getCamp();
                        int level = effect.getLevel();
                        RoleFaceData face = null;
                        int faceId = 0;
                        switch (level){//为将来不同的阵营形象预留
                        	case 0:
                            	if(player.getSex() == 0)	// male
                            	{
                            		if(player.getCamp() == 1){	// black
                            			faceId = 30;
                            			face = RoleFaces.getRoleFace(faceId);
                            		}else if(player.getCamp() == 2){	// light
                            			faceId = 28;
                            			face = RoleFaces.getRoleFace(faceId);
                            		}
                            	} else if (player.getSex() == 1){  // female
                            		if(player.getCamp() == 1){	// black
                            			faceId = 31;
                            			face = RoleFaces.getRoleFace(faceId);
                            		}else if(player.getCamp() == 2){	// light
                            			faceId = 29;
                            			face = RoleFaces.getRoleFace(faceId);
                            		}
                            	}
                        		break;
                        	case 1:
                        		break;
                        }
                        if (face == null) {
                        	needRemove = false;
                            sendMessage(player.getId(), "您暂时不能使用此形象");
                            canUseBuff = false;
                        } /*else if(player.getFace() == faceId){
                        	needRemove = false;
                        	canUseBuff = false;
                        	sendMessage(player.getId(), "您已经是"+face.getName()+"的形象了,无需更换。");
                        }*/ else {
//                            // buff重计
//                         	RoleFaceData selfFace = RoleFaces.getRoleFace(player.getFace());
//                            IItemTemplate oldItemtemplate = Items.getTemplate(selfFace.getItemId());
//                            IItem  SelfFaceItem = oldItemtemplate.newInstance();
////                            IItem  SelfFaceItem = player.completeAddItem(oldItemtemplate.
////                            		newInstance(), 1, changed, player.getClientDataVersion());
////                            
////    	                    if (SelfFaceItem == null) {
////    	                    	connectService.sendMessage(player.getId(),
////    	                    			"由于背包满，您原有的形象物品:" + oldItemtemplate.getName() + "已经邮寄到邮箱中，请注意查收。");
////    	                    	byte[] att = ItemUtils.item2dbAttachment(oldItemtemplate.newInstance(),
////    	                        		1);
////    	                        mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
////    	                        		oldItemtemplate.getName() + "*" + 1, "", att, 0, true);
////    	                    }
//                            Effect[] effectFace = ((IEffectItem)SelfFaceItem).getEffects();
//                            int oldFaceProperty = 0;
//                            int oldFaceBuffTime = 0;
//                            for (int m = 0; m < effectFace.length; m++) {
//     							if (effectFace[m].getType() == 1) {
//     								PropertyEffect effectOld = (PropertyEffect) effectFace[m];
//     								oldFaceProperty = effectOld.getProperty();
//     								oldFaceBuffTime = effectOld.getTime();
//     								// added by Jeremy:遍历下目前所有buff
//     								Buf[] bufArray = player.getBufs();
//     								for (int n = 0; n < bufArray.length; n++) {
//     									if (bufArray[n].getProperty() == oldFaceProperty) {
//     										long now = new Date().getTime();
//     										long checkTime = (((bufArray[n].getTimestamp() + bufArray[n].getTime() * 1000L) - now) / 1000L);
//     										if (checkTime > oldFaceBuffTime) {
//     											player.removeBuf(bufArray[n],changed);
//     											Buf bufReplace = new Buf(
//     													bufId.incrementAndGet(),
//     													(byte) oldFaceProperty,
//     													effectOld.getValue(),
//     													(int) (checkTime - oldFaceBuffTime),
//     													effectOld.getUnit());
//     											bufReplace.setTimestamp(now);
//     											player.addBuf(bufReplace, changed);
//     										} else {
//     											player.removeBuf(bufArray[n],
//     													changed);
//     										}
//     									}
//     								}
//     							}
// 						  }
//    	                  // 更换形象
//                          player.setFace((short)face.getFace());
//                          /*changed = new Changed();*/
//                          changed.setProperty(Changed.FACE, player.getFace());
//                          /*connectService.sendGetItem(changed, player.getId(), (byte) 20);*/
//                          sendMessage(player.getId(), "换装成功!");
                        	int error = player.isCanBuyFace(face.getFace());
                            if(error != 0){
                          	  sendMessage(player.getId(), "你的橱窗中已经存在该形象了。");
//                              canUseBuff = false;
                              needRemove = false;
                            }else{
                          	  if (player.completeAddRoleFace(face.getFace(), 1, changed, face.getExpiration()) != null) {//添加到橱窗里
                          		  player.resetImage();
                          		  playerService.savePlayer(player);
                          	  }			
//                          	  canUseBuff = false;
                          	  needRemove = true;
                              sendMessage(player.getId(), "已经把你的形象放到形象橱窗里了！");
                            }
                            
						}
                        ok = true;		//?
                    }  else if (effects[i].getType() == 65){		//配方书
                    	PrescriptionEffect prescriptionEffect = (PrescriptionEffect)effects[i];
                    	int recipeId = prescriptionEffect.getRecipeId();
                    	Prescription prescription = PrescriptionsAll.getPrescription(recipeId);
                    	Prescription[] Prescriptions = player.getPlayerPrescription(prescription.getEquType());		//取出已经学过的相同类型配方
                    	boolean canLearn = true;
                    	for(int j = 0;j< Prescriptions.length;j++){
                    		if(Prescriptions[j].getId() == recipeId){
                    			canLearn = false;
                    			sendMessage(player.getId(), "你已经学过此配方了！");
                    			needRemove = false;
                    		}
                    	}
                    	if(canLearn){
                    		player.addPrescription(prescription);
                    		playerService.savePlayer(player);
                    		needRemove = true;
                    	}
                    	ok = true;
                    } else if (effects[i].getType() == 66) {	// 宠物悟性经验增加
                    	AddPerceptionPointEffect perceptionPoint = (AddPerceptionPointEffect)effects[i];
                        Pet pet = player.getPet();
                        if (pet != null) {
                        	int lastPerceptionLevel = pet.getPerceptionLevel();
                            if (lastPerceptionLevel > Utils.PET_MAX_PERCEPTION_LEVEL) {
                            	sendMessage(player.getId(), "您宠物的悟性已经满级了哦，不能再使用了。");
	                        	needRemove = false;
	                            ok = true;
                            } else {
                            	int point = perceptionPoint.getValue();
                                if (player.addPetPerceptionPoint(pet.getId(), point, changed)) {
                                	player.setPetSkillAndEnhanceName(pet.getId(), lastPerceptionLevel, changed);
                                	needRemove = true;
                                	int noticLevel = Utils.judgePerceptionSendNotice(pet.getPerceptionLevel(), lastPerceptionLevel);
                                	if (noticLevel > 0) {
                                		chatService.sendWorldMessage(player.getId(), "系统", "恭喜“" + player.getPlayerName()
                                				+ "”将宠物“<c6A5ACD>" + pet.getName() + "</c>”的悟性提升到了"
                                				+ noticLevel + "星！" + Utils.petIdToProtocol(pet));
                                	}
                                }
                                ok = true;
                            }
                        } else {
                        	sendMessage(player.getId(), "请装备上宠物后再使用此功能。");
                        	needRemove = false;
                            ok = true;
                        }
                    } else if (effects[i].getType() == 67) { // 宠物灵性提升
                    	Pet pet = player.getPet();
                    	if (pet != null) {
                    		if (pet.getSpiritualityLevel() > Utils.PET_MAX_SPIRITUALITY_LEVEL) {
                    			sendMessage(player.getId(), "您宠物的灵性已经满级了哦，不能在使用了。");
                    		} else {
                    			int spiritualityLevel = pet.getSpiritualityLevel();
                    			int rate = Utils.PET_SPIRITUALITY_UP_SUCCESS_RATE[spiritualityLevel];
                    			if (Utils.hit(rate, 100)) {
                    				pet.setSpiritualityLevel(pet.getSpiritualityLevel() + 1);
                    				changed.addPetProperty(pet, Changed.PET_SPIRITUALITY_LEVEL, 1);
                    				sendMessage(player.getId(), "使用成功，您宠物的灵性增加了");
                    				int noticLevel = Utils.judgeSpiritualitySendNotice(pet.getPerceptionLevel(), spiritualityLevel);
                    				if (noticLevel > 0) {
            	        				chatService.sendWorldMessage(-1, "系统", "恭喜“" + player.getPlayerName()
            	        						+ "”将宠物“<c6A5ACD>" + pet.getName() + "</c>”的灵性提升到了"
            	        						+ noticLevel + "星！");
            	        			}
                    			} else {
                    				if (Utils.getPetSpiritualityLimit(pet.getSpiritualityLevel())) {
                    					sendMessage(player.getId(), "使用失败，您宠物的灵性没有任何改变。");
                    				} else {
                    					pet.setSpiritualityLevel(pet.getSpiritualityLevel() - 1);
                    					changed.addPetProperty(pet, Changed.PET_SPIRITUALITY_LEVEL, -1);
                    					sendMessage(player.getId(), "使用失败，您宠物的灵性减少了。");
                    				}
                    			}
                    			needRemove = true;
                    			ok = true;
                    		}
                    	} else {
                    		sendMessage(player.getId(), "请装备上宠物后再使用此功能。");
                    		needRemove = false;
                            ok = true;
                    	}
                    } else if (effects[i].getType() == 68) {	//  赠送他人物品自动使用
                    	GiftItemAutoUseEffect effect = (GiftItemAutoUseEffect)effects[i];
                    	int usetype = effect.getUsetype();
                    	switch(usetype){
                        	case GiftItemAutoUseEffect.USETYPE_MARRIAGE:{//夫妻关系
                        		int mateid = mateService.getMateId(player);//配偶playerid
                        		if (mateid == -1){
                        			sendMessage(player.getId(), "还没结婚吗？赶紧在这寒冷的季节里找个爱人吧。");
                        			needRemove = false;
                        		}else{
                        			IItem giftitem = Items.getTemplate(effect.getItemid()).newInstance();
                        			Effect[] effects_item = ((IEffectItem) giftitem).getEffects();
                        			for (int effects_i = 0; effects_i < effects_item.length; effects_i++) {
                        				if (effects_item[effects_i].getType() == effect.getParamtype()){//与配置的Paramtype相符
                        					TreasureEffect effect_item = (TreasureEffect) effects_item[effects_i];
                                            try {
                                            	//同上面的藏宝图处理，只是目标为玩家配偶
                                            	Treasure treasure = null;
                                            	if (effect_item.getShovelId() != -1){//指定挖宝铲的宝藏
                                            		treasure = treasureService.getTreasure_bykey(mateid,effect_item.getShovelId());                        		
                                            	}else{
                                            		treasure = treasureService.getTreasure(mateid);                                
                                            	}
                                            	if (treasure != null) {
                                                    Scene scene = stageService.getScene(treasure.getMapId());
                                                	byte[] bytes = stageService.getTaskBytes((short) 31002,
                                                			new String[] {"您爱人已经有宝藏了，它在" + scene.getName() + "。您是否要替换当前宝藏?替换后原有宝藏会消失。\n1.是\n2.否",
                                                            "treasure " + GiftItemAutoUseEffect.USETYPE_MARRIAGE + " " + item.getItemId() + " "});
                                                    UWAPSegment seg = new UWAPSegment(ClientConstants.
                                                            GET_FILE_OK);
                                                    seg.writeShort((short) 31002);
                                                    seg.writeShort((short) 2);
                                                    seg.write(bytes);
                                                    connectService.writeTo(seg, player.getId());
                                                    needRemove = false;
                                                    ok = true;
                                                } else {
                    	                            treasureService.createTreasure(mateid,
                    	                            		effect_item.getMapId(), effect_item.getMinX(), effect_item.getMaxX(),
                    	                            		effect_item.getMinY(), effect_item.getMaxY(), effect_item.getItemGroupId(), effect_item.getShovelId());
                    	                            Scene scene = stageService.getScene(effect_item.getMapId());
                    	                            sendMessage(player.getId(), "已经将宝藏偷偷埋在" + scene.getName() + "了。并发了一封精灵速递给他(她)哦!");
                    	                            //发信
                    	                            WorldPlayer mateplayer = playerService.getWorldPlayerAndCatch(mateid);
                    	                            try {
//														WorldPlayer mateplayer = playerService.loadWorldPlayer(mateid);
														mailService.sendMail(mateid, mateplayer.getPlayerName(), -1, "系统","感恩节的温暖", "你是风儿我是沙，我来藏宝你去挖！" +
                                                        		"您的爱人"+player.getPlayerName() +"使用“夫妻感恩魔盒”为您在" + scene.getName() + "埋下了一个充满爱的感恩宝藏，赶快拿着“夫妻感恩挖宝铲”去开启吧！" +
                                                        		"（夫妻感恩挖宝铲可以在夫妻感恩使者处领取，也可以在"+Server.iMoneyStoreString+"购买。）", null, 0, true);
													} catch (Exception e) {
														sendMessage(player.getId(), "精灵速递忙碌中，发信失败了，麻烦您亲自告诉他（她）吧。。。在" + scene.getName() + "哦。");
	                    	                            
													}
                    	                            playerService.releasePlayer(mateplayer);
													//发私聊
													mateplayer = playerService.getWorldPlayer(mateid);
													if (mateplayer != null){
														sendMessage(mateid, "您的爱人"+player.getPlayerName() +"使用“夫妻感恩魔盒”为您在" + scene.getName() + "埋下了一个充满爱的感恩宝藏，赶快拿着“夫妻感恩挖宝铲”去开启吧！");
													}
													//自己获得物品
													if(effect.getAddgroupid() > 0){
														DropGroup group = DropGroups.getDropGroup(effect.getAddgroupid(),player.getLevel());
								                        if(group != null){
									                        int rate = rnd.nextInt(group.getRate());
									                        DropItem dropItem = group.calcDropItem(rate);
									                        int count = getCount(rnd, dropItem.getMin(), dropItem.getMax());
									                        IItem di = dropItem.getItem().newInstance();
									                        if(player.completeAddItem(di,count,changed, player.getClientDataVersion())==null){
								                        		byte[] att = ItemUtils.item2dbAttachment(di, count);
								                                mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", di.getName(), "", att, 0, true);
								                                sendMessage(player.getId(),"你的背包满了，已经把物品邮寄到你的邮箱!");
									                        }
								                        }
													}
													
							                        
                    	                            ok = true;
                    	                            needRemove = true;
                                                }
                                            } catch (TreasureException ex) {
                                                Treasure treasure = treasureService.getTreasure(mateid);
                                                if (treasure != null) {
                                                    Scene scene = stageService.getScene(treasure.getMapId());
                                                    sendMessage(player.getId(), "您爱人已经有宝藏了，它在" + scene.getName() + "。快去寻找吧!");
                                                }
                                                ok = true;
                                                needRemove = false;
                                            }
                        				}
                        			}
                        			
                        		}
                        	}
                        	break;
                        }
                    } else if (effects[i].getType() == 69) {	// 打造熟练度增长
                    	AddBuildProficiencyEffect buildProficiencyEffect = (AddBuildProficiencyEffect) effects[i];
                    	int oldPointSkill = player.getSkillPoint2();
                    	if (oldPointSkill >= Utils.MAX_BUILD_PROFICIENCY) {
                    		sendMessage(player.getId(), "您的打造熟练度已经达到上限，无法再使用此类物品了。");
                    		needRemove = false;
                            ok = true;
                    	} else {
                    		int newSkillPoint = oldPointSkill + buildProficiencyEffect.getValue();
                    		if (newSkillPoint > Utils.MAX_BUILD_PROFICIENCY) {
                    			newSkillPoint = Utils.MAX_BUILD_PROFICIENCY;
                    		}
                    		player.setSkillPoint2(newSkillPoint);
                    		changed.addProperty(changed.BUILD_PROFICIENCY, newSkillPoint - oldPointSkill);
                    		needRemove = true;
                    		ok = true;
                    	}
                    } else if (effects[i].getType() == 70) {	// 增加角色的活力值
                    	AddLife life = (AddLife)effects[i];
                    	int lifeValue = player.getAllLife();
                    	if(lifeValue >= UnlineExpConfig.LIFEVALUE_MAX){
                    		sendMessage(player.getId(), "您的活力值已经达到上限，无法再使用此类物品了。");
                    		needRemove = false;
                    	}else{
                    		int addValue = life.getValue();
                    		if(lifeValue + addValue > UnlineExpConfig.LIFEVALUE_MAX){
                    			addValue = UnlineExpConfig.LIFEVALUE_MAX - lifeValue;
                    		}
                    		player.setLifeValue(player.getLifeValue() + addValue);
                    		needRemove = true;
                    	}
                    	ok = true;
                    }else if (effects[i].getType() == 71){		//情人节物品使用
                    	FriendGift friendGift = (FriendGift)effects[i];
                    	if(friendGift.getSex() != player.getSex()){
                    		sendMessage(player.getId(), "该物品只限" + (friendGift.getSex() == 1 ? "女性" : "男性") + "使用。");
                    		needRemove = false;
                    		ok = true;
                    	}else{
                    		Friend friends[] = player.getFriends();
                    		ArrayList<Friend> findFriends = new ArrayList<Friend>();
                    		for(Friend friend : friends){
                    			WorldPlayer p = playerService.getWorldPlayer(friend.getId());
                    			if(p != null && p.online() && p.getSex() != friendGift.getSex()){
                    				findFriends.add(friend);
                    			}
                    		}
                    		if(findFriends.size() == 0){
                    			sendMessage(player.getId(), "现在没有在线的异性好友。");
                    		}else{
	                    		UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL);
	                    		seg.writeShort(ClientConstants.EXTEND_SELECTFRIEND);
	                    		seg.writeInt(item.getItemId());
	                    		seg.writeInt(findFriends.size());
	                    		for(Friend friend : findFriends){
	                    			seg.writeInt(friend.getId());
	                    			seg.writeString(friend.getName());
	                    			seg.writeInt(friend.getFavorite());
	                    		}
	                    		seg.writeInt(ClientConstants.EXTEND_SELECTFRIEND);
	                    		connectService.writeTo(seg, player.getId());
                    		}
                    		needRemove = false;
                    		ok = true;
                    	}
                    }else if (effects[i].getType() == 72){		//给角色加券
                    	AddIMoneyQuan addimoneyquan = (AddIMoneyQuan)effects[i];
                    	if(addimoneyquan.getServer() != Server.iMoneyType){
//                    		sendMessage(player.getId(), "该功能暂不开放。");
                    		needRemove = false;
		                	ok = false;
                    	}else{
	                    	int imoney = addimoneyquan.getValue();
	                    	if(Server.iMoneyType == Server.IMONEY_TYPE_CMCC){
	                    		//不开放
	                    		imoney = 0;
	                    		sendMessage(player.getId(), "该功能暂不开放。");
	                    		needRemove = false;
			                	ok = true;
	                    	}
		                	if(0 < imoney){
		                		Friends friendstmp = friendsService.getFriends(player.getId());
		        				if (friendstmp != null){
		        					friendsService.addfriendimoney(player.getId(), imoney);
		        				}else{
		        					friendstmp = new Friends();
		        					friendstmp.setFriendplayerid(-1);
		        					friendstmp.setImoney(imoney);
		        					friendstmp.setLevel(player.getLevel());
		        					friendstmp.setPlayerid(player.getId());
		        					friendstmp.setPlayername(player.getPlayerName());
		        					friendstmp.setValid((byte) 0);
		        					try{
		        						friendsService.addfriendbyfriend(friendstmp);
		        					}catch(Exception e){
		        						log.info(e);
		        					}
		        				}
	        					sendMessage(player.getId(), "您获得了" + imoney + "点消费券");
		        				log.info("ID[" + player.getId() +"] add iMoneyQuan[" +
		        						imoney + "] useItemID[" + item.getId() + "]");
		        				needRemove = true;
			                	ok = true;
		                	}
                    	}
                    }else if(effects[i].getType() == 73){ //新年爆竹
                    	AddItemAnimate itemAnimate=(AddItemAnimate)effects[i];
                    	positionService.itemAnimate(player, player.getMap(),itemAnimate.getIndex(),itemAnimate.getLifeCycle());
                    	if(itemAnimate.getIndex() == 0){
                    		String playerName=player.getPlayerName();
                    		if(playerName!=null){
                    			chatService.sendWorldMessage( -1, "系统", playerName+"燃放了一个新年爆竹，祝福大家新春快乐！");
                    		}
                    	}else{
                    		chatService.sendPrivateMessage(-1, "系统",player.getId(), "哈哈，逗你玩呢，愚人节快乐。");
                    	}
                    	needRemove = true;
                    	ok = true;
                    } else if (effects[i].getType() == 74) {
                    	SecondGenerationPetEffect effect = (SecondGenerationPetEffect) effects[i];
                    	int type = rnd.nextInt(6) + 1;
                    	int[] PetProperties = Utils.getSecondGenerationPetProperties(type, effect.getMainPerceptionLevel(), effect.getSecondPerceptionLevel());
                        Pet pet = new Pet();
                        pet.setId(IDGenerator.getPetId());
                        pet.setPetType(type);
                        pet.setLevel(1);
                        pet.setItemId(101);
                        pet.setFavor(50);
                        pet.setNextExp(Utils.getPetUpLevelExp(pet.getLevel()));
                        pet.setBinded(true);
                        pet.setBindType((byte)1);
                        pet.setName(Utils.PET_NAME[rnd.nextInt(Utils.PET_NAME.length)]);
                        pet.setIntelligence(PetProperties[0]);
                        pet.setVitality(PetProperties[1]);
                        pet.setAgility(PetProperties[2]);
                        pet.setStrength(PetProperties[3]);
                        pet.setHp(pet.getMaxHp());
                        pet.setMp(pet.getMaxMp());
        	    		
                        Ability[] abs = Utils.getPetAbilities(pet.
                                getPetType());
                        for (int j = 0; j < abs.length; j++) {
                            pet.addAbility(abs[j]);
                        }
                        int lastPetPerceptionLevel = pet.getPerceptionLevel();
                        if(effect.getSetPerceptionLevel() > 0){
                        	pet.addPetPerceptionPoint(Utils.getPetUpLevelPerceptionAllPoint(effect.getSetPerceptionLevel()));
                        	pet.setPetSkillAndEnhanceName(pet.getId(), lastPetPerceptionLevel, null);
                        	pet.setHp(pet.getMaxHp());
                        	pet.setMp(pet.getMaxMp());
                        }
                        if (player.addPet(pet, changed) == 1) {
                        	petId = pet.getId();
                            needRemove = true;
                            ok = true;
                        } else {
                            needRemove = false;
                            ok = true;
                            sendMessage(player.getId(), "宠物栏已经满了。");
                        }
                    }else if (effects[i].getType() == 75) {	//属性攻属性改变
                    	PlayerVianyEffect effect = (PlayerVianyEffect) effects[i];
                    	ok = true;
                    	if(player.getVianyType() == effect.getVianyType()){
                    		needRemove = false;
                    		sendMessage(player.getId(), "您的当前属性与修改的属性相同。");
                    	}else{
                    		player.setVianyType(effect.getVianyType());
                    		if(changed != null){
                    			changed.addProperty(Changed.VIANY_TYPE, effect.getVianyType());
                    		}
                    		needRemove = true;
                    		sendMessage(player.getId(), "您的属性已经更改为" + Viany.getName((byte)player.getVianyType()) + "属性");
                    	}
                    }else if(effects[i].getType() == 76){//表情称号
                    	PhizTitleEffect effect =(PhizTitleEffect)effects[i];
                    	player.setPhizTitleIndex(effect.getPhizIndex());
                    	if(changed != null){
                    		changed.addProperty(Changed.PHIZTITLE, effect.getPhizIndex());
                    	}
                    	if(!player.hasPhizTitle(effect.getPhizIndex())){
                    		player.addPhizTitle(effect.getPhizIndex());
                    	}
                    	ok = true;
                    	needRemove = true;
                    	if(player.getClientDataVersion() > 5){
                    		sendMessage(player.getId(), "您的表情称号已经更改为" + effect.getPhizName());
                    	}else{
                    		sendMessage(player.getId(), "您的表情称号已经更改为" + effect.getPhizName() +"，请登录官网下载新客户端才能看到。");
                    	}
                    	phizService.addChangePhiz(player, effect.getPhizType(), effect.getPhizIndex());
                    }else if(effects[i].getType() == 77){//统御值
                    	LeadershipEffect effect =(LeadershipEffect)effects[i];
                    	int value = player.getLeaderShip();
                    	int addValue = effect.getValue();
                    	ok = true;
                    	if(player.getLeaderShip() >= PlayerData.LEADERSHIP_MAX){
                    		needRemove = false;
                    		sendMessage(player.getId(), "您的统御值已满。");
                    	}else{
	                    	if(player.getLeaderShip() + addValue > PlayerData.LEADERSHIP_MAX){
	                    		addValue = PlayerData.LEADERSHIP_MAX - player.getLeaderShip();
	                    	}
	                    	player.setLeaderShip(player.getLeaderShip() + addValue);
	                    	if(changed != null){
	                    		changed.addProperty(Changed.LEADERSHIP, addValue);
	                    	}else{
	                    		sendMessage(player.getId(), "您的统御值已经加上" + addValue + "点。");
	                    	}
	                    	needRemove = true;
                    	}
                    }else if(effects[i].getType() == 78){	//装备
                    	EquModleEffect effect =(EquModleEffect)effects[i];
                    	int id = effect.getId();
                    	int itemid = effect.getEquid();
                    	IItemTemplate template = Items.getTemplate(itemid);
                    	ok = true;
                    	if(template != null){
                    		IItem iitem = template.newInstance();
                    		IEquipment tmpEqu = (IEquipment)iitem;
                    		tmpEqu.setDataVersion(5);
                    		EquModleData ed = EquModleConfig.getEquModle(id);
                    		if(ed != null){
                    			tmpEqu.setBinded(true);
                    			tmpEqu.setDiamond((byte)ed.getDiamondcount());
                    			Viany viany = tmpEqu.getViany();
                    			viany.setViany(Viany.STONE, ed.getVianystone());
                    			viany.setViany(Viany.SCISSORS, ed.getVianyscissors());
                    			viany.setViany(Viany.PAPER, ed.getVianypaper());
//                    			tmpEqu.setDiamondcount(ed.getOpenDiamondCount());
                    			tmpEqu.setDiamondMosiacRoleInfo(ed.getDiamodMosiacRoleInfo());
                    			for(int d=0; d<5; d++){
                    				int stoneid = ed.getDiamondStoneId(d);
                    				if(stoneid == 0){
                    					break;
                    				}
                    				DiamondMosaic diamondMosaic = DiamondMosaic.getDiamondMosaicMap().get(stoneid);
                    				tmpEqu.diamondMosaic((byte)d, diamondMosaic);
                    			}
                    			int[] enchances = ed.getEnchances();
                    			int count = enchances.length;
                    			for(int c=0; c<count; c+=2){
                    				Enhance enhance = Enhance.getEnhance(enchances[c], tmpEqu.getLevel());
                    				if(enhance != null){
                    					tmpEqu.enhance(enhance);
                    				}
                    			}
                    			if(count > 0){
                    				Utils.resetEnhanceStatus(tmpEqu, true);
                    			}
                    		}
                    		if (player.completeAddItem(tmpEqu, 1, changed, player.getClientDataVersion()) == null){
                            	byte[] att = ItemUtils.item2dbAttachment(tmpEqu, 1);
                                mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
                                                    tmpEqu.getName(), "", att, 0, true);
                                sendMessage(player.getId(), "你的背包满了，已经把物品邮寄到你的邮箱!");
                            }
                    		needRemove = true;
                    	}else{
                    		sendMessage(player.getId(), "没有对应的装备。");
                    	}
                    }else if(effects[i].getType() == 79){	//升级
                    	UpLevelEffect effect =(UpLevelEffect)effects[i];
                    	int level = effect.getLevel();
                    	ok = true;
                    	if(player.getLevel() >= level){
                    		int itemid = effect.getItemid();
                    		IItemTemplate template = Items.getTemplate(itemid);
                    		IItem iitem = template.newInstance();
                    		if (player.completeAddItem(iitem, effect.getCount(), changed, player.getClientDataVersion()) == null){
                            	byte[] att = ItemUtils.item2dbAttachment(iitem, effect.getCount());
                                mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
                                		iitem.getName(), "", att, 0, true);
                                sendMessage(player.getId(), "你的背包满了，已经把物品邮寄到你的邮箱!");
                            }
                    	}else{
                    		int exp = Utils.getUpLevelExp(player.getLevel(), level);
                    		player.addExp(exp, changed);
                    	}
                    	needRemove = true;
                    }else if(effects[i].getType() == 80){	//增加灵性悟性等级
                    	PetSetupEffect effect =(PetSetupEffect)effects[i];
                    	Pet pet = player.getPet();
                    	ok = true;
                    	if(pet == null){
                    		needRemove = false;
                    		sendMessage(player.getId(), "请携带宠物!");
                    	}else{
                    		needRemove = true;
                    		int spiritualityLevel = pet.getSpiritualityLevel();
                    		int newSpiritualityLevel = effect.getSpiritualLevel();
                    		pet.setSpiritualityLevel(effect.getSpiritualLevel());
                    		if(changed != null){
                    			changed.addPetProperty(pet, Changed.PET_SPIRITUALITY_LEVEL, newSpiritualityLevel - spiritualityLevel);
                    		}
                    		if(pet.getPerceptionLevel() < effect.getPerceptionLevel()){
                    			int perceptionLevel = pet.getPerceptionLevel();
                    			pet.setPerceptionLevel(effect.getPerceptionLevel());
                            	player.setPetSkillAndEnhanceName(pet.getId(), perceptionLevel, changed);
                            	changed.addPetProperty(pet, Changed.PET_PERCETPION_LEVEL, pet.getPerceptionLevel() - perceptionLevel);
                    		}
                    	}
                    }else if(effects[i].getType() == 81){	//幸运沙漏
                    	ok = true;
                    	LuckTimeEffect effect =(LuckTimeEffect)effects[i];
                    	long addlucktime = effect.getLuckTime() * 60 * 1000L;
                    	long now = System.currentTimeMillis();
                    	if(player.getLuckTime() > now){
                    		player.setLuckTime(player.getLuckTime() + addlucktime);
                    		sendMessage(player.getId(), "您的幸运时间已经增加" + effect.getLuckTime() + "分钟。");
                    	}else{
                    		player.setLuckTime(now + addlucktime);
                    		sendMessage(player.getId(), "您的幸运时间将持续" + effect.getLuckTime() + "分钟。");
                    	}
                    	needRemove = true;
                    }else if (effects[i].getType() == 82){		//七夕情人节物品使用
                		Friend friends[] = player.getFriends();
                		ArrayList<Friend> findFriends = new ArrayList<Friend>();
                		for(Friend friend : friends){
                			WorldPlayer p = playerService.getWorldPlayer(friend.getId());
                			if(p != null && p.online()){
                				findFriends.add(friend);
                			}
                		}
                		if(findFriends.size() == 0){
                			sendMessage(player.getId(), "现在没有在线的好友。");
                		}else{
                    		UWAPSegment seg = new UWAPSegment(ClientConstants.EXTEND_PROTOCOL);
                    		seg.writeShort(ClientConstants.EXTEND_SELECTFRIEND);
                    		seg.writeInt(item.getItemId());
                    		seg.writeInt(findFriends.size());
                    		for(Friend friend : findFriends){
                    			seg.writeInt(friend.getId());
                    			seg.writeString(friend.getName());
                    			seg.writeInt(friend.getFavorite());
                    		}
                    		seg.writeInt(ClientConstants.EXTEND_LOVESEND);
                    		connectService.writeTo(seg, player.getId());
                		}
                		needRemove = false;
                		ok = true;
                    }else if (effects[i].getType() == 83){		//公会荣誉和贡献值
                    	ok = true;
                    	if(player.getTongId() >= 0){
	                    	TongValueEffect effect =(TongValueEffect)effects[i];
	                    	TongData tongData = tongService.getTongData(player.getTongId());
	                    	if(tongData != null){
		                    	if(effect.getTongCredit() > 0){
		                    		tongData.addCredit(effect.getTongCredit());
		                    		if(changed != null){
		                    			changed.addProperty(Changed.TONGCREIDT, effect.getTongCredit());
		                    		}
		                    	}
		                    	if(effect.getContribution() > 0){
		                    		player.setContribution(player.getContribution() + effect.getContribution());
		                    		if(changed != null){
		                    			changed.addProperty(Changed.CONTRIBUTION, effect.getContribution());
		                    		}
		                    		tongData.modifyPlayer(player);
		                    	}
		                    	tongService.saveTongData(tongData);
		                    	needRemove = true;
		                    	player.getTongDuty();
	                    	}else{
	                    		needRemove = false;
	                    		sendMessage(player.getId(), "您还没有公会。");
	                    	}
                    	}else{
                    		needRemove = false;
                    		sendMessage(player.getId(), "您还没有公会。");
                    	}
                    }else if (effects[i].getType() == 84){		//宠物变色
                    	//功能有待开发
                    	ok = true;
                    	needRemove = false;
                    }else if(effects[i].getType() == 85){	//种子
                    	SeedEffect effect = (SeedEffect)effects[i];
                    	sendMessage(player.getId(), "该物品只能在庄园里使用。");
                    	ok = true;
                    	needRemove = false;
                    }else if(effects[i].getType() == 86){ //永久增加属性
                    	AddAttributeEffect effect = (AddAttributeEffect)effects[i];
                    	int str = effect.getStrength();
                    	int agi = effect.getAgility();
                    	int vit = effect.getVitality();
                    	int inte = effect.getIntelligence();
                    	
                    	String attrName = null;
                    	if(player.getStrength() - player.getLevel() + str > player.getRealStrength() * 10 / 100){
                    		attrName = "力量";
                    	}
                    	if(player.getAgility() - player.getLevel() + agi > player.getRealAgility() * 10 / 100){
                    		if(attrName == null){
                    			attrName = "敏捷";
                    		}else{
                    			attrName += "，敏捷";
                    		}
                    	}else if(player.getVitality() - player.getLevel() + vit > player.getRealVitality() * 10 / 100){
                    		attrName = "体力";
                    	}else if(player.getIntelligence() - player.getLevel() + inte > player.getRealIntelligence() * 10 / 100){
                    		attrName = "智力";
                    	}
                    	if(attrName != null){
                    		ok = true;
                    		needRemove = false;
                    		sendMessage(player.getId(), "你使用属性符所增加的" + attrName + "属性已经超过人物" + attrName + "属性总和的10%，提升一定人物" + attrName + "属性后才能再次使用。");
                    	}else{
	                    	log.info("ID[" + player.getId() +"] playerAttr[" +player.getStrength() + "," 
	                    			+ player.getAgility()+ "," +player.getVitality()+ "," + player.getIntelligence() + "] useItemID[" + item.getId() + "] AddAttribute TRY");
	                    	player.setStrength(player.getStrength() + str);
	                    	player.setAgility(player.getAgility() + agi);
	                    	player.setVitality(player.getVitality() + vit);
	                        player.setIntelligence(player.getIntelligence() + inte);
	                        player.adjustProperty();
	                        
	                        player.setAddAttributes(str, player.ADDATTR_STRENGTH, false,changed);
	                        player.setAddAttributes(agi, player.ADDATTR_AGILITY, false,changed);
	                        player.setAddAttributes(vit, player.ADDATTR_VITALITY, false,changed);
	                        player.setAddAttributes(inte, player.ADDATTR_INTELLIGENCE, false,changed);
	                        
	                        changed.setProperty(Changed.HP,player.getHp());
	                        changed.setProperty(Changed.MP,player.getMp());
	                        log.info("ID[" + player.getId() +"] playerAttr[" +player.getStrength() + "," 
	                    			+ player.getAgility()+ "," +player.getVitality()+ "," + player.getIntelligence() + "] useItemID[" + item.getId() + "] AddAttribute SUCCESS");
	                        playerService.checkPlayer(player);
	                        ok = true;
	                    	needRemove = true;
                    	}
                    }else if(effects[i].getType() == 87){	//随缘物语
                    	//LetItBeEffect effect = (LetItBeEffect)effects[i];
                    	ConcurrentHashMap<Integer, WorldPlayer> onllinePlayers = getPlayers();
                    	WorldPlayer[] p = new WorldPlayer[onllinePlayers.size()];
                    	onllinePlayers.values().toArray(p);
                    	ok = true;
                    	if(p != null && p.length > 1){
                    		int index = Utils.getRandom(0, p.length - 1);
                    		WorldPlayer randomplayer = p[index];
                    		if(randomplayer != null){
                    			if(randomplayer.online() == false){
                    				sendMessage(player.getId(), "当前玩家不在线");
                    				needRemove = false;
                    				break;
                    			}
                    			int randomId = randomplayer.getId();
                    			if(randomId == player.getId()){	//随机到自己
                    				index++;
                    				if(index > p.length - 1){
                    					index = 0;
                    				}
                    				randomplayer = p[index];
                    				randomId = randomplayer.getId();
                    				if(randomId == player.getId()){
                    					sendMessage(player.getId(), "现在还不能使用该物品。");
                    					needRemove = false;
                    					break;
                    				}
                    			}
                    			
                    			byte[] bytes = stageService.getTaskBytes((short) 31002,
                    					new String[] {"缘，妙不可言。在这不蛋定的日子里用随缘物语寻找一个朋友来缓解寂寞空虚冷吧？" +
                    					"\n1.确定\n2.取消",
                    					"LetItBe " + randomId,"ok"});
                    			UWAPSegment seg = new UWAPSegment(ClientConstants.
                    					GET_FILE_OK);
                    			seg.writeShort((short) 31002);
                    			seg.writeShort((short) 2);
                    			seg.write(bytes);
                    			connectService.writeTo(seg, player.getId());
                    			needRemove = true;
                    		}else{
                    			needRemove = false;
                    		}
                    	}else{
                    		needRemove = false;
                    	}
                    }else if(effects[i].getType() == 88){	//丘比特之箭
                    	//TheArrowOfLoveEffect effect = (TheArrowOfLoveEffect)effects[i];
                    	ok = true;
                    	needRemove = false;
                    	if(player.getFriends().length == 0){
                    		sendMessage(player.getId(),"您还没有好友,请有了好友再使用吧！");
                    	}else{
                    		Friend[] friends = player.getFriends();
                    		ArrayList<Friend> findFriends = new ArrayList<Friend>();
                    		for(Friend friend : friends){
                    			WorldPlayer p = playerService.getWorldPlayer(friend.getId());
                    			if(p != null && p.online()){
                    				findFriends.add(friend);
                    			}
                    		}
                    		if(findFriends.size() == 0){
                    			sendMessage(player.getId(), "现在没有在线的好友。");
                    		}else{
                    			UWAPSegment seg = new UWAPSegment(ClientConstants.GENERIC_LIST);
                    			seg.writeShort((short) 10233);
                    			seg.writeString("好友列表");
                    			seg.write((byte) 3);
                    			seg.writeShort((short)findFriends.size());
                    			for(Friend friend : findFriends){
                    				seg.writeInt(friend.getId());
                    				seg.writeString(friend.getName());
                    				seg.writeInt(Utils.CLR_WHITE);
                    			}
                    			seg.write((byte) 1);
                    			seg.writeString("向对方使用丘比特之箭");
                    			seg.writeString("TheArrowOfLove");
                    			connectService.writeTo(seg, player.getId());
                    		}
                    	}
                    }else if(effects[i].getType() == 89){	//领袖效果加血buff
                    	CampleaderEffect effect = (CampleaderEffect)effects[i];
                    	ok = true;
                    	needRemove = false;
                    	int time = 3600 * 24 * 7;	//一周
                    	Buf kingbuf = new Buf(bufId.incrementAndGet(),(byte)13,effect.getHpEffect(),time,(byte)1);
                    	kingbuf.setTimestamp(System.currentTimeMillis());
            			player.addBuf(kingbuf, changed);
            			needRemove = true;
                    }else if(effects[i].getType() == 90){//新钱袋效果
                    	MoneyEffect effect = (MoneyEffect)effects[i];
                    	int min = effect.getminMoney();
                    	int max = effect.getmaxMoney();
                    	int addMoney = Utils.getRandom(min, max);
                    	player.setMoeny(player.getMoeny() + addMoney);
                        changed.addProperty(Changed.MONEY, addMoney);
                    	needRemove = true;
                        ok = true;
                    }else if(effects[i].getType() == 91){//NBShow
                    	byte[] bytes = stageService.getTaskBytes((short) 31056,
            					new String[] {"NBShow", "请输入掉落组ID和次数,空格格开"});
            			UWAPSegment seg = new UWAPSegment(ClientConstants.
            					GET_FILE_OK);
            			seg.writeShort((short) 31056);
            			seg.writeShort((short) 2);
            			seg.write(bytes);
            			connectService.writeTo(seg, player.getId());
                    	needRemove = false;
                        ok = true;
                    }else if(effects[i].getType() == 92){//开学消费大礼包
                    	//SchoolGiftBagEffect effect = (SchoolGiftBagEffect)effects[i];
                    	Changed ch = new Changed();
                    	if(player.isFull() || player.getItemCount(201453) + 1 > 99
                    	|| player.getItemCount(201517) + 10 > 99 || player.getItemCount(201542) + 1 > 99){
                    		//邮件发3种物品
                    		IItemTemplate tmpitem = Items.getTemplate(201453);
                    		byte[] att = ItemUtils.item2dbAttachment(tmpitem.newInstance(), 1);
		    				mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", tmpitem.getName() + "*" + 1, "礼包物品", att, 0, true);
                    		
		    				IItemTemplate tmpitem2 = Items.getTemplate(201517);
		    				att = ItemUtils.item2dbAttachment(tmpitem2.newInstance(), 10);
		    				mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", tmpitem2.getName() + "*" + 10, "礼包物品", att, 0, true);
		    				
		    				IItemTemplate tmpitem3 = Items.getTemplate(201542);
		    				att = ItemUtils.item2dbAttachment(tmpitem3.newInstance(), 1);
		    				mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", tmpitem3.getName() + "*" + 1, "礼包物品", att, 0, true);
		    				sendMessage(player.getId(), "背包已满或物品已达上限,物品已发到邮箱");
		    				needRemove = true;
	                        ok = true;
		    				break;
                    	}
                    	//宠物金色变色符
                    	player.addItem(201453, 1, ch, player.getClientDataVersion());
                    	//地狱套装随机包
                    	player.addItem(201517, 10, ch, player.getClientDataVersion());
                    	//紫罗兰的星空钻戒定向包
                    	player.addItem(201542, 1, ch, player.getClientDataVersion());
                    	connectService.sendGetItem(ch, player.getId(), (byte)0);
                    	log.info("Gift bag ID[" + player.getId() + "] giveitemid[" + 201453 + "] " +
                    			"giveitemid[" + 201517 +"] giveitemid[" + 201542 + "]");
                    	needRemove = true;
                        ok = true;
                    }else if(effects[i].getType() == 93){//99朵蓝色妖姬
                    	//BlueFlowerEffect effect = (BlueFlowerEffect)effects[i];
                    	Changed cd = new Changed();
                    	if(player.isFull() || player.getItemCount(200335) + 99 > 99){
                    		IItemTemplate tmpitem = Items.getTemplate(200335);
                    		byte[] att = ItemUtils.item2dbAttachment(tmpitem.newInstance(), 99);
		    				mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", tmpitem.getName() + "*" + 99, "礼包物品", att, 0, true);
		    				sendMessage(player.getId(), "背包已满或物品已达上限,物品已发到邮箱");
		    				needRemove = true;
	                        ok = true;
		    				break;
                    	}
                    	player.addItem(200335, 99, cd, player.getClientDataVersion());
                    	connectService.sendGetItem(cd, player.getId(), (byte)0);
                    	log.info("BlueFlower bag ID[" + player.getId() + "] giveitemid[" + 200335 + "]");
                    	needRemove = true;
                        ok = true;
                    }else if(effects[i].getType() == 94){//10个高级打孔符
                    	//HighHoleEffect effect = (HighHoleEffect)effects[i];
                    	Changed c = new Changed();
                    	if(player.isFull() || player.getItemCount(200752) + 10 > 99){
                    		IItemTemplate tmpitem = Items.getTemplate(200752);
                    		byte[] att = ItemUtils.item2dbAttachment(tmpitem.newInstance(), 10);
		    				mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", tmpitem.getName() + "*" + 10, "礼包物品", att, 0, true);
		    				sendMessage(player.getId(), "背包已满或物品已达上限,物品已发到邮箱");
		    				needRemove = true;
	                        ok = true;
		    				break;
                    	}
                    	player.addItem(200752, 10, c, player.getClientDataVersion());
                    	connectService.sendGetItem(c, player.getId(), (byte)0);
                    	log.info("HighHoles bag ID[" + player.getId() + "] giveitemid[" + 200752 + "]");
                    	needRemove = true;
                        ok = true;
                    }else if(effects[i].getType() == 95){//5瓶圣水
                    	//GodWaterEffect effect = (GodWaterEffect)effects[i];
                    	Changed c = new Changed();
                    	if(player.isFull() || player.getItemCount(201521) + 5 > 99){
                    		IItemTemplate tmpitem = Items.getTemplate(201521);
                    		byte[] att = ItemUtils.item2dbAttachment(tmpitem.newInstance(), 5);
		    				mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", tmpitem.getName() + "*" + 5, "礼包物品", att, 0, true);
		    				sendMessage(player.getId(), "背包已满或物品已达上限,物品已发到邮箱");
		    				needRemove = true;
	                        ok = true;
		    				break;
                    	}
                    	player.addItem(201521, 5, c, player.getClientDataVersion());
                    	connectService.sendGetItem(c, player.getId(), (byte)0);
                    	log.info("GodWater bag ID[" + player.getId() + "] giveitemid[" + 201521 + "]");
                    	needRemove = true;
                        ok = true;
                    }else if(effects[i].getType() == 96){//白色情人节大礼包
                    	//WhiteLoverBagEffect effect = (WhiteLoverBagEffect)effects[i];
                    	Changed ch = new Changed();
                    	if(player.isFull() || player.getItemCount(200883) + 10 > 99
                            	|| player.getItemCount(211002) + 99 > 99 || player.getItemCount(200885) + 99 > 99){
                    		//邮件发3种物品
                    		IItemTemplate tmpitem = Items.getTemplate(200883);
                    		byte[] att = ItemUtils.item2dbAttachment(tmpitem.newInstance(), 10);
		    				mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", tmpitem.getName() + "*" + 10, "礼包物品", att, 0, true);
                    		
		    				IItemTemplate tmpitem2 = Items.getTemplate(211002);
		    				att = ItemUtils.item2dbAttachment(tmpitem2.newInstance(), 99);
		    				mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", tmpitem2.getName() + "*" + 99, "礼包物品", att, 0, true);
		    				
		    				IItemTemplate tmpitem3 = Items.getTemplate(200885);
		    				att = ItemUtils.item2dbAttachment(tmpitem3.newInstance(), 99);
		    				mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", tmpitem3.getName() + "*" + 99, "礼包物品", att, 0, true);
		    				sendMessage(player.getId(), "背包已满或物品已达上限,物品已发到邮箱");
		    				needRemove = true;
	                        ok = true;
		    				break;
                        }
                    	//4级宝石定向包
                    	player.addItem(200883, 10, ch, player.getClientDataVersion());
                    	//精炼石
                    	player.addItem(211002, 99, ch, player.getClientDataVersion());
                    	//顶级精华定向包
                    	player.addItem(200885, 99, ch, player.getClientDataVersion());
                    	connectService.sendGetItem(ch, player.getId(), (byte)0);
                    	log.info("WhiteLove bag ID[" + player.getId() + "] giveitemid[" + 200883 + "] " +
                    			"giveitemid[" + 211002 +"] giveitemid[" + 200885 + "]");
                    	needRemove = true;
                        ok = true;
                    }else if(effects[i].getType() == 97){//消费大礼包
                    	//LoverImageEffect effect = (LoverImageEffect)effects[i];
                    	Changed ch = new Changed();
                    	if(player.isFull() || player.getItemCount(201456) + 1 > 99 || player.getItemCount(201578) + 1 > 99){
                    		//邮件发2种物品
                    		IItemTemplate tmpitem = Items.getTemplate(201456);
                    		byte[] att = ItemUtils.item2dbAttachment(tmpitem.newInstance(), 1);
		    				mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", tmpitem.getName() + "*" + 1, "礼包物品", att, 0, true);
                    		
		    				IItemTemplate tmpitem2 = Items.getTemplate(201578);
		    				att = ItemUtils.item2dbAttachment(tmpitem2.newInstance(), 1);
		    				mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", tmpitem2.getName() + "*" + 1, "礼包物品", att, 0, true);
		    				
		    				sendMessage(player.getId(), "背包已满或物品已达上限,物品已发到邮箱");
		    				needRemove = true;
	                        ok = true;
		    				break;
                        }
                    	//两种定向包
                    	player.addItem(201456, 1, ch, player.getClientDataVersion());
                    	player.addItem(201578, 1, ch, player.getClientDataVersion());
                    	connectService.sendGetItem(ch, player.getId(), (byte)0);
                    	log.info("ConsumePackage ID[" + player.getId() + "] giveitemid[" + 201456 + "]" +"giveitemid[" + 201578 +"]");
                    	needRemove = true;
                        ok = true;
                    }else if(effects[i].getType() == 98){//聚灵灵力
                    	//SoulEffect effect = (SoulEffect)effects[i];
                    	if(player.getLevel() == 100){
                    		if(item.getItemId() == 201580){//10点
                    			player.setTrainPoint(player.getTrainPoint() + 10);
                    			sendMessage(player.getId(), "您获得了10点灵力");
                    		}else if(item.getItemId() == 201581){//1000点
                    			player.setTrainPoint(player.getTrainPoint() + 1000);
                    			sendMessage(player.getId(), "您获得了1000点灵力");
                    		}
                    		needRemove = true;
                    		ok = true;
                    	}else{
                    		needRemove = false;
                    	}
                    }else if(effects[i].getType() == 99){//超级经验包
                    	//ExpEffect effect = (ExpEffect)effects[i];
                    	if(item.getItemId() == 201583){
                    		int tmpexp = 4000000;
                    		int level_tmp = player.getLevel();
                    		player.addExp(tmpexp, changed);
                    		changed.setProperty(Changed.EXP, player.getExp());
                    		log.info("ExpPlayer ID[" + player.getId() + "] giveExp[" + tmpexp + "]" +"playerExp[" + player.getExp() +"]");
                    		needRemove = true;
                    		ok = true;
                    		if(level_tmp < player.getLevel()){
                    			//推荐人通用函数
                    			playerService.recommendBalance(player, "hopegrass2");
                    			//尝试加到师傅的列表中
                    			playerService.addMasterPlayer(player, changed);
                    			
                    			if(masterService.isPrentice(player)){
                    				try{
                    					Changed changed1 = new Changed(), changed2 = new Changed();
                    					masterService.unRelation(player, changed1, changed2);
                    				}catch(Exception e){
                    				}
                    			}
                    		}
                    	}else{
                    		needRemove = false;
                    	}
                    }else if(effects[i].getType() == 100){//超级宝石大礼包
                    	//SuperGiftBagEffect effect = (SuperGiftBagEffect)effects[i];
                		if(item.getItemId() == 201584){//使用宝石礼包
                			if(player.hasItem(201588)){//有道具
		                		if(player.isFull() || player.getItemCount(201585) + 1 > 99 ){
		                			IItemTemplate tmpitem = Items.getTemplate(201585);
		                			byte[] att = ItemUtils.item2dbAttachment(tmpitem.newInstance(), 1);
		                			mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", tmpitem.getName() + "*" + 1, "礼包物品", att, 0, true);
		                			player.completeRemoveItem(201588, 1, changed);//删除钥匙
		                			chatService.sendWorldMessage(-1, "系统", player.getPlayerName() + "使用了超级宝石大礼包获得了价值150元的4级宝石哦！太给力了！");
		                			sendMessage(player.getId() ,"您的背包已满，奖励物品已经发送到邮箱，请及时查收。");
		                		}else{
		                			player.addItem(201585, 1, changed, player.getClientDataVersion());
		                			player.completeRemoveItem(201588, 1, changed);//删除钥匙
		                			chatService.sendWorldMessage(-1, "系统", player.getPlayerName() + "使用了超级宝石大礼包获得了价值150元的4级宝石哦！太给力了！");
		                		}
		                		needRemove = true;
		                		ok = true;
		                		log.info("ID[" + player.getId() + "] DiamondGiftBag getItemID[" + 201585 +"] success!");
		                	}else{
		                		//sendMessage(player.getId() ,"您还没有开启礼包的钥匙,钥匙可以到商城购买!");
		                		needRemove = false;
		                		ok = true;
		                	}
                		}else if(item.getItemId() == 201585){//使用30级礼包获得4级定向包4个和60级礼包
                			Changed ch = new Changed();
                			if(player.isFull() || player.getItemCount(201586) + 1 > 99 || player.getItemCount(200883) + 4 > 99){
                				IItemTemplate tmpitem = Items.getTemplate(201586);
                				byte[] att = ItemUtils.item2dbAttachment(tmpitem.newInstance(), 1);
                				mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", tmpitem.getName() + "*" + 1, "礼包物品", att, 0, true);
                				
                				tmpitem = Items.getTemplate(200883);
                				att = ItemUtils.item2dbAttachment(tmpitem.newInstance(), 4);
                				mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", tmpitem.getName() + "*" + 4, "礼包物品", att, 0, true);
                				needRemove = true;
                				ok = true;
                				break;
                			}
                			player.addItem(201586, 1, ch, player.getClientDataVersion());
                			player.addItem(200883, 4, ch, player.getClientDataVersion());
                			connectService.sendGetItem(ch, player.getId(), (byte)0);
                			log.info("SuperGiftBag ID[" + player.getId() + "] giveitemid[" + 201586 + "]" +"giveitemid[" + 200883 +"]");
                			needRemove = true;
                			ok = true;
                		}else if(item.getItemId() == 201586){//使用60级礼包获得4个定向包和90级礼包
                			if(player.getLevel() >= 60){
                				Changed ch = new Changed();
                				if(player.isFull() || player.getItemCount(201587) + 1 > 99 || player.getItemCount(200883) + 4 > 99){
                					IItemTemplate tmpitem = Items.getTemplate(201587);
                					byte[] att = ItemUtils.item2dbAttachment(tmpitem.newInstance(), 1);
                					mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", tmpitem.getName() + "*" + 1, "礼包物品", att, 0, true);
                					
                					tmpitem = Items.getTemplate(200883);
                					att = ItemUtils.item2dbAttachment(tmpitem.newInstance(), 4);
                					mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", tmpitem.getName() + "*" + 4, "礼包物品", att, 0, true);
                					needRemove = true;
                					ok = true;
                					break;
                				}
                				player.addItem(201587, 1, ch, player.getClientDataVersion());
                				player.addItem(200883, 4, ch, player.getClientDataVersion());
                				connectService.sendGetItem(ch, player.getId(), (byte)0);
                				log.info("SuperGiftBag ID[" + player.getId() + "] giveitemid[" + 201587 + "]" +"giveitemid[" + 200883 +"]");
                				needRemove = true;
                				ok = true;
                			}else{
                				sendMessage(player.getId(), "需要到60级才能使用哦!");
                				needRemove = false;
                        		ok = true;
                        		break;
                			}
                		}else if(item.getItemId() == 201587){//使用90级礼包获得4个定向包
                			if(player.getLevel() >= 90){
                				Changed ch = new Changed();
                				if(player.isFull() || player.getItemCount(200883) + 4 > 99){
                					IItemTemplate tmpitem = Items.getTemplate(200883);
                					byte [] att = ItemUtils.item2dbAttachment(tmpitem.newInstance(), 4);
                					mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", tmpitem.getName() + "*" + 4, "礼包物品", att, 0, true);
                					needRemove = true;
                					ok = true;
                					break;
                				}
                				player.addItem(200883, 4, ch, player.getClientDataVersion());
                				connectService.sendGetItem(ch, player.getId(), (byte)0);
                				log.info("SuperGiftBag ID[" + player.getId() +"giveitemid[" + 200883 +"]");
                				needRemove = true;
                				ok = true;
                			}else{
                				sendMessage(player.getId(), "需要到90级才能使用哦!");
                				needRemove = false;
                        		ok = true;
                        		break;
                			}
                		}
                    }else if(effects[i].getType() == 101){//16区冲级奖励
                    	//SpurtGiftEffect effect = (SpurtGiftEffect)effects[i];
                    	if(item.getItemId() == 201592){//一等
                    		Changed ch = new Changed();
                    		if(player.isFull() || player.getItemCount(201151) + 1 > 99
                    			|| player.getItemCount(201398) + 5 > 99 
                    			|| player.getItemCount(201517) + 5 > 99
                    			|| player.getItemCount(201577) + 5 > 99){
            					IItemTemplate tmpitem = Items.getTemplate(201151);
            					byte [] att = ItemUtils.item2dbAttachment(tmpitem.newInstance(), 1);
            					mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", tmpitem.getName() + "*" + 1, "礼包物品", att, 0, true);
            					
            					tmpitem = Items.getTemplate(201398);
            					att = ItemUtils.item2dbAttachment(tmpitem.newInstance(), 5);
            					mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", tmpitem.getName() + "*" + 5, "礼包物品", att, 0, true);
            					
            					tmpitem = Items.getTemplate(201517);
            					att = ItemUtils.item2dbAttachment(tmpitem.newInstance(), 5);
            					mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", tmpitem.getName() + "*" + 5, "礼包物品", att, 0, true);
            					
            					tmpitem = Items.getTemplate(201577);
            					att = ItemUtils.item2dbAttachment(tmpitem.newInstance(), 5);
            					mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", tmpitem.getName() + "*" + 5, "礼包物品", att, 0, true);
            					needRemove = true;
            					ok = true;
            					break;
            				}
                    		player.addItem(201151, 1, ch, player.getClientDataVersion());
                    		player.addItem(201398, 5, ch, player.getClientDataVersion());
                    		player.addItem(201517, 5, ch, player.getClientDataVersion());
                    		player.addItem(201577, 5, ch, player.getClientDataVersion());
            				connectService.sendGetItem(ch, player.getId(), (byte)0);
            				log.info("SpurtGift ID[" + player.getId() + "] giveitemid[" + 201151 + "] " +
                        			"giveitemid[" + 201398 +"] giveitemid[" + 201517 + "] giveitemid[" + 201577 + "]");
            				needRemove = true;
            				ok = true;
                    	}else if(item.getItemId() == 201593){//二等
                    		Changed ch = new Changed();
                    		if(player.isFull() ||player.getItemCount(201398) + 1 > 99 
                        			|| player.getItemCount(201517) + 2 > 99
                        			|| player.getItemCount(201577) + 5 > 99
                					|| player.getItemCount(200613) + 2 > 99){
                					IItemTemplate tmpitem = Items.getTemplate(201398);
                					byte [] att = ItemUtils.item2dbAttachment(tmpitem.newInstance(), 1);
                					mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", tmpitem.getName() + "*" + 1, "礼包物品", att, 0, true);
                					
                					tmpitem = Items.getTemplate(201517);
                					att = ItemUtils.item2dbAttachment(tmpitem.newInstance(), 2);
                					mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", tmpitem.getName() + "*" + 2, "礼包物品", att, 0, true);
                					
                					tmpitem = Items.getTemplate(201577);
                					att = ItemUtils.item2dbAttachment(tmpitem.newInstance(), 5);
                					mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", tmpitem.getName() + "*" + 5, "礼包物品", att, 0, true);
                					
                					tmpitem = Items.getTemplate(200613);
                					att = ItemUtils.item2dbAttachment(tmpitem.newInstance(), 2);
                					mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", tmpitem.getName() + "*" + 2, "礼包物品", att, 0, true);
                					
                					needRemove = true;
                					ok = true;
                					break;
                				}
                        		player.addItem(200613, 2, ch, player.getClientDataVersion());
                        		player.addItem(201398, 1, ch, player.getClientDataVersion());
                        		player.addItem(201517, 2, ch, player.getClientDataVersion());
                        		player.addItem(201577, 5, ch, player.getClientDataVersion());
                				connectService.sendGetItem(ch, player.getId(), (byte)0);
                				log.info("SpurtGift ID[" + player.getId() + "] giveitemid[" + 200613 + "] " +
                            			"giveitemid[" + 201398 +"] giveitemid[" + 201517 + "] giveitemid[" + 201577 + "]");
                				needRemove = true;
                				ok = true;
                    	}else if(item.getItemId() == 201594){//三等
                    		Changed ch = new Changed();
                    		if(player.isFull() ||player.getItemCount(201398) + 1 > 99 
                        			|| player.getItemCount(201054) + 5 > 99
                        			|| player.getItemCount(201023) + 5 > 99
                        			|| player.getItemCount(201577) + 5 > 99
                					|| player.getItemCount(200613) + 1 > 99){
                					IItemTemplate tmpitem = Items.getTemplate(201398);
                					byte [] att = ItemUtils.item2dbAttachment(tmpitem.newInstance(), 1);
                					mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", tmpitem.getName() + "*" + 1, "礼包物品", att, 0, true);
                					
                					tmpitem = Items.getTemplate(201054);
                					att = ItemUtils.item2dbAttachment(tmpitem.newInstance(), 5);
                					mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", tmpitem.getName() + "*" + 5, "礼包物品", att, 0, true);
                					
                					tmpitem = Items.getTemplate(201577);
                					att = ItemUtils.item2dbAttachment(tmpitem.newInstance(), 5);
                					mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", tmpitem.getName() + "*" + 5, "礼包物品", att, 0, true);
                					
                					tmpitem = Items.getTemplate(201023);
                					att = ItemUtils.item2dbAttachment(tmpitem.newInstance(), 5);
                					mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", tmpitem.getName() + "*" + 5, "礼包物品", att, 0, true);
                					
                					tmpitem = Items.getTemplate(200613);
                					att = ItemUtils.item2dbAttachment(tmpitem.newInstance(), 1);
                					mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", tmpitem.getName() + "*" + 1, "礼包物品", att, 0, true);
                					needRemove = true;
                					ok = true;
                					break;
                				}
                        		player.addItem(200613, 1, ch, player.getClientDataVersion());
                        		player.addItem(201398, 1, ch, player.getClientDataVersion());
                        		player.addItem(201054, 5, ch, player.getClientDataVersion());
                        		player.addItem(201577, 5, ch, player.getClientDataVersion());
                        		player.addItem(201023, 5, ch, player.getClientDataVersion());
                				connectService.sendGetItem(ch, player.getId(), (byte)0);
                				log.info("SpurtGift ID[" + player.getId() + "] giveitemid[" + 200613 + "] " +
                            			"giveitemid[" + 201398 +"]giveitemid[" + 201577 + "] giveitemid[" + 201054 + "] giveitemid[" + 201023 + "]");
                				needRemove = true;
                				ok = true;
                    		
                    	}else if(item.getItemId() == 201595){//鼓励
                    		Changed ch = new Changed();
                    		if(player.isFull() ||player.getItemCount(550017) + 10 > 99 
                        			|| player.getItemCount(210032) + 2 > 99
                        			|| player.getItemCount(201054) + 10 > 99
                					|| player.getItemCount(201112) + 10 > 99){
                					IItemTemplate tmpitem = Items.getTemplate(550017);
                					byte [] att = ItemUtils.item2dbAttachment(tmpitem.newInstance(), 10);
                					mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", tmpitem.getName() + "*" + 10, "礼包物品", att, 0, true);
                					
                					tmpitem = Items.getTemplate(210032);
                					att = ItemUtils.item2dbAttachment(tmpitem.newInstance(), 2);
                					mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", tmpitem.getName() + "*" + 2, "礼包物品", att, 0, true);
                					
                					tmpitem = Items.getTemplate(201054);
                					att = ItemUtils.item2dbAttachment(tmpitem.newInstance(), 10);
                					mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", tmpitem.getName() + "*" + 10, "礼包物品", att, 0, true);
                					
                					tmpitem = Items.getTemplate(201112);
                					att = ItemUtils.item2dbAttachment(tmpitem.newInstance(), 10);
                					mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", tmpitem.getName() + "*" + 10, "礼包物品", att, 0, true);
                					needRemove = true;
                					ok = true;
                					break;
                				}
                        		player.addItem(550017, 10, ch, player.getClientDataVersion());
                        		player.addItem(210032, 2, ch, player.getClientDataVersion());
                        		player.addItem(201054, 10, ch, player.getClientDataVersion());
                        		player.addItem(201112, 10, ch, player.getClientDataVersion());
                				connectService.sendGetItem(ch, player.getId(), (byte)0);
                				log.info("SpurtGift ID[" + player.getId() + "] giveitemid[" + 550017 + "] " +
                            			"giveitemid[" + 210032 +"]giveitemid[" + 201054 + "] giveitemid[" + 201112 + "]");
                				needRemove = true;
                				ok = true;
                    	}
                    }else if(effects[i].getType() == 102){//一生一世礼包
                    	//oneIsWholeLifeEffect effect = (oneIsWholeLifeEffect)effects[i];
                    	Changed ch = new Changed();
                		if(player.isFull() || player.getItemCount(201555) + 1 > 99
                			|| player.getItemCount(201556) + 1 > 99 
                			|| player.getItemCount(200883) + 10 > 99
                			|| player.getItemCount(201542) + 1 > 99
                			|| player.getItemCount(201599) + 1 > 99){
        					IItemTemplate tmpitem = Items.getTemplate(201555);
        					byte [] att = ItemUtils.item2dbAttachment(tmpitem.newInstance(), 1);
        					mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", tmpitem.getName() + "*" + 1, "礼包物品", att, 0, true);
        					
        					tmpitem = Items.getTemplate(201556);
        					att = ItemUtils.item2dbAttachment(tmpitem.newInstance(), 1);
        					mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", tmpitem.getName() + "*" + 1, "礼包物品", att, 0, true);
        					
        					tmpitem = Items.getTemplate(200883);
        					att = ItemUtils.item2dbAttachment(tmpitem.newInstance(), 10);
        					mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", tmpitem.getName() + "*" + 10, "礼包物品", att, 0, true);
        					
        					tmpitem = Items.getTemplate(201542);
        					att = ItemUtils.item2dbAttachment(tmpitem.newInstance(), 1);
        					mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", tmpitem.getName() + "*" + 1, "礼包物品", att, 0, true);
        					
        					tmpitem = Items.getTemplate(201599);
        					att = ItemUtils.item2dbAttachment(tmpitem.newInstance(), 1);
        					mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统", tmpitem.getName() + "*" + 1, "礼包物品", att, 0, true);
        					
        					sendMessage(player.getId(), "背包已满或物品数量已达上限，物品已发到邮箱!");
        					needRemove = true;
        					ok = true;
        					break;
        				}
                		player.addItem(201555, 1, ch, player.getClientDataVersion());
                		player.addItem(201556, 1, ch, player.getClientDataVersion());
                		player.addItem(200883, 10, ch, player.getClientDataVersion());
                		player.addItem(201542, 1, ch, player.getClientDataVersion());
                		player.addItem(201599, 1, ch, player.getClientDataVersion());
        				connectService.sendGetItem(ch, player.getId(), (byte)0);
        				log.info("onelife ID[" + player.getId() + "] giveitemid[" + 201555 + "] " +
                    			"giveitemid[" + 201556 +"] giveitemid[" + 200883 + "] giveitemid[" + 201542 + "] giveitemid[" + 201599 + "]");
        				needRemove = true;
        				ok = true;
                    }else if(effects[i].getType() == 103){//为掉落组中的装备配置指定的钻数
                    	DropGroupDiamondEffect dgd = (DropGroupDiamondEffect)effects[i];
                    	DropGroup group = DropGroups.getDropGroup(dgd.getDropGroupId(), player.getLevel());
                    	boolean createOk = false;
                		if (group != null) {
                			int rate = rnd.nextInt(group.getRate());
                			DropItem dropItem = group.calcDropItem(rate);
                			if (dropItem.getItem() instanceof EquipmentTemplate) {
                				IItemTemplate template = Items.getTemplate(dropItem.getItem().getItemId());
                            	if(template != null){
                            		IItem iitem = template.newInstance();
                            		IEquipment tmpEqu = (IEquipment)iitem;
                            		tmpEqu.setDataVersion(player.getClientDataVersion());
                            		tmpEqu.setDiamond((byte)dgd.getDiamondCount());
                            		if(dgd.getResetBinded()){
                            			tmpEqu.setBinded(dgd.getSetBinded());
                            		}
                            		if (player.completeAddItem(tmpEqu, 1, changed, player.getClientDataVersion()) == null){
                                    	byte[] att = ItemUtils.item2dbAttachment(tmpEqu, 1);
                                        mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
                                                            tmpEqu.getName(), "", att, 0, true);
                                        sendMessage(player.getId(), "你的背包满了，已经把物品邮寄到你的邮箱!");
                                    }
                            		createOk = true;
                            		ok = true;
                            		needRemove = true;
                            		String item_msg = Items.getMessage(iitem.getItemId(),7,player.getPlayerName(),iitem.getName(),item.getName(), iitem);
                                    if (item_msg != null){
                                    	chatService.sendWorldMessage(-1, "系统", item_msg, iitem);
                                    }
                            	}
							}
                		}
                		if(createOk == false){
                			ok = false;
                			needRemove = false;
                		}
                    }else if(effects[i].getType() == 104){//使用的物品转换成另外的物品
                    	needRemove = false;
                    	UseChangeItemEffect uci = (UseChangeItemEffect)effects[i];
                    	IItemTemplate template = Items.getTemplate(uci.getChangeItemID());
                    	IItem iitem = template.newInstance();
                    	if(player.completeRemoveItem(uci.getNeedItemID(), uci.getNeedItemCount(), changed) != null){
                    		ok = true;
                    		if(player.hasItem(uci.getChangeItemID())){
                    			if(player.getItemCount(uci.getChangeItemID()) + uci.getChangeItemCount() > 99){
                    				byte[] att = ItemUtils.item2dbAttachment(iitem, uci.getChangeItemCount());
                                    mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
                                                        iitem.getName(), "", att, 0, true);
                                    sendMessage(player.getId(), "你的物品达到个数上限，已经把物品邮寄到你的邮箱!");
                    			}else{
                    				player.completeAddItem(iitem, uci.getChangeItemCount(), changed, player.getClientDataVersion());
                    			}
                    		}else{
                    			if(player.isFull()){
                    				byte[] att = ItemUtils.item2dbAttachment(iitem, uci.getChangeItemCount());
                                    mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
                                                        iitem.getName(), "", att, 0, true);
                                    sendMessage(player.getId(), "你的背包已满，已经把物品邮寄到你的邮箱!");
                    			}else{
                    				player.completeAddItem(iitem, uci.getChangeItemCount(), changed, player.getClientDataVersion());
                    			}
                    		}
                    	}else{
                    		ok = false;
                    	}
                    }else if(effects[i].getType() == 105){//占卜之力
                    	needRemove = true;
                    	ok = true;
                    	DivineEffect divine = (DivineEffect)effects[i];
                    	Pet pet = player.getPet();
                    	if(pet == null){
                    		needRemove = false;
                    		sendMessage(player.getId(), "您没有携带宠物!");
                    	}else{
                    		if(pet.getBindType() >= 1){
	                    		EvolutionData nextData = EvolutionLoader.evolutions.get(pet.getEvolutionLevel() + 1);
	                    		if(nextData == null){
	                    			needRemove = false;
	                    			sendMessage(player.getId(), "您携带宠物已经是当前最高级别，不能增加了!");
	                    		}else{
	                    			log.info("Evolution use item currentLevel[" + pet.getEvolutionLevel() + "] point[" + pet.getEvolutionPoint() + "] TRY");
	                    			pet.setEvolutionPoint(pet.getEvolutionPoint() + divine.getValue());
	                    			
	                    			if(pet.getEvolutionPoint() >= nextData.needpoint){
	                    				pet.setEvolutionLevel(pet.getEvolutionLevel() + 1);
	                    				if(pet.getEvolutionLevel() == 4){
	                   						Ability[] abilityCommon  = pet.getAbilities();
	                  						int abiliyCommonId[] = new int[abilityCommon.length];
	                   						for(int j = 0; j < abilityCommon.length ;j++){
	                   							abiliyCommonId[j] = abilityCommon[j].getId();
	                    					}
	                    					Ability[] addSkill = pet.addEvoAbilities(abiliyCommonId);
	                    					changed.addPetAbility(pet, Changed.PET_ADD_SKILL, addSkill);
                    					}    
	                    				pet.setEvolutionPoint(pet.getEvolutionPoint() - nextData.needpoint);
	                    				changed.addPetProperty(pet, Changed.ADD_PET_EVOLUTION_LEVEL, pet.getEvolutionLevel());
	                    				changed.addPetProperty(pet, Changed.ADD_PET_EVOLUTION_PA, nextData.pa);
	                    				changed.addPetProperty(pet, Changed.ADD_PET_EVOLUTION_MA, nextData.ma);
	                    				changed.addPetProperty(pet, Changed.ADD_PET_EVOLUTION_PD, nextData.pd);
	                    				changed.addPetProperty(pet, Changed.ADD_PET_EVOLUTION_MD, nextData.md);
	                    				changed.addPetProperty(pet, Changed.ADD_PET_EVOLUTION_HP, nextData.hp);
	                    				String preName = new String(pet.getName());
	                    				preName = preName.concat("(" + nextData.name + ")");
	                    				changed.addPetProperty(pet, Changed.PET_NAME, preName);
	                    				
	                    				//当前没改过形象并且这一级有新的形象 则进行随机
	                					if(pet.getEvolutionType() == 0 && nextData.types.size() > 0){
	                						pet.setEvolutionType(nextData.getRandomType() + 1);
	                						changed.addPetProperty(pet, Changed.ADD_PET_EVOLUTION_TYPE, pet.getEvolutionType());
	                					}
	                    				chatService.sendWorldMessage(-1, "系统", "恭喜" + player.getPlayerName() + "将自己的爱宠" + pet.getName() + "进化到" + nextData.name + "！");
	                    			}
	                    			int evolutionLevel = pet.getEvolutionLevel();
	                    			int allPoint = EvolutionLoader.getAllNeedPoint(evolutionLevel);
	            					allPoint += pet.getEvolutionPoint();
	            					String evolutionName = EvolutionLoader.getEvolutionName(pet.getEvolutionLevel());
	            					PetEvolutionTopData pdd = new PetEvolutionTopData(player.getId(), player.getPlayerName(), player.getCamp(), pet.getName(), pet.getId(),evolutionName,pet.getEvolutionLevel(),pet.getEvolutionPoint(),allPoint);
	            					PetEvolutionTop.addPetEvolutionData(pdd);
	                    			changed.setProperty(Changed.ADD_PET_DIVINE, divine.getValue());
	                    			log.info("Evolution use item currentLevel[" + pet.getEvolutionLevel() + "] point[" + pet.getEvolutionPoint() + "] type[" + pet.getEvolutionType() + "] OK");
	                    		}
                    		}else{
                    			needRemove = false;
                        		sendMessage(player.getId(), "占卜之力只能给2代及以上的宠物使用!");
                    		}
                    	}
                    }else if(effects[i].getType() == 106){//使用的物品转换成另外的物品
                        needRemove = true;
                        DeleteAddItemEffect eff = (DeleteAddItemEffect)effects[i];
                        IItemTemplate templateDeleted = Items.getTemplate(eff.getDeletedItemId());
                        IItemTemplate template = Items.getTemplate(eff.getAddedItemId());
                        IItem iitem = template.newInstance();
                        if(player.completeRemoveItem(eff.getDeletedItemId(), eff.getDeletedItemCount(), changed) != null){
                            ok = true;
                            if(player.hasItem(eff.getAddedItemId())){
                                if(player.getItemCount(eff.getAddedItemId()) + eff.getAddedItemCount() > 99){
                                    byte[] att = ItemUtils.item2dbAttachment(iitem, eff.getAddedItemCount());
                                    mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
                                                        iitem.getName(), "", att, 0, true);
                                    sendMessage(player.getId(), "你的物品达到个数上限，已经把物品邮寄到你的邮箱!");
                                }else{
                                    player.completeAddItem(iitem, eff.getAddedItemCount(), changed, player.getClientDataVersion());
                                }
                            }else{
                                if(player.isFull()){
                                    byte[] att = ItemUtils.item2dbAttachment(iitem, eff.getAddedItemCount());
                                    mailService.sendMail(player.getId(), player.getPlayerName(), -1, "系统",
                                                        iitem.getName(), "", att, 0, true);
                                    sendMessage(player.getId(), "你的背包已满，已经把物品邮寄到你的邮箱!");
                                }else{
                                    player.completeAddItem(iitem, eff.getAddedItemCount(), changed, player.getClientDataVersion());
                                }
                            }
                        }else{
                            sendMessage(player.getId(), "你需要拥有" + eff.getDeletedItemCount() + "个" + templateDeleted.getName() + "才能使用, 请您到商城购买!");
                            needRemove = false;
                            ok = true;
                        }
                    }
                }
                if (needRemove) {
                	if (item.getType() == IItem.TYPE_BASIC) { // 基本物品在客户端就已经扣除了
                		player.completeRemoveItem(item, 1, null);
                	} else {
                		player.completeRemoveItem(item, 1, changed);
                	}
                }
                
            }
            int[] ret = new int[2];
            ret[0] = ok == true ? 1 : 0;
            ret[1] = petId;
            return ret;
        }
    }

    private String[] getUnRelationString(Master[] masters) {
        String[] ret = new String[masters.length + 4];
        ret[0] = (masters.length + 1) + "";
        ret[1] = "1";
        StringBuilder sb = new StringBuilder();
        sb.append("你要强制解除师徒关系吗?");
        int i = 0;
        for (; i < masters.length; i++) {
            sb.append("\n");
            sb.append(i + 1);
            sb.append(".");
            sb.append(masters[i].getPrenticeName());
        }
        sb.append("\n");
        sb.append(i + 1);
        sb.append(".");
        sb.append("取消");
        ret[2] = sb.toString();
        i = 0;
        for (; i < masters.length; i++) {
            ret[i + 3] = "item_single_unmaster " + masters[i].getPrenticeId();
        }
        ret[i + 3] = "ok";
        return ret;
    }

    private static int getCount(Random rnd, int min, int max) {
        return rnd.nextInt(max - min + 1) + min;
    }

    public void sendGotoMap(int playerId, short mapId, short x, short y) {
        byte[] bytes = stageService.getTaskBytes((short) 31004,
                                                 new String[] {"" + mapId,
                                                 "" + x, "" + y});
        UWAPSegment seg = new UWAPSegment(ClientConstants.
                                          GET_FILE_OK);
        seg.writeShort((short) 31004);
        seg.writeShort((short) 2);
        seg.write(bytes);
        connectService.writeTo(seg, playerId);
    }

    public void sendMessage(int playerId, String message) {
        UWAPSegment seg = new UWAPSegment(ClientConstants.MESSAGE);
        seg.writeString(message);
        connectService.writeTo(seg, playerId);
    }

    public boolean petUseItem(PlayerData player, Pet pet, IEffectItem item, Changed changed) {
        if (!isRedAndBlue(item)) {
            return false;
        } else {
        	if(pet != null && (pet.getMaxHp() == pet.getHp() || pet.getMaxMp() == pet.getMp())){
        		Effect[] effects = item.getEffects();
        		boolean addHP = false;
        		boolean addMP = false;
                for (int i = 0; i < effects.length; i++) {
                    PropertyEffect effect = (PropertyEffect) effects[i];
                    if (effect.getProperty() == Changed.HP) {
                        if(pet.getHp() < pet.getMaxHp()){
                        	addHP = true;
                        }
                    } else if (effect.getProperty() == Changed.MP) {
                        if(pet.getMp() < pet.getMaxMp()){
                        	addMP = true;
                        }
                    }
                }
                //加不蓝也加不了血 不扣除
                if(!addHP && !addMP){
                	return false;
                }
        	}
            IItem im = player.completeRemoveItem(item, 1, null);
            if (im != null) {
                if (im.getType() != IItem.TYPE_BASIC) {
                    changed.addItem(im, -1);
                }
                Effect[] effects = item.getEffects();
                for (int i = 0; i < effects.length; i++) {
                    PropertyEffect effect = (PropertyEffect) effects[i];
                    if (effect.getProperty() == Changed.HP) {
                        pet.addHp(effect.getValue());
                    } else if (effect.getProperty() == Changed.MP) {
                        pet.addMp(effect.getValue());
                    }
                }
                return true;
            }
            return false;
        }
    }

    public boolean isRedAndBlue(IEffectItem item) {
        Effect[] effects = item.getEffects();
        for (int i = 0; i < effects.length; i++) {
            if (effects[i].getType() != 1)
                return false;
            PropertyEffect e = (PropertyEffect) effects[i];
            if (e.getTime() != 0)
                return false;
            if (e.getProperty() != Changed.HP && e.getProperty() != Changed.MP)
                return false;
        }
        return true;
    }

    public void checkBattleBuff(IPlayerData player, Changed changed) {
        Buf[] bufs = player.getBufs();
        for (int i = 0; i < bufs.length; i++) {
            if (bufs[i].getUnit() == 0 || bufs[i].getUnit() == 10) {
                bufs[i].descTime();
                if (bufs[i].getTime() <= 0) {
                    player.removeBuf(bufs[i], changed);
                }
            }
        }
    }

    public void unRegistry(IPlayerData player) {
        synchronized (players) {
            players.remove(player.getId());
        }
    }

    public void registry(PlayerData player) {
        synchronized (players) {
            players.put(player.getId(), player);
        }
    }

    public void run() {
        while (true) {
            try {
                Thread.sleep(30 * 1000L);
            } catch (InterruptedException ex) {
            }
            Iterator ite = players.values().iterator();
            while (ite.hasNext()) {
                IPlayerData player = (IPlayerData) ite.next();
                if (player.getBufs().length > 0) {
                    for (int i = 0; i < player.getBufs().length; i++) {
                        if (player.getBufs()[i].getUnit() == 1 || player.getBufs()[i].getUnit() == 11) {
                            long time = System.currentTimeMillis() - player.getBufs()[i].getTimestamp();
                            if (time > player.getBufs()[i].getTime() * 1000) {
                            	Changed changed = new Changed();
                                player.removeBuf(player.getBufs()[i], changed);
                                connectService.sendGetItem(changed, player.getId(), (byte) 4);
                                i --;
                            }
                        }
                    }
                }
                //玩家的橱窗
                ArrayList image = (ArrayList)player.getImage();
                if (image.size() > 0) {
                	for (int i = 0; i < image.size(); i++) {
                		RoleFaceData selfFace = (RoleFaceData)image.get(i);
                		if (selfFace.getExpiration() > RoleFaceData.EXPIRED) {											// 有时效性的形象
                			Date date = new Date();
                			if (date.getTime() > selfFace.getExpiration()) {						// 形象过期
                				selfFace.setExpiration(RoleFaceData.EXPIRED);											// 设置已过期
//                				player.removeRoleFace(selfFace.getFace());							// 删除形象
                				if (player.getFace() == selfFace.getFace()) {
                					Changed changed = new Changed();
                					player.setDefaultFace();
                					if (player.completeAddRoleFace(player.getFace(), 1, changed, -1) != null) {
                						player.resetImage();
                						playerService.savePlayer(player);
                						
                					}
                					changed.setProperty(Changed.FACE, player.getFace());			// 同步形象
                					connectService.sendGetItem(changed, player.getId(), (byte)22);
                				}
                				log.info("BufService: Check player image playerID[" + player.getId() + "] FaceID[" +
                    					selfFace.getFace() +"] getExpiration[" + selfFace.getExpiration() + "] and this time Date[" + date.getTime() + "] Set the image expired success!");
                			}
                		}
                	}
                }
//                if (player.getBufSize() <= 0) {
//                    ite.remove();
//                }
            }
        }
    }

	public ChatService getChatService() {
		return chatService;
	}
	
	public TreasureService getTreasureService () {
		return treasureService;
	}

	public void setChatService(ChatService chatService) {
		this.chatService = chatService;
	}
	
	public ConcurrentHashMap getPlayers () {
		return players;
	}
}
