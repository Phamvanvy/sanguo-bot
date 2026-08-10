package com.pip.sanguo.editor.clientevent.trigger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.pip.sanguo.data.clientEvent.ClientEvent;

/**
 * 表达式模板：是否完成某个任务。
 * @author ZHANGBAIQUAN
 */
public class G_IsGuideRole extends AbstractExprEvent {
    private static final int PARAM_Max = 1;  //参数的个数
    
    private Combo comboYesOrNo;   //是或否
    
    public String[][] params = {
        {  
            String.valueOf(VARIABLE_INT),       //参数类型
        },    
        {  
            "0",
        }                              
    };
    
    /**
     * 构造指定全局变量的模板。
     * @param name 全局变量名称
     */
    public G_IsGuideRole(ClientEvent event) {
        clientEvent = event;
    }
    
    /**
     * 判断这个模板是一个条件还是一个动作。
     */
    public boolean isCondition() {
        return true;
    }
    
    /**
     * 用模板创建新的表达式片段。
     */
    public IExprEvent createNew(ClientEvent event) {
        return new G_FinisnQuest(event);
    }
    
    /**
     * 取得模板名称。
     */
    public String getName() {
        return "玩家是否是未改名的游客";
    }

    /**
     * 取得生成的表达式。
     */
    public String getExpression() {
        return "g_isGuideRole";
    }

    /**
     * 取得模板参数基本类型。
     */
    public String getParamsType() {
        return TempManagerEvent.getParameterStr(params[PARAM_TYPE]);
    }
    
    /**
     * 取得模板参数值。
     */
    public String getParamsValue() {
        return TempManagerEvent.getParameterStr(params[PARAM_VALUE]);
    }
    
    /**
     * 取得模板的所有参数的值(String)。
     */
    public void initParamsValue(String str){
        String[] paramsValue = TempManagerEvent.getSplit(str, PARAM_Max);
        for(int i=0; i<PARAM_Max ; i++){
            params[PARAM_VALUE][i] = paramsValue[i];
        }
    }
    
    /**
     * 设置模板参数值。
     */
    public void setParamsValue(){
       params[PARAM_VALUE][0] = String.valueOf(comboYesOrNo.getSelectionIndex());
    }
    
    /**
     * 取得改模板的界面
     */
    public Composite getProperty(Composite _con) {
        final Composite composite = new Composite(_con, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 6, 1));
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 6;
        composite.setLayout(gridLayout);
        
        Label label1 = new Label(composite, SWT.NONE);
        label1.setText("条件(是或否)：");
        comboYesOrNo = new Combo(composite, SWT.READ_ONLY);
        comboYesOrNo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        comboYesOrNo.setItems(new String[]{"是","否"});
        return composite;
    }
    
    /**
     * 设置属性当前值。
     */
    public void setPropertyValue(String str) {
        String[] paramsValue = TempManagerEvent.getSplit(str, PARAM_Max);
        comboYesOrNo.select(Integer.parseInt(paramsValue[0]));
    }

}
