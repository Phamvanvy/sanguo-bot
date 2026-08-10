package com.pip.sanguo.editor.clientevent.trigger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.pip.sanguo.data.clientEvent.ClientEvent;

/**
 * 表达式模板：玩家杀死某个怪物。
 * @author ZHANGBAIQUAN
 */
public class M_KILLMonsterID extends AbstractExprEvent {
    private static final int PARAM_Max = 2;  //参数的个数
    
    private Text textMonsterID;    //怪物模板ID
    private Text textNumbers;      //杀死的数量
    
    public String[][] params = {
        {               
            String.valueOf(VARIABLE_INT),   //参数类型
            String.valueOf(VARIABLE_INT),
        },    
        {  
            "",                             //参数数值
            "",
        }                               
    };
    
    /**
     * 构造指定全局变量的模板。
     * @param name 全局变量名称
     */
    public M_KILLMonsterID(ClientEvent event) {
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
        return new M_KILLMonsterID(event);
    }
    
    /**
     * 取得模板名称。
     */
    public String getName() {
        return "玩家杀死怪物";
    }

    /**
     * 取得生成的表达式。
     */
    public String getExpression() {
        return "m_kILLMonsterID";
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
        params[PARAM_VALUE][0] = paramsValue[0];
        params[PARAM_VALUE][1] = paramsValue[1];
    }
    
    /**
     * 设置模板参数值。
     */
    public void setParamsValue(){
       params[PARAM_VALUE][0] = textMonsterID.getText();
       params[PARAM_VALUE][1] = textNumbers.getText();
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
        
        Label labelParams = new Label(composite, SWT.NONE);
        labelParams.setText("怪物模板ID：");
        textMonsterID = new Text(composite, SWT.BORDER);
        textMonsterID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
        
        Label labelParams1 = new Label(composite, SWT.NONE);
        labelParams1.setText("杀死数量：");
        textNumbers = new Text(composite, SWT.BORDER);
        textNumbers.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
        return composite;
    }
    
    /**
     * 设置属性当前值。
     */
    public void setPropertyValue(String str) {
        String[] paramsValue = TempManagerEvent.getSplit(str, PARAM_Max);
        textMonsterID.setText(paramsValue[0]);
        textNumbers.setText(paramsValue[1]);
    }

}
