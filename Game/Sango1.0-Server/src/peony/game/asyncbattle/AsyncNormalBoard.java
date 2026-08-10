package peony.game.asyncbattle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Type;

import peony.game.Time;

/**
 * 异步战场普通榜单
 * 
 * @author dchen
 */
@Entity
@Table(name = "asyncnormalboard")
@AccessType("field")
public class AsyncNormalBoard {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id")
	public int id;

	@Column(name = "playerid", nullable = false)
	public int playerId; // 角色ID
	
	@Column(name = "faction", nullable = false)
	public int faction;

	@Column(name = "rank", nullable = false)
	public int rank; // 名次
	@Type(type = "peony.game.asyncbattle.AsyncBattleInfoUserType")
	@Column(name = "battleinfo")
	public List<AsyncBattleInfo> battleInfos = new ArrayList<AsyncBattleInfo>(
			10); // 最近挑战过本人的10个挑战者
	@Column(name = "uprank", nullable = false)
	public int upRank; // 当日提升名次
	
	@Column(name = "upranktime", nullable = false)
	public int upRankTime; // 当日提升名次的时间
	
	protected int getUpRankTime() {
		return upRankTime;
	}

	protected void setUpRankTime(int upRankTime) {
		this.upRankTime = upRankTime;
	}

	@Column(name = "loginday", nullable = false)
	public int loginDay; // 最后一次loginDay
	@Column(name = "name", nullable = false)
	public String name = "";
	@Column(name = "clazz", nullable = false)
	public int clazz;
	@Column(name = "level", nullable = false)
	public int level;
	@Column(name = "battlecount", nullable = false)
	public int battleCount;// 已经挑战次数

	@Column(name = "officerindex", nullable = false)
	public int officerIndex;// 官职索引(默认为最低等)
	public static final int ACHIEVEMENT_TYPE_500 = 0;// 首进500
	public static final int ACHIEVEMENT_TYPE_200 = 1;// 首进200
	public static final int ACHIEVEMENT_TYPE_100 = 2;// 首进100
	public static final int ACHIEVEMENT_TYPE_50 = 3;// 首进50
	public static final int ACHIEVEMENT_TYPE_10 = 4;// 首进10
	public static final int ACHIEVEMENT_TYPE_5 = 5;// 首进1
	
	public static final int ACHIEVEMENT_TOTAL_1 = 6;//保持第一5天
	public static final int ACHIEVEMENT_TOTAL_2_10 = 7;//保持第2-10 5天
	public static final int ACHIEVEMENT_TOTAL_11_50 = 8;//保持第11-50 5天

	public static final int ACHIEVEMENT_100 = 100;
	public static final int ACHIEVEMENT_500 = 500;
	public static final int ACHIEVEMENT_200 = 200;
	public static final int ACHIEVEMENT_50 = 50;
	
	public static final int BATTLERESULT_WIN=1;
	public static final int BATTLERESULT_LOSE=0;
	
	@Transient
	public int oldRank;

	@Column(name = "dayflag", nullable = false)
	public int dayFlag;// 每天的官职奖励标志1
	
	@Column(name = "dayflag2", nullable = false)
	public int dayFlag_GetRewardTime;//每天的官职奖励标志1(即领奖时间)

	@Column(name = "achievementstate")
	public byte[] achievementState = new byte[] { 0, 0, 0, 0, 0, 0, 0 };// 特殊奖励状态  0-未达到  1-可领取  2-已经领取完
	
	@Column(name = "achievementstatenew")
	public int[] achievementStateNew = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};// 特殊奖励状态  0-未达到  1-可领取  2-已经领取完

	@Column(name = "officerscore", nullable = false)
	public int officerScore;// 官职积分
	
	@Transient
	public int officerScore_Day;//每天获得的积分
	
	@Transient
	public int lastBattleResult;//最后一次挑战的结果0失败，1胜利
	
	protected int getOfficerScore_Day() {
		return officerScore_Day;
	}

	protected void setOfficerScore_Day(int officerScore_Day) {
		this.officerScore_Day = officerScore_Day;
	}

	@Transient
	public HashMap<Integer, Integer> battlePlayers = new HashMap<Integer, Integer>();//index,targetid
	
	@Transient
	public HashMap<Integer, Integer> battlePlayersChangeGroup = new HashMap<Integer, Integer>();//换一批
	

	@Transient
	public int changeNextGroupFlag;//换一批标志9次之后循环
	
	public AsyncNormalBoard() {
		officerIndex = AsyncBattleService.OFFICER_NAME.length - 1;
	}

	protected int getOfficerIndex() {
		return officerIndex;
	}

	protected void setOfficerIndex(int officerIndex) {
		this.officerIndex = officerIndex;
	}

	protected int getDayFlag_GetRewardTime() {
		return dayFlag_GetRewardTime;
	}

	protected void setDayFlag_GetRewardTime(int dayFlag_GetRewardTime) {
		this.dayFlag_GetRewardTime = dayFlag_GetRewardTime;
	}

	protected int getOfficerScore() {
		return officerScore;
	}

	protected void setOfficerScore(int officerScore) {
		this.officerScore = officerScore;
	}

	protected String getName() {
		return name;
	}

	protected void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getPlayerId() {
		return playerId;
	}

	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}

	protected int getFaction() {
		return faction;
	}

	protected void setFaction(int faction) {
		this.faction = faction;
	}

	public int getRank() {
		return rank;
	}

	public int getClazz() {
		return clazz;
	}

	public void setClazz(int clazz) {
		this.clazz = clazz;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getBattleCount() {
		return battleCount;
	}

	public void setBattleCount(int battleCount) {
		this.battleCount = battleCount;
	}

	public int getDayFlag() {
		return dayFlag;
	}

	public void setDayFlag(int dayFlag) {
		this.dayFlag = dayFlag;
	}

	public byte[] getAchievementState() {
		return achievementState;
	}

	public void setAchievementState(byte[] achievementState) {
		this.achievementState = achievementState;
	}

	public void setRank(int rank) {
		this.rank = rank;
		if (achievementStateNew[ACHIEVEMENT_TYPE_500] == 0
				&& rank <= ACHIEVEMENT_500) {
			achievementStateNew[ACHIEVEMENT_TYPE_500] = 1;
		}
		if (achievementStateNew[ACHIEVEMENT_TYPE_200] == 0
				&& rank <= ACHIEVEMENT_200) {
			achievementStateNew[ACHIEVEMENT_TYPE_200] = 1;
		}
		if (achievementStateNew[ACHIEVEMENT_TYPE_50] == 0
				&& rank <= ACHIEVEMENT_50) {
			achievementStateNew[ACHIEVEMENT_TYPE_50] = 1;
		}
	}

	public int getUpRank() {
		return upRank;
	}

	public void setUpRank(int upRank) {
		this.upRank = upRank;
		this.upRankTime=Time.currTime;
	}

	public int getLoginDay() {
		return loginDay;
	}

	public void setLoginDay(int loginDay) {
		this.loginDay = loginDay;
	}

	public List<AsyncBattleInfo> getBattleInfos() {
		return battleInfos;
	}

	public void setBattleInfos(List<AsyncBattleInfo> battleInfos) {
		this.battleInfos = battleInfos;
	}

	public static byte[] getAsyncNormalBoardListDBBytes(
			List<AsyncBattleInfo> battleInfos) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(200);
		DataOutputStream dos = new DataOutputStream(baos);
		try {
			dos.write(1);
			dos.writeInt(battleInfos.size());
			for (AsyncBattleInfo info : battleInfos) {
				dos.write(AsyncBattleInfo.getAsyncBattleInfoDBBytes(info));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return baos.toByteArray();
	}

	public static List<AsyncBattleInfo> getAsyncBattleInfoFromDB(byte[] bytes) {
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		DataInputStream dis = new DataInputStream(bais);
		List<AsyncBattleInfo> infos = new ArrayList<AsyncBattleInfo>();
		try {
			int version = dis.read();
			int size = dis.readInt();
			for (int i = 0; i < size; i++) {
				AsyncBattleInfo info = AsyncBattleInfo
						.getAsyncBattleInfoFromDB(dis, version);
				infos.add(info);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return infos;
	}
	
	/**重置每天的挑战次数及相关*/
	public void resetParaForDay(){
		this.battleCount=0;
		this.upRank=0;
		this.dayFlag=0;
		this.dayFlag_GetRewardTime=1;
		this.officerScore_Day=0;
	}
	
	/***
	 * 检查首进奖励
	 */
	public synchronized void checkFirstEnterReward(){
		//处理奖励
		if(achievementStateNew[AsyncNormalBoard.ACHIEVEMENT_TYPE_500]!=-1
				&&rank<=AsyncNormalBoard.ACHIEVEMENT_500){
			achievementStateNew[AsyncNormalBoard.ACHIEVEMENT_TYPE_500]=1;
		}
		if(achievementStateNew[AsyncNormalBoard.ACHIEVEMENT_TYPE_200]!=-1
				&&rank<=AsyncNormalBoard.ACHIEVEMENT_200){
			achievementStateNew[AsyncNormalBoard.ACHIEVEMENT_TYPE_200]=1;
		}
		if(achievementStateNew[AsyncNormalBoard.ACHIEVEMENT_TYPE_100]!=-1
				&&rank<=AsyncNormalBoard.ACHIEVEMENT_100){
			achievementStateNew[AsyncNormalBoard.ACHIEVEMENT_TYPE_100]=1;
		}
		if(achievementStateNew[AsyncNormalBoard.ACHIEVEMENT_TYPE_50]!=-1
				&&rank<=AsyncNormalBoard.ACHIEVEMENT_50){
			achievementStateNew[AsyncNormalBoard.ACHIEVEMENT_TYPE_50]=1;
		}
		//累计奖励
	}
	
	/**检测累计奖励状态*/
	public void checkTotalRewardState(){
//		if(Time.currentWeekDay==Calendar.TUESDAY){//周二清空累计数据
//			for(int i=ACHIEVEMENT_TOTAL_1;i<=ACHIEVEMENT_TOTAL_11_50;i++){
//				achievementState[i]=1;
//			}
//		}else{
			if(rank==1/*&&achievementStateNew[ACHIEVEMENT_TOTAL_1]!=-1*/){
				achievementStateNew[ACHIEVEMENT_TOTAL_1]+=1;
			}
			if(rank<=10/*&&achievementStateNew[ACHIEVEMENT_TOTAL_2_10]!=-1*/){
				achievementStateNew[ACHIEVEMENT_TOTAL_2_10]+=1;
			}
			if(rank<=50/*&&achievementStateNew[ACHIEVEMENT_TOTAL_11_50]!=-1*/){
				achievementStateNew[ACHIEVEMENT_TOTAL_11_50]+=1;
			}
//		}
//		System.out.println(name);
//		for(byte i:achievementState){
//			System.out.print(i+" ");
//		}
//		System.out.println();
	}
	
	public void transToNewAchieve(){
		if(achievementStateNew==null){
			achievementStateNew=new int[20];
			for(int i=0;i<achievementState.length;i++){
				int index=i;
				if(i >= 4){
					index=i+2;
				}
				achievementStateNew[index]=achievementState[i];
			}
		}
	}
}
