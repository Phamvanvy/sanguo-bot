package com.pip.sanguo.editor.ai;

public class AITargetType {
    /**
     * 目标类型
     * 0 - 第一仇恨，1 - 第二仇恨，2 - 第三仇恨...以此类推
     * -1 - 仇恨表中随机目标，-2 仇恨表中除第一仇恨外的随机目标
     * -3 - 所有武将，-4 - 所有刺客，-5 - 所有谋士，-6 - 所有方士
     * -7 - 自己
     */
    public int targetType = 0;
    
    public static final int[] TYPE_MAP = { 0, 1, 2, 3, -1, -2, -3, -4, -5, -6, -7 };
    public static final String[] TYPE_NAMES = {
        "第一仇恨目标", "第二仇恨目标", "第三仇恨目标", "第四仇恨目标", 
        "随机目标", "除第一仇恨外的随机目标",
        "所有武将", "所有刺客", "所有谋士", "所有方士",
        "自己"
    };
    
    public static int type2index(int t) {
        for (int i = 0; i < TYPE_MAP.length; i++) {
            if (t == TYPE_MAP[i]) {
                return i;
            }
        }
        return 0;
    }
    
    public String getDisplayString() {
        if (targetType > 0) {
            return "第" + (targetType + 1) + "仇恨目标";
        }
        switch (targetType) {
        case 0:
            return "第一仇恨目标";
        case -1:
            return "随机目标";
        case -2:
            return "除第一仇恨外的随机目标";
        case -3:
            return "所有武将";
        case -4:
            return "所有刺客";
        case -5:
            return "所有谋士";
        case -6:
            return "所有方士";
        case -7:
            return "自己";
        }
        return "错误目标";
    }
    
    public String toString() {
        return String.valueOf(targetType);
    }
    
    public void parse(String str) {
        try {
            targetType = Integer.parseInt(str);
        } catch (Exception e) {
        }
    }
}
