package com.pip.ui;
//#if NewUI2
import java.io.BufferedInputStream;
//#endif
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;

//#if Revision == TAIWAN
//#if ModelID == Android
//# import android.util.Log;
//#endif
//#if ModelID == Android || ModelID == AndroidLarge || ModelID == AndroidSmall || ModelID == AndroidAuto
//# import joymaster.igb.billing.IGBKernel;
//# import com.JoyVersionCtrl.JoyVersionCtrlCore;
//# import android.telephony.TelephonyManager;
//#else
import billing.IGBKernel;
//#endif
//#endif

//#if ChannelCode == UC_CHANNEL_JAVA
import extendSDK.ucSDK;
//#endif

//#if ModelID == AndroidAuto
//# import javax.microedition.midlet.MIDlet;
//# import com.pip.android.FormActivity;
//# import com.pip.android.PipActivity;
//# import com.pip.android.media.SoundPlayer;
//#if opengl == true
//# import com.pip.android.opengl.GLGraphics;
//# import com.pip.android.opengl.GLTextureManager;
//# import javax.microedition.lcdui.Display;
//#endif
//#endif
import com.pip.common.Tool;
import com.pip.common.Utilities;
import com.pip.engine.AnimateCache;
import com.pip.engine.AnimatePlayer;
import com.pip.engine.IAnimateCallback;
import com.pip.engine.IVMGameProcessor;
import com.pip.engine.Weather;
import com.pip.gui.GAndroidEditText;
import com.pip.gui.GContainer;
import com.pip.gui.GGameIcon;
import com.pip.gui.GIcon;
import com.pip.gui.GImageNumer;
import com.pip.gui.GLabel;
import com.pip.gui.GLinePanel;
import com.pip.gui.GScrollBar;
import com.pip.gui.GTextArea;
import com.pip.gui.GWidget;
import com.pip.gui.GWebview;
import com.pip.gui.GWindow;
import com.pip.gui.IGCycle;
import com.pip.gui.IGPaint;
import com.pip.image.ImageSet;
import com.pip.image.PipAnimateSet;
import com.pip.io.UASegment;
import com.pip.resource.ResourceManager;
import com.pip.sanguo.GameIcon;
import com.pip.sanguo.GameMain;
import com.pip.sanguo.GameNetPlayer;
import com.pip.sanguo.GameNpc;
import com.pip.sanguo.GameSprite;
import com.pip.sanguo.GameView;
import com.pip.sanguo.GameWorld;
import com.pip.sanguo.SanguoMIDlet;
import com.pip.util.SortHashtable;
import com.pip.util.VMCounter;


//#if Revision == JP
//# import android.app.AlertDialog;
//# import android.content.DialogInterface;
//# import android.content.DialogInterface;
//# import android.os.Build;
//#endif

/**
 * GTVM参考实现。客户端实现时可能需要重新定义结构以适应类合并的需求。
 * 一个GTVM同时只能运行一个脚本，并通过4个VM接口（init, cycle, paint, destory）调用脚本。
 */
public class VM implements CommandListener
//#if NewUI2
//# , Display.TextInputListener
//#endif
{
    public static final byte ADD = (byte)0x01;
    public static final byte SUB = (byte)0x02;
    public static final byte MUL = (byte)0x03;
    public static final byte DIV = (byte)0x04;
    public static final byte MOD = (byte)0x05;
    public static final byte AND = (byte)0x06;
    public static final byte OR = (byte)0x07;
    public static final byte ANDB = (byte)0x08;
    public static final byte ORB = (byte)0x09;
    public static final byte LSHIFT = (byte)0x0A;
    public static final byte RSHIFT = (byte)0x0B;
    public static final byte INCV = (byte)0x0C;
    public static final byte ADDV8 = (byte)0x0D;
    public static final byte SUBV8 = (byte)0x0E;

    public static final byte EQ = (byte)0x11;
    public static final byte GT = (byte)0x12;
    public static final byte LT = (byte)0x13;
    public static final byte EQ8 = (byte)0x14;
    public static final byte GT8 = (byte)0x15;
    public static final byte LT8 = (byte)0x16;
    public static final byte NE8 = (byte)0x17;

    public static final byte INCVS = (byte)0x18;
    public static final byte ADDV8S = (byte)0x19;
    public static final byte SUBV8S = (byte)0x1A;
    public static final byte LOADVS = (byte)0x1B;
    public static final byte SAVEVS = (byte)0x1C;
    public static final byte DUP = (byte)0x1D;

    public static final byte JMP = (byte)0x21;
    public static final byte JEQ = (byte)0x22;
    public static final byte JNE = (byte)0x23;
    public static final byte CALL = (byte)0x24;
    public static final byte RET = (byte)0x25;
    public static final byte VRET = (byte)0x26;
    public static final byte SYSCALL = (byte)0x27;

    public static final byte ALOAD8 = (byte)0x28;
    public static final byte ASAVE8 = (byte)0x29;
    public static final byte STLOAD8 = (byte)0x2A;
    public static final byte STSAVE8 = (byte)0x2B;

    public static final byte TSWITCH = (byte)0x2C;
    public static final byte LSWITCH = (byte)0x2D;
    
    public static final byte CALLPTR = (byte)0x2E;

    public static final byte LOAD = (byte)0x31;
    public static final byte SAVE = (byte)0x32;
    public static final byte LOAD32 = (byte)0x33;
    public static final byte LOAD16 = (byte)0x34;
    public static final byte LOAD8 = (byte)0x35;
    public static final byte ALOAD = (byte)0x36;
    public static final byte ASAVE = (byte)0x37;
    public static final byte ALLOC = (byte)0x38;
    public static final byte FREE = (byte)0x39;
    public static final byte STALLOC = (byte)0x3A;
    public static final byte STLOAD = (byte)0x3B;
    public static final byte STSAVE = (byte)0x3C;
    public static final byte LOADV = (byte)0x3D;
    public static final byte SAVEV = (byte)0x3E;
    public static final byte LOADFUNC = (byte)0x3F;

    public static final byte LOADVS3 = (byte)0x41;
    public static final byte LOADVS2 = (byte)0x42;
    public static final byte LOAD88 = (byte)0x43;
    public static final byte LOAD8VS = (byte)0x44;
    public static final byte LOADVS8 = (byte)0x45;
    public static final byte SYSCALLSAVEVS = (byte)0x46;
    public static final byte LOADVSSTLOAD8 = (byte)0x47;
    public static final byte LOAD8VSSTLOAD8 = (byte)0x48;
    public static final byte LOADVSADDALOAD = (byte)0x49;
    public static final byte LOADVSALOAD = (byte)0x4A;

    public static final int INSTRUCTION_MAX = 0x4A;

    /** 每条指令的长度 */
    public static final byte[] INSTRUCTION_LENGTH = {
        0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 5, 6, 6, 0,
        0, 1, 1, 1, 2, 2, 2, 2, 5, 6, 6, 5, 5, 2, 0, 0,
        0, 3, 3, 3, 4, 1, 1, 5, 2, 2, 2, 2, 0, 0, 2, 0,     // TSWITCH和LSWITCH两条指令的长度特殊处理
        0, 1, 1, 5, 3, 2, 1, 1, 2, 1, 3, 1, 1, 5, 5, 3,
        0,13, 9, 3, 6, 6, 9, 6, 7, 5, 5
    };

    /** 每条指令对栈指针的影响 */
    public static final byte[] STACK_EFFECT = {
        0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,  0,  1,  1,  0,
        0, -1, -1, -1,  0,  0,  0,  0,  0,  1,  1,  1, -1,  1,  0,  0,
        0,  0, -1, -1,  0,  0,  0,  0,  0, -2,  0, -2, -1, -1,  0,  0,  // CALL,RET,VRET,SYSCALL,CALLPTR五个指令的栈指针特殊处理
        0,  0, -2,  1,  1,  1, -1, -3,  0, -1,  1, -1, -3,  1, -1,  1,
        0,  3,  2,  2,  2,  2,  0,  1,  2, -1,  0 
    };

    /** 全局VMDATA数据池，用于不同vm之间传递结构中的Object，格式为<Integer, Object>，key是所有VM的全局索引*/
    public static final Hashtable globalVMData = new Hashtable();
    private static int globalVMDataCurrentKey = 1; //0表示默认时为空

    public static final int TRUE = 1;
    public static final int FALSE = 0;
    
    // VM接口定义
    public static final int INIT = 0;
    public static final int CYCLE = 1;
    public static final int PROCESSPACKET = 2;
    public static final int CYCLEUI = 3;
    public static final int PAINT = 4;
    public static final int DESTROY = 5;

    /** 头信息：语言版本号。版本0没有回调函数功能，版本1有回调函数功能。 */
    public byte languageVersion = 0;

    // 文件版本号：0表示普通执行文件，1表示库
    protected short fileVersion;
    // 文件ID（对于库文件有效）
    protected short libraryID;
    // 静态堆，只能存储整型数据
    protected int[] staticHeap;
    // 栈，只能存储整型数据
    protected int[] stack;
    // 栈顶指针
    protected int esp;
    // 当前函数调用的起始栈位置
    protected int stackBase;
    // 当前执行的VM对象，0表示自己，>0表示调用库
    protected int currentVM;
    // 当前栈中包含函数上下文的个数
    protected int callCount;
    // 当前执行指针，表示函数内地址
    protected int eip;
    // 当前执行的函数ID
    protected int currentFunc;
    // 动态堆，存储所有Object和数组
    protected Object[] dynamicHeap;
    // 动态堆可用空间链表，数组中每个元素存储下一个空闲空间的下标，组成一个循环链表。
    protected short[] freeSpaceList;
    // 可用空间链表头，表示下一个可用空间
    protected int freeHead;
    // 下一个临时变量空间，动态堆的前32个空间是保留给临时变量的
    protected final static int TEMP_OBJECT_COUNT = 32;
    protected int nextTemp;
    protected int tempSpace;

    // 字符串表
    protected String[] stringTable;
    // 代码段
    protected byte[] codeData;
    // 函数表
    protected int[] functions;
    //回调函数表
    protected Hashtable callbacks;
    // 引用库表
    protected String[] libNames;
    // 引用库对象
    protected VM[] libraries;
    // 可以由Java直接调用的函数的最大ID
    protected int javaCallFunctionEnd = DESTROY;

    // 虚拟机运行是否阻塞
    protected boolean blocked;
    protected int[] blockPosition;
    protected int funcBase;
    protected boolean resumeFlag;
    
    // 虚拟机运行标志
    protected boolean running;

   // protected Object owner;
    
    //#if ModelID == Android || ModelID == AndroidLarge || ModelID == AndroidSmall || ModelID == AndroidAuto
    //# public Object owner;          // axq modify
  //#else
      protected Object owner;
  //#endif
    
    protected byte ownerType;

    protected static final byte OWNER_TYPE_UI = 0;
    protected static final byte OWNER_TYPE_PROCESSOR = 1;
    protected static final byte OWNER_TYPE_QUEST = 2;
    
//#if NewUI2
    //# private String lastInputText;
	
	//# public void onTextInputEnd(String result) {
		//# lastInputText = result;
		//# continueProcess(TRUE);
	//# }
//#endif

    public VM(Object owner){
        this.owner = owner;

        if(owner instanceof IVMGameProcessor){
            ownerType = OWNER_TYPE_PROCESSOR;
        }else if(owner instanceof Quest){
            ownerType = OWNER_TYPE_QUEST;
        }else{
            ownerType = OWNER_TYPE_UI;
        }
    }

    public boolean isBlock(){
        return blocked;
    }

    public void pauseProcess(){
        blocked = true;
    }

    public void continueProcess(int returnValue){
        resumeFlag = true;
        if(blockPosition != null){
            blockPosition[blockPosition.length - 1] = returnValue;
        }
    }

    protected int[] saveStack(){
        int[] ret = new int[esp + 6]; //栈内容长度为esp+1，多分配5个空间，用来存储：栈底指针、当前VM、运行指针、当前函数、调用层数
        ret[0] = stackBase;
        ret[1] = currentVM;
        ret[2] = eip;
        ret[3] = currentFunc;
        ret[4] = callCount;
        if(esp >= 0){
            System.arraycopy(stack, 0, ret, 5, esp + 1);
        }
        return ret;
    }
    
    protected void restoreStack(int[] bp) {
        stackBase = bp[0];
        currentVM = bp[1];
        eip = bp[2];
        currentFunc = bp[3];
        callCount = bp[4];
        funcBase = currentFunc * 3;
        esp = bp.length - 6;
        if(esp >= 0){
            System.arraycopy(bp, 5, stack, 0, esp + 1);
        }
    }

    protected void resume(){
        blocked = false;
        if(blockPosition != null){
            int[] bp = blockPosition;
            blockPosition = null;
            restoreStack(bp);
            try{
                processInst(false);
            }catch(Exception e){
            	//#ifdef buildtest
                e.printStackTrace();
              //#endif
            }
        }
    }

    /**
     * 载入一个ETF文件，进入准备执行状态。
     */
    public void init(byte[] data) throws IOException{
        loadETF(data);

        this.esp = -1;
        this.stackBase = -1;
        this.eip = 0;
        this.currentFunc = 0;

        if (fileVersion == 0) {
            // 初始化动态堆，留出前面32个来作为VM临时空间，永远不分配给脚本
            //#if MemoryMode == "small"
            //# int initHeapSize = 48;
            //#else
            int initHeapSize = 128;
            //#endif
            tempSpace = TEMP_OBJECT_COUNT;
            if(owner != null && owner instanceof Quest){
                initHeapSize = 10;
                tempSpace = 8;
            }
            this.dynamicHeap = new Object[initHeapSize];
            this.freeSpaceList = new short[initHeapSize];
            for(int i = tempSpace - 1; i < initHeapSize - 1; i++){
                this.freeSpaceList[i] = (short)(i + 1);
            }
            this.freeSpaceList[initHeapSize - 1] = (short)(tempSpace - 1);
            this.freeHead = tempSpace - 1;
        }
    }
    
    /**
     * 动态链接所有引用的库
     */
    public void link() {
        libraries = new VM[libNames.length + 1];
        libraries[0] = this;
        for (int i = 0; i < libNames.length; i++) {
            libraries[i + 1] = VMGame.getVMGame(libNames[i]).getVM();
        }
    }

    /**
     * 从字节流中读取一个ETF文件的内容。
     */
    public void loadETF(byte[] data) throws IOException{
    	//#if NewUI2
    	DataInputStream is = new DataInputStream(new BufferedInputStream(new ByteArrayInputStream(data)));
    	//#else
    	//# DataInputStream is = new DataInputStream(new ByteArrayInputStream(data));
    	//#endif
        
        // 读取头信息
        int head = is.readInt();
        if(head != 0x45474C00 && head != 0x45474C01){ // EGL0 or EGL1
            throw new IOException("Invalid ETF file!");
        }
        languageVersion = (byte)(head & 0xFF);
        fileVersion = is.readShort();
        libraryID = is.readShort();
        
        is.skip(4); // 跳过修改时间
        short heapSize = is.readShort();
        short taskAttr = is.readShort();
        Tool.readUTF16(is); // 跳过名字
        Tool.readUTF16(is); // 跳过描述
        is.readInt(); // 跳过文件长度
        // 读取字符串表
        short tk = is.readShort();
        String[] etfStringTable;
        if(tk == 0x5354){ // ST
            short count = is.readShort();
            if(count <= 0){
                throw new IOException("Invalid ETF file!");
            }
            etfStringTable = new String[count];
            short len = is.readShort();
            count = 0;
            while(len > 0){
                String s = Tool.readUTF16(is);
                if(s.length() < 128){
                    len -= 1 + 2 * s.length();
                }else{
                    len -= 2 + 2 * s.length();
                }
                etfStringTable[count++] = s;
            }
            if(len != 0 || count != etfStringTable.length){
                throw new IOException("Invalid ETF file!");
            }
            tk = is.readShort(); // 读取下一个段标识
        }else{
            etfStringTable = new String[0];
        }
        // 读取函数
        int[] etfFunctions;
        byte[] etfCode;
        if(tk == 0x4354){ // CT
            short count = is.readShort();
            if(count <= 0){
                throw new IOException("Invalid ETF file!");
            }
            etfFunctions = new int[count * 3];
            int len = is.readInt();
            etfCode = new byte[len];
            int codePos = 0, base = 0;
            for(int i = 0; i < count; i++){
                int paramCount = is.readByte() & 0xFF;
                len -= 1;
                is.skip(paramCount);
                len -= paramCount;
                int localVariables = is.readShort() & 0xFFFF;
                len -= 2;
                int funcLen = is.readInt();
                len -= 4;
                etfFunctions[base] = (paramCount << 16) | localVariables;
                etfFunctions[base + 1] = codePos;
                is.read(etfCode, codePos, funcLen);
                etfFunctions[base + 2] = codePos + funcLen;
                codePos += funcLen;
                len -= funcLen;
                base += 3;
            }
            if(len != 0){
                throw new IOException("Invalid ETF file!");
            }
        }else{
            throw new IOException("Invalid ETF file!");
        }

        if(languageVersion == 1){
            // 读取回调函数
            tk = is.readShort();
            if(tk == 0x4342){ // CB
                short count = is.readShort();
                if(count < 0){
                    throw new IOException("Invalid ETF file!");
                }
                callbacks = new Hashtable();
                short len = is.readShort();
                for(int i = 0; i < count; i++){
                    String s = Tool.readUTF16(is);
                    if(s.length() < 128){
                        len -= 1 + 2 * s.length();
                    }else{
                        len -= 2 + 2 * s.length();
                    }
                    short id = is.readShort();
                    len -= 2;
                    callbacks.put(s, new Short(id));
                }
                if(len != 0){
                    throw new IOException("Invalid ETF file!");
                }
            }else{
                throw new IOException("Invalid ETF file!");
            }
            if (fileVersion == 0) {
                javaCallFunctionEnd += callbacks.size();
            } else {
                javaCallFunctionEnd = callbacks.size();
            }
            
            // 读取引用库
            tk = is.readShort();
            if (tk == 0x4C42) { // LB
                short count = is.readShort();
                if(count < 0){
                    throw new IOException("Invalid ETF file!");
                }
                libNames = new String[count];
                short len = is.readShort();
                for (int i = 0; i < count; i++) {
                    libNames[i] = Tool.readUTF16(is);
                    if(libNames[i].length() < 128){
                        len -= 1 + 2 * libNames[i].length();
                    }else{
                        len -= 2 + 2 * libNames[i].length();
                    }
                }
                if(len != 0){
                    throw new IOException("Invalid ETF file!");
                }
            }else{
                throw new IOException("Invalid ETF file!");
            }
        }

        // 载入完成
        if (fileVersion == 0) {
            // 库不需要创建运行环境
            this.staticHeap = new int[heapSize & 0xFFFF];
            this.stack = new int[taskAttr & 0xFFFF];
        }
        this.stringTable = etfStringTable;
        this.functions = etfFunctions;
        this.codeData = etfCode;
    }

    /**
     * 卸载当前运行的ETF文件。
     */
    public void destroy(){
        staticHeap = null;
        stack = null;
        dynamicHeap = null;
        freeSpaceList = null;
        stringTable = null;
        functions = null;
      
    }

    // 从动态堆中分配一个单元，返回空闲单元的下标
    protected int heapAlloc(){
        if(freeSpaceList[freeHead] == freeHead){
            // 链表中只有一个元素，需要申请新的空间了
            //#if MemoryMode == "small"
            //# int expandSize = dynamicHeap.length / 5;
            //#else
            int expandSize = dynamicHeap.length / 2;
            //#endif
            Object[] newarr = new Object[dynamicHeap.length + expandSize];
            short[] newarr2 = new short[dynamicHeap.length + expandSize];
            System.arraycopy(dynamicHeap, 0, newarr, 0, dynamicHeap.length);
            System.arraycopy(freeSpaceList, 0, newarr2, 0, dynamicHeap.length);
            for(int i = dynamicHeap.length; i < newarr2.length; i++){
                newarr2[i] = (short)(i + 1);
            }
            newarr2[newarr2.length - 1] = freeSpaceList[freeHead];
            newarr2[freeHead] = (short)dynamicHeap.length;
            dynamicHeap = newarr;
            freeSpaceList = newarr2;
        }

        // 取出链表中第二个元素返回，并修改链表
        int next = freeSpaceList[freeHead] & 0xFFFF;
        freeSpaceList[freeHead] = freeSpaceList[next];
        return next;
    }

    // 释放动态堆中的一个单元
    protected void heapFree(int addr){
        if((addr & 0xFFF) < tempSpace){
            return;
        }
        dynamicHeap[addr] = null;
        short tmp = freeSpaceList[freeHead];
        freeSpaceList[freeHead] = (short)addr;
        freeSpaceList[addr] = tmp;
    }

    // 读取静态堆或者栈中的值，这个值可能是一个整数，也可能是一个指针。
    protected int memLoad(int addr){
        if((addr & 0x80000000) == 0){
            // 静态堆地址
            return staticHeap[addr & 0x3FFFFFFF];
        }else{
            // 栈地址
            return stack[stackBase + (addr & 0x3FFFFFFF)];
        }
    }

    // 向静态堆或者栈中存储值，这个值可能是一个整数，也可能是一个指针。
    // 这里需要特殊处理一个情况：当系统函数返回一个对象时，会把这个对象存放在一个临时的位置，并返回这个临时空间的地址。
    // 那么，当把这个临时空间地址保存到内存中的时候，需要看目标单元中存储的当前地址，如果是有效地址，需要用新对象的Java
    // 引用覆盖旧对象；否则，需要在动态堆中额外分配一个单元来存储这个对象，并在目标变量中存入新分配的地址。
    protected void memSave(int addr, int value){
        if((addr & 0x80000000) == 0){
            // 静态堆地址
            staticHeap[addr & 0x3FFFFFFF] = value;

            if(ownerType == OWNER_TYPE_QUEST){
                Tool.sendSyncVMVarialbe(((Quest)owner).id, addr, value);
            }
        }else{
            // 栈地址
            stack[stackBase + (addr & 0x3FFFFFFF)] = value;
        }
    }

    // 读取数组元素的值（数组全部都存储在动态堆中），如果是对象数组，则返回数组中指定对象的指针
    protected int arrLoad(int addr, int offset){
        int pointer = addr;
        int dataType = (pointer >> 26) & 0x0F;
        if(dataType > 3){
            return pointer | (offset << 12) | 0x02000000; // 对于对象数组，返回指定对象指针，用中间14位表示数组内下标
        }
        Object obj = dynamicHeap[pointer & 0xFFF];
        switch(dataType){
            case 0:
                return ((boolean[])obj)[offset]? 1: 0;
            case 1:
                return ((byte[])obj)[offset];
            case 2:
                return ((short[])obj)[offset];
            case 3:
                return ((int[])obj)[offset];
            default:
                return 0; // 不可能出现
        }
    }

    // 向数组中存储值，如果是对象数组，则对对象引用进行复制
    protected void arrSave(int addr, int offset, int value){
        int pointer = addr;
        int dataType = (pointer >> 26) & 0x0F;
        Object obj = dynamicHeap[pointer & 0xFFF];
        if(dataType > 3){
            // 对于对象数组，说明value是一个对象指针，查找这个对象，把引用放到数组中
            ((Object[])obj)[offset] = followPointer(value);
            return;
        }
        switch(dataType){
            case 0:
                ((boolean[])obj)[offset] = value == 0? false: true;
                break;
            case 1:
                ((byte[])obj)[offset] = (byte)value;
                break;
            case 2:
                ((short[])obj)[offset] = (short)value;
                break;
            case 3:
                ((int[])obj)[offset] = value;
                break;
        }
    }
    
    // 根据指针找到对象，指针可以引用动态堆中的对象、动态堆中数组元素、字符串表中的字符串
    public Object followPointer(int pointer){
        if(pointer == 0){
            return null;
        }
        if((pointer & 0x80000000) != 0){
            // 字符串表
            short libID = (short)((pointer >> 16) & 0x7FFF);
            if (libID == 0) {
                // 引用执行文件的字符串表
                return stringTable[pointer & 0xFFFF];
            } else {
                // 引用库文件的字符串表
                for (int i = 1; i < libraries.length; i++) {
                    if (libID == libraries[i].libraryID) {
                        return libraries[i].stringTable[pointer & 0xFFFF];
                    }
                }
                return null;
            }
        }else{
            int dataType = (pointer >> 26) & 0x1F;
            if(dataType >= 4 && dataType <= 19){
                // 这个类型段是普通对象和简单类型数组
                int t = pointer & 0xFFF;
                return dynamicHeap[t & 0xFFF];
            }else if(dataType >= 20){
                // 这个类型段是对象数组，检查这个指针是数组的指针还是数组内元素的指针
                Object[] arr = (Object[])dynamicHeap[pointer & 0xFFF];
                if((pointer & 0x02000000) != 0){
                    // 数组内元素的指针
                    return arr[(pointer >> 12) & 0x1FFF];
                }else{
                    return arr;
                }
            }else{
                return null;
            }
        }
    }

    // 把指针指向的Object数组转换为String数组。因为VM设计的原因，在VM里分配的String数据实际是Object数组
    protected String[] getStringArrayFromParams(int pointer){
        String[] strs = null;
        Object[] obs = (Object[])followPointer(pointer);
        if(obs != null){
            strs = new String[obs.length];
            for(int i = 0; i < strs.length; i++){
                strs[i] = (String)obs[i];
            }
        }
        return strs;
    }

    // 在动态堆上分配一个数组，返回地址
    protected int alloc(byte dataType, int length){
        int ret = heapAlloc();
        switch(dataType){
            case 0:
                dynamicHeap[ret] = new boolean[length];
                break;
            case 1:
                dynamicHeap[ret] = new byte[length];
                break;
            case 2:
                dynamicHeap[ret] = new short[length];
                break;
            case 3:
                dynamicHeap[ret] = new int[length];
                break;
            case 11:
                dynamicHeap[ret] = new String[length];
                break;
            default:
                dynamicHeap[ret] = new Object[length];
                break;
        }
        return ret | ((dataType + 16) << 26);
    }

    // 释放一个指针对象指向的动态堆单元
    public void free(int addr){
        if((addr & 0x82000000) == 0){
            heapFree(addr & 0xFFF);
        }
    }

    // 创建一个临时对象空间，返回其指针地址。动态堆的第0-2个元素始终是不用的，可以用来存储临时对象。通常在系统函数需要返回一个对象时用
    // 这个临时对象空间。
    public synchronized int makeTempObject(Object obj){
        if(obj == null){
            return 0;
        }else{
            dynamicHeap[nextTemp] = obj;
            int addr = nextTemp;
            nextTemp = (nextTemp + 1) & (tempSpace - 1);
            if(obj instanceof boolean[]){
                return (16 << 26) | addr;
            }else if(obj instanceof byte[]){
                return (17 << 26) | addr;
            }else if(obj instanceof short[]){
                return (18 << 26) | addr;
            }else if(obj instanceof int[]){
                return (19 << 26) | addr;
            }else if(obj instanceof Object[]){
                return (20 << 26) | addr;
            }else{
                return (4 << 26) | addr;
            }
        }
    }

    /**
     * 在一个顺序排列的条件值和地址对应表中查找指定条件对应的地址。
     * @param data 代码数据
     * @param pos 查找表开始位置
     * @param count 查找表中包含数据数量
     * @param byteLen 每个数据的条件值的字节位数
     * @param compare 用于比较的条件值
     * @return 如果找到，返回找到的地址（2字节），否则返回-1
     */
    protected int searchTable(byte[] data, int pos, int count, int byteLen, int compare){
        int start = 0;
        int end = count - 1;
        while(start <= end){
            int mid = (start + end) >> 1;
            int pos2 = pos + mid * (byteLen + 2);
            int cmp;
            if(byteLen == 1){
                cmp = data[pos2];
            }else if(byteLen == 2){
                cmp = Tool.getShort(data, pos2);
            }else{
                cmp = Tool.getInt(data, pos2);
            }
            if(cmp == compare){
                return Tool.getShort(data, pos2 + byteLen);
            }else if(cmp < compare){
                start = mid + 1;
            }else{
                end = mid - 1;
            }
        }
        return -1;
    }

    /**
     * 系统调用参数数组
     */
    private int[] syscallParams = new int[20];
    /**
     * 执行当前函数的全部代码。
     * @param ignoreBlock 是否忽略暂停状态强制执行
     */
    public void processInst(boolean ignoreBlock) throws Exception{
        int[] functions = libraries[currentVM].functions;
        byte[] codeData = libraries[currentVM].codeData;
        int eipmax = functions[funcBase + 2];
        while(eip < eipmax){
            if(!ignoreBlock && blocked){
                blockPosition = saveStack();
                break;
            }
            byte inst = codeData[eip];
            switch(inst){
                case ADD:
                    stack[esp - 1] = stack[esp - 1] + stack[esp];
                    break;
                case SUB:
                    stack[esp - 1] = stack[esp - 1] - stack[esp];
                    break;
                case MUL:
                    stack[esp - 1] = stack[esp - 1] * stack[esp];
                    break;
                case DIV:
                    stack[esp - 1] = stack[esp - 1] / stack[esp];
                    break;
                case MOD:
                    stack[esp - 1] = stack[esp - 1] % stack[esp];
                    break;
                case AND:
                    stack[esp - 1] = (stack[esp - 1] != FALSE && stack[esp] != FALSE)? TRUE: FALSE;
                    break;
                case OR:
                    stack[esp - 1] = (stack[esp - 1] != FALSE || stack[esp] != FALSE)? TRUE: FALSE;
                    break;
                case ANDB:
                    stack[esp - 1] = stack[esp - 1] & stack[esp];
                    break;
                case ORB:
                    stack[esp - 1] = stack[esp - 1] | stack[esp];
                    break;
                case LSHIFT:
                    stack[esp - 1] = stack[esp - 1] << stack[esp];
                    break;
                case RSHIFT:
                    stack[esp - 1] = stack[esp - 1] >> stack[esp];
                    break;
                case INCV:
                    staticHeap[Tool.getInt(codeData, eip + 1)]++;
                    if(ownerType == OWNER_TYPE_QUEST){
                        Tool.sendSyncVMVarialbe(((Quest)owner).id, Tool.getInt(codeData, eip + 1), staticHeap[Tool.getInt(codeData, eip + 1)]);
                    }
                    break;
                case ADDV8:
                    stack[esp + 1] = staticHeap[Tool.getInt(codeData, eip + 1)] + codeData[eip + 5];
                    break;
                case SUBV8:
                    stack[esp + 1] = staticHeap[Tool.getInt(codeData, eip + 1)] - codeData[eip + 5];
                    break;
                case INCVS:
                    stack[stackBase + Tool.getInt(codeData, eip + 1)]++;
                    break;
                case ADDV8S:
                    stack[esp + 1] = stack[stackBase + Tool.getInt(codeData, eip + 1)] + codeData[eip + 5];
                    break;
                case SUBV8S:
                    stack[esp + 1] = stack[stackBase + Tool.getInt(codeData, eip + 1)] - codeData[eip + 5];
                    break;
                case EQ:
                    stack[esp - 1] = (stack[esp - 1] == stack[esp])? TRUE: FALSE;
                    break;
                case GT:
                    stack[esp - 1] = (stack[esp - 1] > stack[esp])? TRUE: FALSE;
                    break;
                case LT:
                    stack[esp - 1] = (stack[esp - 1] < stack[esp])? TRUE: FALSE;
                    break;
                case EQ8:
                    stack[esp] = (stack[esp] == codeData[eip + 1])? TRUE: FALSE;
                    break;
                case GT8:
                    stack[esp] = (stack[esp] > codeData[eip + 1])? TRUE: FALSE;
                    break;
                case LT8:
                    stack[esp] = (stack[esp] < codeData[eip + 1])? TRUE: FALSE;
                    break;
                case NE8:
                    stack[esp] = (stack[esp] == codeData[eip + 1])? FALSE: TRUE;
                    break;
                case JMP:
                    eip = functions[funcBase + 1] + (Tool.getShort(codeData, eip + 1) & 0xFFFF);
                    continue;
                case JEQ:
                    if(stack[esp] != FALSE){
                        eip = functions[funcBase + 1] + (Tool.getShort(codeData, eip + 1) & 0xFFFF);
                        esp--;
                        continue;
                    }
                    break;
                case JNE:
                    if(stack[esp] == FALSE){
                        eip = functions[funcBase + 1] + (Tool.getShort(codeData, eip + 1) & 0xFFFF);
                        esp--;
                        continue;
                    }
                    break;
                case CALL:
                case CALLPTR:
                {
                    // 执行到这里时，参数应该都已经压栈了
                    int parCount = codeData[eip + 1] & 0xFF;
                    int callFunc;
                    if (inst == CALL) {
                        callFunc = Tool.getShort(codeData, eip + 2) & 0xFFFF;
                    } else {
                        callFunc = stack[esp] & 0xFFFF;
                        esp--;
                    }
                    int callVM = 0;
                    if ((callFunc & 0xF000) != 0) {
                        // 如果从库中调用另外一个库，这里存储的是相对库本身的库索引，需要转换一下
                        // 如果是CALLPTR指令，那么传入的函数指针在LOADFUNC时已经转换过了
                        callVM = (callFunc & 0xF000) >> 12;
                        if (currentVM != 0 && inst == CALL) {
                            VM nextVM = libraries[currentVM].libraries[callVM];
                            for (int i = 0; i < libraries.length; i++) {
                                if (nextVM == libraries[i]) {
                                    callVM = i;
                                    break;
                                }
                            }
                        }
                        functions = libraries[callVM].functions;
                        codeData = libraries[callVM].codeData;
                        callFunc &= 0x0FFF;
                    } else {
                    	if (inst == CALL) {
                    		callVM = currentVM;
                    	} else {
                    		// 如果从库中通过函数指针调用原始脚本中的函数，切换运行环境到索引0
                    		callVM = 0;
                            functions = libraries[callVM].functions;
                            codeData = libraries[callVM].codeData;
                    	}
                    }
                    int newStackBase = esp - parCount + 1;

                    // 首先预留局部变量位置，局部变量初始值设置为0
                    int localParamCount = functions[callFunc * 3] & 0xFFFF;
                    for(int ii = esp + 1; ii <= esp + localParamCount; ii++){
                        stack[ii] = 0;
                    }
                    esp += localParamCount;

                    // 然后一次压入，当前栈底指针、当前VM、函数ID、EIP
                    stack[esp + 1] = stackBase;
                    stack[esp + 2] = currentVM;
                    stack[esp + 3] = currentFunc;
                    if (inst == CALL) {
                        stack[esp + 4] = eip + 4;
                    } else {
                        stack[esp + 4] = eip + 2;
                    }
                    esp += 4;

                    // 切换运行上下文
                    stackBase = newStackBase;
                    currentVM = callVM;
                    callCount++;
                    currentFunc = callFunc;
                    funcBase = currentFunc * 3;
                    eip = functions[funcBase + 1];
                    eipmax = functions[funcBase + 2];
                    continue;
                }
                case RET: {
                    if (callCount == 0) {
                        // 栈空说明是系统函数，直接退出本次执行
                        return;
                    }
                    eip = stack[esp];
                    currentFunc = stack[esp - 1];
                    currentVM = stack[esp - 2];
                    functions = libraries[currentVM].functions;
                    codeData = libraries[currentVM].codeData;
                    int newStackBase = stackBase;
                    stackBase = stack[esp - 3];
                    callCount--;
                    esp = newStackBase - 1;
                    funcBase = currentFunc * 3;
                    eipmax = functions[funcBase + 2];
                    continue;
                }
                case VRET: {
                    if (callCount == 0) {
                        // 栈空说明是系统函数，直接退出本次执行
                        return;
                    }
                    int retValue = stack[esp];
                    eip = stack[esp - 1];
                    currentFunc = stack[esp - 2];
                    currentVM = stack[esp - 3];
                    functions = libraries[currentVM].functions;
                    codeData = libraries[currentVM].codeData;
                    int newStackBase = stackBase;
                    stackBase = stack[esp - 4];
                    callCount--;
                    esp = newStackBase;
                    stack[esp] = retValue;
                    funcBase = currentFunc * 3;
                    eipmax = functions[funcBase + 2];
                    continue;
                }
                case SYSCALL: 
                case SYSCALLSAVEVS:
                {
                    short callFunc = Tool.getShort(codeData, eip + 1);
                    int parCount = codeData[eip + 3] & 0xFF;
                    boolean hasRet = codeData[eip + 4] == (byte)1;
                    if(syscallParams.length < parCount){
                    	syscallParams = new int[parCount];
                    }
                    System.arraycopy(stack, esp - parCount + 1, syscallParams, 0, parCount);
                    esp -= parCount;
                    int ret;
                    try{
                        ret = syscall(callFunc, syscallParams);
                    }catch(Throwable e){
                    	//#ifdef buildtest
                        e.printStackTrace();
                      //#endif
                        ret = 0;
                    }
                    if (hasRet) {
                        if (inst == SYSCALLSAVEVS) {
                            stack[stackBase + Tool.getInt(codeData, eip + 5)] = ret;
                        } else {
                            stack[esp + 1] = ret;
                            esp++;
                        }
                    }
                    break;
                }
                case TSWITCH: {
                    // 数组索引跳转，2字节缺省分支地址（相对这条指令的地址），4字节表头，4字节表尾（包含），
                    // 所有分支的跳转地址（按分支条件从小到大的顺序，相对地址，每个地址2字节，数量=表尾-表头+1）
                    int first = Tool.getInt(codeData, eip + 3);
                    int last = Tool.getInt(codeData, eip + 7);
                    int cond = stack[esp];
                    int instLen = 11 + 2 * (last - first + 1);
                    if(cond >= first && cond <= last){
                        int off = Tool.getShort(codeData, eip + 11 + (cond - first) * 2) & 0xFFFF;
                        if(off == 0xFFFF){
                            eip += Tool.getShort(codeData, eip + 1) & 0xFFFF;
                        }else{
                            eip += off;
                        }
                    }else{
                        eip += Tool.getShort(codeData, eip + 1) & 0xFFFF;
                    }
                    eip += instLen;
                    break;
                }
                case LSWITCH: {
                    // 列表查找索引跳转，2字节缺货分支地址（相对这条指令的地址），2字节分支数量，分支条件字节
                    // 数（1，2或4），分支数据（条件1、地址1、条件2、地址2......，条件按从小到大的顺序排列）
                    int switchCount = Tool.getShort(codeData, eip + 3);
                    int condBytes = codeData[eip + 5];
                    int cond = stack[esp];
                    int instLen = 6 + switchCount * (condBytes + 2);
                    int addr = searchTable(codeData, eip + 6, switchCount, condBytes, cond);
                    if(addr >= 0){
                        eip += addr;
                    }else{
                        eip += Tool.getShort(codeData, eip + 1) & 0xFFFF;
                    }
                    eip += instLen;
                    break;
                }
                case LOAD:
                    stack[esp] = memLoad(stack[esp]);
                    break;
                case SAVE:
                    memSave(stack[esp], stack[esp - 1]);
                    break;
                case LOADV:
                    stack[esp + 1] = staticHeap[Tool.getInt(codeData, eip + 1)];
                    break;
                case SAVEV:
                    staticHeap[Tool.getInt(codeData, eip + 1)] = stack[esp];
                    if(ownerType == OWNER_TYPE_QUEST){
                        Tool.sendSyncVMVarialbe(((Quest)owner).id, Tool.getInt(codeData, eip + 1), stack[esp]);
                    }
                    break;
                case LOADVS:
                    stack[esp + 1] = stack[stackBase + Tool.getInt(codeData, eip + 1)];
                    break;
                case SAVEVS:
                    stack[stackBase + Tool.getInt(codeData, eip + 1)] = stack[esp];
                    break;
                case DUP:
                    stack[esp + 1] = stack[esp - codeData[eip + 1]];
                    break;
                case LOAD8:
                    stack[esp + 1] = codeData[eip + 1];
                    break;
                case LOAD16:
                    stack[esp + 1] = Tool.getShort(codeData, eip + 1);
                    break;
                case LOAD32:
                    stack[esp + 1] = Tool.getInt(codeData, eip + 1);
                    break;
                case ALOAD:
                    stack[esp - 1] = arrLoad(stack[esp - 1], stack[esp]);
                    break;
                case ASAVE:
                    arrSave(stack[esp - 1], stack[esp], stack[esp - 2]);
                    break;
                case ALOAD8:
                    stack[esp] = arrLoad(stack[esp], codeData[eip + 1]);
                    break;
                case ASAVE8:
                    arrSave(stack[esp], codeData[eip + 1], stack[esp - 1]);
                    break;
                case ALLOC:
                    stack[esp] = alloc(codeData[eip + 1], stack[esp]);
                    break;
                case FREE:
                    free(stack[esp]);
                    break;
                case STALLOC:
                    stack[esp + 1] = makeTempObject(new int[Tool.getShort(codeData, eip + 1)]);
                    break;
                case STLOAD: {
                    int[] arr = (int[])followPointer(stack[esp - 1]);
                    stack[esp - 1] = arr[stack[esp] & 0x3FFFFFFF];
                    break;
                }
                case STSAVE: {
                    int[] arr = (int[])followPointer(stack[esp - 1]);
                    int memberAddr = stack[esp];
                    int saveValue = stack[esp - 2];
                    arr[memberAddr & 0x3FFFFFFF] = saveValue;
                    break;
                }
                case STLOAD8: {
                    int[] arr = (int[])followPointer(stack[esp]);
                    stack[esp] = arr[codeData[eip + 1]];
                    break;
                }
                case STSAVE8: {
                    int[] arr = (int[])followPointer(stack[esp]);
                    int memberAddr = codeData[eip + 1];
                    int saveValue = stack[esp - 1];
                    arr[memberAddr & 0x3FFFFFFF] = saveValue;
                    break;
                }
                case LOADFUNC: {
                    short funcID = Tool.getShort(codeData, eip + 1);
                    
                    // 如果从库中取的函数指针，这里高4位存储的是相对库本身的库索引，需要转换一下
                    if (currentVM != 0) {
                        int callVM = (funcID & 0xF000) >> 12;
                        VM nextVM = libraries[currentVM].libraries[callVM];
                        for (int i = 0; i < libraries.length; i++) {
                            if (nextVM == libraries[i]) {
                                callVM = i;
                                break;
                            }
                        }
                        funcID = (short)((funcID & 0xFFF) | (callVM << 12));
                    }
                    stack[esp + 1] = funcID;
                    break;
                }
                case LOADVS3:
                    stack[esp + 1] = stack[stackBase + Tool.getInt(codeData, eip + 1)];
                    stack[esp + 2] = stack[stackBase + Tool.getInt(codeData, eip + 5)];
                    stack[esp + 3] = stack[stackBase + Tool.getInt(codeData, eip + 9)];
                    break;
                case LOADVS2:
                    stack[esp + 1] = stack[stackBase + Tool.getInt(codeData, eip + 1)];
                    stack[esp + 2] = stack[stackBase + Tool.getInt(codeData, eip + 5)];
                    break;
                case LOAD88:
                    stack[esp + 1] = codeData[eip + 1];
                    stack[esp + 2] = codeData[eip + 2];
                    break;
                case LOAD8VS:
                    stack[esp + 1] = codeData[eip + 1];
                    stack[esp + 2] = stack[stackBase + Tool.getInt(codeData, eip + 2)];
                    break;
                case LOADVS8:
                    stack[esp + 1] = stack[stackBase + Tool.getInt(codeData, eip + 1)];
                    stack[esp + 2] = codeData[eip + 5];
                    break;
                case LOADVSSTLOAD8: {
                    int ptr = stack[stackBase + Tool.getInt(codeData, eip + 1)];
                    int[] arr = (int[])followPointer(ptr);
                    stack[esp + 1] = arr[codeData[eip + 5]];
                    break;
                }
                case LOAD8VSSTLOAD8: {
                    stack[esp + 1] = codeData[eip + 1];
                    int ptr = stack[stackBase + Tool.getInt(codeData, eip + 2)];
                    int[] arr = (int[])followPointer(ptr);
                    stack[esp + 2] = arr[codeData[eip + 6]];
                    break;
                }
                case LOADVSADDALOAD: {
                    int value = stack[esp] + stack[stackBase + Tool.getInt(codeData, eip + 1)];
                    stack[esp - 1] = arrLoad(stack[esp - 1], value);
                    break;
                }
                case LOADVSALOAD: {
                    int value = stack[stackBase + Tool.getInt(codeData, eip + 1)];
                    stack[esp] = arrLoad(stack[esp], value);
                    break;
                }
            }
            esp += STACK_EFFECT[inst & 0xFF];
            eip += INSTRUCTION_LENGTH[inst & 0xFF];
        }
    }

    /**
     * 调用脚本中的一个函数，一直执行到被调用的函数返回。
     */
    public synchronized void execute(int funcID){
        execute(funcID, (int[])null);
    }

    public synchronized void execute(int funcID, int[] params){
        if (running) {
            return;
        }
        try{
            running = true;
            if(resumeFlag && funcID == CYCLEUI){
                resumeFlag = false;
                resume();
            }else if(!blocked || funcID != CYCLEUI){
                currentVM = (funcID >> 12) & 0x0F;
                currentFunc = funcID & 0xFFF;
                funcBase = currentFunc * 3;

                // 根据params来设置栈初始值
                int paramCount = 0;
                if(params != null){
                    System.arraycopy(params, 0, stack, 0, params.length);
                    paramCount += params.length;
                }

                int lcount = libraries[currentVM].functions[funcBase] & 0xFFFF;
                esp = -1 + lcount + paramCount;
                stackBase = 0;
                callCount = 0;

                // 局部变量初始值设置为0
                for(int i = 0; i < lcount; i++){
                    stack[i + paramCount] = 0;
                }

                eip = libraries[currentVM].functions[funcBase + 1];
                processInst(blocked);
            }
        }catch(Exception e){
        	//#ifdef buildtest
            e.printStackTrace();
          //#endif
        } finally {
            running = false;
        }
    }

    /**
     * 通过名字调用回调函数。回调函数是使用CALLBACK关键字定义的函数。
     * @param funcName
     * @param params
     */
    public synchronized int callback(String funcName, int[] params){
        int funcId = -1;
        for (int i = 0; i < libraries.length; i++) {
            Short obj = (Short)libraries[i].callbacks.get(funcName);
            if (obj != null) {
                funcId = (i << 12) + obj.shortValue();
            }
        }
        if (funcId != -1) {
            return callback(funcId, params);
        }
        return 0;
    }
    
    /**
     * 通过函数ID直接调用函数。
     * @param funcId
     * @param params
     * @return
     */
    public synchronized int callback(int funcId, int[] params){
        try{
            if (!running) {
                // 普通回调逻辑
                execute(funcId, params);
                if(esp < 0){
                    return 0;
                }else{
                    return stack[esp];
                }
            } else {
                // 如果当前正在执行，则需要保存当前运行环境，等回调函数执行完后再恢复
                int[] oldStack = saveStack();
                running = false;
                execute(funcId, params);
                int ret = 0;
                if(esp >= 0){
                    ret = stack[esp];
                }
                restoreStack(oldStack);
                running = true;
                return ret;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return 0;
    }

    public int getRealizeAdrr(int objAdrr) {
    	int ret = 0;
    	try {
    		Object obj = followPointer(objAdrr);
            if(obj == null){
                return 0;
            }else{
                boolean isTemp = ((objAdrr & 0x80000000) == 0) && ((objAdrr & 0xFFF) < tempSpace);
                if(isTemp){
                    dynamicHeap[objAdrr & 0xFFF] = null;
                }
                int newaddr = heapAlloc();
                dynamicHeap[newaddr] = obj;
                return (objAdrr & 0xFFFFF000) | newaddr;
            }
		} catch (Exception e) {
			//#ifdef buildtest
			e.printStackTrace();
			//#endif
		}
		return ret;
    }
    
    int syscall2(short funcID, int[] params) throws Exception{
    	switch(funcID){
            case 0x0001: // 0x0001=boolean KeyPressed(byte keyCode, byte clear) 按键
                return KeyPressed((byte)params[0], (byte)params[1])? 1: 0;
            case 0x0002: // 0x0002=boolean NoKeyPressed() 按键
                return NoKeyPressed()? 1: 0;
            case 0x0003: // 0x0003=short Random() 取随机数
                return getNextRnd(0, 10000);
            case 0x0004: // 0x0004=int GetTime() 返回从游戏开始时计算的时间（毫秒）
                return Utilities.getTimeStamp();
            case 0x0005: // 0x0005=int StrToInt(String str) 将字符串变成 int
            {
                try{
                    String str = (String)followPointer(params[0]);
                    if(str.startsWith("u")){
                        return Integer.parseInt(str.substring(1), 16);
                    }else{
                        return Integer.parseInt(str);
                    }
                }catch(Exception e){
                    return 0;
                }
            }
            case 0x000E: // 0x000E=boolean IsNull(Object obj) 判断对象是否为空 
                return followPointer(params[0]) == null? 1: 0;
            case 0x000F: // 0x000F=String IntToStr(int i) 把int变成String
                return makeTempObject(String.valueOf(params[0]));
            case 0x0010: // 0x0010=Object Realize(Object ref) 把对象存入到固态堆里面去
            {
                Object obj = followPointer(params[0]);
                if(obj == null){
                    return 0;
                }else{
                    boolean isTemp = ((params[0] & 0x80000000) == 0) && ((params[0] & 0xFFF) < tempSpace);
                    if(isTemp){
                        dynamicHeap[params[0] & 0xFFF] = null;
                    }
                    int newaddr = heapAlloc();
                    dynamicHeap[newaddr] = obj;
                    return (params[0] & 0xFFFFF000) | newaddr;
                }
            }
            case 0x0011: // 0x0011=void FillRect(Object g, int x, int y, int width, int height) 填充矩形
            {
                Object g = followPointer(params[0]);
                ((Graphics)g).fillRect(params[1], params[2], params[3], params[4]);
                break;
            }
            case 0x0012: // 0x0012=void DrawString(Object g, String str, int x, int y, int anchor) 画字符串
                DrawString((Graphics)followPointer(params[0]), (String)followPointer(params[1]), params[2], params[3], params[4]);
                break;
            case 0x0013: // 0x0013=void SetColor(Object g, int color) 设置Graphics颜色
            {
                Object g = followPointer(params[0]);
                ((Graphics)g).setColor(params[1]);
                break;
            }
            case 0x0014: // 0x0014=void DrawRect(Object g, int x, int y, int width, int height) 画一个矩形框 
                DrawRect((Graphics)followPointer(params[0]), params[1], params[2], params[3], params[4]);
                break;
            case 0x0015: // 0x0015=void SetClip(Object g, int x, int y, int width, int height) 封闭Graphics的setClip()
            {
                Object obj = followPointer(params[0]);
                if(obj instanceof Graphics){
                    SetClip((Graphics)obj, params[1], params[2], params[3], params[4]);
                }
                break;
            }
            case 0x0016: // 0x0016=int GetScreenWidth() 得到屏幕宽度
                return GetScreenWidth();
            case 0x0017: // 0x0017=int GetScreenHeight() 得到屏幕高度
                return GetScreenHeight();
            case 0x0018: // 0x0018=void DrawRoundRect(Object g, int x, int y, int width, int height, int hr, int vr)
                ((Graphics)followPointer(params[0])).drawRoundRect(params[1], params[2], params[3], params[4], params[5], params[6]);
                break;
            case 0x0019: // 0x0019=Object GetSystemGraphics() 取得当前Graphics对象
                return makeTempObject(Utilities.graphics);
            case 0x001A: {// 0x001A=Object CreateImage(int width, int height) 创建一个内存图片
            	//#if opengl == true
            	//# if(Canvas.openglMode){
            		//# GLGraphics ret = new GLGraphics();
            		//# return makeTempObject(ret);
            	//# } else {
            		//# Image ret = Image.createImage(params[0], params[1]);
            		//# return makeTempObject(ret);
            	//# }
            	//#else
            	Image ret = Image.createImage(params[0], params[1]);
        		return makeTempObject(ret);
            	//#endif
            }
            case 0x001B: // 0x001B=Object GetImageGraphics(Object img)  取得图片的Graphics对象
            {
            	Object img = followPointer(params[0]);
            	//#if opengl == true
                //# if(img instanceof GLGraphics){
                	//# return makeTempObject(img);
                //# } else {
                	//# return makeTempObject(((Image)img).getGraphics());
                //# }
            	//#else
                return makeTempObject(((Image)img).getGraphics());
            	//#endif
            }
            case 0x001C: // 0x001C=int GetFontHeight() 字体高度
                return Utilities.CHAR_HEIGHT;
            case 0x001D: // 0x001D=int StringWidth(String s) 求得字符串在屏幕上的宽度
                return Utilities.font.stringWidth((String)followPointer(params[0]));
            case 0x001E: // 0x001E=void DrawImage(Object g, Object img, int x, int y, int anchor) 画Image对象
            {
            	Object img = followPointer(params[1]);
            	//#if opengl == true
            	//# if(img instanceof GLGraphics){
            		//# GLGraphics g = (GLGraphics)followPointer(params[0]);
            		//# GLGraphics img1 = (GLGraphics)img;
            		//# g.drawBatch2(img1,params[2], params[3]);
            	//# } else {
            		//# ((Graphics)followPointer(params[0])).drawImage((Image)img, params[2], params[3], params[4]);
            	//# }
            	//#else
            	((Graphics)followPointer(params[0])).drawImage((Image)img, params[2], params[3], params[4]);
            	//#endif
                
                
                break;
            }
            case 0x001F: // 0x001F=byte[] String_ToUTF(String str)
            {
                String str = (String) followPointer(params[0]);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(bos);
                dos.writeUTF(str);
                dos.flush();
                byte[] buf = bos.toByteArray();
                byte[] ret = new byte[buf.length - 2];
                System.arraycopy(buf, 2, ret, 0, buf.length - 2);
                return makeTempObject(ret);
            }
            case 0x0020: // 0x0020=int GetLineHeight()
                return Utilities.LINE_HEIGHT;
            case 0x0021: // 0x0021=String Object_Create(Object object) 构造一个Object
                return makeTempObject(followPointer(params[0]));
            case 0x0022: // 0x0022=String String_Append(String obj, String str) 连接字符串
                return makeTempObject(((String)followPointer(params[0])) + ((String)followPointer(params[1])));
            case 0x0023: // 0x0023=int String_Length(String obj) 取得字符串长度
                return ((String)followPointer(params[0])).length();
            case 0x0024: // 0x0024=String String_SubString(String obj, int pos, int length) 取得字符串一部分
                return makeTempObject(((String)followPointer(params[0])).substring(params[1], params[1] + params[2]));
            case 0x0025: // 0x0025=int String_Find(String obj, String str, int from) 在一个字符串中查找另一个字符串
                return ((String)followPointer(params[0])).indexOf((String)followPointer(params[1]), params[2]);
            case 0x0026: // 0x0026=String String_AppendInt(String obj, int intvalue) 把一个String和一个int拼接起来
                return makeTempObject(((String)followPointer(params[0])) + params[1]);
            case 0x0027: // 0x0027=boolean String_Equal(String obj, String str) 判断两个字符串是否一样
                return ((String)followPointer(params[0])).equals(followPointer(params[1]))? TRUE: FALSE;
            case 0x0028: // 0x0028=int String_CharAt(String obj, int index) 得到字符串中某一索引的字符
                return ((String)followPointer(params[0])).charAt(params[1]);
            case 0x0029: // 0x0029=String String_Trim(String obj) 去掉字符串前后的空格
                return makeTempObject(((String)followPointer(params[0])).trim());
            case 0x002A: // 0x002A=String String_Create2(byte[] data, String encoding) 从byte[]中创建字符串
                return makeTempObject(new String((byte[])followPointer(params[0]), (String)followPointer(params[1])));
            case 0x002B: // 0x002B=String String_ReplaceChar(String str, String src, String dest) 替换字符
            {
                String str = (String)followPointer(params[0]);
                char src = ((String)followPointer(params[1])).charAt(0);
                char dest = ((String)followPointer(params[2])).charAt(0);
                return makeTempObject(str.replace(src, dest));
            }
            case 0x002C: // 0x002C=String String_AppendChar(String str, int ch)
            {
                String str = (String)followPointer(params[0]);
                return makeTempObject(str + (char)params[1]);
            }
            case 0x002D: // 0x002D=String String_InsertString(String str, int pos, String insertString) 在指定位置插入一个字符串
            {
                String str = (String)followPointer(params[0]);
                return makeTempObject(str.substring(0, params[1]) + (String)followPointer(params[2]) +
                		str.substring(params[1], str.length()));
            }
            case 0x002E: // 0x002E=boolean String_StartsWith(String str, String prefix)
            {
            	String str = (String)followPointer(params[0]);
                return str.startsWith((String)followPointer(params[1]))? 1 : 0;
            }
            case 0x002F: // 0x002F=String String_Merge(Vector v)
            {
                return makeTempObject(Tool.mergeString((Vector)followPointer(params[0])));
            }
            case 0x0030: // 0x0030=boolean String_EndsWith(String str, String endfix)
            {
            	String str = (String)followPointer(params[0]);
                return str.endsWith((String)followPointer(params[1]))? 1 : 0;
            }
            case 0x0031: // 0x0031=Object AnimateSet_Create(ImageSet[] imgs, String ctnFile)
            {
                Object[] objs = (Object[])followPointer(params[0]);
                ImageSet[] imgs = new ImageSet[objs.length];
                System.arraycopy(objs, 0, imgs, 0, objs.length);
                String ctnFile = (String)followPointer(params[1]);
                PipAnimateSet ani = new PipAnimateSet(imgs, GameMain.resourceManager.findResource(ctnFile));
                ani.fileName = ctnFile;
                return makeTempObject(ani);
            }
            case 0x0032: // 0x0032=void AnimateSet_DrawFrame(Object set, Object g, int frame, int x, int y)
                ((PipAnimateSet)followPointer(params[0])).drawFrame((Graphics)followPointer(params[1]), params[2], params[3], params[4]);
                break;
            case 0x0033: // 0x0033=void AnimateSet_DrawAnimate(Object set, Object g, int index, int tick, int x, int y)
                ((PipAnimateSet)followPointer(params[0])).drawAnimateFrame((Graphics)followPointer(params[1]), params[2], params[3], params[4], params[5]);
                break;
            case 0x0034: // 0x0034=int AnimateSet_GetAnimateLength(Object set, int index)
                return ((PipAnimateSet)followPointer(params[0])).getAnimateLength(params[1]);
            case 0x0035: //  0x0035=Object AnimateSet_Create2(ImageSet[] imgs, byte[] ctnData)
                Object[] objs = (Object[])followPointer(params[0]);
                ImageSet[] imgs = new ImageSet[objs.length];
                System.arraycopy(objs, 0, imgs, 0, objs.length);
                byte[] datas = (byte[])followPointer(params[1]);
                return makeTempObject(new PipAnimateSet(imgs, datas));
            case 0x0036: //0x0034=Object AnimateSet_GetAnimateBox(Object set, int index)
            {
                int[] tmp = new int[4];
                return makeTempObject(((PipAnimateSet)followPointer(params[0])).getAnimateBox(tmp, params[1]));
            }
            case 0x0041: // 0x0041=ImageSet ImageSet_Create(String fileName)
                try {
                	String fileName = (String)followPointer(params[0]);
                	ImageSet ret = new ImageSet(fileName);
                	ret.fileName = fileName;
                	if(ret.pipImg != null){
                		ret.pipImg.fileName = fileName;
                	}
                	//#if opengl == true
                	//# if(Canvas.openglMode){
                		//# ret.bindTexture(Canvas.GL_POOL_MISC, fileName);
                	//# }
                	//#endif
                    return makeTempObject(ret);
                } catch (Exception e) {
                	//#ifdef buildtest
                    e.printStackTrace();
                  //#endif
                    return 0;
                }
            case 0x0042: // 0x0042=ImageSet ImageSet_Create1(String fileName, int rows, int cols)
                try {
                    return makeTempObject(new ImageSet((String)followPointer(params[0]), params[1], params[2]));
                } catch (Exception e) {
                	//#ifdef buildtest
                    e.printStackTrace();
                  //#endif
                    return 0;
                }
            case 0x0043: // 0x0043=ImageSet ImageSet_Create2(String fileName)
                try {
                	String fileName = (String)followPointer(params[0]);
                	ImageSet ret = new ImageSet(fileName);
                	ret.fileName = fileName;
            		//#if opengl == true
                	//# if(Canvas.openglMode){
                		//# ret.bindTexture("misc", fileName);
                	//# }
                	//#endif
                    return makeTempObject(ret);
                } catch (Exception e) {
                	//#ifdef buildtest
                    e.printStackTrace();
                  //#endif
                    return 0;
                }
            case 0x0044: // 0x0044=ImageSet ImageSet_Create4(byte[] imgData)
                try {
                	ImageSet ret = new ImageSet((byte[])followPointer(params[0]));
                	//#if opengl == true
                	//# if(Canvas.openglMode){
                	//# 	ret.bindTexture("misc", String.valueOf(System.currentTimeMillis()));
                	//# }
                	//#endif
                    return makeTempObject(ret);
                } catch (Exception e) {
                	//#ifdef buildtest
                    e.printStackTrace();
                  //#endif
                    return 0;
                }
            case 0x0045: // 0x0045=void ImageSet_DrawFrame(ImageSet obj, Object g, int frame, int x, int y, int anchor) 画图片的某一帧
            {
                Object img = followPointer(params[0]);
                ((ImageSet)img).drawFrame((Graphics)followPointer(params[1]), params[2], params[3], params[4], 0, params[5]);
                break;
            }
            case 0x0046: // 0x0046=int ImageSet_GetFrameWidth(ImageSet obj, int frame) 得到某一帧的高度
            {
                Object img = followPointer(params[0]);
                return ((ImageSet)img).getFrameWidth(params[1]);
            }
            case 0x0047: // 0x0047=int ImageSet_GetFrameHeight(ImageSet obj, int frame) 得到某一帧的宽度
            {
                Object img = followPointer(params[0]);
                return ((ImageSet)img).getFrameHeight(params[1]);
            }
            case 0x0048: // 0x0048=void ImageSet_Gray(ImageSet obj) 使图片变灰
            {
                ImageSet img = (ImageSet)followPointer(params[0]);
                if(img.pipImg != null){
                    img.pipImg.gray(); //TODO 变成灰色图
                }
                break;
            }
            case 0x0049: // 0x0049=void ImageSet_Lighter(ImageSet obj, int v)
            {
                ImageSet img = (ImageSet)followPointer(params[0]);
                if(img.pipImg != null){
                    img.pipImg.lighter(params[1]); // TODO 变亮
                }
                break;
            }
            case 0x004A: // 0x004A=void ImageSet_Darker(ImageSet obj, int v)
            {
                ImageSet img = (ImageSet)followPointer(params[0]);
                if(img.pipImg != null){
                    img.pipImg.darker(params[1]); //TODO 变暗
                }
                break;
            }
            case 0x004B: // 0x004B=void ImageSet_Mask(ImageSet obj, int v)
            {
                ImageSet img = (ImageSet)followPointer(params[0]);
                if(img.pipImg != null){
                    img.pipImg.mask(params[1]); //TODO 加层膜
                }
                break;
            }
            case 0x004C: // 0x004C=void ImageSet_DrawFrame2(ImageSet obj, Object g, int frame, int x, int y, int trans, int anchor) //画图片一帧,带翻转
                ((ImageSet)followPointer(params[0])).drawFrame((Graphics)followPointer(params[1]), params[2], params[3], params[4], params[5], params[6]);
                break;
            case 0x004F: //0x004F=int Stream_ReadUByte(Stream s)
                return ((DataInputStream)followPointer(params[0])).readUnsignedByte();
            case 0x0050: //0x0050=int Stream_ReadUShort(Stream s)
                return ((DataInputStream)followPointer(params[0])).readUnsignedShort();
            case 0x0051: // 0x0051=Stream Stream_Create(byte[] buf)
            	//#if NewUI2
            	return makeTempObject(new DataInputStream(new BufferedInputStream(new ByteArrayInputStream((byte[])followPointer(params[0])))));
            	//#else
            	//# return makeTempObject(new DataInputStream(new ByteArrayInputStream((byte[])followPointer(params[0]))));
            	//#endif
            case 0x0052: // 0x0052=Stream Stream_Create2()
                return makeTempObject(new ByteArrayOutputStream());
            case 0x0053: // 0x0053=int Stream_ReadInt(Stream s)
                return ((DataInputStream)followPointer(params[0])).readInt();
            case 0x0054: // 0x0054=short Stream_ReadShort(Stream s)
                return ((DataInputStream)followPointer(params[0])).readShort();
            case 0x0055: // 0x0055=byte Stream_ReadByte(Stream s)
                return ((DataInputStream)followPointer(params[0])).readByte();
            case 0x0056: // 0x0056=boolean Stream_ReadBoolean(Stream s)
                return ((DataInputStream)followPointer(params[0])).readBoolean()? 1: 0;
            case 0x0057: // 0x0057=String Stream_ReadUTF(Stream s)
                return makeTempObject(((DataInputStream)followPointer(params[0])).readUTF());
            case 0x0058: // 0x0058=void Stream_WriteInt(Stream s, int i)
                new DataOutputStream((ByteArrayOutputStream)followPointer(params[0])).writeInt(params[1]);
                break;
            case 0x0059: // 0x0059=void Stream_WriteShort(Stream s, short s)
                new DataOutputStream((ByteArrayOutputStream)followPointer(params[0])).writeShort((short)params[1]);
                break;
            case 0x005A: // 0x005A=void Stream_WriteUTF(Stream s, String s)
                new DataOutputStream((ByteArrayOutputStream)followPointer(params[0])).writeUTF((String)followPointer(params[1]));
                break;
            case 0x005B: // 0x005B=void Stream_WriteByte(Stream s, byte b)
                new DataOutputStream((ByteArrayOutputStream)followPointer(params[0])).writeByte((byte)params[1]);
                break;
            case 0x005C: // 0x005C=void Stream_WriteBoolean(Stream s, boolean b)
                new DataOutputStream((ByteArrayOutputStream)followPointer(params[0])).writeBoolean(params[1] != 0);
                break;
            case 0x005D: // 0x005D=int Stream_Length(Stream s)
                return ((ByteArrayOutputStream)followPointer(params[0])).size();
            case 0x005E: // 0x005E=byte[] Stream_ToBytes(Stream s)
                return makeTempObject(((ByteArrayOutputStream)followPointer(params[0])).toByteArray());
            case 0x005F: // 0x005F=void Stream_ReadBytes(Stream s, byte[] bs)
                ((DataInputStream)followPointer(params[0])).readFully((byte[])followPointer(params[1]));
                break;
            case 0x0060: // 0x0060=void Stream_WriteBytes(Stream s, byte[] bs)
                new DataOutputStream((ByteArrayOutputStream)followPointer(params[0])).write((byte[])followPointer(params[1]));
                break;
            case 0x0061: // 0x0061=Object Form_Create(String title)
            {
                Form form = new Form((String)followPointer(params[0]));
                form.setCommandListener(this);
                return makeTempObject(form);
            }
            case 0x0062: // 0x0062=void Form_AppendTextField(Object form, String name, String value, int maxLength, int type)
            {
                Form form = (Form)followPointer(params[0]);
                String name = (String)followPointer(params[1]);
                String value = (String)followPointer(params[2]);
                form.append(new TextField(name, value, params[3], params[4]));
                break;
            }
            case 0x0063: // 0x0063=void Form_AddCommand(Object form, String text, int type, int priority)
            {
                Form form = (Form)followPointer(params[0]);
                String text = (String)followPointer(params[1]);
                Command cmd = new Command(text, params[2], params[3]);
                form.addCommand(cmd);
                break;
            }
            case 0x0064: // 0x0064=void Form_Show(Object form)
            {
            	//#if (ModelID == Android || ModelID == Lenovo || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad || ModelID == AndroidSmall || ModelID == AndroidAuto) && (KeyCodeType == XperiaPlay)
            	 //# final Form form = (Form)followPointer(params[0]);
              //# MIDlet.DEFAULT_MIDLET.invokeAndWait(new Runnable() {
              //# 	public void run() {
              //# 		PipActivity.DEFAULT_ACTIVITY.showFormActivity(form);
              //# 	}
              //# });
//#             	
            	//#else
               Form form = (Form)followPointer(params[0]);
                GameMain.display.setCurrent(form);
            	//#endif
                
                break;
            }
            case 0x0065: // 0x0065=String Form_GetFieldText(Object form, int index)
            {
                Form form = (Form)followPointer(params[0]);
                String txt = ((TextField)form.get(params[1])).getString();
                if(txt == null){
                    txt = "";
                }
                return makeTempObject(txt);
            }
            case 0x0066: // 0x0066=void TextField_SetText(Object form, int index, String text)
            {	
            	
                Form form = (Form)followPointer(params[0]);
                TextField tf = ((TextField)form.get(params[1]));
                String txt = (String)followPointer(params[2]);
            	if(txt != null) {
            		tf.setString(txt);
            	}         
                
                break;
            }
            case 0x0067: // 0x0067=String Form_GetLastSelection()
                return makeTempObject(lastFormSelection);
            case 0x0068: // 0x0068=void Form_AppendChoiceGroup(Object form, String label, int choiceType, String[] choices)
            {
                Form form = (Form)followPointer(params[0]);
                String label = (String)followPointer(params[1]);
                String[] choices = (String[])followPointer(params[3]);
                ChoiceGroup cg = new ChoiceGroup(label, params[2], choices, null);
                form.append(cg);
                break;
            }
            case 0x0069: // 0x0069=void Form_SetChoiceSelection(Object form, int fieldIndex, boolean[] flags)
            {
                Form form = (Form)followPointer(params[0]);
                ChoiceGroup cg = (ChoiceGroup)form.get(params[1]);
                boolean[] flags = (boolean[])followPointer(params[2]);
                cg.setSelectedFlags(flags);
                break;
            }
            case 0x006A: // 0x006A=boolean[] Form_GetChoiceSelection(Object form, int fieldIndex)
            {
                Form form = (Form)followPointer(params[0]);
                ChoiceGroup cg = (ChoiceGroup)form.get(params[1]);
                boolean[] flags = new boolean[cg.size()];
                cg.getSelectedFlags(flags);
                return makeTempObject(flags);
            }
            case 0x006B: // 0x006B=void Form_InsertStringItem(Object form, int fieldIndex, String lable, String text)
            {
                Form form = (Form)followPointer(params[0]);
                String label = (String)followPointer(params[2]);
                String text = (String)followPointer(params[3]);
                form.insert(params[1], new StringItem(label, text));
                break;
            }
            case 0x006C: // 0x006C=void Show_SysAlert(String title, String alertText, int timeout)
            {
                Alert alert = new Alert((String)followPointer(params[0]), (String)followPointer(params[1]), null, AlertType.INFO);
                alert.setTimeout(params[2]);
                alert.setCommandListener(this); 
                //#if ModelID == AndroidAuto
                 //# alert.addCommand(new Command(PipActivity.DEFAULT_ACTIVITY.getString(com.pip.android.R.string.str_ok), Command.OK, 0));
                //#else
                alert.addCommand(new Command("确认", Command.OK, 0));
                //#endif
                GameMain.display.setCurrent(alert);
                break;
            }
//#if NewUI2
            //# case 0x006D: // 0x006D=boolean Form_DirectGetInput(int constraints, String initValue, int maxSize)
	        //# {
	        	//# SanguoMIDlet.display.getInput(params[0], (String)followPointer(params[1]), params[2], this);
	        	//# return TRUE;
	        //# }
	        //# case 0x006E: // 0x006E=String Form_GetLastDirectInput()
	        //# {
	        	//# return makeTempObject(lastInputText);
	        //# }
//#endif
            case 0x0070: // 0x0070=int TextField_GetCaretPosition(Object form, int index)
            {	
            	Form form = (Form)followPointer(params[0]);
            	TextField tf = ((TextField)form.get(params[1]));
            	return tf.getCaretPosition();
            }
            case 0x0071: // 0x0071=UWAPSegment UWAP_Create(int type, boolean needSerial)
                return makeTempObject(new UASegment((short)params[0], params[1] == TRUE));
            case 0x0072: // 0x0072=int UWAP_GetType(UWAPSegment us)
                return ((UASegment)followPointer(params[0])).type;
            case 0x0073: // 0x0073=void UWAP_Reset(UWAPSegment us)
                ((UASegment)followPointer(params[0])).reset();
                break;
            case 0x0074: // 0x0074=int UWAP_ReadInt(UWAPSegment us)
                return ((UASegment)followPointer(params[0])).readInt();
            case 0x0075: // 0x0075=short UWAP_ReadShort(UWAPSegment us)
                return ((UASegment)followPointer(params[0])).readShort();
            case 0x0076: // 0x0076=byte UWAP_ReadByte(UWAPSegment us)
                return ((UASegment)followPointer(params[0])).readByte();
            case 0x0077: // 0x0077=boolean UWAP_ReadBoolean(UWAPSegment us)
                return ((UASegment)followPointer(params[0])).readBoolean()? 1: 0;
            case 0x0078: // 0x0078=String UWAP_ReadString(UWAPSegment us)
                return makeTempObject(((UASegment)followPointer(params[0])).readString());
            case 0x0079: // 0x0079=void UWAP_WriteInt(UWAPSegment us, int i)
                ((UASegment)followPointer(params[0])).writeInt(params[1]);
                break;
            case 0x007A: // 0x007A=void UWAP_WriteShort(UWAPSegment us, short s)
                ((UASegment)followPointer(params[0])).writeShort((short)params[1]);
                break;
            case 0x007B: // 0x007B=void UWAP_WriteString(UWAPSegment us, String s)
                ((UASegment)followPointer(params[0])).writeString((String)followPointer(params[1]));
                break;
            case 0x007C: // 0x007C=void UWAP_WriteByte(UWAPSegment us, byte b)
                ((UASegment)followPointer(params[0])).writeByte((byte)params[1]);
                break;
            case 0x007D: // 0x007D=void UWAP_WriteBoolean(UWAPSegment us, boolean b)
                ((UASegment)followPointer(params[0])).writeBoolean(params[1] != 0);
                break;
            case 0x007E: // 0x007E=int[] UWAP_ReadInts(UWAPSegment us)
                return makeTempObject(((UASegment)followPointer(params[0])).readInts());
            case 0x007F: // 0x007F=short[] UWAP_ReadShorts(UWAPSegment us)
                return makeTempObject(((UASegment)followPointer(params[0])).readShorts());
            case 0x0080: // 0x0080=byte[] UWAP_ReadBytes(UWAPSegment us)
                return makeTempObject(((UASegment)followPointer(params[0])).readBytes());
            case 0x0081: // 0x0081=boolean[] UWAP_ReadBooleans(UWAPSegment us)
                return makeTempObject(((UASegment)followPointer(params[0])).readBooleans());
            case 0x0082: // 0x0082=String[] UWAP_ReadStrings(UWAPSegment us)
                return makeTempObject(((UASegment)followPointer(params[0])).readStrings());
            case 0x0083: // 0x0083=void UWAP_WriteInts(UWAPSegment us, int[] ia)
                ((UASegment)followPointer(params[0])).writeInts((int[])followPointer(params[1]));
                break;
            case 0x0084: // 0x0084=void UWAP_WriteShorts(UWAPSegment us, short[] sa)
                ((UASegment)followPointer(params[0])).writeShorts((short[])followPointer(params[1]));
                break;
            case 0x0085: // 0x0085=void UWAP_WriteStrings(UWAPSegment us, String[] sa) //写多个String到UWAPSegment
            {
                Object[] tmp1 = (Object[])followPointer(params[1]);
                String[] tmp2 = new String[tmp1.length];
                System.arraycopy(tmp1, 0, tmp2, 0, tmp1.length);
                ((UASegment)followPointer(params[0])).writeStrings(tmp2);
            }
                break;
            case 0x0086: // 0x0086=void UWAP_WriteBooleans(UWAPSegment us, boolean[] ba)
                ((UASegment)followPointer(params[0])).writeBooleans((boolean[])followPointer(params[1]));
                break;
            case 0x0087: // 0x0087=void UWAP_WriteBytes(UWAPSegment us, byte[] ba)
                ((UASegment)followPointer(params[0])).writeBytes((byte[])followPointer(params[1]));
                break;
            case 0x0088: // 0x0088=int SendRequest(UWAPSegment seg)
            {
                UASegment seg = (UASegment)followPointer(params[0]);
                seg.flush();
                return Utilities.sendRequest(seg);
            }
            case 0x0089: // 0x0089=UWAPSegment getNextPacket()
                return makeTempObject(GameMain.instance.nextPacket);
            case 0x008A: // 0x008A=void UWAP_SetHandled(UWAPSegment us, boolean flag)
                ((UASegment)followPointer(params[0])).handled = (params[1] == TRUE);
                break;
            case 0x008B: // 0x008B=void BroadcastPacket(UWAPSegment seg)
            {
                UASegment segment = (UASegment)followPointer(params[0]);
                segment.flush();
                Utilities.segments.addElement(segment);
                break;
            }
            case 0x008C: // 0x008C=int UWAP_GetSerial(UWAPSegment seg)
                return ((UASegment)followPointer(params[0])).serial;
            case 0x008D: // 0x008D=void UWAP_SetNeedResponse(UWAPSegment seg, boolean value)
                ((UASegment)followPointer(params[0])).needResponse = (params[1] == TRUE);
                break;
            case 0x008E: //0x008E=int UWAP_ReadUByte(UWAPSegment seg)
                return ((UASegment)followPointer(params[0])).readUnsignedByte();
            case 0x008F: //0x008F=int UWAP_ReadUShort(UWAP_Segment seg)
                return ((UASegment)followPointer(params[0])).readUnsignedShort();
            case 0x0091: // 0x0091=Vector Vector_Create()
                return makeTempObject(new Vector());
            case 0x0092: // 0x0092=int Vector_Size(Vector v)
                return ((Vector)followPointer(params[0])).size();
            case 0x0093: // 0x0093=void Vector_Add(Vector v, Object obj)
                ((Vector)followPointer(params[0])).addElement(followPointer(params[1]));
                break;
            case 0x0094: // 0x0094=void Vector_Remove(Vector v, int index)
                ((Vector)followPointer(params[0])).removeElementAt(params[1]);
                break;
            case 0x0095: // 0x0095=void Vector_Get(Vector v, int index)
                return makeTempObject(((Vector)followPointer(params[0])).elementAt(params[1]));
            case 0x0096: // 0x0096=void Vector_Clear(Vector v)
            	Vector v = ((Vector)followPointer(params[0]));
            	if(v != null) {
            		((Vector)followPointer(params[0])).removeAllElements();	
            	}                
                break;
            case 0x0097: // 0x0097= void Vector_InsertAt(Vector v,Object obj,int index)
                ((Vector)followPointer(params[0])).insertElementAt(followPointer(params[1]), params[2]);
                break;
            case 0x0098: // 0x0098=Object[] Vector_To_Array(Vector v)
            	Vector vec = (Vector)followPointer(params[0]);
            	if(vec != null) {
            		Object[] objs2 = new Object[vec.size()];
            		for(int i=0; i<objs2.length; i++) {
            			objs2[i] = vec.elementAt(i);
            		}
            		return makeTempObject(objs2);
            	} else {
            		return makeTempObject(null);
            	}
            	
            case 0x00A0: // 0x00A0=void Hashtable_Clear(Hashtable table)
                ((SortHashtable)followPointer(params[0])).clear();
                break;
            case 0x00A1: // 0x00A1=Hashtable Hashtable_Create()
                return makeTempObject(new SortHashtable());
            case 0x00A2: // 0x00A2=void Hashtable_Put(Hashtable table, Object key, Object value)
                ((SortHashtable)followPointer(params[0])).put(followPointer(params[1]), followPointer(params[2]));
                break;
            case 0x00A3: // 0x00A3=Object Hashtable_Get(Hashtable table, Object key)
                return makeTempObject(((SortHashtable)followPointer(params[0])).get(followPointer(params[1])));
            case 0x00A4: // 0x00A4=void Hashtable_Remove(Hashtable table, Object key)
                ((SortHashtable)followPointer(params[0])).remove(followPointer(params[1]));
                break;
            case 0x00A5: // 0x00A5=Object[] Hashtable_GetKeys(Hashtable table)
            {
            	SortHashtable tbl = (SortHashtable)followPointer(params[0]);                
                return makeTempObject(tbl.keys());
            }
            case 0x00A6: // 0x00A6=Object[] Hashtable_GetValues(Hashtable table)
            	SortHashtable tbl = (SortHashtable)followPointer(params[0]);                
                return makeTempObject(tbl.values());
            case 0x00A7: // 0x00A7=int Hashtable_Size(Hashtable table)
            	return ((SortHashtable)followPointer(params[0])).size();
            case 0x00A8: // 0x00A8=Object Hashtable_GetKey(Hashtable table, int index)
            	return makeTempObject(((SortHashtable)followPointer(params[0])).getKey(params[1]));
            case 0x00A9: // 0x00A9=Object Hashtable_GetValue(Hashtable table, int index)
            	return makeTempObject(((SortHashtable)followPointer(params[0])).getValue(params[1]));
            case 0x00B1: // 0x00B1=void PrintInt(int value)
                System.out.println(params[0]);
                break;
            case 0x00B2: // 0x00B2=void PrintObject(Object obj)
                System.out.println(followPointer(params[0]).toString());
                break;
            case 0x00B3: // 0x00B3=Object IntToObj(int value)
                return makeTempObject(new Integer(params[0]));
            case 0x00B4: // 0x00B4=int GetRoleID()
                return GameWorld.player.getId();
            case 0x00B5: // 0x00B5=String GetRoleName()
                return makeTempObject(GameWorld.player.getName());
            case 0x00B6: // 0x00B6=int GetRoleLevel()
                return GameWorld.player.level;
            case 0x00B7: // 0x00B7=void GC()
            {
                for(int i = tempSpace - 1; i >= 0; i--){
                    this.dynamicHeap[i] = null;
                }
                System.gc();
                break;
            }
            case 0x00B8: //0x00B8=Object GetRoleProcessor()
            	return makeTempObject(GameWorld.player);
            case 0x00B9: //0x00B9=Object GetRoleInfo(); see gtl struct GameRoleInfo
            	return makeTempObject( GameWorld.instance.readGameData("game_role_infor"));
            case 0x00C2: // 0x00C2=int Length(Object o)
                return Length(followPointer(params[0]));
            case 0x00C3: // 0x00C3=int ObjToInt(Object o)
                return ((Integer)followPointer(params[0])).intValue();
            case 0x00C5: // 0x00C5=byte[] LoadResourceFile(String name)
                return makeTempObject(LoadFile((String)followPointer(params[0])));
            case 0x00C7: // 0x00C7=String[] SplitString(String msg, int width)
                return makeTempObject(Tool.formatText((String)followPointer(params[0]), params[1], Utilities.font));
            case 0x00CE: // 0x00CE=void FillAlphaRect(Object g, int argb, int x, int y, int width, int height)
                Tool.fillAlphaRect((Graphics)followPointer(params[0]), params[1], params[2], params[3], params[4], params[5]);//画半透明区域
                break;
            case 0x00CF: // 0x00CF=void ClearKeys()
                Utilities.clearKeyStates();
                break;
            case 0x00D0: // 0x00D0=byte[] LoadFile(String name)
                return makeTempObject(loadRMSFile((String)followPointer(params[0])));
            case 0x00D1: // 0x00D1=boolean SaveFile(String name, byte[] data)
                return saveRMSFile((String)followPointer(params[0]), (byte[])followPointer(params[1]))? TRUE: FALSE;
            case 0x00D2: // 0x00D2=void DeleteFile(String name)
                deleteRMSFile((String)followPointer(params[0]));
                break;
            case 0x00E0: //0x00E0 = int MultiKeyCheck(int[] keys, byte clear)
                return Utilities.multiKeyCheck((int[])followPointer(params[0]), params[1] == TRUE);
//#if ModelID == Android || ModelID == Lenovo || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == AndroidSmall || ModelID == AndroidAuto
            //# case 0x00E1: //0x00E1=String getAPN()
            	//# return makeTempObject(PipActivity.getAPN());
//#endif
            case 0x1000: //DeleteGlobalVar
                Tool.deleteGlobalVar((String)followPointer(params[0]));
                break;
            case 0x1001: //SetGlobalInt
                Tool.setGlobalValue((String)followPointer(params[0]), params[1]);
                break;
            case 0x1002: //SetGlobalString
                Tool.setGlobalValue((String)followPointer(params[0]), (String)followPointer(params[1]));
                break;
            case 0x1003: //GetGlobalInt
                return Tool.getGlobalInt((String)followPointer(params[0]));
            case 0x1004: //GetGlobalString
                return makeTempObject(Tool.getGlobalString((String)followPointer(params[0])));
            case 0x1005: //GetGlobalObject
                return makeTempObject(Tool.getGlobalObject((String)followPointer(params[0])));
            case 0x1006: //SetGlobalObject
                Tool.setGlobalValue((String)followPointer(params[0]), followPointer(params[1]));
                break;
            case 0x1008: //0x1008=String GetSystemProperty(String propertyName)
                return makeTempObject(System.getProperty((String)followPointer(params[0])));
            case 0x1009: //0x1009=String GetAppProperty(String propertyName)
                return makeTempObject(SanguoMIDlet.instance.getAppProperty((String)followPointer(params[0])));
            case 0x1100: //0x1100=void SetUICatchInput(boolean value)
                ((VMGame)owner).setCatchInput(params[0] == TRUE);
                break;
            case 0x1101: // void SetUITransparent(boolean value)
                ((VMGame)owner).setTransparent(params[0] == TRUE);
                break;
            case 0x1102: // void CloseUI()
                ((VMGame)owner).close();
                break;
            case 0x1103: //0x1103=String GetVMId(Object vmGame)
            	return makeTempObject(((VMGame)followPointer(params[0])).getVMId());
            case 0x1104: //0x1104=void SetSingleton(boolean value)
                ((VMGame)owner).setSingleton(params[0] == TRUE);
                break;
            case 0x1108: //0x1108=boolean GetSingleton(Object vmGame)
            	return ((VMGame)followPointer(params[0])).getSingleton() ? TRUE : FALSE;
            case 0x1105: //0x1105=Object GetVMGameByVMKey(int vmKey)
            	return makeTempObject(VMGame.getVMGameByVMKey(params[0]));
            case 0x1106: // 0x1106=Object GetVMGameByVMId(String vmName)  #获得一个vmgame         	
            	return makeTempObject(VMGame.getVMGame((String)followPointer(params[0])));
            case 0x1107: //0x1107=int AddGameEvent(String vmId, int priority, Object params)
            	int gameEventKey = 0;
                VMGame gvm = VMGame.getVMGameByVMKey(VMGame.gameWorldVMGameKey);
                if(gvm != null) {
            		VM vm = gvm.getVM();
            		synchronized(vm){
            			gameEventKey = vm.callback(VMGame.CALLBACK_GAME_WORLD_ADD_GAME_EVENT,
                            new int[]{ 
            					vm.makeTempObject((String)followPointer(params[0])), params[1], vm.makeTempObject(followPointer(params[2]))
                            });
                    }
                }
            	return gameEventKey;
            case 0x1007: //0x1007=int GetVMGameKeyByGameEventKey(int gameEventKey)
            	int vmGameKey = 0;
                VMGame gvm2 = VMGame.getVMGameByVMKey(VMGame.gameWorldVMGameKey);
                if(gvm2 != null) {
            		VM vm = gvm2.getVM();
            		synchronized(vm){
            			vmGameKey = vm.callback(VMGame.CALLBACK_GAME_WORLD_GET_VMGAME_KEY,
                            new int[]{ 
            					params[0]
                            });
                    }
                }
            	return vmGameKey;                       
            case 0x1109: //0x1109=int GetCommonKey()
            	return VMGame.getCommonKey();
            case 0x110A: // int OpenUI(String id, boolean sync)
            	return VMGame.loadVMGame((String)followPointer(params[0]), VMGame.VM_TYPE_UI, params[1] == TRUE);
            case 0x110B: // void CloseAllUI(int type)
            	VMGame.closeAllUI((VMGame)owner, params[0]);
                break;
            case 0x110C: // int Distance(int x1, int y1, int x2, int y2)
                return Tool.distance(params[0], params[1], params[2], params[3]);
            case 0x110D: //0x110D=Object GetQuest(int questId)
                return makeTempObject(Quest.findQuest(params[0], true));
            case 0x1200: //0x1200=void LoadGameVm(String vmId)
            	VMGame.loadVMGame((String)followPointer(params[0]), VMGame.VM_TYPE_GAME, true);
                break;
            case 0x1201: //0x1201=void RemoveGameVm(String vmId)
            	VMGame.removeVMGame((String)followPointer(params[0]));
                break;
            case 0x1202: //0x1202=Object GetWorldProc();
            	return makeTempObject(GameWorld.instance);
            case 0x1203: //0x1203=Object GetProcessor(int type, int id);
            {
                switch(params[0]){
                    case Tool.VM_PROCESSOR_PANEL:
                        return makeTempObject(GameWorld.panel);
                    case Tool.VM_PROCESSOR_WORLD:
                        return makeTempObject(GameWorld.instance);
                    case Tool.SPRITE_TYPE_ROLE:
                        return makeTempObject(GameWorld.player);
                    case Tool.SPRITE_TYPE_ICON:
                        return makeTempObject(GameWorld.gameIcons.get(new Integer(params[1])));
                    case Tool.VM_PROCESSOR_GAMESPRITE:
                    	//按照InstanceId取
                    	return makeTempObject(GameWorld.getSprite(params[1]));
                    default:
                    	return makeTempObject(GameWorld.getSprite(params[0], params[1]));
                        
                }
            }
            case 0x1205: //0x1205=Object ReadGameData(Object processor, String dataName)
            {
                Object data = ((IVMGameProcessor)followPointer(params[0])).readGameData((String)followPointer(params[1]));

                if(data instanceof UASegment){
                    UASegment segment = (UASegment)data;
                    segment.flush();
                    segment.reset();
                }

                return makeTempObject(data);
            }
            case 0x1206: //0x1206=void SaveGameData(Object processor, String dataName, Object data)
            {
                Object data = followPointer(params[2]);

                if(data instanceof UASegment){
                    UASegment segment = (UASegment)data;
                    segment.flush();
                    segment.reset();
                }

                ((IVMGameProcessor)followPointer(params[0])).saveGameData((String)followPointer(params[1]), data);
            }
                break;
            case 0x1207: //0x1207= void RemoveGameData(Object processor, String dataName)
            {
                ((IVMGameProcessor)followPointer(params[0])).removeGameData((String)followPointer(params[1]));
            }
                break;
            case 0x1208: //0x1208 = int RealizeVMData(Object data)
            {
                synchronized(globalVMData){
                    globalVMData.put(new Integer(globalVMDataCurrentKey), followPointer(params[0]));
                    return globalVMDataCurrentKey++;
                }
            }
            case 0x1209: //0x1209 = Object ReadVMData(int addr)
            {
                synchronized(globalVMData){
                    return makeTempObject(globalVMData.get(new Integer(params[0])));
                }
            }
            case 0x120A: //0x120A = void FreeVMData(int addr)
            {
                synchronized(globalVMData){
                    globalVMData.remove(new Integer(params[0]));
                }
            }
                break;
            case 0x120B: //0x120B = void DrawIconSprite(Object icon, Object g)
            {
                ((GameIcon)followPointer(params[0])).draw((Graphics)followPointer(params[1]), 0, 0);
            }
            	break;
            case 0x120C: //0x120C=Object ReadWorldData(String dataName);
            	Object worldData = GameWorld.instance.readGameData((String)followPointer(params[0]));

                if(worldData instanceof UASegment){
                    UASegment segment = (UASegment)worldData;
                    segment.flush();
                    segment.reset();
                }

                return makeTempObject(worldData);
            case 0x120E: //0x120E=int DrawMixedString(Object g, String str, int x, int y, int color, boolean is3D, int anchor)
            {
                Tool.drawMixedText((Graphics)followPointer(params[0]), (String)followPointer(params[1]), params[2], params[3], params[4], 0x0, params[5] == TRUE, params[6]);
            }
                break;
            case 0x120F: //0x120F=int DrawMixedString3D(Object g, String str, int x, int y, int color, int bkcolor, int anchor)
            {
                Tool.drawMixedText((Graphics)followPointer(params[0]), (String)followPointer(params[1]), params[2], params[3], params[4], params[5], true, params[6]);
            }
                break;
            case 0x1210: //0x1210=int MixedStringWidth(String str)
                return Tool.drawMixedText(null, (String)followPointer(params[0]), 0, 0, 0, 0, true, 0);
            case 0x1211: //0x1211=Object[] GetVMParam(String vmId)
                return makeTempObject(VMGame.getVMParam((String)followPointer(params[0])));
            case 0x1212: //0x1212=int OpenUIWithParam(String vmId, Object[] param)
                return VMGame.openUI((String)followPointer(params[0]), followPointer(params[1]));
            case 0x1213: //0x1213=void AddCommonCallbackCycle(GWindow gWindow, String funcName)
                ((VMGame)owner).addCommonCallback(CYCLE, (GWindow)getGW(params[0]), (String)followPointer(params[1]));
                break;
            case 0x1214: //0x1214=void AddCommonCallbackCycleUI(GWindow gWindow, String funcName)
           	 	((VMGame)owner).addCommonCallback(CYCLEUI, (GWindow)getGW(params[0]), (String)followPointer(params[1]));
               break;
            case 0x1215: //0x1215=void AddCommonCallbackPaint(GWindow gWindow, String funcName)
                ((VMGame)owner).addCommonCallback(PAINT, (GWindow)getGW(params[0]), (String)followPointer(params[1]));
                break;
            case 0x1216: //0x1216=void AddCommonCallbackPacket(GWindow gWindow, String funcName)
                ((VMGame)owner).addCommonCallback(PROCESSPACKET, (GWindow)getGW(params[0]), (String)followPointer(params[1]));
                break;
            case 0x1217: //0x1217=void RemoveCommonCallbackCycle(GWindow gWindow)
                ((VMGame)owner).removeCommonCallback(CYCLE, (GWindow)getGW(params[0]));
                break;
            case 0x1218: //0x1218=void RemoveCommonCallbackCycleUI(GWindow gWindow)
                ((VMGame)owner).removeCommonCallback(CYCLEUI, (GWindow)getGW(params[0]));
                break;
            case 0x1219: //0x1219=void RemoveCommonCallbackPaint(GWindow gWindow)
                ((VMGame)owner).removeCommonCallback(PAINT, (GWindow)getGW(params[0]));
                break;
            case 0x1220: //0x1220=void RemoveCommonCallbackPacket(GWindow gWindow)
                ((VMGame)owner).removeCommonCallback(PROCESSPACKET, (GWindow)getGW(params[0]));
                break;
            case 0x1230: //0x1230=Object CreateGWindow(Object self, boolean isTransparent, String name)
            	return ((VMGame)owner).createWindow(params[0], (int[])followPointer(params[0]), params[1] == TRUE ? true : false, (String)followPointer(params[2])).vmData[GWidget.GW_VM_SELF];
            case 0x1231: //0x1231=void DestroyGWindow(GWindow gWindow)
            	((VMGame)owner).vmDestroyWindow((GWindow)getGW(params[0]));
            	break;
            case 0x1232: //0x1232=void ShowGWindow(GWindow gWindow)
            	((VMGame)owner).vmShowWindow((GWindow)getGW(params[0]));
            	break;
            case 0x1233: //0x1233=Object CreateGContainer(Object self, String name)
            	return new GContainer((VMGame)owner, params[0], (int[])followPointer(params[0]), (String)followPointer(params[1])).vmData[GWidget.GW_VM_SELF];
            case 0x1234: //0x1234=void GContainerAdd(VMContainer parent, VMWidget child)
            	((VMGame)owner).vmContainerAdd((GContainer)getGW(params[0]), getGW(params[1]));            	
            	break;
            case 0x1235: //0x1235=void GContainerDel(GContainer parent, GWidget child)
            	((VMGame)owner).vmContainerDel((GContainer)getGW(params[0]), getGW(params[1]));
            	break;
            case 0x1236: //0x1236=void CloseGWindow(GWindow gWindow)
            	((VMGame)owner).vmCloseWindow((GWindow)getGW(params[0]));
            	break;
            case 0x1237: //0x1237=Object CreateGWidget(Object vmObj, String name)
            	return new GWidget((VMGame)owner, params[0], (int[])followPointer(params[0]), (String)followPointer(params[1])).vmData[GWidget.GW_VM_SELF];
            case 0x1238: //0x1238=void GContainerInsert(GContainer parent, GWidget child, int index)
            	((VMGame)owner).vmContainerInsert((GContainer)getGW(params[0]), getGW(params[1]), params[2]);
            	break;
            case 0x1239: //0x1239=void GSetBounds(GWidget gWidget, int x, int y, int w, int h)
            	((GWidget)getGW(params[0])).setBounds( params[1], params[2], params[3], params[4]);
            	break;
            case 0x123A: //0x123A=void GSetBorder(GWidget gWidget, int borderTop, int borderBottom, int borderLeft, int borderRight)
            	((GWidget)getGW(params[0])).setBorder(params[1], params[2], params[3], params[4]);
            	break;
            case 0x123B: //0x123B=void GSetLayoutMode(GContainer gContainer, int layoutMode, int hgap, int vgap, int algin, int rows, int cols)
            	((GContainer)getGW(params[0])).setLayoutMode(params[1], params[2], params[3], params[4], params[5], params[6]);
            	break;
            case 0x123C: //0x123C=void GLayout(GContainer gContainer)
            	((GContainer)getGW(params[0])).layout();
            	break;
            case 0x123D: //0x123D=void GSetScale(GWidget gWidget, boolean isScale)
            	((GWidget)getGW(params[0])).setScale(params[1] == TRUE);
            	break;
            case 0x123E: //0x123E=Object CreateGLinePanel(Object vmObj, String name)
            	return new GLinePanel((VMGame)owner, params[0], (int[])followPointer(params[0]), (String)followPointer(params[1])).vmData[GWidget.GW_VM_SELF];
            case 0x123F: //0x123F=void SetGLinePanel(GLinePanel gLinePanel, int[] colors, String cornerResName, int index)
            	((GLinePanel)getGW(params[0])).setData((int[])followPointer(params[1]), (String)followPointer(params[2]), params[3]);
            	break;
            case 0x1240: //0x1240=void GPaint(GWidget gWidget)
            	GWidget gWidget = (GWidget)followPointer(params[0]);
            	if(gWidget instanceof GContainer && ((GContainer)gWidget).isJavaPaint) {
            		((GContainer)gWidget).paintContainer();
            	} else if(gWidget instanceof IGPaint){
            		((IGPaint)gWidget).paint();
            	}
            	break;
            case 0x1241: //0x1241=Object CreateGIcon(Object vmObj, String name)
            	return new GIcon((VMGame)owner, params[0], (int[])followPointer(params[0]), (String)followPointer(params[1])).vmData[GWidget.GW_VM_SELF];
            case 0x1242: //0x1242=void SetGIcon(GIcon gIcon, int[] colors, String cornerResName, int iconIndex, int trans, int anchor)
            	((GIcon)getGW(params[0])).setData((int[])followPointer(params[1]), (String)followPointer(params[2]), params[3], params[4], params[5]);
            	break;
            case 0x1243: //0x1243=Object CreateGLabel(Object vmObj, String name)
            	return new GLabel((VMGame)owner, params[0], (int[])followPointer(params[0]), (String)followPointer(params[1])).vmData[GWidget.GW_VM_SELF];
            case 0x1244: //0x1244=void SetGLabel(GLabel gLabel, String label, boolean is3D, int color, int bgColor, int anchor)
            	((GLabel)getGW(params[0])).setData((String)followPointer(params[1]), params[2]==TRUE, params[3], params[4], params[5]);
            	break;
            case 0x1245: //0x1245=void GCycle(IGCycle iGCycle)
            	((IGCycle)followPointer(params[0])).cycle();          	
            	break;
            case 0x1246: //0x1246=void GContainerClear(GContainer gContainer)
            	((GContainer)getGW(params[0])).clear();          	
            	break;
            case 0x1247: //0x1247=Object GGetVMGWiget(GWidget gWidget)
            	GWidget gWidget3 = ((GWidget)followPointer(params[0]));
            	if(gWidget3 != null) {
            		return this.makeTempObject(gWidget3.vmData);
            	} else {
            		return 0;
            	}            	
            case 0x1248: //0x1248=void GSetLabelBack(GLabel gLabel, int[] colors, String cornerResName, int index)
            	((GLabel)getGW(params[0])).setBack((int[])followPointer(params[1]), (String)followPointer(params[2]), params[3]);
            	break;
            case 0x1249: // 0x1249=boolean Object_Equal(Object obj, Object str) 判断两个对象引用是否一样
                return followPointer(params[0]) == followPointer(params[1]) ? TRUE: FALSE;
            case 0x124A: // 0x124A=void GSetNeedLayout(GWidget gWidget, boolean needLayout) 
            	getGW(params[0]).SetNeedLayout(params[1] == TRUE);
                break;
            case 0x124B: // 0x124B=void GSetFocus(GWindow gWindow, GWidget gWidget)
                ((GWindow)getGW(params[0])).setFocus((GWidget)getGW(params[1]));
                break;
            case 0x124C: // 0x124C=Object GGetParentWindow(GWidget gWidget)
            	GWidget _gParent = getGW(params[0]).getParentWindow();
            	if(_gParent != null) {
            		return _gParent.vmData[GWidget.GW_VM_SELF];
            	} else {
            		return 0;
            	}            	
            case 0x124D: // 0x124D=boolean GIsFocus(GWidget gWidget) 
            	return getGW(params[0]).isFocus() ? TRUE : FALSE;
            case 0x124E: // 0x124E=void GSetEnableFocus(GWidget gWidget, boolean enableFocus) 
            	getGW(params[0]).setEnableFocus(params[1] == TRUE);
                break;
            case 0x124F: // 0x124F=String GLabelGetText(GLabel gLabel) 
            	return this.makeTempObject(((GLabel)getGW(params[0])).getText());
            case 0x1250: // 0x1250=void GLabelSetText(GLabel gLabel, String text) 
            	((GLabel)getGW(params[0])).setText((String)followPointer(params[1]));
            	break;
            case 0x1251: // 0x1251=void GSetNeedExcecuteCycle(IGCycle iGCycle, boolean needExecuteCycle) 
            	((IGCycle)getGW(params[0])).setNeedExecuteCycle(params[1] == TRUE);
            	break;
            case 0x1252: // 0x1252=void GSetIGCycleSpeed(IGCycle iGCycle, int speed) 
            	((IGCycle)getGW(params[0])).setSpeed(params[1]);
            	break;
            case 0x1253: // 0x1253=void GLabelSetIsMixStr(GLabel gLabel, boolean isMixStr) 
            	((GLabel)getGW(params[0])).setIsMixStr(params[1] == TRUE);
            	break;
            case 0x1254: // 0x1254=void addScrollBar(GContainer gContainer, GScrollBar gSb) 
            	((GContainer)getGW(params[0])).addScrollBar((GScrollBar)getGW(params[1]));
            	break;
            case 0x1255: // 0x1255=void GSetPos(GWidget gWidget, int x, int y) 
            	getGW(params[0]).setPos(params[1], params[2]);
            	break;
            case 0x1256: // 0x1256=Object CreateGScrollBar(Object vmObj, String name)
            	return new GScrollBar((VMGame)owner, params[0], (int[])followPointer(params[0]), (String)followPointer(params[1])).vmData[GWidget.GW_VM_SELF];
            case 0x1257: // 0x1257=int GGetMaxScrollDis(GScrollBar gSb)
            	return ((GScrollBar)getGW(params[0])).getMaxScrollDis();            	
            case 0x1258: // 0x1258=int GGetScrollPos(GScrollBar gSb) 
            	return ((GScrollBar)getGW(params[0])).getScrollPos();
            case 0x1259: // 0x1259=int GGetScrollBarTick(GScrollBar gSb) 
            	return ((GScrollBar)getGW(params[0])).getTick();
            case 0x125A: // 0x125A=Object GGetParent(GWidget gWidget) 
            	GWidget parent = getGW(params[0]).parent;
            	if(parent != null) {
            		return parent.vmData[GWidget.GW_VM_SELF];
            	} else {
            		return 0;	
            	}
            case 0x125B: // 0x125B=void GSetScrollBarAlign(GScrollBar gSb, int align) 
            	((GScrollBar)getGW(params[0])).setAlign(params[1]);
            	break;
            case 0x125C: // 0x125C=void GMoveUp(GContainer gContainer) 
            	((GContainer)getGW(params[0])).moveUp();
            	break;
            case 0x125D: // 0x125D=void GMoveDown(GContainer gContainer) 
            	((GContainer)getGW(params[0])).moveDown();
            	break;
            case 0x125E: // 0x125E=void GMoveUpPage(GContainer gContainer) 
            	((GContainer)getGW(params[0])).moveUpPage();
            	break;
            case 0x125F: // 0x125F=void SetGTextArea(GTextArea gTextArea, String text, int color, boolean is3d) 
            	((GTextArea)getGW(params[0])).setData((String)followPointer(params[1]), params[2], params[3] == TRUE);
            	break;
            case 0x1260: // 0x1260=void GTextAreaSetText(GTextArea gTextArea, String text) 
            	((GTextArea)getGW(params[0])).setText((String)followPointer(params[1]));
            	break;
            case 0x1261: // 0x1261=String GTextAreaGetText(GTextArea gTextArea) 
            	return this.makeTempObject(((GTextArea)getGW(params[0])).getText());
            case 0x1262: //0x1262=Object CreateGTextArea(Object vmObj, String name)
            	return new GTextArea((VMGame)owner, params[0], (int[])followPointer(params[0]), (String)followPointer(params[1])).vmData[GWidget.GW_VM_SELF];
            case 0x1263: //0x1263=int GTextAreaTestWidth(GTextArea gTextArea)
            	return ((GTextArea)getGW(params[0])).testWidth();
            case 0x1264: //0x1264=int GTextAreaTestHeight(GTextArea gTextArea, int width)
            	return ((GTextArea)getGW(params[0])).testHeight(params[1]);
            case 0x1265: //0x1265=boolean GIsNeedScrollBar(GContainer gContainer)
            	GContainer con = ((GContainer)getGW(params[0]));
            	if(con != null){
            		return con.needScrollBar ? TRUE : FALSE;
            	} else {
            		return FALSE;
            	}
            case 0x1266: //0x1266=void GSetMaxScrollDis(GScrollBar gSb, int maxScrollDis)
            	 ((GScrollBar)getGW(params[0])).setMaxScrollDis(params[1]);
            	 break;
            case 0x1267: //0x1267=void GSetScrollPos(GScrollBar gSb, int scrollPos)
           	 	((GScrollBar)getGW(params[0])).setScrollPos(params[1]);
           	 	break;
            case 0x1268: //0x1268=void GSetIsJavaPaint(GContainer gContainer, boolean isJavaPaint)
           	 	((GContainer)getGW(params[0])).setIsJavaPaint(params[1] == TRUE);
           	 	break;
            case 0x1269: //0x1269=void GIconSetIndex(GIcon gIcon, int iconIndex)
           	 	((GIcon)getGW(params[0])).setIconIndex(params[1]);
           	 	break;
            case 0x126A: //0x126A=void GIconSetNumberData(GIcon gIcon, String numberResName, int numberIndex, int numberSpace, int numberX, int numberY)
           	 	((GIcon)getGW(params[0])).setNumberData((String)followPointer(params[1]), params[2], params[3], params[4], params[5]);
           	 	break;
            case 0x126B: //0x126B=void GIconSetNumber(GIcon gIcon, int number)
           	 	((GIcon)getGW(params[0])).setNumber(params[1]);
           	 	break;
            case 0x126C: //0x126C=void GSetChildrenOffset(GContainer gContainer, int offsetX, int offsetY)
           	 	((GContainer)getGW(params[0])).setChildrenOffset(params[1], params[2]);
           	 	break;
            case 0x126D: //0x126D=void GTextAreaGetLineSpace(GTextArea gTextArea)
            	return ((GTextArea)getGW(params[0])).lineSpace;
            case 0x126E: //0x126E=void GSetG3Layout(GWidget gWidget, int gridX, int gridY, int gridHCount, int gridVCount, int borderTop, int borderBottom, int borderLeft, int borderRight)
           	 	((GWidget)getGW(params[0])).setGrid3Data(params[1], params[2], params[3], params[4], params[5], params[6], params[7], params[8]);
           	 	break;  
            case 0x126F: //0x126F=void GG3Layout(GContainer gContainer)
           	 	((GContainer)getGW(params[0])).grid3Layout();
           	 	break;
            case 0x1270: //0x1270=void GBorderLayout(GContainer gContainer)
           	 	((GContainer)getGW(params[0])).borderLayout();
           	 	break;
            case 0x1271: //0x1271=void GToTop(GContainer gContainer, int index)
           	 	((GContainer)getGW(params[0])).toTop(params[1]);
           	 	break;
            case 0x1272: //0x1272=Object GGetPressGWidget()
            	if(GWindow.pressWidget != null) {
            		return this.makeTempObject(GWindow.pressWidget.vmData);
            	} else {
            		return 0;
            	}
            case 0x1273: //0x1273=Object CreateGImageNumber(Object vmObj, String name)
            	return new GImageNumer((VMGame)owner, params[0], (int[])followPointer(params[0]), (String)followPointer(params[1])).vmData[GWidget.GW_VM_SELF];
            case 0x1274: //0x1274=void SetGImageNumer(GImageNumer gImageNumer, String resName, boolean isShowSign, int plusStartIndex, int subStartIndex, int space, int anchor)
            	((GImageNumer)getGW(params[0])).setData((String)followPointer(params[1]), params[2] == TRUE, params[3], params[4], params[5], params[6]);
            	break;
            case 0x1275: //0x1275=void GImageNumerSetNum(GImageNumer gImageNumer, String number)
            	((GImageNumer)getGW(params[0])).setNumer((String)followPointer(params[1]));
            	break;
            case 0x1276: //0x1276=void GIconSetMask(GIcon gIcon, boolean hasMask, int maskRgb)
            	((GIcon)getGW(params[0])).setMask(params[1] == TRUE, params[2]);
            	break;
            case 0x1277: //0x1277=void GIconSetBackColors(GIcon gIcon, int[] backColors)
            	((GIcon)getGW(params[0])).setBackColors((int[])followPointer(params[1]));
            	break;
            case 0x1278: //0x1278=Object GetTopGWindow()
            	GWindow topWindow = VMGame.getTopGWindow();
            	if(topWindow != null) {
            		return this.makeTempObject(topWindow.vmData);
            	} else {
            		return 0;
            	}            	
            case 0x1279: //0x1279=Object GetPointerGWidgetInWin(GWindow gWindow, int x, int y)
            	GWidget selWidget = VMGame.getPointerWidget((GWindow)getGW(params[0]), params[1], params[2]);
            	if(selWidget!= null) {
            		return this.makeTempObject(selWidget.vmData);
            	} else {
            		return 0;
            	}            	
            case 0x127A: //0x127A=Object GetPointerGWidget(int x, int y)
            {
            	GWidget selWidget2 = VMGame.getPointerWidget(params[0], params[1]);
            	if(selWidget2!= null) {
            		return this.makeTempObject(selWidget2.vmData);
            	} else {
            		return 0;
            	}            
            	
            }
            	
            case 0x127B: //0x127B=void GSetPressGWidget(GWidget gWidget)
            	GWindow.pressWidget = getGW(params[0]);
            	break;
            case 0x127C: //0x127C=Object GetGWinVMGame(GWindow gWindow)
            	GWindow gwin = (GWindow)getGW(params[0]);
            	if(gwin != null){
            		return this.makeTempObject(gwin.getVMGame());
            	} else {
            		return 0;
            	}
            case 0x127D: //0x127D=void GSendEvent(VMGame vmGame, GWidget gWidget, int _eventType, int _param0, int[] _param2, Object _param1, Object[] _param3)
    			VM gtvm = ((VMGame)followPointer(params[0])).getVM();
    			GWidget _gWidget = (GWidget)getGW(params[1]);
    			if(_gWidget != null && _gWidget.vmData[GWidget.GW_VM_FUNC_SEND_EVENT] > 0) {
    				synchronized (gtvm) {
        				gtvm.execute(_gWidget.vmData[GWidget.GW_VM_FUNC_SEND_EVENT], new int[] { 
        						_gWidget.vmData[GWidget.GW_VM_SELF], 
        						params[2],    //_eventType
        						params[3],
        						gtvm.makeTempObject(followPointer(params[4])),
        						gtvm.makeTempObject(followPointer(params[5])),
        						gtvm.makeTempObject(followPointer(params[6])),
        					});
        			}
    			}            	
            	break; 
            case 0x127E: //0x127E=Object GGetPressJavaGWidget()
            	if(GWindow.pressWidget != null) {
            		return this.makeTempObject(GWindow.pressWidget);
            	} else {
            		return 0;
            	}
            case 0x127F: //0x127F=boolean RectIn(int x1, int y1, int w, int h, int x2, int y2)
            	return Tool.rectIn(params[0], params[1], params[2], params[3], params[4], params[5]) ? TRUE : FALSE;     
            case 0x1280: //0x1280=Object GGetGWindows(VMGame vmGame)
            	return this.makeTempObject(((VMGame)followPointer(params[0])).getGWindows());
            case 0x1281: //0x1281=boolean GWindowIsClose(GWindow gWindow)
            	return ((GWindow)getGW(params[0])).isShow ? FALSE : TRUE;
            case 0x1282: //0x1282=void GSetTextAreaBack(GTextArea gTextArea, int[] colors, String cornerResName, int index)
            	((GTextArea)getGW(params[0])).setBack((int[])followPointer(params[1]), (String)followPointer(params[2]), params[3]);
            	break;
            case 0x1283: //0x1283=void GSetAbs(GContainer gContainer)
            	((GContainer)getGW(params[0])).setAbs();
            	break;
            case 0x1284: //0x1284=void GGetAbsX(GWidget gWidget)
            	return getGW(params[0]).getAbsX();
            case 0x1285: //0x1285=void GGetAbsY(GWidget gWidget)
            	return getGW(params[0]).getAbsY();
            case 0x1286: //0x1286=void GTextAreaSetLineSpace(GTextArea gTextArea, int lineSpace)
            	((GTextArea)getGW(params[0])).setLineSpace(params[1]);
            	break;
            case 0x1287: //0x1287=void GSetIntersectView(GContainer gContainer, boolean isIntersectView)
            	((GContainer)getGW(params[0])).isIntersectView = (params[1] == TRUE);
            	break;
            case 0x1288: //0x1288=void GSetPressXY(GWidget gWidget, int x, int y)
            	getGW(params[0]).setPressXY(params[1], params[2]);
            	break;
            case 0x1289: //0x1289=int GGetPressX(GWidget gWidget)
            	return getGW(params[0]).getPressX();
            case 0x128A: //0x128A=int GGetPressY(GWidget gWidget)
            	return getGW(params[0]).getPressY();
            case 0x128B: //0x128B=void GWidgetMove(GWidget gWidget, int offsetX, int offsetY)
            	getGW(params[0]).move(params[1], params[2]);
            	break;
            case 0x128C: //0x128C=Object CreateGGameIcon(int vmObj, String name)
            	return new GGameIcon((VMGame)owner, params[0], (int[])followPointer(params[0]), (String)followPointer(params[1])).vmData[GWidget.GW_VM_SELF];
            case 0x128D: //0x128D=void GGameIconSetData1(GGameIcon gGameIcon, int _type, int _id, int _animateIndex)
            	((GGameIcon)getGW(params[0])).setData1(params[1], params[2], params[3]);
            	break;
            case 0x128E: //0x128E=void GGameIconSetData2(GGameIcon gGameIcon, GameSprite _processor, int _animateIndex)
            	((GGameIcon)getGW(params[0])).setData2((GameSprite)followPointer(params[1]), params[2]);
            	break;
            case 0x128F: //0x128F=Object GGetGameIcon(GGameIcon gGameIcon)
            	return this.makeTempObject(((GGameIcon)getGW(params[0])).getGameIcon());
            case 0x1290: //0x1290=void GGameIconSetShow(GGameIcon gGameIcon, boolean show)
            	((GGameIcon)getGW(params[0])).setShow(params[1] == TRUE);
            	break;
            case 0x1291: //0x1291=Object GetVMObj(VMGame vmg, Object objPoint)
            	return this.makeTempObject(((VMGame)followPointer(params[0])).getVM().followPointer(params[1]));
            case 0x1292: //0x1292=void GSetReCreateStack(GWindow gWindow)
            	((GWindow)getGW(params[0])).setReCreateStack();
            	break;
            case 0x1293: //0x1293=void GSetTransparent(GWindow gWindow, boolean isTransparent)
            	((GWindow)getGW(params[0])).isTransparent = (params[1] == TRUE);
            	break;
            case 0x1294: //0x1294=boolean IsDragging()
            	return GWindow.isDragging ? TRUE : FALSE;
            case 0x1295: //0x1295=void SetDragging(boolean isDragging)
            	GWindow.isDragging = (params[0] == TRUE);
            	break;
            case 0x1296: //0x1296=boolean RectIntersect(int x1, int y1, int w1, int h1, int x2, int y2, int w2, int h2)
            	return Tool.rectIntersect(params[0], params[1], params[2], params[3], params[4], params[5], params[6], params[7]) ? TRUE : FALSE;     
            case 0x1297: //0x1297=boolean rectContain(int x1, int y1, int w1, int h1, int x2, int y2, int w2, int h2)
            	return Tool.rectContain(params[0], params[1], params[2], params[3], params[4], params[5], params[6], params[7]) ? TRUE : FALSE;
            case 0x1298: //0x1298=Object GGetDropTargetGWidget()
            	if(GWindow.dropTargetWidget != null) {
            		return GWindow.dropTargetWidget.vmData[GWidget.GW_VM_SELF];
            	} else {
            		return 0;
            	}
            case 0x1299: //0x1299=Object GGetDropTargetJavaGWidget()
            	if(GWindow.dropTargetWidget != null) {
            		return this.makeTempObject(GWindow.dropTargetWidget);
            	} else {
            		return 0;
            	}
            case 0x129A: //0x129A=void GSetDropTargetGWidget(GWidget gWidget)
            	GWindow.dropTargetWidget = (GWidget)getGW(params[0]);
            	break;
            case 0x129B: //0x129B=Object GGetGWidgetClone(GWidget gWidget)
            	return getGW(params[0]).getClone((VMGame)owner).vmData[GWidget.GW_VM_SELF];
            case 0x129C: //0x129C=Object[] GGetChildren(GContainer gContainer)
            	return this.makeTempObject(((GContainer)getGW(params[0])).getChildren());
            case 0x129D: //0x129D=Object GGetChild(GContainer gContainer, int index)
            	return this.makeTempObject(((GContainer)getGW(params[0])).getChild(params[1]));
            case 0x129E: //0x129E=int GGetChildSize(GContainer gContainer)
            	return ((GContainer)getGW(params[0])).children.size();
            case 0x129F: //0x129F=void GContainerBatchAdd(GContainer gContainer, GWidget temp, int count)
            	((GContainer)getGW(params[0])).batchAdd(getGW(params[1]), params[2]);
            	break;
            case 0x12A0: //0x12A0=Vector GGetChildrenVector(GContainer gContainer)
            	return this.makeTempObject(((GContainer)getGW(params[0])).children);
            case 0x12A1: //0x12A1=Object[] GGetCloneArray(GWidget gWidget, int count)
            	return this.makeTempObject(GWidget.getCloneArray((VMGame)owner, getGW(params[0]), params[1]));
            case 0x12A2: //0x12A2=Object[] GGetJavaChildren(GContainer gContainer)
            	return this.makeTempObject(((GContainer)getGW(params[0])).getJavaChildren());
            case 0x12A3: //0x12A3=Object GGetJavaChild(GContainer gContainer, int index)
            	return this.makeTempObject(((GContainer)getGW(params[0])).getJavaChild(params[1]));
            case 0x12A4: //0x12A4=Object GGetFocus(GWindow gWindow)
            	//return ((GWindow)getGW(params[0])).focusWidget.vmObj;
            	GWindow gWindow = (GWindow)getGW(params[0]);
            	if(gWindow.focusWidget != null) {
            		return this.makeTempObject(gWindow.focusWidget.vmData);
            	} else {
            		return 0;
            	}            	
            case 0x12A5: //0x12A5=Object GGetJavaFocus(GWindow gWindow)
                return this.makeTempObject(((GWindow)getGW(params[0])).focusWidget);
            case 0x12A6: //0x12A6=boolean GCanHandleCycleUI(GWindow gWindow, GWidget gWidget)
            	return ((GWindow)getGW(params[0])).canHandleCycleUI(getGW(params[1])) ? TRUE : FALSE;
            case 0x12A7: //0x12A7=String GetTopUIVMId()
                return this.makeTempObject(VMGame.getTopUIVMId());
            case 0x12A8: //0x12A8=boolean GIsShow(GWidget gWidget)
                return getGW(params[0]).getShow() ? TRUE : FALSE;
            case 0x12A9: //0x12A9=void GSetShow(GWidget gWidget, boolean isShow)
            	getGW(params[0]).setShow(params[1] == TRUE);
                break;
            case 0x12AA: //0x12AA=void SendKeyPressed(int keyId)
            	Utilities.sendKeyPressed(params[0]);
                break;
            case 0x12AB: //0x12AB=void SendKeyDown(int keyId)
            	Utilities.sendKeyDown(params[0]);
                break;
            case 0x12AC: //0x12AC=void SendKeyUp(int keyId)
            	Utilities.sendKeyUp(params[0]);
                break;
            case 0x12AD: //0x12AD=void DestroyGWidget(GWidget gWidget)
            	GWidget destoryGWidget = getGW(params[0]);
            	if(destoryGWidget!= null ) {
            		if(destoryGWidget.vmData[GWidget.GW_VM_FUNC_DESTROY] > 0) {
            			synchronized (this) {
            				this.callback(destoryGWidget.vmData[GWidget.GW_VM_FUNC_DESTROY], new int[] { 
            						destoryGWidget.vmData[GWidget.GW_VM_SELF]
            					});
            			}
            		}
            		
            		destoryGWidget.freeVMObj();
            		((VMGame)owner).removeGWidget(destoryGWidget);
            	} 		
            	
                break;
            case 0x12AE: //0x12AE=Object GetJavaGWidget(GWidget gWidget)
                return this.makeTempObject(getGW(params[0]));
            case 0x12AF: //0x12AF=void GWindowSetCatchInput(GWindow gWindow, boolean catchInput)
            	((GWindow)getGW(params[0])).catchInput = (params[1] == TRUE);
                break;
            case 0x12B0: //0x12B0=void GLabelSetOffsetScale(GLabel gLabel, int scale)
            	((GLabel)getGW(params[0])).setOffsetScale(params[1]);
                break;
            case 0x12B1: //0x12B1=void GLabelSetOffset(GLabel gLabel, int scrollOffset)
            	((GLabel)getGW(params[0])).setScrollOffset(params[1]);
                break;
            case 0x12B2: //0x12B2=int GLabelGetScrollOffset(GLabel gLabel)
            	return ((GLabel)getGW(params[0])).getScrollOffset();
            case 0x12B3: //0x12B3=Object GetMouseTopGWindow(int x, int y)
            	GWindow mouseTopWindow = VMGame.getMouseTopGWindow(params[0], params[1]);
            	if(mouseTopWindow != null) {
            		return this.makeTempObject(mouseTopWindow.vmData);
            	} else {
            		return 0;
            	}
            case 0x12B4: //0x12B5=void GContainerAdd2(GContainer parent, GWidget child, int borderLayoutType)
            	((GContainer)getGW(params[0])).add(getGW(params[1]), params[2]);
            	break;
            case 0x12B5: //0x12B5=void GSetHLayout(GContainer gContainer, int hgap, int align)
            	((GContainer)getGW(params[0])).setHLayout(params[1], params[2]);
            	break;
            case 0x12B6: //0x12B6=void GSetVLayout(GContainer gContainer, int vgap, int align)
            	((GContainer)getGW(params[0])).setVLayout(params[1], params[2]);
            	break;
            case 0x12B7: //0x12B7=void GSetGridLayout(GContainer gContainer, int rows, int cols)
            	((GContainer)getGW(params[0])).setGridLayout(params[1], params[2]);
            	break;
            case 0x12B8: //0x12B8=void GSetGrid2Layout(GContainer gContainer, int hgap, int vgap, int gridW, int gridH)
            	((GContainer)getGW(params[0])).setGrid2Layout(params[1], params[2], params[3], params[4]);
            	break;
            case 0x12B9: //0x12B9=void GSetGrid3Layout(GContainer gContainer, int rows, int cols)
            	((GContainer)getGW(params[0])).setGrid3Layout(params[1], params[2]);
            	break;
            case 0x12BA: //0x12BA=void GSetBorderLayout(GContainer gContainer, int upGap, int downGap, int leftGap, int rightGap)
            	((GContainer)getGW(params[0])).setBorderLayout(params[1], params[2], params[3], params[4]);
            	break;
            case 0x12BB: //0x12BB=void GLabelSet3D(GLabel gLabel, boolean is3d)
            	((GLabel)getGW(params[0])).is3d = (params[1] == TRUE);
            	break;
            case 0x12BC: //0x12BC=void GLabelSetColor(GLabel gLabel, int color)
            	((GLabel)getGW(params[0])).color = params[1];
            	break;
            case 0x12BD: //0x12BD=void GLabelSetBgColor(GLabel gLabel, int bgColor)
            	((GLabel)getGW(params[0])).bgColor = params[1];
            	break;
            case 0x12BE: //0x12BE=void GLabelSetAnchor(GLabel gLabel, int anchor)
            	((GLabel)getGW(params[0])).anchor = params[1];
            	break;
            case 0x12BF: //0x12BF=void GLabelSetColors(GLabel gLabel, int[] colors)
            	((GLabel)getGW(params[0])).colors = (int[])this.followPointer(params[1]);
            	break;
            case 0x12C0: //0x12C0=void GLabelSetResIndex(GLabel gLabel, int resIndex)
            	((GLabel)getGW(params[0])).index = params[1];
            	break;
            case 0x12C1: //0x12C1=void GTextSetColor(GTextArea gText, int color)
            	((GTextArea)getGW(params[0])).color = params[1];
            	break;
            case 0x12C2: //0x12C2=void GTextSet3D(GTextArea gText, boolean is3d)
            	((GTextArea)getGW(params[0])).is3d = (params[1] == TRUE);
            	break;
            case 0x12C3: //0x12C3=void GTextGetShowLines(GTextArea gText)
            	return ((GTextArea)getGW(params[0])).showLines;
            case 0x12C4: //0x12C4=void GTextGetTotalLines(GTextArea gText)
            	return ((GTextArea)getGW(params[0])).totalLines;
            case 0x12C5: //0x12C5=void GTextGetLastPageLine(GTextArea gText)
            	return ((GTextArea)getGW(params[0])).lastPageLine;
            case 0x12C6: //0x12C6=void GTextGetCurrentLine(GTextArea gText)
            	return ((GTextArea)getGW(params[0])).currentLine;
            case 0x12C7: //0x12C7=void GTextGetCurPage(GTextArea gText)
            	return ((GTextArea)getGW(params[0])).curPage;
            case 0x12C8: //0x12C8=void GTextGetTotalPage(GTextArea gText)
            	return ((GTextArea)getGW(params[0])).totalPage;
            case 0x12C9: //0x12C9=void GTextSetColors(GTextArea gText, int[] colors)
            	((GTextArea)getGW(params[0])).colors = (int[])this.followPointer(params[1]);
            	break;
            case 0x12CA: //0x12CA=void GTextSetResIndex(GTextArea gText, int resIndex)
            	((GTextArea)getGW(params[0])).index = params[1];
            	break;
            case 0x12CB: //0x12CB=void GWidgetGetVMGame(GWidget gWidget)
            	return this.makeTempObject(getGW(params[0]).vmGame);
            case 0x12CC: //0x12CC=void GWidgetGetName(GWidget gWidget)
            	return this.makeTempObject(getGW(params[0]).name);
            case 0x12CD: //0x12CD=void GIconTrans(GIcon gIcon, int trans)
            	((GIcon)getGW(params[0])).trans = params[1];
            	break;
            case 0x12CE: //0x12CE=void GIconAnchor(GIcon gIcon, int anchor)
            	((GIcon)getGW(params[0])).anchor = params[1];
            	break;
            case 0x12CF: //0x12CF=void GToBottom(GContainer gContainer, int index)
           	 	((GContainer)getGW(params[0])).toBottom(params[1]);
           	 	break;
            case 0x12D0: //0x12D0=void GGWidgetIndex(GContainer gContainer, GWidget gWidget)
            	GWidget gWidget4 = getGW(params[1]);
            	if(gWidget4 != null) {
            		return ((GContainer)getGW(params[0])).getIndex(gWidget4); 
            	} else {
            		return -1;
            	}
            case 0x12D1: //0x12D1=Object GetCurrentVM()
           	 	return this.makeTempObject(this.owner);
            case 0x12D2: //0x12D2=boolean IsBreak(VMGame vmGame)
           	 	return ((VMGame)followPointer(params[0])).gtvm.isBlock() ? TRUE : FALSE;
            case 0x12D3: //0x12D3=void GSetIgnorePauseUICycle(GWindow gWindow, boolean ignorePauseUICycle)
            	((GWindow)getGW(params[0])).ignorePauseUICycle = (params[1] == TRUE);
           	 	break;
            case 0x12D4: //0x12D4=booolean GIgnorePauseUICycle(GWindow gWindow)
            	return ((GWindow)getGW(params[0])).ignorePauseUICycle ? TRUE : FALSE;
            case 0x12D5: //0x12D5=int GIconGetNumberIndex(GIcon gIcon)
            	return ((GIcon)getGW(params[0])).numberIndex;
            case 0x12D6: //0x12D6=int GIconGetNumberSpace(GIcon gIcon)
            	return ((GIcon)getGW(params[0])).numberSpace;
            case 0x12D7: //0x12D7=int GIconGetNumberX(GIcon gIcon)
            	return ((GIcon)getGW(params[0])).numberX;
            case 0x12D8: //0x12D8=int GIconGetNumberY(GIcon gIcon)
            	return ((GIcon)getGW(params[0])).numberY;
            case 0x12D9: //0x12D9=int GIconGetNumber(GIcon gIcon)
            	return ((GIcon)getGW(params[0])).number;
            case 0x12DA: //0x12DA=int GIconGetIconIndex(GIcon gIcon)
            	return ((GIcon)getGW(params[0])).iconIndex;
            case 0x12DB: //0x12DB=boolean GGameIconIsShow(GIcon gIcon)
            	return ((GIcon)getGW(params[0])).isShow ? TRUE : FALSE;
            case 0x12DC: //0x12DC=boolean GGontainerGetFirstInViewIndex(GContainer gContainer)
            	return ((GContainer)getGW(params[0])).firstInViewIndex;
            case 0x12DD: //0x12DD=boolean GGontainerGetLastInViewIndex(GContainer gContainer)
            	return ((GContainer)getGW(params[0])).lastInViewIndex;
	    }
    	return 0;
    }
    
    protected int syscall(short funcID, int[] params) throws Exception{
        if(funcID >= 0x4000 && funcID < 0x5000){
            return ((Quest)owner).syscall(funcID, params);
        }

        if(funcID < 0x1301) {
        	return syscall2(funcID, params);
        }
        
        switch(funcID) {
            case 0x1301: //0x1301=void ResSetGameConst(Object numberImageSet, String[] needCacheVm, int[] battleRemind, int[] intConst)
            {
                GameMain.numberImage = (ImageSet)followPointer(params[0]);
                
                Object[] cacheVm = (Object[])followPointer(params[1]);
                GameMain.needCacheVm.clear();
                
                for(int i = 0; i < cacheVm.length; i++){
                    GameMain.needCacheVm.put(cacheVm[i], cacheVm[i]);
                }
                
                GameMain.battleRemind = (int[])followPointer(params[2]);
                
                GameMain.javaWorldPacket.clear();
                int[] javaWorldPacket = (int[])followPointer(params[3]);
                
                for(int i = 0; i < javaWorldPacket.length; i++){
                    GameMain.javaWorldPacket.put(new Integer(javaWorldPacket[i]), new Integer(javaWorldPacket[i]));
                }
                
                int[] intConst = (int[])followPointer(params[4]);
                
                GameMain.numberImageIndex = intConst[0];
                GameMain.flyNumberIndex = intConst[1];
                GameMain.flyNumberBlockCount = intConst[2];
                GameMain.humanAnimateIndex = intConst[3];
                GameMain.autoSelectDistance = intConst[4];
                GameMain.forceSelectDistance = intConst[5];
                GameMain.lostSelectDistance = intConst[6];
                GameMain.positionDistance = intConst[7];
                GameMain.positionTime = intConst[8];
                GameMain.positionLimit = intConst[9];
                GameMain.dropNetplayerTime = intConst[10];
                GameMain.dropflyingStringTime = intConst[11];
                GameMain.spriteFlyingStringDelay = intConst[12];
                GameMain.spritePlayAnimateDelay = intConst[13];
                GameMain.followingNotifyServerTime = intConst[14];
                GameMain.battleModePositionTime = intConst[15];
                GameMain.keepGoingDistance = intConst[16];
                GameMain.followMaxDis = intConst[17];
                GameMain.animatePendingTick = intConst[18];
                GameMain.spriteLeavingSpeed = intConst[19];
                GameMain.netplayerShowNameDistance = intConst[20];
                GameMain.imageScalePercent = intConst[21];
            }
                break;
            case 0x1302: //0x1302=void ResCreateAnimateSet(Object[] images, byte[] ctnData)
                return makeTempObject(new PipAnimateSet((ImageSet[])followPointer(params[0]), (byte[])followPointer(params[1])));
            case 0x1303: //0x1303=void ResCreateAnimatePlayer(String animateName, Object animateSet)
            {
                AnimatePlayer animatePlayer = new AnimatePlayer((String)followPointer(params[0]));
                animatePlayer.init((PipAnimateSet)followPointer(params[1]));
                return makeTempObject(animatePlayer);
            }
            case 0x1304: //0x1304=void ResInitDeamonAnimatePlayer(Object animatePlayer, int deamonIndex)
            {
                AnimatePlayer animatePlayer = (AnimatePlayer)followPointer(params[0]);
                animatePlayer.setAnimate(params[1], Tool.ANIMATE_PLAY_TYPE_ALWAYS, Tool.NO_CALL_BACK, Tool.NO_CALL_BACK_SPRITE);
                animatePlayer.setShown(true);
            }
                break;
            case 0x1305: //0x1305=void ResSetDeamonAnimatePlayerArray(AnimatePlayer[] animatePlayers)
            {
                Object[] data = (Object[])followPointer(params[0]); 
                GameMain.clientAnimates = new AnimatePlayer[data.length];
                System.arraycopy(data, 0, GameMain.clientAnimates, 0, data.length);
            }
                break;
            case 0x1306: //0x1306=int ResAsynLoad(int loadType, String resName)
                return GameMain.resourceAsynLoader.addLoad((byte)params[0], (String)followPointer(params[1]), null);
            case 0x1307: //0x1307=int ResAsynCheck(int key)
                return GameMain.resourceAsynLoader.checkLoad(params[0])? TRUE: FALSE;
            case 0x1308: //0x1308=Object ResAsynGet(int key)
                return makeTempObject(GameMain.resourceAsynLoader.getLoad(params[0]));
            case 0x1309: //0x1309=void ResSetInitializtion(int initializing)
            {
                GameMain.initializing = params[0] == TRUE? true: false;
            }
                break;
            case 0x1310: //0x1310=Object ResGetDeamonAnimatePlayerCopy(int deamonIndex)
                return makeTempObject(GameMain.clientAnimates[params[0]].getRelateCopy());
            case 0x1311: //0x1311 = int ResGetWorldAreaId()
            {
                return GameWorld.currentAreaId;
            }
            case 0x1312: //0x1312=int ResGetGotoMapId()
                return GameWorld.playerNextMap;
            case 0x1319: //0x1319=int ResGetGotoMapInstanceId()
                return GameWorld.playerNextMapInstanceId;
            case 0x1313: //0x1313=void ResRequestPkg(String name)
            {
                GameMain.resourceManager.requestResource((String)followPointer(params[0]));
            }
                break;
            case 0x1314: //0x1314=void ResIninSpecialAnimatePlayer(int count)
            {
                GameMain.specialAnimates = new AnimatePlayer[params[0]];
            }
                break;
            case 0x1315: //0x1315=void ResSetSpecialAnimatePlayer(int index, Object animatePlayer)
            {
                GameMain.specialAnimates[params[0]] = (AnimatePlayer)followPointer(params[1]);
            }
                break;
            case 0x1316: //0x1316=Object ResGetSpecialAnimatePlayer(int specialIndex, int index, int playerType, int callbackIndex, Object callbackObject)
            {
                AnimatePlayer animatePlayer = GameMain.specialAnimates[params[0]].getCopy();
                animatePlayer.setAnimate(params[1], params[2], params[3], (IAnimateCallback)followPointer(params[4]));
                return makeTempObject(animatePlayer);
            }
            case 0x1317: //0x1317= void ResSetAnimatePlayerAnchor(Object animatePlayer, int anchor)
            {
               ((AnimatePlayer)followPointer(params[0])).setAnchor(params[1]);
               ((AnimatePlayer)followPointer(params[0])).setOrder(params[2]);
            }
                break;
            case 0x1318: //0x1318=void ResGetAnimatePlayerBox(Object animatePlayer, index)
                return makeTempObject(((AnimatePlayer)followPointer(params[0])).getAnimateBox(params[1]));
            case 0x1321: //0x1321=void ResDrawFrameBox(Object g, int x, int y, int width, int height, int[] colors)
            {
                Tool.drawFrameBox((Graphics)followPointer(params[0]), params[1], params[2], params[3], params[4], (int[])followPointer(params[5]));
            }
                break;
            case 0x1322: //0x1322=void ResDrawBoxCorner(Object g, int x, int y, int width, int height, ImageSet image, int index)
            {
                Tool.drawBoxCorner((Graphics)followPointer(params[0]), params[1], params[2], params[3], params[4], (ImageSet)followPointer(params[5]), params[6]);
            }
                break;
            case 0x1323: //0x1323=void ResDrawSpellRow(Object g, int x, int y, int width, ImageSet image, int index, int trans)
            {
                Tool.drawSpellRow((Graphics)followPointer(params[0]), params[1], params[2], params[3], (ImageSet)followPointer(params[4]), params[5], params[6]);
            }
                break;
            case 0x1324: //0x1324=void ResDrawSpellCol(Object g, int x, int y, int height, ImageSet image, int index, int trans)
            {
                Tool.drawSpellCol((Graphics)followPointer(params[0]), params[1], params[2], params[3], (ImageSet)followPointer(params[4]), params[5], params[6]);
            }
                break;
            case 0x1325: //0x1325=void ResDrawSpellArea(Object g, int x, int y, int width, int height, ImageSet image, int index, int trans)
            {
                Tool.drawSpellArea((Graphics)followPointer(params[0]), params[1], params[2], params[3], params[4], (ImageSet)followPointer(params[5]), params[6], params[7]);
            }
            	break;
            case 0x1326: {  //0x1326=void ResAnimatePlayerDraw(Object animatePlayer, Object g, int x, int y)
            	AnimatePlayer ap = (AnimatePlayer)followPointer(params[0]);
            	ap.draw((Graphics)followPointer(params[1]), params[2], params[3]);
            }            	
                break;
            case 0x1330: //0x1330=void ResStartFadeEffect(int color, int startAlpha, int endAlpha, int alphaStep, int count)
            {
                //#if (ModelID == Nokia7370) || (ModelID == SEK750) || (ModelID == Nokia6681) || (ModelID == Nokia7610)
                //#else
//                GameMain.fadeData = new int[GameMain.viewWidth * GameMain.viewHeight];
//                GameMain.fadeColor = params[0];
//                GameMain.fadeStartAlpha = params[1];
//                GameMain.fadeEndAlpha = params[2];
//                GameMain.fadeAlphaStep = params[3];
//                GameMain.fadeMaxCount = params[4];
//                
//                GameMain.fadeCurrentAlpha = GameMain.fadeStartAlpha;
//                GameMain.fadeCurrentCount = 0;
//                
//                GameMain.fadeEffect = true;
                //#endif
            }
                break;
            case 0x1331: //x1331=void ResStopFadeEffect()
            {
                GameMain.fadeEffect = false;
                GameMain.fadeData = null;
            }
                break;
            case 0x1332: //0x1332=void ResStartScreenVibra(int[] vibrasData, int tick, int count)
            {
                GameMain.vibraData = (int[])followPointer(params[0]);
                GameMain.vibraTick = params[1];
                GameMain.vibraMaxCount = params[2];
                
                GameMain.vibraCurrentIndex = 0;
                GameMain.vibraCurrentCount = 0;

                GameMain.vibraEffect = true;
            }
                break;
            case 0x1333: //0x1333=void ResStopScreenVibra()
            {
                GameMain.vibraEffect = false;
                GameMain.vibraData = null;
            }
                break;
            case 0x1334: //0x1334=void ResStartWeather(int type, int size, int count, int speed, int speedDiff, int wind, int color, int die, int dieCount, int endTime)
            {
                //#if SupportWeather == true
                GameMain.weather = new Weather(params[0], params[1], params[2], params[3], params[4], params[5], params[6], params[7], params[8], params[9]);
                //#endif
            }
                break;
            case 0x1335: //0x1335=void ResEndWeather()
            {
                //#if SupportWeather == true
                GameMain.weather = null;
                //#endif
            }
                break;
            case 0x1336: //0x1336=void ResModifyWeather(int subType, int value)
            {
                //#if SupportWeather == true
                if(GameMain.weather != null){
                    GameMain.weather.adjustPara(params[0], params[1]);
                }
                //#endif
            }
                break;
            case 0x1337: //0x1337=void ResSetTeamInfo(Hashtable teamInfo)
            {
                GameWorld.teamInfo = (SortHashtable)followPointer(params[0]);
            }
                break;
            case 0x1338: //0x1338=void ResBattleRemind()
            {
                GameMain.battleRemind[5] = 1;
            }
                break;
            case 0x1339: //0x1339=void ResCleanAnimateCache()
                AnimateCache.clearPendingReleaseAnimate();
                break;
            case 0x2000: //GetStringArray
                return makeTempObject(Tool.splitString((String)followPointer(params[0])));
            case 0x2001: //int PauseUICycle()
                pauseProcess();
                break;
            case 0x2002: //ResumeUICycle(int returnValue)
                continueProcess(params[0]);
                break;
            case 0x2003: // 0x2003=String IntToStrHex(int i)
                return makeTempObject(Integer.toHexString(params[0]));
            case 0x2004: // 0x2004=int GetSystemTime()
                return (int)(Tool.getSystemTime() / 1000L);
            case 0x2010: // 0x2010=String[] GetStringArray2(String str, String delimiter)
            {
                String str = (String)followPointer(params[0]);
                String delimiter = (String)followPointer(params[1]);
                return makeTempObject(Tool.splitString(str, delimiter.charAt(0)));
            }
            case 0x2012: // 0x2012=void ExitGame()
                Utilities.isExitGame = true;
                break;
            case 0x2013: // 0x2013=void CloseConnection()
                Utilities.closeConnection();
                break;
            case 0x2014: // 0x2014=void CancelDownload(Object ref)
                ((Utilities)followPointer(params[0])).listenVM = null;
                break;
            case 0x2017: // 0x2017=void OpenWAPPage(String url)
                //#if polish.midp2
                SanguoMIDlet.instance.platformRequest((String)followPointer(params[0]));
                //#endif
                break;
            case 0x2018: // 0x2018=void CreateConnection(String url)
                new Thread(new Utilities((String)followPointer(params[0]), Utilities.THREAD_UWAP, this, false)).start();
                break;
            case 0x2019: // 0x2019=Object DownloadPage(String url, boolean useProxy, Hashtable requestProperties, byte[] postData)
            {
                String url = (String)followPointer(params[0]);
                if(params[1] == TRUE){
                    url = "p" + url;
                }
                Utilities newObj = new Utilities(url, Utilities.THREAD_HTTP, this, false);
                newObj.requestProperties = (SortHashtable)followPointer(params[2]);
                newObj.postData = (byte[])followPointer(params[3]);
                new Thread(newObj).start();
                return makeTempObject(newObj);
            }
            case 0x201A: // 0x201A=byte[] GetDownloadData(Object ref)
            {
                return makeTempObject(((Utilities)followPointer(params[0])).lastDownloadData);
            }
            case 0x201B: //0x201B=Object DownloadPageAsync(String url, boolean useProxy, Hashtable requestProperties, byte[] postData)
            {
                String url = (String)followPointer(params[0]);
                if(params[1] == TRUE){
                    url = "p" + url;
                }
                Utilities newObj = new Utilities(url, Utilities.THREAD_HTTP, this, true);
                newObj.requestProperties = (SortHashtable)followPointer(params[2]);
                newObj.postData = (byte[])followPointer(params[3]);
                new Thread(newObj).start();
                return makeTempObject(newObj);
            }
            case 0x201C: // 0x201C=int GetSystemTick()
                return GameMain.tick;
            case 0x201D: // 0x201D=int Not(int value)
                return ~params[0];
            case 0x201E: // 0x201E=void DrawLine(Object g, int x1, int y1, int x2, int y2)
                ((Graphics)followPointer(params[0])).drawLine(params[1], params[2], params[3], params[4]);
                break;
            case 0x201F: // 0x201F=void FillRoundRect(Object g, int x, int y, int width, int height, int hr, int vr)
                ((Graphics)followPointer(params[0])).fillRoundRect(params[1], params[2], params[3], params[4], params[5], params[6]);
                break;
            case 0x2020: //0x2020=void Draw3DString(Object g, String s, int x, int y, int frontColor, int bgColor, int achor)
                Tool.draw3DString((Graphics)followPointer(params[0]), (String)followPointer(params[1]), params[2], params[3], params[4], params[5], params[6]);
                break;
            case 0x2021: //0x2021=void Draw3DString2(Object g, String s, int x, int y, int frontColor, int bgColor, int achor)
                Tool.draw3DString2((Graphics)followPointer(params[0]), (String)followPointer(params[1]), params[2], params[3], params[4], params[5], params[6]);
                break;
            case 0x2022: //0x2022=void SendSMS(String number, String content)
                new Thread(new Utilities((String)followPointer(params[0]) + "\n" + (String)followPointer(params[1]), Utilities.THREAD_SMS, this, false)).start();
                break;
            case 0x2023: //0x2023=int GetDownloadState(Object thread)
                return ((Utilities)followPointer(params[0])).state;
            case 0x2029://0x2029=int getPgaeSize(int startY)
                return (GameMain.viewHeight - params[0]) / Utilities.LINE_HEIGHT;
            case 0x2039://0x2039=void SetStrokeStyle(Graphics g,boolean dotted)
                int dotted = (params[1] != 0)? Graphics.DOTTED: Graphics.SOLID;
                ((Graphics)followPointer(params[0])).setStrokeStyle(dotted);
                break;
            case 0x203B://0x203B=void DrawBack(Graphics g,int x,int y,int width,int height)
                Tool.drawBack((Graphics)followPointer(params[0]), params[1], params[2], params[3], params[4]);
                break;
            case 0x2049://0x2049=void drawWidthString(Graphic g,String str,int x,int y,int width)
                String[] strSplit = Tool.splitString((String)followPointer(params[1]), params[3], Utilities.font);
                for(int i = 0; i < strSplit.length; i++)
                    ((Graphics)followPointer(params[0])).drawString(strSplit[i], params[2], params[3] + Utilities.LINE_HEIGHT * i, Graphics.TOP | Graphics.LEFT);
                break;
            case 0x204B://0x204B=String GetPlayerName();
                return makeTempObject(GameWorld.player.getName());
//            case 0x2050://0x2050=void DrawSelectTitle(Graphics g,String title,int x,int y,int width,boolean isMid,boolean is3DString)
//                VMWidget.drawSelectTilte((Graphics)followPointer(params[0]), (String)followPointer(params[1]), params[2], params[3], params[4], params[5] != 0, Tool.CL_PEPC, params[6] != 0);
//                break;
            case 0x3000: //0x3000=void InitWorld()
                //GameMain.world = new GameWorld();
                break;
            case 0x3001: //0x3001=void InitResourceManager()
            {
                UASegment segment = (UASegment)followPointer(params[0]);
                segment.flush();
                segment.reset();
                GameMain.resourceManager.initManager(segment);
            }
                break;
            case 0x3002: //0x3002=String[] GetUpdateList()
                return makeTempObject(GameMain.resourceManager.getRemainUpdateList());
            case 0x3003: //0x3003=void SetUpdating(boolean updating)
                GameMain.setUpdating(params[0] == VM.TRUE);
                break;
            case 0x3004:{ //0x3004=void UpdateFile(String name)
            	String name = (String)followPointer(params[0]);
                Tool.sendGetFile(name);
                //#if NewUI2
                //# GameMain.resourceManager.addDownloadFile(name);
                //#endif
            }
                break;
            case 0x3005: //0x3005=void ResetClient(int type)
                GameMain.resetClient(params[0]);
                break;
            case 0x3006: //0x3006=int GetUpdateMode()
                return GameMain.resourceManager.getUpdateMode();
            case 0x3007: //0x3007= void SyncResource(boolean whole)
                GameMain.resourceManager.syncVersion(params[0] == VM.TRUE);
                break;
            case 0x3008: //0x3008=void GetResetType()
                return GameMain.resetType;
            case 0x3009: //0x3009=void ClearClientFileDb()
                GameMain.resourceManager.clearClientFileDb();
                break;
            case 0x300A: //0x300A=void saveUnitViewPackage(int type, int id, UWAPSegment seg)
                Tool.unitViewCache.put(Tool.getSpriteKey(params[0], params[1]), followPointer(params[2]));
                break;
            case 0x300B: //0x300B=void RemoveUnitViewPackageByType(int type)
                Tool.removeUnitViewCacheByType(params[0]);
                break;
            case 0x300C: //0x300C=void RemoveUnitViewPackage(int type, int id)
                Tool.unitViewCache.remove(Tool.getSpriteKey(params[0], params[1]));
                break;
            case 0x300D: //0x300D=void ClearUnitViewPackageByType()
                Tool.unitViewCache.clear();
                break;
            case 0x300E: //0x300E=void ClearWholeData()
                GameMain.resourceManager.clearWholeData();
                break;
            case 0x301C: // 0x301B=Vector FormatString(String msg, int w)
            {
                String msg1 = (String)followPointer(params[0]);
                return makeTempObject(Tool.formatString(msg1, params[1], Utilities.font, false));
            }
//            case 0x301D: // 0x301D=int DrawNumberString(Object g, String str, int x, int y)
//                return VMWidget.drawAttrNum((String)followPointer(params[1]), (Graphics)followPointer(params[0]), params[2], params[3], Graphics.TOP | Graphics.LEFT);
            case 0x3030: // 0x3030=void DrawImageNumber(Object g, ImageSet numberImg, int startIndex, String str, int x, int y, int space, int anchor)
                Tool.drawImageNumber((Graphics)followPointer(params[0]), (ImageSet)followPointer(params[1]), params[2], (String)followPointer(params[3]), params[4], params[5], params[6], params[7]);
                break;
            case 0x3040: // 0x3040=String[] GetJavaStringArray(int count)
                return makeTempObject(new String[params[0]]);
            case 0x3041: //0x3041=ImageSet[] GetJavaImageArray(int count)
                return makeTempObject(new ImageSet[params[0]]);
            case 0x3045: // 0x3045=int GetStringsMaxWidth(String[] strings) 获取字符串数组的最大宽度
                return Tool.getStringsMaxWidth((String[])followPointer(params[0]), false);
            case 0x3046: // 0x3045=int GetMixedStringsMaxWidth(String[] strings) 获取混合字符串数组的最大宽度
                return Tool.getStringsMaxWidth((String[])followPointer(params[0]), true);
            case 0x3051: // 0x3051=byte[] GZIP_Inflate(byte[] data)
            	return makeTempObject(Tool.inflate((byte[])followPointer(params[0])));
            case 0x3052: // 0x3052=int GetServerTime()
            	return Utilities.getServerTime();           	
            case 0x3060: // 0x3060=int GetVMCounter(int counterTime) #获得一个计时器
            	return VMCounter.createVMCounter(params[0]);    
            case 0x3061: // 0x3061=int GetSaveTimeSec(int vmCounterKey)  #获得计时器的剩余秒数
            	return VMCounter.getSaveTimeSec(params[0]); 
            case 0x3062: // 0x3062=int GetSaveTimeMillis(int vmCounterKey)  #获得计时器的剩余毫秒数
            	return VMCounter.getSaveTimeMillis(params[0]);   
            case 0x3063: // 0x3063=void RemoveVMCounter(int vmCounterKey)  #删除一个计时器
            	VMCounter.removeVMCounter(params[0]);
            	break;
            case 0x3064: // 0x3064=void RemoveAllVMCounter()  #删除所有计时器
            	VMCounter.removeAllVMCounters();
            	break;
            case 0x3070: // 0x3070=void ArrayCopy(int[] src, int start, int[] des, int start, int lenght)  #获得计时器的剩余毫秒数
                if(params[4] > 0){
                    System.arraycopy((int[])followPointer(params[0]), params[1], followPointer(params[2]), params[3], params[4]);
                }
                break;
            case 0x3080: // 0x3080=Object VMCallback(String funcName, int callBackId, Object param)  #回调脚本内的一个方法            	
            	return ((VMGame)owner).callback((String)followPointer(params[0]), new Object[]{new Integer(params[1]), (Object)followPointer(params[2])});
            case 0x3082: // 0x3082=void CloseVM(String vmId) #关闭一个指定的脚本
            	VMGame.closeVM((String)followPointer(params[0]));
            	break;
            case 0x3083: // 0x3083=void CloseVMByVMKey(int vmKey) #关闭一个指定的脚本
            	VMGame.closeVM(params[0]);
            	break;
            case 0x3084: // 0x3084=Object CreateNetPlayer(int id) #创建一个netplayer, 目前用于角色列表中显示的动画
            	return makeTempObject(GameNetPlayer.createGameNetPlayer(params[0], 0));
            case 0x3085: //0x3085=Hashtable GetVMGames() #获得VMGAME
            	return makeTempObject(VMGame.getVMGames());
            case 0x3090: // 0x3090=boolean QuestTargetDone(int questId, int targetIndex) #获得任务目标是否完成
            	Quest quest = Quest.findQuest(params[0], false);
            	if(quest != null) {
            		return quest.getTargetStatus(params[1]) ? TRUE : FALSE;            		
            	}
            	return FALSE;
            case 0x3091: //0x3091=Object StringBuffer_Create()
            	return  makeTempObject(new StringBuffer()); 
            case 0x3092: //0x3092=void StringBuffer_Append(Object strBuffer, String str)
            	((StringBuffer)followPointer(params[0])).append((String)followPointer(params[1]));
            	break;
            case 0x3093: //0x3093=void StringBuffer_SetLength(Object strBuffer, int newLength)
            	((StringBuffer)followPointer(params[0])).setLength(params[1]);
            	break;
            case 0x3094: //0x3094=void StringBuffer_ToString(Object strBuffer)
            	return  makeTempObject(((StringBuffer)followPointer(params[0])).toString());
            case 0x5001: //0x5001=void vm_request_animates(Object processor, String[] names)
                ((GameSprite)followPointer(params[0])).vm_request_animates((String[])followPointer(params[1]));
                break;
            case 0x5002: //0x5002=void vm_set_sprite_show(Object processor, boolean show)
                ((GameSprite)followPointer(params[0])).vm_set_sprite_show(params[1] == VM.TRUE);
                break;
            case 0x5003: //vm_add_animate(Object processor, String animateName)
                ((GameSprite)followPointer(params[0])).vm_add_animate((String)followPointer(params[1]));
                break;
            case 0x5004: //vm_set_animate_show(Object processor, String animateName, boolean show)
                ((GameSprite)followPointer(params[0])).vm_set_animate_show((String)followPointer(params[1]), params[2] == VM.TRUE);
                break;
            case 0x5005: //0x5005=void vm_add_animate_replace_images(Object processor, String animateName, String[] imageNames)
                ((GameSprite)followPointer(params[0])).vm_add_animate_replace_images((String)followPointer(params[1]), (String[])followPointer(params[2]));
                break;
            case 0x5006: //0x5006=void vm_test_animates_ok(Object processor, String[] names)
                return ((GameSprite)followPointer(params[0])).vm_test_animate_ok((String[])followPointer(params[1]))? VM.TRUE: VM.FALSE;
            case 0x5007: //0x5007=booelan vm_sprite_has_animate(Object processor, String name)
                return ((GameSprite)followPointer(params[0])).vm_sprite_has_animate((String)followPointer(params[1]))? VM.TRUE: VM.FALSE;
            case 0x5008: //int[] vm_sprite_get_animate_box(Object processor)
                return makeTempObject(((GameSprite)followPointer(params[0])).vm_sprite_get_animate_box());
            case 0x5009: //String vm_sprite_get_name(Object processor)
                return makeTempObject(((GameSprite)followPointer(params[0])).vm_sprite_get_name());
            case 0x5010: //void vm_sprite_set_name(Object processor, String _name)
                ((GameSprite)followPointer(params[0])).vm_sprite_set_name((String)followPointer(params[1]));
                break;
            case 0x5011: //0x5011=int vm_sprite_get_faction(Object processor)
                return ((GameSprite)followPointer(params[0])).vm_sprite_get_faction();
            case 0x5012: //0x5012=void vm_sprite_set_faction(Object processor, int _faction)
                ((GameSprite)followPointer(params[0])).vm_sprite_set_faction(params[1]);
                break;
            case 0x5013: //0x5013=int vm_sprite_get_level(Object processor)
                return ((GameSprite)followPointer(params[0])).vm_sprite_get_level();
            case 0x5014: //0x5014=void vm_sprite_set_level(Object processor, int _level)
                ((GameSprite)followPointer(params[0])).vm_sprite_set_level(params[1]);
                break;
            case 0x5015: //0x5015=int vm_sprite_get_hp(Object processor)
                return ((GameSprite)followPointer(params[0])).vm_sprite_get_hp();
            case 0x5016: //0x5016=int vm_sprite_get_hp_max(Object processor)
                return ((GameSprite)followPointer(params[0])).vm_sprite_get_hp_max();
            case 0x5017: //0x5017=int vm_sprite_get_mp(Object processor)
                return ((GameSprite)followPointer(params[0])).vm_sprite_get_mp();
            case 0x5018: //0x5018=int vm_sprite_get_mp_max(Object processor)
                return ((GameSprite)followPointer(params[0])).vm_sprite_get_mp_max();
            case 0x5019: //0x5019=int[] vm_sprite_get_pos(Object processor)
                return makeTempObject(((GameSprite)followPointer(params[0])).vm_sprite_get_pos());
            case 0x5020: //0x5020=void vm_sprite_set_pos(Object processor, int x, int y)
                ((GameSprite)followPointer(params[0])).vm_sprite_set_pos(params[1], params[2]);
                break;
            case 0x5021: //0x5021=int vm_sprite_get_map_id(Object processor)
                return ((GameSprite)followPointer(params[0])).vm_sprite_get_map_id();
            case 0x5022: //0x5022=int vm_sprite_get_map_instance_id(Object processor)
                return ((GameSprite)followPointer(params[0])).vm_sprite_get_map_instance_id();
            case 0x5023: //0x5023=void vm_game_set_animate_index(Object processor, String _animateName, int _index, int _playType, int _callBackIndex)
                ((GameSprite)followPointer(params[0])).vm_game_set_animate_index((String)followPointer(params[1]), params[2], params[3], params[4]);
                break;
            case 0x5024: //0x5024=int vm_game_sprite_play_animate(Object processor, Object _animatePlayer, int _anchor, int _order)
                return ((GameSprite)followPointer(params[0])).vm_game_sprite_play_animate((AnimatePlayer)followPointer(params[1]), params[2], params[3]);
            case 0x5025: //0x5025=void vm_sprite_set_can_attack(Object processor, boolean _canAttact)
                ((GameSprite)followPointer(params[0])).vm_sprite_set_can_attack(params[1] == VM.TRUE);
                break;
            case 0x5026: //0x5026=int vm_sprite_get_can_attack(Object processor)
                return ((GameSprite)followPointer(params[0])).vm_sprite_get_can_attack()? VM.TRUE: VM.FALSE;
            case 0x5027: //0x5027=void vm_sprite_set_can_select(Object processor, boolean _canSelect)
                ((GameSprite)followPointer(params[0])).vm_sprite_set_can_select(params[1] == VM.TRUE);
                break;
            case 0x5028: //0x5028=int vm_sprite_get_can_select(Object processor)
                return ((GameSprite)followPointer(params[0])).vm_sprite_get_can_select()? VM.TRUE: VM.FALSE;
            case 0x5029: //0x5029=int[] vm_sprite_get_animate_para(Object processor)
                return makeTempObject(((GameSprite)followPointer(params[0])).vm_sprite_get_animate_para());
            case 0x5030: //0x5030=void vm_game_sprite_stop_animate(Object processor, int _animatePlayerKey)
                ((GameSprite)followPointer(params[0])).vm_game_sprite_stop_animate(params[1]);
                break;
            case 0x5031: //0x5031=void vm_game_sprite_add_fly_string(Object _processor, int _type, String _str, int _number, int _paletteColor, int _distance, int _time, int _order, int _delayTick)
                ((GameSprite)followPointer(params[0])).vm_game_sprite_add_fly_string(params[1], (String)followPointer(params[2]), params[3], params[4], params[5], params[6], params[7], params[8]);
                break;
            case 0x5032: //0x5032=void vm_game_sprite_add_across_fly_string(Object _processor, int _type, String _str, int _number, int _paletteColor, int _dir, int _hCycleCount, int _hSpeed, int _stopCycleCount, int _vCycleCount, int _vSpeed, int _order, int _delayTick)
                ((GameSprite)followPointer(params[0])).vm_game_sprite_add_across_fly_string(params[1], (String)followPointer(params[2]), params[3], params[4], params[5], params[6], params[7], params[8], params[9], params[10], params[11], params[12]);
                break;
            case 0x5033: //0x5033=void vm_game_sprite_add_vibar(Object _processor, int _time, int _distance)
                ((GameSprite)followPointer(params[0])).vm_game_sprite_add_vibar(params[1], params[2]);
                break;
            case 0x5034: //0x5034=void vm_game_sprite_set_die(Object _processor, boolean _die)
                ((GameSprite)followPointer(params[0])).vm_game_sprite_set_die(params[1] == VM.TRUE);
                break;
            case 0x5035: //0x5035=void vm_game_sprite_set_waypoint_animate(Object _processor, int _moveAnimate, int _stopAnimate, int _chaseMoveAnimate, int _chaseStopAnimate, int _pendingStopAnimate, int _chasePendingStopAnimate)
                ((GameSprite)followPointer(params[0])).vm_game_sprite_set_waypoint_animate(params[1], params[2], params[3], params[4], params[5], params[6]);
                break;
            case 0x5036: //0x5036=void vm_sprite_set_head_string_config(Object processor, int type, int space, int drawMode, int offsetX, int offsetY, int order)
                ((GameSprite)followPointer(params[0])).vm_sprite_set_head_string_config(params[1], params[2], params[3], params[4], params[5], params[6]);
                break;
            case 0x5037: //0x5037=void vm_sprite_add_head_string(Object _processor, String _str, int _color, Object _image, int[] _imageIndex)
                ((GameSprite)followPointer(params[0])).vm_sprite_add_head_string((String)followPointer(params[1]), params[2], followPointer(params[3]), (int[])followPointer(params[4]));
                break;
            case 0x5038: //0x5038=void vm_sprite_clear_head_string(Object processor)
                ((GameSprite)followPointer(params[0])).vm_sprite_clear_head_string();
                break;
            case 0x5039: //0x5039=void vm_sprite_set_head_string_show(Object processor, boolean _show)
                ((GameSprite)followPointer(params[0])).vm_sprite_set_head_string_show(params[1] == VM.TRUE);
                break;
            case 0x5040: //0x5040=void vm_sprite_set_collision(Object processor, boolean _collision)
                ((GameSprite)followPointer(params[0])).vm_sprite_set_collision(params[1] == VM.TRUE);
                break;
            case 0x5041: //0x5041=int vm_sprite_get_type(Object _processor)
                return ((GameSprite)followPointer(params[0])).vm_sprite_get_type();
            case 0x5042: //0x5042=int vm_sprite_get_id(Object _processor)
                return ((GameSprite)followPointer(params[0])).vm_sprite_get_id();
            case 0x5043: //0x5043=int vm_sprite_get_instanceid(Object _processor)
                return ((GameSprite)followPointer(params[0])).vm_sprite_get_instanceid();
            case 0x5044: //0x5044=int vm_sprite_get_dir(Object _processor)
                return ((GameSprite)followPointer(params[0])).vm_sprite_get_dir();
            case 0x5045: //0x5045=Object vm_sprite_get_animate_player(Object _processor, String _animateName)
                return makeTempObject(((GameSprite)followPointer(params[0])).vm_sprite_get_animate_player((String)followPointer(params[1])));
            case 0x5046: //0x5046=void vm_sprite_send_command(Object _processor, int _command, Object _data)
                ((GameSprite)followPointer(params[0])).vm_sprite_send_command(params[1], followPointer(params[2]));
                break;
            case 0x5047: //0x5047=int vm_sprite_start_chase_position(Object _processor, int _distanceAllow, int _targetX, int _targetY, int _speed, int[] _callbackPara, boolean _always)
                return ((GameSprite)followPointer(params[0])).vm_sprite_start_chase_position(params[1], params[2], params[3], params[4], (int[])followPointer(params[5]), params[6] == VM.TRUE)? VM.TRUE: VM.FALSE;
            case 0x5048: //0x5048=int vm_sprite_start_chase_sprite(Object _processor, int _distanceAllow, int _speed, Object _targetSprite, int[] _callbackPara, boolean _always)
                return ((GameSprite)followPointer(params[0])).vm_sprite_start_chase_sprite(params[1], params[2], (GameSprite)followPointer(params[3]), (int[])followPointer(params[4]), params[5] == VM.TRUE)? VM.TRUE: VM.FALSE;
            case 0x5049: //0x5049=void vm_sprite_clear_chase(Object _processor)
                ((GameSprite)followPointer(params[0])).vm_sprite_clear_chase();
                break;
            case 0x5050: //0x5050=void vm_sprite_set_dir(Object _processor, int _dir)
                ((GameSprite)followPointer(params[0])).vm_sprite_set_dir(params[1]);
                break;
            case 0x5051: //0x5051=void vm_sprite_adjust_animate_dir(Object _processor, int _targetInstanceId, boolean _setAnimate)
                ((GameSprite)followPointer(params[0])).vm_sprite_adjust_animate_dir(params[1], params[2] == VM.TRUE);
                break;
            case 0x5052: //0x5052=String[] vm_sprite_get_extra_name(Object _processor)
                return makeTempObject(((GameSprite)followPointer(params[0])).vm_sprite_get_extra_name());
            case 0x5053: //0x5053=int vm_sprite_get_speed(Object _processor)
                return ((GameSprite)followPointer(params[0])).vm_sprite_get_speed();
            case 0x5054: //0x5054=void vm_sprite_set_speed(Object _processor, int _speed)
                ((GameSprite)followPointer(params[0])).vm_sprite_set_speed(params[1]);
                break;
            case 0x5055: //0x5055=int vm_sprite_get_animate_sub_dir(Object _processor)
                return ((GameSprite)followPointer(params[0])).vm_sprite_get_animate_sub_dir();
            case 0x5056: //0x5056=void vm_sprite_set_animate_sub_dir(Object _processor, int _subDir)
                ((GameSprite)followPointer(params[0])).vm_sprite_set_animate_sub_dir(params[1]);
                break;
            case 0x5057: //0x5057=void vm_sprite_set_attacking(Object _processor, boolean _attacking)
                ((GameSprite)followPointer(params[0])).vm_sprite_set_attacking(params[1] == VM.TRUE);
                break;
            case 0x5058: //0x5058=int vm_sprite_get_weapon_type(Object _processor)
                return ((GameSprite)followPointer(params[0])).vm_sprite_get_weapon_type();
            case 0x5059: //0x5059=void vm_sprite_set_weapon_type(Object _processor, int _weaponType)
                ((GameSprite)followPointer(params[0])).vm_sprite_set_weapon_type(params[1]);
                break;
            case 0x5060: //0x5060=int vm_sprite_get_animate_dir(Object _processor)
                return ((GameSprite)followPointer(params[0])).vm_sprite_get_animate_dir();
            case 0x5061: //0x5061=void vm_sprite_set_animate_dir(Object _processor, int _animateDir)
                ((GameSprite)followPointer(params[0])).vm_sprite_set_animate_dir(params[1]);
                break;
            case 0x5062: //0x5062=int vm_sprite_get_move(Object _processor)
                return ((GameSprite)followPointer(params[0])).vm_sprite_get_move()? VM.TRUE: VM.FALSE;
            case 0x5063: //0x5063=void vm_sprite_set_following(Object _processor, boolean isFollowing)
                ((GameSprite)followPointer(params[0])).vm_sprite_set_following(params[1] == VM.TRUE);
                break;
            case 0x5064: //0x5064=int vm_sprite_get_following(Object _processor)
                return ((GameSprite)followPointer(params[0])).vm_sprite_get_following()? VM.TRUE: VM.FALSE;
            case 0x5065: //0x5065=Object vm_sprite_get_follow_owner(Object _processor)
                return makeTempObject(((GameSprite)followPointer(params[0])).vm_sprite_get_follow_owner());
            case 0x5066: //0x5066=void vm_sprite_set_leaving_pos(Object _processor)
                ((GameSprite)followPointer(params[0])).vm_sprite_set_leaving_pos();
                break;
            case 0x5067: //0x5067=int vm_sprite_test_status(Object _processor, int _status)
                return ((GameSprite)followPointer(params[0])).vm_sprite_test_status(params[1])? VM.TRUE: VM.FALSE;
            case 0x5068: //0x5068=void vm_sprite_add_status(Object _processor, int _status)
                ((GameSprite)followPointer(params[0])).vm_sprite_add_status(params[1]);
                break;
            case 0x5069: //0x5069=void vm_sprite_remove_status(Object _processor, int _status)
                ((GameSprite)followPointer(params[0])).vm_sprite_remove_status(params[1]);
                break;
            case 0x5070: //0x5070=void vm_sprite_clear_status(Object _processor)
                ((GameSprite)followPointer(params[0])).vm_sprite_clear_status();
                break;
            case 0x5071: //0x5071=void vm_sprite_set_force_way_point(Object _processor, boolean _hasReturnPoint, int _forceSpeed, int _returnX, int _returnY, int[] _wayPointList)
                ((GameSprite)followPointer(params[0])).vm_sprite_set_force_way_point(params[1] == VM.TRUE, params[2], params[3], params[4], (int[])followPointer(params[5]));
                break;
            case 0x5072: //0x5072=void vm_sprite_clear_force_way_point(Object _processor)
                ((GameSprite)followPointer(params[0])).vm_sprite_clear_force_way_point();
                break;
            case 0x5073: //0x5073=int vm_sprite_get_speed_addon(Object _processor)
                return ((GameSprite)followPointer(params[0])).vm_sprite_get_speed_addon();
            case 0x5074: //0x5074=void vm_sprite_set_speed_addon(Object _processor, int _speedAddon)
                ((GameSprite)followPointer(params[0])).vm_sprite_set_speed_addon(params[1]);
                break;
            case 0x5075: //0x5075=void vm_set_animate_not_icon(Object processor, String animateName)
                ((GameSprite)followPointer(params[0])).vm_set_animate_not_icon((String)followPointer(params[1]));
                break;
            case 0x5076: //0x5076=void vm_game_sprite_set_animate_layer(Object _processor, String _animateName, int _layer)
                ((GameSprite)followPointer(params[0])).vm_game_sprite_set_animate_layer((String)followPointer(params[1]), params[2]);
                break;
            case 0x5077: //0x5077=void vm_game_sprite_regroup_animate(Object _processor)
                ((GameSprite)followPointer(params[0])).vm_game_sprite_regroup_animate();
                break;
            case 0x5078: //0x5078=int vm_sprite_is_out_view(Object _processor)
                return ((GameSprite)followPointer(params[0])).vm_sprite_is_out_view()? VM.TRUE: VM.FALSE;
            case 0x5079: //0x5079=void vm_set_game_sprite_horse(Object _processor, boolean riding)
                ((GameSprite)followPointer(params[0])).vm_set_game_sprite_horse(params[1] == VM.TRUE);
                break;
            case 0x5080: //0x5080=void vm_set_game_sprite_hold(Object _processor, boolean hold)
                ((GameSprite)followPointer(params[0])).vm_set_game_sprite_hold(params[1] == VM.TRUE);
                break;
            case 0x5081: //0x5081=int vm_sprite_is_team_state(Object _processor)
                return ((GameSprite)followPointer(params[0])).vm_sprite_is_team_state()? VM.TRUE: VM.FALSE;
            case 0x5082: //0x5082=void vm_sprite_set_team_state(Object _processor, boolean isTeamState)
                ((GameSprite)followPointer(params[0])).vm_sprite_set_team_state(params[1] == VM.TRUE);
                break;
            case 0x5083: //0x5083=void vm_sprite_set_animate_draw_replace_data(Object _processor, String _animateName, int _sourceImageId, int[] _sourceFrameId, int _destImageId, int _destFrameId[])
                ((GameSprite)followPointer(params[0])).vm_sprite_set_animate_draw_replace_data((String)followPointer(params[1]), params[2], (int[])followPointer(params[3]), params[4], (int[])followPointer(params[5]));
                break;
            case 0x5084: //0x5084=void vm_remove_animate(Object processor, String animateName)
                ((GameSprite)followPointer(params[0])).vm_remove_animate((String)followPointer(params[1]));
                break;
            case 0x5085: //0x5085=void vm_sprite_set_mini_map_color(int[] miniMapColor)
                ((GameSprite)followPointer(params[0])).vm_sprite_set_mini_map_color((int[])followPointer(params[1]));
                break;
            case 0x5086: //0x5086=void vm_sprite_set_mini_map_show(boolean show)
                ((GameSprite)followPointer(params[0])).vm_sprite_set_mini_map_show(params[1] == VM.TRUE);
                break;
            case 0x5087: //0x5087=void vm_sprite_get_mini_map_show(GameSprite processor)
            	return ((GameSprite)followPointer(params[0])).vm_sprite_get_mini_map_show();   
            case 0x5088: //foreceMiniMapShow(Object proc, int b)
            	((GameSprite)followPointer(params[0])).forceMiniMapShow(params[1]);
            	break;
            case 0x5089: //getForceMiniMapShow(Object proc)
            	return ((GameSprite)followPointer(params[0])).getForceMiniMapShow();
            case 0x5101: //0x5101=void vm_role_change_target()
                GameWorld.player.vm_role_change_target();
                break;
            case 0x5102: //0x5102=void vm_role_set_auto_select(boolean _autoSelect)
            	//#if NewUI2
            	//# GameWorld.player.vm_role_set_auto_select(params[0]);
            	//#else
                GameWorld.player.vm_role_set_auto_select(params[0] == VM.TRUE);
                //#endif
                break;
            case 0x5103: //0x5103=int vm_role_get_target_type()
                return GameWorld.player.vm_role_get_target_type();
            case 0x5104: //0x5104=int vm_role_get_target_id()
                return GameWorld.player.vm_role_get_target_id();
            case 0x5105: //0x5105=String vm_role_get_target_name()
                return makeTempObject(GameWorld.player.vm_role_get_target_name());
            case 0x5106: //0x5106=Object vm_role_get_target()
                return makeTempObject(GameWorld.player.vm_role_get_target());
            case 0x5107: //0x5107=int vm_role_get_target_instanceid()
                return GameWorld.player.vm_role_get_target_instanceid();
            case 0x5108: //0x5108=void vm_set_game_role_change_2468_mode(boolean yesorno)
                GameWorld.player.vm_set_game_role_change_2468_mode(params[0] == VM.TRUE);
                break;
            case 0x5109: //0x5109=int vm_sprite_get_dir_key_valid()
                return GameWorld.player.vm_sprite_get_dir_key_valid()? VM.TRUE: VM.FALSE;
            case 0x510A: //0x510A = int vm_role_get_target_faction()
            	return GameWorld.player.target==null?-1:GameWorld.player.target.faction;
            case 0x510B: //0x510B = int vm_role_dead()
            	return GameWorld.player.die?1:0;
            case 0x510C: //0x510C = int vm_role_target_dead()
            	return GameWorld.player.target==null?-1:GameWorld.player.target.die?1:0;
            case 0x5110: //0x5110=void vm_sprite_set_dir_key_valid(boolean _dirKeyValid)
                GameWorld.player.vm_sprite_set_dir_key_valid(params[0] == VM.TRUE);
            case 0x5111: //0x5111=int vm_sprite_get_fire_key_valid()
                return GameWorld.player.vm_sprite_get_fire_key_valid()? VM.TRUE: VM.FALSE;
            case 0x5112: //0x5112=void vm_sprite_set_fire_key_valid(boolean _fireKeyValid)
                GameWorld.player.vm_sprite_set_fire_key_valid(params[0] == VM.TRUE);
                break;
            case 0x5113: //0x5113=void vm_game_role_set_battle_mode(boolean _inBattle)
                GameWorld.player.vm_game_role_set_battle_mode(params[0] == VM.TRUE);
                break;
            case 0x5114: //0x5114=void vm_role_clear_target()
                GameWorld.player.vm_role_clear_target();
                break;
            case 0x5115: //0x5115=void vm_game_role_set_target(int _instanceId)
                GameWorld.player.vm_game_role_set_target(params[0]);
                break;
            case 0x5116: //0x5116=void vm_game_role_set_select_const(int enemyNpcAutoDist, int allyNpcAutoDist, int enemyPlayerAutoDist, int allyPlayerAutoDist, int teamerAutoDist, int enemyNpcForceDist, int allyNpcForceDist, int enemyPlayerForceDist, int allyPlayerForceDist, int teamerForceDist)
                GameWorld.player.vm_game_role_set_select_const(params[0], params[1], params[2], params[3], params[4], params[5], params[6], params[7], params[8], params[9]);
                break;
            case 0x5117: //0x5117=void vm_role_test_and_change_target(int enemyInstanceId)
                GameWorld.player.vm_role_test_and_change_target(params[0]);
                break;
            case 0x5118: //0x5118=void vm_game_role_point_move(int pointMoveDir)
                GameWorld.player.vm_game_role_point_move(params[0]);
                break;
            case 0x5201: //0x5201=String vm_game_npc_get_animate_name(Object _processor)
                return makeTempObject(((GameNpc)followPointer(params[0])).vm_game_npc_get_animate_name());
            case 0x5202: //0x5202=void vm_game_set_npc_image_id(Object _processor, int _imageId)
                ((GameNpc)followPointer(params[0])).vm_game_set_npc_image_id(params[1]);
                break;
            case 0x5203: //0x5203=void vm_game_set_npc_quest_id(Object _processor, int _questId)
                ((GameNpc)followPointer(params[0])).vm_game_set_npc_quest_id(params[1]);
                break;
            case 0x5204: //0x5204=int vm_game_get_npc_quest_id(Object _processor)
                return ((GameNpc)followPointer(params[0])).vm_game_get_npc_quest_id();
            case 0x5205: //0x5205=int vm_game_npc_is_human(Object _processor)
                return ((GameNpc)followPointer(params[0])).vm_game_npc_is_human()? VM.TRUE: VM.FALSE;
            case 0x5206: //0x5206=int vm_game_npc_get_animate_count(Object _processor)
                return ((GameNpc)followPointer(params[0])).vm_game_npc_get_animate_count();
            case 0x5207: //0x5207=void vm_game_npc_set_need_collision(Object _processor, boolean _needCollision)
                ((GameNpc)followPointer(params[0])).vm_game_npc_set_need_collision(params[1] == VM.TRUE);
                break;
            case 0x5301: //0x5301=void vm_free_icon(Object icon)
                ((GameIcon)followPointer(params[0])).vm_free_icon();
                break;
            case 0x5302: //0x5302=void vm_set_icon_position(Object icon, int x, int y)
                ((GameIcon)followPointer(params[0])).vm_set_icon_position(params[1], params[2]);
                break;
            case 0x5303: //0x5303=void vm_set_icon_clip(Object icon, int x, int y, int w, int h)
                ((GameIcon)followPointer(params[0])).vm_set_icon_clip(params[1], params[2], params[3], params[4]);
                break;
            case 0x5304: //0x5304=void vm_set_icon_show(Object icon, boolean show)
                ((GameIcon)followPointer(params[0])).vm_set_icon_show(params[1] == VM.TRUE);
                break;
            case 0x5305: //0x5305=void vm_set_icon_index(Object icon, int index)
                ((GameIcon)followPointer(params[0])).vm_set_icon_index(params[1]);
                break;
            case 0x5306: //0x5306=void vm_set_icon_play_animate(Object icon, boolean playAnimate)
                ((GameIcon)followPointer(params[0])).vm_set_icon_play_animate(params[1] == VM.TRUE);
                break;
            case 0x5307: //0x5307=int vm_get_icon_id(Object icon)
                return ((GameIcon)followPointer(params[0])).vm_get_icon_id();
            case 0x5308: //0x5308=Object vm_get_icon_father(Object icon)
                return makeTempObject(((GameIcon)followPointer(params[0])).vm_get_icon_father());
            case 0x5401: //0x5401=void vm_player_set_no_need_remove(Object _processor, boolean _need)
                ((GameNetPlayer)followPointer(params[0])).vm_player_set_no_need_remove(params[1] == VM.TRUE);
                break;
            case 0x5402: //0x5402=int vm_player_get_no_need_remove(Object _processor)
                return ((GameNetPlayer)followPointer(params[0])).vm_player_get_no_need_remove()? VM.TRUE: VM.FALSE;
            case 0x5403: //0x5403=int vm_player_get_be_skiped(Object _processor)
                return ((GameNetPlayer)followPointer(params[0])).vm_player_get_be_skiped()? VM.TRUE: VM.FALSE;
            case 0x5501: //0x5501=Object vm_create_icon(int _type, int _id, int _animateIndex)
                return makeTempObject(GameWorld.instance.vm_create_icon(params[0], params[1], params[2]));
            case 0x5502: //0x5502=void vm_close_event(Object _questProcessor, int[] _data)
                GameWorld.instance.vm_close_event((Quest)followPointer(params[0]), (int[])followPointer(params[1]));
                break;
            case 0x5503: //0x5503=Object vm_create_icon2(Object _processor, int _animateIndex)
                return makeTempObject(GameWorld.instance.vm_create_icon2((GameSprite)followPointer(params[0]), params[1]));
            case 0x5506: //0x5506=void vm_world_set_mini_map_config(UWAPSegment _seg)
                GameWorld.instance.vm_world_set_mini_map_config((UASegment)followPointer(params[0]));
                break;
            case 0x5507: //0x5507=void vm_game_set_mini_map_show(boolean show)
                GameWorld.instance.vm_game_set_mini_map_show(params[0] == VM.TRUE);
                break;
            case 0x5508: //0x5508=Object vm_game_get_netplayer_list()
                return makeTempObject(GameWorld.instance.vm_game_get_netplayer_list());
            case 0x5509: //0x5509=int vm_world_get_target_distance()
                return GameWorld.instance.vm_world_get_target_distance();
            case 0x5510: //0x5510=void vm_game_to_map(int _mapId, int _mapInstanceId, int _x, int _y)
                GameWorld.instance.vm_game_to_map(params[0], params[1], params[2], params[3]);
                break;
            case 0x5511: //0x5511=void vm_game_add_follower(int _ownerInstanceId, int _followerInstanceId)
                GameWorld.instance.vm_game_add_follower(params[0], params[1]);
                break;
            case 0x5512: //0x5512=void vm_game_del_follower()
                GameWorld.instance.vm_game_del_follower();
                break;
            case 0x5513: //0x5513=int vm_world_get_target_can_attack()
                return GameWorld.instance.vm_world_get_target_can_attack()? VM.TRUE: VM.FALSE;
            case 0x5514: //0x5514=Object vm_game_get_gamesprite_list()
                return makeTempObject(GameWorld.instance.vm_game_get_gamesprite_list());
            case 0x5515: //0x5515=int vm_game_get_map_id()
                return GameWorld.instance.vm_game_get_map_id();
            case 0x5516: //0x5516=void vm_game_do_touch_npc(Object _processor)
                GameWorld.instance.vm_game_do_touch_npc((GameSprite)followPointer(params[0]));
                break;
            case 0x5517: //0x5517=void vm_request_destroy_sprite(Object _sprite)
                GameWorld.instance.vm_request_destroy_sprite((GameSprite)followPointer(params[0]));
                break;
            case 0x5518: //0x5518=String vm_game_current_landmark_name()
                return makeTempObject(GameWorld.instance.vm_game_current_landmark_name());
            case 0x5519: //0x5519=int vm_game_get_hmsg_count()
                return GameWorld.instance.vm_game_get_hmsg_count();
            case 0x5520: //0x5520=int vm_game_get_vmsg_count()
                return GameWorld.instance.vm_game_get_vmsg_count();
            case 0x5521: //0x5521=int vm_game_get_dis(Object _gameSprite1, Object _gameSprite2)
                return GameWorld.instance.vm_game_get_dis((GameSprite)followPointer(params[0]),(GameSprite)followPointer(params[1]));
            case 0x5522: //0x5522=void vm_game_do_destroy_sprite(int _id, int _instanceId, boolean testLeaving)
                GameWorld.instance.vm_game_do_destroy_sprite(params[0], params[1], params[2] == VM.TRUE);
                break;
            case 0x5523: //0x5523=void vm_world_set_in_loading(boolean _inLoading)
                GameWorld.instance.vm_world_set_in_loading(params[0] == VM.TRUE);
                break;
            case 0x5524: //0x5524=void vm_world_set_netplayer_name_near_show(boolean _nearShow)
                GameWorld.instance.vm_world_set_netplayer_name_near_show(params[0] == VM.TRUE);
                break;
            case 0x5525: //0x5525=int[] vm_game_get_mini_map_size()
                return makeTempObject(GameWorld.instance.vm_game_get_mini_map_size());
            case 0x5526: //0x5526=int vm_world_get_is_team_member(int _instanceId)
                return GameWorld.instance.vm_world_get_is_team_member(params[0])? VM.TRUE: VM.FALSE;
            case 0x5527: //0x5527=int vm_game_add_quest_etf(int _questId, int _type, int _startNpcId, int _endNpcId, byte[] _etf)
                return GameWorld.instance.vm_game_add_quest_etf(params[0], params[1], params[2], params[3], (byte[])followPointer(params[4]));
            case 0x5528: //0x5528=void vm_game_add_quest(int _questId, int _type, int _startNpcId, int _endNpcId)
                GameWorld.instance.vm_game_add_quest(params[0], params[1], params[2], params[3]);
                break;
            case 0x5529: //0x5529=void vm_game_remove_quest(int _questId, int _startNpcId, int _endNpcId)
                GameWorld.instance.vm_game_remove_quest(params[0], params[1], params[2]);
                break;
            case 0x5530: //0x5530=int vm_game_update_quest_etf(int _questId, byte[] _etf)
                return GameWorld.instance.vm_game_update_quest_etf(params[0], (byte[])followPointer(params[1]));
            case 0x5531: //0x5531=void vm_game_set_quest_var(int _questId, int _index, int _var)
                GameWorld.instance.vm_game_set_quest_var(params[0], params[1], params[2]);
                break;
            case 0x5532: //0x5532=String vm_game_translate_text(int _questId, String _text)
                return makeTempObject(GameWorld.instance.vm_game_translate_text(params[0], (String)followPointer(params[1])));
            case 0x5533: //0x5533=void vm_game_set_quest_state(int _questId, int _state)
                GameWorld.instance.vm_game_set_quest_state(params[0], params[1]);
                break;
            case 0x5534: //0x5534=int vm_game_get_quest_state(int _questId)
                return GameWorld.instance.vm_game_get_quest_state(params[0]);
            case 0x5535: //0x5535=void vm_game_clear_scene_quests();
                GameWorld.instance.vm_game_clear_scene_quests();
                break;
            case 0x5536: //0x5536=Object vm_game_vm_callback(int msgId, Object msg, String vmids, String funcName)
                return makeTempObject(GameWorld.vm_game_vm_callback(params[0], followPointer(params[1]), (String)followPointer(params[2]), (String)followPointer(params[3])));
            case 0x5537: //0x5537=Object vm_game_vm_callback2(int msgId, Object msg, String[] vmids, String funcName)
                return makeTempObject(GameWorld.vm_game_vm_callback2(params[0], followPointer(params[1]), (String[])followPointer(params[2]), (String)followPointer(params[3])));
            case 0x5538: //0x5538=int[] vm_game_build_random_pos_list(int orgX, int orgY, int minOffset, int maxOffset, int count)
                return makeTempObject(GameWorld.instance.vm_game_build_random_pos_list(params[0], params[1], params[2], params[3], params[4]));
            case 0x5539: //0x5539=void vm_game_set_netplayer_show_max_count(int maxCount)
                GameMain.netplayerShowMaxCount = params[0];
                break;
            case 0x5540: //0x5540=boolean vm_world_in_game_screen()
                return GameWorld.instance.vm_world_in_game_screen()? VM.TRUE: VM.FALSE;
            case 0x5541: //0x5541=void vm_world_show_map_npc_animate(boolean show)
                GameWorld.instance.vm_world_show_map_npc_animate(params[0] == VM.TRUE);
                break;
            case 0x5542: //0x5542=void vm_world_set_3dstring_level(int level)
                GameWorld.instance.vm_world_set_3dstring_level(params[0]);
                break;
            case 0x5543: //0x5543=void vm_game_set_mini_map_alpha(int alpha)
                GameWorld.instance.vm_game_set_mini_map_alpha(params[0]);
                break;
            case 0x5544: //0x5544=void vm_game_send_position()
            	Tool.sendPosition( GameWorld.player.sprite.getDir(),  GameWorld.player.sprite.getX(),  GameWorld.player.sprite.getY(),  GameWorld.player.state);
                break;
            case 0x5545: //0x5545=void vm_game_set_collision_test_para(int maxStep, int stepAdd)
                GameMain.COLLISION_MAX_STEP = params[0];
                GameMain.COLLISION_STEP_ADD = params[1];
                break;
            case 0x5546: //0x5546=void vm_game_release_map_data_buffer()
                if(GameWorld.gameView != null){
                    GameWorld.gameView.releaseMapDataBuffer();
                }
                break;
            case 0x5547: //0x5547=void vm_game_rebuild_map_data_buffer()
                if(GameWorld.gameView != null){
                    GameWorld.gameView.rebuildMapDataBuffer();
                }
                break;
            case 0x5548: //0x5548=void vm_game_set_map_data_buffer_optimize(boolean optimize)
                GameMain.MAP_DATA_BUFFER_OPTIMIZE = (params[0] == VM.TRUE);
                break;
            case 0x5601: //0x5601=void game_panel_set_state(int state)
                GameWorld.panel.game_panel_set_state(params[0]);
                break;
            case 0x5602: //0x5602=int game_panel_reg_image(ImageSet image)
                return GameWorld.panel.game_panel_reg_image((ImageSet)followPointer(params[0]));
            case 0x5603: //0x5603=void game_panel_release_image(int imageIndex)
                GameWorld.panel.game_panel_release_image(params[0]);
                break;
            case 0x5604: //0x5604=int game_panel_add_item_animate_icon(int layer, int iconIndex)
                return GameWorld.panel.game_panel_add_item_animate_icon(params[0], params[1]);
            case 0x5605: //0x5605=int game_panel_add_item_image_icon(int layer, int iconIndex, int x, int y, int w, int h)
                return GameWorld.panel.game_panel_add_item_image_icon(params[0], params[1], params[2], params[3], params[4], params[5]);
            case 0x5606: //0x5606=int game_panel_add_item_image(int layer, int imageIndex, int frameIndex, int x, int y, int trans, int anchor)
                return GameWorld.panel.game_panel_add_item_image(params[0], params[1], params[2], params[3], params[4], params[5], params[6]);
            case 0x5607: //0x5607=void game_panel_change_item_image(int id, int imageIndex, int startIndex, int offsetX, int offsetY)
                GameWorld.panel.game_panel_change_item_image(params[0], params[1], params[2], params[3], params[4]);
                break;
            case 0x5608: //0x5608=int game_panel_add_item_box(int layer, int color, int x, int y, int w, int h)
                return GameWorld.panel.game_panel_add_item_box(params[0], params[1], params[2], params[3], params[4], params[5]);
            case 0x5609: //0x5609=int game_panel_add_item_fill_box(int layer, int color, int x, int y, int w, int h)
                return GameWorld.panel.game_panel_add_item_fill_box(params[0], params[1], params[2], params[3], params[4], params[5]);
            case 0x5610: //0x5610=int game_panel_add_item_line(int layer, int color, int x1, int y1, int x2, int y2)
                return GameWorld.panel.game_panel_add_item_line(params[0], params[1], params[2], params[3], params[4], params[5]);
            case 0x5611: //0x5611=int game_panel_add_item_status_bar(int layer, int color, int x, int y, int w, int h)
                return GameWorld.panel.game_panel_add_item_status_bar(params[0], params[1], params[2], params[3], params[4], params[5]);
            case 0x5612: //0x5612=void game_panel_change_item_status_bar(int id, int curValue, int maxValue)
                GameWorld.panel.game_panel_change_item_status_bar(params[0], params[1], params[2]);
                break;
            case 0x5613: //0x5613=void game_panel_remove_item(int id)
                GameWorld.panel.game_panel_remove_item(params[0]);
                break;
            case 0x5614: //0x5614=void game_panel_clear_item()
                GameWorld.panel.game_panel_clear_item();
                break;
            case 0x5615: //0x5615=int game_panel_add_item_num(int layer, int imageIndex, int startIndex, int x, int y, int space, int anchor, int num)
                return GameWorld.panel.game_panel_add_item_num(params[0], params[1], params[2], params[3], params[4], params[5], params[6], params[7]);
            case 0x5616: //0x5616=void game_panel_change_item_num(int id, int num)
                GameWorld.panel.game_panel_change_item_num(params[0], params[1]);
                break;
            case 0x5617: //0x5617=int game_panel_add_item_mini_animate(int layer, int imageIndex, int startIndex, int x, int y, int trans, int anchor, int moveType, int totalDistance, int totalTicks)
                return GameWorld.panel.game_panel_add_item_mini_animate(params[0], params[1], params[2], params[3], params[4], params[5], params[6], params[7], params[8], params[9]);
            case 0x5618: //0x5618=int game_panel_add_item_animate(int layer, int imageIndex, int x, int y, int trans, int anchor, int ticks, int[] startIndex)
                return GameWorld.panel.game_panel_add_item_animate(params[0], params[1], params[2], params[3], params[4], params[5], params[6], (int[])followPointer(params[7]));
            case 0x5619: //0x5619=int game_panel_add_item_icon(int state, int x, int y, int imageIndex, int frameIndex, int numImageIndex, int numStart, int key)
                return GameWorld.panel.game_panel_add_item_icon(params[0], params[1], params[2], params[3], params[4], params[5], params[6], params[7]);
            case 0x5620: //0x5620=void game_panel_action_state_change(int _state)
                GameWorld.panel.game_panel_action_state_change(params[0]);
                break;
            case 0x5621: //0x5621=int game_panel_add_skill_animate(int layer, int imageIndex, int startIndex, int x, int y, int anchor, int coldGroup,int coldState,int coldHeight, int color)
                return GameWorld.panel.game_panel_add_skill_animate(params[0], params[1], params[2], params[3], params[4], params[5], params[6], params[7], params[8], params[9]);
            case 0x5622: //0x5622=void game_panel_change_skill_cold_group(int coldDownId, int coldStartTime, int duration)
                GameWorld.panel.game_panel_change_skill_cold_group(params[0], params[1], params[2]);
                break;
            case 0x5623: //0x5623=int game_panel_add_item_countdown(int layer, int imageIndex, int frameIndex, int x, int y, int anchor, int countId,int rollId)
                return GameWorld.panel.game_panel_add_item_countdown(params[0], params[1], params[2], params[3], params[4], params[5], params[6], params[7]);
            case 0x5624: //0x5624=void game_panel_add_obtain_item(int _imageIndex, int _frameIndex, int _color, String _name, int _layer)
                GameWorld.panel.game_panel_add_obtain_item(params[0], params[1], params[2], (String)followPointer(params[3]), params[4]);
                break;
            case 0x5625: //0x5625=void game_panel_add_obtain_value(int imageIndex, int frameIndex, int numImageIndex, int numIndex, int value, int layer)
                GameWorld.panel.game_panel_add_obtain_value(params[0], params[1], params[2], params[3], params[4], params[5]);
                break;
            case 0x5626: //0x5626=int game_panel_add_item_hmessage_bar(int x, int y, int w, int h, int layer, int edge, int alpha)
                return GameWorld.panel.game_panel_add_item_hmessage_bar(params[0], params[1], params[2], params[3], params[4], params[5], params[6]);
            case 0x5628: //0x5628=int game_panel_add_item_vmessage_bar(int x, int y, int w, int h, int layer, int edge, int alpha)
                return GameWorld.panel.game_panel_add_item_vmessage_bar(params[0], params[1], params[2], params[3], params[4], params[5], params[6]);
            case 0x5630: //0x5630=void game_panel_post_hmessage(int id, String msg, int color, int maxRecord)
                GameWorld.panel.game_panel_post_hmessage(params[0], (String)followPointer(params[1]), params[2], params[3]);
                break;
            case 0x5631: //0x5631=void game_panel_post_vmessage(int id, String msg, int color, int maxRecord)
                GameWorld.panel.game_panel_post_vmessage(params[0], (String)followPointer(params[1]), params[2], params[3]);
                break;
            case 0x5632: //0x5632=void game_panel_set_landmark_config(int _x, int _y, int _color, int _bgColor, int _archor, int _layer)
                GameWorld.panel.game_panel_set_landmark_config(params[0], params[1], params[2], params[3], params[4], params[5]);
                break;
            case 0x5633: //0x5633=int game_panel_add_string(String str,int x,int y,int offset,int anchor,int is3d,int isTopOfMsg,int forColor,int bgColor,int layer)
                return GameWorld.panel.game_panel_add_string((String)followPointer(params[0]), params[1], params[2], params[3], params[4], params[5], params[6], params[7], params[8], params[9]);
            case 0x5634: //0x5634=void game_panel_remove_skill_cold_group(int coldDownId)
                GameWorld.panel.game_panel_remove_skill_cold_group(params[0]);
                break;
            case 0x5635: //0x5635=int game_panel_add_item_alpha_fill_box(int layer, int color, int x, int y, int w, int h)
                return GameWorld.panel.game_panel_add_item_alpha_fill_box(params[0], params[1], params[2], params[3], params[4], params[5]);
            case 0x5636: //0x5636=int game_panel_create_template()
                return GameWorld.panel.createTemplate();
            case 0x5637: //0x5637=void game_panel_add_template_item(int tid, byte type, int layer, byte trans, byte anchor, short x, short y, short w, short h, int intData1, int intData2, Object objData, Object objData2, int frame, int tick)
                GameWorld.panel.addTemplateItem(params[0], (byte)params[1], params[2], (byte)params[3], (byte)params[4], (short)params[5], 
                        (short)params[6], (short)params[7], (short)params[8], params[9], params[10], followPointer(params[11]),
                        followPointer(params[12]), params[13], params[14]);
                break;
            case 0x5638: //0x5638=int game_panel_create_with_template(int tid, short x, short y, int[] iparam, Object[] oparam)
                return GameWorld.panel.createWithTemplate(params[0], (short)params[1], (short)params[2], (int[])followPointer(params[3]), 
                        (Object[])followPointer(params[4]));
            case 0x5639: //0x5639=void game_panel_reconfig_template_items(int tid, int firstID, short x, short y, int[] iparam, Object[] oparam)
                GameWorld.panel.reconfigTemplateItems(params[0], params[1], (short)params[2], (short)params[3], (int[])followPointer(params[4]),
                        (Object[])followPointer(params[5]));
                break;
            case 0x563A: //0x563A=void game_panel_remove_template_items(int tid, int firstID)
                GameWorld.panel.removeTemplateItems(params[0], params[1]);
                break;
            case 0x563B: //0x563B=void game_panel_change_item_pos(int id, short x, short y)
                GameWorld.panel.game_panel_change_item_pos(params[0], (short)params[1], (short)params[2]);
                break;
            case 0x563C: //void game_panel_clear_array(int[] arr, int padding)
                GameWorld.panel.game_panel_clear_array((int[])followPointer(params[0]), params[1]);
                break;
            case 0x563D: //0x563D void setToTop(String vmId)
            	VMGame.setToTop((String)followPointer(params[0]));
            	break;
            case 0x563E: //0x563E=void game_panel_change_hmessage_alpha(int alpha)
                GameWorld.panel.game_panel_change_hmessage_alpha(params[0]);
                break;
            case 0x563F: //0x563F=void game_panel_change_vmessage_alpha(int alpha)
                GameWorld.panel.game_panel_change_vmessage_alpha(params[0]);
                break;
            case 0x5640: //0x5640=void GSetSingleDirScroll(GLabel gLabel, boolean b)	
            	((GLabel)getGW(params[0])).setSingleDirScroll(params[1]==1);
            	break;
            case 0x5641: //0x5641=int game_panel_add_item_opposite_image(int layer, int imageIndex, int frameIndex, int x, int y, int trans, int anchor)
                return GameWorld.panel.game_panel_add_item_opposite_image(params[0], params[1], params[2], params[3], params[4], params[5], params[6]);
            case 0x5642: //0x5642=int[] game_panel_get_opposite_image_box(int itemId)
                return makeTempObject(GameWorld.panel.game_panel_get_opposite_image_box(params[0]));
            case 0x5643: //0x5643=boolean game_panel_point_in_box(int pointX, int pointY, int[] box)
                return GameWorld.panel.game_panel_point_in_box(params[0], params[1], (int[])followPointer(params[2]))? VM.TRUE: VM.FALSE;
            case 0x5644: //0x5644=void game_panel_reg_point_item(int notifyId, int x, int y, int w, int h, boolean opposite, int notifyData, int anchor)
            	if(GameWorld.panel!=null){
            		GameWorld.panel.game_panel_reg_point_item(params[0], params[1], params[2], params[3], params[4], params[5] == VM.TRUE, params[6], params[7]);
            	}
                break;
            case 0x5645: //0x5645=void game_panel_remove_point_item(int notifyId)
            	if(GameWorld.panel!=null){
            		GameWorld.panel.game_panel_remove_point_item(params[0]);
            	}
                break;
            case 0x5646: //0x5646=void game_panel_set_point_item_effect(int notifyId, boolean effect)
                GameWorld.panel.game_panel_set_point_item_effect(params[0], params[1] == VM.TRUE);
                break;
            case 0x5647: //0x5647=void game_panel_clear_point_item()
                GameWorld.panel.game_panel_clear_point_item();
                break;
            case 0x5648: //0x5648=void game_panel_get_view_x()
                return GameWorld.viewX;
            case 0x5649: //0x5649=void game_panel_get_view_y()
                return GameWorld.viewY;
            case 0x5650: //0x5650=void game_panel_get_map_width()
                if(GameWorld.gameView != null){
                    return GameWorld.gameView.map.width;
                }else{
                    return -1;
                }
            case 0x5651: //0x5651=void game_panel_get_map_height()
                if(GameWorld.gameView != null){
                    return GameWorld.gameView.map.height;
                }else{
                    return -1;
                }
            case 0x5700: // 0x5700=String mergeString2(Vector v)合并字符串,末尾追加"\n"(最后一个不加),忽略null和空串
            	return makeTempObject(Tool.mergeString2((Vector)followPointer(params[0])));
            //#if ScreenCanReset == true
            //# case 0x5701: // 0x5701=Hashtable game_panel_get_vmessage()
            	//# return makeTempObject(GameWorld.panel.game_panel_get_vmessage());
            //# case 0x5702: // 0x5702=Hashtable game_panel_get_hmessage()
            	//# return makeTempObject(GameWorld.panel.game_panel_get_hmessage());
            //# case 0x5703: // 0x5703=Long IntToLong(int lInt, int hInt)
            	//# return makeTempObject(new Long(params[0] | params[1] << 32));
            //# case 0x5704: // 0x5704=int[] LongToInt(Long long)
            	//# long longs = ((Long)followPointer(params[0])).longValue();            	
            	//# return makeTempObject(new int[]{(int)(longs & 0xFFFFFFFF), (int)(longs >> 32)});
            //# case 0x5705: // 0x5705=void SetUIMaxWidth(int uiMaxWidth)
            	//# GWindow.uiMaxWidth = params[0];
            	//# break;
            //# case 0x5706: // 0x5706=void SetUIMaxHeight(int uiMaxHeight)
            	//# GWindow.uiMaxHeight = params[0];
            	//# break;
            //# case 0x5707: // 0x5707=int GetUIMaxWidth()
            	//# return GWindow.uiMaxWidth;
            //# case 0x5708: // 0x5708=int GetUIMaxHeight()
            	//# return GWindow.uiMaxHeight;
            //# case 0x5709: // 0x5709=void SetUILeft(int uiMaxWidth)
            	//# GWindow.uiLeft = params[0];
            	//# break;
            //# case 0x570A: // 0x570A=void SetUITop(int uiMaxHeight)
            	//# GWindow.uiTop = params[0];
            	//# break;
            //# case 0x570B: // 0x570B=int GetUILeft()
            	//# return GWindow.uiLeft;
            //# case 0x570C: // 0x570C=int GetUITop()
            	//# return GWindow.uiTop;
            //# case 0x570D: //0x570D=void SetForcePaintWorld(boolean forcePaintWorld)
            	//# GWindow.forcePaintWorld = (params[0] == TRUE);
            	//# break;
            //# case 0x570E: //0x570E=Object GetTopUIVM()
            	//# return makeTempObject(VMGame.getTopUIVM());
            //# case 0x570F: //0x570F=GWidget GetGWidgetVMObj(Object javaGWidget)
            	//# return makeTempObject(((GWidget)this.followPointer(params[0])).vmData);    
            //# case 0x5710: //0x5710=void ResetScrollBar(GWindow gWindow)
            	//# ((GWindow)this.followPointer(params[0])).resetScrollBar();
            	//# break;
            //# case 0x5711: //0x5711=void SetFullScreen(GWindow gWindow, boolean isFullScreen)
            	//# ((GWindow)getGW(params[0])).fullScreen = (params[1] == TRUE);
            	//# break;
            //# case 0x5712: //0x5712=boolean IsFullScreen(GWindow gWindow)
            	//# return ((GWindow)getGW(params[0])).fullScreen ? TRUE : FALSE;
            //# case 0x5713: //0x5713=void GLabelSetForceSroll(GLabel gLabel, boolean forceScroll)
            	//# ((GLabel)this.getGW(params[0])).setForceScroll(params[1] == TRUE);
            	//# break;
            //# case 0x5714: //0x5714=void GLabelSetLineHeight(GLabel gLabel, int lineHeight)
            	//# ((GLabel)this.getGW(params[0])).lineHeight = params[1];
            	//# break;
            //# case 0x5715: //0x5715=void GTextAreaLineHeight(GTextArea gTextArea, int lineHeight)
            	//# ((GTextArea)this.getGW(params[0])).lineHeight = params[1];
            	//# break;
            //# case 0x5716: //0x5716=void game_panel_remove_template_items2(int tid, int firstID)
                //# GameWorld.panel.removeTemplateItems2(params[0], params[1]);
                //# break;
            //#endif
            //api版本1.0 begin
            case 0x5717: //0x5717=Object GGetScrollBar(GContainer gContainer)
            	GContainer con = (GContainer)this.getGW(params[0]);
            	if(con != null){
            		GScrollBar sb = con.getScrollBar();
                	if(sb != null) {
                		return this.makeTempObject(sb.vmData);
                	} else {
                		return 0;
                	}
            		
            	} else {
            		return 0;
            	}
            case 0x5718: //0x5718=boolean GIsContainer(GWidget gWidget)
            {
            	GWidget gWidget = this.getGW(params[0]);
            	if(gWidget != null && gWidget instanceof GContainer) {
            		return VM.TRUE;
            	} else {
            		return VM.FALSE;
            	}
            }
            case 0x5719: //0x5719=boolean GIsWindow(GWidget gWidget)
            {
            	GWidget gWidget = this.getGW(params[0]);
            	if(gWidget != null && gWidget instanceof GWindow) {
            		return VM.TRUE;
            	} else {
            		return VM.FALSE;
            	}
            }
            case 0x571A: //0x571A=int[] GGetIntersect(GWidget gWidget)
            {
            	GWidget gWidget = this.getGW(params[0]);
            	if(gWidget != null) {
            	    gWidget.getIntersect(gWidget.rect);
            		return this.makeTempObject(gWidget.rect);
            	} else {
            		return 0;
            	}
            }
            case 0x571B: //0x571B=int getSqrt(int value)
            {
            	return (int)Tool.sqrt((int)params[0]);
            }
            case 0x571C: //0x571C=int MathAbs(int value)
            {
            	return Math.abs(params[0]);
            }
            case 0x571D: //0x571D=void FillTriangle(Object g, int x1, int y1, int x2, int y2, int x3, int y3)
            {
            	((Graphics)followPointer(params[0])).fillTriangle(params[1], params[2], params[3], params[4], params[5], params[6]);
            }
            case 0x571E: //0x571E=int[] GetClip(Object g)
            {
            	Graphics g = (Graphics)this.followPointer(params[0]);
            	if(g != null) {
            		int[] clipRect = new int[4];
            		clipRect[0] = g.getClipX();
            		clipRect[1] = g.getClipY();
            		clipRect[2] = g.getClipWidth();
            		clipRect[3] = g.getClipHeight();
            		return this.makeTempObject(clipRect);
            	}
            	return 0;
            }
            case 0x571F: //0x571F=void GSetNeedScrollBar(GContainer gContainer, boolean needSb)
            {
            	((GContainer)getGW(params[0])).needScrollBar = (params[1] == TRUE);
            }
            break;
            //api版本1.0 end
            case 0x7000: //0x7000=int GetApiVersion()
            {
            	return getApiVersion();
            }
          //api版本2.0 begin
            case 0x5720:// 0x5720=void DrawFullWorldMap(ImageSet img,Vector frame,Vector transit,int mapTileWidth,int mapTileHeight,Graphics g)绘制世界地图
            {
            	ImageSet _img = (ImageSet)followPointer(params[0]);
            	Vector _frame = (Vector)followPointer(params[1]);
            	Vector _transit = (Vector)followPointer(params[2]);
            	Tool.drawWorldMap(_img, _frame, _transit, params[3], params[4], (Graphics)followPointer(params[5]));
            }
            	break;
            case 0x5721:// 0x5721=void DrawScreenWorldMap(ImageSet img,Vector frame,Vector transit,int x,int y,int screenWidth,int screenHeight,int mapTileWidth,int mapTileHeight,Graphics g)绘制单屏世界地图
            {
            	ImageSet _img = (ImageSet)followPointer(params[0]);
            	Vector _frame = (Vector)followPointer(params[1]);
            	Vector _transit = (Vector)followPointer(params[2]);
            	Tool.drawWorldMap(_img, _frame, _transit, params[3], params[4], params[5], params[6], params[7], params[8], (Graphics)followPointer(params[9]));
            }
            	break;
            case 0x5723: //0x5723=byte[] FindResource(String name)
            {
            	return this.makeTempObject(GameMain.resourceManager.findResource((String)followPointer(params[0])));
            }
            //#if SupportSound == true
            //# case 0x5724: //0x5724=void SetSoundSwitch(int type, boolean on)
            //# {
            	//# switch(params[0]) {
            	//# case 0:
            		//# GameMain.backBoundSwitch = (params[1] == VM.TRUE);
            		//# break;
            	//# case 1:
            		//# GameMain.actionBoundSwitch = (params[1] == VM.TRUE);
            		//# break;
            	//# }
//#             	
            //# }
            	//# break;
            //# case 0x5725: //0x5725=boolean GetSoundSwitch(int type)
            //# {
            	//# switch(params[0]) {
            	//# case 0:
            		//# return GameMain.backBoundSwitch ? VM.TRUE : VM.FALSE;
            	//# default:
            		//# return GameMain.actionBoundSwitch ? VM.TRUE : VM.FALSE;
            	//# }
//#             	
            //# }
            //# case 0x5726: // 0x5726=boolean GetPhoneCalling()
    		//# {
    			//# return SoundPlayer.phoneCalling ? VM.TRUE : VM.FALSE;
    		//# }
    		//# case 0x5727: // 0x5727=boolean GetScreenChange()
    		//# {
    			//# return SoundPlayer.screenChangeOff ? VM.TRUE : VM.FALSE;
    		//# }
            //#endif
            case 0x5750: // 0x5750=int GetFontHeightEx(Font font) 字体高度
                return ((Font)followPointer(params[0])).getHeight();
            case 0x5751: // 0x5751=int StringWidthEx(String s, Font font) 求得字符串在屏幕上的宽度
                return ((Font)followPointer(params[1])).stringWidth((String)followPointer(params[0]));
            case 0x5752://0x5752=void drawWidthStringEx(Graphic g,String str,int x,int y,int width, Font font)
            {
                strSplit = Tool.splitString((String)followPointer(params[1]), params[3], (Font)followPointer(params[5]));
                for(int i = 0; i < strSplit.length; i++)
                    ((Graphics)followPointer(params[0])).drawString(strSplit[i], params[2], params[3] + ((Font)followPointer(params[5])).getHeight() * i, Graphics.TOP | Graphics.LEFT);
            }
                break;
            case 0x5753: // 0x5753=String[] SplitStringEx(String msg, int width, Font font)
                return makeTempObject(Tool.formatText((String)followPointer(params[0]), params[1], (Font)followPointer(params[2])));
            case 0x5754://0x5754=void SetFont(Graphic g, Font font)
            	((Graphics)followPointer(params[0])).setFont((Font)followPointer(params[1]));
                break;
            case 0x5755: // 0x5755=Object GetFont(int face, int style, int size)
            	//#if ModelID == Nokia5800New
            	//# return makeTempObject(com.nokia.mid.ui.DirectUtils.getFont(params[0], params[1], params[2]));
            	//#else
//#if NewUI2
//适配非960x640机型
            	//# if(GameMain.realScreenWidth != 960 && GameMain.realScreenHeight != 640){
            	//# 	if(GameMain.realScreenWidth == 480 && GameMain.realScreenHeight == 320){
        		//# 		return makeTempObject(Font.getFont(params[0], params[1], params[2] * 16 / 10));
    			//# 	} else {
    			//# 		return makeTempObject(Font.getFont(params[0], params[1], params[2] * 7 / 5));
    			//# 	}
            	//# } else {
            	//# 	return makeTempObject(Font.getFont(params[0], params[1], params[2]));
            	//# }
//#else
            	return makeTempObject(Font.getFont(params[0], params[1], params[2]));
//#endif
            	//#endif
            case 0x5756://0x5756=void ReSetFont(Graphic g)
            	((Graphics)followPointer(params[0])).setFont(Utilities.font);
                break;
            case 0x5757://0x5757=void GSetFont(GWidget gWidget, Font font)
            	((GWidget)getGW(params[0])).setFont((Font)followPointer(params[1]));
                break;
            case 0x5758: // 0x5758=int GGetFont(GWidget gWidget)
                return this.makeTempObject(((GWidget)getGW(params[0])).getFont());
            case 0x5759: // 0x5759=Font GetCurrentFont(Graphics g)
                return this.makeTempObject(((Graphics)followPointer(params[1])).getFont());
            case 0x575A: //0x575A=int game_panel_add_stringEx(String str,int x,int y,int offset,int anchor,int is3d,int isTopOfMsg,int forColor,int bgColor,int layer, Font font)
                return GameWorld.panel.game_panel_add_stringEx((String)followPointer(params[0]), params[1], params[2], params[3], params[4], params[5], params[6], params[7], params[8], params[9], (Font)followPointer(params[1]));
            //#if ModelID == Lenovo || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad || ModelID == Android || ModelID == AndroidSmall || ModelID == AndroidAuto
              //# case 0x575B://0x575B=void SetGamePanelHMsgFont(Font font)
              	//# GameWorld.panel.hMsgFont = (Font)followPointer(params[0]);
                //# break;
              //# case 0x575C://0x575C=void SetGamePanelVMsgFont(Font font)
              	//# GameWorld.panel.vMsgFont = (Font)followPointer(params[0]);
                 //# break;
              //# case 0x575D://0x575D=void SetLandMarkFont(Font font)
              	//# GameWorld.panel.landMarkFont = (Font)followPointer(params[0]);
                  //# break;
            //#endif
              //#if ModelID == Lenovo || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad || ModelID == Android || ModelID == Nokia5800 || ModelID == AndroidAuto
            //# case 0x575E://0x575E=void SetMiniMapMaxWidth(int miniMapMaxWidth)
            //# {
            	//# if(!GameMain.getUIModel().equals(GameMain.ANDROID_SMALL))
    			//# {
            	//# if(GameWorld.gameView != null){
            		//# GameWorld.gameView.miniMapMaxWidth = params[0];
            	//# }
            	//# }
            //# }
                //# break;
            //# case 0x575F://0x575F=void SetMiniMapMaxHeight(int miniMapMaxHeight)
            	//# if(GameWorld.gameView != null){
            		//# GameWorld.gameView.miniMapMaxHeight = params[0];
            	//# }
                //# break;
            //#endif
            //api版本2.0 end
                
            //api版本3.0 begin added by zlguo
            case 0x5764:{//0x5764=void vm_sprite_set_mini_map_image(int npcId,String resName,int frame)
            	GameSprite sprite = GameWorld.findNpcById(Tool.SPRITE_TYPE_NPC, params[0]);
            	if(sprite != null){
            		sprite.vm_sprite_set_mini_map_image((String)followPointer(params[1]),params[2]);
            	}
            }
                break;  
            //api版本3.0 end 
            //台湾繁体版购买Q币API
                //#if Revision == TAIWAN
            //#if ModelID != Android && ModelID != AndroidSmall && ModelID != AndroidLarge && ModelID != AndroidAuto
			//# case 0x5765:{//0x5765=Object Handler_Create()
				//# System.out.println("enter BillingEventHandler:");
				//#if ModelID == Android || ModelID == AndroidLarge || ModelID == AndroidSmall || ModelID == AndroidAuto
				//# if (SanguoMIDlet.igbKernel == null) {
				//# 	System.out.println("enter BillingEventHandler igbKernel:");
				//#  	SanguoMIDlet.igbKernel = new billing.IGBKernel(PipActivity.DEFAULT_ACTIVITY);
				//# 	IGBKernel.setKernel_trace( false );
				//# }
				//# System.out.println("enter BillingEventHandler1:");
				//# //System.out.println("enter Handler_Create:");
				//# //billing.BillingEventHandler ret = new billing.BillingEventHandler(SanguoMIDlet.DEFAULT_ACTIVITY);
				//# billing.BillingEventHandler ret = new billing.BillingEventHandler(PipActivity.DEFAULT_ACTIVITY);
				//# System.out.println("enter BillingEventHandler:" +  ret);
				//#else
			    //# if (SanguoMIDlet.igbKernel == null) {
					 //# SanguoMIDlet.igbKernel = new billing.IGBKernel(SanguoMIDlet.instance);
					 //# IGBKernel.setKernel_trace( false );
				 //# }
				 //# billing.BillingEventHandler ret = new billing.BillingEventHandler();
				 //# if (GameMain.debugMode) {
					 //# ret.setTrace(true);
				 //# }
				 //# System.out.println("enter BillingEventHandler:" +  ret);
				//#endif
				//# //ret.setTestMode(2);
				//# //ret.setTestMode_sendPayChoose(1);
				//# return makeTempObject(ret);
				//# //System.out.println("leave Handler_Create:");
			//# }
			//# case 0x5766:{//0x5766=Vector Handler_GetStoreType(Object handler,String tsi)
				//# //System.out.println("enter Handler_GetStoreType:");
				//#if ModelID == Android || ModelID == AndroidLarge || ModelID == AndroidSmall || ModelID == AndroidAuto
				//# String temp = (String)followPointer(params[1]);
				//# billing.BillingEventHandler ret = (billing.BillingEventHandler)followPointer(params[0]);
				//# //return makeTempObject(ret.getStoreType(temp));
				//# return makeTempObject(ret.getStoreType(PipActivity.DEFAULT_ACTIVITY.syncData[2]));
				//#else
				//# //System.out.println("leave Handler_GetStoreType:");
				//# return makeTempObject(((billing.BillingEventHandler)followPointer(params[0])).getStoreType((String)followPointer(params[1])));
				//#endif
			//# }
			//# case 0x5767:{//0x5767=String Handler_SendPayChoose(Object handler,String tsi,int choose)
				//# return makeTempObject(((billing.BillingEventHandler)followPointer(params[0])).sendPayChoose((String)followPointer(params[1]),params[2]));
			//# }
			//#endif
			//# case 0x5768:{//0x5769=String IGBKernel_KernelSyncTsi(String user, String pass, String mode, String atsi)
				//# if (SanguoMIDlet.igbKernel == null) {
					//#if ModelID == Android || ModelID == AndroidLarge || ModelID == AndroidSmall || ModelID == AndroidAuto
					//# // SanguoMIDlet.igbKernel = new billing.IGBKernel(PipActivity.DEFAULT_ACTIVITY);
					//#else
					//# SanguoMIDlet.igbKernel = new billing.IGBKernel(SanguoMIDlet.instance);
					//#endif
//# 					
					//# IGBKernel.setKernel_trace( false );
				//# }
				//#if ModelID == Android || ModelID == AndroidLarge || ModelID == AndroidSmall || ModelID == AndroidAuto
				//# return makeTempObject(SanguoMIDlet.igbKernel.getOpidUid(PipActivity.DEFAULT_ACTIVITY,
				//#		(String)followPointer(params[0]), (String)followPointer(params[1]), 
				//#		(String)followPointer(params[2]), (String)followPointer(params[3])));
				//#else
				//# return makeTempObject(SanguoMIDlet.igbKernel.kernelSyncTsi((String)followPointer(params[0]), 
						//# (String)followPointer(params[1]), (String)followPointer(params[2]), 
						//# (String)followPointer(params[3])));
				//#endif
			//# }
			//# case 0x5769:{//0x576A=String IGBKernel_GetAuthorizeResult(String uid, String opid)
				//#if ModelID == Android || ModelID == AndroidLarge || ModelID == AndroidSmall || ModelID == AndroidAuto
				//# return makeTempObject(IGBKernel.getAuthorizeResult((String)followPointer(params[0]), 
				//#		(String)followPointer(params[1]), IGBKernel.getLocalSid(), null));
				//#else
				//# return makeTempObject(IGBKernel.getAuthorizeResult((String)followPointer(params[0]), 
						//# (String)followPointer(params[1]), IGBKernel.getUc_Local_Data_Sid(), null));
				//#endif
			//# }
			//#if ModelID != Android && ModelID != AndroidSmall && ModelID != AndroidLarge && ModelID != AndroidAuto
			//# case 0x5780:{ //0x5780=Handler_setTestMode(Object handler,int mode, String loginname, String loginpw)  axq add
				//#if ModelID == Android || ModelID == AndroidLarge || ModelID == AndroidSmall || ModelID == AndroidAuto
				//# // billing.BillingEventHandler  bill = (billing.BillingEventHandler)(followPointer(params[0]));
				//# //Log.i("BillingEventHandler", bill);	
				//# //System.out.println("BillingEventHandler:" +  bill);
				//# //System.out.println("BillingEventHandlermode:" +  params[1]);
				//# // String LoginName = (String)followPointer(params[2]);
				//# // String LoginPw   = (String)followPointer(params[3]);
			//# //	bill.setTestMode(2);//設定取得儲值時候的測試模式	
				//# // bill.setTestMode_sendPayChoose(0);//設定傳送付費項目時候的測試模式
	        	//# // PipActivity.DEFAULT_ACTIVITY.synctsi(LoginName, LoginPw);
				//# //System.out.println("leave BillingEventHandler");
				//#endif
				//# break;
			//# }
			//#endif
			//#if ModelID == Android || ModelID == AndroidLarge || ModelID == AndroidSmall || ModelID == AndroidAuto
			  //# case 0x6061:{ //0x6061=int  ShowIGBKernel(int chooseIndex, String username, String password, String MyAPPID)
	          //# 	int chooseIndex = params[0];
	          //# 		String username = (String)followPointer(params[1]);
	          //# 		String password = (String)followPointer(params[2]);
			  //#       String myAppid  =  (String)followPointer(params[3]);
			  //# 	return PipActivity.DEFAULT_ACTIVITY.showIGBKernel(this,chooseIndex, username, password, myAppid);	        
			  //# }
//# 	            
				//#  case 0x6062:{ //0x6062=int GetIGBResultCode()
				//#  	return PipActivity.IGBResultCode;
				//#  }
//# 	            
				//# case 0x6063:{ //0x6063=int GetIGBInitResult()
				//#  	return PipActivity.IGBInitResult;
				//# }
//# 	            
				//# case 0x6064:{ //0x6064=void RunIGBKernel()
				//# 	PipActivity.DEFAULT_ACTIVITY.runIGBKernel();
				//# 	break;
				//# }
			//#endif
        //#endif
			//API VERSION 7
			case 0x5784:{//0x5784=void Form_AppendString(Object form, String str)
				Form f = (Form)followPointer(params[0]);
				//#if ModelID == Android || ModelID == AndroidLarge || ModelID == Lenovo || ModelID == IPad || ModelID == IPhone4 || ModelID == LenovoU1 || ModelID == AndroidSmall || ModelID == AndroidAuto
				//# StringItem strit = new StringItem(null,(String)followPointer(params[1]));
				//# f.append(strit);
				//#else
				f.append((String)followPointer(params[1]));
				//#endif
			}
				break;
			case 0x5785:{//0x5786=void Form_SetItemText(Object form, int index,String str)
				Form f = (Form)followPointer(params[0]);
				Item it = f.get(params[1]);
				if(it instanceof StringItem){
					StringItem si = (StringItem)it;
					si.setText((String)followPointer(params[2]));
				} else if(it instanceof TextField){
					TextField tf = (TextField)it;
					tf.setString((String)followPointer(params[2]));
				} else {
					it.setLabel((String)followPointer(params[2]));
				}
			}
				break;	
			case 0x5786:{//0x5787=void Form_RemoveItem(Object form, int index)
				//#if ModelID == Android || ModelID == AndroidLarge || ModelID == Lenovo || ModelID == IPad || ModelID == IPhone4 || ModelID == LenovoU1 || ModelID == AndroidSmall || ModelID == AndroidAuto
				//#else
				Form f = (Form)followPointer(params[0]);
				f.delete(params[1]);
				//#endif
			}
				break;
			case 0x5787:{//0x5788=void Form_ChangeItemIndex(Object form ,int oldIndex,int newIndex)
				//#if ModelID == Android || ModelID == AndroidLarge || ModelID == Lenovo || ModelID == IPad || ModelID == IPhone4 || ModelID == LenovoU1 || ModelID == AndroidSmall || ModelID == AndroidAuto
				//#else
				Form form = (Form)followPointer(params[0]);
				Item it = form.get(params[1]);
				form.delete(params[1]);
				form.insert(params[2],it);
				//#endif
			}
				break;
			//API VERSION 7
			//api版本4.0 
            case 0x5772: //0x5772=void game_panel_add_obtain_item2(int _imageIndex, int _frameIndex, int _color, String _name, int _layer,int numberImageIndex,int numberFrameIndex,int count)
                GameWorld.panel.game_panel_add_obtain_item2(params[0], params[1], params[2], (String)followPointer(params[3]), params[4],params[5],params[6],params[7]);
                break;
			//api版本4.0 end
            case 0x5774://0x651E=String GetIMEI();
            	return makeTempObject(Tool.getIMEI());
            	//#if ModelID == Lenovo || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad || ModelID == Android || ModelID == AndroidSmall || ModelID == AndroidAuto
            //# case 0x5760:////0x5760=Object CreateGAet(Object vmObj, String name)
              	//# return new GAndroidEditText((VMGame)owner, params[0], (int[])followPointer(params[0]), (String)followPointer(params[1])).vmData[GWidget.GW_VM_SELF];
               //# case 0x5761://0x5761=void GAetSetText(GAndroidEditText obj, String text)
              	//# ((GAndroidEditText)getGW(params[0])).setText((String)this.followPointer(params[1]));
                  //# break;
              //# case 0x5762://0x5762=void GAetGetText(GAndroidEditText obj)
              	//# return this.makeTempObject(((GAndroidEditText)getGW(params[0])).getText());
              //# case 0x5763://0x5763=void GAetGetCaretPosition(GAndroidEditText obj)
              	//# return ((GAndroidEditText)getGW(params[0])).getCaretPosition();
              //#endif
                
            //#if ModelID == Lenovo || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad || ModelID == Android || ModelID == AndroidSmall || ModelID == AndroidAuto 
            //# case 0x6500: //0x6500=void ConfirmExit()
            //# {
            	//# if(!(GameMain.display.getCurrent() instanceof Alert)){
            		//# synchronized(this) {            			
    					//# isExit = true;
 //#if Revision == JP
    					//#	new AlertDialog.Builder(PipActivity.DEFAULT_ACTIVITY).setIcon(
    					//#  android.R.drawable.btn_star).setTitle(PipActivity.DEFAULT_ACTIVITY.getString(com.pip.android.R.string.str_end)).setMessage(
    					//#  PipActivity.DEFAULT_ACTIVITY.getString(com.pip.android.R.string.str_tip)).setPositiveButton(PipActivity.DEFAULT_ACTIVITY.getString(com.pip.android.R.string.str_launcher_start),
    					//# 		 new DialogInterface.OnClickListener() {
    					//#             @Override
    					//#             public void onClick(DialogInterface dialog, int which) {
                        //#                PipActivity.DEFAULT_ACTIVITY.hangame.callLuncher();
    					//#             }
    					//#      }).setNegativeButton(PipActivity.DEFAULT_ACTIVITY.getString(com.pip.android.R.string.str_ok), new DialogInterface.OnClickListener() {
    					//#             @Override
    					//#             public void onClick(DialogInterface dialog, int which) {
    					//#					VMGame mainmenu;
    					//#					String strcmd = "确认";
    					//#					mainmenu = VMGame.getVMGame("ui_mainmenu");
    					//#					String topVMId = VMGame.getTopUIVMId();
    					//#					if (mainmenu != null && topVMId.equals("ui_update") == false) {
    					//#						// synchronized(mainmenu.getVM()){
    					//#						mainmenu.getVM().callback("NotifyExit", new int[] {});
    					//#						// }
    					//#					} else {
    					//#						SanguoMIDlet.exit();
    					//#					}
    					//#             }
    					//#     }).setNeutralButton(PipActivity.DEFAULT_ACTIVITY.getString(com.pip.android.R.string.str_cancel), null).create().show();
//#else
    					//# Alert alert = new Alert(PipActivity.DEFAULT_ACTIVITY.getString(com.pip.android.R.string.str_end), PipActivity.DEFAULT_ACTIVITY.getString(com.pip.android.R.string.str_tip), null, AlertType.INFO);
    					//# alert.setCommandListener(VM.this);
    					//# alert.addCommand(new Command(PipActivity.DEFAULT_ACTIVITY.getString(com.pip.android.R.string.str_ok), Command.OK, 0));
    					//# alert.addCommand(new Command(PipActivity.DEFAULT_ACTIVITY.getString(com.pip.android.R.string.str_cancel), Command.CANCEL, 0));
    					//# GameMain.display.setCurrent(alert);
 //#endif     					
    				//# }
            	//# }
             	//# break;
            //# }
            //# case 0x6010: //0x6010=Object Sound_Init(String fileName);
            //# {            	
            	//# return this.makeTempObject(new SoundPlayer((String)followPointer(params[0])));
            //# }
            //# case 0x6011: //0x6011=boolean Sound_Play(Object player, String fileName, int volume, boolean loop);
            //# {
            	//# try {
            		//# SoundPlayer sp = (SoundPlayer)followPointer(params[0]);
            		//# sp.setVolume(params[2]);
            		//# sp.play(params[3] == VM.TRUE);
            	//# }catch (Exception e) {
            		//# System.out.println(e);
            		//# return VM.FALSE;
            	//# }
            	//# 
            	//# return VM.TRUE;
//#             	
            //# }
            //# case 0x6012: //0x6012=void Sound_SetVolume(Object player, int volume, int timeout); 
            //# {            	
            	//# ((SoundPlayer)followPointer(params[0])).setVolume(params[1]);
            //# }
            	//# break;
            //# case 0x6013: //0x6013=boolean Sound_Pause(Object player); 
            //# {
            	//# try {
            		//# ((SoundPlayer)followPointer(params[0])).pause();
            	//# }catch (Exception e) {
            		//# System.out.println(e);
            		//# return VM.FALSE;
            	//# }
            	//# 
            	//# return VM.TRUE;
            //# }
            //# case 0x6014: //0x6014=boolean Sound_Resume(Object player);
            //# {
            	//# try {
            		//# ((SoundPlayer)followPointer(params[0])).resume();
            	//# }catch (Exception e) {
            		//# System.out.println(e);
            		//# return VM.FALSE;
            	//# }
            	//# 
            	//# return VM.TRUE;
            //# }
            //# case 0x6015: //0x6015=boolean Sound_Stop(Object player);
            //# {
            	//# try {
            	//# ((SoundPlayer)followPointer(params[0])).stop();
            	//# }catch (Exception e) {
            		//# System.out.println(e);
            		//# return VM.FALSE;
            	//# }
            	//# 
            	//# return VM.TRUE;
            //# }
            //# case 0x6016: //0x6016=void Sound_Close(Object player);
            //# {
            	//# ((SoundPlayer)followPointer(params[0])).close();
            //# }
            	//# break;
            //# case 0x6017: //0x6017=boolean Sound_IsLooping(Object player);
            //# {
            	//# return ((SoundPlayer)followPointer(params[0])).isLooping() ? VM.TRUE : VM.FALSE;
            //# }
            //# case 0x6018: //0x6018=boolean Sound_IsPlaying(Object player);
            //# {
            	//# return ((SoundPlayer)followPointer(params[0])).isPlaying() ? VM.TRUE : VM.FALSE;
            //# }
            //# case 0x6019: //0x6019=void Sound_Reset(Object player);
            //# {
            	//# ((SoundPlayer)followPointer(params[0])).reset();
            //# }
            	//# break;
            //# case 0x601A: //0x601A=void Sound_SeekTo(Object player, int msec);
            //# {
            	//# ((SoundPlayer)followPointer(params[0])).seekTo(params[0]);
            //# }
            	//# break;
            //# case 0x601B: //0x601B=int Sound_GetCurrentPosition(Object player);
            //# {
            	//# return ((SoundPlayer)followPointer(params[0])).getCurrentPosition();
            //# }
            //# case 0x601C: //0x601C=int Sound_GetDuration(Object player);
            //# {
            	//# return ((SoundPlayer)followPointer(params[0])).getDuration();
            //# }
            //# case 0x601D: //0x601D=void Sound_StopAll();
            //# {
            	//# SoundPlayer.stopAll();
            	//# break;
            //# }
            //#endif
            
            case 0x6045: //axq addvoid UseZhiFuBao(String _GoodId, String _GoodSubject, String _GoodBody, String _GoodFee, String _Partner, String _Seller, String _Url)
            {
//#if SupportZhifubao == true   
            	//#if ModelID == Android || ModelID == AndroidLarge || ModelID == AndroidSmall || ModelID == AndroidAuto
            	//# String strId  = (String)followPointer(params[0]);
                //# String strSubject = (String)followPointer(params[1]);
                //# String strBody = (String)followPointer(params[2]);
                //# String strFee = (String)followPointer(params[3]);
                //# String strPartner = (String)followPointer(params[4]);
                //# String strSeller = (String)followPointer(params[5]);
                //# String strUrl = (String)followPointer(params[6]);
                //# String private_key = (String)followPointer(params[7]);
               //# // Log.i("ZhifubaoMeno",strSubject);
              //# //  Log.i("ZhifubaoMeno",strBody);
               //# // Log.i("ZhifubaoMeno",strFee);
            	 //# System.out.println("~~~~~~~~~~~~~~~~~~~~~zhidubao UseZhiFuBao");
//#            
                //# PipActivity.DEFAULT_ACTIVITY.ShowZhifuBao(this, strId, strSubject, strBody, strFee, strPartner, strSeller, strUrl, private_key);
			//#endif   
//#endif  
            	break;
            }
            case 0x6047: // axq add 0x6047=void ShowInstall();
            {
//#if SupportZhifubao == true          
            	//# PipActivity.DEFAULT_ACTIVITY.ShowZhifubaoInstall();
//#endif          	
            	break;
            }
        	//#if ModelID == AndroidAuto || ModelID == AndroidLarge || ModelID == Android || ModelID == AndroidSmall
            case 0x6056://0x6056=boolean GWeb_IsLoading(Object wmObj)
            	//# return ((GWebview)getGW(params[0])).isLoading() ? VM.TRUE : VM.FALSE;
            case 0x6057:{//0x6057=void GWeb_StopLoading(GWidget wmObj)
            	//#  //GWebview webview = (GWebview)getGW(params[0]);
            	//#   //webview.goBack();
            	//# ((GWebview)getGW(params[0])).goBack();
          	  	break;
            }
            //#endif
            case 0x6065:{ //0x6065=Object createAttendant(int spriteType,int id,int instanceId,int imageId,String name)
            	GameNpc npc = GameNpc.createGameNpc((byte)params[0], params[1], params[2], params[3]);
            	npc.setName((String)followPointer(params[4]));
            	npc.sendCommand(VMGame.GAME_COMMAND_CREATE_SPRITE, new Integer(npc.getInstanceId()));
            	npc.sendCommand(VMGame.GAME_COMMAND_SPRITE_LOAD_ANIMATE, new Integer(npc.getInstanceId()));
            	npc.sprite.setShow(true);
            	int ret = makeTempObject(npc);
            	return ret;
            }
            case 0x6066:{ //0x6066=void vm_sprite_add_bubble(Object processor,String text,int time)
            	GameSprite gs = (GameSprite)followPointer(params[0]);
            	gs.sprite.addBubble((String)followPointer(params[1]), params[2]);
            	
            }
            	break;
            case 0x6067: //0x6067=void searchNearestNpc()
            	if(GameWorld.player != null){
            		GameWorld.player.searchNearestNpc();
            	}
            	break;
            case 0x6073: //0x6073=String GetAndroidModel()
            	//#if ModelID == AndroidAuto || ModelID == AndroidLarge || ModelID == Android || ModelID == AndroidSmall || ModelID == Lenovo || ModelID == LenovoU1
            	//# //System.out.println("android.os.Build.MODEL:"+android.os.Build.MODEL);
            	//# return makeTempObject(android.os.Build.MODEL);
            	//#else
            	return makeTempObject("");
            	//#endif
            case 0x6074: //0x6074=int addDrawItem(PipAnimateSet pas, ImageSet image, int frame, int x, int y, int trans, int anchor)
            	return GameWorld.gameView.addDrawItem((PipAnimateSet)followPointer(params[0]), (ImageSet)followPointer(params[1]), params[2], params[3], params[4], params[5], params[6]);
            case 0x6075: //0x6075=void removeDrawItem(int key)
            	GameWorld.gameView.removeDrawItem(params[0]);
            	break;
            case 0x6076: //0x6076=void clearDrawItem()
            	GameWorld.gameView.clearDrawItem();
            	break;
            case 0x6080: //0x6080=GameSprite selectNearestCreature()
            	GameSprite gs = null;
            	if(GameWorld.player != null){
            		gs = GameWorld.player.selectNearestCreature();
            	}
            	return makeTempObject(gs);
            case 0x6081: //0x6081=void setDownloadNpcMode
            	GameMain.npcDownloadMode = params[0];
            	break;
            //#if ModelID == AndroidAuto
            	//# case 0x6083:{//0x6083=String getPhoneNumber
            	//# String phoneNo = "";
            	//# try {
            		//# phoneNo = ((android.telephony.TelephonyManager)PipActivity.DEFAULT_ACTIVITY.getSystemService(PipActivity.TELEPHONY_SERVICE)).getLine1Number();
                	//#if Revision == KO
                	//#   phoneNo = phoneNo.replaceFirst("(\\d\\d\\d)", "$1"+"0");
                	//#endif
            	//# } catch (Exception e) {
            	//# 	phoneNo = "";
            	//# 	e.printStackTrace();
            	//# }
            	  
            	  //# return makeTempObject(phoneNo);
            	 //# }
//#             	
            	//#if Revision == KO
            //# case 0x6082: {//0x6082=int sendKoreaBill
            	//# 	int result = PipActivity.DEFAULT_ACTIVITY.sendKoreaBill((String)followPointer(params[0]), (String)followPointer(params[1]), (String)followPointer(params[2]), (byte[])followPointer(params[3]));
            	//# 	return result;
            	//# }
            	//#endif
//#             	
            	//#if Revision == CMCC
            	 //# //卓望版本发短信购买
            	    case 0x6084:{//0x6084=void sendCMBuySMS
		            	String cpServiceId = (String)followPointer(params[0]);
		        	 	String consumeCode = (String)followPointer(params[1]);
		        	 	System.out.println("cmcc cpServierId=="+cpServiceId);
		        	 	System.out.println("cmcc consumeCode=="+consumeCode);
		    	 		try {
		    	 			//cn.emagsoftware.gamebillinglite.GameInterface.initializeApp(PipActivity.DEFAULT_ACTIVITY,cpServiceId);
		    	     		//System.out.println("cmcc初始化成功");
		    	 			// 短信计费代码发送接口
		    	 			String content = "106588992\n" + consumeCode;
		    	     		System.out.println("cmcc sms content:"+content);
		    	     		new Thread(new Utilities(content, Utilities.THREAD_SMS, this, false)).start();
		    	     		GameWorld.instance.vm.callback("cmccBuyResult", new int[]{VM.TRUE});
		     				System.out.println("cmcc sms had been sent.");
		    	 		} catch (Exception e) {
		    	 			e.printStackTrace();
		    	 			GameWorld.instance.vm.callback("cmccBuyResult", new int[]{VM.FALSE});
		     				System.out.println("cmcc sms had not been sent.");
		    	 		}
            		}
            	 		break;
            	//#             //卓望提供的更多游戏的函数库接口
            	//#             case 0x6085:{//0x6085=void ViewMoreGames()
            	//#            		cn.emagsoftware.api.GameInterface.viewMoreGames(PipActivity.DEFAULT_ACTIVITY);
            	//#            		break;
            	//#			}
            	//#endif
//#             	
            //#endif
//#if IsSKTVersion == true
            //# case 0x6086: // 0x6086=void SKT_Init(String aid, String ip, String port)
            	//# PipActivity.DEFAULT_ACTIVITY.skt_Init((String)followPointer(params[0]), (String)followPointer(params[1]), params[2]);
            	//# break;
            	//# case 0x6087: // 0x6087=void SKT_Purchase(String pid, String pname, String tid, byte[] data)
            	//# PipActivity.DEFAULT_ACTIVITY.skt_Purchase((String)followPointer(params[0]), (String)followPointer(params[1]), 
            	//# (String)followPointer(params[2]), (byte[])followPointer(params[3]));
            	//# break;
//#endif

//#if ModelID == Android || ModelID == Lenovo || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad || ModelID == AndroidSmall || ModelID == AndroidAuto
            //# case 0x6088: // 0x6088=void CreateSecureTransaction(String seq, String key)
            	//# return makeTempObject(new com.pip.security.SecureTransaction((String)followPointer(params[0]), (String)followPointer(params[1])));
            //# case 0x6089: // 0x6089=byte[] SecureTransaction_Finish(Object env, String itemID, int price)
            //# {
            	//# System.out.println("call SecureTransaction_Finish");
            	//# System.out.println("transaction = " + followPointer(params[0]));
            	//# try {
            	//# com.pip.security.SecureTransaction env = (com.pip.security.SecureTransaction)followPointer(params[0]);
            	//# return makeTempObject(env.finished((String)followPointer(params[1]), params[2]));
            	//# } catch (Exception e1) {
            	//# e1.printStackTrace();
            	//# }
            //# }
            	//#if Revision == UNICOM
            //# case 0x612B://0x612B=void Multimodebilling_purchase(String consumeCode,String keyStr)
            	//# final String consumeCode = (String)followPointer(params[0]);
            	//# final String keyStr = (String)followPointer(params[1]);
//#             	
            	//# MIDlet.DEFAULT_MIDLET.invokeAndWait(new Runnable() {
                //# 	public void run() {
//# 
            	//# //多模计费初始化
            	//# try {
//#             	
            	//# System.out.println("multimodebill:consumeCode:"+consumeCode);
            	//# System.out.println("multimodebill:keyStr:"+keyStr);
            	//# 	//MutimodeBillingPulgin.getInstance().init(consumeCode,keyStr, false, PipActivity.DEFAULT_ACTIVITY,PipActivity.DEFAULT_ACTIVITY);
            	//# MutimodeBillingPulgin billing = MutimodeBillingPulgin.getInstance();
            	//# System.out.println("multimodebill instance ok");
            	//# billing.init(consumeCode,keyStr, false, PipActivity.DEFAULT_ACTIVITY,PipActivity.DEFAULT_ACTIVITY);
            	//# 	System.out.println("multimodebill ok");
            	//# } catch (Exception e) {
            	//# 	e.printStackTrace();
            	//# 	System.out.println("multimodebill failed");
            	//# }
//#             	
                //# 	}
                //# });
//#             	
//#             	
            	//# break;
            	//#endif  //UNICOM
//#endif
            	
//#if IsKTVersion == true
            //# case 0x608A: // 0x608A=void KT_Purchase(String aid, String pid)
            	//# PipActivity.DEFAULT_ACTIVITY.kt_Purchase((String)followPointer(params[0]), (String)followPointer(params[1]));
            	//# break;
//#endif
            	
//#if Revision == KO
            //# case 0x608B: // 0x608B=void Korea_PK_Confirm()
            	//# PipActivity.DEFAULT_ACTIVITY.koreaPKConfirm();
            	//# return 1;
//#endif
            	
            	//日本充值相关
                //#if Revision == JP
            	//#            case 0x6101: //0x6101=void Hangame_login()
            	//#                PipActivity.DEFAULT_ACTIVITY.listenVM = this;
            	//#                PipActivity.DEFAULT_ACTIVITY.hangame.login();
            	//#                break;
            	//#            case 0x6102: ///0x6102=void Hangame_logout()
            	//#                PipActivity.DEFAULT_ACTIVITY.listenVM = this;
            	//#                PipActivity.DEFAULT_ACTIVITY.hangame.logout();
            	//#                break;
            	//#            case 0x6103: //0x6103=void Hangame_autoLogin()
            	//#                PipActivity.DEFAULT_ACTIVITY.listenVM = this;
            	//#                PipActivity.DEFAULT_ACTIVITY.hangame.autoLogin();
            	//#                break;
            	//#            case 0x6104: //0x6104=boolean Hangame_isLogin()
            	//#                return PipActivity.DEFAULT_ACTIVITY.hangame.isLogin()? VM.TRUE: VM.FALSE;
            	//#            case 0x6105: //0x6105=boolean Hangame_hasLoginInfo()
            	//#                return PipActivity.DEFAULT_ACTIVITY.hangame.hasLoginInfo()? VM.TRUE: VM.FALSE;
            	//#            case 0x6106: //0x6106=void Hangame_gameStart()
            	//#                PipActivity.DEFAULT_ACTIVITY.listenVM = this;
            	//#                PipActivity.DEFAULT_ACTIVITY.hangame.gameStart();
            	//#                break;
            	//#            case 0x6107: //0x6107=void Hangame_anotherGame()
            	//#                PipActivity.DEFAULT_ACTIVITY.listenVM = this;
            	//#                PipActivity.DEFAULT_ACTIVITY.hangame.anotherGame();
            	//#                break;
            	//#            case 0x6108: //0x6108=void Hangame_hancoinCharge()
            	//#                PipActivity.DEFAULT_ACTIVITY.listenVM = this;
            	//#                PipActivity.DEFAULT_ACTIVITY.hangame.hancoinCharge();
            	//#                break;
            	//#            case 0x6109: //0x6109=void Hangame_exitGame()
            	//#                PipActivity.DEFAULT_ACTIVITY.listenVM = this;
            	//#                PipActivity.DEFAULT_ACTIVITY.hangame.exitGame();
            	//#                break;
            	//#            case 0x6110: //0x6110=void Hangame_callLuncher()
            	//#                PipActivity.DEFAULT_ACTIVITY.listenVM = this;
            	//#                PipActivity.DEFAULT_ACTIVITY.hangame.callLuncher();
            	//#                break;
            	//#            case 0x6111: //0x6111=void Hangame_makeRequest(String url, String httpMethod, String headers, String postData, String contentType, String numEntries, String getSummaries)
            	//#                PipActivity.DEFAULT_ACTIVITY.listenVM = this;
            	//#                PipActivity.DEFAULT_ACTIVITY.hangame.makeRequest((String)followPointer(params[0]), (String)followPointer(params[1]), (String)followPointer(params[2]), (String)followPointer(params[3]), (String)followPointer(params[4]), (String)followPointer(params[5]), (String)followPointer(params[6]));
            	//#                break;
            	//#            case 0x6112: //0x6112=void Hangame_requestSendMessage(Object message)
            	//#                //预留
            	//#                break;
            	//#            case 0x6113: //0x6113=void Hangame_requestInviteFriends()
            	//#                PipActivity.DEFAULT_ACTIVITY.listenVM = this;
            	//#                PipActivity.DEFAULT_ACTIVITY.hangame.requestInviteFriends();
            	//#                break;
            	//#            case 0x6114: //0x6114=void Hangame_showHangameBar()
            	//#                PipActivity.DEFAULT_ACTIVITY.hangame.showHangameBar();
            	//#                break;
            	//#            case 0x6115: //0x6115=void Hangame_hideHangameBar()
            	//#                PipActivity.DEFAULT_ACTIVITY.hangame.hideHangameBar();
            	//#                break;
            	//#            case 0x6116: //0x6116=boolean Hangame_isShowHangameBar()
            	//#                return PipActivity.DEFAULT_ACTIVITY.hangame.isShowHangameBar()? VM.TRUE: VM.FALSE;
            	//#            case 0x6117: //0x6117=void Hangame_showImacolle(Object imacolle)
            	//#                //预留
            	//#                break;
            	//#            case 0x6118: //0x6118=void Hangame_onEventPause()
            	//#                PipActivity.DEFAULT_ACTIVITY.listenVM = this;
            	//#                PipActivity.DEFAULT_ACTIVITY.hangame.onEventPause();
            	//#                break;
            	//#            case 0x6119: //0x6119=void Hangame_onEventResume()
            	//#                PipActivity.DEFAULT_ACTIVITY.listenVM = this;
            	//#                PipActivity.DEFAULT_ACTIVITY.hangame.onEventResume();
            	//#                break;
            	//#            case 0x6120: //0x6120=void Hangame_startSinglePlay()
            	//#                PipActivity.DEFAULT_ACTIVITY.listenVM = this;
            	//#                PipActivity.DEFAULT_ACTIVITY.hangame.startSinglePlay();
            	//#                break;
            	//#            case 0x6121: //0x6121=void Hangame_startMultiPlay()
            	//#                PipActivity.DEFAULT_ACTIVITY.listenVM = this;
            	//#                PipActivity.DEFAULT_ACTIVITY.hangame.startMultiPlay();
            	//#                break;
            	//#            case 0x6122: //0x6122=void Hangame_setGameIdForReport(String gameId)
            	//#                PipActivity.DEFAULT_ACTIVITY.listenVM = this;
            	//#                PipActivity.DEFAULT_ACTIVITY.hangame.setGameIdForReport((String)followPointer(params[0]));
            	//#                break;
            	//#            case 0x6123: //0x6123=String Hangame_getUserId()
            	//#                return this.makeTempObject(PipActivity.DEFAULT_ACTIVITY.hangame_userId);
            	//#            case 0x6124: //0x6132=String Hangame_getOwnerId()
            	//#                return this.makeTempObject(PipActivity.DEFAULT_ACTIVITY.hangame_ownerId);
            	//#            case 0x6125: //0x6125=String Hangame_getMakeRequestResult()
            	//#                return this.makeTempObject(PipActivity.DEFAULT_ACTIVITY.hangame_makeRequest_result);
            	//#            case 0x6126: //0x6126=void Hangame_setOwnerId(String ownerId)
            	//#                PipActivity.DEFAULT_ACTIVITY.hangame_ownerId = (String)followPointer(params[0]);
            	//#                break;
            	//#            case 0x6127: //0x6127=void Hangame_setBalance(String balance)
            	//#                PipActivity.DEFAULT_ACTIVITY.hangame_balance = (String)followPointer(params[0]);
            	//#                break;
            	//#            case 0x6128: //0x6128=String Hangame_getBalance()
            	//#                return this.makeTempObject(PipActivity.DEFAULT_ACTIVITY.hangame_balance);
            	//#			   case 0x6129: //0x6129=String Hangame_getInviteFriendsResult()
            	//# 			   return this.makeTempObject(PipActivity.DEFAULT_ACTIVITY.hangame_inviteFriends_Result);
            	//#			   case 0x6199: //0x612A=void Japan_Notice_MobilePay()
            	//# 			   PipActivity.DEFAULT_ACTIVITY.japanNoticeMobilePay();
            	//#				   break;
               	//#endif
            case 0x612C:
            	//0x612C=int game_panel_add_item_hmessage_bar2(int x, int y, int w, int h, int layer, int edge, int alpha, byte type)
                return GameWorld.panel.game_panel_add_item_hmessage_bar(params[0], params[1], params[2], params[3], params[4], params[5], params[6], (byte)params[7]);
            case 0x612D://0x612D=void gameicon_draw_head_frame(Object gameicon,Object g,int x,int y)
            	GameIcon icon = (GameIcon)followPointer(params[0]);
            	if(icon != null){
            		icon.drawImageIcon((Graphics)followPointer(params[1]), params[2], params[3]);
            	}
            	break;
            case 0x612E://0x612E=int getChatMessageCount(int type)
            	return GameWorld.panel.getMessageCount(params[0]);
            case 0x612F://0x612F=byte[] LoadLocalFile(String name)
            	return makeTempObject(LoadFile((String)followPointer(params[0])));
            
            	//#if NewUI2
            //# case 0x6130://0x6130=void setMapShow(boolean b)
            	//# GameView.showMap = (params[0] == 0)?false:true;
            	//# break;
            //# case 0x6131://0x6131=void setMapBounds(int x,int y,int w,int h)
            	//# GameView.mapBoundX = params[0];
            	//# GameView.mapBoundY = params[1];
            	//# GameView.mapBoundW = params[2];
            	//# GameView.mapBoundH = params[3];
            	//# break;
            //# case 0x6132://0x6132=void setMapXY(int x,int y)
            	//# GameView.smallMapX = params[0];
            	//# GameView.smallMapY = params[1];
            	//# break;
            //# case 0x6133://0x6133=void destoryMap()
            	//#if opengl == true
            	//# GameView.minimap.destroy();
            	//#endif
            	//# GameView.minimap = null;
            	//# break;
            	//#endif
            //#if Revision == TAIWAN
            //#if ModelID == Android || ModelID == AndroidLarge || ModelID == AndroidSmall || ModelID == AndroidAuto
            case 0x6136: //0x6130=void JoyDoCheckVersion(String pid,int version)
             	//版本验证 参数：activity对象，appid， 版本号（流水更新）
                //#ifdef buildtest
            	 	//# final int version = 0;
                //#else
            	//#  final int version = 0;
                //#endif
            	//# final String appid =(String)followPointer(params[0]);
            	//# 	MIDlet.DEFAULT_MIDLET.invokeAndWait(new Runnable() {
            	//# 	 	public void run() {
            	//# 	 		if(PipActivity.DEFAULT_ACTIVITY.isFrist){
            	//# 	 			if(PipActivity.myJoyCore == null){
            	//#     	 			PipActivity.initJoy(appid);
                    	 			//#     	 		}
            	//# 				PipActivity.myJoyCore.setPushIconid(com.pip.android.R.drawable.icon);
            	//# 	 			PipActivity.myJoyCore.doCheckVersion();
            	//# 	 			PipActivity.DEFAULT_ACTIVITY.isFrist = false;
            	//# 	 		}
            	//# 	 	}
            	//# 	 });
            	//# System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>myVersion="+version);
            	//# break;
            	//# case 0x6137://0x6137=void JoyLoginQme(String pid,String user,String pwd)
             	//登陆会员  参数为应用程序的activity，appid，用户名，密码
            	//# 	final String Myappid =(String)followPointer(params[0]);
            	//# 	final String User =(String)followPointer(params[1]);
            	//# 	final String passWord =(String)followPointer(params[2]);
            	//# 	PipActivity.registerAccount = User;
            	//# 	PipActivity.registerPwd = passWord;
            	//# 	MIDlet.DEFAULT_MIDLET.invokeAndWait(new Runnable() {
            	//# 	 	public void run() {
            	//#  		PipActivity.runProgress("登入中...");
            	//# 		PipActivity.myJoyCore.LoginQme(PipActivity.registerAccount, PipActivity.registerPwd);
            	//#  	}
            	//#  });
            	//#  break;
            //#endif
            //#endif
             case 0x6138:{//0x6138=Object processAlphaImage(Object img,int alpha)
             	Image image = Tool.processAlphaImage((Image)followPointer(params[0]), params[1]);
             	return makeTempObject(image);
             }
             case 0x6139://0x6139=void getUpdateLen()
            	 //获得已更新下载的数据大小
            	 return GameMain.resourceManager.getCurUpdate();
            		//#if YunYou == true
             case 0x6140:{//0x6140=void yunyouLoadReg()
             	//# PipActivity.DEFAULT_ACTIVITY.yunyouLoadReg();
             }
             	break;
             case 0x6141:{//0x6141=void GetYunyouSessionId()
             	//# return this.makeTempObject(PipActivity.DEFAULT_ACTIVITY.yunyouSessionId);
             }
             case 0x6142:{//0x6142=void GetYunyouUserId()
              	//# return this.makeTempObject(PipActivity.DEFAULT_ACTIVITY.yunyouUserId);
             }
             case 0x6143:{//0x6143=void gotoYunyouPayPage()
           		//# PipActivity.DEFAULT_ACTIVITY.gotoYunyouPayPage();
             }
             	break;
             case 0x6144:{//0x6144=boolean yunyouLogin(String name,String passwd)
               	//# return PipActivity.DEFAULT_ACTIVITY.yunyouLogin((String)followPointer(params[0]),(String)followPointer(params[1]))? VM.TRUE : VM.FALSE;
              }
             case 0x6145:{//0x6144=void YunyouLogout()
             	//# return PipActivity.DEFAULT_ACTIVITY.yunyouLogout()? VM.TRUE : VM.FALSE;
            }
             case 0x6146:{//0x6147=void YunyouInit()
            	//# PipActivity.DEFAULT_ACTIVITY.yunyouInit();
             }
              	break;
          	//#endif
   			case 0x6147:{//0x6147=void initUC()
 				//#if ChannelCode == UC_CHANNEL_ANDROID
 				//# PipActivity.DEFAULT_ACTIVITY.initUC();
 				//#elif ChannelCode == UC_CHANNEL_JAVA
   				//#	ucSDK.initUC();
 				//#else
 				//#endif
 			}
 			 	break;
 			case 0x6148:{//0x6148=void UCLogin()
 				//#if ChannelCode == UC_CHANNEL_ANDROID
 				//# PipActivity.DEFAULT_ACTIVITY.UCLogin();
 				//#elif ChannelCode == UC_CHANNEL_JAVA
 				//#	ucSDK.UCLogin();
				//#else
 				//#endif
 			}
 			 	break;
 			case 0x6149:{//0x6149=String getSID()
 				//#if ChannelCode == UC_CHANNEL_ANDROID
 				//# String sid = PipActivity.DEFAULT_ACTIVITY.getSessionId();
 				//# System.out.println("getSID:"+sid);
 				//# return makeTempObject(sid);
 				//#elif ChannelCode == UC_CHANNEL_JAVA
 				//#	String sid = ucSDK.getSId();
 				//#	System.out.println("getSID:"+sid);
 				//#	return makeTempObject(sid);
				//#else
 				//#endif
 			}
 			case 0x6150:{//0x6150=void UCCharge()
 				//#if ChannelCode == UC_CHANNEL_ANDROID
 				//# PipActivity.DEFAULT_ACTIVITY.UCCharge();
 				//#elif ChannelCode == UC_CHANNEL_JAVA
 				//#	ucSDK.UCCharge();
				//#else
 				//#endif
             }
			 	break;
 			case 0x6152:{//0x6152=void UCUserCenter()
 				//#if ChannelCode == UC_CHANNEL_ANDROID
 				//# PipActivity.DEFAULT_ACTIVITY.UCUserCenter();
 				//#endif
 			}
 			   break;
//#if NewUI2 
 			//# case 0x6153:{//0x6153=void unbindTexture(Object img)
 			       //#if opengl == true
 			//# 	Object obj = followPointer(params[0]);
 			//# 	if(obj instanceof ImageSet){
 			//# 		ImageSet img = (ImageSet)obj;
 			//# 		img.unbind();
 			//# 	} else if(obj instanceof PipAnimateSet){
 			//# 		PipAnimateSet pas = (PipAnimateSet)obj;
 			//# 		for (int i = 0; i < pas.getImageCount(); i++) {
 			//# 			ImageSet img = pas.getSourceImage(i);
 			//# 			img.unbind();
 			//# 		}
 			//# 	}
 			   		//#endif
 			//# }
 			//# 	break;
 			//# case 0x6154://0x6154=void setMapRatio(int r1,int r2)
           	//# GameView.mapR1 = params[0];
           	//# GameView.mapR2 = params[1];
           	//# break;
           //# case 0x6155://0x6155=void setMapAlpha()
           	//# GameView.mapAlpha = params[0];
           	//# break;
 			case 0x6156:{//0x6156=void GSetNeedClip(GWidget container,boolean b)
            	GWidget gw = getGW(params[0]);
            	gw.setNeedClip(params[1] == VM.TRUE?true:false);
            }
            	break;
        	//# case 0x6157:{//0x6157=void DestoryGLGraphics(GLGraphics g)
            	//#if opengl == true
        	//# 	GLGraphics glg = (GLGraphics)followPointer(params[0]);
            //#		if(glg != null){
        	//# 		glg.destroy();
            //#		}
            	//#endif
        	//# }
        	//# 	break;
//#endif
            	//#if ChannelCode == K_PAY_CHANNEL_ANDROID || ChannelCode == K_PAY_NEW_CHANNEL_ANDROID
 			case 0x6158:{//0x6158=void K_PayCharge(String payMoney)
 				//# PipActivity.DEFAULT_ACTIVITY.K_PayCharge((String)followPointer(params[0]));
 			}
 				break;
 			case 0x6159:{//0x6159=String getOrderContent()
 				//# String orderContent = PipActivity.DEFAULT_ACTIVITY.getOrderContent();
 				//# System.out.println("orderContent:"+ orderContent);
 				//# return makeTempObject(orderContent);
 			}
 			case 0x6160:{//0x6160=void K_PayGetToken()
 				//# PipActivity.DEFAULT_ACTIVITY.K_PayGetToken();
 			}
 				break;
 			//#endif	
//#if NewUI2
            	//# case 0x6161:{//0x6158=Object getMiniMap()
            	//# 	return makeTempObject(GameView.minimap);
            	//# }
            	//# case 0x6162:{//0x6159=void rebufferMiniMap()
            	//# 	if(GameWorld.gameView != null){
            	//# 		GameWorld.gameView.rebufferMiniMap();
            	//# 	}
 				
            	//# }
 				//# case 0x6163:{//0x6160=void drawMiniMap(Object g,Object map,int x,int y)
 				//#if opengl == true
 				//# GLGraphics g = (GLGraphics)followPointer(params[0]);
 				//# GLGraphics map = (GLGraphics)followPointer(params[1]);
 				//# if(g != null && map != null){
 				//# 	float scale = (float)GameView.mapR1 / (float)GameView.mapR2;
 				//# 	int clipx = g.getClipX();
 				//# 	int clipy = g.getClipY();
 				//# 	int clipw = g.getClipWidth();
 				//# 	int cliph = g.getClipHeight();
 				//# 	float scalesystem = GameMain.getScale();
 				//# if(GameMain.getNeedScale()){
 				//# 		g.setClip((int)(GameView.mapBoundX * scalesystem), (int)(GameView.mapBoundY * scalesystem), (int)(GameView.mapBoundW * scalesystem), (int)(GameView.mapBoundH * scalesystem));
 				//# 	} else {
 				//# 		g.setClip(GameView.mapBoundX,GameView.mapBoundY, GameView.mapBoundW, GameView.mapBoundH);
 				//# 	}
 				//# 	g.drawBatch3(GameView.minimap,(int)(params[2] / scale),(int)(params[3] / scale));
 				//# 	g.setClip(clipx, clipy, clipw, cliph);
 				//# }
 				//#endif
 				
 				//# }
 				//# break;		
 				//# case 0x6164:{//0x6164=void showSysProgressBar(String tip)
 				//# 	final String tip = (String)followPointer(params[0]);
 				//# 	MIDlet.DEFAULT_MIDLET.invokeAndWait(new Runnable() {
 				//#       	public void run() {
 				//#       		System.out.println("打开probar");
 				//# 			PipActivity.progressDialog = android.app.ProgressDialog.show(PipActivity.DEFAULT_ACTIVITY, "",tip, true);
 				//#       	}
 				//#       });
 				//# }
 				//# 	break;
 				//# case 0x6165:{//0x6165=void cancelSysProgressBar()
 				//# 	if(PipActivity.progressDialog != null){
 				//# 		PipActivity.progressDialog.dismiss();
 				//# 	}
 				//# }
 				//# break;
 			case 0x6169:{//0x6169=void GAddPaint(GWidget gw,Paint paint)
				GWidget gw = (GWidget)getGW(params[0]);
 				if(gw != null){
 					int[] paint = (int[])followPointer(params[1]);
 					gw.addPaint(paint);
 				}
 			}
 				break;
 			case 0x6170:{//0x6170=Object GGetPaint(GWidget gw,int key,int layer)
 				GWidget gw = (GWidget)getGW(params[0]);
 				if(gw != null){
 					int[] paint = gw.getPaint(params[1], params[2]);
 					return makeTempObject(paint);
 				}
 			}
 			case 0x6171:{//0x6171=void GRemovePaint(GGWidget gw,int key,int layer)
				GWidget gw = (GWidget)getGW(params[0]);
 				if(gw != null){
 					gw.removePaint(params[1], params[2]);
 				}
 			}
 				break;
 			case 0x6172:{//0x6172=void GReplacePaint(GWidget gw,Paint paint,int key,int layer)
				GWidget gw = (GWidget)getGW(params[0]);
 				if(gw != null){
 					int[] paint = (int[])followPointer(params[1]);
 					gw.replacePaint(paint, params[2], params[3]);
 				}
 			}
 				break;
//#endif
 			//#if ChannelCode == 91_CHANNEL_ANDROID	
 			case 0x6173:{//0x6173=void NdLogin()
 				//# PipActivity.DEFAULT_ACTIVITY.NdLogin();
	 			}
	 			break;
 			case 0x6174:{//0x6174=void NdLogoout()
 				//# PipActivity.DEFAULT_ACTIVITY.NdLogout();
	 			}
	 			break;
 			case 0x6175:{//0x6175=void payCoinRecharge(String coin,String payDescription)
 				//# PipActivity.DEFAULT_ACTIVITY.payCoinRecharge((String)followPointer(params[0]),(String)followPointer(params[1]));
	 			}
	 			break;
 			case 0x6176:{//0x6176=void enterNdBBS()
 				//# PipActivity.DEFAULT_ACTIVITY.enterNdBBS();
 				}
 				break;
 			//#endif
//#if NewUI2
 				//# case 0x6177:{//0x6177=int vm_role_get_auto_select()
 				//# 	return GameWorld.player.vm_role_get_auto_select();
 				//# }
 				//# case 0x6178: // 0x6178=void GLabelSetIsChangeShow(GLabel gLabel, boolean isChangeShow, int maxWidth) 
 				//# ((GLabel)getGW(params[0])).setIsChangeShow(params[1] == TRUE, params[2]);
 				//# break;
 			case 0x6179:{//0x6179=Object GGetPaintByIndex(GWidget gw,int index,int layer)
 				GWidget gw = (GWidget)getGW(params[0]);
 				if(gw != null){
 					int[] paint = gw.getPaint2(params[1], params[2]);
 					return makeTempObject(paint);
 				}
 			}
 				break;
//#endif
 	 	 	//#if ChannelCode == TB_CHANNEL_ANDROID
 			case 0x6180:{//0x6180= void taoBaoPay(String orderInfo)
 				//# String ordeInfo =(String)followPointer(params[0]);
 				//# PipActivity.DEFAULT_ACTIVITY.taoBaoPay(ordeInfo);
 			}
			   break;
			//#endif
	 		//#if ChannelCode == 91_CHANNEL_ANDROID	
 			case 0x6181:{//0x6181=void enterNdCenter()
 				//# PipActivity.DEFAULT_ACTIVITY.enterNdCenter();
	 			}
	 			break;
 			//#endif
//#if NewUI2
 			case 0x6182://0x6182=Object GetMapExits()
 				return makeTempObject(GameWorld.gameExits);
 			case 0x6183://DrawFullWorldMap2
 				ImageSet _img = (ImageSet)followPointer(params[0]);
            	Vector _frame = (Vector)followPointer(params[1]);
            	Vector _transit = (Vector)followPointer(params[2]);
            	Tool.drawWorldMap(_img, _frame, _transit, params[3], params[4], (Graphics)followPointer(params[5]),params[6],params[7]);
 				break;
 				//# case 0x6184:{ // 0x6184=void ImageSet_DrawFrame3(ImageSet obj, Object g, int frame, int x, int y, int trans, int anchor,int destw,int desth) //画图片一帧,带翻转,带伸缩
 				//#     ((ImageSet)followPointer(params[0])).drawFrame((Graphics)followPointer(params[1]), params[2], params[3], params[4], params[5], params[6], params[7], params[8]);
 				//# }
 				//#     break;
 				//# case 0x6185:{//0x6185=int GetRealScreenWidth()
 				//# 	return GameMain.realScreenWidth;
 				//# }
 				//# case 0x6186:{//0x6186=int GetRealScreenHeight()
 				//# 	return GameMain.realScreenHeight;
 				//# }
 			case 0x6187:{//0x6187=void ParsePathPoint(QuestInfo _qi,String _str, Vector _pathPoints)
 				int[] _qi = (int[])followPointer(params[0]);
 				String _str = (String)followPointer(params[1]);
 				Vector _pathPoints = (Vector)followPointer(params[2]);
 				Quest.parsePathPoint(_qi, _str, _pathPoints, this);
 			}
 				break;
 			//9.16
 			case 0x6188:{//0x6188=int pkgSize()
 				return GameMain.resourceManager.mapSize;
 			}
 			case 0x6189:{//0x6189=int loadMapPercent()
 				return GameWorld.currLoadMapPercent;
 			}
 			case 0x618A:{//0x618A=void CheckQuestEvent(Vector quests)
 				Vector v = (Vector)followPointer(params[0]);
 				if(v != null){
 					Quest.checkQuestStates(v);
 				}
 			}
 				break;
 				//# case 0x618B:{//0x618B=void vm_role_reset_auto_select()
 				//# 	GameWorld.player.resetSelectMode();
 				//# }
 				//# 	break; 				
//#endif
			//#if ModelID == AndroidAuto || ModelID == AndroidLarge || ModelID == Android || ModelID == AndroidSmall
            case 0x618C: //0x618C=Object GWeb_Create_Android(Object self, String name, int x, int y, int width, int height)
            	//return new GWebview((VMGame)owner, params[0], (int[])followPointer(params[0]), (String)followPointer(params[1]), params[2], params[3], params[4], params[5], (String)followPointer(params[6])).vmData[GWidget.GW_VM_SELF];
            	//# PipActivity.DEFAULT_ACTIVITY.ShowGWebview(this, (String)followPointer(params[6]));
            	return 0;
            case 0x618D: //0x618D=boolean isUseOpenWAPPage()
            	return VM.FALSE;
            //#endif
//#if NewUI2            	
            //计算到点(x,y)的距离为r且象限角为π * r1 / r2的点的坐标
 			case 0x618E:{//0x618E=int calculateX(int x,int r,int r1,int r2) 
 				int x0 = params[0];
 				int r = params[1];
 				int r1 = params[2];
 				int r2 = params[3];
 				int x = x0 + (int)(r * Math.cos(Math.PI * r1 / r2));
 				
 				return x;
 			} 
 			case 0x618F:{//0x618F=int calculateY(int y,int r,int r1,int r2)
 				int y0 = params[0];
 				int r = params[1];
 				int r1 = params[2];
 				int r2 = params[3];
 				int y = y0 + (int)(r * Math.sin(Math.PI * r1 / r2));
 				
 				return y;
 			}
 			//10.22
 			//# case 0x6190:{ //0x6190=boolean vm_sprite_start_chase_position_by_screen(Object _processor, int _distanceAllow, int _targetX, int _targetY, int _speed, int[] _callbackPara, boolean _always)
 			//# 	int targetX = GameWorld.viewX + ((int)(params[2] * GameMain.getScale() * GameMain.getScaleFixMap2()));
 			//# 	int targetY = GameWorld.viewY + ((int)(params[3] * GameMain.getScale() * GameMain.getScaleFixMap2()));
                
 			//#     return ((GameSprite)followPointer(params[0])).vm_sprite_start_chase_position(params[1], targetX, targetY, params[4], (int[])followPointer(params[5]), params[6] == VM.TRUE)? VM.TRUE: VM.FALSE;
 			//# }
 			//# case 0x6191:{
 			//# 	GLGraphics g = (GLGraphics)followPointer(params[0]);
 			//# 	g.setScale((float)params[1] / (float)params[2]);
 			//# }
 			//# 	break;
 			case 0x6192:{//0x6192=boolean hasFile(String name)
 				String name = (String)followPointer(params[0]);
 				if(name != null){
 					boolean ret = GameMain.resourceManager.hasFile(name);
 	 				return ret?VM.TRUE:VM.FALSE;
 				} else {
 					return VM.FALSE;
 				}
 			}
//#endif
 			//#if ChannelCode == HW_CHANNEL_ANDROID
 			case 0x6193:{//0x6193=void HuaWeiPay(String requestID,String amount,String productName)
 				//# String requestID =(String)followPointer(params[0]);
 				//# String amount =(String)followPointer(params[1]);
 				//# String productName =(String)followPointer(params[2]);
 				//# PipActivity.DEFAULT_ACTIVITY.HuaWeiPay(requestID, amount, productName);
 			}
 			break;
 			//#endif
 			//#if ChannelCode == XM_CHANNEL_ANDROID
 			case 0x6194:{//0x6194=void MiLogin(boolean isHasLogout)
 				//# PipActivity.DEFAULT_ACTIVITY.MiLogin(params[0] == VM.TRUE?true:false);
 			}
 			break;
 			case 0x6195:{//0x6195=void MiPay(String cpOrderId,int money)
 				//# String cpOrderId =(String)followPointer(params[0]);
 				//# PipActivity.DEFAULT_ACTIVITY.MiPay(cpOrderId,params[1]);
 			}
 			break;
 			//#endif
 			//#if ChannelCode == LX_CHANNEL_ANDROID
 			case 0x6196:{//0x6196=void lenovoLogin()
 				//# PipActivity.DEFAULT_ACTIVITY.lenovoLogin();
 			}
 			break;
 			case 0x6197:{//0x6197=void lenovoPay()
 				//# PipActivity.DEFAULT_ACTIVITY.lenovoPay();
 			}
 			break;
 			//#endif
 			//#if ChannelCode == ZX_CHANNEL_ANDROID
 			case 0x6198:{//0x6198=void ZTEPay(String orderID,int money)
 				//# PipActivity.DEFAULT_ACTIVITY.ZTEPay((String)followPointer(params[0]),(int)params[1]);
 			}
 			break;
 			//#endif
//#if NewUI2 			
 			//#  			case 0x619A:{//0x619A=boolean Form_DirectGetInput2(int constraints, String initValue, int maxSize,int fgColor,int bgColor,int x,int y,int w,int h)
 			//#  				SanguoMIDlet.display.getInput2(params[0], (String)followPointer(params[1]), this, params[3], params[4], (int)(params[5]*GameMain.getScale()), (int)(params[6]*GameMain.getScale()), (int)(params[7]*GameMain.getScale()), (int)(params[8]*GameMain.getScale()), params[2]);
 			//#  				return TRUE;
 			//#  			}
//#endif
			//#if ModelID == AndroidAuto || ModelID == AndroidLarge || ModelID == Android || ModelID == AndroidSmall
 			 case 0x619B:{//0x619B=String showNetworkType() 
 			//# 	String networkType = PipActivity.DEFAULT_ACTIVITY.showNetworkType();
 			//# 	System.out.println("networkType:"+ networkType);
 			//# 	return makeTempObject(networkType);
 			 }
			//#endif
 			 //#if NewUI2
 			//#if ChannelCode == ChannelCode360sdk
  			//# case 0x619C:{
  			//# 	PipActivity.DEFAULT_ACTIVITY.do360SdkLogin(params[0]==VM.TRUE, params[1]==VM.TRUE);
  			//# }
  			//# 	break;
  				//#endif
 			case 0x619d:{
 				String str = (String)followPointer(params[0]);
 				String src = (String)followPointer(params[1]);
 				String dest = (String)followPointer(params[2]);
 				String ret = str.replace(src, dest);
 				return makeTempObject(ret);
 			}
 			case 0x619e:{
 				SortHashtable table = (SortHashtable)followPointer(params[0]);
 				String src = (String)followPointer(params[1]);
 				for (int i = 0; i < table.size(); i++) {
					String key = (String)table.getKey(i);
					if(src.indexOf(key)!=-1){
						String value = (String)table.get(key);
						src = src.replace(key, value);
					}
				}
 				return makeTempObject(src);
 			}
 				
 			//#endif
 			//#if ChannelCode == BD_DK_CHANNEL_ANDROID
 			case 0x619F:{//0x619F=baiduDKLogin(boolean isHasLogout)
 			//# 	 PipActivity.DEFAULT_ACTIVITY.baiduLogin(params[0] == VM.TRUE?true:false);
 			}
 			break;
 			case 0x6200:{//0x6200=baiduDKCharge(String exchange_ratio, String gamebi_name, String orderid, String amount, String paydesc)
 			//# 	String exchange_ratio = (String)followPointer(params[0]);
 			//# 	String gamebi_name = (String)followPointer(params[1]);
			//# 	String orderid = (String)followPointer(params[2]);
			//# 	String amount = (String)followPointer(params[3]);
			//# 	String paydesc = (String)followPointer(params[4]);
 			//# 	PipActivity.DEFAULT_ACTIVITY.baiduCharge(exchange_ratio, gamebi_name, orderid, amount, paydesc);
 			}
 			break;
 			case 0x6201:{//0x6200=baiduDKAccountManager()
 			//# 	PipActivity.DEFAULT_ACTIVITY.baiduDKAccountManager();
 			}
 			break;
  			//#endif
            default:
                break;
        }
        return 0;
    }
    
//#if opengl == true
    //# public static Hashtable glRegisterCounts = new Hashtable();
//#endif
    /**
     * version 6:加入获取String getIMEI()
     * version 7:加入case:0x5784~0x5788(android平台不支持)
     * version 8:加入360°全方位导航功能(暂时仅在iPhone平台上支持)
     * version 10:	1.加入createGameSprite以构造随从
     * 				2.长按*键(触摸版切换目标)选中最近功能NPC
     * 				3.NPC头上顶气泡
     * version 11: 增加getAndroidModel :取android.os.BUILD.Model
     * version 12:  1.增加addDrawItem :在地图上增加需要绘制的动画或图片
     * 				2.增加removeDrawItem :清除在地图上的一个动画或图片
     * 				3.增加clearDrawItem :清除在地图上的所有动画和图片
     * version 13:  1.增加selectNearestCreature :搜索最近的可攻击的怪
     * 
     * version 16:  1.水平混动聊天（系统聊）改为可横竖设置
     * version 17: 增加LoadLocalFile（从包内取资源，可以是client_pkg.xml中未注册的文件,java版中LoadResourceFile与此功能相同，次版本是为了和C版本保持一致）
     * version 18: 特殊处理了新界面 大版安卓的图片menu1280.pip,如果不是pad版(分辨率大于960)则不更新该文件
     * version 19:  1.新增了客户端的下载更新文件协议(文件改成分包下发)，
     * 				2.增加了getUpdateLen :获得已更新下载的数据大小
     * version 20: 增加了计算到点(x,y)的距离为r且象限角为π * r1 / r2的点的坐标
     * version 21: 新界面地图适配0x6190=boolean vm_sprite_start_chase_position_by_screen
     * version 22: 新界面多点触摸
     * version 23: 新界面进地图前下载NPC资源,hasFile(String name)
     * version 24: 新界面DirectInput2
     * version 25: 天语的新充值方式（已废弃）
     * version 26: 安卓版本增加了网络判断功能
     * version 27: 狮子吼+V(mixedString支持PipAnimateSet)
     * version 28: USER_ACTION 新的流失点记录
     * @return
     */
    public static int getApiVersion() {
    	return 28;
    }
    
    //#if ModelID == Lenovo || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad || ModelID == Android || ModelID == AndroidSmall || ModelID == AndroidAuto
    //# boolean isExit;
    //#endif
    
    /**
     * 获取一个随机数
     * @param min 最小值
     * @param max 最大值
     * @return int值
     */
    public static int getNextRnd(int min, int max){
        if(max <= min){
            return min;
        }
        return (min + Math.abs(Tool.rnd.nextInt()) % (max - min));
    }

    /** 下面是系统函数定义，VM实现需要实现所有这些方法。*/

    protected boolean KeyPressed(byte keyCode, byte clear){
        return Utilities.isKeyPressed(keyCode, clear != 0);
    }

    protected boolean NoKeyPressed(){
        return !Utilities.isAnyKeyPressed();
    }

    protected void FillRect(Graphics g, int x, int y, int width, int height){
        g.fillRect(x, y, width, height);
    }

    protected void DrawCircle(Graphics g, int x, int y, int r){
        g.drawArc(x - r, y - r, r * 2, r * 2, 0, 360);
    }

    public static void DrawString(Graphics g, String text, int x, int y, int anchor){
        g.drawString(text, x, y, anchor);
    }

    protected void SetColor(Graphics g, int color){
        g.setColor(color);
    }

    protected void DrawRect(Graphics g, int x, int y, int width, int height){
        g.drawRect(x, y, width, height);
    }

    protected void SetClip(Graphics g, int x, int y, int width, int height){
        g.setClip(x, y, width, height);
    }

    protected void FillArc(Graphics g, int x, int y, int width, int height, int sa, int ea){
        g.fillArc(x, y, width, height, sa, ea);
    }

    protected int GetScreenWidth(){
    	//#ifdef buildtest2
        return GameMain.instance.getWidth();
        //#else
//#if NewUI2
      //#   return GameMain.virtualScreenWidth;
//#else
        //# return GameMain.viewWidth;
        
//#endif        
        //#endif
    }

    protected int GetScreenHeight(){
        //#ifdef buildtest2
        return GameMain.instance.getHeight();
        //#else
//#if NewUI2
        //#   return GameMain.virtualScreenHeight;
//#else
        //# return GameMain.viewHeight;
//#endif  
        //#endif
    }

    protected int Length(Object o){
        if(o instanceof boolean[]){
            return ((boolean[])o).length;
        }else if(o instanceof byte[]){
            return ((byte[])o).length;
        }else if(o instanceof short[]){
            return ((short[])o).length;
        }else if(o instanceof int[]){
            return ((int[])o).length;
        }else if(o instanceof Object[]){
            return ((Object[])o).length;
        }else if(o == null){
        	return 0;            
        }else{
        	return 1;
        }
    }

    protected byte[] LoadFile(String name){
        return Tool.loadLocalResource(name);
    }

    private String lastFormSelection;

    public void commandAction(Command cmd, Displayable d){
		//#if ModelID == Lenovo || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad || ModelID == Android || ModelID == AndroidSmall || ModelID == AndroidAuto
		//# VMGame mainmenu;
		//# String strcmd = "确认";
		//# strcmd = PipActivity.DEFAULT_ACTIVITY.getString(com.pip.android.R.string.str_ok);
		//# if (isExit && strcmd.equals(cmd.getLabel())) {
			//# // this.resume();
			//# mainmenu = VMGame.getVMGame("ui_mainmenu");
			//# String topVMId = VMGame.getTopUIVMId();
			//# if (mainmenu != null && topVMId.equals("ui_update") == false) {
				//# // synchronized(mainmenu.getVM()){
				//# mainmenu.getVM().callback("NotifyExit", new int[] {});
				//# // }
			//# } else {
				//# SanguoMIDlet.exit();
			//# }
//# 
		//# } else {
			//# mainmenu = VMGame.getVMGame("ui_mainmenu");
			//# if (mainmenu != null) {
				//# // synchronized(mainmenu.getVM()){
				//# mainmenu.getVM().callback("NotifyExitBack", new int[] {});
				//# // }
			//# }
			//# isExit = false;
		//# }
		//#endif

		lastFormSelection = cmd.getLabel();
		//#if (ModelID == Android || ModelID == Lenovo || ModelID == AndroidLarge || ModelID == LenovoU1 || ModelID == IPhone4 || ModelID == IPad || ModelID == AndroidSmall || ModelID == AndroidAuto) && (KeyCodeType == XperiaPlay)
		//# if(FormActivity.FORM_ACTIVITY != null){
		//# 	FormActivity.FORM_ACTIVITY.finish();
		//# }
		//#else
		GameMain.display.setCurrent(GameMain.instance);
		//#endif
		if (isBlock()) {
			continueProcess(TRUE);
		}
	}

    /**
     * 从指定数据库中读取文件内容。
     * @param dbName 文件存储的数据库名
     * @return 如果载入失败，返回null。
     */
    public static byte[] loadRMSFile(String dbName){
        return Tool.getData(dbName, (byte)0);
    }

    /**
     * 把文件内容保存到指定数据库中。
     * @return 如果保存失败，返回false。
     */
    public static boolean saveRMSFile(String dbName, byte[] data){
        return Tool.saveData(dbName, data, (byte)0);
    }

    private void deleteRMSFile(String dbName){
        Tool.deleteRMSFile(dbName);
    }
    
    private GWidget getGW(int gWidgetAdrr) {
    	int[] gWidget = (int[])this.followPointer(gWidgetAdrr);
    	if(gWidget != null) {
    		return VMGame.getGWidget(gWidget[GWidget.GW_VM_JAVA_GWIDGET]);
    	} else {
    		return null;
    	}
    	
    }
}
