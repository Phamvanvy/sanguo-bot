package com.pip.sanguo.editor.quest.expr;

import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import com.pip.sanguo.data.map.GameMapInfo;
import com.pip.sanguo.data.map.GameMapNPC;
import com.pip.sanguo.data.quest.QuestInfo;
import com.pip.sanguo.data.quest.pqe.Expr0;
import com.pip.sanguo.data.quest.pqe.Expression;
import com.pip.sanguo.data.quest.pqe.PQEUtils;
import com.pip.sanguo.data.quest.pqe.ParserConstants;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.property.LocationPropertyDescriptor;
import com.pip.sanguo.editor.property.NPCPropertyDescriptor;
import com.pip.sanguo.editor.property.VariablePropertyDescriptor;

/**
 * 表达式模板：查找一个NPC是否在某点附近。
 * @author lighthu
 */
public class C_FindNPCAt extends AbstractExpr {
    public String varName;
    public int mapID;
    public int x;
    public int y;
    public int distance;
    public boolean checkTrue = true;
    
	/**
	 * 构造指定全局变量的模板。
	 * @param name 全局变量名称
	 */
	public C_FindNPCAt(QuestInfo qinfo) {
	    varName = "新变量";
	    mapID = 0;
	    x = 0;
	    y = 0;
	    distance = 80;
	    questInfo = qinfo;
	}
	
    /**
     * 判断这个模板是一个条件还是一个动作。
     */
    public boolean isCondition() {
        return true;
    }

    /**
     * 取得生成的表达式。
     */
    public String getExpression() {
        if (checkTrue) {
            return "FindNPCAt(" + varName + ", " + mapID + ", " + x + ", " + y + ", " + distance + ")";
        } else {
            return "FindNPCAt(" + varName + ", " + mapID + ", " + x + ", " + y + ", " + distance + ") == 0";
        }
    }

    /**
	 * 用模板创建新的表达式片段。
	 */
	public IExpr createNew(QuestInfo qinfo) {
		return new C_FindNPCAt(qinfo);
	}

	/**
	 * 取得模板名称。
	 */
	public String getName() {
		return "NPC在某位置附近...";
	}

	/**
	 * 转换为自然语言表示。
	 */
	public String toNatureString() {
	    String targetName = GameMapInfo.locationToString(EditorApplication.getProj(), new int[] { mapID, x, y }, false);
	    if (checkTrue) {
	        return "NPC " + varName + " 在 " + targetName + " 附近 " + (distance / 8.00) + " 码内 ";
	    } else {
	        return "NPC " + varName + " 不在 " + targetName + " 附近 " + (distance / 8.00) + " 码内 ";
	    }
	}

    /**
     * 识别一个表达式是否匹配本模板。如果匹配，返回一个新的表达式片段对象，否则返回null。
     */
    public IExpr recognize(QuestInfo qinfo, Expression expr) {
        if (expr.getLeftExpr().type == Expr0.TYPE_FUNC && expr.getLeftExpr().getFunctionCall().funcName.equals("FindNPCAt")) {
            if (expr.getLeftExpr().getFunctionCall().getParamCount() != 5) {
                return null;
            }
            Expression param1 = expr.getLeftExpr().getFunctionCall().getParam(0);
            Expression param2 = expr.getLeftExpr().getFunctionCall().getParam(1);
            Expression param3 = expr.getLeftExpr().getFunctionCall().getParam(2);
            Expression param4 = expr.getLeftExpr().getFunctionCall().getParam(3);
            Expression param5 = expr.getLeftExpr().getFunctionCall().getParam(4);
            if (param1.getRightExpr() == null && param1.getLeftExpr().type == Expr0.TYPE_IDENTIFIER &&
                param2.getRightExpr() == null && param2.getLeftExpr().type == Expr0.TYPE_NUMBER &&
                param3.getRightExpr() == null && param3.getLeftExpr().type == Expr0.TYPE_NUMBER &&
                param4.getRightExpr() == null && param4.getLeftExpr().type == Expr0.TYPE_NUMBER &&
                param5.getRightExpr() == null && param5.getLeftExpr().type == Expr0.TYPE_NUMBER) {
                C_FindNPCAt ret = (C_FindNPCAt)createNew(qinfo);
                ret.varName = param1.getLeftExpr().value;
                ret.mapID = PQEUtils.translateNumberConstant(param2.getLeftExpr().value);
                ret.x = PQEUtils.translateNumberConstant(param3.getLeftExpr().value);
                ret.y = PQEUtils.translateNumberConstant(param4.getLeftExpr().value);
                ret.distance = PQEUtils.translateNumberConstant(param5.getLeftExpr().value);
                if (expr.getRightExpr() == null) {
                    return ret;
                } else if (expr.getRightExpr().type == Expr0.TYPE_NUMBER && PQEUtils.translateNumberConstant(expr.getRightExpr().value) == 0) {
                    ret.checkTrue = false;
                    return ret;
                }
            }
        }
        return null;
    }

    // 下面是IPropertySource接口的实现

    /**
     * 取得属性描述符。这个模板有1个参数：参数。
     */
    public IPropertyDescriptor[] getPropertyDescriptors() {
        return new IPropertyDescriptor[] { 
                new VariablePropertyDescriptor("varName", "保存ID的变量", questInfo, true),
                new LocationPropertyDescriptor("location", "目标位置"),
                new TextPropertyDescriptor("distance", "有效距离(码)"),
                new ComboBoxPropertyDescriptor("checkTrue", "条件成立", new String[] { "是", "否" })
        };
    }

    /**
     * 取得属性当前值。
     */
    public Object getPropertyValue(Object id) {
        if ("varName".equals(id)) {
            return varName;
        } else if ("distance".equals(id)) {
            return String.valueOf(distance / 8.00);
        } else if ("checkTrue".equals(id)) {
            return checkTrue ? 0 : 1;
        } else if ("location".equals(id)) {
            return new int[] { mapID, x, y };
        }
        return null;
    }

    /**
     * 设置属性当前值。
     */
    public void setPropertyValue(Object id, Object value) {
        if ("varName".equals(id)) {
            String newValue = (String)value;
            if (!newValue.equals(varName)) {
                varName = newValue;
                fireValueChanged();
            }
        } else if ("distance".equals(id)) {
            int newValue = (int)(Double.parseDouble((String)value) * 8);
            if (newValue != distance) {
                distance = newValue;
                fireValueChanged();
            }
        } else if ("checkTrue".equals(id)) {
            boolean newValue = ((Integer)value).intValue() == 0;
            if (newValue != checkTrue) {
                checkTrue = newValue;
                fireValueChanged();
            }
        } else if ("location".equals(id)) {
            int[] newValue = (int[])value;
            if (newValue[0] != mapID || newValue[1] != x || newValue[2] != y) {
                mapID = newValue[0];
                x = newValue[1];
                y = newValue[2];
                fireValueChanged();
            }
        }
    }
}
