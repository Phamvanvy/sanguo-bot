package com.pip.log;

import java.io.BufferedReader;
import java.io.FileReader;

import com.pip.log.define.Definitions;
import com.pip.log.define.Log;
import com.pip.log.define.LogDefine;

public class Main{
    public static void main(String[] args){
        try{
            Definitions.loadDefinitions();
        }catch(Exception e){
            e.printStackTrace();
        }

        BufferedReader br = null;

        try{
            LogDefine define = Definitions.getLogDefine("iTimes");
            br = new BufferedReader(new FileReader("logm20100209-22"));
//            br = new BufferedReader(new FileReader("logm20100209-22"));
//            br = new BufferedReader(new FileReader("logm20100221-01"));

            String line = br.readLine();

            while(line != null){
                try{
                    Log log = new Log(define, line);
                    log.process();
                    System.out.println(log.toString());
                }catch(Exception e){
                    e.printStackTrace();
                }finally{
                    line = br.readLine();
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{
                br.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        //        BufferedReader br = null;
        //        
        //        try{
        //            br = new BufferedReader(new FileReader("id173886.txt"));
        //
        //            String line = br.readLine();
        //
        //            while(line != null){
        //                try{
        //                    Pattern pt = Pattern.compile("Changed\\[[0-9A-F ]+\\]");
        //                    Matcher m = pt.matcher(line);
        //                    if(m.find()){
        //                        String c = m.group();
        //                        Pattern pt1 = Pattern.compile("Changed\\[|\\]");
        //                        Matcher m1 = pt1.matcher(c);
        //                        String pp = m1.replaceAll("");
        //                        LogProcessorChanged.test(pp);
        //                    }
        //                }catch(Exception e){
        //                    e.printStackTrace();
        //                }finally{
        //                    line = br.readLine();
        //                }
        //            }
        //        }catch(Exception e){
        //            e.printStackTrace();
        //        }finally{
        //            try{
        //                br.close();
        //            }catch(Exception e){
        //                e.printStackTrace();
        //            }
        //        }

        //        RegexTest.go();
    }
}
