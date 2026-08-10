package peony.game.stepserver;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Type;
import peony.game.PropertyPool;

@Entity
@Table(name = "stepbattlescoretop16")
@AccessType("field")
public class StepBattleScoreTop16 {

		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		@Column(name = "id")
		public int id;

		@Column(name = "playerid", nullable = false)
		public int playerid;
		
		@Column(name = "accountid", nullable = false)
		public int accountId;
		
		@Column(name = "name", nullable = false)
		public String name;
		
		@Column(name = "faction", nullable = false)
		public int faction;
		
		@Column(name = "wincount", nullable = false)
		public int winCount;
		
		@Column(name = "time", nullable = false)
		public long time;
		
		@Column(name = "gameCode", nullable = true)
		public String gameCode;
		
		@Column(name = "poolbet")
		@Type(type = "peony.game.PropertyPoolType")
		public PropertyPool poolbet;
		
		@Column(name = "poolwatch")
		@Type(type = "peony.game.PropertyPoolType")
		public PropertyPool poolwatch;

		
		@Column(name = "ranking", nullable = false)
		public int ranking;//名次
		
		@Transient
		public static String BETFLAG="BETFLAG";
		@Transient
		public static String WATCHFLAG="WATCHFLAG";
		
		@Transient
		public int onLineState=0;//在线状态
		
		/***
		 * 是否已经观战
		 * @param playerid
		 * @param gamecode
		 * @return
		 */
		public boolean hadWatch(int playerid,String gamecode){
			if(poolwatch!=null){
				String[] allWatchPlayers=poolwatch.getString(WATCHFLAG).split("\\|");
				if(allWatchPlayers!=null&&allWatchPlayers.length>0){
					for(int i=0;i<allWatchPlayers.length;i++){
						String[] playerWatch=allWatchPlayers[i].split(",");
						if(playerid==Integer.parseInt(playerWatch[0])&&playerWatch[1].equals(gamecode)){
							return true;
						}
					}
				}
			}
			return false;
		}
		
		public void addWatchPlayer(int sourcePlayerId,String sourcePlayerGameCode){
			if(poolwatch!=null){
				String allWatchPlayers=poolwatch.getString(WATCHFLAG);
				String updateScore=null;
				updateScore=allWatchPlayers+"|"+sourcePlayerId+","+sourcePlayerGameCode;
				if(updateScore!=null&&updateScore.startsWith("|")){
					updateScore=updateScore.substring(1);
				}
				poolwatch.setString(WATCHFLAG, updateScore);
			}
		}
		
		
		/***
		 * 获取押注数额计算获取
		 * @return
		 */
		public long getBet(){
			if(poolbet!=null){
				String[] allBetPlayers=poolbet.getString(BETFLAG).split("\\|");
				long bet=0;
				for(int i=0;i<allBetPlayers.length;i++){
					if(allBetPlayers[i]!=null&&!allBetPlayers[i].equals("")){
						String[] playerAndBet=allBetPlayers[i].split(",");
						bet+=Integer.parseInt(playerAndBet[2]);
					}
				}
				return bet;
			}
			return 0;
		}
		/**
		 * 当前玩家押注额
		 * @param sourcePlayerId             押注玩家的PlayerId
		 * @param sourcePlayerGameCode		 押注玩家的GameCode
		 * @return
		 */
		public int getPlayerBetCoins(int sourcePlayerId,String sourcePlayerGameCode){
			if(poolbet!=null){
				String allBetPlayers=poolbet.getString(BETFLAG);
				if(allBetPlayers!=null&&!allBetPlayers.equals("")){
					String[]  bets=allBetPlayers.split("\\|");
					if(bets!=null&&bets.length>0){
						for(int i=0;i<bets.length;i++){
							if(bets[i]!=null&&!bets[i].equals("")){
								String[] playerBetInfo=bets[i].split(",");
								if(playerBetInfo[0].equals(sourcePlayerId+"")&&playerBetInfo[1].equals(sourcePlayerGameCode)){
									return Integer.parseInt(playerBetInfo[2]);
								}
							}
						}
					}
				}
			}
			return 0;
		}
		
		/***
		 * 获取每个玩家的押注信息
		 * @return
		 */
		public String[] getAllPlayersBetCoins(){
			String[] all=null;
			if(poolbet!=null){
				String allBetPlayers=poolbet.getString(BETFLAG);
				if(allBetPlayers.startsWith("|")){
					allBetPlayers=allBetPlayers.substring(1);
				}
				return allBetPlayers.split("\\|");
			}
			return all;
		}
		
		
		/**
		 * 添加押注玩家及数额
		 * @param sourcePlayerId
		 * @param betCoins
		 */
		public void addBet(int sourcePlayerId,String sourcePlayerGameCode,int betCoins){
			if(poolbet!=null){
				String allBetPlayers=poolbet.getString(BETFLAG);
				String updateScore=hadBet(sourcePlayerId, sourcePlayerGameCode, allBetPlayers, betCoins);
				if(updateScore.startsWith("|")){
					updateScore=updateScore.substring(1);
				}
				poolbet.setString(BETFLAG, updateScore);
			}
		}
		public String hadBet(int sourcePlayerId,String sourcePlayerGameCode,String allBetPlayers,int betCoins){
			StringBuffer newBet=new StringBuffer();
			if(allBetPlayers!=null){
				String[] players=allBetPlayers.split("\\|");
				if(players!=null){
					int count=0;
					for(int i=0;i<players.length;i++){
						String[] playerInfo=players[i].split(",");
						if(playerInfo!=null&&playerInfo.length==3){
							if(playerInfo[0].equals(sourcePlayerId+"")&&playerInfo[1].equals(sourcePlayerGameCode)){
								playerInfo[2]=(Integer.parseInt(playerInfo[2])+betCoins)+"";
								players[i]=playerInfo[0]+","+playerInfo[1]+","+playerInfo[2];
								count++;
							}
							if(i==0){
								newBet.append(players[i]);
							}else{
								newBet.append("|"+players[i]);
							}
						}
					}
					if(count==0){
						newBet.append("|"+sourcePlayerId+","+sourcePlayerGameCode+","+betCoins);
					}
				}
			}
			return newBet.toString();
		}
	
		public StepBattleScoreTop16(){
			this.poolbet=new PropertyPool();
			this.poolwatch=new PropertyPool();
		}

		protected int getId() {
			return id;
		}

		protected void setId(int id) {
			this.id = id;
		}

		protected int getPlayerid() {
			return playerid;
		}

		protected void setPlayerid(int playerid) {
			this.playerid = playerid;
		}

		protected int getAccountId() {
			return accountId;
		}

		protected void setAccountId(int accountId) {
			this.accountId = accountId;
		}

		protected String getName() {
			return name;
		}

		protected void setName(String name) {
			this.name = name;
		}

		protected int getFaction() {
			return faction;
		}

		protected void setFaction(int faction) {
			this.faction = faction;
		}

		protected int getWinCount() {
			return winCount;
		}

		protected void setWinCount(int winCount) {
			this.winCount = winCount;
		}

		protected long getTime() {
			return time;
		}

		protected void setTime(long time) {
			this.time = time;
		}

		protected String getGameCode() {
			return gameCode;
		}

		protected void setGameCode(String gameCode) {
			this.gameCode = gameCode;
		}
		protected PropertyPool getPoolbet() {
			return poolbet;
		}


		protected void setPoolbet(PropertyPool poolbet) {
			this.poolbet = poolbet;
		}


		protected PropertyPool getPoolwatch() {
			return poolwatch;
		}


		protected void setPoolwatch(PropertyPool poolwatch) {
			this.poolwatch = poolwatch;
		}


		protected int getRanking() {
			return ranking;
		}


		protected void setRanking(int ranking) {
			this.ranking = ranking;
		}
}
