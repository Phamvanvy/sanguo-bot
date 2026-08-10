package peony.game.question;

public class Question {
	
	public int id;
	public String description; //题目描述
	public String answer; //答案
	
	public Question(int id, String description, String answer) {
		super();
		this.answer = answer;
		this.description = description;
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getAnswer() {
		return answer;
	}
	public void setAnswer(String answer) {
		this.answer = answer;
	}
}
