package com.pip.sanguo.editor.quest.expr;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import com.pip.sanguo.data.map.GameMapInfo;
import com.pip.sanguo.data.quest.QuestInfo;
import com.pip.sanguo.data.quest.pqe.Expr0;
import com.pip.sanguo.data.quest.pqe.Expression;
import com.pip.sanguo.data.quest.pqe.PQEUtils;
import com.pip.sanguo.editor.EditorApplication;
import com.pip.sanguo.editor.property.LocationPropertyDescriptor;

public class C_ApprochPosition extends AbstractExpr {
    
    public int mapID;
    public int x;
    public int y;
    public int area;
    
    public C_ApprochPosition(){
        this.mapID = 0;
        this.x = 0;
        this.y = 0;
        this.area = 0;
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
        return "E_ApprochPosition(" + mapID + ", " + x + ", " + y +", " + area+ ")";
    }
    
    /**
     * 用模板创建新的表达式片段。
     */
    public IExpr createNew(QuestInfo qinfo) {
        return new C_ApprochPosition();
    }

    /**
     * 取得模板名称。
     */
    public String getName() {
        return "玩家接近某位置多少码...";
    }

    /**
     * 转换为自然语言表示。
     */
    public String toNatureString() {
        String targetName = GameMapInfo.locationToString(EditorApplication.getProj(), new int[] { mapID, x, y, area}, false);
        return "玩家接近 " + targetName;
    }

    /**
     * 识别一个表达式是否匹配本模板。如果匹配，返回一个新的表达式片段对象，否则返回null。
     */
    public IExpr recognize(QuestInfo qinfo, Expression expr) {
        if (expr.getLeftExpr().type == Expr0.TYPE_FUNC && expr.getLeftExpr().getFunctionCall().funcName.equals("E_ApprochPosition") && expr.getRightExpr() == null) {
            if (expr.getLeftExpr().getFunctionCall().getParamCount() != 4) {
                return null;
            }
            Expression param1 = expr.getLeftExpr().getFunctionCall().getParam(0);
            Expression param2 = expr.getLeftExpr().getFunctionCall().getParam(1);
            Expression param3 = expr.getLeftExpr().getFunctionCall().getParam(2);
            Expression param4 = expr.getLeftExpr().getFunctionCall().getParam(3);
            if (param1.getRightExpr() == null && param1.getLeftExpr().type == Expr0.TYPE_NUMBER &&
                param2.getRightExpr() == null && param2.getLeftExpr().type == Expr0.TYPE_NUMBER &&
                param3.getRightExpr() == null && param3.getLeftExpr().type == Expr0.TYPE_NUMBER &&
                param4.getRightExpr() == null && param4.getLeftExpr().type == Expr0.TYPE_NUMBER) {
                C_ApprochPosition ret = (C_ApprochPosition)createNew(qinfo);
                ret.mapID = PQEUtils.translateNumberConstant(param1.getLeftExpr().value);
                ret.x = PQEUtils.translateNumberConstant(param2.getLeftExpr().value);
                ret.y = PQEUtils.translateNumberConstant(param3.getLeftExpr().value);
                ret.area = PQEUtils.translateNumberConstant(param4.getLeftExpr().value);
                return ret;
            }
        }
        return null;
    }

    // 下面是IPropertySource接口的实现

    /**
     * 取得属性描述符。这个模板有4个参数：mapID，x，y, area。
     */
    public IPropertyDescriptor[] getPropertyDescriptors() {
        return new IPropertyDescriptor[] { 
                new LocationPropertyDescriptor("location", "目标位置"),
                new TextPropertyDescriptor("area", "码")
        };
    }

    /**
     * 取得属性当前值。
     */
    public Object getPropertyValue(Object id) {
        if ("location".equals(id)) {
            return new int[] { mapID, x, y };
        } else if("area".equals(id)){
            return String.valueOf(area);
        }
        return null;
    }

    /**
     * 设置属性当前值。
     */
    public void setPropertyValue(Object id, Object value) {
        if ("location".equals(id)) {
            int[] newValue = (int[])value;
            if (newValue[0] != mapID || newValue[1] != x || newValue[2] != y) {
                mapID = newValue[0];
                x = newValue[1];
                y = newValue[2];
                fireValueChanged();
            }
        }else if ("area".equals(id)) {
            int newValue = Integer.parseInt((String)value);
            if (newValue != area) {
                area = newValue;
                fireValueChanged();
            }
        }
    }
    
}
