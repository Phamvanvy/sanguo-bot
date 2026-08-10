package com.pip.server.account.util;

import java.util.HashMap;
import java.util.Iterator;

public class KeyWords {
	
	private KeyWordsState root = new KeyWordsState();
	
	public void addString(String s) {
		KeyWordsState state = root;
		for (int i = 0; i < s.length(); i++) {
			state = state.addState(new Character(s.charAt(i)));
		}
		state.finalState = true;
	}

	public void init(){
		init(root);
	}
	
	private void init(KeyWordsState state) {
		Iterator ite = state.nextState.values().iterator();
		while (ite.hasNext()) {
			KeyWordsState s1 = (KeyWordsState) ite.next();
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
	
    private int reg(HashMap map, KeyWordsState state, int n) {
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
    
    private KeyWordsState findNextNode(HashMap map, KeyWordsState state,
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
    
    public HashMap match(String target) {
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
}
