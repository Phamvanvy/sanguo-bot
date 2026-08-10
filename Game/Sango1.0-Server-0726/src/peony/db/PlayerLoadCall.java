package peony.db;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.GameItem;
import peony.game.Horse;
import peony.game.Identity;
import peony.game.ItemTemplate;
import peony.game.LogUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.Skills;
import peony.game.TransactionBagGrid;
import peony.game.VMap;
import peony.game.World;
import peony.game.attendant.Attendant;
import peony.game.buff.BuffUtil;
import peony.game.itemenhance.ItemEnhance;
import peony.game.nation.CandidateService;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.account.Account;
import peony.service.friend.PlayerRelation;
import peony.service.player.PlayerService;

public class PlayerLoadCall extends ClientSessionAsyncCall {
	protected static final Logger log = Logger.getLogger(PlayerLoadCall.class); 
	protected World world;
	protected int actorId;
	protected Player player;
	protected int serial;
	protected PlayerService playerService;
	protected String MIEI = "";
	
	protected static int[] EQUIP_SCORE = {
		0,90,90,90,90,90,90,0,57,50,39,25,39,16,32,16,32
	};
	protected static int HORSEEQUIP_SCORE = 18;
	protected static int[] JEWEL_SCORE = {
		0,144,245,416,707,1203,2045,3479
	};
	
	public PlayerLoadCall(PlayerService playerService, ClientSession session,
			int actorId, World world, int serial, String MIEI) {
		super(session);
		this.playerService = playerService;
		this.actorId = actorId;
		this.world = world;
		this.serial = serial;
		this.MIEI = MIEI;
		LogUtil.logLoginTry((Account)session.getIdentity(), actorId, MIEI);
	}
	
	public void callFinish() throws Exception {
		if (success) {
			if (session.isConnected()) {
				session.setClient(player);
				if(player.systemState!=Player.SYSTEMSTATE_LOAD){
					player.removeFromWorld();
				}
				player.loginTime = System.currentTimeMillis();
				
				ObjectAccessor.addGameObject(player);
				Map<Integer, PlayerRelation> relations = Server.server.getServiceRegistry().getRelationService().relations;
				if (!relations.containsKey(player.id)) {
					PlayerRelationDAO dao = Server.server.getServiceRegistry().getDbService().playerRelationDAO;
					PlayerRelation relation = dao.findPlayerRelation(player.id);
					relations.put(player.id, relation);
				}
				VMap map = world.addPlayerToMap(player, player.map.id,
						player.x, player.y,true);
				player.logined();
				player.moveType = 0;
				player.moveExtended = 0;
				Packet pt = new Packet(OpCode.ACTOR_LOGIN_SERVER);
				pt.putInt(serial);
				pt.put(player.toClientBytes());
				session.send(pt);
				
				// 下发小提示
				String hint = Server.server.getServiceRegistry().getDataService().getHint(player);
				String model = (player.getAccount()==null ? "" : player.getAccount().getModel());
				if (hint != null) {
					if(!"Lenovo".equals(model) && !"LenovoU1".equals(model)){
						Packet pt1 = new Packet(OpCode.PUSH_HINT_SERVER);
					    pt1.putString(hint);
					    session.send(pt1);
					}
				}
				
				Packet pt1 = new Packet(OpCode.GOMAP_ALLOW_SERVER);
				pt1.putInt(player.map.id);
				pt1.putInt(player.getVMap().getInstanceId());
				pt1.putInt(player.x);
				pt1.putInt(player.y);
				pt1.put(player.getVMap().allowFollow()?1:0);
				session.send(pt1);
//				session.send(pt);
				player.refreshTitles();
				player.refreshStar7Buff();
				player.refreshStarState();
				player.refreshAllAttendants();
			}
		} else {
			if(errorMessage==null){
				errorMessage = "该用户已下线";
			}
			ErrorHandler.sendErrorMessage(session, serial,
					OpCode.ACTOR_LOGIN_CLIENT,errorMessage);
		}

	}

	public void run() {
		Identity identity = session.getIdentity();
		if (identity == null) {
			error(null, "没有登录");
		} else {
			if (playerService.isMuted(actorId)) {
				error(null, "防作弊系统检测到您的角色状态异常，将临时隔离，请稍候再尝试登录");
			} else {
				CandidateService candidateService = Server.server.getServiceRegistry().getCandidateService();
				player = playerService.loadPlayer(identity.getId(), actorId);
				if (player != null) {
					player.checkActivePower();
					if (player.isKing() == 1) {
						player.buffs.addBuff(BuffUtil.createSuiteBuff(216, 1));
						if(ObjectAccessor.getSkill(Skills.getSkillId(candidateService.getKingSkillGroupId(player.clazz), 1))!=null)
								player.addSkill(ObjectAccessor.getSkill(Skills.getSkillId(candidateService.getKingSkillGroupId(player.clazz), 1)));
					} else {
						player.buffs.removeBuff(216);
						player.skills.removeSkill(candidateService.getKingSkillGroupId(player.clazz), 1);
						player.skills.removeSkill(candidateService.getKingSkillGroupId(player.clazz), 0);
						player.buffs.removeBuff(Skills.getSkillId(candidateService.getKingSkillGroupId(player.clazz), 1));
						Server.server.getServiceRegistry().getCandidateService().processKingItem(player);
					}
					// Server.server.getServiceRegistry().getNationService().checkOnKingMap(player);
					Server.server.getServiceRegistry().getRelationService()
							.removeAllRelation(player);
					Server.server.getServiceRegistry().getAccountDepotService()
					        .loadAccountDepot(player.accountId);
					playerEquipmentLog(player);
				}else{
					log.info("[LOADPLAYERCHEAT]ACCOUNT["+identity.getId()+"]ID["+actorId+"]");
					error(null, "没有找到指定角色");
				}
			}
		}
		addToClientSession();
	}
	
	protected void playerEquipmentLog(Player player){
		try {
			if(player!=null){
				//统计所有装备
				List<GameItem> equips = new ArrayList<GameItem>(); 
				//玩家背包中所有装备
				for(TransactionBagGrid grid : player.bag.getGrids()){
					GameItem item = grid.getItem();
					if(item!=null && item.template!=null && item.template.isEquipment())
						equips.add(item);
				}
				//玩家身上所有装备
				for(GameItem item : player.equipments.equs){
					equips.add(item);
				}
				//随从身上所有装备
				if(player.attendantBag!=null){
					for(Attendant attendant : player.attendantBag.attendants){
						for(GameItem item : attendant.equs){
							if(item!=null && item.template!=null && item.template.isEquipment()){
								equips.add(item);
							}
						}
					}
				}
				//玩家仓库中所有装备
				for(TransactionBagGrid grid : player.depot.getGrids()){
					GameItem item = grid.getItem();
					if(item!=null && item.template!=null && item.template.isEquipment())
						equips.add(item);
				}
				//坐骑身上所有装备
				for(Horse horse : player.horseBag.horses){
					for(GameItem item : horse.equs.equs){
						equips.add(item);
					}
				}
				int levelScore = 0; //统计用级别积分
				int starScore = 0; //统计用星级积分
				int jewelScore = 0; //统计用宝石积分
				int equipValue = 0; //统计装备价值
				int totalScore = 0; //总分
				StringBuilder sb = new StringBuilder();
				sb.append("[EQUIPSCORE]");
				sb.append(LogUtil.getPlayerLogString(player));
				//计算玩家等级分数
				levelScore += player.level * 600;
				for(GameItem item : equips){
					if(item!=null && item.template!=null && item.template.isEquipment()){
						if(item.object!=null && item.object instanceof ItemEnhance){
							ItemEnhance ie = (ItemEnhance)item.object;
							int star = ie.getStar();
							if(star>0){
								//计算玩家星级分数
								if(item.template.isHorseEquipment()){
									starScore += HORSEEQUIP_SCORE * 7 * star;
								}else{
									int minorType = item.template.equipment.minorType;
									int equipScore = EQUIP_SCORE[minorType];
									starScore += equipScore * 7 * star;
								}
								//计算装备基础分
								equipValue += item.template.equipment.value;
								//计算玩家宝石分数
								int[] jewels = ie.getJewelIDs();
								for(int id:jewels){
									ItemTemplate template = ObjectAccessor.getItemTemplate(id);
									if(template != null){
										jewelScore += JEWEL_SCORE[template.useLevel];
									}
								}
							}
						}
					}
				}
				totalScore += (equipValue + starScore + jewelScore + levelScore);
				sb.append("EQUIPVALUE["+equipValue+"]");
				sb.append("STARSCORE["+starScore+"]");
				sb.append("JEWELSCORE["+jewelScore+"]");
				sb.append("LEVELSCORE["+levelScore+"]");
				sb.append("TOTALSCORE["+totalScore+"]");
				log.info(sb.toString());
			}
		} catch (Exception e) {
			log.error(e);
		}
	}
	
}
