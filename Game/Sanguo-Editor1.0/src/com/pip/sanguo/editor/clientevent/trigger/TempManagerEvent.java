package com.pip.sanguo.editor.clientevent.trigger;


/**
 * 管理任务设计中用到的表达式模板。
 * @author zhangbaiquan
 */
public class TempManagerEvent {
	/**
     * 模板选择树的显示。
     */
	public static final String[] TEMPLATES_NAMES = {
        "和配偶一起杀死怪物事件",
        "玩家属性检查",
        "玩家得到某个物品",
        "是否完成某个任务",
        "正在进行某个任务",
        "靠近NPC",
        "进入场景",
        "玩家杀死怪物",
        "玩家被怪物杀死",
        "玩家杀死其他玩家",
        "玩家被其他玩家杀死",
        "完成某个引导事件",
        "玩家职业",
        "未完成某引导事件",
        "玩家有某个装备但未装配",
        "玩家阵营(国家)",
        "提交了某个已完成任务",
        "打开某个NPC对话",
        "玩家学习了某个技能",
        "玩家是否是未改名游客"
	};
	
	/**
	 * 模板选择树的数据
	 * @param index    模板的索引
	 * @return
	 */
	public static IExprEvent getExprEvent(int index){
	    switch(index){
	        case 0:
	            return (new G_KillWithMate(null));         //和配偶一起杀死怪物事件
	        case 1:
	            return (new G_CheckRoleAttribute(null));   //玩家属性检查
	        case 2:
	            return (new G_GetItem(null));              //玩家得到某个物品
	        case 3:
                return (new G_FinisnQuest(null));           //完成某个任务
	        case 4:
                return (new G_DoingQuest(null));            //正在进行某个任务
	        case 5:
	            return (new G_CloseToNPC(null));            //靠近NPC
	        case 6:
                return (new M_EnterScene(null));            //进入场景
	        case 7:
                return (new M_KILLMonsterID(null));         //玩家杀死怪物
	        case 8:
                return (new M_MonsterKillPlayer(null));     //玩家被怪物杀死
	        case 9:
                return (new M_PlayerKillOTHEN(null));       //玩家杀死其他玩家
	        case 10:
                return (new M_OtherKillPlayer(null));       //玩家被其他玩家杀死
	        case 11:
                return (new G_FinisnClientEvent(null));     //完成某个引导事件
	        case 12:
                return (new G_RoleJob(null));               //玩家职业
	        case 13:
                return (new G_NoFinisnClientEvent(null));   //未完成某个引导事件
	        case 14:
                return (new G_NotEquipItem(null));          //玩家有某个装备但未装配
	        case 15:
                return (new G_RoleCamp(null));              //玩家阵营(国家)选择
	        case 16:
                return (new G_SubmitFinisnQuest(null));     //提交了某个已完成任务
	        case 17:
                return (new G_OpenQuestGruide(null));       //打开某个NPC对话
	        case 18:
                return (new G_StudySkill(null));            //玩家学习了某个技能
	        case 19:
                return (new G_IsGuideRole(null));            //玩家是否是未改名的游客
	    }
	    return null;
	}
	
	public static final String[] TEMPLATES_NAMES1 = {
        "是否完成某个任务",
        "正在进行某个任务",
        "靠近NPC",
        "进入场景",
        "玩家杀死怪物",
        "玩家被怪物杀死",
        "玩家杀死其他玩家",
        "玩家被其他玩家杀死",
        "未完成某引导事件",
        "提交了某个已完成任务",
        "打开某个NPC对话",
        "玩家是国公",
        "玩家有配偶",
        "玩家有血盟",
        "玩家有师徒关系",
        "国家开启宣战",
        "国家开启反击战",
        "玩家得到某个物品",
        "玩家学习了某个技能",
        "玩家是否是未改名游客"
    };
	
	public static IExprEvent getExprEvent1(int index){
	    switch(index){
	        case 0:
	            return (new G_FinisnQuest(null));           //完成某个任务
	        case 1:
	            return (new G_DoingQuest(null));            //正在进行某个任务
	        case 2:
	            return (new G_CloseToNPC(null));            //靠近NPC
	        case 3:
	            return (new M_EnterScene(null));            //进入场景
	        case 4:
	            return (new M_KILLMonsterID(null));         //玩家杀死怪物
	        case 5:
	            return (new M_MonsterKillPlayer(null));     //玩家被怪物杀死
	        case 6:
	            return (new M_PlayerKillOTHEN(null));       //玩家杀死其他玩家
	        case 7:
	            return (new M_OtherKillPlayer(null));       //玩家被其他玩家杀死
	        case 8:
	            return (new G_NoFinisnClientEvent(null));   //未完成某个引导事件
	        case 9:
	            return (new G_SubmitFinisnQuest(null));     //提交了某个已完成任务
	        case 10:
	            return (new G_OpenQuestGruide(null));       //打开某个NPC对话
	        case 11:
	            return (new G_IsKing(null));               //玩家是国公
	        case 12:
	            return (new G_IsMates(null));               //玩家有配偶
	        case 13:
	            return (new G_IsAssociationMemeber(null));  //玩家有血盟
	        case 14:
	            return (new G_IsApprenticeMemeber(null));  //玩家有师徒关系
	        case 15:
	            return (new G_NationBattleOpen(null));  //玩家国家开启宣战
	        case 16:
	            return (new G_NationDeclarBattleOpen(null));  //玩家国家开启反击战
	        case 17:
                return (new G_GetItem(null));              //玩家得到某个物品
	        case 18:
                return (new G_StudySkill(null));            //玩家学习了某个技能
	        case 19:
                return (new G_IsGuideRole(null));            //玩家是否是未改名的游客
	    }
	    return null;
	}
	
	/**
     * 得到一个方法字符串中的参数。
     */
	public static String[] getParameter(String str){
	    int startIndex = str.indexOf("(");
	    int endIndex = str.indexOf(")");
	    String parameter = str.substring(startIndex+1, endIndex);
	    String[] parames = parameter.split(",");
	    return parames;
	}
	
	/**
     * 拼接一个数组参数。
     */
	public static String getParameterStr(String[] params){
	    StringBuffer sb = new StringBuffer();
	    int len = params.length;
	    for(int i = 0 ; i < len ; i++){
	        sb.append(params[i]);
	        if(i < len - 1){
	            sb.append(",");
	        }
	    }
        return sb.toString();
    }
	
	/**
	 * ，拆分字符串
	 */
	public static String[] getSplit(String str, int length){
        String[] splits = new String[length];
        int startIndex = 0;
        for(int i = 0; i < length; i++){
            int endIndex = str.indexOf(",", startIndex);
            if(endIndex < 0){
                splits[i] = str.substring(startIndex,str.length());
            }else{
                splits[i] = str.substring(startIndex,endIndex);
            }
            startIndex = endIndex+1;
        }
        return splits;
    }
}
