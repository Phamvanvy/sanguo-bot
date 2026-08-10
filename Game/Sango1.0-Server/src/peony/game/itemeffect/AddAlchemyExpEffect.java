package peony.game.itemeffect;

import java.text.MessageFormat;

import peony.alchemy.AlchemyCall;
import peony.alchemy.AlchemyService;
import peony.game.GameItem;
import peony.game.ItemEffect;
import peony.game.Player;
import peony.game.PlayerTransaction;
import peony.game.Server;
import peony.game.Unit;
import peony.game.UseItemException;
import peony.game.chat.ChatService;

public class AddAlchemyExpEffect implements ItemEffect {
	protected int exp;
	public AddAlchemyExpEffect(int exp){
		this.exp=exp;
	}
	public void use(Unit source, GameItem item, Unit target,
			PlayerTransaction tx) throws UseItemException {
		Player player = (Player)source;
		if(player!=null){
			if(this.exp>0){
				if(player.alchemy!=null&&player.alchemy.practiceLevel==4&&player.alchemy.pulseIndex==4&&player.alchemy.acupointNum==8&&player.alchemy.acupointLevel==10){
					throw new UseItemException("您的修炼已满级，敬请期待更高级别");
				}
				if(player.alchemy.restExp+exp>160000){
					throw new UseItemException("留存经验已达上限，请突破重天再使用！");
				}
				if(player.level<60){
					throw new UseItemException(MessageFormat.format("您需要到达60级才能使用{0}，还请您继续努力升级！", item.template.name));
				}
				for(int i=0;i<exp/AlchemyService.ALCHEMY_EXP_ONCE;i++){
					AlchemyCall call=new AlchemyCall(player.session,null,AlchemyCall.ALCHEMY_EXP);
					Server.server.getServiceRegistry().getDbService().schedule(call);
				}
				ChatService service=Server.server.getServiceRegistry().getChatService();
				service.sendPrivateMessage(player.id, MessageFormat.format("成功使用{0}为您增加了{1}点修炼经验，请尽快进入经脉修炼查看。", item.template.name,exp));
			}
		}
	}
	
	/***
	 * 批量使用
	 * @param source
	 * @param item
	 * @param count
	 * @param tx
	 */
	public void useItems(Unit source,GameItem item,int count,PlayerTransaction tx) throws Exception{
		Player player = (Player)source;
		if(player == null){
			return;
		}
		if(this.exp>0){
			int allExp=exp*count;
			if(player.alchemy!=null&&player.alchemy.practiceLevel==4&&player.alchemy.pulseIndex==4&&player.alchemy.acupointNum==8&&player.alchemy.acupointLevel==10){
				throw new Exception("您的修炼已满级，敬请期待更高级别");
			}
			if(player.alchemy.restExp+allExp>160000){
				throw new Exception("留存经验已达上限，请突破重天再使用！");
			}
			if(player.level<60){
				throw new Exception(MessageFormat.format("您需要到达60级才能使用{0}，还请您继续努力升级！", item.template.name));
			}
			for(int i=0;i<allExp/AlchemyService.ALCHEMY_EXP_ONCE;i++){
				AlchemyCall call=new AlchemyCall(player.session,null,AlchemyCall.ALCHEMY_EXP);
				Server.server.getServiceRegistry().getDbService().schedule(call);
			}
			ChatService service=Server.server.getServiceRegistry().getChatService();
			service.sendPrivateMessage(player.id, MessageFormat.format("成功使用{0}为您增加了{1}点修炼经验，请尽快进入经脉修炼查看。", item.template.name,allExp));
		}
	}

	public boolean isAsync() {
		return false;
	}

	public boolean needRemove() {
		return false;
	}
}
