package com.pip.itimes.server.stage;

import java.io.*;

/**
 * @author Jeffrey
 * @version 1.0
 */
public class TipsLoader {

    public TipsLoader(File file) throws Exception{
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String s = null;
        while((s=reader.readLine())!=null){
            Tips.addTip(s);
        }
    }
}
