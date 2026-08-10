package com.pip.gtl.remotedebugger;


import java.io.*;
import java.awt.*;
import java.util.*;
import java.net.*;

import com.pip.gtl.decompiler.DecompileException;
import com.pip.gtl.decompiler.ETFDebugInfo;
import com.pip.gtl.decompiler.GTLDeCompiler;
import com.pip.gtl.etf.*;
import com.pip.util.Utils;

public class GTLDebugVMServerStub implements Runnable {
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
    public static final int COMMAND_INITALLOCTRACE = 12;
    public static final int COMMAND_ALLOCED = 13;
    public static final int COMMAND_FREED = 14;
    public static final int COMMAND_FUNC_REPORT = 15;
	
	public static final int TOKEN = 0x12345678;
	
    // 当前执行指针，表示函数内地址
    public int eip;
    // 当前执行的函数ID，高4位表示库索引，低12位表示函数ID
    public int currentFunc;
    // 静态堆，只能存储整型数据
    public int[] staticHeap;
    // 栈，只能存储整型数据
    public int[] stack;
    // 栈顶指针
    public int esp;
    // 当前函数调用的起始栈位置
    public int stackBase;
    // 动态堆，这里只有几种对象会被存储为原始值：boolean[], byte[], short[], int[], String,
    // String[]; 其他类型的对象都被转为String或者String[]存储，具体规则如下：
    // Hashtable: 用String[]存储，每个Item的值为 key=value
    // Vector: 用String[]存储
    // Object: 转换为String存储
    // Object[]: 转换为String[]存储
    public Object[] dynamicHeap;
    public boolean[] useFlag;

    // 对于版本2，动态堆修改为全局的，所以查询分配栈的接口也要修改。在每个内存单元分配和
    // 释放时都实时通知调试服务器并用文本来记录当时的调用栈情况。这样即使一个VMUI退出，它
    // 所分配的内存也能被追踪到。
    private static Hashtable<Integer, CallStackItem[]> allocTrace = new Hashtable<Integer, CallStackItem[]>();

    public IDebugVMListener listener;
    public ETFDebugInfo debugInfo;
    
    private Socket connection;
    private DataInputStream dis;
    private DataOutputStream dos;
    
    // 临时通讯文件，服务Flash客户端
    private boolean useFileWake;
    private boolean fileWakePaused;
    private boolean interrupted;
    private File wakeFile;
    
    private boolean modeChanged = false;

    public GTLDebugVMServerStub(IDebugVMListener l) {
        listener = l;
    }
    
    /**
     * 接受一个客户端VM的连接，从客户端读取执行文件和调试信息文件。
     */
    public void init(Socket sock) throws Exception {
    	try {
	    	this.eip = 0;
	    	this.currentFunc = -1;
	    	connection = sock;
	    	dis = new DataInputStream(sock.getInputStream());
	    	dos = new DataOutputStream(sock.getOutputStream());
	    	
	    	// 从客户端读取ETF和ETD文件内容
	    	byte[] etfContent = new byte[dis.readInt()];
	    	dis.readFully(etfContent);
	    	byte[] etdContent = new byte[dis.readInt()];
	    	dis.readFully(etdContent);
	    	
	    	// 把ETF和ETD保存到临时文件并载入
	    	String path = System.getProperty("user.home");
	    	File etfFile = new File(path, "_rdbtemp.etf");
	    	File etdFile = new File(path, "_rdbtemp.etd");
	    	saveFile(etfFile, etfContent);
	    	saveFile(etdFile, etdContent);
	    	debugInfo = new GTLDeCompiler().decompile(etfFile);
	    	etfFile.delete();
	    	etdFile.delete();
	    	
	    	// 检查是否需要启动临时文件唤醒机制
	    	byte[] b = new byte[4];
	    	System.arraycopy(etdContent, etdContent.length - 4, b, 0, 4);
	    	if (Arrays.equals(b, "pipe".getBytes())) {
	    		String name = dis.readUTF();
				useFileWake = true;
				fileWakePaused = true;
				wakeFile = new File(name);
				wakeFile.deleteOnExit();
	    	}
    	} catch (Exception e) {
    		throw e;
    	}
    }
    
    /** 启动读线程 */
    public void startThread() {
    	new Thread(this).start();
    }
    
    // 读取静态堆或者栈中的值，这个值可能是一个整数，也可能是一个指针。
    public int memLoad(int addr){
        if((addr & 0x80000000) == 0){
            // 静态堆地址
            return staticHeap[addr & 0x3FFFFFFF];
        }else{
            // 栈地址
            return stack[stackBase + (addr & 0x3FFFFFFF)];
        }
    }
    
    // 根据指针找到对象，指针可以引用动态堆中的对象、动态堆中数组元素、字符串表中的字符串
    public Object followPointer(int pointer){
        if(pointer == 0){
            return null;
        }
        if((pointer & 0x80000000) != 0){
            // 字符串表
            short libID = (short) ((pointer >> 16) & 0x7FFF);
            if (libID == 0) {
                // 引用执行文件的字符串表
                return debugInfo.stringTable[pointer & 0xFFFF];
            } else {
                // 引用库文件的字符串表
                GTLDebugSession sess = (GTLDebugSession)listener;
                for (int i = 0; i < debugInfo.libraries.length; i++) {
                    GTLDebugSession s = sess.debugManager.findSession(debugInfo.libraries[i]);
                    if (s == null) {
                        return "library " + debugInfo.libraries[i] + " not found";
                    }
                    if (s.getDebugInfo().taskID != libID) {
                        continue;
                    }
                    return s.getDebugInfo().stringTable[pointer & 0xFFFF];
                }
                return null;
            }
        } else {
            if (debugInfo.languageVersion < 2) {
                int dataType = (pointer >> 26) & 0x1F;
                if(dataType >= 4 && dataType <= 19){
                    // 这个类型段是普通对象和简单类型数组
                    return dynamicHeap[pointer & 0xFFF];
                }else if(dataType >= 20){
                    // 这个类型段是对象数组，检查这个指针是数组的指针还是数组内元素的指针
                    Object[] arr = (Object[])dynamicHeap[pointer & 0xFFF];
                    if((pointer & 0x02000000) != 0){
                        // 数组内元素的指针
                        return arr[(pointer >> 12) & 0x1FFF];
                    } else {
                        return arr;
                    }
                } else {
                    return null;
                }
            } else {
                // 版本1的VM修改了指针格式，从高到低依次是：是否字符串常量、是否对象数组、
                // 是否对象数组元素指针、13位偏移量（或数据类型）、16位地址
                boolean isElementPtr = (pointer & 0x20000000) != 0;
                if (isElementPtr) {
                    Object[] arr = (Object[])dynamicHeap[pointer & 0xFFFF];
                    return arr[(pointer >> 16) & 0x1FFF];
                } else {
                    return dynamicHeap[pointer & 0xFFFF];
                }
            }
        }
    }
    
    /**
     * 取得一个变量的值。如果这个变量是一个指针，则返回指针指向的对象，否则，返回变量值的字符串。
     */
    public Object getVariableValue(int address) {
		int value = memLoad(address);
        if ((address & 0x40000000) != 0) {
        	return followPointer(value);
        } else {
        	return String.valueOf(value);
        }
    }
    
    private void wakeByFile(byte[] data) throws Exception {
    	while (wakeFile.exists()) {
    		Thread.sleep(20);
    	}
    	if (data == null) {
    		new Exception().printStackTrace();
    		wakeFile.createNewFile();
    	} else {
    		new Exception().printStackTrace();
    		Utils.saveFileData(wakeFile, data);
    	}
    }
    
    public void notifyStart() throws Exception {
    	if (useFileWake) {
    		if (!interrupted) {
    			wakeByFile(null);
    			fileWakePaused = false;
    		}
    	} else {
	    	synchronized (this) {
				dos.writeInt(TOKEN + 1);
				dos.flush();
			}
    	}
    }

    public void query(int address) throws Exception {
    	if (useFileWake && fileWakePaused) {
    		ByteArrayOutputStream bos = new ByteArrayOutputStream();
    		DataOutputStream dos = new DataOutputStream(bos);
    		dos.writeInt(COMMAND_QUERY);
    		dos.writeInt(address);
    		dos.flush();
    		wakeByFile(bos.toByteArray());
    	} else {
	    	synchronized (this) {
				dos.writeInt(TOKEN);
				dos.writeInt(COMMAND_QUERY);
				dos.writeInt(address);
				dos.flush();
			}
    	}
    }
    
    public void syncState() throws Exception {
    	if (useFileWake && fileWakePaused) {
    		ByteArrayOutputStream bos = new ByteArrayOutputStream();
    		DataOutputStream dos = new DataOutputStream(bos);
    		dos.writeInt(COMMAND_SYNCSTATE);
    		dos.flush();
    		wakeByFile(bos.toByteArray());
    	} else {
	    	synchronized (this) {
				dos.writeInt(TOKEN);
				dos.writeInt(COMMAND_SYNCSTATE);
				dos.flush();
			}
    	}
    }
    
    public void changeDebugMode(int mode) throws Exception {
    	modeChanged = true;
    	if (useFileWake && fileWakePaused) {
    		ByteArrayOutputStream bos = new ByteArrayOutputStream();
    		DataOutputStream dos = new DataOutputStream(bos);
    		dos.writeInt(COMMAND_MODECHANGE);
    		dos.writeInt(mode);
    		dos.flush();
    		wakeByFile(bos.toByteArray());
    	} else {
	    	synchronized (this) {
	    		dos.writeInt(TOKEN);
	    		dos.writeInt(COMMAND_MODECHANGE);
	    		dos.writeInt(mode);
				dos.flush();
	    	}
    	}
    }
    
    public void addBreakPoint(int funcID, int start, int end) throws Exception  {
    	if (useFileWake && fileWakePaused) {
    		ByteArrayOutputStream bos = new ByteArrayOutputStream();
    		DataOutputStream dos = new DataOutputStream(bos);
    		dos.writeInt(COMMAND_ADDBREAKPOINT);
    		dos.writeInt(funcID);
    		dos.writeInt(start);
    		dos.writeInt(end);
    		dos.flush();
    		wakeByFile(bos.toByteArray());
    	} else {
	    	synchronized (this) {
	    		dos.writeInt(TOKEN);
	    		dos.writeInt(COMMAND_ADDBREAKPOINT);
	    		dos.writeInt(funcID);
	    		dos.writeInt(start);
	    		dos.writeInt(end);
				dos.flush();
	    	}
    	}
    }
    
    public void delBreakPoint(int funcID, int start, int end) throws Exception  {
    	if (useFileWake && fileWakePaused) {
    		ByteArrayOutputStream bos = new ByteArrayOutputStream();
    		DataOutputStream dos = new DataOutputStream(bos);
    		dos.writeInt(COMMAND_DELBREAKPOINT);
    		dos.writeInt(funcID);
    		dos.writeInt(start);
    		dos.writeInt(end);
    		dos.flush();
    		wakeByFile(bos.toByteArray());
    	} else {
	    	synchronized (this) {
	    		dos.writeInt(TOKEN);
	    		dos.writeInt(COMMAND_DELBREAKPOINT);
	    		dos.writeInt(funcID);
	    		dos.writeInt(start);
	    		dos.writeInt(end);
				dos.flush();
	    	}
    	}
    }
    
    public void requestTrace() throws Exception  {
    	if (useFileWake && fileWakePaused) {
    		ByteArrayOutputStream bos = new ByteArrayOutputStream();
    		DataOutputStream dos = new DataOutputStream(bos);
    		dos.writeInt(COMMAND_REQUESTTRACE);
    		dos.flush();
    		wakeByFile(bos.toByteArray());
    	} else {
	    	synchronized (this) {
	    		dos.writeInt(TOKEN);
	    		dos.writeInt(COMMAND_REQUESTTRACE);
				dos.flush();
	    	}
    	}
    }
    
    public void queryHeap() throws Exception {
    	if (useFileWake && fileWakePaused) {
    		ByteArrayOutputStream bos = new ByteArrayOutputStream();
    		DataOutputStream dos = new DataOutputStream(bos);
    		dos.writeInt(COMMAND_HEAP);
    		dos.flush();
    		wakeByFile(bos.toByteArray());
    	} else {
	    	synchronized (this) {
	    		dos.writeInt(TOKEN);
	    		dos.writeInt(COMMAND_HEAP);
				dos.flush();
	    	}
	    }
    }

    public void queryAllocTrace(int addr) throws Exception  {
        CallStackItem[] stack = allocTrace.get(addr);
        if (stack != null) {
            if (listener != null) {
                listener.handleAllocTrace(stack);
            }
            return;
        }
    	if (useFileWake && fileWakePaused) {
    		ByteArrayOutputStream bos = new ByteArrayOutputStream();
    		DataOutputStream dos = new DataOutputStream(bos);
    		dos.writeInt(COMMAND_ALLOCTRACE);
    		dos.writeInt(addr);
    		dos.flush();
    		wakeByFile(bos.toByteArray());
    	} else {
	    	synchronized (this) {
	    		dos.writeInt(TOKEN);
	    		dos.writeInt(COMMAND_ALLOCTRACE);
	    		dos.writeInt(addr);
				dos.flush();
	    	}
    	}
    }

    private class InterruptThread extends Thread {
    	int intNumber;
    	
    	public InterruptThread(int i) {
    		intNumber = i;
    	}
    	
    	public void run() {
    		try {
    			fileWakePaused = true;
    			interrupted = true;
	    		if (listener != null) {
					switch (intNumber) {
					case 3:
						listener.int3();
						break;
					case 4:
						listener.int4();
						break;
					case 5:
						listener.int5();
						break;
					case 7:
						listener.int7();
						break;
					}
				}
	    		interrupted = false;
	    		if (useFileWake) {
	    			wakeByFile(null);
	    			fileWakePaused = false;
	    		} else {
		    		synchronized (GTLDebugVMServerStub.this) {
						dos.writeInt(TOKEN);
						dos.writeInt(COMMAND_INTERRUPT);
						dos.writeInt(0);
						dos.flush();
					}
	    		}
    		} catch (Exception e) {
    		}
    	}
    }
    
    public void run() {
    	while (connection != null) {
    		try {
    			int token = dis.readInt();
    			if (token != TOKEN) {
    				continue;
    			}
    			int cmdType = dis.readInt();
    			if (cmdType == COMMAND_INTERRUPT) {
    				int intNum = dis.readInt();
    				eip = dis.readInt();
    				currentFunc = dis.readInt();
    				new InterruptThread(intNum).start();
    			} else if (cmdType == COMMAND_INFO) {
    				String info = dis.readUTF();
    				if (listener != null) {
    					listener.handleInformation(info);
    				}
    			} else if (cmdType == COMMAND_TRACE) {
    				int count = dis.readInt();
    				int[][] stack = new int[count][2];
    				for (int i = 0; i < count; i++) {
    					int funcID = dis.readInt();
    					int eip = dis.readInt();
    					stack[i][0] = funcID;
    					stack[i][1] = eip;
    				}
    				if (listener != null) {
    					listener.handleCallStack(stack);
    				}
    			} else if (cmdType == COMMAND_HEAP) {
    				int count = dis.readInt();
    				boolean[] flag = new boolean[count];
    				String[] ret = new String[count];
    				for (int i = 0; i < count; i++) {
    					flag[i] = dis.readBoolean();
    					ret[i] = dis.readUTF();
    				}
    				if (listener != null) {
    					listener.handleHeap(ret, flag);
    				}
    			} else if (cmdType == COMMAND_ALLOCTRACE) {
    				int count = dis.readInt();
    				int[][] stack = new int[count][2];
    				for (int i = 0; i < count; i++) {
    					int funcID = dis.readInt();
    					int eip = dis.readInt();
    					stack[i][0] = funcID;
    					stack[i][1] = eip;
    				}
    				if (listener != null) {
    					listener.handleAllocTrace(stack);
    				}
    			} else if (cmdType == COMMAND_STATE) {
    				readState(dis);
    				if (listener != null) {
    					listener.syncOver();
    				}
                } else if (cmdType == COMMAND_INITALLOCTRACE) {
                    allocTrace.clear();
                } else if (cmdType == COMMAND_ALLOCED) {
                    int addr = dis.readInt();
                    int count = dis.readInt();
                    CallStackItem[] stack = new CallStackItem[count];
                    for (int i = 0; i < count; i++) {
                        int funcID = dis.readInt();
                        int eip = dis.readInt();
                        if (listener != null) {
                            stack[i] = listener.getLineInfo(funcID, eip);
                        } else {
                            stack[i] = null;
                        }
                    }
                    allocTrace.put(addr, stack);
                } else if (cmdType == COMMAND_FREED) {
                    int addr = dis.readInt();
                    allocTrace.remove(addr);
    			} else if (cmdType == COMMAND_FUNC_REPORT) {
    			    boolean isEnter = dis.readBoolean();
    			    int funcID = dis.readInt();
    			    int execCounter = dis.readInt();
    			    if (listener != null) {
    			        listener.handleFuncReport(isEnter, funcID, execCounter);
    			    }
    			}
    		} catch (IOException e) {
    			// 连接错误
    			// e.printStackTrace();
    			if (listener != null) {
    				try {
    					listener.int6();
    				} catch (Exception e1) {
    				}
    			}
    		} catch (Throwable e) {
    			e.printStackTrace();
    		}
    	}
    }
    
    private void readState(DataInputStream dis) throws IOException {
    	staticHeap = new int[dis.readInt()];
    	for (int i = 0; i < staticHeap.length; i++) {
    		staticHeap[i] = dis.readInt();
    	}
    	stack = new int[dis.readInt()];
    	for (int i = 0; i < stack.length; i++) {
    		stack[i] = dis.readInt();
    	}
    	esp = dis.readInt();
    	stackBase = dis.readInt();
    	
    	// dump dynamic heap
    	dynamicHeap = new Object[dis.readInt()];
    	useFlag = new boolean[dynamicHeap.length];
    	for (int i = 0; i < dynamicHeap.length; i++) {
    		useFlag[i] = dis.readBoolean();
    		dynamicHeap[i] = readObject(dis);
    	}
    }
    
    private Object readObject(DataInputStream dos) throws IOException {
        // 动态堆，这里只有几种对象会被存储为原始值：boolean[], byte[], short[], int[], String,
        // String[]; 其他类型的对象都被转为String或者String[]存储，具体规则如下：
        // Hashtable: 用String[]存储，每个Item的值为 key=value
        // Vector: 用String[]存储
        // Object: 转换为String存储
        // Object[]: 转换为String[]存储
    	int type = dis.readByte() & 0xFF;
    	if (type == 0xFF) {
    		return null;
    	} else if (type == 1) {
    		boolean[] arr = new boolean[dis.readInt()];
    		for (int i = 0; i < arr.length; i++) {
    			arr[i] = dis.readBoolean();
    		}
    		return arr;
    	} else if (type == 2) {
    		byte[] arr = new byte[dis.readInt()];
    		dis.readFully(arr);
    		return arr;
    	} else if (type == 3) {
    		short[] arr = new short[dis.readInt()];
    		for (int i = 0; i < arr.length; i++) {
    			arr[i] = dis.readShort();
    		}
    		return arr;
    	} else if (type == 4) {
    		int[] arr = new int[dis.readInt()];
    		for (int i = 0; i < arr.length; i++) {
    			arr[i] = dis.readInt();
    		}
    		return arr;
    	} else if (type == 5) {
    		return dis.readUTF();
    	} else if (type == 6) {
    		String[] arr = new String[dis.readInt()];
    		for (int i = 0; i < arr.length; i++) {
    			arr[i] = (String)readObject(dis);
    		}
    		return arr;
    	} else if (type == 7) {
    		Object[] arr = new Object[dis.readInt() * 2];
    		for (int i = 0; i < arr.length; i++) {
    			arr[i] = readObject(dis);
    		}
    		return arr;
    	} else if (type == 8 || type == 9) {
    		Object[] arr = new Object[dis.readInt()];
    		for (int i = 0; i < arr.length; i++) {
    			arr[i] = readObject(dis);
    		}
    		return arr;
    	} else if (type == 10) {
    		return dis.readUTF();
    	} else {
    		return null;
    	}
    }
    
    public static void saveFile(File file, byte[] content) throws IOException {
    	FileOutputStream fos = new FileOutputStream(file);
    	fos.write(content);
    	fos.close();
    }

    /**
     * 关闭调试连接。
     */
    public void destroy() {
    	try {
    		if (dis != null) {
    			dis.close();
    		}
    	} catch (Exception e) {
    	}
    	dis = null;
    	try {
    		if (dos != null) {
    			dos.close();
    		}
    	} catch (Exception e) {
    	}
    	dos = null;
    	try {
    		if (connection != null) {
    			connection.close();
    		}
    	} catch (Exception e) {
    	}
    	connection = null;
    }
}
