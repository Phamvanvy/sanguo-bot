package peony.service.exam;

import java.util.ArrayList;
import java.util.List;

public class Exam {
	public int id;
	public String title;
	public List<Answer> answers = new ArrayList<Answer>();
	public int answerIndex;
	
	public boolean pass(int answer){
		return answerIndex == answer;
	}
	
}

class Answer{
	public int index;
	public String desc;
	
	public Answer(int index, String desc) {
		this.index = index;
		this.desc = desc;
	}
}
