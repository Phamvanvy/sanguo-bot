package com.pip.itimes.server.stage;

/**
 * @author Jeffrey
 * @version 1.0
 */
public interface Attachment {
    public long getSendTime();
    public byte[] toDbBytes();
}
