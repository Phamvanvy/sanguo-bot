package com.pip.dispatch;


import org.apache.mina.common.IoSession;
import org.apache.mina.common.IoService;
import org.apache.mina.common.IoServiceConfig;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSessionConfig;
import org.apache.mina.common.IoFilterChain;
import org.apache.mina.common.WriteFuture;
import org.apache.mina.common.CloseFuture;
import java.util.Set;
import org.apache.mina.common.TransportType;
import java.net.SocketAddress;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.TrafficMask;
import org.apache.mina.common.ByteBuffer;


public class SingleConnectSession implements IoSession{

    private int sessionId;
    private IoSession session;

    public SingleConnectSession(IoSession session, int sessionId){
        this.session = session;
        this.sessionId = sessionId;
    }

    public int getSessionId(){
        return sessionId;
    }

    public IoSession getUnderlyingSession() {
    	return session;
    }
    
    public int getFullSessionID() {
    	int proxyID = ((Integer)session.getAttribute(SingleSocketDispatcher.PROXYID)).intValue();
    	return (proxyID << 24) | sessionId;
    }

    /**
     * getService
     *
     * @return IoService
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public IoService getService(){
        return null;
    }

    /**
     * getServiceConfig
     *
     * @return IoServiceConfig
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public IoServiceConfig getServiceConfig(){
        return null;
    }

    /**
     * getHandler
     *
     * @return IoHandler
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public IoHandler getHandler(){
        return null;
    }

    /**
     * getConfig
     *
     * @return IoSessionConfig
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public IoSessionConfig getConfig(){
        return null;
    }

    /**
     * getFilterChain
     *
     * @return IoFilterChain
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public IoFilterChain getFilterChain(){
        return null;
    }

    /**
     * write
     *
     * @param object Object
     * @return WriteFuture
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public WriteFuture write(Object object){
        if(session != null){
            byte[] bytes = ((ByteBuffer)object).array();
            byte[] bytes1 = new byte[bytes.length];
            System.arraycopy(bytes, 0, bytes1, 0, bytes.length);
            ByteBuffer buf = ByteBuffer.wrap(bytes1);
            buf.putInt(5, sessionId);
            return session.write(buf);
        }
        return null;
    }

    /**
     * close
     *
     * @return CloseFuture
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public CloseFuture close(){
        return null;
    }

    /**
     * getAttachment
     *
     * @return Object
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public Object getAttachment(){
        return null;
    }

    /**
     * setAttachment
     *
     * @param object Object
     * @return Object
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public Object setAttachment(Object object){
        return null;
    }

    /**
     * getAttribute
     *
     * @param string String
     * @return Object
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public Object getAttribute(String string){
        return null;
    }

    /**
     * setAttribute
     *
     * @param string String
     * @param object Object
     * @return Object
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public Object setAttribute(String string, Object object){
        return null;
    }

    /**
     * setAttribute
     *
     * @param string String
     * @return Object
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public Object setAttribute(String string){
        return null;
    }

    /**
     * removeAttribute
     *
     * @param string String
     * @return Object
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public Object removeAttribute(String string){
        return null;
    }

    /**
     * containsAttribute
     *
     * @param string String
     * @return boolean
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public boolean containsAttribute(String string){
        return false;
    }

    /**
     * getAttributeKeys
     *
     * @return Set
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public Set getAttributeKeys(){
        return null;
    }

    /**
     * getTransportType
     *
     * @return TransportType
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public TransportType getTransportType(){
        return null;
    }

    /**
     * isConnected
     *
     * @return boolean
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public boolean isConnected(){
        return false;
    }

    /**
     * isClosing
     *
     * @return boolean
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public boolean isClosing(){
        return false;
    }

    /**
     * getCloseFuture
     *
     * @return CloseFuture
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public CloseFuture getCloseFuture(){
        return null;
    }

    /**
     * getRemoteAddress
     *
     * @return SocketAddress
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public SocketAddress getRemoteAddress(){
        return null;
    }

    /**
     * getLocalAddress
     *
     * @return SocketAddress
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public SocketAddress getLocalAddress(){
        return null;
    }

    /**
     * getServiceAddress
     *
     * @return SocketAddress
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public SocketAddress getServiceAddress(){
        return null;
    }

    /**
     * getIdleTime
     *
     * @param idleStatus IdleStatus
     * @return int
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public int getIdleTime(IdleStatus idleStatus){
        return 0;
    }

    /**
     * getIdleTimeInMillis
     *
     * @param idleStatus IdleStatus
     * @return long
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public long getIdleTimeInMillis(IdleStatus idleStatus){
        return 0L;
    }

    /**
     * setIdleTime
     *
     * @param idleStatus IdleStatus
     * @param _int int
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public void setIdleTime(IdleStatus idleStatus, int _int){
    }

    /**
     * getWriteTimeout
     *
     * @return int
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public int getWriteTimeout(){
        return 0;
    }

    /**
     * getWriteTimeoutInMillis
     *
     * @return long
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public long getWriteTimeoutInMillis(){
        return 0L;
    }

    /**
     * setWriteTimeout
     *
     * @param _int int
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public void setWriteTimeout(int _int){
    }

    /**
     * getTrafficMask
     *
     * @return TrafficMask
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public TrafficMask getTrafficMask(){
        return null;
    }

    /**
     * setTrafficMask
     *
     * @param trafficMask TrafficMask
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public void setTrafficMask(TrafficMask trafficMask){
    }

    /**
     * suspendRead
     *
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public void suspendRead(){
    }

    /**
     * suspendWrite
     *
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public void suspendWrite(){
    }

    /**
     * resumeRead
     *
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public void resumeRead(){
    }

    /**
     * resumeWrite
     *
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public void resumeWrite(){
    }

    /**
     * getReadBytes
     *
     * @return long
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public long getReadBytes(){
        return 0L;
    }

    /**
     * getWrittenBytes
     *
     * @return long
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public long getWrittenBytes(){
        return 0L;
    }

    /**
     * getReadMessages
     *
     * @return long
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public long getReadMessages(){
        return 0L;
    }

    /**
     * getWrittenMessages
     *
     * @return long
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public long getWrittenMessages(){
        return 0L;
    }

    /**
     * getWrittenWriteRequests
     *
     * @return long
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public long getWrittenWriteRequests(){
        return 0L;
    }

    /**
     * getScheduledWriteRequests
     *
     * @return int
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public int getScheduledWriteRequests(){
        return 0;
    }

    /**
     * getScheduledWriteBytes
     *
     * @return int
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public int getScheduledWriteBytes(){
        return 0;
    }

    /**
     * getCreationTime
     *
     * @return long
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public long getCreationTime(){
        return 0L;
    }

    /**
     * getLastIoTime
     *
     * @return long
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public long getLastIoTime(){
        return 0L;
    }

    /**
     * getLastReadTime
     *
     * @return long
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public long getLastReadTime(){
        return 0L;
    }

    /**
     * getLastWriteTime
     *
     * @return long
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public long getLastWriteTime(){
        return 0L;
    }

    /**
     * isIdle
     *
     * @param idleStatus IdleStatus
     * @return boolean
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public boolean isIdle(IdleStatus idleStatus){
        return false;
    }

    /**
     * getIdleCount
     *
     * @param idleStatus IdleStatus
     * @return int
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public int getIdleCount(IdleStatus idleStatus){
        return 0;
    }

    /**
     * getLastIdleTime
     *
     * @param idleStatus IdleStatus
     * @return long
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public long getLastIdleTime(IdleStatus idleStatus){
        return 0L;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj the reference object with which to compare.
     * @return <code>true</code> if this object is the same as the obj argument; <code>false</code> otherwise.
     * @todo Implement this java.lang.Object method
     */
    public boolean equals(Object obj){
        return sessionId == ((SingleConnectSession)obj).sessionId;
    }

}
