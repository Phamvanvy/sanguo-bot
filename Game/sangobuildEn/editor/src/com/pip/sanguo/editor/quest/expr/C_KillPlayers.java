package com.pip.sanguo.editor.quest.expr;

import com.pip.sanguo.data.quest.QuestInfo;

public class C_KillPlayers extends AbstractFunctionCheck3 {

    /**
     * 构造指定全局变量的模板。
     * @param name 全局变量名称
     */
    public C_KillPlayers() {
        super("E_KillPlayers");
    }
    
    /**
     * 用模板创建新的表达式片段。
     */
    public IExpr createNew(QuestInfo qinfo) {
        return new C_KillPlayers();
    }

    /**
     * 取得模板名称。
     */
    public String getName() {
        return "杀死玩家";
    }

    /**
     * 转换为自然语言表示。
     */
    public String toNatureString() {
        return "杀死玩家 ";
    }

}
