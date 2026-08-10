package com.pip.sanguo.editor.quest.expr;

import java.util.*;

import com.pip.sanguo.data.quest.QuestInfo;
import com.pip.sanguo.data.quest.QuestVariable;
import com.pip.sanguo.data.quest.pqe.Expression;
import com.pip.sanguo.data.quest.pqe.PQEUtils;
import com.pip.sanguo.data.quest.pqe.ParserConstants;

/**
 * 管理任务设计中用到的表达式模板。
 * @author lighthu
 */
public class TemplateManager {
	/**
	 * 模板选择树的数据。
	 */
	public static final String[] TEMPLATE_TYPES = { "属性检查", "事件检测", "常用动作", "自定义" };
	public static final IExpr[][] TEMPLATES = {
		{
			new C_GlobalVar("_LEVEL", ParserConstants.GE, 10), 		// 级别达到...
			new C_GlobalVar("_MONEY", ParserConstants.GE, 10000), 	// 金钱达到...
			new C_GlobalVar("_IMONEY", ParserConstants.GE, 100), 	// 元宝达到...
			new C_GlobalVar("_HP", ParserConstants.LT, 10), 		// HP小于...
			new C_GlobalVar("_MAXHP", ParserConstants.GE, 100),		// 最大HP达到...
			new C_GlobalVar("_MP", ParserConstants.LT, 10), 		// MP小于...
			new C_GlobalVar("_MAXMP", ParserConstants.GE, 100),		// 最大MP达到...
			new C_GlobalVar("_STR", ParserConstants.GE, 100),		// 力量达到...
			new C_GlobalVar("_STA", ParserConstants.GE, 100),		// 体力达到...
			new C_GlobalVar("_AGI", ParserConstants.GE, 100),		// 敏捷达到...
			new C_GlobalVar("_INT", ParserConstants.GE, 100),		// 智力达到...
            new C_GlobalVar("_CLASS", ParserConstants.EQ, 0),       // 职业等于...
            new C_GlobalVar("_FACTION", ParserConstants.EQ, 0),     // 阵营等于...
			new C_Sex(),											// 性别...
			new C_GlobalVar("_FINISHCHOICE", ParserConstants.EQ, 0),// 选择分支奖励...
			new C_CanFinish(),										// 任务目标达成
			new C_HasTask(),										// 是否已接受某任务
			new C_TaskFinished(),									// 是否完成过某任务
			new C_HitRate(),										// 随机数判断 
			new C_HasItem(),										// 是否有某物品
			new C_True(),                                           // 恒真条件
            new C_GlobalVar("_KEYBOARDTYPE", ParserConstants.EQ, 0),// 按键类型等于...
            new C_GlobalVar("_MOUSETYPE", ParserConstants.EQ, 0),   // 触摸类型等于...
            new C_GlobalVar("_MATEID", ParserConstants.NE, -1),     // 配偶ID...
            new C_GlobalVar("_ISKING", ParserConstants.EQ, 1),      // 是国王
            new C_IsChannel(),                                      // 判断渠道
            new C_GlobalVar("_ISOFFICER", ParserConstants.EQ, 1),   // 是官员
            new C_GlobalVar("_HASTONG", ParserConstants.EQ, 1),   // 有军团
            new C_GlobalVar("_ACTIVEPOWER", ParserConstants.GE, 1),   // 行动力
            new C_GlobalVar("_ADDEDPROPERTYPOINT", ParserConstants.GE, 1),   // 已分配属性点
            new C_GlobalVar("_ADDEDSKILLPOINT", ParserConstants.GE, 1),   // 已分配技能点
            new C_HasTitle(),                                       // 玩家拥有称号
            new C_IsCity(),                                         // 玩家来自某城市
            new C_GlobalVar("_CONTRIBUTEPOINT", ParserConstants.GE, 1),     //贡献度
            new C_GlobalVar("_ASSOCIATIONINVITE", ParserConstants.EQ, 1),      // 接受过结义邀请
            new C_GlobalVar("_HAVEASSOCIATION", ParserConstants.EQ, 1),      // 拥有血盟
            new C_GlobalVar("_FOLLOWATTENDANT", ParserConstants.EQ, 1),      // 携带随从
		},
		{
			new C_Approach(),				                        // 到达某点事件
			new C_Chat(),											// 发送聊天事件
			new C_EnterMap(),										// 进入场景事件
			new C_FindNPC(null),									// 周围有某NPC
			new C_FindNPCByType(null),                              // 周围有某类型的NPC
			new C_FindNPCAt(null),                                  // NPC在某点附近
			new C_FindPlayer(),										// 周围有某玩家
			new C_TaskFinish(),										// 任务结束事件
			new C_Kill(null),										// 杀死怪物事件
            new C_KillWithMate(null),                               // 和配偶一起杀死怪物事件
			new C_Killed(),											// 被怪物杀死事件
			new C_KilledByPlayer(),									// 被玩家杀死事件
			new C_KillPlayer(),										// 杀死玩家事件
			new C_OpenUI(),											// 打开界面事件
			new C_TouchNPC(),										// 和NPC对话事件
			new C_UseItem(),										// 使用物品事件
			new C_UseSkill(),										// 使用技能事件
			new C_HasAction(),                                      //做过动作
			new C_IsInTongBattle(),                                 //玩家进行城战
			new C_IsInNationBattle(),                               //玩家进行国战
			new C_IsInFlagBattle(),                                 //玩家进行战场
			new C_KillPlayers(),                                    //玩家杀死敌人数
			new C_ApprochPosition(),                                //玩家接近某位置多少码
			new C_KillWithAssociation(null),                        // 和盟主一起杀死怪物事件
			new C_KillWithTeacher(null),                            //师徒一起杀死怪物事件
			new C_FirstApproach(),                                   //第一次接近某NPC
			new C_KillPlayersLimited(), 
			new C_AchievePearls(null),                               //玩家达成连珠
		},
		{
			new A_AddItem(),										// 添加物品
			new A_AssignTask(),										// 强制指派任务
			new A_Chat(),											// 显示对话
			new A_Dec(null),										// 减少变量值
			new A_EndTask(null),									// 强制交任务
			new A_Flash(),											// 屏幕闪烁
			new A_GetReward(null),									// 获得任务奖励
			new A_GotoMap(),										// 传送
			new A_Inc(null),										// 增加变量值
			new A_Logout(),											// 强制下线
			new A_Message(),										// 显示消息
			new A_MoveNPC(),										// 传送NPC
			new A_OpenUI(),											// 打开界面
			new A_Question(),										// 显示提问
			new A_RefreshNPC(null),									// 强制刷新NPC
			new A_RefreshNPCAt(null),                               // 在某NPC的位置强制刷新NPC
			new A_RemoveItem(),										// 删除物品
			new A_Set(null),										// 设置变量值
			new A_SetFail(),                                        // 设置任务状态为失败
			new A_RemoveNPC(null),                                  // 移除NPC
			new A_RemoveNPCByID(null),                              // 移除NPC
            new A_SavePosition(),                                   // 保存玩家位置
            new A_DecActivePower(),                                 // 扣除行动力
            new A_NpcShout(),                                       // Npc喊话
            new A_DecContributePoint(),                             // 扣除贡献度
            new A_AddBuff(),                                        //添加BUFF
            new A_InjoinAssociation(),                              // 加入结义血盟
            new A_Bubble(),                                          // 弹出气泡
            new A_Shout(),                                          // 狮子吼喊话
		},
		{
			new C_Custom(),
			new A_Custom()
		}
	};
	
	/**
	 * 所有已知的模板。
	 */
	public static List<IExpr> knownTemplates = new ArrayList<IExpr>();
	static {
        knownTemplates.add(new C_Sex());
		for (PQEUtils.SystemVar fn : PQEUtils.SYSTEM_VARS) {
			knownTemplates.add(new C_GlobalVar(fn.name, ParserConstants.EQ, 0));
		}
		knownTemplates.add(new A_Empty());
        knownTemplates.add(new C_True());
        knownTemplates.add(new C_CloseChat());
        knownTemplates.add(new C_CloseMessage());
        knownTemplates.add(new C_AnswerQuestion());
		for (int i = 0; i < TEMPLATES.length; i++) {
			for (IExpr expr : TEMPLATES[i]) {
				if (expr instanceof C_GlobalVar) {
					continue;
				}
				if (expr instanceof C_Custom || expr instanceof A_Custom) {
					continue;
				}
				knownTemplates.add(expr);
			}
		}
	}
	
	/**
	 * 识别一个表达式对象是否可以用某个模板来表示。
	 * @param expr
	 * @param qinfo
	 * @return
	 */
	public static IExpr recognize(Expression expr, QuestInfo qinfo) {
		for (IExpr t : knownTemplates) {
			IExpr ret = t.recognize(qinfo, expr);
			if (ret != null) {
				return ret;
			}
		}
		for (QuestVariable localVar : qinfo.variables) {
			IExpr t = new C_LocalVar(localVar.name);
			IExpr ret = t.recognize(qinfo, expr);
			if (ret != null) {
				return ret;
			}
		}
		IExpr ret = new C_Custom().recognize(qinfo, expr);
		if (ret != null) {
			return ret;
		} else {
			return new A_Custom().recognize(qinfo, expr);
		}
	}
}
