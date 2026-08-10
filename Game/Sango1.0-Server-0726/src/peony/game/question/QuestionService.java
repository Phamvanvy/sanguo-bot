package peony.game.question;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.dom4j.Document;
import org.dom4j.Element;
import peony.game.CommonUtil;
import peony.game.GameItem;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.mail.MailService;
import peony.net.Packet;
import peony.service.Service;

public class QuestionService implements Service {

	/**<questions>
	 * 	<question id='0' desc='问题描述' answer='答案'/>
	 * 	<question id='1' desc='问题描述' answer='答案'/>
	 * 	<question id='2' desc='问题描述' answer='答案'/>
	 * </questions>
	 */
	protected List<Question> questions = new ArrayList<Question>(); // 题集
	
	public Map<Integer, Integer> questionMap = new HashMap<Integer, Integer>();//记录每次答题请求的questonId
	
	protected static int giftItemId = 1685; // 奖励物品ID

	public void startup() throws Exception {
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data
				.findFile("questions.xml");
		Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
		parse(doc);
	}
	
	/**
	 * 随机抽题
	 * @return
	 */
	 public int getRandomQuestion(){
		 if(questions.size()==0)
			 throw new IllegalArgumentException();
		 return new Random().nextInt(questions.size());
	 }

	/**
	 * 答题
	 * @param questionId 题目ID
	 * @param answer 答案
	 * @throws QuestionException
	 */
	public void anwser(Player p, int questionId, String answer) throws QuestionException{
		if(p!=null){
			if(questionMap.get(p.id)==null || questionMap.get(p.id)!=questionId)
				throw new QuestionException("答题请求不合法");
			Question question = questions.get(questionId);
			if(question==null)
				throw new QuestionException("没有找到此题目");
			String a = question.getAnswer();
			if(!a.equals(answer))
				throw new QuestionException("抱歉，答题错误");
			//答题成功之后进行奖励
			MailService mailService = Server.server.getServiceRegistry().getMailService();
			GameItem item = ObjectAccessor.createGameItem(giftItemId);
			mailService.sendSystemMailAsync(p.id, "系统", "答题成功奖励", "", 0, item, 1, "QSA");
			Packet pt = new Packet(OpCode.MAIL_NEW_SERVER);
			p.send(pt);
		}
	}
	
	/**
	 * 获取题目描述
	 * @param questionId 题目ID
	 * @return
	 */
	public String getQuestionDescription(int questionId){
		Question question = questions.get(questionId);
		if(question==null)
			return null;
		return question.getDescription();
	}
	
	/**
	 * 初始化答题系统的数据
	 * @param doc
	 */
	@SuppressWarnings("unchecked")
	protected void parse(Document doc) {
		Element root = doc.getRootElement();
		List list = root.elements("question");
		if(list.size()==0)
			throw new IllegalArgumentException();
		for(int i=0;i<list.size();i++){
			Element ques = (Element) list.get(i);
			int questionId = Integer.parseInt(ques.attributeValue("id"));
			String questionDescription = ques.attributeValue("desc");
			String answer = ques.attributeValue("answer");
			Question question = new Question(questionId,questionDescription, answer);
			questions.add(questionId, question);
		}
	}

	public void shutdown() {
		
	}

}
