package com.pip.itimes.server.connect;

import java.util.*;

import com.pip.itimes.server.connect.chat.*;
import com.pip.itimes.server.stage.*;
import org.apache.log4j.Logger;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class ChatService{

    private Logger log = Logger.getLogger(ChatService.class);

    private ChatChannel worldChannel = new ChatChannel(0,"世界");
    private Map mapChannels = new TreeMap();
    private ChatChannel systemChannel = new ChatChannel(0,"系统");
    private Map favoriteChannels = new TreeMap();

    private StageService stageService = null;
    private Map players = new HashMap();



    public ChatService() {
        this.stageService = stageService;
    }


    public void setStageService(StageService stageService){
        this.stageService = stageService;
    }


    public void init() {
        Stage[] stages = stageService.getStages();
        for (int i = 0; i < stages.length; i++) {
            ChatChannel chatChannel = new ChatChannel(stages[i].getId(),
                    stages[i].getName());
            Scene[] scenes = stages[i].getScenes();
            for(int j=0;j<scenes.length;j++){
                int id = (stages[i].getId()<<4)|scenes[j].getId();
                chatChannel = new ChatChannel(id,scenes[j].getName());
                mapChannels.put(new Integer(chatChannel.getId()),chatChannel);
            }
        }
        ChatFavorite[] favorites = ChatFavorites.getChatFavorites();
        for(int i=0;i<favorites.length;i++){
            ChatChannel channel = new ChatChannel(favorites[i].id,favorites[i].name);
            favoriteChannels.put(new Integer(channel.getId()),channel);
        }
    }



    public ISendMessage getSendMessage(int srcId, String srcName, int cn,int value,
                                       String msg) {
        if (cn >= 0) { //游戏人物
                ISendMessage ret = new PrivateSendMessage(srcId,
                        srcName,
                        cn, msg);
                return ret;
        } else { //特殊频道
            if(cn==ISendMessage.GUILD){
                ISendMessage ret = new SendMessage(srcId,srcName,new int[]{value},msg);
                return ret;
            }
            else if(cn==ISendMessage.TEAM){
                ISendMessage ret = new SendMessage(srcId,srcName,new int[]{value},msg);
                return ret;
            }
            else{
                IChatChannel channel = getChatChannel(cn, value);
                if (channel != null) {
                    ISendMessage ret = new SendMessage(srcId,
                                                       srcName, channel.getPlayers(), msg);
                    return ret;
                }
            }
        }
        return null;
    }

    private IChatChannel getChatChannel(int cn,int value){
        if(cn==-1){ //世界
            return worldChannel;
        }
        else if(cn==-2){ //地图
            return getMapChannel(value);
        }

        else if(cn==-6){ //圈
            return getFavoriteChannel(value);
        }
        return null;
    }

    private IChatChannel getMapChannel(int id){
        return (IChatChannel)mapChannels.get(new Integer(id));
    }

    private IChatChannel getFavoriteChannel(int id){
        return (IChatChannel)favoriteChannels.get(new Integer(id));
    }

    private PlayerChatInfo getChatPlayer(int id){
        return (PlayerChatInfo)players.get(new Integer(id));
    }



    public void registry(int playerId,short mapId,int favoriteId,boolean inWorldChannel,boolean inMapChannel,boolean inFavoriteChannel) {
        PlayerChatInfo chatPlayer = (PlayerChatInfo)players.get(new Integer(playerId));
        if(chatPlayer!=null){
            return;
        }
        chatPlayer = new PlayerChatInfo(playerId,mapId,favoriteId);
        chatPlayer.setInMapChannel(inMapChannel);
        chatPlayer.setInFavoriteChannel(inFavoriteChannel);
        chatPlayer.setInWorldChannel(inWorldChannel);
        players.put(new Integer(playerId),chatPlayer);
        registry(chatPlayer,systemChannel);
        if(inWorldChannel){
            registry(chatPlayer,worldChannel);
        }
        if(inMapChannel){
            IChatChannel mapChannel = getMapChannel(mapId);
            if(mapChannel!=null){
                registry(chatPlayer,mapChannel);
            }
        }
        if(inFavoriteChannel){
            IChatChannel favoriteChannel = getFavoriteChannel(favoriteId);
            if(favoriteChannel!=null){
                registry(chatPlayer,favoriteChannel);
            }
        }
    }

    private void registry(PlayerChatInfo player,IChatChannel channel){
        channel.registry(player.getId());
        player.addChannel(channel);
    }

    private void unRegistry(PlayerChatInfo player,IChatChannel channel){
        channel.unRegistry(player.getId());
        player.removeChannel(channel);
    }

    public void unRegistry(int playerId) {
        PlayerChatInfo chatPlayer = (PlayerChatInfo)players.remove(new Integer(playerId));
//        if(chatPlayer==null){
//            log.debug("player:"+player.getPlayerName()+" not in chatservice[unregistry]");
//            return;
//        }
        List l = new ArrayList(chatPlayer.getChannels());
        Iterator ite = l.iterator();
        while(ite.hasNext()){
            ChatChannel chatChannel = (ChatChannel)ite.next();
            unRegistry(chatPlayer,chatChannel);
        }
    }

    public void positionChanged(int playerId,short oldMapId,short newMapId) {
        PlayerChatInfo chatPlayer = (PlayerChatInfo) players.get(new Integer(
                playerId));
        if (oldMapId!=newMapId && chatPlayer.inMapChannle()) {
            IChatChannel channel = getMapChannel(oldMapId);
            if (channel != null) {
                unRegistry(chatPlayer, channel);
            }
            channel = getMapChannel(newMapId);
            if (channel != null) {
                registry(chatPlayer, channel);
            }
        }
    }

    public void changeChatFavorite(int playerId, int oldFavoriteId,int favoriteId) {
        PlayerChatInfo chatPlayer = (PlayerChatInfo) players.get(new
                Integer(playerId));
        if (chatPlayer != null && chatPlayer.inFavoriteChannel()) {
            if (oldFavoriteId != favoriteId){
                IChatChannel channel = getFavoriteChannel(oldFavoriteId);
                if (channel != null)
                    unRegistry(chatPlayer, channel);
                channel = getFavoriteChannel(favoriteId);
                if (channel != null)
                    registry(chatPlayer, channel);
            }
        }
    }

    public void setOptions(int playerId,ChatOption[] options, ChatOption[] oldOptions) {
        PlayerChatInfo chatPlayer = (PlayerChatInfo) players.get(new Integer(playerId));
        if (chatPlayer != null) {
            if (oldOptions[ChatOption.WORLD].pri !=
                options[ChatOption.WORLD].pri) {
                if(options[ChatOption.WORLD].pri==0){
                    unRegistry(chatPlayer,worldChannel);
                }else{
                    if(options[ChatOption.WORLD].pri!=0&&oldOptions[ChatOption.WORLD].pri==0)
                        registry(chatPlayer,worldChannel);
                }
            }
            if (oldOptions[ChatOption.MAP].pri !=
                options[ChatOption.MAP].pri) {
                IChatChannel channel = getMapChannel(chatPlayer.getMapId());
                if(channel!=null){
                    if(options[ChatOption.MAP].pri==0){
                        unRegistry(chatPlayer,channel);
                    }else{
                        if(options[ChatOption.MAP].pri!=0&&oldOptions[ChatOption.MAP].pri==0)
                            registry(chatPlayer,channel);
                    }
                }
            }
            if (oldOptions[ChatOption.FAVORITE].pri !=
                options[ChatOption.FAVORITE].pri) {
                IChatChannel channel = getFavoriteChannel(chatPlayer.getFavoriteId());
                if(channel!=null){
                    if(options[ChatOption.FAVORITE].pri==0){
                        unRegistry(chatPlayer,channel);
                    }else{
                        if(options[ChatOption.FAVORITE].pri!=0&&oldOptions[ChatOption.FAVORITE].pri==0)
                            registry(chatPlayer,channel);
                    }
                }
            }
        }
    }
}

class PlayerChatInfo{

    private int id;
    private short mapId;
    private int favoriteId;
    private boolean inMapChannel;
    private boolean inFavoriteChannel;
    private boolean inWorldChannel;
    private Set channels = new HashSet();

    public PlayerChatInfo(int id,short mapId,int favoriteId){
        this.mapId = mapId;
        this.favoriteId = favoriteId;
        this.id = id;
    }

    public void setInMapChannel(boolean value){
        inMapChannel = value;
    }

    public boolean inMapChannle(){
        return inMapChannel;
    }

    public void setInFavoriteChannel(boolean value){
        inFavoriteChannel = value;
    }

    public boolean inFavoriteChannel(){
        return inFavoriteChannel;
    }

    public void setInWorldChannel(boolean value){
        inWorldChannel = value;
    }

    public boolean inWorldChannel(){
        return inWorldChannel;
    }

    public int getId(){
        return id;
    }

    public void addChannel(IChatChannel channel){
        channels.add(channel);
    }

    public void removeChannel(IChatChannel channel){
        channels.remove(channel);
    }

    public Set getChannels(){
        return channels;
    }

    public short getMapId(){
        return mapId;
    }

    public int getFavoriteId(){
        return favoriteId;
    }

}
