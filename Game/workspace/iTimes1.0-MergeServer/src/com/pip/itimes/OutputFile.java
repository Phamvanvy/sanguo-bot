package com.pip.itimes;

import java.io.FileOutputStream;
import java.io.PrintStream;

import org.apache.log4j.Logger;

public class OutputFile{
    private static final Logger log = Logger.getLogger(OutputFile.class);

    private static final String DEFAULT_FILENAME = "output.sql";
    private static final boolean DEFAULT_APPEND = true;

    private static PrintStream printStream = null;
    private static String fileName = DEFAULT_FILENAME;
    private static boolean append = DEFAULT_APPEND;

    private void init() throws Exception{
        printStream = new PrintStream(new FileOutputStream(fileName, append), true, "utf8");
    }

    public void setOut(String fName, boolean apd) throws Exception{
        fileName = fName;
        append = apd;

        try{
            init();
        }catch(Exception e){
            log.error(e, e);

            fileName = DEFAULT_FILENAME;
            append = DEFAULT_APPEND;

            init();
        }
    }

    public void print(String str){
        if(printStream == null){
            try{
                init();
            }catch(Exception e){
                log.error(e, e);
            }
        }

        printStream.print(str);
    }

    public void println(){
        if(printStream == null){
            try{
                init();
            }catch(Exception e){
                log.error(e, e);
            }
        }

        printStream.println();
    }

    public void println(String str){
        if(printStream == null){
            try{
                init();
            }catch(Exception e){
                log.error(e, e);
            }
        }

        printStream.println(str);
    }
}
