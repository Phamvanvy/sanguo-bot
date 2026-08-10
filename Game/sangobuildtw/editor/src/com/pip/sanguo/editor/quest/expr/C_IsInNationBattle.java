package com.pip.sanguo.editor.quest.expr;

import com.pip.sanguo.data.quest.QuestInfo;

public class C_IsInNationBattle extends AbstractFunctionCheck3 {

    /**
     * 构造指定全局变量的模板。
     * @param name 全局变量名称
     */
    public C_IsInNationBattle() {
        super("E_IsInNationBattle");
    }
    
    /**
     * 用模板创建新的表达式片段。
     */
    public IExpr createNew(QuestInfo qinfo) {
        return new C_IsInNationBattle();
    }

    /**
     * 取得模板名称。
     */
    public String getName() {
        return "玩家正在进行国战";
    }

    /**
     * 转换为自然语言表示。
     */
    public String toNatureString() {
        return "玩家正在进行国战";
    }

}
