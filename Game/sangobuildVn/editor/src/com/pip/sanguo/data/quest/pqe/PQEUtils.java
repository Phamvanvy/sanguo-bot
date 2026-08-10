package com.pip.sanguo.data.quest.pqe;

import java.util.HashMap;
import java.util.HashSet;

public class PQEUtils {
	/**
	 * 所有比较操作符。
	 */
	public static int[] COMPARE_OPS = new int[] {
		ParserConstants.EQ, ParserConstants.NE, ParserConstants.LT, 
		ParserConstants.LE, ParserConstants.GT, ParserConstants.GE
	};

	/**
	 * 系统变量描述。
	 * @author lighthu
	 */
	public static class SystemVar {
	    public String name;
	    public int dataType;
	    public boolean clientSupport;
	    public String description;
	    
	    public SystemVar(String name, int dataType, boolean client, String desc) {
	        this.name = name;
	        this.dataType = dataType;
	        this.clientSupport = client;
	        this.description = desc;
	        
	        SYSTEM_VARS_MAP.put(name, this);
	    }
	}
	
	/**
	 * 所有系统变量的查找表。
	 */
	public static HashMap<String, SystemVar> SYSTEM_VARS_MAP = new HashMap<String, SystemVar>();
	
	/**
	 * 所有系统变量的数组。
	 */
	public static SystemVar[] SYSTEM_VARS = new SystemVar[] {
	    new SystemVar("_LEVEL", 0, true, "级别"),
	    new SystemVar("_MONEY", 0, true, "金钱"),
	    new SystemVar("_HP", 0, true, "生命"),
        new SystemVar("_MAXHP", 0, true, "最大生命"),
        new SystemVar("_MP", 0, true, "内力"),
        new SystemVar("_MAXMP", 0, true, "最大内力"),
        new SystemVar("_STR", 0, true, "力量"),
        new SystemVar("_STA", 0, true, "耐力"),
        new SystemVar("_AGI", 0, true, "敏捷"),
        new SystemVar("_INT", 0, true, "智力"),
        new SystemVar("_SEX", 0, true, "性别ID"),
        new SystemVar("_SEXNAME", 1, true, "性别"),
        new SystemVar("_CLASS", 0, true, "职业ID"),
        new SystemVar("_CLASSNAME", 1, true, "职业"),
        new SystemVar("_FACTION", 0, false, "阵营ID"),
        new SystemVar("_FACTIONNAME", 1, false, "阵营"),
        new SystemVar("_NAME", 0, true, "名称"),
        new SystemVar("_MAPID", 0, true, "场景ID"),
        new SystemVar("_X", 0, true, "X坐标"),
        new SystemVar("_Y", 0, true, "Y坐标"),
        new SystemVar("_LASTKILLERNPC", 0, false, "上次杀死怪物ID"),
        new SystemVar("_LASTKILLERNPCNAME", 1, false, "上次杀死怪物名称"),
        new SystemVar("_LASTKILLERPLAYER", 0, false, "上次杀死玩家ID"),
        new SystemVar("_LASTKILLERPLAYERNAME", 1, false, "上次杀死玩家名称"),
        new SystemVar("_LASTCHATMESSAGE", 1, true, "上次发送的聊天消息"),
        new SystemVar("_FINISHCHOICE", 0, false, "选择的任务奖励分支"),
        new SystemVar("_KEYBOARDTYPE", 0, false, "按键类型"),
        new SystemVar("_MOUSETYPE", 0, false, "触摸类型"),
        new SystemVar("_MATEID", 0, false, "配偶ID"),
        new SystemVar("_ISKING", 0, false, "是否是国公"),
        new SystemVar("_ISOFFICER", 0, false, "是否是官员"),
        new SystemVar("_HASTONG", 0, false, "是否有军团"),
        new SystemVar("_ACTIVEPOWER", 0, true, "行动力"),
        new SystemVar("_ADDEDPROPERTYPOINT", 0, false, "已分配属性点"),
        new SystemVar("_ADDEDSKILLPOINT", 0, false, "已分配技能点"),
        new SystemVar("_CONTRIBUTEPOINT", 0, true, "贡献度"),
        new SystemVar("_ASSOCIATIONINVITE", 0, false, "是否接受过结义邀请"),
	    new SystemVar("_HAVEASSOCIATION",0,false,"是否拥有血盟")
	};
	
	/**
	 * 系统函数描述。
	 */
	public static class SystemFunc {
        public String name;
        public int dataType;
        public boolean clientSupport;
        public int[] paramType;
        public String description;
        
        public SystemFunc(String name, int dataType, boolean client, int[] paramType, String desc) {
            this.name = name;
            this.dataType = dataType;
            this.clientSupport = client;
            this.paramType = paramType;
            this.description = desc;
            
            SYSTEM_FUNCS_MAP.put(name, this);
        }
    }
	
	/**
     * 所有系统函数的查找表。
     */
    public static HashMap<String, SystemFunc> SYSTEM_FUNCS_MAP = new HashMap<String, SystemFunc>();
    
    /**
     * 所有系统函数的数组。
     */
    public static SystemFunc[] SYSTEM_FUNCS = new SystemFunc[] {
        new SystemFunc("Set", -1, true, new int[] { 1, 0 }, "设置 {0} 的值为 {1}"),
        new SystemFunc("Inc", -1, true, new int[] { 1, 0 }, "{0} 增加  {1}"),
        new SystemFunc("Dec", -1, true, new int[] { 1, 0 }, "{0} 减少 {1}"),
        new SystemFunc("Random", 0, true, new int[] {  }, "随机数"),
        new SystemFunc("If", -1, true, new int[] { 0, -1, -1 }, "如果 {0} 成立，执行 {1}，否则执行 {2}"),
        new SystemFunc("AssignTask", 0, false, new int[] { 0 }, "强制接受任务 {0}"),
        new SystemFunc("EndTask", 0, false, new int[] { 0 }, "选择奖励 {0} 结束任务"),
        new SystemFunc("HasTask", 0, true, new int[] { 0 }, "拥有任务 {0}"),
        new SystemFunc("TaskFinished", 0, false, new int[] { 0 }, "完成过任务 {0}"),
        new SystemFunc("GetReward", 0, false, new int[] { 0 }, "获得任务奖励 {0}"),
        new SystemFunc("CanFinish", 0, true, new int[] {  }, "任务已完成"),
        new SystemFunc("SetFail", -1, false, new int[] { }, "设置任务状态为失败"),
        new SystemFunc("Chat", -1, true, new int[] { 0, 1, 0 }, "{0} 说： {1}"),
        new SystemFunc("Message", -1, true, new int[] { 1, 0, 0 }, "消息：{0}"),
        new SystemFunc("Question", -1, true, new int[] { 1, 1, 0 }, "询问：{0}"),
        new SystemFunc("OpenUI", -1, false, new int[] { 1 }, "打开界面 {0}"),
        new SystemFunc("Flash", -1, true, new int[] { 0 }, "屏幕闪烁"),
        new SystemFunc("RefreshNPC", -1, false, new int[] { 0, 0, 1 }, "刷新NPC {0}"),
        new SystemFunc("RefreshNPCAt", -1, false, new int[] { 0, 0, 1 }, "在指定位置刷新NPC {0}"),
        new SystemFunc("RemoveNPC", -1, false, new int[] { 0 }, "移除NPC {0}"),
        new SystemFunc("RemoveNPCByID", -1, false, new int[] { 0 }, "移除NPC {0}"),
        new SystemFunc("GotoMap", -1, false, new int[] { 0, 0, 0 }, "传送到 {0}:{1},{2}"),
        new SystemFunc("Logout", -1, true, new int[] {  }, "强制退出"),
        new SystemFunc("MoveNPC", -1, false, new int[] { 0, 0, 0, 0 }, "移动NPC {0} 到 {1}:{2},{3}"),
        new SystemFunc("FindNPC", 0, false, new int[] { 0, 0 }, "NPC {0} 在 {1} 像素内"),
        new SystemFunc("FindNPCByType", 0, false, new int[] { 0, 0, 1 }, "{0} 类型的NPC在玩家周围 {1} 像素内"),
        new SystemFunc("FindNPCAt", 0, false, new int[] { 0, 0, 0, 0, 0 }, "NPC {0} 在 {1}:{2},{3} 附近 {4} 像素内"),
        new SystemFunc("FindPlayer", 0, false, new int[] { 0, 0 }, "玩家 {0} 在 {1} 像素内"),
        new SystemFunc("GetItemCount", 0, true, new int[] { 0 }, "物品 {0} 的数量"),
        new SystemFunc("HasItem", 0, true, new int[] { 0, 0 }, "拥有 {1} 个 物品 {0}"),
        new SystemFunc("AddItem", 0, false, new int[] { 0, 0 }, "添加 {1} 个物品 {0}"),
        new SystemFunc("RemoveItem", 0, false, new int[] { 0, 0 }, "删除 {1} 个物品 {0}"),
        new SystemFunc("SavePosition", 0, false, new int[] { 1 }, "保存玩家当前位置"),
        new SystemFunc("IsChannel", 0, false, new int[] { 1 }, "来自{0}渠道"),
        new SystemFunc("DecActivePower", 0, false, new int[] { 0 }, "扣除{0}行动力"),
        new SystemFunc("DecContributePoint", 0, false, new int[] { 0 }, "扣除{0}贡献度"),
        new SystemFunc("HasTitle", 0, false, new int[] { 0 }, "拥有称号{0}"),
        new SystemFunc("E_Approach", 0, true, new int[] { 0, 0, 0 }, "检测到玩家接近 {0}:{1},{2}"),
        new SystemFunc("E_EnterMap", 0, true, new int[] { 0 }, "检测到玩家进入场景 {0}"),
        new SystemFunc("E_Kill", 0, false, new int[] { 0, 1, 0 }, "检测到玩家杀死怪物 {0} "),
        new SystemFunc("E_KillWithMate", 0, false, new int[] { 0, 1, 0 }, "检测到玩家和配偶一起杀死怪物 {0} "),
        new SystemFunc("E_KillPlayer", 0, false, new int[] { 0 }, "检测到玩家杀死玩家 {0}"),
        new SystemFunc("E_UseSkill", 0, false, new int[] { 0 }, "检测到玩家使用技能 {0}"),
        new SystemFunc("E_TouchNPC", 0, true, new int[] { 0 }, "检测到玩家和NPC {0} 对话"),
        new SystemFunc("E_Killed", 0, false, new int[] {  }, "检测到玩家被怪物杀死"),
        new SystemFunc("E_KilledByPlayer", 0, false, new int[] {  }, "检测到玩家被其他玩家杀死"),
        new SystemFunc("E_UseItem", 0, false, new int[] { 0 }, "检测到玩家使用物品 {0}"),
        new SystemFunc("E_Chat", 0, false, new int[] {  }, "检测到玩家发送聊天消息"),
        new SystemFunc("E_OpenUI", 0, true, new int[] { 1 }, "检测到玩家打开界面 {0}"),
        new SystemFunc("E_AnswerQuestion", 0, true, new int[] { 0, 0 }, "检测到玩家选择 {1}"),
        new SystemFunc("E_CloseChat", 0, true, new int[] { 0 }, "检测到玩家关闭对话 {0}"),
        new SystemFunc("E_CloseMessage", 0, true, new int[] { 0 }, "检测到玩家关闭消息 {0}"),
        new SystemFunc("E_TaskFinish", 0, false, new int[] {  }, "检测到任务结束"),
        new SystemFunc("E_HasAction", 0, false, new int[] { 0 }, "检测玩家做过动作 {0}"),
        new SystemFunc("E_IsInTongBattle", 0, false, new int[] {  }, "检测到玩家正在进行攻城战"),
        new SystemFunc("E_IsInNationBattle", 0, false, new int[] {  }, "检测到玩家正在进行国战"),
        new SystemFunc("E_IsInFlagBattle", 0, false, new int[] {  }, "检测到玩家正在进行战场"),
        new SystemFunc("E_KillPlayers", 0, false, new int[] {  }, "检测到玩家杀死敌人"),
        new SystemFunc("E_ApprochPosition", 0, true, new int[] { 0, 0, 0, 0 }, "检测到玩家接近 {0}:{1},{2} {3}码"),
        new SystemFunc("NpcShout", 0, false, new int[] { 0, 1, 0 }, "npc喊话{0},{1},{2}秒"),
        new SystemFunc("AddBuff", 0, false, new int[] { 0, 0 }, "添加BUFF{0}"),
        new SystemFunc("InjoinAssociation", -1, true, new int[] {  }, "加入结义血盟"),
        new SystemFunc("E_KillWithAssociation", 0, false, new int[] { 0, 1, 0 }, "检测到玩家和盟主一起杀死怪物 {0} "),
        new SystemFunc("Bubble", 0, false, new int[] { 0, 1 ,0}, "{0}弹出气泡\"{1}\"{2}秒"),
    };
    
    // 事件掩码常量定义
    
    /** 游戏循环事件，每个CYCLE这个事件都会触发。*/
    public static final int EVENT_MASK_CYCLE = 1;
    /** 用户位置改变事件。*/
    public static final int EVENT_MASK_POSITION = 1 << 1;
    /** 用户和NPC对话事件。*/
    public static final int EVENT_MASK_TOUCHNPC = 1 << 2;
    /** 用户死亡事件。*/
    public static final int EVENT_MASK_DIE = 1 << 3;
    /** 用户发送聊天消息事件。*/
    public static final int EVENT_MASK_CHAT = 1 << 4;
    /** 用户打开界面事件。*/
    public static final int EVENT_MASK_OPENUI = 1 << 5;
    /** 用户关闭任务中触发的对话（包括对话、消息或提问）事件。*/
    public static final int EVENT_MASK_CLOSECHAT = 1 << 6;
    
    /**
     * 把字符串常量解释为Java字符串。
     */
    public static String translateStringConstant(String str) {
        // 字符串常量必然开头和结尾都是"
        StringBuffer buf = new StringBuffer();
        char[] data = str.toCharArray();
        for (int i = 1; i < data.length - 1; i++) {
            char ch = data[i];
            if (ch == '\\') {
                switch (data[i + 1]) {
                case 'n':
                    buf.append("\n");
                    break;
                case 'r':
                    buf.append("\r");
                    break;
                case 't':
                    buf.append("\t");
                    break;
                default:
                    buf.append(data[i + 1]);
                    break;
                }
                i++;
            } else {
                buf.append(ch);
            }
        }
        return buf.toString();
    }

    /**
     * 解释整型常量。
     */
    public static int translateNumberConstant(String str) {
        if (str.startsWith("0x") || str.startsWith("0X")) {
        	// 16进制
        	str = str.substring(2);
        	if (str.length() < 8) {
        		return Integer.parseInt(str, 16);
        	} else {
        		int low = Integer.parseInt(str.substring(1), 16);
        		int high = Integer.parseInt(str.substring(0, 1), 16);
        		return (high << 28) | low;
        	}
        } else {
            return Integer.parseInt(str);
        }
    }

    /**
     * 把Java字符串转换为表达式中的格式。
     */
    public static String reverseConv(String msg) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < msg.length(); i++) {
            switch (msg.charAt(i)) {
            case '\n':
                buf.append("\\n");
                break;
            case '\r':
                buf.append("\\r");
                break;
            case '\t':
                buf.append("\\t");
                break;
            case '"':
                buf.append("\\\"");
                break;
            case '\\':
                buf.append("\\\\");
                break;
            default:
                buf.append(msg.charAt(i));
                break;
            }
        }
        return buf.toString();
    }

    /**
     * 得到操作符的字符串表现。
     * @param op
     * @return
     */
    public static String op2str(int op) {
        switch (op) {
        case ParserConstants.EQ:
        	return "==";
        case ParserConstants.NE:
        	return "!=";
        case ParserConstants.LT:
        	return "<";
        case ParserConstants.LE:
        	return "<=";
        case ParserConstants.GT:
        	return ">";
        case ParserConstants.GE:
        	return ">=";
        default:
        	return "";
        }
    }
    
    /**
     * 得到操作符的名称。
     */
    public static String op2nstr(int op) {
        switch (op) {
        case ParserConstants.EQ:
        	return "等于";
        case ParserConstants.NE:
        	return "不等于";
        case ParserConstants.LT:
        	return "小于";
        case ParserConstants.LE:
        	return "跌至";
        case ParserConstants.GT:
        	return "大于";
        case ParserConstants.GE:
        	return "达到";
        default:
        	return "";
        }
    }
    
    /**
     * 检查一个混合格式文本中的变量引用格式是否正确。
     * @param str 字符串
     * @param forServer 这个字符串是否是Server端解释。客户端解释的文本格式要求要严格得多。
     * @throws PQEException
     */
    public static void checkRichTextSyntax(String str, String[] localVars, boolean forServer) throws PQEException {
        char[] arr = str.toCharArray();
        int count = arr.length;
        int state = 0;
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < count; i++) {
            char ch = arr[i];
            if (state == 0) {
                if (ch == '$' && i < count - 1 && arr[i + 1] == '{') {
                    i++;
                    state = 1;
                    buf.setLength(0);
                }
            } else if (state == 1) {
                if (ch == '}') {
                    // 变量引用部分结束，检查格式
                    String exprStr = buf.toString();
                    ExpressionList exprList = ExpressionList.fromString(exprStr);
                    if (exprList == null || exprList.getExprCount() > 1) {
                        throw new PQEException("表达式格式错误：" + exprStr);
                    }
                    exprList.checkSyntax(localVars, false);
                    
                    if (!forServer) {
                        // 如果是客户端解释的，还需要额外检查，只允许访问客户端支持的系统变量、局部变量以及GetItemCount函数。
                        Expression expr = exprList.getExpr(0);
                        if (expr.getRightExpr() != null) {
                            throw new PQEException("客户端字符串不支持变量运算：" + exprStr);
                        }
                        Expr0 expr0 = expr.getLeftExpr();
                        if (expr0.type == Expr0.TYPE_IDENTIFIER) {
                            String varName = expr0.value;
                            if (varName.startsWith("_")) {
                                SystemVar sysVar = SYSTEM_VARS_MAP.get(varName);
                                if (sysVar == null || !sysVar.clientSupport) {
                                    throw new PQEException("客户端不支持变量：" + varName);
                                }
                            } else {
                                boolean found = false;
                                for (String lv : localVars) {
                                    if (lv.equals(varName)) {
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found) {
                                    throw new PQEException("客户端不支持变量：" + varName);
                                }
                            }
                        } else if (expr0.type == Expr0.TYPE_FUNC) {
                            FunctionCall fc = expr0.getFunctionCall();
                            if (!fc.funcName.equals("GetItemCount")) {
                                throw new PQEException("客户端字符串只支持GetItemCount函数：" + exprStr);
                            }
                            if (fc.getParam(0).getRightExpr() != null || fc.getParam(0).getLeftExpr().type != Expr0.TYPE_NUMBER) {
                                throw new PQEException("GetItemCount函数必须使用一个数字常量作为参数：" + exprStr);   
                            }
                        } else {
                            throw new PQEException("既然是常量，何必写成变量引用呢？？？？" + exprStr);
                        }
                    }
                    
                    state = 0;
                } else {
                    buf.append(ch);
                }
            }
        }
        if (state == 1) {
            throw new PQEException("未结束的公式引用");
        }
    }

    /**
     * 把一个混合格式文本中所有用到的局部变量名替换为变量索引。
     * @param str 字符串
     */
    public static String convertRichText(String str, String[] localVars) {
        // 构建变量替换表
        HashMap<String, String> varMap = new HashMap<String, String>();
        for (int i = 0; i < localVars.length; i++) {
            varMap.put(localVars[i], "v" + i);
        }
        
        // 扫描字符串查找变量名
        char[] arr = str.toCharArray();
        int count = arr.length;
        int state = 0;
        StringBuffer buf = new StringBuffer();
        StringBuffer retBuf = new StringBuffer();
        for (int i = 0; i < count; i++) {
            char ch = arr[i];
            if (state == 0) {
                if (ch == '$' && i < count - 1 && arr[i + 1] == '{') {
                    i++;
                    state = 1;
                    buf.setLength(0);
                } else {
                    retBuf.append(ch);
                }
            } else if (state == 1) {
                if (ch == '}') {
                    // 变量引用部分结束，替换
                    String exprStr = buf.toString().trim();
                    if (varMap.containsKey(exprStr)) {
                        retBuf.append("${" + varMap.get(exprStr) + "}");
                    } else {
                        retBuf.append("${" + exprStr + "}");
                    }
                    state = 0;
                } else {
                    buf.append(ch);
                }
            }
        }
        if (state == 1) {
            retBuf.append("${");
            retBuf.append(buf.toString());
        }
        return retBuf.toString();
    }
}
