package com.pip.itimes.server.stage;

/**
 * @author Jeffery
 * @version 1.0
 */
public class ChatOption {
    public static final byte WORLD = 0;
    public static final byte MAP = 1;
    public static final byte GUILD = 2;
    public static final byte GROUP = 3;
    public static final byte TEAM = 4;
    public static final byte FAVORITE = 5;
    public static final byte SYSTEM = 6;
    public static final byte PRIVATE = 7;

    public byte pri;
    public byte color;

    public static final byte[][] chatOptionDefault = {
        {1 , 3},
        {1 , 7},
        {2 , 6},
        {2 , 5},
        {2 , 15},
        {2 , 13},
        {2 , 4},
        {2 , 9}
    };

    public static final ChatOption[] getDefaltChatOptions(){
        ChatOption[] ret = new ChatOption[8];
        for(int i=0;i<8;i++){
            ChatOption option = new ChatOption();
            option.pri = chatOptionDefault[i][0];
            option.color = chatOptionDefault[i][1];
            ret[i] = option;
        }
        return ret;
    }
}
