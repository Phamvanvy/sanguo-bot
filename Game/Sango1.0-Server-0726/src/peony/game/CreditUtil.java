package peony.game;

import java.util.List;

import com.pip.sanguo.data.DataObject;
import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.Rank;

public class CreditUtil {
	public static int[] getCredit(int winnerLevel,int loserLevel){
		int[] ret = new int[2];
		int diff = loserLevel - winnerLevel;
		if(diff<=-11){
			ret[0] = 0;
			ret[1] = 0;
		}
		else if(diff>=-10&&diff<=-6){
			ret[0] = 1;
			ret[1] = -2;
		}
		else if(diff>=-5&&diff<=5){
			ret[0] = 2;
			ret[1] = -3;
		}
		else if(diff>=6&&diff<=10){
			ret[0] = 4;
			ret[1] = -5;
		}
		else {
			ret[0] = 6;
			ret[1] = -7;
		}
		return ret;
	}
	
	public static int[] getFlagBattleFieldCredit(int winnerLevel,int loserLevel){
		int[] ret = new int[2];
		int diff = loserLevel - winnerLevel;
		if(diff<=-11){
			ret[0] = 0;
			ret[1] = 0;
		}
		else if(diff>=-10&&diff<=-6){
			ret[0] = 1;
			ret[1] = -1;
		}
		else if(diff>=-5&&diff<=5){
			ret[0] = 2;
			ret[1] = -2;
		}
		else if(diff>=6&&diff<=10){
			ret[0] = 4;
			ret[1] = -4;
		}
		else {
			ret[0] = 6;
			ret[1] = -6;
		}
		return ret;
	}
	
	/**
	 * 取得一个军衔ID对应的军衔名称。
	 * @param rank
	 * @return
	 */
	public static String getCreditString(int rank) {
	    Rank obj = (Rank)Server.server.getServiceRegistry().getDataService().data.findDictObject(Rank.class, rank);
	    if (obj == null) {
	        return "";
	    } else {
	        return obj.title;
	    }
	}
	
	/**
	 * 取得可以获得排名资格的最低战功值。这个值是第一个有效军衔的最低战功要求。
	 */
	public static int getStartCredit(ProjectData prj) {
	    Rank obj = (Rank)prj.findDictObject(Rank.class, 1);
        return obj.minHonor;
	}
	
	/**
	 * 计算一个玩家能够获得的最高军衔。
	 * @param credit 当前战功
	 * @param seq 战功排名，0表示第一
	 * @param total 总共参与排名人数（只有达到最低战功值的才能参与排名）
	 * @return 对应的军衔，如果任何军衔都达不到，则返回缺省军衔。
	 */
	public static Rank getRank(ProjectData prj, int credit, int seq, int total) {
	    List<DataObject> ranks = prj.getDictDataListByType(Rank.class);
	    float per = (seq * 100) / (float)total;
	    for (int i = ranks.size() - 1; i > 0; i--) {
	        Rank rank = (Rank)ranks.get(i);
	        if (credit < rank.minHonor) {
	            continue;
	        }
	        if (rank.maxSeq >= 0 && seq > rank.maxSeq) {
	            continue;
	        }
	        if (rank.maxPercent >= 0.0f && per > rank.maxPercent) {
	            continue;
	        }
	        return rank;
	    }
	    return (Rank)ranks.get(0);
	}
}
