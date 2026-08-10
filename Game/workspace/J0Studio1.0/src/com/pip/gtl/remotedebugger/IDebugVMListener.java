package com.pip.gtl.remotedebugger;

public interface IDebugVMListener {
    /** 处理一个VM中断。使用DebugVM时，每执行一条指令都会产生一个中断。 */
    void int3() throws Exception;

    /** 处理VM接口开始中断。每次VM调用一个VM接口时都会产生这个中断。*/
    void int4() throws Exception;
    
    /** 处理函数上下文切换中断。当调用一个函数或者从函数返回时产生此中断。*/
    void int5() throws Exception;
    
    /** 脚本执行完成中断。*/
    void int6() throws Exception;
    
    /** 脚本执行出错。*/
    void int7() throws Exception;
    
    /** 处理信息 */
    void handleInformation(String info);
    
    /** 处理堆信息 */
    void handleHeap(String[] info, boolean[] used);
    
    /** 处理调用栈信息 */
    void handleCallStack(int[][] stack);
    
    /** 内存状态同步完成 */
    void syncOver();
    
    /** 处理内存分配追踪信息 */
    void handleAllocTrace(int[][] stack);
    
    /** 得到一个运行地址对应的文件名和行号信息，参数：函数ID、函数内EIP */
    CallStackItem getLineInfo(int funcID, int eip);
    
    /** 处理内存分配追踪信息 */
    void handleAllocTrace(CallStackItem[] stack);
    
    /** 处理函数调用统计信息 */
    void handleFuncReport(boolean isEnter, int funcID, int execCounter);
}
