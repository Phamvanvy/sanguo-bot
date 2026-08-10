package peony.game.asyncbattle;

import org.apache.log4j.Logger;

import peony.common.SyncIbuyCall;
import peony.game.ErrorHandler;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.shop.NoItemShopBuy;

public class AsyncBattleCountIBugCall extends SyncIbuyCall {
	private static final Logger log = Logger.getLogger(AsyncBattleCountIBugCall.class);
	private Player player;
	private int serial;
	private int battleType;
	private int targetIndex;
	
	public static int[] cannotSignUpMaps = new int[]{2064,2065,2066,2067};
	
	public AsyncBattleCountIBugCall(ClientSession session, Packet packet) {
		super(session, packet);
		player=(Player)session.getClient();
		serial=packet.getInt();
		this.battleType=packet.get();
		this.targetIndex=packet.get();
	}

	public void callFinish() throws Exception {
		
	}

	public void run() {
			if (player != null) {
			if (player.map.map != null
					&& player.map.map.getId() == AsyncBattleService.battleMap) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.ASYNCBATTLE_CHALLENGE_CLIENT, "您已经在此场景中");
				return;
			}
			if (player.getThreatCount() > 0) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.ASYNCBATTLE_CHALLENGE_CLIENT,
						"战斗状态下无法进入擂台赛");
				return;
			}
			if (player.getVMap() != null && player.getVMap().instance != null || !canSignUp(player)) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.ASYNCBATTLE_CHALLENGE_CLIENT,
						"副本中无法进入擂台赛");
				return;
			}
			if (player.isInStep) {
				ErrorHandler.sendErrorMessage(session, serial,
						OpCode.ASYNCBATTLE_CHALLENGE_CLIENT,
						"跨服战中无法进入擂台赛");
				return;
			}
			try {
				AsyncBattleService service = Server.server.getServiceRegistry()
						.getAsyncBattleService();
				AsyncNormalBoard board = service
						.getAsyncNormalBoardByPlayerId(player.id);
				if (board != null) {
					int vipCount=0;
					if(player.vipLevel<12){
						vipCount=AsyncBattleService.vipBuyCount[player.vipLevel];
					}
					if (board.battleCount >= AsyncBattleService.BATTLECOUNT_MAX+vipCount && player.vipLevel < 12) {//没有挑战次数了
						ErrorHandler.sendErrorMessage(session, serial,
								OpCode.ASYNCBATTLE_CHALLENGE_CLIENT,
								"您今日的挑战次数已用完，提升vip等级可购买更多的挑战次数。");
						return;
					}
					if (battleType == 0) {
						if (board.battleCount >= AsyncBattleService.BATTLECOUNT) {
							int shopId = Server.server.getServiceRegistry()
									.getShopService().getShopByItemId(
											NoItemShopBuy.WUYUANBAO).id;
							int moneyCount = AsyncBattleService.IMONEY_BATTLE / 5;
							waitBuy(player, 0, shopId, NoItemShopBuy.WUYUANBAO,
									moneyCount, this);
							log.info("[ASYNCBATTLECOUNTIBUYCALL_BATTLE]ACC["+player.accountId+"]ID["+player.id+"]IMONEY["+AsyncBattleService.IMONEY_BATTLE+"]");
						}
						int targetPlayerId = board.battlePlayers
								.get(board.battlePlayers.size()-1-targetIndex);
						player.goMap(AsyncBattleService.battleMap, 720, 442);
						player.asyncTargetId = targetPlayerId;
						board.oldRank=board.rank;
					} else if (battleType == 1) {
						int targetId = board.battleInfos.get(board.battleInfos.size()-1-targetIndex).targetId;
						AsyncNormalBoard boardTarget = service
								.getAsyncNormalBoardByPlayerId(targetId);
						if (board.rank < boardTarget.rank) {
							ErrorHandler.sendErrorMessage(session, serial,
									OpCode.ASYNCBATTLE_CHALLENGE_CLIENT,
									"不允许攻击排名比您低的玩家");
							return;
						}
						if (board.battleCount >= AsyncBattleService.BATTLECOUNT) {
							int shopId = Server.server.getServiceRegistry()
									.getShopService().getShopByItemId(
											NoItemShopBuy.WUYUANBAO).id;
							int moneyCount = AsyncBattleService.IMONEY_BATTLE / 5;
							waitBuy(player, 0, shopId, NoItemShopBuy.WUYUANBAO,
									moneyCount, this);
							log.info("[ASYNCBATTLECOUNTIBUYCALL_BEATBACK]ACC["+player.accountId+"]ID["+player.id+"]IMONEY["+AsyncBattleService.IMONEY_BATTLE+"]");
						}
						player.goMap(AsyncBattleService.battleMap, 720, 442);
						player.asyncTargetId = targetId;
						board.oldRank=board.rank;
					}
					log.info("[ASYNCBATTLECOUNTIBUYCALL_EVERYTIME]ACC["+player.accountId+"]ID["+player.id+"]BATTLETYPE["+battleType+"]");
				}
				int vipCount=0;
				if(player.vipLevel<12){
					vipCount=AsyncBattleService.vipBuyCount[player.vipLevel];
				}
				if (board.battleCount < AsyncBattleService.BATTLECOUNT_MAX+vipCount) {
					board.battleCount++;
				}
				Packet pt = new Packet(OpCode.ASYNCBATTLE_CHALLENGE_SERVER);
				pt.putInt(serial);
				player.send(pt);
				addToClientSession();
			} catch (Exception e) {
			}
		}
	}
	
	protected boolean canSignUp(Player player){
		if(player!=null){
			for(int id : cannotSignUpMaps){
				if(player.map!=null && player.map.id==id)
					return false;
			}
		}
		return true;
	}
	
}
