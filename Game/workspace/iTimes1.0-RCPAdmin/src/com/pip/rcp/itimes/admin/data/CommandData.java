package com.pip.rcp.itimes.admin.data;


import java.util.List;
import java.util.Vector;


public class CommandData{
    private String name;
    private String command;
    private boolean needConfirm;

    private List<String> parms = new Vector<String>();

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getCommand(){
        return command;
    }

    public void setCommand(String command){
        this.command = command;
    }

    public String[] getParms(){
        String[] result = new String[parms.size()];
        parms.toArray(result);

        return result;
    }

    public void setParms(String[] parms){
        this.parms.clear();

        for(int i = 0; i < parms.length; i++){
            this.parms.add(parms[i]);
        }
    }

    public void addParm(String parm){
        if(!this.parms.contains(parm)){
            this.parms.add(parm);
        }
    }

    public int getParmCount(){
        return parms.size();
    }

    public boolean isNeedConfirm(){
        return needConfirm;
    }

    public void setNeedConfirm(boolean needConfirm){
        this.needConfirm = needConfirm;
    }

    public String toString(){
        return name + ":" + command;
    }

    public boolean equals(Object obj){
        if(obj instanceof CommandData){
            CommandData other = (CommandData)obj;

            if(command.equals(other.command) && needConfirm == other.needConfirm){
                String[] parms1 = getParms();
                String[] parms2 = other.getParms();

                if(parms1.length != parms2.length){
                    return false;
                }else{
                    for(int i = 0; i < parms1.length; i++){
                        if(!parms1[i].equals(parms2[i])){
                            return false;
                        }
                    }
                }

                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }

    public CommandData clone(){
        CommandData clone = new CommandData();

        clone.name = new String(name);
        clone.command = new String(command);
        clone.needConfirm = needConfirm;

        for(int i = 0; i < parms.size(); i++){
            clone.parms.add(new String(parms.get(i)));
        }

        return clone;
    }
}
