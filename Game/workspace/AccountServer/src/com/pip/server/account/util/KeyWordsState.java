package com.pip.server.account.util;

import java.util.HashMap;
import java.util.Iterator;

public class KeyWordsState {
    private int id; // just for testing purpose
    private static int availableId = 0;

    // the parent node
    KeyWordsState parent = null;
    // the target stage if the current character failed.
    KeyWordsState failState = null;
    // If the stage is the final
    boolean finalState = false;
    HashMap nextState = new HashMap();
    Character character = null;

    public KeyWordsState() {
        id = availableId++;
    }

    public KeyWordsState(Character c) {
        id = availableId++;
        character = c;
    }


    KeyWordsState addState(Character c) {
        Object obj = nextState.get(c);
        if (obj == null) {
            KeyWordsState s = new KeyWordsState(c);
            s.parent = this;
            nextState.put(c, s);
            return s;
        }
        return (KeyWordsState)obj;
    }

    KeyWordsState getState(Character c) {
        Object obj = nextState.get(c);
        if (obj == null) {
            return null;
        }
        return (KeyWordsState)obj;
    }


    public String debug(String s) {
        StringBuffer buf = new StringBuffer();
        buf.append(s);
        buf.append("+--(");
        buf.append(id);
        buf.append(")[");
        buf.append(this.character);
        buf.append(", ");
        KeyWordsState s1 = this.failState;
        if (s1 == null) {
            buf.append("null]\n");
        } else {
            buf.append(s1.id);
            buf.append("]\n");
        }
        Iterator it = this.nextState.values().iterator();
        s += "|  ";
        while (it.hasNext()) {
            s1 = (KeyWordsState)it.next();
            buf.append(s1.debug(s));
        }
        return buf.toString();
    }	
}
