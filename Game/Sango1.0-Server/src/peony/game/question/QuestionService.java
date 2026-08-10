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
	protected List<Question> questions = new ArrayList<Question>(); // 题集0
	protected List<Question> questions1 = new ArrayList<Question>(); // 题集1
	protected List<Question> questions2 = new ArrayList<Question>(); // 题集2
	protected List<Question> questions3 = new ArrayList<Question>(); // 题集3
	protected List<Question> questions4 = new ArrayList<Question>(); // 题集4
	
	public Map<Integer, Integer> questionMap = new HashMap<Integer, Integer>();//记录每次答题请求的questonId
	public Map<Integer, Integer> questionMap1 = new HashMap<Integer, Integer>();//记录每次答题请求的套系
	
	protected static int giftItemId = 1685; // 奖励物品ID
	protected static int giftItemId1 = 4828; // 奖励物品ID
	protected static int giftItemId2 = 4828; // 奖励物品ID
	protected static int giftItemId3 = 4828; // 奖励物品ID
	protected static int giftItemId4 = 4828; // 奖励物品ID
	
	public int volume; //套系

	public void startup() throws Exception {
		byte[] bytes = Server.server.getServiceRegistry().getDataService().data
				.findFile("questions.xml");
		Document doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
		parse(doc, 0);
		
		bytes = Server.server.getServiceRegistry().getDataService().data
				.findFile("questions1.xml");
		doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
		parse(doc, 1);
		
		bytes = Server.server.getServiceRegistry().getDataService().data
				.findFile("questions2.xml");
		doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
		parse(doc, 2);
		
		bytes = Server.server.getServiceRegistry().getDataService().data
				.findFile("questions3.xml");
		doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
		parse(doc, 3);

		bytes = Server.server.getServiceRegistry().getDataService().data
				.findFile("questions4.xml");
		doc = CommonUtil.getDocument(new ByteArrayInputStream(bytes));
		parse(doc, 4);
	}
	
	/**
	 * 随机抽题
	 * @return
	 */
	 public int getRandomQuestion(int volume){
		 List<Question> list = null;
		 if(volume==0)
			 list = questions;
		 else if(volume==1)
			 list = questions1;
		 else if(volume==2)
			 list = questions2;
		 else if(volume==3)
			 list = questions3;
		 else if(volume==4)
			 list = questions4;
		 if(list.size()==0)
			 throw new IllegalArgumentException();
		 return new Random().nextInt(list.size());
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
				throw new QuestionException(peony.Messages.STRING_01191);
			int volume = questionMap1.get(p.id);
			Question question = null;
			int gift = -1;
			if(volume==0){
				question = questions.get(questionId);
				gift = giftItemId;
			}else if(volume==1){
				question = questions1.get(questionId);
				gift = giftItemId1;
			}else if(volume==2){
				question = questions2.get(questionId);
				gift = giftItemId2;
			}else if(volume==3){
				question = questions3.get(questionId);
				gift = giftItemId3;
			}else if(volume==4){
				question = questions4.get(questionId);
				gift = giftItemId4;
			}
			if(question==null)
				throw new QuestionException(peony.Messages.STRING_01192);
			String a = question.getAnswer();
			if(!a.contains(answer))
				throw new QuestionException(peony.Messages.STRING_01193);
			//答题成功之后进行奖励
			MailService mailService = Server.server.getServiceRegistry().getMailService();
			GameItem item = ObjectAccessor.createGameItem(gift);
			mailService.sendSystemMailAsync(p.id, peony.Messages.STRING_00004, peony.Messages.STRING_01194, "", 0, item, 1, "QSA");
			Packet pt = new Packet(OpCode.MAIL_NEW_SERVER);
			p.send(pt);
		}
	}
	
	/**
	 * 获取题目描述
	 * @param questionId 题目ID
	 * @return
	 */
	public String getQuestionDescription(int questionId, int volume){
		Question question = null;
		if(volume==0)
			question = questions.get(questionId);
		else if(volume==1)
			question = questions1.get(questionId);
		else if(volume==2)
			question = questions2.get(questionId);
		else if(volume==3)
			question = questions3.get(questionId);
		else if(volume==4)
			question = questions4.get(questionId);
		if(question==null)
			return null;
		return question.getDescription();
	}
	
	/**
	 * 初始化答题系统的数据
	 * @param doc
	 */
	@SuppressWarnings("unchecked")
	protected void parse(Document doc, int volume) {
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
			if(volume==0)
				questions.add(questionId, question);
			else if(volume==1)
				questions1.add(questionId, question);
			else if(volume==2)
				questions2.add(questionId, question);
			else if(volume==3)
				questions3.add(questionId, question);
			else if(volume==4)
				questions4.add(questionId, question);
		}
	}

	public void shutdown() {
		
	}

}
