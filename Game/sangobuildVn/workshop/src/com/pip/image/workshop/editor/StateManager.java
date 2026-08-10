package com.pip.image.workshop.editor;

import java.util.*;

public class StateManager {
    protected List<byte[]> buffer = new ArrayList<byte[]>();
    protected int capacity;
    protected int size;
    protected int current = -1;
    
    public StateManager(int capacity) {
        this.capacity = capacity;
    }
    
    public void push(byte[] data) {
        if (current >= 0 && Arrays.equals(data, buffer.get(current))) {
            return;
        }
        while (buffer.size() > current + 1) {
            size -= buffer.remove(buffer.size() - 1).length;
        }
        while (size > 0 && buffer.size() > 0 && size + data.length > capacity) {
            size -= buffer.remove(0).length;
        }
        buffer.add(data);
        current = buffer.size() - 1;
        size += data.length;
    }
    
    public boolean canUndo() {
        return current > 0;
    }
    
    public byte[] getUndoData() {
        return buffer.get(--current);
    }
    
    public boolean canRedo() {
        return current < buffer.size() - 1;
    }
    
    public byte[] getRedoData() {
        return buffer.get(++current);
    }
}
