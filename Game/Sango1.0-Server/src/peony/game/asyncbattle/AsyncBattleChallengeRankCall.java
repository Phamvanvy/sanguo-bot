package peony.game.asyncbattle;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import peony.common.ClientSessionAsyncCall;
import peony.game.ErrorHandler;
import peony.game.NoEnoughValueException;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.shop.NoItemShopBuy;
import peony.service.shop.ShopService;
import peony.util.TimeUtil;

/**
 * 擂台榜单
 * @author dchen
 */
public class AsyncBattleChallengeRankCall extends ClientSessionAsyncCall {
	
	private Logger log = Logger.getLogger(AsyncBattleChallengeRankCall.class);

	protected int serial;
	protected Player player;
	protected int type;
	
	public AsyncBattleChallengeRankCall(ClientSession session, Packet packet) {
		super(session);
		this.serial = packet.getInt();
		this.type=packet.get();	 //0-普通列表  1-换一批列表
		this.player = (Player)session.getClient();
	}

	public void callFinish() throws Exception {
	}

	public void run() {
		/**
		 * 异步战场：擂台战排行榜单返回
		 * serial					int
		 * size						byte			
		 * 		id					int				玩家ID
		 * 		name				String			名字
		 * 		sex					byte			性别
		 * 		clazz				byte			职业
		 * 		level				byte			级别
		 * 		headscore			int	`			头部image
		 * 		rank				int				排名
		 * size2
		 * 		id					int				玩家ID
		 * 		name				String			名字
		 * 		role				byte			0为挑战者 1为被挑战者
		 * 		date				String			时间
		 * 		result`				byte			0为失败 1为胜利
		 * 		rank				int				提升或者降低的名次
		 * selfrank					int				本人排名
		 * leave					byte			本天剩余挑战次数
		 * leaveImoney				byte			本天剩余元宝挑战次数
		 */
		if(player!=null){
			AsyncBattleService service = Server.server.getServiceRegistry().getAsyncBattleService();
			AsyncNormalBoard board = service.getAsyncNormalBoardByPlayerId(player.id);
			Packet pt = new Packet(OpCode.ASYNCBATTLE_CHALLENAGE_RANK_SERVER);
			pt.putInt(serial);
			pt.put(type);
			if(board!=null){
				int selfRank = board.rank;
				int[] challengers = service.getChallenges(selfRank);
				if(this.type == 1){//换一批
					if(selfRank<=100){
						ErrorHandler.sendErrorMessage(session, serial, OpCode.ASYNCBATTLE_CHALLENGE_RANK_CLIENT, "此功能只有擂台排名100名以外的玩家才可使用");
						return;
					}
					PlayerTransaction tx = player.newTransaction("ASYNCBATTLECHANGEGROUP");
					try {
						player.decCredit(AsyncBattleService.CHANGEGROUPNEEDCREDIT, tx, false);
						tx.commit();
					} catch (NoEnoughValueException e) {
						tx.rollback();
						ErrorHandler.sendErrorMessage(session, serial, OpCode.ASYNCBATTLE_CHALLENGE_RANK_CLIENT,peony.Messages.STRING_01159);
						return;
					}
					//换一批新规则
					if(challengers.length>2){
						int[] challengers2=new int[challengers.length];
						System.arraycopy(challengers, 0, challengers2, 0, challengers.length);
						board.changeNextGroupFlag++;
						boolean useOther=true;
						for(int i=0;i<challengers2.length-2;i++){
							challengers2[i]=challengers2[i]+board.changeNextGroupFlag;
							if(challengers2[i]==challengers2[challengers2.length-1]||challengers2[i]==challengers2[challengers2.length-2]){
								useOther=false;
							}
						}
						board.battlePlayersChangeGroup.clear();
						if(useOther){
							System.arraycopy(challengers2, 0, challengers, 0, challengers2.length);
							for(int i=0;i<challengers.length;i++){
								board.battlePlayersChangeGroup.put(i, challengers[i]);
							}
						}else{
							for(int i=0;i<challengers.length;i++){
								board.battlePlayersChangeGroup.put(i, challengers[i]);
							}
							board.changeNextGroupFlag=0;
						}
					}
				}else if(type==0){
					if(board.rank!=board.oldRank){//排名变化时的第一次打开界面进行赋值
						board.changeNextGroupFlag=0;
						board.oldRank=board.rank;
						board.battlePlayersChangeGroup.clear();
						for(int i=0;i<challengers.length;i++){
							board.battlePlayersChangeGroup.put(i, challengers[i]);
						}
					}
					if(board.battlePlayersChangeGroup.size()>0){
						challengers=new int[board.battlePlayersChangeGroup.size()];
						int index=0;
						for(int rank:board.battlePlayersChangeGroup.values()){
							challengers[index++]=rank;
						}
					}
				}
				List<Player> battlePlayers=new ArrayList<Player>();
				List<Integer> playerRank=new ArrayList<Integer>();
				try {
					for(int i=challengers.length-1;i>=0;i--){
						int targetRank = challengers[i];
						if(service.getAsyncNormalBoardByRank(targetRank)==null){
							continue;
						}
						int targetId = service.getAsyncNormalBoardByRank(targetRank).playerId;
						if(service.getPlayerInfo(targetId)==null){
							continue;
						}
						Player target = service.getPlayerInfo(targetId);
						battlePlayers.add(target);
						playerRank.add(targetRank);
						board.battlePlayers.put(i, target.id);
					}
					pt.put(battlePlayers.size());
					for(int i=0;i<battlePlayers.size();i++){
						Player target=battlePlayers.get(i);
						int targetRank=playerRank.get(i);
						pt.putInt(target.id);
						pt.putString(target.name);
						pt.put(target.faction);
						pt.put(target.sex);
						pt.put(target.clazz);
						pt.put(target.level);
						pt.putInt(target.equipments.getHeadScore(target.level, target.clazz));
						pt.putInt(targetRank);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				pt.put(board.battleInfos.size());
				for(int i=board.battleInfos.size()-1;i>=0;i--){
					AsyncBattleInfo targetInfo=board.battleInfos.get(i);
					int targetId = targetInfo.targetId;
				    pt.putInt(targetId);
				    Player target = service.getPlayerInfo(targetId);
				    pt.putString(target.name);
				    pt.put(target.faction);
				    pt.put(target.clazz);
				    pt.put(targetInfo.role);
				    Date date = targetInfo.date;
				    pt.putString(getTimeInfo(date)+"前");
				    pt.put(targetInfo.win ? 1 : 0);
				    pt.putInt(targetInfo.rank);
				    pt.put(targetInfo.battleResult);
				}
				
				pt.putInt(board.rank);
				int leaveCount1=AsyncBattleService.BATTLECOUNT-board.battleCount;
				if(leaveCount1<0){
					leaveCount1=0;
				}
				pt.put(leaveCount1);
				int leaveCountImoney=0;//
				int vipCount=0;
				if(player.vipLevel<12){
					vipCount=AsyncBattleService.vipBuyCount[player.vipLevel];
				}
				if(board.battleCount<AsyncBattleService.BATTLECOUNT){
					leaveCountImoney=AsyncBattleService.BATTLECOUNT_MAX+vipCount-AsyncBattleService.BATTLECOUNT;
				}else{
					leaveCountImoney=AsyncBattleService.BATTLECOUNT_MAX+vipCount-board.battleCount;
				}
				if(leaveCountImoney<0){
					leaveCountImoney=0;
				}
				if(player.vipLevel==12){
					leaveCountImoney=99;
				}
				pt.put(leaveCountImoney);
				pt.putInt(board.officerScore);
				ShopService shopService=Server.server.getServiceRegistry().getShopService();
				int itemMoney=Math.round(shopService.getItemPrice(NoItemShopBuy.WUYUANBAO))/36;
				int moneyCount = AsyncBattleService.IMONEY_BATTLE / 5;
				pt.put(itemMoney*moneyCount);
			}
			player.send(pt);
		}
	}
	
	private static String getTimeInfo(Date date){
		int timeDis = (int) ((System.currentTimeMillis() - date.getTime()) / 1000);
		return TimeUtil.getStringH_M_S(timeDis);
	}

}
