package peony.service.exam;

import java.util.List;
import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class ExamPublishBoardDao extends GenericHibernateDAO<ExamPublishBoard, Integer> {

	public List<ExamPublishBoard> getAllBoards(){
		return list("from ExamPublishBoard");
	}
	
}
