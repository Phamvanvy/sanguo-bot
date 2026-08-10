package com.pip.sanguo.performancetest.client;

public class OperationCode{
    /**
     * 代理->世界，通知IP地址
     * ip       int     IP地址
     */
    public static final short PROXY_SYNC_IP = 30000;
    /**
     * 代理->世界，代理服务器登录
     * ip       int     代理服务器IP地址
     * port     short   代理服务器端口号
     */
    public static final short PROXY_LOGIN = 30001;
    /**
     * 代理<->世界，断开客户端连接通知
     */
    public static final short PROXY_SESSION_DISCONNECT = 30002;
    
    /**
     * 错误
     * serial                   int
     * type                     short
     * message                  string
     */
    public static final short ERROR = -1;
    
    public static final short SYSTEM_NEWSESSION = 10;
    public static final short SYSTEM_CLOSESESSION = 11;
    
    public static final short SYNC_TIME_CLIENT = 101;
    public static final short SYNC_TIME_SERVER = 102;
    
    /**
     * 登陆角色
     * serial                   int
     * 角色Id                 int
     */
    public static final short ACTOR_LOGIN_CLIENT = 103;
    /**
     * 角色登陆成功
     * serial                   int
     * 角色信息                 ACTOR
     */
    public static final short ACTOR_LOGIN_SERVER = 104;
    public static final short MOVE_CLIENT = 105;
    
    /**
     * 类型       byte    1 是玩家，3是Npc
     * id       int
     * name     string
     * level    byte
     * 时间       int
     * x        int
     * y        int
     * 方向       byte
     * 状态       short
     * 动画Id int     如果是人物走动动画id为-1
     * 阵营       byte    
     */
    public static final short MOVE_SERVER = 106;
    public static final short RIDE_CLIENT = 107;
    public static final short UNRIDE_CLIENT = 108;
    public static final short LOGOUT_CLIENT = 113;
    public static final short LOGOUT_SERVER = 114;
    public static final short UNIT_INVISIBLE = 115;
    
    /**
     * 时间           int
     * 出口Id         int
     */
    public static final short TOUCHEXIT_CLIENT = 116;
    
    /**
     * 关卡号 int
     * 所在地图号    byte
     * 角色x坐标    int
     * 角色y坐标    int
     * 
     * 数据           byte[]
     */
    public static final short PKG_SERVER = 117;
    
    
    /**
     * animateName  string  图片名字
     */
    public static final short ANIMATEGET_CLIENT = 118;
    
    /**
     * animateName  string
     * byte[]       data
     */
    public static final short ANIMATEGET_SERVER = 119;
    
    /**
     * npcinstanceIdid                  int
     * questId                          int
     */
    public static final short TOUCHNPC_CLIENT = 120;
    
    
    /**
     * npcId        int
     * message      string
     * notifyId     int
     */
    public static final short NPC_CHAT_SERVER = 121;
    
    /**
     * message      string
     * timeout      int
     * notifyId     int
     */
    public static final short MESSAGE_SERVER = 122;
    
    /**
     * message      string
     * options      string
     * notifyId     int
     */
    public static final short QUESTION_SERVER = 123;
    
    
    /**
     * 技能列表，在登陆时下发，跟在关卡包后
     * count            byte
     * 循环:
     *  id              int
     *  name            string
     *  type            byte    0 近战 1 远程 2 魔法 3 AOE
     *  攻击距离            int
     *  攻击时间            int     （单位：毫秒）
     *  cd时间            int     （单位：毫秒）
     *  攻击范围            int
     */
    public static final short ABILITIES_SERVER = 124;
    
    /**
     * 添加关卡中可以接受的任务
     * 起始npcid          int
     * 终止npcid          int
     * 任务id         int
     * 任务等级         byte
     * 任务名字         string
     */
    public static final short QUEST_START_ADDED_SERVER = 125;
    
    
    /**
     * 移除关卡中可以接受的任务
     * 起始npcId          int
     * 任务id         int
     */
    public static final short QUEST_START_REMOVED_SERVER = 126;
    
    /**
     * 添加关卡中可完成的任务
     * 结束npcid          int
     * 任务id         int
     * 是否需要完成提醒 byte
     */
    public static final short QUEST_FINISH_ADDED_SERVER = 127;
    

    /**
     * 移除关卡中可完成的任务
     * 结束npcid          int
     * 任务id         int
     */
    public static final short QUEST_FINISH_REMOVED_SERVER = 128;
    
    /**
     * 获取任务描述
     * 任务id         int

     */
    public static final short QUEST_DESC_CLIENT = 129;
    
    
    /**
     * 任务描述
     * 任务id         int
     * 描述               string
     * 任务目标的数量  byte
     * 奖励组数量        byte
     * 循环n次
        奖励组Id                       byte
        奖励数量                        byte
        循环n次
            奖励类型                        byte
            奖励内容                        int,ITEM(属性值，如果类型是101，那么这个字段就是ITEM)
     */
    public static final short QUEST_DESC_SERVER = 130;
    
    /**
     * 接受任务
     * 任务id         int
     */
    public static final short QUEST_ACCEPT_CLIENT = 131;
    
    /**
     * 接受任务成功
     * 任务id         int
     * 任务etf            byte[]
     * 循环n次
     *  任务目标描述      string
     */
    public static final short QUEST_ACCEPTED_SERVER = 132;
    
    /**
     * 关卡载入完成，通知服务器可以正常进行游戏了
     */
    public static final short LOADING_FINISHED_CLIENT = 133;
    
    /**
     * 允许通过地图
     * 目标地图id       int
     * 目标地图x            int
     * 目标地图y            int
     */
    public static final short GOMAP_ALLOW_SERVER = 134;
    
    /**
     * 任务完成
     * 任务id         int
     */
    public static final short QUEST_FINISHED_SERVER = 135;
    
    /**
     * 攻击失败
     * 类型                       byte    1 距离太远 2 当前有攻击正在进行 3 目标已经死亡 4 目标不存在 
     *                                  5 没有技能 6 此技能不能在马上使用 7 这个技能必须选择一个目标
     *                                  8 这个技能不能对这个目标使用 9 目标没有死亡 10 次技能CD时间没到 11 当前状态不能使用此技能
     *                                  12 没有足够的mana
     * sourceInstanceId         int     源InstanceId
     * targetInstanceId         int     目标InstanceId
     * attackId                 int     技能Id
     */
    public static final short ATTACK_FAIL_SERVER = 136;
    
    /**
     * 完成任务
     * serial                   int
     * 任务Id                 int
     * 任务奖励Id               int
     */
    public static final short QUEST_FINISH_CLIENT = 137;
    
    /**
     * 完成任务失败
     * 任务Id         int
     * 任务失败原因       byte    1 没有任务 2 不能完成
     */
    public static final short QUEST_FINISH_FAIL_SERVER = 138;
    
    /**
     * 任务失败
     * 任务Id             int
     */
    public static final short QUEST_FAIL_SERVER = 139;
    
//  /**
//   * 物品变化的数量  byte
//   * 循环
//   *      itemId      int
//   *      instanceId  int
//   *      数量          byte
//   * 背包变化的数量  byte
//   * 循环
//   *      包格Id        int
//   *      itemId      int     -1 表示没有物品
//   *      instanceId  int 
//   *      当前数量        byte
//   */
//  public static final short BAG_CHANGED_SERVER = 139;
    
    /**
     * 获取unit的ctn文件
     * instanceId               int
     */
    public static final short CTNGET_CLIENT = 140; 
    
    /**
     * 得到unit的ctn文件
     * instanceId               int
     * imageId                  int
     * byte[]                   data
     */
    public static final short CTNGET_SERVER = 141;
    
    /**
     * 请求物品信息
     * 物品Id                         int
     * 物品实例Id                       int     -1 表示请求一般信息
     * 类型                               byte    0位为1说明需要ITEM信息，1位为1说明需要物品描述
     */
    public static final short ITEMINFO_CLIENT = 142;
    
    /**
     * 类型                               byte  0位为1说明需要ITEM信息，1位为1说明需要物品描述
     * 物品Id                         int
     * 物品实例Id                       int
     * 物品信息                         ITEM  如果没有要ITEM信息，那么这个字段不存在
     * 物品描述信息                       String 如果没有要描述信息，这个字段不存在
     */
    public static final short ITEMINFO_SERVER = 143;
    
    /**
     * 同步玩家数据
     * 同步数量                         byte
     * 重复n次
     *  属性描述                            short
     *  属性值                         int,string,complex
     * 提示数量
     * 重复n次
     *  属性描述                            short
     *  属性值                         int,string,complex
     */     
    public static final short SYNC_PLAYER_SERVER = 144;
    
    /**
     * 使用物品
     * 包格Id                     byte    如果没指定包格，那么包格Id为-1
     * 物品Id                     int
     * 物品实例Id                   int
     * 目标Id                     int
     * 使用时间                     int
     */
    public static final short USEITEM_CLIENT = 145;
    
    /**
     * 获取包格物品信息
     */
    public static final short BAG_CLIENT = 146;
    
    /**
     * 包格数量                     byte
     * 循环n次
     *  包格信息                        BAG
     */
    public static final short BAG_SERVER = 147;
    
    /**
     * 移除物品
     * 包格Id                     byte
     * 物品数量                     byte
     */
    public static final short REMOVEITEM_CLIENT = 148;
    

    /**
     * 取任务描述
     * 任务Id                     int
     */
    public static final short QUEST_PREDESC_CLIENT = 149;
    
    /**
     * 任务描述
     * 任务Id                     int
     * 任务起始描述                   String
     * 奖励组数量        byte
     * 循环n次
        奖励组Id                       byte
        奖励数量                        byte
        循环n次
            奖励类型                        byte
            奖励内容                        int,ITEM(属性值，如果类型是101，那么这个字段就是ITEM)
     */
    
    public static final short QUEST_PREDESC_SERVER = 150;
    
    /**
     * 取任务结束描述
     * 任务Id                     int
     */
    public static final short QUEST_POSTDESC_CLIENT = 151;
    
    /**
     * 任务结束描述
     * 任务Id                     int
     * 任务结束描述                   string
     * 奖励组数量        byte
     * 循环n次
        奖励组Id                       byte
        奖励数量                        byte
        循环n次
            奖励类型                        byte
            奖励内容                        int,ITEM(属性值，如果类型是101，那么这个字段就是ITEM)
     */
    public static final short QUEST_POSTDESC_SERVER = 152;
    
    /**
     * 获取玩家当前的技能列表
     */
    public static final short SKILL_LIST_CLIENT = 153;
    /**
     * 玩家当前的技能列表
     * 技能数量                     byte
     * 循环n次
     *  技能                          SKILL
     */
    public static final short SKILL_LIST_SERVER = 154;
    
    /**
     * 给技能加点
     * serial                       int
     * 技能GroupId                    byte
     * 技能等级                     byte
     */
    public static final short SKILL_ADDPOINT_CLIENT = 155;
    
    /**
     * 洗技能点
     * serial                       int
     */
    public static final short SKILL_REFRESH_CLIENT = 156;
    
    /**
     * 取技能名字列表
     */
    public static final short SKILL_NAMELIST_CLIENT = 157;
    
    /**
     * 技能名字列表
     * 技能数量                     byte
     * 循环n次
     *  技能GroupId                   byte
     *  技能名                     string
     */
    public static final short SKILL_NAMELIST_SERVER = 158;
    
    /**
     * 获取任务列表
     */
    public static final short QUEST_LIST_CLIENT = 159;
    
    /**
     * 任务列表
     * 任务数量                     byte
     * 循环n次
     *  任务Id                        int
     *  起始npcid                 int
     *  终止npcid                 int
     *  任务等级                        byte
     *  任务名字                        string
     *  任务etf                       byte[]
     *  任务变量                        int[]
     *  目标数量                        byte
     *  循环n次
     *   任务目标描述                 string
     */
    public static final short QUEST_LIST_SERVER = 160;
    
    /**
     * 任务信息下发，暂时只有场景任务需要
     * 任务Id                     int
     * 任务名字                     string
     * 任务etf                        byte[]
     * 任务变量                     int[]
     */
    public static final short AREAQUEST_INFO_SERVER = 161;
    
    /**
     * 客户端跟服务器端同步变量
     * 任务Id                     int
     * 变量Index                      int
     * 变量值                      int
     */
    public static final short VM_VARIABLE_SYNC_CLIENT = 162;
    
    /**
     * 服务器跟客户端同步任务变量
     * 同步数量                     byte
     * 循环n次
     *  任务Id                        int
     *  变量Index                 int
     *  变量值                     int
     */
    public static final short VM_VARIABLE_SYNC_SERVER = 163;
    
    /**
     * 获取物品描述
     * serial                       int
     * 物品Id                     int
     */
    public static final short ITEM_DESC_CLIENT = 164;
    
    /**
     * 物品描述
     * serial                       int
     * 物品Id                     int
     * 描述                           string
     */
    public static final short ITEM_DESC_SERVER = 165;
    
    /**
     * 登陆帐号
     * serial                       int
     * 帐号名                      string
     * 密码                           string
     */
    public static final short ACCOUNT_LOGIN_CLIENT = 166;
    
    /**
     * 登陆帐号成功
     * serial                       int
     * 帐号Id                     int
     * 帐号名                      string
     * i币                           int                     
     */
    public static final short ACCOUNT_LOGIN_SERVER = 167;
    
    /**
     * 获取角色列表
     * serial                       int
     */
    public static final short ACTOR_LIST_CLIENT = 168; 

    /**
     * 角色列表
     * serial                       int
     * 角色数量                     byte
     * 循环n次                     
     *  Id                      int
     *  名字                      int
     *  性别                      byte
     *  等级                      byte
     *  职业                      byte
     *  阵营                      byte
     */
    public static final short ACTOR_LIST_SERVER = 169;
    
    /**
     * 创建角色
     * serial                       int
     * 名字                           string
     * 性别                           byte
     * 职业                           byte
     * 阵营                           faction
     */
    public static final short ACTOR_CREATE_CLIENT = 170;
    
    /**
     * 创建角色成功
     * serial                       int
     *  Id                      int
     *  名字                      int
     *  性别                      byte
     *  等级                      byte
     *  职业                      byte
     */
    public static final short ACTOR_CREATE_SERVER = 171;
    
    
    /**
     * 放弃任务
     * serial                       int
     * 任务Id                     int
     */
    public static final short QUEST_ABANDON_CLIENT = 172;
    
    /**
     * 放弃任务成功
     * serial                       int
     * 任务Id                     int
     */
    public static final short QUEST_ABANDON_SERVER = 173;
    
    /**
     * 界面Notify
     * 任务Id                     int
     * notifyId                     byte
     * notifyType                   byte    1 chat 2 message 3 question
     * questionAnswer               byte
     */
    public static final short NOTIFY_CLIENT = 174;
    
    /**
     * 装备
     * serial                       int
     * ItemId                       int
     * instanceId                   int
     */
    public static final short EQUIP_CLIENT = 175;

    /**
     * 装备成功
     * serial                       int
     * 
     */
    public static final short EQUIP_SERVER = 176;

    /**
     * 卸下装备
     * serial                       int
     * ItemId                       int
     * instanceId                   int
     */
    public static final short UNEQUIP_CLIENT = 177;

    /**
     * 卸下装备成功
     * serial                       int
     */
    public static final short UNEQUIP_SERVER =178;
    
    /**
     * 增加属性点
     * serial                       int
     * strength                     short   //力量上新添加的点
     * agility                      short
     * stamina                      short
     * intellect                    short
     */
    public static final short PROPERTYPOINT_ADD_CLIENT = 179;
    
    /**
     * 增加属性点成功
     * serial                       int
     */
    public static final short PROPERTYPOINT_ADD_SERVER = 180;
    
    
    /**
     * 获取技能描述
     * serial                       int
     * 技能组Id                        byte
     * 技能等级                     byte
     */
    public static final short SKILL_DESC_CLIENT = 181;
    
    /**
     * 技能描述
     * serial                       int
     * 升级点数                     byte
     * 描述                           string
     */
    public static final short SKILL_DESC_SERVER = 182;
    
    /**
     * 加点成功
     * serial                       int
     */
    public static final short SKILL_ADDPOINT_SERVER = 183;
    
    /**
     * 洗点成功
     * serial                       int
     */
    public static final short SKILL_REFRESH_SERVER = 184;
    
    
    /**
     * 技能攻击
     * 时间                           int
     * x                            int
     * y                            int
     * 方向                           byte
     * 目标InstanceId             int
     * 技能Id                     int     
     */
    public static final short SKILL_ATTACK_CLIENT = 185;
    
    /**
     * 技能攻击
     * 源InstanceId                  int
     * 目标InstanceId             int
     * 释放动画ID                       int
     * 
     */
    public static final short SKILL_ATTACK_SERVER = 186;
    
    /**
     * 被攻击
     * 目标InstanceId             int
     * 时间                           int
     * 源InstanceId                  int
     * 攻击结果类型                   byte    0 命中 1 miss 2 免疫 3 命中且暴击
     * 伤害类型                         byte     0 物理 1 法术 2 抽蓝 3 诅咒 4 加血 5 回蓝 6 增强
     * 伤害值                      int     只有在命中时有意义
     * 受攻击动画                    int
     */
    public static final short SKILL_ATTACKED_SERVER = 187;
    
    /**
     * 版本比较
     * 数量                           short
     * 循环n次
     *  文件名                     string
     *  版本                          int
     */
    public static final short VERSION_COMPARE_CLIENT = 188;
    
    /**
     * 版本比较结果
     * 数量                           short
     * 循环n次
     *  需要删除缓存的文件名          string
     *  文件版本                        int
     */
    public static final short VERSION_COMPARE_SERVER = 189;

    /**
     * 强制进行版本比较
     */
    public static final short SYNC_VERSION_SERVER = 190;
    
    /**
     * 获取文件
     * 文件名                      string
     */
    public static final short GETFILE_CLIENT = 191;
    /**
     * 文件信息
     * 文件名                      string
     * 版本信息                     int
     * 文件内容                     byte[]
     */
    public static final short GETFILE_SERVER = 192;
    
    /**
     * NPC，玩家，怪物进入(走出)视野
     * type                         byte(第7位如果为1，表示走出视野)
     * id                           int
     * instanceId                   int
     */
    public static final short UNIT_REFRESH_SERVER = 193;
    
    
//  /**
//   * UNIT次级信息
//   * type                         byte
//   * id                           int
//   * name                         string
//   * level                        byte
//   * faction                      byte
//   * state                        short
//   */
//  public static final short UNIT_DETAIL_SERVER = 194;
    
    /**
     * UNIT行走信息
     * type                         byte 起始的5位分别代表一下的5段内容是否包含
     * instanceId                   int
     * mapid                        short (第一段)
     * x                            short (第一段)
     * y                            short (第一段) 
     * 角度                           byte（角度/2）(第二段)
     * 时间                           int(从系统启动开始计算的毫秒速) (第二段)
     * 速度                           byte(每秒的像素） (第二段)
     * hp百分比                        byte(200为单位)    (第三段)
     * mp百分比                        byte(200为单位) (第三段)
     * state                        short (第四段) (0 running 1 attack 2 ride 3 die 4 组队 5 队长)
     * 第五段的Mask                 byte  (只有第五段存在时次字段才存在 0 name 1 level 2 faction 3 装备分数 4 sex)
     * name                         string (第五段 0)
     * level                        byte (第五段 1)
     * faction                      byte (第五段 2)
     * headscore                    int  (第五段 3)
     * bodyscore                    int  (第五段 3)
     * weaponscore                  int  (第五段 3)
     * sex                          byte (第五段 4)
     */
    public static final short UNIT_MOVE_SERVER = 195;
    
    
    /**
     * 请求unit信息
     * instanceId                   int
     */
    public static final short UNIT_INFO_CLIENT = 196;
    
    /**
     * unit信息
     * instanceId                   int
     * 如果是NPC
     *  NPCImageID                  int
     *  通过性                     byte
     *  是否是功能NPC                byte
     *  功能名字                        string
     * 如果是其他玩家
     *  性别                          byte
     *  职业                          byte
     *  工会                          string
     *  荣誉                          string
     * 如果是资源
     *  NPCImageID                  int
     *  任务ID                        int (如果小于0，那么跟任务无关)
     */
    public static final short UNIT_INFO_SERVER = 197;
    /**
     * instanceId                   int
     */
    public static final short GATHER_START_CLIENT = 198;
    
    /**
     * instanceId                   int
     */
    public static final short GATHER_END_CLIENT = 199;
    
    public static final short RELOAD_CLIENT = 200;

    /**
     * 请求添加好友/黑名单/仇人。
     * serial   int
     * id       int         玩家ID，-1表示不使用此参数
     * name     String      玩家名称，空串表示不使用此参数
     * type     byte        类型：0 - 好友、1 - 黑名单、2 - 仇人
     */
    public static final short ADD_FRIEND_CLIENT = 241;
    /**
     * 添加好友/黑名单/仇人成功。
     * serial   int
     * id       int         玩家ID
     * name     String      玩家名称
     * type     byte        类型：0 - 好友、1 - 黑名单、2 - 仇人
     * degree   int         好友度/仇人度
     * onine    boolean     是否在线，只有在线玩家的下面4个参数才有效
     * level    short       级别
     * sex      byte        性别
     * clazz    byte        职业
     * tong     String      军团
     */
    public static final short ADD_FRIEND_SERVER = 242;
    /**
     * 请求删除好友/黑名单/仇人。
     * serial   int
     * id       int         玩家ID
     * type     byte        类型：0 - 好友、1 - 黑名单、2 - 仇人
     */
    public static final short DEL_FRIEND_CLIENT = 243;
    /**
     * 删除好友/黑名单/仇人成功。
     * serial   int
     * id       int         玩家ID
     * type     byte        类型：0 - 好友、1 - 黑名单、2 - 仇人
     */
    public static final short DEL_FRIEND_SERVER = 244;
    /**
     * 取关联玩家列表。
     * serial   int
     */
    public static final short GET_FRIENDLIST_CLIENT = 245;
    /**
     * 返回关联玩家列表。
     * serial   int
     * count    int         列表大小
     *  循环N次
     *      id      int         玩家ID
     *      name    String      玩家名称
     *      type    byte        类型：0 - 好友、1 - 黑名单、2 - 仇人、3 - 临时
     *      degree  int         好友度/仇人度/临时好友交互类型
     *      onine   boolean     是否在线，只有在线玩家的下面4个参数才有效
     *      level   short       级别
     *      sex     byte        性别
     *      clazz   byte        职业
     *      tong    String      军团
     */
    public static final short GET_FRIENDLIST_SERVER = 246;
    /**
     * 好友/仇人上下线通知。
     * id       int         好友/仇人ID
     * name     String      好友/仇人名称
     * online   byte        1 - 上线，0 - 下线 
     */
    public static final short FRIEND_ONLINE_SERVER = 247;
    
    /**
     * 发送聊天信息
     * channel                      byte  0 世界 1 国家 2 地区 3 同乡 4 帮派 5 队伍 6 私聊 7 系统(系统频道不可用，私聊需要加上对方Id，其他忽略)
     * destId                       int
     * message                      string
     * attachment                   byte[] 如果是物品{01(byte),itemId(int),instanceId(int)},如果是任务{02{byte},questId(int)}
     */
    public static final short CHAT_CLIENT = 201;
    
    /**
     * 聊天信息
     * channel                      byte
     * sourceId                     int 
     * name                         string 
     * message                      string
     * attachment                   byte[] 如果是物品{01(byte),itemId,instanceId(int),name(string),showType(byte),quality(byte)},如果是任务{02{byte},questId(int,name(string)}
     * 
     */
    public static final short CHAT_SERVER = 202;
    
    /**
     * 聊天信息设置
     * serial                       int
     * 改变了的聊天设置数量           byte
     * 重复n次
     *  聊天频道                        byte
     *  聊天设置                        byte  (0~3 颜色的Index,4 是否加入该频道 5 收到信息是否提示)
     * 
     */
    public static final short CHAT_OPTION_CLIENT = 203;
    
    /**
     * 聊天信息设置成功
     * serial                       int
     */
    public static final short CHAT_OPTION_SERVER = 204;
    
    /**
     * 改变同乡信息
     * serial                       int
     * 地区                           string
     */
    public static final short CHAT_NATIVE_CHANGE_CLIENT = 205;

    /**
     * 改变同乡信息成功
     * serial                       int
     */
    public static final short CHAT_NATIVE_CHANGE_SERVER = 206;
    
    /**
     * 取玩家信息
     * serial                       int
     * 玩家Id                     int
     */
    public static final short PLAYER_INFO_CLIENT = 207;
    
    /**
     * 玩家信息
     * serial                       int
     * 角色名称                     String
     * 级别                           byte
     * 职业                           byte
     * 阵营                           byte
     * 军团                           string
     * 家乡                           string
     * 称号                           string
     * 师傅                           string
     * 夫妻                           string
     * 军衔                           string
     * 装备信息                     byte[]
     */
    public static final short PLAYER_INFO_SERVER = 208;
    
    /**
     * 创建队伍
     * serial                       int
     */
    public static final short PARTY_CREATE_CLIENT = 209;
    
    /**
     * 创建队伍成功
     * serial                       int
     */
    public static final short PARTY_CREATE_SERVER = 210;

    /**
     * 邀请加入队伍
     * id                           int
     */
    public static final short PARTY_INVIT_CLIENT = 211;

    /**
     * 邀请加入队伍
     * 邀请序号                     int
     * 队长Id                     int
     * 队长名字                     string
     * 队长等级                     byte
     * 队长职业                     byte
     */
    public static final short PARTY_INVIT_SERVER = 212;
    
    /**
     * 答应组队请求
     * 邀请序号                     int
     */
    public static final short PARTY_INVIT_OK_CLIENT = 213;
    
    /**
     * 拒绝组队请求
     * 邀请序号                     int
     */
    public static final short PARTY_INVIT_REJECT_CLIENT = 214;
    
    /**
     * 拒绝组队请求
     * 拒绝人名字                    string
     * 拒绝原因                     string
     */
    public static final short PARTY_INVIT_REJECT_SERVER = 215;

    /**
     * 小队信息
     * 小队成员数量                   byte
     * 循环n次
     *  成员Id                        int
     *  成员名字                        string
     *  成员等级                        byte
     *  成员职业                        byte
     *  hp百分比                       byte(200为单位) 
     *  mp百分比                       byte(200为单位)
     *  成员状态                        byte(最高位为1表明是队长,第0位为1表明现在离线)
     */
    public static final short PARTY_INFO_SERVER = 216;
    
    /**
     * 踢出队员(队长有效)
     * 成员Id                     int
     */
    public static final short PARTY_KICK_CLIENT = 217;
    
    
    /**
     * 被踢出队伍
     */
    public static final short PARTY_KICK_SERVER = 218;

    /**
     * 离开队伍
     */
    public static final short PARTY_LEAVE_CLIENT = 219;
    
    /**
     * 离开队伍成功
     */
    public static final short PARTY_LEAVE_SERVER = 220;
    
    /**
     * 发送邮件
     * serial                           int
     * destName                         string
     * title                            string
     * content                          string
     * price                            int
     * attachment                       byte[]如果是物品{1(byte),itemId(int),instanceId(int),count(byte)}，如果是金钱{2(byte),count(int)}
     */
    public static final short MAIL_POST_CLIENT = 221;
    
    /**
     * serial                           int
     */
    public static final short MAIL_POST_SERVER = 222;
    
    /**
     * 获取Mail列表
     * serial                           int
     * 每页条数                         short
     * 页数                               short
     */
    public static final short MAIL_LIST_CLIENT = 223;
    
    /**
     * Mail列表
     * serial                           int
     * 每页条数                         short
     * 页数                               short
     * 信件的总数                        int
     * 本页实际条数                       short
     * 循环n次
     *  mailId                          int
     *  sourceId                        int
     *  sourceName                      string
     *  title                           string
     *  date                            string
     *  status                          byte (0位标识是否看过 1位标识是否收藏)
     *  是否有附件                       byte (0 没有 1 有)
     */
    public static final short MAIL_LIST_SERVER = 224;
    
    /**
     * 获取Mail内容
     * serial                           int
     * mailId                           int
     */
    public static final short MAIL_CONTENT_CLIENT = 225;
    
    /**
     * Mail内容
     * serial                           int
     * mailId                           int
     * sourceId                         int
     * sourceName                       string
     * title                            string
     * content                          string
     * date                             string
     * price                            int
     * attachment                       byte[]如果是物品{1(byte),itemId(int),instanceId(int),count(byte),name(string)}，如果是金钱{2(byte),count(int)}
     */
    public static final short MAIL_CONTENT_SERVER = 226;

    /**
     * 提取附件
     * serial                           int
     * mailId                           int
     */
    public static final short MAIL_ATTACHMENT_CLIENT = 227;
    
    /**
     * 提取附件成功
     * serial                           int
     * mailId                           int
     */
    public static final short MAIL_ATTACHMENT_SERVER = 228;
    
    /**
     * 删除邮件
     * serial                           int
     * mailId                           int 如果是-1那么代表删除非收藏邮件
     */
    public static final short MAIL_DELETE_CLIENT = 229;
    /**
     * 删除邮件成功
     * serial                           int
     * mailId                           int
     */
    public static final short MAIL_DELETE_SERVER = 230;
    
    /**
     * 收藏邮件
     * serial                           int
     * mailId                           int
     */
    public static final short MAIL_FAVORITE_CLIENT = 231;
    /**
     * 搜藏邮件成功
     * serial                           int
     * mailId                           int
     */
    public static final short MAIL_FAVORITE_SERVER = 232;
    
    /**
     * 有新邮件
     */
    public static final short MAIL_NEW_SERVER = 233;
    
    
    /**
     * 脚本名字                             string
     * 参数字符串                            string
     */
    public static final short OPENUI_SERVER = 235;
    
    /**
     * 获取动作条Option
     */
    public static final short ACTIONBAR_OPTION_CLIENT = 236;
    /**
     * 动作条Option
     * options                              byte[]
     */
    public static final short ACTIONBAR_OPTION_SERVER = 237;
    
    /**
     * 设置动作条Option
     * serial                               int
     * options                              byte[]
     */
    public static final short SET_ACTIONBAR_OPTION_CLIENT = 238;
    
    /**
     * 设置动作条Option成功
     * serial                               int
     */
    public static final short SET_ACTIONBAR_OPTION_SERVER = 239;
    
    
    
    /**
     * 请求创建军团。
     * serial   int
     * name     String      军团名称
     */
    public static final short TONG_CREATE_CLIENT = 251;
    /**
     * 创建军团成功。
     * serial   int
     * id       int         军团ID
     * name     int         军团名称
     * duty     int         军团职务
     */
    public static final short TONG_CREATE_SERVER = 252;
    /**
     * 军团成员属性变更通知（可能是自己的，也可能是别人的）。
     * id       int         角色ID
     * tid      int         军团ID，-1表示无军团
     * tname    String      军团名称
     * duty     int         军团职务
     * title    String      军团头衔
     * forbid   byte        是否禁言，1 - 是，0 - 否
     */
    public static final short TONG_MEMBER_CHANGE_SERVER = 253;
    /**
     * 取军团成员列表。
     * serial   int
     * start    short       页号，0表示第一页
     * page     short       页大小
     */
    public static final short TONG_LIST_CLIENT = 254;
    /**
     * 返回军团成员列表。
     * serial   int
     * tid      int         军团ID
     * tname    String      军团名称
     * slogan   String      军团公告
     * duty     int         本用户的职务
     * title    String      本用户的头衔
     * pcount   short       总页数
     * pno      short       当前页号（0表示第一页）
     * count    short       返回记录数
     *  循环N次
     *      pid     int         角色ID
     *      pname   String      角色名称
     *      online  byte        是否在线，0 - 不在线、1 - 在线
     *      level   short       级别（只对在线用户有效）
     *      sex     byte        性别（只对在线用户有效）
     *      clazz   byte        职业（只对在线用户有效）
     *      duty    int         职务
     *      title   String      头衔
     *      honor   int         荣誉
     *      forbid  byte        是否禁言，1 - 禁言、0 - 未禁言
     */
    public static final short TONG_LIST_SERVER = 255;
    /**
     * 邀请加入军团。
     * serial   int
     * tid      int         目标角色ID，-1表示无效
     * tname    int         目标角色名称
     */
    public static final short TONG_INVITE_CLIENT = 256;
    /**
     * 邀请发送成功。
     * serial   int
     */
    public static final short TONG_INVITE_SERVER = 257;
    /**
     * 向被邀请加入军团的角色发送的通知。
     * sid      int         邀请人ID
     * sname    String      邀请人名称
     * tid      int         邀请人军团ID
     * tname    String      邀请人军团名称
     * invid    int         请柬ID
     */
    public static final short TONG_INVITATION_SERVER = 258;
    /**
     * 请求加入军团。
     * serial   int
     * invid    int         请柬ID
     */
    public static final short TONG_JOIN_CLIENT = 259;
    /**
     * 加入军团成功。
     * serial   int
     * tid      int         军团ID
     * tname    String      军团名称
     * duty     int         军团职务
     */
    public static final short TONG_JOIN_SERVER = 260;
    /**
     * 拒绝军团邀请。
     * invid    int         请柬ID
     */
    public static final short TONG_REJECT_CLIENT = 261;
    /**
     * 请求退出军团。
     * serial   int
     */
    public static final short TONG_QUIT_CLIENT = 262;
    /**
     * 退出军团成功。
     * serial   int
     */
    public static final short TONG_QUIT_SERVER = 263;
    /**
     * 请求修改军团公告。
     * serial   int
     * slogan   String      新军团公告
     */
    public static final short TONG_SET_SLOGAN_CLIENT = 264;
    /**
     * 修改军团公告成功。
     * serial   int
     * slogan   String      新军团公告
     */
    public static final short TONG_SET_SLOGAN_SERVER = 265;
    /**
     * 请求提升/降职（包括转让都督职务）。
     * serial   int
     * tid      int         目标角色ID
     * op       byte        0 - 升职、1 - 降职
     */
    public static final short TONG_PROMOTE_CLIENT = 266;
    /**
     * 提升/降职成功。
     * serial   int
     * duty     int         请求者的新职务
     * tid      int         目标角色ID
     * tduty    int         目标角色的新职务
     */
    public static final short TONG_PROMOTE_SERVER = 267;
    /**
     * 请求移除军团成员。
     * serial   int
     * tid      int         目标角色ID
     */
    public static final short TONG_KICK_CLIENT = 268;
    /**
     * 移除军团成员成功。
     * serial   int
     * tid      int         目标角色ID
     */
    public static final short TONG_KICK_SERVER = 269;
    /**
     * 请求禁言/解除禁言某军团成员。
     * serial   int
     * tid      int         目标角色ID
     */
    public static final short TONG_FORBID_CLIENT = 270;
    /**
     * 禁言/解除禁言成功。
     * serial   int
     * tid      int         目标角色ID
     * forbid   byte        目标的新禁言状态，1 - 禁言、0 - 未禁言
     */
    public static final short TONG_FORBID_SERVER = 271;
    
    /**
     * roll点
     * rollId                               int
     * 方式                                   byte (0 放弃 1 roll)
     */
    public static final short ROLL_CLIENT = 280;
    
    /**
     * 下发可roll物品
     *  rollId                              int
     *  物品                                  ITEM
     *  到期时间                                int
     */
    public static final short ROLL_SERVER = 281;
    
    /**
     * 发起PK
     * 对方ID                             int
     * 赌注                                   int
     */
    public static final short PK_INVIT_CLIENT = 282;
    
    /**
     * PKID                                 int
     * 发起PK玩家Id                         int
     * 发起PK玩家名字                     string
     * 发起PK玩家等级                     byte
     * 发起PK玩家职业                     byte
     * 赌注                                   int
     */
    public static final short PK_INVIT_SERVER = 283;
    
    /**
     * 拒绝PK
     * PKID                                 int
     */
    public static final short PK_REFUSE_CLIENT = 284;
    
    /**
     * 拒绝PK
     * 拒绝PK原因                           string
     */
    public static final short PK_REFUSE_SERVER = 285;
    
    /**
     * 同意PK
     * PKID                                 int
     */
    public static final short PK_OK_CLIENT = 286;
    
    /**
     * PK建立成功
     * 源InstanceId                          int
     * 目标InstanceId                     int
     * 赌注                                   int
     * 中心点x坐标                           int
     * 中心点y坐标                           int
     * PK范围(正方形半径)                  int
     */
    public static final short PK_OK_SERVER = 287;
    
    /**
     * PK结束
     * 结束类型                             byte(0 正常结束 1 打断)
     * 获胜方InstanceId                        int
     */
    public static final short PK_OVER_SERVER = 288;
    
    /**
     * Buff列表
     * Buffs                                    BUFFS
     */
    public static final short BUFFLIST_SERVER = 290;
    
    /**
     * 同步buff
     * 移除的Buff数量                        byte
     * 循环n次
     *  instanceId                          int
     * 增加的Buff数量                        byte
     * 循环n次
     *  instanceId                          int
     *  IconId                              int
     *  time(Buff失效时间)                  int
     * 合并了的Buff数量                       byte
     * 循环n次
     *  instanceId                          int
     *  IconId                              int
     *  time                                int
     */
    public static final short SYNC_BUFF_SERVER = 291;
    
    /**
     * 冷却组CoolDown
     * 冷却组Id                                short
     * 开始时间                             int
     * 结束时间                             int
     */
    public static final short COOLDOWN_SERVER = 292;
    
    /**
     * 冷却组消失
     * 数量                                   byte
     * 循环n次
     *  冷却组ID                               short
     */
    public static final short COOLDOWN_END_SERVER = 293;
    
    /**
     * 玩家死亡消息
     * 释放的到期时间                      int
     * 复活选项数量                           byte
     * 循环n次
     *  复活Id                                int
     *  选项显示字符串                     string
     */
    public static final short DIE_SERVER = 294;
    
    
    /**
     * 复活
     * 复活Id                             int
     */
    public static final short RELIVE_CLIENT = 295;
    
    /**
     * 通知玩家复活
     * InstanceId                           int
     * 复活时的地图mapId                      int
     * 复活时的x坐标                          int
     * 复活时的y坐标                          int
     * 复活所使用的动画                     int             
     */
    public static final short RELIVE_SERVER = 296;
    
    /**
     * 技能攻击
     * 源InstanceId                  int
     * 目标InstanceId             int
     * 起手动画ID                   int
     * 
     */
    public static final short SKILL_PREPARE_ATTACK_SERVER = 297;
    
    /**
     * 取商店商品列表
     * serial       int
     * shopIDs      int[]       商店ID列表
     */
    public static final short SHOP_LIST_CLIENT = 300;
    /**
     * 返回商店商品列表
     * serial       int
     * count        byte        商店数量
     * 循环N次
     *   shopID     short       商店ID
     *   title      String      商店标题
     *   itemCount  byte        商品数量
     *   循环N次
     *     id       int         物品ID
     *     name     String      物品名称
     *     quality  byte        品质
     *     icon     byte        图标ID
     *     remain   short       剩余数量，0表示无限制
     *     limit    byte        购买上限，0表示无限制
     *     reqcnt   byte        购买需求项数量，0表示无限制
     *       循环N次
     *         reqtype  byte    需求类型，见Shop类中的常量
     *         amount   int     需求金钱/i币/荣誉，或物品数量，或军衔ID
     *         deduct   byte    是否扣除，1表示是，0表示否
     *         rank     String  需求荣誉名称，只有需求类型为TYPE_RANK时有效
     *         itemID   int     物品ID，只有需求类型为TYPE_ITEM时有效
     *         itemName String  物品名称，只有需求类型为TYPE_ITEM时有效
     *         quality  byte    品质，只有需求类型为TYPE_ITEM时有效
     *         icon     byte    图标ID，只有需求类型为TYPE_ITEM时有效
     */
    public static final short SHOP_LIST_SERVER = 301;
    /**
     * 请求购买商品
     * serial       int
     * shopID       short       商店ID
     * itemID       int         物品ID
     * count        short       购买数量
     */
    public static final short SHOP_BUY_CLIENT = 302;
    /**
     * 购买商品成功
     * serial       int
     * itemID       int         物品ID
     * count        short       购买数量
     * itemName     String      物品名称
     * quality      byte        品质
     * icon         byte        图标ID
     */
    public static final short SHOP_BUY_SERVER = 303;
    /**
     * 请求出售物品
     * serial       int
     * itemID       int         物品ID
     * instanceID   int         实例ID
     * count        short       物品数量
     */
    public static final short SHOP_SELL_CLIENT = 304;
    /**
     * 出售物品成功
     * serial       int
     * amount       int         获得金钱数量
     */
    public static final short SHOP_SELL_SERVER = 305;
    
    /**
     * 删除角色
     * 角色Id                             int
     */
    public static final short ACTOR_DELETE_CLIENT = 310;
    
    /**
     * 删除角色成功
     * 角色Id                             int
     */
    public static final short ACTOR_DELETE_SERVER = 311;
    
    /**
     * 取buff描述
     * 数量                                   byte
     * 循环n次
     *  instanceId                          int
     */
    public static final short BUFF_DESC_CLIENT = 312;
    
    /**
     * buff描述
     * 数量                                   byte
     * 循环n次
     *  instanceId                          int
     *  name                                string
     *  desc                                string
     */
    public static final short BUFF_DESC_SERVER = 313;
    
    /**
     * 取消自动攻击
     */
    public static final short CANCEL_AUTOATTACK_CLIENT = 314;
    
    /**
     * 自动攻击开始
     * 自动攻击对象InstanceId             int
     */
    public static final short AUTOATTACK_START_SERVER = 315;
    
    /**
     * 注册帐号
     * serial                               int
     * 帐号名                              string
     * 电话号码                             string
     * model                                string
     * version                              string
     */
    public static final short ACCOUNT_REG_CLIENT = 316;
    
    /**
     * 注册帐号成功
     * serial                               int
     * 帐号名                              string
     * 帐号Id                             int
     * 密码                                   string
     */
    public static final short ACCOUNT_REG_SERVER = 317;
    
    /**
     * 取消攻击
     */
    public static final short CANCEL_ATTACK = 318;
    /**
     * 取消使用物品
     */
    public static final short CANCEL_USEITEM = 319;
    
    /**
	 * 强制过地图
	 * 目标地图id			int
	 * 目标地图InstanceId	int
	 * 目标地图x				int
	 * 目标地图y				int
	 * 是否允许使用跟随		byte 	(1 允许 0 不允许)
	 */
	public static final short FORCE_GOMAP_SERVER = 321;
    
}
/**
类型定义

1   ITEM                            
        物品Id                        int
        物品名                     string
        最大堆叠                        byte
        物品显示类型+绑定类型         byte(6+2)
            红药水                 1
            蓝药水                 2                                   
            紫药水                 3                                   
            状态类                 4                                   
            生产类                 5                                   
            精炼类                 6                                   
            卷轴类                 7                                   
            坐骑类                 8                                   
            技能类                 9                                   
            书籍类                 10                                  
            杂物类                 11
        使用等级                        byte                                    
        品质                          byte                                    
        价格                          int 
        使用类型                        byte
        是否能使用       (0)
            战斗中使用       (1:非战斗状态使用 2:战斗状态使用)
            使用的目标类型   (3:对自己使用 4:对队友使用 5:对敌人使用) 
            使用是否消耗      (6)
        使用生效时间                  short   (如果不能使用此字段不存在)
        cd组                         byte    (如果不能使用此字段不存在)
        cd时间                        int     (如果不能使用此字段不存在)
        使用距离                        byte    (如果不能使用此字段不存在 单位：码) 
        使用次数                        byte    (如果不能使用此字段不存在)
        是否是装备                   byte
        装备使用等级                  byte    (如果不是装备字段不存在)
        装备职业限制                  byte     (如果不是装备字段不存在)
        装备部位                        byte    (如果不是装备字段不存在)
        装备力量限制                  short   (如果不是装备字段不存在)
        装备敏捷限制                  short   (如果不是装备字段不存在)
        mask1                       byte (0:最大hp 1:最大mp 2:力量 3:敏捷 4:耐力 5:智力 6:物理攻击力 7:魔法攻击力)    (如果不是装备字段不存在)
        mask2                       byte (0:物理防御 1:魔法防御 2:命中等级 3:闪避等级 4:物理暴击等级 5:魔法暴击等级 6:生命恢复 7:魔法恢复)  (如果不是装备字段不存在)
        mask3                       byte (0:装甲 1:伤害下限 2:伤害上限 3:耐久)  (如果不是装备字段不存在)
        循环N次                                (如果不是装备字段不存在)   
            根据mask读取属性          short   (如果不是装备字段不存在)
        剩余次数                        byte    (如果不能使用此字段不存在)
        到期时间                        int
        绑定状态                        byte
        剩余耐久                        short
        物品实例Id                  int     
2   SKILL                           
        技能组Id                       byte
        技能等级                        byte
        技能名字                        string
        技能攻击距离                  short
        技能攻击时间                  short
        技能cd时间                  short
        技能范围                        byte
        技能类型                        byte    //技能类型标志，按位划分(0:主动伤害1:主动辅助2:被动技能3:光环技能7:可装配技能)
        技能目标类型                  byte    //技能目标类型，按位划分(0:目标阵营1:目标范围2:AOE中心)
        升级需要的点数             byte
        技能动画Id                  int     //技能动画Id
        技能图标Id                  int     //图标Id
        技能消耗mana                    short
        技能可使用武器
         数量                     byte
          循环n次
          武器Id                      byte
        是否有下一个级别                boolean
        下一个级别需要的等级          byte
3   GRID
        包格Id                        byte
        物品数量                        byte
        物品                          ITEM    //如果数量为0则没有这个字段存在
4   SKILL_SIMPLE
         技能Id                       byte
         技能level                    byte
5   ACTOR
        id                          int
        名字                          string
        性别                          byte
        等级                          byte
        职业                          byte
        阵营                          byte
        最大Hp                        short
        最大Mp                        short
        当前Hp                        short
        当前Mp                        short
        力量                          short
        敏捷                          short
        耐力                          short
        智力                          short
        攻击上限                        short
        攻击下限                        short
        法术攻击力                   short
        防御                          short
        法术防御                        short
        暴击*100                      short
        法暴*100                      short
        命中*100                      short
        法术命中*100                    short
        闪避*100                      short
        法术闪避*100                    short
        物理减伤                        short
        每五秒回血                   short
        每五秒回蓝                   short
        剩余技能点                   short
        剩余属性点                   short
        当前经验                        int
        到下一级需要的经验           int
        金钱                          int
        地图Id                        short
        x                           short
        y                           short
        方向                          short
        状态                          short
        装备信息                        byte[]
            循环8次
                装备数量                    byte (0 此位置没有装备 1 有装备)
                装备信息                    ITEM (如果装备数量的字段为0，没有此字段)
        头部装备分数                  int
        身体装备分数                  int
        武器装备分数                  int
        聊天信息                        byte[]
            循环8次
                聊天频道定义              byte
            同乡信息                        string
        冷却信息                        byte[]
            冷却组数量                   byte
            循环N次
                冷却组Id                   byte
                冷却组到期时间         int
        buff信息                      byte[]
            buff数量                      byte
            循环n次
                buffId                  int
                IconId                  int
                到期时间                    int

6   EQUIP
        装备部位                        byte
         装备数量                   byte (0 次位置没有装备 1 有装备)
         装备信息                   ITEM (如果装备数量的字段为0，没有此字段)
7   物品数量变化
    同步
        GRID
    显示
        物品Id                        int
        物品instanceId                int
        物品数量                        byte
8   BUFFS
        数量                          byte
        循环n次
            BuffId                  int
            buffIconId              int
            到期时间                    int(如果是-1，那么代表是永久buff)
*/
