package com.pip.rcp.itimes.admin.data;


public class ServerData implements Cloneable{
    private String ip;
    private String port;
    private String desc;
    private String user;
    private String password;

    public static int DATA_COUNT = 5;

    public String getIp(){
        return ip;
    }

    public void setIp(String ip){
        this.ip = ip;
    }

    public String getPort(){
        return port;
    }

    public void setPort(String port){
        this.port = port;
    }

    public String getDesc(){
        return desc;
    }

    public void setDesc(String desc){
        this.desc = desc;
    }

    public String getUser(){
        return user;
    }

    public void setUser(String user){
        this.user = user;
    }

    public String getPassword(){
        return password;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public String toString(){
        return ip + ":" + port + " " + desc + " " + user + " " + password;
    }

    public boolean equals(Object obj){
        if(obj instanceof ServerData){
            ServerData other = (ServerData)obj;

            if(ip.equals(other.ip) && port.equals(other.port) && user.equals(other.user) && password.equals(other.password)){
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }

    public ServerData clone(){
        ServerData clone = new ServerData();

        clone.ip = new String(ip);
        clone.port = new String(port);
        clone.desc = new String(desc);
        clone.user = new String(user);
        clone.password = new String(password);

        return clone;
    }
}
