package com.pip.itimes.server.world.battle.arena;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ArenaWorldListManager implements Runnable{
    private File file;
    private long lastModified;
    private ConcurrentHashMap<String, ArenaWorldDetail> sources = new ConcurrentHashMap<String, ArenaWorldDetail>();

    public ArenaWorldListManager(File file) throws Exception{
        this.file = file;
        lastModified = file.lastModified();
        loadSources();
        new Thread(this, "ClientDetailMonitor").start();
    }

    private void loadSources() throws Exception{
        BufferedReader reader = new BufferedReader(new FileReader(file));
        SourceDef[] preSources = createSources(reader);
        for(int i = 0; i < preSources.length; i++){
            ArenaWorldDetail source = sources.get(preSources[i].id);
            if(source != null){
                source.setPassWord(preSources[i].password);
                source.setAddress(preSources[i].address);
            }else{
                source = new ArenaWorldDetail(preSources[i].id, preSources[i].password, preSources[i].address);
                sources.put(source.getId(), source);
            }
        }
    }

    public ArenaWorldDetail getArenaWorldDetail(String id){
        return sources.get(id);
    }

    private SourceDef[] createSources(BufferedReader reader) throws IOException{
        List<SourceDef> l = new ArrayList<SourceDef>(100);
        String s = null;
        while((s = reader.readLine()) != null){
            if(!s.startsWith("#")){
                try{
                    SourceDef source = createSource(s);
                    l.add(source);
                }catch(ParseException e){
                    e.printStackTrace();
                }
            }
        }
        SourceDef[] source = new SourceDef[l.size()];
        l.toArray(source);
        return source;
    }

    private SourceDef createSource(String s) throws ParseException{
        String[] ss = s.split(";");
        if(ss.length != 3)
            throw new ParseException(s, 0);
        if(!checkServiceId(ss[0]))
            throw new ParseException(s, 0);
        if(!checkPassword(ss[1]))
            throw new ParseException(s, 0);
        int address = strToIP(ss[2]);
        SourceDef ret = new SourceDef();
        ret.id = ss[0];
        ret.password = ss[1];
        ret.address = address;
        return ret;
    }

    private boolean checkServiceId(String s){
        if(s.length() == 0)
            return false;
        return true;
    }

    private boolean checkPassword(String s){
        if(s.length() == 0)
            return false;
        return true;
    }

    public int strToIP(String s){
        String[] secs = s.split("\\.");
        return ((Integer.parseInt(secs[0]) << 24) & 0xFF000000) | ((Integer.parseInt(secs[1]) << 16) & 0xFF0000) | ((Integer.parseInt(secs[2]) << 8) & 0xFF00) | (Integer.parseInt(secs[3]) & 0xFF);
    }

    class SourceDef{
        String id;
        String password;
        int address;
    }

    public void run(){
        while(true){
            if(isFileModified()){
                try{
                    loadSources();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
            try{
                Thread.sleep(3 * 1000L);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    protected boolean isFileModified(){
        long t = file.lastModified();
        if(t != lastModified){
            lastModified = t;
            return true;
        }
        return false;
    }
}
