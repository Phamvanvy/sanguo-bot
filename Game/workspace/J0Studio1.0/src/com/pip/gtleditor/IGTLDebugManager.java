package com.pip.gtleditor;

public interface IGTLDebugManager {
	boolean isBreakpoint(String file, int lineNum);
	boolean isActiveLine(String file, int lineNum);
	void toggleBreakpoint(String file, int lineNum);
	int[] getHightlightLines(String file);
}
