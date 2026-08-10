package peony.game.asyncbattle;

import java.util.List;

import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class AsyncNormalBoardDao extends GenericHibernateDAO<AsyncNormalBoard, Integer> {
	
	public AsyncNormalBoard getAsyncNormalBoardById(int playerId){
		return (AsyncNormalBoard)uniqueResult("from AsyncNormalBoard where playerid=?",playerId);
	}
	
	public List<AsyncNormalBoard> getAsyncNormalBoardList(){
		return list("from AsyncNormalBoard where id > 0 order by uprank desc");
	}
}
