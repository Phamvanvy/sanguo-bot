package peony.game.instance;

import java.util.ArrayList;
import java.util.List;
import peony.game.Player;


public class Score {
	
	public int bossId;
	public BossScore[] bossScores = new BossScore[11];
	public BossTimeScore[] timeScores = new BossTimeScore[10];
	
	public Score(int bossId) {
		super();
		this.bossId = bossId;
	}

	public void addBossTimeScore(BossTimeScore bossTimeScore){
		int index = getBossTimeScoresSize();
		timeScores[index] = bossTimeScore;
	}
	
	public int getBossScoresSize(){
		int count = 0;
		for(BossScore bossScore : bossScores){
			if(bossScore!=null)
				count++;
		}
		return count;
	}
	
	public int getBossTimeScoresSize(){
		int count = 0;
		for(BossTimeScore bossTimeScore : timeScores){
			if(bossTimeScore!=null)
				count++;
		}
		return count;
	}
	
	/**
	 * 获取杀死BOSS时间最长的排行榜index
	 */
	public int getLongestTimeScore(){
		bubbleBossTimeScores();
		int index = 0;
		int time = 0;
		for(int i=0;i<10;i++){
			BossTimeScore bossTimeScore = timeScores[i];
			if(bossTimeScore!=null && bossTimeScore.time>=time){
				index = i;
				time = bossTimeScore.time;
			}
		}
		return index;
	}
	
	/**
	 * 获取队伍在最快杀死BOSS排行榜上的index
	 */
	public int getPartyIndex(List<Player> owners){
		int count = owners.size();
		for(int i=0;i<timeScores.length;i++){
			BossTimeScore bossTimeScore = timeScores[i];
			if(bossTimeScore!=null){
				int listSize = bossTimeScore.members.list.size();
				if(listSize==count){
					List<Integer> ids = new ArrayList<Integer>();
					for(int x=0;x<listSize;x++){
						ids.add(bossTimeScore.members.list.get(x).id);
					}
					int count1 = 0;
					for(Player member : owners){
						if(ids.contains(member.id))
							count1++;
					}
					if(count1==count)
						return i;
				}
			}
		}
		return -1;
	}
	
	/**
	 * 刷新最快击杀BOSS的排行表
	 */
	public void bubbleBossTimeScores(){
		for(int i=0;i<getBossTimeScoresSize();i++){
			for(int j=i+1;j<getBossTimeScoresSize();j++){
				if(timeScores[i].time>=timeScores[j].time){
					BossTimeScore temp;
					temp = timeScores[i];
					timeScores[i] = timeScores[j];
					timeScores[j] = temp;
				}
			}
		}
	}
	
	/**
	 * 判断此队伍中是否有人已经在最早杀死BOSS的榜上
	 */
	public boolean hasOnScoreBoard(List<Player> owners){
		for(Player p : owners){
			for(BossScore bossScore : bossScores){
				if(bossScore!=null){
					for(Member member2 : bossScore.members.list){
						if(member2.id==p.id){
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * 判断此队伍是否已经在最快杀死BOSS的榜上
	 */
	public BossTimeScore partyHasOnTimeScoreBoard(List<Player> owners){
		int count = owners.size();
		for(BossTimeScore bossTimeScore : timeScores){
			if(bossTimeScore!=null){
				int listSize = bossTimeScore.members.list.size();
				if(listSize==count){
					List<Integer> ids = new ArrayList<Integer>();
					for(int x=0;x<listSize;x++){
						ids.add(bossTimeScore.members.list.get(x).id);
					}
					int count1 = 0;
					for(Player member : owners){
						if(ids.contains(member.id))
							count1++;
					}
					if(count1==count)
						return bossTimeScore;
				}
			}
		}
		return null;
	}
	
	/**
	 * 判断队伍中是否有人的级别大于BOSS的级别10级以上
	 */
	public boolean checkLevel(List<Player> owneres, int bossLevel){
		for(Player p : owneres){
			if(p.level > (bossLevel+10))
				return true;
		}
		return false;
	}
	
	/**
	 * 获取当前最大名次
	 */
	public int getMaxScore(){
		int count = 0;
		for(BossScore bossScore : bossScores){
			if(bossScore!=null && bossScore.score!=11 && bossScore.score>count)
				count = bossScore.score;
		}
		return count;
	}
	
}
