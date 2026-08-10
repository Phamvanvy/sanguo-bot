package pip;

/**
 * 定义GTVM中用到的指令。
 */
public interface GTVMConstants {
    public static final byte ADD = (byte)0x01;
    public static final byte SUB = (byte)0x02;
    public static final byte MUL = (byte)0x03;
    public static final byte DIV = (byte)0x04;
    public static final byte MOD = (byte)0x05;
    public static final byte AND = (byte)0x06;
    public static final byte OR = (byte)0x07;
    public static final byte ANDB = (byte)0x08;

    public static final byte EQ = (byte)0x11;
    public static final byte GT = (byte)0x12;
    public static final byte LT = (byte)0x13;

    public static final byte JMP = (byte)0x21;
    public static final byte JEQ = (byte)0x22;
    public static final byte JNE = (byte)0x23;
    public static final byte PSE = (byte)0x24;
    public static final byte CALL = (byte)0x25;
    public static final byte RET = (byte)0x26;
    public static final byte VRET = (byte)0x27;

    public static final byte LOAD = (byte)0x31;
    public static final byte SAVE = (byte)0x32;
    public static final byte LOAD8 = (byte)0x33;
    public static final byte LOAD16 = (byte)0x34;
    public static final byte LOAD32 = (byte)0x35;
    public static final byte LOADS = (byte)0x36;
    public static final byte LOADPARA = (byte)0x37;
    public static final byte SAVEPARA = (byte)0x38;
    public static final byte ALOAD = (byte)0x39;
    public static final byte ASAVE = (byte)0x3A;

    public static final byte C_MAPID = (byte)0x41;
    public static final byte C_GETUNIT = (byte)0x42;
    public static final byte C_GETUSER = (byte)0x43;
    public static final byte C_KEY = (byte)0x44;
    public static final byte C_NANYKEY = (byte)0x45;
    public static final byte C_TUNIT = (byte)0x46;
    public static final byte C_NENEMY = (byte)0x47;
    public static final byte C_POS = (byte)0x48;
    public static final byte C_ATTRI = (byte)0x49;
    public static final byte C_ATTRS = (byte)0x4A;
    public static final byte C_NEARPOS = (byte)0x4B;
    public static final byte C_ANSWER = (byte)0x4C;
    public static final byte C_HASTASK = (byte)0x4D;
    public static final byte C_INPUT = (byte)0x4E;
    public static final byte C_RANDOM = (byte)0x4F;
    public static final byte C_GETTIME = (byte)0x50;
    public static final byte C_BATTLE = (byte)0x51;
    public static final byte C_HASTASKITEM = (byte)0x52;
    public static final byte C_TASKFINISHED = (byte)0x53;
    public static final byte C_BILLING = (byte)0x54;
    public static final byte C_INT = (byte)0x55;
    public static final byte C_GETTILE = (byte)0x56;

    public static final byte D_CHAT = (byte)0x61;
    public static final byte D_MESSAGE = (byte)0x62;
    public static final byte D_MOVESCREEN = (byte)0x63;
    public static final byte D_REMOVEUNIT = (byte)0x64;
    public static final byte D_GOTOMAP = (byte)0x65;
    public static final byte D_SETATTR = (byte)0x66;
    public static final byte D_MOVETOUNIT = (byte)0x67;
    public static final byte D_PLAYSOUND = (byte)0x68;
    public static final byte D_VIBRA = (byte)0x69;
    public static final byte D_ASKQ = (byte)0x6A;
    public static final byte D_ASSIGNTASK = (byte)0x6B;
    public static final byte D_ENDTASK = (byte)0x6C;
    public static final byte D_FLASH = (byte)0x6D;
    public static final byte D_BATTLE = (byte)0x6E;
    public static final byte D_ADDTASKITEM = (byte)0x6F;
    public static final byte D_REMOVETASKITEM = (byte)0x70;
    public static final byte D_BILLING = (byte)0x71;
    public static final byte D_LOGOUT = (byte)0x72;
    public static final byte D_INPUT = (byte)0x73;
    public static final byte D_POPUPLIST = (byte)0x74;
    public static final byte D_SENDCMD = (byte)0x75;
    public static final byte D_SETTILE = (byte)0x76;
    public static final byte D_SENDRQST = (byte)0x77;
    public static final byte D_SETNPCHINT = (byte)0x78;

    public static final int INSTRUCTION_MAX = 0x77;

    /** 每条指令的长度 */
    public static final byte[] INSTRUCTION_LENGTH = {
        0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0,
        0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 3, 3, 3, 1, 3, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 1, 1, 2, 3, 5, 2, 2, 2, 1, 1, 0, 0, 0, 0, 0,
        0, 1, 1, 1, 2, 1, 1, 1, 3, 3, 3, 3, 1, 1, 1, 1,
        1, 1, 2, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 1, 2, 3, 1, 1, 3, 1, 3, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 3, 1, 1, 1, 3, 2
    };

    /** 每条指令对栈指针的影响 */
    public static final byte[] STACK_EFFECT = {
        0, -1, -1, -1, -1, -1, -1, -1, -1,  0,  0,  0,  0,  0,  0,  0,
        0, -1, -1, -1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
        0,  0, -1, -1,  2,  2,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
        0,  0, -2,  1,  1,  1,  1,  1, -1, -1, -3,  0,  0,  0,  0,  0,
        0,  1,  0,  0,  1,  1,  0,  1,  1,  0,  0,  1,  1,  0,  1,  1,
        1,  1, -2,  0,  1,  0, -1,  0,  0,  0,  0,  0,  0,  0,  0,  0,
        0, -2, -1,  0, -1, -3, -2, -1,  0,  0, -2, -2, -2,  0, -1, -1,
       -2, -3, -1, -1, -2, -1, -3, -1, -1
    };
}
