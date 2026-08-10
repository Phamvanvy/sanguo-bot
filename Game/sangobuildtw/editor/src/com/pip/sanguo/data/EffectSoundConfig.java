package com.pip.sanguo.data;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 光效对应的声音配置文件。
 * @author lighthu
 */
public class EffectSoundConfig {
    private File sourceFile;
    private Map<Integer, String> configMap = new HashMap<Integer, String>();
    
    public EffectSoundConfig(File f) {
        sourceFile = f;
        load();
    }
    
    public String getConfig(int id) {
        return configMap.get(id);
    }
    
    public void setConfig(int id, String name) {
        if (name == null) {
            configMap.remove(id);
        } else {
            configMap.put(id, name);
        }
    }
    
    private void load() {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(sourceFile);
            DataInputStream dis = new DataInputStream (fis);
            int count = dis.readShort();
            for (int i = 0; i < count; i++) {
                int id = dis.readInt();
                String name = dis.readUTF();
                configMap.put(id, name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception e) {
                }
            }
        }
    }
    
    public void save() {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(sourceFile);
            DataOutputStream dos = new DataOutputStream(fos);
            dos.writeShort(configMap.size());
            for (int id : configMap.keySet()) {
                dos.writeInt(id);
                dos.writeUTF(configMap.get(id));
            }
            dos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e) {
                }
            }
        }
    }
}
