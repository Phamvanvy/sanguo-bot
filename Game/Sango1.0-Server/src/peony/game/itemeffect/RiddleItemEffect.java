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
	
	protected int volume;
	
	public RiddleItemEffect(int volume) {
		this.volume = volume;
	}

	public boolean isAsync() {
		return false;
	}

	public void use(Unit source, GameItem item, Unit target, PlayerTransaction tx)
			throws UseItemException {
		if(!ItemUtil.checkUseTarget(source, item, target))
			throw new UseItemException(peony.Messages.STRING_00014);
		if(!(target instanceof Player))
			throw new UseItemException(peony.Messages.STRING_00014);
		Player p = (Player)source;
		String args = "";
		QuestionService questionService = Server.server.getServiceRegistry().getQuestionService();
		int questionId = questionService.getRandomQuestion(this.volume);
		String questionDesc = questionService.getQuestionDescription(questionId, this.volume);
		if(questionDesc==null)
			throw new UseItemException(peony.Messages.STRING_01012);
		args = "answer\t"+questionId+"\t"+questionDesc;
		questionService.questionMap.put(p.id, questionId);
		questionService.questionMap1.put(p.id, this.volume);
		Packet pt = new Packet(OpCode.OPENUI_SERVER);
		pt.putString(SCRIPT);
		pt.putString(args);
		p.send(pt);
	}
	
	public boolean needRemove() {
		return false;
	}

}
