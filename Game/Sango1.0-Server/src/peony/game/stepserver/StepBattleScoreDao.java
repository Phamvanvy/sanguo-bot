package peony.game.stepserver;

import java.util.List;
import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class StepBattleScoreDao extends GenericHibernateDAO<StepBattleScore, Integer> {
	
	/**取完胜场数最多并且用时最少的前16名*/
	@SuppressWarnings("unchecked")
	public List<StepBattleScore> getTop16Players(){
		return limitList("from StepBattleScore order by wincount desc,time asc", 0, 16);
	}
	/**根据ID查询胜场数*/
	public StepBattleScore getPlayerStepBattleScoreInfo(int playerid, int accountId){
		return (StepBattleScore)uniqueResult("from StepBattleScore where playerid=? and accountid=?",playerid, accountId);
	}
	
	public void deleteAllScore(){
		delete("delete  from StepBattleScore  where id > ?",0);
	}
}
