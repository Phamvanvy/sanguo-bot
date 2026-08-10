package com.pip.itimes.server.stage;


import java.util.Map;
import java.lang.reflect.Method;

/**
 * @author Jeffery
 * @version 1.0
 */
public class classFile{

    private byte[] data;

    public classFile(byte[] data) {
        this.data = data;
    }


    Object instanse;
    Method getDefaultTasks;
    public void loadClass() throws Exception {
        if(instanse == null&& data != null)
        {
            byteClassLoader loader = new byteClassLoader();
            Class cls = loader.loadClass(data,0,data.length);
            instanse = cls.getConstructor(new Class[0]).newInstance(new Object[0]);
            getDefaultTasks = cls.getMethod("getDefaultTasks",new Class[]{java.util.HashMap.class});
        }
    }
    public void clearClass()
    {
        instanse = null;
        getDefaultTasks = null;
    }
    public short[] getDefaultTasks(Map map) throws Exception {
        loadClass();
        if(getDefaultTasks != null && instanse != null)
        {
           return (short[]) getDefaultTasks.invoke(instanse,new Object[]{map});
        }else
        {
            return new short[0];
        }
    }
}

class byteClassLoader extends ClassLoader {
    public byteClassLoader() {
    }
    public Class loadClass(byte[] data , int start,int len)
    {

        return defineClass(null,data,start,len);
    }
}
