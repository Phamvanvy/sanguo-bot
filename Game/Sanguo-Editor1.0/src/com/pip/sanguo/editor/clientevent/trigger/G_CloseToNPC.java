package com.pip.sanguo.editor.clientevent.trigger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.pip.sanguo.data.clientEvent.ClientEvent;

/**
 * 表达式模板：靠近某个NPC(X,Y)。
 * @author ZHANGBAIQUAN
 */
public class G_CloseToNPC extends AbstractExprEvent {
    private static final int PARAM_Max = 4;  //参数的个数
    
    private Text  textMapId;        //地图ID
    private Text  textX;            //X坐标
    private Text  textY;            //Y坐标
    private Text  textDistance;     //距离
    
    /**
     * 地图ID     x坐标     Y坐标
     */
    public String[][] params = {
        {  
           String.valueOf(VARIABLE_INT),    //参数类型
           String.valueOf(VARIABLE_INT), 
           String.valueOf(VARIABLE_INT),
           String.valueOf(VARIABLE_INT),
        },   
        {  
           "",                              //参数数值
           "",
           "",
           "",
         }                             
    };
    
    /**
     * 构造指定全局变量的模板。
     * @param name 全局变量名称
     */
    public G_CloseToNPC(ClientEvent event) {
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
        return new G_CloseToNPC(event);
    }
    
    /**
     * 取得模板名称。
     */
    public String getName() {
        return "靠近某个NPC";
    }

    /**
     * 取得生成的表达式。
     */
    public String getExpression() {
        return "g_closeToNPC";
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
        params[PARAM_VALUE][2] = paramsValue[2];
        params[PARAM_VALUE][3] = paramsValue[3];
    }
    
    /**
     * 设置模板参数值。
     */
    public void setParamsValue(){
       params[PARAM_VALUE][0] = textMapId.getText();
       params[PARAM_VALUE][1] = textX.getText();
       params[PARAM_VALUE][2] = textY.getText();
       params[PARAM_VALUE][3] = textDistance.getText();
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
        labelParams.setText("地图Id：");
        textMapId = new Text(composite, SWT.BORDER);
        textMapId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
        
        Label labelParams1 = new Label(composite, SWT.NONE);
        labelParams1.setText("X坐标：");
        textX = new Text(composite, SWT.BORDER);
        textX.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
        
        Label labelParams2 = new Label(composite, SWT.NONE);
        labelParams2.setText("Y坐标：");
        textY = new Text(composite, SWT.BORDER);
        textY.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
        
        Label labelParams3 = new Label(composite, SWT.NONE);
        labelParams3.setText("距离：");
        textDistance = new Text(composite, SWT.BORDER);
        textDistance.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
        return composite;
    }
    
    /**
     * 设置属性当前值。
     */
    public void setPropertyValue(String str) {
        String[] paramsValue = TempManagerEvent.getSplit(str, PARAM_Max);
        textMapId.setText(paramsValue[0]);
        textX.setText(paramsValue[1]);
        textY.setText(paramsValue[2]);
        textDistance.setText(paramsValue[3]);
    }

}
