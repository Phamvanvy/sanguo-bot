package peony.game.asyncbattle;

import peony.game.BaseDieCallback;
import peony.game.CreditUtil;
import peony.game.GameObject;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.Server;
import peony.game.Unit;
import peony.game.VMapException;
import peony.game.attendant.Attendant;
import peony.service.account.Account;

public class AsyncBattleDieCallBack extends BaseDieCallback {

	protected int[] getPvpCreditChanged(Player player, int maxWinLevel) {
		return CreditUtil.getCredit(maxWinLevel, player.level);
	}

	public void die(Player player, Unit source) {
		AsyncBattleService battleService = Server.server.getServiceRegistry().getAsyncBattleService();
		if(player!=null && player.type==GameObject.TYPE_PLAYER){
			Player s = null;
			if(source.type==GameObject.TYPE_ATTENDANT){
				s = ((Attendant)source).owner;
			}else{
				s = (Player)source;
			}
			if(s.battleType==Player.TYPE_ASYNC_PLAYER){
				battleService.updateBoard(s, player,true);//更新排名

				sendPrivateMessage(player, false);
				//				player.message(-1, "本次挑战你输了，下次再接再厉", -1, -1);
				player.buffs.clearAllBuffs();
				player.relive(player.maxhp, player.maxmp);
				player.buffs.restoreTempBuffs();
				try {
					if(player.attendant!=null && player.attendant.hp==0)
						player.attendant.setHp(player.attendant.maxhp, false);
					player.goMap(player.asyncEnterMapId, player.asyncEnterX, player.asyncEnterY);
				} catch (VMapException e) {
					e.printStackTrace();
				}
				s.removeFromMap();
				if(s.attendant!=null)
					s.attendant.removeFromWorld();
				ObjectAccessor.asyncPlayers.remove(AsyncPlayer.getSearchKey(s.id, s.asyncMapInstanceId));
				battleService.clearPlayerState(player);
			}else if(player.battleType==Player.TYPE_ASYNC_PLAYER){
				battleService.updateBoard(s, player,false);//更新排名
//				s.message(-1, "恭喜您在擂台战中获得了胜利。", -1, -1);
				sendPrivateMessage(s, true);
				s.buffs.clearAllBuffs();
				s.relive(s.maxhp, s.maxmp);
				s.buffs.restoreTempBuffs();
				try {
					if(s.attendant!=null && s.attendant.hp==0)
						s.attendant.setHp(s.attendant.maxhp, false);
					s.goMap(s.asyncEnterMapId, s.asyncEnterX, s.asyncEnterY);
				} catch (VMapException e) {
					e.printStackTrace();
				}
				if(player.attendant!=null){
					player.attendant.removeFromWorld();
				}
				player.removeFromMap();
				ObjectAccessor.asyncPlayers.remove(AsyncPlayer.getSearchKey(player.id, player.asyncMapInstanceId));
				battleService.clearPlayerState(s);
			}
			Server.server.getServiceRegistry().getAsyncBattleService().removeMap(player.asyncMapInstanceId);
		}
	}
	public void sendPrivateMessage(Player p,boolean isWin){
		Account account = p.getAccount();
		if(account!=null){
			String mod = null;
			if(account.getUiModel()!=null)
				mod = account.getUiModel().trim();
			if(mod!=null){
				if(mod.equals("AndroidNew") || mod.equals("AndroidLargeNew") || mod.equals("iOSNewUI") || mod.equals("iOSNewUILarge")){
				}else if(mod.equals("NewUI_AndroidLarge") || mod.equals("NewUI_Android") || mod.equals("NewUI_iOS") || mod.equals("NewUI_iOSLarge")){
				}else{
					if(!isWin){
						Server.server.getServiceRegistry().getChatService().sendPrivateMessage(p.id, "在刚刚的擂台战中您不幸落败了，获得5点积分");
					}else{
						Server.server.getServiceRegistry().getChatService().sendPrivateMessage(p.id, "恭喜您在擂台战中获得了胜利，获得10点积分");
					}
				}
			}else{
			}
		}
	}
}
