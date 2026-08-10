package com.pip.sanguo.editor.quest.expr;

import com.pip.sanguo.data.quest.QuestInfo;

public class C_IsInFlagBattle extends AbstractFunctionCheck3 {

    /**
     * 构造指定全局变量的模板。
     * @param name 全局变量名称
     */
    public C_IsInFlagBattle() {
        super("E_IsInFlagBattle");
    }
    
    /**
     * 用模板创建新的表达式片段。
     */
    public IExpr createNew(QuestInfo qinfo) {
        return new C_IsInFlagBattle();
    }

    /**
     * 取得模板名称。
     */
    public String getName() {
        return "玩家正在进行战场";
    }

    /**
     * 转换为自然语言表示。
     */
    public String toNatureString() {
        return "玩家正在进行战场";
    }

}
