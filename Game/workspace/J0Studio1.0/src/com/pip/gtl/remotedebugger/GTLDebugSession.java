package com.pip.gtl.remotedebugger;

import javax.swing.*;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.net.*;

import com.pip.gtl.compiler.GTLCompiler;
import com.pip.gtl.decompiler.*;
import com.pip.gtl.preprocess.GTLPreProcessor;
import com.pip.gtl.remotedebugger.ui.*;
import com.pip.gtl.etf.*;
import com.pip.j0ide.Settings;

import javax.swing.event.*;

public class GTLDebugSession implements IDebugVMListener {
    private ETFDebugInfo debugInfo;
    private GTLDebugVMServerStub vm;

    // 调试状态
    private int vmstatus;
    /** 没有开始 */
    public static final int DEBUG_INIT = 0;
    /** 正在运行 */
    public static final int DEBUG_RUNNING = 1;
    /** 中断在代码运行中 */
    public static final int DEBUG_BREAKED = 2;

    // 执行模式
    private int debugMode = MODE_RUN;
    public static final int MODE_RUN = 0;
    public static final int MODE_STEP = 1;
    public static final int MODE_STEPOVER = 2;
    public static final int MODE_STEPOUT = 3;
    
    protected GTLDebugManager debugManager;
    
    // 行号快速查找表
    private File[] sourceFiles;
    private HashMap<File, Integer> fileIndex;
    private HashMap<Integer, Integer> mapSourceToReal;
    private HashMap<Integer, Integer> mapRealToSource;
    
    // 结构继承关系表
    private HashMap<String, HashMap<Integer, String>> structHierachy;
    
    // 当前调用栈
    private CallStackItem[] callStack;
    // 变量表
    private VariableItem[] variables;
    // 当前维护的函数调用序列
    private Vector<int[]> callSequence = new Vector<int[]>();
    // 函数调用表
    public ConcurrentHashMap<String, HashMap<String, int[]>> funcStatistic = new ConcurrentHashMap<String, HashMap<String, int[]>>();

    public GTLDebugSession(Socket socket, GTLDebugManager mgr) throws Exception {
		debugManager = mgr;
        vmstatus = DEBUG_INIT;
		debugMode = MODE_STEP;
        vm = new GTLDebugVMServerStub(this);
        vm.init(socket);
        debugInfo = vm.debugInfo;
        linkDebugFile();
		buildLineMapping();
		buildStructHierachy();
		callStack = new CallStackItem[0];
		variables = new VariableItem[0];
		setVMStatus(DEBUG_RUNNING);
    }
    
    private void linkDebugFile() {
        String file = debugInfo.mainFile;
        if (new File(file).exists()) {
            return;
        }
        file = file.replace('\\', '/');
        int pos = file.indexOf("/gtl/");
        if (pos == -1) {
            return;
        }
        File f = new File(Settings.workingDir, file.substring(pos + 1));
        if (f.exists()) {
           debugInfo.mainFile = f.getAbsolutePath();
        }
    }
    
    public void startThread() {
    	vm.startThread();
    }
    
    private void buildStructHierachy() {
    	structHierachy = new HashMap<String, HashMap<Integer, String>>();
    	Object[] arr = debugInfo.structDefs.values().toArray();
    	for (int i = 0; i < arr.length; i++) {
    		ETFDebugInfo.StructDef st = (ETFDebugInfo.StructDef)arr[i];
    		if (st.parentName.length() > 0) {
    			ETFDebugInfo.StructDef parentSt = (ETFDebugInfo.StructDef)debugInfo.structDefs.get(st.parentName);
    			if (parentSt == null) {
    				continue;
    			}
    			HashMap<Integer, String> cmap = structHierachy.get(parentSt.name);
    			if (cmap == null) {
    				cmap = new HashMap<Integer, String>();
    				structHierachy.put(parentSt.name, cmap);
    			}
    			cmap.put(new Integer(st.typeID), st.name);
    		}
    	}
    }
    
    /**
     * 查找一个结构的运行时类型信息。
     * @param ancestor 父结构定义
     * @param typeID 类型ID
     * @return 如果找到符合条件的继承结构，返回找到的类型信息，否则返回本身。
     */
    public ETFDebugInfo.StructDef getRuntimeType(ETFDebugInfo.StructDef ancestor, int typeID) {
    	// 如果自己的类型ID就符合，直接返回
    	if (ancestor.typeID == typeID) {
    		return ancestor;
    	}
    	
    	HashMap<Integer, String> cmap = structHierachy.get(ancestor.name);
    	
    	// 如果没有继承，直接返回
    	if (cmap == null) {
    		return ancestor;
    	}
    	
    	// 在直接继承表中查找
    	Integer key = new Integer(typeID);
    	String name = cmap.get(key);
    	if (name != null) {
    		return (ETFDebugInfo.StructDef)debugInfo.structDefs.get(name);
    	}
    	
    	// 继续向下遍历继承树
    	Object[] arr = cmap.values().toArray();
    	for (int i = 0; i < arr.length; i++) {
    		ETFDebugInfo.StructDef st = (ETFDebugInfo.StructDef)debugInfo.structDefs.get(arr[i]);
    		if (st != null) {
    			ETFDebugInfo.StructDef st2 = getRuntimeType(st, typeID);
    			if (st2 != null && st2 != st) {
    				return st2;
    			}
    		}
    	}
    	return ancestor;
    }
    
    private void buildLineMapping() {
    	// 创建文件表
    	sourceFiles = new File[debugInfo.referFiles.length];
    	fileIndex = new HashMap<File, Integer>();
    	for (int i = 0; i < debugInfo.referFiles.length; i++) {
    		sourceFiles[i] = resolveFile(debugInfo.mainFile, debugInfo.referFiles[i]);
    		fileIndex.put(sourceFiles[i], i);
    	}
    	
    	// 创建行号对应表
    	mapSourceToReal = new HashMap<Integer, Integer>();
    	mapRealToSource = new HashMap<Integer, Integer>();
    	for (int i = 0; i < debugInfo.lineMapping.length; i++) {
    		File realFile = sourceFiles[debugInfo.lineMapping[i][0]];
    		int realFileIndex = fileIndex.get(realFile);
    		int realLine = debugInfo.lineMapping[i][1];
    		int sourceLine = i;
    		realLine |= realFileIndex << 16;
    		mapSourceToReal.put(sourceLine, realLine);
    		mapRealToSource.put(realLine, sourceLine);
    	}
    }
    
    private File resolveFile(String from, String to) {
    	try {
    		return GTLPreProcessor.resolveFile(new File(from), to);
    	} catch (Exception e) {
    		return new File(to);
    	}
    }
    
    public File getFileOfLine(int lineNum) {
    	try {
    		int ll = mapSourceToReal.get(lineNum);
    		return sourceFiles[ll >> 16];
    	} catch (NullPointerException ne) {
    		return null;
    	}
    }
    
    public int getLineOfLine(int lineNum) {
    	try {
    		int ll = mapSourceToReal.get(lineNum);
    		return ll & 0xFFFF;
    	} catch (NullPointerException ne) {
    		return -1;
    	}
    }
    
    public int getLineAt(File file, int lineNum) {
    	try {
    		int findex = fileIndex.get(file);
    		int temp = (findex << 16) | lineNum;
    		return mapRealToSource.get(temp);
    	} catch (NullPointerException ne) {
    		return -1;
    	}
    }
    
    public GTLDebugVMServerStub getVM() {
        return vm;
    }

    public int getVMStatus() {
        return vmstatus;
    }

    public void setVMStatus(int stat) {
        vmstatus = stat;
        callStack = new CallStackItem[0];
        if (vmstatus == DEBUG_BREAKED) {
        	queryCallStack();
        	syncState();
        }
        debugManager.sessionStatusChanged(this, vmstatus);
    }

    public ETFDebugInfo getDebugInfo() {
    	return debugInfo;
    }
    
    public int[] getBreakpointInfoAt(int lineNum) {
        int status = 0;    // 0 - 未找到，1 - 已找到开头，2 - 已找到结尾
        int funcID = 0, start = 0, end = 0;
        for (int i = 0; i < debugInfo.lineNumTable.length; i++) {
            int[] lineNums = (int[])debugInfo.lineNumTable[i];
            for (int j = 0; j < lineNums.length; j++) {
                if (status == 0) {
                    if (lineNums[j] == lineNum) {
                        funcID = i;
                        start = j;
                        status = 1;
                    }
                } else if (status == 1) {
                    if (lineNums[j] != lineNum) {
                        end = j;
                        status = 2;
                        break;
                    }
                }
            }
            if (status == 1) {
                end = lineNums.length;
                status = 2;
            }
            if (status == 2) {
                break;
            }
        }
        if (status != 2) {
            return null;
        } else {
            return new int[] { funcID, start, end };
        }
    }
    
    public void notifyBreakPointChanged(int lineNum, boolean add) {
        int[] info = getBreakpointInfoAt(lineNum);
        notifyBreakPointChanged(info, add);
    }
    
    public void notifyBreakPointChanged(int[] info, boolean add) {
        if (info != null) {
            try {
                if (add) {
                    vm.addBreakPoint(info[0], info[1], info[2]);
                } else {
                    vm.delBreakPoint(info[0], info[1], info[2]);
                }
            } catch (Exception e) {
            }
        }
    }
    
    public void query(int addr) {
    	try {
    		vm.query(addr);
    	} catch (Exception e) {
    	}
    }
    
    public void queryCallStack() {
    	try {
    		vm.requestTrace();
    	} catch (Exception e) {
    	}
    }

    public void syncState() {
    	try {
    		vm.syncState();
    	} catch (Exception e) {
    	}
    }

    public void queryAllocTrace(int addr) {
    	try {
    		vm.queryAllocTrace(addr);
    	} catch (Exception e) {
    	}
    }
    
    public void queryHeap() {
    	try {
    		vm.queryHeap();
    	} catch (Exception e) {
    	}
    }

    public void close() {
    	if (vm != null) {
    		vm.destroy();
    		vm = null;
    		setVMStatus(DEBUG_INIT);
    	}
    }
    
    public void start() {
    	if (vm != null) {
    		try {
            	vm.notifyStart();
            } catch (Exception e) {
            }
    	}
    }

    public void go() {
        if (vm != null) {
            debugMode = MODE_RUN;
            try {
            	vm.changeDebugMode(MODE_RUN);
                synchronized(vm) {
                    vm.notifyAll();
                }
            } catch (Exception e) {
            }
            setVMStatus(DEBUG_RUNNING);
        }
    }

    public void step() {
        if (vm != null) {
            debugMode = MODE_STEP;
            try {
            	vm.changeDebugMode(MODE_STEP);
                synchronized(vm) {
                    vm.notifyAll();
                }
            } catch (Exception e) {
            }
            setVMStatus(DEBUG_RUNNING);
        }
    }

    public void stepOver() {
        if (vm != null) {
            debugMode = MODE_STEPOVER;
            try {
            	vm.changeDebugMode(MODE_STEPOVER);
                synchronized(vm) {
                    vm.notifyAll();
                }
            } catch (Exception e) {
            }
            setVMStatus(DEBUG_RUNNING);
        }
    }

    public void stepOut() {
        if (vm != null) {
            debugMode = MODE_STEPOUT;
            try {
            	vm.changeDebugMode(MODE_STEPOUT);
                synchronized(vm) {
                    vm.notifyAll();
                }
            } catch (Exception e) {
            }
            setVMStatus(DEBUG_RUNNING);
        }
    }

    public void pause() {
    	if (vm != null) {
    		debugMode = MODE_STEP;
    		try {
    			vm.changeDebugMode(MODE_STEP);
    		} catch (Exception e) {
    		}
    	}
    }
    
    // 返回File, int
    public Object[] getCurrentLine() {
        if (getVMStatus() != DEBUG_BREAKED) {
            return null;
        }
        GTLDebugSession ss;
        ETFDebugInfo dinfo;
        int curFunc;
        if ((vm.currentFunc & 0xF000) == 0) {
            ss = this;
            curFunc = vm.currentFunc;
            dinfo = debugInfo;
        } else {
            int libIndex = (vm.currentFunc >> 12) & 0x0F;
            String libName = debugInfo.libraries[libIndex - 1];
            ss = debugManager.findSession(libName);
            if (ss == null) {
                return null;
            }
            dinfo = ss.debugInfo;
            curFunc = vm.currentFunc & 0xFFF;
        }
        int[] lineNums = (int[])dinfo.lineNumTable[curFunc];
        int thisNum;
        if (vm.eip >= lineNums.length) {
            thisNum = lineNums[lineNums.length - 1] + 1;
        } else {
            thisNum = lineNums[vm.eip];
        }
        Object[] ret = new Object[2];
        ret[0] = ss.getFileOfLine(thisNum);
        ret[1] = ss.getLineOfLine(thisNum);
        return ret;
    }

    /** 处理一个VM中断。使用DebugVM时，每执行一条指令都会产生一个中断。 */
    public void int3() throws Exception {
        // 只在行的第一条指令停止
        int eip = vm.eip;
        int[] lineNums;
        GTLDebugSession ss;
        if ((vm.currentFunc & 0xF000) == 0) {
            ss = this;
            lineNums = (int[])debugInfo.lineNumTable[vm.currentFunc];
        } else {
            int libIndex = (vm.currentFunc >> 12) & 0x0F;
            String libName = debugInfo.libraries[libIndex - 1];
            ss = debugManager.findSession(libName);
            if (ss == null) {
                return;
            }
            lineNums = (int[])ss.debugInfo.lineNumTable[vm.currentFunc & 0xFFF];
        }
        if (eip > 0 && eip < lineNums.length && lineNums[eip - 1] == lineNums[eip]) {
            return;
        }

        if (debugMode == MODE_STEP) {
            setVMStatus(DEBUG_BREAKED);
            synchronized (vm) {
                vm.wait();
            }
        } else {
            // 检查是否执行到断点
            int nowLineNum;
            if (eip >= lineNums.length) {
                nowLineNum = lineNums[lineNums.length - 1] + 1;
            } else {
                nowLineNum = lineNums[eip];
            }
            File f = ss.getFileOfLine(nowLineNum);
            int l = ss.getLineOfLine(nowLineNum);
            if (f != null && l != -1 && debugManager.isBreakpoint(f, l)) {
                setVMStatus(DEBUG_BREAKED);
                synchronized (vm) {
                    vm.wait();
                }
            }
        }
    }

    /** 处理新时间片开始中断。每次DebugVM开始执行一个时间片会产生int4中断。*/
    public void int4() throws Exception {
        // 只在行的第一条指令停止
        int eip = vm.eip;
        int[] lineNums;
        if ((vm.currentFunc & 0xF000) == 0) {
            lineNums = (int[])debugInfo.lineNumTable[vm.currentFunc];
        } else {
            int libIndex = (vm.currentFunc >> 12) & 0x0F;
            String libName = debugInfo.libraries[libIndex - 1];
            GTLDebugSession ss = debugManager.findSession(libName);
            if (ss == null) {
                return;
            }
            lineNums = (int[])ss.debugInfo.lineNumTable[vm.currentFunc & 0xFFF];
        }
        if (eip > 0 && eip < lineNums.length && lineNums[eip - 1] == lineNums[eip]) {
            return;
        }
        if (debugMode == MODE_STEPOVER || debugMode == MODE_STEPOUT) {
            setVMStatus(DEBUG_BREAKED);
            synchronized (vm) {
                vm.wait();
            }
        }
    }
    
    /** 处理函数上下文切换中断。当调用一个函数或者从函数返回时产生此中断。*/
    public void int5() throws Exception {
    }
    
    /** 脚本执行完成中断。*/
    public void int6() throws Exception {
    	vm.destroy();
    	vm = null;
    	setVMStatus(DEBUG_INIT);
    }
    
    /** 脚本执行错误。 */
    public void int7() throws Exception {
        setVMStatus(DEBUG_BREAKED);
        synchronized (vm) {
            vm.wait();
        }
    }

    /** 处理信息 */
    public void handleInformation(String info) {
    }
    
    public void handleHeap(String[] info, boolean[] used) {
    }
    
    public void syncOver() {
    	// 重新生成变量表
    	ArrayList<VariableItem> list = new ArrayList<VariableItem>(); 
    	Object[] arr = debugInfo.globalVariables.values().toArray();
    	for (int i = 0; i < arr.length; i++) {
    		ETFDebugInfo.VariableDef var = (ETFDebugInfo.VariableDef)arr[i];
    		VariableItem newItem = new VariableItem();
    		newItem.parent = null;
    		newItem.variableType = VariableItem.TYPE_GLOBAL;;
    		newItem.type = var.type;
    		newItem.name = var.name;
    		newItem.typeName = var.typeName;
    		newItem.address = var.address;
    		list.add(newItem);
    	}
    	
    	// 引用库的全局变量附在全局变量表的后面
    	for (int i = 0; debugInfo.libraries != null && i < debugInfo.libraries.length; i++) {
    		GTLDebugSession ss = debugManager.findSession(debugInfo.libraries[i]);
            if (ss != null) {
            	arr = ss.debugInfo.globalVariables.values().toArray();
            	for (int j = 0; j < arr.length; j++) {
            		ETFDebugInfo.VariableDef var = (ETFDebugInfo.VariableDef)arr[j];
            		VariableItem newItem = new VariableItem();
            		newItem.parent = null;
            		newItem.variableType = VariableItem.TYPE_GLOBAL;;
            		newItem.type = var.type;
            		newItem.name = var.name;
            		newItem.typeName = var.typeName;
            		newItem.address = (var.address & 0xC0000000) | list.size();
            		list.add(newItem);
            	}
            }
    	}
    	
    	ETFDebugInfo.FunctionDef fdef = null;
        if ((vm.currentFunc & 0xF000) == 0) {
            fdef = debugInfo.userFunctions.get(new Integer(vm.currentFunc));
        } else {
            int libIndex = (vm.currentFunc >> 12) & 0x0F;
            String libName = debugInfo.libraries[libIndex - 1];
            GTLDebugSession ss = debugManager.findSession(libName);
            if (ss != null) {
                fdef = ss.debugInfo.userFunctions.get(new Integer(vm.currentFunc & 0xFFF));
            }
        }
    	if (fdef != null) {
    		for (int i = 0; i < fdef.params.size(); i++) {
    			ETFDebugInfo.VariableDef var = (ETFDebugInfo.VariableDef)fdef.params.get(i);
    			VariableItem newItem = new VariableItem();
        		newItem.parent = null;
        		newItem.variableType = VariableItem.TYPE_PARAM;;
        		newItem.type = var.type;
        		newItem.name = var.name;
        		newItem.typeName = var.typeName;
        		newItem.address = var.address;
        		list.add(newItem);
    		}
    		arr = fdef.localVariables.values().toArray();
        	for (int i = 0; i < arr.length; i++) {
        		ETFDebugInfo.VariableDef var = (ETFDebugInfo.VariableDef)arr[i];
        		VariableItem newItem = new VariableItem();
        		newItem.parent = null;
        		newItem.variableType = VariableItem.TYPE_LOCAL;;
        		newItem.type = var.type;
        		newItem.name = var.name;
        		newItem.typeName = var.typeName;
        		newItem.address = var.address;
        		list.add(newItem);
        	}
    	}
    	VariableItem[] arr2 = new VariableItem[list.size()];
    	list.toArray(arr2);
    	Arrays.sort(arr2);
    	this.variables = arr2;
    	
    	debugManager.staticHeapSyncOver(this);
    }
    
    public CallStackItem[] getCallStack() {
		return callStack;
	}
    
    public VariableItem[] getVariables() {
    	return variables;
    }

	public void handleCallStack(int[][] stack) {
    	CallStackItem[] items = new CallStackItem[stack.length];
    	for (int i = 0; i < stack.length; i++) {
    		items[i] = new CallStackItem();
    		items[i].parent = this;
    		
    		int vmIndex = (stack[i][0] >> 12) & 0x0F;
    	    int vmFunc = stack[i][0] & 0xFFF;
    	    ETFDebugInfo dinfo;
    	    GTLDebugSession ss;
    	    if (vmIndex == 0) {
    	        ss = this;
    	        dinfo = debugInfo;
    	    } else {
    	        String name = debugInfo.libraries[vmIndex - 1];
    	        ss = debugManager.findSession(name);
    	        if (ss == null) {
    	            items[i].file = new File("unknown");
                    items[i].line = 0;
                    continue;
    	        } else {
    	            dinfo = debugManager.findSession(name).debugInfo;
    	        }
    	    }
    	    
			ETFDebugInfo.FunctionDef def = dinfo.userFunctions.get(vmFunc);
			items[i].function = def.name;
			int[] lineNums = (int[])dinfo.lineNumTable[vmFunc];
			if (stack[i][1] >= lineNums.length) {
				stack[i][1] = lineNums.length - 1; 
			}
			try {
				int lineNum = lineNums[stack[i][1]];
				items[i].file = ss.getFileOfLine(lineNum);
				items[i].line = ss.getLineOfLine(lineNum);
			} catch (Exception e) {
				items[i].file = new File("unknown");
				items[i].line = 0;
			}
    	}
    	callStack = items;
    	debugManager.callStackChanged(this);
    }

	public void handleAllocTrace(int[][] stack) {
    	CallStackItem[] items = new CallStackItem[stack.length];
    	for (int i = 0; i < stack.length; i++) {
    		items[i] = new CallStackItem();
    		items[i].parent = this;
    		
    		int vmIndex = (stack[i][0] >> 12) & 0x0F;
            int vmFunc = stack[i][0] & 0xFFF;
            ETFDebugInfo dinfo;
            GTLDebugSession ss;
            if (vmIndex == 0) {
                ss = this;
                dinfo = debugInfo;
            } else {
                String name = debugInfo.libraries[vmIndex - 1];
                ss = debugManager.findSession(name);
                if (ss == null) {
                    items[i].file = new File("unknown");
                    items[i].line = 0;
                    continue;
                } else {
                    dinfo = debugManager.findSession(name).debugInfo;
                }
            }
    		
			ETFDebugInfo.FunctionDef def = dinfo.userFunctions.get(vmFunc);
			items[i].function = def.name;
			int[] lineNums = (int[])dinfo.lineNumTable[vmFunc];
			if (stack[i][1] >= lineNums.length) {
				stack[i][1] = lineNums.length - 1; 
			}
			try {
                int lineNum = lineNums[stack[i][1]];
                items[i].file = ss.getFileOfLine(lineNum);
                items[i].line = ss.getLineOfLine(lineNum);
            } catch (Exception e) {
                items[i].file = new File("unknown");
                items[i].line = 0;
            }
    	}
    	debugManager.showAllocTrace(items);
    }
	
	public CallStackItem getLineInfo(int funcId, int eip) {
	    int vmIndex = (funcId >> 12) & 0x0F;
        int vmFunc = funcId & 0xFFF;
        ETFDebugInfo dinfo;
        GTLDebugSession ss;
        if (vmIndex == 0) {
            ss = this;
            dinfo = debugInfo;
        } else {
            String name = debugInfo.libraries[vmIndex - 1];
            ss = debugManager.findSession(name);
            if (ss == null) {
                return null;
            } else {
                dinfo = debugManager.findSession(name).debugInfo;
            }
        }
        
        ETFDebugInfo.FunctionDef def = dinfo.userFunctions.get(vmFunc);
        int[] lineNums = (int[])dinfo.lineNumTable[vmFunc];
        if (eip >= lineNums.length) {
            eip = lineNums.length - 1; 
        }
        try {
            int lineNum = lineNums[eip];
            File file = ss.getFileOfLine(lineNum);
            int line = ss.getLineOfLine(lineNum);
            CallStackItem ret = new CallStackItem();
            ret.parent = null;
            ret.file = file;
            ret.line = line;
            ret.function = def.name;
            return ret;
        } catch (Exception e) {
            return null;
        }
	}
	
	public void handleAllocTrace(CallStackItem[] stack) {
        debugManager.showAllocTrace(stack);
    }
	
	private String getFuncName(int funcID) {
	    try {
    	    int vmIndex = (funcID >> 12) & 0x0F;
            int vmFunc = funcID & 0xFFF;
            ETFDebugInfo dinfo;
            GTLDebugSession ss;
            if (vmIndex == 0) {
                ss = this;
                dinfo = debugInfo;
            } else {
                String name = debugInfo.libraries[vmIndex - 1];
                ss = debugManager.findSession(name);
                if (ss == null) {
                    return "unknown";
                } else {
                    dinfo = debugManager.findSession(name).debugInfo;
                }
            }
            ETFDebugInfo.FunctionDef def = dinfo.userFunctions.get(vmFunc);
            return def.name;
	    } catch (Exception e) {
	        return "unknown";
	    }
	}
	
	public void handleFuncReport(boolean isEnter, int funcID, int execCounter) {
        if (isEnter) {
            callSequence.add(new int[] { funcID, execCounter });
        } else if (callSequence.size() > 0) {
            int[] callInfo = callSequence.lastElement();
            if (callInfo[0] == funcID) {
                String cs = getFuncStack();
                callSequence.remove(callSequence.size() - 1);
                recordFuncCall(getFuncName(funcID), cs, execCounter - callInfo[1]);
            } else {
                if (funcID == 3) {
                    // cycleUI, may be paused
                    String cs = getFuncStack();
                    while (callSequence.size() > 0 && callSequence.lastElement()[0] != 3) {
                        callSequence.remove(callSequence.size() - 1);
                    }
                    cs = getFuncStack();
                    if(callSequence.size() > 0){
                        callSequence.remove(callSequence.size() - 1);
                    }
                    recordFuncCall(getFuncName(funcID), cs, execCounter - callInfo[1]);
                } else {
                    // just ignore
                }
            }
        }
	}
	
	private String getFuncStack() {
	    StringBuilder sb = new StringBuilder();
	    for (int[] ii : callSequence) {
	        String name = getFuncName(ii[0]);
	        if (sb.length() > 0) {
	            sb.append(" -> ");
	        }
	        sb.append(name);
	    }
	    return sb.toString();
	}
	
	private void recordFuncCall(String name, String cstack, int count) {
	    if (funcStatistic.get(name) == null) {
	        HashMap<String, int[]> map = new HashMap<String, int[]>();
	        map.put(cstack , new int[] { 1, count, count, count });
	        funcStatistic.put(name, map);
	    } else {
	        HashMap<String, int[]> map = funcStatistic.get(name);
	        if (!map.containsKey(cstack)) {
	            map.put(cstack , new int[] { 1, count, count, count });
	        } else {
	            int[] info = map.get(cstack);
    	        info[0]++;
    	        info[1] += count;
    	        if (info[2] < count) {
    	            info[2] = count;
    	        }
    	        if (info[3] > count) {
    	            info[3] = count;
    	        }
	        }
	    }
	}
}
