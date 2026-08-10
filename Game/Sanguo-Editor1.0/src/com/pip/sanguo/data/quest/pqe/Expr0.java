package com.pip.sanguo.data.quest.pqe;

import java.util.HashMap;

import com.pip.sanguo.data.ProjectData;
import com.pip.sanguo.data.quest.Quest;
import com.pip.sanguo.data.quest.pqe.PQEUtils.SystemFunc;

public class Expr0 extends SimpleNode {
    public static final int TYPE_NUMBER = 0;
    public static final int TYPE_STRING = 1;
    public static final int TYPE_IDENTIFIER = 2;
    public static final int TYPE_FUNC = 3;

    /**
     * 类型。
     */
    public int type;
    /**
     * 常量/变量值。
     */
    public String value;

    public Expr0(int id) {
        super(id);
    }

    public Expr0(Parser p, int id) {
        super(p, id);
    }

    /**
     * 转换为原始字符串表示。
     */
    public String toString() {
        switch (type) {
        case TYPE_NUMBER:
        case TYPE_STRING:
        case TYPE_IDENTIFIER:
            return value;
        case TYPE_FUNC:
            return jjtGetChild(0).toString();
        default:
            return "";
        }
    }

    /**
     * 转换为自然语言表示。
     */
    public String toNatureString() {
        switch (type) {
        case TYPE_NUMBER:
        case TYPE_STRING:
        	return value;
        case TYPE_IDENTIFIER:
        	if (PQEUtils.SYSTEM_VARS_MAP.containsKey(value)) {
        		return PQEUtils.SYSTEM_VARS_MAP.get(value).description;
        	}
            return value;
        case TYPE_FUNC:
            return getFunctionCall().toNatureString();
        default:
            return "";
        }
    }
    
    /**
     * 取得类型。
     */
    public int getType() {
        return type;
    }
    
    /**
     * 设置类型。
     */
    public void setType(int t) {
        type = t;
    }
    
    /**
     * 取得值。
     */
    public String getValue() {
        return value;
    }
    
    /**
     * 设置值。
     */
    public void setValue(String s) {
        value = s;
    }
    
    /**
     * 取得函数调用。
     */
    public FunctionCall getFunctionCall() {
        return (FunctionCall)jjtGetChild(0);
    }
    
    /**
     * 设置函数调用。
     */
    public void setFunctionCall(FunctionCall fc) {
        fc.parent = this;
        children = new Node[] { fc };
    }
 
    /**
     * 检查一个表达式是否存在语法错误或不可识别的变量、函数，函数的参数数量是否正确。
     * @return 返回表达式数据类型
     * @exception 如果表达式无法编译，抛出异常，包含错误信息
     */
    public int checkSyntax(String[] localVars) throws PQEException {
        if (type == TYPE_IDENTIFIER) {
            // $开头的是其他任务变量
            if (value.startsWith("$")) {
                int pos = value.indexOf('.');
                if (pos == -1) {
                    throw new PQEException("变量格式错误：" + value);
                }
                try {
                    Integer.parseInt(value.substring(1, pos));
                } catch (Exception e) {
                    throw new PQEException("变量格式错误：" + value);
                }
                return 0;
            }
            
            return checkVarName(value, localVars);
        } else if (type == TYPE_FUNC) {
            // 检查函数是否有效
            FunctionCall fc = getFunctionCall();
            PQEUtils.SystemFunc func = PQEUtils.SYSTEM_FUNCS_MAP.get(fc.funcName);
            if (func == null) {
                throw new PQEException("使用了未定义的函数" + fc.funcName);
            }
            
            // 检查函数参数个数
            if (fc.getParamCount() != func.paramType.length) {
                throw new PQEException("参数数量不正确" + fc.toString());
            }
            
            // 检查参数类型
            for (int i = 0; i < func.paramType.length; i++) {
                int needType = func.paramType[i];
                int realType = fc.getParam(i).checkSyntax(localVars);
                if (needType != -1 && realType != needType) {
                    throw new PQEException("参数类型不正确" + fc.toString());
                }
            }
            
            // Set/Inc/Dec函数特殊处理，第一个参数是变量名，必须是常量; E_Kill, E_KillWithMate函数第二个参数必须是变量名
            if ("Set".equals(fc.funcName) || "Inc".equals(fc.funcName) || "Dec".equals(fc.funcName) ) {
                if (fc.getParam(0).getRightExpr() != null || fc.getParam(0).getLeftExpr().type != TYPE_STRING) {
                    throw new PQEException(fc.funcName + "的第一个参数必须是常量");
                }
                String varName = PQEUtils.translateStringConstant(fc.getParam(0).getLeftExpr().value);
                checkVarName(varName, localVars);
            }
            if ("E_Kill".equals(fc.funcName) || "E_KillWithMate".equals(fc.funcName) || "E_KillWithAssociation".equals(fc.funcName) || "E_KillWithTeacher".equals(fc.funcName)||"E_ReachPearls".equals(fc.funcName)) {
                if (fc.getParam(1).getRightExpr() != null || fc.getParam(1).getLeftExpr().type != TYPE_STRING) {
                    throw new PQEException(fc.funcName + "的第二个参数必须是常量");
                }
                String varName = PQEUtils.translateStringConstant(fc.getParam(1).getLeftExpr().value);
                checkVarName(varName, localVars);
            }
            if ("RefreshNPC".equals(fc.funcName) || "FindNPCByType".equals(fc.funcName) || 
                    "RefreshNPCAt".equals(fc.funcName)) {
                if (fc.getParam(2).getRightExpr() != null || fc.getParam(2).getLeftExpr().type != TYPE_STRING) {
                    throw new PQEException(fc.funcName + "的第三个参数必须是常量");
                }
                String varName = PQEUtils.translateStringConstant(fc.getParam(2).getLeftExpr().value);
                checkVarName(varName, localVars);
            }
            
            // Chat/Message/Question函数特殊处理，对应的消息必须是字符串常量，并且字符串要符合规范
            if ("Chat".equals(fc.funcName)) {
                checkStringParamSyntax(fc.getParam(1), localVars);
            } else if ("Message".equals(fc.funcName)) {
                checkStringParamSyntax(fc.getParam(0), localVars);
            } else if ("Question".equals(fc.funcName)) {
                checkStringParamSyntax(fc.getParam(0), localVars);
                checkStringParamSyntax(fc.getParam(1), localVars);
            }
            
            return func.dataType;
        } else if (type == TYPE_NUMBER) {
            return 0;
        } else {
            return 1;
        }
    }
    
    // 检查
    private int checkVarName(String varName, String[] localVars) throws PQEException {
        // 以v开头的是转换后的局部变量
        if (varName.startsWith("v")) {
            return 0;
        }
        
        // 检查是否系统变量
        PQEUtils.SystemVar var = PQEUtils.SYSTEM_VARS_MAP.get(varName);
        if (var != null) {
            return var.dataType;
        }
        
        // 检查是否局部变量
        for (int i = 0; i < localVars.length; i++) {
            if (varName.equals(localVars[i])) {
                return 0;
            }
        }
            
        // 检查变量名是否全局变量
        if (varName.startsWith("__PLAYER_") || varName.startsWith("__TONG_") || 
                varName.startsWith("__FACTION_") || varName.startsWith("__WORLD_") ||
                varName.startsWith("__PARTY_")) {
            return 0;
        }
            
        throw new PQEException("使用了未定义的变量" + varName);
    }
    
    // 检查一个函数的字符串参数是否符合客户端混合格式字符串的要求。
    private void checkStringParamSyntax(Expression expr, String[] localVars) throws PQEException {
        if (expr.getRightExpr() != null || expr.getLeftExpr().type != Expr0.TYPE_STRING) {
            throw new PQEException("Chat/Message/Question函数的参数必须是常量。");
        }
        String str = PQEUtils.translateStringConstant(expr.getLeftExpr().value);
        PQEUtils.checkRichTextSyntax(str, localVars, true);
    }

    /**
     * 检查一个表达式是否能够由客户端执行。本方法不检查表达式是否有效，首先必须通过
     * checkSyntax的检查才能调用此方法。
     */
    public boolean isClientSupport(String[] localVars) {
        if (type == TYPE_IDENTIFIER) {
            // 如果是全局变量，需要判断是否客户端支持，局部变量客户端全部支持；如果是其他任务变量，总是不支持
            if (value.startsWith("$")) {
                return false;
            }
            PQEUtils.SystemVar var = PQEUtils.SYSTEM_VARS_MAP.get(value);
            if (var != null) {
                return var.clientSupport;
            }
            
            // 检查变量名是否全局变量
            if (value.startsWith("__PLAYER_") || value.startsWith("__TONG_") || 
                    value.startsWith("__FACTION_") || value.startsWith("__WORLD_") ||
                    value.startsWith("__PARTY_")) {
                return false;
            }
            return true;
        } else if (type == TYPE_FUNC) {
            // 检查函数客户端是否支持
            FunctionCall fc = getFunctionCall();
            PQEUtils.SystemFunc func = PQEUtils.SYSTEM_FUNCS_MAP.get(fc.funcName);
            if (!func.clientSupport) {
                return false;
            }
            
            // 检查每个参数客户端是否支持
            for (int i = 0; i < fc.getParamCount(); i++) {
                if (!fc.getParam(i).isClientSupport(localVars)) {
                    return false;
                }
            }
            
            // 如果是Set/Inc/Dec函数，需要特殊处理，客户端只能修改局部变量，并且变量名参数必须是常量
            if ("Set".equals(fc.funcName) || "Inc".equals(fc.funcName) || "Dec".equals(fc.funcName)) {
                if (fc.getParam(0).getRightExpr() != null || fc.getParam(0).getLeftExpr().type != TYPE_STRING) {
                    return false;
                }
                String varName = PQEUtils.translateStringConstant(fc.getParam(0).getLeftExpr().value);
                if (varName.startsWith("$") || PQEUtils.SYSTEM_VARS_MAP.containsKey(varName)) {
                    return false;
                }
            }
            
            return true;
        } else if (type == TYPE_STRING) {
            // 如果字符串中用到了非客户端支持的变量，也属于服务器处理范围
            try {
                PQEUtils.checkRichTextSyntax(value, localVars, false);
                return true;
            } catch (Exception e) {
                return false;
            }
        } else {
            return true;
        }
    }

    /**
     * 搜索表达式中可能会修改的变量名字。
     * @param retList
     */
    public void searchAffectLocalVar(java.util.Set<String> retList) {
        if (type == TYPE_FUNC) {
            // 搜索所有参数
            FunctionCall fc = getFunctionCall();
            for (int i = 0; i < fc.getParamCount(); i++) {
                fc.getParam(i).searchAffectLocalVar(retList);
            }
            
            // 如果是Set/Inc/Dec函数，则把第一个参数变量名加入返回列表中
            if ("Set".equals(fc.funcName) || "Inc".equals(fc.funcName) || "Dec".equals(fc.funcName)) {
                Expression param0 = fc.getParam(0);
                if (param0.getRightExpr() == null && param0.getLeftExpr().type == TYPE_STRING) {
                    retList.add(PQEUtils.translateStringConstant(param0.getLeftExpr().value));
                }
            }
            
            // 如果是E_Kill或者E_KillWithMate函数，则把第二个参数变量名加入返回列表中
            if ("E_Kill".equals(fc.funcName) || "E_KillWithMate".equals(fc.funcName) || "E_KillWithAssociation".equals(fc.funcName) || "E_KillWithTeacher".equals(fc.funcName) || "E_ReachPearls".equals(fc.funcName)) {
                Expression param1 = fc.getParam(1);
                if (param1.getRightExpr() == null && param1.getLeftExpr().type == TYPE_STRING) {
                    retList.add(PQEUtils.translateStringConstant(param1.getLeftExpr().value));
                }
            }
            
            // 如果是RefreshNPC/RefreshNPCAt/FindNPCByType函数，则把第三个参数变量名加入返回列表中
            if ("RefreshNPC".equals(fc.funcName) || "RefreshNPCAt".equals(fc.funcName) ||
                    "FindNPCByType".equals(fc.funcName)) {
                Expression param2 = fc.getParam(2);
                if (param2.getRightExpr() == null && param2.getLeftExpr().type == TYPE_STRING) {
                    retList.add(PQEUtils.translateStringConstant(param2.getLeftExpr().value));
                }
            }
        }
    }

    /**
     * 搜索表达式中可能与之有交互的NPC。
     * @param retList
     */
    public void searchRelateNPC(java.util.Set<Integer> retList) {
        if (type == TYPE_FUNC) {
            // 搜索所有参数
            FunctionCall fc = getFunctionCall();
            for (int i = 0; i < fc.getParamCount(); i++) {
                fc.getParam(i).searchRelateNPC(retList);
            }
            
            // 如果是E_TouchNPC函数，并且第一个参数是常量，则这是关联的NPC
            if ("E_TouchNPC".equals(fc.funcName)) {
                Expression param1 = fc.getParam(0);
                if (param1.getRightExpr() == null && param1.getLeftExpr().type == TYPE_NUMBER) {
                    retList.add(PQEUtils.translateNumberConstant(param1.getLeftExpr().value));
                }
            }
        }
    }
        
    /**
     * 把表达式中用到的局部变量名转换为索引。
     */
    public void convertVarNameToIndex(String[] localVars) {
        if (type == TYPE_IDENTIFIER) {
            // 如果是变量引用，替换变量名
            value = convertVarNameToIndex(value, localVars);
        } else if (type == TYPE_FUNC) {
            FunctionCall fc = getFunctionCall();
            
            // 所有参数转换
            for (int i = 0; i < fc.getParamCount(); i++) {
                fc.getParam(i).convertVarNameToIndex(localVars);
            }
            
            // Set/Inc/Dec函数的第一个变量是变量名字符串，需要进行替换
            if ("Set".equals(fc.funcName) || "Inc".equals(fc.funcName) || "Dec".equals(fc.funcName)) {
                Expression param = fc.getParam(0);
                if (param.getRightExpr() == null && param.getLeftExpr().type == Expr0.TYPE_STRING) {
                    String varName = PQEUtils.translateStringConstant(param.getLeftExpr().value);
                    varName = convertVarNameToIndex(varName, localVars);
                    param.getLeftExpr().value = "\"" + PQEUtils.reverseConv(varName) + "\"";
                }
            }
            
            // E_Kill和E_KillWithMate函数的第二个变量是变量名字符串，需要进行替换
            if ("E_Kill".equals(fc.funcName) || "E_KillWithMate".equals(fc.funcName) || "E_KillWithAssociation".equals(fc.funcName) || "E_KillWithTeacher".equals(fc.funcName)|| "E_ReachPearls".equals(fc.funcName)) {
                Expression param = fc.getParam(1);
                if (param.getRightExpr() == null && param.getLeftExpr().type == Expr0.TYPE_STRING) {
                    String varName = PQEUtils.translateStringConstant(param.getLeftExpr().value);
                    varName = convertVarNameToIndex(varName, localVars);
                    param.getLeftExpr().value = "\"" + PQEUtils.reverseConv(varName) + "\"";
                }
            }
            
            // RefreshNPC/RefreshNPCAt/FindNPCByType函数的第三个变量是变量名字符串，需要进行替换
            if ("RefreshNPC".equals(fc.funcName) || "RefreshNPCAt".equals(fc.funcName) ||
                    "FindNPCByType".equals(fc.funcName)) {
                Expression param = fc.getParam(2);
                if (param.getRightExpr() == null && param.getLeftExpr().type == Expr0.TYPE_STRING) {
                    String varName = PQEUtils.translateStringConstant(param.getLeftExpr().value);
                    varName = convertVarNameToIndex(varName, localVars);
                    param.getLeftExpr().value = "\"" + PQEUtils.reverseConv(varName) + "\"";
                }
            }
            
            // Chat/Message/Question函数的字符串变量需要替换
            if ("Chat".equals(fc.funcName)) {
                convertVarNameInStringToIndex(fc.getParam(1), localVars);
            } else if ("Message".equals(fc.funcName)) {
                convertVarNameInStringToIndex(fc.getParam(0), localVars);
            } else if ("Question".equals(fc.funcName)) {
                convertVarNameInStringToIndex(fc.getParam(0), localVars);
                convertVarNameInStringToIndex(fc.getParam(1), localVars);
            }
        }
    }
    
    // 把变量名转换为索引变量名
    private static String convertVarNameToIndex(String varName, String[] localVars) {
        for (int i = 0; i < localVars.length; i++) {
            if (varName.equals(localVars[i])) {
                return "v" + i;
            }
        }
        return varName;
    }

    // 把字符串常量中的变量名转换为索引变量名
    private void convertVarNameInStringToIndex(Expression expr, String[] localVars) {
        if (expr.getRightExpr() == null && expr.getLeftExpr().type == Expr0.TYPE_STRING) {
            String str = PQEUtils.translateStringConstant(expr.getLeftExpr().value);
            str = PQEUtils.convertRichText(str, localVars);
            expr.getLeftExpr().value = "\"" + PQEUtils.reverseConv(str) + "\"";
        }
    }
    
    /**
     * 计算表达式触发依赖的事件掩码。如果这个条件依赖某个事件才有可能成立，那么表达式的事件掩码就是这个
     * 事件对应的掩码值。如果这个条件的成立可能不依赖于任何一个事件，那么它的掩码是EVENT_MASK_CYCLE，
     * 表示它每一个CYCLE都可能被触发。
     */
    public int getEventMask() {
        if (type != TYPE_FUNC) {
            return PQEUtils.EVENT_MASK_CYCLE;
        }
        String funcName = getFunctionCall().funcName;
        if ("E_Approach".equals(funcName) || "E_EnterMap".equals(funcName)) {
            return PQEUtils.EVENT_MASK_POSITION;
        }
        if ("E_TouchNPC".equals(funcName)) {
            return PQEUtils.EVENT_MASK_TOUCHNPC;
        }
        if ("E_Killed".equals(funcName) || "E_KilledByPlayer".equals(funcName)) {
            return PQEUtils.EVENT_MASK_DIE;
        }
        if ("E_Chat".equals(funcName)) {
            return PQEUtils.EVENT_MASK_CHAT;
        }
        if ("E_OpenUI".equals(funcName)) {
            return PQEUtils.EVENT_MASK_OPENUI;
        }
        if ("E_CloseChat".equals(funcName) || "E_AnswerQuestion".equals(funcName) || "E_CloseMessage".equals(funcName)) {
            return PQEUtils.EVENT_MASK_CLOSECHAT;
        }
        return PQEUtils.EVENT_MASK_CYCLE;
    }

    /**
     * 生成实现表达式功能对应的GTL代码。
     */
    public String toGTL() {
        if (type == TYPE_NUMBER || type == TYPE_STRING) {
            return value;
        } else if (type == TYPE_IDENTIFIER) {
            // 如果是局部变量，直接用原来的写法即可；如果是全局变量，需要根据类型转换为GetGlobalInt或GetGlobalString的调用。
            PQEUtils.SystemVar var = PQEUtils.SYSTEM_VARS_MAP.get(value);
            if (var != null) {
                if (var.dataType == 0) {
                    return "GetGlobalInt(\"" + value + "\")";
                } else {
                    return "GetGlobalString(\"" + value + "\")";
                }
            } else {
                return value;
            }
        } else {
            return getFunctionCall().toGTL();
        }
    }

    /**
     * 把混合格式字符串中用到的NPC引用的场景地点引用更新一下。
     * @throws Exception
     */
    public void validateMixedText(ProjectData proj) throws Exception {
        if (type == TYPE_FUNC) {
            FunctionCall fc = getFunctionCall();
            for (int i = 0; i < fc.getParamCount(); i++) {
                fc.getParam(i).validateMixedText(proj);
            }
        } else if (type == TYPE_STRING) {
            String msg = PQEUtils.translateStringConstant(value);
            msg = Quest.validateMixedText(proj, msg);
            value = "\"" + PQEUtils.reverseConv(msg) + "\"";
        }
    }
}
