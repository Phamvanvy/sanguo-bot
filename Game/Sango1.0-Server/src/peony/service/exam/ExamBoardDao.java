package peony.service.exam;

import java.util.List;
import com.pip.db.hibernateDAO.GenericHibernateDAO;

public class ExamBoardDao extends GenericHibernateDAO<ExamBoard, Integer> {

	public List<ExamBoard> getAllBoards(){
		return list("from ExamBoard");
	}
	
}
