package peony.marriage;

import java.text.MessageFormat;

import org.apache.log4j.Logger;

import peony.common.ClientSessionAsyncCall;
import peony.game.ChatOption;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.game.chat.ChatMessage;
import peony.game.chat.ChatService;
import peony.net.ClientSession;
import peony.net.Packet;

public class MarriageCall extends ClientSessionAsyncCall {

	protected final Logger log = Logger.getLogger(MarriageCall.class);
	private Player player;
	protected int personId;
	protected int playerId;
	protected int annotherPersonId;
	public MarriageCall(ClientSession session, int personId, int annotherPersonId) {
		super(session);
		this.player = ObjectAccessor.getPlayer(personId);
		this.personId = personId;
		this.annotherPersonId = annotherPersonId;
		this.playerId = personId;
	}

	public void callFinish() throws Exception {
		Player person = ObjectAccessor.getPlayer(personId);
		Player annotherPerson = ObjectAccessor.getPlayer(annotherPersonId);
		if(success){
			ChatService chatService = Server.server.getServiceRegistry().getChatService();
			Packet p1 = new Packet(OpCode.MARRIAGE_SERVER);
			p1.putString(MessageFormat.format("恭喜您和{0}结为夫妻", annotherPerson.name));
			person.send(p1);
			Packet p2 = new Packet(OpCode.MARRIAGE_SERVER);
			p2.putString(MessageFormat.format("恭喜您和{0}结为夫妻", person.name));
			annotherPerson.send(p2);
			chatService.addChatMessage(new ChatMessage(ChatOption.FACTION,-1,person.faction,"<cFF0000>[系统]</c>\n<cFF0000>[hệ thống]</c>",person.faction,MessageFormat.format("{0}和{1}已结婚，大家一起祝福他们.", person.name,annotherPerson.name),null));
		}else{
			Packet p1 = new Packet(OpCode.MARRIAGE_SERVER);
			p1.putString(errorMessage);
			person.send(p1);
			annotherPerson.send(p1);
		}
	}
																													
	public void run() {
		MarriageService marriageService = Server.server.getServiceRegistry().getMarriageService();
		try {
			if(player.sex == 0){
				marriageService.createMarriage(personId, annotherPersonId, personId);
			}else{
				marriageService.createMarriage(annotherPersonId, personId, personId);
			}
		} catch (MarriageException e) {
			log.error(e, e);
			error(e, e.getMessage());
		}
		addToClientSession();
	}

}
