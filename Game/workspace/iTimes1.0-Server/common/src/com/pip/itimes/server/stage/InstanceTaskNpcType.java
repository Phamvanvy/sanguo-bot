package com.pip.itimes.server.stage;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class InstanceTaskNpcType extends TaskNpcType{

    private int instanceId;
    private String message;
    private String instanceType;

    public InstanceTaskNpcType(int id,String name,int type) {
        super(id,name,type);
    }

    public void setInstanceId(int instanceId){
        this.instanceId = instanceId;
    }

    public int getInstanceId(){
        return instanceId;
    }

    public String getMessage(){
        return message;
    }

    public String getInstanceType() {
        return instanceType;
    }

    public void setMessage(String message){
        this.message = message;
    }

    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }

}
