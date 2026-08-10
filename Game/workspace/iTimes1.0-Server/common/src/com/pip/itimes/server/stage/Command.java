package com.pip.itimes.server.stage;

import java.util.StringTokenizer;

/**
 * @author Jeffery
 * @version 1.0
 */
public class Command {

    private String command;
    private String[] params;
    private int serial;
    private int sessionId;
    private byte appType;

    public Command(String s) {
        StringTokenizer st = new StringTokenizer(s," ");
        int count = st.countTokens();
        params = new String[count-1];
        command = st.nextToken();
        for(int i=0;i<count-1;i++){
            params[i] = st.nextToken();
        }
    }

    public Command(String s, boolean b) {
        if (b) {
            s = s.substring(1);
            StringTokenizer st = new StringTokenizer(s, " ");
            int count = st.countTokens();
            params = new String[count - 1];
            command = st.nextToken();
            for (int i = 0; i < count - 1; i++) {
                params[i] = st.nextToken();
            }
        } else {
            StringTokenizer st = new StringTokenizer(s, " ");
            int count = st.countTokens();
            params = new String[count - 1];
            command = st.nextToken();
            for (int i = 0; i < count - 1; i++) {
                params[i] = st.nextToken();
            }
        }
    }

    public String getCommand(){
        return command;
    }

    public int getSessionId() {
        return sessionId;
    }

    public int getSerial() {
        return serial;
    }

    public byte getAppType() {
        return appType;
    }

    public String getParam(int i){
        return params[i];
    }

    public int getParamCount(){
        return params.length;
    }

    public void setSerial(int serial) {
        this.serial = serial;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public void setAppType(byte appType) {
        this.appType = appType;
    }

    public String getParamString(int begin){
        StringBuilder sb = new StringBuilder();
        for(int i=begin;i<params.length;i++){
            sb.append(params[i]);
            sb.append(" ");
        }
        return sb.toString();
    }
}
