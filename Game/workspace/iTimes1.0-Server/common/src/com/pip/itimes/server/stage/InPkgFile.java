package com.pip.itimes.server.stage;


/**
 * @author Jeffery
 * @version 1.0
 */
public class InPkgFile{

    private String name;
    private byte[] data;

    public InPkgFile() {
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public void setData(byte[] data){
        this.data = data;
    }

    public byte[] getData(){
        return data;
    }

}
