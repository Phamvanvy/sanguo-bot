package com.pip.itimes.server.world;

import java.lang.reflect.Method;
import java.util.*;
import com.pip.itimes.net.*;
import com.pip.itimes.server.ITimesException;
import com.pip.itimes.server.world.activityService.activity.ActivityData;
import com.pip.itimes.server.bean.Activity;
import com.pip.itimes.server.bean.Admin;
import com.pip.itimes.server.bean.Tong;
import com.pip.itimes.server.bean.Admin.AdminAuth;
import com.pip.itimes.server.bean.Player;
import com.pip.itimes.server.stage.*;
import com.pip.itimes.server.util.Utils;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoSession;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import com.pip.itimes.server.util.IDGenerator;
import com.pip.itimes.server.world.accountbinging.AccountBingingService;
import com.pip.itimes.server.world.activityService.ActivityService;
import com.pip.itimes.server.world.battle.Battle2;
import com.pip.itimes.server.world.battle.BattleService2;
import com.pip.itimes.server.world.camp.CampMainService;
import com.pip.itimes.server.world.game.*;
import com.pip.itimes.server.dao.HibernateUtil;
import com.pip.itimes.server.bean.Mail;
import com.pip.itimes.server.dao.*;
import org.apache.log4j.Logger;

import com.pip.itimes.server.bean.Bbs;
import com.pip.itimes.server.world.sports.SportsService;
import com.pip.itimes.server.world.toplist.TopListService;
import com.pip.accountskeleton.AccountSkeleton;
import com.pip.net.IRequestService;
import com.pip.net.message.gameaccount.AccountInfoMessage;
import com.pip.accountskeleton.AccountInfoRequest;
import com.pip.net.message.gameaccount.AccountInfoOkMessage;
import com.pip.net.message.gameaccount.ChangeStatusMessage;

import java.text.*;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class AdminSession extends Session {

    private static final Logger log = Logger.getLogger(AdminSession.class);

    public AdminService adminService;
    public ConnectService connectService;
    private Admin admin;
    public ChatService chatService;
    public PlayerService playerService;
    public StageService stageService;
    public PhoneService phoneService;
    public ShopService shopService;
    public AuctionService auctionService;
    public AccountSkeleton accountSkeleton;
    public MailService mailService;
    public BattleFieldInstanceModel battleField;
    public GuildBattleFieldInstanceModel guildBattleField;
    public BbsService bbsService;
    public RobotService robotService;
    public HouseInstanceModel houseModel;
    public FarmInstanceModel farmInstanceModel;
    public VersionService versionService;
    public SportsService sportsService;
    public IRequestService requestService;
    public TongService tongService;

    public TopListService topListService = null;
    //mengjie add
    private AccountBingingService accountbingingService;
    private Map id2player = new HashMap();
    private Map name2player = new HashMap();
    
    //jwp add
    //删除角色的时候需要处理一下师徒关系，婚姻关系，知己关系
    protected MateService mateService;
    
    private CampMainService campMainService;
    
    private ActivityService activityService;
    
    private CampBattlefieldService campBattlefieldService;
    
    public MateService getMateService() {
		return mateService;
	}
    
	public void setMateService(MateService mateService) {
		this.mateService = mateService;
	}
	
	public MasterService getMasterService() {
		return masterService;
	}
	
	public void setMasterService(MasterService masterService) {
		this.masterService = masterService;
	}
	
	public FriendsService getFriendsService() {
		return friendsService;
	}
	
	public void setFriendsService(FriendsService friendsService) {
		this.friendsService = friendsService;
	}
	
	public void setCampMainService(CampMainService campMainService){
	    this.campMainService = campMainService;
	}

	protected MasterService masterService;
    protected FriendsService friendsService;

    private static final byte PRO_MAPID = 1;
    private static final byte PRO_X = 2;
    private static final byte PRO_Y = 3;
    private static final byte PRO_FACE = 4;
    private static final byte PRO_EXP = 5;
    private static final byte PRO_MONEY = 6;
    private static final byte PRO_AGILITY = 7;
    private static final byte PRO_STRENGTH = 8;
    private static final byte PRO_INTELLIGENCE = 9;
    private static final byte PRO_VITALITY = 10;
    private static final byte PRO_LEVEL = 11;
    private static final byte PRO_HP = 12;
    private static final byte PRO_MP = 13;
    private static final byte PRO_LEAVEPOINT = 14;
    private static final byte PRO_LEAVEABILITYPOINT = 15;
    private static final byte PRO_GRIDSIZE = 16;
    private static final byte PRO_ADDEDGRIDSIZE = 17;
    private static final byte PRO_PETSIZE = 18;
    
    private static final byte ADMIN_NEW_ACTIVITY_CLIENT = 1;
    private static final byte ADMIN_DELETE_ACTIVITY_CLIENT = 2;
    private static final byte ADMIN_ACTIVITY_DETAIL_CLIENT = 3;
    private static final byte ADMIN_ENABLE_ACTIVITY_CLIENT = 4;

    private boolean isKeepWatch = false;

    private int addedOnline = 0;

    public String type = "pip";
    
    private static final SimpleDateFormat format = new SimpleDateFormat("yy-MM-dd HH:mm");

    static {
        // 缺省的管理员命令, 这里预加载的目的是通过协议包可以访问这些命令，同时可以看到帮助。
    	String[] preLoadedAdminFuncs = { 
    			"", 
    	};
    	for (String s : preLoadedAdminFuncs) {
        	registAdminFunction(s);
        }
    }
    /** 管理员命令的 指令－引擎映射。可以动态替换。 */
    private static HashMap<String,IAdminFunction> adminFuncs = new HashMap<String,IAdminFunction>();
    /** 管理员命令的 协议号－模块 映射。可以动态替换。*/
    private static HashMap<Integer,IAdminFunction> adminIdFuncs = new HashMap<Integer,IAdminFunction>(); 
    /** 注册管理员模块。通过指令，将处在admin包下的模块动态加载，并注册替换同类模块 */
    private static void registAdminFunction(String methodName) {
    	if (methodName != null && methodName.length() > 0) {
	    	IAdminFunction func = adminFuncs.get(methodName);
	    	if (func == null) {
	        	String engineName = "com.pip.itimes.server.world.admin." + methodName;
	        	try {
		        	Class kls = Class.forName(engineName);
		        	if (kls != null) {
		        		registAdminFunction(methodName, (IAdminFunction)kls.newInstance());
		        	}
	        	} catch (Exception e) {
	        		log.warn("没有管理员功能:" + methodName);
	        	}
	    	}
    	}
    }
    /** 注册管理员模块。把模块的所有命令和协议号都挂载上，会替换已经注册的旧模块 */
    private static void registAdminFunction(String methodName, IAdminFunction func) {
    	adminFuncs.put(methodName, func);
		String[] extraMethods = func.getCommands();
		if (extraMethods != null) {
			for (String s: extraMethods) {
				adminFuncs.put(s, func);
			}
		} 
		int handledProtocols[] = func.getProtocolId();
		if (handledProtocols != null) {
			for (int protocolId : handledProtocols) {
				adminIdFuncs.put(protocolId, func);
			}
		}
    }
    public AdminSession(IoSession session,String type) {
        super(session);
        this.type = type;
    }

    public void setRobotService(RobotService robotService){
        this.robotService = robotService;
    }

    public void setAdminService(AdminService adminService) {
        this.adminService = adminService;
    }

    public void setConnectService(ConnectService connectService) {
        this.connectService = connectService;
    }

    public void setChatService(ChatService chatService) {
        this.chatService = chatService;
    }

    public void setPlayerService(PlayerService playerService) {
        this.playerService = playerService;
    }

    public void setStageService(StageService stageService) {
        this.stageService = stageService;
    }

    public void setAccountSkeleton(AccountSkeleton accountSkeleton){
        this.accountSkeleton = accountSkeleton;
    }

    public void setPhoneService(PhoneService phoneService) {
        this.phoneService = phoneService;
    }

    public void setShopService(ShopService shopService) {
        this.shopService = shopService;
    }

    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }

    public void setAuctionService(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    public void setBattleField(BattleFieldInstanceModel battleField) {
        this.battleField = battleField;
    }

    public void setGuildBattleField(GuildBattleFieldInstanceModel guildBattleField){
        this.guildBattleField = guildBattleField;
    }

    public void setBbsService(BbsService bbsService){
        this.bbsService = bbsService;
    }

    public void setHouseModel(HouseInstanceModel houseModel){
        this.houseModel = houseModel;
    }
    
    public void setFarmInstanceModel(FarmInstanceModel farmInstanceModel){
    	this.farmInstanceModel = farmInstanceModel;
    }

    public void setVersionService(VersionService versionService){
        this.versionService = versionService;
    }

    public void setSportsService(SportsService sportsService){
        this.sportsService = sportsService;
    }

    public void setReuqestService(IRequestService requestService){
        this.requestService = requestService;
    }

    public void setTongService(TongService tongService){
        this.tongService = tongService;
    }

	public void setAccountbingingService(AccountBingingService accountbingingService) {
		this.accountbingingService = accountbingingService;
	}
	
	public void setTopListService(TopListService topListService) {
		this.topListService = topListService;
	}
	
	public void setActivityService (ActivityService activityService) {
		this.activityService = activityService;
	}
	
	public void setCampBattlefieldService (CampBattlefieldService campBattlefieldService) {
		this.campBattlefieldService = campBattlefieldService;
	}
	
	public void sendMessage(String message, int destId, int value,
                            boolean system) {
//        UWAPSegment seg = new UWAPSegment(ServerConstants.CHAT);
//        int srcId = -1;
//        String srcName = admin.getName();
//        if(system)
//            srcName = "系统";
//        seg.writeInt(srcId);
//        seg.writeString(srcName);
//        seg.writeInt(destId);
//        seg.writeInt(value);
//        seg.writeString(message);
//        connectService.broadcast(seg);
    }

    public void closed() {
        id2player.clear();
        name2player.clear();
    }

    public void created() {
    }

    public void handle(Packet packet) {
        try {
            UWAPData data = packet.datas[0];
            byte type = data.getAppType();
            IAdminFunction func = adminIdFuncs.get(Integer.valueOf(type));
            if (func != null) {
            	func.execCommand(this, data);
            } else {
	            switch (type) {
	                case ServerConstants.ADMIN_COMMAND:
	                    command(data);
	                    break;
	                case ServerConstants.ADMIN_BATCH_COMMAND:
	                    batchcommand(data);
	                    break;
	                case ServerConstants.ADMIN_LOGIN:
	                    admin_login(data);
	                    break;
	                case ServerConstants.ADMIN_SHOW_PLAYER:
	                    admin_show_player(data);
	                    break;
	                case ServerConstants.ADMIN_WHO:
	                    admin_who(data);
	                    break;
	                case ServerConstants.ADMIN_KICK:
	                    admin_kick(data);
	                    break;
	                case ServerConstants.ADMIN_MUTE:
	                    admin_mute(data);
	                    break;
	                case ServerConstants.ADMIN_SAY:
	                    admin_say(data);
	                    break;
	                case ServerConstants.ADMIN_MODIFY:
	                    admin_modify(data);
	                    break;
	                case ServerConstants.ADMIN_FORBID_ACCOUNT:
	                    admin_forbid_account(data);
	                    break;
	                case ServerConstants.ADMIN_ADD:
	                    admin_add(data);
	                    break;
	                case ServerConstants.ADMIN_DELETE:
	                    admin_delete(data);
	                    break;
	                case ServerConstants.ADMIN_RELEASE_ACCOUNT:
	                    admin_release_account(data);
	                    break;
	                case ServerConstants.ADMIN_ADDIP:
	                    admin_addip(data);
	                    break;
	                case ServerConstants.ADMIN_AUTH:
	                    admin_auth(data);
	                    break;
	                case ServerConstants.ADMIN_KEEPWATCH:
	                    admin_keepwatch(data);
	                    break;
	                case ServerConstants.ADMIN_ACCOUNTINFO:
	                    admin_accountinfo(data);
	                    break;
	                case ServerConstants.ADMIN_PLAYERLIST:
	                    admin_playerlist(data);
	                    break;
	                case ServerConstants.ADMIN_MODIFYACCOUNT:
	                    admin_modifyaccount(data);
	                    break;
	                case ServerConstants.ADMIN_FORBID:
	                    admin_forbid(data);
	                    break;
	                case ServerConstants.ADMIN_SCRIPT:
	                    admin_script(data);
	                    break;
	                case ServerConstants.ADMIN_BATTLEFIELD:
	                    admin_battlefield(data);
	                    break;
	                case ServerConstants.ADMIN_MAIL_GET_LIST:
	                    admin_mailList(data);
	                    break;
	                case ServerConstants.ADMIN_MAIL_SEND:
	                    admin_mailSend(data);
	                    break;
	                case ServerConstants.ADMIN_MAIL_DELETE:
	                    admin_mailDelete(data);
	                    break;
	                    // 
	                case ServerConstants.ADMIN_MAIL_MARK_REPLIED:
	                    admin_mailMarkDone(data);
	                    break;
	                case ServerConstants.ADMIN_REQUEST_ISHOP_LIST:
	                    admin_ishopList(data);
	                    break;
	                case ServerConstants.ADMIN_ISHOP_MODIFY:
	                    admin_ishopModify(data);
	                    break;
	                case ServerConstants.ADMIN_BBS_GET_LIST:
	                    admin_bbsList(data);
	                    break;
	                case ServerConstants.ADMIN_BBS_DELETE:
	                    admin_bbsDelete(data);
	                    break;
	                case ServerConstants.ADMIN_BBS_SEND:
	                    admin_bbsSend(data);
	                    break;
	                //mengjie add
	                case ServerConstants.ADMIN_BBS_GETID:
	                	admin_bbsGetIDinfo(data);
	                    break;
	                case ServerConstants.ADMIN_BBS_GETBYUSER:
	                	admin_bbsListbyuser(data);
	                    break;
	                case ServerConstants.ADMIN_BBS_DELETEBYID:
	                	admin_bbsDeletebyid(data);
	                    break;
	                case ServerConstants.ADMIN_BBS_DELETEBYUSERID:
	                	admin_bbsDeletebyuserid(data);
	                    break;
	                case ServerConstants.ADMIN_MAIL_SENDENHANCE:
	                	admin_mailSendEnhanceEQU(data);
	                    break;
	                //mengjie add end
	                case ServerConstants.ADMIN_CHAP_CONFIG:
	                    admin_setChatConfig(data);
	                    break; 
	                case ServerConstants.ADMIN_EXPADD:
	                	admin_resetexpadd(data);
	                	break;
	                case ServerConstants.ADMIN_DELETE_ROLE:
	                	admin_deleteRole(data);
	                	break;
	                case ServerConstants.ADMIN_RECOVER_ROLE_SHOW:
	                	admin_recoverRoleShow(data);
	                	break;
	                case ServerConstants.ADMIN_RECOVER_ROLE:
	                	admin_recoverRole(data);
	                	break;
	                case ServerConstants.ADMIN_CAMP:
	                	admin_camp(data);
	                	break;
	                case ServerConstants.ADMIN_ACTIVITY:
	                	admin_handleActivities(data);
	                	break;
	                case ServerConstants.ADMIN_IHOP_CREDITADD:
	                	admin_resetIshopCredit(data);
	                	break;
	                case ServerConstants.ADMIN_BATH_EXPCREDITADD:
	                	admin_resetBathExpCredit(data);
	                	break;
	                case ServerConstants.ADMIN_UNLINE_EXP:
	                	admin_resetUnlineExp(data);
	                	break;
	                case ServerConstants.ADMIN_ISHOP_DISCOUNT:
	                	admin_ishopDiscount(data);
	                	break;
	                case ServerConstants.ADMIN_ADD_FARMMONEY:
	                	admin_addFarmMoney(data);
	                	break;
	                case ServerConstants.ADMIN_BATTLE_CLEAR:
	                	admin_battleClear(data);
	                	break;
	               }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    int interestedChatIds[] = new int[12];
    private void admin_setChatConfig(UWAPData data) {
        try {
        	byte channel = data.readByte(); // 私聊， 世界，地区，帮派，, ....
        	int id = data.readInt(); // 世界聊忽略
        	boolean open = data.readBoolean();
        	if (channel >= 0 && channel < interestedChatIds.length) {
	        	if (open) {
	        		interestedChatIds[channel] = id;
	        	} else {
	        		interestedChatIds[channel] = -1;
	        	}
	        	StringBuffer buf = new StringBuffer();
	        	buf.append("窃听器配置：");
	        	if (interestedChatIds[0] > 0) {
	        		buf.append("玩家:[");
	        		buf.append(interestedChatIds[0]);
	        		buf.append("] ");
	        	}
	        	if (interestedChatIds[-ISendMessage.MAP] > 0) {
	        		buf.append("场景:");
	        		buf.append(interestedChatIds[-ISendMessage.MAP]);
	        		buf.append(" ");
	        	}
	        	if (interestedChatIds[-ISendMessage.GUILD] > 0) {
	        		buf.append("帮派:");
	        		buf.append(interestedChatIds[-ISendMessage.GUILD]);
	        		buf.append(" ");
	        	}
	        	if (interestedChatIds[-ISendMessage.TEAM] > 0) {
	        		buf.append("队伍:");
	        		buf.append(interestedChatIds[-ISendMessage.TEAM]);
	        		buf.append(" ");
	        	}
	        	if (interestedChatIds[-ISendMessage.FAVORITE] > 0) {
	        		buf.append("频道:");
	        		buf.append(interestedChatIds[-ISendMessage.FAVORITE]);
	        		buf.append(" ");
	        	}
	        	if (interestedChatIds[-ISendMessage.GM] > 0) {
	        		buf.append("GM");
	        	}
	        	write(buf.toString());
        	} else {
        		write("参数错误");
        	}
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
    }
    public void onChatMessage(int type, int id, int toId, UWAPSegment data) {
    	if (type >= 0 && type < interestedChatIds.length) {
    		if (type == -ISendMessage.WORLD || type == -ISendMessage.SYSTEM) {
    			// 世界聊无条件转发, 系统广播无条件转发
    			write(data);
    		} else if ((type == -ISendMessage.MAP || type == -ISendMessage.FAVORITE) && interestedChatIds[type] == toId) {
    			// 地区频道,圈频道 可以随意侦听
    			write(data);
    		} else if ((type == -ISendMessage.GUILD || type == -ISendMessage.TEAM) && interestedChatIds[type] == toId) {
    			// 帮派,小队
    			if (admin.hasAuth(AdminAuth.overhear)) { 
    				write(data);
    			}
    		} else if ((type == -ISendMessage.GROUP || type == -ISendMessage.GM) && interestedChatIds[type] > 0) {
    			// 团频道,GM频道需要权限
    			if (admin.hasAuth(AdminAuth.overhear)) {
    				write(data);
    			}
    		} else if (interestedChatIds[0] > 0 && 
    				((type == 0 && toId == interestedChatIds[0]) || id == interestedChatIds[0])) { 
    			// 私聊需要权限侦听
    			if (admin.hasAuth(AdminAuth.overhear)) {
    				write(data);
    			}
    		}
    	}
    }
    //mengjie add
    private void admin_bbsGetIDinfo(UWAPData data) throws Exception{
    	int pageSize = data.readShort();
        int pageNo = data.readInt();
        if(BbsInfo.getBbscount()==0){
            write("没有bbsnpc的存在");
            return;
        }else if(BbsInfo.getBbscount()<=pageNo*pageSize){
        	write("没有更多的bbsnpc的存在");
            return;
        }
        BbsInfo bbsInfo;
        UWAPSegment seg = new UWAPSegment(ServerConstants.ADMIN_BBS_GETID,
                data.getSerial());
		seg.writeShort((short) pageSize);
		seg.writeInt(pageNo);
		seg.writeInt((BbsInfo.getBbscount()/pageSize)+1);
		seg.writeShort((short)(BbsInfo.getBbscount() - pageNo*pageSize));//pagecount
		for (int i = 0; i < BbsInfo.getBbscount() - pageNo*pageSize; i++) {
			int id = pageNo*pageSize + i;
			bbsInfo = BbsInfo.getBbsInfobyid(id);
			seg.writeInt(bbsInfo.getId());
			seg.writeInt(bbsInfo.getMapid());
			seg.writeString(bbsInfo.getInfo());
			seg.writeInt(bbsInfo.getNpcid());
			seg.writeString(bbsInfo.getName());
		}
		write(seg);
    }
    private void admin_bbsListbyuser(UWAPData data) throws Exception{
        int playerId = data.readInt();
        int bbsId = data.readInt();
        int pageSize = data.readShort();
        int pageNo = data.readInt();
        BbsService.BbsResult result = bbsService.getBbsListbyplayerid(playerId,bbsId,pageSize,pageNo);
        if(result.bbs.length==0){
            write("没有更多的bbs存在");
            return;
        }
        UWAPSegment seg = new UWAPSegment(ServerConstants.ADMIN_BBS_LIST,
                                          data.getSerial());
        seg.writeInt(bbsId);
        seg.writeShort((short) pageSize);
        seg.writeInt(pageNo);
        seg.writeInt(result.pageCount);
        seg.writeShort((short) result.bbs.length);
        for (int i = 0; i < result.bbs.length; i++) {
            Bbs bbs = result.bbs[i];
            seg.writeInt(bbs.getId());
            seg.writeInt(bbs.getPlayerId());
            seg.writeString(bbs.getPlayerName());
            seg.writeString(bbs.getTitle());
            seg.writeString(bbs.getContent());
            seg.writeInt(bbs.getPriority());
            seg.writeString(Utils.getDateString(bbs.getPostTime()));
        }
        write(seg);
    }

    private void admin_mailSendEnhanceEQU(UWAPData data){
        try {
            int destId = data.readInt();
            String title = data.readString();
            String content = data.readString();
            int attachment = data.readInt();
            byte[] enhance = data.readBytes();
            byte[] att = new byte[0];
            
            //写下鉴定数量
            byte diamondCount = data.readByte();
            
             //孔位信息
            byte[] dimaondMosaicInfo =data.readBytes();
            //先前宝石数量 如果此时为0的话，则说明装备没有消遣宝石
            
            byte[] dimaondMosaicRole =data.readBytes(); //镶嵌部位属性
            int[] diamondMosaicItemId = data.readInts();//镶嵌物品
            
            int enchantingItemID = data.readInt();//附魔卷轴ID, 201109 - 201111, 0或负数表示没有附魔
            //附魔数组  [0]:是否增加属性，[1]：是否增加宝石，[2]:要增加的属性索引，[3]:要增加的属性值，[4]:要增加的宝石对应属性索引，[5]:宝石额外属性值
            byte[] enchanting = data.readBytes();//new byte[]{1,1,3,24,4};
            byte[] vianyValue = data.readBytes();//new byte[]{4,6,8};属性攻(石头，剪子，布)
            
            if (!checkLogined()) {
                errorLogin();
                return;
            }
            if (!checkAuth("add")) {
                errorAuth();
                return;
            }
            if(attachment>0){
                IItemTemplate template = Items.getTemplate(attachment);
                if(template==null){
                    write("附件ID错误");
                    return;
                }else{
	                IItem item = template.newInstance();
	                if(item.getType()!=IItem.TYPE_EQU){
	                	write("物品种类错误（非装备）");
	                	return;
	                }else{
	                	IEquipment equ = (IEquipment)item;
		                if(enhance.length > 0){//精炼
		                	Enhance enhancetmp = null;
		                	for(int k = 0;k<enhance.length;k++){
		                		if ((enhance[k] <= 0) || (enhance[k] > 12)){
		                			break;
		                		}
		                		enhancetmp = Enhance.getEnhance(enhance[k],template.getLevel());
		                		equ.enhance(enhancetmp);
		                	}
		                }
		                //星级鉴定
		               /* if(diamondCount > equ.getDiamondcount() || diamondCount < 0){
  		                	throw new Exception("星级鉴定数量不对");
  		                }else{*/
  		                equ.setDiamond(diamondCount);
  		                //}
		                //宝石检测
		                equ.setDiamondMosiacRoleInfo(dimaondMosaicInfo);
	                	if(dimaondMosaicRole.length != diamondMosaicItemId.length){
	                		throw new Exception("镶嵌宝石信息长度不匹配");
	                	}
	                	
	                	//检测宝石信息
	                	Map<Integer,DiamondMosaic> diamondMosaicMap = DiamondMosaic.getDiamondMosaicMap();
	                	for(int i = 0; i < diamondMosaicItemId.length; i++){
	                		DiamondMosaic diamond = diamondMosaicMap.get(diamondMosaicItemId[i]);
	                		if(diamond == null){
	                			if(dimaondMosaicRole[i] != IEquipment.CURRENT_EQU_DIAMOND_NOTROLE || dimaondMosaicRole[i] != IEquipment.CURRENT_EQU_CANDIAMOND){
	                				throw new Exception("没有此宝石物品id");
	                			}
	                		}
	                		
	                		if(equ.canDiamondMosiacEmbed(dimaondMosaicRole[i], diamond.getProperty()) == IEquipment.CURRENT_EQU_DIAMOND){
	                			throw new ITimesException("该孔位已经镶嵌了宝石", data.getSerial(), data.getSessionId(), (byte) 36);
	                		}else if(equ.canDiamondMosiacEmbed(dimaondMosaicRole[i], diamond.getProperty()) == IEquipment.CURRENT_EQU_DIAMOND_PROPERTY){
	                			throw new ITimesException("该孔位已经镶嵌了同类宝石", data.getSerial(), data.getSessionId(), (byte) 36);
	                		}
	                		
	                		/*//检测宝石镶嵌的级别信息
	                		if(dimaondMosaicRole[i] != IEquipment.CURRENT_EQU_DIAMOND_NOTROLE || dimaondMosaicRole[i] != IEquipment.CURRENT_EQU_CANDIAMOND){
	                			byte diamondMosaicEmbedLevel = diamond.findDiamondMosaicLevel(diamondMosaicItemId[i]);
	                			if(dimaondMosaicInfo[dimaondMosaicRole[i]] != (IEquipment.CURRENT_EQU_CANDIAMOND + diamondMosaicEmbedLevel)){
	                				throw new Exception("此处孔位信息不对");
	                			}
	                		}	*/
	                		byte[] diamondRoleInfo = equ.getDiamondMosiacRoleInfo();
	    					diamondRoleInfo[dimaondMosaicRole[i]] = (byte) (IEquipment.CURRENT_EQU_CANDIAMOND + diamond.getDiamondLevel());
	                		equ.diamondMosaic(dimaondMosaicRole[i], diamond);
	                	}
	                	
	                	//附魔
  	                	if(enchantingItemID > 0){
  	                		if(enchanting.length != 5){
  	                			write("附魔信息长度不匹配。");
  	                			return;
  	                		}
  	                		if(equ.getRequiredLevel() < 30){
  	                			write("只有装备等级大于等于30级的装备可以被附魔。");
  	                			return;
  	                		}
	  	                	Enchanting enchan = equ.getEnchanting();
	  	                	boolean hasArrtType = false;
	  						boolean hasStoneType = false;
	  						byte arrtType = 0;
	  						byte stoneType = 0;
	  						byte arrtValue = 0;
	  						byte stoneValue = 0;
	  						int arrtIndex = 0;
	  						Random rnd = new Random();
	  	                	switch(enchantingItemID){
	  	                	case ConnectSession.ENCHANTING_SCROLLITEMID_BASE:// 初级附魔卷轴
	  	                		if(enchanting[0]==1 && enchanting[1]==0){
	  	                			hasArrtType = true;
	  	                		}else{
	  	                			write("附魔卷轴的附加效果与输入的附加效果不符。");
	  	                			return;
	  	                		}
	  	                		break;
	  	                	case ConnectSession.ENCHANTING_SCROLLITEMID_MID://中级附魔卷轴
	  	                		if(enchanting[0]==1 && enchanting[1]==0){
	  								hasArrtType = true;
	  							}else if(enchanting[0]==0 && enchanting[1]==1){
	  								hasStoneType = true;
	  							}else{
	  								write("附魔卷轴的附加效果与输入的附加效果不符。");
	  	                			return;
	  							}
	  							arrtIndex = 1;
	  	                		break;
	  	                	case ConnectSession.ENCHANTING_SCROLLITEMID_HIGH://高级附魔卷轴
	  	                		if(enchanting[0]==1 && enchanting[1]==1){
	  	                			hasArrtType = true;
	  								hasStoneType = true;
	  								arrtIndex = 2;
	  	                		}else{
	  	                			write("附魔卷轴的附加效果与输入的附加效果不符。");
	  	                			return;
	  	                		}
	  	                		break;
	  	                	default:
	  	                		write("附魔卷轴ID错误。");
  	                			return;
	  	                	}
	  	                	//增加属性
	  						if(hasArrtType){
	  							int typeIndex = 0;
	  							if(enchanting[2] == -1){
	  								typeIndex = Utils.getRandom(rnd, 0, Enchanting.ARRT_TYPE.length - 1);
	  							}else if(enchanting[2] < Enchanting.ARRT_TYPE.length && enchanting[2] > -1){
	  								typeIndex = enchanting[2];
	  							}else{
	  								write("附魔要增加的属性索引错误。");
	  	                			return;
	  							}
	  							arrtType = Enchanting.ARRT_TYPE[typeIndex];
	  							if(enchanting[3] == -1){
	  								arrtValue = (byte)Utils.getRandom(rnd, Enchanting.ARRT_MIN[arrtIndex][typeIndex], Enchanting.ARRT_MAX[arrtIndex][typeIndex]);
	  							}else if(enchanting[3] >= Enchanting.ARRT_MIN[arrtIndex][typeIndex] && enchanting[3] <= Enchanting.ARRT_MAX[arrtIndex][typeIndex]){
	  								arrtValue = enchanting[3];
	  							}else{
	  								write("附魔要增加的属性值错误。");
	  	                			return;
	  							}
	  						}
	  						//增加宝石
	  						if(hasStoneType){
	  							int stoneIndex = 0;
	  							if(enchanting[4] == -1){
	  								stoneIndex = Utils.getRandom(rnd, 0, Enchanting.ARRT_TYPE.length - 1);
	  							}else if(enchanting[4] < Enchanting.ARRT_TYPE.length && enchanting[4] > -1){
	  								stoneIndex = enchanting[4];
	  							}else{
	  								write("附魔要增加的宝石属性索引错误。");
	  	                			return;
	  							}
	  							stoneType = Enchanting.ARRT_TYPE[stoneIndex];
	  							int stoneLevel = Enchanting.hasStoneType(equ, stoneType);
	  							stoneValue = (byte)Enchanting.calcStoneValue(stoneType, (byte)stoneLevel);
	  						}
	  	                	enchan.setEnchantingItemId(enchantingItemID);
	  	                	enchan.setArrtType(arrtType);
							enchan.setArrtValue(arrtValue);
							enchan.setStoneType(stoneType);
							enchan.setStoneValue(stoneValue);
  	                	}
						//属性攻
						Viany viany = equ.getViany();
						if(vianyValue.length != 3){
							write("属性攻信息长度不匹配。");
								return;
						}
						for(int i=0;i<vianyValue.length;i++){
							if(vianyValue[i]<0 || vianyValue[i]>10){
								write("属性攻等级错误。");
  								return;
							}
							viany.setViany((byte)(i+1), vianyValue[i]);
						}
						
		                att = ItemUtils.item2dbAttachment(equ,1);
		                write("精炼装备发送完毕");
	                }
                }
            }
            mailService.sendMail(destId,"",-1,"系统",title,content,att,0,true);
            log.info("Admin["+admin.getId()+"] SendMail Dest["+destId+"] title["+title+"] content["+content+"] attachment["+attachment+"] count[1] enhance");
        } catch (Exception ex) {
            write(ex.getMessage());
            ex.printStackTrace();
        }
    }
    private void admin_bbsDeletebyid(UWAPData data) throws Exception{
        int bbsId = data.readInt();
        int begin = data.readInt();
        int end = data.readInt();
        bbsService.deleteBbsbyid(bbsId,begin,end);
        if (bbsId == -3)
        	write("删除标题为：id为"+begin+"到"+end+"的bbs条目成功");
        else
        	write("删除标题为：bbsid为"+bbsId+"id为"+begin+"到"+end+"的bbs条目成功");
    }
    private void admin_bbsDeletebyuserid(UWAPData data) throws Exception{
    	int bbsId = data.readInt();
        int playerId = data.readInt();
        bbsService.deleteBbsbyplayerid(bbsId,playerId);
        if (bbsId == -3)
        	write("删除标题为：角色id为"+playerId+"的bbs条目成功");
        else
        	write("删除标题为：bbsid为"+bbsId+"角色id为"+playerId+"的bbs条目成功");
    }
    //mengjie add end
    private void admin_bbsSend(UWAPData data) throws Exception{
        int bbsId = data.readInt();
        String title = data.readString();
        String content = data.readString();
        int priority = data.readInt();
        bbsService.addBbs(bbsId,-1,"系统",title,content,priority);
        write("添加Bbs条目成功");
    }

    private void admin_bbsDelete(UWAPData data) throws Exception{
        int bbsId = data.readInt();
        Bbs bbs = bbsService.deleteBbs(bbsId);
        if(bbs==null)
            write("不存在此bbs条目");
        else
            write("删除标题为：["+bbs.getTitle()+"]的bbs条目成功");
    }

    private void admin_bbsList(UWAPData data) throws Exception{
        int bbsId = data.readInt();
        int pageSize = data.readShort();
        int pageNo = data.readInt();
        BbsService.BbsResult result = bbsService.getAdminBbsList(bbsId,pageSize,pageNo);
        if(result.bbs.length==0){
            write("没有更多的bbs存在");
            return;
        }
        UWAPSegment seg = new UWAPSegment(ServerConstants.ADMIN_BBS_LIST,
                                          data.getSerial());
        seg.writeInt(bbsId);
        seg.writeShort((short) pageSize);
        seg.writeInt(pageNo);
        seg.writeInt(result.pageCount);
        seg.writeShort((short) result.bbs.length);
        for (int i = 0; i < result.bbs.length; i++) {
            Bbs bbs = result.bbs[i];
            seg.writeInt(bbs.getId());
            seg.writeInt(bbs.getPlayerId());
            seg.writeString(bbs.getPlayerName());
            seg.writeString(bbs.getTitle());
            seg.writeString(bbs.getContent());
            seg.writeInt(bbs.getPriority());
            seg.writeString(Utils.getDateString(bbs.getPostTime()));
        }
        write(seg);
    }

    private void admin_ishopList(UWAPData data){

        StoreGroup group = StoreGroups.getStoreGroup(110);
        if(group==null){
            write("没有发现i币商品组");
            return;
        }

        StoreItem[] items = group.getItems();
        UWAPSegment seg = new UWAPSegment(ServerConstants.ADMIN_ISHOP_LIST);
        seg.writeInt(items.length);
        for(int i=0;i<items.length;i++){
            seg.writeInt(items[i].item.getItemId());
            seg.writeString(items[i].item.getName());
            seg.writeInt(items[i].price);
            seg.writeString(items[i].desc);
        }
        write(seg);
    }

    private void admin_ishopModify(UWAPData data) throws Exception{
        TaskNpc npc = TaskNpcs.getTaskNpc(41000);
        if(npc==null){
            write("没有发现i币商店");
            return;
        }
        TaskNpcType npcType = TaskNpcTypes.getTaskNpcType(npc.
                getType());
        StoreNpcType sNpc = (StoreNpcType) npcType;
        StoreGroup group = StoreGroups.getStoreGroup(sNpc.
                                                     getGroup());
        byte type = data.readByte();
        int itemId = data.readInt();
        IItemTemplate item = Items.getTemplate(itemId);
        if(item==null){
            write("物品ID错误");
            return;
        }
        if(type==1){
            int price = data.readInt();
            String desc = data.readString();
            StoreItem storeItem = new StoreItem();
            storeItem.item = item;
            storeItem.price = price;
            storeItem.desc = desc;
            group.addItem(storeItem);
            write("添加i币物品成功");
        }
        else if(type==2){
            boolean b = group.removeItem(itemId);
            if(b)
                write("删除i币物品成功");
            else
                write("删除i币物品错误");
        }

    }
    public void sendSosMessage(Mail mail) {
    	UWAPSegment seg = new UWAPSegment(ServerConstants.ADMIN_MAIL_LIST);
        seg.writeShort((short)1);
        seg.writeInt(1);
        seg.writeInt(1);
        seg.writeShort((short) 1);
        seg.writeInt(mail.getId());
        seg.writeInt(mail.getSourceId());
        seg.writeString(mail.getSourceName());
        seg.writeString(mail.getTitle());
        seg.writeString(mail.getContent());
        seg.writeString(Utils.getDateString(mail.getPostTime()));
        seg.writeBoolean(false);
        seg.writeBoolean(mail.getReaded());
        write(seg);
    }

    private void admin_mailList(UWAPData data){
        try {
            int playerId = data.readInt();
            short pageSize = data.readShort();
            int pageNo = data.readInt();
            int minId = 0;
            long startTime = 0;
        	long endTime = 0;
            try {
				minId = data.readInt();
				startTime = data.readLong();
				endTime = data.readLong();
			} catch (Exception e) {
			}
            MailList list = null;
            if(startTime == 0 && endTime == 0){
            	list = mailService.getMailList(playerId, pageSize, pageNo, minId);
            }else{
            	//时间传的是不带小时的 在这里endTime加上一天的时间
            	endTime += 24 * 60 * 60 * 1000L;
            	list = mailService.getMailList(playerId, pageSize, pageNo, minId, new Date(startTime), new Date(endTime));
            }
            int count = list.getCount();
            List l = list.getList();
            int size = l.size();
            int pageCount = count / pageSize;
            if (count % pageSize != 0) {
                pageCount++;
            }
            UWAPSegment seg = new UWAPSegment(ServerConstants.ADMIN_MAIL_LIST,
                                              data.getSerial());
            seg.writeShort(pageSize);
            seg.writeInt(pageNo);
            seg.writeInt(pageCount);
            seg.writeShort((short) size);
            for (int i = 0; i < size; i++) {
                Mail mail = (Mail) l.get(i);
                seg.writeInt(mail.getId());
                seg.writeInt(mail.getSourceId());
                seg.writeString(mail.getSourceName());
                seg.writeString(mail.getTitle());
                seg.writeString(mail.getContent());
                seg.writeString(Utils.getDateString(mail.getPostTime()));
                byte[] att = mail.getAttachment();
                if (att == null || att.length == 0) {
                    seg.writeBoolean(false);
                } else {
                    seg.writeBoolean(true);
                }
                seg.writeBoolean(mail.getReaded());
            }
            write(seg);
        } catch (MailException ex) {
            write(ex.getMessage());
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }
    }

    private void admin_mailSend(UWAPData data){
        try {
            int destId = data.readInt();
            String title = data.readString();
            String content = data.readString();
            int attachment = data.readInt();
            int count = data.readInt();
            byte[] att = new byte[0];
            if(attachment>0){
                IItemTemplate template = Items.getTemplate(attachment);
                if(template==null){
                	write("附件ID错误");
                }else{
                	IItem item = template.newInstance();
                	if(template.getType()!=IItem.TYPE_EQU&&(count<=0||count>99)){
                		write("数量错误");
                	}else{
                		if(item.getType()==IItem.TYPE_EQU){
                            item.setBinded(true);
                        }
                		att = ItemUtils.item2dbAttachment(item,count);
                		write("物品发送完毕");
                	}
                }
            }else if(attachment==-1){
                att = ItemUtils.money2dbAttachment(count);
                write("金钱发送完毕");
            }
            mailService.sendMail(destId,"",-1,"系统",title,content,att,0,true);
            log.info("Admin["+admin.getId()+"] SendMail Dest["+destId+"] title["+title+"] content["+content+"] attachment["+attachment+"] count["+count+"]");
        } catch (Exception ex) {
            write(ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void admin_mailDelete(UWAPData data) {
        try {
            int mailId = data.readInt();
            long time = data.readLong();
            if(time==-1){
                Mail mail = mailService.getMail(mailId);
                if (mail != null) {
                    mailService.deleteMail(mail);
                    write("删除信件成功");
                    adminService.updateSosMessageStatus(mailId, -1, sessionId);
                }
            }else{
                Date date = new Date(time);
                mailService.deleteMail(mailId,date);
            }
        } catch (DataAccessException ex) {
        } catch (IllegalAccessException ex) {
        }
    }
    private void admin_mailMarkDone(UWAPData data) {
        try {
            int mailId = data.readInt();
            Mail mail = mailService.getMail(mailId);
            if (mail != null) {
            	mail.setReaded(true);
            	mailService.saveMail(mail);
                write("标注信件成功");
                adminService.updateSosMessageStatus(mailId, 1, sessionId);
            }
        } catch (Exception ex) {
        }
    }
    public void updateSosMessageStatus(int id, int status) {
    	UWAPSegment seg = new UWAPSegment(ServerConstants.ADMIN_MAIL_STATUS_CHANGE);
        seg.writeInt(id);
        seg.writeInt(status);
        write(seg);
    }

    private void admin_battlefield(UWAPData data) throws Exception {
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        if (!checkAuth("battlefield")) {
            errorAuth();
            return;
        }
        byte type = data.readByte();

        if (type == 0) {
            int id = data.readInt();
            int forbidEnter = data.readInt();
            int forbid = data.readInt();
            int end = data.readInt();
//            int id = Integer.parseInt(command.getParam(1));
//            int forbidEnter = Integer.parseInt(command.getParam(2));
//            int forbid = Integer.parseInt(command.getParam(3));
//            int end = Integer.parseInt(command.getParam(4));
            long current = System.currentTimeMillis();

            try {
                battleField.start(current + forbidEnter * 60 * 1000L,
                                  current + forbid * 60 * 1000L,
                                  current + end * 60 * 1000L);
                write("战场初始化成功");
            } catch (BattleFieldException ex) {
                write(ex.getMessage());
            }
        } else if (type == 1) {
            try {
                battleField.forceStop();
                write("战场关闭成功");
            } catch (Exception ex) {
                write(ex.getMessage());
            }
        } else {
            write("无效战场指令");
        }
    }

    private void admin_forbid(UWAPData data) throws Exception {
        int playerId = data.readInt();
        int second = data.readInt();
        byte channel = data.readByte();
        if ((channel & 1) != 0) {
            UWAPSegment seg = new UWAPSegment(ServerConstants.ADMIN_FORBID);
            connectService.broadcast(seg);
        }
        if ((channel & 2) != 0) {
            auctionService.addForbiden(playerId, second);
        }
        if ((channel & 4) != 0) {
            mailService.addForbiden(playerId, second);
        }
        if ((channel & 8) != 0) {
        	chatService.forbiden(playerId, 128, second);
            write("阵营已经禁言");
        }
    }

    private void admin_modifyaccount(UWAPData data) throws Exception {
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        if (!checkAuth("modifyaccount")) {
            errorAuth();
            return;
        }
        int accountId = data.readInt();
        String password = data.readString();

        Server.instance.authSession.forward(data, getSessionId());
    }

    private void admin_playerlist(UWAPData data) throws Exception {
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        int accountId = data.readInt();
        Player[] players = playerService.getPlayerByAccountId(accountId);
        if (players.length == 0) {
            write("没找到角色");
        }
        UWAPSegment seg = new UWAPSegment(ServerConstants.ADMIN_PLAYERLIST);
        seg.write((byte) players.length);
        for (int i = 0; i < players.length; i++) {
            seg.writeInt(players[i].getId());
            seg.writeString(players[i].getPlayerName());
            seg.writeShort((short) players[i].getLevel());
            seg.write(players[i].getSex());
            seg.write(players[i].getReturnTimes());
        }
        write(seg);
    }

    public void accountInfoResult(AccountInfoOkMessage message,AccountInfoRequest request) throws Exception{

    	UWAPSegment seg = new UWAPSegment(ServerConstants.
                                          ADMIN_ACCOUNTINFO,
                                          request.getId(),
                                          request.getSessionId());
        seg.writeInt(message.getAccountId());
        seg.writeString(message.getName());
        seg.writeString(message.getPassword());
        seg.writeString(message.getPhone());

        if ("pip".equalsIgnoreCase(type)){
        	//mengjie add格式：手机；邮箱；身份证；自定义问题和答案。例：0110(0:未绑定；1：绑定)
            seg.writeString(accountbingingService.bingingstatus(String.valueOf(message.getAccountId())));
        }


        write(seg);
    }

    private void admin_accountinfo(UWAPData data) throws Exception {
        log.info("accountinfo start");
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        if (!checkAuth("accountinfo")) {
            errorAuth();
            return;
        }
        int id = data.readInt();
        if (id != -1) {
            WorldPlayer player = getWorldPlayerAndCatch(id);
            if (player != null) {
                if ("cmcc".equals(type)) {
                    UWAPSegment seg = new UWAPSegment(ServerConstants.
                            ADMIN_ACCOUNTINFO,
                            data.getSerial(),
                            getSessionId());
                    seg.writeInt(player.getAccountId());
                    seg.writeString("");
                    Server.instance.authSession.write(seg);
                    log.info("accountinfo sended1");
                } else {
                    AccountInfoMessage msg = new AccountInfoMessage(player.getAccountId(), "");
                    AccountInfoRequest request = new AccountInfoRequest(data.getSerial(), data.getSessionId(), this);
                    requestService.add(msg.getSerial(), request);
                    accountSkeleton.send(msg);
                }
            } else {
                write("找不到此用户");
            }
            releasePlayer(player);
        } else {
            WorldPlayer player = getWorldPlayerAndCatch(data.readString());
            if (player != null) {
                if ("cmcc".equals(type)) {
                    UWAPSegment seg = new UWAPSegment(ServerConstants.
                            ADMIN_ACCOUNTINFO,
                            data.getSerial(),
                            getSessionId());
                    seg.writeInt(player.getAccountId());
                    seg.writeString("");
                    Server.instance.authSession.write(seg);
                    log.info("accountinfo sended2");
                } else {
                    AccountInfoMessage msg = new AccountInfoMessage(player.getAccountId(), "");
                    AccountInfoRequest request = new AccountInfoRequest(data.getSerial(), data.getSessionId(), this);
                    requestService.add(msg.getSerial(), request);
                    accountSkeleton.send(msg);
                }
            } else {
                write("找不到此用户");
            }
            releasePlayer(player);
//            authSession.forward(data,getSessionId());
        }
    }

    private void admin_keepwatch(UWAPData data) throws Exception {
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        boolean keepwatch = data.readBoolean();
        isKeepWatch = keepwatch;
    }

    private void admin_auth(UWAPData data) throws Exception {
        UWAPSegment seg = new UWAPSegment(ServerConstants.ADMIN_AUTH);
        seg.writeString(admin.getAuth());
        write(seg);
    }

    public void admin_release_account(UWAPData data) throws Exception {
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        if (!checkAuth("releaseaccount")) {
            errorAuth();
            return;
        }
        int accountId = data.readInt();
        UWAPSegment seg = new UWAPSegment(ServerConstants.RELEASEACCOUNT);
        seg.writeInt(accountId);
        Server.instance.authSession.write(seg);
    }

    public void admin_addip(UWAPData data) throws Exception {
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        if (!checkAuth("addip")) {
            errorAuth();
            return;
        }
        int begin = data.readInt();
        int end = data.readInt();
        UWAPSegment seg = new UWAPSegment(ServerConstants.ADMIN_ADDIP);
        seg.writeInt(begin);
        seg.writeInt(end);
        connectService.broadcast(seg);
    }

    public void admin_delete(UWAPData data) throws Exception {
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        if (!checkAuth("delete")) {
            errorAuth();
            return;
        }
        int playerId = data.readInt();
        byte type = data.readByte();
        int itemId = data.readInt();
        int count = data.readInt();
        WorldPlayer player = getWorldPlayerAndCatch(playerId);
        try {
            if (player != null) {
                if (type != IItem.TYPE_PET) {
                    IItem deleted = player.completeRemoveItem(itemId, count, null);
                    if (deleted == null) {
                        write("物品ID或者数量错误");
                    } else {
                        player.reset();
                        playerService.savePlayer(player);
                        write("成功删除物品");
                    }
                } else { //删除宠物
                    Pet pet = player.getPet(count);
                    if (pet == null) {
                        write("宠物不存在");
                    } else {
                        player.removePet(pet);
                        write("成功删除宠物");
                    }
                }
            } else {
                write("找不到指定玩家");
            }
        } finally {
            releasePlayer(player);
        }
    }

    public void admin_add(UWAPData data) throws Exception {
        int playerId = data.readInt();
        int type = data.readByte();
        int itmeId = data.readInt();
        int count = data.readInt();
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        if (!checkAuth("add")) {
            errorAuth();
            return;
        }
        WorldPlayer player = getWorldPlayerAndCatch(playerId);
        try {
            if (player != null) {
                if (type != IItem.TYPE_PET) {
                	count = player.addItem(itmeId, count, null, player.getClientDataVersion());
                    if (count == 0) {
                        write("添加物品错误");
                    } else {
                        player.reset();
                        playerService.savePlayer(player);
                        write("成功添加物品");
                        log.info("admin name[" + (admin != null ? admin.getName() : "null") + "] add command playerID[" + playerId + "] itemId[" + itmeId + "] count[" + count);
                    }
                }
            } else {
                write("找不到指定玩家");
            }
        } finally {
            releasePlayer(player);
        }
    }

    public void admin_forbid_account(UWAPData data) throws Exception {
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        if (!checkAuth("forbidaccount")) {
            errorAuth();
            return;
        }
        byte type = data.readByte();
        int id = data.readInt();
        if (type == 1) {
            String cause = data.readString();
            DateFormat df = DateFormat.getDateInstance();
            if ("cmcc".equals(type)) {
                UWAPSegment seg = new UWAPSegment(ServerConstants.FORBID);
                seg.write(type);
                seg.writeInt(id);

                seg.writeString(df.format(new Date()) + " " + admin.getId() + " " +
                                admin.getName() + cause);
                Server.instance.authSession.write(seg);
            } else {
                ChangeStatusMessage message = new ChangeStatusMessage(id, 0,
                        df.format(new Date()) + " " + admin.getId() + " " +
                        admin.getName() + cause);
                accountSkeleton.send(message);
            }
        } else if (type == 2) {
            if ("cmcc".equals(type)) {
                UWAPSegment seg = new UWAPSegment(ServerConstants.FORBID);
                seg.write(type);
                seg.writeInt(id);
                Server.instance.authSession.write(seg);
            } else {
                ChangeStatusMessage message = new ChangeStatusMessage(id, 1, "");
                accountSkeleton.send(message);
            }
        }

    }

    public void admin_modify(UWAPData data) throws Exception {
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        if (!checkAuth("modify")) {
            errorAuth();
            return;
        }
        int id = data.readInt();
        byte pro = data.readByte();
        int value = data.readInt();
        WorldPlayer player = getWorldPlayerAndCatch(id);
        try {
            if (player != null) {
                if (pro == PRO_MAPID) {
                    player.setMapId((short) value);
                    playerService.savePlayer(player);
                } else if (pro == PRO_X) {
                    player.setX((short) value);
                    playerService.savePlayer(player);
                } else if (pro == PRO_Y) {
                    player.setY((short) value);
                    playerService.savePlayer(player);
                } else if (pro == PRO_FACE) {
                    player.setFace((byte) value);
                    playerService.savePlayer(player);
                } else if (pro == PRO_LEVEL) {
                    player.setLevel(value);
                    playerService.savePlayer(player);
                } else if (pro == PRO_EXP) {
                    player.setExp(value);
                    playerService.savePlayer(player);
                } else if (pro == PRO_MONEY) {
                    player.setMoeny(value);
                    playerService.savePlayer(player);
                } else if (pro == PRO_STRENGTH) {
                    player.setStrength(value);
                    playerService.savePlayer(player);
                } else if (pro == PRO_AGILITY) {
                    player.setAgility(value);
                    playerService.savePlayer(player);
                } else if (pro == PRO_VITALITY) {
                    player.setVitality(value);
                    playerService.savePlayer(player);
                } else if (pro == PRO_INTELLIGENCE) {
                    player.setIntelligence(value);
                    playerService.savePlayer(player);
                } else if (pro == PRO_HP) {
                    player.setHp((short) value);
                    playerService.savePlayer(player);
                } else if (pro == PRO_MP) {
                    player.setMp((short) value);
                    playerService.savePlayer(player);
                } else if (pro == PRO_LEAVEPOINT) {
                    player.setLeavePoints(value);
                    playerService.savePlayer(player);
                } else if (pro == PRO_LEAVEABILITYPOINT) {
                    player.setPoint(value);
                    playerService.savePlayer(player);
                } else if (pro == PRO_GRIDSIZE) {
                    player.setGridSize((short) value);
                    playerService.savePlayer(player);
                } else if (pro == PRO_ADDEDGRIDSIZE) {
                    player.setAddedGridSize(value);
                    playerService.savePlayer(player);

                } else if (pro == PRO_PETSIZE) {
                    player.setPetSize(value);
                    playerService.savePlayer(player);
                } else {
                    write("没有找到指定属性");
                }
            } else {
                write("没找到指定对象");
            }
        } finally {
            releasePlayer(player);
        }
    }

    public void admin_say(UWAPData data) throws Exception {
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        int srcId = data.readInt();
        String srcName = data.readString();
        int channel = data.readInt();
        int value = data.readInt();
        String msg = data.readString();
        UWAPSegment seg = chatService.adminMessage(srcId, srcName, channel,
                value, msg);
        if (seg != null) {
        	adminService.onChatMessage(-ISendMessage.GM, 0, 0, seg);
        }
        UWAPSegment segResult = new UWAPSegment(ServerConstants.ADMIN_ACKNOWLEDGEMENT, data.getSerial(), data.getSessionId());
        segResult.writeInt(data.getAppType());
        write(segResult);
    }

    public void admin_mute(UWAPData data) throws Exception {
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        if (!checkAuth("mute")) {
            errorAuth();
            return;
        }
        int id = data.readInt();
        int second = data.readInt();
        byte mask = data.readByte();
        chatService.forbiden(id, mask, second);
        write("已经禁言");
    }

    public void admin_kick(UWAPData data) throws Exception {
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        if (!checkAuth("kick")) {
            errorAuth();
            return;
        }
        int id = data.readInt();
        int second = data.readInt();
        playerService.addForbiden(id, second);
        connectService.kick(id);
    }

    public void admin_who(UWAPData data) throws Exception {
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        short mapId = data.readShort();
        if (mapId == 0) {
        	write("当前在线玩家共: " + playerService.size());
        	return;
        }
        WorldPlayer[] players = playerService.getPlayers();
        WorldPlayer[] ps = getPlayersByMapId(mapId, players);
        UWAPSegment seg = new UWAPSegment(ServerConstants.ADMIN_WHO,
                                          data.getSerial());
        seg.writeShort((short) ps.length);
        for (int i = 0; i < ps.length; i++) {
            seg.writeInt(ps[i].getId());
            seg.writeString(ps[i].getPlayerName());
            seg.writeInt(ps[i].getLevel());
            seg.writeShort(ps[i].getMapId());
            seg.writeShort(ps[i].getX());
            seg.writeShort(ps[i].getY());
            seg.writeString(ps[i].getTongName());
            seg.writeBoolean(ps[i].online());
            seg.write(ps[i].getCamp());
        }
        write(seg);
    }

    private WorldPlayer[] getPlayersByMapId(short mapId, WorldPlayer[] players) {
        if (mapId == -1)
            return players;
        List l = new ArrayList(players.length);
        for (int i = 0; i < players.length; i++) {
            if (players[i].getMapId() == mapId)
                l.add(players[i]);
        }
        WorldPlayer[] ret = new WorldPlayer[l.size()];
        l.toArray(ret);
        return ret;
    }


    public void admin_show_player(UWAPData data) throws Exception {
        String name = data.readString();
        int playerId = data.readInt();
        if (playerId != -1) {
            WorldPlayer player = getWorldPlayerAndCatch(playerId);
            try {
                if (player == null) {
                    write("没找到此用户");
                } else {
                    UWAPSegment seg = new UWAPSegment(ServerConstants.
                            ADMIN_SHOW_PLAYER,
                            data.getSerial());
                    seg.writeInt(player.getId());
                    seg.writeString(player.getPlayerName());
                    seg.writeShort(player.getMapId());
                    seg.writeShort(player.getX());
                    seg.writeShort(player.getY());
                    seg.write(player.toClientBytes(1));
                    // 增加帮派id和队伍id，为今后跟踪用
                    Team tm = player.getTeam();
                    if (tm != null) {
                        seg.writeInt(tm.getId());
                    } else {
                    	seg.writeInt(-1);
                    }
                    seg.writeInt(player.getPlayer().getTongId());
                    seg.writeInt(player.getArenaV1Id());
                    seg.writeInt(player.getArenaLevel());
                    seg.writeInt(player.getArenaPoint());
                    //mengjie add pet EQU
                    try {
                    	Pet[] pets = player.getPets();
                        if ((pets != null) && (pets.length > 0)){
                        	seg.writeShort((short) pets.length);
                        	for(int i=0;i<pets.length;i++){
                        		int Equcount = 0;
                        		IEquipment[] petEqu_tmp = new IEquipment[9];
                        		IEquipment[] petEqu = pets[i].getUsedEquipments2();
                        		for (int j=0;j<petEqu.length;j++){
                        			if (petEqu[j] != null){
                        				petEqu_tmp[Equcount] = petEqu[j];
                        				Equcount++;
                        			}
                        		}
                        		seg.writeInt(pets[i].getId());//宠物id
                        		seg.writeShort((short) Equcount);//宠物穿着装备的数量
                        		for (int j=0;j<Equcount;j++){
                        			petEqu_tmp[j].setDataVersion(1);
                        			byte []tmp = petEqu_tmp[j].toClientBytesWithLevel(pets[i].getLevel());
                        			seg.writeShort((short) tmp.length);
                        			seg.write(tmp);
                        		}
                        	}
                        }else{
                        	seg.writeShort((short) 0);
                        }
                    }catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    seg.write(player.getCamp());
                    //增加形象橱窗
                    ArrayList image = (ArrayList)player.getImage();
                    if(image != null){
                    	seg.writeInt(image.size());//形象个数
                        for (int i = 0; i < image.size(); i ++) {
                        	RoleFaceData roleFace = (RoleFaceData)image.get(i);
                        	if (roleFace.getExpiration() > RoleFaceData.EXPIRED) {		
                        		seg.write((byte)1);		// 有时效性的
                        	} else if (roleFace.getExpiration() == RoleFaceData.EXPIRED) {	
                        		seg.write((byte)2);// 已过期
                        	} else {	
                        		seg.write((byte)0);		// 永久
                        	}
                        	seg.writeInt(roleFace.getFace());
                        	if (roleFace.getFace() == player.getFace()) {
                    			seg.writeString(roleFace.getName() + " (装备中)");
                        	} else if (roleFace.getExpiration() == RoleFaceData.EXPIRED) {
                        		seg.writeString(roleFace.getName() + " (已过期)");
                        	} else {
                        		seg.writeString(roleFace.getName());
                        	}
                        }
                    }else{
                    	seg.writeInt(0);
                    }
                    
                  //增加称号橱窗
                    ArrayList roleTitle = (ArrayList)player.getRoleTitle();
                    if(roleTitle != null){
                        seg.writeInt(roleTitle.size());
                        for(int i = 0; i < roleTitle.size(); i ++){
                        	String roleTitleStr = (String)roleTitle.get(i);
                        	if(roleTitleStr.equals(player.getTitle())){
                        		seg.writeString(roleTitleStr + " (当前)");
                        	}else{
                        		seg.writeString(roleTitleStr);
                        	}
                        }
                    } else {
                    	seg.writeInt(0);
                    }
                    //增加打造熟练度显示
                    seg.writeInt(player.getSkillPoint2());
                    //增加家园仓库内容
                    try {
	                    HouseData hd = houseModel.getHouseByPlayerId(player.getId());
	                    Grid[] hditems = hd.getItems();
	                    seg.write((byte) hditems.length);
	                    for (int j=0;j<hditems.length;j++){
	                    	seg.writeString(hditems[j].item.getName());
	                    	seg.writeInt(hditems[j].count);
                		}
                    }catch (Exception ex) {
                    	seg.write((byte) 0);
                    }
                    //增加店铺仓库内容
                    try {
                    	ShopData[] shops = shopService.getShops(player.getId());
                    	seg.write((byte) shops.length);
                    	for (int j=0;j<shops.length;j++){
                    		Grid[] shopitems = shops[j].getItems();
                    		seg.write((byte) shopitems.length);
    	                    for (int k=0;k<shopitems.length;k++){
    	                    	seg.writeString(shopitems[k].item.getName());
    	                    	seg.writeInt(shopitems[k].count);
                    		}
                		}
                    }catch (Exception ex) {
                    	seg.write((byte) 0);
                    }
                    // 下发玩家VIP等级服务器存储是从0开始的，下发到客户端是从1开始，方便体验
//                    int toClientVip = 0;
//                    if (player.getVipValidTime() != null && player.getVipValidTime().equals("") == false) {
//            			long validTime = player.getVipValidTime().getTime();
//            			long timeNow = new Date().getTime();
//            			if (validTime >= timeNow) {
//            				toClientVip = player.getVipLevel();
//            			}
//            		}
//                    GM已将VIP等级加上1了 这里返回与数据库一致的值
                    /**
                     * 2012年7月30日10:04:31
                     * zxyu 使用新的VIP等级
                     */
                    int toClientVip = player.getVipNewLevel();
                    seg.writeInt(toClientVip);
                    seg.writeInt(player.getAllLife());
                    write(seg);
                }
            } finally {
                releasePlayer(player);
            }
        } else {
            WorldPlayer player = getWorldPlayerAndCatch(name);
            try {
                if (player == null) {
                    write("没找到此用户");
                } else {
                    UWAPSegment seg = new UWAPSegment(ServerConstants.
                            ADMIN_SHOW_PLAYER,
                            data.getSerial());
                    seg.writeInt(player.getId());
                    seg.writeString(player.getPlayerName());
                    seg.writeShort(player.getMapId());
                    seg.writeShort(player.getX());
                    seg.writeShort(player.getY());
                    seg.write(player.toClientBytes(1));
                    // 增加帮派id和队伍id，为今后跟踪用
                    Team tm = player.getTeam();
                    if (tm != null) {
                        seg.writeInt(tm.getId());
                    } else {
                    	seg.writeInt(-1);
                    }
                    seg.writeInt(player.getPlayer().getTongId());
                    seg.writeInt(player.getArenaV1Id());
                    seg.writeInt(player.getArenaLevel());
                    seg.writeInt(player.getArenaPoint());
                    //mengjie add pet EQU
                    try {
                    	Pet[] pets = player.getPets();
                        if ((pets != null) && (pets.length > 0)){
                        	seg.writeShort((short) pets.length);
                        	for(int i=0;i<pets.length;i++){
                        		int Equcount = 0;
                        		IEquipment[] petEqu_tmp = new IEquipment[9];
                        		IEquipment[] petEqu = pets[i].getUsedEquipments2();
                        		for (int j=0;j<petEqu.length;j++){
                        			if (petEqu[j] != null){
                        				petEqu_tmp[Equcount] = petEqu[j];
                        				Equcount++;
                        			}
                        		}
                        		seg.writeInt(pets[i].getId());//宠物id
                        		seg.writeShort((short) Equcount);//宠物穿着装备的数量
                        		for (int j=0;j<Equcount;j++){
                        			petEqu_tmp[j].setDataVersion(1);
                        			byte []tmp = petEqu_tmp[j].toClientBytesWithLevel(pets[i].getLevel());
                        			seg.writeShort((short) tmp.length);
                        			seg.write(tmp);
                        		}
                        	}
                        }else{
                        	seg.writeShort((short) 0);
                        }
                    }catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    seg.write(player.getCamp());
                  //增加形象橱窗
                    ArrayList image = (ArrayList)player.getImage();
                    if(image != null){
                    	seg.writeInt(image.size());//形象个数
                        for (int i = 0; i < image.size(); i ++) {
                        	RoleFaceData roleFace = (RoleFaceData)image.get(i);
                        	if (roleFace.getExpiration() > RoleFaceData.EXPIRED) {		
                        		seg.write((byte)1);		// 有时效性的
                        	} else if (roleFace.getExpiration() == RoleFaceData.EXPIRED) {	
                        		seg.write((byte)2);// 已过期
                        	} else {	
                        		seg.write((byte)0);		// 永久
                        	}
                        	seg.writeInt(roleFace.getFace());
                        	if (roleFace.getFace() == player.getFace()) {
                    			seg.writeString(roleFace.getName() + " (装备中)");
                        	} else if (roleFace.getExpiration() == RoleFaceData.EXPIRED) {
                        		seg.writeString(roleFace.getName() + " (已过期)");
                        	} else {
                        		seg.writeString(roleFace.getName());
                        	}
                        }
                    }else{
                    	seg.writeInt(0);
                    }
                    
                  //增加称号橱窗
                    ArrayList roleTitle = (ArrayList)player.getRoleTitle();
                    if(roleTitle != null){
                        seg.writeInt(roleTitle.size());
                        for(int i = 0; i < roleTitle.size(); i ++){
                        	String roleTitleStr = (String)roleTitle.get(i);
                        	if(roleTitleStr.equals(player.getTitle())){
                        		seg.writeString(roleTitleStr + " (当前)");
                        	}else{
                        		seg.writeString(roleTitleStr);
                        	}
                        }
                    } else {
                    	seg.writeInt(0);
                    }
                  //增加打造熟练度显示
                    seg.writeInt(player.getSkillPoint2());
                    //增加家园仓库内容
                    try {
	                    HouseData hd = houseModel.getHouseByPlayerId(player.getId());
	                    Grid[] hditems = hd.getItems();
	                    seg.write((byte) hditems.length);
	                    for (int j=0;j<hditems.length;j++){
	                    	seg.writeString(hditems[j].item.getName());
	                    	seg.writeInt(hditems[j].count);
                		}
                    }catch (Exception ex) {
                    	seg.write((byte) 0);
                    }
                    //增加店铺仓库内容
                    try {
                    	ShopData[] shops = shopService.getShops(player.getId());
                    	seg.write((byte) shops.length);
                    	for (int j=0;j<shops.length;j++){
                    		Grid[] shopitems = shops[j].getItems();
                    		seg.write((byte) shopitems.length);
    	                    for (int k=0;k<shopitems.length;k++){
    	                    	seg.writeString(shopitems[k].item.getName());
    	                    	seg.writeInt(shopitems[k].count);
                    		}
                		}
                    }catch (Exception ex) {
                    	seg.write((byte) 0);
                    }
                    // 下发玩家VIP等级服务器存储是从0开始的，下发到客户端是从1开始，方便体验
//                    int toClientVip = 0;
//                    if (player.getVipValidTime() != null && player.getVipValidTime().equals("") == false) {
//            			long validTime = player.getVipValidTime().getTime();
//            			long timeNow = new Date().getTime();
//            			if (validTime >= timeNow) {
//            				toClientVip = player.getVipLevel();
//            			}
//            		}
                    //GM已将VIP等级加上1了 这里返回与数据库一致的值
                    /**
                     * 2012年7月30日10:04:31
                     * zxyu 使用新的VIP等级
                     */
                    int toClientVip = player.getVipNewLevel();
                    seg.writeInt(toClientVip);
                    seg.writeInt(player.getAllLife());
                    write(seg);
                }
            } finally {
                releasePlayer(player);
            }
        }
    }

    public void admin_login(UWAPData data) throws Exception {
    	if (admin != null) {
    		adminService.unRegistry(this);
    	}
        String name = data.readString();
        String password = data.readString();
        admin = adminService.getAdmin(name, password);
        if (admin == null) {
            write("用户密码错误");
        } else {
            adminService.registry(this);
            UWAPSegment seg = new UWAPSegment(ServerConstants.ADMIN_AUTH);
            seg.writeString(admin.getAuth());
            write(seg);
            write("登陆成功");
        }

    }

    public void admin_script(UWAPData data) throws Exception {
        int id = data.readInt();
        short taskId = data.readShort();
        String[] parameters = data.readStrings();
        WorldPlayer player = playerService.getWorldPlayer(id);
        if (player == null) {
            write("玩家不在线");
            return;
        }

        if (parameters.length == 0) {
            byte[] bytes = stageService.getTaskBytes(taskId,player.getLevel());
            if (bytes == null) {
                write("脚本不存在");
                return;
            }
            UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK);
            seg.writeShort(taskId);
            seg.writeShort((short) 2);
            seg.write(bytes);
            connectService.writeTo(seg, player.getId());
        } else {

            byte[] bytes = stageService.getTaskBytes(taskId, parameters);
            if (bytes == null) {
                write("脚本不存在");
                return;
            }
            UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK);
            seg.writeShort(taskId);
            seg.writeShort((short) 2);
            seg.write(bytes);
            connectService.writeTo(seg, player.getId());
        }
    }

    public int getId() {
        return admin.getId();
    }

    public Admin getAdmin() {
        return admin;
    }

    public void receiveMessage(int src, String srcName, String msg) {
        UWAPSegment seg = new UWAPSegment(ServerConstants.ADMIN_SAY);
        seg.writeInt(src);
        seg.writeString(srcName);
        seg.writeString(msg);
        write(seg);
    }

    public void handleServer(Packet packet) {
        try {
            UWAPData data = packet.datas[0];
            byte type = data.getAppType();
            switch (type) {
                case ServerConstants.ADMIN_ACCOUNTINFO:
                    reply(data);
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public void command(UWAPData data) throws Exception {
        try {
			String s = data.readString();
			Command command = new Command(s);
			command.setSessionId(data.getSessionId());
			String methodName = command.getCommand();
			if (methodName != null) {
			    invoke(methodName, command);
			} else {
			    write("命令错误");
			}
		} catch (Exception e) {
			write(e.getMessage());
			e.printStackTrace();
		}
//        String s = data.readString();
//        Command command = new Command(s);
//        String c = command.getCommand();
//        String methodName = getMethodName(c);
//        if (methodName != null) {
//            invoke(methodName, command);
//        }
    }

    public void maintance(Command command) throws Exception{
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        if(command.getParamCount()!=1){
            write("参数错误");
            return;
        }
        if("true".equals(command.getParam(0))){
            UWAPSegment seg = new UWAPSegment(ServerConstants.MAINTANCE);
            seg.writeBoolean(true);
            connectService.broadcast(seg);
            write("maintance true");
        }
        else if("false".equals(command.getParam(0))){
            UWAPSegment seg = new UWAPSegment(ServerConstants.MAINTANCE);
            seg.writeBoolean(false);
            connectService.broadcast(seg);
            write("maintance false");
        }
    }

    public void batchcommand(UWAPData data) throws Exception {
        String s = data.readString();
        StringTokenizer st = new StringTokenizer(s, ";");
        while (st.hasMoreTokens()) {
            String sCommand = st.nextToken();
            Command command = new Command(sCommand);
            String methodName = command.getCommand();
            if (methodName != null) {
                try {
                    invoke(methodName, command);
                    write(sCommand + " ok");
                } catch (Exception ex) {
                    write(sCommand + " fail");
                }
            } else {
                write(sCommand + " not found");
            }
        }
    }

  
    private void invoke(String methodName, Command command) throws Exception {
    	 Method method = null;
         if ("?".equals(methodName)) { // 特殊支持帮助命令
         	help(command);
         	return;
         }
     	IAdminFunction func = adminFuncs.get(methodName);
     	if (func == null) {
     		try {
     			method = AdminSession.class.getDeclaredMethod(methodName,
     					new Class[] {Command.class});
     			if (method != null) {
     				method.invoke(this, new Object[] {command});
     				return;
     			}
     		} catch (NoSuchMethodException e) {
        		String engineName = "com.pip.itimes.server.world.admin." + methodName;
				try {
					Class kls = Class.forName(engineName);
            		func = (IAdminFunction)kls.newInstance();
            		registAdminFunction(methodName, func);
				} catch (ClassNotFoundException e1) {
				}
    		}
    	}
    	if (func != null) {
    		func.execCommand(this, command);
    	} else {
    		write("不支持命令: [" + methodName + "]. 可以使用 \"?\" 命令查询所支持命令列表");
		}        
    }
    private String help(Command command) {
    	int asid = command.getSessionId();
    	Admin admin = getAdmin();
    	if (command == null) {
    		return "提供管理员命令帮助信息\n" +
    				"   命令格式: ? [<命令>]\n" +
    				"   说明:当没有后面参数时列出所有现存命令,否则提供参数指定命令的详细帮助.";
    	} else if (command.getParamCount() == 0) {
	    	StringBuffer buf = new StringBuffer("支持的管理员命令: 可使用 ? <命令> 查看命令详细");
	    	ArrayList<String> commands = new ArrayList<String>(); 
	    	// 抽取所有本类中声明的以command为参数的命令作为命令名称列出
	    	for (Method m : AdminSession.class.getDeclaredMethods()) {
	    		Class []params = m.getParameterTypes();
	    		if (params.length == 1 && params[0] == Command.class) {
	    			commands.add(m.getName());
	    		}
	    	}
	    	// 将已经注册的管理员命令引擎列出
	    	for (String s : adminFuncs.keySet()) {
	    		IAdminFunction fun = adminFuncs.get(s);
	    		if (fun.canExecute(admin)) {
	    			commands.add(s);
	    		}
	    	}
	    	// 将所有命令排序方便浏览
	    	Collections.sort(commands, comparator);
	    	int maxCmdLen = 0;
	    	for (int i = 0; i < commands.size(); i++) {
	    		int k = commands.get(i).length();
	    		if (k > maxCmdLen) {
	    			maxCmdLen = k;
	    		}
	    	}
	    	maxCmdLen++;
	    	// 按行宽 120 为限,尽可能多列显示
	    	int col = maxCmdLen == 0 ? 20 : 120 / maxCmdLen;
	    	if (col < 1) {
	    		col = 1;
	    	}
	    	int n = commands.size();
	    	int m = (n + col - 1) / col;
	    	for (int i = 0; i < m; i++) {
    			buf.append("\n ");
	    		for (int j = i; j < n; j += m) {
		    		String s = commands.get(j) + "                                          ";
		    		buf.append(s.substring(0, maxCmdLen));
	    		}
	    	}
	    	write(buf.toString());
    	} else {
    		StringBuffer buf = new StringBuffer();
    		for (int i = 0; i < command.getParamCount(); i++) {
    			String name = command.getParam(i);
				buf.append("\n" + name + ": ");
    			try {
    				IAdminFunction func = adminFuncs.get(name);
    				if (func != null) {
    					buf.append(func.getHelp());
    				} else {
						Method m = AdminSession.class.getDeclaredMethod(name, new Class[] { Command.class });
						Class retType = m.getReturnType();
						if (retType == String.class) {
							buf.append(m.invoke(this, new Object[]{null}));
						} else {
							buf.append("此命令没有详细帮助信息,如命令名称不够明确,请反应给技术.");
						}
    				}
				} catch (Exception e) {
					e.printStackTrace();
					buf.append("没有此命令");
				}
    			
    		}
    		write(buf.toString());
    	}
    	return null;
    }
    private boolean checkLogined() {
        if (admin == null)
            return false;
        return true;
    }


    private boolean checkAuth(String auth) {
        return admin.hasAuth(auth);
    }

    private void auction(Command command){
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        if(command.getParamCount()!=5){
            write("参数不对");
        }else{
            int itemId = Integer.parseInt(command.getParam(0));
            int count = Integer.parseInt(command.getParam(1));
            int startPrice = Integer.parseInt(command.getParam(2));
            int endPrice = Integer.parseInt(command.getParam(3));
            int areaId = Integer.parseInt(command.getParam(4));
            IItemTemplate template = Items.getTemplate(itemId);
            if(template==null){
                write("物品id错误");
            }else{
                if (endPrice != -1 && (startPrice > endPrice)) {
                    write("出价错误");
                }else{

                    IItem item = template.newInstance();
                    try {
                        auctionService.addAuction( -1, "系统", item, count, startPrice, endPrice, (short) areaId);
                        write("添加拍卖物品：" + item.getName() + "成功");
                    } catch (AuctionException ex) {
                        write(ex.getMessage());
                    }
                }
            }
        }
    }

    private void maxplayer(Command command) {
        if (command.getParamCount() < 1) {
            write("参数不对");
        } else {
            String id = command.getParam(0);
            int count = Integer.parseInt(command.getParam(1));
            ConnectSession[] sessions = connectService.getConnectSession();
            for(int i=0;i<sessions.length;i++){
                if(sessions[i]!=null){
                    sessions[i].setMaxPlayer(count);
                }
            }
//            UWAPSegment seg = new UWAPSegment(ServerConstants.MAXPLAYER);
//            seg.writeInt(count);
//            connectService.write(id, seg);
        }
    }

    private void addip(Command command) {
        if (command.getParamCount() < 1) {
            write("参数不对");
        } else {
            String ipString = command.getParam(0);
            int begin = strToIP(ipString);
            UWAPSegment seg = new UWAPSegment(ServerConstants.ADMIN_ADDIP);
            seg.writeInt(begin);
            seg.writeInt(begin);
            connectService.broadcast(seg);
        }
    }

    public int strToIP(String s) {
        String[] secs = s.split("\\.");
        return ((Integer.parseInt(secs[0]) << 24) & 0xFF000000) |
                ((Integer.parseInt(secs[1]) << 16) & 0xFF0000) |
                ((Integer.parseInt(secs[2]) << 8) & 0xFF00) |
                (Integer.parseInt(secs[3]) & 0xFF);
    }

    private void login(Command command) {
        if (command.getParamCount() < 2) {
            write("参数不对");
        } else {
            admin = adminService.getAdmin(command.getParam(0),
                                          command.getParam(1));
            if (admin == null) {
                write("用户名或者密码错误");
            } else {
            	log.info("login name[" + command.getParam(0));
                write("已经登陆");
            }
        }
    }

    private void show(Command command) {
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        if (command.getParamCount() < 1) {
            errorArgument();
            return;
        }
        WorldPlayer player = getWorldPlayerAndCatch(command.getParam(0));
        String outputstr = "";
        try {
            if (player == null) {
            	outputstr = "没有找到指定用户";
                
            } else {
            	outputstr = Utils.getPlayerString(player);
            }
          //增加打造熟练度显示
            outputstr = outputstr + "\n技能熟练点：" +player.getSkillPoint2();
          //增加家园仓库内容
            outputstr = outputstr + "\n家园仓库内容-仓位[";
            try {
                HouseData hd = houseModel.getHouseByPlayerId(player.getId());
                Grid[] hditems = hd.getItems();
                outputstr = outputstr + hditems.length + "]";
                for (int j=0;j<hditems.length;j++){
                	outputstr = outputstr + hditems[j].item.getName() + "x" + hditems[j].count + ";";
        		}
            }catch (Exception ex) {
            	outputstr = outputstr + "0]";
            }
          //增加店铺仓库内容
            outputstr = outputstr + "\n家园店铺内容-店铺[";
            try {
            	ShopData[] shops = shopService.getShops(player.getId());
            	outputstr = outputstr + shops.length + "]";
            	for (int j=0;j<shops.length;j++){
            		Grid[] shopitems = shops[j].getItems();
            		outputstr = outputstr + "(" + (j+1) + ")[" + shopitems.length + "]";
                    for (int k=0;k<shopitems.length;k++){
                    	outputstr = outputstr + shopitems[k].item.getName() + "x" + shopitems[k].count + ";";
            		}
        		}
            }catch (Exception ex) {
            	outputstr = outputstr + "0]";
            }
            // 下发玩家VIP等级服务器存储是从0开始的，下发到客户端是从1开始，方便体验
//            int toClientVip = 1;
//            if (player.getVipValidTime() != null && player.getVipValidTime().equals("") == false) {
//    			long validTime = player.getVipValidTime().getTime();
//    			long timeNow = new Date().getTime();
//    			if (validTime >= timeNow) {
//    				toClientVip = player.getVipLevel() + 1;
//    			}
//    		}
            /**
             * 2012年7月30日10:04:31
             * zxyu 使用新的VIP等级
             */
            int toClientVip = player.getVipNewLevel();
            outputstr = outputstr + "\nVIP等级=" + toClientVip;
            outputstr = outputstr + "\n活力=" + player.getAllLife();
        } finally {
            releasePlayer(player);
        }
        write(outputstr);
    }
    
    private void show4id(Command command) {
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        if (command.getParamCount() < 1) {
            errorArgument();
            return;
        }
        WorldPlayer player = getWorldPlayerAndCatch(Integer.parseInt(command.getParam(0)));
        String outputstr = "";
        try {
            if (player == null) {
            	outputstr = "没有找到指定用户";
                
            } else {
            	outputstr = Utils.getPlayerString(player);
            }
          //增加打造熟练度显示
            outputstr = outputstr + "\n技能熟练点：" +player.getSkillPoint2();
          //增加家园仓库内容
            outputstr = outputstr + "\n家园仓库内容-仓位[";
            try {
                HouseData hd = houseModel.getHouseByPlayerId(player.getId());
                Grid[] hditems = hd.getItems();
                outputstr = outputstr + hditems.length + "]";
                for (int j=0;j<hditems.length;j++){
                	outputstr = outputstr + hditems[j].item.getName() + "x" + hditems[j].count + ";";
        		}
            }catch (Exception ex) {
            	outputstr = outputstr + "0]";
            }
          //增加店铺仓库内容
            outputstr = outputstr + "\n家园店铺内容-店铺[";
            try {
            	ShopData[] shops = shopService.getShops(player.getId());
            	outputstr = outputstr + shops.length + "]";
            	for (int j=0;j<shops.length;j++){
            		Grid[] shopitems = shops[j].getItems();
            		outputstr = outputstr + "(" + (j+1) + ")[" + shopitems.length + "]";
                    for (int k=0;k<shopitems.length;k++){
                    	outputstr = outputstr + shopitems[k].item.getName() + "x" + shopitems[k].count + ";";
            		}
        		}
            }catch (Exception ex) {
            	outputstr = outputstr + "0]";
            }
            // 下发玩家VIP等级服务器存储是从0开始的，下发到客户端是从1开始，方便体验
            int toClientVip = 1;
            if (player.getVipValidTime() != null && player.getVipValidTime().equals("") == false) {
    			long validTime = player.getVipValidTime().getTime();
    			long timeNow = new Date().getTime();
    			if (validTime >= timeNow) {
    				toClientVip = player.getVipLevel() + 1;
    			}
    		}
            outputstr = outputstr + "\nVIP等级=" + toClientVip;
            outputstr = outputstr + "\n活力=" + player.getAllLife();
        } finally {
            releasePlayer(player);
        }
        write(outputstr);
    }


    private void kick(Command command) {
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        if (command.getParamCount() < 2) {
            errorArgument();
            return;
        }
        int id = Integer.parseInt(command.getParam(0));
        int second = Integer.parseInt(command.getParam(1));
        playerService.addForbiden(id, second);
        connectService.kick(id);
    }

    private void online(Command command) {
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        ConnectSession[] connects = connectService.getConnectSession();
        StringBuffer buff = new StringBuffer();
        for (int i = 0; i < connects.length; i++) {
            if (connects[i] != null) {
                buff.append(connects[i].getName());
                buff.append(":");
                buff.append(connects[i].getPlayerCount());
                buff.append("\n");
            }
        }
        buff.append(playerService.size());
        write(buff.toString());
    }

    private void mute(Command command) {
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        if (command.getParamCount() < 2)
            errorArgument();
        else {
            int id = Integer.parseInt(command.getParam(0));
            int time = Integer.parseInt(command.getParam(1));
            chatService.forbiden(id, (byte) - 1, time);
            write("已经禁言");
        }
    }

    private void reloadtask(Command command){
        if (!checkLogined()) {
           errorLogin();
           return;
       }
       try{
           UWAPSegment seg = new UWAPSegment(ServerConstants.RELOAD);
           seg.write((byte)1);
//           connectService.broadcast(seg);
           stageService.loadTasks();
           write("任务重载完毕");
       }catch(Exception ex){
           ex.printStackTrace();
           write("任务重载错误");
       }
    }

    private void reload(Command command) {
        if (!checkLogined()) {
            errorLogin();
            return;
        }

        try {
            UWAPSegment seg = new UWAPSegment(ServerConstants.RELOAD);
            seg.write((byte)0);
//            connectService.broadcast(seg);
            stageService.reload();
            Server.instance.autoTraceService.reloadData();
            write("重载关卡完毕");
        } catch (Exception ex) {
            ex.printStackTrace();
            write("重载关卡错误");
        }
    }

    private void reloadbattlefield(Command command){
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        try {
            stageService.reloadBattleFieldTimer();
            BattleFieldTimer.start();
            write("重载战场定时器成功");
        } catch (Exception ex) {
            write(ex.getMessage());
        }
    }
    
    private void reloaCampBattlefield () {
    	if (!checkLogined()) {
    		errorLogin();
    		return;
    	}
    	try {
    		stageService.reloadCampBattlefieldTimer();
    		CampBattlefieldService.start();
    		write("重载阵营战场定时器成功");
    	} catch (Exception ex) {
    		write(ex.getMessage());
    	}
    }

    private void shutdown(Command command){
        if(!checkLogined()){
            errorLogin();
            
            return;
        }
        
        playerService.saveAll();
        shopService.saveAll();
        houseModel.saveAll();
        farmInstanceModel.saveAll();

        campMainService.shutdown();
        activityService.shutdown();
        campBattlefieldService.shutDown();
        
        if(Server.iMoneyType == Server.IMONEY_TYPE_CMCC){
            topListService.saveCmccJILINxml(Server.cmcc_jilin_count, Server.cmcc_jilin_playerid);
            topListService.saveCmccFUJIANxml(Server.cmcc_fujian_playerid, Server.cmcc_fujian_totalmoney);
        }
        
        TwelfthLunarService.saveIronChefActivityXml(TwelfthLunarConfig.playerDonateMap);
        
        UWAPSegment seg = new UWAPSegment(ServerConstants.SHUTDOWN);
        connectService.broadcast(seg);
    }

    private void modifyfavoit(Command command){
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        if (command.getParamCount() < 3) {
            errorArgument();
            return;
        }
        int id = Integer.parseInt(command.getParam(0));
        WorldPlayer player = getWorldPlayerAndCatch(id);
        if(player!=null){
            int friendId = Integer.parseInt(command.getParam(1));
            int favoit = Integer.parseInt(command.getParam(2));
            Friend[] friends = player.getFriends();
            for(int i=0;i<friends.length;i++){
                if(friends[i].getId()==friendId){
                    friends[i].setFavorite(favoit);
                    break;
                }
            }
            releasePlayer(player);
        }
    }
    
    private void exp(Command command){
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        if (command.getParamCount() < 2) {
            errorArgument();
            return;
        }
        int oldLevel = Integer.parseInt(command.getParam(0));
        int newLevel = Integer.parseInt(command.getParam(1));
        write("从" + oldLevel + "级升到" + newLevel + "级需要" + Utils.getUpLevelExp(oldLevel, newLevel) + "经验");
    }

    private void modify(Command command) {
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        if (command.getParamCount() < 3) {
            errorArgument();
            return;
        }
        int id = Integer.parseInt(command.getParam(0));
        WorldPlayer player = getWorldPlayerAndCatch(id);
        try {
            if (player != null) {
                String pro = command.getParam(1);
                if (pro.equals("地图") || pro.equals("mapid")) {
                    int mapId = Integer.parseInt(command.getParam(2));
                    player.setMapId((short) mapId);
                    playerService.savePlayer(player);
                } else if (pro.equals("x")) {
                    int x = Integer.parseInt(command.getParam(2));
                    player.setX((short) x);
                    playerService.savePlayer(player);
                } else if (pro.equals("y")) {
                    int y = Integer.parseInt(command.getParam(2));
                    player.setY((short) y);
                    playerService.savePlayer(player);
                } else if (pro.equals("性别") || pro.equals("sex")) {

                } else if (pro.equals("形象") || pro.equals("face")) {
                    int face = Integer.parseInt(command.getParam(2));
                    player.setFace((byte) face);
                    playerService.savePlayer(player);
                } else if (pro.equals("等级") || pro.equals("level")) {
                    int level = Integer.parseInt(command.getParam(2));
                    player.setLevel(level);
                    playerService.savePlayer(player);
                } else if (pro.equals("经验") || pro.equals("exp")) {
                    int exp = Integer.parseInt(command.getParam(2));
                    player.setExp(exp);
                    playerService.savePlayer(player);
                } else if (pro.equals("金钱") || pro.equals("money")) {
                    int money = Integer.parseInt(command.getParam(2));
                    player.setMoeny(money);
                    playerService.savePlayer(player);
                } else if (pro.equals("力量") || pro.equals("strength")) {
                    int strength = Integer.parseInt(command.getParam(2));
                    player.setStrength(strength);
                    playerService.savePlayer(player);
                } else if (pro.equals("敏捷") || pro.equals("agility")) {
                    int agility = Integer.parseInt(command.getParam(2));
                    player.setAgility(agility);
                    playerService.savePlayer(player);
                } else if (pro.equals("体力") || pro.equals("vitality")) {
                    int vitality = Integer.parseInt(command.getParam(2));
                    player.setVitality(vitality);
                    playerService.savePlayer(player);
                } else if (pro.equals("智力") || pro.equals("intelligence")) {
                    int intelligence = Integer.parseInt(command.getParam(2));
                    player.setIntelligence(intelligence);
                    playerService.savePlayer(player);
                } else if (pro.equals("hp")) {
                    int hp = Integer.parseInt(command.getParam(2));
                    player.setHp((short) hp);
                    playerService.savePlayer(player);
                } else if (pro.equals("mp")) {
                    int mp = Integer.parseInt(command.getParam(2));
                    player.setMp((short) mp);
                    playerService.savePlayer(player);
                } else if (pro.equals("剩余属性") || pro.equals("propertypoint")) {
                    int point = Integer.parseInt(command.getParam(2));
                    player.setLeavePoints(point);
                    playerService.savePlayer(player);
                } else if (pro.equals("剩余技能点") || pro.equals("abilitypoint")) {
                    int point = Integer.parseInt(command.getParam(2));
                    player.setPoint(point);
                    playerService.savePlayer(player);
                } else if (pro.equals("包位") || pro.equals("gridsize")) {
                    int gridsize = Integer.parseInt(command.getParam(2));
                    player.setGridSize((short) gridsize);
                    playerService.savePlayer(player);
                } else if (pro.equals("附加包位") || pro.equals("addedgridsize")) {
                    int size = Integer.parseInt(command.getParam(2));
                    player.setAddedGridSize(size);
                    playerService.savePlayer(player);
                } else if (pro.equals("宠物栏") || pro.equals("petsize")) {
                    int petsize = Integer.parseInt(command.getParam(2));
                    player.setPetSize(petsize);
                    playerService.savePlayer(player);
                } else if (pro.equals("荣誉") || pro.equals("credit")) {
                    int credit = Integer.parseInt(command.getParam(2));
                    player.setCredit(credit);
                    playerService.savePlayer(player);
                } else if (pro.equals("贡献值") || pro.equals("contribution")) {
                    int contribution = Integer.parseInt(command.getParam(2));
                    player.setContribution(contribution);
                    playerService.savePlayer(player);
                } else {
                    write("没有找到指定属性");
                }
            } else {
                write("没找到指定对象");
            }
        } finally {
            releasePlayer(player);
        }
    }

    private void revive(Command command) {
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        int n = command.getParamCount(); 
        if (n < 1) {
            errorArgument();
            return;
        }
        for (int i = 0; i < n; i++) {
        	int id = Integer.parseInt(command.getParam(i));
            WorldPlayer player = getWorldPlayerAndCatch(id);
            if (player != null) {
            	int mapId = -1;
            	int x = -1;
            	int y = -1;
            	try {
            		Stage stage = stageService.getStage((short)(player.getMapId() >> 4));
            		if (stage == null) { 
            			stage = stageService.getStage((short)(1649 >> 4)); // 1649 是德恩村地图id
            		}
        			mapId = stage.getDefaultMapId() | (stage.getId() << 4);
        			if (mapId < 0) { // 上面那个异常处理应该是对的，但据GM反映，stage非空，因此在这里处理一遍
        				stage = stageService.getStage((short)(1649 >> 4)); // 1649 是德恩村地图id
        				mapId = stage.getDefaultMapId() | (stage.getId() << 4);
        			}
        			x = stage.getDefaultX();
        			y = stage.getDefaultY();
        			if (mapId > 0 && x >= 0 && y >= 0) {
                    	if (player.online()) {
                    		byte[] bytes = stageService.getTaskBytes((short) 31004, new String[] {
                    				String.valueOf(mapId), String.valueOf(x), String.valueOf(y)});
                    		UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK);
                    		seg.writeShort((short) 31004);
                    		seg.writeShort((short) 2);
                    		seg.write(bytes);
                    		connectService.writeTo(seg, player.getId());
                    	} else {
                    		player = getWorldPlayerAndCatch(id);
                    		com.pip.itimes.server.stage.Scene scene = stageService.getScene((short)mapId);
                    		int tilleHighShift = 4 - scene.getType();
                    		player.setMapId((short) mapId);
                			player.setX((short) (x << 4));
                			player.setY((short) (y << tilleHighShift));
                			playerService.savePlayer(player);
                    	}
                	} else {
                		write("没有执行移动玩家");
                	}
        			
            	} finally {
            		releasePlayer(player);
            	}
            }
        }
    }

    private void systemsay(Command command) {
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        if (command.getParamCount() < 3) {
            errorArgument();
            return;
        }
        int destId = Integer.parseInt(command.getParam(0));
        int value = Integer.parseInt(command.getParam(1));
        chatService.adminMessage( -1, "系统", -7, 0, command.getParam(2));
//        UWAPSegment seg = new UWAPSegment(ServerConstants.CHAT);
//        seg.writeInt(-1);
//        seg.writeString("系统");
//        seg.writeInt(destId);
//        seg.writeInt(value);
//        seg.writeString(command.getParam(2));
//        connectService.broadcast(seg);
    }

    private void say(Command command) {
//        if(!checkLogined()){
//            errorLogin();
//            return;
//        }
//        if(command.getParamCount()<3){
//            errorArgument();
//            return;
//        }
//        int destId = Integer.parseInt(command.getParam(0));
//        int value = Integer.parseInt(command.getParam(1));
//        UWAPSegment seg = new UWAPSegment(ServerConstants.CHAT);
//        seg.writeInt(-1);
//        seg.writeString(admin.getName());
//        seg.writeInt(destId);
//        seg.writeInt(value);
//        seg.writeString(command.getParam(2));
//        connectService.broadcast(seg);
    }

    private void delete(Command command) {
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        if (command.getParamCount() < 3) {
            errorArgument();
            return;
        }
        int id = Integer.parseInt(command.getParam(0));
        WorldPlayer player = getWorldPlayerAndCatch(id);
        try {
            if (player != null) {
                int itemId = Integer.parseInt(command.getParam(1));
                int count = Integer.parseInt(command.getParam(2));
                IItem deleted = player.completeRemoveItem(itemId, count, null);
                if (deleted == null) {
                    write("物品ID或者数量错误");
                } else {
                    player.reset();
                    playerService.savePlayer(player);
                    write("成功删除物品");
                }
            } else {
                write("找不到指定玩家");
            }
        } finally {
            releasePlayer(player);
        }
    }

    private void deleteused(Command command) {
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        if (command.getParamCount() < 3) {
            errorArgument();
            return;
        }
        int id = Integer.parseInt(command.getParam(0));
        WorldPlayer player = getWorldPlayerAndCatch(id);
        try {
            if (player != null) {
                int itemId = Integer.parseInt(command.getParam(1));
                int count = Integer.parseInt(command.getParam(2));
                Grid grid = player.removeUsedEquipment(itemId, count);
                if (grid == null) {
                    write("物品ID或者数量错误");
                } else {
                    player.reset();
                    playerService.savePlayer(player);
                    write("成功删除物品");
                }
            } else {
                write("找不到指定玩家");
            }
        } finally {
            releasePlayer(player);
        }
    }
    
    /**
     * @param command
     * 阵营统计
     */
    private void camp(Command command){
    	int num = Integer.parseInt(command.getParam(0));
    	
    	if(num < 1 || num > 100){
    		write("查询分段错误");
    		
            return;
    	}
    	
    	int startnum = 0;
    	//将在线的t下线封号
		StringBuffer playersShow = new StringBuffer();
		int[] campNum = null;
		int maxLevel = 100;
		
		if(maxLevel % num == 0){
			campNum = new int[maxLevel / num];
		}else{
			campNum = new int[maxLevel / num + 1];
		}
		
		int campBright = 0;
		int campDark = 0;
		int campNo = 0;
		
		int campNoFlag = 0;
        /**
         * 阵营类型  光明
         */
        int campBrightFlag = 2;
        
        /**
         * 阵营类型  黑暗
         */
       int campDarkFlag = 1;
        
    	WorldPlayer[] players = playerService.getPlayers();
    	int total = players.length;
    	
    	if(total == 0){
    		write("当前无在线玩家");
    		
            return;
    	}
    	
        for (int i = 0; i < players.length; i++) {
        		int savenum = 0;
        		
        		for(int k = num, trik = 0; k <= maxLevel + num; k = k + num, trik++){
        			if(k >= players[i].getLevel()){
        				savenum = trik;
        				
        				break;
        			}
        			
        		}
        		
        		campNum[savenum] = campNum[savenum] + 1;
        		
        		if(players[i].getCamp() == campNoFlag){
                 	campNo++;
             	}else if(players[i].getCamp() == campBrightFlag){
             		campBright++;
             	}else if(players[i].getCamp() == campDarkFlag){
             		campDark++;
             	}
        }
        
        StringBuffer show = new StringBuffer();
        show.append(new Date().toLocaleString());
        show.append(" 当前总人数" + total);
        show.append(" 光明人数" + campBright);
        show.append(" 黑暗人数" + campDark);
        show.append(" 无阵营人数" + campNo);
        show.append(" 等级分布");
        
        for(int i = 0; i < campNum.length; i++){
        	show.append(" "); 
        	show.append(startnum);
        	show.append("级");
        	show.append(startnum + num);
        	show.append("级人数为");
        	show.append(campNum[i]);
        	show.append("(");
        	show.append(campNum[i] * 100 /total);
        	show.append("%)");
        	
        	startnum = startnum + num;
        }
        
        write(show.toString());
        
    }
    
    /**
     * @param command
     */
    private void recoverrole(Command command){
    	int playerId = Integer.parseInt(command.getParam(0));
    	
    	WorldPlayer player = getWorldPlayerAndCatch(playerId);
    	String playerName = null;
		if (player == null) {
            write("没找到此用户");
            return;
        } else {
        	playerName = player.getPlayerName();
        }
		
		playerService.addForbiden(playerId, 300);
		StringBuffer playersShow = new StringBuffer();
    	WorldPlayer[] players = playerService.getPlayers();
        for (int i = 0; i < players.length; i++) {
        	if(players[i].getPlayerName().equals(playerName) && players[i].getId() != playerId){
    			playerService.addForbiden(players[i].getId(), 300);
                connectService.kick(players[i].getId());
                playerService.acquire(players[i]);
                players[i].setValid(false);
                playerService.savePlayer(players[i]);
                log.info("ID[" + players[i].getId() + "] deleted");
                playerService.release(players[i]);
                playersShow.append("在线玩家" + players[i].getId()+ "已经下线并删除");
                break;
        	}
        }
        
		//如果是使用角色名字则默认为是战士角色，角色id默认为是修复
        WorldPlayer[] playerList = null;
    	try {
    		playerList = playerService.loadAdminWorldPlayer(playerName);
		} catch (Exception e) {
			 write("找不到指定玩家");
        	 return;
		}
		
		if(playerList.length == 0){
			 write("找不到指定名称玩家");
        	 return;
		}else{
			for(int i = 0; i < playerList.length; i++){
				playerService.acquire(playerList[i]);
				if(playerList[i].getPlayer().getValid()){
					playerList[i].setValid(false);
		            playerService.savePlayer(playerList[i]);
		            playersShow.append("   有效玩家" + playerList[i].getId() + "已经删除");
		            playerService.release(playerList[i]);
		            break;
				}else{
					playerService.release(playerList[i]);
				}
			}
			
	        for(int i = 0; i < playerList.length; i++){
				playerService.acquire(playerList[i]);
				if(playerList[i].getId() == playerId){
					playerList[i].setValid(true);
		            playerService.savePlayer(playerList[i]);
		            playerService.addForbiden(playerList[i].getId(), 120);
		            playersShow.append("   玩家" + playerList[i].getId() + "已经恢复");
		            playerService.release(playerList[i]);
		            break;
				}else{
					playerService.release(playerList[i]);
				}
			   
			}
	        
	        write(playersShow.toString());
		}
    }
    
    /**
     * @param command要恢复角色前的展示
     */
    private void recoverroleshow(Command command){
    	String playerName = null;
    	int playerId = Integer.parseInt(command.getParam(0));
    
		try {
			playerName = command.getParam(1);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			playerName = null;
		}
		
		if(playerId != -1){
			WorldPlayer player = getWorldPlayerAndCatch(playerId);
			if (player == null) {
                write("没找到此用户");
                return;
            } else {
            	playerName = player.getPlayerName();
            }

		}else{
			if(playerName == null || playerName.length() == 0){
				write("角色名，角色id 输入错误");
				return;
			}
		}
        
        //如果是使用角色名字则默认为是战士角色，角色id默认为是修复
        WorldPlayer[] playerList = null;
    	try {
    		playerList = playerService.loadAdminWorldPlayer(playerName);
		} catch (Exception e) {
			 write("找不到指定玩家");
        	 return;
		}
		
		if(playerList.length == 0){
			 write("找不到指定名称玩家");
        	 return;
		}else{
			StringBuffer playersShow = new StringBuffer();
			for(int i = 0; i < playerList.length; i++){
				playersShow.append(Utils.getAdminPlayerString(playerList[i]));
				playersShow.append("\n");
			}
			
			for(int i = 0; i < playerList.length; i++){
				playerService.acquire(playerList[i]);
			    playerService.release(playerList[i]);
			}
			
			write(playersShow.toString());
		}
			
    }
    private void deleterole(Command command){
    	String playerName = null;
    	boolean useName = true;
		try {
			playerName = command.getParam(0);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			playerName = null;
		}
		
		int playerId = 0;
		if(playerName == null || playerName.length() == 0){
			try{
				playerId = Integer.parseInt(command.getParam(1));
			}catch (Exception e1) {
				write("角色名，角色id 输入错误");
	        	return;
			}
			useName = false;
		}
		
    	//先判断玩家是否在线，让玩家下线然后删除
    	boolean onLineFlag = false;
    	WorldPlayer[] players = playerService.getPlayers();
    	WorldPlayer player = null;
        for (int i = 0; i < players.length; i++) {
        	if(useName){
        		if(players[i].getPlayerName().equals(playerName)){
            		player = players[i];
            		onLineFlag = true;
            	}
        	}else{
        		if(players[i].getId() == playerId){
            		player = players[i];
            		onLineFlag = true;
            	}
        	}
        	
        }
        
        if(onLineFlag){
        	playerService.addForbiden(player.getId(), 300);
            connectService.kick(player.getId());
        }else{
        	//此刻开始删除角色
        	try {
        		if(useName){
        			player = playerService.loadWorldPlayer(playerName);
        		}else{
        			player = playerService.loadWorldPlayer(playerId);
        		}
			} catch (Exception e) {
				 write("找不到指定玩家");
	        	 return;
			}
        }
	
	
        if (player == null) {
        	 write("找不到指定玩家");
        	 return;
        }
    /*	if (player.getLevel() >= 70){
    		write("70级以上的角色不可以删除哦");
    		return;
    	}*/
    	
        if (player.getTongDuty() == Tong.OWNER){
        	write("不能删除会长角色");
        	return;
        }
        if (mateService.hasMate(player)){
        	write("必须先解除婚约才能删号");
        	return;
        }
        if (masterService.isMaster(player) || masterService.isPrentice(player)){
        	write("必须先解除师徒关系才能删号");
        	return ;
        }
        if (player.getRef() > 0){
        	write("状态不对，不能删除角色");
        	return ;
        }
        playerService.acquire(player);
        player.setValid(false);
        playerService.savePlayer(player);
        //playerService.addForbiden(player.getId(), 0);
        log.info("ID[" + player.getId() + "] deleted");
        try {
			friendsService.killfriend(player.getId(), -1);
		} catch (DataAccessException e) {
			write("知己删错错误");
		}
        try {
			tongService.quit(player);
		} catch (TongException e) {
			write("公会删除错误");
		}
        playerService.release(player);
        write("删除角色成功");

    }
    private void add(Command command) {
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        if (command.getParamCount() < 3) {
            errorArgument();
            return;
        }
        int id = Integer.parseInt(command.getParam(0));
        WorldPlayer player = getWorldPlayerAndCatch(id);
        try {
            if (player != null) {
                int itemId = Integer.parseInt(command.getParam(1));
                int count = Integer.parseInt(command.getParam(2));
                count = player.addItem(itemId, count, null, player.getClientDataVersion());
                if (count == 0) {
                    write("添加物品错误");
                } else {
                    player.reset();
                    playerService.savePlayer(player);
                    write("成功添加物品");
                    log.info("admin name[" + (admin != null ? admin.getName() : "null") + "] add command playerID[" + id + "] itemId[" + itemId + "] count[" + count);
                }
            } else {
                write("找不到指定玩家");
            }
        } finally {
            releasePlayer(player);
        }
    }
    
    private void mailSendEnhanceEQU(Command command) {
    	  try {
              int destId = Integer.parseInt(command.getParam(0));
              String title = command.getParam(1);
              String content = command.getParam(2);
              int attachment = Integer.parseInt(command.getParam(3));
              //byte[] enhance = data.readBytes();
              byte[] enhance = new byte[]{1};
              byte[] att = new byte[0];
              
              //写下鉴定数量
              byte diamondCount = 9;
              
               //孔位信息
              byte[] dimaondMosaicInfo = new byte[]{1, 1, 1, 1, 1, 1, 1};
              //先前宝石数量 如果此时为0的话，则说明装备没有消遣宝石
              
              /*byte[] dimaondMosaicRole =data.readBytes(); //镶嵌部位属性
              int[] diamondMosaicItemId = data.readInts();//镶嵌物品
              
*/            
              byte[] dimaondMosaicRole = new byte[]{0}; //镶嵌部位
              int[] diamondMosaicItemId = new int[]{200667};//镶嵌物品
              
              int enchantingItemID = 201111;//附魔卷轴ID 0或负数表示没有附魔
              //附魔数组  [0]:是否增加属性，[1]：是否增加宝石，[2]:要增加的属性索引，[3]:要增加的属性值，[4]:要增加的宝石对应属性索引
              byte[] enchanting = new byte[]{1,1,3,24,4};
              byte[] vianyValue = new byte[]{4,6,8};//属性攻(石头，剪子，布)
              if (!checkLogined()) {
                  errorLogin();
                  return;
              }
              if (!checkAuth("add")) {
                  errorAuth();
                  return;
              }
              if(attachment>0){
                  IItemTemplate template = Items.getTemplate(attachment);
                  if(template==null){
                      write("附件ID错误");
                      return;
                  }else{
  	                IItem item = template.newInstance();
  	                if(item.getType()!=IItem.TYPE_EQU){
  	                	write("物品种类错误（非装备）");
  	                	return;
  	                }else{
  	                	IEquipment equ = (IEquipment)item;
  		                if(enhance.length > 0){//精炼
  		                	Enhance enhancetmp = null;
  		                	for(int k = 0;k<enhance.length;k++){
  		                		if ((enhance[k] <= 0) || (enhance[k] > 12)){
  		                			break;
  		                		}
  		                		enhancetmp = Enhance.getEnhance(enhance[k],template.getLevel());
  		                		equ.enhance(enhancetmp);
  		                	}
  		                }
  		                //星级鉴定
  		               /* if(diamondCount > equ.getDiamondcount() || diamondCount < 0){
    		                	throw new Exception("星级鉴定数量不对");
    		                }else{*/
    		                equ.setDiamond(diamondCount);
    		                //}
  		                //宝石检测
  		                equ.setDiamondMosiacRoleInfo(dimaondMosaicInfo);
  	                	if(dimaondMosaicRole.length != diamondMosaicItemId.length){
  	                		throw new Exception("镶嵌宝石信息长度不匹配");
  	                	}
  	                	
  	                	//检测宝石信息
  	                	Map<Integer,DiamondMosaic> diamondMosaicMap = DiamondMosaic.getDiamondMosaicMap();
  	                	for(int i = 0; i < diamondMosaicItemId.length; i++){
  	                		DiamondMosaic diamond = diamondMosaicMap.get(diamondMosaicItemId[i]);
  	                		if(diamond == null){
  	                			if(dimaondMosaicRole[i] != IEquipment.CURRENT_EQU_DIAMOND_NOTROLE || dimaondMosaicRole[i] != IEquipment.CURRENT_EQU_CANDIAMOND){
  	                				//throw new Exception("没有此宝石物品id");
  	                				write("没有此宝石物品id");
  	        	                	return;
  	                			}
  	                		}
  	                		
  	                		if(equ.canDiamondMosiacEmbed(dimaondMosaicRole[i], diamond.getProperty()) == IEquipment.CURRENT_EQU_DIAMOND){
  	                			//throw new ITimesException("该孔位已经镶嵌了宝石", data.getSerial(), data.getSessionId(), (byte) 36);
  	                			write("该孔位已经镶嵌了宝石");
	        	                return;
  	                		}else if(equ.canDiamondMosiacEmbed(dimaondMosaicRole[i], diamond.getProperty()) == IEquipment.CURRENT_EQU_DIAMOND_PROPERTY){
  	                			//throw new ITimesException("该孔位已经镶嵌了同类宝石", data.getSerial(), data.getSessionId(), (byte) 36);
  	                			write("该孔位已经镶嵌了同类宝石");
  	                			return;
  	                		}
  	                		
  	                		/*//检测宝石镶嵌的级别信息
  	                		if(dimaondMosaicRole[i] != IEquipment.CURRENT_EQU_DIAMOND_NOTROLE || dimaondMosaicRole[i] != IEquipment.CURRENT_EQU_CANDIAMOND){
  	                			byte diamondMosaicEmbedLevel = diamond.findDiamondMosaicLevel(diamondMosaicItemId[i]);
  	                			if(dimaondMosaicInfo[dimaondMosaicRole[i]] != (IEquipment.CURRENT_EQU_CANDIAMOND + diamondMosaicEmbedLevel)){
  	                				throw new Exception("此处孔位信息不对");
  	                			}
  	                		}	*/
  	                		byte[] diamondRoleInfo = equ.getDiamondMosiacRoleInfo();
  	    					diamondRoleInfo[dimaondMosaicRole[i]] = (byte) (IEquipment.CURRENT_EQU_CANDIAMOND + diamond.getDiamondLevel());
  	                		equ.diamondMosaic(dimaondMosaicRole[i], diamond);
  	                	}
  	                	//附魔
  	                	if(enchantingItemID > 0){
  	                		if(enchanting.length != 5){
  	                			write("附魔信息长度不匹配。");
  	                			return;
  	                		}
  	                		if(equ.getRequiredLevel() < 30){
  	                			write("只有装备等级大于等于30级的装备可以被附魔。");
  	                			return;
  	                		}
	  	                	Enchanting enchan = equ.getEnchanting();
	  	                	boolean hasArrtType = false;
	  						boolean hasStoneType = false;
	  						byte arrtType = 0;
	  						byte stoneType = 0;
	  						byte arrtValue = 0;
	  						byte stoneValue = 0;
	  						int arrtIndex = 0;
	  						Random rnd = new Random();
	  	                	switch(enchantingItemID){
	  	                	case ConnectSession.ENCHANTING_SCROLLITEMID_BASE:// 初级附魔卷轴
	  	                		if(enchanting[0]==1 && enchanting[1]==0){
	  	                			hasArrtType = true;
	  	                		}else{
	  	                			write("附魔卷轴的附加效果与输入的附加效果不符。");
	  	                			return;
	  	                		}
	  	                		break;
	  	                	case ConnectSession.ENCHANTING_SCROLLITEMID_MID://中级附魔卷轴
	  	                		if(enchanting[0]==1 && enchanting[1]==0){
	  								hasArrtType = true;
	  							}else if(enchanting[0]==0 && enchanting[1]==1){
	  								hasStoneType = true;
	  							}else{
	  								write("附魔卷轴的附加效果与输入的附加效果不符。");
	  	                			return;
	  							}
	  							arrtIndex = 1;
	  	                		break;
	  	                	case ConnectSession.ENCHANTING_SCROLLITEMID_HIGH://高级附魔卷轴
	  	                		if(enchanting[0]==1 && enchanting[1]==1){
	  	                			hasArrtType = true;
	  								hasStoneType = true;
	  								arrtIndex = 2;
	  	                		}else{
	  	                			write("附魔卷轴的附加效果与输入的附加效果不符。");
	  	                			return;
	  	                		}
	  	                		break;
	  	                	default:
	  	                		write("附魔卷轴ID错误。");
  	                			return;
	  	                	}
	  	                	//增加属性
	  						if(hasArrtType){
	  							int typeIndex = 0;
	  							if(enchanting[2] == -1){
	  								typeIndex = Utils.getRandom(rnd, 0, Enchanting.ARRT_TYPE.length - 1);
	  							}else if(enchanting[2] < Enchanting.ARRT_TYPE.length && enchanting[2] > -1){
	  								typeIndex = enchanting[2];
	  							}else{
	  								write("附魔要增加的属性索引错误。");
	  	                			return;
	  							}
	  							arrtType = Enchanting.ARRT_TYPE[typeIndex];
	  							if(enchanting[3] == -1){
	  								arrtValue = (byte)Utils.getRandom(rnd, Enchanting.ARRT_MIN[arrtIndex][typeIndex], Enchanting.ARRT_MAX[arrtIndex][typeIndex]);
	  							}else if(enchanting[3] >= Enchanting.ARRT_MIN[arrtIndex][typeIndex] && enchanting[3] <= Enchanting.ARRT_MAX[arrtIndex][typeIndex]){
	  								arrtValue = enchanting[3];
	  							}else{
	  								write("附魔要增加的属性值错误。");
	  	                			return;
	  							}
	  						}
	  						//增加宝石
	  						if(hasStoneType){
	  							int stoneIndex = 0;
	  							if(enchanting[4] == -1){
	  								stoneIndex = Utils.getRandom(rnd, 0, Enchanting.ARRT_TYPE.length - 1);
	  							}else if(enchanting[4] < Enchanting.ARRT_TYPE.length && enchanting[4] > -1){
	  								stoneIndex = enchanting[4];
	  							}else{
	  								write("附魔要增加的宝石属性索引错误。");
	  	                			return;
	  							}
	  							stoneType = Enchanting.ARRT_TYPE[stoneIndex];
	  							int stoneLevel = Enchanting.hasStoneType(equ, stoneType);
	  							stoneValue = (byte)Enchanting.calcStoneValue(stoneType, (byte)stoneLevel);
	  						}
	  	                	enchan.setEnchantingItemId(enchantingItemID);
	  	                	enchan.setArrtType(arrtType);
							enchan.setArrtValue(arrtValue);
							enchan.setStoneType(stoneType);
							enchan.setStoneValue(stoneValue);
  	                	}
						//属性攻
						Viany viany = equ.getViany();
						if(vianyValue.length != 3){
							write("属性攻信息长度不匹配。");
								return;
						}
						for(int i=0;i<vianyValue.length;i++){
							if(vianyValue[i]<0 || vianyValue[i]>10){
								write("属性攻等级错误。");
  								return;
							}
							viany.setViany((byte)(i+1), vianyValue[i]);
						}
						
  		                att = ItemUtils.item2dbAttachment(equ,1);
  		                write("精炼装备发送完毕");
  	                }
                  }
              }
              mailService.sendMail(destId,"",-1,"系统",title,content,att,0,true);
              log.info("Admin["+admin.getId()+"] SendMail Dest["+destId+"] title["+title+"] content["+content+"] attachment["+attachment+"] count[1] enhance");
          } catch (Exception ex) {
              write(ex.getMessage());
              ex.printStackTrace();
          }
    }
    
    private void batchadd(Command command){
    }

    private void deletepet(Command command) {
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        if (command.getParamCount() < 2) {
            errorArgument();
            return;
        }
        int id = Integer.parseInt(command.getParam(0));
        WorldPlayer player = getWorldPlayerAndCatch(id);
        try {
            if (player != null) {
                int petId = Integer.parseInt(command.getParam(1));
                Pet pet = player.getPet(petId);
                if (pet == null) {
                    write("宠物不存在");
                } else {
                    player.removePet(pet);
                    write("成功删除宠物");
                }
            } else {
                write("没找到指定玩家");
            }
        } finally {
            releasePlayer(player);
        }
    }

    public void forbid(Command command) {
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        if (command.getParamCount() < 1) {
            errorArgument();
            return;
        }
        int id = Integer.parseInt(command.getParam(0));
        WorldPlayer player = getWorldPlayerAndCatch(id);
        try {
            if (player != null) {

                if("cmcc".equals(type)){
                    UWAPSegment seg = new UWAPSegment(ServerConstants.FORBID);
                    seg.writeInt(player.getAccountId());
                    Server.instance.authSession.write(seg);
                }else{
                    ChangeStatusMessage message = new ChangeStatusMessage(id, 0, "");
                    accountSkeleton.send(message);
                }
            } else {
                write("没找到指定玩家");
            }
        } finally {
            releasePlayer(player);
        }
    }

    public void who(Command command) {
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        if (command.getParamCount() < 1) {
            errorArgument();
            return;
        }
        String mapString = command.getParam(0);
        boolean all = false;
        short mapId = -1;
        if ("all".equals(mapString)) {
            all = true;
        } else {
            mapId = Short.parseShort(command.getParam(0));
        }
        if (mapId != -1 || all) {
            StringBuffer buffer = new StringBuffer(4000);
            WorldPlayer[] players = playerService.getPlayers();
            for (int i = 0; i < players.length; i++) {
                if (players[i].getMapId() == mapId || all) {
                    buffer.append("{");
                    buffer.append(players[i].getId());
                    buffer.append(",");
                    buffer.append(players[i].getPlayerName());
                    buffer.append(",");
                    buffer.append(players[i].getLevel());
                    buffer.append(",");
                    buffer.append(players[i].getMapId());
                    buffer.append(",");
                    buffer.append(players[i].getTongName() == null ? "没有帮派" :
                                  players[i].getTongName());
                    buffer.append(",");
                    buffer.append(players[i].getX());
                    buffer.append(",");
                    buffer.append(players[i].getY());
                    buffer.append("}\n");
                }
            }
            write(buffer.toString());
        }

    }

    public void reloadversion(Command command) {
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        versionService.reload();
        write("版本文件重载成功");
    }
    
    public void resetexpadd(Command command) {
    	if (!checkLogined()) {
            errorLogin();
            return;
        }
    	if (command.getParamCount() < 1) {
            errorArgument();
            return;
        }
    	int addexppercent = Integer.parseInt(command.getParam(0));
        if (addexppercent == -1){
        	write("服务器战斗当前经验比值:"+Discount.EXPADDPERCENT+"%");
        }else if (addexppercent<100){
    		write("重置服务器战斗经验失败：比例不能小于100%");
    	}else if (addexppercent > 999){
    		write("重置服务器战斗经验比例为：比例不能大于等于1000%");
    	}else{
	    	Discount.EXPADDPERCENT = addexppercent;
	        write("重置服务器战斗经验比例为：" + addexppercent + "%");
    	}
    }
    
     public void reloadip(Command command) {
        if (!checkLogined()) {
            errorLogin();
            return;
        }

        UWAPSegment seg = new UWAPSegment(ServerConstants.FINITERELOAD);
        seg.write((byte) 1);
        connectService.broadcast(seg);

    }
     public void reloadphone(Command command) {
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        phoneService.reload();
    }
    
        private void setskill(Command command) {
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        if (command.getParamCount() < 3) {
            errorArgument();
            return;
        }
        int id = Integer.parseInt(command.getParam(0));
        WorldPlayer player = getWorldPlayerAndCatch(id);
        try {
            if (player != null) {
                String typeString = command.getParam(1);
                if ("all".equals(typeString)) {
                    short value = Short.parseShort(command.getParam(2));
                    for (int i = 0; i < 8; i++) {
                        player.setSkillPoint(i, value);
                    }
                    player.reset();
                    playerService.savePlayer(player);
                } else {
                    int type = Integer.parseInt(command.getParam(1));
                    short value = Short.parseShort(command.getParam(2));
                    if (type >= 0 && type <= 7) {
                        player.setSkillPoint(type, value);
                        player.reset();
                        playerService.savePlayer(player);
                    }
                }
            }
        } finally {
            releasePlayer(player);
        }
    }
        
    private void admin_camp(UWAPData data){
    	int num = -1;
    	
		try {
			num = data.readInt();
		} catch (IllegalAccessException e) {
			num = -1;
		}
		
    	if(num < 1 || num > 100){
    		write("查询分段错误");
    		
            return;
    	}
    	
    	int startnum = 0;
    	//将在线的t下线封号
		StringBuffer playersShow = new StringBuffer();
		int[] campNum = null;
		int maxLevel = 100;
		
		if(maxLevel % num == 0){
			campNum = new int[maxLevel / num];
		}else{
			campNum = new int[maxLevel / num + 1];
		}
		
		int campBright = 0;
		int campDark = 0;
		int campNo = 0;
		
		int campNoFlag = 0;
        /**
         * 阵营类型  光明
         */
        int campBrightFlag = 2;
        
        /**
         * 阵营类型  黑暗
         */
       int campDarkFlag = 1;
        
    	WorldPlayer[] players = playerService.getPlayers();
    	int total = players.length;
    	
    	if(total == 0){
    		write("当前无在线玩家");
    		
            return;
    	}
    	
        for (int i = 0; i < players.length; i++) {
        		int savenum = 0;
        		
        		for(int k = num, trik = 0; k <= maxLevel + num; k = k + num, trik++){
        			if(k >= players[i].getLevel()){
        				savenum = trik;
        				
        				break;
        			}
        			
        		}
        		
        		campNum[savenum] = campNum[savenum] + 1;
        		
        		if(players[i].getCamp() == campNoFlag){
                 	campNo++;
             	}else if(players[i].getCamp() == campBrightFlag){
             		campBright++;
             	}else if(players[i].getCamp() == campDarkFlag){
             		campDark++;
             	}
        }
        
        StringBuffer show = new StringBuffer();
        show.append(new Date().toLocaleString());
        show.append(" 当前总人数" + total);
        show.append(" 光明人数" + campBright);
        show.append(" 黑暗人数" + campDark);
        show.append(" 无阵营人数" + campNo);
        show.append(" 等级分布");
        
        for(int i = 0; i < campNum.length; i++){
        	show.append(" "); 
        	show.append(startnum);
        	show.append("级");
        	show.append(startnum + num);
        	show.append("级人数为");
        	show.append(campNum[i]);
        	show.append("(");
        	show.append(campNum[i] * 100 /total);
        	show.append("%)");
        	
        	startnum = startnum + num;
        }
        
        write(show.toString());
    } 
    
    public void admin_handleActivities (UWAPData data) throws Exception {
    	if (!checkLogined()) {
            errorLogin();
            return;
        }
    	byte type = data.readByte();
    	switch (type) {
    	case ADMIN_NEW_ACTIVITY_CLIENT:
    		newActivity(data);
    		break;
    	case ADMIN_DELETE_ACTIVITY_CLIENT:
    		deleteActivity(data);
    		break;
    	case ADMIN_ACTIVITY_DETAIL_CLIENT:
    		showActivityDetail(data);
    		break;
    	case ADMIN_ENABLE_ACTIVITY_CLIENT:
    		switchActivity(data);
    		break;
		default:
			break;
    	}
    }
    
    private void newActivity (UWAPData data) throws Exception {
    	String beginText = data.readString();
        String endText = data.readString();
        String name = data.readString();
        String implClass = data.readString();
		String configData = data.readString();
        Date beginTime;
        Date endTime;
        try {
        	beginTime = format.parse(beginText);
        	endTime = format.parse(endText);
        } catch (Exception e1) {
        	write("时间格式错误");
        	return;
        }
        if (activityService.getActivityByName(name) != null) {
        	write("已经有一个同名的活动了，每个活动必须有一个唯一的内部名称。");
        	return;
		}
		Activity act = new Activity(beginTime, endTime, name);
		ActivityData ad = new ActivityData(act);
		ad.setImplClass(implClass);
		ad.setConfigData(configData);
		if (ad.getImpl() == null) {
			write("错误的活动实现类");
        	return;
		}
		activityService.addActivity(ad);
		write("活动添加成功");
    }
    
    private void showActivityDetail (UWAPData data) throws Exception {
    	ActivityData[] activities = activityService.getActivities();
    	UWAPSegment seg = new UWAPSegment(ServerConstants.ADMIN_ACTIVITY_OK,
		                  data.getSerial());
		seg.writeShort((short) activities.length);
		for (int i = 0; i < activities.length; i++) {
			seg.writeString(format.format(activities[i].getBeginTime()));
			seg.writeString(format.format(activities[i].getEndTime()));
			seg.writeString(activities[i].getName());
			seg.writeString(activities[i].getImplClass());
			seg.writeString(activities[i].getConfigData());
			seg.writeBoolean(activities[i].getValid());
			seg.writeBoolean(activities[i].isEnabled());
		}
        write(seg);
    }
    
    private void switchActivity (UWAPData data) throws Exception {
		String name = data.readString();
		boolean valid = data.readByte() == 1;
		ActivityData act = activityService.getActivityByName(name);
		if (act == null) {
			write("没有此活动");
			return;
		}
		act.setValid(valid);
		if (activityService.saveActivity(act)) {
			write("修改成功");
		} else {
			write("修改失败");
		}
    }
    
    private void deleteActivity (UWAPData data) throws Exception {
    	String name = data.readString();
    	if (name == null) {
    		write("没有此活动");
			return;
    	}
    	ActivityData ad = activityService.getActivityByName(name);
    	if (ad == null) {
    		write("没有此活动");
			return;
    	}
    	if (activityService.removeActivity(ad)) {
    		write(name + "删除成功");
    	} else {
    		write(name + "删除失败");
    	}
    }
    
    private void admin_recoverRole(UWAPData data){

    	int playerId = 0;
		try {
			playerId = data.readInt();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			 write("用户id读取错误");
	         return;
		}
    	
    	WorldPlayer player = getWorldPlayerAndCatch(playerId);
    	String playerName = null;
		if (player == null) {
            write("没找到此用户");
            return;
        } else {
        	playerName = player.getPlayerName();
        }
		
		
		playerService.addForbiden(playerId, 300);
		//将在线的t下线封号
		StringBuffer playersShow = new StringBuffer();
    	WorldPlayer[] players = playerService.getPlayers();
        for (int i = 0; i < players.length; i++) {
        	if(players[i].getPlayerName().equals(playerName) && players[i].getId() != playerId){
    			playerService.addForbiden(players[i].getId(), 300);
    			
                connectService.kick(players[i].getId());
                playerService.acquire(players[i]);
                players[i].setValid(false);
                playerService.savePlayer(players[i]);
                log.info("ID[" + players[i].getId() + "] deleted");
                playerService.release(players[i]);
                playersShow.append("在线玩家" + players[i].getId()+ "已经下线并删除");
                break;
        	}
        }
        
		//如果是使用角色名字则默认为是战士角色，角色id默认为是修复
        WorldPlayer[] playerList = null;
    	try {
    		playerList = playerService.loadAdminWorldPlayer(playerName);
		} catch (Exception e) {
			 write("找不到指定玩家");
        	 return;
		}
		
		if(playerList.length == 0){
			 write("找不到指定名称玩家");
        	 return;
		}else{
			for(int i = 0; i < playerList.length; i++){
				playerService.acquire(playerList[i]);
				if(playerList[i].getPlayer().getValid()){
					playerList[i].setValid(false);
		            playerService.savePlayer(playerList[i]);
		            playersShow.append("   有效玩家" + playerList[i].getId() + "已经删除");
		            playerService.release(playerList[i]);
		            break;
				}else{
					playerService.release(playerList[i]);
				}
			}
			
	        for(int i = 0; i < playerList.length; i++){
				playerService.acquire(playerList[i]);
				if(playerList[i].getId() == playerId){
					playerList[i].setValid(true);
		            playerService.savePlayer(playerList[i]);
		            playerService.addForbiden(playerList[i].getId(), 120);
		            playersShow.append("   玩家" + playerList[i].getId() + "已经恢复");
		            playerService.release(playerList[i]);
		            break;
				}else{
					playerService.release(playerList[i]);
				}
			   
			}
	        
	        write(playersShow.toString());
		}
    
    }
    
    private void admin_recoverRoleShow(UWAPData data){

    	String playerName = null;
    	int playerId = -1;
		try {
			playerId = data.readInt();
		} catch (IllegalAccessException e2) {
			// TODO Auto-generated catch block
			write("用户id读取错误");
            return;
		}
    
		try {
			playerName = data.readString();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			playerName = null;
		}
		
		if(playerId != -1){
			WorldPlayer player = getWorldPlayerAndCatch(playerId);
			if (player == null) {
                write("没找到此用户");
                return;
            } else {
            	playerName = player.getPlayerName();
            }

		}else{
			if(playerName == null || playerName.length() == 0){
				write("角色名，角色id 输入错误");
				return;
			}
		}
        
        //如果是使用角色名字则默认为是战士角色，角色id默认为是修复
        WorldPlayer[] playerList = null;
    	try {
    		playerList = playerService.loadAdminWorldPlayer(playerName);
		} catch (Exception e) {
			 write("找不到指定玩家");
        	 return;
		}
		
		if(playerList.length == 0){
			 write("找不到指定名称玩家");
        	 return;
		}else{
			StringBuffer playersShow = new StringBuffer();
			for(int i = 0; i < playerList.length; i++){
				playersShow.append(Utils.getAdminPlayerString(playerList[i]));
				playersShow.append("\n");
			}
			
			for(int i = 0; i < playerList.length; i++){
				playerService.acquire(playerList[i]);
			    playerService.release(playerList[i]);
			}
			
			write(playersShow.toString());
			
		}
			
    
    }
    private void admin_deleteRole(UWAPData data){
    	String playerName = null;
    	boolean useName = true;
		try {
			playerName = data.readString();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			playerName = null;
		}
		int playerId = 0;
		if(playerName == null || playerName.length() == 0){
			try{
				playerId = data.readInt();
			}catch (IllegalAccessException e1) {
				write("角色名，角色id 输入错误");
	        	return;
			}
			useName = false;
		}
		
    	//先判断玩家是否在线，让玩家下线然后删除
    	boolean onLineFlag = false;
    	WorldPlayer[] players = playerService.getPlayers();
    	WorldPlayer player = null;
        for (int i = 0; i < players.length; i++) {
        	if(useName){
        		if(players[i].getPlayerName().equals(playerName)){
            		player = players[i];
            		onLineFlag = true;
            	}
        	}else{
        		if(players[i].getId() == playerId){
            		player = players[i];
            		onLineFlag = true;
            	}
        	}
        	
        }
        
        if(onLineFlag){
        	playerService.addForbiden(player.getId(), 300);
            connectService.kick(player.getId());
        }else{
        	//此刻开始删除角色
        	try {
        		if(useName){
        			player = playerService.loadWorldPlayer(playerName);
        		}else{
        			player = playerService.loadWorldPlayer(playerId);
        		}
			} catch (Exception e) {
				 write("找不到指定玩家");
	        	 return;
			}
        }
	
	
        if (player == null) {
        	 write("找不到指定玩家");
        	 return;
        }
    /*	if (player.getLevel() >= 70){
    		write("70级以上的角色不可以删除哦");
    		return;
    	}*/
    	
        if (player.getTongDuty() == Tong.OWNER){
        	write("不能删除会长角色");
        	return;
        }
        if (mateService.hasMate(player)){
        	write("必须先解除婚约才能删号");
        	return;
        }
        if (masterService.isMaster(player) || masterService.isPrentice(player)){
        	write("必须先解除师徒关系才能删号");
        	return ;
        }
        if (player.getRef() > 0){
        	write("状态不对，不能删除角色");
        	return ;
        }
      /*  //mengjie add 踢出角色后封闭删除角色
        if (playerService.isFrobiden(player.getId())) {
        	write("该角色隔离中，不可删除");
        	return;
        }*/
        playerService.acquire(player);
        player.setValid(false);
        playerService.savePlayer(player);
        log.info("ID[" + player.getId() + "] deleted");
        try {
			friendsService.killfriend(player.getId(), -1);
		} catch (DataAccessException e) {
			write("知己删错错误");
		}
        try {
			tongService.quit(player);
		} catch (TongException e) {
			write("公会删除错误");
		}
        playerService.release(player);
        write("删除角色成功");
    }
    private void admin_resetexpadd(UWAPData data) {
        try {
        	int addexppercent = data.readInt();
            if (!checkLogined()) {
                errorLogin();
                return;
            }
            if (!checkAuth("regulateExp")) {
                errorAuth();
                return;
            }
            if (addexppercent == -1){
            	write("服务器战斗当前经验比值:"+Discount.EXPADDPERCENT+"%");
            }else if (addexppercent<100){
        		write("重置服务器战斗经验失败：比例不能小于100%");
        	}else if (addexppercent > 999){
        		write("重置服务器战斗经验失败：比例不能大于等于1000%");
        	}else{
    	    	Discount.EXPADDPERCENT = addexppercent;
    	        write("重置服务器战斗经验比例为：" + addexppercent + "%");
        	}
        } catch (Exception ex) {
            write(ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    public void admin_resetIshopCredit (UWAPData data) {
    	try {
    		if (!checkLogined()) {
    			errorLogin();
    			return;
    		}
    		byte type = data.readByte();
			if (type == -1) {
				write("服务器战斗当前I币卖场获得荣誉比值:" + Discount.ISHOP_CREDIT_PERCENT + "%");
				return;
			} 
        	int addpercent = data.readInt();
			if (addpercent < 100) {
        		write("重置服务器I币卖场荣誉失败：比例不能小于100%");
        	} else if (addpercent > 999) {
        		write("重置服务器I币卖场荣誉失败：比例不能大于等于1000%");
        	} else {
    	    	Discount.ISHOP_CREDIT_PERCENT = addpercent;
    	        write("重置服务器I币卖场荣誉比例为：" + addpercent + "%");
        	}
        } catch (Exception ex) {
            write(ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    public void admin_resetBathExpCredit (UWAPData data) {
    	try {
    		if (!checkLogined()) {
    			errorLogin();
    			return;
    		}
    		byte type = data.readByte();
			if (type == -1) {
		        write("服务器泡澡获得经验比例:" + Discount.BATH_EXP_PERCENT + "%。服务器泡澡获得荣誉比例:" + Discount.BATH_CREDIT_PERCENT + "%");
		        return;
	        }
        	int addExpPercent = data.readInt();
        	int addCreditPercent = data.readInt();
            if (addExpPercent < 100) {
        		write("重置服务器泡澡经验比例失败：比例不能小于100%");
        	} else if (addCreditPercent < 100) {
        		write("重置服务器泡澡荣誉比例失败：比例不能小于100%");
        	} else if (addExpPercent > 999) {
        		write("重置服务器泡澡经验比例失败：比例不能大于等于1000%");
        	} else if (addCreditPercent > 999) {
        		write("重置服务器泡澡荣誉比例失败：比例不能大于等于1000%");
        	} else {
    	    	Discount.BATH_EXP_PERCENT = addExpPercent;
    	    	Discount.BATH_CREDIT_PERCENT = addCreditPercent;
    	        write("重置服务器泡澡经验比例为：" + addExpPercent + "%。重置服务器泡澡荣誉比例为：" + addCreditPercent + "%");
        	}
        } catch (Exception ex) {
            write(ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    public void admin_resetUnlineExp (UWAPData data) {
    	try {
    		if (!checkLogined()) {
    			errorLogin();
    			return;
    		}
    		byte type = data.readByte();
			if (type == -1) {
		        write("服务器离线经验比例:" + Discount.UNLINE_EXP_PERCENT + "%。");
		        return;
	        }
        	int addExpPercent = data.readInt();
            if (addExpPercent < 100) {
        		write("重置服务器离线经验比例失败：比例不能小于100%");
        	} else if (addExpPercent > 999) {
        		write("重置服务器离线经验比例失败：比例不能大于等于1000%");
        	} else {
    	    	Discount.UNLINE_EXP_PERCENT = addExpPercent;
    	        write("重置服务器离线经验比例为：" + addExpPercent + "%。");
        	}
        } catch (Exception ex) {
            write(ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    public void reloadmodel(Command command) {
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        Server.instance.modelService.reload();
        write("机型配置重载成功");
    }

    public void releaseaccount(Command command) {
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        if (command.getParamCount() < 1) {
            errorArgument();
            return;
        }
        UWAPSegment seg = new UWAPSegment(ServerConstants.RELEASEACCOUNT);
        int id = Integer.parseInt(command.getParam(0));
        seg.writeInt(id);
        Server.instance.authSession.write(seg);
    }

    public void loghibernate(Command command) {
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        HibernateUtil.getSessionFactory().getStatistics().logSummary();
        String[] querys = HibernateUtil.getSessionFactory().getStatistics().
                          getQueries();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < querys.length; i++) {
            sb.append(querys[i]);
            sb.append("\n");
        }

        write(sb.toString());
    }

    public void errorArgument() {
        write("参数不对");
    }

    public void errorAuth() {
        write("没有此权限");
    }

    public void errorLogin() {
        write("没有登陆");
    }


    private WorldPlayer getWorldPlayer(String name) {
        WorldPlayer player = (WorldPlayer) name2player.get(name);
        if (player == null) {
            Player p = playerService.getPlayer(name);
            try {
                player = new WorldPlayer(p);
            } catch (Exception ex) {
            }
        }
        return player;
    }

    private void releasePlayer(WorldPlayer player) {
        if (player != null)
            playerService.release(player);
    }

    private WorldPlayer getWorldPlayerAndCatch(String name) {
        try {
            WorldPlayer player = playerService.loadWorldPlayer(name);
            if (player != null) {
                playerService.acquire(player);
            }
            return player;
        } catch (Exception ex) {
            return null;
        }
//        WorldPlayer player = (WorldPlayer)name2player.get(name);
//        if(player==null){
//            Player p = playerService.getPlayer(name);
//            try {
//                player = new WorldPlayer(p);
//                id2player.put(new Integer(player.getId()), player);
//                name2player.put(player.getPlayerName(), player);
//            } catch (Exception ex) {
//            }
//        }
//        return player;
    }

    private WorldPlayer getWorldPlayerAndCatch(int id) {
        try {
            WorldPlayer player = playerService.loadWorldPlayer(id);
            if (player != null) {
                playerService.acquire(player);
            }
            return player;
        } catch (Exception ex) {
            return null;
        }
//        WorldPlayer player = (WorldPlayer)id2player.get(new Integer(id));
//        if(player==null){
//            Player p = playerService.loadPlayerById(id);
//            try {
//                player = new WorldPlayer(p);
//                id2player.put(new Integer(player.getId()), player);
//                name2player.put(player.getPlayerName(), player);
//            } catch (Exception ex) {
//            }
//        }
//        return player;
    }

    public void write(String s) {
        UWAPSegment seg = new UWAPSegment(ServerConstants.ADMIN_COMMAND, -1,
                                          getSessionId());
        seg.writeString(s + "\n");
        write(seg);
    }

    public boolean isKeepWatch() {
        return isKeepWatch;
    }

    public void idle(IdleStatus status) {
    }

    public void opened() {
    }

    private void execute(Command command) {
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        if (command.getParamCount() < 2) {
            errorArgument();
            return;
        }
        try {
            String root = command.getParam(0);
            String className = command.getParam(1);
            FileClassLoader cl = new FileClassLoader(root);
            Class cls = cl.loadClass(className, true);
            Runnable instance = (Runnable) cls.newInstance();
            instance.run();
            write("执行结束");
        } catch (Exception e) {
            write(e.toString());
        }
    }

    private void addpet(Command command) {
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        if (command.getParamCount() < 8) {
            errorArgument();
            return;
        }
        int id = Integer.parseInt(command.getParam(0));
        WorldPlayer player = getWorldPlayerAndCatch(id);
        short stageId = Short.parseShort(command.getParam(1));
        int mIndex = Integer.parseInt(command.getParam(2));
        try {
            Stage stage = stageService.getStage(stageId);
            if (stage == null) {
                write("关卡号错误");
                return;
            }
            Monster m = stage.getMonster(mIndex);
            if (m == null) {
                write("宠物号错误");
                return;
            }
            boolean baby = Integer.parseInt(command.getParam(3)) == 0 ? false : true;
            Pet pet = null;
            if (!baby) {
                pet = new Pet();
                pet.setId(IDGenerator.getPetId());
                pet.setPetType(m.getPetType());
                pet.setBaby(false);
                Utils.initPet(pet, 4 * m.getLevel(), 0 , m.getLevel());
                pet.setLevel(m.getLevel());
                pet.setItemId(m.getStageId() << 16 | m.getIndex());
                pet.setCurrentPoint(0);
                pet.setExp(0);
                pet.setFavor(50);
//                                pet.setPoint(20);
                pet.setHp(pet.getMaxHp());
                pet.setMp(pet.getMaxMp());
            } else {
                pet = new Pet();
                pet.setId(IDGenerator.getPetId());
                pet.setPetType(m.getPetType());
                pet.setBaby(true);
                Utils.initPet(pet,
                              Utils.getBabyPetPoint(m.
                        getLevel()),
                              Utils.getBabyPetAddedPoint(m.
                        getLevel()) , 1);
                pet.setLevel(1);
                pet.setItemId(m.getStageId() << 16 | m.getIndex());
                pet.setCurrentPoint(0);
                pet.setExp(0);
                pet.setFavor(50);
                pet.setHp(pet.getMaxHp());
                pet.setMp(pet.getMaxMp());
            }
            for (int i = 0; i < 5; i++) {
                pet.addAbility(Ability.getAbility(Integer.parseInt(command.
                        getParam(4 + i))));
            }
            int count = player.addPet(pet, null);
            
            if (count > 0)
                write("添加宠物成功");
            else
                write("宠物包格不够");
        } catch (Exception ex) {
            write(ex.toString());
        } finally {
            releasePlayer(player);
        }

    }

    private void shopshow(Command command) {
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        if (command.getParamCount() < 1) {
            errorArgument();
            return;
        }
        int playerId = Integer.parseInt(command.getParam(0));
        ShopData[] shops = shopService.getShops(playerId);
        StringBuffer buff = new StringBuffer(3000);
        for (int i = 0; i < shops.length; i++) {
            buff.append(Utils.getShopString(shops[i]));
            buff.append("\n");
        }
        write(buff.toString());
    }

    private void shopmodify(Command command) {
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        if (command.getParamCount() < 3) {
            errorArgument();
            return;
        }
        int shopId = Integer.parseInt(command.getParam(0));
        ShopData shop = shopService.getShopData(shopId);
        if (shop == null) {
            write("没找到店铺");
            return;
        }
        String pro = command.getParam(1);
        if ("money".equals(pro)) {
            synchronized (shop) {
                int money = Integer.parseInt(command.getParam(2));
                shop.setMoney(money);
                write("修改店铺成功");
            }
        } else if ("level".equals(pro)) {
            synchronized (shop) {
                int level = Integer.parseInt(command.getParam(2));
                shop.setLevel(level);
                write("店铺修改成功");
            }
        } else if ("size".equals(pro)) {
            synchronized (shop) {
                int size = Integer.parseInt(command.getParam(2));
                shop.setGridSize((short) size);
                write("店铺修改成功");
            }
        } else if ("empty".equals(pro)) {
            synchronized (shop) {
                shop.empty();
                write("店铺修改成功");
            }
        }
    }

    private void shopdelete(Command command) {
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        if (command.getParamCount() < 3) {
            errorArgument();
            return;
        }
        int shopId = Integer.parseInt(command.getParam(0));
        ShopData shop = shopService.getShopData(shopId);
        if (shop == null) {
            write("没找到店铺");
            return;
        }
        int itemId = Integer.parseInt(command.getParam(1));
        int count = Integer.parseInt(command.getParam(2));
        synchronized (shop) {
            IItem item = shop.completeRemoveItem(itemId, count);
            if (item == null) {
                write("物品ID或者数量错误");
            } else {
                shop.reset();
                write("成功删除物品");
            }
        }
    }

    private void script(Command command) {
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        if (command.getParamCount() < 2) {
            errorArgument();
            return;
        }
        int id = Integer.parseInt(command.getParam(0));
        WorldPlayer player = playerService.getWorldPlayer(id);
        if (player == null) {
            write("玩家不在线");
            return;
        }
        short taskId = Short.parseShort(command.getParam(1));
        int count = command.getParamCount() - 2;
        if (count == 0) {
            byte[] bytes = stageService.getTaskBytes(taskId,player.getLevel());
            if (bytes == null) {
                write("脚本不存在");
                return;
            }
            UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK);
            seg.writeShort(taskId);
            seg.writeShort((short) 2);
            seg.write(bytes);
            connectService.writeTo(seg, player.getId());
        } else {
            String[] parameters = new String[count];
            for (int i = 0; i < count; i++) {
                parameters[i] = command.getParam(2 + i);
            }
            byte[] bytes = stageService.getTaskBytes(taskId, parameters);
            if (bytes == null) {
                write("脚本不存在");
                return;
            }
            UWAPSegment seg = new UWAPSegment(ClientConstants.GET_FILE_OK);
            seg.writeShort(taskId);
            seg.writeShort((short) 2);
            seg.write(bytes);
            connectService.writeTo(seg, player.getId());
        }
    }

    private void battlefield(Command command) {
        if (!checkLogined()) {
            errorLogin();
            return;
        }

        String subCommand = command.getParam(0);

        if ("start".equals(subCommand)) {
            int id = Integer.parseInt(command.getParam(1));
            int forbidEnter = Integer.parseInt(command.getParam(2));
            int forbid = Integer.parseInt(command.getParam(3));
            int end = Integer.parseInt(command.getParam(4));
            long current = System.currentTimeMillis();

            try {
                battleField.start(current + forbidEnter * 60 * 1000L,
                                  current + forbid * 60 * 1000L,
                                  current + end * 60 * 1000L);
                write("战场初始化成功");
            } catch (BattleFieldException ex) {
                write(ex.getMessage());
            }
        } else if ("stop".equals(subCommand)) {
            try {
                battleField.forceStop();
                write("战场关闭成功");
            } catch (Exception ex) {
                write(ex.getMessage());
            }
        } else {
            write("无效战场指令");
        }
    }

    private void guildbattlefield(Command command){
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        if (command == null){
        	write("start/stop id forbidEnter forbid end");
        	return;
        }
        String subCommand = command.getParam(0);

        if ("start".equals(subCommand)) {
            int id = Integer.parseInt(command.getParam(1));
            int forbidEnter = Integer.parseInt(command.getParam(2));
            int forbid = Integer.parseInt(command.getParam(3));
            int end = Integer.parseInt(command.getParam(4));
            long current = System.currentTimeMillis();

            try {
                guildBattleField.start(current + forbidEnter * 60 * 1000L,
                                  current + forbid * 60 * 1000L,
                                  current + end * 60 * 1000L);
                write("公会试练场初始化成功");
            } catch (BattleFieldException ex) {
                write(ex.getMessage());
            }
        } else if ("stop".equals(subCommand)) {
            try {
                guildBattleField.forceStop();
                write("公会试练场关闭成功");
            } catch (Exception ex) {
                write(ex.getMessage());
            }
        } else {
            write("无效战场指令");
        }
    }

    private void addstoreitem(Command command){
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        if(command.getParamCount()!=5){
            errorArgument();
            return;
        }
        String groupName = command.getParam(0);
        int itemId = Integer.parseInt(command.getParam(1));
        int count = Integer.parseInt(command.getParam(2));
        int price = Integer.parseInt(command.getParam(3));
        String consumeCode = command.getParam(4);
        IStoreGroup group = IStoreGroups.getGroup(groupName);
        if(group==null){
            write("没有此商品组");
            return;
        }
        IStoreItem tmpItem = group.getItem(itemId);
        if(tmpItem != null){
        	tmpItem.count = count;
        	tmpItem.price = price;
        	tmpItem.consumeCode = consumeCode;
        	write("修改"+tmpItem.item.getName()+"  count["+count+"]  price["+price+"] consumeCode ["+consumeCode+"]成功");
        }else{
	        IStoreItem item = new IStoreItem();
	        item.item = Items.getTemplate(itemId);
	        item.count = count;
	        item.price = price;
	        item.consumeCode = consumeCode;
	        item.discount = 100;
	        group.addItem(item);
	        write("添加"+item.item.getName()+"  count["+count+"]  price["+price+"] consumeCode ["+consumeCode+"]成功");
        }
    }
    
    private void removestoreitem(Command command){
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        if(command.getParamCount()!=2){
            errorArgument();
            return;
        }
        String groupName = command.getParam(0);
        int itemId = Integer.parseInt(command.getParam(1));
        IStoreGroup group = IStoreGroups.getGroup(groupName);
        if(group==null){
            write("没有此商品组");
            return;
        }
        if(group.removeItem(itemId)){
            write("删除成功");
        }else{
            write("删除失败");
        }
    }
    
    private void addCstoreitem(Command command){
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        if (command.getParamCount() != 5) {
            errorArgument();
            return;
        }
        String groupName = command.getParam(0);
        int itemId = Integer.parseInt(command.getParam(1));
        int count = Integer.parseInt(command.getParam(2));
        int credit = Integer.parseInt(command.getParam(3));
        int typeId = Integer.parseInt(command.getParam(4));
        CStoreGroup group = CStoreGroups.getGroup(groupName, typeId);
        if(group==null){
            write("没有此商品组");
            return;
        }
        CStoreItem item = new CStoreItem();
        item.item = Items.getTemplate(itemId);
        item.count = count;
        item.credit = credit;
        group.addItem(item);
        write("添加" + item.item.getName() + "  count[" + count + "]");
    }
    
    private void removeCstoreitem(Command command){
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        if (command.getParamCount() != 3) {
            errorArgument();
            return;
        }
        String groupName = command.getParam(0);
        int itemId = Integer.parseInt(command.getParam(1));
        int typeId = Integer.parseInt(command.getParam(2));
        CStoreGroup group = CStoreGroups.getGroup(groupName, typeId);
        if(group==null){
            write("没有此商品组");
            return;
        }
        if(group.removeItem(itemId)){
            write("删除成功");
        }else{
            write("删除失败");
        }
    }

    private void addrobot(Command command){
        if (!checkLogined()) {
            errorLogin();
            return;
        }
//        mapid x y startx starty wdith height count
        if(command.getParamCount()==8){
            short mapId = Short.parseShort(command.getParam(0));
            short x = Short.parseShort(command.getParam(1));
            short y = Short.parseShort(command.getParam(2));
            short startX = Short.parseShort(command.getParam(3));
            short startY = Short.parseShort(command.getParam(4));
            short width = Short.parseShort(command.getParam(5));
            short height = Short.parseShort(command.getParam(6));
            int count = Integer.parseInt(command.getParam(7));
            robotService.loadPlayers(mapId,x,y,startX,startY,width,height,count);

            write("添加机器人成功");
        }
    }

    private void clearrobot(Command command){
        if (!checkLogined()) {
            errorLogin();
            return;
        }

        robotService.clearRobot();

        write("清除机器人成功");
    }

    private void addonline(Command command){
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        if(command.getParamCount()==1){
            addedOnline = Integer.parseInt(command.getParam(0));
            UWAPSegment seg = new UWAPSegment(ServerConstants.ADD_ONLINE);
            seg.writeInt(addedOnline);
            connectService.broadcast(seg);
            write("增加在线数量:"+addedOnline);
        }
    }

    private void showaddedonline(Command command){
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        write("当前增加的在线数量:"+addedOnline);
    }

    private void addimoney(Command command){
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        if(command.getParamCount()==2){
            int accountId = Integer.parseInt(command.getParam(0));
            int money = Integer.parseInt(command.getParam(1))*100;
            UWAPSegment seg = new UWAPSegment(
                   ServerConstants.ADD_IMONEY);
           seg.writeInt(accountId);
           seg.writeInt(money);
           seg.writeInt(-1);
           Server.instance.authSession.write(seg);
        }
    }

    private void sport(Command command){
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        if(command.getParamCount()==5){
            int start = Integer.parseInt(command.getParam(0));
            int end = Integer.parseInt(command.getParam(1));
            int interval = Integer.parseInt(command.getParam(2));
            String name = command.getParam(3);
            int bbsId = Integer.parseInt(command.getParam(4));
            long current = System.currentTimeMillis();
            sportsService.start(current + start * 60 * 1000L, current + end * 60 * 1000L, interval * 60 * 1000L, name,
                                bbsId);
            write("比赛开始了");
        }
    }

    private void bind(Command command){
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        if(command.getParamCount()==4){
            int playerId = Integer.parseInt(command.getParam(0));
            int itemId = Integer.parseInt(command.getParam(1));
            int id = Integer.parseInt(command.getParam(2));
            boolean bind = Boolean.parseBoolean(command.getParam(3));
            WorldPlayer player = getWorldPlayerAndCatch(playerId);
            if(player!=null){
                IEquipment equ = player.getEquipment(itemId,id);
                if(equ!=null){
                    equ.setBinded(bind);
                    write(equ.getName()+" bind "+bind);
                }
            }
            releasePlayer(player);
        }
    }

    private void island(Command command){
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        if(command.getParamCount()>=1){
            String cmd = command.getParam(0);
            if(cmd.equals("end")){
                tongService.endIsland();
            }else if(cmd.equals("start")){
                if(command.getParamCount()==6){
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'&'HH:mm");
                        try {
                            Date prepare = format.parse(command.getParam(1));
                            Date startAuction = format.parse(command.getParam(2));
                            Date prepareEndAuction = format.parse(command.getParam(3));
                            Date endAuction = format.parse(command.getParam(4));
                            Date end = format.parse(command.getParam(5));
                            tongService.prepareStartIslandAuction(prepare, startAuction, prepareEndAuction, endAuction, end);
                            write("已经启动");
                        } catch (ParseException ex) {
                        	log.error(ex, ex);
                            write("日期格式错误");
                        } catch (Exception ex) {
                        	log.error(ex, ex);
                            write("没有启动");
                        }

                }else{
                    if(command.getParamCount()==1){
                        Calendar cal = Calendar.getInstance();
                        cal.add(Calendar.MINUTE,2);
                        cal.set(cal.SECOND,0);
                        cal.set(cal.MILLISECOND,0);
                        Date prepare = cal.getTime();
                        cal.add(Calendar.MINUTE,2);
                        Date startAuction = cal.getTime();
                        cal.add(Calendar.MINUTE,2);
                        Date prepareEndAuction = cal.getTime();
                        cal.add(Calendar.MINUTE,2);
                        Date endAuction = cal.getTime();
                        cal.add(Calendar.MINUTE,2);
                        Date end = cal.getTime();
                        try {
                            tongService.prepareStartIslandAuction(prepare, startAuction,prepareEndAuction, endAuction, end);
                            write("已经启动");
                        } catch (Exception ex1) {
                        	log.error(ex1, ex1);
                            write("没有启动");
                        }
                    }else{
                        write("参数错误");
                    }
                }
            }
        }else{
            write("参数错误");
        }
    }

    private void tongcredit(Command command){
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        if(command.getParamCount()==2){
            int tongId = Integer.parseInt(command.getParam(0));
            int credit = Integer.parseInt(command.getParam(1));
            TongData td = tongService.getTongData(tongId);
            td.setCredit(credit);
            tongService.saveTongData(td);
        }
    }
    private void adminlist(Command command) {
    	if (admin == null || !admin.hasAuth(AdminAuth.root)) {
			write("没有登陆或者没有权限");
			return;
		}
        if (command.getParamCount() != 0) {
            write("参数不对");
        } else {
        	List adminList = adminService.getAdminList();
        	if (adminList != null) {
	        	StringBuffer buf = new StringBuffer("[ADMIN-LIST]账户名;密码;权限:");
	        	for (Object obj : adminList) {
	        		Admin adm = (Admin)obj;
	        		buf.append("\n");
	        		buf.append(adm.getName());
	        		buf.append(";");
	        		buf.append(adm.getPassword());
	        		buf.append(";");
	        		buf.append(adm.getAuth());
	        	}
	    		write(buf.toString());
        	} else {
        		write("请求失败");
        	}
        }
    }

    /** 可动态挂载的管理员功能模块 */
    public static interface IAdminFunction {
    	/** 文本命令执行接口 */
    	public void execCommand(AdminSession admin, Command command) throws Exception;
    	/** 协议命令执行接口 */
    	public void execCommand(AdminSession admin, UWAPData data) throws Exception;
    	/** 返回本模块功能说明 */
    	public String getHelp();
    	/** 返回本模块侦听的所有协议号  */
    	public int[] getProtocolId(); 
    	/** 返回本协议侦听的所有指令 */
    	public String[] getCommands();
    	/** 确认管理员是否有权限执行此命令 */
    	public boolean canExecute(Admin admin);
    }
    private static final Comparator comparator = new Comparator() {
    	 public int compare(Object left, Object right) {
             return ((String) left).compareTo((String) right);
         }
    };
    
    /**
     * 设置打折信息
     * @param data
     */
    public void admin_ishopDiscount (UWAPData data) {
    	try {
    		if (!checkLogined()) {
    			errorLogin();
    			return;
    		}
    		String oldMessage = IStoreGroups.getMessage();
    		String message = data.readString();
    		if(message == null){
    			message = "";
    		}
	    	IStoreGroups.setMessage(message);
	        write("已将\"" + oldMessage + "\"替换为\"" + message + "\"。");
        } catch (Exception ex) {
            write(ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    /**
     * 添加指定玩家的金元
     * @param data
     */
    public void admin_addFarmMoney(UWAPData data) throws Exception {
		if (!checkLogined()) {
			errorLogin();
			return;
		}
		int playerId = data.readInt();
		int addFarmMoney = data.readInt();
		if(playerId < 0){
			return;
		}
		WorldPlayer player = getWorldPlayerAndCatch(playerId);
        try {
            if (player != null) {
            	player.setFarmMoney(player.getFarmMoney() + addFarmMoney); 
                player.reset();
                playerService.savePlayer(player);
                write("成功增加吸血鬼金元：" + addFarmMoney + " 现有金元[" + player.getFarmMoney() + "]");
            } else {
                write("找不到指定玩家");
            }
        } finally {
            releasePlayer(player);
        }
    }
    
    public void admin_battleClear(UWAPData data) throws Exception {
		if (!checkLogined()) {
			errorLogin();
			return;
		}
		int playerId = data.readInt();
		if(battleField != null){
			BattleService2 battleService = battleField.getBattleService();
			if(battleService != null){
				Battle2 battle = battleService.getBattleByPlayer(playerId);
				if(battle != null){
					battleService.removeBattle(battle);
					write("清除指定角色战斗成功。");
				}else{
					write("指定角色不在战斗。");
				}
			}else{
				write("战斗服务器不存在。");
			}
		}else{
			write("战斗服务器没有初始化。");
		}
    }
    
    /**
     * 当角色出现一些怪异的问题时,可以通过这种方式将角色信息全部释放掉
     * @param command
     */
    private void logoutPlayer(Command command) {
        if (!checkLogined()) {
            errorLogin();
            return;
        }
        if (command.getParamCount() < 1) {
            errorArgument();
            return;
        }
        int playerid = Integer.parseInt(command.getParam(0));
        if(playerid < 0){
        	write("无效的角色ID");
        	return;
        }
        WorldPlayer player = playerService.getWorldPlayer(playerid);
        if(player == null){
        	write("该角色无需清除");
        	return;
        }
        String outstr = "清除记录:";
        if(battleField != null){
			BattleService2 battleService = battleField.getBattleService();
			if(battleService != null){
				Battle2 battle = battleService.getBattleByPlayer(playerid);
				if(battle != null){
					battleService.removeBattle(battle);
					outstr += "\n清除角色战斗";
				}
			}
		}
        int releaseTime = player.online() ? 1 : 0;
        int releasecount = 0;
    	while(player.getRef() > releaseTime){
    		playerService.release(player);
    		releasecount++;
    	}
        outstr += "\n释放次数:" + releasecount;
        outstr += "\n踢除角色";
        try{
        	playerService.addForbiden(playerid, 10);
        	connectService.kick(playerid);
        }catch(Exception e){
        	outstr += "\n踢除出错!";
        }
        write(outstr);
    }
}
