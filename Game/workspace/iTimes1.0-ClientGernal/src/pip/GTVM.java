package pip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;

import pip.io.Network;
import pip.io.UWAPSegment;


/**
 * GTVM参考实现。客户端实现时可能需要重新定义结构以适应类合并的需求。
 * 一个GTVM可以同时载入多个任务运行。所有的任务共享一个运行栈；每个任务有独立的状态机内存；
 * 每个线索有独立的运行指针。
 * 初始化一个GTVM的过程如下：
 *     GTVM gtvm = new GTVM();
 *     gtvm.init(saveData);
 *     gtvm.addTask(etf1);
 *     gtvm.addTask(etf2);
 *     gtvm.start();
 *     while (!quit) {
 *         Thread.sleep(50);
 *         gtvm.run();
 *     }
 *     saveData = gtvm.save();
 */
public class GTVM implements GTVMConstants, Runnable{
    private static final Integer TRUE = new Integer(1);
    private static final Integer FALSE = new Integer(0);

    // 全局栈
    private Object[] stack = new Object[32];
    // 全局栈指针
    private int esp = -1;
    // 当前函数调用的起始栈位置
    private int espSave = -1;
    // 状态机内存。数组中每个对象是一个byte数组，表示每个任务的状态机内存。
    private Object[] statMem;
    // 状态机字符串。数组中每个对象都是一个String数组，表示每个任务的状态机字符串存储。
    private Object[] statStr;
    // 运行指针。数组中每个对象是一个short数组，表示每个任务的每个线索的运行指针。
    private Object[] eip;

    // 任务名字
    private Object[] taskName;

    // 任务ID。对应于每个任务的ID。
    private short[] taskID;
    // 字符串表。数组中每个对象是一个String数组，表示每个任务的字符串表。
    private Object[] stringTable;
    // 线索属性。数组中每个对象是一个int数组，表示每个任务的每个线索的属性。
    private Object[] threadAttr;
    // 线索代码。数组中每个对象是一个byte[]数组的数组，表示每个任务的每个线索的代码。
    private Object[] threadCode;

    // 初始化数据。初始化数据时上次GTVM保存的虚拟机状态。这块内存在GTVM开始执行后就可以释
    // 放了。
    private byte[] saveData;

    // 当前正在执行的任务的下标
    private int activeTask;
    // 当前正在执行的任务的状态内存
    private byte[] activeStatMem;
    // 当前正在执行的任务的状态字符串
    private String[] activeStatStr;
    // 当前正在执行的任务的执行指针
    private short[] activeEip;
    // 当前正在执行的任务的字符串表
    private String[] activeStringTable;

    private Random rand;

    public static short localTaskIdStart = 30000;
    public static short serverPushTaskIdStart = 31000;

    private static Hashtable endTaskWaitingTable = new Hashtable();
    private boolean currentTaskPause = false;
    public static boolean endTaskProcessed = false;

    public GTVM(){
    }

    /**
     * 初始化虚拟机。参数saveData是上次存盘时保存状态，null表示没有。
     */
    public void init(byte[] saveData){
        this.saveData = saveData;
        esp = -1;
        statMem = new Object[0];
        statStr = new Object[0];
        eip = new Object[0];
        taskID = new short[0];
        taskName = new String[0];
        stringTable = new Object[0];
        threadAttr = new Object[0];
        threadCode = new Object[0];
        rand = new Random(System.currentTimeMillis());
    }

    // 扩展一个数组（长度加1），留出第一个空间。
    private Object[] realloc(Object[] arr){
        Object[] ret = new Object[arr.length + 1];
        System.arraycopy(arr, 0, ret, 1, arr.length);

        return ret;
    }

    // 删除数组中的一个记录。
    private Object[] removeAlloc(Object[] arr, int index){
        for(int i = index; i < arr.length - 1; i++){
            arr[i] = arr[i + 1];
        }

        Object[] ret = new Object[arr.length - 1];
        System.arraycopy(arr, 0, ret, 0, arr.length - 1);

        return ret;
    }

    /**
     * 从字节流中得到一个short值。
     */
    public static short getShort(byte[] data, int off){
        return (short)(((data[off] & 0xFF) << 8) | (data[off + 1] & 0xFF));
    }

    /**
     * 保存一个short值到字节流中。
     */
    public static void setShort(byte[] data, int off, short value){
        data[off] = (byte)((value >> 8) & 0xFF);
        data[off + 1] = (byte)(value & 0xFF);
    }

    /**
     * 从字节流中得到一个int值。
     */
    public static int getInt(byte[] data, int off){
        return ((data[off] & 0xFF) << 24) | ((data[off + 1] & 0xFF) << 16) | ((data[off + 2] & 0xFF) << 8) | (data[off + 3] & 0xFF);
    }

    /**
     * 保存一个int值到字节流中。
     */
    public static void setInt(byte[] data, int off, int value){
        data[off] = (byte)((value >> 24) & 0xFF);
        data[off + 1] = (byte)((value >> 16) & 0xFF);
        data[off + 2] = (byte)((value >> 8) & 0xFF);
        data[off + 3] = (byte)(value & 0xFF);
    }

    /**
     * 向虚拟机中添加任务。如果是在初始化阶段，则添加任务的同时恢复上次存盘时保存的状态。
     */
    public void addTask(ETFFile etf){
        boolean isLocal = false;

        if(etf.taskID >= localTaskIdStart/* && etf.taskID < serverPushTaskIdStart*/){
            isLocal = true;
        }

        short[] removeID = new short[taskID.length];
        short removeCount = 0;

        if(isLocal){
            for(int i = 0; i < taskID.length; i++){
                if(taskID[i] > localTaskIdStart && taskID[i] < serverPushTaskIdStart){
                    removeID[removeCount] = taskID[i];
                    removeCount++;
                }
            }

            Vector removeEvents = new Vector();

            for(int i = 0; i < removeCount; i++){
                for(int j = 0; j < World.events.size(); j++){
                    GameEvent e = (GameEvent)World.events.elementAt(i);

                    if(e.isTaskUIEvent && e.getEventTaskId() != etf.taskID){
                        removeEvents.addElement(e);
                        GameEvent.eventRemove(e);

                        break;
                    }
                }

                removeTask(removeID[i], false);
            }

            for(int i = 0; i < removeEvents.size(); i++){
                World.events.removeElement(removeEvents.elementAt(i));
            }
        }

        // 分配空间以保存所有任务数据
        statMem = realloc(statMem);
        statStr = realloc(statStr);
        eip = realloc(eip);
        short[] newArr = new short[taskID.length + 1];
        System.arraycopy(taskID, 0, newArr, 1, taskID.length);
        taskID = newArr;
        stringTable = realloc(stringTable);
        threadAttr = realloc(threadAttr);
        threadCode = realloc(threadCode);

        taskName = realloc(taskName);
        taskName[0] = etf.taskName;

        // 把所有任务的数据加入数组中
        statMem[0] = new byte[etf.stateMemSize];
        statStr[0] = new String[etf.stateStrCount];

        for(int i = 0; i < etf.stateStrCount; i++){
            ((String[])statStr[0])[i] = "";
        }

        eip[0] = new short[etf.threadAttr.length];
        taskID[0] = etf.taskID;
        stringTable[0] = etf.stringTable;
        threadAttr[0] = etf.threadAttr;
        threadCode[0] = etf.threadCode;

        World.addTaskAsUnFinish(etf.taskID, etf.taskName, false);

        // 根据用户ID查找保存的数据里是否有这个任务的状态
        if(saveData == null){
            return;
        }

        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(saveData));

        try{
            while(true){
                short tid = dis.readShort();

                if(tid == etf.taskID){ // ID匹配
                    // 拷贝状态内存
                    int statLen = dis.readShort() & 0xFFFF;

                    if(statLen == etf.stateMemSize){
                        dis.readFully((byte[])statMem[0]);
                    }else{
                        dis.skipBytes(statLen);
                    }

                    // 拷贝字符串
                    int strCount = dis.readShort() & 0xFFFF;
                    int strLen = dis.readShort() & 0xFFFF;

                    if(strCount == etf.stateStrCount){
                        for(int i = 0; i < strCount; i++){
                            ((String[])statStr[0])[i] = dis.readUTF();
                        }
                    }else{
                        dis.skipBytes(strLen);
                    }

                    // 拷贝EIP
                    int eipLen = dis.readByte() & 0xFF;

                    if(eipLen == etf.threadAttr.length){
                        short[] taskEip = (short[])eip[0];

                        for(int i = 0; i < eipLen; i++){
                            taskEip[i] = dis.readShort();
                        }
                    }

                    break;
                }else{
                    int statLen = dis.readShort() & 0xFFFF;
                    dis.skipBytes(statLen);
                    dis.skipBytes(2);
                    int strLen = dis.readShort() & 0xFFFF;
                    dis.skipBytes(strLen);
                    int eipLen = dis.readByte() & 0xFF;
                    dis.skipBytes(eipLen * 2);
                }
            }
        }catch(Exception e){
        }
    }

    /**
     * 从虚拟机中移除任务。
     */
    public void removeTask(short taskId, boolean reset){
        endTaskWaitingTable.remove(new Integer(taskId));

        int taskIndex = -1;

        for(int i = 0; i < taskID.length; i++){
            if(taskId == taskID[i]){
                taskIndex = i;

                break;
            }
        }

        if(taskIndex < 0){
            return;
        }

        if(reset){
            short[] resetEip = (short[])eip[taskIndex];

            for(int i = 0; i < resetEip.length; i++){
                resetEip[i] = 0;
            }
        }else{
            statMem = removeAlloc(statMem, taskIndex);
            statStr = removeAlloc(statStr, taskIndex);
            eip = removeAlloc(eip, taskIndex);

            for(int i = taskIndex; i < taskID.length - 1; i++){
                taskID[i] = taskID[i + 1];
            }

            short[] newArr = new short[taskID.length - 1];
            System.arraycopy(taskID, 0, newArr, 0, taskID.length - 1);
            taskID = newArr;

            stringTable = removeAlloc(stringTable, taskIndex);
            threadAttr = removeAlloc(threadAttr, taskIndex);
            threadCode = removeAlloc(threadCode, taskIndex);
            taskName = removeAlloc(taskName, taskIndex);
        }

        System.gc();
    }

    /**
     * 清除虚拟机的所有任务
     */
    public void release(){
        for(int taskIndex = 0; taskIndex < taskID.length; taskIndex++){
            statMem = removeAlloc(statMem, taskIndex);
            statStr = removeAlloc(statStr, taskIndex);
            eip = removeAlloc(eip, taskIndex);

            for(int i = taskIndex; i < taskID.length - 1; i++){
                taskID[i] = taskID[i + 1];
            }

            short[] newArr = new short[taskID.length - 1];
            System.arraycopy(taskID, 0, newArr, 0, taskID.length - 1);
            taskID = newArr;

            stringTable = removeAlloc(stringTable, taskIndex);
            threadAttr = removeAlloc(threadAttr, taskIndex);
            threadCode = removeAlloc(threadCode, taskIndex);
            taskName = removeAlloc(taskName, taskIndex);
        }
    }

    /**
     * 启动虚拟机。这个函数只是设置一个状态，而真正的执行是通过每个时间片调用step开始的。
     */
    public void start(){
        saveData = null;
    }

    // 读取状态值
    private Object readStat(int addr, int offset){
        int type = (addr >> 13) & 0x07;
        int raddr = addr & 0x1FFF;

        if(type == 4){
            return activeStatStr[raddr + offset];
        }

        if(type == 0){ // boolean
            int off1 = raddr + offset;
            byte b = activeStatMem[off1 >> 3];

            return new Integer((b >> (7 - off1 & 0x07)) & 0x01);
        }else if(type == 1){ // byte
            return new Integer(activeStatMem[(raddr) + offset] & 0xFF);
        }else if(type == 2){ // short
            return new Integer(getShort(activeStatMem, (raddr) + (offset << 1)) & 0xFFFF);
        }else{ // int
            return new Integer(getInt(activeStatMem, (raddr) + (offset << 2)));
        }
    }

    // 写入状态值
    private void writeStat(int addr, int offset, Object value){
        int type = (addr >> 13) & 0x07;
        int raddr = addr & 0x1FFF;

        if(type == 4){
            activeStatStr[raddr + offset] = (String)value;

            return;
        }

        int ivalue = ((Integer)value).intValue();

        if(type == 0){ // boolean
            int off1 = raddr + offset;
            int mask = 1 << (7 - off1 & 0x07);
            off1 >>= 3;
            byte b = activeStatMem[off1];

            if(ivalue == 0){
                activeStatMem[off1] = (byte)((b & ~mask) & 0xFF);
            }else{
                activeStatMem[off1] = (byte)((b | mask) & 0xFF);
            }
        }else if(type == 1){ // byte
            activeStatMem[(raddr) + offset] = (byte)ivalue;
        }else if(type == 2){ // short
            setShort(activeStatMem, (raddr) + (offset << 1), (short)ivalue);
        }else{ // int
            setInt(activeStatMem, (raddr) + (offset << 2), ivalue);
        }
    }

    /**
     * 向前执行一个时间片。这个方法执行到所有线索都放弃了执行权后返回。如果没有任何指令可以
     * 执行，此方法返回false。
     */
    public boolean step(boolean teamMode){
        // 循环执行每个任务
        boolean ret = false;

        for(activeTask = 0; activeTask < threadCode.length; activeTask++){
            int[] threadAttrs = (int[])threadAttr[activeTask];
            Object[] threads = (Object[])threadCode[activeTask];
            activeStatMem = (byte[])statMem[activeTask];
            activeStatStr = (String[])statStr[activeTask];
            activeEip = (short[])eip[activeTask];
            activeStringTable = (String[])stringTable[activeTask];
            int tmax = threads.length;

            // 执行每个线索
            for(int tid = 0; tid < tmax; tid++){
                // 检查task是否因为EndTask被暂停了，如果是则跳过此task
                if(endTaskWaitingTable.containsKey(new Integer(taskID[activeTask]))){
                    continue;
                }

                if(teamMode && taskID[activeTask] < serverPushTaskIdStart){
                    continue;
                }

                currentTaskPause = false;

                // 检查线索是否场景线索，如果是，检查当前是否指定场景
                int ttype = (threadAttrs[tid] >> 16) & 0x03;

                if(ttype == 1){
                    if((short)(threadAttrs[tid] & 0xFFFF) != getMapID()){
                        continue;
                    }
                }else if(ttype == 2){
                    continue;
                }

                // 重置堆栈，开始逐条运行指令
                esp = -1;
                byte[] code = (byte[])threads[tid];
                short teip = activeEip[tid];
                int eipmax = code.length;

                while(teip < eipmax){
                    byte inst = code[teip];
                    ret = true;

                    // 如果遇到PAUSE语句，则放弃时间片
                    if(inst == PSE){
                        teip++;

                        break;
                    }

                    teip++;

                    switch(inst){
                        case ADD:
                            if(stack[esp - 1] instanceof String || stack[esp] instanceof String){
                                stack[esp - 1] = stack[esp - 1].toString() + stack[esp].toString();
                            }else{
                                stack[esp - 1] = new Integer(((Integer)stack[esp - 1]).intValue() + ((Integer)stack[esp]).intValue());
                            }

                            break;
                        case SUB:
                            stack[esp - 1] = new Integer(((Integer)stack[esp - 1]).intValue() - ((Integer)stack[esp]).intValue());

                            break;
                        case MUL:
                            stack[esp - 1] = new Integer(((Integer)stack[esp - 1]).intValue() * ((Integer)stack[esp]).intValue());

                            break;
                        case DIV:
                            stack[esp - 1] = new Integer(((Integer)stack[esp - 1]).intValue() / ((Integer)stack[esp]).intValue());

                            break;
                        case MOD:
                            stack[esp - 1] = new Integer(((Integer)stack[esp - 1]).intValue() % ((Integer)stack[esp]).intValue());

                            break;
                        case AND:
                            stack[esp - 1] = (((Integer)stack[esp - 1]).intValue() != 0 && ((Integer)stack[esp]).intValue() != 0)? TRUE: FALSE;

                            break;
                        case OR:
                            stack[esp - 1] = (((Integer)stack[esp - 1]).intValue() != 0 || ((Integer)stack[esp]).intValue() != 0)? TRUE: FALSE;

                            break;
                        case ANDB:
                            stack[esp - 1] = new Integer(((Integer)stack[esp - 1]).intValue() & ((Integer)stack[esp]).intValue());

                            break;
                        case EQ:
                            stack[esp - 1] = stack[esp - 1].equals(stack[esp])? TRUE: FALSE;

                            break;
                        case GT:
                            stack[esp - 1] = (((Integer)stack[esp - 1]).intValue() > ((Integer)stack[esp]).intValue())? TRUE: FALSE;

                            break;
                        case LT:
                            stack[esp - 1] = (((Integer)stack[esp - 1]).intValue() < ((Integer)stack[esp]).intValue())? TRUE: FALSE;

                            break;
                        case JMP:
                            // 这里需要减去3，因为switch后面会把teip加上指令参数长度
                            teip = (short)(getShort(code, teip) - 2);

                            break;
                        case JEQ:
                            if(!stack[esp].equals(FALSE)){
                                // 这里需要减去3，因为switch后面会把teip加上指令参数长度
                                teip = (short)(getShort(code, teip) - 2);
                            }

                            break;
                        case JNE:
                            if(stack[esp].equals(FALSE)){
                                // 这里需要减去3，因为switch后面会把teip加上指令参数长度
                                teip = (short)(getShort(code, teip) - 2);
                            }

                            break;
                        case CALL:
                            // 把当前ESP和EIP压栈，切换到指定函数执行
                            byte parCount = code[teip];
                            byte callFunc = code[teip + 1];
                            stack[esp + 1] = new Integer(espSave);
                            espSave = esp - parCount;
                            stack[esp + 2] = new Integer((tid << 16) + teip + 2);
                            esp += 2;
                            tid = callFunc;
                            code = (byte[])threads[tid];
                            teip = 0;
                            eipmax = code.length;

                            continue;
                        case RET:
                            // 恢复ESP和EIP，此时EIP应该在栈顶，ESP跟随
                            tid = ((Integer)stack[esp]).intValue() >> 16;
                            code = (byte[])threads[tid];
                            teip = (short)(((Integer)stack[esp]).intValue() & 0xFFFF);
                            eipmax = code.length;
                            int temp = espSave;
                            espSave = ((Integer)stack[esp - 1]).intValue();
                            esp = temp;

                            continue;
                        case VRET:
                            // 恢复ESP和EIP，并拷贝返回值，此时返回值在栈顶，EIP和ESP跟随
                            Object retValue = stack[esp];
                            tid = ((Integer)stack[esp - 1]).intValue() >> 16;
                            code = (byte[])threads[tid];
                            teip = (short)(((Integer)stack[esp - 1]).intValue() & 0xFFFF);
                            eipmax = code.length;
                            temp = espSave;
                            espSave = ((Integer)stack[esp - 1]).intValue();
                            esp = temp;
                            stack[++esp] = retValue;

                            continue;
                        case LOAD:
                            stack[esp] = readStat(((Integer)stack[esp]).intValue(), 0);

                            break;
                        case SAVE:
                            writeStat(((Integer)stack[esp]).intValue(), 0, stack[esp - 1]);

                            break;
                        case LOAD8:
                            stack[esp + 1] = new Integer((int)code[teip]);

                            break;
                        case LOAD16:
                            stack[esp + 1] = new Integer((int)getShort(code, teip));

                            break;
                        case LOAD32:
                            stack[esp + 1] = new Integer(getInt(code, teip));

                            break;
                        case LOADS:
                            stack[esp + 1] = activeStringTable[code[teip] & 0xFF];

                            break;
                        case LOADPARA:
                            stack[esp + 1] = stack[espSave + code[teip] + 1];

                            break;
                        case SAVEPARA:
                            stack[espSave + code[teip] + 1] = stack[esp];

                            break;
                        case ALOAD:
                            stack[esp - 1] = readStat(((Integer)stack[esp]).intValue(), ((Integer)stack[esp - 1]).intValue());

                            break;
                        case ASAVE:
                            writeStat(((Integer)stack[esp]).intValue(), ((Integer)stack[esp - 1]).intValue(), stack[esp - 2]);

                            break;
                        case C_MAPID:
                            stack[esp + 1] = new Integer(getMapID());

                            break;
                        case C_GETUNIT:
                            stack[esp] = isUnitExists(((Integer)stack[esp]).intValue())? TRUE: FALSE;

                            break;
                        case C_GETUSER:
                            stack[esp] = isUserExists((String)stack[esp])? TRUE: FALSE;

                            break;
                        case C_KEY:
                            stack[esp + 1] = isKeyPressed(code[teip])? TRUE: FALSE;

                            break;
                        case C_NANYKEY:
                            stack[esp + 1] = noKeyPressed()? TRUE: FALSE;

                            break;
                        case C_TUNIT:
                            stack[esp] = touchUnit(((Integer)stack[esp]).intValue())? TRUE: FALSE;

                            break;
                        case C_NENEMY:
                            stack[esp + 1] = new Integer(getLastEnemy());

                            break;
                        case C_POS:
                            stack[esp + 1] = atPos(code[teip], code[teip + 1])? TRUE: FALSE;

                            break;
                        case C_ATTRI:
                            stack[esp] = new Integer(getAttrI(code[teip], code[teip + 1], ((Integer)stack[esp]).intValue()));

                            break;
                        case C_ATTRS:
                            stack[esp] = getAttrS(code[teip], code[teip + 1], ((Integer)stack[esp]).intValue());

                            break;
                        case C_NEARPOS:
                            stack[esp + 1] = near(code[teip], code[teip + 1])? TRUE: FALSE;

                            break;
                        case C_ANSWER:
                            stack[esp + 1] = new Integer(getLastAnswer());

                            break;
                        case C_HASTASK:
                            stack[esp] = hasTask(((Integer)stack[esp]).intValue())? TRUE: FALSE;

                            break;
                        case C_INPUT:
                            stack[esp + 1] = getLastInput();

                            break;
                        case C_RANDOM:
                            stack[esp + 1] = new Integer(random());

                            break;
                        case C_GETTIME:
                            stack[esp + 1] = new Integer(getTime());

                            break;
                        case C_BATTLE:
                            stack[esp + 1] = new Integer(getBattleResult());

                            break;
                        case C_HASTASKITEM:
                            stack[esp - 2] = hasTaskItem(code[teip], ((Integer)stack[esp - 2]).intValue(), (String)stack[esp - 1], ((Integer)stack[esp]).intValue())? TRUE: FALSE;

                            break;
                        case C_TASKFINISHED:
                            stack[esp] = taskFinished(((Integer)stack[esp]).intValue())? TRUE: FALSE;

                            break;
                        case C_BILLING:
                            stack[esp + 1] = new Integer(getBillingResult())/*? TRUE: FALSE;*/;

                            break;
                        case C_INT:
                            try{
                                stack[esp] = Integer.valueOf((String)stack[esp]);
                            }catch(Exception e){
                                stack[esp] = new Integer(0);
                                //#debug
                                e.printStackTrace();
                            }

                            break;
                        case D_CHAT:
                            chat(((Integer)stack[esp - 2]).intValue(), (String)stack[esp - 1], ((Integer)stack[esp]).intValue());

                            break;
                        case D_MESSAGE:
                            message((String)stack[esp], code[teip]);

                            break;
                        case D_MOVESCREEN:
                            moveScreen(code[teip], code[teip + 1]);

                            break;
                        case D_REMOVEUNIT:
                            removeUnit(((Integer)stack[esp]).intValue());

                            break;
                        case D_GOTOMAP:
                            gotoMap((short)((Integer)stack[esp - 2]).intValue(), (short)((Integer)stack[esp - 1]).intValue(), (short)((Integer)stack[esp]).intValue());

                            break;
                        case D_SETATTR:
                            setAttr(code[teip], code[teip + 1], ((Integer)stack[esp - 1]).intValue(), (String)stack[esp]);

                            break;
                        case D_MOVETOUNIT:
                            moveToUnit(((Integer)stack[esp]).intValue());

                            break;
                        case D_PLAYSOUND:
                            playSound((byte)code[teip]);

                            break;
                        case D_VIBRA:
                            vibra();

                            break;
                        case D_ASKQ:
                            question((String)stack[esp - 1], ((Integer)stack[esp]).intValue());

                            break;
                        case D_ASSIGNTASK:
                            assignTask(((Integer)stack[esp - 1]).shortValue(), ((Integer)stack[esp]).byteValue());

                            break;
                        case D_ENDTASK:
                            endTask(taskID[activeTask], (short)((Integer)stack[esp - 1]).intValue(), (short)((Integer)stack[esp]).intValue());

                            break;
                        case D_FLASH:
                            flash();

                            break;
                        case D_BATTLE:
                            battle(((Integer)stack[esp]).intValue());

                            break;
                        case D_ADDTASKITEM:
                            addTaskItem((String)stack[esp]);

                            break;
                        case D_REMOVETASKITEM:
                            removeTaskItem((String)stack[esp - 1], ((Integer)stack[esp]).intValue());

                            break;
                        case D_BILLING:
                            billing((String)stack[esp - 2], (String)stack[esp - 1], ((Integer)stack[esp]).intValue());

                            break;
                        case D_LOGOUT:
                            logout((String)stack[esp]);

                            break;
                        case D_INPUT:
                            getInput((String)stack[esp], code[teip], code[teip + 1]);

                            break;
                        case D_POPUPLIST:
                            popupList((String)stack[esp - 1], (String)stack[esp]);

                            break;
                        case D_SENDCMD:
                            sendCmd((String)stack[esp]);

                            break;
                        case C_GETTILE:
                            stack[esp - 1] = new Integer(getTile(((Integer)stack[esp - 1]).intValue(), ((Integer)stack[esp]).intValue()));

                            break;
                        case D_SENDRQST:
                            sendRequst(code[teip], code[teip + 1], (String)stack[esp]);

                            break;
                        case D_SETNPCHINT:
                            setNpcHint(((Integer)stack[esp]).intValue(), code[teip]);

                            break;
                    }

                    esp += STACK_EFFECT[inst & 0xFF];
                    teip += INSTRUCTION_LENGTH[inst & 0xFF] - 1;

                    if(currentTaskPause){
                        break;
                    }
                }

                activeEip[tid] = teip;
            }
        }

        return ret;
    }

    /**
     * 保存所有任务的状态，返回打包后的数据。
     */
    public byte[] save(){
        try{
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);

            for(int i = 0; i < taskID.length; i++){
                if(taskID[i] >= localTaskIdStart){
                    continue;
                }

                // Task ID
                dos.writeShort(taskID[i]);

                // 状态内存
                byte[] stats = (byte[])statMem[i];
                dos.writeShort(stats.length);
                dos.write(stats);

                // 状态字符串
                String[] strs = (String[])statStr[i];
                dos.writeShort(strs.length);
                ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
                DataOutputStream dos2 = new DataOutputStream(bos2);

                for(int j = 0; j < strs.length; j++){
                    dos2.writeUTF(strs[j]);
                }

                dos.writeShort(bos2.size());
                dos.write(bos2.toByteArray());

                // EIP
                short[] eips = (short[])eip[i];
                dos.writeByte(eips.length);

                for(int j = 0; j < eips.length; j++){
                    dos.writeShort(eips[j]);
                }
            }

            dos.close();

            return bos.toByteArray();
        }catch(IOException e){
            // Should never touch this
            return null;
        }
    }

    int getTile(int x, int y){
        return -1;
    }

    void setTile(int x, int y, int v){
        return;
    }

    short getMapID(){
        return World.currMapId;
    }

    boolean isUnitExists(int unitId){
        return false;
    }

    boolean isUserExists(String userName){
        return false;
    }

    boolean isKeyPressed(byte keyCode){
        return World.isKeyPressedVM(keyCode, true);
    }

    boolean noKeyPressed(){
        return !World.isAnyKeyPressed();
    }

    boolean touchUnit(int unitId){
        return World.touchNpc(unitId);
    }

    int getLastEnemy(){
        return -1;
    }

    boolean atPos(byte x, byte y){
        return false;
    }

    int getAttrI(byte type, byte subType, int index){
        return GameState.taskGetAttrI(type, subType, index);
    }

    String getAttrS(byte type, byte subType, int index){
        return GameState.taskGetAttrS(type, subType, index);
    }

    boolean near(byte x, byte y){
        return false;
    }

    int getLastAnswer(){
        return World.lastAnswer;
    }

    boolean hasTask(int taskId){
        return hasTask(taskId, false);
    }

    boolean hasTask(int taskId, boolean ignoreUnFinished){
        boolean result = false;

        for(int i = 0; i < taskID.length; i++){
            if(taskId == taskID[i]){
                result = true;

                break;
            }
        }

        if(!result && !ignoreUnFinished){
            Enumeration taskEnum = World.unFinishedTask.keys();

            while(taskEnum.hasMoreElements()){
                int tid = ((Integer)taskEnum.nextElement()).intValue();
                if(tid == taskId){
                    result = true;
                    break;
                }
            }

        }

        return result;
    }

    String getLastInput(){
        return World.lastInput;
    }

    int random(){
        return Math.abs(rand.nextInt());
    }

    int getTime(){
        long off = (System.currentTimeMillis() - GameState.lastSyncTime) / 1000L;
        if(off > 0 && off < 120){
            return (int)(GameState.serverTime + off);
        }else{
            return (int)(GameState.serverTime);
        }
    }
    
    int getDay(){
    	return (int)GameState.serverTime%604800000;
    }

    int getBattleResult(){
        return World.instance.getBattleResult();
    }

    boolean hasTaskItem(byte type, int itemId, String taskName, int count){
        GameItem item = null;

        switch(type){
            case GameItem.TYPE_BASIC:
                item = new GameItem(GameItem.TYPE_BASIC);
                item.itemId = itemId;
                item.count = (short)count;

                break;
            case GameItem.TYPE_TASK:
                item = new GameItem(GameItem.TYPE_TASK);
                item.name = taskName;
                item.count = (short)count;

                break;
            case GameItem.TYPE_EXTEND:
                item = new GameItem(GameItem.TYPE_EXTEND);
                item.itemId = itemId;
                item.count = (short)count;

                break;
            case GameItem.TYPE_EQUIP:
                item = new GameItem(GameItem.TYPE_EQUIP);
                item.itemId = itemId;
                item.count = (short)count;

                break;
        }
        //return true;
        return World.player.hasItem(item) != null;
    }

    boolean taskFinished(int taskId){
        Integer tmp = new Integer(taskId);

        if(World.finishedTask.contains(tmp)){
            return true;
        }else{
            return false;
        }
    }

    int lastBillingResult = 0;

    int getBillingResult(){
        int ret = lastBillingResult;
        lastBillingResult = 0;
        return ret;
    }

    void chat(int npcId, String msg, int block){
        World.showChat(npcId, msg, block);
    }

    void message(String msg, byte timeout){
        if(timeout == -1){
            //#debug
            System.out.println("VM debug: " + msg);
        }else{
            World.showMessage(msg, timeout);
        }
    }

    void moveScreen(byte x, byte y){
    }

    void removeUnit(int unitId){
    }

    void gotoMap(short mapId, short x, short y){
        if(mapId == -1 && x == -1 && y == -1){
            World.gotoMap((short)((World.areaId << 4) | (World._defaultMapId & 0xF)), World._defaultX, World._defaultY, true);
        }else{
            World.gotoMap(mapId, x, y, true);
        }

        World.setGameState(null);
    }

    void setAttr(byte type, byte index, int valueI, String valueS){
        GameState.taskSetAttr(type, index, valueI, valueS);
    }

    void moveToUnit(int unitId){
    }

    void playSound(byte soundId){
    }

    void vibra(){
    }

    void question(String msg, int count){
        World.showQuestion(msg, count);
    }

    void assignTask(short taskId, byte showConfirm){
        if(showConfirm == 1){
            World.showTaskConfirm(taskId);
        }else{
            World.addTaskAsUnFinish(taskId, null, true);
            World.requestDownloadTask(taskId);
        }
    }

    void endTask(short taskId, int returnValue, short otherTaskId){
        short endTaskId = taskId;

        if(otherTaskId > 0){
            endTaskId = otherTaskId;
        }

        if(endTaskId > 0 && endTaskId < localTaskIdStart){
            Integer tmpTaskId = new Integer(endTaskId);
            //#debug
            System.out.println("End task : " + taskId + " , " + returnValue);

            int endTaskSerial = World.sendRequest(GameState.CONN_TASK_COMPLETED, new Object[]{
                            new Short(endTaskId), new Short((short)returnValue)
            }, false);

            //World.requestEndTask(endTaskId, (short)returnValue);
            Integer tmpSerial = new Integer(endTaskSerial);
            endTaskWaitingTable.put(tmpTaskId, tmpSerial);
            //currentTaskPause = true;
            endTaskProcessed = true;
        }else if(endTaskId >= localTaskIdStart){
            removeTask(endTaskId, false);
        }
    }

    public void endTaskResult(int taskId, int serial, boolean allEnd){
        Integer tmpTaskId = null;
        Integer tmpSerial = null;
        boolean flag = false;

        if(taskId < 0){
            Enumeration emu = endTaskWaitingTable.keys();

            while(emu.hasMoreElements()){
                tmpTaskId = (Integer)emu.nextElement();
                tmpSerial = (Integer)endTaskWaitingTable.get(tmpTaskId);

                if(tmpSerial != null && tmpSerial.intValue() == serial){
                    flag = true;

                    break;
                }
            }
            
            Vector notAssignedTask = new Vector();
            
            emu = World.unFinishedTask.keys();

            while(emu.hasMoreElements()){
                Integer tmpAssignId = (Integer)emu.nextElement();
                String tmpDesc = (String)World.unFinishedTask.get(tmpAssignId);

                if(tmpDesc != null && tmpDesc.trim().length() > 0){
                    continue;
                }else{
                    notAssignedTask.addElement(tmpAssignId);
                }
            }
            
            for(int i = 0; i < notAssignedTask.size(); i++){
                World.unFinishedTask.remove(notAssignedTask.elementAt(i));
            }
        }else{
            tmpTaskId = new Integer(taskId);
            tmpSerial = (Integer)endTaskWaitingTable.get(tmpTaskId);

            if(tmpSerial != null && tmpSerial.intValue() == serial){
                flag = true;
            }
        }

        if(flag){
            endTaskWaitingTable.remove(tmpTaskId);

            if(allEnd){
                World.finishedTask.put(tmpTaskId, tmpTaskId);
                World.unFinishedTask.remove(tmpTaskId);

                removeTask((short)tmpTaskId.intValue(), false);
            }
        }
    }

    void flash(){
    }

    void battle(int unitId){
        for(int i = 0; i < World.monsters.length; i++){
            if(World.monsters[i].id == unitId){
                try{
                    World.instance.startBattle(i);
                }catch(IOException e){
                    //#debug
                    e.printStackTrace();
                }
            }
        }
    }

    void addTaskItem(String name){
    }

    void removeTaskItem(String name, int count){
        Vector items = World.player.taskItems;
        GameItem tmp = null;
        boolean flag = false;

        for(int i = 0; i < items.size(); i++){
            tmp = (GameItem)items.elementAt(i);

            if(tmp.type == GameItem.TYPE_TASK && tmp.name.equals(name)){
                flag = true;

                break;
            }
        }

        if(flag){
            tmp.count -= count;

            if(tmp.count < 0){
                items.removeElement(tmp);
            }
        }
    }

    public static int expect(String source, String search, int from, StringBuffer buf){
        int pos = source.indexOf(search, from);
        if(pos == -1){
            if(buf != null){
                buf.append(source.substring(from));
            }
            return -1;
        }else{
            if(buf != null){
                buf.append(source.substring(from, pos));
            }
            return pos;
        }
    }

    public static String expect2(String source, String before, String after){
        int pos = expect(source, before, 0, null);
        if(pos == -1){
            return null;
        }
        pos += before.length();
        StringBuffer buf = new StringBuffer();
        pos = expect(source, after, pos, buf);
        if(pos == -1){
            return null;
        }
        return buf.toString();
    }

    public static String normalizeURL(String url){
        // 把URL里的&amp;替换为&
        StringBuffer buf = new StringBuffer();
        int pos = 0;
        do{
            pos = expect(url, "&amp;", pos, buf);
            if(pos == -1){
                break;
            }
            buf.append("&");
            pos += 5;
        }while(true);
        return buf.toString();
    }

    private String billingNum, billingMsg;
    private int billingID;

    public void run(){
        //#if Revision == QQ
        try{
            if(billingNum.startsWith("qqsms")){
                String[] tmp = GameState.splitTaskString(4, billingMsg);
                int areaId = Integer.parseInt(tmp[0]);
                int goodId = Integer.parseInt(tmp[1]);
                int gameId = Integer.parseInt(tmp[2]);
                int channelId = Integer.parseInt(tmp[3]);
                
                iTimesMIDlet.smsBuyGood(World.instance, areaId, goodId, gameId, channelId);
            }else if(billingNum.startsWith("qqszf")){
                String[] tmp = GameState.splitTaskString(3, billingMsg);
                byte cpId = (byte)Integer.parseInt(tmp[0]);
                int goodId = Integer.parseInt(tmp[1]);
                String linkId = tmp[2];
                //#if (Directory == SE-K300) || (Directory == SE-K500) || (Directory == SE-K700)
                // K300,K500,K700三款机型内存无法承载新功能
                //# iTimesMIDlet.shenZhouFu(World.instance, cpId, goodId, linkId);
                //#else
                if (linkId.startsWith("a")) {
                	GameState.enterShenZhouFu();
                } else {
                	iTimesMIDlet.shenZhouFu(World.instance, cpId, goodId, linkId);
                }
                //#endif
            }else{
                String[] tmp = GameState.splitTaskString(3, billingMsg);
                String linkId = tmp[0];
                int goodId = Integer.parseInt(tmp[1]);
                int count = Integer.parseInt(tmp[2]);
                
                iTimesMIDlet.buyQQGood(World.instance, linkId, goodId, count);
            }
            
            lastBillingResult = 1;
        }catch(Exception e){
            lastBillingResult = 2;
        }
        //#else
        try{
            if(billingNum.startsWith("sms:")){
                // 发送短信
                MessageConnection connection = null;
                connection = (MessageConnection)Connector.open(billingNum);
                TextMessage message = (TextMessage)connection.newMessage(MessageConnection.TEXT_MESSAGE);
                message.setAddress(billingNum);
                message.setPayloadText(billingMsg);
                connection.send(message);
            }

            lastBillingResult = 1;

            // 向服务器发送计费成功标志
            UWAPSegment segment = new UWAPSegment(GameState.CONN_BILLING_OK);
            segment.writeInt(billingID);
            segment.flush();
            GameState.connection.writeSegment(segment);
        }catch(Exception e){
            lastBillingResult = 2;
        }
        //#endif
    }

    void billing(final String num, final String msg, final int id){
//#if Directory == NK-NGage
    	// NGage版本不能发送短信
    	//# if (num.startsWith("sms://")) {
    		//# World.showMessage("对不起，您的手机不支持此功能。", (byte)10);
    		//# return;
    	//# }
//#endif
        lastBillingResult = 0;
        billingNum = num;
        billingMsg = msg;
        billingID = id;
        new Thread(this).start();
    }
    
    void logout(String msg){
        GameState.exitToGameMenu(msg, false);
    }

    void getInput(String msg, byte type, byte allowEmpty){
        World.getFormInput(msg, type, allowEmpty == 0? false: true);
    }

    void popupList(String options, String cmds){
    }

    void sendCmd(String command){
    	//#if Revision == CMCC || (Revision == JIANGSUNCMCC) 
    	if("cmccPoint".equals(command)){
            GameState.cmccPoint = new CMCCPointCanvas();
            World.RecordPreousDisplay(GameState.cmccPoint);
        }else
        //#endif
        {
            World.sendRequest(GameState.CONN_COMMAND, new Object[]{
                command
            }, false);
        }
    }

    void sendRequst(byte type, byte count, String valueS){
        GameState.taskSendRequst(type, count, valueS);
    }

    void setNpcHint(int npcId, byte type){
        World.setNpcHint(npcId, type);
    }
}