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
 * 表达式模板：检测玩家和配偶一起杀死某类怪物。
 * @author lighthu
 */
public class G_CheckRoleAttribute extends AbstractExprEvent {
    private static final int PARAM_Max = 8;  //参数的个数
    
    private Text  textLevel;    //级别
    private Combo comboLevel;
    
    private Text  textMoney;    //金钱
    private Combo comboMoney;
    
    private Text  textHP;       //生命
    private Combo comboHP;
    
    private Text  textMP;       //内力
    private Combo comboMP;
    
    private Text  textSTR;      //力量
    private Combo comboSTR;
    
    private Text  textAGI;      //敏捷
    private Combo comboAGI;
    
    private Text  textINT;      //智力
    private Combo comboINT;
    
    private Text  textSTA;      //体力
    private Combo comboSTA;
    
//    private Text  textJOB;      //职业
//    private Combo comboJOB;
    
    private static final String[][] mathContrast = {
        {"等于", "小于", "大于"},
        {"==",   "<",    ">"},
    };
    
    /**参数的基本类型和数值
     * 1.基本类型    2.参数数值
     * leng    8
     */
    private String[][] params = {
        {
            String.valueOf(VARIABLE_INT),  //级别
            String.valueOf(VARIABLE_INT),  //金钱
            String.valueOf(VARIABLE_INT),  //生命
            String.valueOf(VARIABLE_INT),  //内力
            String.valueOf(VARIABLE_INT),  //力量
            String.valueOf(VARIABLE_INT),  //敏捷
            String.valueOf(VARIABLE_INT),  //智力
            String.valueOf(VARIABLE_INT),  //体力
        },
        {
             "",   //级别
             "",   //金钱
             "",   //生命
             "",   //内力
             "",   //力量
             "",   //敏捷
             "",   //智力
             "",   //体力
        }
    };
    
    /**
     * 构造指定全局变量的模板。
     * @param name 全局变量名称
     */
    public G_CheckRoleAttribute(ClientEvent event) {
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
        return new G_CheckRoleAttribute(event);
    }
    
    /**
     * 取得模板名称。
     */
    public String getName() {
        return "玩家属性检查";
    }
    
    /**
     * 取得生成的表达式。
     */
    public String getExpression() {
        return "g_checkRoleAttribute";
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
        String[] paramsValue = getSplit(str);
        for(int i=0; i<paramsValue.length; i++){
            params[PARAM_VALUE][i] = paramsValue[i];
        }
    }
    
    /**
     * 设置模板参数值。
     */
    public void setParamsValue(){
        String textStr = textLevel.getText();   //级别
        if(textStr.equals("")){
            params[PARAM_VALUE][0] = "";
        }else{
            params[PARAM_VALUE][0] = mathContrast[1][comboLevel.getSelectionIndex()]+" "+textStr;
        }
        
        textStr = textMoney.getText();      //金钱
        if(textStr.equals("")){
            params[PARAM_VALUE][1] = "";
        }else{
            params[PARAM_VALUE][1] = mathContrast[1][comboMoney.getSelectionIndex()]+" "+textStr;
        }
        
        textStr = textHP.getText();      //生命
        if(textStr.equals("")){
            params[PARAM_VALUE][2] = "";
        }else{
            params[PARAM_VALUE][2] = mathContrast[1][comboHP.getSelectionIndex()]+" "+textStr;
        }
        
        textStr = textMP.getText();      //内力
        if(textStr.equals("")){
            params[PARAM_VALUE][3] = "";
        }else{
            params[PARAM_VALUE][3] = mathContrast[1][comboMP.getSelectionIndex()]+" "+textStr;
        }
        
        textStr = textSTR.getText();      //力量
        if(textStr.equals("")){
            params[PARAM_VALUE][4] = "";
        }else{
            params[PARAM_VALUE][4] = mathContrast[1][comboSTR.getSelectionIndex()]+" "+textStr;
        }
        
        textStr = textAGI.getText();      //敏捷
        if(textStr.equals("")){
            params[PARAM_VALUE][5] = "";
        }else{
            params[PARAM_VALUE][5] = mathContrast[1][comboAGI.getSelectionIndex()]+" "+textStr;
        }
        
        textStr = textINT.getText();      //智力
        if(textStr.equals("")){
            params[PARAM_VALUE][6] = "";
        }else{
            params[PARAM_VALUE][6] = mathContrast[1][comboINT.getSelectionIndex()]+" "+textStr;
        }
        
        textStr = textSTA.getText();      //体力
        if(textStr.equals("")){
            params[PARAM_VALUE][7] = "";
        }else{
            params[PARAM_VALUE][7] = mathContrast[1][comboSTA.getSelectionIndex()]+" "+textStr;
        }
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
        
        Label labelLevel = new Label(composite, SWT.NONE);
        labelLevel.setText("级别：");
        comboLevel = new Combo(composite, SWT.READ_ONLY);
        comboLevel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        comboLevel.setItems(mathContrast[0]);
        textLevel = new Text(composite, SWT.BORDER);
        textLevel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
        
        Label labelMoney = new Label(composite, SWT.NONE);
        labelMoney.setText("金钱：");
        comboMoney = new Combo(composite, SWT.READ_ONLY);
        comboMoney.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        comboMoney.setItems(mathContrast[0]);
        textMoney = new Text(composite, SWT.BORDER);
        textMoney.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
        
        Label labelHP = new Label(composite, SWT.NONE);
        labelHP.setText("生命：");
        comboHP = new Combo(composite, SWT.READ_ONLY);
        comboHP.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        comboHP.setItems(mathContrast[0]);
        textHP = new Text(composite, SWT.BORDER);
        textHP.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
        
        Label labelMP = new Label(composite, SWT.NONE);
        labelMP.setText("内力：");
        comboMP = new Combo(composite, SWT.READ_ONLY);
        comboMP.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        comboMP.setItems(mathContrast[0]);
        textMP = new Text(composite, SWT.BORDER);
        textMP.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
        
        Label labelSTR = new Label(composite, SWT.NONE);
        labelSTR.setText("力量：");
        comboSTR = new Combo(composite, SWT.READ_ONLY);
        comboSTR.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        comboSTR.setItems(mathContrast[0]);
        textSTR = new Text(composite, SWT.BORDER);
        textSTR.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
        
        Label labelAGI = new Label(composite, SWT.NONE);
        labelAGI.setText("敏捷：");
        comboAGI = new Combo(composite, SWT.READ_ONLY);
        comboAGI.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        comboAGI.setItems(mathContrast[0]);
        textAGI = new Text(composite, SWT.BORDER);
        textAGI.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
        
        Label labelINT = new Label(composite, SWT.NONE);
        labelINT.setText("智力：");
        comboINT = new Combo(composite, SWT.READ_ONLY);
        comboINT.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        comboINT.setItems(mathContrast[0]);
        textINT = new Text(composite, SWT.BORDER);
        textINT.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
        
        Label labelSTA = new Label(composite, SWT.NONE);
        labelSTA.setText("体力：");
        comboSTA = new Combo(composite, SWT.READ_ONLY);
        comboSTA.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        comboSTA.setItems(mathContrast[0]);
        textSTA = new Text(composite, SWT.BORDER);
        textSTA.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
        
//        Label labelJOB = new Label(composite, SWT.NONE);
//        labelJOB.setText("职业：");
//        comboJOB = new Combo(composite, SWT.READ_ONLY);
//        comboJOB.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
//        comboJOB.setItems(mathContrast[0]);
//        textJOB = new Text(composite, SWT.BORDER);
//        textJOB.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
        return composite;
    }
    
    /**
     * 设置属性当前值。
     */
    public void setPropertyValue(String str) {
        String[] paramsValue = getSplit(str);
        
        if(!paramsValue[0].equals("")){      //级别
            String[] param = paramsValue[0].split(" ");
            comboLevel.select(getMathIndex(param[0]));
            textLevel.setText(param[1]);
        }
        
        if(!paramsValue[1].equals("")){      //金钱
            String[] param = paramsValue[1].split(" ");
            comboMoney.select(getMathIndex(param[0]));
            textMoney.setText(param[1]);
        }
        
        if(!paramsValue[2].equals("")){       //生命
            String[] param = paramsValue[2].split(" ");
            comboHP.select(getMathIndex(param[0]));
            textHP.setText(param[1]);
        }
        
        if(!paramsValue[3].equals("")){     //内力
            String[] param = paramsValue[3].split(" ");
            comboMP.select(getMathIndex(param[0]));
            textMP.setText(param[1]);
        }
        
        if(!paramsValue[4].equals("")){     //力量
            String[] param = paramsValue[4].split(" ");
            comboSTR.select(getMathIndex(param[0]));
            textSTR.setText(param[1]);
        }
        
        if(!paramsValue[5].equals("")){     //敏捷
            String[] param = paramsValue[5].split(" ");
            comboAGI.select(getMathIndex(param[0]));
            textAGI.setText(param[1]);
        }
        
        if(!paramsValue[6].equals("")){      //智力
            String[] param = paramsValue[6].split(" ");
            comboINT.select(getMathIndex(param[0]));
            textINT.setText(param[1]);
        }
        
        if(!paramsValue[7].equals("")){      //体力
            String[] param = paramsValue[7].split(" ");
            comboSTA.select(getMathIndex(param[0]));
            textSTA.setText(param[1]);
        }
    }
    
    public String[] getSplit(String str){
        String[] splits = new String[PARAM_Max];
        int startIndex = 0;
        for(int i = 0; i < PARAM_Max; i++){
            int endIndex = str.indexOf(",", startIndex);
            if(endIndex < 0){
                splits[i] = str.substring(startIndex,str.length());
            }else{
                splits[i] = str.substring(startIndex,endIndex);
            }
            startIndex = endIndex+1;
        }
        return splits;
    }
    
    public int getMathIndex(String str){
        for(int i=0; i < mathContrast[0].length; i++){
            if(str.equals(mathContrast[1][i])){
                return i;
            }
        }
        return -1;
    }
}
