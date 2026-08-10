package peony.game.chat;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import peony.game.ChatOption;
import peony.game.GameItem;
import peony.game.ItemUtil;
import peony.game.ObjectAccessor;
import peony.game.OpCode;
import peony.game.Player;
import peony.game.Server;
import peony.net.ClientSession;
import peony.net.Packet;
import peony.service.tong.TongMember;
import peony.service.tong.TongService;
import peony.vm.ASMQuest;
import peony.vm.ASMQuestUtil;
import peony.game.nation.Officer;

public class ChatMessage {
	protected int sourceId; //发布信息的人的ID
	protected String sourceName; //发布信息的人名
	protected int faction; //发送的阵营
	protected String message;
	protected ChatAttachment attachment;
	protected int channel;
	public int destId;
	public ClientSession[] sessions; //只有发送队伍聊天的时候有效
	public String destName = ""; //只有在发送nativemessage的时候才有用
	public String maskMessage = ""; //经过处理以后的信息，发送给不同正营的玩家用的
	protected Packet packet;
	protected Packet maskPacket;
	public boolean isKing; //是否国公的聊天，如果是，那么在发送的时候需要把channel的最高位设置成1
	public boolean isOfficer;
	public int vipLevel;	//VIP level
	public int shoutColor;
	public int shoutDuration;
	public int playerLevel;//玩家等级 
	
	protected static final byte[] EMPTY = new byte[0];
	
	public ChatMessage(int channel, int sourceId, int faction,String sourceName,
			String message, ChatAttachment attachment) {
		this(channel,sourceId,faction,sourceName,-1,message,attachment);
	}
	
	public ChatMessage(int channel, int sourceId, int faction,String sourceName,int destId,
			String message, ChatAttachment attachment) {
		this(channel,sourceId,faction,sourceName,destId,message,attachment,null);
	}
	
	public ChatMessage(int channel, int sourceId,int faction, String sourceName,int destId,
			String message, ChatAttachment attachment,ClientSession[] sessions) {
		this.channel = channel;
		this.sourceId = sourceId;
		this.faction = faction;
		this.sourceName = sourceName;
		this.destId = destId;
		this.message = message;
		this.attachment = attachment;
		this.sessions = sessions;
		if(faction!=-1){
			maskMessage = getMaskMessage(message);
		}
		if(this.attachment == null && this.message != null && this.message.indexOf("/-1")!=-1){
			this.message = this.message.replace("/-1", "");
		}
		if (attachment != null && attachment instanceof ItemChatAttachment) {
			ItemChatAttachment att = (ItemChatAttachment) attachment;
			ObjectAccessor.addGameItemToCached(att.item);
		}
	}
	
	public Packet getPacket() {
		if (packet == null) {
			Packet pt = new Packet(OpCode.CHAT_SERVER);
			int ch = channel;
			if(isKing)	ch |= (1<<7);//国公
			if(isOfficer) ch |= (1<<6);//官员
			if(vipLevel > 0 && sourceId != -1) ch |= (1<<5);//是否VIP
			
			pt.put(ch);
			pt.putInt(sourceId);
			if(isOfficer){
				Officer o = Server.server.getServiceRegistry().getNationService().getNationByFaction(faction).getOfficerByPlayerId(sourceId);
				if(o != null){
					pt.put(o.level);
				}
			}else{
				pt.put(-1);
			}
			//VIP等级
			if(vipLevel > 0 && sourceId != -1){
				pt.put(vipLevel);
			}
			
			if(channel == ChatOption.WORLD){
				pt.put(faction);
			} else if(channel == ChatOption.PRIVATE){
				pt.putString(destName);
				pt.putInt(destId);
			}  else if(channel == ChatOption.GUILD){
				TongService tongService = Server.server.getServiceRegistry().getTongService();
				TongMember tongMember = tongService.getPlayerInfo(sourceId);
				if(tongMember!=null){
					String dutyName = TongService.getDutyName(tongMember.duty);
					pt.putString(dutyName);
				}
			}
			pt.putString(sourceName);
			if(sourceName.contains("(成就)")){
				String[] strs = message.split("#");
				if(strs.length>=2){
					message = strs[0];
					pt.putString(strs[1]);//描述
				}
			}
			pt.putString(message);
			pt.putInt(playerLevel);
			pt.put(getBytes());
			packet =  pt;
		}
		return packet;
	}
	
	public Packet getShoutPacket(){
		Packet pt = new Packet(OpCode.SHOUT_SERVER);
		//VIP狮子吼+V
		String realMsg = vipLevel>0?"{#V.pip,0}"+message:message;
		pt.putString(realMsg);
		pt.putInt(shoutColor);
		pt.putInt(shoutDuration);
		return pt;
	}
	
	public Packet getMaskPacket(){
		if (maskPacket == null) {
			Packet pt = new Packet(OpCode.CHAT_SERVER);
			int ch=channel;
			if(vipLevel > 0 && sourceId != -1) ch |= (1<<5);//是否VIP
			pt.put(ch);
			pt.putInt(sourceId);
			if(isOfficer){
				Officer o = Server.server.getServiceRegistry().getNationService().getNationByFaction(faction).getOfficerByPlayerId(sourceId);
				if(o != null){
					pt.put(o.level);
				}
			}else{
				pt.put(-1);
			}
			//VIP等级
			if(vipLevel > 0 && sourceId != -1){
				pt.put(vipLevel);
			}
			if(channel == ChatOption.WORLD){
				pt.put(faction);
			} else if(channel == ChatOption.PRIVATE){
				pt.putString(destName);
				pt.putInt(destId);
			}
			pt.putString(sourceName);
			pt.putString(maskMessage);
			pt.putInt(playerLevel);
			pt.put(new byte[0]);
			maskPacket =  pt;
		}
		return maskPacket;
	}
	
	public String getMessage(){
		return message;
	}
	
	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}
	
	public byte[] getBytes(){
		if(attachment==null)
			return EMPTY;
		else{
			return attachment.toBytes();
		}
	}
	
	protected static Pattern pattern = Pattern.compile("/\\d\\d");
	
	public static String faces="[顶][摸头][抱抱][快乐][潜水][打包带走][荡秋千][扇巴掌][情人节][劝说][蹭蹭][围观][你妹][打伞][扑倒][七夕][吻][扔砖头][女王笑][织毛衣][砍人][训人][喵][苍天啊]";
	
	protected static String getMaskMessage(String message){
		Matcher ma = pattern.matcher(message);
		StringBuilder sb = new StringBuilder();
		boolean m = ma.find();
		if(!m){
			ma=Pattern.compile("\\[[\\D]{1,4}\\]").matcher(message);
			m=ma.find();
			while(m){
				if(faces.indexOf(ma.group())!=-1){
					sb.append(ma.group());
				}
				m = ma.find();
			}
			return sb.toString();
		}else{
			while(m){
				sb.append(ma.group());
				m = ma.find();
			}
		}
		return sb.toString();
	}
	
	public static ChatMessage parse(String message, byte[] bytes,
			Player player, int channel, int destId) {
		if (channel == ChatOption.SYSTEM) {
			throw new IllegalArgumentException();
		}
		if (bytes.length > 0) {
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			DataInputStream dis = new DataInputStream(bais);
			try {
				int type = dis.read();
				if (type == 1) { // 如果是物品
					int itemId = dis.readInt();
					int instanceId = dis.readInt();
					
					// 查找物品，从缓存物品，背包、仓库、装备中查找。
//					GameItem item = ObjectAccessor.getCachedGameItem(itemId, instanceId);
					GameItem item = null;
					if (item == null) {
						Object[] os = ItemUtil.findPlayerEquipment(player, itemId, instanceId);
						if (os != null) {
							item = (GameItem) os[0];
						}else if (player.depot!=null && player.depot.getGrids().size()>0) {
							item = player.depot.getGameItem(-1, itemId, instanceId);
						}
					}
					if (item != null) {
						return new ChatMessage(channel, player.id, player.faction,player.name,
								destId, message, new ItemChatAttachment(item.clone()));
					} else {
						return null;
					}
				} else if (type == 2) { // 如果是任务
					int questId = dis.readInt();
					ASMQuest quest = ASMQuestUtil.getQuest(questId);
					if (quest != null) {
						return new ChatMessage(channel, player.id, player.faction, player.name,
								destId, message, new QuestChatAttachment(quest));
					} else {
						return null;
					}
				} else if (type == 3) { // 如果是成就
					int cato = dis.readByte();
					String title = dis.readUTF();
					return new ChatMessage(channel, player.id, player.faction, player.name, 
							destId, message, new StatChatAttachment(cato, title));
				} else if(type == 4){
					int cato = dis.readByte();
					String title = dis.readUTF();
					int cardLevel=dis.readByte();
					int quality=dis.readByte();
					return new ChatMessage(channel, player.id, player.faction, player.name, 
							destId, message, new CardChatAttachment(cato, title,quality,cardLevel));
				} else {
					return null;
				}
			} catch (IOException ex) {
				return null;
			}
		}
		return new ChatMessage(channel, player.id, player.faction, player.name, destId,
				message, null);
	}
}
