package peony.game.stepserver;

import java.util.List;

import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class StepBattleScore_FinalsDao extends GenericHibernateDAO<StepBattleScoreTop16, Integer> {
	
	/**取16强的押注数并按从大到小排列*/
	@SuppressWarnings("unchecked")
	public List<StepBattleScoreTop16> getTop16Players(){
		return limitList("from StepBattleScoreTop16 order by wincount desc", 0, 16);
	}
	/**按争霸赛结果取列表*/
	@SuppressWarnings("unchecked")
	public List<StepBattleScoreTop16> getFinalsPlayersList(){
		return limitList("from StepBattleScoreTop16 order by ranking desc wincount asc",0,16);
	}
	/**根据ID查询胜场数*/
	public StepBattleScoreTop16 getPlayerStepBattleScoreInfo(int playerid, int accountId){
		return (StepBattleScoreTop16)uniqueResult("from StepBattleScoreTop16 where playerid=? and accountid=?",playerid, accountId);
	}
	
	public void deleteAllScore(){
		delete("delete  from StepBattleScoreTop16  where id > ?",0);
	}
	
}
