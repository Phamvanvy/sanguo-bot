package com.pip.gtl.remotedebugger;

import java.io.File;

public class CallStackItem {
	public GTLDebugSession parent;
	public File file;
	public int line;
	public String function;
	
	public String toString() {
		return function + "() " + file.getName() + ":" + (line + 1);
	}
	
	public boolean equals(Object o) {
		if (o == null || !(o instanceof CallStackItem)) {
			return false;
		}
		CallStackItem oo = (CallStackItem)o;
		return parent == oo.parent && file.equals(oo.file) && line == oo.line;
	}
}
