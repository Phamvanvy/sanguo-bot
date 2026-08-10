package peony.game.itemeffect;

import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.ItemUtil;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Unit;
import peony.game.UseItemException;
import peony.game.question.QuestionService;
import peony.net.Packet;

public class RiddleItemEffect implements ItemEffect {

	private static final String SCRIPT = "ui_show_input";
	
	public boolean isAsync() {
		return false;
	}

	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx)
			throws UseItemException {
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException("错误的目标");
		if(!(target instanceof Player))
			throw new UseItemException("错误的目标");
		Player p = (Player)source;
		String args = "";
		QuestionService questionService = Server.server.getServiceRegistry().getQuestionService();
		int questionId = questionService.getRandomQuestion();
		String questionDesc = questionService.getQuestionDescription(questionId);
		if(questionDesc==null)
			throw new UseItemException("没有找到问题");
		args = "answer\t"+questionId+"\t"+questionDesc;
		questionService.questionMap.put(p.id, questionId);
		Packet pt = new Packet(OpCode.OPENUI_SERVER);
		pt.putString(SCRIPT);
		pt.putString(args);
		p.send(pt);
	}

}
