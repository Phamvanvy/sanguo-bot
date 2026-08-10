package com.pip.ui;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;

import com.pip.common.Tool;
import com.pip.sanguo.GameMain;
import com.pip.util.SortHashtable;

public class DebugVM extends VM implements Runnable{
    public static final int COMMAND_INTERRUPT = 0;
    public static final int COMMAND_INFO = 1;
    public static final int COMMAND_QUERY = 2;
    public static final int COMMAND_MODECHANGE = 3;
    public static final int COMMAND_ADDBREAKPOINT = 4;
    public static final int COMMAND_DELBREAKPOINT = 5;
    public static final int COMMAND_REQUESTTRACE = 6;
    public static final int COMMAND_TRACE = 7;
    public static final int COMMAND_HEAP = 8;
    public static final int COMMAND_ALLOCTRACE = 9;
    public static final int COMMAND_SYNCSTATE = 10;
    public static final int COMMAND_STATE = 11;
    public static final int COMMAND_FUNC_REPORT = 15;
    
    private int execCounter = 0;   // 统计已执行指令条数 

    public static final int TOKEN = 0x12345678;

    public static final int MODE_RUN = 0;
    public static final int MODE_STEP = 1;
    public static final int MODE_STEPOVER = 2;
    public static final int MODE_STEPOUT = 3;

    private int debugMode = MODE_STEP;
    private int stepStartVM;
    private int stepStartFunc; // STEPOVER和STEPOUT模式下，记录启动时的函数ID
    private int stepStartStackBase; // STEPOVER和STEPOUT模式下，记录启动时的StackBase值

    //执行计数
    private static int execFuncCount = 0;
    private static int execFuncMaxCountSecond = 0;
    private static long execFuncMaxCountUpdateTime = System.currentTimeMillis();
    private static int statMillis = 3000;
    private static boolean printInstructionTable = false;
    private static Hashtable instructionTable = new Hashtable();
    private static Hashtable instructionMillTable = new Hashtable();
    private static long instructionMillTime = System.currentTimeMillis();
    private static long startTime = System.currentTimeMillis();
    private static Vector instructionList = new Vector();
    private static int instructionTick = -1;
    private static boolean printInstructionList = false;

    private static Hashtable instructionConst = new Hashtable();

    static{
        instructionConst.put(new Integer((byte) 0x01), "ADD");
        instructionConst.put(new Integer((byte) 0x02), "SUB");
        instructionConst.put(new Integer((byte) 0x03), "MUL");
        instructionConst.put(new Integer((byte) 0x04), "DIV");
        instructionConst.put(new Integer((byte) 0x05), "MOD");
        instructionConst.put(new Integer((byte) 0x06), "AND");
        instructionConst.put(new Integer((byte) 0x07), "OR");
        instructionConst.put(new Integer((byte) 0x08), "ANDB");
        instructionConst.put(new Integer((byte) 0x09), "ORB");
        instructionConst.put(new Integer((byte) 0x0A), "LSHIFT");
        instructionConst.put(new Integer((byte) 0x0B), "RSHIFT");
        instructionConst.put(new Integer((byte) 0x0C), "INCV");
        instructionConst.put(new Integer((byte) 0x0D), "ADDV8");
        instructionConst.put(new Integer((byte) 0x0E), "SUBV8");
        instructionConst.put(new Integer((byte) 0x11), "EQ");
        instructionConst.put(new Integer((byte) 0x12), "GT");
        instructionConst.put(new Integer((byte) 0x13), "LT");
        instructionConst.put(new Integer((byte) 0x14), "EQ8");
        instructionConst.put(new Integer((byte) 0x15), "GT8");
        instructionConst.put(new Integer((byte) 0x16), "LT8");
        instructionConst.put(new Integer((byte) 0x17), "NE8");
        instructionConst.put(new Integer((byte) 0x18), "INCVS");
        instructionConst.put(new Integer((byte) 0x19), "ADDV8S");
        instructionConst.put(new Integer((byte) 0x1A), "SUBV8S");
        instructionConst.put(new Integer((byte) 0x1B), "LOADVS");
        instructionConst.put(new Integer((byte) 0x1C), "SAVEVS");
        instructionConst.put(new Integer((byte) 0x1D), "DUP");
        instructionConst.put(new Integer((byte) 0x21), "JMP");
        instructionConst.put(new Integer((byte) 0x22), "JEQ");
        instructionConst.put(new Integer((byte) 0x23), "JNE");
        instructionConst.put(new Integer((byte) 0x24), "CALL");
        instructionConst.put(new Integer((byte) 0x25), "RET");
        instructionConst.put(new Integer((byte) 0x26), "VRET");
        instructionConst.put(new Integer((byte) 0x27), "SYSCALL");
        instructionConst.put(new Integer((byte) 0x28), "ALOAD8");
        instructionConst.put(new Integer((byte) 0x29), "ASAVE8");
        instructionConst.put(new Integer((byte) 0x2A), "STLOAD8");
        instructionConst.put(new Integer((byte) 0x2B), "STSAVE8");
        instructionConst.put(new Integer((byte) 0x2C), "TSWITCH");
        instructionConst.put(new Integer((byte) 0x2D), "LSWITCH");
        instructionConst.put(new Integer((byte) 0x2E), "CALLPTR");
        instructionConst.put(new Integer((byte) 0x31), "LOAD");
        instructionConst.put(new Integer((byte) 0x32), "SAVE");
        instructionConst.put(new Integer((byte) 0x33), "LOAD32");
        instructionConst.put(new Integer((byte) 0x34), "LOAD16");
        instructionConst.put(new Integer((byte) 0x35), "LOAD8");
        instructionConst.put(new Integer((byte) 0x36), "ALOAD");
        instructionConst.put(new Integer((byte) 0x37), "ASAVE");
        instructionConst.put(new Integer((byte) 0x38), "ALLOC");
        instructionConst.put(new Integer((byte) 0x39), "FREE");
        instructionConst.put(new Integer((byte) 0x3A), "STALLOC");
        instructionConst.put(new Integer((byte) 0x3B), "STLOAD");
        instructionConst.put(new Integer((byte) 0x3C), "STSAVE");
        instructionConst.put(new Integer((byte) 0x3D), "LOADV");
        instructionConst.put(new Integer((byte) 0x3E), "SAVEV");
        instructionConst.put(new Integer((byte) 0x3F), "LOADFUNC");
    }

    //动态堆大小
    private static int dynamicHeapSize = 0;

    private static class BreakPoint{
        int funcID;
        int start;
        int end;

        public boolean equals(Object o){
            if(o == null || !(o instanceof BreakPoint))
                return false;
            BreakPoint p = (BreakPoint) o;
            return (funcID == p.funcID) && (start == p.start) && (end == p.end);
        }
    }

    private Vector breakPoints = new Vector();
    private Hashtable allocTrace = new Hashtable();

    SocketConnection connection;
    DataOutputStream dos;
    DataInputStream dis;

    private int procCounter;
    private int syscallCounter;

    public DebugVM(VMGame vmGame){
        super(vmGame);
    }

    public void init(byte[] etfContent, byte[] etdContent) throws Exception{
        init(etfContent);
        allocTrace = new Hashtable();

        // 尝试连接调试服务器
        try{
            connection = (SocketConnection) Connector.open("socket://127.0.0.1:32167");
            dos = connection.openDataOutputStream();
            dis = connection.openDataInputStream();
            dos.writeInt(etfContent.length);
            dos.write(etfContent);
            dos.writeInt(etdContent.length);
            dos.write(etdContent);
            dis.readInt();
        }catch(Exception e){
        }
        new Thread(this).start();
    }

    protected int heapAlloc(){
        int ret = super.heapAlloc();
        allocTrace.put(new Integer(ret), getCurrentTrace());
        dynamicHeapSize++;

        return ret;
    }

    protected void heapFree(int addr){
        allocTrace.remove(new Integer(addr));
        if((addr & 0xFFF) < tempSpace){
            return;
        }

        // DebugVM：加上双重free检查
        int head = freeHead;
        do{
            if(head == addr){
                throw new RuntimeException("错误：试图重复free一个内存单元。");
            }
            head = freeSpaceList[head];
        }while(head != freeHead);
        dynamicHeap[addr] = null;
        short tmp = freeSpaceList[freeHead];
        freeSpaceList[freeHead] = (short) addr;
        freeSpaceList[addr] = tmp;

        dynamicHeapSize--;
    }

    public void run(){
        while(connection != null){
            try{
                int token = dis.readInt();
                if(token != TOKEN){
                    continue;
                }
                int cmdType = dis.readInt();
                if(cmdType == COMMAND_INTERRUPT){
                    int result = dis.readInt();
                    synchronized(this){
                        notifyAll();
                    }
                }else if(cmdType == COMMAND_QUERY){
                    int address = dis.readInt();
                    int value = memLoad(address);
                    String info = String.valueOf(value);
                    if((address & 0x40000000) != 0){
                        Object obj = followPointer(value);
                        info = printObject(obj);
                    }
                    synchronized(this){
                        dos.writeInt(TOKEN);
                        dos.writeInt(COMMAND_INFO);
                        dos.writeUTF(info);
                    }
                }else if(cmdType == COMMAND_MODECHANGE){
                    debugMode = dis.readInt();
                    if(debugMode == MODE_STEPOVER || debugMode == MODE_STEPOUT){
                        stepStartVM = currentVM;
                        stepStartFunc = currentFunc;
                        stepStartStackBase = stackBase;
                    }
                }else if(cmdType == COMMAND_ADDBREAKPOINT){
                    BreakPoint p = new BreakPoint();
                    p.funcID = dis.readInt();
                    int ff = p.funcID & 0xFFF;
                    int li = (p.funcID >> 12) & 0x0F;
                    p.start = dis.readInt() + libraries[li].functions[ff * 3 + 1];
                    p.end = dis.readInt() + libraries[li].functions[ff * 3 + 1];
                    if(breakPoints.indexOf(p) == -1){
                        breakPoints.addElement(p);
                    }
                }else if(cmdType == COMMAND_DELBREAKPOINT){
                    BreakPoint p = new BreakPoint();
                    p.funcID = dis.readInt();
                    int ff = p.funcID & 0xFFF;
                    int li = (p.funcID >> 12) & 0x0F;
                    p.start = dis.readInt() + libraries[li].functions[ff * 3 + 1];
                    p.end = dis.readInt() + libraries[li].functions[ff * 3 + 1];
                    breakPoints.removeElement(p);
                }else if(cmdType == COMMAND_REQUESTTRACE){
                    if(currentFunc == -1){
                        continue;
                    }
                    int[][] trace = getCurrentTrace();
                    synchronized(this){
                        dos.writeInt(TOKEN);
                        dos.writeInt(COMMAND_TRACE);
                        dos.writeInt(trace.length);
                        for(int i = 0; i < trace.length; i++){
                            dos.writeInt(trace[i][0]);
                            dos.writeInt(trace[i][1]);
                        }
                    }
                }else if(cmdType == COMMAND_HEAP){
                    boolean[] flag = new boolean[dynamicHeap.length];
                    for(int i = 0; i < flag.length; i++){
                        flag[i] = true;
                    }
                    int head = freeHead;
                    while(freeSpaceList[head] != freeHead){
                        head = freeSpaceList[head];
                        flag[head] = false;
                    }
                    dos.writeInt(TOKEN);
                    dos.writeInt(COMMAND_HEAP);
                    dos.writeInt(dynamicHeap.length);
                    for(int i = 0; i < dynamicHeap.length; i++){
                        dos.writeBoolean(flag[i]);
                        dos.writeUTF(printObject(dynamicHeap[i]));
                    }
                }else if(cmdType == COMMAND_ALLOCTRACE){
                    int addr = dis.readInt();
                    int[][] trace = (int[][]) allocTrace.get(new Integer(addr));
                    if(trace != null){
                        dos.writeInt(TOKEN);
                        dos.writeInt(COMMAND_ALLOCTRACE);
                        dos.writeInt(trace.length);
                        for(int i = 0; i < trace.length; i++){
                            dos.writeInt(trace[i][0]);
                            dos.writeInt(trace[i][1]);
                        }
                    }
                }else if(cmdType == COMMAND_SYNCSTATE){
                    dos.writeInt(TOKEN);
                    dos.writeInt(COMMAND_STATE);
                    writeState(dos);
                }
            }catch(IOException e){
                // 连接错误
                // e.printStackTrace();
                closeConnection();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    private void writeState(DataOutputStream dos) throws IOException{
        dos.writeInt(staticHeap.length);
        for(int i = 0; i < staticHeap.length; i++){
            dos.writeInt(staticHeap[i]);
        }
        dos.writeInt(stack.length);
        for(int i = 0; i < stack.length; i++){
            dos.writeInt(stack[i]);
        }
        dos.writeInt(esp);
        dos.writeInt(stackBase);

        // dump dynamic heap
        boolean[] flag = new boolean[dynamicHeap.length];
        for(int i = 0; i < flag.length; i++){
            flag[i] = true;
        }
        int head = freeHead;
        while(freeSpaceList[head] != freeHead){
            head = freeSpaceList[head];
            flag[head] = false;
        }
        dos.writeInt(dynamicHeap.length);
        for(int i = 0; i < dynamicHeap.length; i++){
            dos.writeBoolean(flag[i]);
            dumpObject(dos, dynamicHeap[i]);
        }
    }

    private void dumpObject(DataOutputStream dos, Object obj) throws IOException{
        // 动态堆，这里只有几种对象会被存储为原始值：boolean[], byte[], short[], int[], String,
        // String[]; 其他类型的对象都被转为String或者String[]存储，具体规则如下：
        // Hashtable: 用String[]存储，每个Item的值为 key=value
        // Vector: 用String[]存储
        // Object: 转换为String存储
        // Object[]: 转换为String[]存储
        if(obj == null){
            dos.writeByte(0xFF);
        }else if(obj instanceof boolean[]){
            boolean[] arr = (boolean[]) obj;
            dos.writeByte(1);
            dos.writeInt(arr.length);
            for(int i = 0; i < arr.length; i++){
                dos.writeBoolean(arr[i]);
            }
        }else if(obj instanceof byte[]){
            byte[] arr = (byte[]) obj;
            dos.writeByte(2);
            dos.writeInt(arr.length);
            dos.write(arr);
        }else if(obj instanceof short[]){
            short[] arr = (short[]) obj;
            dos.writeByte(3);
            dos.writeInt(arr.length);
            for(int i = 0; i < arr.length; i++){
                dos.writeShort(arr[i]);
            }
        }else if(obj instanceof int[]){
            int[] arr = (int[]) obj;
            dos.writeByte(4);
            dos.writeInt(arr.length);
            for(int i = 0; i < arr.length; i++){
                dos.writeInt(arr[i]);
            }
        }else if(obj instanceof String){
            dos.writeByte(5);
            dos.writeUTF((String) obj);
        }else if(obj instanceof String[]){
            String[] arr = (String[]) obj;
            dos.writeByte(6);
            dos.writeInt(arr.length);
            for(int i = 0; i < arr.length; i++){
                dumpObject(dos, arr[i]);
            }
        }else if(obj instanceof Hashtable){
            Hashtable table = (Hashtable) obj;
            dos.writeByte(7);
            dos.writeInt(table.size());
            Enumeration ee = ((Hashtable) obj).keys();
            while(ee.hasMoreElements()){
                Object key = ee.nextElement();
                Object value = ((Hashtable) obj).get(key);
                dumpObject(dos, key);
                dumpObject(dos, value);
            }
        }else if(obj instanceof SortHashtable){
            SortHashtable table = (SortHashtable) obj;
            dos.writeByte(7);
            dos.writeInt(table.size());
            Object[] keys = ((SortHashtable) obj).keys();
            Object[] values = ((SortHashtable) obj).values();
            int _count = keys.length;
            for(int i = 0; i < _count; i++){
                dumpObject(dos, keys[i]);
                dumpObject(dos, values[i]);
            }
        }else if(obj instanceof Vector){
            Vector v = (Vector) obj;
            dos.writeByte(8);
            dos.writeInt(v.size());
            for(int i = 0; i < v.size(); i++){
                dumpObject(dos, v.elementAt(i));
            }
        }else if(obj instanceof Object[]){
            Object[] arr = (Object[]) obj;
            dos.writeByte(9);
            dos.writeInt(arr.length);
            for(int i = 0; i < arr.length; i++){
                dumpObject(dos, arr[i]);
            }
        }else{
            dos.writeByte(10);
            dos.writeUTF(String.valueOf(obj));
        }
    }

    private int[][] getCurrentTrace(){
        Vector ret = new Vector();
        int thisVM = currentVM;
        int thisFunc = currentFunc;
        int thisEip = eip - libraries[thisVM].functions[thisFunc * 3 + 1];
        int thisStackBase = stackBase;
        ret.addElement(new int[]{
                        (thisVM << 12) + thisFunc, thisEip
        });
        for(int i = 0; i < callCount; i++){
            int parCount = libraries[thisVM].functions[thisFunc * 3] >> 16;
            int localParamCount = libraries[thisVM].functions[thisFunc * 3] & 0xFFFF;
            int pos = thisStackBase + parCount + localParamCount;
            thisStackBase = stack[pos];
            thisVM = stack[pos + 1];
            thisFunc = stack[pos + 2];
            thisEip = stack[pos + 3] - 4 - libraries[thisVM].functions[thisFunc * 3 + 1];
            ret.addElement(new int[]{
                            (thisVM << 12) + thisFunc, thisEip
            });
        }
        int[][] ret1 = new int[ret.size()][];
        for(int i = 0; i < ret1.length; i++){
            ret1[i] = (int[]) ret.elementAt(i);
        }
        return ret1;
    }

    private String printObject(Object o){
        if(o == null){
            return "null";
        }
        if(o instanceof boolean[]){
            return printBooleans((boolean[]) o);
        }else if(o instanceof byte[]){
            return printBytes((byte[]) o);
        }else if(o instanceof short[]){
            return printShorts((short[]) o);
        }else if(o instanceof int[]){
            return printInts((int[]) o);
        }else if(o instanceof String){
            return (String) o;
        }else if(o instanceof Integer){
            return o.toString();
        }else if(o instanceof java.util.Vector){
            java.util.Vector v = (java.util.Vector) o;
            Object[] arr = new Object[v.size()];
            v.copyInto(arr);
            return printObjects(arr);
        }else if(o instanceof Object[]){
            return printObjects((Object[]) o);
        }else if(o instanceof java.util.Hashtable){
            return printHashtable((Hashtable) o);
        }else{
            return o.getClass().getName() + ": " + o.toString();
        }
    }

    private String printBooleans(boolean[] arr){
        StringBuffer buf = new StringBuffer();
        buf.append("boolean[] {");
        for(int i = 0; i < arr.length; i++){
            if(i > 0){
                buf.append(", ");
            }
            buf.append(arr[i]);
        }
        buf.append(" }");
        return buf.toString();
    }

    private String printBytes(byte[] arr){
        StringBuffer buf = new StringBuffer();
        buf.append("byte[] {");
        for(int i = 0; i < arr.length; i++){
            if(i > 0){
                buf.append(", ");
            }
            buf.append("0x");
            buf.append(Integer.toHexString(arr[i] & 0xFF));
        }
        buf.append(" }");
        return buf.toString();
    }

    private String printShorts(short[] arr){
        StringBuffer buf = new StringBuffer();
        buf.append("short[] {");
        for(int i = 0; i < arr.length; i++){
            if(i > 0){
                buf.append(", ");
            }
            buf.append(arr[i]);
        }
        buf.append(" }");
        return buf.toString();
    }

    private String printInts(int[] arr){
        StringBuffer buf = new StringBuffer();
        buf.append("int[] {");
        for(int i = 0; i < arr.length; i++){
            if(i > 0){
                buf.append(", ");
            }
            buf.append(arr[i]);
        }
        buf.append(" }");
        return buf.toString();
    }

    private String printObjects(Object[] arr){
        StringBuffer buf = new StringBuffer();
        buf.append("Object[] {");
        for(int i = 0; i < arr.length; i++){
            if(i > 0){
                buf.append("\n");
            }
            buf.append(printObject(arr[i]));
        }
        buf.append("\n}");
        return buf.toString();
    }

    private String printHashtable(Hashtable t){
        Enumeration enum1 = t.keys();
        StringBuffer buf = new StringBuffer();
        buf.append("Hashtable[] {");
        int i = 0;
        while(enum1.hasMoreElements()){
            Object key = enum1.nextElement();
            Object value = t.get(key);
            if(i > 0){
                buf.append("\n");
            }
            buf.append(printObject(key));
            buf.append(" = ");
            buf.append(printObject(value));
            i++;
        }
        buf.append("\n}");
        return buf.toString();
    }

    private void closeConnection(){
        try{
            dis.close();
        }catch(Exception e){
        }
        dis = null;
        try{
            dos.close();
        }catch(Exception e){
        }
        dos = null;
        try{
            connection.close();
        }catch(Exception e){
        }
        connection = null;
    }

    public void destroy(){
        dynamicHeapSize -= allocTrace.size();

        super.destroy();
        breakPoints.removeAllElements();
        debugMode = MODE_STEP;
        closeConnection();
    }

    // 生成一个中断，发送到调试服务器
    protected void generateInterrupt(int code) throws Exception{
        if(dos != null){
            synchronized(this){
                dos.writeInt(TOKEN);
                dos.writeInt(COMMAND_INTERRUPT);
                dos.writeInt(code);
                dos.writeInt(eip - libraries[currentVM].functions[currentFunc * 3 + 1]);
                dos.writeInt((currentVM << 12) + currentFunc);
                wait();
            }
        }
    }
    
    protected void reportEnterFunc(int funcID) throws Exception {
        if(dos != null){
            synchronized(this){
                dos.writeInt(TOKEN);
                dos.writeInt(COMMAND_FUNC_REPORT);
                dos.writeBoolean(true);
                dos.writeInt(funcID);
                dos.writeInt(execCounter);
            }
        }
    }

    protected void reportExitFunc(int funcID) throws Exception {
        if(dos != null){
            synchronized(this){
                dos.writeInt(TOKEN);
                dos.writeInt(COMMAND_FUNC_REPORT);
                dos.writeBoolean(false);
                dos.writeInt(funcID);
                dos.writeInt(execCounter);
            }
        }
    }
    
    private boolean isBreakPoint(int funcID, int ip){
        for(int i = 0; i < breakPoints.size(); i++){
            BreakPoint p = (BreakPoint) breakPoints.elementAt(i);
            if(p.funcID == funcID && ip >= p.start && ip < p.end){
                return true;
            }
        }
        return false;
    }

    private int getHeapFree(){
        int heapFreeCount = 0;
        int temp = freeHead;
        while(freeSpaceList[temp] != freeHead){
            temp = freeSpaceList[temp];
            heapFreeCount++;
        }
        return heapFreeCount;
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
                e.printStackTrace();
                try{
                    generateInterrupt(7);
                }catch(Exception ee){
                }
                eip = libraries[currentVM].functions[funcBase + 1];
                int parCount = libraries[currentVM].functions[funcBase] >> 16;
                int localParamCount = libraries[currentVM].functions[funcBase] & 0xFFFF;
                esp = stackBase + parCount + localParamCount + 3 - 1;

                // 局部变量初始值设置为0
                for(int ii = 0; ii < localParamCount; ii++){
                    stack[esp - 3 - ii] = 0;
                }
            }
        }
    }

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

            boolean inted = false;
            if(!inted && (debugMode == MODE_STEP || isBreakPoint((currentVM << 12) + currentFunc, eip))){
                generateInterrupt(3);
                inted = true;
            }
            if(!inted && debugMode == MODE_STEPOVER){
                boolean needBreak = false;
                if(currentVM == stepStartVM && currentFunc == stepStartFunc && stackBase == stepStartStackBase){
                    needBreak = true;
                }else if(stackBase < stepStartStackBase){
                    needBreak = true;
                }
                if(needBreak){
                    generateInterrupt(4);
                    inted = true;
                }
            }
            if(!inted && debugMode == MODE_STEPOUT && stackBase < stepStartStackBase){
                generateInterrupt(4);
                inted = true;
            }
            byte inst = codeData[eip];
            
            statInstruction(inst);
            
            procCounter++;
            execCounter++;
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
                    if(ownerType == VM.OWNER_TYPE_QUEST){
                        Tool.sendSyncVMVarialbe(((Quest) owner).id, Tool.getInt(codeData, eip + 1), staticHeap[Tool.getInt(codeData, eip + 1)]);
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
                case CALLPTR: {
                    // 执行到这里时，参数应该都已经压栈了
                    int parCount = codeData[eip + 1] & 0xFF;
                    int callFunc;
                    if(inst == CALL){
                        callFunc = Tool.getShort(codeData, eip + 2) & 0xFFFF;
                    }else{
                        callFunc = stack[esp] & 0xFFFF;
                        esp--;
                    }
                    int callVM = 0;
                    if((callFunc & 0xF000) != 0){
                        // 如果从库中调用另外一个库，这里存储的是相对库本身的库索引，需要转换一下
                        // 如果是CALLPTR指令，那么传入的函数指针在LOADFUNC时已经转换过了
                        callVM = (callFunc & 0xF000) >> 12;
                        if(currentVM != 0 && inst == CALL){
                            VM nextVM = libraries[currentVM].libraries[callVM];
                            for(int i = 0; i < libraries.length; i++){
                                if(nextVM == libraries[i]){
                                    callVM = i;
                                    break;
                                }
                            }
                        }
                        functions = libraries[callVM].functions;
                        codeData = libraries[callVM].codeData;
                        callFunc &= 0x0FFF;
                    }else{
                        if(inst == CALL){
                            callVM = currentVM;
                        }else{
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
                    if(inst == CALL){
                        stack[esp + 4] = eip + 4;
                    }else{
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
                    reportEnterFunc(currentFunc | (currentVM << 12));
                    continue;
                }
                case RET: {
                    reportExitFunc(currentFunc | (currentVM << 12));
                    if(callCount == 0){
                        // 栈空说明是系统函数，直接退出本次执行
                        return;
                    }
                    
                    // 检查栈的情况
                    int parCount = functions[funcBase] >> 16;
                    int localParamCount = functions[funcBase] & 0xFFFF;
                    int rightesp = stackBase + parCount + localParamCount + 4 - 1;
                    if (esp != rightesp) {
                        throw new Exception("从函数返回时栈不为空，函数ID：" + currentFunc);
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
                    reportExitFunc(currentFunc | (currentVM << 12));
                    if(callCount == 0){
                        // 栈空说明是系统函数，直接退出本次执行
                        return;
                    }
                    
                    // 检查栈的情况
                    int parCount = functions[funcBase] >> 16;
                    int localParamCount = functions[funcBase] & 0xFFFF;
                    int rightesp = stackBase + parCount + localParamCount + 4;
                    if (esp != rightesp) {
                        throw new Exception("从函数返回时栈不为空，函数ID：" + currentFunc);
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
                    syscallCounter++;
                    short callFunc = Tool.getShort(codeData, eip + 1);
                    int parCount = codeData[eip + 3] & 0xFF;
                    boolean hasRet = codeData[eip + 4] == (byte) 1;
                    int[] params = new int[parCount];
                    System.arraycopy(stack, esp - parCount + 1, params, 0, parCount);
                    esp -= parCount;
                    int ret = syscall(callFunc, params);
                    if(hasRet){
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
                        Tool.sendSyncVMVarialbe(((Quest) owner).id, Tool.getInt(codeData, eip + 1), stack[esp]);
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
                    int[] arr = (int[]) followPointer(stack[esp - 1]);
                    stack[esp - 1] = arr[stack[esp] & 0x3FFFFFFF];
                    break;
                }
                case STSAVE: {
                    int[] arr = (int[]) followPointer(stack[esp - 1]);
                    int memberAddr = stack[esp];
                    int saveValue = stack[esp - 2];
                    arr[memberAddr & 0x3FFFFFFF] = saveValue;
                    break;
                }
                case STLOAD8: {
                    int[] arr = (int[]) followPointer(stack[esp]);
                    stack[esp] = arr[codeData[eip + 1]];
                    break;
                }
                case STSAVE8: {
                    int[] arr = (int[]) followPointer(stack[esp]);
                    int memberAddr = codeData[eip + 1];
                    int saveValue = stack[esp - 1];
                    arr[memberAddr & 0x3FFFFFFF] = saveValue;
                    break;
                }
                case LOADFUNC: {
                    short funcID = Tool.getShort(codeData, eip + 1);

                    // 如果从库中取的函数指针，这里高4位存储的是相对库本身的库索引，需要转换一下
                    if(currentVM != 0){
                        int callVM = (funcID & 0xF000) >> 12;
                        VM nextVM = libraries[currentVM].libraries[callVM];
                        for(int i = 0; i < libraries.length; i++){
                            if(nextVM == libraries[i]){
                                callVM = i;
                                break;
                            }
                        }
                        funcID = (short) ((funcID & 0xFFF) | (callVM << 12));
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
        execute(funcID, (int[]) null);
    }

    public synchronized void execute(int funcID, int[] params){
        if(running){
            return;
        }
        try{
            running = true;
            long startTime = System.currentTimeMillis();
            int oldCounter = procCounter;
            int oldCounter2 = syscallCounter;
            reportEnterFunc(funcID);
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
            reportExitFunc(funcID);

            execFuncCount += procCounter - oldCounter;
            //System.out.println("exec func " + funcID + ": " + (procCounter - oldCounter) + "/" + (syscallCounter - oldCounter2) + "/" + (System.currentTimeMillis() - startTime));
        }catch(Exception e){
            e.printStackTrace();
            try{
                generateInterrupt(7);
            }catch(Exception ee){
            }
            eip = libraries[currentVM].functions[funcBase + 1];
            int parCount = libraries[currentVM].functions[funcBase] >> 16;
            int localParamCount = libraries[currentVM].functions[funcBase] & 0xFFFF;
            esp = stackBase + parCount + localParamCount + 3 - 1;

            // 局部变量初始值设置为0
            for(int ii = 0; ii < localParamCount; ii++){
                stack[esp - 3 - ii] = 0;
            }
        }finally{
            running = false;
        }
    }

    public static String getExecCount(){
        int result = execFuncCount;
        execFuncCount = 0;

        if(execFuncMaxCountSecond < result){
            execFuncMaxCountSecond = result;
            execFuncMaxCountUpdateTime = System.currentTimeMillis();
        }else if(System.currentTimeMillis() - execFuncMaxCountUpdateTime > statMillis){
            execFuncMaxCountSecond = result;
            execFuncMaxCountUpdateTime = System.currentTimeMillis();
        }

        return result + " (" + execFuncMaxCountSecond + ")";
    }
    
    private static void statInstruction(int inst){
        if(printInstructionList){
            instructionList.addElement(new Integer(inst));
            
            if(instructionTick != GameMain.tick){
                instructionTick = GameMain.tick;
                printInstList();
            }
        }
        
        if(printInstructionTable){
            long now = System.currentTimeMillis();
    
            tableIncCount(instructionTable, inst);
            tableIncCount(instructionMillTable, inst);
            
            if(now - instructionMillTime > statMillis){
                printInstTable(instructionTable, "Whole Instruction Stat.", now - startTime);
                printInstTable(instructionMillTable, "Last" + statMillis + " mills Instruction Stat.", statMillis);
                
                instructionMillTime = now;
                instructionMillTable.clear();
            }
        }
    }
    
    private static void tableIncCount(Hashtable table, int inst){
        Integer key = new Integer(inst);
        Integer count = (Integer)table.get(new Integer(inst));
        
        if(count == null){
            table.put(key, new Integer(1));
        }else{
            table.put(key, new Integer(count.intValue() + 1));
        }
    }
    
    private static void printInstTable(Hashtable table, String title, long totalMills){
        System.out.println("###########");
        Enumeration enu = table.keys();
        int wholeCount = 0;
        
        while(enu.hasMoreElements()){
            Integer key = (Integer)enu.nextElement();
            Integer count = (Integer)table.get(key);
            wholeCount += count.intValue();
            
            System.out.println(instructionConst.get(key) + " : " + count.intValue());
        }
        
        System.out.println(title + " : " + wholeCount + "/" + totalMills + " , " + (long)wholeCount * GameMain.MILLIS_PRE_UPDATE / totalMills);
        System.out.println("###########");
    }
    
    private static void printInstList(){
        int count = instructionList.size();
        Hashtable statTable = new Hashtable();
        String[][] often = new String[8][];
        
        for(int i = 0; i < often.length; i++){
            often[i] = new String[i + 1];
        }
        
        if(count > 3000){
            for(int i = 0; i < count; i++){
                String inst = (String)instructionConst.get(instructionList.elementAt(i));
                often[0][0] = inst;
                
                for(int j = 1; j < often.length; j++){
                    String[] tmp = new String[often[j].length];
                    System.arraycopy(often[j], 0, tmp, 1, tmp.length - 1);
                    tmp[0] = inst;
                    often[j] = tmp;
                }
                
                for(int j = 1; j < often.length; j++){
                    String tmp = "";
                    boolean valued = true;
                    
                    for(int k = 0; k < often[j].length; k++){
                        if(often[j][k] == null){
                            valued = false;
                            
                            break;
                        }
                        
                        tmp += often[j][k] + ":";
                    }
                    
                    if(valued){
                        Integer oftenCount = (Integer)statTable.get(tmp);
                        
                        if(oftenCount == null){
                            statTable.put(tmp, new Integer(1));
                        }else{
                            statTable.put(tmp, new Integer(oftenCount.intValue() + 1));
                        }
                    }
                }
            }
            
            Enumeration enu = statTable.keys();
            Vector printList = new Vector();
            
            while(enu.hasMoreElements()){
                String key = (String)enu.nextElement();
                Integer oftenCount = (Integer)statTable.get(key);
                boolean inserted = false;
                Object[] p = new Object[]{
                                key, oftenCount
                };
                
                for(int i = 0; i < printList.size(); i++){
                    Object[] tmp = (Object[])printList.elementAt(i);
                    
                    if(((Integer)tmp[1]).intValue() < ((Integer)oftenCount).intValue()){
                        printList.insertElementAt(p, i);
                        inserted = true;
                        break;
                    }
                }
                
                if(!inserted){
                    printList.addElement(p);
                }
            }
            
            System.out.println("###########");
            
            for(int i = 0; i < printList.size(); i++){
                Object[] p = (Object[])printList.elementAt(i);
                
                if(((Integer)p[1]).intValue() > 100){
                    System.out.println("Often Stat : " + p[0] + " " + p[1]);
                }
            }
            
            System.out.println("###########");
        }
        
        instructionList.removeAllElements();
    }

    public static int getDynamicHeapSize(){
        return dynamicHeapSize;
    }
}