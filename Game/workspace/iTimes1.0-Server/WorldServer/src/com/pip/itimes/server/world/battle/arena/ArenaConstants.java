package com.pip.itimes.server.world.battle.arena;

public interface ArenaConstants{
    public static final byte CONN_ARENA_ERROR = (byte)-1;
    
    public static final byte CONN_ARENA_WORLD_LOGIN = (byte)1;
    public static final byte CONN_ARENA_WORLD_LOGIN_OK = (byte)2;
    public static final byte CONN_ARENA_WORLD_LOGIN_FAIL = (byte)3;
    
    public static final byte CONN_ARENA_GET_TIME = (byte)10;
    public static final byte CONN_ARENA_NOTIFY_TIME = (byte)11;
    
    public static final byte CONN_ARENA_QUEUE = (byte)20;
    public static final byte CONN_ARENA_QUEUE_OK = (byte)21;
    public static final byte CONN_ARENA_QUEUE_FAIL = (byte)22;
    public static final byte CONN_ARENA_CANCEL_QUEUE = (byte)23;
    public static final byte CONN_ARENA_CANCEL_QUEUE_OK = (byte)24;
    public static final byte CONN_ARENA_CANCEL_QUEUE_FAIL = (byte)25;
    
    public static final byte CONN_ARENA_REMOVE_QUEUE = (byte)30;
    public static final byte CONN_ARENA_REMOVE_QUEUE_TIMEOUNT = (byte)31;

    public static final byte CONN_ARENA_SYNC_PLAYER = (byte)40;
    public static final byte CONN_ARENA_SYNC_PLAYER_DATA = (byte)41;
    public static final byte CONN_ARENA_SYNC_PLAYER_FAIL = (byte)42;
    public static final byte CONN_ARENA_SYNC_PLAYER_CATCH_TO_BATTLE = (byte)43;
    
    public static final byte CONN_ARENA_BATTLE_START = (byte)50;
    public static final byte CONN_ARENA_BATTLE_ABORT = (byte)51;
    public static final byte CONN_ARENA_ROUND_END = (byte)52;
    public static final byte CONN_ARENA_BATTLE_FIGHT = (byte)53;
    
    public static final byte CONN_ARENA_REMOVE_BATTLE = (byte)60;
    
    public static final byte CONN_ARENA_TOP10 = (byte)70;
    public static final byte CONN_ARENA_TOP10_OK = (byte)71;
    public static final byte CONN_ARENA_TOP10_WORLDWAR = (byte)72;
    public static final byte CONN_ARENA_TOP10_WORLDWAR_OK = (byte)73;
    
    public static final byte CONN_ARENA_LYRIC_ADD = (byte)80;	//添加要播放的歌曲
    public static final byte CONN_ARENA_SERVERLIST = (byte)81;	//获取连接的服务器列表
    public static final byte CONN_ARENA_LYRIC_SEND = (byte)82;	//发送歌词内容
    
    public static final int ARENA_TYPE_ONE = 1;
    public static final int ARENA_TYPE_TWO = 2;
    public static final int ARENA_TYPE_THREE = 3;
    
    public static final int ARENA_QUEUE_ERROR = -1;
    public static final int ARENA_QUEUE_SUCCESSFUL = 1;
    public static final int ARENA_QUEUE_DUPLICATE = 2;
    public static final int ARENA_QUEUE_OTHER = 3;
    public static final int ARENA_QUEUE_DUPLICATE_OTHER = 4;
    public static final int ARENA_QUEUE_DUPLICATE_PLAYER = 5;
    
    public static final int ARENA_CANCEL_QUEUE_ERROR = -1;
    public static final int ARENA_CANCEL_QUEUE_SUCCESSFUL = 1;
    public static final int ARENA_CANCEL_QUEUE_BATTLE = 2;
    public static final int ARENA_CANCEL_QUEUE_OTHER = 3;
    public static final int ARENA_CANCEL_QUEUE_BATTLE_OTHER = 4;
    
    public static final int MAX_IDLE = 3;
    
    public static final int ARENA_LYRIC_ADD_ERROR  = -1;
    public static final int ARENA_LYRIC_ADD_SUCCESSFUL  = 1;
    
}
