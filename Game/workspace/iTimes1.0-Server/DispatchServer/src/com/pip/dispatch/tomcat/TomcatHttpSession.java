package com.pip.dispatch.tomcat;

import java.net.*;
import java.util.*;

import org.apache.mina.common.*;

/**
 * 一个用户连接会话。
 * @author lighthu
 */
public class TomcatHttpSession implements IoSession {
    /*
     * 待发的消息包。
     */
    private List messages = Collections.synchronizedList(new LinkedList());

    private TomcatHttpAcceptor acceptor;
    SocketAddress address;

    private long lastReadTime = 0;

    private boolean isClosing = false;

    private int sessionId;


    public TomcatHttpSession(TomcatHttpAcceptor acceptor,SocketAddress address,int sessionId){
        this.acceptor = acceptor;
        this.address = address;
        lastReadTime = System.currentTimeMillis();
        this.sessionId = sessionId;
    }

    /**
     * Returns the {@link IoService} which provides I/O service to this
     * session.
     *
     * @return IoService
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public IoService getService() {
        return null;
    }

    /**
     * Returns the {@link IoServiceConfig} of this session.
     *
     * @return IoServiceConfig
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public IoServiceConfig getServiceConfig() {
        return null;
    }

    /**
     * Returns the {@link IoHandler} which handles this session.
     *
     * @return IoHandler
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public IoHandler getHandler() {
        return null;
    }

    /**
     * Returns the configuration of this session.
     *
     * @return IoSessionConfig
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public IoSessionConfig getConfig() {
        return null;
    }

    /**
     * Returns the filter chain that only affects this session.
     *
     * @return IoFilterChain
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public IoFilterChain getFilterChain() {
        return null;
    }

    /**
     * Writes the specified <code>message</code> to remote peer.
     *
     * @param message Object
     * @return WriteFuture
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public WriteFuture write(Object message) {
        synchronized (this) {
            messages.add(message);
            if(messages.size()>=3){
                notify();
            }
        }
        return null;
    }

    public ByteBuffer[] getSegments(){
        synchronized(this){
            ByteBuffer[] ret = new ByteBuffer[messages.size()];
            messages.toArray(ret);
            messages.clear();
            return ret;
        }
    }

    /**
     * Closes this session immediately.
     *
     * @return CloseFuture
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public CloseFuture close() {
        isClosing = true;
        acceptor.notifyClose(this);
        return null;
    }

    /**
     * Returns an attachment of this session.
     *
     * @return Object
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public Object getAttachment() {
        return null;
    }

    /**
     * Sets an attachment of this session.
     *
     * @return Old attachment. <tt>null</tt> if it is new.
     * @param attachment Object
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public Object setAttachment(Object attachment) {
        return null;
    }

    /**
     * Returns the value of user-defined attribute of this session.
     *
     * @param key the key of the attribute
     * @return <tt>null</tt> if there is no attribute with the specified key
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public Object getAttribute(String key) {
        return null;
    }

    /**
     * Sets a user-defined attribute.
     *
     * @param key the key of the attribute
     * @param value the value of the attribute
     * @return The old value of the attribute. <tt>null</tt> if it is new.
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public Object setAttribute(String key, Object value) {
        return null;
    }

    /**
     * Sets a user defined attribute without a value.
     *
     * @param key the key of the attribute
     * @return The old value of the attribute. <tt>null</tt> if it is new.
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public Object setAttribute(String key) {
        return null;
    }

    /**
     * Removes a user-defined attribute with the specified key.
     *
     * @return The old value of the attribute. <tt>null</tt> if not found.
     * @param key String
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public Object removeAttribute(String key) {
        return null;
    }

    /**
     * Returns <tt>true</tt> if this session contains the attribute with the
     * specified <tt>key</tt>.
     *
     * @param key String
     * @return boolean
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public boolean containsAttribute(String key) {
        return false;
    }

    /**
     * Returns the set of keys of all user-defined attributes.
     *
     * @return Set
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public Set getAttributeKeys() {
        return null;
    }

    /**
     * Returns transport type of this session.
     *
     * @return TransportType
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public TransportType getTransportType() {
        return null;
    }

    /**
     * Returns <code>true</code> if this session is connected with remote
     * peer.
     *
     * @return boolean
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public boolean isConnected() {
        return false;
    }

    /**
     * Returns <code>true</tt> if and only if this session is being closed
     * (but not disconnected yet) or is closed.
     *
     * @return boolean
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public boolean isClosing() {
        return isClosing;
    }

    /**
     * Returns the {@link CloseFuture} of this session.
     *
     * @return CloseFuture
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public CloseFuture getCloseFuture() {
        return null;
    }

    /**
     * Returns the socket address of remote peer.
     *
     * @return SocketAddress
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public SocketAddress getRemoteAddress() {
        return address;
    }

    /**
     * Returns the socket address of local machine which is associated with
     * this session.
     *
     * @return SocketAddress
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public SocketAddress getLocalAddress() {
        return null;
    }

    /**
     * Returns the socket address of the {@link IoService} listens to to
     * manage this session.
     *
     * @return SocketAddress
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public SocketAddress getServiceAddress() {
        return null;
    }

    /**
     * Returns idle time for the specified type of idleness in seconds.
     *
     * @param status IdleStatus
     * @return int
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public int getIdleTime(IdleStatus status) {
        return 0;
    }

    /**
     * Returns idle time for the specified type of idleness in milliseconds.
     *
     * @param status IdleStatus
     * @return long
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public long getIdleTimeInMillis(IdleStatus status) {
        return 0L;
    }

    /**
     * Sets idle time for the specified type of idleness in seconds.
     *
     * @param status IdleStatus
     * @param idleTime int
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public void setIdleTime(IdleStatus status, int idleTime) {
    }

    /**
     * Returns write timeout in seconds.
     *
     * @return int
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public int getWriteTimeout() {
        return 0;
    }

    /**
     * Returns write timeout in milliseconds.
     *
     * @return long
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public long getWriteTimeoutInMillis() {
        return 0L;
    }

    /**
     * Sets write timeout in seconds.
     *
     * @param writeTimeout int
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public void setWriteTimeout(int writeTimeout) {
    }

    /**
     * Returns the current {@link TrafficMask} of this session.
     *
     * @return TrafficMask
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public TrafficMask getTrafficMask() {
        return null;
    }

    /**
     * Sets the {@link TrafficMask} of this session which will result the
     * parent {@link IoService} to start to control the traffic of this
     * session immediately.
     *
     * @param trafficMask TrafficMask
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public void setTrafficMask(TrafficMask trafficMask) {
    }

    /**
     * A shortcut method for {@link #setTrafficMask(TrafficMask)} that
     * suspends read operations for this session.
     *
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public void suspendRead() {
    }

    /**
     * A shortcut method for {@link #setTrafficMask(TrafficMask)} that
     * suspends write operations for this session.
     *
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public void suspendWrite() {
    }

    /**
     * A shortcut method for {@link #setTrafficMask(TrafficMask)} that
     * resumes read operations for this session.
     *
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public void resumeRead() {
    }

    /**
     * A shortcut method for {@link #setTrafficMask(TrafficMask)} that
     * resumes write operations for this session.
     *
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public void resumeWrite() {
    }

    /**
     * Returns the total number of bytes which were read from this session.
     *
     * @return long
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public long getReadBytes() {
        return 0L;
    }

    /**
     * Returns the total number of bytes which were written to this session.
     *
     * @return long
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public long getWrittenBytes() {
        return 0L;
    }

    /**
     * Returns the total number of messages which were read and decoded from
     * this session.
     *
     * @return long
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public long getReadMessages() {
        return 0L;
    }

    /**
     * Returns the total number of messages which were written and encoded by
     * this session.
     *
     * @return long
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public long getWrittenMessages() {
        return 0L;
    }

    /**
     * Returns the total number of write requests which were written to this
     * session.
     *
     * @return long
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public long getWrittenWriteRequests() {
        return 0L;
    }

    /**
     * Returns the number of write requests which are scheduled to be written
     * to this session.
     *
     * @return int
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public int getScheduledWriteRequests() {
        return 0;
    }

    /**
     * Returns the number of bytes which are scheduled to be written to this
     * session.
     *
     * @return int
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public int getScheduledWriteBytes() {
        return 0;
    }

    /**
     * Returns the time in millis when this session is created.
     *
     * @return long
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public long getCreationTime() {
        return 0L;
    }

    /**
     * Returns the time in millis when I/O occurred lastly.
     *
     * @return long
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public long getLastIoTime() {
        return 0L;
    }

    public void setLastReadTime(long time){
        this.lastReadTime = time;
    }

    /**
     * Returns the time in millis when read operation occurred lastly.
     *
     * @return long
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public long getLastReadTime() {
        return lastReadTime;
    }

    public int getSessionId() {
        return sessionId;
    }

    /**
     * Returns the time in millis when write operation occurred lastly.
     *
     * @return long
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public long getLastWriteTime() {
        return 0L;
    }

    /**
     * Returns <code>true</code> if this session is idle for the specified
     * {@link IdleStatus}.
     *
     * @param status IdleStatus
     * @return boolean
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public boolean isIdle(IdleStatus status) {
        return false;
    }

    /**
     * Returns the number of the fired continuous <tt>sessionIdle</tt> events
     * for the specified {@link IdleStatus}.
     *
     * @param status IdleStatus
     * @return int
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public int getIdleCount(IdleStatus status) {
        return 0;
    }

    /**
     * Returns the time in millis when the last <tt>sessionIdle</tt> event is
     * fired for the specified {@link IdleStatus}.
     *
     * @param status IdleStatus
     * @return long
     * @todo Implement this org.apache.mina.common.IoSession method
     */
    public long getLastIdleTime(IdleStatus status) {
        return 0L;
    }

}
