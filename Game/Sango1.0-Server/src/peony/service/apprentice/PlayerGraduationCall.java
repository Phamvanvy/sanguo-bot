package peony.service.apprentice;

import java.text.MessageFormat;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.GameItem;
import peony.game.ObjectAccessor;
import peony.game.Player;
import peony.game.Server;
import peony.game.chat.ChatService;
import peony.game.mail.MailService;
import peony.net.ClientSession;
import peony.service.pluginstance.ChessInstanceService;
import peony.service.stat.StatService;

public class PlayerGraduationCall extends ClientSessionAsyncCall{
	private static Logger log = Logger.getLogger(PlayerGraduationCall.class);

	Player p;
	public PlayerGraduationCall(ClientSession session,Player p) {
		super(session);
		this.p = p;
	}

	public void callFinish() throws Exception {
		
		
	}

	public void run() {
		if (p != null) {
			ApprenticeService service = Server.server.getServiceRegistry().getApprenticeService();
			Player teacher = service.getTeacherByApp(p);
			if (teacher != null) {
				int dex = service.getRewardIndex(p);
				if(dex!=-1){
					for(int i=0;i<=dex;i++){
						
						int rewardIndex = i;
						if(p.pool.getInt(service.getRewardProperty(ApprenticeService.rewardLevel[i]), 0)==0){
							MailService mailService = Server.server.getServiceRegistry()
									.getMailService();
							ChatService chatService = Server.server.getServiceRegistry()
									.getChatService();
							GameItem teaItem = ObjectAccessor.createGameItem(ApprenticeService.teacherRewards[rewardIndex]);
							String appMsg = MessageFormat.format("恭喜你达到了{0}级，师傅为让你再接再厉，特意送了你一本武林秘籍给你，看完之后想必你定能经验大涨。",ApprenticeService.rewardLevel[rewardIndex]);
							String teacherMsg = MessageFormat.format("恭喜你的徒弟达到了{0}级，他为了报答你，特意为你准备了{1}，请笑纳。",ApprenticeService.rewardLevel[rewardIndex],teaItem.template.name);
							if(rewardIndex == ApprenticeService.rewardLevel.length-1){
							    log.info("[PLAYERGRADUATE]ID["+p.id+"]");
							    teacherMsg = MessageFormat.format("恭喜你的徒弟达到了{0}级，他为了报答你，特意为你准备{1}，请笑纳。他也终于出师了！",ApprenticeService.rewardLevel[rewardIndex],teaItem.template.name);
								p.pool.setInt(Player.PROPERTY_GRADUATE_TEACHER, teacher.id); //记录一日为师终生为父的师父
//								chatService.sendPrivateMessage(p.id, peony.Messages.STRING_01045);
								chatService.sendPrivateMessage(p.id, appMsg);
								if(ObjectAccessor.getPlayer(teacher.id)!=null){
									chatService.sendPrivateMessage(teacher.id, teacherMsg);
								}
								mailService.sendSystemMailAsync(p.id, peony.Messages.STRING_00004,peony.Messages.STRING_01046 , peony.Messages.STRING_01048, 0,
										ObjectAccessor.createGameItem(ApprenticeService.atitle), 1,
										"APPRENTICETITLEREWARD");
								int teacherTimes = teacher.pool.getInt(Player.PROPERTY_TEACHER_TIMES, 0);
								teacherTimes++;
								int index = service.getIndex(teacherTimes);
								if (index != -1) {
									mailService.sendSystemMailAsync(teacher.id, peony.Messages.STRING_00004,
											peony.Messages.STRING_01046, MessageFormat.format(peony.Messages.STRING_01047,
													p.name), 0, ObjectAccessor
													.createGameItem(ApprenticeService.teacherReward[index]), 1,
											"TEACHERTITLEREWARD");
								} 
								teacher.pool.setInt(Player.PROPERTY_TEACHER_TIMES,
										teacherTimes);
								try {
									service.saveRemove(p.id, teacher.id);
									service.removeProperty(p);
								} catch (Exception e) {
				
								}
								//统计玩家徒弟出师成就
								StatService serivce = Server.server.getServiceRegistry().getStatService();
								serivce.apprenticeGraduate(teacher);
								log.info("[PLAYERGRADUATE]ID["+p.id+"]TEACHERID["+teacher.id+"]");
							}else{
								chatService.sendPrivateMessage(p.id, appMsg);
								if(ObjectAccessor.getPlayer(teacher.id)!=null){
									chatService.sendPrivateMessage(teacher.id, teacherMsg);
								}
							}
							mailService.sendSystemMailAsync(p.id, peony.Messages.STRING_00004,"师徒奖励" , appMsg, 0,
									ObjectAccessor.createGameItem(ApprenticeService.apprenticeReward[rewardIndex]), 1,
									"APPRENTICETITLEREWARD");
							
							mailService.sendSystemMailAsync(teacher.id, peony.Messages.STRING_00004,"师徒奖励" , teacherMsg, 0,
									teaItem, 1,"APPRENTICETITLEREWARD");
							p.pool.setInt(service.getRewardProperty(ApprenticeService.rewardLevel[i]), 1);
						}
					}
				}
		}
		}
	}

}
