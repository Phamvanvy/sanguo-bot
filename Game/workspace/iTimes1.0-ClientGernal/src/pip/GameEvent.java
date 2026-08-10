package pip;


public class GameEvent{
    public static final short EVENT_GOTO_MAP = 0;
    public static final short EVENT_CHAT = 1;
    public static final short EVENT_MESSAGE = 2;
    public static final short EVENT_QUESTION = 3;
    public static final short EVENT_SHOWGETITEM = 4;
    public static final short EVENT_BATTLE = 5;
    public static final short EVENT_NPC_CONFIRM = 6;
    public static final short EVENT_BBS = 7;
    public static final short EVENT_PRODUCT_TEACHER = 8;
    public static final short EVENT_BATTLE_TEACHER = 9;
    public static final short EVENT_VIEW_BATTLE_SKILL = 10;
    public static final short EVENT_PRODUCE_ITEM = 11;
    public static final short EVENT_RESOURCE_CONFIRM = 12;
    public static final short EVENT_GOTO_MAP_LOCAL = 13;
    public static final short EVENT_RESOURCE_GATHER = 14;
    public static final short EVENT_RESOURCE_GAME = 15;
    public static final short EVENT_CHAT_CIRCLE_OPTION = 16;
    public static final short EVENT_TASK_VIEW = 17;
    public static final short EVENT_PLAYER_VIEW = 18;
    public static final short EVENT_FRIEND_VIEW = 19;
    public static final short EVENT_CHANGEATTRIBUTE_CONFIRM = 20;
    public static final short EVENT_CHANGEEQUIP_CONFIRM = 21;
    public static final short EVENT_PK_REQUEST = 22;
    public static final short EVENT_TEAM_INVITE = 23;
    public static final short EVENT_MAIL_VIEW = 24;
    public static final short EVENT_FORM_INPUT = 25;
    public static final short EVENT_AUCTION = 26;
    public static final short EVENT_SHOP = 27;
    public static final short EVENT_BUY_MATERIAL = 28;
    public static final short EVENT_OEM = 29;
    public static final short EVENT_STORE = 30;
    public static final short EVENT_TASK_CONFIRM = 31;
    public static final short EVENT_PAUSE = 32;
    public static final short EVENT_TONG_LIST = 33;
    public static final short EVENT_CHAT_OPTION = 34;
    public static final short EVENT_PET_OPTION = 35;
    public static final short EVENT_PET_TRADE = 36;
    public static final short EVENT_DIE_CONFIRM = 37;
    public static final short EVENT_SYSTEM_OPTION = 38;
    public static final short EVENT_REPAIR = 39;
    public static final short EVENT_FAQ = 40;
    public static final short EVENT_CALLGM = 41;
    public static final short EVENT_BLACKLIST_VIEW = 42;
    public static final short EVENT_GENERIC_LIST = 43;
    public static final short EVENT_ISHOP_LIST = 44;
    public static final short EVENT_VIEW_EQUIP = 45;

    public static final short NET_CHAT_MESSAGE = 1000;

    public static final byte EVENT_BLOCK_NONE = 0;
    public static final byte EVENT_BLOCK_CYCLE = 1;
    public static final byte EVENT_BLOCK_PLAYER = 2;
    public static final byte EVENT_BLOCK_DOWNLOAD = 3;

    private int _type;
    private byte _state;
    private byte _priority;

    /**
     * 事件阻塞类型
     */
    private byte _blockType = EVENT_BLOCK_NONE;

    /**
     * 事件中发出的请求
     */
    public int serial;
    public boolean isTaskUIEvent = false;

    public int idata[];
    public String sdata[];
    public Object odata[];
    private int[] _collisionBox = null;

    /**
     * 事件参数配置：每个事件1个整数，高16位为对应脚本ID，次8位表示优先级，低8位表示阻塞类型
     */
    private static final int[] EVENT_CONFIG = new int[]{
                    0x0A01, // EVENT_GOTO_MAP
                    0, // EVENT_CHAT                               
                    0, // EVENT_MESSAGE
                    0x0101, // EVENT_QUESTION
                    0, // EVENT_SHOWGETITEM
                    0x0601, // EVENT_BATTLE
                    0x0200, // EVENT_NPC_CONFIRM
                    0x75310101, // EVENT_BBS
                    0x75320101, // EVENT_PRODUCT_TEACHER
                    0x75330101, // EVENT_BATTLE_TEACHER
                    0x75350101, // EVENT_VIEW_BATTLE_SKILL
                    0x75340101, // EVENT_PRODUCE_ITEM
                    0x0200, // EVENT_RESOURCE_CONFIRM
                    0x0A01, // EVENT_GOTO_MAP_LOCAL
                    0x0501, // EVENT_RESOURCE_GATHER
                    0x75360501, // EVENT_RESOURCE_GAME
                    0x75370501, // EVENT_CHAT_CIRCLE_OPTION
                    0x75380101, // EVENT_TASK_VIEW
                    0x75390101, // EVENT_PLAYER_VIEW
                    0x753A0101, // EVENT_FRIEND_VIEW
                    0x0500, // EVENT_CHANGEATTRIBUTE_CONFIRM
                    0x0500, // EVENT_CHANGEEQUIP_CONFIRM
                    0x0501, // EVENT_PK_REQUEST
                    0x0501, // EVENT_TEAM_INVITE
                    0x753B0501, // EVENT_MAIL_VIEW
                    0x0501, // EVENT_FORM_INPUT
                    0x753D0501, // EVENT_AUCTION
                    0x753C0101, // EVENT_SHOP
                    0x753E0101, // EVENT_BUY_MATERIAL
                    0x753F0101, // EVENT_OEM
                    0x75400501, // EVENT_STORE
                    0x0201, // EVENT_TASK_CONFIRM
                    0x0701, // EVENT_PAUSE
                    0x75410101, // EVENT_TONG_LIST
                    0x75420101, // EVENT_CHAT_OPTION
                    0x75430101, // EVENT_PET_OPTION
                    0x75440101, // EVENT_PET_TRADE
                    0x0701, // EVENT_DIE_CONFIRM
                    0x75450101, // EVENT_SYSTEM_OPTION
                    0x75460101,  // EVENT_REPAIR
                    0x75470101, // EVENT_FAQ
                    0x754A0101, // EVENT_TEST
                    0x754B0101, // EVENT_BLACKLIST
                    0x754C0101, //EVENT_GENERIC_LIST
                    0x754D0101, //EVENT_ISHOP_LIST
                    0x75490501, //EVENT_VIEW_EQUIP
    };

    public GameEvent(int type, int i_size, int s_size){
        _type = type;
        setOption();

        if(i_size > 0){
            idata = new int[i_size];
        }

        if(s_size > 0){
            sdata = new String[s_size];
        }
    }

    public byte getState(){
        return _state;
    }

    public void setState(byte state){
        _state = state;
    }

    public int getType(){
        return _type;
    }

    public void setBlockType(byte block){
        _blockType = block;
    }

    private void setOption(){
        if(_type < NET_CHAT_MESSAGE){
            int config = EVENT_CONFIG[_type];
            _priority = (byte)(config >> 8);
            _blockType = (byte)(config & 0x0F);
            isTaskUIEvent = (config >> 16) != 0;
        }
    }

    public int getEventTaskId(){
        return EVENT_CONFIG[_type] >> 16;
    }

    public static void eventRemove(GameEvent e){
        int config = EVENT_CONFIG[e._type];
        config = (config >> 16) & 0xFFFF;

        if(config != 0){
            World._gtvm.removeTask((short)config, true);
            GameState.clearTaskUI();
        }
    }

    public byte getPriority(){
        return _priority;
    }

    public byte getBlockType(){
        return _blockType;
    }

    public int[] getCollisionBox(){
        return _collisionBox;
    }

    public void setCollisionBox(int x, int y, int w, int h){
        _collisionBox = new int[]{
                        x, y, w, h
        };
    }
}