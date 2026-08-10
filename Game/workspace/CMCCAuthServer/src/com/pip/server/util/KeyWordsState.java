package com.pip.server.util;


import java.util.*;

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
    public static KeyWordsState root = new KeyWordsState();

    public KeyWordsState() {
        id = availableId++;
    }

    public KeyWordsState(Character c) {
        id = availableId++;
        character = c;
    }

    /** to add one new string into the data structure */
    public static void addString(String s) {
        KeyWordsState state = root;
        for (int i = 0; i < s.length(); i++) {
            state = state.addState(new Character(s.charAt(i)));
        }
        state.finalState = true;
    }

    /** to init the state */
    public static void init() {
        init(root);
    }

    public static HashMap match(String target) {
        HashMap map = new HashMap();
        int i = 0;
        int n = target.length();
        KeyWordsState state = root;
        int count = 0;

        while (i < n) {
            count++;
            Character c = new Character(target.charAt(i));
            KeyWordsState nS = state.getState(c);
            if (nS == null) {
                if (state == root) {
                    i++;
                } else {
                    int temp = reg(map, state, i);
                    if (temp == -1) {
                        state = state.failState;
                    } else {
                        i = temp;
                        state = root;
                        c = new Character(target.charAt(i));
                    }
                    i++;
                    state = findNextNode(map, state, c, i);
                }
            } else {
                i++;
                state = nS;
            }
        }
        reg(map, state, n);
        return map;
    }

    private static KeyWordsState findNextNode(HashMap map, KeyWordsState state,
                                              Character c, int i) {
        KeyWordsState tempState = state.getState(c);
        if (tempState == null) {
            if (state == root) {
                return state;
            }
            reg(map, state, i);
            state = state.failState;
            return findNextNode(map, state, c, i);
        } else {
            return tempState;
        }
    }

    private KeyWordsState addState(Character c) {
        Object obj = nextState.get(c);
        if (obj == null) {
            KeyWordsState s = new KeyWordsState(c);
            s.parent = this;
            nextState.put(c, s);
            return s;
        }
        return (KeyWordsState)obj;
    }

    private KeyWordsState getState(Character c) {
        Object obj = nextState.get(c);
        if (obj == null) {
            return null;
        }
        return (KeyWordsState)obj;
    }

    private static int reg(HashMap map, KeyWordsState state, int n) {
        int t = 0;
        while (state != root) {
            if (state.finalState) {
                t = n;
                String s = "";
                while (state != root) {
                    s = state.character + s;
                    state = state.parent;
                    n--;
                }
                map.put(new Integer(n), new Integer(s.length()));
                return t;
            }
            state = state.parent;
            n--;
        }
        return -1;
    }

    private static void init(KeyWordsState state) {
        Iterator ite = state.nextState.values().iterator();
        while (ite.hasNext()) {
            KeyWordsState s1 = (KeyWordsState)ite.next();
            KeyWordsState s2 = state.failState;
            while (true) {
                if (s2 == null) {
                    s1.failState = root;
                    break;
                }
                KeyWordsState s3 = s2.getState(s1.character);
                if (s3 != null) {
                    s1.failState = s3;
                    break;
                }
                s2 = s2.failState;
            }
            init(s1);
        }
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
